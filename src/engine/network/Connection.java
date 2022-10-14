package sekelsta.engine.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import sekelsta.engine.Log;

public class Connection {
    public static final int BUFFER_SIZE = 1024;
    private static final int[] retryWaitsMillis = new int[] {250, 500, 1000, 2000, 3000, 4000, 4000};
    private static Timer retryTimer;

    private InetSocketAddress socketAddress;
    private int sequenceNumber = 0;

    private PacketHeader header;
    private ByteVector buffer;
    private Map<DatagramPacket, PacketHeader> readyPackets = new HashMap<>();
    private Map<Integer, DatagramPacket> resendingPackets = new ConcurrentHashMap<>();
    private SortedSet<Integer> receivedPacketIDs = new TreeSet<>();

    private static class PacketHeader {
        public final int packetID;
        private boolean reliable = false;
        public final Set<Integer> packetIDsToAck = new HashSet<>();

        public PacketHeader(int packetID) {
            this.packetID = packetID;
        }

        public int sizeInBytes() {
            // packetID, reliable, packetIDsToAck.size(), each item in packetIDsToAck
            return Integer.BYTES + 1 + Integer.BYTES + packetIDsToAck.size() * Integer.BYTES;
        }

        public void updateReliable(Message message) {
            this.reliable = this.reliable || message.reliable();
        }

        public boolean isReliable() {
            return reliable;
        }
    }

    private class RetryTask extends TimerTask {
        private int retriesSent;
        private int packetID;
        private DatagramSocket socket;

        public RetryTask(int packetID, DatagramSocket socket) {
            this(0, packetID, socket);
        }

        private RetryTask(int retriesSent, int packetID, DatagramSocket socket) {
            this.retriesSent = retriesSent;
            this.packetID = packetID;
            this.socket = socket;
        }

        @Override
        public void run() {
            DatagramPacket packet = resendingPackets.get(packetID);
            if (packet == null) {
                return;
            }
            if (socket.isClosed()) {
                return;
            }

            try {
                socket.send(packet);
                retriesSent += 1;
                if (retriesSent < retryWaitsMillis.length) {
                    retryTimer.schedule(new RetryTask(retriesSent, packetID, socket), retryWaitsMillis[retriesSent]);
                }
                else {
                    Log.debug("No ACK received for packet ID " + packetID + " after " + retriesSent + " retries");
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Connection(InetSocketAddress address) {
        this.socketAddress = address;
        if (retryTimer == null) {
            retryTimer = new Timer("network_retry_thread", true);
        }
        // Don't mark packet 0 as a duplicate if packet 1 arrives first
        receivedPacketIDs.add(-1);
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    // Deliberately package-private
    void queueMessage(MessageRegistry registry, Message message) {
        if (buffer == null) {
            buffer = new ByteVector(BUFFER_SIZE);
        }
        prepareHeader();

        int start = buffer.position();
        int type = registry.getMessageType(message);
        buffer.putInt(type);
        message.encode(buffer);

        // If this message takes us past the max size, send all previously queued messages for this recipient
        if (overfilled()) {
            preparePacket(start);
            prepareHeader();
        }
        if (overfilled()) {
            // TO_LATER_DO: Handle case where a single message is beyond the max size
            throw new RuntimeException("Not yet implemented");
        }

        header.updateReliable(message);
    }

    private synchronized boolean overfilled() {
        if (header == null) {
            if (buffer != null) {
                assert(buffer.position() == 0);
            }
            return false;
        }
        return buffer.position() + header.sizeInBytes() > BUFFER_SIZE;
    }

    private synchronized void prepareHeader() {
        if (header == null) {
            header = new PacketHeader(sequenceNumber);
            sequenceNumber += 1;
        }
    }

    // Beware, this may be called from a different thread than everything else
    public synchronized boolean readPacketHeader(ByteBuffer packetData) {
        prepareHeader();
        if (header.sizeInBytes() + Integer.BYTES > BUFFER_SIZE) {
            preparePacket();
        }
        prepareHeader();
        int seq = packetData.getInt();
        boolean reliable = packetData.get() != 0;
        if (reliable) {
            header.packetIDsToAck.add(seq);
        }
        if ((receivedPacketIDs.size() > 0 && seq < receivedPacketIDs.first()) 
                || receivedPacketIDs.contains(seq)) {
            return false;
        }
        receivedPacketIDs.add(seq);

        int numAcks = packetData.getInt();
        for (int i = 0; i < numAcks; ++i) {
            int acked = packetData.getInt();
            resendingPackets.remove(acked);
        }
        return true;
    }

    public void flush(DatagramSocket socket) throws IOException {
        preparePacket();

        for (Map.Entry<DatagramPacket, PacketHeader> entry : readyPackets.entrySet()) {
            DatagramPacket packet = entry.getKey();
            socket.send(packet);
            PacketHeader h = entry.getValue();
            if (h.isReliable()) {
                resendingPackets.put(h.packetID, packet);
                retryTimer.schedule(new RetryTask(h.packetID, socket), retryWaitsMillis[0]);
            }
        }
        readyPackets.clear();

        // Clean up receivedPacketIDs so as not to use an ever increasing amount of memory
        if (receivedPacketIDs.size() == 0) {
            return;
        }
        // 27000 = 75 packets per tick * 24 ticks per second * 15 seconds
        final int MAX_PACKET_DISTANCE = 27000;
        int minElement = receivedPacketIDs.last() - MAX_PACKET_DISTANCE;
        receivedPacketIDs = receivedPacketIDs.tailSet(minElement);
        minElement = Math.max(minElement, receivedPacketIDs.first());
        while (receivedPacketIDs.size() > 1 && receivedPacketIDs.contains(minElement + 1)) {
            receivedPacketIDs.remove(minElement);
            minElement += 1;
        }
    }

    public static void closeAll() {
        if (retryTimer != null) {
            retryTimer.cancel();
            retryTimer = null;
        }
    }

    private synchronized void preparePacket() {
        if (buffer != null && buffer.position() > 0) {
            preparePacket(buffer.position());
        }
        else if (header != null) {
            preparePacket(0);
        }
    }

    private synchronized void preparePacket(int length) {
        ByteBuffer packetBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        packetBuffer.putInt(header.packetID);
        packetBuffer.put((byte)(header.isReliable()? 1 : 0));
        packetBuffer.putInt(header.packetIDsToAck.size());
        for (int num : header.packetIDsToAck) {
            packetBuffer.putInt(num);
        }

        if (buffer != null && length > 0) {
            packetBuffer.put(buffer.array(), 0, length);
        }
        DatagramPacket packet = new DatagramPacket(packetBuffer.array().clone(), packetBuffer.position(), 
            socketAddress.getAddress(), socketAddress.getPort());
        readyPackets.put(packet, header);
        header = null;
        if (buffer != null) {
            buffer.limit(buffer.position());
            buffer.position(length);
            buffer.compact();
        }
    }
}

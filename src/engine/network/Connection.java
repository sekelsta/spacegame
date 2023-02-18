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
    // We initialize with a fake -1 id
    private static final int WRAP_POINT = -2;
    private static final long TIMEOUT_NANOS = 16000000000L;
    private static final int[] retryWaitsMillis = new int[] {250, 500, 750, 1000, 1500, 2000, 2000, 2000, 2000};
    private static Timer retryTimer;

    private InetSocketAddress socketAddress;
    private final long connectionID;
    protected int sequenceNumber = 0;
    private long aliveTime = System.nanoTime();

    protected PacketHeader header;
    private ByteVector buffer;
    private Map<DatagramPacket, PacketHeader> readyPackets = new HashMap<>();
    private Map<Integer, DatagramPacket> resendingPackets = new ConcurrentHashMap<>();
    private SortedSet<Integer> receivedPacketIDs = new TreeSet<>();

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
            synchronized (resendingPackets) {
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
                        resendingPackets.remove(packetID);
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public Connection(InetSocketAddress address, boolean isClient) {
        this.socketAddress = address;
        this.connectionID = address.hashCode();
        if (retryTimer == null) {
            retryTimer = new Timer("network_retry_thread", true);
        }
        // Don't mark packet 0 as a duplicate if packet 1 arrives first
        receivedPacketIDs.add(-1);
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public long getID() {
        return connectionID;
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

    protected synchronized void prepareHeader() {
        if (header == null) {
            header = new PacketHeader(sequenceNumber);
            sequenceNumber += 1;
        }
    }

    // Beware, this may be called from a different thread than everything else
    public synchronized MessageContext processPacketHeader(ByteBuffer packetData) {
        prepareHeader();
        if (header.sizeInBytes() + Integer.BYTES > BUFFER_SIZE) {
            preparePacket();
            prepareHeader();
        }
        PacketHeader inHeader = null;
        try {
            inHeader = new PacketHeader(packetData);
        }
        catch (MessageParsingException e) {
            return null;
        }
        if (inHeader.isReliable()) {
            header.packetIDsToAck.add(inHeader.packetID);
        }
        if (inHeader.packetID < WRAP_POINT && receivedPacketIDs.first() > WRAP_POINT) {
            // Because we set it to its own tail, we aren't allowed to insert elements below the min value anymore
            SortedSet<Integer> oldSet = receivedPacketIDs;
            receivedPacketIDs = new TreeSet<>();
            receivedPacketIDs.addAll(oldSet);
        }
        else if ((receivedPacketIDs.size() > 0 && inHeader.packetID < receivedPacketIDs.first()) 
                || receivedPacketIDs.contains(inHeader.packetID)) {
            // Skip duplicate packet
            return null;
        }
        receivedPacketIDs.add(inHeader.packetID);

        synchronized (resendingPackets) {
            for (int acked : inHeader.packetIDsToAck) {
                resendingPackets.remove(acked);
            }
        }
        return inHeader.context;
    }

    public synchronized void flush(DatagramSocket socket) throws IOException {
        preparePacket();

        for (Map.Entry<DatagramPacket, PacketHeader> entry : readyPackets.entrySet()) {
            DatagramPacket packet = entry.getKey();
            socket.send(packet);
            PacketHeader h = entry.getValue();
            if (h.isReliable()) {
                synchronized (resendingPackets) {
                    resendingPackets.put(h.packetID, packet);
                }
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
        if (receivedPacketIDs.first() < WRAP_POINT && receivedPacketIDs.last() > WRAP_POINT) {
            minElement = receivedPacketIDs.headSet(WRAP_POINT).last() - MAX_PACKET_DISTANCE;
            if (minElement < WRAP_POINT) {
                receivedPacketIDs.removeAll(receivedPacketIDs.tailSet(WRAP_POINT));
            }
            else {
                receivedPacketIDs.removeAll(receivedPacketIDs.tailSet(WRAP_POINT).headSet(minElement));
                return;
            }
        }
        receivedPacketIDs = receivedPacketIDs.tailSet(minElement);
        minElement = Math.max(minElement, receivedPacketIDs.first());
        while (receivedPacketIDs.size() > 1 && receivedPacketIDs.contains(minElement + 1)) {
            receivedPacketIDs.remove(minElement);
            minElement += 1;
        }
    }

    public void markAlive() {
        this.aliveTime = System.nanoTime();
    }

    public boolean shouldTimeOut(long currentTime) {
        return currentTime - aliveTime > TIMEOUT_NANOS;
    }

    public void close() {
        synchronized (resendingPackets) {
            resendingPackets.clear();
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
        header.write(packetBuffer);

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

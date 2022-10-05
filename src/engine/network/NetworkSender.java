package sekelsta.engine.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NetworkSender {
    public static final int BUFFER_SIZE = 1024;

    private DatagramSocket socket;
    private MessageRegistry registry;

    private Map<InetSocketAddress, ByteVector> queuedIndividualMessages = new HashMap<>();
    private ByteVector broadcastBuffer = new ByteVector(BUFFER_SIZE);

    private ArrayList<InetSocketAddress> broadcastRecipients = new ArrayList<>();

    public NetworkSender(MessageRegistry registry, DatagramSocket socket) {
        this.registry = registry;
        this.socket = socket;
    }

    public void addBroadcastRecipient(InetSocketAddress socketAddress) {
        if (!isBroadcastRecipient(socketAddress)) {
            broadcastRecipients.add(socketAddress);
        }
    }

    public boolean isBroadcastRecipient(InetSocketAddress socketAddress) {
        return broadcastRecipients.contains(socketAddress);
    }

    public void queueBroadcast(Message message) {
        queueMessage(broadcastBuffer, message);
    }

    public void queueMessage(InetSocketAddress receiver, Message message) {
        if (!queuedIndividualMessages.containsKey(receiver)) {
            queuedIndividualMessages.put(receiver, new ByteVector(BUFFER_SIZE));
        }
        queueMessage(queuedIndividualMessages.get(receiver), message);
    }

    private void queueMessage(ByteVector outBuffer, Message message) {
        int type = registry.getMessageType(message);
        outBuffer.putInt(type);
        message.encode(outBuffer);
    }

    public void flush() {
        // TO_OPTIMIZE: Break up large packets so that IP fragmentation doesn't have to do it for us
        // TO_OPTIMIZE: If small enough, the packet for everyone can be merged into the packets for specific clients
        if (broadcastBuffer.position() != 0) {
            for (InetSocketAddress connection : broadcastRecipients) {
                sendToConnection(connection, broadcastBuffer);
            }
            broadcastBuffer.clear();
        }

        for (Map.Entry<InetSocketAddress, ByteVector> entry : queuedIndividualMessages.entrySet()) {
            sendToConnection(entry.getKey(), entry.getValue());
        }
        queuedIndividualMessages.clear();
    }

    private void sendToConnection(InetSocketAddress connection, ByteVector buffer) {
        if (buffer.array().length >= 64 * 1024) {
            // TODO: Prevent this from ever, even theoretically, happening
            throw new RuntimeException("Trying to send a packet that is too large for UDP.");
        }
        DatagramPacket dataPacket = new DatagramPacket(buffer.array(), buffer.position(), connection.getAddress(), connection.getPort());
        try {
            socket.send(dataPacket);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

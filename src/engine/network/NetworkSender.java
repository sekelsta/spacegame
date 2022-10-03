package sekelsta.engine.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NetworkSender {
    public static final int BUFFER_SIZE = 1024;

    private DatagramSocket socket;
    private MessageRegistry registry;

    private Map<InetSocketAddress, ByteBuffer> queuedIndividualMessages = new HashMap<>();
    private ByteBuffer broadcastBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    private ArrayList<InetSocketAddress> broadcastRecipients = new ArrayList<>();

    public NetworkSender(MessageRegistry registry, DatagramSocket socket) {
        this.registry = registry;
        this.socket = socket;
    }

    public void addBroadcastRecipient(InetSocketAddress socketAddress) {
        broadcastRecipients.add(socketAddress);
    }

    public void queueBroadcast(Message message) {
        queueMessage(broadcastBuffer, message);
    }

    public void queueMessage(InetSocketAddress receiver, Message message) {
        if (!queuedIndividualMessages.containsKey(receiver)) {
            queuedIndividualMessages.put(receiver, ByteBuffer.allocate(BUFFER_SIZE));
        }
        queueMessage(queuedIndividualMessages.get(receiver), message);
    }

    private void queueMessage(ByteBuffer outBuffer, Message message) {
        int type = registry.getMessageType(message);
        // TODO: gracefully handle the buffer filling up
        outBuffer.putInt(type);
        message.encode(outBuffer);
    }

    public void flush() {
        if (broadcastBuffer.position() != 0) {
            for (InetSocketAddress connection : broadcastRecipients) {
                sendToConnection(connection, broadcastBuffer);
            }
            broadcastBuffer.clear();
        }

        for (Map.Entry<InetSocketAddress, ByteBuffer> entry : queuedIndividualMessages.entrySet()) {
            sendToConnection(entry.getKey(), entry.getValue());
        }
        queuedIndividualMessages.clear();
    }

    private void sendToConnection(InetSocketAddress connection, ByteBuffer buffer) {
        DatagramPacket dataPacket = new DatagramPacket(buffer.array(), buffer.position(), connection.getAddress(), connection.getPort());
        try {
            socket.send(dataPacket);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

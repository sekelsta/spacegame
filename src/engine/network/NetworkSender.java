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

    public void removeBroadcastRecipient(InetSocketAddress socketAddress) {
        broadcastRecipients.remove(socketAddress);
    }

    public boolean isBroadcastRecipient(InetSocketAddress socketAddress) {
        return broadcastRecipients.contains(socketAddress);
    }

    public void queueBroadcast(Message message) {
        // TO_OPTIMIZE: In case encoding the message is slow, it only really needs to be done once here
        for (InetSocketAddress recipient : broadcastRecipients) {
            queueMessage(recipient, message);
        }
    }

    public void queueMessage(InetSocketAddress recipient, Message message) {
        if (!queuedIndividualMessages.containsKey(recipient)) {
            queuedIndividualMessages.put(recipient, new ByteVector(BUFFER_SIZE));
        }
        ByteVector buffer = queuedIndividualMessages.get(recipient);
        int start = buffer.position();
        int type = registry.getMessageType(message);
        buffer.putInt(type);
        message.encode(buffer);
        // If this message takes us past the max size, send all previously queued messages for this recipient
        if (buffer.position() > BUFFER_SIZE) {
            sendToAddress(recipient, buffer, start);
            buffer.limit(buffer.position());
            buffer.position(start);
            buffer.compact();
        }
        if (buffer.position() > BUFFER_SIZE) {
            // TODO: Handle case where a single message is beyond the max size
            throw new RuntimeException("Not yet implemented");
        }
    }

    public void flush() {
        for (Map.Entry<InetSocketAddress, ByteVector> entry : queuedIndividualMessages.entrySet()) {
            sendToAddress(entry.getKey(), entry.getValue());
        }
        queuedIndividualMessages.clear();
    }

    private void sendToAddress(InetSocketAddress recipient, ByteVector buffer) {
        sendToAddress(recipient, buffer, buffer.position());
    }

    private void sendToAddress(InetSocketAddress recipient, ByteVector buffer, int length) {
        assert(length <= BUFFER_SIZE);
        DatagramPacket dataPacket = new DatagramPacket(buffer.array(), length, recipient.getAddress(), recipient.getPort());
        try {
            socket.send(dataPacket);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

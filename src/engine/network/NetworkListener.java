package sekelsta.engine.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import sekelsta.engine.Log;
import sekelsta.engine.network.Message;

public class NetworkListener extends Thread {
    private NetworkManager networkManager;
    private MessageRegistry registry;
    private DatagramSocket socket;
    private boolean running = true;

    private List<Message> messages = Collections.synchronizedList(new LinkedList<Message>());

    public NetworkListener(NetworkManager networkManager, MessageRegistry registry, DatagramSocket socket) {
        this.networkManager = networkManager;
        this.registry = registry;
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[Connection.BUFFER_SIZE];
        while(running) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            }
            catch (IOException e) {
                if (running) {
                    throw new RuntimeException(e);
                }
                continue;
            }
            // Confirmed that if a packet arrives during this time, the next receive() call will still see it.
            handlePacket(
                new InetSocketAddress(packet.getAddress(), packet.getPort()), 
                packet.getData(), 
                packet.getLength()
            );
        }
    }

    private void handlePacket(InetSocketAddress socketAddress, byte[] data, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);

        Connection connection = networkManager.getOrCreateConnection(socketAddress);
        MessageContext context = connection.processPacketHeader(buffer);
        if (context == null) {
            return;
        }
        connection.markAlive();
        // Extract potentially multiple messages from the same packet
        while (buffer.hasRemaining()) {
            int messageType = -1;
            Message message = null;
            try {
                messageType = buffer.getInt();
                message = registry.createMessage(messageType);
            }
            catch (Exception e) {
                Log.debug("Exception while handling packet metadata (sent from " + socketAddress + "):\n    " + e);
                break;
            }

            if (message != null) {
                try {
                    message.decode(buffer);
                    message.sender = connection;
                    message.context = context;
                    messages.add(message);
                }
                catch (Exception e) {
                    Log.debug("Exception while reading packet (sent from " + socketAddress + "):\n    " + e);
                    break;
                }
            }
        }
    }
    
    public void setDone() {
        running = false;
        socket.close();
    }

    public boolean hasMessage() {
        return messages.size() > 0;
    }

    public Message popMessage() {
        return messages.remove(0);
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }
}

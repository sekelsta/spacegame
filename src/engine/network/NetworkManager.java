package sekelsta.engine.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.function.Supplier;

import sekelsta.engine.IGame;
import sekelsta.engine.Log;
import sekelsta.engine.network.Message;

public class NetworkManager {
    private NetworkListener listener;
    private NetworkSender sender;
    private MessageRegistry registry = new MessageRegistry();
    private NetworkDirection acceptDirection = NetworkDirection.CLIENT_TO_SERVER;

    public NetworkManager(int port) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.listener = new NetworkListener(registry, socket);
        this.sender = new NetworkSender(registry, socket);
    }

    public void registerMessageType(Supplier<Message> messageSupplier) {
        registry.registerMessageType(messageSupplier);
    }

    public void start() {
        registry.freeze();
        listener.start();
    }

    public void update(IGame game) {
        sender.flush();
        while (listener.hasMessage()) {
            Message message = listener.popMessage();
            if (message.getDirection() == acceptDirection || message.getDirection() == NetworkDirection.BIDIRECTIONAL) {
                message.handle(game);
            }
            else {
                Log.debug("Received message of invalid type: " + message);
            }
        }
    }

    public void close() {
        listener.setDone();
    }

    public void joinServer(InetSocketAddress serverAddress) {
        acceptDirection = NetworkDirection.SERVER_TO_CLIENT;
        sender.addBroadcastRecipient(serverAddress);
        // TODO: Need to actually send a message to the server, either here or in Game
    }

    public void acceptClient(InetSocketAddress clientAddress) {
        assert(acceptDirection == NetworkDirection.CLIENT_TO_SERVER);
        sender.addBroadcastRecipient(clientAddress);

    }

    public void queueBroadcast(Message message) {
        assert(message.getDirection() != acceptDirection);
        sender.queueBroadcast(message);
    }

    public void queueMessage(InetSocketAddress receiver, Message message) {
        assert(message.getDirection() != acceptDirection);
        sender.queueMessage(receiver, message);
    }

    public boolean isBroadcastRecipient(InetSocketAddress socketAddress) {
        return sender.isBroadcastRecipient(socketAddress);
    }
}

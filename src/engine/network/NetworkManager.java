package sekelsta.engine.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import sekelsta.engine.IGame;
import sekelsta.engine.Log;
import sekelsta.engine.network.Message;

public class NetworkManager {
    private NetworkListener listener;
    protected NetworkSender sender;
    protected int port;
    private MessageRegistry registry = new MessageRegistry();
    protected NetworkDirection acceptDirection = NetworkDirection.CLIENT_TO_SERVER;

    private Map<InetSocketAddress, Long> pendingConnections = new HashMap<>();

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
        this.port = socket.getLocalPort();

        registerMessageType(ClientHello::new);
        registerMessageType(ServerRejectIncompatibleVersion::new);
        registerMessageType(ServerHello::new);
        registerMessageType(ClientConnect::new);
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
            if (message.getDirection() != acceptDirection && message.getDirection() != NetworkDirection.BIDIRECTIONAL) {
                Log.debug("Received message of invalid type: " + message);
                continue;
            }
            if (message.requiresConnection() && !isBroadcastRecipient(message.sender)) {
                Log.debug("Received message from invalid sender: " + message);
                continue;
            }
            message.handle(game);
        }
    }

    public void close() {
        listener.setDone();
        try {
            listener.join();
        }
        catch (InterruptedException e) {}
    }

    public void joinServer(IGame game, InetSocketAddress serverAddress) {
        acceptDirection = NetworkDirection.SERVER_TO_CLIENT;
        sender.addBroadcastRecipient(serverAddress);
        ClientHello clientHello = new ClientHello(game.getGameID(), game.getVersion());
        queueBroadcast(clientHello);
    }

    public boolean isPendingConnection(InetSocketAddress socketAddress) {
        return pendingConnections.containsKey(socketAddress);
    }

    public void addPendingClient(InetSocketAddress clientAddress, long nonce) {
        assert(acceptDirection == NetworkDirection.CLIENT_TO_SERVER);
        pendingConnections.put(clientAddress, nonce);
    }

    public long getExpectedNonce(InetSocketAddress clientAddress) {
        return pendingConnections.get(clientAddress);
    }

    public boolean confirmPendingClient(InetSocketAddress clientAddress, long nonce) {
        if (isPendingConnection(clientAddress) && pendingConnections.get(clientAddress) == nonce) {
            sender.addBroadcastRecipient(clientAddress);
            pendingConnections.remove(clientAddress);
            return true;
        }
        return false;
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

    public void removeBroadcastRecipient(InetSocketAddress socketAddress) {
        sender.removeBroadcastRecipient(socketAddress);
    }
}

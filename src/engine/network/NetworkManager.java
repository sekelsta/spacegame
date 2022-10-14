package sekelsta.engine.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import sekelsta.engine.IGame;
import sekelsta.engine.Log;
import sekelsta.engine.network.Message;

public class NetworkManager {
    private NetworkListener listener;
    private MessageRegistry registry = new MessageRegistry();
    protected NetworkDirection acceptDirection = NetworkDirection.CLIENT_TO_SERVER;

    protected DatagramSocket socket = null;

    private Set<Connection> queuedMessages = new HashSet<>();
    private Set<Connection> broadcastRecipients = new HashSet<>();
    private Map<Connection, Long> pendingConnections = new HashMap<>();

    public NetworkManager(int port) {
        try {
            socket = new DatagramSocket(port);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.listener = new NetworkListener(this, registry, socket);

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
        flush();
        while (listener.hasMessage()) {
            Message message = listener.popMessage();
            if (message.getDirection() != acceptDirection && message.getDirection() != NetworkDirection.BIDIRECTIONAL) {
                Log.debug("Received message of invalid type: " + message);
                continue;
            }
            if (message.requiresConfirmedAddress() && !broadcastRecipients.contains(message.sender)) {
                assert(!isBroadcastRecipient(message.sender.getSocketAddress()));
                Log.debug("Received message from invalid sender: " + message);
                continue;
            }
            message.handle(game);
        }
    }

    public void close() {
        Connection.closeAll();
        listener.setDone();
        try {
            listener.join();
        }
        catch (InterruptedException e) {}
    }

    public void joinServer(IGame game, InetSocketAddress serverAddress) {
        acceptDirection = NetworkDirection.SERVER_TO_CLIENT;
        addBroadcastRecipient(serverAddress);
        ClientHello clientHello = new ClientHello(game.getGameID(), game.getVersion());
        queueBroadcast(clientHello);
    }

    public boolean isPendingConnection(Connection client) {
        return pendingConnections.containsKey(client);
    }

    public void addPendingClient(Connection client, long nonce) {
        assert(acceptDirection == NetworkDirection.CLIENT_TO_SERVER);
        assert(!hasConnection(client.getSocketAddress()));
        pendingConnections.put(client, nonce);
    }

    public long getExpectedNonce(Connection client) {
        return pendingConnections.get(client);
    }

    public boolean confirmPendingClient(Connection client, long nonce) {
        if (!pendingConnections.containsKey(client)) {
            return false;
        }
        if (nonce != pendingConnections.get(client)) {
            return false;
        }
        pendingConnections.remove(client);
        broadcastRecipients.add(client);
        return true;
    }

    public void queueBroadcast(Message message) {
        assert(message.getDirection() != acceptDirection);
        // TO_OPTIMIZE: In case encoding the message is slow, it only really needs to be done once here
        for (Connection recipient : broadcastRecipients) {
            queueMessage(recipient, message);
        }
    }

    public void queueMessage(Connection recipient, Message message) {
        assert(message.getDirection() != acceptDirection);
        queuedMessages.add(recipient);
        recipient.queueMessage(registry, message);
    }

    public void addBroadcastRecipient(InetSocketAddress socketAddress) {
        if (!isBroadcastRecipient(socketAddress)) {
            broadcastRecipients.add(new Connection(socketAddress));
        }
    }

    public boolean isBroadcastRecipient(Connection connection) {
        return broadcastRecipients.contains(connection);
    }

    public boolean isBroadcastRecipient(InetSocketAddress socketAddress) {
        for (Connection connection : broadcastRecipients) {
            if (connection.getSocketAddress().equals(socketAddress)) {
                return true;
            }
        }
        return false;
    }

    public void removeBroadcastRecipient(Connection connection) {
        broadcastRecipients.remove(connection);
    }

    private void flush() {
        try {
            for (Connection connection : queuedMessages) {
                connection.flush(socket);
            }
            for (Connection connection : broadcastRecipients) {
                connection.flush(socket);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        queuedMessages.clear();
    }

    public Connection getOrCreateConnection(InetSocketAddress address) {
        Connection connection = getConnection(address);
        if (connection == null) {
            return new Connection(address);
        }
        return connection;
    }

    private Connection getConnection(InetSocketAddress address) {
        for (Connection connection : broadcastRecipients) {
            if (connection.getSocketAddress().equals(address)) {
                return connection;
            }
        }
        for (Map.Entry<Connection, Long> entry : pendingConnections.entrySet()) {
            if (entry.getKey().getSocketAddress().equals(address)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private boolean hasConnection(InetSocketAddress address) {
        return getConnection(address) != null;
    }
}

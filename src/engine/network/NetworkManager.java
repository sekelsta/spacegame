package sekelsta.engine.network;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import sekelsta.engine.Log;
import sekelsta.engine.network.Message;

public class NetworkManager {
    public static MessageContext context = null;

    private NetworkListener listener;
    private MessageRegistry registry = new MessageRegistry();
    protected NetworkDirection acceptDirection = NetworkDirection.CLIENT_TO_SERVER;

    protected DatagramSocket socket = null;

    private Set<Connection> queuedMessages = new HashSet<>();
    protected Set<Connection> broadcastRecipients = new HashSet<>();

    public NetworkManager(int port) {
        try {
            socket = new DatagramSocket(port);
        }
        catch (BindException e) {
            throw new RuntimeBindException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.listener = new NetworkListener(this, registry, socket);

        registerMessageType(ClientHello::new);
        registerMessageType(ServerRejectIncompatibleVersion::new);
        registerMessageType(ServerHello::new);
        registerMessageType(DisconnectMessage::new);
    }

    public void registerMessageType(Supplier<Message> messageSupplier) {
        registry.registerMessageType(messageSupplier);
    }

    public void start() {
        registry.freeze();
        listener.start();
    }

    public void update(INetworked game) {
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

        long currentTime = System.nanoTime();
        for (Connection connection : broadcastRecipients) {
            if (connection.shouldTimeOut(currentTime)) {
                broadcastRecipients.remove(connection);
                connection.close();
                game.connectionTimedOut(connection.getID());
            }
        }
    }

    public void close() {
        queueBroadcast(new DisconnectMessage());
        flush();
        Connection.closeAll();
        listener.setDone();
        try {
            listener.join();
        }
        catch (InterruptedException e) {}
    }

    public void joinServer(INetworked game, InetSocketAddress serverAddress) {
        acceptDirection = NetworkDirection.SERVER_TO_CLIENT;
        addBroadcastRecipient(serverAddress);
        ClientHello clientHello = new ClientHello(game.getGameID(), game.getVersion());
        queueBroadcast(clientHello);
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
        assert(broadcastRecipients.contains(recipient) || !message.reliable());
        queuedMessages.add(recipient);
        recipient.queueMessage(registry, message);
    }

    public void addBroadcastRecipient(Connection connection) {
        if (!isBroadcastRecipient(connection.getSocketAddress())) {
            broadcastRecipients.add(connection);
        }
    }

    public void addBroadcastRecipient(InetSocketAddress socketAddress) {
        if (!isBroadcastRecipient(socketAddress)) {
            broadcastRecipients.add(getOrCreateConnection(socketAddress));
        }
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
            return new Connection(address, acceptDirection == NetworkDirection.SERVER_TO_CLIENT);
        }
        return connection;
    }

    private Connection getConnection(InetSocketAddress address) {
        for (Connection connection : broadcastRecipients) {
            if (connection.getSocketAddress().equals(address)) {
                return connection;
            }
        }
        return null;
    }

    private boolean hasConnection(InetSocketAddress address) {
        return getConnection(address) != null;
    }
}

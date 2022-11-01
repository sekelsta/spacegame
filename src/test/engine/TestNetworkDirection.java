package sekelsta.test.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import sekelsta.engine.network.Connection;
import sekelsta.engine.network.NetworkDirection;

class TestNetworkDirection {
    private static class ClientByteMessage extends ByteMessage {
        public ClientByteMessage() {}

        public ClientByteMessage(byte[] bytes) {
            super(bytes);
        }

        @Override
        public NetworkDirection getDirection() {
            return NetworkDirection.CLIENT_TO_SERVER;
        }
    }

    private static class ServerByteMessage extends ByteMessage {
        public ServerByteMessage() {}

        public ServerByteMessage(byte[] bytes) {
            super(bytes);
        }

        @Override
        public NetworkDirection getDirection() {
            return NetworkDirection.SERVER_TO_CLIENT;
        }
    }

    private ExtendedNetworkManager server = new ExtendedNetworkManager(4321);
    private ExtendedNetworkManager client = new ExtendedNetworkManager(0);
    private NetworkedGame serverGame = new NetworkedGame(server);
    private NetworkedGame clientGame = new NetworkedGame(client);

    public TestNetworkDirection() {
        server.registerMessageType(ClientByteMessage::new);
        server.registerMessageType(ServerByteMessage::new);

        client.registerMessageType(ClientByteMessage::new);
        client.registerMessageType(ServerByteMessage::new);
        client.becomeClient();

        client.shortcutConnect(server.getAddress());
        server.shortcutConnect(client.getAddress());

        client.start();
        server.start();
    }

    @AfterEach
    void tearDown() {
        client.close();
        server.close();
        Connection.closeAll();
    }

    @Test
    void sendBidirectionalMessageToServer() {
        ByteMessage message = new ByteMessage(new byte[10]);
        client.queueBroadcast(message);
        clientGame.update();
        serverGame.update();
        assertEquals(1, serverGame.handledTestMessages.size());
    }

    @Test
    void sendBidirectionalMessageToClient() {
        ByteMessage message = new ByteMessage(new byte[10]);
        server.queueBroadcast(message);
        serverGame.update();
        clientGame.update();
        assertEquals(1, clientGame.handledTestMessages.size());
    }

    @Test
    void sendMessageClientToServer() {
        ClientByteMessage message = new ClientByteMessage(new byte[10]);
        client.queueBroadcast(message);
        clientGame.update();
        serverGame.update();
        assertEquals(1, serverGame.handledTestMessages.size());
    }

    @Test
    void sendMessageServerToClient() {
        ServerByteMessage message = new ServerByteMessage(new byte[10]);
        server.queueBroadcast(message);
        serverGame.update();
        clientGame.update();
        assertEquals(1, clientGame.handledTestMessages.size());
    }

    @Test
    void sendMessageClientToClient() {
        try {
            ServerByteMessage message = new ServerByteMessage(new byte[10]);
            client.queueBroadcast(message);
            clientGame.update();
            serverGame.update();
        }
        catch (AssertionError e) {
            // Test passed
            return;
        }

        assertEquals(0, serverGame.handledTestMessages.size());
    }

    @Test
    void sendMessageServerToServer() {
        try {
            ClientByteMessage message = new ClientByteMessage(new byte[10]);
            server.queueBroadcast(message);
            serverGame.update();
            clientGame.update();
        }
        catch (AssertionError e) {
            // Test passed
            return;
        }

        assertEquals(0, clientGame.handledTestMessages.size());
    }
}

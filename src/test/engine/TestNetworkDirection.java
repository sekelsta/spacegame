package sekelsta.test.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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

    private ExtendedNetworkManager server;
    private ExtendedNetworkManager client;
    private NetworkedGame serverGame;
    private NetworkedGame clientGame;

    public TestNetworkDirection() {
        server = new ExtendedNetworkManager(4321);
        server.registerMessageType(ClientByteMessage::new);
        server.registerMessageType(ServerByteMessage::new);
        client = new ExtendedNetworkManager(0);
        client.registerMessageType(ClientByteMessage::new);
        client.registerMessageType(ServerByteMessage::new);
        client.becomeClient();

        client.shortcutConnect(server.getAddress());
        server.shortcutConnect(client.getAddress());

        serverGame = new NetworkedGame();
        serverGame.networkManager = server;

        clientGame = new NetworkedGame();
        clientGame.networkManager = client;

        client.start();
        server.start();
    }

    @AfterEach
    void tearDown() {
        client.close();
        server.close();
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

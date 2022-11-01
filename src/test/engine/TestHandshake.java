package sekelsta.test.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import sekelsta.engine.SoftwareVersion;
import sekelsta.engine.network.Connection;

class TestHandshake {
    private ExtendedNetworkManager server = new ExtendedNetworkManager(4321);
    private ExtendedNetworkManager client = new ExtendedNetworkManager(0);
    private NetworkedGame serverGame = new NetworkedGame(server);
    private NetworkedGame clientGame = new NetworkedGame(client);

    public TestHandshake() {
        client.becomeClient();

        client.start();
        server.start();

        clientGame.gameID = "test_engine";
        clientGame.version = new SoftwareVersion(1, 5, 2);
        serverGame.gameID = "test_engine";
        serverGame.version = new SoftwareVersion(1, 5, 2);
    }

    @AfterEach
    void tearDown() {
        client.close();
        server.close();
        Connection.closeAll();
    }

    @Test
    void testHandshake() {
        client.joinServer(clientGame, server.getAddress());
        clientGame.update();
        serverGame.update();
        clientGame.update();
        serverGame.update();
        clientGame.update();
        serverGame.update();
        clientGame.update();
        serverGame.update();

        assertEquals(0, clientGame.getConnectionRejectedCount());
        assertEquals(1, clientGame.getHelloFromServerCount());
        assertEquals(1, serverGame.getClientConnectionAcceptedCount());

        Connection clientConnection = server.getOrCreateConnection(client.getAddress());
        Connection serverConnection = client.getOrCreateConnection(server.getAddress());
        assertTrue(!server.isPendingConnection(clientConnection));
        assertTrue(!client.isPendingConnection(serverConnection));
        assertTrue(server.isBroadcastRecipient(clientConnection));
        assertTrue(client.isBroadcastRecipient(serverConnection));
    }

    @Test
    void rejectNonmatchingVersion() {
        clientGame.version = new SoftwareVersion(4, 2, 0);

        client.joinServer(clientGame, server.getAddress());
        clientGame.update();
        serverGame.update();
        clientGame.update();
        serverGame.update();
        clientGame.update();

        assertEquals(1, clientGame.getConnectionRejectedCount());
        assertEquals(0, clientGame.getHelloFromServerCount());
        assertEquals(0, serverGame.getClientConnectionAcceptedCount());

        Connection clientConnection = server.getOrCreateConnection(client.getAddress());
        Connection serverConnection = client.getOrCreateConnection(server.getAddress());
        assertTrue(!server.isPendingConnection(clientConnection));
        assertTrue(!client.isPendingConnection(serverConnection));
        assertTrue(!server.isBroadcastRecipient(clientConnection));
        assertTrue(!client.isBroadcastRecipient(serverConnection));
    }
}

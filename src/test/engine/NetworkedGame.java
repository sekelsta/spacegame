package sekelsta.test.engine;

import java.util.ArrayList;

import sekelsta.engine.SoftwareVersion;
import sekelsta.engine.network.Connection;
import sekelsta.engine.network.INetworked;
import sekelsta.engine.network.Message;
import sekelsta.engine.network.NetworkManager;

public class NetworkedGame implements INetworked {
    // Note this only includes messages that add themselves to this list
    public ArrayList<Message> handledTestMessages = new ArrayList<>();

    public String gameID;
    public SoftwareVersion version;

    private NetworkManager networkManager;

    private int connectionRejectedCount;
    private int helloFromServerCount;
    private int clientConnectionAcceptedCount;

    public NetworkedGame(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public SoftwareVersion getVersion() {
        return version;
    }

    @Override
    public String getGameID() {
        return gameID;
    }

    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void update() {
        if (networkManager != null) {
            networkManager.update(this);
        }
    }

    @Override
    public void connectionRejected(String reason) {
        connectionRejectedCount += 1;
    }

    public int getConnectionRejectedCount() {
        return connectionRejectedCount;
    }

    @Override
    public void receivedHelloFromServer(SoftwareVersion version) {
        helloFromServerCount += 1;
    }

    public int getHelloFromServerCount() {
        return helloFromServerCount;
    }

    @Override
    public void clientConnectionAccepted(Connection client) {
        clientConnectionAcceptedCount += 1;
    }

    public int getClientConnectionAcceptedCount() {
        return clientConnectionAcceptedCount;
    }

    @Override
    public void handleDisconnect(long connectionID) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void connectionTimedOut(long connectionID) {
        throw new RuntimeException("Not implemented");
    }
}

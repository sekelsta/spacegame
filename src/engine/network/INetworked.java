package sekelsta.engine.network;

import sekelsta.engine.SoftwareVersion;

public interface INetworked {
    SoftwareVersion getVersion();
    String getGameID();
    NetworkManager getNetworkManager();

    void connectionRejected(String reason);
    void receivedHelloFromServer(SoftwareVersion version);
    void clientConnectionAccepted(Connection client);
    void connectionTimedOut(long connectionID);
    void handleDisconnect(long connectionID);
}

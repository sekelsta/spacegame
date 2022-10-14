package sekelsta.engine;

import sekelsta.engine.network.Connection;
import sekelsta.engine.network.NetworkManager;

public interface IGame {
    SoftwareVersion getVersion();
    String getGameID();
    boolean isRunning();
    NetworkManager getNetworkManager();

    void update();
    void render(float interpolation);
    void close();

    void connectionRejected(String reason);
    void receivedHelloFromServer(SoftwareVersion version);
    void clientConnectionAccepted(Connection client);
}

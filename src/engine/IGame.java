package sekelsta.engine;

import sekelsta.engine.network.NetworkManager;

public interface IGame {
    boolean isRunning();
    NetworkManager getNetworkManager();
    void update();
    void render(float interpolation);
    void close();
}

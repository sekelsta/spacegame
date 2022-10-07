package sekelsta.test.engine;

import java.util.ArrayList;

import java.net.InetSocketAddress;

import sekelsta.engine.IGame;
import sekelsta.engine.network.Message;
import sekelsta.engine.network.NetworkManager;

public class NetworkedGame implements IGame {
    public NetworkManager networkManager;

    // Note this only includes messages that add themselves to this list
    public ArrayList<Message> handledTestMessages = new ArrayList<>();

    @Override
    public boolean isRunning() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    @Override
    public void update() {
        handledTestMessages.clear();
        if (networkManager != null) {
            networkManager.update(this);
        }
    }

    @Override
    public void render(float interpolation) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void close() {
        throw new RuntimeException("Not implemented");
    }
}

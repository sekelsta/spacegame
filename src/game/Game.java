package sekelsta.game;

import java.net.InetSocketAddress;

import sekelsta.engine.DataFolders;
import sekelsta.engine.IGame;
import sekelsta.engine.SoftwareVersion;
import sekelsta.engine.network.Connection;
import sekelsta.engine.network.NetworkManager;
import sekelsta.engine.render.Window;
import sekelsta.game.render.Renderer;

public class Game implements IGame {
    public static final SoftwareVersion VERSION = new SoftwareVersion(0, 0, 0);
    public static final String GAME_ID = "MySpaceGame";

    private boolean running = true;

    private World world;
    private Window window;
    private Renderer renderer;
    private Input input;
    private Camera camera;
    private NetworkManager networkManager;

    public Game(boolean graphical) {
        if (graphical) {
            this.window = new Window(DataFolders.getUserMachineFolder("initconfig.toml"), GAME_ID);
            this.renderer = new Renderer();
            this.window.setResizeListener(renderer);
            this.input = new Input();
            this.window.setInput(input);
        }
        this.world = new World(this, true);
    }

    public void enterWorld() {
        if (isGraphical()) {
            this.world.spawnLocalPlayer(input);
            this.camera = new Camera(world.getLocalPlayer());
            this.input.setCamera(camera);
            this.input.setPlayer(this.world.getLocalPlayer());
            this.input.setWorld(this.world);
        }
    }

    private boolean isGraphical() {
        return window != null;
    }

    @Override
    public SoftwareVersion getVersion() {
        return VERSION;
    }

    @Override
    public String getGameID() {
        return GAME_ID;
    }

    @Override
    public boolean isRunning() {
        return running && (window == null || !window.shouldClose());
    }

    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void allowConnections(int port) {
        assert(networkManager == null);
        networkManager = new NetworkManager(port);
        // TODO: Register messages
        networkManager.start();
    }

    public void joinServer(InetSocketAddress socketAddress) {
        allowConnections(0);
        networkManager.joinServer(this, socketAddress);
        this.world = new World(this, false);
    }

    @Override
    public void update() {
        if (world != null) {
            world.update();
        }
        if (window != null) {
            window.updateInput();
        }
        if (networkManager != null) {
            networkManager.update(this);
        }
    }

    @Override
    public void render(float interpolation) {
        if (window == null) {
            return;
        }
        window.updateInput();
        renderer.render(interpolation, camera, world);
        window.swapBuffers();
    }

    @Override
    public void close() {
        if (!running) {
            // Already closed
            return;
        }
        running = false;
        if (window != null) {
            window.close();
        }
        if (networkManager != null) {
            networkManager.close();
        }
    }

    @Override
    public void connectionRejected(String reason) {
        // TODO
    }

    @Override
    public void receivedHelloFromServer(SoftwareVersion version) {
        // TODO
    }

    @Override
    public void clientConnectionAccepted(Connection client) {
        // TODO
    }

    public World getWorld() {
        return world;
    }
}

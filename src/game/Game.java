package sekelsta.game;

import java.net.InetSocketAddress;

import sekelsta.engine.DataFolders;
import sekelsta.engine.IGame;
import sekelsta.engine.SoftwareVersion;
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
        this.world = new World();
        if (graphical) {
            this.window = new Window(DataFolders.getUserMachineFolder("initconfig.toml"), GAME_ID);
            this.renderer = new Renderer();
            this.window.setResizeListener(renderer);
            this.input = new Input();
            this.window.setInput(input);
            this.world.spawnLocalPlayer(input);
            this.camera = new Camera(world.getLocalPlayer());
            this.input.setCamera(camera);
            this.input.setPlayer(this.world.getLocalPlayer());
            this.input.setWorld(this.world);
        }
    }

    public boolean isRunning() {
        return running && (window == null || !window.shouldClose());
    }

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
        networkManager.joinServer(socketAddress);
    }

    public void update() {
        world.update();
        if (window != null) {
            window.updateInput();
        }
        if (networkManager != null) {
            networkManager.update(this);
        }
    }

    public void render(float interpolation) {
        if (window == null) {
            return;
        }
        window.updateInput();
        renderer.render(interpolation, camera, world);
        window.swapBuffers();
    }

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
}

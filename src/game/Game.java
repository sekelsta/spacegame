package sekelsta.game;

import java.net.InetAddress;

import sekelsta.engine.DataFolders;
import sekelsta.engine.IGame;
import sekelsta.engine.render.Window;
import sekelsta.game.render.Renderer;

public class Game implements IGame {
    private boolean running = true;

    private World world;
    private Window window;
    private Renderer renderer;
    private Input input;
    private Camera camera;

    public Game(boolean graphical) {
        String appName = "MySpaceGame";
        DataFolders.init(appName);
        this.world = new World();
        if (graphical) {
            this.window = new Window(DataFolders.getUserMachineFolder("initconfig.toml"), appName);
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

    public void allowConnections(int port) {
        throw new RuntimeException("TODO: allowConnections() not implemented");
    }

    public void joinServer(InetAddress address, int port) {
        throw new RuntimeException("TODO: joinServer() not implemented");
    }

    public void update() {
        // TODO: handle networking
        world.update();
        if (window != null) {
            window.updateInput();
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
        running = false;
        if (window != null) {
            window.close();
        }
    }
}

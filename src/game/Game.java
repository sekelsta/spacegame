package sekelsta.game;

import java.util.Random;
import sekelsta.engine.DataFolders;
import sekelsta.engine.IGame;
import sekelsta.engine.render.Window;
import sekelsta.game.render.Renderer;

public class Game implements IGame {
    public static final Random RANDOM = new Random();
    private boolean running = true;

    private World world;
    private Window window;
    private Renderer renderer;
    private Input input;
    private Camera camera;

    public Game() {
        String appName = "MySpaceGame";
        DataFolders.init(appName);        
        this.window = new Window(DataFolders.getUserMachineFolder("initconfig.toml"), appName);
        this.renderer = new Renderer();
        this.window.setResizeListener(renderer);
        this.input = new Input();
        this.window.setInput(input);
        this.world = new World(input);
        this.camera = new Camera(world.getPlayer().getPosition());
        this.input.setCamera(camera);
        this.input.setPlayer(this.world.getPlayer());
    }

    public boolean isRunning() {
        return running && (window == null || !window.shouldClose());
    }

    public void update() {
        // TODO: handle networking
        world.update();
        window.updateInput();
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

package sekelsta.game;

import java.util.Random;
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
        this.input = new Input();
        this.world = new World(input);
        this.window = new Window(600, 400, "Hello world!");
        this.renderer = new Renderer();
        this.window.setResizeListener(renderer);
        this.camera = new Camera(world.getPlayer().getPosition());
        this.window.setInput(input);
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

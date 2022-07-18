package sekelsta.engine;

public interface IGame {
    boolean isRunning();
    void update();
    void render(float interpolation);
    void close();
}

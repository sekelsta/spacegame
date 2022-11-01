package sekelsta.engine;

public interface ILoopable {
    boolean isRunning();

    void update();
    void render(float interpolation);
    void close();
}

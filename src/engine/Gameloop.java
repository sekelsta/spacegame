package sekelsta.engine;

public class Gameloop {
    public static final int TICKS_PER_SECOND = 24;
    static final int MAX_FRAME_SKIP = 5;

    private ILoopable game;
    // If making this modifiable in the future, remember it could be changed from within run()
    private final int fps_cap;

    // Use an FPS cap of 0 or lower to mean unlimited
    public Gameloop(ILoopable game, int fps_cap) {
        this.game = game;
        this.fps_cap = fps_cap;
    }

    // A deWitters game loop, modified to allow for framerate capping
    public void run() {
        final long NANOSECONDS_PER_TICK = 1000000000 / TICKS_PER_SECOND;
        final long NANOSECONDS_PER_FRAME = fps_cap > 0 ? 1000000000 / fps_cap : 0;
        long nextTickTime = System.nanoTime();
        long nextFrameTime = nextTickTime;

        while (game.isRunning()) {
            int skipped_frames = 0;
            while (nextTickTime < System.nanoTime() && skipped_frames < MAX_FRAME_SKIP) {
                game.update();
                nextTickTime += NANOSECONDS_PER_TICK;
                skipped_frames += 1;
            }

            float interpolation = (float)(System.nanoTime() + NANOSECONDS_PER_TICK - nextTickTime) / (float)NANOSECONDS_PER_TICK;
            game.render(interpolation);
            nextFrameTime += NANOSECONDS_PER_FRAME;
            if (fps_cap > 0) {
                long now = System.nanoTime();
                long waitTime = Math.min(nextFrameTime, nextTickTime) - now;
                if (waitTime > 0) {
                    try {
                        // Don't busy wait
                        Thread.sleep(waitTime / 1000000);
                    }
                    catch (InterruptedException e) {
                        Log.warn(e.toString());
                    }
                }
            }
        }

        game.close();
    }
}

package sekelsta.game.entity;

import java.util.Random;
import sekelsta.engine.Position;
import sekelsta.game.World;

public class Asteroid extends Mob {
    int size;

    public Asteroid(long x, long y, long z, World world, int s) {
        super(Entities.ASTEROID, x, y, z, world);
        size = s;
        collisionRadius *= getSizeScale();

        Random random = new Random();
        int velocityCap = Position.RESOLUTION * 1;
        getPosition().accelerate(randRange(random, velocityCap), randRange(random, velocityCap), randRange(random, velocityCap));
        getPosition().setAngle(random.nextInt((int)Position.ANGLE_RESOLUTION), 
            random.nextInt((int)Position.ANGLE_RESOLUTION), random.nextInt((int)Position.ANGLE_RESOLUTION));
        int rotCap = (int)(Position.ANGLE_RESOLUTION) / 64;
        getPosition().angularAccelerate(randRange(random, rotCap), randRange(random, rotCap), randRange(random, rotCap));
    }

    private int randRange(Random random, int range) {
        return random.nextInt(2 * range) - range;
    }

    public Asteroid(long x, long y, long z, World world) {
        this(x, y, z, world, 1);
    }

    public Asteroid(long x, long y, long z, World world, Random random) {
        this(x, y, z, world, random.nextInt(4));
    }

    public int getSize() {
        return size;
    }

    public int getSizeScale() {
        return 1 << size;
    }

    public void destroy() {
        if (size > 0) {
            for (int i = 0; i < 2; i++) {
                world.spawn(new Asteroid(this.getPosition().getX(), this.getPosition().getY(), this.getPosition().getZ(), world, size - 1));
            }
            // TODO: Spawn two smaller asteroids
            System.out.println("TODO: spawn two asteroids of size " + (size - 1));
        }
        world.kill(this);
    }
}

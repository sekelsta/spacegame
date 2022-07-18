package sekelsta.game.entity;

import java.util.Random;
import sekelsta.game.World;

public class Asteroid extends Mob {
    int size;

    public Asteroid(long x, long y, long z, World world, int s) {
        super(Entities.ASTEROID, x, y, z, world);
        size = s;
        collisionRadius *= size;
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
            // TODO: Spawn two smaller asteroids
            System.out.println("TODO: spawn two asteroids of size " + (size - 1));
        }
        world.kill(this);
    }
}

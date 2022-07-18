package sekelsta.game.entity;

import sekelsta.game.World;

public class Asteroid extends Mob {
    int size;

    public Asteroid(long x, long y, long z, World world, int s) {
        super(Entities.ASTEROID, x, y, z, world);
        size = s;
    }

    public Asteroid(long x, long y, long z, World world) {
        this(x, y, z, world, 1);
    }

    public int getSize() {
        return size;
    }
}

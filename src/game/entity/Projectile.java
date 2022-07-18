package sekelsta.game.entity;

import sekelsta.game.World;

public class Projectile extends Mob {
    public Projectile(long x, long y, long z, World world) {
        super(Entities.PROJECTILE, x, y, z, world);
    }

    @Override
    public boolean hasCollisions() {
        return true;
    }

    @Override
    public void collide(Entity other) {
        if (other.type == Entities.ASTEROID) {
            assert(other instanceof Asteroid);
            ((Asteroid)other).destroy();
        }
    }
}

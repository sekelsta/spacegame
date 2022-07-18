package sekelsta.game.entity;

import sekelsta.game.World;

public class Projectile extends Mob {
    Entity owner;

    public Projectile(Entity owner, long x, long y, long z, World world) {
        super(Entities.PROJECTILE, x, y, z, world);
        this.owner = owner;
    }

    @Override
    public boolean hasCollisions() {
        return true;
    }

    @Override
    public void collide(Entity other) {
        if (other == owner) {
            return;
        }

        if (other.type == Entities.ASTEROID) {
            assert(other instanceof Asteroid);
            ((Asteroid)other).destroy();
        }

        world.kill(this);
    }
}

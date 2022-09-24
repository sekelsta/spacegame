package sekelsta.game.entity;

import sekelsta.engine.entity.Entity;
import sekelsta.engine.entity.EntityType;
import sekelsta.engine.entity.ICollidable;
import sekelsta.engine.entity.Movable;
import sekelsta.game.World;

public class Projectile extends Movable implements ICollidable {
    Entity owner;
    private final World world;

    public Projectile(Entity owner, double x, double y, double z, World world) {
        super(x, y, z);
        this.world = world;
        this.owner = owner;
    }

    @Override
    public double getCollisionRadius() {
        return 1.0;
    }

    @Override
    public EntityType getType() {
        return Entities.PROJECTILE;
    }

    @Override
    public void collide(Entity other) {
        if (other == owner) {
            return;
        }

        if (other instanceof Asteroid) {
            ((Asteroid)other).destroy();
        }

        world.kill(this);
    }
}

package sekelsta.game.entity;

import java.nio.ByteBuffer;

import sekelsta.engine.entity.*;
import sekelsta.engine.network.ByteVector;
import sekelsta.game.World;

public class Projectile extends Entity implements ICollider {
    Entity owner;

    public Projectile(Entity owner, double x, double y, double z) {
        super(x, y, z);
        this.owner = owner;
    }

    public Projectile(ByteBuffer buffer) {
        super(buffer);
        // TODO #27
        this.owner = null;
    }

    @Override
    public void encode(ByteVector buffer) {
        super.encode(buffer);
        // TODO #27
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

        world.remove(this);
    }

    public boolean isOwnedBy(Entity entity) {
        return entity == owner;
    }
}

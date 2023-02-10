package sekelsta.game.entity;

import java.nio.ByteBuffer;

import sekelsta.engine.entity.*;
import sekelsta.engine.network.ByteVector;
import sekelsta.game.World;

public class Projectile extends Entity implements ICollider {
    private int ownerID;

    public Projectile(Entity owner, double x, double y, double z) {
        super(x, y, z);
        this.ownerID = owner.getID();
    }

    public Projectile(ByteBuffer buffer) {
        super(buffer);
        ownerID = buffer.getInt();
    }

    @Override
    public void encode(ByteVector buffer) {
        super.encode(buffer);
        buffer.putInt(ownerID);
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
        if (isOwnedBy(other)) {
            return;
        }

        if (other instanceof Asteroid) {
            ((Asteroid)other).destroy();
        }

        world.remove(this);
    }

    public boolean isOwnedBy(Entity entity) {
        return entity.getID() == ownerID;
    }

    public int getOwnerID() {
        return ownerID;
    }
}

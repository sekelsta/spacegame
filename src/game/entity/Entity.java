package sekelsta.game.entity;

import sekelsta.engine.Position;

public class Entity {
    public final EntityType type;
    protected int collisionRadius = Position.RESOLUTION;
    protected final Position position;

    public Entity(EntityType type, long x, long y, long z) {
        assert(type != null);
        this.type = type;
        this.position = new Position(x, y, z);
    }

    public Position getPosition() {
        return position;
    }

    public int getCollisionRadius() {
        return collisionRadius;
    }

    public boolean hasCollisions() {
        return false;
    }

    public void collide(Entity other) { }

    public float getInterpolatedX(float lerp) {
        return position.getInterpolatedX(lerp);
    }

    public float getInterpolatedY(float lerp) {
        return position.getInterpolatedY(lerp);
    }

    public float getInterpolatedZ(float lerp) {
        return position.getInterpolatedZ(lerp);
    }
}

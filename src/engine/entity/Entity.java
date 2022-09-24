package sekelsta.engine.entity;

public interface Entity {
    public float getInterpolatedX(float lerp);
    public float getInterpolatedY(float lerp);
    public float getInterpolatedZ(float lerp);

    public float getInterpolatedYaw(float lerp);
    public float getInterpolatedPitch(float lerp);
    public float getInterpolatedRoll(float lerp);

    public double getCollisionRadius();

    public EntityType getType();
}

package sekelsta.engine.entity;

import sekelsta.engine.network.ByteVector;
import sekelsta.math.Vector3f;

public interface Entity {
    public void encode(ByteVector buffer);
    public EntityType getType();
    public int getID();

    public float getInterpolatedX(float lerp);
    public float getInterpolatedY(float lerp);
    public float getInterpolatedZ(float lerp);

    public float getInterpolatedYaw(float lerp);
    public float getInterpolatedPitch(float lerp);
    public float getInterpolatedRoll(float lerp);

    public double getCollisionRadius();

    default Vector3f getEyeOffset() {
        return new Vector3f(0, 0, 0);
    }
}

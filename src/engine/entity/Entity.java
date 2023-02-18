package sekelsta.engine.entity;

import java.nio.ByteBuffer;

import sekelsta.engine.Particle;
import sekelsta.engine.network.ByteVector;
import shadowfox.math.Matrix3f;
import shadowfox.math.Vector3f;

public class Entity {
    public static final double ONE_METER = 1.0;
    public static final float TAU = 2 * (float)Math.PI; // Full circle
    private int id = -1;
    protected IEntitySpace world;
    protected IController controller = null;
    protected double x, y, z;
    protected double prevX, prevY, prevZ;
    protected float velocityX, velocityY, velocityZ;

    // Angular values range from 0.0 to 1.0
    protected float yaw, pitch, roll;
    private float prevYaw, prevPitch, prevRoll;
    private float angularVelocityX, angularVelocityY, angularVelocityZ;

    // 0.99 or 0.98 is like ice
    // 0.8 is like land
    // We're in space now, so set this high
    protected float drag = 1.0f;
    protected float angularDrag = 1.0f;

    public Entity(double x, double y, double z) {
        teleport(x, y, z);
    }

    public Entity(ByteBuffer buffer) {
        id = buffer.getInt();
        x = buffer.getDouble();
        y = buffer.getDouble();
        z = buffer.getDouble();
        prevX = x;
        prevY = y;
        prevZ = z;
        velocityX = buffer.getFloat();
        velocityY = buffer.getFloat();
        velocityZ = buffer.getFloat();
        yaw = buffer.getFloat();
        pitch = buffer.getFloat();
        roll = buffer.getFloat();
        prevYaw = yaw;
        prevPitch = pitch;
        prevRoll = roll;
        angularVelocityX = buffer.getFloat();
        angularVelocityY = buffer.getFloat();
        angularVelocityZ = buffer.getFloat();
        drag = buffer.getFloat();
        angularDrag = buffer.getFloat();
    }

    public void encode(ByteVector buffer) {
        buffer.putInt(id);
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
        // Skip prevX, prevY, prevZ
        buffer.putFloat(velocityX);
        buffer.putFloat(velocityY);
        buffer.putFloat(velocityZ);
        buffer.putFloat(yaw);
        buffer.putFloat(pitch);
        buffer.putFloat(roll);
        // Skip prevYaw, prevPitch, prevRoll
        buffer.putFloat(angularVelocityX);
        buffer.putFloat(angularVelocityY);
        buffer.putFloat(angularVelocityZ);
        buffer.putFloat(drag);
        buffer.putFloat(angularDrag);
    }

    public void updateFrom(Entity other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        // Skip prevX, prevY, prevZ
        this.velocityX = other.velocityX;
        this.velocityY = other.velocityY;
        this.velocityZ = other.velocityZ;
        this.yaw = other.yaw;
        this.pitch = other.pitch;
        this.roll = other.roll;
        // Skip prevYaw, prevPitch, prevRoll
        this.angularVelocityX = other.angularVelocityX;
        this.angularVelocityY = other.angularVelocityY;
        this.angularVelocityZ = other.angularVelocityZ;
    }

    public void updateFromLate(Entity other, int ticksLate) {
        updateFrom(other);
        for (int i = 0; i < ticksLate; ++i) {
            tick();
        }
    }

    public EntityType getType() {
        return null;
    }

    public double getCollisionRadius() {
        throw new RuntimeException("getCollisionRadius() not implemented");
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public final void enterWorld(IEntitySpace world) {
        this.world = world;
    }

    public final IEntitySpace getWorld() {
        return world;
    }

    public void setController(IController controller) {
        this.controller = controller;
    }

    public IController getController() {
        return controller;
    }

    public void update() {
        if (controller != null)
        {
            controller.preUpdate();
        }
        tick();
        if (controller != null)
        {
            controller.postUpdate();
        }
    }

    public boolean mayDespawn() {
        return true;
    }

    public void accelerate(float x, float y, float z) {
        velocityX += x;
        velocityY += y;
        velocityZ += z;
    }

    public void setVelocity(float x, float y, float z) {
        velocityX = x;
        velocityY = y;
        velocityZ = z;
    }

    public void scaleVelocity(float s) {
        velocityX *= s;
        velocityY *= s;
        velocityZ *= s;
    }

    public void angularAccelerate(float x, float y, float z) {
        angularVelocityX += x;
        angularVelocityY += y;
        angularVelocityZ += z;
    }

    public void scaleAngularVelocity(float s) {
        angularVelocityX *= s;
        angularVelocityY *= s;
        angularVelocityZ *= s;
    }

    public void snapToAngle(float yaw, float pitch, float roll) {
        this.yaw =  yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.prevYaw = yaw;
        this.prevPitch = pitch;
        this.prevRoll = roll;
    }

    protected void tick() {
        prevX = x;
        prevY = y;
        prevZ = z;
        x += velocityX;
        y += velocityY;
        z += velocityZ;

        prevYaw = yaw;
        prevRoll = roll;
        prevPitch = pitch;

        // Combine rotations with matrices
        // TO_OPTIMIZE: Quaternions might be faster
        Matrix3f rotation = new Matrix3f();
        rotation.rotate(angularVelocityZ, angularVelocityX, angularVelocityY);
        rotation.rotate(yaw, pitch, roll);

        this.yaw = rotation.getYaw();
        this.pitch = rotation.getPitch();
        this.roll = rotation.getRoll();

        yaw %= TAU;
        pitch %= TAU;
        roll %= TAU;

        // If yaw and roll just changed by 180 degrees, adjust prevYaw, prevPitch, and prevRoll to match
        float ninetyDegrees = TAU / 4;
        if (getPositiveAngleBetween(yaw, prevYaw) > ninetyDegrees 
                && getPositiveAngleBetween(roll, prevRoll) > ninetyDegrees) {
            prevYaw += TAU / 2;
            prevRoll += TAU / 2;
            prevPitch = TAU / 2 - prevPitch;

            prevYaw %= TAU;
            prevPitch %= TAU;
            prevRoll %= TAU;
        }

        scaleVelocity(drag);
        scaleAngularVelocity(angularDrag);
    }

    public final void teleport(double x, double y, double z) {
        this.x = this.prevX = x;
        this.y = this.prevY = y;
        this.z = this.prevZ = z;
    }

    public final void setAngle(float y, float p, float r) {
        this.yaw = y;
        this.pitch = p;
        this.roll = r;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getInterpolatedX(float lerp) {
        return (float)(lerp * x + (1 - lerp) * prevX);
    }

    public float getInterpolatedY(float lerp) {
        return (float)(lerp * y + (1 - lerp) * prevY);
    }

    public float getInterpolatedZ(float lerp) {
        return (float)(lerp * z + (1 - lerp) * prevZ);
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public float getVelocityZ() {
        return velocityZ;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

    public float getYawVelocity() {
        return angularVelocityZ;
    }

    public float getPitchVelocity() {
        return angularVelocityX;
    }

    public float getRollVelocity() {
        return angularVelocityY;
    }

    public float getInterpolatedYaw(float lerp) {
        return interpolateAngle(yaw, prevYaw, lerp);
    }

    public float getInterpolatedPitch(float lerp) {
        return interpolateAngle(pitch, prevPitch, lerp);
    }

    public float getInterpolatedRoll(float lerp) {
        return interpolateAngle(roll, prevRoll, lerp);
    }

    private float interpolateAngle(float current, float prev, float lerp) {
        if (current - prev > TAU / 2) {
            current -= TAU;
        }
        else if (prev - current > TAU / 2) {
            current += TAU;
        }
        return lerp * current + (1 - lerp) * prev;
    }

    private float getPositiveAngleBetween(float theta, float phi) {
        float diff = Math.abs(theta - phi) % TAU;
        return (float)Math.min(diff, TAU - diff);
    }

    public double distSquared(Entity other) {
        return distSquared(other.getX(), other.getY(), other.getZ());
    }

    public double distSquared(double x, double y, double z) {
        double distX = getX() - x;
        double distY = getY() - y;
        double distZ = getZ() - z;
        return distX * distX + distY * distY + distZ * distZ;
    }

    public void accelerateForwards(float amount) {
        // Formula obtained by transforming the forward vector (0, 1, 0) by the rotation matrix from yaw, pitch, and roll
        double dx = -1 * Math.cos(pitch) * Math.sin(yaw);
        double dy = Math.cos(pitch) * Math.cos(yaw);
        double dz = Math.sin(pitch);
        accelerate((float)(dx * amount), (float)(dy * amount), (float)(dz * amount));
    }

    public void angularAccelerateLocalAxis(float amount, float x, float y, float z) {
        // TO_OPTIMIZE: For axis-aligned rotations, the full matrix is not really needed
        Vector3f axis = new Vector3f(x, y, z);
        axis.rotate(yaw, pitch, roll);

        Matrix3f rotation = new Matrix3f();
        rotation.rotate(amount, axis.x, axis.y, axis.z);

        this.angularVelocityX += rotation.getPitch();
        this.angularVelocityY += rotation.getRoll();
        this.angularVelocityZ += rotation.getYaw();
    }

    public Vector3f getEyeOffset() {
        return new Vector3f(0, 0, 0);
    }

    public Particle getParticleRelative(Vector3f offset, int lifespan, Vector3f velocity) {
        Particle particle = new Particle((float)x + offset.x, (float)y + offset.y, (float)z + offset.z, lifespan, (float)prevX + offset.x, (float)prevY + offset.y, (float)prevZ + offset.z);
        particle.setVelocity(getVelocityX() + velocity.x, getVelocityY() + velocity.y, getVelocityZ() + velocity.z);
        return particle;
    }
}

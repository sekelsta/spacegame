package sekelsta.engine.entity;

import java.nio.ByteBuffer;

import sekelsta.engine.network.ByteVector;
import sekelsta.math.Matrix3f;
import sekelsta.math.Vector3f;

public abstract class Movable implements Entity {
    public static final double RESOLUTION = 65536; // One meter
    public static final float ANGLE_RESOLUTION = 1f; // Full circle
    private static final float FLOAT_PI = (float)Math.PI;
    private int id = -1;
    protected IEntitySpace world;
    protected IController controller = null;
    private double x, y, z;
    private double prevX, prevY, prevZ;
    private int velocityX, velocityY, velocityZ;

    // Angular values range from 0.0 to 1.0
    private float yaw, pitch, roll;
    private float prevYaw, prevPitch, prevRoll;
    private float angularVelocityX, angularVelocityY, angularVelocityZ;

    // 0.99 or 0.98 is like ice
    // 0.8 is like land
    // We're in space now, so set this high
    protected float drag = 1.0f;
    protected float angularDrag = 1.0f;

    public Movable(double x, double y, double z) {
        teleport(x, y, z);
    }

    public Movable(ByteBuffer buffer) {
        id = buffer.getInt();
        x = buffer.getDouble();
        y = buffer.getDouble();
        z = buffer.getDouble();
        prevX = x;
        prevY = y;
        prevZ = z;
        velocityX = buffer.getInt();
        velocityY = buffer.getInt();
        velocityZ = buffer.getInt();
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

    @Override
    public void encode(ByteVector buffer) {
        buffer.putInt(id);
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
        // Skip prevX, prevY, prevZ
        buffer.putInt(velocityX);
        buffer.putInt(velocityY);
        buffer.putInt(velocityZ);
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

    public void updateFrom(Movable other) {
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

    public void updateFromLate(Movable other, int ticksLate) {
        // TODO #22: Better way of handling this???
        updateFrom(other);
    }

    @Override
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
        scaleVelocity(drag);
        scaleAngularVelocity(angularDrag);
        if (controller != null)
        {
            controller.postUpdate();
        }
    }

    public boolean mayDespawn() {
        return true;
    }

    public static double toRadians(float angle) {
        return angle / ANGLE_RESOLUTION * 2 * Math.PI;
    }

    private static float fromRadians(double angle) {
        return (float)(angle / (2 * Math.PI) * ANGLE_RESOLUTION);
    }

    public void accelerate(int x, int y, int z) {
        velocityX += x;
        velocityY += y;
        velocityZ += z;
    }

    public void setVelocity(int x, int y, int z) {
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

    public void tick() {
        prevX = x;
        prevY = y;
        prevZ = z;
        x += velocityX / RESOLUTION;
        y += velocityY / RESOLUTION;
        z += velocityZ / RESOLUTION;

        prevYaw = yaw;
        prevRoll = roll;
        prevPitch = pitch;

        // Combine rotations with matrices
        // TO_OPTIMIZE: Quaternions might be faster
        Matrix3f rotation = new Matrix3f();
        rotation.rotate(angularVelocityZ * 2 * FLOAT_PI, angularVelocityX * 2 * FLOAT_PI, angularVelocityY * 2 * FLOAT_PI);
        rotation.rotate(yaw * 2 * FLOAT_PI, pitch * 2 * FLOAT_PI, roll * 2 * FLOAT_PI);

        this.yaw = fromRadians(rotation.getYaw());
        this.pitch = fromRadians(rotation.getPitch());
        this.roll = fromRadians(rotation.getRoll());

        yaw %= ANGLE_RESOLUTION;
        pitch %= ANGLE_RESOLUTION;
        roll %= ANGLE_RESOLUTION;

        // If yaw and roll just changed by 180 degrees, adjust prevYaw, prevPitch, and prevRoll to match
        float ninetyDegrees = ANGLE_RESOLUTION / 4;
        if (getPositiveAngleBetween(yaw, prevYaw) > ninetyDegrees 
                && getPositiveAngleBetween(roll, prevRoll) > ninetyDegrees) {
            prevYaw += ANGLE_RESOLUTION / 2;
            prevRoll += ANGLE_RESOLUTION / 2;
            prevPitch = ANGLE_RESOLUTION / 2 - prevPitch;

            prevYaw %= ANGLE_RESOLUTION;
            prevPitch %= ANGLE_RESOLUTION;
            prevRoll %= ANGLE_RESOLUTION;
        }
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

    public int getVelocityX() {
        return velocityX;
    }

    public int getVelocityY() {
        return velocityY;
    }

    public int getVelocityZ() {
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
        return interpolateAngle(yaw, prevYaw, lerp) / ANGLE_RESOLUTION * 2 * FLOAT_PI;
    }

    public float getInterpolatedPitch(float lerp) {
        return interpolateAngle(pitch, prevPitch, lerp) / ANGLE_RESOLUTION * 2 * FLOAT_PI;
    }

    public float getInterpolatedRoll(float lerp) {
        return interpolateAngle(roll, prevRoll, lerp) / ANGLE_RESOLUTION * 2 * FLOAT_PI;
    }

    private float interpolateAngle(float current, float prev, float lerp) {
        if (current - prev > ANGLE_RESOLUTION / 2) {
            current -= ANGLE_RESOLUTION;
        }
        else if (prev - current > ANGLE_RESOLUTION / 2) {
            current += ANGLE_RESOLUTION;
        }
        return lerp * current + (1 - lerp) * prev;
    }

    private float getPositiveAngleBetween(float theta, float phi) {
        float diff = Math.abs(theta - phi) % ANGLE_RESOLUTION;
        return (float)Math.min(diff, ANGLE_RESOLUTION - diff);
    }

    public double distSquared(Movable other) {
        return distSquared(other.getX(), other.getY(), other.getZ());
    }

    public double distSquared(double x, double y, double z) {
        double distX = getX() - x;
        double distY = getY() - y;
        double distZ = getZ() - z;
        return distX * distX + distY * distY + distZ * distZ;
    }

    // TODO #32: Stay more consistent about working with floats / ints
    public void accelerateForwards(int amount) {
        // Formula obtained by transforming the forward vector (0, 1, 0) by the rotation matrix from yaw, pitch, and roll
        double dx = -1 * Math.cos(toRadians(pitch)) * Math.sin(toRadians(yaw));
        double dy = Math.cos(toRadians(pitch)) * Math.cos(toRadians(yaw));
        double dz = Math.sin(toRadians(pitch));
        accelerate((int)(dx * amount), (int)(dy * amount), (int)(dz * amount));
    }

    public void angularAccelerateLocalAxis(float amount, float x, float y, float z) {
        // TO_OPTIMIZE: For axis-aligned rotations, the full matrix is not really needed
        Vector3f axis = new Vector3f(x, y, z);
        axis.rotate((float)toRadians(yaw), (float)toRadians(pitch), (float)toRadians(roll));

        Matrix3f rotation = new Matrix3f();
        rotation.rotate((float)toRadians(amount), axis.x, axis.y, axis.z);

        this.angularVelocityX += fromRadians(rotation.getPitch());
        this.angularVelocityY += fromRadians(rotation.getRoll());
        this.angularVelocityZ += fromRadians(rotation.getYaw());
    }
}

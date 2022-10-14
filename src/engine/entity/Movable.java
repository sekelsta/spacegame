package sekelsta.engine.entity;

import sekelsta.math.Matrix3f;
import sekelsta.math.Vector3f;

public abstract class Movable implements Entity {
    public static final double RESOLUTION = 65536;
    public static final float ANGLE_RESOLUTION = 65536; // Integer units per full circle
    private static final float FLOAT_PI = (float)Math.PI;
    private double x, y, z;
    private double prevX, prevY, prevZ;
    private int velocityX, velocityY, velocityZ;

    // TODO: Consider storing radians directly instead of converting constantly
    private int yaw, pitch, roll;
    private int prevYaw, prevPitch, prevRoll;
    private int angularVelocityX, angularVelocityY, angularVelocityZ;

    // 0.99 or 0.98 is like ice
    // 0.8 is like land
    // We're in space now, so set this high
    protected float drag = 1.0f;
    protected float angularDrag = 1.0f;

    public Movable(double x, double y, double z) {
        teleport(x, y, z);
    }

    public void update() {
        tick();
        scaleVelocity(drag);
        scaleAngularVelocity(angularDrag);
    }

    public static double toRadians(int angle) {
        return angle / ANGLE_RESOLUTION * 2 * Math.PI;
    }

    private static int fromRadians(double angle) {
        return (int)(angle / (2 * Math.PI) * ANGLE_RESOLUTION);
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

    public void angularAccelerate(int x, int y, int z) {
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
        rotation.rotate((float)toRadians(angularVelocityZ), (float)toRadians(angularVelocityX), (float)toRadians(angularVelocityY));
        rotation.rotate((float)toRadians(yaw), (float)toRadians(pitch), (float)toRadians(roll));

        this.yaw = fromRadians(rotation.getYaw());
        this.pitch = fromRadians(rotation.getPitch());
        this.roll = fromRadians(rotation.getRoll());

        yaw %= ANGLE_RESOLUTION;
        pitch %= ANGLE_RESOLUTION;
        roll %= ANGLE_RESOLUTION;
    }

    public final void teleport(double x, double y, double z) {
        this.x = this.prevX = x;
        this.y = this.prevY = y;
        this.z = this.prevZ = z;
    }

    public final void setAngle(int y, int p, int r) {
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

    public int getYaw() {
        return yaw;
    }

    public int getPitch() {
        return pitch;
    }

    public int getRoll() {
        return roll;
    }

    public int getYawVelocity() {
        return angularVelocityZ;
    }

    public int getPitchVelocity() {
        return angularVelocityX;
    }

    public int getRollVelocity() {
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

    private float interpolateAngle(int current, int prev, float lerp) {
        if (current - prev > (ANGLE_RESOLUTION / 2)) {
            current -= ANGLE_RESOLUTION;
        }
        else if (prev - current > (ANGLE_RESOLUTION / 2)) {
            current += ANGLE_RESOLUTION;
        }
        return lerp * current + (1 - lerp) * prev;
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

    // TODO: Stay more consistent about working with floats / ints
    public void accelerateForwards(int amount) {
        // Formula obtained by transforming the forward vector (0, 1, 0) by the rotation matrix from yaw, pitch, and roll
        double dx = -1 * Math.cos(toRadians(pitch)) * Math.sin(toRadians(yaw));
        double dy = Math.cos(toRadians(pitch)) * Math.cos(toRadians(yaw));
        double dz = Math.sin(toRadians(pitch));
        accelerate((int)(dx * amount), (int)(dy * amount), (int)(dz * amount));
    }

    public void angularAccelerateLocalAxis(int amount, float x, float y, float z) {
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

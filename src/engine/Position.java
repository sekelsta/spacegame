package sekelsta.engine;

import sekelsta.math.Matrix4f;
import sekelsta.math.Vector3f;
import sekelsta.math.Vector4f;

public class Position {
    public static final double RESOLUTION = 65536;
    public static final float ANGLE_RESOLUTION = 65536; // Integer units per full circle
    private static final float FLOAT_PI = (float)Math.PI;
    private double x, y, z;
    private double prevX, prevY, prevZ;
    private int velocityX, velocityY, velocityZ;

    private int yaw, pitch, roll;
    private int prevYaw, prevPitch, prevRoll;
    private int velocityYaw, velocityPitch, velocityRoll;

    public Position(double x, double y, double z) {
        teleport(x, y, z);
    }

    public static double toRadians(int angle) {
        return angle / ANGLE_RESOLUTION * 2 * Math.PI;
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

    public void scaleVelocity(float drag) {
        velocityX *= drag;
        velocityY *= drag;
        velocityZ *= drag;
    }

    public void angularAccelerate(int y, int p, int r) {
        velocityYaw += y;
        velocityPitch += p;
        velocityRoll += r;
    }

    public void scaleAngularVelocity(float drag) {
        velocityYaw *= drag;
        velocityPitch *= drag;
        velocityRoll *= drag;
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

        yaw += velocityYaw;
        pitch += velocityPitch;
        roll += velocityRoll;

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
        return velocityYaw;
    }

    public int getPitchVelocity() {
        return velocityPitch;
    }

    public int getRollVelocity() {
        return velocityRoll;
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

    public double distSquared(Position other) {
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
}

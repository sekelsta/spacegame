package sekelsta.engine;

public class Position {
    //public static final float RESOLUTION = 65536;
    public static final int RESOLUTION = 64;
    public static final float ANGLE_RESOLUTION = 65536; // Integer units per full circle
    private static final float FLOAT_PI = (float)Math.PI;
    private long x, y, z;
    private long prevX, prevY, prevZ;
    private int velocityX, velocityY, velocityZ;

    private int yaw, pitch, roll;
    private int prevYaw, prevPitch, prevRoll;
    private int velocityYaw, velocityPitch, velocityRoll;

    public Position(long x, long y, long z) {
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
        x += velocityX;
        y += velocityY;
        z += velocityZ;

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

    public final void teleport(long x, long y, long z) {
        this.x = this.prevX = x;
        this.y = this.prevY = y;
        this.z = this.prevZ = z;
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public long getZ() {
        return z;
    }

    public float getInterpolatedX(float lerp) {
        return (lerp * x + (1 - lerp) * prevX) / RESOLUTION;
    }

    public float getInterpolatedY(float lerp) {
        return (lerp * y + (1 - lerp) * prevY) / RESOLUTION;
    }

    public float getInterpolatedZ(float lerp) {
        return (lerp * z + (1 - lerp) * prevZ) / RESOLUTION;
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

    public long distSquared(Position other) {
        long x = getX() - other.getX();
        long y = getY() - other.getY();
        long z = getZ() - other.getZ();
        return x * x + y * y + z * z;
    }

    public long distSquared(long x, long y, long z) {
        long distX = getX() - x;
        long distY = getY() - y;
        long distZ = getZ() - z;
        return distX * distX + distY * distY + distZ * distZ;
    }
}

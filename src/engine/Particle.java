package sekelsta.engine;

public class Particle {
    protected float x, y, z;
    protected float prevX, prevY, prevZ;
    protected float velocityX, velocityY, velocityZ;
    protected float drag = 0.999f;

    protected int age = 0;
    protected int lifespan;

    public Particle(float x, float y, float z, int lifespan) {
        this(x, y, z, lifespan, x, y, z);
    }

    public Particle(float x, float y, float z, int lifespan, float prevX, float prevY, float prevZ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.prevX = prevX;
        this.prevY = prevY;
        this.prevZ = prevZ;
        this.lifespan = lifespan;
    }

    public float getInterpolatedX(float lerp) {
        return lerp * x + (1 - lerp) * prevX;
    }

    public float getInterpolatedY(float lerp) {
        return lerp * y + (1 - lerp) * prevY;
    }

    public float getInterpolatedZ(float lerp) {
        return lerp * z + (1 - lerp) * prevZ;
    }

    public float getRelativeAge(float lerp) {
        return (age + lerp) / lifespan;
    }

    public void update() {
        prevX = x;
        prevY = y;
        prevZ = z;
        x += velocityX;
        y += velocityY;
        z += velocityZ;
        velocityX *= drag;
        velocityY *= drag;
        velocityZ *= drag;

        age += 1;
    }

    public boolean isDead() {
        return age >= lifespan;
    }

    public void setVelocity(float vx, float vy, float vz) {
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
    }
}

package sekelsta.game.entity;

import java.nio.ByteBuffer;
import java.util.Random;
import sekelsta.engine.Particle;
import sekelsta.engine.entity.*;
import sekelsta.engine.network.ByteVector;
import sekelsta.game.World;
import shadowfox.math.Vector3f;

public class Spaceship extends Entity implements ICollider {
    public static final int NUM_SKINS = 3;
    private static final float shootSpeed = (float)Entity.ONE_METER;
    private static final float thrustSpeed = (float)Entity.ONE_METER / 32;
    private static final float reverseSpeed = -1 * (float)Entity.ONE_METER / 64;
    private final float angularAcceleration = Entity.TAU / 4096;

    public int skin;
    private boolean isThrusting = false;
    private boolean wasThrusting = false;
    private boolean isReversing = false;
    private boolean wasReversing = false;

    public Spaceship(int x, int y, int z) {
        this(x, y, z, null);
    }

    public Spaceship(int x, int y, int z, IController controller) {
        super(x, y, z);
        this.controller = controller;
        angularDrag = 0.7f;
    }

    public Spaceship(ByteBuffer buffer) {
        super(buffer);
        skin = buffer.getInt();
        isThrusting = buffer.get() != 0;
        isReversing = buffer.get() != 0;
    }

    @Override
    public boolean mayDespawn() {
        return false;
    }

    @Override
    public void encode(ByteVector buffer) {
        super.encode(buffer);
        buffer.putInt(skin);
        buffer.put((isThrusting || wasThrusting)? (byte)1 : (byte)0);
        buffer.put((isReversing || wasReversing)? (byte)1 : (byte)0);
    }

    @Override
    public void updateFrom(Entity other) {
        super.updateFrom(other);
        Spaceship ship = (Spaceship)other;
        isThrusting = ship.isThrusting;
        isReversing = ship.isReversing;
    }

    @Override
    public double getCollisionRadius() {
        return 3.0;
    }

    @Override
    public EntityType getType() {
        return Entities.SPACESHIP;
    }

    @Override
    public void collide(Entity other) {
        // TODO #26: Get injured/die    
    }

    @Override
    protected void tick() {
        if (isThrusting) {
            Vector3f spawnPoint = new Vector3f(0, -1.3f, 0);
            spawnPoint.rotate(yaw, pitch, roll);
            // Ratio of minimum backwards velocity to sideways velocity
            float velocityRatio = 6;
            float maxVelocity = -16 * thrustSpeed;
            for (int i = 0; i < 20; ++i) {
                spawnParticle(spawnPoint, velocityRatio, maxVelocity);
            }
        }
        if (isReversing) {
            Vector3f spawnPoint = new Vector3f(0, 2.3f, 0);
            spawnPoint.rotate(yaw, pitch, roll);
            // Ratio of minimum backwards velocity to sideways velocity
            float velocityRatio = 8;
            float maxVelocity = -16 * reverseSpeed;
            for (int i = 0; i < 10; ++i) {
                spawnParticle(spawnPoint, velocityRatio, maxVelocity);
            }
        }

        wasThrusting = isThrusting;
        isThrusting = false;
        wasReversing = isReversing;
        isReversing = false;
        super.tick();
    }

    @Override
    public Vector3f getEyeOffset() {
        return new Vector3f(0, 2.204f, 0.365f);
    }

    private void spawnParticle(Vector3f spawnPoint, float velocityRatio, float maxVelocity) {
        Random random = world.getRandom();
        int lifespan = random.nextInt(15) + 15;
        Particle particle = new Particle((float)getX() + spawnPoint.x, (float)getY() + spawnPoint.y, (float)getZ() + spawnPoint.z, lifespan);
        float vy = random.nextFloat() * maxVelocity;
        float vx = 1;
        float vz = 1;
        while (vx * vx + vz * vz > 0.5f * 0.5f) {
            vx = random.nextFloat() - 0.5f;
            vz = random.nextFloat() - 0.5f;
        }
        float s = Math.abs(vy * 2 / velocityRatio);
        vx *= s;
        vz *= s;
        Vector3f velocity = new Vector3f(vx, vy, vz);
        velocity.rotate(yaw, pitch, roll);
        particle.setVelocity(getVelocityX() + velocity.x, getVelocityY() + velocity.y, getVelocityZ() + velocity.z);
        world.addParticle(particle);
    }

    public void thrust() {
        accelerateForwards(thrustSpeed);
        isThrusting = true;
    }

    public void reverse() {
        accelerateForwards(reverseSpeed);
        isReversing = true;
    }

    public void pitchUp() {
        angularAccelerateLocalAxis(angularAcceleration, 1, 0, 0);
    }

    public void pitchDown() {
        angularAccelerateLocalAxis(-1 * angularAcceleration, 1, 0, 0);
    }

    public void yawLeft() {
        angularAccelerateLocalAxis(angularAcceleration, 0, 0, 1);
    }

    public void yawRight() {
        angularAccelerateLocalAxis(-1 * angularAcceleration, 0, 0, 1);
    }

    // Counterclockwise as viewed from rear
    public void rollCounterclockwise() {
        angularAccelerateLocalAxis(-1 * angularAcceleration, 0, 1, 0);
    }

    // Clockwise as viewed from rear
    public void rollClockwise() {
        angularAccelerateLocalAxis(angularAcceleration, 0, 1, 0);
    }

    public void fire() {
        Entity projectile = world.spawn(new Projectile(this, getX(), getY(), getZ()));
        projectile.accelerate(getVelocityX(), getVelocityY(), getVelocityZ());
        projectile.setAngle(getYaw(), getPitch(), getRoll());
        projectile.accelerateForwards(shootSpeed);
    }
}

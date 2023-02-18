package sekelsta.game.entity;

import java.nio.ByteBuffer;
import java.util.Random;
import sekelsta.engine.Particle;
import sekelsta.engine.entity.*;
import sekelsta.engine.network.ByteVector;
import sekelsta.game.Input;
import sekelsta.game.RemotePlayer;
import sekelsta.game.World;
import shadowfox.math.Vector3f;

public class Spaceship extends Entity implements ICollider {
    public static final int NUM_SKINS = 3;
    private static final float shootSpeed = (float)Entity.ONE_METER;
    private static final float thrustSpeed = (float)Entity.ONE_METER / 32;
    private static final float reverseSpeed = -1 * (float)Entity.ONE_METER / 64;
    private final float angularAcceleration = Entity.TAU / 2048;

    public int skin;
    private boolean isThrusting = false;
    private boolean wasThrusting = false;
    private boolean isReversing = false;
    private boolean wasReversing = false;

    public Spaceship() {
        this(0, 0, 0, null);
    }

    public Spaceship(int x, int y, int z) {
        this(x, y, z, null);
    }

    public Spaceship(IController controller) {
        this(0, 0, 0, controller);
    }


    public Spaceship(int x, int y, int z, IController controller) {
        super(x, y, z);
        this.controller = controller;
        drag = 0.999f;
        angularDrag = 0.7f;
    }

    public Spaceship(ByteBuffer buffer) {
        super(buffer);
        skin = buffer.getInt();
        isThrusting = buffer.get() != 0;
        isReversing = buffer.get() != 0;
    }

    public boolean isLocalPlayer() {
        return controller instanceof Input;
    }

    public boolean isControlledBy(Long connectionID) {
        if (connectionID == null) {
            return controller instanceof Input;
        }

        if (controller instanceof RemotePlayer) {
            return ((RemotePlayer)controller).connectionID == connectionID;
        }

        return false;
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
        if (other instanceof Projectile) {
            Projectile projectile = (Projectile)other;
            if (projectile.isOwnedBy(this)) {
                return;
            }
        }
        ((World)world).destroyShip(this);
    }

    @Override
    public void update() {
        super.update();
        if (isThrusting) {
            Vector3f spawnPoint = new Vector3f(0, -1.3f, 0);
            spawnPoint.rotate(yaw, pitch, roll);
            // Ratio of minimum backwards velocity to sideways velocity
            float velocityRatio = 4;
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
    }

    @Override
    public Vector3f getEyeOffset() {
        return new Vector3f(0, 2.204f, 0.365f);
    }

    private void spawnParticle(Vector3f spawnPoint, float velocityRatio, float maxVelocity) {
        Random random = world.getRandom();
        int lifespan = random.nextInt(15) + 15;

        Vector3f velocity = Vector3f.randomNonzero(random);
        velocity.y = Math.abs(velocity.y);
        float s = velocity.y * maxVelocity / velocityRatio;
        velocity.scale(s, maxVelocity, s);

        velocity.rotate(yaw, pitch, roll);
        Particle particle = getParticleRelative(spawnPoint, lifespan, velocity);
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
        Entity projectile = new Projectile(this, getX(), getY(), getZ());
        projectile.accelerate(getVelocityX(), getVelocityY(), getVelocityZ());
        projectile.setAngle(getYaw(), getPitch(), getRoll());
        projectile.accelerateForwards(shootSpeed);
        ((World)world).clientSpawn(projectile);
    }

    public void explode() {
        Random random = world.getRandom();
        for (int i = 0; i < 300; ++i) {
            int lifespan = random.nextInt(20) + 20;
            Vector3f velocity = Vector3f.randomNonzero(random);
            velocity.scale(0.5f);
            Particle particle = getParticleRelative(new Vector3f(), lifespan, velocity);
            world.addParticle(particle);
        }
    }
}

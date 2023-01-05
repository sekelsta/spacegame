package sekelsta.game.entity;

import java.nio.ByteBuffer;

import sekelsta.engine.entity.*;
import sekelsta.engine.network.ByteVector;
import sekelsta.game.World;
import sekelsta.math.Vector3f;

public class Spaceship extends Movable implements ICollider {
    public static final int NUM_SKINS = 3;
    int shootSpeed = (int)Movable.RESOLUTION;
    private final int angularAcceleration = (int)(Movable.ANGLE_RESOLUTION / 1024);

    public int skin;

    public Spaceship(int x, int y, int z) {
        this(x, y, z, null);
    }

    public Spaceship(int x, int y, int z, IController controller) {
        super(x, y, z);
        this.controller = controller;
        angularDrag = 0.9f;
    }

    public Spaceship(ByteBuffer buffer) {
        super(buffer);
        skin = buffer.getInt();
    }

    @Override
    public boolean mayDespawn() {
        return false;
    }

    @Override
    public void encode(ByteVector buffer) {
        super.encode(buffer);
        // TODO
        buffer.putInt(skin);
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
        // TODO: Get injured/die    
    }

    @Override
    public Vector3f getEyeOffset() {
        return new Vector3f(0, 2.204f, 0.365f);
    }

    public void thrust() {
        accelerateForwards((int)Movable.RESOLUTION / 16);
    }

    public void reverse() {
        accelerateForwards(-1 * (int)Movable.RESOLUTION / 16);
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
        Movable projectile = world.spawn(new Projectile(this, getX(), getY(), getZ()));
        projectile.accelerate(getVelocityX(), getVelocityY(), getVelocityZ());
        projectile.setAngle(getYaw(), getPitch(), getRoll());
        projectile.accelerateForwards(shootSpeed);
    }
}

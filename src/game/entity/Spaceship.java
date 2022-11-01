package sekelsta.game.entity;

import java.nio.ByteBuffer;

import sekelsta.engine.entity.*;
import sekelsta.engine.network.ByteVector;
import sekelsta.game.World;

public class Spaceship extends Movable implements ICollider {
    public static final int NUM_SKINS = 3;
    int shootSpeed = (int)Movable.RESOLUTION;
    private final int angularAcceleration = (int)(Movable.ANGLE_RESOLUTION / 1024);

    protected Controller controller;
    public int skin;

    public Spaceship(int x, int y, int z) {
        this(x, y, z, null);
    }

    public Spaceship(int x, int y, int z, Controller controller) {
        super(x, y, z);
        this.controller = controller;
        angularDrag = 0.9f;
    }

    public Spaceship(ByteBuffer buffer) {
        super(buffer);
        // TODO
        this.controller = null;
        skin = buffer.getInt();
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
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
    public void update() {
        if (controller != null)
        {
            controller.update();
        }
        super.update();
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

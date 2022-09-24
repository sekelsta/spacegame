package sekelsta.game.entity;

import sekelsta.engine.entity.Entity;
import sekelsta.engine.entity.EntityType;
import sekelsta.engine.entity.ICollidable;
import sekelsta.engine.entity.Movable;
import sekelsta.game.World;

public class Spaceship extends Movable implements ICollidable {
    int shootSpeed = (int)Movable.RESOLUTION;
    private final int angularAcceleration = (int)(Movable.ANGLE_RESOLUTION / 1024);

    protected final Controller controller;
    private final World world;

    public Spaceship(int x, int y, int z, World world) {
        this(x, y, z, world, null);
    }

    public Spaceship(int x, int y, int z, World world, Controller controller) {
        super(x, y, z);
        this.world = world;
        this.controller = controller;
        angularDrag = 0.9f;
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
        Movable projectile = world.spawn(new Projectile(this, getX(), getY(), getZ(), world));
        projectile.accelerate(getVelocityX(), getVelocityY(), getVelocityZ());
        projectile.setAngle(getYaw(), getPitch(), getRoll());
        projectile.accelerateForwards(shootSpeed);
    }
}

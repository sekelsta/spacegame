package sekelsta.game.entity;

import sekelsta.engine.Position;
import sekelsta.game.World;

public class Spaceship extends Mob {
    int shootSpeed = (int)Position.RESOLUTION;
    private final int angularAcceleration = (int)(Position.ANGLE_RESOLUTION / 1024);

    public Spaceship(int x, int y, int z, World world) {
        super(Entities.SPACESHIP, x, y, z, world);
    }

    public Spaceship(int x, int y, int z, World world, Controller controller) {
        super(Entities.SPACESHIP, x, y, z, world, controller);
    }

    @Override
    public double getCollisionRadius() {
        return 3.0;
    }

    @Override
    public boolean hasCollisions() {
        return true;    
    }

    public void thrust() {
        getPosition().accelerateForwards((int)Position.RESOLUTION / 16);
    }

    public void reverse() {
        getPosition().accelerateForwards(-1 * (int)Position.RESOLUTION / 16);
    }

    public void pitchUp() {
        getPosition().angularAccelerateLocalAxis(angularAcceleration, 1, 0, 0);
    }

    public void pitchDown() {
        getPosition().angularAccelerateLocalAxis(-1 * angularAcceleration, 1, 0, 0);
    }

    public void yawLeft() {
        getPosition().angularAccelerateLocalAxis(angularAcceleration, 0, 0, 1);
    }

    public void yawRight() {
        getPosition().angularAccelerateLocalAxis(-1 * angularAcceleration, 0, 0, 1);
    }

    // Counterclockwise as viewed from rear
    public void rollCounterclockwise() {
        getPosition().angularAccelerateLocalAxis(-1 * angularAcceleration, 0, 1, 0);
    }

    // Clockwise as viewed from rear
    public void rollClockwise() {
        getPosition().angularAccelerateLocalAxis(angularAcceleration, 0, 1, 0);
    }

    public void fire() {
        Mob projectile = world.spawn(new Projectile(this, position.getX(), position.getY(), position.getZ(), world));
        projectile.addVelocity(position.getVelocityX(), position.getVelocityY(), position.getVelocityZ());
        projectile.getPosition().setAngle(getPosition().getYaw(), getPosition().getPitch(), getPosition().getRoll());
        projectile.getPosition().accelerateForwards(shootSpeed);
    }

    @Override
    public void updateMovement() {
        super.updateMovement();
        position.scaleAngularVelocity(0.9f);
    }
}

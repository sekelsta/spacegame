package sekelsta.game.entity;

import sekelsta.engine.Position;
import sekelsta.game.World;

public class Spaceship extends Mob {
    int shootSpeed = 1 * Position.RESOLUTION;
    private final int angularAcceleration = (int)(Position.ANGLE_RESOLUTION / 1024);

    public Spaceship(int x, int y, int z, World world) {
        super(Entities.SPACESHIP, x, y, z, world);
    }

    public Spaceship(int x, int y, int z, World world, Controller controller) {
        super(Entities.SPACESHIP, x, y, z, world, controller);
    }

    @Override
    public int getCollisionRadius() {
        return 3 * Position.RESOLUTION;
    }

    @Override
    public boolean hasCollisions() {
        return true;    
    }

    public void thrust() {
        getPosition().accelerateForwards(Position.RESOLUTION / 16);
    }

    public void reverse() {
        getPosition().accelerateForwards(-1 * Position.RESOLUTION / 16);
    }

    public void pitchUp() {
        getPosition().angularAccelerate(0, angularAcceleration, 0);
    }

    public void pitchDown() {
        getPosition().angularAccelerate(0, -1 * angularAcceleration, 0);
    }

    public void yawLeft() {
        getPosition().angularAccelerate(angularAcceleration, 0, 0);
    }

    public void yawRight() {
        getPosition().angularAccelerate(-1 * angularAcceleration, 0, 0);
    }

    // Counterclockwise as viewed from rear
    public void rollCounterclockwise() {
        getPosition().angularAccelerate(0, 0, -1 * angularAcceleration);
    }

    // Clockwise as viewed from rear
    public void rollClockwise() {
        getPosition().angularAccelerate(0, 0, angularAcceleration);
    }

    public void fire() {
        Mob projectile = world.spawn(new Projectile(this, position.getX(), position.getY(), position.getZ(), world));
        projectile.addVelocity(position.getVelocityX(), position.getVelocityY(), position.getVelocityZ());
        projectile.getPosition().setAngle(getPosition().getYaw(), getPosition().getPitch(), getPosition().getRoll());
        projectile.getPosition().accelerateForwards(shootSpeed);
    }
}

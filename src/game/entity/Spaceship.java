package sekelsta.game.entity;

import sekelsta.engine.Position;
import sekelsta.game.World;

public class Spaceship extends Mob {
    int shootSpeed = 1 * Position.RESOLUTION;

    public Spaceship(int x, int y, int z, World world) {
        super(Entities.SPACESHIP, x, y, z, world);
    }

    public Spaceship(int x, int y, int z, World world, Controller controller) {
        super(Entities.SPACESHIP, x, y, z, world, controller);
    }

    public void fire() {
        Mob projectile = world.spawn(new Mob(Entities.PROJECTILE, position.getX(), position.getY(), position.getZ(), world));
        projectile.addVelocity(position.getVelocityX(), position.getVelocityY(), position.getVelocityZ());
        // TODO: Handle pitch and roll
        double yaw = Position.toRadians(position.getYaw());
        int vx = (int)(-1 * shootSpeed * Math.sin(yaw));
        int vy = (int)(shootSpeed * Math.cos(yaw));
        projectile.addVelocity(vx, vy, 0);
    }
}

package sekelsta.game.entity;

import sekelsta.engine.Position;
import sekelsta.game.World;

public class Mob extends Entity {
    protected final Controller controller;
    protected final World world;

    // 0.99 or 0.98 is like ice
    // 0.8 is like land
    // We're in space now, so set this high
    protected float drag = 1.0f;

    public Mob(EntityType type, long x, long y, long z, World world) {
        this(type, x, y, z, world, null);
    }

    public Mob(EntityType type, long x, long y, long z, World world, Controller controller) {
        super(type, x, y, z);
        this.controller = controller;
        this.world = world;
    }

    public void update() {
        if (controller != null)
        {
            controller.update(this);
        }
        updateMovement();
    }

    public void addVelocity(int x, int y, int z) {
        position.accelerate(x, y, z);
    }

    protected void updateMovement() {
        position.tick();
        position.scaleVelocity(drag);
        position.scaleAngularVelocity(drag);

    }
/* TODO: Use this or delete it
    private void faceTowardsVelocity() {
        float vx = position.getVelocityX();
        float vy = position.getVelocityY();
        double speed = Math.sqrt(vx * vx + vy * vy);

        if (speed > Position.RESOLUTION / 128 && speed > 1) {
            int aimingYaw = (int)(Position.ANGLE_RESOLUTION * Math.acos(vy / speed) / Math.PI / 2);
            if (vx > 0) {
               aimingYaw *= -1;
            }

            int yawV = position.getYawVelocity();
            float foretold = 1 + drag + drag * drag;
            int diff = aimingYaw - position.getYaw() - (int)(yawV * foretold);
            diff %= Position.ANGLE_RESOLUTION;
            if (diff > Position.ANGLE_RESOLUTION / 2) {
                diff -= Position.ANGLE_RESOLUTION;
            }
            else if (diff < -Position.ANGLE_RESOLUTION / 2) {
                diff += Position.ANGLE_RESOLUTION;
            }
            position.angularAccelerate(diff / 8, 0, 0);
        }
    }*/

    public int getAccelerationXY() {
        return (int)(Position.RESOLUTION / 16);
    }
}

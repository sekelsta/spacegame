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

    public Mob(EntityType type, double x, double y, double z, World world) {
        this(type, x, y, z, world, null);
    }

    public Mob(EntityType type, double x, double y, double z, World world, Controller controller) {
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
}

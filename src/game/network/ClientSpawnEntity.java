package sekelsta.game.network;

import sekelsta.engine.entity.Entity;
import sekelsta.engine.network.*;
import sekelsta.game.Game;
import sekelsta.game.World;
import sekelsta.game.entity.Projectile;
import sekelsta.game.entity.Spaceship;

public class ClientSpawnEntity extends ServerSpawnEntity {
    public ClientSpawnEntity() {}

    public ClientSpawnEntity(Entity entity) {
        super(entity);
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.CLIENT_TO_SERVER;
    }

    @Override
    public void handle(INetworked game) {
        // Cheat prevention
        // Players are only allowed to spawn projectiles
        if (!(entity instanceof Projectile)) {
            return;
        }
        // Owner must be a valid spaceship
        World world = ((Game)game).getWorld();
        Projectile projectile = (Projectile)entity;
        Entity owner = world.getEntityByID(projectile.getOwnerID());
        if (!(owner instanceof Spaceship)) {
            return;
        }
        // Owner must be the player who sent this message
        Spaceship spaceship = (Spaceship)owner;
        if (!spaceship.isControlledBy(sender.getID())) {
            return;
        }
        // Projectile must spawn reasonably near to the shooting ship
        if (spaceship.distSquared(projectile) > spaceship.getCollisionRadius() * spaceship.getCollisionRadius()) {
            return;
        }

        // All checks passed, spawn the entity
        world.spawn(entity);
    }
}

package sekelsta.game.network;

import java.nio.ByteBuffer;

import sekelsta.engine.Log;
import sekelsta.engine.entity.Entity;
import sekelsta.engine.network.*;
import sekelsta.game.Game;
import sekelsta.game.World;
import sekelsta.game.entity.Spaceship;

public class ServerExplodeShip extends Message {
    private int entityID;

    public ServerExplodeShip() {}

    public ServerExplodeShip(int entityID) {
        this.entityID = entityID;
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.SERVER_TO_CLIENT;
    }

    @Override
    public void encode(ByteVector buffer) {
        buffer.putInt(entityID);
    }

    @Override
    public void decode(ByteBuffer buffer) {
        entityID = buffer.getInt();
    }

    @Override
    public void handle(INetworked game) {
        World world = ((Game)game).getWorld();
        Entity entity = world.getEntityByID(entityID);
        if (! (entity instanceof Spaceship)) {
            Log.debug("ServerExplodeShip message received for invalid entity ID " + entityID + ", entity " + entity);
            return;
        }
        Spaceship spaceship = (Spaceship)entity;
        spaceship.explode();
    }

}

package sekelsta.game.network;

import java.nio.ByteBuffer;

import sekelsta.engine.entity.Entity;
import sekelsta.engine.network.ByteVector;
import sekelsta.engine.network.INetworked;
import sekelsta.engine.network.Message;
import sekelsta.engine.network.NetworkDirection;
import sekelsta.game.Game;
import sekelsta.game.entity.Spaceship;

public class ServerGivePawn extends Message {
    private int entityID;

    public ServerGivePawn() {}

    public ServerGivePawn(int entityID) {
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
    public void handle(INetworked INetworked) {
        Game game = (Game)INetworked;
        // TODO #20: What if the mob isn't an instanceof Spaceship?
        // TODO #21: Don't crash if, by the time takePawn runs, a local player has already been set
        game.getWorld().runWhenEntitySpawns(mob -> game.takePawn((Spaceship)mob), entityID);
    }
}

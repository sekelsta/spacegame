package sekelsta.game.network;

import java.nio.ByteBuffer;

import sekelsta.engine.network.ByteVector;
import sekelsta.engine.network.INetworked;
import sekelsta.engine.network.Message;
import sekelsta.engine.network.NetworkDirection;
import sekelsta.game.Game;
import sekelsta.game.World;

public class ServerRemoveEntity extends Message {
    private int entityID;

    public ServerRemoveEntity() {}

    public ServerRemoveEntity(int entityID) {
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
        world.runWhenEntitySpawns(mob -> world.remove(mob), entityID);
    }
}

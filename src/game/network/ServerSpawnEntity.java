package sekelsta.game.network;

import java.nio.ByteBuffer;

import sekelsta.engine.entity.Entity;
import sekelsta.engine.entity.EntityType;
import sekelsta.engine.network.ByteVector;
import sekelsta.engine.network.INetworked;
import sekelsta.engine.network.Message;
import sekelsta.engine.network.NetworkDirection;
import sekelsta.game.Game;

public class ServerSpawnEntity extends Message {
    private Entity entity;

    public ServerSpawnEntity() {}

    public ServerSpawnEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.SERVER_TO_CLIENT;
    }

    @Override
    public void encode(ByteVector buffer) {
        buffer.putInt(entity.getType().getID());
        entity.encode(buffer);
    }

    @Override
    public void decode(ByteBuffer buffer) {
        int id = buffer.getInt();
        EntityType type = EntityType.getByID(id);
        entity = type.decode(buffer);
    }

    @Override
    public void handle(INetworked game) {
        assert(game instanceof Game);
        ((Game)game).getWorld().spawn(entity);
    }
}

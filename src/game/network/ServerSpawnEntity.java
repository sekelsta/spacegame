package sekelsta.game.network;

import java.nio.ByteBuffer;

import sekelsta.engine.entity.EntityType;
import sekelsta.engine.entity.Entity;
import sekelsta.engine.network.*;
import sekelsta.game.Game;
import sekelsta.game.World;
import sekelsta.game.RemoteController;

public class ServerSpawnEntity extends Message {
    protected Entity entity;

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
        long tickSent = context.tick;
        World world = ((Game)game).getWorld();
        long currentTick = world.getCurrentTick();

        if (tickSent > currentTick) {
            world.runDelayed(() -> spawn(world), (int)(tickSent - currentTick));
        }
        else {
            spawn(world);
        }
    }

    private void spawn(World world) {
        entity.setController(new RemoteController(entity));
        world.spawn(entity);
    }
}

package sekelsta.game.network;

import java.nio.ByteBuffer;

import sekelsta.engine.entity.*;
import sekelsta.engine.network.*;
import sekelsta.game.Game;
import sekelsta.game.World;
import sekelsta.game.RemoteController;
import sekelsta.game.RemotePlayer;

public class EntityUpdate extends Message {
    protected Entity entity;

    public EntityUpdate() {}

    public EntityUpdate(Entity entity) {
        this.entity = entity;
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.BIDIRECTIONAL;
    }

    @Override
    public boolean reliable() {
        return false;
    }

    // TO_OPTIMIZE: Only changeable data needs to be included, not anything that stays constant the whole lifetime
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
        World world = ((Game)game).getWorld();
        Entity mob = world.getEntityByID(entity.getID());
        if (mob == null) {
            return;
        }
        IController controller = mob.getController();
        if (controller == null || !(controller instanceof RemoteController)) {
            // TODO #22: If the controller is an instanceof Input, maybe we shouldn't entirely ignore updates from the server
            return;
        }
        if (controller instanceof RemotePlayer 
            && ((RemotePlayer)controller).connectionID != sender.getID()) {
            return;
        }
        if (world.authoritative && !(controller instanceof RemotePlayer)) {
            return;
        }

        ((RemoteController)controller).handleUpdateMessage(context.tick, entity);
    }
}

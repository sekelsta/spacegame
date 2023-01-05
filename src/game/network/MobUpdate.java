package sekelsta.game.network;

import java.nio.ByteBuffer;

import sekelsta.engine.entity.*;
import sekelsta.engine.network.*;
import sekelsta.game.Game;
import sekelsta.game.RemoteController;
import sekelsta.game.RemotePlayer;

public class MobUpdate extends Message {
    protected Movable entity;

    public MobUpdate() {}

    public MobUpdate(Movable entity) {
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
        entity = (Movable)type.decode(buffer);
    }

    @Override
    public void handle(INetworked game) {
        Movable mob = ((Game)game).getWorld().getMovableByID(entity.getID());
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

        ((RemoteController)controller).handleUpdateMessage(((GameContext)context).tick, entity);
    }
}

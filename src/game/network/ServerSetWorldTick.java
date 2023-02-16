package sekelsta.game.network;

import java.nio.ByteBuffer;

import sekelsta.engine.network.ByteVector;
import sekelsta.engine.network.INetworked;
import sekelsta.engine.network.Message;
import sekelsta.engine.network.NetworkDirection;
import sekelsta.game.Game;
import sekelsta.game.World;

public class ServerSetWorldTick extends Message {
    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.SERVER_TO_CLIENT;
    }

    @Override
    public void encode(ByteVector buffer) {
        // Do nothing
    }

    @Override
    public void decode(ByteBuffer buffer) {
        // Do nothing
    }

    @Override
    public void handle(INetworked game) {
        World world = ((Game)game).getWorld();
        long tick = context.tick;
        world.setTickIfClient(tick);
    }
}

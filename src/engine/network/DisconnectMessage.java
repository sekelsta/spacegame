package sekelsta.engine.network;

import java.nio.ByteBuffer;

public class DisconnectMessage extends Message {
    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.BIDIRECTIONAL;
    }

    @Override
    public boolean reliable() {
        return false;
    }

    @Override
    public void encode(ByteVector buffer) { }

    @Override
    public void decode(ByteBuffer buffer) { }

    @Override
    public void handle(INetworked game) {
        sender.close();
        game.getNetworkManager().removeBroadcastRecipient(sender);
        game.handleDisconnect(sender.getID());
    }
}

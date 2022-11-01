package sekelsta.engine.network;

import java.nio.ByteBuffer;

public class ClientConnect extends Message {
    private long nonce;

    public ClientConnect() {}

    public ClientConnect(long nonce) {
        this.nonce = nonce;
    }

    @Override
    public boolean requiresConfirmedAddress() {
        return false;
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.CLIENT_TO_SERVER;
    }

    @Override
    public void encode(ByteVector buffer) {
        buffer.putLong(nonce);
    }

    @Override
    public void decode(ByteBuffer buffer) {
        nonce = buffer.getLong();
    }

    @Override
    public void handle(INetworked game) {
        boolean success = game.getNetworkManager().confirmPendingClient(sender, nonce);
        if (success) {
            game.clientConnectionAccepted(sender);
        }
    }

}

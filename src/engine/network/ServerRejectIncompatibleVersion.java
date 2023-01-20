package sekelsta.engine.network;

import java.nio.ByteBuffer;

import sekelsta.engine.SoftwareVersion;

public class ServerRejectIncompatibleVersion extends Message {
    private SoftwareVersion version;

    public ServerRejectIncompatibleVersion() {}

    public ServerRejectIncompatibleVersion(SoftwareVersion version) {
        this.version = version;
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.SERVER_TO_CLIENT;
    }

    @Override
    public boolean reliable() {
        return false;
    }

    @Override
    public void encode(ByteVector buffer) {
        version.encode(buffer);
    }

    @Override
    public void decode(ByteBuffer buffer) {
        version = SoftwareVersion.fromBuffer(buffer);
    }

    @Override
    public void handle(INetworked game) {
        game.getNetworkManager().removeBroadcastRecipient(sender);
        game.connectionRejected("Incompatible game version\nServer version: " + version + "\nClient version: " + game.getVersion());
    }
}

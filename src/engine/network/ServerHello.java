package sekelsta.engine.network;

import java.nio.ByteBuffer;

import sekelsta.engine.Log;
import sekelsta.engine.SoftwareVersion;

public class ServerHello extends Message {
    private SoftwareVersion version;

    public ServerHello() {}

    public ServerHello(SoftwareVersion version) {
        this.version = version;
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.SERVER_TO_CLIENT;
    }

    // Note: I'd like this to be reliable, but the one-off nature of the send means we don't actually store the
    // Connection instance that has the ack info. So instead count on this being sent in the same packet as the ack
    // for ClientHello. That way if it is lost, the ClientHello will be re-sent.
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
        if (!version.equals(game.getVersion())) {
            Log.info("Server accepted connection despite running non-matching version " + version);
        }
        game.receivedHelloFromServer(version);
    }
}

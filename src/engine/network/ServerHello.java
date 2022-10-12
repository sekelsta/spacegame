package sekelsta.engine.network;

import java.nio.ByteBuffer;

import sekelsta.engine.IGame;
import sekelsta.engine.Log;
import sekelsta.engine.SoftwareVersion;

public class ServerHello extends Message {
    // Foil casual DDOS amplification attacks. Before the server ever sends a long message to the client, we want
    // some sort of evidence that the client's address really has sent us a message. So we require the client to
    // include this same token in its join request, otherwise we'll assume the address is spoofed.
    private long nonce;
    private SoftwareVersion version;

    public ServerHello() {}

    public ServerHello(long nonce, SoftwareVersion version) {
        this.nonce = nonce;
        this.version = version;
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.SERVER_TO_CLIENT;
    }

    @Override
    public void encode(ByteVector buffer) {
        buffer.putLong(nonce);
        version.encode(buffer);
    }

    @Override
    public void decode(ByteBuffer buffer) {
        nonce = buffer.getLong();
        version = SoftwareVersion.fromBuffer(buffer);
    }

    @Override
    public void handle(IGame game) {
        if (!version.equals(game.getVersion())) {
            Log.info("Server accepted connection despite running non-matching version " + version);
        }
        Message clientConnect = new ClientConnect(nonce);
        game.getNetworkManager().queueBroadcast(clientConnect);
        game.receivedHelloFromServer(version);
    }
}

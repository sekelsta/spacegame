package sekelsta.engine.network;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

import sekelsta.engine.Log;
import sekelsta.engine.SoftwareVersion;

public class ClientHello extends Message {
    private static final int HANDSHAKE_PROTOCOL_VERSION = 0;

    private int handshakeProtocolVersion = -1;
    private String gameID;
    private SoftwareVersion version;

    public ClientHello() {}

    public ClientHello(String gameID, SoftwareVersion version) {
        this.handshakeProtocolVersion = HANDSHAKE_PROTOCOL_VERSION;
        this.gameID = gameID;
        this.version = version;
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
        buffer.putInt(handshakeProtocolVersion);
        writeString(buffer, gameID);
        version.encode(buffer);
    }

    @Override
    public void decode(ByteBuffer buffer) {
        handshakeProtocolVersion = buffer.getInt();
        gameID = readString(buffer);
        version = SoftwareVersion.fromBuffer(buffer);
    }

    @Override
    public void handle(INetworked game) {
        // Check for socket address equality, not Connection equality, in case this address connected in the time
        // between when this.sender was set and this code actually runs
        if (game.getNetworkManager().isBroadcastRecipient(sender.getSocketAddress())) {
            return;
        }
        if (this.handshakeProtocolVersion != HANDSHAKE_PROTOCOL_VERSION) {
            throw new MessageParsingException(
                "Ignoring incoming connection due to mismatched handshake protocol version"
            );
        }
        if (!game.getGameID().equals(this.gameID)) {
            throw new MessageParsingException(
                "Ignoring incoming connection attempting to join \"" + gameID + "\" (we are \"" + game.getGameID() + "\")"
            );
        }
        if (!game.getVersion().matchesMajorMinor(version)) {
            game.getNetworkManager().queueMessage(sender, new ServerRejectIncompatibleVersion(game.getVersion()));
            Log.debug("Rejecting connection from " + sender + " due to incompatible version " + version);
            return;
        }
        if (!game.getVersion().equals(version)) {
            Log.debug("Accepting connection with nearly-matching version " + version);
        }

        long nonce = new SecureRandom().nextLong();
        if (game.getNetworkManager().isPendingConnection(sender)) {
            nonce = game.getNetworkManager().getExpectedNonce(sender);
        }
        else {
            game.getNetworkManager().addPendingClient(sender, nonce);
        }
        ServerHello serverHello = new ServerHello(nonce, game.getVersion());
        game.getNetworkManager().queueMessage(sender, serverHello);
    }
}

package sekelsta.test.engine;

import java.nio.ByteBuffer;

import sekelsta.engine.network.ByteVector;
import sekelsta.engine.network.INetworked;
import sekelsta.engine.network.Message;
import sekelsta.engine.network.NetworkDirection;

class ByteMessage extends Message {
    byte[] bytes;

    public ByteMessage() {}

    public ByteMessage(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.BIDIRECTIONAL;
    }

    @Override
    public void encode(ByteVector buffer) {
        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }

    @Override
    public void decode(ByteBuffer buffer) {
        int length = buffer.getInt();
        bytes = new byte[length];
        buffer.get(bytes);
    }

    @Override
    public void handle(INetworked game) {
        if (game instanceof NetworkedGame) {
            ((NetworkedGame)game).handledTestMessages.add(this);
        }
    }
}

package sekelsta.game.network;

import java.nio.ByteBuffer;

import sekelsta.engine.network.MessageContext;
import sekelsta.engine.network.MessageParsingException;

public class GameContext implements MessageContext {
    public long tick;

    public GameContext(long tick) {
        this.tick = tick;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putLong(tick);
    }

    @Override
    public GameContext read(ByteBuffer buffer) {
        if (buffer.remaining() < Long.BYTES) {
            throw new MessageParsingException();
        }
        long tickIn = buffer.getLong();
        return new GameContext(tickIn);
    }

    @Override
    public int sizeInBytes() {
        return Long.BYTES;
    }
}

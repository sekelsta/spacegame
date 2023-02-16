package sekelsta.engine.network;

import java.nio.ByteBuffer;

public class MessageContext {
    public long tick;

    public MessageContext() {
        this(-1);
    }

    public MessageContext(long tick) {
        this.tick = tick;
    }

    public void write(ByteBuffer buffer) {
        buffer.putLong(tick);
    }

    // Not static, to allow overrides
    public MessageContext read(ByteBuffer buffer) {
        if (buffer.remaining() < Long.BYTES) {
            throw new MessageParsingException();
        }
        long tickIn = buffer.getLong();
        return new MessageContext(tickIn);
    }

    public int sizeInBytes() {
        return Long.BYTES;
    }
}

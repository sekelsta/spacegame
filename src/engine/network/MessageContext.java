package sekelsta.engine.network;

import java.nio.ByteBuffer;

public interface MessageContext {
    public void write(ByteBuffer buffer);
    public MessageContext read(ByteBuffer buffer);
    public int sizeInBytes();
}

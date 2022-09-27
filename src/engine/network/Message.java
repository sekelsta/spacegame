package sekelsta.engine.network;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public abstract class Message {
    public Connection sender;
    public MessageContext context;

    public boolean requiresConfirmedAddress() {
        return true;
    }

    public abstract NetworkDirection getDirection();

    public boolean reliable() {
        return true;
    }

    public abstract void encode(ByteVector buffer);

    public abstract void decode(ByteBuffer buffer);

    public abstract void handle(INetworked game);

    // The logic in readString and writeString works for collections in general, but String does not implement that
    protected static void writeString(ByteVector buffer, String s) {
        buffer.putInt(s.length());
        for (char c : s.toCharArray()) {
            buffer.putChar(c);
        }
    }

    protected static String readString(ByteBuffer buffer) {
        int length = buffer.getInt();
        if (length < 0) {
            throw new MessageParsingException("Can't read string of negative length");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            sb.append(buffer.getChar());
        }
        return sb.toString();
    }
}

package sekelsta.engine.network;

import java.nio.ByteBuffer;
import java.util.*;

public class PacketHeader {
    public final int packetID;
    private boolean reliable = false;
    public final Set<Integer> packetIDsToAck = new HashSet<>();
    public final MessageContext context;

    public PacketHeader(int packetID) {
        this.packetID = packetID;
        this.context = null;
    }

    public PacketHeader(ByteBuffer buffer) {
        if (buffer.remaining() < Integer.BYTES + 1 + Integer.BYTES) {
            throw new MessageParsingException();
        }
        this.packetID = buffer.getInt();
        this.reliable = buffer.get() != 0;
        int numAcks = buffer.getInt();
        if (buffer.remaining() < numAcks * Integer.BYTES) {
            throw new MessageParsingException();
        }
        for (int i = 0; i < numAcks; ++i) {
            packetIDsToAck.add(buffer.getInt());
        }
        this.context = NetworkManager.context.read(buffer);
    }

    public void write(ByteBuffer buffer) {
        buffer.putInt(packetID);
        buffer.put((byte)(reliable? 1 : 0));
        buffer.putInt(packetIDsToAck.size());
        for (int id : packetIDsToAck) {
            buffer.putInt(id);
        }
        NetworkManager.context.write(buffer);
    }

    public int sizeInBytes() {
        // packetID, reliable, packetIDsToAck.size(), each item in packetIDsToAck
        int size = Integer.BYTES + 1 + Integer.BYTES + packetIDsToAck.size() * Integer.BYTES;
        if (NetworkManager.context != null) {
            size += NetworkManager.context.sizeInBytes();
        }
        return size;
    }

    public void updateReliable(Message message) {
        this.reliable = this.reliable || message.reliable();
    }

    public boolean isReliable() {
        return reliable;
    }
}

package sekelsta.engine.network;

import java.nio.ByteBuffer;
import java.util.*;

public class PacketHeader {
    public final int packetID;
    private boolean reliable = false;
    public final List<Integer> packetIDsToAck = new ArrayList<>();
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

    public List<Integer> write(ByteBuffer buffer, int maxLength) {
        buffer.putInt(packetID);
        buffer.put((byte)(reliable? 1 : 0));

        int maxPacketIDs = packetIDsToAck.size();
        int size = sizeInBytes();
        if (size > maxLength) {
            int baseSize = size - packetIDsToAck.size() * Integer.BYTES;
            int ackSpace = maxLength - baseSize;
            maxPacketIDs = ackSpace / Integer.BYTES;
        }

        buffer.putInt(packetIDsToAck.size());
        for (int i = 0; i < maxPacketIDs; ++i) {
            buffer.putInt(packetIDsToAck.get(i));
        }
        NetworkManager.context.write(buffer);

        if (maxPacketIDs < packetIDsToAck.size()) {
            return packetIDsToAck.subList(maxPacketIDs, packetIDsToAck.size());
        }
        return null;
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

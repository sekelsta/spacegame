// Based loosely on https://gist.github.com/DudeMartin/5273469
// Per author's comment: 
// "Anyone that wants to use this code has my permission to use it anywhere and in any way that they want."
// The same applies to my changes to it, I release those under CC0 with the usual disclaimer of no warranty.
package sekelsta.engine.network;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteVector implements Comparable<ByteBuffer> {

    private ByteBuffer byteBuffer;
    private float expandFactor = 2;

    public ByteVector(int initialCapacity) {
        this.byteBuffer = ByteBuffer.allocate(initialCapacity);
    }

    // Not implemented: slice, duplicate

    public ByteBuffer asReadOnlyBuffer() {
        return byteBuffer.asReadOnlyBuffer();
    }

    public byte get() {
        return byteBuffer.get();
    }

    public ByteVector put(byte b) {
        ensureSpace(1);
        byteBuffer.put(b);
        return this;
    }

    public byte get(int index) {
        return byteBuffer.get(index);
    }

    public ByteVector put(int index, byte b) {
        ensureSpace(index, 1);
        byteBuffer.put(index, b);
        return this;
    }

    public ByteVector get(byte[] dst, int offset, int length) {
        byteBuffer.get(dst, offset, length);
        return this;
    }

    public ByteVector get(byte[] dst) {
        byteBuffer.get(dst);
        return this;
    }

    public ByteVector get(int index, byte[] dst, int offset, int length) {
        byteBuffer.get(index, dst, offset, length);
        return this;
    }

    public ByteVector get(int index, byte[] dst) {
        byteBuffer.get(index, dst);
        return this;
    }

    public ByteVector put(ByteBuffer src) {
        ensureSpace(src.remaining());
        byteBuffer.put(src);
        return this;
    }

    public ByteVector put(int index, ByteBuffer src, int offset, int length) {
        ensureSpace(index, length);
        byteBuffer.put(index, src, offset, length);
        return this;
    }

    public ByteVector put(byte[] src, int offset, int length) {
        ensureSpace(length);
        byteBuffer.put(src, offset, length);
        return this;
    }

    public ByteVector put(byte[] src) {
        ensureSpace(src.length);
        byteBuffer.put(src);
        return this;
    }

    public ByteVector put(int index, byte[] src, int offset, int length) {
        ensureSpace(index, length);
        byteBuffer.put(index, src, offset, length);
        return this;
    }

    public ByteVector put(int index, byte[] src) {
        ensureSpace(index, src.length);
        byteBuffer.put(index, src);
        return this;
    }

    public int capacity() {
        return byteBuffer.capacity();
    }

    public int position() {
        return byteBuffer.position();
    }

    public int limit() {
        return byteBuffer.limit();
    }

    public int remaining() {
        return byteBuffer.remaining();
    }

    public boolean hasRemaining() {
        return byteBuffer.hasRemaining();
    }

    public boolean isReadOnly() {
        return byteBuffer.isReadOnly();
    }

    public boolean hasArray() {
        return byteBuffer.hasArray();
    }

    public byte[] array() {
        return byteBuffer.array();
    }

    public int arrayOffset() {
        return byteBuffer.arrayOffset();
    }

    public ByteVector position(int newPosition) {
        byteBuffer.position(newPosition);
        return this;
    }

    public ByteVector limit(int newLimit) {
        byteBuffer.limit(newLimit);
        return this;
    }

    // Not implemented: mark, reset

    public ByteVector clear() {
        byteBuffer.clear();
        return this;
    }

    public ByteVector flip() {
        byteBuffer.flip();
        return this;
    }

    public ByteVector rewind() {
        byteBuffer.rewind();
        return this;
    }

    public ByteVector compact() {
        byteBuffer.compact();
        return this;
    }

    public boolean isDirect() {
        return byteBuffer.isDirect();
    }

    @Override
    public String toString() {
        return byteBuffer.toString();
    }

    @Override
    public int hashCode() {
        return byteBuffer.hashCode();
    }

    @Override
    public boolean equals(Object ob) {
        if (ob == this) {
            return true;
        }
        if (ob instanceof ByteVector) {
            return byteBuffer.equals(((ByteVector)ob).byteBuffer);
        }
        return false;
    }

    public int compareTo(ByteBuffer that) {
        return byteBuffer.compareTo(that);
    }

    public int mismatch(ByteBuffer that) {
        return byteBuffer.mismatch(that);
    }

    public ByteOrder order() {
        return byteBuffer.order();
    }

    public ByteVector order(ByteOrder bo) {
        byteBuffer.order(bo);
        return this;
    }

    // Not implemented: alignmentOffset, alignedSlice

    public char getChar() {
        return byteBuffer.getChar();
    }

    public ByteVector putChar(char value) {
        ensureSpace(2);
        byteBuffer.putChar(value);
        return this;
    }

    public char getChar(int index) {
        return byteBuffer.getChar(index);
    }

    public ByteVector putChar(int index, char value) {
        ensureSpace(index, 2);
        byteBuffer.putChar(index, value);
        return this;
    }

    // Not implemented: asCharBuffer

    public short getShort() {
        return byteBuffer.getShort();
    }

    public ByteVector putShort(short value) {
        ensureSpace(2);
        byteBuffer.putShort(value);
        return this;
    }

    public short getShort(int index) {
        return byteBuffer.getShort(index);
    }

    public ByteVector putShort(int index, short value) {
        ensureSpace(index, 2);
        byteBuffer.putShort(index, value);
        return this;
    }

    // Not implemented: asShortBuffer

    public int getInt() {
        return byteBuffer.getInt();
    }

    public ByteVector putInt(int value) {
        ensureSpace(4);
        byteBuffer.putInt(value);
        return this;
    }

    public int getInt(int index) {
        return byteBuffer.getInt(index);
    }

    public ByteVector putInt(int index, int value) {
        ensureSpace(index, 4);
        byteBuffer.putInt(index, value);
        return this;
    }

    // Not implemented: asIntBuffer

    public long getLong() {
        return byteBuffer.getLong();
    }

    public ByteVector putLong(long value) {
        ensureSpace(8);
        byteBuffer.putLong(value);
        return this;
    }

    public long getLong(int index) {
        return byteBuffer.getLong(index);
    }

    public ByteVector putLong(int index, long value) {
        ensureSpace(index, 8);
        byteBuffer.putLong(index, value);
        return this;
    }

    // Not implemented: asLongBuffer

    public float getFloat() {
        return byteBuffer.getFloat();
    }

    public ByteVector putFloat(float value) {
        ensureSpace(4);
        byteBuffer.putFloat(value);
        return this;
    }

    public float getFloat(int index) {
        return byteBuffer.getFloat(index);
    }

    public ByteVector putFloat(int index, float value) {
        ensureSpace(index, 4);
        byteBuffer.putFloat(index, value);
        return this;
    }

    // Not implemented: asFloatBuffer

    public double getDouble() {
        return byteBuffer.getDouble();
    }

    public double getDouble(int index) {
        return byteBuffer.getDouble(index);
    }

    public ByteVector putDouble(double value) {
        ensureSpace(8);
        byteBuffer.putDouble(value);
        return this;
    }

    public ByteVector putDouble(int index, double value) {
        ensureSpace(index, 8);
        byteBuffer.putDouble(index, value);
        return this;
    }

    // Not implemented: asDoubleBuffer

    private void ensureSpace(int index, int length) {
        if (limit() < capacity()) {
            return;
        }
        if (index + length <= limit()) {
            return;
        }
        expandCapacity(index + length);
    }

    private void ensureSpace(int needed) {
        if (limit() < capacity()) {
            return;
        }
        if (needed <= remaining()) {
            return;
        }
        expandCapacity(position() + needed);
    }

    private void expandCapacity(int needed) {
        int newCapacity = (int) (byteBuffer.capacity() * expandFactor);
        while (newCapacity < needed) {
            newCapacity *= expandFactor;
        }

        int position = byteBuffer.position();
        ByteBuffer expanded = ByteBuffer.allocate(newCapacity);
        expanded.order(byteBuffer.order());
        byteBuffer.flip();
        expanded.put(byteBuffer);
        expanded.position(position);
        byteBuffer = expanded;
    }
}

package sekelsta.test.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sekelsta.engine.network.ByteVector;

class TestByteVector {
    @Test
    void expandCopy() {
        ByteVector vector = new ByteVector(3);
        vector.putShort((short)12345);
        vector.putInt(54321);
        vector.flip();
        assertEquals((short)12345, vector.getShort());
        assertEquals(54321, vector.getInt());
        assertEquals(6, vector.capacity());
    }

    @Test
    void insertKeepSize() {
        ByteVector vector = new ByteVector(4);
        vector.putInt(54321);
        vector.putInt(0, 12345);
        assertEquals(4, vector.capacity());
    }

    @Test
    void insertExpand() {
        ByteVector vector = new ByteVector(4);
        vector.putInt(54321);
        vector.putInt(1, 12345);
        assertEquals(8, vector.capacity());
    }
}

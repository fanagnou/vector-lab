package io.github.fanagnou.vectorlab.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class VectorStoreTest {

    @Test
    void addsGrowsAndReturnsOrdinals() {
        VectorStore store = new VectorStore(2, 1);
        for (int i = 0; i < 100; i++) {
            assertEquals(i, store.add(new float[] {i, -i}));
        }
        assertEquals(100, store.size());
        assertArrayEquals(new float[] {42, -42}, store.copyOf(42));
        assertEquals(84, store.offset(42));
        assertEquals(42f, store.data()[store.offset(42)]);
    }

    @Test
    void addCopiesTheCallersArray() {
        VectorStore store = new VectorStore(2, 4);
        float[] v = {1, 2};
        store.add(v);
        v[0] = 99;
        assertArrayEquals(new float[] {1, 2}, store.copyOf(0));
    }

    @Test
    void wrapUsesTheFlatArray() {
        VectorStore store = VectorStore.wrap(3, new float[] {1, 2, 3, 4, 5, 6});
        assertEquals(2, store.size());
        assertArrayEquals(new float[] {4, 5, 6}, store.copyOf(1));
    }

    @Test
    void rejectsBadInput() {
        VectorStore store = new VectorStore(3, 0);
        assertThrows(IllegalArgumentException.class, () -> store.add(new float[2]));
        assertThrows(IndexOutOfBoundsException.class, () -> store.offset(0));
        assertThrows(IllegalArgumentException.class, () -> VectorStore.wrap(3, new float[4]));
        assertThrows(IllegalArgumentException.class, () -> new VectorStore(0, 1));
    }
}

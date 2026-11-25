package io.github.fanagnou.vectorlab.data;

import java.util.Arrays;
import java.util.Objects;

/**
 * Vectors of one dimension stored back to back in a single {@code float[]} (row-major): vector
 * {@code i} occupies {@code data[i*dim .. (i+1)*dim)}.
 *
 * <p>Why not {@code float[][]}: one object instead of {@code n}, no per-row headers or pointers,
 * and a scan reads memory sequentially (cache- and prefetch-friendly). Lucene stores vectors the
 * same way, in one file per segment.
 */
public final class VectorStore {

    private static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    private final int dim;
    private float[] data;
    private int size;

    public VectorStore(int dim, int initialCapacity) {
        if (dim <= 0) {
            throw new IllegalArgumentException("dim must be > 0, got " + dim);
        }
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity must be >= 0, got " + initialCapacity);
        }
        this.dim = dim;
        this.data = new float[checkedLength((long) initialCapacity * dim)];
    }

    /** Wraps an existing flat array (not copied) holding {@code flat.length / dim} vectors. */
    public static VectorStore wrap(int dim, float[] flat) {
        if (dim <= 0 || flat.length % dim != 0) {
            throw new IllegalArgumentException("flat.length " + flat.length + " is not a multiple of dim " + dim);
        }
        VectorStore store = new VectorStore(dim, 0);
        store.data = flat;
        store.size = flat.length / dim;
        return store;
    }

    /** Appends a copy of {@code vector}; returns its ordinal. */
    public int add(float[] vector) {
        if (vector.length != dim) {
            throw new IllegalArgumentException("expected dim " + dim + ", got " + vector.length);
        }
        ensureCapacity(size + 1);
        System.arraycopy(vector, 0, data, size * dim, dim);
        return size++;
    }

    public int dim() {
        return dim;
    }

    public int size() {
        return size;
    }

    /** Start index of vector {@code ordinal} inside {@link #data()}. */
    public int offset(int ordinal) {
        Objects.checkIndex(ordinal, size);
        return ordinal * dim;
    }

    /**
     * The backing array, for scoring in place: valid entries are {@code [0, size()*dim())}. Don't
     * keep the reference across {@link #add}: growing replaces the array.
     */
    public float[] data() {
        return data;
    }

    /** A fresh copy of vector {@code ordinal}. */
    public float[] copyOf(int ordinal) {
        int from = offset(ordinal);
        return Arrays.copyOfRange(data, from, from + dim);
    }

    /** Approximate heap bytes: array header + capacity (not just size) + this object. */
    public long ramBytesUsed() {
        return 16L + 16L + (long) data.length * Float.BYTES;
    }

    private void ensureCapacity(int vectors) {
        long needed = (long) vectors * dim;
        if (needed <= data.length) {
            return;
        }
        checkedLength(needed); // fail fast if even the exact size can't fit in one array
        long grown = Math.max(needed, data.length + (data.length >> 1) + dim); // ~1.5x, like ArrayList
        data = Arrays.copyOf(data, (int) Math.min(grown, MAX_ARRAY_LENGTH));
    }

    private static int checkedLength(long length) {
        if (length > MAX_ARRAY_LENGTH) {
            throw new IllegalStateException("too many floats for one array: " + length);
        }
        return (int) length;
    }
}

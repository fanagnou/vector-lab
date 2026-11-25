package io.github.fanagnou.vectorlab;

/**
 * A k-nearest-neighbour index over vectors of one fixed dimension.
 *
 * <p>Vectors are identified by their <em>ordinal</em>: 0, 1, 2, ... in insertion order (Lucene
 * uses the same idea inside a segment). Implementations: exact ({@code flat}), HNSW, quantized
 * HNSW, IVF. They are not thread-safe unless they say so.
 */
public interface VectorIndex {

    /**
     * Adds a vector and returns its ordinal.
     *
     * @throws IllegalArgumentException if {@code vector.length} differs from the index dimension
     */
    int add(float[] vector);

    /**
     * Returns the ordinals of the {@code k} vectors most similar to {@code query}, most similar
     * first. Exact indexes return the true top-k; approximate ones may miss some. Returns fewer
     * than {@code k} ordinals only if the index holds fewer than {@code k} vectors.
     *
     * @throws IllegalArgumentException if {@code k <= 0} or the query has the wrong dimension
     */
    int[] search(float[] query, int k);

    /** Number of vectors in the index. */
    int size();

    /** Approximate heap bytes used by the index, stored vectors included. */
    long ramBytesUsed();
}

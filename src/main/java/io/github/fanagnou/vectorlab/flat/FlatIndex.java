package io.github.fanagnou.vectorlab.flat;

import io.github.fanagnou.vectorlab.Similarity;
import io.github.fanagnou.vectorlab.VectorIndex;
import io.github.fanagnou.vectorlab.data.VectorStore;

/**
 * Exact kNN: scores the query against every stored vector. O(n·dim) per query, recall 1.0.
 * The baseline every approximate index is measured against.
 */
public final class FlatIndex implements VectorIndex {

    private final Similarity similarity;
    private final VectorStore vectors;

    public FlatIndex(int dim, Similarity similarity) {
        this.similarity = similarity;
        this.vectors = new VectorStore(dim, 1024);
    }

    @Override
    public int add(float[] vector) {
        return vectors.add(vector);
    }

    @Override
    public int[] search(float[] query, int k) {
        // TODO(Foivos) slice 0: exact top-k, most similar first. Spec: FlatIndexTest.
        // Score in place with similarity.score(query, 0, vectors.data(), vectors.offset(i), dim).
        throw new UnsupportedOperationException("slice 0: FlatIndex.search is not implemented yet");
    }

    @Override
    public int size() {
        return vectors.size();
    }

    @Override
    public long ramBytesUsed() {
        return 16L + vectors.ramBytesUsed();
    }
}

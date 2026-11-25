package io.github.fanagnou.vectorlab;

import io.github.fanagnou.vectorlab.data.VectorStore;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Test oracle: the exact top-k by scoring everything and sorting everything. Deliberately the
 * simplest correct code (O(n log n) per query), so it is easy to trust and independent of the
 * implementations under test. Ties are broken by the smaller ordinal.
 */
public final class BruteForce {

    private BruteForce() {}

    public static int[] topK(VectorStore vectors, Similarity similarity, float[] query, int k) {
        int n = vectors.size();
        float[] scores = new float[n];
        for (int i = 0; i < n; i++) {
            scores[i] = similarity.score(query, 0, vectors.data(), vectors.offset(i), vectors.dim());
        }
        Integer[] ordinals = new Integer[n];
        for (int i = 0; i < n; i++) {
            ordinals[i] = i;
        }
        Arrays.sort(ordinals, Comparator.<Integer>comparingDouble(i -> -scores[i]).thenComparingInt(i -> i));
        int count = Math.min(k, n);
        int[] top = new int[count];
        for (int i = 0; i < count; i++) {
            top[i] = ordinals[i];
        }
        return top;
    }
}

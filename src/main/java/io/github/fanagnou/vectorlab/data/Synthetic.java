package io.github.fanagnou.vectorlab.data;

import java.util.SplittableRandom;

/**
 * Seeded random vectors for tests: same seed, same vectors, on every machine. Clustered data is
 * closer to real embeddings than uniform noise (and harder for graph indexes).
 */
public final class Synthetic {

    private Synthetic() {}

    /**
     * {@code n} vectors around {@code clusters} random centres in {@code [-1, 1]^dim}, each
     * coordinate offset by Gaussian noise with standard deviation {@code spread}.
     */
    public static VectorStore gaussianClusters(int n, int dim, int clusters, float spread, long seed) {
        if (n < 0 || clusters <= 0) {
            throw new IllegalArgumentException("need n >= 0 and clusters > 0");
        }
        SplittableRandom random = new SplittableRandom(seed);
        float[][] centres = new float[clusters][dim];
        for (float[] centre : centres) {
            for (int d = 0; d < dim; d++) {
                centre[d] = (float) random.nextDouble(-1.0, 1.0);
            }
        }
        VectorStore store = new VectorStore(dim, n);
        float[] v = new float[dim];
        for (int i = 0; i < n; i++) {
            float[] centre = centres[random.nextInt(clusters)];
            for (int d = 0; d < dim; d++) {
                v[d] = centre[d] + spread * (float) random.nextGaussian();
            }
            store.add(v);
        }
        return store;
    }
}

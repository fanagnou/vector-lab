package io.github.fanagnou.vectorlab;

import io.github.fanagnou.vectorlab.simd.ScalarOps;

/**
 * How two vectors are compared. {@link #score} is always "higher = more similar" and never
 * negative, following Lucene's conventions, so every index can rank with one rule.
 */
public enum Similarity {

    /** L2 distance turned into a score: {@code 1 / (1 + squaredDistance)}. SIFT uses this. */
    EUCLIDEAN {
        @Override
        public float score(float[] a, int aOffset, float[] b, int bOffset, int dim) {
            return 1f / (1f + ScalarOps.squareDistance(a, aOffset, b, bOffset, dim));
        }
    },

    /** For unit-length vectors: {@code (1 + dot) / 2}, which maps [-1, 1] to [0, 1]. */
    DOT_PRODUCT {
        @Override
        public float score(float[] a, int aOffset, float[] b, int bOffset, int dim) {
            return Math.max((1f + ScalarOps.dotProduct(a, aOffset, b, bOffset, dim)) / 2f, 0f);
        }
    },

    /** Angle only, length ignored: {@code (1 + cosine) / 2}. */
    COSINE {
        @Override
        public float score(float[] a, int aOffset, float[] b, int bOffset, int dim) {
            return Math.max((1f + ScalarOps.cosine(a, aOffset, b, bOffset, dim)) / 2f, 0f);
        }
    };

    /**
     * Scores {@code a[aOffset .. aOffset+dim)} against {@code b[bOffset .. bOffset+dim)}. Offsets
     * let callers score vectors in place inside a flat array, without copying them out.
     */
    public abstract float score(float[] a, int aOffset, float[] b, int bOffset, int dim);

    /** Scores two whole arrays of equal length. */
    public float score(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("dimension mismatch: " + a.length + " vs " + b.length);
        }
        return score(a, 0, b, 0, a.length);
    }
}

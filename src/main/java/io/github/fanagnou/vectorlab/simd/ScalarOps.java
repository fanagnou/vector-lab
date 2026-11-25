package io.github.fanagnou.vectorlab.simd;

/**
 * Plain-loop vector maths: the baseline for slice 2, where a Panama Vector API version
 * ({@code PanamaOps}, written by Foivos) is benchmarked against it with JMH.
 */
public final class ScalarOps {

    private ScalarOps() {}

    public static float dotProduct(float[] a, int aOffset, float[] b, int bOffset, int dim) {
        float sum = 0f;
        for (int i = 0; i < dim; i++) {
            sum += a[aOffset + i] * b[bOffset + i];
        }
        return sum;
    }

    public static float squareDistance(float[] a, int aOffset, float[] b, int bOffset, int dim) {
        float sum = 0f;
        for (int i = 0; i < dim; i++) {
            float diff = a[aOffset + i] - b[bOffset + i];
            sum += diff * diff;
        }
        return sum;
    }

    /** Cosine of the angle between the vectors; 0 if either vector is all zeros. */
    public static float cosine(float[] a, int aOffset, float[] b, int bOffset, int dim) {
        float dot = 0f;
        float normA = 0f;
        float normB = 0f;
        for (int i = 0; i < dim; i++) {
            float x = a[aOffset + i];
            float y = b[bOffset + i];
            dot += x * y;
            normA += x * x;
            normB += y * y;
        }
        if (normA == 0f || normB == 0f) {
            return 0f;
        }
        return (float) (dot / Math.sqrt((double) normA * normB));
    }
}

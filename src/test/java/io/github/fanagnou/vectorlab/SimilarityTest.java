package io.github.fanagnou.vectorlab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimilarityTest {

    private static final float EPS = 1e-6f;

    @Test
    void euclideanIsOneForIdenticalVectorsAndDropsWithDistance() {
        float[] a = {1, 2, 3};
        assertEquals(1f, Similarity.EUCLIDEAN.score(a, a), EPS);
        // squared distance = 1 + 1 + 1 = 3  ->  1 / (1 + 3)
        assertEquals(0.25f, Similarity.EUCLIDEAN.score(a, new float[] {2, 3, 4}), EPS);
    }

    @Test
    void dotProductMapsMinusOneToOneOntoZeroToOne() {
        float[] x = {1, 0};
        assertEquals(1f, Similarity.DOT_PRODUCT.score(x, x), EPS);
        assertEquals(0.5f, Similarity.DOT_PRODUCT.score(x, new float[] {0, 1}), EPS);
        assertEquals(0f, Similarity.DOT_PRODUCT.score(x, new float[] {-1, 0}), EPS);
    }

    @Test
    void cosineIgnoresLength() {
        assertEquals(1f, Similarity.COSINE.score(new float[] {1, 1}, new float[] {5, 5}), EPS);
        assertEquals(0.5f, Similarity.COSINE.score(new float[] {0, 0}, new float[] {1, 1}), EPS);
    }

    @Test
    void offsetsScoreVectorsInsideAFlatArray() {
        float[] flat = {9, 9, 1, 2, 3, 9};
        float[] q = {1, 2, 3};
        assertEquals(1f, Similarity.EUCLIDEAN.score(q, 0, flat, 2, 3), EPS);
    }

    @Test
    void scoresAreSymmetricAndNeverNegative() {
        float[] a = {0.3f, -1.2f, 4f};
        float[] b = {-2f, 0.5f, 1f};
        for (Similarity s : Similarity.values()) {
            assertEquals(s.score(a, b), s.score(b, a), EPS, s.name());
            assertTrue(s.score(a, b) >= 0f, s.name());
        }
    }

    @Test
    void rejectsDimensionMismatch() {
        assertThrows(IllegalArgumentException.class,
                () -> Similarity.EUCLIDEAN.score(new float[2], new float[3]));
    }
}

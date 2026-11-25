package io.github.fanagnou.vectorlab.flat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.fanagnou.vectorlab.BruteForce;
import io.github.fanagnou.vectorlab.Similarity;
import io.github.fanagnou.vectorlab.TestSeeds;
import io.github.fanagnou.vectorlab.data.Synthetic;
import io.github.fanagnou.vectorlab.data.VectorStore;
import java.util.Arrays;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** The spec for slice 0. Delete the @Disabled line once FlatIndex.search exists. */
@Disabled("slice 0: implement FlatIndex.search, then delete this line")
class FlatIndexTest {

    private static final int DIM = 16;

    @ParameterizedTest
    @EnumSource(Similarity.class)
    void matchesTheBruteForceOracle(Similarity similarity) {
        long seed = TestSeeds.seed();
        VectorStore data = Synthetic.gaussianClusters(2_000, DIM, 20, 0.2f, seed);
        VectorStore queries = Synthetic.gaussianClusters(50, DIM, 20, 0.2f, seed + 1);
        FlatIndex index = build(data, similarity);

        for (int k : new int[] {1, 10, 100}) {
            for (int q = 0; q < queries.size(); q++) {
                float[] query = queries.copyOf(q);
                int[] expected = BruteForce.topK(data, similarity, query, k);
                assertArrayEquals(expected, index.search(query, k),
                        similarity + " k=" + k + " query=" + q + " " + TestSeeds.msg(seed));
            }
        }
    }

    @Test
    void returnsEverythingWhenKExceedsSize() {
        long seed = TestSeeds.seed();
        VectorStore data = Synthetic.gaussianClusters(5, DIM, 2, 0.2f, seed);
        int[] result = build(data, Similarity.EUCLIDEAN).search(data.copyOf(0), 50);
        assertEquals(5, result.length, TestSeeds.msg(seed));
        assertEquals(0, result[0], "a stored vector is its own nearest neighbour. " + TestSeeds.msg(seed));
        int[] sorted = result.clone();
        Arrays.sort(sorted);
        assertArrayEquals(new int[] {0, 1, 2, 3, 4}, sorted, "no duplicates, nothing missing");
    }

    @Test
    void emptyIndexReturnsNothing() {
        assertEquals(0, new FlatIndex(DIM, Similarity.EUCLIDEAN).search(new float[DIM], 10).length);
    }

    @Test
    void doesNotModifyTheQuery() {
        VectorStore data = Synthetic.gaussianClusters(100, DIM, 4, 0.2f, 42);
        float[] query = data.copyOf(7);
        float[] before = query.clone();
        build(data, Similarity.COSINE).search(query, 10);
        assertArrayEquals(before, query);
    }

    @Test
    void rejectsBadArguments() {
        FlatIndex index = new FlatIndex(DIM, Similarity.EUCLIDEAN);
        index.add(new float[DIM]);
        assertThrows(IllegalArgumentException.class, () -> index.search(new float[DIM], 0));
        assertThrows(IllegalArgumentException.class, () -> index.search(new float[DIM + 1], 1));
        assertThrows(IllegalArgumentException.class, () -> index.add(new float[DIM - 1]));
    }

    private static FlatIndex build(VectorStore data, Similarity similarity) {
        FlatIndex index = new FlatIndex(data.dim(), similarity);
        for (int i = 0; i < data.size(); i++) {
            assertEquals(i, index.add(data.copyOf(i)));
        }
        return index;
    }
}

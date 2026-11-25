package io.github.fanagnou.vectorlab.bench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RecallTest {

    @Test
    void countsOverlapInTopKIgnoringOrder() {
        int[][] truth = {{1, 2, 3, 4}, {5, 6, 7, 8}};
        int[][] results = {{3, 2, 1}, {5, 9, 9}};
        // query 0: 3/3, query 1: 1/3 -> mean 2/3
        assertEquals(2.0 / 3.0, Recall.atK(results, truth, 3), 1e-9);
    }

    @Test
    void shortResultListsLoseRecall() {
        assertEquals(0.5, Recall.atK(new int[][] {{1}}, new int[][] {{1, 2}}, 2), 1e-9);
    }

    @Test
    void validatesInput() {
        assertThrows(IllegalArgumentException.class, () -> Recall.atK(new int[1][], new int[2][], 1));
        assertThrows(IllegalArgumentException.class, () -> Recall.atK(new int[][] {{1}}, new int[][] {{1}}, 2));
        assertThrows(IllegalArgumentException.class, () -> Recall.atK(new int[][] {{1}}, new int[][] {{1}}, 0));
    }
}

package io.github.fanagnou.vectorlab.bench;

/** Recall@k: the share of the true top-k neighbours that an index returned in its top k. */
public final class Recall {

    private Recall() {}

    /**
     * Mean recall@k over all queries. {@code results[q]} and {@code truth[q]} are ordinals, most
     * similar first; only their first {@code k} entries count. Order inside the top k is ignored.
     */
    public static double atK(int[][] results, int[][] truth, int k) {
        if (results.length != truth.length) {
            throw new IllegalArgumentException(results.length + " result rows vs " + truth.length + " truth rows");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("k must be > 0");
        }
        if (results.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (int q = 0; q < results.length; q++) {
            int[] want = truth[q];
            if (want.length < k) {
                throw new IllegalArgumentException("ground truth row " + q + " has only " + want.length + " entries");
            }
            int found = 0;
            int returned = Math.min(k, results[q].length);
            for (int i = 0; i < returned; i++) {
                for (int j = 0; j < k; j++) {
                    if (results[q][i] == want[j]) {
                        found++;
                        break;
                    }
                }
            }
            sum += (double) found / k;
        }
        return sum / results.length;
    }
}

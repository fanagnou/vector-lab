package io.github.fanagnou.vectorlab.data;

import io.github.fanagnou.vectorlab.Similarity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A benchmark dataset: base vectors to index, query vectors, and the exact top neighbours of each
 * query (ground truth, as base ordinals, nearest first).
 */
public record Dataset(String name, VectorStore base, VectorStore queries, int[][] groundTruth, Similarity similarity) {

    public Dataset {
        if (base.dim() != queries.dim()) {
            throw new IllegalArgumentException("base dim " + base.dim() + " != query dim " + queries.dim());
        }
        if (groundTruth.length != queries.size()) {
            throw new IllegalArgumentException(groundTruth.length + " ground-truth rows for " + queries.size() + " queries");
        }
    }

    /**
     * Loads a TEXMEX SIFT folder: {@code <dir>/<name>_base.fvecs}, {@code _query.fvecs} and
     * {@code _groundtruth.ivecs}, where {@code <name>} is the folder name ({@code siftsmall},
     * {@code sift}). SIFT is compared with Euclidean distance.
     */
    public static Dataset sift(Path dir) throws IOException {
        String name = dir.getFileName().toString();
        Path base = dir.resolve(name + "_base.fvecs");
        if (!Files.exists(base)) {
            throw new IOException("missing " + base + " (run scripts/get-sift.sh first)");
        }
        return new Dataset(
                name,
                Fvecs.readFvecs(base),
                Fvecs.readFvecs(dir.resolve(name + "_query.fvecs")),
                Fvecs.readIvecs(dir.resolve(name + "_groundtruth.ivecs")),
                Similarity.EUCLIDEAN);
    }

    public int dim() {
        return base.dim();
    }
}

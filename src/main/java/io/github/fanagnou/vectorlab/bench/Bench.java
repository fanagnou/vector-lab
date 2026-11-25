package io.github.fanagnou.vectorlab.bench;

import io.github.fanagnou.vectorlab.VectorIndex;
import io.github.fanagnou.vectorlab.data.Dataset;
import io.github.fanagnou.vectorlab.data.VectorStore;
import io.github.fanagnou.vectorlab.flat.FlatIndex;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Index-level benchmark: build time, query throughput and latency, recall@k and memory, printed as
 * one Markdown table row for the README.
 *
 * <pre>
 * ./gradlew run --args="--data data/siftsmall --index flat --k 10"
 * </pre>
 *
 * <p>Single-threaded, wall-clock timing after one warm-up pass: good for comparing indexes on the
 * same machine, not a substitute for JMH on tight loops.
 */
public final class Bench {

    private Bench() {}

    public static void main(String[] args) throws IOException {
        Map<String, String> opts = parse(args);
        Path dataDir = Path.of(opts.getOrDefault("data", "data/siftsmall"));
        String indexName = opts.getOrDefault("index", "flat");
        int k = Integer.parseInt(opts.getOrDefault("k", "10"));

        Dataset ds = Dataset.sift(dataDir);
        System.out.printf(Locale.ROOT, "dataset %s: %d base, %d queries, dim %d, %s%n",
                ds.name(), ds.base().size(), ds.queries().size(), ds.dim(), ds.similarity());

        long t0 = System.nanoTime();
        VectorIndex index = create(indexName, ds, opts);
        VectorStore base = ds.base();
        for (int i = 0; i < base.size(); i++) {
            index.add(base.copyOf(i));
        }
        double buildSeconds = (System.nanoTime() - t0) / 1e9;

        VectorStore queries = ds.queries();
        int[][] results = new int[queries.size()][];
        for (int q = 0; q < queries.size(); q++) { // warm-up: let the JIT compile the hot paths
            index.search(queries.copyOf(q), k);
        }
        long[] latencies = new long[queries.size()];
        long searchStart = System.nanoTime();
        for (int q = 0; q < queries.size(); q++) {
            float[] query = queries.copyOf(q);
            long start = System.nanoTime();
            results[q] = index.search(query, k);
            latencies[q] = System.nanoTime() - start;
        }
        double searchSeconds = (System.nanoTime() - searchStart) / 1e9;

        Arrays.sort(latencies);
        double recall = Recall.atK(results, ds.groundTruth(), k);
        System.out.println("| dataset | index | params | build s | QPS | p50 ms | p99 ms | recall@" + k + " | RAM MB |");
        System.out.println("|---|---|---|---|---|---|---|---|---|");
        System.out.printf(Locale.ROOT, "| %s | %s | %s | %.2f | %.0f | %.3f | %.3f | %.3f | %.1f |%n",
                ds.name(), indexName, params(opts), buildSeconds, queries.size() / searchSeconds,
                percentileMillis(latencies, 0.50), percentileMillis(latencies, 0.99), recall,
                index.ramBytesUsed() / (1024.0 * 1024.0));
    }

    /** Add a case per index as the slices land, e.g. "hnsw" -> new HnswIndex(dim, sim, m, efConstruction, efSearch). */
    private static VectorIndex create(String name, Dataset ds, Map<String, String> opts) {
        return switch (name) {
            case "flat" -> new FlatIndex(ds.dim(), ds.similarity());
            default -> throw new IllegalArgumentException("unknown index '" + name + "' (known: flat)");
        };
    }

    private static Map<String, String> parse(String[] args) {
        Map<String, String> opts = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("--") || i + 1 >= args.length) {
                throw new IllegalArgumentException("expected --name value pairs, got: " + String.join(" ", args));
            }
            opts.put(args[i].substring(2), args[++i]);
        }
        return opts;
    }

    /** Everything except data/index/k, e.g. "m=16 ef=64". */
    private static String params(Map<String, String> opts) {
        StringBuilder sb = new StringBuilder();
        opts.forEach((key, value) -> {
            if (!key.equals("data") && !key.equals("index") && !key.equals("k")) {
                sb.append(sb.isEmpty() ? "" : " ").append(key).append('=').append(value);
            }
        });
        return sb.isEmpty() ? "-" : sb.toString();
    }

    private static double percentileMillis(long[] sortedNanos, double p) {
        if (sortedNanos.length == 0) {
            return 0.0;
        }
        int idx = (int) Math.ceil(p * sortedNanos.length) - 1;
        return sortedNanos[Math.max(0, Math.min(idx, sortedNanos.length - 1))] / 1e6;
    }
}

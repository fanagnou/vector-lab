package io.github.fanagnou.vectorlab;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Random-but-reproducible tests, the idea behind Lucene's and Elasticsearch's randomized testing:
 * each run uses a new seed (more inputs covered over time), every failure message prints it, and
 * {@code ./gradlew test -Dtests.seed=<seed>} replays exactly that run.
 */
public final class TestSeeds {

    private TestSeeds() {}

    public static long seed() {
        String fixed = System.getProperty("tests.seed");
        return fixed != null ? Long.parseLong(fixed) : ThreadLocalRandom.current().nextLong();
    }

    /** Use as the assertion message: {@code assertEquals(a, b, TestSeeds.msg(seed))}. */
    public static String msg(long seed) {
        return "reproduce with: ./gradlew test -Dtests.seed=" + seed;
    }
}

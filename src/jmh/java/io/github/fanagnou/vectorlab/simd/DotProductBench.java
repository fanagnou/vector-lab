package io.github.fanagnou.vectorlab.simd;

import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Slice 2 baseline: nanoseconds per dot product at common embedding sizes. Add a
 * {@code panama()} benchmark next to {@code scalar()} once PanamaOps exists.
 *
 * <pre>./gradlew jmh -PjmhArgs="DotProduct"</pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgsAppend = {"--add-modules", "jdk.incubator.vector"})
@State(Scope.Thread)
public class DotProductBench {

    @Param({"128", "384", "768"})
    int dim;

    float[] a;
    float[] b;

    @Setup
    public void setup() {
        SplittableRandom random = new SplittableRandom(42);
        a = new float[dim];
        b = new float[dim];
        for (int i = 0; i < dim; i++) {
            a[i] = (float) random.nextDouble(-1, 1);
            b[i] = (float) random.nextDouble(-1, 1);
        }
    }

    @Benchmark
    public float scalar() {
        return ScalarOps.dotProduct(a, 0, b, 0, dim); // returned, so the JIT can't drop it as dead code
    }
}

package io.github.fanagnou.vectorlab.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.fanagnou.vectorlab.TestSeeds;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FvecsTest {

    @TempDir
    Path tmp;

    @Test
    void fvecsRoundTrip() throws IOException {
        long seed = TestSeeds.seed();
        VectorStore original = Synthetic.gaussianClusters(50, 7, 3, 0.1f, seed);
        Path file = tmp.resolve("v.fvecs");
        Fvecs.writeFvecs(file, original);

        VectorStore read = Fvecs.readFvecs(file);
        assertEquals(7, read.dim());
        assertEquals(50, read.size());
        for (int i = 0; i < 50; i++) {
            assertArrayEquals(original.copyOf(i), read.copyOf(i), TestSeeds.msg(seed));
        }
        assertEquals(10, Fvecs.readFvecs(file, 10).size());
    }

    @Test
    void ivecsRoundTrip() throws IOException {
        int[][] rows = {{3, 1, 2}, {0, 5, 4}};
        Path file = tmp.resolve("g.ivecs");
        Fvecs.writeIvecs(file, rows);
        assertArrayEquals(rows, Fvecs.readIvecs(file));
    }

    @Test
    void rejectsTruncatedFile() throws IOException {
        Path file = tmp.resolve("bad.fvecs");
        Fvecs.writeFvecs(file, VectorStore.wrap(2, new float[] {1, 2, 3, 4}));
        byte[] bytes = Files.readAllBytes(file);
        Files.write(file, java.util.Arrays.copyOf(bytes, bytes.length - 1));
        assertThrows(IOException.class, () -> Fvecs.readFvecs(file));
    }
}

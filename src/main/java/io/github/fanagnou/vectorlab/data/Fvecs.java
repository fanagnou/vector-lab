package io.github.fanagnou.vectorlab.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Readers and writers for the TEXMEX formats used by SIFT and most ANN benchmarks.
 *
 * <p>Each record is a little-endian {@code int dim} followed by {@code dim} little-endian values:
 * {@code float} in {@code .fvecs}, {@code int} in {@code .ivecs}. All records in a file share one
 * dimension. Files are memory-mapped, which limits one file to 2 GB (SIFT1M base is ~0.5 GB).
 */
public final class Fvecs {

    private Fvecs() {}

    /** Reads every vector in an {@code .fvecs} file. */
    public static VectorStore readFvecs(Path path) throws IOException {
        return readFvecs(path, Integer.MAX_VALUE);
    }

    /** Reads at most {@code limit} vectors from an {@code .fvecs} file. */
    public static VectorStore readFvecs(Path path, int limit) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer buf = map(channel);
            int dim = firstDim(buf, path);
            int count = Math.min(recordCount(channel.size(), dim, path), limit);
            float[] flat = new float[Math.multiplyExact(count, dim)];
            int recordBytes = Integer.BYTES + dim * Float.BYTES;
            for (int i = 0; i < count; i++) {
                int pos = i * recordBytes;
                checkDim(buf.getInt(pos), dim, i, path);
                buf.slice(pos + Integer.BYTES, dim * Float.BYTES)
                        .order(ByteOrder.LITTLE_ENDIAN) // slices start big-endian: set the order again
                        .asFloatBuffer()
                        .get(flat, i * dim, dim);
            }
            return VectorStore.wrap(dim, flat);
        }
    }

    /** Reads an {@code .ivecs} file, e.g. ground truth: one row of neighbour ordinals per query. */
    public static int[][] readIvecs(Path path) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer buf = map(channel);
            int dim = firstDim(buf, path);
            int count = recordCount(channel.size(), dim, path);
            int[][] rows = new int[count][dim];
            int recordBytes = Integer.BYTES + dim * Integer.BYTES;
            for (int i = 0; i < count; i++) {
                int pos = i * recordBytes;
                checkDim(buf.getInt(pos), dim, i, path);
                buf.slice(pos + Integer.BYTES, dim * Integer.BYTES)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .asIntBuffer()
                        .get(rows[i]);
            }
            return rows;
        }
    }

    /** Writes vectors in {@code .fvecs} format (used by tests and for exporting subsets). */
    public static void writeFvecs(Path path, VectorStore vectors) throws IOException {
        int dim = vectors.dim();
        ByteBuffer buf = ByteBuffer.allocate(Math.multiplyExact(vectors.size(), Integer.BYTES + dim * Float.BYTES))
                .order(ByteOrder.LITTLE_ENDIAN);
        float[] data = vectors.data();
        for (int i = 0; i < vectors.size(); i++) {
            buf.putInt(dim);
            int off = vectors.offset(i);
            for (int d = 0; d < dim; d++) {
                buf.putFloat(data[off + d]);
            }
        }
        write(path, buf);
    }

    /** Writes rows of equal length in {@code .ivecs} format. */
    public static void writeIvecs(Path path, int[][] rows) throws IOException {
        int dim = rows.length == 0 ? 0 : rows[0].length;
        ByteBuffer buf = ByteBuffer.allocate(Math.multiplyExact(rows.length, Integer.BYTES + dim * Integer.BYTES))
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int[] row : rows) {
            if (row.length != dim) {
                throw new IllegalArgumentException("all rows must have length " + dim);
            }
            buf.putInt(dim);
            for (int v : row) {
                buf.putInt(v);
            }
        }
        write(path, buf);
    }

    private static ByteBuffer map(FileChannel channel) throws IOException {
        if (channel.size() > Integer.MAX_VALUE) {
            throw new IOException("file larger than 2 GB is not supported: " + channel.size() + " bytes");
        }
        MappedByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf;
    }

    private static int firstDim(ByteBuffer buf, Path path) throws IOException {
        if (buf.limit() < Integer.BYTES) {
            throw new IOException("empty or truncated file: " + path);
        }
        int dim = buf.getInt(0);
        if (dim <= 0) {
            throw new IOException("bad dimension " + dim + " in " + path);
        }
        return dim;
    }

    private static int recordCount(long fileSize, int dim, Path path) throws IOException {
        long recordBytes = Integer.BYTES + (long) dim * 4;
        if (fileSize % recordBytes != 0) {
            throw new IOException("size " + fileSize + " of " + path + " is not a multiple of record size " + recordBytes);
        }
        return Math.toIntExact(fileSize / recordBytes);
    }

    private static void checkDim(int actual, int expected, int record, Path path) throws IOException {
        if (actual != expected) {
            throw new IOException("record " + record + " of " + path + " has dim " + actual + ", expected " + expected);
        }
    }

    private static void write(Path path, ByteBuffer buf) throws IOException {
        buf.flip();
        try (FileChannel channel = FileChannel.open(path,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            while (buf.hasRemaining()) {
                channel.write(buf);
            }
        }
    }
}

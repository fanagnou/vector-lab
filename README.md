# vector-lab

Approximate nearest-neighbour search built from scratch in Java 21: exact kNN, **HNSW**, **int8 scalar quantization** and **IVF**, benchmarked on SIFT for recall, latency, throughput and memory.

It's a learning project. The goal is to understand the algorithms behind vector search in Lucene and Elasticsearch well enough to explain and measure their trade-offs, not to compete with them.

## Status

| Slice | What | Status |
|---|---|---|
| 0 | Data loaders, recall harness, exact kNN baseline | 🚧 |
| 1 | HNSW: layered graph, beam search, neighbour-selection heuristic | ⏳ |
| 2 | `ef` sweep (recall vs QPS); scalar vs Panama Vector API dot product (JMH) | ⏳ |
| 3 | int8 scalar quantization with oversampling + rescoring | ⏳ |
| 4 | Concurrent graph build and search; platform vs virtual threads | ⏳ |
| 5 | IVF (k-means, `nprobe`) vs HNSW | ⏳ |

## Run it

Needs JDK 21 (Gradle finds a local one or downloads it) and Linux/macOS for the data script.

```bash
./gradlew check                     # build + tests
./scripts/get-sift.sh               # SIFT10K into data/siftsmall
./gradlew run --args="--data data/siftsmall --index flat --k 10"
./gradlew jmh -PjmhArgs="DotProduct"
```

## Results

Machine: Intel Core i7-1165G7 (4 cores / 8 threads, AVX-512), 64 GB RAM, Ubuntu 26.04, JDK 21. Single-threaded search unless noted.

| dataset | index | params | build s | QPS | p50 ms | p99 ms | recall@10 | RAM MB |
|---|---|---|---|---|---|---|---|---|
| | | | | | | | | |

## Layout

```
src/main/java/io/github/fanagnou/vectorlab/
  VectorIndex, Similarity      the contract every index implements; scoring
  data/                        VectorStore (flat float[]), .fvecs/.ivecs readers, synthetic data
  bench/                       Bench CLI, recall@k
  flat/  hnsw/  quant/  ivf/   the indexes
  simd/                        scalar (and, later, Panama Vector API) kernels
src/test/java/...              unit tests; seeded random data (replay: -Dtests.seed=<seed>)
src/jmh/java/...               JMH micro-benchmarks
```

## How I used AI

I used Claude Code throughout, with a fixed split (see `CLAUDE.md`):

- **AI-written, reviewed by me:** build and CI setup, data loaders, the benchmark harness, test scaffolding and the brute-force test oracle.
- **Written by me:** every index algorithm (`flat/` search, `hnsw/`, `quant/`, `ivf/`) and the SIMD kernel. AI was used for explaining concepts, reviewing my diffs and suggesting tests, not for writing this code.
- **Notes per slice:** _to be filled in as the slices land: what I asked, what I accepted or rejected, and why._

## References

- Malkov & Yashunin, *Efficient and robust approximate nearest neighbor search using Hierarchical Navigable Small World graphs*, [arXiv:1603.09320](https://arxiv.org/abs/1603.09320)
- Jégou, Douze & Schmid, *Product quantization for nearest neighbor search* (SIFT datasets: [TEXMEX corpus](http://corpus-texmex.irisa.fr/))
- Elastic Search Labs: [scalar quantization 101](https://www.elastic.co/search-labs/blog/scalar-quantization-101), [DiskBBQ (IVF)](https://www.elastic.co/search-labs/blog/diskbbq-elasticsearch-introduction)

## Licence

Apache License 2.0

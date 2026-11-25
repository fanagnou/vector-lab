# CLAUDE.md: rules for AI assistants in vector-lab

This is Foivos's learning and interview project: exact kNN, HNSW, int8 quantization and IVF built from scratch in Java 21, benchmarked against exact search. He must be able to explain every line of the algorithm code in an interview, so the split below is strict.

## Who writes what

| Area | Who | Notes |
|---|---|---|
| `flat/` search, `hnsw/`, `quant/`, `ivf/`, `simd/PanamaOps` | **Foivos** | Don't write or rewrite this code, not even "just a sketch". Allowed: explain a concept, ask a guiding question, point at the paper section, review his diff, write tests for it |
| `data/`, `bench/`, `simd/ScalarOps`, build files, CI, scripts | Claude | Keep it small and readable; he reads it to learn |
| Tests (`src/test`) and benchmarks (`src/jmh`) | Claude drafts, Foivos reviews | Tests are the spec: write them before he implements |
| README results + "How I used AI" | Both | Log every substantial AI contribution in the README |

Hints for his code follow steps: (1) a question, (2) the key observation or data structure, (3) the idea in words, (4) code only if he asks after trying.

## Conventions
- Java 21, no dependencies in main code (JDK only). Package `io.github.fanagnou.vectorlab`.
- Vectors live in one flat `float[]` (`VectorStore`); score in place with offsets, don't copy per comparison.
- `Similarity.score` is "higher = more similar", never negative. Ties rank the smaller ordinal first.
- Randomness is always seeded. Tests take seeds from `TestSeeds.seed()` and print `TestSeeds.msg(seed)` on failure.
- Every index implements `VectorIndex` and gets a case in `Bench.create`.

## Commands
```bash
./gradlew check                                   # compile + unit tests + compile benchmarks (CI)
./gradlew test --tests '*FlatIndexTest*'          # one test class
./gradlew test -Dtests.seed=123 --rerun           # replay a random failure
./scripts/get-sift.sh                             # SIFT10K into data/siftsmall (add --full for SIFT1M)
./gradlew run --args="--data data/siftsmall --index flat --k 10"
./gradlew jmh -PjmhArgs="DotProduct"
```

## Workflow
- One branch + PR per slice (`slice-1-hnsw`, ...). CI must be green. Review = numbered comments on bugs, concurrency, edge cases, missing tests, naming; no rewrites of his algorithm code.
- Default permission mode: ask before every edit.

# Vacancy top-K microbenchmarks

Select the K highest-paying vacancies from N in-memory records, ordered by salary descending and ID ascending for ties.

| Implementation | One measured operation |
| --- | --- |
| `sort` | Copy all N records, sort them, materialize the first K as an ArrayList |
| `heap` | Scan N records, maintain at most K candidates in a worst-first PriorityQueue, materialize and sort the selected K |

Both return the same ordered records without modifying the input. Working allocations and final result construction are timed. Fixture generation is outside measurement. No Spring, HTTP, database or production service is involved.

## Run

From the repository root, with Python 3.10+ and Docker running:

```sh
python3 -m unittest discover -s benchmarks -p 'test_*.py'
python3 benchmarks/run.py
```

The runner compiles and checks the Java implementation in a JDK 25 image. No host JDK or Python packages are required. The first image build may require network access; measured containers have no network.

| Parameter | Default |
| --- | --- |
| Independent Docker/JVM runs | 2 |
| N | 1 000; 10 000; 100 000 |
| K | 20; 100; N / 2 |
| Input order | Random; salary ascending; salary descending |
| Measured combinations | 54 + one harness baseline per run |
| Trials per combination | 100 |
| Individually timed operations per trial | 4 |
| Warmup | At least 64 operations AND 250 ms per combination |
| Teardown | 16 tail operations per combination |

```sh
python3 benchmarks/run.py --runs 4 --trials 100 --samples 8
python3 benchmarks/run.py --smoke --runs 1
python3 benchmarks/report.py benchmarks/results/<run-directory>
```

Smoke runs only check wiring and carry an explicit warning. Each invocation creates a new directory and refuses to overwrite an existing one. Older loop/stream results remain readable and can still be regenerated from their original manifests.

## Measurement

- Each run uses a distinct derived Docker image, fresh container and JVM, built from the same compiled base image. Only the run label differs.
- Resources: one logical CPU with affinity and quota, 512 MiB memory without extra swap, 128 MiB fixed heap, Serial GC, one Java-visible processor, synchronous JIT compilation. Docker Desktop pins a Linux VM CPU, not an exclusive physical host core.
- All cases use the same seeded vacancies; only order and K differ. Salary ties have an explicit ID tie-break. Alternate runs reverse a seeded shuffle of the case order.
- Setup creates immutable fixtures and preallocates timing buffers. Each measured operation starts from that unchanged fixture; neither implementation reuses a previous result.
- `System.nanoTime()` surrounds algorithm execution. A volatile sink consumes the result afterward. CSV output and fixture setup are outside the timed interval.
- Warmup and teardown operations/durations are recorded separately and excluded from latency statistics. Fixed warmup cannot prove steady state; inspect time plots and compare independent runs.
- The harness baseline uses the same timing/dispatch path with a trivial cached result. The ideal ≤5 ns target is reported even when missed. Baseline timings are never subtracted.
- The recommended 100 ns–50 ms interval is checked against individual operation latencies. Outliers are retained and counted, not removed.
- Cleanup removes only images and containers created by the current invocation. Application containers and Docker build cache are left alone.

`--verify` runs during image build and before each benchmark: known ranking, salary ties, empty input, K=0/1/N/>N, negative K, all benchmark sizes/orders, 32 random seeds, and input immutability. These checks are outside the measured phases.

## Results

[Saved report](results/20261012T203308Z-52fb4024/index.html) · [Diagnostics](results/20261012T203308Z-52fb4024/diagnostics.html) · [Verification](verification-short.md)

| File | Contents |
| --- | --- |
| `index.html` | Run/N/K selectors; p50/p75/p90/p95/p99 comparison tabs; expanded numerical statistics for each input order and baseline |
| `diagnostics.html` | Extended report with scenario time/distribution/histogram plots, phase counters and durations |
| `run-*.csv` | Every operation's latency/timestamp, trial, N, K, order and implementation; phase counters and durations |
| `summary.json` | Mean, sample SD, min/max, p50/p75/p90/p95/p99, counts and out-of-range operations for every case/run |
| `manifest.json` | Configuration, source hash, image/container IDs, actual Docker limits, JVM details and wall durations |
| `build.log`, `run-*.log`, `cleanup.log` | Correctness checks, build/runtime and scoped cleanup diagnostics |

The offline HTML contains no explanatory paragraphs. Both implementations share chart axes and colors. Percentile comparisons use tabs; run/N/K selectors remain. “Подробная статистика” is expanded by default. “Накладные расходы” and “Фазы и счётчики” are static sections with always-visible tables. In the main report, “Подробная статистика” contains only three numerical tables. Required diagnostic charts appear consecutively in the separate diagnostics report, which also includes phase details.

Percentiles are linearly interpolated over individual operations, not trial averages. Scenario details show trial means against trial number. The time plot uses those means against actual elapsed time relative to each implementation's first sample. The cumulative distribution shows the percentage of individual operations completed within a given duration, including all samples. Scenario histograms count every operation in 24 shared linear bins. Display units adapt between ns/µs/ms without changing measurements. The harness contains numerical statistics and phase counters without plots. Separate JVM runs are not pooled. Wall time includes container lifecycle and, for the whole invocation, builds; phase times separately record benchmark execution.

The benchmark is separate from Gradle correctness tests and CI. One repeated trial is not an independent JVM restart. Differences apply to the measured JVM, hardware, fixtures, input order and K; the report does not assume a universal winner.

References: [PriorityQueue](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/PriorityQueue.html), [System.nanoTime](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/System.html#nanoTime()).

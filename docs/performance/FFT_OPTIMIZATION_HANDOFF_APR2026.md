# FFT Optimization Handoff - April 28, 2026

## Scope

This handoff records the current optimization state before shutdown. The work intentionally focused on the active optimized FFT implementations only:

- `FFTOptimized8`
- `FFTOptimized16`

The generic `FFTBase` implementation was used as a benchmark baseline only.

## Files Changed

- `src/main/java/com/fft/core/FFTResult.java`
  - Added `FFTResult.fromTrustedArray(double[])`.
  - Purpose: let internal FFT producers hand off a newly allocated result array without a second defensive copy.
  - Public immutable accessors still return copies, so external immutability behavior is preserved.

- `src/main/java/com/fft/optimized/FFTOptimized8.java`
  - Added package-private `transformToInterleaved(...)`.
  - `transform(...)` now writes into one interleaved output array and returns it through `FFTResult.fromTrustedArray(...)`.
  - Purpose: reuse FFT8 as a low-allocation internal kernel for FFT16.

- `src/main/java/com/fft/optimized/FFTOptimized16.java`
  - Removed intermediate `FFTResult` objects and `getRealParts()` / `getImaginaryParts()` extraction.
  - Uses `FFTOptimized8.transformToInterleaved(...)` for even and odd halves.
  - Uses two `ThreadLocal<double[]>` buffers for the even/odd 8-point intermediate outputs.
  - Still allocates the final 16-point interleaved result array per transform.

- `src/test/java/com/fft/performance/FFTPerformanceBenchmark.java`
  - Benchmarks now separate normal API/kernel calls from `WithInputClone` variants.
  - Purpose: avoid mixing caller-side defensive input cloning into the optimized transform measurement.

## Benchmark Commands

Correctness and concurrency:

```bash
mvn -q '-Dtest=FFTOptimized8Test,FFTOptimized16Test,PerformanceComparisonTest,ConcurrencyTest' test
```

Main JMH comparison:

```bash
cmd /c run-jmh-benchmarks.bat FFTPerformanceBenchmark -f 1 -wi 3 -i 5 -r 500ms -w 500ms
```

FFT16 allocation check:

```bash
java -cp <target/test-classes;target/classes;JMH deps> org.openjdk.jmh.Main ^
  "FFTPerformanceBenchmark.benchmarkFFT16_Optimized$" ^
  -f 1 -wi 3 -i 5 -r 500ms -w 500ms -prof gc
```

The direct PowerShell classpath used during this session was built from:

- `target/test-classes`
- `target/classes`
- `jmh-core-1.37.jar`
- `jmh-generator-annprocess-1.37.jar`
- `jopt-simple-5.0.4.jar`
- `commons-math3-3.6.1.jar`
- `slf4j-api-2.0.9.jar`
- `logback-classic-1.5.19.jar`
- `logback-core-1.5.19.jar`

## Measured Results

Environment:

- Windows / PowerShell
- JDK `24.0.2`
- JMH `1.37`
- Benchmark mode: average time, nanoseconds/op
- Short run for engineering feedback: `-f 1 -wi 3 -i 5 -r 500ms -w 500ms`

Before this session:

| Benchmark | Time | Allocation |
| --- | ---: | ---: |
| `FFT8_Optimized` | ~45.5 ns/op | ~472 B/op |
| `FFT16_Optimized` | ~170.3 ns/op | ~2120 B/op |

After `fromTrustedArray`, FFT8 helper extraction, and FFT16 intermediate removal:

| Benchmark | Time | Allocation |
| --- | ---: | ---: |
| `FFT8_Optimized` | ~21.8 ns/op | ~168 B/op |
| `FFT16_Optimized` | ~57.9-70.5 ns/op | ~584 B/op |

After adding `ThreadLocal` even/odd buffers in `FFTOptimized16`:

| Benchmark | Time | Allocation |
| --- | ---: | ---: |
| `FFT16_Optimized` | ~49.5 ns/op | ~296 B/op |

Current best comparison from the full JMH run:

| Benchmark | Time |
| --- | ---: |
| `FFT16_Base` | ~297.2 ns/op |
| `FFT16_Optimized` | ~70.5 ns/op |
| `FFT16_Optimized_WithInputClone` | ~112.3 ns/op |
| `FFT8_Base` | ~185.4 ns/op |
| `FFT8_Optimized` | ~21.8 ns/op |
| `FFT8_Optimized_WithInputClone` | ~44.2 ns/op |

Interpretation:

- `FFTOptimized8` is now roughly 8x faster than the measured `FFT8_Base` in the short JMH run.
- `FFTOptimized16` is now roughly 4x-6x faster than the measured `FFT16_Base`, depending on run variance.
- The major win came from removing avoidable array copies and intermediate `FFTResult` extraction.
- The remaining optimized allocation floor is mostly the final immutable result representation and small unavoidable object overhead.

## Validation Status

The following command passed:

```bash
mvn -q '-Dtest=FFTOptimized8Test,FFTOptimized16Test,PerformanceComparisonTest,ConcurrencyTest' test
```

The `ConcurrencyTest` pass is important because `FFTOptimized16` now uses `ThreadLocal` intermediate buffers.

## Known Caveats

- Benchmark runs were intentionally short to fit the session. For release-grade numbers, rerun with more forks and longer measurement windows.
- JDK 24 emits warnings for JMH / Maven dependencies using restricted or deprecated APIs. These warnings did not block tests or benchmarks.
- `ThreadLocal` buffers trade a small per-thread retained memory footprint for lower per-call allocation. This is appropriate for hot FFT16 loops, but should be reconsidered if the library is used with very high thread churn.
- `.factorypath` was untracked before this handoff and was not modified.

## Recommended Next Steps

1. Rerun full JMH with `-f 3 -wi 10 -i 20` before treating the numbers as final.
2. Consider a direct fully unrolled FFT16 kernel if more speed is needed; current FFT16 still calls the FFT8 kernel twice.
3. Consider package-private result accessors or internal views only if more multi-stage optimized FFTs are added.
4. Keep `FFTBase` changes out of scope unless the goal shifts from optimized specializations to generic-size performance.

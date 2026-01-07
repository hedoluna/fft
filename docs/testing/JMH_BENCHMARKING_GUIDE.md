# JMH Benchmarking Guide

**Purpose**: Provide statistically rigorous performance measurements with variance analysis to address the performance instability identified in P0 analysis (see `../performance/CONSENSUS_ANALYSIS.md`).

---

## Table of Contents

- [Background](#background)
- [Installation](#installation)
- [Running Benchmarks](#running-benchmarks)
  - [Using Helper Scripts (Recommended)](#using-helper-scripts-recommended)
  - [Using Maven Exec Plugin](#using-maven-exec-plugin)
- [Available Benchmarks](#available-benchmarks)
- [Understanding Results](#understanding-results)
- [Benchmark Configuration](#benchmark-configuration)
- [Recommended Workflow](#recommended-workflow)
- [Technical Details](#technical-details)
- [Troubleshooting](#troubleshooting)
- [Integration with CI/CD](#integration-with-cicd)

---

## Background

Multi-agent consensus analysis revealed FFT8 performance varies significantly (2.36x-3.36x) across different measurement runs. This variance is likely due to:
- JIT compilation variability
- Timing measurement methodology issues
- System load during benchmarking

JMH (Java Microbenchmark Harness) provides:
- Proper JVM warmup
- Statistical variance calculation
- Fork-based isolation
- Dead code elimination prevention

---

## Installation

JMH dependencies are already configured in `pom.xml`:
- `org.openjdk.jmh:jmh-core:1.37`
- `org.openjdk.jmh:jmh-generator-annprocess:1.37`

The JMH annotation processor is configured in the Maven compiler plugin to automatically generate benchmark metadata during `mvn test-compile`.

---

## Running Benchmarks

### Using Helper Scripts (Recommended)

**The `maven-exec-plugin` has resource loading issues** that cause JMH to fail finding the `BenchmarkList` file. Use the provided helper scripts instead, which use direct `java -cp` command with proper classpath configuration.

#### Windows
```bash
# Run ALL benchmarks
run-jmh-benchmarks.bat

# Run benchmarks matching "FFT8"
run-jmh-benchmarks.bat FFT8

# Run all FFTBaseProfiling benchmarks
run-jmh-benchmarks.bat FFTBaseProfiling

# With custom JMH options
run-jmh-benchmarks.bat FFT8 -f 3 -wi 10 -i 20
```

#### Linux/Mac
```bash
# Run ALL benchmarks
./run-jmh-benchmarks.sh

# Run benchmarks matching "FFT8"
./run-jmh-benchmarks.sh FFT8

# Run all FFTBaseProfiling benchmarks
./run-jmh-benchmarks.sh FFTBaseProfiling

# With custom JMH options
./run-jmh-benchmarks.sh FFT8 -f 3 -wi 10 -i 20
```

**Important Notes**:
- When no pattern is specified, JMH runs ALL benchmarks
- Do NOT use `.*` as a pattern - it won't match anything
- Use specific patterns like `FFT8`, `FFTBaseProfiling`, or leave empty for all

#### How the Helper Scripts Work

The scripts:
1. Compile test classes: `mvn clean test-compile`
2. Generate JMH metadata via annotation processor → `target/test-classes/META-INF/BenchmarkList`
3. Build classpath with:
   - `target/test-classes` (JMH generated benchmarks)
   - `target/classes` (production code)
   - JMH dependencies (jmh-core, jmh-generator-annprocess)
   - Project dependencies (SLF4J, Logback, etc.)
4. Run JMH directly: `java -cp <classpath> org.openjdk.jmh.Main <pattern> <options>`

### Using Maven Exec Plugin

**Note**: The maven-exec-plugin has known issues with resource loading from the test classpath. If you encounter errors about missing `META-INF/BenchmarkList`, use the helper scripts instead.

#### Run All Benchmarks
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.classpathScope=test
```

#### Run Specific Size (e.g., FFT8)
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="FFT8" \
  -Dexec.classpathScope=test
```

#### Run with Custom Options
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="-f 1 -wi 3 -i 5 FFT8" \
  -Dexec.classpathScope=test
```

#### Generate Full Report with Variance
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="-rf json -rff jmh-results.json" \
  -Dexec.classpathScope=test
```

---

## Available Benchmarks

### FFTBaseProfilingBenchmark
Profiling individual FFTBase operations:
- Twiddle factor calculation
- Bit-reversal permutation
- Butterfly operations
- Array copying methods
- Full FFT transform

**Usage**:
```bash
./run-jmh-benchmarks.sh FFTBaseProfiling
```

### FFTPerformanceBenchmark
Comparing optimized vs base implementations:
- FFT8, FFT16, FFT32, FFT64, FFT128

**Usage**:
```bash
./run-jmh-benchmarks.sh FFTPerformanceBenchmark
```

### Running Specific Benchmarks
```bash
# Run only FFT8 benchmarks
./run-jmh-benchmarks.sh FFT8

# Run profiling benchmarks with custom options
./run-jmh-benchmarks.sh FFTBaseProfiling -f 1 -wi 3 -i 5 -r 1s
```

---

## JMH Command-Line Options

Common JMH options you can pass to the scripts or maven exec:

- `-f <forks>` - Number of JVM forks (default: 3)
- `-wi <iterations>` - Warmup iterations (default: 5)
- `-i <iterations>` - Measurement iterations (default: 10)
- `-r <time>` - Time per measurement iteration (default: 2s)
- `-w <time>` - Warmup time per iteration (default: 2s)
- `-t <threads>` - Number of threads (default: 1)
- `-tu <unit>` - Time unit (ns, us, ms, s)
- `-rf <format>` - Result format (json, csv, text)
- `-rff <file>` - Result file name

**Example**:
```bash
./run-jmh-benchmarks.sh FFTBaseProfiling -f 1 -wi 3 -i 5 -r 1s
```

---

## Understanding Results

### Sample Output Format
```
Benchmark                                    Mode  Cnt     Score      Error  Units
FFTPerformanceBenchmark.benchmarkFFT8_Base   avgt   30   239.234 ±   12.456  ns/op
FFTPerformanceBenchmark.benchmarkFFT8_Opt    avgt   30    71.234 ±    8.123  ns/op
```

**Interpreting Results:**
- **Score**: Mean execution time
- **Error**: 99.9% confidence interval (±)
- **Cnt**: Total samples (forks × iterations)
- **Mode**: avgt = Average Time
- **Units**: ns/op = nanoseconds per operation

Benchmarks output detailed statistics including:
- **Average time per operation** (ns/op)
- **Confidence intervals** (99.9%)
- **Min/Max/Avg** values
- **Standard deviation**

### Calculate Speedup
```
Speedup = Base Score / Optimized Score
Example: 239.234 / 71.234 = 3.36x

Speedup Range (accounting for variance):
Lower bound = (Base - Error) / (Opt + Error)
Upper bound = (Base + Error) / (Opt - Error)
```

### Variance Analysis
```
Relative Error = (Error / Score) × 100%
Example: (8.123 / 71.234) × 100% = 11.4%

If Relative Error > 5%:
  - Increase warmup iterations (-wi)
  - Increase measurement iterations (-i)
  - Check for background system load
```

---

## Benchmark Configuration

Current settings in `FFTPerformanceBenchmark.java`:
- **Warmup**: 5 iterations × 2 seconds = 10s per fork
- **Measurement**: 10 iterations × 2 seconds = 20s per fork
- **Forks**: 3 (for statistical confidence)
- **JVM Args**: `-Xms2G -Xmx2G` (consistent heap)
- **Mode**: AverageTime (mean execution time)
- **Unit**: NANOSECONDS

**Total runtime per benchmark**: ~90 seconds (30s × 3 forks)

---

## Recommended Workflow

### 1. Quick Check (Single Fork)
```bash
./run-jmh-benchmarks.sh FFT8 -f 1 -wi 3 -i 5
```
**Time**: ~40 seconds per size

### 2. Full Analysis (Default Config)
```bash
./run-jmh-benchmarks.sh
```
**Time**: ~15 minutes for all sizes

### 3. Generate Report
```bash
./run-jmh-benchmarks.sh -rf csv -rff jmh-results.csv
```

Then analyze with Excel/Python:
```python
import pandas as pd
df = pd.read_csv('jmh-results.csv')
print(df[['Benchmark', 'Score', 'Score Error (99.9%)']])
```

---

## Technical Details

### JMH Annotation Processor Configuration

The `pom.xml` includes JMH annotation processor in the compiler plugin:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.openjdk.jmh</groupId>
                <artifactId>jmh-generator-annprocess</artifactId>
                <version>1.37</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

During `mvn test-compile`, the annotation processor:
1. Scans test classes for JMH annotations (`@Benchmark`, `@State`, etc.)
2. Generates test harness classes in `target/generated-test-sources/test-annotations/`
3. Creates `target/test-classes/META-INF/BenchmarkList` with benchmark metadata

### Classpath Requirements

JMH requires these JARs on the classpath:
- `jmh-core-1.37.jar` - Core JMH runtime
- `jmh-generator-annprocess-1.37.jar` - Annotation processor
- `jopt-simple-5.0.4.jar` - Command-line parsing
- `commons-math3-3.6.1.jar` - Statistical analysis

The helper scripts automatically configure all required dependencies.

### Why Maven Exec Plugin Fails

The `maven-exec-plugin` does not properly load resources from the test classpath, causing JMH to fail with:
```
ERROR: Unable to find the resource: /META-INF/BenchmarkList
```

Even when using `-Dexec.classpathScope=test`, the plugin fails to find the `BenchmarkList` file generated by the JMH annotation processor at `target/test-classes/META-INF/BenchmarkList`.

**Solution**: Use the helper scripts (`run-jmh-benchmarks.bat` / `run-jmh-benchmarks.sh`) which use direct `java -cp` command with proper classpath configuration.

---

## Addressing P0 Recommendations

From P0 analysis (see `../performance/CONSENSUS_ANALYSIS.md`):

### ✅ Stabilize FFT8 Performance Measurement
- JMH provides proper warmup (5 iterations)
- Multiple forks (3) for statistical independence
- Consistent JVM settings (-Xms2G -Xmx2G)
- Error bars with 99.9% confidence intervals

### ✅ Report Mean ± Standard Deviation
- JMH automatically calculates Score ± Error
- Error represents 99.9% confidence interval
- Can export to CSV/JSON for detailed analysis

### 📊 Expected Results (Based on Current Measurements)
- **FFT8**: Should stabilize around 2.7-3.0x ± 10-15%
- **FFT16**: ~0.99x ± 5%
- **FFT32**: ~1.12x ± 5%
- **FFT64**: ~1.01x ± 5%
- **FFT128**: ~1.42x ± 5%

### Goal: Reduce Variance from ±15% to ±5%
If variance remains high:
1. Increase warmup iterations: `-wi 10`
2. Increase measurement iterations: `-i 20`
3. Check system stability (close other apps)
4. Consider different JVM flags

---

## Troubleshooting

### High Variance (>15%)
**Symptoms**: Large Error values relative to Score

**Solutions**:
1. Increase iterations: `-wi 10 -i 20`
2. Ensure consistent system load
3. Use fixed CPU frequency (disable turbo boost)
4. Check for GC activity

### Benchmark Too Slow
**Symptoms**: Takes >30 minutes

**Solutions**:
1. Reduce forks: `-f 1`
2. Reduce iterations: `-wi 3 -i 5`
3. Run specific benchmarks: `./run-jmh-benchmarks.sh FFT8`

### OutOfMemoryError
**Symptoms**: JVM crashes during benchmark

**Solutions**:
1. Increase heap: Add `-jvmArgs '-Xms4G -Xmx4G'` option
2. Check for memory leaks in FFT implementations

### BenchmarkList Not Found Error
**Symptoms**: `ERROR: Unable to find the resource: /META-INF/BenchmarkList`

**Solutions**:
1. **Use helper scripts** (recommended): `./run-jmh-benchmarks.sh`
2. Ensure `mvn test-compile` ran successfully
3. Check that `target/test-classes/META-INF/BenchmarkList` exists
4. Verify JMH annotation processor is configured in pom.xml

---

## Integration with CI/CD

### Performance Regression Detection
```bash
# Run benchmarks and save baseline
./run-jmh-benchmarks.sh -rf json -rff baseline.json

# Compare with new results
./run-jmh-benchmarks.sh -rf json -rff current.json

# Use jmh-result-analysis tool or custom script to compare
```

---

## References

- [JMH Documentation](https://github.com/openjdk/jmh)
- [JMH Samples](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/)
- `../performance/CONSENSUS_ANALYSIS.md`: Multi-agent analysis of P0/P1 recommendations
- `../performance/FASE_2_LESSONS_LEARNED.md`: Strategic lessons and future optimization directions

---

## See Also

- `../../CLAUDE.md` - **Current optimization status** (v2.1 verified: 1.06-1.09x speedup)
- `VALIDATION_FRAMEWORK.md` - FFT correctness validation
- `PITCH_DETECTION_ANALYSIS.md` - Audio processing accuracy analysis
- `../performance/BASELINE_MEASUREMENT_JAN2026.md` - v2.1 baseline measurements

---

**Next Steps After JMH Analysis:**
1. Update README.md with stable performance numbers ± variance
2. Investigate any sizes with >15% variance
3. Consider profiling if optimization targets aren't met
4. Document methodology in all performance claims

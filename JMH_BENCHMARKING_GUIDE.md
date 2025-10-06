# JMH Benchmarking Guide

**Purpose**: Provide statistically rigorous performance measurements with variance analysis to address the performance instability identified in CONSENSUS_ANALYSIS.md.

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

## Installation

JMH dependencies are already configured in `pom.xml`:
- `org.openjdk.jmh:jmh-core:1.37`
- `org.openjdk.jmh:jmh-generator-annprocess:1.37`

## Running Benchmarks

### Run All Benchmarks
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.classpathScope=test
```

### Run Specific Size (e.g., FFT8)
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="FFT8" \
  -Dexec.classpathScope=test
```

### Run with Custom Options
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="-f 1 -wi 3 -i 5 FFT8" \
  -Dexec.classpathScope=test
```

**Options:**
- `-f <num>`: Number of forks (default: 3)
- `-wi <num>`: Warmup iterations (default: 5)
- `-i <num>`: Measurement iterations (default: 10)
- `-tu <unit>`: Time unit (ns, us, ms, s)

### Generate Full Report with Variance
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="-rf json -rff jmh-results.json" \
  -Dexec.classpathScope=test
```

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

## Benchmark Configuration

Current settings in `FFTPerformanceBenchmark.java`:
- **Warmup**: 5 iterations × 2 seconds = 10s per fork
- **Measurement**: 10 iterations × 2 seconds = 20s per fork
- **Forks**: 3 (for statistical confidence)
- **JVM Args**: `-Xms2G -Xmx2G` (consistent heap)
- **Mode**: AverageTime (mean execution time)
- **Unit**: NANOSECONDS

**Total runtime per benchmark**: ~90 seconds (30s × 3 forks)

## Recommended Workflow

### 1. Quick Check (Single Fork)
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="-f 1 -wi 3 -i 5 FFT8" \
  -Dexec.classpathScope=test
```
**Time**: ~40 seconds per size

### 2. Full Analysis (Default Config)
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.classpathScope=test
```
**Time**: ~15 minutes for all sizes

### 3. Generate Report
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="-rf csv -rff jmh-results.csv" \
  -Dexec.classpathScope=test
```

Then analyze with Excel/Python:
```python
import pandas as pd
df = pd.read_csv('jmh-results.csv')
print(df[['Benchmark', 'Score', 'Score Error (99.9%)']])
```

## Addressing P0 Recommendations

From CONSENSUS_ANALYSIS.md P0 priorities:

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
3. Run specific benchmarks: `-Dexec.args="FFT8"`

### OutOfMemoryError
**Symptoms**: JVM crashes during benchmark

**Solutions**:
1. Increase heap: `-Dexec.args="-jvmArgs '-Xms4G -Xmx4G'"`
2. Check for memory leaks in FFT implementations

## Integration with CI/CD

### Performance Regression Detection
```bash
# Run benchmarks and save baseline
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="-rf json -rff baseline.json" \
  -Dexec.classpathScope=test

# Compare with new results
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="-rf json -rff current.json" \
  -Dexec.classpathScope=test

# Use jmh-result-analysis tool or custom script to compare
```

## References

- [JMH Documentation](https://github.com/openjdk/jmh)
- [JMH Samples](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/)
- CONSENSUS_ANALYSIS.md: Multi-agent analysis identifying performance variance
- OPTIMIZATION_LESSONS_LEARNED.md: What worked and what didn't

---

**Next Steps After JMH Analysis:**
1. Update README.md with stable performance numbers ± variance
2. Investigate any sizes with >15% variance
3. Consider profiling if optimization targets aren't met
4. Document methodology in all performance claims

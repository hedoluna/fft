# FFTBase Profiling Results

**Purpose**: Identify performance bottlenecks in FFTBase reference implementation
**Created**: October 6, 2025 (P1 Implementation)
**Status**: ⏳ BENCHMARKS PENDING - Run required

---

## How to Run Profiling Benchmarks

The profiling benchmark suite has been created in `src/test/java/com/fft/performance/FFTBaseProfilingBenchmark.java` but needs to be executed to generate results.

### Quick Run (Single Fork, Faster)
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="-f 1 -wi 3 -i 5 FFTBaseProfiling" \
  -Dexec.classpathScope=test
```
**Time**: ~5-10 minutes

### Full Analysis (3 Forks, Statistical Rigor)
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="FFTBaseProfiling" \
  -Dexec.classpathScope=test
```
**Time**: ~30-45 minutes

### Generate CSV Report
```bash
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="-rf csv -rff profiling-results.csv FFTBaseProfiling" \
  -Dexec.classpathScope=test
```

---

## Benchmarks Included

The profiling suite isolates the following operations across sizes 32, 64, 128, 256:

| Benchmark | What It Measures | Expected Bottleneck |
|-----------|-----------------|---------------------|
| `benchmarkFullFFT` | Complete FFT transform | Baseline for comparison |
| `benchmarkTwiddleFactors` | Math.cos/sin calls | **HIGH** - Called in inner loops |
| `benchmarkPrecomputedTwiddles` | Precomputed vs on-the-fly | Compare approaches |
| `benchmarkBitReversal` | Bit-reversal permutation | **MEDIUM** - Called per element |
| `benchmarkButterfly` | Butterfly operations | **MEDIUM** - Core computation |
| `benchmarkManualArrayCopy` | Manual for-loop copy | Compare with stdlib |
| `benchmarkSystemArrayCopy` | System.arraycopy | Compare with stdlib |
| `benchmarkArrayClone` | Array.clone() | Compare with stdlib |

---

## Expected Findings (Hypothesis)

Based on algorithm analysis and CONSENSUS_ANALYSIS.md insights:

### Primary Bottlenecks (Predicted)
1. **Twiddle Factor Calculation**: 40-60% of time
   - Math.cos() and Math.sin() called in nested loops
   - Dominant cost for sizes > 64
   - **Solution**: Precomputed lookup tables

2. **Bit-Reversal**: 15-25% of time
   - Called for every element
   - Complex bit manipulation logic
   - **Solution**: Hardcoded swap patterns for small sizes

3. **Butterfly Operations**: 20-30% of time
   - Core computation (unavoidable)
   - Memory access patterns affect cache
   - **Solution**: Loop unrolling, cache optimization

### Secondary Analysis
4. **Array Copying**: 5-10% of time
   - Compare manual vs System.arraycopy
   - Hypothesis: System.arraycopy should be faster for size > 16
   - **Finding from FFT8**: Manual copy competitive for small sizes

---

## Analysis Template (To Fill After Running)

Once benchmarks complete, update this section with actual results:

### Performance Distribution

```
[TO BE FILLED]

Example format:
Operation                Time (ns)   % of Total
--------------------------------------------
Full FFT                   1234       100%
Twiddle Factors            567        46%
Bit-Reversal               234        19%
Butterfly Operations       345        28%
Array Copy (manual)        45         4%
Array Copy (System)        33         3%
```

### Key Insights

1. **Dominant Bottleneck**: [TBD]
2. **Optimization Priority Order**: [TBD]
3. **Unexpected Findings**: [TBD]

### Recommendations

Based on profiling results:

**P0 - Critical**:
- [ ] [TBD - Based on top bottleneck]

**P1 - Important**:
- [ ] [TBD - Based on second bottleneck]

**P2 - Nice to Have**:
- [ ] [TBD - Based on minor optimizations]

---

## A/B Comparison Results

### Array Copying (Manual vs System.arraycopy)

**Size 8**:
- Manual: [TBD] ns/op
- System.arraycopy: [TBD] ns/op
- Winner: [TBD]

**Size 32**:
- Manual: [TBD] ns/op
- System.arraycopy: [TBD] ns/op
- Winner: [TBD]

**Size 128**:
- Manual: [TBD] ns/op
- System.arraycopy: [TBD] ns/op
- Winner: [TBD]

**Conclusion**: [TBD - Likely System.arraycopy for larger sizes, manual competitive for small]

### Twiddle Factors (On-the-fly vs Precomputed)

**Size 32**:
- On-the-fly (Math.cos/sin): [TBD] ns/op
- Precomputed lookup: [TBD] ns/op
- Speedup: [TBD]x

**Conclusion**: [TBD - Expected 2-3x speedup with precomputation]

---

## Integration with Optimization Work

### How to Use These Results

1. **Identify Top Bottleneck**: Focus optimization efforts on highest time consumer
2. **Validate Controversial Claims** (from OPTIMIZATION_LESSONS_LEARNED.md):
   - Manual array copy vs System.arraycopy → Resolved by benchmarks
   - Loop unrolling effectiveness → Compare butterfly vs full FFT
   - Hardcoded constants benefit → Compare twiddle benchmarks

3. **Guide FFT16/32 Optimization**:
   - If twiddle factors dominant → Use precomputed tables
   - If bit-reversal significant → Use hardcoded swaps
   - If butterfly operations dominant → Focus on algorithm choice (radix-4 vs radix-2)

### Expected Impact on Future Optimizations

**FFT16 Radix-4 Approach**:
- If twiddle dominates: Radix-4 uses fewer twiddle factors (win)
- If butterfly dominates: Radix-4 has different computation pattern

**FFT32 Composite 4×FFT8**:
- Leverage FFT8's proven optimizations
- Inter-block twiddles need careful analysis

**FFT64 Radix-8**:
- Only 2 stages vs 6 for radix-2
- Twiddle factor savings significant

---

## Notes for Running Benchmarks

**System Requirements**:
- Quiet system (close other applications)
- At least 2GB RAM available
- Fixed CPU frequency (disable turbo boost if possible)
- Run multiple times to verify consistency

**Interpretation Tips**:
- Focus on relative comparisons, not absolute times
- Look for operations >25% of total time
- Consider variance (Error column in JMH output)
- Compare across different sizes for scaling behavior

**After Completing Benchmarks**:
1. Fill in actual numbers in tables above
2. Update "Expected Findings" with "Actual Findings"
3. Generate specific optimization recommendations
4. Update P1_IMPLEMENTATION_SUMMARY.md with key insights

---

## References

- [CONSENSUS_ANALYSIS.md](CONSENSUS_ANALYSIS.md) - P1 recommendation for profiling
- [JMH_BENCHMARKING_GUIDE.md](JMH_BENCHMARKING_GUIDE.md) - JMH methodology
- [OPTIMIZATION_LESSONS_LEARNED.md](OPTIMIZATION_LESSONS_LEARNED.md) - Controversial claims to validate

---

**Status**: ✅ Benchmark Suite Ready | ⏳ Results Pending
**Next Step**: Run profiling benchmarks and update this document with findings

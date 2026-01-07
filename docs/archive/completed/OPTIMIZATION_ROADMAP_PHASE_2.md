# FFT Optimization Roadmap - Phase 2 & Beyond
**Date**: January 7, 2026
**Status**: Ready for Implementation
**Approach**: Real measurement at every step (JMH benchmarks, not theoretical estimates)

---

## 🎯 Master Roadmap Summary

**Current State**:
- ✅ Optimization #1: System.arraycopy (2-3% measured)
- ✅ Optimization #2 Phase 1: Bit-reversal cache (expected 4-6%, NOT YET MEASURED)
- **Build Status**: 410/410 tests passing
- **Current Performance**: ~1.06-1.09x speedup (theoretical)

**Target**: 1.1-1.3x speedup with real, measured improvements

**Strategy**: Proceed phase-by-phase with **actual JMH measurements before and after each optimization**

---

## Phase 2: BASELINE MEASUREMENT
### Objective: Establish current performance as baseline

**Why This Matters**: We have theoretical improvements but NO actual JMH measurements for the bit-reversal cache. Need real data to:
1. Verify bit-reversal cache actually helps
2. Establish baseline for next optimizations
3. Confirm theoretical estimates match reality

**What to Do**:
```bash
# Run baseline JMH benchmark on current code (with both optimizations already in place)
# This measures: System.arraycopy (✅ already implemented) + Bit-reversal cache (✅ already implemented)
.\run-jmh-benchmarks.bat FFTBaseProfiling -f 3 -wi 10 -i 20

# Sizes to measure: 8, 16, 32, 64, 128, 256, 512, 1024, 2048
# Expected results to capture:
#  - FFT8 speedup (should be 2.27x from before)
#  - FFT16-128 speedup (should show System.arraycopy + Bit-reversal cache benefit)
#  - FFT256+ speedup (similar benefit but on larger data)
```

**Success Criteria**:
- ✅ JMH benchmarks complete without errors
- ✅ All sizes show performance data
- ✅ Bit-reversal cache shows >0% improvement (if not, investigate why)
- ✅ Overall speedup ≥1.05x (accounting for measurement variance)

**Deliverables**:
- Create `docs/performance/BASELINE_MEASUREMENT_JAN2026.md` with JMH results
- Document any variance or surprises
- Identify which optimizations contribute to total speedup

---

## Phase 3: BUTTERFLY OPERATIONS OPTIMIZATION
### Objective: 3-4% additional FFT speedup via butterfly optimization

**Target Metric**: 1.09-1.13x total speedup (combines Phases 1+2+3)

**Implementation Strategy** (TDD + Real Measurement):

### Step 1: RED - Create Butterfly Benchmark
**File**: `src/test/java/com/fft/core/ButterflyBenchmark.java` (new)
**What**: JMH benchmark isolating butterfly operations
**How**:
```java
@Benchmark
public void butterflyOperations_Current(Blackhole bh, ButterflyBenchmarkState state) {
    // Current implementation: 6 array accesses per butterfly
    for (int k = 0; k < state.n; k += state.n2) {
        double xr_k = state.xReal[k];      // Array access 1
        double xi_k = state.xImag[k];      // Array access 2
        double xr_kn2 = state.xReal[k + state.n2];  // Array access 3
        double xi_kn2 = state.xImag[k + state.n2];  // Array access 4
        double tReal = xr_kn2 * state.c + xi_kn2 * state.s;
        double tImag = xi_kn2 * state.c - xr_kn2 * state.s;
        state.xReal[k] = xr_k + tReal;     // Array access 5
        state.xImag[k] = xi_k + tImag;     // Array access 6
        state.xReal[k + state.n2] = xr_k - tReal;
        state.xImag[k + state.n2] = xi_k - tImag;
    }
    bh.consume(state.xReal);
    bh.consume(state.xImag);
}
```

**Benchmark Variants**:
- Current: Baseline (6 array accesses)
- Optimized: Cached approach (4 array accesses)
- Aggressive: Direct computation (no temporaries)

**Success Criteria**:
- ✅ Benchmark compiles without warnings
- ✅ Runs for all sizes 8-1024
- ✅ Shows baseline and optimized variants

### Step 2: GREEN - Implement Butterfly Optimization
**File**: `src/main/java/com/fft/core/FFTBase.java` (modify lines ~175-180)
**Strategy**: Array access reduction (conservative, proven approach)

**Current Code** (6 accesses):
```java
tReal = xReal[k + n2] * c + xImag[k + n2] * s;
tImag = xImag[k + n2] * c - xReal[k + n2] * s;
xReal[k + n2] = xReal[k] - tReal;
xImag[k + n2] = xImag[k] - tImag;
xReal[k] += tReal;
xImag[k] += tImag;
```

**Optimized Code** (4 accesses):
```java
double xr_k = xReal[k];
double xi_k = xImag[k];
double xr_kn2 = xReal[k + n2];
double xi_kn2 = xImag[k + n2];
double tReal = xr_kn2 * c + xi_kn2 * s;
double tImag = xi_kn2 * c - xr_kn2 * s;
xReal[k] = xr_k + tReal;
xImag[k] = xi_k + tImag;
xReal[k + n2] = xr_k - tReal;
xImag[k + n2] = xi_k - tImag;
```

**SOLID Compliance**:
- ✅ Single Responsibility: Still just butterfly computation
- ✅ Open/Closed: Internal optimization only
- ✅ Liskov Substitution: Same result, faster execution
- ✅ Interface Segregation: No interface changes
- ✅ Dependency Inversion: No new dependencies

### Step 3: REFACTOR - Verify and Measure
```bash
# 1. Verify correctness (all tests must pass)
mvn clean test
# Expected: 410/410 tests passing

# 2. Verify validation framework (stage-by-stage)
mvn test -Dtest=FFTValidationFrameworkTest

# 3. Measure actual improvement with JMH
.\run-jmh-benchmarks.bat FFTBaseProfiling -f 3 -wi 10 -i 20

# 4. Specifically measure butterfly benchmark
.\run-jmh-benchmarks.bat ButterflyBenchmark -f 3 -wi 10 -i 20
```

**Success Criteria**:
- ✅ All 410 tests passing
- ✅ Zero new failures
- ✅ JMH shows measurable improvement (>2% on butterfly is success)
- ✅ Overall FFT speedup measurable
- ✅ No regressions on any size

**Expected Results**:
- Butterfly operations: 15-25% faster (from 6→4 array accesses)
- Overall FFT: 2-4% faster (butterfly is 14.2% of total time, 0.25*0.142=3.5% potential)
- **Combined with previous**: 1.09-1.13x total speedup

**Measurement Documentation**:
- Create `docs/performance/BUTTERFLY_OPTIMIZATION_RESULTS.md`
- Record JMH data for all sizes
- Compare to baseline from Phase 2
- Calculate actual vs expected improvement

---

## Phase 4: DECIDE ON PHASE 2 PHASE 3
### Objective: If Phase 3 successful, continue with more optimizations

**What Comes After Butterfly?** (if we hit 1.1x target)

**Option A: Stop and Release** ✅
- Hit 1.1x target
- High-ROI optimizations complete
- Risk: diminishing returns on further work
- **Recommendation if speedup ≥1.1x**: Take the win

**Option B: Loop Unrolling** (if still <1.1x)
- **Location**: Inner FFT loops (small n2 values)
- **Expected gain**: 2-5% for small sizes
- **Risk**: May not help large sizes, increased code complexity
- **ROI**: Medium-low compared to previous optimizations

**Option C: Cache-Friendly Layout** (advanced)
- **Strategy**: Reorder computation to improve CPU cache hits
- **Expected gain**: 3-8% (highly architecture-dependent)
- **Risk**: Requires profiling on target hardware
- **Complexity**: High - requires significant refactoring

**Option D: SIMD/Vectorization** (advanced)
- **Strategy**: Use JVM's vector API for parallel operations
- **Expected gain**: 2-4x (but requires Java 19+, preview API)
- **Risk**: API stability, platform-dependent
- **Complexity**: Very high

**Decision Point**: After Phase 3, measure and decide:
- If ≥1.1x achieved → Release and document
- If <1.1x but ≥1.08x → Consider Phase 4 options
- If <1.08x → Investigate why; may need to revisit approach

---

## Phase 5: COMPARATIVE BENCHMARKING
### Objective: Compare against other FFT libraries

**If/When**: After achieving 1.1x+ speedup

**Libraries to Benchmark**:
1. **JTransforms** (popular pure Java FFT)
2. **Apache Commons Math** (general math library)
3. **FFTW-JNI** (JNI binding to optimized C library)

**Benchmark Suite**:
```java
// Measure FFT for sizes 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096
// Compare:
// - Our FFT vs each library
// - Raw performance
// - Accuracy (where applicable)
// - Memory usage
```

**Deliverables**:
- `docs/performance/COMPARATIVE_BENCHMARK_RESULTS.md`
- Performance comparison table
- Accuracy comparison (if applicable)
- Recommendations for users

---

## 📊 Measurement Strategy

### Before Any Optimization
1. **Establish Baseline**: Run JMH on current code
   ```bash
   .\run-jmh-benchmarks.bat FFTBaseProfiling -f 3 -wi 10 -i 20 > baseline_before.txt
   ```
2. **Record**: Save baseline numbers for all sizes

### After Implementation
1. **Run Same Benchmark**: Use identical parameters
   ```bash
   .\run-jmh-benchmarks.bat FFTBaseProfiling -f 3 -wi 10 -i 20 > baseline_after.txt
   ```
2. **Compare**: Calculate speedup for each size
   ```
   Speedup = Before_ns / After_ns
   % Improvement = (Speedup - 1) * 100
   ```

### Variance Handling
- **Accept**: ±5% variance from repeated runs (normal)
- **Investigate**: >15% variance (may indicate environmental factors)
- **Retry**: If variance >15%, run with more warmup iterations (-wi 20 or -wi 50)

### Documentation
- Always record:
  - Date, time, hardware (CPU model, RAM, JVM version)
  - JMH parameters (forks, warmup iterations, measured iterations)
  - Raw numbers (ns/op) for all sizes
  - Calculated speedups
  - Any anomalies or environmental factors

---

## ⚠️ Risk Mitigation

### Risk: Optimization doesn't help as expected
**Mitigation**:
- Real measurement catches this immediately
- Can revert and investigate (TDD catches regressions)
- Profiling data available to debug

### Risk: Optimization helps one size, hurts another
**Mitigation**:
- Benchmark all sizes (8-2048)
- Weighted average speedup across sizes
- Can create size-specific optimizations if needed

### Risk: Optimization introduces subtle bugs
**Mitigation**:
- Full test suite (410/410 tests)
- Validation framework (stage-by-stage verification)
- Correctness takes priority over speed

### Risk: Code becomes too complex
**Mitigation**:
- SOLID principles enforced at each step
- Code reviews for each optimization
- Clear, commented implementations
- If complexity exceeds benefit → revert

---

## 🔄 Execution Timeline

**When Ready to Start**:

### Day 1: Phase 2 (Baseline Measurement)
- [ ] Run JMH benchmark on current code
- [ ] Save baseline numbers
- [ ] Document in BASELINE_MEASUREMENT_JAN2026.md
- [ ] Estimate if we already hit 1.1x target

### Day 2-3: Phase 3 (Butterfly Optimization)
- [ ] Create ButterflyBenchmark.java (RED phase)
- [ ] Implement optimization in FFTBase.java (GREEN phase)
- [ ] Run full test suite (REFACTOR phase)
- [ ] Run JMH benchmark to measure improvement
- [ ] Document results in BUTTERFLY_OPTIMIZATION_RESULTS.md

### Decision Point
- If ≥1.1x speedup → Done! Release.
- If <1.1x but on track → Continue to Phase 4 options
- If disappointing results → Debug and investigate

---

## Summary: Why This Approach

✅ **Real Measurement**: JMH benchmarks, not theoretical estimates
✅ **Testable Improvements**: Before/after comparisons
✅ **Go/No-Go Gates**: Decide to continue based on real data
✅ **Risk Mitigation**: Full test suite catches regressions
✅ **Documented Decisions**: Clear reasons for each optimization
✅ **Reversible**: If an optimization doesn't help, easy to revert

---

**Ready to proceed? Let's start Phase 2 - run the baseline benchmark!**

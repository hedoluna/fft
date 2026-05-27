# Pitch Detection Accuracy & Performance Analysis

**Original investigation:** 2025-10-06
**Resolution verified:** 2026-05-27
**Test Suite:** `PitchDetectionAccuracyTest.java`
**Objective:** Evaluate accuracy and performance of pitch detection with base vs optimized FFT implementations

---

## Status: RESOLVED ✅

The October 2025 investigation (preserved below) found that the YIN algorithm
suffered a **40.6% mean error** on pure tones because its autocorrelation
function locked onto **subharmonics** (detecting 110 Hz instead of 440 Hz, etc.).

That defect has since been **fixed in code** by adding a *harmonic sieve* to
`PitchDetectionUtils.detectPitchYin` (commit `6bafe58`,
`applyHarmonicSieve`). The sieve scans from the shortest period upward and
selects the shortest `tau` whose cumulative-mean-normalized-difference (CMND) is
within an acceptable band of the global minimum, rejecting the longer
subharmonic periods.

**Re-measured evidence (2026-05-27):**

| Method | Mean Error (pure tones) | Behaviour |
|--------|------------------------|-----------|
| YIN (with harmonic sieve) | **0.827%** | No subharmonic errors; degrades under heavy noise |
| Spectral (FFTBase) | **0.922%** | Robust across all SNR levels |
| Spectral (FFTOptimized path) | **0.922%** | Identical to FFTBase (see note on FFT selection below) |

YIN and the spectral method are now **comparable on clean tones**. The library
keeps the **spectral method as primary** because it remains markedly more
**robust to noise** (YIN still fails at ≤5 dB SNR — see §4), and uses YIN as a
cross-check/validation pass. All the action items from the original
investigation have been implemented (see §7).

> **FFT-selection correction:** There is no `FFTOptimized4096`. Only
> `FFTOptimized8` and `FFTOptimized16` exist; every other size (including the
> 4096-point transform used here) falls back to `FFTBase` enriched with the
> universal `TwiddleFactorCache` / `BitReversalCache`. The "FFTBase vs
> FFTOptimized" columns in this document therefore exercise the *same*
> implementation for size 4096, which is why their results are byte-for-byte
> identical. The earlier "FFTOptimized4096 / 1.18x speedup" claims were
> inaccurate and have been removed.

---

## Executive Summary

**FINDINGS (current, post-fix):**
1. ✅ **YIN subharmonic defect is fixed** — harmonic sieve brings mean error from 40.6% down to **0.83%** on pure tones.
2. ✅ **Spectral method (FFT-based) remains the primary detector** — 0.92% mean error and superior noise robustness.
3. ✅ **FFT implementation does NOT affect accuracy** — for size 4096 both code paths resolve to `FFTBase` and produce identical results.
4. ✅ **Spectral method is faster than YIN** — FFT spectral analysis is O(N log N) vs YIN's O(N²) difference function.
5. ✅ **Recommended fixes are implemented** — demo and `detectPitchHybrid` use spectral as primary with YIN validation.

**CURRENT STRATEGY:** Spectral method is primary; YIN provides a subharmonic/octave cross-check. Results are averaged only when both methods agree within 5%.

---

## 1. Accuracy Analysis (re-measured 2026-05-27)

### Test Setup
- **Signal Type:** Pure sine waves with known frequencies
- **Frequency Range:** 82.41 Hz (E2) to 1318.51 Hz (E6)
- **Sample Rate:** 44100 Hz
- **FFT Size:** 4096 samples

### Results Summary

| Method | Mean Error | Max Error | Min Error | Mean Confidence |
|--------|-----------|-----------|-----------|----------------|
| YIN Algorithm (harmonic sieve) | **0.827%** | 1.057% | 0.260% | 0.763 |
| Spectral (FFTBase) | **0.922%** | 2.883% | 0.110% | 27.774 |
| Spectral (FFTOptimized path) | **0.922%** | 2.883% | 0.110% | 27.774 |

> Note: the YIN and spectral confidence scores are on different scales — YIN's
> confidence is a normalized [0,1] CMND-derived value, whereas the spectral
> "confidence" is the raw peak magnitude. They are not directly comparable.

### Detailed Frequency Analysis (current)

| Test Freq (Hz) | YIN Detected (Hz) | YIN Error | Spectral Detected (Hz) | Spectral Error |
|---------------|-------------------|-----------|------------------------|----------------|
| 82.41 | 81.60 | 0.977% | 84.79 | 2.883% |
| 110.00 | 108.84 | 1.056% | 108.03 | 1.794% |
| 146.83 | 145.28 | 1.057% | 149.23 | 1.636% |
| 196.00 | 194.05 | 0.996% | 194.11 | 0.964% |
| 246.94 | 244.82 | 0.859% | 247.62 | 0.273% |
| 329.63 | 326.83 | 0.849% | 332.01 | 0.721% |
| 440.00 | 435.60 | 0.999% | 441.33 | 0.301% |
| 659.25 | 653.61 | 0.856% | 657.17 | 0.316% |
| 987.77 | 985.21 | 0.260% | 990.00 | 0.226% |
| 1318.51 | 1313.81 | 0.356% | 1317.06 | 0.110% |

**Observation:** YIN now tracks the true fundamental at every tested frequency
(no more 1/2, 1/3, 1/4 subharmonic locks). It shows a small (~1%) consistent
*underestimate* on low/mid frequencies, attributable to period quantization and
the parabolic refinement — well within musical tolerance and far from the
previous octave-scale errors.

---

## 2. Performance Analysis

The spectral pipeline (FFT + peak picking) is consistently faster than YIN's
O(N²) difference function for a 4096-sample window. Exact ns/op figures vary
with JIT warmup and host load, so treat the numbers from a single in-process run
as indicative rather than authoritative; use the JMH harness for rigorous
measurement.

**Key Findings:**
- Spectral method (FFT-based) is faster than YIN for this window size.
- The performance gap comes from algorithmic complexity: O(N log N) spectral vs O(N²) YIN difference function.

---

## 3. Complex Waveform Analysis (re-measured 2026-05-27)

### Test: Harmonic-Rich Signals (Fundamental + 3 Harmonics)

| Fundamental | YIN Result | YIN Error | Spectral Result | Spectral Error |
|-------------|-----------|-----------|----------------|----------------|
| 110.0 Hz | 109.16 Hz | 0.763% | 107.89 Hz | 1.918% |
| 220.0 Hz | 218.20 Hz | 0.817% | 217.73 Hz | 1.032% |
| 440.0 Hz | 437.64 Hz | 0.536% | 441.30 Hz | 0.296% |

**Findings:**
- YIN now handles harmonic-rich signals correctly (≤0.8% error); the previous 50–75% subharmonic errors are gone.
- Spectral method continues to handle harmonics well (0.3–1.9% error).
- Both FFT code paths produce **identical results** for size 4096 (same underlying `FFTBase`).

---

## 4. Noise Tolerance Analysis (re-measured 2026-05-27)

### Test: Different SNR Levels with 440 Hz Sine Wave

| SNR (dB) | YIN Result | Spectral Result |
|----------|-----------|-----------------|
| 30 dB | 435.55 Hz ✓ | 441.33 Hz ✓ |
| 20 dB | 434.01 Hz ✓ | 441.33 Hz ✓ |
| 10 dB | **808.66 Hz ✗** | 441.34 Hz ✓ |
| 5 dB | **0.00 Hz (failed)** | 441.36 Hz ✓ |

**Findings:**
- With the harmonic sieve, YIN now reports the correct fundamental at moderate-to-high SNR (30/20 dB).
- YIN still degrades under heavy noise: it picks a spurious period at 10 dB and fails entirely at 5 dB.
- The **spectral method remains robust** across all SNR levels — this is the primary reason it is kept as the primary detector.

---

## 5. FFT Implementation Comparison

### Accuracy Impact: NONE

For the 4096-point transform, the factory has **no size-specific optimized
implementation** and returns `FFTBase`. Both the "FFTBase" and "optimized" code
paths in the test therefore run the same algorithm and yield identical results.

| Metric | FFTBase | "Optimized" path | Difference |
|--------|---------|------------------|------------|
| Mean Error | 0.922% | 0.922% | **0.000%** |
| Max Error | 2.883% | 2.883% | **0.000%** |
| Min Error | 0.110% | 0.110% | **0.000%** |

### Performance Impact

There is no dedicated `FFTOptimized4096`; size-specific unrolling exists only for
sizes 8 and 16 (`FFTOptimized8`, `FFTOptimized16`). For size 4096 the speedups
come from the **universal caches** (`TwiddleFactorCache`, `BitReversalCache`)
that `FFTBase` already uses, not from a separate optimized class. Any
"FFTBase vs optimized" timing difference observed for 4096 is measurement noise.

---

## 6. Current Implementation

### PitchDetectionDemo Strategy (current — correct)

```java
// From PitchDetectionDemo.processAudioStream()
if (isVoiced) {
    // Primary: spectral method (robust, accurate)
    pitchResult = detectPitch(spectrum);

    // Cross-check with YIN to catch subharmonic/octave issues
    PitchDetectionResult yinResult = detectPitchYin(audioSamples);
    if (pitchResult.frequency > 0 && yinResult.frequency > 0) {
        if (isSubharmonic(yinResult.frequency, pitchResult.frequency)) {
            // YIN locked a subharmonic -> trust spectral
        } else if (resultsAgree(pitchResult, yinResult, 0.05)) {
            // Both agree within 5% -> average for best accuracy
            ...
        }
        // Otherwise trust spectral (more robust)
    }
}
```

### PitchDetectionUtils.detectPitchHybrid Strategy (current — correct)

```java
// From PitchDetectionUtils.detectPitchHybrid()
// 1. Spectral method runs first (primary)
PitchResult spectralResult = detectPitchSpectral(spectrum, sampleRate);

// 2. YIN runs as validation
PitchResult yinResult = detectPitchYin(audioSamples, sampleRate);

// 3. Combine: reject YIN subharmonics, average on agreement, else prefer spectral
if (isSubharmonic(yinResult.frequency, spectralResult.frequency)) {
    finalResult = spectralResult;
} else if (resultsAgree(...)) {
    finalResult = average(...);
} else {
    finalResult = spectralResult;
}
```

Both methods now order spectral first and treat YIN as a validator — exactly the
strategy recommended by the original investigation.

---

## 7. Recommendations — Implementation Status

| # | Original recommendation | Status |
|---|-------------------------|--------|
| 1 | Make PitchDetectionDemo use spectral method as primary | ✅ Done (`processAudioStream`) |
| 2 | Fix `detectPitchHybrid` to call spectral first, YIN as validation | ✅ Done |
| 3 | Always use optimized FFT (factory auto-selection) | ✅ Factory selects best available per size |
| 4 | Add subharmonic detection | ✅ `isSubharmonic` + `applyHarmonicSieve` (in YIN itself) |
| 5 | Remove "YIN is more accurate" comments | ✅ Updated in demo and tests |

The most impactful additional fix beyond the original list was the **harmonic
sieve inside YIN itself** (`applyHarmonicSieve`), which repairs YIN at the
source rather than only working around it at the caller.

---

## 8. Conclusion

### Main Findings (current)

1. **The YIN subharmonic defect is fixed.** With the harmonic sieve, YIN tracks
   the true fundamental on both pure and harmonic-rich signals (≤~1% error),
   versus the 40.6% mean error documented in October 2025.

2. **The spectral method remains primary** — comparable accuracy on clean tones
   and clearly superior noise robustness (YIN still fails at ≤5 dB SNR).

3. **FFT implementation choice affects performance, not accuracy** — and for
   size 4096 there is no dedicated optimized class, so both code paths are
   `FFTBase` and yield identical numbers.

### Action Items — all complete

- [x] Update PitchDetectionDemo to use spectral method as primary
- [x] Fix `PitchDetectionUtils.detectPitchHybrid` logic
- [x] Add subharmonic detection validation (and an in-algorithm harmonic sieve)
- [x] Update comments (remove "YIN is more accurate" claims)
- [x] Align documentation (this file, README.md, CLAUDE.md)

### Long-Term Considerations

YIN's residual weakness is **noise robustness**, not subharmonics. For musical
instrument pitch detection on clean signals it is now accurate; for noisy input
the spectral method is preferred, which is why the hybrid strategy keeps spectral
as primary.

---

## 9. Test Reproducibility

All results can be reproduced by running:

```bash
mvn test -Dtest=PitchDetectionAccuracyTest
```

Test source: `src/test/java/com/fft/analysis/PitchDetectionAccuracyTest.java`

**Test Coverage:**
- ✓ Pure sine wave accuracy
- ✓ Complex harmonic signal accuracy
- ✓ Noise tolerance (SNR 30–5 dB)
- ✓ Performance comparison
- ✓ FFT implementation comparison

---

## Appendix A — Original Investigation (2025-10-06, pre-fix)

The following is the original analysis that motivated the fix. **It describes
the pre-fix behaviour and is retained for historical context only.** The numbers
below no longer reflect the current code (see §1–§5 above).

### YIN Algorithm Failure Pattern (pre-fix)

The YIN algorithm consistently detected **subharmonics** instead of the fundamental:
- 196 Hz → detected as 98 Hz (exactly **1/2**)
- 246.94 Hz → detected as 123.47 Hz (exactly **1/2**)
- 329.63 Hz → detected as 82.41 Hz (exactly **1/4**)
- 440 Hz → detected as 110 Hz (exactly **1/4**)
- 987.77 Hz → detected as 329.27 Hz (exactly **1/3**)
- 1318.51 Hz → detected as 146.50 Hz (approximately **1/9**)

**Root Cause:** YIN's autocorrelation function found strong correlations at
subharmonic periods, and the threshold-based selection preferred these incorrect
(longer) periods. This is a known limitation of autocorrelation-based pitch
detection on pure tones, and is exactly what the harmonic sieve now corrects.

### Pre-fix Accuracy Summary

| Method | Mean Error | Max Error | Min Error | Mean Confidence |
|--------|-----------|-----------|-----------|----------------|
| YIN Algorithm | **40.557%** | 88.889% | 0.000% | 1.000 |
| Spectral (FFTBase) | **0.922%** | 2.883% | 0.110% | 27.774 |

The misleadingly high (1.000) YIN confidence on wrong subharmonics was a key
symptom: callers could not rely on YIN's confidence score to reject bad
detections, which is why the original demo/hybrid logic that "trusted" YIN
confidence was incorrect.

---

**End of Analysis**

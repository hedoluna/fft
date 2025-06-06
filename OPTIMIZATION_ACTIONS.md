# Actionable Optimization Activities

This document lists concrete tasks to enhance the performance of the optimized FFT implementations.

## Context
The README notes that only sizes 8 and 32 offer verified performance gains and that larger sizes need further work:

```
1. **Fix optimized implementation correctness** (FFTOptimized32, FFTOptimized8 accuracy issues)
4. **Complete remaining optimizations** for sizes 64-65536
```
【F:README.md†L287-L295】

`OptimizedFFTUtils.fftRecursiveInternal` still uses `Math.cos` and `Math.sin` for large radix steps:

```
for (int j = 0; j < 4; j++) {
    double angle = constant * j * q * k / size;
    double cos = Math.cos(angle);
    double sin = Math.sin(angle);
    realSum += r[j][k] * cos - i[j][k] * sin;
    imagSum += r[j][k] * sin + i[j][k] * cos;
}
```
【F:src/main/java/com/fft/optimized/OptimizedFFTUtils.java†L652-L662】

## Tasks
1. **Precompute Twiddle Factors for All Sizes**
   - Extend the lookup tables (`TWIDDLES_*`) in `OptimizedFFTUtils` beyond size 64.
   - Replace direct `Math.cos`/`Math.sin` calls in `fftRecursiveInternal` with cached values.
2. **Adopt Split‑Radix Decomposition**
   - Implement a split‑radix algorithm in `fftRecursiveInternal` to reduce arithmetic operations.
   - Use the existing radix‑4 and radix‑2 code as a starting point.
3. **Vectorize Small Kernels**
   - Introduce Java Vector API usage for the 8, 16, 32, and 64‑point kernels.
   - Refactor loops to operate on vector lanes where possible.
4. **Introduce Memory Pools**
   - Create reusable buffers for temporary arrays to avoid frequent allocations inside recursive calls.
5. **Precompute Bit‑Reversal Tables**
   - Generate bit‑reversal lookups for all supported sizes to eliminate repeated calculation in bit‑reversal stages.
6. **Parallelize Large Transforms**
   - Split large transforms across threads using `ForkJoinPool` or parallel streams when `size >= 4096`.
7. **Ensure FMA‑Friendly Operations**
   - Restructure butterfly computations so the JIT can emit fused multiply-add instructions.
8. **Add Startup Auto‑Tuning**
   - Benchmark available implementations at library initialization and select the fastest for the current JVM.

Implementing these activities will move the codebase toward state‑of‑the‑art FFT performance while keeping the existing API intact.

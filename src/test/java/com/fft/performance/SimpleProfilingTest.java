package com.fft.performance;

import com.fft.core.FFTBase;

import org.junit.jupiter.api.Test;

/**
 * Simple profiling test to identify FFTBase bottlenecks.
 *
 * This is a fallback when JMH annotation processing isn't working.
 * Provides quick bottleneck identification without JMH complexity.
 */
public class SimpleProfilingTest {

    private static final int WARMUP_ITERATIONS = 1000;
    private static final int MEASUREMENT_ITERATIONS = 10000;

    @Test
    public void profileFFTOperations() {
        System.out.println("\n=== FFT Performance Profiling ===\n");

        for (int size : new int[] { 32, 64, 128, 256 }) {
            System.out.println("--- Size: " + size + " ---");
            profileSize(size);
            System.out.println();
        }
    }

    private void profileSize(int size) {
        double[] testReal = new double[size];
        double[] testImag = new double[size];

        // Initialize with sine wave
        for (int i = 0; i < size; i++) {
            testReal[i] = Math.sin(2 * Math.PI * 3 * i / size);
            testImag[i] = Math.cos(2 * Math.PI * 5 * i / size);
        }

        // 1. Full FFT Baseline
        long fullFFTTime = profileFullFFT(testReal, testImag);
        System.out.printf("Full FFT:            %,10d ns  (100%% baseline)\n", fullFFTTime);

        // 2. Twiddle Factor Calculation
        long twiddleTime = profileTwiddleFactors(size);
        double twiddlePercent = (twiddleTime * 100.0) / fullFFTTime;
        System.out.printf("Twiddle Factors:     %,10d ns  (%.1f%%)\n", twiddleTime, twiddlePercent);

        // 3. Precomputed Twiddles
        long precomputedTime = profilePrecomputedTwiddles(size);
        double precomputedPercent = (precomputedTime * 100.0) / fullFFTTime;
        System.out.printf("Precomputed Twiddles:%,10d ns  (%.1f%%)\n", precomputedTime, precomputedPercent);

        // 4. Bit-Reversal
        long bitReversalTime = profileBitReversal(size);
        double bitReversalPercent = (bitReversalTime * 100.0) / fullFFTTime;
        System.out.printf("Bit-Reversal:        %,10d ns  (%.1f%%)\n", bitReversalTime, bitReversalPercent);

        // 5. Butterfly Operations
        long butterflyTime = profileButterfly(testReal.clone(), testImag.clone());
        double butterflyPercent = (butterflyTime * 100.0) / fullFFTTime;
        System.out.printf("Butterfly Ops:       %,10d ns  (%.1f%%)\n", butterflyTime, butterflyPercent);

        // 6. Array Copying
        long manualCopyTime = profileManualArrayCopy(testReal);
        long systemCopyTime = profileSystemArrayCopy(testReal);
        long cloneTime = profileArrayClone(testReal);

        System.out.printf("Manual Copy:         %,10d ns\n", manualCopyTime);
        System.out.printf("System.arraycopy:    %,10d ns\n", systemCopyTime);
        System.out.printf("Array.clone:         %,10d ns\n", cloneTime);

        String copyWinner = manualCopyTime < systemCopyTime ? "Manual"
                : systemCopyTime < cloneTime ? "System.arraycopy" : "clone";
        System.out.printf("Copy Winner:         %s\n", copyWinner);
    }

    private long profileFullFFT(double[] real, double[] imag) {
        FFTBase fftBase = new FFTBase();

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            fftBase.transform(real.clone(), imag.clone(), true);
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            fftBase.transform(real.clone(), imag.clone(), true);
        }
        long end = System.nanoTime();

        return (end - start) / MEASUREMENT_ITERATIONS;
    }

    private long profileTwiddleFactors(int size) {
        int nu = log2(size);

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            computeTwiddles(size, nu);
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            computeTwiddles(size, nu);
        }
        long end = System.nanoTime();

        return (end - start) / MEASUREMENT_ITERATIONS;
    }

    private double computeTwiddles(int n, int nu) {
        double sum = 0.0;
        for (int l = 1; l <= nu; l++) {
            for (int k = 0; k < n; k += (1 << l)) {
                for (int i = 0; i < (1 << (l - 1)); i++) {
                    double p = bitreverseReference(k >> (nu - l), l);
                    double arg = -2 * Math.PI * p / n;
                    sum += Math.cos(arg) + Math.sin(arg);
                }
            }
        }
        return sum;
    }

    private long profilePrecomputedTwiddles(int size) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            precomputeTwiddles(size);
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            precomputeTwiddles(size);
        }
        long end = System.nanoTime();

        return (end - start) / MEASUREMENT_ITERATIONS;
    }

    private double precomputeTwiddles(int n) {
        double[] cosTable = new double[n];
        double[] sinTable = new double[n];

        for (int i = 0; i < n; i++) {
            double arg = -2 * Math.PI * i / n;
            cosTable[i] = Math.cos(arg);
            sinTable[i] = Math.sin(arg);
        }

        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += cosTable[i] + sinTable[i];
        }
        return sum;
    }

    private long profileBitReversal(int size) {
        int nu = log2(size);

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            doBitReversal(size, nu);
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            doBitReversal(size, nu);
        }
        long end = System.nanoTime();

        return (end - start) / MEASUREMENT_ITERATIONS;
    }

    private int doBitReversal(int n, int nu) {
        int sum = 0;
        for (int k = 0; k < n; k++) {
            sum += bitreverseReference(k, nu);
        }
        return sum;
    }

    private long profileButterfly(double[] re, double[] im) {
        int n2 = re.length / 2;

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            doButterfly(re.clone(), im.clone(), n2);
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            doButterfly(re.clone(), im.clone(), n2);
        }
        long end = System.nanoTime();

        return (end - start) / MEASUREMENT_ITERATIONS;
    }

    private void doButterfly(double[] re, double[] im, int n2) {
        for (int k = 0; k < n2; k++) {
            double tRe = re[k + n2];
            double tIm = im[k + n2];
            re[k + n2] = re[k] - tRe;
            im[k + n2] = im[k] - tIm;
            re[k] += tRe;
            im[k] += tIm;
        }
    }

    private long profileManualArrayCopy(double[] source) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            double[] dest = new double[source.length];
            for (int j = 0; j < source.length; j++) {
                dest[j] = source[j];
            }
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            double[] dest = new double[source.length];
            for (int j = 0; j < source.length; j++) {
                dest[j] = source[j];
            }
        }
        long end = System.nanoTime();

        return (end - start) / MEASUREMENT_ITERATIONS;
    }

    private long profileSystemArrayCopy(double[] source) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            double[] dest = new double[source.length];
            System.arraycopy(source, 0, dest, 0, source.length);
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            double[] dest = new double[source.length];
            System.arraycopy(source, 0, dest, 0, source.length);
        }
        long end = System.nanoTime();

        return (end - start) / MEASUREMENT_ITERATIONS;
    }

    private long profileArrayClone(double[] source) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            double[] dest = source.clone();
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            double[] dest = source.clone();
        }
        long end = System.nanoTime();

        return (end - start) / MEASUREMENT_ITERATIONS;
    }

    private static int bitreverseReference(int j, int nu) {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= nu; i++) {
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
        }
        return k;
    }

    private static int log2(int n) {
        int log = 0;
        int temp = n;
        while (temp > 1) {
            temp >>= 1;
            log++;
        }
        return log;
    }

    @Test
    public void verifyFFT8Performance() {
        System.out.println("\n=== FFT8 Performance Verification ===\n");

        double[] real = new double[8];
        double[] imag = new double[8];
        for (int i = 0; i < 8; i++) {
            real[i] = Math.sin(2 * Math.PI * i / 8);
            imag[i] = Math.cos(2 * Math.PI * i / 8);
        }

        FFTBase base = new FFTBase();
        com.fft.optimized.FFTOptimized8 optimized = new com.fft.optimized.FFTOptimized8();

        // Heavy warmup (10x more than standard)
        for (int i = 0; i < 10000; i++) {
            base.transform(real.clone(), imag.clone(), true);
            optimized.transform(real.clone(), imag.clone(), true);
        }

        // Benchmark base implementation
        long baseStart = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            base.transform(real.clone(), imag.clone(), true);
        }
        long baseTime = (System.nanoTime() - baseStart) / MEASUREMENT_ITERATIONS;

        // Benchmark optimized implementation
        long optStart = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            optimized.transform(real.clone(), imag.clone(), true);
        }
        long optTime = (System.nanoTime() - optStart) / MEASUREMENT_ITERATIONS;

        double speedup = (double) baseTime / optTime;

        System.out.printf("FFTBase:         %,10d ns\n", baseTime);
        System.out.printf("FFTOptimized8:   %,10d ns\n", optTime);
        System.out.printf("Speedup:         %.2fx\n", speedup);
        System.out.printf("Expected:        2.0-3.5x\n");

        if (speedup >= 2.0) {
            System.out.println("Status:          ✅ EXCELLENT - Optimization working as expected");
        } else if (speedup >= 1.5) {
            System.out.println("Status:          ⚠️  GOOD - Optimization present but below target");
        } else if (speedup >= 1.0) {
            System.out.println("Status:          ⚠️  WEAK - Minimal speedup detected");
        } else {
            System.out.println("Status:          ❌ REGRESSION - Slower than base!");
        }

        System.out.println("\nNote: This test uses heavy warmup (10,000 iterations) to ensure");
        System.out.println("      JIT compilation is complete before measurement.");
    }

    @Test
    public void measureTwiddleCacheImpact() {
        System.out.println("\n=== Twiddle Cache Performance Impact ===\n");

        System.out.println("Profiling Prediction:");
        System.out.println("  - Twiddle factors: 43-56% of FFT time");
        System.out.println("  - Precomputation speedup: 2.3-3.2x for twiddle operations");
        System.out.println("  - Expected overall improvement: 30-50%\n");

        for (int size : new int[] { 32, 64, 128, 256 }) {
            double[] real = new double[size];
            double[] imag = new double[size];
            for (int i = 0; i < size; i++) {
                real[i] = Math.sin(2 * Math.PI * 3 * i / size);
                imag[i] = Math.cos(2 * Math.PI * 5 * i / size);
            }

            FFTBase fftBase = new FFTBase();

            // Warmup
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                fftBase.transform(real.clone(), imag.clone(), true);
            }

            // Measure
            long start = System.nanoTime();
            for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                fftBase.transform(real.clone(), imag.clone(), true);
            }
            long time = (System.nanoTime() - start) / MEASUREMENT_ITERATIONS;

            System.out.printf("Size %d:  %,10d ns  (with twiddle cache)\n", size, time);
        }

        System.out.println("\nCache Status: " + com.fft.core.TwiddleFactorCache.getCacheStats());
        System.out.println("\nNote: FFTBase now uses precomputed twiddle factors for sizes 8-4096.");
        System.out.println("      Compare with profiling-results.txt baseline (before optimization).");
    }
}

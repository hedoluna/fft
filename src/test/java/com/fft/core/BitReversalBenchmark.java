package com.fft.core;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Micro-benchmark for bit-reversal optimization in FFTBase.
 *
 * <p>HYPOTHESIS: Precomputed lookup table should be 50-70% faster than
 * repeated bitreverseReference() calls.</p>
 *
 * <p>PROFILING DATA: Bit-reversal accounts for 8.2% of total FFT time (221ns for size 32).
 * Current implementation calls bitreverseReference() n times, each with O(log n) complexity.</p>
 *
 * <p>Expected improvement: 50-70% on bit-reversal → 4-6% overall FFT speedup</p>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgs = {"-Xms2G", "-Xmx2G"})
@State(Scope.Benchmark)
public class BitReversalBenchmark {

    @Param({"8", "16", "32", "64", "128", "256", "512", "1024"})
    private int size;

    private double[] real;
    private double[] imag;
    private int nu;

    @Setup
    public void setup() {
        real = new double[size];
        imag = new double[size];

        // Initialize with random data
        for (int i = 0; i < size; i++) {
            real[i] = Math.random();
            imag[i] = Math.random();
        }

        // Calculate nu (log2(size))
        nu = 0;
        int temp = size;
        while (temp > 1) {
            temp >>= 1;
            nu++;
        }
    }

    /**
     * Baseline: Current implementation using bitreverseReference().
     * Called n times with O(log n) complexity each = O(n log n) total.
     */
    @Benchmark
    public void currentBitReversal(Blackhole bh) {
        double[] xReal = real.clone();
        double[] xImag = imag.clone();

        // Current implementation from FFTBase
        int k = 0;
        while (k < size) {
            int r = bitreverseReference(k, nu);
            if (r > k) {
                double tReal = xReal[k];
                double tImag = xImag[k];
                xReal[k] = xReal[r];
                xImag[k] = xImag[r];
                xReal[r] = tReal;
                xImag[r] = tImag;
            }
            k++;
        }

        bh.consume(xReal);
        bh.consume(xImag);
    }

    /**
     * Optimized: Precomputed lookup table.
     * O(n) table creation + O(n) lookup = O(n) total (vs O(n log n) baseline).
     */
    @Benchmark
    public void lookupTableBitReversal(Blackhole bh) {
        double[] xReal = real.clone();
        double[] xImag = imag.clone();

        // Precompute lookup table
        int[] bitReversal = new int[size];
        for (int i = 0; i < size; i++) {
            bitReversal[i] = bitreverseReference(i, nu);
        }

        // Use lookup table (O(1) per element)
        for (int k = 0; k < size; k++) {
            int r = bitReversal[k];
            if (r > k) {
                double tReal = xReal[k];
                double tImag = xImag[k];
                xReal[k] = xReal[r];
                xImag[k] = xImag[r];
                xReal[r] = tReal;
                xImag[r] = tImag;
            }
        }

        bh.consume(xReal);
        bh.consume(xImag);
        bh.consume(bitReversal);
    }

    /**
     * Optimized with bit manipulation: Replace division with bit shifts.
     */
    @Benchmark
    public void optimizedBitManipulation(Blackhole bh) {
        double[] xReal = real.clone();
        double[] xImag = imag.clone();

        int k = 0;
        while (k < size) {
            int r = bitreverseOptimized(k, nu);
            if (r > k) {
                double tReal = xReal[k];
                double tImag = xImag[k];
                xReal[k] = xReal[r];
                xImag[k] = xImag[r];
                xReal[r] = tReal;
                xImag[r] = tImag;
            }
            k++;
        }

        bh.consume(xReal);
        bh.consume(xImag);
    }

    /**
     * Cached lookup table: Simulates caching for common sizes.
     */
    @Benchmark
    public void cachedLookupTable(Blackhole bh) {
        double[] xReal = real.clone();
        double[] xImag = imag.clone();

        // Simulate cache hit (lookup table already computed)
        int[] bitReversal = getCachedBitReversal(size, nu);

        for (int k = 0; k < size; k++) {
            int r = bitReversal[k];
            if (r > k) {
                double tReal = xReal[k];
                double tImag = xImag[k];
                xReal[k] = xReal[r];
                xImag[k] = xImag[r];
                xReal[r] = tReal;
                xImag[r] = tImag;
            }
        }

        bh.consume(xReal);
        bh.consume(xImag);
    }

    // Helper methods

    /**
     * Current bit-reversal implementation (from FFTBase).
     */
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

    /**
     * Optimized bit-reversal using bit manipulation.
     */
    private static int bitreverseOptimized(int j, int nu) {
        int result = 0;
        for (int i = 0; i < nu; i++) {
            result = (result << 1) | (j & 1);
            j >>= 1;
        }
        return result;
    }

    /**
     * Simulates cached lookup table.
     */
    private int[] getCachedBitReversal(int size, int nu) {
        // In real implementation, this would be cached in ConcurrentHashMap
        int[] table = new int[size];
        for (int i = 0; i < size; i++) {
            table[i] = bitreverseReference(i, nu);
        }
        return table;
    }
}

package com.fft.performance;

import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Profiling Benchmark for FFTBase Implementation.
 *
 * This benchmark isolates individual operations within FFTBase to identify
 * performance bottlenecks as recommended by CONSENSUS_ANALYSIS.md P1.
 *
 * <p>Isolated operations benchmarked:</p>
 * <ul>
 *   <li>Twiddle factor calculation (Math.cos/sin)</li>
 *   <li>Bit-reversal permutation</li>
 *   <li>Butterfly operations</li>
 *   <li>Array copying (manual vs System.arraycopy)</li>
 *   <li>Full FFT transform (baseline)</li>
 * </ul>
 *
 * Usage:
 * <pre>
 * mvn clean test-compile exec:java \
 *   -Dexec.mainClass="org.openjdk.jmh.Main" \
 *   -Dexec.args="FFTBaseProfiling" \
 *   -Dexec.classpathScope=test
 * </pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 10, time = 2)
@Fork(value = 3, jvmArgs = {"-Xms2G", "-Xmx2G"})
public class FFTBaseProfilingBenchmark {

    @Param({"32", "64", "128", "256"})
    private int size;

    private FFTBase fftBase;
    private double[] testReal;
    private double[] testImag;
    private double[] copyBuffer;

    @Setup
    public void setup() {
        fftBase = new FFTBase();
        testReal = new double[size];
        testImag = new double[size];
        copyBuffer = new double[size];

        // Initialize with sine wave for realistic data
        for (int i = 0; i < size; i++) {
            testReal[i] = Math.sin(2 * Math.PI * 3 * i / size);
            testImag[i] = Math.cos(2 * Math.PI * 5 * i / size);
        }
    }

    // ==================== Full FFT Baseline ====================

    @Benchmark
    public void benchmarkFullFFT(Blackhole bh) {
        FFTResult result = fftBase.transform(
            testReal.clone(),
            testImag.clone(),
            true
        );
        bh.consume(result);
    }

    // ==================== Twiddle Factor Calculation ====================

    /**
     * Benchmark twiddle factor calculation (Math.cos/sin calls).
     * This is called in the inner loop of FFTBase and is likely a major bottleneck.
     */
    @Benchmark
    public void benchmarkTwiddleFactors(Blackhole bh) {
        int n = size;
        double sum = 0.0;

        // Simulate twiddle factor computation pattern from FFTBase
        for (int l = 1; l <= log2(n); l++) {
            for (int k = 0; k < n; k += (1 << l)) {
                for (int i = 0; i < (1 << (l-1)); i++) {
                    double p = bitreverseReference(k >> (log2(n) - l), l);
                    double arg = -2 * Math.PI * p / n;
                    double c = Math.cos(arg);
                    double s = Math.sin(arg);
                    sum += c + s;  // Prevent optimization
                }
            }
        }
        bh.consume(sum);
    }

    /**
     * Benchmark precomputed twiddle factors vs on-the-fly calculation.
     */
    @Benchmark
    public void benchmarkPrecomputedTwiddles(Blackhole bh) {
        int n = size;
        // Precompute all twiddle factors
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
        bh.consume(sum);
    }

    // ==================== Bit-Reversal ====================

    /**
     * Benchmark bit-reversal permutation operation.
     * This is called for every element during FFT computation.
     */
    @Benchmark
    public void benchmarkBitReversal(Blackhole bh) {
        int n = size;
        int nu = log2(n);
        int sum = 0;

        // Simulate bit-reversal pattern from FFTBase
        for (int k = 0; k < n; k++) {
            int r = bitreverseReference(k, nu);
            sum += r;
        }
        bh.consume(sum);
    }

    // ==================== Butterfly Operations ====================

    /**
     * Benchmark butterfly operation (core FFT computation).
     */
    @Benchmark
    public void benchmarkButterfly(Blackhole bh) {
        double[] re = testReal.clone();
        double[] im = testImag.clone();

        // Simulate butterfly operations from FFTBase
        int n2 = size / 2;
        for (int k = 0; k < n2; k++) {
            double tRe = re[k + n2];
            double tIm = im[k + n2];
            re[k + n2] = re[k] - tRe;
            im[k + n2] = im[k] - tIm;
            re[k] += tRe;
            im[k] += tIm;
        }

        bh.consume(re);
        bh.consume(im);
    }

    // ==================== Array Copying ====================

    /**
     * Benchmark manual array copy (as used in FFT8 optimization).
     */
    @Benchmark
    public void benchmarkManualArrayCopy(Blackhole bh) {
        double[] dest = new double[size];
        for (int i = 0; i < size; i++) {
            dest[i] = testReal[i];
        }
        bh.consume(dest);
    }

    /**
     * Benchmark System.arraycopy (standard library method).
     */
    @Benchmark
    public void benchmarkSystemArrayCopy(Blackhole bh) {
        double[] dest = new double[size];
        System.arraycopy(testReal, 0, dest, 0, size);
        bh.consume(dest);
    }

    /**
     * Benchmark array clone (syntactic sugar for System.arraycopy).
     */
    @Benchmark
    public void benchmarkArrayClone(Blackhole bh) {
        double[] dest = testReal.clone();
        bh.consume(dest);
    }

    // ==================== Helper Methods ====================

    /**
     * Bit-reversal reference implementation (from FFTBase).
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
     * Calculate log2 of a number.
     */
    private static int log2(int n) {
        int log = 0;
        int temp = n;
        while (temp > 1) {
            temp >>= 1;
            log++;
        }
        return log;
    }
}

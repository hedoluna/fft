package com.fft.optimized;

import com.fft.core.FFT;
import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import com.fft.factory.FFTFactory;
import com.fft.factory.DefaultFFTFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.*;

/**
 * Performance comparison tests to measure optimization effectiveness.
 */
public class PerformanceComparisonTest {

    private static final int WARMUP = 1000;
    private static final int MEASURE_REPEATS = 5;

    private FFTFactory factory;

    // Consumes transform output so the JIT cannot eliminate the benchmarked call as dead code.
    @SuppressWarnings("unused")
    private volatile double sink;

    @BeforeEach
    void setUp() {
        factory = new DefaultFFTFactory();
    }

    /**
     * Returns the best (minimum) wall-clock time over several batches of {@code iterations}
     * transforms. Taking the fastest batch rejects GC/scheduling outliers, so the
     * base-vs-optimized ratio is stable instead of flaking under host load. The transform
     * output is accumulated into a volatile sink so the JIT cannot dead-code-eliminate the
     * call. These are coarse sanity checks; use the JMH harness for rigorous benchmarking.
     */
    private long bestTransformNanos(FFT fft, double[] real, double[] imag, int iterations) {
        double local = 0.0;
        for (int i = 0; i < WARMUP; i++) {
            local += fft.transform(real, imag, true).getRealAt(0);
        }
        long best = Long.MAX_VALUE;
        for (int rep = 0; rep < MEASURE_REPEATS; rep++) {
            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                local += fft.transform(real, imag, true).getRealAt(0);
            }
            best = Math.min(best, System.nanoTime() - start);
        }
        sink = local;
        return best;
    }
    
    @Test
    void compareFFT8Performance() {
        double[] real = generateTestSignal(8);
        double[] imag = new double[8];
        FFTBase base = new FFTBase();
        FFT optimized = factory.createFFT(8);

        long baseTime = bestTransformNanos(base, real, imag, 10000);
        long optimizedTime = bestTransformNanos(optimized, real, imag, 10000);

        double speedup = (double) baseTime / optimizedTime;
        System.out.printf("FFT Size 8 - Base: %,d ns, Optimized: %,d ns, Speedup: %.2fx%n",
                         baseTime, optimizedTime, speedup);

        // Conservative regression guard: the size-8 optimized impl must beat FFTBase.
        // This is a coarse in-JVM check, not the canonical speedup figure (use JMH for that).
        assertThat(speedup).isGreaterThan(1.1);
    }
    
    @Test
    void compareFFT16Performance() {
        double[] real = generateTestSignal(16);
        double[] imag = new double[16];
        FFTBase base = new FFTBase();
        FFT optimized = factory.createFFT(16);

        long baseTime = bestTransformNanos(base, real, imag, 10000);
        long optimizedTime = bestTransformNanos(optimized, real, imag, 10000);

        double speedup = (double) baseTime / optimizedTime;
        System.out.printf("FFT Size 16 - Base: %,d ns, Optimized: %,d ns, Speedup: %.2fx%n",
                         baseTime, optimizedTime, speedup);

        // Conservative regression guard: the size-16 optimized impl must beat FFTBase.
        // Coarse in-JVM check, not the canonical speedup figure (use JMH for that).
        assertThat(speedup).isGreaterThan(1.1);
    }
    
    @Test
    void fft32UsesBaseFallback() {
        // No size-32 optimized implementation exists, so factory.createFFT(32) returns the
        // FFTBase fallback. A "base vs optimized" speedup would compare FFTBase to itself and
        // measure nothing, so we assert the fallback instead. (Output correctness for size 32
        // is covered by validateCorrectness.)
        assertThat(factory.createFFT(32)).isInstanceOf(FFTBase.class);
    }

    @Test
    void fft64UsesBaseFallback() {
        // No size-64 optimized implementation exists: factory.createFFT(64) falls back to
        // FFTBase, so there is no speedup to measure here.
        assertThat(factory.createFFT(64)).isInstanceOf(FFTBase.class);
    }
    
    @Test
    void validateCorrectness() {
        // Ensure optimizations don't break correctness
        for (int size : new int[]{8, 16, 32, 64}) {
            double[] real = generateTestSignal(size);
            double[] imag = new double[size];
            
            FFTResult baseResult = new FFTBase().transform(real, imag, true);
            FFTResult optimizedResult = factory.createFFT(size).transform(real, imag, true);
            
            // Check that results are equivalent
            double[] baseReal = baseResult.getRealParts();
            double[] baseImag = baseResult.getImaginaryParts();
            double[] optReal = optimizedResult.getRealParts();
            double[] optImag = optimizedResult.getImaginaryParts();
            
            for (int i = 0; i < size; i++) {
                assertThat(optReal[i]).isCloseTo(baseReal[i], within(3.0));
                assertThat(optImag[i]).isCloseTo(baseImag[i], within(3.0));
            }
        }
    }
    
    private double[] generateTestSignal(int size) {
        double[] signal = new double[size];
        for (int i = 0; i < size; i++) {
            signal[i] = Math.sin(2.0 * Math.PI * i / size) + 0.5 * Math.cos(4.0 * Math.PI * i / size);
        }
        return signal;
    }
}

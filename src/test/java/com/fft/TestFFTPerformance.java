package com.fft;

import com.fft.core.FFT;
import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import com.fft.factory.DefaultFFTFactory;
import com.fft.optimized.OptimizedFFTUtils; // For direct test of fftRecursive

import org.junit.Test;

import java.util.Arrays; // Required for Arrays.asList
import java.util.List;
import java.util.Random;

public class TestFFTPerformance {

    private static final int WARMUP_ITERATIONS = 100;
    private static final int BENCHMARK_ITERATIONS = 500;
    private static DefaultFFTFactory factory = new DefaultFFTFactory();

    private double[] generateRandomSignal(int size, long seed) {
        Random random = new Random(seed);
        double[] signal = new double[size];
        for (int i = 0; i < size; i++) {
            signal[i] = random.nextGaussian();
        }
        return signal;
    }

    private void benchmarkFFT(String name, FFT fft, int size, boolean isRecursiveWithError) {
        if (!fft.supportsSize(size) && !(name.contains("Recursive") || name.contains("Base"))) {
            System.out.println(String.format(
                    "Skipping benchmark for %s with size %d (not supported by this specific impl)", name, size));
            return;
        }
        if (size == 0)
            return;

        double[] real = generateRandomSignal(size, (long) size * 30 + 1);
        double[] imag = generateRandomSignal(size, (long) size * 30 + 2);

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            fft.transform(real, imag, true);
        }

        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            fft.transform(real, imag, true);
        }
        long endTime = System.nanoTime();

        long durationNanos = (endTime - startTime);
        double durationMillis = durationNanos / 1_000_000.0;
        double opsPerSecond = (BENCHMARK_ITERATIONS / (durationNanos / 1_000_000_000.0));

        System.out.println(String.format("Benchmark: %s (N=%d)%s - Total Time: %.2f ms, Ops/sec: %.2f",
                name, size, (isRecursiveWithError ? " [KNOWN INCORRECT]" : ""), durationMillis, opsPerSecond));
    }

    private FFT createRecursiveWrapper() {
        return new FFT() {
            @Override
            public FFTResult transform(double[] real, double[] imaginary, boolean forward) {
                double[] result = OptimizedFFTUtils.fftRecursive(real.length, real, imaginary, forward);
                return new FFTResult(result);
            }

            @Override
            public FFTResult transform(double[] real, boolean forward) {
                return transform(real, new double[real.length], forward);
            }

            @Override
            public int getSupportedSize() {
                return -1;
            }

            @Override
            public boolean supportsSize(int s) {
                return (s > 0 && (s & (s - 1)) == 0 && s >= 8);
            }

            @Override
            public String getDescription() {
                return "OptimizedFFTUtils.fftRecursive wrapper";
            }
        };
    }

    @Test
    public void benchmarkAll() {
        System.out.println("Starting FFT Benchmarks...");
        List<Integer> sizesToTest = Arrays.asList(8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096);

        FFT fftBase = new FFTBase();
        FFT fftRecursiveWrapper = createRecursiveWrapper();

        for (int size : sizesToTest) {
            try { // Wrap FFTBase in try-catch as it throws for non-power-of-2, though sizesToTest
                  // are powers of 2
                benchmarkFFT("FFTBase", fftBase, size, false);
            } catch (IllegalArgumentException e) {
                System.out.println(String.format("Skipping FFTBase for size %d due to: %s", size, e.getMessage()));
            }

            // Test optimized versions obtained from factory
            // FFTOptimized classes will be chosen by the factory for relevant sizes (8, 16,
            // 32, 64)
            // For larger sizes, factory might return FFTBase or a future different
            // optimized version
            FFT factoryFFT = factory.createFFT(size); // This can throw if size not supported by any specific impl and
                                                      // no fallback
            if (factoryFFT != null) {
                String factoryFFTName = factory.getImplementationInfo(size);
                if (factoryFFTName == null || factoryFFTName.isEmpty()) {
                    factoryFFTName = factoryFFT.getClass().getSimpleName(); // Fallback name
                } else {
                    factoryFFTName = factoryFFTName.split(" ")[0]; // Get class name like "FFTOptimized32" or "FFTBase"
                }

                if (factoryFFT != fftBase || !factoryFFTName.equals("FFTBase")) { // Avoid double testing if factory
                                                                                  // falls back to base
                    benchmarkFFT(factoryFFTName, factoryFFT, size, false);
                }
            } else {
                System.out.println(String.format("No FFT implementation found by factory for size %d", size));
            }

            // Explicitly benchmark the fftRecursive wrapper
            // It is known to be incorrect for N >= 128 due to normalization issues.
            if (size >= 8) { // fftRecursive supports N>=8
                benchmarkFFT("OptimizedFFTUtils.fftRecursive", fftRecursiveWrapper, size, size >= 128);
            }
        }
        System.out.println("FFT Benchmarks Complete.");
    }
}

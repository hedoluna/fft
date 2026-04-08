package com.fft;

import com.fft.core.FFT;
import com.fft.core.FFTBase;
import com.fft.factory.DefaultFFTFactory;
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

    @Test
    public void benchmarkAll() {
        System.out.println("Starting FFT Benchmarks...");
        List<Integer> sizesToTest = Arrays.asList(8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096);

        FFT fftBase = new FFTBase();

        for (int size : sizesToTest) {
            benchmarkFFT("FFTBase", fftBase, size, false);

            FFT factoryFFT = factory.createFFT(size);
            if (factoryFFT != null) {
                String factoryFFTName = factory.getImplementationInfo(size);
                if (factoryFFTName == null || factoryFFTName.isEmpty()) {
                    factoryFFTName = factoryFFT.getClass().getSimpleName();
                } else {
                    factoryFFTName = factoryFFTName.split(" ")[0];
                }

                if (factoryFFT != fftBase || !factoryFFTName.equals("FFTBase")) {
                    benchmarkFFT(factoryFFTName, factoryFFT, size, false);
                }
            }
        }
        System.out.println("FFT Benchmarks Complete.");
    }
}

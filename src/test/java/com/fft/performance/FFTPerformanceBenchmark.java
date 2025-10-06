package com.fft.performance;

import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import com.fft.optimized.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * JMH Performance Benchmark for FFT implementations.
 *
 * This benchmark provides statistically rigorous performance measurements
 * addressing the variance issues identified in CONSENSUS_ANALYSIS.md.
 *
 * Usage:
 * mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" \
 *     -Dexec.classpathScope=test
 *
 * Or run specific benchmarks:
 * mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" \
 *     -Dexec.args="FFT8" -Dexec.classpathScope=test
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 2)  // 5 warmup iterations, 2 seconds each
@Measurement(iterations = 10, time = 2)  // 10 measurement iterations, 2 seconds each
@Fork(value = 3, jvmArgs = {"-Xms2G", "-Xmx2G"})  // 3 forks for statistical confidence
public class FFTPerformanceBenchmark {

    private FFTBase fftBase;

    // Optimized implementations
    private FFTOptimized8 optimized8;
    private FFTOptimized16 optimized16;
    private FFTOptimized32 optimized32;
    private FFTOptimized64 optimized64;
    private FFTOptimized128 optimized128;

    // Test data for each size
    private double[] testData8;
    private double[] testImag8;

    private double[] testData16;
    private double[] testImag16;

    private double[] testData32;
    private double[] testImag32;

    private double[] testData64;
    private double[] testImag64;

    private double[] testData128;
    private double[] testImag128;

    @Setup
    public void setup() {
        fftBase = new FFTBase();

        optimized8 = new FFTOptimized8();
        optimized16 = new FFTOptimized16();
        optimized32 = new FFTOptimized32();
        optimized64 = new FFTOptimized64();
        optimized128 = new FFTOptimized128();

        // Initialize test data with sine wave patterns
        testData8 = new double[8];
        testImag8 = new double[8];
        for (int i = 0; i < 8; i++) {
            testData8[i] = Math.sin(2 * Math.PI * i / 8);
        }

        testData16 = new double[16];
        testImag16 = new double[16];
        for (int i = 0; i < 16; i++) {
            testData16[i] = Math.sin(2 * Math.PI * i / 16);
        }

        testData32 = new double[32];
        testImag32 = new double[32];
        for (int i = 0; i < 32; i++) {
            testData32[i] = Math.cos(2 * Math.PI * i / 32);
        }

        testData64 = new double[64];
        testImag64 = new double[64];
        for (int i = 0; i < 64; i++) {
            testData64[i] = Math.sin(2 * Math.PI * 3 * i / 64);
        }

        testData128 = new double[128];
        testImag128 = new double[128];
        for (int i = 0; i < 128; i++) {
            testData128[i] = Math.cos(2 * Math.PI * 5 * i / 128);
        }
    }

    // ==================== FFT8 Benchmarks ====================

    @Benchmark
    public void benchmarkFFT8_Optimized(Blackhole bh) {
        FFTResult result = optimized8.transform(
            testData8.clone(),
            testImag8.clone(),
            true
        );
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkFFT8_Base(Blackhole bh) {
        FFTResult result = fftBase.transform(
            testData8.clone(),
            testImag8.clone(),
            true
        );
        bh.consume(result);
    }

    // ==================== FFT16 Benchmarks ====================

    @Benchmark
    public void benchmarkFFT16_Optimized(Blackhole bh) {
        FFTResult result = optimized16.transform(
            testData16.clone(),
            testImag16.clone(),
            true
        );
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkFFT16_Base(Blackhole bh) {
        FFTResult result = fftBase.transform(
            testData16.clone(),
            testImag16.clone(),
            true
        );
        bh.consume(result);
    }

    // ==================== FFT32 Benchmarks ====================

    @Benchmark
    public void benchmarkFFT32_Optimized(Blackhole bh) {
        FFTResult result = optimized32.transform(
            testData32.clone(),
            testImag32.clone(),
            true
        );
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkFFT32_Base(Blackhole bh) {
        FFTResult result = fftBase.transform(
            testData32.clone(),
            testImag32.clone(),
            true
        );
        bh.consume(result);
    }

    // ==================== FFT64 Benchmarks ====================

    @Benchmark
    public void benchmarkFFT64_Optimized(Blackhole bh) {
        FFTResult result = optimized64.transform(
            testData64.clone(),
            testImag64.clone(),
            true
        );
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkFFT64_Base(Blackhole bh) {
        FFTResult result = fftBase.transform(
            testData64.clone(),
            testImag64.clone(),
            true
        );
        bh.consume(result);
    }

    // ==================== FFT128 Benchmarks ====================

    @Benchmark
    public void benchmarkFFT128_Optimized(Blackhole bh) {
        FFTResult result = optimized128.transform(
            testData128.clone(),
            testImag128.clone(),
            true
        );
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkFFT128_Base(Blackhole bh) {
        FFTResult result = fftBase.transform(
            testData128.clone(),
            testImag128.clone(),
            true
        );
        bh.consume(result);
    }
}

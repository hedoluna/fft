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
 * -Dexec.classpathScope=test
 *
 * Or run specific benchmarks:
 * mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" \
 * -Dexec.args="FFT8" -Dexec.classpathScope=test
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 2) // 5 warmup iterations, 2 seconds each
@Measurement(iterations = 10, time = 2) // 10 measurement iterations, 2 seconds each
@Fork(value = 3, jvmArgs = { "-Xms2G", "-Xmx2G" }) // 3 forks for statistical confidence
public class FFTPerformanceBenchmark {

    private FFTBase fftBase;

    // Optimized implementations
    private FFTOptimized8 optimized8;
    private FFTOptimized16 optimized16;

    // Test data for FFT8
    private double[] testData8;
    private double[] testImag8;

    // Test data for FFT16
    private double[] testData16;
    private double[] testImag16;

    @Setup
    public void setup() {
        fftBase = new FFTBase();

        optimized8 = new FFTOptimized8();
        optimized16 = new FFTOptimized16();

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

    }

    // ==================== FFT8 Benchmarks ====================

    @Benchmark
    public void benchmarkFFT8_Optimized(Blackhole bh) {
        FFTResult result = optimized8.transform(
                testData8,
                testImag8,
                true);
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkFFT8_Base(Blackhole bh) {
        FFTResult result = fftBase.transform(
                testData8,
                testImag8,
                true);
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkFFT16_Optimized(Blackhole bh) {
        FFTResult result = optimized16.transform(
                testData16,
                testImag16,
                true);
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkFFT16_Base(Blackhole bh) {
        FFTResult result = fftBase.transform(
                testData16,
                testImag16,
                true);
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkFFT8_Optimized_WithInputClone(Blackhole bh) {
        FFTResult result = optimized8.transform(
                testData8.clone(),
                testImag8.clone(),
                true);
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkFFT8_Base_WithInputClone(Blackhole bh) {
        FFTResult result = fftBase.transform(
                testData8.clone(),
                testImag8.clone(),
                true);
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkFFT16_Optimized_WithInputClone(Blackhole bh) {
        FFTResult result = optimized16.transform(
                testData16.clone(),
                testImag16.clone(),
                true);
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkFFT16_Base_WithInputClone(Blackhole bh) {
        FFTResult result = fftBase.transform(
                testData16.clone(),
                testImag16.clone(),
                true);
        bh.consume(result);
    }

}

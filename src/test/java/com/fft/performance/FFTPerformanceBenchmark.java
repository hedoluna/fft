package com.fft.performance;

import com.fft.core.FFTBase;
import com.fft.optimized.FFTOptimized32;
import com.fft.optimized.FFTOptimized8;
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class FFTPerformanceBenchmark {
    
    private FFTBase fftBase;
    private FFTOptimized8 optimized8;
    private FFTOptimized32 optimized32;
    private double[] testData8;
    private double[] testData32;

    @Setup
    public void setup() {
        fftBase = new FFTBase();
        optimized8 = new FFTOptimized8();
        optimized32 = new FFTOptimized32();
        
        // Initialize test data
        testData8 = new double[8];
        for (int i = 0; i < 8; i++) {
            testData8[i] = Math.sin(2 * Math.PI * i / 8);
        }
        
        testData32 = new double[32];
        for (int i = 0; i < 32; i++) {
            testData32[i] = Math.cos(2 * Math.PI * i / 32);
        }
    }

    @Benchmark
    public Object benchmarkOptimized8() {
        return optimized8.transform(testData8.clone(), true);
    }

    @Benchmark
    public Object benchmarkBase8() {
        return fftBase.transform(testData8.clone(), new double[8], true);
    }

    @Benchmark
    public Object benchmarkOptimized32() {
        return optimized32.transform(testData32.clone(), true);
    }

    @Benchmark
    public Object benchmarkBase32() {
        return fftBase.transform(testData32.clone(), new double[32], true);
    }
}

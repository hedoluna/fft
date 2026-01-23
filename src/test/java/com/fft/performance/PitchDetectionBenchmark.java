package com.fft.performance;

import com.fft.utils.PitchDetectionUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 2)
@Measurement(iterations = 15, time = 2)
@Fork(value = 1, jvmArgs = { "-Xms2G", "-Xmx2G" })
public class PitchDetectionBenchmark {

    private double[] audioSamples;
    private double sampleRate = 44100.0;

    @Setup
    public void setup() {
        int bufferSize = 4096;
        audioSamples = new double[bufferSize];
        double frequency = 440.0;

        // Generate a sine wave
        for (int i = 0; i < bufferSize; i++) {
            audioSamples[i] = Math.sin(2.0 * Math.PI * frequency * i / sampleRate);
        }
    }

    @Benchmark
    public void benchmarkDetectPitchYin(Blackhole bh) {
        PitchDetectionUtils.PitchResult result = PitchDetectionUtils.detectPitchYin(audioSamples, sampleRate);
        bh.consume(result);
    }
}

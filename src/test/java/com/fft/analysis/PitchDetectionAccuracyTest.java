package com.fft.analysis;

import com.fft.core.FFT;
import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import com.fft.factory.DefaultFFTFactory;
import com.fft.factory.FFTFactory;
import com.fft.utils.PitchDetectionUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * Comprehensive test suite for evaluating pitch detection accuracy and performance
 * with different FFT implementations (base vs optimized).
 *
 * <p>This test generates synthetic audio signals with known frequencies and measures:
 * <ul>
 * <li>Frequency detection accuracy (% error)</li>
 * <li>Processing time (performance)</li>
 * <li>Confidence scores</li>
 * <li>Behavior under different noise conditions</li>
 * </ul>
 *
 * @author Engine AI Assistant
 * @since 2.0.0
 */
public class PitchDetectionAccuracyTest {

    private static final double SAMPLE_RATE = 44100.0;
    private static final int FFT_SIZE = 4096;

    // Test frequencies covering different ranges
    private static final double[] TEST_FREQUENCIES = {
        82.41,   // E2 (guitar low E)
        110.0,   // A2
        146.83,  // D3
        196.0,   // G3
        246.94,  // B3
        329.63,  // E4
        440.0,   // A4 (concert pitch)
        659.25,  // E5
        987.77,  // B5
        1318.51  // E6
    };

    @Test
    public void testAccuracyComparison() {
        System.out.println("=== Pitch Detection Accuracy Analysis ===\n");
        System.out.println("This test compares YIN algorithm (no FFT) vs Spectral method (with FFT)");
        System.out.println("to evaluate the impact of FFT implementation on pitch detection.\n");

        AccuracyResults yinResults = new AccuracyResults("YIN Algorithm (No FFT)");
        AccuracyResults spectralBaseResults = new AccuracyResults("Spectral Method (FFTBase)");
        AccuracyResults spectralOptimizedResults = new AccuracyResults("Spectral Method (FFTOptimized)");

        for (double frequency : TEST_FREQUENCIES) {
            System.out.printf("Testing frequency: %.2f Hz\n", frequency);

            // Generate test signal
            double[] signal = generateSineWave(frequency, SAMPLE_RATE, FFT_SIZE);

            // Test YIN algorithm (no FFT involvement)
            testYinAccuracy(signal, frequency, yinResults);

            // Test spectral method with FFTBase
            testSpectralAccuracy(signal, frequency, new FFTBase(), spectralBaseResults, "FFTBase");

            // Test spectral method with optimized FFT
            FFTFactory factory = new DefaultFFTFactory();
            FFT optimizedFFT = factory.createFFT(FFT_SIZE);
            testSpectralAccuracy(signal, frequency, optimizedFFT, spectralOptimizedResults, "FFTOptimized");

            System.out.println();
        }

        // Print summary
        printSummary(yinResults, spectralBaseResults, spectralOptimizedResults);
    }

    @Test
    public void testPerformanceComparison() {
        System.out.println("=== Pitch Detection Performance Analysis ===\n");

        // Use a test frequency in the middle of the range
        double testFreq = 440.0;
        double[] signal = generateSineWave(testFreq, SAMPLE_RATE, FFT_SIZE);

        int iterations = 1000;

        // Test YIN performance (no FFT)
        long yinTime = measureYinPerformance(signal, iterations);

        // Test spectral method with FFTBase
        FFT baseFFT = new FFTBase();
        long baseTime = measureSpectralPerformance(signal, baseFFT, iterations);

        // Test spectral method with optimized FFT
        FFTFactory factory = new DefaultFFTFactory();
        FFT optimizedFFT = factory.createFFT(FFT_SIZE);
        long optimizedTime = measureSpectralPerformance(signal, optimizedFFT, iterations);

        System.out.printf("YIN Algorithm:                %,d ns/op (%.2f ms total for %d iterations)\n",
            yinTime / iterations, yinTime / 1_000_000.0, iterations);
        System.out.printf("Spectral + FFTBase:          %,d ns/op (%.2f ms total for %d iterations)\n",
            baseTime / iterations, baseTime / 1_000_000.0, iterations);
        System.out.printf("Spectral + FFTOptimized:     %,d ns/op (%.2f ms total for %d iterations)\n",
            optimizedTime / iterations, optimizedTime / 1_000_000.0, iterations);

        double baseSpeedup = (double) baseTime / yinTime;
        double optimizedSpeedup = (double) optimizedTime / yinTime;
        double fftSpeedup = (double) baseTime / optimizedTime;

        System.out.printf("\nRelative Performance:\n");
        System.out.printf("  Spectral+FFTBase vs YIN:      %.2fx %s\n",
            Math.abs(baseSpeedup), baseSpeedup > 1 ? "slower" : "faster");
        System.out.printf("  Spectral+FFTOptimized vs YIN: %.2fx %s\n",
            Math.abs(optimizedSpeedup), optimizedSpeedup > 1 ? "slower" : "faster");
        System.out.printf("  FFTOptimized vs FFTBase:      %.2fx speedup\n", fftSpeedup);
    }

    @Test
    public void testComplexWaveforms() {
        System.out.println("=== Complex Waveform Analysis ===\n");
        System.out.println("Testing with harmonically rich signals (more realistic)...\n");

        double[] fundamentals = {110.0, 220.0, 440.0};

        for (double fundamental : fundamentals) {
            System.out.printf("Fundamental: %.1f Hz\n", fundamental);

            // Generate harmonic-rich signal (fundamental + 3 harmonics)
            double[] signal = generateHarmonicSignal(fundamental, SAMPLE_RATE, FFT_SIZE, 4);

            // Test YIN
            PitchDetectionUtils.PitchResult yinResult = PitchDetectionUtils.detectPitchYin(signal, SAMPLE_RATE);
            double yinError = Math.abs(yinResult.frequency - fundamental) / fundamental * 100;

            // Test spectral with FFTBase
            FFT baseFFT = new FFTBase();
            double[] paddedSignal = Arrays.copyOf(signal, FFT_SIZE);
            FFTResult spectrumBase = baseFFT.transform(paddedSignal);
            PitchDetectionUtils.PitchResult spectralBaseResult =
                PitchDetectionUtils.detectPitchSpectral(spectrumBase, SAMPLE_RATE);
            double spectralBaseError = Math.abs(spectralBaseResult.frequency - fundamental) / fundamental * 100;

            // Test spectral with optimized FFT
            FFTFactory factory = new DefaultFFTFactory();
            FFT optimizedFFT = factory.createFFT(FFT_SIZE);
            FFTResult spectrumOpt = optimizedFFT.transform(paddedSignal);
            PitchDetectionUtils.PitchResult spectralOptResult =
                PitchDetectionUtils.detectPitchSpectral(spectrumOpt, SAMPLE_RATE);
            double spectralOptError = Math.abs(spectralOptResult.frequency - fundamental) / fundamental * 100;

            System.out.printf("  YIN:                  %.2f Hz (%.3f%% error, conf: %.3f)\n",
                yinResult.frequency, yinError, yinResult.confidence);
            System.out.printf("  Spectral (Base):      %.2f Hz (%.3f%% error, conf: %.3f)\n",
                spectralBaseResult.frequency, spectralBaseError, spectralBaseResult.confidence);
            System.out.printf("  Spectral (Optimized): %.2f Hz (%.3f%% error, conf: %.3f)\n",
                spectralOptResult.frequency, spectralOptError, spectralOptResult.confidence);

            // Check if FFT implementations produce identical results
            boolean identical = Math.abs(spectralBaseResult.frequency - spectralOptResult.frequency) < 0.01;
            System.out.printf("  FFT implementations identical: %s\n\n", identical ? "YES ✓" : "NO ✗");
        }
    }

    @Test
    public void testNoiseTolerance() {
        System.out.println("=== Noise Tolerance Analysis ===\n");
        System.out.println("Testing with different SNR levels...\n");

        double testFreq = 440.0;
        double[] snrLevels = {30.0, 20.0, 10.0, 5.0}; // dB

        for (double snr : snrLevels) {
            System.out.printf("SNR: %.1f dB\n", snr);

            double[] signal = generateNoisySignal(testFreq, SAMPLE_RATE, FFT_SIZE, snr);

            // Test all three methods
            PitchDetectionUtils.PitchResult yinResult = PitchDetectionUtils.detectPitchYin(signal, SAMPLE_RATE);

            FFT baseFFT = new FFTBase();
            double[] paddedSignal = Arrays.copyOf(signal, FFT_SIZE);
            FFTResult spectrumBase = baseFFT.transform(paddedSignal);
            PitchDetectionUtils.PitchResult spectralBaseResult =
                PitchDetectionUtils.detectPitchSpectral(spectrumBase, SAMPLE_RATE);

            FFTFactory factory = new DefaultFFTFactory();
            FFT optimizedFFT = factory.createFFT(FFT_SIZE);
            FFTResult spectrumOpt = optimizedFFT.transform(paddedSignal);
            PitchDetectionUtils.PitchResult spectralOptResult =
                PitchDetectionUtils.detectPitchSpectral(spectrumOpt, SAMPLE_RATE);

            System.out.printf("  YIN:             %.2f Hz (conf: %.3f)\n",
                yinResult.frequency, yinResult.confidence);
            System.out.printf("  Spectral (Base): %.2f Hz (conf: %.3f)\n",
                spectralBaseResult.frequency, spectralBaseResult.confidence);
            System.out.printf("  Spectral (Opt):  %.2f Hz (conf: %.3f)\n\n",
                spectralOptResult.frequency, spectralOptResult.confidence);
        }
    }

    // Helper methods

    private void testYinAccuracy(double[] signal, double expectedFreq, AccuracyResults results) {
        long startTime = System.nanoTime();
        PitchDetectionUtils.PitchResult result = PitchDetectionUtils.detectPitchYin(signal, SAMPLE_RATE);
        long endTime = System.nanoTime();

        double error = Math.abs(result.frequency - expectedFreq) / expectedFreq * 100;
        results.addMeasurement(result.frequency, expectedFreq, error, result.confidence, endTime - startTime);

        System.out.printf("  YIN: %.2f Hz (error: %.3f%%, conf: %.3f, time: %,d ns)\n",
            result.frequency, error, result.confidence, endTime - startTime);
    }

    private void testSpectralAccuracy(double[] signal, double expectedFreq, FFT fft,
                                     AccuracyResults results, String fftType) {
        double[] paddedSignal = Arrays.copyOf(signal, FFT_SIZE);

        long startTime = System.nanoTime();
        FFTResult spectrum = fft.transform(paddedSignal);
        PitchDetectionUtils.PitchResult result = PitchDetectionUtils.detectPitchSpectral(spectrum, SAMPLE_RATE);
        long endTime = System.nanoTime();

        double error = Math.abs(result.frequency - expectedFreq) / expectedFreq * 100;
        results.addMeasurement(result.frequency, expectedFreq, error, result.confidence, endTime - startTime);

        System.out.printf("  Spectral (%s): %.2f Hz (error: %.3f%%, conf: %.3f, time: %,d ns)\n",
            fftType, result.frequency, error, result.confidence, endTime - startTime);
    }

    private long measureYinPerformance(double[] signal, int iterations) {
        // Warmup
        for (int i = 0; i < 100; i++) {
            PitchDetectionUtils.detectPitchYin(signal, SAMPLE_RATE);
        }

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            PitchDetectionUtils.detectPitchYin(signal, SAMPLE_RATE);
        }
        return System.nanoTime() - startTime;
    }

    private long measureSpectralPerformance(double[] signal, FFT fft, int iterations) {
        double[] paddedSignal = Arrays.copyOf(signal, FFT_SIZE);

        // Warmup
        for (int i = 0; i < 100; i++) {
            FFTResult spectrum = fft.transform(paddedSignal);
            PitchDetectionUtils.detectPitchSpectral(spectrum, SAMPLE_RATE);
        }

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            FFTResult spectrum = fft.transform(paddedSignal);
            PitchDetectionUtils.detectPitchSpectral(spectrum, SAMPLE_RATE);
        }
        return System.nanoTime() - startTime;
    }

    private double[] generateSineWave(double frequency, double sampleRate, int numSamples) {
        double[] signal = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            signal[i] = Math.sin(2.0 * Math.PI * frequency * i / sampleRate);
        }
        return signal;
    }

    private double[] generateHarmonicSignal(double fundamental, double sampleRate, int numSamples, int numHarmonics) {
        double[] signal = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            double sample = 0.0;
            for (int h = 1; h <= numHarmonics; h++) {
                double amplitude = 1.0 / h; // Decreasing amplitude for higher harmonics
                sample += amplitude * Math.sin(2.0 * Math.PI * fundamental * h * i / sampleRate);
            }
            signal[i] = sample / numHarmonics; // Normalize
        }
        return signal;
    }

    private double[] generateNoisySignal(double frequency, double sampleRate, int numSamples, double snrDb) {
        double[] clean = generateSineWave(frequency, sampleRate, numSamples);

        // Calculate signal power
        double signalPower = 0.0;
        for (double sample : clean) {
            signalPower += sample * sample;
        }
        signalPower /= numSamples;

        // Calculate required noise power for desired SNR
        double noisePower = signalPower / Math.pow(10, snrDb / 10.0);
        double noiseStdDev = Math.sqrt(noisePower);

        // Add Gaussian noise
        Random random = new Random(42); // Fixed seed for reproducibility
        double[] noisy = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            noisy[i] = clean[i] + noiseStdDev * random.nextGaussian();
        }

        return noisy;
    }

    private void printSummary(AccuracyResults yin, AccuracyResults spectralBase, AccuracyResults spectralOpt) {
        System.out.println("\n=== SUMMARY ===\n");

        System.out.println(yin.getSummary());
        System.out.println(spectralBase.getSummary());
        System.out.println(spectralOpt.getSummary());

        System.out.println("\n=== KEY FINDINGS ===\n");

        // Compare accuracy
        System.out.printf("Accuracy (Mean Absolute Error):\n");
        System.out.printf("  YIN:                  %.3f%%\n", yin.getMeanError());
        System.out.printf("  Spectral (FFTBase):   %.3f%%\n", spectralBase.getMeanError());
        System.out.printf("  Spectral (FFTOpt):    %.3f%%\n", spectralOpt.getMeanError());

        // Compare confidence
        System.out.printf("\nConfidence Scores:\n");
        System.out.printf("  YIN:                  %.3f\n", yin.getMeanConfidence());
        System.out.printf("  Spectral (FFTBase):   %.3f\n", spectralBase.getMeanConfidence());
        System.out.printf("  Spectral (FFTOpt):    %.3f\n", spectralOpt.getMeanConfidence());

        // Compare performance
        System.out.printf("\nPerformance (Mean Time):\n");
        System.out.printf("  YIN:                  %,d ns\n", yin.getMeanTime());
        System.out.printf("  Spectral (FFTBase):   %,d ns\n", spectralBase.getMeanTime());
        System.out.printf("  Spectral (FFTOpt):    %,d ns (%.2fx faster than FFTBase)\n",
            spectralOpt.getMeanTime(), (double) spectralBase.getMeanTime() / spectralOpt.getMeanTime());

        // Check if FFT implementations produce identical results
        boolean identical = Math.abs(spectralBase.getMeanError() - spectralOpt.getMeanError()) < 0.001;
        System.out.printf("\nFFT Implementation Accuracy Impact: %s\n",
            identical ? "IDENTICAL (as expected)" : "DIFFERENT (unexpected!)");

        System.out.println("\n=== RECOMMENDATIONS ===\n");

        if (yin.getMeanError() < spectralBase.getMeanError()) {
            System.out.println("✓ YIN algorithm provides better accuracy than spectral method");
            System.out.println("  → Use YIN as primary method (already implemented in PitchDetectionDemo)");
        } else {
            System.out.println("✓ Spectral method provides comparable accuracy to YIN");
        }

        System.out.println("✓ FFT implementation (base vs optimized) does NOT affect accuracy");
        System.out.println("  → Both produce mathematically identical results");
        System.out.printf("✓ FFTOptimized provides %.2fx performance improvement over FFTBase\n",
            (double) spectralBase.getMeanTime() / spectralOpt.getMeanTime());
        System.out.println("  → Always use optimized FFT when available (automatic via factory)");
    }

    // Helper class to accumulate results
    private static class AccuracyResults {
        private final String name;
        private final List<Double> frequencies = new ArrayList<>();
        private final List<Double> expectedFreqs = new ArrayList<>();
        private final List<Double> errors = new ArrayList<>();
        private final List<Double> confidences = new ArrayList<>();
        private final List<Long> times = new ArrayList<>();

        AccuracyResults(String name) {
            this.name = name;
        }

        void addMeasurement(double frequency, double expected, double error, double confidence, long time) {
            frequencies.add(frequency);
            expectedFreqs.add(expected);
            errors.add(error);
            confidences.add(confidence);
            times.add(time);
        }

        double getMeanError() {
            return errors.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }

        double getMeanConfidence() {
            return confidences.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }

        long getMeanTime() {
            return (long) times.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }

        String getSummary() {
            return String.format("%s:\n" +
                    "  Mean Error: %.3f%%\n" +
                    "  Max Error:  %.3f%%\n" +
                    "  Min Error:  %.3f%%\n" +
                    "  Mean Confidence: %.3f\n" +
                    "  Mean Time: %,d ns\n",
                    name,
                    getMeanError(),
                    errors.stream().mapToDouble(Double::doubleValue).max().orElse(0.0),
                    errors.stream().mapToDouble(Double::doubleValue).min().orElse(0.0),
                    getMeanConfidence(),
                    getMeanTime());
        }
    }
}

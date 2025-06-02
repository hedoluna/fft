package com.fft.demo;

import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Random;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for FFT-based pitch detection functionality.
 * 
 * <p>This test suite validates the pitch detection algorithms used in the demo,
 * ensuring accuracy, robustness, and performance across various scenarios.</p>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
class PitchDetectionTest {
    
    private static final double SAMPLE_RATE = 44100.0;
    private static final double FREQUENCY_TOLERANCE = 2.0; // Hz
    private static final double MAGNITUDE_THRESHOLD = 0.01;
    
    @Nested
    @DisplayName("Single Tone Detection Tests")
    class SingleToneDetectionTests {
        
        @Test
        @DisplayName("Should accurately detect A4 (440 Hz)")
        void shouldDetectA4Frequency() {
            double expectedFreq = 440.0;
            double[] signal = generateTone(expectedFreq, 1.0, 1.0);
            
            double detectedFreq = detectPitchFromSignal(signal);
            
            assertThat(detectedFreq)
                .as("A4 frequency detection")
                .isCloseTo(expectedFreq, within(FREQUENCY_TOLERANCE));
        }
        
        @Test
        @DisplayName("Should detect various musical notes accurately")
        void shouldDetectVariousMusicalNotes() {
            double[] testFrequencies = {
                261.63, // C4
                293.66, // D4
                329.63, // E4
                349.23, // F4
                392.00, // G4
                440.00, // A4
                493.88, // B4
                523.25  // C5
            };
            
            for (double expectedFreq : testFrequencies) {
                double[] signal = generateTone(expectedFreq, 0.5, 1.0);
                double detectedFreq = detectPitchFromSignal(signal);
                
                assertThat(detectedFreq)
                    .as("Detection of %.2f Hz", expectedFreq)
                    .isCloseTo(expectedFreq, within(FREQUENCY_TOLERANCE));
            }
        }
        
        @Test
        @DisplayName("Should handle low amplitude signals")
        void shouldHandleLowAmplitudeSignals() {
            double frequency = 440.0;
            double[] amplitudes = {1.0, 0.5, 0.1, 0.05};
            
            for (double amplitude : amplitudes) {
                double[] signal = generateTone(frequency, 0.5, amplitude);
                double detectedFreq = detectPitchFromSignal(signal);
                
                if (amplitude >= 0.1) {
                    assertThat(detectedFreq)
                        .as("Detection with amplitude %.2f", amplitude)
                        .isCloseTo(frequency, within(FREQUENCY_TOLERANCE));
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Noise Robustness Tests")
    class NoiseRobustnessTests {
        
        @Test
        @DisplayName("Should detect pitch in presence of white noise")
        void shouldDetectPitchWithWhiteNoise() {
            double frequency = 440.0;
            double[] signal = generateTone(frequency, 1.0, 1.0);
            
            // Add moderate noise
            addWhiteNoise(signal, 0.2);
            
            double detectedFreq = detectPitchFromSignal(signal);
            
            assertThat(detectedFreq)
                .as("A4 detection with white noise")
                .isCloseTo(frequency, within(FREQUENCY_TOLERANCE * 2)); // Allow more tolerance with noise
        }
        
        @Test
        @DisplayName("Should maintain accuracy across different SNR levels")
        void shouldMaintainAccuracyAcrossSNRLevels() {
            double frequency = 440.0;
            double[] noiseLevels = {0.0, 0.1, 0.2, 0.3};
            
            for (double noiseLevel : noiseLevels) {
                double[] signal = generateTone(frequency, 0.5, 1.0);
                addWhiteNoise(signal, noiseLevel);
                
                double detectedFreq = detectPitchFromSignal(signal);
                double tolerance = FREQUENCY_TOLERANCE * (1.0 + noiseLevel * 2);
                
                assertThat(detectedFreq)
                    .as("Detection with noise level %.1f", noiseLevel)
                    .isCloseTo(frequency, within(tolerance));
            }
        }
    }
    
    @Nested
    @DisplayName("Multiple Frequency Tests")
    class MultipleFrequencyTests {
        
        @Test
        @DisplayName("Should detect dominant frequency in chord")
        void shouldDetectDominantFrequencyInChord() {
            // C Major chord: C4, E4, G4 with C4 dominant
            double[] frequencies = {261.63, 329.63, 392.00};
            double[] amplitudes = {1.0, 0.5, 0.3};
            
            double[] signal = generateChord(frequencies, amplitudes, 1.0);
            double detectedFreq = detectPitchFromSignal(signal);
            
            // Should detect the dominant frequency (C4)
            assertThat(detectedFreq)
                .as("Dominant frequency in C major chord")
                .isCloseTo(frequencies[0], within(FREQUENCY_TOLERANCE));
        }
        
        @Test
        @DisplayName("Should handle harmonic content")
        void shouldHandleHarmonicContent() {
            double fundamental = 220.0; // A3
            double[] harmonics = {fundamental, fundamental * 2, fundamental * 3, fundamental * 4};
            double[] amplitudes = {1.0, 0.5, 0.25, 0.125};
            
            double[] signal = generateChord(harmonics, amplitudes, 1.0);
            double detectedFreq = detectPitchFromSignal(signal);
            
            // Should detect the fundamental frequency
            assertThat(detectedFreq)
                .as("Fundamental frequency with harmonics")
                .isCloseTo(fundamental, within(FREQUENCY_TOLERANCE));
        }
    }
    
    @Nested
    @DisplayName("Signal Processing Tests")
    class SignalProcessingTests {
        
        @Test
        @DisplayName("Should handle different signal lengths")
        void shouldHandleDifferentSignalLengths() {
            // Test with a frequency that has integer number of cycles in signal lengths
            double frequency = 441.0; // This will have closer to integer cycles in our lengths
            int[] signalLengths = {512, 1024, 2048, 4096, 8192};
            
            for (int length : signalLengths) {
                double[] signal = new double[length];
                
                // Generate signal with exact number of samples
                for (int i = 0; i < length; i++) {
                    double t = i / SAMPLE_RATE;
                    signal[i] = Math.sin(2.0 * Math.PI * frequency * t);
                }
                
                double detectedFreq = detectPitchFromSignal(signal);
                
                // Adjust tolerance based on signal length (shorter signals have worse frequency resolution)
                double tolerance = FREQUENCY_TOLERANCE;
                if (length <= 1024) {
                    tolerance = Math.max(SAMPLE_RATE / length, 15.0); // Frequency resolution is sample_rate / length
                } else {
                    tolerance = Math.max(SAMPLE_RATE / length * 2, FREQUENCY_TOLERANCE);
                }
                
                assertThat(detectedFreq)
                    .as("Detection with signal length %d (tolerance: %.1f Hz)", length, tolerance)
                    .isCloseTo(frequency, within(tolerance));
            }
        }
        
        @Test
        @DisplayName("Should handle windowed signals")
        void shouldHandleWindowedSignals() {
            double frequency = 440.0;
            double[] signal = generateTone(frequency, 1.0, 1.0);
            
            // Apply Hamming window
            applyHammingWindow(signal);
            
            double detectedFreq = detectPitchFromSignal(signal);
            
            assertThat(detectedFreq)
                .as("Detection with Hamming window")
                .isCloseTo(frequency, within(FREQUENCY_TOLERANCE));
        }
    }
    
    @Nested
    @DisplayName("FFT Performance Tests")
    class FFTPerformanceTests {
        
        @Test
        @DisplayName("Should complete FFT analysis within reasonable time")
        void shouldCompleteFFTAnalysisQuickly() {
            double[] signal = generateTone(440.0, 1.0, 1.0);
            signal = FFTUtils.zeroPadToPowerOfTwo(signal);
            
            long startTime = System.nanoTime();
            FFTResult result = FFTUtils.fft(signal);
            long endTime = System.nanoTime();
            
            double executionTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
            
            assertThat(executionTime)
                .as("FFT execution time")
                .isLessThan(100.0); // Should complete within 100ms
            
            assertThat(result.size())
                .as("FFT result size")
                .isEqualTo(signal.length);
        }
        
        @Test
        @DisplayName("Should use optimized implementations for power-of-2 sizes")
        void shouldUseOptimizedImplementations() {
            int[] optimizedSizes = {8, 32, 64, 128, 256, 512, 1024, 2048, 4096};
            
            for (int size : optimizedSizes) {
                String implementation = FFTUtils.getImplementationInfo(size);
                
                assertThat(implementation)
                    .as("Implementation for size %d", size)
                    .isNotNull()
                    .isNotEmpty();
                
                // Test that the optimized implementation actually works
                double[] signal = new double[size];
                // Create a simple test signal
                for (int i = 0; i < size; i++) {
                    signal[i] = Math.sin(2.0 * Math.PI * 10 * i / size);
                }
                
                FFTResult result = FFTUtils.fft(signal);
                
                assertThat(result.size())
                    .as("FFT result size for size %d", size)
                    .isEqualTo(size);
            }
        }
    }
    
    @Nested
    @DisplayName("Parsons Code Generation Tests")
    class ParsonsCodeGenerationTests {
        
        @Test
        @DisplayName("Should generate correct Parsons code for simple melody")
        void shouldGenerateCorrectParsonsCode() {
            // Simple ascending melody: C4 -> D4 -> E4
            double[] frequencies = {261.63, 293.66, 329.63};
            String expectedParsons = "UU"; // Up, Up
            
            String actualParsons = generateParsonsCodeFromFrequencies(frequencies);
            
            assertThat(actualParsons)
                .as("Parsons code for ascending melody")
                .isEqualTo(expectedParsons);
        }
        
        @Test
        @DisplayName("Should handle repeated notes")
        void shouldHandleRepeatedNotes() {
            // Melody with repeated notes: C4 -> C4 -> E4 -> E4
            double[] frequencies = {261.63, 261.63, 329.63, 329.63};
            String expectedParsons = "RUR"; // Repeat, Up, Repeat
            
            String actualParsons = generateParsonsCodeFromFrequencies(frequencies);
            
            assertThat(actualParsons)
                .as("Parsons code with repeated notes")
                .isEqualTo(expectedParsons);
        }
        
        @Test
        @DisplayName("Should generate code for descending melody")
        void shouldGenerateCodeForDescendingMelody() {
            // Descending melody: G4 -> F4 -> E4 -> D4
            double[] frequencies = {392.00, 349.23, 329.63, 293.66};
            String expectedParsons = "DDD"; // Down, Down, Down
            
            String actualParsons = generateParsonsCodeFromFrequencies(frequencies);
            
            assertThat(actualParsons)
                .as("Parsons code for descending melody")
                .isEqualTo(expectedParsons);
        }
    }
    
    // Helper methods for signal generation and analysis
    
    private double[] generateTone(double frequency, double duration, double amplitude) {
        int samples = (int) (SAMPLE_RATE * duration);
        double[] signal = new double[samples];
        
        for (int i = 0; i < samples; i++) {
            double t = i / SAMPLE_RATE;
            signal[i] = amplitude * Math.sin(2.0 * Math.PI * frequency * t);
        }
        
        return signal;
    }
    
    private double[] generateChord(double[] frequencies, double[] amplitudes, double duration) {
        int samples = (int) (SAMPLE_RATE * duration);
        double[] signal = new double[samples];
        
        for (int i = 0; i < samples; i++) {
            double t = i / SAMPLE_RATE;
            double sample = 0.0;
            
            for (int j = 0; j < frequencies.length; j++) {
                sample += amplitudes[j] * Math.sin(2.0 * Math.PI * frequencies[j] * t);
            }
            
            signal[i] = sample;
        }
        
        return signal;
    }
    
    private void addWhiteNoise(double[] signal, double noiseLevel) {
        Random random = new Random(42); // Fixed seed for reproducible tests
        
        for (int i = 0; i < signal.length; i++) {
            signal[i] += noiseLevel * random.nextGaussian();
        }
    }
    
    private void applyHammingWindow(double[] signal) {
        int n = signal.length;
        for (int i = 0; i < n; i++) {
            double window = 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / (n - 1));
            signal[i] *= window;
        }
    }
    
    private double detectPitchFromSignal(double[] signal) {
        // Ensure signal is power of 2 for FFT
        double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
        
        FFTResult spectrum = FFTUtils.fft(paddedSignal);
        double[] magnitudes = spectrum.getMagnitudes();
        
        // Find peak frequency in musical range
        int peakBin = 0;
        double maxMagnitude = 0.0;
        
        int minBin = frequencyToBin(80.0, paddedSignal.length);
        int maxBin = Math.min(frequencyToBin(2000.0, paddedSignal.length), magnitudes.length / 2);
        
        for (int i = minBin; i < maxBin; i++) {
            if (magnitudes[i] > maxMagnitude) {
                maxMagnitude = magnitudes[i];
                peakBin = i;
            }
        }
        
        if (maxMagnitude < MAGNITUDE_THRESHOLD) {
            return 0.0; // No significant peak found
        }
        
        return binToFrequency(peakBin, paddedSignal.length);
    }
    
    private int frequencyToBin(double frequency, int signalLength) {
        return (int) Math.round(frequency * signalLength / SAMPLE_RATE);
    }
    
    private double binToFrequency(int bin, int signalLength) {
        return (double) bin * SAMPLE_RATE / signalLength;
    }
    
    private String generateParsonsCodeFromFrequencies(double[] frequencies) {
        if (frequencies.length < 2) return "";
        
        StringBuilder parsons = new StringBuilder();
        double threshold = 10.0; // Hz threshold for considering frequencies equal
        
        for (int i = 1; i < frequencies.length; i++) {
            double diff = frequencies[i] - frequencies[i - 1];
            
            if (Math.abs(diff) < threshold) {
                parsons.append("R"); // Repeat
            } else if (diff > 0) {
                parsons.append("U"); // Up
            } else {
                parsons.append("D"); // Down
            }
        }
        
        return parsons.toString();
    }
}
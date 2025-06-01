package com.fft.demos.audio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.Map;
import java.util.Arrays;

/**
 * Comprehensive test suite for AudioSignalProcessor.
 * 
 * <p>This test suite validates all audio processing functionality including:</p>
 * <ul>
 * <li>Filter applications and correctness</li>
 * <li>Audio analysis accuracy</li>
 * <li>Noise reduction effectiveness</li>
 * <li>Reverb processing quality</li>
 * <li>Performance characteristics</li>
 * <li>Edge cases and error handling</li>
 * </ul>
 */
class AudioSignalProcessorTest {
    
    private AudioSignalProcessor processor;
    private static final double SAMPLE_RATE = 44100.0;
    private static final double TOLERANCE = 1e-10;
    private static final double FREQUENCY_TOLERANCE = 5.0; // 5 Hz tolerance for frequency measurements
    
    @BeforeEach
    void setUp() {
        processor = new AudioSignalProcessor();
        processor.clearHistory(); // Start with clean history
    }
    
    @Nested
    @DisplayName("Filter Tests")
    class FilterTests {
        
        @Test
        @DisplayName("Should provide access to all predefined filters")
        void shouldProvideAllPredefinedFilters() {
            Set<String> filters = processor.getAvailableFilters();
            
            assertThat(filters)
                .containsExactlyInAnyOrder("lowpass", "highpass", "vocal", "notch60")
                .hasSize(4);
        }
        
        @Test
        @DisplayName("Low-pass filter should attenuate high frequencies")
        void lowPassFilterShouldAttenuateHighFrequencies() {
            // Generate signal with low and high frequency components
            double[] signal = generateTestSignal(1024, 100.0, 4000.0); // 100Hz + 4kHz
            
            double[] filtered = processor.applyFilter(signal, "lowpass", null, SAMPLE_RATE);
            
            // Analyze frequency content
            AudioSignalProcessor.AudioMetrics originalMetrics = processor.analyzeSignal(signal, SAMPLE_RATE);
            AudioSignalProcessor.AudioMetrics filteredMetrics = processor.analyzeSignal(filtered, SAMPLE_RATE);
            
            // Low-pass should reduce spectral centroid (shift energy to lower frequencies)
            assertThat(filteredMetrics.getSpectralCentroid())
                .isLessThan(originalMetrics.getSpectralCentroid());
        }
        
        @Test
        @DisplayName("High-pass filter should attenuate low frequencies")
        void highPassFilterShouldAttenuateLowFrequencies() {
            double[] signal = generateTestSignal(1024, 50.0, 500.0); // 50Hz + 500Hz
            
            double[] filtered = processor.applyFilter(signal, "highpass", null, SAMPLE_RATE);
            
            AudioSignalProcessor.AudioMetrics originalMetrics = processor.analyzeSignal(signal, SAMPLE_RATE);
            AudioSignalProcessor.AudioMetrics filteredMetrics = processor.analyzeSignal(filtered, SAMPLE_RATE);
            
            // High-pass should increase spectral centroid
            assertThat(filteredMetrics.getSpectralCentroid())
                .isGreaterThan(originalMetrics.getSpectralCentroid());
        }
        
        @Test
        @DisplayName("Vocal filter should preserve vocal frequencies")
        void vocalFilterShouldPreserveVocalFrequencies() {
            // Generate signal with vocal (200Hz) and non-vocal frequencies
            double[] signal = generateTestSignal(1024, 200.0, 50.0, 5000.0); // Vocal + low + high
            
            double[] filtered = processor.applyFilter(signal, "vocal", null, SAMPLE_RATE);
            
            AudioSignalProcessor.AudioMetrics filteredMetrics = processor.analyzeSignal(filtered, SAMPLE_RATE);
            
            // Should preserve energy in vocal range
            assertThat(filteredMetrics.getSpectralCentroid())
                .isBetween(80.0, 4000.0); // Vocal filter range
        }
        
        @Test
        @DisplayName("60Hz notch filter should remove power line noise")
        void notch60FilterShouldRemove60HzNoise() {
            // Generate signal with 60Hz interference
            double[] signal = generateTestSignal(1024, 440.0, 60.0); // Music + 60Hz hum
            
            double[] filtered = processor.applyFilter(signal, "notch60", null, SAMPLE_RATE);
            
            // The 60Hz component should be significantly reduced
            AudioSignalProcessor.AudioMetrics originalMetrics = processor.analyzeSignal(signal, SAMPLE_RATE);
            AudioSignalProcessor.AudioMetrics filteredMetrics = processor.analyzeSignal(filtered, SAMPLE_RATE);
            
            // Should reduce overall energy (60Hz component removed)
            assertThat(filteredMetrics.getRmsEnergy())
                .isLessThan(originalMetrics.getRmsEnergy());
        }
        
        @Test
        @DisplayName("Custom filter should be applied correctly")
        void customFilterShouldBeAppliedCorrectly() {
            double[] signal = generateTestSignal(512, 440.0);
            
            // Custom filter that zeros out everything
            AudioSignalProcessor.FilterFunction zeroFilter = (freq, real, imag, sampleRate) -> {
                Arrays.fill(real, 0.0);
                Arrays.fill(imag, 0.0);
            };
            
            double[] filtered = processor.applyFilter(signal, "custom", zeroFilter, SAMPLE_RATE);
            
            // Should be essentially zero
            for (double sample : filtered) {
                assertThat(Math.abs(sample)).isLessThan(1e-10);
            }
        }
        
        @Test
        @DisplayName("Should handle power-of-two and non-power-of-two signals")
        void shouldHandleDifferentSignalSizes() {
            double[] powerOfTwo = generateTestSignal(1024, 440.0);
            double[] nonPowerOfTwo = generateTestSignal(1000, 440.0);
            
            // Both should process without errors
            assertDoesNotThrow(() -> {
                processor.applyFilter(powerOfTwo, "lowpass", null, SAMPLE_RATE);
                processor.applyFilter(nonPowerOfTwo, "lowpass", null, SAMPLE_RATE);
            });
            
            // Output should have same length as input
            double[] filtered1 = processor.applyFilter(powerOfTwo, "lowpass", null, SAMPLE_RATE);
            double[] filtered2 = processor.applyFilter(nonPowerOfTwo, "lowpass", null, SAMPLE_RATE);
            
            assertThat(filtered1).hasSize(powerOfTwo.length);
            assertThat(filtered2).hasSize(nonPowerOfTwo.length);
        }
    }
    
    @Nested
    @DisplayName("Audio Analysis Tests")
    class AudioAnalysisTests {
        
        @Test
        @DisplayName("Should accurately detect fundamental frequency")
        void shouldAccuratelyDetectFundamentalFrequency() {
            double targetFreq = 440.0; // A4
            double[] signal = generatePureSignal(2048, targetFreq);
            
            AudioSignalProcessor.AudioMetrics metrics = processor.analyzeSignal(signal, SAMPLE_RATE);
            
            assertThat(metrics.getFundamentalFrequency())
                .isCloseTo(targetFreq, within(FREQUENCY_TOLERANCE));
        }
        
        @Test
        @DisplayName("Should detect harmonics correctly")
        void shouldDetectHarmonicsCorrectly() {
            double fundamental = 220.0; // A3
            double[] signal = generateHarmonicSignal(2048, fundamental, 5); // 5 harmonics
            
            AudioSignalProcessor.AudioMetrics metrics = processor.analyzeSignal(signal, SAMPLE_RATE);
            
            assertThat(metrics.getHarmonicPeaks()).isNotEmpty();
            assertThat(metrics.getFundamentalFrequency())
                .isCloseTo(fundamental, within(FREQUENCY_TOLERANCE));
            
            // Should detect multiple harmonics
            assertThat(metrics.getHarmonicPeaks().size()).isGreaterThanOrEqualTo(3);
        }
        
        @Test
        @DisplayName("Should calculate spectral centroid correctly")
        void shouldCalculateSpectralCentroidCorrectly() {
            // Low frequency signal should have low centroid
            double[] lowFreqSignal = generatePureSignal(1024, 100.0);
            AudioSignalProcessor.AudioMetrics lowMetrics = processor.analyzeSignal(lowFreqSignal, SAMPLE_RATE);
            
            // High frequency signal should have high centroid
            double[] highFreqSignal = generatePureSignal(1024, 2000.0);
            AudioSignalProcessor.AudioMetrics highMetrics = processor.analyzeSignal(highFreqSignal, SAMPLE_RATE);
            
            assertThat(highMetrics.getSpectralCentroid())
                .isGreaterThan(lowMetrics.getSpectralCentroid());
        }
        
        @Test
        @DisplayName("Should calculate RMS energy correctly")
        void shouldCalculateRmsEnergyCorrectly() {
            // Higher amplitude should result in higher RMS
            double[] lowAmpSignal = generatePureSignal(1024, 440.0, 0.1);
            double[] highAmpSignal = generatePureSignal(1024, 440.0, 0.8);
            
            AudioSignalProcessor.AudioMetrics lowMetrics = processor.analyzeSignal(lowAmpSignal, SAMPLE_RATE);
            AudioSignalProcessor.AudioMetrics highMetrics = processor.analyzeSignal(highAmpSignal, SAMPLE_RATE);
            
            assertThat(highMetrics.getRmsEnergy())
                .isGreaterThan(lowMetrics.getRmsEnergy());
        }
        
        @Test
        @DisplayName("Should calculate zero crossing rate correctly")
        void shouldCalculateZeroCrossingRateCorrectly() {
            // Higher frequency should have higher ZCR
            double[] lowFreqSignal = generatePureSignal(1024, 100.0);
            double[] highFreqSignal = generatePureSignal(1024, 1000.0);
            
            AudioSignalProcessor.AudioMetrics lowMetrics = processor.analyzeSignal(lowFreqSignal, SAMPLE_RATE);
            AudioSignalProcessor.AudioMetrics highMetrics = processor.analyzeSignal(highFreqSignal, SAMPLE_RATE);
            
            assertThat(highMetrics.getZeroCrossingRate())
                .isGreaterThan(lowMetrics.getZeroCrossingRate());
        }
        
        @Test
        @DisplayName("Should handle silence correctly")
        void shouldHandleSilenceCorrectly() {
            double[] silence = new double[1024]; // All zeros
            
            AudioSignalProcessor.AudioMetrics metrics = processor.analyzeSignal(silence, SAMPLE_RATE);
            
            assertThat(metrics.getRmsEnergy()).isEqualTo(0.0);
            assertThat(metrics.getZeroCrossingRate()).isEqualTo(0.0);
            assertThat(metrics.getSpectralCentroid()).isEqualTo(0.0);
        }
    }
    
    @Nested
    @DisplayName("Noise Reduction Tests")
    class NoiseReductionTests {
        
        @Test
        @DisplayName("Should reduce noise level effectively")
        void shouldReduceNoiseLevelEffectively() {
            // Generate clean signal and noise profile
            double[] cleanSignal = generatePureSignal(1024, 440.0);
            double[] noise = generateNoise(512);
            double[] noisySignal = addNoise(cleanSignal, 0.3);
            
            double[] denoised = processor.reduceNoise(noisySignal, noise, SAMPLE_RATE, 2.0);
            
            // Denoised signal should be closer to original than noisy signal
            double noisyError = calculateMeanSquaredError(cleanSignal, noisySignal);
            double denoisedError = calculateMeanSquaredError(cleanSignal, denoised);
            
            assertThat(denoisedError).isLessThan(noisyError);
        }
        
        @Test
        @DisplayName("Should preserve signal quality during noise reduction")
        void shouldPreserveSignalQualityDuringNoiseReduction() {
            double[] signal = generateHarmonicSignal(1024, 220.0, 3);
            double[] noise = generateNoise(256);
            double[] noisy = addNoise(signal, 0.2);
            
            double[] denoised = processor.reduceNoise(noisy, noise, SAMPLE_RATE, 1.5);
            
            // Analyze signal characteristics
            AudioSignalProcessor.AudioMetrics originalMetrics = processor.analyzeSignal(signal, SAMPLE_RATE);
            AudioSignalProcessor.AudioMetrics denoisedMetrics = processor.analyzeSignal(denoised, SAMPLE_RATE);
            
            // Fundamental frequency should be preserved
            assertThat(denoisedMetrics.getFundamentalFrequency())
                .isCloseTo(originalMetrics.getFundamentalFrequency(), within(FREQUENCY_TOLERANCE * 2));
        }
        
        @Test
        @DisplayName("Should handle different alpha values")
        void shouldHandleDifferentAlphaValues() {
            double[] signal = generatePureSignal(512, 440.0);
            double[] noise = generateNoise(256);
            double[] noisy = addNoise(signal, 0.25);
            
            // Test different over-subtraction factors
            double[] conservative = processor.reduceNoise(noisy, noise, SAMPLE_RATE, 1.0);
            double[] aggressive = processor.reduceNoise(noisy, noise, SAMPLE_RATE, 3.0);
            
            // Both should process without errors
            assertThat(conservative).hasSize(signal.length);
            assertThat(aggressive).hasSize(signal.length);
            
            // Aggressive should typically have lower energy
            double conservativeEnergy = calculateRmsEnergy(conservative);
            double aggressiveEnergy = calculateRmsEnergy(aggressive);
            
            assertThat(aggressiveEnergy).isLessThanOrEqualTo(conservativeEnergy);
        }
    }
    
    @Nested
    @DisplayName("Reverb Processing Tests")
    class ReverbProcessingTests {
        
        @Test
        @DisplayName("Should add reverb effect successfully")
        void shouldAddReverbEffectSuccessfully() {
            double[] drySignal = generatePureSignal(1024, 440.0);
            
            double[] withReverb = processor.addReverb(drySignal, 0.5, SAMPLE_RATE);
            
            assertThat(withReverb).hasSize(drySignal.length);
            
            // Reverb should change the signal characteristics
            AudioSignalProcessor.AudioMetrics dryMetrics = processor.analyzeSignal(drySignal, SAMPLE_RATE);
            AudioSignalProcessor.AudioMetrics wetMetrics = processor.analyzeSignal(withReverb, SAMPLE_RATE);
            
            // Reverb typically increases spectral spread and changes energy distribution
            assertThat(wetMetrics.getSpectralSpread())
                .isGreaterThanOrEqualTo(dryMetrics.getSpectralSpread() * 0.8); // Allow some tolerance
        }
        
        @Test
        @DisplayName("Should handle different reverb times")
        void shouldHandleDifferentReverbTimes() {
            double[] signal = generatePureSignal(512, 220.0);
            
            double[] shortReverb = processor.addReverb(signal, 0.2, SAMPLE_RATE);
            double[] longReverb = processor.addReverb(signal, 1.0, SAMPLE_RATE);
            
            assertThat(shortReverb).hasSize(signal.length);
            assertThat(longReverb).hasSize(signal.length);
            
            // Both should produce valid audio
            assertThat(calculateRmsEnergy(shortReverb)).isGreaterThan(0);
            assertThat(calculateRmsEnergy(longReverb)).isGreaterThan(0);
        }
        
        @Test
        @DisplayName("Should preserve fundamental frequency in reverb")
        void shouldPreserveFundamentalFrequencyInReverb() {
            double frequency = 330.0;
            double[] signal = generatePureSignal(1024, frequency);
            
            double[] withReverb = processor.addReverb(signal, 0.8, SAMPLE_RATE);
            
            AudioSignalProcessor.AudioMetrics originalMetrics = processor.analyzeSignal(signal, SAMPLE_RATE);
            AudioSignalProcessor.AudioMetrics reverbMetrics = processor.analyzeSignal(withReverb, SAMPLE_RATE);
            
            // Fundamental frequency should be preserved (within tolerance)
            assertThat(reverbMetrics.getFundamentalFrequency())
                .isCloseTo(originalMetrics.getFundamentalFrequency(), within(FREQUENCY_TOLERANCE * 2));
        }
    }
    
    @Nested
    @DisplayName("Performance and History Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should track processing history")
        void shouldTrackProcessingHistory() {
            double[] signal = generatePureSignal(512, 440.0);
            
            int initialCount = processor.getProcessingHistory().size();
            
            // Perform several operations
            processor.analyzeSignal(signal, SAMPLE_RATE);
            processor.applyFilter(signal, "lowpass", null, SAMPLE_RATE);
            
            assertThat(processor.getProcessingHistory()).hasSize(initialCount + 2);
        }
        
        @Test
        @DisplayName("Should provide performance statistics")
        void shouldProvidePerformanceStatistics() {
            double[] signal = generatePureSignal(1024, 440.0);
            
            // Perform multiple operations
            for (int i = 0; i < 5; i++) {
                processor.analyzeSignal(signal, SAMPLE_RATE);
            }
            
            Map<String, Double> stats = processor.getPerformanceStats();
            
            assertThat(stats).containsKey("analysis_avg_ms");
            assertThat(stats).containsKey("analysis_count");
            
            assertThat(stats.get("analysis_count")).isEqualTo(5.0);
            assertThat(stats.get("analysis_avg_ms")).isGreaterThan(0.0);
        }
        
        @Test
        @DisplayName("Should clear history correctly")
        void shouldClearHistoryCorrectly() {
            double[] signal = generatePureSignal(256, 440.0);
            
            // Generate some history
            processor.analyzeSignal(signal, SAMPLE_RATE);
            processor.applyFilter(signal, "lowpass", null, SAMPLE_RATE);
            
            assertThat(processor.getProcessingHistory()).isNotEmpty();
            
            processor.clearHistory();
            
            assertThat(processor.getProcessingHistory()).isEmpty();
        }
        
        @Test
        @DisplayName("Should process typical buffer sizes efficiently")
        void shouldProcessTypicalBufferSizesEfficiently() {
            int[] bufferSizes = {256, 512, 1024, 2048};
            
            for (int size : bufferSizes) {
                double[] signal = generatePureSignal(size, 440.0);
                
                long startTime = System.nanoTime();
                processor.analyzeSignal(signal, SAMPLE_RATE);
                processor.applyFilter(signal, "vocal", null, SAMPLE_RATE);
                long endTime = System.nanoTime();
                
                double processingTimeMs = (endTime - startTime) / 1_000_000.0;
                
                // Should complete within reasonable time (adjust based on system performance)
                assertThat(processingTimeMs).isLessThan(50.0); // 50ms threshold
            }
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle invalid filter names")
        void shouldHandleInvalidFilterNames() {
            double[] signal = generatePureSignal(256, 440.0);
            
            assertThrows(IllegalArgumentException.class, () -> {
                processor.applyFilter(signal, "invalid_filter", null, SAMPLE_RATE);
            });
        }
        
        @Test
        @DisplayName("Should handle empty signals")
        void shouldHandleEmptySignals() {
            double[] emptySignal = new double[0];
            
            // Should not crash, but may return empty or default results
            assertDoesNotThrow(() -> {
                processor.analyzeSignal(emptySignal, SAMPLE_RATE);
            });
        }
        
        @Test
        @DisplayName("Should handle very small signals")
        void shouldHandleVerySmallSignals() {
            double[] tinySignal = {1.0, -1.0}; // 2 samples
            
            assertDoesNotThrow(() -> {
                processor.analyzeSignal(tinySignal, SAMPLE_RATE);
                processor.applyFilter(tinySignal, "lowpass", null, SAMPLE_RATE);
            });
        }
    }
    
    // Utility methods for test signal generation
    
    private double[] generatePureSignal(int length, double frequency) {
        return generatePureSignal(length, frequency, 0.5);
    }
    
    private double[] generatePureSignal(int length, double frequency, double amplitude) {
        double[] signal = new double[length];
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            signal[i] = amplitude * Math.sin(2 * Math.PI * frequency * t);
        }
        return signal;
    }
    
    private double[] generateTestSignal(int length, double... frequencies) {
        double[] signal = new double[length];
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            for (double freq : frequencies) {
                signal[i] += Math.sin(2 * Math.PI * freq * t) / frequencies.length;
            }
        }
        return signal;
    }
    
    private double[] generateHarmonicSignal(int length, double fundamental, int numHarmonics) {
        double[] signal = new double[length];
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            for (int h = 1; h <= numHarmonics; h++) {
                double amplitude = 1.0 / h; // Decreasing amplitude for higher harmonics
                signal[i] += amplitude * Math.sin(2 * Math.PI * fundamental * h * t);
            }
        }
        return signal;
    }
    
    private double[] generateNoise(int length) {
        double[] noise = new double[length];
        java.util.Random random = new java.util.Random(42); // Deterministic for testing
        for (int i = 0; i < length; i++) {
            noise[i] = random.nextGaussian() * 0.1;
        }
        return noise;
    }
    
    private double[] addNoise(double[] signal, double noiseLevel) {
        double[] noisy = signal.clone();
        java.util.Random random = new java.util.Random(123);
        for (int i = 0; i < noisy.length; i++) {
            noisy[i] += random.nextGaussian() * noiseLevel;
        }
        return noisy;
    }
    
    private double calculateMeanSquaredError(double[] reference, double[] test) {
        if (reference.length != test.length) {
            throw new IllegalArgumentException("Arrays must have same length");
        }
        
        double mse = 0;
        for (int i = 0; i < reference.length; i++) {
            double error = reference[i] - test[i];
            mse += error * error;
        }
        return mse / reference.length;
    }
    
    private double calculateRmsEnergy(double[] signal) {
        double sum = 0;
        for (double sample : signal) {
            sum += sample * sample;
        }
        return Math.sqrt(sum / signal.length);
    }
}
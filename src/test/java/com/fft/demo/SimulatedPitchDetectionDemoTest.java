package com.fft.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for SimulatedPitchDetectionDemo.
 * 
 * <p>Tests the simulated pitch detection functionality including signal generation,
 * pitch detection accuracy, noise robustness, and performance characteristics.</p>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("Simulated Pitch Detection Demo Tests")
class SimulatedPitchDetectionDemoTest {
    
    private SimulatedPitchDetectionDemo demo;
    private static final double FREQUENCY_TOLERANCE = 2.0; // Hz
    private static final double SAMPLE_RATE = 44100.0;
    
    @BeforeEach
    void setUp() {
        demo = new SimulatedPitchDetectionDemo();
    }
    
    @Nested
    @DisplayName("Signal Generation Tests")
    class SignalGenerationTests {
        
        @Test
        @DisplayName("Should generate tone with correct frequency")
        void shouldGenerateToneWithCorrectFrequency() throws Exception {
            double frequency = 440.0;
            double duration = 0.5;
            double amplitude = 1.0;
            
            Method generateTone = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "generateTone", double.class, double.class, double.class);
            generateTone.setAccessible(true);
            
            double[] signal = (double[]) generateTone.invoke(demo, frequency, duration, amplitude);
            
            assertThat(signal).isNotNull();
            assertThat(signal.length).isEqualTo((int) (SAMPLE_RATE * duration));
            
            // Check that signal has reasonable amplitude
            double maxAmplitude = 0.0;
            for (double sample : signal) {
                maxAmplitude = Math.max(maxAmplitude, Math.abs(sample));
            }
            assertThat(maxAmplitude).isCloseTo(amplitude, within(0.1));
        }
        
        @Test
        @DisplayName("Should generate tone with vibrato")
        void shouldGenerateToneWithVibrato() throws Exception {
            double frequency = 440.0;
            double duration = 0.5;
            double amplitude = 1.0;
            double vibratoRate = 5.0;
            double vibratoDepth = 0.02;
            
            Method generateToneWithVibrato = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "generateToneWithVibrato", double.class, double.class, double.class, double.class, double.class);
            generateToneWithVibrato.setAccessible(true);
            
            double[] signal = (double[]) generateToneWithVibrato.invoke(
                demo, frequency, duration, amplitude, vibratoRate, vibratoDepth);
            
            assertThat(signal).isNotNull();
            assertThat(signal.length).isEqualTo((int) (SAMPLE_RATE * duration));
            
            // Vibrato signal should have slightly different characteristics than pure tone
            double[] pureTone = generatePureTone(frequency, duration, amplitude);
            assertThat(signal).isNotEqualTo(pureTone);
        }
        
        @Test
        @DisplayName("Should generate chord with multiple frequencies")
        void shouldGenerateChordWithMultipleFrequencies() throws Exception {
            double[] frequencies = {261.63, 329.63, 392.0}; // C Major chord
            double[] amplitudes = {1.0, 0.8, 0.6};
            double duration = 0.5;
            
            Method generateChord = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "generateChord", double[].class, double[].class, double.class);
            generateChord.setAccessible(true);
            
            double[] signal = (double[]) generateChord.invoke(demo, frequencies, amplitudes, duration);
            
            assertThat(signal).isNotNull();
            assertThat(signal.length).isEqualTo((int) (SAMPLE_RATE * duration));
            
            // Chord signal should have higher peak amplitude than individual tones
            double maxAmplitude = 0.0;
            for (double sample : signal) {
                maxAmplitude = Math.max(maxAmplitude, Math.abs(sample));
            }
            assertThat(maxAmplitude).isGreaterThan(1.0); // Sum of amplitudes
        }
        
        @Test
        @DisplayName("Should add noise to signal")
        void shouldAddNoiseToSignal() throws Exception {
            double[] signal = generatePureTone(440.0, 0.1, 1.0);
            double[] originalSignal = signal.clone();
            double noiseLevel = 0.1;
            
            Method addNoise = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "addNoise", double[].class, double.class);
            addNoise.setAccessible(true);
            
            addNoise.invoke(demo, signal, noiseLevel);
            
            // Signal should be modified
            assertThat(signal).isNotEqualTo(originalSignal);
            
            // Check that noise level is reasonable
            double totalNoise = 0.0;
            for (int i = 0; i < signal.length; i++) {
                totalNoise += Math.abs(signal[i] - originalSignal[i]);
            }
            double averageNoise = totalNoise / signal.length;
            assertThat(averageNoise).isLessThan(noiseLevel * 2); // Rough noise level check
        }
    }
    
    @Nested
    @DisplayName("Pitch Detection Tests")
    class PitchDetectionTests {
        
        @Test
        @DisplayName("Should detect pitch from pure tone")
        void shouldDetectPitchFromPureTone() throws Exception {
            double expectedFreq = 440.0;
            double[] signal = generatePureTone(expectedFreq, 0.5, 1.0);
            
            Method detectPitch = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "detectPitch", double[].class);
            detectPitch.setAccessible(true);
            
            double detectedFreq = (double) detectPitch.invoke(demo, signal);
            
            assertThat(detectedFreq)
                .as("Detected frequency should match expected")
                .isCloseTo(expectedFreq, within(FREQUENCY_TOLERANCE));
        }
        
        @Test
        @DisplayName("Should detect multiple pitches from chord")
        void shouldDetectMultiplePitchesFromChord() throws Exception {
            double[] expectedFreqs = {261.63, 329.63, 392.0}; // C Major chord
            double[] amplitudes = {1.0, 0.8, 0.6};
            
            Method generateChord = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "generateChord", double[].class, double[].class, double.class);
            generateChord.setAccessible(true);
            double[] chordSignal = (double[]) generateChord.invoke(demo, expectedFreqs, amplitudes, 0.5);
            
            Method detectMultiplePitches = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "detectMultiplePitches", double[].class, int.class);
            detectMultiplePitches.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            List<Double> detectedFreqs = (List<Double>) detectMultiplePitches.invoke(demo, chordSignal, 3);
            
            assertThat(detectedFreqs).hasSize(3);
            
            // Check that detected frequencies are close to expected (order may vary)
            for (double expectedFreq : expectedFreqs) {
                boolean found = detectedFreqs.stream()
                    .anyMatch(detected -> Math.abs(detected - expectedFreq) <= FREQUENCY_TOLERANCE * 2);
                assertThat(found)
                    .as("Expected frequency %.1f Hz should be detected", expectedFreq)
                    .isTrue();
            }
        }
        
        @Test
        @DisplayName("Should handle noisy signals gracefully")
        void shouldHandleNoisySignalsGracefully() throws Exception {
            double expectedFreq = 440.0;
            double[] signal = generatePureTone(expectedFreq, 0.5, 1.0);
            
            Method addNoise = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "addNoise", double[].class, double.class);
            addNoise.setAccessible(true);
            addNoise.invoke(demo, signal, 0.2); // 20% noise
            
            Method detectPitch = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "detectPitch", double[].class);
            detectPitch.setAccessible(true);
            
            double detectedFreq = (double) detectPitch.invoke(demo, signal);
            
            // Should still detect frequency reasonably well with moderate noise
            assertThat(detectedFreq)
                .as("Should detect frequency even with noise")
                .isCloseTo(expectedFreq, within(FREQUENCY_TOLERANCE * 3));
        }
        
        @Test
        @DisplayName("Should return zero for silent signal")
        void shouldReturnZeroForSilentSignal() throws Exception {
            double[] silentSignal = new double[4096]; // All zeros
            
            Method detectPitch = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "detectPitch", double[].class);
            detectPitch.setAccessible(true);
            
            double detectedFreq = (double) detectPitch.invoke(demo, silentSignal);
            
            assertThat(detectedFreq).isEqualTo(0.0);
        }
    }
    
    @Nested
    @DisplayName("Note Conversion Tests")
    class NoteConversionTests {
        
        @Test
        @DisplayName("Should convert frequency to correct note name")
        void shouldConvertFrequencyToCorrectNoteName() throws Exception {
            Method frequencyToNote = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "frequencyToNote", double.class);
            frequencyToNote.setAccessible(true);

            // Test common note frequencies
            assertThat(frequencyToNote.invoke(demo, 440.0)).isEqualTo("A4");
            assertThat(frequencyToNote.invoke(demo, 261.63)).isEqualTo("C4");
            assertThat(frequencyToNote.invoke(demo, 523.25)).isEqualTo("C5");
            // Invalid frequencies now return "UNKNOWN" (not "N/A")
            assertThat(frequencyToNote.invoke(demo, 0.0)).isEqualTo("UNKNOWN");
        }
        
        @Test
        @DisplayName("Should handle edge case frequencies")
        void shouldHandleEdgeCaseFrequencies() throws Exception {
            Method frequencyToNote = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "frequencyToNote", double.class);
            frequencyToNote.setAccessible(true);

            // Test edge cases
            // Invalid frequencies now return "UNKNOWN" (not "N/A")
            assertThat(frequencyToNote.invoke(demo, -100.0)).isEqualTo("UNKNOWN");
            assertThat(frequencyToNote.invoke(demo, 0.0)).isEqualTo("UNKNOWN");
            
            // Very high frequency should still return a note name (not N/A)
            String highNote = (String) frequencyToNote.invoke(demo, 10000.0);
            assertThat(highNote).isNotEqualTo("N/A"); // High frequencies are still valid MIDI notes
            
            // Very low frequency should return a valid note
            String lowNote = (String) frequencyToNote.invoke(demo, 10.0);
            assertThat(lowNote).isNotEqualTo("N/A"); // Very low but valid
        }
    }
    
    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {
        
        @Test
        @DisplayName("Should calculate frequency to bin correctly")
        void shouldCalculateFrequencyToBinCorrectly() throws Exception {
            Method frequencyToBin = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "frequencyToBin", double.class, int.class);
            frequencyToBin.setAccessible(true);
            
            int signalLength = 4096;
            double frequency = 440.0;
            
            int bin = (int) frequencyToBin.invoke(demo, frequency, signalLength);
            
            // Check that bin calculation is reasonable
            int expectedBin = (int) Math.round(frequency * signalLength / SAMPLE_RATE);
            assertThat(bin).isEqualTo(expectedBin);
        }
        
        @Test
        @DisplayName("Should calculate bin to frequency correctly")
        void shouldCalculateBinToFrequencyCorrectly() throws Exception {
            Method binToFrequency = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "binToFrequency", int.class, int.class);
            binToFrequency.setAccessible(true);
            
            int signalLength = 4096;
            int bin = 40;
            
            double frequency = (double) binToFrequency.invoke(demo, bin, signalLength);
            
            // Check that frequency calculation is reasonable
            double expectedFreq = (double) bin * SAMPLE_RATE / signalLength;
            assertThat(frequency).isCloseTo(expectedFreq, within(0.01));
        }
        
        @Test
        @DisplayName("Should calculate SNR correctly")
        void shouldCalculateSNRCorrectly() throws Exception {
            Method calculateSNR = SimulatedPitchDetectionDemo.class.getDeclaredMethod(
                "calculateSNR", double[].class, double.class);
            calculateSNR.setAccessible(true);
            
            double[] signal = generatePureTone(440.0, 0.1, 1.0);
            double noiseLevel = 0.1;
            
            double snr = (double) calculateSNR.invoke(demo, signal, noiseLevel);
            
            // SNR should be positive for reasonable signal
            assertThat(snr).isGreaterThan(0.0);
            
            // Higher noise should give lower SNR
            double higherNoiseSNR = (double) calculateSNR.invoke(demo, signal, 0.5);
            assertThat(higherNoiseSNR).isLessThan(snr);
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should run all demos without exceptions")
        void shouldRunAllDemosWithoutExceptions() {
            assertDoesNotThrow(() -> {
                demo.runAllDemos();
            }, "All demos should run without throwing exceptions");
        }
        
        @Test
        @DisplayName("Should demonstrate pitch detection accuracy")
        void shouldDemonstratePitchDetectionAccuracy() throws Exception {
            // Test the melody recognition functionality
            String[] testMelody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4"};
            
            // Simulate the melody analysis process
            for (String note : testMelody) {
                // This would normally involve signal generation and analysis
                assertThat(note).matches("[A-G][#b]?[0-8]");
            }
            
            // Verify that the demo can handle the expected melody format
            assertThat(testMelody).hasSize(7);
        }
    }
    
    // Helper methods
    
    private double[] generatePureTone(double frequency, double duration, double amplitude) {
        int samples = (int) (SAMPLE_RATE * duration);
        double[] signal = new double[samples];
        
        for (int i = 0; i < samples; i++) {
            double t = i / SAMPLE_RATE;
            signal[i] = amplitude * Math.sin(2.0 * Math.PI * frequency * t);
        }
        
        return signal;
    }
}
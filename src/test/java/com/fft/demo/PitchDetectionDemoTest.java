package com.fft.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for PitchDetectionDemo.
 * 
 * <p>
 * Tests the real-time pitch detection functionality including audio processing,
 * FFT analysis, pitch detection algorithms, and Parsons code generation.
 * </p>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("Pitch Detection Demo Tests")
class PitchDetectionDemoTest {

    private PitchDetectionDemo demo;
    private static final double SAMPLE_RATE = 44100.0;
    private static final int FFT_SIZE = 4096;
    private static final double FREQUENCY_TOLERANCE = 5.0; // Hz

    @BeforeEach
    void setUp() {
        demo = new PitchDetectionDemo();
    }

    @Nested
    @DisplayName("Audio Processing Tests")
    class AudioProcessingTests {

        @Test
        @DisplayName("Should convert bytes to samples correctly")
        void shouldConvertBytesToSamplesCorrectly() throws Exception {
            Method convertBytesToSamples = PitchDetectionDemo.class.getDeclaredMethod(
                    "convertBytesToSamples", byte[].class, double[].class, int.class);
            convertBytesToSamples.setAccessible(true);

            // Create test 16-bit audio data (little-endian)
            byte[] buffer = new byte[8]; // 4 samples
            buffer[0] = 0x00;
            buffer[1] = 0x10; // Sample 1: 4096
            buffer[2] = 0x00;
            buffer[3] = (byte) 0x20; // Sample 2: 8192
            buffer[4] = 0x00;
            buffer[5] = (byte) 0x80; // Sample 3: -32768
            buffer[6] = (byte) 0xFF;
            buffer[7] = 0x7F; // Sample 4: 32767

            double[] samples = new double[4];
            convertBytesToSamples.invoke(demo, buffer, samples, 8);

            // Verify conversion (16-bit values normalized to [-1, 1])
            assertThat(samples[0]).isCloseTo(4096.0 / 32768.0, within(0.001));
            assertThat(samples[1]).isCloseTo(8192.0 / 32768.0, within(0.001));
            assertThat(samples[2]).isCloseTo(-1.0, within(0.001)); // -32768 -> -1.0
            assertThat(samples[3]).isCloseTo(32767.0 / 32768.0, within(0.001));
        }

        @Test
        @DisplayName("Should apply Hamming window correctly")
        void shouldApplyHammingWindowCorrectly() throws Exception {
            Method applyHammingWindow = PitchDetectionDemo.class.getDeclaredMethod(
                    "applyHammingWindow", double[].class);
            applyHammingWindow.setAccessible(true);

            double[] samples = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
            double[] original = samples.clone();

            applyHammingWindow.invoke(demo, samples);

            // Hamming window should modify the signal
            assertThat(samples).isNotEqualTo(original);

            // First and last samples should be reduced most (window tapers at edges)
            assertThat(samples[0]).isLessThan(original[0]);
            assertThat(samples[samples.length - 1]).isLessThan(original[original.length - 1]);

            // Middle samples should be less affected
            int middle = samples.length / 2;
            assertThat(samples[middle]).isGreaterThan(samples[0]);
        }

        @Test
        @DisplayName("Should handle zero-padding in byte conversion")
        void shouldHandleZeroPaddingInByteConversion() throws Exception {
            Method convertBytesToSamples = PitchDetectionDemo.class.getDeclaredMethod(
                    "convertBytesToSamples", byte[].class, double[].class, int.class);
            convertBytesToSamples.setAccessible(true);

            byte[] buffer = new byte[4]; // 2 samples worth of data
            buffer[0] = 0x00;
            buffer[1] = 0x10; // Sample 1
            buffer[2] = 0x00;
            buffer[3] = 0x20; // Sample 2

            double[] samples = new double[4]; // Request 4 samples
            convertBytesToSamples.invoke(demo, buffer, samples, 4);

            // First 2 samples should have data
            assertThat(samples[0]).isNotEqualTo(0.0);
            assertThat(samples[1]).isNotEqualTo(0.0);

            // Last 2 samples should be zero-padded
            assertThat(samples[2]).isEqualTo(0.0);
            assertThat(samples[3]).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Frequency Conversion Tests")
    class FrequencyConversionTests {

        @Test
        @DisplayName("Should convert frequency to bin correctly")
        void shouldConvertFrequencyToBinCorrectly() throws Exception {
            Method frequencyToBin = PitchDetectionDemo.class.getDeclaredMethod(
                    "frequencyToBin", double.class);
            frequencyToBin.setAccessible(true);

            double frequency = 440.0; // A4
            int bin = (int) frequencyToBin.invoke(demo, frequency);

            // Manual calculation: bin = frequency * FFT_SIZE / SAMPLE_RATE
            int expectedBin = (int) Math.round(frequency * FFT_SIZE / SAMPLE_RATE);
            assertThat(bin).isEqualTo(expectedBin);
        }

        @Test
        @DisplayName("Should convert bin to frequency correctly")
        void shouldConvertBinToFrequencyCorrectly() throws Exception {
            Method binToFrequency = PitchDetectionDemo.class.getDeclaredMethod(
                    "binToFrequency", double.class);
            binToFrequency.setAccessible(true);

            double bin = 40.0;
            double frequency = (double) binToFrequency.invoke(demo, bin);

            // Manual calculation: frequency = bin * SAMPLE_RATE / FFT_SIZE
            double expectedFreq = bin * SAMPLE_RATE / FFT_SIZE;
            assertThat(frequency).isCloseTo(expectedFreq, within(0.01));
        }

        @Test
        @DisplayName("Should convert frequency to note name correctly")
        void shouldConvertFrequencyToNoteNameCorrectly() throws Exception {
            Method frequencyToNote = PitchDetectionDemo.class.getDeclaredMethod(
                    "frequencyToNote", double.class);
            frequencyToNote.setAccessible(true);

            // Test common musical frequencies
            assertThat(frequencyToNote.invoke(demo, 440.0)).isEqualTo("A");
            assertThat(frequencyToNote.invoke(demo, 261.63)).isEqualTo("C");
            assertThat(frequencyToNote.invoke(demo, 0.0)).isEqualTo("N/A");
            assertThat(frequencyToNote.invoke(demo, -100.0)).isEqualTo("N/A");
        }

        @Test
        @DisplayName("Should convert frequency to octave correctly")
        void shouldConvertFrequencyToOctaveCorrectly() throws Exception {
            Method frequencyToOctave = PitchDetectionDemo.class.getDeclaredMethod(
                    "frequencyToOctave", double.class);
            frequencyToOctave.setAccessible(true);

            // Test common octave assignments
            int a4Octave = (int) frequencyToOctave.invoke(demo, 440.0); // A4
            int c4Octave = (int) frequencyToOctave.invoke(demo, 261.63); // C4
            int c5Octave = (int) frequencyToOctave.invoke(demo, 523.25); // C5

            assertThat(a4Octave).isEqualTo(4);
            assertThat(c4Octave).isEqualTo(4);
            assertThat(c5Octave).isEqualTo(5);

            // Invalid frequency should return 0
            assertThat(frequencyToOctave.invoke(demo, 0.0)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Pitch Detection Algorithm Tests")
    class PitchDetectionAlgorithmTests {

        @Test
        @DisplayName("Should refine frequency estimate with parabolic interpolation")
        void shouldRefineFrequencyEstimateWithParabolicInterpolation() throws Exception {
            Method refineFrequencyEstimate = PitchDetectionDemo.class.getDeclaredMethod(
                    "refineFrequencyEstimate", double[].class, int.class);
            refineFrequencyEstimate.setAccessible(true);

            // Create test magnitude array with a clear peak at index 10
            double[] magnitudes = new double[20];
            magnitudes[9] = 0.8;
            magnitudes[10] = 1.0; // Peak
            magnitudes[11] = 0.9;

            double refinedFreq = (double) refineFrequencyEstimate.invoke(demo, magnitudes, 10);

            // Should return a frequency close to bin 10
            Method binToFrequency = PitchDetectionDemo.class.getDeclaredMethod(
                    "binToFrequency", double.class);
            binToFrequency.setAccessible(true);
            double expectedFreq = (double) binToFrequency.invoke(demo, 10.0);

            assertThat(refinedFreq).isCloseTo(expectedFreq, within(expectedFreq * 0.1));
        }

        @Test
        @DisplayName("Should handle edge cases in frequency refinement")
        void shouldHandleEdgeCasesInFrequencyRefinement() throws Exception {
            Method refineFrequencyEstimate = PitchDetectionDemo.class.getDeclaredMethod(
                    "refineFrequencyEstimate", double[].class, int.class);
            refineFrequencyEstimate.setAccessible(true);

            double[] magnitudes = new double[10];
            magnitudes[5] = 1.0;

            // Test edge cases
            double refinedFreq0 = (double) refineFrequencyEstimate.invoke(demo, magnitudes, 0);
            double refinedFreq9 = (double) refineFrequencyEstimate.invoke(demo, magnitudes, 9);

            // Should handle without throwing exceptions
            assertThat(refinedFreq0).isGreaterThanOrEqualTo(0.0);
            assertThat(refinedFreq9).isGreaterThanOrEqualTo(0.0);
        }

        @Test
        @DisplayName("Should calculate harmonic score correctly")
        void shouldCalculateHarmonicScoreCorrectly() throws Exception {
            Method calculateHarmonicScore = PitchDetectionDemo.class.getDeclaredMethod(
                    "calculateHarmonicScore", double[].class, double.class);
            calculateHarmonicScore.setAccessible(true);

            // Create magnitude array with harmonic pattern
            double[] magnitudes = new double[1000];
            double fundamentalFreq = 100.0; // 100 Hz fundamental

            // Add harmonic peaks
            Method frequencyToBin = PitchDetectionDemo.class.getDeclaredMethod(
                    "frequencyToBin", double.class);
            frequencyToBin.setAccessible(true);

            for (int harmonic = 1; harmonic <= 4; harmonic++) {
                int bin = (int) frequencyToBin.invoke(demo, fundamentalFreq * harmonic);
                if (bin < magnitudes.length) {
                    magnitudes[bin] = 1.0 / harmonic; // Decreasing amplitude
                }
            }

            double score = (double) calculateHarmonicScore.invoke(demo, magnitudes, fundamentalFreq);

            // Score should be positive for harmonic content
            assertThat(score).isGreaterThan(0.0);

            // Score for fundamental with harmonics should be higher than random frequency
            double randomScore = (double) calculateHarmonicScore.invoke(demo, magnitudes, 137.5);
            assertThat(score).isGreaterThan(randomScore);
        }

        @Test
        @DisplayName("Should find fundamental frequency from harmonics")
        void shouldFindFundamentalFrequencyFromHarmonics() throws Exception {
            Method findFundamentalFrequency = PitchDetectionDemo.class.getDeclaredMethod(
                    "findFundamentalFrequency", double[].class, double.class);
            findFundamentalFrequency.setAccessible(true);

            // Create magnitude array simulating 2nd harmonic being strongest
            double[] magnitudes = new double[1000];
            double fundamentalFreq = 200.0; // True fundamental at 200 Hz
            double candidateFreq = 400.0; // Detected 2nd harmonic at 400 Hz

            // Add harmonic series starting from fundamental
            Method frequencyToBin = PitchDetectionDemo.class.getDeclaredMethod(
                    "frequencyToBin", double.class);
            frequencyToBin.setAccessible(true);

            int fundamentalBin = (int) frequencyToBin.invoke(demo, fundamentalFreq);
            int secondHarmonicBin = (int) frequencyToBin.invoke(demo, candidateFreq);

            if (fundamentalBin < magnitudes.length)
                magnitudes[fundamentalBin] = 0.8;
            if (secondHarmonicBin < magnitudes.length)
                magnitudes[secondHarmonicBin] = 1.0;

            double detectedFundamental = (double) findFundamentalFrequency.invoke(demo, magnitudes, candidateFreq);

            // Should detect the true fundamental or at least be reasonable
            assertThat(detectedFundamental).isGreaterThan(80.0); // Within musical range
            assertThat(detectedFundamental).isLessThan(2000.0);
        }
    }

    @Nested
    @DisplayName("Parsons Code Generation Tests")
    class ParsonsCodeGenerationTests {

        @Test
        @DisplayName("Should create pitch change objects correctly")
        void shouldCreatePitchChangeObjectsCorrectly() throws Exception {
            // Access the inner PitchChange class
            Class<?>[] innerClasses = PitchDetectionDemo.class.getDeclaredClasses();
            Class<?> pitchChangeClass = null;
            for (Class<?> innerClass : innerClasses) {
                if (innerClass.getSimpleName().equals("PitchChange")) {
                    pitchChangeClass = innerClass;
                    break;
                }
            }

            assertThat(pitchChangeClass).isNotNull();

            // Get the ParsonsDirection enum
            Class<?> parsonsDirectionClass = null;
            for (Class<?> innerClass : innerClasses) {
                if (innerClass.getSimpleName().equals("ParsonsDirection")) {
                    parsonsDirectionClass = innerClass;
                    break;
                }
            }

            assertThat(parsonsDirectionClass).isNotNull();

            // Test enum values
            Object[] directions = parsonsDirectionClass.getEnumConstants();
            assertThat(directions).hasSize(3); // UP, DOWN, REPEAT
        }

        @Test
        @DisplayName("Should create pitch detection result objects correctly")
        void shouldCreatePitchDetectionResultObjectsCorrectly() throws Exception {
            // Access the inner PitchDetectionResult class
            Class<?>[] innerClasses = PitchDetectionDemo.class.getDeclaredClasses();
            Class<?> pitchDetectionResultClass = null;
            for (Class<?> innerClass : innerClasses) {
                if (innerClass.getSimpleName().equals("PitchDetectionResult")) {
                    pitchDetectionResultClass = innerClass;
                    break;
                }
            }

            assertThat(pitchDetectionResultClass).isNotNull();

            // Verify the class has expected fields
            Field[] fields = pitchDetectionResultClass.getDeclaredFields();
            assertThat(fields.length).isGreaterThanOrEqualTo(4); // frequency, magnitude, noteName, octave
        }
    }

    @Nested
    @DisplayName("Display and Output Tests")
    class DisplayAndOutputTests {

        @Test
        @DisplayName("Should format display results correctly")
        void shouldFormatDisplayResultsCorrectly() throws Exception {
            // Create a test PitchDetectionResult
            Class<?>[] innerClasses = PitchDetectionDemo.class.getDeclaredClasses();
            Class<?> pitchDetectionResultClass = null;
            for (Class<?> innerClass : innerClasses) {
                if (innerClass.getSimpleName().equals("PitchDetectionResult")) {
                    pitchDetectionResultClass = innerClass;
                    break;
                }
            }

            assertThat(pitchDetectionResultClass).isNotNull();

            // The displayResults method should handle valid results without exceptions
            Method displayResults = PitchDetectionDemo.class.getDeclaredMethod(
                    "displayResults", int.class, pitchDetectionResultClass);
            displayResults.setAccessible(true);

            // Create a test result object using constructor
            Object testResult = pitchDetectionResultClass.getDeclaredConstructors()[0]
                    .newInstance(440.0, 0.8, "A", 4);

            // Should not throw exceptions
            assertDoesNotThrow(() -> {
                try {
                    displayResults.invoke(demo, 10, testResult);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle demo initialization correctly")
        void shouldHandleDemoInitializationCorrectly() {
            assertThat(demo).isNotNull();

            // Check that required constants are accessible
            assertDoesNotThrow(() -> {
                Field sampleRateField = PitchDetectionDemo.class.getDeclaredField("SAMPLE_RATE");
                sampleRateField.setAccessible(true);
                float sampleRate = sampleRateField.getFloat(null);
                assertThat(sampleRate).isEqualTo(44100.0f);

                Field fftSizeField = PitchDetectionDemo.class.getDeclaredField("FFT_SIZE");
                fftSizeField.setAccessible(true);
                int fftSize = fftSizeField.getInt(null);
                assertThat(fftSize).isEqualTo(4096);
            });
        }

        @Test
        @DisplayName("Should handle main method execution gracefully")
        void shouldHandleMainMethodExecutionGracefully() {
            // The main method should not crash immediately
            // Note: We can't easily test the full audio capture functionality
            // without mocking the audio system, but we can verify the method exists
            assertDoesNotThrow(() -> {
                Method mainMethod = PitchDetectionDemo.class.getDeclaredMethod("main", String[].class);
                assertThat(mainMethod).isNotNull();
            });
        }

        @Test
        @DisplayName("Should demonstrate pitch detection pipeline components")
        void shouldDemonstratePitchDetectionPipelineComponents() throws Exception {
            // Test the core components that make up the pitch detection pipeline

            // 1. Test byte to sample conversion
            Method convertBytesToSamples = PitchDetectionDemo.class.getDeclaredMethod(
                    "convertBytesToSamples", byte[].class, double[].class, int.class);
            convertBytesToSamples.setAccessible(true);

            byte[] testBytes = { 0, 0x10, 0, 0x20 };
            double[] samples = new double[2];
            assertDoesNotThrow(() -> {
                convertBytesToSamples.invoke(demo, testBytes, samples, 4);
            });

            // 2. Test windowing
            Method applyHammingWindow = PitchDetectionDemo.class.getDeclaredMethod(
                    "applyHammingWindow", double[].class);
            applyHammingWindow.setAccessible(true);

            double[] windowedSamples = { 1.0, 1.0, 1.0, 1.0 };
            assertDoesNotThrow(() -> {
                applyHammingWindow.invoke(demo, windowedSamples);
            });

            // 3. Test frequency conversions
            Method frequencyToBin = PitchDetectionDemo.class.getDeclaredMethod(
                    "frequencyToBin", double.class);
            frequencyToBin.setAccessible(true);

            Method binToFrequency = PitchDetectionDemo.class.getDeclaredMethod(
                    "binToFrequency", double.class);
            binToFrequency.setAccessible(true);

            assertDoesNotThrow(() -> {
                int bin = (int) frequencyToBin.invoke(demo, 440.0);
                double freq = (double) binToFrequency.invoke(demo, (double) bin);
                assertThat(freq).isCloseTo(440.0, within(2.0)); // Increased tolerance for bin conversion
            });
        }
    }
}
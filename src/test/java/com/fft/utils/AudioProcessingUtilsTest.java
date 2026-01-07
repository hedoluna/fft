package com.fft.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for AudioProcessingUtils.
 * Tests audio signal processing: windows, RMS, voicing, normalization, etc.
 */
@DisplayName("AudioProcessingUtils Test Suite")
class AudioProcessingUtilsTest {

    private static final double EPSILON = 1e-10;

    // ========== Hamming Window Tests ==========

    @Test
    @DisplayName("Hamming window should apply without error to valid samples")
    void testHammingWindowValidInput() {
        double[] samples = {1.0, 0.5, -0.5, -1.0, 0.0};
        double[] original = samples.clone();

        AudioProcessingUtils.applyHammingWindow(samples);

        // Window should reduce amplitude but not change sign pattern
        assertThat(samples[0]).isLessThan(original[0]);
        assertThat(samples[4]).isEqualTo(0.0); // Hamming window goes to 0 at edges
    }

    @Test
    @DisplayName("Hamming window should handle null input gracefully")
    void testHammingWindowNullInput() {
        // Should not throw
        assertThatCode(() -> AudioProcessingUtils.applyHammingWindow(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Hamming window should handle empty array gracefully")
    void testHammingWindowEmptyArray() {
        double[] samples = new double[0];
        assertThatCode(() -> AudioProcessingUtils.applyHammingWindow(samples))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Hamming window should produce values in [0, 1] range")
    void testHammingWindowValueRange() {
        double[] samples = new double[1024];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = 1.0; // Start with 1.0
        }

        AudioProcessingUtils.applyHammingWindow(samples);

        for (double sample : samples) {
            assertThat(sample)
                    .isBetween(0.0, 1.0); // Window coefficients are in [0, 1]
        }
    }

    @Test
    @DisplayName("Hamming window should taper edges")
    void testHammingWindowEdgeTapering() {
        double[] samples = new double[1024];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = 1.0;
        }

        AudioProcessingUtils.applyHammingWindow(samples);

        // Edge samples should be significantly reduced
        assertThat(samples[0])
                .isLessThan(0.1); // Very small at start
        assertThat(samples[samples.length - 1])
                .isLessThan(0.1); // Very small at end

        // Middle sample should be closer to 1.0
        assertThat(samples[512])
                .isGreaterThan(0.99);
    }

    // ========== Hann Window Tests ==========

    @Test
    @DisplayName("Hann window should apply without error to valid samples")
    void testHannWindowValidInput() {
        double[] samples = {1.0, 0.5, -0.5, -1.0, 0.0};
        double[] original = samples.clone();

        AudioProcessingUtils.applyHannWindow(samples);

        assertThat(samples[0]).isLessThan(original[0]);
    }

    @Test
    @DisplayName("Hann window should handle null input gracefully")
    void testHannWindowNullInput() {
        assertThatCode(() -> AudioProcessingUtils.applyHannWindow(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Hann window should go to exactly 0 at edges")
    void testHannWindowGoesToZeroAtEdges() {
        double[] samples = new double[100];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = 1.0;
        }

        AudioProcessingUtils.applyHannWindow(samples);

        // Hann window goes completely to 0 at edges
        assertThat(samples[0]).isCloseTo(0.0, within(EPSILON));
        assertThat(samples[samples.length - 1]).isCloseTo(0.0, within(EPSILON));
    }

    // ========== Rectangular Window Tests ==========

    @Test
    @DisplayName("Rectangular window should be no-op (all 1.0)")
    void testRectangularWindowNoOp() {
        double[] samples = {1.0, 0.5, -0.5, -1.0, 0.0};
        double[] original = samples.clone();

        AudioProcessingUtils.applyRectangularWindow(samples);

        assertThat(samples)
                .isEqualTo(original); // No modification
    }

    // ========== RMS Calculation Tests ==========

    @Test
    @DisplayName("RMS of zeros should be 0")
    void testRMSOfZeros() {
        double[] samples = {0.0, 0.0, 0.0, 0.0, 0.0};
        double rms = AudioProcessingUtils.calculateRMS(samples);

        assertThat(rms)
                .isCloseTo(0.0, within(EPSILON));
    }

    @Test
    @DisplayName("RMS of constant value 1.0 should be 1.0")
    void testRMSOfOnes() {
        double[] samples = {1.0, 1.0, 1.0, 1.0, 1.0};
        double rms = AudioProcessingUtils.calculateRMS(samples);

        assertThat(rms)
                .isCloseTo(1.0, within(EPSILON));
    }

    @Test
    @DisplayName("RMS of ±1.0 alternating should be 1.0")
    void testRMSOfAlternating() {
        double[] samples = {1.0, -1.0, 1.0, -1.0, 1.0};
        double rms = AudioProcessingUtils.calculateRMS(samples);

        assertThat(rms)
                .isCloseTo(1.0, within(EPSILON));
    }

    @Test
    @DisplayName("RMS should be sqrt of mean of squares")
    void testRMSFormula() {
        double[] samples = {1.0, 2.0, 3.0, 4.0, 5.0};
        double expectedRMS = Math.sqrt((1.0 + 4.0 + 9.0 + 16.0 + 25.0) / 5.0); // sqrt(11)

        double rms = AudioProcessingUtils.calculateRMS(samples);

        assertThat(rms)
                .isCloseTo(expectedRMS, within(EPSILON));
    }

    @Test
    @DisplayName("RMS of null should be 0")
    void testRMSOfNull() {
        assertThat(AudioProcessingUtils.calculateRMS(null))
                .isCloseTo(0.0, within(EPSILON));
    }

    @Test
    @DisplayName("RMS of empty array should be 0")
    void testRMSOfEmptyArray() {
        assertThat(AudioProcessingUtils.calculateRMS(new double[0]))
                .isCloseTo(0.0, within(EPSILON));
    }

    // ========== Voicing Detection Tests ==========

    @Test
    @DisplayName("Silence (low RMS) should not be voiced")
    void testSilenceNotVoiced() {
        double[] samples = new double[1024];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = 0.00001; // Very quiet
        }

        assertThat(AudioProcessingUtils.isVoiced(samples))
                .isFalse();
    }

    @Test
    @DisplayName("Normal speech (moderate RMS) should be voiced")
    void testSpeechVoiced() {
        double[] samples = new double[1024];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = 0.1; // Moderate amplitude
        }

        assertThat(AudioProcessingUtils.isVoiced(samples))
                .isTrue();
    }

    @Test
    @DisplayName("Loud speech (high RMS) should be confidently voiced")
    void testLoudSpeechConfidentlyVoiced() {
        double[] samples = new double[1024];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = 0.5; // High amplitude
        }

        assertThat(AudioProcessingUtils.isConfidentlyVoiced(samples))
                .isTrue();
    }

    @Test
    @DisplayName("Confident voicing requires higher threshold than basic voicing")
    void testConfidentVoicingHigherThreshold() {
        double[] samples = new double[1024];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = 0.005; // Between basic and confident threshold
        }

        // Should be voiced but not confidently
        assertThat(AudioProcessingUtils.isVoiced(samples))
                .isTrue();
        assertThat(AudioProcessingUtils.isConfidentlyVoiced(samples))
                .isFalse();
    }

    // ========== Normalization Tests ==========

    @Test
    @DisplayName("Normalization should scale to peak of 1.0")
    void testNormalizationScalesToPeak() {
        double[] samples = {0.5, 0.25, -0.5, 0.3, 0.1};
        double maxBefore = 0.5;

        AudioProcessingUtils.normalize(samples);

        double maxAfter = 0.0;
        for (double sample : samples) {
            maxAfter = Math.max(maxAfter, Math.abs(sample));
        }

        assertThat(maxAfter)
                .isCloseTo(1.0, within(EPSILON));
    }

    @Test
    @DisplayName("Normalization of silent signal should be no-op")
    void testNormalizationOfSilence() {
        double[] samples = {0.0, 0.0, 0.0, 0.0};
        double[] original = samples.clone();

        AudioProcessingUtils.normalize(samples);

        assertThat(samples)
                .isEqualTo(original);
    }

    @Test
    @DisplayName("Normalization should preserve sign")
    void testNormalizationPreservesSign() {
        double[] samples = {-0.5, 0.25, -0.5, 0.3};

        AudioProcessingUtils.normalize(samples);

        assertThat(samples[0])
                .isNegative();
        assertThat(samples[1])
                .isPositive();
    }

    @Test
    @DisplayName("Normalization should handle null gracefully")
    void testNormalizationNullInput() {
        assertThatCode(() -> AudioProcessingUtils.normalize(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Normalization should handle empty array gracefully")
    void testNormalizationEmptyArray() {
        double[] samples = new double[0];
        assertThatCode(() -> AudioProcessingUtils.normalize(samples))
                .doesNotThrowAnyException();
    }

    // ========== Scaling Tests ==========

    @Test
    @DisplayName("Scaling by 2.0 should double all samples")
    void testScalingByTwo() {
        double[] samples = {0.5, 0.25, -0.5, 0.3};
        double[] original = samples.clone();

        AudioProcessingUtils.scale(samples, 2.0);

        for (int i = 0; i < samples.length; i++) {
            assertThat(samples[i])
                    .isCloseTo(original[i] * 2.0, within(EPSILON));
        }
    }

    @Test
    @DisplayName("Scaling by 1.0 should be no-op")
    void testScalingByOne() {
        double[] samples = {0.5, 0.25, -0.5, 0.3};
        double[] original = samples.clone();

        AudioProcessingUtils.scale(samples, 1.0);

        assertThat(samples)
                .isEqualTo(original);
    }

    @Test
    @DisplayName("Scaling by 0.5 should halve all samples")
    void testScalingByHalf() {
        double[] samples = {0.5, 0.25, -0.5, 0.3};
        double[] original = samples.clone();

        AudioProcessingUtils.scale(samples, 0.5);

        for (int i = 0; i < samples.length; i++) {
            assertThat(samples[i])
                    .isCloseTo(original[i] * 0.5, within(EPSILON));
        }
    }

    @Test
    @DisplayName("Scaling by 0.0 should silence")
    void testScalingByZero() {
        double[] samples = {0.5, 0.25, -0.5, 0.3};

        AudioProcessingUtils.scale(samples, 0.0);

        for (double sample : samples) {
            assertThat(sample)
                    .isCloseTo(0.0, within(EPSILON));
        }
    }

    @Test
    @DisplayName("Scaling null input should be no-op")
    void testScalingNullInput() {
        assertThatCode(() -> AudioProcessingUtils.scale(null, 2.0))
                .doesNotThrowAnyException();
    }

    // ========== Byte Conversion Tests ==========

    @Test
    @DisplayName("Sample to byte conversion should map [0, 1] to [0, 127]")
    void testSamplesToBytesPositive() {
        double[] samples = {0.0, 0.5, 1.0};
        byte[] bytes = AudioProcessingUtils.samplesToBytes(samples);

        assertThat(bytes[0])
                .isEqualTo((byte) 0);
        assertThat(bytes[1])
                .isEqualTo((byte) (0.5 * 127)); // ~63
        assertThat(bytes[2])
                .isEqualTo((byte) 127);
    }

    @Test
    @DisplayName("Sample to byte conversion should map [-1, 0] to approximately [-127, 0]")
    void testSamplesToByteNegative() {
        double[] samples = {-1.0, -0.5, 0.0};
        byte[] bytes = AudioProcessingUtils.samplesToBytes(samples);

        // Implementation multiplies by 127, so -1.0 * 127 = -127 (not -128)
        assertThat(bytes[0])
                .isEqualTo((byte) -127);
        assertThat(bytes[1])
                .isEqualTo((byte) (-0.5 * 127)); // ~-63
        assertThat(bytes[2])
                .isEqualTo((byte) 0);
    }

    @Test
    @DisplayName("Byte to sample conversion should map [0, 127] to [0, 1]")
    void testBytesToSamplesPositive() {
        byte[] bytes = {0, 64, 127};
        double[] samples = AudioProcessingUtils.bytesToSamples(bytes);

        assertThat(samples[0])
                .isCloseTo(0.0, within(0.01));
        assertThat(samples[1])
                .isCloseTo(64.0 / 128.0, within(0.01)); // 0.5
        assertThat(samples[2])
                .isCloseTo(127.0 / 128.0, within(0.01)); // ~0.992
    }

    @Test
    @DisplayName("Conversion round trip should be approximately accurate")
    void testConversionRoundTrip() {
        double[] original = {0.1, 0.5, 0.9, -0.1, -0.5, -0.9};
        byte[] bytes = AudioProcessingUtils.samplesToBytes(original);
        double[] recovered = AudioProcessingUtils.bytesToSamples(bytes);

        for (int i = 0; i < original.length; i++) {
            assertThat(recovered[i])
                    .isCloseTo(original[i], within(0.01)); // ~1% tolerance from byte quantization
        }
    }

    @Test
    @DisplayName("bytesToSamples with destination array should work")
    void testBytesToSamplesWithDestination() {
        byte[] bytes = {0, 64, 127};
        double[] samples = new double[3];

        AudioProcessingUtils.bytesToSamples(bytes, samples);

        assertThat(samples[0])
                .isCloseTo(0.0, within(0.01));
        assertThat(samples[1])
                .isCloseTo(0.5, within(0.01));
    }

    @Test
    @DisplayName("bytesToSamples with null destination should be no-op")
    void testBytesToSamplesNullDestination() {
        byte[] bytes = {0, 64, 127};
        assertThatCode(() -> AudioProcessingUtils.bytesToSamples(bytes, null))
                .doesNotThrowAnyException();
    }

    // ========== Clipping Tests ==========

    @Test
    @DisplayName("Hard clipping should bound values at clip level")
    void testHardClipping() {
        double[] samples = {0.5, 0.9, 1.5, -0.5, -1.5};
        double clipLevel = 1.0;

        AudioProcessingUtils.clip(samples, clipLevel, false);

        assertThat(samples[0])
                .isCloseTo(0.5, within(EPSILON));
        assertThat(samples[1])
                .isCloseTo(0.9, within(EPSILON));
        assertThat(samples[2])
                .isCloseTo(1.0, within(EPSILON)); // Clipped to 1.0
        assertThat(samples[3])
                .isCloseTo(-0.5, within(EPSILON));
        assertThat(samples[4])
                .isCloseTo(-1.0, within(EPSILON)); // Clipped to -1.0
    }

    @Test
    @DisplayName("Soft clipping should smooth values using tanh")
    void testSoftClipping() {
        double[] samples = {0.5, 1.0, 2.0};
        double clipLevel = 1.0;

        AudioProcessingUtils.clip(samples, clipLevel, true);

        // All samples should be in [-1.0, 1.0] after soft clipping
        for (double sample : samples) {
            assertThat(sample)
                    .isBetween(-clipLevel, clipLevel);
        }

        // First sample (0.5) should be mostly unchanged by soft clipping
        assertThat(samples[0])
                .isGreaterThan(0.4)
                .isLessThan(0.6);

        // High samples should be significantly compressed
        assertThat(samples[2])
                .isGreaterThan(0.9)
                .isLessThan(1.0);
    }

    @Test
    @DisplayName("Soft clipping should differ from hard clipping")
    void testSoftVsHardClipping() {
        double[] softSamples = {2.0};
        double[] hardSamples = {2.0};
        double clipLevel = 1.0;

        AudioProcessingUtils.clip(softSamples, clipLevel, true);
        AudioProcessingUtils.clip(hardSamples, clipLevel, false);

        // Soft clipping produces smooth saturation, hard produces 1.0
        assertThat(softSamples[0])
                .isGreaterThan(0.9)
                .isLessThan(hardSamples[0]); // Softer than hard clipping
    }

    @Test
    @DisplayName("Clipping should handle null input gracefully")
    void testClippingNullInput() {
        assertThatCode(() -> AudioProcessingUtils.clip(null, 1.0, false))
                .doesNotThrowAnyException();
    }

}

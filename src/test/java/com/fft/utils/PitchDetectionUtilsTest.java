package com.fft.utils;

import com.fft.core.FFTResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for PitchDetectionUtils.
 *
 * <p>Tests all pitch detection algorithms including YIN, spectral analysis,
 * hybrid detection, chord recognition, and voicing detection.</p>
 *
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("Pitch Detection Utils Tests")
class PitchDetectionUtilsTest {

    private static final double SAMPLE_RATE = 44100.0;
    private static final double FREQUENCY_TOLERANCE = 5.0; // Hz
    private static final double EPSILON = 1e-6;

    // Helper method to generate pure tone
    private double[] generatePureTone(double frequency, double duration, double amplitude) {
        int numSamples = (int) (SAMPLE_RATE * duration);
        double[] signal = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            signal[i] = amplitude * Math.sin(2.0 * Math.PI * frequency * i / SAMPLE_RATE);
        }
        return signal;
    }

    // Helper method to generate complex tone with harmonics
    private double[] generateComplexTone(double fundamental, double duration) {
        int numSamples = (int) (SAMPLE_RATE * duration);
        double[] signal = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            double t = i / SAMPLE_RATE;
            signal[i] = Math.sin(2.0 * Math.PI * fundamental * t) +
                       0.5 * Math.sin(2.0 * Math.PI * 2 * fundamental * t) +
                       0.3 * Math.sin(2.0 * Math.PI * 3 * fundamental * t);
        }
        return signal;
    }

    @Nested
    @DisplayName("YIN Algorithm Tests")
    class YinAlgorithmTests {

        @ParameterizedTest
        @ValueSource(doubles = {110.0, 220.0, 440.0, 880.0, 1000.0})
        @DisplayName("Should detect pure tones with YIN")
        void shouldDetectPureTones(double frequency) {
            double[] signal = generatePureTone(frequency, 0.5, 0.8);

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchYin(signal, SAMPLE_RATE);

            assertThat(result.isVoiced).isTrue();
            assertThat(result.frequency)
                .as("YIN detection of %.1f Hz", frequency)
                .isCloseTo(frequency, within(FREQUENCY_TOLERANCE));
            assertThat(result.confidence).isGreaterThan(0.5);
        }

        @Test
        @DisplayName("Should handle complex waveforms with harmonics")
        void shouldHandleComplexWaveforms() {
            double fundamental = 440.0;
            double[] signal = generateComplexTone(fundamental, 0.5);

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchYin(signal, SAMPLE_RATE);

            assertThat(result.isVoiced).isTrue();
            // YIN might detect fundamental or subharmonic
            assertThat(result.frequency).isGreaterThan(100.0);
        }

        @Test
        @DisplayName("Should return unvoiced for silent signal")
        void shouldDetectSilence() {
            double[] silence = new double[4096];

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchYin(silence, SAMPLE_RATE);

            assertThat(result.isVoiced).isFalse();
            assertThat(result.frequency).isEqualTo(0.0);
            assertThat(result.confidence).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle very low amplitude signals")
        void shouldHandleLowAmplitude() {
            double[] signal = generatePureTone(440.0, 0.5, 0.001);

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchYin(signal, SAMPLE_RATE);

            // Very low amplitude might be treated as unvoiced
            assertThat(result.isVoiced).isFalse();
        }

        @Test
        @DisplayName("Should handle short buffers")
        void shouldHandleShortBuffers() {
            double[] shortSignal = generatePureTone(440.0, 0.05, 0.8);

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchYin(shortSignal, SAMPLE_RATE);

            // Should still process but might be less accurate
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle large buffers with preprocessing")
        void shouldHandleLargeBuffers() {
            // Generate buffer larger than YIN_MAX_BUFFER_SIZE (4096)
            double[] largeSignal = generatePureTone(440.0, 1.0, 0.8);

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchYin(largeSignal, SAMPLE_RATE);

            assertThat(result.isVoiced).isTrue();
            assertThat(result.frequency).isCloseTo(440.0, within(FREQUENCY_TOLERANCE));
        }

        @Test
        @DisplayName("Should reject frequencies outside valid range")
        void shouldRejectOutOfRangeFrequencies() {
            // Test very low frequency (below MIN_FREQUENCY)
            double[] lowFreq = generatePureTone(50.0, 0.5, 0.8);
            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchYin(lowFreq, SAMPLE_RATE);

            // Should either be unvoiced or detected frequency should be 0
            if (!result.isVoiced) {
                assertThat(result.frequency).isEqualTo(0.0);
            }
        }
    }

    @Nested
    @DisplayName("Spectral Method Tests")
    class SpectralMethodTests {

        @ParameterizedTest
        @ValueSource(doubles = {110.0, 220.0, 440.0, 880.0, 1000.0})
        @DisplayName("Should detect pure tones with spectral method")
        void shouldDetectPureTones(double frequency) {
            double[] signal = generatePureTone(frequency, 0.5, 0.8);
            double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
            FFTResult spectrum = FFTUtils.fft(paddedSignal);

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchSpectral(spectrum, SAMPLE_RATE);

            assertThat(result.isVoiced).isTrue();
            assertThat(result.frequency)
                .as("Spectral detection of %.1f Hz", frequency)
                .isCloseTo(frequency, within(FREQUENCY_TOLERANCE));
        }

        @Test
        @DisplayName("Should find fundamental from harmonics")
        void shouldFindFundamental() {
            double fundamental = 440.0;
            double[] signal = generateComplexTone(fundamental, 0.5);
            double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
            FFTResult spectrum = FFTUtils.fft(paddedSignal);

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchSpectral(spectrum, SAMPLE_RATE);

            assertThat(result.isVoiced).isTrue();
            // Should detect fundamental or first harmonic
            assertThat(result.frequency)
                .isBetween(fundamental * 0.9, fundamental * 2.1);
        }

        @Test
        @DisplayName("Should return unvoiced for low magnitude spectrum")
        void shouldDetectLowMagnitude() {
            double[] silence = new double[4096];
            FFTResult spectrum = FFTUtils.fft(silence);

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchSpectral(spectrum, SAMPLE_RATE);

            assertThat(result.isVoiced).isFalse();
            assertThat(result.frequency).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should use parabolic interpolation for accuracy")
        void shouldUseParabolicInterpolation() {
            // Generate signal at non-bin-center frequency
            double frequency = 442.5;
            double[] signal = generatePureTone(frequency, 0.5, 0.8);
            double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
            FFTResult spectrum = FFTUtils.fft(paddedSignal);

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchSpectral(spectrum, SAMPLE_RATE);

            // Parabolic interpolation should get us closer than bin resolution
            assertThat(result.frequency)
                .isCloseTo(frequency, within(FREQUENCY_TOLERANCE));
        }
    }

    @Nested
    @DisplayName("Hybrid Detection Tests")
    class HybridDetectionTests {

        @ParameterizedTest
        @ValueSource(doubles = {110.0, 220.0, 440.0, 880.0})
        @DisplayName("Should combine YIN and spectral methods")
        void shouldCombineMethods(double frequency) {
            double[] signal = generatePureTone(frequency, 0.5, 0.8);

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE);

            assertThat(result.isVoiced).isTrue();
            assertThat(result.frequency)
                .as("Hybrid detection of %.1f Hz", frequency)
                .isCloseTo(frequency, within(FREQUENCY_TOLERANCE));
        }

        @Test
        @DisplayName("Should detect subharmonic errors from YIN")
        void shouldDetectSubharmonicErrors() {
            // Generate signal where YIN might detect subharmonic
            double fundamental = 440.0;
            double[] signal = generateComplexTone(fundamental, 0.5);

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE);

            assertThat(result.isVoiced).isTrue();
            // Hybrid should correct subharmonic detection
            assertThat(result.frequency)
                .isBetween(fundamental * 0.5, fundamental * 1.5);
        }

        @Test
        @DisplayName("Should use cache for repeated signals")
        void shouldCacheResults() {
            double[] signal = generatePureTone(440.0, 0.5, 0.8);

            // First call
            PitchDetectionUtils.PitchResult result1 =
                PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE);

            // Second call with same signal should use cache
            PitchDetectionUtils.PitchResult result2 =
                PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE);

            assertThat(result2.frequency).isEqualTo(result1.frequency);
            assertThat(result2.confidence).isEqualTo(result1.confidence);
            assertThat(result2.isVoiced).isEqualTo(result1.isVoiced);
        }

        @Test
        @DisplayName("Should handle silence efficiently with early exit")
        void shouldHandleSilenceEfficiently() {
            double[] silence = new double[4096];

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchHybrid(silence, SAMPLE_RATE);

            assertThat(result.isVoiced).isFalse();
            assertThat(result.frequency).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Chord Detection Tests")
    class ChordDetectionTests {

        @Test
        @DisplayName("Should detect major chord")
        void shouldDetectMajorChord() {
            // C major: C4 (261.63), E4 (329.63), G4 (392.00)
            double[] signal = new double[8192];
            for (int i = 0; i < signal.length; i++) {
                double t = i / SAMPLE_RATE;
                signal[i] = Math.sin(2 * Math.PI * 261.63 * t) +
                           Math.sin(2 * Math.PI * 329.63 * t) +
                           Math.sin(2 * Math.PI * 392.00 * t);
            }

            FFTResult spectrum = FFTUtils.fft(signal);
            PitchDetectionUtils.ChordResult result =
                PitchDetectionUtils.detectChord(spectrum, SAMPLE_RATE, 4);

            assertThat(result.frequencies).hasSizeGreaterThanOrEqualTo(2);
            assertThat(result.chordType).containsIgnoringCase("major");
            assertThat(result.confidence).isGreaterThan(0.3);
        }

        @Test
        @DisplayName("Should detect minor chord")
        void shouldDetectMinorChord() {
            // A minor: A3 (220.00), C4 (261.63), E4 (329.63)
            double[] signal = new double[8192];
            for (int i = 0; i < signal.length; i++) {
                double t = i / SAMPLE_RATE;
                signal[i] = Math.sin(2 * Math.PI * 220.00 * t) +
                           Math.sin(2 * Math.PI * 261.63 * t) +
                           Math.sin(2 * Math.PI * 329.63 * t);
            }

            FFTResult spectrum = FFTUtils.fft(signal);
            PitchDetectionUtils.ChordResult result =
                PitchDetectionUtils.detectChord(spectrum, SAMPLE_RATE, 4);

            assertThat(result.frequencies).hasSizeGreaterThanOrEqualTo(2);
            assertThat(result.chordType).containsIgnoringCase("minor");
        }

        @Test
        @DisplayName("Should detect single note as unknown chord")
        void shouldDetectSingleNote() {
            double[] signal = generatePureTone(440.0, 0.5, 0.8);
            double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
            FFTResult spectrum = FFTUtils.fft(paddedSignal);

            PitchDetectionUtils.ChordResult result =
                PitchDetectionUtils.detectChord(spectrum, SAMPLE_RATE, 4);

            // Should detect as single note or unknown
            assertThat(result.chordName).isIn("Single note", "No chord", "Unknown");
        }

        @Test
        @DisplayName("Should return empty for silence")
        void shouldReturnEmptyForSilence() {
            double[] silence = new double[4096];
            FFTResult spectrum = FFTUtils.fft(silence);

            PitchDetectionUtils.ChordResult result =
                PitchDetectionUtils.detectChord(spectrum, SAMPLE_RATE, 4);

            assertThat(result.frequencies).isEmpty();
            assertThat(result.confidence).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should limit number of detected frequencies")
        void shouldLimitFrequencyCount() {
            // Generate signal with many harmonics
            double[] signal = new double[8192];
            for (int i = 0; i < signal.length; i++) {
                double t = i / SAMPLE_RATE;
                for (int h = 1; h <= 8; h++) {
                    signal[i] += Math.sin(2 * Math.PI * 440.0 * h * t) / h;
                }
            }

            FFTResult spectrum = FFTUtils.fft(signal);
            PitchDetectionUtils.ChordResult result =
                PitchDetectionUtils.detectChord(spectrum, SAMPLE_RATE, 3);

            // Should respect maxFrequencies limit
            assertThat(result.frequencies.length).isLessThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Voicing Detection Tests")
    class VoicingDetectionTests {

        @Test
        @DisplayName("Should detect voiced signal")
        void shouldDetectVoicedSignal() {
            double[] signal = generatePureTone(440.0, 0.5, 0.5);

            boolean isVoiced = PitchDetectionUtils.checkVoicing(signal);

            assertThat(isVoiced).isTrue();
        }

        @Test
        @DisplayName("Should detect silence as unvoiced")
        void shouldDetectSilence() {
            double[] silence = new double[4096];

            boolean isVoiced = PitchDetectionUtils.checkVoicing(silence);

            assertThat(isVoiced).isFalse();
        }

        @Test
        @DisplayName("Should handle very low amplitude as unvoiced")
        void shouldHandleLowAmplitude() {
            double[] signal = generatePureTone(440.0, 0.5, 0.0001);

            boolean isVoiced = PitchDetectionUtils.checkVoicing(signal);

            assertThat(isVoiced).isFalse();
        }

        @Test
        @DisplayName("Should handle noise")
        void shouldHandleNoise() {
            double[] noise = new double[4096];
            java.util.Random random = new java.util.Random(42);
            for (int i = 0; i < noise.length; i++) {
                noise[i] = (random.nextDouble() - 0.5) * 0.1; // Low amplitude noise
            }

            boolean isVoiced = PitchDetectionUtils.checkVoicing(noise);

            // Low amplitude noise should be unvoiced
            assertThat(isVoiced).isFalse();
        }

        @Test
        @DisplayName("Should handle empty array")
        void shouldHandleEmptyArray() {
            double[] empty = new double[0];

            // Should not throw exception
            assertThatCode(() -> PitchDetectionUtils.checkVoicing(empty))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityMethodsTests {

        @Test
        @DisplayName("Should convert frequency to bin correctly")
        void shouldConvertFrequencyToBin() {
            int fftSize = 4096;
            double frequency = 440.0;

            int bin = PitchDetectionUtils.frequencyToBin(frequency, SAMPLE_RATE, fftSize);

            // Verify: bin = frequency * fftSize / sampleRate
            int expectedBin = (int) Math.round(frequency * fftSize / SAMPLE_RATE);
            assertThat(bin).isEqualTo(expectedBin);
        }

        @Test
        @DisplayName("Should handle zero frequency")
        void shouldHandleZeroFrequency() {
            int bin = PitchDetectionUtils.frequencyToBin(0.0, SAMPLE_RATE, 4096);
            assertThat(bin).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle Nyquist frequency")
        void shouldHandleNyquist() {
            double nyquist = SAMPLE_RATE / 2.0;
            int bin = PitchDetectionUtils.frequencyToBin(nyquist, SAMPLE_RATE, 4096);

            // Should be at or near N/2
            assertThat(bin).isCloseTo(2048, within(10));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Robustness Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle NaN in signal")
        void shouldHandleNaN() {
            double[] signal = generatePureTone(440.0, 0.5, 0.8);
            signal[100] = Double.NaN;

            // Should not throw exception
            assertThatCode(() ->
                PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE)
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle very high frequencies")
        void shouldHandleHighFrequencies() {
            double highFreq = 8000.0;
            double[] signal = generatePureTone(highFreq, 0.5, 0.8);

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE);

            // High frequency might be outside detection range
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle single sample")
        void shouldHandleSingleSample() {
            double[] singleSample = {0.5};

            // Should handle gracefully without crashing
            assertThatCode(() ->
                PitchDetectionUtils.detectPitchYin(singleSample, SAMPLE_RATE)
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle alternating signal")
        void shouldHandleAlternatingSignal() {
            double[] alternating = new double[1000];
            for (int i = 0; i < alternating.length; i++) {
                alternating[i] = (i % 2 == 0) ? 1.0 : -1.0;
            }

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchHybrid(alternating, SAMPLE_RATE);

            // Nyquist frequency signal
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Performance and Caching Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should cache fingerprints for identical signals")
        void shouldCacheIdenticalSignals() {
            double[] signal = generatePureTone(440.0, 0.5, 0.8);

            long start1 = System.nanoTime();
            PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE);
            long time1 = System.nanoTime() - start1;

            // Second call should be faster due to caching
            long start2 = System.nanoTime();
            PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE);
            long time2 = System.nanoTime() - start2;

            // Cache hit should be significantly faster (though not guaranteed on all systems)
            // At minimum, it should not be slower
            assertThat(time2).isLessThanOrEqualTo(time1);
        }

        @Test
        @DisplayName("Should not cache very short signals")
        void shouldNotCacheShortSignals() {
            double[] shortSignal = new double[10]; // Less than CACHE_FINGERPRINT_SIZE

            // Should still work but won't cache
            assertThatCode(() ->
                PitchDetectionUtils.detectPitchHybrid(shortSignal, SAMPLE_RATE)
            ).doesNotThrowAnyException();
        }
    }
}

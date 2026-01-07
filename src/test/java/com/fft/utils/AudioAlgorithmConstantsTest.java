package com.fft.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for AudioAlgorithmConstants.
 * Verifies all algorithm tuning parameters are correct, consistent, and within valid ranges.
 */
@DisplayName("AudioAlgorithmConstants Test Suite")
class AudioAlgorithmConstantsTest {

    // ========== Frequency Range Tests ==========

    @Test
    @DisplayName("MIN_FREQUENCY should be 75 Hz (below reliable detection threshold)")
    void testMinFrequency() {
        assertThat(AudioAlgorithmConstants.MIN_FREQUENCY)
                .isEqualTo(75.0);
    }

    @Test
    @DisplayName("MAX_FREQUENCY should be 2500 Hz (upper pitch detection limit)")
    void testMaxFrequency() {
        assertThat(AudioAlgorithmConstants.MAX_FREQUENCY)
                .isEqualTo(2500.0);
    }

    @Test
    @DisplayName("MAX_FREQUENCY should be greater than MIN_FREQUENCY")
    void testFrequencyRangeOrder() {
        assertThat(AudioAlgorithmConstants.MAX_FREQUENCY)
                .isGreaterThan(AudioAlgorithmConstants.MIN_FREQUENCY);
    }

    @Test
    @DisplayName("Frequency range should cover typical human voice (80-2000 Hz)")
    void testFrequencyRangeCoversHumanVoice() {
        assertThat(AudioAlgorithmConstants.MIN_FREQUENCY)
                .isLessThanOrEqualTo(80.0); // Typical male voice low
        assertThat(AudioAlgorithmConstants.MAX_FREQUENCY)
                .isGreaterThanOrEqualTo(2000.0); // Typical female voice high
    }

    @Test
    @DisplayName("MIN_FREQUENCY should be positive")
    void testMinFrequencyPositive() {
        assertThat(AudioAlgorithmConstants.MIN_FREQUENCY)
                .isPositive();
    }

    @Test
    @DisplayName("MAX_FREQUENCY should be less than Nyquist frequency (22.05 kHz)")
    void testMaxFrequencyBelowNyquist() {
        double nyquistFrequency = AudioConstants.SAMPLE_RATE / 2.0;
        assertThat(AudioAlgorithmConstants.MAX_FREQUENCY)
                .isLessThan(nyquistFrequency);
    }

    // ========== YIN Algorithm Parameters Tests ==========

    @Test
    @DisplayName("YIN_THRESHOLD should be 0.15 (standard balance)")
    void testYINThreshold() {
        assertThat(AudioAlgorithmConstants.YIN_THRESHOLD)
                .isEqualTo(0.15);
    }

    @Test
    @DisplayName("YIN_THRESHOLD should be between 0.0 and 1.0")
    void testYINThresholdRange() {
        assertThat(AudioAlgorithmConstants.YIN_THRESHOLD)
                .isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("YIN_PROBABILITY_THRESHOLD should reduce false positives (0.1)")
    void testYINProbabilityThreshold() {
        assertThat(AudioAlgorithmConstants.YIN_PROBABILITY_THRESHOLD)
                .isEqualTo(0.1);
    }

    @Test
    @DisplayName("YIN_PROBABILITY_THRESHOLD should be between 0.0 and 1.0")
    void testYINProbabilityThresholdRange() {
        assertThat(AudioAlgorithmConstants.YIN_PROBABILITY_THRESHOLD)
                .isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("YIN thresholds should be ordered: PROBABILITY < main THRESHOLD")
    void testYINThresholdsOrdered() {
        assertThat(AudioAlgorithmConstants.YIN_PROBABILITY_THRESHOLD)
                .isLessThan(AudioAlgorithmConstants.YIN_THRESHOLD);
    }

    @Test
    @DisplayName("YIN_MIN_PERIOD should be 100 samples")
    void testYINMinPeriod() {
        assertThat(AudioAlgorithmConstants.YIN_MIN_PERIOD)
                .isEqualTo(100);
    }

    @Test
    @DisplayName("YIN_MAX_PERIOD should be 2000 samples")
    void testYINMaxPeriod() {
        assertThat(AudioAlgorithmConstants.YIN_MAX_PERIOD)
                .isEqualTo(2000);
    }

    @Test
    @DisplayName("YIN_MAX_PERIOD should be greater than YIN_MIN_PERIOD")
    void testYINPeriodRange() {
        assertThat(AudioAlgorithmConstants.YIN_MAX_PERIOD)
                .isGreaterThan(AudioAlgorithmConstants.YIN_MIN_PERIOD);
    }

    @Test
    @DisplayName("YIN periods should be positive")
    void testYINPeriodsPositive() {
        assertThat(AudioAlgorithmConstants.YIN_MIN_PERIOD)
                .isPositive();
        assertThat(AudioAlgorithmConstants.YIN_MAX_PERIOD)
                .isPositive();
    }

    @Test
    @DisplayName("YIN_MIN_PERIOD should correspond to reasonable upper frequency")
    void testYINMinPeriodFrequency() {
        // Min period 100 at 44.1 kHz → max freq ~441 Hz
        double maxFreqFromPeriod = AudioConstants.SAMPLE_RATE / AudioAlgorithmConstants.YIN_MIN_PERIOD;
        assertThat(maxFreqFromPeriod)
                .isGreaterThan(400.0); // Reasonable upper limit
    }

    @Test
    @DisplayName("YIN_MAX_PERIOD should correspond to reasonable lower frequency")
    void testYINMaxPeriodFrequency() {
        // Max period 2000 at 44.1 kHz → min freq ~22 Hz
        double minFreqFromPeriod = AudioConstants.SAMPLE_RATE / AudioAlgorithmConstants.YIN_MAX_PERIOD;
        assertThat(minFreqFromPeriod)
                .isLessThan(30.0); // Reasonable lower limit
    }

    // ========== Voicing Detection Tests ==========

    @Test
    @DisplayName("VOICING_THRESHOLD should be 0.001 for standard balance")
    void testVoicingThreshold() {
        assertThat(AudioAlgorithmConstants.VOICING_THRESHOLD)
                .isEqualTo(0.001);
    }

    @Test
    @DisplayName("VOICING_THRESHOLD should be between 0.0 and 1.0 (normalized RMS)")
    void testVoicingThresholdRange() {
        assertThat(AudioAlgorithmConstants.VOICING_THRESHOLD)
                .isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("MIN_VOICING_CONFIDENCE should be 0.01 (higher confidence)")
    void testMinVoicingConfidence() {
        assertThat(AudioAlgorithmConstants.MIN_VOICING_CONFIDENCE)
                .isEqualTo(0.01);
    }

    @Test
    @DisplayName("MIN_VOICING_CONFIDENCE should be greater than VOICING_THRESHOLD")
    void testVoicingConfidenceOrdered() {
        assertThat(AudioAlgorithmConstants.MIN_VOICING_CONFIDENCE)
                .isGreaterThan(AudioAlgorithmConstants.VOICING_THRESHOLD);
    }

    @Test
    @DisplayName("Both voicing thresholds should be in normalized RMS range")
    void testVoicingThresholdsNormalized() {
        assertThat(AudioAlgorithmConstants.VOICING_THRESHOLD)
                .isBetween(0.0, 1.0);
        assertThat(AudioAlgorithmConstants.MIN_VOICING_CONFIDENCE)
                .isBetween(0.0, 1.0);
    }

    // ========== Spectral Analysis Parameters Tests ==========

    @Test
    @DisplayName("MIN_MAGNITUDE_THRESHOLD should be 0.05 (5% of max magnitude)")
    void testMinMagnitudeThreshold() {
        assertThat(AudioAlgorithmConstants.MIN_MAGNITUDE_THRESHOLD)
                .isEqualTo(0.05);
    }

    @Test
    @DisplayName("MIN_MAGNITUDE_THRESHOLD should be in valid range [0, 1]")
    void testMinMagnitudeThresholdRange() {
        assertThat(AudioAlgorithmConstants.MIN_MAGNITUDE_THRESHOLD)
                .isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("PARABOLIC_INTERPOLATION_FACTOR should be 1.0")
    void testParabolicInterpolationFactor() {
        assertThat(AudioAlgorithmConstants.PARABOLIC_INTERPOLATION_FACTOR)
                .isEqualTo(1.0);
    }

    @Test
    @DisplayName("HARMONIC_TOLERANCE_CENTS should be 50 cents (half semitone)")
    void testHarmonicToleranceCents() {
        assertThat(AudioAlgorithmConstants.HARMONIC_TOLERANCE_CENTS)
                .isEqualTo(50.0);
    }

    @Test
    @DisplayName("HARMONIC_TOLERANCE_CENTS should be positive")
    void testHarmonicToleranceCentsPositive() {
        assertThat(AudioAlgorithmConstants.HARMONIC_TOLERANCE_CENTS)
                .isPositive();
    }

    @Test
    @DisplayName("HARMONIC_TOLERANCE_CENTS should be less than 100 cents (one semitone)")
    void testHarmonicToleranceCentsReasonable() {
        assertThat(AudioAlgorithmConstants.HARMONIC_TOLERANCE_CENTS)
                .isLessThan(100.0); // Typically less than 1 semitone
    }

    // ========== Filtering and Smoothing Tests ==========

    @Test
    @DisplayName("PITCH_MEDIAN_FILTER_SIZE should be 5")
    void testPitchMedianFilterSize() {
        assertThat(AudioAlgorithmConstants.PITCH_MEDIAN_FILTER_SIZE)
                .isEqualTo(5);
    }

    @Test
    @DisplayName("PITCH_MEDIAN_FILTER_SIZE should be odd (for median calculation)")
    void testPitchMedianFilterSizeIsOdd() {
        assertThat(AudioAlgorithmConstants.PITCH_MEDIAN_FILTER_SIZE % 2)
                .isEqualTo(1);
    }

    @Test
    @DisplayName("PITCH_MEDIAN_FILTER_SIZE should be small (3-7)")
    void testPitchMedianFilterSizeReasonable() {
        assertThat(AudioAlgorithmConstants.PITCH_MEDIAN_FILTER_SIZE)
                .isBetween(3, 7);
    }

    @Test
    @DisplayName("MAGNITUDE_FILTER_SIZE should be 3")
    void testMagnitudeFilterSize() {
        assertThat(AudioAlgorithmConstants.MAGNITUDE_FILTER_SIZE)
                .isEqualTo(3);
    }

    @Test
    @DisplayName("MAGNITUDE_FILTER_SIZE should be odd")
    void testMagnitudeFilterSizeIsOdd() {
        assertThat(AudioAlgorithmConstants.MAGNITUDE_FILTER_SIZE % 2)
                .isEqualTo(1);
    }

    // ========== Time-Domain Analysis Tests ==========

    @Test
    @DisplayName("WINDOW_OVERLAP_FACTOR should be 0.5 (50% overlap)")
    void testWindowOverlapFactor() {
        assertThat(AudioAlgorithmConstants.WINDOW_OVERLAP_FACTOR)
                .isEqualTo(0.5);
    }

    @Test
    @DisplayName("WINDOW_OVERLAP_FACTOR should be between 0.0 and 1.0")
    void testWindowOverlapFactorRange() {
        assertThat(AudioAlgorithmConstants.WINDOW_OVERLAP_FACTOR)
                .isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("ONSET_LOOKAHEAD_FRAMES should be 5")
    void testOnsetLookaheadFrames() {
        assertThat(AudioAlgorithmConstants.ONSET_LOOKAHEAD_FRAMES)
                .isEqualTo(5);
    }

    @Test
    @DisplayName("ONSET_LOOKAHEAD_FRAMES should be positive")
    void testOnsetLookaheadFramesPositive() {
        assertThat(AudioAlgorithmConstants.ONSET_LOOKAHEAD_FRAMES)
                .isPositive();
    }

    // ========== Noise Robustness Tests ==========

    @Test
    @DisplayName("SNR_FLOOR_DB should be 5.0 dB")
    void testSNRFloor() {
        assertThat(AudioAlgorithmConstants.SNR_FLOOR_DB)
                .isEqualTo(5.0);
    }

    @Test
    @DisplayName("SNR_FLOOR_DB should be in reasonable range (5-15 dB)")
    void testSNRFloorReasonable() {
        assertThat(AudioAlgorithmConstants.SNR_FLOOR_DB)
                .isBetween(5.0, 15.0);
    }

    @Test
    @DisplayName("NOISE_FLOOR_MAGNITUDE should be 0.01")
    void testNoiseFlorMagnitude() {
        assertThat(AudioAlgorithmConstants.NOISE_FLOOR_MAGNITUDE)
                .isEqualTo(0.01);
    }

    @Test
    @DisplayName("NOISE_FLOOR_MAGNITUDE should be lower than MIN_MAGNITUDE_THRESHOLD")
    void testNoiseFloorMagnitudeRange() {
        // Noise floor (1%) should be lower than minimum detection threshold (5%)
        assertThat(AudioAlgorithmConstants.NOISE_FLOOR_MAGNITUDE)
                .isLessThan(AudioAlgorithmConstants.MIN_MAGNITUDE_THRESHOLD);
        assertThat(AudioAlgorithmConstants.NOISE_FLOOR_MAGNITUDE)
                .isGreaterThan(0.0);
    }

    // ========== Cache Size Tests ==========

    @Test
    @DisplayName("FFT_CACHE_SIZE should be 128")
    void testFFTCacheSize() {
        assertThat(AudioAlgorithmConstants.FFT_CACHE_SIZE)
                .isEqualTo(128);
    }

    @Test
    @DisplayName("PITCH_CACHE_SIZE should be 256")
    void testPitchCacheSize() {
        assertThat(AudioAlgorithmConstants.PITCH_CACHE_SIZE)
                .isEqualTo(256);
    }

    @Test
    @DisplayName("Cache sizes should be positive")
    void testCacheSizesPositive() {
        assertThat(AudioAlgorithmConstants.FFT_CACHE_SIZE)
                .isPositive();
        assertThat(AudioAlgorithmConstants.PITCH_CACHE_SIZE)
                .isPositive();
    }

    @Test
    @DisplayName("PITCH_CACHE_SIZE should be larger than FFT_CACHE_SIZE (more pitch results)")
    void testCacheSizesOrdered() {
        assertThat(AudioAlgorithmConstants.PITCH_CACHE_SIZE)
                .isGreaterThan(AudioAlgorithmConstants.FFT_CACHE_SIZE);
    }

    @Test
    @DisplayName("Cache sizes should be powers of 2 (efficient hashing)")
    void testCacheSizesPowerOfTwo() {
        int fftSize = AudioAlgorithmConstants.FFT_CACHE_SIZE;
        int pitchSize = AudioAlgorithmConstants.PITCH_CACHE_SIZE;

        assertThat(fftSize & (fftSize - 1)).isEqualTo(0);
        assertThat(pitchSize & (pitchSize - 1)).isEqualTo(0);
    }

    // ========== Cross-Parameter Consistency Tests ==========

    @Test
    @DisplayName("Algorithm parameters should be consistent with audio constants")
    void testConsistencyWithAudioConstants() {
        // Frequency range should be within Nyquist
        double nyquist = AudioConstants.SAMPLE_RATE / 2.0;
        assertThat(AudioAlgorithmConstants.MAX_FREQUENCY)
                .isLessThan(nyquist);

        // YIN periods should be reasonable for sample rate
        double minPeriodFreq = AudioConstants.SAMPLE_RATE / AudioAlgorithmConstants.YIN_MAX_PERIOD;
        double maxPeriodFreq = AudioConstants.SAMPLE_RATE / AudioAlgorithmConstants.YIN_MIN_PERIOD;

        assertThat(minPeriodFreq).isPositive();
        assertThat(maxPeriodFreq).isPositive();
    }

    @Test
    @DisplayName("Filter window overlap should allow continuous analysis")
    void testWindowOverlapAllowsContinuousAnalysis() {
        // With 50% overlap, adjacent frames will have some overlap
        assertThat(AudioAlgorithmConstants.WINDOW_OVERLAP_FACTOR)
                .isGreaterThan(0.0);
        assertThat(AudioAlgorithmConstants.WINDOW_OVERLAP_FACTOR)
                .isLessThan(1.0);
    }

    @Test
    @DisplayName("Voicing detection range should be reasonable")
    void testVoicingDetectionRange() {
        // Confidence threshold should be reasonable multiple of voicing threshold
        double ratio = AudioAlgorithmConstants.MIN_VOICING_CONFIDENCE / AudioAlgorithmConstants.VOICING_THRESHOLD;
        assertThat(ratio)
                .isBetween(5.0, 20.0); // Roughly 10x higher
    }

}

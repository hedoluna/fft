package com.fft.utils;

/**
 * Configuration parameters for audio processing algorithms.
 * Centralizes tuning parameters for pitch detection, voicing detection,
 * and frequency analysis to ensure consistency across all implementations.
 *
 * Single source of truth for:
 * - YIN autocorrelation algorithm thresholds
 * - Voicing detection (sound vs. silence discrimination)
 * - Frequency range constraints for pitch detection
 * - Magnitude and spectral analysis thresholds
 * - Median filtering window sizes
 */
public final class AudioAlgorithmConstants {

    // ========== Frequency Range Constraints ==========

    /**
     * Minimum frequency for valid pitch detection.
     * Below 75 Hz, detection becomes unreliable due to:
     * - FFT bin resolution (44100 / 4096 ≈ 10.77 Hz)
     * - Autocorrelation artifacts at low frequencies
     * - Standard human voice lower limit (~80 Hz)
     */
    public static final double MIN_FREQUENCY = 75.0;

    /**
     * Maximum frequency for valid pitch detection.
     * Above 2500 Hz, detection becomes unreliable due to:
     * - Harmonics become less distinct
     * - Energy distribution changes
     * - Typical range of sustained pitch detection
     *
     * Note: Some demos use 2000 Hz for stricter range.
     */
    public static final double MAX_FREQUENCY = 2500.0;

    // ========== YIN Algorithm Parameters ==========

    /**
     * YIN algorithm absolute threshold for autocorrelation function.
     * Range: [0.0, 1.0]
     * - Lower (0.05): More pitch candidates, more false positives
     * - 0.10: Conservative, fewer false positives
     * - 0.15: Standard balance (recommended)
     * - Higher (0.20): Very selective, may miss valid pitches
     *
     * The YIN algorithm computes autocorrelation difference function
     * and selects the minimum below this threshold.
     */
    public static final double YIN_THRESHOLD = 0.15;

    /**
     * YIN algorithm probability threshold.
     * Reduces false positives from subharmonic detection.
     * Thresholds pitch candidates that don't meet probability criteria.
     */
    public static final double YIN_PROBABILITY_THRESHOLD = 0.1;

    /**
     * Minimum period for YIN autocorrelation lag.
     * Period < this value are ignored (prevents too-high frequencies).
     * Used to set lower bound on detected pitch.
     */
    public static final int YIN_MIN_PERIOD = 100;

    /**
     * Maximum period for YIN autocorrelation lag.
     * Period > this value are ignored (prevents too-low frequencies).
     * Used to set upper bound on detected pitch.
     */
    public static final int YIN_MAX_PERIOD = 2000;

    // ========== Voicing Detection (Sound vs. Silence) ==========

    /**
     * RMS (Root Mean Square) energy threshold for voicing detection.
     * Audio below this threshold is considered silence/background noise.
     * Range: [0.0, 1.0] for normalized samples
     * - 0.0001: Very sensitive, picks up background noise
     * - 0.001: Standard balance (recommended) - typical threshold
     * - 0.01: Conservative, misses quiet speech/music
     *
     * Voicing detection prevents pitch detection in silence,
     * reducing false positives and computational waste.
     */
    public static final double VOICING_THRESHOLD = 0.001;

    /**
     * Minimum RMS for confident voice activity detection.
     * Used in hybrid algorithms to validate pitch detections.
     */
    public static final double MIN_VOICING_CONFIDENCE = 0.01;

    // ========== Spectral Analysis Parameters ==========

    /**
     * Minimum magnitude (normalized power spectrum) for peak detection.
     * Spectral peaks below this are considered noise/harmonics.
     * Range: [0.0, 1.0] for normalized spectrum
     * - 0.01: Very sensitive
     * - 0.05: Standard balance
     * - 0.1: Conservative
     */
    public static final double MIN_MAGNITUDE_THRESHOLD = 0.05;

    /**
     * Parabolic interpolation threshold for frequency refinement.
     * Amount of frequency shift to attempt via interpolation
     * when finding precise peak location between FFT bins.
     */
    public static final double PARABOLIC_INTERPOLATION_FACTOR = 1.0;

    /**
     * Harmonic series tolerance for fundamental frequency extraction.
     * When detecting multiple frequencies, this determines how tightly
     * harmonics must align to be considered part of the same timbre.
     */
    public static final double HARMONIC_TOLERANCE_CENTS = 50.0; // 50 cents

    // ========== Filtering and Smoothing ==========

    /**
     * Median filter window size for pitch stability.
     * Reduces jitter in detected pitch over time by smoothing
     * using median of surrounding pitch values.
     * Typical range: 3-7 frames
     * - 3: Minimal smoothing, quick response
     * - 5: Standard balance (recommended)
     * - 7: Heavy smoothing, lag in pitch changes
     */
    public static final int PITCH_MEDIAN_FILTER_SIZE = 5;

    /**
     * Moving average window size for magnitude smoothing.
     * Reduces noise in spectral magnitude measurements.
     */
    public static final int MAGNITUDE_FILTER_SIZE = 3;

    // ========== Time-Domain Analysis ==========

    /**
     * Window overlap factor for STFT (Short-Time Fourier Transform).
     * 0.5 = 50% overlap (standard choice)
     * 0.75 = 75% overlap (more frequent updates, higher computation)
     */
    public static final double WINDOW_OVERLAP_FACTOR = 0.5;

    /**
     * Number of frames to look-ahead for onset detection.
     * Helps identify speech/note onsets early.
     */
    public static final int ONSET_LOOKAHEAD_FRAMES = 5;

    // ========== Noise Robustness ==========

    /**
     * SNR (Signal-to-Noise Ratio) floor for reliable detection.
     * Below this SNR, pitch detection confidence drops significantly.
     * Typical range: 5-15 dB
     * - 5 dB: Very noisy, low confidence
     * - 10 dB: Moderate noise
     * - 15 dB: Clean audio
     */
    public static final double SNR_FLOOR_DB = 5.0;

    /**
     * Noise floor magnitude level.
     * Spectral components below this are considered background noise.
     */
    public static final double NOISE_FLOOR_MAGNITUDE = 0.01;

    // ========== Performance Tuning ==========

    /**
     * Cache size for FFT-based computations.
     * Caches FFT results to avoid recomputation on same input.
     */
    public static final int FFT_CACHE_SIZE = 128;

    /**
     * LRU cache size for pitch detection results.
     * Caches results for identical audio windows.
     */
    public static final int PITCH_CACHE_SIZE = 256;

    // ========== Private Constructor ==========

    /**
     * Private constructor to prevent instantiation.
     * This is a constants-only class.
     */
    private AudioAlgorithmConstants() {
        throw new AssertionError("Cannot instantiate AudioAlgorithmConstants utility class");
    }

}

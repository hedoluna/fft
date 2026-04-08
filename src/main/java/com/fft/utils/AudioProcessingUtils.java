package com.fft.utils;

/**
 * Audio signal processing utilities for window functions and basic audio operations.
 * Centralizes common audio processing tasks used across demos and analysis:
 * - Hamming window function for spectral analysis
 * - Hann window function (alternative)
 * - Voicing detection (sound vs. silence discrimination)
 * - Audio normalization
 * - RMS (Root Mean Square) energy calculation
 *
 * Eliminates duplicate window function implementations across demo files.
 */
public final class AudioProcessingUtils {

    /**
     * Apply Hamming window function to audio samples.
     * Window equation: w[n] = 0.54 - 0.46 * cos(2π * n / (N-1))
     *
     * The Hamming window reduces spectral leakage in FFT analysis by
     * tapering the signal smoothly to zero at the edges. This reduces
     * the appearance of false frequency components due to the rectangular
     * window implicit in fixed-length FFT.
     *
     * Trade-offs:
     * - Frequency resolution: Lower than rectangular window
     * - Spectral leakage: Much better than rectangular window
     * - Side-lobe level: -43 dB (good for most applications)
     * - Main lobe width: 8π/N (moderate width)
     *
     * @param samples Audio samples to window (modified in-place)
     */
    public static void applyHammingWindow(double[] samples) {
        if (samples == null || samples.length == 0) {
            return;
        }

        int n = samples.length;
        for (int i = 0; i < n; i++) {
            // Hamming window formula
            double window = 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / (n - 1));
            samples[i] *= window;
        }
    }

    /**
     * Apply Hann (Hanning) window function to audio samples.
     * Window equation: w[n] = 0.5 * (1 - cos(2π * n / (N-1)))
     *
     * Alternative to Hamming window with slightly different characteristics.
     * Hann window goes completely to zero at the edges (better for some applications).
     *
     * Trade-offs vs. Hamming:
     * - Side-lobe level: -32 dB (less aggressive than Hamming)
     * - Main lobe width: 8π/N (same as Hamming)
     * - Better for overlapping analysis (STFT)
     *
     * @param samples Audio samples to window (modified in-place)
     */
    public static void applyHannWindow(double[] samples) {
        if (samples == null || samples.length == 0) {
            return;
        }

        int n = samples.length;
        for (int i = 0; i < n; i++) {
            // Hann window formula
            double window = 0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / (n - 1)));
            samples[i] *= window;
        }
    }

    /**
     * Apply rectangular (boxcar) window to audio samples.
     * Mathematically just multiplies all samples by 1.0 (no-op).
     * Provided for completeness and clarity in code.
     *
     * @param samples Audio samples (unchanged)
     */
    public static void applyRectangularWindow(double[] samples) {
        // No-op: rectangular window has value 1.0 everywhere
        // But we keep the method for API consistency and code clarity
    }

    /**
     * Calculate RMS (Root Mean Square) energy of audio samples.
     * RMS is used for voicing detection and signal level measurement.
     *
     * Formula: RMS = sqrt(sum(sample^2) / N)
     *
     * @param samples Audio samples
     * @return RMS energy (0.0 for silence, higher for louder audio)
     */
    public static double calculateRMS(double[] samples) {
        if (samples == null || samples.length == 0) {
            return 0.0;
        }

        double sumSquares = 0.0;
        for (double sample : samples) {
            sumSquares += sample * sample;
        }

        return Math.sqrt(sumSquares / samples.length);
    }

    /**
     * Detect if audio contains voicing (sound vs. silence).
     * Uses RMS energy threshold to discriminate.
     *
     * @param samples Audio samples
     * @return true if RMS >= VOICING_THRESHOLD (audio contains voice/sound)
     */
    public static boolean isVoiced(double[] samples) {
        double rms = calculateRMS(samples);
        return rms >= AudioAlgorithmConstants.VOICING_THRESHOLD;
    }

    /**
     * Detect if audio contains confident voicing (loud sound).
     * Uses higher threshold than simple voicing detection.
     *
     * @param samples Audio samples
     * @return true if RMS >= MIN_VOICING_CONFIDENCE
     */
    public static boolean isConfidentlyVoiced(double[] samples) {
        double rms = calculateRMS(samples);
        return rms >= AudioAlgorithmConstants.MIN_VOICING_CONFIDENCE;
    }

    /**
     * Normalize audio samples to peak amplitude of 1.0.
     * Finds the maximum absolute value and divides all samples by it.
     * Prevents clipping and ensures consistent signal level.
     *
     * @param samples Audio samples to normalize (modified in-place)
     */
    public static void normalize(double[] samples) {
        if (samples == null || samples.length == 0) {
            return;
        }

        // Find peak amplitude
        double peak = 0.0;
        for (double sample : samples) {
            double abs = Math.abs(sample);
            if (abs > peak) {
                peak = abs;
            }
        }

        // Avoid division by zero
        if (peak == 0.0) {
            return;
        }

        // Normalize to peak of 1.0
        for (int i = 0; i < samples.length; i++) {
            samples[i] /= peak;
        }
    }

    /**
     * Scale audio samples by a constant factor.
     * Useful for amplitude adjustment and gain control.
     *
     * @param samples Audio samples to scale (modified in-place)
     * @param factor Scaling factor (1.0 = no change, 2.0 = double amplitude)
     */
    public static void scale(double[] samples, double factor) {
        if (samples == null || factor == 1.0) {
            return;
        }

        for (int i = 0; i < samples.length; i++) {
            samples[i] *= factor;
        }
    }

    /**
     * Add white Gaussian noise to audio samples.
     * Useful for testing noise robustness.
     *
     * @param samples Audio samples to corrupt (modified in-place)
     * @param noiseFraction Fraction of RMS energy to add as noise (0.0-1.0)
     */
    public static void addWhiteNoise(double[] samples, double noiseFraction) {
        if (samples == null || noiseFraction <= 0) {
            return;
        }

        double rms = calculateRMS(samples);
        double noiseAmplitude = rms * noiseFraction;

        java.util.Random random = new java.util.Random(System.nanoTime());
        for (int i = 0; i < samples.length; i++) {
            // Add Gaussian noise with specified amplitude
            samples[i] += noiseAmplitude * random.nextGaussian();
        }
    }

    /**
     * Convert normalized audio samples to byte array PCM format.
     * Converts double[] [-1.0, 1.0] to byte[] [-128, 127] (8-bit PCM).
     * Common format for WAV files and audio I/O.
     *
     * @param samples Normalized samples in range [-1.0, 1.0]
     * @return Byte array in range [-128, 127]
     */
    public static byte[] samplesToBytes(double[] samples) {
        if (samples == null) {
            return new byte[0];
        }

        byte[] bytes = new byte[samples.length];
        for (int i = 0; i < samples.length; i++) {
            // Clamp to [-1.0, 1.0] and convert to byte range
            double clamped = Math.max(-1.0, Math.min(1.0, samples[i]));
            bytes[i] = (byte) (clamped * 127);
        }

        return bytes;
    }

    /**
     * Convert byte array PCM format to normalized audio samples.
     * Converts byte[] [-128, 127] to double[] [-1.0, 1.0].
     *
     * @param bytes Byte array in range [-128, 127]
     * @return Normalized samples in range [-1.0, 1.0]
     */
    public static double[] bytesToSamples(byte[] bytes) {
        if (bytes == null) {
            return new double[0];
        }

        double[] samples = new double[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            samples[i] = bytes[i] / 128.0;
        }

        return samples;
    }

    /**
     * Convert byte array PCM to samples with destination array.
     * Avoids allocation when reusing existing array.
     *
     * @param bytes Source byte array
     * @param samples Destination sample array (must be same length as bytes)
     */
    public static void bytesToSamples(byte[] bytes, double[] samples) {
        if (bytes == null || samples == null || bytes.length != samples.length) {
            return;
        }

        for (int i = 0; i < bytes.length; i++) {
            samples[i] = bytes[i] / 128.0;
        }
    }

    /**
     * Clip audio samples to prevent distortion.
     * Soft clipping using tanh for smooth saturation.
     * Hard clipping (with clipping = true) uses hard limits.
     *
     * @param samples Audio samples to clip (modified in-place)
     * @param clipLevel Clipping threshold (typically 1.0)
     * @param softClip true for soft clipping (tanh), false for hard clipping
     */
    public static void clip(double[] samples, double clipLevel, boolean softClip) {
        if (samples == null || clipLevel <= 0) {
            return;
        }

        if (softClip) {
            // Soft clipping: smooth saturation using tanh
            for (int i = 0; i < samples.length; i++) {
                double normalized = samples[i] / clipLevel;
                samples[i] = clipLevel * Math.tanh(normalized);
            }
        } else {
            // Hard clipping: sharp cutoff
            for (int i = 0; i < samples.length; i++) {
                if (samples[i] > clipLevel) {
                    samples[i] = clipLevel;
                } else if (samples[i] < -clipLevel) {
                    samples[i] = -clipLevel;
                }
            }
        }
    }

    /**
     * Calculate the median of an array of values.
     *
     * @param values input values
     * @return median value, or 0.0 if empty
     */
    public static double calculateMedian(double[] values) {
        if (values == null || values.length == 0) return 0.0;
        double[] sorted = values.clone();
        java.util.Arrays.sort(sorted);
        if (sorted.length % 2 == 0) {
            return (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2.0;
        }
        return sorted[sorted.length / 2];
    }

    /**
     * Calculate the median of a list of values.
     *
     * @param values input values
     * @return median value, or 0.0 if empty
     */
    public static double calculateMedian(java.util.List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        double[] arr = values.stream().mapToDouble(Double::doubleValue).toArray();
        return calculateMedian(arr);
    }

    /**
     * Add white Gaussian noise with a fixed amplitude (not relative to RMS).
     *
     * @param samples        audio samples to corrupt (modified in-place)
     * @param noiseAmplitude absolute amplitude of the noise
     * @param random         random number generator to use
     */
    public static void addWhiteNoise(double[] samples, double noiseAmplitude, java.util.Random random) {
        if (samples == null || noiseAmplitude <= 0) {
            return;
        }
        for (int i = 0; i < samples.length; i++) {
            samples[i] += noiseAmplitude * random.nextGaussian();
        }
    }

    /**
     * Generate a pure sine tone at the given frequency.
     *
     * @param frequency frequency in Hz
     * @param duration  duration in seconds
     * @param amplitude peak amplitude (typically 1.0)
     * @return array of audio samples
     */
    public static double[] generateTone(double frequency, double duration, double amplitude) {
        int samples = (int) (AudioConstants.SAMPLE_RATE * duration);
        double[] signal = new double[samples];
        for (int i = 0; i < samples; i++) {
            double t = i / AudioConstants.SAMPLE_RATE;
            signal[i] = amplitude * Math.sin(2.0 * Math.PI * frequency * t);
        }
        return signal;
    }

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private AudioProcessingUtils() {
        throw new AssertionError("Cannot instantiate AudioProcessingUtils utility class");
    }

}

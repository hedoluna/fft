package com.fft.utils;

/**
 * Central configuration for audio processing parameters.
 * Eliminates scattered constant definitions across demos and tests.
 *
 * Single source of truth for:
 * - Sample rate configuration (44.1 kHz standard)
 * - FFT window size (4096 samples)
 * - Musical note reference (A4 = 440 Hz, MIDI note 69)
 * - Note name mappings for display and analysis
 */
public final class AudioConstants {

    // ========== Audio Configuration ==========

    /**
     * Standard CD-quality sample rate: 44.1 kHz.
     * Nyquist frequency: 22.05 kHz (covers human hearing 20 Hz - 20 kHz)
     */
    public static final double SAMPLE_RATE = 44100.0;

    /**
     * FFT window size for spectral analysis.
     * Provides good balance between frequency resolution and time resolution.
     * Frequency resolution: 44100 / 4096 ≈ 10.77 Hz per bin
     */
    public static final int FFT_SIZE = 4096;

    /**
     * Audio buffer size for real-time processing.
     * One FFT window of samples at SAMPLE_RATE.
     */
    public static final int BUFFER_SIZE = FFT_SIZE;

    // ========== Musical Reference ==========

    /**
     * Reference frequency for note A4.
     * Standard in modern equal temperament tuning.
     */
    public static final double A4_FREQUENCY = 440.0;

    /**
     * MIDI note number for A4 (used in frequency-to-note conversions).
     */
    public static final int A4_NOTE_NUMBER = 69;

    /**
     * Chromatic note names in order from C to B.
     * Index maps to semitone offset within octave:
     * - 0: C
     * - 1: C#/Db
     * - 2: D
     * - 3: D#/Eb
     * - 4: E
     * - 5: F
     * - 6: F#/Gb
     * - 7: G
     * - 8: G#/Ab
     * - 9: A
     * - 10: A#/Bb
     * - 11: B
     */
    public static final String[] NOTE_NAMES = {
        "C", "C#", "D", "D#", "E", "F",
        "F#", "G", "G#", "A", "A#", "B"
    };

    /**
     * Number of semitones per octave (constant for equal temperament).
     */
    public static final int SEMITONES_PER_OCTAVE = 12;

    /**
     * Lowest note in standard piano range: A0 (~27.5 Hz).
     */
    public static final int LOWEST_MIDI_NOTE = 21;

    /**
     * Highest note in standard piano range: C8 (~4186 Hz).
     */
    public static final int HIGHEST_MIDI_NOTE = 108;

    // ========== Private Constructor ==========

    /**
     * Private constructor to prevent instantiation.
     * This is a constants-only class.
     */
    private AudioConstants() {
        throw new AssertionError("Cannot instantiate AudioConstants utility class");
    }

}

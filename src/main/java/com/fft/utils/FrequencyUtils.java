package com.fft.utils;

/**
 * Frequency and pitch conversion utilities for audio processing.
 * Centralizes all frequency-related conversions:
 * - Frequency ↔ FFT bin index
 * - Frequency ↔ MIDI note number
 * - MIDI note ↔ Note name
 * - Frequency ↔ Note name (with octave)
 *
 * Eliminates scattered implementations across demo and test classes.
 * Uses equal temperament tuning with A4 = 440 Hz as reference.
 */
public final class FrequencyUtils {

    /**
     * Convert a frequency (Hz) to the nearest FFT bin index.
     * Each bin represents a frequency increment of:
     * sampleRate / fftSize (e.g., 44100 / 4096 ≈ 10.77 Hz per bin)
     *
     * @param frequency Frequency in Hz
     * @param fftSize FFT window size (typically 4096 for 44.1 kHz)
     * @return Bin index (0 to fftSize/2)
     */
    public static int frequencyToBin(double frequency, int fftSize) {
        double binFrequency = AudioConstants.SAMPLE_RATE / fftSize;
        return (int) Math.round(frequency / binFrequency);
    }

    /**
     * Convert a frequency (Hz) to FFT bin index using default FFT size.
     * Convenience method for standard 4096 FFT window.
     *
     * @param frequency Frequency in Hz
     * @return Bin index for AudioConstants.FFT_SIZE
     */
    public static int frequencyToBin(double frequency) {
        return frequencyToBin(frequency, AudioConstants.FFT_SIZE);
    }

    /**
     * Convert an FFT bin index to frequency (Hz).
     * Uses formula: frequency = bin * (sampleRate / fftSize)
     *
     * @param bin Bin index (0 to fftSize/2)
     * @param fftSize FFT window size (typically 4096)
     * @return Frequency in Hz
     */
    public static double binToFrequency(int bin, int fftSize) {
        return bin * AudioConstants.SAMPLE_RATE / fftSize;
    }

    /**
     * Convert an FFT bin index to frequency using default FFT size.
     * Convenience method for standard 4096 FFT window.
     *
     * @param bin Bin index
     * @return Frequency in Hz for AudioConstants.FFT_SIZE
     */
    public static double binToFrequency(int bin) {
        return binToFrequency(bin, AudioConstants.FFT_SIZE);
    }

    /**
     * Convert a frequency (Hz) to a MIDI note number.
     * Uses equal temperament with A4 (440 Hz) = MIDI note 69.
     *
     * Formula: MIDI_NOTE = A4_NOTE + 12 * log2(frequency / A4_FREQUENCY)
     *
     * @param frequency Frequency in Hz
     * @return MIDI note number (0-127, clamped to piano range 21-108)
     */
    public static int frequencyToMidiNote(double frequency) {
        if (frequency <= 0) {
            return -1; // Invalid frequency
        }

        double semitonesFromA4 = 12.0 * Math.log(frequency / AudioConstants.A4_FREQUENCY) / Math.log(2);
        int midiNote = (int) Math.round(AudioConstants.A4_NOTE_NUMBER + semitonesFromA4);

        // Clamp to valid MIDI note range
        return Math.max(0, Math.min(127, midiNote));
    }

    /**
     * Convert a MIDI note number to frequency (Hz).
     * Uses equal temperament with A4 (440 Hz) = MIDI note 69.
     *
     * Formula: frequency = A4_FREQUENCY * 2^((MIDI_NOTE - A4_NOTE) / 12)
     *
     * @param midiNote MIDI note number (0-127)
     * @return Frequency in Hz
     */
    public static double midiNoteToFrequency(int midiNote) {
        return AudioConstants.A4_FREQUENCY * Math.pow(2, (midiNote - AudioConstants.A4_NOTE_NUMBER) / 12.0);
    }

    /**
     * Convert a MIDI note number to a note name with octave.
     * Example: 69 → "A4", 60 → "C4", 72 → "C5"
     *
     * @param midiNote MIDI note number (0-127)
     * @return Note name like "C4", "A#4", "D5", or "UNKNOWN" for invalid notes
     */
    public static String midiNoteToNoteName(int midiNote) {
        if (midiNote < 0 || midiNote > 127) {
            return "UNKNOWN";
        }

        int semitonesInOctave = AudioConstants.SEMITONES_PER_OCTAVE;
        int octave = (midiNote / semitonesInOctave) - 1; // MIDI octaves start at -1
        int noteIndex = midiNote % semitonesInOctave;

        return AudioConstants.NOTE_NAMES[noteIndex] + octave;
    }

    /**
     * Convert a frequency (Hz) directly to a note name with octave.
     * Combines frequencyToMidiNote() and midiNoteToNoteName().
     * Example: 440.0 Hz → "A4", 261.6 Hz → "C4"
     *
     * @param frequency Frequency in Hz
     * @return Note name like "A4", "C#5", or "UNKNOWN" for invalid frequencies
     */
    public static String frequencyToNoteName(double frequency) {
        int midiNote = frequencyToMidiNote(frequency);
        return midiNoteToNoteName(midiNote);
    }

    /**
     * Convert a note name with octave to frequency (Hz).
     * Example: "A4" → 440.0, "C4" → 261.63, "D#5" → 311.13, "Db5" → 277.18
     * Supports both sharp (#) and flat (b) notation.
     *
     * @param noteName Note name like "A4", "C#5", "Db5", "E3"
     * @return Frequency in Hz, or -1 if note name is invalid
     */
    public static double noteNameToFrequency(String noteName) {
        if (noteName == null || noteName.isEmpty()) {
            return -1;
        }

        try {
            // Parse note name: extract note and octave
            // Format: "[Note][Accidental][Octave]" where Note is 1 char, Accidental is 0-1 chars
            noteName = noteName.toUpperCase();

            // Extract note character (first char)
            char noteChar = noteName.charAt(0);

            // Extract accidental (# or B for sharp/flat)
            char accidental = '\0';
            int octaveStartIndex = 1;

            if (noteName.length() > 1 && (noteName.charAt(1) == '#' || noteName.charAt(1) == 'B')) {
                accidental = noteName.charAt(1);
                octaveStartIndex = 2;
            }

            // Extract octave
            int octave = Integer.parseInt(noteName.substring(octaveStartIndex));

            // Find base note index
            String noteStr = String.valueOf(noteChar);
            int noteIndex = -1;
            for (int i = 0; i < AudioConstants.NOTE_NAMES.length; i++) {
                if (AudioConstants.NOTE_NAMES[i].startsWith(noteStr)) {
                    noteIndex = i;
                    break;
                }
            }

            if (noteIndex == -1) {
                return -1; // Invalid note
            }

            // Handle accidental
            if (accidental == '#') {
                // Sharp: move up one semitone
                noteIndex = (noteIndex + 1) % AudioConstants.SEMITONES_PER_OCTAVE;
            } else if (accidental == 'B') {
                // Flat: move down one semitone
                noteIndex = (noteIndex - 1 + AudioConstants.SEMITONES_PER_OCTAVE) % AudioConstants.SEMITONES_PER_OCTAVE;
            }

            // Convert to MIDI note number
            // MIDI octaves: Middle C (C4) = note 60, which is octave 4, semitone 0
            // Formula: MIDI = (octave + 1) * 12 + noteIndex
            int midiNote = (octave + 1) * AudioConstants.SEMITONES_PER_OCTAVE + noteIndex;

            return midiNoteToFrequency(midiNote);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return -1; // Invalid format
        }
    }

    /**
     * Calculate the cents difference between two frequencies.
     * 1 semitone = 100 cents
     * 1 octave = 1200 cents
     *
     * @param freq1 First frequency (Hz)
     * @param freq2 Second frequency (Hz)
     * @return Difference in cents (positive if freq2 > freq1)
     */
    public static double frequencyDifferenceInCents(double freq1, double freq2) {
        if (freq1 <= 0 || freq2 <= 0) {
            return 0;
        }
        return 1200.0 * Math.log(freq2 / freq1) / Math.log(2);
    }

    /**
     * Clamp a frequency to the valid detection range.
     * Uses AudioAlgorithmConstants.MIN_FREQUENCY and MAX_FREQUENCY.
     *
     * @param frequency Frequency in Hz
     * @return Frequency clamped to [MIN_FREQUENCY, MAX_FREQUENCY]
     */
    public static double clampFrequency(double frequency) {
        return Math.max(AudioAlgorithmConstants.MIN_FREQUENCY,
                Math.min(frequency, AudioAlgorithmConstants.MAX_FREQUENCY));
    }

    /**
     * Check if a frequency is within the valid detection range.
     *
     * @param frequency Frequency in Hz
     * @return true if frequency is within [MIN_FREQUENCY, MAX_FREQUENCY]
     */
    public static boolean isValidFrequency(double frequency) {
        return frequency >= AudioAlgorithmConstants.MIN_FREQUENCY &&
               frequency <= AudioAlgorithmConstants.MAX_FREQUENCY;
    }

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private FrequencyUtils() {
        throw new AssertionError("Cannot instantiate FrequencyUtils utility class");
    }

}

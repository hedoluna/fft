package com.fft.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for FrequencyUtils.
 * Tests frequency conversions (Hz ↔ FFT bins ↔ MIDI ↔ note names).
 */
@DisplayName("FrequencyUtils Test Suite")
class FrequencyUtilsTest {

    private static final double EPSILON = 0.01; // 0.01 Hz tolerance

    // ========== Frequency to FFT Bin Tests ==========

    @Test
    @DisplayName("A4 (440 Hz) should map to specific bin")
    void testA4FrequencyToBin() {
        int expectedBin = (int) Math.round(440.0 / (AudioConstants.SAMPLE_RATE / AudioConstants.FFT_SIZE));
        int actualBin = FrequencyUtils.frequencyToBin(440.0);

        assertThat(actualBin)
                .isEqualTo(expectedBin);
    }

    @Test
    @DisplayName("frequencyToBin should use default FFT size when not specified")
    void testFrequencyToBinDefaultSize() {
        int binWithDefault = FrequencyUtils.frequencyToBin(440.0);
        int binWithExplicitSize = FrequencyUtils.frequencyToBin(440.0, AudioConstants.FFT_SIZE);

        assertThat(binWithDefault)
                .isEqualTo(binWithExplicitSize);
    }

    @Test
    @DisplayName("0 Hz should map to bin 0")
    void testZeroFrequencyToBin() {
        assertThat(FrequencyUtils.frequencyToBin(0.0))
                .isEqualTo(0);
    }

    @Test
    @DisplayName("Nyquist frequency should map to FFT_SIZE/2 bin")
    void testNyquistFrequencyToBin() {
        double nyquistFreq = AudioConstants.SAMPLE_RATE / 2.0;
        int expectedBin = AudioConstants.FFT_SIZE / 2;
        int actualBin = FrequencyUtils.frequencyToBin(nyquistFreq);

        assertThat(actualBin)
                .isCloseTo(expectedBin, within(1)); // Allow 1 bin rounding
    }

    // ========== FFT Bin to Frequency Tests ==========

    @Test
    @DisplayName("Bin 0 should map to 0 Hz")
    void testBin0ToFrequency() {
        assertThat(FrequencyUtils.binToFrequency(0))
                .isEqualTo(0.0);
    }

    @Test
    @DisplayName("Bin FFT_SIZE/2 should map to Nyquist frequency")
    void testBinToNyquistFrequency() {
        int nyquistBin = AudioConstants.FFT_SIZE / 2;
        double expectedFrequency = AudioConstants.SAMPLE_RATE / 2.0;
        double actualFrequency = FrequencyUtils.binToFrequency(nyquistBin);

        assertThat(actualFrequency)
                .isCloseTo(expectedFrequency, within(1.0)); // Allow ~1 Hz tolerance
    }

    @Test
    @DisplayName("binToFrequency should use default FFT size when not specified")
    void testBinToFrequencyDefaultSize() {
        double freqWithDefault = FrequencyUtils.binToFrequency(100);
        double freqWithExplicitSize = FrequencyUtils.binToFrequency(100, AudioConstants.FFT_SIZE);

        assertThat(freqWithDefault)
                .isEqualTo(freqWithExplicitSize);
    }

    @Test
    @DisplayName("Frequency bin conversion should be invertible")
    void testFrequencyBinRoundTrip() {
        double originalFreq = 440.0;
        int bin = FrequencyUtils.frequencyToBin(originalFreq);
        double recoveredFreq = FrequencyUtils.binToFrequency(bin);

        // Allow small rounding error from quantization to bins
        assertThat(recoveredFreq)
                .isCloseTo(originalFreq, within(11.0)); // Max ~10.77 Hz per bin
    }

    // ========== Frequency to MIDI Note Tests ==========

    @Test
    @DisplayName("A4 (440 Hz) should map to MIDI note 69")
    void testA4ToMIDINote() {
        int midiNote = FrequencyUtils.frequencyToMidiNote(440.0);
        assertThat(midiNote)
                .isEqualTo(69);
    }

    @Test
    @DisplayName("C4 (261.63 Hz) should map to MIDI note 60")
    void testC4ToMIDINote() {
        int midiNote = FrequencyUtils.frequencyToMidiNote(261.63);
        assertThat(midiNote)
                .isEqualTo(60);
    }

    @Test
    @DisplayName("A3 (220 Hz) should map to MIDI note 57")
    void testA3ToMIDINote() {
        int midiNote = FrequencyUtils.frequencyToMidiNote(220.0);
        assertThat(midiNote)
                .isEqualTo(57);
    }

    @Test
    @DisplayName("Negative frequency should return -1 (invalid)")
    void testNegativeFrequencyToMIDI() {
        assertThat(FrequencyUtils.frequencyToMidiNote(-440.0))
                .isEqualTo(-1);
    }

    @Test
    @DisplayName("Zero frequency should return -1 (invalid)")
    void testZeroFrequencyToMIDI() {
        assertThat(FrequencyUtils.frequencyToMidiNote(0.0))
                .isEqualTo(-1);
    }

    @Test
    @DisplayName("Very low frequency should be clamped to valid MIDI range")
    void testVeryLowFrequencyToMIDI() {
        int midiNote = FrequencyUtils.frequencyToMidiNote(1.0);
        assertThat(midiNote)
                .isBetween(0, 127);
    }

    @Test
    @DisplayName("Very high frequency should be clamped to valid MIDI range")
    void testVeryHighFrequencyToMIDI() {
        int midiNote = FrequencyUtils.frequencyToMidiNote(20000.0);
        assertThat(midiNote)
                .isBetween(0, 127);
    }

    // ========== MIDI Note to Frequency Tests ==========

    @Test
    @DisplayName("MIDI note 69 (A4) should map to 440 Hz")
    void testMIDI69ToFrequency() {
        double frequency = FrequencyUtils.midiNoteToFrequency(69);
        assertThat(frequency)
                .isCloseTo(440.0, within(EPSILON));
    }

    @Test
    @DisplayName("MIDI note 60 (C4) should map to approximately 261.63 Hz")
    void testMIDI60ToFrequency() {
        double frequency = FrequencyUtils.midiNoteToFrequency(60);
        assertThat(frequency)
                .isCloseTo(261.63, within(0.1));
    }

    @Test
    @DisplayName("MIDI note 57 (A3) should map to 220 Hz")
    void testMIDI57ToFrequency() {
        double frequency = FrequencyUtils.midiNoteToFrequency(57);
        assertThat(frequency)
                .isCloseTo(220.0, within(EPSILON));
    }

    @Test
    @DisplayName("MIDI note 0 should produce a valid low frequency")
    void testMIDI0ToFrequency() {
        double frequency = FrequencyUtils.midiNoteToFrequency(0);
        assertThat(frequency)
                .isPositive();
        assertThat(frequency)
                .isLessThan(100.0); // Very low frequency
    }

    @Test
    @DisplayName("MIDI note 127 should produce a valid high frequency")
    void testMIDI127ToFrequency() {
        double frequency = FrequencyUtils.midiNoteToFrequency(127);
        assertThat(frequency)
                .isGreaterThan(1000.0); // Very high frequency
    }

    @Test
    @DisplayName("Frequency-MIDI round trip should be accurate")
    void testFrequencyMIDIRoundTrip() {
        double originalFreq = 440.0;
        int midiNote = FrequencyUtils.frequencyToMidiNote(originalFreq);
        double recoveredFreq = FrequencyUtils.midiNoteToFrequency(midiNote);

        assertThat(recoveredFreq)
                .isCloseTo(originalFreq, within(EPSILON));
    }

    // ========== MIDI Note to Note Name Tests ==========

    @Test
    @DisplayName("MIDI note 69 (A4) should map to 'A4'")
    void testMIDI69ToNoteName() {
        String noteName = FrequencyUtils.midiNoteToNoteName(69);
        assertThat(noteName)
                .isEqualTo("A4");
    }

    @Test
    @DisplayName("MIDI note 60 (C4) should map to 'C4'")
    void testMIDI60ToNoteName() {
        String noteName = FrequencyUtils.midiNoteToNoteName(60);
        assertThat(noteName)
                .isEqualTo("C4");
    }

    @Test
    @DisplayName("MIDI note 61 (C#4) should map to 'C#4'")
    void testMIDI61ToNoteName() {
        String noteName = FrequencyUtils.midiNoteToNoteName(61);
        assertThat(noteName)
                .isEqualTo("C#4");
    }

    @Test
    @DisplayName("MIDI note 72 (C5) should map to 'C5'")
    void testMIDI72ToNoteName() {
        String noteName = FrequencyUtils.midiNoteToNoteName(72);
        assertThat(noteName)
                .isEqualTo("C5");
    }

    @Test
    @DisplayName("Invalid MIDI note (-1) should return 'UNKNOWN'")
    void testInvalidMIDIToNoteName() {
        assertThat(FrequencyUtils.midiNoteToNoteName(-1))
                .isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("MIDI note 128 (out of range) should return 'UNKNOWN'")
    void testOutOfRangeMIDIToNoteName() {
        assertThat(FrequencyUtils.midiNoteToNoteName(128))
                .isEqualTo("UNKNOWN");
    }

    // ========== Frequency to Note Name Tests ==========

    @Test
    @DisplayName("A4 (440 Hz) should map to 'A4'")
    void testA4FrequencyToNoteName() {
        String noteName = FrequencyUtils.frequencyToNoteName(440.0);
        assertThat(noteName)
                .isEqualTo("A4");
    }

    @Test
    @DisplayName("C4 (261.63 Hz) should map to 'C4'")
    void testC4FrequencyToNoteName() {
        String noteName = FrequencyUtils.frequencyToNoteName(261.63);
        assertThat(noteName)
                .isEqualTo("C4");
    }

    @Test
    @DisplayName("Invalid frequency should return 'UNKNOWN'")
    void testInvalidFrequencyToNoteName() {
        assertThat(FrequencyUtils.frequencyToNoteName(-1.0))
                .isEqualTo("UNKNOWN");
    }

    // ========== Note Name to Frequency Tests ==========

    @Test
    @DisplayName("'A4' should map to 440 Hz")
    void testA4NoteNameToFrequency() {
        double frequency = FrequencyUtils.noteNameToFrequency("A4");
        assertThat(frequency)
                .isCloseTo(440.0, within(EPSILON));
    }

    @Test
    @DisplayName("'C4' should map to approximately 261.63 Hz")
    void testC4NoteNameToFrequency() {
        double frequency = FrequencyUtils.noteNameToFrequency("C4");
        assertThat(frequency)
                .isCloseTo(261.63, within(0.1));
    }

    @Test
    @DisplayName("'C#4' (sharp) should map to correct frequency")
    void testC4SharpNoteNameToFrequency() {
        double c4Freq = FrequencyUtils.noteNameToFrequency("C4");
        double c4SharpFreq = FrequencyUtils.noteNameToFrequency("C#4");

        // C# is one semitone (100 cents) higher than C
        assertThat(c4SharpFreq)
                .isGreaterThan(c4Freq);

        // 12 semitones = 2x frequency ratio
        double expectedRatio = Math.pow(2, 1.0 / 12.0);
        assertThat(c4SharpFreq / c4Freq)
                .isCloseTo(expectedRatio, within(0.001));
    }

    @Test
    @DisplayName("'Db4' (flat notation) should map to same frequency as 'C#4'")
    void testDb4FlatNoteNameToFrequency() {
        double c4SharpFreq = FrequencyUtils.noteNameToFrequency("C#4");
        double db4Freq = FrequencyUtils.noteNameToFrequency("Db4");

        assertThat(db4Freq)
                .isCloseTo(c4SharpFreq, within(EPSILON));
    }

    @Test
    @DisplayName("'D#5' (sharp) should parse correctly")
    void testD5SharpNoteNameToFrequency() {
        double d5SharpFreq = FrequencyUtils.noteNameToFrequency("D#5");
        assertThat(d5SharpFreq)
                .isPositive()
                .isGreaterThan(100.0);
    }

    @Test
    @DisplayName("'Eb5' (flat) should parse correctly")
    void testEb5FlatNoteNameToFrequency() {
        double eb5Freq = FrequencyUtils.noteNameToFrequency("Eb5");
        assertThat(eb5Freq)
                .isPositive();
    }

    @Test
    @DisplayName("'Bb3' (flat) should parse correctly")
    void testBb3FlatNoteNameToFrequency() {
        double bb3Freq = FrequencyUtils.noteNameToFrequency("Bb3");
        assertThat(bb3Freq)
                .isPositive();
    }

    @Test
    @DisplayName("Case insensitivity: 'a4' should equal 'A4'")
    void testNoteNameCaseInsensitive() {
        double lowerCaseFreq = FrequencyUtils.noteNameToFrequency("a4");
        double upperCaseFreq = FrequencyUtils.noteNameToFrequency("A4");

        assertThat(lowerCaseFreq)
                .isCloseTo(upperCaseFreq, within(EPSILON));
    }

    @Test
    @DisplayName("'c#4' (lowercase sharp) should equal 'C#4'")
    void testSharpNoteNameCaseInsensitive() {
        double lowerCaseFreq = FrequencyUtils.noteNameToFrequency("c#4");
        double upperCaseFreq = FrequencyUtils.noteNameToFrequency("C#4");

        assertThat(lowerCaseFreq)
                .isCloseTo(upperCaseFreq, within(EPSILON));
    }

    @Test
    @DisplayName("Invalid note name should return -1")
    void testInvalidNoteNameToFrequency() {
        assertThat(FrequencyUtils.noteNameToFrequency("Z9"))
                .isEqualTo(-1.0);
    }

    @Test
    @DisplayName("Null note name should return -1")
    void testNullNoteNameToFrequency() {
        assertThat(FrequencyUtils.noteNameToFrequency(null))
                .isEqualTo(-1.0);
    }

    @Test
    @DisplayName("Empty note name should return -1")
    void testEmptyNoteNameToFrequency() {
        assertThat(FrequencyUtils.noteNameToFrequency(""))
                .isEqualTo(-1.0);
    }

    @Test
    @DisplayName("Malformed note name (invalid octave) should return -1")
    void testMalformedNoteNameToFrequency() {
        assertThat(FrequencyUtils.noteNameToFrequency("C4X"))
                .isEqualTo(-1.0);
    }

    @Test
    @DisplayName("Note name round trip should be accurate")
    void testNoteNameRoundTrip() {
        String originalNote = "A4";
        double frequency = FrequencyUtils.noteNameToFrequency(originalNote);
        String recoveredNote = FrequencyUtils.frequencyToNoteName(frequency);

        assertThat(recoveredNote)
                .isEqualTo(originalNote);
    }

    // ========== Frequency Difference in Cents Tests ==========

    @Test
    @DisplayName("Identical frequencies should have 0 cent difference")
    void testIdenticalFrequenciesCentDifference() {
        double cents = FrequencyUtils.frequencyDifferenceInCents(440.0, 440.0);
        assertThat(cents)
                .isCloseTo(0.0, within(EPSILON));
    }

    @Test
    @DisplayName("Octave difference (2x frequency) should be 1200 cents")
    void testOctaveCentDifference() {
        double cents = FrequencyUtils.frequencyDifferenceInCents(440.0, 880.0);
        assertThat(cents)
                .isCloseTo(1200.0, within(1.0)); // 1 octave = 1200 cents
    }

    @Test
    @DisplayName("Semitone difference (12th root of 2) should be ~100 cents")
    void testSemitoneCentDifference() {
        double freq1 = 440.0;
        double freq2 = freq1 * Math.pow(2, 1.0 / 12.0); // One semitone up

        double cents = FrequencyUtils.frequencyDifferenceInCents(freq1, freq2);
        assertThat(cents)
                .isCloseTo(100.0, within(1.0)); // 1 semitone = 100 cents
    }

    @Test
    @DisplayName("Lower frequency should have negative cent difference")
    void testLowerFrequencyNegativeCents() {
        double cents = FrequencyUtils.frequencyDifferenceInCents(880.0, 440.0);
        assertThat(cents)
                .isNegative();
    }

    @Test
    @DisplayName("Negative or zero frequency should return 0")
    void testInvalidFrequencyCentDifference() {
        assertThat(FrequencyUtils.frequencyDifferenceInCents(-1.0, 440.0))
                .isEqualTo(0.0);
        assertThat(FrequencyUtils.frequencyDifferenceInCents(440.0, -1.0))
                .isEqualTo(0.0);
        assertThat(FrequencyUtils.frequencyDifferenceInCents(0.0, 440.0))
                .isEqualTo(0.0);
    }

    // ========== Clamp Frequency Tests ==========

    @Test
    @DisplayName("Frequency within valid range should not be clamped")
    void testClampFrequencyWithinRange() {
        double frequency = 440.0; // Within typical range
        double clamped = FrequencyUtils.clampFrequency(frequency);

        assertThat(clamped)
                .isEqualTo(frequency);
    }

    @Test
    @DisplayName("Frequency below MIN_FREQUENCY should be clamped to minimum")
    void testClampFrequencyBelowMinimum() {
        double belowMin = AudioAlgorithmConstants.MIN_FREQUENCY - 10.0;
        double clamped = FrequencyUtils.clampFrequency(belowMin);

        assertThat(clamped)
                .isEqualTo(AudioAlgorithmConstants.MIN_FREQUENCY);
    }

    @Test
    @DisplayName("Frequency above MAX_FREQUENCY should be clamped to maximum")
    void testClampFrequencyAboveMaximum() {
        double aboveMax = AudioAlgorithmConstants.MAX_FREQUENCY + 100.0;
        double clamped = FrequencyUtils.clampFrequency(aboveMax);

        assertThat(clamped)
                .isEqualTo(AudioAlgorithmConstants.MAX_FREQUENCY);
    }

    // ========== Validate Frequency Tests ==========

    @Test
    @DisplayName("Frequency within valid range should be valid")
    void testValidFrequencyWithinRange() {
        assertThat(FrequencyUtils.isValidFrequency(440.0))
                .isTrue();
    }

    @Test
    @DisplayName("Frequency at MIN_FREQUENCY boundary should be valid")
    void testValidFrequencyAtMinBoundary() {
        assertThat(FrequencyUtils.isValidFrequency(AudioAlgorithmConstants.MIN_FREQUENCY))
                .isTrue();
    }

    @Test
    @DisplayName("Frequency at MAX_FREQUENCY boundary should be valid")
    void testValidFrequencyAtMaxBoundary() {
        assertThat(FrequencyUtils.isValidFrequency(AudioAlgorithmConstants.MAX_FREQUENCY))
                .isTrue();
    }

    @Test
    @DisplayName("Frequency below MIN_FREQUENCY should be invalid")
    void testInvalidFrequencyBelowMinimum() {
        double belowMin = AudioAlgorithmConstants.MIN_FREQUENCY - 1.0;
        assertThat(FrequencyUtils.isValidFrequency(belowMin))
                .isFalse();
    }

    @Test
    @DisplayName("Frequency above MAX_FREQUENCY should be invalid")
    void testInvalidFrequencyAboveMaximum() {
        double aboveMax = AudioAlgorithmConstants.MAX_FREQUENCY + 1.0;
        assertThat(FrequencyUtils.isValidFrequency(aboveMax))
                .isFalse();
    }

    @Test
    @DisplayName("Negative frequency should be invalid")
    void testNegativeFrequencyInvalid() {
        assertThat(FrequencyUtils.isValidFrequency(-1.0))
                .isFalse();
    }

    @Test
    @DisplayName("Zero frequency should be invalid")
    void testZeroFrequencyInvalid() {
        assertThat(FrequencyUtils.isValidFrequency(0.0))
                .isFalse();
    }

}

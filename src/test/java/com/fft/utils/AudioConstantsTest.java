package com.fft.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for AudioConstants.
 * Verifies all audio configuration parameters are correct and consistent.
 */
@DisplayName("AudioConstants Test Suite")
class AudioConstantsTest {

    // ========== Sample Rate and FFT Configuration Tests ==========

    @Test
    @DisplayName("SAMPLE_RATE should be standard CD-quality 44.1 kHz")
    void testSampleRateIsCDQuality() {
        assertThat(AudioConstants.SAMPLE_RATE)
                .isEqualTo(44100.0);
    }

    @Test
    @DisplayName("FFT_SIZE should be 4096 for good frequency resolution")
    void testFFTSizeIsStandard() {
        assertThat(AudioConstants.FFT_SIZE)
                .isEqualTo(4096);
    }

    @Test
    @DisplayName("BUFFER_SIZE should equal FFT_SIZE")
    void testBufferSizeEqualsFFTSize() {
        assertThat(AudioConstants.BUFFER_SIZE)
                .isEqualTo(AudioConstants.FFT_SIZE);
    }

    @Test
    @DisplayName("Frequency resolution should be correct: sampleRate / fftSize")
    void testFrequencyResolution() {
        double expectedResolution = AudioConstants.SAMPLE_RATE / AudioConstants.FFT_SIZE;
        double actualResolution = 44100.0 / 4096.0; // ~10.77 Hz per bin

        assertThat(expectedResolution)
                .isCloseTo(actualResolution, within(0.01));
    }

    @Test
    @DisplayName("Nyquist frequency should be half of sample rate")
    void testNyquistFrequency() {
        double nyquistFrequency = AudioConstants.SAMPLE_RATE / 2.0;
        assertThat(nyquistFrequency)
                .isEqualTo(22050.0);
    }

    // ========== Musical Reference Tests ==========

    @Test
    @DisplayName("A4 reference frequency should be 440 Hz (concert pitch)")
    void testA4ReferenceFrequency() {
        assertThat(AudioConstants.A4_FREQUENCY)
                .isEqualTo(440.0);
    }

    @Test
    @DisplayName("A4 MIDI note number should be 69")
    void testA4MIDINoteNumber() {
        assertThat(AudioConstants.A4_NOTE_NUMBER)
                .isEqualTo(69);
    }

    @Test
    @DisplayName("SEMITONES_PER_OCTAVE should be 12 for equal temperament")
    void testSemitonesPerOctave() {
        assertThat(AudioConstants.SEMITONES_PER_OCTAVE)
                .isEqualTo(12);
    }

    // ========== Note Names Array Tests ==========

    @Test
    @DisplayName("NOTE_NAMES array should have exactly 12 entries")
    void testNoteNamesLength() {
        assertThat(AudioConstants.NOTE_NAMES)
                .hasSize(12);
    }

    @Test
    @DisplayName("NOTE_NAMES array should contain all chromatic notes in order")
    void testNoteNamesContent() {
        String[] expectedNotes = {
                "C", "C#", "D", "D#", "E", "F",
                "F#", "G", "G#", "A", "A#", "B"
        };

        assertThat(AudioConstants.NOTE_NAMES)
                .isEqualTo(expectedNotes);
    }

    @Test
    @DisplayName("NOTE_NAMES should be in ascending order by frequency")
    void testNoteNamesOrderByFrequency() {
        // C is at index 0 (lowest), B is at index 11 (highest in octave)
        assertThat(AudioConstants.NOTE_NAMES[0])
                .isEqualTo("C");
        assertThat(AudioConstants.NOTE_NAMES[11])
                .isEqualTo("B");
    }

    @Test
    @DisplayName("A note should be at index 9 (standard MIDI position)")
    void testANotePosition() {
        assertThat(AudioConstants.NOTE_NAMES[9])
                .isEqualTo("A");
    }

    @Test
    @DisplayName("Sharp notes should follow natural notes")
    void testSharpNotePositions() {
        assertThat(AudioConstants.NOTE_NAMES[1]).isEqualTo("C#");
        assertThat(AudioConstants.NOTE_NAMES[3]).isEqualTo("D#");
        assertThat(AudioConstants.NOTE_NAMES[6]).isEqualTo("F#");
        assertThat(AudioConstants.NOTE_NAMES[8]).isEqualTo("G#");
        assertThat(AudioConstants.NOTE_NAMES[10]).isEqualTo("A#");
    }

    @Test
    @DisplayName("E and B notes should not have sharps (natural semitone)")
    void testNoSharpForEandB() {
        // E (index 4) is followed by F (index 5)
        // B (index 11) is followed by C (index 0, next octave)
        // So no sharp between E-F and B-C
        assertThat(AudioConstants.NOTE_NAMES[4]).isEqualTo("E");
        assertThat(AudioConstants.NOTE_NAMES[5]).isEqualTo("F");
        assertThat(AudioConstants.NOTE_NAMES[11]).isEqualTo("B");
    }

    @Test
    @DisplayName("All note names should be non-empty")
    void testAllNoteNamesNonEmpty() {
        for (String note : AudioConstants.NOTE_NAMES) {
            assertThat(note)
                    .isNotEmpty();
        }
    }

    // ========== Piano Range Tests ==========

    @Test
    @DisplayName("LOWEST_MIDI_NOTE should be A0 (MIDI note 21)")
    void testLowestMIDINote() {
        assertThat(AudioConstants.LOWEST_MIDI_NOTE)
                .isEqualTo(21);
    }

    @Test
    @DisplayName("HIGHEST_MIDI_NOTE should be C8 (MIDI note 108)")
    void testHighestMIDINote() {
        assertThat(AudioConstants.HIGHEST_MIDI_NOTE)
                .isEqualTo(108);
    }

    @Test
    @DisplayName("Piano range should span 88 notes (A0 to C8)")
    void testPianoRangeSpan() {
        int pianoRange = AudioConstants.HIGHEST_MIDI_NOTE - AudioConstants.LOWEST_MIDI_NOTE + 1;
        assertThat(pianoRange)
                .isEqualTo(88);
    }

    @Test
    @DisplayName("Piano lowest note (A0) should be less than A4")
    void testLowestNoteBeforeA4() {
        assertThat(AudioConstants.LOWEST_MIDI_NOTE)
                .isLessThan(AudioConstants.A4_NOTE_NUMBER);
    }

    @Test
    @DisplayName("Piano highest note (C8) should be greater than A4")
    void testHighestNoteAfterA4() {
        assertThat(AudioConstants.HIGHEST_MIDI_NOTE)
                .isGreaterThan(AudioConstants.A4_NOTE_NUMBER);
    }

    @Test
    @DisplayName("A4 should be within piano range")
    void testA4WithinPianoRange() {
        assertThat(AudioConstants.A4_NOTE_NUMBER)
                .isBetween(AudioConstants.LOWEST_MIDI_NOTE, AudioConstants.HIGHEST_MIDI_NOTE);
    }

    // ========== Relationship Consistency Tests ==========

    @Test
    @DisplayName("LOWEST_MIDI_NOTE should map to a valid note name")
    void testLowestNoteIsValid() {
        int noteIndex = AudioConstants.LOWEST_MIDI_NOTE % AudioConstants.SEMITONES_PER_OCTAVE;
        assertThat(noteIndex)
                .isBetween(0, AudioConstants.SEMITONES_PER_OCTAVE - 1);
    }

    @Test
    @DisplayName("HIGHEST_MIDI_NOTE should map to a valid note name")
    void testHighestNoteIsValid() {
        int noteIndex = AudioConstants.HIGHEST_MIDI_NOTE % AudioConstants.SEMITONES_PER_OCTAVE;
        assertThat(noteIndex)
                .isBetween(0, AudioConstants.SEMITONES_PER_OCTAVE - 1);
    }

    @Test
    @DisplayName("A4 note number should be consistent with SEMITONES_PER_OCTAVE")
    void testA4NotesConsistency() {
        int octave = AudioConstants.A4_NOTE_NUMBER / AudioConstants.SEMITONES_PER_OCTAVE;
        int noteInOctave = AudioConstants.A4_NOTE_NUMBER % AudioConstants.SEMITONES_PER_OCTAVE;

        // A4 = MIDI 69, which is (5 * 12) + 9 = octave 5, note index 9 (A)
        assertThat(octave).isEqualTo(5);
        assertThat(noteInOctave).isEqualTo(9);
        assertThat(AudioConstants.NOTE_NAMES[noteInOctave]).isEqualTo("A");
    }

    // ========== Value Range Tests ==========

    @Test
    @DisplayName("SAMPLE_RATE should be positive")
    void testSampleRatePositive() {
        assertThat(AudioConstants.SAMPLE_RATE)
                .isPositive();
    }

    @Test
    @DisplayName("FFT_SIZE should be power of 2")
    void testFFTSizeIsPowerOfTwo() {
        int fftSize = AudioConstants.FFT_SIZE;
        // Check if power of 2: (n & (n - 1)) == 0
        assertThat(fftSize & (fftSize - 1))
                .isEqualTo(0);
    }

    @Test
    @DisplayName("FFT_SIZE should be reasonable (between 256 and 16384)")
    void testFFTSizeReasonable() {
        assertThat(AudioConstants.FFT_SIZE)
                .isBetween(256, 16384);
    }

    @Test
    @DisplayName("MIDI note numbers should be in valid MIDI range [0, 127]")
    void testMIDINoteNumbersInValidRange() {
        assertThat(AudioConstants.A4_NOTE_NUMBER)
                .isBetween(0, 127);
        assertThat(AudioConstants.LOWEST_MIDI_NOTE)
                .isBetween(0, 127);
        assertThat(AudioConstants.HIGHEST_MIDI_NOTE)
                .isBetween(0, 127);
    }

    @Test
    @DisplayName("A4 frequency should be reasonable for concert pitch (430-450 Hz)")
    void testA4FrequencyReasonable() {
        assertThat(AudioConstants.A4_FREQUENCY)
                .isBetween(430.0, 450.0);
    }

    // ========== Immutability Tests ==========

    @Test
    @DisplayName("Array should not be modified during test access")
    void testArrayNotModified() {
        String originalFirstNote = AudioConstants.NOTE_NAMES[0];

        // Attempt to modify (we can't actually prevent this, but test documents expected behavior)
        assertThat(AudioConstants.NOTE_NAMES[0])
                .isEqualTo(originalFirstNote);
    }

    // ========== Integration Tests ==========

    @Test
    @DisplayName("Sample rate should support Nyquist coverage up to 20 kHz (human hearing)")
    void testNyquistCoverageHumanHearing() {
        double nyquistFrequency = AudioConstants.SAMPLE_RATE / 2.0;
        assertThat(nyquistFrequency)
                .isGreaterThanOrEqualTo(20000.0); // 20 kHz human hearing upper limit
    }

    @Test
    @DisplayName("FFT resolution should provide at least 10 Hz per bin")
    void testFFTResolutionAcceptable() {
        double binsPerHz = AudioConstants.FFT_SIZE / AudioConstants.SAMPLE_RATE;
        double hzPerBin = AudioConstants.SAMPLE_RATE / AudioConstants.FFT_SIZE;

        assertThat(hzPerBin)
                .isLessThan(11.0); // ~10.77 Hz per bin for 44.1kHz/4096
    }

    @Test
    @DisplayName("BUFFER_SIZE * sample_rate should represent about 93ms of audio")
    void testBufferDurationReasonable() {
        double durationMs = (AudioConstants.BUFFER_SIZE / AudioConstants.SAMPLE_RATE) * 1000.0;
        // 4096 / 44100 * 1000 ≈ 92.88 ms
        assertThat(durationMs)
                .isCloseTo(92.88, within(0.1));
    }

}

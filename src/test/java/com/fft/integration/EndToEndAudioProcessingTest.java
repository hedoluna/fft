package com.fft.integration;

import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;
import com.fft.utils.PitchDetectionUtils;
import com.fft.demo.ParsonsCodeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-end integration tests for complete audio processing workflows.
 *
 * <p>Tests realistic scenarios from signal generation through FFT, pitch detection,
 * and musical analysis.</p>
 *
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("End-to-End Audio Processing Integration Tests")
class EndToEndAudioProcessingTest {

    private static final double SAMPLE_RATE = 44100.0;
    private static final double FREQUENCY_TOLERANCE = 10.0;

    /**
     * Generate a realistic audio signal with multiple harmonics.
     */
    private double[] generateRealisticTone(double fundamental, double duration, double amplitude) {
        int numSamples = (int) (SAMPLE_RATE * duration);
        double[] signal = new double[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double t = i / SAMPLE_RATE;
            // Fundamental + harmonics
            signal[i] = amplitude * (
                Math.sin(2.0 * Math.PI * fundamental * t) +
                0.5 * Math.sin(2.0 * Math.PI * 2 * fundamental * t) +
                0.25 * Math.sin(2.0 * Math.PI * 3 * fundamental * t)
            );
        }

        return signal;
    }

    @Nested
    @DisplayName("Signal to Pitch Workflow")
    class SignalToPitchWorkflow {

        @Test
        @DisplayName("Should detect pitch from raw audio signal")
        void shouldDetectPitchFromRawSignal() {
            double expectedFreq = 440.0; // A4
            double[] signal = generateRealisticTone(expectedFreq, 0.5, 0.8);

            // Complete workflow: signal → FFT → spectral analysis → pitch
            double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
            FFTResult spectrum = FFTUtils.fft(paddedSignal);
            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchSpectral(spectrum, SAMPLE_RATE);

            assertThat(result.isVoiced).isTrue();
            assertThat(result.frequency).isCloseTo(expectedFreq, within(FREQUENCY_TOLERANCE));
        }

        @Test
        @DisplayName("Should process complete melody sequence")
        void shouldProcessMelodySequence() {
            // Simulate a melody: C4 → E4 → G4 → C5
            double[] frequencies = {261.63, 329.63, 392.00, 523.25};
            double[] detectedFreqs = new double[frequencies.length];

            for (int i = 0; i < frequencies.length; i++) {
                double[] signal = generateRealisticTone(frequencies[i], 0.3, 0.8);
                PitchDetectionUtils.PitchResult result =
                    PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE);

                detectedFreqs[i] = result.frequency;
            }

            // Verify all notes were detected correctly
            for (int i = 0; i < frequencies.length; i++) {
                assertThat(detectedFreqs[i])
                    .as("Note %d detection", i)
                    .isCloseTo(frequencies[i], within(FREQUENCY_TOLERANCE));
            }

            // Generate Parsons code from detected frequencies
            String parsonsCode = ParsonsCodeUtils.generateParsonsCode(detectedFreqs);
            assertThat(parsonsCode).startsWith("*");
            assertThat(parsonsCode.length()).isEqualTo(frequencies.length);
        }
    }

    @Nested
    @DisplayName("FFT to Musical Analysis Workflow")
    class FFTToMusicalAnalysisWorkflow {

        @Test
        @DisplayName("Should analyze chord from FFT spectrum")
        void shouldAnalyzeChordFromSpectrum() {
            // Generate C major chord: C4, E4, G4
            int numSamples = 8192;
            double[] signal = new double[numSamples];

            for (int i = 0; i < numSamples; i++) {
                double t = i / SAMPLE_RATE;
                signal[i] = Math.sin(2 * Math.PI * 261.63 * t) +  // C4
                           Math.sin(2 * Math.PI * 329.63 * t) +  // E4
                           Math.sin(2 * Math.PI * 392.00 * t);   // G4
            }

            // Workflow: signal → FFT → chord detection
            FFTResult spectrum = FFTUtils.fft(signal);
            PitchDetectionUtils.ChordResult chord =
                PitchDetectionUtils.detectChord(spectrum, SAMPLE_RATE, 4);

            assertThat(chord.frequencies).hasSizeGreaterThanOrEqualTo(2);
            assertThat(chord.chordType).containsIgnoringCase("major");
        }

        @Test
        @DisplayName("Should extract harmonic content from FFT")
        void shouldExtractHarmonicContent() {
            double fundamental = 440.0;
            double[] signal = generateRealisticTone(fundamental, 0.5, 1.0);
            double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);

            FFTResult spectrum = FFTUtils.fft(paddedSignal);
            double[] magnitudes = spectrum.getMagnitudes();

            // Verify fundamental and harmonics are present
            int fundamentalBin = PitchDetectionUtils.frequencyToBin(
                fundamental, SAMPLE_RATE, magnitudes.length);
            int secondHarmonicBin = PitchDetectionUtils.frequencyToBin(
                2 * fundamental, SAMPLE_RATE, magnitudes.length);

            assertThat(magnitudes[fundamentalBin]).isGreaterThan(magnitudes[fundamentalBin - 10]);
            assertThat(magnitudes[secondHarmonicBin]).isGreaterThan(magnitudes[secondHarmonicBin - 10]);
        }
    }

    @Nested
    @DisplayName("Multi-Step Processing Workflows")
    class MultiStepWorkflows {

        @Test
        @DisplayName("Should process signal through complete pipeline")
        void shouldProcessThroughCompletePipeline() {
            // Step 1: Generate test signal
            double[] signal = FFTUtils.generateSineWave(256, 440.0, SAMPLE_RATE);

            // Step 2: Verify signal generation
            assertThat(signal).hasSize(256);

            // Step 3: Zero-pad if necessary
            double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
            assertThat(FFTUtils.isPowerOfTwo(paddedSignal.length)).isTrue();

            // Step 4: Perform FFT
            FFTResult spectrum = FFTUtils.fft(paddedSignal);
            assertThat(spectrum.size()).isEqualTo(paddedSignal.length);

            // Step 5: Extract magnitudes
            double[] magnitudes = spectrum.getMagnitudes();
            assertThat(magnitudes).hasSize(paddedSignal.length);

            // Step 6: Detect pitch from spectrum
            PitchDetectionUtils.PitchResult pitch =
                PitchDetectionUtils.detectPitchSpectral(spectrum, SAMPLE_RATE);

            assertThat(pitch.isVoiced).isTrue();
            assertThat(pitch.frequency).isCloseTo(440.0, within(FREQUENCY_TOLERANCE));
        }

        @Test
        @DisplayName("Should handle inverse transform round-trip")
        void shouldHandleInverseTransformRoundTrip() {
            // Generate original signal
            double[] original = FFTUtils.generateTestSignal(512, "mixed");

            // Forward transform
            FFTResult forward = FFTUtils.fft(original.clone(), true);

            // Inverse transform
            FFTResult inverse = FFTUtils.fft(
                forward.getRealParts(),
                forward.getImaginaryParts(),
                false
            );

            // Verify round-trip accuracy
            double[] recovered = inverse.getRealParts();
            assertThat(recovered).hasSize(original.length);

            for (int i = 0; i < original.length; i++) {
                assertThat(recovered[i]).isCloseTo(original[i], within(1e-10));
            }
        }
    }

    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Should handle instrument tuner scenario")
        void shouldHandleInstrumentTunerScenario() {
            // Simulate slightly detuned A4 (should be 440 Hz)
            double detunedFreq = 437.5; // -10 cents
            double[] signal = generateRealisticTone(detunedFreq, 1.0, 0.9);

            // Detect pitch
            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE);

            assertThat(result.isVoiced).isTrue();
            assertThat(result.frequency).isCloseTo(detunedFreq, within(5.0));

            // Calculate cents deviation from A440
            double centsDeviation = 1200 * Math.log(result.frequency / 440.0) / Math.log(2);
            assertThat(Math.abs(centsDeviation)).isLessThan(20); // Within ±20 cents
        }

        @Test
        @DisplayName("Should handle melody recognition scenario")
        void shouldHandleMelodyRecognitionScenario() {
            // Simulate "Twinkle Twinkle Little Star" beginning (C C G G A A G)
            double[] melody = {261.63, 261.63, 392.00, 392.00, 440.00, 440.00, 392.00};
            double[] detectedFreqs = new double[melody.length];

            // Detect each note
            for (int i = 0; i < melody.length; i++) {
                double[] noteSignal = generateRealisticTone(melody[i], 0.4, 0.8);
                PitchDetectionUtils.PitchResult result =
                    PitchDetectionUtils.detectPitchHybrid(noteSignal, SAMPLE_RATE);
                detectedFreqs[i] = result.frequency;
            }

            // Generate Parsons code
            String parsonsCode = ParsonsCodeUtils.generateParsonsCode(detectedFreqs);

            // Expected: *RUURRD (R=repeat, U=up, D=down)
            assertThat(parsonsCode).hasSize(melody.length);
            assertThat(parsonsCode.charAt(0)).isEqualTo('*');
        }

        @Test
        @DisplayName("Should handle noisy signal scenario")
        void shouldHandleNoisySignalScenario() {
            double frequency = 440.0;
            double[] signal = generateRealisticTone(frequency, 0.5, 0.8);

            // Add moderate noise (SNR ~10dB)
            java.util.Random random = new java.util.Random(42);
            for (int i = 0; i < signal.length; i++) {
                signal[i] += (random.nextDouble() - 0.5) * 0.25;
            }

            // Hybrid method should still detect correctly
            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE);

            assertThat(result.isVoiced).isTrue();
            // Allow larger tolerance for noisy signal
            assertThat(result.frequency).isCloseTo(frequency, within(20.0));
        }
    }

    @Nested
    @DisplayName("Edge Cases in Complete Workflows")
    class EdgeCaseWorkflows {

        @Test
        @DisplayName("Should handle very short signal")
        void shouldHandleVeryShortSignal() {
            double[] shortSignal = new double[64];
            for (int i = 0; i < shortSignal.length; i++) {
                shortSignal[i] = Math.sin(2 * Math.PI * 440.0 * i / SAMPLE_RATE);
            }

            // Complete workflow should not crash
            assertThatCode(() -> {
                FFTResult spectrum = FFTUtils.fft(shortSignal);
                PitchDetectionUtils.detectPitchSpectral(spectrum, SAMPLE_RATE);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle silence in complete workflow")
        void shouldHandleSilenceInWorkflow() {
            double[] silence = new double[4096];

            PitchDetectionUtils.PitchResult result =
                PitchDetectionUtils.detectPitchHybrid(silence, SAMPLE_RATE);

            assertThat(result.isVoiced).isFalse();
            assertThat(result.frequency).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle frequency sweep")
        void shouldHandleFrequencySweep() {
            // Chirp signal: frequency increases over time
            int numSamples = 8192;
            double[] chirp = new double[numSamples];
            double startFreq = 200.0;
            double endFreq = 800.0;

            for (int i = 0; i < numSamples; i++) {
                double t = i / SAMPLE_RATE;
                double freq = startFreq + (endFreq - startFreq) * t * 2;
                chirp[i] = Math.sin(2 * Math.PI * freq * t);
            }

            // Process sweep
            FFTResult spectrum = FFTUtils.fft(chirp);

            assertThat(spectrum.size()).isEqualTo(numSamples);
            assertThat(spectrum.getMagnitudes()).isNotEmpty();
        }
    }
}

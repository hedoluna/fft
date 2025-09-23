package com.fft.demo;

import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Real-time pitch and frequency detection demo using optimized FFTs.
 * 
 * <p>This demo captures live audio from the microphone, analyzes it using FFT,
 * and detects the fundamental frequency (pitch) in real-time. It provides the
 * foundation for the second goal of song recognition using Parsons code.</p>
 * 
 * <h3>Features:</h3>
 * <ul>
 * <li><b>Real-time Audio Capture:</b> Uses Java Sound API to capture microphone input</li>
 * <li><b>FFT-based Analysis:</b> Leverages optimized FFT implementations for frequency analysis</li>
 * <li><b>Pitch Detection:</b> Identifies fundamental frequency and musical note</li>
 * <li><b>Frequency Visualization:</b> Displays frequency spectrum and peak detection</li>
 * <li><b>Musical Note Recognition:</b> Converts frequencies to musical notes</li>
 * <li><b>Parsons Code Preparation:</b> Tracks pitch changes for melody analysis</li>
 * </ul>
 * 
 * <h3>Algorithm Overview:</h3>
 * <pre>
 * 1. Capture audio samples from microphone
 * 2. Apply windowing function (Hamming window) to reduce spectral leakage
 * 3. Perform voicing detection to distinguish sound from silence
 * 4. Use YIN algorithm for accurate fundamental frequency estimation
 * 5. Apply spectral analysis as fallback method
 * 6. Use median filtering for pitch stability
 * 7. Convert frequency to musical note
 * 8. Track pitch changes for Parsons code generation
 * </pre>
 *
 * <h3>Key Improvements:</h3>
 * <ul>
 * <li><b>YIN Algorithm:</b> More accurate pitch detection using autocorrelation</li>
 * <li><b>Voicing Detection:</b> Distinguishes between sound and silence</li>
 * <li><b>Median Filtering:</b> Reduces pitch jitter for stable detection</li>
 * <li><b>Dynamic Processing:</b> Adapts to signal characteristics</li>
 * </ul>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
public class PitchDetectionDemo {
    
    // Audio configuration
    private static final float SAMPLE_RATE = 44100.0f;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    
    // FFT configuration
    private static final int FFT_SIZE = 4096;  // Good balance of frequency resolution and real-time performance
    private static final int OVERLAP_SIZE = FFT_SIZE / 2;  // 50% overlap for better temporal resolution
    private static final double MIN_FREQUENCY = 80.0;   // Lowest guitar string (E2)
    private static final double MAX_FREQUENCY = 2000.0; // Upper harmonics range
    
    // Pitch detection parameters
    private static final double MAGNITUDE_THRESHOLD = 0.01;  // Minimum magnitude to consider as valid signal
    private static final int SMOOTHING_WINDOW = 5;  // Number of frames to average for stability

    // YIN algorithm parameters
    private static final double YIN_THRESHOLD = 0.15;  // Threshold for voicing detection
    private static final int YIN_MIN_PERIOD = (int)(SAMPLE_RATE / MAX_FREQUENCY);  // Minimum period in samples
    private static final int YIN_MAX_PERIOD = (int)(SAMPLE_RATE / MIN_FREQUENCY);  // Maximum period in samples

    // Voicing detection parameters
    private static final double VOICING_THRESHOLD = 0.001;  // RMS threshold for sound detection
    private static final int VOICING_HISTORY_SIZE = 10;  // Frames to consider for voicing stability
    
    // Musical note data
    private static final String[] NOTE_NAMES = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    private static final double A4_FREQUENCY = 440.0;  // A4 reference frequency
    private static final int A4_NOTE_NUMBER = 69;      // MIDI note number for A4
    
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Queue<Double> recentPitches = new ArrayDeque<>();
    private final List<PitchChange> parsonsSequence = new ArrayList<>();
    private final Queue<Boolean> voicingHistory = new ArrayDeque<>();
    private double lastStablePitch = 0.0;
    private boolean isVoiced = false;
    
    /**
     * Application entry point for the real-time pitch detection demo.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        System.out.println("=== FFT-Based Pitch Detection Demo ===\n");
        
        PitchDetectionDemo demo = new PitchDetectionDemo();
        demo.runDemo();
    }
    
    /**
     * Captures live audio and performs continuous pitch detection until the
     * user stops the demo.
     */
    public void runDemo() {
        System.out.println("Setting up audio capture...");
        
        AudioFormat format = new AudioFormat(
            SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN
        );
        
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        
        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Audio line not supported!");
            return;
        }
        
        try (TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info)) {
            microphone.open(format);
            microphone.start();
            
            System.out.println("Audio capture started!");
            System.out.println("Optimized FFT implementation: " + FFTUtils.getImplementationInfo(FFT_SIZE));
            System.out.println("Sample rate: " + SAMPLE_RATE + " Hz");
            System.out.println("FFT size: " + FFT_SIZE + " samples");
            System.out.printf("Frequency resolution: %.2f Hz\n", SAMPLE_RATE / FFT_SIZE);
            System.out.println("\nPress Enter to stop...\n");
            
            // Start background thread to detect Enter key
            Thread stopThread = new Thread(() -> {
                try {
                    System.in.read();
                    running.set(false);
                } catch (IOException e) {
                    // Ignore
                }
            });
            stopThread.setDaemon(true);
            stopThread.start();
            
            running.set(true);
            processAudioStream(microphone);
            
        } catch (LineUnavailableException e) {
            System.err.println("Could not open microphone: " + e.getMessage());
        }
        
        printParsonsCode();
    }
    
    /**
     * Continuously reads audio from the microphone and performs pitch analysis
     * on successive frames.
     *
     * @param microphone initialized and started {@link TargetDataLine}
     */
    private void processAudioStream(TargetDataLine microphone) {
        byte[] buffer = new byte[FFT_SIZE * 2]; // 16-bit samples
        double[] audioSamples = new double[FFT_SIZE];
        double[] previousSamples = new double[OVERLAP_SIZE];
        
        int frameCount = 0;
        
        while (running.get()) {
            // Read audio data
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            if (bytesRead <= 0) continue;
            
            // Convert bytes to double samples
            convertBytesToSamples(buffer, audioSamples, bytesRead);
            
            // Apply overlap from previous frame
            if (frameCount > 0) {
                System.arraycopy(previousSamples, 0, audioSamples, 0, OVERLAP_SIZE);
            }
            
            // Store samples for next overlap
            System.arraycopy(audioSamples, FFT_SIZE - OVERLAP_SIZE, previousSamples, 0, OVERLAP_SIZE);
            
            // Apply windowing function
            applyHammingWindow(audioSamples);

            // Check if signal is voiced (contains sound)
            boolean currentVoiced = isSignalVoiced(audioSamples);
            updateVoicingHistory(currentVoiced);

            // Perform FFT analysis
            FFTResult spectrum = FFTUtils.fft(audioSamples);

            PitchDetectionResult pitchResult;

            if (isVoiced) {
                // Detect pitch using YIN algorithm (more accurate)
                pitchResult = detectPitchYin(audioSamples);

                // Fallback to spectral method if YIN fails
                if (pitchResult.frequency == 0.0) {
                    pitchResult = detectPitch(spectrum);
                }
            } else {
                // No sound detected
                pitchResult = new PitchDetectionResult(0.0, 0.0, "Silence", 0);
            }

            // Apply smoothing to pitch detection
            pitchResult = smoothPitchDetection(pitchResult);

            // Display results
            displayResults(frameCount, pitchResult);

            // Track for Parsons code
            updateParsonsSequence(pitchResult);
            
            frameCount++;
            
            // Small delay to make output readable
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    /**
     * Converts raw little-endian PCM bytes to normalized double samples.
     *
     * @param buffer audio byte buffer
     * @param samples destination array for normalized samples
     * @param bytesRead number of valid bytes in the buffer
     */
    private void convertBytesToSamples(byte[] buffer, double[] samples, int bytesRead) {
        int sampleCount = Math.min(bytesRead / 2, samples.length);
        
        for (int i = 0; i < sampleCount; i++) {
            // Convert 16-bit little-endian to double
            int sample = (buffer[i * 2 + 1] << 8) | (buffer[i * 2] & 0xFF);
            samples[i] = sample / 32768.0; // Normalize to [-1, 1]
        }
        
        // Zero-pad if necessary
        for (int i = sampleCount; i < samples.length; i++) {
            samples[i] = 0.0;
        }
    }
    
    /**
     * Applies a Hamming window to the provided sample array in-place.
     *
     * @param samples audio frame to window
     */
    private void applyHammingWindow(double[] samples) {
        int n = samples.length;
        for (int i = 0; i < n; i++) {
            double window = 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / (n - 1));
            samples[i] *= window;
        }
    }
    
    /**
     * Determines the fundamental frequency of the provided spectrum.
     *
     * @param spectrum FFT result of the current audio frame
     * @return detection result containing frequency, magnitude and note name
     */
    private PitchDetectionResult detectPitch(FFTResult spectrum) {
        double[] magnitudes = spectrum.getMagnitudes();
        int maxBin = 0;
        double maxMagnitude = 0.0;
        
        // Find frequency range of interest
        int minBin = frequencyToBin(MIN_FREQUENCY);
        int maxBinIndex = Math.min(frequencyToBin(MAX_FREQUENCY), magnitudes.length / 2);
        
        // Find peak in frequency domain
        for (int i = minBin; i < maxBinIndex; i++) {
            if (magnitudes[i] > maxMagnitude) {
                maxMagnitude = magnitudes[i];
                maxBin = i;
            }
        }
        
        if (maxMagnitude < MAGNITUDE_THRESHOLD) {
            return new PitchDetectionResult(0.0, 0.0, "Silence", 0);
        }
        
        // Improve frequency accuracy using parabolic interpolation
        double refinedFrequency = refineFrequencyEstimate(magnitudes, maxBin);
        
        // Apply harmonic analysis for better pitch detection
        double fundamentalFreq = findFundamentalFrequency(magnitudes, refinedFrequency);
        
        // Convert to musical note
        String noteName = frequencyToNote(fundamentalFreq);
        int octave = frequencyToOctave(fundamentalFreq);
        
        return new PitchDetectionResult(fundamentalFreq, maxMagnitude, noteName, octave);
    }
    
    /**
     * Uses parabolic interpolation to refine the frequency estimate of a peak
     * bin.
     *
     * @param magnitudes spectrum magnitudes
     * @param peakBin index of the spectral peak
     * @return interpolated frequency in Hz
     */
    private double refineFrequencyEstimate(double[] magnitudes, int peakBin) {
        if (peakBin <= 0 || peakBin >= magnitudes.length - 1) {
            return binToFrequency(peakBin);
        }
        
        // Parabolic interpolation for sub-bin accuracy
        double y1 = magnitudes[peakBin - 1];
        double y2 = magnitudes[peakBin];
        double y3 = magnitudes[peakBin + 1];
        
        double a = (y1 - 2 * y2 + y3) / 2;
        double b = (y3 - y1) / 2;
        
        double xPeak = a != 0 ? -b / (2 * a) : 0;
        
        return binToFrequency(peakBin + xPeak);
    }
    
    /**
     * Performs pitch detection using the YIN algorithm for improved accuracy.
     *
     * @param audioSamples the audio samples to analyze
     * @return pitch detection result with improved accuracy
     */
    private PitchDetectionResult detectPitchYin(double[] audioSamples) {
        // Apply YIN algorithm for more accurate pitch detection
        double[] yinBuffer = new double[YIN_MAX_PERIOD];
        double[] cmnd = new double[YIN_MAX_PERIOD];  // Cumulative Mean Normalized Difference

        // Calculate difference function
        for (int tau = YIN_MIN_PERIOD; tau < YIN_MAX_PERIOD; tau++) {
            double sum = 0.0;
            for (int i = 0; i < audioSamples.length - tau; i++) {
                double diff = audioSamples[i] - audioSamples[i + tau];
                sum += diff * diff;
            }
            yinBuffer[tau] = sum;
        }

        // Calculate cumulative mean normalized difference
        cmnd[0] = 1.0;  // By definition
        double runningSum = 0.0;
        for (int tau = YIN_MIN_PERIOD; tau < YIN_MAX_PERIOD; tau++) {
            runningSum += yinBuffer[tau];
            cmnd[tau] = yinBuffer[tau] / ((1.0 / tau) * runningSum);
        }

        // Find minimum below threshold
        int bestTau = -1;
        double minCmnd = Double.MAX_VALUE;
        for (int tau = YIN_MIN_PERIOD; tau < YIN_MAX_PERIOD; tau++) {
            if (cmnd[tau] < YIN_THRESHOLD && cmnd[tau] < minCmnd) {
                minCmnd = cmnd[tau];
                bestTau = tau;
            }
        }

        if (bestTau == -1) {
            // No voiced sound detected
            return new PitchDetectionResult(0.0, 0.0, "N/A", 0);
        }

        // Refine the period estimate using parabolic interpolation
        double refinedTau = refineTauEstimate(cmnd, bestTau);

        // Convert period to frequency
        double frequency = SAMPLE_RATE / refinedTau;

        // Validate frequency range
        if (frequency < MIN_FREQUENCY || frequency > MAX_FREQUENCY) {
            return new PitchDetectionResult(0.0, 0.0, "N/A", 0);
        }

        // Convert to musical note
        String noteName = frequencyToNote(frequency);
        int octave = frequencyToOctave(frequency);

        return new PitchDetectionResult(frequency, minCmnd, noteName, octave);
    }

    /**
     * Refines the period estimate using parabolic interpolation around the minimum.
     *
     * @param cmnd the cumulative mean normalized difference array
     * @param tau the initial tau estimate
     * @return refined tau value
     */
    private double refineTauEstimate(double[] cmnd, int tau) {
        if (tau <= YIN_MIN_PERIOD || tau >= YIN_MAX_PERIOD - 1) {
            return tau;
        }

        double x1 = cmnd[tau - 1];
        double x2 = cmnd[tau];
        double x3 = cmnd[tau + 1];

        // Parabolic interpolation: y = ax^2 + bx + c
        double a = (x1 - 2*x2 + x3) / 2;
        double b = (x3 - x1) / 2;

        if (a != 0) {
            double delta = -b / (2 * a);
            return tau + delta;
        }

        return tau;
    }

    /**
     * Determines if the audio signal contains voiced sound (not silence).
     *
     * @param samples the audio samples to analyze
     * @return true if the signal is voiced, false if silent
     */
    private boolean isSignalVoiced(double[] samples) {
        // Calculate RMS (Root Mean Square) energy
        double sum = 0.0;
        for (double sample : samples) {
            sum += sample * sample;
        }
        double rms = Math.sqrt(sum / samples.length);

        return rms > VOICING_THRESHOLD;
    }

    /**
     * Updates the voicing history for stability analysis.
     *
     * @param currentVoiced current voicing state
     */
    private void updateVoicingHistory(boolean currentVoiced) {
        voicingHistory.add(currentVoiced);
        if (voicingHistory.size() > VOICING_HISTORY_SIZE) {
            voicingHistory.poll();
        }

        // Determine stable voicing state (majority vote)
        long voicedCount = voicingHistory.stream().filter(v -> v).count();
        isVoiced = voicedCount > voicingHistory.size() / 2;
    }

    /**
     * Applies smoothing to pitch detection results for stability.
     *
     * @param currentResult the current pitch detection result
     * @return smoothed pitch detection result
     */
    private PitchDetectionResult smoothPitchDetection(PitchDetectionResult currentResult) {
        if (!isVoiced || currentResult.frequency == 0.0) {
            // Clear pitch history when no sound is detected
            recentPitches.clear();
            lastStablePitch = 0.0;
            return currentResult;
        }

        // Add current pitch to history
        recentPitches.add(currentResult.frequency);
        if (recentPitches.size() > SMOOTHING_WINDOW) {
            recentPitches.poll();
        }

        // Calculate median pitch for stability
        if (recentPitches.size() >= SMOOTHING_WINDOW / 2) {
            double[] pitchArray = recentPitches.stream().mapToDouble(Double::doubleValue).toArray();
            double medianPitch = calculateMedian(pitchArray);

            // Check if pitch is stable (within 5% of median)
            double stabilityThreshold = medianPitch * 0.05;
            if (Math.abs(currentResult.frequency - medianPitch) < stabilityThreshold) {
                lastStablePitch = medianPitch;
                return new PitchDetectionResult(medianPitch, currentResult.magnitude,
                                              frequencyToNote(medianPitch), frequencyToOctave(medianPitch));
            }
        }

        // Return current result if not stable enough
        return currentResult;
    }

    /**
     * Calculates the median of an array of values.
     *
     * @param values the array of values
     * @return the median value
     */
    private double calculateMedian(double[] values) {
        if (values.length == 0) return 0.0;

        double[] sorted = values.clone();
        java.util.Arrays.sort(sorted);

        if (sorted.length % 2 == 0) {
            return (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2.0;
        } else {
            return sorted[sorted.length / 2];
        }
    }

    /**
     * Performs a simple harmonic analysis to determine the most likely
     * fundamental frequency (fallback method).
     *
     * @param magnitudes spectrum magnitudes
     * @param candidateFreq initial frequency estimate
     * @return refined fundamental frequency
     */
    private double findFundamentalFrequency(double[] magnitudes, double candidateFreq) {
        // Simple harmonic analysis - check if this might be a harmonic
        // Look for a potential fundamental at half, third, quarter, etc.

        double[] candidates = {candidateFreq, candidateFreq / 2, candidateFreq / 3, candidateFreq / 4};
        double bestScore = 0;
        double bestFreq = candidateFreq;

        for (double testFreq : candidates) {
            if (testFreq < MIN_FREQUENCY) continue;

            double score = calculateHarmonicScore(magnitudes, testFreq);
            if (score > bestScore) {
                bestScore = score;
                bestFreq = testFreq;
            }
        }

        return bestFreq;
    }
    
    /**
     * Calculates a simple score representing how strong the harmonics of a
     * given frequency are in the spectrum.
     *
     * @param magnitudes magnitude spectrum
     * @param fundamentalFreq candidate fundamental frequency
     * @return harmonic strength score
     */
    private double calculateHarmonicScore(double[] magnitudes, double fundamentalFreq) {
        double score = 0;
        
        // Check strength of fundamental and first few harmonics
        for (int harmonic = 1; harmonic <= 4; harmonic++) {
            double harmonicFreq = fundamentalFreq * harmonic;
            if (harmonicFreq > MAX_FREQUENCY) break;
            
            int bin = frequencyToBin(harmonicFreq);
            if (bin < magnitudes.length) {
                score += magnitudes[bin] / harmonic; // Weight by inverse of harmonic number
            }
        }
        
        return score;
    }
    
    /**
     * Converts a frequency in Hz to the corresponding FFT bin index.
     *
     * @param frequency frequency in Hz
     * @return bin index
     */
    private int frequencyToBin(double frequency) {
        return (int) Math.round(frequency * FFT_SIZE / SAMPLE_RATE);
    }

    /**
     * Converts a bin index to the corresponding frequency in Hz.
     *
     * @param bin FFT bin index
     * @return frequency in Hz
     */
    private double binToFrequency(double bin) {
        return bin * SAMPLE_RATE / FFT_SIZE;
    }

    /**
     * Maps a frequency to the nearest musical note name.
     *
     * @param frequency frequency in Hz
     * @return note name or {@code "N/A"} if invalid
     */
    private String frequencyToNote(double frequency) {
        if (frequency <= 0) return "N/A";
        
        int noteNumber = (int) Math.round(A4_NOTE_NUMBER + 12 * Math.log(frequency / A4_FREQUENCY) / Math.log(2));
        return NOTE_NAMES[noteNumber % 12];
    }
    
    /**
     * Calculates the octave number for a given frequency.
     *
     * @param frequency frequency in Hz
     * @return octave number or 0 if frequency is invalid
     */
    private int frequencyToOctave(double frequency) {
        if (frequency <= 0) return 0;
        
        int noteNumber = (int) Math.round(A4_NOTE_NUMBER + 12 * Math.log(frequency / A4_FREQUENCY) / Math.log(2));
        return (noteNumber / 12) - 1;
    }
    
    /**
     * Prints pitch detection results to the console every few frames.
     *
     * @param frameCount index of the current audio frame
     * @param result detected pitch information
     */
    private void displayResults(int frameCount, PitchDetectionResult result) {
        if (frameCount % 10 == 0) { // Display every 10th frame to avoid overwhelming output
            System.out.printf("Frame %d: ", frameCount);
            
            if (result.frequency > 0) {
                System.out.printf("Pitch: %s%d (%.1f Hz) | Magnitude: %.3f\n",
                    result.noteName, result.octave, result.frequency, result.magnitude);
            } else {
                System.out.println("No pitch detected");
            }
        }
    }
    
    /**
     * Updates the internal Parsons code sequence based on the latest pitch
     * detection result.
     *
     * @param result detected pitch information
     */
    private void updateParsonsSequence(PitchDetectionResult result) {
        if (result.frequency <= 0) return;
        
        // Add to recent pitches for smoothing
        recentPitches.offer(result.frequency);
        if (recentPitches.size() > SMOOTHING_WINDOW) {
            recentPitches.poll();
        }
        
        // Calculate smoothed pitch
        double smoothedPitch = recentPitches.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        // Determine pitch change for Parsons code
        if (lastStablePitch > 0) {
            double threshold = 50.0; // Hz threshold for considering a pitch change
            ParsonsDirection direction;
            
            if (Math.abs(smoothedPitch - lastStablePitch) < threshold) {
                direction = ParsonsDirection.REPEAT;
            } else if (smoothedPitch > lastStablePitch) {
                direction = ParsonsDirection.UP;
            } else {
                direction = ParsonsDirection.DOWN;
            }
            
            // Only add if it's different from the last direction (to avoid repetition)
            if (parsonsSequence.isEmpty() || 
                parsonsSequence.get(parsonsSequence.size() - 1).direction != direction) {
                parsonsSequence.add(new PitchChange(smoothedPitch, direction, System.currentTimeMillis()));
            }
        }
        
        lastStablePitch = smoothedPitch;
    }
    
    /**
     * Prints the captured Parsons code sequence and a human readable summary
     * of each pitch change.
     */
    private void printParsonsCode() {
        System.out.println("\n=== Captured Parsons Code Sequence ===");
        
        if (parsonsSequence.isEmpty()) {
            System.out.println("No pitch changes detected.");
            return;
        }
        
        StringBuilder parsonsCode = new StringBuilder("*"); // Start symbol
        
        for (PitchChange change : parsonsSequence) {
            switch (change.direction) {
                case UP:
                    parsonsCode.append("U");
                    break;
                case DOWN:
                    parsonsCode.append("D");
                    break;
                case REPEAT:
                    parsonsCode.append("R");
                    break;
            }
        }
        
        System.out.println("Parsons Code: " + parsonsCode.toString());
        System.out.println("\nDetailed Sequence:");
        
        for (int i = 0; i < parsonsSequence.size(); i++) {
            PitchChange change = parsonsSequence.get(i);
            String note = frequencyToNote(change.frequency);
            int octave = frequencyToOctave(change.frequency);
            
            System.out.printf("%d. %s → %s%d (%.1f Hz)\n", 
                i + 1, change.direction, note, octave, change.frequency);
        }
        
        System.out.println("\nThis Parsons code can be used for melody matching and song identification!");
    }
    
    // Data classes
    private static class PitchDetectionResult {
        final double frequency;
        final double magnitude;
        final String noteName;
        final int octave;
        
        PitchDetectionResult(double frequency, double magnitude, String noteName, int octave) {
            this.frequency = frequency;
            this.magnitude = magnitude;
            this.noteName = noteName;
            this.octave = octave;
        }
    }
    
    private static class PitchChange {
        final double frequency;
        final ParsonsDirection direction;
        final long timestamp;
        
        PitchChange(double frequency, ParsonsDirection direction, long timestamp) {
            this.frequency = frequency;
            this.direction = direction;
            this.timestamp = timestamp;
        }
    }
    
    private enum ParsonsDirection {
        UP("↑"), DOWN("↓"), REPEAT("→");
        
        private final String symbol;
        
        ParsonsDirection(String symbol) {
            this.symbol = symbol;
        }
        
        @Override
        public String toString() {
            return symbol;
        }
    }
}
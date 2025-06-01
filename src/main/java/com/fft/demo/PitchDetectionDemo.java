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
 * 3. Perform FFT using optimized implementation
 * 4. Find frequency bin with maximum magnitude (fundamental frequency)
 * 5. Apply harmonic analysis to improve pitch accuracy
 * 6. Convert frequency to musical note
 * 7. Track pitch changes for Parsons code generation
 * </pre>
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
    
    // Musical note data
    private static final String[] NOTE_NAMES = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    private static final double A4_FREQUENCY = 440.0;  // A4 reference frequency
    private static final int A4_NOTE_NUMBER = 69;      // MIDI note number for A4
    
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Queue<Double> recentPitches = new ArrayDeque<>();
    private final List<PitchChange> parsonsSequence = new ArrayList<>();
    private double lastStablePitch = 0.0;
    
    public static void main(String[] args) {
        System.out.println("=== FFT-Based Pitch Detection Demo ===\n");
        
        PitchDetectionDemo demo = new PitchDetectionDemo();
        demo.runDemo();
    }
    
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
            
            // Perform FFT analysis
            FFTResult spectrum = FFTUtils.fft(audioSamples);
            
            // Detect pitch
            PitchDetectionResult pitchResult = detectPitch(spectrum);
            
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
    
    private void applyHammingWindow(double[] samples) {
        int n = samples.length;
        for (int i = 0; i < n; i++) {
            double window = 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / (n - 1));
            samples[i] *= window;
        }
    }
    
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
    
    private int frequencyToBin(double frequency) {
        return (int) Math.round(frequency * FFT_SIZE / SAMPLE_RATE);
    }
    
    private double binToFrequency(double bin) {
        return bin * SAMPLE_RATE / FFT_SIZE;
    }
    
    private String frequencyToNote(double frequency) {
        if (frequency <= 0) return "N/A";
        
        int noteNumber = (int) Math.round(A4_NOTE_NUMBER + 12 * Math.log(frequency / A4_FREQUENCY) / Math.log(2));
        return NOTE_NAMES[noteNumber % 12];
    }
    
    private int frequencyToOctave(double frequency) {
        if (frequency <= 0) return 0;
        
        int noteNumber = (int) Math.round(A4_NOTE_NUMBER + 12 * Math.log(frequency / A4_FREQUENCY) / Math.log(2));
        return (noteNumber / 12) - 1;
    }
    
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
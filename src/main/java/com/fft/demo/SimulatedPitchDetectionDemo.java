package com.fft.demo;

import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;

import java.util.*;

/**
 * Simulated pitch detection demo using generated audio signals.
 * 
 * <p>This demo generates realistic audio signals with known frequencies and melodies,
 * then demonstrates the pitch detection algorithm. It's perfect for testing and
 * showcasing the FFT-based pitch detection capabilities without requiring a microphone.</p>
 * 
 * <h3>Features:</h3>
 * <ul>
 * <li><b>Realistic Signal Generation:</b> Creates audio signals with noise, harmonics, and vibrato</li>
 * <li><b>Known Test Melodies:</b> Includes famous melody snippets for testing</li>
 * <li><b>Parsons Code Generation:</b> Demonstrates automatic melody encoding</li>
 * <li><b>Algorithm Validation:</b> Verifies pitch detection accuracy against known frequencies</li>
 * <li><b>Performance Analysis:</b> Shows FFT performance with different optimized implementations</li>
 * </ul>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
public class SimulatedPitchDetectionDemo {
    
    // Audio simulation parameters
    private static final double SAMPLE_RATE = 44100.0;
    private static final int FFT_SIZE = 4096;
    private static final double NOTE_DURATION = 0.5; // seconds per note
    private static final int SAMPLES_PER_NOTE = (int) (SAMPLE_RATE * NOTE_DURATION);

    // Random number generator for noise
    private static final Random RANDOM = new Random(42);
    
    // Musical note frequencies (in Hz) - Equal temperament, A4 = 440 Hz
    private static final Map<String, Double> NOTE_FREQUENCIES = new HashMap<>();
    static {
        // Initialize note frequencies
        String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        double A4 = 440.0;
        
        for (int octave = 0; octave <= 8; octave++) {
            for (int note = 0; note < 12; note++) {
                String noteName = noteNames[note] + octave;
                // Calculate frequency using equal temperament formula
                int semitonesFromA4 = (octave - 4) * 12 + (note - 9); // A is the 9th note (0-indexed)
                double frequency = A4 * Math.pow(2.0, semitonesFromA4 / 12.0);
                NOTE_FREQUENCIES.put(noteName, frequency);
            }
        }
    }
    
    /**
     * Application entry point for the simulated pitch detection demonstrations.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        System.out.println("=== Simulated FFT-Based Pitch Detection Demo ===\n");
        
        SimulatedPitchDetectionDemo demo = new SimulatedPitchDetectionDemo();
        demo.runAllDemos();
    }
    
    /**
     * Runs all simulation scenarios in sequence.
     */
    public void runAllDemos() {
        demonstrateSingleToneDetection();
        demonstrateChordDetection();
        demonstrateMelodyRecognition();
        demonstrateNoisySignalDetection();
        demonstratePerformanceComparison();
    }
    
    /**
     * Detects single note frequencies from generated tone samples.
     */
    private void demonstrateSingleToneDetection() {
        System.out.println("1. Single Tone Detection:");
        System.out.println("-------------------------");
        
        String[] testNotes = {"A4", "C4", "E4", "G4", "C5"};
        
        for (String note : testNotes) {
            double expectedFreq = NOTE_FREQUENCIES.get(note);
            double[] signal = generateTone(expectedFreq, NOTE_DURATION, 1.0);
            
            // Add some realistic noise
            addNoise(signal, 0.05);
            
            double detectedFreq = detectPitch(signal);
            String detectedNote = frequencyToNote(detectedFreq);
            
            double error = Math.abs(detectedFreq - expectedFreq);
            double errorPercent = (error / expectedFreq) * 100;
            
            System.out.printf("Note: %s | Expected: %.1f Hz | Detected: %.1f Hz (%s) | Error: %.2f%%\n",
                note, expectedFreq, detectedFreq, detectedNote, errorPercent);
        }
        System.out.println();
    }
    
    /**
     * Demonstrates detection of multiple simultaneous notes forming a chord.
     */
    private void demonstrateChordDetection() {
        System.out.println("2. Chord Detection (Multiple Frequencies):");
        System.out.println("------------------------------------------");
        
        // Test major chord: C4, E4, G4
        double[] frequencies = {
            NOTE_FREQUENCIES.get("C4"),
            NOTE_FREQUENCIES.get("E4"),
            NOTE_FREQUENCIES.get("G4")
        };
        double[] amplitudes = {1.0, 0.8, 0.6};
        
        double[] chordSignal = generateChord(frequencies, amplitudes, NOTE_DURATION);
        addNoise(chordSignal, 0.02);
        
        List<Double> detectedFreqs = detectMultiplePitches(chordSignal, 3);
        
        System.out.println("C Major Chord - Expected frequencies:");
        for (int i = 0; i < frequencies.length; i++) {
            System.out.printf("  %.1f Hz\n", frequencies[i]);
        }
        
        System.out.println("Detected frequencies:");
        for (double freq : detectedFreqs) {
            System.out.printf("  %.1f Hz (%s)\n", freq, frequencyToNote(freq));
        }
        System.out.println();
    }
    
    /**
     * Generates a short melody and verifies that the detected Parsons code
     * matches the expected pattern.
     */
    private void demonstrateMelodyRecognition() {
        System.out.println("3. Melody Recognition - \"Twinkle, Twinkle, Little Star\":");
        System.out.println("--------------------------------------------------------");
        
        // First line: "Twinkle, twinkle, little star"
        String[] melody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4"};
        String[] expectedParsons = {"*", "R", "U", "R", "U", "R", "D"};
        
        List<String> detectedNotes = new ArrayList<>();
        List<String> parsonsCode = new ArrayList<>();
        parsonsCode.add("*"); // Start symbol
        
        System.out.println("Analyzing melody note by note:");
        
        double lastFreq = 0.0;
        for (int i = 0; i < melody.length; i++) {
            String note = melody[i];
            double expectedFreq = NOTE_FREQUENCIES.get(note);
            
            // Generate note with some vibrato for realism
            double[] noteSignal = generateToneWithVibrato(expectedFreq, NOTE_DURATION, 1.0, 5.0, 0.02);
            addNoise(noteSignal, 0.03);
            
            double detectedFreq = detectPitch(noteSignal);
            String detectedNote = frequencyToNote(detectedFreq);
            detectedNotes.add(detectedNote);
            
            // Generate Parsons code
            if (i > 0) {
                String parsonsSymbol;
                double freqThreshold = 20.0; // Hz
                
                if (Math.abs(detectedFreq - lastFreq) < freqThreshold) {
                    parsonsSymbol = "R"; // Repeat
                } else if (detectedFreq > lastFreq) {
                    parsonsSymbol = "U"; // Up
                } else {
                    parsonsSymbol = "D"; // Down
                }
                parsonsCode.add(parsonsSymbol);
            }
            
            System.out.printf("Note %d: %s → %s (%.1f Hz)\n", i + 1, note, detectedNote, detectedFreq);
            lastFreq = detectedFreq;
        }
        
        System.out.println("\nParsons Code Analysis:");
        System.out.println("Expected: " + String.join("", expectedParsons));
        System.out.println("Detected: " + String.join("", parsonsCode));
        
        // Calculate accuracy
        int matches = 0;
        for (int i = 0; i < Math.min(expectedParsons.length, parsonsCode.size()); i++) {
            if (expectedParsons[i].equals(parsonsCode.get(i))) {
                matches++;
            }
        }
        double accuracy = (double) matches / expectedParsons.length * 100;
        System.out.printf("Parsons Code Accuracy: %.1f%%\n\n", accuracy);
    }
    
    /**
     * Tests pitch detection accuracy over a range of noise levels.
     */
    private void demonstrateNoisySignalDetection() {
        System.out.println("4. Noisy Signal Detection:");
        System.out.println("--------------------------");
        
        String testNote = "A4";
        double testFreq = NOTE_FREQUENCIES.get(testNote);
        double[] noiselevels = {0.0, 0.1, 0.2, 0.5, 1.0};
        
        System.out.println("Testing robustness to noise:");
        
        for (double noiseLevel : noiselevels) {
            double[] signal = generateTone(testFreq, NOTE_DURATION, 1.0);
            addNoise(signal, noiseLevel);
            
            double detectedFreq = detectPitch(signal);
            String detectedNote = frequencyToNote(detectedFreq);
            
            double error = Math.abs(detectedFreq - testFreq);
            double snr = calculateSNR(signal, noiseLevel);
            
            System.out.printf("Noise level: %.1f | SNR: %.1f dB | Detected: %.1f Hz (%s) | Error: %.1f Hz\n",
                noiseLevel, snr, detectedFreq, detectedNote, error);
        }
        System.out.println();
    }
    
    /**
     * Benchmarks FFT performance for several signal sizes using the utility
     * methods.
     */
    private void demonstratePerformanceComparison() {
        System.out.println("5. FFT Performance Comparison:");
        System.out.println("------------------------------");
        
        int[] testSizes = {512, 1024, 2048, 4096, 8192};
        int iterations = 1000;
        
        System.out.println("Comparing FFT implementations across different sizes:");
        
        for (int size : testSizes) {
            // Generate test signal
            double[] signal = generateTone(440.0, (double) size / SAMPLE_RATE, 1.0);
            addNoise(signal, 0.1);
            
            // Ensure signal is correct size
            if (signal.length != size) {
                signal = Arrays.copyOf(signal, size);
            }
            
            String implementation = FFTUtils.getImplementationInfo(size);
            
            // Benchmark FFT performance
            long startTime = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                FFTUtils.fft(signal);
            }
            long endTime = System.nanoTime();
            
            double avgTime = (endTime - startTime) / (double) iterations / 1_000_000.0; // Convert to milliseconds
            
            System.out.printf("Size: %4d | Implementation: %-20s | Avg time: %.3f ms\n",
                size, implementation.split(" ")[0], avgTime);
        }
        System.out.println();
    }
    
    // Signal generation methods
    
    /**
     * Creates a pure sine-wave tone.
     *
     * @param frequency tone frequency in Hz
     * @param duration length in seconds
     * @param amplitude amplitude of the wave
     * @return generated signal samples
     */
    private double[] generateTone(double frequency, double duration, double amplitude) {
        int samples = (int) (SAMPLE_RATE * duration);
        double[] signal = new double[samples];
        
        for (int i = 0; i < samples; i++) {
            double t = i / SAMPLE_RATE;
            signal[i] = amplitude * Math.sin(2.0 * Math.PI * frequency * t);
        }
        
        return signal;
    }
    
    /**
     * Generates a tone that includes vibrato modulation.
     *
     * @param frequency base frequency in Hz
     * @param duration signal duration in seconds
     * @param amplitude amplitude of the tone
     * @param vibratoRate vibrato frequency in Hz
     * @param vibratoDepth vibrato depth factor
     * @return generated signal samples
     */
    private double[] generateToneWithVibrato(double frequency, double duration, double amplitude,
                                           double vibratoRate, double vibratoDepth) {
        int samples = (int) (SAMPLE_RATE * duration);
        double[] signal = new double[samples];
        
        for (int i = 0; i < samples; i++) {
            double t = i / SAMPLE_RATE;
            double vibrato = vibratoDepth * Math.sin(2.0 * Math.PI * vibratoRate * t);
            double instantFreq = frequency * (1.0 + vibrato);
            signal[i] = amplitude * Math.sin(2.0 * Math.PI * instantFreq * t);
        }
        
        return signal;
    }
    
    /**
     * Generates a multi-tone chord signal.
     *
     * @param frequencies array of frequencies in Hz
     * @param amplitudes amplitude for each frequency
     * @param duration length of the chord in seconds
     * @return generated chord samples
     */
    private double[] generateChord(double[] frequencies, double[] amplitudes, double duration) {
        int samples = (int) (SAMPLE_RATE * duration);
        double[] signal = new double[samples];
        
        for (int i = 0; i < samples; i++) {
            double t = i / SAMPLE_RATE;
            double sample = 0.0;
            
            for (int j = 0; j < frequencies.length; j++) {
                sample += amplitudes[j] * Math.sin(2.0 * Math.PI * frequencies[j] * t);
            }
            
            signal[i] = sample;
        }
        
        return signal;
    }
    
    /**
     * Injects Gaussian noise into a signal for testing robustness.
     *
     * @param signal signal array to modify
     * @param noiseLevel standard deviation of the noise
     */
    private void addNoise(double[] signal, double noiseLevel) {
        for (int i = 0; i < signal.length; i++) {
            signal[i] += noiseLevel * RANDOM.nextGaussian();
        }
    }
    
    // Pitch detection methods
    
    /**
     * Detects the dominant frequency in the provided signal.
     *
     * @param signal input signal samples
     * @return detected frequency in Hz
     */
    private double detectPitch(double[] signal) {
        // Ensure signal is power of 2 for FFT
        double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
        
        FFTResult spectrum = FFTUtils.fft(paddedSignal);
        double[] magnitudes = spectrum.getMagnitudes();
        
        // Find peak frequency
        int peakBin = 0;
        double maxMagnitude = 0.0;
        
        // Only look in musical frequency range (80 Hz - 2000 Hz)
        int minBin = frequencyToBin(80.0, paddedSignal.length);
        int maxBin = Math.min(frequencyToBin(2000.0, paddedSignal.length), magnitudes.length / 2);
        
        for (int i = minBin; i < maxBin; i++) {
            if (magnitudes[i] > maxMagnitude) {
                maxMagnitude = magnitudes[i];
                peakBin = i;
            }
        }
        
        return binToFrequency(peakBin, paddedSignal.length);
    }
    
    /**
     * Finds several prominent frequencies within a signal.
     *
     * @param signal input signal samples
     * @param numPeaks number of peaks to return
     * @return list of detected frequencies
     */
    private List<Double> detectMultiplePitches(double[] signal, int numPeaks) {
        double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
        FFTResult spectrum = FFTUtils.fft(paddedSignal);
        double[] magnitudes = spectrum.getMagnitudes();
        
        List<Double> frequencies = new ArrayList<>();
        
        int minBin = frequencyToBin(80.0, paddedSignal.length);
        int maxBin = Math.min(frequencyToBin(2000.0, paddedSignal.length), magnitudes.length / 2);
        
        // Find multiple peaks
        for (int peak = 0; peak < numPeaks; peak++) {
            int peakBin = 0;
            double maxMagnitude = 0.0;
            
            for (int i = minBin; i < maxBin; i++) {
                if (magnitudes[i] > maxMagnitude) {
                    maxMagnitude = magnitudes[i];
                    peakBin = i;
                }
            }
            
            if (maxMagnitude > 0.01) { // Threshold
                frequencies.add(binToFrequency(peakBin, paddedSignal.length));
                
                // Zero out this peak and neighbors to find next peak
                for (int i = Math.max(0, peakBin - 5); i < Math.min(magnitudes.length, peakBin + 6); i++) {
                    magnitudes[i] = 0.0;
                }
            }
        }
        
        frequencies.sort(Double::compareTo);
        return frequencies;
    }
    
    /**
     * Converts a frequency to the corresponding FFT bin index.
     *
     * @param frequency frequency in Hz
     * @param signalLength length of the signal used for the FFT
     * @return bin index
     */
    private int frequencyToBin(double frequency, int signalLength) {
        return (int) Math.round(frequency * signalLength / SAMPLE_RATE);
    }
    
    /**
     * Converts an FFT bin index back to frequency in Hz.
     *
     * @param bin bin index
     * @param signalLength length of the signal used for the FFT
     * @return frequency in Hz
     */
    private double binToFrequency(int bin, int signalLength) {
        return (double) bin * SAMPLE_RATE / signalLength;
    }
    
    /**
     * Converts a frequency to a note name with octave.
     *
     * @param frequency frequency in Hz
     * @return note name string or {@code "N/A"}
     */
    private String frequencyToNote(double frequency) {
        if (frequency <= 0) return "N/A";
        
        double A4 = 440.0;
        int A4_MIDI = 69;
        
        int midiNote = (int) Math.round(A4_MIDI + 12 * Math.log(frequency / A4) / Math.log(2));
        
        if (midiNote < 0 || midiNote > 127) return "N/A";
        
        String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        String noteName = noteNames[midiNote % 12];
        int octave = (midiNote / 12) - 1;
        
        return noteName + octave;
    }
    
    /**
     * Estimates the signal-to-noise ratio for a given noise level.
     *
     * @param signal reference signal (amplitude assumed to be 1)
     * @param noiseLevel noise multiplier used when adding noise
     * @return SNR value in decibels
     */
    private double calculateSNR(double[] signal, double noiseLevel) {
        // Calculate signal power (assuming signal amplitude is 1.0)
        double signalPower = 1.0;
        double noisePower = noiseLevel * noiseLevel;
        
        return 10 * Math.log10(signalPower / noisePower);
    }
}
package com.fft.demo;

import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;
import com.fft.utils.PitchDetectionUtils;
import com.fft.utils.AudioConstants;
import com.fft.utils.AudioAlgorithmConstants;
import com.fft.utils.FrequencyUtils;
import com.fft.utils.AudioProcessingUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced chord recognition demo using FFT-based harmonic analysis.
 *
 * <p>This demo demonstrates chord recognition capabilities by detecting
 * multiple simultaneous frequencies and identifying chord types (major, minor, etc.).
 * It extends melody recognition to include harmonic analysis for complete music understanding.</p>
 *
 * <h3>Chord Recognition Process:</h3>
 * <ol>
 * <li><b>Multi-Pitch Detection:</b> Extract multiple fundamental frequencies from spectrum</li>
 * <li><b>Chord Identification:</b> Analyze frequency relationships to identify chord types</li>
 * <li><b>Progression Analysis:</b> Track chord sequences over time</li>
 * <li><b>Harmony Matching:</b> Compare against known chord progressions</li>
 * </ol>
 *
 * <h3>Features:</h3>
 * <ul>
 * <li><b>Real-time Chord Detection:</b> Identify chords in audio streams</li>
 * <li><b>Progression Recognition:</b> Recognize common chord sequences</li>
 * <li><b>Harmony Analysis:</b> Understand musical structure beyond melody</li>
 * <li><b>Integration with Melody:</b> Combined melody and harmony recognition</li>
 * </ul>
 *
 * @author Engine AI Assistant
 * @since 2.1.0
 */
public class ChordRecognitionDemo {

    private static final Logger logger = LoggerFactory.getLogger(ChordRecognitionDemo.class);

    private static final double CHORD_DURATION = 1.0; // seconds per chord
    private static final int MAX_SIMULTANEOUS_FREQUENCIES = 4;

    // Enhanced chord progression database
    private final Map<String, ChordProgressionEntry> chordDatabase;

    public ChordRecognitionDemo() {
        this.chordDatabase = createChordProgressionDatabase();
    }

    /**
     * Entry point for the chord recognition demonstration.
     */
    public static void main(String[] args) {
        logger.info("=== FFT-Based Chord Recognition Demo ===%n");

        ChordRecognitionDemo demo = new ChordRecognitionDemo();
        demo.runAllChordDemos();
    }

    /**
     * Executes all chord recognition demonstrations.
     */
    public void runAllChordDemos() {
        demonstrateBasicChordDetection();
        demonstrateChordProgressionRecognition();
        demonstrateHarmonicAnalysis();
        demonstrateCombinedMelodyHarmony();
        demonstratePerformanceAnalysis();
    }

    /**
     * Demonstrates basic chord detection capabilities.
     */
    public void demonstrateBasicChordDetection() {
        logger.info("1. Basic Chord Detection:");
        logger.info("-------------------------");

        // Test individual chords
        String[] testChords = {"C", "Dm", "Em", "F", "G", "Am", "Bdim"};

        for (String chordName : testChords) {
            testChordDetection(chordName);
        }

        logger.info("");
    }

    /**
     * Demonstrates chord progression recognition.
     */
    public void demonstrateChordProgressionRecognition() {
        logger.info("2. Chord Progression Recognition:");
        logger.info("---------------------------------");

        // Test common progressions
        String[][] progressions = {
            {"C", "G", "Am", "F"}, // I-V-vi-IV (Pop progression)
            {"Dm", "Bb", "F", "C"}, // ii-bVI-V-I (Jazz progression)
            {"Em", "C", "G", "D"}, // vi-IV-I-V (Classic rock)
            {"Am", "F", "C", "G"}  // vi-IV-I-V (Alternative)
        };

        String[] progressionNames = {
            "I-V-vi-IV (Pop)",
            "ii-bVI-V-I (Jazz)",
            "vi-IV-I-V (Rock)",
            "vi-IV-I-V (Alternative)"
        };

        for (int i = 0; i < progressions.length; i++) {
            testChordProgression(progressionNames[i], progressions[i]);
        }

        logger.info("");
    }

    /**
     * Demonstrates harmonic analysis of musical pieces.
     */
    private void demonstrateHarmonicAnalysis() {
        logger.info("3. Harmonic Analysis:");
        logger.info("---------------------");

        // Analyze harmonic content of different genres
        analyzeHarmonicContent("Twinkle, Twinkle, Little Star", new String[]{"C", "F", "C", "G"});
        analyzeHarmonicContent("Amazing Grace", new String[]{"G", "Em", "D", "C"});

        logger.info("");
    }

    /**
     * Demonstrates combined melody and harmony recognition.
     */
    private void demonstrateCombinedMelodyHarmony() {
        logger.info("4. Combined Melody & Harmony Recognition:");
        logger.info("-----------------------------------------");

        // Test songs with both melody and chord information
        testCombinedRecognition("Happy Birthday",
            new String[]{"C4", "C4", "D4", "C4", "F4", "E4"},
            new String[]{"C", "C", "C", "C", "F", "C"});

        logger.info("");
    }

    /**
     * Benchmarks chord recognition performance.
     */
    public void demonstratePerformanceAnalysis() {
        logger.info("5. Chord Recognition Performance:");
        logger.info("---------------------------------");

        String[] testChords = {"C", "Dm", "Em", "F", "G", "Am"};
        int iterations = 50;

        long totalTime = 0;

        for (int i = 0; i < iterations; i++) {
            for (String chord : testChords) {
                long startTime = System.nanoTime();
                recognizeChord(chord, false);
                long endTime = System.nanoTime();
                totalTime += (endTime - startTime);
            }
        }

        double averageTime = totalTime / (double) (iterations * testChords.length) / 1_000_000.0;

        logger.info("Chord recognition performance (%d iterations):%n", iterations * testChords.length);
        logger.info("Average recognition time: %.2f ms per chord%n", averageTime);
        logger.info("Recognition rate: %.1f chords/second%n", 1000.0 / averageTime);

        // Test accuracy
        testChordRecognitionAccuracy();

        logger.info("");
    }

    /**
     * Tests recognition of a single chord.
     */
    private void testChordDetection(String chordName) {
        logger.info("Testing: %s%n", chordName);

        PitchDetectionUtils.ChordResult result = recognizeChord(chordName, true);

        if (result.frequencies.length > 0) {
            logger.info("  Detected: %s (%s)%n", result.chordName, result.chordType);
            StringBuilder freqBuilder = new StringBuilder("  Frequencies: ");
            for (int i = 0; i < result.frequencies.length; i++) {
                freqBuilder.append(String.format("%.1f Hz", result.frequencies[i]));
                if (i < result.frequencies.length - 1) freqBuilder.append(", ");
            }
            logger.info(freqBuilder.toString());
            logger.info("  Confidence: {}%", String.format("%.1f", result.confidence * 100));
        } else {
            logger.info("  No chord detected");
        }
        logger.info("");
    }

    /**
     * Tests recognition of a chord progression.
     */
    private void testChordProgression(String progressionName, String[] chords) {
        logger.info("Testing: %s%n", progressionName);

        List<PitchDetectionUtils.ChordResult> results = new ArrayList<>();
        for (String chord : chords) {
            results.add(recognizeChord(chord, false));
        }

        // Analyze progression
        String detectedProgression = results.stream()
            .map(r -> r.chordName)
            .collect(Collectors.joining(" → "));

        double avgConfidence = results.stream()
            .mapToDouble(r -> r.confidence)
            .average()
            .orElse(0.0);

        logger.info("  Detected progression: %s%n", detectedProgression);
        logger.info("  Average confidence: %.1f%%%n", avgConfidence * 100);

        // Find matching known progressions
        List<ProgressionMatch> matches = findProgressionMatches(results);
        if (!matches.isEmpty()) {
            logger.info("  Best matches:");
            for (int i = 0; i < Math.min(2, matches.size()); i++) {
                ProgressionMatch match = matches.get(i);
                logger.info("    %s (%.1f%% match)%n", match.progressionName, match.similarity * 100);
            }
        }

        logger.info("");
    }

    /**
     * Analyzes the harmonic content of a piece.
     */
    private void analyzeHarmonicContent(String pieceName, String[] chords) {
        logger.info("Analyzing: %s%n", pieceName);

        List<PitchDetectionUtils.ChordResult> results = Arrays.stream(chords)
            .map(chord -> recognizeChord(chord, false))
            .collect(Collectors.toList());

        // Calculate harmonic complexity
        long uniqueChords = results.stream()
            .map(r -> r.chordType)
            .distinct()
            .count();

        double avgConfidence = results.stream()
            .mapToDouble(r -> r.confidence)
            .average()
            .orElse(0.0);

        // Determine key
        String likelyKey = determineKey(results);

        logger.info("  Key: %s%n", likelyKey);
        logger.info("  Unique chord types: %d%n", uniqueChords);
        logger.info("  Average detection confidence: %.1f%%%n", avgConfidence * 100);

        // Chord type distribution
        Map<String, Long> typeDistribution = results.stream()
            .collect(Collectors.groupingBy(r -> r.chordType, Collectors.counting()));

        logger.info("  Chord distribution: %s%n", typeDistribution);
    }

    /**
     * Tests combined melody and harmony recognition.
     */
    private void testCombinedRecognition(String songName, String[] melody, String[] chords) {
        logger.info("Testing: %s (Melody + Harmony)%n", songName);

        // Generate combined signal (simplified - alternating melody and chords)
        double[] combinedSignal = generateCombinedSignal(melody, chords);

        // Extract both melody and harmony
        List<PitchDetectionUtils.PitchResult> melodyNotes = extractMelodyFromCombined(combinedSignal);
        List<PitchDetectionUtils.ChordResult> chordSequence = extractChordsFromCombined(combinedSignal);

        logger.info("  Detected %d melody notes and %d chords%n", melodyNotes.size(), chordSequence.size());

        if (!chordSequence.isEmpty()) {
            double avgChordConfidence = chordSequence.stream()
                .mapToDouble(c -> c.confidence)
                .average()
                .orElse(0.0);
            logger.info("  Average chord confidence: %.1f%%%n", avgChordConfidence * 100);
        }
    }

    /**
     * Tests chord recognition accuracy across different conditions.
     */
    private void testChordRecognitionAccuracy() {
        logger.info("Chord Recognition Accuracy Test:");
        logger.info("--------------------------------");

        String[] testChords = {"C", "Dm", "Em", "F", "G", "Am", "Bdim"};
        int correct = 0;
        int total = 0;

        for (String expectedChord : testChords) {
            PitchDetectionUtils.ChordResult result = recognizeChord(expectedChord, false);
            total++;

            // Simple accuracy check (chord root matches)
            if (result.chordName.equals(expectedChord.replaceAll("[^A-G#]", ""))) {
                correct++;
            }
        }

        double accuracy = (double) correct / total * 100;
        logger.info("Accuracy: %.1f%% (%d/%d correct)%n", accuracy, correct, total);
    }

    /**
     * Recognizes a chord from its name (generates synthetic signal).
     */
    public PitchDetectionUtils.ChordResult recognizeChord(String chordName, boolean verbose) {
        // Generate synthetic chord signal
        double[] chordSignal = generateChordSignal(chordName);

        // Analyze with FFT
        double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(chordSignal);
        FFTResult spectrum = FFTUtils.fft(paddedSignal);

        // Detect chord
        PitchDetectionUtils.ChordResult result = PitchDetectionUtils.detectChord(spectrum, AudioConstants.SAMPLE_RATE, MAX_SIMULTANEOUS_FREQUENCIES);

        if (verbose) {
            logger.info("  Generated signal for: %s%n", chordName);
        }

        return result;
    }

    /**
     * Finds matching chord progressions in the database.
     */
    private List<ProgressionMatch> findProgressionMatches(List<PitchDetectionUtils.ChordResult> detectedChords) {
        List<ProgressionMatch> matches = new ArrayList<>();

        for (Map.Entry<String, ChordProgressionEntry> entry : chordDatabase.entrySet()) {
            String progressionName = entry.getKey();
            ChordProgressionEntry progression = entry.getValue();

            double similarity = calculateProgressionSimilarity(detectedChords, progression);
            if (similarity > 0.5) { // Minimum similarity threshold
                matches.add(new ProgressionMatch(progressionName, similarity));
            }
        }

        matches.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        return matches;
    }

    /**
     * Calculates similarity between detected chords and known progression.
     */
    private double calculateProgressionSimilarity(List<PitchDetectionUtils.ChordResult> detected,
                                                ChordProgressionEntry known) {
        if (detected.size() != known.chords.size()) {
            return 0.0; // Different lengths
        }

        double totalSimilarity = 0.0;
        for (int i = 0; i < detected.size(); i++) {
            PitchDetectionUtils.ChordResult detectedChord = detected.get(i);
            String knownChord = known.chords.get(i);

            // Simple similarity based on chord type matching
            if (detectedChord.chordType.equals(chordNameToType(knownChord))) {
                totalSimilarity += 1.0;
            } else if (detectedChord.chordName.equals(knownChord.replaceAll("[^A-G#]", ""))) {
                totalSimilarity += 0.8; // Root matches but type different
            }
        }

        return totalSimilarity / detected.size();
    }

    /**
     * Determines the likely key of a chord sequence.
     */
    private String determineKey(List<PitchDetectionUtils.ChordResult> chords) {
        // Simple key detection based on most common root
        Map<String, Integer> rootCounts = new HashMap<>();
        for (PitchDetectionUtils.ChordResult chord : chords) {
            String root = chord.chordName.replaceAll("[^A-G#]", "");
            rootCounts.put(root, rootCounts.getOrDefault(root, 0) + 1);
        }

        return rootCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Unknown");
    }

    /**
     * Generates a synthetic signal for a chord.
     */
    private double[] generateChordSignal(String chordName) {
        double[] frequencies = chordNameToFrequencies(chordName);
        if (frequencies.length == 0) {
            return new double[(int) (AudioConstants.SAMPLE_RATE * CHORD_DURATION)]; // Return silence for unknown chords
        }
        return generateMultiTone(frequencies, CHORD_DURATION, 1.0);
    }

    /**
     * Converts chord name to frequencies.
     */
    private double[] chordNameToFrequencies(String chordName) {
        // Parse chord name (simplified)
        String root = chordName.replaceAll("[^A-G#]", "");
        String type = chordName.replaceAll("[A-G#]", "").toLowerCase();

        if (root.isEmpty()) {
            return new double[0]; // Invalid chord name
        }

        double rootFreq = noteToFrequency(root + "4");

        // Define intervals for different chord types
        Map<String, int[]> intervals = new HashMap<>();
        intervals.put("", new int[]{0, 4, 7});        // Major
        intervals.put("m", new int[]{0, 3, 7});       // Minor
        intervals.put("7", new int[]{0, 4, 7, 10});   // Dominant 7th
        intervals.put("maj7", new int[]{0, 4, 7, 11}); // Major 7th
        intervals.put("m7", new int[]{0, 3, 7, 10});  // Minor 7th
        intervals.put("dim", new int[]{0, 3, 6});      // Diminished
        intervals.put("aug", new int[]{0, 4, 8});      // Augmented

        int[] chordIntervals = intervals.getOrDefault(type, new int[]{0, 4, 7});

        return Arrays.stream(chordIntervals)
            .mapToDouble(interval -> rootFreq * Math.pow(2, interval / 12.0))
            .toArray();
    }

    /**
     * Converts chord name to type string.
     */
    private String chordNameToType(String chordName) {
        if (chordName.contains("m")) return "minor";
        if (chordName.contains("7")) return "major7";
        if (chordName.contains("dim")) return "diminished";
        if (chordName.contains("aug")) return "augmented";
        return "major";
    }

    /**
     * Generates a signal with multiple simultaneous tones.
     */
    private double[] generateMultiTone(double[] frequencies, double duration, double amplitude) {
        int samples = (int) (AudioConstants.SAMPLE_RATE * duration);
        double[] signal = new double[samples];

        for (int i = 0; i < samples; i++) {
            double t = i / AudioConstants.SAMPLE_RATE;
            double sample = 0.0;
            for (double freq : frequencies) {
                sample += Math.sin(2.0 * Math.PI * freq * t);
            }
            signal[i] = amplitude * sample / frequencies.length; // Normalize
        }

        return signal;
    }

    /**
     * Generates a combined melody and chord signal.
     */
    private double[] generateCombinedSignal(String[] melody, String[] chords) {
        List<Double> signal = new ArrayList<>();

        // Alternate between melody notes and chords
        for (int i = 0; i < Math.max(melody.length, chords.length); i++) {
            if (i < melody.length) {
                double[] noteSignal = generateTone(noteToFrequency(melody[i]), 0.5, 1.0);
                for (double s : noteSignal) signal.add(s);
            }
            if (i < chords.length) {
                double[] chordSignal = generateChordSignal(chords[i]);
                for (double s : chordSignal) signal.add(s);
            }
        }

        return signal.stream().mapToDouble(Double::doubleValue).toArray();
    }

    /**
     * Extracts melody from combined signal (simplified).
     */
    private List<PitchDetectionUtils.PitchResult> extractMelodyFromCombined(double[] signal) {
        List<PitchDetectionUtils.PitchResult> results = new ArrayList<>();
        int windowSize = (int) (AudioConstants.SAMPLE_RATE * 0.1);

        for (int start = 0; start < signal.length - windowSize; start += windowSize / 2) {
            double[] window = Arrays.copyOfRange(signal, start, start + windowSize);
            PitchDetectionUtils.PitchResult result = PitchDetectionUtils.detectPitchHybrid(window, AudioConstants.SAMPLE_RATE);
            if (result.isVoiced && result.confidence > 0.3) {
                results.add(result);
            }
        }

        return results;
    }

    /**
     * Extracts chords from combined signal (simplified).
     */
    private List<PitchDetectionUtils.ChordResult> extractChordsFromCombined(double[] signal) {
        List<PitchDetectionUtils.ChordResult> results = new ArrayList<>();
        int windowSize = AudioConstants.FFT_SIZE;

        for (int start = 0; start < signal.length - windowSize; start += windowSize / 2) {
            double[] window = Arrays.copyOfRange(signal, start, start + windowSize);
            double[] padded = FFTUtils.zeroPadToPowerOfTwo(window);
            FFTResult spectrum = FFTUtils.fft(padded);
            PitchDetectionUtils.ChordResult result = PitchDetectionUtils.detectChord(spectrum, AudioConstants.SAMPLE_RATE, MAX_SIMULTANEOUS_FREQUENCIES);
            if (result.confidence > 0.3) {
                results.add(result);
            }
        }

        return results;
    }

    /**
     * Generates a basic sine wave tone.
     */
    private double[] generateTone(double frequency, double duration, double amplitude) {
        int samples = (int) (AudioConstants.SAMPLE_RATE * duration);
        double[] signal = new double[samples];

        for (int i = 0; i < samples; i++) {
            double t = i / AudioConstants.SAMPLE_RATE;
            signal[i] = amplitude * Math.sin(2.0 * Math.PI * frequency * t);
        }

        return signal;
    }

    /**
     * Maps a note name to its frequency in Hz.
     */
    private double noteToFrequency(String note) {
        Map<String, Double> noteFreqs = new HashMap<>();
        noteFreqs.put("C4", 261.63);
        noteFreqs.put("C#4", 277.18);
        noteFreqs.put("D4", 293.66);
        noteFreqs.put("D#4", 311.13);
        noteFreqs.put("E4", 329.63);
        noteFreqs.put("F4", 349.23);
        noteFreqs.put("F#4", 369.99);
        noteFreqs.put("G4", 392.00);
        noteFreqs.put("G#4", 415.30);
        noteFreqs.put("A4", 440.00);
        noteFreqs.put("A#4", 466.16);
        noteFreqs.put("B4", 493.88);
        noteFreqs.put("C5", 523.25);

        return noteFreqs.getOrDefault(note, 440.0);
    }

    /**
     * Creates a database of common chord progressions.
     */
    private Map<String, ChordProgressionEntry> createChordProgressionDatabase() {
        Map<String, ChordProgressionEntry> database = new HashMap<>();

        database.put("I-V-vi-IV (Pop)",
            new ChordProgressionEntry(Arrays.asList("C", "G", "Am", "F"), "Pop", "Contemporary"));

        database.put("ii-V-I (Jazz)",
            new ChordProgressionEntry(Arrays.asList("Dm", "G", "C"), "Jazz", "Traditional"));

        database.put("vi-IV-I-V (Rock)",
            new ChordProgressionEntry(Arrays.asList("Am", "F", "C", "G"), "Rock", "Classic"));

        database.put("I-IV-V-I (Blues)",
            new ChordProgressionEntry(Arrays.asList("C", "F", "G", "C"), "Blues", "Traditional"));

        return database;
    }

    // Data classes
    private static class ChordProgressionEntry {
        final List<String> chords;
        final String genre;
        final String style;

        ChordProgressionEntry(List<String> chords, String genre, String style) {
            this.chords = chords;
            this.genre = genre;
            this.style = style;
        }
    }

    private static class ProgressionMatch {
        final String progressionName;
        final double similarity;

        ProgressionMatch(String progressionName, double similarity) {
            this.progressionName = progressionName;
            this.similarity = similarity;
        }
    }
}
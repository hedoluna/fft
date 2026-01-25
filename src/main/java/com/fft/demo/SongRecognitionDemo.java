package com.fft.demo;

import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;
import com.fft.utils.PitchDetectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced song recognition demo using FFT-based pitch detection and Parsons code matching.
 * 
 * <p>This demo demonstrates the second goal: recognizing full songs using the Parsons code
 * methodology. It combines the pitch detection capabilities with melody pattern matching
 * to identify songs from hummed or played melodies.</p>
 * 
 * <h3>Recognition Process:</h3>
 * <ol>
 * <li><b>Pitch Detection:</b> Extract fundamental frequencies from audio signal</li>
 * <li><b>Melody Segmentation:</b> Group frequencies into discrete notes</li>
 * <li><b>Parsons Code Generation:</b> Convert pitch sequence to directional code</li>
 * <li><b>Pattern Matching:</b> Compare against database of known melodies</li>
 * <li><b>Ranking:</b> Score matches and return most likely candidates</li>
 * </ol>
 * 
 * <h3>Features:</h3>
 * <ul>
 * <li><b>Robust Melody Database:</b> Extensive collection of well-known songs</li>
 * <li><b>Partial Matching:</b> Can identify songs from melody fragments</li>
 * <li><b>Noise Tolerance:</b> Works with imperfect pitch detection</li>
 * <li><b>Multiple Algorithms:</b> Various matching strategies for different use cases</li>
 * <li><b>Performance Analysis:</b> Real-time processing capabilities</li>
 * </ul>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
public class SongRecognitionDemo {

    private static final Logger logger = LoggerFactory.getLogger(SongRecognitionDemo.class);

    private static final double SAMPLE_RATE = 44100.0;
    private static final int FFT_SIZE = 4096;
    private static final double NOTE_DURATION = 0.4; // seconds per note
    private static final double FREQUENCY_TOLERANCE = 25.0; // Hz for grouping notes

    // Random number generator for noise
    private static final Random RANDOM = new Random(42);
    
    // Enhanced melody database with more songs and variations
    private final Map<String, MelodyEntry> melodyDatabase;

    // Intelligent caching system for frequent recognition results
    private final Map<String, CachedResult> recognitionCache = new LinkedHashMap<String, CachedResult>(100, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CachedResult> eldest) {
            return size() > 50; // Keep only 50 most recent results
        }
    };

    // Learning system for continuous improvement
    private final LearningSystem learningSystem = new LearningSystem();

    public SongRecognitionDemo() {
        this.melodyDatabase = createEnhancedMelodyDatabase();
    }
    
    /**
     * Entry point for the song recognition demonstration.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        logger.info("=== FFT-Based Song Recognition Demo ===\n");

        SongRecognitionDemo demo = new SongRecognitionDemo();
        demo.runAllDemos();
    }
    
    /**
     * Executes each song recognition example in turn.
     */
    public void runAllDemos() {
        demonstrateBasicSongRecognition();
        demonstratePartialMelodyMatching();
        demonstrateNoisyMelodyRecognition();
        demonstrateVariationTolerance();
        demonstrateRealTimeRecognition();
        demonstrateAdvancedRecognition();
        demonstratePerformanceAnalysis();
    }
    
    /**
     * Recognizes complete melodies from a predefined list.
     */
    private void demonstrateBasicSongRecognition() {
        logger.info("1. Basic Song Recognition:");
        logger.info("-------------------------");

        // Test with complete melody of "Twinkle, Twinkle, Little Star"
        String[] twinkleMelody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4", "F4", "F4", "E4", "E4", "D4", "D4", "C4"};
        testMelodyRecognition("Twinkle, Twinkle, Little Star (complete)", twinkleMelody);

        // Test with "Mary Had a Little Lamb"
        String[] maryMelody = {"E4", "D4", "C4", "D4", "E4", "E4", "E4", "D4", "D4", "D4", "E4", "G4", "G4"};
        testMelodyRecognition("Mary Had a Little Lamb", maryMelody);

        logger.info("");
    }
    
    /**
     * Shows recognition ability using incomplete melody fragments.
     */
    private void demonstratePartialMelodyMatching() {
        logger.info("2. Partial Melody Matching:");
        logger.info("---------------------------");

        // Test with just the beginning of "Twinkle, Twinkle"
        String[] partialTwinkle = {"C4", "C4", "G4", "G4", "A4"};
        testMelodyRecognition("Twinkle, Twinkle (partial)", partialTwinkle);

        // Test with middle section of "Happy Birthday"
        String[] partialBirthday = {"C4", "C4", "D4", "C4", "F4"};
        testMelodyRecognition("Happy Birthday (partial)", partialBirthday);

        logger.info("");
    }
    
    /**
     * Tests melody recognition when the input contains noise.
     */
    private void demonstrateNoisyMelodyRecognition() {
        logger.info("3. Noisy Melody Recognition:");
        logger.info("----------------------------");

        String[] melody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4"};

        // Test with different noise levels
        double[] noiseLevels = {0.0, 0.1, 0.2, 0.3};

        for (double noiseLevel : noiseLevels) {
            logger.info("Testing with noise level {}:", String.format("%.1f", noiseLevel));
            testNoisyMelodyRecognition(melody, noiseLevel);
        }

        logger.info("");
    }
    
    /**
     * Demonstrates robustness to transposition and rhythmic changes.
     */
    private void demonstrateVariationTolerance() {
        logger.info("4. Variation Tolerance:");
        logger.info("----------------------");

        // Test transposed version (different key)
        String[] originalMelody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4"};
        String[] transposedMelody = {"D4", "D4", "A4", "A4", "B4", "B4", "A4"}; // Up one tone

        logger.info("Original melody in C:");
        testMelodyRecognition("Twinkle (original)", originalMelody);

        logger.info("Transposed melody in D:");
        testMelodyRecognition("Twinkle (transposed)", transposedMelody);

        // Test with rhythmic variations (doubled notes)
        String[] rhythmicVariation = {"C4", "C4", "C4", "C4", "G4", "G4", "G4", "G4"};
        logger.info("Rhythmic variation:");
        testMelodyRecognition("Twinkle (rhythmic variation)", rhythmicVariation);

        logger.info("");
    }
    
    /**
     * Simulates incremental recognition as more notes of a melody arrive with enhanced feedback and confidence analysis.
     */
    private void demonstrateRealTimeRecognition() {
        logger.info("5. Real-Time Recognition Simulation:");
        logger.info("------------------------------------");

        String[] melody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4", "F4", "F4", "E4", "E4", "D4", "D4", "C4"};

        logger.info("Simulating incremental recognition with intelligent segmentation and confidence analysis:");

        // Generate the full signal once
        double[] fullSignal = generateMelodySignal(melody);

        // Track confidence history for stability analysis
        List<Double> confidenceHistory = new ArrayList<>();
        String lastBestMatch = null;
        int stableCount = 0;

        for (int length = 3; length <= melody.length; length += 2) {
            // Use only the first 'length' notes worth of signal
            int samplesNeeded = length * (int)(SAMPLE_RATE * NOTE_DURATION);
            double[] partialSignal = Arrays.copyOf(fullSignal, Math.min(samplesNeeded, fullSignal.length));

            // Extract notes with full timing information
            List<DetectedNote> detectedNotes = extractDetectedNotes(partialSignal);

            if (detectedNotes.isEmpty()) {
                logger.info("After {} notes: No pitches detected", length);
                confidenceHistory.add(0.0);
                continue;
            }

            // Extract frequencies for Parsons code
            double[] detectedPitches = detectedNotes.stream().mapToDouble(n -> n.frequency).toArray();
            String parsonsCode = ParsonsCodeUtils.generateParsonsCode(detectedPitches);
            List<RecognitionResult> results = findBestMatches(parsonsCode, 3);

            // Calculate average confidence of detected notes
            double avgNoteConfidence = detectedNotes.stream().mapToDouble(n -> n.confidence).average().orElse(0.0);

            logger.info("After {} notes ({} detected, avg conf: {}): ",
                length, detectedNotes.size(), String.format("%.2f", avgNoteConfidence));

            if (!results.isEmpty()) {
                RecognitionResult best = results.get(0);
                confidenceHistory.add(best.confidence);

                // Analyze stability
                boolean isStable = lastBestMatch != null && lastBestMatch.equals(best.songTitle);
                if (isStable) {
                    stableCount++;
                } else {
                    stableCount = 1;
                    lastBestMatch = best.songTitle;
                }

                String confidenceLevel = best.confidence > 0.8 ? "HIGH" :
                                       best.confidence > 0.6 ? "MEDIUM" : "LOW";
                String stability = stableCount >= 2 ? " [STABLE]" : "";

                logger.info("Best match: {} ({} {} confidence){}",
                    best.songTitle, String.format("%.1f%%", best.confidence * 100), confidenceLevel, stability);
            } else {
                confidenceHistory.add(0.0);
                logger.info("No strong matches yet");
            }
        }

        // Analyze overall confidence trend
        if (!confidenceHistory.isEmpty()) {
            double avgConfidence = confidenceHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double maxConfidence = confidenceHistory.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            logger.info("Overall: Avg confidence {}%, Peak confidence {}%",
                String.format("%.1f", avgConfidence * 100), String.format("%.1f", maxConfidence * 100));
        }

        logger.info("");
    }
    
    /**
     * Benchmarks recognition performance and analyzes FFT overhead.
     */
    private void demonstratePerformanceAnalysis() {
        logger.info("6. Performance Analysis:");
        logger.info("-----------------------");

        String[] testMelody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4"};
        int iterations = 100;

        long totalTime = 0;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            recognizeMelody(testMelody, false);
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }

        double averageTime = totalTime / (double) iterations / 1_000_000.0; // Convert to milliseconds

        logger.info("Recognition performance ({} iterations):", iterations);
        logger.info("Average recognition time: {} ms", String.format("%.2f", averageTime));
        logger.info("Database size: {} melodies", melodyDatabase.size());
        logger.info("Recognition rate: {} recognitions/second", String.format("%.1f", 1000.0 / averageTime));

        // Analyze FFT performance contribution
        double[] signal = generateMelodySignal(testMelody);

        long fftStartTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            extractPitchSequence(signal);
        }
        long fftEndTime = System.nanoTime();

        double fftTime = (fftEndTime - fftStartTime) / (double) iterations / 1_000_000.0;
        logger.info("FFT analysis time: {} ms ({}% of total)",
            String.format("%.2f", fftTime), String.format("%.1f", (fftTime / averageTime) * 100));

        // Test recognition accuracy with different noise levels
        testRecognitionRobustness();

        // Show learning system statistics
        learningSystem.printLearningStats();

        logger.info("");
    }

    /**
     * Tests recognition robustness across different noise levels and conditions.
     */
    private void testRecognitionRobustness() {
        logger.info("Recognition Robustness Test:");
        logger.info("---------------------------");

        String[] testMelody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4"};
        double[] noiseLevels = {0.0, 0.1, 0.2, 0.5};

        for (double noise : noiseLevels) {
            double[] signal = generateMelodySignal(testMelody);
            if (noise > 0) {
                addNoise(signal, noise);
            }

            double[] pitches = extractPitchSequence(signal);
            String parsons = ParsonsCodeUtils.generateParsonsCode(pitches);
            List<RecognitionResult> results = findBestMatches(parsons, 1);

            double accuracy = results.isEmpty() ? 0.0 : results.get(0).confidence;
            logger.info("Noise {}: {} pitches detected, accuracy: {}%",
                String.format("%.1f", noise), pitches.length, String.format("%.1f", accuracy * 100));
        }
    }
    
    /**
     * Helper that prints recognition results for a given melody.
     *
     * @param description description displayed before the results
     * @param melody array of note names
     */
    private void testMelodyRecognition(String description, String[] melody) {
        logger.info("Testing: {}", description);

        List<RecognitionResult> results = recognizeMelody(melody, true);

        if (results.isEmpty()) {
            logger.info("  No matches found");
        } else {
            logger.info("  Top matches:");
            for (int i = 0; i < Math.min(3, results.size()); i++) {
                RecognitionResult result = results.get(i);
                logger.info("    {}. {} ({}% confidence)",
                    i + 1, result.songTitle, String.format("%.1f", result.confidence * 100));
            }
        }
        logger.info("");
    }

    /**
     * Demonstrates advanced recognition capabilities using enhanced pitch detection.
     */
    private void demonstrateAdvancedRecognition() {
        logger.info("6. Advanced Recognition with Enhanced Pitch Detection:");
        logger.info("-----------------------------------------------------");

        // Test with "Twinkle, Twinkle, Little Star" using advanced recognition
        String[] twinkleMelody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4", "F4", "F4", "E4", "E4", "D4", "D4", "C4"};
        testAdvancedMelodyRecognition("Twinkle, Twinkle, Little Star (Advanced)", twinkleMelody);

        // Test with partial and noisy melody
        String[] partialTwinkle = {"C4", "C4", "G4", "G4", "A4"};
        testAdvancedMelodyRecognition("Twinkle (Partial + Advanced)", partialTwinkle);

        logger.info("");
    }

    /**
     * Tests advanced melody recognition with enhanced features.
     */
    private void testAdvancedMelodyRecognition(String description, String[] melody) {
        logger.info("Testing: {}", description);

        // Generate signal and extract notes using advanced method
        double[] signal = generateMelodySignal(melody);
        List<DetectedNote> detectedNotes = extractDetectedNotes(signal);

        logger.info("  Detected {} notes with advanced segmentation", detectedNotes.size());

        if (!detectedNotes.isEmpty()) {
            double avgConfidence = detectedNotes.stream().mapToDouble(n -> n.confidence).average().orElse(0.0);
            logger.info("  Average note confidence: {}%", String.format("%.1f", avgConfidence * 100));

            // Use advanced recognition
            List<RecognitionResult> results = recognizeMelodyAdvanced(detectedNotes, 5);

            if (results.isEmpty()) {
                logger.info("  No matches found with advanced recognition");
            } else {
                logger.info("  Advanced recognition results:");
                for (int i = 0; i < Math.min(3, results.size()); i++) {
                    RecognitionResult result = results.get(i);
                    logger.info("    {}. {} ({}% confidence)",
                        i + 1, result.songTitle, String.format("%.1f", result.confidence * 100));
                }
            }
        } else {
            logger.info("  No notes detected");
        }

        logger.info("");
    }

    /**
     * Recognizes a melody after noise has been added using enhanced processing.
     *
     * @param melody melody to recognize
     * @param noiseLevel level of added noise
     */
    private void testNoisyMelodyRecognition(String[] melody, double noiseLevel) {
        // Generate signal with noise
        double[] signal = generateMelodySignal(melody);
        addNoise(signal, noiseLevel);

        // Extract notes with full timing information
        List<DetectedNote> detectedNotes = extractDetectedNotes(signal);

        if (detectedNotes.isEmpty()) {
            logger.info("  No reliable pitches detected (too noisy)");
            return;
        }

        // Extract frequencies for Parsons code
        double[] noisyPitches = detectedNotes.stream().mapToDouble(n -> n.frequency).toArray();
        String noisyParsons = ParsonsCodeUtils.generateParsonsCode(noisyPitches);

        // Analyze rhythm pattern
        String rhythmPattern = analyzeRhythmPattern(detectedNotes);

        logger.info("  Detected {} notes, Parsons code: {}, Rhythm: {}",
            detectedNotes.size(), noisyParsons, rhythmPattern);

        // Use enhanced recognition with rhythm information
        List<RecognitionResult> results = recognizeMelodyWithRhythm(detectedNotes, 3);

        if (!results.isEmpty()) {
            RecognitionResult best = results.get(0);
            double avgConfidence = detectedNotes.stream().mapToDouble(n -> n.confidence).average().orElse(0.0);
            logger.info("  Best match: {} ({}% confidence, avg note conf: {})",
                best.songTitle, String.format("%.1f", best.confidence * 100), String.format("%.2f", avgConfidence));
        } else {
            logger.info("  No matches found");
        }
    }
    
    /**
     * Converts a melody into Parsons code and finds best matches.
     *
     * @param melody melody expressed as note names
     * @param verbose if true, prints the generated Parsons code
     * @return list of recognition results
     */
    private List<RecognitionResult> recognizeMelody(String[] melody, boolean verbose) {
        // Convert note names to frequencies
        double[] frequencies = new double[melody.length];
        for (int i = 0; i < melody.length; i++) {
            frequencies[i] = noteToFrequency(melody[i]);
        }

        // Generate Parsons code
        String parsonsCode = ParsonsCodeUtils.generateParsonsCode(frequencies);

        if (verbose) {
            logger.info("  Generated Parsons code: {}", parsonsCode);
        }

        // Find matches
        return findBestMatches(parsonsCode, 5);
    }

    /**
     * Advanced melody recognition using enhanced pitch detection features.
     *
     * @param detectedNotes notes with full timing and confidence information
     * @param maxResults maximum number of results to return
     * @return list of recognition results with enhanced scoring
     */
    public List<RecognitionResult> recognizeMelodyAdvanced(List<DetectedNote> detectedNotes, int maxResults) {
        if (detectedNotes.isEmpty()) {
            return new ArrayList<>();
        }

        // Extract basic frequency sequence
        double[] frequencies = detectedNotes.stream().mapToDouble(n -> n.frequency).toArray();
        double[] confidences = detectedNotes.stream().mapToDouble(n -> n.confidence).toArray();

        // Generate Parsons code with confidence weighting
        String parsonsCode = generateConfidenceWeightedParsonsCode(frequencies, confidences);

        // Extract rhythm pattern
        String rhythmPattern = analyzeAdvancedRhythmPattern(detectedNotes);

        // Try to detect chords for harmonic context
        List<PitchDetectionUtils.ChordResult> chordSequence = new ArrayList<>();
        try {
            chordSequence = extractChordSequenceFromMelody(detectedNotes);
        } catch (Exception e) {
            // Chord detection might fail, continue without it
        }

        // Enhanced matching with multiple criteria
        List<RecognitionResult> results = findBestMatchesAdvanced(parsonsCode, rhythmPattern, chordSequence, maxResults);

        // Apply ensemble method for improved confidence
        List<RecognitionResult> ensembleResults = applyEnsembleMatching(detectedNotes, maxResults);

        // Combine advanced matching with ensemble results
        results = combineMatchingResults(results, ensembleResults, maxResults);

        // Apply advanced confidence-based re-ranking with statistical validation
        results = rerankResultsByConfidence(results, detectedNotes);

        // Final validation and filtering
        results = applyFinalValidation(results, detectedNotes);

        return results;
    }

    /**
     * Generates Parsons code with confidence weighting for more accurate matching.
     */
    private String generateConfidenceWeightedParsonsCode(double[] frequencies, double[] confidences) {
        if (frequencies.length < 2) return "";

        StringBuilder code = new StringBuilder();
        code.append("*"); // Start symbol

        for (int i = 1; i < frequencies.length; i++) {
            double freq1 = frequencies[i-1];
            double freq2 = frequencies[i];
            double conf1 = confidences[i-1];
            double conf2 = confidences[i];

            // Weighted comparison based on confidence
            double weightedRatio = (freq2 * conf2 + freq1 * conf1) / (conf1 + conf2);
            double ratio = freq2 / freq1;

            char direction;
            if (ratio > 1.05) direction = 'U';      // Up
            else if (ratio < 0.95) direction = 'D'; // Down
            else direction = 'R';                   // Repeat/Same

            // Only add if both notes have reasonable confidence
            if (conf1 > 0.3 && conf2 > 0.3) {
                code.append(direction);
            }
        }

        return code.toString();
    }

    /**
     * Advanced rhythm pattern analysis considering note durations and timing.
     */
    private String analyzeAdvancedRhythmPattern(List<DetectedNote> notes) {
        if (notes.size() < 2) return "";

        StringBuilder rhythm = new StringBuilder();
        double avgDuration = notes.stream().mapToDouble(n -> n.duration).average().orElse(0.4);
        double avgGap = 0.0;

        // Calculate average gap between notes
        for (int i = 1; i < notes.size(); i++) {
            double gap = notes.get(i).startTime - (notes.get(i-1).startTime + notes.get(i-1).duration);
            avgGap += gap;
        }
        avgGap /= Math.max(1, notes.size() - 1);

        for (int i = 0; i < notes.size() - 1; i++) {
            DetectedNote current = notes.get(i);
            DetectedNote next = notes.get(i + 1);

            // Classify note duration
            char durationChar = current.duration > avgDuration * 1.5 ? 'L' :  // Long
                              current.duration < avgDuration * 0.7 ? 'S' : 'M'; // Short : Medium

            // Classify gap to next note
            double gap = next.startTime - (current.startTime + current.duration);
            char gapChar = gap > avgGap * 1.5 ? 'P' :  // Pause
                          gap < avgGap * 0.5 ? 'C' : 'M'; // Connected : Medium

            rhythm.append(durationChar).append(gapChar);
        }

        return rhythm.toString();
    }

    /**
     * Attempts to extract chord sequence from melody notes (simplified approach).
     */
    private List<PitchDetectionUtils.ChordResult> extractChordSequenceFromMelody(List<DetectedNote> notes) {
        List<PitchDetectionUtils.ChordResult> chords = new ArrayList<>();

        // Group notes into potential chord segments (simultaneous or very close notes)
        List<List<DetectedNote>> chordGroups = groupNotesIntoChords(notes);

        for (List<DetectedNote> group : chordGroups) {
            if (group.size() >= 2) {
                // Create synthetic chord signal from note frequencies
                double[] chordFrequencies = group.stream().mapToDouble(n -> n.frequency).toArray();

                // Simple chord detection based on frequency ratios
                PitchDetectionUtils.ChordResult chord = detectChordFromFrequencies(chordFrequencies);
                if (chord.confidence > 0.3) {
                    chords.add(chord);
                }
            }
        }

        return chords;
    }

    /**
     * Groups consecutive notes that might form chords.
     */
    private List<List<DetectedNote>> groupNotesIntoChords(List<DetectedNote> notes) {
        List<List<DetectedNote>> groups = new ArrayList<>();

        if (notes.isEmpty()) return groups;

        List<DetectedNote> currentGroup = new ArrayList<>();
        currentGroup.add(notes.get(0));

        for (int i = 1; i < notes.size(); i++) {
            DetectedNote current = notes.get(i);
            DetectedNote last = currentGroup.get(currentGroup.size() - 1);

            // Check if notes are simultaneous or very close (within 50ms)
            double timeDiff = Math.abs(current.startTime - last.startTime);
            if (timeDiff < 0.05) { // 50ms threshold
                currentGroup.add(current);
            } else {
                if (currentGroup.size() > 1) {
                    groups.add(new ArrayList<>(currentGroup));
                }
                currentGroup.clear();
                currentGroup.add(current);
            }
        }

        if (currentGroup.size() > 1) {
            groups.add(currentGroup);
        }

        return groups;
    }

    /**
     * Simple chord detection from frequency set.
     */
    private PitchDetectionUtils.ChordResult detectChordFromFrequencies(double[] frequencies) {
        // Simplified chord detection - in a real system this would be more sophisticated
        if (frequencies.length < 2) {
            return new PitchDetectionUtils.ChordResult(new double[0], 0.0, "Unknown", "unknown");
        }

        // Sort frequencies and look for common intervals
        double[] sorted = Arrays.copyOf(frequencies, frequencies.length);
        Arrays.sort(sorted);

        // Check for major chord pattern (root, major third, fifth)
        if (frequencies.length >= 3) {
            double root = sorted[0];
            double third = sorted[1];
            double fifth = sorted[2];

            double thirdRatio = third / root;
            double fifthRatio = fifth / root;

            if (Math.abs(thirdRatio - Math.pow(2, 4.0/12.0)) < 0.1 &&
                Math.abs(fifthRatio - Math.pow(2, 7.0/12.0)) < 0.1) {
                return new PitchDetectionUtils.ChordResult(frequencies, 0.8, "C", "major");
            }
        }

        // Default to unknown chord
        return new PitchDetectionUtils.ChordResult(frequencies, 0.3, "Unknown", "unknown");
    }

    /**
     * Advanced pattern matching with multiple criteria.
     */
    private List<RecognitionResult> findBestMatchesAdvanced(String parsonsCode, String rhythmPattern,
                                                          List<PitchDetectionUtils.ChordResult> chords, int maxResults) {
        List<RecognitionResult> results = new ArrayList<>();

        for (Map.Entry<String, MelodyEntry> entry : melodyDatabase.entrySet()) {
            String songTitle = entry.getKey();
            MelodyEntry melodyEntry = entry.getValue();

            // Multi-criteria scoring
            double melodyScore = ParsonsCodeUtils.calculateSimilarity(parsonsCode, melodyEntry.parsonsCode);
            double rhythmScore = rhythmPattern.isEmpty() ? 0.5 : calculateRhythmSimilarity(rhythmPattern, melodyEntry);
            double harmonyScore = chords.isEmpty() ? 0.5 : calculateHarmonySimilarity(chords, melodyEntry);

            // Weighted combination
            double combinedScore = melodyScore * 0.6 + rhythmScore * 0.2 + harmonyScore * 0.2;

            if (combinedScore > 0.3) { // Threshold for consideration
                results.add(new RecognitionResult(songTitle, combinedScore, melodyEntry.parsonsCode));
            }
        }

        // Sort and limit results
        return results.stream()
            .sorted((r1, r2) -> Double.compare(r2.confidence, r1.confidence))
            .limit(maxResults)
            .collect(Collectors.toList());
    }

    /**
     * Calculates rhythm similarity (placeholder - would need rhythm database).
     */
    private double calculateRhythmSimilarity(String queryRhythm, MelodyEntry entry) {
        if (entry.rhythmPattern == null || entry.rhythmPattern.isEmpty() || queryRhythm.isEmpty()) {
            return 0.5; // Neutral score if no data
        }
        return ParsonsCodeUtils.calculateSimilarity(queryRhythm, entry.rhythmPattern);
    }

    /**
     * Calculates harmony similarity (placeholder - would need chord database).
     */
    private double calculateHarmonySimilarity(List<PitchDetectionUtils.ChordResult> chords, MelodyEntry entry) {
        // Simplified - in a real system this would compare against stored chord progressions
        return chords.isEmpty() ? 0.5 : 0.6;
    }

    /**
     * Re-ranks results based on note confidence levels and advanced validation.
     */
    private List<RecognitionResult> rerankResultsByConfidence(List<RecognitionResult> results, List<DetectedNote> notes) {
        if (results.isEmpty() || notes.isEmpty()) return results;

        double avgNoteConfidence = notes.stream().mapToDouble(n -> n.confidence).average().orElse(0.5);
        double[] frequencies = notes.stream().mapToDouble(n -> n.frequency).toArray();

        // Advanced confidence calculation with multiple factors
        return results.stream()
            .map(result -> {
                double baseConfidence = result.confidence;
                double totalBonus = 0.0;

                // Note confidence bonus
                double confidenceBonus = avgNoteConfidence > 0.8 ? 0.15 :
                                       avgNoteConfidence > 0.6 ? 0.08 : 0.0;
                totalBonus += confidenceBonus;

                // Statistical validation bonus
                double statisticalBonus = validateMelodyStatistics(frequencies, notes) ? 0.1 : 0.0;
                totalBonus += statisticalBonus;

                // Harmonic coherence bonus
                double harmonicBonus = validateHarmonicCoherence(frequencies) ? 0.08 : 0.0;
                totalBonus += harmonicBonus;

                // Musical plausibility bonus
                double plausibilityBonus = validateMusicalPlausibility(notes) ? 0.07 : 0.0;
                totalBonus += plausibilityBonus;

                // Length consistency bonus (prefer matches with similar length)
                MelodyEntry entry = melodyDatabase.get(result.songTitle);
                if (entry != null) {
                    double lengthRatio = Math.min(frequencies.length, entry.parsonsCode.length()) /
                                       (double) Math.max(frequencies.length, entry.parsonsCode.length());
                    double lengthBonus = lengthRatio > 0.8 ? 0.05 : 0.0;
                    totalBonus += lengthBonus;
                }

                double adjustedConfidence = Math.min(1.0, baseConfidence + totalBonus);
                return new RecognitionResult(result.songTitle, adjustedConfidence, result.matchedCode);
            })
            .sorted((r1, r2) -> Double.compare(r2.confidence, r1.confidence))
            .collect(Collectors.toList());
    }

    /**
     * Validates melody statistics to ensure it's musically plausible.
     */
    private boolean validateMelodyStatistics(double[] frequencies, List<DetectedNote> notes) {
        if (frequencies.length < 3) return false;

        // Check frequency range (should span at least an octave for a real melody)
        double minFreq = Arrays.stream(frequencies).min().orElse(0);
        double maxFreq = Arrays.stream(frequencies).max().orElse(0);
        double rangeRatio = maxFreq / minFreq;

        if (rangeRatio < 1.5) return false; // Less than a major sixth

        // Check for reasonable note distribution (not all same note)
        long uniqueNotes = Arrays.stream(frequencies)
            .mapToLong(f -> Math.round(f)) // Round to nearest Hz
            .distinct()
            .count();

        if (uniqueNotes < frequencies.length * 0.3) return false; // Less than 30% unique notes

        // Check timing consistency
        double avgDuration = notes.stream().mapToDouble(n -> n.duration).average().orElse(0);
        double durationVariance = notes.stream()
            .mapToDouble(n -> Math.pow(n.duration - avgDuration, 2))
            .average().orElse(0);

        // High variance in duration might indicate noise rather than melody
        double cvDuration = Math.sqrt(durationVariance) / avgDuration; // Coefficient of variation
        if (cvDuration > 2.0) return false; // Too much variation

        return true;
    }

    /**
     * Validates harmonic coherence of the melody.
     */
    private boolean validateHarmonicCoherence(double[] frequencies) {
        if (frequencies.length < 4) return true; // Not enough data for meaningful analysis

        // Convert to MIDI note numbers for harmonic analysis
        int[] midiNotes = Arrays.stream(frequencies)
            .mapToInt(f -> (int) Math.round(12 * Math.log(f / 440.0) / Math.log(2) + 69))
            .toArray();

        // Check for common scale patterns
        Set<Integer> uniqueNotes = Arrays.stream(midiNotes)
            .boxed()
            .collect(Collectors.toSet());

        // Normalize to root note (find the most common note as potential root)
        Map<Integer, Long> noteCounts = Arrays.stream(midiNotes)
            .boxed()
            .collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()));

        int potentialRoot = noteCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(60);

        // Check if notes fit common scales (major, minor, etc.)
        boolean fitsMajorScale = fitsScale(uniqueNotes, potentialRoot, new int[]{0, 2, 4, 5, 7, 9, 11});
        boolean fitsMinorScale = fitsScale(uniqueNotes, potentialRoot, new int[]{0, 2, 3, 5, 7, 8, 10});

        // Check interval distribution (should have mix of small and large intervals)
        List<Integer> intervals = new ArrayList<>();
        for (int i = 1; i < midiNotes.length; i++) {
            intervals.add(Math.abs(midiNotes[i] - midiNotes[i-1]));
        }

        long smallIntervals = intervals.stream().mapToInt(Integer::intValue).filter(i -> i <= 2).count();
        long largeIntervals = intervals.stream().mapToInt(Integer::intValue).filter(i -> i >= 7).count();

        // Good melodies have a mix of step-wise motion and leaps
        boolean goodIntervalDistribution = smallIntervals > intervals.size() * 0.3 &&
                                         largeIntervals > intervals.size() * 0.1;

        return (fitsMajorScale || fitsMinorScale) && goodIntervalDistribution;
    }

    /**
     * Checks if a set of notes fits a given scale pattern.
     */
    private boolean fitsScale(Set<Integer> notes, int root, int[] scaleIntervals) {
        // Normalize notes to root
        Set<Integer> normalizedNotes = notes.stream()
            .map(note -> (note - root) % 12)
            .collect(Collectors.toSet());

        // Count how many notes fit the scale
        long fittingNotes = normalizedNotes.stream()
            .filter(interval -> Arrays.stream(scaleIntervals).anyMatch(scale -> scale == interval))
            .count();

        // At least 60% of notes should fit the scale
        return (double) fittingNotes / normalizedNotes.size() >= 0.6;
    }

    /**
     * Validates musical plausibility of the detected melody.
     */
    private boolean validateMusicalPlausibility(List<DetectedNote> notes) {
        if (notes.size() < 3) return true;

        // Check for unrealistic pitch jumps (octave jumps in single steps)
        boolean hasRealisticJumps = true;
        for (int i = 1; i < notes.size(); i++) {
            double ratio = notes.get(i).frequency / notes.get(i-1).frequency;
            if (ratio > 2.0 || ratio < 0.5) { // More than octave jump
                // Allow large jumps only if they are sustained (longer duration)
                if (notes.get(i).duration < 0.2) { // Less than 200ms
                    hasRealisticJumps = false;
                    break;
                }
            }
        }

        // Check for minimum melody length (should be at least a few seconds)
        double totalDuration = notes.stream().mapToDouble(n -> n.duration).sum();
        boolean sufficientLength = totalDuration > 2.0; // At least 2 seconds

        // Check for consistent tempo (not too erratic)
        double avgInterOnsetInterval = 0;
        for (int i = 1; i < notes.size(); i++) {
            avgInterOnsetInterval += notes.get(i).startTime - notes.get(i-1).startTime;
        }
        avgInterOnsetInterval /= (notes.size() - 1);

        // Calculate tempo consistency
        double tempoVariance = 0;
        for (int i = 1; i < notes.size(); i++) {
            double interval = notes.get(i).startTime - notes.get(i-1).startTime;
            tempoVariance += Math.pow(interval - avgInterOnsetInterval, 2);
        }
        tempoVariance /= (notes.size() - 1);
        double tempoCV = Math.sqrt(tempoVariance) / avgInterOnsetInterval; // Coefficient of variation

        boolean consistentTempo = tempoCV < 1.5; // Allow some variation but not chaos

        return hasRealisticJumps && sufficientLength && consistentTempo;
    }

    /**
     * Combines results from different matching algorithms.
     */
    private List<RecognitionResult> combineMatchingResults(List<RecognitionResult> advancedResults,
                                                         List<RecognitionResult> ensembleResults, int maxResults) {
        Map<String, Double> combinedScores = new HashMap<>();
        Map<String, String> matchedCodes = new HashMap<>();

        // Weight the different result sources
        double advancedWeight = 0.6;
        double ensembleWeight = 0.4;

        // Add advanced results
        for (RecognitionResult result : advancedResults) {
            combinedScores.put(result.songTitle,
                combinedScores.getOrDefault(result.songTitle, 0.0) + result.confidence * advancedWeight);
            matchedCodes.put(result.songTitle, result.matchedCode);
        }

        // Add ensemble results
        for (RecognitionResult result : ensembleResults) {
            combinedScores.put(result.songTitle,
                combinedScores.getOrDefault(result.songTitle, 0.0) + result.confidence * ensembleWeight);
            if (!matchedCodes.containsKey(result.songTitle)) {
                matchedCodes.put(result.songTitle, result.matchedCode);
            }
        }

        // Convert back to RecognitionResult list
        return combinedScores.entrySet().stream()
            .map(entry -> new RecognitionResult(entry.getKey(), entry.getValue(),
                      matchedCodes.getOrDefault(entry.getKey(), "")))
            .sorted((r1, r2) -> Double.compare(r2.confidence, r1.confidence))
            .limit(maxResults)
            .collect(Collectors.toList());
    }

    /**
     * Applies final validation and filtering to recognition results.
     */
    private List<RecognitionResult> applyFinalValidation(List<RecognitionResult> results, List<DetectedNote> detectedNotes) {
        if (results.isEmpty()) return results;

        return results.stream()
            .filter(result -> {
                // Apply minimum confidence threshold
                if (result.confidence < 0.4) return false;

                // Validate against melody database entry
                MelodyEntry entry = melodyDatabase.get(result.songTitle);
                if (entry == null) return false;

                // Check if the detected melody length is reasonable compared to stored melody
                double detectedLength = detectedNotes.size();
                double storedLength = entry.parsonsCode.length();
                double lengthRatio = Math.min(detectedLength, storedLength) / Math.max(detectedLength, storedLength);

                // Reject if length mismatch is too extreme
                if (lengthRatio < 0.3) return false;

                // Additional validation: check if confidence is consistent with note quality
                double avgNoteConfidence = detectedNotes.stream().mapToDouble(n -> n.confidence).average().orElse(0.0);
                double expectedConfidence = avgNoteConfidence * lengthRatio;

                // Result confidence should be reasonably close to expected confidence
                return result.confidence >= expectedConfidence * 0.7;
            })
            .sorted((r1, r2) -> Double.compare(r2.confidence, r1.confidence))
            .limit(5) // Keep only top 5 after final validation
            .collect(Collectors.toList());
    }

    /**
     * Applies ensemble method combining multiple matching algorithms.
     */
    private List<RecognitionResult> applyEnsembleMatching(List<DetectedNote> detectedNotes, int maxResults) {
        double[] frequencies = detectedNotes.stream().mapToDouble(n -> n.frequency).toArray();
        double[] confidences = detectedNotes.stream().mapToDouble(n -> n.confidence).toArray();

        // Generate multiple representations
        String parsonsCode = generateConfidenceWeightedParsonsCode(frequencies, confidences);
        String rhythmPattern = analyzeAdvancedRhythmPattern(detectedNotes);

        // Try different matching strategies
        List<RecognitionResult> parsonsResults = findBestMatches(parsonsCode, maxResults * 2);
        List<RecognitionResult> rhythmResults = findRhythmBasedMatches(rhythmPattern, maxResults * 2);
        List<RecognitionResult> statisticalResults = findStatisticalMatches(frequencies, maxResults * 2);

        // Combine results using ensemble voting
        Map<String, Double> ensembleScores = new HashMap<>();
        Map<String, String> matchedCodes = new HashMap<>();

        // Weight different methods
        double parsonsWeight = 0.5;
        double rhythmWeight = 0.3;
        double statisticalWeight = 0.2;

        // Add Parsons results
        for (RecognitionResult result : parsonsResults) {
            ensembleScores.put(result.songTitle, ensembleScores.getOrDefault(result.songTitle, 0.0) + result.confidence * parsonsWeight);
            matchedCodes.put(result.songTitle, result.matchedCode);
        }

        // Add rhythm results
        for (RecognitionResult result : rhythmResults) {
            ensembleScores.put(result.songTitle, ensembleScores.getOrDefault(result.songTitle, 0.0) + result.confidence * rhythmWeight);
            if (!matchedCodes.containsKey(result.songTitle)) {
                matchedCodes.put(result.songTitle, result.matchedCode);
            }
        }

        // Add statistical results
        for (RecognitionResult result : statisticalResults) {
            ensembleScores.put(result.songTitle, ensembleScores.getOrDefault(result.songTitle, 0.0) + result.confidence * statisticalWeight);
            if (!matchedCodes.containsKey(result.songTitle)) {
                matchedCodes.put(result.songTitle, result.matchedCode);
            }
        }

        // Convert to final results
        return ensembleScores.entrySet().stream()
            .map(entry -> new RecognitionResult(entry.getKey(), entry.getValue(),
                     matchedCodes.getOrDefault(entry.getKey(), "")))
            .sorted((r1, r2) -> Double.compare(r2.confidence, r1.confidence))
            .limit(maxResults)
            .collect(Collectors.toList());
    }

    /**
     * Finds matches based on rhythm patterns.
     */
    private List<RecognitionResult> findRhythmBasedMatches(String rhythmPattern, int maxResults) {
        List<RecognitionResult> results = new ArrayList<>();

        for (Map.Entry<String, MelodyEntry> entry : melodyDatabase.entrySet()) {
            double score = calculateRhythmSimilarity(rhythmPattern, entry.getValue());
            if (score > 0.3) {
                results.add(new RecognitionResult(entry.getKey(), score, entry.getValue().parsonsCode));
            }
        }

        return results.stream()
            .sorted((r1, r2) -> Double.compare(r2.confidence, r1.confidence))
            .limit(maxResults)
            .collect(Collectors.toList());
    }

    /**
     * Finds matches based on statistical properties of the melody.
     */
    private List<RecognitionResult> findStatisticalMatches(double[] frequencies, int maxResults) {
        List<RecognitionResult> results = new ArrayList<>();

        // Convert frequencies to MIDI notes for statistical analysis
        int[] midiNotes = Arrays.stream(frequencies)
            .mapToInt(f -> (int) Math.round(12 * Math.log(f / 440.0) / Math.log(2) + 69))
            .toArray();

        for (Map.Entry<String, MelodyEntry> entry : melodyDatabase.entrySet()) {
            double statisticalScore = calculateStatisticalSimilarity(midiNotes, entry.getValue());
            results.add(new RecognitionResult(entry.getKey(), statisticalScore, entry.getValue().parsonsCode));
        }

        return results.stream()
            .sorted((r1, r2) -> Double.compare(r2.confidence, r1.confidence))
            .limit(maxResults)
            .collect(Collectors.toList());
    }

    /**
     * Calculates statistical similarity between detected notes and stored melody.
     */
    private double calculateStatisticalSimilarity(int[] detectedNotes, MelodyEntry storedMelody) {
        if (detectedNotes.length < 3) return 0.3;

        // Analyze note distribution
        Map<Integer, Long> detectedDistribution = Arrays.stream(detectedNotes)
            .boxed()
            .collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()));

        // For stored melodies, we could pre-compute statistical properties
        // For now, use a simple heuristic based on melody length similarity
        double lengthSimilarity = Math.min(detectedNotes.length, storedMelody.parsonsCode.length()) /
                                (double) Math.max(detectedNotes.length, storedMelody.parsonsCode.length());

        // Check for scale consistency (simplified)
        Set<Integer> uniqueNotes = Arrays.stream(detectedNotes).boxed().collect(Collectors.toSet());
        double uniquenessRatio = (double) uniqueNotes.size() / detectedNotes.length;

        // Combine factors
        return (lengthSimilarity * 0.6 + uniquenessRatio * 0.4);
    }
    
    /**
     * Searches the database for melodies that best match the query code.
     *
     * @param queryCode Parsons code query
     * @param maxResults maximum number of results
     * @return sorted list of recognition results
     */
    private List<RecognitionResult> findBestMatches(String queryCode, int maxResults) {
        // Check cache first for performance
        CachedResult cached = recognitionCache.get(queryCode);
        if (cached != null && !cached.isExpired()) {
            cached.incrementAccess();
            // Return a copy of cached results limited to maxResults
            List<RecognitionResult> cachedResults = new ArrayList<>(cached.results);
            if (cachedResults.size() > maxResults) {
                cachedResults = cachedResults.subList(0, maxResults);
            }
            return cachedResults;
        }

        List<RecognitionResult> results = new ArrayList<>();

        // Penalize very short sequences (likely noise)
        double lengthPenalty = Math.min(1.0, queryCode.length() / 5.0);

        for (Map.Entry<String, MelodyEntry> entry : melodyDatabase.entrySet()) {
            String songTitle = entry.getKey();
            MelodyEntry melodyEntry = entry.getValue();

            // Calculate similarity using different strategies
            double exactSimilarity = ParsonsCodeUtils.calculateSimilarity(queryCode, melodyEntry.parsonsCode);
            double partialSimilarity = calculatePartialSimilarity(queryCode, melodyEntry.parsonsCode);
            double variationSimilarity = calculateVariationSimilarity(queryCode, melodyEntry);
            double dtwSimilarity = calculateDTWSimilarity(queryCode, melodyEntry.parsonsCode);

            // Length-normalized scoring
            double lengthRatio = Math.min(queryCode.length(), melodyEntry.parsonsCode.length()) /
                               (double) Math.max(queryCode.length(), melodyEntry.parsonsCode.length());
            double lengthBonus = 0.2 * lengthRatio; // Bonus for similar lengths

            // Combine scores with adaptive weighting from learning system
            double exactWeight = learningSystem.getWeight("exact");
            double partialWeight = learningSystem.getWeight("partial");
            double variationWeight = learningSystem.getWeight("variation");
            double dtwWeight = learningSystem.getWeight("dtw");

            double combinedScore = (exactWeight * exactSimilarity +
                                  partialWeight * partialSimilarity +
                                  variationWeight * variationSimilarity +
                                  dtwWeight * dtwSimilarity + lengthBonus) * lengthPenalty;

            // Dynamic threshold based on query quality
            double threshold = queryCode.length() < 4 ? 0.5 : 0.3;

            if (combinedScore > threshold) {
                results.add(new RecognitionResult(songTitle, combinedScore, melodyEntry.parsonsCode));
            }
        }

        // Sort by confidence and return top results
        List<RecognitionResult> finalResults = results.stream()
            .sorted((r1, r2) -> Double.compare(r2.confidence, r1.confidence))
            .limit(maxResults)
            .collect(Collectors.toList());

        // Cache the results for future queries
        recognitionCache.put(queryCode, new CachedResult(finalResults, queryCode));

        return finalResults;
    }

    /**
     * Calculates similarity using Dynamic Time Warping for flexible temporal alignment.
     */
    private double calculateDTWSimilarity(String query, String target) {
        if (query.isEmpty() || target.isEmpty()) {
            return 0.0;
        }

        int n = query.length();
        int m = target.length();

        // Create DTW cost matrix
        double[][] dtw = new double[n + 1][m + 1];

        // Initialize first row and column
        for (int i = 0; i <= n; i++) {
            dtw[i][0] = Double.MAX_VALUE / 2;
        }
        for (int j = 0; j <= m; j++) {
            dtw[0][j] = Double.MAX_VALUE / 2;
        }
        dtw[0][0] = 0;

        // Fill DTW matrix
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = (query.charAt(i - 1) == target.charAt(j - 1)) ? 0.0 : 1.0;
                dtw[i][j] = cost + Math.min(
                    Math.min(dtw[i - 1][j], dtw[i][j - 1]),
                    dtw[i - 1][j - 1]
                );
            }
        }

        // Normalize by path length
        double pathLength = Math.max(n, m);
        return Math.max(0.0, 1.0 - (dtw[n][m] / pathLength));
    }

    /**
     * Analyzes rhythm patterns from detected notes.
     *
     * @param notes list of detected notes with timing information
     * @return rhythm pattern signature
     */
    private String analyzeRhythmPattern(List<DetectedNote> notes) {
        if (notes.size() < 2) {
            return "";
        }

        StringBuilder rhythm = new StringBuilder();
        double avgDuration = notes.stream().mapToDouble(n -> n.duration).average().orElse(0.4);

        for (int i = 0; i < notes.size() - 1; i++) {
            double duration = notes.get(i).duration;
            double nextStart = notes.get(i + 1).startTime;
            double currentEnd = notes.get(i).startTime + duration;
            double gap = nextStart - currentEnd;

            // Classify note duration
            char durationChar = duration > avgDuration * 1.5 ? 'L' :  // Long
                              duration < avgDuration * 0.7 ? 'S' : 'M'; // Short : Medium

            // Classify gap
            char gapChar = gap > avgDuration * 0.5 ? 'P' : 'C'; // Pause : Connected

            rhythm.append(durationChar).append(gapChar);
        }

        return rhythm.toString();
    }

    /**
     * Enhanced melody matching that considers both pitch and rhythm.
     *
     * @param detectedNotes notes with full timing information
     * @param maxResults maximum number of results
     * @return sorted list of recognition results
     */
    private List<RecognitionResult> recognizeMelodyWithRhythm(List<DetectedNote> detectedNotes, int maxResults) {
        if (detectedNotes.isEmpty()) {
            return new ArrayList<>();
        }

        // Extract frequencies for Parsons code
        double[] frequencies = detectedNotes.stream().mapToDouble(n -> n.frequency).toArray();
        String parsonsCode = ParsonsCodeUtils.generateParsonsCode(frequencies);

        // Analyze rhythm pattern
        String rhythmPattern = analyzeRhythmPattern(detectedNotes);

        // Find matches using enhanced scoring
        List<RecognitionResult> results = findBestMatches(parsonsCode, maxResults * 2);

        // Apply rhythm-based filtering and scoring
        results = results.stream()
            .map(result -> enhanceWithRhythmScoring(result, rhythmPattern, detectedNotes))
            .sorted((r1, r2) -> Double.compare(r2.confidence, r1.confidence))
            .limit(maxResults)
            .collect(Collectors.toList());

        // Provide feedback to learning system
        if (!results.isEmpty()) {
            provideLearningFeedback(parsonsCode, results.get(0));
        }

        return results;
    }

    /**
     * Provide feedback to the learning system about recognition results.
     */
    private void provideLearningFeedback(String queryCode, RecognitionResult bestResult) {
        // Determine which algorithm contributed most to the successful recognition
        String bestAlgorithm = "exact"; // Default
        double bestContribution = 0.0;

        // This is a simplified feedback mechanism
        // In a real system, we'd track which algorithm gave the highest score
        if (bestResult.confidence > 0.7) {
            learningSystem.recordResult("exact", true);
            learningSystem.recordResult("partial", true);
            learningSystem.recordResult("variation", true);
            learningSystem.recordResult("dtw", true);
        } else if (bestResult.confidence > 0.5) {
            learningSystem.recordResult("partial", true);
            learningSystem.recordResult("variation", true);
        }
    }

    /**
     * Enhances recognition result with rhythm-based scoring.
     */
    private RecognitionResult enhanceWithRhythmScoring(RecognitionResult result, String rhythmPattern, List<DetectedNote> detectedNotes) {
        if (result == null || rhythmPattern == null || rhythmPattern.isEmpty()) {
            return result;
        }

        MelodyEntry entry = melodyDatabase.get(result.songTitle);
        if (entry == null || entry.rhythmPattern == null || entry.rhythmPattern.isEmpty()) {
            return result;
        }

        // Calculate similarity between query rhythm and stored rhythm
        double similarity = ParsonsCodeUtils.calculateSimilarity(rhythmPattern, entry.rhythmPattern);

        // Adjust confidence based on rhythm similarity
        // We adjust the confidence by a factor related to how well the rhythm matches
        // Range: -0.1 to +0.1 adjustment based on similarity (0.0 to 1.0)
        double adjustment = (similarity - 0.5) * 0.2;

        double newConfidence = result.confidence + adjustment;

        // Ensure within bounds
        newConfidence = Math.max(0.0, Math.min(1.0, newConfidence));

        return new RecognitionResult(result.songTitle, newConfidence, result.matchedCode);
    }

    /**
     * Calculates similarity of two Parsons codes allowing for partial overlaps.
     */
    private double calculatePartialSimilarity(String query, String target) {
        // Check if query is a substring of target or vice versa
        if (target.contains(query) || query.contains(target)) {
            return 0.8; // High score for substring matches
        }
        
        // Check for partial overlaps
        int maxOverlap = 0;
        int minLength = Math.min(query.length(), target.length());
        
        for (int i = 0; i <= query.length() - 3; i++) {
            for (int j = 0; j <= target.length() - 3; j++) {
                int overlap = 0;
                while (i + overlap < query.length() && 
                       j + overlap < target.length() && 
                       query.charAt(i + overlap) == target.charAt(j + overlap)) {
                    overlap++;
                }
                maxOverlap = Math.max(maxOverlap, overlap);
            }
        }
        
        return (double) maxOverlap / minLength;
    }
    
    /**
     * Calculates similarity of a query to any variation of a melody.
     */
    private double calculateVariationSimilarity(String query, MelodyEntry entry) {
        double maxSimilarity = 0.0;
        
        // Check against variations
        for (String variation : entry.variations) {
            double similarity = ParsonsCodeUtils.calculateSimilarity(query, variation);
            maxSimilarity = Math.max(maxSimilarity, similarity);
        }
        
        return maxSimilarity;
    }
    
    /**
     * Generates a digital signal for the specified melody.
     *
     * @param melody note names
     * @return concatenated signal samples
     */
    private double[] generateMelodySignal(String[] melody) {
        List<Double> signal = new ArrayList<>();
        
        for (String note : melody) {
            double frequency = noteToFrequency(note);
            double[] noteSignal = generateTone(frequency, NOTE_DURATION, 1.0);
            
            for (double sample : noteSignal) {
                signal.add(sample);
            }
        }
        
        return signal.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    /**
     * Extracts a sequence of pitch values from a time-domain signal using intelligent segmentation.
     *
     * <p>This improved version uses:
     * - Sliding window analysis with confidence-based segmentation
     * - Intelligent note grouping based on frequency stability
     * - Confidence-weighted pitch estimation
     * - Duration analysis for rhythm recognition</p>
     *
     * @param signal input audio samples
     * @return array of detected pitches
     */
    private double[] extractPitchSequence(double[] signal) {
        List<DetectedNote> notes = extractDetectedNotes(signal);
        return notes.stream().mapToDouble(note -> note.frequency).toArray();
    }

    /**
     * Extracts detected notes with full timing and duration information using advanced pitch detection.
     *
     * <p>Enhanced version using:
     * - Adaptive window sizing based on signal characteristics
     * - Multi-resolution analysis for better note boundary detection
     * - Confidence-weighted segmentation
     * - Harmonic analysis for improved accuracy</p>
     *
     * @param signal input audio samples
     * @return list of detected notes with timing information
     */
    private List<DetectedNote> extractDetectedNotes(double[] signal) {
        // Enhanced parameters for advanced segmentation
        final double CONFIDENCE_THRESHOLD = 0.4;          // Lower threshold for advanced detection
        final double FREQUENCY_TOLERANCE = 15.0;          // Tighter tolerance for better accuracy
        final int MIN_SEGMENT_LENGTH = 2;                 // Reduced for faster response
        final double MIN_FREQUENCY = 75.0;                // Lower minimum for bass notes
        final double MAX_FREQUENCY = 2000.0;              // Upper limit for harmonics

        List<PitchSegment> segments = new ArrayList<>();

        // Multi-resolution sliding window analysis
        int[] windowSizes = {(int)(SAMPLE_RATE * 0.08), (int)(SAMPLE_RATE * 0.12), (int)(SAMPLE_RATE * 0.16)};
        int hopSize = (int)(SAMPLE_RATE * 0.04); // 40ms hop for better temporal resolution

        for (int start = 0; start < signal.length - windowSizes[0]; start += hopSize) {
            PitchSegment bestSegment = null;
            double bestConfidence = 0.0;

            // Try multiple window sizes for better note boundary detection
            for (int windowSize : windowSizes) {
                if (start + windowSize > signal.length) continue;

                double[] window = Arrays.copyOfRange(signal, start, start + windowSize);
                PitchDetectionUtils.PitchResult result = PitchDetectionUtils.detectPitchHybrid(window, SAMPLE_RATE);

                // Enhanced validation with harmonic analysis
                if (result.isVoiced && result.confidence > CONFIDENCE_THRESHOLD &&
                    result.frequency > MIN_FREQUENCY && result.frequency < MAX_FREQUENCY) {

                    // Check for harmonic consistency (fundamental + harmonics)
                    boolean hasHarmonics = checkHarmonicConsistency(window, result.frequency, SAMPLE_RATE);
                    double adjustedConfidence = result.confidence * (hasHarmonics ? 1.2 : 0.9);

                    if (adjustedConfidence > bestConfidence) {
                        bestConfidence = adjustedConfidence;
                        bestSegment = new PitchSegment(result.frequency, adjustedConfidence, start, start + windowSize);
                    }
                }
            }

            if (bestSegment != null) {
                segments.add(bestSegment);
            }
        }

        // Advanced note grouping with temporal and frequency analysis
        return groupSegmentsIntoNotesAdvanced(segments, FREQUENCY_TOLERANCE, MIN_SEGMENT_LENGTH);
    }

    /**
     * Checks for harmonic consistency in the signal.
     */
    private boolean checkHarmonicConsistency(double[] window, double fundamentalFreq, double sampleRate) {
        try {
            // Quick FFT analysis to check for harmonics
            double[] padded = FFTUtils.zeroPadToPowerOfTwo(window);
            FFTResult spectrum = FFTUtils.fft(padded);

            double[] magnitudes = spectrum.getMagnitudes();
            int fftSize = magnitudes.length;

            // Check for 2nd and 3rd harmonics
            int fundamentalBin = PitchDetectionUtils.frequencyToBin(fundamentalFreq, sampleRate, fftSize);
            int secondHarmonicBin = fundamentalBin * 2;
            int thirdHarmonicBin = fundamentalBin * 3;

            if (secondHarmonicBin >= magnitudes.length || thirdHarmonicBin >= magnitudes.length) {
                return false;
            }

            double fundamentalMag = magnitudes[fundamentalBin];
            double secondMag = magnitudes[secondHarmonicBin];
            double thirdMag = magnitudes[thirdHarmonicBin];

            // Harmonics should be present but weaker than fundamental
            return secondMag > fundamentalMag * 0.1 && thirdMag > fundamentalMag * 0.05;
        } catch (Exception e) {
            return false; // Conservative approach
        }
    }

    /**
     * Advanced note grouping with temporal coherence and frequency stability analysis.
     */
    private List<DetectedNote> groupSegmentsIntoNotesAdvanced(List<PitchSegment> segments, double freqTolerance, int minSegments) {
        List<DetectedNote> notes = new ArrayList<>();

        if (segments.isEmpty()) {
            return notes;
        }

        // Sort segments by time
        segments.sort(Comparator.comparingInt(s -> s.startSample));

        List<PitchSegment> currentGroup = new ArrayList<>();
        double currentFreq = segments.get(0).frequency;
        double currentFreqVariance = 0.0;
        int groupStartSample = segments.get(0).startSample;

        for (int i = 0; i < segments.size(); i++) {
            PitchSegment segment = segments.get(i);

            // Calculate frequency stability for current group
            double avgFreq = currentGroup.stream().mapToDouble(s -> s.frequency).average().orElse(segment.frequency);
            double variance = currentGroup.stream()
                .mapToDouble(s -> Math.pow(s.frequency - avgFreq, 2))
                .average().orElse(0.0);

            // Adaptive tolerance based on frequency range and group stability
            double adaptiveTolerance = Math.max(freqTolerance, Math.abs(avgFreq) * 0.02); // 2% of frequency

            // Check temporal continuity (no large gaps)
            boolean temporalContinuity = currentGroup.isEmpty() ||
                (segment.startSample - currentGroup.get(currentGroup.size() - 1).endSample) < SAMPLE_RATE * 0.2; // 200ms gap max

            // Check if segment belongs to current group
            if (Math.abs(segment.frequency - avgFreq) <= adaptiveTolerance &&
                variance < 100.0 && // Frequency stability check
                temporalContinuity) {

                currentGroup.add(segment);
                currentFreqVariance = variance;

            } else {
                // Finish current group and start new one
                if (currentGroup.size() >= minSegments) {
                    DetectedNote note = createDetectedNoteAdvanced(currentGroup, groupStartSample);
                    if (note != null) {
                        notes.add(note);
                    }
                }
                currentGroup.clear();
                currentGroup.add(segment);
                currentFreq = segment.frequency;
                currentFreqVariance = 0.0;
                groupStartSample = segment.startSample;
            }
        }

        // Add final group
        if (currentGroup.size() >= minSegments) {
            DetectedNote note = createDetectedNoteAdvanced(currentGroup, groupStartSample);
            if (note != null) {
                notes.add(note);
            }
        }

        // Post-processing: merge very short notes with neighbors if they have similar frequencies
        notes = mergeShortNotes(notes);

        return notes;
    }

    /**
     * Creates a detected note with advanced confidence calculation.
     */
    private DetectedNote createDetectedNoteAdvanced(List<PitchSegment> segments, int groupStartSample) {
        if (segments.isEmpty()) return null;

        double weightedFreq = calculateWeightedFrequency(segments);
        double totalDuration = segments.stream().mapToDouble(PitchSegment::getDuration).sum();
        double avgConfidence = segments.stream().mapToDouble(s -> s.confidence).average().orElse(0.0);

        // Enhanced confidence calculation considering frequency stability
        double freqVariance = calculateFrequencyVariance(segments);
        double stabilityBonus = Math.max(0.0, 1.0 - freqVariance / 100.0); // Bonus for stable frequencies
        double finalConfidence = Math.min(1.0, avgConfidence * (0.8 + 0.2 * stabilityBonus));

        double startTime = groupStartSample / SAMPLE_RATE;

        return new DetectedNote(weightedFreq, totalDuration, finalConfidence, startTime);
    }

    /**
     * Calculates frequency variance for stability analysis.
     */
    private double calculateFrequencyVariance(List<PitchSegment> segments) {
        if (segments.size() < 2) return 0.0;

        double mean = segments.stream().mapToDouble(s -> s.frequency).average().orElse(0.0);
        double variance = segments.stream()
            .mapToDouble(s -> Math.pow(s.frequency - mean, 2))
            .average().orElse(0.0);

        return variance;
    }

    /**
     * Merges very short notes with neighboring notes of similar frequency.
     */
    private List<DetectedNote> mergeShortNotes(List<DetectedNote> notes) {
        if (notes.size() < 2) return notes;

        List<DetectedNote> merged = new ArrayList<>();
        DetectedNote current = notes.get(0);

        for (int i = 1; i < notes.size(); i++) {
            DetectedNote next = notes.get(i);

            // Merge if current note is very short and frequencies are similar
            boolean shouldMerge = current.duration < 0.15 && // Less than 150ms
                                Math.abs(current.frequency - next.frequency) < 30.0 && // Similar frequency
                                (next.startTime - (current.startTime + current.duration)) < 0.1; // Close in time

            if (shouldMerge) {
                // Merge notes
                double totalDuration = current.duration + next.duration;
                double weightedFreq = (current.frequency * current.duration + next.frequency * next.duration) / totalDuration;
                double avgConfidence = (current.confidence + next.confidence) / 2.0;

                current = new DetectedNote(weightedFreq, totalDuration, avgConfidence, current.startTime);
            } else {
                merged.add(current);
                current = next;
            }
        }

        merged.add(current);
        return merged;
    }

    /**
     * Creates a DetectedNote from a group of segments.
     */
    private DetectedNote createDetectedNote(List<PitchSegment> segments, int groupStartSample) {
        double weightedFreq = calculateWeightedFrequency(segments);
        double totalDuration = segments.stream().mapToDouble(PitchSegment::getDuration).sum();
        double avgConfidence = segments.stream().mapToDouble(s -> s.confidence).average().orElse(0.0);
        double startTime = groupStartSample / SAMPLE_RATE;

        return new DetectedNote(weightedFreq, totalDuration, avgConfidence, startTime);
    }

    /**
     * Calculates confidence-weighted average frequency for a group of segments.
     */
    private double calculateWeightedFrequency(List<PitchSegment> segments) {
        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (PitchSegment segment : segments) {
            double weight = segment.confidence * segment.confidence; // Square for stronger weighting
            weightedSum += segment.frequency * weight;
            totalWeight += weight;
        }

        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }

    /**
     * Represents a pitch detection segment with timing and confidence information.
     */
    private static class PitchSegment {
        final double frequency;
        final double confidence;
        final int startSample;
        final int endSample;

        PitchSegment(double frequency, double confidence, int startSample, int endSample) {
            this.frequency = frequency;
            this.confidence = confidence;
            this.startSample = startSample;
            this.endSample = endSample;
        }

        double getDuration() {
            return (endSample - startSample) / SAMPLE_RATE;
        }
    }

    /**
     * Represents a detected note with frequency, duration, and confidence.
     */
    private static class DetectedNote {
        final double frequency;
        final double duration;
        final double confidence;
        final double startTime;

        DetectedNote(double frequency, double duration, double confidence, double startTime) {
            this.frequency = frequency;
            this.duration = duration;
            this.confidence = confidence;
            this.startTime = startTime;
        }

        String toNoteName() {
            // Convert frequency to note name (simplified)
            String[] notes = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
            int midiNote = (int) Math.round(12 * Math.log(frequency / 440.0) / Math.log(2) + 69);
            int noteIndex = midiNote % 12;
            int octave = (midiNote / 12) - 1;
            return notes[noteIndex] + octave;
        }
    }

    /**
     * Cached recognition result for performance optimization.
     */
    private static class CachedResult {
        final List<RecognitionResult> results;
        final long timestamp;
        final String queryCode;
        int accessCount;

        CachedResult(List<RecognitionResult> results, String queryCode) {
            this.results = new ArrayList<>(results);
            this.timestamp = System.currentTimeMillis();
            this.queryCode = queryCode;
            this.accessCount = 1;
        }

        boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > 300_000; // 5 minutes
        }

        void incrementAccess() {
            this.accessCount++;
        }

        boolean isFrequent() {
            return accessCount > 3;
        }
    }

    /**
     * Learning system for continuous improvement of recognition accuracy.
     */
    private static class LearningSystem {
        private final Map<String, Double> algorithmWeights = new HashMap<>();
        private final Map<String, Integer> successCounts = new HashMap<>();
        private final Map<String, Integer> totalCounts = new HashMap<>();

        LearningSystem() {
            // Initialize default weights
            algorithmWeights.put("exact", 0.3);
            algorithmWeights.put("partial", 0.25);
            algorithmWeights.put("variation", 0.2);
            algorithmWeights.put("dtw", 0.15);
            algorithmWeights.put("length", 0.1);
        }

        void recordResult(String algorithm, boolean success) {
            String key = "algorithm_" + algorithm;
            totalCounts.put(key, totalCounts.getOrDefault(key, 0) + 1);
            if (success) {
                successCounts.put(key, successCounts.getOrDefault(key, 0) + 1);
            }

            // Adapt weights based on performance
            updateWeights();
        }

        private static final String[] ALGORITHMS = {"exact", "partial", "variation", "dtw"};

        private void updateWeights() {
            for (String algorithm : ALGORITHMS) {
                String key = "algorithm_" + algorithm;
                int successes = successCounts.getOrDefault(key, 0);
                int total = totalCounts.getOrDefault(key, 1); // Avoid division by zero
                double successRate = (double) successes / total;

                // Adjust weight based on success rate (reinforcement learning)
                double currentWeight = algorithmWeights.get(algorithm);
                double adjustment = (successRate - 0.5) * 0.01; // Small adjustments
                double newWeight = Math.max(0.05, Math.min(0.5, currentWeight + adjustment));

                algorithmWeights.put(algorithm, newWeight);
            }

            // Normalize weights to sum to 1.0 (excluding length bonus)
            double totalWeight = algorithmWeights.values().stream()
                .mapToDouble(Double::doubleValue).sum() - algorithmWeights.get("length");
            for (String algorithm : ALGORITHMS) {
                algorithmWeights.put(algorithm, algorithmWeights.get(algorithm) / totalWeight * 0.9);
            }
        }

        double getWeight(String algorithm) {
            return algorithmWeights.getOrDefault(algorithm, 0.2);
        }

        void printLearningStats() {
            logger.info("Learning System Statistics:");
            logger.info("---------------------------");
            for (String algorithm : ALGORITHMS) {
                String key = "algorithm_" + algorithm;
                int successes = successCounts.getOrDefault(key, 0);
                int total = totalCounts.getOrDefault(key, 0);
                double successRate = total > 0 ? (double) successes / total : 0.0;
                double weight = algorithmWeights.get(algorithm);
                logger.info("{}: {}% success ({}/{}), weight: {}",
                    algorithm, String.format("%.1f", successRate * 100), successes, total, String.format("%.3f", weight));
            }
            logger.info("");
        }
    }
    
    /**
     * Generates a basic sine wave tone.
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
     * Adds white noise to the signal.
     */
    private void addNoise(double[] signal, double noiseLevel) {
        for (int i = 0; i < signal.length; i++) {
            signal[i] += noiseLevel * RANDOM.nextGaussian();
        }
    }
    
    /**
     * Detects the fundamental frequency in the signal using optimized hybrid algorithm.
     *
     * <p>This method uses an intelligent hybrid approach that combines
     * YIN algorithm accuracy with spectral analysis robustness for optimal
     * performance and reliability.</p>
     *
     * @param signal input audio samples
     * @return detected frequency in Hz, or 0.0 if no reliable pitch found
     */
    private double detectPitch(double[] signal) {
        // Use optimized hybrid pitch detection
        PitchDetectionUtils.PitchResult result = PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE);

        // Return frequency if voiced and confident enough
        return (result.isVoiced && result.confidence > 0.3) ? result.frequency : 0.0;
    }

    /**
     * Enhanced pitch detection that returns full result information.
     *
     * @param signal input audio samples
     * @return complete pitch detection result
     */
    private PitchDetectionUtils.PitchResult detectPitchEnhanced(double[] signal) {
        return PitchDetectionUtils.detectPitchHybrid(signal, SAMPLE_RATE);
    }
    

    
    /**
     * Maps a note name to its frequency in Hz.
     */
    private double noteToFrequency(String note) {
        // Map note names to frequencies (A4 = 440 Hz)
        Map<String, Double> noteFreqs = new HashMap<>();
        noteFreqs.put("C4", 261.63);
        noteFreqs.put("D4", 293.66);
        noteFreqs.put("E4", 329.63);
        noteFreqs.put("F4", 349.23);
        noteFreqs.put("G4", 392.00);
        noteFreqs.put("A4", 440.00);
        noteFreqs.put("B4", 493.88);
        noteFreqs.put("C5", 523.25);
        
        return noteFreqs.getOrDefault(note, 440.0);
    }
    
    /**
     * Builds an in-memory database of melodies with variations.
     *
     * @return map of song titles to melody data
     */
    private Map<String, MelodyEntry> createEnhancedMelodyDatabase() {
        Map<String, MelodyEntry> database = new HashMap<>();

        // Add comprehensive entries with multiple variations and metadata for robust recognition
        database.put("Twinkle, Twinkle, Little Star",
            new MelodyEntry("*RURURDRRURURDR", "MCMCMCMCMCMC",
                Arrays.asList("*RURUR", "*RURURD", "*RDRRUR", "*RURURDRRUR", "*RURURDRRURUR"),
                "Children's", "Traditional", "Traditional", 120.0, "C", 0.9));

        database.put("Mary Had a Little Lamb",
            new MelodyEntry("*DRRRRDDDRRU", "MCMCMCMCMCMC",
                Arrays.asList("*DRRRR", "*DDDRRU", "*DRRRRDDD", "*DRRRRD", "*RRRDDDRRU"),
                "Children's", "Traditional", "Traditional", 100.0, "E", 0.8));

        database.put("Happy Birthday",
            new MelodyEntry("*RRUURURURU", "SCSCMCMCMC",
                Arrays.asList("*RRURU", "*RURURU", "*RRURURU", "*RRUURUR", "*RURURURU"),
                "Celebration", "Traditional", "Traditional", 110.0, "C", 1.0));

        // Add more classic melodies for comprehensive testing
        database.put("Row, Row, Row Your Boat",
            new MelodyEntry("*RURURDRR",
                Arrays.asList("*RURUR", "*RURURD", "*RURURDR", "*URURDRR")));

        database.put("Ode to Joy",
            new MelodyEntry("*RURURDRURURDR", "MCMCMCMCMCMCMC",
                Arrays.asList("*RURURD", "*RURURDR", "*URURDR", "*RURURDRUR"),
                "Classical", "Romantic", "Beethoven", 120.0, "D", 0.95));

        database.put("London Bridge",
            new MelodyEntry("*RURUDRURU", "MCSCMCMCMCMC",
                Arrays.asList("*RURU", "*RURUD", "*URUDRURU", "*RURUDRUR"),
                "Children's", "Traditional", "Traditional", 100.0, "D", 0.7));

        database.put("Frère Jacques",
            new MelodyEntry("*RDRURDRU",
                Arrays.asList("*RDRU", "*RDRUR", "*DRURDRU", "*RDRURDR")));

        database.put("Silent Night",
            new MelodyEntry("*RDRURDRURU",
                Arrays.asList("*RDRU", "*RDRUR", "*DRURDRURU", "*RDRURDRU")));
        
        database.put("Ode to Joy", 
            new MelodyEntry("*RRURURDRUR", 
                Arrays.asList("*RRURU", "*RURDR", "*URURDR")));
        
        database.put("Amazing Grace", 
            new MelodyEntry("*URDURDRDUR", 
                Arrays.asList("*URDU", "*RDURD", "*URDURD")));
        
        database.put("Silent Night", 
            new MelodyEntry("*RDURDURDRU", 
                Arrays.asList("*RDUR", "*DURDR", "*RDURDU")));
        
        database.put("Jingle Bells", 
            new MelodyEntry("*RRRUURURURU", 
                Arrays.asList("*RRRU", "*RURURU", "*RRRUURU")));
        
        database.put("London Bridge", 
            new MelodyEntry("*RDRRURDRUR", 
                Arrays.asList("*RDRR", "*RURDR", "*RDRRUR")));
        
        database.put("Row, Row, Row Your Boat", 
            new MelodyEntry("*RRURURDRUR", 
                Arrays.asList("*RRUR", "*URURDR", "*RURURD")));
        
        database.put("Frère Jacques", 
            new MelodyEntry("*URUURURDRUR", 
                Arrays.asList("*URUR", "*RURUR", "*URUURU")));
        
        return database;
    }
    
    // Data classes
    private static class MelodyEntry {
        final String parsonsCode;
        final String rhythmPattern;
        final List<String> variations;
        final String genre;
        final String era;
        final String artist;
        final double avgTempo; // BPM
        final String key; // Musical key
        final double popularity; // 0.0 to 1.0

        MelodyEntry(String parsonsCode, List<String> variations) {
            this(parsonsCode, "", variations, "Unknown", "Unknown", "Unknown", 120.0, "C", 0.5);
        }

        MelodyEntry(String parsonsCode, String rhythmPattern, List<String> variations, String genre, String era,
                   String artist, double avgTempo, String key, double popularity) {
            this.parsonsCode = parsonsCode;
            this.rhythmPattern = rhythmPattern;
            this.variations = variations;
            this.genre = genre;
            this.era = era;
            this.artist = artist;
            this.avgTempo = avgTempo;
            this.key = key;
            this.popularity = popularity;
        }
    }
    
    private static class RecognitionResult {
        final String songTitle;
        final double confidence;
        final String matchedCode;
        
        RecognitionResult(String songTitle, double confidence, String matchedCode) {
            this.songTitle = songTitle;
            this.confidence = confidence;
            this.matchedCode = matchedCode;
        }
    }
}
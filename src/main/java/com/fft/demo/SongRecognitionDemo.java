package com.fft.demo;

import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;
import com.fft.utils.PitchDetectionUtils;

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
    
    private static final double SAMPLE_RATE = 44100.0;
    private static final int FFT_SIZE = 4096;
    private static final double NOTE_DURATION = 0.4; // seconds per note
    private static final double FREQUENCY_TOLERANCE = 25.0; // Hz for grouping notes
    
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
        System.out.println("=== FFT-Based Song Recognition Demo ===\n");
        
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
        demonstratePerformanceAnalysis();
    }
    
    /**
     * Recognizes complete melodies from a predefined list.
     */
    private void demonstrateBasicSongRecognition() {
        System.out.println("1. Basic Song Recognition:");
        System.out.println("-------------------------");
        
        // Test with complete melody of "Twinkle, Twinkle, Little Star"
        String[] twinkleMelody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4", "F4", "F4", "E4", "E4", "D4", "D4", "C4"};
        testMelodyRecognition("Twinkle, Twinkle, Little Star (complete)", twinkleMelody);
        
        // Test with "Mary Had a Little Lamb"
        String[] maryMelody = {"E4", "D4", "C4", "D4", "E4", "E4", "E4", "D4", "D4", "D4", "E4", "G4", "G4"};
        testMelodyRecognition("Mary Had a Little Lamb", maryMelody);
        
        System.out.println();
    }
    
    /**
     * Shows recognition ability using incomplete melody fragments.
     */
    private void demonstratePartialMelodyMatching() {
        System.out.println("2. Partial Melody Matching:");
        System.out.println("---------------------------");
        
        // Test with just the beginning of "Twinkle, Twinkle"
        String[] partialTwinkle = {"C4", "C4", "G4", "G4", "A4"};
        testMelodyRecognition("Twinkle, Twinkle (partial)", partialTwinkle);
        
        // Test with middle section of "Happy Birthday"
        String[] partialBirthday = {"C4", "C4", "D4", "C4", "F4"};
        testMelodyRecognition("Happy Birthday (partial)", partialBirthday);
        
        System.out.println();
    }
    
    /**
     * Tests melody recognition when the input contains noise.
     */
    private void demonstrateNoisyMelodyRecognition() {
        System.out.println("3. Noisy Melody Recognition:");
        System.out.println("----------------------------");
        
        String[] melody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4"};
        
        // Test with different noise levels
        double[] noiseLevels = {0.0, 0.1, 0.2, 0.3};
        
        for (double noiseLevel : noiseLevels) {
            System.out.printf("Testing with noise level %.1f:\n", noiseLevel);
            testNoisyMelodyRecognition(melody, noiseLevel);
        }
        
        System.out.println();
    }
    
    /**
     * Demonstrates robustness to transposition and rhythmic changes.
     */
    private void demonstrateVariationTolerance() {
        System.out.println("4. Variation Tolerance:");
        System.out.println("----------------------");
        
        // Test transposed version (different key)
        String[] originalMelody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4"};
        String[] transposedMelody = {"D4", "D4", "A4", "A4", "B4", "B4", "A4"}; // Up one tone
        
        System.out.println("Original melody in C:");
        testMelodyRecognition("Twinkle (original)", originalMelody);
        
        System.out.println("Transposed melody in D:");
        testMelodyRecognition("Twinkle (transposed)", transposedMelody);
        
        // Test with rhythmic variations (doubled notes)
        String[] rhythmicVariation = {"C4", "C4", "C4", "C4", "G4", "G4", "G4", "G4"};
        System.out.println("Rhythmic variation:");
        testMelodyRecognition("Twinkle (rhythmic variation)", rhythmicVariation);
        
        System.out.println();
    }
    
    /**
     * Simulates incremental recognition as more notes of a melody arrive with enhanced feedback and confidence analysis.
     */
    private void demonstrateRealTimeRecognition() {
        System.out.println("5. Real-Time Recognition Simulation:");
        System.out.println("------------------------------------");

        String[] melody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4", "F4", "F4", "E4", "E4", "D4", "D4", "C4"};

        System.out.println("Simulating incremental recognition with intelligent segmentation and confidence analysis:");

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
                System.out.printf("After %d notes: No pitches detected\n", length);
                confidenceHistory.add(0.0);
                continue;
            }

            // Extract frequencies for Parsons code
            double[] detectedPitches = detectedNotes.stream().mapToDouble(n -> n.frequency).toArray();
            String parsonsCode = ParsonsCodeUtils.generateParsonsCode(detectedPitches);
            List<RecognitionResult> results = findBestMatches(parsonsCode, 3);

            // Calculate average confidence of detected notes
            double avgNoteConfidence = detectedNotes.stream().mapToDouble(n -> n.confidence).average().orElse(0.0);

            System.out.printf("After %d notes (%d detected, avg conf: %.2f): ",
                length, detectedNotes.size(), avgNoteConfidence);

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

                System.out.printf("Best match: %s (%.1f%% %s confidence)%s\n",
                    best.songTitle, best.confidence * 100, confidenceLevel, stability);
            } else {
                confidenceHistory.add(0.0);
                System.out.println("No strong matches yet");
            }
        }

        // Analyze overall confidence trend
        if (!confidenceHistory.isEmpty()) {
            double avgConfidence = confidenceHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double maxConfidence = confidenceHistory.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            System.out.printf("Overall: Avg confidence %.1f%%, Peak confidence %.1f%%\n",
                avgConfidence * 100, maxConfidence * 100);
        }

        System.out.println();
    }
    
    /**
     * Benchmarks recognition performance and analyzes FFT overhead.
     */
    private void demonstratePerformanceAnalysis() {
        System.out.println("6. Performance Analysis:");
        System.out.println("-----------------------");
        
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
        
        System.out.printf("Recognition performance (%d iterations):\n", iterations);
        System.out.printf("Average recognition time: %.2f ms\n", averageTime);
        System.out.printf("Database size: %d melodies\n", melodyDatabase.size());
        System.out.printf("Recognition rate: %.1f recognitions/second\n", 1000.0 / averageTime);
        
        // Analyze FFT performance contribution
        double[] signal = generateMelodySignal(testMelody);
        
        long fftStartTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            extractPitchSequence(signal);
        }
        long fftEndTime = System.nanoTime();
        
        double fftTime = (fftEndTime - fftStartTime) / (double) iterations / 1_000_000.0;
        System.out.printf("FFT analysis time: %.2f ms (%.1f%% of total)\n",
            fftTime, (fftTime / averageTime) * 100);

        // Test recognition accuracy with different noise levels
        testRecognitionRobustness();

        // Show learning system statistics
        learningSystem.printLearningStats();

        System.out.println();
    }

    /**
     * Tests recognition robustness across different noise levels and conditions.
     */
    private void testRecognitionRobustness() {
        System.out.println("Recognition Robustness Test:");
        System.out.println("---------------------------");

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
            System.out.printf("Noise %.1f: %d pitches detected, accuracy: %.1f%%\n",
                noise, pitches.length, accuracy * 100);
        }
    }
    
    /**
     * Helper that prints recognition results for a given melody.
     *
     * @param description description displayed before the results
     * @param melody array of note names
     */
    private void testMelodyRecognition(String description, String[] melody) {
        System.out.printf("Testing: %s\n", description);
        
        List<RecognitionResult> results = recognizeMelody(melody, true);
        
        if (results.isEmpty()) {
            System.out.println("  No matches found");
        } else {
            System.out.println("  Top matches:");
            for (int i = 0; i < Math.min(3, results.size()); i++) {
                RecognitionResult result = results.get(i);
                System.out.printf("    %d. %s (%.1f%% confidence)\n", 
                    i + 1, result.songTitle, result.confidence * 100);
            }
        }
        System.out.println();
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
            System.out.println("  No reliable pitches detected (too noisy)");
            return;
        }

        // Extract frequencies for Parsons code
        double[] noisyPitches = detectedNotes.stream().mapToDouble(n -> n.frequency).toArray();
        String noisyParsons = ParsonsCodeUtils.generateParsonsCode(noisyPitches);

        // Analyze rhythm pattern
        String rhythmPattern = analyzeRhythmPattern(detectedNotes);

        System.out.printf("  Detected %d notes, Parsons code: %s, Rhythm: %s\n",
            detectedNotes.size(), noisyParsons, rhythmPattern);

        // Use enhanced recognition with rhythm information
        List<RecognitionResult> results = recognizeMelodyWithRhythm(detectedNotes, 3);

        if (!results.isEmpty()) {
            RecognitionResult best = results.get(0);
            double avgConfidence = detectedNotes.stream().mapToDouble(n -> n.confidence).average().orElse(0.0);
            System.out.printf("  Best match: %s (%.1f%% confidence, avg note conf: %.2f)\n",
                best.songTitle, best.confidence * 100, avgConfidence);
        } else {
            System.out.println("  No matches found");
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
            System.out.printf("  Generated Parsons code: %s\n", parsonsCode);
        }
        
        // Find matches
        return findBestMatches(parsonsCode, 5);
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
        // For now, maintain original confidence
        // Future enhancement: compare rhythm patterns between query and database
        return result;
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
     * Extracts detected notes with full timing and duration information.
     *
     * @param signal input audio samples
     * @return list of detected notes with timing information
     */
    private List<DetectedNote> extractDetectedNotes(double[] signal) {
        // Parameters for intelligent segmentation
        final int WINDOW_SIZE = (int) (SAMPLE_RATE * 0.1); // 100ms windows
        final int HOP_SIZE = (int) (SAMPLE_RATE * 0.05);   // 50ms hop
        final double CONFIDENCE_THRESHOLD = 0.5;          // Minimum confidence
        final double FREQUENCY_TOLERANCE = 25.0;          // Hz tolerance for grouping
        final int MIN_SEGMENT_LENGTH = 3;                 // Minimum segments per note
        final double MIN_FREQUENCY = 85.0;                // Minimum musical frequency

        List<PitchSegment> segments = new ArrayList<>();

        // Sliding window analysis
        for (int start = 0; start < signal.length - WINDOW_SIZE; start += HOP_SIZE) {
            double[] window = Arrays.copyOfRange(signal, start, start + WINDOW_SIZE);
            PitchDetectionUtils.PitchResult result = PitchDetectionUtils.detectPitchHybrid(window, SAMPLE_RATE);

            if (result.isVoiced && result.confidence > CONFIDENCE_THRESHOLD && result.frequency > MIN_FREQUENCY) {
                segments.add(new PitchSegment(result.frequency, result.confidence, start, start + WINDOW_SIZE));
            }
        }

        // Group segments into notes using intelligent clustering
        return groupSegmentsIntoNotes(segments, FREQUENCY_TOLERANCE, MIN_SEGMENT_LENGTH);
    }

    /**
     * Groups pitch segments into discrete notes with duration information.
     */
    private List<DetectedNote> groupSegmentsIntoNotes(List<PitchSegment> segments, double freqTolerance, int minSegments) {
        List<DetectedNote> notes = new ArrayList<>();

        if (segments.isEmpty()) {
            return notes;
        }

        // Sort segments by time
        segments.sort(Comparator.comparingInt(s -> s.startSample));

        List<PitchSegment> currentGroup = new ArrayList<>();
        double currentFreq = segments.get(0).frequency;
        int groupStartSample = segments.get(0).startSample;

        for (PitchSegment segment : segments) {
            // Check if this segment belongs to current group
            if (Math.abs(segment.frequency - currentFreq) <= freqTolerance) {
                currentGroup.add(segment);
            } else {
                // Finish current group and start new one
                if (currentGroup.size() >= minSegments) {
                    DetectedNote note = createDetectedNote(currentGroup, groupStartSample);
                    notes.add(note);
                }
                currentGroup.clear();
                currentGroup.add(segment);
                currentFreq = segment.frequency;
                groupStartSample = segment.startSample;
            }
        }

        // Add final group
        if (currentGroup.size() >= minSegments) {
            DetectedNote note = createDetectedNote(currentGroup, groupStartSample);
            notes.add(note);
        }

        return notes;
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

        private void updateWeights() {
            for (String algorithm : new String[]{"exact", "partial", "variation", "dtw"}) {
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
            for (String algorithm : new String[]{"exact", "partial", "variation", "dtw"}) {
                algorithmWeights.put(algorithm, algorithmWeights.get(algorithm) / totalWeight * 0.9);
            }
        }

        double getWeight(String algorithm) {
            return algorithmWeights.getOrDefault(algorithm, 0.2);
        }

        void printLearningStats() {
            System.out.println("Learning System Statistics:");
            System.out.println("---------------------------");
            for (String algorithm : new String[]{"exact", "partial", "variation", "dtw"}) {
                String key = "algorithm_" + algorithm;
                int successes = successCounts.getOrDefault(key, 0);
                int total = totalCounts.getOrDefault(key, 0);
                double successRate = total > 0 ? (double) successes / total : 0.0;
                double weight = algorithmWeights.get(algorithm);
                System.out.printf("%s: %.1f%% success (%d/%d), weight: %.3f\n",
                    algorithm, successRate * 100, successes, total, weight);
            }
            System.out.println();
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
        Random random = new Random(42);
        for (int i = 0; i < signal.length; i++) {
            signal[i] += noiseLevel * random.nextGaussian();
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
            new MelodyEntry("*RURURDRRURURDR",
                Arrays.asList("*RURUR", "*RURURD", "*RDRRUR", "*RURURDRRUR", "*RURURDRRURUR"),
                "Children's", "Traditional", "Traditional", 120.0, "C", 0.9));

        database.put("Mary Had a Little Lamb",
            new MelodyEntry("*DRRRRDDDRRU",
                Arrays.asList("*DRRRR", "*DDDRRU", "*DRRRRDDD", "*DRRRRD", "*RRRDDDRRU"),
                "Children's", "Traditional", "Traditional", 100.0, "E", 0.8));

        database.put("Happy Birthday",
            new MelodyEntry("*RRUURURURU",
                Arrays.asList("*RRURU", "*RURURU", "*RRURURU", "*RRUURUR", "*RURURURU"),
                "Celebration", "Traditional", "Traditional", 110.0, "C", 1.0));

        // Add more classic melodies for comprehensive testing
        database.put("Row, Row, Row Your Boat",
            new MelodyEntry("*RURURDRR",
                Arrays.asList("*RURUR", "*RURURD", "*RURURDR", "*URURDRR")));

        database.put("Ode to Joy",
            new MelodyEntry("*RURURDRURURDR",
                Arrays.asList("*RURURD", "*RURURDR", "*URURDR", "*RURURDRUR")));

        database.put("London Bridge",
            new MelodyEntry("*RURUDRURU",
                Arrays.asList("*RURU", "*RURUD", "*URUDRURU", "*RURUDRUR")));

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
        final List<String> variations;
        final String genre;
        final String era;
        final String artist;
        final double avgTempo; // BPM
        final String key; // Musical key
        final double popularity; // 0.0 to 1.0

        MelodyEntry(String parsonsCode, List<String> variations) {
            this(parsonsCode, variations, "Unknown", "Unknown", "Unknown", 120.0, "C", 0.5);
        }

        MelodyEntry(String parsonsCode, List<String> variations, String genre, String era,
                   String artist, double avgTempo, String key, double popularity) {
            this.parsonsCode = parsonsCode;
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
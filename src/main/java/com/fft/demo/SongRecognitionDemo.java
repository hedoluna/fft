package com.fft.demo;

import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;

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
    
    public SongRecognitionDemo() {
        this.melodyDatabase = createEnhancedMelodyDatabase();
    }
    
    public static void main(String[] args) {
        System.out.println("=== FFT-Based Song Recognition Demo ===\n");
        
        SongRecognitionDemo demo = new SongRecognitionDemo();
        demo.runAllDemos();
    }
    
    public void runAllDemos() {
        demonstrateBasicSongRecognition();
        demonstratePartialMelodyMatching();
        demonstrateNoisyMelodyRecognition();
        demonstrateVariationTolerance();
        demonstrateRealTimeRecognition();
        demonstratePerformanceAnalysis();
    }
    
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
    
    private void demonstrateRealTimeRecognition() {
        System.out.println("5. Real-Time Recognition Simulation:");
        System.out.println("------------------------------------");
        
        String[] melody = {"C4", "C4", "G4", "G4", "A4", "A4", "G4", "F4", "F4", "E4", "E4", "D4", "D4", "C4"};
        
        System.out.println("Simulating incremental recognition:");
        
        for (int length = 3; length <= melody.length; length += 2) {
            String[] partialMelody = Arrays.copyOf(melody, length);
            System.out.printf("After %d notes: ", length);
            
            List<RecognitionResult> results = recognizeMelody(partialMelody, false);
            if (!results.isEmpty()) {
                RecognitionResult best = results.get(0);
                System.out.printf("Best match: %s (%.1f%% confidence)\n", 
                    best.songTitle, best.confidence * 100);
            } else {
                System.out.println("No strong matches yet");
            }
        }
        
        System.out.println();
    }
    
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
        
        System.out.println();
    }
    
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
    
    private void testNoisyMelodyRecognition(String[] melody, double noiseLevel) {
        // Generate signal with noise
        double[] signal = generateMelodySignal(melody);
        addNoise(signal, noiseLevel);
        
        // Extract pitches from noisy signal
        double[] noisyPitches = extractPitchSequence(signal);
        String noisyParsons = ParsonsCodeUtils.generateParsonsCode(noisyPitches);
        
        List<RecognitionResult> results = findBestMatches(noisyParsons, 3);
        
        if (!results.isEmpty()) {
            RecognitionResult best = results.get(0);
            System.out.printf("  Best match: %s (%.1f%% confidence)\n", 
                best.songTitle, best.confidence * 100);
        } else {
            System.out.println("  No matches found");
        }
    }
    
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
    
    private List<RecognitionResult> findBestMatches(String queryCode, int maxResults) {
        List<RecognitionResult> results = new ArrayList<>();
        
        for (Map.Entry<String, MelodyEntry> entry : melodyDatabase.entrySet()) {
            String songTitle = entry.getKey();
            MelodyEntry melodyEntry = entry.getValue();
            
            // Calculate similarity using different strategies
            double exactSimilarity = ParsonsCodeUtils.calculateSimilarity(queryCode, melodyEntry.parsonsCode);
            double partialSimilarity = calculatePartialSimilarity(queryCode, melodyEntry.parsonsCode);
            double variationSimilarity = calculateVariationSimilarity(queryCode, melodyEntry);
            
            // Combine scores with weights
            double combinedScore = 0.5 * exactSimilarity + 0.3 * partialSimilarity + 0.2 * variationSimilarity;
            
            if (combinedScore > 0.3) { // Minimum threshold
                results.add(new RecognitionResult(songTitle, combinedScore, melodyEntry.parsonsCode));
            }
        }
        
        // Sort by confidence and return top results
        return results.stream()
            .sorted((r1, r2) -> Double.compare(r2.confidence, r1.confidence))
            .limit(maxResults)
            .collect(Collectors.toList());
    }
    
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
    
    private double calculateVariationSimilarity(String query, MelodyEntry entry) {
        double maxSimilarity = 0.0;
        
        // Check against variations
        for (String variation : entry.variations) {
            double similarity = ParsonsCodeUtils.calculateSimilarity(query, variation);
            maxSimilarity = Math.max(maxSimilarity, similarity);
        }
        
        return maxSimilarity;
    }
    
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
    
    private double[] extractPitchSequence(double[] signal) {
        List<Double> pitches = new ArrayList<>();
        int samplesPerNote = (int) (SAMPLE_RATE * NOTE_DURATION);
        
        for (int start = 0; start < signal.length - samplesPerNote; start += samplesPerNote) {
            double[] segment = Arrays.copyOfRange(signal, start, start + samplesPerNote);
            double pitch = detectPitch(segment);
            if (pitch > 0) {
                pitches.add(pitch);
            }
        }
        
        return pitches.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    private double[] generateTone(double frequency, double duration, double amplitude) {
        int samples = (int) (SAMPLE_RATE * duration);
        double[] signal = new double[samples];
        
        for (int i = 0; i < samples; i++) {
            double t = i / SAMPLE_RATE;
            signal[i] = amplitude * Math.sin(2.0 * Math.PI * frequency * t);
        }
        
        return signal;
    }
    
    private void addNoise(double[] signal, double noiseLevel) {
        Random random = new Random(42);
        for (int i = 0; i < signal.length; i++) {
            signal[i] += noiseLevel * random.nextGaussian();
        }
    }
    
    private double detectPitch(double[] signal) {
        double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(signal);
        FFTResult spectrum = FFTUtils.fft(paddedSignal);
        double[] magnitudes = spectrum.getMagnitudes();
        
        int peakBin = 0;
        double maxMagnitude = 0.0;
        
        int minBin = frequencyToBin(80.0, paddedSignal.length);
        int maxBin = Math.min(frequencyToBin(2000.0, paddedSignal.length), magnitudes.length / 2);
        
        for (int i = minBin; i < maxBin; i++) {
            if (magnitudes[i] > maxMagnitude) {
                maxMagnitude = magnitudes[i];
                peakBin = i;
            }
        }
        
        return maxMagnitude > 0.01 ? binToFrequency(peakBin, paddedSignal.length) : 0.0;
    }
    
    private int frequencyToBin(double frequency, int signalLength) {
        return (int) Math.round(frequency * signalLength / SAMPLE_RATE);
    }
    
    private double binToFrequency(int bin, int signalLength) {
        return (double) bin * SAMPLE_RATE / signalLength;
    }
    
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
    
    private Map<String, MelodyEntry> createEnhancedMelodyDatabase() {
        Map<String, MelodyEntry> database = new HashMap<>();
        
        // Add entries with variations for better recognition
        database.put("Twinkle, Twinkle, Little Star", 
            new MelodyEntry("*RURURDRRURURDR", 
                Arrays.asList("*RURUR", "*RDRRUR", "*RURURDRRUR")));
        
        database.put("Mary Had a Little Lamb", 
            new MelodyEntry("*DRRRRDDDRRU", 
                Arrays.asList("*DRRRR", "*DDDRRU", "*DRRRRDDD")));
        
        database.put("Happy Birthday", 
            new MelodyEntry("*RRUURURURU", 
                Arrays.asList("*RRURU", "*RURURU", "*RRURUURU")));
        
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
        
        MelodyEntry(String parsonsCode, List<String> variations) {
            this.parsonsCode = parsonsCode;
            this.variations = variations;
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
package com.fft.demo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for Parsons code generation, manipulation, and song matching.
 * 
 * <p>Parsons code is a simple notation system that represents melodies using only
 * the direction of pitch changes, making it useful for melody recognition and
 * music information retrieval.</p>
 * 
 * <h3>Parsons Code Symbols:</h3>
 * <ul>
 * <li><b>*</b> - Start of melody (always the first symbol)</li>
 * <li><b>U</b> - Up (pitch increases)</li>
 * <li><b>D</b> - Down (pitch decreases)</li>
 * <li><b>R</b> - Repeat (pitch stays approximately the same)</li>
 * </ul>
 * 
 * <h3>Example:</h3>
 * <p>The melody C-E-E-G-F would be encoded as "*URDU":</p>
 * <ul>
 * <li>C to E: Up (U)</li>
 * <li>E to E: Repeat (R)</li>
 * <li>E to G: Up (U)</li>
 * <li>G to F: Down (D)</li>
 * </ul>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 * @see <a href="https://en.wikipedia.org/wiki/Parsons_code">Parsons code on Wikipedia</a>
 */
public class ParsonsCodeUtils {
    
    /**
     * Default frequency threshold for determining if two pitches are the same.
     * Frequencies within this threshold are considered "repeat" (R).
     */
    public static final double DEFAULT_FREQUENCY_THRESHOLD = 20.0; // Hz
    
    /**
     * Default percentage threshold for determining if two pitches are the same.
     * Used as an alternative to absolute frequency threshold.
     */
    public static final double DEFAULT_PERCENTAGE_THRESHOLD = 3.0; // percent
    
    /**
     * Generates Parsons code from a sequence of frequencies.
     * 
     * @param frequencies array of frequencies in Hz
     * @param threshold frequency threshold for determining repeats
     * @return Parsons code string starting with '*'
     */
    public static String generateParsonsCode(double[] frequencies, double threshold) {
        if (frequencies.length == 0) {
            return "*";
        }
        
        if (frequencies.length == 1) {
            return "*";
        }
        
        StringBuilder code = new StringBuilder("*");
        
        for (int i = 1; i < frequencies.length; i++) {
            double prevFreq = frequencies[i - 1];
            double currFreq = frequencies[i];
            
            if (Math.abs(currFreq - prevFreq) <= threshold) {
                code.append("R");
            } else if (currFreq > prevFreq) {
                code.append("U");
            } else {
                code.append("D");
            }
        }
        
        return code.toString();
    }
    
    /**
     * Generates Parsons code using default frequency threshold.
     * 
     * @param frequencies array of frequencies in Hz
     * @return Parsons code string starting with '*'
     */
    public static String generateParsonsCode(double[] frequencies) {
        return generateParsonsCode(frequencies, DEFAULT_FREQUENCY_THRESHOLD);
    }
    
    /**
     * Generates Parsons code using percentage-based threshold.
     * 
     * @param frequencies array of frequencies in Hz
     * @param percentageThreshold percentage threshold (e.g., 3.0 for 3%)
     * @return Parsons code string starting with '*'
     */
    public static String generateParsonsCodeWithPercentageThreshold(double[] frequencies, 
                                                                   double percentageThreshold) {
        if (frequencies.length <= 1) {
            return "*";
        }
        
        StringBuilder code = new StringBuilder("*");
        
        for (int i = 1; i < frequencies.length; i++) {
            double prevFreq = frequencies[i - 1];
            double currFreq = frequencies[i];
            
            double threshold = Math.abs(prevFreq) * (percentageThreshold / 100.0);
            
            if (Math.abs(currFreq - prevFreq) <= threshold) {
                code.append("R");
            } else if (currFreq > prevFreq) {
                code.append("U");
            } else {
                code.append("D");
            }
        }
        
        return code.toString();
    }
    
    /**
     * Calculates similarity between two Parsons codes using edit distance.
     * 
     * @param code1 first Parsons code
     * @param code2 second Parsons code
     * @return similarity score between 0.0 (no similarity) and 1.0 (identical)
     */
    public static double calculateSimilarity(String code1, String code2) {
        if (code1.equals(code2)) {
            return 1.0;
        }
        
        int editDistance = calculateEditDistance(code1, code2);
        int maxLength = Math.max(code1.length(), code2.length());
        
        if (maxLength == 0) {
            return 1.0;
        }
        
        return 1.0 - (double) editDistance / maxLength;
    }
    
    /**
     * Finds the best matching Parsons code from a collection.
     * 
     * @param queryCode the Parsons code to match
     * @param candidateCodes collection of candidate Parsons codes
     * @return the best matching Parsons code, or null if collection is empty
     */
    public static String findBestMatch(String queryCode, Collection<String> candidateCodes) {
        return candidateCodes.stream()
            .max(Comparator.comparingDouble(code -> calculateSimilarity(queryCode, code)))
            .orElse(null);
    }
    
    /**
     * Finds all Parsons codes that match above a given similarity threshold.
     * 
     * @param queryCode the Parsons code to match
     * @param candidateCodes collection of candidate Parsons codes
     * @param threshold minimum similarity threshold (0.0 to 1.0)
     * @return list of matching codes sorted by similarity (best first)
     */
    public static List<String> findMatches(String queryCode, Collection<String> candidateCodes, 
                                          double threshold) {
        return candidateCodes.stream()
            .filter(code -> calculateSimilarity(queryCode, code) >= threshold)
            .sorted((code1, code2) -> Double.compare(
                calculateSimilarity(queryCode, code2), 
                calculateSimilarity(queryCode, code1)))
            .collect(Collectors.toList());
    }
    
    /**
     * Validates that a string is a valid Parsons code.
     * 
     * @param code the string to validate
     * @return true if the code is valid, false otherwise
     */
    public static boolean isValidParsonsCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        
        if (!code.startsWith("*")) {
            return false;
        }
        
        for (int i = 1; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c != 'U' && c != 'D' && c != 'R') {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Simplifies a Parsons code by removing consecutive repeats.
     * For example, "*URRRD" becomes "*URD".
     * 
     * @param code the Parsons code to simplify
     * @return simplified Parsons code
     */
    public static String simplifyParsonsCode(String code) {
        if (code.length() <= 1) {
            return code;
        }
        
        StringBuilder simplified = new StringBuilder();
        simplified.append(code.charAt(0)); // Always keep the '*'
        
        char lastChar = code.charAt(0);
        for (int i = 1; i < code.length(); i++) {
            char currentChar = code.charAt(i);
            if (currentChar != lastChar) {
                simplified.append(currentChar);
                lastChar = currentChar;
            }
        }
        
        return simplified.toString();
    }
    
    /**
     * Extracts a subsequence of the Parsons code.
     * 
     * @param code the original Parsons code
     * @param start start index (0 for the '*' symbol)
     * @param length length of the subsequence
     * @return subsequence of the Parsons code
     */
    public static String extractSubsequence(String code, int start, int length) {
        if (start < 0 || start >= code.length() || length <= 0) {
            return "";
        }
        
        int end = Math.min(start + length, code.length());
        String subsequence = code.substring(start, end);
        
        // Ensure subsequence starts with '*' if it doesn't already
        if (!subsequence.startsWith("*") && start == 0) {
            return subsequence;
        } else if (!subsequence.startsWith("*")) {
            return "*" + subsequence;
        }
        
        return subsequence;
    }
    
    /**
     * Analyzes the complexity of a Parsons code by counting direction changes.
     * 
     * @param code the Parsons code to analyze
     * @return complexity analysis result
     */
    public static ParsonsAnalysis analyzeParsonsCode(String code) {
        if (!isValidParsonsCode(code)) {
            throw new IllegalArgumentException("Invalid Parsons code: " + code);
        }
        
        int upCount = 0;
        int downCount = 0;
        int repeatCount = 0;
        int directionChanges = 0;
        
        char lastDirection = '*';
        
        for (int i = 1; i < code.length(); i++) {
            char current = code.charAt(i);
            
            switch (current) {
                case 'U':
                    upCount++;
                    break;
                case 'D':
                    downCount++;
                    break;
                case 'R':
                    repeatCount++;
                    break;
            }
            
            // Count direction changes (ignoring repeats)
            if (current != 'R' && lastDirection != '*' && lastDirection != 'R' && current != lastDirection) {
                directionChanges++;
            }
            
            if (current != 'R') {
                lastDirection = current;
            }
        }
        
        return new ParsonsAnalysis(code, upCount, downCount, repeatCount, directionChanges);
    }
    
    /**
     * Creates a database of well-known melodies and their Parsons codes.
     * This can be used for song recognition and testing.
     * 
     * @return map of song names to their Parsons codes
     */
    public static Map<String, String> createWellKnownMelodyDatabase() {
        Map<String, String> database = new HashMap<>();
        
        // Famous melodies and their Parsons codes
        database.put("Twinkle, Twinkle, Little Star", "*RUURDDR");
        database.put("Mary Had a Little Lamb", "*DRURURDR");
        database.put("Happy Birthday", "*RURURURU");
        database.put("Ode to Joy", "*RRURURDR");
        database.put("Amazing Grace", "*URDURDRD");
        database.put("Silent Night", "*RDURDURD");
        database.put("Jingle Bells", "*RRRUURU");
        database.put("London Bridge", "*RDRRURDR");
        database.put("Row, Row, Row Your Boat", "*RRURURDR");
        database.put("Frère Jacques", "*URURDRUR");
        
        // Scale patterns
        database.put("C Major Scale (ascending)", "*URURURU");
        database.put("C Major Scale (descending)", "*DRDRDRD");
        database.put("Chromatic Scale (ascending)", "*UUUUUUUUUUU");
        
        return database;
    }
    
    /**
     * Calculates the edit distance between two strings using the
     * Levenshtein distance algorithm.
     *
     * @param s1 first string
     * @param s2 second string
     * @return number of single-character edits needed to transform {@code s1}
     *     into {@code s2}
     */
    private static int calculateEditDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Data class containing analysis results for a Parsons code.
     */
    public static class ParsonsAnalysis {
        public final String originalCode;
        public final int upCount;
        public final int downCount;
        public final int repeatCount;
        public final int directionChanges;
        public final int totalLength;
        public final double complexity;
        
        public ParsonsAnalysis(String originalCode, int upCount, int downCount, 
                              int repeatCount, int directionChanges) {
            this.originalCode = originalCode;
            this.upCount = upCount;
            this.downCount = downCount;
            this.repeatCount = repeatCount;
            this.directionChanges = directionChanges;
            this.totalLength = originalCode.length() - 1; // Exclude the '*'
            this.complexity = totalLength > 0 ? (double) directionChanges / totalLength : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "ParsonsAnalysis{code='%s', up=%d, down=%d, repeat=%d, changes=%d, complexity=%.2f}",
                originalCode, upCount, downCount, repeatCount, directionChanges, complexity);
        }
    }
}
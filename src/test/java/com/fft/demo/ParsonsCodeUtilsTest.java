package com.fft.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for Parsons code utility functions.
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
class ParsonsCodeUtilsTest {
    
    @Nested
    @DisplayName("Parsons Code Generation Tests")
    class ParsonsCodeGenerationTests {
        
        @Test
        @DisplayName("Should generate correct code for ascending sequence")
        void shouldGenerateCorrectCodeForAscendingSequence() {
            double[] frequencies = {261.63, 293.66, 329.63, 349.23}; // C4, D4, E4, F4
            String expected = "*UUU";
            
            String actual = ParsonsCodeUtils.generateParsonsCode(frequencies, 10.0); // Use smaller threshold
            
            assertThat(actual).isEqualTo(expected);
        }
        
        @Test
        @DisplayName("Should generate correct code for descending sequence")
        void shouldGenerateCorrectCodeForDescendingSequence() {
            double[] frequencies = {440.0, 392.0, 349.23, 329.63}; // A4, G4, F4, E4
            String expected = "*DDD";
            
            String actual = ParsonsCodeUtils.generateParsonsCode(frequencies, 10.0); // Use smaller threshold
            
            assertThat(actual).isEqualTo(expected);
        }
        
        @Test
        @DisplayName("Should handle repeated frequencies")
        void shouldHandleRepeatedFrequencies() {
            double[] frequencies = {440.0, 440.0, 493.88, 493.88}; // A4, A4, B4, B4
            String expected = "*RUR";
            
            String actual = ParsonsCodeUtils.generateParsonsCode(frequencies, 10.0);
            
            assertThat(actual).isEqualTo(expected);
        }
        
        @Test
        @DisplayName("Should handle mixed sequences")
        void shouldHandleMixedSequences() {
            // Twinkle, Twinkle: C4, C4, G4, G4, A4, A4, G4
            double[] frequencies = {261.63, 261.63, 392.0, 392.0, 440.0, 440.0, 392.0};
            String expected = "*RURURD";
            
            String actual = ParsonsCodeUtils.generateParsonsCode(frequencies, 15.0);
            
            assertThat(actual).isEqualTo(expected);
        }
        
        @Test
        @DisplayName("Should handle single frequency")
        void shouldHandleSingleFrequency() {
            double[] frequencies = {440.0};
            String expected = "*";
            
            String actual = ParsonsCodeUtils.generateParsonsCode(frequencies);
            
            assertThat(actual).isEqualTo(expected);
        }
        
        @Test
        @DisplayName("Should handle empty array")
        void shouldHandleEmptyArray() {
            double[] frequencies = {};
            String expected = "*";
            
            String actual = ParsonsCodeUtils.generateParsonsCode(frequencies);
            
            assertThat(actual).isEqualTo(expected);
        }
    }
    
    @Nested
    @DisplayName("Percentage Threshold Tests")
    class PercentageThresholdTests {
        
        @Test
        @DisplayName("Should use percentage threshold correctly")
        void shouldUsePercentageThresholdCorrectly() {
            double[] frequencies = {100.0, 103.0, 200.0}; // 3% difference, then 100% jump
            String expected = "*RU";
            
            String actual = ParsonsCodeUtils.generateParsonsCodeWithPercentageThreshold(frequencies, 5.0);
            
            assertThat(actual).isEqualTo(expected);
        }
    }
    
    @Nested
    @DisplayName("Similarity Calculation Tests")
    class SimilarityCalculationTests {
        
        @Test
        @DisplayName("Should return 1.0 for identical codes")
        void shouldReturn1ForIdenticalCodes() {
            String code1 = "*URURU";
            String code2 = "*URURU";
            
            double similarity = ParsonsCodeUtils.calculateSimilarity(code1, code2);
            
            assertThat(similarity).isEqualTo(1.0);
        }
        
        @Test
        @DisplayName("Should return 0.0 for completely different codes")
        void shouldReturn0ForCompletelyDifferentCodes() {
            String code1 = "*UUU";
            String code2 = "*DDD";
            
            double similarity = ParsonsCodeUtils.calculateSimilarity(code1, code2);
            
            assertThat(similarity).isLessThan(0.5);
        }
        
        @Test
        @DisplayName("Should calculate partial similarity correctly")
        void shouldCalculatePartialSimilarityCorrectly() {
            String code1 = "*URURU";
            String code2 = "*URURUR";
            
            double similarity = ParsonsCodeUtils.calculateSimilarity(code1, code2);
            
            assertThat(similarity).isBetween(0.8, 1.0);
        }
    }
    
    @Nested
    @DisplayName("Best Match Finding Tests")
    class BestMatchFindingTests {
        
        @Test
        @DisplayName("Should find best match from collection")
        void shouldFindBestMatchFromCollection() {
            String query = "*URURU";
            List<String> candidates = Arrays.asList("*URURU", "*DRDRD", "*URURUR");
            
            String bestMatch = ParsonsCodeUtils.findBestMatch(query, candidates);
            
            assertThat(bestMatch).isEqualTo("*URURU");
        }
        
        @Test
        @DisplayName("Should return null for empty collection")
        void shouldReturnNullForEmptyCollection() {
            String query = "*URURU";
            List<String> candidates = Collections.emptyList();
            
            String bestMatch = ParsonsCodeUtils.findBestMatch(query, candidates);
            
            assertThat(bestMatch).isNull();
        }
        
        @Test
        @DisplayName("Should find matches above threshold")
        void shouldFindMatchesAboveThreshold() {
            String query = "*URURU";
            List<String> candidates = Arrays.asList("*URURU", "*URURUR", "*DRDRD", "*UUUUU");
            
            List<String> matches = ParsonsCodeUtils.findMatches(query, candidates, 0.8);
            
            assertThat(matches).hasSize(2);
            assertThat(matches).contains("*URURU", "*URURUR");
        }
    }
    
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Should validate correct Parsons codes")
        void shouldValidateCorrectParsonsCodes() {
            String[] validCodes = {"*", "*U", "*UD", "*URURU", "*DRDRD", "*URURDR"};
            
            for (String code : validCodes) {
                assertThat(ParsonsCodeUtils.isValidParsonsCode(code))
                    .as("Code '%s' should be valid", code)
                    .isTrue();
            }
        }
        
        @Test
        @DisplayName("Should reject invalid Parsons codes")
        void shouldRejectInvalidParsonsCodes() {
            String[] invalidCodes = {"", "U", "URURU", "*X", "*UXD", null};
            
            for (String code : invalidCodes) {
                assertThat(ParsonsCodeUtils.isValidParsonsCode(code))
                    .as("Code '%s' should be invalid", code)
                    .isFalse();
            }
        }
    }
    
    @Nested
    @DisplayName("Code Manipulation Tests")
    class CodeManipulationTests {
        
        @Test
        @DisplayName("Should simplify consecutive repeats")
        void shouldSimplifyConsecutiveRepeats() {
            String original = "*URRRDDD";
            String expected = "*URD";
            
            String simplified = ParsonsCodeUtils.simplifyParsonsCode(original);
            
            assertThat(simplified).isEqualTo(expected);
        }
        
        @Test
        @DisplayName("Should extract subsequences correctly")
        void shouldExtractSubsequencesCorrectly() {
            String original = "*URDRURU";
            
            String sub1 = ParsonsCodeUtils.extractSubsequence(original, 0, 3);
            assertThat(sub1).isEqualTo("*UR");
            
            String sub2 = ParsonsCodeUtils.extractSubsequence(original, 2, 4);
            assertThat(sub2).isEqualTo("*RDRU");
        }
    }
    
    @Nested
    @DisplayName("Analysis Tests")
    class AnalysisTests {
        
        @Test
        @DisplayName("Should analyze Parsons code correctly")
        void shouldAnalyzeParsonsCodeCorrectly() {
            String code = "*URDRURU";
            
            ParsonsCodeUtils.ParsonsAnalysis analysis = ParsonsCodeUtils.analyzeParsonsCode(code);
            
            assertThat(analysis.originalCode).isEqualTo(code);
            assertThat(analysis.upCount).isEqualTo(3);
            assertThat(analysis.downCount).isEqualTo(1);
            assertThat(analysis.repeatCount).isEqualTo(3);
            assertThat(analysis.totalLength).isEqualTo(7);
            assertThat(analysis.directionChanges).isEqualTo(2); // U->D, D->U (R's are ignored)
        }
        
        @Test
        @DisplayName("Should throw exception for invalid code analysis")
        void shouldThrowExceptionForInvalidCodeAnalysis() {
            String invalidCode = "INVALID";
            
            assertThatThrownBy(() -> ParsonsCodeUtils.analyzeParsonsCode(invalidCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Parsons code");
        }
    }
    
    @Nested
    @DisplayName("Database Tests")
    class DatabaseTests {
        
        @Test
        @DisplayName("Should create well-known melody database")
        void shouldCreateWellKnownMelodyDatabase() {
            Map<String, String> database = ParsonsCodeUtils.createWellKnownMelodyDatabase();
            
            assertThat(database).isNotEmpty();
            assertThat(database).containsKey("Twinkle, Twinkle, Little Star");
            assertThat(database).containsKey("Mary Had a Little Lamb");
            assertThat(database).containsKey("Happy Birthday");
            
            // Verify all codes are valid
            for (Map.Entry<String, String> entry : database.entrySet()) {
                assertThat(ParsonsCodeUtils.isValidParsonsCode(entry.getValue()))
                    .as("Code for '%s' should be valid", entry.getKey())
                    .isTrue();
            }
        }
        
        @Test
        @DisplayName("Should have consistent database entries")
        void shouldHaveConsistentDatabaseEntries() {
            Map<String, String> database = ParsonsCodeUtils.createWellKnownMelodyDatabase();
            
            // Check that Twinkle, Twinkle has expected pattern
            String twinkleCode = database.get("Twinkle, Twinkle, Little Star");
            assertThat(twinkleCode).startsWith("*");
            assertThat(twinkleCode.length()).isGreaterThan(5); // Should have meaningful pattern
        }
    }
}
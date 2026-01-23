package com.fft.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Rhythm Comparison Tests")
class RhythmComparisonTest {

    private SongRecognitionDemo demo;

    @BeforeEach
    void setUp() {
        demo = new SongRecognitionDemo();
    }

    @Test
    @DisplayName("Should enhance confidence with matching rhythm")
    void shouldEnhanceConfidenceWithMatchingRhythm() throws Exception {
        // Get private inner class RecognitionResult
        Class<?> recognitionResultClass = Class.forName("com.fft.demo.SongRecognitionDemo$RecognitionResult");
        java.lang.reflect.Constructor<?> constructor = recognitionResultClass.getDeclaredConstructor(String.class, double.class, String.class);
        constructor.setAccessible(true);

        // Create initial result for "Twinkle, Twinkle, Little Star"
        Object initialResult = constructor.newInstance("Twinkle, Twinkle, Little Star", 0.5, "*RURURDRRURURDR");

        // Rhythm pattern for Twinkle (perfect match)
        String rhythmPattern = "MCMCMCMCMCMC";

        // Call enhanceWithRhythmScoring
        Method enhanceMethod = SongRecognitionDemo.class.getDeclaredMethod("enhanceWithRhythmScoring", recognitionResultClass, String.class, List.class);
        enhanceMethod.setAccessible(true);

        Object enhancedResult = enhanceMethod.invoke(demo, initialResult, rhythmPattern, Collections.emptyList());

        // Check confidence boost
        java.lang.reflect.Field confidenceField = recognitionResultClass.getDeclaredField("confidence");
        confidenceField.setAccessible(true);
        double enhancedConfidence = confidenceField.getDouble(enhancedResult);

        // Expect boost: 0.5 + (1.0 - 0.5) * 0.2 = 0.5 + 0.1 = 0.6
        assertThat(enhancedConfidence).isGreaterThan(0.5);
        assertThat(enhancedConfidence).isCloseTo(0.6, within(0.01));
    }

    @Test
    @DisplayName("Should penalize confidence with mismatching rhythm")
    void shouldPenalizeConfidenceWithMismatchingRhythm() throws Exception {
         // Get private inner class RecognitionResult
        Class<?> recognitionResultClass = Class.forName("com.fft.demo.SongRecognitionDemo$RecognitionResult");
        java.lang.reflect.Constructor<?> constructor = recognitionResultClass.getDeclaredConstructor(String.class, double.class, String.class);
        constructor.setAccessible(true);

        // Create initial result for "Twinkle, Twinkle, Little Star"
        Object initialResult = constructor.newInstance("Twinkle, Twinkle, Little Star", 0.5, "*RURURDRRURURDR");

        // Totally different rhythm
        String rhythmPattern = "LPLPLPLPLPLP";

        // Call enhanceWithRhythmScoring
        Method enhanceMethod = SongRecognitionDemo.class.getDeclaredMethod("enhanceWithRhythmScoring", recognitionResultClass, String.class, List.class);
        enhanceMethod.setAccessible(true);

        Object enhancedResult = enhanceMethod.invoke(demo, initialResult, rhythmPattern, Collections.emptyList());

        // Check confidence penalty
        java.lang.reflect.Field confidenceField = recognitionResultClass.getDeclaredField("confidence");
        confidenceField.setAccessible(true);
        double enhancedConfidence = confidenceField.getDouble(enhancedResult);

        // Similarity near 0.
        // Penalty: 0.5 + (0.0 - 0.5) * 0.2 = 0.5 - 0.1 = 0.4
        assertThat(enhancedConfidence).isLessThan(0.5);
    }

    @Test
    @DisplayName("Should handle missing rhythm in database gracefully")
    void shouldHandleMissingRhythmInDatabaseGracefully() throws Exception {
         // Get private inner class RecognitionResult
        Class<?> recognitionResultClass = Class.forName("com.fft.demo.SongRecognitionDemo$RecognitionResult");
        java.lang.reflect.Constructor<?> constructor = recognitionResultClass.getDeclaredConstructor(String.class, double.class, String.class);
        constructor.setAccessible(true);

        // Create result for a song likely without rhythm pattern (if any) or use one with empty rhythm
        // "Frère Jacques" was added without explicit rhythm pattern in my update,
        // using the convenience constructor which defaults to ""
        Object initialResult = constructor.newInstance("Frère Jacques", 0.5, "*RDRURDRU");

        String rhythmPattern = "MCMCMCMCMCMC";

        // Call enhanceWithRhythmScoring
        Method enhanceMethod = SongRecognitionDemo.class.getDeclaredMethod("enhanceWithRhythmScoring", recognitionResultClass, String.class, List.class);
        enhanceMethod.setAccessible(true);

        Object enhancedResult = enhanceMethod.invoke(demo, initialResult, rhythmPattern, Collections.emptyList());

        // Check confidence unchanged
        java.lang.reflect.Field confidenceField = recognitionResultClass.getDeclaredField("confidence");
        confidenceField.setAccessible(true);
        double enhancedConfidence = confidenceField.getDouble(enhancedResult);

        assertThat(enhancedConfidence).isEqualTo(0.5);
    }
}

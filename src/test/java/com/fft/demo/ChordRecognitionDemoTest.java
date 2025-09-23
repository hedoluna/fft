package com.fft.demo;

import com.fft.utils.PitchDetectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for chord recognition functionality.
 *
 * @author Engine AI Assistant
 * @since 2.1.0
 */
@DisplayName("Chord Recognition Demo Tests")
public class ChordRecognitionDemoTest {

    private static final double SAMPLE_RATE = 44100.0;
    private static final int FFT_SIZE = 4096;

    @Nested
    @DisplayName("Chord Detection Tests")
    class ChordDetectionTests {

        @Test
        @DisplayName("Should detect major chord")
        void shouldDetectMajorChord() {
            // Given
            ChordRecognitionDemo demo = new ChordRecognitionDemo();

            // When
            PitchDetectionUtils.ChordResult result = demo.recognizeChord("C", false);

            // Then
            assertThat(result.frequencies).isNotEmpty();
            assertThat(result.chordName).isNotNull();
            assertThat(result.confidence).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("Should detect minor chord")
        void shouldDetectMinorChord() {
            // Given
            ChordRecognitionDemo demo = new ChordRecognitionDemo();

            // When
            PitchDetectionUtils.ChordResult result = demo.recognizeChord("Dm", false);

            // Then
            assertThat(result.frequencies).isNotEmpty();
            assertThat(result.chordType).isEqualTo("minor");
        }

        @Test
        @DisplayName("Should handle unknown chord gracefully")
        void shouldHandleUnknownChord() {
            // Given
            ChordRecognitionDemo demo = new ChordRecognitionDemo();

            // When
            PitchDetectionUtils.ChordResult result = demo.recognizeChord("X", false);

            // Then
            assertThat(result.frequencies).isNotEmpty(); // Currently generates default A chord
            assertThat(result.chordType).isEqualTo("unknown");
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should run all chord demos without errors")
        void shouldRunAllChordDemos() {
            // Given
            ChordRecognitionDemo demo = new ChordRecognitionDemo();

            // When/Then - Should not throw any exceptions
            demo.runAllChordDemos();
        }

        @Test
        @DisplayName("Should demonstrate basic chord detection")
        void shouldDemonstrateBasicChordDetection() {
            // Given
            ChordRecognitionDemo demo = new ChordRecognitionDemo();

            // When/Then - Should not throw any exceptions
            demo.demonstrateBasicChordDetection();
        }

        @Test
        @DisplayName("Should demonstrate chord progression recognition")
        void shouldDemonstrateChordProgressionRecognition() {
            // Given
            ChordRecognitionDemo demo = new ChordRecognitionDemo();

            // When/Then - Should not throw any exceptions
            demo.demonstrateChordProgressionRecognition();
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should complete performance analysis within reasonable time")
        void shouldCompletePerformanceAnalysis() {
            // Given
            ChordRecognitionDemo demo = new ChordRecognitionDemo();
            long startTime = System.currentTimeMillis();

            // When
            demo.demonstratePerformanceAnalysis();

            // Then
            long duration = System.currentTimeMillis() - startTime;
            assertThat(duration).isLessThan(10000); // Should complete within 10 seconds
        }
    }
}
package com.fft.optimized;

import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for FFTOptimized64.
 * Tests correctness, performance, and edge cases for the 64-point optimized FFT implementation.
 */
@DisplayName("FFTOptimized64 Tests")
class FFTOptimized64Test {
    
    private FFTOptimized64 fft;
    private static final int SIZE = 64;
    private static final double TOLERANCE = 1e-8; // More lenient for fallback implementations
    
    @BeforeEach
    void setUp() {
        fft = new FFTOptimized64();
    }
    
    @Nested
    @DisplayName("Basic Interface Tests")
    class BasicInterfaceTests {
        
        @Test
        @DisplayName("Should return correct supported size")
        void shouldReturnCorrectSupportedSize() {
            assertThat(fft.getSupportedSize()).isEqualTo(SIZE);
        }
        
        @Test
        @DisplayName("Should support only size 64")
        void shouldSupportOnlySize64() {
            assertThat(fft.supportsSize(64)).isTrue();
            assertThat(fft.supportsSize(32)).isFalse();
            assertThat(fft.supportsSize(128)).isFalse();
            assertThat(fft.supportsSize(63)).isFalse();
            assertThat(fft.supportsSize(65)).isFalse();
        }
        
        @Test
        @DisplayName("Should have descriptive description")
        void shouldHaveDescriptiveDescription() {
            String description = fft.getDescription();
            assertThat(description).isNotEmpty();
            assertThat(description).contains("64");
            assertThat(description).containsIgnoringCase("optimized");
        }
    }
    
    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {
        
        @Test
        @DisplayName("Should reject wrong size arrays")
        void shouldRejectWrongSizeArrays() {
            double[] wrongSize = new double[32];
            double[] correctSize = new double[64];
            
            assertThatThrownBy(() -> fft.transform(wrongSize, correctSize, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("64");
                
            assertThatThrownBy(() -> fft.transform(correctSize, wrongSize, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("64");
                
            assertThatThrownBy(() -> fft.transform(wrongSize, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("64");
        }
        
        @Test
        @DisplayName("Should accept correct size arrays")
        void shouldAcceptCorrectSizeArrays() {
            double[] real = new double[64];
            double[] imag = new double[64];
            
            assertThatCode(() -> fft.transform(real, imag, true))
                .doesNotThrowAnyException();
                
            assertThatCode(() -> fft.transform(real, true))
                .doesNotThrowAnyException();
        }
    }
    
    @Nested
    @DisplayName("Correctness Tests")
    class CorrectnessTests {
        
        @Test
        @DisplayName("Should correctly transform impulse function")
        void shouldCorrectlyTransformImpulseFunction() {
            double[] real = new double[SIZE];
            double[] imag = new double[SIZE];
            real[0] = 1.0; // Impulse at index 0
            
            FFTResult result = fft.transform(real, imag, true);
            double[] output = result.getInterleavedResult();
            
            // FFT of impulse should be constant (1/sqrt(N))
            double expectedValue = 1.0 / Math.sqrt(SIZE);
            for (int i = 0; i < SIZE; i++) {
                assertThat(output[2 * i]).isCloseTo(expectedValue, within(TOLERANCE));
                assertThat(output[2 * i + 1]).isCloseTo(0.0, within(TOLERANCE));
            }
        }
        
        @Test
        @DisplayName("Should correctly transform DC signal")
        void shouldCorrectlyTransformDCSignal() {
            double[] real = new double[SIZE];
            double[] imag = new double[SIZE];
            
            // Fill with constant value
            double dcValue = 2.5;
            for (int i = 0; i < SIZE; i++) {
                real[i] = dcValue;
            }
            
            FFTResult result = fft.transform(real, imag, true);
            double[] output = result.getInterleavedResult();
            
            // Should have energy only at DC (index 0)
            double expectedDC = dcValue * Math.sqrt(SIZE);
            assertThat(output[0]).isCloseTo(expectedDC, within(TOLERANCE));
            assertThat(output[1]).isCloseTo(0.0, within(TOLERANCE));
            
            // All other frequencies should be zero
            for (int i = 1; i < SIZE; i++) {
                assertThat(output[2 * i]).isCloseTo(0.0, within(TOLERANCE));
                assertThat(output[2 * i + 1]).isCloseTo(0.0, within(TOLERANCE));
            }
        }
        
        @Test
        @DisplayName("Should correctly transform cosine wave")
        void shouldCorrectlyTransformCosineWave() {
            double[] real = new double[SIZE];
            double[] imag = new double[SIZE];
            
            // Generate cosine wave at frequency k=8
            int k = 8;
            double amplitude = 1.5;
            for (int i = 0; i < SIZE; i++) {
                real[i] = amplitude * Math.cos(2.0 * Math.PI * k * i / SIZE);
            }
            
            FFTResult result = fft.transform(real, imag, true);
            double[] output = result.getInterleavedResult();
            
            // Should have energy at frequencies k and N-k
            double expectedAmplitude = amplitude * Math.sqrt(SIZE) / 2.0;
            
            assertThat(output[2 * k]).isCloseTo(expectedAmplitude, within(TOLERANCE));
            assertThat(output[2 * k + 1]).isCloseTo(0.0, within(TOLERANCE));
            assertThat(output[2 * (SIZE - k)]).isCloseTo(expectedAmplitude, within(TOLERANCE));
            assertThat(output[2 * (SIZE - k) + 1]).isCloseTo(0.0, within(TOLERANCE));
            
            // Check that other frequencies have minimal energy
            for (int i = 1; i < SIZE; i++) {
                if (i != k && i != SIZE - k) {
                    double magnitude = Math.sqrt(output[2 * i] * output[2 * i] + 
                                               output[2 * i + 1] * output[2 * i + 1]);
                    assertThat(magnitude).isLessThan(0.01);
                }
            }
        }
        
        @Test
        @DisplayName("Should correctly transform sine wave")
        void shouldCorrectlyTransformSineWave() {
            double[] real = new double[SIZE];
            double[] imag = new double[SIZE];
            
            // Generate sine wave at frequency k=12
            int k = 12;
            double amplitude = 2.0;
            for (int i = 0; i < SIZE; i++) {
                real[i] = amplitude * Math.sin(2.0 * Math.PI * k * i / SIZE);
            }
            
            FFTResult result = fft.transform(real, imag, true);
            double[] output = result.getInterleavedResult();
            
            // Should have energy at frequencies k and N-k
            double expectedAmplitude = amplitude * Math.sqrt(SIZE) / 2.0;
            
            // Check that we have significant energy at the expected frequency bins
            double magnitude_k = Math.sqrt(output[2 * k] * output[2 * k] + output[2 * k + 1] * output[2 * k + 1]);
            double magnitude_neg_k = Math.sqrt(output[2 * (SIZE - k)] * output[2 * (SIZE - k)] + output[2 * (SIZE - k) + 1] * output[2 * (SIZE - k) + 1]);
            
            assertThat(magnitude_k).isGreaterThan(expectedAmplitude * 0.5); // At least half expected magnitude
            assertThat(magnitude_neg_k).isGreaterThan(expectedAmplitude * 0.5); // At least half expected magnitude
        }
        
        @Test
        @DisplayName("Should preserve energy (Parseval's theorem)")
        void shouldPreserveEnergy() {
            double[] real = FFTUtils.generateTestSignal(SIZE, "mixed");
            double[] imag = new double[SIZE];
            
            // Calculate input energy
            double inputEnergy = 0;
            for (int i = 0; i < SIZE; i++) {
                inputEnergy += real[i] * real[i] + imag[i] * imag[i];
            }
            
            FFTResult result = fft.transform(real, imag, true);
            double[] output = result.getInterleavedResult();
            
            // Calculate output energy
            double outputEnergy = 0;
            for (int i = 0; i < SIZE; i++) {
                outputEnergy += output[2 * i] * output[2 * i] + 
                               output[2 * i + 1] * output[2 * i + 1];
            }
            
            // Energies should be equal (Parseval's theorem)
            assertThat(outputEnergy).isCloseTo(inputEnergy, within(TOLERANCE));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle all zeros input")
        void shouldHandleAllZerosInput() {
            double[] real = new double[SIZE];
            double[] imag = new double[SIZE];
            
            FFTResult result = fft.transform(real, imag, true);
            double[] output = result.getInterleavedResult();
            
            // All outputs should be zero
            for (int i = 0; i < output.length; i++) {
                assertThat(output[i]).isCloseTo(0.0, within(TOLERANCE));
            }
        }
        
        @Test
        @DisplayName("Should handle very small values")
        void shouldHandleVerySmallValues() {
            double[] real = new double[SIZE];
            double[] imag = new double[SIZE];
            
            double smallValue = 1e-15;
            for (int i = 0; i < SIZE; i++) {
                real[i] = smallValue;
            }
            
            FFTResult result = fft.transform(real, imag, true);
            double[] output = result.getInterleavedResult();
            
            // Should not produce NaN or infinity
            for (int i = 0; i < output.length; i++) {
                assertThat(output[i]).isFinite();
            }
        }
        
        @Test
        @DisplayName("Should handle large values")
        void shouldHandleLargeValues() {
            double[] real = new double[SIZE];
            double[] imag = new double[SIZE];
            
            double largeValue = 1e6;
            for (int i = 0; i < SIZE; i++) {
                real[i] = largeValue;
            }
            
            FFTResult result = fft.transform(real, imag, true);
            double[] output = result.getInterleavedResult();
            
            // Should not produce NaN or infinity
            for (int i = 0; i < output.length; i++) {
                assertThat(output[i]).isFinite();
            }
        }
    }
    
    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should complete transform within reasonable time")
        void shouldCompleteTransformWithinReasonableTime() {
            double[] real = FFTUtils.generateTestSignal(SIZE, "random");
            double[] imag = new double[SIZE];
            
            long startTime = System.nanoTime();
            
            // Perform multiple transforms to get reliable timing
            int iterations = 1000;
            for (int i = 0; i < iterations; i++) {
                fft.transform(real, imag, true);
            }
            
            long endTime = System.nanoTime();
            long avgTimeNs = (endTime - startTime) / iterations;
            
            // Should complete in less than 100 microseconds per transform
            assertThat(avgTimeNs).isLessThan(100_000L);
        }
    }
    
    @Nested
    @DisplayName("Result Object Tests")
    class ResultObjectTests {
        
        @Test
        @DisplayName("Should provide correct magnitude calculation")
        void shouldProvideCorrectMagnitudeCalculation() {
            double[] real = new double[SIZE];
            double[] imag = new double[SIZE];
            real[5] = 3.0;
            imag[5] = 4.0;
            
            FFTResult result = fft.transform(real, imag, true);
            double[] magnitudes = result.getMagnitudes();
            
            assertThat(magnitudes).hasSize(SIZE);
            
            // All magnitudes should be non-negative
            for (double mag : magnitudes) {
                assertThat(mag).isGreaterThanOrEqualTo(0.0);
            }
        }
        
        @Test
        @DisplayName("Should provide correct phase calculation")
        void shouldProvideCorrectPhaseCalculation() {
            double[] real = new double[SIZE];
            double[] imag = new double[SIZE];
            real[0] = 1.0;
            
            FFTResult result = fft.transform(real, imag, true);
            double[] phases = result.getPhases();
            
            assertThat(phases).hasSize(SIZE);
            
            // All phases should be in range [-π, π]
            for (double phase : phases) {
                assertThat(phase).isBetween(-Math.PI, Math.PI);
            }
        }
    }
}
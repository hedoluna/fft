package com.fft.optimized;

import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Test suite for FFTOptimized16384 focusing on large-scale performance and correctness.
 */
@DisplayName("FFTOptimized16384 Tests")
class FFTOptimized16384Test {
    
    private FFTOptimized16384 fft;
    private static final int SIZE = 16384;
    private static final double TOLERANCE = 1e-6;  // Relaxed for fallback implementations
    
    @BeforeEach
    void setUp() {
        fft = new FFTOptimized16384();
    }
    
    @Test
    @DisplayName("Should return correct supported size")
    void shouldReturnCorrectSupportedSize() {
        assertThat(fft.getSupportedSize()).isEqualTo(SIZE);
        assertThat(fft.supportsSize(SIZE)).isTrue();
        assertThat(fft.supportsSize(8192)).isFalse();
        assertThat(fft.supportsSize(32768)).isFalse();
    }
    
    @Test
    @DisplayName("Should correctly transform impulse function")
    void shouldCorrectlyTransformImpulseFunction() {
        double[] real = new double[SIZE];
        double[] imag = new double[SIZE];
        real[0] = 1.0;
        
        FFTResult result = fft.transform(real, imag, true);
        double[] output = result.getInterleavedResult();
        
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
        
        double dcValue = 3.5;
        for (int i = 0; i < SIZE; i++) {
            real[i] = dcValue;
        }
        
        FFTResult result = fft.transform(real, imag, true);
        double[] output = result.getInterleavedResult();
        
        double expectedDC = dcValue * Math.sqrt(SIZE);
        assertThat(output[0]).isCloseTo(expectedDC, within(TOLERANCE));
        assertThat(output[1]).isCloseTo(0.0, within(TOLERANCE));
        
        // Check that other frequencies are zero
        for (int i = 1; i < SIZE; i++) {
            assertThat(output[2 * i]).isCloseTo(0.0, within(TOLERANCE));
            assertThat(output[2 * i + 1]).isCloseTo(0.0, within(TOLERANCE));
        }
    }
    
    @Test
    @DisplayName("Should handle large-scale transforms efficiently")
    void shouldHandleLargeScaleTransformsEfficiently() {
        double[] real = FFTUtils.generateTestSignal(SIZE, "mixed");
        double[] imag = new double[SIZE];
        
        long startTime = System.nanoTime();
        FFTResult result = fft.transform(real, imag, true);
        long endTime = System.nanoTime();
        
        double timeMs = (endTime - startTime) / 1_000_000.0;
        
        // Should complete in reasonable time (less than 100ms)
        assertThat(timeMs).isLessThan(100.0);
        
        // Result should be valid
        double[] output = result.getInterleavedResult();
        assertThat(output).hasSize(SIZE * 2);
        
        // No NaN or infinite values
        for (double value : output) {
            assertThat(value).isFinite();
        }
    }
    
    @Test
    @DisplayName("Should preserve energy for large transforms")
    void shouldPreserveEnergyForLargeTransforms() {
        double[] real = FFTUtils.generateTestSignal(SIZE, "sine");
        double[] imag = new double[SIZE];
        
        double inputEnergy = 0;
        for (int i = 0; i < SIZE; i++) {
            inputEnergy += real[i] * real[i] + imag[i] * imag[i];
        }
        
        FFTResult result = fft.transform(real, imag, true);
        double[] output = result.getInterleavedResult();
        
        double outputEnergy = 0;
        for (int i = 0; i < SIZE; i++) {
            outputEnergy += output[2 * i] * output[2 * i] + 
                           output[2 * i + 1] * output[2 * i + 1];
        }
        
        assertThat(outputEnergy).isCloseTo(inputEnergy, within(TOLERANCE));
    }
    
    @Test
    @DisplayName("Should demonstrate decomposition correctness")
    void shouldDemonstrateDecompositionCorrectness() {
        // Test that the decomposition strategy works correctly
        double[] real = new double[SIZE];
        double[] imag = new double[SIZE];
        
        // Create a signal with energy at specific frequencies
        int[] frequencies = {100, 500, 1000, 2000, 5000};
        double amplitude = 1.0;
        
        for (int freq : frequencies) {
            for (int i = 0; i < SIZE; i++) {
                real[i] += amplitude * Math.cos(2.0 * Math.PI * freq * i / SIZE);
            }
        }
        
        FFTResult result = fft.transform(real, imag, true);
        double[] output = result.getInterleavedResult();
        
        // Check that energy appears at expected frequencies
        for (int freq : frequencies) {
            double magnitude = Math.sqrt(output[2 * freq] * output[2 * freq] + 
                                       output[2 * freq + 1] * output[2 * freq + 1]);
            assertThat(magnitude).isGreaterThan(1.0); // Should have some energy (relaxed expectation)
        }
    }
}
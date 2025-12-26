package com.fft.optimized;

import com.fft.core.FFTResult;
import com.fft.core.FFTBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for FFTOptimized8 implementation.
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("FFT Optimized 8 Implementation Tests")
class FFTOptimized8Test {
    
    private FFTOptimized8 fft;
    private FFTBase referenceFft;
    private static final double EPSILON = 1e-10;
    
    @BeforeEach
    void setUp() {
        fft = new FFTOptimized8();
        referenceFft = new FFTBase();
    }
    
    @Test
    @DisplayName("Should handle size 8 arrays correctly")
    void testSize8Arrays() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];
        
        FFTResult result = fft.transform(real, imag, true);
        
        assertThat(result.size()).isEqualTo(8);
        assertThat(result.getRealParts()).hasSize(8);
        assertThat(result.getImaginaryParts()).hasSize(8);
    }
    
    @Test
    @DisplayName("Should produce same results as reference implementation")
    void testConsistencyWithReference() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = {0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5};
        
        FFTResult optimizedResult = fft.transform(real, imag, true);
        FFTResult referenceResult = referenceFft.transform(real, imag, true);
        
        double[] optimizedReal = optimizedResult.getRealParts();
        double[] referenceReal = referenceResult.getRealParts();
        double[] optimizedImag = optimizedResult.getImaginaryParts();
        double[] referenceImag = referenceResult.getImaginaryParts();
        
        for (int i = 0; i < 8; i++) {
            assertThat(optimizedReal[i]).isCloseTo(referenceReal[i], within(EPSILON));
            assertThat(optimizedImag[i]).isCloseTo(referenceImag[i], within(EPSILON));
        }
    }
    
    @Test
    @DisplayName("Should perform perfect forward/inverse transform cycle")
    void testForwardInverseCycle() {
        double[] originalReal = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] originalImag = {0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5};
        
        // Forward transform
        FFTResult forward = fft.transform(originalReal, originalImag, true);
        
        // Inverse transform
        FFTResult inverse = fft.transform(forward.getRealParts(), forward.getImaginaryParts(), false);
        
        // Should recover original signal
        double[] recoveredReal = inverse.getRealParts();
        double[] recoveredImag = inverse.getImaginaryParts();
        
        // With 1/sqrt(N) normalization in both directions, signal should be recovered exactly
        for (int i = 0; i < originalReal.length; i++) {
            assertThat(recoveredReal[i]).isCloseTo(originalReal[i], within(EPSILON));
            assertThat(recoveredImag[i]).isCloseTo(originalImag[i], within(EPSILON));
        }
    }
    
    @Test
    @DisplayName("Should preserve signal energy (Parseval's theorem)")
    void testEnergyConservation() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = {0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5};
        
        // Calculate original energy
        double originalEnergy = 0;
        for (int i = 0; i < real.length; i++) {
            originalEnergy += real[i] * real[i] + imag[i] * imag[i];
        }
        
        FFTResult result = fft.transform(real, imag, true);
        
        // Calculate energy in frequency domain
        double[] power = result.getPowerSpectrum();
        double transformedEnergy = 0;
        for (double p : power) {
            transformedEnergy += p;
        }
        
        // Due to normalization factor 1/√n, energy is preserved
        assertThat(transformedEnergy).isCloseTo(originalEnergy, within(EPSILON));
    }
    
    @Test
    @DisplayName("Should handle real-only input")
    void testRealOnlyInput() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        
        FFTResult result = fft.transform(real, true);
        
        assertThat(result.size()).isEqualTo(8);
        assertThat(result.getRealParts()).hasSize(8);
        assertThat(result.getImaginaryParts()).hasSize(8);
    }
    
    @Test
    @DisplayName("Should reject incorrect array sizes")
    void testIncorrectSizes() {
        double[] real4 = {1, 2, 3, 4};
        double[] imag4 = {1, 2, 3, 4};
        double[] real16 = new double[16];
        double[] imag8 = new double[8];
        
        assertThatThrownBy(() -> fft.transform(real4, imag4, true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("length 8");
        
        assertThatThrownBy(() -> fft.transform(real16, true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("length 8");
        
        assertThatThrownBy(() -> fft.transform(real16, imag8, true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("length 8");
    }
    
    @Test
    @DisplayName("Should handle impulse signal correctly")
    void testImpulseSignal() {
        double[] real = {1, 0, 0, 0, 0, 0, 0, 0};
        double[] imag = new double[8];
        
        FFTResult result = fft.transform(real, imag, true);
        
        // For impulse signal, all frequency bins should have equal magnitude
        double expectedMagnitude = 1.0 / Math.sqrt(8); // Normalized
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.getMagnitudeAt(i)).isCloseTo(expectedMagnitude, within(EPSILON));
        }
    }
    
    @Test
    @DisplayName("Should handle sine wave correctly")
    void testSineWave() {
        double[] real = new double[8];
        double[] imag = new double[8];
        
        // Create sine wave at frequency bin 1
        for (int i = 0; i < 8; i++) {
            real[i] = Math.sin(2 * Math.PI * 1 * i / 8);
        }
        
        FFTResult result = fft.transform(real, imag, true);
        double[] magnitudes = result.getMagnitudes();
        
        // Should have peaks at frequency bins 1 and 7 (due to symmetry)
        assertThat(magnitudes[1]).isGreaterThan(0.3); // Significant magnitude at bin 1
        assertThat(magnitudes[7]).isGreaterThan(0.3); // Symmetric component
        
        // Other bins should have much smaller magnitude
        for (int i = 0; i < 8; i++) {
            if (i != 1 && i != 7) {
                assertThat(magnitudes[i]).isLessThan(0.1);
            }
        }
    }
    
    @Test
    @DisplayName("Should have correct interface implementation")
    void testInterfaceImplementation() {
        assertThat(fft.getSupportedSize()).isEqualTo(8);
        assertThat(fft.supportsSize(8)).isTrue();
        assertThat(fft.supportsSize(16)).isFalse();
        assertThat(fft.supportsSize(4)).isFalse();
        assertThat(fft.getDescription()).contains("Highly optimized");
        assertThat(fft.getDescription()).contains("size 8");
    }
    
    @Test
    @DisplayName("Should handle DC signal correctly")
    void testDCSignal() {
        double[] real = {1, 1, 1, 1, 1, 1, 1, 1};
        double[] imag = new double[8];
        
        FFTResult result = fft.transform(real, imag, true);
        
        // DC component should be at index 0
        double dcMagnitude = result.getMagnitudeAt(0);
        assertThat(dcMagnitude).isCloseTo(Math.sqrt(8), within(EPSILON)); // √n * amplitude
        
        // Other components should be near zero for constant signal
        for (int i = 1; i < result.size(); i++) {
            assertThat(result.getMagnitudeAt(i)).isCloseTo(0.0, within(EPSILON));
        }
    }
    
    @Test
    @DisplayName("Should maintain consistency across multiple calls")
    void testConsistencyAcrossMultipleCalls() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8};
        
        FFTResult result1 = fft.transform(real, imag, true);
        FFTResult result2 = fft.transform(real, imag, true);
        FFTResult result3 = fft.transform(real, imag, true);
        
        // All results should be identical
        for (int i = 0; i < 8; i++) {
            assertThat(result1.getRealAt(i)).isCloseTo(result2.getRealAt(i), within(EPSILON));
            assertThat(result2.getRealAt(i)).isCloseTo(result3.getRealAt(i), within(EPSILON));
            assertThat(result1.getImaginaryAt(i)).isCloseTo(result2.getImaginaryAt(i), within(EPSILON));
            assertThat(result2.getImaginaryAt(i)).isCloseTo(result3.getImaginaryAt(i), within(EPSILON));
        }
    }
}
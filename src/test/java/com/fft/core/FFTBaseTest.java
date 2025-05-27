package com.fft.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for FFTBase implementation.
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("FFT Base Implementation Tests")
class FFTBaseTest {
    
    private FFTBase fft;
    private static final double EPSILON = 1e-10;
    
    @BeforeEach
    void setUp() {
        fft = new FFTBase();
    }
    
    @Test
    @DisplayName("Should handle simple real input correctly")
    void testSimpleRealInput() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];
        
        FFTResult result = fft.transform(real, imag, true);
        
        assertThat(result.size()).isEqualTo(8);
        assertThat(result.getRealParts()).hasSize(8);
        assertThat(result.getImaginaryParts()).hasSize(8);
    }
    
    @Test
    @DisplayName("Should preserve signal energy (Parseval's theorem)")
    void testEnergyConservation() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];
        
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
        
        for (int i = 0; i < originalReal.length; i++) {
            assertThat(recoveredReal[i]).isCloseTo(originalReal[i], within(EPSILON));
            assertThat(recoveredImag[i]).isCloseTo(originalImag[i], within(EPSILON));
        }
    }
    
    @ParameterizedTest
    @ValueSource(ints = {2, 4, 8, 16, 32, 64, 128, 256, 512, 1024})
    @DisplayName("Should support all power-of-2 sizes")
    void testPowerOfTwoSizes(int size) {
        double[] real = new double[size];
        double[] imag = new double[size];
        
        // Fill with simple test pattern
        for (int i = 0; i < size; i++) {
            real[i] = Math.sin(2 * Math.PI * i / size);
            imag[i] = Math.cos(2 * Math.PI * i / size);
        }
        
        FFTResult result = fft.transform(real, imag, true);
        
        assertThat(result.size()).isEqualTo(size);
        assertThat(fft.supportsSize(size)).isTrue();
    }
    
    @Test
    @DisplayName("Should reject arrays with different lengths")
    void testDifferentArrayLengths() {
        double[] real = {1, 2, 3, 4};
        double[] imag = {1, 2, 3};
        
        assertThatThrownBy(() -> fft.transform(real, imag, true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("same length");
    }
    
    @Test
    @DisplayName("Should reject non-power-of-2 sizes")
    void testNonPowerOfTwoSize() {
        double[] real = {1, 2, 3};
        double[] imag = {1, 2, 3};
        
        assertThatThrownBy(() -> fft.transform(real, imag, true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("power of 2");
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
    @DisplayName("Should have correct DC component for constant signal")
    void testDCComponent() {
        double[] real = {1, 1, 1, 1, 1, 1, 1, 1};
        
        FFTResult result = fft.transform(real, true);
        
        // DC component should be at index 0
        double dcMagnitude = result.getMagnitudeAt(0);
        assertThat(dcMagnitude).isCloseTo(Math.sqrt(8), within(EPSILON)); // √n * amplitude
        
        // Other components should be near zero for constant signal
        for (int i = 1; i < result.size(); i++) {
            assertThat(result.getMagnitudeAt(i)).isCloseTo(0.0, within(EPSILON));
        }
    }
    
    @Test
    @DisplayName("Should have correct interface implementation")
    void testInterfaceImplementation() {
        assertThat(fft.getSupportedSize()).isEqualTo(-1); // Generic implementation
        assertThat(fft.supportsSize(8)).isTrue();
        assertThat(fft.supportsSize(7)).isFalse(); // Not power of 2
        assertThat(fft.getDescription()).contains("Generic FFT implementation");
    }
    
    @Test
    @DisplayName("Should handle impulse signal correctly")
    void testImpulseSignal() {
        double[] real = {1, 0, 0, 0, 0, 0, 0, 0};
        
        FFTResult result = fft.transform(real, true);
        
        // For impulse signal, all frequency bins should have equal magnitude
        double expectedMagnitude = 1.0 / Math.sqrt(8); // Normalized
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.getMagnitudeAt(i)).isCloseTo(expectedMagnitude, within(EPSILON));
        }
    }
    
    @Test
    @DisplayName("Should handle sine wave correctly")
    void testSineWave() {
        int size = 32;
        double[] real = new double[size];
        
        // Create sine wave at frequency bin 4
        for (int i = 0; i < size; i++) {
            real[i] = Math.sin(2 * Math.PI * 4 * i / size);
        }
        
        FFTResult result = fft.transform(real, true);
        
        // Should have peaks at frequency bins 4 and size-4 (due to symmetry)
        double[] magnitudes = result.getMagnitudes();
        
        assertThat(magnitudes[4]).isGreaterThan(0.4); // Significant magnitude at bin 4
        assertThat(magnitudes[size - 4]).isGreaterThan(0.4); // Symmetric component
        
        // Other bins should have much smaller magnitude
        for (int i = 1; i < size / 2; i++) {
            if (i != 4) {
                assertThat(magnitudes[i]).isLessThan(0.1);
            }
        }
    }
}
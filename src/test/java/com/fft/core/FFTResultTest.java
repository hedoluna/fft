package com.fft.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for FFTResult class.
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("FFT Result Tests")
class FFTResultTest {
    
    private static final double EPSILON = 1e-10;
    
    @Test
    @DisplayName("Should create result from interleaved array")
    void testInterleavedConstructor() {
        double[] interleaved = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        
        FFTResult result = new FFTResult(interleaved);
        
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.getRealParts()).containsExactly(1.0, 3.0, 5.0);
        assertThat(result.getImaginaryParts()).containsExactly(2.0, 4.0, 6.0);
    }
    
    @Test
    @DisplayName("Should create result from separate arrays")
    void testSeparateArraysConstructor() {
        double[] real = {1.0, 3.0, 5.0};
        double[] imag = {2.0, 4.0, 6.0};
        
        FFTResult result = new FFTResult(real, imag);
        
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.getRealParts()).containsExactly(1.0, 3.0, 5.0);
        assertThat(result.getImaginaryParts()).containsExactly(2.0, 4.0, 6.0);
        assertThat(result.getInterleavedResult()).containsExactly(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
    }
    
    @Test
    @DisplayName("Should reject odd-length interleaved array")
    void testOddInterleavedArray() {
        double[] oddInterleaved = {1.0, 2.0, 3.0};
        
        assertThatThrownBy(() -> new FFTResult(oddInterleaved))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("even");
    }
    
    @Test
    @DisplayName("Should reject different length arrays")
    void testDifferentLengthArrays() {
        double[] real = {1.0, 2.0};
        double[] imag = {1.0, 2.0, 3.0};
        
        assertThatThrownBy(() -> new FFTResult(real, imag))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("same length");
    }
    
    @Test
    @DisplayName("Should compute magnitudes correctly")
    void testMagnitudes() {
        double[] real = {3.0, 0.0, 4.0};
        double[] imag = {4.0, 5.0, 0.0};
        
        FFTResult result = new FFTResult(real, imag);
        double[] magnitudes = result.getMagnitudes();
        
        assertThat(magnitudes[0]).isCloseTo(5.0, within(EPSILON)); // sqrt(3^2 + 4^2)
        assertThat(magnitudes[1]).isCloseTo(5.0, within(EPSILON)); // sqrt(0^2 + 5^2)
        assertThat(magnitudes[2]).isCloseTo(4.0, within(EPSILON)); // sqrt(4^2 + 0^2)
    }
    
    @Test
    @DisplayName("Should compute phases correctly")
    void testPhases() {
        double[] real = {1.0, 0.0, -1.0, 0.0};
        double[] imag = {0.0, 1.0, 0.0, -1.0};
        
        FFTResult result = new FFTResult(real, imag);
        double[] phases = result.getPhases();
        
        assertThat(phases[0]).isCloseTo(0.0, within(EPSILON)); // atan2(0, 1) = 0
        assertThat(phases[1]).isCloseTo(Math.PI / 2, within(EPSILON)); // atan2(1, 0) = π/2
        assertThat(phases[2]).isCloseTo(Math.PI, within(EPSILON)); // atan2(0, -1) = π
        assertThat(phases[3]).isCloseTo(-Math.PI / 2, within(EPSILON)); // atan2(-1, 0) = -π/2
    }
    
    @Test
    @DisplayName("Should compute power spectrum correctly")
    void testPowerSpectrum() {
        double[] real = {3.0, 0.0, 4.0};
        double[] imag = {4.0, 5.0, 0.0};
        
        FFTResult result = new FFTResult(real, imag);
        double[] power = result.getPowerSpectrum();
        
        assertThat(power[0]).isCloseTo(25.0, within(EPSILON)); // 3^2 + 4^2
        assertThat(power[1]).isCloseTo(25.0, within(EPSILON)); // 0^2 + 5^2
        assertThat(power[2]).isCloseTo(16.0, within(EPSILON)); // 4^2 + 0^2
    }
    
    @Test
    @DisplayName("Should provide indexed access methods")
    void testIndexedAccess() {
        double[] real = {1.0, 3.0, 5.0};
        double[] imag = {2.0, 4.0, 6.0};
        
        FFTResult result = new FFTResult(real, imag);
        
        assertThat(result.getRealAt(1)).isEqualTo(3.0);
        assertThat(result.getImaginaryAt(1)).isEqualTo(4.0);
        assertThat(result.getMagnitudeAt(1)).isCloseTo(5.0, within(EPSILON)); // sqrt(3^2 + 4^2)
        assertThat(result.getPhaseAt(1)).isCloseTo(Math.atan2(4.0, 3.0), within(EPSILON));
    }
    
    @Test
    @DisplayName("Should handle bounds checking")
    void testBoundsChecking() {
        FFTResult result = new FFTResult(new double[]{1, 2, 3, 4});
        
        assertThatThrownBy(() -> result.getRealAt(-1))
            .isInstanceOf(IndexOutOfBoundsException.class);
        
        assertThatThrownBy(() -> result.getRealAt(2))
            .isInstanceOf(IndexOutOfBoundsException.class);
        
        assertThatThrownBy(() -> result.getImaginaryAt(2))
            .isInstanceOf(IndexOutOfBoundsException.class);
        
        assertThatThrownBy(() -> result.getMagnitudeAt(2))
            .isInstanceOf(IndexOutOfBoundsException.class);
        
        assertThatThrownBy(() -> result.getPhaseAt(2))
            .isInstanceOf(IndexOutOfBoundsException.class);
    }
    
    @Test
    @DisplayName("Should be immutable")
    void testImmutability() {
        double[] originalInterleaved = {1.0, 2.0, 3.0, 4.0};
        FFTResult result = new FFTResult(originalInterleaved);
        
        // Modify original array
        originalInterleaved[0] = 999.0;
        
        // Result should not be affected
        assertThat(result.getRealAt(0)).isEqualTo(1.0);
        
        // Modify returned array
        double[] returnedReal = result.getRealParts();
        returnedReal[0] = 888.0;
        
        // Result should not be affected
        assertThat(result.getRealAt(0)).isEqualTo(1.0);
        assertThat(result.getRealParts()[0]).isEqualTo(1.0);
    }
    
    @Test
    @DisplayName("Should handle empty result")
    void testEmptyResult() {
        FFTResult result = new FFTResult(new double[0]);
        
        assertThat(result.size()).isEqualTo(0);
        assertThat(result.getRealParts()).isEmpty();
        assertThat(result.getImaginaryParts()).isEmpty();
        assertThat(result.getMagnitudes()).isEmpty();
        assertThat(result.getPhases()).isEmpty();
        assertThat(result.getPowerSpectrum()).isEmpty();
    }
    
    @Test
    @DisplayName("Should provide meaningful toString")
    void testToString() {
        double[] real = {3.0, 4.0};
        double[] imag = {4.0, 3.0};
        
        FFTResult result = new FFTResult(real, imag);
        String toString = result.toString();
        
        assertThat(toString).contains("FFTResult");
        assertThat(toString).contains("size=2");
        assertThat(toString).contains("5.000"); // First magnitude
    }
    
    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        double[] data1 = {1.0, 2.0, 3.0, 4.0};
        double[] data2 = {1.0, 2.0, 3.0, 4.0};
        double[] data3 = {1.0, 2.0, 5.0, 6.0};
        
        FFTResult result1 = new FFTResult(data1);
        FFTResult result2 = new FFTResult(data2);
        FFTResult result3 = new FFTResult(data3);
        
        assertThat(result1).isEqualTo(result2);
        assertThat(result1).isNotEqualTo(result3);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        assertThat(result1.hashCode()).isNotEqualTo(result3.hashCode());
    }
}
package com.fft.utils;

import com.fft.core.FFTResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for FFTUtils.
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("FFT Utils Tests")
class FFTUtilsTest {
    
    private static final double EPSILON = 1e-10;
    
    @Test
    @DisplayName("Should perform FFT with real and imaginary input")
    void testFFTWithComplexInput() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = {0, 0, 0, 0, 0, 0, 0, 0};
        
        FFTResult result = FFTUtils.fft(real, imag, true);
        
        assertThat(result.size()).isEqualTo(8);
        assertThat(result.getRealParts()).hasSize(8);
        assertThat(result.getImaginaryParts()).hasSize(8);
    }
    
    @Test
    @DisplayName("Should perform FFT with real-only input")
    void testFFTWithRealInput() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        
        FFTResult result = FFTUtils.fft(real, true);
        
        assertThat(result.size()).isEqualTo(8);
        assertThat(result.getRealParts()).hasSize(8);
        assertThat(result.getImaginaryParts()).hasSize(8);
    }
    
    @Test
    @DisplayName("Should perform forward FFT by default")
    void testDefaultForwardFFT() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        
        FFTResult result1 = FFTUtils.fft(real);
        FFTResult result2 = FFTUtils.fft(real, true);
        
        assertThat(result1.getRealParts()).containsExactly(result2.getRealParts(), within(EPSILON));
        assertThat(result1.getImaginaryParts()).containsExactly(result2.getImaginaryParts(), within(EPSILON));
    }
    
    @Test
    @DisplayName("Should reject different array lengths")
    void testDifferentArrayLengths() {
        double[] real = {1, 2, 3, 4};
        double[] imag = {1, 2, 3};
        
        assertThatThrownBy(() -> FFTUtils.fft(real, imag, true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("same length");
    }
    
    @Test
    @DisplayName("Should reject non-power-of-2 sizes")
    void testNonPowerOfTwoSize() {
        double[] real = {1, 2, 3};
        
        assertThatThrownBy(() -> FFTUtils.fft(real))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("power of 2");
    }
    
    @Test
    @DisplayName("Should generate test signal correctly")
    void testGenerateTestSignal() {
        double[] frequencies = {1.0, 2.0};
        double[] amplitudes = {1.0, 0.5};
        
        double[] signal = FFTUtils.generateTestSignal(64, 64.0, frequencies, amplitudes);
        
        assertThat(signal).hasSize(64);
        
        // Check that signal contains expected frequency components
        FFTResult spectrum = FFTUtils.fft(signal);
        double[] magnitudes = spectrum.getMagnitudes();
        
        // Should have peaks at the specified frequencies (adjusted expectations)
        assertThat(magnitudes[1]).isGreaterThan(2.0); // Frequency bin 1
        assertThat(magnitudes[2]).isGreaterThan(1.0);  // Frequency bin 2
    }
    
    @Test
    @DisplayName("Should generate sine wave correctly")
    void testGenerateSineWave() {
        double[] signal = FFTUtils.generateSineWave(32, 4.0, 32.0);
        
        assertThat(signal).hasSize(32);
        
        // Check that signal is a proper sine wave
        FFTResult spectrum = FFTUtils.fft(signal);
        double[] magnitudes = spectrum.getMagnitudes();
        
        // Should have peak at frequency bin 4 (adjusted expectations)
        assertThat(magnitudes[4]).isGreaterThan(2.0);
        
        // Other bins should be much smaller
        for (int i = 1; i < 16; i++) {
            if (i != 4 && i != 32 - 4) {
                assertThat(magnitudes[i]).isLessThan(1.0);
            }
        }
    }
    
    @Test
    @DisplayName("Should reject mismatched frequency/amplitude arrays")
    void testMismatchedFrequencyAmplitudeArrays() {
        double[] frequencies = {1.0, 2.0};
        double[] amplitudes = {1.0};
        
        assertThatThrownBy(() -> FFTUtils.generateTestSignal(64, 64.0, frequencies, amplitudes))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("same length");
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024})
    @DisplayName("Should correctly identify powers of 2")
    void testIsPowerOfTwo(int value) {
        assertThat(FFTUtils.isPowerOfTwo(value)).isTrue();
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, 3, 5, 6, 7, 9, 10, 15, 17, 31, 33, 100})
    @DisplayName("Should correctly identify non-powers of 2")
    void testIsNotPowerOfTwo(int value) {
        assertThat(FFTUtils.isPowerOfTwo(value)).isFalse();
    }
    
    @Test
    @DisplayName("Should find next power of 2")
    void testNextPowerOfTwo() {
        assertThat(FFTUtils.nextPowerOfTwo(1)).isEqualTo(1);
        assertThat(FFTUtils.nextPowerOfTwo(2)).isEqualTo(2);
        assertThat(FFTUtils.nextPowerOfTwo(3)).isEqualTo(4);
        assertThat(FFTUtils.nextPowerOfTwo(5)).isEqualTo(8);
        assertThat(FFTUtils.nextPowerOfTwo(8)).isEqualTo(8);
        assertThat(FFTUtils.nextPowerOfTwo(9)).isEqualTo(16);
        assertThat(FFTUtils.nextPowerOfTwo(100)).isEqualTo(128);
        assertThat(FFTUtils.nextPowerOfTwo(0)).isEqualTo(1);
        assertThat(FFTUtils.nextPowerOfTwo(-5)).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should zero-pad to power of 2")
    void testZeroPadToPowerOfTwo() {
        double[] input = {1, 2, 3, 4, 5};
        double[] padded = FFTUtils.zeroPadToPowerOfTwo(input);
        
        assertThat(padded).hasSize(8);
        assertThat(padded).startsWith(1.0, 2.0, 3.0, 4.0, 5.0);
        assertThat(padded).endsWith(0.0, 0.0, 0.0);
        
        // Original should not be modified
        assertThat(input).containsExactly(1, 2, 3, 4, 5);
    }
    
    @Test
    @DisplayName("Should not pad if already power of 2")
    void testNoPaddingIfPowerOfTwo() {
        double[] input = {1, 2, 3, 4};
        double[] result = FFTUtils.zeroPadToPowerOfTwo(input);
        
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly(1, 2, 3, 4);
    }
    
    @Test
    @DisplayName("Should provide implementation info")
    void testGetImplementationInfo() {
        String info8 = FFTUtils.getImplementationInfo(8);
        String info7 = FFTUtils.getImplementationInfo(7);
        
        assertThat(info8).contains("implementation");
        assertThat(info7).contains("Invalid size");
    }
    
    @Test
    @DisplayName("Should provide supported sizes")
    void testGetSupportedSizes() {
        List<Integer> sizes = FFTUtils.getSupportedSizes();
        
        assertThat(sizes).contains(2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192);
        assertThat(sizes).isSorted();
    }
    
    @Test
    @DisplayName("Should create factory")
    void testCreateFactory() {
        var factory = FFTUtils.createFactory();
        
        assertThat(factory).isNotNull();
        assertThat(factory.supportsSize(8)).isTrue();
        assertThat(factory.supportsSize(7)).isFalse();
    }
    
    // Legacy compatibility tests
    
    @Test
    @DisplayName("Should support legacy fft method")
    void testLegacyFFT() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];
        
        double[] result = FFTUtils.fftLegacy(real, imag, true);
        
        assertThat(result).hasSize(16); // Interleaved format
        
        // Should match new API result
        FFTResult newResult = FFTUtils.fft(real, imag, true);
        assertThat(result).containsExactly(newResult.getInterleavedResult(), within(EPSILON));
    }
    
    @Test
    @DisplayName("Should support legacy extraction methods")
    void testLegacyExtractionMethods() {
        double[] interleaved = {1, 2, 3, 4, 5, 6, 7, 8};
        
        double[] real = FFTUtils.getRealParts(interleaved);
        double[] imag = FFTUtils.getImagParts(interleaved);
        double[] magnitudes = FFTUtils.getMagnitudes(interleaved);
        double[] phases = FFTUtils.getPhases(interleaved);
        
        assertThat(real).containsExactly(1, 3, 5, 7);
        assertThat(imag).containsExactly(2, 4, 6, 8);
        assertThat(magnitudes).hasSize(4);
        assertThat(phases).hasSize(4);
        
        // Should match FFTResult methods
        FFTResult result = new FFTResult(interleaved);
        assertThat(real).containsExactly(result.getRealParts(), within(EPSILON));
        assertThat(imag).containsExactly(result.getImaginaryParts(), within(EPSILON));
        assertThat(magnitudes).containsExactly(result.getMagnitudes(), within(EPSILON));
        assertThat(phases).containsExactly(result.getPhases(), within(EPSILON));
    }
}
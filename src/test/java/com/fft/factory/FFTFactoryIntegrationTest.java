package com.fft.factory;

import com.fft.core.FFT;
import com.fft.core.FFTResult;
import com.fft.optimized.FFTOptimized8;
import com.fft.utils.FFTUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for FFT factory functionality.
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("FFT Factory Integration Tests")
class FFTFactoryIntegrationTest {
    
    @Test
    @DisplayName("Should automatically select optimized implementation for size 8")
    void testOptimizedImplementationSelection() {
        DefaultFFTFactory factory = new DefaultFFTFactory();
        
        FFT fft8 = factory.createFFT(8);
        
        // Should get the optimized implementation for size 8
        assertThat(fft8).isInstanceOf(FFTOptimized8.class);
        assertThat(fft8.getSupportedSize()).isEqualTo(8);
        assertThat(fft8.getDescription()).contains("Highly optimized");
    }
    
    @Test
    @DisplayName("Should use optimized implementation through FFTUtils")
    void testOptimizedImplementationThroughUtils() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        
        FFTResult result = FFTUtils.fft(real);
        
        assertThat(result.size()).isEqualTo(8);
        
        // Verify the implementation info indicates optimized version
        String implInfo = FFTUtils.getImplementationInfo(8);
        assertThat(implInfo).contains("Highly optimized");
        assertThat(implInfo).contains("priority: 50");
    }
    
    @Test
    @DisplayName("Should handle forward and inverse transforms correctly")
    void testForwardInverseTransformIntegration() {
        double[] originalReal = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] originalImag = {0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5};
        
        // Forward transform using optimized implementation
        FFTResult forward = FFTUtils.fft(originalReal, originalImag, true);
        
        // Inverse transform (should use fallback for optimized implementation)
        FFTResult inverse = FFTUtils.fft(forward.getRealParts(), forward.getImaginaryParts(), false);
        
        // Should recover original signal
        double[] recoveredReal = inverse.getRealParts();
        double[] recoveredImag = inverse.getImaginaryParts();
        
        for (int i = 0; i < originalReal.length; i++) {
            assertThat(recoveredReal[i]).isCloseTo(originalReal[i], within(1e-10));
            assertThat(recoveredImag[i]).isCloseTo(originalImag[i], within(1e-10));
        }
    }
    
    @Test
    @DisplayName("Should provide correct implementation registry report")
    void testImplementationRegistryReport() {
        DefaultFFTFactory factory = new DefaultFFTFactory();
        
        String report = factory.getRegistryReport();
        
        assertThat(report).contains("FFT Factory Implementation Registry");
        assertThat(report).contains("Size 8: Highly optimized FFT implementation");
        assertThat(report).contains("priority: 50");
    }
    
    @Test
    @DisplayName("Should maintain compatibility with legacy API")
    void testLegacyAPICompatibility() {
        // Test that legacy FFTUtils still works
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];
        
        // Legacy API should still work and delegate to new implementation
        @SuppressWarnings("deprecation")
        double[] legacyResult = FFTUtils.fftLegacy(real, imag, true);
        
        // New API result
        FFTResult newResult = FFTUtils.fft(real, imag, true);
        
        // Results should be identical
        double[] newResultInterleaved = newResult.getInterleavedResult();
        assertThat(legacyResult).containsExactly(newResultInterleaved, within(1e-15));
    }
    
    @Test
    @DisplayName("Should handle performance comparison correctly")
    void testPerformanceCharacteristics() {
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];
        
        DefaultFFTFactory factory = new DefaultFFTFactory();
        FFT optimized = factory.createFFT(8);
        
        // Multiple runs to ensure consistency
        FFTResult result1 = optimized.transform(real, imag, true);
        FFTResult result2 = optimized.transform(real, imag, true);
        FFTResult result3 = optimized.transform(real, imag, true);
        
        // All results should be identical (deterministic)
        for (int i = 0; i < 8; i++) {
            assertThat(result1.getRealAt(i)).isCloseTo(result2.getRealAt(i), within(1e-15));
            assertThat(result2.getRealAt(i)).isCloseTo(result3.getRealAt(i), within(1e-15));
            assertThat(result1.getImaginaryAt(i)).isCloseTo(result2.getImaginaryAt(i), within(1e-15));
            assertThat(result2.getImaginaryAt(i)).isCloseTo(result3.getImaginaryAt(i), within(1e-15));
        }
    }
    
    @Test
    @DisplayName("Should register and unregister implementations correctly")
    void testImplementationRegistration() {
        DefaultFFTFactory factory = new DefaultFFTFactory();
        
        int initialCount = factory.getImplementationCount(8);
        assertThat(initialCount).isGreaterThan(0);
        
        // Register a new mock implementation with higher priority
        FFT mockFFT = new MockFFT();
        factory.registerImplementation(8, () -> mockFFT, 100);
        
        assertThat(factory.getImplementationCount(8)).isEqualTo(initialCount + 1);
        
        // Should now select the mock implementation due to higher priority
        FFT selected = factory.createFFT(8);
        assertThat(selected).isSameAs(mockFFT);
        
        // Unregister all implementations for size 8
        factory.unregisterImplementations(8);
        assertThat(factory.getImplementationCount(8)).isEqualTo(0);
        
        // Should now fall back to FFTBase
        FFT fallback = factory.createFFT(8);
        assertThat(fallback.getSupportedSize()).isEqualTo(-1); // FFTBase returns -1
    }
    
    /**
     * Mock FFT implementation for testing.
     */
    private static class MockFFT implements FFT {
        @Override
        public FFTResult transform(double[] real, double[] imaginary, boolean forward) {
            return new FFTResult(new double[real.length * 2]);
        }
        
        @Override
        public FFTResult transform(double[] real, boolean forward) {
            return transform(real, new double[real.length], forward);
        }
        
        @Override
        public int getSupportedSize() {
            return 8;
        }
        
        @Override
        public boolean supportsSize(int size) {
            return size == 8;
        }
        
        @Override
        public String getDescription() {
            return "Mock FFT implementation for testing";
        }
    }
}
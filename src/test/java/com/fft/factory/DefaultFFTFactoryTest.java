package com.fft.factory;

import com.fft.core.FFT;
import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for DefaultFFTFactory.
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("Default FFT Factory Tests")
class DefaultFFTFactoryTest {
    
    private DefaultFFTFactory factory;
    
    @BeforeEach
    void setUp() {
        factory = new DefaultFFTFactory();
    }
    
    @Test
    @DisplayName("Should create FFT implementations for valid sizes")
    void testCreateFFT() {
        FFT fft8 = factory.createFFT(8);
        FFT fft16 = factory.createFFT(16);
        FFT fft1024 = factory.createFFT(1024);
        
        assertThat(fft8).isNotNull();
        assertThat(fft16).isNotNull();
        assertThat(fft1024).isNotNull();
        
        // Should be able to perform transforms
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];
        
        FFTResult result = fft8.transform(real, imag, true);
        assertThat(result.size()).isEqualTo(8);
    }
    
    @Test
    @DisplayName("Should reject non-power-of-2 sizes")
    void testNonPowerOfTwoSize() {
        assertThatThrownBy(() -> factory.createFFT(7))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("power of 2");
        
        assertThatThrownBy(() -> factory.createFFT(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("power of 2");
        
        assertThatThrownBy(() -> factory.createFFT(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("power of 2");
    }
    
    @Test
    @DisplayName("Should support common power-of-2 sizes")
    void testSupportedSizes() {
        assertThat(factory.supportsSize(2)).isTrue();
        assertThat(factory.supportsSize(4)).isTrue();
        assertThat(factory.supportsSize(8)).isTrue();
        assertThat(factory.supportsSize(16)).isTrue();
        assertThat(factory.supportsSize(32)).isTrue();
        assertThat(factory.supportsSize(64)).isTrue();
        assertThat(factory.supportsSize(128)).isTrue();
        assertThat(factory.supportsSize(256)).isTrue();
        assertThat(factory.supportsSize(512)).isTrue();
        assertThat(factory.supportsSize(1024)).isTrue();
        assertThat(factory.supportsSize(2048)).isTrue();
        assertThat(factory.supportsSize(4096)).isTrue();
        assertThat(factory.supportsSize(8192)).isTrue();
        assertThat(factory.supportsSize(16384)).isTrue();
        assertThat(factory.supportsSize(32768)).isTrue();
        assertThat(factory.supportsSize(65536)).isTrue();
        
        assertThat(factory.supportsSize(7)).isFalse();
        assertThat(factory.supportsSize(15)).isFalse();
        assertThat(factory.supportsSize(100)).isFalse();
    }
    
    @Test
    @DisplayName("Should return list of supported sizes")
    void testGetSupportedSizes() {
        var sizes = factory.getSupportedSizes();
        
        assertThat(sizes).contains(2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192,
                                  16384, 32768, 65536);
        assertThat(sizes).isSorted();
        assertThat(sizes).doesNotContain(7, 15, 100);
    }
    
    @Test
    @DisplayName("Should register custom implementations")
    void testRegisterImplementation() {
        // Create a mock implementation
        FFT customFFT = new MockFFT(64);
        
        factory.registerImplementation(64, () -> customFFT, 100); // High priority
        
        FFT created = factory.createFFT(64);
        assertThat(created).isSameAs(customFFT);
    }
    
    @Test
    @DisplayName("Should respect implementation priority")
    void testImplementationPriority() {
        FFT lowPriorityFFT = new MockFFT(32);
        FFT highPriorityFFT = new MockFFT(32);
        
        // Use higher priorities than auto-discovered implementations (which have priority 50)
        factory.registerImplementation(32, () -> lowPriorityFFT, 55);
        factory.registerImplementation(32, () -> highPriorityFFT, 60);
        
        FFT created = factory.createFFT(32);
        assertThat(created).isSameAs(highPriorityFFT);
    }
    
    @Test
    @DisplayName("Should reject null implementations")
    void testNullImplementation() {
        assertThatThrownBy(() -> factory.registerImplementation(8, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null");
    }
    
    @Test
    @DisplayName("Should provide implementation info")
    void testImplementationInfo() {
        String info8 = factory.getImplementationInfo(8);
        String info7 = factory.getImplementationInfo(7);
        
        assertThat(info8).contains("implementation");
        assertThat(info7).contains("Invalid size");
    }
    
    @Test
    @DisplayName("Should unregister implementations")
    void testUnregisterImplementations() {
        FFT customFFT = new MockFFT(16);
        factory.registerImplementation(16, () -> customFFT);
        
        assertThat(factory.getImplementationCount(16)).isGreaterThan(0);
        
        boolean removed = factory.unregisterImplementations(16);
        assertThat(removed).isTrue();
        assertThat(factory.getImplementationCount(16)).isEqualTo(0);
        
        // Should fall back to default implementation
        FFT fft = factory.createFFT(16);
        assertThat(fft).isInstanceOf(FFTBase.class);
    }
    
    @Test
    @DisplayName("Should count implementations correctly")
    void testImplementationCount() {
        int initialCount = factory.getImplementationCount(64);
        
        factory.registerImplementation(64, () -> new MockFFT(64), 10);
        assertThat(factory.getImplementationCount(64)).isEqualTo(initialCount + 1);
        
        factory.registerImplementation(64, () -> new MockFFT(64), 20);
        assertThat(factory.getImplementationCount(64)).isEqualTo(initialCount + 2);
    }
    
    @Test
    @DisplayName("Should provide registry report")
    void testRegistryReport() {
        String report = factory.getRegistryReport();
        
        assertThat(report).contains("FFT Factory Implementation Registry");
        assertThat(report).contains("Size");
        assertThat(report).contains("implementation");
    }
    
    /**
     * Mock FFT implementation for testing purposes.
     */
    @FFTImplementation(size = 16, priority = 100, description = "Mock FFT for testing")
    private static class MockFFT implements FFT {
        private final int supportedSize;
        
        MockFFT(int supportedSize) {
            this.supportedSize = supportedSize;
        }
        
        @Override
        public FFTResult transform(double[] real, double[] imaginary, boolean forward) {
            if (real.length != supportedSize) {
                throw new IllegalArgumentException("Unsupported size: " + real.length);
            }
            // Return dummy result
            return new FFTResult(new double[real.length * 2]);
        }
        
        @Override
        public FFTResult transform(double[] real, boolean forward) {
            return transform(real, new double[real.length], forward);
        }
        
        @Override
        public int getSupportedSize() {
            return supportedSize;
        }
        
        @Override
        public boolean supportsSize(int size) {
            return size == supportedSize;
        }
        
        @Override
        public String getDescription() {
            return "Mock FFT implementation (size " + supportedSize + ")";
        }
    }
}
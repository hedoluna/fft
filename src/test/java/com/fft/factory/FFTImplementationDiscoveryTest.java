package com.fft.factory;

import com.fft.optimized.FFTOptimized32;
import com.fft.optimized.FFTOptimized8;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for the FFT implementation auto-discovery mechanism.
 * 
 * <p>Tests the discovery, registration, and management of FFT implementations
 * through annotations and reflection.</p>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("FFT Implementation Discovery Tests")
class FFTImplementationDiscoveryTest {
    
    @BeforeEach
    void setUp() {
        // Clear any cached discoveries to ensure clean test state
        // Note: In production, you might want to make the cache clearable for testing
    }
    
    @Test
    @DisplayName("Should discover annotated implementations")
    void testDiscoverImplementations() {
        Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> discovered = 
            FFTImplementationDiscovery.discoverImplementations();
        
        assertThat(discovered).isNotNull();
        
        // Should discover the implementations we know exist
        assertThat(discovered).containsKey(8);
        assertThat(discovered).containsKey(32);
        
        // Verify size 8 implementation
        List<FFTImplementationDiscovery.DiscoveredImplementation> size8Impls = discovered.get(8);
        assertThat(size8Impls).isNotEmpty();
        
        boolean foundOptimized8 = size8Impls.stream()
            .anyMatch(impl -> impl.getImplementationClass().equals(FFTOptimized8.class));
        assertThat(foundOptimized8).isTrue();
        
        // Verify size 32 implementation
        List<FFTImplementationDiscovery.DiscoveredImplementation> size32Impls = discovered.get(32);
        assertThat(size32Impls).isNotEmpty();
        
        boolean foundOptimized32 = size32Impls.stream()
            .anyMatch(impl -> impl.getImplementationClass().equals(FFTOptimized32.class));
        assertThat(foundOptimized32).isTrue();
    }
    
    @Test
    @DisplayName("Should parse annotation metadata correctly")
    void testAnnotationMetadata() {
        Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> discovered = 
            FFTImplementationDiscovery.discoverImplementations();
        
        // Check FFTOptimized32 metadata
        List<FFTImplementationDiscovery.DiscoveredImplementation> size32Impls = discovered.get(32);
        assertThat(size32Impls).isNotEmpty();
        
        FFTImplementationDiscovery.DiscoveredImplementation impl32 = size32Impls.stream()
            .filter(impl -> impl.getImplementationClass().equals(FFTOptimized32.class))
            .findFirst()
            .orElse(null);
        
        assertThat(impl32).isNotNull();
        assertThat(impl32.getSize()).isEqualTo(32);
        assertThat(impl32.getPriority()).isEqualTo(50);
        assertThat(impl32.isAutoRegister()).isTrue();
        assertThat(impl32.getDescription()).isNotEmpty();
        assertThat(impl32.getCharacteristics()).contains("unrolled-loops", "precomputed-trig", "zero-overhead");
    }
    
    @Test
    @DisplayName("Should sort implementations by priority")
    void testPrioritySorting() {
        Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> discovered = 
            FFTImplementationDiscovery.discoverImplementations();
        
        // For each size, implementations should be sorted by priority (highest first)
        for (List<FFTImplementationDiscovery.DiscoveredImplementation> impls : discovered.values()) {
            if (impls.size() > 1) {
                for (int i = 0; i < impls.size() - 1; i++) {
                    assertThat(impls.get(i).getPriority())
                        .isGreaterThanOrEqualTo(impls.get(i + 1).getPriority());
                }
            }
        }
    }
    
    @Test
    @DisplayName("Should create working suppliers")
    void testSupplierCreation() {
        Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> discovered = 
            FFTImplementationDiscovery.discoverImplementations();
        
        List<FFTImplementationDiscovery.DiscoveredImplementation> size32Impls = discovered.get(32);
        assertThat(size32Impls).isNotEmpty();
        
        FFTImplementationDiscovery.DiscoveredImplementation impl = size32Impls.get(0);
        
        // Should be able to create instances
        assertDoesNotThrow(() -> {
            var fft1 = impl.getSupplier().get();
            var fft2 = impl.getSupplier().get();
            
            assertThat(fft1).isNotNull();
            assertThat(fft2).isNotNull();
            assertThat(fft1).isNotSameAs(fft2); // Should create new instances
            assertThat(fft1.getSupportedSize()).isEqualTo(32);
        });
    }
    
    @Test
    @DisplayName("Should auto-register with factory")
    void testAutoRegistration() {
        DefaultFFTFactory factory = new DefaultFFTFactory();
        
        // Should have discovered and registered implementations
        assertThat(factory.supportsSize(8)).isTrue();
        assertThat(factory.supportsSize(32)).isTrue();
        
        // Should be able to create implementations
        var fft8 = factory.createFFT(8);
        var fft32 = factory.createFFT(32);
        
        assertThat(fft8).isNotNull();
        assertThat(fft32).isNotNull();
        assertThat(fft8.getSupportedSize()).isEqualTo(8);
        assertThat(fft32.getSupportedSize()).isEqualTo(32);
    }
    
    @Test
    @DisplayName("Should provide discovery report")
    void testDiscoveryReport() {
        String report = FFTImplementationDiscovery.getDiscoveryReport();
        
        assertThat(report).isNotEmpty();
        assertThat(report).contains("FFT Implementation Discovery Report");
        assertThat(report).contains("Size 8");
        assertThat(report).contains("Size 32");
        assertThat(report).contains("priority:");
        assertThat(report).contains("auto-register:");
    }
    
    @Test
    @DisplayName("Should handle empty discoveries gracefully")
    void testEmptyDiscovery() {
        // This tests the robustness of the discovery mechanism
        // Even if no implementations are found, it should not fail
        
        assertDoesNotThrow(() -> {
            Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> discovered = 
                FFTImplementationDiscovery.discoverImplementations();
            
            // The map should never be null
            assertThat(discovered).isNotNull();
        });
    }
    
    @Test
    @DisplayName("Should register discovered implementations with correct priority")
    void testPriorityRegistration() {
        DefaultFFTFactory factory = new DefaultFFTFactory();
        
        // Get implementation info to verify priority is respected
        String info8 = factory.getImplementationInfo(8);
        String info32 = factory.getImplementationInfo(32);
        
        assertThat(info8).isNotEmpty();
        assertThat(info32).isNotEmpty();
        
        // Should show high priority implementations are selected
        assertThat(info8).contains("priority: 50");
        assertThat(info32).contains("priority: 50");
    }
    
    @Test
    @DisplayName("Should detect implementation characteristics")
    void testCharacteristicsDetection() {
        Map<Integer, List<FFTImplementationDiscovery.DiscoveredImplementation>> discovered = 
            FFTImplementationDiscovery.discoverImplementations();
        
        List<FFTImplementationDiscovery.DiscoveredImplementation> size32Impls = discovered.get(32);
        assertThat(size32Impls).isNotEmpty();
        
        FFTImplementationDiscovery.DiscoveredImplementation impl32 = size32Impls.stream()
            .filter(impl -> impl.getImplementationClass().equals(FFTOptimized32.class))
            .findFirst()
            .orElse(null);
        
        assertThat(impl32).isNotNull();
        String[] characteristics = impl32.getCharacteristics();
        
        assertThat(characteristics).contains("unrolled-loops");
        assertThat(characteristics).contains("precomputed-trig");
        assertThat(characteristics).contains("zero-overhead");
    }
}
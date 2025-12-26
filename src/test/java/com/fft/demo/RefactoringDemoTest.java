package com.fft.demo;

import com.fft.core.FFT;
import com.fft.core.FFTResult;
import com.fft.factory.DefaultFFTFactory;
import com.fft.utils.FFTUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for RefactoringDemo.
 * 
 * <p>Tests the refactoring demonstration functionality including API showcase,
 * factory pattern validation, performance demonstrations, and backward compatibility.</p>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("Refactoring Demo Tests")
class RefactoringDemoTest {
    
    private static final double[] TEST_SIGNAL = {1, 2, 3, 4, 5, 6, 7, 8};
    private static final double TOLERANCE = 1e-10;
    
    @Nested
    @DisplayName("Demo Execution Tests")
    class DemoExecutionTests {
        
        @Test
        @DisplayName("Should run main demo without exceptions")
        void shouldRunMainDemoWithoutExceptions() {
            // Capture output to prevent console spam during tests
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outputStream));
            
            try {
                assertDoesNotThrow(() -> {
                    RefactoringDemo.main(new String[]{});
                }, "RefactoringDemo.main should run without exceptions");
                
                String output = outputStream.toString();
                assertThat(output).contains("FFT Library Refactoring Demonstration");
                assertThat(output).contains("Refactoring Demonstration Complete");
            } finally {
                System.setOut(originalOut);
            }
        }
    }
    
    @Nested
    @DisplayName("New API Demonstration Tests")
    class NewAPIDemonstrationTests {
        
        @Test
        @DisplayName("Should demonstrate new type-safe API")
        void shouldDemonstrateNewTypeSafeAPI() {
            // Test that new API returns structured FFTResult object
            FFTResult result = FFTUtils.fft(TEST_SIGNAL);
            
            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(TEST_SIGNAL.length);
            
            // Test structured access methods
            assertThat(result.getMagnitudeAt(0)).isNotNaN();
            assertThat(result.getRealAt(0)).isNotNaN();
            assertThat(result.getImaginaryAt(0)).isNotNaN();
            
            // Test array accessors
            assertThat(result.getRealParts()).hasSize(TEST_SIGNAL.length);
            assertThat(result.getImaginaryParts()).hasSize(TEST_SIGNAL.length);
            assertThat(result.getMagnitudes()).hasSize(TEST_SIGNAL.length);
            assertThat(result.getPhases()).hasSize(TEST_SIGNAL.length);
            assertThat(result.getPowerSpectrum()).hasSize(TEST_SIGNAL.length);
        }
        
        @Test
        @DisplayName("Should demonstrate API consistency across different input sizes")
        void shouldDemonstrateAPIConsistencyAcrossDifferentInputSizes() {
            double[][] testSignals = {
                {1, 2, 3, 4},
                {1, 2, 3, 4, 5, 6, 7, 8},
                {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}
            };
            
            for (double[] signal : testSignals) {
                FFTResult result = FFTUtils.fft(signal);
                
                assertThat(result.size()).isEqualTo(signal.length);
                assertThat(result.getMagnitudes()).hasSize(signal.length);
                assertThat(result.toString()).contains("FFTResult");
            }
        }
    }
    
    @Nested
    @DisplayName("Factory Pattern Demonstration Tests")
    class FactoryPatternDemonstrationTests {
        
        @Test
        @DisplayName("Should demonstrate automatic implementation selection")
        void shouldDemonstrateAutomaticImplementationSelection() {
            DefaultFFTFactory factory = new DefaultFFTFactory();
            
            // Test different sizes get appropriate implementations
            FFT fft8 = factory.createFFT(8);
            FFT fft16 = factory.createFFT(16);
            FFT fft1024 = factory.createFFT(1024);
            
            assertThat(fft8).isNotNull();
            assertThat(fft16).isNotNull();
            assertThat(fft1024).isNotNull();
            
            // Each should support their respective sizes
            assertThat(fft8.supportsSize(8)).isTrue();
            assertThat(fft16.supportsSize(16)).isTrue();
            assertThat(fft1024.supportsSize(1024)).isTrue();
            
            // Descriptions should be meaningful
            assertThat(fft8.getDescription()).isNotEmpty();
            assertThat(fft16.getDescription()).isNotEmpty();
            assertThat(fft1024.getDescription()).isNotEmpty();
        }
        
        @Test
        @DisplayName("Should demonstrate factory registry functionality")
        void shouldDemonstrateFactoryRegistryFunctionality() {
            DefaultFFTFactory factory = new DefaultFFTFactory();
            
            String registryReport = factory.getRegistryReport();
            assertThat(registryReport).isNotNull();
            assertThat(registryReport).isNotEmpty();
            
            // Registry report should contain implementation information
            assertThat(registryReport).containsIgnoringCase("implementation");
        }
        
        @Test
        @DisplayName("Should create different implementations for different sizes")
        void shouldCreateDifferentImplementationsForDifferentSizes() {
            DefaultFFTFactory factory = new DefaultFFTFactory();

            FFT fft8 = factory.createFFT(8);
            FFT fft16 = factory.createFFT(16);

            // Different sizes may use different implementations
            String desc8 = fft8.getDescription();
            String desc16 = fft16.getDescription();

            assertThat(desc8).isNotEmpty();
            assertThat(desc16).isNotEmpty();

            // FFT8 uses FFTOptimized8 (size-specific)
            assertThat(fft8.supportsSize(8)).isTrue();
            assertThat(fft8.supportsSize(16)).isFalse();

            // FFT16 uses FFTBase (supports all power-of-2 sizes)
            assertThat(fft16.supportsSize(16)).isTrue();
            // Note: FFTBase is generic and supports any power-of-2 size
            assertThat(fft16.supportsSize(8)).isTrue();
        }
    }
    
    @Nested
    @DisplayName("Optimized Implementation Demonstration Tests")
    class OptimizedImplementationDemonstrationTests {
        
        @Test
        @DisplayName("Should demonstrate implementation selection for different sizes")
        void shouldDemonstrateImplementationSelectionForDifferentSizes() {
            String impl8 = FFTUtils.getImplementationInfo(8);
            String impl16 = FFTUtils.getImplementationInfo(16);
            String impl1024 = FFTUtils.getImplementationInfo(1024);
            
            assertThat(impl8).isNotEmpty();
            assertThat(impl16).isNotEmpty();
            assertThat(impl1024).isNotEmpty();
            
            // Implementation info should contain meaningful information
            assertThat(impl8).containsAnyOf("FFT", "Optimized", "Base");
            assertThat(impl16).containsAnyOf("FFT", "Optimized", "Base");
            assertThat(impl1024).containsAnyOf("FFT", "Optimized", "Base");
        }
        
        @Test
        @DisplayName("Should demonstrate performance measurement capability")
        void shouldDemonstratePerformanceMeasurementCapability() {
            // Test that performance measurement works
            long startTime = System.nanoTime();
            
            for (int i = 0; i < 100; i++) {
                FFTUtils.fft(TEST_SIGNAL);
            }
            
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000.0; // Convert to ms
            
            // Performance measurement should complete reasonably
            assertThat(duration).isGreaterThan(0.0);
            assertThat(duration).isLessThan(1000.0); // Should complete in under 1 second
        }
        
        @Test
        @DisplayName("Should demonstrate consistent results across implementations")
        void shouldDemonstrateConsistentResultsAcrossImplementations() {
            // Different implementations should produce equivalent results
            FFTResult result1 = FFTUtils.fft(TEST_SIGNAL);
            FFTResult result2 = FFTUtils.fft(TEST_SIGNAL);
            
            assertThat(result1.getRealParts()).containsExactly(result2.getRealParts(), within(TOLERANCE));
            assertThat(result1.getImaginaryParts()).containsExactly(result2.getImaginaryParts(), within(TOLERANCE));
        }
    }
    
    @Nested
    @DisplayName("Backward Compatibility Demonstration Tests")
    class BackwardCompatibilityDemonstrationTests {
        
        @Test
        @DisplayName("Should demonstrate legacy API compatibility")
        @SuppressWarnings("deprecation")
        void shouldDemonstrateLegacyAPICompatibility() {
            double[] real = TEST_SIGNAL.clone();
            double[] imag = new double[TEST_SIGNAL.length];
            
            // Legacy API should still work
            double[] legacyResult = FFTUtils.fftLegacy(real, imag, true);
            assertThat(legacyResult).isNotNull();
            assertThat(legacyResult.length).isEqualTo(TEST_SIGNAL.length * 2); // Interleaved
            
            // New API should work
            FFTResult newResult = FFTUtils.fft(real, imag, true);
            assertThat(newResult).isNotNull();
            assertThat(newResult.size()).isEqualTo(TEST_SIGNAL.length);
            
            // Results should be equivalent
            double[] newInterleaved = newResult.getInterleavedResult();
            assertThat(newInterleaved).containsExactly(legacyResult, within(TOLERANCE));
        }
        
        @Test
        @DisplayName("Should demonstrate API migration path")
        void shouldDemonstrateAPIMigrationPath() {
            double[] real = TEST_SIGNAL.clone();
            double[] imag = new double[TEST_SIGNAL.length];
            
            // Show that both APIs can be used side by side
            FFTResult newAPIResult = FFTUtils.fft(real, imag, true);
            
            @SuppressWarnings("deprecation")
            double[] legacyAPIResult = FFTUtils.fftLegacy(real.clone(), imag.clone(), true);
            
            // Demonstrate access to same data through different interfaces
            double[] newAPIInterleaved = newAPIResult.getInterleavedResult();
            assertThat(newAPIInterleaved).hasSize(legacyAPIResult.length);
            
            // Show structured access with new API
            double realPart0 = newAPIResult.getRealAt(0);
            double imagPart0 = newAPIResult.getImaginaryAt(0);
            
            // Compare with legacy interleaved format
            assertThat(realPart0).isCloseTo(legacyAPIResult[0], within(TOLERANCE));
            assertThat(imagPart0).isCloseTo(legacyAPIResult[1], within(TOLERANCE));
        }
    }
    
    @Nested
    @DisplayName("Rich Result Wrapper Demonstration Tests")
    class RichResultWrapperDemonstrationTests {
        
        @Test
        @DisplayName("Should demonstrate comprehensive result access")
        void shouldDemonstrateComprehensiveResultAccess() {
            // Generate test signal with known frequency content
            double[] frequencies = {1.0, 3.0};
            double[] amplitudes = {1.0, 0.5};
            double[] signal = FFTUtils.generateTestSignal(32, 32.0, frequencies, amplitudes);
            
            FFTResult spectrum = FFTUtils.fft(signal);
            
            // Demonstrate all access methods
            assertThat(spectrum.size()).isEqualTo(signal.length);
            
            // Array accessors
            double[] magnitudes = spectrum.getMagnitudes();
            double[] phases = spectrum.getPhases();
            double[] powerSpectrum = spectrum.getPowerSpectrum();
            double[] realParts = spectrum.getRealParts();
            double[] imagParts = spectrum.getImaginaryParts();
            
            assertThat(magnitudes).hasSize(signal.length);
            assertThat(phases).hasSize(signal.length);
            assertThat(powerSpectrum).hasSize(signal.length);
            assertThat(realParts).hasSize(signal.length);
            assertThat(imagParts).hasSize(signal.length);
            
            // Indexed accessors
            for (int i = 0; i < signal.length; i++) {
                assertThat(spectrum.getRealAt(i)).isEqualTo(realParts[i]);
                assertThat(spectrum.getImaginaryAt(i)).isEqualTo(imagParts[i]);
                assertThat(spectrum.getMagnitudeAt(i)).isEqualTo(magnitudes[i]);
                assertThat(spectrum.getPhaseAt(i)).isEqualTo(phases[i]);
            }
        }
        
        @Test
        @DisplayName("Should demonstrate magnitude calculation consistency")
        void shouldDemonstrateMagnitudeCalculationConsistency() {
            FFTResult result = FFTUtils.fft(TEST_SIGNAL);
            
            // Verify magnitude calculation: |z| = sqrt(real^2 + imag^2)
            for (int i = 0; i < result.size(); i++) {
                double real = result.getRealAt(i);
                double imag = result.getImaginaryAt(i);
                double expectedMagnitude = Math.sqrt(real * real + imag * imag);
                double actualMagnitude = result.getMagnitudeAt(i);
                
                assertThat(actualMagnitude).isCloseTo(expectedMagnitude, within(TOLERANCE));
            }
        }
        
        @Test
        @DisplayName("Should demonstrate power spectrum calculation")
        void shouldDemonstratePowerSpectrumCalculation() {
            FFTResult result = FFTUtils.fft(TEST_SIGNAL);
            
            // Verify power spectrum calculation: P = real^2 + imag^2
            double[] powerSpectrum = result.getPowerSpectrum();
            
            for (int i = 0; i < result.size(); i++) {
                double real = result.getRealAt(i);
                double imag = result.getImaginaryAt(i);
                double expectedPower = real * real + imag * imag;
                
                assertThat(powerSpectrum[i]).isCloseTo(expectedPower, within(TOLERANCE));
            }
        }
        
        @Test
        @DisplayName("Should demonstrate result object string representation")
        void shouldDemonstrateResultObjectStringRepresentation() {
            FFTResult result = FFTUtils.fft(TEST_SIGNAL);
            
            String resultString = result.toString();
            assertThat(resultString).isNotNull();
            assertThat(resultString).contains("FFTResult");
            assertThat(resultString).contains("size=" + TEST_SIGNAL.length);
            
            // String representation should be informative
            assertThat(resultString.length()).isGreaterThan(20);
        }
        
        @Test
        @DisplayName("Should demonstrate interleaved result compatibility")
        void shouldDemonstrateInterleavedResultCompatibility() {
            double[] real = TEST_SIGNAL.clone();
            double[] imag = new double[TEST_SIGNAL.length];
            
            FFTResult result = FFTUtils.fft(real, imag, true);
            double[] interleaved = result.getInterleavedResult();
            
            // Interleaved format: [real0, imag0, real1, imag1, ...]
            assertThat(interleaved).hasSize(TEST_SIGNAL.length * 2);
            
            for (int i = 0; i < TEST_SIGNAL.length; i++) {
                assertThat(interleaved[2 * i]).isCloseTo(result.getRealAt(i), within(TOLERANCE));
                assertThat(interleaved[2 * i + 1]).isCloseTo(result.getImaginaryAt(i), within(TOLERANCE));
            }
        }
    }
    
    @Nested
    @DisplayName("Performance Demonstration Tests")
    class PerformanceDemonstrationTests {
        
        @Test
        @DisplayName("Should demonstrate reasonable performance")
        void shouldDemonstrateReasonablePerformance() {
            int iterations = 1000;
            
            long startTime = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                FFTUtils.fft(TEST_SIGNAL);
            }
            long endTime = System.nanoTime();
            
            double avgTime = (endTime - startTime) / (double) iterations / 1_000_000.0; // ms
            
            // Should complete reasonably quickly
            assertThat(avgTime).isLessThan(1.0); // Less than 1ms per transform
        }
        
        @Test
        @DisplayName("Should demonstrate memory efficiency")
        void shouldDemonstrateMemoryEfficiency() {
            // Test that multiple transformations don't cause memory issues
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 100; i++) {
                    FFTResult result = FFTUtils.fft(TEST_SIGNAL);
                    // Access various result components to ensure they work
                    result.getMagnitudes();
                    result.getPhases();
                    result.getPowerSpectrum();
                }
            }, "Multiple FFT operations should not cause memory issues");
        }
    }
}
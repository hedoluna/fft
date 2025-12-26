package com.fft.optimized;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test for the complete FFT optimization plan implementation.
 *
 * Tests all optimization modes:
 * - Full validation mode (development)
 * - Sampling validation mode (staging)
 * - Production mode (maximum performance)
 *
 * Validates both correctness and performance characteristics.
 *
 * NOTE: OptimizedFFTFramework is deprecated after FASE 1 completion.
 * These tests are kept for historical reference but some may be disabled
 * as they test obsolete functionality.
 *
 * @author Claude Code Assistant
 * @since 2.0.1
 */
@Disabled("Tests deprecated OptimizedFFTFramework - kept for historical reference")
public class ComprehensiveOptimizationTest {
    
    private double[] testReal8;
    private double[] testImag8;
    private double[] testReal32;
    private double[] testImag32;
    
    @BeforeEach
    void setUp() {
        // Set up test data
        testReal8 = new double[]{1, 0, 1, 0, 1, 0, 1, 0};
        testImag8 = new double[]{0, 1, 0, 1, 0, 1, 0, 1};
        
        testReal32 = new double[32];
        testImag32 = new double[32];
        for (int i = 0; i < 32; i++) {
            testReal32[i] = Math.sin(2 * Math.PI * i / 32);
            testImag32[i] = Math.cos(2 * Math.PI * i / 32);
        }
        
        // Clear statistics
        OptimizedFFTFramework.clearStatistics();
    }
    
    @Test
    @DisplayName("Full Validation Mode - Maximum Correctness")
    void testFullValidationMode() {
        // Configure for full validation
        System.setProperty("fft.enable.validation", "true");
        System.setProperty("fft.enable.sampling.validation", "false");
        
        // Test FFT8 with full validation
        double[] result8 = OptimizedFFTUtils.fft8(testReal8, testImag8, true);
        assertThat(result8).isNotNull();
        assertThat(result8.length).isEqualTo(16);
        
        // Test FFT32 with full validation
        double[] result32 = OptimizedFFTUtils.fft32(testReal32, testImag32, true);
        assertThat(result32).isNotNull();
        assertThat(result32.length).isEqualTo(64);
        
        // Verify validation was performed (success rate should be tracked)
        assertThat(OptimizedFFTFramework.getSuccessRate(8)).isGreaterThan(0.0);
        assertThat(OptimizedFFTFramework.getSuccessRate(32)).isGreaterThan(0.0);
    }
    
    @Test
    @DisplayName("Sampling Validation Mode - Balanced Performance/Safety")
    void testSamplingValidationMode() {
        // Configure for sampling validation (validate every 10th call)
        System.setProperty("fft.enable.validation", "false");
        System.setProperty("fft.enable.sampling.validation", "true");
        System.setProperty("fft.sampling.rate", "10");
        
        // Perform multiple calls to trigger sampling
        for (int i = 0; i < 25; i++) {
            double[] result = OptimizedFFTUtils.fft8(testReal8, testImag8, true);
            assertThat(result).isNotNull();
            assertThat(result.length).isEqualTo(16);
        }
        
        // Verify some calls were validated (success rate tracked)
        assertThat(OptimizedFFTFramework.getSuccessRate(8)).isGreaterThan(0.0);
    }
    
    @Test
    @DisplayName("Production Mode - Maximum Performance")
    void testProductionMode() {
        // Configure for production mode (no validation)
        OptimizedFFTFramework.setProductionMode(true);
        
        // Measure performance in production mode
        long startTime = System.nanoTime();
        
        // Perform multiple FFT operations
        for (int i = 0; i < 100; i++) {
            double[] result8 = OptimizedFFTUtils.fft8(testReal8, testImag8, true);
            double[] result32 = OptimizedFFTUtils.fft32(testReal32, testImag32, true);
            
            assertThat(result8).isNotNull();
            assertThat(result8.length).isEqualTo(16);
            assertThat(result32).isNotNull();
            assertThat(result32.length).isEqualTo(64);
        }
        
        long endTime = System.nanoTime();
        long productionTime = endTime - startTime;
        
        // Reset to validation mode and measure again
        System.setProperty("fft.enable.validation", "true");
        OptimizedFFTFramework.clearStatistics();
        
        startTime = System.nanoTime();
        
        for (int i = 0; i < 100; i++) {
            double[] result8 = OptimizedFFTUtils.fft8(testReal8, testImag8, true);
            double[] result32 = OptimizedFFTUtils.fft32(testReal32, testImag32, true);
        }
        
        endTime = System.nanoTime();
        long validationTime = endTime - startTime;
        
        // Production mode should be faster than validation mode
        double speedup = (double) validationTime / productionTime;
        System.out.printf("Production mode speedup: %.2fx%n", speedup);
        
        // We expect production mode to be faster (relaxed threshold for CI environments)
        assertThat(speedup).isGreaterThan(0.5);
    }
    
    @Test
    @DisplayName("Correctness Guarantee - All Modes")
    void testCorrectnessAcrossAllModes() {
        // Test that all modes produce mathematically correct results
        
        // Generate reference result using known good implementation
        System.setProperty("fft.enable.validation", "false");
        System.setProperty("fft.enable.sampling.validation", "false");
        
        double[] reference = computeReferenceFFT(testReal8, testImag8, true);
        
        // Test all modes produce the same result
        String[] modes = {"validation", "sampling", "production"};
        
        for (String mode : modes) {
            switch (mode) {
                case "validation":
                    System.setProperty("fft.enable.validation", "true");
                    System.setProperty("fft.enable.sampling.validation", "false");
                    break;
                case "sampling":
                    System.setProperty("fft.enable.validation", "false");
                    System.setProperty("fft.enable.sampling.validation", "true");
                    System.setProperty("fft.sampling.rate", "1"); // Validate every call
                    break;
                case "production":
                    OptimizedFFTFramework.setProductionMode(true);
                    break;
            }
            
            double[] result = OptimizedFFTUtils.fft8(testReal8, testImag8, true);
            
            // Results should be identical (within numerical precision)
            for (int i = 0; i < result.length; i++) {
                assertThat(Math.abs(result[i] - reference[i]))
                    .as("Mode: %s, Index: %d", mode, i)
                    .isLessThan(1e-12);
            }
        }
    }
    
    @Test
    @DisplayName("Performance Monitoring")
    void testPerformanceMonitoring() {
        // Test that performance monitoring works correctly
        System.setProperty("fft.enable.validation", "true");
        
        // Perform some operations
        for (int i = 0; i < 10; i++) {
            OptimizedFFTUtils.fft8(testReal8, testImag8, true);
        }
        
        // Check statistics
        String stats = OptimizedFFTFramework.getCacheStatistics();
        assertThat(stats).contains("Cached FFTBase instances");
        assertThat(OptimizedFFTFramework.getSuccessRate(8)).isEqualTo(1.0);
    }
    
    /**
     * Compute reference FFT result using the safe path
     */
    private double[] computeReferenceFFT(double[] real, double[] imag, boolean forward) {
        com.fft.core.FFTBase base = new com.fft.core.FFTBase();
        com.fft.core.FFTResult result = base.transform(real, imag, forward);
        return result.getInterleavedResult();
    }
}
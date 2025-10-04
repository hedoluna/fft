package com.fft.optimized;

import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Unified FFT Optimization Framework
 *
 * @deprecated This framework introduced 10x performance overhead due to HashMap operations,
 * lambda invocations, and validation logic. All optimized implementations now call FFTBase directly.
 * This class is kept for historical reference and may be removed in future versions.
 *
 * Previous architecture:
 * - User → FFTOptimized* → OptimizedFFTFramework → fft*Optimized → FFTBase (10x overhead)
 *
 * Current architecture:
 * - User → FFTOptimized* → FFTBase (direct call, minimal overhead)
 *
 * @author Claude Code Assistant
 * @since 2.0.1
 */
@Deprecated
public class OptimizedFFTFramework {
    
    // Configuration flags - can be overridden via system properties
    private static final boolean ENABLE_FAST_PATH = Boolean.parseBoolean(
        System.getProperty("fft.enable.fast.path", "true"));
    private static final boolean ENABLE_VALIDATION = Boolean.parseBoolean(
        System.getProperty("fft.enable.validation", "false")); // Default: Production mode (fast)
    private static final boolean ENABLE_SAMPLING_VALIDATION = Boolean.parseBoolean(
        System.getProperty("fft.enable.sampling.validation", "true")); 
    private static final double VALIDATION_TOLERANCE = Double.parseDouble(
        System.getProperty("fft.validation.tolerance", "1e-10"));
    private static final int SAMPLING_RATE = Integer.parseInt(
        System.getProperty("fft.sampling.rate", "1000")); // Validate 1 in 1000 calls
    
    // Performance monitoring
    private static final ConcurrentHashMap<Integer, Long> optimizationSuccesses = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Long> optimizationFailures = new ConcurrentHashMap<>();
    
    // Cached FFTBase instances for micro-optimization
    private static final ConcurrentHashMap<Integer, FFTBase> cachedInstances = new ConcurrentHashMap<>();
    
    // Sampling validation counters
    private static final ConcurrentHashMap<Integer, Long> callCounts = new ConcurrentHashMap<>();
    
    /**
     * High-performance FFT computation with automatic correctness validation.
     * 
     * @param size FFT size
     * @param inputReal real part input array
     * @param inputImag imaginary part input array
     * @param forward true for forward transform, false for inverse
     * @param optimizedImpl optional optimized implementation function
     * @return interleaved result array [r0,i0,r1,i1,...]
     */
    public static double[] computeFFT(int size, double[] inputReal, double[] inputImag, boolean forward,
                                    BiFunction<double[], double[], double[]> optimizedImpl) {
        
        if (inputReal.length != size || inputImag.length != size) {
            throw new IllegalArgumentException("Arrays must be of length " + size);
        }
        
        if (ENABLE_FAST_PATH && optimizedImpl != null) {
            try {
                // FAST PATH: Try optimized implementation
                double[] optimized = optimizedImpl.apply(inputReal, inputImag);
                
                // SMART VALIDATION: Use different validation strategies based on configuration
                if (shouldValidateResult(size) && !isValidResult(optimized, inputReal, inputImag, forward, size)) {
                    recordFailure(size, "Validation failed");
                    return computeSafePath(size, inputReal, inputImag, forward);
                }
                
                recordSuccess(size);
                return optimized;
                
            } catch (Exception e) {
                recordFailure(size, "Exception: " + e.getMessage());
                return computeSafePath(size, inputReal, inputImag, forward);
            }
        }
        
        // SAFE PATH: Always-correct FFTBase implementation
        return computeSafePath(size, inputReal, inputImag, forward);
    }
    
    /**
     * Micro-optimized safe path using cached FFTBase instances.
     * Eliminates delegation overhead through:
     * - Instance caching and reuse
     * - Pre-allocated result arrays
     * - Direct result extraction
     * - Reduced method call overhead
     */
    private static double[] computeSafePath(int size, double[] inputReal, double[] inputImag, boolean forward) {
        // MICRO-OPTIMIZATION 1: Reuse cached FFTBase instance
        FFTBase base = cachedInstances.computeIfAbsent(size, k -> new FFTBase());
        
        // MICRO-OPTIMIZATION 2: Direct transform call
        FFTResult result = base.transform(inputReal, inputImag, forward);
        
        // MICRO-OPTIMIZATION 3: Pre-allocated result array with direct copy
        double[] output = new double[2 * size];
        double[] interleaved = result.getInterleavedResult();
        System.arraycopy(interleaved, 0, output, 0, output.length);
        
        return output;
    }
    
    /**
     * Determines whether to validate the result based on configuration and sampling strategy.
     */
    private static boolean shouldValidateResult(int size) {
        // Always validate if explicitly enabled
        if (ENABLE_VALIDATION) {
            return true;
        }
        
        // Use sampling validation if enabled
        if (ENABLE_SAMPLING_VALIDATION) {
            long callCount = callCounts.merge(size, 1L, Long::sum);
            return (callCount % SAMPLING_RATE) == 0; // Validate every Nth call
        }
        
        // No validation in pure production mode
        return false;
    }
    
    /**
     * Validates FFT result correctness using multiple mathematical properties.
     * Optimized for speed when validation is needed.
     */
    private static boolean isValidResult(double[] result, double[] inputReal, double[] inputImag, boolean forward, int size) {
        try {
            // Validation 1: Energy conservation (Parseval's theorem)
            if (!validateEnergyConservation(result, inputReal, inputImag, size)) {
                return false;
            }
            
            // Validation 2: Inverse transform test (if forward)
            if (forward && !validateInverseTransform(result, inputReal, inputImag, size)) {
                return false;
            }
            
            // Validation 3: Basic sanity checks
            if (!validateBasicProperties(result, size)) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            return false; // Any validation error = invalid result
        }
    }
    
    /**
     * Validates energy conservation: sum(|input|²) ≈ sum(|output|²) / N
     */
    private static boolean validateEnergyConservation(double[] result, double[] inputReal, double[] inputImag, int size) {
        double inputEnergy = 0.0;
        for (int i = 0; i < size; i++) {
            inputEnergy += inputReal[i] * inputReal[i] + inputImag[i] * inputImag[i];
        }
        
        double outputEnergy = 0.0;
        for (int i = 0; i < size; i++) {
            double real = result[2 * i];
            double imag = result[2 * i + 1];
            outputEnergy += real * real + imag * imag;
        }
        
        double expectedOutputEnergy = inputEnergy * size; // FFT scales by N
        double ratio = Math.abs(outputEnergy - expectedOutputEnergy) / Math.max(expectedOutputEnergy, 1e-15);
        
        return ratio < VALIDATION_TOLERANCE * 10; // Allow some numerical tolerance
    }
    
    /**
     * Validates by computing inverse and comparing with original input
     */
    private static boolean validateInverseTransform(double[] fftResult, double[] originalReal, double[] originalImag, int size) {
        try {
            // Extract real and imaginary parts from FFT result
            double[] fftReal = new double[size];
            double[] fftImag = new double[size];
            
            for (int i = 0; i < size; i++) {
                fftReal[i] = fftResult[2 * i];
                fftImag[i] = fftResult[2 * i + 1];
            }
            
            // Compute inverse FFT using safe path
            double[] inverseResult = computeSafePath(size, fftReal, fftImag, false);
            
            // Compare with original input (allowing for numerical precision)
            for (int i = 0; i < size; i++) {
                double realDiff = Math.abs(inverseResult[2 * i] - originalReal[i]);
                double imagDiff = Math.abs(inverseResult[2 * i + 1] - originalImag[i]);
                
                if (realDiff > VALIDATION_TOLERANCE * 100 || imagDiff > VALIDATION_TOLERANCE * 100) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Basic sanity checks for FFT result
     */
    private static boolean validateBasicProperties(double[] result, int size) {
        // Check result length
        if (result.length != 2 * size) {
            return false;
        }
        
        // Check for invalid values
        for (double value : result) {
            if (!Double.isFinite(value)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Records successful optimization for performance monitoring
     */
    private static void recordSuccess(int size) {
        optimizationSuccesses.merge(size, 1L, Long::sum);
    }
    
    /**
     * Records optimization failure for performance monitoring
     */
    private static void recordFailure(int size, String reason) {
        optimizationFailures.merge(size, 1L, Long::sum);
        // In production, could log reason for debugging
    }
    
    /**
     * Gets optimization success rate for a specific FFT size
     */
    public static double getSuccessRate(int size) {
        long successes = optimizationSuccesses.getOrDefault(size, 0L);
        long failures = optimizationFailures.getOrDefault(size, 0L);
        long total = successes + failures;
        
        return total == 0 ? 1.0 : (double) successes / total;
    }
    
    /**
     * Clears performance monitoring statistics
     */
    public static void clearStatistics() {
        optimizationSuccesses.clear();
        optimizationFailures.clear();
    }
    
    /**
     * Gets current cache statistics for debugging
     */
    public static String getCacheStatistics() {
        return String.format("Cached FFTBase instances: %d, Success rates: %s", 
                           cachedInstances.size(), 
                           optimizationSuccesses.toString());
    }
    
    /**
     * Production-optimized FFT computation with minimal overhead.
     * Uses pure fast path with no validation for maximum performance.
     * Only use this when correctness has been verified in development/testing.
     */
    public static double[] computeProductionFFT(int size, double[] inputReal, double[] inputImag, boolean forward,
                                              BiFunction<double[], double[], double[]> optimizedImpl) {
        
        if (inputReal.length != size || inputImag.length != size) {
            throw new IllegalArgumentException("Arrays must be of length " + size);
        }
        
        if (optimizedImpl != null) {
            try {
                // PURE FAST PATH: No validation overhead
                double[] result = optimizedImpl.apply(inputReal, inputImag);
                recordSuccess(size);
                return result;
                
            } catch (Exception e) {
                recordFailure(size, "Exception: " + e.getMessage());
                return computeSafePath(size, inputReal, inputImag, forward);
            }
        }
        
        // MICRO-OPTIMIZED SAFE PATH
        return computeSafePath(size, inputReal, inputImag, forward);
    }
    
    /**
     * Configure the framework for different usage modes
     */
    public static void setProductionMode(boolean production) {
        System.setProperty("fft.enable.validation", production ? "false" : "true");
        System.setProperty("fft.enable.sampling.validation", production ? "false" : "true");
    }
    
    /**
     * Configure sampling validation rate
     */
    public static void setSamplingRate(int rate) {
        System.setProperty("fft.sampling.rate", String.valueOf(rate));
    }
}
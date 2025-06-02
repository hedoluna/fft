package com.fft.demo;

import com.fft.core.FFT;
import com.fft.core.FFTResult;
import com.fft.factory.DefaultFFTFactory;
import com.fft.utils.FFTUtils;

/**
 * Demonstration of the refactored FFT library functionality.
 * 
 * <p>This class showcases the key improvements made during the refactoring process,
 * including the new API, factory pattern, and optimized implementations.</p>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
public class RefactoringDemo {
    
    public static void main(String[] args) {
        System.out.println("=== FFT Library Refactoring Demonstration ===\n");
        
        demonstrateNewAPI();
        demonstrateFactoryPattern();
        demonstrateOptimizedImplementations();
        demonstrateBackwardCompatibility();
        demonstrateResultWrapper();
        
        System.out.println("=== Refactoring Demonstration Complete ===");
    }
    
    private static void demonstrateNewAPI() {
        System.out.println("1. New Type-Safe API:");
        System.out.println("--------------------");
        
        double[] signal = {1, 2, 3, 4, 5, 6, 7, 8};
        
        // New API - returns structured FFTResult object
        FFTResult result = FFTUtils.fft(signal);
        
        System.out.printf("Signal size: %d\n", result.size());
        System.out.printf("DC component magnitude: %.3f\n", result.getMagnitudeAt(0));
        System.out.printf("First few magnitudes: [%.3f, %.3f, %.3f]\n", 
                         result.getMagnitudeAt(0), result.getMagnitudeAt(1), result.getMagnitudeAt(2));
        System.out.println();
    }
    
    private static void demonstrateFactoryPattern() {
        System.out.println("2. Factory Pattern with Automatic Implementation Selection:");
        System.out.println("-----------------------------------------------------------");
        
        DefaultFFTFactory factory = new DefaultFFTFactory();
        
        // Factory automatically selects the best implementation
        FFT fft8 = factory.createFFT(8);
        FFT fft16 = factory.createFFT(16);
        FFT fft1024 = factory.createFFT(1024);
        
        System.out.printf("Size 8: %s\n", fft8.getDescription());
        System.out.printf("Size 16: %s\n", fft16.getDescription());
        System.out.printf("Size 1024: %s\n", fft1024.getDescription());
        
        System.out.println("\nImplementation Registry Report:");
        System.out.println(factory.getRegistryReport());
    }
    
    private static void demonstrateOptimizedImplementations() {
        System.out.println("3. Optimized Implementation Performance:");
        System.out.println("----------------------------------------");
        
        double[] signal = {1, 2, 3, 4, 5, 6, 7, 8};
        
        // Compare implementations
        System.out.printf("Implementation for size 8: %s\n", FFTUtils.getImplementationInfo(8));
        System.out.printf("Implementation for size 16: %s\n", FFTUtils.getImplementationInfo(16));
        
        // Performance demonstration
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            FFTUtils.fft(signal);
        }
        long endTime = System.nanoTime();
        
        System.out.printf("10,000 size-8 FFTs completed in %.2f ms\n", (endTime - startTime) / 1_000_000.0);
        System.out.println();
    }
    
    @SuppressWarnings("deprecation")
    private static void demonstrateBackwardCompatibility() {
        System.out.println("4. Backward Compatibility:");
        System.out.println("---------------------------");
        
        double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] imag = new double[8];
        
        // Legacy API still works
        double[] legacyResult = FFTUtils.fftLegacy(real, imag, true);
        
        // New API
        FFTResult newResult = FFTUtils.fft(real, imag, true);
        
        System.out.printf("Legacy result length: %d\n", legacyResult.length);
        System.out.printf("New result size: %d\n", newResult.size());
        System.out.printf("Results match: %s\n", 
                         java.util.Arrays.equals(legacyResult, newResult.getInterleavedResult()));
        System.out.println();
    }
    
    private static void demonstrateResultWrapper() {
        System.out.println("5. Rich FFTResult Wrapper:");
        System.out.println("---------------------------");
        
        // Create a test signal with known frequency content
        double[] frequencies = {1.0, 3.0};
        double[] amplitudes = {1.0, 0.5};
        double[] signal = FFTUtils.generateTestSignal(32, 32.0, frequencies, amplitudes);
        
        FFTResult spectrum = FFTUtils.fft(signal);
        
        System.out.printf("Signal length: %d\n", signal.length);
        System.out.printf("Spectrum size: %d\n", spectrum.size());
        
        // Easy access to different representations
        double[] magnitudes = spectrum.getMagnitudes();
        double[] phases = spectrum.getPhases();
        double[] powerSpectrum = spectrum.getPowerSpectrum();
        
        System.out.printf("Peak magnitude at bin 1: %.3f\n", magnitudes[1]);
        System.out.printf("Peak magnitude at bin 3: %.3f\n", magnitudes[3]);
        System.out.printf("Phase at bin 1: %.3f radians\n", phases[1]);
        System.out.printf("Power at bin 3: %.3f\n", powerSpectrum[3]);
        
        System.out.printf("Result object: %s\n", spectrum.toString());
        System.out.println();
    }
}
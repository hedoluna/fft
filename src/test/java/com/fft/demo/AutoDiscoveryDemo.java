package com.fft.demo;

import com.fft.factory.DefaultFFTFactory;
import com.fft.factory.FFTImplementationDiscovery;
import com.fft.core.FFT;
import com.fft.core.FFTResult;

/**
 * Demonstration of the auto-discovery mechanism for FFT implementations.
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
public class AutoDiscoveryDemo {
    
    public static void main(String[] args) {
        System.out.println("=== FFT Auto-Discovery Demo ===\n");
        
        // Print discovery report
        String discoveryReport = FFTImplementationDiscovery.getDiscoveryReport();
        System.out.println(discoveryReport);
        
        // Create factory (auto-discovery happens during construction)
        DefaultFFTFactory factory = new DefaultFFTFactory();
        
        // Print factory registry report
        String registryReport = factory.getRegistryReport();
        System.out.println(registryReport);
        
        // Test discovered implementations
        testImplementation(factory, 8);
        testImplementation(factory, 32);
        testImplementation(factory, 64); // Should fall back to generic
    }
    
    private static void testImplementation(DefaultFFTFactory factory, int size) {
        System.out.println("\n--- Testing Size " + size + " ---");
        
        try {
            FFT fft = factory.createFFT(size);
            System.out.println("Implementation: " + fft.getDescription());
            
            // Generate test signal
            double[] real = new double[size];
            double[] imag = new double[size];
            
            // Simple test: DC signal
            for (int i = 0; i < size; i++) {
                real[i] = 1.0;
            }
            
            // Transform
            long startTime = System.nanoTime();
            FFTResult result = fft.transform(real, imag, true);
            long endTime = System.nanoTime();
            
            double timeMs = (endTime - startTime) / 1_000_000.0;
            
            System.out.println("Transform time: " + String.format("%.3f ms", timeMs));
            System.out.println("DC magnitude: " + String.format("%.3f", result.getMagnitudeAt(0)));
            System.out.println("Success: Transform completed");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
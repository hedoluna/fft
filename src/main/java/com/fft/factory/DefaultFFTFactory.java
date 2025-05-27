package com.fft.factory;

import com.fft.core.FFT;
import com.fft.core.FFTBase;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Default implementation of the FFT factory.
 * 
 * <p>This factory provides automatic selection of the most appropriate FFT implementation
 * based on input size. It maintains a registry of implementations with priority-based
 * selection when multiple implementations are available for the same size.</p>
 * 
 * <h3>Implementation Selection Strategy:</h3>
 * <ol>
 * <li>Look for registered optimized implementations for the exact size</li>
 * <li>Select the implementation with the highest priority</li>
 * <li>Fall back to the generic FFTBase implementation if no optimized version exists</li>
 * </ol>
 * 
 * <h3>Thread Safety:</h3>
 * <p>This implementation is thread-safe and can be used concurrently from multiple threads.
 * Registration operations are synchronized to ensure consistency.</p>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 * @see FFTFactory for interface details
 * @see FFTBase for fallback implementation
 */
public class DefaultFFTFactory implements FFTFactory {
    
    /**
     * Implementation entry with priority support.
     */
    private static class ImplementationEntry {
        final Supplier<FFT> supplier;
        final int priority;
        
        ImplementationEntry(Supplier<FFT> supplier, int priority) {
            this.supplier = supplier;
            this.priority = priority;
        }
    }
    
    private final Map<Integer, List<ImplementationEntry>> implementations = new ConcurrentHashMap<>();
    private final FFT fallbackImplementation = new FFTBase();
    
    /**
     * Creates a new default factory with standard optimized implementations registered.
     */
    public DefaultFFTFactory() {
        registerDefaultImplementations();
    }
    
    /**
     * Registers the standard set of optimized implementations.
     * This method will be updated as optimized implementations are moved to the new package structure.
     */
    private void registerDefaultImplementations() {
        // Register optimized implementations with high priority
        registerImplementation(8, com.fft.optimized.FFTOptimized8::new, 50);
        
        // Register FFTBase as fallback for all power-of-2 sizes up to 8192
        for (int size = 2; size <= 8192; size *= 2) {
            registerImplementation(size, FFTBase::new, 0); // Low priority fallback
        }
    }
    
    @Override
    public FFT createFFT(int size) {
        if (!isPowerOfTwo(size)) {
            throw new IllegalArgumentException("Array length must be a power of 2, got: " + size);
        }
        
        List<ImplementationEntry> entries = implementations.get(size);
        if (entries != null && !entries.isEmpty()) {
            // Return the highest priority implementation
            ImplementationEntry best = entries.stream()
                .max(Comparator.comparingInt(e -> e.priority))
                .orElse(null);
            
            if (best != null) {
                return best.supplier.get();
            }
        }
        
        // Fallback to generic implementation
        return new FFTBase();
    }
    
    @Override
    public List<Integer> getSupportedSizes() {
        Set<Integer> sizes = new TreeSet<>(implementations.keySet());
        
        // Add common power-of-2 sizes that are supported by fallback
        for (int size = 2; size <= 8192; size *= 2) {
            sizes.add(size);
        }
        
        return new ArrayList<>(sizes);
    }
    
    @Override
    public void registerImplementation(int size, Supplier<FFT> implementation) {
        registerImplementation(size, implementation, 10); // Default priority
    }
    
    @Override
    public synchronized void registerImplementation(int size, Supplier<FFT> implementation, int priority) {
        if (!isPowerOfTwo(size)) {
            throw new IllegalArgumentException("Size must be a power of 2, got: " + size);
        }
        
        if (implementation == null) {
            throw new IllegalArgumentException("Implementation supplier cannot be null");
        }
        
        implementations.computeIfAbsent(size, k -> new ArrayList<>())
                      .add(new ImplementationEntry(implementation, priority));
        
        // Sort by priority (highest first)
        implementations.get(size).sort((a, b) -> Integer.compare(b.priority, a.priority));
    }
    
    @Override
    public boolean supportsSize(int size) {
        return isPowerOfTwo(size);
    }
    
    @Override
    public String getImplementationInfo(int size) {
        if (!isPowerOfTwo(size)) {
            return "Invalid size (not power of 2)";
        }
        
        List<ImplementationEntry> entries = implementations.get(size);
        if (entries != null && !entries.isEmpty()) {
            ImplementationEntry best = entries.get(0); // Already sorted by priority
            FFT instance = best.supplier.get();
            return String.format("%s (priority: %d)", instance.getDescription(), best.priority);
        }
        
        return "FFTBase (generic fallback implementation for size " + size + ")";
    }
    
    @Override
    public synchronized boolean unregisterImplementations(int size) {
        return implementations.remove(size) != null;
    }
    
    @Override
    public int getImplementationCount(int size) {
        List<ImplementationEntry> entries = implementations.get(size);
        return entries != null ? entries.size() : 0;
    }
    
    /**
     * Checks if a number is a power of 2.
     * 
     * @param n the number to check
     * @return true if n is a power of 2, false otherwise
     */
    private static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
    
    /**
     * Returns a detailed report of all registered implementations.
     * 
     * @return string containing implementation registry details
     */
    public String getRegistryReport() {
        StringBuilder report = new StringBuilder();
        report.append("FFT Factory Implementation Registry:\n");
        report.append("=====================================\n");
        
        List<Integer> sizes = getSupportedSizes();
        for (Integer size : sizes) {
            report.append(String.format("Size %d: %s\n", size, getImplementationInfo(size)));
            
            List<ImplementationEntry> entries = implementations.get(size);
            if (entries != null && entries.size() > 1) {
                report.append("  Alternative implementations:\n");
                for (int i = 1; i < entries.size(); i++) {
                    ImplementationEntry entry = entries.get(i);
                    FFT instance = entry.supplier.get();
                    report.append(String.format("    - %s (priority: %d)\n", 
                                               instance.getDescription(), entry.priority));
                }
            }
        }
        
        return report.toString();
    }
}
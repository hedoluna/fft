package com.fft.core;

/**
 * Core Fast Fourier Transform interface.
 * 
 * <p>This interface defines the contract for all FFT implementations in the library.
 * It provides a unified API for performing forward and inverse FFT operations,
 * enabling polymorphic usage of different optimization strategies.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 * <li><b>Type Safety:</b> Strong typing for complex number operations</li>
 * <li><b>Polymorphism:</b> Unified interface for all implementation strategies</li>
 * <li><b>Size Validation:</b> Built-in support for size checking and validation</li>
 * <li><b>Result Wrapper:</b> Returns structured FFTResult objects for easy processing</li>
 * </ul>
 * 
 * <h3>Implementation Guidelines:</h3>
 * <ul>
 * <li>All implementations must support both forward and inverse transforms</li>
 * <li>Input arrays must be power-of-2 in length</li>
 * <li>Results should maintain numerical precision within reasonable bounds</li>
 * <li>Implementations should handle edge cases gracefully</li>
 * </ul>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 * @see FFTResult for result structure details
 * @see FFTBase for reference implementation
 */
public interface FFT {
    
    /**
     * Performs FFT on complex input data.
     * 
     * @param real real parts of the input signal
     * @param imaginary imaginary parts of the input signal  
     * @param forward true for forward transform, false for inverse
     * @return FFT result containing the transformed data
     * @throws IllegalArgumentException if arrays have different lengths or invalid size
     */
    FFTResult transform(double[] real, double[] imaginary, boolean forward);
    
    /**
     * Performs FFT on real-valued input (imaginary part assumed zero).
     * 
     * @param real real-valued input signal
     * @param forward true for forward transform, false for inverse
     * @return FFT result containing the transformed data
     * @throws IllegalArgumentException if array size is not a power of 2
     */
    FFTResult transform(double[] real, boolean forward);
    
    /**
     * Performs forward FFT on real-valued input.
     * 
     * @param real real-valued input signal
     * @return FFT result containing the transformed data
     * @throws IllegalArgumentException if array size is not a power of 2
     */
    default FFTResult transform(double[] real) {
        return transform(real, true);
    }
    
    /**
     * Returns the specific size this implementation is optimized for.
     * Returns -1 if the implementation supports arbitrary power-of-2 sizes.
     * 
     * @return the optimized size, or -1 for generic implementations
     */
    int getSupportedSize();
    
    /**
     * Checks if this implementation supports the given size.
     * 
     * @param size the array size to check
     * @return true if this implementation can handle the given size
     */
    boolean supportsSize(int size);
    
    /**
     * Returns a human-readable description of this implementation.
     * 
     * @return implementation description
     */
    default String getDescription() {
        int supportedSize = getSupportedSize();
        if (supportedSize == -1) {
            return "Generic FFT implementation (supports any power-of-2 size)";
        } else {
            return "Optimized FFT implementation (size " + supportedSize + ")";
        }
    }
}
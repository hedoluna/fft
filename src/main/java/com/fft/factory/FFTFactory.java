package com.fft.factory;

import com.fft.core.FFT;
import java.util.List;
import java.util.function.Supplier;

/**
 * Factory interface for creating FFT implementations.
 * 
 * <p>This interface defines the contract for FFT factories that can create
 * appropriate FFT implementations based on input size. The factory pattern
 * allows for extensible implementation selection and registration of new
 * optimized implementations.</p>
 * 
 * <h3>Design Goals:</h3>
 * <ul>
 * <li><b>Extensibility:</b> Allow runtime registration of new implementations</li>
 * <li><b>Performance:</b> Automatic selection of optimal implementations</li>
 * <li><b>Flexibility:</b> Support for multiple implementations per size</li>
 * <li><b>Discoverability:</b> Easy introspection of available implementations</li>
 * </ul>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 * @see DefaultFFTFactory for default implementation
 * @see FFT for implementation interface
 */
public interface FFTFactory {
    
    /**
     * Creates an FFT implementation suitable for the given size.
     * 
     * @param size the array size for which to create an FFT implementation
     * @return an FFT implementation optimized for the given size
     * @throws IllegalArgumentException if no implementation supports the given size
     */
    FFT createFFT(int size);
    
    /**
     * Returns a list of all sizes supported by this factory.
     * 
     * @return sorted list of supported sizes
     */
    List<Integer> getSupportedSizes();
    
    /**
     * Registers a new FFT implementation for a specific size.
     * 
     * @param size the size this implementation supports
     * @param implementation supplier that creates the implementation instance
     * @throws IllegalArgumentException if size is not a power of 2
     */
    void registerImplementation(int size, Supplier<FFT> implementation);
    
    /**
     * Registers a new FFT implementation with priority.
     * Higher priority implementations are preferred when multiple
     * implementations exist for the same size.
     * 
     * @param size the size this implementation supports
     * @param implementation supplier that creates the implementation instance
     * @param priority priority level (higher values = higher priority)
     * @throws IllegalArgumentException if size is not a power of 2
     */
    void registerImplementation(int size, Supplier<FFT> implementation, int priority);
    
    /**
     * Checks if this factory can create an implementation for the given size.
     * 
     * @param size the array size to check
     * @return true if an implementation is available for this size
     */
    boolean supportsSize(int size);
    
    /**
     * Returns information about the implementation that would be used for a given size.
     * 
     * @param size the array size
     * @return description of the implementation that would be selected
     */
    String getImplementationInfo(int size);
    
    /**
     * Removes all registered implementations for a given size.
     * 
     * @param size the size for which to remove implementations
     * @return true if any implementations were removed
     */
    boolean unregisterImplementations(int size);
    
    /**
     * Returns the number of registered implementations for a given size.
     * 
     * @param size the size to check
     * @return number of registered implementations
     */
    int getImplementationCount(int size);
}
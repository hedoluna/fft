package com.fft.factory;

import java.lang.annotation.*;

/**
 * Annotation to mark FFT implementation classes for auto-discovery.
 * 
 * <p>This annotation provides metadata for automatic registration of FFT implementations
 * in the factory system. Annotated classes will be discovered at runtime and automatically
 * registered with the appropriate priority.</p>
 * 
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * @FFTImplementation(size = 1024, priority = 50, description = "Optimized for size 1024")
 * public class FFTOptimized1024 implements FFT {
 *     // Implementation
 * }
 * }</pre>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 * @see DefaultFFTFactory
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface FFTImplementation {
    
    /**
     * The FFT size this implementation is optimized for.
     * Must be a power of 2.
     * 
     * @return the supported FFT size
     */
    int size();
    
    /**
     * Priority for this implementation. Higher values indicate higher priority.
     * When multiple implementations exist for the same size, the one with the
     * highest priority will be selected.
     * 
     * <p>Recommended priority ranges:</p>
     * <ul>
     * <li>0-10: Generic implementations</li>
     * <li>10-50: Standard optimized implementations</li>
     * <li>50-100: Highly optimized implementations</li>
     * <li>100+: Experimental or specialized implementations</li>
     * </ul>
     * 
     * @return the priority value (default: 10)
     */
    int priority() default 10;
    
    /**
     * Optional description of this implementation.
     * Used for debugging and reporting purposes.
     * 
     * @return human-readable description
     */
    String description() default "";
    
    /**
     * Whether this implementation should be automatically registered.
     * If false, the implementation must be manually registered.
     * 
     * @return true to enable auto-registration (default: true)
     */
    boolean autoRegister() default true;
    
    /**
     * Optional performance characteristics metadata.
     * This can be used for adaptive selection algorithms.
     * 
     * @return performance characteristics as key-value pairs
     */
    String[] characteristics() default {};
}
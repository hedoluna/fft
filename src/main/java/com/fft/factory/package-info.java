/**
 * Factory pattern and automatic FFT implementation discovery.
 *
 * <p>This package implements a flexible factory pattern that automatically selects the optimal
 * FFT implementation based on input size. Features include auto-discovery of implementations,
 * priority-based selection, and runtime registration capabilities.</p>
 *
 * <h2>Key Components</h2>
 * <dl>
 * <dt>{@link com.fft.factory.FFTFactory}</dt>
 * <dd>Factory interface defining the contract for implementation creation and management.
 * Supports registration, deregistration, and introspection of available implementations.</dd>
 *
 * <dt>{@link com.fft.factory.DefaultFFTFactory}</dt>
 * <dd>Default implementation featuring automatic discovery, priority-based selection, and
 * thread-safe lazy initialization. Scans classpath for @FFTImplementation annotations.</dd>
 *
 * <dt>{@link com.fft.factory.FFTImplementationDiscovery}</dt>
 * <dd>Auto-discovery system that scans packages (com.fft.optimized, com.fft.experimental,
 * com.fft.custom) for FFT implementations and registers them automatically.</dd>
 *
 * <dt>{@link com.fft.factory.FFTImplementation}</dt>
 * <dd>Annotation for marking FFT implementations with metadata including size, priority,
 * description, and characteristics. Enables automatic discovery and registration.</dd>
 * </dl>
 *
 * <h2>Factory Pattern Benefits</h2>
 * <ul>
 * <li><b>Automatic Selection:</b> Chooses optimal implementation based on size</li>
 * <li><b>Extensibility:</b> Add new implementations without modifying factory code</li>
 * <li><b>Priority System:</b> Control which implementation is selected when multiple exist</li>
 * <li><b>Zero Configuration:</b> Auto-discovery eliminates manual registration</li>
 * <li><b>Runtime Flexibility:</b> Implementations can be registered/unregistered dynamically</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Factory Usage</h3>
 * <pre>{@code
 * FFTFactory factory = new DefaultFFTFactory();
 *
 * // Factory selects optimal implementation for size 128
 * FFT fft128 = factory.createFFT(128);
 * FFTResult result = fft128.transform(data, true);
 *
 * // Introspect available sizes
 * List<Integer> sizes = factory.getSupportedSizes();
 * System.out.println("Available sizes: " + sizes);
 * }</pre>
 *
 * <h3>Implementation Information</h3>
 * <pre>{@code
 * FFTFactory factory = new DefaultFFTFactory();
 *
 * // Get implementation details
 * String info = factory.getImplementationInfo(1024);
 * System.out.println(info);
 * // Output: "FFTOptimized1024: Optimized implementation for size 1024"
 *
 * // Check if size is supported
 * boolean supported = factory.supportsSize(2048);
 * }</pre>
 *
 * <h3>Custom Implementation Registration</h3>
 * <pre>{@code
 * FFTFactory factory = new DefaultFFTFactory();
 *
 * // Register custom implementation with high priority
 * factory.registerImplementation(
 *     512,
 *     () -> new MyCustomFFT512(),
 *     100  // High priority
 * );
 *
 * // Factory will now use custom implementation for size 512
 * FFT fft = factory.createFFT(512);
 * }</pre>
 *
 * <h3>Creating Annotated Implementation</h3>
 * <pre>{@code
 * @FFTImplementation(
 *     size = 256,
 *     priority = 75,
 *     description = "Radix-4 optimized FFT for size 256",
 *     characteristics = {"radix-4", "cache-optimized", "2.5x-speedup"}
 * )
 * public class MyFFT256 implements FFT {
 *     // Implementation auto-discovered and registered
 * }
 * }</pre>
 *
 * <h2>Auto-Discovery Process</h2>
 * <ol>
 * <li>Factory initialization triggers {@link com.fft.factory.FFTImplementationDiscovery}</li>
 * <li>Discovery scans packages: com.fft.optimized, com.fft.experimental, com.fft.custom</li>
 * <li>Classes with @FFTImplementation annotation are identified</li>
 * <li>Implementations registered with their specified size and priority</li>
 * <li>Factory ready to create instances on-demand</li>
 * </ol>
 *
 * <h2>Priority System</h2>
 * <ul>
 * <li><b>Higher Priority = Preferred Selection:</b> When multiple implementations exist for same size</li>
 * <li><b>Default Priorities:</b> FFTBase=1 (lowest), Optimized=50 (default), Custom=75+ (recommended)</li>
 * <li><b>Fallback Behavior:</b> FFTBase used if no size-specific implementation found</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <ul>
 * <li>DefaultFFTFactory is thread-safe for concurrent createFFT() calls</li>
 * <li>Registration methods (registerImplementation, unregisterImplementations) are synchronized</li>
 * <li>Auto-discovery happens once during first factory access (lazy initialization)</li>
 * </ul>
 *
 * <h2>Performance Considerations</h2>
 * <ul>
 * <li><b>Factory Overhead:</b> Negligible (simple map lookup)</li>
 * <li><b>Instance Creation:</b> Implementations created fresh each time (stateless)</li>
 * <li><b>Discovery Cost:</b> One-time classpath scan during initialization</li>
 * </ul>
 *
 * @author Claude Code (factory pattern implementation, 2025)
 * @version 2.0.0
 * @since 2.0
 * @see com.fft.core.FFT For FFT interface contract
 * @see com.fft.optimized For available optimized implementations
 */
package com.fft.factory;

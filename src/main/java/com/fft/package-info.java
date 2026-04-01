/**
 * Fast Fourier Transform Library - High-performance Java implementation.
 *
 * <p>This library provides FFT (Fast Fourier Transform) functionality with automatic size-based
 * selection, one active size-specific optimized implementation, and advanced audio processing capabilities.
 * Built using the Cooley-Tukey algorithm with modern Java patterns for production-ready signal processing.</p>
 *
 * <h2>Core Features</h2>
 * <ul>
 * <li><b>High Performance:</b> Shared cache optimizations in {@link com.fft.core.FFTBase} plus a dedicated FFT-8 implementation</li>
 * <li><b>Factory Pattern:</b> Automatic implementation selection based on input size</li>
 * <li><b>Type Safety:</b> Immutable {@link com.fft.core.FFTResult} objects with rich data extraction</li>
 * <li><b>Zero Dependencies:</b> Pure Java 17 implementation (javax.sound only for audio demos)</li>
 * <li><b>Comprehensive Testing:</b> A large JUnit suite covering correctness, demos, and regression checks</li>
 * </ul>
 *
 * <h2>Package Organization</h2>
 * <ul>
 * <li>{@link com.fft.core} - Core interfaces, base implementation, and result types</li>
 * <li>{@link com.fft.factory} - Factory pattern for automatic implementation selection</li>
 * <li>{@link com.fft.optimized} - Optimized FFT code, currently centered on {@code FFTOptimized8}</li>
 * <li>{@link com.fft.utils} - Utility classes for convenient FFT operations and pitch detection</li>
 * <li>{@link com.fft.demo} - Demonstration applications for audio processing use cases</li>
 * </ul>
 *
 * <h2>Quick Start Example</h2>
 * <pre>{@code
 * // Simple FFT with automatic implementation selection
 * double[] signal = {1, 2, 3, 4, 5, 6, 7, 8};
 * FFTResult result = FFTUtils.fft(signal);
 *
 * // Extract magnitudes and phases
 * double[] magnitudes = result.getMagnitudes();
 * double[] phases = result.getPhases();
 *
 * // Find dominant frequency
 * int peakIndex = result.findPeakIndex();
 * }</pre>
 *
 * <h2>Performance Characteristics</h2>
 * <ul>
 * <li><b>Twiddle Cache:</b> 30-50% overall speedup by precomputing cos/sin tables</li>
 * <li><b>FFT8:</b> Dedicated implementation with complete loop unrolling</li>
 * <li><b>Other power-of-two sizes:</b> Handled by {@link com.fft.core.FFTBase}</li>
 * <li><b>Time Complexity:</b> O(n log n) for all implementations</li>
 * <li><b>Space Complexity:</b> O(n) additional memory</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <ul>
 * <li>All FFT implementations are thread-safe for concurrent use</li>
 * <li>FFTResult objects are immutable and safe to share across threads</li>
 * <li>DefaultFFTFactory uses thread-safe lazy initialization</li>
 * </ul>
 *
 * <h2>Mathematical Properties</h2>
 * <ul>
 * <li><b>Parseval's Theorem:</b> Energy conservation maintained across transformations</li>
 * <li><b>Normalization:</b> 1/√n factor applied to both forward and inverse transforms</li>
 * <li><b>Precision:</b> Double-precision (64-bit) floating-point throughout</li>
 * <li><b>Round-trip Accuracy:</b> &lt;1e-10 error for forward-inverse transform pairs</li>
 * </ul>
 *
 * @author Orlando Selenu (original implementation, 2008)
 * @author Claude Code (refactoring and optimization, 2025)
 * @version 2.0.0
 * @since 1.0
 * @see com.fft.core.FFT Main FFT interface
 * @see com.fft.utils.FFTUtils Convenience methods for common operations
 * @see <a href="https://en.wikipedia.org/wiki/Fast_Fourier_transform">FFT on Wikipedia</a>
 * @see <a href="https://en.wikipedia.org/wiki/Cooley%E2%80%93Tukey_FFT_algorithm">Cooley-Tukey Algorithm</a>
 */
package com.fft;

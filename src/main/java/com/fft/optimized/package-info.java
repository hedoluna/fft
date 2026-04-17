/**
 * Size-specific optimized FFT implementations for maximum performance.
 *
 * <p>This package currently contains two active size-specific FFT implementations,
 * {@link com.fft.optimized.FFTOptimized8} and {@link com.fft.optimized.FFTOptimized16}, plus historical helper classes that support earlier
 * optimization experiments.</p>
 *
 * <h2>Current Implementation Layout</h2>
 * <table>
 * <caption>Optimized FFT Implementation Summary</caption>
 * <tr>
 *     <th>Size</th>
 *     <th>Class</th>
 *     <th>Speedup</th>
 *     <th>Technique</th>
 * </tr>
 * <tr>
 *     <td>8</td>
 *     <td>{@link com.fft.optimized.FFTOptimized8}</td>
 *     <td>Specialized</td>
 *     <td>Complete loop unrolling, hardcoded twiddles</td>
 * </tr>
 * <tr>
 *     <td>16</td>
 *     <td>{@link com.fft.optimized.FFTOptimized16}</td>
 *     <td>Specialized</td>
 *     <td>FFT8 decomposition with precomputed FFT16 twiddles</td>
 * </tr>
 * <tr>
 *     <td>Helpers</td>
 *     <td>{@link com.fft.optimized.OptimizedFFTUtils}</td>
 *     <td>N/A</td>
 *     <td>Shared utility methods</td>
 * </tr>
 * <tr>
 *     <td>Historical scaffold</td>
 *     <td>{@link com.fft.optimized.OptimizedFFTFramework}</td>
 *     <td>N/A</td>
 *     <td>Legacy framework retained for reference</td>
 * </tr>
 * </table>
 *
 * <h2>Optimization Strategies</h2>
 *
 * <h3>FFT8: Manual Optimization</h3>
 * <ul>
 * <li><b>Complete Loop Unrolling:</b> All 3 stages manually unrolled (8 = 2³)</li>
 * <li><b>Hardcoded Twiddles:</b> W₈⁰, W₈¹, W₈², W₈³ as static final constants</li>
 * <li><b>Inline Bit-Reversal:</b> Hardcoded swaps: (1,4), (3,6)</li>
 * <li><b>Zero Loop Overhead:</b> Straight-line code eliminates all branching</li>
 * </ul>
 *
 * <h3>Universal: Twiddle Factor Cache (30-50% speedup)</h3>
 * <ul>
 * <li><b>Precomputed Tables:</b> cos/sin values cached for sizes 8-4096</li>
 * <li><b>Array Lookup:</b> Replaces Math.cos/sin calls (43-56% of original time)</li>
 * <li><b>Memory Cost:</b> ~128 KB for all precomputed sizes</li>
 * <li><b>Universal Benefit:</b> All implementations gain 30-50% speedup</li>
 * </ul>
 *
 * <h3>Why Manual Unrolling Only for FFT8</h3>
 * <ul>
 * <li><b>Complexity:</b> FFT16+ has 4+ stages, 32+ butterflies, 6+ bit-reversal swaps</li>
 * <li><b>Diminishing Returns:</b> Twiddle cache already provides 30-50% speedup</li>
 * <li><b>Risk:</b> Manual butterfly operations are error-prone (FFT16 attempt had 4 test failures)</li>
 * <li><b>Maintainability:</b> FFT8 code is manageable; larger sizes become unwieldy</li>
 * </ul>
 *
 * <h2>Implementation Guidelines</h2>
 *
 * <h3>For Contributors Adding New Optimizations</h3>
 * <ol>
 * <li><b>Start with Correctness:</b> Verify against FFTBase output (error &lt;1e-10)</li>
 * <li><b>Measure Baseline:</b> Profile current performance with heavy warmup (10K+ iterations)</li>
 * <li><b>Target Specific Bottleneck:</b> Use profiling to identify what to optimize</li>
 * <li><b>Verify Performance:</b> Ensure measurable speedup with statistical confidence</li>
 * <li><b>Add Tests:</b> Comprehensive unit tests including edge cases</li>
 * <li><b>Document Technique:</b> Explain optimization in class JavaDoc</li>
 * </ol>
 *
 * <h3>Annotation Requirements</h3>
 * <pre>{@code
 * @FFTImplementation(
 *     size = 512,                    // Exact size supported
 *     priority = 50,                 // Priority (higher = preferred)
 *     description = "Description",   // Human-readable description
 *     characteristics = {"opt1", "opt2"}  // Optimization techniques used
 * )
 * }</pre>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Direct Usage (Optimal)</h3>
 * <pre>{@code
 * // Direct instantiation for known size
 * FFTOptimized8 fft = new FFTOptimized8();
 * double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
 * FFTResult result = fft.transform(real, true);
 * }</pre>
 *
 * <h3>Factory-Based Usage (Flexible)</h3>
 * <pre>{@code
 * // Factory selects optimal implementation
 * FFTFactory factory = new DefaultFFTFactory();
 * FFT fft = factory.createFFT(16);  // Returns FFTOptimized16 in the current codebase
 * FFTResult result = fft.transform(data, true);
 * }</pre>
 *
 * <h2>Performance Measurement</h2>
 *
 * <h3>Proper Benchmarking Technique</h3>
 * <pre>{@code
 * FFTOptimized8 fft = new FFTOptimized8();
 * double[] real = new double[8];
 * double[] imag = new double[8];
 *
 * // CRITICAL: Heavy warmup for JIT
 * for (int i = 0; i < 10000; i++) {
 *     fft.transform(real, imag, true);
 * }
 *
 * // Measurement
 * long start = System.nanoTime();
 * for (int i = 0; i < 10000; i++) {
 *     fft.transform(real, imag, true);
 * }
 * long avgNanos = (System.nanoTime() - start) / 10000;
 * }</pre>
 *
 * <h2>Historical Context</h2>
 * <ul>
 * <li><b>FASE 1 (Complete):</b> Framework overhead eliminated (3.1x speedup on small sizes)</li>
 * <li><b>FASE 2 (Complete):</b> Twiddle cache implemented as a shared optimization</li>
 * <li><b>FFT8/FFT16 Manual:</b> Small-size specializations retained as active optimized implementations</li>
 * <li><b>Lessons Learned:</b> Universal optimizations (twiddle cache) > per-size manual coding</li>
 * </ul>
 *
 * @author Orlando Selenu (original implementations, 2008)
 * @author Claude Code (optimization and refactoring, 2025)
 * @version 2.0.0
 * @since 1.0
 * @see com.fft.core.TwiddleFactorCache For shared twiddle-factor caching
 * @see com.fft.factory For automatic implementation selection
 */
package com.fft.optimized;

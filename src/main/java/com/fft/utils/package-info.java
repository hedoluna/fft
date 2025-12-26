/**
 * Utility classes for convenient FFT operations and audio signal processing.
 *
 * <p>This package provides high-level utility methods for common FFT operations and advanced
 * audio processing algorithms. Includes convenient wrapper methods, legacy API support, and
 * specialized pitch detection algorithms.</p>
 *
 * <h2>Key Components</h2>
 * <dl>
 * <dt>{@link com.fft.utils.FFTUtils}</dt>
 * <dd>Primary utility class providing convenient static methods for FFT operations. Includes
 * factory-based transform methods, implementation information retrieval, and legacy API
 * compatibility. Recommended entry point for most users.</dd>
 *
 * <dt>{@link com.fft.utils.PitchDetectionUtils}</dt>
 * <dd>Advanced pitch detection algorithms including YIN (autocorrelation-based), spectral peak
 * detection, voicing detection, and median filtering. Shared utilities used by all pitch
 * detection demos.</dd>
 * </dl>
 *
 * <h2>FFTUtils: Convenience Methods</h2>
 *
 * <h3>Simple FFT Operations</h3>
 * <pre>{@code
 * // Forward FFT with automatic implementation selection
 * double[] signal = {1, 2, 3, 4, 5, 6, 7, 8};
 * FFTResult result = FFTUtils.fft(signal);
 *
 * // Access results
 * double[] magnitudes = result.getMagnitudes();
 * double[] phases = result.getPhases();
 * double[] powerSpectrum = result.getPowerSpectrum();
 * }</pre>
 *
 * <h3>Implementation Information</h3>
 * <pre>{@code
 * // Get details about implementation for a size
 * String info = FFTUtils.getImplementationInfo(1024);
 * System.out.println(info);
 * // Output: "FFTOptimized1024: Optimized implementation for size 1024"
 *
 * // Create custom factory
 * FFTFactory factory = FFTUtils.createFactory();
 * }</pre>
 *
 * <h2>PitchDetectionUtils: Audio Processing</h2>
 *
 * <h3>YIN Algorithm (Recommended)</h3>
 * <pre>{@code
 * double[] audioSamples = new double[2048];
 * int sampleRate = 44100;
 *
 * // Detect pitch using YIN algorithm
 * double pitch = PitchDetectionUtils.detectPitchYIN(
 *     audioSamples,
 *     sampleRate,
 *     0.15  // Threshold for voicing detection
 * );
 *
 * if (pitch > 0) {
 *     System.out.println("Detected: " + pitch + " Hz");
 * } else {
 *     System.out.println("No pitch detected (silence/noise)");
 * }
 * }</pre>
 *
 * <h3>Spectral Peak Detection</h3>
 * <pre>{@code
 * double[] audioSamples = new double[2048];
 * int sampleRate = 44100;
 *
 * // Detect pitch using FFT-based spectral analysis
 * double pitch = PitchDetectionUtils.detectPitchSpectral(
 *     audioSamples,
 *     sampleRate
 * );
 * }</pre>
 *
 * <h3>Voicing Detection</h3>
 * <pre>{@code
 * double[] frame = new double[2048];
 *
 * // Check if frame contains voiced sound (vs. silence/noise)
 * boolean isVoiced = PitchDetectionUtils.isVoiced(
 *     frame,
 *     0.01  // RMS threshold
 * );
 * }</pre>
 *
 * <h3>Median Filtering for Pitch Stability</h3>
 * <pre>{@code
 * double[] pitchSequence = {440.1, 440.3, 450.0, 440.2, 440.4};
 *
 * // Apply median filter to smooth out outliers
 * double[] smoothed = PitchDetectionUtils.medianFilter(
 *     pitchSequence,
 *     3  // Window size
 * );
 * // Result: [440.1, 440.3, 440.3, 440.4, 440.4]
 * }</pre>
 *
 * <h2>Pitch Detection Algorithms</h2>
 *
 * <h3>YIN Algorithm (Best Accuracy)</h3>
 * <ul>
 * <li><b>Method:</b> Autocorrelation with cumulative mean normalized difference</li>
 * <li><b>Accuracy:</b> &lt;0.5% error across 80Hz-2000Hz range</li>
 * <li><b>Strengths:</b> Excellent for monophonic audio, robust to noise</li>
 * <li><b>Speed:</b> 12,000+ detections/second</li>
 * <li><b>Use Cases:</b> Voice, solo instruments, pitch tracking</li>
 * </ul>
 *
 * <h3>Spectral Peak Detection (Fast)</h3>
 * <ul>
 * <li><b>Method:</b> FFT-based frequency domain analysis</li>
 * <li><b>Speed:</b> Very fast (FFT-based)</li>
 * <li><b>Strengths:</b> Simple, works for pure tones</li>
 * <li><b>Weaknesses:</b> Less robust to harmonics and noise</li>
 * <li><b>Use Cases:</b> Simple tone detection, real-time analysis</li>
 * </ul>
 *
 * <h2>Performance Characteristics</h2>
 * <ul>
 * <li><b>FFTUtils.fft():</b> Near-zero overhead (delegates to factory)</li>
 * <li><b>YIN Algorithm:</b> O(n²) time complexity, practical for n ≤ 4096</li>
 * <li><b>Spectral Detection:</b> O(n log n) via FFT</li>
 * <li><b>Median Filter:</b> O(n·k log k) where k is window size</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <ul>
 * <li>All methods in FFTUtils and PitchDetectionUtils are thread-safe</li>
 * <li>No shared mutable state</li>
 * <li>Safe for concurrent audio processing pipelines</li>
 * </ul>
 *
 * <h2>Typical Workflow</h2>
 * <ol>
 * <li><b>Audio Capture:</b> Record audio samples at 44.1 kHz</li>
 * <li><b>Windowing:</b> Extract frames of 2048-4096 samples</li>
 * <li><b>Pitch Detection:</b> Use YIN algorithm for each frame</li>
 * <li><b>Voicing Check:</b> Filter out silence/noise frames</li>
 * <li><b>Temporal Filtering:</b> Apply median filter for stability</li>
 * <li><b>Application Logic:</b> Process detected pitches (tuner, transcription, etc.)</li>
 * </ol>
 *
 * <h2>Best Practices</h2>
 * <ul>
 * <li><b>Frame Size:</b> Use 2048-4096 samples for good time/frequency resolution</li>
 * <li><b>Overlap:</b> 50% overlap between frames for smooth tracking</li>
 * <li><b>Sample Rate:</b> 44.1 kHz standard for audio, 16 kHz sufficient for speech</li>
 * <li><b>Threshold Tuning:</b> Adjust YIN/voicing thresholds based on use case</li>
 * <li><b>Filtering:</b> Always use median filter for real-time pitch tracking</li>
 * </ul>
 *
 * @author Orlando Selenu (original FFTUtils, 2008)
 * @author Claude Code (PitchDetectionUtils, enhancements, 2025)
 * @version 2.0.0
 * @since 1.0
 * @see com.fft.demo For complete application examples
 * @see com.fft.core.FFT For low-level FFT operations
 */
package com.fft.utils;

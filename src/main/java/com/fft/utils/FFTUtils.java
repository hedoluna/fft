package com.fft.utils;

import com.fft.core.FFT;
import com.fft.core.FFTResult;
import com.fft.factory.DefaultFFTFactory;
import com.fft.factory.FFTFactory;

/**
 * FFT Utility Class - Comprehensive helper methods and automatic implementation selection.
 * 
 * <p>This utility class provides a convenient interface to the FFT library with automatic
 * selection of the most appropriate optimized implementation based on input size. It also
 * includes helper methods for common signal processing tasks, data manipulation, and
 * result extraction.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 * <li><b>Automatic Implementation Selection:</b> Chooses the fastest available FFT implementation</li>
 * <li><b>Convenient Wrapper Methods:</b> Simplified interfaces for common use cases</li>
 * <li><b>Signal Generation:</b> Tools for creating test signals with known frequency content</li>
 * <li><b>Result Processing:</b> Extract magnitude, phase, and component arrays from FFT results</li>
 * <li><b>Input Validation:</b> Comprehensive error checking and helpful error messages</li>
 * <li><b>Zero Padding:</b> Automatic extension of signals to power-of-2 lengths</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Simple FFT with automatic implementation selection
 * double[] signal = {1, 2, 3, 4, 5, 6, 7, 8};
 * FFTResult result = FFTUtils.fft(signal);
 * double[] magnitudes = result.getMagnitudes();
 * 
 * // Signal analysis
 * double[] testSignal = FFTUtils.generateTestSignal(1024, 1000.0, 
 *                                                   new double[]{50, 120}, 
 *                                                   new double[]{1.0, 0.5});
 * FFTResult spectrum = FFTUtils.fft(testSignal);
 * 
 * // Handle arbitrary-length signals
 * double[] arbitrarySignal = new double[300]; // Not power of 2
 * double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(arbitrarySignal);
 * FFTResult result = FFTUtils.fft(paddedSignal);
 * }</pre>
 * 
 * @author Engine AI Assistant (comprehensive implementation, 2025)
 * @since 2.0.0
 * @see FFTFactory for implementation selection details
 * @see FFTResult for result processing methods
 */
public class FFTUtils {
    
    private static final FFTFactory DEFAULT_FACTORY = new DefaultFFTFactory();
    
    /**
     * Performs FFT using the most appropriate implementation for the given size.
     * Automatically selects optimized versions when available.
     * 
     * @param inputReal real part of input (must be power of 2 length)
     * @param inputImag imaginary part of input (must be power of 2 length)
     * @param forward true for forward transform, false for inverse
     * @return FFT result containing the transformed data
     * @throws IllegalArgumentException if arrays have different lengths or invalid size
     */
    public static FFTResult fft(double[] inputReal, double[] inputImag, boolean forward) {
        if (inputReal.length != inputImag.length) {
            throw new IllegalArgumentException("Real and imaginary arrays must have same length");
        }
        
        if (!isPowerOfTwo(inputReal.length)) {
            throw new IllegalArgumentException("Array length must be a power of 2");
        }
        
        FFT fft = DEFAULT_FACTORY.createFFT(inputReal.length);
        return fft.transform(inputReal, inputImag, forward);
    }
    
    /**
     * Performs FFT on real-valued input (imaginary part assumed to be zero).
     * 
     * @param inputReal real-valued input array
     * @param forward true for forward transform, false for inverse
     * @return FFT result containing the transformed data
     * @throws IllegalArgumentException if array size is not a power of 2
     */
    public static FFTResult fft(double[] inputReal, boolean forward) {
        if (!isPowerOfTwo(inputReal.length)) {
            throw new IllegalArgumentException("Array length must be a power of 2");
        }
        
        FFT fft = DEFAULT_FACTORY.createFFT(inputReal.length);
        return fft.transform(inputReal, forward);
    }
    
    /**
     * Performs forward FFT on real-valued input.
     * 
     * @param inputReal real-valued input array
     * @return FFT result containing the transformed data
     * @throws IllegalArgumentException if array size is not a power of 2
     */
    public static FFTResult fft(double[] inputReal) {
        return fft(inputReal, true);
    }
    
    /**
     * Generates a test signal with specified frequencies.
     */
    public static double[] generateTestSignal(int size, double sampleRate, 
                                            double[] frequencies, double[] amplitudes) {
        double[] signal = new double[size];
        double dt = 1.0 / sampleRate;
        
        for (int i = 0; i < size; i++) {
            double t = i * dt;
            for (int j = 0; j < frequencies.length; j++) {
                signal[i] += amplitudes[j] * Math.sin(2 * Math.PI * frequencies[j] * t);
            }
        }
        return signal;
    }
    
    /**
     * Generates a simple sine wave test signal.
     * 
     * @param size size of the signal
     * @param frequency frequency of the sine wave
     * @param sampleRate sampling rate in Hz
     * @return generated sine wave signal
     */
    public static double[] generateSineWave(int size, double frequency, double sampleRate) {
        return generateTestSignal(size, sampleRate, new double[]{frequency}, new double[]{1.0});
    }
    
    /**
     * Generates test signals with predefined types for testing purposes.
     * 
     * @param size size of the signal
     * @param type type of signal ("impulse", "dc", "sine", "cosine", "mixed", "random")
     * @return generated signal
     */
    public static double[] generateTestSignal(int size, String type) {
        double[] signal = new double[size];
        
        switch (type.toLowerCase()) {
            case "impulse":
                if (size > 0) signal[0] = 1.0;
                break;
                
            case "dc":
                for (int i = 0; i < size; i++) {
                    signal[i] = 1.0;
                }
                break;
                
            case "sine":
                for (int i = 0; i < size; i++) {
                    signal[i] = Math.sin(2.0 * Math.PI * 5 * i / size); // 5 cycles
                }
                break;
                
            case "cosine":
                for (int i = 0; i < size; i++) {
                    signal[i] = Math.cos(2.0 * Math.PI * 3 * i / size); // 3 cycles
                }
                break;
                
            case "mixed":
                for (int i = 0; i < size; i++) {
                    signal[i] = Math.sin(2.0 * Math.PI * 5 * i / size) + 
                               0.5 * Math.cos(2.0 * Math.PI * 10 * i / size) +
                               0.25 * Math.sin(2.0 * Math.PI * 15 * i / size);
                }
                break;
                
            case "random":
                java.util.Random random = new java.util.Random(42); // Fixed seed for reproducibility
                for (int i = 0; i < size; i++) {
                    signal[i] = random.nextGaussian();
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unknown signal type: " + type);
        }
        
        return signal;
    }
    
    /**
     * Checks if a number is a power of 2.
     * 
     * @param n the number to check
     * @return true if n is a power of 2, false otherwise
     */
    public static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
    
    /**
     * Finds the next power of 2 greater than or equal to n.
     * 
     * @param n the input number
     * @return the next power of 2 >= n
     */
    public static int nextPowerOfTwo(int n) {
        if (n <= 0) return 1;
        n--;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        return n + 1;
    }
    
    /**
     * Zero-pads an array to the next power of 2 size.
     * 
     * @param input the input array
     * @return zero-padded array with power-of-2 length
     */
    public static double[] zeroPadToPowerOfTwo(double[] input) {
        int newSize = nextPowerOfTwo(input.length);
        double[] padded = new double[newSize];
        System.arraycopy(input, 0, padded, 0, input.length);
        return padded;
    }
    
    /**
     * Returns information about which FFT implementation would be used for a given size.
     * 
     * @param size the array size
     * @return string describing the implementation that would be used
     */
    public static String getImplementationInfo(int size) {
        return DEFAULT_FACTORY.getImplementationInfo(size);
    }
    
    /**
     * Returns a list of all sizes supported by the default factory.
     * 
     * @return list of supported sizes
     */
    public static java.util.List<Integer> getSupportedSizes() {
        return DEFAULT_FACTORY.getSupportedSizes();
    }
    
    /**
     * Creates a custom FFT factory instance.
     * 
     * @return new DefaultFFTFactory instance
     */
    public static FFTFactory createFactory() {
        return new DefaultFFTFactory();
    }
    
    // Legacy compatibility methods that return double[] arrays
    
    /**
     * Legacy method: Performs FFT and returns interleaved result array.
     * 
     * @deprecated Use {@link #fft(double[], double[], boolean)} which returns FFTResult
     * @param inputReal real part of input
     * @param inputImag imaginary part of input
     * @param direct true for forward transform, false for inverse
     * @return interleaved result array [real0, imag0, real1, imag1, ...]
     */
    @Deprecated
    public static double[] fftLegacy(double[] inputReal, double[] inputImag, boolean direct) {
        FFTResult result = fft(inputReal, inputImag, direct);
        return result.getInterleavedResult();
    }
    
    /**
     * Legacy method: Extracts real parts from interleaved FFT result.
     * 
     * @deprecated Use FFTResult.getRealParts() instead
     * @param fftResult interleaved real/imaginary result from FFT
     * @return array containing only the real parts
     */
    @Deprecated
    public static double[] getRealParts(double[] fftResult) {
        return new FFTResult(fftResult).getRealParts();
    }
    
    /**
     * Legacy method: Extracts imaginary parts from interleaved FFT result.
     * 
     * @deprecated Use FFTResult.getImaginaryParts() instead
     * @param fftResult interleaved real/imaginary result from FFT
     * @return array containing only the imaginary parts
     */
    @Deprecated
    public static double[] getImagParts(double[] fftResult) {
        return new FFTResult(fftResult).getImaginaryParts();
    }
    
    /**
     * Legacy method: Computes magnitudes from interleaved FFT result.
     * 
     * @deprecated Use FFTResult.getMagnitudes() instead
     * @param fftResult interleaved real/imaginary result from FFT
     * @return array containing the magnitudes
     */
    @Deprecated
    public static double[] getMagnitudes(double[] fftResult) {
        return new FFTResult(fftResult).getMagnitudes();
    }
    
    /**
     * Legacy method: Computes phases from interleaved FFT result.
     * 
     * @deprecated Use FFTResult.getPhases() instead
     * @param fftResult interleaved real/imaginary result from FFT
     * @return array containing the phase angles in radians
     */
    @Deprecated
    public static double[] getPhases(double[] fftResult) {
        return new FFTResult(fftResult).getPhases();
    }
}

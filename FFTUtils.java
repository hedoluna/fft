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
 * <h3>Performance Optimization:</h3>
 * <p>The class automatically selects optimized implementations that provide significant
 * speedup over the generic FFTbase implementation:</p>
 * <ul>
 * <li>Size 8: FFToptim8 (1.4x speedup)</li>
 * <li>Size 32: FFToptim32 (3.6x speedup)</li>
 * <li>Size 64-512: FFToptim64-512 (2.9x-7.2x speedup)</li>
 * <li>Size 1024-8192: FFToptim1024-8192 (~8x speedup)</li>
 * <li>Other sizes: FFTbase (fallback implementation)</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Simple FFT with automatic implementation selection
 * double[] signal = {1, 2, 3, 4, 5, 6, 7, 8};
 * double[] result = FFTUtils.fft(signal);
 * double[] magnitudes = FFTUtils.getMagnitudes(result);
 * 
 * // Signal analysis
 * double[] testSignal = FFTUtils.generateTestSignal(1024, 1000.0, 
 *                                                   new double[]{50, 120}, 
 *                                                   new double[]{1.0, 0.5});
 * double[] spectrum = FFTUtils.fft(testSignal);
 * 
 * // Handle arbitrary-length signals
 * double[] arbitrarySignal = new double[300]; // Not power of 2
 * double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(arbitrarySignal);
 * double[] result = FFTUtils.fft(paddedSignal);
 * }</pre>
 * 
 * @author Engine AI Assistant (comprehensive implementation, 2025)
 * @since 1.0
 * @see FFTbase for reference implementation details
 * @see "E. Oran Brigham, The Fast Fourier Transform, 1973"
 */
public class FFTUtils {
    
    /**
     * Performs FFT using the most appropriate implementation for the given size.
     * Automatically selects optimized versions when available.
     * 
     * @param inputReal real part of input (must be power of 2 length)
     * @param inputImag imaginary part of input (must be power of 2 length)
     * @param direct true for forward transform, false for inverse
     * @return result array with interleaved real and imaginary parts
     */
    public static double[] fft(double[] inputReal, double[] inputImag, boolean direct) {
        int size = inputReal.length;
        
        // Validate input
        if (inputReal.length != inputImag.length) {
            throw new IllegalArgumentException("Real and imaginary arrays must have same length");
        }
        
        if (!isPowerOfTwo(size)) {
            throw new IllegalArgumentException("Array length must be a power of 2");
        }
        
        // Select appropriate implementation based on size
        return switch (size) {
            case 8 -> direct ? FFToptim8.fft(inputReal, inputImag, direct) 
                             : FFTbase.fft(inputReal, inputImag, direct); // FFToptim8 only supports direct
            case 32 -> FFToptim32.fft(inputReal, inputImag, direct);
            case 64 -> FFToptim64.fft(inputReal, inputImag, direct);
            case 128 -> FFToptim128.fft(inputReal, inputImag, direct);
            case 256 -> FFToptim256.fft(inputReal, inputImag, direct);
            case 512 -> FFToptim512.fft(inputReal, inputImag, direct);
            case 1024 -> FFToptim1024.fft(inputReal, inputImag, direct);
            case 2048 -> FFToptim2048.fft(inputReal, inputImag, direct);
            case 4096 -> FFToptim4096.fft(inputReal, inputImag, direct);
            case 8192 -> FFToptim8192.fft(inputReal, inputImag, direct);
            default -> FFTbase.fft(inputReal, inputImag, direct); // Fallback to base implementation
        };
    }
    
    /**
     * Performs FFT on real-valued input (imaginary part assumed to be zero).
     * 
     * @param inputReal real-valued input array
     * @param direct true for forward transform, false for inverse
     * @return result array with interleaved real and imaginary parts
     */
    public static double[] fft(double[] inputReal, boolean direct) {
        double[] inputImag = new double[inputReal.length];
        return fft(inputReal, inputImag, direct);
    }
    
    /**
     * Performs forward FFT on real-valued input.
     * 
     * @param inputReal real-valued input array
     * @return result array with interleaved real and imaginary parts
     */
    public static double[] fft(double[] inputReal) {
        return fft(inputReal, true);
    }
    
    /**
     * Extracts the real parts from an interleaved FFT result.
     * 
     * @param fftResult interleaved real/imaginary result from FFT
     * @return array containing only the real parts
     */
    public static double[] getRealParts(double[] fftResult) {
        double[] real = new double[fftResult.length / 2];
        for (int i = 0; i < real.length; i++) {
            real[i] = fftResult[2 * i];
        }
        return real;
    }
    
    /**
     * Extracts the imaginary parts from an interleaved FFT result.
     * 
     * @param fftResult interleaved real/imaginary result from FFT
     * @return array containing only the imaginary parts
     */
    public static double[] getImagParts(double[] fftResult) {
        double[] imag = new double[fftResult.length / 2];
        for (int i = 0; i < imag.length; i++) {
            imag[i] = fftResult[2 * i + 1];
        }
        return imag;
    }
    
    /**
     * Computes the magnitude (absolute value) of complex FFT results.
     * 
     * @param fftResult interleaved real/imaginary result from FFT
     * @return array containing the magnitudes
     */
    public static double[] getMagnitudes(double[] fftResult) {
        double[] magnitudes = new double[fftResult.length / 2];
        for (int i = 0; i < magnitudes.length; i++) {
            double real = fftResult[2 * i];
            double imag = fftResult[2 * i + 1];
            magnitudes[i] = Math.sqrt(real * real + imag * imag);
        }
        return magnitudes;
    }
    
    /**
     * Computes the phase angles of complex FFT results.
     * 
     * @param fftResult interleaved real/imaginary result from FFT
     * @return array containing the phase angles in radians
     */
    public static double[] getPhases(double[] fftResult) {
        double[] phases = new double[fftResult.length / 2];
        for (int i = 0; i < phases.length; i++) {
            double real = fftResult[2 * i];
            double imag = fftResult[2 * i + 1];
            phases[i] = Math.atan2(imag, real);
        }
        return phases;
    }
    
    /**
     * Generates a test signal with multiple frequency components.
     * 
     * @param size size of the signal (should be power of 2)
     * @param sampleRate sampling rate in Hz
     * @param frequencies array of frequencies to include in the signal
     * @param amplitudes array of amplitudes for each frequency
     * @return generated signal
     */
    public static double[] generateTestSignal(int size, double sampleRate, 
                                            double[] frequencies, double[] amplitudes) {
        if (frequencies.length != amplitudes.length) {
            throw new IllegalArgumentException("Frequencies and amplitudes arrays must have same length");
        }
        
        double[] signal = new double[size];
        for (int i = 0; i < size; i++) {
            double t = i / sampleRate;
            double sample = 0.0;
            for (int j = 0; j < frequencies.length; j++) {
                sample += amplitudes[j] * Math.sin(2 * Math.PI * frequencies[j] * t);
            }
            signal[i] = sample;
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
        if (isPowerOfTwo(n)) return n;
        
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
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
        if (!isPowerOfTwo(size)) {
            return "Invalid size (not power of 2)";
        }
        
        return switch (size) {
            case 8 -> "FFToptim8 (highly optimized for size 8)";
            case 32 -> "FFToptim32 (optimized for size 32)";
            case 64 -> "FFToptim64 (optimized for size 64)";
            case 128 -> "FFToptim128 (optimized for size 128)";
            case 256 -> "FFToptim256 (optimized for size 256)";
            case 512 -> "FFToptim512 (optimized for size 512)";
            case 1024 -> "FFToptim1024 (optimized for size 1024)";
            case 2048 -> "FFToptim2048 (optimized for size 2048)";
            case 4096 -> "FFToptim4096 (optimized for size 4096)";
            case 8192 -> "FFToptim8192 (optimized for size 8192)";
            default -> "FFTbase (generic implementation for size " + size + ")";
        };
    }
}
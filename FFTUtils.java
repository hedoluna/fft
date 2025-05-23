/**
 * Utility class providing convenient methods for FFT operations
 * and automatic selection of optimized implementations.
 * 
 * @author Engine AI Assistant
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
            default -> "FFTbase (generic implementation for size " + size + ")";
        };
    }
}
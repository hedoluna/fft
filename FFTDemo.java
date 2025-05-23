/**
 * Demonstration of FFT utilities and practical usage examples.
 * 
 * @author Engine AI Assistant  
 */
public class FFTDemo {
    
    public static void main(String[] args) {
        System.out.println("FFT Utilities Demonstration");
        System.out.println("===========================");
        System.out.println();
        
        demonstrateBasicUsage();
        demonstrateSignalAnalysis();
        demonstrateImplementationSelection();
    }
    
    /**
     * Demonstrate basic FFT usage with the utilities
     */
    private static void demonstrateBasicUsage() {
        System.out.println("1. Basic FFT Usage:");
        System.out.println("------------------");
        
        // Create a simple test signal
        double[] signal = {1, 2, 3, 4, 5, 6, 7, 8};
        
        System.out.println("Input signal: " + java.util.Arrays.toString(signal));
        
        // Perform FFT using the utility method
        double[] fftResult = FFTUtils.fft(signal);
        
        // Extract components
        double[] realParts = FFTUtils.getRealParts(fftResult);
        double[] imagParts = FFTUtils.getImagParts(fftResult);
        double[] magnitudes = FFTUtils.getMagnitudes(fftResult);
        double[] phases = FFTUtils.getPhases(fftResult);
        
        System.out.println("FFT Real parts: " + formatArray(realParts));
        System.out.println("FFT Imag parts: " + formatArray(imagParts));
        System.out.println("FFT Magnitudes: " + formatArray(magnitudes));
        System.out.println("FFT Phases:     " + formatArray(phases));
        
        System.out.println();
    }
    
    /**
     * Demonstrate signal analysis with multiple frequency components
     */
    private static void demonstrateSignalAnalysis() {
        System.out.println("2. Signal Analysis Example:");
        System.out.println("---------------------------");
        
        // Generate a test signal with known frequency components
        int size = 64;
        double sampleRate = 1000.0; // 1kHz sampling rate
        double[] frequencies = {50.0, 120.0, 300.0}; // 50Hz, 120Hz, 300Hz
        double[] amplitudes = {1.0, 0.5, 0.3};
        
        double[] signal = FFTUtils.generateTestSignal(size, sampleRate, frequencies, amplitudes);
        
        System.out.println("Generated signal with frequencies: " + java.util.Arrays.toString(frequencies) + " Hz");
        System.out.println("Signal amplitudes: " + java.util.Arrays.toString(amplitudes));
        
        // Perform FFT
        double[] fftResult = FFTUtils.fft(signal);
        double[] magnitudes = FFTUtils.getMagnitudes(fftResult);
        
        // Find peaks in the frequency domain
        System.out.println("\nFrequency analysis (showing largest magnitude bins):");
        for (int i = 0; i < magnitudes.length / 2; i++) { // Only show first half (Nyquist)
            double frequency = i * sampleRate / size;
            if (magnitudes[i] > 0.1) { // Only show significant peaks
                System.out.printf("Bin %2d: %.1f Hz, Magnitude: %.3f%n", i, frequency, magnitudes[i]);
            }
        }
        
        System.out.println();
    }
    
    /**
     * Demonstrate automatic implementation selection
     */
    private static void demonstrateImplementationSelection() {
        System.out.println("3. Implementation Selection:");
        System.out.println("---------------------------");
        
        int[] testSizes = {4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048};
        
        for (int size : testSizes) {
            String info = FFTUtils.getImplementationInfo(size);
            System.out.printf("Size %4d: %s%n", size, info);
        }
        
        System.out.println("\nTesting with non-power-of-2 sizes:");
        int[] invalidSizes = {3, 5, 7, 10, 15};
        for (int size : invalidSizes) {
            String info = FFTUtils.getImplementationInfo(size);
            System.out.printf("Size %4d: %s%n", size, info);
        }
        
        System.out.println("\nZero-padding example:");
        double[] shortSignal = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        System.out.println("Original length: " + shortSignal.length);
        
        double[] paddedSignal = FFTUtils.zeroPadToPowerOfTwo(shortSignal);
        System.out.println("Padded length: " + paddedSignal.length);
        System.out.println("Implementation for padded: " + FFTUtils.getImplementationInfo(paddedSignal.length));
        
        System.out.println();
    }
    
    /**
     * Format array for display with limited precision
     */
    private static String formatArray(double[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Math.min(array.length, 8); i++) { // Show first 8 elements
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.3f", array[i]));
        }
        if (array.length > 8) {
            sb.append(", ...");
        }
        sb.append("]");
        return sb.toString();
    }
}
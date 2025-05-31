import com.fft.optimized.FFTOptimized64;
import com.fft.core.FFTResult;

public class debug_test {
    public static void main(String[] args) {
        try {
            FFTOptimized64 fft = new FFTOptimized64();
            double[] real = new double[64];
            double[] imag = new double[64];
            real[0] = 1.0; // Impulse
            
            System.out.println("Starting FFT transform...");
            FFTResult result = fft.transform(real, imag, true);
            System.out.println("Transform completed successfully!");
            System.out.println("Result length: " + result.getInterleavedResult().length);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
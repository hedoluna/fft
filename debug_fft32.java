import com.fft.optimized.FFTOptimized32;
import com.fft.core.FFTBase;
import com.fft.core.FFTResult;

public class debug_fft32 {
    public static void main(String[] args) {
        // Simple test case
        double[] real = new double[32];
        double[] imag = new double[32];
        
        // Simple impulse test
        real[0] = 1.0;
        for (int i = 1; i < 32; i++) {
            real[i] = 0.0;
            imag[i] = 0.0;
        }
        
        System.out.println("=== Original signal ===");
        for (int i = 0; i < 4; i++) {
            System.out.printf("real[%d] = %.6f, imag[%d] = %.6f\n", i, real[i], i, imag[i]);
        }
        
        // Test with reference implementation
        FFTBase reference = new FFTBase();
        FFTResult refForward = reference.transform(real, imag, true);
        FFTResult refInverse = reference.transform(refForward.getRealParts(), refForward.getImaginaryParts(), false);
        
        System.out.println("\n=== Reference FFT Forward ===");
        double[] refRealF = refForward.getRealParts();
        double[] refImagF = refForward.getImaginaryParts();
        for (int i = 0; i < 4; i++) {
            System.out.printf("real[%d] = %.6f, imag[%d] = %.6f\n", i, refRealF[i], i, refImagF[i]);
        }
        
        System.out.println("\n=== Reference FFT Inverse ===");
        double[] refRealI = refInverse.getRealParts();
        double[] refImagI = refInverse.getImaginaryParts();
        for (int i = 0; i < 4; i++) {
            System.out.printf("real[%d] = %.6f, imag[%d] = %.6f\n", i, refRealI[i], i, refImagI[i]);
        }
        
        // Test with optimized implementation
        FFTOptimized32 optimized = new FFTOptimized32();
        FFTResult optForward = optimized.transform(real, imag, true);
        FFTResult optInverse = optimized.transform(optForward.getRealParts(), optForward.getImaginaryParts(), false);
        
        System.out.println("\n=== Optimized FFT Forward ===");
        double[] optRealF = optForward.getRealParts();
        double[] optImagF = optForward.getImaginaryParts();
        for (int i = 0; i < 4; i++) {
            System.out.printf("real[%d] = %.6f, imag[%d] = %.6f\n", i, optRealF[i], i, optImagF[i]);
        }
        
        System.out.println("\n=== Optimized FFT Inverse ===");
        double[] optRealI = optInverse.getRealParts();
        double[] optImagI = optInverse.getImaginaryParts();
        for (int i = 0; i < 4; i++) {
            System.out.printf("real[%d] = %.6f, imag[%d] = %.6f\n", i, optRealI[i], i, optImagI[i]);
        }
        
        // Compare differences
        System.out.println("\n=== Forward Differences ===");
        for (int i = 0; i < 4; i++) {
            double realDiff = Math.abs(refRealF[i] - optRealF[i]);
            double imagDiff = Math.abs(refImagF[i] - optImagF[i]);
            System.out.printf("real[%d] diff = %.8f, imag[%d] diff = %.8f\n", i, realDiff, i, imagDiff);
        }
        
        System.out.println("\n=== Inverse Differences ===");
        for (int i = 0; i < 4; i++) {
            double realDiff = Math.abs(refRealI[i] - optRealI[i]);
            double imagDiff = Math.abs(refImagI[i] - optImagI[i]);
            System.out.printf("real[%d] diff = %.8f, imag[%d] diff = %.8f\n", i, realDiff, i, imagDiff);
        }
    }
}
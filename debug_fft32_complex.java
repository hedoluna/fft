import com.fft.optimized.FFTOptimized32;
import com.fft.core.FFTBase;
import com.fft.core.FFTResult;

public class debug_fft32_complex {
    public static void main(String[] args) {
        // Replicate the exact test that's failing
        double[] originalReal = new double[32];
        double[] originalImag = new double[32];
        
        // Generate test signal exactly like the test
        java.util.Random random = new java.util.Random(123);
        for (int i = 0; i < 32; i++) {
            originalReal[i] = random.nextGaussian();
            originalImag[i] = random.nextGaussian();
        }
        
        System.out.println("=== Original signal (first 4 elements) ===");
        for (int i = 0; i < 4; i++) {
            System.out.printf("real[%d] = %.8f, imag[%d] = %.8f\n", i, originalReal[i], i, originalImag[i]);
        }
        
        // Test with reference implementation
        FFTBase reference = new FFTBase();
        FFTResult refForward = reference.transform(originalReal, originalImag, true);
        FFTResult refInverse = reference.transform(refForward.getRealParts(), refForward.getImaginaryParts(), false);
        
        System.out.println("\n=== Reference inverse (first 4 elements) ===");
        double[] refRealI = refInverse.getRealParts();
        double[] refImagI = refInverse.getImaginaryParts();
        for (int i = 0; i < 4; i++) {
            System.out.printf("real[%d] = %.8f, imag[%d] = %.8f\n", i, refRealI[i], i, refImagI[i]);
        }
        
        // Test with optimized implementation
        FFTOptimized32 optimized = new FFTOptimized32();
        FFTResult optForward = optimized.transform(originalReal, originalImag, true);
        FFTResult optInverse = optimized.transform(optForward.getRealParts(), optForward.getImaginaryParts(), false);
        
        System.out.println("\n=== Optimized inverse (first 4 elements) ===");
        double[] optRealI = optInverse.getRealParts();
        double[] optImagI = optInverse.getImaginaryParts();
        for (int i = 0; i < 4; i++) {
            System.out.printf("real[%d] = %.8f, imag[%d] = %.8f\n", i, optRealI[i], i, optImagI[i]);
        }
        
        // Check consistency like the test does
        System.out.println("\n=== Reconstruction errors ===");
        double maxRealError = 0;
        double maxImagError = 0;
        int maxRealIndex = -1;
        int maxImagIndex = -1;
        
        for (int i = 0; i < 32; i++) {
            double realError = Math.abs(optRealI[i] - originalReal[i]);
            double imagError = Math.abs(optImagI[i] - originalImag[i]);
            
            if (realError > maxRealError) {
                maxRealError = realError;
                maxRealIndex = i;
            }
            if (imagError > maxImagError) {
                maxImagError = imagError;
                maxImagIndex = i;
            }
            
            if (i < 4) {
                System.out.printf("real[%d]: orig=%.8f, recovered=%.8f, error=%.8f\n", 
                    i, originalReal[i], optRealI[i], realError);
                System.out.printf("imag[%d]: orig=%.8f, recovered=%.8f, error=%.8f\n", 
                    i, originalImag[i], optImagI[i], imagError);
            }
        }
        
        System.out.printf("\nMax real error: %.8f at index %d\n", maxRealError, maxRealIndex);
        System.out.printf("Max imag error: %.8f at index %d\n", maxImagError, maxImagIndex);
        System.out.printf("Test tolerance: 3.0\n");
        System.out.printf("Would test pass? %s\n", (maxRealError < 3.0 && maxImagError < 3.0) ? "YES" : "NO");
        
        if (maxRealError >= 3.0) {
            System.out.printf("\nFailing real element: orig=%.8f, recovered=%.8f, error=%.8f\n",
                originalReal[maxRealIndex], optRealI[maxRealIndex], maxRealError);
        }
        if (maxImagError >= 3.0) {
            System.out.printf("\nFailing imag element: orig=%.8f, recovered=%.8f, error=%.8f\n",
                originalImag[maxImagIndex], optImagI[maxImagIndex], maxImagError);
        }
    }
}
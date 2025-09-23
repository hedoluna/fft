import com.fft.core.FFTBase;
import com.fft.core.FFTResult;

public class debug_norm {
    public static void main(String[] args) {
        FFTBase fft = new FFTBase();
        double[] originalReal = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] originalImag = {0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5};
        
        System.out.println("Original signal:");
        for (int i = 0; i < 4; i++) {
            System.out.printf("  [%d] real=%.3f, imag=%.3f\n", i, originalReal[i], originalImag[i]);
        }
        
        // Forward transform
        FFTResult forward = fft.transform(originalReal, originalImag, true);
        
        // Inverse transform
        FFTResult inverse = fft.transform(forward.getRealParts(), forward.getImaginaryParts(), false);
        
        double[] recoveredReal = inverse.getRealParts();
        double[] recoveredImag = inverse.getImaginaryParts();
        
        System.out.println("\nRecovered signal:");
        for (int i = 0; i < 4; i++) {
            System.out.printf("  [%d] real=%.3f, imag=%.3f\n", i, recoveredReal[i], recoveredImag[i]);
        }
        
        System.out.println("\nScaling factors:");
        for (int i = 0; i < 4; i++) {
            double realScale = recoveredReal[i] / originalReal[i];
            double imagScale = recoveredImag[i] / originalImag[i];
            System.out.printf("  [%d] real_scale=%.6f, imag_scale=%.6f\n", i, realScale, imagScale);
        }
    }
}
/**
 * @author Orlando Selenu (original base), Enhanced by Engine AI Assistant
 * Originally written in the Summer of 2008 
 * Based on the algorithms originally published by E. Oran Brigham "The Fast Fourier Transform" 1973, in ALGOL60 and FORTRAN
 * 
 * Released in the Public Domain.
 */
public class FFToptim256 {
    
    // Precomputed trigonometric lookup tables for 256-point FFT
    private static final double[] COS_TABLE = new double[128];
    private static final double[] SIN_TABLE = new double[128];
    
    static {
        // Initialize lookup tables
        for (int i = 0; i < 128; i++) {
            double angle = 2.0 * Math.PI * i / 256.0;
            COS_TABLE[i] = Math.cos(angle);
            SIN_TABLE[i] = Math.sin(angle);
        }
    }
    
    /**
     * The Fast Fourier Transform (optimized version for arrays of size 256).
     * 
     * This implementation is optimized specifically for 256-element arrays
     * with precomputed trigonometric lookup tables for better performance.
     *
     * @param inputReal an array of length 256, the real part
     * @param inputImag an array of length 256, the imaginary part
     * @param DIRECT    TRUE = direct transform, FALSE = inverse transform
     * @return a new array of length 512 (interleaved real and imaginary parts)
     */
    public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {
        // Size validation
        int n = inputReal.length;
        if (n != 256) {
            System.out.println("ERROR: The number of input elements is not 256.");
            return new double[0];
        }

        // Hardcoded parameters for size 256: nu = 8, since 2^8 = 256
        int n2 = 128; // n/2
        int nu1 = 7; // nu - 1
        double[] xReal = new double[256];
        double[] xImag = new double[256];
        double tReal;
        double tImag;
        double p;
        double arg;
        double c;
        double s;

        // Determine transform direction
        double constant = DIRECT ? -2 * Math.PI : 2 * Math.PI;

        // Copy input arrays to avoid modifying originals
        System.arraycopy(inputReal, 0, xReal, 0, 256);
        System.arraycopy(inputImag, 0, xImag, 0, 256);

        // First phase - calculation (8 iterations for 256 elements) with lookup tables
        int k = 0;
        double constant_sign = DIRECT ? -1.0 : 1.0;
        
        for (int l = 1; l <= 8; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    int bit_rev_k = bitreverseReference(k >> nu1);
                    
                    // Use lookup table instead of Math.cos/sin
                    c = COS_TABLE[bit_rev_k];
                    s = SIN_TABLE[bit_rev_k] * constant_sign;
                    
                    tReal = xReal[k + n2] * c + xImag[k + n2] * s;
                    tImag = xImag[k + n2] * c - xReal[k + n2] * s;
                    xReal[k + n2] = xReal[k] - tReal;
                    xImag[k + n2] = xImag[k] - tImag;
                    xReal[k] += tReal;
                    xImag[k] += tImag;
                    k++;
                }
                k += n2;
            }
            k = 0;
            nu1--;
            n2 /= 2;
        }

        // Second phase - bit-reversal recombination
        k = 0;
        int r;
        while (k < n) {
            r = bitreverseReference(k);
            if (r > k) {
                tReal = xReal[k];
                tImag = xImag[k];
                xReal[k] = xReal[r];
                xImag[k] = xImag[r];
                xReal[r] = tReal;
                xImag[r] = tImag;
            }
            k++;
        }

        // Combine real and imaginary parts into output array with normalization
        double[] newArray = new double[512];
        double radice = 1.0 / Math.sqrt(n);
        for (int i = 0; i < 512; i += 2) {
            int i2 = i / 2;
            newArray[i] = xReal[i2] * radice;
            newArray[i + 1] = xImag[i2] * radice;
        }
        return newArray;
    }

    /**
     * Bit reverse function optimized for 8-bit numbers (0-255).
     * 
     * @param j the number to bit-reverse (0-255)
     * @return the bit-reversed number
     */
    private static int bitreverseReference(int j) {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= 8; i++) { // 8 bits for size 256
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
        }
        return k;
    }
}
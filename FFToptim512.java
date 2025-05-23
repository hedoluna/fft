/**
 * @author Orlando Selenu (original base), Enhanced by Engine AI Assistant
 * Originally written in the Summer of 2008 
 * Based on the algorithms originally published by E. Oran Brigham "The Fast Fourier Transform" 1973, in ALGOL60 and FORTRAN
 * 
 * Released in the Public Domain.
 */
public class FFToptim512 {
    
    // Precomputed trigonometric lookup tables for 512-point FFT
    private static final double[] COS_TABLE = new double[256];
    private static final double[] SIN_TABLE = new double[256];
    
    static {
        // Initialize lookup tables
        for (int i = 0; i < 256; i++) {
            double angle = 2.0 * Math.PI * i / 512.0;
            COS_TABLE[i] = Math.cos(angle);
            SIN_TABLE[i] = Math.sin(angle);
        }
    }
    
    /**
     * The Fast Fourier Transform (optimized version for arrays of size 512).
     * 
     * This implementation is optimized specifically for 512-element arrays
     * with precomputed trigonometric lookup tables for better performance.
     *
     * @param inputReal an array of length 512, the real part
     * @param inputImag an array of length 512, the imaginary part
     * @param DIRECT    TRUE = direct transform, FALSE = inverse transform
     * @return a new array of length 1024 (interleaved real and imaginary parts)
     */
    public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {
        // Size validation
        int n = inputReal.length;
        if (n != 512) {
            System.out.println("ERROR: The number of input elements is not 512.");
            return new double[0];
        }

        // Hardcoded parameters for size 512: nu = 9, since 2^9 = 512
        int n2 = 256; // n/2
        int nu1 = 8; // nu - 1
        double[] xReal = new double[512];
        double[] xImag = new double[512];
        double tReal;
        double tImag;
        double p;
        double arg;
        double c;
        double s;

        // Determine transform direction
        double constant = DIRECT ? -2 * Math.PI : 2 * Math.PI;

        // Copy input arrays to avoid modifying originals
        System.arraycopy(inputReal, 0, xReal, 0, 512);
        System.arraycopy(inputImag, 0, xImag, 0, 512);

        // First phase - calculation (9 iterations for 512 elements) with lookup tables
        int k = 0;
        double constant_sign = DIRECT ? -1.0 : 1.0;
        
        for (int l = 1; l <= 9; l++) {
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
        double[] newArray = new double[1024];
        double radice = 1.0 / Math.sqrt(n);
        for (int i = 0; i < 1024; i += 2) {
            int i2 = i / 2;
            newArray[i] = xReal[i2] * radice;
            newArray[i + 1] = xImag[i2] * radice;
        }
        return newArray;
    }

    /**
     * Bit reverse function optimized for 9-bit numbers (0-511).
     * 
     * @param j the number to bit-reverse (0-511)
     * @return the bit-reversed number
     */
    private static int bitreverseReference(int j) {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= 9; i++) { // 9 bits for size 512
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
        }
        return k;
    }
}
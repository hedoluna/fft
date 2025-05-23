/**
 * @author Orlando Selenu (original base), Enhanced by Engine AI Assistant
 * Originally written in the Summer of 2008 
 * Based on the algorithms originally published by E. Oran Brigham "The Fast Fourier Transform" 1973, in ALGOL60 and FORTRAN
 * 
 * Released in the Public Domain.
 */
public class FFToptim128 {
    
    /**
     * The Fast Fourier Transform (optimized version for arrays of size 128).
     * 
     * This implementation is optimized specifically for 128-element arrays
     * and hardcodes many loop parameters for better performance.
     *
     * @param inputReal an array of length 128, the real part
     * @param inputImag an array of length 128, the imaginary part
     * @param DIRECT    TRUE = direct transform, FALSE = inverse transform
     * @return a new array of length 256 (interleaved real and imaginary parts)
     */
    public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {
        // Size validation
        int n = inputReal.length;
        if (n != 128) {
            System.out.println("ERROR: The number of input elements is not 128.");
            return new double[0];
        }

        // Hardcoded parameters for size 128: nu = 7, since 2^7 = 128
        int n2 = 64; // n/2
        int nu1 = 6; // nu - 1
        double[] xReal = new double[128];
        double[] xImag = new double[128];
        double tReal;
        double tImag;
        double p;
        double arg;
        double c;
        double s;

        // Determine transform direction
        double constant = DIRECT ? -2 * Math.PI : 2 * Math.PI;

        // Copy input arrays to avoid modifying originals
        System.arraycopy(inputReal, 0, xReal, 0, 128);
        System.arraycopy(inputImag, 0, xImag, 0, 128);

        // First phase - calculation (7 iterations for 128 elements)
        int k = 0;
        for (int l = 1; l <= 7; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    p = bitreverseReference(k >> nu1);
                    arg = constant * p / n;
                    c = Math.cos(arg);
                    s = Math.sin(arg);
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
        double[] newArray = new double[256];
        double radice = 1.0 / Math.sqrt(n);
        for (int i = 0; i < 256; i += 2) {
            int i2 = i / 2;
            newArray[i] = xReal[i2] * radice;
            newArray[i + 1] = xImag[i2] * radice;
        }
        return newArray;
    }

    /**
     * Bit reverse function optimized for 7-bit numbers (0-127).
     * 
     * @param j the number to bit-reverse (0-127)
     * @return the bit-reversed number
     */
    private static int bitreverseReference(int j) {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= 7; i++) { // 7 bits for size 128
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
        }
        return k;
    }
}
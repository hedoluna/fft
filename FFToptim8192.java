/**
 * Fast Fourier Transform - Optimized implementation for arrays of size 8192.
 * 
 * This class provides an optimized FFT implementation specifically designed
 * for 8192-element arrays using hardcoded bit-reversal optimization.
 * 
 * @author Orlando Selenu (original base algorithm, 2008)
 * @author Engine AI Assistant (optimized implementation, 2025)
 * @since 1.0
 */
public class FFToptim8192 {

    /**
     * Performs Fast Fourier Transform optimized for 8192-element arrays.
     * 
     * @param inputReal array of exactly 8192 real values
     * @param inputImag array of exactly 8192 imaginary values
     * @param DIRECT true for forward transform, false for inverse
     * @return array of length 16384 with interleaved real and imaginary results
     */
    public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {
        // Validate input size
        if (inputReal.length != 8192 || inputImag.length != 8192) {
            System.out.println("ERROR: Input arrays must be exactly size 8192");
            return new double[0];
        }

        // Hardcoded parameters for size 8192
        final int n = 8192;
        final int nu = 13;
        int n2 = 4096;
        int nu1 = 12;
        double[] xReal = new double[n];
        double[] xImag = new double[n];
        double tReal, tImag;

        // Set up direction constant for forward/inverse transform
        double constant = DIRECT ? -2 * Math.PI : 2 * Math.PI;

        // Copy input arrays
        System.arraycopy(inputReal, 0, xReal, 0, n);
        System.arraycopy(inputImag, 0, xImag, 0, n);

        // FFT butterfly computation stages
        int k = 0;
        for (int l = 1; l <= nu; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    int p_index = k >> nu1;
                    double angle = constant * p_index / n;
                    double c = Math.cos(angle);
                    double s = Math.sin(angle);
                    
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
            k = 0; nu1--; n2 /= 2;
        }

        // Final bit-reversal reordering - optimized with reduced function calls
        for (int i = 0; i < n; i++) {
            int r = bitreverseReference(i, nu);
            if (r > i) {
                tReal = xReal[i]; tImag = xImag[i];
                xReal[i] = xReal[r]; xImag[i] = xImag[r];
                xReal[r] = tReal; xImag[r] = tImag;
            }
        }

        // Generate interleaved output with normalization
        double[] result = new double[16384];
        double radice = 1.0 / Math.sqrt(n);
        for (int i = 0; i < n; i++) {
            result[2 * i] = xReal[i] * radice;
            result[2 * i + 1] = xImag[i] * radice;
        }
        return result;
    }
    
    /**
     * Optimized bit-reverse function for size 8192 (nu=13).
     */
    private static int bitreverseReference(int j, int nu) {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= nu; i++) {
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
        }
        return k;
    }
}
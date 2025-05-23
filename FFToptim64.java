/**
 * @author Orlando Selenu (original base), Enhanced by Engine AI Assistant
 * Originally written in the Summer of 2008 
 * Based on the algorithms originally published by E. Oran Brigham "The Fast Fourier Transform" 1973, in ALGOL60 and FORTRAN
 * 
 * Released in the Public Domain.
 */
public class FFToptim64 {
    
    // Precomputed trigonometric lookup tables for 64-point FFT
    private static final double[] COS_TABLE = new double[32];
    private static final double[] SIN_TABLE = new double[32];
    
    static {
        // Initialize lookup tables
        for (int i = 0; i < 32; i++) {
            double angle = 2.0 * Math.PI * i / 64.0;
            COS_TABLE[i] = Math.cos(angle);
            SIN_TABLE[i] = Math.sin(angle);
        }
    }
    
    /**
     * The Fast Fourier Transform (optimized version for arrays of size 64).
     * 
     * This implementation is optimized specifically for 64-element arrays
     * and hardcodes many loop parameters for better performance.
     *
     * @param inputReal an array of length 64, the real part
     * @param inputImag an array of length 64, the imaginary part
     * @param DIRECT    TRUE = direct transform, FALSE = inverse transform
     * @return a new array of length 128 (interleaved real and imaginary parts)
     */
    public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {
        // Size validation
        int n = inputReal.length;
        if (n != 64) {
            System.out.println("ERROR: The number of input elements is not 64.");
            return new double[0];
        }

        // Hardcoded parameters for size 64: nu = 6, since 2^6 = 64
        int n2 = 32; // n/2
        int nu1 = 5; // nu - 1
        double[] xReal = new double[64];
        double[] xImag = new double[64];
        double tReal;
        double tImag;
        int p;
        double arg;
        double c;
        double s;

        // Determine transform direction
        double constant = DIRECT ? -2 * Math.PI : 2 * Math.PI;

        // Copy input arrays to avoid modifying originals
        System.arraycopy(inputReal, 0, xReal, 0, 64);
        System.arraycopy(inputImag, 0, xImag, 0, 64);

        // First phase - calculation (6 stages, completely unrolled)
        
        // Stage 1: n2 = 32, nu1 = 5, distance = 32
        // All twiddle factors are 1+0j (p=0), so c=1, s=0
        for (int i = 0; i < 32; i++) {
            tReal = xReal[i + 32];
            tImag = xImag[i + 32];
            xReal[i + 32] = xReal[i] - tReal;
            xImag[i + 32] = xImag[i] - tImag;
            xReal[i] += tReal;
            xImag[i] += tImag;
        }
        
        // Stage 2: n2 = 16, nu1 = 4, distance = 16
        // First half: p=0 -> c=1, s=0
        for (int i = 0; i < 16; i++) {
            tReal = xReal[i + 16];
            tImag = xImag[i + 16];
            xReal[i + 16] = xReal[i] - tReal;
            xImag[i + 16] = xImag[i] - tImag;
            xReal[i] += tReal;
            xImag[i] += tImag;
        }
        // Second half: p=1 -> c=cos(π/32), s=sin(π/32)*constant
        double c1 = 0.9951847266721969;
        double s1 = constant * 0.09801714032956077;
        for (int i = 32; i < 48; i++) {
            tReal = xReal[i + 16] * c1 + xImag[i + 16] * s1;
            tImag = xImag[i + 16] * c1 - xReal[i + 16] * s1;
            xReal[i + 16] = xReal[i] - tReal;
            xImag[i + 16] = xImag[i] - tImag;
            xReal[i] += tReal;
            xImag[i] += tImag;
        }
        
        // Stage 3: n2 = 8, nu1 = 3, distance = 8
        // k=0-7: p=0 -> c=1, s=0
        for (int i = 0; i < 8; i++) {
            tReal = xReal[i + 8];
            tImag = xImag[i + 8];
            xReal[i + 8] = xReal[i] - tReal;
            xImag[i + 8] = xImag[i] - tImag;
            xReal[i] += tReal;
            xImag[i] += tImag;
        }
        // k=16-23: p=1 -> c=cos(π/16), s=sin(π/16)*constant
        double c2 = 0.9807852804032304;
        double s2 = constant * 0.19509032201612825;
        for (int i = 16; i < 24; i++) {
            tReal = xReal[i + 8] * c2 + xImag[i + 8] * s2;
            tImag = xImag[i + 8] * c2 - xReal[i + 8] * s2;
            xReal[i + 8] = xReal[i] - tReal;
            xImag[i + 8] = xImag[i] - tImag;
            xReal[i] += tReal;
            xImag[i] += tImag;
        }
        // k=32-39: p=0 -> c=1, s=0
        for (int i = 32; i < 40; i++) {
            tReal = xReal[i + 8];
            tImag = xImag[i + 8];
            xReal[i + 8] = xReal[i] - tReal;
            xImag[i + 8] = xImag[i] - tImag;
            xReal[i] += tReal;
            xImag[i] += tImag;
        }
        // k=48-55: p=3 -> c=cos(3π/16), s=sin(3π/16)*constant
        double c3 = 0.8314696123025452;
        double s3 = constant * 0.5555702330196023;
        for (int i = 48; i < 56; i++) {
            tReal = xReal[i + 8] * c3 + xImag[i + 8] * s3;
            tImag = xImag[i + 8] * c3 - xReal[i + 8] * s3;
            xReal[i + 8] = xReal[i] - tReal;
            xImag[i + 8] = xImag[i] - tImag;
            xReal[i] += tReal;
            xImag[i] += tImag;
        }
        
        // Stage 4: n2 = 4, nu1 = 2, distance = 4
        // Unroll for all twiddle factor combinations
        double sqrt2_2 = 0.7071067811865476;
        double c4_0 = 1.0;
        double s4_0 = 0.0;
        double c4_1 = sqrt2_2;
        double s4_1 = constant * sqrt2_2;
        double c4_2 = 0.0;
        double s4_2 = constant * 1.0;
        double c4_3 = -sqrt2_2;
        double s4_3 = constant * sqrt2_2;
        
        // Apply butterflies with appropriate twiddle factors
        for (int block = 0; block < 4; block++) {
            int base = block * 16;
            for (int i = 0; i < 4; i++) {
                // Select twiddle factor based on bit-reversed position
                int k = base + i;
                p = (k >> 2) & 3;
                double tc, ts;
                switch (p) {
                    case 0: tc = c4_0; ts = s4_0; break;
                    case 1: tc = c4_1; ts = s4_1; break;
                    case 2: tc = c4_2; ts = s4_2; break;
                    default: tc = c4_3; ts = s4_3; break;
                }
                
                tReal = xReal[k + 4] * tc + xImag[k + 4] * ts;
                tImag = xImag[k + 4] * tc - xReal[k + 4] * ts;
                xReal[k + 4] = xReal[k] - tReal;
                xImag[k + 4] = xImag[k] - tImag;
                xReal[k] += tReal;
                xImag[k] += tImag;
            }
        }
        
        // Stage 5: n2 = 2, nu1 = 1, distance = 2
        for (int block = 0; block < 16; block++) {
            int base = block * 4;
            for (int i = 0; i < 2; i++) {
                int k = base + i;
                p = (k >> 1) & 1;
                if (p == 0) {
                    // c=1, s=0
                    tReal = xReal[k + 2];
                    tImag = xImag[k + 2];
                    xReal[k + 2] = xReal[k] - tReal;
                    xImag[k + 2] = xImag[k] - tImag;
                    xReal[k] += tReal;
                    xImag[k] += tImag;
                } else {
                    // c=0, s=constant
                    tReal = xImag[k + 2] * constant;
                    tImag = -xReal[k + 2] * constant;
                    xReal[k + 2] = xReal[k] - tReal;
                    xImag[k + 2] = xImag[k] - tImag;
                    xReal[k] += tReal;
                    xImag[k] += tImag;
                }
            }
        }
        
        // Stage 6: n2 = 1, nu1 = 0, distance = 1
        for (int block = 0; block < 32; block++) {
            int k = block * 2;
            p = k & 1;
            if (p == 0) {
                // c=1, s=0
                tReal = xReal[k + 1];
                tImag = xImag[k + 1];
                xReal[k + 1] = xReal[k] - tReal;
                xImag[k + 1] = xImag[k] - tImag;
                xReal[k] += tReal;
                xImag[k] += tImag;
            } else {
                // c=0, s=constant
                tReal = xImag[k + 1] * constant;
                tImag = -xReal[k + 1] * constant;
                xReal[k + 1] = xReal[k] - tReal;
                xImag[k + 1] = xImag[k] - tImag;
                xReal[k] += tReal;
                xImag[k] += tImag;
            }
        }

        // Second phase - bit-reversal recombination (hardcoded swaps for 64-point)
        // Precomputed bit-reverse pairs that need swapping
        int[] bitReversePairs = {
            1, 32, 2, 16, 3, 48, 4, 8, 5, 40, 6, 24, 7, 56, 
            9, 36, 10, 20, 11, 52, 12, 12, 13, 44, 14, 28, 15, 60,
            17, 34, 18, 18, 19, 50, 21, 42, 22, 26, 23, 58,
            25, 38, 27, 54, 29, 46, 31, 62, 33, 33, 35, 49,
            37, 41, 39, 57, 43, 53, 45, 51, 47, 59
        };
        
        for (int i = 0; i < bitReversePairs.length; i += 2) {
            int k = bitReversePairs[i];
            int r = bitReversePairs[i + 1];
            if (r > k) {
                tReal = xReal[k];
                tImag = xImag[k];
                xReal[k] = xReal[r];
                xImag[k] = xImag[r];
                xReal[r] = tReal;
                xImag[r] = tImag;
            }
        }

        // Combine real and imaginary parts into output array with normalization
        double[] newArray = new double[128];
        double radice = 1.0 / Math.sqrt(n);
        for (int i = 0; i < 128; i += 2) {
            int i2 = i / 2;
            newArray[i] = xReal[i2] * radice;
            newArray[i + 1] = xImag[i2] * radice;
        }
        return newArray;
    }

    /**
     * Bit reverse function optimized for 6-bit numbers (0-63).
     * 
     * @param j the number to bit-reverse (0-63)
     * @return the bit-reversed number
     */
    private static int bitreverseReference(int j) {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= 6; i++) { // 6 bits for size 64
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
        }
        return k;
    }
}
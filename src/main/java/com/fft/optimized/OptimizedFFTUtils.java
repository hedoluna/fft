package com.fft.optimized;

import com.fft.core.FFTBase;

/**
 * Utility class containing optimized FFT implementations without reflection dependencies.
 * Features precomputed twiddle factors and stage-by-stage optimizations.
 */
public class OptimizedFFTUtils {

    // Precomputed twiddle factors for 8-point FFT (k = 0..7)
    private static final double[] TWIDDLES_8_REAL = {
        1.0,
        0.7071067811865476,
        6.123233995736766e-17,
        -0.7071067811865475,
        -1.0,
        -0.7071067811865477,
        -1.8369701987210297e-16,
        0.7071067811865474
    };

    private static final double[] TWIDDLES_8_IMAG = {
        0.0,
        -0.7071067811865475,
        -1.0,
        -0.7071067811865476,
        -1.2246467991473532e-16,
        0.7071067811865475,
        1.0,
        0.7071067811865477
    };

    // Precomputed twiddle factors for 16-point FFT (k = 0..15)
    private static final double[] TWIDDLES_16_REAL = {
        1.0,
        0.9238795325112867,
        0.7071067811865476,
        0.3826834323650898,
        6.123233995736766e-17,
        -0.3826834323650897,
        -0.7071067811865475,
        -0.9238795325112867,
        -1.0,
        -0.9238795325112868,
        -0.7071067811865477,
        -0.38268343236509034,
        -1.8369701987210297e-16,
        0.38268343236509,
        0.7071067811865474,
        0.9238795325112865
    };

    private static final double[] TWIDDLES_16_IMAG = {
        0.0,
        -0.3826834323650898,
        -0.7071067811865475,
        -0.9238795325112867,
        -1.0,
        -0.9238795325112867,
        -0.7071067811865476,
        -0.3826834323650899,
        -1.2246467991473532e-16,
        0.38268343236508967,
        0.7071067811865475,
        0.9238795325112865,
        1.0,
        0.9238795325112866,
        0.7071067811865477,
        0.3826834323650904
    };
    
    // Precomputed twiddle factors for 32-point FFT
    // Real parts: cos(-2πk/32) for k = 0, 1, 2, ..., 15
    private static final double[] TWIDDLES_32_REAL = {
        1.0,                    // cos(0)
        0.9807852804032304,     // cos(-π/16) 
        0.9238795325112867,     // cos(-π/8)
        0.8314696123025452,     // cos(-3π/16)
        0.7071067811865476,     // cos(-π/4)
        0.5555702330196022,     // cos(-5π/16)
        0.3826834323650898,     // cos(-3π/8)
        0.19509032201612825,    // cos(-7π/16)
        6.123233995736766e-17,  // cos(-π/2) ≈ 0
        -0.1950903220161282,    // cos(-9π/16)
        -0.3826834323650897,    // cos(-5π/8)
        -0.5555702330196023,    // cos(-11π/16)
        -0.7071067811865475,    // cos(-3π/4)
        -0.8314696123025453,    // cos(-13π/16)
        -0.9238795325112867,    // cos(-7π/8)
        -0.9807852804032304     // cos(-15π/16)
    };
    
    // Imaginary parts: sin(-2πk/32) for k = 0, 1, 2, ..., 15
    private static final double[] TWIDDLES_32_IMAG = {
        0.0,                    // sin(0)
        -0.19509032201612825,   // sin(-π/16)
        -0.3826834323650898,    // sin(-π/8)
        -0.5555702330196022,    // sin(-3π/16)
        -0.7071067811865475,    // sin(-π/4)
        -0.8314696123025453,    // sin(-5π/16)
        -0.9238795325112867,    // sin(-3π/8)
        -0.9807852804032304,    // sin(-7π/16)
        -1.0,                   // sin(-π/2)
        -0.9807852804032304,    // sin(-9π/16)
        -0.9238795325112867,    // sin(-5π/8)
        -0.8314696123025452,    // sin(-11π/16)
        -0.7071067811865476,    // sin(-3π/4)
        -0.5555702330196022,    // sin(-13π/16)
        -0.3826834323650898,    // sin(-7π/8)
        -0.19509032201612833    // sin(-15π/16)
    };

    // Precomputed twiddle factors for 64-point FFT
    // Real parts: cos(-2πk/64) for k = 0..31
    private static final double[] TWIDDLES_64_REAL = {
        1.0,
        0.9951847266721969,
        0.9807852804032304,
        0.9569403357322088,
        0.9238795325112867,
        0.881921264348355,
        0.8314696123025452,
        0.773010453362737,
        0.7071067811865476,
        0.6343932841636455,
        0.5555702330196023,
        0.4713967368259978,
        0.38268343236508984,
        0.29028467725446233,
        0.19509032201612833,
        0.09801714032956077,
        6.123233995736766e-17,
        -0.09801714032956065,
        -0.1950903220161282,
        -0.29028467725446216,
        -0.3826834323650897,
        -0.4713967368259977,
        -0.555570233019602,
        -0.6343932841636454,
        -0.7071067811865475,
        -0.773010453362737,
        -0.8314696123025453,
        -0.8819212643483549,
        -0.9238795325112867,
        -0.9569403357322088,
        -0.9807852804032304,
        -0.9951847266721968
    };

    // Imaginary parts: sin(-2πk/64) for k = 0..31
    private static final double[] TWIDDLES_64_IMAG = {
        0.0,
        -0.0980171403295606,
        -0.19509032201612825,
        -0.29028467725446233,
        -0.3826834323650898,
        -0.47139673682599764,
        -0.5555702330196022,
        -0.6343932841636455,
        -0.7071067811865475,
        -0.773010453362737,
        -0.8314696123025452,
        -0.8819212643483549,
        -0.9238795325112867,
        -0.9569403357322089,
        -0.9807852804032304,
        -0.9951847266721968,
        -1.0,
        -0.9951847266721969,
        -0.9807852804032304,
        -0.9569403357322089,
        -0.9238795325112867,
        -0.881921264348355,
        -0.8314696123025455,
        -0.7730104533627371,
        -0.7071067811865476,
        -0.6343932841636455,
        -0.5555702330196022,
        -0.47139673682599786,
        -0.3826834323650899,
        -0.2902846772544624,
        -0.1950903220161286,
        -0.09801714032956083
    };

    /** 8-point FFT using precomputed twiddle factors */
    public static double[] fft8(double[] inputReal, double[] inputImag, boolean forward) {
        if (inputReal.length != 8 || inputImag.length != 8) {
            throw new IllegalArgumentException("Arrays must be of length 8");
        }

        int n = 8;
        int nu = 3;
        int n2 = n / 2;
        int nu1 = nu - 1;
        double[] xReal = new double[n];
        double[] xImag = new double[n];
        System.arraycopy(inputReal, 0, xReal, 0, n);
        System.arraycopy(inputImag, 0, xImag, 0, n);

        double tReal;
        double tImag;
        double c;
        double s;

        int k = 0;
        for (int l = 1; l <= nu; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    int p = bitreverseReference(k >> nu1, nu);
                    c = TWIDDLES_8_REAL[p];
                    s = forward ? TWIDDLES_8_IMAG[p] : -TWIDDLES_8_IMAG[p];

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

        k = 0;
        while (k < n) {
            int r = bitreverseReference(k, nu);
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

        double[] result = new double[2 * n];
        double radice = 1 / Math.sqrt(n);
        for (int i = 0; i < n; i++) {
            result[2 * i] = xReal[i] * radice;
            result[2 * i + 1] = xImag[i] * radice;
        }
        return result;
    }

    public static double[] ifft8(double[] inputReal, double[] inputImag) {
        return fft8(inputReal, inputImag, false);
    }

    /** 16-point FFT fallback */
    public static double[] fft16(double[] inputReal, double[] inputImag, boolean forward) {
        if (inputReal.length != 16 || inputImag.length != 16) {
            throw new IllegalArgumentException("Arrays must be of length 16");
        }

        int n = 16;
        int nu = 4;
        int n2 = n / 2;
        int nu1 = nu - 1;
        double[] xReal = new double[n];
        double[] xImag = new double[n];
        System.arraycopy(inputReal, 0, xReal, 0, n);
        System.arraycopy(inputImag, 0, xImag, 0, n);

        double tReal;
        double tImag;
        double c;
        double s;

        int k = 0;
        for (int l = 1; l <= nu; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    int p = bitreverseReference(k >> nu1, nu);
                    c = TWIDDLES_16_REAL[p];
                    s = forward ? TWIDDLES_16_IMAG[p] : -TWIDDLES_16_IMAG[p];

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

        k = 0;
        while (k < n) {
            int r = bitreverseReference(k, nu);
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

        double[] result = new double[2 * n];
        double radice = 1 / Math.sqrt(n);
        for (int i = 0; i < n; i++) {
            result[2 * i] = xReal[i] * radice;
            result[2 * i + 1] = xImag[i] * radice;
        }
        return result;
    }

    public static double[] ifft16(double[] inputReal, double[] inputImag) {
        return fft16(inputReal, inputImag, false);
    }

    /** 32-point FFT with precomputed twiddle factors - Stage 1: Basic algorithm with twiddles */
    public static double[] fft32(double[] inputReal, double[] inputImag, boolean forward) {
        if (inputReal.length != 32 || inputImag.length != 32) {
            throw new IllegalArgumentException("Arrays must be of length 32");
        }
        
        // This is the exact same algorithm as FFTBase.fft() but with precomputed twiddle factors
        int n = 32;
        
        // Check that n is a power of 2 (we know it is for 32, but keep for consistency)
        double ld = Math.log(n) / Math.log(2.0);
        if (((int) ld) - ld != 0) {
            throw new IllegalArgumentException("The number of elements is not a power of 2.");
        }
        
        int nu = (int) ld;
        int n2 = n / 2;
        int nu1 = nu - 1;
        double[] xReal = new double[n];
        double[] xImag = new double[n];
        double tReal;
        double tImag;
        double p;
        double c;
        double s;
        
        // Copy input arrays
        for (int i = 0; i < n; i++) {
            xReal[i] = inputReal[i];
            xImag[i] = inputImag[i];
        }
        
        // First phase - calculation
        int k = 0;
        for (int l = 1; l <= nu; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    p = bitreverseReference(k >> nu1, nu);

                    // Use precomputed twiddle factors instead of Math.cos/sin
                    // Map to precomputed table with correct sign handling
                    int pInt = (int) Math.round(p);
                    int index = pInt % 16;  // Table contains values for 0-15
                    int sign = pInt >= 16 ? -1 : 1;
                    if (forward) {
                        c = sign * TWIDDLES_32_REAL[index];
                        s = sign * TWIDDLES_32_IMAG[index];
                    } else {
                        c = sign * TWIDDLES_32_REAL[index];
                        s = -sign * TWIDDLES_32_IMAG[index]; // Negate for inverse and sign
                    }
                    
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
        
        // Second phase - recombination
        k = 0;
        int r;
        while (k < n) {
            r = bitreverseReference(k, nu);
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
        
        // Normalization and output (same as FFTBase)
        double[] newArray = new double[2 * n];
        double radice = 1 / Math.sqrt(n);
        for (int i = 0; i < n; i++) {
            newArray[2 * i] = xReal[i] * radice;
            newArray[2 * i + 1] = xImag[i] * radice;
        }
        return newArray;
    }

    public static double[] ifft32(double[] inputReal, double[] inputImag) {
        return fft32(inputReal, inputImag, false);
    }
    
    /**
     * Reverse 5 bits for 32-point FFT bit-reversal permutation
     */
    private static int reverseBits5(int x) {
        int result = 0;
        result |= ((x & 0x01) << 4);  // Bit 0 -> Bit 4
        result |= ((x & 0x02) << 2);  // Bit 1 -> Bit 3
        result |= ((x & 0x04) << 0);  // Bit 2 -> Bit 2 (stays)
        result |= ((x & 0x08) >> 2);  // Bit 3 -> Bit 1
        result |= ((x & 0x10) >> 4);  // Bit 4 -> Bit 0
        return result;
    }
    
    /**
     * The bit reversing function from FFTBase, used for twiddle factor computation
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

    /**
     * Optimized 64-point FFT implementation with precomputed twiddle factors.
     * This method is based on {@link FFTBase#fft(double[], double[], boolean)}
     * but avoids expensive trigonometric calls by using lookup tables.
     */
    public static double[] fft64(double[] inputReal, double[] inputImag, boolean forward) {
        if (inputReal.length != 64 || inputImag.length != 64) {
            throw new IllegalArgumentException("Arrays must be of length 64");
        }

        int n = 64;
        int nu = 6;
        int n2 = n / 2;
        int nu1 = nu - 1;

        double[] xReal = new double[n];
        double[] xImag = new double[n];
        System.arraycopy(inputReal, 0, xReal, 0, n);
        System.arraycopy(inputImag, 0, xImag, 0, n);

        double tReal;
        double tImag;
        double c;
        double s;

        int k = 0;
        for (int l = 1; l <= nu; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    int p = bitreverseReference(k >> nu1, nu);
                    int index = p & 31; // p mod 32
                    if (forward) {
                        c = TWIDDLES_64_REAL[index];
                        s = TWIDDLES_64_IMAG[index];
                    } else {
                        c = TWIDDLES_64_REAL[index];
                        s = -TWIDDLES_64_IMAG[index];
                    }

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

        k = 0;
        while (k < n) {
            int r = bitreverseReference(k, nu);
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

        double[] newArray = new double[2 * n];
        double radice = 1 / Math.sqrt(n);
        for (int i = 0; i < n; i++) {
            newArray[2 * i] = xReal[i] * radice;
            newArray[2 * i + 1] = xImag[i] * radice;
        }
        return newArray;
    }

    public static double[] ifft64(double[] inputReal, double[] inputImag) {
        return fft64(inputReal, inputImag, false);
    }
}
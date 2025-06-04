package com.fft.optimized;

import com.fft.core.FFTBase;

/**
 * Utility class containing optimized FFT implementations without reflection dependencies.
 * Features precomputed twiddle factors and stage-by-stage optimizations.
 */
public class OptimizedFFTUtils {
    
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

    /** 8-point FFT fallback */
    public static double[] fft8(double[] inputReal, double[] inputImag, boolean forward) {
        return new FFTBase().transform(inputReal, inputImag, forward).getInterleavedResult();
    }

    public static double[] ifft8(double[] inputReal, double[] inputImag) {
        return new FFTBase().transform(inputReal, inputImag, false).getInterleavedResult();
    }

    /** 16-point FFT fallback */
    public static double[] fft16(double[] inputReal, double[] inputImag, boolean forward) {
        return new FFTBase().transform(inputReal, inputImag, forward).getInterleavedResult();
    }

    public static double[] ifft16(double[] inputReal, double[] inputImag) {
        return new FFTBase().transform(inputReal, inputImag, false).getInterleavedResult();
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

    /** 64-point FFT fallback */
    public static double[] fft64(double[] inputReal, double[] inputImag, boolean forward) {
        return new FFTBase().transform(inputReal, inputImag, forward).getInterleavedResult();
    }

    public static double[] ifft64(double[] inputReal, double[] inputImag) {
        return new FFTBase().transform(inputReal, inputImag, false).getInterleavedResult();
    }
}
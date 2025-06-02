package com.fft.core;

import com.fft.factory.FFTImplementation;

/**
 * Fast Fourier Transform (FFT) - Generic Reference Implementation
 * 
 * This class provides a generic, unoptimized implementation of the Fast Fourier Transform
 * using the Cooley-Tukey algorithm. It serves as both a reference implementation and
 * a fallback for sizes that don't have specialized optimized versions.
 * 
 * <p>The implementation uses the decimation-in-frequency approach with bit-reversal
 * permutation and supports both forward and inverse transforms. While not optimized
 * for performance, this implementation is clear, well-documented, and works for any
 * power-of-2 input size.</p>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * FFT fft = new FFTBase();
 * double[] real = {1, 2, 3, 4, 5, 6, 7, 8};
 * double[] imag = {0, 0, 0, 0, 0, 0, 0, 0};
 * 
 * // Forward FFT
 * FFTResult result = fft.transform(real, imag, true);
 * 
 * // Inverse FFT
 * FFTResult inverse = fft.transform(result.getRealParts(), result.getImaginaryParts(), false);
 * }</pre>
 * 
 * <h3>Algorithm Details:</h3>
 * <ul>
 * <li>Complexity: O(n log n)</li>
 * <li>Memory: O(n) additional space for computation</li>
 * <li>Normalization: 1/√n factor applied to results</li>
 * <li>Bit-reversal: Performed in final recombination stage</li>
 * </ul>
 * 
 * @author Orlando Selenu (original implementation, 2008)
 * @author Engine AI Assistant (enhanced documentation and refactoring, 2025)
 * @since 1.0
 * @see FFT for interface details
 * @see "E. Oran Brigham, The Fast Fourier Transform, 1973"
 */
@FFTImplementation(
    size = -1, // Supports any power-of-2 size
    priority = 1, // Lowest priority - used as fallback
    description = "Generic FFT implementation (Cooley-Tukey algorithm)",
    characteristics = {"generic", "reference-implementation", "fallback", "cooley-tukey", "unoptimized"}
)
public class FFTBase implements FFT {
    
    @Override
    public FFTResult transform(double[] real, double[] imaginary, boolean forward) {
        if (real.length != imaginary.length) {
            throw new IllegalArgumentException("Real and imaginary arrays must have same length");
        }
        
        double[] result = fft(real, imaginary, forward);
        if (result.length == 0) {
            throw new IllegalArgumentException("Array length must be a power of 2");
        }
        
        return new FFTResult(result);
    }
    
    @Override
    public FFTResult transform(double[] real, boolean forward) {
        double[] imaginary = new double[real.length];
        return transform(real, imaginary, forward);
    }
    
    @Override
    public int getSupportedSize() {
        return -1; // Supports any power-of-2 size
    }
    
    @Override
    public boolean supportsSize(int size) {
        return isPowerOfTwo(size);
    }
    
    @Override
    public String getDescription() {
        return "Generic FFT implementation (Cooley-Tukey algorithm)";
    }
    
    /**
     * Checks if a number is a power of 2.
     * 
     * @param n the number to check
     * @return true if n is a power of 2, false otherwise
     */
    private static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
    
    /**
     * Performs Fast Fourier Transform using the Cooley-Tukey algorithm.
     * 
     * <p>This is the generic reference implementation that works for any power-of-2 size.
     * It uses the decimation-in-frequency approach with bit-reversal permutation.
     * The algorithm performs the transform in-place to minimize memory usage, but
     * creates a copy of the input arrays to avoid modifying the original data.</p>
     * 
     * <h4>Mathematical Definition:</h4>
     * <ul>
     * <li>Forward: X[k] = (1/√n) * Σ(j=0 to n-1) x[j] * e^(-2πijk/n)</li>
     * <li>Inverse: x[j] = (1/√n) * Σ(k=0 to n-1) X[k] * e^(2πijk/n)</li>
     * </ul>
     * 
     * <h4>Performance Characteristics:</h4>
     * <ul>
     * <li>Time Complexity: O(n log n)</li>
     * <li>Space Complexity: O(n) additional memory</li>
     * <li>Numerical Stability: Excellent for practical applications</li>
     * <li>Optimization Level: None (reference implementation)</li>
     * </ul>
     *
     * @param inputReal array of length n (must be a power of 2) containing real parts
     * @param inputImag array of length n (must be a power of 2) containing imaginary parts
     * @param DIRECT true for forward transform (time→frequency), false for inverse transform (frequency→time)
     * @return new array of length 2n with interleaved real and imaginary parts:
     *         [real0, imag0, real1, imag1, ...], or empty array if input size is not a power of 2
     * @see #bitreverseReference(int, int) for bit-reversal implementation details
     * 
     * @since 1.0
     */
    public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {
        // - n is the dimension of the problem
        // - nu is its logarithm in base e
        int n = inputReal.length;

        // If n is a power of 2, then ld is an integer (_without_ decimals)
        double ld = Math.log(n) / Math.log(2.0);

        // Here I check if n is a power of 2. If exist decimals in ld, I quit
        // from the function returning null.
        if (((int) ld) - ld != 0) {
            System.out.println("The number of elements is not a power of 2.");
            return new double[0];
        }

        // Declaration and initialization of the variables
        // ld should be an integer, actually, so I don't lose any information in
        // the cast
        int nu = (int) ld;
        int n2 = n / 2;
        int nu1 = nu - 1;
        double[] xReal = new double[n];
        double[] xImag = new double[n];
        double tReal;
        double tImag;
        double p;
        double arg;
        double c;
        double s;

        // Here I copy the inputReal in xReal and inputImag in xImag
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
                    // direct FFT or inverse FFT
                    if (DIRECT)
                        arg = -2 * (double) Math.PI * p / n;
                    else
                        arg = 2 * (double) Math.PI * p / n;
                    c = (double) Math.cos(arg);
                    s = (double) Math.sin(arg);
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

        // Here I have to normalize the result (just for the direct FFT)
        double[] newArray = new double[2 * n];
        double radice = 1 / Math.sqrt(n);
        for (int i = 0; i < n; i++) {
            newArray[2 * i] = xReal[i] * radice;
            newArray[2 * i + 1] = xImag[i] * radice;
        }
        return newArray;
    }

    /**
     * The bit reversing function, which is used for the second phase of the
     * recursive algorithm
     * 
     * @param j input value for bit reversal
     * @param nu number of bits to reverse
     * @return bit-reversed value of j
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
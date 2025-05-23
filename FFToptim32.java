/**
 * @author Orlando Selenu Originally written in the Summer of 2008 Based on the
 * algorithms originally published by E. Oran Brigham "The Fast Fourier
 * Transform" 1973, in ALGOL60 and FORTRAN
 * Enhanced with complete loop unrolling by Engine AI Assistant
 * <p>
 * Released in the Public Domain.
 */
public class FFToptim32 {
    /**
     * The Fast Fourier Transform (fully unrolled optimized version for arrays of size 32).
     * 
     * This implementation is highly optimized for 32-element arrays with
     * completely unrolled loops and precomputed trigonometric values.
     *
     * @param inputReal an array of length 32, the real part
     * @param inputImag an array of length 32, the imaginary part
     * @param DIRECT    TRUE = direct transform, FALSE = inverse transform
     * @return a new array of length 64 (interleaved real and imaginary parts)
     */
    public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {
        int n = inputReal.length;

        if (n != 32) {
            System.out.println("The number of elements is not 32.");
            return new double[0];
        }

        // Working arrays
        double[] xReal = new double[32];
        double[] xImag = new double[32];
        double tReal;
        double tImag;

        // Copy input arrays (using System.arraycopy for performance)
        System.arraycopy(inputReal, 0, xReal, 0, 32);
        System.arraycopy(inputImag, 0, xImag, 0, 32);

        // Precomputed trigonometric constants for 32-point FFT
        // These are cos(2πk/32) and sin(2πk/32) for various k values
        double constant = DIRECT ? -1.0 : 1.0;
        
        // Stage 1: n2 = 16, nu1 = 4
        // Butterflies with distance 16
        
        // k=0, i=1-16, p=0 -> c=1, s=0
        tReal = xReal[16];
        tImag = xImag[16];
        xReal[16] = xReal[0] - tReal;
        xImag[16] = xImag[0] - tImag;
        xReal[0] += tReal;
        xImag[0] += tImag;
        
        tReal = xReal[17];
        tImag = xImag[17];
        xReal[17] = xReal[1] - tReal;
        xImag[17] = xImag[1] - tImag;
        xReal[1] += tReal;
        xImag[1] += tImag;
        
        tReal = xReal[18];
        tImag = xImag[18];
        xReal[18] = xReal[2] - tReal;
        xImag[18] = xImag[2] - tImag;
        xReal[2] += tReal;
        xImag[2] += tImag;
        
        tReal = xReal[19];
        tImag = xImag[19];
        xReal[19] = xReal[3] - tReal;
        xImag[19] = xImag[3] - tImag;
        xReal[3] += tReal;
        xImag[3] += tImag;
        
        tReal = xReal[20];
        tImag = xImag[20];
        xReal[20] = xReal[4] - tReal;
        xImag[20] = xImag[4] - tImag;
        xReal[4] += tReal;
        xImag[4] += tImag;
        
        tReal = xReal[21];
        tImag = xImag[21];
        xReal[21] = xReal[5] - tReal;
        xImag[21] = xImag[5] - tImag;
        xReal[5] += tReal;
        xImag[5] += tImag;
        
        tReal = xReal[22];
        tImag = xImag[22];
        xReal[22] = xReal[6] - tReal;
        xImag[22] = xImag[6] - tImag;
        xReal[6] += tReal;
        xImag[6] += tImag;
        
        tReal = xReal[23];
        tImag = xImag[23];
        xReal[23] = xReal[7] - tReal;
        xImag[23] = xImag[7] - tImag;
        xReal[7] += tReal;
        xImag[7] += tImag;
        
        tReal = xReal[24];
        tImag = xImag[24];
        xReal[24] = xReal[8] - tReal;
        xImag[24] = xImag[8] - tImag;
        xReal[8] += tReal;
        xImag[8] += tImag;
        
        tReal = xReal[25];
        tImag = xImag[25];
        xReal[25] = xReal[9] - tReal;
        xImag[25] = xImag[9] - tImag;
        xReal[9] += tReal;
        xImag[9] += tImag;
        
        tReal = xReal[26];
        tImag = xImag[26];
        xReal[26] = xReal[10] - tReal;
        xImag[26] = xImag[10] - tImag;
        xReal[10] += tReal;
        xImag[10] += tImag;
        
        tReal = xReal[27];
        tImag = xImag[27];
        xReal[27] = xReal[11] - tReal;
        xImag[27] = xImag[11] - tImag;
        xReal[11] += tReal;
        xImag[11] += tImag;
        
        tReal = xReal[28];
        tImag = xImag[28];
        xReal[28] = xReal[12] - tReal;
        xImag[28] = xImag[12] - tImag;
        xReal[12] += tReal;
        xImag[12] += tImag;
        
        tReal = xReal[29];
        tImag = xImag[29];
        xReal[29] = xReal[13] - tReal;
        xImag[29] = xImag[13] - tImag;
        xReal[13] += tReal;
        xImag[13] += tImag;
        
        tReal = xReal[30];
        tImag = xImag[30];
        xReal[30] = xReal[14] - tReal;
        xImag[30] = xImag[14] - tImag;
        xReal[14] += tReal;
        xImag[14] += tImag;
        
        tReal = xReal[31];
        tImag = xImag[31];
        xReal[31] = xReal[15] - tReal;
        xImag[31] = xImag[15] - tImag;
        xReal[15] += tReal;
        xImag[15] += tImag;

        // Stage 2: n2 = 8, nu1 = 3
        // k=0, p=0 -> c=1, s=0
        tReal = xReal[8];
        tImag = xImag[8];
        xReal[8] = xReal[0] - tReal;
        xImag[8] = xImag[0] - tImag;
        xReal[0] += tReal;
        xImag[0] += tImag;
        
        tReal = xReal[9];
        tImag = xImag[9];
        xReal[9] = xReal[1] - tReal;
        xImag[9] = xImag[1] - tImag;
        xReal[1] += tReal;
        xImag[1] += tImag;
        
        tReal = xReal[10];
        tImag = xImag[10];
        xReal[10] = xReal[2] - tReal;
        xImag[10] = xImag[2] - tImag;
        xReal[2] += tReal;
        xImag[2] += tImag;
        
        tReal = xReal[11];
        tImag = xImag[11];
        xReal[11] = xReal[3] - tReal;
        xImag[11] = xImag[3] - tImag;
        xReal[3] += tReal;
        xImag[3] += tImag;
        
        tReal = xReal[12];
        tImag = xImag[12];
        xReal[12] = xReal[4] - tReal;
        xImag[12] = xImag[4] - tImag;
        xReal[4] += tReal;
        xImag[4] += tImag;
        
        tReal = xReal[13];
        tImag = xImag[13];
        xReal[13] = xReal[5] - tReal;
        xImag[13] = xImag[5] - tImag;
        xReal[5] += tReal;
        xImag[5] += tImag;
        
        tReal = xReal[14];
        tImag = xImag[14];
        xReal[14] = xReal[6] - tReal;
        xImag[14] = xImag[6] - tImag;
        xReal[6] += tReal;
        xImag[6] += tImag;
        
        tReal = xReal[15];
        tImag = xImag[15];
        xReal[15] = xReal[7] - tReal;
        xImag[15] = xImag[7] - tImag;
        xReal[7] += tReal;
        xImag[7] += tImag;
        
        // k=16, p=1 -> c=cos(π/16), s=sin(π/16)*constant
        double c1 = 0.9807852804032304;
        double s1 = constant * 0.19509032201612825;
        tReal = xReal[24] * c1 + xImag[24] * s1;
        tImag = xImag[24] * c1 - xReal[24] * s1;
        xReal[24] = xReal[16] - tReal;
        xImag[24] = xImag[16] - tImag;
        xReal[16] += tReal;
        xImag[16] += tImag;
        
        tReal = xReal[25] * c1 + xImag[25] * s1;
        tImag = xImag[25] * c1 - xReal[25] * s1;
        xReal[25] = xReal[17] - tReal;
        xImag[25] = xImag[17] - tImag;
        xReal[17] += tReal;
        xImag[17] += tImag;
        
        tReal = xReal[26] * c1 + xImag[26] * s1;
        tImag = xImag[26] * c1 - xReal[26] * s1;
        xReal[26] = xReal[18] - tReal;
        xImag[26] = xImag[18] - tImag;
        xReal[18] += tReal;
        xImag[18] += tImag;
        
        tReal = xReal[27] * c1 + xImag[27] * s1;
        tImag = xImag[27] * c1 - xReal[27] * s1;
        xReal[27] = xReal[19] - tReal;
        xImag[27] = xImag[19] - tImag;
        xReal[19] += tReal;
        xImag[19] += tImag;
        
        tReal = xReal[28] * c1 + xImag[28] * s1;
        tImag = xImag[28] * c1 - xReal[28] * s1;
        xReal[28] = xReal[20] - tReal;
        xImag[28] = xImag[20] - tImag;
        xReal[20] += tReal;
        xImag[20] += tImag;
        
        tReal = xReal[29] * c1 + xImag[29] * s1;
        tImag = xImag[29] * c1 - xReal[29] * s1;
        xReal[29] = xReal[21] - tReal;
        xImag[29] = xImag[21] - tImag;
        xReal[21] += tReal;
        xImag[21] += tImag;
        
        tReal = xReal[30] * c1 + xImag[30] * s1;
        tImag = xImag[30] * c1 - xReal[30] * s1;
        xReal[30] = xReal[22] - tReal;
        xImag[30] = xImag[22] - tImag;
        xReal[22] += tReal;
        xImag[22] += tImag;
        
        tReal = xReal[31] * c1 + xImag[31] * s1;
        tImag = xImag[31] * c1 - xReal[31] * s1;
        xReal[31] = xReal[23] - tReal;
        xImag[31] = xImag[23] - tImag;
        xReal[23] += tReal;
        xImag[23] += tImag;

        // Stage 3: n2 = 4, nu1 = 2
        // k=0, p=0 -> c=1, s=0
        tReal = xReal[4];
        tImag = xImag[4];
        xReal[4] = xReal[0] - tReal;
        xImag[4] = xImag[0] - tImag;
        xReal[0] += tReal;
        xImag[0] += tImag;
        
        tReal = xReal[5];
        tImag = xImag[5];
        xReal[5] = xReal[1] - tReal;
        xImag[5] = xImag[1] - tImag;
        xReal[1] += tReal;
        xImag[1] += tImag;
        
        tReal = xReal[6];
        tImag = xImag[6];
        xReal[6] = xReal[2] - tReal;
        xImag[6] = xImag[2] - tImag;
        xReal[2] += tReal;
        xImag[2] += tImag;
        
        tReal = xReal[7];
        tImag = xImag[7];
        xReal[7] = xReal[3] - tReal;
        xImag[7] = xImag[3] - tImag;
        xReal[3] += tReal;
        xImag[3] += tImag;
        
        // k=8, p=1 -> c=√2/2, s=√2/2*constant
        double sqrt2_2 = 0.7071067811865476;
        double cs2 = sqrt2_2;
        double ss2 = constant * sqrt2_2;
        tReal = xReal[12] * cs2 + xImag[12] * ss2;
        tImag = xImag[12] * cs2 - xReal[12] * ss2;
        xReal[12] = xReal[8] - tReal;
        xImag[12] = xImag[8] - tImag;
        xReal[8] += tReal;
        xImag[8] += tImag;
        
        tReal = xReal[13] * cs2 + xImag[13] * ss2;
        tImag = xImag[13] * cs2 - xReal[13] * ss2;
        xReal[13] = xReal[9] - tReal;
        xImag[13] = xImag[9] - tImag;
        xReal[9] += tReal;
        xImag[9] += tImag;
        
        tReal = xReal[14] * cs2 + xImag[14] * ss2;
        tImag = xImag[14] * cs2 - xReal[14] * ss2;
        xReal[14] = xReal[10] - tReal;
        xImag[14] = xImag[10] - tImag;
        xReal[10] += tReal;
        xImag[10] += tImag;
        
        tReal = xReal[15] * cs2 + xImag[15] * ss2;
        tImag = xImag[15] * cs2 - xReal[15] * ss2;
        xReal[15] = xReal[11] - tReal;
        xImag[15] = xImag[11] - tImag;
        xReal[11] += tReal;
        xImag[11] += tImag;
        
        // k=16, p=0 -> c=1, s=0
        tReal = xReal[20];
        tImag = xImag[20];
        xReal[20] = xReal[16] - tReal;
        xImag[20] = xImag[16] - tImag;
        xReal[16] += tReal;
        xImag[16] += tImag;
        
        tReal = xReal[21];
        tImag = xImag[21];
        xReal[21] = xReal[17] - tReal;
        xImag[21] = xImag[17] - tImag;
        xReal[17] += tReal;
        xImag[17] += tImag;
        
        tReal = xReal[22];
        tImag = xImag[22];
        xReal[22] = xReal[18] - tReal;
        xImag[22] = xImag[18] - tImag;
        xReal[18] += tReal;
        xImag[18] += tImag;
        
        tReal = xReal[23];
        tImag = xImag[23];
        xReal[23] = xReal[19] - tReal;
        xImag[23] = xImag[19] - tImag;
        xReal[19] += tReal;
        xImag[19] += tImag;
        
        // k=24, p=3 -> c=cos(3π/8), s=sin(3π/8)*constant  
        double c3 = 0.38268343236509;
        double s3 = constant * 0.9238795325112867;
        tReal = xReal[28] * c3 + xImag[28] * s3;
        tImag = xImag[28] * c3 - xReal[28] * s3;
        xReal[28] = xReal[24] - tReal;
        xImag[28] = xImag[24] - tImag;
        xReal[24] += tReal;
        xImag[24] += tImag;
        
        tReal = xReal[29] * c3 + xImag[29] * s3;
        tImag = xImag[29] * c3 - xReal[29] * s3;
        xReal[29] = xReal[25] - tReal;
        xImag[29] = xImag[25] - tImag;
        xReal[25] += tReal;
        xImag[25] += tImag;
        
        tReal = xReal[30] * c3 + xImag[30] * s3;
        tImag = xImag[30] * c3 - xReal[30] * s3;
        xReal[30] = xReal[26] - tReal;
        xImag[30] = xImag[26] - tImag;
        xReal[26] += tReal;
        xImag[26] += tImag;
        
        tReal = xReal[31] * c3 + xImag[31] * s3;
        tImag = xImag[31] * c3 - xReal[31] * s3;
        xReal[31] = xReal[27] - tReal;
        xImag[31] = xImag[27] - tImag;
        xReal[27] += tReal;
        xImag[27] += tImag;

        // Stage 4: n2 = 2, nu1 = 1  
        // k=0, p=0 -> c=1, s=0
        tReal = xReal[2];
        tImag = xImag[2];
        xReal[2] = xReal[0] - tReal;
        xImag[2] = xImag[0] - tImag;
        xReal[0] += tReal;
        xImag[0] += tImag;
        
        tReal = xReal[3];
        tImag = xImag[3];
        xReal[3] = xReal[1] - tReal;
        xImag[3] = xImag[1] - tImag;
        xReal[1] += tReal;
        xImag[1] += tImag;
        
        // k=4, p=1 -> c=0, s=constant
        tReal = xImag[6] * constant;
        tImag = -xReal[6] * constant;
        xReal[6] = xReal[4] - tReal;
        xImag[6] = xImag[4] - tImag;
        xReal[4] += tReal;
        xImag[4] += tImag;
        
        tReal = xImag[7] * constant;
        tImag = -xReal[7] * constant;
        xReal[7] = xReal[5] - tReal;
        xImag[7] = xImag[5] - tImag;
        xReal[5] += tReal;
        xImag[5] += tImag;
        
        // Complete stage 4 remaining butterflies
        // k=16, p=0
        tReal = xReal[18];
        tImag = xImag[18];
        xReal[18] = xReal[16] - tReal;
        xImag[18] = xImag[16] - tImag;
        xReal[16] += tReal;
        xImag[16] += tImag;
        
        tReal = xReal[19];
        tImag = xImag[19];
        xReal[19] = xReal[17] - tReal;
        xImag[19] = xImag[17] - tImag;
        xReal[17] += tReal;
        xImag[17] += tImag;
        
        // k=20, p=1 -> c=0, s=constant
        tReal = xImag[22] * constant;
        tImag = -xReal[22] * constant;
        xReal[22] = xReal[20] - tReal;
        xImag[22] = xImag[20] - tImag;
        xReal[20] += tReal;
        xImag[20] += tImag;
        
        tReal = xImag[23] * constant;
        tImag = -xReal[23] * constant;
        xReal[23] = xReal[21] - tReal;
        xImag[23] = xImag[21] - tImag;
        xReal[21] += tReal;
        xImag[21] += tImag;
        
        // k=24, p=0
        tReal = xReal[26];
        tImag = xImag[26];
        xReal[26] = xReal[24] - tReal;
        xImag[26] = xImag[24] - tImag;
        xReal[24] += tReal;
        xImag[24] += tImag;
        
        tReal = xReal[27];
        tImag = xImag[27];
        xReal[27] = xReal[25] - tReal;
        xImag[27] = xImag[25] - tImag;
        xReal[25] += tReal;
        xImag[25] += tImag;
        
        // k=28, p=3 -> c=-√2/2, s=√2/2*constant
        double c3s4_2 = -sqrt2_2;
        double s3s4_2 = constant * sqrt2_2;
        tReal = xReal[30] * c3s4_2 + xImag[30] * s3s4_2;
        tImag = xImag[30] * c3s4_2 - xReal[30] * s3s4_2;
        xReal[30] = xReal[28] - tReal;
        xImag[30] = xImag[28] - tImag;
        xReal[28] += tReal;
        xImag[28] += tImag;
        
        tReal = xReal[31] * c3s4_2 + xImag[31] * s3s4_2;
        tImag = xImag[31] * c3s4_2 - xReal[31] * s3s4_2;
        xReal[31] = xReal[29] - tReal;
        xImag[31] = xImag[29] - tImag;
        xReal[29] += tReal;
        xImag[29] += tImag;
        
        // Stage 5: n2 = 1, nu1 = 0 (final stage) - completely unrolled
        // k=0, p=0 -> c=1, s=0
        tReal = xReal[1];
        tImag = xImag[1];
        xReal[1] = xReal[0] - tReal;
        xImag[1] = xImag[0] - tImag;
        xReal[0] += tReal;
        xImag[0] += tImag;
        
        // k=2, p=1 -> c=0, s=constant
        tReal = xImag[3] * constant;
        tImag = -xReal[3] * constant;
        xReal[3] = xReal[2] - tReal;
        xImag[3] = xImag[2] - tImag;
        xReal[2] += tReal;
        xImag[2] += tImag;
        
        // k=4, p=0
        tReal = xReal[5];
        tImag = xImag[5];
        xReal[5] = xReal[4] - tReal;
        xImag[5] = xImag[4] - tImag;
        xReal[4] += tReal;
        xImag[4] += tImag;
        
        // k=6, p=1
        tReal = xImag[7] * constant;
        tImag = -xReal[7] * constant;
        xReal[7] = xReal[6] - tReal;
        xImag[7] = xImag[6] - tImag;
        xReal[6] += tReal;
        xImag[6] += tImag;
        
        // k=8, p=0
        tReal = xReal[9];
        tImag = xImag[9];
        xReal[9] = xReal[8] - tReal;
        xImag[9] = xImag[8] - tImag;
        xReal[8] += tReal;
        xImag[8] += tImag;
        
        // k=10, p=1
        tReal = xImag[11] * constant;
        tImag = -xReal[11] * constant;
        xReal[11] = xReal[10] - tReal;
        xImag[11] = xImag[10] - tImag;
        xReal[10] += tReal;
        xImag[10] += tImag;
        
        // k=12, p=0
        tReal = xReal[13];
        tImag = xImag[13];
        xReal[13] = xReal[12] - tReal;
        xImag[13] = xImag[12] - tImag;
        xReal[12] += tReal;
        xImag[12] += tImag;
        
        // k=14, p=1
        tReal = xImag[15] * constant;
        tImag = -xReal[15] * constant;
        xReal[15] = xReal[14] - tReal;
        xImag[15] = xImag[14] - tImag;
        xReal[14] += tReal;
        xImag[14] += tImag;
        
        // k=16, p=0
        tReal = xReal[17];
        tImag = xImag[17];
        xReal[17] = xReal[16] - tReal;
        xImag[17] = xImag[16] - tImag;
        xReal[16] += tReal;
        xImag[16] += tImag;
        
        // k=18, p=1
        tReal = xImag[19] * constant;
        tImag = -xReal[19] * constant;
        xReal[19] = xReal[18] - tReal;
        xImag[19] = xImag[18] - tImag;
        xReal[18] += tReal;
        xImag[18] += tImag;
        
        // k=20, p=0
        tReal = xReal[21];
        tImag = xImag[21];
        xReal[21] = xReal[20] - tReal;
        xImag[21] = xImag[20] - tImag;
        xReal[20] += tReal;
        xImag[20] += tImag;
        
        // k=22, p=1
        tReal = xImag[23] * constant;
        tImag = -xReal[23] * constant;
        xReal[23] = xReal[22] - tReal;
        xImag[23] = xImag[22] - tImag;
        xReal[22] += tReal;
        xImag[22] += tImag;
        
        // k=24, p=0
        tReal = xReal[25];
        tImag = xImag[25];
        xReal[25] = xReal[24] - tReal;
        xImag[25] = xImag[24] - tImag;
        xReal[24] += tReal;
        xImag[24] += tImag;
        
        // k=26, p=1
        tReal = xImag[27] * constant;
        tImag = -xReal[27] * constant;
        xReal[27] = xReal[26] - tReal;
        xImag[27] = xImag[26] - tImag;
        xReal[26] += tReal;
        xImag[26] += tImag;
        
        // k=28, p=0
        tReal = xReal[29];
        tImag = xImag[29];
        xReal[29] = xReal[28] - tReal;
        xImag[29] = xImag[28] - tImag;
        xReal[28] += tReal;
        xImag[28] += tImag;
        
        // k=30, p=1
        tReal = xImag[31] * constant;
        tImag = -xReal[31] * constant;
        xReal[31] = xReal[30] - tReal;
        xImag[31] = xImag[30] - tImag;
        xReal[30] += tReal;
        xImag[30] += tImag;

        // Bit-reversal recombination (completely unrolled)
        // Swap 1 <-> 16
        tReal = xReal[1];
        tImag = xImag[1];
        xReal[1] = xReal[16];
        xImag[1] = xImag[16];
        xReal[16] = tReal;
        xImag[16] = tImag;
        
        // Swap 2 <-> 8
        tReal = xReal[2];
        tImag = xImag[2];
        xReal[2] = xReal[8];
        xImag[2] = xImag[8];
        xReal[8] = tReal;
        xImag[8] = tImag;
        
        // Swap 3 <-> 24
        tReal = xReal[3];
        tImag = xImag[3];
        xReal[3] = xReal[24];
        xImag[3] = xImag[24];
        xReal[24] = tReal;
        xImag[24] = tImag;
        
        // Swap 5 <-> 20
        tReal = xReal[5];
        tImag = xImag[5];
        xReal[5] = xReal[20];
        xImag[5] = xImag[20];
        xReal[20] = tReal;
        xImag[20] = tImag;
        
        // Swap 6 <-> 12
        tReal = xReal[6];
        tImag = xImag[6];
        xReal[6] = xReal[12];
        xImag[6] = xImag[12];
        xReal[12] = tReal;
        xImag[12] = tImag;
        
        // Swap 7 <-> 28
        tReal = xReal[7];
        tImag = xImag[7];
        xReal[7] = xReal[28];
        xImag[7] = xImag[28];
        xReal[28] = tReal;
        xImag[28] = tImag;
        
        // Swap 9 <-> 18
        tReal = xReal[9];
        tImag = xImag[9];
        xReal[9] = xReal[18];
        xImag[9] = xImag[18];
        xReal[18] = tReal;
        xImag[18] = tImag;
        
        // Swap 11 <-> 26
        tReal = xReal[11];
        tImag = xImag[11];
        xReal[11] = xReal[26];
        xImag[11] = xImag[26];
        xReal[26] = tReal;
        xImag[26] = tImag;
        
        // Swap 13 <-> 22
        tReal = xReal[13];
        tImag = xImag[13];
        xReal[13] = xReal[22];
        xImag[13] = xImag[22];
        xReal[22] = tReal;
        xImag[22] = tImag;
        
        // Swap 15 <-> 30
        tReal = xReal[15];
        tImag = xImag[15];
        xReal[15] = xReal[30];
        xImag[15] = xImag[30];
        xReal[30] = tReal;
        xImag[30] = tImag;
        
        // Swap 19 <-> 25
        tReal = xReal[19];
        tImag = xImag[19];
        xReal[19] = xReal[25];
        xImag[19] = xImag[25];
        xReal[25] = tReal;
        xImag[25] = tImag;
        
        // Swap 23 <-> 29
        tReal = xReal[23];
        tImag = xImag[23];
        xReal[23] = xReal[29];
        xImag[23] = xImag[29];
        xReal[29] = tReal;
        xImag[29] = tImag;

        // Combine real and imaginary parts into output array with normalization
        double[] newArray = new double[64];
        double radice = 1.0 / Math.sqrt(32);
        for (int i = 0; i < 64; i += 2) {
            int i2 = i / 2;
            newArray[i] = xReal[i2] * radice;
            newArray[i + 1] = xImag[i2] * radice;
        }
        return newArray;
    }

    /**
     * The reference bit reverse function.
     */
    private static int bitreverseReference(int j) {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= 5; i++) {
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
        }
        return k;
    }
}
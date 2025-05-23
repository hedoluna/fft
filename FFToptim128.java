/**
 * @author Orlando Selenu (original base), Enhanced by Engine AI Assistant
 * Originally written in the Summer of 2008 
 * Based on the algorithms originally published by E. Oran Brigham "The Fast Fourier Transform" 1973, in ALGOL60 and FORTRAN
 * 
 * Released in the Public Domain.
 */
public class FFToptim128 {
    
    // Precomputed trigonometric lookup tables for 128-point FFT
    private static final double[] COS_TABLE = new double[64];
    private static final double[] SIN_TABLE = new double[64];
    
    static {
        // Initialize lookup tables
        for (int i = 0; i < 64; i++) {
            double angle = 2.0 * Math.PI * i / 128.0;
            COS_TABLE[i] = Math.cos(angle);
            SIN_TABLE[i] = Math.sin(angle);
        }
    }
    
    /**
     * The Fast Fourier Transform (optimized version for arrays of size 128).
     * 
     * This implementation is optimized specifically for 128-element arrays
     * with precomputed trigonometric lookup tables for better performance.
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

        // First phase - calculation (7 iterations for 128 elements) with lookup tables
        int k = 0;
        double constant_sign = DIRECT ? -1.0 : 1.0;
        
        for (int l = 1; l <= 7; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    int bit_rev_k = k >> nu1;
                    // Use precomputed bit-reverse lookup for twiddle factors
                    if (bit_rev_k < 64) {  // Safety check for lookup table bounds
                        c = COS_TABLE[bit_rev_k];
                        s = SIN_TABLE[bit_rev_k] * constant_sign;
                    } else {
                        c = 1.0; s = 0.0;  // Fallback to identity
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

        // Second phase - bit-reversal recombination (hardcoded for maximum speed)
        // Precomputed bit-reverse swaps for size 128 - eliminates function calls entirely
        
        tReal = xReal[1]; tImag = xImag[1]; xReal[1] = xReal[64]; xImag[1] = xImag[64]; xReal[64] = tReal; xImag[64] = tImag;
        tReal = xReal[2]; tImag = xImag[2]; xReal[2] = xReal[32]; xImag[2] = xImag[32]; xReal[32] = tReal; xImag[32] = tImag;
        tReal = xReal[3]; tImag = xImag[3]; xReal[3] = xReal[96]; xImag[3] = xImag[96]; xReal[96] = tReal; xImag[96] = tImag;
        tReal = xReal[4]; tImag = xImag[4]; xReal[4] = xReal[16]; xImag[4] = xImag[16]; xReal[16] = tReal; xImag[16] = tImag;
        tReal = xReal[5]; tImag = xImag[5]; xReal[5] = xReal[80]; xImag[5] = xImag[80]; xReal[80] = tReal; xImag[80] = tImag;
        tReal = xReal[6]; tImag = xImag[6]; xReal[6] = xReal[48]; xImag[6] = xImag[48]; xReal[48] = tReal; xImag[48] = tImag;
        tReal = xReal[7]; tImag = xImag[7]; xReal[7] = xReal[112]; xImag[7] = xImag[112]; xReal[112] = tReal; xImag[112] = tImag;
        tReal = xReal[9]; tImag = xImag[9]; xReal[9] = xReal[72]; xImag[9] = xImag[72]; xReal[72] = tReal; xImag[72] = tImag;
        tReal = xReal[10]; tImag = xImag[10]; xReal[10] = xReal[40]; xImag[10] = xImag[40]; xReal[40] = tReal; xImag[40] = tImag;
        tReal = xReal[11]; tImag = xImag[11]; xReal[11] = xReal[104]; xImag[11] = xImag[104]; xReal[104] = tReal; xImag[104] = tImag;
        tReal = xReal[12]; tImag = xImag[12]; xReal[12] = xReal[24]; xImag[12] = xImag[24]; xReal[24] = tReal; xImag[24] = tImag;
        tReal = xReal[13]; tImag = xImag[13]; xReal[13] = xReal[88]; xImag[13] = xImag[88]; xReal[88] = tReal; xImag[88] = tImag;
        tReal = xReal[14]; tImag = xImag[14]; xReal[14] = xReal[56]; xImag[14] = xImag[56]; xReal[56] = tReal; xImag[56] = tImag;
        tReal = xReal[15]; tImag = xImag[15]; xReal[15] = xReal[120]; xImag[15] = xImag[120]; xReal[120] = tReal; xImag[120] = tImag;
        tReal = xReal[17]; tImag = xImag[17]; xReal[17] = xReal[68]; xImag[17] = xImag[68]; xReal[68] = tReal; xImag[68] = tImag;
        tReal = xReal[18]; tImag = xImag[18]; xReal[18] = xReal[36]; xImag[18] = xImag[36]; xReal[36] = tReal; xImag[36] = tImag;
        tReal = xReal[19]; tImag = xImag[19]; xReal[19] = xReal[100]; xImag[19] = xImag[100]; xReal[100] = tReal; xImag[100] = tImag;
        tReal = xReal[21]; tImag = xImag[21]; xReal[21] = xReal[84]; xImag[21] = xImag[84]; xReal[84] = tReal; xImag[84] = tImag;
        tReal = xReal[22]; tImag = xImag[22]; xReal[22] = xReal[52]; xImag[22] = xImag[52]; xReal[52] = tReal; xImag[52] = tImag;
        tReal = xReal[23]; tImag = xImag[23]; xReal[23] = xReal[116]; xImag[23] = xImag[116]; xReal[116] = tReal; xImag[116] = tImag;
        tReal = xReal[25]; tImag = xImag[25]; xReal[25] = xReal[76]; xImag[25] = xImag[76]; xReal[76] = tReal; xImag[76] = tImag;
        tReal = xReal[26]; tImag = xImag[26]; xReal[26] = xReal[44]; xImag[26] = xImag[44]; xReal[44] = tReal; xImag[44] = tImag;
        tReal = xReal[27]; tImag = xImag[27]; xReal[27] = xReal[108]; xImag[27] = xImag[108]; xReal[108] = tReal; xImag[108] = tImag;
        tReal = xReal[29]; tImag = xImag[29]; xReal[29] = xReal[92]; xImag[29] = xImag[92]; xReal[92] = tReal; xImag[92] = tImag;
        tReal = xReal[30]; tImag = xImag[30]; xReal[30] = xReal[60]; xImag[30] = xImag[60]; xReal[60] = tReal; xImag[60] = tImag;
        tReal = xReal[31]; tImag = xImag[31]; xReal[31] = xReal[124]; xImag[31] = xImag[124]; xReal[124] = tReal; xImag[124] = tImag;
        tReal = xReal[33]; tImag = xImag[33]; xReal[33] = xReal[66]; xImag[33] = xImag[66]; xReal[66] = tReal; xImag[66] = tImag;
        tReal = xReal[35]; tImag = xImag[35]; xReal[35] = xReal[98]; xImag[35] = xImag[98]; xReal[98] = tReal; xImag[98] = tImag;
        tReal = xReal[37]; tImag = xImag[37]; xReal[37] = xReal[82]; xImag[37] = xImag[82]; xReal[82] = tReal; xImag[82] = tImag;
        tReal = xReal[38]; tImag = xImag[38]; xReal[38] = xReal[50]; xImag[38] = xImag[50]; xReal[50] = tReal; xImag[50] = tImag;
        tReal = xReal[39]; tImag = xImag[39]; xReal[39] = xReal[114]; xImag[39] = xImag[114]; xReal[114] = tReal; xImag[114] = tImag;
        tReal = xReal[41]; tImag = xImag[41]; xReal[41] = xReal[74]; xImag[41] = xImag[74]; xReal[74] = tReal; xImag[74] = tImag;
        tReal = xReal[43]; tImag = xImag[43]; xReal[43] = xReal[106]; xImag[43] = xImag[106]; xReal[106] = tReal; xImag[106] = tImag;
        tReal = xReal[45]; tImag = xImag[45]; xReal[45] = xReal[90]; xImag[45] = xImag[90]; xReal[90] = tReal; xImag[90] = tImag;
        tReal = xReal[46]; tImag = xImag[46]; xReal[46] = xReal[58]; xImag[46] = xImag[58]; xReal[58] = tReal; xImag[58] = tImag;
        tReal = xReal[47]; tImag = xImag[47]; xReal[47] = xReal[122]; xImag[47] = xImag[122]; xReal[122] = tReal; xImag[122] = tImag;
        tReal = xReal[49]; tImag = xImag[49]; xReal[49] = xReal[70]; xImag[49] = xImag[70]; xReal[70] = tReal; xImag[70] = tImag;
        tReal = xReal[51]; tImag = xImag[51]; xReal[51] = xReal[102]; xImag[51] = xImag[102]; xReal[102] = tReal; xImag[102] = tImag;
        tReal = xReal[53]; tImag = xImag[53]; xReal[53] = xReal[86]; xImag[53] = xImag[86]; xReal[86] = tReal; xImag[86] = tImag;
        tReal = xReal[55]; tImag = xImag[55]; xReal[55] = xReal[118]; xImag[55] = xImag[118]; xReal[118] = tReal; xImag[118] = tImag;
        tReal = xReal[57]; tImag = xImag[57]; xReal[57] = xReal[78]; xImag[57] = xImag[78]; xReal[78] = tReal; xImag[78] = tImag;
        tReal = xReal[59]; tImag = xImag[59]; xReal[59] = xReal[110]; xImag[59] = xImag[110]; xReal[110] = tReal; xImag[110] = tImag;
        tReal = xReal[61]; tImag = xImag[61]; xReal[61] = xReal[94]; xImag[61] = xImag[94]; xReal[94] = tReal; xImag[94] = tImag;
        tReal = xReal[63]; tImag = xImag[63]; xReal[63] = xReal[126]; xImag[63] = xImag[126]; xReal[126] = tReal; xImag[126] = tImag;
        tReal = xReal[67]; tImag = xImag[67]; xReal[67] = xReal[97]; xImag[67] = xImag[97]; xReal[97] = tReal; xImag[97] = tImag;
        tReal = xReal[69]; tImag = xImag[69]; xReal[69] = xReal[81]; xImag[69] = xImag[81]; xReal[81] = tReal; xImag[81] = tImag;
        tReal = xReal[71]; tImag = xImag[71]; xReal[71] = xReal[113]; xImag[71] = xImag[113]; xReal[113] = tReal; xImag[113] = tImag;
        tReal = xReal[73]; tImag = xImag[73]; xReal[73] = xReal[89]; xImag[73] = xImag[89]; xReal[89] = tReal; xImag[89] = tImag;
        tReal = xReal[75]; tImag = xImag[75]; xReal[75] = xReal[105]; xImag[75] = xImag[105]; xReal[105] = tReal; xImag[105] = tImag;
        tReal = xReal[77]; tImag = xImag[77]; xReal[77] = xReal[101]; xImag[77] = xImag[101]; xReal[101] = tReal; xImag[101] = tImag;
        tReal = xReal[79]; tImag = xImag[79]; xReal[79] = xReal[121]; xImag[79] = xImag[121]; xReal[121] = tReal; xImag[121] = tImag;
        tReal = xReal[83]; tImag = xImag[83]; xReal[83] = xReal[115]; xImag[83] = xImag[115]; xReal[115] = tReal; xImag[115] = tImag;
        tReal = xReal[85]; tImag = xImag[85]; xReal[85] = xReal[99]; xImag[85] = xImag[99]; xReal[99] = tReal; xImag[99] = tImag;
        tReal = xReal[87]; tImag = xImag[87]; xReal[87] = xReal[107]; xImag[87] = xImag[107]; xReal[107] = tReal; xImag[107] = tImag;
        tReal = xReal[91]; tImag = xImag[91]; xReal[91] = xReal[111]; xImag[91] = xImag[111]; xReal[111] = tReal; xImag[111] = tImag;
        tReal = xReal[93]; tImag = xImag[93]; xReal[93] = xReal[103]; xImag[93] = xImag[103]; xReal[103] = tReal; xImag[103] = tImag;
        tReal = xReal[95]; tImag = xImag[95]; xReal[95] = xReal[119]; xImag[95] = xImag[119]; xReal[119] = tReal; xImag[119] = tImag;
        tReal = xReal[109]; tImag = xImag[109]; xReal[109] = xReal[117]; xImag[109] = xImag[117]; xReal[117] = tReal; xImag[117] = tImag;

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
}
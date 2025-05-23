/**
 * Generator for compact optimized FFT implementations.
 * Creates optimized FFT classes with hardcoded bit-reversal but manageable code size.
 * 
 * @author Engine AI Assistant
 */
public class GenerateCompactOptimizedFFTs {
    
    public static void main(String[] args) {
        // Generate compact optimized implementations
        generateCompactOptimizedFFT(1024);
        generateCompactOptimizedFFT(2048);
        generateCompactOptimizedFFT(4096);
        generateCompactOptimizedFFT(8192);
    }
    
    /**
     * Generates a compact optimized FFT implementation for the specified size.
     */
    private static void generateCompactOptimizedFFT(int size) {
        int nu = (int)(Math.log(size) / Math.log(2));
        String className = "FFToptim" + size;
        
        System.out.println("Generating compact " + className + ".java for size " + size + " (nu=" + nu + ")");
        
        StringBuilder code = new StringBuilder();
        
        // Class header and documentation
        code.append("/**\n");
        code.append(" * Fast Fourier Transform - Optimized implementation for arrays of size " + size + ".\n");
        code.append(" * \n");
        code.append(" * This class provides a highly optimized FFT implementation specifically designed\n");
        code.append(" * for " + size + "-element arrays using hardcoded bit-reversal lookup tables.\n");
        code.append(" * \n");
        code.append(" * @author Orlando Selenu (original base algorithm, 2008)\n");
        code.append(" * @author Engine AI Assistant (optimized implementation, 2025)\n");
        code.append(" * @since 1.0\n");
        code.append(" */\n");
        code.append("public class " + className + " {\n\n");
        
        // Trigonometric lookup tables
        int tableSize = size / 2;
        code.append("    // Precomputed trigonometric lookup tables for " + size + "-point FFT\n");
        code.append("    private static final double[] COS_TABLE = new double[" + tableSize + "];\n");
        code.append("    private static final double[] SIN_TABLE = new double[" + tableSize + "];\n\n");
        
        // Bit-reversal lookup table
        int[] bitReversalPairs = generateBitReversalPairs(size, nu);
        code.append("    // Hardcoded bit-reversal swap pairs for optimal performance\n");
        code.append("    private static final int[][] BIT_REVERSAL_PAIRS = {\n");
        for (int i = 0; i < bitReversalPairs.length; i += 2) {
            code.append("        {" + bitReversalPairs[i] + ", " + bitReversalPairs[i + 1] + "}");
            if (i + 2 < bitReversalPairs.length) {
                code.append(",");
            }
            code.append("\n");
        }
        code.append("    };\n\n");
        
        // Static initializer for lookup tables
        code.append("    static {\n");
        code.append("        // Initialize trigonometric lookup tables\n");
        code.append("        for (int i = 0; i < " + tableSize + "; i++) {\n");
        code.append("            double angle = 2.0 * Math.PI * i / " + size + ".0;\n");
        code.append("            COS_TABLE[i] = Math.cos(angle);\n");
        code.append("            SIN_TABLE[i] = Math.sin(angle);\n");
        code.append("        }\n");
        code.append("    }\n\n");
        
        // Main FFT method
        code.append("    /**\n");
        code.append("     * Performs Fast Fourier Transform optimized for " + size + "-element arrays.\n");
        code.append("     * \n");
        code.append("     * @param inputReal array of exactly " + size + " real values\n");
        code.append("     * @param inputImag array of exactly " + size + " imaginary values\n");
        code.append("     * @param DIRECT true for forward transform, false for inverse\n");
        code.append("     * @return array of length " + (size * 2) + " with interleaved real and imaginary results\n");
        code.append("     */\n");
        code.append("    public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {\n");
        code.append("        // Validate input size\n");
        code.append("        if (inputReal.length != " + size + " || inputImag.length != " + size + ") {\n");
        code.append("            System.out.println(\"ERROR: Input arrays must be exactly size " + size + "\");\n");
        code.append("            return new double[0];\n");
        code.append("        }\n\n");
        
        // Hardcoded parameters
        code.append("        // Hardcoded parameters for size " + size + "\n");
        code.append("        final int n = " + size + ";\n");
        code.append("        final int nu = " + nu + ";\n");
        code.append("        int n2 = " + (size / 2) + ";\n");
        code.append("        int nu1 = " + (nu - 1) + ";\n");
        code.append("        double[] xReal = new double[n];\n");
        code.append("        double[] xImag = new double[n];\n");
        code.append("        double tReal, tImag;\n\n");
        
        // Copy input arrays
        code.append("        // Copy input arrays\n");
        code.append("        System.arraycopy(inputReal, 0, xReal, 0, n);\n");
        code.append("        System.arraycopy(inputImag, 0, xImag, 0, n);\n\n");
        
        // Butterfly computation stages - compact version
        code.append("        // FFT butterfly computation stages\n");
        code.append("        int k = 0;\n");
        code.append("        for (int l = 1; l <= nu; l++) {\n");
        code.append("            while (k < n) {\n");
        code.append("                for (int i = 1; i <= n2; i++) {\n");
        code.append("                    int p_index = k >> nu1;\n");
        code.append("                    double c, s;\n");
        code.append("                    if (p_index < " + tableSize + ") {\n");
        code.append("                        c = DIRECT ? COS_TABLE[p_index] : COS_TABLE[p_index];\n");
        code.append("                        s = DIRECT ? -SIN_TABLE[p_index] : SIN_TABLE[p_index];\n");
        code.append("                    } else {\n");
        code.append("                        c = 1.0; s = 0.0;\n");
        code.append("                    }\n");
        code.append("                    tReal = xReal[k + n2] * c + xImag[k + n2] * s;\n");
        code.append("                    tImag = xImag[k + n2] * c - xReal[k + n2] * s;\n");
        code.append("                    xReal[k + n2] = xReal[k] - tReal;\n");
        code.append("                    xImag[k + n2] = xImag[k] - tImag;\n");
        code.append("                    xReal[k] += tReal;\n");
        code.append("                    xImag[k] += tImag;\n");
        code.append("                    k++;\n");
        code.append("                }\n");
        code.append("                k += n2;\n");
        code.append("            }\n");
        code.append("            k = 0; nu1--; n2 /= 2;\n");
        code.append("        }\n\n");
        
        // Bit-reversal reordering using lookup table
        code.append("        // Final bit-reversal reordering using hardcoded lookup table\n");
        code.append("        for (int[] pair : BIT_REVERSAL_PAIRS) {\n");
        code.append("            int i = pair[0], j = pair[1];\n");
        code.append("            tReal = xReal[i]; tImag = xImag[i];\n");
        code.append("            xReal[i] = xReal[j]; xImag[i] = xImag[j];\n");
        code.append("            xReal[j] = tReal; xImag[j] = tImag;\n");
        code.append("        }\n\n");
        
        // Output generation
        code.append("        // Generate interleaved output with normalization\n");
        code.append("        double[] result = new double[" + (size * 2) + "];\n");
        code.append("        double radice = 1.0 / Math.sqrt(n);\n");
        code.append("        for (int i = 0; i < n; i++) {\n");
        code.append("            result[2 * i] = xReal[i] * radice;\n");
        code.append("            result[2 * i + 1] = xImag[i] * radice;\n");
        code.append("        }\n");
        code.append("        return result;\n");
        code.append("    }\n");
        code.append("}\n");
        
        // Write to file
        try {
            java.io.FileWriter writer = new java.io.FileWriter(className + ".java");
            writer.write(code.toString());
            writer.close();
            System.out.println("Generated compact " + className + ".java successfully");
        } catch (java.io.IOException e) {
            System.err.println("Error writing " + className + ".java: " + e.getMessage());
        }
    }
    
    /**
     * Generates bit-reversal swap pairs (only those that need swapping).
     */
    private static int[] generateBitReversalPairs(int size, int nu) {
        java.util.List<Integer> pairs = new java.util.ArrayList<>();
        for (int i = 0; i < size; i++) {
            int r = bitreverseReference(i, nu);
            if (r > i) {  // Only generate swaps in one direction to avoid double-swapping
                pairs.add(i);
                pairs.add(r);
            }
        }
        return pairs.stream().mapToInt(Integer::intValue).toArray();
    }
    
    /**
     * Reference bit-reverse function.
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
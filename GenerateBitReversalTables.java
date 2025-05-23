public class GenerateBitReversalTables {
    public static void main(String[] args) {
        generateBitReversalTable(64, 6);
        generateBitReversalTable(128, 7);
        generateBitReversalTable(256, 8);
        generateBitReversalTable(512, 9);
    }
    
    private static void generateBitReversalTable(int size, int bits) {
        System.out.println("// Bit-reversal lookup table for size " + size);
        System.out.print("private static final int[] BIT_REVERSAL_TABLE = {");
        
        for (int i = 0; i < size; i++) {
            if (i % 16 == 0) System.out.print("\n    ");
            int reversed = bitreverseReference(i, bits);
            System.out.print(reversed);
            if (i < size - 1) System.out.print(", ");
            if ((i + 1) % 16 == 0 && i < size - 1) System.out.print(" // " + (i - 15) + "-" + i);
        }
        System.out.println("\n};");
        System.out.println();
        
        // Generate pairs that need swapping
        System.out.println("// Bit-reversal swaps for size " + size);
        for (int i = 0; i < size; i++) {
            int r = bitreverseReference(i, bits);
            if (r > i) {
                System.out.println("// Swap " + i + " <-> " + r);
            }
        }
        System.out.println();
    }
    
    private static int bitreverseReference(int j, int bits) {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= bits; i++) {
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
        }
        return k;
    }
}

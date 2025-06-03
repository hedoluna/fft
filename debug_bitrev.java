public class debug_bitrev {
    private static int reverseBits5(int x) {
        int result = 0;
        result |= ((x & 0x01) << 4);  // Bit 0 -> Bit 4
        result |= ((x & 0x02) << 2);  // Bit 1 -> Bit 3
        result |= ((x & 0x04) << 0);  // Bit 2 -> Bit 2 (stays)
        result |= ((x & 0x08) >> 2);  // Bit 3 -> Bit 1
        result |= ((x & 0x10) >> 4);  // Bit 4 -> Bit 0
        return result;
    }
    
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
    
    public static void main(String[] args) {
        System.out.println("Comparing bit reversal functions for 5-bit (32-point):");
        System.out.println("Index\tMine\tReference\tMatch");
        
        boolean allMatch = true;
        for (int i = 0; i < 32; i++) {
            int mine = reverseBits5(i);
            int ref = bitreverseReference(i, 5);
            boolean match = (mine == ref);
            if (!match) allMatch = false;
            
            System.out.printf("%2d\t%2d\t%2d\t\t%s\n", i, mine, ref, match ? "YES" : "NO");
        }
        
        System.out.println("\nAll match: " + allMatch);
    }
}
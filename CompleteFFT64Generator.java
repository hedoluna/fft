public class CompleteFFT64Generator {
    public static void main(String[] args) {
        // Generate all 6 stages for 64-point FFT
        System.out.println("// COMPLETE FFT64 STAGES:");
        
        // Stage patterns for 64-point FFT (6 stages total)
        // Stage 1: n2=32, distance=32, 1 group
        // Stage 2: n2=16, distance=16, 2 groups  
        // Stage 3: n2=8,  distance=8,  4 groups
        // Stage 4: n2=4,  distance=4,  8 groups
        // Stage 5: n2=2,  distance=2,  16 groups
        // Stage 6: n2=1,  distance=1,  32 groups
        
        generateStage3();
        generateStage4();
        generateStage5();
        generateStage6();
    }
    
    static void generateStage3() {
        System.out.println("\n// STAGE 3: n2 = 8, distance = 8, 4 groups");
        
        // 4 groups with different twiddle factors
        for (int group = 0; group < 4; group++) {
            int baseK = group * 16;
            int twiddleIndex = group; // 0, 1, 2, 3
            
            System.out.println("// Group " + group + " (k=" + baseK + "-" + (baseK + 7) + "): twiddle factor exp(-i" + twiddleIndex + "π/16)");
            
            if (twiddleIndex == 0) {
                System.out.println("// c=1, s=0");
                for (int i = 0; i < 8; i++) {
                    int k = baseK + i;
                    System.out.println("tReal = xReal[" + (k + 8) + "]; tImag = xImag[" + (k + 8) + "]; xReal[" + (k + 8) + "] = xReal[" + k + "] - tReal; xImag[" + (k + 8) + "] = xImag[" + k + "] - tImag; xReal[" + k + "] += tReal; xImag[" + k + "] += tImag;");
                }
            } else {
                double angle = twiddleIndex * Math.PI / 16.0;
                double c = Math.cos(angle);
                double s = Math.sin(angle);
                System.out.println("// c=" + c + ", s=" + s);
                System.out.println("double c3_" + twiddleIndex + " = " + c + ";");
                System.out.println("double s3_" + twiddleIndex + " = constant * " + s + ";");
                
                for (int i = 0; i < 8; i++) {
                    int k = baseK + i;
                    System.out.println("tReal = xReal[" + (k + 8) + "] * c3_" + twiddleIndex + " + xImag[" + (k + 8) + "] * s3_" + twiddleIndex + "; tImag = xImag[" + (k + 8) + "] * c3_" + twiddleIndex + " - xReal[" + (k + 8) + "] * s3_" + twiddleIndex + "; xReal[" + (k + 8) + "] = xReal[" + k + "] - tReal; xImag[" + (k + 8) + "] = xImag[" + k + "] - tImag; xReal[" + k + "] += tReal; xImag[" + k + "] += tImag;");
                }
            }
            System.out.println();
        }
    }
    
    static void generateStage4() {
        System.out.println("\n// STAGE 4: n2 = 4, distance = 4, 8 groups");
        
        for (int group = 0; group < 8; group++) {
            int baseK = group * 8;
            int twiddleIndex = group % 4; // 0, 1, 2, 3, 0, 1, 2, 3
            
            System.out.println("// Group " + group + " (k=" + baseK + "-" + (baseK + 3) + "): twiddle factor exp(-i" + twiddleIndex + "π/8)");
            
            if (twiddleIndex == 0) {
                System.out.println("// c=1, s=0");
                for (int i = 0; i < 4; i++) {
                    int k = baseK + i;
                    System.out.println("tReal = xReal[" + (k + 4) + "]; tImag = xImag[" + (k + 4) + "]; xReal[" + (k + 4) + "] = xReal[" + k + "] - tReal; xImag[" + (k + 4) + "] = xImag[" + k + "] - tImag; xReal[" + k + "] += tReal; xImag[" + k + "] += tImag;");
                }
            } else {
                double angle = twiddleIndex * Math.PI / 8.0;
                double c = Math.cos(angle);
                double s = Math.sin(angle);
                System.out.println("// c=" + c + ", s=" + s);
                System.out.println("double c4_" + twiddleIndex + " = " + c + ";");
                System.out.println("double s4_" + twiddleIndex + " = constant * " + s + ";");
                
                for (int i = 0; i < 4; i++) {
                    int k = baseK + i;
                    System.out.println("tReal = xReal[" + (k + 4) + "] * c4_" + twiddleIndex + " + xImag[" + (k + 4) + "] * s4_" + twiddleIndex + "; tImag = xImag[" + (k + 4) + "] * c4_" + twiddleIndex + " - xReal[" + (k + 4) + "] * s4_" + twiddleIndex + "; xReal[" + (k + 4) + "] = xReal[" + k + "] - tReal; xImag[" + (k + 4) + "] = xImag[" + k + "] - tImag; xReal[" + k + "] += tReal; xImag[" + k + "] += tImag;");
                }
            }
            System.out.println();
        }
    }
    
    static void generateStage5() {
        System.out.println("\n// STAGE 5: n2 = 2, distance = 2, 16 groups");
        
        for (int group = 0; group < 16; group++) {
            int baseK = group * 4;
            int twiddleIndex = group % 2; // alternating 0, 1
            
            System.out.println("// Group " + group + " (k=" + baseK + "-" + (baseK + 1) + "): twiddle factor exp(-i" + twiddleIndex + "π/4)");
            
            if (twiddleIndex == 0) {
                System.out.println("// c=1, s=0");
                for (int i = 0; i < 2; i++) {
                    int k = baseK + i;
                    System.out.println("tReal = xReal[" + (k + 2) + "]; tImag = xImag[" + (k + 2) + "]; xReal[" + (k + 2) + "] = xReal[" + k + "] - tReal; xImag[" + (k + 2) + "] = xImag[" + k + "] - tImag; xReal[" + k + "] += tReal; xImag[" + k + "] += tImag;");
                }
            } else {
                System.out.println("// c=0, s=1 -> multiply by constant*i");
                for (int i = 0; i < 2; i++) {
                    int k = baseK + i;
                    System.out.println("tReal = xImag[" + (k + 2) + "] * constant; tImag = -xReal[" + (k + 2) + "] * constant; xReal[" + (k + 2) + "] = xReal[" + k + "] - tReal; xImag[" + (k + 2) + "] = xImag[" + k + "] - tImag; xReal[" + k + "] += tReal; xImag[" + k + "] += tImag;");
                }
            }
            System.out.println();
        }
    }
    
    static void generateStage6() {
        System.out.println("\n// STAGE 6: n2 = 1, distance = 1, 32 groups");
        
        for (int group = 0; group < 32; group++) {
            int k = group * 2;
            int twiddleIndex = group % 2; // alternating 0, 1
            
            System.out.println("// Group " + group + " (k=" + k + "): twiddle factor exp(-i" + twiddleIndex + "π/2)");
            
            if (twiddleIndex == 0) {
                System.out.println("// c=1, s=0");
                System.out.println("tReal = xReal[" + (k + 1) + "]; tImag = xImag[" + (k + 1) + "]; xReal[" + (k + 1) + "] = xReal[" + k + "] - tReal; xImag[" + (k + 1) + "] = xImag[" + k + "] - tImag; xReal[" + k + "] += tReal; xImag[" + k + "] += tImag;");
            } else {
                System.out.println("// c=0, s=1 -> multiply by constant*i");
                System.out.println("tReal = xImag[" + (k + 1) + "] * constant; tImag = -xReal[" + (k + 1) + "] * constant; xReal[" + (k + 1) + "] = xReal[" + k + "] - tReal; xImag[" + (k + 1) + "] = xImag[" + k + "] - tImag; xReal[" + k + "] += tReal; xImag[" + k + "] += tImag;");
            }
        }
    }
}

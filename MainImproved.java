import static java.lang.String.format;

/**
 * Enhanced main class that demonstrates all FFT implementations and provides
 * proper performance comparisons and correctness validation.
 * 
 * @author Orlando Selenu (original), Enhanced by Engine AI Assistant
 */
class MainImproved {
    
    public static void main(String[] args) {
        System.out.println("Enhanced FFT Demonstration");
        System.out.println("==========================");
        System.out.println();
        
        testCorrectnessAndPerformance();
        System.out.println("\nDemonstration completed!");
    }
    
    /**
     * Test correctness and performance for all available FFT implementations
     */
    private static void testCorrectnessAndPerformance() {
        // Test size 8
        System.out.println("Testing Size 8 FFT:");
        System.out.println("------------------");
        double[] inputReal8 = {1, 22, 33, 44, 15, 16, 17, 18};
        double[] inputImag8 = {0, 0, 0, 0, 0, 0, 0, 0};
        compareImplementations("Size 8", inputReal8, inputImag8, 
            () -> FFTbase.fft(inputReal8, inputImag8, true),
            () -> FFToptim8.fft(inputReal8, inputImag8, true));
        
        // Test size 32
        System.out.println("\nTesting Size 32 FFT:");
        System.out.println("-------------------");
        double[] inputReal32 = {
            1, 22, 33, 44, 15, 16, 17, 18, 1, 22, 33, 44, 15, 16, 17, 18,
            1, 22, 33, 44, 15, 16, 17, 18, 1, 22, 33, 44, 15, 16, 17, 18
        };
        double[] inputImag32 = new double[32];
        compareImplementations("Size 32", inputReal32, inputImag32,
            () -> FFTbase.fft(inputReal32, inputImag32, true),
            () -> FFToptim32.fft(inputReal32, inputImag32, true));
        
        // Test size 64
        System.out.println("\nTesting Size 64 FFT:");
        System.out.println("-------------------");
        double[] inputReal64 = generateTestData(64);
        double[] inputImag64 = new double[64];
        compareImplementations("Size 64", inputReal64, inputImag64,
            () -> FFTbase.fft(inputReal64, inputImag64, true),
            () -> FFToptim64.fft(inputReal64, inputImag64, true));
        
        // Test size 128
        System.out.println("\nTesting Size 128 FFT:");
        System.out.println("--------------------");
        double[] inputReal128 = generateTestData(128);
        double[] inputImag128 = new double[128];
        compareImplementations("Size 128", inputReal128, inputImag128,
            () -> FFTbase.fft(inputReal128, inputImag128, true),
            () -> FFToptim128.fft(inputReal128, inputImag128, true));
        
        // Test size 256
        System.out.println("\nTesting Size 256 FFT:");
        System.out.println("--------------------");
        double[] inputReal256 = generateTestData(256);
        double[] inputImag256 = new double[256];
        compareImplementations("Size 256", inputReal256, inputImag256,
            () -> FFTbase.fft(inputReal256, inputImag256, true),
            () -> FFToptim256.fft(inputReal256, inputImag256, true));
        
        // Test size 512
        System.out.println("\nTesting Size 512 FFT:");
        System.out.println("--------------------");
        double[] inputReal512 = generateTestData(512);
        double[] inputImag512 = new double[512];
        compareImplementations("Size 512", inputReal512, inputImag512,
            () -> FFTbase.fft(inputReal512, inputImag512, true),
            () -> FFToptim512.fft(inputReal512, inputImag512, true));
    }
    
    /**
     * Compare two FFT implementations for correctness and performance
     */
    private static void compareImplementations(String sizeName, double[] inputReal, double[] inputImag,
                                             FFTSupplier baseImpl, FFTSupplier optimImpl) {
        final int PERFORMANCE_ITERATIONS = 1000;
        
        // Get results from both implementations
        double[] baseResult = baseImpl.get();
        double[] optimResult = optimImpl.get();
        
        // Check correctness
        boolean isCorrect = true;
        double maxDifference = 0.0;
        final double EPSILON = 1e-10;
        
        if (baseResult.length != optimResult.length) {
            isCorrect = false;
            System.out.printf("❌ %s: Different result lengths! Base: %d, Optimized: %d%n", 
                sizeName, baseResult.length, optimResult.length);
        } else {
            for (int i = 0; i < baseResult.length; i++) {
                double diff = Math.abs(baseResult[i] - optimResult[i]);
                maxDifference = Math.max(maxDifference, diff);
                if (diff > EPSILON) {
                    isCorrect = false;
                }
            }
        }
        
        if (isCorrect) {
            System.out.printf("✅ %s: Correctness check PASSED (max difference: %.2e)%n", sizeName, maxDifference);
        } else {
            System.out.printf("❌ %s: Correctness check FAILED (max difference: %.2e)%n", sizeName, maxDifference);
        }
        
        // Performance comparison
        // Warmup
        for (int i = 0; i < 100; i++) {
            baseImpl.get();
            optimImpl.get();
        }
        
        // Measure base implementation
        long startTime = System.nanoTime();
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            baseImpl.get();
        }
        long baseTime = System.nanoTime() - startTime;
        
        // Measure optimized implementation
        startTime = System.nanoTime();
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            optimImpl.get();
        }
        long optimTime = System.nanoTime() - startTime;
        
        double speedup = (double) baseTime / optimTime;
        System.out.printf("🚀 %s Performance: Base=%.2fms, Optimized=%.2fms, Speedup=%.2fx%n", 
            sizeName, baseTime / 1_000_000.0, optimTime / 1_000_000.0, speedup);
    }
    
    /**
     * Generate test data for arrays of given size
     */
    private static double[] generateTestData(int size) {
        double[] data = new double[size];
        for (int i = 0; i < size; i++) {
            data[i] = Math.sin(2 * Math.PI * i / size) + 0.5 * Math.cos(4 * Math.PI * i / size);
        }
        return data;
    }
    
    /**
     * Functional interface for FFT suppliers
     */
    @FunctionalInterface
    private interface FFTSupplier {
        double[] get();
    }
}
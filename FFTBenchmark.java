/**
 * Performance benchmark for different FFT implementations.
 * Tests the actual FFT computation time (not just array iteration).
 * 
 * @author Engine AI Assistant
 */
public class FFTBenchmark {
    
    private static final int WARMUP_ITERATIONS = 1000;
    private static final int BENCHMARK_ITERATIONS = 10000;
    
    public static void main(String[] args) {
        System.out.println("FFT Performance Benchmark");
        System.out.println("=========================");
        System.out.println("Warmup iterations: " + WARMUP_ITERATIONS);
        System.out.println("Benchmark iterations: " + BENCHMARK_ITERATIONS);
        System.out.println();
        
        benchmarkSize8();
        benchmarkSize32();
        benchmarkSize64();
        benchmarkSize128();
        benchmarkSize256();
        benchmarkSize512();
        benchmarkSize1024();
        benchmarkSize2048();
        benchmarkSize4096();
        benchmarkSize8192();
    }
    
    private static void benchmarkSize8() {
        System.out.println("Benchmarking Size 8:");
        System.out.println("-------------------");
        
        double[] inputReal = {1, 2, 3, 4, 5, 6, 7, 8};
        double[] inputImag = {0, 0, 0, 0, 0, 0, 0, 0};
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
            FFToptim8.fft(inputReal, inputImag, true);
        }
        
        // Benchmark FFTbase
        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
        }
        long baseTime = System.nanoTime() - startTime;
        
        // Benchmark FFToptim8
        startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFToptim8.fft(inputReal, inputImag, true);
        }
        long optimTime = System.nanoTime() - startTime;
        
        double speedup = (double) baseTime / optimTime;
        System.out.printf("FFTbase:     %,d ns (%.2f ms)\n", baseTime, baseTime / 1_000_000.0);
        System.out.printf("FFToptim8:   %,d ns (%.2f ms)\n", optimTime, optimTime / 1_000_000.0);
        System.out.printf("Speedup:     %.2fx\n", speedup);
        System.out.println();
    }
    
    private static void benchmarkSize32() {
        System.out.println("Benchmarking Size 32:");
        System.out.println("--------------------");
        
        double[] inputReal = new double[32];
        double[] inputImag = new double[32];
        for (int i = 0; i < 32; i++) {
            inputReal[i] = Math.sin(2 * Math.PI * i / 32);
            inputImag[i] = 0;
        }
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
            FFToptim32.fft(inputReal, inputImag, true);
        }
        
        // Benchmark FFTbase
        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
        }
        long baseTime = System.nanoTime() - startTime;
        
        // Benchmark FFToptim32
        startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFToptim32.fft(inputReal, inputImag, true);
        }
        long optimTime = System.nanoTime() - startTime;
        
        double speedup = (double) baseTime / optimTime;
        System.out.printf("FFTbase:     %,d ns (%.2f ms)\n", baseTime, baseTime / 1_000_000.0);
        System.out.printf("FFToptim32:  %,d ns (%.2f ms)\n", optimTime, optimTime / 1_000_000.0);
        System.out.printf("Speedup:     %.2fx\n", speedup);
        System.out.println();
    }
    
    private static void benchmarkSize64() {
        System.out.println("Benchmarking Size 64:");
        System.out.println("--------------------");
        
        double[] inputReal = generateTestData(64);
        double[] inputImag = new double[64];
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
            FFToptim64.fft(inputReal, inputImag, true);
        }
        
        // Benchmark FFTbase
        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
        }
        long baseTime = System.nanoTime() - startTime;
        
        // Benchmark FFToptim64
        startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFToptim64.fft(inputReal, inputImag, true);
        }
        long optimTime = System.nanoTime() - startTime;
        
        double speedup = (double) baseTime / optimTime;
        System.out.printf("FFTbase:     %,d ns (%.2f ms)\n", baseTime, baseTime / 1_000_000.0);
        System.out.printf("FFToptim64:  %,d ns (%.2f ms)\n", optimTime, optimTime / 1_000_000.0);
        System.out.printf("Speedup:     %.2fx\n", speedup);
        System.out.println();
    }
    
    private static void benchmarkSize128() {
        System.out.println("Benchmarking Size 128:");
        System.out.println("---------------------");
        
        double[] inputReal = generateTestData(128);
        double[] inputImag = new double[128];
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
            FFToptim128.fft(inputReal, inputImag, true);
        }
        
        // Benchmark FFTbase
        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
        }
        long baseTime = System.nanoTime() - startTime;
        
        // Benchmark FFToptim128
        startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFToptim128.fft(inputReal, inputImag, true);
        }
        long optimTime = System.nanoTime() - startTime;
        
        double speedup = (double) baseTime / optimTime;
        System.out.printf("FFTbase:     %,d ns (%.2f ms)\n", baseTime, baseTime / 1_000_000.0);
        System.out.printf("FFToptim128: %,d ns (%.2f ms)\n", optimTime, optimTime / 1_000_000.0);
        System.out.printf("Speedup:     %.2fx\n", speedup);
        System.out.println();
    }
    
    private static void benchmarkSize256() {
        System.out.println("Benchmarking Size 256:");
        System.out.println("---------------------");
        
        double[] inputReal = generateTestData(256);
        double[] inputImag = new double[256];
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
            FFToptim256.fft(inputReal, inputImag, true);
        }
        
        // Benchmark FFTbase
        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
        }
        long baseTime = System.nanoTime() - startTime;
        
        // Benchmark FFToptim256
        startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFToptim256.fft(inputReal, inputImag, true);
        }
        long optimTime = System.nanoTime() - startTime;
        
        double speedup = (double) baseTime / optimTime;
        System.out.printf("FFTbase:     %,d ns (%.2f ms)\n", baseTime, baseTime / 1_000_000.0);
        System.out.printf("FFToptim256: %,d ns (%.2f ms)\n", optimTime, optimTime / 1_000_000.0);
        System.out.printf("Speedup:     %.2fx\n", speedup);
        System.out.println();
    }
    
    private static void benchmarkSize512() {
        System.out.println("Benchmarking Size 512:");
        System.out.println("---------------------");
        
        double[] inputReal = generateTestData(512);
        double[] inputImag = new double[512];
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
            FFToptim512.fft(inputReal, inputImag, true);
        }
        
        // Benchmark FFTbase
        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
        }
        long baseTime = System.nanoTime() - startTime;
        
        // Benchmark FFToptim512
        startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFToptim512.fft(inputReal, inputImag, true);
        }
        long optimTime = System.nanoTime() - startTime;
        
        double speedup = (double) baseTime / optimTime;
        System.out.printf("FFTbase:     %,d ns (%.2f ms)\n", baseTime, baseTime / 1_000_000.0);
        System.out.printf("FFToptim512: %,d ns (%.2f ms)\n", optimTime, optimTime / 1_000_000.0);
        System.out.printf("Speedup:     %.2fx\n", speedup);
        System.out.println();
    }
    
    private static void benchmarkSize1024() {
        System.out.println("Benchmarking Size 1024:");
        System.out.println("----------------------");
        
        double[] inputReal = generateTestData(1024);
        double[] inputImag = new double[1024];
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
            FFToptim1024.fft(inputReal, inputImag, true);
        }
        
        // Benchmark FFTbase
        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
        }
        long baseTime = System.nanoTime() - startTime;
        
        // Benchmark FFToptim1024
        startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFToptim1024.fft(inputReal, inputImag, true);
        }
        long optimTime = System.nanoTime() - startTime;
        
        double speedup = (double) baseTime / optimTime;
        System.out.printf("FFTbase:      %,d ns (%.2f ms)\n", baseTime, baseTime / 1_000_000.0);
        System.out.printf("FFToptim1024: %,d ns (%.2f ms)\n", optimTime, optimTime / 1_000_000.0);
        System.out.printf("Speedup:      %.2fx\n", speedup);
        System.out.println();
    }
    
    private static void benchmarkSize2048() {
        System.out.println("Benchmarking Size 2048:");
        System.out.println("----------------------");
        
        double[] inputReal = generateTestData(2048);
        double[] inputImag = new double[2048];
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
            FFToptim2048.fft(inputReal, inputImag, true);
        }
        
        // Benchmark FFTbase
        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
        }
        long baseTime = System.nanoTime() - startTime;
        
        // Benchmark FFToptim2048
        startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFToptim2048.fft(inputReal, inputImag, true);
        }
        long optimTime = System.nanoTime() - startTime;
        
        double speedup = (double) baseTime / optimTime;
        System.out.printf("FFTbase:      %,d ns (%.2f ms)\n", baseTime, baseTime / 1_000_000.0);
        System.out.printf("FFToptim2048: %,d ns (%.2f ms)\n", optimTime, optimTime / 1_000_000.0);
        System.out.printf("Speedup:      %.2fx\n", speedup);
        System.out.println();
    }
    
    private static void benchmarkSize4096() {
        System.out.println("Benchmarking Size 4096:");
        System.out.println("----------------------");
        
        double[] inputReal = generateTestData(4096);
        double[] inputImag = new double[4096];
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
            FFToptim4096.fft(inputReal, inputImag, true);
        }
        
        // Benchmark FFTbase
        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
        }
        long baseTime = System.nanoTime() - startTime;
        
        // Benchmark FFToptim4096
        startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFToptim4096.fft(inputReal, inputImag, true);
        }
        long optimTime = System.nanoTime() - startTime;
        
        double speedup = (double) baseTime / optimTime;
        System.out.printf("FFTbase:      %,d ns (%.2f ms)\n", baseTime, baseTime / 1_000_000.0);
        System.out.printf("FFToptim4096: %,d ns (%.2f ms)\n", optimTime, optimTime / 1_000_000.0);
        System.out.printf("Speedup:      %.2fx\n", speedup);
        System.out.println();
    }
    
    private static void benchmarkSize8192() {
        System.out.println("Benchmarking Size 8192:");
        System.out.println("----------------------");
        
        double[] inputReal = generateTestData(8192);
        double[] inputImag = new double[8192];
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
            FFToptim8192.fft(inputReal, inputImag, true);
        }
        
        // Benchmark FFTbase
        long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFTbase.fft(inputReal, inputImag, true);
        }
        long baseTime = System.nanoTime() - startTime;
        
        // Benchmark FFToptim8192
        startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            FFToptim8192.fft(inputReal, inputImag, true);
        }
        long optimTime = System.nanoTime() - startTime;
        
        double speedup = (double) baseTime / optimTime;
        System.out.printf("FFTbase:      %,d ns (%.2f ms)\n", baseTime, baseTime / 1_000_000.0);
        System.out.printf("FFToptim8192: %,d ns (%.2f ms)\n", optimTime, optimTime / 1_000_000.0);
        System.out.printf("Speedup:      %.2fx\n", speedup);
        System.out.println();
    }
    
    private static double[] generateTestData(int size) {
        double[] data = new double[size];
        for (int i = 0; i < size; i++) {
            data[i] = Math.sin(2 * Math.PI * i / size) + 0.5 * Math.cos(4 * Math.PI * i / size);
        }
        return data;
    }
}
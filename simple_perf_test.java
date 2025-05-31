import com.fft.optimized.FFTOptimized8;
import com.fft.optimized.FFTOptimized32;
import com.fft.optimized.FFTOptimized64;
import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;

public class simple_perf_test {
    public static void main(String[] args) {
        int size = 8;
        
        // Generate test signal
        double[] real = FFTUtils.generateTestSignal(size, "impulse");
        double[] imag = new double[size];
        
        FFTOptimized8 opt8 = new FFTOptimized8();
        FFTBase base = new FFTBase();
        
        // Warmup
        for (int i = 0; i < 1000; i++) {
            opt8.transform(real, imag, true);
            base.transform(real, imag, true);
        }
        
        // Benchmark
        int iterations = 10000;
        
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            FFTResult result = opt8.transform(real, imag, true);
        }
        long optTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            FFTResult result = base.transform(real, imag, true);
        }
        long baseTime = System.nanoTime() - start;
        
        System.out.println("Size 8 Performance:");
        System.out.println("Optimized: " + (optTime / iterations) + " ns per call");
        System.out.println("Base: " + (baseTime / iterations) + " ns per call");
        System.out.println("Speedup: " + String.format("%.2fx", (double)baseTime / optTime));
        
        // Test FFTOptimized32
        size = 32;
        real = FFTUtils.generateTestSignal(size, "impulse");
        imag = new double[size];
        
        FFTOptimized32 opt32 = new FFTOptimized32();
        
        // Warmup
        for (int i = 0; i < 1000; i++) {
            opt32.transform(real, imag, true);
            base.transform(real, imag, true);
        }
        
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            FFTResult result = opt32.transform(real, imag, true);
        }
        optTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            FFTResult result = base.transform(real, imag, true);
        }
        baseTime = System.nanoTime() - start;
        
        System.out.println("\nSize 32 Performance:");
        System.out.println("Optimized: " + (optTime / iterations) + " ns per call");
        System.out.println("Base: " + (baseTime / iterations) + " ns per call");
        System.out.println("Speedup: " + String.format("%.2fx", (double)baseTime / optTime));
    }
}
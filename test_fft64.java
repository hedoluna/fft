import com.fft.optimized.FFTOptimized64;
import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;

public class test_fft64 {
    public static void main(String[] args) {
        int size = 64;
        
        // Generate test signal
        double[] real = FFTUtils.generateTestSignal(size, "impulse");
        double[] imag = new double[size];
        
        FFTOptimized64 opt64 = new FFTOptimized64();
        FFTBase base = new FFTBase();
        
        // Test correctness first
        FFTResult optResult = opt64.transform(real, imag, true);
        FFTResult baseResult = base.transform(real, imag, true);
        
        double[] optData = optResult.getInterleavedResult();
        double[] baseData = baseResult.getInterleavedResult();
        
        boolean correct = true;
        double maxError = 0.0;
        for (int i = 0; i < optData.length; i++) {
            double error = Math.abs(optData[i] - baseData[i]);
            if (error > maxError) maxError = error;
            if (error > 1e-6) {
                correct = false;
            }
        }
        
        System.out.println("Correctness test: " + (correct ? "PASS" : "FAIL"));
        System.out.println("Maximum error: " + maxError);
        
        if (!correct) {
            System.out.println("Test failed, aborting performance test");
            return;
        }
        
        // Warmup
        for (int i = 0; i < 1000; i++) {
            opt64.transform(real, imag, true);
            base.transform(real, imag, true);
        }
        
        // Benchmark
        int iterations = 10000;
        
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            FFTResult result = opt64.transform(real, imag, true);
        }
        long optTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            FFTResult result = base.transform(real, imag, true);
        }
        long baseTime = System.nanoTime() - start;
        
        System.out.println("\nSize 64 Performance:");
        System.out.println("Optimized: " + (optTime / iterations) + " ns per call");
        System.out.println("Base: " + (baseTime / iterations) + " ns per call");
        System.out.println("Speedup: " + String.format("%.2fx", (double)baseTime / optTime));
    }
}
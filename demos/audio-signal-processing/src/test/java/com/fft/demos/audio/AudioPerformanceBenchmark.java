package com.fft.demos.audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Performance benchmark suite for Audio Signal Processing Demo.
 * 
 * <p>This benchmark suite evaluates the real-time processing capabilities
 * and performance characteristics of audio signal processing operations.
 * It simulates real-world audio processing scenarios and validates
 * performance requirements for professional audio applications.</p>
 * 
 * <h3>Benchmark Categories:</h3>
 * <ul>
 * <li><b>Real-time Processing:</b> Validates processing within audio buffer constraints</li>
 * <li><b>Throughput Analysis:</b> Measures processing rates for different operations</li>
 * <li><b>Memory Performance:</b> Monitors memory allocation and garbage collection</li>
 * <li><b>Scalability Testing:</b> Performance across different signal sizes</li>
 * <li><b>Quality vs Performance:</b> Trade-offs between quality and speed</li>
 * </ul>
 */
class AudioPerformanceBenchmark {
    
    private AudioSignalProcessor processor;
    private static final double SAMPLE_RATE = 44100.0;
    
    // Real-time audio constraints
    private static final int[] BUFFER_SIZES = {256, 512, 1024, 2048};
    private static final double[] BUFFER_DURATIONS_MS = {
        256 * 1000.0 / SAMPLE_RATE,  // ~5.8ms
        512 * 1000.0 / SAMPLE_RATE,  // ~11.6ms  
        1024 * 1000.0 / SAMPLE_RATE, // ~23.2ms
        2048 * 1000.0 / SAMPLE_RATE  // ~46.4ms
    };
    
    @BeforeEach
    void setUp() {
        processor = new AudioSignalProcessor();
        // Warm up JVM
        warmUpJvm();
    }
    
    @Nested
    @DisplayName("Real-time Processing Benchmarks")
    class RealtimeBenchmarks {
        
        @Test
        @DisplayName("Filter processing should meet real-time constraints")
        void filterProcessingShouldMeetRealtimeConstraints() {
            System.out.println("\n=== Filter Processing Real-time Benchmark ===");
            
            for (int i = 0; i < BUFFER_SIZES.length; i++) {
                int bufferSize = BUFFER_SIZES[i];
                double maxAllowedMs = BUFFER_DURATIONS_MS[i] * 0.8; // 80% CPU budget
                
                System.out.printf("\nBuffer Size: %d samples (%.1fms duration, %.1fms budget)\n", 
                                 bufferSize, BUFFER_DURATIONS_MS[i], maxAllowedMs);
                
                benchmarkFilterOperation(bufferSize, maxAllowedMs, "lowpass");
                benchmarkFilterOperation(bufferSize, maxAllowedMs, "highpass");
                benchmarkFilterOperation(bufferSize, maxAllowedMs, "vocal");
                benchmarkFilterOperation(bufferSize, maxAllowedMs, "notch60");
            }
        }
        
        @Test
        @DisplayName("Audio analysis should meet real-time constraints")
        void audioAnalysisShouldMeetRealtimeConstraints() {
            System.out.println("\n=== Audio Analysis Real-time Benchmark ===");
            
            for (int i = 0; i < BUFFER_SIZES.length; i++) {
                int bufferSize = BUFFER_SIZES[i];
                double maxAllowedMs = BUFFER_DURATIONS_MS[i] * 0.5; // 50% CPU budget for analysis
                
                System.out.printf("\nBuffer Size: %d samples (%.1fms budget)\n", 
                                 bufferSize, maxAllowedMs);
                
                benchmarkAnalysisOperation(bufferSize, maxAllowedMs);
            }
        }
        
        @Test
        @DisplayName("Noise reduction should meet real-time constraints")
        void noiseReductionShouldMeetRealtimeConstraints() {
            System.out.println("\n=== Noise Reduction Real-time Benchmark ===");
            
            // Generate noise profile once
            double[] noiseProfile = generateNoise(256);
            
            for (int i = 0; i < BUFFER_SIZES.length; i++) {
                int bufferSize = BUFFER_SIZES[i];
                double maxAllowedMs = BUFFER_DURATIONS_MS[i] * 0.9; // 90% CPU budget
                
                System.out.printf("\nBuffer Size: %d samples (%.1fms budget)\n", 
                                 bufferSize, maxAllowedMs);
                
                benchmarkNoiseReductionOperation(bufferSize, maxAllowedMs, noiseProfile);
            }
        }
        
        @Test
        @DisplayName("Complete processing chain should meet real-time constraints")
        void completeProcessingChainShouldMeetRealtimeConstraints() {
            System.out.println("\n=== Complete Processing Chain Real-time Benchmark ===");
            
            double[] noiseProfile = generateNoise(256);
            
            for (int i = 0; i < BUFFER_SIZES.length; i++) {
                int bufferSize = BUFFER_SIZES[i];
                double maxAllowedMs = BUFFER_DURATIONS_MS[i]; // Full budget for complete chain
                
                System.out.printf("\nBuffer Size: %d samples (%.1fms budget)\n", 
                                 bufferSize, maxAllowedMs);
                
                double[] signal = generateComplexAudioSignal(bufferSize);
                
                long startTime = System.nanoTime();
                
                // Complete processing chain
                double[] filtered = processor.applyFilter(signal, "vocal", null, SAMPLE_RATE);
                double[] denoised = processor.reduceNoise(filtered, noiseProfile, SAMPLE_RATE, 2.0);
                AudioSignalProcessor.AudioMetrics metrics = processor.analyzeSignal(denoised, SAMPLE_RATE);
                
                long endTime = System.nanoTime();
                double processingTimeMs = (endTime - startTime) / 1_000_000.0;
                
                System.out.printf("  Complete chain: %.2fms (%.1f%% CPU)\n", 
                                 processingTimeMs, processingTimeMs / BUFFER_DURATIONS_MS[i] * 100);
                
                // Should meet real-time constraint
                assertThat(processingTimeMs)
                    .withFailMessage("Processing time %.2fms exceeds budget %.2fms for buffer size %d", 
                                   processingTimeMs, maxAllowedMs, bufferSize)
                    .isLessThan(maxAllowedMs);
            }
        }
        
        private void benchmarkFilterOperation(int bufferSize, double maxAllowedMs, String filterName) {
            double[] signal = generateComplexAudioSignal(bufferSize);
            
            // Warm up
            for (int i = 0; i < 5; i++) {
                processor.applyFilter(signal, filterName, null, SAMPLE_RATE);
            }
            
            // Benchmark
            List<Double> times = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                long startTime = System.nanoTime();
                processor.applyFilter(signal, filterName, null, SAMPLE_RATE);
                long endTime = System.nanoTime();
                
                times.add((endTime - startTime) / 1_000_000.0);
            }
            
            double avgTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double maxTime = times.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            
            System.out.printf("  %-8s filter: avg=%.2fms, max=%.2fms (%.1f%% CPU)\n", 
                             filterName, avgTime, maxTime, avgTime / BUFFER_DURATIONS_MS[getBufferIndex(bufferSize)] * 100);
            
            assertThat(avgTime)
                .withFailMessage("Average filter processing time %.2fms exceeds budget %.2fms", avgTime, maxAllowedMs)
                .isLessThan(maxAllowedMs);
        }
        
        private void benchmarkAnalysisOperation(int bufferSize, double maxAllowedMs) {
            double[] signal = generateComplexAudioSignal(bufferSize);
            
            // Warm up
            for (int i = 0; i < 5; i++) {
                processor.analyzeSignal(signal, SAMPLE_RATE);
            }
            
            // Benchmark
            List<Double> times = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                long startTime = System.nanoTime();
                processor.analyzeSignal(signal, SAMPLE_RATE);
                long endTime = System.nanoTime();
                
                times.add((endTime - startTime) / 1_000_000.0);
            }
            
            double avgTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double maxTime = times.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            
            System.out.printf("  Analysis: avg=%.2fms, max=%.2fms (%.1f%% CPU)\n", 
                             avgTime, maxTime, avgTime / BUFFER_DURATIONS_MS[getBufferIndex(bufferSize)] * 100);
            
            assertThat(avgTime)
                .withFailMessage("Average analysis time %.2fms exceeds budget %.2fms", avgTime, maxAllowedMs)
                .isLessThan(maxAllowedMs);
        }
        
        private void benchmarkNoiseReductionOperation(int bufferSize, double maxAllowedMs, double[] noiseProfile) {
            double[] signal = generateNoisyAudioSignal(bufferSize);
            
            // Warm up
            for (int i = 0; i < 5; i++) {
                processor.reduceNoise(signal, noiseProfile, SAMPLE_RATE, 2.0);
            }
            
            // Benchmark
            List<Double> times = new ArrayList<>();
            for (int i = 0; i < 10; i++) { // Fewer iterations as this is more expensive
                long startTime = System.nanoTime();
                processor.reduceNoise(signal, noiseProfile, SAMPLE_RATE, 2.0);
                long endTime = System.nanoTime();
                
                times.add((endTime - startTime) / 1_000_000.0);
            }
            
            double avgTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double maxTime = times.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            
            System.out.printf("  Noise reduction: avg=%.2fms, max=%.2fms (%.1f%% CPU)\n", 
                             avgTime, maxTime, avgTime / BUFFER_DURATIONS_MS[getBufferIndex(bufferSize)] * 100);
            
            assertThat(avgTime)
                .withFailMessage("Average noise reduction time %.2fms exceeds budget %.2fms", avgTime, maxAllowedMs)
                .isLessThan(maxAllowedMs);
        }
        
        private int getBufferIndex(int bufferSize) {
            for (int i = 0; i < BUFFER_SIZES.length; i++) {
                if (BUFFER_SIZES[i] == bufferSize) return i;
            }
            return 0;
        }
    }
    
    @Nested
    @DisplayName("Throughput Analysis")
    class ThroughputBenchmarks {
        
        @Test
        @DisplayName("Should achieve high throughput for filter operations")
        void shouldAchieveHighThroughputForFilterOperations() {
            System.out.println("\n=== Filter Throughput Benchmark ===");
            
            int bufferSize = 1024;
            int numBuffers = 1000;
            double[] signal = generateComplexAudioSignal(bufferSize);
            
            for (String filterName : processor.getAvailableFilters()) {
                // Warm up
                for (int i = 0; i < 10; i++) {
                    processor.applyFilter(signal, filterName, null, SAMPLE_RATE);
                }
                
                long startTime = System.nanoTime();
                for (int i = 0; i < numBuffers; i++) {
                    processor.applyFilter(signal, filterName, null, SAMPLE_RATE);
                }
                long endTime = System.nanoTime();
                
                double totalTimeS = (endTime - startTime) / 1_000_000_000.0;
                double audioDurationS = (double) (numBuffers * bufferSize) / SAMPLE_RATE;
                double realtimeFactor = audioDurationS / totalTimeS;
                
                System.out.printf("  %-8s: %.1fx real-time (%.1fMB/s audio)\n", 
                                 filterName, realtimeFactor, 
                                 audioDurationS * 44.1 * 2 / totalTimeS / 1024 / 1024); // Assume 16-bit
                
                // Should process much faster than real-time
                assertThat(realtimeFactor).isGreaterThan(5.0);
            }
        }
        
        @Test
        @DisplayName("Should achieve efficient batch processing")
        void shouldAchieveEfficientBatchProcessing() {
            System.out.println("\n=== Batch Processing Benchmark ===");
            
            int[] batchSizes = {10, 50, 100, 500};
            int bufferSize = 1024;
            
            for (int batchSize : batchSizes) {
                List<double[]> signals = new ArrayList<>();
                for (int i = 0; i < batchSize; i++) {
                    signals.add(generateComplexAudioSignal(bufferSize));
                }
                
                long startTime = System.nanoTime();
                for (double[] signal : signals) {
                    processor.applyFilter(signal, "vocal", null, SAMPLE_RATE);
                    processor.analyzeSignal(signal, SAMPLE_RATE);
                }
                long endTime = System.nanoTime();
                
                double totalTimeMs = (endTime - startTime) / 1_000_000.0;
                double avgTimePerBuffer = totalTimeMs / batchSize;
                double audioDuration = (double) bufferSize / SAMPLE_RATE * 1000;
                
                System.out.printf("  Batch %3d: %.2fms/buffer (%.1fx real-time)\n", 
                                 batchSize, avgTimePerBuffer, audioDuration / avgTimePerBuffer);
                
                // Batch processing should be efficient
                assertThat(avgTimePerBuffer).isLessThan(audioDuration * 0.5); // 50% CPU max
            }
        }
    }
    
    @Nested
    @DisplayName("Memory Performance")
    class MemoryBenchmarks {
        
        @Test
        @DisplayName("Should have minimal garbage collection impact")
        void shouldHaveMinimalGarbageCollectionImpact() {
            System.out.println("\n=== Memory Performance Benchmark ===");
            
            Runtime runtime = Runtime.getRuntime();
            
            // Force GC before test
            System.gc();
            System.gc();
            
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();
            
            // Simulate sustained processing
            double[] signal = generateComplexAudioSignal(1024);
            double[] noiseProfile = generateNoise(256);
            
            for (int i = 0; i < 500; i++) {
                processor.applyFilter(signal, "vocal", null, SAMPLE_RATE);
                processor.analyzeSignal(signal, SAMPLE_RATE);
                
                if (i % 100 == 0) {
                    processor.reduceNoise(signal, noiseProfile, SAMPLE_RATE, 2.0);
                }
            }
            
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();
            long memoryIncrease = finalMemory - initialMemory;
            
            System.out.printf("  Initial memory: %.1f MB\n", initialMemory / (1024.0 * 1024.0));
            System.out.printf("  Final memory: %.1f MB\n", finalMemory / (1024.0 * 1024.0));
            System.out.printf("  Memory increase: %.1f MB\n", memoryIncrease / (1024.0 * 1024.0));
            
            // Memory increase should be minimal (< 10MB for sustained processing)
            assertThat(memoryIncrease).isLessThan(10 * 1024 * 1024);
        }
        
        @Test
        @DisplayName("Should handle memory pressure gracefully")
        void shouldHandleMemoryPressureGracefully() {
            System.out.println("\n=== Memory Pressure Test ===");
            
            // Process large signals to test memory efficiency
            int[] largeSizes = {8192, 16384, 32768};
            
            for (int size : largeSizes) {
                double[] signal = generateComplexAudioSignal(size);
                
                long startTime = System.nanoTime();
                
                // Should handle large signals without excessive memory usage
                assertDoesNotThrow(() -> {
                    processor.applyFilter(signal, "lowpass", null, SAMPLE_RATE);
                    processor.analyzeSignal(signal, SAMPLE_RATE);
                });
                
                long endTime = System.nanoTime();
                double processingTimeMs = (endTime - startTime) / 1_000_000.0;
                
                System.out.printf("  Size %5d: %.2fms\n", size, processingTimeMs);
                
                // Processing time should scale reasonably with size
                double expectedTime = size / 1024.0 * 5.0; // ~5ms per 1024 samples
                assertThat(processingTimeMs).isLessThan(expectedTime);
            }
        }
        
        private void assertDoesNotThrow(Runnable runnable) {
            try {
                runnable.run();
            } catch (Exception e) {
                throw new AssertionError("Expected no exception but got: " + e.getMessage(), e);
            }
        }
    }
    
    @Nested
    @DisplayName("Quality vs Performance Analysis")
    class QualityPerformanceBenchmarks {
        
        @Test
        @DisplayName("Should demonstrate quality-performance trade-offs")
        void shouldDemonstrateQualityPerformanceTradeoffs() {
            System.out.println("\n=== Quality vs Performance Analysis ===");
            
            double[] cleanSignal = generateComplexAudioSignal(2048);
            double[] noisySignal = addNoise(cleanSignal, 0.2);
            double[] noiseProfile = generateNoise(256);
            
            // Test different noise reduction aggressiveness levels
            double[] alphaValues = {1.0, 1.5, 2.0, 2.5, 3.0};
            
            System.out.println("  Noise Reduction Alpha vs Performance:");
            
            for (double alpha : alphaValues) {
                List<Double> times = new ArrayList<>();
                
                for (int i = 0; i < 10; i++) {
                    long startTime = System.nanoTime();
                    double[] denoised = processor.reduceNoise(noisySignal, noiseProfile, SAMPLE_RATE, alpha);
                    long endTime = System.nanoTime();
                    
                    times.add((endTime - startTime) / 1_000_000.0);
                }
                
                double avgTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                
                // Calculate quality metric (simplified)
                double[] denoised = processor.reduceNoise(noisySignal, noiseProfile, SAMPLE_RATE, alpha);
                double qualityScore = calculateSignalQuality(cleanSignal, denoised);
                
                System.out.printf("    α=%.1f: %.2fms processing, quality=%.3f\n", 
                                 alpha, avgTime, qualityScore);
            }
        }
        
        @Test
        @DisplayName("Should show reverb quality vs performance")
        void shouldShowReverbQualityVsPerformance() {
            System.out.println("\n  Reverb Time vs Performance:");
            
            double[] signal = generateComplexAudioSignal(1024);
            double[] reverbTimes = {0.2, 0.5, 1.0, 1.5, 2.0};
            
            for (double reverbTime : reverbTimes) {
                long startTime = System.nanoTime();
                double[] reverb = processor.addReverb(signal, reverbTime, SAMPLE_RATE);
                long endTime = System.nanoTime();
                
                double processingTimeMs = (endTime - startTime) / 1_000_000.0;
                
                // Analyze reverb characteristics
                AudioSignalProcessor.AudioMetrics metrics = processor.analyzeSignal(reverb, SAMPLE_RATE);
                
                System.out.printf("    %.1fs reverb: %.2fms processing, spread=%.1fHz\n", 
                                 reverbTime, processingTimeMs, metrics.getSpectralSpread());
            }
        }
        
        private double calculateSignalQuality(double[] reference, double[] test) {
            // Simplified SNR calculation
            double signalPower = 0;
            double noisePower = 0;
            
            for (int i = 0; i < Math.min(reference.length, test.length); i++) {
                signalPower += reference[i] * reference[i];
                double noise = reference[i] - test[i];
                noisePower += noise * noise;
            }
            
            if (noisePower == 0) return Double.POSITIVE_INFINITY;
            return 10 * Math.log10(signalPower / noisePower);
        }
    }
    
    // Utility methods for benchmark signal generation
    
    private void warmUpJvm() {
        double[] signal = generateComplexAudioSignal(1024);
        for (int i = 0; i < 50; i++) {
            processor.analyzeSignal(signal, SAMPLE_RATE);
            processor.applyFilter(signal, "lowpass", null, SAMPLE_RATE);
        }
    }
    
    private double[] generateComplexAudioSignal(int length) {
        double[] signal = new double[length];
        Random random = new Random(42);
        
        for (int i = 0; i < length; i++) {
            double t = (double) i / SAMPLE_RATE;
            
            // Complex audio signal with multiple components
            signal[i] = 
                0.3 * Math.sin(2 * Math.PI * 220 * t) +                    // Fundamental
                0.2 * Math.sin(2 * Math.PI * 440 * t) +                    // Harmonic
                0.1 * Math.sin(2 * Math.PI * 660 * t) +                    // Harmonic
                0.05 * Math.sin(2 * Math.PI * 1200 * t) * Math.exp(-t) +   // Transient
                0.02 * random.nextGaussian();                              // Noise
        }
        
        return signal;
    }
    
    private double[] generateNoisyAudioSignal(int length) {
        double[] clean = generateComplexAudioSignal(length);
        return addNoise(clean, 0.15);
    }
    
    private double[] generateNoise(int length) {
        double[] noise = new double[length];
        Random random = new Random(123);
        for (int i = 0; i < length; i++) {
            noise[i] = random.nextGaussian() * 0.1;
        }
        return noise;
    }
    
    private double[] addNoise(double[] signal, double noiseLevel) {
        double[] noisy = signal.clone();
        Random random = new Random(456);
        for (int i = 0; i < noisy.length; i++) {
            noisy[i] += random.nextGaussian() * noiseLevel;
        }
        return noisy;
    }
}
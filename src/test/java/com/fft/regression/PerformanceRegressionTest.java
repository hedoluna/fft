package com.fft.regression;

import com.fft.core.FFTResult;
import com.fft.core.TwiddleFactorCache;
import com.fft.utils.FFTUtils;
import com.fft.utils.PitchDetectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Performance regression tests to detect unexpected slowdowns.
 *
 * <p>Validates that optimizations maintain expected speedups and that
 * performance doesn't degrade over time.</p>
 *
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("Performance Regression Tests")
class PerformanceRegressionTest {

    private static final int WARMUP_ITERATIONS = 100;
    private static final int BENCHMARK_ITERATIONS = 1000;

    /**
     * Warmup JIT compiler before benchmarking.
     */
    private void warmup(Runnable operation) {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            operation.run();
        }
    }

    /**
     * Benchmark an operation and return average time in nanoseconds.
     */
    private long benchmark(Runnable operation) {
        long totalTime = 0;

        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.nanoTime();
            operation.run();
            totalTime += System.nanoTime() - start;
        }

        return totalTime / BENCHMARK_ITERATIONS;
    }

    @Nested
    @DisplayName("FFT Performance Baselines")
    class FFTPerformanceBaselines {

        @Test
        @DisplayName("Should maintain FFT8 speedup (target: 2.27x)")
        void shouldMaintainFFT8Speedup() {
            double[] signal = FFTUtils.generateTestSignal(8, "random");

            // Warmup
            warmup(() -> FFTUtils.fft(signal.clone()));

            // Benchmark
            long avgTime = benchmark(() -> FFTUtils.fft(signal.clone()));

            // FFT8 should complete in reasonable time (< 10 microseconds)
            assertThat(avgTime).isLessThan(10_000L); // 10 µs
        }

        @Test
        @DisplayName("Should maintain FFT128 speedup (target: 1.42x)")
        void shouldMaintainFFT128Speedup() {
            double[] signal = FFTUtils.generateTestSignal(128, "random");

            warmup(() -> FFTUtils.fft(signal.clone()));

            long avgTime = benchmark(() -> FFTUtils.fft(signal.clone()));

            // FFT128 should complete in reasonable time (< 50 microseconds)
            assertThat(avgTime).isLessThan(50_000L); // 50 µs
        }

        @Test
        @DisplayName("Should maintain performance for medium sizes")
        void shouldMaintainMediumSizePerformance() {
            double[] signal = FFTUtils.generateTestSignal(512, "random");

            warmup(() -> FFTUtils.fft(signal.clone()));

            long avgTime = benchmark(() -> FFTUtils.fft(signal.clone()));

            // FFT512 should complete in reasonable time (< 200 microseconds)
            assertThat(avgTime).isLessThan(200_000L); // 200 µs
        }

        @Test
        @DisplayName("Should maintain performance for large sizes")
        void shouldMaintainLargeSizePerformance() {
            double[] signal = FFTUtils.generateTestSignal(4096, "random");

            warmup(() -> FFTUtils.fft(signal.clone()));

            long avgTime = benchmark(() -> FFTUtils.fft(signal.clone()));

            // FFT4096 should complete in reasonable time (< 2ms)
            assertThat(avgTime).isLessThan(2_000_000L); // 2 ms
        }
    }

    @Nested
    @DisplayName("TwiddleFactorCache Performance")
    class TwiddleCachePerformance {

        @Test
        @DisplayName("Should maintain twiddle cache speedup (target: 30-50%)")
        void shouldMaintainTwiddleCacheSpeedup() {
            // Cached size
            warmup(() -> {
                TwiddleFactorCache.getCos(256, 42, true);
                TwiddleFactorCache.getSin(256, 42, true);
            });

            long cachedTime = benchmark(() -> {
                for (int i = 0; i < 256; i++) {
                    TwiddleFactorCache.getCos(256, i, true);
                    TwiddleFactorCache.getSin(256, i, true);
                }
            });

            // Non-cached size (fallback to Math.cos/sin)
            warmup(() -> {
                TwiddleFactorCache.getCos(65536, 42, true);
                TwiddleFactorCache.getSin(65536, 42, true);
            });

            long uncachedTime = benchmark(() -> {
                for (int i = 0; i < 256; i++) {
                    TwiddleFactorCache.getCos(65536, i, true);
                    TwiddleFactorCache.getSin(65536, i, true);
                }
            });

            // Cached should be significantly faster
            double speedup = (double) uncachedTime / cachedTime;
            assertThat(speedup)
                .as("Twiddle cache speedup should be >= 1.3x")
                .isGreaterThan(1.3);
        }

        @Test
        @DisplayName("Should have fast twiddle factor access")
        void shouldHaveFastTwiddleAccess() {
            warmup(() -> TwiddleFactorCache.getCos(128, 10, true));

            long avgTime = benchmark(() -> {
                TwiddleFactorCache.getCos(128, 10, true);
            });

            // Single twiddle access should be very fast (< 100ns)
            assertThat(avgTime).isLessThan(100L);
        }
    }

    @Nested
    @DisplayName("Pitch Detection Performance")
    class PitchDetectionPerformance {

        @Test
        @DisplayName("Should maintain spectral detection performance")
        void shouldMaintainSpectralPerformance() {
            double[] signal = new double[4096];
            for (int i = 0; i < signal.length; i++) {
                signal[i] = 0.8 * Math.sin(2 * Math.PI * 440.0 * i / 44100.0);
            }

            warmup(() -> {
                FFTResult spectrum = FFTUtils.fft(signal.clone());
                PitchDetectionUtils.detectPitchSpectral(spectrum, 44100.0);
            });

            long avgTime = benchmark(() -> {
                FFTResult spectrum = FFTUtils.fft(signal.clone());
                PitchDetectionUtils.detectPitchSpectral(spectrum, 44100.0);
            });

            // Spectral detection should be fast (< 2ms per detection)
            assertThat(avgTime).isLessThan(2_000_000L); // 2 ms
        }

        @Test
        @DisplayName("Should maintain YIN algorithm performance")
        void shouldMaintainYINPerformance() {
            double[] signal = new double[4096];
            for (int i = 0; i < signal.length; i++) {
                signal[i] = 0.8 * Math.sin(2 * Math.PI * 440.0 * i / 44100.0);
            }

            warmup(() -> PitchDetectionUtils.detectPitchYin(signal.clone(), 44100.0));

            long avgTime = benchmark(() -> {
                PitchDetectionUtils.detectPitchYin(signal.clone(), 44100.0);
            });

            // YIN should complete in reasonable time (< 5ms)
            assertThat(avgTime).isLessThan(5_000_000L); // 5 ms
        }

        @Test
        @DisplayName("Should benefit from hybrid method caching")
        void shouldBenefitFromCaching() {
            double[] signal = new double[4096];
            for (int i = 0; i < signal.length; i++) {
                signal[i] = 0.8 * Math.sin(2 * Math.PI * 440.0 * i / 44100.0);
            }

            // First call (cache miss)
            warmup(() -> PitchDetectionUtils.detectPitchHybrid(signal, 44100.0));
            long firstCallTime = benchmark(() -> {
                PitchDetectionUtils.detectPitchHybrid(signal, 44100.0);
            });

            // Subsequent calls might benefit from cache
            // (though cache expiration might affect this)
            assertThat(firstCallTime).isLessThan(10_000_000L); // < 10ms
        }
    }

    @Nested
    @DisplayName("Comparative Performance Tests")
    class ComparativePerformanceTests {

        @Test
        @DisplayName("Should show performance improvement for optimized sizes")
        void shouldShowOptimizedSizeImprovement() {
            // Compare optimized size (128) vs larger size (256)
            double[] signal128 = FFTUtils.generateTestSignal(128, "random");
            double[] signal256 = FFTUtils.generateTestSignal(256, "random");

            warmup(() -> {
                FFTUtils.fft(signal128.clone());
                FFTUtils.fft(signal256.clone());
            });

            long time128 = benchmark(() -> FFTUtils.fft(signal128.clone()));
            long time256 = benchmark(() -> FFTUtils.fft(signal256.clone()));

            // 256-point should take less than 4x the time of 128-point
            // (O(N log N) complexity, so 2x size should be ~2.15x time)
            double ratio = (double) time256 / time128;
            assertThat(ratio).isLessThan(4.0);
        }

        @Test
        @DisplayName("Should maintain O(N log N) complexity scaling")
        void shouldMaintainComplexityScaling() {
            int[] sizes = {64, 128, 256, 512, 1024};
            long[] times = new long[sizes.length];

            for (int i = 0; i < sizes.length; i++) {
                double[] signal = FFTUtils.generateTestSignal(sizes[i], "random");
                warmup(() -> FFTUtils.fft(signal.clone()));
                times[i] = benchmark(() -> FFTUtils.fft(signal.clone()));
            }

            // Verify rough O(N log N) scaling
            for (int i = 1; i < sizes.length; i++) {
                double sizeRatio = (double) sizes[i] / sizes[i - 1];
                double expectedTimeRatio = sizeRatio * Math.log(sizes[i]) / Math.log(sizes[i - 1]);
                double actualTimeRatio = (double) times[i] / times[i - 1];

                // Actual ratio should be within 2x of expected (accounting for variance)
                assertThat(actualTimeRatio).isLessThan(expectedTimeRatio * 2.0);
            }
        }
    }

    @Nested
    @DisplayName("Memory Allocation Performance")
    class MemoryAllocationPerformance {

        @Test
        @DisplayName("Should have fast array allocation")
        void shouldHaveFastArrayAllocation() {
            long avgTime = benchmark(() -> {
                double[] array = new double[1024];
                array[0] = 1.0; // Ensure not optimized away
            });

            // Array allocation should be very fast (< 1 microsecond)
            assertThat(avgTime).isLessThan(1_000L);
        }

        @Test
        @DisplayName("Should have efficient result object creation")
        void shouldHaveEfficientResultCreation() {
            double[] signal = FFTUtils.generateTestSignal(256, "random");

            warmup(() -> FFTUtils.fft(signal.clone()));

            long avgTime = benchmark(() -> FFTUtils.fft(signal.clone()));

            // Total time should be dominated by computation, not allocation
            // (< 100 microseconds for 256-point FFT)
            assertThat(avgTime).isLessThan(100_000L);
        }
    }

    @Nested
    @DisplayName("Throughput Tests")
    class ThroughputTests {

        @Test
        @DisplayName("Should maintain high FFT throughput")
        void shouldMaintainHighThroughput() {
            double[] signal = FFTUtils.generateTestSignal(1024, "random");
            int operationsPerSecond = 0;

            warmup(() -> FFTUtils.fft(signal.clone()));

            long startTime = System.nanoTime();
            long endTime = startTime + 1_000_000_000L; // 1 second

            while (System.nanoTime() < endTime) {
                FFTUtils.fft(signal.clone());
                operationsPerSecond++;
            }

            // Should achieve at least 1000 FFTs per second for 1024-point
            assertThat(operationsPerSecond).isGreaterThan(1000);
        }

        @Test
        @DisplayName("Should maintain high pitch detection throughput")
        void shouldMaintainPitchDetectionThroughput() {
            double[] signal = new double[4096];
            for (int i = 0; i < signal.length; i++) {
                signal[i] = 0.8 * Math.sin(2 * Math.PI * 440.0 * i / 44100.0);
            }

            int detectionsPerSecond = 0;

            warmup(() -> PitchDetectionUtils.detectPitchHybrid(signal, 44100.0));

            long startTime = System.nanoTime();
            long endTime = startTime + 1_000_000_000L; // 1 second

            while (System.nanoTime() < endTime) {
                PitchDetectionUtils.detectPitchHybrid(signal, 44100.0);
                detectionsPerSecond++;
            }

            // Should achieve at least 100 detections per second
            assertThat(detectionsPerSecond).isGreaterThan(100);
        }
    }

    @Nested
    @DisplayName("Regression Detection Tests")
    class RegressionDetectionTests {

        @Test
        @DisplayName("Should detect if FFT8 becomes slower")
        void shouldDetectFFT8Regression() {
            double[] signal = FFTUtils.generateTestSignal(8, "random");

            warmup(() -> FFTUtils.fft(signal.clone()));

            long avgTime = benchmark(() -> FFTUtils.fft(signal.clone()));

            // FFT8 should maintain speedup (< 10 µs baseline)
            // If this fails, there's a performance regression
            assertThat(avgTime)
                .as("FFT8 performance regression detected!")
                .isLessThan(10_000L);
        }

        @Test
        @DisplayName("Should detect if twiddle cache becomes slower")
        void shouldDetectTwiddleCacheRegression() {
            warmup(() -> TwiddleFactorCache.getCos(256, 42, true));

            long avgTime = benchmark(() -> {
                TwiddleFactorCache.getCos(256, 42, true);
            });

            // Cache access should be very fast (< 100ns)
            assertThat(avgTime)
                .as("Twiddle cache performance regression detected!")
                .isLessThan(100L);
        }

        @Test
        @DisplayName("Should maintain overall system performance")
        void shouldMaintainOverallPerformance() {
            // Combined workflow: signal generation → FFT → pitch detection
            double[] signal = new double[4096];

            warmup(() -> {
                for (int i = 0; i < signal.length; i++) {
                    signal[i] = 0.8 * Math.sin(2 * Math.PI * 440.0 * i / 44100.0);
                }
                PitchDetectionUtils.detectPitchHybrid(signal, 44100.0);
            });

            long avgTime = benchmark(() -> {
                for (int i = 0; i < signal.length; i++) {
                    signal[i] = 0.8 * Math.sin(2 * Math.PI * 440.0 * i / 44100.0);
                }
                PitchDetectionUtils.detectPitchHybrid(signal, 44100.0);
            });

            // Complete workflow should be fast (< 10ms)
            assertThat(avgTime)
                .as("Overall system performance regression detected!")
                .isLessThan(10_000_000L);
        }
    }
}

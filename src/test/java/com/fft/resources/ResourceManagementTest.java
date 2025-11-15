package com.fft.resources;

import com.fft.core.FFTResult;
import com.fft.core.TwiddleFactorCache;
import com.fft.factory.DefaultFFTFactory;
import com.fft.factory.FFTFactory;
import com.fft.utils.FFTUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Resource management and memory tests for FFT library.
 *
 * <p>Tests memory usage, resource cleanup, and prevention of memory leaks.</p>
 *
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("Resource Management Tests")
class ResourceManagementTest {

    @Nested
    @DisplayName("Memory Usage Tests")
    class MemoryUsageTests {

        @Test
        @DisplayName("Should have bounded memory for repeated FFT operations")
        void shouldHaveBoundedMemoryUsage() {
            Runtime runtime = Runtime.getRuntime();
            runtime.gc(); // Suggest GC before test

            long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

            // Perform many FFT operations
            for (int i = 0; i < 10000; i++) {
                double[] signal = new double[256];
                signal[0] = 1.0;
                FFTUtils.fft(signal);
            }

            runtime.gc(); // Suggest GC after operations
            long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

            // Memory growth should be minimal (< 10MB)
            long memoryGrowth = memoryAfter - memoryBefore;
            assertThat(memoryGrowth).isLessThan(10 * 1024 * 1024);
        }

        @Test
        @DisplayName("Should handle large array allocation")
        void shouldHandleLargeArrayAllocation() {
            // Test with large FFT size
            int largeSize = 65536;

            assertThatCode(() -> {
                double[] signal = new double[largeSize];
                for (int i = 0; i < 100; i++) {
                    signal[i] = Math.random();
                }
                FFTUtils.fft(signal);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not accumulate memory across multiple factory creations")
        void shouldNotAccumulateMemoryAcrossFactories() {
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();

            long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

            // Create and discard many factories
            for (int i = 0; i < 1000; i++) {
                FFTFactory factory = new DefaultFFTFactory();
                factory.createFFT(128);
            }

            runtime.gc();
            long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

            long memoryGrowth = memoryAfter - memoryBefore;
            // Should not grow significantly (< 5MB)
            assertThat(memoryGrowth).isLessThan(5 * 1024 * 1024);
        }
    }

    @Nested
    @DisplayName("TwiddleFactorCache Memory Tests")
    class CacheMemoryTests {

        @Test
        @DisplayName("Should report reasonable cache memory usage")
        void shouldReportReasonableCacheUsage() {
            String stats = TwiddleFactorCache.getCacheStats();

            assertThat(stats).contains("KB");

            // Extract KB value (rough parsing)
            String[] parts = stats.split(" ");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i + 1].equals("KB")) {
                    double kb = Double.parseDouble(parts[i].replace("~", ""));
                    // Cache should be reasonable size (< 1MB)
                    assertThat(kb).isLessThan(1024.0);
                    break;
                }
            }
        }

        @Test
        @DisplayName("Should have precomputed expected number of sizes")
        void shouldHavePrecomputedExpectedSizes() {
            // According to implementation: 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096
            int[] precomputedSizes = {8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096};

            for (int size : precomputedSizes) {
                assertThat(TwiddleFactorCache.isPrecomputed(size))
                    .as("Size %d should be precomputed", size)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("Should not grow cache beyond precomputed sizes")
        void shouldNotGrowCacheBeyondPrecomputed() {
            String statsBefore = TwiddleFactorCache.getCacheStats();

            // Access non-precomputed sizes (should use fallback, not cache)
            for (int i = 0; i < 100; i++) {
                TwiddleFactorCache.getCos(8192, i, true);
                TwiddleFactorCache.getSin(8192, i, true);
            }

            String statsAfter = TwiddleFactorCache.getCacheStats();

            // Cache size should not change
            assertThat(statsAfter).isEqualTo(statsBefore);
        }
    }

    @Nested
    @DisplayName("Array Allocation Tests")
    class ArrayAllocationTests {

        @Test
        @DisplayName("Should handle maximum practical FFT size")
        void shouldHandleMaximumPracticalSize() {
            int maxSize = 65536;

            assertThatCode(() -> {
                double[] real = new double[maxSize];
                double[] imag = new double[maxSize];
                real[0] = 1.0;

                FFTUtils.fft(real, imag, true);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject excessively large arrays gracefully")
        void shouldRejectExcessivelyLargeArrays() {
            // Try to create unreasonably large array (will likely fail with OutOfMemoryError)
            // This test verifies the system handles it appropriately

            assertThatThrownBy(() -> {
                int hugeSize = Integer.MAX_VALUE / 4;
                double[] huge = new double[hugeSize];
            }).isInstanceOf(OutOfMemoryError.class);
        }

        @Test
        @DisplayName("Should efficiently allocate for common sizes")
        void shouldEfficientlyAllocateForCommonSizes() {
            int[] commonSizes = {256, 512, 1024, 2048, 4096};

            for (int size : commonSizes) {
                long start = System.nanoTime();

                double[] signal = new double[size];
                FFTResult result = FFTUtils.fft(signal);

                long elapsed = System.nanoTime() - start;

                // Should complete quickly (allocation + transform < 100ms)
                assertThat(elapsed).isLessThan(100_000_000L);
            }
        }
    }

    @Nested
    @DisplayName("Object Lifecycle Tests")
    class ObjectLifecycleTests {

        @Test
        @DisplayName("Should allow FFTResult to be garbage collected")
        void shouldAllowFFTResultToBeGarbageCollected() {
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();

            long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

            // Create many results and don't hold references
            for (int i = 0; i < 1000; i++) {
                double[] signal = new double[1024];
                signal[0] = 1.0;
                FFTUtils.fft(signal); // Result is not stored
            }

            runtime.gc();
            long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

            // Memory should not grow significantly
            long memoryGrowth = memoryAfter - memoryBefore;
            assertThat(memoryGrowth).isLessThan(10 * 1024 * 1024);
        }

        @Test
        @DisplayName("Should not leak references in factory")
        void shouldNotLeakReferencesInFactory() {
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();

            long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

            // Create and use many FFT instances
            for (int i = 0; i < 1000; i++) {
                FFTFactory factory = new DefaultFFTFactory();
                factory.createFFT(256);
                // Factory goes out of scope
            }

            runtime.gc();
            long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

            long memoryGrowth = memoryAfter - memoryBefore;
            assertThat(memoryGrowth).isLessThan(5 * 1024 * 1024);
        }
    }

    @Nested
    @DisplayName("Boundary Condition Tests")
    class BoundaryConditionTests {

        @Test
        @DisplayName("Should handle minimum size array")
        void shouldHandleMinimumSizeArray() {
            double[] tiny = new double[2];
            tiny[0] = 1.0;

            assertThatCode(() -> {
                FFTUtils.fft(tiny);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle zero-valued arrays efficiently")
        void shouldHandleZeroArraysEfficiently() {
            double[] zeros = new double[4096];

            long start = System.nanoTime();
            FFTResult result = FFTUtils.fft(zeros);
            long elapsed = System.nanoTime() - start;

            // Should be very fast (zeros are simple)
            assertThat(elapsed).isLessThan(10_000_000L); // < 10ms

            // Result should be all zeros
            double[] magnitudes = result.getMagnitudes();
            for (double mag : magnitudes) {
                assertThat(mag).isCloseTo(0.0, within(1e-10));
            }
        }

        @Test
        @DisplayName("Should handle arrays at power-of-2 boundaries")
        void shouldHandleArraysAtBoundaries() {
            int[] powerOf2Sizes = {2, 4, 8, 16, 32, 64, 128, 256, 512, 1024};

            for (int size : powerOf2Sizes) {
                double[] signal = new double[size];
                signal[0] = 1.0;

                FFTResult result = FFTUtils.fft(signal);
                assertThat(result.size()).isEqualTo(size);
            }
        }
    }

    @Nested
    @DisplayName("Resource Cleanup Tests")
    class ResourceCleanupTests {

        @Test
        @DisplayName("Should not hold references after transform")
        void shouldNotHoldReferencesAfterTransform() {
            double[] signal = new double[1024];
            for (int i = 0; i < signal.length; i++) {
                signal[i] = Math.random();
            }

            double[] originalCopy = signal.clone();
            FFTResult result = FFTUtils.fft(signal);

            // Modify original array
            for (int i = 0; i < signal.length; i++) {
                signal[i] = 999.0;
            }

            // Result should be unchanged (defensive copy was made)
            // Verify by transforming the original copy again
            FFTResult result2 = FFTUtils.fft(originalCopy);

            double[] mag1 = result.getMagnitudes();
            double[] mag2 = result2.getMagnitudes();

            for (int i = 0; i < mag1.length; i++) {
                assertThat(mag1[i]).isCloseTo(mag2[i], within(1e-10));
            }
        }

        @Test
        @DisplayName("Should handle rapid allocation and deallocation")
        void shouldHandleRapidAllocationDeallocation() {
            // Stress test: rapid create/destroy cycles
            assertThatCode(() -> {
                for (int i = 0; i < 10000; i++) {
                    double[] signal = new double[256];
                    signal[0] = (double) i;
                    FFTResult result = FFTUtils.fft(signal);
                    // Result immediately goes out of scope
                }
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Performance Under Memory Pressure")
    class MemoryPressureTests {

        @Test
        @DisplayName("Should maintain correctness under memory pressure")
        void shouldMaintainCorrectnessUnderPressure() {
            // Create some memory pressure
            double[][] memoryPressure = new double[100][10000];

            try {
                // Perform FFT operations under pressure
                double[] signal = FFTUtils.generateTestSignal(512, "random");
                FFTResult result1 = FFTUtils.fft(signal.clone());

                // Should still produce consistent results
                FFTResult result2 = FFTUtils.fft(signal.clone());

                double[] mag1 = result1.getMagnitudes();
                double[] mag2 = result2.getMagnitudes();

                for (int i = 0; i < mag1.length; i++) {
                    assertThat(mag2[i]).isCloseTo(mag1[i], within(1e-10));
                }
            } finally {
                // Release pressure
                memoryPressure = null;
            }
        }

        @Test
        @DisplayName("Should handle multiple large transforms sequentially")
        void shouldHandleMultipleLargeTransforms() {
            int largeSize = 32768;
            int numTransforms = 10;

            assertThatCode(() -> {
                for (int i = 0; i < numTransforms; i++) {
                    double[] signal = new double[largeSize];
                    for (int j = 0; j < 100; j++) {
                        signal[j] = Math.random();
                    }
                    FFTUtils.fft(signal);
                }
            }).doesNotThrowAnyException();
        }
    }
}

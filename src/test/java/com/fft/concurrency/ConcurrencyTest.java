package com.fft.concurrency;

import com.fft.core.FFT;
import com.fft.core.FFTResult;
import com.fft.core.TwiddleFactorCache;
import com.fft.factory.DefaultFFTFactory;
import com.fft.factory.FFTFactory;
import com.fft.utils.FFTUtils;
import com.fft.utils.PitchDetectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Thread safety and concurrency tests for FFT library components.
 *
 * <p>
 * Tests concurrent access to factory, cache, and FFT operations to ensure
 * thread-safe operation in multi-threaded environments.
 * </p>
 *
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("Concurrency Tests")
class ConcurrencyTest {

    private static final int NUM_THREADS = 10;
    private static final int ITERATIONS_PER_THREAD = 50;
    private static final double TOLERANCE = 1e-10;

    @Nested
    @DisplayName("Factory Concurrency Tests")
    class FactoryConcurrencyTests {

        @Test
        @DisplayName("Should handle concurrent factory access")
        void shouldHandleConcurrentFactoryAccess() throws InterruptedException, ExecutionException {
            FFTFactory factory = new DefaultFFTFactory();
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            List<Future<FFT>> futures = new ArrayList<>();

            // Submit multiple tasks to create FFT instances concurrently
            for (int i = 0; i < NUM_THREADS; i++) {
                // Use bit-shifting to ensure power-of-2 sizes: 32, 64, 128, 256, 512
                final int size = 32 << (i % 5); // 32, 64, 128, 256, 512
                futures.add(executor.submit(() -> factory.createFFT(size)));
            }

            // Wait for all tasks to complete
            for (Future<FFT> future : futures) {
                FFT fft = future.get();
                assertThat(fft).isNotNull();
            }

            // Properly shutdown executor
            executor.shutdown();
            boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
            assertThat(terminated).as("Executor should terminate within timeout").isTrue();
        }

        @Test
        @DisplayName("Should handle concurrent factory creation")
        void shouldHandleConcurrentFactoryCreation() throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            CountDownLatch latch = new CountDownLatch(NUM_THREADS);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 0; i < NUM_THREADS; i++) {
                executor.submit(() -> {
                    try {
                        FFTFactory factory = FFTUtils.createFactory();
                        if (factory != null) {
                            successCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completed).as("All threads should complete within timeout").isTrue();
            assertThat(successCount.get()).isEqualTo(NUM_THREADS);
        }

        @Test
        @DisplayName("Should handle concurrent getImplementationInfo calls")
        void shouldHandleConcurrentImplementationInfoCalls() throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            CountDownLatch latch = new CountDownLatch(NUM_THREADS);
            List<String> results = new CopyOnWriteArrayList<>();

            for (int i = 0; i < NUM_THREADS; i++) {
                final int size = 128;
                executor.submit(() -> {
                    try {
                        String info = FFTUtils.getImplementationInfo(size);
                        results.add(info);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completed).as("All threads should complete within timeout").isTrue();
            assertThat(results).hasSize(NUM_THREADS);
            // All results should be identical for the same size
            assertThat(results).allMatch(info -> info.equals(results.get(0)));
        }
    }

    @Nested
    @DisplayName("TwiddleFactorCache Concurrency Tests")
    class TwiddleFactorCacheConcurrencyTests {

        @Test
        @DisplayName("Should handle concurrent cache reads")
        void shouldHandleConcurrentCacheReads() throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            CountDownLatch latch = new CountDownLatch(NUM_THREADS);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 0; i < NUM_THREADS; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                            int size = 128;
                            int k = (threadId * ITERATIONS_PER_THREAD + j) % size;

                            double cos = TwiddleFactorCache.getCos(size, k, true);
                            double sin = TwiddleFactorCache.getSin(size, k, true);

                            // Verify values are correct
                            double expectedCos = Math.cos(-2.0 * Math.PI * k / size);
                            double expectedSin = Math.sin(-2.0 * Math.PI * k / size);

                            if (Math.abs(cos - expectedCos) < TOLERANCE &&
                                    Math.abs(sin - expectedSin) < TOLERANCE) {
                                successCount.incrementAndGet();
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(successCount.get()).isEqualTo(NUM_THREADS * ITERATIONS_PER_THREAD);
        }

        @Test
        @DisplayName("Should handle concurrent isPrecomputed checks")
        void shouldHandleConcurrentIsPrecomputedChecks() throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            CountDownLatch latch = new CountDownLatch(NUM_THREADS);
            List<Boolean> results = new CopyOnWriteArrayList<>();

            for (int i = 0; i < NUM_THREADS; i++) {
                executor.submit(() -> {
                    try {
                        boolean result = TwiddleFactorCache.isPrecomputed(256);
                        results.add(result);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            // All results should be true and consistent
            assertThat(results).hasSize(NUM_THREADS);
            assertThat(results).allMatch(result -> result == true);
        }

        @Test
        @DisplayName("Should handle concurrent getCacheStats calls")
        void shouldHandleConcurrentGetCacheStats() throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            CountDownLatch latch = new CountDownLatch(NUM_THREADS);
            List<String> stats = new CopyOnWriteArrayList<>();

            for (int i = 0; i < NUM_THREADS; i++) {
                executor.submit(() -> {
                    try {
                        String stat = TwiddleFactorCache.getCacheStats();
                        stats.add(stat);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(stats).hasSize(NUM_THREADS);
            // All stats should be identical (cache is immutable after init)
            assertThat(stats).allMatch(stat -> stat.equals(stats.get(0)));
        }
    }

    @Nested
    @DisplayName("FFT Transform Concurrency Tests")
    class FFTTransformConcurrencyTests {

        @Test
        @DisplayName("Should handle concurrent FFT transforms")
        void shouldHandleConcurrentFFTTransforms() throws InterruptedException, ExecutionException {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            List<Future<FFTResult>> futures = new ArrayList<>();

            for (int i = 0; i < NUM_THREADS; i++) {
                final int threadId = i;
                futures.add(executor.submit(() -> {
                    double[] signal = new double[256];
                    for (int j = 0; j < signal.length; j++) {
                        signal[j] = Math.sin(2 * Math.PI * (threadId + 1) * j / signal.length);
                    }
                    return FFTUtils.fft(signal);
                }));
            }

            // Verify all transforms completed successfully
            for (Future<FFTResult> future : futures) {
                FFTResult result = future.get();
                assertThat(result).isNotNull();
                assertThat(result.size()).isEqualTo(256);
            }

            executor.shutdown();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        @Test
        @DisplayName("Should produce consistent results under concurrent load")
        void shouldProduceConsistentResults() throws InterruptedException, ExecutionException {
            double[] signal = FFTUtils.generateTestSignal(512, "random");
            FFTResult expected = FFTUtils.fft(signal);

            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            List<Future<FFTResult>> futures = new ArrayList<>();

            // All threads transform the same signal
            for (int i = 0; i < NUM_THREADS; i++) {
                futures.add(executor.submit(() -> FFTUtils.fft(signal.clone())));
            }

            // Verify all results match
            for (Future<FFTResult> future : futures) {
                FFTResult result = future.get();
                double[] resultReal = result.getRealParts();
                double[] expectedReal = expected.getRealParts();

                assertThat(resultReal).hasSize(expectedReal.length);
                for (int j = 0; j < resultReal.length; j++) {
                    assertThat(resultReal[j]).isCloseTo(expectedReal[j], within(TOLERANCE));
                }
            }

            executor.shutdown();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Nested
    @DisplayName("FFTResult Immutability Tests")
    class FFTResultImmutabilityTests {

        @Test
        @DisplayName("Should maintain immutability under concurrent access")
        void shouldMaintainImmutability() throws InterruptedException {
            double[] signal = FFTUtils.generateTestSignal(256, "random");
            FFTResult result = FFTUtils.fft(signal);

            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            CountDownLatch latch = new CountDownLatch(NUM_THREADS);
            List<double[]> realParts = new CopyOnWriteArrayList<>();

            for (int i = 0; i < NUM_THREADS; i++) {
                executor.submit(() -> {
                    try {
                        // Multiple threads reading from same FFTResult
                        double[] real = result.getRealParts();
                        realParts.add(real);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            // Verify all threads got identical copies
            assertThat(realParts).hasSize(NUM_THREADS);
            double[] first = realParts.get(0);
            for (double[] real : realParts) {
                assertThat(real).containsExactly(first);
            }
        }

        @Test
        @DisplayName("Should not be affected by external modifications")
        void shouldNotBeAffectedByExternalModifications() throws InterruptedException {
            double[] signal = FFTUtils.generateTestSignal(256, "random");
            FFTResult result = FFTUtils.fft(signal);
            double[] originalReal = result.getRealParts();

            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            CountDownLatch latch = new CountDownLatch(NUM_THREADS);

            for (int i = 0; i < NUM_THREADS; i++) {
                executor.submit(() -> {
                    try {
                        // Attempt to modify returned arrays
                        double[] real = result.getRealParts();
                        for (int j = 0; j < real.length; j++) {
                            real[j] = 999.0;
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            // Original result should be unchanged
            double[] afterReal = result.getRealParts();
            assertThat(afterReal).containsExactly(originalReal);
        }
    }

    @Nested
    @DisplayName("PitchDetectionUtils Concurrency Tests")
    class PitchDetectionConcurrencyTests {

        @Test
        @DisplayName("Should handle concurrent pitch detection")
        void shouldHandleConcurrentPitchDetection() throws InterruptedException, ExecutionException {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            List<Future<PitchDetectionUtils.PitchResult>> futures = new ArrayList<>();

            for (int i = 0; i < NUM_THREADS; i++) {
                final double frequency = 440.0 + (i * 10);
                futures.add(executor.submit(() -> {
                    double[] signal = new double[4096];
                    for (int j = 0; j < signal.length; j++) {
                        signal[j] = 0.8 * Math.sin(2 * Math.PI * frequency * j / 44100.0);
                    }
                    return PitchDetectionUtils.detectPitchHybrid(signal, 44100.0);
                }));
            }

            // Verify all detections completed
            for (Future<PitchDetectionUtils.PitchResult> future : futures) {
                PitchDetectionUtils.PitchResult result = future.get();
                assertThat(result).isNotNull();
            }

            executor.shutdown();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        @Test
        @DisplayName("Should handle concurrent cache access in pitch detection")
        void shouldHandleConcurrentCacheAccess() throws InterruptedException {
            // Generate same signal to trigger cache
            double[] signal = new double[4096];
            for (int i = 0; i < signal.length; i++) {
                signal[i] = 0.8 * Math.sin(2 * Math.PI * 440.0 * i / 44100.0);
            }

            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            CountDownLatch latch = new CountDownLatch(NUM_THREADS);
            List<PitchDetectionUtils.PitchResult> results = new CopyOnWriteArrayList<>();

            for (int i = 0; i < NUM_THREADS; i++) {
                executor.submit(() -> {
                    try {
                        PitchDetectionUtils.PitchResult result = PitchDetectionUtils.detectPitchHybrid(signal.clone(),
                                44100.0);
                        results.add(result);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(results).hasSize(NUM_THREADS);
            // All results should be similar (same input signal)
            double firstFreq = results.get(0).frequency;
            for (PitchDetectionUtils.PitchResult result : results) {
                assertThat(result.frequency).isCloseTo(firstFreq, within(10.0));
            }
        }
    }

    @Nested
    @DisplayName("Race Condition Tests")
    class RaceConditionTests {

        @Test
        @DisplayName("Should not have race conditions in factory registration")
        void shouldNotHaveRaceConditionsInRegistration() throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            CountDownLatch latch = new CountDownLatch(NUM_THREADS);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 0; i < NUM_THREADS; i++) {
                executor.submit(() -> {
                    try {
                        FFTFactory factory = new DefaultFFTFactory();
                        int count = factory.getSupportedSizes().size();
                        if (count > 0) {
                            successCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(successCount.get()).isEqualTo(NUM_THREADS);
        }

        @Test
        @DisplayName("Should handle concurrent FFT operations on different sizes")
        void shouldHandleConcurrentDifferentSizes() throws InterruptedException, ExecutionException {
            int[] sizes = { 64, 128, 256, 512, 1024 };
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            List<Future<Boolean>> futures = new ArrayList<>();

            for (int i = 0; i < NUM_THREADS; i++) {
                final int size = sizes[i % sizes.length];
                futures.add(executor.submit(() -> {
                    double[] signal = FFTUtils.generateTestSignal(size, "random");
                    FFTResult result = FFTUtils.fft(signal);
                    return result.size() == size;
                }));
            }

            // Verify all transforms completed correctly
            for (Future<Boolean> future : futures) {
                assertThat(future.get()).isTrue();
            }

            executor.shutdown();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Nested
    @DisplayName("Stress Tests")
    class StressTests {

        @Test
        @DisplayName("Should handle high concurrent load")
        void shouldHandleHighConcurrentLoad() throws InterruptedException {
            final int HIGH_THREAD_COUNT = 50;
            final int HIGH_ITERATIONS = 100;

            ExecutorService executor = Executors.newFixedThreadPool(HIGH_THREAD_COUNT);
            CountDownLatch latch = new CountDownLatch(HIGH_THREAD_COUNT);
            AtomicInteger completedOperations = new AtomicInteger(0);

            for (int i = 0; i < HIGH_THREAD_COUNT; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < HIGH_ITERATIONS; j++) {
                            double[] signal = new double[128];
                            for (int k = 0; k < signal.length; k++) {
                                signal[k] = Math.random();
                            }
                            FFTUtils.fft(signal);
                            completedOperations.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completedOperations.get()).isEqualTo(HIGH_THREAD_COUNT * HIGH_ITERATIONS);
        }
    }
}

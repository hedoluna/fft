package com.fft.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test and benchmark for butterfly operations optimization.
 *
 * RED PHASE: Establish baseline performance of current vs optimized approaches.
 */
@DisplayName("Butterfly Operations Optimization Tests")
public class ButterflyOptimizationTest {

    private static final int WARMUP_ITERATIONS = 10000;
    private static final int MEASUREMENT_ITERATIONS = 20000;
    private static final int SIZE = 32;

    /**
     * Setup for butterfly benchmark state
     */
    static class ButterflyMeasurement {
        double[] xReal;
        double[] xImag;
        double c;
        double s;
        int n;
        int n2;

        void setup() {
            n = SIZE;
            n2 = SIZE / 4;  // Simulate stage 1 butterfly
            xReal = new double[SIZE];
            xImag = new double[SIZE];

            // Initialize with test data
            for (int i = 0; i < SIZE; i++) {
                xReal[i] = Math.sin(i * 0.1);
                xImag[i] = Math.cos(i * 0.1);
            }

            // Precompute twiddle factors
            c = 0.9238795325;  // cos(PI/8)
            s = 0.3826834324;  // sin(PI/8)
        }
    }

    /**
     * Current butterfly implementation: 6 array accesses per butterfly
     */
    private static void butterflyOperations_Current(ButterflyMeasurement state) {
        double[] xReal = state.xReal;
        double[] xImag = state.xImag;
        double c = state.c;
        double s = state.s;
        int n2 = state.n2;

        for (int k = 0; k < SIZE - n2; k += 2) {
            // Current: xReal[k+n2] accessed 3 times, xImag[k+n2] accessed 3 times
            double tReal = xReal[k + n2] * c + xImag[k + n2] * s;
            double tImag = xImag[k + n2] * c - xReal[k + n2] * s;
            xReal[k + n2] = xReal[k] - tReal;
            xImag[k + n2] = xImag[k] - tImag;
            xReal[k] += tReal;
            xImag[k] += tImag;
        }
    }

    /**
     * Optimized butterfly implementation: 4 array accesses (cache in locals)
     */
    private static void butterflyOperations_Optimized(ButterflyMeasurement state) {
        double[] xReal = state.xReal;
        double[] xImag = state.xImag;
        double c = state.c;
        double s = state.s;
        int n2 = state.n2;

        for (int k = 0; k < SIZE - n2; k += 2) {
            // Optimized: Cache array values in locals (4 array accesses)
            double xr_k = xReal[k];
            double xi_k = xImag[k];
            double xr_kn2 = xReal[k + n2];
            double xi_kn2 = xImag[k + n2];

            // Compute using cached values
            double tReal = xr_kn2 * c + xi_kn2 * s;
            double tImag = xi_kn2 * c - xr_kn2 * s;

            // Store results
            xReal[k] = xr_k + tReal;
            xImag[k] = xi_k + tImag;
            xReal[k + n2] = xr_k - tReal;
            xImag[k + n2] = xi_k - tImag;
        }
    }

    /**
     * RED PHASE: Measure current implementation performance
     */
    @Test
    @DisplayName("Baseline: Current butterfly implementation")
    public void testCurrentButterflyPerformance() {
        ButterflyMeasurement state = new ButterflyMeasurement();
        state.setup();

        // Warmup phase
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            state.setup();
            butterflyOperations_Current(state);
        }

        // Measurement phase
        long startTime = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            state.setup();
            butterflyOperations_Current(state);
        }
        long endTime = System.nanoTime();

        long totalNs = endTime - startTime;
        double avgNs = (double) totalNs / MEASUREMENT_ITERATIONS;

        System.out.printf("=== Current Butterfly Implementation ===%n");
        System.out.printf("Total Time: %,d ns%n", totalNs);
        System.out.printf("Average per iteration: %.2f ns%n", avgNs);
        System.out.printf("Operations per iteration: %d%n", SIZE / 2);
        System.out.printf("Per-operation time: %.4f ns%n", avgNs / (SIZE / 2));
    }

    /**
     * RED PHASE: Measure optimized implementation performance
     */
    @Test
    @DisplayName("Optimized: Cached array access butterfly implementation")
    public void testOptimizedButterflyPerformance() {
        ButterflyMeasurement state = new ButterflyMeasurement();
        state.setup();

        // Warmup phase
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            state.setup();
            butterflyOperations_Optimized(state);
        }

        // Measurement phase
        long startTime = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            state.setup();
            butterflyOperations_Optimized(state);
        }
        long endTime = System.nanoTime();

        long totalNs = endTime - startTime;
        double avgNs = (double) totalNs / MEASUREMENT_ITERATIONS;

        System.out.printf("=== Optimized Butterfly Implementation ===%n");
        System.out.printf("Total Time: %,d ns%n", totalNs);
        System.out.printf("Average per iteration: %.2f ns%n", avgNs);
        System.out.printf("Operations per iteration: %d%n", SIZE / 2);
        System.out.printf("Per-operation time: %.4f ns%n", avgNs / (SIZE / 2));
    }

    /**
     * RED PHASE: Compare the two implementations
     */
    @Test
    @DisplayName("Compare current vs optimized butterfly implementations")
    public void testButterflyComparison() {
        ButterflyMeasurement state = new ButterflyMeasurement();

        // Measure current
        state.setup();
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            butterflyOperations_Current(state);
        }

        long startCurrent = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            state.setup();
            butterflyOperations_Current(state);
        }
        long endCurrent = System.nanoTime();
        double currentTime = (double) (endCurrent - startCurrent) / MEASUREMENT_ITERATIONS;

        // Measure optimized
        state.setup();
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            butterflyOperations_Optimized(state);
        }

        long startOptimized = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            state.setup();
            butterflyOperations_Optimized(state);
        }
        long endOptimized = System.nanoTime();
        double optimizedTime = (double) (endOptimized - startOptimized) / MEASUREMENT_ITERATIONS;

        // Calculate speedup
        double speedup = currentTime / optimizedTime;
        double improvement = (speedup - 1) * 100;

        System.out.printf("%n=== BUTTERFLY OPTIMIZATION COMPARISON ===%n");
        System.out.printf("Current:   %.2f ns/iter%n", currentTime);
        System.out.printf("Optimized: %.2f ns/iter%n", optimizedTime);
        System.out.printf("Speedup:   %.2f x%n", speedup);
        System.out.printf("Improvement: %.1f%%%n", improvement);

        if (speedup > 1.0) {
            System.out.printf("✅ OPTIMIZED VERSION IS FASTER%n");
        } else {
            System.out.printf("⚠️  Current version is still faster (optimization may not be beneficial)%n");
        }
    }

    /**
     * GREEN PHASE: Verify correctness of optimized implementation
     */
    @Test
    @DisplayName("Verify optimized butterfly produces same results as current")
    public void testButterflyCorrectness() {
        ButterflyMeasurement currentState = new ButterflyMeasurement();
        currentState.setup();

        ButterflyMeasurement optimizedState = new ButterflyMeasurement();
        optimizedState.setup();

        // Make copies so arrays have identical initial state
        System.arraycopy(currentState.xReal, 0, optimizedState.xReal, 0, SIZE);
        System.arraycopy(currentState.xImag, 0, optimizedState.xImag, 0, SIZE);

        // Run both implementations
        butterflyOperations_Current(currentState);
        butterflyOperations_Optimized(optimizedState);

        // Verify results match within floating-point precision
        double tolerance = 1e-15;
        for (int i = 0; i < SIZE; i++) {
            assertEquals(currentState.xReal[i], optimizedState.xReal[i], tolerance,
                    "Real value mismatch at index " + i);
            assertEquals(currentState.xImag[i], optimizedState.xImag[i], tolerance,
                    "Imaginary value mismatch at index " + i);
        }

        System.out.printf("✅ Correctness verified: Optimized produces identical results to current%n");
    }
}

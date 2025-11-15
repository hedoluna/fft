package com.fft.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for TwiddleFactorCache.
 *
 * <p>Tests the critical twiddle factor precomputation system that provides
 * 30-50% overall FFT speedup by caching cos/sin calculations.</p>
 *
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("Twiddle Factor Cache Tests")
class TwiddleFactorCacheTest {

    private static final double EPSILON = 1e-10;

    // Precomputed sizes according to TwiddleFactorCache implementation
    private static final int[] PRECOMPUTED_SIZES = {8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096};

    @Test
    @DisplayName("Should precompute twiddle factors for common sizes")
    void testPrecomputedSizes() {
        for (int size : PRECOMPUTED_SIZES) {
            assertThat(TwiddleFactorCache.isPrecomputed(size))
                .as("Size %d should be precomputed", size)
                .isTrue();
        }
    }

    @Test
    @DisplayName("Should not precompute uncommon sizes")
    void testNonPrecomputedSizes() {
        int[] nonPrecomputedSizes = {8192, 16384, 32768, 65536};

        for (int size : nonPrecomputedSizes) {
            assertThat(TwiddleFactorCache.isPrecomputed(size))
                .as("Size %d should not be precomputed", size)
                .isFalse();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096})
    @DisplayName("Should compute accurate cosine values for cached sizes")
    void testCachedCosineAccuracy(int size) {
        for (int k = 0; k < size; k++) {
            double cachedCos = TwiddleFactorCache.getCos(size, k, true);
            double expectedCos = Math.cos(-2.0 * Math.PI * k / size);

            assertThat(cachedCos)
                .as("Cached cos for size=%d, k=%d", size, k)
                .isCloseTo(expectedCos, within(EPSILON));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096})
    @DisplayName("Should compute accurate sine values for cached sizes")
    void testCachedSineAccuracy(int size) {
        for (int k = 0; k < size; k++) {
            double cachedSin = TwiddleFactorCache.getSin(size, k, true);
            double expectedSin = Math.sin(-2.0 * Math.PI * k / size);

            assertThat(cachedSin)
                .as("Cached sin for size=%d, k=%d", size, k)
                .isCloseTo(expectedSin, within(EPSILON));
        }
    }

    @Test
    @DisplayName("Should compute accurate values for non-cached sizes (fallback)")
    void testFallbackAccuracy() {
        int size = 8192; // Not in precomputed list

        for (int k = 0; k < 10; k++) {
            double fallbackCos = TwiddleFactorCache.getCos(size, k, true);
            double expectedCos = Math.cos(-2.0 * Math.PI * k / size);

            assertThat(fallbackCos)
                .as("Fallback cos for size=%d, k=%d", size, k)
                .isCloseTo(expectedCos, within(EPSILON));

            double fallbackSin = TwiddleFactorCache.getSin(size, k, true);
            double expectedSin = Math.sin(-2.0 * Math.PI * k / size);

            assertThat(fallbackSin)
                .as("Fallback sin for size=%d, k=%d", size, k)
                .isCloseTo(expectedSin, within(EPSILON));
        }
    }

    @Test
    @DisplayName("Should handle forward transform twiddle factors")
    void testForwardTransform() {
        int size = 64;
        int k = 7;

        double cos = TwiddleFactorCache.getCos(size, k, true);
        double sin = TwiddleFactorCache.getSin(size, k, true);

        // Forward transform: W_N^k = e^(-2πik/N) = cos(-2πk/N) - i*sin(-2πk/N)
        double expectedCos = Math.cos(-2.0 * Math.PI * k / size);
        double expectedSin = Math.sin(-2.0 * Math.PI * k / size);

        assertThat(cos).isCloseTo(expectedCos, within(EPSILON));
        assertThat(sin).isCloseTo(expectedSin, within(EPSILON));
    }

    @Test
    @DisplayName("Should handle inverse transform twiddle factors")
    void testInverseTransform() {
        int size = 64;
        int k = 7;

        double cos = TwiddleFactorCache.getCos(size, k, false);
        double sin = TwiddleFactorCache.getSin(size, k, false);

        // Inverse transform: W_N^k = e^(2πik/N) = cos(2πk/N) + i*sin(2πk/N)
        double expectedCos = Math.cos(2.0 * Math.PI * k / size);
        double expectedSin = Math.sin(2.0 * Math.PI * k / size);

        assertThat(cos).isCloseTo(expectedCos, within(EPSILON));
        assertThat(sin).isCloseTo(expectedSin, within(EPSILON));
    }

    @Test
    @DisplayName("Should handle getTwiddle method")
    void testGetTwiddleMethod() {
        int size = 128;
        double p = 15.0;

        double[] twiddle = TwiddleFactorCache.getTwiddle(size, p, true);

        assertThat(twiddle).hasSize(2);

        int k = (int) p;
        double expectedCos = Math.cos(-2.0 * Math.PI * k / size);
        double expectedSin = Math.sin(-2.0 * Math.PI * k / size);

        assertThat(twiddle[0]).isCloseTo(expectedCos, within(EPSILON));
        assertThat(twiddle[1]).isCloseTo(expectedSin, within(EPSILON));
    }

    @Test
    @DisplayName("Should handle k=0 (DC component)")
    void testDCComponent() {
        int size = 256;

        double cos = TwiddleFactorCache.getCos(size, 0, true);
        double sin = TwiddleFactorCache.getSin(size, 0, true);

        // W_N^0 = 1 + 0i
        assertThat(cos).isCloseTo(1.0, within(EPSILON));
        assertThat(sin).isCloseTo(0.0, within(EPSILON));
    }

    @Test
    @DisplayName("Should handle k=N/2 (Nyquist component)")
    void testNyquistComponent() {
        int size = 256;
        int k = size / 2;

        double cos = TwiddleFactorCache.getCos(size, k, true);
        double sin = TwiddleFactorCache.getSin(size, k, true);

        // W_N^(N/2) = e^(-iπ) = -1 + 0i
        assertThat(cos).isCloseTo(-1.0, within(EPSILON));
        assertThat(sin).isCloseTo(0.0, within(EPSILON));
    }

    @Test
    @DisplayName("Should handle k=N/4 (quarter frequency)")
    void testQuarterFrequency() {
        int size = 256;
        int k = size / 4;

        double cos = TwiddleFactorCache.getCos(size, k, true);
        double sin = TwiddleFactorCache.getSin(size, k, true);

        // W_N^(N/4) = e^(-iπ/2) = 0 - i
        assertThat(cos).isCloseTo(0.0, within(EPSILON));
        assertThat(sin).isCloseTo(-1.0, within(EPSILON));
    }

    @Test
    @DisplayName("Should verify cache statistics")
    void testCacheStats() {
        String stats = TwiddleFactorCache.getCacheStats();

        assertThat(stats)
            .isNotNull()
            .contains("TwiddleFactorCache")
            .contains("sizes cached")
            .contains("twiddle factors")
            .contains("KB");

        // Verify it mentions the correct number of cached sizes
        assertThat(stats).contains(String.valueOf(PRECOMPUTED_SIZES.length));
    }

    @Test
    @DisplayName("Should maintain periodicity (k mod N)")
    void testPeriodicity() {
        int size = 64;
        int k = 7;

        // Test that twiddle factors are periodic: W_N^k = W_N^(k+N)
        double cos1 = TwiddleFactorCache.getCos(size, k, true);
        double cos2 = TwiddleFactorCache.getCos(size, k + size, true);

        double sin1 = TwiddleFactorCache.getSin(size, k, true);
        double sin2 = TwiddleFactorCache.getSin(size, k + size, true);

        assertThat(cos2).isCloseTo(cos1, within(EPSILON));
        assertThat(sin2).isCloseTo(sin1, within(EPSILON));
    }

    @Test
    @DisplayName("Should handle symmetry properties")
    void testSymmetry() {
        int size = 128;
        int k = 17;

        // Test conjugate symmetry: W_N^(-k) = conj(W_N^k)
        double cosK = TwiddleFactorCache.getCos(size, k, true);
        double sinK = TwiddleFactorCache.getSin(size, k, true);

        double cosNegK = TwiddleFactorCache.getCos(size, size - k, true);
        double sinNegK = TwiddleFactorCache.getSin(size, size - k, true);

        // W_N^(-k) = W_N^(N-k) should be conjugate of W_N^k
        assertThat(cosNegK).isCloseTo(cosK, within(EPSILON));
        assertThat(sinNegK).isCloseTo(-sinK, within(EPSILON));
    }

    @Test
    @DisplayName("Should handle edge case: very small k")
    void testVerySmallK() {
        int size = 1024;

        for (int k = 0; k < 5; k++) {
            double cos = TwiddleFactorCache.getCos(size, k, true);
            double sin = TwiddleFactorCache.getSin(size, k, true);

            double expectedCos = Math.cos(-2.0 * Math.PI * k / size);
            double expectedSin = Math.sin(-2.0 * Math.PI * k / size);

            assertThat(cos).isCloseTo(expectedCos, within(EPSILON));
            assertThat(sin).isCloseTo(expectedSin, within(EPSILON));
        }
    }

    @Test
    @DisplayName("Should handle edge case: k near N")
    void testKNearN() {
        int size = 1024;

        for (int k = size - 5; k < size; k++) {
            double cos = TwiddleFactorCache.getCos(size, k, true);
            double sin = TwiddleFactorCache.getSin(size, k, true);

            double expectedCos = Math.cos(-2.0 * Math.PI * k / size);
            double expectedSin = Math.sin(-2.0 * Math.PI * k / size);

            assertThat(cos).isCloseTo(expectedCos, within(EPSILON));
            assertThat(sin).isCloseTo(expectedSin, within(EPSILON));
        }
    }

    @Test
    @DisplayName("Should verify consistency between cached and fallback")
    void testCachedVsFallbackConsistency() {
        // Compare a cached size vs hypothetical non-cached computation
        int cachedSize = 256;
        int k = 42;

        double cachedCos = TwiddleFactorCache.getCos(cachedSize, k, true);
        double cachedSin = TwiddleFactorCache.getSin(cachedSize, k, true);

        // Manual computation (what fallback would do)
        double arg = -2.0 * Math.PI * k / cachedSize;
        double manualCos = Math.cos(arg);
        double manualSin = Math.sin(arg);

        assertThat(cachedCos).isCloseTo(manualCos, within(EPSILON));
        assertThat(cachedSin).isCloseTo(manualSin, within(EPSILON));
    }

    @Test
    @DisplayName("Should verify inverse vs forward transform relationship")
    void testInverseVsForwardRelationship() {
        int size = 512;
        int k = 99;

        double forwardCos = TwiddleFactorCache.getCos(size, k, true);
        double forwardSin = TwiddleFactorCache.getSin(size, k, true);

        double inverseCos = TwiddleFactorCache.getCos(size, k, false);
        double inverseSin = TwiddleFactorCache.getSin(size, k, false);

        // Cosine should be the same (even function)
        assertThat(inverseCos).isCloseTo(forwardCos, within(EPSILON));

        // Sine should be negated (odd function)
        assertThat(inverseSin).isCloseTo(-forwardSin, within(EPSILON));
    }

    @Test
    @DisplayName("Should verify multiple calls return consistent results")
    void testConsistencyAcrossMultipleCalls() {
        int size = 128;
        int k = 23;

        // Call multiple times to ensure cache is stable
        double cos1 = TwiddleFactorCache.getCos(size, k, true);
        double cos2 = TwiddleFactorCache.getCos(size, k, true);
        double cos3 = TwiddleFactorCache.getCos(size, k, true);

        assertThat(cos2).isEqualTo(cos1);
        assertThat(cos3).isEqualTo(cos1);

        double sin1 = TwiddleFactorCache.getSin(size, k, true);
        double sin2 = TwiddleFactorCache.getSin(size, k, true);
        double sin3 = TwiddleFactorCache.getSin(size, k, true);

        assertThat(sin2).isEqualTo(sin1);
        assertThat(sin3).isEqualTo(sin1);
    }
}

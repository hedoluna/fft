package com.fft.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for FFTMath utility methods.
 */
class FFTMathTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536})
    @DisplayName("Should recognize valid powers of 2")
    void testIsPowerOfTwo_validPowers(int value) {
        assertThat(FFTMath.isPowerOfTwo(value)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -2, 3, 5, 6, 7, 9, 10, 15, 17, 100, 1000, 1023, Integer.MIN_VALUE})
    @DisplayName("Should reject non-powers of 2")
    void testIsPowerOfTwo_nonPowers(int value) {
        assertThat(FFTMath.isPowerOfTwo(value)).isFalse();
    }

    @Test
    @DisplayName("Should handle edge case: Integer.MAX_VALUE is not power of 2")
    void testIsPowerOfTwo_maxValue() {
        assertThat(FFTMath.isPowerOfTwo(Integer.MAX_VALUE)).isFalse();
    }

    @Test
    @DisplayName("Should handle largest valid power of 2 in int range")
    void testIsPowerOfTwo_largestPowerOfTwo() {
        // 2^30 = 1073741824 is the largest power of 2 fitting in a positive int
        assertThat(FFTMath.isPowerOfTwo(1 << 30)).isTrue();
    }
}

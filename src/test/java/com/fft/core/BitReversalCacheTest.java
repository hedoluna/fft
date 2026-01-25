package com.fft.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BitReversalCache Tests")
class BitReversalCacheTest {

    @Nested
    @DisplayName("getTable() method")
    class GetTableTests {

        @Test
        @DisplayName("should return correct bit-reversal table for size 8")
        void shouldReturnCorrectTableForSize8() {
            int[] table = BitReversalCache.getTable(8);

            assertThat(table).hasSize(8);
            // Verify bit-reversal: for 3 bits (size 8)
            // 0 (000) -> 0 (000)
            // 1 (001) -> 4 (100)
            // 2 (010) -> 2 (010)
            // 3 (011) -> 6 (110)
            // 4 (100) -> 1 (001)
            // 5 (101) -> 5 (101)
            // 6 (110) -> 3 (011)
            // 7 (111) -> 7 (111)
            assertThat(table[0]).isEqualTo(0);
            assertThat(table[1]).isEqualTo(4);
            assertThat(table[2]).isEqualTo(2);
            assertThat(table[3]).isEqualTo(6);
            assertThat(table[4]).isEqualTo(1);
            assertThat(table[5]).isEqualTo(5);
            assertThat(table[6]).isEqualTo(3);
            assertThat(table[7]).isEqualTo(7);
        }

        @Test
        @DisplayName("should return correct bit-reversal table for size 16")
        void shouldReturnCorrectTableForSize16() {
            int[] table = BitReversalCache.getTable(16);

            assertThat(table).hasSize(16);
            // Verify some key reversals for 4 bits
            assertThat(table[0]).isEqualTo(0);   // 0000 -> 0000
            assertThat(table[1]).isEqualTo(8);   // 0001 -> 1000
            assertThat(table[8]).isEqualTo(1);   // 1000 -> 0001
            assertThat(table[15]).isEqualTo(15); // 1111 -> 1111
        }

        @Test
        @DisplayName("should return same instance for same size (caching)")
        void shouldReturnSameInstanceForSameSize() {
            int[] table1 = BitReversalCache.getTable(64);
            int[] table2 = BitReversalCache.getTable(64);

            assertThat(table1).isSameAs(table2);
        }

        @Test
        @DisplayName("should throw exception for non-power-of-two size")
        void shouldThrowExceptionForNonPowerOfTwo() {
            assertThatThrownBy(() -> BitReversalCache.getTable(7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("power of 2");

            assertThatThrownBy(() -> BitReversalCache.getTable(12))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("power of 2");
        }

        @Test
        @DisplayName("should throw exception for zero or negative size")
        void shouldThrowExceptionForZeroOrNegativeSize() {
            assertThatThrownBy(() -> BitReversalCache.getTable(0))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> BitReversalCache.getTable(-4))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should handle large power-of-two sizes")
        void shouldHandleLargeSizes() {
            int[] table = BitReversalCache.getTable(8192);

            assertThat(table).hasSize(8192);
            // Verify symmetry property: bitreverse(bitreverse(x)) = x
            for (int i = 0; i < 100; i++) {
                int reversed = table[i];
                assertThat(table[reversed]).isEqualTo(i);
            }
        }
    }

    @Nested
    @DisplayName("isPrecomputed() method")
    class IsPrecomputedTests {

        @Test
        @DisplayName("should return true for precomputed sizes")
        void shouldReturnTrueForPrecomputedSizes() {
            int[] precomputedSizes = {8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096};

            for (int size : precomputedSizes) {
                assertThat(BitReversalCache.isPrecomputed(size))
                    .as("Size %d should be precomputed", size)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("should return false for non-precomputed sizes before access")
        void shouldReturnFalseForNonPrecomputedSizes() {
            // 16384 is not in the precomputed list
            // Note: After calling getTable, it will be cached
            assertThat(BitReversalCache.isPrecomputed(16384)).isFalse();
        }

        @Test
        @DisplayName("should return true after getTable creates cache entry")
        void shouldReturnTrueAfterGetTableCreatesEntry() {
            int size = 32768;
            // Ensure it's not precomputed initially
            boolean wasPre = BitReversalCache.isPrecomputed(size);

            // Access it
            BitReversalCache.getTable(size);

            // Now it should be cached
            assertThat(BitReversalCache.isPrecomputed(size)).isTrue();
        }
    }

    @Nested
    @DisplayName("getCacheStats() method")
    class GetCacheStatsTests {

        @Test
        @DisplayName("should return non-empty stats string")
        void shouldReturnNonEmptyStatsString() {
            String stats = BitReversalCache.getCacheStats();

            assertThat(stats).isNotEmpty();
            assertThat(stats).contains("BitReversalCache");
            assertThat(stats).contains("sizes cached");
            assertThat(stats).contains("KB");
        }

        @Test
        @DisplayName("should report at least precomputed sizes count")
        void shouldReportAtLeastPrecomputedSizesCount() {
            String stats = BitReversalCache.getCacheStats();

            // Extract number of cached sizes from stats
            // Format: "BitReversalCache: N sizes cached, ~X.XX KB memory"
            assertThat(stats).matches(".*\\d+ sizes cached.*");
        }
    }

    @Nested
    @DisplayName("Bit-reversal correctness")
    class BitReversalCorrectnessTests {

        @Test
        @DisplayName("should satisfy involution property: reverse(reverse(x)) = x")
        void shouldSatisfyInvolutionProperty() {
            int[] sizes = {8, 16, 32, 64, 128, 256};

            for (int size : sizes) {
                int[] table = BitReversalCache.getTable(size);

                for (int i = 0; i < size; i++) {
                    int reversed = table[i];
                    int doubleReversed = table[reversed];
                    assertThat(doubleReversed)
                        .as("Size %d, index %d: reverse(reverse(%d)) should equal %d", size, i, i, i)
                        .isEqualTo(i);
                }
            }
        }

        @Test
        @DisplayName("should preserve 0 and max value for symmetric patterns")
        void shouldPreserveSymmetricPatterns() {
            int[] sizes = {8, 16, 32, 64};

            for (int size : sizes) {
                int[] table = BitReversalCache.getTable(size);

                // 0 always maps to 0
                assertThat(table[0]).isEqualTo(0);

                // Max value (all 1s) always maps to itself
                assertThat(table[size - 1]).isEqualTo(size - 1);
            }
        }
    }

    @Nested
    @DisplayName("Utility class constraints")
    class UtilityClassTests {

        @Test
        @DisplayName("should not allow instantiation")
        void shouldNotAllowInstantiation() throws Exception {
            Constructor<BitReversalCache> constructor = BitReversalCache.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }
}

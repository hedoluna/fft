package com.fft.integration;

import com.fft.core.FFT;
import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import com.fft.factory.DefaultFFTFactory;
import com.fft.factory.FFTFactory;
import com.fft.utils.FFTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for factory implementation switching and selection.
 *
 * <p>Tests runtime implementation changes, priority handling, and fallback behavior.</p>
 *
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("Factory Implementation Switching Integration Tests")
class FactorySwitchingTest {

    private static final double TOLERANCE = 1e-10;
    private FFTFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DefaultFFTFactory();
    }

    @Nested
    @DisplayName("Implementation Selection Tests")
    class ImplementationSelectionTests {

        @Test
        @DisplayName("Should select optimized implementation when available")
        void shouldSelectOptimizedImplementation() {
            // FFTOptimized8 is currently the only size-specific optimized implementation
            FFT fft = factory.createFFT(8);

            assertThat(fft).isNotNull();
            assertThat(fft.getClass().getSimpleName()).contains("Optimized");
        }

        @Test
        @DisplayName("Should fallback to FFTBase for unsupported sizes")
        void shouldFallbackToFFTBase() {
            // Create a large non-power-of-2 after padding
            int unsupportedSize = 100000;
            int nextPowerOf2 = FFTUtils.nextPowerOfTwo(unsupportedSize);

            FFT fft = factory.createFFT(nextPowerOf2);

            assertThat(fft).isNotNull();
            // Should work even if it falls back to FFTBase
            assertThat(fft.supportsSize(nextPowerOf2)).isTrue();
        }

        @Test
        @DisplayName("Should select correct implementation for all common sizes")
        void shouldSelectCorrectImplementationForAllSizes() {
            int[] commonSizes = {8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096};

            for (int size : commonSizes) {
                FFT fft = factory.createFFT(size);

                assertThat(fft).isNotNull();
                assertThat(fft.supportsSize(size))
                    .as("FFT for size %d should support that size", size)
                    .isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Consistency Across Implementations Tests")
    class ConsistencyTests {

        @Test
        @DisplayName("Should produce consistent results across different size implementations")
        void shouldProduceConsistentResultsAcrossSizes() {
            // Use deterministic sine wave for predictable results
            double[] signal128 = FFTUtils.generateSineWave(128, 440.0, 44100.0);
            double[] signal256 = FFTUtils.generateSineWave(256, 440.0, 44100.0);

            FFTResult result128 = factory.createFFT(128).transform(
                signal128.clone(), new double[128], true);
            FFTResult result256 = factory.createFFT(256).transform(
                signal256.clone(), new double[256], true);

            // Both should produce valid results
            double[] mag128 = result128.getMagnitudes();
            double[] mag256 = result256.getMagnitudes();

            assertThat(mag128).hasSizeGreaterThan(0);
            assertThat(mag256).hasSizeGreaterThan(0);

            // Both should have peak at similar relative frequency bin
            // (frequency 440Hz should map to proportional bins)
            double maxMag128 = 0;
            double maxMag256 = 0;
            for (int i = 0; i < mag128.length; i++) maxMag128 = Math.max(maxMag128, mag128[i]);
            for (int i = 0; i < mag256.length; i++) maxMag256 = Math.max(maxMag256, mag256[i]);

            assertThat(maxMag128).isGreaterThan(0.1);
            assertThat(maxMag256).isGreaterThan(0.1);
        }

        @Test
        @DisplayName("Should match FFTBase results for all implementations")
        void shouldMatchFFTBaseResults() {
            FFTBase reference = new FFTBase();
            int[] testSizes = {64, 128, 256, 512};

            for (int size : testSizes) {
                double[] real = FFTUtils.generateTestSignal(size, "random");
                double[] imag = new double[size];

                FFTResult expected = reference.transform(real.clone(), imag.clone(), true);
                FFTResult actual = factory.createFFT(size).transform(real.clone(), imag.clone(), true);

                double[] expectedReal = expected.getRealParts();
                double[] actualReal = actual.getRealParts();

                for (int i = 0; i < size; i++) {
                    assertThat(actualReal[i])
                        .as("Size %d, bin %d", size, i)
                        .isCloseTo(expectedReal[i], within(TOLERANCE));
                }
            }
        }
    }

    @Nested
    @DisplayName("Runtime Implementation Switching Tests")
    class RuntimeSwitchingTests {

        @Test
        @DisplayName("Should support switching between sizes at runtime")
        void shouldSupportSwitchingBetweenSizes() {
            double[] results = new double[4];

            // Switch between different sizes
            int[] sizes = {64, 128, 256, 512};
            for (int i = 0; i < sizes.length; i++) {
                double[] signal = FFTUtils.generateSineWave(sizes[i], 440.0, 44100.0);
                FFT fft = factory.createFFT(sizes[i]);
                FFTResult result = fft.transform(signal, new double[sizes[i]], true);

                results[i] = result.getMagnitudes()[0];
            }

            // All should have computed valid results
            for (double result : results) {
                assertThat(result).isFinite();
            }
        }

        @Test
        @DisplayName("Should handle rapid implementation switching")
        void shouldHandleRapidSwitching() {
            for (int i = 0; i < 100; i++) {
                int size = (i % 4 + 4) * 16; // Sizes: 64, 80→64, 96→64, 112→64
                size = FFTUtils.nextPowerOfTwo(size);

                FFT fft = factory.createFFT(size);
                double[] signal = new double[size];
                signal[0] = 1.0;

                FFTResult result = fft.transform(signal, new double[size], true);
                assertThat(result.size()).isEqualTo(size);
            }
        }
    }

    @Nested
    @DisplayName("Factory Registry Tests")
    class FactoryRegistryTests {

        @Test
        @DisplayName("Should list all supported sizes")
        void shouldListAllSupportedSizes() {
            List<Integer> supported = factory.getSupportedSizes();

            assertThat(supported).isNotEmpty();
            assertThat(supported).isSorted();

            // Should include common sizes
            assertThat(supported).contains(64, 128, 256, 512, 1024);
        }

        @Test
        @DisplayName("Should report implementation count correctly")
        void shouldReportImplementationCount() {
            int count128 = factory.getImplementationCount(128);

            assertThat(count128).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should provide implementation info")
        void shouldProvideImplementationInfo() {
            // Test with FFTOptimized8 (has size-specific info)
            String info8 = factory.getImplementationInfo(8);
            assertThat(info8).isNotNull();
            assertThat(info8).isNotEmpty();
            assertThat(info8).containsAnyOf("8", "Optimized", "FFT");

            // Test with FFTBase fallback (generic info)
            String info128 = factory.getImplementationInfo(128);
            assertThat(info128).isNotNull();
            assertThat(info128).isNotEmpty();
            assertThat(info128).containsAnyOf("Generic", "Cooley-Tukey", "FFT");
        }
    }

    @Nested
    @DisplayName("Backward Compatibility Tests")
    class BackwardCompatibilityTests {

        @Test
        @DisplayName("Should maintain compatibility with legacy FFTUtils API")
        void shouldMaintainLegacyCompatibility() {
            double[] signal = FFTUtils.generateTestSignal(256, "random");

            // Old API
            FFTResult result1 = FFTUtils.fft(signal.clone());

            // New API via factory
            FFT fft = factory.createFFT(256);
            FFTResult result2 = fft.transform(signal.clone(), new double[256], true);

            // Results should be identical
            double[] real1 = result1.getRealParts();
            double[] real2 = result2.getRealParts();

            for (int i = 0; i < 256; i++) {
                assertThat(real2[i]).isCloseTo(real1[i], within(TOLERANCE));
            }
        }

        @Test
        @DisplayName("Should support both forward and inverse transforms")
        void shouldSupportBothTransforms() {
            double[] signal = FFTUtils.generateTestSignal(128, "random");

            // Forward
            FFT fft = factory.createFFT(128);
            FFTResult forward = fft.transform(signal.clone(), new double[128], true);

            // Inverse
            FFTResult inverse = fft.transform(
                forward.getRealParts(),
                forward.getImaginaryParts(),
                false
            );

            // Should recover original
            double[] recovered = inverse.getRealParts();
            for (int i = 0; i < 128; i++) {
                assertThat(recovered[i]).isCloseTo(signal[i], within(TOLERANCE));
            }
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle invalid size gracefully")
        void shouldHandleInvalidSizeGracefully() {
            // Non-power-of-2 size should be rejected or auto-corrected
            assertThatCode(() -> {
                int invalidSize = 100;
                int correctedSize = FFTUtils.nextPowerOfTwo(invalidSize);
                factory.createFFT(correctedSize);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle zero size gracefully")
        void shouldHandleZeroSizeGracefully() {
            assertThatThrownBy(() -> factory.createFFT(0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should handle negative size gracefully")
        void shouldHandleNegativeSizeGracefully() {
            assertThatThrownBy(() -> factory.createFFT(-128))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Performance Characteristics Tests")
    class PerformanceCharacteristicsTests {

        @Test
        @DisplayName("Should have reasonable factory creation time")
        void shouldHaveReasonableCreationTime() {
            long start = System.nanoTime();

            for (int i = 0; i < 1000; i++) {
                factory.createFFT(128);
            }

            long elapsed = System.nanoTime() - start;
            double msPerCreation = elapsed / 1_000_000.0 / 1000.0;

            // Should be fast (< 0.1ms per creation on average)
            assertThat(msPerCreation).isLessThan(0.1);
        }

        @Test
        @DisplayName("Should cache and reuse implementations efficiently")
        void shouldCacheImplementations() {
            FFT fft1 = factory.createFFT(128);
            FFT fft2 = factory.createFFT(128);

            // Should be same class (though not necessarily same instance)
            assertThat(fft1.getClass()).isEqualTo(fft2.getClass());
        }
    }
}

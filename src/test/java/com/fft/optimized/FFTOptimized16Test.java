package com.fft.optimized;

import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@DisplayName("FFT Optimized 16 Implementation Tests")
class FFTOptimized16Test {

    private static final double EPSILON = 1e-10;

    private FFTOptimized16 fft;
    private FFTBase referenceFft;

    @BeforeEach
    void setUp() {
        fft = new FFTOptimized16();
        referenceFft = new FFTBase();
    }

    @Test
    @DisplayName("Should produce same results as reference implementation")
    void testConsistencyWithReference() {
        double[] real = new double[16];
        double[] imag = new double[16];

        for (int i = 0; i < 16; i++) {
            real[i] = Math.sin(2 * Math.PI * i / 16) + 0.25 * Math.cos(4 * Math.PI * i / 16);
            imag[i] = 0.1 * i;
        }

        FFTResult optimizedResult = fft.transform(real, imag, true);
        FFTResult referenceResult = referenceFft.transform(real, imag, true);

        for (int i = 0; i < 16; i++) {
            assertThat(optimizedResult.getRealAt(i)).isCloseTo(referenceResult.getRealAt(i), within(EPSILON));
            assertThat(optimizedResult.getImaginaryAt(i)).isCloseTo(referenceResult.getImaginaryAt(i), within(EPSILON));
        }
    }

    @Test
    @DisplayName("Should perform perfect forward/inverse transform cycle")
    void testForwardInverseCycle() {
        double[] originalReal = new double[16];
        double[] originalImag = new double[16];

        for (int i = 0; i < 16; i++) {
            originalReal[i] = Math.cos(2 * Math.PI * 3 * i / 16);
            originalImag[i] = Math.sin(2 * Math.PI * i / 16) * 0.5;
        }

        FFTResult forward = fft.transform(originalReal, originalImag, true);
        FFTResult inverse = fft.transform(forward.getRealParts(), forward.getImaginaryParts(), false);

        for (int i = 0; i < 16; i++) {
            assertThat(inverse.getRealAt(i)).isCloseTo(originalReal[i], within(EPSILON));
            assertThat(inverse.getImaginaryAt(i)).isCloseTo(originalImag[i], within(EPSILON));
        }
    }

    @Test
    @DisplayName("Should detect dominant bins for a sine wave")
    void testSineWavePeak() {
        double[] real = new double[16];

        for (int i = 0; i < 16; i++) {
            real[i] = Math.sin(2 * Math.PI * 2 * i / 16);
        }

        FFTResult result = fft.transform(real, true);
        double[] magnitudes = result.getMagnitudes();

        assertThat(magnitudes[2]).isGreaterThan(1.0);
        assertThat(magnitudes[14]).isGreaterThan(1.0);
    }

    @Test
    @DisplayName("Should reject incorrect array sizes")
    void testIncorrectSizes() {
        assertThatThrownBy(() -> fft.transform(new double[8], new double[8], true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("length 16");

        assertThatThrownBy(() -> fft.transform(new double[16], new double[8], true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("length 16");
    }

    @Test
    @DisplayName("Should report correct interface information")
    void testInterfaceImplementation() {
        assertThat(fft.getSupportedSize()).isEqualTo(16);
        assertThat(fft.supportsSize(16)).isTrue();
        assertThat(fft.supportsSize(8)).isFalse();
        assertThat(fft.getDescription()).contains("size 16");
    }
}

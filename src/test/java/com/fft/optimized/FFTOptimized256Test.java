package com.fft.optimized;

import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FFTOptimized256 Tests")
class FFTOptimized256Test {

    private FFTOptimized256 fft;
    private FFTBase reference;
    private static final int SIZE = 256;
    private static final double TOLERANCE = 1e-10;

    @BeforeEach
    void setUp() {
        fft = new FFTOptimized256();
        reference = new FFTBase();
    }

    @Test
    @DisplayName("Should return correct supported size")
    void shouldReturnCorrectSupportedSize() {
        assertThat(fft.getSupportedSize()).isEqualTo(SIZE);
        assertThat(fft.supportsSize(SIZE)).isTrue();
        assertThat(fft.supportsSize(128)).isFalse();
        assertThat(fft.supportsSize(512)).isFalse();
    }

    @Test
    @DisplayName("Should correctly transform impulse function")
    void shouldCorrectlyTransformImpulseFunction() {
        double[] real = new double[SIZE];
        double[] imag = new double[SIZE];
        real[0] = 1.0;

        FFTResult result = fft.transform(real, imag, true);
        double[] output = result.getInterleavedResult();

        double expectedValue = 1.0 / Math.sqrt(SIZE);
        for (int i = 0; i < SIZE; i++) {
            assertThat(output[2 * i]).isCloseTo(expectedValue, within(TOLERANCE));
            assertThat(output[2 * i + 1]).isCloseTo(0.0, within(TOLERANCE));
        }
    }

    @Test
    @DisplayName("Should match FFTBase results")
    void shouldMatchFFTBaseResults() {
        double[] real = FFTUtils.generateTestSignal(SIZE, "random");
        double[] imag = new double[SIZE];

        FFTResult expected = reference.transform(real.clone(), imag.clone(), true);
        FFTResult actual = fft.transform(real.clone(), imag.clone(), true);

        double[] expReal = expected.getRealParts();
        double[] expImag = expected.getImaginaryParts();
        double[] actReal = actual.getRealParts();
        double[] actImag = actual.getImaginaryParts();

        for (int i = 0; i < SIZE; i++) {
            assertThat(actReal[i]).isCloseTo(expReal[i], within(TOLERANCE));
            assertThat(actImag[i]).isCloseTo(expImag[i], within(TOLERANCE));
        }
    }
}

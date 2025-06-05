package com.fft.optimized;

import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;
import com.fft.core.FFTBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Test suite for FFTOptimized128.
 */
@DisplayName("FFTOptimized128 Tests")
class FFTOptimized128Test {
    
    private FFTOptimized128 fft;
    private static final int SIZE = 128;
    private static final double TOLERANCE = 1e-10;
    
    @BeforeEach
    void setUp() {
        fft = new FFTOptimized128();
    }
    
    @Test
    @DisplayName("Should return correct supported size")
    void shouldReturnCorrectSupportedSize() {
        assertThat(fft.getSupportedSize()).isEqualTo(SIZE);
        assertThat(fft.supportsSize(SIZE)).isTrue();
        assertThat(fft.supportsSize(64)).isFalse();
        assertThat(fft.supportsSize(256)).isFalse();
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
    @DisplayName("Should correctly transform cosine wave")
    void shouldCorrectlyTransformCosineWave() {
        double[] real = new double[SIZE];
        double[] imag = new double[SIZE];
        
        int k = 16;
        double amplitude = 2.0;
        for (int i = 0; i < SIZE; i++) {
            real[i] = amplitude * Math.cos(2.0 * Math.PI * k * i / SIZE);
        }
        
        FFTResult result = fft.transform(real, imag, true);
        double[] output = result.getInterleavedResult();
        
        double expectedAmplitude = amplitude * Math.sqrt(SIZE) / 2.0;
        
        assertThat(output[2 * k]).isCloseTo(expectedAmplitude, within(TOLERANCE));
        assertThat(output[2 * k + 1]).isCloseTo(0.0, within(TOLERANCE));
        assertThat(output[2 * (SIZE - k)]).isCloseTo(expectedAmplitude, within(TOLERANCE));
        assertThat(output[2 * (SIZE - k) + 1]).isCloseTo(0.0, within(TOLERANCE));
    }
    
    @Test
    @DisplayName("Should preserve energy")
    void shouldPreserveEnergy() {
        double[] real = FFTUtils.generateTestSignal(SIZE, "mixed");
        double[] imag = new double[SIZE];
        
        double inputEnergy = 0;
        for (int i = 0; i < SIZE; i++) {
            inputEnergy += real[i] * real[i] + imag[i] * imag[i];
        }
        
        FFTResult result = fft.transform(real, imag, true);
        double[] output = result.getInterleavedResult();
        
        double outputEnergy = 0;
        for (int i = 0; i < SIZE; i++) {
            outputEnergy += output[2 * i] * output[2 * i] + 
                           output[2 * i + 1] * output[2 * i + 1];
        }
        
        assertThat(outputEnergy).isCloseTo(inputEnergy, within(TOLERANCE));
    }
    @Test
    @DisplayName("Should match FFTBase results")
    void shouldMatchFFTBaseResults() {
        FFTBase reference = new FFTBase();
        double[] real = FFTUtils.generateTestSignal(SIZE, "random");
        double[] imag = new double[SIZE];

        FFTResult expected = reference.transform(real.clone(), imag.clone(), true);
        FFTResult actual = fft.transform(real.clone(), imag.clone(), true);

        double[] expectedReal = expected.getRealParts();
        double[] expectedImag = expected.getImaginaryParts();
        double[] actualReal = actual.getRealParts();
        double[] actualImag = actual.getImaginaryParts();

        for (int i = 0; i < SIZE; i++) {
            assertThat(actualReal[i]).isCloseTo(expectedReal[i], within(TOLERANCE));
            assertThat(actualImag[i]).isCloseTo(expectedImag[i], within(TOLERANCE));
        }
    }
}

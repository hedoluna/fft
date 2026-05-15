package com.fft.optimized;

import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

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

        // Unit-amplitude real sine at bin 2 with orthonormal 1/sqrt(N) scaling
        // yields exact peak magnitude N/(2*sqrt(N)) = sqrt(16)/2 = 2.0 at bins 2 and 14.
        assertThat(magnitudes[2]).isCloseTo(2.0, within(1e-10));
        assertThat(magnitudes[14]).isCloseTo(2.0, within(1e-10));
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

    @Test
    @DisplayName("DC input concentrates energy in bin 0 (orthonormal scaling: sqrt(16)=4.0)")
    void testDcComponent() {
        double[] real = new double[16];
        java.util.Arrays.fill(real, 1.0);

        double[] mag = fft.transform(real, true).getMagnitudes();

        assertThat(mag[0]).isCloseTo(4.0, within(1e-10));
        for (int k = 1; k < 16; k++) {
            assertThat(mag[k]).as("bin %d", k).isCloseTo(0.0, within(1e-10));
        }
    }

    @Test
    @DisplayName("Impulse input yields flat spectrum at 1/sqrt(16)=0.25")
    void testImpulseResponse() {
        double[] real = new double[16];
        real[0] = 1.0;

        double[] mag = fft.transform(real, true).getMagnitudes();

        for (int k = 0; k < 16; k++) {
            assertThat(mag[k]).as("bin %d", k).isCloseTo(0.25, within(1e-10));
        }
    }

    @Test
    @DisplayName("Parseval's theorem holds for orthonormal forward transform")
    void testParsevalEnergyConservation() {
        double[] real = {0.4, -0.8, 1.2, 0.1, -0.5, 0.9, 0.3, -0.7,
                         0.6, -0.2, 0.8, -0.4, 0.5, 0.0, -1.0, 0.2};
        double[] imag = {0.1, 0.3, -0.5, 0.2, -0.1, 0.4, 0.6, -0.3,
                         0.0, 0.5, -0.2, 0.7, -0.4, 0.3, 0.1, -0.6};

        double timeEnergy = 0.0;
        for (int i = 0; i < 16; i++) {
            timeEnergy += real[i] * real[i] + imag[i] * imag[i];
        }

        double[] power = fft.transform(real, imag, true).getPowerSpectrum();
        double freqEnergy = 0.0;
        for (double p : power) freqEnergy += p;

        assertThat(freqEnergy).isCloseTo(timeEnergy, within(1e-10));
    }

    @Test
    @DisplayName("Inverse transform matches FFTBase reference")
    void testInverseConsistencyWithReference() {
        double[] real = new double[16];
        double[] imag = new double[16];
        for (int i = 0; i < 16; i++) {
            real[i] = 0.5 * Math.sin(2 * Math.PI * 3 * i / 16);
            imag[i] = 0.3 * Math.cos(2 * Math.PI * 5 * i / 16);
        }

        FFTResult opt = fft.transform(real, imag, false);
        FFTResult ref = referenceFft.transform(real, imag, false);

        for (int i = 0; i < 16; i++) {
            assertThat(opt.getRealAt(i)).as("real[%d]", i).isCloseTo(ref.getRealAt(i), within(EPSILON));
            assertThat(opt.getImaginaryAt(i)).as("imag[%d]", i).isCloseTo(ref.getImaginaryAt(i), within(EPSILON));
        }
    }

    @Test
    @DisplayName("ThreadLocal scratch buffers isolate concurrent invocations")
    void testConcurrentInvocationIsolation() throws Exception {
        int threadCount = 8;
        int iterations = 200;

        double[] real = new double[16];
        double[] imag = new double[16];
        for (int i = 0; i < 16; i++) {
            real[i] = Math.sin(2 * Math.PI * 4 * i / 16);
            imag[i] = 0.0;
        }
        FFTResult expected = fft.transform(real, imag, true);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int t = 0; t < threadCount; t++) {
                futures.add(pool.submit(() -> {
                    for (int n = 0; n < iterations; n++) {
                        FFTResult r = fft.transform(real, imag, true);
                        for (int i = 0; i < 16; i++) {
                            if (Math.abs(r.getRealAt(i) - expected.getRealAt(i)) > EPSILON) return false;
                            if (Math.abs(r.getImaginaryAt(i) - expected.getImaginaryAt(i)) > EPSILON) return false;
                        }
                    }
                    return true;
                }));
            }
            for (Future<Boolean> f : futures) {
                assertThat(f.get(30, TimeUnit.SECONDS)).isTrue();
            }
        } finally {
            pool.shutdownNow();
        }
    }
}

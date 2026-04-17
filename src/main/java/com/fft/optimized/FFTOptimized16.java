package com.fft.optimized;

import com.fft.core.FFT;
import com.fft.core.FFTResult;
import com.fft.factory.FFTImplementation;

/**
 * Optimized FFT implementation for 16-element arrays.
 *
 * <p>This implementation decomposes the 16-point transform into two 8-point
 * transforms, reusing the optimized FFT8 path and combining the sub-results
 * with precomputed FFT16 twiddle factors.</p>
 */
@FFTImplementation(
    size = 16,
    priority = 45,
    description = "Optimized with FFT8 decomposition for size 16",
    characteristics = {"fft8-decomposition", "precomputed-twiddles", "size-16"}
)
public class FFTOptimized16 implements FFT {

    private static final int SIZE = 16;
    private static final int HALF_SIZE = 8;
    private static final double COMBINE_SCALE = 1.0 / Math.sqrt(2.0);

    private static final double[] W16_COS = {
        1.0,
        0.9238795325112867,
        0.7071067811865476,
        0.38268343236508984,
        0.0,
        -0.3826834323650897,
        -0.7071067811865475,
        -0.9238795325112867
    };

    private static final double[] W16_SIN = {
        0.0,
        0.3826834323650898,
        0.7071067811865475,
        0.9238795325112867,
        1.0,
        0.9238795325112867,
        0.7071067811865476,
        0.3826834323650899
    };

    private final FFTOptimized8 fft8 = new FFTOptimized8();

    @Override
    public FFTResult transform(double[] real, double[] imaginary, boolean forward) {
        if (real.length != SIZE || imaginary.length != SIZE) {
            throw new IllegalArgumentException("Arrays must be of length " + SIZE);
        }

        double[] evenReal = new double[HALF_SIZE];
        double[] evenImag = new double[HALF_SIZE];
        double[] oddReal = new double[HALF_SIZE];
        double[] oddImag = new double[HALF_SIZE];

        for (int i = 0; i < HALF_SIZE; i++) {
            int evenIndex = i << 1;
            evenReal[i] = real[evenIndex];
            evenImag[i] = imaginary[evenIndex];
            oddReal[i] = real[evenIndex + 1];
            oddImag[i] = imaginary[evenIndex + 1];
        }

        FFTResult evenResult = fft8.transform(evenReal, evenImag, forward);
        FFTResult oddResult = fft8.transform(oddReal, oddImag, forward);

        double[] evenOutReal = evenResult.getRealParts();
        double[] evenOutImag = evenResult.getImaginaryParts();
        double[] oddOutReal = oddResult.getRealParts();
        double[] oddOutImag = oddResult.getImaginaryParts();

        double[] result = new double[SIZE * 2];
        double sign = forward ? -1.0 : 1.0;

        for (int k = 0; k < HALF_SIZE; k++) {
            double tReal = W16_COS[k] * oddOutReal[k] + sign * W16_SIN[k] * oddOutImag[k];
            double tImag = W16_COS[k] * oddOutImag[k] - sign * W16_SIN[k] * oddOutReal[k];

            result[2 * k] = (evenOutReal[k] + tReal) * COMBINE_SCALE;
            result[2 * k + 1] = (evenOutImag[k] + tImag) * COMBINE_SCALE;
            result[2 * (k + HALF_SIZE)] = (evenOutReal[k] - tReal) * COMBINE_SCALE;
            result[2 * (k + HALF_SIZE) + 1] = (evenOutImag[k] - tImag) * COMBINE_SCALE;
        }

        return new FFTResult(result);
    }

    @Override
    public FFTResult transform(double[] real, boolean forward) {
        return transform(real, new double[SIZE], forward);
    }

    @Override
    public int getSupportedSize() {
        return SIZE;
    }

    @Override
    public boolean supportsSize(int size) {
        return size == SIZE;
    }

    @Override
    public String getDescription() {
        return "Optimized FFT implementation (size 16 via FFT8 decomposition)";
    }
}

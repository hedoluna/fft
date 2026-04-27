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

    private static final ThreadLocal<double[]> EVEN_BUFFER =
        ThreadLocal.withInitial(() -> new double[HALF_SIZE * 2]);

    private static final ThreadLocal<double[]> ODD_BUFFER =
        ThreadLocal.withInitial(() -> new double[HALF_SIZE * 2]);

    @Override
    public FFTResult transform(double[] real, double[] imaginary, boolean forward) {
        if (real.length != SIZE || imaginary.length != SIZE) {
            throw new IllegalArgumentException("Arrays must be of length " + SIZE);
        }

        double[] even = EVEN_BUFFER.get();
        double[] odd = ODD_BUFFER.get();
        double[] result = new double[SIZE * 2];

        FFTOptimized8.transformToInterleaved(real, imaginary, 0, 2, forward, even);
        FFTOptimized8.transformToInterleaved(real, imaginary, 1, 2, forward, odd);

        double sign = forward ? -1.0 : 1.0;

        for (int k = 0; k < HALF_SIZE; k++) {
            int sourceIndex = k << 1;
            double evenReal = even[sourceIndex];
            double evenImag = even[sourceIndex + 1];
            double oddReal = odd[sourceIndex];
            double oddImag = odd[sourceIndex + 1];
            double tReal = W16_COS[k] * oddReal + sign * W16_SIN[k] * oddImag;
            double tImag = W16_COS[k] * oddImag - sign * W16_SIN[k] * oddReal;

            int lowerIndex = (k + HALF_SIZE) << 1;
            result[sourceIndex] = (evenReal + tReal) * COMBINE_SCALE;
            result[sourceIndex + 1] = (evenImag + tImag) * COMBINE_SCALE;
            result[lowerIndex] = (evenReal - tReal) * COMBINE_SCALE;
            result[lowerIndex + 1] = (evenImag - tImag) * COMBINE_SCALE;
        }

        return FFTResult.fromTrustedArray(result);
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

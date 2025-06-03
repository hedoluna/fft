package com.fft.optimized;

import com.fft.core.FFTBase;

/**
 * Utility class containing optimized FFT implementations without reflection dependencies.
 * Currently all optimized methods delegate to the generic FFTBase implementation.
 */
public class OptimizedFFTUtils {

    /** 8-point FFT fallback */
    public static double[] fft8(double[] inputReal, double[] inputImag, boolean forward) {
        return new FFTBase().transform(inputReal, inputImag, forward).getInterleavedResult();
    }

    public static double[] ifft8(double[] inputReal, double[] inputImag) {
        return new FFTBase().transform(inputReal, inputImag, false).getInterleavedResult();
    }

    /** 16-point FFT fallback */
    public static double[] fft16(double[] inputReal, double[] inputImag, boolean forward) {
        return new FFTBase().transform(inputReal, inputImag, forward).getInterleavedResult();
    }

    public static double[] ifft16(double[] inputReal, double[] inputImag) {
        return new FFTBase().transform(inputReal, inputImag, false).getInterleavedResult();
    }

    /** 32-point FFT fallback */
    public static double[] fft32(double[] inputReal, double[] inputImag, boolean forward) {
        return new FFTBase().transform(inputReal, inputImag, forward).getInterleavedResult();
    }

    public static double[] ifft32(double[] inputReal, double[] inputImag) {
        return new FFTBase().transform(inputReal, inputImag, false).getInterleavedResult();
    }

    /** 64-point FFT fallback */
    public static double[] fft64(double[] inputReal, double[] inputImag, boolean forward) {
        return new FFTBase().transform(inputReal, inputImag, forward).getInterleavedResult();
    }

    public static double[] ifft64(double[] inputReal, double[] inputImag) {
        return new FFTBase().transform(inputReal, inputImag, false).getInterleavedResult();
    }
}
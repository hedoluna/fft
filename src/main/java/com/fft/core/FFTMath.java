package com.fft.core;

/**
 * Shared mathematical utilities for FFT operations.
 *
 * <p>Centralizes common math functions used across the FFT library
 * to avoid duplication and ensure consistent behavior.</p>
 *
 * @since 2.1.0
 */
public final class FFTMath {

    private FFTMath() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Checks if a number is a power of 2.
     *
     * @param n the number to check
     * @return true if n is a positive power of 2, false otherwise
     */
    public static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
}

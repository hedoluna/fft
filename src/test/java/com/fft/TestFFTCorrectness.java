package com.fft;

import com.fft.core.FFT;
import com.fft.core.FFTBase;
import com.fft.core.FFTResult;
import com.fft.factory.DefaultFFTFactory;
import com.fft.optimized.OptimizedFFTUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

@RunWith(Parameterized.class)
public class TestFFTCorrectness {

    private static final double DELTA = 1e-9; // Tolerance for float comparisons
    private static final double LOOSER_DELTA = 1e-6; // Looser tolerance for more complex ops

    private static DefaultFFTFactory factory = new DefaultFFTFactory();
    private final int size;
    private final FFT fftUnderTest;
    private final FFT referenceFFT;
    private final String fftName;

    public TestFFTCorrectness(int size, String fftName, FFT fftUnderTest, FFT referenceFFT) {
        this.size = size;
        this.fftName = fftName;
        this.fftUnderTest = fftUnderTest;
        this.referenceFFT = referenceFFT;
    }

    @Parameters(name = "{index}: size={0}, fft={1}")
    public static Collection<Object[]> data() {
        List<Object[]> params = new ArrayList<>();
        int[] optimizedSizes = {8, 16, 32, 64}; // Sizes with specific FFTOptimizedN classes
        int[] recursiveSizes = {128, 256, 512}; // Sizes that will use fftRecursive or FFTBase via factory

        FFT base = new FFTBase();

        for (int size : optimizedSizes) {
            params.add(new Object[]{size, "Optimized" + size, factory.createFFT(size), base});
        }

        // Test FFTBase itself for a few sizes
        params.add(new Object[]{32, "FFTBase", base, base}); // Compare base to itself as a sanity check
        params.add(new Object[]{128, "FFTBase", base, base});

        // Test fftRecursive directly against FFTBase for sizes that would use it
        for (int size : recursiveSizes) {
            FFT recursiveImpl = new FFT() { // Anonymous class to wrap OptimizedFFTUtils.fftRecursive
                @Override
                public FFTResult transform(double[] real, double[] imaginary, boolean forward) {
                    double[] result = OptimizedFFTUtils.fftRecursive(real.length, real, imaginary, forward);
                    return new FFTResult(result);
                }
                @Override
                public FFTResult transform(double[] real, boolean forward) {
                    return transform(real, new double[real.length], forward);
                }
                @Override
                public int getSupportedSize() { return -1; } // Supports multiple sizes
                @Override
                public boolean supportsSize(int s) { return (s > 0 && (s & (s-1))==0 && s >=8); } // Power of 2, >=8
                @Override
                public String getDescription() { return "OptimizedFFTUtils.fftRecursive wrapper"; }
            };
            params.add(new Object[]{size, "Recursive" + size, recursiveImpl, base});
        }
        return params;
    }

    // --- Helper Methods (static to be accessible by @Parameters) ---

    private static double[] generateImpulse(int size, int impulseAt) {
        double[] signal = new double[size];
        if (impulseAt < size && impulseAt >= 0) {
            signal[impulseAt] = 1.0;
        }
        return signal;
    }

    private static double[] generateDCSignal(int size, double amplitude) {
        double[] signal = new double[size];
        Arrays.fill(signal, amplitude);
        return signal;
    }

    private static double[] generateSinusoid(int size, double freq, double sampleRate, double amplitude) {
        double[] signal = new double[size];
        for (int i = 0; i < size; i++) {
            signal[i] = amplitude * Math.sin(2 * Math.PI * freq * i / sampleRate);
        }
        return signal;
    }

    private static double[] generateMultipleSinusoids(int size, double sampleRate, double amplitude) {
        double[] signal = new double[size];
        if (size == 0) return signal;
        double freq1 = size / 8.0; // Example: N/8 Hz
        double freq2 = size / 4.0; // Example: N/4 Hz
        for (int i = 0; i < size; i++) {
            signal[i] = amplitude * Math.sin(2 * Math.PI * freq1 * i / sampleRate);
            signal[i] += amplitude * Math.sin(2 * Math.PI * freq2 * i / sampleRate);
        }
        return signal;
    }

    private static double[] generateRandomSignal(int size, long seed) {
        Random random = new Random(seed);
        double[] signal = new double[size];
        for (int i = 0; i < size; i++) {
            signal[i] = random.nextGaussian(); // Range approx -3 to 3
        }
        return signal;
    }

    private void assertFFTResultsClose(String message, FFTResult expected, FFTResult actual, double currentDelta) {
        assertEquals(message + ": Size mismatch", expected.size(), actual.size());
        assertArrayEquals(message + ": Real parts differ", expected.getRealParts(), actual.getRealParts(), currentDelta);
        assertArrayEquals(message + ": Imaginary parts differ", expected.getImaginaryParts(), actual.getImaginaryParts(), currentDelta);
    }

    // --- Parameterized Test Cases ---

    @Test
    public void testImpulseSignal() {
        if (size == 0) return; // Skip for size 0 if it could occur
        double[] real = generateImpulse(size, size / 4); // Impulse at N/4
        double[] imag = new double[size];

        FFTResult expectedResult = referenceFFT.transform(real, imag, true);
        FFTResult actualResult = fftUnderTest.transform(real, imag, true);

        // Specific check for FFTOptimized64 suspected error with impulse
        if (fftName.equals("Optimized64") && size == 64 && (size/4) != 0) {
            // Impulse at k0=16 for N=64. X[n] = (1/8)exp(-j*2pi*n*16/64) = (1/8)exp(-j*pi*n/2)
            // X[0]=(1/8)*1; X[1]=(1/8)*exp(-j*pi/2)=(1/8)*(-j); X[2]=(1/8)*exp(-j*pi)=-(1/8)
            System.out.println(String.format("Optimized64 Impulse(16) X[0]: R=%.3f, I=%.3f (Base: R=%.3f, I=%.3f)", actualResult.getRealAt(0), actualResult.getImaginaryAt(0), expectedResult.getRealAt(0), expectedResult.getImaginaryAt(0)));
            System.out.println(String.format("Optimized64 Impulse(16) X[1]: R=%.3f, I=%.3f (Base: R=%.3f, I=%.3f)", actualResult.getRealAt(1), actualResult.getImaginaryAt(1), expectedResult.getRealAt(1), expectedResult.getImaginaryAt(1)));
             // If FFT64 twiddle error exists, this might fail more noticeably
        }

        // Specific check for fftRecursive normalization issue (N=128, 256, 512)
        if (fftName.startsWith("Recursive")) {
            double expectedMagAtNyquist = expectedResult.getMagnitudeAt(size/2);
            double actualMagAtNyquist = actualResult.getMagnitudeAt(size/2);
            System.out.println(String.format("%s Impulse N=%d: Base Mag[N/2]=%.5f, Actual Mag[N/2]=%.5f", fftName, size, expectedMagAtNyquist, actualMagAtNyquist));

            if (Math.abs(expectedMagAtNyquist) > 1e-7) { // Avoid division by zero or near-zero
                 // If base case (e.g. N=64) normalized, and recursive also normalized, actual will be smaller.
                 // Example: N=128, recursive calls fft64 (norm by 1/sqrt(64)), then recursive normalizes by 1/sqrt(128)
                 // Effective factor is 1/(sqrt(64)*sqrt(128)) vs expected 1/sqrt(128). So actual = expected / sqrt(64)
                 double expectedFactor = 1.0;
                 if (size == 128) expectedFactor = Math.sqrt(64); // Calls fft64
                 if (size == 256) expectedFactor = Math.sqrt(64); // Calls fft64 via radix-4 path
                 if (size == 512) expectedFactor = Math.sqrt(64); // Calls fft64 via radix-4 path

                 if (expectedFactor > 1.0 && Math.abs(actualMagAtNyquist * expectedFactor - expectedMagAtNyquist) < LOOSER_DELTA * expectedMagAtNyquist) {
                     fail(String.format("Normalization BUG LIKELY in %s for N=%d. Actual result is too small by a factor of sqrt(64). ActualMag: %.5e, ExpectedMag: %.5e", fftName, size, actualMagAtNyquist, expectedMagAtNyquist));
                 }
            }
        }
        assertFFTResultsClose(fftName + " Impulse N=" + size, expectedResult, actualResult, LOOSER_DELTA);
    }

    @Test
    public void testDCSignal() {
        if (size == 0) return;
        double[] real = generateDCSignal(size, 1.0);
        double[] imag = new double[size];

        FFTResult expectedResult = referenceFFT.transform(real, imag, true);
        FFTResult actualResult = fftUnderTest.transform(real, imag, true);
        assertFFTResultsClose(fftName + " DC N=" + size, expectedResult, actualResult, DELTA);
    }

    @Test
    public void testSingleSinusoid() {
        if (size == 0) return;
        double[] real = generateSinusoid(size, size / 8.0, size, 1.0); // Freq = N/8 Hz, so k=N/8 bin
        double[] imag = new double[size];

        FFTResult expectedResult = referenceFFT.transform(real, imag, true);
        FFTResult actualResult = fftUnderTest.transform(real, imag, true);
        assertFFTResultsClose(fftName + " SingleSinusoid N=" + size, expectedResult, actualResult, LOOSER_DELTA);
    }

    @Test
    public void testMultipleSinusoids() {
        if (size == 0) return;
        double[] real = generateMultipleSinusoids(size, size, 1.0);
        double[] imag = new double[size];

        FFTResult expectedResult = referenceFFT.transform(real, imag, true);
        FFTResult actualResult = fftUnderTest.transform(real, imag, true);
        assertFFTResultsClose(fftName + " MultiSinusoid N=" + size, expectedResult, actualResult, LOOSER_DELTA);
    }

    @Test
    public void testRandomSignal() {
        if (size == 0) return;
        double[] real = generateRandomSignal(size, (long)size * 10 + 1);
        double[] imag = generateRandomSignal(size, (long)size * 10 + 2);

        FFTResult expectedResult = referenceFFT.transform(real, imag, true);
        FFTResult actualResult = fftUnderTest.transform(real, imag, true);
        assertFFTResultsClose(fftName + " Random N=" + size, expectedResult, actualResult, LOOSER_DELTA);
    }

    @Test
    public void testReconstruction() {
        if (size == 0) return;
        double[] originalReal = generateRandomSignal(size, (long)size * 20 + 1);
        double[] originalImag = generateRandomSignal(size, (long)size * 20 + 2);

        FFTResult forwardResult = fftUnderTest.transform(originalReal, originalImag, true);
        FFTResult reconstructedResult = fftUnderTest.transform(forwardResult.getRealParts(), forwardResult.getImaginaryParts(), false);

        assertArrayEquals(fftName + " Reconstruction N=" + size + " Real", originalReal, reconstructedResult.getRealParts(), LOOSER_DELTA);
        assertArrayEquals(fftName + " Reconstruction N=" + size + " Imag", originalImag, reconstructedResult.getImaginaryParts(), LOOSER_DELTA);
    }


    // --- Non-Parameterized Tests from previous setup (can be kept or refactored) ---
    // For simplicity, the original non-parameterized tests are removed here, assuming
    // their logic is covered or better handled by the new parameterized tests.
    // If specific non-parameterized scenarios are still needed, they can be added back
    // as static @Test methods within this class (but outside the parameterized scope).

    @Test
    public void testNonPowerOfTwoHandling() {
        // This test is not parameterized by size, so it runs once.
        FFT baseFFT = new FFTBase();
        int nonPowerOfTwoSize = 96;
        double[] real = generateRandomSignal(nonPowerOfTwoSize, 789L);
        double[] imag = new double[nonPowerOfTwoSize];

        try {
            baseFFT.transform(real, imag, true);
            fail("FFTBase should throw for non-power-of-2 size N=96");
        } catch (IllegalArgumentException e) {
            String expectedMessage = "Array length must be a power of 2"; // Adjusted to match actual error
             assertTrue("Unexpected error message from FFTBase for N=96: " + e.getMessage(), e.getMessage().startsWith(expectedMessage));
        }

        try {
            // Using the anonymous wrapper for fftRecursive
            FFT recursiveImpl = new FFT() {
                @Override public FFTResult transform(double[] r, double[] i, boolean f) { return new FFTResult(OptimizedFFTUtils.fftRecursive(r.length, r, i, f)); }
                @Override public FFTResult transform(double[] r, boolean f) { return transform(r, new double[r.length], f); }
                @Override public int getSupportedSize() { return -1; }
                @Override public boolean supportsSize(int s) { return (s > 0 && (s & (s-1))==0 && s >=8); }
                @Override public String getDescription() { return "TestWrapper for fftRecursive"; }
            };
            recursiveImpl.transform(real, imag, true); // This should fail within fftRecursive due to size check or if it reaches the final throw
            fail("fftRecursive should throw for N=96 (non-power-of-2)");
        } catch (IllegalArgumentException e) {
             // fftRecursive itself throws "Size must be a power of two and >= 8"
             // or the sub-calls like fft8, fft16 etc. might throw "Arrays must be of length X" if it somehow gets there.
             // The most specific one from fftRecursive itself is "Size must be a power of two and >= 8"
            String expectedMessage = "Size must be a power of two and >= 8";
             // Or if it's from a sub-call: "Arrays must be of length"
            boolean messageMatches = e.getMessage().equals(expectedMessage) || e.getMessage().startsWith("Arrays must be of length");
            if (!messageMatches && e.getMessage().startsWith("The number of elements is not a power of 2")) {
                 // This can happen if fftRecursive calls fft32/fft64 which then make this check.
                 messageMatches = true;
            }
            assertTrue("Unexpected error message from fftRecursive for N=96: " + e.getMessage(), messageMatches);
        }
    }
}

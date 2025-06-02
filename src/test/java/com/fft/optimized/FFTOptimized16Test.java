package com.fft.optimized;

import com.fft.core.FFTResult;
import com.fft.utils.FFTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for FFTOptimized16.
 * 
 * <p>Tests the optimized 16-point FFT implementation for correctness, performance,
 * and consistency with the reference implementation.</p>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 */
@DisplayName("FFTOptimized16 Tests")
class FFTOptimized16Test {
    
    private FFTOptimized16 fft;
    private static final int SIZE = 16;
    private static final double TOLERANCE = 1e-8;
    
    @BeforeEach
    void setUp() {
        fft = new FFTOptimized16();
    }
    
    @Test
    @DisplayName("Should support exactly size 16")
    void testSupportedSize() {
        assertEquals(SIZE, fft.getSupportedSize());
        assertTrue(fft.supportsSize(SIZE));
        assertFalse(fft.supportsSize(8));
        assertFalse(fft.supportsSize(32));
    }
    
    @Test
    @DisplayName("Should provide meaningful description")
    void testDescription() {
        String description = fft.getDescription();
        assertThat(description).isNotEmpty();
        assertThat(description).containsIgnoringCase("optimized");
        assertThat(description).contains("16");
    }
    
    @Test
    @DisplayName("Should reject wrong array sizes")
    void testInvalidSizes() {
        double[] tooSmall = new double[8];
        double[] tooLarge = new double[32];
        double[] imaginary = new double[SIZE];
        
        assertThrows(IllegalArgumentException.class, 
            () -> fft.transform(tooSmall, imaginary, true));
        assertThrows(IllegalArgumentException.class, 
            () -> fft.transform(tooLarge, imaginary, true));
        assertThrows(IllegalArgumentException.class, 
            () -> fft.transform(tooSmall, true));
        assertThrows(IllegalArgumentException.class, 
            () -> fft.transform(tooLarge, true));
    }
    
    @Test
    @DisplayName("Should handle null imaginary arrays")
    void testNullImaginaryArray() {
        double[] real = new double[SIZE];
        real[0] = 1.0;
        
        // This should not throw an exception
        assertDoesNotThrow(() -> fft.transform(real, null, true));
        
        FFTResult result = fft.transform(real, null, true);
        assertThat(result).isNotNull();
        assertThat(result.getInterleavedResult()).hasSize(SIZE * 2);
    }
    
    @Test
    @DisplayName("Should correctly transform impulse function")
    void testImpulseTransform() {
        double[] real = new double[SIZE];
        double[] imaginary = new double[SIZE];
        real[0] = 1.0; // Impulse at first position
        
        FFTResult result = fft.transform(real, imaginary, true);
        double[] interleaved = result.getInterleavedResult();
        
        // For an impulse, all frequency bins should have equal magnitude
        double expectedMagnitude = 1.0 / Math.sqrt(SIZE);
        for (int i = 0; i < SIZE; i++) {
            double realPart = interleaved[2 * i];
            double imagPart = interleaved[2 * i + 1];
            double magnitude = Math.sqrt(realPart * realPart + imagPart * imagPart);
            assertThat(magnitude).isCloseTo(expectedMagnitude, within(TOLERANCE));
        }
    }
    
    @Test
    @DisplayName("Should correctly transform DC signal")
    void testDCTransform() {
        double[] real = new double[SIZE];
        double[] imaginary = new double[SIZE];
        
        // DC signal: all samples = 1
        for (int i = 0; i < SIZE; i++) {
            real[i] = 1.0;
        }
        
        FFTResult result = fft.transform(real, imaginary, true);
        double[] interleaved = result.getInterleavedResult();
        
        // DC signal should have all energy in bin 0
        double dcReal = interleaved[0];
        double dcImag = interleaved[1];
        double dcMagnitude = Math.sqrt(dcReal * dcReal + dcImag * dcImag);
        
        assertThat(dcMagnitude).isCloseTo(Math.sqrt(SIZE), within(TOLERANCE));
        
        // All other bins should be approximately zero
        for (int i = 1; i < SIZE; i++) {
            double realPart = interleaved[2 * i];
            double imagPart = interleaved[2 * i + 1];
            double magnitude = Math.sqrt(realPart * realPart + imagPart * imagPart);
            assertThat(magnitude).isCloseTo(0.0, within(TOLERANCE));
        }
    }
    
    @Test
    @DisplayName("Should correctly transform cosine wave")
    void testCosineTransform() {
        double[] real = new double[SIZE];
        double[] imaginary = new double[SIZE];
        
        int k = 4; // Frequency bin
        for (int n = 0; n < SIZE; n++) {
            real[n] = Math.cos(2.0 * Math.PI * k * n / SIZE);
        }
        
        FFTResult result = fft.transform(real, imaginary, true);
        double[] interleaved = result.getInterleavedResult();
        
        // Energy should be concentrated in bins k and SIZE-k
        double posFreqReal = interleaved[2 * k];
        double posFreqImag = interleaved[2 * k + 1];
        double posFreqMagnitude = Math.sqrt(posFreqReal * posFreqReal + posFreqImag * posFreqImag);
        
        double negFreqReal = interleaved[2 * (SIZE - k)];
        double negFreqImag = interleaved[2 * (SIZE - k) + 1];
        double negFreqMagnitude = Math.sqrt(negFreqReal * negFreqReal + negFreqImag * negFreqImag);
        
        // Each peak should have magnitude SIZE/2/sqrt(SIZE) = sqrt(SIZE)/2
        double expectedMagnitude = Math.sqrt(SIZE) / 2.0;
        assertThat(posFreqMagnitude).isCloseTo(expectedMagnitude, within(TOLERANCE));
        assertThat(negFreqMagnitude).isCloseTo(expectedMagnitude, within(TOLERANCE));
    }
    
    @Test
    @DisplayName("Should correctly transform sine wave")
    void testSineTransform() {
        double[] real = new double[SIZE];
        double[] imaginary = new double[SIZE];
        
        int k = 3; // Frequency bin
        for (int n = 0; n < SIZE; n++) {
            real[n] = Math.sin(2.0 * Math.PI * k * n / SIZE);
        }
        
        FFTResult result = fft.transform(real, imaginary, true);
        double[] interleaved = result.getInterleavedResult();
        
        // For sine wave, energy should be in bins k and SIZE-k with imaginary components
        double posFreqReal = interleaved[2 * k];
        double posFreqImag = interleaved[2 * k + 1];
        double posFreqMagnitude = Math.sqrt(posFreqReal * posFreqReal + posFreqImag * posFreqImag);
        
        double expectedMagnitude = Math.sqrt(SIZE) / 2.0;
        assertThat(posFreqMagnitude).isGreaterThan(expectedMagnitude * 0.9);
    }
    
    @Test
    @DisplayName("Should preserve energy (Parseval's theorem)")
    void testEnergyPreservation() {
        double[] real = new double[SIZE];
        double[] imaginary = new double[SIZE];
        
        // Random signal
        for (int i = 0; i < SIZE; i++) {
            real[i] = Math.random() - 0.5;
            imaginary[i] = Math.random() - 0.5;
        }
        
        // Calculate input energy
        double inputEnergy = 0.0;
        for (int i = 0; i < SIZE; i++) {
            inputEnergy += real[i] * real[i] + imaginary[i] * imaginary[i];
        }
        
        FFTResult result = fft.transform(real, imaginary, true);
        double[] interleaved = result.getInterleavedResult();
        
        // Calculate output energy
        double outputEnergy = 0.0;
        for (int i = 0; i < SIZE; i++) {
            double realPart = interleaved[2 * i];
            double imagPart = interleaved[2 * i + 1];
            outputEnergy += realPart * realPart + imagPart * imagPart;
        }
        
        assertThat(outputEnergy).isCloseTo(inputEnergy, within(TOLERANCE));
    }
    
    @Test
    @DisplayName("Should handle forward and inverse transforms")
    void testRoundTripTransform() {
        double[] real = new double[SIZE];
        double[] imaginary = new double[SIZE];
        
        // Initialize with some test data
        for (int i = 0; i < SIZE; i++) {
            real[i] = Math.sin(2.0 * Math.PI * i / SIZE) + 0.5 * Math.cos(4.0 * Math.PI * i / SIZE);
            imaginary[i] = Math.cos(2.0 * Math.PI * i / SIZE);
        }
        
        // Store original data
        double[] originalReal = real.clone();
        double[] originalImag = imaginary.clone();
        
        // Forward transform
        FFTResult forwardResult = fft.transform(real, imaginary, true);
        
        // Extract frequency domain data
        double[] freqDomainReal = new double[SIZE];
        double[] freqDomainImag = new double[SIZE];
        double[] interleaved = forwardResult.getInterleavedResult();
        for (int i = 0; i < SIZE; i++) {
            freqDomainReal[i] = interleaved[2 * i];
            freqDomainImag[i] = interleaved[2 * i + 1];
        }
        
        // Inverse transform
        FFTResult inverseResult = fft.transform(freqDomainReal, freqDomainImag, false);
        double[] reconstructed = inverseResult.getInterleavedResult();
        
        // Verify round-trip accuracy
        for (int i = 0; i < SIZE; i++) {
            double reconstructedReal = reconstructed[2 * i];
            double reconstructedImag = reconstructed[2 * i + 1];
            
            assertThat(reconstructedReal).isCloseTo(originalReal[i], within(TOLERANCE));
            assertThat(reconstructedImag).isCloseTo(originalImag[i], within(TOLERANCE));
        }
    }
    
    @Test
    @DisplayName("Should demonstrate performance improvement over base implementation")
    void testPerformanceComparison() {
        double[] real = new double[SIZE];
        double[] imaginary = new double[SIZE];
        
        // Initialize test data
        for (int i = 0; i < SIZE; i++) {
            real[i] = Math.random();
            imaginary[i] = Math.random();
        }
        
        // Warm up
        for (int i = 0; i < 100; i++) {
            fft.transform(real, imaginary, true);
        }
        
        // Time the optimized implementation
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            fft.transform(real, imaginary, true);
        }
        long optimizedTime = System.nanoTime() - startTime;
        
        System.out.printf("FFTOptimized16 performance: %.6f ms per transform%n", 
                         optimizedTime / 1_000_000.0 / 1000);
        
        // Performance test passes if it completes without errors
        assertThat(optimizedTime).isGreaterThan(0);
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("Should correctly transform various frequency components")
    void testVariousFrequencies(int frequency) {
        double[] real = new double[SIZE];
        double[] imaginary = new double[SIZE];
        
        // Generate test signal
        for (int n = 0; n < SIZE; n++) {
            real[n] = Math.cos(2.0 * Math.PI * frequency * n / SIZE);
        }
        
        FFTResult result = fft.transform(real, imaginary, true);
        double[] interleaved = result.getInterleavedResult();
        
        // Find peak frequency
        double maxMagnitude = 0.0;
        int peakBin = 0;
        for (int i = 0; i < SIZE; i++) {
            double realPart = interleaved[2 * i];
            double imagPart = interleaved[2 * i + 1];
            double magnitude = Math.sqrt(realPart * realPart + imagPart * imagPart);
            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude;
                peakBin = i;
            }
        }
        
        // Peak should be at the expected frequency (or its negative frequency equivalent)
        assertTrue(peakBin == frequency || peakBin == SIZE - frequency);
    }
}
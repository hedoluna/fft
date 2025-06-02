package com.fft.core;

import java.util.Arrays;

/**
 * FFT result wrapper class providing convenient access to transform results.
 * 
 * <p>This class encapsulates the results of an FFT operation and provides convenient 
 * methods for extracting different representations of the frequency domain data.
 * The internal storage uses an interleaved format for efficiency, but provides
 * clean accessor methods for different use cases.</p>
 * 
 * <h3>Storage Format:</h3>
 * <p>Internally stores data as interleaved real and imaginary components:
 * [real0, imag0, real1, imag1, real2, imag2, ...]</p>
 * 
 * <h3>Thread Safety:</h3>
 * <p>This class is immutable and thread-safe. All accessor methods return new arrays
 * to prevent external modification of the internal state.</p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * FFTResult result = fft.transform(inputSignal);
 * double[] powerSpectrum = result.getPowerSpectrum();
 * double[] magnitudes = result.getMagnitudes();
 * }</pre>
 * 
 * @author Engine AI Assistant  
 * @since 2.0.0
 * @see FFT for transform operations
 */
public class FFTResult {
    
    private final double[] interleavedResult;
    private final int size;
    
    /**
     * Creates a new FFT result from interleaved real/imaginary data.
     * 
     * @param interleavedResult the interleaved result array [real0, imag0, real1, imag1, ...]
     * @throws IllegalArgumentException if the result array length is not even
     */
    public FFTResult(double[] interleavedResult) {
        if (interleavedResult.length % 2 != 0) {
            throw new IllegalArgumentException("Interleaved result array length must be even");
        }
        // Create defensive copy to ensure immutability
        this.interleavedResult = Arrays.copyOf(interleavedResult, interleavedResult.length);
        this.size = interleavedResult.length / 2;
    }
    
    /**
     * Creates a new FFT result from separate real and imaginary arrays.
     * 
     * @param realParts the real parts array
     * @param imaginaryParts the imaginary parts array
     * @throws IllegalArgumentException if arrays have different lengths
     */
    public FFTResult(double[] realParts, double[] imaginaryParts) {
        if (realParts.length != imaginaryParts.length) {
            throw new IllegalArgumentException("Real and imaginary arrays must have same length");
        }
        
        this.size = realParts.length;
        this.interleavedResult = new double[size * 2];
        
        // Interleave the data
        for (int i = 0; i < size; i++) {
            interleavedResult[2 * i] = realParts[i];
            interleavedResult[2 * i + 1] = imaginaryParts[i];
        }
    }
    
    /**
     * Returns the number of frequency bins in the result.
     * 
     * @return the size (number of complex values)
     */
    public int size() {
        return size;
    }
    
    /**
     * Extracts the real parts from the FFT result.
     * 
     * @return new array containing only the real parts
     */
    public double[] getRealParts() {
        double[] real = new double[size];
        for (int i = 0; i < size; i++) {
            real[i] = interleavedResult[2 * i];
        }
        return real;
    }
    
    /**
     * Extracts the imaginary parts from the FFT result.
     * 
     * @return new array containing only the imaginary parts
     */
    public double[] getImaginaryParts() {
        double[] imaginary = new double[size];
        for (int i = 0; i < size; i++) {
            imaginary[i] = interleavedResult[2 * i + 1];
        }
        return imaginary;
    }
    
    /**
     * Computes the magnitude (absolute value) of each complex frequency bin.
     * 
     * @return array containing the magnitudes
     */
    public double[] getMagnitudes() {
        double[] magnitudes = new double[size];
        for (int i = 0; i < size; i++) {
            double real = interleavedResult[2 * i];
            double imaginary = interleavedResult[2 * i + 1];
            magnitudes[i] = Math.sqrt(real * real + imaginary * imaginary);
        }
        return magnitudes;
    }
    
    /**
     * Computes the phase angles of each complex frequency bin.
     * 
     * @return array containing the phase angles in radians
     */
    public double[] getPhases() {
        double[] phases = new double[size];
        for (int i = 0; i < size; i++) {
            double real = interleavedResult[2 * i];
            double imaginary = interleavedResult[2 * i + 1];
            phases[i] = Math.atan2(imaginary, real);
        }
        return phases;
    }
    
    /**
     * Returns the power spectrum (squared magnitudes).
     * 
     * @return array containing the power spectrum values
     */
    public double[] getPowerSpectrum() {
        double[] power = new double[size];
        for (int i = 0; i < size; i++) {
            double real = interleavedResult[2 * i];
            double imaginary = interleavedResult[2 * i + 1];
            power[i] = real * real + imaginary * imaginary;
        }
        return power;
    }
    
    /**
     * Returns the raw interleaved result array.
     * This is provided for compatibility with legacy code.
     * 
     * @return copy of the interleaved result array [real0, imag0, real1, imag1, ...]
     */
    public double[] getInterleavedResult() {
        return Arrays.copyOf(interleavedResult, interleavedResult.length);
    }
    
    /**
     * Gets the real part at the specified index.
     * 
     * @param index the frequency bin index
     * @return the real part at the given index
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public double getRealAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        return interleavedResult[2 * index];
    }
    
    /**
     * Gets the imaginary part at the specified index.
     * 
     * @param index the frequency bin index
     * @return the imaginary part at the given index
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public double getImaginaryAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        return interleavedResult[2 * index + 1];
    }
    
    /**
     * Gets the magnitude at the specified index.
     * 
     * @param index the frequency bin index
     * @return the magnitude at the given index
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public double getMagnitudeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        double real = interleavedResult[2 * index];
        double imaginary = interleavedResult[2 * index + 1];
        return Math.sqrt(real * real + imaginary * imaginary);
    }
    
    /**
     * Gets the phase at the specified index.
     * 
     * @param index the frequency bin index
     * @return the phase angle in radians at the given index
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public double getPhaseAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        double real = interleavedResult[2 * index];
        double imaginary = interleavedResult[2 * index + 1];
        return Math.atan2(imaginary, real);
    }
    
    @Override
    public String toString() {
        return String.format("FFTResult[size=%d, first_magnitude=%.3f]", 
                           size, 
                           size > 0 ? getMagnitudeAt(0) : 0.0);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FFTResult other = (FFTResult) obj;
        return Arrays.equals(interleavedResult, other.interleavedResult);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(interleavedResult);
    }
}

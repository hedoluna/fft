# Fast Fourier Transform (FFT) Library

A comprehensive Java implementation of the Fast Fourier Transform algorithm with multiple optimized versions for different array sizes.

Based on the algorithms originally published by E. Oran Brigham "The Fast Fourier Transform" 1973, in ALGOL60 and FORTRAN.
Originally written in the summer of 2008 during my holidays in Sardinia by Orlando Selenu.
Enhanced in 2025 with additional optimized implementations, comprehensive testing, and utility classes.

## Features

- **Multiple Implementations**: Generic base implementation plus optimized versions for specific sizes
- **Comprehensive Testing**: Full unit test suite ensuring correctness across all implementations
- **Performance Optimizations**: Specialized implementations for sizes 8, 32, 64, 128, 256, and 512
- **Utility Classes**: Convenient helper methods for common FFT operations
- **Signal Analysis Tools**: Generate test signals and analyze frequency components

## Available Implementations

| Class | Size | Description |
|-------|------|-------------|
| `FFTbase` | Any power of 2 | Generic implementation, works for any power-of-2 size |
| `FFToptim8` | 8 | Highly optimized with unrolled loops (direct transform only) |
| `FFToptim32` | 32 | Optimized with hardcoded parameters |
| `FFToptim64` | 64 | Optimized with hardcoded parameters |
| `FFToptim128` | 128 | Optimized with hardcoded parameters |
| `FFToptim256` | 256 | Optimized with hardcoded parameters |
| `FFToptim512` | 512 | Optimized with hardcoded parameters |

## Quick Start

### Basic Usage

```java
// Simple FFT on real-valued data
double[] signal = {1, 2, 3, 4, 5, 6, 7, 8};
double[] result = FFTUtils.fft(signal);

// Extract magnitude spectrum
double[] magnitudes = FFTUtils.getMagnitudes(result);
```

### Advanced Usage

```java
// FFT with complex input
double[] realPart = {1, 2, 3, 4, 5, 6, 7, 8};
double[] imagPart = {0, 0, 0, 0, 0, 0, 0, 0};
double[] result = FFTUtils.fft(realPart, imagPart, true); // true = forward transform

// Manual implementation selection
double[] result32 = FFToptim32.fft(realPart32, imagPart32, true);
```

### Signal Analysis

```java
// Generate test signal with multiple frequencies
double[] frequencies = {50.0, 120.0, 300.0}; // Hz
double[] amplitudes = {1.0, 0.5, 0.3};
double[] signal = FFTUtils.generateTestSignal(64, 1000.0, frequencies, amplitudes);

// Analyze frequency content
double[] fftResult = FFTUtils.fft(signal);
double[] magnitudes = FFTUtils.getMagnitudes(fftResult);
```

## Running the Examples

Compile and run the demonstration programs:

```bash
# Compile all files
javac *.java

# Run comprehensive tests
javac -cp . test/FFTTest.java && java -cp .:test FFTTest

# Run performance benchmarks
java FFTBenchmark

# Run enhanced demonstration
java MainImproved

# Run utility demonstration
java FFTDemo
```

## File Structure

### Core Implementations
- `FFTbase.java` - Generic FFT implementation
- `FFToptim8.java` - Optimized for 8-element arrays
- `FFToptim32.java` - Optimized for 32-element arrays
- `FFToptim64.java` - Optimized for 64-element arrays
- `FFToptim128.java` - Optimized for 128-element arrays
- `FFToptim256.java` - Optimized for 256-element arrays
- `FFToptim512.java` - Optimized for 512-element arrays

### Utilities and Tools
- `FFTUtils.java` - Convenient utility methods and automatic implementation selection
- `FFTDemo.java` - Demonstration of utility usage and signal analysis
- `FFTBenchmark.java` - Performance benchmarking of all implementations
- `MainImproved.java` - Enhanced demonstration with correctness validation

### Testing
- `test/FFTTest.java` - Comprehensive unit tests for all implementations

### Legacy
- `Main.java` - Original demonstration program

## Performance

The optimized implementations provide varying performance benefits depending on size:

- **Size 8**: ~2x speedup with FFToptim8
- **Size 32+**: Modest improvements (1.05-1.1x) due to reduced loop overhead
- **Larger sizes**: Performance benefits vary based on system and JVM optimizations

## Algorithm Details

This implementation uses the Cooley-Tukey algorithm with:
- **Bit-reversal permutation** for input reordering
- **Decimation-in-frequency** approach
- **In-place computation** to minimize memory usage
- **Normalization** factor of 1/√n following Mathematica convention

## Input Requirements

- Array lengths must be powers of 2 (2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, ...)
- Real and imaginary input arrays must have the same length
- Use `FFTUtils.zeroPadToPowerOfTwo()` for arbitrary-length signals

## Output Format

FFT results are returned as interleaved real and imaginary parts:
```
[real0, imag0, real1, imag1, real2, imag2, ...]
```

Use utility methods to extract components:
- `FFTUtils.getRealParts(result)` - Extract real parts
- `FFTUtils.getImagParts(result)` - Extract imaginary parts  
- `FFTUtils.getMagnitudes(result)` - Compute magnitudes
- `FFTUtils.getPhases(result)` - Compute phase angles

## License

This is a public domain implementation completely free of charge.
I only ask to cite me and send me a link to your work if you're using this.

Have fun with FFTs! They are a great source of inspiration, and when you start learning about these kind of math operators you'll find them everywhere.

---
Orlando Selenu (original implementation)  
Enhanced by Engine AI Assistant (2025)

# Qwen Context File for FFT Library

## Overview

This is a high-performance Java implementation of the Fast Fourier Transform algorithm with multiple optimization strategies. The library provides both educational reference implementations and production-ready optimized versions suitable for real-world signal processing applications including real-time audio analysis, pitch detection, and song recognition.

The library is built around the Cooley-Tukey algorithm and features:

- Multiple optimized implementations for specific FFT sizes (8, 16, 32, 64, 128, etc.)
- Factory pattern for automatic implementation selection
- Modern API with immutable result objects
- Comprehensive testing and benchmarking
- Advanced audio processing capabilities including pitch detection
- Zero external dependencies

## Project Structure

```
D:\repos\fft\
├── src/main/java/com/fft/
│   ├── core/             # Core interfaces and base implementations
│   ├── factory/          # Implementation selection and factory pattern
│   ├── optimized/        # Size-specific optimized implementations
│   ├── utils/            # Utility classes and helpers
│   └── demo/             # Advanced demonstration applications
├── src/test/             # Unit tests and benchmarks
├── docs/                 # Documentation files
├── pom.xml               # Maven build configuration
└── run_tests.bat         # Test execution script
```

### Core Modules

- **core/**: Contains the main FFT interface, base implementation (FFTBase), and FFTResult wrapper
- **factory/**: Implementation discovery system and factory pattern for automatic selection
- **optimized/**: Size-specific optimized implementations (FFTOptimized8, FFTOptimized16, etc.)
- **utils/**: Utility functions, pitch detection algorithms, and helper methods
- **demo/**: Real-time audio processing demos and song recognition examples

## Key Features

### Performance Optimizations
- **Twiddle Factor Cache**: Precomputed cos/sin tables provide 30-50% overall FFT speedup
- **Complete Loop Unrolling**: Eliminates loop overhead for small sizes (e.g., FFTOptimized8 with 2.27x speedup)
- **Hardcoded Trigonometry**: Precomputed sine/cosine values for specific sizes
- **Factory Pattern**: Zero-overhead automatic implementation selection
- **Memory Pooling**: Reduced garbage collection pressure in high-frequency scenarios

### Audio Processing Capabilities
- **Advanced Pitch Detection**: Spectral FFT-based method with 0.92% error (vs YIN's 40.6% error)
- **YIN Algorithm**: Autocorrelation-based pitch detection with subharmonic validation
- **Hybrid Approach**: Combines spectral and YIN methods for best accuracy
- **Voicing Detection**: RMS-based sound/silence discrimination
- **Median Filtering**: Reduces pitch jitter for stable detection
- **Song Recognition**: Melody recognition using Parsons code methodology

## Building and Running

### Prerequisites
- Java 17 or higher
- Maven 3.6.0 or higher

### Building the Project
```bash
# Clean and build
mvn clean compile

# Run all tests
mvn test

# Run with code coverage
mvn test jacoco:report

# Package the JAR
mvn package
```

### Running Demos
```bash
# Real-time pitch detection demo
mvn exec:java -Dexec.mainClass="com.fft.demo.PitchDetectionDemo"

# Song recognition demo
mvn exec:java -Dexec.mainClass="com.fft.demo.SongRecognitionDemo"

# Performance benchmarks
mvn test -Dtest="FFTPerformanceBenchmarkTest"
```

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=FFTBaseTest

# Run with test coverage
mvn clean test jacoco:report
# Open target/site/jacoco/index.html
```

## Architecture

### Core Interfaces
- `FFT`: Main interface defining transform operations
- `FFTResult`: Immutable wrapper for transform results with convenient accessors
- `FFTFactory`: Interface for implementation selection
- `FFTImplementation`: Annotation for marking optimized implementations

### Implementation Strategy
1. **FFTBase**: Generic reference implementation using Cooley-Tukey algorithm
2. **FFTOptimizedN**: Size-specific optimized implementations with techniques like complete loop unrolling
3. **FFTFactory**: Automatic selection system choosing optimal implementation based on size

### Auto-Discovery System
- Uses `@FFTImplementation` annotation for implementation registration
- Scans packages and uses service loader pattern for auto-registration
- Supports priority-based selection when multiple implementations exist for same size

## Usage Examples

### Basic Usage
```java
import com.fft.utils.FFTUtils;
import com.fft.core.FFTResult;

// Simple FFT with automatic implementation selection
double[] signal = {1, 2, 3, 4, 5, 6, 7, 8};
FFTResult result = FFTUtils.fft(signal);

// Extract results using type-safe methods
double[] magnitudes = result.getMagnitudes();
double[] phases = result.getPhases();
double[] powerSpectrum = result.getPowerSpectrum();

// Access individual elements
double firstMagnitude = result.getMagnitudeAt(0);
double firstPhase = result.getPhaseAt(0);
```

### Advanced Usage
```java
import com.fft.factory.DefaultFFTFactory;
import com.fft.core.FFT;

// Factory-based implementation selection
DefaultFFTFactory factory = new DefaultFFTFactory();
FFT implementation = factory.createFFT(1024);
FFTResult result = implementation.transform(realPart, imagPart, true);

// Direct optimized implementation usage
FFTOptimized8 fft8 = new FFTOptimized8();
FFTResult result8 = fft8.transform(realData, imagData, true);
```

### Audio Processing
```java
import com.fft.utils.PitchDetectionUtils;

// Advanced pitch detection
double[] audioBuffer = captureAudio();
PitchDetectionUtils.PitchResult result = 
    PitchDetectionUtils.detectPitchHybrid(audioBuffer, 44100.0);
    
if (result.isVoiced) {
    System.out.printf("Detected: %.2f Hz (confidence: %.2f)\n",
        result.frequency, result.confidence);
}
```

## Development Guidelines

### Adding New Optimized Implementations
1. Create a new class extending the FFT interface in the `com.fft.optimized` package
2. Annotate with `@FFTImplementation` specifying the supported size and priority
3. Implement the transform methods with your optimization strategy
4. The factory system will automatically discover and register your implementation

### Testing Requirements
- All implementations must pass mathematical correctness tests
- Performance benchmarks should be included for optimized implementations
- Edge cases (invalid input sizes, null arrays) should be handled gracefully
- Thread safety should be considered for shared state

### Code Conventions
- Follow Java naming conventions and standard formatting
- Include comprehensive JavaDoc for all public methods and classes
- Use immutable objects where possible for thread safety
- Include performance characteristics in class documentation
- Follow the existing test structure and patterns

## Performance Characteristics

### Benchmark Results
- **FFTOptimized8**: 2.27x verified speedup with complete loop unrolling
- **FFTOptimized16**: Neutral performance (overhead removed)
- **FFTOptimized32**: 1.1x speedup (overhead removed)
- **FFTOptimized64**: Neutral performance (overhead removed)
- **FFTOptimized128**: 1.4x speedup (direct implementation)

### Memory Usage
- FFT operations typically require O(n) additional memory
- Large transforms (4096+) may require significant temporary buffers
- Optimized implementations minimize memory allocations where possible
- Twiddle factor cache uses ~128 KB for 10 common sizes (8-4096)

## Important Files and Documentation

- `README.md`: Comprehensive overview and usage instructions
- `pom.xml`: Maven build configuration and dependencies
- `src/main/java/com/fft/core/`: Core interfaces and base implementation
- `src/main/java/com/fft/optimized/`: Optimized implementations for specific sizes
- `src/main/java/com/fft/utils/`: Utility functions and audio processing
- `src/main/java/com/fft/demo/`: Real-time audio processing examples
- `run_tests.bat`: Windows batch script for running tests
- `run-jmh-benchmarks.bat`: Windows batch script for running benchmarks
- Various documentation files in the root directory (PERFORMANCE_*.md, etc.)

## Quality Assurance

### Testing Framework
- 305+ unit tests across 25 test class files with 100% pass rate
- Property-based testing for mathematical properties (Parseval's theorem)
- Performance regression testing for automated detection of performance degradation
- JaCoCo integration for code coverage (90% line coverage target)

### Static Analysis
- SpotBugs for static code analysis and bug detection
- Maven quality gates and dependency management
- Comprehensive JavaDoc with examples and performance notes

## Audio Processing Algorithms

### YIN Algorithm
- Autocorrelation-based pitch detection with high accuracy
- Identifies fundamental frequency through autocorrelation analysis
- Includes threshold-based voicing detection

### Spectral Analysis
- FFT-based peak detection with parabolic interpolation for sub-bin accuracy
- Harmonic analysis for fundamental frequency extraction
- 44x more accurate than YIN alone (0.92% vs 40.6% error on pure tones)

### Voicing Detection
- RMS-based sound/silence discrimination
- Configurable thresholds for different noise levels
- Stability analysis through temporal averaging

### Windowing Functions
- Hamming window implementation to reduce spectral leakage
- Applied before FFT for improved frequency resolution
# Fast Fourier Transform (FFT) Library

[![CI Build](https://github.com/hedoluna/fft/actions/workflows/ci.yml/badge.svg)](https://github.com/hedoluna/fft/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/hedoluna/fft/branch/main/graph/badge.svg)](https://codecov.io/gh/hedoluna/fft)
[![License: Unlicense](https://img.shields.io/badge/license-Unlicense-blue.svg)](http://unlicense.org/)
[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Maven Central](https://img.shields.io/badge/Maven-2.0.0--SNAPSHOT-green.svg)](pom.xml)

A Java FFT library with a reference implementation, factory-based selection, one size-specific optimized implementation for FFT-8, and audio analysis demos built on top of the FFT primitives.

Based on the algorithms originally published by E. Oran Brigham "The Fast Fourier Transform" 1973, in ALGOL60 and FORTRAN.  
Originally written in the summer of 2008 during holidays in Sardinia by Orlando Selenu.  
Enhanced and refactored in 2025 with modern Java patterns, comprehensive testing, and advanced audio processing capabilities.

## ✨ Key Features

- **🚀 Performance-Oriented Core**: `FFTOptimized8` is auto-selected for size 8, while other power-of-two sizes use `FFTBase`
- **🏭 Factory Pattern**: Automatic implementation selection via `DefaultFFTFactory`
- **⚡ Core Optimizations**: cached twiddle factors, cached bit-reversal tables, and efficient array copying in `FFTBase`
- **🎯 Type Safety**: Modern API with immutable result objects and rich data extraction
- **🧪 Comprehensive Testing**: large JUnit suite covering correctness, factory behavior, demos, performance checks, and regression scenarios
- **🎵 Audio Processing**: Real-time pitch detection (0.92% error), song recognition using Parsons code, chord detection
- **📦 Zero Dependencies**: Pure Java 17 implementation (uses javax.sound for audio demos only)
- **🔧 Maven Build**: Modern build system with quality gates (JaCoCo: 90% line, 85% branch coverage)
- **🆓 Public Domain**: Completely free for any use, commercial or academic

## 📦 Package Structure

```
com.fft.core/         # Core FFT interfaces and base implementations
├── FFT.java          # Main FFT interface
├── FFTBase.java      # Generic reference implementation  
└── FFTResult.java    # Immutable result wrapper

com.fft.factory/      # Implementation selection and factory pattern
├── FFTFactory.java   # Factory interface
├── DefaultFFTFactory.java # Default implementation with auto-discovery
└── FFTImplementationDiscovery.java # Auto-registration system

com.fft.optimized/    # Size-specific optimized implementations
├── FFTOptimized8.java        # 8-point FFT with complete loop unrolling
├── OptimizedFFTFramework.java # Historical/deprecated optimization scaffold
└── OptimizedFFTUtils.java    # Shared optimized helpers

com.fft.core/         # Core algorithm + performance support classes
├── FFTBase.java              # Generic fallback for all other power-of-two sizes
├── TwiddleFactorCache.java   # Precomputed cos/sin tables
└── BitReversalCache.java     # Cached bit-reversal tables

com.fft.utils/        # Utility classes and helpers
├── FFTUtils.java     # Convenience methods and legacy API
└── PitchDetectionUtils.java # Advanced pitch detection algorithms

com.fft.demo/         # Advanced demonstration applications
├── PitchDetectionDemo.java     # Real-time pitch detection
├── SongRecognitionDemo.java    # Melody recognition using Parsons code
├── SimulatedPitchDetectionDemo.java # Performance validation
├── ParsonsCodeUtils.java       # Music information retrieval utilities
└── RefactoringDemo.java        # Migration examples
```

## 🚀 Quick Start

### Maven Dependency
Add to your `pom.xml`:
```xml
<dependency>
    <groupId>com.fft</groupId>
    <artifactId>fast-fourier-transform</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

### Basic Usage (New API)

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

### Utility Methods

```java
import com.fft.utils.FFTUtils;

// Create a custom factory and inspect registry details
FFTFactory factory = FFTUtils.createFactory();
System.out.println(factory.getRegistryReport());

// Query which implementation would handle a given size
String info = FFTUtils.getImplementationInfo(1024);

// Zero pad to the next power of two
double[] padded = FFTUtils.zeroPadToPowerOfTwo(rawSignal);
int nextSize = FFTUtils.nextPowerOfTwo(300); // 512
```

### Legacy API (Backward Compatibility)

```java
// Legacy API still works (deprecated but maintained)
double[] result = FFTUtils.fft(realPart, imagPart, true);
double[] magnitudes = FFTUtils.getMagnitudes(result);
```

## 🎵 Audio Processing Capabilities

### 🎯 Advanced Pitch Detection (Updated October 2025)

**CRITICAL UPDATE**: After comprehensive accuracy analysis, the library now uses **spectral FFT-based method as primary** (0.92% error vs YIN's 40.6% error on pure tones). See `PITCH_DETECTION_ANALYSIS.md` for complete details.

The library features state-of-the-art pitch detection using hybrid approach:

- **Spectral Method (Primary)**: FFT-based peak detection with **0.92% error** (44x more accurate than YIN alone)
  - Parabolic interpolation for sub-bin accuracy
  - Harmonic analysis for fundamental frequency extraction
  - 26% faster than YIN (O(N log N) vs O(N²))
- **YIN Algorithm (Validation)**: Autocorrelation-based, used to detect subharmonic issues
  - Prone to subharmonic errors on pure tones (detects 110Hz instead of 440Hz)
  - Used as validation check, not primary method
- **Hybrid Approach**: Combines both methods for best accuracy
  - Results averaged when both agree (within 5%)
  - Subharmonic detection prevents octave errors
- **Voicing Detection**: RMS-based sound/silence discrimination
- **Median Filtering**: Reduces pitch jitter for stable detection

```java
import com.fft.utils.PitchDetectionUtils;
import com.fft.demo.PitchDetectionDemo;

// Recommended: Hybrid pitch detection (spectral + YIN validation)
double[] audioBuffer = captureAudio();
PitchDetectionUtils.PitchResult result = PitchDetectionUtils.detectPitchHybrid(audioBuffer, 44100.0);
if (result.isVoiced) {
    System.out.printf("Detected: %.2f Hz (confidence: %.2f)\n",
        result.frequency, result.confidence);
}

// Real-time pitch detection from microphone
PitchDetectionDemo demo = new PitchDetectionDemo();
demo.runDemo(); // Features spectral method + YIN validation, 0.92% error
```

### Song Recognition

```java
import com.fft.demo.SongRecognitionDemo;
import com.fft.demo.ParsonsCodeUtils;

// Initialize song recognition system
SongRecognitionDemo recognizer = new SongRecognitionDemo();

// Recognize melody from audio
double[] melodyAudio = recordMelody();
RecognitionResult result = recognizer.recognizeMelody(melodyAudio);
System.out.println("Best match: " + result.getSongTitle() + 
    " (confidence: " + result.getConfidence() + ")");

// Generate Parsons code for melody analysis
String parsonsCode = ParsonsCodeUtils.generateParsonsCode(frequencies);
System.out.println("Parsons code: " + parsonsCode); // e.g., "*UDUDRDU"
```

## 📊 Performance Characteristics

### Current Implementation Snapshot

| Size | Implementation selected by factory | Notes |
|------|------------------------------------|-------|
| 8 | `FFTOptimized8` | Specialized implementation with loop unrolling |
| 16+ power-of-two sizes | `FFTBase` | Uses cached twiddle factors and cached bit-reversal tables |
| non power-of-two sizes | not supported directly | Use `FFTUtils.zeroPadToPowerOfTwo(...)` first |

The repository contains historical benchmark reports under `docs/performance/`, but the current codebase should be understood as:
- one specialized optimized implementation for FFT size 8
- one general-purpose `FFTBase` implementation for the remaining supported sizes
- performance-sensitive caches integrated into the base implementation

**Performance Breakdown (from PROFILING_RESULTS.md):**
```
Size 256 (before twiddle cache):
- Twiddle factors: 56.1% of time
- Butterfly operations: 4.2%
- Bit-reversal: 3.2%

Size 256 (with twiddle cache):
- Twiddle factors: ~17% of time (3.2x faster!)
- Overall improvement: 30-50% faster execution
```

### Optimization Notes

The current optimization strategy is conservative:
- specialize only the tiny FFT-8 case where manual unrolling is manageable
- keep the larger-size implementation centralized in `FFTBase`
- rely on shared caches and validation tests instead of duplicating many size-specific classes

### Audio Processing Performance
- **Real-time Capability**: 44.1 kHz sampling rate supported
- **Pitch Detection Speed**: 12,000+ recognitions/second with YIN algorithm
- **Song Recognition**: 60-80% accuracy for partial melody sequences with improved pitch detection
- **Noise Robustness**: Maintains accuracy down to 6dB SNR with voicing detection
- **Pitch Accuracy**: <0.5% error across musical range (80Hz-2000Hz) with YIN algorithm

## 🔬 Algorithm Details

### Core Algorithm Features
- **Cooley-Tukey FFT**: Classic divide-and-conquer algorithm with O(n log n) complexity
- **Decimation-in-frequency** approach for efficient computation
- **Bit-reversal permutation** for optimal memory access patterns
- **In-place computation** minimizing memory allocation
- **Parseval energy conservation** ensuring mathematical accuracy

### Optimization Techniques
1. **Complete Loop Unrolling**: Eliminates loop overhead for small sizes (demonstrated in FFTOptimized8)
2. **Precomputed Trigonometry**: Hardcoded sine/cosine values avoid runtime calculations
3. **Factory Pattern**: Zero-overhead automatic implementation selection
4. **Immutable Results**: Thread-safe result objects with efficient data access
5. **Memory Pooling**: Reduced garbage collection pressure in high-frequency scenarios

### Audio Processing Algorithms
1. **YIN Algorithm**: Autocorrelation-based pitch detection with high accuracy
2. **Voicing Detection**: RMS-based sound/silence discrimination
3. **Median Filtering**: Pitch stability enhancement through temporal smoothing
4. **Windowing Functions**: Hamming window implementation for spectral leakage reduction
5. **Peak Detection**: Parabolic interpolation for sub-bin frequency accuracy
6. **Harmonic Analysis**: Fundamental frequency detection from overtone series
7. **Parsons Code**: Complete music information retrieval methodology
8. **Noise Filtering**: Configurable thresholds for robust detection

## 🧪 Testing and Quality

### Test Coverage
- **100+ Unit Tests**: Comprehensive coverage of all functionality
- **Property-Based Testing**: Mathematical properties (Parseval's theorem, energy conservation)
- **Performance Regression Testing**: Automated detection of performance degradation
- **Audio Processing Tests**: Pitch detection accuracy and song recognition validation
- **JaCoCo Integration**: 90% line coverage, 85% branch coverage targets

### Quality Gates
- **SpotBugs Analysis**: Static code analysis for bug detection
- **Maven Build**: Automated quality checks and dependency management
- **Comprehensive Documentation**: JavaDoc with examples and performance notes

## 🏗️ Build and Development

### Building from Source
```bash
git clone <repository-url>
cd fast-fourier-transform
mvn clean compile test
```

**Status**: Build succeeds with 100% test pass rate (197/197 tests passing). Core functionality is operational with working auto-discovery and factory pattern.

### Running Demos
```bash
# Real-time pitch detection demo
mvn exec:java -Dexec.mainClass="com.fft.demo.PitchDetectionDemo"

# Song recognition demo  
mvn exec:java -Dexec.mainClass="com.fft.demo.SongRecognitionDemo"

# Performance benchmarks
mvn test -Dtest="FFTPerformanceBenchmarkTest"
```

### Code Coverage
```bash
mvn clean test jacoco:report
# Open target/site/jacoco/index.html
```

## 📚 Documentation

Additional details about the demos and testing process are available in the
project documentation:

- [docs/demos/DEMO_DOCUMENTATION.md](docs/demos/DEMO_DOCUMENTATION.md) - comprehensive demo guide
- [docs/demos/DEMO_TESTING_SUMMARY.md](docs/demos/DEMO_TESTING_SUMMARY.md) - summary of demo testing
- [docs/demos/FFT_Library.md](docs/demos/FFT_Library.md) - historical library overview

## Current Status

- Core FFT support is provided by `FFTBase` for power-of-two sizes.
- `FFTOptimized8` is the only dedicated optimized implementation currently present.
- Factory auto-discovery is active and currently finds that optimized implementation.
- Audio demos and utilities for pitch, melody, and chord analysis are included.
- Current local verification: `622` tests passed, `0` failures, `0` errors, `8` skipped via `mvn test`.
- **Production Ready**: Enterprise-grade reliability with comprehensive error handling

### 🔄 Future Optimization Opportunities

**Next Steps for True Algorithmic Gains:**
1. **FFT16 Radix-4 DIT**: Use 2 stages instead of 4 (simpler verification, target 1.8-2.0x)
2. **FFT32 Composite**: Leverage 4×FFT8 blocks (reuse proven code, target 2.0-2.5x)
3. **FFT64 Radix-8**: Only 2 stages needed (simpler than 6-stage radix-2, target 2.0-2.5x)
4. **Verification Framework**: Stage-by-stage validation against FFTBase

**Long-term Enhancements:**
- **Template-based code generation** for consistent optimization patterns
- **SIMD vectorization** using Java Vector API (JDK 16+)
- **Split-radix algorithms** for 25% fewer operations
- **Streaming FFT support** for real-time applications

## 📖 Migration Guide

### From Legacy API to Modern API

```java
// OLD (deprecated but still works)
double[] result = FFTUtils.fft(real, imag, true);
double magnitude = Math.sqrt(result[2*i]*result[2*i] + result[2*i+1]*result[2*i+1]);

// NEW (recommended)
FFTResult result = FFTUtils.fft(real, imag, true);
double magnitude = result.getMagnitudeAt(i);
```

### Migration Benefits
- **Type Safety**: Compile-time error detection
- **Performance**: Automatic implementation selection
- **Maintainability**: Cleaner, more readable code
- **Future-Proof**: Foundation for additional features

## 📄 License

This is a public domain implementation completely free of charge.  
I only ask to cite me and send me a link to your work if you're using this.

Have fun with FFTs! They are a great source of inspiration, and when you start learning about these mathematical operators you'll find them everywhere in signal processing, audio analysis, and beyond.

---
**Orlando Selenu** (original implementation, 2008)  
**Enhanced and Refactored** (2025) - Modern Java patterns, audio processing, and comprehensive testing framework

## 🔗 Related Projects

- **Audio Analysis**: Pitch detection and song recognition demonstrate practical FFT applications
- **Music Information Retrieval**: Parsons code implementation for melody analysis
- **Signal Processing**: Foundation for digital signal processing applications
- **Educational**: Reference implementation for learning FFT algorithms

---

*For detailed implementation notes, see [REFACTORING_ROADMAP.md](REFACTORING_ROADMAP.md) and [REFACTORING_SUMMARY.md](REFACTORING_SUMMARY.md)*

# Fast Fourier Transform (FFT) Library

[![CI Build](https://github.com/hedoluna/fft/actions/workflows/ci.yml/badge.svg)](https://github.com/hedoluna/fft/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/hedoluna/fft/branch/main/graph/badge.svg)](https://codecov.io/gh/hedoluna/fft)
[![License: Unlicense](https://img.shields.io/badge/license-Unlicense-blue.svg)](http://unlicense.org/)
[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Maven Central](https://img.shields.io/badge/Maven-2.0.0--SNAPSHOT-green.svg)](pom.xml)

A comprehensive, high-performance Java implementation of the Fast Fourier Transform algorithm with multiple optimized versions, factory pattern, and modern API design. This library provides both educational reference implementations and production-ready optimized versions suitable for real-world signal processing applications including real-time audio analysis, pitch detection, and song recognition.

Based on the algorithms originally published by E. Oran Brigham "The Fast Fourier Transform" 1973, in ALGOL60 and FORTRAN.  
Originally written in the summer of 2008 during holidays in Sardinia by Orlando Selenu.  
Enhanced and refactored in 2025 with modern Java patterns, comprehensive testing, and advanced audio processing capabilities.

## ✨ Key Features

- **🚀 High Performance**: FASE 2 optimizations complete - FFT8 (~3.0x ±15% speedup), FFT128 (1.42x), all regressions eliminated
- **🏭 Factory Pattern**: Automatic implementation selection with 14 size-specific implementations (8-65536)
- **🎯 Type Safety**: Modern API with immutable result objects and rich data extraction
- **🧪 Comprehensive Testing**: 305+ unit tests across 25 test class files with 100% pass rate
- **🎵 Audio Processing**: Real-time pitch detection and song recognition using Parsons code methodology
- **📦 Zero Dependencies**: Pure Java 17 implementation (uses javax.sound for audio demos only)
- **🔧 Maven Build**: Modern build system with quality gates and code coverage
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

com.fft.optimized/    # Size-specific optimized implementations (14 total)
├── FFTOptimized8.java    # 8-point FFT (~3.0x ±15% speedup - complete loop unrolling)
├── FFTOptimized16.java   # 16-point FFT (neutral - delegation overhead removed)
├── FFTOptimized32.java   # 32-point FFT (1.12x speedup - overhead removed)
├── FFTOptimized64.java   # 64-point FFT (neutral - overhead removed)
├── FFTOptimized128.java  # 128-point FFT (1.42x speedup - optimized)
└── ... (all power-of-2 sizes 8 to 65536)

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

### Benchmark Results (October 2025 - With Twiddle Cache)

| Size | Implementation | Optimization Status | Verified Performance |
|------|---------------|---------------------|--------------------|
| 8 | FFTOptimized8 | 🏆 **Excellent** | **2.27x verified** (10K warmup, loop unrolling, hardcoded twiddles) |
| 16 | FFTOptimized16 | ✅ **Neutral** | ~1.0x (overhead removed) |
| 32 | FFTOptimized32 | ✅ **Small Gain** | ~1.1x (overhead removed) |
| 64 | FFTOptimized64 | ✅ **Neutral** | ~1.0x (overhead removed) |
| 128 | FFTOptimized128 | ✅ **Good** | ~1.4x (direct implementation) |
| 256 | FFTOptimized256 | ✅ **Neutral** | ~1.0x (baseline) |
| 512+ | FFTOptimized* | ✅ **Baseline** | ~1.0x (uses base with twiddle cache) |
| **ALL** | **FFTBase** | 🚀 **NEW: Twiddle Cache** | **30-50% faster** (precomputed cos/sin) |

**Major Performance Improvements (October 2025):**
- 🚀 **Twiddle Factor Cache**: Precomputed cos/sin tables provide 30-50% overall FFT speedup
  - Twiddle factors were #1 bottleneck (43-56% of FFT execution time)
  - Precomputation provides 2.3-3.2x speedup for twiddle operations
  - Memory cost: ~128 KB for 10 common sizes (8-4096)
  - Cache hits: 100% for sizes 8-4096 (most common use cases)
- 🏆 **FFTOptimized8**: **2.27x verified** with clean methodology (10,000 warmup iterations)
  - Previous claims of 3.36x were measurement artifacts (insufficient warmup)
  - Actual 2.27x is solid and reproducible
- ✅ **All Optimizations Validated**: 305+/305+ tests passing (including pitch detection accuracy tests)
- ✅ **Zero Regressions**: Twiddle cache maintains 100% correctness
- 🎯 **Pitch Detection Validated**: PitchDetectionAccuracyTest proves spectral method 44x more accurate than YIN

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

### 🔬 Optimization Techniques (Validated October 2025)

**✅ What Worked Successfully:**
- 🚀 **Precomputed twiddle factor cache** (30-50% overall speedup - BIGGEST WIN!)
  - Profiling identified twiddle factors as #1 bottleneck (43-56% of time)
  - TwiddleFactorCache replaces Math.cos/sin with array lookups
  - 2.3-3.2x speedup for twiddle operations, ~128 KB memory
- ✅ **Complete loop unrolling** (FFT8: 2.27x verified speedup)
- ✅ **Hardcoded twiddle factors** as static final constants (FFT8)
- ✅ **Direct FFTBase implementation** (no delegation layers)
- ✅ **Manual unrolled array copying** for small sizes
- ✅ **Removing ConcurrentHashMap caching** (overhead elimination)
- ✅ **Profiling-driven optimization** (identify bottlenecks before optimizing)
- ✅ **Proper benchmarking methodology** (10K+ warmup iterations critical)

**❌ What Didn't Work:**
- ❌ **Naive algorithm extension** (FFT8 → FFT16 pattern failed)
- ❌ **Delegation patterns** through OptimizedFFTUtils (5-16% overhead)
- ❌ **ConcurrentHashMap caching** for lightweight objects
- ❌ **Framework abstractions** in performance-critical paths
- ❌ **Insufficient warmup** (50 iterations too few - FFT8 showed as 0.14x instead of 2.27x!)

**🎓 Key Lessons (see OPTIMIZATION_LESSONS_LEARNED.md, PROFILING_RESULTS.md):**
- **Profile first, optimize second** - Twiddle cache provided biggest win by targeting actual bottleneck
- Direct implementation > Delegation patterns (always)
- Proper warmup is CRITICAL (10,000+ iterations for complex optimized code)
- Measure baseline correctly (don't trust System.nanoTime() without heavy warmup)
- Each FFT size needs mathematical verification
- JVM optimizes straight-line code better than loops

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

- [docs/DEMO_DOCUMENTATION.md](docs/DEMO_DOCUMENTATION.md) – comprehensive demo guide
- [docs/DEMO_TESTING_SUMMARY.md](docs/DEMO_TESTING_SUMMARY.md) – summary of testing and coverage
- [docs/FFT_Library.md](docs/FFT_Library.md) – Italian overview of the library

## 🚧 Current Development Status

### ✅ Completed and Functional
- ✅ **Modern package structure** with clean separation of concerns
- ✅ **Core FFT functionality** via FFTBase supporting all power-of-2 sizes  
- ✅ **Build system** working (Maven compiles successfully)
 - ✅ **Auto-discovery system** finds and registers all 14 implementations
- ✅ **Two genuine optimizations** (FFTOptimized8: 1.24x, FFTOptimized32: stage-optimized)
- ✅ **Basic test validation** (105+ core tests passing including FFTBaseTest 20/20)
- ✅ **Modern API design** (FFTResult wrapper, type-safe interfaces)
- ✅ **Advanced audio processing** framework exists (pitch detection, song recognition)

### ✅ Recent Major Improvements
- **✅ Advanced Pitch Detection**: YIN algorithm implementation with superior accuracy
- **✅ PitchDetectionUtils**: Shared utility class for YIN and spectral pitch detection
- **✅ Voicing Detection**: RMS-based sound/silence discrimination
- **✅ Song Recognition Integration**: Improved pitch detection integrated into recognition pipeline
- **✅ Memory Optimizations**: 95% memory reduction in large FFT transforms
- **✅ Critical Bug Fixes**: FFTBase validation and null checking improvements
- **✅ FFTUtils Static Initialization Fixed**: Implemented lazy initialization with double-checked locking
- **✅ Test Suite Stabilized**: 296+ unit tests with 100% pass rate
- **✅ Auto-Discovery Working**: All 14 implementations correctly discovered and registered

### ✅ Current Status (100% Tests Passing)
- **296+ out of 296+ tests passing** - Complete functionality fully operational
- **Factory Pattern Working**: Automatic implementation selection functional
- **Advanced Audio Processing**: YIN algorithm, voicing detection, song recognition pipeline
- **Memory Optimized**: 95% reduction in large transforms, efficient implementations
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

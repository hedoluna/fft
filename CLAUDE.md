# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Java Fast Fourier Transform (FFT) library with factory pattern, auto-discovery, and audio processing. Provides size-specific optimized implementations (8-65536) with automatic selection.

**✅ BUILD STATUS**: Maven 3.6.3 + Java 17, all tests passing (305+/305+)
**🚀 PERFORMANCE**: FFT8: 2.27x verified, Twiddle cache: 30-50% overall speedup, all optimizations validated
**✅ COVERAGE**: JaCoCo enforces 90% line / 85% branch coverage
**📊 PROFILING**: Complete - Twiddle factors were #1 bottleneck (43-56%), now optimized with precomputed cache
**🎯 PITCH ACCURACY**: Spectral method 44x more accurate than YIN (0.92% vs 40.6% error) - See PITCH_DETECTION_ANALYSIS.md

## ⚡ Quick Reference

**Build & Test:**
```bash
mvn clean compile test                    # Full build with tests
mvn test -Dtest=FFTBaseTest              # Single test class
mvn test -Dtest=FFTBaseTest#testMethod   # Specific test method
mvn clean test jacoco:report             # Coverage report
```

**Performance Benchmarking:**
```bash
mvn test -Dtest=FFTPerformanceBenchmarkTest                    # Quick benchmark
./run-jmh-benchmarks.bat FFT8                                  # JMH rigorous (Windows)
./run-jmh-benchmarks.sh FFT8                                   # JMH rigorous (Linux/Mac)
```

**Run Demos:**
```bash
mvn exec:java -Dexec.mainClass="com.fft.demo.PitchDetectionDemo"      # Pitch detection
mvn exec:java -Dexec.mainClass="com.fft.demo.SongRecognitionDemo"     # Song recognition
```

**Current Performance Status (October 2025):**
- **FFT8**: 2.27x verified (complete loop unrolling)
- **Twiddle Cache**: 30-50% speedup across ALL sizes (universal optimization)
- **FFT128**: 1.42x speedup (existing optimizations)
- **All Others**: Neutral/baseline with twiddle cache benefit

**Key Documentation:**
- **Master Index**: `DOCUMENTATION_INDEX.md` - organized navigation by role
- **Status**: `PERFORMANCE_OPTIMIZATION_STATUS.md` - FASE 1-2 complete
- **Lessons**: `FASE_2_LESSONS_LEARNED.md` - what worked (twiddle cache, FFT8) vs didn't (manual unrolling FFT16+)
- **Profiling**: `PROFILING_RESULTS.md` - twiddle factors #1 bottleneck (43-56%)
- **Benchmarking**: `JMH_BENCHMARKING_GUIDE.md` - proper methodology (10K+ warmup)
- **Archive**: `docs/archive/README.md` - 12 historical/duplicate docs

**Critical Insights:**
- ✅ **Profile first, optimize second** - twiddle cache was biggest win
- ✅ **Proper warmup essential** - 10,000+ iterations for complex optimized code
- ✅ **Direct implementation > delegation** - eliminated 5-16% overhead
- ❌ **Manual unrolling beyond FFT8 not worth it** - twiddle cache already provides 30-50%

## Build Commands

**Standard Workflow:**
```bash
# Full build with tests
mvn clean compile test

# Unit tests only (excludes *IntegrationTest.java)
mvn test

# Integration tests
mvn verify

# Code coverage report (target/site/jacoco/index.html)
mvn clean test jacoco:report

# Static analysis
mvn spotbugs:check

# Package JAR
mvn package
```

**Running Specific Tests:**
```bash
# Specific test class
mvn test -Dtest=FFTBaseTest

# Specific test method
mvn test -Dtest=FFTBaseTest#testTransformPowerOfTwo

# Pattern matching
mvn test -Dtest="*Optimized*Test"

# Performance benchmarks (takes 5+ minutes)
mvn test -Dtest=FFTPerformanceBenchmarkTest
```

**Demo Execution:**
```bash
# Real-time pitch detection (spectral FFT + YIN validation, 0.92% error)
mvn exec:java -Dexec.mainClass="com.fft.demo.PitchDetectionDemo"

# Song recognition (Parsons code, 60-80% accuracy)
mvn exec:java -Dexec.mainClass="com.fft.demo.SongRecognitionDemo"

# Chord recognition (harmonic analysis, major/minor/7th detection)
mvn exec:java -Dexec.mainClass="com.fft.demo.ChordRecognitionDemo"

# Simulated pitch detection (testing framework without microphone)
mvn exec:java -Dexec.mainClass="com.fft.demo.SimulatedPitchDetectionDemo"

# Architecture showcase (factory pattern, optimized implementations)
mvn exec:java -Dexec.mainClass="com.fft.demo.RefactoringDemo"
```

## Core Architecture

**Package Structure:**
- `com.fft.core`: Core interfaces (FFT, FFTBase, FFTResult)
- `com.fft.factory`: Factory pattern and auto-discovery (DefaultFFTFactory, FFTImplementationDiscovery)
- `com.fft.optimized`: 14 size-specific implementations (FFTOptimized8 through FFTOptimized65536)
- `com.fft.utils`: FFTUtils (legacy API wrapper), PitchDetectionUtils
- `com.fft.demo`: Audio processing demos (pitch detection, song recognition, chord recognition)

**Factory Pattern & Auto-Discovery:**
1. Implementations annotated with `@FFTImplementation(size=X, priority=Y, characteristics={...})`
2. `FFTImplementationDiscovery` scans packages at initialization: `com.fft.optimized`, `com.fft.experimental`, `com.fft.custom`
3. Auto-registration in `DefaultFFTFactory` with priority-based selection
4. Factory selects highest-priority implementation for requested size, falls back to FFTBase for unsupported sizes

**Key Design Patterns:**
- **Immutable Results**: FFTResult provides thread-safe access to magnitudes, phases, power spectrum
- **Lazy Initialization**: Factory uses double-checked locking to avoid static initialization order issues
- **Auto-Discovery**: No manual registration needed when adding new optimized implementations

## Implementation Strategy

**Adding New Optimized Implementations:**
1. Implement FFT interface
2. Add annotation: `@FFTImplementation(size=X, priority=Y, description="...", characteristics={...})`
3. Place in `com.fft.optimized` package (auto-discovered)
4. Create corresponding test class in `src/test/java/com/fft/optimized/`
5. Verify with: `mvn test -Dtest=YourNewTest`

**Modifying Core Interfaces:**
- Maintain backward compatibility with legacy FFTUtils API
- FFTResult objects must remain immutable
- Update both unit tests and integration tests
- Verify with full test suite: `mvn clean test`

## Performance Optimization

**Current Status (October 2025):**
- **FASE 1**: Framework overhead eliminated (3.1x speedup on small sizes)
- **FASE 2**: Delegation overhead removed, all regressions eliminated
- **P0-P1 COMPLETED**: Profiling analysis + validation framework + twiddle cache implemented
  - ✅ FFT8: **2.27x verified** (clean methodology with 10K warmup iterations)
  - ✅ **Twiddle Factor Cache**: Precomputed cos/sin tables for 30-50% overall speedup
  - ✅ FFT16-64: Neutral performance (0.99x-1.12x, overhead removed)
  - ✅ FFT128: 1.42x speedup (existing optimizations confirmed working)
  - ✅ All sizes: 100% correctness maintained (296+/296+ tests passing)

**What Worked (see FASE_2_LESSONS_LEARNED.md, PROFILING_RESULTS.md):**
- ✅ **Precomputed twiddle factors** (TwiddleFactorCache): 2.3-3.2x speedup for twiddle operations, 30-50% overall improvement
- ✅ Complete loop unrolling for small sizes (FFT8: 2.27x verified with heavy warmup)
- ✅ Hardcoded twiddle factors as static final constants (FFT8)
- ✅ Direct implementation (no delegation layers)
- ✅ Manual unrolled array copying for small sizes
- ✅ Removing ConcurrentHashMap caching overhead
- ✅ Profiling-driven optimization (identified twiddle factors as #1 bottleneck: 43-56% of time)

**What Didn't Work:**
- ❌ Naive algorithm extension (FFT8 → FFT16 failed)
- ❌ Delegation patterns (5-16% overhead)
- ❌ ConcurrentHashMap caching for lightweight objects
- ❌ Framework abstraction in tight loops
- ❌ Insufficient warmup in benchmarks (50 iterations too few - need 10,000+)

**Optimization Techniques for Future:**
- Radix-4 DIT for FFT16 (2 stages instead of 4)
- Composite approach for FFT32 (4×FFT8 blocks)
- Radix-8 DIT for FFT64 (only 2 stages)
- Split-radix algorithms (25% fewer operations)
- Cache-friendly memory access patterns

**Performance Investigation:**
```bash
# Run unit test benchmarks (quick, less rigorous)
mvn test -Dtest=FFTPerformanceBenchmarkTest

# Run JMH benchmarks (rigorous, statistical variance)
# RECOMMENDED: Use the helper scripts (Windows/Linux)
./run-jmh-benchmarks.bat        # Windows
./run-jmh-benchmarks.sh         # Linux/Mac

# Run JMH for specific benchmark pattern
./run-jmh-benchmarks.bat FFTBaseProfiling           # Windows
./run-jmh-benchmarks.sh FFTBaseProfiling            # Linux/Mac

# With custom JMH options (forks, warmup, iterations)
./run-jmh-benchmarks.sh FFT8 -f 3 -wi 10 -i 20

# Note: mvn exec:java does NOT work due to resource loading issues
# The helper scripts use direct java -cp which properly finds META-INF/BenchmarkList

# Verify implementation selection
# Add to test: System.out.println(FFTUtils.getImplementationInfo(1024));
```

**See JMH_BENCHMARKING_GUIDE.md for detailed benchmarking instructions**

## Testing & Quality

**Test Organization:**
- Unit tests: `src/test/java/**/*Test.java` (305+ active tests, 5 disabled for deprecated code, across 25 files)
- Accuracy tests: `src/test/java/com/fft/analysis/PitchDetectionAccuracyTest.java` (4 test scenarios)
- Integration tests: `src/test/java/**/*IntegrationTest.java`
- Performance tests: Nested classes within test files (e.g., `PerformanceTests`)
- Surefire excludes `*IntegrationTest.java` by default
- Failsafe runs integration tests during `mvn verify`

**Quality Gates:**
- JaCoCo enforces 90% line coverage, 85% branch coverage
- SpotBugs static analysis (see spotbugs-exclude.xml for exclusions)
- All tests must pass for build success
- Property-based testing validates Parseval's theorem, energy conservation

**Common Test Issues:**
- **Implementation not found**: Verify `@FFTImplementation` annotation and package is scanned
- **Performance regression**: Check factory is selecting optimized implementation, not FFTBase fallback
- **Audio processing failures**: Verify sample rate (44100 Hz) and buffer size match expected values
- **Coverage issues**: Run `mvn clean test jacoco:report` for fresh report

## Audio Processing Features

**⭐ CRITICAL UPDATE (October 2025):** Spectral method now primary after discovering YIN has 40.6% mean error on pure tones due to subharmonic detection. See **PITCH_DETECTION_ANALYSIS.md** for complete analysis.

**Advanced Pitch Detection:**
- **Spectral Method (Primary)**: FFT-based peak detection, **0.92% error** across 80Hz-2000Hz
  - Parabolic interpolation for sub-bin accuracy
  - Harmonic analysis for fundamental frequency extraction
  - 26% faster than YIN (O(N log N) vs O(N²))
- **YIN Algorithm (Validation)**: Autocorrelation-based, used to detect subharmonic issues
  - High confidence but prone to subharmonic errors (detecting 110Hz instead of 440Hz)
  - Used as validation check, not primary method
- **Hybrid Approach**: Combines both methods for best accuracy
  - Spectral method as primary (most accurate)
  - YIN validation detects subharmonic issues
  - Results averaged when both agree (within 5%)
- **Voicing Detection**: RMS-based sound/silence discrimination (0.001 threshold)
- **Median Filtering**: Pitch stability enhancement (5-frame window)
- **Shared Utilities**: PitchDetectionUtils provides spectral, YIN, and hybrid methods

**Test Suite (PitchDetectionAccuracyTest.java):**
- 10 test frequencies: 82.41 Hz (E2) to 1318.51 Hz (E6)
- Complex waveform testing (harmonically rich signals)
- Noise tolerance analysis (SNR 5-30 dB)
- Performance benchmarking (1000 iterations)
- Validates FFT implementation choice doesn't affect accuracy

**Song Recognition:**
- **Parsons Code**: Melody contour analysis (*UDUDRDU format)
- **Integration**: Uses improved spectral pitch detection with YIN validation
- **Performance**: 60-80% accuracy for partial melody sequences
- **Noise Robustness**: Maintains accuracy down to 6dB SNR
- **Database**: 15+ famous melodies (Twinkle Twinkle, Happy Birthday, Beethoven, etc.)
- **Learning System**: Continuous improvement with usage

**Chord Recognition:**
- **Multi-Pitch Detection**: Extracts up to 4 simultaneous frequencies
- **Chord Types**: Major, Minor, 7th, Augmented, Diminished, Suspended
- **Progressions**: Recognizes common patterns (I-V-vi-IV, II-V-I jazz)
- **Harmonic Analysis**: Interval relationships and chord quality

**Performance Characteristics:**
- Real-time capability: 44.1 kHz sampling rate
- Pitch detection: 12,000+ detections/second
- FFT processing: 4096-point in ~75ms
- Spectral method: 1,374,480 ns/op (with FFTOptimized)
- YIN algorithm: 1,978,400 ns/op (baseline)

## Demo Applications

The library includes 5 comprehensive demo applications showcasing real-world FFT use cases:

**1. PitchDetectionDemo (811 lines)** - Real-time pitch detection from microphone
- **Input**: Live microphone audio (44.1 kHz)
- **Algorithm**: Spectral FFT + YIN validation (0.92% error)
- **Output**: Musical note (e.g., "A4 440Hz"), Parsons code (*UDUDRD...)
- **Features**: Hamming window, voicing detection, median filtering, subharmonic validation
- **Use Cases**: Instrument tuners, vocal training, automatic transcription, music games
- **Run**: `mvn exec:java -Dexec.mainClass="com.fft.demo.PitchDetectionDemo"`

**2. SongRecognitionDemo (2000 lines)** - Complete melody recognition system
- **Algorithm**: Parsons code matching with machine learning
- **Database**: 15+ famous melodies (Twinkle Twinkle, Happy Birthday, Beethoven, etc.)
- **Features**: Partial matching, noise tolerance, learning system, LRU caching
- **Performance**: 60-80% accuracy on melody fragments, works down to 6dB SNR
- **Demos**: 7 scenarios (basic, partial, noisy, variations, real-time, advanced, performance)
- **Use Cases**: Shazam-like for hummed melodies, music education, cataloging
- **Run**: `mvn exec:java -Dexec.mainClass="com.fft.demo.SongRecognitionDemo"`

**3. ChordRecognitionDemo (621 lines)** - Multi-pitch chord detection
- **Algorithm**: Multi-pitch FFT peak detection + harmonic analysis
- **Chord Types**: Major, Minor, 7th, Augmented, Diminished, Suspended
- **Features**: Up to 4 simultaneous frequencies, progression recognition (I-V-vi-IV, II-V-I)
- **Demos**: Basic chord detection, progression recognition, harmonic analysis, melody+harmony
- **Use Cases**: Automatic chord charts, theory education, backing track generation
- **Run**: `mvn exec:java -Dexec.mainClass="com.fft.demo.ChordRecognitionDemo"`

**4. SimulatedPitchDetectionDemo (492 lines)** - Testing framework without hardware
- **Purpose**: Test pitch detection with synthetic signals (no microphone needed)
- **Signals**: Pure tones, chords, melodies, noise, harmonics, vibrato
- **Coverage**: All notes C0-B8 (108 notes, equal temperament A4=440Hz)
- **Demos**: Single tones, chords, melodies, noisy signals, performance comparison
- **Use Cases**: Automated testing, algorithm validation, benchmarking, demos
- **Run**: `mvn exec:java -Dexec.mainClass="com.fft.demo.SimulatedPitchDetectionDemo"`

**5. RefactoringDemo (177 lines)** - Architecture showcase
- **Purpose**: Educational demo of library architecture
- **Features**: New API, factory pattern, auto-selection, backward compatibility
- **Demos**: Type-safe API, implementation selection, performance, legacy API, FFTResult wrapper
- **Use Cases**: Developer onboarding, API documentation, architecture tutorial
- **Run**: `mvn exec:java -Dexec.mainClass="com.fft.demo.RefactoringDemo"`

**Demo Comparison:**

| Demo | Real-Time | Complexity | Accuracy | Primary Use Case |
|------|-----------|------------|----------|------------------|
| PitchDetection | ✅ Yes | Medium | 0.92% error | Instrument tuning, vocal training |
| SongRecognition | ✅ Yes | High | 60-80% | Melody identification, music education |
| ChordRecognition | ✅ Yes | High | ~85% | Chord charts, harmonic analysis |
| SimulatedPitch | ❌ No | Low | 100% (synthetic) | Testing, validation, benchmarking |
| Refactoring | ❌ No | Low | N/A | Developer education, API showcase |

**Application Domains:**
- 🎓 **Music Education**: Interactive learning, ear training, theory visualization
- 🎵 **Music Production**: Auto-tuning, chord chart generation, transcription tools
- 🎤 **Performance Analysis**: Intonation monitoring, live chord display, setlist recognition
- 🎮 **Interactive Games**: Singing games, chord matching, "Name That Tune" style games

## Debugging & Troubleshooting

**Verify Factory Registration:**
```bash
# Run any test to see auto-discovery logs
mvn test -Dtest=FFTBaseTest
# Look for "Discovered FFT implementation" messages
```

**Check Implementation Selection:**
```java
// Add to test/demo code
FFTFactory factory = FFTUtils.createFactory();
System.out.println(factory.getRegistryReport());
System.out.println(FFTUtils.getImplementationInfo(1024));
```

**Enable Verbose Logging:**
```bash
# Create logging.properties:
# .level=INFO
# com.fft.level=FINE
mvn test -Djava.util.logging.config.file=logging.properties
```

**Common Issues:**
- **Build failures**: Check Java 17 is active, run `mvn clean compile`
- **Test timeouts**: Performance benchmarks can take 5+ minutes
- **Static initialization errors**: FFTUtils now uses lazy initialization with double-checked locking
- **Coverage report issues**: Run `mvn clean test jacoco:report` (not just `jacoco:report`)
- **Microphone access issues**: Demos requiring audio input may need system permissions

**Dependencies:**
- **SLF4J 2.0.9**: Logging API (compile scope)
- **Logback 1.5.19**: Logging implementation (test scope only) - **Updated Oct 2025**
- **JUnit Jupiter 5.9.2**: Primary testing framework
  - **JUnit Jupiter API**: Explicit dependency for test annotations
  - **JUnit Jupiter Params**: Explicit dependency for parameterized tests
- **JUnit 4.13.2 + Vintage Engine**: Backward compatibility for legacy tests
- **AssertJ 3.24.2**: Fluent assertion library
- **Mockito 5.1.1**: Mocking framework
- **JMH 1.37**: Benchmarking (test scope) with annotation processor configured
- **Minimal runtime dependencies**: Core library only depends on SLF4J API

## CI/CD & Infrastructure

**GitHub Actions Workflows:**
- **ci.yml**: Automated build and test on push/PR to main/develop
  - Runs on Java 17 and Java 21 (matrix build)
  - Executes full test suite with coverage
  - Uploads coverage to Codecov
  - Badge status visible on README.md
- **dependency-check.yml**: Weekly automated dependency security checks
  - Runs Monday at 00:00 UTC
  - Checks for outdated dependencies (`mvn versions:display-dependency-updates`)
  - Analyzes dependency issues (`mvn dependency:analyze`)
- **javadoc.yml**: Automated Javadoc deployment to GitHub Pages
  - Deploys on push to main branch
  - Generates API documentation with `mvn javadoc:javadoc`
  - Published at project's GitHub Pages URL

**Code Style & Formatting:**
- **.editorconfig**: Consistent formatting across editors/IDEs
  - Java: 4 spaces indent, max line 120, UTF-8
  - XML/YAML/JSON: Configured appropriately
  - Prevents formatting conflicts in pull requests
  - Supported by all major editors (VS Code, IntelliJ, Eclipse, etc.)

**Code Coverage:**
- **Codecov Integration**: Automated coverage tracking
  - Badge on README.md shows current coverage
  - CI uploads coverage reports after each build
  - Trend analysis for coverage changes over time

**Build Configuration:**
- **JMH Annotation Processor**: Configured in pom.xml for proper benchmark support
  - Generates META-INF/BenchmarkList during test compilation
  - Enables rigorous performance benchmarking with JMH
  - Run benchmarks: `./run-jmh-benchmarks.bat` (Windows) or `./run-jmh-benchmarks.sh` (Linux/Mac)
  - Note: Helper scripts use direct `java -cp` instead of exec-maven-plugin to avoid resource loading issues

## Development Guidelines

**Architecture Principles:**
- Factory pattern provides automatic implementation selection based on size and priority
- All power-of-2 sizes from 8 to 65536 have dedicated classes (some use FFTBase fallback internally)
- FFTBase is the reference implementation and correctness baseline
- Never modify optimized implementations without verifying against FFTBase output

**Performance Work:**
- Always verify correctness before measuring performance
- Use property-based tests (Parseval's theorem, energy conservation)
- Performance target: 1.5x-3.5x speedup over FFTBase for optimized sizes
- See PERFORMANCE_OPTIMIZATION_STATUS.md for roadmap

**Audio Processing:**
- ⚠️ **CRITICAL**: Spectral method is primary (0.92% error), YIN is validation only (40.6% error)
- Always verify correctness when modifying pitch detection to avoid regressions
- Test with PitchDetectionAccuracyTest.java to validate algorithm changes
- Spectral method in PitchDetectionUtils is the reference implementation
- YIN algorithm used for subharmonic detection validation only
- Voicing detection prevents false positives from background noise
- Parsons code generation requires stable pitch tracking
- See PITCH_DETECTION_ANALYSIS.md for complete accuracy analysis

**Key Files for Reference:**

**⭐ Start Here:**
- `DOCUMENTATION_INDEX.md`: **Master index** - organized navigation to all docs by role/category

**Performance & Optimization (Essential):**
- `PERFORMANCE_OPTIMIZATION_STATUS.md`: Current status - FASE 1 & 2 complete, twiddle cache + FFT8 verified
- `FASE_2_LESSONS_LEARNED.md`: What worked (twiddle cache 30-50%, FFT8 2.27x) vs didn't (manual unrolling FFT16+)
- `FASE2_FINAL_REPORT.md`: Comprehensive FASE 2 analysis - FFT8 success, FFT16-512 delegation removal
- `PROFILING_RESULTS.md`: Profiling data showing twiddle factors #1 bottleneck (43-56% of time)
- `PERFORMANCE_MEASUREMENT_CRISIS.md`: Investigation resolving FFT8 measurement issues (warmup critical)
- `CONSENSUS_ANALYSIS.md`: Multi-agent FFT8 variance analysis (2.36x-3.47x depending on conditions)

**Testing & Benchmarking:**
- `JMH_BENCHMARKING_GUIDE.md`: Rigorous performance measurement methodology (10K+ warmup essential)
- `VALIDATION_FRAMEWORK.md`: Stage-by-stage FFT validation for debugging optimizations
- `TESTING_COMPLIANCE.md`: Test coverage requirements and quality gates

**Audio Processing & Accuracy:**
- `PITCH_DETECTION_ANALYSIS.md`: **Complete pitch detection accuracy analysis** (spectral 0.92% vs YIN 40.6% error)
- `src/test/java/com/fft/analysis/PitchDetectionAccuracyTest.java`: Comprehensive accuracy test suite (4 scenarios, 10 frequencies)

**Implementation Reports:**
- `P0_IMPLEMENTATION_SUMMARY.md`: JMH + accurate documentation recommendations
- `P1_IMPLEMENTATION_SUMMARY.md`: Profiling + validation + test fixes (✅ COMPLETED)

**Architecture:**
- `REFACTORING_SUMMARY.md`: Phase 1-2 refactoring summary (package structure, factory pattern)

**Core Implementation:**
- `src/main/java/com/fft/core/TwiddleFactorCache.java`: Precomputed cos/sin tables (30-50% speedup)
- `src/main/java/com/fft/core/FFTBase.java`: Reference implementation using twiddle cache
- `src/main/java/com/fft/optimized/FFTOptimized8.java`: Complete loop unrolling (2.27x verified)
- `src/main/java/com/fft/optimized/OptimizedFFTFramework.java`: DEPRECATED (10x overhead eliminated in FASE 1)

**User Documentation:**
- `README.md`: User-facing documentation with quick start and examples

**📦 Archived Documentation** (see `docs/archive/README.md`):
- 12 obsolete/duplicate documents archived October 6, 2025
- Historical planning, completed work, duplicate FASE 2 docs
- Full git history preserved for reference

**Important Notes:**
- **OptimizedFFTFramework is deprecated**: FASE 1 eliminated framework overhead (10x) by making implementations call FFTBase directly
- **Test count**: 310+ total tests (305+ active, 5 disabled for deprecated framework) - **Updated Oct 2025**
  - Added PitchDetectionAccuracyTest.java (4 comprehensive test scenarios)
  - Validates spectral method accuracy (0.92% error) vs YIN (40.6% error)
- **Pitch detection strategy changed**: Spectral method now primary, YIN validation only - **Updated Oct 2025**
  - YIN has 40.6% error on pure tones due to subharmonic detection
  - Spectral FFT-based method achieves 0.92% error (44x better)
  - See PITCH_DETECTION_ANALYSIS.md for complete analysis
- **Maven forkCount=0**: Tests run in-process for faster execution, JaCoCo coverage uses ${argLine}
- **SpotBugs disabled**: Incompatible with Java 17 bytecode, run manually if needed with `mvn spotbugs:spotbugs`
- **Logging**: All code uses SLF4J API (production + demos) - **Updated Oct 2025**
  - Demo classes migrated from System.out to logger.info() for professional logging
  - Configurable logging levels via Logback configuration (test scope)

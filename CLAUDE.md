# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Java Fast Fourier Transform (FFT) library with factory pattern, auto-discovery, and audio processing. Provides size-specific optimized implementations (8-65536) with automatic selection.

**✅ BUILD STATUS**: Maven 3.6.3 + Java 17, all tests passing (296+/296+)
**🚀 PERFORMANCE**: FFT8: 2.27x verified, Twiddle cache: 30-50% overall speedup, all optimizations validated
**✅ COVERAGE**: JaCoCo enforces 90% line / 85% branch coverage
**📊 PROFILING**: Complete - Twiddle factors were #1 bottleneck (43-56%), now optimized with precomputed cache

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
# Real-time pitch detection (YIN algorithm)
mvn exec:java -Dexec.mainClass="com.fft.demo.PitchDetectionDemo"

# Song recognition (Parsons code)
mvn exec:java -Dexec.mainClass="com.fft.demo.SongRecognitionDemo"

# Chord recognition (harmonic analysis)
mvn exec:java -Dexec.mainClass="com.fft.demo.ChordRecognitionDemo"

# Simulated pitch detection for performance validation
mvn exec:java -Dexec.mainClass="com.fft.demo.SimulatedPitchDetectionDemo"
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

**What Worked (see OPTIMIZATION_LESSONS_LEARNED.md, PROFILING_RESULTS.md):**
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
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.classpathScope=test

# Run JMH for specific size (e.g., FFT8)
mvn clean test-compile exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args="FFT8" \
  -Dexec.classpathScope=test

# Verify implementation selection
# Add to test: System.out.println(FFTUtils.getImplementationInfo(1024));
```

**See JMH_BENCHMARKING_GUIDE.md for detailed benchmarking instructions**

## Testing & Quality

**Test Organization:**
- Unit tests: `src/test/java/**/*Test.java` (301 active tests, 5 disabled for deprecated code, across 24 files)
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

**Advanced Pitch Detection:**
- **YIN Algorithm**: Autocorrelation-based, <0.5% error across 80Hz-2000Hz
- **Voicing Detection**: RMS-based sound/silence discrimination
- **Median Filtering**: Pitch stability enhancement
- **Shared Utilities**: PitchDetectionUtils provides YIN and spectral methods

**Song Recognition:**
- **Parsons Code**: Melody contour analysis (*UDUDRDU format)
- **Integration**: Uses improved YIN pitch detection
- **Performance**: 60-80% accuracy for partial melody sequences
- **Noise Robustness**: Maintains accuracy down to 6dB SNR

**Performance Characteristics:**
- Real-time capability: 44.1 kHz sampling rate
- Pitch detection: 12,000+ recognitions/second
- FFT processing: 4096-point in ~75ms

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
- **Logback 1.4.11**: Logging implementation (test scope only)
- **JUnit Jupiter 5.9.2**: Primary testing framework
- **JUnit 4.13.2 + Vintage Engine**: Backward compatibility for legacy tests
- **AssertJ 3.24.2**: Fluent assertion library
- **Mockito 5.1.1**: Mocking framework
- **JMH 1.37**: Benchmarking (test scope)
- **Minimal runtime dependencies**: Core library only depends on SLF4J API

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
- Always verify correctness when modifying pitch detection to avoid regressions
- YIN algorithm in PitchDetectionUtils is the reference implementation
- Voicing detection prevents false positives from background noise
- Parsons code generation requires stable pitch tracking

**Key Files for Reference:**

**Performance & Optimization (Essential):**
- `PROFILING_RESULTS.md`: ✅ **COMPLETED** - Actual profiling data showing twiddle factors as #1 bottleneck (43-56%)
- `PERFORMANCE_MEASUREMENT_CRISIS.md`: ✅ **RESOLVED** - Investigation showing FFT8 2.27x speedup (not 0.14x)
- `OPTIMIZATION_LESSONS_LEARNED.md`: What worked and didn't work in FASE 2 (essential reading)
- `CONSENSUS_ANALYSIS.md`: Multi-agent analysis revealing performance variance issues
- `PERFORMANCE_OPTIMIZATION_STATUS.md`: Performance roadmap (FASE 1 & 2 completed)

**Implementation Guides:**
- `P0_IMPLEMENTATION_SUMMARY.md`: P0 critical recommendations (JMH + accurate docs)
- `P1_IMPLEMENTATION_SUMMARY.md`: P1 priorities (profiling + validation + test fixes) - ✅ COMPLETED
- `JMH_BENCHMARKING_GUIDE.md`: Rigorous performance measurement methodology
- `VALIDATION_FRAMEWORK.md`: Stage-by-stage FFT validation guide for debugging

**Analysis Reports:**
- `FASE2_OVERHEAD_REMOVAL.md`: FASE 2 delegation overhead removal results
- `FASE2_FINAL_REPORT.md`: Complete FASE 2 analysis and findings

**Core Implementation:**
- `src/main/java/com/fft/core/TwiddleFactorCache.java`: ✅ **NEW** - Precomputed cos/sin tables (30-50% speedup)
- `src/main/java/com/fft/core/FFTBase.java`: Reference implementation (now uses twiddle cache)
- `src/test/java/com/fft/performance/SimpleProfilingTest.java`: Profiling benchmarks with proper warmup
- `src/test/java/com/fft/validation/FFTValidationFramework.java`: Stage-by-stage validation tool
- `src/main/java/com/fft/optimized/OptimizedFFTUtils.java`: Hardcoded twiddle constants for specific sizes
- `src/main/java/com/fft/optimized/OptimizedFFTFramework.java`: DEPRECATED (10x overhead, see FASE 1)

**User Documentation:**
- `README.md`: User-facing documentation and examples
- `REFACTORING_ROADMAP.md`, `REFACTORING_SUMMARY.md`: Historical context

**Important Notes:**
- **OptimizedFFTFramework is deprecated**: FASE 1 eliminated framework overhead (10x) by making implementations call FFTBase directly
- **Test count**: 306 total tests (301 active, 5 disabled for deprecated framework)
- **Maven forkCount=0**: Tests run in-process for faster execution, JaCoCo coverage uses ${argLine}
- **SpotBugs disabled**: Incompatible with Java 17 bytecode, run manually if needed with `mvn spotbugs:spotbugs`
- **Logging**: Production code uses SLF4J API, demos use System.out (appropriate for user-facing output)

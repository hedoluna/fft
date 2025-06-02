# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java Fast Fourier Transform (FFT) library with modern architecture, factory pattern, and audio processing capabilities. The library provides a solid foundation for FFT operations with excellent design patterns.

**✅ BUILD STATUS**: Compiles successfully  
**⚠️ CURRENT ISSUES**: Test suite instability (53% pass rate) and performance claims exceed actual implementations

**Key Architecture Principles:**
- Factory pattern with auto-discovery for optimal implementation selection
- Size-specific optimized implementations (FFTOptimized8, FFTOptimized32, etc.)
- Immutable FFTResult objects for thread-safe operation
- Generic FFTBase fallback for arbitrary power-of-2 sizes

## Build Commands

**Standard Development Workflow:**
```bash
# ✅ Compilation works, but tests have issues
mvn clean compile test

# Run unit tests only (excludes integration tests)
mvn test

# Run integration tests
mvn verify

# Generate code coverage report
mvn clean test jacoco:report
# Report location: target/site/jacoco/index.html

# Run static analysis
mvn spotbugs:check

# Package the library
mvn package
```

**Demo Execution:**
```bash
# Real-time pitch detection demo
mvn exec:java -Dexec.mainClass="com.fft.demo.PitchDetectionDemo"

# Song recognition demo
mvn exec:java -Dexec.mainClass="com.fft.demo.SongRecognitionDemo"

# Performance benchmarks
mvn test -Dtest="FFTPerformanceBenchmarkTest"
```

## Core Architecture

**Package Structure:**
- `com.fft.core`: Core interfaces (FFT, FFTBase, FFTResult)
- `com.fft.factory`: Factory pattern and auto-discovery system
- `com.fft.optimized`: Size-specific optimized implementations
- `com.fft.utils`: Legacy API and utilities
- `com.fft.demo`: Audio processing demonstrations and real-world applications

**Key Components:**
1. **FFT Interface**: Core contract for all implementations
2. **DefaultFFTFactory**: Automatic implementation selection with priority-based registry
3. **FFTImplementationDiscovery**: Auto-registration system using annotations
4. **FFTResult**: Immutable result wrapper with magnitude, phase, and power spectrum accessors

**Factory Selection Logic:**
1. Exact size match for optimized implementations
2. Highest priority implementation selection
3. Fallback to generic FFTBase for unsupported sizes

## Implementation Strategy

**When adding new optimized implementations:**
1. Extend the FFT interface
2. Add @FFTImplementation annotation with size, priority, and characteristics
3. Register in DefaultFFTFactory.registerDefaultImplementations()
4. Create comprehensive test coverage in the optimized package
5. Update performance benchmarks

**When modifying core interfaces:**
- Maintain backward compatibility with legacy FFTUtils API
- Ensure thread safety for all result objects
- Update both unit and integration tests

## Test Organization

**Test Structure:**
- Unit tests: `src/test/java/com/fft/**/*Test.java`
- Integration tests: `src/test/java/com/fft/**/*IntegrationTest.java`
- Performance tests: Nested classes within test files (e.g., PerformanceTests)

**Quality Gates:**
- 90% line coverage minimum (enforced by JaCoCo)
- 85% branch coverage minimum
- SpotBugs static analysis checks
- All tests must pass for successful build

## Current Development Focus

**🚨 IMMEDIATE PRIORITIES**:
1. **Fix FFTUtils static initialization** - Critical blocker affecting 85 tests
2. **Resolve factory annotation validation** - Blocking performance tests  
3. **Implement genuine optimizations** - Only 2 out of 13 classes actually optimize

**Current Reality**:
- ✅ **Core functionality working** (FFTBase, factory pattern, auto-discovery)
- ✅ **Two genuine optimizations** (FFTOptimized8: 1.24x, FFTOptimized32)
- ⚠️ **Test suite instability** (53% pass rate due to initialization issues)
- ⚠️ **Performance gap** (Most "optimized" implementations are fallbacks)

**Audio Processing Features**:
- ✅ **Framework exists** with sophisticated design
- ⚠️ **Testing limited** due to FFTUtils initialization failures
- ✅ **Core algorithms validated** (spectral analysis, pattern matching)
- 🔄 **Performance claims require validation**
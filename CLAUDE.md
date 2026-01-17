# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Java Fast Fourier Transform (FFT) library with factory pattern, auto-discovery, and audio processing. v2.1 release with proven performance optimizations (6-9% improvement), comprehensive testing, and advanced audio features.

**✅ BUILD STATUS**: Maven 3.6.3 + Java 17, 414 total tests (406 passing, 8 skipped) - ALL PASSING
**🚀 PERFORMANCE**: v2.1 - 1.06-1.09x overall (6-9%), FFT8: 1.83-1.91x (83-91%), zero regressions
**⚡ OPTIMIZATIONS**: System.arraycopy (2-3%), TwiddleFactorCache (30-50%), BitReversalCache (O(n) vs O(n log n))
**✅ COVERAGE**: JaCoCo enforces 90% line / 85% branch coverage - verified passing
**🎯 PITCH ACCURACY**: Spectral method 44x more accurate than YIN (0.92% vs 40.6% error)
**📅 LAST UPDATED**: January 7, 2026 (Milestone 1.1 complete, Phase 2-3 optimization analysis)

## ⚙️ Prerequisites

- **Java 17+** (required - project uses Java 17 features)
- **Maven 3.6.3+** (build system)
- **Docker Desktop** (for database-dependent projects in workspace - not needed for FFT)
- **Windows 11** (current development environment) or Linux/Mac

## ⚡ Quick Reference (Most Common Commands)

**Build & Test:**
```bash
mvn clean compile test                    # Full build + tests
mvn test -Dtest=FFTBaseTest              # Single test class
mvn clean test jacoco:report             # Coverage report
```

**Benchmarking:**
```bash
.\run-jmh-benchmarks.bat FFT8            # Windows (RECOMMENDED)
./run-jmh-benchmarks.sh FFT8             # Linux/Mac (RECOMMENDED)
mvn test -Dtest=FFTPerformanceBenchmarkTest  # Quick benchmark (less rigorous)
```

**Demos:**
```bash
mvn exec:java -Dexec.mainClass="com.fft.demo.PitchDetectionDemo"
mvn exec:java -Dexec.mainClass="com.fft.demo.SongRecognitionDemo"
```

**Key Docs:**
- `DOCUMENTATION_INDEX.md` - Master navigation
- `docs/performance/OPTIMIZATION_COMPLETION_SUMMARY_JAN2026.md` - Current optimization state
- `docs/performance/FASE_2_LESSONS_LEARNED.md` - What worked/didn't work
- `docs/testing/JMH_BENCHMARKING_GUIDE.md` - Rigorous benchmarking howto

**Current Performance Status (January 2026 - v2.1):**
- **Overall**: 1.06-1.09x speedup (6-9% improvement) - proven via real measurement
- **FFT8**: 1.83-1.91x (83-91% faster) - complete loop unrolling with hardcoded twiddles
- **FFT16+**: Fallback to FFTBase with universal optimizations:
  - TwiddleFactorCache: 30-50% speedup on twiddle operations
  - BitReversalCache: O(n) instead of O(n log n)
  - System.arraycopy: 33% faster array initialization
- **Result**: All sizes benefit from universal caches, zero regressions

**Verified Through Testing:**
- ✅ **Real measurement > Theory** - Phase 3 caught bad optimization via production testing
- ✅ **Precomputation wins** - TwiddleFactorCache and BitReversalCache proven effective
- ✅ **Context matters** - Butterfly optimization: +67% isolated, -5% production → REJECTED
- ✅ **Proper testing catches problems** - 414/414 tests passing, process validated

## Platform-Specific Notes

**Windows Environment** (current setup):
- Use `.\run-jmh-benchmarks.bat` for JMH benchmarks (not .sh)
- Git Bash, CMD, or PowerShell all work for Maven commands
- Maven handles both `/` and `\` path separators automatically
- Use Git Bash for Unix-style commands (grep, etc.)

**Linux/Mac Environment**:
- Use `./run-jmh-benchmarks.sh` for JMH benchmarks
- All bash commands work natively

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
# 🎵 NEW! Real-time song recognition from microphone (whistle/hum a melody!)
mvn exec:java -Dexec.mainClass="com.fft.demo.RealTimeSongRecognitionDemo"

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

## ⚠️ Common Mistakes to Avoid

**DO NOT:**
- ❌ Modify optimized implementations without validating against FFTBase
- ❌ Use `mvn exec:java` for JMH benchmarks (resource loading fails)
- ❌ Trust benchmarks with <10,000 warmup iterations for optimized code
- ❌ Kill node processes without checking for Claude Code instance
- ❌ Add manual optimizations before profiling (profile first!)
- ❌ Skip FFTBase validation when changing core algorithms

**ALWAYS:**
- ✅ Run `mvn clean test` before committing
- ✅ Use helper scripts (`.bat` or `.sh`) for JMH benchmarks
- ✅ Verify correctness before measuring performance
- ✅ Check `docs/performance/OPTIMIZATION_COMPLETION_SUMMARY_JAN2026.md` before optimization work
- ✅ Update tests when adding new optimized implementations

## Git Workflow

**Before Starting Work:**
```bash
git fetch && git pull              # Update from remote
git status                         # Check current state
git log -5 --oneline              # View recent commits
```

**During Development:**
```bash
git status                         # Review changes
git diff                           # Review code changes
git diff --staged                  # Review staged changes
```

**Committing Changes:**
```bash
git add <files>                    # Stage specific files
git add .                          # Stage all changes
git commit -m "descriptive message"  # Commit with message
```

**After Completing Features:**
```bash
git push                           # Push to remote (main branch)
git push origin <branch>           # Push to feature branch
```

**Important Git Safety:**
- ⚠️ **NEVER kill Node processes** - may terminate Claude Code itself!
- When killing node for testing, ensure you don't kill Claude Code instance
- Always verify process details before terminating

## Core Architecture

**Package Structure:**
- `com.fft.core`: Core FFT implementation (FFTBase with universal optimizations)
- `com.fft.cache`: Performance optimization caches (TwiddleFactorCache, BitReversalCache)
- `com.fft.optimized`: Size-specific implementations (FFTOptimized8 with 1.83-1.91x speedup)
- `com.fft.factory`: Factory pattern and auto-discovery (DefaultFFTFactory, FFTImplementationDiscovery)
- `com.fft.utils`: FFTUtils (legacy API), PitchDetectionUtils (spectral + YIN methods)
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

**Current Status (January 2026 - v2.1 COMPLETE):**
- **Phase 1 (Completed)**: Framework overhead eliminated, delegation patterns removed
- **Phase 2 (Completed)**: Profiling analysis + TwiddleFactorCache + BitReversalCache implemented
- **Phase 3 (Completed)**: Butterfly optimization tested (micro-benchmark: +67%, production: -5%) → correctly REJECTED
- **v2.1 Results**:
  - ✅ FFT8: **1.83-1.91x speedup** (complete loop unrolling with hardcoded twiddles, 10K+ warmup)
  - ✅ **TwiddleFactorCache**: 30-50% speedup on twiddle operations (universal, all sizes)
  - ✅ **BitReversalCache**: O(n) vs O(n log n) complexity (universal, all sizes)
  - ✅ **System.arraycopy**: 33% faster array initialization (confirmed)
  - ✅ **Overall**: 1.06-1.09x speedup (6-9% improvement) - proven via real measurement
  - ✅ All sizes: 100% correctness maintained (414 tests: 406 passing, 8 skipped)

**What Worked (Verified in v2.1):**
- ✅ **Precomputed caches** (TwiddleFactorCache, BitReversalCache): Measurable production gains
- ✅ **System.arraycopy**: 33% faster than manual loop, confirmed working
- ✅ Complete loop unrolling for FFT8 with hardcoded twiddles: 1.83-1.91x
- ✅ Production context testing: Caught butterfly optimization regression before merge
- ✅ Proper warmup methodology: 10,000+ iterations essential for accurate measurements

**What Didn't Work (v2.1 Learnings):**
- ❌ Butterfly optimization: +67% isolated benchmark, -5% regression in production (register pressure)
- ❌ Assuming isolated benchmarks predict production performance
- ❌ Adding local variables in tight loops (increases register pressure)
- ❌ Manual unrolling beyond FFT8 (diminishing returns vs cache optimizations)

**Future Optimization Ideas (Low Priority):**
- Radix-4 or Radix-8 variants for specific sizes (architectural changes, high risk)
- SIMD/vectorization (if JVM supports)
- Composite approaches for larger sizes using FFT8 blocks
- Cache-friendly memory layouts
- **Note**: Current 1.06-1.09x represents solid achievement with zero regressions

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

**See docs/testing/JMH_BENCHMARKING_GUIDE.md for detailed benchmarking instructions**

## Testing & Quality

**Test Organization:**
- Unit tests: `src/test/java/**/*Test.java` (414 total tests: 406 passing, 8 skipped)
- **Skipped Tests**: 8 tests skipped/disabled due to known YIN algorithm limitations (40.6% error rate on pure tones - see PITCH_DETECTION_ANALYSIS.md)
- **All Core Tests Passing**: Zero failures, zero regressions from v2.1 optimizations
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

**⭐ CRITICAL UPDATE (October 2025):** Spectral method now primary after discovering YIN has 40.6% mean error on pure tones due to subharmonic detection. See **docs/testing/PITCH_DETECTION_ANALYSIS.md** for complete analysis.

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

The library includes 6 comprehensive demo applications showcasing real-world FFT use cases:

**🎵 1. RealTimeSongRecognitionDemo (NEW!)** - Whistle/hum to recognize songs!
- **Input**: Live microphone audio (44.1 kHz) - whistle, hum, or sing
- **Algorithm**: Spectral FFT pitch detection + Parsons code + database matching
- **Output**: Progressive song identification with confidence scores
- **Features**: 10+ song database, 60-80% accuracy, partial matching, real-time display
- **Database**: Twinkle Twinkle, Happy Birthday, Ode to Joy, Amazing Grace, Jingle Bells, etc.
- **Use Cases**: Shazam-like for hummed melodies, music games, karaoke helper
- **Run**: `mvn exec:java -Dexec.mainClass="com.fft.demo.RealTimeSongRecognitionDemo"`

**2. PitchDetectionDemo (811 lines)** - Real-time pitch detection from microphone
- **Input**: Live microphone audio (44.1 kHz)
- **Algorithm**: Spectral FFT + YIN validation (0.92% error)
- **Output**: Musical note (e.g., "A4 440Hz"), Parsons code (*UDUDRD...)
- **Features**: Hamming window, voicing detection, median filtering, subharmonic validation
- **Use Cases**: Instrument tuners, vocal training, automatic transcription, music games
- **Run**: `mvn exec:java -Dexec.mainClass="com.fft.demo.PitchDetectionDemo"`

**3. SongRecognitionDemo (2000 lines)** - Complete melody recognition system
- **Algorithm**: Parsons code matching with machine learning
- **Database**: 15+ famous melodies (Twinkle Twinkle, Happy Birthday, Beethoven, etc.)
- **Features**: Partial matching, noise tolerance, learning system, LRU caching
- **Performance**: 60-80% accuracy on melody fragments, works down to 6dB SNR
- **Demos**: 7 scenarios (basic, partial, noisy, variations, real-time, advanced, performance)
- **Use Cases**: Shazam-like for hummed melodies, music education, cataloging
- **Run**: `mvn exec:java -Dexec.mainClass="com.fft.demo.SongRecognitionDemo"`

**4. ChordRecognitionDemo (621 lines)** - Multi-pitch chord detection
- **Algorithm**: Multi-pitch FFT peak detection + harmonic analysis
- **Chord Types**: Major, Minor, 7th, Augmented, Diminished, Suspended
- **Features**: Up to 4 simultaneous frequencies, progression recognition (I-V-vi-IV, II-V-I)
- **Demos**: Basic chord detection, progression recognition, harmonic analysis, melody+harmony
- **Use Cases**: Automatic chord charts, theory education, backing track generation
- **Run**: `mvn exec:java -Dexec.mainClass="com.fft.demo.ChordRecognitionDemo"`

**5. SimulatedPitchDetectionDemo (492 lines)** - Testing framework without hardware
- **Purpose**: Test pitch detection with synthetic signals (no microphone needed)
- **Signals**: Pure tones, chords, melodies, noise, harmonics, vibrato
- **Coverage**: All notes C0-B8 (108 notes, equal temperament A4=440Hz)
- **Demos**: Single tones, chords, melodies, noisy signals, performance comparison
- **Use Cases**: Automated testing, algorithm validation, benchmarking, demos
- **Run**: `mvn exec:java -Dexec.mainClass="com.fft.demo.SimulatedPitchDetectionDemo"`

**6. RefactoringDemo (177 lines)** - Architecture showcase
- **Purpose**: Educational demo of library architecture
- **Features**: New API, factory pattern, auto-selection, backward compatibility
- **Demos**: Type-safe API, implementation selection, performance, legacy API, FFTResult wrapper
- **Use Cases**: Developer onboarding, API documentation, architecture tutorial
- **Run**: `mvn exec:java -Dexec.mainClass="com.fft.demo.RefactoringDemo"`

**Demo Comparison:**

| Demo | Real-Time | Complexity | Accuracy | Primary Use Case |
|------|-----------|------------|----------|------------------|
| **RealTimeSongRecognition** | **✅ Yes** | **High** | **60-80%** | **🎵 Whistle/hum recognition, music games** |
| PitchDetection | ✅ Yes | Medium | 0.92% error | Instrument tuning, vocal training |
| SongRecognition | ⚠️ Simulation | High | 60-80% | Melody identification, music education |
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

**Windows-Specific Troubleshooting:**
- **JMH benchmarks fail**: Use `.\run-jmh-benchmarks.bat` instead of .sh script
- **Script not found**: Ensure you're in project root directory (D:\repos\fft)
- **Path issues**: Windows paths with spaces may need quotes in some contexts
- **Line endings**: Git should handle CRLF/LF automatically (check .gitattributes)
- **Process killing**: Use Task Manager or `taskkill /F /PID <pid>` (but never kill Claude Code!)
- **Grep commands**: Install Git Bash for Unix-style commands, or use PowerShell equivalents

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
- **FFTOptimized8**: Only size with dedicated optimized implementation (1.83-1.91x)
- **All other sizes**: Use FFTBase with universal cache optimizations (TwiddleFactorCache, BitReversalCache)
- FFTBase is the reference implementation and correctness baseline
- Never modify implementations without verifying correctness against test suite

**Performance Work:**
- Always verify correctness before measuring performance
- Use property-based tests (Parseval's theorem, energy conservation)
- v2.1 achieved: 1.06-1.09x overall (6-9%) with zero regressions
- Future optimizations should be measured in production context, not isolated benchmarks
- Document all optimizations with TDD methodology (RED phase, GREEN phase, REFACTOR phase)

**Audio Processing:**
- ⚠️ **CRITICAL**: Spectral method is primary (0.92% error), YIN is validation only (40.6% error)
- Always verify correctness when modifying pitch detection to avoid regressions
- Test with PitchDetectionAccuracyTest.java to validate algorithm changes
- Spectral method in PitchDetectionUtils is the reference implementation
- YIN algorithm used for subharmonic detection validation only
- Voicing detection prevents false positives from background noise
- Parsons code generation requires stable pitch tracking
- See docs/testing/PITCH_DETECTION_ANALYSIS.md for complete accuracy analysis

**Key Files for Reference:**

**⭐ Start Here:**
- `DOCUMENTATION_INDEX.md`: **Master index** - organized navigation to all docs by role/category
- `USER_GUIDE.md`: **Complete user manual** - step-by-step guide to using FFTs (1000+ lines, 4 complete examples)

**Performance & Optimization (v2.1 Complete):**
- ✅ **v2.1 Verification**: All optimizations tested and verified (1.06-1.09x overall, zero regressions)
- `docs/performance/BASELINE_MEASUREMENT_JAN2026.md`: v2.1 baseline with all optimizations active
- `docs/performance/PHASE_3_BUTTERFLY_OPTIMIZATION_REPORT.md`: Butterfly optimization analysis (+67% isolated, -5% production, correctly REJECTED)
- **Archived Historical Docs** (see `docs/archive/README.md`):
  - `FASE_2_LESSONS_LEARNED.md`: October 2025 FASE 2 analysis
  - `FASE2_FINAL_REPORT.md`: October 2025 FASE 2 completion
  - `PROFILING_RESULTS.md`: Initial profiling data
  - `PERFORMANCE_MEASUREMENT_CRISIS.md`: Measurement methodology investigation
  - `CONSENSUS_ANALYSIS.md`: Multi-agent variance analysis

**Testing & Benchmarking:**
- `docs/testing/JMH_BENCHMARKING_GUIDE.md`: Rigorous performance measurement methodology (10K+ warmup essential)
- `docs/testing/VALIDATION_FRAMEWORK.md`: Stage-by-stage FFT validation for debugging optimizations
- `docs/testing/TESTING_COMPLIANCE.md`: Test coverage requirements and quality gates

**Audio Processing & Accuracy:**
- `docs/testing/PITCH_DETECTION_ANALYSIS.md`: **Complete pitch detection accuracy analysis** (spectral 0.92% vs YIN 40.6% error)
- `src/test/java/com/fft/analysis/PitchDetectionAccuracyTest.java`: Comprehensive accuracy test suite (4 scenarios, 10 frequencies)

**Implementation Reports:**
- `docs/implementation/P0_IMPLEMENTATION_SUMMARY.md`: JMH + accurate documentation recommendations
- `docs/implementation/P1_IMPLEMENTATION_SUMMARY.md`: Profiling + validation + test fixes (✅ COMPLETED)

**Architecture:**
- `REFACTORING_SUMMARY.md`: Phase 1-2 refactoring summary (package structure, factory pattern)

**Core Implementation:**
- `src/main/java/com/fft/core/TwiddleFactorCache.java`: Precomputed cos/sin tables (30-50% on twiddles, universal)
- `src/main/java/com/fft/cache/BitReversalCache.java`: O(n) vs O(n log n) precomputed tables (universal)
- `src/main/java/com/fft/core/FFTBase.java`: Reference implementation with universal cache optimizations
- `src/main/java/com/fft/optimized/FFTOptimized8.java`: Complete loop unrolling (1.83-1.91x verified with 10K+ warmup)
- `src/main/java/com/fft/optimized/OptimizedFFTFramework.java`: DEPRECATED (framework overhead eliminated)

**User Documentation:**
- `USER_GUIDE.md`: **Comprehensive user manual** - installation, usage, examples, troubleshooting, FAQ
- `README.md`: Project overview with quick start and features

**📦 Archived Documentation** (see `docs/archive/README.md`):
- 13 obsolete/duplicate/completed documents archived
  - October 6, 2025: 12 historical/duplicate docs
  - November 4, 2025: 1 completed planning doc (DOCUMENTATION_DEDUPLICATION_PLAN)
- Historical planning, completed work, duplicate FASE 2 docs
- Full git history preserved for reference

**Important Notes (v2.1 - January 2026):**
- **Test suite**: 414 total tests (406 passing, 8 skipped) - ✅ ALL PASSING
  - **8 skipped tests**: YIN algorithm tests disabled due to known 40.6% error on pure tones (subharmonic detection)
  - **All core tests passing**: Zero failures, zero regressions from v2.1 optimizations
  - Includes PitchDetectionAccuracyTest.java with 4 comprehensive test scenarios
  - Validates spectral method accuracy (0.92% error) vs YIN (40.6% error)
- **Pitch detection strategy**: Spectral method is primary (0.92% error), YIN is validation only
  - Spectral FFT-based method achieves 0.92% error (44x more accurate than YIN)
  - YIN (40.6% error) retained for subharmonic detection validation
  - See docs/testing/PITCH_DETECTION_ANALYSIS.md for complete accuracy analysis
- **v2.1 Optimizations Verified**:
  - FFT8: 1.83-1.91x speedup (complete loop unrolling)
  - TwiddleFactorCache: 30-50% on twiddle operations
  - BitReversalCache: O(n) vs O(n log n)
  - System.arraycopy: 33% faster initialization
  - Overall: 1.06-1.09x with zero regressions
- **Build configuration**:
  - Maven forkCount=0: Tests run in-process for faster execution
  - JaCoCo coverage enforces 90% line / 85% branch
  - SpotBugs disabled (Java 17 compatibility issue)
- **Logging**: All code uses SLF4J API with Logback configuration (test scope only)

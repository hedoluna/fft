# Contributing to FFT Library

Thank you for your interest in contributing to the FFT Library! This document provides guidelines and instructions for contributors.

## Code of Conduct

This project follows a simple principle: be respectful and professional. We welcome contributions from everyone.

## How to Contribute

### Reporting Bugs

1. Check if the bug has already been reported in the Issues section
2. If not, create a new issue with:
   - Clear description of the problem
   - Steps to reproduce
   - Expected vs actual behavior
   - System information (Java version, OS)
   - Relevant code snippets or test cases

### Suggesting Features

1. Check if the feature has already been suggested
2. Create a new issue with:
   - Clear description of the feature
   - Use case and motivation
   - Proposed implementation approach (if applicable)

### Contributing Code

## Development Setup

### Prerequisites

- **Java 17 or higher** (tested with Java 17 and 21)
- **Maven 3.6.3+**
- Git

### Getting Started

```bash
# Clone the repository
git clone https://github.com/hedoluna/fft.git
cd fft

# Build and test
mvn clean compile test

# Run specific tests
mvn test -Dtest=FFTBaseTest

# Generate coverage report
mvn clean test jacoco:report
# Open target/site/jacoco/index.html
```

## Development Guidelines

### Code Style

- Follow standard Java conventions
- Use clear, descriptive variable and method names
- Keep methods focused and concise
- Add JavaDoc for all public classes and methods
- Include code examples in JavaDoc where appropriate
- **EditorConfig**: Project includes `.editorconfig` for consistent formatting
  - Java: 4 spaces indent, max line 120 characters
  - Automatically enforced by most editors (VS Code, IntelliJ, Eclipse)
- **Logging**: Use SLF4J API for all logging
  - Production code: `LoggerFactory.getLogger(ClassName.class)`
  - Demo classes: Use `logger.info()` instead of `System.out.println()`
  - No `System.out` or `System.err` in production or demo code

### Testing Requirements

**All contributions MUST include tests:**

- Unit tests for new functionality
- Integration tests where applicable
- Property-based tests for mathematical correctness
- Performance benchmarks for optimizations

**Quality Gates:**
- **Line Coverage**: Minimum 90%
- **Branch Coverage**: Minimum 85%
- **All tests must pass**: No exceptions

```bash
# Run tests with coverage
mvn clean test jacoco:report

# View coverage report
# target/site/jacoco/index.html
```

### Performance Optimization Guidelines

If you're contributing a performance optimization:

1. **Profile First**
   - Use JMH benchmarks to identify bottlenecks
   - See `JMH_BENCHMARKING_GUIDE.md` for methodology

2. **Verify Correctness**
   - Always verify against `FFTBase` reference implementation
   - Error tolerance: < 1e-10
   - Validate Parseval's theorem (energy conservation)

3. **Measure Impact**
   - Include before/after JMH benchmark results
   - Minimum 10,000 warmup iterations
   - Report speedup with statistical variance

4. **Document Techniques**
   - Explain optimization approach in JavaDoc
   - Update `PERFORMANCE_OPTIMIZATION_STATUS.md`
   - Add lessons learned to relevant docs

**Example Benchmark:**
```bash
# Run JMH benchmarks using helper scripts
./run-jmh-benchmarks.bat FFT8           # Windows
./run-jmh-benchmarks.sh FFT8            # Linux/Mac

# With custom JMH options
./run-jmh-benchmarks.sh FFT8 -f 3 -wi 10 -i 20
```

### Adding New FFT Implementations

To add a new size-specific optimized implementation:

1. **Create Implementation Class**
   ```java
   @FFTImplementation(
       size = 128,
       priority = 50,
       description = "Optimized implementation for size 128",
       characteristics = {"loop-unrolling", "twiddle-cache"}
   )
   public class FFTOptimized128 implements FFT {
       // Implementation
   }
   ```

2. **Place in Correct Package**
   - `com.fft.optimized` for standard optimizations
   - `com.fft.experimental` for experimental work

3. **Create Corresponding Test**
   - `src/test/java/com/fft/optimized/FFTOptimized128Test.java`
   - Extend from appropriate base test class
   - Include correctness and performance tests

4. **Verify Auto-Discovery**
   ```bash
   mvn test -Dtest=FFTFactoryTest
   ```

## Pull Request Process

### Before Submitting

1. **Build and Test**
   ```bash
   mvn clean compile test
   ```

2. **Check Coverage**
   ```bash
   mvn clean test jacoco:report
   # Ensure 90%/85% line/branch coverage
   ```

3. **Run Integration Tests**
   ```bash
   mvn verify
   ```

4. **Update Documentation**
   - Update relevant markdown files
   - Add/update JavaDoc
   - Update CLAUDE.md if architecture changes

### Submitting Pull Request

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Commit Guidelines**
   - Use clear, descriptive commit messages
   - One logical change per commit
   - Reference issue numbers where applicable

   Example:
   ```
   Add radix-4 FFT16 optimization (#42)

   - Implement 2-stage radix-4 DIT algorithm
   - Achieve 1.8x speedup over FFTBase
   - Add comprehensive test coverage (95%)
   - Update performance documentation

   Closes #42
   ```

3. **Create Pull Request**
   - Describe changes clearly
   - Reference related issues
   - Include benchmark results for performance changes
   - Ensure CI passes (GitHub Actions)

4. **Code Review**
   - Address review comments promptly
   - Update tests/docs as needed
   - Keep commits clean and organized

## Continuous Integration

The project uses **GitHub Actions** for automated quality checks. All pull requests must pass:

### Automated Checks

1. **Build & Test** (ci.yml)
   - Runs on Java 17 and Java 21
   - Executes full test suite (296+ tests)
   - Generates code coverage report
   - Uploads coverage to Codecov
   - **Must pass before merge**

2. **Code Coverage**
   - Minimum 90% line coverage required
   - Minimum 85% branch coverage required
   - Coverage badge updated automatically on README.md
   - View detailed reports on Codecov

3. **Dependency Security** (dependency-check.yml)
   - Weekly automated scans for outdated dependencies
   - Checks for known vulnerabilities
   - Runs on schedule (Mondays at 00:00 UTC)

4. **API Documentation** (javadoc.yml)
   - Automatically deploys Javadoc to GitHub Pages
   - Runs on push to main branch
   - Public documentation available at project's GitHub Pages URL

### Local Pre-Commit Checks

Before pushing, run these commands locally:

```bash
# Verify build and tests
mvn clean compile test

# Check code coverage meets requirements
mvn clean test jacoco:report
# Open target/site/jacoco/index.html and verify 90%/85%

# Run integration tests
mvn verify

# (Optional) Run JMH benchmarks if performance-related changes
./run-jmh-benchmarks.bat        # Windows
./run-jmh-benchmarks.sh         # Linux/Mac
```

## Architecture Overview

For detailed architecture information, see:
- `CLAUDE.md` - Development guide
- `DOCUMENTATION_INDEX.md` - Master documentation index
- `REFACTORING_SUMMARY.md` - Architecture overview

**Key Architectural Principles:**

1. **Factory Pattern**: Auto-discovery of implementations
2. **Immutable Results**: Thread-safe `FFTResult` objects
3. **Correctness First**: Never sacrifice correctness for performance
4. **Backward Compatibility**: Maintain legacy API where possible

## Performance Optimization Status

**Current Status (October 2025):**
- FASE 1: ✅ Framework overhead eliminated (3.1x speedup)
- FASE 2: ✅ Twiddle cache + FFT8 optimization complete
- FFT8: 2.27x verified speedup
- Twiddle Cache: 30-50% universal speedup

**See PERFORMANCE_OPTIMIZATION_STATUS.md for details**

## Common Tasks

### Running Demos

```bash
# Pitch detection demo
mvn exec:java -Dexec.mainClass="com.fft.demo.PitchDetectionDemo"

# Song recognition demo
mvn exec:java -Dexec.mainClass="com.fft.demo.SongRecognitionDemo"

# Chord recognition demo
mvn exec:java -Dexec.mainClass="com.fft.demo.ChordRecognitionDemo"
```

### Debugging Tips

```bash
# Verify factory registration
mvn test -Dtest=FFTBaseTest
# Look for "Discovered FFT implementation" messages

# Enable verbose logging
mvn test -Djava.util.logging.config.file=logging.properties

# Check implementation selection
# Add to test code:
System.out.println(FFTUtils.getImplementationInfo(1024));
```

## Getting Help

- **Documentation**: See `DOCUMENTATION_INDEX.md` for all docs
- **Issues**: Check existing issues or create new one
- **Development**: See `CLAUDE.md` for development guide

## License

By contributing, you agree that your contributions will be released into the public domain under the Unlicense. See the `LICENSE` file for details.

---

**Questions?** Feel free to ask in the Issues section!

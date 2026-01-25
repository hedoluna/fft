# Agent Instructions for FFT Library

These guidelines apply to entire repository. Follow them when modifying files or creating pull requests.

## Build & Test Commands

**Build:**
```bash
mvn clean compile
```

**Test All:**
```bash
mvn test                    # Unit tests only
mvn verify                  # Unit + integration tests
```

**Single Test:**
```bash
mvn test -Dtest=FFTBaseTest
mvn test -Dtest=FFTBaseTest#testTransformPowerOfTwo
mvn test -Dtest="*Optimized*Test"  # Pattern matching
```

**Coverage:**
```bash
mvn clean test jacoco:report
```

**JMH Benchmarks (Rigorous):**
```bash
.\run-jmh-benchmarks.bat        # Windows (RECOMMENDED)
./run-jmh-benchmarks.sh         # Linux/Mac
```

**Linting:**
```bash
mvn spotbugs:check             # Disabled in CI (Java 17 incompatibility)
mvn spotbugs:spotbugs          # Manual execution only
```

## Code Style - Formatting

- **Java Version**: Java 17 required (uses Java 17 features)
- **Indentation**: 4 spaces (no tabs)
- **Max Line Length**: 120 characters
- **Line Endings**: LF (Unix-style, see .editorconfig)
- **Encoding**: UTF-8
- **Whitespace**: Trim trailing whitespace, insert final newline
- **Braces**: Preserve existing placement style

## Code Style - Imports

**Import Order:**
1. java.*
2. javax.*
3. Third-party libraries (org.*, com.* external)
4. com.fft.* (local packages)

**Rules:**
- Static imports allowed but avoid wildcard `import *` except specific cases
- Separate import groups with blank lines
- Remove unused imports

## Code Style - Naming & Types

- **Classes**: PascalCase (FFT, FFTBase, FFTResult)
- **Methods/Variables**: camelCase, explicit names over abbreviations (transform, not trnsfrm)
- **Constants**: UPPER_SNAKE_CASE (EPSILON, NORM_FACTOR)
- **Types**: Use `double` for floating-point (not float), `double[]` for arrays (not ArrayList<Double>)
- **Generics**: Explicit type arguments where clarity matters

## Code Style - Architecture Patterns

- **Interface-Based Design**: FFT interface defines contract for all implementations
- **Factory Pattern**: DefaultFFTFactory with auto-discovery via @FFTImplementation annotation
- **Immutable Results**: FFTResult objects are immutable (thread-safe, cacheable)
- **Lazy Initialization**: Double-checked locking for static fields (FFTUtils.getDefaultFactory())
- **Auto-Discovery**: Place optimized implementations in com.fft.optimized package
- **Fallback Strategy**: FFTBase supports any power-of-2 size when optimized versions unavailable

## Documentation Standards

**JavaDoc Requirements:**
- Public APIs: Comprehensive JavaDoc with @author, @since, @see tags
- Methods: @param, @return, @throws for all public/protected members
- Package-level: package-info.java with package description and usage examples
- Examples: Use @code/@pre blocks for code samples

**HTML Tags in JavaDoc:**
- Use <h3>, <ul>, <li>, <pre> for structured documentation
- See FFT.java, FFTBase.java, package-info.java for examples

**Content:**
- Describe mathematical properties (Parseval's theorem, normalization factors)
- Include performance characteristics where applicable
- Reference related classes with @see tags

## Testing Guidelines

**Framework**: JUnit 5 with AssertJ assertions

**Annotations:**
- @Test for single tests
- @BeforeEach for setup (not @Before from JUnit 4)
- @DisplayName for descriptive test names
- @ParameterizedTest + @ValueSource for parameterized tests

**Assertions (AssertJ):**
```java
assertThat(result.size()).isEqualTo(8);
assertThat(value).isCloseTo(expected, within(EPSILON));
assertThat(array).hasSize(10);
```

**Test Structure:**
- Test classes end with "Test"
- Test methods: descriptive names starting with "test" or "should"
- Constants: private static final EPSILON = 1e-10 for precision testing
- Property-based tests: Validate Parseval's theorem, energy conservation

## Error Handling

**Exceptions:**
- IllegalArgumentException for invalid inputs (null arrays, wrong lengths, non-power-of-2 sizes)
- Descriptive messages with actual values: "Array length must be a power of 2, got: " + length
- Validation at public method entry points (transform(), createFFT(), etc.)
- No unchecked exceptions in core FFT logic

**Examples:**
```java
if (real.length != imaginary.length) {
    throw new IllegalArgumentException("Real and imaginary arrays must have same length");
}
if (!isPowerOfTwo(real.length)) {
    throw new IllegalArgumentException("Array length must be a power of 2, got: " + real.length);
}
```

## Performance Guidelines

**Critical Principles:**
1. **Profile First, Optimize Second**: See docs/performance/FASE_2_LESSONS_LEARNED.md
2. **Test in Production Context**: Micro-benchmarks ≠ real-world performance (butterfly optimization lesson)
3. **Proper Warmup**: JMH requires 10,000+ iterations for accurate measurements

**What Works (Proven):**
- Precomputed caches: TwiddleFactorCache (30-50% twiddle speedup), BitReversalCache (O(n) vs O(n log n))
- System.arraycopy: 33% faster than manual loop for array copying
- Complete loop unrolling: FFT8 with hardcoded twiddles (1.83-1.91x verified)

**What Doesn't Work:**
- Adding local variables in tight loops (register pressure → stack spilling)
- Manual unrolling beyond FFT8 (diminishing returns)
- Isolated micro-benchmarks without production testing

**Benchmarking:**
- Use helper scripts: .\run-jmh-benchmarks.bat (Windows) or ./run-jmh-benchmarks.sh (Linux/Mac)
- Never use mvn exec:java for JMH (resource loading fails)
- Warmup: 10,000+ iterations essential for optimized code

## Commit & PR Guidelines

**Commit Messages:**
- Summary line: Max 72 characters, imperative mood ("Add feature" not "Added feature")
- Optional blank line followed by detailed explanation
- Reference affected modules/features where relevant

**Pull Request Format:**
```
Summary:
[Describe changes in 2-3 sentences, referencing modified files]

Testing:
mvn test -Dtest=FFTBaseTest
[or]
mvn clean test
Result: 406 tests passing, 8 skipped, 0 failures
```

**File References:**
- Use repository-relative paths (src/main/java/com/fft/core/FFTBase.java)

## Critical Repository Notes

**Build Status:**
- Java 17 + Maven 3.6.3
- 414 total tests (406 passing, 8 skipped)
- Zero failures, zero regressions from v2.1 optimizations
- Coverage: JaCoCo enforces 90% line / 85% branch

**Skipped Tests:**
- 8 YIN algorithm tests disabled (40.6% error on pure tones - known limitation)
- See docs/testing/PITCH_DETECTION_ANALYSIS.md for complete analysis

**Pitch Detection Strategy:**
- Spectral FFT method: PRIMARY (0.92% error, 44x more accurate)
- YIN algorithm: VALIDATION ONLY (40.6% error, prone to subharmonic detection)

**Performance v2.1:**
- Overall: 1.06-1.09x speedup (6-9% improvement)
- FFTOptimized8: 1.83-1.91x via complete loop unrolling with hardcoded twiddles
- All other sizes: Use FFTBase with TwiddleFactorCache and BitReversalCache
- Zero regressions from optimization campaign

**Implementation Hierarchy:**
- FFTOptimized8: Only size-specific optimized implementation (1.83-1.91x)
- FFTBase: Generic fallback for all other sizes with universal cache optimizations
- Factory: Automatic selection based on size and priority

**Important:**
- NEVER modify FFTBase without comprehensive testing (it's the correctness baseline)
- Always verify optimizations against FFTBase before merging
- Spectral method is primary pitch detection, YIN is validation only

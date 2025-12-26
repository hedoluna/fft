# AGENTS.md - Agentic Coding Guidelines

## Build/Lint/Test Commands
- **Build**: `mvn clean compile`
- **Test all**: `mvn test` (unit tests) or `mvn verify` (with integration tests)
- **Single test**: `mvn test -Dtest=ClassName` or `mvn test -Dtest=ClassName#testMethod`
- **Coverage**: `mvn clean test jacoco:report`
- **Lint**: `mvn spotbugs:check` (manual, disabled in CI due to Java 17 incompatibility)

## Code Style Guidelines
- **Language**: Java 17 with Maven build system
- **Formatting**: 4-space indentation, max line length 120 chars (.editorconfig enforced)
- **Imports**: java.*, javax.*, third-party, then local packages (com.fft.*)
- **Naming**: Explicit variable names over abbreviations, camelCase for methods/variables, PascalCase for classes
- **Documentation**: Comprehensive JavaDoc for public APIs with @author, @since, @see tags
- **Error Handling**: IllegalArgumentException for invalid inputs, checked exceptions where appropriate
- **Architecture**: Interface-based design, factory pattern, immutable FFTResult objects
- **Testing**: JUnit 5 with AssertJ assertions, descriptive @DisplayName annotations, 90% line/85% branch coverage required
- **Logging**: SLF4J API (production), Logback (test scope only)

## Implementation Guidelines
- Factory pattern with auto-discovery via @FFTImplementation annotations
- Maintain backward compatibility with legacy FFTUtils API
- Verify correctness against FFTBase before performance optimization
- Use TwiddleFactorCache for precomputed cos/sin tables (30-50% speedup)
- Spectral pitch detection primary (0.92% error), YIN validation only (40.6% error)</content>
<parameter name="filePath">AGENTS.md
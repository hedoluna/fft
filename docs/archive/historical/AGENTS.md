# AGENT Instructions

These guidelines apply to the entire repository. Follow them whenever modifying files or creating pull requests.

## Build/Lint/Test Commands
- **Build**: `mvn clean compile`
- **Test all**: `mvn test` (unit tests) or `mvn verify` (with integration tests)
- **Single test**: `mvn test -Dtest=ClassName` or `mvn test -Dtest=ClassName#testMethod`
- **Lint**: `mvn spotbugs:check`
- **Coverage**: `mvn clean test jacoco:report`

## Code Style Guidelines
- **Language**: Java 17 with Maven build system
- **Imports**: java.*, javax.*, third-party, then local packages (com.fft.*)
- **Formatting**: 4-space indentation, preserve existing brace placement
- **Naming**: Explicit variable names over abbreviations, camelCase for methods/variables
- **Documentation**: Comprehensive JavaDoc for public APIs with @author, @since, @see tags
- **Error Handling**: IllegalArgumentException for invalid inputs, checked exceptions where appropriate
- **Architecture**: Interface-based design, factory pattern, immutable result objects
- **Testing**: JUnit 5 with AssertJ assertions, descriptive @DisplayName annotations

## Commit Messages
- Use a short summary line in the imperative mood (max 72 characters).
- Provide a blank line followed by an optional detailed explanation.
- Reference affected modules or features where relevant.

## Pull Request Messages
- Include **Summary** and **Testing** sections.
- Summaries must reference any modified files using repository-relative paths.
- The Testing section must mention the command used to run tests and whether they passed.


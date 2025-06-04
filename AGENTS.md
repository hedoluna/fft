# AGENT Instructions

These guidelines apply to the entire repository. Follow them whenever modifying files or creating pull requests.

## Commit Messages
- Use a short summary line in the imperative mood (max 72 characters).
- Provide a blank line followed by an optional detailed explanation.
- Reference affected modules or features where relevant.

## Pull Request Messages
- Include **Summary** and **Testing** sections.
- Summaries must reference any modified files using repository-relative paths.
- The Testing section must mention the command used to run tests and whether they passed.

## Testing
- Run `mvn -q test` before committing. If tests fail or cannot be executed, note this in the PR Testing section.

## Code Style
- Preserve existing formatting and indentation.
- Prefer explicit variable names over abbreviations.


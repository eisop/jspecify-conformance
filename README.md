## JSpecify Conformance Tests with the EISOP Checker Framework

This repository runs the [JSpecify conformance tests](https://github.com/jspecify/jspecify/tree/main/conformance-tests)
with the [EISOP Nullness Checker](https://eisop.github.io/cf/manual/#nullness-checker).

## Prerequisites

- Java 8 or higher

## Running the Tests

1. Clone the repository:
   ```bash
   git clone https://github.com/eisop/jspecify-conformance.git
   cd jspecify-conformance
   ```

2. Gradle Assemble:
   ```bash
   ./gradlew assmble
   ```

3. Run the Tests:
   ```bash
   ./gradlew test
   ```

The project uses a custom test runner, `ConformanceTestRunner`, which is invoked in the `NullnessJSpecifyConformanceTest`.
This runner checks the conformance of the EISOP Nullness Checker against the JSpecify Conformance Tests.

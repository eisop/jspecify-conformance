## JSpecify Conformance Tests

This repository specifically runs the EISOP Nullness Checker using the JSpecify conformance tests.

## Prerequisites

- Java 8 or higher

## Running the Tests

1. **Clone the repository:**
   ```bash
   git clone https://github.com/eisop/jspecify-conformance.git
   cd jspecify-conformance
2. **Gradle Assemble:**
   ```bash
   ./gradlew assmble
3. **Run the Test：**
   ```bash
   ./gradlew test

The project uses a custom test runner, **ConformanceTestRunner**, which is invoked in the **NullnessJSpecifyConformanceTest.java**. This runner will then check the conformance of the specified tools against the JSpecify annotations.

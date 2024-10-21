package conformance;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.checkerframework.checker.nullness.NullnessChecker;
import org.checkerframework.framework.test.TestConfiguration;
import org.checkerframework.framework.test.TestConfigurationBuilder;
import org.checkerframework.framework.test.TestUtilities;
import org.checkerframework.framework.test.TypecheckExecutor;
import org.checkerframework.framework.test.TypecheckResult;
import org.jspecify.conformance.ConformanceTestRunner;
import org.jspecify.conformance.ReportedFact;
import org.junit.jupiter.api.Test;

/** A class to run the conformance tests against the EISOP Checker Framework. */
public final class NullnessJSpecifyConformanceTest {

  /** Options to pass to the checker. */
  private static final ImmutableList<String> TEST_OPTIONS =
      ImmutableList.of("-AassumePure", "-Adetailedmsgtext");


  /** Run the conformance tests. */
  @Test
  public void conformanceTests() throws IOException {
    runConformanceTests(
      "ConformanceTest.inputs",
      "ConformanceTest.report",
      "ConformanceTest.deps");
  }

  /** Run the conformance tests on the samples. */
  @Test
  public void conformanceTestsOnSamples() throws IOException {
    runConformanceTests(
      "ConformanceTest.samples.inputs",
      "ConformanceTest.samples.report",
      null);  // No deps needed for conformance samples
  }

  /**
   * Runs the conformance tests with the specified test directory and report path.
   *
   * @param testDirProperty the system property key for the test directory path
   * @param reportPathProperty the system property key for the report file path
   * @param depsProperty the system property key for dependencies, or null if no dependencies are required
   */
  private void runConformanceTests(String testDirProperty, String reportPathProperty, String depsProperty) throws IOException {
      Path testDir = getSystemPropertyPath(testDirProperty);
      Path reportPath = getSystemPropertyPath(reportPathProperty);
      ImmutableList<Path> deps = depsProperty != null ?
        Splitter.on(":").splitToList(depsProperty).stream().map(Paths::get).collect(toImmutableList()) :
          ImmutableList.of(); // for conformance samples, creates an empty immutable list

      ConformanceTestRunner runner = new ConformanceTestRunner(NullnessJSpecifyConformanceTest::analyze);
      runner.checkConformance(testDir, deps, reportPath);
  }

  /**
   * Get an equivalent path from a system property.
   *
   * @param propertyName the name of the property.
   */
  private Path getSystemPropertyPath(String propertyName) {
    String path = System.getProperty(propertyName);
    if (path == null) {
      throw new IllegalArgumentException("System property " + propertyName + " not set");
    }
    return Paths.get(path);
  }

  /**
   * Analyze the conformance tests by comparing reported facts against expected facts.
   *
   * @param testDir the directory of the conformance tests.
   * @param files the files to analyze.
   * @param deps the dependencies of the conformance tests.
   */
  private static ImmutableSet<ReportedFact> analyze(
      Path testDir, ImmutableSortedSet<Path> files, ImmutableList<Path> deps) {
    ImmutableSet<File> fileInputs = files.stream().map(Path::toFile).collect(toImmutableSet());

    ImmutableList<String> depsAsStrings =
        deps.stream().map(Path::toString).collect(toImmutableList());

    TestConfiguration testConfig =
        TestConfigurationBuilder.buildDefaultConfiguration(
            null,
            fileInputs,
            depsAsStrings,
            ImmutableList.of(NullnessChecker.class.getName()),
            TEST_OPTIONS,
            TestUtilities.getShouldEmitDebugInfo());
    TypecheckExecutor typecheckExecutor = new TypecheckExecutor();
    TypecheckResult result = typecheckExecutor.runTest(testConfig);
    ImmutableSet<ReportedFact> reportedFacts =
        result.getUnexpectedDiagnostics().stream()
            .map(
                diagnostic ->
                    new ReportedFactMessage(
                        Path.of(diagnostic.getFilename()),
                        diagnostic.getLineNumber(),
                        diagnostic.getMessage()))
            .collect(toImmutableSet());
    return reportedFacts;
  }
}

/** A reported fact with a message as a string. */
final class ReportedFactMessage extends ReportedFact {

  /** The message that the fact describes. */
  private final String message;

  /**
   * Create a ReportedFactMessage.
   *
   * @param filename the file that the fact is in.
   * @param lineNumber the line number of the fact.
   * @param message the message that the fact describes.
   */
  ReportedFactMessage(Path filename, long lineNumber, String message) {
    super(filename, lineNumber);
    this.message = requireNonNull(message);
  }

  /** Indicates if the fact must be an expected fact. */
  @Override
  protected boolean mustBeExpected() {
    return false;
  }

  /** Get the message that the fact describes. */
  @Override
  protected String getFactText() {
    return message;
  }
}

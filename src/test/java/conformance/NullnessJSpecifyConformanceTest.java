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

  /** Directory of the JSpecify Conformance Tests. */
  private final Path testDir;

  /** Location of the report. */
  private final Path reportPath;

  /** JSpecify conformance test dependencies. */
  private final ImmutableList<Path> deps;

  /** Options to pass to the checker. */
  private static final ImmutableList<String> TEST_OPTIONS =
      ImmutableList.of("-AassumePure", "-Adetailedmsgtext");

  /** Create a NullnessJSpecifyConformanceTest. */
  public NullnessJSpecifyConformanceTest() {
    this.testDir = getSystemPropertyPath("ConformanceTest.inputs");
    this.reportPath = getSystemPropertyPath("ConformanceTest.report");
    this.deps =
        Splitter.on(":").splitToList(System.getProperty("ConformanceTest.deps")).stream()
            .map(dep -> Paths.get(dep))
            .collect(toImmutableList());
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

  /** Run the conformance tests. */
  @Test
  public void conformanceTests() throws IOException {
    ConformanceTestRunner runner =
        new ConformanceTestRunner(NullnessJSpecifyConformanceTest::analyze);
    runner.checkConformance(testDir, deps, reportPath);
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

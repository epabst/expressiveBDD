package geeks.jcucumber;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.StringReader;
import java.net.URL;

import geeks.expressive.Scopes;
import geeks.expressive.Scope;
import geeks.jcucumber.steps.CalculatorSteps;
import geeks.jcucumber.steps.StepsRequiringNonStepComponent;
import cuke4duke.Given;

/**
 * A test to show support for doing what Cucumber does.
 *
 * @author pabstec
 */
public class TestJCucumber {
  private StringWriter stringWriter;
  private WriterResultPublisher resultPublisher;
  private JCucumber cucumber;

  @Test
  public void shouldSupportCucumber() throws IOException {
    URL feature1Url = getClass().getResource("features/Addition.feature");
    assertNotNull(feature1Url, "features/Addition.feature should be available");
    URL feature2Url = getClass().getResource("features/Division.feature");
    assertNotNull(feature2Url, "features/Division.feature should be available");

    Scope scope = Scopes.asScope(CalculatorSteps.class.getPackage());
    cucumber.run(feature1Url, scope);
    println(stringWriter.toString());
    assertEquals(resultPublisher.getScenarioCount(), 4);
    assertEquals(resultPublisher.getFailedCount(), 1);
    String output = stringWriter.toString();
    assertSubstring(output, "Feature: Addition Using the Calculator");
    assertSubstring(output, "Scenario: 1+1");
    assertSubstring(output, "Then the result should be \"3\"");
    assertSubstring(output, "FAILED:    Then the result should be \"1\"");

    cucumber.run(feature2Url, scope);
    println(stringWriter.toString());
    assertEquals(resultPublisher.getScenarioCount(), 4 + 3);
    assertEquals(resultPublisher.getFailedCount(), 1 + 1);
    output = stringWriter.toString();
    assertSubstring(output, "Feature: Division Using the Calculator");
    assertSubstring(output, "Scenario: intentionally cause a problem");
    assertSubstring(output, "FAILED:    When I push \"/\"");
  }

  private void println(String string) {
    System.out.println(string);
    System.out.flush();
  }

  @Test
  public void shouldNotEatSeriousError() throws IOException {
    StringReader reader = new StringReader(
            "Scenario: Serious Error\n" +
            "  Given a serious error occurs");
    Scope scope = Scopes.asScope(TestJCucumber.class);
    try {
      cucumber.run(reader, scope);
      fail("expected exception");
    }
    catch (OutOfMemoryError e) {
      assertEquals("false alarm", e.getMessage());
    }
  }

  @Test
  public void shouldIgnoreTags() throws IOException {
    cucumber.run(new StringReader(
            "Feature: Ignore tags\n" +
            "  @scenario1\n" +
            "  Scenario: some prior scenario\n" +
            "    Given no more work to do\n" +
            "\n" +
            "  @mytag\n" +
            "  Scenario: some scenario\n" +
            "    Given no more work to do"), Scopes.asScope(CalculatorSteps.class));
    assertEquals(resultPublisher.getScenarioCount(), 2, stringWriter.toString());
    assertEquals(resultPublisher.getFailedCount(), 0, stringWriter.toString());
  }

  @Test
  public void shouldIgnoreExtraTextLines() throws IOException {
    cucumber.run(new StringReader(
            "Feature: Ignore Text Lines\n" +
            "  extra text\n" +
            "  Scenario: some prior scenario\n" +
            "    Given no more work to do\n" +
            "\n" +
            "  more extra text\n" +
            "  Scenario: some scenario\n" +
            "    Given no more work to do"), Scopes.asScope(CalculatorSteps.class));
    assertEquals(resultPublisher.getScenarioCount(), 2, stringWriter.toString());
    assertEquals(resultPublisher.getFailedCount(), 0, stringWriter.toString());
  }

  @Test
  public void shouldSupportNonStepComponents() throws IOException {
    cucumber.run(new StringReader(
            "Feature: Support Non-Step Components\n" +
            "  Scenario: #1\n" +
            "    Given a non-step component is required"), Scopes.asScope(StepsRequiringNonStepComponent.class));
    assertEquals(resultPublisher.getScenarioCount(), 1, stringWriter.toString());
    assertEquals(resultPublisher.getFailedCount(), 0, stringWriter.toString());
  }

  @Given("a serious error occurs")
  public void aSeriousErrorOccurs() {
    throw new OutOfMemoryError("false alarm");
  }

  private void assertSubstring(String string, String expectedSubstring) {
    assertTrue(string.contains(expectedSubstring), "Expected '" + expectedSubstring + "' within '" + string + "'");
  }

  @BeforeMethod
  protected void setUp() throws Exception {
    stringWriter = new StringWriter();
    resultPublisher = new WriterResultPublisher(stringWriter);
    cucumber = new JCucumber(resultPublisher);
  }
}

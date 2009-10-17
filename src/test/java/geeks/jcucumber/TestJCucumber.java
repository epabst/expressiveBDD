package geeks.jcucumber;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import geeks.expressive.Scopes;
import geeks.jcucumber.steps.CalculatorSteps;

/**
 * A test to show support for doing what Cucumber does.
 *
 * @author pabstec
 */
public class TestJCucumber {
  @Test
  public void shouldSupportCucumber() throws IOException {
    StringWriter stringWriter = new StringWriter();
    WriterResultPublisher resultPublisher = new WriterResultPublisher(stringWriter);
    JCucumber cucumber = new JCucumber(resultPublisher);
    URL featureUrl = getClass().getResource("features/Addition.feature");
    assertNotNull(featureUrl, "features/Addition.feature should be available");

    cucumber.run(featureUrl, Scopes.asScope(CalculatorSteps.class.getPackage()));
    System.out.println(stringWriter.toString());
    assertEquals(resultPublisher.getTestCount(), 4);
    assertEquals(resultPublisher.getFailedCount(), 1);
    String output = stringWriter.toString();
    assertSubstring(output, "Feature: Addition Using the Calculator");
    assertSubstring(output, "Scenario: 1+1");
    assertSubstring(output, "Then the result should be \"3\"");
    assertSubstring(output, "FAILED:    Then the result should be \"1\"");
  }

  private void assertSubstring(String string, String expectedSubstring) {
    assertTrue(string.contains(expectedSubstring), "Expected '" + expectedSubstring + "' within '" + string + "'");
  }
}

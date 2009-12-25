package geeks.jcucumber;

import java.io.Writer;
import java.io.PrintWriter;

/**
 * A {@link geeks.jcucumber.ResultPublisher} that prints to a {@link Writer}.
 *
 * @author pabstec
 */
public class WriterResultPublisher implements ResultPublisher {
  private int scenarioCount = 0;
  private int failedCount = 0;
  private final PrintWriter writer;
  private String scenarioName;

  public WriterResultPublisher(Writer outputWriter) {
    writer = new PrintWriter(outputWriter);
  }

  public void stepPassed(String string) {
    writeln("PASSED:" + string);
  }

  public void stepFailed(String string, Throwable throwable) {
    writeln("FAILED:" + string);
    throwable.printStackTrace(writer);
  }

  public void writeln(String string) {
    writer.println(string);
  }

  public void startScenario(String scenarioName) {
    this.scenarioName = scenarioName;
    writeln("********************");
    writeln(scenarioName);
    writeln("********************");
  }

  public void succeeded() {
    writeln("  " + scenarioName + " succeeded :)");
    scenarioCount++;
  }

  public void failed() {
    writeln("  " + scenarioName + " failed :(");
    scenarioCount++;
    failedCount++;
  }

  public int getScenarioCount() {
    return scenarioCount;
  }

  public int getFailedCount() {
    return failedCount;
  }

  public void finished() {
    writeln("-----------------------------------------------");
    writeln("Total scenarios: " + getScenarioCount() + "   Failed: " + getFailedCount());
  }
}

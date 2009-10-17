package geeks.jcucumber;

/**
 * A Cucumber result publisher.
 *
 * @author pabstec
 */
public interface ResultPublisher {
  void stepPassed(String string);

  void stepFailed(String string, Throwable throwable);

  void writeln(String string);

  void startScenario(String scenarioName);

  void succeeded();

  void failed();

  void finished();
}

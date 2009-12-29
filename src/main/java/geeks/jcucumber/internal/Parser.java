package geeks.jcucumber.internal;

import geeks.jcucumber.ResultPublisher;
import geeks.expressive.*;
import cuke4duke.StepMother;
import cuke4duke.Before;
import cuke4duke.After;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
   * A Parser for Cucumber feature files.
 *
 * @author pabstec
 */

public class Parser {
  private final JCucumberStepMother stepMother;
  private final ResultPublisher resultPublisher;
  private Mode mode = Mode.NONE;
  private int stepFailedCountForScenario;
  public static final AnnotationMethodRegexAssociation COMMAND_ASSOCIATION = new AnnotationMethodRegexAssociation(Command.class);
  private static final AnnotationMethodSpecifier BEFORE_SPECIFIER = new AnnotationMethodSpecifier(Before.class);
  private static final AnnotationMethodSpecifier AFTER_SPECIFIER = new AnnotationMethodSpecifier(After.class);

  public Parser(ResultPublisher resultPublisher, final Scope stepsScope) {
    ObjectFactory stepsObjectFactory = new DefaultObjectFactory();
    stepsObjectFactory.addInstance(Expressive.class, new Expressive(stepsObjectFactory));
    stepsObjectFactory.addInstance(Scope.class, stepsScope);
    this.stepMother = stepsObjectFactory.getInstance(JCucumberStepMother.class);
    //make StepMother available for constructor injection of steps components
    stepsObjectFactory.addInstance(StepMother.class, stepMother);
    this.resultPublisher = resultPublisher;
  }

  private void setMode(Mode mode) {
    if ((this.mode == Mode.IN_SCENARIO_AFTER_WHEN || this.mode == Mode.IN_SCENARIO_BEFORE_WHEN) && mode == Mode.IN_FEATURE) {
      if (stepFailedCountForScenario != 0) {
        resultPublisher.failed();
      }
      else {
        resultPublisher.succeeded();
      }
      stepMother.invokeEvent(AFTER_SPECIFIER);
    }
    else if (this.mode == Mode.IN_FEATURE && mode == Mode.IN_SCENARIO_BEFORE_WHEN) {
      stepFailedCountForScenario = 0;
      stepMother.invokeEvent(BEFORE_SPECIFIER);
    }
    this.mode = mode;
  }

  @Command("^(Feature: .*)$")
  public void feature(String feature) {
    assertMode(Mode.NONE, feature);
    resultPublisher.writeln(feature);
    setMode(Mode.IN_FEATURE);
  }

  @Command("^(\\s*Scenario: .*)$")
  public void scenario(String scenario) {
    setMode(Mode.IN_FEATURE);
    resultPublisher.startScenario(scenario);
    setMode(Mode.IN_SCENARIO_BEFORE_WHEN);
  }

  @Command("^(\\s*Given (.*))$")
  public void given(String string, String step) {
    assertMode(Mode.IN_SCENARIO_BEFORE_WHEN, string);
    executeStepAndWriteString(string, step, JCucumberStepMother.GIVEN_ASSOCIATION);
  }

  @Command("^(\\s*When (.*))$")
  public void when(String string, String step) {
    assertMode(Mode.IN_SCENARIO_BEFORE_WHEN, string);
    executeStepAndWriteString(string, step, JCucumberStepMother.WHEN_ASSOCIATION);
    setMode(Mode.IN_SCENARIO_AFTER_WHEN);
  }

  @Command("^(\\s*Then (.*))$")
  public void then(String string, String step) {
    assertMode(Mode.IN_SCENARIO_AFTER_WHEN, string);
    executeStepAndWriteString(string, step, JCucumberStepMother.THEN_ASSOCIATION);
  }

  @Command("^(\\s*And (.*))$")
  public void and(String string, String step) {
    if (mode != Mode.IN_SCENARIO_AFTER_WHEN) {
      assertMode(Mode.IN_SCENARIO_BEFORE_WHEN, string);
      given(string, step);
    } else {
      then(string, step);
    }
  }

  @Command("^\\s*$")
  public void blankLine() {
    //do nothing
  }

  @Command(Expressive.EVERYTHING_ELSE_REGEX)
  public void everythingElse(String line) {
    resultPublisher.writeln(line);
  }

  private void executeStepAndWriteString(String stepLine, String step, AnnotationMethodRegexAssociation annotationAssociation) {
    try {
      stepMother.invoke(step, annotationAssociation);
      resultPublisher.stepPassed(stepLine);
    } catch (Exception e) {
      stepFailedCountForScenario++;
      resultPublisher.stepFailed(stepLine, e);
    } catch (AssertionError e) {
      stepFailedCountForScenario++;
      resultPublisher.stepFailed(stepLine, e);
    } catch (Error e) {
      stepFailedCountForScenario++;
      resultPublisher.stepFailed(stepLine, e);
      if (e.getClass().getName().startsWith("java.lang.")) {
        throw e;
      }
    }
  }

  private void assertMode(Mode expectedMode, String statement) {
    if (mode != expectedMode) {
      throw new AssertionError("expected the mode to be " + expectedMode + " but it was " + mode + " for '" + statement + "'");
    }
  }

  public void finished() {
    setMode(Mode.IN_FEATURE);
    setMode(Mode.NONE);
    resultPublisher.finished();
  }

  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  private static @interface Command {
    /**
     * The regular expression to match against for the target method to be used.
     * @return the regular expression string
     */
    public abstract String value();
  }

  private static enum Mode {
    NONE, IN_FEATURE, IN_SCENARIO_BEFORE_WHEN, IN_SCENARIO_AFTER_WHEN
  }
}

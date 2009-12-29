package geeks.jcucumber;

import java.net.URL;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import geeks.expressive.*;
import cuke4duke.*;

/**
 * A simplistic implementation of Cucumber.
 *
 * @author pabstec
 */
public class JCucumber {
  private static final AnnotationMethodRegexAssociation COMMAND_ASSOCIATION = new AnnotationMethodRegexAssociation(Command.class);
  private static final AnnotationMethodRegexAssociation GIVEN_ASSOCIATION = new AnnotationMethodRegexAssociation(Given.class);
  private static final AnnotationMethodRegexAssociation WHEN_ASSOCIATION = new AnnotationMethodRegexAssociation(When.class);
  private static final AnnotationMethodRegexAssociation THEN_ASSOCIATION = new AnnotationMethodRegexAssociation(Then.class);
  private static final AnnotationMethodRegexAssociation TRANSFORM_ASSOCIATION = new AnnotationMethodRegexAssociation(Transform.class);
  private static final AnnotationMethodSpecifier BEFORE_SPECIFIER = new AnnotationMethodSpecifier(Before.class);
  private static final AnnotationMethodSpecifier AFTER_SPECIFIER = new AnnotationMethodSpecifier(After.class);
  private final ResultPublisher resultPublisher;
  private final Scope parserScope;

  public JCucumber(ResultPublisher resultPublisher) {
    this.resultPublisher = resultPublisher;
    this.parserScope = Scopes.asScope(Parser.class);
  }

  public void run(URL featureResource, Scope stepsScope) throws IOException {
    run(new InputStreamReader(featureResource.openStream(), "UTF-8"), stepsScope);
  }

  public void run(Reader reader, Scope stepsScope) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(reader);
    Parser parser = new Parser(resultPublisher, stepsScope);
    ObjectFactory parserObjectFactory = new DefaultObjectFactory();
    parserObjectFactory.addInstance(Parser.class, parser);
    new Expressive(parserObjectFactory).execute(bufferedReader, COMMAND_ASSOCIATION, TRANSFORM_ASSOCIATION, parserScope);
    parser.finished();
  }

  /**
   * A context for running a JCucumber scenario.
   *
   * @author pabstec
   */
  static class ScenarioContext {
    private final Expressive expressive;
    private final Scope stepsScope;

    public ScenarioContext(final Scope stepsScope, ObjectFactory objectFactory) {
      this.stepsScope = stepsScope;
      expressive = new Expressive(objectFactory);
    }

    protected static void execute(Expressive expressive, Scope stepsScope, String step, MethodRegexAssociation regexAssociation) {
      expressive.execute(step, regexAssociation, TRANSFORM_ASSOCIATION, stepsScope);
    }

    protected static void executeEvent(Expressive expressive, Scope stepsScope, MethodSpecifier eventSpecifier) {
      expressive.executeEvent(eventSpecifier, stepsScope);
    }
  }

  private static class JCucumberStepMother implements StepMother {
    private final ScenarioContext scenarioContext;

    public JCucumberStepMother(ScenarioContext scenarioContext) {
      this.scenarioContext = scenarioContext;
    }

    public void invoke(String step) {
      MethodRegexAssociation regexAssociation = new CompositeMethodRegexAssociation(
              GIVEN_ASSOCIATION, WHEN_ASSOCIATION, THEN_ASSOCIATION);
      ScenarioContext.execute(scenarioContext.expressive, scenarioContext.stepsScope, step, regexAssociation);
    }

    public void invoke(String step, Table table) {
      throw new UnsupportedOperationException("not implemented yet");
    }

    public void invoke(String step, String multiLineString) {
      throw new UnsupportedOperationException("not implemented yet");
    }
  }

  /**
   * A Parser for Cucumber feature files.
   *
   * @author pabstec
   */

  private static class Parser {
    private Mode mode = Mode.NONE;
    private final ResultPublisher resultPublisher;
    private int stepFailedCountForScenario;
    private final ScenarioContext scenarioContext;

    private Parser(ResultPublisher resultPublisher, final Scope stepsScope) {
      ObjectFactory objectFactory = new DefaultObjectFactory();
      scenarioContext = new ScenarioContext(stepsScope, objectFactory);
      StepMother stepMother = new JCucumberStepMother(scenarioContext);
      objectFactory.addInstance(StepMother.class, stepMother);
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
        ScenarioContext.executeEvent(scenarioContext.expressive, scenarioContext.stepsScope, AFTER_SPECIFIER);
      }
      else if (this.mode == Mode.IN_FEATURE && mode == Mode.IN_SCENARIO_BEFORE_WHEN) {
        stepFailedCountForScenario = 0;
        ScenarioContext.executeEvent(scenarioContext.expressive, scenarioContext.stepsScope, BEFORE_SPECIFIER);
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
      executeStepAndWriteString(string, step, GIVEN_ASSOCIATION);
    }

    @Command("^(\\s*When (.*))$")
    public void when(String string, String step) {
      assertMode(Mode.IN_SCENARIO_BEFORE_WHEN, string);
      executeStepAndWriteString(string, step, WHEN_ASSOCIATION);
      setMode(Mode.IN_SCENARIO_AFTER_WHEN);
    }

    @Command("^(\\s*Then (.*))$")
    public void then(String string, String step) {
      assertMode(Mode.IN_SCENARIO_AFTER_WHEN, string);
      executeStepAndWriteString(string, step, THEN_ASSOCIATION);
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
        ScenarioContext.execute(scenarioContext.expressive, scenarioContext.stepsScope, step, annotationAssociation);
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

    private void finished() {
      setMode(Mode.IN_FEATURE);
      setMode(Mode.NONE);
      resultPublisher.finished();
    }

  }

  private static class CompositeMethodRegexAssociation implements MethodRegexAssociation {
    private final List<MethodRegexAssociation> delegates;

    public CompositeMethodRegexAssociation(MethodRegexAssociation... delegates) {
      this(Arrays.asList(delegates));
    }

    public CompositeMethodRegexAssociation(List<MethodRegexAssociation> delegates) {
      this.delegates = delegates;
    }

    public String findRegex(Method method) {
      for (MethodRegexAssociation association : delegates) {
        String regex = association.findRegex(method);
        if (regex != null) {
          return regex;
        }
      }
      return null;
    }

    public Set<Method> getMethods(Scope scope) {
      Set<Method> methods = new HashSet<Method>();
      for (MethodRegexAssociation association : delegates) {
        methods.addAll(association.getMethods(scope));
      }
      return methods;
    }
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

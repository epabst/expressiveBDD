package geeks.jcucumber.internal;

import geeks.expressive.*;

/**
   * A context for running a JCucumber scenario.
 *
 * @author pabstec
 */
public class ScenarioContext {
  //todo make private
  public final Expressive expressive;
  //todo make private
  public final Scope stepsScope;

  public ScenarioContext(final Scope stepsScope, ObjectFactory objectFactory) {
    this.stepsScope = stepsScope;
    expressive = new Expressive(objectFactory);
  }

  public static void execute(Expressive expressive, Scope stepsScope, String step, MethodRegexAssociation regexAssociation) {
    expressive.execute(step, regexAssociation, JCucumberStepMother.TRANSFORM_ASSOCIATION, stepsScope);
  }

  public static void executeEvent(Expressive expressive, Scope stepsScope, MethodSpecifier eventSpecifier) {
    expressive.executeEvent(eventSpecifier, stepsScope);
  }
}

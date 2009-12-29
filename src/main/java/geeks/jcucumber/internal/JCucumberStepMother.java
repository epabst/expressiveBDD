package geeks.jcucumber.internal;

import cuke4duke.*;
import geeks.jcucumber.Transform;
import geeks.expressive.MethodRegexAssociation;
import geeks.expressive.AnnotationMethodRegexAssociation;
import geeks.expressive.AnnotationMethodSpecifier;
import geeks.expressive.Scope;

import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Method;

/**
 * A JCucumber implementation of a {@link StepMother}.
 *
 * @author pabstec
 */
public class JCucumberStepMother implements StepMother {
  private final ScenarioContext scenarioContext;
  public static final AnnotationMethodRegexAssociation GIVEN_ASSOCIATION = new AnnotationMethodRegexAssociation(Given.class);
  public static final AnnotationMethodRegexAssociation WHEN_ASSOCIATION = new AnnotationMethodRegexAssociation(When.class);
  public static final AnnotationMethodRegexAssociation THEN_ASSOCIATION = new AnnotationMethodRegexAssociation(Then.class);
  public static final AnnotationMethodRegexAssociation TRANSFORM_ASSOCIATION = new AnnotationMethodRegexAssociation(Transform.class);
  public static final AnnotationMethodSpecifier BEFORE_SPECIFIER = new AnnotationMethodSpecifier(Before.class);
  public static final AnnotationMethodSpecifier AFTER_SPECIFIER = new AnnotationMethodSpecifier(After.class);

  public JCucumberStepMother(ScenarioContext scenarioContext) {
    this.scenarioContext = scenarioContext;
  }

  public void invoke(String step) {
    MethodRegexAssociation regexAssociation = new CompositeMethodRegexAssociation(
            GIVEN_ASSOCIATION, WHEN_ASSOCIATION, THEN_ASSOCIATION);
    ScenarioContext.execute(scenarioContext.expressive, scenarioContext.stepsScope, step, regexAssociation);
  }

  /**
   * Not supported yet.
   * @param step a step
   * @param table a Table
   */
  public void invoke(String step, Table table) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  /**
   * Not supported yet.
   * @param step a step
   * @param multiLineString a multi-line String
   */
  public void invoke(String step, String multiLineString) {
    throw new UnsupportedOperationException("not implemented yet");
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
}

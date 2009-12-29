package geeks.jcucumber.internal;

import cuke4duke.*;
import geeks.jcucumber.Transform;
import geeks.expressive.*;

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
  public static final AnnotationMethodRegexAssociation GIVEN_ASSOCIATION = new AnnotationMethodRegexAssociation(Given.class);
  public static final AnnotationMethodRegexAssociation WHEN_ASSOCIATION = new AnnotationMethodRegexAssociation(When.class);
  public static final AnnotationMethodRegexAssociation THEN_ASSOCIATION = new AnnotationMethodRegexAssociation(Then.class);
  private static final MethodRegexAssociation STEP_ASSOCIATION = new CompositeMethodRegexAssociation(
          GIVEN_ASSOCIATION, WHEN_ASSOCIATION, THEN_ASSOCIATION);
  public static final AnnotationMethodRegexAssociation TRANSFORM_ASSOCIATION = new AnnotationMethodRegexAssociation(Transform.class);
  public static final AnnotationMethodSpecifier BEFORE_SPECIFIER = new AnnotationMethodSpecifier(Before.class);
  public static final AnnotationMethodSpecifier AFTER_SPECIFIER = new AnnotationMethodSpecifier(After.class);
  private final Expressive expressive;
  private final Scope stepsScope;

  public JCucumberStepMother(Expressive expressive, Scope stepsScope) {
    this.expressive = expressive;
    this.stepsScope = stepsScope;
  }

  public void invoke(String step) {
    invoke(step, STEP_ASSOCIATION);
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

  public void invoke(String step, MethodRegexAssociation regexAssociation) {
    expressive.execute(step, regexAssociation, TRANSFORM_ASSOCIATION, stepsScope);
  }

  public void invokeEvent(MethodSpecifier eventSpecifier) {
    expressive.executeEvent(eventSpecifier, stepsScope);
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

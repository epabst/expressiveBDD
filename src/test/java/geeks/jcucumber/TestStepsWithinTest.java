package geeks.jcucumber;

import geeks.jcucumber.internal.JCucumberStepMother;
import geeks.expressive.*;

import cuke4duke.StepMother;
import org.testng.annotations.Test;

/**
 * A test that uses JCucumber steps without a full feature scenariofile.
 *
 * @author pabstec
 */
//intentionally don't extend something to show that it's not necessary to do so.
public class TestStepsWithinTest {
  private final StepMother stepMother = createStepMother(new ScopeBuilder().with(getClass()).build(), new DefaultObjectFactory());

  @Test
  public void shouldSupportStepsWithinTest() {
    stepMother.invoke("hello");
  }

  private static StepMother createStepMother(Scope scope, ObjectFactory objectFactory) {
    objectFactory.addInstance(Scope.class, scope);
    objectFactory.addInstance(Expressive.class, new Expressive(objectFactory));
    JCucumberStepMother stepMother = objectFactory.getInstance(JCucumberStepMother.class);
    //make StepMother available for constructor injection of steps components
    objectFactory.addInstance(StepMother.class, stepMother);
    return stepMother;
  }

  public void Given(String step) {

  }

}

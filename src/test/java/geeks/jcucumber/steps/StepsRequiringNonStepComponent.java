package geeks.jcucumber.steps;

import cuke4duke.Given;
import static org.testng.Assert.assertNotNull;

/**
 * A set of steps that require a non-stop component.
 * This is here to test that components can be constructor-injected even if they don't contain steps.
 *
 * @author pabstec
 */
public class StepsRequiringNonStepComponent {
  private final NonStepComponent nonStepComponent;

  public StepsRequiringNonStepComponent(NonStepComponent nonStepComponent) {
    this.nonStepComponent = nonStepComponent;
  }

  @Given("^a non-step component is required$")
  public void aNonStepComponentIsRequired() {
    assertNotNull(nonStepComponent);
  }
}

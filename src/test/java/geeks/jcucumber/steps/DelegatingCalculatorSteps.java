package geeks.jcucumber.steps;

import cuke4duke.StepMother;
import cuke4duke.Steps;
import cuke4duke.When;

/**
 * Calculator steps that delegate.
 *
 * @author pabstec
 */
public class DelegatingCalculatorSteps extends Steps {

  public DelegatingCalculatorSteps(StepMother stepMother) {
    super(stepMother);
  }

  @When("^\"([0-9]+) (\\+) ([0-9]+)\" is entered$")
  public void numberIsEntered(String firstNumber, String operator, String secondNumber) {
    Given("\"" + firstNumber + "\" is entered");
    Given("\"" + secondNumber + "\" is entered");
    When("I push \"" + operator + "\"");
  }
}
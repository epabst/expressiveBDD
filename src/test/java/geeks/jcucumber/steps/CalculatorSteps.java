package geeks.jcucumber.steps;

import static org.testng.Assert.assertEquals;

import java.util.Stack;
import java.util.logging.Logger;
import java.util.logging.Level;

import cuke4duke.*;
import geeks.jcucumber.Transform;

/**
 * Calculator steps.
 *
 * @author pabstec
 */
public class CalculatorSteps {
  private final Stack<Integer> stack = new Stack<Integer>();
  private int runCount = 0;
  private int runCountFromLastWhen = -1;
  private static final Logger LOGGER = Logger.getLogger(CalculatorSteps.class.getName());
  private static final Level DEBUG_LEVEL = Level.FINE;

  @Before()
  public void clearStack() {
    stack.clear();
  }

  @After()
  public void incrementRunCount() {
    runCount++;
  }

  @Given("^\"([0-9]+)\" is entered$")
  public void numberIsEntered(int number) {
    stack.push(number);
    LOGGER.log(DEBUG_LEVEL, "stack after entering number: " + stack);
  }

  @When("^I push \"\\+\"$")
  public void add() {
    assertEquals(runCountFromLastWhen, runCount - 1, "run count should match");
    runCountFromLastWhen = runCount;
    LOGGER.log(DEBUG_LEVEL, "stack before +: " + stack);
    stack.push(stack.pop() + stack.pop());
    LOGGER.log(DEBUG_LEVEL, "stack after +: " + stack);
  }

  @When("^I push \"\\/\"$")
  public void divide() {
    assertEquals(runCountFromLastWhen, runCount - 1, "run count should match");
    runCountFromLastWhen = runCount;
    LOGGER.log(DEBUG_LEVEL, "stack before +: " + stack);
    Integer divisor = stack.pop();
    Integer dividend = stack.pop();
    stack.push(dividend / divisor);
    LOGGER.log(DEBUG_LEVEL, "stack after +: " + stack);
  }

  @Then("^the result should be \"([0-9]+)\"$")
  public void theResultShouldBe(int expectedResult) {
    int result = stack.peek();
    assertEquals(result, expectedResult, "result");
    assertEquals(stack.size(), 1, "stack size");
  }

  @Given("^no more work to do$")
  public void noMoreWorkToDo() {
    //do nothing
  }

  @Transform("^([0-9]+)$")
  public int integer(String number) {
    return Integer.parseInt(number);
  }
}

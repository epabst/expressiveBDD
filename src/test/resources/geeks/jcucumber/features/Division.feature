Feature: Division Using the Calculator
  As a 4th grade student
  I want to check my division answers using an RPN calculator
  So that I can recognize my mistakes

  Scenario: intentionally cause a problem
    Given "27" is entered
    When I push "/"
    Then the result should be "9"

  Scenario: 25/5
    Given "25" is entered
    And "5" is entered
    When I push "/"
    Then the result should be "5"

  Scenario: 24/6
    Given "24" is entered
    And "6" is entered
    When I push "/"
    Then the result should be "4"

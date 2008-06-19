package org.scalatest

class MyStory extends Story("transfer from savings to checking account") {

  info("As a savings account holder")
  info("I want to transfer money from my savings account to my checking account")
  info("So that I can get cash easily from an ATM")

  scenario("savings account has sufficient funds") {
    given("my savings account balance is $100")

    // put the code here

    and("my checking account balance is $10")

    // put the code here

    when("I transfer $20 from savings to checking")
    then("my savings account balance should be $80")
    and("my checking account balance should be $30")
  }

  scenario("savings account has insufficient funds") {
    given("my savings account balance is $50")
    and("my checking account balance is $10")
    when("I transfer $60 from savings to checking")
    then("my savings account balance should be $50")
    and("my checking account balance should be $10")
  }
}

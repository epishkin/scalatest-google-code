package org.scalatest

class InsufficientFundsException extends RuntimeException

class Account(private var theBalance: Int) {
  def this() = this(0)
  def balance = theBalance
  def deposit(amount: Int) {
    if (amount < 0)
      throw new IllegalArgumentException("amount passed to balance can't be negative")
    theBalance += amount
  }
  def withdraw(amount: Int) {
    if (amount < 0)
      throw new IllegalArgumentException("amount passed to balance can't be negative")
    if (amount > theBalance)
      throw new InsufficientFundsException
    theBalance -= amount
  }
  def transfer(amount: Int, toAccount: Account) {
    withdraw(amount)
    toAccount.deposit(amount)
  }
}

class MyStory extends Story("transfer from savings to checking account") {

  info("As a savings account holder")
  info("I want to transfer money from my savings account to my checking account")
  info("So that I can get cash easily from an ATM")

  scenario("savings account has sufficient funds") {

    given("my savings account balance is $100")
    val mySavings = new Account(100)

    and("my checking account balance is $10")
    val myChecking = new Account(10)

    when("I transfer $20 from savings to checking")
    mySavings.transfer(20, myChecking)

    then("my savings account balance should be $80")
    expect(80) { mySavings.balance }

    and("my checking account balance should be $30")
    expect(30) { myChecking.balance }
  }

  scenario("savings account has insufficient funds") {

    given("my savings account balance is $50")
    val mySavings = new Account(50)

    and("my checking account balance is $10")
    val myChecking = new Account(10)

    when("I transfer $60 from savings to checking")
    intercept[InsufficientFundsException] {
      mySavings.transfer(60, myChecking)
    }

    then("my savings account balance should be $50")
    expect(50) { mySavings.balance }

    and("my checking account balance should be $10")
    expect(10) { myChecking.balance }
  }
}

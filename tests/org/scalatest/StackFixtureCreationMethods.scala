package org.scalatest

trait StackFixtureCreationMethods {

  def emptyStack = new Stack[Int]
  def fullStack = {
    val stack = new Stack[Int]
    for (i <- 0 until stack.MAX)
      stack.push(i)
    stack
  }
  def stackWithOneItem = {
    val stack = new Stack[Int]
    stack.push(9)
    stack
  }
  def stackWithOneItemLessThanCapacity = {
    val stack = new Stack[Int]
    for (i <- 1 to 9)
      stack.push(i)
    stack
  }
  val lastValuePushed = 9
}
      

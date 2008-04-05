package org.scalatest.junit.example;

import _root_.junit.framework.Assert

class ExampleJUnitSuite extends JUnit3Suite {
  
  override def setUp = println("this should get printed out before each method!")
  
  def testThatThrows = {
    throw new Exception("this test throws!")
  }
  
  def testThatFails = {
    Assert.assertEquals( "this test fails!", 1, 2 )
  }
  
  def testThatPasses = {
    // do nothing
  }
  
  override def tearDown = println("this should get printed out after each method!")

}

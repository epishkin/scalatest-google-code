package org.scalatest.testng.example;

import org.testng.annotations.Test

class ExampleTestNGSuite extends TestNGSuite{

  @Test{val invocationCount=10}
  def thisTestRunsTenTimes = {}
  
  @Test{val groups=Array("runMe")} def testWithException() {
    throw new Exception("exception!!!")
  }
  
  @Test{val groups=Array("runMe")} def testWithAssertFail() {
    assert( 1 === 2, "assert fail!!!" )
  }
  
  @Test{val dependsOnMethods=Array("testWithException")}
  def testToGetSkipped() = {}
  
  
}


package org.scalatest.events

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.scalatest.DoNotDiscover

@DoNotDiscover
class TestLocationMethodTestNGSuite extends TestNGSuite with TestLocationMethodServices {
  val suiteTypeName = "org.scalatest.events.TestLocationMethodTestNGSuite"
  val expectedStartingList = List(TestStartingPair("succeed", "succeed"))
  val expectedResultList = List(TestResultPair(classOf[TestSucceeded], "succeed"))
  
  @Test
  def succeed() { 
      
  }
  @Test(enabled=false) 
  def ignore() {
    
  }
}
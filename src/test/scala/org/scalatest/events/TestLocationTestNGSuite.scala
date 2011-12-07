package org.scalatest.events

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.scalatest.DoNotDiscover

@DoNotDiscover
class TestLocationTestNGSuite extends TestNGSuite with TestLocationServices {
  val suiteTypeName = "org.scalatest.events.TestLocationTestNGSuite"
  val expectedSuiteStartingList = List(TopOfClassPair(suiteTypeName))
  val expectedSuiteCompletedList = List(TopOfClassPair(suiteTypeName))
  val expectedSuiteAbortedList = Nil
  val expectedTestFailedList = List(SeeStackDepthExceptionPair("testFail"))
  val expectedInfoProvidedList = Nil
  
  @Test
  def testFail() { 
    fail
  }
}
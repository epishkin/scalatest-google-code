package org.scalatest.events

import org.scalatest.junit.JUnit3Suite
import org.scalatest.DoNotDiscover

@DoNotDiscover
class TestLocationMethodJUnit3Suite extends JUnit3Suite with TestLocationMethodServices {
  val suiteTypeName = "org.scalatest.events.TestLocationMethodJUnit3Suite"
  val expectedStartingList = List(TestStartingPair("testSucceed(org.scalatest.events.TestLocationMethodJUnit3Suite)", "testSucceed"))
  val expectedResultList = List(TestResultPair(classOf[TestSucceeded], "testSucceed"))
  
  def testSucceed() { 
    
  }
}
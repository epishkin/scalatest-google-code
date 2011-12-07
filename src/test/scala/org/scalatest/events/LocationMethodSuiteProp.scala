package org.scalatest.events
import org.scalatest.Stopper
import org.scalatest.Filter
import org.scalatest.Tracker
import org.scalatest.Suite
import org.scalatest.Ignore
import org.scalatest.MethodSuiteProp
import org.scalatest.fixture.FixtureSuite
import org.scalatest.StringFixture

class LocationMethodSuiteProp extends MethodSuiteProp {
  
  test("Method suites should have correct TopOfMethod location in test events.") {
    forAll(examples) { suite =>
      val reporter = new EventRecordingReporter
      suite.run(None, reporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
      val eventList = reporter.eventsReceived
      eventList.foreach { event => suite.checkFun(event) }
      suite.allChecked
    }
  }
  
  type FixtureServices = TestLocationMethodServices
  
  def suite = new TestLocationSuite
  class TestLocationSuite extends Suite with FixtureServices {
    val suiteTypeName = "org.scalatest.events.LocationMethodSuiteProp$TestLocationSuite"
    val expectedStartingList = List(TestStartingPair("testSucceed", "testSucceed"), 
                                TestStartingPair("testPending", "testPending"), 
                                TestStartingPair("testCancel", "testCancel"))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], "testSucceed"), 
                              TestResultPair(classOf[TestPending], "testPending"),
                              TestResultPair(classOf[TestCanceled], "testCancel"),
                              TestResultPair(classOf[TestIgnored], "testIgnore"))
    
    def testSucceed() {
      
    }
    def testPending() {
      pending
    }
    def testCancel() {
      cancel
    }
    @Ignore
    def testIgnore() {
      
    }
  }
  
  def fixtureSuite = new TestLocationFixtureSuite
  class TestLocationFixtureSuite extends FixtureSuite with FixtureServices with StringFixture {
    val suiteTypeName = "org.scalatest.events.LocationMethodSuiteProp$TestLocationFixtureSuite"
    val expectedStartingList = List(TestStartingPair("testSucceed", "testSucceed"), 
                                TestStartingPair("testPending", "testPending"), 
                                TestStartingPair("testCancel", "testCancel"))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], "testSucceed"), 
                              TestResultPair(classOf[TestPending], "testPending"),
                              TestResultPair(classOf[TestCanceled], "testCancel"),
                              TestResultPair(classOf[TestIgnored], "testIgnore"))
    
    def testSucceed() {
      
    }
    def testPending() {
      pending
    }
    def testCancel() {
      cancel
    }
    @Ignore
    def testIgnore() {
      
    }
  }
  
  def junit3Suite = new TestLocationMethodJUnit3Suite
  
  def junitSuite = new TestLocationMethodJUnitSuite
  
  def testngSuite = new TestLocationMethodTestNGSuite
}
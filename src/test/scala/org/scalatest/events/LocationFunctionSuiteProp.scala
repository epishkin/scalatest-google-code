package org.scalatest.events

import org.scalatest.FunctionSuiteProp
import org.scalatest.Stopper
import org.scalatest.Filter
import org.scalatest.Tracker
import org.scalatest.FunSuite
import org.scalatest.Spec
import org.scalatest.FeatureSpec
import org.scalatest.FlatSpec
import org.scalatest.FreeSpec
import org.scalatest.PropSpec
import org.scalatest.WordSpec
import org.scalatest.fixture.FixtureFeatureSpec
import org.scalatest.fixture.FixtureFlatSpec
import org.scalatest.fixture.FixtureFreeSpec
import org.scalatest.fixture.FixtureFunSuite
import org.scalatest.fixture.FixturePropSpec
import org.scalatest.fixture.FixtureSpec
import org.scalatest.fixture.FixtureWordSpec
import org.scalatest.StringFixture
import org.scalatest.SharedHelpers.thisLineNumber

class LocationFunctionSuiteProp extends FunctionSuiteProp {
  
  test("Function suites should have correct LineInFile location in test events.") {
    forAll(examples) { suite =>
      val reporter = new EventRecordingReporter
      suite.run(None, reporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
      val eventList = reporter.eventsReceived
      eventList.foreach { event => suite.checkFun(event) }
      suite.allChecked
    }
  }
  
  type FixtureServices = TestLocationFunctionServices
  
  val expectedSourceFileName: String = "LocationFunctionSuiteProp.scala"
  
  def funSuite = new FunSuite with FixtureServices {
    test("succeed") {
      
    }
    test("pending") {
      pending
    }
    test("cancel") {
      cancel
    }
    ignore("ignore") {
      
    }
    val suiteTypeName: String = "FunSuite"
    val expectedStartingList = List(TestStartingPair("succeed", expectedSourceFileName, thisLineNumber - 13), 
                                   TestStartingPair("pending", expectedSourceFileName, thisLineNumber - 11),
                                   TestStartingPair("cancel", expectedSourceFileName, thisLineNumber - 9))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 16), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 14),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 12),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 10))
    val expectedScopeOpenedList = Nil
    val expectedScopeClosedList = Nil
  }
  
  def fixtureFunSuite = new FixtureFunSuite with FixtureServices with StringFixture {
    test("succeed") { param =>
      
    }
    test("pending") { param =>
      pending
    }
    test("cancel") { param =>
      cancel
    }
    ignore("ignore") { param =>
      
    }
    val suiteTypeName: String = "FunSuite"
    val expectedStartingList = List(TestStartingPair("succeed", expectedSourceFileName, thisLineNumber - 13), 
                                   TestStartingPair("pending", expectedSourceFileName, thisLineNumber - 11),
                                   TestStartingPair("cancel", expectedSourceFileName, thisLineNumber - 9))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 16), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 14),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 12),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 10))
    val expectedScopeOpenedList = Nil
    val expectedScopeClosedList = Nil
  }
  
  def spec = new Spec with FixtureServices {
    describe("A Spec") {
      it("succeed") {
        
      }
      it("pending") {
        pending
      }
      it("cancel") {
        cancel
      }
      ignore("ignore") {
      
      }
    }
    val suiteTypeName: String = "Spec"
    val expectedStartingList = List(TestStartingPair("A Spec succeed", expectedSourceFileName, thisLineNumber - 14), 
                                   TestStartingPair("A Spec pending", expectedSourceFileName, thisLineNumber - 12),
                                   TestStartingPair("A Spec cancel", expectedSourceFileName, thisLineNumber - 10))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 17), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 15),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 13),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 11))
    val expectedScopeOpenedList = List(ScopeOpenedPair("A Spec", expectedSourceFileName, thisLineNumber - 22))
    val expectedScopeClosedList = List(ScopeClosedPair("A Spec", expectedSourceFileName, thisLineNumber - 23))
  }
  
  def fixtureSpec = new FixtureSpec with FixtureServices with StringFixture {
    describe("A Spec") {
      it("succeed") { param =>
        
      }
      it("pending") { param =>
        pending
      }
      it("cancel") { param =>
        cancel
      }
      ignore("ignore") { param =>
      
      }
    }
    val suiteTypeName: String = "FixtureSpec"
    val expectedStartingList = List(TestStartingPair("A Spec succeed", expectedSourceFileName, thisLineNumber - 14), 
                                   TestStartingPair("A Spec pending", expectedSourceFileName, thisLineNumber - 12),
                                   TestStartingPair("A Spec cancel", expectedSourceFileName, thisLineNumber - 10))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 17), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 15),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 13),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 11))
    val expectedScopeOpenedList = List(ScopeOpenedPair("A Spec", expectedSourceFileName, thisLineNumber - 22))
    val expectedScopeClosedList = List(ScopeClosedPair("A Spec", expectedSourceFileName, thisLineNumber - 23))
  }
  
  def featureSpec = new FeatureSpec with FixtureServices {
    scenario("succeed") {
      
    }
    scenario("pending") {
      pending
    }
    scenario("cancel") {
      cancel
    }
    ignore("ignore") {
      
    }
    val suiteTypeName: String = "FeatureSpec"
    val expectedStartingList = List(TestStartingPair("Scenario: succeed", expectedSourceFileName, thisLineNumber - 13), 
                                   TestStartingPair("Scenario: pending", expectedSourceFileName, thisLineNumber - 11),
                                   TestStartingPair("Scenario: cancel", expectedSourceFileName, thisLineNumber - 9))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 16), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 14),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 12),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 10))
    val expectedScopeOpenedList = Nil
    val expectedScopeClosedList = Nil
  }
  
  def fixtureFeatureSpec = new FixtureFeatureSpec with FixtureServices with StringFixture {
    feature("Test") {
      scenario("succeed") { param =>
      
      }
      scenario("pending") { param =>
        pending
      }
      scenario("cancel") { param =>
        cancel
      }
      ignore("ignore") { param =>
      
      }
    }
    val suiteTypeName: String = "FixtureFeatureSpec"
    val expectedStartingList = List(TestStartingPair("Test Scenario: succeed", expectedSourceFileName, thisLineNumber - 14), 
                                   TestStartingPair("Test Scenario: pending", expectedSourceFileName, thisLineNumber - 12),
                                   TestStartingPair("Test Scenario: cancel", expectedSourceFileName, thisLineNumber - 10))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 17), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 15),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 13),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 11))
    val expectedScopeOpenedList = List(ScopeOpenedPair("Test", expectedSourceFileName, thisLineNumber - 22))
    val expectedScopeClosedList = List(ScopeClosedPair("Test", expectedSourceFileName, thisLineNumber - 23))
  }
  
  def flatSpec = new FlatSpec with FixtureServices {
    "Test 1" should "succeed" in {
      
    }
    "Test 2" should "pending" in {
      pending
    }
    "Test 3" should "cancel" in {
      cancel
    }
    "Test 4" should "be ignored" ignore {
      
    }
    val suiteTypeName: String = "FlatSpec"
    val expectedStartingList = List(TestStartingPair("Test 1 should succeed", expectedSourceFileName, thisLineNumber - 13), 
                                   TestStartingPair("Test 2 should pending", expectedSourceFileName, thisLineNumber - 11),
                                   TestStartingPair("Test 3 should cancel", expectedSourceFileName, thisLineNumber - 9))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 16), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 14),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 12),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 10))
    val expectedScopeOpenedList = List(ScopeOpenedPair("Test 1", expectedSourceFileName, thisLineNumber - 20), 
                                       ScopeOpenedPair("Test 2", expectedSourceFileName, thisLineNumber - 18),
                                       ScopeOpenedPair("Test 3", expectedSourceFileName, thisLineNumber - 16),
                                       ScopeOpenedPair("Test 4", expectedSourceFileName, thisLineNumber - 14))
    val expectedScopeClosedList = List(ScopeClosedPair("Test 1", expectedSourceFileName, thisLineNumber - 24),
                                       ScopeClosedPair("Test 2", expectedSourceFileName, thisLineNumber - 22),
                                       ScopeClosedPair("Test 3", expectedSourceFileName, thisLineNumber - 20),
                                       ScopeClosedPair("Test 4", expectedSourceFileName, thisLineNumber - 18))
  }
  
  def fixtureFlatSpec = new FixtureFlatSpec with FixtureServices with StringFixture {
    "Test 1" should "succeed" in { param =>
      
    }
    "Test 2" should "pending" in { param =>
      pending
    }
    "Test 3" should "cancel" in { param =>
      cancel
    }
    "Test 4" should "be ignored" ignore { param =>
      
    }
    val suiteTypeName: String = "FixtureFlatSpec"
    val expectedStartingList = List(TestStartingPair("Test 1 should succeed", expectedSourceFileName, thisLineNumber - 13), 
                                   TestStartingPair("Test 2 should pending", expectedSourceFileName, thisLineNumber - 11),
                                   TestStartingPair("Test 3 should cancel", expectedSourceFileName, thisLineNumber - 9))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 16), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 14),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 12),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 10))
    val expectedScopeOpenedList = List(ScopeOpenedPair("Test 1", expectedSourceFileName, thisLineNumber - 20), 
                                       ScopeOpenedPair("Test 2", expectedSourceFileName, thisLineNumber - 18),
                                       ScopeOpenedPair("Test 3", expectedSourceFileName, thisLineNumber - 16),
                                       ScopeOpenedPair("Test 4", expectedSourceFileName, thisLineNumber - 14))
    val expectedScopeClosedList = List(ScopeClosedPair("Test 1", expectedSourceFileName, thisLineNumber - 24),
                                       ScopeClosedPair("Test 2", expectedSourceFileName, thisLineNumber - 22),
                                       ScopeClosedPair("Test 3", expectedSourceFileName, thisLineNumber - 20),
                                       ScopeClosedPair("Test 4", expectedSourceFileName, thisLineNumber - 18))
  }
  
  def freeSpec = new FreeSpec with FixtureServices {
    "Test" - {
      "should succeed" in {
        
      }
      "should pending" in {
        pending
      }
      "should cancel" in {
        cancel
      }
      "should ignore" ignore {
        
      }
    }
    val suiteTypeName: String = "FreeSpec"
    val expectedStartingList = List(TestStartingPair("Test should succeed", expectedSourceFileName, thisLineNumber - 14), 
                                   TestStartingPair("Test should pending", expectedSourceFileName, thisLineNumber - 12),
                                   TestStartingPair("Test should cancel", expectedSourceFileName, thisLineNumber - 10))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 17), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 15),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 13),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 11))
    val expectedScopeOpenedList = List(ScopeOpenedPair("Test", expectedSourceFileName, thisLineNumber - 22))
    val expectedScopeClosedList = List(ScopeClosedPair("Test", expectedSourceFileName, thisLineNumber - 23))
  }
  
  def fixtureFreeSpec = new FixtureFreeSpec with FixtureServices with StringFixture {
    "Test" - {
      "should succeed" in { param =>
        
      }
      "should pending" in { param =>
        pending
      }
      "should cancel" in { param =>
        cancel
      }
      "should ignore" ignore { param =>
        
      }
    }
    val suiteTypeName: String = "FixtureFreeSpec"
    val expectedStartingList = List(TestStartingPair("Test should succeed", expectedSourceFileName, thisLineNumber - 14), 
                                   TestStartingPair("Test should pending", expectedSourceFileName, thisLineNumber - 12),
                                   TestStartingPair("Test should cancel", expectedSourceFileName, thisLineNumber - 10))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 17), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 15),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 13),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 11))
    val expectedScopeOpenedList = List(ScopeOpenedPair("Test", expectedSourceFileName, thisLineNumber - 22))
    val expectedScopeClosedList = List(ScopeClosedPair("Test", expectedSourceFileName, thisLineNumber - 23))
  }
  
  def propSpec = new PropSpec with FixtureServices {
    property("Test should succeed") {
      
    }
    property("Test should pending") {
      pending
    }
    property("Test should cancel") {
      cancel
    }
    ignore("Test should ignore") {
        
    }
    val suiteTypeName: String = "PropSpec"
    val expectedStartingList = List(TestStartingPair("Test should succeed", expectedSourceFileName, thisLineNumber - 13), 
                                   TestStartingPair("Test should pending", expectedSourceFileName, thisLineNumber - 11),
                                   TestStartingPair("Test should cancel", expectedSourceFileName, thisLineNumber - 9))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 16), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 14),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 12),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 10))
    val expectedScopeOpenedList = Nil
    val expectedScopeClosedList = Nil
  }
  
  def fixturePropSpec = new FixturePropSpec with FixtureServices with StringFixture {
    property("Test should succeed") { param =>
      
    }
    property("Test should pending") { param =>
      pending
    }
    property("Test should cancel") { param =>
      cancel
    }
    ignore("Test should ignore") { param =>
        
    }
    val suiteTypeName: String = "FixturePropSpec"
    val expectedStartingList = List(TestStartingPair("Test should succeed", expectedSourceFileName, thisLineNumber - 13), 
                                   TestStartingPair("Test should pending", expectedSourceFileName, thisLineNumber - 11),
                                   TestStartingPair("Test should cancel", expectedSourceFileName, thisLineNumber - 9))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 16), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 14),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 12),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 10))
    val expectedScopeOpenedList = Nil
    val expectedScopeClosedList = Nil
  }
  
  def wordSpec = new WordSpec with FixtureServices {
    "Test" should {
      "succeed" in {
        
      }
      "pending" in {
        pending
      }
      "cancel" in {
        cancel
      }
      "ignore " ignore {
        
      }
    }
    val suiteTypeName: String = "WordSpec"
    val expectedStartingList = List(TestStartingPair("Test should succeed", expectedSourceFileName, thisLineNumber - 14), 
                                   TestStartingPair("Test should pending", expectedSourceFileName, thisLineNumber - 12),
                                   TestStartingPair("Test should cancel", expectedSourceFileName, thisLineNumber - 10))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 17), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 15),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 13),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 11))
    val expectedScopeOpenedList = List(ScopeOpenedPair("Test", expectedSourceFileName, thisLineNumber - 22))
    val expectedScopeClosedList = List(ScopeClosedPair("Test", expectedSourceFileName, thisLineNumber - 23))
  }
  
  def fixtureWordSpec = new FixtureWordSpec with FixtureServices with StringFixture {
    "Test" should {
      "succeed" in { param =>
        
      }
      "pending" in { param =>
        pending
      }
      "cancel" in { param =>
        cancel
      }
      "ignore " ignore { param =>
        
      }
    }
    val suiteTypeName: String = "FixtureWordSpec"
    val expectedStartingList = List(TestStartingPair("Test should succeed", expectedSourceFileName, thisLineNumber - 14), 
                                   TestStartingPair("Test should pending", expectedSourceFileName, thisLineNumber - 12),
                                   TestStartingPair("Test should cancel", expectedSourceFileName, thisLineNumber - 10))
    val expectedResultList = List(TestResultPair(classOf[TestSucceeded], expectedSourceFileName, thisLineNumber - 17), 
                                 TestResultPair(classOf[TestPending], expectedSourceFileName, thisLineNumber - 15),
                                 TestResultPair(classOf[TestCanceled], expectedSourceFileName, thisLineNumber - 13),
                                 TestResultPair(classOf[TestIgnored], expectedSourceFileName, thisLineNumber - 11))
    val expectedScopeOpenedList = List(ScopeOpenedPair("Test", expectedSourceFileName, thisLineNumber - 22))
    val expectedScopeClosedList = List(ScopeClosedPair("Test", expectedSourceFileName, thisLineNumber - 23))
  }
}
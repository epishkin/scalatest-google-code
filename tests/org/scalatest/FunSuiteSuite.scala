/*
 * Copyright 2001-2008 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest

import org.scalatest.events._

// Tag classes used in tests
package mytags {
  object SlowAsMolasses extends Tag("org.scalatest.SlowAsMolasses")
  object FastAsLight extends Tag("org.scalatest.FastAsLight")
  object WeakAsAKitten extends Tag("org.scalatest.WeakAsAKitten")
}

class FunSuiteSuite extends Suite with SharedHelpers {

  def testExcludes() {

    val a = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", mytags.SlowAsMolasses) { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }
    val repA = new TestIgnoredTrackingReporter
    a.run(None, repA, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(!repA.testIgnoredReceived)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", mytags.SlowAsMolasses) { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }
    val repB = new TestIgnoredTrackingReporter
    b.run(None, repB, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), Map(), None, new Tracker)
    assert(!repB.testIgnoredReceived)
    assert(b.theTestThisCalled)
    assert(!b.theTestThatCalled)

    val c = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", mytags.SlowAsMolasses) { theTestThisCalled = true }
      test("test that", mytags.SlowAsMolasses) { theTestThatCalled = true }
    }
    val repC = new TestIgnoredTrackingReporter
    c.run(None, repB, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), Map(), None, new Tracker)
    assert(!repC.testIgnoredReceived)
    assert(c.theTestThisCalled)
    assert(c.theTestThatCalled)

    val d = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this", mytags.SlowAsMolasses) { theTestThisCalled = true }
      test("test that", mytags.SlowAsMolasses) { theTestThatCalled = true }
    }
    val repD = new TestIgnoredTrackingReporter
    d.run(None, repD, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.Ignore")), Map(), None, new Tracker)
    assert(repD.testIgnoredReceived)
    assert(!d.theTestThisCalled)
    assert(d.theTestThatCalled)

    val e = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true }
      test("test that", mytags.SlowAsMolasses) { theTestThatCalled = true }
      test("test the other") { theTestTheOtherCalled = true }
    }
    val repE = new TestIgnoredTrackingReporter
    e.run(None, repE, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
              Map(), None, new Tracker)
    assert(!repE.testIgnoredReceived)
    assert(!e.theTestThisCalled)
    assert(e.theTestThatCalled)
    assert(!e.theTestTheOtherCalled)

    val f = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignore("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true }
      test("test that", mytags.SlowAsMolasses) { theTestThatCalled = true }
      test("test the other") { theTestTheOtherCalled = true }
    }
    val repF = new TestIgnoredTrackingReporter
    f.run(None, repF, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
              Map(), None, new Tracker)
    assert(!repF.testIgnoredReceived)
    assert(!f.theTestThisCalled)
    assert(f.theTestThatCalled)
    assert(!f.theTestTheOtherCalled)

    val g = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true }
      test("test that", mytags.SlowAsMolasses) { theTestThatCalled = true }
      ignore("test the other") { theTestTheOtherCalled = true }
    }
    val repG = new TestIgnoredTrackingReporter
    g.run(None, repG, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
              Map(), None, new Tracker)
    assert(!repG.testIgnoredReceived)
    assert(!g.theTestThisCalled)
    assert(g.theTestThatCalled)
    assert(!g.theTestTheOtherCalled)

    val h = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true }
      test("test that", mytags.SlowAsMolasses) { theTestThatCalled = true }
      test("test the other") { theTestTheOtherCalled = true }
    }
    val repH = new TestIgnoredTrackingReporter
    h.run(None, repH, new Stopper {}, Filter(None, Set("org.scalatest.FastAsLight")), Map(), None, new Tracker)
    assert(!repH.testIgnoredReceived)
    assert(!h.theTestThisCalled)
    assert(h.theTestThatCalled)
    assert(h.theTestTheOtherCalled)

    val i = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true }
      test("test that", mytags.SlowAsMolasses) { theTestThatCalled = true }
      test("test the other") { theTestTheOtherCalled = true }
    }
    val repI = new TestIgnoredTrackingReporter
    i.run(None, repI, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses")), Map(), None, new Tracker)
    assert(!repI.testIgnoredReceived)
    assert(!i.theTestThisCalled)
    assert(!i.theTestThatCalled)
    assert(i.theTestTheOtherCalled)

    val j = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignore("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true }
      ignore("test that", mytags.SlowAsMolasses) { theTestThatCalled = true }
      test("test the other") { theTestTheOtherCalled = true }
    }
    val repJ = new TestIgnoredTrackingReporter
    j.run(None, repJ, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses")), Map(), None, new Tracker)
    assert(!repI.testIgnoredReceived)
    assert(!j.theTestThisCalled)
    assert(!j.theTestThatCalled)
    assert(j.theTestTheOtherCalled)

    val k = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignore("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { theTestThisCalled = true }
      ignore("test that", mytags.SlowAsMolasses) { theTestThatCalled = true }
      ignore("test the other") { theTestTheOtherCalled = true }
    }
    val repK = new TestIgnoredTrackingReporter
    k.run(None, repK, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore")), Map(), None, new Tracker)
    assert(repK.testIgnoredReceived)
    assert(!k.theTestThisCalled)
    assert(!k.theTestThatCalled)
    assert(!k.theTestTheOtherCalled)
  }

  def testTestCount() {

    val a = new FunSuite {
      test("test this") {}
      test("test that") {}
    }
    assert(a.expectedTestCount(Filter()) === 2)

    val b = new FunSuite {
      ignore("test this") {}
      test("test that") {}
    }
    assert(b.expectedTestCount(Filter()) === 1)

    val c = new FunSuite {
      test("test this", mytags.FastAsLight) {}
      test("test that") {}
    }
    assert(c.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
    assert(c.expectedTestCount(Filter(None, Set("org.scalatest.FastAsLight"))) === 1)

    val d = new FunSuite {
      test("test this", mytags.FastAsLight, mytags.SlowAsMolasses) {}
      test("test that", mytags.SlowAsMolasses) {}
      test("test the other thing") {}
    }
    assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
    assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
    assert(d.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 1)
    assert(d.expectedTestCount(Filter()) === 3)

    val e = new FunSuite {
      test("test this", mytags.FastAsLight, mytags.SlowAsMolasses) {}
      test("test that", mytags.SlowAsMolasses) {}
      ignore("test the other thing") {}
    }
    assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
    assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
    assert(e.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 0)
    assert(e.expectedTestCount(Filter()) === 2)

    val f = new SuperSuite(List(a, b, c, d, e))
    assert(f.expectedTestCount(Filter()) === 10)
  }

  def testThatTestMethodsWithNoTagsDontShowUpInTagsMap() {
    
    val a = new FunSuite {
      test("test not in a group") {}
    }
    assert(a.tags.keySet.size === 0)
  }

  def testThatTestFunctionsThatResultInNonUnitAreRegistered() {
    val a = new FunSuite {
      test("test this") { 1 }
      test("test that") { "hi" }
    }
    assert(a.expectedTestCount(Filter()) === 2)
    assert(a.testNames.size === 2)
    assert(a.tags.keySet.size === 0)
  }

  def testThatTestNameCantBeReused() {
    intercept[DuplicateTestNameException] {
      new FunSuite {
        test("test this") {}
        test("test this") {}
      }
    }
    intercept[DuplicateTestNameException] {
      new FunSuite {
        ignore("test this") {}
        test("test this") {}
      }
    }
    intercept[DuplicateTestNameException] {
      new FunSuite {
        test("test this") {}
        ignore("test this") {}
      }
    }
    intercept[DuplicateTestNameException] {
      new FunSuite {
        ignore("test this") {}
        ignore("test this") {}
      }
    }
  }
  
  def testThatIfYouCallTestAfterExecuteYouGetAnTestFailedExceptionAndTheTestDoesntRun() {
    class MySuite extends FunSuite {
      var fromMethodTestExecuted = false
      var fromConstructorTestExecuted = false
      test("from constructor") {
        fromConstructorTestExecuted = true
      }
      def registerOne() {
        test("from method") {
          fromMethodTestExecuted = true
        }
      }
    }
    val a = new MySuite
    a.run()
    assert(a.fromConstructorTestExecuted)
    assert(!a.fromMethodTestExecuted)
    intercept[TestRegistrationClosedException] {
      a.registerOne()
    }
    a.run()
    assert(!a.fromMethodTestExecuted)
  }
  
  def testThatInfoInsideATestMethodGetsOutTheDoor() {
    class MyReporter extends Reporter {
      var infoProvidedReceived = false
      var lastEvent: InfoProvided = null
      def apply(event: Event) {
        event match {
          case event: InfoProvided =>
            infoProvidedReceived = true
            lastEvent = event
          case _ =>
        }
      }
    }
    val msg = "hi there, dude"
    class MySuite extends FunSuite {
      test("test this") {
        info(msg)
      }
    }
    val a = new MySuite
    val myRep = new MyReporter
    a.run(None, myRep, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(myRep.infoProvidedReceived)
    assert(myRep.lastEvent.message === msg)
  }
  
  def testThatInfoInTheConstructorGetsOutTheDoor() {
    class MyReporter extends Reporter {
      var infoProvidedReceived = false
      var lastEvent: InfoProvided = null
      def apply(event: Event) {
        event match {
          case event: InfoProvided =>
            infoProvidedReceived = true
            lastEvent = event
          case _ =>
        }
      }
    }
    val msg = "hi there, dude"
    class MySuite extends FunSuite {
      info(msg)
      test("test this") {
      }
    }
    val a = new MySuite
    val myRep = new MyReporter
    a.run(None, myRep, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(myRep.infoProvidedReceived)
    assert(myRep.lastEvent.message === msg)
  }

  def testThatInfoInTheConstructorBeforeATestHappensFirst() {
    var infoProvidedReceived = false
    var infoProvidedReceivedBeforeTest = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case event: InfoProvided =>
            infoProvidedReceived = true
          case _ =>
        }
      }
    }
    val msg = "hi there, dude"
    class MySuite extends FunSuite {
      info(msg)
      test("test this") {
        if (infoProvidedReceived)
          infoProvidedReceivedBeforeTest = true
      }
    }
    val a = new MySuite
    val myRep = new MyReporter
    a.run(None, myRep, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(infoProvidedReceivedBeforeTest)
  }

  def testThatInfoInTheConstructorAfterATestHappensSecond() {
    var infoProvidedReceived = false
    var infoProvidedReceivedAfterTest = true
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case event: InfoProvided =>
            infoProvidedReceived = true
          case _ =>
        }
      }
    }
    val msg = "hi there, dude"
    class MySuite extends FunSuite {
      test("test this") {
        if (infoProvidedReceived)
          infoProvidedReceivedAfterTest = false
      }
      info(msg)
    }
    val a = new MySuite
    val myRep = new MyReporter
    a.run(None, myRep, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(infoProvidedReceivedAfterTest)
    assert(infoProvidedReceived)
  }

  def callingTestFromWithinATestClauseResultsInATestFailedErrorAtRuntime() {

    var testFailedAsExpected = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case event: TestFailed =>
            if (event.testName.indexOf("this test should blow up") != -1)
              testFailedAsExpected = true
          case _ =>
        }
      }
    }

    class MySuite extends FunSuite {
      test("this test should blow up") {
        test("is in the wrong place also") {
          assert(1 === 1)
        }
      }
    }

    val a = new MySuite
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testFailedAsExpected)
  }

  def callingTestFromWithinATestWithTagsClauseResultsInATestFailedErrorAtRuntime() {
    
    var testFailedAsExpected = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case event: TestFailed =>
          if (event.testName.indexOf("this test should blow up") != -1)
            testFailedAsExpected = true
          case _ =>
        }
      }
    }

    class MySuite extends FunSuite {
      test("this test should blow up") {
        test("is in the wrong place also", mytags.SlowAsMolasses) {
          assert(1 === 1)
        }
      }
    }

    val a = new MySuite
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testFailedAsExpected)
  }

  def callingIgnoreFromWithinATestClauseResultsInATestFailedErrorAtRuntime() {
    
    var testFailedAsExpected = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case event: TestFailed =>
            if (event.testName.indexOf("this test should blow up") != -1)
              testFailedAsExpected = true
          case _ =>
        }
      }
    }

    class MySuite extends FunSuite {
      test("this test should blow up") {
        ignore("is in the wrong place also") {
          assert(1 === 1)
        }
      }
    }

    val a = new MySuite
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testFailedAsExpected)
  }

  def callingIgnoreWithTagsFromWithinATestClauseResultsInATestFailedErrorAtRuntime() {
    
    var testFailedAsExpected = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case event: TestFailed =>
            if (event.testName.indexOf("this test should blow up") != -1)
              testFailedAsExpected = true
          case _ =>
        }
      }
    }

    class MySuite extends FunSuite {
      test("this test should blow up") {
        ignore("is in the wrong place also", mytags.SlowAsMolasses) {
          assert(1 === 1)
        }
      }
    }

    val a = new MySuite
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testFailedAsExpected)
  }

  def testThatTestDurationsAreIncludedInTestFailedAndTestSucceededEventsFiredFromFunSuite() {

    class MyFunSuite extends FunSuite {
      test("that it succeeds") {}
      test("that it fails") { fail() }
    }

    val myFunSuite = new MyFunSuite
    val myReporter = new TestDurationReporter
    myFunSuite.run(None, myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.testSucceededWasFiredAndHadADuration)
    assert(myReporter.testFailedWasFiredAndHadADuration)
  }

  def testThatSuiteDurationsAreIncludedInSuiteCompletedEventsFiredFromFunSuite() {

    class MyFunSuite extends FunSuite {
      override def nestedSuites = List(new Suite {})
    }

    val myFunSuite = new MyFunSuite
    val myReporter = new SuiteDurationReporter
    myFunSuite.run(None, myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.suiteCompletedWasFiredAndHadADuration)
  }

  def testThatSuiteDurationsAreIncludedInSuiteAbortedEventsFiredFromFunSuite() {

    class SuiteThatAborts extends Suite {
      override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
              config: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
        throw new RuntimeException("Aborting for testing purposes")
      }
    }

    class MyFunSuite extends FunSuite {
      override def nestedSuites = List(new SuiteThatAborts {})
    }

    val myFunSuite = new MyFunSuite
    val myReporter = new SuiteDurationReporter
    myFunSuite.run(None, myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.suiteAbortedWasFiredAndHadADuration)
  }

  def testPendingWorksInFunSuite() {

    class MyFunSuite extends FunSuite {
      test("this test is pending") (pending)
    }

    val mySuite = new MyFunSuite
    val myReporter = new PendingReporter
    mySuite.run(None, myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.testPendingWasFired)
  }
}


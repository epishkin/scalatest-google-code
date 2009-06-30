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

class FunSuiteSuite extends Suite with HandyReporters {

  def testTestNames() {

    val a = new FunSuite {
      test("test this") {}
      test("test that") {}
    }

    expect(List("test this", "test that")) {
      a.testNames.elements.toList
    }

    val b = new FunSuite {}

    expect(List[String]()) {
      b.testNames.elements.toList
    }

    val c = new FunSuite {
      test("test this") {}
      test("test that") {}
    }

    expect(List("test this", "test that")) {
      c.testNames.elements.toList
    }

    // Test duplicate names
    intercept[TestFailedException] {
      new FunSuite {
        test("test this") {}
        test("test this") {}
      }
    }
    intercept[TestFailedException] {
      new FunSuite {
        test("test this") {}
        ignore("test this") {}
      }
    }
    intercept[TestFailedException] {
      new FunSuite {
        ignore("test this") {}
        ignore("test this") {}
      }
    }
    intercept[TestFailedException] {
      new FunSuite {
        ignore("test this") {}
        test("test this") {}
      }
    }
  }

  def testTestTags() {
    
    val a = new FunSuite {
      ignore("test this") {}
      test("test that") {}
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"))) {
      a.tags
    }

    val b = new FunSuite {
      test("test this") {}
      ignore("test that") {}
    }
    expect(Map("test that" -> Set("org.scalatest.Ignore"))) {
      b.tags
    }

    val c = new FunSuite {
      ignore("test this") {}
      ignore("test that") {}
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"), "test that" -> Set("org.scalatest.Ignore"))) {
      c.tags
    }

    val d = new FunSuite {
      test("test this", mytags.SlowAsMolasses) {}
      ignore("test that", mytags.SlowAsMolasses) {}
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
      d.tags
    }

    val e = new FunSuite {}
    expect(Map()) {
      e.tags
    }

    val f = new FunSuite {
      test("test this", mytags.SlowAsMolasses, mytags.WeakAsAKitten) {}
      test("test that", mytags.SlowAsMolasses) {}
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
      f.tags
    }
  }

  def testExecuteOneTest() {
    
    class MySuite extends FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this") { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }

    val a = new MySuite 
    a.run("test this")
    assert(a.theTestThisCalled)
    assert(!a.theTestThatCalled)

    val b = new MySuite
    b.run()
    assert(b.theTestThisCalled)
    assert(b.theTestThatCalled)
  }

  class MyReporter extends Reporter {
    var testIgnoredReceived = false
    var lastEvent: TestIgnored = null
    def apply(event: Event) {
      event match {
        case event: TestIgnored =>
          testIgnoredReceived = true
          lastEvent = event
        case _ =>
      }
    }
  }

  def testTestMethodsWithIgnores() {

    val a = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this") { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }

    val repA = new MyReporter
    a.run(None, repA, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
    assert(!repA.testIgnoredReceived)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }

    val repB = new MyReporter
    b.run(None, repB, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None, new Tracker)
    assert(repB.testIgnoredReceived)
    assert(repB.lastEvent.testName endsWith "test this")
    assert(!b.theTestThisCalled)
    assert(b.theTestThatCalled)

    val c = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this") { theTestThisCalled = true }
      ignore("test that") { theTestThatCalled = true }
    }

    val repC = new MyReporter
    c.run(None, repC, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None, new Tracker)
    assert(repC.testIgnoredReceived)
    assert(repC.lastEvent.testName endsWith "test that", repC.lastEvent.testName)
    assert(c.theTestThisCalled)
    assert(!c.theTestThatCalled)

    // The order I want is order of appearance in the file.
    // Will try and implement that tomorrow. Subtypes will be able to change the order.
    val d = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      ignore("test that") { theTestThatCalled = true }
    }

    val repD = new MyReporter
    d.run(None, repD, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None, new Tracker)
    assert(repD.testIgnoredReceived)
    assert(repD.lastEvent.testName endsWith "test that") // last because should be in order of appearance
    assert(!d.theTestThisCalled)
    assert(!d.theTestThatCalled)

    // If I provide a specific testName to run, then it should ignore an Ignore on that test
    // method and actually invoke it.
    val e = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }

    val repE = new MyReporter
    e.run(Some("test this"), repE, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
    assert(!repE.testIgnoredReceived)
    assert(e.theTestThisCalled)
  }

  def testExcludes() {

    val a = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", mytags.SlowAsMolasses) { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }
    val repA = new MyReporter
    a.run(None, repA, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
    assert(!repA.testIgnoredReceived)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", mytags.SlowAsMolasses) { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }
    val repB = new MyReporter
    b.run(None, repB, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set(), Map(), None, new Tracker)
    assert(!repB.testIgnoredReceived)
    assert(b.theTestThisCalled)
    assert(!b.theTestThatCalled)

    val c = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", mytags.SlowAsMolasses) { theTestThisCalled = true }
      test("test that", mytags.SlowAsMolasses) { theTestThatCalled = true }
    }
    val repC = new MyReporter
    c.run(None, repB, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set(), Map(), None, new Tracker)
    assert(!repC.testIgnoredReceived)
    assert(c.theTestThisCalled)
    assert(c.theTestThatCalled)

    val d = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this", mytags.SlowAsMolasses) { theTestThisCalled = true }
      test("test that", mytags.SlowAsMolasses) { theTestThatCalled = true }
    }
    val repD = new MyReporter
    d.run(None, repD, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.Ignore"), Map(), None, new Tracker)
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
    val repE = new MyReporter
    e.run(None, repE, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight"),
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
    val repF = new MyReporter
    f.run(None, repF, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight"),
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
    val repG = new MyReporter
    g.run(None, repG, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight"),
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
    val repH = new MyReporter
    h.run(None, repH, new Stopper {}, Set(), Set("org.scalatest.FastAsLight"), Map(), None, new Tracker)
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
    val repI = new MyReporter
    i.run(None, repI, new Stopper {}, Set(), Set("org.scalatest.SlowAsMolasses"), Map(), None, new Tracker)
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
    val repJ = new MyReporter
    j.run(None, repJ, new Stopper {}, Set(), Set("org.scalatest.SlowAsMolasses"), Map(), None, new Tracker)
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
    val repK = new MyReporter
    k.run(None, repK, new Stopper {}, Set(), Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore"), Map(), None, new Tracker)
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
    assert(a.expectedTestCount(Set(), Set()) === 2)

    val b = new FunSuite {
      ignore("test this") {}
      test("test that") {}
    }
    assert(b.expectedTestCount(Set(), Set()) === 1)

    val c = new FunSuite {
      test("test this", mytags.FastAsLight) {}
      test("test that") {}
    }
    assert(c.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(c.expectedTestCount(Set(), Set("org.scalatest.FastAsLight")) === 1)

    val d = new FunSuite {
      test("test this", mytags.FastAsLight, mytags.SlowAsMolasses) {}
      test("test that", mytags.SlowAsMolasses) {}
      test("test the other thing") {}
    }
    assert(d.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(d.expectedTestCount(Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight")) === 1)
    assert(d.expectedTestCount(Set(), Set("org.scalatest.SlowAsMolasses")) === 1)
    assert(d.expectedTestCount(Set(), Set()) === 3)

    val e = new FunSuite {
      test("test this", mytags.FastAsLight, mytags.SlowAsMolasses) {}
      test("test that", mytags.SlowAsMolasses) {}
      ignore("test the other thing") {}
    }
    assert(e.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(e.expectedTestCount(Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight")) === 1)
    assert(e.expectedTestCount(Set(), Set("org.scalatest.SlowAsMolasses")) === 0)
    assert(e.expectedTestCount(Set(), Set()) === 2)

    val f = new SuperSuite(List(a, b, c, d, e))
    assert(f.expectedTestCount(Set(), Set()) === 10)
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
    assert(a.expectedTestCount(Set(), Set()) === 2)
    assert(a.testNames.size === 2)
    assert(a.tags.keySet.size === 0)
  }

  def testThatTestNameCantBeReused() {
    intercept[TestFailedException] {
      new FunSuite {
        test("test this") {}
        test("test this") {}
      }
    }
    intercept[TestFailedException] {
      new FunSuite {
        ignore("test this") {}
        test("test this") {}
      }
    }
    intercept[TestFailedException] {
      new FunSuite {
        test("test this") {}
        ignore("test this") {}
      }
    }
    intercept[TestFailedException] {
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
    intercept[TestFailedException] {
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
    a.run(None, myRep, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
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
    a.run(None, myRep, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
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
    a.run(None, myRep, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
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
    a.run(None, myRep, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
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
    a.run(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
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
    a.run(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
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
    a.run(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
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
    a.run(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
    assert(testFailedAsExpected)
  }

  def testThatTestDurationsAreIncludedInTestFailedAndTestSucceededEventsFiredFromFunSuite() {

    class MyFunSuite extends FunSuite {
      test("that it succeeds") {}
      test("that it fails") { fail() }
    }

    val myFunSuite = new MyFunSuite
    val myReporter = new TestDurationReporter
    myFunSuite.run(None, myReporter, new Stopper {}, Set(), Set(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.testSucceededWasFiredAndHadADuration)
    assert(myReporter.testFailedWasFiredAndHadADuration)
  }

  def testThatSuiteDurationsAreIncludedInSuiteCompletedEventsFiredFromFunSuite() {

    class MyFunSuite extends FunSuite {
      override def nestedSuites = List(new Suite {})
    }

    val myFunSuite = new MyFunSuite
    val myReporter = new SuiteDurationReporter
    myFunSuite.run(None, myReporter, new Stopper {}, Set(), Set(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.suiteCompletedWasFiredAndHadADuration)
  }

  def testThatSuiteDurationsAreIncludedInSuiteAbortedEventsFiredFromFunSuite() {

    class SuiteThatAborts extends Suite {
      override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, groupsToInclude: Set[String], groupsToExclude: Set[String],
              goodies: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
        throw new RuntimeException("Aborting for testing purposes")
      }
    }

    class MyFunSuite extends FunSuite {
      override def nestedSuites = List(new SuiteThatAborts {})
    }

    val myFunSuite = new MyFunSuite
    val myReporter = new SuiteDurationReporter
    myFunSuite.run(None, myReporter, new Stopper {}, Set(), Set(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.suiteAbortedWasFiredAndHadADuration)
  }

  def testPendingWorksInFunSuite() {

    class MyFunSuite extends FunSuite {
      test("this test is pending") (pending)
    }

    val mySuite = new MyFunSuite
    val myReporter = new PendingReporter
    mySuite.run(None, myReporter, new Stopper {}, Set(), Set(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.testPendingWasFired)
  }
}


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

import org.scalatest.events.Event

// Group classes used in tests
package mygroups {
  object SlowAsMolasses extends Group("org.scalatest.SlowAsMolasses")
  object FastAsLight extends Group("org.scalatest.FastAsLight")
  object WeakAsAKitten extends Group("org.scalatest.WeakAsAKitten")
}

class FunSuiteSuite extends Suite {

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

  def testTestGroups() {
    
    val a = new FunSuite {
      ignore("test this") {}
      test("test that") {}
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"))) {
      a.groups
    }

    val b = new FunSuite {
      test("test this") {}
      ignore("test that") {}
    }
    expect(Map("test that" -> Set("org.scalatest.Ignore"))) {
      b.groups
    }

    val c = new FunSuite {
      ignore("test this") {}
      ignore("test that") {}
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"), "test that" -> Set("org.scalatest.Ignore"))) {
      c.groups
    }

    val d = new FunSuite {
      test("test this", mygroups.SlowAsMolasses) {}
      ignore("test that", mygroups.SlowAsMolasses) {}
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
      d.groups
    }

    val e = new FunSuite {}
    expect(Map()) {
      e.groups
    }

    val f = new FunSuite {
      test("test this", mygroups.SlowAsMolasses, mygroups.WeakAsAKitten) {}
      test("test that", mygroups.SlowAsMolasses) {}
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
      f.groups
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
    a.execute("test this")
    assert(a.theTestThisCalled)
    assert(!a.theTestThatCalled)

    val b = new MySuite
    b.execute()
    assert(b.theTestThisCalled)
    assert(b.theTestThatCalled)
  }

  class MyReporter extends Reporter {
    var testIgnoredCalled = false
    var lastReport: Report = null
    override def apply(event: Event) {
      super.apply(event)
    }
    override def testIgnored(report: Report) {
      testIgnoredCalled = true
      lastReport = report
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
    a.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repA.testIgnoredCalled)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }

    val repB = new MyReporter
    b.execute(None, repB, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repB.testIgnoredCalled)
    assert(repB.lastReport.name endsWith "test this")
    assert(!b.theTestThisCalled)
    assert(b.theTestThatCalled)

    val c = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this") { theTestThisCalled = true }
      ignore("test that") { theTestThatCalled = true }
    }

    val repC = new MyReporter
    c.execute(None, repC, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repC.testIgnoredCalled)
    assert(repC.lastReport.name endsWith "test that", repC.lastReport.name)
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
    d.execute(None, repD, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repD.testIgnoredCalled)
    assert(repD.lastReport.name endsWith "test that") // last because should be in order of appearance
    assert(!d.theTestThisCalled)
    assert(!d.theTestThatCalled)

    // If I provide a specific testName to execute, then it should ignore an Ignore on that test
    // method and actually invoke it.
    val e = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }

    val repE = new MyReporter
    e.execute(Some("test this"), repE, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repE.testIgnoredCalled)
    assert(e.theTestThisCalled)
  }

  def testExcludes() {

    val a = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", mygroups.SlowAsMolasses) { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }
    val repA = new MyReporter
    a.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repA.testIgnoredCalled)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", mygroups.SlowAsMolasses) { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }
    val repB = new MyReporter
    b.execute(None, repB, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set(), Map(), None)
    assert(!repB.testIgnoredCalled)
    assert(b.theTestThisCalled)
    assert(!b.theTestThatCalled)

    val c = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", mygroups.SlowAsMolasses) { theTestThisCalled = true }
      test("test that", mygroups.SlowAsMolasses) { theTestThatCalled = true }
    }
    val repC = new MyReporter
    c.execute(None, repB, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set(), Map(), None)
    assert(!repC.testIgnoredCalled)
    assert(c.theTestThisCalled)
    assert(c.theTestThatCalled)

    val d = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this", mygroups.SlowAsMolasses) { theTestThisCalled = true }
      test("test that", mygroups.SlowAsMolasses) { theTestThatCalled = true }
    }
    val repD = new MyReporter
    d.execute(None, repD, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.Ignore"), Map(), None)
    assert(repD.testIgnoredCalled)
    assert(!d.theTestThisCalled)
    assert(d.theTestThatCalled)

    val e = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", mygroups.SlowAsMolasses, mygroups.FastAsLight) { theTestThisCalled = true }
      test("test that", mygroups.SlowAsMolasses) { theTestThatCalled = true }
      test("test the other") { theTestTheOtherCalled = true }
    }
    val repE = new MyReporter
    e.execute(None, repE, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight"),
              Map(), None)
    assert(!repE.testIgnoredCalled)
    assert(!e.theTestThisCalled)
    assert(e.theTestThatCalled)
    assert(!e.theTestTheOtherCalled)

    val f = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignore("test this", mygroups.SlowAsMolasses, mygroups.FastAsLight) { theTestThisCalled = true }
      test("test that", mygroups.SlowAsMolasses) { theTestThatCalled = true }
      test("test the other") { theTestTheOtherCalled = true }
    }
    val repF = new MyReporter
    f.execute(None, repF, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight"),
              Map(), None)
    assert(!repF.testIgnoredCalled)
    assert(!f.theTestThisCalled)
    assert(f.theTestThatCalled)
    assert(!f.theTestTheOtherCalled)

    val g = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", mygroups.SlowAsMolasses, mygroups.FastAsLight) { theTestThisCalled = true }
      test("test that", mygroups.SlowAsMolasses) { theTestThatCalled = true }
      ignore("test the other") { theTestTheOtherCalled = true }
    }
    val repG = new MyReporter
    g.execute(None, repG, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight"),
              Map(), None)
    assert(!repG.testIgnoredCalled)
    assert(!g.theTestThisCalled)
    assert(g.theTestThatCalled)
    assert(!g.theTestTheOtherCalled)

    val h = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", mygroups.SlowAsMolasses, mygroups.FastAsLight) { theTestThisCalled = true }
      test("test that", mygroups.SlowAsMolasses) { theTestThatCalled = true }
      test("test the other") { theTestTheOtherCalled = true }
    }
    val repH = new MyReporter
    h.execute(None, repH, new Stopper {}, Set(), Set("org.scalatest.FastAsLight"), Map(), None)
    assert(!repH.testIgnoredCalled)
    assert(!h.theTestThisCalled)
    assert(h.theTestThatCalled)
    assert(h.theTestTheOtherCalled)

    val i = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", mygroups.SlowAsMolasses, mygroups.FastAsLight) { theTestThisCalled = true }
      test("test that", mygroups.SlowAsMolasses) { theTestThatCalled = true }
      test("test the other") { theTestTheOtherCalled = true }
    }
    val repI = new MyReporter
    i.execute(None, repI, new Stopper {}, Set(), Set("org.scalatest.SlowAsMolasses"), Map(), None)
    assert(!repI.testIgnoredCalled)
    assert(!i.theTestThisCalled)
    assert(!i.theTestThatCalled)
    assert(i.theTestTheOtherCalled)

    val j = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignore("test this", mygroups.SlowAsMolasses, mygroups.FastAsLight) { theTestThisCalled = true }
      ignore("test that", mygroups.SlowAsMolasses) { theTestThatCalled = true }
      test("test the other") { theTestTheOtherCalled = true }
    }
    val repJ = new MyReporter
    j.execute(None, repJ, new Stopper {}, Set(), Set("org.scalatest.SlowAsMolasses"), Map(), None)
    assert(!repI.testIgnoredCalled)
    assert(!j.theTestThisCalled)
    assert(!j.theTestThatCalled)
    assert(j.theTestTheOtherCalled)

    val k = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignore("test this", mygroups.SlowAsMolasses, mygroups.FastAsLight) { theTestThisCalled = true }
      ignore("test that", mygroups.SlowAsMolasses) { theTestThatCalled = true }
      ignore("test the other") { theTestTheOtherCalled = true }
    }
    val repK = new MyReporter
    k.execute(None, repK, new Stopper {}, Set(), Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore"), Map(), None)
    assert(repK.testIgnoredCalled)
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
      test("test this", mygroups.FastAsLight) {}
      test("test that") {}
    }
    assert(c.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(c.expectedTestCount(Set(), Set("org.scalatest.FastAsLight")) === 1)

    val d = new FunSuite {
      test("test this", mygroups.FastAsLight, mygroups.SlowAsMolasses) {}
      test("test that", mygroups.SlowAsMolasses) {}
      test("test the other thing") {}
    }
    assert(d.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(d.expectedTestCount(Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight")) === 1)
    assert(d.expectedTestCount(Set(), Set("org.scalatest.SlowAsMolasses")) === 1)
    assert(d.expectedTestCount(Set(), Set()) === 3)

    val e = new FunSuite {
      test("test this", mygroups.FastAsLight, mygroups.SlowAsMolasses) {}
      test("test that", mygroups.SlowAsMolasses) {}
      ignore("test the other thing") {}
    }
    assert(e.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(e.expectedTestCount(Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight")) === 1)
    assert(e.expectedTestCount(Set(), Set("org.scalatest.SlowAsMolasses")) === 0)
    assert(e.expectedTestCount(Set(), Set()) === 2)

    val f = new SuperSuite(List(a, b, c, d, e))
    assert(f.expectedTestCount(Set(), Set()) === 10)
  }

  def testThatTestMethodsWithNoGroupsDontShowUpInGroupsMap() {
    
    val a = new FunSuite {
      test("test not in a group") {}
    }
    assert(a.groups.keySet.size === 0)
  }

  def testThatTestFunctionsThatResultInNonUnitAreRegistered() {
    val a = new FunSuite {
      test("test this") { 1 }
      test("test that") { "hi" }
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)
    assert(a.testNames.size === 2)
    assert(a.groups.keySet.size === 0)
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
    a.execute()
    assert(a.fromConstructorTestExecuted)
    assert(!a.fromMethodTestExecuted)
    intercept[TestFailedException] {
      a.registerOne()
    }
    a.execute()
    assert(!a.fromMethodTestExecuted)
  }
  
  def testThatInfoInsideATestMethodGetsOutTheDoor() {
    class MyReporter extends Reporter {
      var infoProvidedCalled = false
      var lastReport: Report = null
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        infoProvidedCalled = true
        lastReport = report
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
    a.execute(None, myRep, new Stopper {}, Set(), Set(), Map(), None)
    assert(myRep.infoProvidedCalled)
    assert(myRep.lastReport.message === msg)
  }
  
  def testThatInfoInTheConstructorGetsOutTheDoor() {
    class MyReporter extends Reporter {
      var infoProvidedCalled = false
      var lastReport: Report = null
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        infoProvidedCalled = true
        lastReport = report
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
    a.execute(None, myRep, new Stopper {}, Set(), Set(), Map(), None)
    assert(myRep.infoProvidedCalled)
    assert(myRep.lastReport.message === msg)
  }
  
  def testThatInfoInTheConstructorBeforeATestHappensFirst() {
    var infoProvidedCalled = false
    var infoProvidedCalledBeforeTest = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        infoProvidedCalled = true
      }
    }
    val msg = "hi there, dude"
    class MySuite extends FunSuite {
      info(msg)
      test("test this") {
        if (infoProvidedCalled)
          infoProvidedCalledBeforeTest = true
      }
    }
    val a = new MySuite
    val myRep = new MyReporter
    a.execute(None, myRep, new Stopper {}, Set(), Set(), Map(), None)
    assert(infoProvidedCalledBeforeTest)
  }
  
  def testThatInfoInTheConstructorAfterATestHappensSecond() {
    var infoProvidedCalled = false
    var infoProvidedCalledAfterTest = true
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        infoProvidedCalled = true
      }
    }
    val msg = "hi there, dude"
    class MySuite extends FunSuite {
      test("test this") {
        if (infoProvidedCalled)
          infoProvidedCalledAfterTest = false
      }
      info(msg)
    }
    val a = new MySuite
    val myRep = new MyReporter
    a.execute(None, myRep, new Stopper {}, Set(), Set(), Map(), None)
    assert(infoProvidedCalledAfterTest)
    assert(infoProvidedCalled)
  }

  def callingTestFromWithinATestClauseResultsInATestFailedErrorAtRuntime() {

    var testFailedAdExpected = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testFailed(report: Report) {
        if (report.name.indexOf("this test should blow up") != -1)
          testFailedAdExpected = true
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
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedAdExpected)
  }

  def callingTestFromWithinATestWithGroupsClauseResultsInATestFailedErrorAtRuntime() {
    
    var testFailedAdExpected = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testFailed(report: Report) {
        if (report.name.indexOf("this test should blow up") != -1)
          testFailedAdExpected = true
      }
    }

    class MySuite extends FunSuite {
      test("this test should blow up") {
        test("is in the wrong place also", mygroups.SlowAsMolasses) {
          assert(1 === 1)
        }
      }
    }

    val a = new MySuite
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedAdExpected)
  }

  def callingIgnoreFromWithinATestClauseResultsInATestFailedErrorAtRuntime() {
    
    var testFailedAdExpected = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testFailed(report: Report) {
        if (report.name.indexOf("this test should blow up") != -1)
          testFailedAdExpected = true
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
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedAdExpected)
  }

  def callingIgnoreWithGroupsFromWithinATestClauseResultsInATestFailedErrorAtRuntime() {
    
    var testFailedAdExpected = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testFailed(report: Report) {
        if (report.name.indexOf("this test should blow up") != -1)
          testFailedAdExpected = true
      }
    }

    class MySuite extends FunSuite {
      test("this test should blow up") {
        ignore("is in the wrong place also", mygroups.SlowAsMolasses) {
          assert(1 === 1)
        }
      }
    }

    val a = new MySuite
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedAdExpected)
  }
}


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

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.events.Event

class SpecSuite extends FunSuite {

  test("calling a describe from within an it clause results in a TestFailedError at runtime") {
    
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

    class MySpec extends Spec {
      it("this test should blow up") {
        describe("in the wrong place, at the wrong time") {
        }
      }
    }

    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedAdExpected)
  }

  test("calling a describe with a nested it from within an it clause results in a TestFailedError at runtime") {
    
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

    class MySpec extends Spec {
      it("this test should blow up") {
        describe("in the wrong place, at the wrong time") {
          it("is in the wrong place also") {
            assert(1 === 1)
          }
        }
      }
    }

    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedAdExpected)
  }

  test("calling an it from within an it clause results in a TestFailedError at runtime") {
    
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

    class MySpec extends Spec {
      it("this test should blow up") {
        it("is in the wrong place also") {
          assert(1 === 1)
        }
      }
    }

    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedAdExpected)
  }

  test("calling an it from within an it with groups clause results in a TestFailedError at runtime") {
    
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

    class MySpec extends Spec {
      it("this test should blow up") {
        it("is in the wrong place also", mygroups.SlowAsMolasses) {
          assert(1 === 1)
        }
      }
    }

    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedAdExpected)
  }

  test("calling an ignore from within an it clause results in a TestFailedError at runtime") {
    
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

    class MySpec extends Spec {
      it("this test should blow up") {
        ignore("is in the wrong place also") {
          assert(1 === 1)
        }
      }
    }

    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedAdExpected)
  }

  test("calling an ignore with groups from within an it clause results in a TestFailedError at runtime") {
    
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

    class MySpec extends Spec {
      it("this test should blow up") {
        ignore("is in the wrong place also", mygroups.SlowAsMolasses) {
          assert(1 === 1)
        }
      }
    }

    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedAdExpected)
  }

  test("groups work correctly in Spec") {
    
    val d = new Spec {
      it("test this", mygroups.SlowAsMolasses) {}
      ignore("test that", mygroups.SlowAsMolasses) {}
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
      d.groups
    }

    val e = new Spec {}
    expect(Map()) {
      e.groups
    }

    val f = new Spec {
      it("test this", mygroups.SlowAsMolasses, mygroups.WeakAsAKitten) {}
      it("test that", mygroups.SlowAsMolasses) {}
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
      f.groups
    }
  }

  test("duplicate test names should generate an exception") {

    intercept[TestFailedException] {
      new Spec {
        it("test this") {}
        it("test this") {}
      }
    }
    intercept[TestFailedException] {
      new Spec {
        it("test this") {}
        ignore("test this") {}
      }
    }
    intercept[TestFailedException] {
      new Spec {
        ignore("test this") {}
        ignore("test this") {}
      }
    }
    intercept[TestFailedException] {
      new Spec {
        ignore("test this") {}
        it("test this") {}
      }
    }
  }

  test("make sure ignored examples show up in groups list") {

    val a = new Spec {
      ignore("test this") {}
      it("test that") {}
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"))) {
      a.groups
    }

    val b = new Spec {
      it("test this") {}
      ignore("test that") {}
    }
    expect(Map("test that" -> Set("org.scalatest.Ignore"))) {
      b.groups
    }

    val c = new Spec {
      ignore("test this") {}
      ignore("test that") {}
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"), "test that" -> Set("org.scalatest.Ignore"))) {
      c.groups
    }

    val d = new Spec {
      it("test this") {}
      it("test that") {}
    }
    expect(Map()) {
      d.groups
    }
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

  test("make sure ignored tests don't get executed") {

    val a = new Spec {
      var theTestThisCalled = false
      var theTestThatCalled = false
      it("test this") { theTestThisCalled = true }
      it("test that") { theTestThatCalled = true }
    }

    val repA = new MyReporter
    a.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repA.testIgnoredCalled)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new Spec {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      it("test that") { theTestThatCalled = true }
    }

    val repB = new MyReporter
    b.execute(None, repB, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repB.testIgnoredCalled)
    assert(repB.lastReport.name endsWith "test this")
    assert(!b.theTestThisCalled)
    assert(b.theTestThatCalled)

    val c = new Spec {
      var theTestThisCalled = false
      var theTestThatCalled = false
      it("test this") { theTestThisCalled = true }
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
    val d = new Spec {
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
    val e = new Spec {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      it("test that") { theTestThatCalled = true }
    }

    val repE = new MyReporter
    e.execute(Some("test this"), repE, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repE.testIgnoredCalled)
    assert(e.theTestThisCalled)
  }

  test("three plain-old specifiers should be invoked in order") {
    class MySpec extends Spec with ShouldMatchers {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      it("should get invoked") {
        example1WasInvoked = true
      }
      it("should also get invoked") {
        if (example1WasInvoked)
          example2WasInvokedAfterExample1 = true
      }
      it("should also also get invoked") {
        if (example2WasInvokedAfterExample1)
          example3WasInvokedAfterExample2 = true
      }
    }
    val a = new MySpec
    a.execute()
    assert(a.example1WasInvoked)
    assert(a.example2WasInvokedAfterExample1)
    assert(a.example3WasInvokedAfterExample2)
  }

  test("three plain-old specifiers should be invoked in order when two are surrounded by a plain-old describe") {
    class MySpec extends Spec with ShouldMatchers {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      it("should get invoked") {
        example1WasInvoked = true
      }
      describe("Stack") {
        it("should also get invoked") {
          if (example1WasInvoked)
            example2WasInvokedAfterExample1 = true
        }
        it("should also also get invoked") {
          if (example2WasInvokedAfterExample1)
            example3WasInvokedAfterExample2 = true
        }
      }
    }
    val a = new MySpec
    a.execute()
    assert(a.example1WasInvoked)
    assert(a.example2WasInvokedAfterExample1)
    assert(a.example3WasInvokedAfterExample2)
  }
   
  test("two plain-old specifiers should show up in order of appearance in testNames") {
    class MySpec extends Spec with ShouldMatchers {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      it("should get invoked") {
        example1WasInvoked = true
      }
      it("should also get invoked") {
        if (example1WasInvoked)
          example2WasInvokedAfterExample1 = true
      }
    }
    val a = new MySpec
    a.execute()
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "should get invoked")
    assert(a.testNames.elements.toList(1) === "should also get invoked")
  }
 
  test("plain-old specifier test names should include an enclosing describe string, separated by a space") {
    class MySpec extends Spec with ShouldMatchers {
      describe("A Stack") {
        it("must allow me to pop") {}
        it("must allow me to push") {}
      }
    }
    val a = new MySpec
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "A Stack must allow me to pop")
    assert(a.testNames.elements.toList(1) === "A Stack must allow me to push")
  }

  test("plain-old test names should properly nest plain-old descriptions in test names") {
    class MySpec extends Spec with ShouldMatchers {
      describe("A Stack") {
        describe("(when not empty)") {
          it("must allow me to pop") {}
        }
        describe("(when not full)") {
          it("must allow me to push") {}
        }
      }
    }
    val a = new MySpec
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "A Stack (when not empty) must allow me to pop")
    assert(a.testNames.elements.toList(1) === "A Stack (when not full) must allow me to push")
  }
  
  test("should be able to mix in BeforeAndAfter without any problems") {
    class MySpec extends Spec with ShouldMatchers with BeforeAndAfter {
      describe("A Stack") {
        describe("(when not empty)") {
          it("should allow me to pop") {}
        }
        describe("(when not full)") {
          it("should allow me to push") {}
        }
      }
    }
    val a = new MySpec
    a.execute()
  }
  
  // Test for good strings in report for top-level examples  
  test("Top-level plain-old specifiers should yield good strings in a testStarting report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testStarting(report: Report) {
        if (report.name.indexOf("must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("must start with proper words") {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }
    
  test("Top-level plain-old specifiers should yield good strings in a testSucceeded report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testSucceeded(report: Report) {
        if (report.name.indexOf("must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("must start with proper words") {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }
  
  test("Top-level plain-old specifiers should yield good strings in a testFailed report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testFailed(report: Report) {
        if (report.name.indexOf("must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("must start with proper words") { fail() }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  // Tests for good strings in report for nested-one-level examples
  test("Nested-one-level plain-old specifiers should yield good strings in a testStarting report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "My Spec")
              infoReportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "My Spec")
              infoReportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
      override def testStarting(report: Report) {
        // infoProvided should be invoked before the this method
        assert(infoProvidedHasBeenInvoked)
        theOtherMethodHasBeenInvoked = true
        if (report.name.indexOf("My Spec must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      describe("My Spec") {
        it("must start with proper words") {}
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
    assert(infoReportHadCorrectTestName)
    assert(infoReportHadCorrectSpecText)
    assert(infoReportHadCorrectFormattedSpecText)
  }

  test("Nested-one-level plain-old specifiers should yield good strings in a testSucceeded report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "My Spec")
              infoReportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "My Spec")
              infoReportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
      override def testSucceeded(report: Report) {
        // infoProvided should be invoked before the this method
        assert(infoProvidedHasBeenInvoked)
        theOtherMethodHasBeenInvoked = true
        if (report.name.indexOf("My Spec must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      describe("My Spec") {
        it("must start with proper words") {}
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
    assert(infoReportHadCorrectTestName)
    assert(infoReportHadCorrectSpecText)
    assert(infoReportHadCorrectFormattedSpecText)
  }

  test("Nested-one-level plain-old specifiers should yield good strings in a testFailed report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "My Spec")
              infoReportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "My Spec")
              infoReportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
      override def testFailed(report: Report) {
        // infoProvided should be invoked before the this method
        assert(infoProvidedHasBeenInvoked)
        theOtherMethodHasBeenInvoked = true
        if (report.name.indexOf("My Spec must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      describe("My Spec") {
        it("must start with proper words") { fail() }
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
    assert(infoReportHadCorrectTestName)
    assert(infoReportHadCorrectSpecText)
    assert(infoReportHadCorrectFormattedSpecText)
  }

  
  // Tests for good strings in report for nested-two-levels examples
  test("Nested-two-levels plain-old specifiers should yield good strings in a testStarting report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "My Spec")
              infoReportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "My Spec")
              infoReportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
      override def testStarting(report: Report) {
        // infoProvided should be invoked before the this method
        assert(infoProvidedHasBeenInvoked)
        theOtherMethodHasBeenInvoked = true
        if (report.name.indexOf("My Spec must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      describe("My") {
        describe("Spec") {
          it("must start with proper words") {}
        }
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
    assert(infoReportHadCorrectTestName)
    assert(infoReportHadCorrectSpecText)
    assert(infoReportHadCorrectFormattedSpecText)
  }

  test("Nested-two-levels plain-old specifiers should yield good strings in a testSucceeded report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "My Spec")
              infoReportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "My Spec")
              infoReportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
      override def testSucceeded(report: Report) {
        // infoProvided should be invoked before the this method
        assert(infoProvidedHasBeenInvoked)
        theOtherMethodHasBeenInvoked = true
        if (report.name.indexOf("My Spec must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      describe("My") {
        describe("Spec") {
          it("must start with proper words") {}
        }
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
    assert(infoReportHadCorrectTestName)
    assert(infoReportHadCorrectSpecText)
    assert(infoReportHadCorrectFormattedSpecText)
  }

  test("Nested-two-levels plain-old specifiers should yield good strings in a testFailed report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "My Spec")
              infoReportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "My Spec")
              infoReportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
      override def testFailed(report: Report) {
        // infoProvided should be invoked before the this method
        assert(infoProvidedHasBeenInvoked)
        theOtherMethodHasBeenInvoked = true
        if (report.name.indexOf("My Spec must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      describe("My") {
        describe("Spec") {
          it("must start with proper words") { fail() }
        }
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
    assert(infoReportHadCorrectTestName)
    assert(infoReportHadCorrectSpecText)
    assert(infoReportHadCorrectFormattedSpecText)
  }

    // Test for good strings in report for top-level shared behavior examples
  test("Top-level 'shared behavior - fancy specifiers' should yield good strings in a testStarting report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testStarting(report: Report) {
        if (report.name.indexOf("it should start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "it should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      def myBehavior(i: Int) {
        it("it should start with proper words") {}
      }
      1 should behave like myBehavior
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }
  
  test("Top-level 'shared behavior - plain-old specifiers' should yield good strings in a testStarting report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testStarting(report: Report) {
        if (report.name.indexOf("must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      def myBehavior(i: Int) {
        it("must start with proper words") {}
      }
      1 should behave like myBehavior
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  test("Top-level 'shared behavior - plain-old specifiers' should yield good strings in a testSucceeded report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testSucceeded(report: Report) {
        if (report.name.indexOf("must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      def myBehavior(i: Int) {
        it("must start with proper words") {}
      }
      1 should behave like myBehavior
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  test("Top-level 'shared behavior - plain-old specifiers' should yield good strings in a testFailed report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testFailed(report: Report) {
        if (report.name.indexOf("must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      def myBehavior(i: Int) {
        it("must start with proper words") { fail() }
      }
      1 should behave like myBehavior
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  // Tests for good strings in report for shared-behavior, nested-one-level specifiers
  test("Nested-one-level 'shared behavior' should yield good strings in a testStarting report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "My Spec")
              infoReportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "My Spec")
              infoReportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
      override def testStarting(report: Report) {
        // infoProvided should be invoked before the this method
        assert(infoProvidedHasBeenInvoked)
        theOtherMethodHasBeenInvoked = true
        if (report.name.indexOf("My Spec should start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      def myBehavior(i: Int) {
        it("should start with proper words") {}
      }
      describe("My Spec") {
        1 should behave like myBehavior
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
    assert(infoReportHadCorrectTestName)
    assert(infoReportHadCorrectSpecText)
    assert(infoReportHadCorrectFormattedSpecText)
  }

  // Huh? what was I testing here?
  test("An empty describe shouldn't throw an exception") {
    class MySpec extends Spec with ShouldMatchers {
      describe("this will be empty") {}
    }
    val a = new MySpec
    a.execute()
  }  
  
  test("Only a passed test name should be invoked.") {
    var correctTestWasInvoked = false
    var wrongTestWasInvoked = false
    class MySpec extends Spec with ShouldMatchers {
      it("it should be invoked") {
        correctTestWasInvoked = true
      }
      it("it should not be invoked") {
        wrongTestWasInvoked = true
      }
    }
    val a = new MySpec
    a.execute(Some("it should be invoked"), StubReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(correctTestWasInvoked)
    assert(!wrongTestWasInvoked)
  }
  
  test("Goodies should make it through to runTest") {
    var foundMyGoodie = false
    class MySpec extends Spec with ShouldMatchers {
      override def runTest(testName: String, reporter: Reporter, stopper: Stopper, goodies: Map[String, Any]) {
        foundMyGoodie = goodies.contains("my goodie")
        super.runTest(testName, reporter, stopper, goodies)
      }
      it("it should find my goodie") {}
    }
    val a = new MySpec
    a.execute(None, StubReporter, new Stopper {}, Set(), Set(), Map("my goodie" -> "hi"), None)
    assert(foundMyGoodie)  
  }
  
  // I think delete this one. Repeat.
  test("In a testStarting report, the example name should start with '<description> should' if nested two levels inside describe clauses") {
    var testStartingReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testStarting(report: Report) {
        if (report.name.indexOf("A Stack (when working right) should push and pop properly") != -1) {
          testStartingReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      describe("A Stack") {
        describe("(when working right)") {
          it("should push and pop properly") {}
        }
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectTestName)
  }
  
  test("expectedTestCount is the number of plain-old specifiers if no shares") {
    class MySpec extends Spec with ShouldMatchers {
      it("must one") {}
      it("must two") {}
      describe("behavior") {
        it("must three") {}  
        it("must four") {}
      }
      it("must five") {}
    }
    val a = new MySpec
    assert(a.expectedTestCount(Set(), Set()) === 5)
  }

  // Testing strings sent in reports
  test("In a testStarting report, the example name should be verbatim if top level if example registered with it") {
    var testStartingReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testStarting(report: Report) {
        if (report.name.indexOf("this thing must start with proper words") != -1) {
          testStartingReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("this thing must start with proper words") {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectTestName)
  }

  test("In a testSucceeded report, the example name should be verbatim if top level if example registered with it") {
    var testSucceededReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testSucceeded(report: Report) {
        if (report.name.indexOf("this thing must start with proper words") != -1) {
          testSucceededReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("this thing must start with proper words") {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testSucceededReportHadCorrectTestName)
  }

  test("In a testFailed report, the example name should be verbatim if top level if example registered with it") {
    var testFailedReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testFailed(report: Report) {
        if (report.name.indexOf("this thing must start with proper words") != -1) {
          testFailedReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("this thing must start with proper words") { fail() }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedReportHadCorrectTestName)
  }
  
  test("In a testStarting report, the example name should start with '<description> ' if nested one level " +
        "inside a describe clause and registered with it") {
    var testStartingReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testStarting(report: Report) {
        if (report.name.indexOf("A Stack needs to push and pop properly") != -1) {
          testStartingReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      describe("A Stack") {
        it("needs to push and pop properly") {}
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectTestName)
  }
    
  test("Specs should send SpecReports") {
    class MyReporter extends Reporter {
      var gotANonSpecReport = false
      var lastNonSpecReport: Option[Report] = None
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testStarting(report: Report) {
        ensureSpecReport(report)
      }

      private def ensureSpecReport(report: Report) {
	    report match {
          case sr: SpecReport => 
          case r: Report => {
            gotANonSpecReport = true
            lastNonSpecReport = Some(report)
          }
        }
      }
      override def testSucceeded(report: Report) {
        ensureSpecReport(report)
      }
	    
      override def testIgnored(report: Report) {
        ensureSpecReport(report)
      }
	
      override def testFailed(report: Report) {
        ensureSpecReport(report)
      }
	
      override def infoProvided(report: Report) {
        ensureSpecReport(report)
      }
	
      override def suiteStarting(report: Report) {
        ensureSpecReport(report)
      }
	
      override def suiteCompleted(report: Report) {
        ensureSpecReport(report)
      }
	
      override def suiteAborted(report: Report) {
        ensureSpecReport(report)
      }
	
      override def runAborted(report: Report) {
        ensureSpecReport(report)
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("it should send SpecReports") {
        assert(true)
      }
      it("it should also send SpecReports") {
        assert(false)
      }
    }
    val a = new MySpec
    val myRep = new MyReporter
    a.execute(None, myRep, new Stopper {}, Set(), Set(), Map(), None)
    assert(!myRep.gotANonSpecReport)
  }

  test("SpecText should come through correctly in a SpecReport when registering with it") {
    var testStartingReportHadCorrectSpecText = false
    var lastSpecText: Option[String] = None
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testStarting(report: Report) {
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "My spec text must have the proper words")
              testStartingReportHadCorrectSpecText = true
            else
              lastSpecText = Some(specReport.plainSpecText)
          case _ => throw new RuntimeException("Got a non-SpecReport")
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("My spec text must have the proper words") {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectSpecText, lastSpecText match { case Some(s) => s; case None => "No report"})
  }

  test("Spec text should come through correctly in a SpecReport when registering with it when nested in one describe") {
    var testStartingReportHadCorrectSpecText = false
    var lastSpecText: Option[String] = None
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testStarting(report: Report) {
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "My short name must have the proper words")
              testStartingReportHadCorrectSpecText = true
            else
              lastSpecText = Some(specReport.plainSpecText)
          case _ => throw new RuntimeException("Got a non-SpecReport")
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      describe("A Stack") {
        it("My short name must have the proper words") {}
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectSpecText, lastSpecText match { case Some(s) => s; case None => "No report"})
  }

  test("Spec text should come through correctly in a SpecReport when registering with it when nested in two describes") {
    var testStartingReportHadCorrectSpecText = false
    var lastSpecText: Option[String] = None
    class MyReporter extends Reporter {
      override def apply(event: Event) {
        super.apply(event)
      }
      override def testStarting(report: Report) {
        report match {
          case specReport: SpecReport =>
            if (specReport.plainSpecText == "My short name must have the proper words")
              testStartingReportHadCorrectSpecText = true
            else
              lastSpecText = Some(specReport.plainSpecText)
          case _ => throw new RuntimeException("Got a non-SpecReport")
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      describe("A Stack") {
        describe("(when empty)") {
          it("My short name must have the proper words") {}
        }
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectSpecText, lastSpecText match { case Some(s) => s; case None => "No report"})
  }

  ignore("A specifyGivenReporter clause should be able to send info to the reporter") {

    val expectedMessage = "this is the expected message"

    class MyReporter extends Reporter {
      var infoProvidedCalled = false
      var expectedMessageReceived = false
      var lastReport: Report = null
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        infoProvidedCalled = true
        if (!expectedMessageReceived) {
          expectedMessageReceived = report.message.indexOf(expectedMessage) != -1
        }
      }
    }

    class MySpec extends Spec with ShouldMatchers {
      describe("A Stack") {
        describe("(when not empty)") {
          it("might allow me to pop") {
            val report = new Report("myName", expectedMessage)
            // info(report)
            ()
          }
        }
        describe("(when not full)") {
          it("allow me to push") {}
        }
      }
    }
    
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
  }

  test("Should get infoProvided with description if one and only one describe clause") {

    val expectedSpecText = "A Stack"

    class MyReporter extends Reporter {
      var infoProvidedCalled = false
      var expectedMessageReceived = false
      var lastReport: Report = null
      override def apply(event: Event) {
        super.apply(event)
      }
      override def infoProvided(report: Report) {
        report match {
          case specReport: SpecReport => {
            infoProvidedCalled = true
            if (!expectedMessageReceived) {
              expectedMessageReceived = (specReport.plainSpecText == expectedSpecText)
            }
          }
          case _ =>
        }
      }
    }

    class MySpec extends Spec with ShouldMatchers {
      describe("A Stack") {
        it("should allow me to push") {}
      }
    }
    
    val a = new MySpec
    val myRep = new MyReporter
    a.execute(None, myRep, new Stopper {}, Set(), Set(), Map(), None)
    assert(myRep.infoProvidedCalled)
    assert(myRep.expectedMessageReceived)
  }
  
  ignore("That level gets sent correctly if no describe clauses.") {

    class MyReporter extends Reporter {

      val ExpectedLevel = 0
      var testSucceededCalled = false
      var testFailedCalled = false
      var expectedLevelReceivedByTestSucceeded = false
      var expectedLevelReceivedByTestFailed = false
 
      override def apply(event: Event) {
        super.apply(event)
      }

      override def testSucceeded(report: Report) {
        report match {
          case specReport: SpecReport => {
            testSucceededCalled = true
            if (!expectedLevelReceivedByTestSucceeded) {
              // expectedLevelReceivedByTestSucceeded = (specReport.level == ExpectedLevel)
            }
          }
          case _ =>
        }
      }
 
      override def testFailed(report: Report) {
        report match {
          case specReport: SpecReport => {
            testFailedCalled = true
            if (!expectedLevelReceivedByTestFailed) {
              // expectedLevelReceivedByTestFailed = (specReport.level == ExpectedLevel)
            }
          }
          case _ =>
        }
      }
    }

    class MySpec extends Spec with ShouldMatchers {
      it("it should be at level 0") {}
      it("it should also be at level 0") { fail() }
    }
    val a = new MySpec
    val myRep = new MyReporter
    a.execute(None, myRep, new Stopper {}, Set(), Set(), Map(), None)
    assert(myRep.testSucceededCalled)
    assert(myRep.testFailedCalled)
    assert(myRep.expectedLevelReceivedByTestSucceeded)
    assert(myRep.expectedLevelReceivedByTestFailed)
  }
  
  ignore("That level gets sent correctly if one and only one describe clause.") {

    class MyReporter extends Reporter {

      val ExpectedLevelForDescribe = 0
      val ExpectedLevelForExamples = 1
      var testSucceededCalled = false
      var testFailedCalled = false
      var infoProvidedCalled = false
      var expectedLevelReceivedByTestSucceeded = false
      var expectedLevelReceivedByTestFailed = false
      var expectedLevelReceivedByInfoProvided = false
 
      override def apply(event: Event) {
        super.apply(event)
      }

      override def testSucceeded(report: Report) {
        report match {
          case specReport: SpecReport => {
            testSucceededCalled = true
            if (!expectedLevelReceivedByTestSucceeded) {
              // expectedLevelReceivedByTestSucceeded = (specReport.level == ExpectedLevelForExamples)
            }
          }
          case _ =>
        }
      }
 
      override def testFailed(report: Report) {
        report match {
          case specReport: SpecReport => {
            testFailedCalled = true
            if (!expectedLevelReceivedByTestFailed) {
              // expectedLevelReceivedByTestFailed = (specReport.level == ExpectedLevelForExamples)
            }
          }
          case _ =>
        }
      }
 
      override def infoProvided(report: Report) {
        report match {
          case specReport: SpecReport => {
            infoProvidedCalled = true
            if (!expectedLevelReceivedByInfoProvided) {
              // expectedLevelReceivedByInfoProvided = (specReport.level == ExpectedLevelForDescribe)
            }
          }
          case _ =>
        }
      }
    }

    class MySpec extends Spec with ShouldMatchers {
      describe("my describe clause") {
        it("should be at level 1") {}
        it("should also be at level 1") { fail() }
      }
    }
    val a = new MySpec
    val myRep = new MyReporter
    a.execute(None, myRep, new Stopper {}, Set(), Set(), Map(), None)
    assert(myRep.testSucceededCalled)
    assert(myRep.testFailedCalled)
    assert(myRep.infoProvidedCalled)
    assert(myRep.expectedLevelReceivedByTestSucceeded)
    assert(myRep.expectedLevelReceivedByTestFailed)
    assert(myRep.expectedLevelReceivedByInfoProvided)
  }
  
 
  // Testing Shared behaviors
  test("a shared specifier invoked with 'should behave like a' should get invoked") {
    class MySpec extends Spec with ShouldMatchers with BeforeAndAfter {
      var sharedExampleInvoked = false
      def invocationVerifier(i: Int) {
        it("should be invoked") {
          sharedExampleInvoked = true
        }
      }
      describe("A Stack") {
        describe("(when not empty)") {
          it("should allow me to pop") {}
          1 should behave like invocationVerifier
        }
        describe("(when not full)") {
          it("should allow me to push") {}
        }
      }
    }
    val a = new MySpec
    a.execute()
    assert(a.sharedExampleInvoked)
  }
  
  test("two examples in a shared behavior should get invoked") {
    class MySpec extends Spec with ShouldMatchers with BeforeAndAfter {
      var sharedExampleInvoked = false
      var sharedExampleAlsoInvoked = false
      def invocationVerifier(i: Int) {
        it("should be invoked") {
          sharedExampleInvoked = true
        }
        it("should also be invoked") {
          sharedExampleAlsoInvoked = true
        }
      }
      describe("A Stack") {
        describe("(when not empty)") {
          it("should allow me to pop") {}
          1 should behave like invocationVerifier
        }
        describe("(when not full)") {
          it("should allow me to push") {}
        }
      }
    }
    val a = new MySpec
    a.execute()
    assert(a.sharedExampleInvoked)
    assert(a.sharedExampleAlsoInvoked)
  }

  test("three examples in a shared behavior should be invoked in order") {
    class MySpec extends Spec with ShouldMatchers {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      def invocationVerifier(i: Int) {
        it("should get invoked") {
          example1WasInvoked = true
        }
        it("should also get invoked") {
          if (example1WasInvoked)
            example2WasInvokedAfterExample1 = true
        }
        it("should also also get invoked") {
          if (example2WasInvokedAfterExample1)
            example3WasInvokedAfterExample2 = true
        }
      }
      1 should behave like invocationVerifier
    }
    val a = new MySpec
    a.execute()
    assert(a.example1WasInvoked)
    assert(a.example2WasInvokedAfterExample1)
    assert(a.example3WasInvokedAfterExample2)
  }
  
  test("three examples in a shared behavior should not get invoked at all if the behavior isn't used in a like clause") {
    class MySpec extends Spec with ShouldMatchers {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      def invocationVerifier(i: Int) {
        it("should get invoked") {
          example1WasInvoked = true
        }
        it("should also get invoked") {
          if (example1WasInvoked)
            example2WasInvokedAfterExample1 = true
        }
        it("should also also get invoked") {
          if (example2WasInvokedAfterExample1)
            example3WasInvokedAfterExample2 = true
        }
      }
      // don't use it: should behave like an InvocationVerifier()
    }
    val a = new MySpec
    a.execute()
    assert(!a.example1WasInvoked)
    assert(!a.example2WasInvokedAfterExample1)
    assert(!a.example3WasInvokedAfterExample2)
  }
  
  // Probably delete
  test("The test name for a shared specifier invoked with 'should behave like a' should be verbatim if top level") {
    var testStartingReportHadCorrectTestName = false
    class MyReporter extends Reporter {

      override def apply(event: Event) {
        super.apply(event)
      }

      override def testStarting(report: Report) {
        if (report.name.indexOf("it should be invoked") != -1) {
          testStartingReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec with ShouldMatchers with BeforeAndAfter {
      var sharedExampleInvoked = false
      def invocationVerifier(i: Int) {
        it("it should be invoked") {
          sharedExampleInvoked = true
        }
      }
      1 should behave like invocationVerifier
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectTestName)
  }
  
  ignore("The example name for a shared example invoked with 'it should behave like' should start with '<description> should' if nested one level in a describe clause") {
    var testStartingReportHadCorrectTestName = false
    class MyReporter extends Reporter {

      override def apply(event: Event) {
        super.apply(event)
      }

      override def testStarting(report: Report) {
        if (report.name.indexOf("A Stack should pop properly") != -1) {
          testStartingReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      var sharedExampleInvoked = false
      def invocationVerifier(i: Int) {
        it("should pop properly") {
          sharedExampleInvoked = true
        }
      }
      describe("A Stack") {
        1 should behave like invocationVerifier
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectTestName)
  }
 
  test("expectedTestCount should not include tests in shares if never called") {
    class MySpec extends Spec with ShouldMatchers {
      class Misbehavior extends Spec with ShouldMatchers {
        it("should six") {}
        it("should seven") {}
      }
      it("should one") {}
      it("should two") {}
      describe("behavior") {
        it("should three") {}
        it("should four") {}
      }
      it("should five") {}
    }
    val a = new MySpec
    assert(a.expectedTestCount(Set(), Set()) === 5)
  }

  test("expectedTestCount should include tests in a share that is called") {
    class MySpec extends Spec with ShouldMatchers {
      def misbehavior(i: Int) {
        it("should six") {}
        it("should seven") {}
      }
      it("should one") {}
      it("should two") {}
      describe("behavior") {
        it("should three") {}
        1 should behave like misbehavior
        it("should four") {}
      }
      it("should five") {}
    }
    val a = new MySpec
    assert(a.expectedTestCount(Set(), Set()) === 7)
  }

  test("expectedTestCount should include tests in a share that is called twice") {
    class MySpec extends Spec with ShouldMatchers {
      def misbehavior(i: Int) {
        it("should six") {}
        it("should seven") {}
      }
      it("should one") {}
      it("should two") {}
      describe("behavior") {
        it("should three") {}
        1 should behave like misbehavior
        it("should four") {}
      }
      it("should five") {}
      1 should behave like misbehavior
    }
    val a = new MySpec
    assert(a.expectedTestCount(Set(), Set()) === 9)
  }

  test("Spec's expectedTestCount includes tests in nested suites") {
    class TwoTestSpec extends Spec {
      it("should count this test") {}
      it("should count this test also") {}
    }
    class MySpec extends Spec {

      override def nestedSuites = List(new TwoTestSpec, new TwoTestSpec, new TwoTestSpec)

      it("should count this here test") {}
    }
    val mySpec = new MySpec
    assert(mySpec.expectedTestCount(Set(), Set()) === 7)
  }

  // End of Share stuff
  ignore("A given reporter clause should be able to send info to the reporter") {

    val expectedMessage = "this is the expected message"

    class MyReporter extends Reporter {
      var infoProvidedCalled = false
      var expectedMessageReceived = false
      var lastReport: Report = null

      override def apply(event: Event) {
        super.apply(event)
      }

      override def infoProvided(report: Report) {
        infoProvidedCalled = true
        if (!expectedMessageReceived) {
          expectedMessageReceived = report.message.indexOf(expectedMessage) != -1
        }
      }
    }

    class MySpec extends Spec with ShouldMatchers {
      describe("A Stack") {
        describe("(when not empty)") {
          it("should allow me to pop") {
            val report = new Report("myName", expectedMessage)
            // info(report)
            ()
          }
        }
        describe("(when not full)") {
          it("should allow me to push") {}
        }
      }
    }
    val a = new MySpec
    val myRep = new MyReporter
    a.execute(None, myRep, new Stopper {}, Set(), Set(), Map(), None)
    assert(myRep.infoProvidedCalled)
    assert(myRep.expectedMessageReceived)
  }

  test("that a null specText results in a thrown NPE at construction time") {
    intercept[NullPointerException] {
      new Spec {
        it(null) {}
      }
    }
    intercept[NullPointerException] {
      new Spec {
        ignore(null) {}
      }
    }
  }

  test("that a null test group results in a thrown NPE at construction time") {
    // it
    intercept[NullPointerException] {
      new Spec {
        it("hi", null) {}
      }
    }
    val caught = intercept[NullPointerException] {
      new Spec {
        it("hi", mygroups.SlowAsMolasses, null) {}
      }
    }
    assert(caught.getMessage === "a test group was null")
    intercept[NullPointerException] {
      new Spec {
        it("hi", mygroups.SlowAsMolasses, null, mygroups.WeakAsAKitten) {}
      }
    }
    // ignore
    intercept[NullPointerException] {
      new Spec {
        ignore("hi", null) {}
      }
    }
    val caught2 = intercept[NullPointerException] {
      new Spec {
        ignore("hi", mygroups.SlowAsMolasses, null) {}
      }
    }
    assert(caught2.getMessage === "a test group was null")
    intercept[NullPointerException] {
      new Spec {
        ignore("hi", mygroups.SlowAsMolasses, null, mygroups.WeakAsAKitten) {}
      }
    }
  }
/* remove if really ditching includeExamples
  test("that a null sharedExamples passed to includeExamples results in a thrown NPE at construction time") {
    intercept[NullPointerException] {
      new Spec {
        includeExamples(null)
      }
    }
  } */
}


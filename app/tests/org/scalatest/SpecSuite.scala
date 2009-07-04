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
import org.scalatest.events._

class SpecSuite extends FunSuite with SharedHelpers {

  test("calling a describe from within an it clause results in a TestFailedError at runtime") {
    
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

    class MySpec extends Spec {
      it("this test should blow up") {
        describe("in the wrong place, at the wrong time") {
        }
      }
    }

    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testFailedAsExpected)
  }

  info("This one is outside")

  test("calling a describe with a nested it from within an it clause results in a TestFailedError at runtime") {
    info("This one is inside")
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
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testFailedAsExpected)
  }

  test("calling an it from within an it clause results in a TestFailedError at runtime") {
    
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

    class MySpec extends Spec {
      it("this test should blow up") {
        it("is in the wrong place also") {
          assert(1 === 1)
        }
      }
    }

    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testFailedAsExpected)
  }

  test("calling an it from within an it with tags clause results in a TestFailedError at runtime") {
    
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

    class MySpec extends Spec {
      it("this test should blow up") {
        it("is in the wrong place also", mytags.SlowAsMolasses) {
          assert(1 === 1)
        }
      }
    }

    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testFailedAsExpected)
  }

  test("calling an ignore from within an it clause results in a TestFailedError at runtime") {
    
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

    class MySpec extends Spec {
      it("this test should blow up") {
        ignore("is in the wrong place also") {
          assert(1 === 1)
        }
      }
    }

    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testFailedAsExpected)
  }

  test("calling an ignore with tags from within an it clause results in a TestFailedError at runtime") {
    
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

    class MySpec extends Spec {
      it("this test should blow up") {
        ignore("is in the wrong place also", mytags.SlowAsMolasses) {
          assert(1 === 1)
        }
      }
    }

    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testFailedAsExpected)
  }

  test("tags work correctly in Spec") {
    
    val d = new Spec {
      it("test this", mytags.SlowAsMolasses) {}
      ignore("test that", mytags.SlowAsMolasses) {}
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
      d.tags
    }

    val e = new Spec {}
    expect(Map()) {
      e.tags
    }

    val f = new Spec {
      it("test this", mytags.SlowAsMolasses, mytags.WeakAsAKitten) {}
      it("test that", mytags.SlowAsMolasses) {}
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
      f.tags
    }
  }

  test("duplicate test names should generate an exception") {

    intercept[DuplicateTestNameException] {
      new Spec {
        it("test this") {}
        it("test this") {}
      }
    }
    intercept[DuplicateTestNameException] {
      new Spec {
        it("test this") {}
        ignore("test this") {}
      }
    }
    intercept[DuplicateTestNameException] {
      new Spec {
        ignore("test this") {}
        ignore("test this") {}
      }
    }
    intercept[DuplicateTestNameException] {
      new Spec {
        ignore("test this") {}
        it("test this") {}
      }
    }
  }

  test("make sure ignored examples show up in tags list") {

    val a = new Spec {
      ignore("test this") {}
      it("test that") {}
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"))) {
      a.tags
    }

    val b = new Spec {
      it("test this") {}
      ignore("test that") {}
    }
    expect(Map("test that" -> Set("org.scalatest.Ignore"))) {
      b.tags
    }

    val c = new Spec {
      ignore("test this") {}
      ignore("test that") {}
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"), "test that" -> Set("org.scalatest.Ignore"))) {
      c.tags
    }

    val d = new Spec {
      it("test this") {}
      it("test that") {}
    }
    expect(Map()) {
      d.tags
    }
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

  test("make sure ignored tests don't get run") {

    val a = new Spec {
      var theTestThisCalled = false
      var theTestThatCalled = false
      it("test this") { theTestThisCalled = true }
      it("test that") { theTestThatCalled = true }
    }

    val repA = new MyReporter
    a.run(None, repA, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(!repA.testIgnoredReceived)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new Spec {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      it("test that") { theTestThatCalled = true }
    }

    val repB = new MyReporter
    b.run(None, repB, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(repB.testIgnoredReceived)
    assert(repB.lastEvent.testName endsWith "test this")
    assert(!b.theTestThisCalled)
    assert(b.theTestThatCalled)

    val c = new Spec {
      var theTestThisCalled = false
      var theTestThatCalled = false
      it("test this") { theTestThisCalled = true }
      ignore("test that") { theTestThatCalled = true }
    }

    val repC = new MyReporter
    c.run(None, repC, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(repC.testIgnoredReceived)
    assert(repC.lastEvent.testName endsWith "test that", repC.lastEvent.testName)
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
    d.run(None, repD, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(repD.testIgnoredReceived)
    assert(repD.lastEvent.testName endsWith "test that") // last because should be in order of appearance
    assert(!d.theTestThisCalled)
    assert(!d.theTestThatCalled)

    // If I provide a specific testName to run, then it should ignore an Ignore on that test
    // method and actually invoke it.
    val e = new Spec {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      it("test that") { theTestThatCalled = true }
    }

    val repE = new MyReporter
    e.run(Some("test this"), repE, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(!repE.testIgnoredReceived)
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
    a.run()
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
    a.run()
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
    a.run()
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
    a.run()
  }
  
  // Test for good strings in report for top-level examples  
  test("Top-level plain-old specifiers should yield good strings in a TestSucceeded report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            if (testName.indexOf("must start with proper words") != -1)
              reportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "must start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- must start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("must start with proper words") {}
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }
    
  test("Top-level plain-old specifiers should yield good strings in a testSucceeded report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            if (testName.indexOf("must start with proper words") != -1)
              reportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "must start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- must start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("must start with proper words") {}
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  test("Top-level plain-old specifiers should yield good strings in a testFailed report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case event: TestFailed =>
            if (event.testName.indexOf("must start with proper words") != -1)
              reportHadCorrectTestName = true
            event.formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "must start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- must start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("must start with proper words") { fail() }
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  // Tests for good strings in report for nested-one-level examples
  test("Nested-one-level plain-old specifiers should yield good strings in a TestSucceeded report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case InfoProvided(ordinal, message, nameInfo, throwable, formatter, payload, threadName, timeStamp) =>
            // infoProvided should be invoked before the other method
            assert(!theOtherMethodHasBeenInvoked)
            infoProvidedHasBeenInvoked = true
            if (message.indexOf("My Spec") != -1)
              infoReportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "My Spec")
                  infoReportHadCorrectSpecText = true
                if (formattedText == "My Spec")
                  infoReportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            // infoProvided should be invoked before the this method
            assert(infoProvidedHasBeenInvoked)
            theOtherMethodHasBeenInvoked = true
            if (testName.indexOf("My Spec must start with proper words") != -1)
              reportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "must start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- must start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
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
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
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
      def apply(event: Event) {
        event match {
          case InfoProvided(ordinal, message, nameInfo, throwable, formatter, payload, threadName, timeStamp) =>
            // infoProvided should be invoked before the other method
            assert(!theOtherMethodHasBeenInvoked)
            infoProvidedHasBeenInvoked = true
            if (message.indexOf("My Spec") != -1)
              infoReportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "My Spec")
                  infoReportHadCorrectSpecText = true
                if (formattedText == "My Spec")
                  infoReportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            // infoProvided should be invoked before the this method
            assert(infoProvidedHasBeenInvoked)
            theOtherMethodHasBeenInvoked = true
            if (testName.indexOf("My Spec must start with proper words") != -1)
              reportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "must start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- must start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
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
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
    assert(infoReportHadCorrectTestName)
    assert(infoReportHadCorrectSpecText)
    assert(infoReportHadCorrectFormattedSpecText)
  }

  test("Nested-one-level plain-old specifiers should yield good strings in a TestFailed report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case InfoProvided(ordinal, message, nameInfo, throwable, formatter, payload, threadName, timeStamp) =>
            // infoProvided should be invoked before the other method
            assert(!theOtherMethodHasBeenInvoked)
            infoProvidedHasBeenInvoked = true
            if (message.indexOf("My Spec") != -1)
              infoReportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "My Spec")
                  infoReportHadCorrectSpecText = true
                if (formattedText == "My Spec")
                  infoReportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case event: TestFailed =>
            // infoProvided should be invoked before the this method
            assert(infoProvidedHasBeenInvoked)
            theOtherMethodHasBeenInvoked = true
            if (event.testName.indexOf("My Spec must start with proper words") != -1)
              reportHadCorrectTestName = true
            event.formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "must start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- must start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
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
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
    assert(infoReportHadCorrectTestName)
    assert(infoReportHadCorrectSpecText)
    assert(infoReportHadCorrectFormattedSpecText)
  }

  
  // Tests for good strings in report for nested-two-levels examples
  test("Nested-two-levels plain-old specifiers should yield good strings in a TestSucceeded report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case InfoProvided(ordinal, message, nameInfo, throwable, formatter, payload, threadName, timeStamp) =>
            // infoProvided should be invoked before the other method
            assert(!theOtherMethodHasBeenInvoked)
            infoProvidedHasBeenInvoked = true
            if (message.indexOf("My Spec") != -1)
              infoReportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "My Spec")
                  infoReportHadCorrectSpecText = true
                if (formattedText == "My Spec")
                  infoReportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            // infoProvided should be invoked before the this method
            assert(infoProvidedHasBeenInvoked)
            theOtherMethodHasBeenInvoked = true
            if (testName.indexOf("My Spec must start with proper words") != -1)
              reportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "must start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- must start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
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
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
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
      def apply(event: Event) {
        event match {
          case InfoProvided(ordinal, message, nameInfo, throwable, formatter, payload, threadName, timeStamp) =>
            // infoProvided should be invoked before the other method
            assert(!theOtherMethodHasBeenInvoked)
            infoProvidedHasBeenInvoked = true
            if (message.indexOf("My Spec") != -1)
              infoReportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "My Spec")
                  infoReportHadCorrectSpecText = true
                if (formattedText == "My Spec")
                  infoReportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            // infoProvided should be invoked before the this method
            assert(infoProvidedHasBeenInvoked)
            theOtherMethodHasBeenInvoked = true
            if (testName.indexOf("My Spec must start with proper words") != -1)
              reportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "must start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- must start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
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
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
    assert(infoReportHadCorrectTestName)
    assert(infoReportHadCorrectSpecText)
    assert(infoReportHadCorrectFormattedSpecText)
  }

  test("Nested-two-levels plain-old specifiers should yield good strings in a TestFailed report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case InfoProvided(ordinal, message, nameInfo, throwable, formatter, payload, threadName, timeStamp) =>
            // infoProvided should be invoked before the other method
            assert(!theOtherMethodHasBeenInvoked)
            infoProvidedHasBeenInvoked = true
            if (message.indexOf("My Spec") != -1)
              infoReportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "My Spec")
                  infoReportHadCorrectSpecText = true
                if (formattedText == "My Spec")
                  infoReportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case event: TestFailed =>
            // infoProvided should be invoked before the this method
            assert(infoProvidedHasBeenInvoked)
            theOtherMethodHasBeenInvoked = true
            if (event.testName.indexOf("My Spec must start with proper words") != -1)
              reportHadCorrectTestName = true
            event.formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "must start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- must start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
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
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
    assert(infoReportHadCorrectTestName)
    assert(infoReportHadCorrectSpecText)
    assert(infoReportHadCorrectFormattedSpecText)
  }

  // Test for good strings in report for top-level shared behavior examples
  test("Top-level 'shared behavior - fancy specifiers' should yield good strings in a TestSucceeded report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            if (testName.indexOf("it should start with proper words") != -1)
              reportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "it should start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- it should start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case _ =>
        }
      }
    }
    class MySpec extends Spec with SharedTests {
      def myBehavior(i: Int) {
        it("it should start with proper words") {}
      }
      ensure (1) behaves like (myBehavior)
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }
  
  test("Top-level 'shared behavior - plain-old specifiers' should yield good strings in a TestSucceeded report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            if (testName.indexOf("must start with proper words") != -1)
              reportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "must start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- must start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case _ =>
        }
      }
    }
    class MySpec extends Spec with SharedTests {
      def myBehavior(i: Int) {
        it("must start with proper words") {}
      }
      ensure (1) behaves like (myBehavior)
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  test("Top-level 'shared behavior - plain-old specifiers' should yield good strings in a testSucceeded report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            if (testName.indexOf("must start with proper words") != -1)
              reportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "must start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- must start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case _ =>
        }
      }
    }
    class MySpec extends Spec with SharedTests {
      def myBehavior(i: Int) {
        it("must start with proper words") {}
      }
      ensure (1) behaves like (myBehavior)
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  test("Top-level 'shared behavior - plain-old specifiers' should yield good strings in a TestFailed report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case event: TestFailed =>
            if (event.testName.indexOf("must start with proper words") != -1)
              reportHadCorrectTestName = true
            event.formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "must start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- must start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case _ =>
        }
      }
    }
    class MySpec extends Spec with SharedTests {
      def myBehavior(i: Int) {
        it("must start with proper words") { fail() }
      }
      ensure (1) behaves like (myBehavior)
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  // Tests for good strings in report for shared-behavior, nested-one-level specifiers
  test("Nested-one-level 'shared behavior' should yield good strings in a TestSucceeded report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case InfoProvided(ordinal, message, nameInfo, throwable, formatter, payload, threadName, timeStamp) =>
            // infoProvided should be invoked before the other method
            assert(!theOtherMethodHasBeenInvoked)
            infoProvidedHasBeenInvoked = true
            if (message.indexOf("My Spec") != -1)
              infoReportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "My Spec")
                  infoReportHadCorrectSpecText = true
                if (formattedText == "My Spec")
                  infoReportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            // infoProvided should be invoked before the this method
            assert(infoProvidedHasBeenInvoked)
            theOtherMethodHasBeenInvoked = true
            if (testName.indexOf("My Spec should start with proper words") != -1)
              reportHadCorrectTestName = true
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "should start with proper words")
                  reportHadCorrectSpecText = true
                if (formattedText == "- should start with proper words")
                  reportHadCorrectFormattedSpecText = true
              case _ =>
            }
          case _ =>
        }
      }
    }
    class MySpec extends Spec with SharedTests {
      def myBehavior(i: Int) {
        it("should start with proper words") {}
      }
      describe("My Spec") {
        ensure (1) behaves like (myBehavior)
      }
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
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
    a.run()
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
    a.run(Some("it should be invoked"), StubReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(correctTestWasInvoked)
    assert(!wrongTestWasInvoked)
  }
  
  test("Goodies should make it through to runTest") {
    var foundMyGoodie = false
    class MySpec extends Spec with ShouldMatchers {
      override def runTest(testName: String, reporter: Reporter, stopper: Stopper, goodies: Map[String, Any], tracker: Tracker) {
        foundMyGoodie = goodies.contains("my goodie")
        super.runTest(testName, reporter, stopper, goodies, tracker)
      }
      it("it should find my goodie") {}
    }
    val a = new MySpec
    a.run(None, StubReporter, new Stopper {}, Filter(), Map("my goodie" -> "hi"), None, new Tracker)
    assert(foundMyGoodie)  
  }
  
  // I think delete this one. Repeat.
  test("In a TestSucceeded report, the example name should start with '<description> should' if nested two levels inside describe clauses") {
    var testSucceededReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            if (testName.indexOf("A Stack (when working right) should push and pop properly") != -1) {
              testSucceededReportHadCorrectTestName = true
            }  
          case _ =>
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
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testSucceededReportHadCorrectTestName)
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
    assert(a.expectedTestCount(Filter()) === 5)
  }

  // Testing strings sent in reports
  test("In a TestSucceeded report, the example name should be verbatim if top level if example registered with it") {
    var testSucceededReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            if (testName.indexOf("this thing must start with proper words") != -1) {
              testSucceededReportHadCorrectTestName = true
            }  
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("this thing must start with proper words") {}
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testSucceededReportHadCorrectTestName)
  }

  test("In a testSucceeded report, the example name should be verbatim if top level if example registered with it") {
    var testSucceededReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            if (testName.indexOf("this thing must start with proper words") != -1) {
              testSucceededReportHadCorrectTestName = true
            }  
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("this thing must start with proper words") {}
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testSucceededReportHadCorrectTestName)
  }

  test("In a TestFailed report, the example name should be verbatim if top level if example registered with it") {
    var testFailedReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case event: TestFailed =>
            if (event.testName.indexOf("this thing must start with proper words") != -1)
              testFailedReportHadCorrectTestName = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("this thing must start with proper words") { fail() }
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testFailedReportHadCorrectTestName)
  }
  
  test("In a TestStarting report, the example name should start with '<description> ' if nested one level " +
        "inside a describe clause and registered with it") {
    var testSucceededReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case TestStarting(_, _, _, testName, _, _, _, _, _) =>
            if (testName == "A Stack needs to push and pop properly") {
              testSucceededReportHadCorrectTestName = true
            }
          case _ => 
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      describe("A Stack") {
        it("needs to push and pop properly") {}
      }
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testSucceededReportHadCorrectTestName)
  }
    
  test("Specs should send defined formatters") {
    class MyReporter extends Reporter {

      var gotAnUndefinedFormatter = false
      var lastEventWithUndefinedFormatter: Option[Event] = None

      private def ensureFormatterIsDefined(event: Event) {
        if (!event.formatter.isDefined) {
          gotAnUndefinedFormatter = true
          lastEventWithUndefinedFormatter = Some(event)
        }
      }

      def apply(event: Event) {
        event match {
          case event: RunAborted => ensureFormatterIsDefined(event)
          case event: SuiteAborted => ensureFormatterIsDefined(event)
          case event: SuiteStarting => ensureFormatterIsDefined(event)
          case event: SuiteCompleted => ensureFormatterIsDefined(event)
          case event: TestStarting => ensureFormatterIsDefined(event)
          case event: TestSucceeded => ensureFormatterIsDefined(event)
          case event: TestIgnored => ensureFormatterIsDefined(event)
          case event: TestFailed => ensureFormatterIsDefined(event)
          case event: InfoProvided => ensureFormatterIsDefined(event)
          case _ =>
        }
      }
    }

    class MySpec extends Spec with ShouldMatchers {
      it("it should send defined formatters") {
        assert(true)
      }
      it("it should also send defined formatters") {
        assert(false)
      }
    }
    val a = new MySpec
    val myRep = new MyReporter
    a.run(None, myRep, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(!myRep.gotAnUndefinedFormatter, myRep.lastEventWithUndefinedFormatter.toString)
  }

  test("SpecText should come through correctly in a SpecReport when registering with it") {
    var testSucceededReportHadCorrectSpecText = false
    var lastSpecText: Option[String] = None
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "My spec text must have the proper words")
                  testSucceededReportHadCorrectSpecText = true
                else
                  lastSpecText = Some(rawText)
              case _ => throw new RuntimeException("Got a non-SpecReport")
            }
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      it("My spec text must have the proper words") {}
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testSucceededReportHadCorrectSpecText, lastSpecText match { case Some(s) => s; case None => "No report"})
  }

  test("Spec text should come through correctly in a SpecReport when registering with it when nested in one describe") {
    var testSucceededReportHadCorrectSpecText = false
    var lastSpecText: Option[String] = None
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "My short name must have the proper words")
                  testSucceededReportHadCorrectSpecText = true
                else
                  lastSpecText = Some(rawText)
              case _ => throw new RuntimeException("Got a non-SpecReport")
            }
          case _ =>
        }
      }
    }
    class MySpec extends Spec with ShouldMatchers {
      describe("A Stack") {
        it("My short name must have the proper words") {}
      }
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testSucceededReportHadCorrectSpecText, lastSpecText match { case Some(s) => s; case None => "No report"})
  }

  test("Spec text should come through correctly in a SpecReport when registering with it when nested in two describes") {
    var testSucceededReportHadCorrectSpecText = false
    var lastSpecText: Option[String] = None
    class MyReporter extends Reporter {
      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                if (rawText == "My short name must have the proper words")
                  testSucceededReportHadCorrectSpecText = true
                else
                  lastSpecText = Some(rawText)
              case _ => throw new RuntimeException("Got a non-SpecReport")
            }
          case _ =>
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
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testSucceededReportHadCorrectSpecText, lastSpecText match { case Some(s) => s; case None => "No report"})
  }

  test("Should get infoProvided with description if one and only one describe clause") {

    val expectedSpecText = "A Stack"

    class MyReporter extends Reporter {
      var infoProvidedCalled = false
      var expectedMessageReceived = false
      def apply(event: Event) {
        event match {
          case event: InfoProvided =>
            event.formatter match {
              case Some(IndentedText(formattedText, rawText, indentationLevel)) =>
                infoProvidedCalled = true
                if (!expectedMessageReceived) {
                  expectedMessageReceived = (rawText == expectedSpecText)
                }
              case _ =>
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
    a.run(None, myRep, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(myRep.infoProvidedCalled)
    assert(myRep.expectedMessageReceived)
  }
 
  // Testing Shared behaviors
  test("a shared specifier invoked with 'should behave like a' should get invoked") {
    class MySpec extends Spec with SharedTests with BeforeAndAfter {
      var sharedExampleInvoked = false
      def invocationVerifier(i: Int) {
        it("should be invoked") {
          sharedExampleInvoked = true
        }
      }
      describe("A Stack") {
        describe("(when not empty)") {
          it("should allow me to pop") {}
          ensure (1) behaves like (invocationVerifier)
        }
        describe("(when not full)") {
          it("should allow me to push") {}
        }
      }
    }
    val a = new MySpec
    a.run()
    assert(a.sharedExampleInvoked)
  }
  
  test("two examples in a shared behavior should get invoked") {
    class MySpec extends Spec with SharedTests with BeforeAndAfter {
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
          ensure (1) behaves like (invocationVerifier)
        }
        describe("(when not full)") {
          it("should allow me to push") {}
        }
      }
    }
    val a = new MySpec
    a.run()
    assert(a.sharedExampleInvoked)
    assert(a.sharedExampleAlsoInvoked)
  }

  test("three examples in a shared behavior should be invoked in order") {
    class MySpec extends Spec with SharedTests {
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
      ensure (1) behaves like (invocationVerifier)
    }
    val a = new MySpec
    a.run()
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
      // don't use it: behaves like (an InvocationVerifier())
    }
    val a = new MySpec
    a.run()
    assert(!a.example1WasInvoked)
    assert(!a.example2WasInvokedAfterExample1)
    assert(!a.example3WasInvokedAfterExample2)
  }
  
  // Probably delete
  test("The test name for a shared specifier invoked with 'should behave like a' should be verbatim if top level") {
    var testSucceededReportHadCorrectTestName = false
    class MyReporter extends Reporter {

      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            if (testName.indexOf("it should be invoked") != -1) {
              testSucceededReportHadCorrectTestName = true
            }  
          case _ =>
        }
      }
    }
    class MySpec extends Spec with SharedTests with BeforeAndAfter {
      var sharedExampleInvoked = false
      def invocationVerifier(i: Int) {
        it("it should be invoked") {
          sharedExampleInvoked = true
        }
      }
      ensure (1) behaves like (invocationVerifier)
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testSucceededReportHadCorrectTestName)
  }
  
  ignore("The example name for a shared example invoked with 'it should behave like' should start with '<description> should' if nested one level in a describe clause") {
    var testSucceededReportHadCorrectTestName = false
    class MyReporter extends Reporter {

      def apply(event: Event) {
        event match {
          case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) =>
            if (testName.indexOf("A Stack should pop properly") != -1) {
              testSucceededReportHadCorrectTestName = true
            }  
          case _ =>
        }
      }
    }
    class MySpec extends Spec with SharedTests {
      var sharedExampleInvoked = false
      def invocationVerifier(i: Int) {
        it("should pop properly") {
          sharedExampleInvoked = true
        }
      }
      describe("A Stack") {
        ensure (1) behaves like (invocationVerifier)
      }
    }
    val a = new MySpec
    a.run(None, new MyReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(testSucceededReportHadCorrectTestName)
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
    assert(a.expectedTestCount(Filter()) === 5)
  }

  test("expectedTestCount should include tests in a share that is called") {
    class MySpec extends Spec with SharedTests {
      def misbehavior(i: Int) {
        it("should six") {}
        it("should seven") {}
      }
      it("should one") {}
      it("should two") {}
      describe("behavior") {
        it("should three") {}
        ensure (1) behaves like (misbehavior)
        it("should four") {}
      }
      it("should five") {}
    }
    val a = new MySpec
    assert(a.expectedTestCount(Filter()) === 7)
  }

  test("expectedTestCount should include tests in a share that is called twice") {
    class MySpec extends Spec with SharedTests {
      def misbehavior(i: Int) {
        it("should six") {}
        it("should seven") {}
      }
      it("should one") {}
      it("should two") {}
      describe("behavior") {
        it("should three") {}
        ensure (1) behaves like (misbehavior)
        it("should four") {}
      }
      it("should five") {}
      ensure (1) behaves like (misbehavior)
    }
    val a = new MySpec
    assert(a.expectedTestCount(Filter()) === 9)
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
    assert(mySpec.expectedTestCount(Filter()) === 7)
  }

  // End of Share stuff
  ignore("should be able to send info to the reporter") { // Can't do this yet, no info in Spec yet

    val expectedMessage = "this is the expected message"

    class MyReporter extends Reporter {
      var infoProvidedCalled = false
      var expectedMessageReceived = false

      def apply(event: Event) {
        event match {
          case event: InfoProvided =>
            infoProvidedCalled = true
            if (!expectedMessageReceived) {
              expectedMessageReceived = event.message.indexOf(expectedMessage) != -1
            }
          case _ =>
        }
      }
    }

    class MySpec extends Spec with ShouldMatchers {
      describe("A Stack") {
        describe("(when not empty)") {
          it("should allow me to pop") {
            info(expectedMessage)
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
    a.run(None, myRep, new Stopper {}, Filter(), Map(), None, new Tracker)
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

  test("that a null test tag results in a thrown NPE at construction time") {
    // it
    intercept[NullPointerException] {
      new Spec {
        it("hi", null) {}
      }
    }
    val caught = intercept[NullPointerException] {
      new Spec {
        it("hi", mytags.SlowAsMolasses, null) {}
      }
    }
    assert(caught.getMessage === "a test tag was null")
    intercept[NullPointerException] {
      new Spec {
        it("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) {}
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
        ignore("hi", mytags.SlowAsMolasses, null) {}
      }
    }
    assert(caught2.getMessage === "a test tag was null")
    intercept[NullPointerException] {
      new Spec {
        ignore("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) {}
      }
    }
  }

  test("test durations are included in TestFailed and TestSucceeded events fired from Spec") {

    class MySpec extends Spec {
      it("should succeed") {}
      it("should fail") { fail() }
    }

    val mySpec = new MySpec
    val myReporter = new TestDurationReporter
    mySpec.run(None, myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.testSucceededWasFiredAndHadADuration)
    assert(myReporter.testFailedWasFiredAndHadADuration)
  }

  test("suite durations are included in SuiteCompleted events fired from Spec") {

    class MySpec extends Spec {
      override def nestedSuites = List(new Suite {})
    }

    val mySuite = new MySpec
    val myReporter = new SuiteDurationReporter
    mySuite.run(None, myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.suiteCompletedWasFiredAndHadADuration)
  }

  test("suite durations are included in SuiteAborted events fired from Spec") {

    class SuiteThatAborts extends Suite {
      override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
              goodies: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
        throw new RuntimeException("Aborting for testing purposes")
      }
    }

    class MySpec extends Spec {
      override def nestedSuites = List(new SuiteThatAborts {})
    }

    val mySuite = new MySpec
    val myReporter = new SuiteDurationReporter
    mySuite.run(None, myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.suiteAbortedWasFiredAndHadADuration)
  }

  test("pending in a Spec should cause TestPending to be fired") {

    class MySpec extends Spec {
      it("should be pending") (pending)
    }

    val mySuite = new MySpec
    val myReporter = new PendingReporter
    mySuite.run(None, myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.testPendingWasFired)
  }
}


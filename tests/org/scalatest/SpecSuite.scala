package org.scalatest

class SpecSuite extends FunSuite {

  test("three fancy specifiers should be invoked in order") {
    class MySpec extends Spec {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      "it should get invoked" - {
        example1WasInvoked = true
      }
      "it should also get invoked" - {
        if (example1WasInvoked)
          example2WasInvokedAfterExample1 = true
      }
      "it should also also get invoked" - {
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

  test("three plain-old specifiers should be invoked in order") {
    class MySpec extends Spec {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      specify("it should get invoked") {
        example1WasInvoked = true
      }
      specify("it should also get invoked") {
        if (example1WasInvoked)
          example2WasInvokedAfterExample1 = true
      }
      specify("it should also also get invoked") {
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

  test("three fancy specifiers should be invoked in order when two are surrounded by a plain-old describer") {
    class MySpec extends Spec {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      "it should get invoked" - {
        example1WasInvoked = true
      }
      describe("Stack") {
        "should also get invoked" - {
          if (example1WasInvoked)
            example2WasInvokedAfterExample1 = true
        }
        "should also also get invoked" - {
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

  test("three fancy specifiers should be invoked in order when two are surrounded by an fancy describer") {
    class MySpec extends Spec {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      "it should get invoked" - {
        example1WasInvoked = true
      }
      "A Stack" -- {
        "should also get invoked" - {
          if (example1WasInvoked)
            example2WasInvokedAfterExample1 = true
        }
        "should also also get invoked" - {
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

  test("three plain-old specifiers should be invoked in order when two are surrounded by a plain-old describe") {
    class MySpec extends Spec {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      specify("it should get invoked") {
        example1WasInvoked = true
      }
      describe("Stack") {
        specify("should also get invoked") {
          if (example1WasInvoked)
            example2WasInvokedAfterExample1 = true
        }
        specify("should also also get invoked") {
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
   
  test("two fancy specifiers should show up in order of appearance in testNames") {
    class MySpec extends Spec {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      "it should get invoked" - {
        example1WasInvoked = true
      }
      "it should also get invoked" - {
        if (example1WasInvoked)
          example2WasInvokedAfterExample1 = true
      }
    }
    val a = new MySpec
    a.execute()
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "it should get invoked")
    assert(a.testNames.elements.toList(1) === "it should also get invoked")
  }
 
  test("two plain-old specifiers should show up in order of appearance in testNames") {
    class MySpec extends Spec {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      specify("it should get invoked") {
        example1WasInvoked = true
      }
      specify("it should also get invoked") {
        if (example1WasInvoked)
          example2WasInvokedAfterExample1 = true
      }
    }
    val a = new MySpec
    a.execute()
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "it should get invoked")
    assert(a.testNames.elements.toList(1) === "it should also get invoked")
  }
 
  test("fancy specifier test names should include an enclosing describe string, separated by a space") {
    class MySpec extends Spec {
      describe("A Stack") {
        "should allow me to pop" - {}
        "should allow me to push" - {}
      }
    }
    val a = new MySpec
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "A Stack should allow me to pop")
    assert(a.testNames.elements.toList(1) === "A Stack should allow me to push")
  }

  test("plain-old specifier test names should include an enclosing describe string, separated by a space") {
    class MySpec extends Spec {
      describe("A Stack") {
        specify("must allow me to pop") {}
        specify("must allow me to push") {}
      }
    }
    val a = new MySpec
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "A Stack must allow me to pop")
    assert(a.testNames.elements.toList(1) === "A Stack must allow me to push")
  }

  test("fancy specifier test names should properly nest descriptions in test names") {
    class MySpec extends Spec {
      describe("A Stack") {
        describe("(when not empty)") {
          "should allow me to pop" - {}
        }
        describe("(when not full)") {
          "should allow me to push" - {}
        }
      }
    }
    val a = new MySpec
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "A Stack (when not empty) should allow me to pop")
    assert(a.testNames.elements.toList(1) === "A Stack (when not full) should allow me to push")
  }
  
  test("plain-old test names should properly nest plain-old descriptions in test names") {
    class MySpec extends Spec {
      describe("A Stack") {
        describe("(when not empty)") {
          specify("must allow me to pop") {}
        }
        describe("(when not full)") {
          specify("must allow me to push") {}
        }
      }
    }
    val a = new MySpec
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "A Stack (when not empty) must allow me to pop")
    assert(a.testNames.elements.toList(1) === "A Stack (when not full) must allow me to push")
  }
  
  test("should be able to mix in ImpSuite without any problems") {
    class MySpec extends Spec with ImpSuite {
      describe("A Stack") {
        describe("(when not empty)") {
          "should allow me to pop" - {}
        }
        describe("(when not full)") {
          "should allow me to push" - {}
        }
      }
    }
    val a = new MySpec
    a.execute()
  }
  
  // Test for good strings in report for top-level examples
  test("Top-level fancy specifiers should yield good strings in a testStarting report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        if (report.name.indexOf("it should start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      "it should start with proper words" - {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }
  
  test("Top-level plain-old specifiers should yield good strings in a testStarting report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        if (report.name.indexOf("it must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      specify("it must start with proper words") {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }
  
  test("Top-level fancy specifiers should yield good strings in a testSucceeded report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def testSucceeded(report: Report) {
        if (report.name.indexOf("it should start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      "it should start with proper words" - {}
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
      override def testSucceeded(report: Report) {
        if (report.name.indexOf("it must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      specify("it must start with proper words") {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  test("Top-level fancy specifiers should yield good strings in a testFailed report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def testFailed(report: Report) {
        if (report.name.indexOf("it should start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      "it should start with proper words" - { fail() }
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
      override def testFailed(report: Report) {
        if (report.name.indexOf("it must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      specify("it must start with proper words") { fail() }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  // Tests for good strings in report for nested-one-level examples
  test("Nested-one-level fancy specifiers should yield good strings in a testStarting report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
            if (specReport.specText == "should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      "My Spec" -- {
        "should start with proper words" - {}
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
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
            if (specReport.specText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      describe("My Spec") {
        specify("must start with proper words") {}
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
  
  test("Nested-one-level fancy specifiers should yield good strings in a testSucceeded report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
        if (report.name.indexOf("My Spec should start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      "My Spec" -- {
        "should start with proper words" - {}
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
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
            if (specReport.specText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      describe("My Spec") {
        specify("must start with proper words") {}
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
    
  test("Nested-one-level fancy specifiers should yield good strings in a testFailed report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
        if (report.name.indexOf("My Spec should start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      "My Spec" -- {
        "should start with proper words" - { fail() }
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
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
            if (specReport.specText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      describe("My Spec") {
        specify("must start with proper words") { fail() }
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
  test("Nested-two-levels fancy specifiers should yield good strings in a testStarting report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
            if (specReport.specText == "should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      "My" -- {
        "Spec" -- {
          "should start with proper words" - {}
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
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
            if (specReport.specText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      describe("My") {
        describe("Spec") {
          specify("must start with proper words") {}
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
  
  test("Nested-two-levels fancy specifiers should yield good strings in a testSucceeded report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
        if (report.name.indexOf("My Spec should start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      "My" -- {
        "Spec" -- {
          "should start with proper words" - {}
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
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
            if (specReport.specText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      describe("My") {
        describe("Spec") {
          specify("must start with proper words") {}
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
    
  test("Nested-two-levels fancy specifiers should yield good strings in a testFailed report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
        if (report.name.indexOf("My Spec should start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      "My" -- {
        "Spec" -- {
          "should start with proper words" - { fail() }
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
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
            if (specReport.specText == "must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MySpec extends Spec {
      describe("My") {
        describe("Spec") {
          specify("must start with proper words") { fail() }
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
      override def testStarting(report: Report) {
        if (report.name.indexOf("it should start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MyBehavior extends Behavior {
      "it should start with proper words" - {}
    }
    class MySpec extends Spec {
      should behave like a { new MyBehavior }
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
      override def testStarting(report: Report) {
        if (report.name.indexOf("it must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MyBehavior extends Behavior {
      specify("it must start with proper words") {}
    }
    class MySpec extends Spec {
      should behave like a { new MyBehavior }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }
  
  test("Top-level 'shared behavior - fancy specifiers' should yield good strings in a testSucceeded report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def testSucceeded(report: Report) {
        if (report.name.indexOf("it should start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MyBehavior extends Behavior {
      "it should start with proper words" - {}
    }
    class MySpec extends Spec {
      should behave like a { new MyBehavior }
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
      override def testSucceeded(report: Report) {
        if (report.name.indexOf("it must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MyBehavior extends Behavior {
      specify("it must start with proper words") {}
    }
    class MySpec extends Spec {
      should behave like a { new MyBehavior }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  test("Top-level 'shared behavior - fancy specifiers' should yield good strings in a testFailed report") {
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    class MyReporter extends Reporter {
      override def testFailed(report: Report) {
        if (report.name.indexOf("it should start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MyBehavior extends Behavior {
      "it should start with proper words" - { fail() }
    }
    class MySpec extends Spec {
      should behave like a { new MyBehavior }
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
      override def testFailed(report: Report) {
        if (report.name.indexOf("it must start with proper words") != -1)
          reportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it must start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- it must start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    class MyBehavior extends Behavior {
      specify("it must start with proper words") { fail() }
    }
    class MySpec extends Spec {
      should behave like a { new MyBehavior }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(reportHadCorrectTestName)
    assert(reportHadCorrectSpecText)
    assert(reportHadCorrectFormattedSpecText)
  }

  // Tests for good strings in report for shared-behavior, nested-one-level specifiers
  test("Nested-one-level 'shared behavior - fancy specifiers' should yield good strings in a testStarting report") {
    var infoReportHadCorrectTestName = false
    var infoReportHadCorrectSpecText = false
    var infoReportHadCorrectFormattedSpecText = false
    var reportHadCorrectTestName = false
    var reportHadCorrectSpecText = false
    var reportHadCorrectFormattedSpecText = false
    var infoProvidedHasBeenInvoked = false
    var theOtherMethodHasBeenInvoked = false
    class MyReporter extends Reporter {
      override def infoProvided(report: Report) {
        // infoProvided should be invoked before the other method
        assert(!theOtherMethodHasBeenInvoked)
        infoProvidedHasBeenInvoked = true
        if (report.name.indexOf("My Spec") != -1)
          infoReportHadCorrectTestName = true
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My Spec")
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
            if (specReport.specText == "should start with proper words")
              reportHadCorrectSpecText = true
            if (specReport.formattedSpecText == "- should start with proper words")
              reportHadCorrectFormattedSpecText = true
          case _ =>
        }
      }
    }
    case class MyBehavior extends Behavior {
      "should start with proper words" - {}
    }
    class MySpec extends Spec {
      describe("My Spec") {
        should behave like a MyBehavior()
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
    class MySpec extends Spec {
      describe("this will be empty") {}
    }
    val a = new MySpec
    a.execute()
  }  
  
  test("Only a passed test name should be invoked.") {
    var correctTestWasInvoked = false
    var wrongTestWasInvoked = false
    class MySpec extends Spec {
      "it should be invoked" - {
        correctTestWasInvoked = true
      }
      "it should not be invoked" - {
        wrongTestWasInvoked = true
      }
    }
    val a = new MySpec
    a.execute(Some("it should be invoked"), new Reporter {}, new Stopper {}, Set(), Set(), Map(), None)
    assert(correctTestWasInvoked)
    assert(!wrongTestWasInvoked)
  }
  
  test("Goodies should make it through to runTest") {
    var foundMyGoodie = false
    class MySpec extends Spec {
      override def runTest(testName: String, reporter: Reporter, stopper: Stopper, goodies: Map[String, Any]) {
        foundMyGoodie = goodies.contains("my goodie")
        super.runTest(testName, reporter, stopper, goodies)
      }
      "it should find my goodie" - {}
    }
    val a = new MySpec
    a.execute(None, new Reporter {}, new Stopper {}, Set(), Set(), Map("my goodie" -> "hi"), None)
    assert(foundMyGoodie)  
  }
  
  // The old ones XXXXXX
  test("In a testSucceeded report, the specifier name should start with 'it should' if top level") { // To delete
    var testSucceededReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def testSucceeded(report: Report) {
        if (report.name.indexOf("it should start with proper words") != -1) {
          testSucceededReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec {
      "it should start with proper words" - {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testSucceededReportHadCorrectTestName)
  }
  
  test("In a testFailed report, the specifier name should start with 'it should' if top level") { // To delete
    var testFailedReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def testFailed(report: Report) {
        if (report.name.indexOf("it should start with proper words") != -1) {
          testFailedReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec {
      "it should start with proper words" - { fail() }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedReportHadCorrectTestName)
  }
  
  // To delete. I think this is a repeat.
  test("In a testStarting report, the specifier name should start with '<description> should' if nested one level inside a describe clause") {
    var testStartingReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        if (report.name.indexOf("A Stack should push and pop properly") != -1) {
          testStartingReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec {
      "A Stack" -- {
        "should push and pop properly" - {}
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectTestName)
  }
  
  // I think delete this one. Repeat.
  test("In a testStarting report, the example name should start with '<description> should' if nested two levels inside describe clauses") {
    var testStartingReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        if (report.name.indexOf("A Stack (when working right) should push and pop properly") != -1) {
          testStartingReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec {
      describe("A Stack") {
        describe("(when working right)") {
          "should push and pop properly" - {}
        }
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectTestName)
  }
  
  test("expectedTestCount is the number of fancy specifiers if no shares") {
    class MySpec extends Spec {
      "it should one" - {}
      "it should two" - {}
      describe("behavior") {
        "should three" - {}  
        "should four" - {}
      }
      "should five" - {}
    }
    val a = new MySpec
    assert(a.expectedTestCount(Set(), Set()) === 5)
  }

  test("expectedTestCount is the number of plain-old specifiers if no shares") {
    class MySpec extends Spec {
      specify("must one") {}
      specify("must two") {}
      describe("behavior") {
        specify("must three") {}  
        specify("must four") {}
      }
      specify("must five") {}
    }
    val a = new MySpec
    assert(a.expectedTestCount(Set(), Set()) === 5)
  }

  // Testing strings sent in reports
  test("In a testStarting report, the example name should be verbatim if top level if example registered with specify") {
    var testStartingReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        if (report.name.indexOf("this thing must start with proper words") != -1) {
          testStartingReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec {
      specify("this thing must start with proper words") {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectTestName)
  }

  test("In a testSucceeded report, the example name should be verbatim if top level if example registered with specify") {
    var testSucceededReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def testSucceeded(report: Report) {
        if (report.name.indexOf("this thing must start with proper words") != -1) {
          testSucceededReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec {
      specify("this thing must start with proper words") {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testSucceededReportHadCorrectTestName)
  }

  test("In a testFailed report, the example name should be verbatim if top level if example registered with specify") {
    var testFailedReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def testFailed(report: Report) {
        if (report.name.indexOf("this thing must start with proper words") != -1) {
          testFailedReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec {
      specify("this thing must start with proper words") { fail() }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testFailedReportHadCorrectTestName)
  }
  
  test("In a testStarting report, the example name should start with '<description> ' if nested one level " +
        "inside a describe clause and registered with specify") {
    var testStartingReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        if (report.name.indexOf("A Stack needs to push and pop properly") != -1) {
          testStartingReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec {
      describe("A Stack") {
        specify("needs to push and pop properly") {}
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
    class MySpec extends Spec {
      "it should send SpecReports" - {
        assert(true)
      }
      "it should send SpecReports" - {
        assert(false)
      }
    }
    val a = new MySpec
    val myRep = new MyReporter
    a.execute(None, myRep, new Stopper {}, Set(), Set(), Map(), None)
    assert(!myRep.gotANonSpecReport)
  }
  
  test("SpecText should come through correctly in a SpecReport when registering with fancy specifier") {
    var testStartingReportHadCorrectShortName = false
    var lastShortName: Option[String] = None
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "it should start with proper words")
              testStartingReportHadCorrectShortName = true
            else
              lastShortName = Some(specReport.specText)
          case _ => throw new RuntimeException("Got a non-SpecReport")
        }
      }
    }
    class MySpec extends Spec {
      "it should start with proper words" - {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectShortName, lastShortName match { case Some(s) => s; case None => "No report"})
  }

  test("SpecText should come through correctly in a SpecReport when registering with specify") {
    var testStartingReportHadCorrectSpecText = false
    var lastSpecText: Option[String] = None
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My spec text must have the proper words")
              testStartingReportHadCorrectSpecText = true
            else
              lastSpecText = Some(specReport.specText)
          case _ => throw new RuntimeException("Got a non-SpecReport")
        }
      }
    }
    class MySpec extends Spec {
      specify("My spec text must have the proper words") {}
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectSpecText, lastSpecText match { case Some(s) => s; case None => "No report"})
  }
   
  test("Spec text should come through correctly in a SpecReport when registering with fancy specifier when nested in one describer") {
    var testStartingReportHadCorrectSpecText = false
    var lastSpecText: Option[String] = None
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "should start with proper words")
              testStartingReportHadCorrectSpecText = true
            else
              lastSpecText = Some(specReport.specText)
          case _ => throw new RuntimeException("Got a non-SpecReport")
        }
      }
    }
    class MySpec extends Spec {
      "A Stack" -- {
        "should start with proper words" - {}
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectSpecText, lastSpecText match { case Some(s) => s; case None => "No report"})
  }

  test("Spec text should come through correctly in a SpecReport when registering with specify when nested in one describe") {
    var testStartingReportHadCorrectSpecText = false
    var lastSpecText: Option[String] = None
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My short name must have the proper words")
              testStartingReportHadCorrectSpecText = true
            else
              lastSpecText = Some(specReport.specText)
          case _ => throw new RuntimeException("Got a non-SpecReport")
        }
      }
    }
    class MySpec extends Spec {
      describe("A Stack") {
        specify("My short name must have the proper words") {}
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectSpecText, lastSpecText match { case Some(s) => s; case None => "No report"})
  }
   
  test("Spec text should come through correctly in a SpecReport when registering with fancy specifiers when nested in two describers") {
    var testStartingReportHadCorrectSpecText = false
    var lastSpecText: Option[String] = None
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "should start with proper words")
              testStartingReportHadCorrectSpecText = true
            else
              lastSpecText = Some(specReport.specText)
          case _ => throw new RuntimeException("Got a non-SpecReport")
        }
      }
    }
    class MySpec extends Spec {
      "A Stack" -- {
        "(when empty)" -- {
          "should start with proper words" - {}
        }
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectSpecText, lastSpecText match { case Some(s) => s; case None => "No report"})
  }

  test("Spec text should come through correctly in a SpecReport when registering with specify when nested in two describes") {
    var testStartingReportHadCorrectSpecText = false
    var lastSpecText: Option[String] = None
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        report match {
          case specReport: SpecReport =>
            if (specReport.specText == "My short name must have the proper words")
              testStartingReportHadCorrectSpecText = true
            else
              lastSpecText = Some(specReport.specText)
          case _ => throw new RuntimeException("Got a non-SpecReport")
        }
      }
    }
    class MySpec extends Spec {
      describe("A Stack") {
        describe("(when empty)") {
          specify("My short name must have the proper words") {}
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
      override def infoProvided(report: Report) {
        infoProvidedCalled = true
        if (!expectedMessageReceived) {
          expectedMessageReceived = report.message.indexOf(expectedMessage) != -1
        }
      }
    }

    class MySpec extends Spec {
      describe("A Stack") {
        describe("(when not empty)") {
          specify("might allow me to pop") {
            val report = new Report("myName", expectedMessage)
            // info(report)
            ()
          }
        }
        describe("(when not full)") {
          specify("allow me to push") {}
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
      override def infoProvided(report: Report) {
        report match {
          case specReport: SpecReport => {
            infoProvidedCalled = true
            if (!expectedMessageReceived) {
              expectedMessageReceived = (specReport.specText == expectedSpecText)
            }
          }
          case _ =>
        }
      }
    }

    class MySpec extends Spec {
      describe("A Stack") {
        specify("should allow me to push") {}
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

    class MySpec extends Spec {
      "it should be at level 0" - {}
      "it should also be at level 0" - { fail() }
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

    class MySpec extends Spec {
      describe("my describe clause") {
        "should be at level 1" - {}
        "should also be at level 1" - { fail() }
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
    class MySpec extends Spec with ImpSuite {
      var sharedExampleInvoked = false
      case class InvocationVerifier extends Behavior {
        "should be invoked" - {
          sharedExampleInvoked = true
        }
      }
      describe("A Stack") {
        describe("(when not empty)") {
          "should allow me to pop" - {}
          should behave like an InvocationVerifier()
        }
        describe("(when not full)") {
          "should allow me to push" - {}
        }
      }
    }
    val a = new MySpec
    a.execute()
    assert(a.sharedExampleInvoked)
  }
  
  test("two examples in a shared behavior should get invoked") {
    class MySpec extends Spec with ImpSuite {
      var sharedExampleInvoked = false
      var sharedExampleAlsoInvoked = false
      case class InvocationVerifier extends Behavior {
        "should be invoked" - {
          sharedExampleInvoked = true
        }
        "should also be invoked" - {
          sharedExampleAlsoInvoked = true
        }
      }
      "A Stack" -- {
        "(when not empty)" -- {
          "should allow me to pop" - {}
          should behave like the InvocationVerifier()
        }
        "(when not full)" -- {
          "should allow me to push" - {}
        }
      }
    }
    val a = new MySpec
    a.execute()
    assert(a.sharedExampleInvoked)
    assert(a.sharedExampleAlsoInvoked)
  }

  test("three examples in a shared behavior should be invoked in order") {
    class MySpec extends Spec {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      case class InvocationVerifier extends Behavior {
        "should get invoked" - {
          example1WasInvoked = true
        }
        "should also get invoked" - {
          if (example1WasInvoked)
            example2WasInvokedAfterExample1 = true
        }
        "should also also get invoked" - {
          if (example2WasInvokedAfterExample1)
            example3WasInvokedAfterExample2 = true
        }
      }
      should behave like an InvocationVerifier()
    }
    val a = new MySpec
    a.execute()
    assert(a.example1WasInvoked)
    assert(a.example2WasInvokedAfterExample1)
    assert(a.example3WasInvokedAfterExample2)
  }
  
  test("three examples in a shared behavior should not get invoked at all if the behavior isn't used in a like clause") {
    class MySpec extends Spec {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      case class InvocationVerifier extends Behavior {
        "should get invoked" - {
          example1WasInvoked = true
        }
        "should also get invoked" - {
          if (example1WasInvoked)
            example2WasInvokedAfterExample1 = true
        }
        "should also also get invoked" - {
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
      override def testStarting(report: Report) {
        if (report.name.indexOf("it should be invoked") != -1) {
          testStartingReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec with ImpSuite {
      var sharedExampleInvoked = false
      case class InvocationVerifier extends Behavior {
        "it should be invoked" - {
          sharedExampleInvoked = true
        }
      }
      should behave like the InvocationVerifier()
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectTestName)
  }
  
  ignore("The example name for a shared example invoked with 'it should behave like' should start with '<description> should' if nested one level in a describe clause") {
    var testStartingReportHadCorrectTestName = false
    class MyReporter extends Reporter {
      override def testStarting(report: Report) {
        if (report.name.indexOf("A Stack should pop properly") != -1) {
          testStartingReportHadCorrectTestName = true
        }  
      }
    }
    class MySpec extends Spec {
      var sharedExampleInvoked = false
      case class InvocationVerifier extends Behavior {
        "should pop properly" - {
          sharedExampleInvoked = true
        }
      }
      "A Stack" -- {
        should behave like an InvocationVerifier()
      }
    }
    val a = new MySpec
    a.execute(None, new MyReporter, new Stopper {}, Set(), Set(), Map(), None)
    assert(testStartingReportHadCorrectTestName)
  }
 
  test("expectedTestCount should not include tests in shares if never called") {
    class MySpec extends Spec {
      class Misbehavior extends Spec {
        "should six" - {}
        "should seven" - {}
      }
      "should one" - {}
      "should two" - {}
      "behavior" -- {
        "should three" - {}  
        "should four" - {}
      }
      "should five" - {}
    }
    val a = new MySpec
    assert(a.expectedTestCount(Set(), Set()) === 5)
  }

  test("expectedTestCount should include tests in a share that is called") {
    class MySpec extends Spec {
      case class Misbehavior extends Behavior {
        "should six" - {}
        "should seven" - {}
      }
      "should one" - {}
      "should two" - {}
      describe("behavior") {
        "should three" - {} 
        should behave like a Misbehavior()
        "should four" - {}
      }
      "should five" - {}
    }
    val a = new MySpec
    assert(a.expectedTestCount(Set(), Set()) === 7)
  }

  test("expectedTestCount should include tests in a share that is called twice") {
    class MySpec extends Spec {
      case class Misbehavior extends Behavior {
        "should six" - {}
        "should seven" - {}
      }
      "should one" - {}
      "should two" - {}
      describe("behavior") {
        "should three" - {} 
        should behave like a Misbehavior()
        "should four" - {}
      }
      "should five" - {}
      should behave like a Misbehavior()
    }
    val a = new MySpec
    assert(a.expectedTestCount(Set(), Set()) === 9)
  }

  // End of Share stuff
  ignore("A given reporter clause should be able to send info to the reporter") {

    val expectedMessage = "this is the expected message"

    class MyReporter extends Reporter {
      var infoProvidedCalled = false
      var expectedMessageReceived = false
      var lastReport: Report = null
      override def infoProvided(report: Report) {
        infoProvidedCalled = true
        if (!expectedMessageReceived) {
          expectedMessageReceived = report.message.indexOf(expectedMessage) != -1
        }
      }
    }

    class MySpec extends Spec {
      describe("A Stack") {
        describe("(when not empty)") {
          "should allow me to pop" - {
            val report = new Report("myName", expectedMessage)
            // info(report)
            ()
          }
        }
        describe("(when not full)") {
          "should allow me to push" - {}
        }
      }
    }
    val a = new MySpec
    val myRep = new MyReporter
    a.execute(None, myRep, new Stopper {}, Set(), Set(), Map(), None)
    assert(myRep.infoProvidedCalled)
    assert(myRep.expectedMessageReceived)
  }
}


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
package org.scalatest.path

import org.scalatest._

import org.scalatest.path.{ FunSpec => PathFunSpec }
import org.scalatest.events._

class FunSpecSpec extends org.scalatest.FreeSpec with SharedHelpers with GivenWhenThen {

  "A path.FunSpec" - {

    "should return the test names in registration order from testNames" in {

      class AFunSpec extends PathFunSpec {
        it("should test this") {}
        it("should test that") {}
        override def newInstance = new AFunSpec
      }
      val a = new AFunSpec

      expect(List("should test this", "should test that")) {
        a.testNames.iterator.toList
      }

      val b = new PathFunSpec {}

      expect(List[String]()) {
        b.testNames.iterator.toList
      }

      class CFunSpec extends PathFunSpec {
        it("should test that") {}
        it("should test this") {}
        override def newInstance = new CFunSpec
      }
      val c = new CFunSpec

      expect(List("should test that", "should test this")) {
        c.testNames.iterator.toList
      }

      class DFunSpec extends PathFunSpec {
        describe("A Tester") {
          it("should test that") {}
          it("should test this") {}
        }
        override def newInstance = new DFunSpec
      }
      val d = new DFunSpec

      expect(List("A Tester should test that", "A Tester should test this")) {
        d.testNames.iterator.toList
      }

      class EFunSpec extends PathFunSpec {
        describe("A Tester") {
          it("should test this") {}
          it("should test that") {}
        }
        override def newInstance = new EFunSpec
      }
      val e = new EFunSpec

      expect(List("A Tester should test this", "A Tester should test that")) {
        e.testNames.iterator.toList
      }
    }

    "should throw DuplicateTestNameException if a duplicate test name registration is attempted" in {
      
      intercept[DuplicateTestNameException] {
        class AFunSpec extends PathFunSpec {
          it("should test this") {}
          it("should test this") {}
          override def newInstance = new AFunSpec
        }
        (new AFunSpec).tags // Must call a method to get it to attempt to register the second test
      }
      intercept[DuplicateTestNameException] {
        class AFunSpec extends PathFunSpec {
          it("should test this") {}
          ignore("should test this") {}
          override def newInstance = new AFunSpec
        }
        (new AFunSpec).tags
      }
      intercept[DuplicateTestNameException] {
        class AFunSpec extends PathFunSpec {
          ignore("should test this") {}
          ignore("should test this") {}
          override def newInstance = new AFunSpec
        }
        (new AFunSpec).tags
      }
      intercept[DuplicateTestNameException] {
        class AFunSpec extends PathFunSpec {
          ignore("should test this") {}
          it("should test this") {}
          override def newInstance = new AFunSpec
        }
        (new AFunSpec).tags
      }
    }

    "(with info calls)" - {
      class InfoInsideTestSpec extends PathFunSpec {
        val msg = "hi there, dude"
        val testName = "test name"
        it(testName) {
          info(msg)
        }
        override def newInstance = new InfoInsideTestSpec
      }
      // In a Spec, any InfoProvided's fired during the test should be cached and sent out after the test has
      // suceeded or failed. This makes the report look nicer, because the info is tucked under the "specifier'
      // text for that test.
      "should, when the info appears in the code of a successful test, report the info after the TestSucceeded" in {
        val spec = new InfoInsideTestSpec
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(spec, spec.testName, spec.msg)
        assert(testSucceededIndex < infoProvidedIndex)
      }
      class InfoBeforeTestSpec extends PathFunSpec {
        val msg = "hi there, dude"
        val testName = "test name"
        info(msg)
        it(testName) {}
      }
      "should, when the info appears in the body before a test, report the info before the test" in {
        val spec = new InfoBeforeTestSpec
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(spec, spec.testName, spec.msg)
        assert(infoProvidedIndex < testStartingIndex)
        assert(testStartingIndex < testSucceededIndex)
      }
      "should, when the info appears in the body after a test, report the info after the test runs" in {
        val msg = "hi there, dude"
        val testName = "test name"
        class MySpec extends PathFunSpec {
          it(testName) {}
          info(msg)
        }
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(new MySpec, testName, msg)
        assert(testStartingIndex < testSucceededIndex)
        assert(testSucceededIndex < infoProvidedIndex)
      }
      "should throw an IllegalStateException when info is called by a method invoked after the suite has been executed" in {
        class MySpec extends PathFunSpec {
          callInfo() // This should work fine
          def callInfo() {
            info("howdy")
          }
          it("howdy also") {
            callInfo() // This should work fine
          }
          override def newInstance = new MySpec
        }
        val spec = new MySpec
        val myRep = new EventRecordingReporter
        spec.run(None, myRep, new Stopper {}, Filter(), Map(), None, new Tracker)
        intercept[IllegalStateException] {
          spec.callInfo()
        }
      }
      "should send an InfoProvided with an IndentedText formatter with level 1 when called outside a test" in {
        val spec = new InfoBeforeTestSpec
        val indentedText = getIndentedTextFromInfoProvided(spec)
        assert(indentedText === IndentedText("+ " + spec.msg, spec.msg, 0))
      }
      "should send an InfoProvided with an IndentedText formatter with level 2 when called within a test" in {
        val spec = new InfoInsideTestSpec
        val indentedText = getIndentedTextFromInfoProvided(spec)
        assert(indentedText === IndentedText("  + " + spec.msg, spec.msg, 1))
      }
    }

    "(when a nesting rule has been violated)" - {

      "should, if they call a describe from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends PathFunSpec {
          it("should blow up") {
            describe("in the wrong place, at the wrong time") {
            }
          }
          override def newInstance = new MySpec
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      "should, if they call a describe with a nested it from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends PathFunSpec {
          it("should blow up") {
            describe("in the wrong place, at the wrong time") {
              it("should never run") {
                assert(1 === 1)
              }
            }
          }
          override def newInstance = new MySpec
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      "should, if they call a nested it from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends PathFunSpec {
          it("should blow up") {
            it("should never run") {
              assert(1 === 1)
            }
          }
          override def newInstance = new MySpec
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      "should, if they call a nested it with tags from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends PathFunSpec {
          it("should blow up") {
            it("should never run", mytags.SlowAsMolasses) {
              assert(1 === 1)
            }
          }
          override def newInstance = new MySpec
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      "should, if they call a describe with a nested ignore from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends PathFunSpec {
          it("should blow up") {
            describe("in the wrong place, at the wrong time") {
              ignore("should never run") {
                assert(1 === 1)
              }
            }
          }
          override def newInstance = new MySpec
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      "should, if they call a nested ignore from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends PathFunSpec {
          it("should blow up") {
            ignore("should never run") {
              assert(1 === 1)
            }
          }
          override def newInstance = new MySpec
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      "should, if they call a nested ignore with tags from within an it clause, result in a TestFailedException when running the test" in {

        class MySpec extends PathFunSpec {
          it("should blow up") {
            ignore("should never run", mytags.SlowAsMolasses) {
              assert(1 === 1)
            }
          }
          override def newInstance = new MySpec
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
    }

    "should run tests registered via the 'it should behave like' syntax" in {
      trait SharedSpecTests { this: PathFunSpec =>
        def nonEmptyStack(s: String)(i: Int) {
          it("should be that I am shared") {}
        }
      }
      class MySpec extends PathFunSpec with SharedSpecTests {
        it should behave like nonEmptyStack("hi")(1)
        override def newInstance = new MySpec
      }
      val suite = new MySpec
      val reporter = new EventRecordingReporter
      suite.run(None, reporter, new Stopper {}, Filter(), Map(), None, new Tracker)

      val indexedList = reporter.eventsReceived

      val testStartingOption = indexedList.find(_.isInstanceOf[TestStarting])
      assert(testStartingOption.isDefined)
      assert(testStartingOption.get.asInstanceOf[TestStarting].testName === "should be that I am shared")
    }

    "should throw NullPointerException if a null test tag is provided" in {
      // it
      intercept[NullPointerException] {
        new PathFunSpec {
          it("hi", null) {}
        }
      }
      val caught = intercept[NullPointerException] {
        new PathFunSpec {
          it("hi", mytags.SlowAsMolasses, null) {}
        }
      }
      assert(caught.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new PathFunSpec {
          it("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) {}
        }
      }
      // ignore
      intercept[NullPointerException] {
        new PathFunSpec {
          ignore("hi", null) {}
        }
      }
      val caught2 = intercept[NullPointerException] {
        new PathFunSpec {
          ignore("hi", mytags.SlowAsMolasses, null) {}
        }
      }
      assert(caught2.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new PathFunSpec {
          ignore("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) {}
        }
      }
    }
    case class TestWasCalledCounts(var theTestThisCalled: Boolean, var theTestThatCalled: Boolean)
    
    class TestWasCalledSuite(val counts: TestWasCalledCounts) extends PathFunSpec {
      def this() { this(TestWasCalledCounts(false, false)) }
      it("should run this") { counts.theTestThisCalled = true }
      it("should run that, maybe") { counts.theTestThatCalled = true }
      override def newInstance = new TestWasCalledSuite(counts)
    }

    "should execute all tests when run is called with testName None" in {

      val b = new TestWasCalledSuite
      b.run(None, SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(b.counts.theTestThisCalled)
      assert(b.counts.theTestThatCalled)
    }

    "should execute one test when run is called with a defined testName" in {

      val a = new TestWasCalledSuite
      val rep = new EventRecordingReporter
      a.run(Some("should run this"), rep, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(a.counts.theTestThisCalled)
      assert(a.counts.theTestThatCalled) // In a path trait, this gets executed, but not reported
      val tse = rep.testSucceededEventsReceived
      assert(tse.size == 1)
      assert(tse(0).testName === "should run this") 
      val tfe = rep.testFailedEventsReceived
      assert(tfe.size === 0)
      val tste = rep.testStartingEventsReceived
      assert(tste.size === 1)
    }

    "should report as ignored, and not run, tests marked ignored" in {

      class AFunSpec(val counts: TestWasCalledCounts) extends PathFunSpec {
        it("test this") { counts.theTestThisCalled = true }
        it("test that") { counts.theTestThatCalled = true }
        override def newInstance = new AFunSpec(counts)
      }
      val a = new AFunSpec(TestWasCalledCounts(false, false))

      val repA = new TestIgnoredTrackingReporter
      a.run(None, repA, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(!repA.testIgnoredReceived)
      assert(a.counts.theTestThisCalled)
      assert(a.counts.theTestThatCalled)

      class BFunSpec(val counts: TestWasCalledCounts) extends PathFunSpec {
        ignore("test this") { counts.theTestThisCalled = true }
        it("test that") { counts.theTestThatCalled = true }
        override def newInstance = new BFunSpec(counts)
      }
      val b = new BFunSpec(TestWasCalledCounts(false, false))

      val repB = new TestIgnoredTrackingReporter
      b.run(None, repB, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repB.testIgnoredReceived)
      assert(repB.lastEvent.isDefined)
      assert(repB.lastEvent.get.testName endsWith "test this")
      assert(!b.counts.theTestThisCalled)
      assert(b.counts.theTestThatCalled)

      class CFunSpec(val counts: TestWasCalledCounts) extends PathFunSpec {
        it("test this") { counts.theTestThisCalled = true }
        ignore("test that") { counts.theTestThatCalled = true }
        override def newInstance = new CFunSpec(counts)
      }
      val c = new CFunSpec(TestWasCalledCounts(false, false))

      val repC = new TestIgnoredTrackingReporter
      c.run(None, repC, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repC.testIgnoredReceived)
      assert(repC.lastEvent.isDefined)
      assert(repC.lastEvent.get.testName endsWith "test that", repC.lastEvent.get.testName)
      assert(c.counts.theTestThisCalled)
      assert(!c.counts.theTestThatCalled)

      // The order I want is order of appearance in the file.
      // Will try and implement that tomorrow. Subtypes will be able to change the order.
      class DFunSpec(val counts: TestWasCalledCounts) extends PathFunSpec {
        ignore("test this") { counts.theTestThisCalled = true }
        ignore("test that") { counts.theTestThatCalled = true }
        override def newInstance = new DFunSpec(counts)
      }
      val d = new DFunSpec(TestWasCalledCounts(false, false))

      val repD = new TestIgnoredTrackingReporter
      d.run(None, repD, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repD.testIgnoredReceived)
      assert(repD.lastEvent.isDefined)
      assert(repD.lastEvent.get.testName endsWith "test that") // last because should be in order of appearance
      assert(!d.counts.theTestThisCalled)
      assert(!d.counts.theTestThatCalled)
    }

    "should ignore a test marked as ignored if run is invoked with that testName" in {
      // If I provide a specific testName to run, then it should ignore an Ignore on that test
      // method and actually invoke it.
      class EFunSpec(val counts: TestWasCalledCounts) extends PathFunSpec {
        def this() { this(TestWasCalledCounts(false, false)) }
        ignore("test this") { counts.theTestThisCalled = true }
        it("test that") { counts.theTestThatCalled = true }
        override def newInstance = new EFunSpec(counts)
      }
      val e = new EFunSpec

      val repE = new EventRecordingReporter
      e.run(Some("test this"), repE, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repE.testIgnoredEventsReceived.size === 1)
      assert(!e.counts.theTestThisCalled)
      assert(e.counts.theTestThatCalled)  // In a path trait, tests other than the Some(testName) get executed, but not reported
      val tste = repE.testStartingEventsReceived
      assert(tste.size === 0)
    }

    "should run only those tests selected by the tags to include and exclude sets" in {

      // Nothing is excluded
      class AFunSpec(val counts: TestWasCalledCounts) extends PathFunSpec {
        it("test this", mytags.SlowAsMolasses) { counts.theTestThisCalled = true }
        it("test that") { counts.theTestThatCalled = true }
        override def newInstance = new AFunSpec(counts)
      }
      val a = new AFunSpec(TestWasCalledCounts(false, false))
      val repA = new TestIgnoredTrackingReporter
      a.run(None, repA, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(!repA.testIgnoredReceived)
      assert(a.counts.theTestThisCalled)
      assert(a.counts.theTestThatCalled)

      // SlowAsMolasses is included, one test should be excluded
      class BFunSpec(val counts: TestWasCalledCounts) extends PathFunSpec {
        it("test this", mytags.SlowAsMolasses) { counts.theTestThisCalled = true }
        it("test that") { counts.theTestThatCalled = true }
        override def newInstance = new BFunSpec(counts)
      }
      val b = new BFunSpec(TestWasCalledCounts(false, false))
      val repB = new EventRecordingReporter
      b.run(None, repB, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), Map(), None, new Tracker)
      assert(repB.testIgnoredEventsReceived.isEmpty)
      assert(b.counts.theTestThisCalled)
      assert(b.counts.theTestThatCalled)
      assert(repB.testStartingEventsReceived.size === 1)
      assert(repB.testStartingEventsReceived(0).testName == "test this")

      // SlowAsMolasses is included, and both tests should be included
      class CFunSpec(val counts: TestWasCalledCounts) extends PathFunSpec {
        it("test this", mytags.SlowAsMolasses) { counts.theTestThisCalled = true }
        it("test that", mytags.SlowAsMolasses) { counts.theTestThatCalled = true }
        override def newInstance = new CFunSpec(counts)
      }
      val c = new CFunSpec(TestWasCalledCounts(false, false))
      val repC = new EventRecordingReporter
      c.run(None, repC, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), Map(), None, new Tracker)
      assert(repC.testIgnoredEventsReceived.isEmpty)
      assert(c.counts.theTestThisCalled)
      assert(c.counts.theTestThatCalled)
      assert(repC.testStartingEventsReceived.size === 2)

      // SlowAsMolasses is included. both tests should be included but one ignored
      class DFunSpec(val counts: TestWasCalledCounts) extends PathFunSpec {
        ignore("test this", mytags.SlowAsMolasses) { counts.theTestThisCalled = true }
        it("test that", mytags.SlowAsMolasses) { counts.theTestThatCalled = true }
        override def newInstance = new DFunSpec(counts)
      }
      val d = new DFunSpec(TestWasCalledCounts(false, false))
      val repD = new EventRecordingReporter
      d.run(None, repD, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.Ignore")), Map(), None, new Tracker)
      assert(repD.testIgnoredEventsReceived.size === 1)
      assert(!d.counts.theTestThisCalled)
      assert(d.counts.theTestThatCalled)
      assert(repD.testStartingEventsReceived.size === 1)
      assert(repD.testStartingEventsReceived(0).testName === "test that")

      case class ThreeCounts(var theTestThisCalled: Boolean, var theTestThatCalled: Boolean, var theTestTheOtherCalled: Boolean)
      // SlowAsMolasses included, FastAsLight excluded
      class EFunSpec(val counts: ThreeCounts) extends PathFunSpec {
        it("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { counts.theTestThisCalled = true }
        it("test that", mytags.SlowAsMolasses) { counts.theTestThatCalled = true }
        it("test the other") { counts.theTestTheOtherCalled = true }
        override def newInstance = new EFunSpec(counts)
      }
      val e = new EFunSpec(ThreeCounts(false, false, false))
      val repE = new EventRecordingReporter
      e.run(None, repE, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
                Map(), None, new Tracker)
      assert(repE.testIgnoredEventsReceived.isEmpty)
      assert(e.counts.theTestThisCalled)
      assert(e.counts.theTestThatCalled)
      assert(e.counts.theTestTheOtherCalled)
      assert(repE.testStartingEventsReceived.size === 1)
      assert(repE.testStartingEventsReceived(0).testName === "test that")

      // An Ignored test that was both included and excluded should not generate a TestIgnored event
      class FFunSpec(val counts: ThreeCounts) extends PathFunSpec {
        ignore("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { counts.theTestThisCalled = true }
        it("test that", mytags.SlowAsMolasses) { counts.theTestThatCalled = true }
        it("test the other") { counts.theTestTheOtherCalled = true }
        override def newInstance = new FFunSpec(counts)
      }
      val f = new FFunSpec(ThreeCounts(false, false, false))
      val repF = new EventRecordingReporter
      f.run(None, repF, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
                Map(), None, new Tracker)
      assert(repF.testIgnoredEventsReceived.isEmpty)
      assert(!f.counts.theTestThisCalled)
      assert(f.counts.theTestThatCalled)
      assert(f.counts.theTestTheOtherCalled)
      assert(repE.testStartingEventsReceived.size === 1)
      assert(repE.testStartingEventsReceived(0).testName === "test that")

      // An Ignored test that was not included should not generate a TestIgnored event
      class GFunSpec(val counts: ThreeCounts) extends PathFunSpec {
        it("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { counts.theTestThisCalled = true }
        it("test that", mytags.SlowAsMolasses) { counts.theTestThatCalled = true }
        ignore("test the other") { counts.theTestTheOtherCalled = true }
        override def newInstance = new GFunSpec(counts)
      }
      val g = new GFunSpec(ThreeCounts(false, false, false))
      val repG = new EventRecordingReporter
      g.run(None, repG, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
                Map(), None, new Tracker)
      assert(repG.testIgnoredEventsReceived.isEmpty)
      assert(g.counts.theTestThisCalled)
      assert(g.counts.theTestThatCalled)
      assert(!g.counts.theTestTheOtherCalled)
      assert(repG.testStartingEventsReceived.size === 1)
      assert(repG.testStartingEventsReceived(0).testName === "test that")

      // No tagsToInclude set, FastAsLight excluded
      class HFunSpec(val counts: ThreeCounts) extends PathFunSpec {
        it("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { counts.theTestThisCalled = true }
        it("test that", mytags.SlowAsMolasses) { counts.theTestThatCalled = true }
        it("test the other") { counts.theTestTheOtherCalled = true }
        override def newInstance = new HFunSpec(counts)
      }
      val h = new HFunSpec(ThreeCounts(false, false, false))
      val repH = new EventRecordingReporter
      h.run(None, repH, new Stopper {}, Filter(None, Set("org.scalatest.FastAsLight")), Map(), None, new Tracker)
      assert(repH.testIgnoredEventsReceived.isEmpty)
      assert(h.counts.theTestThisCalled)
      assert(h.counts.theTestThatCalled)
      assert(h.counts.theTestTheOtherCalled)
      assert(repH.testStartingEventsReceived.size === 2)
      assert(repH.testStartingEventsReceived.exists(_.testName == "test that"))
      assert(repH.testStartingEventsReceived.exists(_.testName == "test the other"))

      // No tagsToInclude set, mytags.SlowAsMolasses excluded
      class IFunSpec(val counts: ThreeCounts) extends PathFunSpec {
        it("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { counts.theTestThisCalled = true }
        it("test that", mytags.SlowAsMolasses) { counts.theTestThatCalled = true }
        it("test the other") { counts.theTestTheOtherCalled = true }
        override def newInstance = new IFunSpec(counts)
      }
      val i = new IFunSpec(ThreeCounts(false, false, false))
      val repI = new EventRecordingReporter
      i.run(None, repI, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses")), Map(), None, new Tracker)
      assert(repI.testIgnoredEventsReceived.isEmpty)
      assert(i.counts.theTestThisCalled)
      assert(i.counts.theTestThatCalled)
      assert(i.counts.theTestTheOtherCalled)
      assert(repI.testStartingEventsReceived.size === 1)
      assert(repI.testStartingEventsReceived(0).testName === "test the other")

      // No tagsToInclude set, mytags.SlowAsMolasses excluded, TestIgnored should not be received on excluded ones
      class JFunSpec(val counts: ThreeCounts) extends PathFunSpec {
        ignore("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { counts.theTestThisCalled = true }
        ignore("test that", mytags.SlowAsMolasses) { counts.theTestThatCalled = true }
        it("test the other") { counts.theTestTheOtherCalled = true }
        override def newInstance = new JFunSpec(counts)
      }
      val j = new JFunSpec(ThreeCounts(false, false, false))
      val repJ = new TestIgnoredTrackingReporter
      j.run(None, repJ, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses")), Map(), None, new Tracker)
      assert(!repJ.testIgnoredReceived)
      assert(!j.counts.theTestThisCalled)
      assert(!j.counts.theTestThatCalled)
      assert(j.counts.theTestTheOtherCalled)

      // Same as previous, except Ignore specifically mentioned in excludes set
      class KFunSpec(val counts: ThreeCounts) extends PathFunSpec {
        ignore("test this", mytags.SlowAsMolasses, mytags.FastAsLight) { counts.theTestThisCalled = true }
        ignore("test that", mytags.SlowAsMolasses) { counts.theTestThatCalled = true }
        ignore("test the other") { counts.theTestTheOtherCalled = true }
        override def newInstance = new KFunSpec(counts)
      }
      val k = new KFunSpec(ThreeCounts(false, false, false))
      val repK = new TestIgnoredTrackingReporter
      k.run(None, repK, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore")), Map(), None, new Tracker)
      assert(repK.testIgnoredReceived)
      assert(!k.counts.theTestThisCalled)
      assert(!k.counts.theTestThatCalled)
      assert(!k.counts.theTestTheOtherCalled)
    }

    "should return the correct test count from its expectedTestCount method" in {

      class AFunSpec extends PathFunSpec {
        it("test this") {}
        it("test that") {}
        override def newInstance = new AFunSpec
      }
      val a = new AFunSpec
      assert(a.expectedTestCount(Filter()) === 2)

      class BFunSpec extends PathFunSpec {
        ignore("test this") {}
        it("test that") {}
        override def newInstance = new BFunSpec
      }
      val b = new BFunSpec
      assert(b.expectedTestCount(Filter()) === 1)

      class CFunSpec extends PathFunSpec {
        it("test this", mytags.FastAsLight) {}
        it("test that") {}
        override def newInstance = new CFunSpec
      }
      val c = new CFunSpec
      assert(c.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(c.expectedTestCount(Filter(None, Set("org.scalatest.FastAsLight"))) === 1)

      class DFunSpec extends PathFunSpec {
        it("test this", mytags.FastAsLight, mytags.SlowAsMolasses) {}
        it("test that", mytags.SlowAsMolasses) {}
        it("test the other thing") {}
        override def newInstance = new DFunSpec
      }
      val d = new DFunSpec
      assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
      assert(d.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 1)
      assert(d.expectedTestCount(Filter()) === 3)

      println("got here")
      class EFunSpec extends PathFunSpec {
        it("test this", mytags.FastAsLight, mytags.SlowAsMolasses) {}
        it("test that", mytags.SlowAsMolasses) {}
        ignore("test the other thing") {}
       // ignore("test the other thing") {}
        override def newInstance = new EFunSpec
      }
      val e = new EFunSpec
      assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
      assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
      assert(e.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 0)
      assert(e.expectedTestCount(Filter()) === 2)

      val f = new Suites(a, b, c, d, e)
      assert(f.expectedTestCount(Filter()) === 10)
    }
    
    "should generate a TestPending message when the test body is (pending)" in {

      class AFunSpec extends PathFunSpec {

        it("should do this") (pending)

        it("should do that") {
          assert(2 + 2 === 4)
        }
        
        it("should do something else") {
          assert(2 + 2 === 4)
          pending
        }

        override def newInstance = new AFunSpec
      }
      val a = new AFunSpec
      
      val rep = new EventRecordingReporter
      a.run(None, rep, new Stopper {}, Filter(), Map(), None, new Tracker())
      val tp = rep.testPendingEventsReceived
      assert(tp.size === 2)
    }

    "should generate a test failure if a Throwable, or an Error other than direct Error subtypes known in JDK 1.5, excluding AssertionError" in {
      class AFunSpec extends PathFunSpec {
        it("throws AssertionError") { throw new AssertionError }
        it("throws plain old Error") { throw new Error }
        it("throws Throwable") { throw new Throwable }
        override def newInstance = new AFunSpec
      }
      val a = new AFunSpec
      val rep = new EventRecordingReporter
      a.run(None, rep, new Stopper {}, Filter(), Map(), None, new Tracker())
      val tf = rep.testFailedEventsReceived
      assert(tf.size === 3)
    }

    "should propagate out Errors that are direct subtypes of Error in JDK 1.5, other than AssertionError, causing Suites and Runs to abort." in {
      class AFunSpec extends PathFunSpec {
        it("throws AssertionError") { throw new OutOfMemoryError }
        override def newInstance = new AFunSpec
      }
      // val a = new AFunSpec
      intercept[OutOfMemoryError] {
        new AFunSpec
        // a.run(None, SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker())
      }
    }

    "should send InfoProvided events with aboutAPendingTest set to true for info calls made from a test that is pending" in {
      class AFunSpec extends PathFunSpec with GivenWhenThen {
        it("should do something else") {
          given("two integers")
          when("one is subracted from the other")
          then("the result is the difference between the two numbers")
          pending
        }
        override def newInstance = new AFunSpec
      }
      val a = new AFunSpec
      val rep = new EventRecordingReporter
      a.run(None, rep, new Stopper {}, Filter(), Map(), None, new Tracker())
      val ip = rep.infoProvidedEventsReceived
      assert(ip.size === 3)
      for (event <- ip) {
        assert(event.aboutAPendingTest.isDefined && event.aboutAPendingTest.get)
      }
    }
    "should send InfoProvided events with aboutAPendingTest set to false for info calls made from a test that is not pending" in {
      class AFunSpec extends PathFunSpec with GivenWhenThen {
        it("should do something else") {
          given("two integers")
          when("one is subracted from the other")
          then("the result is the difference between the two numbers")
          assert(1 + 1 === 2)
        }
        override def newInstance = new AFunSpec
      }
      val a = new AFunSpec
      val rep = new EventRecordingReporter
      a.run(None, rep, new Stopper {}, Filter(), Map(), None, new Tracker())
      val ip = rep.infoProvidedEventsReceived
      assert(ip.size === 3)
      for (event <- ip) {
        assert(event.aboutAPendingTest.isDefined && !event.aboutAPendingTest.get)
      }
    }
  }
}


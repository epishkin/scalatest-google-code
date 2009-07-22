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
import org.scalatest.mytags._

class WordSpecSpec extends Spec with SharedHelpers with GivenWhenThen {

  describe("A WordSpec") {


    describe("(when a nesting rule has been violated)") {

      it("should, if they call a describe from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends WordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" should {
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a describe with a nested it from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends WordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" should {
              "should never run" in {
                assert(1 === 2)
              }
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested it from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends WordSpec {
          "should blow up" in {
            "should never run" in {
              assert(1 === 2)
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested it with tags from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends WordSpec {
          "should blow up" in {
            "should never run" taggedAs(SlowAsMolasses) in {
              assert(1 === 2)
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a describe with a nested ignore from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends WordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" should {
              "should never run" ignore {
                assert(1 === 2)
              }
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested ignore from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends WordSpec {
          "should blow up" in {
            "should never run" ignore {
              assert(1 === 2)
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested ignore with tags from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends WordSpec {
          "should blow up" in {
            "should never run" taggedAs(SlowAsMolasses) ignore {
              assert(1 === 2)
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
    }

    it("should return the test names in registration order from testNames") {

      val a = new WordSpec {
        "it should test this" in {}
        "it should test that" in {}
      }

      expect(List("it should test this", "it should test that")) {
        a.testNames.elements.toList
      }

      val b = new WordSpec {}

      expect(List[String]()) {
        b.testNames.elements.toList
      }

      val c = new WordSpec {
        "it should test that" in {}
        "it should test this" in {}
      }

      expect(List("it should test that", "it should test this")) {
        c.testNames.elements.toList
      }

      val d = new WordSpec {
        "A Tester" should {
          "test that" in {}
          "test this" in {}
        }
      }

      expect(List("A Tester should test that", "A Tester should test this")) {
        d.testNames.elements.toList
      }

      val e = new WordSpec {
        "A Tester" should {
          "test this" in {}
          "test that" in {}
        }
      }

      expect(List("A Tester should test this", "A Tester should test that")) {
        e.testNames.elements.toList
      }
    }

    it("should throw DuplicateTestNameException if a duplicate test name registration is attempted") {
      
      intercept[DuplicateTestNameException] {
        new WordSpec {
          "should test this" in {}
          "should test this" in {}
        }
      }
      intercept[DuplicateTestNameException] {
        new WordSpec {
          "should test this" in {}
          "should test this" ignore {}
        }
      }
      intercept[DuplicateTestNameException] {
        new WordSpec {
          "should test this" ignore {}
          "should test this" ignore {}
        }
      }
      intercept[DuplicateTestNameException] {
        new WordSpec {
          "should test this" ignore {}
          "should test this" in {}
        }
      }
    }

    describe("(with info calls)") {
      class InfoInsideTestSpec extends WordSpec {
        val msg = "hi there, dude"
        val testName = "test name"
        testName in {
          info(msg)
        }
      }
      // In a Spec, any InfoProvided's fired during the test should be cached and sent out after the test has
      // suceeded or failed. This makes the report look nicer, because the info is tucked under the "specifier'
      // text for that test.
      it("should, when the info appears in the code of a successful test, report the info after the TestSucceeded") {
        val spec = new InfoInsideTestSpec
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(spec, spec.testName, spec.msg)
        assert(testSucceededIndex < infoProvidedIndex)
      }
      class InfoBeforeTestSpec extends WordSpec {
        val msg = "hi there, dude"
        val testName = "test name"
        info(msg)
        testName in {}
      }
      it("should, when the info appears in the body before a test, report the info before the test") {
        val spec = new InfoBeforeTestSpec
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(spec, spec.testName, spec.msg)
        assert(infoProvidedIndex < testStartingIndex)
        assert(testStartingIndex < testSucceededIndex)
      }
      it("should, when the info appears in the body after a test, report the info after the test runs") {
        val msg = "hi there, dude"
        val testName = "test name"
        class MySpec extends WordSpec {
          testName in {}
          info(msg)
        }
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(new MySpec, testName, msg)
        assert(testStartingIndex < testSucceededIndex)
        assert(testSucceededIndex < infoProvidedIndex)
      }
      it("should throw an IllegalStateException when info is called by a method invoked after the suite has been executed") {
        class MySpec extends WordSpec {
          callInfo() // This should work fine
          def callInfo() {
            info("howdy")
          }
          "howdy also" in {
            callInfo() // This should work fine
          }
        }
        val spec = new MySpec
        val myRep = new EventRecordingReporter
        spec.run(None, myRep, new Stopper {}, Filter(), Map(), None, new Tracker)
        intercept[IllegalStateException] {
          spec.callInfo()
        }
      }
      it("should send an InfoProvided with an IndentedText formatter with level 1 when called outside a test") {
        val spec = new InfoBeforeTestSpec
        val indentedText = getIndentedTextFromInfoProvided(spec)
        assert(indentedText === IndentedText("+ " + spec.msg, spec.msg, 1))
      }
      it("should send an InfoProvided with an IndentedText formatter with level 2 when called within a test") {
        val spec = new InfoInsideTestSpec
        val indentedText = getIndentedTextFromInfoProvided(spec)
        assert(indentedText === IndentedText("  + " + spec.msg, spec.msg, 2))
      }
    }
    it("should return registered tags, including ignore tags, from the tags method") {

      val a = new WordSpec {
        "should test this" ignore {}
        "should test that" in {}
      }
      expect(Map("should test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new WordSpec {
        "should test this" in {}
        "should test that" ignore {}
      }
      expect(Map("should test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new WordSpec {
        "should test this" ignore {}
        "should test that" ignore {}
      }
      expect(Map("should test this" -> Set("org.scalatest.Ignore"), "should test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new WordSpec {
        "should test this" in {}
        "should test that" in {} // was an in
      }
      expect(Map()) {
        d.tags
      }

      val e = new WordSpec {
        "should test this" taggedAs(SlowAsMolasses) in {}
        "should test that" taggedAs(SlowAsMolasses) ignore {}
      }
      expect(Map("should test this" -> Set("org.scalatest.SlowAsMolasses"), "should test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        e.tags
      }

      val f = new WordSpec {}
      expect(Map()) {
        f.tags
      }

      val g = new WordSpec {
        "should test this" taggedAs(SlowAsMolasses, WeakAsAKitten) in {}
        "should test that" taggedAs(SlowAsMolasses) in {}
      }
      expect(Map("should test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "should test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        g.tags
      }
    }
    it("should throw NullPointerException if a null test tag is provided") {
      // it
      intercept[NullPointerException] {
        new WordSpec {
          "hi" taggedAs(null) in {}
        }
      }
      val caught = intercept[NullPointerException] {
        new WordSpec {
          "hi" taggedAs(mytags.SlowAsMolasses, null) in {}
        }
      }
      assert(caught.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new WordSpec {
          "hi" taggedAs(mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) in {}
        }
      }
      // ignore
      intercept[NullPointerException] {
        new WordSpec {
          "hi" taggedAs(null) ignore {}
        }
      }
      val caught2 = intercept[NullPointerException] {
        new WordSpec {
          "hi" taggedAs(mytags.SlowAsMolasses, null) ignore {}
        }
      }
      assert(caught2.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new WordSpec {
          "hi" taggedAs(mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) ignore {}
        }
      }
    }
    it("should return a correct tags map from the tags method") {

      val a = new WordSpec {
        "test this" ignore {}
        "test that" in {}
      }
      expect(Map("test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new WordSpec {
        "test this" in {}
        "test that" ignore {}
      }
      expect(Map("test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new WordSpec {
        "test this" ignore {}
        "test that" ignore {}
      }
      expect(Map("test this" -> Set("org.scalatest.Ignore"), "test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new WordSpec {
        "test this" taggedAs(mytags.SlowAsMolasses) in {}
        "test that" taggedAs(mytags.SlowAsMolasses) ignore {}
      }
      expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }

      val e = new WordSpec {
        "test this" in {}
        "test that" in {}
      }
      expect(Map()) {
        e.tags
      }

      val f = new WordSpec {
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) in {}
        "test that" taggedAs(mytags.SlowAsMolasses) in  {}
      }
      expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        f.tags
      }

      val g = new WordSpec {
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) in {}
        "test that" taggedAs(mytags.SlowAsMolasses) in  {}
      }
      expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        g.tags
      }
    }
    it("should return a correct tags map from the tags method using is (pending)") {

      val a = new WordSpec {
        "test this" ignore {}
        "test that" is (pending)
      }
      expect(Map("test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new WordSpec {
        "test this" is (pending)
        "test that" ignore {}
      }
      expect(Map("test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new WordSpec {
        "test this" ignore {}
        "test that" ignore {}
      }
      expect(Map("test this" -> Set("org.scalatest.Ignore"), "test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new WordSpec {
        "test this" taggedAs(mytags.SlowAsMolasses) is (pending)
        "test that" taggedAs(mytags.SlowAsMolasses) ignore {}
      }
      expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }

      val e = new WordSpec {
        "test this" is (pending)
        "test that" is (pending)
      }
      expect(Map()) {
        e.tags
      }

      val f = new WordSpec {
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) is (pending)
        "test that" taggedAs(mytags.SlowAsMolasses) in  {}
      }
      expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        f.tags
      }

      val g = new WordSpec {
        "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) is (pending)
        "test that" taggedAs(mytags.SlowAsMolasses) in  {}
      }
      expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        g.tags
      }
    }

    class TestWasCalledSuite extends WordSpec {
      var theTestThisCalled = false
      var theTestThatCalled = false
      "run this" in { theTestThisCalled = true }
      "run that, maybe" in { theTestThatCalled = true }
    }

    it("should execute all tests when run is called with testName None") {

      val b = new TestWasCalledSuite
      b.run(None, SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(b.theTestThisCalled)
      assert(b.theTestThatCalled)
    }

    it("should execute one test when run is called with a defined testName") {

      val a = new TestWasCalledSuite
      a.run(Some("run this"), SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(a.theTestThisCalled)
      assert(!a.theTestThatCalled)
    }

    it("should report as ignored, and not run, tests marked ignored") {

      val a = new WordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" in { theTestThisCalled = true }
        "test that" in { theTestThatCalled = true }
      }

      val repA = new TestIgnoredTrackingReporter
      a.run(None, repA, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(!repA.testIgnoredReceived)
      assert(a.theTestThisCalled)
      assert(a.theTestThatCalled)

      val b = new WordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" ignore { theTestThisCalled = true }
        "test that" in { theTestThatCalled = true }
      }

      val repB = new TestIgnoredTrackingReporter
      b.run(None, repB, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repB.testIgnoredReceived)
      assert(repB.lastEvent.isDefined)
      assert(repB.lastEvent.get.testName endsWith "test this")
      assert(!b.theTestThisCalled)
      assert(b.theTestThatCalled)

      val c = new WordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" in { theTestThisCalled = true }
        "test that" ignore { theTestThatCalled = true }
      }

      val repC = new TestIgnoredTrackingReporter
      c.run(None, repC, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repC.testIgnoredReceived)
      assert(repC.lastEvent.isDefined)
      assert(repC.lastEvent.get.testName endsWith "test that", repC.lastEvent.get.testName)
      assert(c.theTestThisCalled)
      assert(!c.theTestThatCalled)

      // The order I want is order of appearance in the file.
      // Will try and implement that tomorrow. Subtypes will be able to change the order.
      val d = new WordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" ignore { theTestThisCalled = true }
        "test that" ignore { theTestThatCalled = true }
      }

      val repD = new TestIgnoredTrackingReporter
      d.run(None, repD, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repD.testIgnoredReceived)
      assert(repD.lastEvent.isDefined)
      assert(repD.lastEvent.get.testName endsWith "test that") // last because should be in order of appearance
      assert(!d.theTestThisCalled)
      assert(!d.theTestThatCalled)
    }

    it("should run a test marked as ignored if run is invoked with that testName") {
      // If I provide a specific testName to run, then it should ignore an Ignore on that test
      // method and actually invoke it.
      val e = new WordSpec {
        var theTestThisCalled = false
        var theTestThatCalled = false
        "test this" ignore { theTestThisCalled = true }
        "test that" in { theTestThatCalled = true }
      }

      val repE = new TestIgnoredTrackingReporter
      e.run(Some("test this"), repE, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(!repE.testIgnoredReceived)
      assert(e.theTestThisCalled)
      assert(!e.theTestThatCalled)
    }
  }
}

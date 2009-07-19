/*
 * Copyright 2001-2009 Artima, Inc.
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
import org.scalatest.matchers.ShouldVerb
import org.scalatest.matchers.MustVerb
import org.scalatest.matchers.CanVerb
import org.scalatest.events._
import org.scalatest.mytags._

class FlatSpecSpec extends Spec with SharedHelpers with GivenWhenThen {

  describe("A FlatSpec") {

    it("should return the test names in registration order from testNames when using 'it should'") {

      val a = new FlatSpec {
        it should "test this" in {}
        it should "test that" in {}
      }

      expect(List("should test this", "should test that")) {
        a.testNames.elements.toList
      }

      val b = new FlatSpec {}

      expect(List[String]()) {
        b.testNames.elements.toList
      }

      val c = new FlatSpec {
        it should "test that" in {}
        it should "test this" in {}
      }

      expect(List("should test that", "should test this")) {
        c.testNames.elements.toList
      }

      val d = new FlatSpec {
        behavior of "A Tester"
        it should "test that" in {}
        it should "test this" in {}
      }

      expect(List("A Tester should test that", "A Tester should test this")) {
        d.testNames.elements.toList
      }

      val e = new FlatSpec {
        behavior of "A Tester"
        it should "test this" in {}
        it should "test that" in {}
      }

      expect(List("A Tester should test this", "A Tester should test that")) {
        e.testNames.elements.toList
      }
    }

    it("should throw DuplicateTestNameException if a duplicate test name registration is attempted") {
      
      intercept[DuplicateTestNameException] {
        new FlatSpec {
          it should "test this" in {}
          it should "test this" in {}
        }
      }
      intercept[DuplicateTestNameException] {
        new FlatSpec {
          it should "test this" in {}
          ignore should "test this" in {}
        }
      }
      intercept[DuplicateTestNameException] {
        new FlatSpec {
          ignore should "test this" in {}
          it should "test this" ignore {}
        }
      }
      intercept[DuplicateTestNameException] {
        new FlatSpec {
          ignore should "test this" in {}
          it should "test this" in {}
        }
      }
    }

    it("should return registered tags, including ignore tags, from the tags method") {

      val a = new FlatSpec {
        ignore should "test this" in {}
        it should "test that" in {}
      }
      expect(Map("should test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new FlatSpec {
        it should "test this" in {}
        ignore should "test that" in {}
      }
      expect(Map("should test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new FlatSpec {
        ignore should "test this" in {}
        ignore should "test that" in {}
      }
      expect(Map("should test this" -> Set("org.scalatest.Ignore"), "should test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new FlatSpec {
        it should "test this" in {}
        it should "test that" in {}
      }
      expect(Map()) {
        d.tags
      }

      val e = new FlatSpec {
        it should "test this" taggedAs(SlowAsMolasses) in {}
        ignore should "test that" taggedAs(SlowAsMolasses) in {}
      }
      expect(Map("should test this" -> Set("org.scalatest.SlowAsMolasses"), "should test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        e.tags
      }

      val f = new FlatSpec {}
      expect(Map()) {
        f.tags
      }

      val g = new FlatSpec {
        it should "test this" taggedAs(SlowAsMolasses, mytags.WeakAsAKitten) in {}
        it should "test that" taggedAs(SlowAsMolasses) in {}
      }
      expect(Map("should test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "should test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        g.tags
      }
    }

    describe("(with info calls)") {
      class InfoInsideTestFlatSpec extends FlatSpec {
        val msg = "hi there, dude"
        val partialTestName = "test name"
        val testName = "should " + partialTestName
        it should partialTestName in {
          info(msg)
        }
      }
      // In a FlatSpec, any InfoProvided's fired during the test should be cached and sent out after the test has
      // suceeded or failed. This makes the report look nicer, because the info is tucked under the "specifier'
      // text for that test.
      it("should, when the info appears in the code of a successful test, report the info after the TestSucceeded") {
        val spec = new InfoInsideTestFlatSpec
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(spec, spec.testName, spec.msg)
        assert(testSucceededIndex < infoProvidedIndex)
      }
      class InfoBeforeTestFlatSpec extends FlatSpec {
        val msg = "hi there, dude"
        val partialTestName = "test name"
        val testName = "should " + partialTestName
        info(msg)
        it should partialTestName in {}
      }
      it("should, when the info appears in the body before a test, report the info before the test") {
        val spec = new InfoBeforeTestFlatSpec
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(spec, spec.testName, spec.msg)
        assert(infoProvidedIndex < testStartingIndex)
        assert(testStartingIndex < testSucceededIndex)
      }
      it("should, when the info appears in the body after a test, report the info after the test runs") {
        val msg = "hi there, dude"
        val partialTestName = "test name"
        val testName = "should " + partialTestName
        class MyFlatSpec extends FlatSpec {
          it should partialTestName in {}
          info(msg)
        }
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(new MyFlatSpec, testName, msg)
        assert(testStartingIndex < testSucceededIndex)
        assert(testSucceededIndex < infoProvidedIndex)
      }
      it("should throw an IllegalStateException when info is called by a method invoked after the suite has been executed") {
        class MyFlatSpec extends FlatSpec {
          callInfo() // This should work fine
          def callInfo() {
            info("howdy")
          }
          it should "howdy also" in {
            callInfo() // This should work fine
          }
        }
        val spec = new MyFlatSpec
        val myRep = new EventRecordingReporter
        spec.run(None, myRep, new Stopper {}, Filter(), Map(), None, new Tracker)
        intercept[IllegalStateException] {
          spec.callInfo()
        }
      }
      it("should send an InfoProvided with an IndentedText formatter with level 1 when called outside a test") {
        val spec = new InfoBeforeTestFlatSpec
        val indentedText = getIndentedTextFromInfoProvided(spec)
        assert(indentedText === IndentedText("+ " + spec.msg, spec.msg, 1))
      }
      it("should send an InfoProvided with an IndentedText formatter with level 2 when called within a test") {
        val spec = new InfoInsideTestFlatSpec
        val indentedText = getIndentedTextFromInfoProvided(spec)
        assert(indentedText === IndentedText("  + " + spec.msg, spec.msg, 2))
      }
      it("should work when using the shorthand notation for 'behavior of'") {
        val e = new FlatSpec with ShouldMatchers {
          "A Tester" should "test this" in {}
          it should "test that" in {}
        }

        expect(List("A Tester should test this", "A Tester should test that")) {
          e.testNames.elements.toList
        }

      }
    }
    describe("(when a nesting rule has been violated)") {

      it("should, if they call a behavior-of from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends FlatSpec {
          it should "blow up" in {
            behavior of "in the wrong place, at the wrong time"
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a behavior-of with a nested it from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends FlatSpec {
          it should "blow up" in {
            behavior of "in the wrong place, at the wrong time"
            it should "never run" in {
              assert(1 === 2)
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested it from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends FlatSpec {
          it should "blow up" in {
            it should "never run" in {
              assert(1 === 2)
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested it with tags from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends FlatSpec {
          it should "blow up" in {
            it should "never run" taggedAs(SlowAsMolasses) in {
              assert(1 === 2)
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a behavior-of with a nested ignore from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends FlatSpec {
          it should "blow up" in {
            behavior of "in the wrong place, at the wrong time"
            ignore should "never run" in {
              assert(1 === 2)
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested ignore from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends FlatSpec {
          it should "blow up" in {
            ignore should "never run" in {
              assert(1 === 2)
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a nested ignore with tags from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends FlatSpec {
          it should "blow up" in {
            ignore should "never run" taggedAs(SlowAsMolasses) in {
              assert(1 === 2)
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
    }
    it("should run tests registered via the 'it should behave like' syntax") {
      trait SharedFlatSpecTests { this: FlatSpec =>
        def nonEmptyStack(s: String)(i: Int) {
          it should "I am shared" in {}
        }
      }
      class MyFlatSpec extends FlatSpec with SharedFlatSpecTests {
        it should behave like nonEmptyStack("hi")(1)
      }
      val suite = new MyFlatSpec
      val reporter = new EventRecordingReporter
      suite.run(None, reporter, new Stopper {}, Filter(), Map(), None, new Tracker)

      val indexedList = reporter.eventsReceived

      val testStartingOption = indexedList.find(_.isInstanceOf[TestStarting])
      assert(testStartingOption.isDefined)
      assert(testStartingOption.get.asInstanceOf[TestStarting].testName === "should I am shared")
    }
    it("should run tests registered via the 'it can behave like' syntax") {
      trait SharedFlatSpecTests { this: FlatSpec =>
        def nonEmptyStack(s: String)(i: Int) {
          it can "I am shared" in {}
        }
      }
      class MyFlatSpec extends FlatSpec with SharedFlatSpecTests {
        it can behave like nonEmptyStack("hi")(1)
      }
      val suite = new MyFlatSpec
      val reporter = new EventRecordingReporter
      suite.run(None, reporter, new Stopper {}, Filter(), Map(), None, new Tracker)

      val indexedList = reporter.eventsReceived

      val testStartingOption = indexedList.find(_.isInstanceOf[TestStarting])
      assert(testStartingOption.isDefined)
      assert(testStartingOption.get.asInstanceOf[TestStarting].testName === "can I am shared")
    }
    it("should throw NullPointerException if a null test tag is provided") {
      // it
      intercept[NullPointerException] {
        new FlatSpec {
          it should "hi" taggedAs(null) in {}
        }
      }
      val caught = intercept[NullPointerException] {
        new FlatSpec {
          it should "hi" taggedAs(mytags.SlowAsMolasses, null) in {}
        }
      }
      assert(caught.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new FlatSpec {
          it should "hi" taggedAs(mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) in {}
        }
      }
      // ignore
      intercept[NullPointerException] {
        new FlatSpec {
          ignore should "hi" taggedAs(null) in {}
        }
      }
      val caught2 = intercept[NullPointerException] {
        new FlatSpec {
          ignore should "hi" taggedAs(mytags.SlowAsMolasses, null) in {}
        }
      }
      assert(caught2.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new FlatSpec {
          ignore should "hi" taggedAs(mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) in {}
        }
      }
      intercept[NullPointerException] {
        new FlatSpec {
          it should "hi" taggedAs(null) ignore {}
        }
      }
      val caught3 = intercept[NullPointerException] {
        new FlatSpec {
          it should "hi" taggedAs(mytags.SlowAsMolasses, null) ignore {}
        }
      }
      assert(caught3.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new FlatSpec {
          it should "hi" taggedAs(mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) ignore {}
        }
      }
    }
    it("should return a correct tags map from the tags method, when using regular (not shorthand)" +
            " notation and ignore replacing it") {

      val a = new FlatSpec {
        ignore should "test this" in {}
        it should "test that" in {}
      }
      expect(Map("should test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new FlatSpec {
        it can "test this" in {}
        ignore can "test that" in {}
      }
      expect(Map("can test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new FlatSpec {
        ignore must "test this" in {}
        ignore must "test that" in {}
      }
      expect(Map("must test this" -> Set("org.scalatest.Ignore"), "must test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new FlatSpec {
        it must "test this" taggedAs(mytags.SlowAsMolasses) in {}
        ignore must "test that" taggedAs(mytags.SlowAsMolasses) in {}
      }
      expect(Map("must test this" -> Set("org.scalatest.SlowAsMolasses"), "must test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }

      val e = new FlatSpec {
        it must "test this" in {}
        it must "test that" in {}
      }
      expect(Map()) {
        e.tags
      }

      val f = new FlatSpec {
        it can "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) in {}
        it can "test that" taggedAs(mytags.SlowAsMolasses) in  {}
      }
      expect(Map("can test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "can test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        f.tags
      }

      val g = new FlatSpec {
        it should "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) in {}
        it should "test that" taggedAs(mytags.SlowAsMolasses) in  {}
      }
      expect(Map("should test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "should test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        g.tags
      }
    }
    it("should return a correct tags map from the tags method, when using regular (not shorthand)" +
            " notation and ignore replacing in") {

      val a = new FlatSpec {
        it should "test this" ignore {}
        it should "test that" in {}
      }
      expect(Map("should test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new FlatSpec {
        it can "test this" in {}
        it can "test that" ignore {}
      }
      expect(Map("can test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new FlatSpec {
        it must "test this" ignore {}
        it must "test that" ignore {}
      }
      expect(Map("must test this" -> Set("org.scalatest.Ignore"), "must test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new FlatSpec {
        it must "test this" taggedAs(mytags.SlowAsMolasses) in {}
        it must "test that" taggedAs(mytags.SlowAsMolasses) ignore {}
      }
      expect(Map("must test this" -> Set("org.scalatest.SlowAsMolasses"), "must test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }
    }

    it("should return a correct tags map from the tags method, when using shorthand notation") {

      val a = new FlatSpec with ShouldVerb {
        "A Stack" should "test this" ignore {}
        "A Stack" should "test that" in {}
      }
      expect(Map("A Stack should test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new FlatSpec with CanVerb {
        "A Stack" can "test this" in {}
        "A Stack" can "test that" ignore {}
      }
      expect(Map("A Stack can test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new FlatSpec with MustVerb {
        "A Stack" must "test this" ignore {}
        "A Stack" must "test that" ignore {}
      }
      expect(Map("A Stack must test this" -> Set("org.scalatest.Ignore"), "A Stack must test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new FlatSpec with MustVerb {
        "A Stack" must "test this" taggedAs(mytags.SlowAsMolasses) in {}
        "A Stack" must "test that" taggedAs(mytags.SlowAsMolasses) ignore {}
      }
      expect(Map("A Stack must test this" -> Set("org.scalatest.SlowAsMolasses"), "A Stack must test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }

      val e = new FlatSpec with MustVerb {
        "A Stack" must "test this" in {}
        "A Stack" must "test that" in {}
      }
      expect(Map()) {
        e.tags
      }

      val f = new FlatSpec with CanVerb {
        "A Stack" can "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) in {}
        "A Stack" can "test that" taggedAs(mytags.SlowAsMolasses) in  {}
      }
      expect(Map("A Stack can test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "A Stack can test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        f.tags
      }

      val g = new FlatSpec with ShouldVerb {
        "A Stack" should "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) in {}
        "A Stack" should "test that" taggedAs(mytags.SlowAsMolasses) in  {}
      }
      expect(Map("A Stack should test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "A Stack should test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        g.tags
      }
    }

    it("should return a correct tags map from the tags method when is (pending), when using regular (not shorthand)" +
            " notation and ignore replacing it") {

      val a = new FlatSpec {
        ignore should "test this" is (pending)
        it should "test that" is (pending)
      }
      expect(Map("should test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new FlatSpec {
        it can "test this" is (pending)
        ignore can "test that" is (pending)
      }
      expect(Map("can test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new FlatSpec {
        ignore must "test this" is (pending)
        ignore must "test that" is (pending)
      }
      expect(Map("must test this" -> Set("org.scalatest.Ignore"), "must test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new FlatSpec {
        it must "test this" taggedAs(mytags.SlowAsMolasses) is (pending)
        ignore must "test that" taggedAs(mytags.SlowAsMolasses) is (pending)
      }
      expect(Map("must test this" -> Set("org.scalatest.SlowAsMolasses"), "must test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }

      val e = new FlatSpec {
        it must "test this" is (pending)
        it must "test that" is (pending)
      }
      expect(Map()) {
        e.tags
      }

      val f = new FlatSpec {
        it can "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) is (pending)
        it can "test that" taggedAs(mytags.SlowAsMolasses) in  {}
      }
      expect(Map("can test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "can test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        f.tags
      }

      val g = new FlatSpec {
        it should "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) is (pending)
        it should "test that" taggedAs(mytags.SlowAsMolasses) in  {}
      }
      expect(Map("should test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "should test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        g.tags
      }
    }
    it("should return a correct tags map from the tags method is (pending), when using regular (not shorthand)" +
            " notation and ignore replacing is") {

      val a = new FlatSpec {
        it should "test this" ignore {}
        it should "test that" is (pending)
      }
      expect(Map("should test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new FlatSpec {
        it can "test this" is (pending)
        it can "test that" ignore {}
      }
      expect(Map("can test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new FlatSpec {
        it must "test this" ignore {}
        it must "test that" ignore {}
      }
      expect(Map("must test this" -> Set("org.scalatest.Ignore"), "must test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new FlatSpec {
        it must "test this" taggedAs(mytags.SlowAsMolasses) is (pending)
        it must "test that" taggedAs(mytags.SlowAsMolasses) ignore {}
      }
      expect(Map("must test this" -> Set("org.scalatest.SlowAsMolasses"), "must test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }
    }

    it("should return a correct tags map from the tags method is (pending), when using shorthand notation") {

      val a = new FlatSpec with ShouldVerb {
        "A Stack" should "test this" ignore {}
        "A Stack" should "test that" is (pending)
      }
      expect(Map("A Stack should test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new FlatSpec with CanVerb {
        "A Stack" can "test this" is (pending)
        "A Stack" can "test that" ignore {}
      }
      expect(Map("A Stack can test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new FlatSpec with MustVerb {
        "A Stack" must "test this" ignore {}
        "A Stack" must "test that" ignore {}
      }
      expect(Map("A Stack must test this" -> Set("org.scalatest.Ignore"), "A Stack must test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new FlatSpec with MustVerb {
        "A Stack" must "test this" taggedAs(mytags.SlowAsMolasses) is (pending)
        "A Stack" must "test that" taggedAs(mytags.SlowAsMolasses) ignore {}
      }
      expect(Map("A Stack must test this" -> Set("org.scalatest.SlowAsMolasses"), "A Stack must test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }

      val e = new FlatSpec with MustVerb {
        "A Stack" must "test this" is (pending)
        "A Stack" must "test that" is (pending)
      }
      expect(Map()) {
        e.tags
      }

      val f = new FlatSpec with CanVerb {
        "A Stack" can "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) is (pending)
        "A Stack" can "test that" taggedAs(mytags.SlowAsMolasses) is (pending)
      }
      expect(Map("A Stack can test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "A Stack can test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        f.tags
      }

      val g = new FlatSpec with ShouldVerb {
        "A Stack" should "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) is (pending)
        "A Stack" should "test that" taggedAs(mytags.SlowAsMolasses) is (pending)
      }
      expect(Map("A Stack should test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "A Stack should test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        g.tags
      }
    }
  }
}


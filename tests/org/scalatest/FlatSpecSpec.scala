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

import matchers.ShouldMatchers
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
          ignore should "test this" in {}
        }
      }
      intercept[DuplicateTestNameException] {
        new FlatSpec {
          ignore should "test this" in {}
          it should "test this" in {}
        }
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
  }
}


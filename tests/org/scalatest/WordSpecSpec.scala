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
        "A Tester" can {
          "test that" in {}
          "test this" in {}
        }
      }

      expect(List("A Tester can test that", "A Tester can test this")) {
        d.testNames.elements.toList
      }

      val e = new WordSpec {
        "A Tester" can {
          "test this" in {}
          "test that" in {}
        }
      }

      expect(List("A Tester can test this", "A Tester can test that")) {
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
          ignore test "should test this" in {}  
        }
      }
      intercept[DuplicateTestNameException] {
        new WordSpec {
          ignore test "should test this" in {}
          ignore test "should test this" in {}
        }
      }
      intercept[DuplicateTestNameException] {
        new WordSpec {
          ignore test "should test this" in {}
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
        ignore test "should test this" in {}
        "should test that" in {}
      }
      expect(Map("should test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new WordSpec {
        "should test this" in {}
        ignore test "should test that" in {}
      }
      expect(Map("should test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new WordSpec {
        ignore test "should test this" in {}
        ignore test "should test that" in {}
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
        ignore test "should test that" taggedAs(SlowAsMolasses) in {}
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

    describe("(when a nesting rule has been violated)") {

      it("should, if they call a describe from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends WordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" can {
            }
          }
        }

        val spec = new MySpec
        ensureTestFailedEventReceived(spec, "should blow up")
      }
      it("should, if they call a describe with a nested it from within an it clause, result in a TestFailedException when running the test") {

        class MySpec extends WordSpec {
          "should blow up" in {
            "in the wrong place, at the wrong time" can {
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
            "in the wrong place, at the wrong time" can {
              ignore test "should never run" in {
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
            ignore test "should never run" in {
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
            ignore test "should never run" taggedAs(SlowAsMolasses) in {
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


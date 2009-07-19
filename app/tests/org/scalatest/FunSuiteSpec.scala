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

class FunSuiteSpec extends Spec with SharedHelpers {

  describe("A FunSuite") {

    it("should return the test names in registration order from testNames") {
      
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
        test("test that") {}
        test("test this") {}
      }

      expect(List("test that", "test this")) {
        c.testNames.elements.toList
      }
    }

    it("should throw NotAllowedException if a duplicate test name registration is attempted") {

      intercept[DuplicateTestNameException] {
        new FunSuite {
          test("test this") {}
          test("test this") {}
        }
      }
      intercept[DuplicateTestNameException] {
        new FunSuite {
          test("test this") {}
          ignore("test this") {}
        }
      }
      intercept[DuplicateTestNameException] {
        new FunSuite {
          ignore("test this") {}
          ignore("test this") {}
        }
      }
      intercept[DuplicateTestNameException] {
        new FunSuite {
          ignore("test this") {}
          test("test this") {}
        }
      }
    }

    it("should throw NotAllowedException if test registration is attempted after run has been invoked on a suite") {
      class InvokedWhenNotRunningSuite extends FunSuite {
        var fromMethodTestExecuted = false
        var fromConstructorTestExecuted = false
        test("from constructor") {
          fromConstructorTestExecuted = true
        }
        def tryToRegisterATest() {
          test("from method") {
            fromMethodTestExecuted = true
          }
        }
      }
      val suite = new InvokedWhenNotRunningSuite
      suite.run(None, SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(suite.fromConstructorTestExecuted)
      assert(!suite.fromMethodTestExecuted)
      intercept[TestRegistrationClosedException] {
        suite.tryToRegisterATest()
      }
      suite.run(None, SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(!suite.fromMethodTestExecuted)
/*
      class InvokedWhenRunningSuite extends FunSuite {
        var fromMethodTestExecuted = false
        var fromConstructorTestExecuted = false
        test("from constructor") {
          tryToRegisterATest()
          fromConstructorTestExecuted = true
        }
        def tryToRegisterATest() {
          test("from method") {
            fromMethodTestExecuted = true
          }
        }
      }
      val a = new InvokedWhenNotRunningSuite
      a.run()
      intercept[TestFailedException] {
        new InvokedWhenRunningSuite
      } */
    }

    describe("(with info calls)") {
      it("should, when the info appears in the code of a successful test, report the info between the TestStarting and TestSucceeded") {
        val msg = "hi there, dude"
        val testName = "test name"
        class MySuite extends FunSuite {
          test(testName) {
            info(msg)
          }
        }
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(new MySuite, testName, msg)
        assert(testStartingIndex < infoProvidedIndex)
        assert(infoProvidedIndex < testSucceededIndex)
      }
      it("should, when the info appears in the body before a test, report the info before the test") {
        val msg = "hi there, dude"
        val testName = "test name"
        class MySuite extends FunSuite {
          info(msg)
          test(testName) {}
        }
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(new MySuite, testName, msg)
        assert(infoProvidedIndex < testStartingIndex)
        assert(testStartingIndex < testSucceededIndex)
      }
      it("should, when the info appears in the body after a test, report the info after the test runs") {
        val msg = "hi there, dude"
        val testName = "test name"
        class MySuite extends FunSuite {
          test(testName) {}
          info(msg)
        }
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(new MySuite, testName, msg)
        assert(testStartingIndex < testSucceededIndex)
        assert(testSucceededIndex < infoProvidedIndex)
      }
      it("should throw an NotAllowedException when info is called by a method invoked after the suite has been executed") {
        class MySuite extends FunSuite {
          callInfo() // This should work fine
          def callInfo() {
            info("howdy")
          }
          test("howdy also") {
            callInfo() // This should work fine
          }
        }
        val suite = new MySuite
        val myRep = new EventRecordingReporter
        suite.run(None, myRep, new Stopper {}, Filter(), Map(), None, new Tracker)
        intercept[IllegalStateException] {
          suite.callInfo()
        }
      }
    }
    it("should run tests registered via the testsFor syntax") {
      trait SharedFunSuiteTests { this: FunSuite =>
        def nonEmptyStack(s: String)(i: Int) {
          test("I am shared") {}
        }
      }
      class MySuite extends FunSuite with SharedFunSuiteTests {
        testsFor(nonEmptyStack("hi")(1))
      }
      val suite = new MySuite
      val reporter = new EventRecordingReporter
      suite.run(None, reporter, new Stopper {}, Filter(), Map(), None, new Tracker)

      val indexedList = reporter.eventsReceived

      val testStartingOption = indexedList.find(_.isInstanceOf[TestStarting])
      assert(testStartingOption.isDefined)
      assert(testStartingOption.get.asInstanceOf[TestStarting].testName === "I am shared")
    }
    it("should throw NullPointerException if a null test tag is provided") {
      // test
      intercept[NullPointerException] {
        new FunSuite {
          test("hi", null) {}
        }
      }
      val caught = intercept[NullPointerException] {
        new FunSuite {
          test("hi", mytags.SlowAsMolasses, null) {}
        }
      }
      assert(caught.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new FunSuite {
          test("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) {}
        }
      }
      // ignore
      intercept[NullPointerException] {
        new FunSuite {
          ignore("hi", null) {}
        }
      }
      val caught2 = intercept[NullPointerException] {
        new FunSuite {
          ignore("hi", mytags.SlowAsMolasses, null) {}
        }
      }
      assert(caught2.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new FunSuite {
          ignore("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) {}
        }
      }
    }
    it("should return a correct tags map from the tags method") {

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
  }
}



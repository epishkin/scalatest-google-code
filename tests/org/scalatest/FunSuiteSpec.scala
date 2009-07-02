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

class FunSuiteSpec extends Spec with HandyReporters {

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

    it("should throw TestFailedException if a duplicate test name registration is attempted") {

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
          runCommonInformerTestCode(new MySuite, testName, msg)
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
          runCommonInformerTestCode(new MySuite, testName, msg)
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
          runCommonInformerTestCode(new MySuite, testName, msg)
        assert(testStartingIndex < testSucceededIndex)
        assert(testSucceededIndex < infoProvidedIndex)
      }
      it("should throw an IllegalStateException when info is called by a method invoked after the suite has been executed") {
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
        suite.run(None, myRep, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
        intercept[IllegalStateException] {
          suite.callInfo()
        }
      }
    }
  }
}



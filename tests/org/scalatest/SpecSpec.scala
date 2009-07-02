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

class SpecSpec extends Spec with HandyReporters {

  describe("A Spec") {

    it("should return the test names in registration order from testNames") {

      val a = new Spec {
        it("should test this") {}
        it("should test that") {}
      }

      expect(List("should test this", "should test that")) {
        a.testNames.elements.toList
      }

      val b = new Spec {}

      expect(List[String]()) {
        b.testNames.elements.toList
      }

      val c = new Spec {
        it("should test that") {}
        it("should test this") {}
      }

      expect(List("should test that", "should test this")) {
        c.testNames.elements.toList
      }

      val d = new Spec {
        describe("A Tester") {
          it("should test that") {}
          it("should test this") {}
        }
      }

      expect(List("A Tester should test that", "A Tester should test this")) {
        d.testNames.elements.toList
      }

      val e = new Spec {
        describe("A Tester") {
          it("should test this") {}
          it("should test that") {}
        }
      }

      expect(List("A Tester should test this", "A Tester should test that")) {
        e.testNames.elements.toList
      }
    }

    it("should throw TestFailedException if a duplicate test name registration is attempted") {
      
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
    describe("(with info calls)") {
      it("should, when the info appears in the code of a successful test, report the info between the TestStarting and TestSucceeded") {
        val msg = "hi there, dude"
        val testName = "test name"
        class MySpec extends Spec {
          it(testName) {
            info(msg)
          }
        }
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          runCommonInformerTestCode(new MySpec, testName, msg)
        assert(testStartingIndex < infoProvidedIndex)
        assert(infoProvidedIndex < testSucceededIndex)
      }
      ignore("should, when the info appears in the body before a test, report the info before the test") {
        val msg = "hi there, dude"
        val testName = "test name"
        class MySpec extends Spec {
          info(msg)
          it(testName) {}
        }
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          runCommonInformerTestCode(new MySpec, testName, msg)
        assert(infoProvidedIndex < testStartingIndex)
        assert(testStartingIndex < testSucceededIndex)
      }
      ignore("should, when the info appears in the body after a test, report the info after the test runs") {
        val msg = "hi there, dude"
        val testName = "test name"
        class MySpec extends Spec {
          it(testName) {}
          info(msg)
        }
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          runCommonInformerTestCode(new MySpec, testName, msg)
        assert(testStartingIndex < testSucceededIndex)
        assert(testSucceededIndex < infoProvidedIndex)
      }
      ignore("should throw an IllegalStateException when info is called by a method invoked after the suite has been executed") {
        class MySpec extends Spec {
          callInfo() // This should work fine
          def callInfo() {
            info("howdy")
          }
          it("howdy also") {
            callInfo() // This should work fine
          }
        }
        val spec = new MySpec
        val myRep = new EventRecordingReporter
        spec.run(None, myRep, new Stopper {}, Set(), Set(), Map(), None, new Tracker)
        intercept[IllegalStateException] {
          spec.callInfo()
        }
      }
    }
  }
}


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
package org.scalatest.junit {

  package nestedHelpers {

    import _root_.org.junit.Test
    import _root_.org.junit.Ignore

    class HappySuite extends JUnitSuite {

      @Test def verifySomething() = () // Don't do nothin'
    }

    class BitterSuite extends JUnitSuite {

      @Test def verifySomething() {
        assert(1 === 2) // This will fail
      }
    }

    class IgnoredSuite extends JUnitSuite {

      @Ignore @Test def verifySomething() {
        assert(1 === 2) // This would fail if it were not ignored
      }
    }

    // Used to make sure testStarting gets invoked twice
    class ManySuite extends JUnitSuite {

      @Test def verifySomething() = ()
      @Test def verifySomethingElse() = ()
    }
  }

  import nestedHelpers._

  class JUnitSuiteSuite extends FunSuite {

    class MyReporter extends Reporter {

      var runStartingCount = 0
      var testCountPassedToRunStarting = -1
      override def runStarting(testCount: Int) {
        testCountPassedToRunStarting = testCount
        runStartingCount += 1
      }

      var testStartingCount = 0
      var testStartingReport: Option[Report] = None
      override def testStarting(report: Report) {
        testStartingReport = Some(report)
        testStartingCount += 1
      }

      var testSucceededCount = 0
      var testSucceededReport: Option[Report] = None
      override def testSucceeded(report: Report) {
        testSucceededReport = Some(report)
        testSucceededCount += 1
      }

      var testFailedReport: Option[Report] = None
      override def testFailed(report: Report) {
        testFailedReport = Some(report)
      }

      var testIgnoredReport: Option[Report] = None
      override def testIgnored(report: Report) {
        testIgnoredReport = Some(report)
      }
    }

    test("A JUnitSuite with a JUnit 4 Test annotation will cause testStarting to be invoked") {

      val happy = new HappySuite
      val repA = new MyReporter
      happy.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
      assert(repA.testStartingReport.isDefined)
      assert(repA.testStartingReport.get.name === "verifySomething(org.scalatest.junit.nestedHelpers.HappySuite)")
    }

    test("A JUnitSuite with a JUnit 4 Test annotation will cause testSucceeded to be invoked") {

      val happy = new HappySuite
      val repA = new MyReporter
      happy.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
      assert(repA.testSucceededReport.isDefined)
      assert(repA.testSucceededReport.get.name === "verifySomething(org.scalatest.junit.nestedHelpers.HappySuite)")
    }

    test("A JUnitSuite with a JUnit 4 Test annotation on a bad test will cause testFailed to be invoked") {

      val bitter = new BitterSuite
      val repA = new MyReporter
      bitter.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
      assert(repA.testFailedReport.isDefined)
      assert(repA.testFailedReport.get.name === "verifySomething(org.scalatest.junit.nestedHelpers.BitterSuite)")
    }

    test("A JUnitSuite with JUnit 4 Ignore and Test annotations will cause testIgnored to be invoked") {

      val ignored = new IgnoredSuite
      val repA = new MyReporter
      ignored.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
      assert(repA.testIgnoredReport.isDefined)
      assert(repA.testIgnoredReport.get.name === "verifySomething(org.scalatest.junit.nestedHelpers.IgnoredSuite)")
    }

    test("A JUnitSuite with two JUnit 4 Test annotations will cause testStarting and testSucceeded to be invoked twice each") {

      val many = new ManySuite
      val repA = new MyReporter
      many.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)

      assert(repA.testStartingReport.isDefined)
      assert(repA.testStartingReport.get.name === "verifySomethingElse(org.scalatest.junit.nestedHelpers.ManySuite)")
      assert(repA.testStartingCount === 2)

      assert(repA.testSucceededReport.isDefined)
      assert(repA.testSucceededReport.get.name === "verifySomethingElse(org.scalatest.junit.nestedHelpers.ManySuite)")
      assert(repA.testSucceededCount === 2)
    }

    test("A JUnitSuite with a JUnit 4 Test annotation will cause runStarting to be invoked") {

      val happy = new HappySuite
      val repA = new MyReporter
      happy.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
      assert(repA.runStartingCount === 1)
      assert(repA.testCountPassedToRunStarting === 1)
    }
  }
}

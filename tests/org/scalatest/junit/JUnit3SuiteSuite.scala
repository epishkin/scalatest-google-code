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

  import org.scalatest.events.Event

  // Put fixture Suites in a subpackage, so they won't be discovered by
  // -m org.scalatest.junit when running the test target for this project.
  package helpers {

    import _root_.org.junit.Test
    import _root_.org.junit.Ignore

    class Happy3Suite extends JUnit3Suite {

      def testSomething() = () // Don't do nothin'
    }

    class Bitter3Suite extends JUnit3Suite {

      def testSomething() {
        assert(1 === 2) // This will fail
      }
    }

    // Used to make sure testStarting gets invoked twice
    class Many3Suite extends JUnit3Suite {

      def testSomething() = ()
      def testSomethingElse() = ()
    }
  }

  import helpers._

  class JUnit3SuiteSuite extends FunSuite {

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

      var runCompletedCount = 0
      override def runCompleted() {
        runCompletedCount += 1
      }
    }

    test("A JUnit3Suite with a JUnit 3 test method will cause testStarting to be invoked") {

      val happy = new Happy3Suite
      val repA = new MyReporter
      happy.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
      assert(repA.testStartingReport.isDefined)
      assert(repA.testStartingReport.get.name === "testSomething(org.scalatest.junit.helpers.Happy3Suite)")
    }

    test("A JUnit3Suite with a JUnit 3 test method will cause testSucceeded to be invoked") {

      val happy = new Happy3Suite
      val repA = new MyReporter
      happy.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
      assert(repA.testSucceededReport.isDefined)
      assert(repA.testSucceededReport.get.name === "testSomething(org.scalatest.junit.helpers.Happy3Suite)")
    }

    test("A JUnit3Suite with a JUnit 3 test method that's bad will cause testFailed to be invoked") {

      val bitter = new Bitter3Suite
      val repA = new MyReporter
      bitter.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
      assert(repA.testFailedReport.isDefined)
      assert(repA.testFailedReport.get.name === "testSomething(org.scalatest.junit.helpers.Bitter3Suite)")
    }

    test("A JUnit3Suite with two JUnit 3 test methods will cause testStarting and testSucceeded to be invoked twice each") {

      val many = new Many3Suite
      val repA = new MyReporter
      many.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)

      assert(repA.testStartingReport.isDefined)
      assert(repA.testStartingReport.get.name === "testSomethingElse(org.scalatest.junit.helpers.Many3Suite)")
      assert(repA.testStartingCount === 2)

      assert(repA.testSucceededReport.isDefined)
      assert(repA.testSucceededReport.get.name === "testSomethingElse(org.scalatest.junit.helpers.Many3Suite)")
      assert(repA.testSucceededCount === 2)
    }

    test("A JUnit3Suite with a JUnit 3 test method will cause runStarting to be invoked") {

      val happy = new Happy3Suite
      val repA = new MyReporter
      happy.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
      assert(repA.runStartingCount === 1)
      assert(repA.testCountPassedToRunStarting === 1)
    }

    test("A JUnit3Suite with a JUnit 3 test method will cause runCompleted to be invoked") {

      val happy = new Happy3Suite
      val repA = new MyReporter
      happy.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
      assert(repA.runCompletedCount === 1)
    }
  }
}
/*
package org.scalatest.junit {

import org.scalatest.jmock._
import junit.test._

class JUnit3SuiteSuite extends SMockFunSuite  with SuiteExpectations {

     mockTest( "Reporter Should Be Notified When Test Passes" ){
       
       val reporter = mock[Reporter]

       expecting{
         singleTestToPass( reporter )
       }
       
       when{
         new SuccessSuite().runJUnit(reporter)
       }
     }
  
     mockTest( "Reporter Should Be Notified When Test Fails" ){
       
       val reporter = mock[Reporter]

       expecting { 
         singleTestToFail( reporter )
       }

       when{
         new ErrorSuite().runJUnit(reporter)
       }
     }
  
     
     test( "If a test fails due to an exception, Report should have the exception" ){
       
       val testReporter = new TestReporter

       // when
       new ErrorSuite().runJUnit(testReporter)

       // then
       assert( testReporter.errorMessage === "fail" )
     }
     
     test( "If a test fails due to an assertion failure, Report should have the info" ){
       
       val testReporter = new TestReporter

       // when
       new FailureSuite().runJUnit(testReporter)

       // then
       assert( testReporter.errorMessage === "fail expected:<1> but was:<2>" )
     }
     
     
     mockTest( "Report should be generated for each invocation" ){
       
       val reporter = mock[Reporter]

       expecting( "reporter gets 2 passing reports because there are 2 test methods" ) {
         nTestsToPass( 2, reporter )
       }

       when ( "run the suite with 2 tests" ){
        new SuiteWithTwoTests().runJUnit(reporter)
       }
     }
     
     
   }

   package test{
     
     import _root_.junit.framework.Assert
     
     class SuccessSuite extends JUnit3Suite {
       def testThatPasses = {}
     }
     
     class ErrorSuite extends JUnit3Suite {
       def testThatThrows() { throw new Exception("fail") }
     }
     
     class FailureSuite extends JUnit3Suite {
       def testWithAssertionFailure() { Assert.assertEquals("fail", 1, 2) }
     }

     class SuiteWithTwoTests extends JUnit3Suite {
       def testThatPasses() {}
       def testAnotherTestThatPasses() {}
     }  
   }
}
*/


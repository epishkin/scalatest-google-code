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
package org.scalatest.testng {

  import org.scalatest.TestRerunner
  import org.scalatest.jmock._
  import testng.testpackage._
  import org.jmock.Mockery
  import org.jmock.Expectations
  import org.hamcrest.core.IsAnything
  import org.scalatest.events._

  class TestNGSuiteSuite extends FunSuite with SuiteExpectations {

    test("Reporter should be notified when test passes") {
 
      val context = new Mockery
      val reporter = context.mock(classOf[Reporter])

      context.checking(
        new Expectations() {
          expectSingleTestToPass(this, reporter)
        }
      )
      
      (new SuccessTestNGSuite()).runTestNG(reporter, new Tracker)

      context.assertIsSatisfied()
    }

    test("Reporter should be notified when test fails") {
   
      val context = new Mockery
      val reporter = context.mock(classOf[Reporter])

      context.checking(
        new Expectations() {
          expectSingleTestToFail(this, reporter)
        }
      )

      (new FailureTestNGSuite()).runTestNG(reporter, new Tracker)

      context.assertIsSatisfied()
    }

    test("If a test fails due to an exception, Report should have the exception") {
      
      val testReporter = new TestReporter

      // when
      (new FailureTestNGSuite()).runTestNG(testReporter, new Tracker)

      // then
      assert(testReporter.errorMessage === "fail") // detail message in exception thrown by FailureTestNGSuite
    }

    test("Report should be generated for each invocation") {
      
      val context = new Mockery
      val reporter = context.mock(classOf[Reporter])

      // expect reporter gets 10 passing reports because invocationCount = 10
      context.checking(
        new Expectations() {
          expectNTestsToPass(this, 10, reporter)
        }
      )

      // when runnning the suite with method that has invocationCount = 10") {
      (new TestNGSuiteWithInvocationCount()).runTestNG(reporter, new Tracker)

      context.assertIsSatisfied()
    }

    test("Reporter should be notified when test is skipped") {

      val context = new Mockery
      val reporter = context.mock(classOf[Reporter])

      // expect a single test should fail, followed by a single test being skipped
      context.checking(
        new Expectations() {
          one(reporter).apply(`with`(new IsAnything[SuiteStarting]))
          one(reporter).apply(`with`(new IsAnything[TestStarting]))
          one(reporter).testFailed(`with`(new IsAnything[Report]))
          one(reporter).testIgnored(`with`(new IsAnything[Report]))
          one(reporter).apply(`with`(new IsAnything[SuiteCompleted]))
        }
      )

      // when runnning the suite with a test that should fail and a test that should be skipped
      (new SuiteWithSkippedTest()).runTestNG(reporter, new Tracker)

      context.assertIsSatisfied()
    }
    
    test("Only the correct method should be run when specifying a single method to run") {
      
      val context = new Mockery
      val reporter = context.mock(classOf[Reporter])

      context.checking(
        new Expectations() {
          expectSingleTestToPass(this, reporter)
        }
      )
      
      (new SuiteWithTwoTests()).runTestNG("testThatPasses", reporter, new Tracker)

      context.assertIsSatisfied()
    }

    test("Report for failing tests should include rerunner") {
      
      val testReporter = new TestReporter

      // when - run the failing suite
      new FailureTestNGSuite().runTestNG(testReporter, new Tracker)

      // then get rerunnable from report 
      val rerunner = testReporter.report.rerunnable.get.asInstanceOf[TestRerunner];
      // TODO we need a better assertion here
    }

    
    test("Report for passing tests should include rerunner") {
      
      val testReporter = new TestReporter

      // when - run the passing suite
      new SuccessTestNGSuite().runTestNG(testReporter, new Tracker)

      // then get rerunnable from report 
      val rerunner = testReporter.report.rerunnable.get.asInstanceOf[TestRerunner];
      // TODO we need a better assertion here
    }
    
    
    test("infoProvided should be available for BeforeMethod/Class/Suite annotations") {
      // this needs to be written after i figure out the mock integration
    }     
    
    test("infoProvided should be available for AfterMethod/Class/Suite annotations") {
      // this needs to be written after i figure out the mock integration
    }     
  }

  package testpackage {
    
    import org.testng.annotations._
    
    class FailureTestNGSuite extends TestNGSuite {
      @Test def testThatFails() { throw new Exception("fail") }
    }
    
    class SuccessTestNGSuite extends TestNGSuite {
      @Test def testThatPasses() {}
    }
    
    class TestNGSuiteWithInvocationCount extends TestNGSuite {
      @Test{val invocationCount = 10} def testThatPassesTenTimes() {}
    }
    
    class SuiteWithSkippedTest extends TestNGSuite {
      @Test{val groups = Array("run")} def dependeeThatFails() { throw new Exception("fail") }
      @Test{val dependsOnGroups = Array("run")} def depender() {}
    } 

    class SuiteWithTwoTests extends TestNGSuite {
      @Test def testThatPasses() {}
      @Test def anotherTestThatPasses() {}
    }      
    
    class SuiteWithBeforeAndAfterAnnotations extends TestNGSuite {
    }
  }
}



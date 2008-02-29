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
   import org.scalatest.jmock.SMocker
   import org.scalatest.jmock.SMockFunSuite
   import testng.test._

   //execute(None, new StandardOutReporter, new Stopper {}, Set(), Set(IgnoreAnnotation), Map(), None)
   class TestNGSuiteSuite extends SMockFunSuite{

     
     mockTest( "Reporter Should Be Notified When Test Passes" ){
    
       val reporter = mock(classOf[Reporter])

       expecting { 
         one(reporter).testStarting(any(classOf[Report])) 
         one(reporter).testSucceeded(any(classOf[Report])) 
       }

       // when
       new SuccessTestNGSuite().runTestNG(reporter)
     }
  

     mockTest( "Reporter Should Be Notified When Test Fails" ){
    
       val reporter = mock(classOf[Reporter])

       expecting { 
         one(reporter).testStarting(any(classOf[Report])) 
         one(reporter).testFailed(any(classOf[Report])) 
       }

       // when
       new FailureTestNGSuite().runTestNG(reporter)
     }

     
     test( "If a test fails due to an exception, Report should have the exception" ){
       
       val testReporter = new TestReporter

       // when
       new FailureTestNGSuite().runTestNG(testReporter)

       // then
       assert( testReporter.errorMessage === "fail" )
     }
     

     mockTest( "Report should be generated for each invocation" ){
       
       val reporter = mock(classOf[Reporter])

       for( i <- 1 to 10 ){
         expecting { 
           one(reporter).testStarting(any(classOf[Report])) 
           one(reporter).testSucceeded(any(classOf[Report])) 
         }
       }

       // when
       new TestNGSuiteWithInvocationCount().runTestNG(reporter)

     }
     
     
     mockTest( "Report should be notified when test is skipped" ){
       
       val reporter = mock(classOf[Reporter])

       expecting { 
         one(reporter).testStarting(any(classOf[Report])) 
         one(reporter).testFailed(any(classOf[Report])) 
         one(reporter).testIgnored(any(classOf[Report])) 
       }

       // when
       new SuiteWithSkippedTest().runTestNG(reporter)

     }
     

     
     mockTest( "Only the correct method should be run when specifying a single method to run" ){
       
       val reporter = mock(classOf[Reporter])

       expecting { 
         one(reporter).testStarting(any(classOf[Report])) 
         one(reporter).testSucceeded(any(classOf[Report])) 
       }
       
       // when
       new SuiteWithTwoTests().runTestNG("testThatPasses", reporter)

     }
     
     
     test( "Report for failing tests should include rerunner" ){
       
       val testReporter = new TestReporter

       // when - run the failing suite
       new FailureTestNGSuite().runTestNG(testReporter)

       // then get rerunnable from report 
       val rerunner = testReporter.report.rerunnable.get.asInstanceOf[TestRerunner];
       // TODO we need a better assertion here
     }

     
     test( "Report for passing tests should include rerunner" ){
       
       val testReporter = new TestReporter

       // when - run the passing suite
       new SuccessTestNGSuite().runTestNG(testReporter)

       // then get rerunnable from report 
       val rerunner = testReporter.report.rerunnable.get.asInstanceOf[TestRerunner];
       // TODO we need a better assertion here
     }
     
     
     test( "infoProvided should be available for BeforeMethod/Class/Suite annotations" ){
       // this needs to be written after i figure out the mock integration
     }     
     
     test( "infoProvided should be available for AfterMethod/Class/Suite annotations" ){
       // this needs to be written after i figure out the mock integration
     }     

     
   }

   package test{
     
     import org.testng.annotations._
     
     class FailureTestNGSuite extends TestNGSuite {
       @Test def testThatFails() { throw new Exception("fail") }
     }
     
     class SuccessTestNGSuite extends TestNGSuite {
       @Test def testThatPasses() {}
     }
     
     class TestNGSuiteWithInvocationCount extends TestNGSuite {
       @Test{val invocationCount=10} def testThatPassesTenTimes() {}
     }
     
     class SuiteWithSkippedTest extends TestNGSuite {
       @Test{val groups=Array("run")} def dependeeThatFails() { throw new Exception("fail") }
       @Test{val dependsOnGroups=Array("run")} def depender() {}
     } 

     class SuiteWithTwoTests extends TestNGSuite {
       @Test def testThatPasses() {}
       @Test def anotherTestThatPasses() {}
     }      
     
     class SuiteWithBeforeAndAfterAnnotations extends TestNGSuite {
       
     }
     
   }
}



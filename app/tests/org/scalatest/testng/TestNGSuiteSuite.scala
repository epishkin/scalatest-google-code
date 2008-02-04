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

   import org.scalatest.Suite
   import org.scalatest.fun.FunSuite
   import org.testng.annotations.Test
   import testng.test._

   //execute(None, new StandardOutReporter, new Stopper {}, Set(), Set(IgnoreAnnotation), Map(), None)
   class TestNGSuiteSuite extends FunSuite {

     test( "Reporter Should Be Notified When Test Passes" ){
    
       val testReporter = new TestReporter

       // when
       new SuccessTestNGSuite().runTestNG(testReporter)

       // then
       assert( testReporter.successCount === 1 )
     }
  

     test( "Reporter Should Be Notified When Test Fails" ){
    
       val testReporter = new TestReporter

       // when
       new FailureTestNGSuite().runTestNG(testReporter)

       // then
       assert( testReporter.failureCount === 1 )
     }

     
     test( "If a test fails due to an exception, Report should have the exception" ){
       
       val testReporter = new TestReporter

       // when
       new FailureTestNGSuite().runTestNG(testReporter)

       // then
       assert( testReporter.errorMessage === "fail" )
     }
     

     test( "Report should be generated for each invocation" ){
       
       val testReporter = new TestReporter

       // when
       new TestNGSuiteWithInvocationCount().runTestNG(testReporter)

       // then
       assert( testReporter.successCount === 10 )
     }
     
     
     test( "Report should be notified when test is skipped" ){
       
       val testReporter = new TestReporter

       // when
       new SuiteWithSkippedTest().runTestNG(testReporter)

       // then
       assert( testReporter.ignoreCount === 1 )
     }
     

     
     test( "Only the correct method should be run when specifying a single method to run" ){
       
       val testReporter = new TestReporter

       // when
       new SuiteWithTwoTests().runTestNG("testThatPasses", testReporter)

       // then
       assert( testReporter.successCount === 1 )
     }
     
   }

   package test{
     
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
     
   }
}



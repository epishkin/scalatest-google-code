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

   import org.scalatest.jmock._
   import junit.test._

   class JUnitSuiteSuite extends SMockFunSuite  with SuiteExpectations {

     mockTest( "Reporter Should Be Notified When Test Passes" ){
       
       val reporter = mock(classOf[Reporter])

       expecting{
         singleTestToPass( reporter )
       }
       
       when{
         new SuccessSuite().runJUnit(reporter)
       }
     }
  
     mockTest( "Reporter Should Be Notified When Test Fails" ){
       
       val reporter = mock(classOf[Reporter])

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
       
       val reporter = mock(classOf[Reporter])

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



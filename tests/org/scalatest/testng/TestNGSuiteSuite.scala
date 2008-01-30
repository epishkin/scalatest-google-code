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

   //execute(None, new StandardOutReporter, new Stopper {}, Set(), Set(IgnoreAnnotation), Map(), None)
   class TestNGSuiteSuite extends FunSuite {

     test( "Reporter Should Be Notified When Test Passes" ){
    
       val testReporter = new TestReporter

       // when
       new testng.test.SuccessTestNGSuite().runTestNG(testReporter)

       // then
       assert( testReporter.successCount === 1 )
     }
  

     test( "Reporter Should Be Notified When Test Fails" ){
    
       val testReporter = new TestReporter

       // when
       new testng.test.FailureTestNGSuite().runTestNG(testReporter)

       // then
       assert( testReporter.failureCount === 1 )
     }
  
  
  
     /**
      * This class only exists because I cant get jmock to work with Scala. 
      * Other people seem to do it. Frustrating. 
      */
     class TestReporter extends Reporter{
       var successCount = 0;
       override def testSucceeded(report: Report){ successCount = successCount + 1 }
       var failureCount = 0;
       override def testFailed(report: Report){ failureCount = failureCount + 1 }
     }
  
   }

   package test{
     class FailureTestNGSuite extends TestNGSuite {
       @Test def testThatFails() { throw new Exception }
     }
     class SuccessTestNGSuite extends TestNGSuite {
       @Test def testThatPasses() {}
     }
   }
}



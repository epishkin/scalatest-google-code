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
package org.scalatest.jmock

import scala.reflect.Manifest

trait SuiteExpectations extends SMocker{

  def singleTestToPass( reporter: Reporter ) = nTestsToPass( 1, reporter )
  def singleTestToFail( reporter: Reporter ) = nTestsToFail( 1, reporter )
  
  def nTestsToPass( n: int, reporter: Reporter ) = {
    expectNTestsToRun( n, reporter ){ 
      one(reporter).testSucceeded(any[Report]) 
    }
  }
  
  def nTestsToFail( n: int, reporter: Reporter ) = {
    expectNTestsToRun( n, reporter ){ 
      one(reporter).testFailed(any[Report]) 
    }
  }

  def expectNTestsToRun(n: int, reporter: Reporter)(f: => Unit) = {
    one(reporter).suiteStarting(any[Report]) 
    for( i <- 1 to n ){
      one(reporter).testStarting(any[Report]) 
      f
    }
    one(reporter).suiteCompleted(any[Report]) 
  }
  
}

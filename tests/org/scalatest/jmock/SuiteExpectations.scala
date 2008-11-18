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

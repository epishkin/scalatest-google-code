package org.scalatest.testng;

import org.scalatest.jmock.SMocker

trait TestNGSuiteExpectations extends SMocker{

  def singleTestToPass( reporter: Reporter ) = nTestsToPass( 1, reporter )
  def singleTestToFail( reporter: Reporter ) = nTestsToFail( 1, reporter )
  
  def nTestsToPass( n: int, reporter: Reporter ) = {
    expectNTestsToRun( n, reporter ){ 
      one(reporter).testSucceeded(any(classOf[Report])) 
    }
  }
  
  def nTestsToFail( n: int, reporter: Reporter ) = {
    expectNTestsToRun( n, reporter ){ 
      one(reporter).testFailed(any(classOf[Report])) 
    }
  }

  def expectNTestsToRun(n: int, reporter: Reporter)(f: => Unit) = {
    one(reporter).suiteStarting(any(classOf[Report])) 
    for( i <- 1 to n ){
      one(reporter).testStarting(any(classOf[Report])) 
      f
    }
    one(reporter).suiteCompleted(any(classOf[Report])) 
  }
  
}

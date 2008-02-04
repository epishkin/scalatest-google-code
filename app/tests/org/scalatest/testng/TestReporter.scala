package org.scalatest.testng;


/**
 * This class only exists because I cant get jmock to work with Scala. 
 * Other people seem to do it. Frustrating. 
 */
class TestReporter extends Reporter{

  var report: Report = null;
  var successCount = 0;
  var failureCount = 0;
  
  var ignoreReport: Report = null;
  var ignoreCount = 0;
  
  override def testSucceeded(report: Report){ 
    successCount = successCount + 1 
    this.report = report;
  }
  
  override def testFailed(report: Report){ 
    failureCount = failureCount + 1 
    this.report = report;
  }

  override def testIgnored(report: Report){ 
    ignoreCount = ignoreCount + 1 
    ignoreReport = report;
  }
  
  def errorMessage = report.throwable.get.getMessage
  
}

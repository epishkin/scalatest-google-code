package org.scalatest.testng;

import org.scalatest.Suite
import org.testng.TestNG
import org.testng.TestListenerAdapter
import org.testng.ITestResult

trait TestNGSuite extends Suite{

  override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
      properties: Map[String, Any], distributor: Option[Distributor]) {
    
    runTestNG(reporter);
    //super.execute(testName, reporter, stopper, includes, excludes, properties, distributor)
  }
  
  private class MyTestListenerAdapter( reporter: Reporter ) extends TestListenerAdapter{
    val className = TestNGSuite.this.getClass.getName
    override def onTestStart(result: ITestResult) = reporter.testStarting( buildReport( result, None ))
    override def onTestSuccess(itr: ITestResult) = {
      val report = buildReport( itr, None )
      println( "report = " + report )
      reporter.testSucceeded( report )
    }
    override def onTestFailure(itr: ITestResult) = reporter.testFailed( buildReport( itr, Some(itr.getThrowable)))
    private def buildReport( itr: ITestResult, t: Option[Throwable] ): Report = new Report(className + "." + itr.getName, className )
  }
  
  private[testng] def runTestNG(reporter: Reporter) : TestListenerAdapter = {
    val tla = new MyTestListenerAdapter(reporter);
    val testng = new TestNG();
    testng.setTestClasses(Array(this.getClass));
    testng.addListener(tla);
    testng.run();
    tla
  }
  
}

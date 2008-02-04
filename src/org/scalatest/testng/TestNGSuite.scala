package org.scalatest.testng;

import org.scalatest.Suite
import org.scalatest.Report
import org.scalatest.TestRerunner

import org.testng.internal.annotations.ITest
import org.testng.internal.annotations.IAnnotationTransformer
import org.testng.TestNG
import org.testng.ITestResult
import org.testng.TestListenerAdapter
import java.lang.reflect.Method
import java.lang.reflect.Constructor

trait TestNGSuite extends Suite{

  override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], 
      excludes: Set[String], properties: Map[String, Any], distributor: Option[Distributor]) {
    
    runTestNG(testName, reporter, includes, excludes);
    //super.execute(testName, reporter, stopper, includes, excludes, properties, distributor)
  }
  
  private[testng] def runTestNG(reporter: Reporter) : TestListenerAdapter = {
    runTestNG( None, reporter, Set(), Set() )
  }
 
  private[testng] def runTestNG(testName: String, reporter: Reporter) : TestListenerAdapter = {
    runTestNG( Some(testName), reporter, Set(), Set() )
  }
  
  
  private[testng] def runTestNG(testName: Option[String], reporter: Reporter, groupsToInclude: Set[String], 
      groupsToExclude: Set[String]) : TestListenerAdapter = {
    
    val testng = new TestNG()
    testng.setTestClasses(Array(this.getClass))
    
    
    if( testName.isDefined ){
      handleGroupsForRunningSingleMethod( testName.get, testng );
    }
    else{
      testng.setGroups(groupsToInclude.foldLeft(""){_+","+_})
      testng.setExcludedGroups(groupsToExclude.foldLeft(""){_+","+_})
    }

    
    val tla = new MyTestListenerAdapter(reporter)
    testng.addListener(tla)
    testng.run()

    tla
  }
  
  
  def handleGroupsForRunningSingleMethod( testName: String, testng: TestNG ) = {
    
    class MyTransformer extends IAnnotationTransformer {
      override def transform( annotation: ITest, testClass: Class, testConstructor: Constructor, testMethod: Method){
        if (testName.equals(testMethod.getName)) {
          annotation.setGroups(Array("org.scalatest.testng.singlemethodrun.methodname"))  
        }
      }
    }
    testng.setGroups("org.scalatest.testng.singlemethodrun.methodname")
    testng.setAnnotationTransformer(new MyTransformer())
  }
  

  private class MyTestListenerAdapter( reporter: Reporter ) extends TestListenerAdapter{
    
    val className = TestNGSuite.this.getClass.getName
    
    override def onTestStart(result: ITestResult) = {
      reporter.testStarting( buildReport( result, None ))
    }
    
    override def onTestSuccess(itr: ITestResult) = {
      val report = buildReport( itr, None )
      reporter.testSucceeded( report )
    }
    
    override def onTestFailure(itr: ITestResult) = {
      reporter.testFailed( buildReport( itr, Some(itr.getThrowable)))
    }
    
    override def onTestSkipped(itr: ITestResult) = {
      reporter.testIgnored( buildReport( itr, Some(itr.getThrowable)))
    }
    
    private def buildReport( itr: ITestResult, t: Option[Throwable] ): Report = {
      val testName = itr.getName
      new Report(testName, className, t, Some(new TestRerunner(className, testName)) )
    }
  }
  
  
  
  /**
     TODO
    (12:02:27 AM) bvenners: onTestFailedButWithinSuccessPercentage(ITestResult tr) 
    (12:02:34 AM) bvenners: maybe a testSucceeded with some extra info in the report
    (12:02:49 AM) bvenners: onStart and onFinish are starting and finishing what, a run?
    (12:02:57 AM) bvenners: if so then runStarting and runCompleted
    (12:03:14 AM) bvenners: onConfiguration/Success/Failure we don't have, so put that in an infoProvided
    (12:03:29 AM) joshcoughx: ok
    (12:03:50 AM) bvenners: i
    (12:03:56 AM) bvenners: i'm not sure what a config success failure is
    (12:04:18 AM) joshcoughx: me either. 
    (12:04:22 AM) joshcoughx: and no javadoc.
    (12:05:57 AM) joshcoughx: i can ask him though
    (12:06:11 AM) joshcoughx: and i can always look at the code
    (12:06:14 AM) joshcoughx: i have it 
    (12:06:51 AM) bvenners: whatever it is i'm pretty sure it will map to infoProvided in ScalaTest
    **/    
    
      
   /**
    val xmlSuite = new XmlSuite()
    val xmlClass = new XmlClass(this.getClass.getName)
    //xmlClass.setIncludedMethods(java.util.Collections.singletonList("testWithAssertFail"))
    val xmlTest = new XmlTest(xmlSuite)
    xmlTest.setXmlClasses(java.util.Collections.singletonList(xmlClass))
    println(xmlSuite.toXml())
    testng.setXmlSuites(java.util.Collections.singletonList(xmlSuite))
    **/
  
}

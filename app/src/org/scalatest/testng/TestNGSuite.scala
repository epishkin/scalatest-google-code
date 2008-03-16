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
package org.scalatest.testng;

import org.scalatest.Suite
import org.scalatest.Report
import org.scalatest.TestRerunner

import org.testng.TestNG
import org.testng.TestListenerAdapter

/**
 * <p>
 * Extending <code>TestNGSuite</code> enables you to write TestNG tests in Scala, continue to run them in your 
 * standard TestNG runner, <em>and</em> run them in ScalaTest runners.
 * </p>
 *
 * <p>
 * Doing so is very straightforward - simply write a Scala class that extends <code>TestNGSuite</code> and 
 * add standard TestNG annotations to your methods. Here is a simple example that demonstrates the use of 
 * a number of TestNG features. 
 * </p>
 * 
 * <pre>
 * class ExampleTestNGSuite extends TestNGSuite{
 *  
 *   @Test{val invocationCount=10} def thisTestRunsTenTimes = {}
 * 
 *   @Test{val groups=Array("runMe")} 
 *   def testWithException(){ 
 *     throw new Exception("exception!!!") 
 *   }
 * }
 * </pre>
 *
 * <p>
 * Notice that other than a few minor details (or if you are wearing sunglasses), you could almost trick 
 * yourself into thinking this was Java code. That is the point. The idea is to make the transition to 
 * Scala as simple as possible. Starting in this comfort zone should make it easier to transition into 
 * some of ScalaTest's more advanced features.
 * </p>
 *
 * @author Josh Cough
 */
trait TestNGSuite extends Suite{

  /**
   * Runs TestNG. <br>
   * 
   * @param   testName   If present (Some), then only the method with the supplied name is executed and groups will be ignored.
   * @param   reporter	 The reporter to be notified of test events (success, failure, etc).
   * @param   groupsToInclude	Contains the names of groups to run. Only tests in these groups will be executed.
   * @param   groupsToExclude	Tests in groups in this Set will not be executed.
   *
   * @param   stopper    	Ignored. TestNG doesn't have a stopping mechanism.
   * @param   properties	Currently ignored. see note above...should we be ignoring these?
   * @param   distributor	Ignored. TestNG handles its own concurrency. We consciously chose to leave that as it was. 
   * <br><br>
   * TODO: Currently doing nothing with properties. Should we be?<br>
   * NOTE: TestNG doesn't have a stopping mechanism. Stopper is ignored.<br>
   * NOTE: TestNG handles its own concurrency. Distributor is ignored.<br>
   */
  override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], 
      excludes: Set[String], properties: Map[String, Any], distributor: Option[Distributor]) {
    
    runTestNG(testName, reporter, includes, excludes);
  }
  
  /**
   * Runs TestNG with no test name, no groups. All tests in the class will be executed.
   * @param   reporter   the reporter to be notified of test events (success, failure, etc)
   */
  private[testng] def runTestNG(reporter: Reporter) : TestListenerAdapter = {
    runTestNG( None, reporter, Set(), Set() )
  }
 
  /**
   * Runs TestNG, running only the test method with the given name. 
   * @param   testName   the name of the method to run
   * @param   reporter   the reporter to be notified of test events (success, failure, etc)
   */
  private[testng] def runTestNG(testName: String, reporter: Reporter) : TestListenerAdapter = {
    runTestNG( Some(testName), reporter, Set(), Set() )
  }
  
  /**
   * Runs TestNG. The meat and potatoes. 
   *
   * @param   testName   if present (Some), then only the method with the supplied name is executed and groups will be ignored
   * @param   reporter   the reporter to be notified of test events (success, failure, etc)
   * @param   groupsToInclude    contains the names of groups to run. only tests in these groups will be executed
   * @param   groupsToExclude    tests in groups in this Set will not be executed
   */  
  private[testng] def runTestNG(testName: Option[String], reporter: Reporter, groupsToInclude: Set[String], 
      groupsToExclude: Set[String]) : TestListenerAdapter = {
    
    val testng = new TestNG()
    
    // only run the test methods in this class
    testng.setTestClasses(Array(this.getClass))
    
    // if testName is supplied, ignore groups.
    testName match {
      case Some(tn) => setupTestNGToRunSingleMethod(tn, testng)
      case None => handleGroups( groupsToInclude, groupsToExclude, testng )
    }

    this.run(testng, reporter)
    
  }
  
  /**
   * Runs the TestNG object which calls back to the given Reporter.
   */
  private[testng] def run( testng: TestNG, reporter: Reporter ): TestListenerAdapter = {
    
    // setup the callback mechanism
    val tla = new MyTestListenerAdapter(reporter)
    testng.addListener(tla)
    
    //finally, run TestNG
    testng.run()
    
    tla
  }
  
  /**
   * Tells TestNG which groups to include and exclude, which is directly a one-to-one mapping.
   */
  private[testng] def handleGroups( groupsToInclude: Set[String], groupsToExclude: Set[String], testng: TestNG){
    testng.setGroups(groupsToInclude.mkString(","))
    testng.setExcludedGroups(groupsToExclude.mkString(","))
  }
  
  /**
   * This method ensures that TestNG will only run the test method whos name matches testName.
   * 
   * The approach is a bit odd however because TestNG doesn't have an easy API for
   * running a single method. To get around that we chose to use an AnnotationTransformer 
   * to add a secret group to the test method's annotation. We then set up TestNG to run only that group.
   *
   * NOTE: There was another option - we could TestNG's XmlSuites to specify which method to run.
   * This approach was about as much work, offered no clear benefits, and no additional problems either.
   * 
   * @param    testName    the name of the test method to be executed
   */
  private def setupTestNGToRunSingleMethod( testName: String, testng: TestNG ) = {
    
    import org.testng.internal.annotations.IAnnotationTransformer
    import org.testng.internal.annotations.ITest
    import java.lang.reflect.Method
    import java.lang.reflect.Constructor
    
    class MyTransformer extends IAnnotationTransformer {
      override def transform( annotation: ITest, testClass: java.lang.Class[_], testConstructor: Constructor[_], testMethod: Method){
        if (testName.equals(testMethod.getName)) {
          annotation.setGroups(Array("org.scalatest.testng.singlemethodrun.methodname"))  
        }
      }
    }
    testng.setGroups("org.scalatest.testng.singlemethodrun.methodname")
    testng.setAnnotationTransformer(new MyTransformer())
  }
  
  /**
   * This class hooks TestNG's callback mechanism (TestListenerAdapter) to ScalaTest's
   * reporting mechanism. TestNG has many different callback points which are a near one-to-one
   * mapping with ScalaTest. At each callback point, this class simply creates ScalaTest 
   * reports and calls the appropriate method on the Reporter.
   * 
   * TODO: 
   * (12:02:27 AM) bvenners: onTestFailedButWithinSuccessPercentage(ITestResult tr) 
   * (12:02:34 AM) bvenners: maybe a testSucceeded with some extra info in the report
   */
  private[testng] class MyTestListenerAdapter( reporter: Reporter ) extends TestListenerAdapter{
    
    import org.testng.ITestContext
    import org.testng.ITestResult
    
    private val className = TestNGSuite.this.getClass.getName

    /**
     * This method is called when TestNG starts, and maps to ScalaTest's suiteStarting. 
     * @TODO TestNG doesn't seem to know how many tests are going to be executed.
     * We are currently telling ScalaTest that 0 tests are about to be run. Investigate
     * and/or chat with Cedric to determine if its possible to get this number from TestNG.
     */
    override def onStart(itc: ITestContext) = {
      reporter.suiteStarting( new Report(itc.getName, className, None, None ) )
    }

    /**
     * TestNG's onFinish maps cleanly to suiteCompleted.
     * TODO: TestNG does have some extra info here. One thing we could do is map the info
     * in the ITestContext object into ScalaTest Reports and call reporter.infoProvided.
     */
    override def onFinish(itc: ITestContext) = {
       reporter.suiteCompleted( new Report(itc.getName, className, None, None ) )
    }
    
    /**
     * TestNG's onTestStart maps cleanly to testStarting. Simply build a report 
     * and pass it to the Reporter.
     */
    override def onTestStart(result: ITestResult) = {
      reporter.testStarting( buildReport( result, None ) )
    }
    
    /**
     * TestNG's onTestSuccess maps cleanly to testSucceeded. Again, simply build
     * a report and pass it to the Reporter.
     */
    override def onTestSuccess(itr: ITestResult) = {
      reporter.testSucceeded( buildReport( itr, None ) )
    }

    /**
     * TestNG's onTestSkipped maps cleanly to testIgnored. Again, simply build
     * a report and pass it to the Reporter.
     */
    override def onTestSkipped(itr: ITestResult) = {
      reporter.testIgnored( buildReport( itr, None ) )
    }

    /**
     * TestNG's onTestFailure maps cleanly to testFailed. This differs slighly from
     * the other calls however. An expection is available on the ITestResult,
     * and it gets put into the Report object that is given to the Reporter.
     */
    override def onTestFailure(itr: ITestResult) = {
      reporter.testFailed( buildReport( itr, Some(itr.getThrowable)) )
    }

    /**
     * A TestNG setup method resulted in an exception, and a test method will later fail to run. 
     * This TestNG callback method has the exception that caused the problem, as well
     * as the name of the method that failed. Create a Report with the method name and the
     * exception and call reporter.testFailed. 
     * 
     * Calling testFailed isn't really a clean one-to-one mapping between TestNG and ScalaTest
     * and somewhat exposes a ScalaTest implementation detail. By default, ScalaTest only shows
     * failing tests in red in the UI, and does not show additional information such as testIgnored,
     * and infoProvided. Had I chose to use either of those calls here, the user wouldn't 
     * immediately know what the root cause of the problem is. They might not even know there
     * was a problem at all. 
     * 
     * In my opinion, we need an additional failure indicator available on the Reporter which
     * also shows up in red on the UI. Something like, "reporter.failure", or "reporter.setupFailure".
     * Something that differentiates between test's failing, and other types of failures such as a 
     * setup method.
     */
    override def onConfigurationFailure(itr: ITestResult) = {
      reporter.testFailed( new Report(itr.getName, className, Some(itr.getThrowable), None) )
    }

    /**
     * TestNG's onConfigurationSuccess doesn't have a clean mapping in ScalaTest.
     * Simply create a Report and call infoProvided on the Reporter. This works well
     * because there may be a large number of setup methods and infoProvided doesn't 
     * show up in your face on the UI, and so doesn't clutter the UI. 
     */
    override def onConfigurationSuccess(itr: ITestResult) = {
      reporter.infoProvided( new Report(itr.getName, className) )
    }
    
    /**
     * Constructs the report ojbect.
     */
    private def buildReport( itr: ITestResult, t: Option[Throwable] ): Report = {
      new Report(itr.getName, className, t, Some(new TestRerunner(className, itr.getName)) )
    }
  }
  
  /**
     TODO
    (12:02:27 AM) bvenners: onTestFailedButWithinSuccessPercentage(ITestResult tr)
    (12:02:34 AM) bvenners: maybe a testSucceeded with some extra info in the report
  **/    
}

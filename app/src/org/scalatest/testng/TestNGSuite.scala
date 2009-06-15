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
package org.scalatest.testng

import org.scalatest.Suite
import org.scalatest.Report
import org.scalatest.TestRerunner
import org.scalatest.events._

import org.testng.TestNG
import org.testng.TestListenerAdapter

/**
 * A suite of tests that can be run with either TestNG or ScalaTest. This trait allows you to mark any
 * method as a test using TestNG's <code>@Test</code> annotation, and supports all other TestNG annotations.
 * Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest.testng.TestNGSuite
 * import org.testng.annotations.Test
 * import org.testng.annotations.Configuration
 * import scala.collection.mutable.ListBuffer
 * 
 * class MySuite extends TestNGSuite {
 * 
 *   var sb: StringBuilder = _
 *   var lb: ListBuffer[String] = _
 * 
 *   @Configuration { val beforeTestMethod = true }
 *   def setUpFixture() {
 *     sb = new StringBuilder("ScalaTest is ")
 *     lb = new ListBuffer[String]
 *   }
 * 
 *   @Test { val invocationCount = 3 }
 *   def easyTest() {
 *     sb.append("easy!")
 *     assert(sb.toString === "ScalaTest is easy!")
 *     assert(lb.isEmpty)
 *     lb += "sweet"
 *   }
 * 
 *   @Test { val groups = Array("com.mycompany.groups.SlowTest") }
 *   def funTest() {
 *     sb.append("fun!")
 *     assert(sb.toString === "ScalaTest is fun!")
 *     assert(lb.isEmpty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * To execute <code>TestNGSuite</code>s with ScalaTest's <code>Runner</code>, you must include TestNG's jar file on the class path or runpath.
 * This version of <code>TestNGSuite</code> was tested with TestNG version 5.7.
 * </p>
 *
 * @author Josh Cough
 */
trait TestNGSuite extends Suite { thisSuite =>

  /**
   * Execute this <code>TestNGSuite</code>.
   * 
   * @param testName an optional name of one test to execute. If <code>None</code>, this class will execute all relevant tests.
   *                 I.e., <code>None</code> acts like a wildcard that means execute all relevant tests in this <code>TestNGSuite</code>.
   * @param   reporter	 The reporter to be notified of test events (success, failure, etc).
   * @param   groupsToInclude	Contains the names of groups to run. Only tests in these groups will be executed.
   * @param   groupsToExclude	Tests in groups in this Set will not be executed.
   *
   * @param stopper the <code>Stopper</code> may be used to request an early termination of a suite of tests. However, because TestNG does
   *                not support the notion of aborting a run early, this class ignores this parameter.
   * @param   properties         a <code>Map</code> of properties that can be used by the executing <code>Suite</code> of tests. This class
   *                      does not use this parameter.
   * @param distributor an optional <code>Distributor</code>, into which nested <code>Suite</code>s could be put to be executed
   *              by another entity, such as concurrently by a pool of threads. If <code>None</code>, nested <code>Suite</code>s will be executed sequentially.
   *              Because TestNG handles its own concurrency, this class ignores this parameter.
   * <br><br>
   */
  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, groupsToInclude: Set[String],
      groupsToExclude: Set[String], properties: Map[String, Any], distributor: Option[Distributor], firstOrdinal: Ordinal): Ordinal = {
    
    runTestNG(testName, reporter, groupsToInclude, groupsToExclude, firstOrdinal)
  }
  
  /**
   * Runs TestNG with no test name, no groups. All tests in the class will be executed.
   * @param   reporter   the reporter to be notified of test events (success, failure, etc)
   */
  private[testng] def runTestNG(reporter: Reporter, firstOrdinal: Ordinal): Ordinal = {
    runTestNG(None, reporter, Set(), Set(), firstOrdinal)
  }
 
  /**
   * Runs TestNG, running only the test method with the given name. 
   * @param   testName   the name of the method to run
   * @param   reporter   the reporter to be notified of test events (success, failure, etc)
   */
  private[testng] def runTestNG(testName: String, reporter: Reporter, firstOrdinal: Ordinal): Ordinal = {
    runTestNG(Some(testName), reporter, Set(), Set(), firstOrdinal)
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
      groupsToExclude: Set[String], firstOrdinal: Ordinal): Ordinal = {
    
    val testng = new TestNG()
    
    // only run the test methods in this class
    testng.setTestClasses(Array(this.getClass))
    
    // if testName is supplied, ignore groups.
    testName match {
      case Some(tn) => setupTestNGToRunSingleMethod(tn, testng)
      case None => handleGroups(groupsToInclude, groupsToExclude, testng)
    }

    this.run(testng, reporter, firstOrdinal)
  }
  
  /**
   * Runs the TestNG object which calls back to the given Reporter.
   */
  private[testng] def run(testng: TestNG, reporter: Reporter, firstOrdinal: Ordinal): Ordinal = {
    
    // setup the callback mechanism
    val tla = new MyTestListenerAdapter(reporter, firstOrdinal)
    testng.addListener(tla)
    
    // finally, run TestNG
    testng.run()
    
    tla.ordinal
  }
  
  /**
   * Tells TestNG which groups to include and exclude, which is directly a one-to-one mapping.
   */
  private[testng] def handleGroups(groupsToInclude: Set[String], groupsToExclude: Set[String], testng: TestNG) {
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
  private def setupTestNGToRunSingleMethod(testName: String, testng: TestNG) = {
    
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
  private[testng] class MyTestListenerAdapter(reporter: Reporter, firstOrdinal: Ordinal) extends TestListenerAdapter {
    
    var ordinal = firstOrdinal // TODO: Put this in an atomic, because TestNG can go multithreaded I think

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
      reporter(SuiteStarting(ordinal, thisSuite.suiteName, Some(thisSuite.getClass.getName)))
      ordinal = ordinal.next
    }

    /**
     * TestNG's onFinish maps cleanly to suiteCompleted.
     * TODO: TestNG does have some extra info here. One thing we could do is map the info
     * in the ITestContext object into ScalaTest Reports and call reporter.infoProvided.
     */
    override def onFinish(itc: ITestContext) = {
      reporter(SuiteCompleted(ordinal, thisSuite.suiteName, Some(thisSuite.getClass.getName)))
      ordinal = ordinal.next
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
     * setup method. TODO: This is probably a SuiteAborted.
     */
    override def onConfigurationFailure(itr: ITestResult) = {
      //reporter.testFailed(new Report(itr.getName, className, Some(itr.getThrowable), None))
      reporter.testFailed(new Report(itr.getName, className, Some(itr.getThrowable), None))
    }

    /**
     * TestNG's onConfigurationSuccess doesn't have a clean mapping in ScalaTest.
     * Simply create a Report and call infoProvided on the Reporter. This works well
     * because there may be a large number of setup methods and infoProvided doesn't 
     * show up in your face on the UI, and so doesn't clutter the UI. 
     */
    override def onConfigurationSuccess(itr: ITestResult) = { // TODO: Work on this report
      //reporter.infoProvided(new Report(itr.getName, className, Some(suiteName), Some(className), Some(itr.getName)))
      reporter.infoProvided(new Report(itr.getName, className))
    }
    
    /**
     * Constructs the report ojbect.
     */
    private def buildReport( itr: ITestResult, t: Option[Throwable] ): Report = {
      
      val params =
        itr.getParameters match {   
          case Array() => ""
          case _ => "(" + itr.getParameters.mkString(",") + ")"
        }
      
      new Report(itr.getName + params, className, t, Some(new TestRerunner(className, itr.getName)) )
    }
  }
  
  /**
     TODO
    (12:02:27 AM) bvenners: onTestFailedButWithinSuccessPercentage(ITestResult tr)
    (12:02:34 AM) bvenners: maybe a testSucceeded with some extra info in the report
  **/    
}

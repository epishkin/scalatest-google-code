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
package org.scalatest.junit;

import org.scalatest.Suite
import org.scalatest.Report

import _root_.junit.framework._
import _root_.junit.textui._

/**
 * A suite of tests that can be run with either JUnit 3 or ScalaTest. This trait allows you to write JUnit 3 tests
 * with ScalaTest's more concise assertion syntax as well as JUnit's assertions (<code>assertEquals</code>, etc.).
 * You create tests by defining methods that start with <code>test</code>, and can create fixtures with methods
 * named <code>setUp</code> and <code>tearDown</code>. For example:
 *
 * <pre>
 * import org.scalatest.junit.JUnit3Suite
 * import scala.collection.mutable.ListBuffer
 *
 * class TwoSuite extends JUnit3Suite {
 *
 *   var sb: StringBuilder = _
 *   var lb: ListBuffer[String] = _
 *
 *   override def setUp() {
 *     sb = new StringBuilder("ScalaTest is ")
 *     lb = new ListBuffer[String]
 *   }
 *
 *   def testEasy() {
 *     sb.append("easy!")
 *     assert(sb.toString === "ScalaTest is easy!")
 *     assert(lb.isEmpty)
 *     lb += "sweet"
 *   }
 *
 *   def testFun() {
 *     sb.append("fun!")
 *     assert(sb.toString === "ScalaTest is fun!")
 *     assert(lb.isEmpty)
 *   }
 * }
 * </pre>
 * 
 * @author Josh Cough
 */
trait JUnit3Suite extends TestCase with Suite {

  /**
   * Runs JUnit. <br>
   * 
   * @param   testName   If present (Some), then only the method with the supplied name is executed and groups will be ignored.
   * @param   reporter	 The reporter to be notified of test events (success, failure, etc).
   * @param   groupsToInclude	Contains the names of groups to run. Only tests in these groups will be executed.
   * @param   groupsToExclude	Tests in groups in this Set will not be executed.
   *
   * @param   stopper    	Currently Ignored.
   * @param   properties	Currently ignored. see note above...should we be ignoring these?
   * @param   distributor	Currently ignored.
   */
  /**
   * Execute this <code>JUnit3Suite</code>.
   *
   * <p>If <code>testName</code> is <code>None</code>, this trait's implementation of this method
   * calls these two methods on this object in this order:</p>
   *
   * <ol>
   * <li><code>runNestedSuites(wrappedReporter, stopper, includes, excludes, properties, distributor)</code></li>
   * <li><code>runTests(testName, wrappedReporter, stopper, includes, excludes, properties)</code></li>
   * </ol>
   *
   * <p>
   * If <code>testName</code> is <code>Some</code>, then this trait's implementation of this method
   * calls <code>runTests</code>, but does not call <code>runNestedSuites</code>.
   * </p>
   *
   * @param testName an optional name of one test to execute. If <code>None</code>, all tests will be executed.
   *                 I.e., <code>None</code> acts like a wildcard that means execute all tests in this <code>JUnit3Suite</code>.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param includes a <code>Set</code> of <code>String</code> test names to include in the execution of this <code>Suite</code>
   * @param excludes a <code>Set</code> of <code>String</code> test names to exclude in the execution of this <code>Suite</code>
   * @param properties a <code>Map</code> of properties that can be used by the executing <code>Suite</code> of tests.
   * @param distributor an optional <code>Distributor</code>, into which to put nested <code>Suite</code>s to be executed
   *              by another entity, such as concurrently by a pool of threads. If <code>None</code>, nested <code>Suite</code>s will be executed sequentially.
   *         
   *
   * @throws NullPointerException if any passed parameter is <code>null</code>.
   */
  override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], 
      excludes: Set[String], properties: Map[String, Any], distributor: Option[Distributor]) {

    runJUnit(testName, reporter, includes, excludes);
  }

  /**
   * Runs JUnit. The meat and potatoes. 
   *
   * @param   testName   if present (Some), then only the method with the supplied name is executed and groups will be ignored
   * @param   reporter   the reporter to be notified of test events (success, failure, etc)
   * @param   groupsToInclude    contains the names of groups to run. only tests in these groups will be executed
   * @param   groupsToExclude    tests in groups in this Set will not be executed
   */  
  private def runJUnit(testName: Option[String], reporter: Reporter, groupsToInclude: Set[String], groupsToExclude: Set[String]) {
    runJUnit(reporter)
  }   
   
  private def buildReport( testName: String, t: Option[Throwable] ): Report = {
    new Report(testName, this.getClass.getName, t, None )
  }
  
  private[junit] def runJUnit(reporter: Reporter) = {
    reporter.suiteStarting( buildReport( this.getClass.getName, None ) ) 
    testNames.foreach(runSingleTest( _, reporter))
    //new TestSuite(this.getClass).run(new MyTestResult(reporter))
    reporter.suiteCompleted( buildReport( this.getClass.getName, None ) )
  }
  
  private def runSingleTest( testName: String, reporter: Reporter ) = {
    this.setName(testName)
    this.run(new MyTestResult(reporter))
  }
   
  private class MyTestResult(reporter: Reporter) extends TestResult {

    override def addFailure(test: Test, t: AssertionFailedError) = {
      super.addFailure(test, t)
      reporter.testFailed( buildReport( test, Some(t) ) ) 
    }

    override def addError(test: Test, t: Throwable) = {
      super.addError(test, t)
      reporter.testFailed( buildReport( test, Some(t) ) )
    }
    
    override def startTest(test: Test) = {
      super.startTest(test)
      reporter.testStarting( buildReport( test, None ) )
    }
    
    override def endTest(test: Test) = {
      super.endTest(test)
      if( this.wasSuccessful ) reporter.testSucceeded( buildReport( test, None ) ) 
    }
    
    implicit def testToString(t: Test) = JUnitVersionHelper.getTestCaseName(t)
  }
  

}

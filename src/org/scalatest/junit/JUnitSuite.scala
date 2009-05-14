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
import org.junit.runner.JUnitCore
import org.junit.runner.notification.RunListener
import org.junit.runner.notification.Failure
import org.junit.runner.Description
import org.junit.runner.Result

/**
 * A suite of tests that can be run with either JUnit or ScalaTest. This trait allows you to write JUnit 4 tests
 * with ScalaTest's more concise assertion syntax as well as JUnit's assertions (<code>assertEquals</code>, etc.).
 * You create tests by defining methods that are annotated with <code>Test</code>, and can create fixtures with
 * methods annotated with <code>Before</code> and <code>After</code>. For example:
 *
 * <pre>
 * import org.scalatest.junit.JUnitSuite
 * import scala.collection.mutable.ListBuffer
 * import _root_.org.junit.Test
 * import _root_.org.junit.Before
 *
 * class TwoSuite extends JUnitSuite {
 *
 *   var sb: StringBuilder = _
 *   var lb: ListBuffer[String] = _
 *
 *   @Before override def initialize() {
 *     sb = new StringBuilder("ScalaTest is ")
 *     lb = new ListBuffer[String]
 *   }
 *
 *   @Test def verifyEasy() {
 *     sb.append("easy!")
 *     assert(sb.toString === "ScalaTest is easy!")
 *     assert(lb.isEmpty)
 *     lb += "sweet"
 *   }
 *
 *   @Test def verifyFun() {
 *     sb.append("fun!")
 *     assert(sb.toString === "ScalaTest is fun!")
 *     assert(lb.isEmpty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * To execute <code>JUnitSuite</code>s with ScalaTest's <code>Runner</code>, you must include JUnit's jar file on the class path or runpath.
 * This version of <code>JUnitSuite</code> was tested with JUnit version 4.4.
 * </p>
 *
 * @author Bill Venners
 * @author Daniel Watson
 * @author Joel Neely
 */
trait JUnitSuite extends Suite {

  override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, groupsToInclude: Set[String],
      groupsToExclude: Set[String], properties: Map[String, Any], distributor: Option[Distributor]) {

    val jUnitCore = new JUnitCore
    jUnitCore.addListener(new MyRunListener(reporter))
    val myClass = getClass
    jUnitCore.run(myClass)
  }

// verifySomething(org.scalatest.junit.helpers.HappySuite)
// Description.displayName of a test report has the form <testMethodName>(<suiteClassName>)
  private class MyRunListener(reporter: Reporter) extends RunListener {

/*
    override def testAssumptionFailure(failure: Failure) {
    }
*/

    // For now, at least, pass Nones for suiteName, suiteClassName, and testName. Possibly
    // later enhance this so that I'm grabbing this info out of JUnit's stupid displayName string.
    override def testFailure(failure: Failure) {
      val report = new Report(failure.getDescription.getDisplayName, "", None, None, None)
      reporter.testFailed(report)
    }

    override def testFinished(description: Description) {
      val report = new Report(description.getDisplayName, "", None, None, None)
      reporter.testSucceeded(report)
    }

    override def testIgnored(description: Description) {
      val report = new Report(description.getDisplayName, "", None, None, None)
      reporter.testIgnored(report)
    }

    override def testRunFinished(result: Result) {
      reporter.runCompleted()
    }

    override def testRunStarted(description: Description) {
      // Not sure what to do here with respect to ordinal and apply. Will have to see when it is called.
      reporter.runStarting(description.testCount)
    }

    override def testStarted(description: Description) {
      val report = new Report(description.getDisplayName, "", None, None, None)
      reporter.testStarting(report)
    }
  }
}
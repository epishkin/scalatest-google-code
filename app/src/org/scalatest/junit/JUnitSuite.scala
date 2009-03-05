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

    override def testFailure(failure: Failure) {
println("testFailure CALLED")
    }

    override def testFinished(description: Description) {
      val report = new Report(description.getDisplayName, "")
      reporter.testSucceeded(report)
println("testFinished CALLED" + description.getDisplayName)
    }

    override def testIgnored(description: Description) {
println("testIgnored CALLED")
    }

    override def testRunFinished(result: Result) {
println("testRunFinished CALLED")
    }

    override def testRunStarted(description: Description) {
      val report = new Report(description.getDisplayName, "")
      reporter.testStarting(report)
println("testRunStarted CALLED")
    }

    override def testStarted(description: Description) {
println("testStarted CALLED")
    }
  }
}

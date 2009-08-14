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
package org.scalatest.junit

import _root_.junit.framework.TestCase
import _root_.junit.framework.TestResult
import _root_.junit.framework.TestSuite
import _root_.junit.framework.TestListener
import _root_.junit.framework.Test
import _root_.junit.framework.AssertionFailedError
import scala.collection.mutable.HashSet
import org.scalatest.events.TestStarting
import org.scalatest.events.TestSucceeded
import org.scalatest.events.TestFailed

class JUnit3Suite extends TestCase with Suite {

  private var theTracker = new Tracker

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper,
      filter: Filter, configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    theTracker = tracker

    val testResult = new TestResult
    testResult.addListener(new MyTestListener(reporter, tracker))
    new TestSuite(this.getClass).run(testResult)
  }
}

private[scalatest] class MyTestListener(report: Reporter, tracker: Tracker) extends TestListener {

  // TODO: worry about threading
  private val failedTestsSet = scala.collection.mutable.Set[Test]()

  private def getSuiteNameForTestCase(testCase: Test) =
    testCase match {
      case junit3Suite: JUnit3Suite => junit3Suite.suiteName
      case _ => Suite.getSimpleNameOfAnObjectsClass(testCase) // Should never happen, but just in case
    }

  def getMessageGivenThrowable(throwable: Throwable, isAssertionFailedError: Boolean) =
    if (throwable.getMessage == null)
      "A JUnit3Suite test failed with an " + (if (isAssertionFailedError) "AssertionFailedError" else "exception") // Hopefully will never happen
    else
      throwable.getMessage

  // The Test passed to these methods is an instance of the JUnit3Suite class, Calling
  // test.getClass.getName on it gets the fully qualified name of the class
  // test.asInstanceOf[TestCase].getName gives you the name of the test method, without any parens
  // Calling test.toSring gives you testError(org.scalatestexamples.junit.JUnit3ExampleSuite)
  // So that's that old JUnit-style test name thing.
  def startTest(testCase: Test) {
    if (testCase == null)
      throw new NullPointerException("testCase was null")
    report(TestStarting(tracker.nextOrdinal, getSuiteNameForTestCase(testCase),
      Some(testCase.getClass.getName), testCase.toString))
  }
  
  def addError(testCase: Test, throwable: Throwable) {

    if (testCase == null)
      throw new NullPointerException("testCase was null")
    if (throwable == null)
      throw new NullPointerException("throwable was null")

    report(TestFailed(tracker.nextOrdinal, getMessageGivenThrowable(throwable, false),
      getSuiteNameForTestCase(testCase), Some(testCase.getClass.getName), testCase.toString, Some(throwable)))

    failedTestsSet += testCase
  }
  
  def addFailure(testCase: Test, assertionFailedError: AssertionFailedError) {

    if (testCase == null)
      throw new NullPointerException("testCase was null")
    if (assertionFailedError == null)
      throw new NullPointerException("throwable was null")

    report(TestFailed(tracker.nextOrdinal, getMessageGivenThrowable(assertionFailedError, true),
      getSuiteNameForTestCase(testCase), Some(testCase.getClass.getName), testCase.toString, Some(assertionFailedError)))

    failedTestsSet += testCase
  }

  def endTest(testCase: Test) {

    val testHadFailed = failedTestsSet.contains(testCase)

    if (!testHadFailed) {
      if (testCase == null)
        throw new NullPointerException("testCase was null")
      report(TestSucceeded(tracker.nextOrdinal, getSuiteNameForTestCase(testCase),
        Some(testCase.getClass.getName), testCase.toString))
    }
    else {
      failedTestsSet -= testCase  
    }
  }

}


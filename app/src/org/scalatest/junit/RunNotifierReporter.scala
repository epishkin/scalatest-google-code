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

import org.junit.runner.notification.RunNotifier
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.scalatest.events._

// TODO: Mention on each Reporter method that it does nothing

// There's no way to really pass along a suiteStarting or suiteCompleted
// report. They have a dumb comment to "Do not invoke" fireTestRunStarted
// and fireTestRunFinished, so I think they must be doing that themselves.
// This means we don't have a way to really forward runStarting and
// runCompleted reports either. But runAborted reports should be sent
// out the door somehow, so we report them with yet another fireTestFailure.
private[junit] class RunNotifierReporter(runNotifier: RunNotifier) extends Reporter {

  private def getNameFromReport(report: Report): String = report.name

  private def testDescriptionName(suiteName: String, suiteClassName: Option[String], testName: String) =
    suiteClassName match {
      case Some(suiteClassName) => testName + "(" + suiteClassName + ")"
      case None => testName + "(" + suiteName + ")"
    }

  private def suiteDescriptionName(suiteName: String, suiteClassName: Option[String]) =
    suiteClassName match {
      case Some(suiteClassName) => suiteClassName
      case None => suiteName
    }

  def apply(event: Event) {

    event match {

      case TestStarting(ordinal, suiteName, suiteClassName, testName, formatter, rerunnable, payload, threadName, timeStamp) =>
        runNotifier.fireTestStarted(Description.createSuiteDescription(testDescriptionName(suiteName, suiteClassName, testName)))

      case TestFailed(ordinal, message, suiteName, suiteClassName, testName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 
        val throwableOrNull =
          throwable match {
            case Some(t) => t
            case None => null // Yuck. Not sure if the exception passed to new Failure can be null, but it could be given this code. Usually throwable would be defined.
          }
        val description = Description.createSuiteDescription(testDescriptionName(suiteName, suiteClassName, testName))
        runNotifier.fireTestFailure(new Failure(description, throwableOrNull))
        runNotifier.fireTestFinished(description)

      case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) => 
        runNotifier.fireTestFinished(Description.createSuiteDescription(testDescriptionName(suiteName, suiteClassName, testName)))

      case TestIgnored(ordinal, suiteName, suiteClassName, testName, formatter, payload, threadName, timeStamp) => 
        runNotifier.fireTestIgnored(Description.createSuiteDescription(testDescriptionName(suiteName, suiteClassName, testName)))

      case SuiteAborted(ordinal, message, suiteName, suiteClassName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 
        val throwableOrNull =
          throwable match {
            case Some(t) => t
            case None => null // Yuck. Not sure if the exception passed to new Failure can be null, but it could be given this code. Usually throwable would be defined.
          }
        val description = Description.createSuiteDescription(suiteDescriptionName(suiteName, suiteClassName))
        runNotifier.fireTestFailure(new Failure(description, throwableOrNull)) // Best we can do in JUnit, as far as I know
        runNotifier.fireTestFinished(description)

      case RunAborted(ordinal, message, throwable, duration, summary, formatter, payload, threadName, timeStamp) => 
        val throwableOrNull =
          throwable match {
            case Some(t) => t
            case None => null // Yuck. Not sure if the exception passed to new Failure can be null, but it could be given this code. Usually throwable would be defined.
          }
        val description = Description.createSuiteDescription("org.scalatest.tools.Runner")
        runNotifier.fireTestFailure(new Failure(description, throwableOrNull)) // Best we can do in JUnit, as far as I know
        runNotifier.fireTestFinished(description)

      case _ =>
    }
  }

  override def testStarting(report: Report) {
    runNotifier.fireTestStarted(Description.createSuiteDescription(getNameFromReport(report)))
  }

  override def testSucceeded(report: Report) {
    runNotifier.fireTestFinished(Description.createSuiteDescription(getNameFromReport(report)))
  }

  override def testIgnored(report: Report) {
    val description = Description.createSuiteDescription(getNameFromReport(report))
    runNotifier.fireTestIgnored(description)
  }

  // Not sure if the exception passed to new Failure can be null, but it will be
  override def testFailed(report: Report) {
    val throwable =
      report.throwable match {
        case Some(t) => t
        case None => null // yuck
      }
    val description = Description.createSuiteDescription(getNameFromReport(report))
    runNotifier.fireTestFailure(new Failure(description, throwable))
    runNotifier.fireTestFinished(description)
  }

  override def suiteAborted(report: Report) {
    testFailed(report) // Best we can do in JUnit, as far as I know
  }

  // Not sure if the exception passed to new Failure can be null, but it will be
  override def runAborted(report: Report) {
    testFailed(report) // Best we can do in JUnit, as far as I know
  }
}

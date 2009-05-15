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
import org.scalatest.events.Event

// TODO: Mention on each Reporter method that it does nothing

// There's no way to really pass along a suiteStarting or suiteCompleted
// report. They have a dumb comment to "Do not invoke" fireTestRunStarted
// and fireTestRunFinished, so I think they must be doing that themselves.
// This means we don't have a way to really forward runStarting and
// runCompleted reports either. But runAborted reports should be sent
// out the door somehow, so we report them with yet another fireTestFailure.
private[junit] class RunNotifierReporter(runNotifier: RunNotifier) extends Reporter {

  private def getNameFromReport(report: Report): String = report.name // TODO: handle these things going through events.
/*
    report.suiteClassName match {
      case Some(suiteClassName) =>
        report.testName match {
          case Some(testName) => testName + "(" + suiteClassName + ")"
          case None => report.name
        }
      case None => report.name
    }
*/

  override def apply(event: Event) {
    super.apply(event)
/*
    event match {

      case RunStarting(ordinal, testCount, formatter, payload, threadName, timeStamp) => runStarting(testCount)

      case TestStarting(ordinal, name, suiteName, suiteClassName, testName, formatter, rerunnable, payload, threadName, timeStamp) =>
        testStarting(new Report(name, "XXX test starting", None, rerunnable, threadName, new Date(timeStamp)))

      case TestSucceeded(ordinal, name, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) => 
        testSucceeded(new Report(name, "XXX test succeeded", None, rerunnable, threadName, new Date(timeStamp)))

      case TestFailed(ordinal, name, message, suiteName, suiteClassName, testName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 
        testFailed(new Report(name, message, throwable, rerunnable, threadName, new Date(timeStamp)))

      case TestIgnored(ordinal, name, suiteName, suiteClassName, testName, formatter, payload, threadName, timeStamp) => 
        testIgnored(new Report(name, "XXX test ignored", None, None, threadName, new Date(timeStamp)))

      case TestPending(ordinal, name, suiteName, suiteClassName, testName, formatter, payload, threadName, timeStamp) => 

      case SuiteStarting(ordinal, suiteName, suiteClassName, formatter, rerunnable, payload, threadName, timeStamp) =>
        suiteStarting(new Report(suiteName, "XXX suite starting", None, rerunnable, threadName, new Date(timeStamp)))

/*
      case SuiteStarting(ordinal, suiteName, suiteClassName, formatter, rerunnable, payload, threadName, timeStamp) =>
        formatter match {
          case Some(formatter) =>
            suiteStarting(new SpecReport(suiteName, "XXX suite starting", None, rerunnable, threadName, new Date(timeStamp)))

          case None =>
            suiteStarting(new Report(suiteName, "XXX suite starting", None, rerunnable, threadName, new Date(timeStamp)))
        }
*/

      case SuiteCompleted(ordinal, name, suiteName, suiteClassName, duration, formatter, rerunnable, payload, threadName, timeStamp) => 
        suiteCompleted(new Report(name, "XXX suite completed", None, rerunnable, threadName, new Date(timeStamp)))

      case SuiteAborted(ordinal, name, message, suiteName, suiteClassName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 
        suiteAborted(new Report(name, message, throwable, rerunnable, threadName, new Date(timeStamp)))

      case InfoProvided(ordinal, message, nameInfo, throwable, formatter, payload, threadName, timeStamp) => 
        infoProvided(new Report("XXX Unknown", message, throwable, None, threadName, new Date(timeStamp)))

      case RunStopped(ordinal, duration, summary, formatter, payload, threadName, timeStamp) => runStopped()

      case RunAborted(ordinal, message, throwable, duration, summary, formatter, payload, threadName, timeStamp) => 
        runAborted(new Report("org.scalatest.tools.Runner", message, throwable, None, threadName, new Date(timeStamp)))

      case RunCompleted(ordinal, duration, summary, formatter, payload, threadName, timeStamp) => runCompleted()
    }
*/
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

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

// TODO: Mention on each Reporter method that it does nothing

// There's no way to really pass along a suiteStarting or suiteCompleted
// report. They have a dumb comment to "Do not invoke" fireTestRunStarted
// and fireTestRunFinished, so I think they must be doing that themselves.
// This means we don't have a way to really forward runStarting and
// runCompleted reports either. But runAborted reports should be sent
// out the door somehow, so we report them with yet another fireTestFailure.
private[junit] class RunNotifierReporter(runNotifier: RunNotifier) extends Reporter {

  private def getNameFromReport(report: Report): String =
    report.suiteClassName match {
      case Some(suiteClassName) =>
          report.testName match {
            case Some(testName) => testName + "(" + suiteClassName + ")"
            case None => report.name
          }
      case None => report.name
    }

  override def testStarting(report: Report) {
    runNotifier.fireTestStarted(Description.createSuiteDescription(getNameFromReport(report)))
  }

  override def testSucceeded(report: Report) {
    runNotifier.fireTestFinished(Description.createSuiteDescription(report.name))
  }

  override def testIgnored(report: Report) {
    runNotifier.fireTestIgnored(Description.createSuiteDescription(report.name))
  }

  // Not sure if the exception passed to new Failure can be null, but it will be
  override def testFailed(report: Report) {
    val throwable =
      report.throwable match {
        case Some(t) => t
        case None => null // yuck
      }
    runNotifier.fireTestFailure(new Failure(Description.createSuiteDescription(report.name), throwable))
  }

  // Not sure if the exception passed to new Failure can be null, but it will be
  override def suiteAborted(report: Report) {
    val throwable =
      report.throwable match {
        case Some(t) => t
        case None => null // yuck
      }
    runNotifier.fireTestFailure(new Failure(Description.createSuiteDescription(report.name), throwable))
  }

  // Not sure if the exception passed to new Failure can be null, but it will be
  override def runAborted(report: Report) {
    val throwable =
      report.throwable match {
        case Some(t) => t
        case None => null // yuck
      }
    runNotifier.fireTestFailure(new Failure(Description.createSuiteDescription(report.name), throwable))
  }
}

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

private[junit] class RunNotifierReporter(runNotifier: RunNotifier) extends Reporter {

  override def testStarting(report: Report) {
    runNotifier.fireTestStarted(Description.createSuiteDescription(report.name))
  }

  override def testSucceeded(report: Report) {
    runNotifier.fireTestFinished(Description.createSuiteDescription(report.name))
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
}

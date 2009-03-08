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

// There's no way to really pass along a suiteStarting or suiteCompleted
// report. They have a dumb comment to "Do not invoke" fireTestRunStarted
// and fireTestRunFinished, so I think they must be doing that themselves.
// This means we don't have a way to really forward runStarting and
// runCompleted reports either. But runAborted reports should be sent
// out the door somehow, so we report them with yet another fireTestFailure.
class RunNotifierSuite extends FunSuite {

  test("report.testStarting generates a fireTestStarted invocation") {

    val runNotifier =
      new RunNotifier {
        var fireTestStartedInvocationCount = 0
        var passedDesc: Option[Description] = None
        override def fireTestStarted(description: Description) {
          fireTestStartedInvocationCount += 1
          passedDesc = Some(description)
        }
      }

    val reporter = new RunNotifierReporter(runNotifier)
    val report = new Report("some test name", "test starting just fine we think", None, None, None, None)
    reporter.testStarting(report)
    assert(runNotifier.fireTestStartedInvocationCount === 1)
    assert(runNotifier.passedDesc.get.getDisplayName === "some test name")

    val report2 = new Report("name", "message", Some("fully.qualified.SuiteClassName"), Some("theTestName"), None, None)
    reporter.testStarting(report2)
    assert(runNotifier.fireTestStartedInvocationCount === 2)
    assert(runNotifier.passedDesc.get.getDisplayName === "theTestName(fully.qualified.SuiteClassName)")
  }

  test("report.testFailed generates a fireTestFailure invocation") {

    val runNotifier =
      new RunNotifier {
        var methodInvocationCount = 0
        var passed: Option[Failure] = None
        override def fireTestFailure(failure: Failure) {
          methodInvocationCount += 1
          passed = Some(failure)
        }
      }

    val reporter = new RunNotifierReporter(runNotifier)
    val exception = new IllegalArgumentException
    val report = new Report("some test name", "test starting just fine we think", None, None, Some(exception), None)
    reporter.testFailed(report)
    assert(runNotifier.methodInvocationCount === 1)
    assert(runNotifier.passed.get.getException === exception)
    assert(runNotifier.passed.get.getDescription.getDisplayName === "some test name")

    val report2 = new Report("some test name", "test starting just fine we think", Some("fully.qualified.SuiteClassName"), Some("theTestName"), Some(exception), None)
    reporter.testFailed(report2)
    assert(runNotifier.methodInvocationCount === 2)
    assert(runNotifier.passed.get.getException === exception)
    assert(runNotifier.passed.get.getDescription.getDisplayName === "theTestName(fully.qualified.SuiteClassName)")
  }

  test("report.testSucceeded generates a fireTestFinished invocation") {

    val runNotifier =
      new RunNotifier {
        var methodWasInvoked = false
        var passed: Description = _
        override def fireTestFinished(description: Description) {
          methodWasInvoked = true
          passed = description
        }
      }

    val reporter = new RunNotifierReporter(runNotifier)
    val report = new Report("some test name", "test starting just fine we think")
    reporter.testSucceeded(report)
    assert(runNotifier.methodWasInvoked)
    assert(runNotifier.passed.getDisplayName === "some test name")
  }

  test("report.testIgnored generates a fireTestIgnored invocation") {

    val runNotifier =
      new RunNotifier {
        var methodWasInvoked = false
        var passed: Description = _
        override def fireTestIgnored(description: Description) {
          methodWasInvoked = true
          passed = description
        }
      }

    val reporter = new RunNotifierReporter(runNotifier)
    val report = new Report("some test name", "test starting just fine we think")
    reporter.testIgnored(report)
    assert(runNotifier.methodWasInvoked)
    assert(runNotifier.passed.getDisplayName === "some test name")
  }

  // fireTestFailure is the best we could do given the RunNotifier interface
  test("report.suiteAborted generates a fireTestFailure invocation") {

    val runNotifier =
      new RunNotifier {
        var methodWasInvoked = false
        var passed: Failure = _
        override def fireTestFailure(failure: Failure) {
          methodWasInvoked = true
          passed = failure
        }
      }

    val reporter = new RunNotifierReporter(runNotifier)
    val exception = new IllegalArgumentException
    val report = new Report("some test name", "test starting just fine we think", Some(exception), None)
    reporter.suiteAborted(report)
    assert(runNotifier.methodWasInvoked)
    assert(runNotifier.passed.getException === exception)
    assert(runNotifier.passed.getDescription.getDisplayName === "some test name")
  }

  // fireTestFailure is the best we could do given the RunNotifier interface
  test("report.runAborted generates a fireTestFailure invocation") {

    val runNotifier =
      new RunNotifier {
        var methodWasInvoked = false
        var passed: Failure = _
        override def fireTestFailure(failure: Failure) {
          methodWasInvoked = true
          passed = failure
        }
      }

    val reporter = new RunNotifierReporter(runNotifier)
    val exception = new IllegalArgumentException
    val report = new Report("some test name", "test starting just fine we think", Some(exception), None)
    reporter.runAborted(report)
    assert(runNotifier.methodWasInvoked)
    assert(runNotifier.passed.getException === exception)
    assert(runNotifier.passed.getDescription.getDisplayName === "some test name")
  }
}

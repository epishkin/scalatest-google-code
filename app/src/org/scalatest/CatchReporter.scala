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
package org.scalatest

import java.io.PrintStream

/**
 * This reporter just catches exceptions thrown by the passed reporter and
 * prints info about them to the standard error stream. This is because people
 * can pass in custom reporters that may have bugs. I want the test run to continue
 * in case one of them throws back an exception.
 *
 * @author Bill Venners
 */
private[scalatest] class CatchReporter(reporter: Reporter, out: PrintStream) extends Reporter {

  def this(reporter: Reporter) = this(reporter, System.err)

  override def runStarting(testCount: Int) = dispatch("runStarting", (reporter: Reporter) => reporter.runStarting(testCount))
  override def testSucceeded(report: Report) = dispatch("testSucceeded", (reporter: Reporter) => reporter.testSucceeded(report))
  override def testIgnored(report: Report) = dispatch("testIgnored", (reporter: Reporter) => reporter.testIgnored(report))
  override def testFailed(report: Report) = dispatch("testFailed", (reporter: Reporter) => reporter.testFailed(report))
  override def infoProvided(report: Report) = dispatch("infoProvided", (reporter: Reporter) => reporter.infoProvided(report))
  override def testStarting(report: Report) = dispatch("testStarting", (reporter: Reporter) => reporter.testStarting(report))
  override def suiteStarting(report: Report) = dispatch("suiteStarting", (reporter: Reporter) => reporter.suiteStarting(report))
  override def suiteCompleted(report: Report) = dispatch("suiteCompleted", (reporter: Reporter) => reporter.suiteCompleted(report))
  override def suiteAborted(report: Report)  = dispatch("suiteAborted", (reporter: Reporter) => reporter.suiteAborted(report))
  override def runStopped() = dispatch("runStopped", (reporter: Reporter) => reporter.runStopped())
  override def runAborted(report: Report) = dispatch("runAborted", (reporter: Reporter) => reporter.runAborted(report))
  override def runCompleted() = dispatch("runCompleted", (reporter: Reporter) => reporter.runCompleted())
  override def dispose() = dispatch("dispose", (reporter: Reporter) => reporter.dispose())

  private[scalatest] def dispatch(methodName: String, methodCall: (Reporter) => Unit) {

    try {
      methodCall(reporter)
    }
    catch {
      case e: Exception => CatchReporter.handleReporterException(e, methodName, out)
    }
  }
}

private[scalatest] object CatchReporter {

  def handleReporterException(e: Exception, methodName: String, out: PrintStream) {

    val stringToPrint = Resources("reporterThrew", methodName)

    out.println(stringToPrint)
    e.printStackTrace(out)
  }
}


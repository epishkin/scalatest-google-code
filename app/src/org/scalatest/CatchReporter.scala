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
import org.scalatest.events.Event

/**
 * This report just catches exceptions thrown by the passed report and
 * prints info about them to the standard error stream. This is because people
 * can pass in custom reports that may have bugs. I want the test run to continue
 * in case one of them throws back an exception.
 *
 * @author Bill Venners
 */
private[scalatest] class CatchReporter(report: Reporter, out: PrintStream) extends Reporter {

  def this(report: Reporter) = this(report, System.err)

  def apply(event: Event) {
    try {
      report(event)
    }
    catch {
      case e: Exception => 
        val stringToPrint = Resources("reporterThrew", event)
        out.println(stringToPrint)
        e.printStackTrace(out)
    }
  }

  // Won't need the rest of this class after phase II of the report refactor is done, probably 0.9.8
  override def testSucceeded(rpt: Report) = dispatch("testSucceeded", (report: Reporter) => report.testSucceeded(rpt))
  override def testIgnored(rpt: Report) = dispatch("testIgnored", (report: Reporter) => report.testIgnored(rpt))
  override def testFailed(rpt: Report) = dispatch("testFailed", (report: Reporter) => report.testFailed(rpt))
  override def infoProvided(rpt: Report) = dispatch("infoProvided", (report: Reporter) => report.infoProvided(rpt))
  override def testStarting(rpt: Report) = dispatch("testStarting", (report: Reporter) => report.testStarting(rpt))
  override def suiteStarting(rpt: Report) = dispatch("suiteStarting", (report: Reporter) => report.suiteStarting(rpt))
  override def suiteCompleted(rpt: Report) = dispatch("suiteCompleted", (report: Reporter) => report.suiteCompleted(rpt))
  override def suiteAborted(rpt: Report)  = dispatch("suiteAborted", (report: Reporter) => report.suiteAborted(rpt))
  override def runStopped() = dispatch("runStopped", (report: Reporter) => report.runStopped())
  override def dispose() = dispatch("dispose", (report: Reporter) => report.dispose())

  private[scalatest] def dispatch(methodName: String, methodCall: (Reporter) => Unit) {

    try {
      methodCall(report)
    }
    catch {
      case e: Exception => CatchReporter.handleReporterException(e, methodName, out)
    }
  }
}

// Won't need this after phase II of the report refactor is done, probably 0.9.8
private[scalatest] object CatchReporter {

  def handleReporterException(e: Exception, methodName: String, out: PrintStream) {

    val stringToPrint = Resources("reporterThrew", methodName)

    out.println(stringToPrint)
    e.printStackTrace(out)
  }
}


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

import scala.actors.Exit
import scala.actors.Actor
import scala.actors.Actor.actor
import scala.actors.Actor.loop
import scala.actors.Actor.receive
import org.scalatest.CatchReporter.handleReporterException
import java.io.PrintStream
import org.scalatest.events.Event

/**
 * A <code>Reporter</code> that dispatches test results to other <code>Reporter</code>s.
 * Attempts to dispatch each method invocation to each contained <code>Reporter</code>,
 * even if some <code>Reporter</code> methods throw <code>Exception</code>s. Catches
 * <code>Exception</code>s thrown by <code>Reporter</code> methods and prints error
 * messages to the standard error stream.
 *
 * The primary constructor creates a new <code>DispatchReporter</code> with specified <code>Reporter</code>s list.
 * Each object in the <code>reporters</code> list must implement <code>Reporter</code>.
 *
 * @param reporters the initial <code>Reporter</code>s list for this
 * <code>DispatchReporter</code>
 * @throws NullPointerException if <code>reporters</code> is <code>null</code>.
 * @author Bill Venners
 */
private[scalatest] class DispatchReporter(val reporters: List[Reporter], out: PrintStream) extends Reporter {

  private val julia = actor {
    var alive = true // local variable, right? Only used by the Actor's thread, so no need for synchronization
    while (alive) {
      receive {
        case event: Event => 
          try {
            for (report <- reporters)
              report(event)
          }
          catch {
            case e: Exception => 
              val stringToPrint = Resources("reporterThrew", event)
              out.println(stringToPrint)
              e.printStackTrace(out)
          }
        case TestStartingMsg(rpt) => dispatch("testStarting", (reporter: Reporter) => reporter.testStarting(rpt))
        case TestIgnoredMsg(rpt) => dispatch("testIgnored", (reporter: Reporter) => reporter.testIgnored(rpt))
        case TestSucceededMsg(rpt) => dispatch("testSucceeded", (reporter: Reporter) => reporter.testSucceeded(rpt))
        case TestFailedMsg(rpt) => dispatch("testFailed", (reporter: Reporter) => reporter.testFailed(rpt))
        case SuiteStartingMsg(rpt) => dispatch("suiteStarting", (reporter: Reporter) => reporter.suiteStarting(rpt))
        case SuiteCompletedMsg(rpt) => dispatch("suiteCompleted", (reporter: Reporter) => reporter.suiteCompleted(rpt))
        case SuiteAbortedMsg(rpt) => dispatch("suiteAborted", (reporter: Reporter) => reporter.suiteAborted(rpt))
        case InfoProvidedMsg(rpt) => dispatch("infoProvided", (reporter: Reporter) => reporter.infoProvided(rpt))
        case RunStoppedMsg() => dispatch("runStopped", (reporter: Reporter) => reporter.runStopped())
        case DisposeMsg() => {
          dispatch("dispose", (reporter: Reporter) => reporter.dispose())
          alive = false
        }
      }
    }
  }

  def this(reporters: List[Reporter]) = this(reporters, System.out)
  def this(reporter: Reporter) = this(List(reporter), System.out)

  /* where do I put this Scaladoc?
   * Returns a <code>List</code> of the <code>Reporter</code>s contained in this
   * <code>DispatchReporter</code>.
   *
   * @return a <code>List</code> of the <code>Reporter</code>s contained in this
   * <code>DispatchReporter</code>.
   */

  /**
   * Invokes <code>testSucceeded</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>testSucceeded</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>testSucceeded</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>testSucceeded</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report the <code>Report</code> encapsulating this test succeeded event
   * @throws NullPointerException if <code>report</code> is <code>null</code>
   */
  override def testSucceeded(report: Report) = julia ! TestSucceededMsg(report)

  /**
   * Invokes <code>testIgnored</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>
   * 
   * <P>
   * This method attempts to invoke <code>testIgnored</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>testIgnored</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>testIgnored</code> method and handles it by printing an error message to the
   * standard error stream.
   * 
   */
  override def testIgnored(report: Report) = julia ! TestIgnoredMsg(report)

  /**
   * Invokes <code>testFailed</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>testFailed</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>testFailed</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>testFailed</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report the <code>Report</code> encapsulating this test failed event
   * @throws NullPointerException if <code>report</code> is <code>null</code>
   */
  override def testFailed(report: Report) = julia ! TestFailedMsg(report)

  /**
   * Invokes <code>infoProvided</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>infoProvided</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>infoProvided</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>infoProvided</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report the <code>Report</code> encapsulating this info provided event
   * @throws NullPointerException if <code>report</code> is <code>null</code>
   */
  override def infoProvided(report: Report) = julia ! InfoProvidedMsg(report)

 /**
  * Invokes <code>testStarting</code> on each <code>Reporter</code> in this
  * <code>DispatchReporter</code>'s reporters list, passing the specified
  * <code>report</code>.
  *
  * <P>
  * This method attempts to invoke <code>testStarting</code> on each contained <code>Reporter</code>,
  * even if some <code>Reporter</code>'s <code>testStarting</code> methods throw
  * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
  * a <code>testStarting</code> method and handles it by printing an error message to the
  * standard error stream.
  *
  * @param report the <code>Report</code> encapsulating this test starting event
  * @throws NullPointerException if <code>report</code> is <code>null</code>
  */
  override def testStarting(report: Report) = julia ! TestStartingMsg(report)
  
  /**
   * Invokes <code>suiteStarting</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>suiteStarting</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>suiteStarting</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>suiteStarting</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report a <code>Report</code> that encapsulates the suite starting event to report.
   *
   * @throws NullPointerException if <code>report</code> reference is <code>null</code>
   */
  override def suiteStarting(report: Report) = julia ! SuiteStartingMsg(report)

  /**
   * Invokes <code>suiteCompleted</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>suiteCompleted</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>suiteCompleted</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>suiteCompleted</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report a <code>Report</code> that encapsulates the suite completed event to report.
   * @throws NullPointerException if <code>report</code> reference is <code>null</code>
   */
  override def suiteCompleted(report: Report) = julia ! SuiteCompletedMsg(report)

  /**
   * Indicates the execution of a suite of tests has aborted prior to completion.
   * Invokes <code>suiteAborted</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>suiteAborted</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>suiteAborted</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>suiteAborted</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report a <code>Report</code> that encapsulates the suite aborted event to report.
   * @throws NullPointerException if <code>report</code> reference is <code>null</code>
   */
  override def suiteAborted(report: Report) = julia ! SuiteAbortedMsg(report)

  /**
   * Indicates a runner has stopped running a suite of tests prior to completion.
   * Invokes <code>runStopped</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list.
   *
   * <P>
   * This method attempts to invoke <code>runStopped</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>runStopped</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>runStopped</code> method and handles it by printing an error message to the
   * standard error stream.
   */
  override def runStopped() = julia ! RunStoppedMsg()

  /**
   * Invokes <code>dispose</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list.
   *
   * <P>
   * This method attempts to invoke <code>dispose</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>dispose</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>dispose</code> method and handles it by printing an error message to the
   * standard error stream.
   */
  override def dispose() = julia ! DisposeMsg()

  def apply(event: Event) {
    julia ! event
  }

  private def dispatch(methodName: String, methodCall: (Reporter) => Unit) {
 
    try {
      reporters.foreach(methodCall)
    }
    catch {
      case e: Exception => handleReporterException(e, methodName, out)
    }
  }
}


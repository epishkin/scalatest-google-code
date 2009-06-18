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
        case InfoProvidedMsg(rpt) => dispatch("infoProvided", (reporter: Reporter) => reporter.infoProvided(rpt))
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


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

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.Iterator
import java.util.Set
import java.io.StringWriter

/**
 * A <code>Reporter</code> that prints test status information to
 * a <code>Writer</code>, <code>OutputStream</code>, or file.
 *
 * @author Bill Venners
 */
private[scalatest] abstract class PrintReporter(pw: PrintWriter) extends Reporter {

  // This is only modified by the actor thread that serializes reports, so no need for synchronization.
  private var testsCompletedCount = 0
  private var testsFailedCount = 0
  private var suitesAbortedCount = 0

  /**
  * Construct a <code>PrintReporter</code> with passed
  * <code>OutputStream</code>. Information about events reported to instances of this
  * class will be written to the <code>OutputStream</code> using the
  * default character encoding.
  *
  * @param os the <code>OutputStream</code> to which to print reported info
  * @throws NullPointerException if passed <code>os</code> reference is <code>null</code>
  */
  def this(os: OutputStream) = this(new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os, PrintReporter.BufferSize))))

  /**
  * Construct a <code>PrintReporter</code> with passed
  * <code>String</code> file name. Information about events reported to instances of this
  * class will be written to the specified file using the
  * default character encoding.
  *
  * @param filename the <code>String</code> name of the file to which to print reported info
  * @throws NullPointerException if passed <code>filename</code> reference is <code>null</code>
  * @throws IOException if unable to open the specified file for writing
  */
  def this(filename: String) = this(new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File(filename)), PrintReporter.BufferSize)))

  /**
  * Prints information indicating that a run with an expected <code>testCount</code>
  * number of tests is starting.
  *
  * @param report a <code>Report</code> that encapsulates the run starting event to report.
  * @throws IllegalArgumentException if <code>testCount</code> is less than zero.
  */
  override def runStarting(testCount: Int) {

    if (testCount < 0)
      throw new IllegalArgumentException
  
    testsCompletedCount = 0
    testsFailedCount = 0
    suitesAbortedCount = 0

    printResourceStringWithInt("runStarting", testCount)
  }

  /**
  * Prints information extracted from the specified <code>Report</code>
  * about a test about to be run.
  *
  * @param report a <code>Report</code> that encapsulates the test starting event to report.
  * @throws NullPointerException if <code>report</code> reference is <code>null</code>
  */
  override def testStarting(report: Report) {
    makeReport(report, "testStarting")
  }

  /**
  * Prints information extracted from the specified <code>Report</code>
  * about a test that succeeded.
  *
  * @param report a <code>Report</code> that encapsulates the test succeeded event to report.
  * @throws NullPointerException if <code>report</code> reference is <code>null</code>
  */
  override def testSucceeded(report: Report) {
    makeReport(report, "testSucceeded")
    testsCompletedCount += 1
  }
    
  /**
  * Prints information extracted from the specified <code>Report</code>
  * about a test that succeeded.
  *
  * @param report a <code>Report</code> that encapsulates the test succeeded event to report.
  * @throws NullPointerException if <code>report</code> reference is <code>null</code>
  */
  override def testIgnored(report: Report) {
    makeReport(report, "testIgnored")
  }

  /**
  * Prints information extracted from the specified <code>Report</code>
  * about a test that failed.
  *
  * @param report a <code>Report</code> that encapsulates the test failed event to report.
  * @throws NullPointerException if <code>report</code> reference is <code>null</code>
  */
  override def testFailed(report: Report) {
    makeReport(report, "testFailed")
    testsCompletedCount += 1
    testsFailedCount += 1
  }

  /**
  * Prints information extracted from the specified <code>Report</code>.
  *
  * @param report a <code>Report</code> that encapsulates the event to report.
  * @throws NullPointerException if <code>report</code> reference is <code>null</code>
  */
  override def infoProvided(report: Report) {
    makeReport(report, "infoProvided")
  }

  /**
  * Prints information indicating a suite of tests is about to start executing.
  *
  * @param report a <code>Report</code> that encapsulates the suite starting event to report.
  * @throws NullPointerException if <code>report</code> reference is <code>null</code>
   */
  override def suiteStarting(report: Report) {
    makeReport(report, "suiteStarting")
  }

  /**
  * Prints information indicating a suite of tests has completed executing.
  *
  * @param report a <code>Report</code> that encapsulates the suite completed event to report.
  * @throws NullPointerException if <code>report</code> reference is <code>null</code>
  */
  override def suiteCompleted(report: Report) {
    makeReport(report, "suiteCompleted")
  }

  /**
  * Prints information indicating the execution of a suite of tests has aborted prior to completion.
  *
  * @param report a <code>Report</code> that encapsulates the suite aborted event to report.
  * @throws NullPointerException if <code>report</code> reference is <code>null</code>
  */
  override def suiteAborted(report: Report) {
    suitesAbortedCount += 1
    makeReport(report, "suiteAborted")
  }

  /**
  * Prints information indicating a runner has stopped running a suite of tests prior to completion.
  */
  override def runStopped() {
    makeFinalReport("runStopped")
  }

  /**
  * Prints information indicating a run has aborted prior to completion.
  *
  * @param report a <code>Report</code> that encapsulates the suite aborted event to report.
  * @throws NullPointerException if <code>report</code> reference is <code>null</code>
  */
  override def runAborted(report: Report) {
    makeReport(report, "runAborted")
  }

  /**
  * Prints information indicating a run has completed.
  */
  override def runCompleted() {
    makeFinalReport("runCompleted")
  }

  /**
  * Releases any resources, such as file handles, held by this <code>PrintReporter</code>. Clients should
  * call this method when they no longer need the <code>PrintReporter</code>, before releasing the last reference
  * to the <code>PrintReporter</code>. After this method is invoked, the <code>PrintReporter</code> is defunct,
  * and not usable anymore.
  */
  override def dispose() {
    pw.close()
  }

  private def printResourceStringWithInt(resourceName: String, testCount: Int) {

    val stringToReport = Resources(resourceName, testCount.toString)

    pw.println(stringToReport)
    pw.flush()
  }

  private def printResourceString(resourceName: String) {

    pw.println(Resources(resourceName))
    pw.flush()
  }

  private def makeFinalReport(resourceName: String) {

    printResourceStringWithInt(resourceName, testsCompletedCount)

    // *** 1 SUITE ABORTED ***
    if (suitesAbortedCount == 1)
      printResourceString("oneSuiteAborted") 

    // *** {0} SUITES ABORTED ***
    else if (suitesAbortedCount > 1)
      printResourceStringWithInt("multipleSuitesAborted", suitesAbortedCount) 

    // *** 1 TEST FAILED ***
    if (testsFailedCount == 1)
      printResourceString("oneTestFailed") 

    // *** {0} TESTS FAILED ***
    else if (testsFailedCount > 1)
      printResourceStringWithInt("multipleTestsFailed", testsFailedCount) 

    else if (suitesAbortedCount == 0)
      printResourceString("allTestsPassed")

    pw.flush()
  }


  // We subtract one from test reports because we add "- " in front, so if one is actually zero, it will come here as -1
  private def indent(s: String, times: Int) = if (times <= 0) s else ("  " * times) + s

  // Stupid properties file won't let me put spaces at the beginning of a property
  // "  {0}" comes out as "{0}", so I can't do indenting in a localizable way. For now
  // just indent two space to the left.  //  if (times <= 0) s 
  //  else Resources("indentOnce", indent(s, times - 1))
  
  private def makeReport(report: Report, resourceName: String) {

    if (report == null)
      throw new NullPointerException("report is null")

    val stringToPrintOption: Option[String] = 
      report match {
        case specReport: SpecReport =>
          resourceName match {
            case "testStarting" => {
              report.throwable match {
                case Some(t) => Some(Resources(resourceName, report.name, report.message)) // Ugly, but at least they'll get info in the unlikely event of an exception in a testStarting report
                case None => None // In the general case, we won't show anything in a spec output for testStarting.
              }
            }
            case "testSucceeded" => {
              val icon = Resources("exampleSucceededIconChar")
              Some(indent(Resources("exampleIconPlusShortName", icon, specReport.shortName), specReport.level - 1))
            }
            case "testFailed" => {
              val icon = Resources("exampleFailedIconChar")
              Some(indent(Resources("exampleIconPlusShortNameAndNote", icon, specReport.shortName, Resources("failedNote")), specReport.level - 1))
            }
            case _ => Some(indent(specReport.shortName, specReport.level))
          }
        case _ => {
          val resName = if (report.message.trim.isEmpty) resourceName + "NoMessage" else resourceName
          Some(Resources(resName, report.name, report.message))
        }
      }

    stringToPrintOption match {
      case Some(stringToPrint) => {
        pw.println(stringToPrint)
        report.throwable match {
          case Some(t) => {
            report match {
              case specReport: SpecReport => {
                val sw = new StringWriter
                t.printStackTrace(new PrintWriter(sw))
                val stackTrace = sw.toString
                val indentedStackTrace = PrintReporter.indentStackTrace(stackTrace, specReport.level)
                pw.print(indentedStackTrace) // Do I need a println here? Eyeball it.
              }  
              case _ => t.printStackTrace(pw)
            }
          }
          case None => // do nothing}
        }
        pw.flush()
      }
      case None => // Don't print anything for testStarting if a SpecReport (so long as there was no exception in the testStarting report)
    }
  }
}
 
private object PrintReporter {
  val BufferSize = 4096
  private[scalatest] def indentStackTrace(stackTrace: String, level: Int): String = {
    val indentation = if (level > 0) "  " * level else ""
    val withTabsZapped = stackTrace.replaceAll("\t", "  ")
    val withInitialIndent = indentation + withTabsZapped
    withInitialIndent.replaceAll("\n", "\n" + indentation) // I wonder if I need to worry about alternate line endings. Probably.
  }
}


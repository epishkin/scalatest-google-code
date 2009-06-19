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
import org.scalatest.events._

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

  private def withPossibleLineNumber(stringToPrint: String, throwable: Option[Throwable]): String = {
    throwable match {
      case Some(testFailedException: TestFailedException) =>
        testFailedException.failedTestCodeFileNameAndLineNumberString match {
          case Some(lineNumberString) =>
            Resources("printedReportPlusLineNumber", stringToPrint, lineNumberString)
          case None => stringToPrint
        }
      case _ => stringToPrint
    }
  }

  private def stringsToPrintOnError(noteResourceName: String, errorResourceName: String, message: String, throwable: Option[Throwable],
    formatter: Option[Formatter], suiteName: Option[String], testName: Option[String]): List[String] = {

    val stringToPrint =
      formatter match {
        case Some(IndentedText(formattedText, _, _)) =>
          Resources("specTextAndNote", formattedText, Resources(noteResourceName))
        case _ =>
          // Deny MotionToSuppress directives in error events, because error info needs to be seen by users
          PrintReporter.messageToPrint(errorResourceName, message, throwable, suiteName, testName)
      }

    val stringToPrintWithPossibleLineNumber = withPossibleLineNumber(stringToPrint, throwable)

    def getStackTrace(throwable: Option[Throwable]): List[String] =
      throwable match {
        case Some(throwable) =>
          def stackTrace(throwable: Throwable, isCause: Boolean): List[String] = {
            val className = throwable.getClass.getName 
            val labeledClassName = if (isCause) Resources("DetailsCause") + ": " + className else className
            val labeledClassNameWithMessage =
              if (throwable.getMessage != null && !throwable.getMessage.trim.isEmpty)
                labeledClassName + ": " + throwable.getMessage.trim
              else
                labeledClassName

            val stackTraceElements = throwable.getStackTrace.toList map { "  " + _.toString } // Indent each stack trace item two spaces
            val cause = throwable.getCause

            val stackTraceThisThrowable = labeledClassNameWithMessage :: stackTraceElements
            if (cause == null)
              stackTraceThisThrowable
            else
              stackTraceThisThrowable ::: stackTrace(cause, true) // Not tail recursive, but shouldn't be too deep
          }
          stackTrace(throwable, false)
        case None => List()
      }

    stringToPrintWithPossibleLineNumber :: getStackTrace(throwable)
  }

  private def stringToPrintWhenNoError(resourceName: String, formatter: Option[Formatter], suiteName: String, testName: Option[String]): Option[String] = {

    formatter match {
      case Some(IndentedText(formattedText, _, _)) => Some(formattedText)
      case Some(MotionToSuppress) => None
      case _ =>
        val arg =
          testName match {
            case Some(tn) => suiteName + ": " + tn
            case None => suiteName
          }
        Some(Resources(resourceName + "NoMessage", arg))
    }
  }

  def apply(event: Event) {

    event match {

      case RunStarting(ordinal, testCount, formatter, payload, threadName, timeStamp) => 

        if (testCount < 0)
          throw new IllegalArgumentException
  
        testsCompletedCount = 0
        testsFailedCount = 0
        suitesAbortedCount = 0

        printResourceStringWithInt("runStarting", testCount)

      case RunCompleted(ordinal, duration, summary, formatter, payload, threadName, timeStamp) => 

        makeFinalReport("runCompleted") // TODO: use Summary info

      case RunStopped(ordinal, duration, summary, formatter, payload, threadName, timeStamp) =>

        makeFinalReport("runStopped") // TODO: use Summary info

      case RunAborted(ordinal, message, throwable, duration, summary, formatter, payload, threadName, timeStamp) => 

        val lines = stringsToPrintOnError("abortedNote", "runAborted", message, throwable, formatter, None, None)
        for (line <- lines) pw.println(line)

      case SuiteStarting(ordinal, suiteName, suiteClassName, formatter, rerunnable, payload, threadName, timeStamp) =>

        val stringToPrint = stringToPrintWhenNoError("suiteStarting", formatter, suiteName, None)

        stringToPrint match {
          case Some(string) => pw.println(string)
          case None =>
        }

      case SuiteCompleted(ordinal, suiteName, suiteClassName, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

        val stringToPrint = stringToPrintWhenNoError("suiteCompleted", formatter, suiteName, None)

        stringToPrint match {
          case Some(string) => pw.println(string)
          case None =>
        }

      case SuiteAborted(ordinal, message, suiteName, suiteClassName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

        suitesAbortedCount += 1
        val lines = stringsToPrintOnError("abortedNote", "suiteAborted", message, throwable, formatter, Some(suiteName), None)
        for (line <- lines) pw.println(line)

      case TestStarting(ordinal, suiteName, suiteClassName, testName, formatter, rerunnable, payload, threadName, timeStamp) =>

        val stringToPrint = stringToPrintWhenNoError("testStarting", formatter, suiteName, Some(testName))

        stringToPrint match {
          case Some(string) => pw.println(string)
          case None =>
        }

      case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

        val stringToPrint = stringToPrintWhenNoError("testSucceeded", formatter, suiteName, Some(testName))

        stringToPrint match {
          case Some(string) => pw.println(string)
          case None =>
        }

        testsCompletedCount += 1
    
      case TestIgnored(ordinal, suiteName, suiteClassName, testName, formatter, payload, threadName, timeStamp) => 

        val stringToPrint =
          formatter match {
            case Some(IndentedText(formattedText, _, _)) => Some(Resources("specTextAndNote", formattedText, Resources("ignoredNote")))
            case Some(MotionToSuppress) => None
            case _ => Some(Resources("testIgnoredNoMessage"))
          }

        stringToPrint match {
          case Some(string) => pw.println(string)
          case None =>
        }

      case TestFailed(ordinal, message, suiteName, suiteClassName, testName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

        testsCompletedCount += 1
        testsFailedCount += 1

        val lines = stringsToPrintOnError("failedNote", "testFailed", message, throwable, formatter, Some(suiteName), Some(testName))
        for (line <- lines) pw.println(line)

      case InfoProvided(ordinal, message, nameInfo, throwable, formatter, payload, threadName, timeStamp) =>

        val (suiteName, testName) =
          nameInfo match {
            case Some(NameInfo(suiteName, _, testName)) => (Some(suiteName), testName)
            case None => (None, None)
          }
        val lines = stringsToPrintOnError("infoProvidedNote", "infoProvided", message, throwable, formatter, suiteName, testName)
        for (line <- lines) pw.println(line)

      case _ => throw new RuntimeException("Unhandled event")
    }

    pw.flush()
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
  // private def indent(s: String, times: Int) = if (times <= 0) s else ("  " * times) + s

  // Stupid properties file won't let me put spaces at the beginning of a property
  // "  {0}" comes out as "{0}", so I can't do indenting in a localizable way. For now
  // just indent two space to the left.  //  if (times <= 0) s 
  //  else Resources("indentOnce", indent(s, times - 1))
  
/*
  private def makeReport(report: Report, resourceName: String) {

    if (report == null)
      throw new NullPointerException("report is null")

    val stringToPrintOption: Option[String] = 
      report match {
        case specReport: SpecReport =>
          resourceName match {
            case "testFailed" =>
              if (specReport.includeInSpecOutput)
                Some(Resources("specTextAndNote", specReport.formattedSpecText, Resources("failedNote")))
              else
                None
            case "testIgnored" =>
              if (specReport.includeInSpecOutput)
                Some(Resources("specTextAndNote", specReport.formattedSpecText, Resources("ignoredNote")))
              else
                None
            case _ => 
              if (specReport.includeInSpecOutput)
                Some(specReport.formattedSpecText)
              else
                None
          }
        case _ => {
          val resName = if (report.message.trim.isEmpty) resourceName + "NoMessage" else resourceName
          Some(Resources(resName, report.name, report.message))
        }
      }

    val stringToPrintWithPossibleLineNumberOption: Option[String] = 
      stringToPrintOption match {
        case Some(stringToPrint) =>
          report.throwable match {
            case Some(t: TestFailedException) =>
              t.failedTestCodeFileNameAndLineNumberString match {
                case Some(lineNumberString) =>
                  Some(Resources("printedReportPlusLineNumber", stringToPrint, lineNumberString))
                case None => stringToPrintOption
              }
            case _ => stringToPrintOption
          }
        case None => None
      }

    stringToPrintWithPossibleLineNumberOption match {
      case Some(stringToPrint) => {
        pw.println(stringToPrint)
        report.throwable match {
          case Some(t) => {
            report match {
              case specReport: SpecReport => {
                val sw = new StringWriter
                t.printStackTrace(new PrintWriter(sw))
                val stackTrace = sw.toString
                val indentedStackTrace = PrintReporter.indentStackTrace(stackTrace, 1) // Darn forgot about indenting stack traces
                pw.print(indentedStackTrace) // Do I need a println here? Eyeball it.
              }  
              case _ => t.printStackTrace(pw)
            }
          }
          case None => // do nothing
        }
        pw.flush()
      }
      case None => // Don't print anything for TestStarting if a SpecReport (so long as there was no exception in the TestStarting report)
    }
  }
*/
}
 
private object PrintReporter {
  val BufferSize = 4096
  private[scalatest] def indentStackTrace(stackTrace: String, level: Int): String = {
    val indentation = if (level > 0) "  " * level else ""
    val withTabsZapped = stackTrace.replaceAll("\t", "  ")
    val withInitialIndent = indentation + withTabsZapped
    withInitialIndent.replaceAll("\n", "\n" + indentation) // I wonder if I need to worry about alternate line endings. Probably.
  }

  // In the unlikely event that a message is blank, use the throwable's detail message
  private[scalatest] def messageOrThrowablesDetailMessage(message: String, throwable: Option[Throwable]): String = {
    val trimmedMessage = message.trim
    if (!trimmedMessage.isEmpty)
      trimmedMessage
    else
      throwable match {
        case Some(t) => t.getMessage.trim
        case None => ""
      }
  }

  private[scalatest] def messageToPrint(resourceName: String, message: String, throwable: Option[Throwable], suiteName: Option[String],
    testName: Option[String]): String = {

    val arg =
      suiteName match {
        case Some(sn) =>
          testName match {
            case Some(tn) => sn + ": " + tn
            case None => sn
          }
        case None => ""
      }

    val msgToPrint = messageOrThrowablesDetailMessage(message, throwable)
    if (msgToPrint.isEmpty)
      Resources(resourceName + "NoMessage", arg)
    else
      if (resourceName == "runAborted")
        Resources(resourceName, msgToPrint)
      else
        Resources(resourceName, arg, msgToPrint)
  }
}


/*
 * Copyright 2001-2011 Artima, Inc.
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
package org.scalatest.tools

import org.scalatest.events._
import org.scalatest.Reporter
import org.scalatest.events.MotionToSuppress
import org.scalatest.StackDepthException

import java.io.PrintWriter
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.File
import java.util.Date
import java.text.SimpleDateFormat

import scala.collection.mutable.Stack
import scala.collection.mutable.ListBuffer
import scala.xml.XML
import scala.xml.NodeSeq
import scala.xml.Elem
import scala.xml.Node

import com.github.rjeschke.txtmark.Processor

/**
 * A <code>Reporter</code> that writes test status information in xml format
 * for use by Flex formatter.
 */
private[scalatest] class FlexReporter(directory: String) extends Reporter {
  final val BufferSize = 4096

  private val events = ListBuffer[Event]()
  private var index = 0
  private val timestamp =
    new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date)

  private val runsDir      = new File(directory + "/runs")
  private val durationsDir = new File(directory + "/durations")
  private val summariesDir = new File(directory + "/summaries")
  private val summaryFile  = new File(directory + "/summary.xml")
  private val thisRunFile  = new File(runsDir, "run-" + timestamp + ".xml")

  runsDir.mkdir()
  durationsDir.mkdir()
  summariesDir.mkdir()

  //
  // Records events as they are received.  Initiates processing once
  // a run-termination event comes in.
  //
  def apply(event: Event) {
    event match {
      case _: RunStarting  =>
      case _: RunCompleted => writeFiles(event)
      case _: RunStopped   => writeFiles(event)
      case _: RunAborted   => writeFiles(event)
      case _ => events += event
    }
  }

  //
  // Provides sequential index values for xml entries.
  //
  def nextIndex(): Int = {
    index += 1
    index
  }

  //
  // Throws exception for specified unexpected event.
  //
  def unexpectedEvent(e: Event) {
    throw new RuntimeException("unexpected event [" + e + "]")
  }

  //
  // Escapes html entities and curly braces in specified string.
  //
  def escape(s: String): String =
    scala.xml.Utility.escape(s).
      replaceAll("""\{""", """\\{""").
      replaceAll("""\}""", """\\}""")

  //
  // Formats date for inclusion in as 'date' attribute in xml.
  //
  // E.g.: "Mon May 30 10:29:58 PDT 2011"
  //
  def formatDate(timeStamp: Long): String = {
    val df = new SimpleDateFormat("EEE MMM d kk:mm:ss zzz yyyy")
    df.format(new Date(timeStamp))
  }

  def markdownToHtml(markdown: String): String = {
      Processor.process(markdown)
  }

  //
  // Writes flex reporter summary, duration, and run files at completion
  // of a run.  Archives old copies of summary and duration files into
  // summaries/ and durations/ subdirectories.
  //
  def writeFiles(terminatingEvent: Event) {
    writeRunFile(terminatingEvent)
    writeSummaryFile(terminatingEvent)
  }

  //
  // Writes the summary.xml file and archives the previous copy.
  //
  def writeSummaryFile(terminatingEvent: Event) {
    val SummaryTemplate =
      """|<summary>
         |  <runs>
         |$runs$
         |  </runs>
         |  <regressions>
         |  </regressions>
         |  <recentlySlower>
         |  </recentlySlower>
         |</summary>
         |""".stripMargin

    //
    // Formats a <run> element of summary file.
    //
    def formatRun(id: String, succeeded: String, failed: String,
                  ignored: String, canceled: String, pending: String): String =
    {
      "    <run "     +
      "id=\""         + id         + "\" " +
      "succeeded=\""  + succeeded  + "\" " +
      "failed=\""     + failed     + "\" " +
      "ignored=\""    + ignored    + "\" " +
      "canceled=\""   + canceled   + "\" " +
      "pending=\""    + pending    + "\" " + "/>\n"
    }

    //
    // Generates the summary file <run> element for the current run.
    //
    def genThisRun(terminatingEvent: Event): String = {
      val summaryOption = 
        terminatingEvent match {
          case e: RunCompleted => e.summary
          case e: RunAborted   => e.summary
          case e: RunStopped   => e.summary
          case _ => unexpectedEvent(terminatingEvent); None
        }
  
      val summary  = summaryOption.getOrElse(Summary(0, 0, 0, 0, 0, 0, 0))
  
      formatRun(timestamp,
                "" + summary.testsSucceededCount,
                "" + summary.testsFailedCount,
                "" + summary.testsIgnoredCount,
                "" + summary.testsCanceledCount,
                "" + summary.testsPendingCount)
    }

    //
    // Formats <run> elements for previous runs.
    //
    def formatOldRuns(oldRunsXml: NodeSeq): String = {
      val buf = new StringBuilder

      for (run <- oldRunsXml) {
        val id        = "" + (run \ "@id")
        val succeeded = "" + (run \ "@succeeded")
        val failed    = "" + (run \ "@failed")
        val ignored   = "" + (run \ "@ignored")
        val canceled  = "" + (run \ "@canceled")
        val pending   = "" + (run \ "@pending")

        buf.append(
          formatRun(id, succeeded, failed, ignored, canceled, pending))
      }
      buf.toString
    }

    //
    // Reads existing summary.xml file, or, if none exists, returns a <summary>
    // xml containing all empty elements.
    //
    def getOldSummaryXml: Elem = {
      if (summaryFile.exists)
        XML.loadFile(summaryFile)
      else
        <summary>
          <runs/>
          <regressions/>
          <recentlySlower/>
        </summary>
    }

    //
    // If a summary file containing previous run histories exists, moves
    // it to the summaries/ subdirectory and renames it to a filename
    // containing the timestamp of the most recent run the file contains.
    //
    def archiveOldSummaryFile(oldRunsXml: NodeSeq) {
      if (summaryFile.exists) {
        if (oldRunsXml.size > 0) {
          val lastRunId = oldRunsXml(0) \ "@id"
          summaryFile.renameTo(
            new File(summariesDir + "/summary-" + lastRunId + ".xml"))
        }
      }
    }

    def getThisRunXml: Elem = {
      XML.loadFile(thisRunFile)
    }

    def genRegressions(oldRegressionsXml: NodeSeq, thisRunXml: Elem): String =
    {
      def getOldRegression(suite: Node, test: Node): Option[Node] = {
        val matches =
          oldRegressionsXml.filter(
            node => node \ "@testName" == test \ "@name")

        if (matches.size > 0)
          Some(matches(0))
        else
          None
      }

      //
      // genRegressionsMain
      //
      val suites = thisRunXml \\ "suite"

      for (suite <- suites) {
        val tests = suite \ "test"

        for (test <- tests) {
          val result = test \ "result"

          if (result != "passed") {
            val oldRegression = getOldRegression(suite, test)
          }
        }
      }
      "genRegressions output"
    }

    //
    // writeSummaryFile main
    //
    val oldSummaryXml = getOldSummaryXml
    val thisRunXml    = getThisRunXml

    val oldRunsXml        = oldSummaryXml \\ "run"
    val oldRegressionsXml = oldSummaryXml \\ "regression"

    archiveOldSummaryFile(oldRunsXml)

    val thisRun = genThisRun(terminatingEvent)
    val oldRuns = formatOldRuns(oldRunsXml)

    val regressions = genRegressions(oldRegressionsXml, thisRunXml)

    val summaryText =
      SummaryTemplate.replaceFirst("""\$runs\$""", thisRun + oldRuns)

    writeFile("summary.xml", summaryText)
  }

  //
  // Writes specified text to specified file in output directory.
  //
  def writeFile(filename: String, text: String) {
    val out = new PrintWriter(directory + "/" + filename)
    out.print(text)
    out.close()
  }

  //
  // Writes timestamped output file to 'runs' subdirectory beneath specified
  // output dir.  Format of file name is, e.g. for timestamp
  // "2011-01-07-143216", "run-2011-01-07-143216.xml".
  //
  def writeRunFile(event: Event) {
    index = 0
    var suiteRecord: SuiteRecord = null
    val stack = new Stack[SuiteRecord]
    val pw =
      new PrintWriter(
        new BufferedOutputStream(
          new FileOutputStream(thisRunFile), BufferSize))

    //
    // Formats <summary> element of output xml.
    //
    def formatSummary(event: Event): String = {
      val (summaryOption, durationOption) =
        event match {
          case e: RunCompleted => (e.summary, e.duration)
          case e: RunAborted   => (e.summary, e.duration)
          case e: RunStopped   => (e.summary, e.duration)
          case _ => unexpectedEvent(event); (None, None)
        }

      val summary  = summaryOption.getOrElse(Summary(0, 0, 0, 0, 0, 0, 0))
      val duration = durationOption.getOrElse(0)

      "<summary index=\"" + nextIndex() + "\" text=\"\" " +
      "duration=\""             + duration                     + "\" " +
      "testsSucceededCount=\""  + summary.testsSucceededCount  + "\" " +
      "testsFailedCount=\""     + summary.testsFailedCount     + "\" " +
      "testsIgnoredCount=\""    + summary.testsIgnoredCount    + "\" " +
      "testsPendingCount=\""    + summary.testsPendingCount    + "\" " +
      "testsCancelledCount=\""  + summary.testsCanceledCount   + "\" " +
      "suitesCompletedCount=\"" + summary.suitesCompletedCount + "\" " +
      "suitesAbortedCount=\""   + summary.suitesAbortedCount   + "\" " +
      "date=\""                 + formatDate(event.timeStamp)  + "\" " +
      "thread=\""               + event.threadName             + "\"/>\n"
    }

    //
    // Closes out a SuiteRecord.  Gets called upon receipt of a
    // SuiteCompleted or SuiteAborted event.
    //
    // If the suite being closed is nested within another suite, its
    // completed record is added to the record of the suite it is nested
    // in.  Otherwise its xml is written to the output file.
    //
    def endSuite(e: Event) {
      suiteRecord.addEndEvent(e)

      val prevRecord = stack.pop()

      if (prevRecord != null)
        prevRecord.addNestedElement(suiteRecord)
      else
        pw.print(suiteRecord.toXml)

      suiteRecord = prevRecord
    }

    //
    // writeRunFile main
    //
    pw.println("<doc>")
    pw.print(formatSummary(event))

    val sortedEvents = events.toList.sortWith((a, b) => a.ordinal < b.ordinal)

    for (event <- sortedEvents) {
      event match {
        case e: SuiteStarting  =>
          stack.push(suiteRecord)
          suiteRecord = new SuiteRecord(e)
          
        case e: InfoProvided   => suiteRecord.addNestedElement(e)
        case e: ScopeOpened    => suiteRecord.addNestedElement(e)
        case e: ScopeClosed    => suiteRecord.addNestedElement(e)
        case e: MarkupProvided => suiteRecord.addNestedElement(e)
        case e: TestStarting   => suiteRecord.addNestedElement(e)
        case e: TestSucceeded  => suiteRecord.addNestedElement(e)
        case e: TestIgnored    => suiteRecord.addNestedElement(e)
        case e: TestFailed     => suiteRecord.addNestedElement(e)
        case e: TestPending    => suiteRecord.addNestedElement(e)
        case e: TestCanceled   => suiteRecord.addNestedElement(e)

        case e: SuiteCompleted => endSuite(e)
        case e: SuiteAborted   => endSuite(e)

        case e: RunStarting  => unexpectedEvent(e)
        case e: RunCompleted => unexpectedEvent(e)
        case e: RunStopped   => unexpectedEvent(e)
        case e: RunAborted   => unexpectedEvent(e)
      }
    }
    pw.println("</doc>")
    pw.flush()
    pw.close()
  }

  //
  // Generates xml for an InfoProvided event.
  //
  def formatInfoProvided(event: InfoProvided): String = {
    "<info index=\"" + nextIndex()                 + "\" " +
    "text=\""        + escape(event.message)       + "\" " +
    "thread=\""      + event.threadName            + "\"/>\n"
  }

  //
  // Generates xml for ScopeOpened event.
  //
  def formatScopeOpened(event: ScopeOpened): String = {
    "<info index=\"" + nextIndex()                 + "\" " +
    "text=\""        + escape(event.message)       + "\" " +
    "thread=\""      + event.threadName            + "\">\n"
  }

  //
  // Generates xml for a MarkupProvided event.
  //
  def formatMarkupProvided(event: MarkupProvided): String = {
    "<markup index=\"" + nextIndex()                 + "\" "   +
    "thread=\""        + event.threadName            + "\">\n" +
    "<data><![CDATA["  + markdownToHtml(event.text)  + "]]></data>\n" +
    "</markup>\n"
  }

  //
  // Generates xml for a TestIgnored event.
  //
  def formatTestIgnored(event: TestIgnored): String = {
    "<test index=\"" + nextIndex() + "\" " +
    "result=\"ignored\" " +
    "text=\"" + testMessage(event.testName, event.formatter) + "\" " +
    "name=\"" + escape(event.testName) + "\" " +
    "thread=\"" + event.threadName + "\"" +
    "/>\n"
  }

  //
  // Extracts message from specified formatter if there is one, otherwise
  // returns test name.
  //
  def testMessage(testName: String, formatter: Option[Formatter]): String = {
    val message =
      formatter match {
        case Some(IndentedText(_, rawText, _)) => rawText
        case _ => testName
      }
    escape(message)
  }

  //
  // Class that aggregates events that make up a suite.
  //
  // Holds all the events encountered from SuiteStarting through its
  // corresponding end event (e.g. SuiteCompleted).  Once the end event
  // is received, this class's toXml method can be called to generate the
  // complete xml string for the <suite> element.
  //
  class SuiteRecord(startEvent: SuiteStarting) {
    var nestedElements = List[Any]()
    var endEvent: Event = null

    //
    // Adds either an Event or a nested SuiteRecord to this object's
    // list of elements.
    //
    def addNestedElement(element: Any) {
      nestedElements ::= element
    }

    //
    // Adds suite closing event (SuiteCompleted or SuiteAborted) to the
    // object.
    //
    def addEndEvent(event: Event) {
      def isEndEvent(e: Event): Boolean = {
        e match {
          case _: SuiteCompleted => true
          case _: SuiteAborted   => true
          case _ => false
        }
      }

      require(endEvent == null)
      require(isEndEvent(event))

      endEvent = event
    }

    //
    // Generates value to be used in <suite> element's 'result' attribute.
    //
    def result: String = {
      endEvent match {
        case _: SuiteCompleted => "completed"
        case _: SuiteAborted   => "aborted"
        case _ => unexpectedEvent(endEvent); ""
      }
    }

    //
    // Generates xml string representation of object.
    //
    def toXml: String = {
      val buf = new StringBuilder
      var testRecord: TestRecord = null

      //
      // Generates opening <suite ...> element
      //
      def formatStartOfSuite: String = {
        val duration = endEvent.timeStamp - startEvent.timeStamp
        "\n" +
        "<suite index=\"" + nextIndex()                   + "\" " +
        "result=\""       + result                        + "\" " +
        "name=\""         + escape(startEvent.suiteName)  + "\" " +
        "duration=\""     + duration                      + "\" " +
        "thread=\""       + startEvent.threadName         + "\">\n"
      }

      //
      // Indicates whether a test record is currently open during
      // event processing.
      //
      def inATest: Boolean =
        (testRecord != null) && (testRecord.endEvent == null)

      //
      // toXml main
      //
      if (startEvent.suiteName != "DiscoverySuite")
        buf.append(formatStartOfSuite)

      for (element <- nestedElements.reverse) {
        if (inATest) {
          testRecord.addEvent(element.asInstanceOf[Event])

          if (testRecord.isComplete)
            buf.append(testRecord.toXml)
        }
        else {
          element match {
            case e: InfoProvided   => buf.append(formatInfoProvided(e))
            case e: ScopeOpened    => buf.append(formatScopeOpened(e))
            case e: ScopeClosed    => buf.append("</info>\n")
            case e: MarkupProvided => buf.append(formatMarkupProvided(e))
            case e: TestIgnored    => buf.append(formatTestIgnored(e))
            case e: SuiteRecord    => buf.append(e.toXml)
            case e: TestStarting   => testRecord = new TestRecord(e)
            case _ =>
              throw new RuntimeException("unexpected [" + element + "]")
          }
        }
      }
      if (startEvent.suiteName != "DiscoverySuite")
        buf.append("</suite>\n")

      buf.toString
    }
  }

  //
  // Class that aggregates events that make up a test.
  //
  // Holds all the events encountered from TestStarting through its
  // corresponding end event (e.g. TestSucceeded).  Once the end event
  // is received, this class's toXml method can be called to generate
  // the complete xml string for the <test> element.
  //
  class TestRecord(startEvent: TestStarting) {
    var nestedEvents = List[Event]()
    var endEvent: Event = null

    //
    // Adds specified event to object's list of nested events.
    //
    def addEvent(event: Event) {
      def isNestedEvent: Boolean = {
        event match {
          case _: InfoProvided => true
          case _: ScopeOpened => true
          case _: ScopeClosed => true
          case _: MarkupProvided => true
          case _ => false
        }
      }

      def isEndEvent: Boolean = {
        event match {
          case _: TestSucceeded => true
          case _: TestFailed => true
          case _: TestPending => true
          case _: TestCanceled => true
          case _ => false
        }
      }

      if (isNestedEvent)
        nestedEvents ::= event
      else if (isEndEvent)
        endEvent = event
      else
        unexpectedEvent(event)
    }

    //
    // Indicates whether an end event has been received yet for this
    // record.
    //
    def isComplete: Boolean = (endEvent != null)

    //
    // Generates value for use as 'result' attribute of <test> element.
    //
    def result: String = {
      endEvent match {
        case _: TestSucceeded => "passed"
        case _: TestFailed    => "failed"
        case _: TestPending   => "pending"
        case _: TestCanceled  => "canceled"
        case _ => unexpectedEvent(endEvent); ""
      }
    }

    //
    // Generates initial <test> element of object's xml.
    //
    def formatTestStart: String = {
      val duration = endEvent.timeStamp - startEvent.timeStamp

      "<test index=\"" + nextIndex()                 + "\" " +
      "result=\""      + result                      + "\" " +
      "text=\""        + testMessage(startEvent.testName, endEvent.formatter) +
      "\" " +
      "name=\""        + escape(startEvent.testName) + "\" " +
      "duration=\""    + duration                    + "\" " +
      "thread=\""      + startEvent.threadName       + "\"" +
      ">\n"
    }

    //
    // Generates <exception> xml for a test failure.
    //
    def formatException(event: TestFailed): String = {
      val buf = new StringBuilder
      var depth = -1

      def nextDepth: Int = {
        depth += 1
        depth
      }

      buf.append("<exception ")

      if (event.suiteClassName.isDefined)
        buf.append("className=\"" + event.suiteClassName.get + "\"")

      buf.append(">\n")
      
      if (event.throwable.isDefined) {
        val throwable = event.throwable.get
        val stackTrace = throwable.getStackTrace
        require(stackTrace.size > 0)

        buf.append("<message>" + event.message + "</message>\n")

        if (throwable.isInstanceOf[StackDepthException]) {
          val sde = throwable.asInstanceOf[StackDepthException]

          if (sde.failedCodeFileName.isDefined &&
              sde.failedCodeLineNumber.isDefined)
          {
            buf.append(
              "<stackDepth>\n" +
              "<depth>" + sde.failedCodeStackDepth + "</depth>\n" +
              "<fileName>" + sde.failedCodeFileName.get + "</fileName>\n" +
              "<lineNumber>" +
                sde.failedCodeLineNumber.get +
              "</lineNumber>\n" +
              "</stackDepth>\n")
          }
        }

        buf.append("<stackTrace>\n")
        for (frame <- stackTrace) {
          buf.append(
            "<stackFrame depth=\"" + nextDepth + "\">" +
              frame.getClassName + "(" + frame.getFileName + ":" +
              frame.getLineNumber + ")" +
            "</stackFrame>\n")
        }
        buf.append("</stackTrace>\n")
      }
      buf.append("</exception>\n")




      buf.toString
    }

    //
    // Generates xml string representation of object.
    //
    def toXml: String = {
      val buf = new StringBuilder

      if (endEvent == null)
        throw new IllegalStateException("toXml called without endEvent")

      buf.append(formatTestStart)

      for (event <- nestedEvents) {
        event match {
          case e: InfoProvided   => buf.append(formatInfoProvided(e))
          case e: ScopeOpened    => buf.append(formatScopeOpened(e))
          case e: ScopeClosed    => buf.append("</info>\n")
          case e: MarkupProvided => buf.append(formatMarkupProvided(e))
          case _ => unexpectedEvent(event)
        }
      }

      if (endEvent.isInstanceOf[TestFailed])
        buf.append(formatException(endEvent.asInstanceOf[TestFailed]))

      buf.append("</test>\n")

      buf.toString
    }
  }
}


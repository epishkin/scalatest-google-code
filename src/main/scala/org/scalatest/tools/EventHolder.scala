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
package org.scalatest.tools

import org.scalatest._
import org.scalatest.events._
import EventHolder.suiteAndTestName

/**
 * Used to hold Reports in the GUI, so that I can keep track of which report method was called
 * on the reporter to deliver it.
 *
 * @author Bill Venners
 */
private[tools] class EventHolder(val event: Event, val message: Option[String], val throwable: Option[Throwable],
    val rerunner: Option[Rerunner], val summary: Option[Summary], val isRerun: Boolean) {

  if (event == null || message == null || throwable == null || rerunner == null || summary == null)
    throw new NullPointerException()
 
  def this(event: Event, message: Option[String], throwable: Option[Throwable],
      rerunner: Option[Rerunner]) = this(event, message, throwable, rerunner, None, false)

  def this(event: Event, message: Option[String], throwable: Option[Throwable],
      rerunner: Option[Rerunner], summary: Option[Summary]) = this(event, message, throwable, rerunner, summary, false)

  override def toString = {
    event.formatter match {
      case Some(IndentedText(_, rawText, indentationLevel)) =>
        event match {
          case _: SuiteStarting => rawText + ":"
          case _: TestPending => Resources("specTextAndNote", rawText, Resources("pendingNote"))
          case _ => rawText
        }
      case _ =>
        val firstString: String =
          if (isRerun)
            Resources("RERUN_" + RunnerJFrame.getUpperCaseName(event))
          else
            Resources(RunnerJFrame.getUpperCaseName(event))

        def firstAndSecondString(first: String, second: String) = first + " - " + second

        event match {
          case event: RunStarting => firstString
          case event: RunStopped => firstString
          case event: RunAborted => firstString
          case event: RunCompleted => firstString
          case event: ScopeOpened => firstString + " - " + event.message
          case event: ScopeClosed => firstString
          case event: InfoProvided => firstString + " - " + event.message
          case event: MarkupProvided => firstString + " - " + event.text
          case event: SuiteStarting => firstAndSecondString(firstString, event.suiteName)
          case event: SuiteCompleted => firstAndSecondString(firstString, event.suiteName)
          case event: SuiteAborted => firstAndSecondString(firstString, event.suiteName)
          case event: TestStarting => firstAndSecondString(firstString, suiteAndTestName(event.suiteName, event.decodedSuiteName, event.testName, event.decodedTestName))
          case event: TestPending => firstAndSecondString(firstString, suiteAndTestName(event.suiteName, event.decodedSuiteName, event.testName, event.decodedTestName))
          case event: TestIgnored => firstAndSecondString(firstString, suiteAndTestName(event.suiteName, event.decodedSuiteName, event.testName, event.decodedTestName))
          case event: TestSucceeded => firstAndSecondString(firstString, suiteAndTestName(event.suiteName, event.decodedSuiteName, event.testName, event.decodedTestName))
          case event: TestFailed => firstAndSecondString(firstString, suiteAndTestName(event.suiteName, event.decodedSuiteName, event.testName, event.decodedTestName))
          case event: TestCanceled => firstAndSecondString(firstString, suiteAndTestName(event.suiteName, event.decodedSuiteName, event.testName, event.decodedTestName))
        }
    }
  }
}

private[tools] object EventHolder {
  
  def makeSuiteName(suiteName: String, decodedSuiteName:Option[String]) = suiteName + 
                                                                      (decodedSuiteName match {
                                                                      	case Some(name) => " (" + name + ")"
                                                                      	case None => ""
                                                                      })
  
  def makeTestName(testName: String, decodedTestName:Option[String]) = testName + 
                                                                   (decodedTestName match {
                                                                     case Some(name) => " (" + name + ")"
                                                                     case None => ""
                                                                   })
  
  def suiteAndTestName(suiteName: String, decodedSuiteName:Option[String], testName: String, decodedTestName:Option[String]) = makeSuiteName(suiteName, decodedSuiteName) + ": " + makeTestName(testName, decodedTestName)
}

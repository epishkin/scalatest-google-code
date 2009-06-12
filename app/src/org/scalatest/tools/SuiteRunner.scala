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

import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

private[scalatest] class SuiteRunner(suite: Suite, dispatchReporter: DispatchReporter, stopRequested: Stopper, includes: Set[String],
    excludes: Set[String], propertiesMap: Map[String, Any], distributor: Option[Distributor]) extends Runnable {

  def run() {

    if (!stopRequested()) {
      // Create a Rerunner if the Suite has a no-arg constructor
      val hasPublicNoArgConstructor: Boolean =
        try {
          val constructor: Constructor[_] = suite.getClass.getConstructor(Array[java.lang.Class[_]](): _*)
          Modifier.isPublic(constructor.getModifiers())
        }
        catch {
          case nsme: NoSuchMethodException => false
        }
  
      val rerunnable: Option[Rerunner] =
        if (hasPublicNoArgConstructor)
          Some(new SuiteRerunner(suite.getClass.getName))
        else
          None
  
      val rawString = Resources("suiteExecutionStarting")
      val report =
        suite match {
          case spec: Spec =>
            //new SpecReport(suite.suiteName, rawString, suite.suiteName, suite.suiteName, true, Some(suite.suiteName), Some(suite.getClass.getName), None, None, rerunnable)
            new SpecReport(suite.suiteName, rawString, suite.suiteName, suite.suiteName, true, None, rerunnable)
          case _ =>
            //new Report(suite.suiteName, rawString, Some(suite.suiteName), Some(suite.getClass.getName), None, None, rerunnable)
            new Report(suite.suiteName, rawString, None, rerunnable)
        }
  
      dispatchReporter.suiteStarting(report)
  
      try {
        suite.execute(None, dispatchReporter, stopRequested, includes, excludes, propertiesMap, distributor)
  
        val rawString2 = Resources("suiteCompletedNormally")
  
        val report2 =
          suite match {
            case spec: Spec =>
              //new SpecReport(suite.suiteName, rawString2, suite.suiteName, suite.suiteName, false, Some(suite.suiteName), Some(suite.getClass.getName), None, None, rerunnable)
              new SpecReport(suite.suiteName, rawString2, suite.suiteName, suite.suiteName, false, None, rerunnable)
            case _ =>
              // new Report(suite.suiteName, rawString2, Some(suite.suiteName), Some(suite.getClass.getName), None, None, rerunnable)
              new Report(suite.suiteName, rawString2, None, rerunnable)
          }
  
        dispatchReporter.suiteCompleted(report2)
      }
      catch {
        case e: RuntimeException => {
          val rawString3 = Resources("executeException")
  
          val report3 =
            suite match {
              case spec: Spec =>
                //new SpecReport(suite.suiteName, rawString3, suite.suiteName, suite.suiteName, true, Some(suite.suiteName), Some(suite.getClass.getName), None, Some(e), rerunnable)
                new SpecReport(suite.suiteName, rawString3, suite.suiteName, suite.suiteName, true, Some(e), rerunnable)
              case _ =>
                //new Report(suite.suiteName, rawString3, Some(suite.suiteName), Some(suite.getClass.getName), None, Some(e), rerunnable)
                new Report(suite.suiteName, rawString3, Some(e), rerunnable)
            }
  
          dispatchReporter.suiteAborted(report3)
        }
      }
    }
  }
}

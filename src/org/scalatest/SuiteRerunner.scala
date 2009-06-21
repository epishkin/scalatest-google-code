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

import org.scalatest.Suite.checkForPublicNoArgConstructor

import org.scalatest.events._
import Suite.formatterForSuiteStarting
import Suite.formatterForSuiteCompleted
import Suite.formatterForSuiteAborted

/**
 * A Rerunner for Suites.
 *
 * @author Bill Venners
 */
private[scalatest] class SuiteRerunner(suiteClassName: String) extends Rerunner {

  if (suiteClassName == null)
    throw new NullPointerException

  def apply(report: Reporter, stopRequested: Stopper, includes: Set[String], excludes: Set[String],
            goodies: Map[String, Any], distributor: Option[Distributor], tracker: Tracker, loader: ClassLoader) {

    try {
      val suiteClass = loader.loadClass(suiteClassName)
      val suite = suiteClass.newInstance().asInstanceOf[Suite]
      val expectedTestCount = suite.expectedTestCount(includes, excludes)

      // Create a Rerunner if the Suite has a public no-arg constructor
      val rerunnable =
        if (Suite.checkForPublicNoArgConstructor(suite.getClass))
          Some(new SuiteRerunner(suite.getClass.getName))
        else
          None

      report(RunStarting(tracker.nextOrdinal(), expectedTestCount))

      try {

        val rawString = Resources("suiteExecutionStarting")
        val formatter = formatterForSuiteStarting(suite)

        report(SuiteStarting(tracker.nextOrdinal(), suite.suiteName, Some(suite.getClass.getName), formatter, rerunnable))

        suite.run(None, report, stopRequested, includes, excludes, goodies, distributor, tracker)

        val rawString2 = Resources("suiteCompletedNormally")
        val formatter2 = formatterForSuiteCompleted(suite)

        report(SuiteCompleted(tracker.nextOrdinal(), suite.suiteName, Some(suite.getClass.getName), None, formatter2, rerunnable)) // TODO: add a duration
      }
      catch {
        case e: RuntimeException => {
          val rawString3 = Resources("executeException")
          val formatter3 = formatterForSuiteAborted(suite, rawString3)

          report(SuiteAborted(tracker.nextOrdinal(), rawString3, suite.suiteName, Some(suite.getClass.getName), Some(e), None, formatter3, rerunnable)) // TODO: add a duration
        }
      }
      
      if (stopRequested()) {
        report(RunStopped(tracker.nextOrdinal()))
      }
      else {
        report(RunCompleted(tracker.nextOrdinal())) // TODO: pass a duration
      }
    }
    catch {
      case e: ClassNotFoundException => {
        report(RunAborted(tracker.nextOrdinal(), Resources("cannotLoadSuite", e.getMessage), Some(e)))
      }
      case e: InstantiationException => {
        report(RunAborted(tracker.nextOrdinal(), Resources("cannotInstantiateSuite", e.getMessage), Some(e)))
      }
      case e: IllegalAccessException => {
        report(RunAborted(tracker.nextOrdinal(), Resources("cannotInstantiateSuite", e.getMessage), Some(e)))
      }
      case e: SecurityException => {
        report(RunAborted(tracker.nextOrdinal(), Resources("securityWhenRerruning", e.getMessage), Some(e)))
      }
      case e: NoClassDefFoundError => {
        // Suggest the problem might be a bad runpath
        // Maybe even print out the current runpath
        report(RunAborted(tracker.nextOrdinal(), Resources("cannotLoadClass", e.getMessage), Some(e)))
      }
      case e: Throwable => {
        report(RunAborted(tracker.nextOrdinal(), Resources.bigProblems(e), Some(e)))
      }
    }
  }
}

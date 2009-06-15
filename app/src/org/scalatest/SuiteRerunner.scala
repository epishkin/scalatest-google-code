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

/**
 * A Rerunner for Suites.
 *
 * @author Bill Venners
 */
private[scalatest] class SuiteRerunner(suiteClassName: String) extends Rerunner {

  if (suiteClassName == null)
    throw new NullPointerException

  def apply(report: Reporter, stopRequested: Stopper, includes: Set[String], excludes: Set[String],
            goodies: Map[String, Any], distributor: Option[Distributor], firstOrdinal: Ordinal, loader: ClassLoader) {

    var ordinal = firstOrdinal

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

      report(RunStarting(ordinal, expectedTestCount))
      ordinal = ordinal.next

      try {

        val rawString = Resources("suiteExecutionStarting")
/*
        val rpt =
          suite match {
            case spec: Spec =>
              new SpecReport(suite.suiteName, rawString, suite.suiteName, suite.suiteName, true, None, rerunnable)
            case _ =>
              new Report(suite.suiteName, rawString, None, rerunnable)
          }
*/
        val formatter =
          suite match {
            case spec: Spec => Some(IndentedText(rawString, rawString, 0))
            case _ => None
          }
        report(SuiteStarting(ordinal, suite.suiteName, Some(suite.getClass.getName), formatter, rerunnable))

        suite.run(None, report, stopRequested, includes, excludes, goodies, distributor, ordinal)

        val rawString2 = Resources("suiteCompletedNormally")
/*
        val rpt2 =
          suite match {
            case spec: Spec =>
              new SpecReport(suite.suiteName, rawString2, suite.suiteName, suite.suiteName, false, None, rerunnable)
            case _ =>
              new Report(suite.suiteName, rawString2, None, rerunnable)
          }
*/
        val formatter2 =
          suite match {
            case spec: Spec => Some(MotionToSuppress)
            case _ => None
          }
        report(SuiteCompleted(ordinal, suite.suiteName, Some(suite.getClass.getName), None, formatter2, rerunnable)) // TODO: add a duration
      }
      catch {
        case e: RuntimeException => {
          val rawString3 = Resources("executeException")
/*
          val rpt3 =
            suite match {
              case spec: Spec =>
                new SpecReport(suite.suiteName, rawString3, suite.suiteName, suite.suiteName, true, Some(e), rerunnable)
              case _ =>
                new Report(suite.suiteName, rawString3, Some(e), rerunnable)
            }
*/
          val formatter3 =
            suite match {
              case spec: Spec => Some(IndentedText(rawString3, rawString3, 0))
              case _ => None
            }
          report(SuiteAborted(ordinal, rawString3, suite.suiteName, Some(suite.getClass.getName), Some(e), None, formatter3, rerunnable)) // TODO: add a duration
          ordinal = ordinal.next
        }
      }
      
      if (stopRequested()) {
        report(RunStopped(ordinal))
        // Don't need to increment ordinal, because it isn't used after this
      }
      else {
        report(RunCompleted(ordinal)) // TODO: pass a duration
        // Don't need to increment ordinal, because it isn't used after this
      }
    }
    catch {
      case e: ClassNotFoundException => {
        report(RunAborted(ordinal, Resources("cannotLoadSuite", e.getMessage), Some(e)))
        // Don't need to increment ordinal, because it isn't used after this
      }
      case e: InstantiationException => {
        report(RunAborted(ordinal, Resources("cannotInstantiateSuite", e.getMessage), Some(e)))
        // Don't need to increment ordinal, because it isn't used after this
      }
      case e: IllegalAccessException => {
        report(RunAborted(ordinal, Resources("cannotInstantiateSuite", e.getMessage), Some(e)))
        // Don't need to increment ordinal, because it isn't used after this
      }
      case e: SecurityException => {
        report(RunAborted(ordinal, Resources("securityWhenRerruning", e.getMessage), Some(e)))
        // Don't need to increment ordinal, because it isn't used after this
      }
      case e: NoClassDefFoundError => {
        // Suggest the problem might be a bad runpath
        // Maybe even print out the current runpath
        report(RunAborted(ordinal, Resources("cannotLoadClass", e.getMessage), Some(e)))
        // Don't need to increment ordinal, because it isn't used after this
      }
      case e: Throwable => {
        report(RunAborted(ordinal, Resources.bigProblems(e), Some(e)))
        // Don't need to increment ordinal, because it isn't used after this
      }
    }
  }
}

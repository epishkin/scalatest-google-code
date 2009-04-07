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

/**
 * A Rerunner for Suites.
 *
 * @author Bill Venners
 */
private[scalatest] class SuiteRerunner(suiteClassName: String) extends Rerunnable {

  if (suiteClassName == null)
    throw new NullPointerException

  def rerun(reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
            properties: Map[String, Any], distributor: Option[Distributor], loader: ClassLoader) {

    try {
      val suiteClass = loader.loadClass(suiteClassName)
      val suite = suiteClass.newInstance().asInstanceOf[Suite]
      val expectedTestCount = suite.expectedTestCount(includes, excludes)

      // Create a Rerunnable if the Suite has a public no-arg constructor
      val rerunnable = if (Suite.checkForPublicNoArgConstructor(suite.getClass))
                         Some(new SuiteRerunner(suite.getClass.getName))
                       else
                         None

      reporter.runStarting(expectedTestCount)

      try {

        val rawString = Resources("suiteExecutionStarting")
        val report =
          suite match {
            case spec: Spec =>
              new SpecReport(suite.suiteName, rawString, suite.suiteName, suite.suiteName, true, Some(suite.suiteName), Some(suite.getClass.getName), None, None, rerunnable)
            case _ =>
              new Report(suite.suiteName, rawString, Some(suite.suiteName), Some(suite.getClass.getName), None, None, rerunnable)
          }
        reporter.suiteStarting(report)

        suite.execute(None, reporter, stopper, includes, excludes, properties, distributor)

        val rawString2 = Resources("suiteCompletedNormally")
        val report2 =
          suite match {
            case spec: Spec =>
              new SpecReport(suite.suiteName, rawString2, suite.suiteName, suite.suiteName, false, Some(suite.suiteName), Some(suite.getClass.getName), None, None, rerunnable)
            case _ =>
              new Report(suite.suiteName, rawString2, Some(suite.suiteName), Some(suite.getClass.getName), None, None, rerunnable)
          }
        reporter.suiteCompleted(report2)
      }
      catch {
        case e: RuntimeException => {
          val rawString3 = Resources("executeException")
          val report3 =
            suite match {
              case spec: Spec =>
                new SpecReport(suite.suiteName, rawString3, suite.suiteName, suite.suiteName, true, Some(suite.suiteName), Some(suite.getClass.getName), None, Some(e), rerunnable)
              case _ =>
                new Report(suite.suiteName, rawString3, Some(suite.suiteName), Some(suite.getClass.getName), None, Some(e), rerunnable)
            }
          reporter.suiteAborted(report3)
        }
      }
      
      if (stopper.stopRequested) {
        reporter.runStopped()
      }
      else
        reporter.runCompleted()
    }
    catch {
      case ex: ClassNotFoundException => {
        val report = new Report("org.scalatest.tools.Runner", Resources("cannotLoadSuite", ex.getMessage), None, Some(suiteClassName), None, Some(ex), None)
        reporter.runAborted(report)
      }
      case ex: InstantiationException => {
        val report = new Report("org.scalatest.tools.Runner", Resources("cannotInstantiateSuite", ex.getMessage), None, Some(suiteClassName), None, Some(ex), None)
        reporter.runAborted(report)
      }
      case ex: IllegalAccessException => {
        val report = new Report("org.scalatest.tools.Runner", Resources("cannotInstantiateSuite", ex.getMessage), None, Some(suiteClassName), None, Some(ex), None)
        reporter.runAborted(report)
      }
      case e: SecurityException => {
        val report = new Report("org.scalatest.tools.Runner", Resources("securityWhenRerruning", e.getMessage), None, Some(suiteClassName), None, Some(e), None)
        reporter.runAborted(report)
      }
      case ex: NoClassDefFoundError => {
            // Suggest the problem might be a bad runpath
            // Maybe even print out the current runpath
        val report = new Report("org.scalatest.tools.Runner", Resources("cannotLoadClass", ex.getMessage), None, Some(suiteClassName), None, Some(ex), None)
        reporter.runAborted(report)
      }
      case ex: Throwable => {
        val report = new Report("org.scalatest.tools.Runner", Resources.bigProblems(ex), None, Some(suiteClassName), None, Some(ex), None)
        reporter.runAborted(report)
      }
    }
  }
}

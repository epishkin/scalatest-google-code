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
        reporter.suiteStarting(new Report(suite.suiteName, rawString, None, rerunnable))

        suite.execute(None, reporter, stopper, includes, excludes, properties, distributor)

        val rawString2 = Resources("suiteCompletedNormally")
        reporter.suiteCompleted(new Report(suite.suiteName, rawString2, None, rerunnable))
      }
      catch {
        case e: RuntimeException => {
          val rawString = Resources("executeException")
          reporter.suiteAborted(new Report(suite.suiteName, rawString, Some(e), rerunnable))
        }
      }
      
      if (stopper.stopRequested) {
        reporter.runStopped()
      }
      else
        reporter.runCompleted()
    }
    catch { // CLOSE THIS
      case ex: ClassNotFoundException => {
        val report = new Report("org.scalatest.Runner", Resources("cannotLoadSuite"), Some(ex), None)
        reporter.runAborted(report)
      }
      case ex: InstantiationException => {
        val report = new Report("org.scalatest.Runner", Resources("cannotInstantiateSuite"), Some(ex), None)
        reporter.runAborted(report)
      }
      case ex: IllegalAccessException => {
        val report = new Report("org.scalatest.Runner", Resources("cannotInstantiateSuite"), Some(ex), None)
        reporter.runAborted(report)
      }
      case e: SecurityException => {
        val report = new Report("org.scalatest.Runner", Resources("securityWhenReruning"), Some(e), None)
        reporter.runAborted(report)
      }
      case ex: NoClassDefFoundError => {
            // Suggest the problem might be a bad runpath
            // Maybe even print out the current runpath
        val report = new Report("org.scalatest.Runner", Resources("cannotLoadClass"), Some(ex), None)
        reporter.runAborted(report)
      }
      case ex: Throwable => {
        val report = new Report("org.scalatest.Runner", Resources("bigProblems"), Some(ex), None)
        reporter.runAborted(report)
      }
    }
  }
}

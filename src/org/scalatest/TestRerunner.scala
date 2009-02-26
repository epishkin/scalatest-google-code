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

import java.lang.reflect.Method

/**
 * A rerunner for test methods.
 *
 * @author Bill Venners
 */
private[scalatest] class TestRerunner(suiteClassName: String, testName: String) extends Rerunnable {

  if (suiteClassName == null || testName == null)
    throw new NullPointerException

  // [bv: I wasn't sure if I need to say override here.]
  def rerun(reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String], properties: Map[String, Any],
            distributor: Option[Distributor], loader: ClassLoader) {

    try {
      val suiteClass = loader.loadClass(suiteClassName)
      val suite = suiteClass.newInstance.asInstanceOf[Suite]

      reporter.runStarting(1)
      suite.execute(Some(testName), reporter, stopper, includes, excludes, properties, distributor) 
      reporter.runCompleted()
    }
    catch {
      case ex: ClassNotFoundException => {
        val report = new Report("org.scalatest.TestRerunner", Resources("cannotLoadSuite", ex.getMessage), Some(ex), None)
        reporter.runAborted(report)
      }
      case ex: InstantiationException => {
        val report = new Report("org.scalatest.TestRerunner", Resources("cannotInstantiateSuite", ex.getMessage), Some(ex), None)
        reporter.runAborted(report)
      }
      case ex: IllegalAccessException => {
        val report = new Report("org.scalatest.TestRerunner", Resources("cannotInstantiateSuite", ex.getMessage), Some(ex), None)
        reporter.runAborted(report)
      }
      case e: NoSuchMethodException => {
        val report = new Report("org.scalatest.TestRerunner", Resources("cannotFindMethod"), Some(e), None)
        reporter.runAborted(report)
      }
      case e: SecurityException => {
        val report = new Report("org.scalatest.TestRerunner", Resources("securityWhenReruning"), Some(e), None)
        reporter.runAborted(report)
      }
      case ex: NoClassDefFoundError => {
        // Suggest the problem might be a bad runpath
        // Maybe even print out the current runpath
        val report = new Report("org.scalatest.TestRerunner", Resources("cannotLoadClass", ex.getMessage), Some(ex), None)
        reporter.runAborted(report)
      }
      case ex: Throwable => {
        val report = new Report("org.scalatest.TestRerunner", Resources.bigProblems(ex), Some(ex), None)
        reporter.runAborted(report)
      }
    }
  }
}

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
import org.scalatest.events._

/**
 * A rerunner for test methods.
 *
 * @author Bill Venners
 */
private[scalatest] class TestRerunner(suiteClassName: String, testName: String) extends Rerunner {

  if (suiteClassName == null || testName == null)
    throw new NullPointerException

  // [bv: I wasn't sure if I need to say override here.]
  def apply(report: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String], goodies: Map[String, Any],
            distributor: Option[Distributor], firstOrdinal: Ordinal, loader: ClassLoader) {

    var ordinal = firstOrdinal

    try {
      val suiteClass = loader.loadClass(suiteClassName)
      val suite = suiteClass.newInstance.asInstanceOf[Suite]

      report(RunStarting(ordinal, 1))
      ordinal = ordinal.next

      ordinal = suite.run(Some(testName), report, stopper, includes, excludes, goodies, distributor, ordinal) 

      report(RunCompleted(ordinal)) // TODO: pass a duration
      // Don't need to increment ordinal, because it isn't used after this
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
      case e: NoSuchMethodException => {
        report(RunAborted(ordinal, Resources("cannotFindMethod", e.getMessage), Some(e)))
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

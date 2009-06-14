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

import org.scalatest.events._

 /**
 * FiterReporter catches exceptions that may be thrown by custom reporters, and doesn't forward
 * reports that were not selected by the passed configuration.
 *
 * @author Bill Venners
 */
private[scalatest] class FilterReporter(report: Reporter, configSet: ReporterOpts.Set32) extends Reporter {

  def reFilter(configSet: ReporterOpts.Set32) = new FilterReporter(report, configSet)
      
  override def apply(event: Event) {
    event match {

      case event: RunStarting => if (configSet.contains(ReporterOpts.PresentRunStarting)) report(event)
      case event: RunCompleted => if (configSet.contains(ReporterOpts.PresentRunCompleted)) report(event)
      case event: RunAborted => if (configSet.contains(ReporterOpts.PresentRunAborted)) report(event)
      case event: RunStopped => if (configSet.contains(ReporterOpts.PresentRunStopped)) report(event)
      case event: SuiteAborted => if (configSet.contains(ReporterOpts.PresentSuiteAborted)) report(event)

      case _ => throw new RuntimeException("Unhandled event")
    }
  }

  // Have some methods that translate chars & strings to Opts things, and vice versa

  override def testStarting(rpt: Report) =
    if (configSet.contains(ReporterOpts.PresentTestStarting))
      report.testStarting(rpt)

  override def testSucceeded(rpt: Report) =
    if (configSet.contains(ReporterOpts.PresentTestSucceeded))
      report.testSucceeded(rpt)
    
  override def testIgnored(rpt: Report) =
    if (configSet.contains(ReporterOpts.PresentTestIgnored))
      report.testIgnored(rpt)

  override def testFailed(rpt: Report) =
    if (configSet.contains(ReporterOpts.PresentTestFailed))
      report.testFailed(rpt)

  override def suiteStarting(rpt: Report) =
    if (configSet.contains(ReporterOpts.PresentSuiteStarting))
      report.suiteStarting(rpt)

  override def suiteCompleted(rpt: Report) =
    if (configSet.contains(ReporterOpts.PresentSuiteCompleted))
      report.suiteCompleted(rpt)

  override def infoProvided(rpt: Report) =
    if (configSet.contains(ReporterOpts.PresentInfoProvided))
      report.infoProvided(rpt)

  override def dispose() = report.dispose()
}

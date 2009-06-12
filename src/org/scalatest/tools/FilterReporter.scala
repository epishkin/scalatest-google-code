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

import org.scalatest.events.Event

 /**
 * FiterReporter catches exceptions that may be thrown by custom reporters, and doesn't forward
 * reports that were not selected by the passed configuration.
 *
 * @author Bill Venners
 */
private[scalatest] class FilterReporter(wrappedReporter: Reporter, configSet: ReporterOpts.Set32) extends Reporter {

  def reFilter(configSet: ReporterOpts.Set32) = new FilterReporter(wrappedReporter, configSet)
      
/* Dropping under theory I'll simply have two running schemes for two releases
  override def apply(event: Event) {
    super.apply(event)
  }
*/

  // Have some methods that translate chars & strings to Opts things, and vice versa
 
  override def runStarting(testCount: Int) =
    if (configSet.contains(ReporterOpts.PresentRunStarting))
      wrappedReporter.runStarting(testCount)

  override def testStarting(report: Report) =
    if (configSet.contains(ReporterOpts.PresentTestStarting))
      wrappedReporter.testStarting(report)

  override def testSucceeded(report: Report) =
    if (configSet.contains(ReporterOpts.PresentTestSucceeded))
      wrappedReporter.testSucceeded(report)
    
  override def testIgnored(report: Report) =
    if (configSet.contains(ReporterOpts.PresentTestIgnored))
      wrappedReporter.testIgnored(report)

  override def testFailed(report: Report) =
    if (configSet.contains(ReporterOpts.PresentTestFailed))
      wrappedReporter.testFailed(report)

  override def suiteStarting(report: Report) =
    if (configSet.contains(ReporterOpts.PresentSuiteStarting))
      wrappedReporter.suiteStarting(report)

  override def suiteCompleted(report: Report) =
    if (configSet.contains(ReporterOpts.PresentSuiteCompleted))
      wrappedReporter.suiteCompleted(report)

  override def suiteAborted(report: Report) =
    if (configSet.contains(ReporterOpts.PresentSuiteAborted))
      wrappedReporter.suiteAborted(report)

  override def infoProvided(report: Report) =
    if (configSet.contains(ReporterOpts.PresentInfoProvided))
      wrappedReporter.infoProvided(report)

  override def runStopped() =
    if (configSet.contains(ReporterOpts.PresentRunStopped))
      wrappedReporter.runStopped()

  override def runAborted(report: Report) =
    if (configSet.contains(ReporterOpts.PresentRunAborted))
      wrappedReporter.runAborted(report)

  override def runCompleted() =
    if (configSet.contains(ReporterOpts.PresentRunCompleted))
      wrappedReporter.runCompleted()

  override def dispose() = wrappedReporter.dispose()
}

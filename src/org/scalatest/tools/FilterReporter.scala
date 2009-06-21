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
import DispatchReporter.propagateDispose

/**
 * FiterReporter catches exceptions that may be thrown by custom reporters, and doesn't forward
 * reports that were not selected by the passed configuration.
 *
 * @author Bill Venners
 */
private[scalatest] class FilterReporter(reporter: Reporter, configSet: ReporterOpts.Set32) extends ResourcefulReporter {

  def reFilter(configSet: ReporterOpts.Set32) = new FilterReporter(reporter, configSet)

  override def apply(event: Event) {
    val report = reporter
    event match {
      case event: RunStarting => if (configSet.contains(ReporterOpts.PresentRunStarting)) report(event)
      case event: RunCompleted => if (configSet.contains(ReporterOpts.PresentRunCompleted)) report(event)
      case event: RunAborted => if (configSet.contains(ReporterOpts.PresentRunAborted)) report(event)
      case event: RunStopped => if (configSet.contains(ReporterOpts.PresentRunStopped)) report(event)
      case event: SuiteAborted => if (configSet.contains(ReporterOpts.PresentSuiteAborted)) report(event)
      case event: SuiteCompleted => if (configSet.contains(ReporterOpts.PresentSuiteCompleted)) report(event)
      case event: SuiteStarting => if (configSet.contains(ReporterOpts.PresentSuiteStarting)) report(event)
      case event: TestStarting => if (configSet.contains(ReporterOpts.PresentTestStarting)) report(event)
      case event: TestSucceeded => if (configSet.contains(ReporterOpts.PresentTestSucceeded)) report(event)
      case event: TestIgnored => if (configSet.contains(ReporterOpts.PresentTestIgnored)) report(event)
      case event: TestPending => if (configSet.contains(ReporterOpts.PresentTestPending)) report(event)
      case event: TestFailed => if (configSet.contains(ReporterOpts.PresentTestFailed)) report(event)
      case event: InfoProvided => if (configSet.contains(ReporterOpts.PresentInfoProvided)) report(event)
    }
  }

  override def dispose() = propagateDispose(reporter)
}
// Have some methods that translate chars & strings to Opts things, and vice versa?

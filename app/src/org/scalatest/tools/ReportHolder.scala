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

import java.util.HashSet
import java.util.Set

/**
 * Used to hold Reports in the GUI, so that I can keep track of which report method was called
 * on the reporter to deliver it.
 *
 * @author Bill Venners
 */
private[scalatest] class ReportHolder(val report: Report, val reportType: ReporterOpts.Value, val isRerun: Boolean) {

  if (report == null || reportType == null)
    throw new NullPointerException()
 
  def this(report: Report, reportType: ReporterOpts.Value) = this(report, reportType, false)

  override def toString(): String = {

    report match {
      case sr: SpecReport =>
        if (reportType == ReporterOpts.PresentSuiteStarting)
          sr.plainSpecText + ":"
        else 
          sr.plainSpecText
      case _ => 
        val firstString: String =
          if (isRerun)
            Resources("RERUN_" + ReporterOpts.getUpperCaseName(reportType))
          else
            Resources(ReporterOpts.getUpperCaseName(reportType))

        if (reportType != ReporterOpts.PresentRunStarting && reportType != ReporterOpts.PresentRunStopped &&
            reportType != ReporterOpts.PresentRunAborted && reportType != ReporterOpts.PresentRunCompleted) {
          firstString + " - " + report.name
        }
        else firstString 
    }
  }
}

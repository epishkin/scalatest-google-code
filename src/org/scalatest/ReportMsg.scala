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

/**
 * Case classes for Reports, so I can send them to an Actor to serialize the handling
 * of reports, when tests are being run concurrently.
 *
 * @author Bill Venners
 */
private[scalatest] abstract class ReportMsg(report: Report)
private[scalatest] case class TestStarting(report: Report) extends ReportMsg(report)
private[scalatest] case class TestIgnored(report: Report) extends ReportMsg(report)
private[scalatest] case class TestSucceeded(report: Report) extends ReportMsg(report)
private[scalatest] case class TestFailed(report: Report) extends ReportMsg(report)
private[scalatest] case class SuiteStarting(report: Report) extends ReportMsg(report)
private[scalatest] case class SuiteCompleted(report: Report) extends ReportMsg(report)
private[scalatest] case class SuiteAborted(report: Report) extends ReportMsg(report)
private[scalatest] case class InfoProvided(report: Report) extends ReportMsg(report)
private[scalatest] case class RunStarting (expectedTestCount: Int)
private[scalatest] case class RunStopped
private[scalatest] case class RunAborted(report: Report) extends ReportMsg(report)
private[scalatest] case class RunCompleted
private[scalatest] case class Dispose

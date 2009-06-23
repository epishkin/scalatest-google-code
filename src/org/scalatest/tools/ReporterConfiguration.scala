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

/**
 * This file has types that are used in parsing command line arguments to Runner.
 *
 * @author Bill Venners
 */
private[scalatest] abstract class ReporterConfiguration(configSet: ReporterOpts.Set32)

private[scalatest] case class GraphicReporterConfiguration(configSet: ReporterOpts.Set32) extends ReporterConfiguration(configSet)
private[scalatest] case class StandardOutReporterConfiguration(configSet: ReporterOpts.Set32) extends ReporterConfiguration(configSet)
private[scalatest] case class StandardErrReporterConfiguration(configSet: ReporterOpts.Set32) extends ReporterConfiguration(configSet)
private[scalatest] case class FileReporterConfiguration(configSet: ReporterOpts.Set32, fileName: String) extends ReporterConfiguration(configSet)
private[scalatest] case class CustomReporterConfiguration(configSet: ReporterOpts.Set32, reporterClass: String) extends ReporterConfiguration(configSet)

// If there were no fileReporterSpecList or customReporterSpecList specified, you get Nil
// If there were no graphicReporterSpec, standardOutReporterSpec, or standardErrReporterSpec, you get None
private[scalatest] case class ReporterConfigurations(val graphicReporterConfiguration: Option[GraphicReporterConfiguration],
    val fileReporterConfigurationList: List[FileReporterConfiguration], val standardOutReporterConfiguration: Option[StandardOutReporterConfiguration],
    val standardErrReporterConfiguration: Option[StandardErrReporterConfiguration], val customReporterConfigurationList: List[CustomReporterConfiguration])
    extends Seq[ReporterConfiguration] {

  val reporterConfigurationList =
    List.concat[ReporterConfiguration](
      graphicReporterConfiguration.toList,
      fileReporterConfigurationList,
      standardOutReporterConfiguration.toList,
      standardErrReporterConfiguration.toList,
      customReporterConfigurationList
    )

  // Need to add the null pointer checks, or later, NotNull

  override def length = reporterConfigurationList.length
  override def elements = reporterConfigurationList.elements
  override def apply(i: Int) = reporterConfigurationList(i)
}


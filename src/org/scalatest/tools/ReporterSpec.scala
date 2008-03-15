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

// Another thing is dang this is concise way to create types. So it makes it easier (lower cost, lower risk) to
// represent things in the type system compared to Java. Note also that in Java each of these darn things would
// need to be in a different file, well maybe not, since they would be package access.

/**
 * This file has types that are used in parsing command line arguments to Runner.
 *
 * @author Bill Venners
 */
private[scalatest] abstract class ReporterSpec(configSet: ReporterOpts.Set32)

private[scalatest] case class GraphicReporterSpec(configSet: ReporterOpts.Set32) extends ReporterSpec(configSet)
private[scalatest] case class StandardOutReporterSpec(configSet: ReporterOpts.Set32) extends ReporterSpec(configSet)
private[scalatest] case class StandardErrReporterSpec(configSet: ReporterOpts.Set32) extends ReporterSpec(configSet)
private[scalatest] case class FileReporterSpec(configSet: ReporterOpts.Set32, fileName: String) extends ReporterSpec(configSet)
private[scalatest] case class CustomReporterSpec(configSet: ReporterOpts.Set32, reporterClass: String) extends ReporterSpec(configSet)

// If there were no fileReporterSpecList or customReporterSpecList specified, you get Nil
// If there were no graphicReporterSpec, standardOutReporterSpec, or standardErrReporterSpec, you get None
private[scalatest] case class ReporterSpecs(val graphicReporterSpec: Option[GraphicReporterSpec],
    val fileReporterSpecList: List[FileReporterSpec], val standardOutReporterSpec: Option[StandardOutReporterSpec], 
    val standardErrReporterSpec: Option[StandardErrReporterSpec], val customReporterSpecList: List[CustomReporterSpec])
    extends Seq[ReporterSpec] {

  val reporterSpecList =
    List.concat[ReporterSpec](
      graphicReporterSpec.toList,
      fileReporterSpecList,
      standardOutReporterSpec.toList,
      standardErrReporterSpec.toList,
      customReporterSpecList
    )

  // Need to add the null pointer checks, or later, NotNull

  override def length = reporterSpecList.length
  override def elements = reporterSpecList.elements
  override def apply(i: Int) = reporterSpecList(i)
}


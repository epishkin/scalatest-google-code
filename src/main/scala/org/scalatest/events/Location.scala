/*
 * Copyright 2001-2011 Artima, Inc.
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
package org.scalatest.events

/**
 * Location in source code about which an event concerns.
 */
sealed abstract class Location

/**
 * The location in a source file where the class whose by the fully qualified name
 * is passed as <code>className</code> is declared.
 */
final case class TopOfClass(className: String) extends Location

/**
 * The location in a source file where the method whose name is passed as
 * <code>methodName</code> in the class whose fully qualified name is passed
 * as <code>className</code> is declared.
 */
final case class TopOfMethod(className: String, methodId: String) extends Location

/**
 * An arbitrary line number in a named source file.
 */
final case class LineInFile(stackDepth: Int) extends Location {

  private val e = new Exception
  private lazy val stackTraceElement = e.getStackTrace()(stackDepth + 1)

  lazy val className: String = stackTraceElement.getClassName
  lazy val methodName: String = stackTraceElement.getMethodName
  lazy val fileName: Option[String] = Option(stackTraceElement.getFileName)
  lazy val lineNumber: Option[Int] = Option(stackTraceElement.getLineNumber)
}

/**
 * Indicates the location should be taken from the stack depth exception, included elsewhere in 
 * the event that contained this location.
 */
final case object SeeStackDepthException extends Location

/**
 * Interim one that I can quickly put in to get the compile going, and later I can go back carefully and do them correctly.
 */
final case object ToDoLocation extends Location


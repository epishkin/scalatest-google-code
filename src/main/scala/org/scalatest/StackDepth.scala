/*
 * Copyright 2001-2009 Artima, Inc.
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

trait StackDepth { this: Throwable =>

  val message: Option[String]
  val cause: Option[Throwable]
  val failedCodeStackDepth: Int

  /**
   * A string that provides the filename and line number of the line of code that failed, suitable
   * for presenting to a user, which is taken from this exception's <code>StackTraceElement</code> at the depth specified
   * by <code>failedCodeStackDepth</code>.
   *
   * @return a user-presentable string containing the filename and line number that caused the failed test
   */
  val failedCodeFileNameAndLineNumberString: Option[String] = {
    val stackTraceElement = getStackTrace()(failedCodeStackDepth)
    val fileName = stackTraceElement.getFileName
    if (fileName != null) {
      Some(fileName + ":" + stackTraceElement.getLineNumber)
    }
    else None
  }
}
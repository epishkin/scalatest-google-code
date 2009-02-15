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

// Idea is if you don't know the information, don't throw a TestFailedError. Throw
// something else, such as a plain old AssertionError
class TestFailedError(val failedTestCodeStackDepth: Int) extends AssertionError {

  // An option because getFileName on the StackTraceElement may return null
  // Only provide a method for that which I am using. Other info can be grabbed
  // with the failedTestCodeStackDepth information.
  val failedTestCodeFileNameAndLineNumberString: Option[String] = {
    val stackTraceElement = getStackTrace()(failedTestCodeStackDepth)
    val fileName = stackTraceElement.getFileName
    if (fileName != null) {
      Some(fileName + ":" + stackTraceElement.getLineNumber)
    }
    else None
  }
}

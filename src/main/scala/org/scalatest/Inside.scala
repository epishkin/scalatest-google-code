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
package org.scalatest

private[scalatest] trait Inside {
  def inside[T](value: T)(pf: PartialFunction[T, Unit]) {
    def appendInsideMessage(currentMessage: Option[String]) =
      currentMessage match { // TODO, grab strings from resource file and maybe zap trailing white space before appending ", inside ..."
        case Some(msg) => Some(Resources("insidePartialFunctionAppendSomeMsg", msg.trim, value.toString()))
        case None => Some(Resources("insidePartialFunctionAppendNone", value.toString()))
      }
    if (pf.isDefinedAt(value)) {
      try {
        pf(value)
      }
      catch {
        case e: ModifiableMessage[_] =>
          throw e.modifyMessage(appendInsideMessage)
      }
    }
    else 
      throw new TestFailedException(Resources("insidePartialFunctionNotDefined", value.toString()), 2)
  }
}

object Inside extends Inside

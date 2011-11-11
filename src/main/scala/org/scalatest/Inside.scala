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

// TODO: Make this private[scalatest]
trait Inside {
  def inside[T](value: T)(pf: PartialFunction[T, Unit]) {
    if (pf.isDefinedAt(value)) {
      pf(value)
    }
    else // TODO: Get string from resource file, and verify and maybe be smarter about stack depth
      throw new TestFailedException("The partial function passed as the second parameter to inside was not defined at the value passed as the first parameter to inside, which was: " + value, 2)
  }
}

object Inside extends Inside

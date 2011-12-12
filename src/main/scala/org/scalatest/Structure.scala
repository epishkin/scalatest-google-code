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

import org.scalatest.events.Location

sealed abstract class Structure {
  val text: String // Can be lazy
  val location: Location // Can be lazy
}

sealed abstract class SuiteStructure extends Structure {
  val text: String
  val location: Location
  val suiteId: String
  val nestedStructures: List[Structure]
}

object SuiteStructure {
  def unapply(ss: SuiteStructure): Option[(String, Location, String, List[Structure])] = {
    Some(ss.text, ss.location, ss.suiteId, ss.nestedStructures)
  }
}

final class EagerSuiteStructure(val text: String, val location: Location, val suiteId: String, val nestedStructures: List[Structure]) extends SuiteStructure

final class LazySuiteStructure(suite: Suite) extends SuiteStructure {
  private lazy val structure = suite.suiteStructure
  lazy val text: String = structure.text
  lazy val location: Location = structure.location
  lazy val suiteId: String = structure.suiteId
  lazy val nestedStructures: List[Structure] = structure.nestedStructures
}

final case class ScopeStructure(text: String, location: Location, nestedStructures: List[Structure]) extends Structure

final case class TestStructure(text: String, location: Location, testName: String) extends Structure

/*
suiteStructure on Suite returns a SuiteStructure
a SuiteStructure can contain zero to many substructures of any kind, right? A SuiteStructure inside a SuiteStructure is a nestedSuite (so these should be lazy)
a TestStructure can't contain any more substructures
a ScopeStructure can contain other ScopeStructures and other TestStructures, but can it contain a SuiteStructure?
*/


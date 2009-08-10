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
package org.scalatestexamples.fixture


import scalatest.fixture.MultipleFixtureFunSuite

class MultiFixtureFunSuite extends MultipleFixtureFunSuite {

   // Uses the implicit conversion from by-name to Fixture => Unit
  implicit def withStringFixture(testFunction: String => Unit): Fixture => Unit =
    testFunction("howdy")

  implicit def withListFixture(testFunction: List[Int] => Unit): Fixture => Unit =
    configMap => testFunction(List(configMap.size))

  test("a by name version") {
    assert(1 === 1)
  }

  test("a configMap version") { configMap =>
    assert(configMap.isEmpty)
  }

  test("a string fixture") { (s: String) =>
    assert(s === "howdy")
  }

  test("a list fixture") { (list: List[Int]) =>
    assert(list.size === 1)
  }
}

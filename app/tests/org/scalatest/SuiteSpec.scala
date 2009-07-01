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

import org.scalatest.events._

class SuiteSpec extends Spec with HandyReporters {
  describe("A Suite") {
    it("should return the test names in alphabetical order from testNames") {
      val a = new Suite {
        def testThis() {}
        def testThat() {}
      }

      expect(List("testThat", "testThis")) {
        a.testNames.elements.toList
      }

      val b = new Suite {}

      expect(List[String]()) {
        b.testNames.elements.toList
      }

      val c = new Suite {
        def testThat() {}
        def testThis() {}
      }

      expect(List("testThat", "testThis")) {
        c.testNames.elements.toList
      }
    }
  }
}


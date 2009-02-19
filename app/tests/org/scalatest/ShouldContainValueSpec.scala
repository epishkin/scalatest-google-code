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

import prop.Checkers
import org.scalacheck._
import Arbitrary._
import Prop._

class ShouldContainValueSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion {

  // Checking for a specific size
  describe("The 'contain value (Int)' syntax") {

    describe("on scala.collection.immutable.Map") {

      it("should do nothing if map contains specified value") {
        Map("one" -> 1, "two" -> 2) should contain value (2)
        Map("one" -> 1, "two" -> 2) should (contain value (2))
        Map(1 -> "one", 2 -> "two") should contain value ("two")
      }

      it("should do nothing if map does not contain the specified value and used with not") {
        Map("one" -> 1, "two" -> 2) should not { contain value (3) }
        Map("one" -> 1, "two" -> 2) should not contain value (3)
        Map("one" -> 1, "two" -> 2) should (not contain value (3))
      }

      it("should do nothing when map contains specified value and used in a logical-and expression") {
        Map("one" -> 1, "two" -> 2) should { contain value (2) and (contain value (1)) }
        Map("one" -> 1, "two" -> 2) should ((contain value (2)) and (contain value (1)))
        Map("one" -> 1, "two" -> 2) should (contain value (2) and contain value (1))
      }

      it("should do nothing when map contains specified value and used in a logical-or expression") {
        Map("one" -> 1, "two" -> 2) should { contain value (7) or (contain value (1)) }
        Map("one" -> 1, "two" -> 2) should ((contain value (7)) or (contain value (1)))
        Map("one" -> 1, "two" -> 2) should (contain value (7) or contain value (1))
      }

      it("should do nothing when map does not contain the specified value and used in a logical-and expression with not") {
        Map("one" -> 1, "two" -> 2) should { not { contain value (5) } and not { contain value (3) }}
        Map("one" -> 1, "two" -> 2) should ((not contain value (5)) and (not contain value (3)))
        Map("one" -> 1, "two" -> 2) should (not contain value (5) and not contain value (3))
      }

      it("should do nothing when map does not contain the specified value and used in a logical-or expression with not") {
        Map("one" -> 1, "two" -> 2) should { not { contain value (2) } or not { contain value (3) }}
        Map("one" -> 1, "two" -> 2) should ((not contain value (2)) or (not contain value (3)))
        Map("one" -> 1, "two" -> 2) should (not contain value (2) or not contain value (3))
      }

      it("should throw TestFailedException if map does not contain the specified value") {
        val caught1 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should contain value (3)
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not contain value 3")
      }

      it("should throw TestFailedException if contains the specified value when used with not") {

        val caught1 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should (not contain value (2))
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) contained value 2")

        val caught2 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should not (contain value (2))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) contained value 2")

        val caught3 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should not contain value (2)
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) contained value 2")
      }

      it("should throw an TestFailedException when map doesn't contain specified value and used in a logical-and expression") {

        val caught1 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should { contain value (5) and (contain value (2)) }
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not contain value 5")

        val caught2 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should ((contain value (5)) and (contain value (2)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not contain value 5")

        val caught3 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should (contain value (5) and contain value (2))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not contain value 5")
      }

      it("should throw an TestFailedException when map doesn't contain specified value and used in a logical-or expression") {

        val caught1 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should { contain value (55) or (contain value (22)) }
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not contain value 55, and Map(one -> 1, two -> 2) did not contain value 22")

        val caught2 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should ((contain value (55)) or (contain value (22)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not contain value 55, and Map(one -> 1, two -> 2) did not contain value 22")

        val caught3 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should (contain value (55) or contain value (22))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not contain value 55, and Map(one -> 1, two -> 2) did not contain value 22")
      }

      it("should throw an TestFailedException when map contains specified value and used in a logical-and expression with not") {

        val caught1 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should { not { contain value (3) } and not { contain value (2) }}
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not contain value 3, but Map(one -> 1, two -> 2) contained value 2")

        val caught2 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should ((not contain value (3)) and (not contain value (2)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not contain value 3, but Map(one -> 1, two -> 2) contained value 2")

        val caught3 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should (not contain value (3) and not contain value (2))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not contain value 3, but Map(one -> 1, two -> 2) contained value 2")
      }

      it("should throw an TestFailedException when map contains specified value and used in a logical-or expression with not") {

        val caught1 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should { not { contain value (2) } or not { contain value (2) }}
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) contained value 2, and Map(one -> 1, two -> 2) contained value 2")

        val caught2 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should ((not contain value (2)) or (not contain value (2)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) contained value 2, and Map(one -> 1, two -> 2) contained value 2")

        val caught3 = intercept[TestFailedException] {
          Map("one" -> 1, "two" -> 2) should (not contain value (2) or not contain value (2))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) contained value 2, and Map(one -> 1, two -> 2) contained value 2")
      }
    }
  }
}

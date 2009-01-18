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

class ShouldOrderedSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion {

  // Checking for a specific size
  describe("The 'be >/</>=/<= (x)' syntax") {

    describe("on Int") {

      it("should do nothing if the comparison holds true") {
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should be < (right)))
        check((left: Int, right: Int) => left <= right ==> returnsNormally(left should be <= (right)))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should be > (right)))
        check((left: Int, right: Int) => left >= right ==> returnsNormally(left should be >= (right)))
      }

      it("should do nothing if the comparison fails and used with not") {

        check((left: Int, right: Int) => left >= right ==> returnsNormally(left should not be < (right)))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should not be <= (right)))
        check((left: Int, right: Int) => left <= right ==> returnsNormally(left should not be > (right)))
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should not be >= (right)))

        check((left: Int, right: Int) => left >= right ==> returnsNormally(left should not (be < (right))))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should not (be <= (right))))
        check((left: Int, right: Int) => left <= right ==> returnsNormally(left should not (be > (right))))
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should not (be >= (right))))
      }

      it("should do nothing when comparison succeeds and used in a logical-and expression") {

        check((left: Int, right: Int) => left < right ==> returnsNormally(left should ((be < (right)) and (be < (right + 1)))))
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should (be < (right) and (be < (right + 1)))))
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should (be < (right) and be < (right + 1))))

        check((left: Int, right: Int) => left <= right ==> returnsNormally(left should ((be <= (right)) and (be <= (right + 1)))))
        check((left: Int, right: Int) => left <= right ==> returnsNormally(left should (be <= (right) and (be <= (right + 1)))))
        check((left: Int, right: Int) => left <= right ==> returnsNormally(left should (be <= (right) and be <= (right + 1))))

        check((left: Int, right: Int) => left > right ==> returnsNormally(left should ((be > (right)) and (be > (right - 1)))))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should (be > (right) and (be > (right - 1)))))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should (be > (right) and be > (right - 1))))

        check((left: Int, right: Int) => left >= right ==> returnsNormally(left should ((be >= (right)) and (be >= (right - 1)))))
        check((left: Int, right: Int) => left >= right ==> returnsNormally(left should (be >= (right) and (be >= (right - 1)))))
        check((left: Int, right: Int) => left >= right ==> returnsNormally(left should (be >= (right) and be >= (right - 1))))
      }

      it("should do nothing when array size matches and used in a logical-or expression") {

        check((left: Int, right: Int) => left < right ==> returnsNormally(left should ((be < (right - 1)) or (be < (right + 1)))))
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should (be < (right - 1) or (be < (right + 1)))))
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should (be < (right - 1) or be < (right + 1))))

        check((left: Int, right: Int) => left <= right ==> returnsNormally(left should ((be <= (right - 1)) or (be <= (right + 1)))))
        check((left: Int, right: Int) => left <= right ==> returnsNormally(left should (be <= (right - 1) or (be <= (right + 1)))))
        check((left: Int, right: Int) => left <= right ==> returnsNormally(left should (be <= (right - 1) or be <= (right + 1))))

        check((left: Int, right: Int) => left > right ==> returnsNormally(left should ((be > (right + 1)) or (be > (right - 1)))))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should (be > (right + 1) or (be > (right - 1)))))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should (be > (right + 1) or be > (right - 1))))

        check((left: Int, right: Int) => left >= right ==> returnsNormally(left should ((be >= (right + 1)) or (be >= (right - 1)))))
        check((left: Int, right: Int) => left >= right ==> returnsNormally(left should (be >= (right + 1) or (be >= (right - 1)))))
        check((left: Int, right: Int) => left >= right ==> returnsNormally(left should (be >= (right + 1) or be >= (right - 1))))

        check((left: Int, right: Int) => returnsNormally(left should (be >= (right) or be < (right))))
        check((left: Int, right: Int) => returnsNormally(left should (be > (right) or be <= (right))))
      }

      it("should do nothing when comparison fails and used in a logical-and expression with not") {

        check((left: Int, right: Int) => left > right ==> returnsNormally(left should (not (be < (right)) and not (be < (right + 1)))))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should ((not be < (right)) and (not be < (right + 1)))))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should (not be < (right) and not be < (right + 1))))

        check((left: Int, right: Int) => left > right ==> returnsNormally(left should (not (be <= (right)) and not (be <= (right)))))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should ((not be <= (right)) and (not be <= (right)))))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should (not be <= (right) and not be <= (right))))

        check((left: Int, right: Int) => left < right ==> returnsNormally(left should (not (be > (right)) and not (be > (right - 1)))))
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should ((not be > (right)) and (not be > (right - 1)))))
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should (not be > (right) and not be > (right - 1))))

        check((left: Int, right: Int) => left < right ==> returnsNormally(left should (not (be >= (right)) and not (be >= (right)))))
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should ((not be >= (right)) and (not be >= (right)))))
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should (not be >= (right) and not be >= (right))))
      }

      it("should do nothing when comparison fails and used in a logical-or expression with not") {

        check((left: Int, right: Int) => left > right ==> returnsNormally(left should (not (be >= (right)) or not (be < (right)))))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should ((not be >= (right)) or (not be < (right)))))
        check((left: Int, right: Int) => left > right ==> returnsNormally(left should (not be >= (right) or not be < (right))))

        check((left: Int, right: Int) => left >= right ==> returnsNormally(left should (not (be > (right)) or not (be <= (right)))))
        check((left: Int, right: Int) => left >= right ==> returnsNormally(left should ((not be > (right)) or (not be <= (right)))))
        check((left: Int, right: Int) => left >= right ==> returnsNormally(left should (not be > (right) or not be <= (right))))

        check((left: Int, right: Int) => left < right ==> returnsNormally(left should (not (be <= (right)) or not (be > (right)))))
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should ((not be <= (right)) or (not be > (right)))))
        check((left: Int, right: Int) => left < right ==> returnsNormally(left should (not be <= (right) or not be > (right))))

        check((left: Int, right: Int) => left <= right ==> returnsNormally(left should (not (be < (right)) or not (be >= (right)))))
        check((left: Int, right: Int) => left <= right ==> returnsNormally(left should ((not be < (right)) or (not be >= (right)))))
        check((left: Int, right: Int) => left <= right ==> returnsNormally(left should (not be < (right) or not be >= (right))))
      }
    }

    describe("on String") {

      it("should do nothing if the comparison holds true") {
        check((left: String, right: String) => left < right ==> returnsNormally(left should be < (right)))
        check((left: String, right: String) => left <= right ==> returnsNormally(left should be <= (right)))
        check((left: String, right: String) => left > right ==> returnsNormally(left should be > (right)))
        check((left: String, right: String) => left >= right ==> returnsNormally(left should be >= (right)))
      }

      it("should do nothing if the comparison fails and used with not") {

        check((left: String, right: String) => left >= right ==> returnsNormally(left should not be < (right)))
        check((left: String, right: String) => left > right ==> returnsNormally(left should not be <= (right)))
        check((left: String, right: String) => left <= right ==> returnsNormally(left should not be > (right)))
        check((left: String, right: String) => left < right ==> returnsNormally(left should not be >= (right)))

        check((left: String, right: String) => left >= right ==> returnsNormally(left should not (be < (right))))
        check((left: String, right: String) => left > right ==> returnsNormally(left should not (be <= (right))))
        check((left: String, right: String) => left <= right ==> returnsNormally(left should not (be > (right))))
        check((left: String, right: String) => left < right ==> returnsNormally(left should not (be >= (right))))
      }

      it("should do nothing when comparison succeeds and used in a logical-and expression") {

        check((left: String, right: String) => left < right ==> returnsNormally(left should ((be < (right)) and (be < (right)))))
        check((left: String, right: String) => left < right ==> returnsNormally(left should (be < (right) and (be < (right)))))
        check((left: String, right: String) => left < right ==> returnsNormally(left should (be < (right) and be < (right))))

        check((left: String, right: String) => left <= right ==> returnsNormally(left should ((be <= (right)) and (be <= (right)))))
        check((left: String, right: String) => left <= right ==> returnsNormally(left should (be <= (right) and (be <= (right)))))
        check((left: String, right: String) => left <= right ==> returnsNormally(left should (be <= (right) and be <= (right))))

        check((left: String, right: String) => left > right ==> returnsNormally(left should ((be > (right)) and (be > (right)))))
        check((left: String, right: String) => left > right ==> returnsNormally(left should (be > (right) and (be > (right)))))
        check((left: String, right: String) => left > right ==> returnsNormally(left should (be > (right) and be > (right))))

        check((left: String, right: String) => left >= right ==> returnsNormally(left should ((be >= (right)) and (be >= (right)))))
        check((left: String, right: String) => left >= right ==> returnsNormally(left should (be >= (right) and (be >= (right)))))
        check((left: String, right: String) => left >= right ==> returnsNormally(left should (be >= (right) and be >= (right))))
      }

      it("should do nothing when array size matches and used in a logical-or expression") {

        check((left: String, right: String) => left < right ==> returnsNormally(left should ((be < (right)) or (be < (right)))))
        check((left: String, right: String) => left < right ==> returnsNormally(left should (be < (right) or (be < (right)))))
        check((left: String, right: String) => left < right ==> returnsNormally(left should (be < (right) or be < (right))))

        check((left: String, right: String) => left <= right ==> returnsNormally(left should ((be <= (right)) or (be <= (right)))))
        check((left: String, right: String) => left <= right ==> returnsNormally(left should (be <= (right) or (be <= (right)))))
        check((left: String, right: String) => left <= right ==> returnsNormally(left should (be <= (right) or be <= (right))))

        check((left: String, right: String) => left > right ==> returnsNormally(left should ((be > (right)) or (be > (right)))))
        check((left: String, right: String) => left > right ==> returnsNormally(left should (be > (right) or (be > (right)))))
        check((left: String, right: String) => left > right ==> returnsNormally(left should (be > (right) or be > (right))))

        check((left: String, right: String) => left >= right ==> returnsNormally(left should ((be >= (right)) or (be >= (right)))))
        check((left: String, right: String) => left >= right ==> returnsNormally(left should (be >= (right) or (be >= (right)))))
        check((left: String, right: String) => left >= right ==> returnsNormally(left should (be >= (right) or be >= (right))))

        check((left: String, right: String) => returnsNormally(left should (be >= (right) or be < (right))))
        check((left: String, right: String) => returnsNormally(left should (be > (right) or be <= (right))))
      }

      it("should do nothing when comparison fails and used in a logical-and expression with not") {

        check((left: String, right: String) => left >= right ==> returnsNormally(left should (not (be < (right)) and not (be < (right)))))
        check((left: String, right: String) => left >= right ==> returnsNormally(left should ((not be < (right)) and (not be < (right)))))
        check((left: String, right: String) => left >= right ==> returnsNormally(left should (not be < (right) and not be < (right))))

        check((left: String, right: String) => left > right ==> returnsNormally(left should (not (be <= (right)) and not (be <= (right)))))
        check((left: String, right: String) => left > right ==> returnsNormally(left should ((not be <= (right)) and (not be <= (right)))))
        check((left: String, right: String) => left > right ==> returnsNormally(left should (not be <= (right) and not be <= (right))))

        check((left: String, right: String) => left <= right ==> returnsNormally(left should (not (be > (right)) and not (be > (right)))))
        check((left: String, right: String) => left <= right ==> returnsNormally(left should ((not be > (right)) and (not be > (right)))))
        check((left: String, right: String) => left <= right ==> returnsNormally(left should (not be > (right) and not be > (right))))

        check((left: String, right: String) => left < right ==> returnsNormally(left should (not (be >= (right)) and not (be >= (right)))))
        check((left: String, right: String) => left < right ==> returnsNormally(left should ((not be >= (right)) and (not be >= (right)))))
        check((left: String, right: String) => left < right ==> returnsNormally(left should (not be >= (right) and not be >= (right))))
      }

      it("should do nothing when comparison fails and used in a logical-or expression with not") {

        check((left: String, right: String) => left > right ==> returnsNormally(left should (not (be >= (right)) or not (be < (right)))))
        check((left: String, right: String) => left > right ==> returnsNormally(left should ((not be >= (right)) or (not be < (right)))))
        check((left: String, right: String) => left > right ==> returnsNormally(left should (not be >= (right) or not be < (right))))

        check((left: String, right: String) => left >= right ==> returnsNormally(left should (not (be > (right)) or not (be <= (right)))))
        check((left: String, right: String) => left >= right ==> returnsNormally(left should ((not be > (right)) or (not be <= (right)))))
        check((left: String, right: String) => left >= right ==> returnsNormally(left should (not be > (right) or not be <= (right))))

        check((left: String, right: String) => left < right ==> returnsNormally(left should (not (be <= (right)) or not (be > (right)))))
        check((left: String, right: String) => left < right ==> returnsNormally(left should ((not be <= (right)) or (not be > (right)))))
        check((left: String, right: String) => left < right ==> returnsNormally(left should (not be <= (right) or not be > (right))))

        check((left: String, right: String) => left <= right ==> returnsNormally(left should (not (be < (right)) or not (be >= (right)))))
        check((left: String, right: String) => left <= right ==> returnsNormally(left should ((not be < (right)) or (not be >= (right)))))
        check((left: String, right: String) => left <= right ==> returnsNormally(left should (not be < (right) or not be >= (right))))
      }
/*
      it("should throw AssertionError if array size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          Array(1, 2) should have size (3)
        }
        assert(caught1.getMessage === "Array(1, 2) did not have size 3")
        check((arr: Array[String]) => throwsAssertionError(arr should have size (arr.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          Array(1, 2) should have size (-2)
        }
        assert(caught1.getMessage === "Array(1, 2) did not have size -2")
        check((arr: Array[Int]) => throwsAssertionError(arr should have size (if (arr.size == 0) -1 else -arr.size)))
      }

      it("should throw an assertion error when array size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          Array(1, 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "Array(1, 2) did not have size 5")

        val caught2 = intercept[AssertionError] {
          Array(1, 2) should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "Array(1, 2) did not have size 5")

        val caught3 = intercept[AssertionError] {
          Array(1, 2) should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "Array(1, 2) did not have size 5")
      }

      it("should throw an assertion error when array size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          Array(1, 2) should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "Array(1, 2) did not have size 55, and Array(1, 2) did not have size 22")

        val caught2 = intercept[AssertionError] {
          Array(1, 2) should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "Array(1, 2) did not have size 55, and Array(1, 2) did not have size 22")

        val caught3 = intercept[AssertionError] {
          Array(1, 2) should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "Array(1, 2) did not have size 55, and Array(1, 2) did not have size 22")
      }

      it("should throw an assertion error when array size matches and used in a logical-and expression with not") {

        val caught1 = intercept[AssertionError] {
          Array(1, 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "Array(1, 2) did not have size 3, but Array(1, 2) had size 2")

        val caught2 = intercept[AssertionError] {
          Array(1, 2) should ((not have size (3)) and (not have size (2)))
        }
        assert(caught2.getMessage === "Array(1, 2) did not have size 3, but Array(1, 2) had size 2")

        val caught3 = intercept[AssertionError] {
          Array(1, 2) should (not have size (3) and not have size (2))
        }
        assert(caught3.getMessage === "Array(1, 2) did not have size 3, but Array(1, 2) had size 2")
      }

      it("should throw an assertion error when array size matches and used in a logical-or expression with not") {

        val caught1 = intercept[AssertionError] {
          Array(1, 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "Array(1, 2) had size 2, and Array(1, 2) had size 2")

        val caught2 = intercept[AssertionError] {
          Array(1, 2) should ((not have size (2)) or (not have size (2)))
        }
        assert(caught2.getMessage === "Array(1, 2) had size 2, and Array(1, 2) had size 2")

        val caught3 = intercept[AssertionError] {
          Array(1, 2) should (not have size (2) or not have size (2))
        }
        assert(caught3.getMessage === "Array(1, 2) had size 2, and Array(1, 2) had size 2")
      }
*/
    }
  }
}

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

      it("should throw AssertionError if comparison does not succeed") {

        val caught1 = intercept[AssertionError] {
          1 should be < (1)
        }
        assert(caught1.getMessage === "1 was not less than 1")
        check((left: Int, right: Int) => left >= right ==> throwsAssertionError(left should be < (right)))

        val caught2 = intercept[AssertionError] {
          2 should be <= (1)
        }
        assert(caught2.getMessage === "2 was not less than or equal to 1")
        check((left: Int, right: Int) => left > right ==> throwsAssertionError(left should be <= (right)))

        val caught3 = intercept[AssertionError] {
          1 should be > (1)
        }
        assert(caught3.getMessage === "1 was not greater than 1")
        check((left: Int, right: Int) => left <= right ==> throwsAssertionError(left should be > (right)))

        val caught4 = intercept[AssertionError] {
          1 should be >= (2)
        }
        assert(caught4.getMessage === "1 was not greater than or equal to 2")
        check((left: Int, right: Int) => left < right ==> throwsAssertionError(left should be >= (right)))
      }

      it("should throw AssertionError if comparison succeeds but used with not") {

        val caught1 = intercept[AssertionError] {
          1 should not be < (2)
        }
        assert(caught1.getMessage === "1 was less than 2")
        check((left: Int, right: Int) => left < right ==> throwsAssertionError(left should not be < (right)))

        val caught2 = intercept[AssertionError] {
          1 should not be <= (1)
        }
        assert(caught2.getMessage === "1 was less than or equal to 1")
        check((left: Int, right: Int) => left <= right ==> throwsAssertionError(left should not be <= (right)))

        val caught3 = intercept[AssertionError] {
          2 should not be > (1)
        }
        assert(caught3.getMessage === "2 was greater than 1")
        check((left: Int, right: Int) => left > right ==> throwsAssertionError(left should not be > (right)))

        val caught4 = intercept[AssertionError] {
          1 should not be >= (1)
        }
        assert(caught4.getMessage === "1 was greater than or equal to 1")
        check((left: Int, right: Int) => left >= right ==> throwsAssertionError(left should not be >= (right)))
      }

      it("should throw an assertion error when less than comparison doesn't succeed and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          2 should { be < (5) and (be < (2)) }
        }
        assert(caught1.getMessage === "2 was less than 5, but 2 was not less than 2")

        val caught2 = intercept[AssertionError] {
          2 should ((be < (5)) and (be < (2)))
        }
        assert(caught2.getMessage === "2 was less than 5, but 2 was not less than 2")

        val caught3 = intercept[AssertionError] {
          2 should (be < (5) and be < (2))
        }
        assert(caught3.getMessage === "2 was less than 5, but 2 was not less than 2")
      }

      it("should throw an assertion error when greater than comparison doesn't succeed and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          7 should { be > (5) and (be > (12)) }
        }
        assert(caught1.getMessage === "7 was greater than 5, but 7 was not greater than 12")

        val caught2 = intercept[AssertionError] {
          7 should ((be > (5)) and (be > (12)))
        }
        assert(caught2.getMessage === "7 was greater than 5, but 7 was not greater than 12")

        val caught3 = intercept[AssertionError] {
          7 should (be > (5) and be > (12))
        }
        assert(caught3.getMessage === "7 was greater than 5, but 7 was not greater than 12")
      }

      it("should throw an assertion error when less than or equal to comparison doesn't succeed and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          2 should { be <= (2) and (be <= (1)) }
        }
        assert(caught1.getMessage === "2 was less than or equal to 2, but 2 was not less than or equal to 1")

        val caught2 = intercept[AssertionError] {
          2 should ((be <= (2)) and (be <= (1)))
        }
        assert(caught2.getMessage === "2 was less than or equal to 2, but 2 was not less than or equal to 1")

        val caught3 = intercept[AssertionError] {
          2 should (be <= (2) and be <= (1))
        }
        assert(caught3.getMessage === "2 was less than or equal to 2, but 2 was not less than or equal to 1")
      }

      it("should throw an assertion error when greater than or equal to comparison doesn't succeed and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          7 should { be >= (7) and (be >= (8)) }
        }
        assert(caught1.getMessage === "7 was greater than or equal to 7, but 7 was not greater than or equal to 8")

        val caught2 = intercept[AssertionError] {
          7 should ((be >= (7)) and (be >= (8)))
        }
        assert(caught2.getMessage === "7 was greater than or equal to 7, but 7 was not greater than or equal to 8")

        val caught3 = intercept[AssertionError] {
          7 should (be >= (7) and be >= (8))
        }
        assert(caught3.getMessage === "7 was greater than or equal to 7, but 7 was not greater than or equal to 8")
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

      it("should throw AssertionError if comparison does not succeed") {

        val caught1 = intercept[AssertionError] {
          "aaa" should be < ("aaa")
        }
        assert(caught1.getMessage === "\"aaa\" was not less than \"aaa\"")
        check((left: String, right: String) => left >= right ==> throwsAssertionError(left should be < (right)))

        val caught2 = intercept[AssertionError] {
          "bbb" should be <= ("aaa")
        }
        assert(caught2.getMessage === "\"bbb\" was not less than or equal to \"aaa\"")
        check((left: String, right: String) => left > right ==> throwsAssertionError(left should be <= (right)))

        val caught3 = intercept[AssertionError] {
          "aaa" should be > ("aaa")
        }
        assert(caught3.getMessage === "\"aaa\" was not greater than \"aaa\"")
        check((left: String, right: String) => left <= right ==> throwsAssertionError(left should be > (right)))

        val caught4 = intercept[AssertionError] {
          "aaa" should be >= ("bbb")
        }
        assert(caught4.getMessage === "\"aaa\" was not greater than or equal to \"bbb\"")
        check((left: String, right: String) => left < right ==> throwsAssertionError(left should be >= (right)))
      }

      it("should throw AssertionError if comparison succeeds but used with not") {

        val caught1 = intercept[AssertionError] {
          "aaa" should not be < ("bbb")
        }
        assert(caught1.getMessage === "\"aaa\" was less than \"bbb\"")
        check((left: String, right: String) => left < right ==> throwsAssertionError(left should not be < (right)))

        val caught2 = intercept[AssertionError] {
          "aaa" should not be <= ("aaa")
        }
        assert(caught2.getMessage === "\"aaa\" was less than or equal to \"aaa\"")
        check((left: String, right: String) => left <= right ==> throwsAssertionError(left should not be <= (right)))

        val caught3 = intercept[AssertionError] {
          "bbb" should not be > ("aaa")
        }
        assert(caught3.getMessage === "\"bbb\" was greater than \"aaa\"")
        check((left: String, right: String) => left > right ==> throwsAssertionError(left should not be > (right)))

        val caught4 = intercept[AssertionError] {
          "aaa" should not be >= ("aaa")
        }
        assert(caught4.getMessage === "\"aaa\" was greater than or equal to \"aaa\"")
        check((left: String, right: String) => left >= right ==> throwsAssertionError(left should not be >= (right)))
      }

/*
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

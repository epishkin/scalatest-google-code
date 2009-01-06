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

class ShouldContainSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion {

  // Checking for a specific size
  describe("The 'contain element (Int)' syntax") {

    describe("on Array") {

      it("should do nothing if array contains the specified element") {
        Array(1, 2) should contain element (2)
        check((arr: Array[Int]) => arr.size != 0 ==> returnsNormally(arr should contain element (arr(arr.length - 1))))
      }

      it("should do nothing if array does not contain the element and used with should not") {
        Array(1, 2) should not { contain element (3) }
        check((arr: Array[Int], i: Int) => !arr.exists(_ == i) ==> returnsNormally(arr should not { contain element (i) }))
      }

      it("should do nothing when array contains the specified element and used in a logical-and expression") {
        Array(1, 2) should { contain element (2) and (contain element (1)) }
        // Array(1, 2) should both (contain element (2)) and (contain element (3 - 1))
       }

      it("should do nothing when array contains the specified element and used in a logical-or expression") {
        Array(1, 2) should { contain element (77) or (contain element (2)) }
        // Array(1, 2) should either (contain element (77)) or (contain element (3 - 1))
      }

      it("should do nothing when array doesn't contain the specified element and used in a logical-and expression with not") {
        Array(1, 2) should { not { contain element (5) } and not { contain element (3) }}
        // Array(1, 2) should both (not contain element (5)) and (either (not contain element (3)) or (equal (2)))
      }

      it("should do nothing when array doesn't contain the specified element and used in a logical-or expression with not") {
        Array(1, 2) should { not { contain element (2) } or not { contain element (3) }}
      }

      it("should throw AssertionError if array does not contain the specified element") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should contain element (3)
        }
        assert(caught.getMessage === "Array(1, 2) did not contain element 3")
        check((arr: Array[String], s: String) => !arr.exists(_ == s) ==> throwsAssertionError(arr should contain element (s)))
      }

      it("should throw an assertion error when array doesn't contain the specified element and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should { contain element (5) and (contain element (2 - 1)) }
        }
        assert(caught.getMessage === "Array(1, 2) did not contain element 5")
      }

      it("should throw an assertion error when array doesn't contain the specified element and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should { contain element (55) or (contain element (22)) }
        }
        assert(caught.getMessage === "Array(1, 2) did not contain element 55, and Array(1, 2) did not contain element 22")
      }

      it("should throw an assertion error when array contains the specified element and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should { not { contain element (3) } and not { contain element (2) }}
        }
        assert(caught.getMessage === "Array(1, 2) did not contain element 3, but Array(1, 2) contained element 2")
      }

      it("should throw an assertion error when array contains the specified element and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should { not { contain element (2) } or not { contain element (2) }}
        }
        assert(caught.getMessage === "Array(1, 2) contained element 2, and Array(1, 2) contained element 2")
      }
    }
  }
}

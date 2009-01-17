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

class ShouldSizeSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion {

  // Checking for a specific size
  describe("The 'have size (Int)' syntax") {

    describe("on Array") {

      it("should do nothing if array size matches specified size") {
        Array(1, 2) should have size (2)
        check((arr: Array[Int]) => returnsNormally(arr should have size (arr.size)))
      }

      it("should do nothing if array size does not match and used with should not") {
        Array(1, 2) should not { have size (3) }
        Array(1, 2) should not have size (3)
        check((arr: Array[Int], i: Int) => i != arr.size ==> returnsNormally(arr should not { have size (i) }))
        check((arr: Array[Int], i: Int) => i != arr.size ==> returnsNormally(arr should not have size (i)))
      }

      it("should do nothing when array size matches and used in a logical-and expression") {
        Array(1, 2) should { have size (2) and (have size (3 - 1)) }
        Array(1, 2) should ((have size (2)) and (have size (3 - 1)))
        Array(1, 2) should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when array size matches and used in a logical-or expression") { Array(1, 2) should { have size (77) or (have size (3 - 1)) }
        Array(1, 2) should ((have size (77)) or (have size (3 - 1)))
        Array(1, 2) should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when array size doesn't match and used in a logical-and expression with not") {
        Array(1, 2) should { not { have size (5) } and not { have size (3) }}
        Array(1, 2) should ((not have size (5)) and (not have size (3)))
        Array(1, 2) should (not have size (5) and not have size (3))
      }

      it("should do nothing when array size doesn't match and used in a logical-or expression with not") {
        Array(1, 2) should { not { have size (2) } or not { have size (3) }}
        Array(1, 2) should ((not have size (2)) or (not have size (3)))
        Array(1, 2) should (not have size (2) or not have size (3))
      }

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
      }

      it("should throw an assertion error when array size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          Array(1, 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "Array(1, 2) had size 2, and Array(1, 2) had size 2")
      }
    }

    describe("on scala.collection.immutable.Set") {

      it("should do nothing if set size matches specified size") {
        Set(1, 2) should have size (2)
        Set("one", "two") should have size (2)
        // check((set: Set[Int]) => returnsNormally(set should have size (set.size)))
      }

      it("should do nothing if set size does not match and used with should not") {
        Set(1, 2) should not { have size (3) }
        Set(1, 2) should not have size (3)
        // check((set: Set[Int], i: Int) => i != set.size ==> returnsNormally(set should not { have size (i) }))
      }

      it("should do nothing when set size matches and used in a logical-and expression") {
        Set(1, 2) should { have size (2) and (have size (3 - 1)) }
        Set(1, 2) should ((have size (2)) and (have size (3 - 1)))
        Set(1, 2) should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when set size matches and used in a logical-or expression") {
        Set(1, 2) should { have size (77) or (have size (3 - 1)) }
        Set(1, 2) should ((have size (77)) or (have size (3 - 1)))
        Set(1, 2) should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when set size doesn't match and used in a logical-and expression with not") {
        Set(1, 2) should { not { have size (5) } and not { have size (3) }}
        Set(1, 2) should ((not have size (5)) and (not have size (3)))
        Set(1, 2) should (not have size (5) and not have size (3))
      }

      it("should do nothing when set size doesn't match and used in a logical-or expression with not") {
        Set(1, 2) should { not { have size (2) } or not { have size (3) }}
        Set(1, 2) should ((not have size (2)) or (not have size (3)))
        Set(1, 2) should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if set size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          Set(1, 2) should have size (3)
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 3")
        // check((set: Set[String]) => throwsAssertionError(set should have size (set.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          Set(1, 2) should have size (-2)
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size -2")
        // check((set: Set[Int]) => throwsAssertionError(set should have size (if (set.size == 0) -1 else -set.size)))
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          Set(1, 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 5")

        val caught2 = intercept[AssertionError] {
          Set(1, 2) should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not have size 5")

        val caught3 = intercept[AssertionError] {
          Set(1, 2) should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "Set(1, 2) did not have size 5")
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          Set(1, 2) should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")

        val caught2 = intercept[AssertionError] {
          Set(1, 2) should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")

        val caught3 = intercept[AssertionError] {
          Set(1, 2) should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")
      }

      it("should throw an assertion error when set size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          Set(1, 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 3, but Set(1, 2) had size 2")
      }

      it("should throw an assertion error when set size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          Set(1, 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) had size 2, and Set(1, 2) had size 2")
      }
    }

    describe("on scala.collection.mutable.Set") {

      import scala.collection.mutable

      it("should do nothing if set size matches specified size") {
        mutable.Set(1, 2) should have size (2)
        mutable.Set("one", "two") should have size (2)
        // check((set: Set[Int]) => returnsNormally(set should have size (set.size)))
      }

      it("should do nothing if set size does not match and used with should not") {
        mutable.Set(1, 2) should not { have size (3) }
        mutable.Set(1, 2) should not have size (3)
        // check((set: Set[Int], i: Int) => i != set.size ==> returnsNormally(set should not { have size (i) }))
      }

      it("should do nothing when set size matches and used in a logical-and expression") {
        mutable.Set(1, 2) should { have size (2) and (have size (3 - 1)) }
        mutable.Set(1, 2) should ((have size (2)) and (have size (3 - 1)))
        mutable.Set(1, 2) should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when set size matches and used in a logical-or expression") {
        mutable.Set(1, 2) should { have size (77) or (have size (3 - 1)) }
        mutable.Set(1, 2) should ((have size (77)) or (have size (3 - 1)))
        mutable.Set(1, 2) should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when set size doesn't match and used in a logical-and expression with not") {
        mutable.Set(1, 2) should { not { have size (5) } and not { have size (3) }}
        mutable.Set(1, 2) should ((not have size (5)) and (not have size (3)))
        mutable.Set(1, 2) should (not have size (5) and not have size (3))
      }

      it("should do nothing when set size doesn't match and used in a logical-or expression with not") {
        mutable.Set(1, 2) should { not { have size (2) } or not { have size (3) }}
        mutable.Set(1, 2) should ((not have size (2)) or (not have size (3)))
        mutable.Set(1, 2) should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if set size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          mutable.Set(1, 2) should have size (3)
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 3")
        // check((set: Set[String]) => throwsAssertionError(set should have size (set.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          mutable.Set(1, 2) should have size (-2)
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size -2")
        // check((set: Set[Int]) => throwsAssertionError(set should have size (if (set.size == 0) -1 else -set.size)))
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          mutable.Set(1, 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 5")

        val caught2 = intercept[AssertionError] {
          mutable.Set(1, 2) should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not have size 5")

        val caught3 = intercept[AssertionError] {
          mutable.Set(1, 2) should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "Set(1, 2) did not have size 5")
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          mutable.Set(1, 2) should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")

        val caught2 = intercept[AssertionError] {
          mutable.Set(1, 2) should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")

        val caught3 = intercept[AssertionError] {
          mutable.Set(1, 2) should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")
      }

      it("should throw an assertion error when set size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          mutable.Set(1, 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 3, but Set(1, 2) had size 2")
      }

      it("should throw an assertion error when set size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          mutable.Set(1, 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) had size 2, and Set(1, 2) had size 2")
      }
    }

    describe("on scala.collection.Set") {

      val set: scala.collection.Set[Int] = Set(1, 2)

      it("should do nothing if set size matches specified size") {
        set should have size (2)
        Set("one", "two") should have size (2)
        // check((set: Set[Int]) => returnsNormally(set should have size (set.size)))
      }

      it("should do nothing if set size does not match and used with should not") {
        set should not { have size (3) }
        set should not have size (3)
        // check((set: Set[Int], i: Int) => i != set.size ==> returnsNormally(set should not { have size (i) }))
      }

      it("should do nothing when set size matches and used in a logical-and expression") {
        set should { have size (2) and (have size (3 - 1)) }
        set should ((have size (2)) and (have size (3 - 1)))
        set should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when set size matches and used in a logical-or expression") {
        set should { have size (77) or (have size (3 - 1)) }
        set should ((have size (77)) or (have size (3 - 1)))
        set should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when set size doesn't match and used in a logical-and expression with not") {
        set should { not { have size (5) } and not { have size (3) }}
        set should ((not have size (5)) and (not have size (3)))
        set should (not have size (5) and not have size (3))
      }

      it("should do nothing when set size doesn't match and used in a logical-or expression with not") {
        set should { not { have size (2) } or not { have size (3) }}
        set should ((not have size (2)) or (not have size (3)))
        set should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if set size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          set should have size (3)
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 3")
        // check((set: Set[String]) => throwsAssertionError(set should have size (set.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          set should have size (-2)
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size -2")
        // check((set: Set[Int]) => throwsAssertionError(set should have size (if (set.size == 0) -1 else -set.size)))
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          set should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 5")

        val caught2 = intercept[AssertionError] {
          set should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not have size 5")

        val caught3 = intercept[AssertionError] {
          set should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "Set(1, 2) did not have size 5")
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          set should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")

        val caught2 = intercept[AssertionError] {
          set should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")

        val caught3 = intercept[AssertionError] {
          set should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")
      }

      it("should throw an assertion error when set size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          set should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 3, but Set(1, 2) had size 2")
      }

      it("should throw an assertion error when set size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          set should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) had size 2, and Set(1, 2) had size 2")
      }
    }

    describe("on scala.collection.immutable.HashSet") {

      import scala.collection.immutable.HashSet
        
      it("should do nothing if set size matches specified size") {
        HashSet(1, 2) should have size (2)
        Set("one", "two") should have size (2)
        // check((set: Set[Int]) => returnsNormally(set should have size (set.size)))
      }

      it("should do nothing if set size does not match and used with should not") {
        HashSet(1, 2) should not { have size (3) }
        HashSet(1, 2) should not have size (3)
        // check((set: Set[Int], i: Int) => i != set.size ==> returnsNormally(set should not { have size (i) }))
      }

      it("should do nothing when set size matches and used in a logical-and expression") {
        HashSet(1, 2) should { have size (2) and (have size (3 - 1)) }
        HashSet(1, 2) should ((have size (2)) and (have size (3 - 1)))
        HashSet(1, 2) should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when set size matches and used in a logical-or expression") {
        HashSet(1, 2) should { have size (77) or (have size (3 - 1)) }
        HashSet(1, 2) should ((have size (77)) or (have size (3 - 1)))
        HashSet(1, 2) should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when set size doesn't match and used in a logical-and expression with not") {
        HashSet(1, 2) should { not { have size (5) } and not { have size (3) }}
        HashSet(1, 2) should ((not have size (5)) and (not have size (3)))
        HashSet(1, 2) should (not have size (5) and not have size (3))
      }

      it("should do nothing when set size doesn't match and used in a logical-or expression with not") {
        HashSet(1, 2) should { not { have size (2) } or not { have size (3) }}
        HashSet(1, 2) should ((not have size (2)) or (not have size (3)))
        HashSet(1, 2) should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if set size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          HashSet(1, 2) should have size (3)
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 3")
        // check((set: Set[String]) => throwsAssertionError(set should have size (set.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          HashSet(1, 2) should have size (-2)
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size -2")
        // check((set: Set[Int]) => throwsAssertionError(set should have size (if (set.size == 0) -1 else -set.size)))
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          HashSet(1, 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 5")

        val caught2 = intercept[AssertionError] {
          HashSet(1, 2) should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not have size 5")

        val caught3 = intercept[AssertionError] {
          HashSet(1, 2) should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "Set(1, 2) did not have size 5")
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          HashSet(1, 2) should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")

        val caught2 = intercept[AssertionError] {
          HashSet(1, 2) should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")

        val caught3 = intercept[AssertionError] {
          HashSet(1, 2) should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")
      }

      it("should throw an assertion error when set size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          HashSet(1, 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 3, but Set(1, 2) had size 2")
      }

      it("should throw an assertion error when set size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          HashSet(1, 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) had size 2, and Set(1, 2) had size 2")
      }
    }

    describe("on scala.collection.mutable.HashSet") {

      import scala.collection.mutable

      it("should do nothing if set size matches specified size") {
        mutable.HashSet(1, 2) should have size (2)
        mutable.HashSet("one", "two") should have size (2)
        // check((set: Set[Int]) => returnsNormally(set should have size (set.size)))
      }

      it("should do nothing if set size does not match and used with should not") {
        mutable.HashSet(1, 2) should not { have size (3) }
        mutable.HashSet(1, 2) should not have size (3)
        // check((set: Set[Int], i: Int) => i != set.size ==> returnsNormally(set should not { have size (i) }))
      }

      it("should do nothing when set size matches and used in a logical-and expression") {
        mutable.HashSet(1, 2) should { have size (2) and (have size (3 - 1)) }
        mutable.HashSet(1, 2) should ((have size (2)) and (have size (3 - 1)))
        mutable.HashSet(1, 2) should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when set size matches and used in a logical-or expression") {
        mutable.HashSet(1, 2) should { have size (77) or (have size (3 - 1)) }
        mutable.HashSet(1, 2) should ((have size (77)) or (have size (3 - 1)))
        mutable.HashSet(1, 2) should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when set size doesn't match and used in a logical-and expression with not") {
        mutable.HashSet(1, 2) should { not { have size (5) } and not { have size (3) }}
        mutable.HashSet(1, 2) should ((not have size (5)) and (not have size (3)))
        mutable.HashSet(1, 2) should (not have size (5) and not have size (3))
      }

      it("should do nothing when set size doesn't match and used in a logical-or expression with not") {
        mutable.HashSet(1, 2) should { not { have size (2) } or not { have size (3) }}
        mutable.HashSet(1, 2) should ((not have size (2)) or (not have size (3)))
        mutable.HashSet(1, 2) should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if set size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          mutable.HashSet(1, 2) should have size (3)
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 3")
        // check((set: Set[String]) => throwsAssertionError(set should have size (set.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          mutable.HashSet(1, 2) should have size (-2)
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size -2")
        // check((set: Set[Int]) => throwsAssertionError(set should have size (if (set.size == 0) -1 else -set.size)))
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          mutable.HashSet(1, 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 5")

        val caught2 = intercept[AssertionError] {
          mutable.HashSet(1, 2) should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not have size 5")

        val caught3 = intercept[AssertionError] {
          mutable.HashSet(1, 2) should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "Set(1, 2) did not have size 5")
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          mutable.HashSet(1, 2) should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")

        val caught2 = intercept[AssertionError] {
          mutable.HashSet(1, 2) should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")

        val caught3 = intercept[AssertionError] {
          mutable.HashSet(1, 2) should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")
      }

      it("should throw an assertion error when set size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          mutable.HashSet(1, 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) did not have size 3, but Set(1, 2) had size 2")
      }

      it("should throw an assertion error when set size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          mutable.HashSet(1, 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) had size 2, and Set(1, 2) had size 2")
      }
    }

    describe("on scala.List") {

      it("should do nothing if list size matches specified size") {
        List(1, 2) should have size (2)
        check((lst: List[Int]) => returnsNormally(lst should have size (lst.size)))
      }

      it("should do nothing if list size does not match and used with should not") {
        List(1, 2) should not { have size (3) }
        List(1, 2) should not have size (3)
        check((lst: List[Int], i: Int) => i != lst.size ==> returnsNormally(lst should not { have size (i) }))
      }

      it("should do nothing when list size matches and used in a logical-and expression") {
        List(1, 2) should { have size (2) and (have size (3 - 1)) }
        List(1, 2) should ((have size (2)) and (have size (3 - 1)))
        List(1, 2) should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when list size matches and used in a logical-or expression") {
        List(1, 2) should { have size (77) or (have size (3 - 1)) }
        List(1, 2) should ((have size (77)) or (have size (3 - 1)))
        List(1, 2) should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when list size doesn't match and used in a logical-and expression with not") {
        List(1, 2) should { not { have size (5) } and not { have size (3) }}
        List(1, 2) should ((not have size (5)) and (not have size (3)))
        List(1, 2) should (not have size (5) and not have size (3))
      }

      it("should do nothing when list size doesn't match and used in a logical-or expression with not") {
        List(1, 2) should { not { have size (2) } or not { have size (3) }}
        List(1, 2) should ((not have size (2)) or (not have size (3)))
        List(1, 2) should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if list size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          List(1, 2) should have size (3)
        }
        assert(caught1.getMessage === "List(1, 2) did not have size 3")
        check((lst: List[String]) => throwsAssertionError(lst should have size (lst.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          List(1, 2) should have size (-2)
        }
        assert(caught1.getMessage === "List(1, 2) did not have size -2")
        check((lst: List[Int]) => throwsAssertionError(lst should have size (if (lst.size == 0) -1 else -lst.size)))
      }

      it("should throw an assertion error when list size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          List(1, 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "List(1, 2) did not have size 5")

        val caught2 = intercept[AssertionError] {
          List(1, 2) should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "List(1, 2) did not have size 5")

        val caught3 = intercept[AssertionError] {
          List(1, 2) should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "List(1, 2) did not have size 5")
      }

      it("should throw an assertion error when list size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          List(1, 2) should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "List(1, 2) did not have size 55, and List(1, 2) did not have size 22")

        val caught2 = intercept[AssertionError] {
          List(1, 2) should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "List(1, 2) did not have size 55, and List(1, 2) did not have size 22")

        val caught3 = intercept[AssertionError] {
          List(1, 2) should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "List(1, 2) did not have size 55, and List(1, 2) did not have size 22")
      }

      it("should throw an assertion error when list size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          List(1, 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "List(1, 2) did not have size 3, but List(1, 2) had size 2")
      }

      it("should throw an assertion error when list size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          List(1, 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "List(1, 2) had size 2, and List(1, 2) had size 2")
      }
    }

    describe("on java.util.List") {

      val javaList: java.util.List[Int] = new java.util.ArrayList
      javaList.add(1)
      javaList.add(2)
      
      it("should do nothing if list size matches specified size") {
        javaList should have size (2)
        // check((lst: java.util.List[Int]) => returnsNormally(lst should have size (lst.size)))
      }

      it("should do nothing if list size does not match and used with should not") {
        javaList should not { have size (3) }
        javaList should not have size (3)
        // check((lst: List[Int], i: Int) => i != lst.size ==> returnsNormally(lst should not { have size (i) }))
      }

      it("should do nothing when list size matches and used in a logical-and expression") {
        javaList should { have size (2) and (have size (3 - 1)) }
        javaList should ((have size (2)) and (have size (3 - 1)))
        javaList should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when list size matches and used in a logical-or expression") {
        javaList should { have size (77) or (have size (3 - 1)) }
        javaList should ((have size (77)) or (have size (3 - 1)))
        javaList should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when list size doesn't match and used in a logical-and expression with not") {
        javaList should { not { have size (5) } and not { have size (3) }}
        javaList should ((not have size (5)) and (not have size (3)))
        javaList should (not have size (5) and not have size (3))
      }

      it("should do nothing when list size doesn't match and used in a logical-or expression with not") {
        javaList should { not { have size (2) } or not { have size (3) }}
        javaList should ((not have size (2)) or (not have size (3)))
        javaList should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if list size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          javaList should have size (3)
        }
        assert(caught1.getMessage === "[1, 2] did not have size 3")
        // check((lst: List[String]) => throwsAssertionError(lst should have size (lst.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          javaList should have size (-2)
        }
        assert(caught1.getMessage === "[1, 2] did not have size -2")
        // check((lst: List[Int]) => throwsAssertionError(lst should have size (if (lst.size == 0) -1 else -lst.size)))
      }

      it("should throw an assertion error when list size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          javaList should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "[1, 2] did not have size 5")

        val caught2 = intercept[AssertionError] {
          javaList should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "[1, 2] did not have size 5")

        val caught3 = intercept[AssertionError] {
          javaList should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "[1, 2] did not have size 5")
      }

      it("should throw an assertion error when list size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          javaList should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "[1, 2] did not have size 55, and [1, 2] did not have size 22")

        val caught2 = intercept[AssertionError] {
          javaList should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "[1, 2] did not have size 55, and [1, 2] did not have size 22")

        val caught3 = intercept[AssertionError] {
          javaList should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "[1, 2] did not have size 55, and [1, 2] did not have size 22")
      }

      it("should throw an assertion error when list size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          javaList should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "[1, 2] did not have size 3, but [1, 2] had size 2")
      }

      it("should throw an assertion error when list size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          javaList should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "[1, 2] had size 2, and [1, 2] had size 2")
      }
    }

    describe("on scala.collection.immutable.Map") {

      it("should do nothing if set size matches specified size") {
        Map("one" -> 1, "two" -> 2) should have size (2)
        Map(1 -> "one", 2 -> "two") should have size (2)
        // check((set: Map[Int]) => returnsNormally(set should have size (set.size)))
      }

      it("should do nothing if set size does not match and used with should not") {
        Map("one" -> 1, "two" -> 2) should not { have size (3) }
        Map("one" -> 1, "two" -> 2) should not have size (3)
        // check((set: Map[Int], i: Int) => i != set.size ==> returnsNormally(set should not { have size (i) }))
      }

      it("should do nothing when set size matches and used in a logical-and expression") {
        Map("one" -> 1, "two" -> 2) should { have size (2) and (have size (3 - 1)) }
        Map("one" -> 1, "two" -> 2) should ((have size (2)) and (have size (3 - 1)))
        Map("one" -> 1, "two" -> 2) should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when set size matches and used in a logical-or expression") {
        Map("one" -> 1, "two" -> 2) should { have size (77) or (have size (3 - 1)) }
        Map("one" -> 1, "two" -> 2) should ((have size (77)) or (have size (3 - 1)))
        Map("one" -> 1, "two" -> 2) should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when set size doesn't match and used in a logical-and expression with not") {
        Map("one" -> 1, "two" -> 2) should { not { have size (5) } and not { have size (3) }}
        Map("one" -> 1, "two" -> 2) should ((not have size (5)) and (not have size (3)))
        Map("one" -> 1, "two" -> 2) should (not have size (5) and not have size (3))
      }

      it("should do nothing when set size doesn't match and used in a logical-or expression with not") {
        Map("one" -> 1, "two" -> 2) should { not { have size (2) } or not { have size (3) }}
        Map("one" -> 1, "two" -> 2) should ((not have size (2)) or (not have size (3)))
        Map("one" -> 1, "two" -> 2) should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if set size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          Map("one" -> 1, "two" -> 2) should have size (3)
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 3")
        // check((set: Map[String]) => throwsAssertionError(set should have size (set.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          Map("one" -> 1, "two" -> 2) should have size (-2)
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size -2")
        // check((set: Map[Int]) => throwsAssertionError(set should have size (if (set.size == 0) -1 else -set.size)))
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          Map("one" -> 1, "two" -> 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 5")

        val caught2 = intercept[AssertionError] {
          Map("one" -> 1, "two" -> 2) should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not have size 5")

        val caught3 = intercept[AssertionError] {
          Map("one" -> 1, "two" -> 2) should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not have size 5")
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          Map("one" -> 1, "two" -> 2) should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")

        val caught2 = intercept[AssertionError] {
          Map("one" -> 1, "two" -> 2) should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")

        val caught3 = intercept[AssertionError] {
          Map("one" -> 1, "two" -> 2) should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")
      }

      it("should throw an assertion error when set size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          Map("one" -> 1, "two" -> 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 3, but Map(one -> 1, two -> 2) had size 2")
      }

      it("should throw an assertion error when set size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          Map("one" -> 1, "two" -> 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) had size 2, and Map(one -> 1, two -> 2) had size 2")
      }
    }

    describe("on scala.collection.mutable.Map") {

      import scala.collection.mutable

      it("should do nothing if set size matches specified size") {
        mutable.Map("one" -> 1, "two" -> 2) should have size (2)
        mutable.Map(1 -> "one", 2 -> "two") should have size (2)
        // check((set: Map[Int]) => returnsNormally(set should have size (set.size)))
      }

      it("should do nothing if set size does not match and used with should not") {
        mutable.Map("one" -> 1, "two" -> 2) should not { have size (3) }
        mutable.Map("one" -> 1, "two" -> 2) should not have size (3)
        // check((set: Map[Int], i: Int) => i != set.size ==> returnsNormally(set should not { have size (i) }))
      }

      it("should do nothing when set size matches and used in a logical-and expression") {
        mutable.Map("one" -> 1, "two" -> 2) should { have size (2) and (have size (3 - 1)) }
        mutable.Map("one" -> 1, "two" -> 2) should ((have size (2)) and (have size (3 - 1)))
        mutable.Map("one" -> 1, "two" -> 2) should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when set size matches and used in a logical-or expression") {
        mutable.Map("one" -> 1, "two" -> 2) should { have size (77) or (have size (3 - 1)) }
        mutable.Map("one" -> 1, "two" -> 2) should ((have size (77)) or (have size (3 - 1)))
        mutable.Map("one" -> 1, "two" -> 2) should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when set size doesn't match and used in a logical-and expression with not") {
        mutable.Map("one" -> 1, "two" -> 2) should { not { have size (5) } and not { have size (3) }}
        mutable.Map("one" -> 1, "two" -> 2) should ((not have size (5)) and (not have size (3)))
        mutable.Map("one" -> 1, "two" -> 2) should (not have size (5) and not have size (3))
      }

      it("should do nothing when set size doesn't match and used in a logical-or expression with not") {
        mutable.Map("one" -> 1, "two" -> 2) should { not { have size (2) } or not { have size (3) }}
        mutable.Map("one" -> 1, "two" -> 2) should ((not have size (2)) or (not have size (3)))
        mutable.Map("one" -> 1, "two" -> 2) should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if set size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          mutable.Map("one" -> 1, "two" -> 2) should have size (3)
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 3")
        // check((set: Map[String]) => throwsAssertionError(set should have size (set.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          mutable.Map("one" -> 1, "two" -> 2) should have size (-2)
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size -2")
        // check((set: Map[Int]) => throwsAssertionError(set should have size (if (set.size == 0) -1 else -set.size)))
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          mutable.Map("one" -> 1, "two" -> 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 5")

        val caught2 = intercept[AssertionError] {
          mutable.Map("one" -> 1, "two" -> 2) should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not have size 5")

        val caught3 = intercept[AssertionError] {
          mutable.Map("one" -> 1, "two" -> 2) should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not have size 5")
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          mutable.Map("one" -> 1, "two" -> 2) should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")

        val caught2 = intercept[AssertionError] {
          mutable.Map("one" -> 1, "two" -> 2) should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")

        val caught3 = intercept[AssertionError] {
          mutable.Map("one" -> 1, "two" -> 2) should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")
      }

      it("should throw an assertion error when set size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          mutable.Map("one" -> 1, "two" -> 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 3, but Map(one -> 1, two -> 2) had size 2")
      }

      it("should throw an assertion error when set size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          mutable.Map("one" -> 1, "two" -> 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) had size 2, and Map(one -> 1, two -> 2) had size 2")
      }
    }

    describe("on scala.collection.Map") {

      val map: scala.collection.Map[String, Int] = Map("one" -> 1, "two" -> 2)

      it("should do nothing if set size matches specified size") {
        map should have size (2)
        Map(1 -> "one", 2 -> "two") should have size (2)
        // check((set: Map[Int]) => returnsNormally(set should have size (set.size)))
      }

      it("should do nothing if set size does not match and used with should not") {
        map should not { have size (3) }
        map should not have size (3)
        // check((set: Map[Int], i: Int) => i != set.size ==> returnsNormally(set should not { have size (i) }))
      }

      it("should do nothing when set size matches and used in a logical-and expression") {
        map should { have size (2) and (have size (3 - 1)) }
        map should ((have size (2)) and (have size (3 - 1)))
        map should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when set size matches and used in a logical-or expression") {
        map should { have size (77) or (have size (3 - 1)) }
        map should ((have size (77)) or (have size (3 - 1)))
        map should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when set size doesn't match and used in a logical-and expression with not") {
        map should { not { have size (5) } and not { have size (3) }}
        map should ((not have size (5)) and (not have size (3)))
        map should (not have size (5) and not have size (3))
      }

      it("should do nothing when set size doesn't match and used in a logical-or expression with not") {
        map should { not { have size (2) } or not { have size (3) }}
        map should ((not have size (2)) or (not have size (3)))
        map should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if set size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          map should have size (3)
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 3")
        // check((set: Map[String]) => throwsAssertionError(set should have size (set.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          map should have size (-2)
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size -2")
        // check((set: Map[Int]) => throwsAssertionError(set should have size (if (set.size == 0) -1 else -set.size)))
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          map should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 5")

        val caught2 = intercept[AssertionError] {
          map should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not have size 5")

        val caught3 = intercept[AssertionError] {
          map should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not have size 5")
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          map should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")

        val caught2 = intercept[AssertionError] {
          map should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")

        val caught3 = intercept[AssertionError] {
          map should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")
      }

      it("should throw an assertion error when set size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          map should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 3, but Map(one -> 1, two -> 2) had size 2")
      }

      it("should throw an assertion error when set size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          map should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) had size 2, and Map(one -> 1, two -> 2) had size 2")
      }
    }

    describe("on scala.collection.immutable.HashMap") {

      import scala.collection.immutable.HashMap

      it("should do nothing if set size matches specified size") {
        HashMap("one" -> 1, "two" -> 2) should have size (2)
        Map(1 -> "one", 2 -> "two") should have size (2)
        // check((set: Map[Int]) => returnsNormally(set should have size (set.size)))
      }

      it("should do nothing if set size does not match and used with should not") {
        HashMap("one" -> 1, "two" -> 2) should not { have size (3) }
        HashMap("one" -> 1, "two" -> 2) should not have size (3)
        // check((set: Map[Int], i: Int) => i != set.size ==> returnsNormally(set should not { have size (i) }))
      }

      it("should do nothing when set size matches and used in a logical-and expression") {
        HashMap("one" -> 1, "two" -> 2) should { have size (2) and (have size (3 - 1)) }
        HashMap("one" -> 1, "two" -> 2) should ((have size (2)) and (have size (3 - 1)))
        HashMap("one" -> 1, "two" -> 2) should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when set size matches and used in a logical-or expression") {
        HashMap("one" -> 1, "two" -> 2) should { have size (77) or (have size (3 - 1)) }
        HashMap("one" -> 1, "two" -> 2) should ((have size (77)) or (have size (3 - 1)))
        HashMap("one" -> 1, "two" -> 2) should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when set size doesn't match and used in a logical-and expression with not") {
        HashMap("one" -> 1, "two" -> 2) should { not { have size (5) } and not { have size (3) }}
        HashMap("one" -> 1, "two" -> 2) should ((not have size (5)) and (not have size (3)))
        HashMap("one" -> 1, "two" -> 2) should (not have size (5) and not have size (3))
      }

      it("should do nothing when set size doesn't match and used in a logical-or expression with not") {
        HashMap("one" -> 1, "two" -> 2) should { not { have size (2) } or not { have size (3) }}
        HashMap("one" -> 1, "two" -> 2) should ((not have size (2)) or (not have size (3)))
        HashMap("one" -> 1, "two" -> 2) should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if set size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          HashMap("one" -> 1, "two" -> 2) should have size (3)
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 3")
        // check((set: Map[String]) => throwsAssertionError(set should have size (set.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          HashMap("one" -> 1, "two" -> 2) should have size (-2)
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size -2")
        // check((set: Map[Int]) => throwsAssertionError(set should have size (if (set.size == 0) -1 else -set.size)))
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          HashMap("one" -> 1, "two" -> 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 5")

        val caught2 = intercept[AssertionError] {
          HashMap("one" -> 1, "two" -> 2) should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not have size 5")

        val caught3 = intercept[AssertionError] {
          HashMap("one" -> 1, "two" -> 2) should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not have size 5")
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          HashMap("one" -> 1, "two" -> 2) should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")

        val caught2 = intercept[AssertionError] {
          HashMap("one" -> 1, "two" -> 2) should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")

        val caught3 = intercept[AssertionError] {
          HashMap("one" -> 1, "two" -> 2) should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")
      }

      it("should throw an assertion error when set size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          HashMap("one" -> 1, "two" -> 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 3, but Map(one -> 1, two -> 2) had size 2")
      }

      it("should throw an assertion error when set size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          HashMap("one" -> 1, "two" -> 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) had size 2, and Map(one -> 1, two -> 2) had size 2")
      }
    }

    describe("on scala.collection.mutable.HashMap") {

      import scala.collection.mutable

      it("should do nothing if set size matches specified size") {
        mutable.HashMap("one" -> 1, "two" -> 2) should have size (2)
        mutable.HashMap(1 -> "one", 2 -> "two") should have size (2)
        // check((set: Map[Int]) => returnsNormally(set should have size (set.size)))
      }

      it("should do nothing if set size does not match and used with should not") {
        mutable.HashMap("one" -> 1, "two" -> 2) should not { have size (3) }
        mutable.HashMap("one" -> 1, "two" -> 2) should not have size (3)
        // check((set: Map[Int], i: Int) => i != set.size ==> returnsNormally(set should not { have size (i) }))
      }

      it("should do nothing when set size matches and used in a logical-and expression") {
        mutable.HashMap("one" -> 1, "two" -> 2) should { have size (2) and (have size (3 - 1)) }
        mutable.HashMap("one" -> 1, "two" -> 2) should ((have size (2)) and (have size (3 - 1)))
        mutable.HashMap("one" -> 1, "two" -> 2) should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when set size matches and used in a logical-or expression") {
        mutable.HashMap("one" -> 1, "two" -> 2) should { have size (77) or (have size (3 - 1)) }
        mutable.HashMap("one" -> 1, "two" -> 2) should ((have size (77)) or (have size (3 - 1)))
        mutable.HashMap("one" -> 1, "two" -> 2) should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when set size doesn't match and used in a logical-and expression with not") {
        mutable.HashMap("one" -> 1, "two" -> 2) should { not { have size (5) } and not { have size (3) }}
        mutable.HashMap("one" -> 1, "two" -> 2) should ((not have size (5)) and (not have size (3)))
        mutable.HashMap("one" -> 1, "two" -> 2) should (not have size (5) and not have size (3))
      }

      it("should do nothing when set size doesn't match and used in a logical-or expression with not") {
        mutable.HashMap("one" -> 1, "two" -> 2) should { not { have size (2) } or not { have size (3) }}
        mutable.HashMap("one" -> 1, "two" -> 2) should ((not have size (2)) or (not have size (3)))
        mutable.HashMap("one" -> 1, "two" -> 2) should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if set size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          mutable.HashMap("one" -> 1, "two" -> 2) should have size (3)
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 3")
        // check((set: Map[String]) => throwsAssertionError(set should have size (set.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          mutable.HashMap("one" -> 1, "two" -> 2) should have size (-2)
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size -2")
        // check((set: Map[Int]) => throwsAssertionError(set should have size (if (set.size == 0) -1 else -set.size)))
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          mutable.HashMap("one" -> 1, "two" -> 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 5")

        val caught2 = intercept[AssertionError] {
          mutable.HashMap("one" -> 1, "two" -> 2) should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not have size 5")

        val caught3 = intercept[AssertionError] {
          mutable.HashMap("one" -> 1, "two" -> 2) should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not have size 5")
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          mutable.HashMap("one" -> 1, "two" -> 2) should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")

        val caught2 = intercept[AssertionError] {
          mutable.HashMap("one" -> 1, "two" -> 2) should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")

        val caught3 = intercept[AssertionError] {
          mutable.HashMap("one" -> 1, "two" -> 2) should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "Map(one -> 1, two -> 2) did not have size 55, and Map(one -> 1, two -> 2) did not have size 22")
      }

      it("should throw an assertion error when set size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          mutable.HashMap("one" -> 1, "two" -> 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) did not have size 3, but Map(one -> 1, two -> 2) had size 2")
      }

      it("should throw an assertion error when set size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          mutable.HashMap("one" -> 1, "two" -> 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "Map(one -> 1, two -> 2) had size 2, and Map(one -> 1, two -> 2) had size 2")
      }
    }

    describe("on java.util.Set") {

      val javaSet: java.util.Set[Int] = new java.util.HashSet
      javaSet.add(1)
      javaSet.add(2)

      it("should do nothing if list size matches specified size") {
        javaSet should have size (2)
        // check((lst: java.util.List[Int]) => returnsNormally(lst should have size (lst.size)))
      }

      it("should do nothing if list size does not match and used with should not") {
        javaSet should not { have size (3) }
        javaSet should not have size (3)
        // check((lst: List[Int], i: Int) => i != lst.size ==> returnsNormally(lst should not { have size (i) }))
      }

      it("should do nothing when list size matches and used in a logical-and expression") {
        javaSet should { have size (2) and (have size (3 - 1)) }
        javaSet should ((have size (2)) and (have size (3 - 1)))
        javaSet should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when list size matches and used in a logical-or expression") {
        javaSet should { have size (77) or (have size (3 - 1)) }
        javaSet should ((have size (77)) or (have size (3 - 1)))
        javaSet should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when list size doesn't match and used in a logical-and expression with not") {
        javaSet should { not { have size (5) } and not { have size (3) }}
        javaSet should ((not have size (5)) and (not have size (3)))
        javaSet should (not have size (5) and not have size (3))
      }

      it("should do nothing when list size doesn't match and used in a logical-or expression with not") {
        javaSet should { not { have size (2) } or not { have size (3) }}
        javaSet should ((not have size (2)) or (not have size (3)))
        javaSet should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if list size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          javaSet should have size (3)
        }
        assert(caught1.getMessage === "[2, 1] did not have size 3")
        // check((lst: List[String]) => throwsAssertionError(lst should have size (lst.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          javaSet should have size (-2)
        }
        assert(caught1.getMessage === "[2, 1] did not have size -2")
        // check((lst: List[Int]) => throwsAssertionError(lst should have size (if (lst.size == 0) -1 else -lst.size)))
      }

      it("should throw an assertion error when list size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          javaSet should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "[2, 1] did not have size 5")

        val caught2 = intercept[AssertionError] {
          javaSet should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "[2, 1] did not have size 5")

        val caught3 = intercept[AssertionError] {
          javaSet should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "[2, 1] did not have size 5")
      }

      it("should throw an assertion error when list size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          javaSet should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "[2, 1] did not have size 55, and [2, 1] did not have size 22")

        val caught2 = intercept[AssertionError] {
          javaSet should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "[2, 1] did not have size 55, and [2, 1] did not have size 22")

        val caught3 = intercept[AssertionError] {
          javaSet should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "[2, 1] did not have size 55, and [2, 1] did not have size 22")
      }

      it("should throw an assertion error when list size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          javaSet should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "[2, 1] did not have size 3, but [2, 1] had size 2")
      }

      it("should throw an assertion error when list size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          javaSet should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "[2, 1] had size 2, and [2, 1] had size 2")
      }
    }

    describe("on java.util.Map") {

      val javaMap: java.util.Map[String, Int] = new java.util.HashMap
      javaMap.put("one",1)
      javaMap.put("two", 2)

      it("should do nothing if list size matches specified size") {
        javaMap should have size (2)
        // check((lst: java.util.List[Int]) => returnsNormally(lst should have size (lst.size)))
      }

      it("should do nothing if list size does not match and used with should not") {
        javaMap should not { have size (3) }
        // check((lst: List[Int], i: Int) => i != lst.size ==> returnsNormally(lst should not { have size (i) }))
      }

      it("should do nothing when list size matches and used in a logical-and expression") {
        javaMap should { have size (2) and (have size (3 - 1)) }
        javaMap should ((have size (2)) and (have size (3 - 1)))
        javaMap should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when list size matches and used in a logical-or expression") {
        javaMap should { have size (77) or (have size (3 - 1)) }
        javaMap should ((have size (77)) or (have size (3 - 1)))
        javaMap should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when list size doesn't match and used in a logical-and expression with not") {
        javaMap should { not { have size (5) } and not { have size (3) }}
        javaMap should ((not have size (5)) and (not have size (3)))
        javaMap should (not have size (5) and not have size (3))
      }

      it("should do nothing when list size doesn't match and used in a logical-or expression with not") {
        javaMap should { not { have size (2) } or not { have size (3) }}
        javaMap should ((not have size (2)) or (not have size (3)))
        javaMap should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if list size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          javaMap should have size (3)
        }
        assert(caught1.getMessage === "{one=1, two=2} did not have size 3")
        // check((lst: List[String]) => throwsAssertionError(lst should have size (lst.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          javaMap should have size (-2)
        }
        assert(caught1.getMessage === "{one=1, two=2} did not have size -2")
        // check((lst: List[Int]) => throwsAssertionError(lst should have size (if (lst.size == 0) -1 else -lst.size)))
      }

      it("should throw an assertion error when list size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          javaMap should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "{one=1, two=2} did not have size 5")

        val caught2 = intercept[AssertionError] {
          javaMap should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "{one=1, two=2} did not have size 5")

        val caught3 = intercept[AssertionError] {
          javaMap should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "{one=1, two=2} did not have size 5")
      }

      it("should throw an assertion error when list size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          javaMap should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "{one=1, two=2} did not have size 55, and {one=1, two=2} did not have size 22")

        val caught2 = intercept[AssertionError] {
          javaMap should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "{one=1, two=2} did not have size 55, and {one=1, two=2} did not have size 22")

        val caught3 = intercept[AssertionError] {
          javaMap should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "{one=1, two=2} did not have size 55, and {one=1, two=2} did not have size 22")
      }

      it("should throw an assertion error when list size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          javaMap should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "{one=1, two=2} did not have size 3, but {one=1, two=2} had size 2")
      }

      it("should throw an assertion error when list size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          javaMap should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "{one=1, two=2} had size 2, and {one=1, two=2} had size 2")
      }
    }

    // I repeat these with copy and paste, becuase I need to test that each static structural type works, and
    // that makes it hard to pass them to a common "behaves like" method
    describe("on an arbitrary object that has an empty-paren Int size method") {
  
      class Sizey(len: Int) {
        def size(): Int = len
        override def toString = "sizey"
      }
      val obj = new Sizey(2)
  
      it("should do nothing if object size matches specified size") {
        obj should have size (2)
        check((len: Int) => returnsNormally(new Sizey(len) should have size (len)))
      }
  
      it("should do nothing if object size does not match and used with should not") {
        obj should not { have size (3) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
      }
  
      it("should do nothing when object size matches and used in a logical-and expression") {
        obj should { have size (2) and (have size (3 - 1)) }
        obj should ((have size (2)) and (have size (3 - 1)))
        obj should (have size (2) and have size (3 - 1))
      }
  
      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
        obj should ((have size (77)) or (have size (3 - 1)))
        obj should (have size (77) or have size (3 - 1))
      }
  
      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
        obj should ((not have size (5)) and (not have size (3)))
        obj should (not have size (5) and not have size (3))
      }
  
      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
        obj should ((not have size (2)) or (not have size (3)))
        obj should (not have size (2) or not have size (3))
      }
  
      it("should throw AssertionError if object size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught1.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }
  
      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught1.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }
  
      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "sizey did not have size 5")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "sizey did not have size 5")

        val caught3 = intercept[AssertionError] {
          obj should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "sizey did not have size 5")
      }
  
      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught3 = intercept[AssertionError] {
          obj should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }
  
      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey did not have size 3, but sizey had size 2")
      }
  
      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey had size 2, and sizey had size 2")
      }
    }

    describe("on an arbitrary object that has a parameterless Int size method") {

      class Sizey(len: Int) {
        def size: Int = len  // The only difference between the previous is the structure of this member
        override def toString = "sizey"
      }
      val obj = new Sizey(2)

      it("should do nothing if object size matches specified size") {
        obj should have size (2)
        check((len: Int) => returnsNormally(new Sizey(len) should have size (len)))
      }

      it("should do nothing if object size does not match and used with should not") {
        obj should not { have size (3) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
      }

      it("should do nothing when object size matches and used in a logical-and expression") {
        obj should { have size (2) and (have size (3 - 1)) }
        obj should ((have size (2)) and (have size (3 - 1)))
        obj should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
        obj should ((have size (77)) or (have size (3 - 1)))
        obj should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
        obj should ((not have size (5)) and (not have size (3)))
        obj should (not have size (5) and not have size (3))
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
        obj should ((not have size (2)) or (not have size (3)))
        obj should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught1.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught1.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "sizey did not have size 5")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "sizey did not have size 5")

        val caught3 = intercept[AssertionError] {
          obj should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught3 = intercept[AssertionError] {
          obj should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey had size 2, and sizey had size 2")
      }
    }

    describe("on an arbitrary object that has a Int size field") {

      class Sizey(len: Int) {
        val size: Int = len // The only difference between the previous is the structure of this member
        override def toString = "sizey"
      }
      val obj = new Sizey(2)

      it("should do nothing if object size matches specified size") {
        obj should have size (2)
        check((len: Int) => returnsNormally(new Sizey(len) should have size (len)))
      }

      it("should do nothing if object size does not match and used with should not") {
        obj should not { have size (3) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
      }

      it("should do nothing when object size matches and used in a logical-and expression") {
        obj should { have size (2) and (have size (3 - 1)) }
        obj should ((have size (2)) and (have size (3 - 1)))
        obj should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
        obj should ((have size (77)) or (have size (3 - 1)))
        obj should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
        obj should ((not have size (5)) and (not have size (3)))
        obj should (not have size (5) and not have size (3))
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
        obj should ((not have size (2)) or (not have size (3)))
        obj should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught1.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught1.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "sizey did not have size 5")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "sizey did not have size 5")

        val caught3 = intercept[AssertionError] {
          obj should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught3 = intercept[AssertionError] {
          obj should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey had size 2, and sizey had size 2")
      }
    }

    describe("on an arbitrary object that has an empty-paren Int getSize method") {

      class Sizey(len: Int) {
        def getSize(): Int = len  // The only difference between the previous is the structure of this member
        override def toString = "sizey"
      }
      val obj = new Sizey(2)

      it("should do nothing if object size matches specified size") {
        obj should have size (2)
        check((len: Int) => returnsNormally(new Sizey(len) should have size (len)))
      }

      it("should do nothing if object size does not match and used with should not") {
        obj should not { have size (3) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
      }

      it("should do nothing when object size matches and used in a logical-and expression") {
        obj should { have size (2) and (have size (3 - 1)) }
        obj should ((have size (2)) and (have size (3 - 1)))
        obj should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
        obj should ((have size (77)) or (have size (3 - 1)))
        obj should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
        obj should ((not have size (5)) and (not have size (3)))
        obj should (not have size (5) and not have size (3))
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
        obj should ((not have size (2)) or (not have size (3)))
        obj should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught1.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught1.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "sizey did not have size 5")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "sizey did not have size 5")

        val caught3 = intercept[AssertionError] {
          obj should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught3 = intercept[AssertionError] {
          obj should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey had size 2, and sizey had size 2")
      }
    }

    describe("on an arbitrary object that has a parameterless Int getSize method") {

      class Sizey(len: Int) {
        def getSize: Int = len  // The only difference between the previous is the structure of this member
        override def toString = "sizey"
      }
      val obj = new Sizey(2)

      it("should do nothing if object size matches specified size") {
        obj should have size (2)
        check((len: Int) => returnsNormally(new Sizey(len) should have size (len)))
      }

      it("should do nothing if object size does not match and used with should not") {
        obj should not { have size (3) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
      }

      it("should do nothing when object size matches and used in a logical-and expression") {
        obj should { have size (2) and (have size (3 - 1)) }
        obj should ((have size (2)) and (have size (3 - 1)))
        obj should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
        obj should ((have size (77)) or (have size (3 - 1)))
        obj should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
        obj should ((not have size (5)) and (not have size (3)))
        obj should (not have size (5) and not have size (3))
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
        obj should ((not have size (2)) or (not have size (3)))
        obj should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught1.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught1.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "sizey did not have size 5")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "sizey did not have size 5")

        val caught3 = intercept[AssertionError] {
          obj should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught3 = intercept[AssertionError] {
          obj should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey had size 2, and sizey had size 2")
      }
    }

    describe("on an arbitrary object that has an Int getSize field") {

      class Sizey(len: Int) {
        val getSize: Int = len // The only difference between the previous is the structure of this member
        override def toString = "sizey"
      }
      val obj = new Sizey(2)

      it("should do nothing if object size matches specified size") {
        obj should have size (2)
        check((len: Int) => returnsNormally(new Sizey(len) should have size (len)))
      }

      it("should do nothing if object size does not match and used with should not") {
        obj should not { have size (3) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
      }

      it("should do nothing when object size matches and used in a logical-and expression") {
        obj should { have size (2) and (have size (3 - 1)) }
        obj should ((have size (2)) and (have size (3 - 1)))
        obj should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
        obj should ((have size (77)) or (have size (3 - 1)))
        obj should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
        obj should ((not have size (5)) and (not have size (3)))
        obj should (not have size (5) and not have size (3))
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
        obj should ((not have size (2)) or (not have size (3)))
        obj should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught1.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught1.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "sizey did not have size 5")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "sizey did not have size 5")

        val caught3 = intercept[AssertionError] {
          obj should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught3 = intercept[AssertionError] {
          obj should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey had size 2, and sizey had size 2")
      }
    }

    describe("on an arbitrary object that has an empty-paren Long size method") {

      class Sizey(len: Long) {
        def size(): Long = len
        override def toString = "sizey"
      }
      val obj = new Sizey(2)

      it("should do nothing if object size matches specified size") {
        obj should have size (2)
        obj should have size (2L)
        check((len: Int) => returnsNormally(new Sizey(len) should have size (len)))
        check((len: Long) => returnsNormally(new Sizey(len) should have size (len)))
      }

      it("should do nothing if object size does not match and used with should not") {
        obj should not { have size (3) }
        obj should not { have size (3L) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
        check((len: Long, wrongLen: Long) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
      }

      it("should do nothing when object size matches and used in a logical-and expression") {
        obj should { have size (2) and (have size (3 - 1)) }
        obj should { have size (2L) and (have size (3 - 1)) }
        obj should ((have size (2)) and (have size (3 - 1)))
        obj should ((have size (2L)) and (have size (3 - 1)))
        obj should (have size (2) and have size (3 - 1))
        obj should (have size (2L) and have size (3 - 1))
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (2L)) }
        obj should { have size (77L) or (have size (2)) }
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
        obj should ((not have size (5)) and (not have size (3)))
        obj should (not have size (5) and not have size (3))
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
        obj should ((not have size (2)) or (not have size (3)))
        obj should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught1.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught1.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "sizey did not have size 5")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "sizey did not have size 5")

        val caught3 = intercept[AssertionError] {
          obj should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught3 = intercept[AssertionError] {
          obj should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey had size 2, and sizey had size 2")
      }
    }

    describe("on an arbitrary object that has a parameterless Long size method") {

      class Sizey(len: Long) {
        def size: Long = len  // The only difference between the previous is the structure of this member
        override def toString = "sizey"
      }
      val obj = new Sizey(2)

      it("should do nothing if object size matches specified size") {
        obj should have size (2)
        obj should have size (2L)
        check((len: Int) => returnsNormally(new Sizey(len) should have size (len)))
        check((len: Long) => returnsNormally(new Sizey(len) should have size (len)))
      }

      it("should do nothing if object size does not match and used with should not") {
        obj should not { have size (3) }
        obj should not { have size (3L) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
        check((len: Long, wrongLen: Long) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
      }

      it("should do nothing when object size matches and used in a logical-and expression") {
        obj should { have size (2) and (have size (3 - 1)) }
        obj should ((have size (2)) and (have size (3 - 1)))
        obj should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
        obj should ((have size (77)) or (have size (3 - 1)))
        obj should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
        obj should ((not have size (5)) and (not have size (3)))
        obj should (not have size (5) and not have size (3))
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
        obj should ((not have size (2)) or (not have size (3)))
        obj should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught1.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught1.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "sizey did not have size 5")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "sizey did not have size 5")

        val caught3 = intercept[AssertionError] {
          obj should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught3 = intercept[AssertionError] {
          obj should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey had size 2, and sizey had size 2")
      }
    }

    describe("on an arbitrary object that has a Long size field") {

      class Sizey(len: Long) {
        val size: Long = len // The only difference between the previous is the structure of this member
        override def toString = "sizey"
      }
      val obj = new Sizey(2)

      it("should do nothing if object size matches specified size") {
        obj should have size (2)
        obj should have size (2L)
        check((len: Int) => returnsNormally(new Sizey(len) should have size (len)))
        check((len: Long) => returnsNormally(new Sizey(len) should have size (len)))
      }

      it("should do nothing if object size does not match and used with should not") {
        obj should not { have size (3) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
      }

      it("should do nothing when object size matches and used in a logical-and expression") {
        obj should { have size (2) and (have size (3 - 1)) }
        obj should ((have size (2)) and (have size (3 - 1)))
        obj should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
        obj should ((have size (77)) or (have size (3 - 1)))
        obj should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
        obj should ((not have size (5)) and (not have size (3)))
        obj should (not have size (5) and not have size (3))
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
        obj should ((not have size (2)) or (not have size (3)))
        obj should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught1.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught1.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "sizey did not have size 5")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "sizey did not have size 5")

        val caught3 = intercept[AssertionError] {
          obj should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught3 = intercept[AssertionError] {
          obj should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey had size 2, and sizey had size 2")
      }
    }

    describe("on an arbitrary object that has an empty-paren Long getSize method") {

      class Sizey(len: Long) {
        def getSize(): Long = len  // The only difference between the previous is the structure of this member
        override def toString = "sizey"
      }
      val obj = new Sizey(2)

      it("should do nothing if object size matches specified size") {
        obj should have size (2)
        obj should have size (2L)
        check((len: Int) => returnsNormally(new Sizey(len) should have size (len)))
        check((len: Long) => returnsNormally(new Sizey(len) should have size (len)))
      }

      it("should do nothing if object size does not match and used with should not") {
        obj should not { have size (3) }
        obj should not { have size (3) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
        check((len: Long, wrongLen: Long) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
      }

      it("should do nothing when object size matches and used in a logical-and expression") {
        obj should { have size (2) and (have size (3 - 1)) }
        obj should ((have size (2)) and (have size (3 - 1)))
        obj should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
        obj should ((have size (77)) or (have size (3 - 1)))
        obj should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
        obj should ((not have size (5)) and (not have size (3)))
        obj should (not have size (5) and not have size (3))
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
        obj should ((not have size (2)) or (not have size (3)))
        obj should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught1.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught1.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "sizey did not have size 5")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "sizey did not have size 5")

        val caught3 = intercept[AssertionError] {
          obj should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught3 = intercept[AssertionError] {
          obj should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey had size 2, and sizey had size 2")
      }
    }

    describe("on an arbitrary object that has a parameterless Long getSize method") {

      class Sizey(len: Long) {
        def getSize: Long = len  // The only difference between the previous is the structure of this member
        override def toString = "sizey"
      }
      val obj = new Sizey(2)

      it("should do nothing if object size matches specified size") {
        obj should have size (2)
        obj should have size (2L)
        check((len: Int) => returnsNormally(new Sizey(len) should have size (len)))
        check((len: Long) => returnsNormally(new Sizey(len) should have size (len)))
      }

      it("should do nothing if object size does not match and used with should not") {
        obj should not { have size (3) }
        obj should not { have size (3L) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
        check((len: Long, wrongLen: Long) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
      }

      it("should do nothing when object size matches and used in a logical-and expression") {
        obj should { have size (2) and (have size (3 - 1)) }
        obj should ((have size (2)) and (have size (3 - 1)))
        obj should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
        obj should ((have size (77)) or (have size (3 - 1)))
        obj should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
        obj should ((not have size (5)) and (not have size (3)))
        obj should (not have size (5) and not have size (3))
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
        obj should ((not have size (2)) or (not have size (3)))
        obj should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught1.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught1.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "sizey did not have size 5")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "sizey did not have size 5")

        val caught3 = intercept[AssertionError] {
          obj should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught3 = intercept[AssertionError] {
          obj should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey had size 2, and sizey had size 2")
      }
    }

    describe("on an arbitrary object that has a Long getSize field") {

      class Sizey(len: Long) {
        val getSize: Long = len // The only difference between the previous is the structure of this member
        override def toString = "sizey"
      }
      val obj = new Sizey(2)

      it("should do nothing if object size matches specified size") {
        obj should have size (2)
        obj should have size (2L)
        check((len: Int) => returnsNormally(new Sizey(len) should have size (len)))
        check((len: Long) => returnsNormally(new Sizey(len) should have size (len)))
      }

      it("should do nothing if object size does not match and used with should not") {
        obj should not { have size (3) }
        obj should not { have size (3L) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
        check((len: Long, wrongLen: Long) => len != wrongLen ==> returnsNormally(new Sizey(len) should not { have size (wrongLen) }))
      }

      it("should do nothing when object size matches and used in a logical-and expression") {
        obj should { have size (2) and (have size (3 - 1)) }
        obj should ((have size (2)) and (have size (3 - 1)))
        obj should (have size (2) and have size (3 - 1))
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
        obj should ((have size (77)) or (have size (3 - 1)))
        obj should (have size (77) or have size (3 - 1))
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
        obj should ((not have size (5)) and (not have size (3)))
        obj should (not have size (5) and not have size (3))
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
        obj should ((not have size (2)) or (not have size (3)))
        obj should (not have size (2) or not have size (3))
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught1 = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught1.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught1 = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught1.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught1.getMessage === "sizey did not have size 5")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (5)) and (have size (2 - 1)))
        }
        assert(caught2.getMessage === "sizey did not have size 5")

        val caught3 = intercept[AssertionError] {
          obj should (have size (5) and have size (2 - 1))
        }
        assert(caught3.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {

        val caught1 = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught1.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught2 = intercept[AssertionError] {
          obj should ((have size (55)) or (have size (22)))
        }
        assert(caught2.getMessage === "sizey did not have size 55, and sizey did not have size 22")

        val caught3 = intercept[AssertionError] {
          obj should (have size (55) or have size (22))
        }
        assert(caught3.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught1 = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught1.getMessage === "sizey had size 2, and sizey had size 2")
      }
    }

    it("should give an AssertionError with an arbitrary object that has no size member in an and expression") {
      class HasNoSize {
        val sizeiness: Int = 2
      }
      val hasNoSize = new HasNoSize
      val caught1 = intercept[AssertionError] {
        hasNoSize should { have size (2) and equal (hasNoSize) }
      }
      val expectedMessage = "have size (2) used with an object that had no public field or method named size or getSize"
      assert(caught1.getMessage === expectedMessage)
      val caught2 = intercept[AssertionError] {
        hasNoSize should not { have size (2) and equal (hasNoSize) }
      }
      assert(caught2.getMessage === expectedMessage)
    }

    it("should give an IllegalArgumentException with an arbitrary object that has multiple members with a valid sizes structure") {
      class Sizey(len: Int) {
        def getSize: Int = len
        def size: Int = len
        override def toString = "sizey"
      }
      val obj = new Sizey(2)
      val sizeMatcher = have size (2)
      val caught1 = intercept[IllegalArgumentException] {
        sizeMatcher.apply(obj)
      }
      assert(caught1.getMessage === "have size (2) used with an object that has multiple fields and/or methods named size and getSize")

      class IntAndLong(intLen: Int, longLen: Long) {
        def getSize: Int = intLen
        def size: Long = longLen
        override def toString = "sizey"
      }
      val obj2 = new Sizey(2)
      val sizeMatcher2 = have size (2)
      val caught2 = intercept[IllegalArgumentException] {
        sizeMatcher2.apply(obj)
      }
      assert(caught2.getMessage === "have size (2) used with an object that has multiple fields and/or methods named size and getSize")
    }
  }
}

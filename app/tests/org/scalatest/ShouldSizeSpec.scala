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
        check((arr: Array[Int], i: Int) => i != arr.size ==> returnsNormally(arr should not { have size (i) }))
      }

      it("should do nothing when array size matches and used in a logical-and expression") {
        Array(1, 2) should { have size (2) and (have size (3 - 1)) }
      }

      it("should do nothing when array size matches and used in a logical-or expression") {
        Array(1, 2) should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when array size doesn't match and used in a logical-and expression with not") {
        Array(1, 2) should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when array size doesn't match and used in a logical-or expression with not") {
        Array(1, 2) should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if array size does not match specified size") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should have size (3)
        }
        assert(caught.getMessage === "Array(1, 2) did not have size 3")
        check((arr: Array[String]) => throwsAssertionError(arr should have size (arr.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should have size (-2)
        }
        assert(caught.getMessage === "Array(1, 2) did not have size -2")
        check((arr: Array[Int]) => throwsAssertionError(arr should have size (if (arr.size == 0) -1 else -arr.size)))
      }

      it("should throw an assertion error when array size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "Array(1, 2) did not have size 5")
      }

      it("should throw an assertion error when array size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "Array(1, 2) did not have size 55, and Array(1, 2) did not have size 22")
      }

      it("should throw an assertion error when array size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "Array(1, 2) did not have size 3, but Array(1, 2) had size 2")
      }

      it("should throw an assertion error when array size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "Array(1, 2) had size 2, and Array(1, 2) had size 2")
      }
    }

    // TODO: Don't forget to make sure this stuff works with mutable and other sets, maps, etc.
    describe("on scala.Set") {

      it("should do nothing if set size matches specified size") {
        Set(1, 2) should have size (2)
        // check((set: Set[Int]) => returnsNormally(set should have size (set.size)))
      }

      it("should do nothing if set size does not match and used with should not") {
        Set(1, 2) should not { have size (3) }
        // check((set: Set[Int], i: Int) => i != set.size ==> returnsNormally(set should not { have size (i) }))
      }

      it("should do nothing when set size matches and used in a logical-and expression") {
        Set(1, 2) should { have size (2) and (have size (3 - 1)) }
      }

      it("should do nothing when set size matches and used in a logical-or expression") {
        Set(1, 2) should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when set size doesn't match and used in a logical-and expression with not") {
        Set(1, 2) should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when set size doesn't match and used in a logical-or expression with not") {
        Set(1, 2) should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if set size does not match specified size") {
        val caught = intercept[AssertionError] {
          Set(1, 2) should have size (3)
        }
        assert(caught.getMessage === "Set(1, 2) did not have size 3")
        // check((set: Set[String]) => throwsAssertionError(set should have size (set.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          Set(1, 2) should have size (-2)
        }
        assert(caught.getMessage === "Set(1, 2) did not have size -2")
        // check((set: Set[Int]) => throwsAssertionError(set should have size (if (set.size == 0) -1 else -set.size)))
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          Set(1, 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "Set(1, 2) did not have size 5")
      }

      it("should throw an assertion error when set size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          Set(1, 2) should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "Set(1, 2) did not have size 55, and Set(1, 2) did not have size 22")
      }

      it("should throw an assertion error when set size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          Set(1, 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "Set(1, 2) did not have size 3, but Set(1, 2) had size 2")
      }

      it("should throw an assertion error when set size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          Set(1, 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "Set(1, 2) had size 2, and Set(1, 2) had size 2")
      }
    }

    describe("on scala.List") {

      it("should do nothing if list size matches specified size") {
        List(1, 2) should have size (2)
        check((lst: List[Int]) => returnsNormally(lst should have size (lst.size)))
      }

      it("should do nothing if list size does not match and used with should not") {
        List(1, 2) should not { have size (3) }
        check((lst: List[Int], i: Int) => i != lst.size ==> returnsNormally(lst should not { have size (i) }))
      }

      it("should do nothing when list size matches and used in a logical-and expression") {
        List(1, 2) should { have size (2) and (have size (3 - 1)) }
      }

      it("should do nothing when list size matches and used in a logical-or expression") {
        List(1, 2) should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when list size doesn't match and used in a logical-and expression with not") {
        List(1, 2) should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when list size doesn't match and used in a logical-or expression with not") {
        List(1, 2) should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if list size does not match specified size") {
        val caught = intercept[AssertionError] {
          List(1, 2) should have size (3)
        }
        assert(caught.getMessage === "List(1, 2) did not have size 3")
        check((lst: List[String]) => throwsAssertionError(lst should have size (lst.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          List(1, 2) should have size (-2)
        }
        assert(caught.getMessage === "List(1, 2) did not have size -2")
        check((lst: List[Int]) => throwsAssertionError(lst should have size (if (lst.size == 0) -1 else -lst.size)))
      }

      it("should throw an assertion error when list size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          List(1, 2) should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "List(1, 2) did not have size 5")
      }

      it("should throw an assertion error when list size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          List(1, 2) should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "List(1, 2) did not have size 55, and List(1, 2) did not have size 22")
      }

      it("should throw an assertion error when list size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          List(1, 2) should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "List(1, 2) did not have size 3, but List(1, 2) had size 2")
      }

      it("should throw an assertion error when list size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          List(1, 2) should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "List(1, 2) had size 2, and List(1, 2) had size 2")
      }
    }

    /* describe("on java.List") {

      val javaList: java.util.List[Int] = new java.util.ArrayList
      javaList.add(1)
      javaList.add(2)
      
      it("should do nothing if list size matches specified size") {
        javaList should have size (2)
        // check((lst: java.util.List[Int]) => returnsNormally(lst should have size (lst.size)))
      }

      it("should do nothing if list size does not match and used with should not") {
        javaList should not { have size (3) }
        // check((lst: List[Int], i: Int) => i != lst.size ==> returnsNormally(lst should not { have size (i) }))
      }

      it("should do nothing when list size matches and used in a logical-and expression") {
        javaList should { have size (2) and (have size (3 - 1)) }
      }

      it("should do nothing when list size matches and used in a logical-or expression") {
        javaList should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when list size doesn't match and used in a logical-and expression with not") {
        javaList should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when list size doesn't match and used in a logical-or expression with not") {
        javaList should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if list size does not match specified size") {
        val caught = intercept[AssertionError] {
          javaList should have size (3)
        }
        assert(caught.getMessage === "[1, 2] did not have size 3")
        // check((lst: List[String]) => throwsAssertionError(lst should have size (lst.size + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          javaList should have size (-2)
        }
        assert(caught.getMessage === "[1, 2] did not have size -2")
        // check((lst: List[Int]) => throwsAssertionError(lst should have size (if (lst.size == 0) -1 else -lst.size)))
      }

      it("should throw an assertion error when list size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          javaList should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "[1, 2] did not have size 5")
      }

      it("should throw an assertion error when list size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          javaList should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "[1, 2] did not have size 55, and [1, 2] did not have size 22")
      }

      it("should throw an assertion error when list size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          javaList should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "[1, 2] did not have size 3, but [1, 2] had size 2")
      }

      it("should throw an assertion error when list size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          javaList should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "[1, 2] had size 2, and [1, 2] had size 2")
      }
    } */

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
      }
  
      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
      }
  
      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
      }
  
      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
      }
  
      it("should throw AssertionError if object size does not match specified size") {
        val caught = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }
  
      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }
  
      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "sizey did not have size 5")
      }
  
      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }
  
      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "sizey did not have size 3, but sizey had size 2")
      }
  
      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "sizey had size 2, and sizey had size 2")
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
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "sizey had size 2, and sizey had size 2")
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
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "sizey had size 2, and sizey had size 2")
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
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "sizey had size 2, and sizey had size 2")
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
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "sizey had size 2, and sizey had size 2")
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
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "sizey had size 2, and sizey had size 2")
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
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (2L)) }
        obj should { have size (77L) or (have size (2)) }
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "sizey had size 2, and sizey had size 2")
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
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "sizey had size 2, and sizey had size 2")
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
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "sizey had size 2, and sizey had size 2")
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
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "sizey had size 2, and sizey had size 2")
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
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "sizey had size 2, and sizey had size 2")
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
      }

      it("should do nothing when object size matches and used in a logical-or expression") {
        obj should { have size (77) or (have size (3 - 1)) }
      }

      it("should do nothing when object size doesn't match and used in a logical-and expression with not") {
        obj should { not { have size (5) } and not { have size (3) }}
      }

      it("should do nothing when object size doesn't match and used in a logical-or expression with not") {
        obj should { not { have size (2) } or not { have size (3) }}
      }

      it("should throw AssertionError if object size does not match specified size") {
        val caught = intercept[AssertionError] {
          obj should have size (3)
        }
        assert(caught.getMessage === "sizey did not have size 3")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (len + 1)))
      }

      it("should throw AssertionError with normal error message if specified size is negative") {
        val caught = intercept[AssertionError] {
          obj should have size (-2)
        }
        assert(caught.getMessage === "sizey did not have size -2")
        check((len: Int) => throwsAssertionError(new Sizey(len) should have size (if (len == 0) -1 else -len)))
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (5) and (have size (2 - 1)) }
        }
        assert(caught.getMessage === "sizey did not have size 5")
      }

      it("should throw an assertion error when object size doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have size (55) or (have size (22)) }
        }
        assert(caught.getMessage === "sizey did not have size 55, and sizey did not have size 22")
      }

      it("should throw an assertion error when object size matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (3) } and not { have size (2) }}
        }
        assert(caught.getMessage === "sizey did not have size 3, but sizey had size 2")
      }

      it("should throw an assertion error when object size matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have size (2) } or not { have size (2) }}
        }
        assert(caught.getMessage === "sizey had size 2, and sizey had size 2")
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
      val caught = intercept[IllegalArgumentException] {
        sizeMatcher.apply(obj)
      }
      assert(caught.getMessage === "have size (2) used with an object that has multiple fields and/or methods named size and getSize")

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

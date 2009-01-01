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

class ShouldLengthSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion {

  // Checking for a specific length
  describe("The 'have length (Int)' syntax") {

    describe("on String") {

      it("should do nothing if string length matches specified length") {
        "hi" should have length (2)
        check((s: String) => returnsNormally(s should have length (s.length)))
      }

      it("should do nothing if string length does not match and used with should not") {
        "hi" should not { have length (3) }
        check((s: String, i: Int) => i != s.length ==> returnsNormally(s should not { have length (i) }))
      }

      it("should do nothing when string length matches and used in a logical-and expression") {
        "hi" should { have length (2) and (have length (3 - 1)) }
      }

      it("should do nothing when string length matches and used in a logical-or expression") {
        "hi" should { have length (77) or (have length (3 - 1)) }
      }

      it("should do nothing when string length doesn't match and used in a logical-and expression with not") {
        "hi" should { not { have length (5) } and not { have length (3) }}
      }

      it("should do nothing when string length doesn't match and used in a logical-or expression with not") {
        "hi" should { not { have length (2) } or not { have length (3) }}
      }

      it("should throw AssertionError if string length does not match specified length") {
        val caught = intercept[AssertionError] {
          "hi" should have length (3)
        }
        assert(caught.getMessage === "\"hi\" did not have length 3")
        check((s: String) => throwsAssertionError(s should have length (s.length + 1)))
      }

      it("should throw AssertionError with normal error message if specified length is negative") {
        val caught = intercept[AssertionError] {
          "hi" should have length (-2)
        }
        assert(caught.getMessage === "\"hi\" did not have length -2")
        check((s: String) => throwsAssertionError(s should have length (if (s.length == 0) -1 else -s.length)))
      }

      it("should throw an assertion error when string length doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          "hi" should { have length (5) and (have length (2 - 1)) }
        }
        assert(caught.getMessage === "\"hi\" did not have length 5")
      }

      it("should throw an assertion error when string length doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          "hi" should { have length (55) or (have length (22)) }
        }
        assert(caught.getMessage === "\"hi\" did not have length 55, and \"hi\" did not have length 22")
      }

      it("should throw an assertion error when string length matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          "hi" should { not { have length (3) } and not { have length (2) }}
        }
        assert(caught.getMessage === "\"hi\" did not have length 3, but \"hi\" had length 2")
      }

      it("should throw an assertion error when string length matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          "hi" should { not { have length (2) } or not { have length (2) }}
        }
        assert(caught.getMessage === "\"hi\" had length 2, and \"hi\" had length 2")
      }
    }

    describe("on Array") {

      it("should do nothing if array length matches specified length") {
        Array(1, 2) should have length (2)
        check((arr: Array[Int]) => returnsNormally(arr should have length (arr.length)))
      }

      it("should do nothing if array length does not match and used with should not") {
        Array(1, 2) should not { have length (3) }
        check((arr: Array[Int], i: Int) => i != arr.length ==> returnsNormally(arr should not { have length (i) }))
      }

      it("should do nothing when array length matches and used in a logical-and expression") {
        Array(1, 2) should { have length (2) and (have length (3 - 1)) }
      }

      it("should do nothing when array length matches and used in a logical-or expression") {
        Array(1, 2) should { have length (77) or (have length (3 - 1)) }
      }

      it("should do nothing when array length doesn't match and used in a logical-and expression with not") {
        Array(1, 2) should { not { have length (5) } and not { have length (3) }}
      }

      it("should do nothing when array length doesn't match and used in a logical-or expression with not") {
        Array(1, 2) should { not { have length (2) } or not { have length (3) }}
      }

      it("should throw AssertionError if array length does not match specified length") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should have length (3)
        }
        assert(caught.getMessage === "Array(1, 2) did not have length 3")
        check((arr: Array[String]) => throwsAssertionError(arr should have length (arr.length + 1)))
      }

      it("should throw AssertionError with normal error message if specified length is negative") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should have length (-2)
        }
        assert(caught.getMessage === "Array(1, 2) did not have length -2")
        check((arr: Array[Int]) => throwsAssertionError(arr should have length (if (arr.length == 0) -1 else -arr.length)))
      }

      it("should throw an assertion error when array length doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should { have length (5) and (have length (2 - 1)) }
        }
        assert(caught.getMessage === "Array(1, 2) did not have length 5")
      }

      it("should throw an assertion error when array length doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should { have length (55) or (have length (22)) }
        }
        assert(caught.getMessage === "Array(1, 2) did not have length 55, and Array(1, 2) did not have length 22")
      }

      it("should throw an assertion error when array length matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should { not { have length (3) } and not { have length (2) }}
        }
        assert(caught.getMessage === "Array(1, 2) did not have length 3, but Array(1, 2) had length 2")
      }

      it("should throw an assertion error when array length matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          Array(1, 2) should { not { have length (2) } or not { have length (2) }}
        }
        assert(caught.getMessage === "Array(1, 2) had length 2, and Array(1, 2) had length 2")
      }
    }

    describe("on scala.List") {

      it("should do nothing if list length matches specified length") {
        List(1, 2) should have length (2)
        check((lst: List[Int]) => returnsNormally(lst should have length (lst.length)))
      }

      it("should do nothing if list length does not match and used with should not") {
        List(1, 2) should not { have length (3) }
        check((lst: List[Int], i: Int) => i != lst.length ==> returnsNormally(lst should not { have length (i) }))
      }

      it("should do nothing when list length matches and used in a logical-and expression") {
        List(1, 2) should { have length (2) and (have length (3 - 1)) }
      }

      it("should do nothing when list length matches and used in a logical-or expression") {
        List(1, 2) should { have length (77) or (have length (3 - 1)) }
      }

      it("should do nothing when list length doesn't match and used in a logical-and expression with not") {
        List(1, 2) should { not { have length (5) } and not { have length (3) }}
      }

      it("should do nothing when list length doesn't match and used in a logical-or expression with not") {
        List(1, 2) should { not { have length (2) } or not { have length (3) }}
      }

      it("should throw AssertionError if list length does not match specified length") {
        val caught = intercept[AssertionError] {
          List(1, 2) should have length (3)
        }
        assert(caught.getMessage === "List(1, 2) did not have length 3")
        check((lst: List[String]) => throwsAssertionError(lst should have length (lst.length + 1)))
      }

      it("should throw AssertionError with normal error message if specified length is negative") {
        val caught = intercept[AssertionError] {
          List(1, 2) should have length (-2)
        }
        assert(caught.getMessage === "List(1, 2) did not have length -2")
        check((lst: List[Int]) => throwsAssertionError(lst should have length (if (lst.length == 0) -1 else -lst.length)))
      }

      it("should throw an assertion error when list length doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          List(1, 2) should { have length (5) and (have length (2 - 1)) }
        }
        assert(caught.getMessage === "List(1, 2) did not have length 5")
      }

      it("should throw an assertion error when list length doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          List(1, 2) should { have length (55) or (have length (22)) }
        }
        assert(caught.getMessage === "List(1, 2) did not have length 55, and List(1, 2) did not have length 22")
      }

      it("should throw an assertion error when list length matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          List(1, 2) should { not { have length (3) } and not { have length (2) }}
        }
        assert(caught.getMessage === "List(1, 2) did not have length 3, but List(1, 2) had length 2")
      }

      it("should throw an assertion error when list length matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          List(1, 2) should { not { have length (2) } or not { have length (2) }}
        }
        assert(caught.getMessage === "List(1, 2) had length 2, and List(1, 2) had length 2")
      }
    }

    describe("on java.List") {

      val javaList: java.util.List[Int] = new java.util.ArrayList
      javaList.add(1)
      javaList.add(2)
      
      it("should do nothing if list length matches specified length") {
        javaList should have length (2)
        // check((lst: java.util.List[Int]) => returnsNormally(lst should have length (lst.length)))
      }

      it("should do nothing if list length does not match and used with should not") {
        javaList should not { have length (3) }
        // check((lst: List[Int], i: Int) => i != lst.length ==> returnsNormally(lst should not { have length (i) }))
      }

      it("should do nothing when list length matches and used in a logical-and expression") {
        javaList should { have length (2) and (have length (3 - 1)) }
      }

      it("should do nothing when list length matches and used in a logical-or expression") {
        javaList should { have length (77) or (have length (3 - 1)) }
      }

      it("should do nothing when list length doesn't match and used in a logical-and expression with not") {
        javaList should { not { have length (5) } and not { have length (3) }}
      }

      it("should do nothing when list length doesn't match and used in a logical-or expression with not") {
        javaList should { not { have length (2) } or not { have length (3) }}
      }

      it("should throw AssertionError if list length does not match specified length") {
        val caught = intercept[AssertionError] {
          javaList should have length (3)
        }
        assert(caught.getMessage === "[1, 2] did not have length 3")
        // check((lst: List[String]) => throwsAssertionError(lst should have length (lst.length + 1)))
      }

      it("should throw AssertionError with normal error message if specified length is negative") {
        val caught = intercept[AssertionError] {
          javaList should have length (-2)
        }
        assert(caught.getMessage === "[1, 2] did not have length -2")
        // check((lst: List[Int]) => throwsAssertionError(lst should have length (if (lst.length == 0) -1 else -lst.length)))
      }

      it("should throw an assertion error when list length doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          javaList should { have length (5) and (have length (2 - 1)) }
        }
        assert(caught.getMessage === "[1, 2] did not have length 5")
      }

      it("should throw an assertion error when list length doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          javaList should { have length (55) or (have length (22)) }
        }
        assert(caught.getMessage === "[1, 2] did not have length 55, and [1, 2] did not have length 22")
      }

      it("should throw an assertion error when list length matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          javaList should { not { have length (3) } and not { have length (2) }}
        }
        assert(caught.getMessage === "[1, 2] did not have length 3, but [1, 2] had length 2")
      }

      it("should throw an assertion error when list length matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          javaList should { not { have length (2) } or not { have length (2) }}
        }
        assert(caught.getMessage === "[1, 2] had length 2, and [1, 2] had length 2")
      }
    }

    describe("on an arbitrary object that has an empty-paren length method") {
  
      class HasLengthMethod(len: Int) {
        def length(): Int = len
        override def toString = "HasLengthMethod instance"
      }
      val obj = new HasLengthMethod(2)
  
      it("should do nothing if object length matches specified length") {
        obj should have length (2)
        check((len: Int) => returnsNormally(new HasLengthMethod(len) should have length (len)))
      }
  
      it("should do nothing if object length does not match and used with should not") {
        obj should not { have length (3) }
        check((len: Int, wrongLen: Int) => len != wrongLen ==> returnsNormally(new HasLengthMethod(len) should not { have length (wrongLen) }))
      }
  
      it("should do nothing when object length matches and used in a logical-and expression") {
        obj should { have length (2) and (have length (3 - 1)) }
      }
  
      it("should do nothing when object length matches and used in a logical-or expression") {
        obj should { have length (77) or (have length (3 - 1)) }
      }
  
      it("should do nothing when object length doesn't match and used in a logical-and expression with not") {
        obj should { not { have length (5) } and not { have length (3) }}
      }
  
      it("should do nothing when object length doesn't match and used in a logical-or expression with not") {
        obj should { not { have length (2) } or not { have length (3) }}
      }
  
      it("should throw AssertionError if object length does not match specified length") {
        val caught = intercept[AssertionError] {
          obj should have length (3)
        }
        assert(caught.getMessage === "HasLengthMethod instance did not have length 3")
        check((len: Int) => throwsAssertionError(new HasLengthMethod(len) should have length (len + 1)))
      }
  
      it("should throw AssertionError with normal error message if specified length is negative") {
        val caught = intercept[AssertionError] {
          obj should have length (-2)
        }
        assert(caught.getMessage === "HasLengthMethod instance did not have length -2")
        check((len: Int) => throwsAssertionError(new HasLengthMethod(len) should have length (if (len == 0) -1 else -len)))
      }
  
      it("should throw an assertion error when object length doesn't match and used in a logical-and expression") {
        val caught = intercept[AssertionError] {
          obj should { have length (5) and (have length (2 - 1)) }
        }
        assert(caught.getMessage === "HasLengthMethod instance did not have length 5")
      }
  
      it("should throw an assertion error when object length doesn't match and used in a logical-or expression") {
        val caught = intercept[AssertionError] {
          obj should { have length (55) or (have length (22)) }
        }
        assert(caught.getMessage === "HasLengthMethod instance did not have length 55, and HasLengthMethod instance did not have length 22")
      }
  
      it("should throw an assertion error when object length matches and used in a logical-and expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have length (3) } and not { have length (2) }}
        }
        assert(caught.getMessage === "HasLengthMethod instance did not have length 3, but HasLengthMethod instance had length 2")
      }
  
      it("should throw an assertion error when object length matches and used in a logical-or expression with not") {
        val caught = intercept[AssertionError] {
          obj should { not { have length (2) } or not { have length (2) }}
        }
        assert(caught.getMessage === "HasLengthMethod instance had length 2, and HasLengthMethod instance had length 2")
      }
    }

      // I think I need one batch each for
      //   def length: Int
      //   val length: Int
      //   def length(): Int
      //   def getLength(): Int
      // and maybe also for:
      //   def getLength: Int
      // yes, and need implicit conversions for the two getLength variants too
    it("should work with any arbitrary object that has a length method in an and expression") {
      class HasLengthMethod {
        def length(): Int = 2
      }
      val hasLengthMethod = new HasLengthMethod
      hasLengthMethod should { have length (2) and equal (hasLengthMethod) }
      val caught = intercept[AssertionError] {
        // hasLengthMethod shouldNot { have length (2) and equal (hasLengthMethod) }
        hasLengthMethod should not { have length (2) and equal (hasLengthMethod) }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    it("should work with any arbitrary object that has a parameterless length method in an and expression") {
      class HasLengthMethod {
        def length: Int = 2
      }
      val hasLengthMethod = new HasLengthMethod
      hasLengthMethod should { have length (2) and equal (hasLengthMethod) }
      val caught = intercept[AssertionError] {
        // hasLengthMethod shouldNot { have length (2) and equal (hasLengthMethod) }
        hasLengthMethod should not { have length (2) and equal (hasLengthMethod) }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    it("should work with any arbitrary object that has a length field in an and expression") {
      class HasLengthField {
        val length: Int = 2
      }
      val hasLengthField = new HasLengthField
      hasLengthField should { have length (2) and equal (hasLengthField) }
      val caught = intercept[AssertionError] {
        // hasLengthField shouldNot { have length (2) and equal (hasLengthField) }
        hasLengthField should not { have length (2) and equal (hasLengthField) }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    it("should give an AssertionError with an arbitrary object that has no length member in an and expression") {
      class HasNoLength {
        val lengthiness: Int = 2
      }
      val hasNoLength = new HasNoLength
      val caught1 = intercept[AssertionError] {
        hasNoLength should { have length (2) and equal (hasNoLength) }
      }
      val expectedSubstring = "used with an object that had neither a public field or method named 'length'"
      assert(caught1.getMessage.indexOf(expectedSubstring) != -1)
      val caught2 = intercept[AssertionError] {
        // hasNoLength shouldNot { have length (2) and equal (hasNoLength) }
        hasNoLength should not { have length (2) and equal (hasNoLength) }
      }
      assert(caught2.getMessage.indexOf(expectedSubstring) != -1)
    }
  }
}

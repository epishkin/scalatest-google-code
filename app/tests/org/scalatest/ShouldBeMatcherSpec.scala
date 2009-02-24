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
import scala.reflect.BeanProperty

class ShouldBeMatcherSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion with BookPropertyMatchers {

  class OddMatcher extends BeMatcher[Int] {
    def apply(left: Int): MatchResult = {
      MatchResult(
        left % 2 == 1,
        left.toString + " was even",
        left.toString + " was odd"
      )
    }
  }
  val odd = new OddMatcher
  val even = not (odd)

  // Checking for a specific size
  describe("The BeMatcher syntax") {

    describe("on an object with properties") {

      it("should do nothing if a BeMatcher matches") {
        1 should be (odd)
        2 should be (even)
      }

      it("should throw TestFailedException if a BeMatcher does not match") {

        val caught1 = intercept[TestFailedException] {
          4 should be (odd)
        }
        assert(caught1.getMessage === "4 was even")

        val caught2 = intercept[TestFailedException] {
          5 should be (even)
        }
        assert(caught2.getMessage === "5 was odd")
      }

      it("should do nothing if a BeMatcher does not match, when used with not") {
        2 should not be (odd)
        1 should not be (even)
        22 should not (not (be (even)))
        1 should not (not (be (odd)))
      }

      it("should throw TestFailedException if a BeMatcher matches, when used with not") {

        val caught1 = intercept[TestFailedException] {
          3 should not be (odd)
        }
        assert(caught1.getMessage === "3 was odd")

        val caught2 = intercept[TestFailedException] {
          6 should not be (even)
        }
        assert(caught2.getMessage === "6 was even")

        val caught3 = intercept[TestFailedException] {
          6 should not (not (be (odd)))
        }
        assert(caught3.getMessage === "6 was even")
      }

      it("should do nothing if a BeMatcher matches, when used in a logical-and expression") {
        1 should (be (odd) and be (odd))
        1 should (be (odd) and (be (odd)))
        2 should (be (even) and be (even))
        2 should (be (even) and (be (even)))
      }

      it("should throw TestFailedException if at least one BeMatcher does not match, when used in a logical-or expression") {

        // both false
        val caught1 = intercept[TestFailedException] {
          2 should (be (odd) and be (odd))
        }
        assert(caught1.getMessage === "2 was even")

        val caught2 = intercept[TestFailedException] {
          2 should (be (odd) and (be (odd)))
        }
        assert(caught2.getMessage === "2 was even")

        val caught3 = intercept[TestFailedException] {
          1 should (be (even) and be (even))
        }
        assert(caught3.getMessage === "1 was odd")

        val caught4 = intercept[TestFailedException] {
          1 should (be (even) and (be (even)))
        }
        assert(caught4.getMessage === "1 was odd")


        // first false
        val caught5 = intercept[TestFailedException] {
          1 should (be (even) and be (odd))
        }
        assert(caught5.getMessage === "1 was odd")

        val caught6 = intercept[TestFailedException] {
          1 should (be (even) and (be (odd)))
        }
        assert(caught6.getMessage === "1 was odd")

        val caught7 = intercept[TestFailedException] {
          2 should (be (odd) and be (even))
        }
        assert(caught7.getMessage === "2 was even")

        val caught8 = intercept[TestFailedException] {
          2 should (be (odd) and (be (even)))
        }
        assert(caught8.getMessage === "2 was even")


// TODO: Remember to try a BeMatcher[Any] one, to make sure it works on an Int

        // second false
        val caught9 = intercept[TestFailedException] {
          1 should (be (odd) and be (even))
        }
        assert(caught9.getMessage === "1 was odd, but 1 was odd")

        val caught10 = intercept[TestFailedException] {
          1 should (be (odd) and (be (even)))
        }
        assert(caught10.getMessage === "1 was odd, but 1 was odd")

        val caught11 = intercept[TestFailedException] {
          2 should (be (even) and be (odd))
        }
        assert(caught11.getMessage === "2 was even, but 2 was even")

        val caught12 = intercept[TestFailedException] {
          2 should (be (even) and (be (odd)))
        }
        assert(caught12.getMessage === "2 was even, but 2 was even")
      }

      it("should do nothing if at least one BeMatcher matches, when used in a logical-or expression") {

        // both true
        1 should (be (odd) or be (odd))
        1 should (be (odd) or (be (odd)))
        2 should (be (even) or be (even))
        2 should (be (even) or (be (even)))

        // first false
        1 should (be (even) or be (odd))
        1 should (be (even) or (be (odd)))
        2 should (be (odd) or be (even))
        2 should (be (odd) or (be (even)))

        // second false
        1 should (be (odd) or be (even))
        1 should (be (odd) or (be (even)))
        2 should (be (even) or be (odd))
        2 should (be (even) or (be (odd)))
      }

      it("should throw TestFailedException if a BeMatcher does not match, when used in a logical-or expression") {

        val caught1 = intercept[TestFailedException] {
          2 should (be (odd) or be (odd))
        }
        assert(caught1.getMessage === "2 was even, and 2 was even")

        val caught2 = intercept[TestFailedException] {
          2 should (be (odd) or (be (odd)))
        }
        assert(caught2.getMessage === "2 was even, and 2 was even")

        val caught3 = intercept[TestFailedException] {
          1 should (be (even) or be (even))
        }
        assert(caught3.getMessage === "1 was odd, and 1 was odd")

        val caught4 = intercept[TestFailedException] {
          1 should (be (even) or (be (even)))
        }
        assert(caught4.getMessage === "1 was odd, and 1 was odd")
      }

    }
  }
}

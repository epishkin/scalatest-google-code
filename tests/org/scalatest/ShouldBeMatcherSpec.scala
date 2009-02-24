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
    }
  }
}

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
package org.scalatest.matchers

import prop.Checkers
import org.scalacheck._
import Arbitrary._
import Prop._

class ShouldBeNullSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion {

  val nullMap: Map[Int, String] = null

  describe("the be null syntax") {

    it("should throw a NullPointerException if they try to short circuit with a null check first") {
      // The reason I check this is I warn that this will happen in the ShouldMatcher scaladoc
      intercept[NullPointerException] {
        nullMap should (not be (null) and contain key (7))
      }
    }

    it("should compile and run when used in any position") {

      val caught1 = intercept[TestFailedException] {
        Map(1 -> "one") should (contain key (7) and not be (null))
      }
      assert(caught1.getMessage === "Map(1 -> one) did not contain key 7")

      Map(1 -> "one") should (contain key (1) and not be (null))

      val caught2 = intercept[TestFailedException] {
        Map(1 -> "one") should (contain key (1) and be (null))
      }
      assert(caught2.getMessage === "Map(1 -> one) contained key 1, but Map(1 -> one) was not null")

      val caught3 = intercept[TestFailedException] {
        Map(1 -> "one") should (be (null) and not be (null))
      }
      assert(caught3.getMessage === "Map(1 -> one) was not null")

      val caught4 = intercept[TestFailedException] {
        nullMap should (be (null) and not be (null))
      }
      assert(caught4.getMessage === "The reference was null, but the reference was null")
    }
  }
}

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

class ShouldEqualSpec extends Spec with ShouldMatchers {

  // Checking for equality with "equal"
  describe("The equal token") {

    it("should do nothing when equal") {
      1 should equal (2)
    }

    it("should do nothing when not equal and used with should not") {
      1 should not { equal (2) }
    }

    it("should do nothing when equal and used in a logical-and expression") {
      1 should { equal (1) and equal (2 - 1) }
    }

    it("should do nothing when equal and used in a logical-or expression") {
      1 should { equal (1) or equal (2 - 1) }
    }

    it("should do nothing when not equal and used in a logical-and expression with not") {
      1 should { not { equal (2) } and not { equal (3 - 1) }}
    }

    it("should do nothing when not equal and used in a logical-or expression with not") {
      1 should { not { equal (2) } or not { equal (3 - 1) }}
    }

    it("should throw an assertion error when not equal") {
      val caught = intercept[AssertionError] {
        1 should equal (2)
      }
      assert(caught.getMessage === "1 did not equal 2")
    }

    it("should throw an assertion error when equal but used with should not") {
      val caught = intercept[AssertionError] {
        1 should not { equal (1) }
      }
      assert(caught.getMessage === "1 equaled 1")
    }

    it("should throw an assertion error when not equal and used in a logical-and expression") {
      val caught = intercept[AssertionError] {
        1 should { equal (5) and equal (2 - 1) }
      }
      assert(caught.getMessage === "1 did not equal 5")
    }

    it("should throw an assertion error when not equal and used in a logical-or expression") {
      val caught = intercept[AssertionError] {
        1 should { equal (5) or equal (5 - 1) }
      }
      assert(caught.getMessage === "1 did not equal 5, and 1 did not equal 4")
    }

    it("should throw an assertion error when equal and used in a logical-and expression with not") {
      val caught = intercept[AssertionError] {
        1 should { not { equal (1) } and not { equal (3 - 1) }}
      }
      assert(caught.getMessage === "1 equaled 1")
    }

    it("should throw an assertion error when equal and used in a logical-or expression with not") {
      val caught = intercept[AssertionError] {
        1 should { not { equal (1) } or not { equal (2 - 1) }}
      }
      assert(caught.getMessage === "1 equaled 1, and 1 equaled 1")
    }
  }
}
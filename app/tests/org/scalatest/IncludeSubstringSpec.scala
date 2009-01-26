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

class IncludeSubstringSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion {

/*
s should include substring t
s should include substring t
s should startWith substring t
s should startWith substring t
s should endWith substring t
s should endWith substring t
s should fullyMatch substring t
*/

describe("The include substring syntax") {

    it("should do nothing if the string includes substring specified as a string") {

      "1.78" should include substring ("1.7")
      "21.7" should include substring ("1.7")
      "21.78" should include substring ("1.7")
      "1.7" should include substring ("1.7")
      check((s: String, t: String, u: String) => returnsNormally(s + t + u should include substring (t)))
    }

    it("should do nothing if the string does not include substring specified as a string when used with not") {

      "eight" should not { include substring ("1.7") }
      "eight" should not include substring ("1.7")
      check((s: String, t: String, u: String) => (s + u).indexOf(t) == -1 ==> returnsNormally(s + u should not (include substring (t))))
      check((s: String, t: String, u: String) => (s + u).indexOf(t) == -1 ==> returnsNormally(s + u should not include substring (t)))
    }

    it("should do nothing if the string does not include substring specified as a string when used in a logical-and expression") {

      "a1.7" should (include substring ("1.7") and (include substring ("1.7")))
      "a1.7" should (include substring ("1.7") and (include substring ("1.7")))
      "a1.7" should (include substring ("1.7") and (include substring ("1.7")))

      "1.7b" should ((include substring ("1.7")) and (include substring ("1.7")))
      "1.7b" should ((include substring ("1.7")) and (include substring ("1.7")))
      "1.7b" should ((include substring ("1.7")) and (include substring ("1.7")))

      "a1.7b" should (include substring ("1.7") and include substring ("1.7"))
      "a1.7b" should (include substring ("1.7") and include substring ("1.7"))
      "a1.7b" should (include substring ("1.7") and include substring ("1.7"))

      "1.7" should (include substring ("1.7") and (include substring ("1.7")))
      "1.7" should ((include substring ("1.7")) and (include substring ("1.7")))
      "1.7" should (include substring ("1.7") and include substring ("1.7"))
    }

    it("should do nothing if the string does not include substring specified as a string when used in a logical-or expression") {

      "a1.7" should (include substring ("hello") or (include substring ("1.7")))
      "a1.7" should (include substring ("hello") or (include substring ("1.7")))
      "a1.7" should (include substring ("hello") or (include substring ("1.7")))

      "1.7b" should ((include substring ("hello")) or (include substring ("1.7")))
      "1.7b" should ((include substring ("hello")) or (include substring ("1.7")))
      "1.7b" should ((include substring ("hello")) or (include substring ("1.7")))

      "a1.7b" should (include substring ("hello") or include substring ("1.7"))
      "a1.7b" should (include substring ("hello") or include substring ("1.7"))
      "a1.7b" should (include substring ("hello") or include substring ("1.7"))

      "1.7" should (include substring ("hello") or (include substring ("1.7")))
      "1.7" should ((include substring ("hello")) or (include substring ("1.7")))
      "1.7" should (include substring ("hello") or include substring ("1.7"))
    }

    it("should do nothing if the string does not include substring specified as a string when used in a logical-and expression with not") {
      "fred" should (not (include substring ("bob")) and not (include substring ("1.7")))
      "fred" should ((not include substring ("bob")) and (not include substring ("1.7")))
      "fred" should (not include substring ("bob") and not include substring ("1.7"))
    }

    it("should do nothing if the string does not include substring specified as a string when used in a logical-or expression with not") {
      "fred" should (not (include substring ("fred")) or not (include substring ("1.7")))
      "fred" should ((not include substring ("fred")) or (not include substring ("1.7")))
      "fred" should (not include substring ("fred") or not include substring ("1.7"))
    }

    it("should throw AssertionError if the string does not match substring specified as a string") {

      val caught1 = intercept[AssertionError] {
        "1.7" should include substring ("1.78")
      }
      assert(caught1.getMessage === "\"1.7\" did not include substring \"1.78\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should include substring ("21.7")
      }
      assert(caught2.getMessage === "\"1.7\" did not include substring \"21.7\"")

      val caught3 = intercept[AssertionError] {
        "-one.eight" should include substring ("1.7")
      }
      assert(caught3.getMessage === "\"-one.eight\" did not include substring \"1.7\"")

      val caught6 = intercept[AssertionError] {
        "eight" should include substring ("1.7")
      }
      assert(caught6.getMessage === "\"eight\" did not include substring \"1.7\"")

      val caught7 = intercept[AssertionError] {
        "one.eight" should include substring ("1.7")
      }
      assert(caught7.getMessage === "\"one.eight\" did not include substring \"1.7\"")

      val caught8 = intercept[AssertionError] {
        "onedoteight" should include substring ("1.7")
      }
      assert(caught8.getMessage === "\"onedoteight\" did not include substring \"1.7\"")

      val caught9 = intercept[AssertionError] {
        "***" should include substring ("1.7")
      }
      assert(caught9.getMessage === "\"***\" did not include substring \"1.7\"")
    }

    it("should throw AssertionError if the string does matches substring specified as a string when used with not") {

      val caught1 = intercept[AssertionError] {
        "1.7" should not { include substring ("1.7") }
      }
      assert(caught1.getMessage === "\"1.7\" included substring \"1.7\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should not { include substring ("1.7") }
      }
      assert(caught2.getMessage === "\"1.7\" included substring \"1.7\"")

      val caught3 = intercept[AssertionError] {
        "-1.8" should not { include substring ("1.8") }
      }
      assert(caught3.getMessage === "\"-1.8\" included substring \"1.8\"")

      val caught4 = intercept[AssertionError] {
        "8" should not { include substring ("8") }
      }
      assert(caught4.getMessage === "\"8\" included substring \"8\"")

      val caught5 = intercept[AssertionError] {
        "1." should not { include substring (".") }
      }
      assert(caught5.getMessage === "\"1.\" included substring \".\"")

      val caught11 = intercept[AssertionError] {
        "1.7" should not include substring ("1.7")
      }
      assert(caught11.getMessage === "\"1.7\" included substring \"1.7\"")

      val caught13 = intercept[AssertionError] {
        "-1.8" should not include substring ("-")
      }
      assert(caught13.getMessage === "\"-1.8\" included substring \"-\"")

      val caught14 = intercept[AssertionError] {
        "8" should not include substring ("")
      }
      assert(caught14.getMessage === "\"8\" included substring \"\"")

      val caught15 = intercept[AssertionError] {
        "1." should not include substring ("1.")
      }
      assert(caught15.getMessage === "\"1.\" included substring \"1.\"")

      val caught21 = intercept[AssertionError] {
        "a1.7" should not { include substring ("1.7") }
      }
      assert(caught21.getMessage === "\"a1.7\" included substring \"1.7\"")

      val caught22 = intercept[AssertionError] {
        "1.7b" should not { include substring ("1.7") }
      }
      assert(caught22.getMessage === "\"1.7b\" included substring \"1.7\"")

      val caught23 = intercept[AssertionError] {
        "a-1.8b" should not { include substring ("1.8") }
      }
      assert(caught23.getMessage === "\"a-1.8b\" included substring \"1.8\"")
    }

    it("should throw AssertionError if the string includes substring specified as a string when used in a logical-and expression") {

      val caught1 = intercept[AssertionError] {
        "1.7" should (include substring ("1.7") and (include substring ("1.8")))
      }
      assert(caught1.getMessage === "\"1.7\" included substring \"1.7\", but \"1.7\" did not include substring \"1.8\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should ((include substring ("1.7")) and (include substring ("1.8")))
      }
      assert(caught2.getMessage === "\"1.7\" included substring \"1.7\", but \"1.7\" did not include substring \"1.8\"")

      val caught3 = intercept[AssertionError] {
        "1.7" should (include substring ("1.7") and include substring ("1.8"))
      }
      assert(caught3.getMessage === "\"1.7\" included substring \"1.7\", but \"1.7\" did not include substring \"1.8\"")

      // Check to make sure the error message "short circuits" (i.e., just reports the left side's failure)
      val caught4 = intercept[AssertionError] {
        "one.eight" should (include substring ("1.7") and (include substring ("1.8")))
      }
      assert(caught4.getMessage === "\"one.eight\" did not include substring \"1.7\"")

      val caught5 = intercept[AssertionError] {
        "one.eight" should ((include substring ("1.7")) and (include substring ("1.8")))
      }
      assert(caught5.getMessage === "\"one.eight\" did not include substring \"1.7\"")

      val caught6 = intercept[AssertionError] {
        "one.eight" should (include substring ("1.7") and include substring ("1.8"))
      }
      assert(caught6.getMessage === "\"one.eight\" did not include substring \"1.7\"")
    }

    it("should throw AssertionError if the string includes substring specified as a string when used in a logical-or expression") {

      val caught1 = intercept[AssertionError] {
        "one.seven" should (include substring ("1.7") or (include substring ("1.8")))
      }
      assert(caught1.getMessage === "\"one.seven\" did not include substring \"1.7\", and \"one.seven\" did not include substring \"1.8\"")

      val caught2 = intercept[AssertionError] {
        "one.seven" should ((include substring ("1.7")) or (include substring ("1.8")))
      }
      assert(caught2.getMessage === "\"one.seven\" did not include substring \"1.7\", and \"one.seven\" did not include substring \"1.8\"")

      val caught3 = intercept[AssertionError] {
        "one.seven" should (include substring ("1.7") or include substring ("1.8"))
      }
      assert(caught3.getMessage === "\"one.seven\" did not include substring \"1.7\", and \"one.seven\" did not include substring \"1.8\"")
    }

    it("should throw AssertionError if the string includes substring specified as a string when used in a logical-and expression used with not") {

      val caught1 = intercept[AssertionError] {
        "1.7" should (not include substring ("1.8") and (not include substring ("1.7")))
      }
      assert(caught1.getMessage === "\"1.7\" did not include substring \"1.8\", but \"1.7\" included substring \"1.7\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should ((not include substring ("1.8")) and (not include substring ("1.7")))
      }
      assert(caught2.getMessage === "\"1.7\" did not include substring \"1.8\", but \"1.7\" included substring \"1.7\"")

      val caught3 = intercept[AssertionError] {
        "1.7" should (not include substring ("1.8") and not include substring ("1.7"))
      }
      assert(caught3.getMessage === "\"1.7\" did not include substring \"1.8\", but \"1.7\" included substring \"1.7\"")

      val caught4 = intercept[AssertionError] {
        "a1.7" should (not include substring ("1.8") and (not include substring ("1.7")))
      }
      assert(caught4.getMessage === "\"a1.7\" did not include substring \"1.8\", but \"a1.7\" included substring \"1.7\"")

      val caught5 = intercept[AssertionError] {
        "1.7b" should ((not include substring ("1.8")) and (not include substring ("1.7")))
      }
      assert(caught5.getMessage === "\"1.7b\" did not include substring \"1.8\", but \"1.7b\" included substring \"1.7\"")

      val caught6 = intercept[AssertionError] {
        "a1.7b" should (not include substring ("1.8") and not include substring ("1.7"))
      }
      assert(caught6.getMessage === "\"a1.7b\" did not include substring \"1.8\", but \"a1.7b\" included substring \"1.7\"")
    }

    it("should throw AssertionError if the string includes substring specified as a string when used in a logical-or expression used with not") {

      val caught1 = intercept[AssertionError] {
        "1.7" should (not include substring ("1.7") or (not include substring ("1.7")))
      }
      assert(caught1.getMessage === "\"1.7\" included substring \"1.7\", and \"1.7\" included substring \"1.7\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should ((not include substring ("1.7")) or (not include substring ("1.7")))
      }
      assert(caught2.getMessage === "\"1.7\" included substring \"1.7\", and \"1.7\" included substring \"1.7\"")

      val caught3 = intercept[AssertionError] {
        "1.7" should (not include substring ("1.7") or not include substring ("1.7"))
      }
      assert(caught3.getMessage === "\"1.7\" included substring \"1.7\", and \"1.7\" included substring \"1.7\"")

      val caught4 = intercept[AssertionError] {
        "1.7" should (not (include substring ("1.7")) or not (include substring ("1.7")))
      }
      assert(caught4.getMessage === "\"1.7\" included substring \"1.7\", and \"1.7\" included substring \"1.7\"")

      val caught5 = intercept[AssertionError] {
        "a1.7" should (not include substring ("1.7") or (not include substring ("1.7")))
      }
      assert(caught5.getMessage === "\"a1.7\" included substring \"1.7\", and \"a1.7\" included substring \"1.7\"")

      val caught6 = intercept[AssertionError] {
        "1.7b" should ((not include substring ("1.7")) or (not include substring ("1.7")))
      }
      assert(caught6.getMessage === "\"1.7b\" included substring \"1.7\", and \"1.7b\" included substring \"1.7\"")

      val caught7 = intercept[AssertionError] {
        "a1.7b" should (not include substring ("1.7") or not include substring ("1.7"))
      }
      assert(caught7.getMessage === "\"a1.7b\" included substring \"1.7\", and \"a1.7b\" included substring \"1.7\"")

      val caught8 = intercept[AssertionError] {
        "a1.7b" should (not (include substring ("1.7")) or not (include substring ("1.7")))
      }
      assert(caught8.getMessage === "\"a1.7b\" included substring \"1.7\", and \"a1.7b\" included substring \"1.7\"")
    }
  }
}


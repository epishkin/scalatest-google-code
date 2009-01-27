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

class StartWithSubstringSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion {

/*
s should startWith substring t
s should startWith substring t
s should startWith substring t
s should startWith substring t
s should endWith substring t
s should endWith substring t
s should fullyMatch substring t
*/

describe("The startWith substring syntax") {

    it("should do nothing if the string starts with the specified substring") {

      "1.78" should startWith substring ("1.7")
      "21.7" should startWith substring ("2")
      "21.78" should startWith substring ("21.78")
      check((s: String, t: String) => returnsNormally(s + t should startWith substring (s)))
    }

    it("should do nothing if the string does not start with the specified substring when used with not") {

      "eight" should not { startWith substring ("1.7") }
      "eight" should not startWith substring ("1.7")
      check((s: String, t: String) => (s + t).indexOf(t) != 0 ==> returnsNormally(s + t should not (startWith substring (t))))
      check((s: String, t: String) => (s + t).indexOf(t) != 0 ==> returnsNormally(s + t should not startWith substring (t)))
    }

    it("should do nothing if the string does not start with the specified substring when used in a logical-and expression") {

      "1.7b" should ((startWith substring ("1.7")) and (startWith substring ("1.7")))
      "1.7b" should ((startWith substring ("1.7")) and (startWith substring ("1.7")))
      "1.7b" should ((startWith substring ("1.7")) and (startWith substring ("1.7")))

      "1.7" should (startWith substring ("1.7") and (startWith substring ("1.7")))
      "1.7" should ((startWith substring ("1.7")) and (startWith substring ("1.7")))
      "1.7" should (startWith substring ("1.7") and startWith substring ("1.7"))

      check((s: String, t: String) => returnsNormally(s + t should (startWith substring (s) and startWith substring (s))))
    }

    it("should do nothing if the string does not start with the specified substring when used in a logical-or expression") {

      "1.7b" should ((startWith substring ("hello")) or (startWith substring ("1.7")))
      "1.7b" should ((startWith substring ("hello")) or (startWith substring ("1.7")))
      "1.7b" should ((startWith substring ("hello")) or (startWith substring ("1.7")))

      "1.7" should (startWith substring ("hello") or (startWith substring ("1.7")))
      "1.7" should ((startWith substring ("hello")) or (startWith substring ("1.7")))
      "1.7" should (startWith substring ("hello") or startWith substring ("1.7"))

      check((s: String, t: String) => returnsNormally(s + t should (startWith substring ("hi") or startWith substring (s))))
    }

    it("should do nothing if the string does not start with the specified substring when used in a logical-and expression with not") {
      "fred" should (not (startWith substring ("red")) and not (startWith substring ("1.7")))
      "fred" should ((not startWith substring ("red")) and (not startWith substring ("1.7")))
      "fred" should (not startWith substring ("red") and not startWith substring ("1.7"))
      check((s: String) => s.indexOf("bob") != 0 && s.indexOf("1.7") != 0 ==> returnsNormally(s should (not startWith substring ("bob") and not startWith substring ("1.7"))))
    }

    it("should do nothing if the string does not start with the specified substring when used in a logical-or expression with not") {
      "fred" should (not (startWith substring ("fred")) or not (startWith substring ("1.7")))
      "fred" should ((not startWith substring ("fred")) or (not startWith substring ("1.7")))
      "fred" should (not startWith substring ("fred") or not startWith substring ("1.7"))
      check((s: String) => s.indexOf("a") != 0 || s.indexOf("b") != 0 ==> returnsNormally(s should (not startWith substring ("a") or not startWith substring ("b"))))
    }

    it("should throw AssertionError if the string does not match substring specified as a string") {

      val caught1 = intercept[AssertionError] {
        "1.7" should startWith substring ("1.78")
      }
      assert(caught1.getMessage === "\"1.7\" did not start with substring \"1.78\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should startWith substring ("21.7")
      }
      assert(caught2.getMessage === "\"1.7\" did not start with substring \"21.7\"")

      val caught3 = intercept[AssertionError] {
        "-one.eight" should startWith substring ("1.7")
      }
      assert(caught3.getMessage === "\"-one.eight\" did not start with substring \"1.7\"")

      val caught6 = intercept[AssertionError] {
        "eight" should startWith substring ("1.7")
      }
      assert(caught6.getMessage === "\"eight\" did not start with substring \"1.7\"")

      val caught7 = intercept[AssertionError] {
        "one.eight" should startWith substring ("1.7")
      }
      assert(caught7.getMessage === "\"one.eight\" did not start with substring \"1.7\"")

      val caught8 = intercept[AssertionError] {
        "onedoteight" should startWith substring ("1.7")
      }
      assert(caught8.getMessage === "\"onedoteight\" did not start with substring \"1.7\"")

      val caught9 = intercept[AssertionError] {
        "***" should startWith substring ("1.7")
      }
      assert(caught9.getMessage === "\"***\" did not start with substring \"1.7\"")

      check((s: String) => s.indexOf("1.7") == -1 ==> throwsAssertionError(s should startWith substring ("1.7")))
    }

    it("should throw AssertionError if the string does matches substring specified as a string when used with not") {

      val caught1 = intercept[AssertionError] {
        "1.7" should not { startWith substring ("1.7") }
      }
      assert(caught1.getMessage === "\"1.7\" started with substring \"1.7\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should not { startWith substring ("1.7") }
      }
      assert(caught2.getMessage === "\"1.7\" started with substring \"1.7\"")

      val caught3 = intercept[AssertionError] {
        "-1.8" should not { startWith substring ("-1") }
      }
      assert(caught3.getMessage === "\"-1.8\" started with substring \"-1\"")

      val caught4 = intercept[AssertionError] {
        "8" should not { startWith substring ("8") }
      }
      assert(caught4.getMessage === "\"8\" started with substring \"8\"")

      val caught5 = intercept[AssertionError] {
        "1." should not { startWith substring ("1") }
      }
      assert(caught5.getMessage === "\"1.\" started with substring \"1\"")

      val caught11 = intercept[AssertionError] {
        "1.7" should not startWith substring ("1.7")
      }
      assert(caught11.getMessage === "\"1.7\" started with substring \"1.7\"")

      val caught13 = intercept[AssertionError] {
        "-1.8" should not startWith substring ("-")
      }
      assert(caught13.getMessage === "\"-1.8\" started with substring \"-\"")

      val caught14 = intercept[AssertionError] {
        "8" should not startWith substring ("")
      }
      assert(caught14.getMessage === "\"8\" started with substring \"\"")

      val caught15 = intercept[AssertionError] {
        "1." should not startWith substring ("1.")
      }
      assert(caught15.getMessage === "\"1.\" started with substring \"1.\"")

      val caught21 = intercept[AssertionError] {
        "a1.7" should not { startWith substring ("a1") }
      }
      assert(caught21.getMessage === "\"a1.7\" started with substring \"a1\"")

      val caught22 = intercept[AssertionError] {
        "1.7b" should not { startWith substring ("1.7") }
      }
      assert(caught22.getMessage === "\"1.7b\" started with substring \"1.7\"")

      val caught23 = intercept[AssertionError] {
        "a-1.8b" should not { startWith substring ("a-1.8") }
      }
      assert(caught23.getMessage === "\"a-1.8b\" started with substring \"a-1.8\"")

      check((s: String) => s.length != 0 ==> throwsAssertionError(s should not startWith substring (s.substring(0, 1))))
    }

    it("should throw AssertionError if the string starts with the specified substring when used in a logical-and expression") {

      val caught1 = intercept[AssertionError] {
        "1.7" should (startWith substring ("1.7") and (startWith substring ("1.8")))
      }
      assert(caught1.getMessage === "\"1.7\" started with substring \"1.7\", but \"1.7\" did not start with substring \"1.8\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should ((startWith substring ("1")) and (startWith substring ("1.8")))
      }
      assert(caught2.getMessage === "\"1.7\" started with substring \"1\", but \"1.7\" did not start with substring \"1.8\"")

      val caught3 = intercept[AssertionError] {
        "1.7" should (startWith substring ("1.7") and startWith substring ("1.8"))
      }
      assert(caught3.getMessage === "\"1.7\" started with substring \"1.7\", but \"1.7\" did not start with substring \"1.8\"")

      // Check to make sure the error message "short circuits" (i.e., just reports the left side's failure)
      val caught4 = intercept[AssertionError] {
        "one.eight" should (startWith substring ("1.7") and (startWith substring ("1.8")))
      }
      assert(caught4.getMessage === "\"one.eight\" did not start with substring \"1.7\"")

      val caught5 = intercept[AssertionError] {
        "one.eight" should ((startWith substring ("1.7")) and (startWith substring ("1.8")))
      }
      assert(caught5.getMessage === "\"one.eight\" did not start with substring \"1.7\"")

      val caught6 = intercept[AssertionError] {
        "one.eight" should (startWith substring ("1.7") and startWith substring ("1.8"))
      }
      assert(caught6.getMessage === "\"one.eight\" did not start with substring \"1.7\"")

      check((s: String, t: String, u: String) => (s + u).indexOf(t) != 0 ==> throwsAssertionError(s + u should (startWith substring (s) and startWith substring (t))))
    }

    it("should throw AssertionError if the string starts with the specified substring when used in a logical-or expression") {

      val caught1 = intercept[AssertionError] {
        "one.seven" should (startWith substring ("1.7") or (startWith substring ("1.8")))
      }
      assert(caught1.getMessage === "\"one.seven\" did not start with substring \"1.7\", and \"one.seven\" did not start with substring \"1.8\"")

      val caught2 = intercept[AssertionError] {
        "one.seven" should ((startWith substring ("1.7")) or (startWith substring ("1.8")))
      }
      assert(caught2.getMessage === "\"one.seven\" did not start with substring \"1.7\", and \"one.seven\" did not start with substring \"1.8\"")

      val caught3 = intercept[AssertionError] {
        "one.seven" should (startWith substring ("1.7") or startWith substring ("1.8"))
      }
      assert(caught3.getMessage === "\"one.seven\" did not start with substring \"1.7\", and \"one.seven\" did not start with substring \"1.8\"")

      check(
        (s: String, t: String, u: String, v: String) => {
          (t.length != 0 && v.length != 0 && (s + u).indexOf(t) != 0 && (s + u).indexOf(v) != 0) ==>
            throwsAssertionError(s + u should (startWith substring (t) or startWith substring (v)))
        }
      )
    }

    it("should throw AssertionError if the string starts with the specified substring when used in a logical-and expression used with not") {

      val caught1 = intercept[AssertionError] {
        "1.7" should (not startWith substring ("1.8") and (not startWith substring ("1.7")))
      }
      assert(caught1.getMessage === "\"1.7\" did not start with substring \"1.8\", but \"1.7\" started with substring \"1.7\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should ((not startWith substring ("1.8")) and (not startWith substring ("1.7")))
      }
      assert(caught2.getMessage === "\"1.7\" did not start with substring \"1.8\", but \"1.7\" started with substring \"1.7\"")

      val caught3 = intercept[AssertionError] {
        "1.7" should (not startWith substring ("1.8") and not startWith substring ("1.7"))
      }
      assert(caught3.getMessage === "\"1.7\" did not start with substring \"1.8\", but \"1.7\" started with substring \"1.7\"")

      val caught4 = intercept[AssertionError] {
        "a1.7" should (not startWith substring ("1.8") and (not startWith substring ("a1.7")))
      }
      assert(caught4.getMessage === "\"a1.7\" did not start with substring \"1.8\", but \"a1.7\" started with substring \"a1.7\"")

      val caught5 = intercept[AssertionError] {
        "1.7b" should ((not startWith substring ("1.8")) and (not startWith substring ("1.7")))
      }
      assert(caught5.getMessage === "\"1.7b\" did not start with substring \"1.8\", but \"1.7b\" started with substring \"1.7\"")

      val caught6 = intercept[AssertionError] {
        "a1.7b" should (not startWith substring ("1.8") and not startWith substring ("a1.7"))
      }
      assert(caught6.getMessage === "\"a1.7b\" did not start with substring \"1.8\", but \"a1.7b\" started with substring \"a1.7\"")

      check(
        (s: String, t: String, u: String) =>
          (s + t + u).indexOf("hi") != 0 ==>
            throwsAssertionError(s + t + u should (not startWith substring ("hi") and not startWith substring (s)))
      )
    }

    it("should throw AssertionError if the string starts with the specified substring when used in a logical-or expression used with not") {

      val caught1 = intercept[AssertionError] {
        "1.7" should (not startWith substring ("1.7") or (not startWith substring ("1.7")))
      }
      assert(caught1.getMessage === "\"1.7\" started with substring \"1.7\", and \"1.7\" started with substring \"1.7\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should ((not startWith substring ("1.7")) or (not startWith substring ("1.7")))
      }
      assert(caught2.getMessage === "\"1.7\" started with substring \"1.7\", and \"1.7\" started with substring \"1.7\"")

      val caught3 = intercept[AssertionError] {
        "1.7" should (not startWith substring ("1.7") or not startWith substring ("1.7"))
      }
      assert(caught3.getMessage === "\"1.7\" started with substring \"1.7\", and \"1.7\" started with substring \"1.7\"")

      val caught4 = intercept[AssertionError] {
        "1.7" should (not (startWith substring ("1.7")) or not (startWith substring ("1.7")))
      }
      assert(caught4.getMessage === "\"1.7\" started with substring \"1.7\", and \"1.7\" started with substring \"1.7\"")

      val caught5 = intercept[AssertionError] {
        "a1.7" should (not startWith substring ("a1.") or (not startWith substring ("a1.7")))
      }
      assert(caught5.getMessage === "\"a1.7\" started with substring \"a1.\", and \"a1.7\" started with substring \"a1.7\"")

      val caught6 = intercept[AssertionError] {
        "1.7b" should ((not startWith substring ("1.7")) or (not startWith substring ("1.7")))
      }
      assert(caught6.getMessage === "\"1.7b\" started with substring \"1.7\", and \"1.7b\" started with substring \"1.7\"")

      val caught7 = intercept[AssertionError] {
        "a1.7b" should (not startWith substring ("a1.7") or not startWith substring ("a1"))
      }
      assert(caught7.getMessage === "\"a1.7b\" started with substring \"a1.7\", and \"a1.7b\" started with substring \"a1\"")

      val caught8 = intercept[AssertionError] {
        "a1.7b" should (not (startWith substring ("a1.7")) or not (startWith substring ("a1")))
      }
      assert(caught8.getMessage === "\"a1.7b\" started with substring \"a1.7\", and \"a1.7b\" started with substring \"a1\"")

      check(
        (s: String, t: String) =>
          throwsAssertionError(s + t should (not startWith substring (s) or not startWith substring ("")))
      )
    }
  }
}


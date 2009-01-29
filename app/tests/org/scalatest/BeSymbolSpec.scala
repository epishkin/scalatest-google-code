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

class BeSymbolSpec extends Spec with ShouldMatchers {

  describe("The be ('symbol) syntax") {

    it("should throw IllegalArgumentException if no <symbol> or is<Symbol> method exists") {
      class NoPredicateMock {
        override def toString = "NoPredicateMock"
      }
      val ex1 = intercept[IllegalArgumentException] {
        (new NoPredicateMock) should be ('empty)
      }
      ex1.getMessage should equal ("NoPredicateMock has neither an empty nor an isEmpty method")
      val ex2 = intercept[IllegalArgumentException] {
        (new NoPredicateMock) should not { be ('empty) }
      }
      ex2.getMessage should equal ("NoPredicateMock has neither an empty nor an isEmpty method")
      val ex3 = intercept[IllegalArgumentException] {
        (new NoPredicateMock) should be ('full)
      }
      ex3.getMessage should equal ("NoPredicateMock has neither a full nor an isFull method")
      val ex4 = intercept[IllegalArgumentException] {
        (new NoPredicateMock) should not (be ('full))
      }
      ex4.getMessage should equal ("NoPredicateMock has neither a full nor an isFull method")
    }
  }
/*
    it("should do nothing if the object has an appropriately named method, which returns true") {

      "1.78" should endWith substring (".78")
      "21.7" should endWith substring ("7")
      "21.78" should endWith substring ("21.78")
      check((s: String, t: String) => returnsNormally(s + t should endWith substring (t)))
    }

    it("should do nothing if the string does not end with the specified substring when used with not") {

      "eight" should not { endWith substring ("1.7") }
      "eight" should not endWith substring ("1.7")
      check((s: String, t: String) => !(s + t).endsWith(s) ==> returnsNormally(s + t should not (endWith substring (s))))
      check((s: String, t: String) => !(s + t).endsWith(s) ==> returnsNormally(s + t should not endWith substring (s)))
    }

    it("should do nothing if the string does not end with the specified substring when used in a logical-and expression") {

      "1.7b" should ((endWith substring ("1.7b")) and (endWith substring ("7b")))
      "1.7b" should (endWith substring ("1.7b") and (endWith substring ("7b")))
      "1.7b" should (endWith substring ("1.7b") and endWith substring ("7b"))

      check((s: String, t: String) => returnsNormally(s + t should (endWith substring (t) and endWith substring (""))))
    }

    it("should do nothing if the string does not end with the specified substring when used in a logical-or expression") {

      "1.7b" should (endWith substring ("hello") or (endWith substring ("1.7b")))
      "1.7b" should ((endWith substring ("hello")) or (endWith substring ("1.7b")))
      "1.7b" should (endWith substring ("hello") or endWith substring ("1.7b"))

      "1.7b" should (endWith substring ("hello") or (endWith substring ("7b")))
      "1.7b" should ((endWith substring ("hello")) or (endWith substring ("7b")))
      "1.7b" should (endWith substring ("hello") or endWith substring ("7b"))

      check((s: String, t: String) => returnsNormally(s + t should (endWith substring ("hi") or endWith substring (t))))
    }

    it("should do nothing if the string does not end with the specified substring when used in a logical-and expression with not") {

      "fred" should (not (endWith substring ("fre")) and not (endWith substring ("1.7")))
      "fred" should ((not endWith substring ("fre")) and (not endWith substring ("1.7")))
      "fred" should (not endWith substring ("fre") and not endWith substring ("1.7"))
      check((s: String) => !(s endsWith "bob") && !(s endsWith "1.7") ==> returnsNormally(s should (not endWith substring ("bob") and not endWith substring ("1.7"))))
    }

    it("should do nothing if the string does not end with the specified substring when used in a logical-or expression with not") {
      "fred" should (not (endWith substring ("fred")) or not (endWith substring ("1.7")))
      "fred" should ((not endWith substring ("fred")) or (not endWith substring ("1.7")))
      "fred" should (not endWith substring ("fred") or not endWith substring ("1.7"))
      check((s: String) => s.indexOf("a") != 0 || s.indexOf("b") != 0 ==> returnsNormally(s should (not endWith substring ("a") or not endWith substring ("b"))))
    }

    it("should throw AssertionError if the string does not match the specified substring") {

      val caught1 = intercept[AssertionError] {
        "1.7" should endWith substring ("1.78")
      }
      assert(caught1.getMessage === "\"1.7\" did not end with substring \"1.78\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should endWith substring ("21.7")
      }
      assert(caught2.getMessage === "\"1.7\" did not end with substring \"21.7\"")

      val caught3 = intercept[AssertionError] {
        "1.78" should endWith substring ("1.7")
      }
      assert(caught3.getMessage === "\"1.78\" did not end with substring \"1.7\"")

      val caught6 = intercept[AssertionError] {
        "eight" should endWith substring ("1.7")
      }
      assert(caught6.getMessage === "\"eight\" did not end with substring \"1.7\"")

      val caught7 = intercept[AssertionError] {
        "one.eight" should endWith substring ("1.7")
      }
      assert(caught7.getMessage === "\"one.eight\" did not end with substring \"1.7\"")

      val caught8 = intercept[AssertionError] {
        "onedoteight" should endWith substring ("1.7")
      }
      assert(caught8.getMessage === "\"onedoteight\" did not end with substring \"1.7\"")

      val caught9 = intercept[AssertionError] {
        "***" should endWith substring ("1.7")
      }
      assert(caught9.getMessage === "\"***\" did not end with substring \"1.7\"")

      check((s: String) => !(s endsWith "1.7") ==> throwsAssertionError(s should endWith substring ("1.7")))
    }

    it("should throw AssertionError if the string does matches the specified substring when used with not") {

      val caught1 = intercept[AssertionError] {
        "1.7" should not { endWith substring ("1.7") }
      }
      assert(caught1.getMessage === "\"1.7\" ended with substring \"1.7\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should not { endWith substring ("7") }
      }
      assert(caught2.getMessage === "\"1.7\" ended with substring \"7\"")

      val caught3 = intercept[AssertionError] {
        "-1.8" should not { endWith substring (".8") }
      }
      assert(caught3.getMessage === "\"-1.8\" ended with substring \".8\"")

      val caught4 = intercept[AssertionError] {
        "8b" should not { endWith substring ("b") }
      }
      assert(caught4.getMessage === "\"8b\" ended with substring \"b\"")

      val caught5 = intercept[AssertionError] {
        "1." should not { endWith substring ("1.") }
      }
      assert(caught5.getMessage === "\"1.\" ended with substring \"1.\"")

      val caught11 = intercept[AssertionError] {
        "1.7" should not endWith substring (".7")
      }
      assert(caught11.getMessage === "\"1.7\" ended with substring \".7\"")

      val caught13 = intercept[AssertionError] {
        "-1.8" should not endWith substring ("8")
      }
      assert(caught13.getMessage === "\"-1.8\" ended with substring \"8\"")

      val caught14 = intercept[AssertionError] {
        "8" should not endWith substring ("")
      }
      assert(caught14.getMessage === "\"8\" ended with substring \"\"")

      val caught15 = intercept[AssertionError] {
        "1." should not endWith substring ("1.")
      }
      assert(caught15.getMessage === "\"1.\" ended with substring \"1.\"")

      val caught21 = intercept[AssertionError] {
        "1.7a" should not { endWith substring ("7a") }
      }
      assert(caught21.getMessage === "\"1.7a\" ended with substring \"7a\"")

      val caught22 = intercept[AssertionError] {
        "b1.7" should not { endWith substring ("1.7") }
      }
      assert(caught22.getMessage === "\"b1.7\" ended with substring \"1.7\"")

      val caught23 = intercept[AssertionError] {
        "ba-1.8" should not { endWith substring ("a-1.8") }
      }
      assert(caught23.getMessage === "\"ba-1.8\" ended with substring \"a-1.8\"")

      check((s: String) => s.length != 0 ==> throwsAssertionError(s should not endWith substring (s.substring(s.length - 1, s.length))))
    }

    it("should throw AssertionError if the string ends with the specified substring when used in a logical-and expression") {

      val caught1 = intercept[AssertionError] {
        "1.7" should (endWith substring ("1.7") and (endWith substring ("1.8")))
      }
      assert(caught1.getMessage === "\"1.7\" ended with substring \"1.7\", but \"1.7\" did not end with substring \"1.8\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should ((endWith substring ("7")) and (endWith substring ("1.8")))
      }
      assert(caught2.getMessage === "\"1.7\" ended with substring \"7\", but \"1.7\" did not end with substring \"1.8\"")

      val caught3 = intercept[AssertionError] {
        "1.7" should (endWith substring (".7") and endWith substring ("1.8"))
      }
      assert(caught3.getMessage === "\"1.7\" ended with substring \".7\", but \"1.7\" did not end with substring \"1.8\"")

      // Check to make sure the error message "short circuits" (i.e., just reports the left side's failure)
      val caught4 = intercept[AssertionError] {
        "one.eight" should (endWith substring ("1.7") and (endWith substring ("1.8")))
      }
      assert(caught4.getMessage === "\"one.eight\" did not end with substring \"1.7\"")

      val caught5 = intercept[AssertionError] {
        "one.eight" should ((endWith substring ("1.7")) and (endWith substring ("1.8")))
      }
      assert(caught5.getMessage === "\"one.eight\" did not end with substring \"1.7\"")

      val caught6 = intercept[AssertionError] {
        "one.eight" should (endWith substring ("1.7") and endWith substring ("1.8"))
      }
      assert(caught6.getMessage === "\"one.eight\" did not end with substring \"1.7\"")

      check((s: String, t: String, u: String) => !((s + u) endsWith t) ==> throwsAssertionError(s + u should (endWith substring (u) and endWith substring (t))))
    }

    it("should throw AssertionError if the string ends with the specified substring when used in a logical-or expression") {

      val caught1 = intercept[AssertionError] {
        "one.seven" should (endWith substring ("1.7") or (endWith substring ("1.8")))
      }
      assert(caught1.getMessage === "\"one.seven\" did not end with substring \"1.7\", and \"one.seven\" did not end with substring \"1.8\"")

      val caught2 = intercept[AssertionError] {
        "one.seven" should ((endWith substring ("1.7")) or (endWith substring ("1.8")))
      }
      assert(caught2.getMessage === "\"one.seven\" did not end with substring \"1.7\", and \"one.seven\" did not end with substring \"1.8\"")

      val caught3 = intercept[AssertionError] {
        "one.seven" should (endWith substring ("1.7") or endWith substring ("1.8"))
      }
      assert(caught3.getMessage === "\"one.seven\" did not end with substring \"1.7\", and \"one.seven\" did not end with substring \"1.8\"")

      check(
        (s: String, t: String, u: String, v: String) => {
          (t.length != 0 && v.length != 0 && (s + u).indexOf(t) != 0 && (s + u).indexOf(v) != 0) ==>
            throwsAssertionError(s + u should (endWith substring (t) or endWith substring (v)))
        }
      )
    }

    it("should throw AssertionError if the string ends with the specified substring when used in a logical-and expression used with not") {

      val caught1 = intercept[AssertionError] {
        "1.7" should (not endWith substring ("1.8") and (not endWith substring ("1.7")))
      }
      assert(caught1.getMessage === "\"1.7\" did not end with substring \"1.8\", but \"1.7\" ended with substring \"1.7\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should ((not endWith substring ("1.8")) and (not endWith substring ("1.7")))
      }
      assert(caught2.getMessage === "\"1.7\" did not end with substring \"1.8\", but \"1.7\" ended with substring \"1.7\"")

      val caught3 = intercept[AssertionError] {
        "1.7" should (not endWith substring ("1.8") and not endWith substring ("1.7"))
      }
      assert(caught3.getMessage === "\"1.7\" did not end with substring \"1.8\", but \"1.7\" ended with substring \"1.7\"")

      val caught4 = intercept[AssertionError] {
        "a1.7" should (not endWith substring ("1.8") and (not endWith substring ("a1.7")))
      }
      assert(caught4.getMessage === "\"a1.7\" did not end with substring \"1.8\", but \"a1.7\" ended with substring \"a1.7\"")

      val caught5 = intercept[AssertionError] {
        "b1.7" should ((not endWith substring ("1.8")) and (not endWith substring ("1.7")))
      }
      assert(caught5.getMessage === "\"b1.7\" did not end with substring \"1.8\", but \"b1.7\" ended with substring \"1.7\"")

      val caught6 = intercept[AssertionError] {
        "a1.7b" should (not endWith substring ("1.8") and not endWith substring ("1.7b"))
      }
      assert(caught6.getMessage === "\"a1.7b\" did not end with substring \"1.8\", but \"a1.7b\" ended with substring \"1.7b\"")

      check(
        (s: String, t: String, u: String) =>
          (s + t + u).indexOf("hi") != 0 ==>
            throwsAssertionError(s + t + u should (not endWith substring ("hi") and not endWith substring (u)))
      )
    }

    it("should throw AssertionError if the string ends with the specified substring when used in a logical-or expression used with not") {

      val caught1 = intercept[AssertionError] {
        "1.7" should (not endWith substring ("1.7") or (not endWith substring ("1.7")))
      }
      assert(caught1.getMessage === "\"1.7\" ended with substring \"1.7\", and \"1.7\" ended with substring \"1.7\"")

      val caught2 = intercept[AssertionError] {
        "1.7" should ((not endWith substring ("1.7")) or (not endWith substring ("1.7")))
      }
      assert(caught2.getMessage === "\"1.7\" ended with substring \"1.7\", and \"1.7\" ended with substring \"1.7\"")

      val caught3 = intercept[AssertionError] {
        "1.7" should (not endWith substring ("1.7") or not endWith substring ("1.7"))
      }
      assert(caught3.getMessage === "\"1.7\" ended with substring \"1.7\", and \"1.7\" ended with substring \"1.7\"")

      val caught4 = intercept[AssertionError] {
        "1.7" should (not (endWith substring ("1.7")) or not (endWith substring ("1.7")))
      }
      assert(caught4.getMessage === "\"1.7\" ended with substring \"1.7\", and \"1.7\" ended with substring \"1.7\"")

      val caught5 = intercept[AssertionError] {
        "a1.7" should (not endWith substring (".7") or (not endWith substring ("a1.7")))
      }
      assert(caught5.getMessage === "\"a1.7\" ended with substring \".7\", and \"a1.7\" ended with substring \"a1.7\"")

      val caught6 = intercept[AssertionError] {
        "b1.7" should ((not endWith substring ("1.7")) or (not endWith substring ("1.7")))
      }
      assert(caught6.getMessage === "\"b1.7\" ended with substring \"1.7\", and \"b1.7\" ended with substring \"1.7\"")

      val caught7 = intercept[AssertionError] {
        "a1.7b" should (not endWith substring ("1.7b") or not endWith substring ("7b"))
      }
      assert(caught7.getMessage === "\"a1.7b\" ended with substring \"1.7b\", and \"a1.7b\" ended with substring \"7b\"")

      val caught8 = intercept[AssertionError] {
        "a1.7b" should (not (endWith substring ("1.7b")) or not (endWith substring ("7b")))
      }
      assert(caught8.getMessage === "\"a1.7b\" ended with substring \"1.7b\", and \"a1.7b\" ended with substring \"7b\"")

      check(
        (s: String, t: String) =>
          throwsAssertionError(s + t should (not endWith substring (t) or not endWith substring ("")))
      )
    }
  }

  describe("The be matcher") {

    describe("(for symbols)") {

      it("should call isEmpty when passed 'empty") {
        val emptySet = Set[Int]()
        emptySet should be ('empty)
        val nonEmptySet = Set(1, 2, 3)
        nonEmptySet should not { be ('empty) }
        val caught1 = intercept[AssertionError] {
          nonEmptySet should be ('empty)
        }
        assert(caught1.getMessage === "Set(1, 2, 3) was not empty")
        / * val caught2 = intercept[AssertionError] {
          nonEmptySet shouldNot be ('hasDefiniteSize)
        }
        assert(caught2.getMessage === "Set(1, 2, 3) was hasDefiniteSize") * /
        val caught2 = intercept[AssertionError] {
          nonEmptySet should not { be ('hasDefiniteSize) }
        }
        assert(caught2.getMessage === "Set(1, 2, 3) was hasDefiniteSize")
        / * val caught3 = intercept[IllegalArgumentException] {
          nonEmptySet shouldNot be ('happy)
        }
        assert(caught3.getMessage === "Set(1, 2, 3) has neither a happy nor an isHappy method")
        val caught4 = intercept[IllegalArgumentException] {
          "unhappy" shouldNot be ('happy)
        }
        assert(caught4.getMessage === "\"unhappy\" has neither a happy nor an isHappy method") * /
        val caught3 = intercept[IllegalArgumentException] {
          nonEmptySet should not { be ('happy) }
        }
        assert(caught3.getMessage === "Set(1, 2, 3) has neither a happy nor an isHappy method")
        val caught4 = intercept[IllegalArgumentException] {
          "unhappy" should not { be ('happy) }
        }
        assert(caught4.getMessage === "\"unhappy\" has neither a happy nor an isHappy method")
      }

      it("should be invokable from be a Symbol and be an Symbol") {
        val emptySet = Set()
        emptySet should be a ('empty)
        emptySet should be an ('empty)
        val nonEmptySet = Set(1, 2, 3)
        nonEmptySet should not { be a ('empty) }
        nonEmptySet should not { be an ('empty) }
      }

      it("should call empty when passed 'empty") {
        class EmptyMock {
          def empty: Boolean = true
        }
        class NonEmptyMock {
          def empty: Boolean = false
        }
        (new EmptyMock) should be ('empty)
        (new NonEmptyMock) should not { be ('empty) }
        // (new NonEmptyMock) shouldNot be ('empty)
      }

// STOLE FROM HERE
      it("should throw IllegalArgumentException if both an empty and an isEmpty method exist") {
        class EmptyMock {
          def empty: Boolean = true
          def isEmpty: Boolean = true
          override def toString = "EmptyMock"
        }
        class NonEmptyMock {
          def empty: Boolean = true
          def isEmpty: Boolean = true
          override def toString = "NonEmptyMock"
        }
        val ex1 = intercept[IllegalArgumentException] {
          (new EmptyMock) should be ('empty)
        }
        ex1.getMessage should equal ("EmptyMock has both an empty and an isEmpty method")
        val ex2 = intercept[IllegalArgumentException] {
          (new NonEmptyMock) should not { be ('empty) }
        }
        ex2.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method")
        / * val ex3 = intercept[IllegalArgumentException] {
          (new NonEmptyMock) shouldNot be ('empty)
        }
        ex3.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method") * /
      }

      it("should access an 'empty' val when passed 'empty") {
        class EmptyMock {
          val empty: Boolean = true
        }
        class NonEmptyMock {
          val empty: Boolean = false
        }
        (new EmptyMock) should be ('empty)
        (new NonEmptyMock) should not { be ('empty) }
        // (new NonEmptyMock) shouldNot be ('empty)
      }
    }
  }

  describe("the be ('empty) syntax") {

    it("should call isEmpty") {
      val emptySet = Set[Int]()
      emptySet should be ('empty)
      val nonEmptySet = Set(1, 2, 3)
      nonEmptySet should not { be ('empty) }
    }

    it("should call empty when passed 'empty") {
      class EmptyMock {
        def empty: Boolean = true
      }
      class NonEmptyMock {
        def empty: Boolean = false
      }
      (new EmptyMock) should be ('empty)
      (new NonEmptyMock) should not { be ('empty) }
      // (new NonEmptyMock) shouldNot be ('empty)
    }

    it("should throw IllegalArgumentException if no empty or isEmpty method") {
      class EmptyMock {
        override def toString = "EmptyMock"
      }
      class NonEmptyMock {
        override def toString = "NonEmptyMock"
      }
      val ex1 = intercept[IllegalArgumentException] {
        (new EmptyMock) should be ('empty)
      }
      ex1.getMessage should equal ("EmptyMock has neither an empty nor an isEmpty method")
      val ex2 = intercept[IllegalArgumentException] {
        (new NonEmptyMock) should not { be ('empty) }
      }
      ex2.getMessage should equal ("NonEmptyMock has neither an empty nor an isEmpty method")
      / * val ex3 = intercept[IllegalArgumentException] {
        (new NonEmptyMock) shouldNot be ('empty)
      }
      ex3.getMessage should equal ("NonEmptyMock has neither an empty nor an isEmpty method") * /
    }

    it("should throw IllegalArgumentException if both an empty and an isEmpty method exist") {
      class EmptyMock {
        def empty: Boolean = true
        def isEmpty: Boolean = true
        override def toString = "EmptyMock"
      }
      class NonEmptyMock {
        def empty: Boolean = true
        def isEmpty: Boolean = true
        override def toString = "NonEmptyMock"
      }
      val ex1 = intercept[IllegalArgumentException] {
        (new EmptyMock) should be ('empty)
      }
      ex1.getMessage should equal ("EmptyMock has both an empty and an isEmpty method")
      val ex2 = intercept[IllegalArgumentException] {
        (new NonEmptyMock) should not { be ('empty) }
      }
      ex2.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method")
      / * val ex3 = intercept[IllegalArgumentException] {
        (new NonEmptyMock) shouldNot be ('empty)
      }
      ex3.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method") * /
    }

    it("should access an 'empty' val when passed 'empty") {
      class EmptyMock {
        val empty: Boolean = true
      }
      class NonEmptyMock {
        val empty: Boolean = false
      }
      (new EmptyMock) should be ('empty)
      (new NonEmptyMock) should not { be ('empty) }
      // (new NonEmptyMock) shouldNot be ('empty)
    }
  }

  describe("The be 'defined syntax") {

    it("should do nothing when used with a Some") {
      val someString: Some[String] = Some("hi")
      someString should be ('defined)
      val optionString: Option[String] = Some("hi")
      optionString should be ('defined)
    }

    it("should throw AssertionError when used with a None") {
      val none: None.type = None
      val caught1 = intercept[AssertionError] {
        none should be ('defined)
      }
      assert(caught1.getMessage === "None was not defined")
      val option: Option[Int] = None
      val caught2 = intercept[AssertionError] {
        option should be ('defined)
      }
      assert(caught2.getMessage === "None was not defined")
    }

    it("should call defined") {
      class DefinedMock {
        def defined: Boolean = true
      }
      class NonDefinedMock {
        def defined: Boolean = false
      }
      (new DefinedMock) should be ('defined)
      (new NonDefinedMock) should not { be ('defined) }
      // (new NonDefinedMock) shouldNot be ('defined)
    }

    it("should throw IllegalArgumentException if no defined or isDefined method") {
      class DefinedMock {
        override def toString = "DefinedMock"
      }
      class NonDefinedMock {
        override def toString = "NonDefinedMock"
      }
      val ex1 = intercept[IllegalArgumentException] {
        (new DefinedMock) should be ('defined)
      }
      ex1.getMessage should equal ("DefinedMock has neither a defined nor an isDefined method")
      val ex2 = intercept[IllegalArgumentException] {
        (new NonDefinedMock) should not { be ('defined) }
      }
      ex2.getMessage should equal ("NonDefinedMock has neither a defined nor an isDefined method")
      / * val ex3 = intercept[IllegalArgumentException] {
        (new NonDefinedMock) shouldNot be ('defined)
      }
      ex3.getMessage should equal ("NonDefinedMock has neither a defined nor an isDefined method") * /
    }

    it("should throw IllegalArgumentException if both a defined and an isDefined method exist") {
      class DefinedMock {
        def defined: Boolean = true
        def isDefined: Boolean = true
        override def toString = "DefinedMock"
      }
      class NonDefinedMock {
        def defined: Boolean = true
        def isDefined: Boolean = true
        override def toString = "NonDefinedMock"
      }
      val ex1 = intercept[IllegalArgumentException] {
        (new DefinedMock) should be ('defined)
      }
      ex1.getMessage should equal ("DefinedMock has both a defined and an isDefined method")
      val ex2 = intercept[IllegalArgumentException] {
        (new NonDefinedMock) should not { be ('defined) }
      }
      ex2.getMessage should equal ("NonDefinedMock has both a defined and an isDefined method")
      / * val ex3 = intercept[IllegalArgumentException] {
        (new NonDefinedMock) shouldNot be ('defined)
      }
      ex3.getMessage should equal ("NonDefinedMock has both a defined and an isDefined method") * /
    }

    it("should access an 'defined' val") {
      class DefinedMock {
        val defined: Boolean = true
      }
      class NonDefinedMock {
        val defined: Boolean = false
      }
      (new DefinedMock) should be ('defined)
      (new NonDefinedMock) should not { be ('defined) }
      // (new NonDefinedMock) shouldNot be ('defined)
    }
  }
*/
}

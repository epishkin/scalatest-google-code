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

class BeSymbolSpec extends Spec with ShouldMatchers with EmptyMocks {

  describe("The be ('symbol) syntax") {

    it("should do nothing if the object has an appropriately named method, which returns true") {
      emptyMock should be ('empty)
      isEmptyMock should be ('empty)
    }

    it("should throw IllegalArgumentException if no <symbol> or is<Symbol> method exists") {
      val ex1 = intercept[IllegalArgumentException] {
        noPredicateMock should be ('empty)
      }
      ex1.getMessage should equal ("NoPredicateMock has neither an empty nor an isEmpty method")
      // Check message for name that starts with a consonant (should use a instead of an)
      val ex2 = intercept[IllegalArgumentException] {
        noPredicateMock should be ('full)
      }
      ex2.getMessage should equal ("NoPredicateMock has neither a full nor an isFull method")
    }

    it("should do nothing if the object has an appropriately named method, which returns true, even if the method contains operator characters") {
      val opNames = new OperatorNames
      opNames should be ('op_21_!)
      opNames should be ('op_23_#)
      opNames should be ('op_25_%)
      opNames should be ('op_26_&)
      opNames should be ('op_2a_*)
      opNames should be ('op_2b_+)
      opNames should be ('op_2d_-)
      opNames should be ('op_2f_/)
      opNames should be ('op_3a_:)
      opNames should be ('op_3c_<)
      opNames should be ('op_3d_=)
      opNames should be ('op_3e_>)
      opNames should be ('op_3f_?)
      opNames should be ('op_40_@)
      opNames should be ('op_5c_\)
      opNames should be ('op_5e_^)
      opNames should be ('op_7c_|)
      opNames should be ('op_7e_~)

/*
      opNames should be ('!!!)
      opNames should be ('###)
      opNames should be ('%%%)
      opNames should be ('&&&)
      opNames should be ('***)
      opNames should be ('+++)
      opNames should be ('---)
      opNames should be ('/)
      opNames should be (':::)
      opNames should be ('<<<)
      opNames should be ('===)
      opNames should be ('>>>)
      opNames should be ('???)
      opNames should be ('@@@)
      opNames should be ('\\\)
      opNames should be ('^^^)
      opNames should be ('|||)
      opNames should be ('~~~)
*/
    }

    it("should do nothing if the object has an appropriately named method, which returns false when used with not") {
      notEmptyMock should not { be ('empty) }
      notEmptyMock should not be ('empty)
      isNotEmptyMock should not { be ('empty) }
      isNotEmptyMock should not be ('empty)
    }

    it("should throw IllegalArgumentException if no <symbol> or is<Symbol> method exists, when used with not") {
      val ex1 = intercept[IllegalArgumentException] {
        noPredicateMock should not { be ('empty) }
      }
      ex1.getMessage should equal ("NoPredicateMock has neither an empty nor an isEmpty method")
      val ex2 = intercept[IllegalArgumentException] {
        noPredicateMock should not (be ('full))
      }
      ex2.getMessage should equal ("NoPredicateMock has neither a full nor an isFull method")
      val ex3 = intercept[IllegalArgumentException] {
        noPredicateMock should not be ('empty)
      }
      ex3.getMessage should equal ("NoPredicateMock has neither an empty nor an isEmpty method")
      val ex4 = intercept[IllegalArgumentException] {
        noPredicateMock should not be ('full)
      }
      ex4.getMessage should equal ("NoPredicateMock has neither a full nor an isFull method")
    }

    it("should do nothing if the object has an appropriately named method, which returns true, when used in a logical-and expression") {
      emptyMock should ((be ('empty)) and (be ('empty)))
      emptyMock should (be ('empty) and (be ('empty)))
      emptyMock should (be ('empty) and be ('empty))
      isEmptyMock should ((be ('empty)) and (be ('empty)))
      isEmptyMock should (be ('empty) and (be ('empty)))
      isEmptyMock should (be ('empty) and be ('empty))
    }

    it("should do nothing if the object has an appropriately named method, which returns true, when used in a logical-or expression") {

      emptyMock should ((be ('full)) or (be ('empty)))
      emptyMock should (be ('full) or (be ('empty)))
      emptyMock should (be ('full) or be ('empty))
      isEmptyMock should ((be ('full)) or (be ('empty)))
      isEmptyMock should (be ('full) or (be ('empty)))
      isEmptyMock should (be ('full) or be ('empty))

      emptyMock should ((be ('empty)) or (be ('full)))
      emptyMock should (be ('empty) or (be ('full)))
      emptyMock should (be ('empty) or be ('full))
      isEmptyMock should ((be ('empty)) or (be ('full)))
      isEmptyMock should (be ('empty) or (be ('full)))
      isEmptyMock should (be ('empty) or be ('full))
    }

    it("should do nothing if the object has an appropriately named method, which returns false, when used in a logical-and expression with not") {

      notEmptyMock should (not (be ('empty)) and not (be ('empty)))
      notEmptyMock should ((not be ('empty)) and (not be ('empty)))
      notEmptyMock should (not be ('empty) and not be ('empty))

      isNotEmptyMock should (not (be ('empty)) and not (be ('empty)))
      isNotEmptyMock should ((not be ('empty)) and (not be ('empty)))
      isNotEmptyMock should (not be ('empty) and not be ('empty))
    }

    it("should do nothing if the object has an appropriately named method, which returns false, when used in a logical-or expression with not") {

      notEmptyMock should (not (be ('empty)) or not (be ('empty)))
      notEmptyMock should ((not be ('empty)) or (not be ('empty)))
      notEmptyMock should (not be ('empty) or not be ('empty))

      isNotEmptyMock should (not (be ('empty)) or not (be ('empty)))
      isNotEmptyMock should ((not be ('empty)) or (not be ('empty)))
      isNotEmptyMock should (not be ('empty) or not be ('empty))

      notEmptyMock should (not (be ('full)) or not (be ('empty)))
      notEmptyMock should ((not be ('full)) or (not be ('empty)))
      notEmptyMock should (not be ('full) or not be ('empty))

      isNotEmptyMock should (not (be ('full)) or not (be ('empty)))
      isNotEmptyMock should ((not be ('full)) or (not be ('empty)))
      isNotEmptyMock should (not be ('full) or not be ('empty))
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns false") {
      val caught1 = intercept[AssertionError] {
        notEmptyMock should be ('empty)
      }
      assert(caught1.getMessage === "NotEmptyMock was not empty")
      val caught2 = intercept[AssertionError] {
        isNotEmptyMock should be ('empty)
      }
      assert(caught2.getMessage === "IsNotEmptyMock was not empty")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true when used with not") {
      val caught1 = intercept[AssertionError] {
        emptyMock should not { be ('empty) }
      }
      assert(caught1.getMessage === "EmptyMock was empty")
      val caught2 = intercept[AssertionError] {
        emptyMock should not be ('empty)
      }
      assert(caught2.getMessage === "EmptyMock was empty")
      val caught3 = intercept[AssertionError] {
        isEmptyMock should not { be ('empty) }
      }
      assert(caught3.getMessage === "IsEmptyMock was empty")
      val caught4 = intercept[AssertionError] {
        isEmptyMock should not be ('empty)
      }
      assert(caught4.getMessage === "IsEmptyMock was empty")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns false, when used in a logical-and expression") {
      val caught1 = intercept[AssertionError] {
        emptyMock should ((be ('empty)) and (be ('full)))
      }
      assert(caught1.getMessage === "EmptyMock was empty, but EmptyMock was not full")
      val caught2 = intercept[AssertionError] {
        emptyMock should (be ('empty) and (be ('full)))
      }
      assert(caught2.getMessage === "EmptyMock was empty, but EmptyMock was not full")
      val caught3 = intercept[AssertionError] {
        emptyMock should (be ('empty) and be ('full))
      }
      assert(caught3.getMessage === "EmptyMock was empty, but EmptyMock was not full")
      val caught4 = intercept[AssertionError] {
        isEmptyMock should ((be ('empty)) and (be ('full)))
      }
      assert(caught4.getMessage === "IsEmptyMock was empty, but IsEmptyMock was not full")
      val caught5 = intercept[AssertionError] {
        isEmptyMock should (be ('empty) and (be ('full)))
      }
      assert(caught5.getMessage === "IsEmptyMock was empty, but IsEmptyMock was not full")
      val caught6 = intercept[AssertionError] {
        isEmptyMock should (be ('empty) and be ('full))
      }
      assert(caught6.getMessage === "IsEmptyMock was empty, but IsEmptyMock was not full")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns false, when used in a logical-or expression") {

      val caught1 = intercept[AssertionError] {
        notEmptyMock should ((be ('empty)) or (be ('empty)))
      }
      assert(caught1.getMessage === "NotEmptyMock was not empty, and NotEmptyMock was not empty")
      val caught2 = intercept[AssertionError] {
        notEmptyMock should (be ('empty) or (be ('empty)))
      }
      assert(caught2.getMessage === "NotEmptyMock was not empty, and NotEmptyMock was not empty")
      val caught3 = intercept[AssertionError] {
        notEmptyMock should (be ('empty) or be ('empty))
      }
      assert(caught3.getMessage === "NotEmptyMock was not empty, and NotEmptyMock was not empty")
      val caught4 = intercept[AssertionError] {
        isNotEmptyMock should ((be ('empty)) or (be ('empty)))
      }
      assert(caught4.getMessage === "IsNotEmptyMock was not empty, and IsNotEmptyMock was not empty")
      val caught5 = intercept[AssertionError] {
        isNotEmptyMock should (be ('empty) or (be ('empty)))
      }
      assert(caught5.getMessage === "IsNotEmptyMock was not empty, and IsNotEmptyMock was not empty")
      val caught6 = intercept[AssertionError] {
        isNotEmptyMock should (be ('empty) or be ('empty))
      }
      assert(caught6.getMessage === "IsNotEmptyMock was not empty, and IsNotEmptyMock was not empty")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true, when used in a logical-and expression with not") {

      val caught1 = intercept[AssertionError] {
        emptyMock should (not (be ('full)) and not (be ('empty)))
      }
      assert(caught1.getMessage === "EmptyMock was not full, but EmptyMock was empty")
      val caught2 = intercept[AssertionError] {
        emptyMock should ((not be ('full)) and (not be ('empty)))
      }
      assert(caught2.getMessage === "EmptyMock was not full, but EmptyMock was empty")
      val caught3 = intercept[AssertionError] {
        emptyMock should (not be ('full) and not be ('empty))
      }
      assert(caught3.getMessage === "EmptyMock was not full, but EmptyMock was empty")
      val caught4 = intercept[AssertionError] {
        isEmptyMock should (not (be ('full)) and not (be ('empty)))
      }
      assert(caught4.getMessage === "IsEmptyMock was not full, but IsEmptyMock was empty")
      val caught5 = intercept[AssertionError] {
        isEmptyMock should ((not be ('full)) and (not be ('empty)))
      }
      assert(caught5.getMessage === "IsEmptyMock was not full, but IsEmptyMock was empty")
      val caught6 = intercept[AssertionError] {
        isEmptyMock should (not be ('full) and not be ('empty))
      }
      assert(caught6.getMessage === "IsEmptyMock was not full, but IsEmptyMock was empty")
      // Check that the error message "short circuits"
      val caught7 = intercept[AssertionError] {
        emptyMock should (not (be ('empty)) and not (be ('full)))
      }
      assert(caught7.getMessage === "EmptyMock was empty")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true, when used in a logical-or expression with not") {

      val caught1 = intercept[AssertionError] {
        emptyMock should (not (be ('empty)) or not (be ('empty)))
      }
      assert(caught1.getMessage === "EmptyMock was empty, and EmptyMock was empty")
      val caught2 = intercept[AssertionError] {
        emptyMock should ((not be ('empty)) or (not be ('empty)))
      }
      assert(caught2.getMessage === "EmptyMock was empty, and EmptyMock was empty")
      val caught3 = intercept[AssertionError] {
        emptyMock should (not be ('empty) or not be ('empty))
      }
      assert(caught3.getMessage === "EmptyMock was empty, and EmptyMock was empty")
      val caught4 = intercept[AssertionError] {
        isEmptyMock should (not (be ('empty)) or not (be ('empty)))
      }
      assert(caught4.getMessage === "IsEmptyMock was empty, and IsEmptyMock was empty")
      val caught5 = intercept[AssertionError] {
        isEmptyMock should ((not be ('empty)) or (not be ('empty)))
      }
      assert(caught5.getMessage === "IsEmptyMock was empty, and IsEmptyMock was empty")
      val caught6 = intercept[AssertionError] {
        isEmptyMock should (not be ('empty) or not be ('empty))
      }
      assert(caught6.getMessage === "IsEmptyMock was empty, and IsEmptyMock was empty")
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
        val caught2 = intercept[AssertionError] {
          nonEmptySet should not { be ('hasDefiniteSize) }
        }
        assert(caught2.getMessage === "Set(1, 2, 3) was hasDefiniteSize")
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
}

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
        /* val caught2 = intercept[AssertionError] {
          nonEmptySet shouldNot be ('hasDefiniteSize)
        }
        assert(caught2.getMessage === "Set(1, 2, 3) was hasDefiniteSize") */
        val caught2 = intercept[AssertionError] {
          nonEmptySet should not { be ('hasDefiniteSize) }
        }
        assert(caught2.getMessage === "Set(1, 2, 3) was hasDefiniteSize")
        /* val caught3 = intercept[IllegalArgumentException] {
          nonEmptySet shouldNot be ('happy)
        }
        assert(caught3.getMessage === "Set(1, 2, 3) has neither a happy nor an isHappy method")
        val caught4 = intercept[IllegalArgumentException] {
          "unhappy" shouldNot be ('happy)
        }
        assert(caught4.getMessage === "\"unhappy\" has neither a happy nor an isHappy method") */
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
        /* val ex3 = intercept[IllegalArgumentException] {
          (new NonEmptyMock) shouldNot be ('empty)
        }
        ex3.getMessage should equal ("NonEmptyMock has neither an empty nor an isEmpty method") */
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
        /* val ex3 = intercept[IllegalArgumentException] {
          (new NonEmptyMock) shouldNot be ('empty)
        }
        ex3.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method") */
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
      /* val ex3 = intercept[IllegalArgumentException] {
        (new NonEmptyMock) shouldNot be ('empty)
      }
      ex3.getMessage should equal ("NonEmptyMock has neither an empty nor an isEmpty method") */
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
      /* val ex3 = intercept[IllegalArgumentException] {
        (new NonEmptyMock) shouldNot be ('empty)
      }
      ex3.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method") */
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

    /* This was dropped to eliminate redundancy and the special treatment of strings 
    it("should look for a length of 0 when called on a string") {
      "" should be ('empty)
      val caught = intercept[AssertionError] {
        "hi" should be ('empty)
      }
      caught.getMessage should equal ("\"hi\" was not empty")
    } */
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
      /* val ex3 = intercept[IllegalArgumentException] {
        (new NonDefinedMock) shouldNot be ('defined)
      }
      ex3.getMessage should equal ("NonDefinedMock has neither a defined nor an isDefined method") */
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
      /* val ex3 = intercept[IllegalArgumentException] {
        (new NonDefinedMock) shouldNot be ('defined)
      }
      ex3.getMessage should equal ("NonDefinedMock has both a defined and an isDefined method") */
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

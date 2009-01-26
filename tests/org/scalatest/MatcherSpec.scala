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

class MatcherSpec extends Spec with ShouldMatchers {

  describe("The be matcher") {

    describe("(for booleans)") {

      it("should do nothing when false is compared to false") {
        false should be (false)
      }

      it("should do nothing when true is compared to true") {
        true should be (true)
      }

      it("should throw an assertion error when not equal") {
        val caught = intercept[AssertionError] {
          false should be (true)
        }
        assert(caught.getMessage === "false was not true")
      }
    }

    describe("(for null)") {

      it("should do nothing when null is compared to null") {
        val o: String = null
        o should be (null)
        o should equal (null)
      }

      it("should throw an assertion error when non-null compared to null") {
        val caught = intercept[AssertionError] {
          val o = "Helloooooo"
          o should be (null)
        }
        assert(caught.getMessage === "\"Helloooooo\" was not null")
      }

      it("should do nothing when non-null is compared to not null") {
        val o = "Helloooooo"
        o should not { be (null) }
        // o shouldNot be (null)
      }

      it("should throw an assertion error when null compared to not null") {
        val caught1 = intercept[AssertionError] {
          val o: String = null
          o should not { be (null) }
        }
        assert(caught1.getMessage === "null was null")
        /* val caught2 = intercept[AssertionError] {
          val o: String = null
          o shouldNot be (null)
        }
        assert(caught2.getMessage === "null was null")  */
      }

      it("should work when used in a logical expression") {
        val o: String = null
        o should { be (null) and equal (null) }
        o should { equal (null) and be (null) }
      }
    }

    describe("(for Nil)") {

      it("should do nothing when an empty list is compared to Nil") {
        val emptyList = List[String]()
        emptyList should be (Nil)
        emptyList should equal (Nil)
      }

      it("should throw an assertion error when a non-empty list is compared to Nil") {
        val nonEmptyList = List("Helloooooo")
        val caught1 = intercept[AssertionError] {
          nonEmptyList should be (Nil)
        }
        assert(caught1.getMessage === "List(Helloooooo) was not List()")
        val caught2 = intercept[AssertionError] {
          nonEmptyList should equal (Nil)
        }
        assert(caught2.getMessage === "List(Helloooooo) did not equal List()")
      }

      it("should do nothing when non-null is compared to not null") {
        val nonEmptyList = List("Helloooooo")
        nonEmptyList should not { be (Nil) }
        // nonEmptyList shouldNot be (Nil)
        nonEmptyList should not { equal (Nil) }
        // nonEmptyList shouldNot equal (Nil)
      }

      it("should throw an assertion error when null compared to not null") {
        val emptyList = List[String]()
        val caught1 = intercept[AssertionError] {
          emptyList should not { be (Nil) }
        }
        assert(caught1.getMessage === "List() was List()")

        /* val caught2 = intercept[AssertionError] {
          emptyList shouldNot be (Nil)
        }
        assert(caught2.getMessage === "List() was List()") */

        val caught3 = intercept[AssertionError] {
          emptyList should not { equal (Nil) }
        }
        assert(caught3.getMessage === "List() equaled List()")

        /* val caught4 = intercept[AssertionError] {
          emptyList shouldNot equal (Nil)
        }
        assert(caught4.getMessage === "List() equaled List()") */
      }

      it("should work when used in a logical expression") {
        val emptyList = List[Int]()
        emptyList should { be (Nil) and equal (Nil) }
        emptyList should { equal (Nil) and be (Nil) } // Nada, and nada is nada
      }
    }

    describe("(for None)") {

        /* I think I should have tests for options somewhere
        val option = Some(1)
        option should equal (Some(1))
      val option = Some(1)
      option should not { equal (Some(2)) }

         */
      it("should do nothing when a None option is compared to None") {
        val option: Option[String] = None
        option should be (None)
        option should equal (None)
      }

      it("should throw an assertion error when a Some is compared to None") {
        val someString = Some("Helloooooo")
        val caught1 = intercept[AssertionError] {
          someString should be (None)
        }
        assert(caught1.getMessage === "Some(Helloooooo) was not None")
        val caught2 = intercept[AssertionError] {
          someString should equal (None)
        }
        assert(caught2.getMessage === "Some(Helloooooo) did not equal None")
      }

      it("should do nothing when Some is compared to not None") {
        val someString = Some("Helloooooo")
        someString should not { be (None) }
        // someString shouldNot be (None)
        someString should not { equal (None) }
        // someString shouldNot equal (None)
      }

      it("should throw an assertion error when None compared to not None") {
        val none = None
        val caught1 = intercept[AssertionError] {
          none should not { be (None) }
        }
        assert(caught1.getMessage === "None was None")

        /* val caught2 = intercept[AssertionError] {
          none shouldNot be (None)
        }
        assert(caught2.getMessage === "None was None")  */

        val caught3 = intercept[AssertionError] {
          none should not { equal (None) }
        }
        assert(caught3.getMessage === "None equaled None")

        /* val caught4 = intercept[AssertionError] {
          none shouldNot equal (None)
        }
        assert(caught4.getMessage === "None equaled None") */

        val noString: Option[String] = None
        val caught5 = intercept[AssertionError] {
          noString should not { be (None) }
        }
        assert(caught5.getMessage === "None was None")

        /* val caught6 = intercept[AssertionError] {
          noString shouldNot be (None)
        }
        assert(caught6.getMessage === "None was None") */

        val caught7 = intercept[AssertionError] {
          noString should not { equal (None) }
        }
        assert(caught7.getMessage === "None equaled None")

        /* val caught8 = intercept[AssertionError] {
          noString shouldNot equal (None)
        }
        assert(caught8.getMessage === "None equaled None") */
      }

      it("should work when used in a logical expression") {
        val none = None
        none should { be (None) and equal (None) }
        none should { equal (None) and be (None) }
        val noString: Option[String] = None
        noString should { be (None) and equal (None) }
        noString should { equal (None) and be (None) }
      }
    }

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

    describe("(for Any)") {
      it("should do nothing when equal") {
        1 should be (1)
        val option = Some(1)
        option should be (Some(1)) 
      }

      it("should throw an assertion error when not equal") {
        val caught = intercept[AssertionError] {
          1 should be (2)
        }
        assert(caught.getMessage === "1 was not 2")
      }

      it("should do nothing when not equal and used with shouldNot") {
        // 1 shouldNot be (2)
        1 should not { be (2) }
        val option = Some(1)
        // option shouldNot be (Some(2))
        option should not { be (Some(2)) }
      }

      it("should throw an assertion error when equal but used with shouldNot") {
        val caught = intercept[AssertionError] {
          // 1 shouldNot be (1)
          1 should not { be (1) }
        }
        assert(caught.getMessage === "1 was 1")
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
  describe("The not matcher") {
    it("should do nothing when not true") {
      1 should not { equal (2) }
    }
    it("should throw an assertion error when true") {
      val caught = intercept[AssertionError] {
        1 should not { equal (1) }
      }
      assert(caught.getMessage === "1 equaled 1")
    }
    it("should work at the beginning of an and expression") {
      val string = "Hello, world!"
      string should { not { have length 7 } and { startWith substring "Hello" } }
    }
  }

  describe("The shouldNot method") {
    it("should do nothing when not true") {
      // 1 shouldNot equal (2)
      1 should not { equal (2) }
    }
    it("should throw an assertion error when true") {
      val caught = intercept[AssertionError] {
        // 1 shouldNot equal (1)
        1 should not { equal (1) }
      }
      assert(caught.getMessage === "1 equaled 1")
    }
  }

  describe("The endWith matcher") {
    it("should do nothing when true") {
      "Hello, world" should endWith substring ("world")
      // "Hello, world" shouldNot endWith substring ("Hello")
      "Hello, world" should not { endWith substring ("Hello") }
      "Hello, world" should endWith regex ("wo.ld")
      // "Hello, world" shouldNot endWith regex ("Hel*o")
      "Hello, world" should not { endWith regex ("Hel*o") }
      "Hello, world" should endWith regex ("wo.ld".r)
      // "Hello, world" shouldNot endWith regex ("Hel*o".r)
      "Hello, world" should not { endWith regex ("Hel*o".r) }
    }
    it("should throw an assertion error when not true") {
      val caught1 = intercept[AssertionError] {
        "Hello, world" should endWith substring ("planet")
      }
      assert(caught1.getMessage.indexOf("did not end with") != -1)
      val caught2 = intercept[AssertionError] {
        // "Hello, world" shouldNot endWith substring ("world")
        "Hello, world" should not { endWith substring ("world") }
      }
      assert(caught2.getMessage.indexOf("ended with") != -1)
      val caught3 = intercept[AssertionError] {
        "Hello, world" should endWith regex ("pla.et")
      }
      //assert(caught3.getMessage.indexOf("did not end with a match for the regular expression") != -1)
      val caught4 = intercept[AssertionError] {
        // "Hello, world" shouldNot endWith regex ("wo.ld")
        "Hello, world" should not { endWith regex ("wo.ld") }
      }
      //assert(caught4.getMessage.indexOf("ended with a match for the regular expression") != -1)
      val caught5 = intercept[AssertionError] {
        "Hello, world" should endWith regex ("pla.et")
      }
      //assert(caught5.getMessage.indexOf("did not end with a match for the regular expression") != -1)
      intercept[AssertionError] {
        // "Hello, world" shouldNot endWith regex ("wo.ld")
        "Hello, world" should not { endWith regex ("wo.ld") }
      }
      //assert(caught6.getMessage.indexOf("ended with a match for the regular expression") != -1)
    }
    it("should work inside an and clause") {

      "Hello, world" should { endWith substring "world" and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and (endWith substring "world") }
      "Hello, world" should { endWith regex "wo.ld" and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and (endWith regex "wo.ld") }
      "Hello, world" should { endWith regex "wo.ld".r and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and (endWith regex "wo.ld".r) }

      /* "Hello, world" shouldNot { endWith substring "planet" and equal ("Hello, world") }
      "Hello, world" shouldNot { equal ("Hello, world") and (endWith substring "planet") }
      "Hello, world" shouldNot { endWith regex "wo.l" and equal ("Hello, world") }
      "Hello, world" shouldNot { equal ("Hello, world") and (endWith regex "wo.l") }
      "Hello, world" shouldNot { endWith regex "wo.l".r and equal ("Hello, world") }
      "Hello, world" shouldNot { equal ("Hello, world") and (endWith regex "wo.l".r) } */

      "Hello, world" should not { endWith substring "planet" and equal ("Hello, world") }
      "Hello, world" should not { equal ("Hello, world") and (endWith substring "planet") }
      "Hello, world" should not { endWith regex "wo.l" and equal ("Hello, world") }
      "Hello, world" should not { equal ("Hello, world") and (endWith regex "wo.l") }
      "Hello, world" should not { endWith regex "wo.l".r and equal ("Hello, world") }
      "Hello, world" should not { equal ("Hello, world") and (endWith regex "wo.l".r) }
    }
  }

  describe("The startWith matcher") {
    it("should do nothing when true") {
      "Hello, world" should startWith substring ("Hello")
      // "Hello, world" shouldNot startWith substring ("Goodbye")
      "Hello, world" should not { startWith substring ("Goodbye") }
      "Hello, world" should startWith regex ("Hel*o")
      // "Hello, world" shouldNot startWith regex ("Yel*o")
      "Hello, world" should not { startWith regex ("Yel*o") }
      "Hello, world" should startWith regex ("Hel*o".r)
      // "Hello, world" shouldNot startWith regex ("Yel*o".r)
      "Hello, world" should not { startWith regex ("Yel*o".r) }
    }
    it("should throw an assertion error when not true") {
      val caught1 = intercept[AssertionError] {
        "Hello, world" should startWith substring ("Greetings")
      }
      assert(caught1.getMessage.indexOf("did not start with") != -1)
      val caught2 = intercept[AssertionError] {
        // "Hello, world" shouldNot startWith substring ("Hello")
        "Hello, world" should not { startWith substring ("Hello") }
      }
      assert(caught2.getMessage.indexOf("started with") != -1)
      val caught3 = intercept[AssertionError] {
        "Hello, world" should startWith regex ("Gre*tings")
      }
      // assert(caught3.getMessage.indexOf("did not start with a match for the regular expression") != -1)
      val caught4 = intercept[AssertionError] {
        // "Hello, world" shouldNot startWith regex ("Hel*o")
        "Hello, world" should not { startWith regex ("Hel*o") }
      }
      // assert(caught4.getMessage.indexOf("started with a match for the regular expression") != -1)
      val caught5 = intercept[AssertionError] {
        "Hello, world" should startWith regex ("Gre*tings".r)
      }
      // assert(caught5.getMessage.indexOf("did not start with a match for the regular expression") != -1)
      intercept[AssertionError] {
        // "Hello, world" shouldNot startWith regex ("Hel*o".r)
        "Hello, world" should not { startWith regex ("Hel*o".r) }
      }
      // assert(caught6.getMessage.indexOf("started with a match for the regular expression") != -1)
    }
    it("should work inside an and clause") {

      "Hello, world" should { startWith substring ("Hello") and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and (startWith substring ("Hello")) }
      "Hello, world" should { startWith regex ("Hel*o") and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and (startWith regex ("Hel*o")) }
      "Hello, world" should { startWith regex ("Hel*o".r) and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and (startWith regex ("Hel*o".r)) }

      /*"Hello, world" shouldNot { startWith substring ("Yello") and equal ("Hello, world") }
      "Hello, world" shouldNot { equal ("Hello, world") and (startWith substring ("Yello")) }
      "Hello, world" shouldNot { startWith regex ("Yel*o") and equal ("Hello, world") }
      "Hello, world" shouldNot { equal ("Hello, world") and (startWith regex ("Yel*o")) }
      "Hello, world" shouldNot { startWith regex ("Yel*o".r) and equal ("Hello, world") }
      "Hello, world" shouldNot { equal ("Hello, world") and (startWith regex ("Yel*o".r)) } */

      "Hello, world" should not { startWith substring ("Yello") and equal ("Hello, world") }
      "Hello, world" should not { equal ("Hello, world") and (startWith substring ("Yello")) }
      "Hello, world" should not { startWith regex ("Yel*o") and equal ("Hello, world") }
      "Hello, world" should not { equal ("Hello, world") and (startWith regex ("Yel*o")) }
      "Hello, world" should not { startWith regex ("Yel*o".r) and equal ("Hello, world") }
      "Hello, world" should not { equal ("Hello, world") and (startWith regex ("Yel*o".r)) }
    }
  }

  describe("The and matcher") {

    it("should do nothing when both operands are true") {
      1 should { equal (1) and equal (2 - 1) }
    }

    it("should throw AssertionError when first operands is false") {
      val caught = intercept[AssertionError] {
        1 should (equal (2) and equal (1))
      }
      caught.getMessage should equal ("1 did not equal 2") // because and short circuits
    }

    it("should throw AssertionError when second operand is false") {
      val caught = intercept[AssertionError] {
        1 should (equal (1) and equal (2))
      }
      caught.getMessage should equal ("1 equaled 1, but 1 did not equal 2")
    }

    it("should execute the right matcher creation function when the left operand is false") {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      val caught = intercept[AssertionError] {
        // This should fail, but without applying the matcher returned by mockMatcher
        1 should { equal (2) and mockMatcher }
      }
      called should be (true)
      assert(caught.getMessage === "1 did not equal 2")
    }

    it("should execute the right matcher creation function when the left operand is true") {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      1 should { equal (1) and mockMatcher }
      called should be (true)
    }
    it("should give good failure messages when used with not") {
      val caught1 = intercept[AssertionError] {
        1 should (not { equal (1) } and equal (1))
      }
      caught1.getMessage should equal ("1 equaled 1") // because and short circuits
      val caught2 = intercept[AssertionError] {
        1 should (equal (1) and not { equal (1) })
      }
      caught2.getMessage should equal ("1 equaled 1, but 1 equaled 1")
      val caught3 = intercept[AssertionError] {
        1 should (not { equal (2) } and not { equal (1) })
      }
      caught3.getMessage should equal ("1 did not equal 2, but 1 equaled 1")
    }
  }

  describe("The or matcher") {

    it("should do nothing when both operands are true") {
      1 should { equal (1) or equal (2 - 1) }
    }

    it("should throw AssertionError when both operands are false") {
      val caught = intercept[AssertionError] {
        1 should (equal (2) or equal (3))
      }
      caught.getMessage should equal ("1 did not equal 2, and 1 did not equal 3") // because and short circuits
    }

    it("should do nothing when first operand is true and second operand is false") {
      1 should (equal (1) or equal (2))
    }

    it("should do nothing when first operand is false and second operand is true") {
      1 should (equal (2) or equal (1))
    }

    it("should execute the right matcher creation function when the left operand is true") {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      // This should succeed, but without applying the matcher returned by mockMatcher
      1 should { equal (1) or mockMatcher }
      called should be (true)
    }

    it("should execute the right matcher creation function when the left operand is false") {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      1 should { equal (2) or mockMatcher }
      called should be (true)
    }

    it("should give good failure messages when used with not") {
      val caught1 = intercept[AssertionError] {
        1 should (not { equal (1) } or equal (2))
      }
      caught1.getMessage should equal ("1 equaled 1, and 1 did not equal 2")
      val caught2 = intercept[AssertionError] {
        1 should (equal (2) or not { equal (1) })
      }
      caught2.getMessage should equal ("1 did not equal 2, and 1 equaled 1")
      val caught3 = intercept[AssertionError] {
        1 should (not { equal (1) } or not { equal (1) })
      }
      caught3.getMessage should equal ("1 equaled 1, and 1 equaled 1")
    }
  }

  // I dropped the tests for andNot and orNot, and replaced them with tests for and not { ... } and or not { ... }
  describe("The and not combination") {

    it("should do nothing when left operands is true and right false") {
      1 should { equal (1) and not { equal (2) }}
    }

    it("should throw AssertionError when first operands is false") {
      val caught = intercept[AssertionError] {
        1 should (equal (2) and not (equal (2)))
      }
      caught.getMessage should equal ("1 did not equal 2") // because and short circuits
    }

    it("should throw AssertionError when second operand is true") {
      val caught = intercept[AssertionError] {
        1 should (equal (1) and not (equal (1)))
      }
      caught.getMessage should equal ("1 equaled 1, but 1 equaled 1")
    }

    it("should execute the right matcher creation function when the left operand is false") {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      val caught = intercept[AssertionError] {
        // This should fail, but without applying the matcher returned by mockMatcher
        1 should { equal (2) and not { mockMatcher }}
      }
      called should be (true)
      assert(caught.getMessage === "1 did not equal 2")
    }

    it("should execute the right matcher creation function when the left operand is true") {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(false, "", "") } }
      1 should { equal (1) and not { mockMatcher }}
      called should be (true)
    }

    it("should give good failure messages when used with not") {
      val caught1 = intercept[AssertionError] {
        1 should (not { equal (1) } and not (equal (2)))
      }
      caught1.getMessage should equal ("1 equaled 1") // because andNot short circuits
      val caught2 = intercept[AssertionError] {
        1 should (equal (1) and not { equal (1) })
      }
      caught2.getMessage should equal ("1 equaled 1, but 1 equaled 1")
    }
  }

  describe("The or not combination") {

    it("should do nothing when left operand is true and right false") {
      1 should { equal (1) or not { equal (2) }}
    }

    it("should do nothing when when both operands are false") {
      1 should (equal (2) or not (equal (2)))
    }

    it("should do nothing when left operand is true and right true") {
      1 should { equal (1) or not { equal (1) }}
    }

    it("should throw AssertionError when first operand is false and second operand is true") {
      val caught = intercept[AssertionError] {
        1 should (equal (2) or not (equal (1)))
      }
      caught.getMessage should equal ("1 did not equal 2, and 1 equaled 1")
    }

    it("should execute the right matcher creation function when the left operand is true") {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      // This should succeed, but without applying the matcher returned by mockMatcher
      1 should { equal (1) or not { mockMatcher }}
      called should be (true)
    }

    it("should execute the right matcher creation function when the left operand is false") {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(false, "", "") } }
      1 should { equal (2) or not { mockMatcher }}
      called should be (true)
    }

    it("should give good failure messages when used with not") {
      val caught1 = intercept[AssertionError] {
        1 should (not { equal (1) } or not (equal (1)))
      }
      caught1.getMessage should equal ("1 equaled 1, and 1 equaled 1")
      val caught2 = intercept[AssertionError] {
        1 should (equal (2) or not { not { equal (2) }}) // Don't do this at home
      }
      caught2.getMessage should equal ("1 did not equal 2, and 1 did not equal 2")
    }
  }

  describe("The have word") {

    it("should work with map and key, right after a 'should'") {
      val map = Map(1 -> "Howdy")
      map should contain key (1)
      map should contain key (1)
      map should equal { Map(1 -> "Howdy") }
      val otherMap = Map("Howdy" -> 1)
      otherMap should contain key ("Howdy")
      otherMap should equal { Map("Howdy" -> 1) }
      import scala.collection.immutable.TreeMap
      val treeMap = TreeMap(1 -> "hi", 2 -> "howdy")
      treeMap should contain key (1)
    }

    it("should work with map and key, in a logical expression") {
      val map = Map(1 -> "Howdy")
      // The compiler infer the type of the value to be Nothing if I say: map should { contain key 1 and equal (Map(1 -> "Howdy")) }
      // map should { have.key[Int, String](1) and equal (Map(1 -> "Howdy")) }
      map should { contain key (1) and equal (Map(1 -> "Howdy")) }
      val otherMap = Map("Howdy" -> 1)
      // otherMap should { have.key[String, Int]("Howdy") and equal (Map("Howdy" -> 1)) }
      otherMap should { contain key ("Howdy") and equal (Map("Howdy" -> 1)) }
    }

    it("should work with map and key, right after a 'shouldNot'") {
      val map = Map(1 -> "Howdy")
      // map shouldNot contain key (2)
      map should not { contain key (2) }
    }

    it("should work with map and value, right after a 'should'") {
      val map = Map(1 -> "Howdy")
      map should contain value ("Howdy")
      map should contain value ("Howdy")
      map should equal { Map(1 -> "Howdy") }
      val otherMap = Map("Howdy" -> 1)
      otherMap should contain value (1)
      otherMap should equal { Map("Howdy" -> 1) }
    }

    it("should work with map and value, in a logical expression") {
      val map = Map(1 -> "Howdy")
      map should { equal (Map(1 -> "Howdy")) and (contain value "Howdy") }
      val otherMap = Map("Howdy" -> 1)
      otherMap should { contain value (1) and equal (Map("Howdy" -> 1)) }
    }

    it("should work with map and value, right after a 'shouldNot'") {
      val map = Map(1 -> "Howdy")
      // map shouldNot contain value ("Doody")
      map should not { contain value ("Doody") }
    }

    it("should work with collection and size, in an and expression.") {
      val list = List(1, 2, 3)
      list should { have size (3) and equal (List(1, 2, 3)) }
    }

    it("should work with collection and size, right after a 'should'") {

      val map = Map(1 -> "Howdy")
      map should have size (1)
      val caught1 = intercept[AssertionError] {
        map should have size (5)
      }
      assert(caught1.getMessage.indexOf("did not have size") != -1)

      val list = List(1, 2, 3, 4, 5)
      list should have size (5)
      val caught2 = intercept[AssertionError] {
        list should have size (6)
      }
      assert(caught2.getMessage.indexOf("did not have size") != -1)

      val set = Set(1.0, 2.0, 3.0)
      set should have size (3)
      val caught3 = intercept[AssertionError] {
        set should have size (0)
      }
      assert(caught3.getMessage.indexOf("did not have size") != -1)

      val array = Array[String]()
      array should have size 0
      val caught4 = intercept[AssertionError] {
        array should have size 2
      }
      assert(caught4.getMessage.indexOf("did not have size") != -1)
    }

    it("should work with collection and size, right after a 'shouldNot'") {

      val map = Map(1 -> "Howdy")
      // map shouldNot have size (2)
      map should not { have size (2) }
      val caught1 = intercept[AssertionError] {
        // map shouldNot have size (1)
        map should not { have size (1) }
      }
      assert(caught1.getMessage.indexOf("had size") != -1, caught1.getMessage)

      val list = List(1, 2, 3, 4, 5)
      //list shouldNot have size (6)
      list should not { have size (6) }
      val caught2 = intercept[AssertionError] {
        // list shouldNot have size (5)
        list should not { have size (5) }
      }
      assert(caught2.getMessage.indexOf("had size") != -1)

      val set = Set(1.0, 2.0, 3.0)
      // set shouldNot have size (0)
      set should not { have size (0) }
      val caught3 = intercept[AssertionError] {
        // set shouldNot have size (3)
        set should not { have size (3) }
      }
      assert(caught3.getMessage.indexOf("had size") != -1)

      val array = Array[String]()
      // array shouldNot have size (2)
      array should not { have size (2) }
      val caught4 = intercept[AssertionError] {
        // array shouldNot have size (0)
        array should not { have size (0) }
      }
      assert(caught4.getMessage.indexOf("had size") != -1)
    }
  }

  describe("The contain word") {
 
    it("should work with a set, list, array, and map right after a 'should'") {

      val set = Set(1, 2, 3)
      set should contain element (2)
      val caught1 = intercept[AssertionError] {
        set should contain element (5)
      }
      assert(caught1.getMessage.indexOf("did not contain element") != -1)

      set should { contain element (2) and equal (Set(1, 2, 3)) }
      val caught1b = intercept[AssertionError] {
        set should { contain element (5) and equal(Set(1, 2, 3)) }
      }
      assert(caught1b.getMessage.indexOf("did not contain element") != -1)

      val list = List("one", "two", "three")
      list should contain element ("two")
      val caught2 = intercept[AssertionError] {
        list should contain element ("five")
      }
      assert(caught2.getMessage.indexOf("did not contain element") != -1)

      val array = Array("one", "two", "three")
      array should contain element ("one")
      val caught3 = intercept[AssertionError] {
        array should contain element ("five")
      }
      assert(caught3.getMessage.indexOf("did not contain element") != -1)

      val map = Map(1 -> "one", 2 -> "two", 3 -> "three")
      val tuple2: Tuple2[Int, String] = 1 -> "one"
      map should contain element (tuple2)
      val caught4 = intercept[AssertionError] {
        map should contain element (1 -> "won")
      }
      assert(caught4.getMessage.indexOf("did not contain element") != -1)
    }

    it("should work with a set, list, array, and map right after a 'shouldNot'") {

      val set = Set(1, 2, 3)
      // set shouldNot contain element (5)
      set should not { contain element (5) }
      val caught1 = intercept[AssertionError] {
        // set shouldNot contain element (2)
        set should not { contain element (2) }
      }
      assert(caught1.getMessage.indexOf("contained element") != -1)

      val list = List("one", "two", "three")
      // list shouldNot contain element ("five")
      list should not { contain element ("five") }
      val caught2 = intercept[AssertionError] {
        // list shouldNot contain element ("two")
        list should not { contain element ("two") }
      }
      assert(caught2.getMessage.indexOf("contained element") != -1)

      val array = Array("one", "two", "three")
      // array shouldNot contain element ("five")
      array should not { contain element ("five") }
      val caught3 = intercept[AssertionError] {
        // array shouldNot contain element ("one")
        array should not { contain element ("one") }
      }
      assert(caught3.getMessage.indexOf("contained element") != -1)

      val map = Map(1 -> "one", 2 -> "two", 3 -> "three")
      val tuple2: Tuple2[Int, String] = 1 -> "won"
      // map shouldNot contain element (tuple2)
      map should not { contain element (tuple2) }
      val caught4 = intercept[AssertionError] {
        // map shouldNot contain element (1 -> "one")
        map should not { contain element (1 -> "one") }
      }
      assert(caught4.getMessage.indexOf("contained element") != -1)
    }

  }

  describe("Any object with a getLength method") {
    it("should be usable with 'should have length N' syntax") {
      class PurpleElephant {
        def getLength(): Int = 7
      }
      val bob = new PurpleElephant
      bob should have length 7
    }
  }

  describe("Any object with a length field") {
    it("should be usable with 'should have length N' syntax") {
      class PurpleElephant {
        val length: Int = 7
      }
      val bob = new PurpleElephant
      bob should have length 7
      class GreenElephant {
        def length: Int = 7
      }
      val john = new GreenElephant
      john should have length 7
    }
  }

  describe("Any object with a length method") {
    it("should be usable with 'should have length N' syntax") {
      class PurpleElephant {
        def length(): Int = 7
      }
      val bob = new PurpleElephant
      bob should have length 7
    }
  }

  describe("The be theSameInstanceAs syntax") {

    val string = "Hi"
    val obj: AnyRef = string
    val otherString = new String("Hi")

    it("should do nothing if the two objects are the same") {
      string should be theSameInstanceAs (string)
      obj should be theSameInstanceAs (string)
      string should be theSameInstanceAs (obj)
      // otherString shouldNot be theSameInstanceAs (string)
      otherString should not { be theSameInstanceAs (string) }
    }

    it("should throw AssertionError if the two objects are not the same") {
      val caught1 = intercept[AssertionError] {
        // string shouldNot be theSameInstanceAs (string)
        string should not { be theSameInstanceAs (string) }
      }
      val caught2 = intercept[AssertionError] {
        // obj shouldNot be theSameInstanceAs (string)
        obj should not { be theSameInstanceAs (string) }
      }
      val caught3 = intercept[AssertionError] {
        // string shouldNot be theSameInstanceAs (obj)
        string should not { be theSameInstanceAs (obj) }
      }
      val caught4 = intercept[AssertionError] {
        otherString should be theSameInstanceAs (string)
      }
      assert(true) // TODO: test the failure message
    }
  }

  describe("The include substring syntax") {

    val decimal = """(-)?(\d+)(\.\d*)?"""
    val decimalRegex = """(-)?(\d+)(\.\d*)?""".r

    it("should do nothing if the string includes the expected substring") {
      val string = "Four score and seven years ago,..."
      string should include substring ("seven")
      string should include substring ("Four")
      string should include substring (",...")
      // string shouldNot include substring ("on this continent")
      string should not { include substring ("on this continent") }
      "4 score and seven years ago" should include regex (decimal)
      "Four score and 7 years ago" should include regex (decimal)
      "4.0 score and seven years ago" should include regex (decimal)
      "Four score and 7.0 years ago" should include regex (decimal)
      "Four score and 7.0" should include regex (decimal)
      // string shouldNot include regex (decimal)
      string should not { include regex (decimal) }
    }

    it("should throw AssertionError if the string does not include the expected substring") {
      val string = "Four score and seven years ago,..."
      val caught1 = intercept[AssertionError] {
        // string shouldNot include substring ("seven")
        string should not { include substring ("seven") }
      }
      assert(caught1.getMessage === "\"Four score and seven years ago,...\" included substring \"seven\"")
      val caught2 = intercept[AssertionError] {
        // string shouldNot include substring ("Four")
        string should not { include substring ("Four") }
      }
      assert(caught2.getMessage === "\"Four score and seven years ago,...\" included substring \"Four\"")
      val caught3 = intercept[AssertionError] {
        // string shouldNot include substring (",...")
        string should not { include substring (",...") }
      }
      assert(caught3.getMessage === "\"Four score and seven years ago,...\" included substring \",...\"")
      val caught4 = intercept[AssertionError] {
        string should include substring ("on this continent")
      }
      assert(caught4.getMessage === "\"Four score and seven years ago,...\" did not include substring \"on this continent\"")
    }
  }

  describe("The should be >/>=/</<= syntax") {
    it("should do nothing if the specified relation is true") {
      val one = 1
      one should be < (7)
      one should be > (0)
      one should be <= (7)
      one should be >= (0)
      one should be <= (1)
      one should be >= (1)
      /* one shouldNot be < (0)
      one shouldNot be > (9)
      one shouldNot be <= (-4)
      one shouldNot be >= (21) */

      one should not { be < (0) }
      one should not { be > (9) }
      one should not { be <= (-4) }
      one should not { be >= (21) }
    }
    it("should throw AssertionError if the specified relation is not true") {
      val one = 1
      val caught1 = intercept[AssertionError] {
        // one shouldNot be < (7)
        one should not { be < (7) }
      }
      assert(caught1.getMessage === "1 was less than 7")

      val caught2 = intercept[AssertionError] {
        // one shouldNot be > (0)
        one should not { be > (0) }
      }
      assert(caught2.getMessage === "1 was greater than 0")

      val caught3 = intercept[AssertionError] {
        // one shouldNot be <= (7)
        one should not { be <= (7) }
      }
      assert(caught3.getMessage === "1 was less than or equal to 7")

      val caught4 = intercept[AssertionError] {
        // one shouldNot be >= (0)
        one should not { be >= (0) }
      }
      assert(caught4.getMessage === "1 was greater than or equal to 0")

      val caught5 = intercept[AssertionError] {
        // one shouldNot be <= (1)
        one should not { be <= (1) }
      }
      assert(caught5.getMessage === "1 was less than or equal to 1")

      val caught6 = intercept[AssertionError] {
        // one shouldNot be >= (1)
        one should not { be >= (1) }
      }
      assert(caught6.getMessage === "1 was greater than or equal to 1")

      val caught7 = intercept[AssertionError] {
        one should be < (0)
      }
      assert(caught7.getMessage === "1 was not less than 0")

      val caught8 = intercept[AssertionError] {
        one should be > (9)
      }
      assert(caught8.getMessage === "1 was not greater than 9")

      val caught9 = intercept[AssertionError] {
        one should be <= (-4)
      }
      assert(caught9.getMessage === "1 was not less than or equal to -4")

      val caught10 = intercept[AssertionError] {
        one should be >= (21)
      }
      assert(caught10.getMessage === "1 was not greater than or equal to 21")
    }
  }

  describe("The floating point numbers when compared with equals") {
    it("should do nothing if the floating point number is exactly equal to the specified value") {
      val sevenDotOh = 7.0
      // sevenDotOh should be (7.0 exactly)
      sevenDotOh should be (7.0)
      sevenDotOh should equal (7.0)
      // sevenDotOh shouldNot be (7.0001 exactly)
      // sevenDotOh shouldNot be (7.0001)
      sevenDotOh should not { be (7.0001) }

      val sixDotOh: Float = 6.0f
      // sixDotOh should be (6.0 exactly)
      sixDotOh should be (6.0)
      sixDotOh should equal (6.0)
      // sixDotOh shouldNot be (6.0001 exactly)
      // sixDotOh shouldNot be (6.0001)
      sixDotOh should not { be (6.0001) }
    }

    it("should throw AssertionError if the floating point number is not exactly equal to the specified value") {
      val sevenDotOh = 7.0001
      val caught1 = intercept[AssertionError] {
        sevenDotOh should be (7.0)
        // sevenDotOh should be (7.0 exactly)
      }
      assert(caught1.getMessage === "7.0001 was not 7.0")

      val caught2 = intercept[AssertionError] {
        sevenDotOh should equal (7.0)
      }
      assert(caught2.getMessage === "7.0001 did not equal 7.0")

      val caught3 = intercept[AssertionError] {
        // sevenDotOh shouldNot be (7.0001 exactly)
        // sevenDotOh shouldNot be (7.0001)
        sevenDotOh should not { be (7.0001) }
      }
      assert(caught3.getMessage === "7.0001 was 7.0001")

      val sixDotOh: Float = 6.0001f
      val caught4 = intercept[AssertionError] {
        // sixDotOh should be (6.0f exactly)
        sixDotOh should be (6.0f)
      }
      assert(caught4.getMessage === "6.0001 was not 6.0")

      val caught5 = intercept[AssertionError] {
        sixDotOh should equal (6.0f)
      }
      assert(caught5.getMessage === "6.0001 did not equal 6.0")

      val caught6 = intercept[AssertionError] {
        // sixDotOh shouldNot be (6.0001f exactly)
        // sixDotOh shouldNot be (6.0001f)
        sixDotOh should not { be (6.0001f) }
      }
      assert(caught6.getMessage === "6.0001 was 6.0001")
    }
  }

  describe("The floating point 'plusOrMinus' operator") {
    it("should do nothing if the floating point number is within the specified range") {
      val sevenDotOh = 7.0
      sevenDotOh should be (7.1 plusOrMinus 0.2)
      sevenDotOh should be (6.9 plusOrMinus 0.2)
      /* sevenDotOh shouldNot be (7.5 plusOrMinus 0.2)
      sevenDotOh shouldNot be (6.5 plusOrMinus 0.2) */
      sevenDotOh should not { be (7.5 plusOrMinus 0.2) }
      sevenDotOh should not { be (6.5 plusOrMinus 0.2) }
      val minusSevenDotOh = -7.0
      minusSevenDotOh should be (-7.1 plusOrMinus 0.2)
      minusSevenDotOh should be (-6.9 plusOrMinus 0.2)
      /* minusSevenDotOh shouldNot be (-7.5 plusOrMinus 0.2)
      minusSevenDotOh shouldNot be (-6.5 plusOrMinus 0.2) */
      minusSevenDotOh should not { be (-7.5 plusOrMinus 0.2) }
      minusSevenDotOh should not { be (-6.5 plusOrMinus 0.2) }
    }

    it("should throw AssertionError if the floating point number is not within the specified range") {
      val sevenDotOh = 7.0
      val caught1 = intercept[AssertionError] {
        // sevenDotOh shouldNot be (7.1 plusOrMinus 0.2)
        sevenDotOh should not { be (7.1 plusOrMinus 0.2) }
      }
      assert(caught1.getMessage === "7.0 was 7.1 plus or minus 0.2")

      val caught2 = intercept[AssertionError] {
        // sevenDotOh shouldNot be (6.9 plusOrMinus 0.2)
        sevenDotOh should not { be (6.9 plusOrMinus 0.2) }
      }
      assert(caught2.getMessage === "7.0 was 6.9 plus or minus 0.2")

      val caught3 = intercept[AssertionError] {
        sevenDotOh should be (7.5 plusOrMinus 0.2)
      }
      assert(caught3.getMessage === "7.0 was not 7.5 plus or minus 0.2")

      val caught4 = intercept[AssertionError] {
        sevenDotOh should be (6.5 plusOrMinus 0.2)
      }
      assert(caught4.getMessage === "7.0 was not 6.5 plus or minus 0.2")

      val minusSevenDotOh = -7.0
      val caught5 = intercept[AssertionError] {
        // minusSevenDotOh shouldNot be (-7.1 plusOrMinus 0.2)
        minusSevenDotOh should not { be (-7.1 plusOrMinus 0.2) }
      }
      assert(caught5.getMessage === "-7.0 was -7.1 plus or minus 0.2")

      val caught6 = intercept[AssertionError] {
        // minusSevenDotOh shouldNot be (-6.9 plusOrMinus 0.2)
        minusSevenDotOh should not { be (-6.9 plusOrMinus 0.2) }
      }
      assert(caught6.getMessage === "-7.0 was -6.9 plus or minus 0.2")

      val caught7 = intercept[AssertionError] {
        minusSevenDotOh should be (-7.5 plusOrMinus 0.2)
      }
      assert(caught7.getMessage === "-7.0 was not -7.5 plus or minus 0.2")

      val caught8 = intercept[AssertionError] {
        minusSevenDotOh should be (-6.5 plusOrMinus 0.2)
      }
      assert(caught8.getMessage === "-7.0 was not -6.5 plus or minus 0.2")
    }
  }

  describe("The fullyMatch regex syntax") {

    val decimal = """(-)?(\d+)(\.\d*)?"""
    val decimalRegex = """(-)?(\d+)(\.\d*)?""".r

    it("should do nothing if the string matches the regular expression specified as a string") {
      "1.7" should fullyMatch regex ("1.7")
      "1.7" should fullyMatch regex (decimal)
      "-1.8" should fullyMatch regex (decimal)
      "8" should fullyMatch regex (decimal)
      "1." should fullyMatch regex (decimal)
      /* "eight" shouldNot fullyMatch regex (decimal)
      "1.eight" shouldNot fullyMatch regex (decimal)
      "one.8" shouldNot fullyMatch regex (decimal)
      "1.8-" shouldNot fullyMatch regex (decimal)
      "1.7" should { fullyMatch regex (decimal) and equal ("1.7") }
      "1.7++" shouldNot { fullyMatch regex (decimal) and equal ("1.7") } */
      "eight" should not { fullyMatch regex (decimal) }
      "1.eight" should not { fullyMatch regex (decimal) }
      "one.8" should not { fullyMatch regex (decimal) }
      "1.8-" should not { fullyMatch regex (decimal) }
      "1.7" should { fullyMatch regex (decimal) and equal ("1.7") }
      "1.7++" should not { fullyMatch regex (decimal) and equal ("1.7") }
    }

    it("should do nothing if the string matches the regular expression specified as a Regex") {
      "1.7" should fullyMatch regex (decimalRegex)
      "-1.8" should fullyMatch regex (decimalRegex)
      "8" should fullyMatch regex (decimalRegex)
      "1." should fullyMatch regex (decimalRegex)

      /* "eight" shouldNot fullyMatch regex (decimalRegex)
      "1.eight" shouldNot fullyMatch regex (decimalRegex)
      "one.8" shouldNot fullyMatch regex (decimalRegex)
      "1.8-" shouldNot fullyMatch regex (decimalRegex)
      "1.7" should { fullyMatch regex (decimalRegex) and equal ("1.7") }
      "1.7++" shouldNot { fullyMatch regex (decimalRegex) and equal ("1.7") } */

      "eight" should not { fullyMatch regex (decimalRegex) }
      "1.eight" should not { fullyMatch regex (decimalRegex) }
      "one.8" should not { fullyMatch regex (decimalRegex) }
      "1.8-" should not { fullyMatch regex (decimalRegex) }
      "1.7" should { fullyMatch regex (decimalRegex) and equal ("1.7") }
      "1.7++" should not { fullyMatch regex (decimalRegex) and equal ("1.7") }
    }

    it("should throw AssertionError if the string does not match the regular expression specified as a string") {
      val caught1 = intercept[AssertionError] {
        // "1.7" shouldNot fullyMatch regex ("1.7")
        "1.7" should not { fullyMatch regex ("1.7") }
      }
      assert(caught1.getMessage === "\"1.7\" fully matched the regular expression 1.7")

      val caught2 = intercept[AssertionError] {
        // "1.7" shouldNot fullyMatch regex (decimal)
        "1.7" should not { fullyMatch regex (decimal) }
      }
      assert(caught2.getMessage === "\"1.7\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught3 = intercept[AssertionError] {
        // "-1.8" shouldNot fullyMatch regex (decimal)
        "-1.8" should not { fullyMatch regex (decimal) }
      }
      assert(caught3.getMessage === "\"-1.8\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught4 = intercept[AssertionError] {
        // "8" shouldNot fullyMatch regex (decimal)
        "8" should not { fullyMatch regex (decimal) }
      }
      assert(caught4.getMessage === "\"8\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught5 = intercept[AssertionError] {
        // "1." shouldNot fullyMatch regex (decimal)
        "1." should not { fullyMatch regex (decimal) }
      }
      assert(caught5.getMessage === "\"1.\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught6 = intercept[AssertionError] {
        "eight" should fullyMatch regex (decimal)
      }
      assert(caught6.getMessage === "\"eight\" did not fully match the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught7 = intercept[AssertionError] {
        "1.eight" should fullyMatch regex (decimal)
      }
      assert(caught7.getMessage === "\"1.eight\" did not fully match the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught8 = intercept[AssertionError] {
        "one.8" should fullyMatch regex (decimal)
      }
      assert(caught8.getMessage === "\"one.8\" did not fully match the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught9 = intercept[AssertionError] {
        "1.8-" should fullyMatch regex (decimal)
      }
      assert(caught9.getMessage === "\"1.8-\" did not fully match the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught10 = intercept[AssertionError] {
        // "1.7" shouldNot { fullyMatch regex (decimal) and equal ("1.7") }
        "1.7" should not { fullyMatch regex (decimal) and equal ("1.7") }
      }
      assert(caught10.getMessage === "\"1.7\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?, and \"1.7\" equaled \"1.7\"")

      val caught11 = intercept[AssertionError] {
        "1.7++" should { fullyMatch regex (decimal) and equal ("1.7") }
      }
      assert(caught11.getMessage === "\"1.7++\" did not fully match the regular expression (-)?(\\d+)(\\.\\d*)?")
    }

    it("should throw AssertionError if the string does not match the regular expression specified as a Regex") {
      val caught2 = intercept[AssertionError] {
        // "1.7" shouldNot fullyMatch regex (decimalRegex)
        "1.7" should not { fullyMatch regex (decimalRegex) }
      }
      assert(caught2.getMessage === "\"1.7\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught3 = intercept[AssertionError] {
        // "-1.8" shouldNot fullyMatch regex (decimalRegex)
        "-1.8" should not { fullyMatch regex (decimalRegex) }
      }
      assert(caught3.getMessage === "\"-1.8\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught4 = intercept[AssertionError] {
        // "8" shouldNot fullyMatch regex (decimalRegex)
        "8" should not { fullyMatch regex (decimalRegex) }
      }
      assert(caught4.getMessage === "\"8\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught5 = intercept[AssertionError] {
        // "1." shouldNot fullyMatch regex (decimalRegex)
        "1." should not { fullyMatch regex (decimalRegex) }
      }
      assert(caught5.getMessage === "\"1.\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught6 = intercept[AssertionError] {
        "eight" should fullyMatch regex (decimalRegex)
      }
      assert(caught6.getMessage === "\"eight\" did not fully match the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught7 = intercept[AssertionError] {
        "1.eight" should fullyMatch regex (decimalRegex)
      }
      assert(caught7.getMessage === "\"1.eight\" did not fully match the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught8 = intercept[AssertionError] {
        "one.8" should fullyMatch regex (decimalRegex)
      }
      assert(caught8.getMessage === "\"one.8\" did not fully match the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught9 = intercept[AssertionError] {
        "1.8-" should fullyMatch regex (decimalRegex)
      }
      assert(caught9.getMessage === "\"1.8-\" did not fully match the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught10 = intercept[AssertionError] {
        // "1.7" shouldNot { fullyMatch regex (decimalRegex) and equal ("1.7") }
        "1.7" should not { fullyMatch regex (decimalRegex) and equal ("1.7") }
      }
      assert(caught10.getMessage === "\"1.7\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?, and \"1.7\" equaled \"1.7\"")

      val caught11 = intercept[AssertionError] {
        "1.7++" should { fullyMatch regex (decimalRegex) and equal ("1.7") }
      }
      assert(caught11.getMessage === "\"1.7++\" did not fully match the regular expression (-)?(\\d+)(\\.\\d*)?")
    }
  }
}

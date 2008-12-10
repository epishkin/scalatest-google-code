package org.scalatest

class MatcherSpec extends Spec with ShouldMatchers {

  "The equal matcher" -- {

    "should do nothing when equal" - {
      1 should equal (1)
      val option = Some(1)
      option should equal (Some(1)) 
    }

    "should throw an assertion error when not equal" - {
      val caught = intercept[AssertionError] {
        1 should equal (2)
      }
      assert(caught.getMessage === "1 did not equal 2")
    }

    "should do nothing when not equal and used with shouldNot" - {
      1 shouldNot equal (2)
      val option = Some(1)
      option shouldNot equal (Some(2)) 
    }

    "should throw an assertion error when equal but used with shouldNot" - {
      val caught = intercept[AssertionError] {
        1 shouldNot equal (1)
      }
      assert(caught.getMessage === "1 equaled 1")
    }
  }

  "The be matcher" -- {

    "(for booleans)" -- {

      "should do nothing when false is compared to false" - {
        false should be (false)
      }

      "should do nothing when true is compared to true" - {
        true should be (true)
      }

      "should throw an assertion error when not equal" - {
        val caught = intercept[AssertionError] {
          false should be (true)
        }
        assert(caught.getMessage === "false was not true")
      }
    }

    "(for null)" -- {

      "should do nothing when null is compared to null" - {
        val o: String = null
        o should be (null)
        o should equal (null)
      }

      "should throw an assertion error when non-null compared to null" - {
        val caught = intercept[AssertionError] {
          val o = "Helloooooo"
          o should be (null)
        }
        assert(caught.getMessage === "\"Helloooooo\" was not null")
      }

      "should do nothing when non-null is compared to not null" - {
        val o = "Helloooooo"
        o should not { be (null) }
        o shouldNot be (null)
      }

      "should throw an assertion error when null compared to not null" - {
        val caught1 = intercept[AssertionError] {
          val o: String = null
          o should not { be (null) }
        }
        assert(caught1.getMessage === "null was null")
        val caught2 = intercept[AssertionError] {
          val o: String = null
          o shouldNot be (null)
        }
        assert(caught2.getMessage === "null was null")
      }

      "should work when used in a logical expression" - {
        val o: String = null
        o should { be (null) and equal (null) }
        o should { equal (null) and be (null) }
      }
    }

    "(for Nil)" -- {

      "should do nothing when an empty list is compared to Nil" - {
        val emptyList = List[String]()
        emptyList should be (Nil)
        emptyList should equal (Nil)
      }

      "should throw an assertion error when a non-empty list is compared to Nil" - {
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

      "should do nothing when non-null is compared to not null" - {
        val nonEmptyList = List("Helloooooo")
        nonEmptyList should not { be (Nil) }
        nonEmptyList shouldNot be (Nil)
        nonEmptyList should not { equal (Nil) }
        nonEmptyList shouldNot equal (Nil)
      }

      "should throw an assertion error when null compared to not null" - {
        val emptyList = List[String]()
        val caught1 = intercept[AssertionError] {
          emptyList should not { be (Nil) }
        }
        assert(caught1.getMessage === "List() was List()")

        val caught2 = intercept[AssertionError] {
          emptyList shouldNot be (Nil)
        }
        assert(caught2.getMessage === "List() was List()")

        val caught3 = intercept[AssertionError] {
          emptyList should not { equal (Nil) }
        }
        assert(caught3.getMessage === "List() equaled List()")

        val caught4 = intercept[AssertionError] {
          emptyList shouldNot equal (Nil)
        }
        assert(caught4.getMessage === "List() equaled List()")
      }

      "should work when used in a logical expression" - {
        val emptyList = List[Int]()
        emptyList should { be (Nil) and equal (Nil) }
        emptyList should { equal (Nil) and be (Nil) } // Nada, and nada is nada
      }
    }

    "(for None)" -- {

      "should do nothing when a None option is compared to None" - {
        val option: Option[String] = None
        option should be (None)
        option should equal (None)
      }

      "should throw an assertion error when a Some is compared to None" - {
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

      "should do nothing when Some is compared to not None" - {
        val someString = Some("Helloooooo")
        someString should not { be (None) }
        someString shouldNot be (None)
        someString should not { equal (None) }
        someString shouldNot equal (None)
      }

      "should throw an assertion error when None compared to not None" - {
        val none = None
        val caught1 = intercept[AssertionError] {
          none should not { be (None) }
        }
        assert(caught1.getMessage === "None was None")

        val caught2 = intercept[AssertionError] {
          none shouldNot be (None)
        }
        assert(caught2.getMessage === "None was None")

        val caught3 = intercept[AssertionError] {
          none should not { equal (None) }
        }
        assert(caught3.getMessage === "None equaled None")

        val caught4 = intercept[AssertionError] {
          none shouldNot equal (None)
        }
        assert(caught4.getMessage === "None equaled None")

        val noString: Option[String] = None
        val caught5 = intercept[AssertionError] {
          noString should not { be (None) }
        }
        assert(caught5.getMessage === "None was None")

        val caught6 = intercept[AssertionError] {
          noString shouldNot be (None)
        }
        assert(caught6.getMessage === "None was None")

        val caught7 = intercept[AssertionError] {
          noString should not { equal (None) }
        }
        assert(caught7.getMessage === "None equaled None")

        val caught8 = intercept[AssertionError] {
          noString shouldNot equal (None)
        }
        assert(caught8.getMessage === "None equaled None")
      }

      "should work when used in a logical expression" - {
        val none = None
        none should { be (None) and equal (None) }
        none should { equal (None) and be (None) }
        val noString: Option[String] = None
        noString should { be (None) and equal (None) }
        noString should { equal (None) and be (None) }
      }
    }

    "(for symbols)" -- {

      "should look for a length of 0 when called on a string" - {
        "" should be ('empty)
        val caught = intercept[AssertionError] {
          "hi" should be ('empty)
        }
        caught.getMessage should equal ("\"hi\" was not empty")
      }

      "should call isEmpty when passed 'empty" - {
        val emptySet = Set[Int]()
        emptySet should be ('empty)
        val nonEmptySet = Set(1, 2, 3)
        nonEmptySet should not { be ('empty) }
        val caught1 = intercept[AssertionError] {
          nonEmptySet should be ('empty)
        }
        assert(caught1.getMessage === "Set(1, 2, 3) was not empty")
        val caught2 = intercept[AssertionError] {
          nonEmptySet shouldNot be ('hasDefiniteSize)
        }
        assert(caught2.getMessage === "Set(1, 2, 3) was hasDefiniteSize")
        val caught3 = intercept[IllegalArgumentException] {
          nonEmptySet shouldNot be ('happy)
        }
        assert(caught3.getMessage === "Set(1, 2, 3) has neither a happy nor an isHappy method")
        val caught4 = intercept[IllegalArgumentException] {
          "unhappy" shouldNot be ('happy)
        }
        assert(caught4.getMessage === "\"unhappy\" has neither a happy nor an isHappy method")
      }

      "should be invokable from be a Symbol, be an Symbol, and be the Symbol" - {
        val emptySet = Set()
        emptySet should be a ('empty)
        emptySet should be an ('empty)
        val nonEmptySet = Set(1, 2, 3)
        nonEmptySet should not { be a ('empty) }
        nonEmptySet should not { be an ('empty) }
      }

      "should call empty when passed 'empty" - {
        class EmptyMock {
          def empty: Boolean = true
        }
        class NonEmptyMock {
          def empty: Boolean = false
        }
        (new EmptyMock) should be ('empty)
        (new NonEmptyMock) should not { be ('empty) }
        (new NonEmptyMock) shouldNot be ('empty)
      }

      "should throw IllegalArgumentException if no empty or isEmpty method" - {
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
        val ex3 = intercept[IllegalArgumentException] {
          (new NonEmptyMock) shouldNot be ('empty)
        }
        ex3.getMessage should equal ("NonEmptyMock has neither an empty nor an isEmpty method")
      }

      "should throw IllegalArgumentException if both an empty and an isEmpty method exist" - {
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
        val ex3 = intercept[IllegalArgumentException] {
          (new NonEmptyMock) shouldNot be ('empty)
        }
        ex3.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method")
      }

      "should access an 'empty' val when passed 'empty" - {
        class EmptyMock {
          val empty: Boolean = true
        }
        class NonEmptyMock {
          val empty: Boolean = false
        }
        (new EmptyMock) should be ('empty)
        (new NonEmptyMock) should not { be ('empty) }
        (new NonEmptyMock) shouldNot be ('empty)
      }
    }

    "(for Any)" -- {
      "should do nothing when equal" - {
        1 should be (1)
        val option = Some(1)
        option should be (Some(1)) 
      }

      "should throw an assertion error when not equal" - {
        val caught = intercept[AssertionError] {
          1 should be (2)
        }
        assert(caught.getMessage === "1 was not 2")
      }

      "should do nothing when not equal and used with shouldNot" - {
        1 shouldNot be (2)
        val option = Some(1)
        option shouldNot be (Some(2)) 
      }

      "should throw an assertion error when equal but used with shouldNot" - {
        val caught = intercept[AssertionError] {
          1 shouldNot be (1)
        }
        assert(caught.getMessage === "1 was 1")
      }
    }
  }

  "the be ('empty) syntax" -- {

    "should call isEmpty" - {
      val emptySet = Set[Int]()
      emptySet should be ('empty)
      val nonEmptySet = Set(1, 2, 3)
      nonEmptySet should not { be ('empty) }
    }

    "should call empty when passed 'empty" - {
      class EmptyMock {
        def empty: Boolean = true
      }
      class NonEmptyMock {
        def empty: Boolean = false
      }
      (new EmptyMock) should be ('empty)
      (new NonEmptyMock) should not { be ('empty) }
      (new NonEmptyMock) shouldNot be ('empty)
    }

    "should throw IllegalArgumentException if no empty or isEmpty method" - {
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
      val ex3 = intercept[IllegalArgumentException] {
        (new NonEmptyMock) shouldNot be ('empty)
      }
      ex3.getMessage should equal ("NonEmptyMock has neither an empty nor an isEmpty method")
    }

    "should throw IllegalArgumentException if both an empty and an isEmpty method exist" - {
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
      val ex3 = intercept[IllegalArgumentException] {
        (new NonEmptyMock) shouldNot be ('empty)
      }
      ex3.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method")
    }

    "should access an 'empty' val when passed 'empty" - {
      class EmptyMock {
        val empty: Boolean = true
      }
      class NonEmptyMock {
        val empty: Boolean = false
      }
      (new EmptyMock) should be ('empty)
      (new NonEmptyMock) should not { be ('empty) }
      (new NonEmptyMock) shouldNot be ('empty)
    }

    "should look for a length of 0 when called on a string" - {
      "" should be ('empty)
      val caught = intercept[AssertionError] {
        "hi" should be ('empty)
      }
      caught.getMessage should equal ("\"hi\" was not empty")
    }
  }

/*
  "the beEmpty matcher" -- {

    "should call isEmpty" - {
      val emptySet = Set[Int]()
      emptySet should be (empty)
      val nonEmptySet = Set(1, 2, 3)
      nonEmptySet should not { beEmpty }
    }

    "should call empty when passed 'empty" - {
      class EmptyMock {
        def empty: Boolean = true
      }
      class NonEmptyMock {
        def empty: Boolean = false
      }
      (new EmptyMock) should beEmpty
      (new NonEmptyMock) should not { beEmpty }
      (new NonEmptyMock) shouldNot beEmpty
    }

    "should throw IllegalArgumentException if no empty or isEmpty method" - {
      class EmptyMock {
        override def toString = "EmptyMock"
      }
      class NonEmptyMock {
        override def toString = "NonEmptyMock"
      }
      val ex1 = intercept[IllegalArgumentException] {
        (new EmptyMock) should beEmpty
      }
      ex1.getMessage should equal ("EmptyMock has neither an empty nor an isEmpty method")
      val ex2 = intercept[IllegalArgumentException] {
        (new NonEmptyMock) should not { beEmpty }
      }
      ex2.getMessage should equal ("NonEmptyMock has neither an empty nor an isEmpty method")
      val ex3 = intercept[IllegalArgumentException] {
        (new NonEmptyMock) shouldNot beEmpty
      }
      ex3.getMessage should equal ("NonEmptyMock has neither an empty nor an isEmpty method")
    }

    "should throw IllegalArgumentException if both an empty and an isEmpty method exist" - {
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
        (new EmptyMock) should beEmpty
      }
      ex1.getMessage should equal ("EmptyMock has both an empty and an isEmpty method")
      val ex2 = intercept[IllegalArgumentException] {
        (new NonEmptyMock) should not { be (empty) }
      }
      ex2.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method")
      val ex3 = intercept[IllegalArgumentException] {
        (new NonEmptyMock) shouldNot beEmpty
      }
      ex3.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method")
    }

    "should access an 'empty' val when passed 'empty" - {
      class EmptyMock {
        val empty: Boolean = true
      }
      class NonEmptyMock {
        val empty: Boolean = false
      }
      (new EmptyMock) should beEmpty
      (new NonEmptyMock) should not { beEmpty }
      (new NonEmptyMock) shouldNot beEmpty
    }

    "should look for a length of 0 when called on a string" - {
      "" should beEmpty
      val caught = intercept[AssertionError] {
        "hi" should beEmpty
      }
      caught.getMessage should equal ("\"hi\" was not empty")
    }
  }

  "the beNull matcher" -- {
    "should do nothing when null is compared to null" - {
      val o: String = null
      o should beNull
    }

    "should throw an assertion error when non-null compared to null" - {
      val caught = intercept[AssertionError] {
        val o = "Helloooooo"
        o should beNull
      }
      assert(caught.getMessage === "\"Helloooooo\" was not null")
    }

    "should do nothing when non-null is compared to not null" - {
      val o = "Helloooooo"
      o should not { beNull }
      o shouldNot beNull
    }

    "should throw an assertion error when null compared to not null" - {
      val caught1 = intercept[AssertionError] {
        val o: String = null
        o should not { beNull }
      }
      assert(caught1.getMessage === "null was null")
      val caught2 = intercept[AssertionError] {
        val o: String = null
        o shouldNot beNull
      }
      assert(caught2.getMessage === "null was null")
    }

    "should work when used in a logical expression" - {
      val o: String = null
      o should { beNull and equal (null) }
      o should { equal (null) and beNull }
    }
  }

  "the beNil matcher" -- {

    "should do nothing when an empty list is compared to Nil" - {
      val emptyList = List[String]()
      emptyList should beNil
    }

    "should throw an assertion error when a non-empty list is compared to Nil" - {
      val nonEmptyList = List("Helloooooo")
      val caught1 = intercept[AssertionError] {
        nonEmptyList should beNil
      }
      assert(caught1.getMessage === "List(Helloooooo) was not List()")
    }

    "should do nothing when non-Nil is compared to not Nil" - {
      val nonEmptyList = List("Helloooooo")
      nonEmptyList should not { beNil }
      nonEmptyList shouldNot beNil
    }

    "should throw an assertion error when Nil compared to not Nil" - {
      val emptyList = List[String]()
      val caught1 = intercept[AssertionError] {
        emptyList should not { beNil }
      }
      assert(caught1.getMessage === "List() was List()")
      val caught2 = intercept[AssertionError] {
        emptyList shouldNot beNil
      }
      assert(caught2.getMessage === "List() was List()")
    }

    "should work when used in a logical expression" - {
      val emptyList = List[Int]()
      emptyList should { beNil and equal (Nil) }
      emptyList should { equal (Nil) and beNil } // Nada, and nada is nada
    }
  }

  "The beNone matcher" -- {

      "should do nothing when a None option is compared to None" - {
        val option: Option[String] = None
        option should beNone
      }

      "should throw an assertion error when a Some is compared to None" - {
        val someString = Some("Helloooooo")
        val caught1 = intercept[AssertionError] {
          someString should beNone
        }
        assert(caught1.getMessage === "Some(Helloooooo) was not None")
      }

      "should do nothing when Some is compared to not None" - {
        val someString = Some("Helloooooo")
        someString should not { beNone }
        someString shouldNot beNone
      }

      "should throw an assertion error when None compared to not None" - {

        val none = None
        val caught1 = intercept[AssertionError] {
          none should not { beNone }
        }
        assert(caught1.getMessage === "None was None")

        val caught2 = intercept[AssertionError] {
          none shouldNot beNone
        }
        assert(caught2.getMessage === "None was None")

        val noString: Option[String] = None
        val caught3 = intercept[AssertionError] {
          noString should not { beNone }
        }
        assert(caught3.getMessage === "None was None")

        val caught4 = intercept[AssertionError] {
          noString shouldNot beNone
        }
        assert(caught4.getMessage === "None was None")
      }

      "should work when used in a logical expression" - {
        val none = None
        none should { beNone and equal (None) }
        none should { equal (None) and beNone }
        val noString: Option[String] = None
        noString should { beNone and equal (None) }
        noString should { equal (None) and beNone }
      }
  }

  "The beDefined matcher" -- {

    "should do nothing when used with a Some" - {
      val someString: Some[String] = Some("hi")
      someString should beDefined
      val optionString: Option[String] = Some("hi")
      optionString should beDefined
    }

    "should throw AssertionError when used with a None" - {
      val none: None.type = None
      val caught1 = intercept[AssertionError] {
        none should beDefined
      }
      assert(caught1.getMessage === "None was not defined")
      val option: Option[Int] = None
      val caught2 = intercept[AssertionError] {
        option should beDefined
      }
      assert(caught2.getMessage === "None was not defined")
    }

    "should call defined" - {
      class DefinedMock {
        def defined: Boolean = true
      }
      class NonDefinedMock {
        def defined: Boolean = false
      }
      (new DefinedMock) should beDefined
      (new NonDefinedMock) should not { beDefined }
      (new NonDefinedMock) shouldNot beDefined
    }

    "should throw IllegalArgumentException if no defined or isDefined method" - {
      class DefinedMock {
        override def toString = "DefinedMock"
      }
      class NonDefinedMock {
        override def toString = "NonDefinedMock"
      }
      val ex1 = intercept[IllegalArgumentException] {
        (new DefinedMock) should beDefined
      }
      ex1.getMessage should equal ("DefinedMock has neither a defined nor an isDefined method")
      val ex2 = intercept[IllegalArgumentException] {
        (new NonDefinedMock) should not { beDefined }
      }
      ex2.getMessage should equal ("NonDefinedMock has neither a defined nor an isDefined method")
      val ex3 = intercept[IllegalArgumentException] {
        (new NonDefinedMock) shouldNot beDefined
      }
      ex3.getMessage should equal ("NonDefinedMock has neither a defined nor an isDefined method")
    }

    "should throw IllegalArgumentException if both a defined and an isDefined method exist" - {
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
        (new DefinedMock) should beDefined
      }
      ex1.getMessage should equal ("DefinedMock has both a defined and an isDefined method")
      val ex2 = intercept[IllegalArgumentException] {
        (new NonDefinedMock) should not { beDefined }
      }
      ex2.getMessage should equal ("NonDefinedMock has both a defined and an isDefined method")
      val ex3 = intercept[IllegalArgumentException] {
        (new NonDefinedMock) shouldNot beDefined
      }
      ex3.getMessage should equal ("NonDefinedMock has both a defined and an isDefined method")
    }

    "should access an 'defined' val" - {
      class DefinedMock {
        val defined: Boolean = true
      }
      class NonDefinedMock {
        val defined: Boolean = false
      }
      (new DefinedMock) should beDefined
      (new NonDefinedMock) should not { be (defined) }
      (new NonDefinedMock) shouldNot be (defined)
    }
  }

  "The beSome matcher" -- {
    "should do nothing if defined and equal" - {
      val option: Option[Int] = Some(1)
      option should beSome(1)
      val someString: Some[String] = Some("hi")
      someString should beSome("hi")
      val optionString: Option[String] = Some("hi")
      optionString should beSome("hi")
    }

    "should throw AssertionError when used with a None" - {
      val none: None.type = None
      val caught1 = intercept[AssertionError] {
        none should beSome("hi")
      }
      assert(caught1.getMessage === "None was not Some(\"hi\")")
      val option: Option[Int] = None
      val caught2 = intercept[AssertionError] {
        option should be (Some(3))
      }
      assert(caught2.getMessage === "None was not Some(3)")
    }
  }

  "The beTrue matcher" -- {
    val trueBoolean: Boolean = true
    val falseBoolean: Boolean = false
    "should do nothing if true" - {
      trueBoolean should beTrue
      falseBoolean shouldNot beTrue
    }
    "should throw AssertionError if false" - {
      val caught1 = intercept[AssertionError] {
        falseBoolean should beTrue
      }
      caught1.getMessage should equal ("the boolean expression was not true")
      val caught2 = intercept[AssertionError] {
        trueBoolean shouldNot beTrue
      }
      caught2.getMessage should equal ("the boolean expression was true")
    }
  }

  "The beFalse matcher" -- {
    val trueBoolean: Boolean = true
    val falseBoolean: Boolean = false
    "should do nothing if false" - {
      falseBoolean should beFalse
      trueBoolean shouldNot beFalse
    }
    "should throw AssertionError if true" - {
      val caught1 = intercept[AssertionError] {
        trueBoolean should beFalse
      }
      caught1.getMessage should equal ("the boolean expression was not false")
      val caught2 = intercept[AssertionError] {
        falseBoolean shouldNot beFalse
      }
      caught2.getMessage should equal ("the boolean expression was false")
    }
  }
*/

  "The be 'defined syntax" -- {

    "should do nothing when used with a Some" - {
      val someString: Some[String] = Some("hi")
      someString should be ('defined)
      val optionString: Option[String] = Some("hi")
      optionString should be ('defined)
    }

    "should throw AssertionError when used with a None" - {
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

    "should call defined" - {
      class DefinedMock {
        def defined: Boolean = true
      }
      class NonDefinedMock {
        def defined: Boolean = false
      }
      (new DefinedMock) should be ('defined)
      (new NonDefinedMock) should not { be ('defined) }
      (new NonDefinedMock) shouldNot be ('defined)
    }

    "should throw IllegalArgumentException if no defined or isDefined method" - {
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
      val ex3 = intercept[IllegalArgumentException] {
        (new NonDefinedMock) shouldNot be ('defined)
      }
      ex3.getMessage should equal ("NonDefinedMock has neither a defined nor an isDefined method")
    }

    "should throw IllegalArgumentException if both a defined and an isDefined method exist" - {
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
      val ex3 = intercept[IllegalArgumentException] {
        (new NonDefinedMock) shouldNot be ('defined)
      }
      ex3.getMessage should equal ("NonDefinedMock has both a defined and an isDefined method")
    }

    "should access an 'defined' val" - {
      class DefinedMock {
        val defined: Boolean = true
      }
      class NonDefinedMock {
        val defined: Boolean = false
      }
      (new DefinedMock) should be ('defined)
      (new NonDefinedMock) should not { be ('defined) }
      (new NonDefinedMock) shouldNot be ('defined)
    }
  }
  "The not matcher" -- {
    "should do nothing when not true" - {
      1 should not { equal (2) }
    }
    "should throw an assertion error when true" - {
      val caught = intercept[AssertionError] {
        1 should not { equal (1) }
      }
      assert(caught.getMessage === "1 equaled 1")
    }
    "should work at the beginning of an and expression" - {
      val string = "Hello, world!"
      string should { not { have length 7 } and { startWith substring "Hello" } }
    }
  }

  "The shouldNot method" -- {
    "should do nothing when not true" - {
      1 shouldNot equal (2)
    }
    "should throw an assertion error when true" - {
      val caught = intercept[AssertionError] {
        1 shouldNot equal (1)
      }
      assert(caught.getMessage === "1 equaled 1")
    }
  }

  "The endWith matcher" -- {
    "should do nothing when true" - {
      "Hello, world" should endWith substring ("world")
      "Hello, world" shouldNot endWith substring ("Hello")
      "Hello, world" should endWith regex ("wo.ld")
      "Hello, world" shouldNot endWith regex ("Hel*o")
      "Hello, world" should endWith regex ("wo.ld".r)
      "Hello, world" shouldNot endWith regex ("Hel*o".r)
    }
    "should throw an assertion error when not true" - {
      val caught1 = intercept[AssertionError] {
        "Hello, world" should endWith substring ("planet")
      }
      assert(caught1.getMessage.indexOf("did not end with") != -1)
      val caught2 = intercept[AssertionError] {
        "Hello, world" shouldNot endWith substring ("world")
      }
      assert(caught2.getMessage.indexOf("ended with") != -1)
      val caught3 = intercept[AssertionError] {
        "Hello, world" should endWith regex ("pla.et")
      }
      assert(caught3.getMessage.indexOf("did not end with a match for the regular expression") != -1)
      val caught4 = intercept[AssertionError] {
        "Hello, world" shouldNot endWith regex ("wo.ld")
      }
      assert(caught4.getMessage.indexOf("ended with a match for the regular expression") != -1)
      val caught5 = intercept[AssertionError] {
        "Hello, world" should endWith regex ("pla.et")
      }
      assert(caught5.getMessage.indexOf("did not end with a match for the regular expression") != -1)
      val caught6 = intercept[AssertionError] {
        "Hello, world" shouldNot endWith regex ("wo.ld")
      }
      assert(caught6.getMessage.indexOf("ended with a match for the regular expression") != -1)
    }
    "should work inside an and clause" - {

      "Hello, world" should { endWith substring "world" and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and (endWith substring "world") }
      "Hello, world" should { endWith regex "wo.ld" and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and (endWith regex "wo.ld") }
      "Hello, world" should { endWith regex "wo.ld".r and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and (endWith regex "wo.ld".r) }

      "Hello, world" shouldNot { endWith substring "planet" and equal ("Hello, world") }
      "Hello, world" shouldNot { equal ("Hello, world") and (endWith substring "planet") }
      "Hello, world" shouldNot { endWith regex "wo.l" and equal ("Hello, world") }
      "Hello, world" shouldNot { equal ("Hello, world") and (endWith regex "wo.l") }
      "Hello, world" shouldNot { endWith regex "wo.l".r and equal ("Hello, world") }
      "Hello, world" shouldNot { equal ("Hello, world") and (endWith regex "wo.l".r) }
    }
  }

  "The startWith matcher" -- {
    "should do nothing when true" - {
      "Hello, world" should startWith substring ("Hello")
      "Hello, world" shouldNot startWith substring ("Goodbye")
      "Hello, world" should startWith regex ("Hel*o")
      "Hello, world" shouldNot startWith regex ("Yel*o")
      "Hello, world" should startWith regex ("Hel*o".r)
      "Hello, world" shouldNot startWith regex ("Yel*o".r)
    }
    "should throw an assertion error when not true" - {
      val caught1 = intercept[AssertionError] {
        "Hello, world" should startWith substring ("Greetings")
      }
      assert(caught1.getMessage.indexOf("did not start with") != -1)
      val caught2 = intercept[AssertionError] {
        "Hello, world" shouldNot startWith substring ("Hello")
      }
      assert(caught2.getMessage.indexOf("started with") != -1)
      val caught3 = intercept[AssertionError] {
        "Hello, world" should startWith regex ("Gre*tings")
      }
      assert(caught3.getMessage.indexOf("did not start with a match for the regular expression") != -1)
      val caught4 = intercept[AssertionError] {
        "Hello, world" shouldNot startWith regex ("Hel*o")
      }
      assert(caught4.getMessage.indexOf("started with a match for the regular expression") != -1)
      val caught5 = intercept[AssertionError] {
        "Hello, world" should startWith regex ("Gre*tings".r)
      }
      assert(caught5.getMessage.indexOf("did not start with a match for the regular expression") != -1)
      val caught6 = intercept[AssertionError] {
        "Hello, world" shouldNot startWith regex ("Hel*o".r)
      }
      assert(caught6.getMessage.indexOf("started with a match for the regular expression") != -1)
    }
    "should work inside an and clause" - {

      "Hello, world" should { startWith substring ("Hello") and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and (startWith substring ("Hello")) }
      "Hello, world" should { startWith regex ("Hel*o") and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and (startWith regex ("Hel*o")) }
      "Hello, world" should { startWith regex ("Hel*o".r) and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and (startWith regex ("Hel*o".r)) }

      "Hello, world" shouldNot { startWith substring ("Yello") and equal ("Hello, world") }
      "Hello, world" shouldNot { equal ("Hello, world") and (startWith substring ("Yello")) }
      "Hello, world" shouldNot { startWith regex ("Yel*o") and equal ("Hello, world") }
      "Hello, world" shouldNot { equal ("Hello, world") and (startWith regex ("Yel*o")) }
      "Hello, world" shouldNot { startWith regex ("Yel*o".r) and equal ("Hello, world") }
      "Hello, world" shouldNot { equal ("Hello, world") and (startWith regex ("Yel*o".r)) }
    }
  }

  "The and matcher" -- {

    "should do nothing when both operands are true" - {
      1 should { equal (1) and equal (2 - 1) }
    }

    "should throw AssertionError when first operands is false" - {
      val caught = intercept[AssertionError] {
        1 should (equal (2) and equal (1))
      }
      caught.getMessage should equal ("1 did not equal 2") // because and short circuits
    }

    "should throw AssertionError when second operand is false" - {
      val caught = intercept[AssertionError] {
        1 should (equal (1) and equal (2))
      }
      caught.getMessage should equal ("1 equaled 1, but 1 did not equal 2")
    }

    "should not execute the right matcher creation function when the left operand is false" - {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      val caught = intercept[AssertionError] {
        // This should fail, but without applying the matcher returned by mockMatcher
        1 should { equal (2) and mockMatcher }
      }
      called should be (false)
      assert(caught.getMessage === "1 did not equal 2")
    }

    "should execute the right matcher creation function when the left operand is true" - {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      1 should { equal (1) and mockMatcher }
      called should be (true)
    }
    "should give good failure messages when used with not" - {
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

  "The or matcher" -- {

    "should do nothing when both operands are true" - {
      1 should { equal (1) or equal (2 - 1) }
    }

    "should throw AssertionError when both operands are false" - {
      val caught = intercept[AssertionError] {
        1 should (equal (2) or equal (3))
      }
      caught.getMessage should equal ("1 did not equal 2, and 1 did not equal 3") // because and short circuits
    }

    "should do nothing when first operand is true and second operand is false" - {
      1 should (equal (1) or equal (2))
    }

    "should do nothing when first operand is false and second operand is true" - {
      1 should (equal (2) or equal (1))
    }

    "should not execute the right matcher creation function when the left operand is true" - {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      // This should succeed, but without applying the matcher returned by mockMatcher
      1 should { equal (1) or mockMatcher }
      called should be (false)
    }

    "should execute the right matcher creation function when the left operand is false" - {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      1 should { equal (2) or mockMatcher }
      called should be (true)
    }

    "should give good failure messages when used with not" - {
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

  "The andNot matcher" -- {

    "should do nothing when left operands is true and right false" - {
      1 should { equal (1) andNot equal (2) }
    }

    "should throw AssertionError when first operands is false" - {
      val caught = intercept[AssertionError] {
        1 should (equal (2) andNot equal (2))
      }
      caught.getMessage should equal ("1 did not equal 2") // because and short circuits
    }

    "should throw AssertionError when second operand is true" - {
      val caught = intercept[AssertionError] {
        1 should (equal (1) andNot equal (1))
      }
      caught.getMessage should equal ("1 equaled 1, but 1 equaled 1")
    }

    "should not execute the right matcher creation function when the left operand is false" - {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      val caught = intercept[AssertionError] {
        // This should fail, but without applying the matcher returned by mockMatcher
        1 should { equal (2) andNot mockMatcher }
      }
      called should be (false)
      assert(caught.getMessage === "1 did not equal 2")
    }

    "should execute the right matcher creation function when the left operand is true" - {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(false, "", "") } }
      1 should { equal (1) andNot mockMatcher }
      called should be (true)
    }

    "should give good failure messages when used with not" - {
      val caught1 = intercept[AssertionError] {
        1 should (not { equal (1) } andNot equal (2))
      }
      caught1.getMessage should equal ("1 equaled 1") // because andNot short circuits
      val caught2 = intercept[AssertionError] {
        1 should (equal (1) andNot { equal (1) })
      }
      caught2.getMessage should equal ("1 equaled 1, but 1 equaled 1")
    }
  }

  "The orNot matcher" -- {

    "should do nothing when left operand is true and right false" - {
      1 should { equal (1) orNot equal (2) }
    }

    "should do nothing when when both operands are false" - {
      1 should (equal (2) orNot equal (2))
    }

    "should do nothing when left operand is true and right true" - {
      1 should { equal (1) orNot equal (1) }
    }

    "should throw AssertionError when first operand is false and second operand is true" - {
      val caught = intercept[AssertionError] {
        1 should (equal (2) orNot equal (1))
      }
      caught.getMessage should equal ("1 did not equal 2, and 1 equaled 1")
    }

    "should not execute the right matcher creation function when the left operand is true" - {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      // This should succeed, but without applying the matcher returned by mockMatcher
      1 should { equal (1) orNot mockMatcher }
      called should be (false)
    }

    "should execute the right matcher creation function when the left operand is false" - {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(false, "", "") } }
      1 should { equal (2) orNot mockMatcher }
      called should be (true)
    }

    "should give good failure messages when used with not" - {
      val caught1 = intercept[AssertionError] {
        1 should (not { equal (1) } orNot equal (1))
      }
      caught1.getMessage should equal ("1 equaled 1, and 1 equaled 1")
      val caught2 = intercept[AssertionError] {
        1 should (equal (2) orNot not { equal (2) }) // Don't do this at home
      }
      caught2.getMessage should equal ("1 did not equal 2, and 1 did not equal 2")
    }
  }

  "The have word" -- {

    "should work with map and key, right after a 'should'" - {
      val map = Map(1 -> "Howdy")
      map should have key (1)
      map should have key (1)
      map should equal { Map(1 -> "Howdy") }
      val otherMap = Map("Howdy" -> 1)
      otherMap should have key ("Howdy")
      otherMap should equal { Map("Howdy" -> 1) }
      import scala.collection.immutable.TreeMap
      val treeMap = TreeMap(1 -> "hi", 2 -> "howdy")
      treeMap should have key (1)
    }

    "should work with map and key, in a logical expression" - {
      val map = Map(1 -> "Howdy")
      // The compiler infer the type of the value to be Nothing if I say: map should { have key 1 and equal (Map(1 -> "Howdy")) }
      // map should { have.key[Int, String](1) and equal (Map(1 -> "Howdy")) }
      map should { have key (1) and equal (Map(1 -> "Howdy")) }
      val otherMap = Map("Howdy" -> 1)
      // otherMap should { have.key[String, Int]("Howdy") and equal (Map("Howdy" -> 1)) }
      otherMap should { have key ("Howdy") and equal (Map("Howdy" -> 1)) }
    }

    "should work with map and key, right after a 'shouldNot'" - {
      val map = Map(1 -> "Howdy")
      map shouldNot have key (2)
    }

    "should work with map and value, right after a 'should'" - {
      val map = Map(1 -> "Howdy")
      map should have value ("Howdy")
      map should have value ("Howdy")
      map should equal { Map(1 -> "Howdy") }
      val otherMap = Map("Howdy" -> 1)
      otherMap should have value (1)
      otherMap should equal { Map("Howdy" -> 1) }
    }

    "should work with map and value, in a logical expression" - {
      val map = Map(1 -> "Howdy")
      map should { equal (Map(1 -> "Howdy")) and (have value "Howdy") }
      val otherMap = Map("Howdy" -> 1)
      otherMap should { have value (1) and equal (Map("Howdy" -> 1)) }
    }

    "should work with map and value, right after a 'shouldNot'" - {
      val map = Map(1 -> "Howdy")
      map shouldNot have value ("Doody")
    }

    "should work with collection and size, in an and expression." - {
      val list = List(1, 2, 3)
      list should { have size (3) and equal (List(1, 2, 3)) }
    }

    "should work with collection and size, right after a 'should'" - {

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

    "should work with collection and size, right after a 'shouldNot'" - {

      val map = Map(1 -> "Howdy")
      map shouldNot have size (2)
      val caught1 = intercept[AssertionError] {
        map shouldNot have size (1)
      }
      assert(caught1.getMessage.indexOf("had size") != -1)

      val list = List(1, 2, 3, 4, 5)
      list shouldNot have size (6)
      val caught2 = intercept[AssertionError] {
        list shouldNot have size (5)
      }
      assert(caught2.getMessage.indexOf("had size") != -1)

      val set = Set(1.0, 2.0, 3.0)
      set shouldNot have size (0)
      val caught3 = intercept[AssertionError] {
        set shouldNot have size (3)
      }
      assert(caught3.getMessage.indexOf("had size") != -1)

      val array = Array[String]()
      array shouldNot have size (2)
      val caught4 = intercept[AssertionError] {
        array shouldNot have size (0)
      }
      assert(caught4.getMessage.indexOf("had size") != -1)
    }
  }

  "The contain word" -- {
 
    "should work with a set, list, array, and map right after a 'should'" - {

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

    "should work with a set, list, array, and map right after a 'shouldNot'" - {

      val set = Set(1, 2, 3)
      set shouldNot contain element (5)
      val caught1 = intercept[AssertionError] {
        set shouldNot contain element (2)
      }
      assert(caught1.getMessage.indexOf("contained element") != -1)

      val list = List("one", "two", "three")
      list shouldNot contain element ("five")
      val caught2 = intercept[AssertionError] {
        list shouldNot contain element ("two")
      }
      assert(caught2.getMessage.indexOf("contained element") != -1)

      val array = Array("one", "two", "three")
      array shouldNot contain element ("five")
      val caught3 = intercept[AssertionError] {
        array shouldNot contain element ("one")
      }
      assert(caught3.getMessage.indexOf("contained element") != -1)

      val map = Map(1 -> "one", 2 -> "two", 3 -> "three")
      val tuple2: Tuple2[Int, String] = 1 -> "won"
      map shouldNot contain element (tuple2)
      val caught4 = intercept[AssertionError] {
        map shouldNot contain element (1 -> "one")
      }
      assert(caught4.getMessage.indexOf("contained element") != -1)
    }

    "should work with string and have length right after a 'should'" - {
      val string = "hi"
      string should have length (2)
      val caught = intercept[AssertionError] {
        string should have length (3)
      }
      assert(caught.getMessage.indexOf("did not have length") != -1)
    }

    "should work with string and have length right after a 'shouldNot'" - {
      val string = "hi"
      string shouldNot have length (3)
      val caught = intercept[AssertionError] {
        string shouldNot have length (2)
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should work with string, should, and have length in an and expression" - {
      val string = "hi"
      string should { have length (2) and equal ("hi") }
      val caught = intercept[AssertionError] {
        string should { have length (3) and equal ("hi") }
      }
      assert(caught.getMessage.indexOf("did not have length") != -1)
    }

    "should work with string, shouldNot, and have length in an and expression" - {
      val string = "hi"
      string shouldNot { have length (3) and equal ("hi") }
      val caught = intercept[AssertionError] {
        string shouldNot { have length (2) and equal ("hi") }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should work with array and have length right after a 'should'" - {
      val array = Array('h', 'i')
      array should have length (2)
      val caught = intercept[AssertionError] {
        array should have length (3)
      }
      assert(caught.getMessage.indexOf("did not have length") != -1)
    }

    "should work with array and have length right after a 'shouldNot'" - {
      val array = Array('h', 'i')
      array shouldNot have length (3)
      val caught = intercept[AssertionError] {
        array shouldNot have length (2)
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should work with array, should, and have length in an and expression" - {
      val array = Array('h', 'i')
      array should { have length (2) and equal (Array('h', 'i')) }
      val caught = intercept[AssertionError] {
        array should { have length (3) and equal (Array('h', 'i')) }
      }
      assert(caught.getMessage.indexOf("did not have length") != -1)
    }

    "should work with array, shouldNot, and have length in an and expression" - {
      val array = Array('h', 'i')
      array shouldNot { have length (3) and equal (Array('h', 'i')) }
      val caught = intercept[AssertionError] {
        array shouldNot { have length (2) and equal (Array('h', 'i')) }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should work with any arbitrary object that has a length method in an and expression" - {
      class HasLengthMethod {
        def length(): Int = 2
      }
      val hasLengthMethod = new HasLengthMethod
      hasLengthMethod should { have length (2) and equal (hasLengthMethod) }
      val caught = intercept[AssertionError] {
        hasLengthMethod shouldNot { have length (2) and equal (hasLengthMethod) }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should work with any arbitrary object that has a parameterless length method in an and expression" - {
      class HasLengthMethod {
        def length: Int = 2
      }
      val hasLengthMethod = new HasLengthMethod
      hasLengthMethod should { have length (2) and equal (hasLengthMethod) }
      val caught = intercept[AssertionError] {
        hasLengthMethod shouldNot { have length (2) and equal (hasLengthMethod) }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should work with any arbitrary object that has a length field in an and expression" - {
      class HasLengthField {
        val length: Int = 2
      }
      val hasLengthField = new HasLengthField
      hasLengthField should { have length (2) and equal (hasLengthField) }
      val caught = intercept[AssertionError] {
        hasLengthField shouldNot { have length (2) and equal (hasLengthField) }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should give an AssertionError with an arbitrary object that has no length member in an and expression" - {
      class HasNoLength {
        val lengthiness: Int = 2
      }
      val hasNoLength = new HasNoLength
      val caught1 = intercept[AssertionError] {
        hasNoLength should { have length (2) and equal (hasNoLength) }
      }
      val expectedSubstring = "used with an object that had neither a public field or method named 'length'"
      assert(caught1.getMessage.indexOf(expectedSubstring) != -1)
      val caught2 = intercept[AssertionError] {
        hasNoLength shouldNot { have length (2) and equal (hasNoLength) }
      }
      assert(caught2.getMessage.indexOf(expectedSubstring) != -1)
    }
  }

  "Any object with a getLength method" -- {
    "should be usable with 'should have length N' syntax" - {
      class PurpleElephant {
        def getLength(): Int = 7
      }
      val bob = new PurpleElephant
      bob should have length 7
    }
  }

  "Any object with a length field" -- {
    "should be usable with 'should have length N' syntax" - {
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

  "Any object with a length method" -- {
    "should be usable with 'should have length N' syntax" - {
      class PurpleElephant {
        def length(): Int = 7
      }
      val bob = new PurpleElephant
      bob should have length 8989
    }
  }

  "The be theSameInstanceAs syntax" -- {

    val string = "Hi"
    val obj: AnyRef = string
    val otherString = new String("Hi")

    "should do nothing if the two objects are the same" - {
      string should be theSameInstanceAs (string)
      obj should be theSameInstanceAs (string)
      string should be theSameInstanceAs (obj)
      otherString shouldNot be theSameInstanceAs (string)
    }

    "should throw AssertionError if the two objects are not the same" - {
      val caught1 = intercept[AssertionError] {
        string shouldNot be theSameInstanceAs (string)
      }
      val caught2 = intercept[AssertionError] {
        obj shouldNot be theSameInstanceAs (string)
      }
      val caught3 = intercept[AssertionError] {
        string shouldNot be theSameInstanceAs (obj)
      }
      val caught4 = intercept[AssertionError] {
        otherString should be theSameInstanceAs (string)
      }
      assert(true) // TODO: test the failure message
    }
  }

  "The include substring syntax" -- {

    val decimal = """(-)?(\d+)(\.\d*)?"""
    val decimalRegex = """(-)?(\d+)(\.\d*)?""".r

    "should do nothing if the string includes the expected substring" - {
      val string = "Four score and seven years ago,..."
      string should include substring ("seven")
      string should include substring ("Four")
      string should include substring (",...")
      string shouldNot include substring ("on this continent")
      "4 score and seven years ago" should include regex (decimal)
      "Four score and 7 years ago" should include regex (decimal)
      "4.0 score and seven years ago" should include regex (decimal)
      "Four score and 7.0 years ago" should include regex (decimal)
      "Four score and 7.0" should include regex (decimal)
      string shouldNot include regex (decimal)
    }

    "should throw AssertionError if the string does not include the expected substring" - {
      val string = "Four score and seven years ago,..."
      val caught1 = intercept[AssertionError] {
        string shouldNot include substring ("seven")
      }
      assert(caught1.getMessage === "\"Four score and seven years ago,...\" included substring \"seven\"")
      val caught2 = intercept[AssertionError] {
        string shouldNot include substring ("Four")
      }
      assert(caught2.getMessage === "\"Four score and seven years ago,...\" included substring \"Four\"")
      val caught3 = intercept[AssertionError] {
        string shouldNot include substring (",...")
      }
      assert(caught3.getMessage === "\"Four score and seven years ago,...\" included substring \",...\"")
      val caught4 = intercept[AssertionError] {
        string should include substring ("on this continent")
      }
      assert(caught4.getMessage === "\"Four score and seven years ago,...\" did not include substring \"on this continent\"")
    }
  }

  "The should be >/>=/</<= syntax" -- {
    "should do nothing if the specified relation is true" - {
      val one = 1
      one should be < (7)
      one should be > (0)
      one should be <= (7)
      one should be >= (0)
      one should be <= (1)
      one should be >= (1)
      one shouldNot be < (0)
      one shouldNot be > (9)
      one shouldNot be <= (-4)
      one shouldNot be >= (21)
    }
    "should throw AssertionError if the specified relation is not true" - {
      val one = 1
      val caught1 = intercept[AssertionError] {
        one shouldNot be < (7)
      }
      assert(caught1.getMessage === "1 was less than 7")

      val caught2 = intercept[AssertionError] {
        one shouldNot be > (0)
      }
      assert(caught2.getMessage === "1 was greater than 0")

      val caught3 = intercept[AssertionError] {
        one shouldNot be <= (7)
      }
      assert(caught3.getMessage === "1 was less than or equal to 7")

      val caught4 = intercept[AssertionError] {
        one shouldNot be >= (0)
      }
      assert(caught4.getMessage === "1 was greater than or equal to 0")

      val caught5 = intercept[AssertionError] {
        one shouldNot be <= (1)
      }
      assert(caught5.getMessage === "1 was less than or equal to 1")

      val caught6 = intercept[AssertionError] {
        one shouldNot be >= (1)
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

  "The floating point numbers when compared with equals" -- {
    "should do nothing if the floating point number is exactly equal to the specified value" - {
      val sevenDotOh = 7.0
      // sevenDotOh should be (7.0 exactly)
      sevenDotOh should be (7.0)
      sevenDotOh should equal (7.0)
      // sevenDotOh shouldNot be (7.0001 exactly)
      sevenDotOh shouldNot be (7.0001)

      val sixDotOh: Float = 6.0f
      // sixDotOh should be (6.0 exactly)
      sixDotOh should be (6.0)
      sixDotOh should equal (6.0)
      // sixDotOh shouldNot be (6.0001 exactly)
      sixDotOh shouldNot be (6.0001)
    }

    "should throw AssertionError if the floating point number is not exactly equal to the specified value" - {
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
        sevenDotOh shouldNot be (7.0001)
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
        sixDotOh shouldNot be (6.0001f)
      }
      assert(caught6.getMessage === "6.0001 was 6.0001")
    }
  }

  "The floating point 'plusOrMinus' operator" --{
    "should do nothing if the floating point number is within the specified range" -{
      val sevenDotOh = 7.0
      sevenDotOh should be (7.1 plusOrMinus 0.2)
      sevenDotOh should be (6.9 plusOrMinus 0.2)
      sevenDotOh shouldNot be (7.5 plusOrMinus 0.2)
      sevenDotOh shouldNot be (6.5 plusOrMinus 0.2)
      val minusSevenDotOh = -7.0
      minusSevenDotOh should be (-7.1 plusOrMinus 0.2)
      minusSevenDotOh should be (-6.9 plusOrMinus 0.2)
      minusSevenDotOh shouldNot be (-7.5 plusOrMinus 0.2)
      minusSevenDotOh shouldNot be (-6.5 plusOrMinus 0.2)
    }

    "should throw AssertionError if the floating point number is not within the specified range" - {
      val sevenDotOh = 7.0
      val caught1 = intercept[AssertionError] {
        sevenDotOh shouldNot be (7.1 plusOrMinus 0.2)
      }
      assert(caught1.getMessage === "7.0 was 7.1 plus or minus 0.2")

      val caught2 = intercept[AssertionError] {
        sevenDotOh shouldNot be (6.9 plusOrMinus 0.2)
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
        minusSevenDotOh shouldNot be (-7.1 plusOrMinus 0.2)
      }
      assert(caught5.getMessage === "-7.0 was -7.1 plus or minus 0.2")

      val caught6 = intercept[AssertionError] {
        minusSevenDotOh shouldNot be (-6.9 plusOrMinus 0.2)
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

/*
  "The shouldThrow method" -- {

    "should do nothing if the expected exception is thrown" - {

      // "Howdy".charAt(-1) shouldThrow [StringIndexOutOfBoundsException]

      theBlock { "Howdy".charAt(-1) } shouldThrow classOf[StringIndexOutOfBoundsException]
      theBlock {
        "Howdy".charAt(-1)
      } shouldThrow (classOf[StringIndexOutOfBoundsException]
      theBlock { "Howdy".charAt(-1); println("hi") } shouldThrow classOf[StringIndexOutOfBoundsException]
      theBlock { "Howdy".charAt(-1); println("hi") } shouldThrow anException
      "Howdy".charAt(1) shouldNotThrow (anException)
      // "Howdy".charAt(1) shouldNotThrow classOf[IllegalArgumentException] This doesn't compile, by design
      val caught = intercept[AssertionError] {
        "Howdy".charAt(-1) shouldNotThrow (anException)
      }
      assert(caught.getMessage === "Expected no exception to be thrown, but java.lang.StringIndexOutOfBoundsException was thrown.")
    }

    "should do nothing if an instance of a subclass of the specified expected exception class is thrown" - {
      class MyException extends RuntimeException
      class MyExceptionSubClass extends MyException
      theBlock { throw new MyException } shouldThrow (classOf[MyException]
      theBlock { throw new MyException } shouldThrow (anException)
      theBlock { throw new MyExceptionSubClass } shouldThrow (classOf[MyException]
      theBlock { throw new MyExceptionSubClass } shouldThrow (anException)
      // Try with a trait
      trait MyTrait
      class AnotherException extends RuntimeException with MyTrait
      theBlock { throw new AnotherException } shouldThrow (classOf[MyTrait])
      theBlock { throw new AnotherException } shouldThrow (anException)
    }

    "should return the caught exception" - {
      val e = new RuntimeException
      val result1 = theBlock { throw e } shouldThrow (classOf[RuntimeException]
      result1 should be theSameInstanceAs (e)
      val result2 = theBlock { throw e } shouldThrow (anException)
      result2 should be theSameInstanceAs (e)
    }

    "should throw AssertionError if the expected exception is not thrown" - {

      val caught1 = intercept[AssertionError] {
        "Howdy".charAt(1) shouldThrow (classOf[StringIndexOutOfBoundsException]
      } // TODO: Try shouldNotThrow. Try throwing wrong exception. See tests for intercept.
      assert(caught1.getMessage === "Expected exception java.lang.StringIndexOutOfBoundsException to be thrown, but no exception was thrown.")

      val caught2 = intercept[AssertionError] {
        "Howdy".charAt(-1) shouldThrow (classOf[ArrayIndexOutOfBoundsException]
      }
      assert(caught2.getMessage === "Expected exception java.lang.ArrayIndexOutOfBoundsException to be thrown, but java.lang.StringIndexOutOfBoundsException was thrown.")
    }
  }
*/

  "The fullyMatch regex syntax" -- {

    val decimal = """(-)?(\d+)(\.\d*)?"""
    val decimalRegex = """(-)?(\d+)(\.\d*)?""".r

    "should do nothing if the string matches the regular expression specified as a string" - {
      "1.7" should fullyMatch regex ("1.7")
      "1.7" should fullyMatch regex (decimal)
      "-1.8" should fullyMatch regex (decimal)
      "8" should fullyMatch regex (decimal)
      "1." should fullyMatch regex (decimal)
      "eight" shouldNot fullyMatch regex (decimal)
      "1.eight" shouldNot fullyMatch regex (decimal)
      "one.8" shouldNot fullyMatch regex (decimal)
      "1.8-" shouldNot fullyMatch regex (decimal)
      "1.7" should { fullyMatch regex (decimal) and equal ("1.7") }
      "1.7++" shouldNot { fullyMatch regex (decimal) and equal ("1.7") }
    }

    "should do nothing if the string matches the regular expression specified as a Regex" - {
      "1.7" should fullyMatch regex (decimalRegex)
      "-1.8" should fullyMatch regex (decimalRegex)
      "8" should fullyMatch regex (decimalRegex)
      "1." should fullyMatch regex (decimalRegex)
      "eight" shouldNot fullyMatch regex (decimalRegex)
      "1.eight" shouldNot fullyMatch regex (decimalRegex)
      "one.8" shouldNot fullyMatch regex (decimalRegex)
      "1.8-" shouldNot fullyMatch regex (decimalRegex)
      "1.7" should { fullyMatch regex (decimalRegex) and equal ("1.7") }
      "1.7++" shouldNot { fullyMatch regex (decimalRegex) and equal ("1.7") }
    }

    "should throw AssertionError if the string does not match the regular expression specified as a string" - {
      val caught1 = intercept[AssertionError] {
        "1.7" shouldNot fullyMatch regex ("1.7")
      }
      assert(caught1.getMessage === "\"1.7\" fully matched the regular expression 1.7")

      val caught2 = intercept[AssertionError] {
        "1.7" shouldNot fullyMatch regex (decimal)
      }
      assert(caught2.getMessage === "\"1.7\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught3 = intercept[AssertionError] {
        "-1.8" shouldNot fullyMatch regex (decimal)
      }
      assert(caught3.getMessage === "\"-1.8\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught4 = intercept[AssertionError] {
        "8" shouldNot fullyMatch regex (decimal)
      }
      assert(caught4.getMessage === "\"8\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught5 = intercept[AssertionError] {
        "1." shouldNot fullyMatch regex (decimal)
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
        "1.7" shouldNot { fullyMatch regex (decimal) and equal ("1.7") }
      }
      assert(caught10.getMessage === "\"1.7\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?, and \"1.7\" equaled \"1.7\"")

      val caught11 = intercept[AssertionError] {
        "1.7++" should { fullyMatch regex (decimal) and equal ("1.7") }
      }
      assert(caught11.getMessage === "\"1.7++\" did not fully match the regular expression (-)?(\\d+)(\\.\\d*)?")
    }

    "should throw AssertionError if the string does not match the regular expression specified as a Regex" - {
      val caught2 = intercept[AssertionError] {
        "1.7" shouldNot fullyMatch regex (decimalRegex)
      }
      assert(caught2.getMessage === "\"1.7\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught3 = intercept[AssertionError] {
        "-1.8" shouldNot fullyMatch regex (decimalRegex)
      }
      assert(caught3.getMessage === "\"-1.8\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught4 = intercept[AssertionError] {
        "8" shouldNot fullyMatch regex (decimalRegex)
      }
      assert(caught4.getMessage === "\"8\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?")

      val caught5 = intercept[AssertionError] {
        "1." shouldNot fullyMatch regex (decimalRegex)
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
        "1.7" shouldNot { fullyMatch regex (decimalRegex) and equal ("1.7") }
      }
      assert(caught10.getMessage === "\"1.7\" fully matched the regular expression (-)?(\\d+)(\\.\\d*)?, and \"1.7\" equaled \"1.7\"")

      val caught11 = intercept[AssertionError] {
        "1.7++" should { fullyMatch regex (decimalRegex) and equal ("1.7") }
      }
      assert(caught11.getMessage === "\"1.7++\" did not fully match the regular expression (-)?(\\d+)(\\.\\d*)?")
    }
  }

  "The shouldMatch method" -- {

    "should do nothing if the object matches such that the partial function results in true" - {
      List(1, 2, 3) shouldMatch {
        case 1 :: _ :: 3 :: Nil => true
        case _ => false
      }
      List(1, 2, 3) shouldMatch {
        case 1 :: _ :: 3 :: Nil => true
      }
      List(1, 2, 3) shouldMatch {
        case 1 :: a :: 3 :: Nil => a == 2
      }
      List(1, 2, 4) shouldNotMatch {
        case 1 :: _ :: 3 :: Nil => true
        case _ => false
      }
      List(1, 2, 4) shouldNotMatch {
        case 1 :: _ :: 3 :: Nil => true
      }
      List(1, 2, 3) shouldNotMatch {
        case 1 :: a :: 3 :: Nil => a == 5
      }
    }

    "should throw AssertionError if the object matches, but the partial function results in false" - {
      val caught = intercept[AssertionError] {
        List(1, 2, 4) shouldMatch {
          case 1 :: _ :: 3 :: Nil => true
          case _ => false
        }
      }
      assert(caught.getMessage === "The value List(1, 2, 4) matched a case in the specified partial function, but the result was false")
    }

    "should throw AssertionError if the object does not match any case" - {
      val caught = intercept[AssertionError] {
        List(1, 2, 4) shouldMatch {
          case 1 :: _ :: 3 :: Nil => true
        }
      }
      assert(caught.getMessage === "The value List(1, 2, 4) did not match any case in the specified partial function")
    }

    "should throw AssertionError (when using shouldNot) if the object matches, and the partial function results in true" - {
      val caught = intercept[AssertionError] {
        List(1, 2, 3) shouldNotMatch {
          case 1 :: _ :: 3 :: Nil => true
          case _ => false
        }
      }
      assert(caught.getMessage === "The value List(1, 2, 3) matched a case in the specified partial function, and the result was true")
    }
  }
} // THE END

    /*
     // After should/shouldNot, if an even number of tokens, you need parens on the last thing.
     // If an odd number of tokens, you need not put parens on the last thing, but usually could if you wanted to.

     map should have key 8 // DONE haveKey(8)
     map shouldNot have key 8 // DONE
     map should { have key 8 and equal (Map(8 -> "eight")) } // DONE

     map should have value "eleven" // DONE haveValue("eleven") haveValue("eleven")
     map shouldNot have value "eleven" // DONE
 
     iterable should contain element 42 // DONE contain(42)   containElement(42)
     iterable shouldNot contain element 42 // DONE
     iterable should_not contain element 42 // DONE
     assert(iterable contains 42) // DONE

     collection should have size 3 // DONE     haveSize(3)
     collection shouldNot have size 3 // DONE

     string should have length 0 // DONE      haveLength(0)
     string shouldNot have length 0 // DONE

     array should have length 9 // DONE
     array shouldNot have length 9 // DONE

     option should equal (Some(1)) // DONE
     option shouldEqual Some(1) // DONE
     option shouldNotEqual Some(1) // DONE

     object should be a 'file // DONE
     object shouldNot be a 'file // DONE
     object should be an 'openBook // DONE
     object shouldNot be an 'openBook // DONE
     object should be ('hidden) // DONE
     object shouldNot be ('hidden) // DONE
     val file = 'file // DONE
     val openBook = 'openBook// DONE
     val hidden = 'hidden // DONE
     val empty = 'empty // DONE
     object should be (empty) // DONE
     object should be (hidden) // DONE
     object should be a file // DONE
     object shouldNot be an openBook // DONE
     object shouldNot be an openBook // DONE

     string should startWith substring "something" // DONE   startWith("something")
     string shouldNot startWith substring "something" // DONE
     string should endWith substring "something" // DONE  endWith("something")
     string shouldNot endWith substring "something" // DONE

     buf.length should be (20) // DONE

     string should { not { have length 7 } and (startWith substring "Hello") } // DONE

     map should { have key 8 andNot equal (Map(8 -> "eight")) } // DONE

     list should be (Nil) // DONE
     list shouldNot be (Nil) // DONE
     list should equal (Nil) // DONE
     list shouldNot equal (Nil) // DONE

     object should be (null) // DONE
     object shouldNot be (null) // DONE

     option should be (None) // DONE

     list should beNil // DONE
     list shouldNot beNil // DONE

     object should beNull // DONE
     object shouldNot beNull // DONE

     option should beNone // DONE
     option should beDefined // DONE

     // beEmpty is actually probably a Matcher[AnyRef], and it first does a pattern match. If it
     // is a String, then it checks for length is zero. Otherwise it does the already-written reflection
     // stuff to look for empty and isEmpty.
     iterable should beEmpty // DONE
     iterable shouldNot beEmpty // DONE
     string should beEmpty // DONE
     string shouldNot beEmpty // DONE
     object should beEmpty // DONE
     something should beEmpty // DONE

     option should beSome("hi") // DONE

     boolean should beTrue // DONE
     boolean should beFalse // DONE

     object should be theSameInstanceAs anotherObjectReference // DONE
     object shouldNot be theSameInstanceAs anotherObjectReference // DONE
     object should beTheSameInstanceAs(anotherObjectReference) // DONE

     string should include substring "bob" // DONE
     string shouldNot include substring "bob" // DONE

     string shouldNot include("bob") // DONE

     ordered should be > 7 // DONE
     ordered should be >= 7 // DONE
     ordered should be < 7 // DONE
     ordered should be <= 7 // DONE ordered.should(be.<=(7))

     floatingPointNumber should be (7.0 plusOrMinus 0.01) // DONE

     "Howdy".charAt(-1) shouldThrow (classOf[StringIndexOutOfBoundsException] // DONE
     theBlock { "Howdy".charAt(-1) } shouldThrow classOf[StringIndexOutOfBoundsException] // DONE
     theBlock { throw new Something } shouldThrow classOf[StringIndexOutOfBoundsException] // DONE

     val anException = classOf[Throwable] // DONE
     theBlock { throw new Something } shouldNotThrow anException // DONE
     theBlock { throw new Something } shouldThrow anException // DONE

     string should fullyMatch regex """[a-zA-Z_]\w*""" // DONE
     string should include substring "howdy" // DONE
     string should startWith substring "howdy" // DONE string.should(startWith).substring("howdy")
     string should endWith substring "howdy" // DONE
     string should include regex "howdy" // DONE
     string should startWith regex "howdy" // DONE
     string should endWith regex "howdy" // DONE

     // This could be nice. It's pretty clear, and a pattern match is
     // sometimes the most concise way to check an object.
     object shouldMatch { // DONE
       case 1 :: _ :: 3 :: Nil => true
       case _ => false
     }

     // for symmetry:
     object shouldNotMatch { // DONE
       case 1 :: _ :: 3 :: Nil => true
       case _ => false
     }

     THINGS I WON'T DO

     // I cut this one. Just use (7.0)
     floatingPointNumber should be (7.0 exactly) // DONE

     // Decided not to allow this
     byNameParam shouldNotThrow classOf[IllegalArgumentException]

     // Although string should startWith substring "howdy" also looks verbose, it is preferable I
     // think to these parenthesized versions:
     string should fullyMatch ("""[a-zA-Z_]\w*""")
     string should include ("howdy")
     string should include ("howdy")
     string should startWith ("howdy")
     string should endWith ("howdy")
     string should startWith ("howdy")
     string should endWith ("howdy")

     // This doesn't work
     object should be anInstanceOf[Something] 
     object shouldNot be anInstanceOf[Something] 

     // Had to do this instead:
     object should be anInstanceOf classOf[String]

     // That's ugly, and it has trouble with arrays and value types, because of boxing
     // So instead, I'll let them do this:
     anyObject1.isInstanceOf[Integer] should beTrue

     // Another one I won't do for now:
     string should equalIgnoringCase ("happy")
     string should equalIgnoringCase ("happy")
     string shouldEqual ignoring case "happy"
     string.toLowerCase shouldEqual "happy" // Seems like this should suffice
     string1.toLowerCase shouldEqual string2.toLowerCase

     // For now I won't do this:

     // Ah, to support this, the should method needs to return T, the left value, not Unit. Then
     // I could chain these. But would that cause problems elsewhere that this isn't Unit? Oh, the one should method
     // that would do this is the one that takes whatever the beSome type is.
     // It needn't return T. It should return a very special type that already has a whoseValue method, and that method
     // returns the payload of the option.
     option should beSome[String] whoseValue should startWith ("prefix")

     // Because this is about as good:
     option should beDefined
     option.get should startWith ("prefix")

     // And since this is a test, which should fail if the thing isn't defined, they can just do this:
     option.get should startWith ("prefix")

     // I could add this later, but for now, it's a bit wordy. I'd just let them say equal "(Some(1))" or "be (Some(1))"
     option should be equalTo Some(1)

     // I could add this one later, but don't need it for this release. Not
     // sure how often this would get used.
     iterable should contain elements (42, 43, 59)

     // I won't do something like asNullAs, at least for now. Can just do this:
     if (object1 == null)
       object2 should beNull
     else 
       object2 shouldNot beNull

     // I'm not going to do the shouldChange one in this go round, maybe never
     val name = "Bob"
     name.toLowerCase shouldChange name from "Bob" to "bob"

     person.happyBirthday shouldChange person.age from 32 to 33

     val buf = new StringBuffer("Hello,")
     buf.append(" world!") shouldChange buf.length from 6 to 13
     buf.append(" world!") shouldChange buf.length to 20

     // I think without shouldChange, the code is a bit less obvious:
     val buf = new StringBuffer("Hello,")
     buf.length should equal (6)
     buf.append(" world!")
     buf.length should equal (13)
     buf.append(" world!")
     buf.length should equal (20)

     (Both the code to the left and right of shouldChange could be by name params)

     byNameParam shouldNotChange byNameParam // can't use from or to on this
     // Can't use from or to on this, it will just grab the 2nd byNameParam
     // do the first one, grab the 2nd one again and compare it with its
     // earlier value.

     // End of shouldChange examples

     // I won't do something like aka

     // Don't need an xor. To be consistent, would want an xorNot, which is very confusing. So
     // wouldn't do that, and then it would be inconsistent with and and or. It is also not
     // spoken English, where as and, or, and not are.

     val beesKnees = 'beesKnees // NO
     object should be the 'beesKnees // NO
     object shouldNot be the 'beesKnees // NO
     object should 'beHidden // NO
     object shouldNot 'beHidden // NO
     object should 'beEmpty // NO
     val beHidden = 'beHidden // NO
     val beEmpty = 'beEmpty // NO
     object should beHidden // NO

     string should startWith prefix "something" // NO
     string shouldNot startWith prefix "something" // NO
     string should endWith suffix "something" // NO
     string shouldNot endWith suffix "something" // NO

     string should have prefix "something" // NO
     string shouldNot have prefix "something" // NO
     string should have suffix "something" // NO
     string shouldNot have suffix "something" // NO

     string should start With "something" // NO
     string shouldNot start With "something" // NO
     string should end With "something" // NO
     string shouldNot end With "something" // NO

     string should start `with` "something" // NO
     string shouldNot start `with` "something" // NO
     string should end `with` "something" // NO
     string shouldNot end `with` "something" // NO

     string should equalIgnoringCase ("happy") // NO
     string shouldNot equalIgnoringCase ("happy") // NO

     // Not going to the the satisfy predicate one. Can use other forms.
     object should satisfy predicate { (list: List) => list.filter(_ startsWith "hi").size == 3 }
     // could be:
     list.filter(_ startsWith "hi").size should equal (3)
     // or:
     val numberOfStringsThatStartWithHi = list.filter(_ startsWith "hi").size
     numberOfStringsThatStartWithHi should equal (3)
     // or if they wanted to reuse the same predicate a bunch of times, as in:
     val pred = (list: List) => list.filter(_ startsWith "hi").size
     firstList should satisfy predicate pred
     secondList should satisfy precicate pred
     // Could do this simply like this:
     val pred = (list: List) => list.filter(_ startsWith "hi").size
     // or nicer:
     def pred(list: List) = list.filter(_ startsWith "hi").size
     pred(firstList) should be 3
     pred(secondList) should be 3
     // Or something like this:
     object should satisfy ((arg: Int) => arg > 12)
     // Can be done more readably like this:
     arg should be > 12

     I CAN'T QUITE FOLLOW WHAT I WAS AFTER, EXCEPT I WANTED IT TO BE EASIER TO MAKE A MATCHER, BUT 
     I DON'T THINK ANY OF THIS IS WORTH THE WEIGHT

     It might be nice to let people say this:

  def key[K, V](expectedKey: K): Matcher[Map[K, V]] =
    (left: Map[K, V]) => {
      MatcherResult(
        left.contains(expectedKey),
        Resources("didNotHaveKey", left, expectedKey),
        Resources("hadKey", left, expectedKey)
      ) 
    }

    Instead of this:

    new Matcher[Map[K, V]] {
      def apply(left: Map[K, V]) =
        MatcherResult(
          left.contains(expectedKey),
          Resources("didNotHaveKey", left, expectedKey),
          Resources("hadKey", left, expectedKey)
        )
    }

    Since there's a result type that's fixed at MatcherResult, I could have an implicit conversion, something like:
   
    def functionToMatcher(f: (T) => MatcherResult): Matcher[T] = // here I can create a matcher that takes a function, and delegates
      // to that function in the apply method. This way it would pick up the and, or, andNot, and orNot methods.

    Actually, I think this doesn't rise to the occasion of an implicit, but I could provide a trait or class that takes 
    a function, like, no, a factory method, like:

  def key[K, V](expectedKey: K) =
    Matcher[Map[K, V]] {
      (left: Map[K, V]) => {
        MatcherResult(
          left.contains(expectedKey),
          Resources("didNotHaveKey", left, expectedKey),
          Resources("hadKey", left, expectedKey)
        ) 
      }
    }

    Matcher {
      (left: Map[K, V]) => {
        MatcherResult(
          left.contains(expectedKey),
          Resources("didNotHaveKey", left, expectedKey),
          Resources("hadKey", left, expectedKey)
        ) 
      }
    }

    Nah, this doesn't look nice, and it is less explicit.
    */


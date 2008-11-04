package org.scalatest

class MatcherSpec extends Spec with Matchers {

  "The equal matcher" -- {

    "should do nothing when equal" - {
      1 should equal (1)
      val option = Some(1)
      option should equal (Some(1)) 
    }

    "should throw an assertion error when not equal" - {
      intercept(classOf[AssertionError]) {
        1 should equal (2)
      }
    }

    "should do nothing when not equal and used with shouldNot" - {
      1 shouldNot equal (2)
      val option = Some(1)
      option shouldNot equal (Some(2)) 
    }

    "should throw an assertion error when equal but used with shouldNot" - {
      intercept(classOf[AssertionError]) {
        1 shouldNot equal (1)
      }
    }
  }

  "The shouldEqual method" -- {

    "should do nothing when equal" - {
      1 shouldEqual 1
      val option = Some(1)
      option shouldEqual Some(1)
    }

    "should throw an assertion error when not equal" - {
      intercept(classOf[AssertionError]) {
        1 shouldEqual 2
      }
    }

    "should do nothing when not equal and used with shouldNot" - {
      1 shouldNotEqual 2
      val option = Some(1)
      option shouldNotEqual Some(2)
    }

    "should throw an assertion error when equal but used with shouldNot" - {
      intercept(classOf[AssertionError]) {
        1 shouldNotEqual 1
      }
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
        intercept(classOf[AssertionError]) {
          false should be (true)
        }
      }
    }

    "(for null)" -- {

      "should do nothing when null is compared to null" - {
        val o: String = null
        o should be (null)
        o should equal (null)
      }

      "should throw an assertion error when non-null compared to null" - {
        intercept(classOf[AssertionError]) {
          val o = "Helloooooo"
          o should be (null)
        }
      }

      "should do nothing when non-null is compared to not null" - {
        val o = "Helloooooo"
        o should not { be (null) }
        o shouldNot be (null)
      }

      "should throw an assertion error when null compared to not null" - {
        intercept(classOf[AssertionError]) {
          val o: String = null
          o should not { be (null) }
        }
        intercept(classOf[AssertionError]) {
          val o: String = null
          o shouldNot be (null)
        }
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
        val caught1 = intercept(classOf[AssertionError]) {
          nonEmptyList should be (Nil)
        }
        assert(caught1.getMessage === "List(Helloooooo) did not equal List()")
        val caught2 = intercept(classOf[AssertionError]) {
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
        intercept(classOf[AssertionError]) {
          emptyList should not { be (Nil) }
        }
        intercept(classOf[AssertionError]) {
          emptyList shouldNot be (Nil)
        }
        intercept(classOf[AssertionError]) {
          emptyList should not { equal (Nil) }
        }
        intercept(classOf[AssertionError]) {
          emptyList shouldNot equal (Nil)
        }
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
        val caught1 = intercept(classOf[AssertionError]) {
          someString should be (None)
        }
        assert(caught1.getMessage === "Some(Helloooooo) was not None")
        val caught2 = intercept(classOf[AssertionError]) {
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
        intercept(classOf[AssertionError]) {
          none should not { be (None) }
        }
        intercept(classOf[AssertionError]) {
          none shouldNot be (None)
        }
        intercept(classOf[AssertionError]) {
          none should not { equal (None) }
        }
        intercept(classOf[AssertionError]) {
          none shouldNot equal (None)
        }
        val noString: Option[String] = None
        intercept(classOf[AssertionError]) {
          noString should not { be (None) }
        }
        intercept(classOf[AssertionError]) {
          noString shouldNot be (None)
        }
        intercept(classOf[AssertionError]) {
          noString should not { equal (None) }
        }
        intercept(classOf[AssertionError]) {
          noString shouldNot equal (None)
        }
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

      "should call isEmpty when passed 'empty" - {
        val emptySet = Set[Int]()
        emptySet should be ('empty)
        val nonEmptySet = Set(1, 2, 3)
        nonEmptySet should not { be ('empty) }
      }

      "should be invokable from be a Symbol, be an Symbol, and be the Symbol" - {
        val emptySet = Set()
        emptySet should be a 'empty
        emptySet should be an 'empty
        val nonEmptySet = Set(1, 2, 3)
        nonEmptySet should not { be a 'empty }
        nonEmptySet should not { be an 'empty }
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
        val ex1 = intercept(classOf[IllegalArgumentException]) {
          (new EmptyMock) should be ('empty)
        }
        ex1.getMessage should equal ("EmptyMock has neither an empty or an isEmpty method.")
        val ex2 = intercept(classOf[IllegalArgumentException]) {
          (new NonEmptyMock) should not { be ('empty) }
        }
        ex2.getMessage should equal ("NonEmptyMock has neither an empty or an isEmpty method.")
        val ex3 = intercept(classOf[IllegalArgumentException]) {
          (new NonEmptyMock) shouldNot be ('empty)
        }
        ex3.getMessage should equal ("NonEmptyMock has neither an empty or an isEmpty method.")
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
        val ex1 = intercept(classOf[IllegalArgumentException]) {
          (new EmptyMock) should be ('empty)
        }
        ex1.getMessage should equal ("EmptyMock has both an empty and an isEmpty method.")
        val ex2 = intercept(classOf[IllegalArgumentException]) {
          (new NonEmptyMock) should not { be ('empty) }
        }
        ex2.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method.")
        val ex3 = intercept(classOf[IllegalArgumentException]) {
          (new NonEmptyMock) shouldNot be ('empty)
        }
        ex3.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method.")
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
        val caught = intercept(classOf[AssertionError]) {
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
        val caught = intercept(classOf[AssertionError]) {
          1 shouldNot be (1)
        }
        assert(caught.getMessage === "1 was 1")
      }
    }
  }

  "the beEmpty matcher" -- {

    "should call isEmpty" - {
      val emptySet = Set[Int]()
      emptySet should beEmpty
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
      val ex1 = intercept(classOf[IllegalArgumentException]) {
        (new EmptyMock) should beEmpty
      }
      ex1.getMessage should equal ("EmptyMock has neither an empty or an isEmpty method.")
      val ex2 = intercept(classOf[IllegalArgumentException]) {
        (new NonEmptyMock) should not { beEmpty }
      }
      ex2.getMessage should equal ("NonEmptyMock has neither an empty or an isEmpty method.")
      val ex3 = intercept(classOf[IllegalArgumentException]) {
        (new NonEmptyMock) shouldNot beEmpty
      }
      ex3.getMessage should equal ("NonEmptyMock has neither an empty or an isEmpty method.")
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
      val ex1 = intercept(classOf[IllegalArgumentException]) {
        (new EmptyMock) should beEmpty
      }
      ex1.getMessage should equal ("EmptyMock has both an empty and an isEmpty method.")
      val ex2 = intercept(classOf[IllegalArgumentException]) {
        (new NonEmptyMock) should not { beEmpty }
      }
      ex2.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method.")
      val ex3 = intercept(classOf[IllegalArgumentException]) {
        (new NonEmptyMock) shouldNot beEmpty
      }
      ex3.getMessage should equal ("NonEmptyMock has both an empty and an isEmpty method.")
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
      val caught = intercept(classOf[AssertionError]) {
        "hi" should beEmpty
      }
      caught.getMessage shouldEqual "hi was not empty"
    }
  }

  "the beNull matcher" -- {
    "should do nothing when null is compared to null" - {
      val o: String = null
      o should beNull
    }

    "should throw an assertion error when non-null compared to null" - {
      intercept(classOf[AssertionError]) {
        val o = "Helloooooo"
        o should beNull
      }
    }

    "should do nothing when non-null is compared to not null" - {
      val o = "Helloooooo"
      o should not { beNull }
      o shouldNot beNull
    }

    "should throw an assertion error when null compared to not null" - {
      intercept(classOf[AssertionError]) {
        val o: String = null
        o should not { beNull }
      }
      intercept(classOf[AssertionError]) {
        val o: String = null
        o shouldNot beNull
      }
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
      val caught1 = intercept(classOf[AssertionError]) {
        nonEmptyList should beNil
      }
      assert(caught1.getMessage === "List(Helloooooo) was not List()")
    }

    "should do nothing when non-null is compared to not null" - {
      val nonEmptyList = List("Helloooooo")
      nonEmptyList should not { beNil }
      nonEmptyList shouldNot beNil
    }

    "should throw an assertion error when null compared to not null" - {
      val emptyList = List[String]()
      intercept(classOf[AssertionError]) {
        emptyList should not { beNil }
      }
      intercept(classOf[AssertionError]) {
        emptyList shouldNot beNil
      }
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
        val caught1 = intercept(classOf[AssertionError]) {
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
        intercept(classOf[AssertionError]) {
          none should not { beNone }
        }
        intercept(classOf[AssertionError]) {
          none shouldNot beNone
        }
        val noString: Option[String] = None
        intercept(classOf[AssertionError]) {
          noString should not { beNone }
        }
        intercept(classOf[AssertionError]) {
          noString shouldNot beNone
        }
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
      intercept(classOf[AssertionError]) {
        none should beDefined
      }
      val option: Option[Int] = None
      intercept(classOf[AssertionError]) {
        option should beDefined
      }
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
      val ex1 = intercept(classOf[IllegalArgumentException]) {
        (new DefinedMock) should beDefined
      }
      ex1.getMessage should equal ("DefinedMock has neither a defined or an isDefined method.")
      val ex2 = intercept(classOf[IllegalArgumentException]) {
        (new NonDefinedMock) should not { beDefined }
      }
      ex2.getMessage should equal ("NonDefinedMock has neither a defined or an isDefined method.")
      val ex3 = intercept(classOf[IllegalArgumentException]) {
        (new NonDefinedMock) shouldNot beDefined
      }
      ex3.getMessage should equal ("NonDefinedMock has neither a defined or an isDefined method.")
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
      val ex1 = intercept(classOf[IllegalArgumentException]) {
        (new DefinedMock) should beDefined
      }
      ex1.getMessage should equal ("DefinedMock has both a defined and an isDefined method.")
      val ex2 = intercept(classOf[IllegalArgumentException]) {
        (new NonDefinedMock) should not { beDefined }
      }
      ex2.getMessage should equal ("NonDefinedMock has both a defined and an isDefined method.")
      val ex3 = intercept(classOf[IllegalArgumentException]) {
        (new NonDefinedMock) shouldNot beDefined
      }
      ex3.getMessage should equal ("NonDefinedMock has both a defined and an isDefined method.")
    }

    "should access an 'defined' val" - {
      class DefinedMock {
        val defined: Boolean = true
      }
      class NonDefinedMock {
        val defined: Boolean = false
      }
      (new DefinedMock) should beDefined
      (new NonDefinedMock) should not { beDefined }
      (new NonDefinedMock) shouldNot beDefined
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
      intercept(classOf[AssertionError]) {
        none should beSome("hi")
      }
      val option: Option[Int] = None
      intercept(classOf[AssertionError]) {
        option should beSome(3)
      }
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
      val caught1 = intercept(classOf[AssertionError]) {
        falseBoolean should beTrue
      }
      caught1.getMessage shouldEqual "the boolean expression was not true"
      val caught2 = intercept(classOf[AssertionError]) {
        trueBoolean shouldNot beTrue
      }
      caught2.getMessage shouldEqual "the boolean expression was true"
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
      val caught1 = intercept(classOf[AssertionError]) {
        trueBoolean should beFalse
      }
      caught1.getMessage shouldEqual "the boolean expression was not false"
      val caught2 = intercept(classOf[AssertionError]) {
        falseBoolean shouldNot beFalse
      }
      caught2.getMessage shouldEqual "the boolean expression was false"
    }
  }

  "The not matcher" -- {
    "should do nothing when not true" - {
      1 should not { equal (2) }
    }
    "should throw an assertion error when true" - {
      intercept(classOf[AssertionError]) {
        1 should not { equal (1) }
      }
    }
    "should work at the beginning of an and expression" - {
      val string = "Hello, world!"
      string should { not { have length 7 } and startWith ("Hello") }
    }
  }

  "The shouldNot method" -- {
    "should do nothing when not true" - {
      1 shouldNot equal (2)
    }
    "should throw an assertion error when true" - {
      intercept(classOf[AssertionError]) {
        1 shouldNot equal (1)
      }
    }
  }

  "The endWith matcher" -- {
    "should do nothing when true" - {
      "Hello, world" should endWith ("world")
    }
    "should throw an assertion error when not true" - {
      val caught = intercept(classOf[AssertionError]) {
        "Hello, world" should endWith ("planet")
      }
      assert(caught.getMessage.indexOf("did not end with") != -1)
      val caught1 = intercept(classOf[AssertionError]) {
        "Hello, world" shouldNot endWith ("world")
      }
      assert(caught1.getMessage.indexOf("ended with") != -1)
    }
    "should work inside an and clause" - {
      "Hello, world" should { endWith ("world") and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and endWith ("world") }
    }
  }

  "The startWith matcher" -- {
    "should do nothing when true" - {
      "Hello, world" should startWith ("Hello")
    }
    "should throw an assertion error when not true" - {
      val caught = intercept(classOf[AssertionError]) {
        "Hello, world" should startWith ("Greetings")
      }
      assert(caught.getMessage.indexOf("did not start with") != -1)
      val caught1 = intercept(classOf[AssertionError]) {
        "Hello, world" shouldNot startWith ("Hello")
      }
      assert(caught1.getMessage.indexOf("started with") != -1)
    }
    "should work inside an and clause" - {
      "Hello, world" should { startWith ("Hello") and equal ("Hello, world") }
      "Hello, world" should { equal ("Hello, world") and startWith ("Hello") }
    }
  }

  "The and matcher" -- {

    "should do nothing when both operands are true" - {
      1 should { equal (1) and equal (2 - 1) }
    }

    "should throw AssertionError when first operands is false" - {
      val caught = intercept(classOf[AssertionError]) {
        1 should (equal (2) and equal (1))
      }
      caught.getMessage should equal ("1 did not equal 2") // because and short circuits
    }

    "should throw AssertionError when second operand is false" - {
      val caught = intercept(classOf[AssertionError]) {
        1 should (equal (1) and equal (2))
      }
      caught.getMessage should equal ("1 equaled 1, but 1 did not equal 2")
    }

    "should not execute the right matcher creation function when the left operand is false" - {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      intercept(classOf[AssertionError]) {
        // This should fail, but without applying the matcher returned by mockMatcher
        1 should { equal (2) and mockMatcher }
      }
      called should be (false)
    }

    "should execute the right matcher creation function when the left operand is true" - {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      1 should { equal (1) and mockMatcher }
      called should be (true)
    }
    "should give good failure messages when used with not" - {
      val caught1 = intercept(classOf[AssertionError]) {
        1 should (not { equal (1) } and equal (1))
      }
      caught1.getMessage should equal ("1 equaled 1") // because and short circuits
      val caught2 = intercept(classOf[AssertionError]) {
        1 should (equal (1) and not { equal (1) })
      }
      caught2.getMessage should equal ("1 equaled 1, but 1 equaled 1")
      val caught3 = intercept(classOf[AssertionError]) {
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
      val caught = intercept(classOf[AssertionError]) {
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
      val caught1 = intercept(classOf[AssertionError]) {
        1 should (not { equal (1) } or equal (2))
      }
      caught1.getMessage should equal ("1 equaled 1, and 1 did not equal 2")
      val caught2 = intercept(classOf[AssertionError]) {
        1 should (equal (2) or not { equal (1) })
      }
      caught2.getMessage should equal ("1 did not equal 2, and 1 equaled 1")
      val caught3 = intercept(classOf[AssertionError]) {
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
      val caught = intercept(classOf[AssertionError]) {
        1 should (equal (2) andNot equal (2))
      }
      caught.getMessage should equal ("1 did not equal 2") // because and short circuits
    }

    "should throw AssertionError when second operand is true" - {
      val caught = intercept(classOf[AssertionError]) {
        1 should (equal (1) andNot equal (1))
      }
      caught.getMessage should equal ("1 equaled 1, but 1 equaled 1")
    }

    "should not execute the right matcher creation function when the left operand is false" - {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(true, "", "") } }
      intercept(classOf[AssertionError]) {
        // This should fail, but without applying the matcher returned by mockMatcher
        1 should { equal (2) andNot mockMatcher }
      }
      called should be (false)
    }

    "should execute the right matcher creation function when the left operand is true" - {
      var called = false
      def mockMatcher = new Matcher[Int] { def apply(i: Int) = { called = true; MatcherResult(false, "", "") } }
      1 should { equal (1) andNot mockMatcher }
      called should be (true)
    }

    "should give good failure messages when used with not" - {
      val caught1 = intercept(classOf[AssertionError]) {
        1 should (not { equal (1) } andNot equal (2))
      }
      caught1.getMessage should equal ("1 equaled 1") // because andNot short circuits
      val caught2 = intercept(classOf[AssertionError]) {
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
      val caught = intercept(classOf[AssertionError]) {
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
      val caught1 = intercept(classOf[AssertionError]) {
        1 should (not { equal (1) } orNot equal (1))
      }
      caught1.getMessage should equal ("1 equaled 1, and 1 equaled 1")
      val caught2 = intercept(classOf[AssertionError]) {
        1 should (equal (2) orNot not { equal (2) }) // Don't do this at home
      }
      caught2.getMessage should equal ("1 did not equal 2, and 1 did not equal 2")
    }
  }

  "The have word" -- {

    "should work with map and key, right after a 'should'" - {
      val map = Map(1 -> "Howdy")
      map should have key 1
      map should have key (1)
      map should equal { Map(1 -> "Howdy") }
      val otherMap = Map("Howdy" -> 1)
      otherMap should have key "Howdy"
      otherMap should equal { Map("Howdy" -> 1) }
    }

    "should work with map and key, in a logical expression" - {
      val map = Map(1 -> "Howdy")
      // The compiler infer the type of the value to be Nothing if I say: map should { have key 1 and equal (Map(1 -> "Howdy")) }
      // map should { have.key[Int, String](1) and equal (Map(1 -> "Howdy")) }
      map should { have key 1 and equal (Map(1 -> "Howdy")) }
      val otherMap = Map("Howdy" -> 1)
      // otherMap should { have.key[String, Int]("Howdy") and equal (Map("Howdy" -> 1)) }
      otherMap should { have key "Howdy" and equal (Map("Howdy" -> 1)) }
    }

    "should work with map and key, right after a 'shouldNot'" - {
      val map = Map(1 -> "Howdy")
      map shouldNot have key 2
    }

    "should work with map and value, right after a 'should'" - {
      val map = Map(1 -> "Howdy")
      map should have value "Howdy"
      map should have value ("Howdy")
      map should equal { Map(1 -> "Howdy") }
      val otherMap = Map("Howdy" -> 1)
      otherMap should have value 1
      otherMap should equal { Map("Howdy" -> 1) }
    }

    "should work with map and value, in a logical expression" - {
      val map = Map(1 -> "Howdy")
      map should { equal (Map(1 -> "Howdy")) and (have value "Howdy") }
      val otherMap = Map("Howdy" -> 1)
      otherMap should { have value 1 and equal (Map("Howdy" -> 1)) }
    }

    "should work with map and value, right after a 'shouldNot'" - {
      val map = Map(1 -> "Howdy")
      map shouldNot have value "Doody"
    }

    "should work with collection and size, in an and expression." - {
      val list = List(1, 2, 3)
      list should { have size 3 and equal (List(1, 2, 3)) }
    }

    "should work with collection and size, right after a 'should'" - {

      val map = Map(1 -> "Howdy")
      map should have size 1
      val caught1 = intercept(classOf[AssertionError]) {
        map should have size 5
      }
      assert(caught1.getMessage.indexOf("did not have size") != -1)

      val list = List(1, 2, 3, 4, 5)
      list should have size 5
      val caught2 = intercept(classOf[AssertionError]) {
        list should have size 6
      }
      assert(caught2.getMessage.indexOf("did not have size") != -1)

      val set = Set(1.0, 2.0, 3.0)
      set should have size 3
      val caught3 = intercept(classOf[AssertionError]) {
        set should have size 0
      }
      assert(caught3.getMessage.indexOf("did not have size") != -1)

      val array = Array[String]()
      array should have size 0
      val caught4 = intercept(classOf[AssertionError]) {
        array should have size 2
      }
      assert(caught4.getMessage.indexOf("did not have size") != -1)
    }

    "should work with collection and size, right after a 'shouldNot'" - {

      val map = Map(1 -> "Howdy")
      map shouldNot have size 2
      val caught1 = intercept(classOf[AssertionError]) {
        map shouldNot have size 1
      }
      assert(caught1.getMessage.indexOf("had size") != -1)

      val list = List(1, 2, 3, 4, 5)
      list shouldNot have size 6
      val caught2 = intercept(classOf[AssertionError]) {
        list shouldNot have size 5
      }
      assert(caught2.getMessage.indexOf("had size") != -1)

      val set = Set(1.0, 2.0, 3.0)
      set shouldNot have size 0
      val caught3 = intercept(classOf[AssertionError]) {
        set shouldNot have size 3
      }
      assert(caught3.getMessage.indexOf("had size") != -1)

      val array = Array[String]()
      array shouldNot have size 2
      val caught4 = intercept(classOf[AssertionError]) {
        array shouldNot have size 0
      }
      assert(caught4.getMessage.indexOf("had size") != -1)
    }
  }

  "The contain word" -- {
 
    "should work with a set, list, array, and map right after a 'should'" - {

      val set = Set(1, 2, 3)
      set should contain element 2
      val caught1 = intercept(classOf[AssertionError]) {
        set should contain element 5
      }
      assert(caught1.getMessage.indexOf("did not contain element") != -1)

      val list = List("one", "two", "three")
      list should contain element "two"
      val caught2 = intercept(classOf[AssertionError]) {
        list should contain element "five"
      }
      assert(caught2.getMessage.indexOf("did not contain element") != -1)

      val array = Array("one", "two", "three")
      array should contain element "one"
      val caught3 = intercept(classOf[AssertionError]) {
        array should contain element "five"
      }
      assert(caught3.getMessage.indexOf("did not contain element") != -1)

      val map = Map(1 -> "one", 2 -> "two", 3 -> "three")
      val tuple2: Tuple2[Int, String] = 1 -> "one"
      map should contain element tuple2
      val caught4 = intercept(classOf[AssertionError]) {
        map should contain element 1 -> "won"
      }
      assert(caught4.getMessage.indexOf("did not contain element") != -1)
    }

    "should work with a set, list, array, and map right after a 'shouldNot'" - {

      val set = Set(1, 2, 3)
      set shouldNot contain element 5
      val caught1 = intercept(classOf[AssertionError]) {
        set shouldNot contain element 2
      }
      assert(caught1.getMessage.indexOf("contained element") != -1)

      val list = List("one", "two", "three")
      list shouldNot contain element "five"
      val caught2 = intercept(classOf[AssertionError]) {
        list shouldNot contain element "two"
      }
      assert(caught2.getMessage.indexOf("contained element") != -1)

      val array = Array("one", "two", "three")
      array shouldNot contain element "five"
      val caught3 = intercept(classOf[AssertionError]) {
        array shouldNot contain element "one"
      }
      assert(caught3.getMessage.indexOf("contained element") != -1)

      val map = Map(1 -> "one", 2 -> "two", 3 -> "three")
      val tuple2: Tuple2[Int, String] = 1 -> "won"
      map shouldNot contain element tuple2
      val caught4 = intercept(classOf[AssertionError]) {
        map shouldNot contain element 1 -> "one"
      }
      assert(caught4.getMessage.indexOf("contained element") != -1)
    }

    "should work with string and have length right after a 'should'" - {
      val string = "hi"
      string should have length 2
      val caught = intercept(classOf[AssertionError]) {
        string should have length 3
      }
      assert(caught.getMessage.indexOf("did not have length") != -1)
    }

    "should work with string and have length right after a 'shouldNot'" - {
      val string = "hi"
      string shouldNot have length 3
      val caught = intercept(classOf[AssertionError]) {
        string shouldNot have length 2
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should work with string, should, and have length in an and expression" - {
      val string = "hi"
      string should { have length 2 and equal ("hi") }
      val caught = intercept(classOf[AssertionError]) {
        string should { have length 3 and equal ("hi") }
      }
      assert(caught.getMessage.indexOf("did not have length") != -1)
    }

    "should work with string, shouldNot, and have length in an and expression" - {
      val string = "hi"
      string shouldNot { have length 3 and equal ("hi") }
      val caught = intercept(classOf[AssertionError]) {
        string shouldNot { have length 2 and equal ("hi") }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should work with array and have length right after a 'should'" - {
      val array = Array('h', 'i')
      array should have length 2
      val caught = intercept(classOf[AssertionError]) {
        array should have length 3
      }
      assert(caught.getMessage.indexOf("did not have length") != -1)
    }

    "should work with array and have length right after a 'shouldNot'" - {
      val array = Array('h', 'i')
      array shouldNot have length 3
      val caught = intercept(classOf[AssertionError]) {
        array shouldNot have length 2
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should work with array, should, and have length in an and expression" - {
      val array = Array('h', 'i')
      array should { have length 2 and equal (Array('h', 'i')) }
      val caught = intercept(classOf[AssertionError]) {
        array should { have length 3 and equal (Array('h', 'i')) }
      }
      assert(caught.getMessage.indexOf("did not have length") != -1)
    }

    "should work with array, shouldNot, and have length in an and expression" - {
      val array = Array('h', 'i')
      array shouldNot { have length 3 and equal (Array('h', 'i')) }
      val caught = intercept(classOf[AssertionError]) {
        array shouldNot { have length 2 and equal (Array('h', 'i')) }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should work with any arbitrary object that has a length method in an and expression" - {
      class HasLengthMethod {
        def length(): Int = 2
      }
      val hasLengthMethod = new HasLengthMethod
      hasLengthMethod should { have length 2 and equal (hasLengthMethod) }
      val caught = intercept(classOf[AssertionError]) {
        hasLengthMethod shouldNot { have length 2 and equal (hasLengthMethod) }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should work with any arbitrary object that has a parameterless length method in an and expression" - {
      class HasLengthMethod {
        def length: Int = 2
      }
      val hasLengthMethod = new HasLengthMethod
      hasLengthMethod should { have length 2 and equal (hasLengthMethod) }
      val caught = intercept(classOf[AssertionError]) {
        hasLengthMethod shouldNot { have length 2 and equal (hasLengthMethod) }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should work with any arbitrary object that has a length field in an and expression" - {
      class HasLengthField {
        val length: Int = 2
      }
      val hasLengthField = new HasLengthField
      hasLengthField should { have length 2 and equal (hasLengthField) }
      val caught = intercept(classOf[AssertionError]) {
        hasLengthField shouldNot { have length 2 and equal (hasLengthField) }
      }
      assert(caught.getMessage.indexOf("had length") != -1)
    }

    "should give an AssertionError with an arbitrary object that has no length member in an and expression" - {
      class HasNoLength {
        val lengthiness: Int = 2
      }
      val hasNoLength = new HasNoLength
      val caught1 = intercept(classOf[AssertionError]) {
        hasNoLength should { have length 2 and equal (hasNoLength) }
      }
      val expectedSubstring = "used with an object that had neither a public field or method named 'length'"
      assert(caught1.getMessage.indexOf(expectedSubstring) != -1)
      val caught2 = intercept(classOf[AssertionError]) {
        hasNoLength shouldNot { have length 2 and equal (hasNoLength) }
      }
      assert(caught2.getMessage.indexOf(expectedSubstring) != -1)
    }
  }

  "The be theSameInstanceAs syntax" -- {

    val string = "Hi"
    val obj: AnyRef = string
    val otherString = new String("Hi")

    "should do nothing if the two objects are the same" - {
      string should be theSameInstanceAs string
      obj should be theSameInstanceAs string
      string should be theSameInstanceAs obj
      otherString shouldNot be theSameInstanceAs string
    }

    "should throw AssertionError if the two objects are not the same" - {
      val caught1 = intercept(classOf[AssertionError]) {
        string shouldNot be theSameInstanceAs string
      }
      val caught2 = intercept(classOf[AssertionError]) {
        obj shouldNot be theSameInstanceAs string
      }
      val caught3 = intercept(classOf[AssertionError]) {
        string shouldNot be theSameInstanceAs obj
      }
      val caught4 = intercept(classOf[AssertionError]) {
        otherString should be theSameInstanceAs string
      }
      assert(true) // TODO: test the failure message
    }
  }

  "The include substring syntax" -- {
    "should do nothing if the string includes the expected substring" - {
      val string = "Four score and seven years ago,..."
      string should include substring "seven"
      string should include substring "Four"
      string should include substring ",..."
      string shouldNot include substring "on this continent"
    }

    "should throw AssertionError if the string does not include the expected substring" - {
      val string = "Four score and seven years ago,..."
      val caught1 = intercept(classOf[AssertionError]) {
        string shouldNot include substring "seven"
      }
      val caught2 = intercept(classOf[AssertionError]) {
        string shouldNot include substring "Four"
      }
      val caught3 = intercept(classOf[AssertionError]) {
        string shouldNot include substring ",..."
      }
      val caught4 = intercept(classOf[AssertionError]) {
        string should include substring "on this continent"
      }
      assert(true) // TODO: test the failure message
    }
  }

  "The should be >/>=/</<= syntax" -- {
    "should do nothing if the specified relation is true" - {
      val one = 1
      one should be < 7
      one should be > 0
      one should be <= 7
      one should be >= 0
      one should be <= 1
      one should be >= 1
      one shouldNot be < 0
      one shouldNot be > 9
      one shouldNot be <= -4
      one shouldNot be >= 21
    }
    "should throw AssertionError if the specified relation is not true" - {
      val one = 1
      val caught1 = intercept(classOf[AssertionError]) {
        one shouldNot be < 7
      }
      val caught2 = intercept(classOf[AssertionError]) {
        one shouldNot be > 0
      }
      val caught3 = intercept(classOf[AssertionError]) {
        one shouldNot be <= 7
      }
      val caught4 = intercept(classOf[AssertionError]) {
        one shouldNot be >= 0
      }
      val caught5 = intercept(classOf[AssertionError]) {
        one shouldNot be <= 1
      }
      val caught6 = intercept(classOf[AssertionError]) {
        one shouldNot be >= 1
      }
      val caught7 = intercept(classOf[AssertionError]) {
        one should be < 0
      }
      val caught8 = intercept(classOf[AssertionError]) {
        one should be > 9
      }
      val caught9 = intercept(classOf[AssertionError]) {
        one should be <= -4
      }
      val caught10 = intercept(classOf[AssertionError]) {
        one should be >= 21
      }
      assert(true) // TODO: test the failure message
    }
  }

  "The floating point 'exactly' operator" -- {
    "should do nothing if the floating point number is exactly equal to the specified value" - {
      val sevenDotOh = 7.0
      sevenDotOh should be (7.0 exactly)
      sevenDotOh shouldEqual 7.0
      sevenDotOh shouldNot be (7.0001 exactly)

      val sixDotOh: Float = 6.0f
      sixDotOh should be (6.0 exactly)
      sixDotOh shouldEqual 6.0
      sixDotOh shouldNot be (6.0001 exactly)
    }

    "should throw AssertionError if the floating point number is not exactly equal to the specified value" - {
      val sevenDotOh = 7.0001
      val caught1 = intercept(classOf[AssertionError]) {
        sevenDotOh should be (7.0 exactly)
      }
      val caught2 = intercept(classOf[AssertionError]) {
        sevenDotOh shouldEqual 7.0
      }
      val caught3 = intercept(classOf[AssertionError]) {
        sevenDotOh shouldNot be (7.0001 exactly)
      }

      val sixDotOh: Float = 6.0001f
      val caught4 = intercept(classOf[AssertionError]) {
        sixDotOh should be (6.0f exactly)
      }
      val caught5 = intercept(classOf[AssertionError]) {
        sixDotOh shouldEqual 6.0f
      }
      val caught6 = intercept(classOf[AssertionError]) {
        sixDotOh shouldNot be (6.0001f exactly)
      }
      assert(true) // TODO: test the failure message
    }
  }
  "The floating point 'plusOrMinus' operator" -- {
    "should do nothing if the floating point number is within the specified range" - {
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
      val caught1 = intercept(classOf[AssertionError]) {
        sevenDotOh shouldNot be (7.1 plusOrMinus 0.2)
      }
      val caught2 = intercept(classOf[AssertionError]) {
        sevenDotOh shouldNot be (6.9 plusOrMinus 0.2)
      }
      val caught3 = intercept(classOf[AssertionError]) {
        sevenDotOh should be (7.5 plusOrMinus 0.2)
      }
      val caught4 = intercept(classOf[AssertionError]) {
        sevenDotOh should be (6.5 plusOrMinus 0.2)
      }
      val minusSevenDotOh = -7.0
      val caught5 = intercept(classOf[AssertionError]) {
        minusSevenDotOh shouldNot be (-7.1 plusOrMinus 0.2)
      }
      val caught6 = intercept(classOf[AssertionError]) {
        minusSevenDotOh shouldNot be (-6.9 plusOrMinus 0.2)
      }
      val caught7 = intercept(classOf[AssertionError]) {
        minusSevenDotOh should be (-7.5 plusOrMinus 0.2)
      }
      val caught8 = intercept(classOf[AssertionError]) {
        minusSevenDotOh should be (-6.5 plusOrMinus 0.2)
      }
      assert(true) // TODO: test the failure message
    }
  }

  "The shouldThrow method" -- {

    "should do nothing if the expected exception is thrown" - {
      theBlock { "Howdy".charAt(-1) } shouldThrow classOf[StringIndexOutOfBoundsException]
      theBlock {
        "Howdy".charAt(-1)
      } shouldThrow classOf[StringIndexOutOfBoundsException]
      theBlock { "Howdy".charAt(-1); println("hi") } shouldThrow classOf[StringIndexOutOfBoundsException]
    }

    "should do nothing if an instance of a subclass of the specified expected exception class is thrown" - {
      class MyException extends RuntimeException
      class MyExceptionSubClass extends MyException
      theBlock { throw new MyException } shouldThrow classOf[MyException]
      theBlock { throw new MyExceptionSubClass } shouldThrow classOf[MyException]
      // Try with a trait
      trait MyTrait
      class AnotherException extends RuntimeException with MyTrait
      theBlock { throw new AnotherException } shouldThrow classOf[MyTrait]
    }

    "should return the caught exception" - {
      val e = new RuntimeException
      val result = theBlock { throw e } shouldThrow classOf[RuntimeException]
      result should be theSameInstanceAs e
    }

    "should throw AssertionError if the expected exception is not thrown" - {
      intercept(classOf[AssertionError]) {
        "Howdy".charAt(1) shouldThrow (classOf[StringIndexOutOfBoundsException])
      } // TODO: Check messages. Try shouldNotThrow. Try throwing wrong exception. See tests for intercept.
      intercept(classOf[AssertionError]) {
        "Howdy".charAt(-1) shouldThrow (classOf[ArrayIndexOutOfBoundsException])
      }
    }
  }

  "The shouldMatch method" -- {

    val decimal = """(-)?(\d+)(\.\d*)?"""
    val decimalRegex = """(-)?(\d+)(\.\d*)?""".r

    "should do nothing if the string matches the regular expression specified as a string" - {
      "1.7" should fullyMatch regex "1.7"
      "1.7" should fullyMatch regex decimal
      "-1.8" should fullyMatch regex decimal
      "8" should fullyMatch regex decimal
      "1." should fullyMatch regex decimal
      "eight" shouldNot fullyMatch regex decimal
      "1.eight" shouldNot fullyMatch regex decimal
      "one.8" shouldNot fullyMatch regex decimal
      "1.8-" shouldNot fullyMatch regex decimal
      "1.7" should { fullyMatch regex decimal and equal ("1.7") }
      "1.7++" shouldNot { fullyMatch regex decimal and equal ("1.7") }
    }

    "should do nothing if the string matches the regular expression specified as a Regex" - {
      "1.7" should fullyMatch regex decimalRegex
      "-1.8" should fullyMatch regex decimalRegex
      "8" should fullyMatch regex decimalRegex
      "1." should fullyMatch regex decimalRegex
      "eight" shouldNot fullyMatch regex decimalRegex
      "1.eight" shouldNot fullyMatch regex decimalRegex
      "one.8" shouldNot fullyMatch regex decimalRegex
      "1.8-" shouldNot fullyMatch regex decimalRegex
      "1.7" should { fullyMatch regex decimalRegex and equal ("1.7") }
      "1.7++" shouldNot { fullyMatch regex decimalRegex and equal ("1.7") }
    }

    "should throw AssertionError if the string does not match the regular expression specified as a string" - {
      val caught1 = intercept(classOf[AssertionError]) {
        "1.7" shouldNot fullyMatch regex "1.7"
      }
      val caught2 = intercept(classOf[AssertionError]) {
        "1.7" shouldNot fullyMatch regex decimal
      }
      val caught3 = intercept(classOf[AssertionError]) {
        "-1.8" shouldNot fullyMatch regex decimal
      }
      val caught4 = intercept(classOf[AssertionError]) {
        "8" shouldNot fullyMatch regex decimal
      }
      val caught5 = intercept(classOf[AssertionError]) {
        "1." shouldNot fullyMatch regex decimal
      }
      val caught6 = intercept(classOf[AssertionError]) {
        "eight" should fullyMatch regex decimal
      }
      val caught7 = intercept(classOf[AssertionError]) {
        "1.eight" should fullyMatch regex decimal
      }
      val caught8 = intercept(classOf[AssertionError]) {
        "one.8" should fullyMatch regex decimal
      }
      val caught9 = intercept(classOf[AssertionError]) {
        "1.8-" should fullyMatch regex decimal
      }
      val caught10 = intercept(classOf[AssertionError]) {
        "1.7" shouldNot { fullyMatch regex decimal and equal ("1.7") }
      }
      val caught11 = intercept(classOf[AssertionError]) {
        "1.7++" should { fullyMatch regex decimal and equal ("1.7") }
      }
      assert(true) // TODO: check failure messages
    }

    "should throw AssertionError if the string does not match the regular expression specified as a Regex" - {
      val caught2 = intercept(classOf[AssertionError]) {
        "1.7" shouldNot fullyMatch regex decimalRegex
      }
      val caught3 = intercept(classOf[AssertionError]) {
        "-1.8" shouldNot fullyMatch regex decimalRegex
      }
      val caught4 = intercept(classOf[AssertionError]) {
        "8" shouldNot fullyMatch regex decimalRegex
      }
      val caught5 = intercept(classOf[AssertionError]) {
        "1." shouldNot fullyMatch regex decimalRegex
      }
      val caught6 = intercept(classOf[AssertionError]) {
        "eight" should fullyMatch regex decimalRegex
      }
      val caught7 = intercept(classOf[AssertionError]) {
        "1.eight" should fullyMatch regex decimalRegex
      }
      val caught8 = intercept(classOf[AssertionError]) {
        "one.8" should fullyMatch regex decimalRegex
      }
      val caught9 = intercept(classOf[AssertionError]) {
        "1.8-" should fullyMatch regex decimalRegex
      }
      val caught10 = intercept(classOf[AssertionError]) {
        "1.7" shouldNot { fullyMatch regex decimalRegex and equal ("1.7") }
      }
      val caught11 = intercept(classOf[AssertionError]) {
        "1.7++" should { fullyMatch regex decimalRegex and equal ("1.7") }
      }
      assert(true) // TODO: check failure messages
    }
  }
} // THE END

    /*
     // After should/shouldNot, if an even number of tokens, you need parens on the last thing.
     // If an odd number of tokens, you need not put parens on the last thing, but usually could if you wanted to.

     map should have key 8 // DONE
     map shouldNot have key 8 // DONE
     map should { have key 8 and equal (Map(8 -> "eight")) } // DONE

     map should have value "eleven" // DONE
     map shouldNot have value "eleven" // DONE

     iterable should contain element 42 // DONE
     iterable shouldNot contain element 42 // DONE
     assert(iterable contains 42) // DONE

     collection should have size 3 // DONE
     collection shouldNot have size 3 // DONE

     string should have length 0 // DONE
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

     string should startWith ("something") // DONE
     string shouldNot startWith ("something") // DONE
     string should endWith ("something") // DONE
     string shouldNot endWith ("something") // DONE

     buf.length should be (20) // DONE

     string should { not { have length 7 } and startWith "Hello" } // DONE

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

     string should include substring "bob" // DONE
     string shouldNot include substring "bob" // DONE

     ordered should be > 7 // DONE
     ordered should be >= 7 // DONE
     ordered should be < 7 // DONE
     ordered should be <= 7 // DONE

     floatingPointNumber should be (7.0 exactly) // DONE
     floatingPointNumber should be (7.0 plusOrMinus 0.01) // DONE

     "Howdy".charAt(-1) shouldThrow (classOf[StringIndexOutOfBoundsException]) // DONE
     theBlock { "Howdy".charAt(-1) } shouldThrow classOf[StringIndexOutOfBoundsException] // DONE
     theBlock { throw new Something } shouldThrow classOf[StringIndexOutOfBoundsException] // DONE

     theBlock { throw new Something } shouldNotThrow anException
     theBlock { throw new Something } shouldThrow anException

     string should fullyMatch regex """[a-zA-Z_]\w*""" // DONE
     string should include substring "howdy"
     string should include regex "howdy"
     string should startWith substring "howdy"
     string should endWith substring "howdy"
     string should startWith regex "howdy"
     string should endWith regex "howdy"

     // This could be nice. It's pretty clear, and a pattern match is
     // sometimes the most concise way to check an object.
     object shouldMatch {
       case 1 :: _ :: 3 :: Nil => true
       case _ => false
     }

     // for symmetry, and perhaps some utility, have Not forms of other shoulds:
     byNameParam shouldNotThrow classOf[IllegalArgumentException]
     object shouldNotMatch {
       case 1 :: _ :: 3 :: Nil => true
       case _ => false
     }

     THINGS I WON'T DO

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
        Resources("didNotHaveKey", left.toString, expectedKey.toString),
        Resources("hadKey", left.toString, expectedKey.toString)
      ) 
    }

    Instead of this:

    new Matcher[Map[K, V]] {
      def apply(left: Map[K, V]) =
        MatcherResult(
          left.contains(expectedKey),
          Resources("didNotHaveKey", left.toString, expectedKey.toString),
          Resources("hadKey", left.toString, expectedKey.toString)
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
          Resources("didNotHaveKey", left.toString, expectedKey.toString),
          Resources("hadKey", left.toString, expectedKey.toString)
        ) 
      }
    }

    Matcher {
      (left: Map[K, V]) => {
        MatcherResult(
          left.contains(expectedKey),
          Resources("didNotHaveKey", left.toString, expectedKey.toString),
          Resources("hadKey", left.toString, expectedKey.toString)
        ) 
      }
    }

    Nah, this doesn't look nice, and it is less explicit.
    */


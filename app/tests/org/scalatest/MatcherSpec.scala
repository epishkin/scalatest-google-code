package org.scalatest

class MatcherSpec extends Spec {

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

    "(for booleans)" -- {

      "should do nothing when null is compared to null" - {
        val o: String = null
        o should be (null)
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
    }

    "(for symbols)" -- {

      "should call isEmpty when passed 'empty" - {
        val emptySet = Set[Int]()
        emptySet should be ('empty)
        val nonEmptySet = Set(1, 2, 3)
        nonEmptySet should not { be ('empty) }
      }

      "should be invokable from be a Symbol, be an Symbol, and be the Symbol" - {
        val emptySet = Set() // XXX
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
      intercept(classOf[AssertionError]) {
        1 should (equal (2) and equal (1))
      }
    }

    "should throw AssertionError when second operands is false" - {
      intercept(classOf[AssertionError]) {
        1 should (equal (1) and equal (2))
      }
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
      // mySet should not { be (empty) }
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
}

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

     buf.length should be (20)

     map should { have key 8 andNot equal (Map(8 -> "eight")) }

     // Some of the be's
     beNone, beNil, beNull, beEmpty, beSome[String], beDefined

     object should beEmpty // MAYBE
     list should beNil // MAYBE
     list shouldNot beNil // MAYBE
     list should beNil // MAYBE

     something should beEmpty // NO
     option should be (None)
     option should beDefined // I may not do this one, because they can say beSome[X], which I think is clearer. Though, in the beDefined case, you need not say the type.
     option should be equalTo Some(1)
     // Ah, to support this, the should method needs to return T, the left value, not Unit. Then
     // I could chain these. But would that cause problems elsewhere that this isn't Unit? Oh, the one should method
     // that would do this is the one that takes whatever the beSome type is.
     // It needn't return T. It should return a very special type that already has a whoseValue method, and that method
     // returns the payload of the option.
     option should beSome[String] whoseValue should startWith ("prefix")

     // beEmpty is actually probably a Matcher[AnyRef], and it first does a pattern match. If it
     // is a String, then it checks for length is zero. Otherwise it does the already-written reflection
     // stuff to look for empty and isEmpty.
     iterable should beEmpty
     iterable shouldNot beEmpty
     string should beEmpty
     string shouldNot beEmpty

     string should { not { have length 7 } and startWith "Hello" }

     list should be (Nil)
     list shouldNot be (Nil)

     // anInstanceOf takes a type param but no value params, used in postfix notation
     object should be anInstanceOf[Something] 
     object shouldNot be anInstanceOf[Something] 

     object should be theSameInstanceAs anotherObjectReference
     object shouldNot be theSameInstanceAs anotherObjectReference

     object should be (null)
     object should beNull
     object shouldNot beNull
     object shouldNot be (null)

     if (object1 == null)
       object2 should beNull
     else 
       object2 shouldNot beNull

     string should equal ignoringCase "happy"
     string should equal ignoringCase "happy"

     string should contain substring "bob" // or if this is hard, could use "include" instead of "contain"
     string shouldNot contain substring "bob"

     string should matchRegEx ("""[a-zA-Z_]\w*""")
     string shouldNot matchRegEx ("""[a-zA-Z_]\w*""")

     // I think these could
     // take a view bounds, something implicitly convertable to Ordered
     ordered should be > 7
     ordered should be >= 7
     ordered should be < 7
     ordered should be <= 7

     floatingPointNumber should be (7.0 plusOrMinus 0.01)
     floatingPointNumber should be (7.0 exactly)

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

     // I could add this one later, but don't need it for this release. Not
     // sure how often this would get used.
     iterable should contain elements (42, 43, 59)

     // I'm not going to do the shouldChange one in this go round, maybe never
     val name = "Bob"
     name.toLowerCase shouldChange name from "Bob" to "bob"

     "Howdy".charAt(-1) shouldThrow (classOf[StringIndexOutOfBoundsException])

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

     val beesKnees = 'beesKnees // DONE
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


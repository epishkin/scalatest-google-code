package org.scalatest.matchers

import Matchers._

class MatcherSpec extends Spec {
  "The equal matcher" -- {
    "should do nothing when equal" - {
      1 should equal (1)
    }
    "should throw an assertion error when not equal" - {
      intercept(classOf[AssertionError]) {
        1 should equal (2)
      }
    }
  }
  "The be matcher" -- {

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
    }

    "should throw an assertion error when null compared to not null" - {
      intercept(classOf[AssertionError]) {
        val o: String = null
        o should not { be (null) }
      }
    }

    "should call isEmpty when passed 'empty" - {
      val emptySet = Set()
      emptySet should be ('empty)
      val nonEmptySet = Set(1, 2, 3)
      nonEmptySet should not { be ('empty) }
    }

    "should be invokable from beA(Symbol) and beAn(Symbol)" - {
      val emptySet = Set()
      emptySet should beA ('empty)
      emptySet should beAn ('empty)
      val nonEmptySet = Set(1, 2, 3)
      nonEmptySet should not { beA ('empty) }
      nonEmptySet should not { beAn ('empty) }
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
      ex1.getMessage should equal ("EmptyMock has neither a empty or a isEmpty method.")
      val ex2 = intercept(classOf[IllegalArgumentException]) {
        (new NonEmptyMock) should not { be ('empty) }
      }
      ex2.getMessage should equal ("NonEmptyMock has neither a empty or a isEmpty method.")
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
      ex1.getMessage should equal ("EmptyMock has both a empty and a isEmpty method.")
      val ex2 = intercept(classOf[IllegalArgumentException]) {
        (new NonEmptyMock) should not { be ('empty) }
      }
      ex2.getMessage should equal ("NonEmptyMock has both a empty and a isEmpty method.")
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
  "The endsWith matcher" -- {
    "should do nothing when true" - {
      "Hello, world" should endWith ("world")
    }
    "should throw an assertion error when not true" - {
      intercept(classOf[AssertionError]) {
        "Hello, world" should endWith ("planet")
      }
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
    /*
     // After should, if an even number of tokens, you need parens on the last thing.
     // If an odd number of tokens, you don't need parens on the last thing.

     map should have key 8
     map shouldNot have key 8

     map should have value "eleven"
     map shouldNot have value "eleven"

     iterable should contain (42)
     iterable shouldNot contain (42)

     iterable should have size 3
     iterable shouldNot have size 3

     iterable should beEmpty
     iterable shouldNot beEmpty

     string should have length 0
     string shouldNot have length 0
     string should when trimmed have length 0
     string shouldNot when trimmed have length 0

     array should have length 9
     array shouldNot have length 9

     // Using boolean properties dynamically
     object should be ('empty)
     object shouldNot be ('empty)
     object should be a 'file
     object shouldNot be a 'file
     object should be an 'openBook
     object shouldNot be an 'openBook
     object should be the 'beesKnees
     object shouldNot be the 'beesKnees

     list should be (Nil)
     list shouldNot be (Nil)

     // of takes a type param but no value params, used in postfix notation
     object should be anInstanceOf[Something] 
     object shouldNot be anInstanceOf[Something] 

     object should be theSameInstanceAs anotherObjectReference
     object shouldNot be theSameInstanceAs anotherObjectReference

     string should equalIgnoringCase ("happy")
     string shouldNot equalIgnoringCase ("happy")
     string should equalWhenTrimmed (" happy  ")
     string shouldNot equalWhenTrimmed (" happy  ")

     string should startWith ("something")
     string shouldNot startWith ("something")
     string should endWith ("something")
     string shouldNot endWith ("something")

     // Check if a string includes a substring
     string should include ("bob")
     string shouldNot include ("bob")

     string should matchRegEx ("""[a-zA-Z_]\w*""")
     string shouldNot matchRegEx ("""[a-zA-Z_]\w*""")

     // Maybe these could be structural. Actually, I think these could
     // take a view bounds, something implicitly convertable to Ordered
     ordered should be > 7
     ordered should be >= 7
     ordered should be < 7
     ordered should be <= 7

     // ordered should be greater than (7)
     // ordered should be greater than or equal to 7
     // ordered should be less than (7)
     // ordered should be less than or equal to 7

     floatingPointNumber should be ( 7.0 plusOrMinus 0.01 )
     floatingPointNumber should be ( 7.0 exactly )
     option should beNone
     option should beDefined
     option should equal (Some(1))
     option should be (some[String])
     option should be (some[String], which should start with "prefix") // or this
     object should satisfy (_ > 12)
     object should satisfy { case 1 :: _ :: 3 :: Nil => true } 
     // object should matchPattern { case 1 :: _ :: 3 :: Nil => true } // or..

     { "Howdy".charAt(-1) } shouldThrow (classOf[StringIndexOutOfBoundsException])

     theFollowing shouldThrow (classOf[StringIndexOutOfBoundsException]) {
       "Howdy".charAt(-1)
     }
    */
  }
}

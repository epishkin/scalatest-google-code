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
        val caught = intercept[TestFailedException] {
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
        val caught = intercept[TestFailedException] {
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
        val caught1 = intercept[TestFailedException] {
          val o: String = null
          o should not { be (null) }
        }
        assert(caught1.getMessage === "null was null")
        /* val caught2 = intercept[TestFailedException] {
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
        val caught1 = intercept[TestFailedException] {
          nonEmptyList should be (Nil)
        }
        assert(caught1.getMessage === "List(Helloooooo) was not List()")
        val caught2 = intercept[TestFailedException] {
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
        val caught1 = intercept[TestFailedException] {
          emptyList should not { be (Nil) }
        }
        assert(caught1.getMessage === "List() was List()")

        /* val caught2 = intercept[TestFailedException] {
          emptyList shouldNot be (Nil)
        }
        assert(caught2.getMessage === "List() was List()") */

        val caught3 = intercept[TestFailedException] {
          emptyList should not { equal (Nil) }
        }
        assert(caught3.getMessage === "List() equaled List()")

        /* val caught4 = intercept[TestFailedException] {
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
        val caught1 = intercept[TestFailedException] {
          someString should be (None)
        }
        assert(caught1.getMessage === "Some(Helloooooo) was not None")
        val caught2 = intercept[TestFailedException] {
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
        val caught1 = intercept[TestFailedException] {
          none should not { be (None) }
        }
        assert(caught1.getMessage === "None was None")

        /* val caught2 = intercept[TestFailedException] {
          none shouldNot be (None)
        }
        assert(caught2.getMessage === "None was None")  */

        val caught3 = intercept[TestFailedException] {
          none should not { equal (None) }
        }
        assert(caught3.getMessage === "None equaled None")

        /* val caught4 = intercept[TestFailedException] {
          none shouldNot equal (None)
        }
        assert(caught4.getMessage === "None equaled None") */

        val noString: Option[String] = None
        val caught5 = intercept[TestFailedException] {
          noString should not { be (None) }
        }
        assert(caught5.getMessage === "None was None")

        /* val caught6 = intercept[TestFailedException] {
          noString shouldNot be (None)
        }
        assert(caught6.getMessage === "None was None") */

        val caught7 = intercept[TestFailedException] {
          noString should not { equal (None) }
        }
        assert(caught7.getMessage === "None equaled None")

        /* val caught8 = intercept[TestFailedException] {
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

    describe("(for Any)") {
      it("should do nothing when equal") {
        1 should be (1)
        val option = Some(1)
        option should be (Some(1)) 
      }

      it("should throw an assertion error when not equal") {
        val caught = intercept[TestFailedException] {
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
        val caught = intercept[TestFailedException] {
          // 1 shouldNot be (1)
          1 should not { be (1) }
        }
        assert(caught.getMessage === "1 was 1")
      }
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
      val caught1 = intercept[TestFailedException] {
        map should have size (5)
      }
      assert(caught1.getMessage.indexOf("did not have size") != -1)

      val list = List(1, 2, 3, 4, 5)
      list should have size (5)
      val caught2 = intercept[TestFailedException] {
        list should have size (6)
      }
      assert(caught2.getMessage.indexOf("did not have size") != -1)

      val set = Set(1.0, 2.0, 3.0)
      set should have size (3)
      val caught3 = intercept[TestFailedException] {
        set should have size (0)
      }
      assert(caught3.getMessage.indexOf("did not have size") != -1)

      val array = Array[String]()
      array should have size 0
      val caught4 = intercept[TestFailedException] {
        array should have size 2
      }
      assert(caught4.getMessage.indexOf("did not have size") != -1)
    }

    it("should work with collection and size, right after a 'shouldNot'") {

      val map = Map(1 -> "Howdy")
      // map shouldNot have size (2)
      map should not { have size (2) }
      val caught1 = intercept[TestFailedException] {
        // map shouldNot have size (1)
        map should not { have size (1) }
      }
      assert(caught1.getMessage.indexOf("had size") != -1, caught1.getMessage)

      val list = List(1, 2, 3, 4, 5)
      //list shouldNot have size (6)
      list should not { have size (6) }
      val caught2 = intercept[TestFailedException] {
        // list shouldNot have size (5)
        list should not { have size (5) }
      }
      assert(caught2.getMessage.indexOf("had size") != -1)

      val set = Set(1.0, 2.0, 3.0)
      // set shouldNot have size (0)
      set should not { have size (0) }
      val caught3 = intercept[TestFailedException] {
        // set shouldNot have size (3)
        set should not { have size (3) }
      }
      assert(caught3.getMessage.indexOf("had size") != -1)

      val array = Array[String]()
      // array shouldNot have size (2)
      array should not { have size (2) }
      val caught4 = intercept[TestFailedException] {
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
      val caught1 = intercept[TestFailedException] {
        set should contain element (5)
      }
      assert(caught1.getMessage.indexOf("did not contain element") != -1)

      set should { contain element (2) and equal (Set(1, 2, 3)) }
      val caught1b = intercept[TestFailedException] {
        set should { contain element (5) and equal(Set(1, 2, 3)) }
      }
      assert(caught1b.getMessage.indexOf("did not contain element") != -1)

      val list = List("one", "two", "three")
      list should contain element ("two")
      val caught2 = intercept[TestFailedException] {
        list should contain element ("five")
      }
      assert(caught2.getMessage.indexOf("did not contain element") != -1)

      val array = Array("one", "two", "three")
      array should contain element ("one")
      val caught3 = intercept[TestFailedException] {
        array should contain element ("five")
      }
      assert(caught3.getMessage.indexOf("did not contain element") != -1)

      val map = Map(1 -> "one", 2 -> "two", 3 -> "three")
      val tuple2: Tuple2[Int, String] = 1 -> "one"
      map should contain element (tuple2)
      val caught4 = intercept[TestFailedException] {
        map should contain element (1 -> "won")
      }
      assert(caught4.getMessage.indexOf("did not contain element") != -1)
    }

    it("should work with a set, list, array, and map right after a 'shouldNot'") {

      val set = Set(1, 2, 3)
      // set shouldNot contain element (5)
      set should not { contain element (5) }
      val caught1 = intercept[TestFailedException] {
        // set shouldNot contain element (2)
        set should not { contain element (2) }
      }
      assert(caught1.getMessage.indexOf("contained element") != -1)

      val list = List("one", "two", "three")
      // list shouldNot contain element ("five")
      list should not { contain element ("five") }
      val caught2 = intercept[TestFailedException] {
        // list shouldNot contain element ("two")
        list should not { contain element ("two") }
      }
      assert(caught2.getMessage.indexOf("contained element") != -1)

      val array = Array("one", "two", "three")
      // array shouldNot contain element ("five")
      array should not { contain element ("five") }
      val caught3 = intercept[TestFailedException] {
        // array shouldNot contain element ("one")
        array should not { contain element ("one") }
      }
      assert(caught3.getMessage.indexOf("contained element") != -1)

      val map = Map(1 -> "one", 2 -> "two", 3 -> "three")
      val tuple2: Tuple2[Int, String] = 1 -> "won"
      // map shouldNot contain element (tuple2)
      map should not { contain element (tuple2) }
      val caught4 = intercept[TestFailedException] {
        // map shouldNot contain element (1 -> "one")
        map should not { contain element (1 -> "one") }
      }
      assert(caught4.getMessage.indexOf("contained element") != -1)
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

    it("should throw TestFailedException if the two objects are not the same") {
      val caught1 = intercept[TestFailedException] {
        // string shouldNot be theSameInstanceAs (string)
        string should not { be theSameInstanceAs (string) }
      }
      val caught2 = intercept[TestFailedException] {
        // obj shouldNot be theSameInstanceAs (string)
        obj should not { be theSameInstanceAs (string) }
      }
      val caught3 = intercept[TestFailedException] {
        // string shouldNot be theSameInstanceAs (obj)
        string should not { be theSameInstanceAs (obj) }
      }
      val caught4 = intercept[TestFailedException] {
        otherString should be theSameInstanceAs (string)
      }
      assert(true) // TODO: test the failure message
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

    it("should throw TestFailedException if the floating point number is not exactly equal to the specified value") {
      val sevenDotOh = 7.0001
      val caught1 = intercept[TestFailedException] {
        sevenDotOh should be (7.0)
        // sevenDotOh should be (7.0 exactly)
      }
      assert(caught1.getMessage === "7.0001 was not 7.0")

      val caught2 = intercept[TestFailedException] {
        sevenDotOh should equal (7.0)
      }
      assert(caught2.getMessage === "7.0001 did not equal 7.0")

      val caught3 = intercept[TestFailedException] {
        // sevenDotOh shouldNot be (7.0001 exactly)
        // sevenDotOh shouldNot be (7.0001)
        sevenDotOh should not { be (7.0001) }
      }
      assert(caught3.getMessage === "7.0001 was 7.0001")

      val sixDotOh: Float = 6.0001f
      val caught4 = intercept[TestFailedException] {
        // sixDotOh should be (6.0f exactly)
        sixDotOh should be (6.0f)
      }
      assert(caught4.getMessage === "6.0001 was not 6.0")

      val caught5 = intercept[TestFailedException] {
        sixDotOh should equal (6.0f)
      }
      assert(caught5.getMessage === "6.0001 did not equal 6.0")

      val caught6 = intercept[TestFailedException] {
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

    it("should throw TestFailedException if the floating point number is not within the specified range") {
      val sevenDotOh = 7.0
      val caught1 = intercept[TestFailedException] {
        // sevenDotOh shouldNot be (7.1 plusOrMinus 0.2)
        sevenDotOh should not { be (7.1 plusOrMinus 0.2) }
      }
      assert(caught1.getMessage === "7.0 was 7.1 plus or minus 0.2")

      val caught2 = intercept[TestFailedException] {
        // sevenDotOh shouldNot be (6.9 plusOrMinus 0.2)
        sevenDotOh should not { be (6.9 plusOrMinus 0.2) }
      }
      assert(caught2.getMessage === "7.0 was 6.9 plus or minus 0.2")

      val caught3 = intercept[TestFailedException] {
        sevenDotOh should be (7.5 plusOrMinus 0.2)
      }
      assert(caught3.getMessage === "7.0 was not 7.5 plus or minus 0.2")

      val caught4 = intercept[TestFailedException] {
        sevenDotOh should be (6.5 plusOrMinus 0.2)
      }
      assert(caught4.getMessage === "7.0 was not 6.5 plus or minus 0.2")

      val minusSevenDotOh = -7.0
      val caught5 = intercept[TestFailedException] {
        // minusSevenDotOh shouldNot be (-7.1 plusOrMinus 0.2)
        minusSevenDotOh should not { be (-7.1 plusOrMinus 0.2) }
      }
      assert(caught5.getMessage === "-7.0 was -7.1 plus or minus 0.2")

      val caught6 = intercept[TestFailedException] {
        // minusSevenDotOh shouldNot be (-6.9 plusOrMinus 0.2)
        minusSevenDotOh should not { be (-6.9 plusOrMinus 0.2) }
      }
      assert(caught6.getMessage === "-7.0 was -6.9 plus or minus 0.2")

      val caught7 = intercept[TestFailedException] {
        minusSevenDotOh should be (-7.5 plusOrMinus 0.2)
      }
      assert(caught7.getMessage === "-7.0 was not -7.5 plus or minus 0.2")

      val caught8 = intercept[TestFailedException] {
        minusSevenDotOh should be (-6.5 plusOrMinus 0.2)
      }
      assert(caught8.getMessage === "-7.0 was not -6.5 plus or minus 0.2")
    }
  }
}

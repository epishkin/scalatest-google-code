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

class ShouldContainElementSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion {

  // Checking for a specific size
  describe("The 'contain element (Int)' syntax") {

    describe("on Array") {

      it("should do nothing if array contains the specified element") {
        Array(1, 2) should contain element (2)
        Array(1, 2) should (contain element (2))
        check((arr: Array[Int]) => arr.size != 0 ==> returnsNormally(arr should contain element (arr(arr.length - 1))))
      }

      it("should do nothing if array does not contain the element and used with should not") {
        Array(1, 2) should not { contain element (3) }
        Array(1, 2) should not contain element (3)
        check((arr: Array[Int], i: Int) => !arr.exists(_ == i) ==> returnsNormally(arr should not { contain element (i) }))
        check((arr: Array[Int], i: Int) => !arr.exists(_ == i) ==> returnsNormally(arr should not contain element (i)))
      }

      it("should do nothing when array contains the specified element and used in a logical-and expression") {
        Array(1, 2) should { contain element (2) and (contain element (1)) }
        Array(1, 2) should ((contain element (2)) and (contain element (1)))
        Array(1, 2) should (contain element (2) and contain element (1))
       }

      it("should do nothing when array contains the specified element and used in a logical-or expression") {
        Array(1, 2) should { contain element (77) or (contain element (2)) }
        Array(1, 2) should ((contain element (77)) or (contain element (2)))
        Array(1, 2) should (contain element (77) or contain element (2))
      }

      it("should do nothing when array doesn't contain the specified element and used in a logical-and expression with not") {
        Array(1, 2) should { not { contain element (5) } and not { contain element (3) }}
        Array(1, 2) should ((not contain element (5)) and (not contain element (3)))
        Array(1, 2) should (not contain element (5) and not contain element (3))
      }

      it("should do nothing when array doesn't contain the specified element and used in a logical-or expression with not") {
        Array(1, 2) should { not { contain element (1) } or not { contain element (3) }}
        Array(1, 2) should ((not contain element (1)) or (not contain element (3)))
        Array(1, 2) should (not contain element (3) or not contain element (2))
      }

      it("should throw TestFailedException if array does not contain the specified element") {
        val caught = intercept[TestFailedException] {
          Array(1, 2) should contain element (3)
        }
        assert(caught.getMessage === "Array(1, 2) did not contain element 3")
        check((arr: Array[String], s: String) => !arr.exists(_ == s) ==> throwsTestFailedException(arr should contain element (s)))
      }

      it("should throw TestFailedException if array contains the specified element, when used with not") {

        val caught1 = intercept[TestFailedException] {
          Array(1, 2) should not contain element (2)
        }
        assert(caught1.getMessage === "Array(1, 2) contained element 2")
        check((arr: Array[String]) => arr.length > 0 ==> throwsTestFailedException(arr should not contain element (arr(0))))

        val caught2 = intercept[TestFailedException] {
          Array(1, 2) should not (contain element (2))
        }
        assert(caught2.getMessage === "Array(1, 2) contained element 2")
        check((arr: Array[String]) => arr.length > 0 ==> throwsTestFailedException(arr should not (contain element (arr(0)))))

        val caught3 = intercept[TestFailedException] {
          Array(1, 2) should (not contain element (2))
        }
        assert(caught3.getMessage === "Array(1, 2) contained element 2")
        check((arr: Array[String]) => arr.length > 0 ==> throwsTestFailedException(arr should not (contain element (arr(0)))))
      }

      it("should throw a TestFailedException when array doesn't contain the specified element and used in a logical-and expression") {

        val caught1 = intercept[TestFailedException] {
          Array(1, 2) should { contain element (5) and (contain element (2 - 1)) }
        }
        assert(caught1.getMessage === "Array(1, 2) did not contain element 5")

        val caught2 = intercept[TestFailedException] {
          Array(1, 2) should (contain element (5) and contain element (2 - 1))
        }
        assert(caught2.getMessage === "Array(1, 2) did not contain element 5")
      }

      it("should throw a TestFailedException when array doesn't contain the specified element and used in a logical-or expression") {

        val caught1 = intercept[TestFailedException] {
          Array(1, 2) should { contain element (55) or (contain element (22)) }
        }
        assert(caught1.getMessage === "Array(1, 2) did not contain element 55, and Array(1, 2) did not contain element 22")

        val caught2 = intercept[TestFailedException] {
          Array(1, 2) should (contain element (55) or contain element (22))
        }
        assert(caught2.getMessage === "Array(1, 2) did not contain element 55, and Array(1, 2) did not contain element 22")
      }

      it("should throw a TestFailedException when array contains the specified element and used in a logical-and expression with not") {

        val caught1 = intercept[TestFailedException] {
          Array(1, 2) should { not { contain element (3) } and not { contain element (2) }}
        }
        assert(caught1.getMessage === "Array(1, 2) did not contain element 3, but Array(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          Array(1, 2) should ((not contain element (3)) and (not contain element (2)))
        }
        assert(caught2.getMessage === "Array(1, 2) did not contain element 3, but Array(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          Array(1, 2) should (not contain element (3) and not contain element (2))
        }
        assert(caught3.getMessage === "Array(1, 2) did not contain element 3, but Array(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when array contains the specified element and used in a logical-or expression with not") {

        val caught1 = intercept[TestFailedException] {
          Array(1, 2) should { not { contain element (2) } or not { contain element (2) }}
        }
        assert(caught1.getMessage === "Array(1, 2) contained element 2, and Array(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          Array(1, 2) should ((not contain element (2)) or (not contain element (2)))
        }
        assert(caught2.getMessage === "Array(1, 2) contained element 2, and Array(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          Array(1, 2) should (not contain element (2) or not contain element (2))
        }
        assert(caught3.getMessage === "Array(1, 2) contained element 2, and Array(1, 2) contained element 2")
      }
    }

    describe("on scala.collection.immutable.Set") {

      it("should do nothing if set contains the specified element") {
        Set(1, 2) should contain element (2)
        Set(1, 2) should (contain element (2))
      }

      it("should do nothing if set does not contain the element and used with should not") {
        Set(1, 2) should not { contain element (3) }
        Set(1, 2) should not contain element (3)
      }

      it("should do nothing when set contains the specified element and used in a logical-and expression") {
        Set(1, 2) should { contain element (2) and (contain element (1)) }
        Set(1, 2) should ((contain element (2)) and (contain element (1)))
        Set(1, 2) should (contain element (2) and contain element (1))
       }

      it("should do nothing when set contains the specified element and used in a logical-or expression") {
        Set(1, 2) should { contain element (77) or (contain element (2)) }
        Set(1, 2) should ((contain element (77)) or (contain element (2)))
        Set(1, 2) should (contain element (77) or contain element (2))
      }

      it("should do nothing when set doesn't contain the specified element and used in a logical-and expression with not") {
        Set(1, 2) should { not { contain element (5) } and not { contain element (3) }}
        Set(1, 2) should ((not contain element (5)) and (not contain element (3)))
        Set(1, 2) should (not contain element (5) and not contain element (3))
      }

      it("should do nothing when set doesn't contain the specified element and used in a logical-or expression with not") {
        Set(1, 2) should { not { contain element (1) } or not { contain element (3) }}
        Set(1, 2) should ((not contain element (1)) or (not contain element (3)))
        Set(1, 2) should (not contain element (3) or not contain element (2))
      }

      it("should throw TestFailedException if set does not contain the specified element") {
        val caught = intercept[TestFailedException] {
          Set(1, 2) should contain element (3)
        }
        assert(caught.getMessage === "Set(1, 2) did not contain element 3")
      }

      it("should throw TestFailedException if set contains the specified element, when used with not") {

        val caught1 = intercept[TestFailedException] {
          Set(1, 2) should not contain element (2)
        }
        assert(caught1.getMessage === "Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          Set(1, 2) should not (contain element (2))
        }
        assert(caught2.getMessage === "Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          Set(1, 2) should (not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when set doesn't contain the specified element and used in a logical-and expression") {

        val caught1 = intercept[TestFailedException] {
          Set(1, 2) should { contain element (5) and (contain element (2 - 1)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 5")

        val caught2 = intercept[TestFailedException] {
          Set(1, 2) should (contain element (5) and contain element (2 - 1))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 5")
      }

      it("should throw a TestFailedException when set doesn't contain the specified element and used in a logical-or expression") {

        val caught1 = intercept[TestFailedException] {
          Set(1, 2) should { contain element (55) or (contain element (22)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 55, and Set(1, 2) did not contain element 22")

        val caught2 = intercept[TestFailedException] {
          Set(1, 2) should (contain element (55) or contain element (22))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 55, and Set(1, 2) did not contain element 22")
      }

      it("should throw a TestFailedException when set contains the specified element and used in a logical-and expression with not") {

        val caught1 = intercept[TestFailedException] {
          Set(1, 2) should { not { contain element (3) } and not { contain element (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          Set(1, 2) should ((not contain element (3)) and (not contain element (2)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          Set(1, 2) should (not contain element (3) and not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when set contains the specified element and used in a logical-or expression with not") {

        val caught1 = intercept[TestFailedException] {
          Set(1, 2) should { not { contain element (2) } or not { contain element (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          Set(1, 2) should ((not contain element (2)) or (not contain element (2)))
        }
        assert(caught2.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          Set(1, 2) should (not contain element (2) or not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")
      }
    }

    describe("on scala.collection.mutable.Set") {

      import scala.collection.mutable

      it("should do nothing if set contains the specified element") {
        mutable.Set(1, 2) should contain element (2)
        mutable.Set(1, 2) should (contain element (2))
      }

      it("should do nothing if set does not contain the element and used with should not") {
        mutable.Set(1, 2) should not { contain element (3) }
        mutable.Set(1, 2) should not contain element (3)
      }

      it("should do nothing when set contains the specified element and used in a logical-and expression") {
        mutable.Set(1, 2) should { contain element (2) and (contain element (1)) }
        mutable.Set(1, 2) should ((contain element (2)) and (contain element (1)))
        mutable.Set(1, 2) should (contain element (2) and contain element (1))
       }

      it("should do nothing when set contains the specified element and used in a logical-or expression") {
        mutable.Set(1, 2) should { contain element (77) or (contain element (2)) }
        mutable.Set(1, 2) should ((contain element (77)) or (contain element (2)))
        mutable.Set(1, 2) should (contain element (77) or contain element (2))
      }

      it("should do nothing when set doesn't contain the specified element and used in a logical-and expression with not") {
        mutable.Set(1, 2) should { not { contain element (5) } and not { contain element (3) }}
        mutable.Set(1, 2) should ((not contain element (5)) and (not contain element (3)))
        mutable.Set(1, 2) should (not contain element (5) and not contain element (3))
      }

      it("should do nothing when set doesn't contain the specified element and used in a logical-or expression with not") {
        mutable.Set(1, 2) should { not { contain element (1) } or not { contain element (3) }}
        mutable.Set(1, 2) should ((not contain element (1)) or (not contain element (3)))
        mutable.Set(1, 2) should (not contain element (3) or not contain element (2))
      }

      it("should throw TestFailedException if set does not contain the specified element") {
        val caught = intercept[TestFailedException] {
          mutable.Set(1, 2) should contain element (3)
        }
        assert(caught.getMessage === "Set(1, 2) did not contain element 3")
      }

      it("should throw TestFailedException if set contains the specified element, when used with not") {

        val caught1 = intercept[TestFailedException] {
          mutable.Set(1, 2) should not contain element (2)
        }
        assert(caught1.getMessage === "Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          mutable.Set(1, 2) should not (contain element (2))
        }
        assert(caught2.getMessage === "Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          mutable.Set(1, 2) should (not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when set doesn't contain the specified element and used in a logical-and expression") {

        val caught1 = intercept[TestFailedException] {
          mutable.Set(1, 2) should { contain element (5) and (contain element (2 - 1)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 5")

        val caught2 = intercept[TestFailedException] {
          mutable.Set(1, 2) should (contain element (5) and contain element (2 - 1))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 5")
      }

      it("should throw a TestFailedException when set doesn't contain the specified element and used in a logical-or expression") {

        val caught1 = intercept[TestFailedException] {
          mutable.Set(1, 2) should { contain element (55) or (contain element (22)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 55, and Set(1, 2) did not contain element 22")

        val caught2 = intercept[TestFailedException] {
          mutable.Set(1, 2) should (contain element (55) or contain element (22))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 55, and Set(1, 2) did not contain element 22")
      }

      it("should throw a TestFailedException when set contains the specified element and used in a logical-and expression with not") {

        val caught1 = intercept[TestFailedException] {
          mutable.Set(1, 2) should { not { contain element (3) } and not { contain element (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          mutable.Set(1, 2) should ((not contain element (3)) and (not contain element (2)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          mutable.Set(1, 2) should (not contain element (3) and not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when set contains the specified element and used in a logical-or expression with not") {

        val caught1 = intercept[TestFailedException] {
          mutable.Set(1, 2) should { not { contain element (2) } or not { contain element (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          mutable.Set(1, 2) should ((not contain element (2)) or (not contain element (2)))
        }
        assert(caught2.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          mutable.Set(1, 2) should (not contain element (2) or not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")
      }
    }

    describe("on scala.collection.Set") {

      val set: scala.collection.Set[Int] = Set(1, 2)

      it("should do nothing if set contains the specified element") {
        set should contain element (2)
        set should (contain element (2))
      }

      it("should do nothing if set does not contain the element and used with should not") {
        set should not { contain element (3) }
        set should not contain element (3)
      }

      it("should do nothing when set contains the specified element and used in a logical-and expression") {
        set should { contain element (2) and (contain element (1)) }
        set should ((contain element (2)) and (contain element (1)))
        set should (contain element (2) and contain element (1))
       }

      it("should do nothing when set contains the specified element and used in a logical-or expression") {
        set should { contain element (77) or (contain element (2)) }
        set should ((contain element (77)) or (contain element (2)))
        set should (contain element (77) or contain element (2))
      }

      it("should do nothing when set doesn't contain the specified element and used in a logical-and expression with not") {
        set should { not { contain element (5) } and not { contain element (3) }}
        set should ((not contain element (5)) and (not contain element (3)))
        set should (not contain element (5) and not contain element (3))
      }

      it("should do nothing when set doesn't contain the specified element and used in a logical-or expression with not") {
        set should { not { contain element (1) } or not { contain element (3) }}
        set should ((not contain element (1)) or (not contain element (3)))
        set should (not contain element (3) or not contain element (2))
      }

      it("should throw TestFailedException if set does not contain the specified element") {
        val caught = intercept[TestFailedException] {
          set should contain element (3)
        }
        assert(caught.getMessage === "Set(1, 2) did not contain element 3")
      }

      it("should throw TestFailedException if set contains the specified element, when used with not") {

        val caught1 = intercept[TestFailedException] {
          set should not contain element (2)
        }
        assert(caught1.getMessage === "Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          set should not (contain element (2))
        }
        assert(caught2.getMessage === "Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          set should (not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when set doesn't contain the specified element and used in a logical-and expression") {

        val caught1 = intercept[TestFailedException] {
          set should { contain element (5) and (contain element (2 - 1)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 5")

        val caught2 = intercept[TestFailedException] {
          set should (contain element (5) and contain element (2 - 1))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 5")
      }

      it("should throw a TestFailedException when set doesn't contain the specified element and used in a logical-or expression") {

        val caught1 = intercept[TestFailedException] {
          set should { contain element (55) or (contain element (22)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 55, and Set(1, 2) did not contain element 22")

        val caught2 = intercept[TestFailedException] {
          set should (contain element (55) or contain element (22))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 55, and Set(1, 2) did not contain element 22")
      }

      it("should throw a TestFailedException when set contains the specified element and used in a logical-and expression with not") {

        val caught1 = intercept[TestFailedException] {
          set should { not { contain element (3) } and not { contain element (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          set should ((not contain element (3)) and (not contain element (2)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          set should (not contain element (3) and not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when set contains the specified element and used in a logical-or expression with not") {

        val caught1 = intercept[TestFailedException] {
          set should { not { contain element (2) } or not { contain element (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          set should ((not contain element (2)) or (not contain element (2)))
        }
        assert(caught2.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          set should (not contain element (2) or not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")
      }
    }

    describe("on scala.collection.immutable.HashSet") {

      import scala.collection.immutable.HashSet
        
      it("should do nothing if set contains the specified element") {
        HashSet(1, 2) should contain element (2)
        HashSet(1, 2) should (contain element (2))
      }

      it("should do nothing if set does not contain the element and used with should not") {
        HashSet(1, 2) should not { contain element (3) }
        HashSet(1, 2) should not contain element (3)
      }

      it("should do nothing when set contains the specified element and used in a logical-and expression") {
        HashSet(1, 2) should { contain element (2) and (contain element (1)) }
        HashSet(1, 2) should ((contain element (2)) and (contain element (1)))
        HashSet(1, 2) should (contain element (2) and contain element (1))
       }

      it("should do nothing when set contains the specified element and used in a logical-or expression") {
        HashSet(1, 2) should { contain element (77) or (contain element (2)) }
        HashSet(1, 2) should ((contain element (77)) or (contain element (2)))
        HashSet(1, 2) should (contain element (77) or contain element (2))
      }

      it("should do nothing when set doesn't contain the specified element and used in a logical-and expression with not") {
        HashSet(1, 2) should { not { contain element (5) } and not { contain element (3) }}
        HashSet(1, 2) should ((not contain element (5)) and (not contain element (3)))
        HashSet(1, 2) should (not contain element (5) and not contain element (3))
      }

      it("should do nothing when set doesn't contain the specified element and used in a logical-or expression with not") {
        HashSet(1, 2) should { not { contain element (1) } or not { contain element (3) }}
        HashSet(1, 2) should ((not contain element (1)) or (not contain element (3)))
        HashSet(1, 2) should (not contain element (3) or not contain element (2))
      }

      it("should throw TestFailedException if set does not contain the specified element") {
        val caught = intercept[TestFailedException] {
          HashSet(1, 2) should contain element (3)
        }
        assert(caught.getMessage === "Set(1, 2) did not contain element 3")
      }

      it("should throw TestFailedException if set contains the specified element, when used with not") {

        val caught1 = intercept[TestFailedException] {
          HashSet(1, 2) should not contain element (2)
        }
        assert(caught1.getMessage === "Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          HashSet(1, 2) should not (contain element (2))
        }
        assert(caught2.getMessage === "Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          HashSet(1, 2) should (not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when set doesn't contain the specified element and used in a logical-and expression") {

        val caught1 = intercept[TestFailedException] {
          HashSet(1, 2) should { contain element (5) and (contain element (2 - 1)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 5")

        val caught2 = intercept[TestFailedException] {
          HashSet(1, 2) should (contain element (5) and contain element (2 - 1))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 5")
      }

      it("should throw a TestFailedException when set doesn't contain the specified element and used in a logical-or expression") {

        val caught1 = intercept[TestFailedException] {
          HashSet(1, 2) should { contain element (55) or (contain element (22)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 55, and Set(1, 2) did not contain element 22")

        val caught2 = intercept[TestFailedException] {
          HashSet(1, 2) should (contain element (55) or contain element (22))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 55, and Set(1, 2) did not contain element 22")
      }

      it("should throw a TestFailedException when set contains the specified element and used in a logical-and expression with not") {

        val caught1 = intercept[TestFailedException] {
          HashSet(1, 2) should { not { contain element (3) } and not { contain element (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          HashSet(1, 2) should ((not contain element (3)) and (not contain element (2)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          HashSet(1, 2) should (not contain element (3) and not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when set contains the specified element and used in a logical-or expression with not") {

        val caught1 = intercept[TestFailedException] {
          HashSet(1, 2) should { not { contain element (2) } or not { contain element (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          HashSet(1, 2) should ((not contain element (2)) or (not contain element (2)))
        }
        assert(caught2.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          HashSet(1, 2) should (not contain element (2) or not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")
      }
    }

    describe("on scala.collection.mutable.HashSet") {

      import scala.collection.mutable

      it("should do nothing if set contains the specified element") {
        mutable.HashSet(1, 2) should contain element (2)
        mutable.HashSet(1, 2) should (contain element (2))
      }

      it("should do nothing if set does not contain the element and used with should not") {
        mutable.HashSet(1, 2) should not { contain element (3) }
        mutable.HashSet(1, 2) should not contain element (3)
      }

      it("should do nothing when set contains the specified element and used in a logical-and expression") {
        mutable.HashSet(1, 2) should { contain element (2) and (contain element (1)) }
        mutable.HashSet(1, 2) should ((contain element (2)) and (contain element (1)))
        mutable.HashSet(1, 2) should (contain element (2) and contain element (1))
       }

      it("should do nothing when set contains the specified element and used in a logical-or expression") {
        mutable.HashSet(1, 2) should { contain element (77) or (contain element (2)) }
        mutable.HashSet(1, 2) should ((contain element (77)) or (contain element (2)))
        mutable.HashSet(1, 2) should (contain element (77) or contain element (2))
      }

      it("should do nothing when set doesn't contain the specified element and used in a logical-and expression with not") {
        mutable.HashSet(1, 2) should { not { contain element (5) } and not { contain element (3) }}
        mutable.HashSet(1, 2) should ((not contain element (5)) and (not contain element (3)))
        mutable.HashSet(1, 2) should (not contain element (5) and not contain element (3))
      }

      it("should do nothing when set doesn't contain the specified element and used in a logical-or expression with not") {
        mutable.HashSet(1, 2) should { not { contain element (1) } or not { contain element (3) }}
        mutable.HashSet(1, 2) should ((not contain element (1)) or (not contain element (3)))
        mutable.HashSet(1, 2) should (not contain element (3) or not contain element (2))
      }

      it("should throw TestFailedException if set does not contain the specified element") {
        val caught = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should contain element (3)
        }
        assert(caught.getMessage === "Set(1, 2) did not contain element 3")
      }

      it("should throw TestFailedException if set contains the specified element, when used with not") {

        val caught1 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should not contain element (2)
        }
        assert(caught1.getMessage === "Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should not (contain element (2))
        }
        assert(caught2.getMessage === "Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should (not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when set doesn't contain the specified element and used in a logical-and expression") {

        val caught1 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should { contain element (5) and (contain element (2 - 1)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 5")

        val caught2 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should (contain element (5) and contain element (2 - 1))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 5")
      }

      it("should throw a TestFailedException when set doesn't contain the specified element and used in a logical-or expression") {

        val caught1 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should { contain element (55) or (contain element (22)) }
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 55, and Set(1, 2) did not contain element 22")

        val caught2 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should (contain element (55) or contain element (22))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 55, and Set(1, 2) did not contain element 22")
      }

      it("should throw a TestFailedException when set contains the specified element and used in a logical-and expression with not") {

        val caught1 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should { not { contain element (3) } and not { contain element (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should ((not contain element (3)) and (not contain element (2)))
        }
        assert(caught2.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should (not contain element (3) and not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) did not contain element 3, but Set(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when set contains the specified element and used in a logical-or expression with not") {

        val caught1 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should { not { contain element (2) } or not { contain element (2) }}
        }
        assert(caught1.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should ((not contain element (2)) or (not contain element (2)))
        }
        assert(caught2.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          mutable.HashSet(1, 2) should (not contain element (2) or not contain element (2))
        }
        assert(caught3.getMessage === "Set(1, 2) contained element 2, and Set(1, 2) contained element 2")
      }
    }

    describe("on List") {

      it("should do nothing if list contains the specified element") {
        List(1, 2) should contain element (2)
        List(1, 2) should (contain element (2))
        check((list: List[Int]) => list.size != 0 ==> returnsNormally(list should contain element (list(list.length - 1))))
      }

      it("should do nothing if list does not contain the element and used with should not") {
        List(1, 2) should not { contain element (3) }
        List(1, 2) should not contain element (3)
        check((list: List[Int], i: Int) => !list.exists(_ == i) ==> returnsNormally(list should not { contain element (i) }))
        check((list: List[Int], i: Int) => !list.exists(_ == i) ==> returnsNormally(list should not contain element (i)))
      }

      it("should do nothing when list contains the specified element and used in a logical-and expression") {
        List(1, 2) should { contain element (2) and (contain element (1)) }
        List(1, 2) should ((contain element (2)) and (contain element (1)))
        List(1, 2) should (contain element (2) and contain element (1))
       }

      it("should do nothing when list contains the specified element and used in a logical-or expression") {
        List(1, 2) should { contain element (77) or (contain element (2)) }
        List(1, 2) should ((contain element (77)) or (contain element (2)))
        List(1, 2) should (contain element (77) or contain element (2))
      }

      it("should do nothing when list doesn't contain the specified element and used in a logical-and expression with not") {
        List(1, 2) should { not { contain element (5) } and not { contain element (3) }}
        List(1, 2) should ((not contain element (5)) and (not contain element (3)))
        List(1, 2) should (not contain element (5) and not contain element (3))
      }

      it("should do nothing when list doesn't contain the specified element and used in a logical-or expression with not") {
        List(1, 2) should { not { contain element (1) } or not { contain element (3) }}
        List(1, 2) should ((not contain element (1)) or (not contain element (3)))
        List(1, 2) should (not contain element (3) or not contain element (2))
      }

      it("should throw TestFailedException if list does not contain the specified element") {
        val caught = intercept[TestFailedException] {
          List(1, 2) should contain element (3)
        }
        assert(caught.getMessage === "List(1, 2) did not contain element 3")
        check((list: List[String], s: String) => !list.exists(_ == s) ==> throwsTestFailedException(list should contain element (s)))
      }

      it("should throw TestFailedException if list contains the specified element, when used with not") {

        val caught1 = intercept[TestFailedException] {
          List(1, 2) should not contain element (2)
        }
        assert(caught1.getMessage === "List(1, 2) contained element 2")
        check((list: List[String]) => list.length > 0 ==> throwsTestFailedException(list should not contain element (list(0))))

        val caught2 = intercept[TestFailedException] {
          List(1, 2) should not (contain element (2))
        }
        assert(caught2.getMessage === "List(1, 2) contained element 2")
        check((list: List[String]) => list.length > 0 ==> throwsTestFailedException(list should not (contain element (list(0)))))

        val caught3 = intercept[TestFailedException] {
          List(1, 2) should (not contain element (2))
        }
        assert(caught3.getMessage === "List(1, 2) contained element 2")
        check((list: List[String]) => list.length > 0 ==> throwsTestFailedException(list should not (contain element (list(0)))))
      }

      it("should throw a TestFailedException when list doesn't contain the specified element and used in a logical-and expression") {

        val caught1 = intercept[TestFailedException] {
          List(1, 2) should { contain element (5) and (contain element (2 - 1)) }
        }
        assert(caught1.getMessage === "List(1, 2) did not contain element 5")

        val caught2 = intercept[TestFailedException] {
          List(1, 2) should (contain element (5) and contain element (2 - 1))
        }
        assert(caught2.getMessage === "List(1, 2) did not contain element 5")
      }

      it("should throw a TestFailedException when list doesn't contain the specified element and used in a logical-or expression") {

        val caught1 = intercept[TestFailedException] {
          List(1, 2) should { contain element (55) or (contain element (22)) }
        }
        assert(caught1.getMessage === "List(1, 2) did not contain element 55, and List(1, 2) did not contain element 22")

        val caught2 = intercept[TestFailedException] {
          List(1, 2) should (contain element (55) or contain element (22))
        }
        assert(caught2.getMessage === "List(1, 2) did not contain element 55, and List(1, 2) did not contain element 22")
      }

      it("should throw a TestFailedException when list contains the specified element and used in a logical-and expression with not") {

        val caught1 = intercept[TestFailedException] {
          List(1, 2) should { not { contain element (3) } and not { contain element (2) }}
        }
        assert(caught1.getMessage === "List(1, 2) did not contain element 3, but List(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          List(1, 2) should ((not contain element (3)) and (not contain element (2)))
        }
        assert(caught2.getMessage === "List(1, 2) did not contain element 3, but List(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          List(1, 2) should (not contain element (3) and not contain element (2))
        }
        assert(caught3.getMessage === "List(1, 2) did not contain element 3, but List(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when list contains the specified element and used in a logical-or expression with not") {

        val caught1 = intercept[TestFailedException] {
          List(1, 2) should { not { contain element (2) } or not { contain element (2) }}
        }
        assert(caught1.getMessage === "List(1, 2) contained element 2, and List(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          List(1, 2) should ((not contain element (2)) or (not contain element (2)))
        }
        assert(caught2.getMessage === "List(1, 2) contained element 2, and List(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          List(1, 2) should (not contain element (2) or not contain element (2))
        }
        assert(caught3.getMessage === "List(1, 2) contained element 2, and List(1, 2) contained element 2")
      }
    }

    describe("on java.util.List") {

      val javaList: java.util.List[Int] = new java.util.ArrayList
      javaList.add(1)
      javaList.add(2)
      
      it("should do nothing if list contains the specified element") {
        javaList should contain element (2)
        javaList should (contain element (2))
      }

      it("should do nothing if list does not contain the element and used with should not") {
        javaList should (not contain element (3))
        javaList should not { contain element (3) }
        javaList should not contain element (3)
      }

/*
      it("should do nothing when list contains the specified element and used in a logical-and expression") {
        javaList should { contain element (2) and (contain element (1)) }
        javaList should ((contain element (2)) and (contain element (1)))
        javaList should (contain element (2) and contain element (1))
       }

      it("should do nothing when list contains the specified element and used in a logical-or expression") {
        javaList should { contain element (77) or (contain element (2)) }
        javaList should ((contain element (77)) or (contain element (2)))
        javaList should (contain element (77) or contain element (2))
      }

      it("should do nothing when list doesn't contain the specified element and used in a logical-and expression with not") {
        javaList should { not { contain element (5) } and not { contain element (3) }}
        javaList should ((not contain element (5)) and (not contain element (3)))
        javaList should (not contain element (5) and not contain element (3))
      }

      it("should do nothing when list doesn't contain the specified element and used in a logical-or expression with not") {
        javaList should { not { contain element (1) } or not { contain element (3) }}
        javaList should ((not contain element (1)) or (not contain element (3)))
        javaList should (not contain element (3) or not contain element (2))
      }

      it("should throw TestFailedException if list does not contain the specified element") {
        val caught = intercept[TestFailedException] {
          javaList should contain element (3)
        }
        assert(caught.getMessage === "javaList did not contain element 3")
        check((list: javaList[String], s: String) => !list.exists(_ == s) ==> throwsTestFailedException(list should contain element (s)))
      }

      it("should throw TestFailedException if list contains the specified element, when used with not") {

        val caught1 = intercept[TestFailedException] {
          javaList should not contain element (2)
        }
        assert(caught1.getMessage === "javaList contained element 2")
        check((list: javaList[String]) => list.length > 0 ==> throwsTestFailedException(list should not contain element (list(0))))

        val caught2 = intercept[TestFailedException] {
          javaList should not (contain element (2))
        }
        assert(caught2.getMessage === "javaList contained element 2")
        check((list: javaList[String]) => list.length > 0 ==> throwsTestFailedException(list should not (contain element (list(0)))))

        val caught3 = intercept[TestFailedException] {
          javaList should (not contain element (2))
        }
        assert(caught3.getMessage === "javaList contained element 2")
        check((list: javaList[String]) => list.length > 0 ==> throwsTestFailedException(list should not (contain element (list(0)))))
      }

      it("should throw a TestFailedException when list doesn't contain the specified element and used in a logical-and expression") {

        val caught1 = intercept[TestFailedException] {
          javaList should { contain element (5) and (contain element (2 - 1)) }
        }
        assert(caught1.getMessage === "javaList did not contain element 5")

        val caught2 = intercept[TestFailedException] {
          javaList should (contain element (5) and contain element (2 - 1))
        }
        assert(caught2.getMessage === "javaList did not contain element 5")
      }

      it("should throw a TestFailedException when list doesn't contain the specified element and used in a logical-or expression") {

        val caught1 = intercept[TestFailedException] {
          javaList should { contain element (55) or (contain element (22)) }
        }
        assert(caught1.getMessage === "javaList did not contain element 55, and List(1, 2) did not contain element 22")

        val caught2 = intercept[TestFailedException] {
          javaList should (contain element (55) or contain element (22))
        }
        assert(caught2.getMessage === "javaList did not contain element 55, and List(1, 2) did not contain element 22")
      }

      it("should throw a TestFailedException when list contains the specified element and used in a logical-and expression with not") {

        val caught1 = intercept[TestFailedException] {
          javaList should { not { contain element (3) } and not { contain element (2) }}
        }
        assert(caught1.getMessage === "javaList did not contain element 3, but List(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          javaList should ((not contain element (3)) and (not contain element (2)))
        }
        assert(caught2.getMessage === "javaList did not contain element 3, but List(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          javaList should (not contain element (3) and not contain element (2))
        }
        assert(caught3.getMessage === "javaList did not contain element 3, but List(1, 2) contained element 2")
      }

      it("should throw a TestFailedException when list contains the specified element and used in a logical-or expression with not") {

        val caught1 = intercept[TestFailedException] {
          javaList should { not { contain element (2) } or not { contain element (2) }}
        }
        assert(caught1.getMessage === "javaList contained element 2, and List(1, 2) contained element 2")

        val caught2 = intercept[TestFailedException] {
          javaList should ((not contain element (2)) or (not contain element (2)))
        }
        assert(caught2.getMessage === "javaList contained element 2, and List(1, 2) contained element 2")

        val caught3 = intercept[TestFailedException] {
          javaList should (not contain element (2) or not contain element (2))
        }
        assert(caught3.getMessage === "javaList contained element 2, and List(1, 2) contained element 2")
      }
*/
    }
  }
}

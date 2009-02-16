/* * Copyright 2001-2008 Artima, Inc.
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
package org.scalatest.prop

import org.scalatest._
import org.scalacheck._
import Arbitrary._
import Prop._

class FunSuiteSuite extends prop.FunSuite {

  test("Checkers' check methods must be callable directly from prop.FunSuite") {

    // Ensure a success does not fail in an exception
    val propConcatLists = property((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
    check(propConcatLists)

    // Ensure a failed property does throw an assertion error
    val propConcatListsBadly = property((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size + 1)
    intercept[TestFailedException] {
      check(propConcatListsBadly)
    }

    // Ensure a property that throws an exception causes an assertion error
    val propConcatListsExceptionally = property((a: List[Int], b: List[Int]) => throw new StringIndexOutOfBoundsException)
    intercept[TestFailedException] {
      check(propConcatListsExceptionally)
    }

    // Ensure a property that doesn't generate enough test cases throws an assertion error
    val propTrivial = property( (n: Int) => (n == 0) ==> (n == 0) )
    intercept[TestFailedException] {
      check(propTrivial)
    }

    // Make sure a Generator that doesn't throw an exception works OK
    val smallInteger = Gen.choose(0, 100)
    val propSmallInteger = Prop.forAll(smallInteger)(n => n >= 0 && n <= 100)
    check(propSmallInteger)

    // Make sure a Generator that doesn't throw an exception works OK
    val smallEvenInteger = Gen.choose(0, 200) suchThat (_ % 2 == 0)
    val propEvenInteger = Prop.forAll(smallEvenInteger)(n => n >= 0 && n <= 200 && n % 2 == 0)
    check(propEvenInteger)

    // Make sure a Generator t throws an exception results in an TestFailedException
    // val smallEvenIntegerWithBug = Gen.choose(0, 200) suchThat (throw new ArrayIndexOutOfBoundsException)
    val smallEvenIntegerWithBug = Gen.choose(0, 200) suchThat (n => throw new ArrayIndexOutOfBoundsException)
    val propEvenIntegerWithBuggyGen = Prop.forAll(smallEvenIntegerWithBug)(n => n >= 0 && n <= 200 && n % 2 == 0)
    intercept[TestFailedException] {
      check(propEvenIntegerWithBuggyGen)
    }
  }

  test("tests that take properties get executed") {

    val a = new prop.FunSuite {
      var prop1Used = false
      var prop2Used = false
      val fun1 = (a: Int) => { prop1Used = true; true }
      val fun2 = (a: Int) => { prop2Used = true; true }
      test("this test", fun1)
      test("that test", fun2)
    }
    a.execute("this test")
    a.execute("that test")
    assert(a.prop1Used)
    assert(a.prop2Used)

    val b = new prop.FunSuite {
      var prop1Used = false
      var prop2Used = false
      val fun1 = (a: Int, b: Int) => { prop1Used = true; true }
      val fun2 = (a: Int, b: Int) => { prop2Used = true; true }
      test("this test", fun1)
      test("that test", fun2)
    }
    b.execute("this test")
    b.execute("that test")
    assert(b.prop1Used)
    assert(b.prop2Used)

    val c = new prop.FunSuite {
      var prop1Used = false
      var prop2Used = false
      val fun1 = (a: Int, b: Int, c: Int) => { prop1Used = true; true }
      val fun2 = (a: Int, b: Int, c: Int) => { prop2Used = true; true }
      test("this test", fun1)
      test("that test", fun2)
    }
    c.execute("this test")
    c.execute("that test")
    assert(c.prop1Used)
    assert(c.prop2Used)

    val d = new prop.FunSuite {
      var prop1Used = false
      var prop2Used = false
      val fun1 = (a: Int, b: Int, c: Int, d: Int) => { prop1Used = true; true }
      val fun2 = (a: Int, b: Int, c: Int, d: Int) => { prop2Used = true; true }
      test("this test", fun1)
      test("that test", fun2)
    }
    d.execute("this test")
    d.execute("that test")
    assert(d.prop1Used)
    assert(d.prop2Used)

    val e = new prop.FunSuite {
      var prop1Used = false
      var prop2Used = false
      val fun1 = (a: Int, b: Int, c: Int, d: Int, e: Int) => { prop1Used = true; true }
      val fun2 = (a: Int, b: Int, c: Int, d: Int, e: Int) => { prop2Used = true; true }
      test("this test", fun1)
      test("that test", fun2)
    }
    e.execute("this test")
    e.execute("that test")
    assert(e.prop1Used)
    assert(e.prop2Used)

    val f = new prop.FunSuite {
      var prop1Used = false
      var prop2Used = false
      val fun1 = (a: Int, b: Int, c: Int, d: Int, e: Int, f: Int) => { prop1Used = true; true }
      val fun2 = (a: Int, b: Int, c: Int, d: Int, e: Int, f: Int) => { prop2Used = true; true }
      test("this test", fun1)
      test("that test", fun2)
    }
    f.execute("this test")
    f.execute("that test")
    assert(f.prop1Used)
    assert(f.prop2Used)
  }
}


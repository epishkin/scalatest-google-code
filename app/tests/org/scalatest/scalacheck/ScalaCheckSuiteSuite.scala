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
package org.scalatest.scalacheck

import org.scalacheck._
import Arbitrary._
import Prop._

class ScalaCheckSuiteSuite extends ScalaCheckSuite {

  def testCheckProp() {

    // Ensure a success does not fail in an exception
    val propConcatLists = property((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
    checkProperty(propConcatLists)

    // Ensure a failed property does throw an assertion error
    val propConcatListsBadly = property((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size + 1)
    intercept(classOf[AssertionError]) {
      checkProperty(propConcatListsBadly)
    }

    // Ensure a property that throws an exception causes an assertion error
    val propConcatListsExceptionally = property((a: List[Int], b: List[Int]) => throw new StringIndexOutOfBoundsException)
    intercept(classOf[AssertionError]) {
      checkProperty(propConcatListsExceptionally)
    }

    // Ensure a property that doesn't generate enough test cases throws an assertion error
    val propTrivial = property( (n: Int) => (n == 0) ==> (n == 0) )
    intercept(classOf[AssertionError]) {
      checkProperty(propTrivial)
    }

    // Make sure a Generator that doesn't throw an exception works OK
    val smallInteger = Gen.choose(0, 100)
    val propSmallInteger = Prop.forAll(smallInteger)(n => n >= 0 && n <= 100)
    checkProperty(propSmallInteger)

    // Make sure a Generator that doesn't throw an exception works OK
    val smallEvenInteger = Gen.choose(0, 200) suchThat (_ % 2 == 0)
    val propEvenInteger = Prop.forAll(smallEvenInteger)(n => n >= 0 && n <= 200 && n % 2 == 0)
    checkProperty(propEvenInteger)

    // Make sure a Generator t throws an exception results in an AssertionError
    // val smallEvenIntegerWithBug = Gen.choose(0, 200) suchThat (throw new ArrayIndexOutOfBoundsException)
    val smallEvenIntegerWithBug = Gen.choose(0, 200) suchThat (n => throw new ArrayIndexOutOfBoundsException)
    val propEvenIntegerWithBuggyGen = Prop.forAll(smallEvenIntegerWithBug)(n => n >= 0 && n <= 200 && n % 2 == 0)
    intercept(classOf[AssertionError]) {
      checkProperty(propEvenIntegerWithBuggyGen)
    }
  }
}

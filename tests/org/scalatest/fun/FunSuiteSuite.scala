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
package org.scalatest.fun

import org.scalatest._
import org.scalacheck._
import Arbitrary._
import Prop._

/*
class SuiteFriend(suite: Suite) {

  def simpleNameForTest(testName: String) = {
    val m = Class.forName("org.scalatest.Suite$class").getDeclaredMethod("simpleNameForTest", Array(classOf[org.scalatest.Suite], classOf[String]))
    m.setAccessible(true)
    m.invoke(suite, Array[Object](suite, testName)).asInstanceOf[String]
  }
}
*/

// Group classes used in tests
object SlowAsMolasses extends Group("org.scalatest.SlowAsMolasses")
object FastAsLight extends Group("org.scalatest.FastAsLight")
object WeakAsAKitten extends Group("org.scalatest.WeakAsAKitten")

class FunSuiteSuite extends Suite {

  def testSpecifyMethods() {

    val a = new FunSuite {
      specify("test this") {}
      specifyWithReporter("test that") { reporter => () }
    }

    expect(List("test this", "test that")) {
      a.testNames.elements.toList
    }
  }

  def testTestNames() {

    val a = new FunSuite {
      test("test this") {}
      testWithReporter("test that") { reporter => () }
    }

    expect(List("test this", "test that")) {
      a.testNames.elements.toList
    }

    val b = new FunSuite {}

    expect(List[String]()) {
      b.testNames.elements.toList
    }

    val c = new FunSuite {
      test("test this") {}
      testWithReporter("test that") { reporter => () }
    }

    expect(List("test this", "test that")) {
      c.testNames.elements.toList
    }

    // Test duplicate names
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        test("test this") {}
        testWithReporter("test this") { reporter => () }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        testWithReporter("test this") { reporter => () }
        test("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        testWithReporter("test this") { reporter => () }
        ignore("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        ignoreWithReporter("test this") { reporter => () }
        ignore("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        ignore("test this") {}
        ignoreWithReporter("test this") { reporter => () }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        test("test this") {}
        ignoreWithReporter("test this") { reporter => () }
      }
    }

    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        test("test this") {}
        testWithReporter("test this") { reporter => () }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        testWithReporter("test this") { reporter => () }
        test("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        testWithReporter("test this") { reporter => () }
        ignore("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        test("test this") {}
        ignoreWithReporter("test this") { reporter => () }
      }
    }

    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        testWithReporter("test this") { reporter => () }
        test("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        test("test this") {}
        testWithReporter("test this") { reporter => () }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        test("test this") {}
        testWithReporter("test this") { reporter => () }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        testWithReporter("test this") { reporter => () }
        test("test this") {}
      }
    }
  }

  def testTestGroups() {
    
    val a = new FunSuite {
      ignore("test this") {}
      testWithReporter("test that") { reporter => () }
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"))) {
      a.groups
    }

    val b = new FunSuite {
      test("test this") {}
      ignoreWithReporter("test that") { reporter => () }
    }
    expect(Map("test that" -> Set("org.scalatest.Ignore"))) {
      b.groups
    }

    val c = new FunSuite {
      ignore("test this") {}
      ignoreWithReporter("test that") { reporter => () }
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"), "test that" -> Set("org.scalatest.Ignore"))) {
      c.groups
    }

    val d = new FunSuite {
      test("test this", SlowAsMolasses) {}
      ignoreWithReporter("test that", SlowAsMolasses) { reporter => () }
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
      d.groups
    }

    val e = new FunSuite {}
    expect(Map()) {
      e.groups
    }

    val f = new FunSuite {
      test("test this", SlowAsMolasses, WeakAsAKitten) {}
      testWithReporter("test that", SlowAsMolasses) { reporter => () }
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
      f.groups
    }
  }

  def testExecuteOneTest() {
    
    class MySuite extends FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this") { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
    }

    val a = new MySuite 
    a.execute("test this")
    assert(a.theTestThisCalled)
    assert(!a.theTestThatCalled)

    val b = new MySuite
    b.execute()
    assert(b.theTestThisCalled)
    assert(b.theTestThatCalled)
  }

  class MyReporter extends Reporter {
    var testIgnoredCalled = false
    var lastReport: Report = null
    override def testIgnored(report: Report) {
      testIgnoredCalled = true
      lastReport = report
    }
  }

  def testTestMethodsWithIgnores() {

    val a = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this") { theTestThisCalled = true }
      testWithReporter("test that") { reporter => theTestThatCalled = true }
    }

    val repA = new MyReporter
    a.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repA.testIgnoredCalled)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      testWithReporter("test that") { reporter => theTestThatCalled = true }
    }

    val repB = new MyReporter
    b.execute(None, repB, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repB.testIgnoredCalled)
    assert(repB.lastReport.name endsWith "test this")
    assert(!b.theTestThisCalled)
    assert(b.theTestThatCalled)

    val c = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this") { theTestThisCalled = true }
      ignoreWithReporter("test that") { reporter => theTestThatCalled = true }
    }

    val repC = new MyReporter
    c.execute(None, repC, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repC.testIgnoredCalled)
    assert(repC.lastReport.name endsWith "test that", repC.lastReport.name)
    assert(c.theTestThisCalled)
    assert(!c.theTestThatCalled)

    // The order I want is order of appearance in the file.
    // Will try and implement that tomorrow. Subtypes will be able to change the order.
    val d = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      ignoreWithReporter("test that") { reporter => theTestThatCalled = true }
    }

    val repD = new MyReporter
    d.execute(None, repD, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repD.testIgnoredCalled)
    assert(repD.lastReport.name endsWith "test that") // last because should be in order of appearance
    assert(!d.theTestThisCalled)
    assert(!d.theTestThatCalled)

    // If I provide a specific testName to execute, then it should ignore an Ignore on that test
    // method and actually invoke it.
    val e = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      testWithReporter("test that") { reporter => theTestThatCalled = true }
    }

    val repE = new MyReporter
    e.execute(Some("test this"), repE, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repE.testIgnoredCalled)
    assert(e.theTestThisCalled)
  }

  def testExcludes() {

    val a = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", SlowAsMolasses) { theTestThisCalled = true }
      testWithReporter("test that") { reporter => theTestThatCalled = true }
    }
    val repA = new MyReporter
    a.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repA.testIgnoredCalled)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", SlowAsMolasses) { theTestThisCalled = true }
      testWithReporter("test that") { reporter => theTestThatCalled = true }
    }
    val repB = new MyReporter
    b.execute(None, repB, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set(), Map(), None)
    assert(!repB.testIgnoredCalled)
    assert(b.theTestThisCalled)
    assert(!b.theTestThatCalled)

    val c = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", SlowAsMolasses) { theTestThisCalled = true }
      testWithReporter("test that", SlowAsMolasses) { reporter => theTestThatCalled = true }
    }
    val repC = new MyReporter
    c.execute(None, repB, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set(), Map(), None)
    assert(!repC.testIgnoredCalled)
    assert(c.theTestThisCalled)
    assert(c.theTestThatCalled)

    val d = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this", SlowAsMolasses) { theTestThisCalled = true }
      testWithReporter("test that", SlowAsMolasses) { reporter => theTestThatCalled = true }
    }
    val repD = new MyReporter
    d.execute(None, repD, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.Ignore"), Map(), None)
    assert(repD.testIgnoredCalled)
    assert(!d.theTestThisCalled)
    assert(d.theTestThatCalled)

    val e = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", SlowAsMolasses, FastAsLight) { theTestThisCalled = true }
      testWithReporter("test that", SlowAsMolasses) { reporter => theTestThatCalled = true }
      testWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
    }
    val repE = new MyReporter
    e.execute(None, repE, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight"),
              Map(), None)
    assert(!repE.testIgnoredCalled)
    assert(!e.theTestThisCalled)
    assert(e.theTestThatCalled)
    assert(!e.theTestTheOtherCalled)

    val f = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignore("test this", SlowAsMolasses, FastAsLight) { theTestThisCalled = true }
      testWithReporter("test that", SlowAsMolasses) { reporter => theTestThatCalled = true }
      testWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
    }
    val repF = new MyReporter
    f.execute(None, repF, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight"),
              Map(), None)
    assert(!repF.testIgnoredCalled)
    assert(!f.theTestThisCalled)
    assert(f.theTestThatCalled)
    assert(!f.theTestTheOtherCalled)

    val g = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", SlowAsMolasses, FastAsLight) { theTestThisCalled = true }
      testWithReporter("test that", SlowAsMolasses) { reporter => theTestThatCalled = true }
      ignoreWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
    }
    val repG = new MyReporter
    g.execute(None, repG, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight"),
              Map(), None)
    assert(!repG.testIgnoredCalled)
    assert(!g.theTestThisCalled)
    assert(g.theTestThatCalled)
    assert(!g.theTestTheOtherCalled)

    val h = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", SlowAsMolasses, FastAsLight) { theTestThisCalled = true }
      testWithReporter("test that", SlowAsMolasses) { reporter => theTestThatCalled = true }
      testWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
    }
    val repH = new MyReporter
    h.execute(None, repH, new Stopper {}, Set(), Set("org.scalatest.FastAsLight"), Map(), None)
    assert(!repH.testIgnoredCalled)
    assert(!h.theTestThisCalled)
    assert(h.theTestThatCalled)
    assert(h.theTestTheOtherCalled)

    val i = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", SlowAsMolasses, FastAsLight) { theTestThisCalled = true }
      testWithReporter("test that", SlowAsMolasses) { reporter => theTestThatCalled = true }
      testWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
    }
    val repI = new MyReporter
    i.execute(None, repI, new Stopper {}, Set(), Set("org.scalatest.SlowAsMolasses"), Map(), None)
    assert(!repI.testIgnoredCalled)
    assert(!i.theTestThisCalled)
    assert(!i.theTestThatCalled)
    assert(i.theTestTheOtherCalled)

    val j = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignore("test this", SlowAsMolasses, FastAsLight) { theTestThisCalled = true }
      ignoreWithReporter("test that", SlowAsMolasses) { reporter => theTestThatCalled = true }
      testWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
    }
    val repJ = new MyReporter
    j.execute(None, repJ, new Stopper {}, Set(), Set("org.scalatest.SlowAsMolasses"), Map(), None)
    assert(!repI.testIgnoredCalled)
    assert(!j.theTestThisCalled)
    assert(!j.theTestThatCalled)
    assert(j.theTestTheOtherCalled)

    val k = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignore("test this", SlowAsMolasses, FastAsLight) { theTestThisCalled = true }
      ignoreWithReporter("test that", SlowAsMolasses) { reporter => theTestThatCalled = true }
      ignoreWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
    }
    val repK = new MyReporter
    k.execute(None, repK, new Stopper {}, Set(), Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore"), Map(), None)
    assert(repK.testIgnoredCalled)
    assert(!k.theTestThisCalled)
    assert(!k.theTestThatCalled)
    assert(!k.theTestTheOtherCalled)
  }

  def testTestCount() {

    val a = new FunSuite {
      test("test this") {}
      testWithReporter("test that") { reporter => () }
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)

    val b = new FunSuite {
      ignore("test this") {}
      testWithReporter("test that") { reporter => () }
    }
    assert(b.expectedTestCount(Set(), Set()) === 1)

    val c = new FunSuite {
      test("test this", FastAsLight) {}
      testWithReporter("test that") { reporter => () }
    }
    assert(c.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(c.expectedTestCount(Set(), Set("org.scalatest.FastAsLight")) === 1)

    val d = new FunSuite {
      test("test this", FastAsLight, SlowAsMolasses) {}
      testWithReporter("test that", SlowAsMolasses) { reporter => () }
      testWithReporter("test the other thing") { reporter => () }
    }
    assert(d.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(d.expectedTestCount(Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight")) === 1)
    assert(d.expectedTestCount(Set(), Set("org.scalatest.SlowAsMolasses")) === 1)
    assert(d.expectedTestCount(Set(), Set()) === 3)

    val e = new FunSuite {
      test("test this", FastAsLight, SlowAsMolasses) {}
      testWithReporter("test that", SlowAsMolasses) { reporter => () }
      ignoreWithReporter("test the other thing") { reporter => () }
    }
    assert(e.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(e.expectedTestCount(Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight")) === 1)
    assert(e.expectedTestCount(Set(), Set("org.scalatest.SlowAsMolasses")) === 0)
    assert(e.expectedTestCount(Set(), Set()) === 2)

    val f = new SuperSuite(List(a, b, c, d, e))
    assert(f.expectedTestCount(Set(), Set()) === 10)
  }

  def testThatTestMethodsWithNoGroupsDontShowUpInGroupsMap() {
    
    val a = new FunSuite {
      test("test not in a group") {}
    }
    assert(a.groups.keySet.size === 0)
  }

  def testThatTestFunctionsThatResultInNonUnitAreRegistered() {
    val a = new FunSuite {
      test("test this") { 1 }
      testWithReporter("test that") { reporter => "hi" }
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)
    assert(a.testNames.size === 2)
    assert(a.groups.keySet.size === 0)
  }

  def testThatTestNameCantBeReused() {
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        test("test this") {}
        test("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        ignore("test this") {}
        test("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        test("test this") {}
        ignore("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        test("test this") {}
        testWithReporter("test this") { reporter => () }
      }
    }
  }
}

class FunSuiteCheckSuite extends FunSuite {

  specify("Checkers' check methods must be callable directly from FunSuite") {

    // Ensure a success does not fail in an exception
    val propConcatLists = property((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
    check(propConcatLists)

    // Ensure a failed property does throw an assertion error
    val propConcatListsBadly = property((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size + 1)
    intercept(classOf[AssertionError]) {
      check(propConcatListsBadly)
    }

    // Ensure a property that throws an exception causes an assertion error
    val propConcatListsExceptionally = property((a: List[Int], b: List[Int]) => throw new StringIndexOutOfBoundsException)
    intercept(classOf[AssertionError]) {
      check(propConcatListsExceptionally)
    }

    // Ensure a property that doesn't generate enough test cases throws an assertion error
    val propTrivial = property( (n: Int) => (n == 0) ==> (n == 0) )
    intercept(classOf[AssertionError]) {
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

    // Make sure a Generator t throws an exception results in an AssertionError
    // val smallEvenIntegerWithBug = Gen.choose(0, 200) suchThat (throw new ArrayIndexOutOfBoundsException)
    val smallEvenIntegerWithBug = Gen.choose(0, 200) suchThat (n => throw new ArrayIndexOutOfBoundsException)
    val propEvenIntegerWithBuggyGen = Prop.forAll(smallEvenIntegerWithBug)(n => n >= 0 && n <= 200 && n % 2 == 0)
    intercept(classOf[AssertionError]) {
      check(propEvenIntegerWithBuggyGen)
    }
  }

  specify("tests that take properties get executed") {

    val a = new FunSuite {
      var propUsed = false
      val fun = (a: Int) => { propUsed = true; true }
      test("this test", fun)
    }

    a.execute("this test")
    assert(a.propUsed)
  }
}


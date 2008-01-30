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
import scala.collection.immutable.TreeSet

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
private case class SlowAsMolasses extends Group
private case class WeakAsAKitten extends Group

class FunSuiteSuite extends Suite {

  def testTestNames() {

    val a = new FunSuite {
      test("test this") {}
      testWithReporter("test that") { Reporter => () }
    }

    expect(TreeSet("test this", "test that")) {
      a.testNames
    }

    val b = new FunSuite {}

    expect(TreeSet[String]()) {
      b.testNames
    }

    val c = new FunSuite {
      specify("test this") {}
      specifyWithReporter("test that") { Reporter => () }
    }

    expect(TreeSet("test this", "test that")) {
      c.testNames
    }

    // Test duplicate names
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        test("test this") {}
        testWithReporter("test this") { Reporter => () }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        testWithReporter("test this") { Reporter => () }
        test("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        testWithReporter("test this") { Reporter => () }
        ignore("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        ignoreWithReporter("test this") { Reporter => () }
        ignore("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        ignore("test this") {}
        ignoreWithReporter("test this") { Reporter => () }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        test("test this") {}
        ignoreWithReporter("test this") { Reporter => () }
      }
    }

    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        specify("test this") {}
        specifyWithReporter("test this") { Reporter => () }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        specifyWithReporter("test this") { Reporter => () }
        specify("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        specifyWithReporter("test this") { Reporter => () }
        ignore("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        specify("test this") {}
        ignoreWithReporter("test this") { Reporter => () }
      }
    }

    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        specifyWithReporter("test this") { Reporter => () }
        test("test this") {}
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        specify("test this") {}
        testWithReporter("test this") { Reporter => () }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        test("test this") {}
        specifyWithReporter("test this") { Reporter => () }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite {
        testWithReporter("test this") { Reporter => () }
        specify("test this") {}
      }
    }
  }

  def testTestGroups() {
    
    val a = new FunSuite {
      ignore("test this") {}
      testWithReporter("test that") { Reporter => () }
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"))) {
      a.groups
    }

    val b = new FunSuite {
      test("test this") {}
      ignoreWithReporter("test that") { Reporter => () }
    }
    expect(Map("test that (Reporter)" -> Set("org.scalatest.Ignore"))) {
      b.groups
    }

    val c = new FunSuite {
      ignore("test this") {}
      ignoreWithReporter("test that") { Reporter => () }
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"), "test that (Reporter)" -> Set("org.scalatest.Ignore"))) {
      c.groups
    }

    val d = new FunSuite {
      test("test this", SlowAsMolasses) {}
      ignoreWithReporter("test that", SlowAsMolasses) { Reporter => () }
    }
    expect(Map("test this" -> Set("org.scalatest.fun.SlowAsMolasses"), "test that (Reporter)" -> Set("org.scalatest.Ignore", "org.scalatest.fun.SlowAsMolasses"))) {
      d.groups
    }

    val e = new FunSuite {}
    expect(Map()) {
      e.groups
    }

    val f = new FunSuite {
      specify("test this", SlowAsMolasses, WeakAsAKitten) {}
      specifyWithReporter("test that", SlowAsMolasses) { Reporter => () }
    }
    expect(Map("test this" -> Set("org.scalatest.fun.SlowAsMolasses", "org.scalatest.fun.WeakAsAKitten"), "test that (Reporter)" -> Set("org.scalatest.fun.SlowAsMolasses"))) {
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

/*
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
      def testThis() = { theTestThisCalled = true }
      def testThat(reporter: Reporter) = { theTestThatCalled = true }
    }

    val repA = new MyReporter
    a.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repA.testIgnoredCalled)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      @Ignore
      def testThis() = { theTestThisCalled = true }
      def testThat(reporter: Reporter) = { theTestThatCalled = true }
    }

    val repB = new MyReporter
    b.execute(None, repB, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repB.testIgnoredCalled)
    assert(repB.lastReport.name endsWith "testThis")
    assert(!b.theTestThisCalled)
    assert(b.theTestThatCalled)

    val c = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      def testThis() = { theTestThisCalled = true }
      @Ignore
      def testThat(reporter: Reporter) = { theTestThatCalled = true }
    }

    val repC = new MyReporter
    c.execute(None, repC, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repC.testIgnoredCalled)
    assert(repC.lastReport.name endsWith "testThat(Reporter)", repC.lastReport.name)
    assert(c.theTestThisCalled)
    assert(!c.theTestThatCalled)

    val d = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      @Ignore
      def testThis() = { theTestThisCalled = true }
      @Ignore
      def testThat(reporter: Reporter) = { theTestThatCalled = true }
    }

    val repD = new MyReporter
    d.execute(None, repD, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repD.testIgnoredCalled)
    assert(repD.lastReport.name endsWith "testThis") // last because executed alphabetically
    assert(!d.theTestThisCalled)
    assert(!d.theTestThatCalled)

    // If I provide a specific testName to execute, then it should ignore an Ignore on that test
    // method and actually invoke it.

    val e = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      @Ignore
      def testThis() { theTestThisCalled = true }
      def testThat(report: Reporter) { theTestThatCalled = true }
    }

    val repE = new MyReporter
    e.execute(Some("testThis"), repE, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repE.testIgnoredCalled)
    assert(e.theTestThisCalled)
  }

  def testExcludes() {

    val a = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      @SlowAsMolasses
      def testThis() { theTestThisCalled = true }
      def testThat(report: Reporter) { theTestThatCalled = true }
    }
    val repA = new MyReporter
    a.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repA.testIgnoredCalled)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      @SlowAsMolasses
      def testThis() { theTestThisCalled = true }
      def testThat(report: Reporter) { theTestThatCalled = true }
    }
    val repB = new MyReporter
    b.execute(None, repB, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set(), Map(), None)
    assert(!repB.testIgnoredCalled)
    assert(b.theTestThisCalled)
    assert(!b.theTestThatCalled)

    val c = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      @SlowAsMolasses
      def testThis() { theTestThisCalled = true }
      @SlowAsMolasses
      def testThat(report: Reporter) { theTestThatCalled = true }
    }
    val repC = new MyReporter
    c.execute(None, repB, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set(), Map(), None)
    assert(!repC.testIgnoredCalled)
    assert(c.theTestThisCalled)
    assert(c.theTestThatCalled)

    val d = new FunSuite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      @Ignore
      @SlowAsMolasses
      def testThis() { theTestThisCalled = true }
      @SlowAsMolasses
      def testThat(report: Reporter) { theTestThatCalled = true }
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
      @FastAsLight
      @SlowAsMolasses
      def testThis() { theTestThisCalled = true }
      @SlowAsMolasses
      def testThat(report: Reporter) { theTestThatCalled = true }
      def testTheOther(report: Reporter) { theTestTheOtherCalled = true }
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
      @Ignore
      @FastAsLight
      @SlowAsMolasses
      def testThis() { theTestThisCalled = true }
      @SlowAsMolasses
      def testThat(report: Reporter) { theTestThatCalled = true }
      def testTheOther(report: Reporter) { theTestTheOtherCalled = true }
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
      @FastAsLight
      @SlowAsMolasses
      def testThis() { theTestThisCalled = true }
      @SlowAsMolasses
      def testThat(report: Reporter) { theTestThatCalled = true }
      @Ignore
      def testTheOther(report: Reporter) { theTestTheOtherCalled = true }
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
      @FastAsLight
      @SlowAsMolasses
      def testThis() { theTestThisCalled = true }
      @SlowAsMolasses
      def testThat(report: Reporter) { theTestThatCalled = true }
      def testTheOther(report: Reporter) { theTestTheOtherCalled = true }
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
      @FastAsLight
      @SlowAsMolasses
      def testThis() { theTestThisCalled = true }
      @SlowAsMolasses
      def testThat(report: Reporter) { theTestThatCalled = true }
      def testTheOther(report: Reporter) { theTestTheOtherCalled = true }
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
      @Ignore
      @FastAsLight
      @SlowAsMolasses
      def testThis() { theTestThisCalled = true }
      @Ignore
      @SlowAsMolasses
      def testThat(report: Reporter) { theTestThatCalled = true }
      def testTheOther(report: Reporter) { theTestTheOtherCalled = true }
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
      @Ignore
      @FastAsLight
      @SlowAsMolasses
      def testThis() { theTestThisCalled = true }
      @Ignore
      @SlowAsMolasses
      def testThat(report: Reporter) { theTestThatCalled = true }
      @Ignore
      def testTheOther(report: Reporter) { theTestTheOtherCalled = true }
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
      def testThis() = ()
      def testThat(reporter: Reporter) = ()
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)

    val b = new FunSuite {
      @Ignore
      def testThis() = ()
      def testThat(reporter: Reporter) = ()
    }
    assert(b.expectedTestCount(Set(), Set()) === 1)

    val c = new FunSuite {
      @FastAsLight
      def testThis() = ()
      def testThat(reporter: Reporter) = ()
    }
    assert(c.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(c.expectedTestCount(Set(), Set("org.scalatest.FastAsLight")) === 1)

    val d = new FunSuite {
      @FastAsLight
      @SlowAsMolasses
      def testThis() = ()
      @SlowAsMolasses
      def testThat(reporter: Reporter) = ()
      def testTheOtherThing(reporter: Reporter) = ()
    }
    assert(d.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(d.expectedTestCount(Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight")) === 1)
    assert(d.expectedTestCount(Set(), Set("org.scalatest.SlowAsMolasses")) === 1)
    assert(d.expectedTestCount(Set(), Set()) === 3)

    val e = new FunSuite {
      @FastAsLight
      @SlowAsMolasses
      def testThis() = ()
      @SlowAsMolasses
      def testThat(reporter: Reporter) = ()
      @Ignore
      def testTheOtherThing(reporter: Reporter) = ()
    }
    assert(e.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(e.expectedTestCount(Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight")) === 1)
    assert(e.expectedTestCount(Set(), Set("org.scalatest.SlowAsMolasses")) === 0)
    assert(e.expectedTestCount(Set(), Set()) === 2)

    val f = new SuperSuite(List(a, b, c, d, e))
    assert(f.expectedTestCount(Set(), Set()) === 10)
  }

  def testNamesAndGroupsMethodsDiscovered() {

    val a = new FunSuite {
      def testNames(reporter: Reporter): Unit = ()
    }
    assert(a.expectedTestCount(Set(), Set()) === 1)
    val tnResult: Set[String] = a.testNames
    val gResult: Map[String, Set[String]] = a.groups
    assert(tnResult.size === 1)
    assert(gResult.keySet.size === 0)
  }

  def testThatTestMethodsWithNoGroupsDontShowUpInGroupsMap() {
    
    val a = new FunSuite {
      def testNotInAGroup() = ()
    }
    assert(a.groups.keySet.size === 0)
  }

  def testThatTestMethodsThatReturnNonUnitAreDiscovered() {
    val a = new FunSuite {
      def testThis(): Int = 1
      def testThat(reporter: Reporter): String = "hi"
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)
    assert(a.testNames.size === 2)
    assert(a.groups.keySet.size === 0)
  }

  def testThatOverloadedTestMethodsAreDiscovered() {
    val a = new FunSuite {
      def testThis() = ()
      def testThis(reporter: Reporter) = ()
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)
    assert(a.testNames.size === 2)
    assert(a.groups.keySet.size === 0)
  }

  def testThatInterceptCatchesSubtypes() {
    class MyException extends RuntimeException
    class MyExceptionSubClass extends MyException
    intercept(classOf[MyException]) {
      throw new MyException
    }
    intercept(classOf[MyException]) {
      throw new MyExceptionSubClass
    }
    // Try with a trait
    trait MyTrait
    class AnotherException extends RuntimeException with MyTrait
    intercept(classOf[MyTrait]) {
      throw new AnotherException
    }
  }

  def testThatInterceptReturnsTheCaughtException() {
    val e = new RuntimeException
    val result = intercept(classOf[RuntimeException]) {
      throw e
    }
    assert(result === e)
  }
*/
}


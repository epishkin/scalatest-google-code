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

import scala.collection.immutable.TreeSet

class SuiteFriend(suite: Suite) {

  def simpleNameForTest(testName: String) = {
    val m = Class.forName("org.scalatest.Suite$class").getDeclaredMethod("simpleNameForTest", Array(classOf[org.scalatest.Suite], classOf[String]))
    m.setAccessible(true)
    m.invoke(suite, Array[Object](suite, testName)).asInstanceOf[String]
  }
}

class SuiteSuite extends Suite {

  def testSimpleNameForTest() {
    val s = new SuiteFriend(new Suite {})
    assert(s.simpleNameForTest("testThis") === "testThis")
    assert(s.simpleNameForTest("testThis(Reporter)") === "testThis")
    assert(s.simpleNameForTest("test(Reporter)") === "test")
    assert(s.simpleNameForTest("test") === "test")
  }

  def testTestNames() {

    val a = new Suite {
      def testThis() = ()
      def testThat(reporter: Reporter) = ()
    }
    assert(a.testNames === TreeSet("testThat(Reporter)", "testThis"))

    val b = new Suite {}
    assert(b.testNames === TreeSet[String]())
  }

  def testTestGroups() {
    
    val a = new Suite {
      @Ignore
      def testThis() = ()
      def testThat(reporter: Reporter) = ()
    }

    assert(a.groups === Map("testThis" -> Set("org.scalatest.Ignore")))

    val b = new Suite {
      def testThis() = ()
      @Ignore
      def testThat(reporter: Reporter) = ()
    }

    assert(b.groups === Map("testThat(Reporter)" -> Set("org.scalatest.Ignore")))

    val c = new Suite {
      @Ignore
      def testThis() = ()
      @Ignore
      def testThat(reporter: Reporter) = ()
    }

    assert(c.groups === Map("testThis" -> Set("org.scalatest.Ignore"), "testThat(Reporter)" -> Set("org.scalatest.Ignore")))

    val d = new Suite {
      @SlowAsMolasses
      def testThis() = ()
      @SlowAsMolasses
      @Ignore
      def testThat(reporter: Reporter) = ()
    }

    assert(d.groups === Map("testThis" -> Set("org.scalatest.SlowAsMolasses"), "testThat(Reporter)" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses")))

    val e = new Suite {}
    assert(e.groups === Map())
  }

  def testExecuteOneTest() {
    
    class MySuite extends Suite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      def testThis() { theTestThisCalled = true }
      def testThat() { theTestThatCalled = true }
    }

    val a = new MySuite 
    a.execute("testThis")
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

    val a = new Suite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      def testThis() { theTestThisCalled = true }
      def testThat(reporter: Reporter) { theTestThatCalled = true }
    }

    val repA = new MyReporter
    a.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repA.testIgnoredCalled)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new Suite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      @Ignore
      def testThis() { theTestThisCalled = true }
      def testThat(reporter: Reporter) { theTestThatCalled = true }
    }

    val repB = new MyReporter
    b.execute(None, repB, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repB.testIgnoredCalled)
    assert(repB.lastReport.name endsWith "testThis")
    assert(!b.theTestThisCalled)
    assert(b.theTestThatCalled)

    val c = new Suite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      def testThis() { theTestThisCalled = true }
      @Ignore
      def testThat(reporter: Reporter) { theTestThatCalled = true }
    }

    val repC = new MyReporter
    c.execute(None, repC, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repC.testIgnoredCalled)
    assert(repC.lastReport.name endsWith "testThat(Reporter)", repC.lastReport.name)
    assert(c.theTestThisCalled)
    assert(!c.theTestThatCalled)

    val d = new Suite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      @Ignore
      def testThis() { theTestThisCalled = true }
      @Ignore
      def testThat(reporter: Reporter) { theTestThatCalled = true }
    }

    val repD = new MyReporter
    d.execute(None, repD, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repD.testIgnoredCalled)
    assert(repD.lastReport.name endsWith "testThis") // last because executed alphabetically
    assert(!d.theTestThisCalled)
    assert(!d.theTestThatCalled)

    // If I provide a specific testName to execute, then it should ignore an Ignore on that test
    // method and actually invoke it.

    val e = new Suite {
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

    val a = new Suite {
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

    val b = new Suite {
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

    val c = new Suite {
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

    val d = new Suite {
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

    val e = new Suite {
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

    val f = new Suite {
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

    val g = new Suite {
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

    val h = new Suite {
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

    val i = new Suite {
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

    val j = new Suite {
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

    val k = new Suite {
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

    val a = new Suite {
      def testThis() = ()
      def testThat(reporter: Reporter) = ()
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)

    val b = new Suite {
      @Ignore
      def testThis() = ()
      def testThat(reporter: Reporter) = ()
    }
    assert(b.expectedTestCount(Set(), Set()) === 1)

    val c = new Suite {
      @FastAsLight
      def testThis() = ()
      def testThat(reporter: Reporter) = ()
    }
    assert(c.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(c.expectedTestCount(Set(), Set("org.scalatest.FastAsLight")) === 1)

    val d = new Suite {
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

    val e = new Suite {
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

    val a = new Suite {
      def testNames(reporter: Reporter): Unit = ()
    }
    assert(a.expectedTestCount(Set(), Set()) === 1)
    val tnResult: Set[String] = a.testNames
    val gResult: Map[String, Set[String]] = a.groups
    assert(tnResult.size === 1)
    assert(gResult.keySet.size === 0)
  }

  def testThatTestMethodsWithNoGroupsDontShowUpInGroupsMap() {
    
    val a = new Suite {
      def testNotInAGroup() = ()
    }
    assert(a.groups.keySet.size === 0)
  }

  def testThatTestMethodsThatReturnNonUnitAreDiscovered() {
    val a = new Suite {
      def testThis(): Int = 1
      def testThat(reporter: Reporter): String = "hi"
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)
    assert(a.testNames.size === 2)
    assert(a.groups.keySet.size === 0)
  }

  def testThatOverloadedTestMethodsAreDiscovered() {
    val a = new Suite {
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

  def testStripDollars() {
    expect("MySuite") {
     Suite.stripDollars("line8$object$$iw$$iw$$iw$$iw$$iw$MySuite") 
    }
    expect("MySuite") {
     Suite.stripDollars("MySuite") 
    }
    expect("nested.MySuite") {
     Suite.stripDollars("nested.MySuite") 
    }
    expect("$$$") {
     Suite.stripDollars("$$$") 
    }
    expect("DollarAtEnd") {
     Suite.stripDollars("DollarAtEnd$") 
    }
    expect("DollarAtEnd") {
     Suite.stripDollars("line8$object$$iw$$iw$$iw$$iw$$iw$DollarAtEnd$")
    }
  }
}


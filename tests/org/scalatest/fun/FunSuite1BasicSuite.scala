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

class FunSuite1BasicSuite extends Suite {

  def testTestNames() {

    val a = new FunSuite1[Int] {
      test("test this") {}
      testWithReporter("test that") { reporter => () }
      def withFixture(f: Int => Unit) { f }
    }

    expect(TreeSet("test this", "test that")) {
      a.testNames
    }

    val b = new FunSuite1[Int] {
      def withFixture(f: Int => Unit) { f }
    }

    expect(TreeSet[String]()) {
      b.testNames
    }

    val c = new FunSuite1[Int] {
      test("test this") {}
      testWithReporter("test that") { reporter => () }
      def withFixture(f: Int => Unit) { f }
    }

    expect(TreeSet("test this", "test that")) {
      c.testNames
    }

    // Test duplicate names
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        test("test this") {}
        testWithReporter("test this") { reporter => () }
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithReporter("test this") { reporter => () }
        test("test this") {}
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithReporter("test this") { reporter => () }
        ignore("test this") {}
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        ignoreWithReporter("test this") { reporter => () }
        ignore("test this") {}
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        ignore("test this") {}
        ignoreWithReporter("test this") { reporter => () }
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        test("test this") {}
        ignoreWithReporter("test this") { reporter => () }
        def withFixture(f: Int => Unit) { f }
      }
    }

    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        test("test this") {}
        testWithReporter("test this") { reporter => () }
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithReporter("test this") { reporter => () }
        test("test this") {}
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithReporter("test this") { reporter => () }
        ignore("test this") {}
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        test("test this") {}
        ignoreWithReporter("test this") { reporter => () }
        def withFixture(f: Int => Unit) { f }
      }
    }

    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithReporter("test this") { reporter => () }
        test("test this") {}
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        test("test this") {}
        testWithReporter("test this") { reporter => () }
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        test("test this") {}
        testWithReporter("test this") { reporter => () }
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithReporter("test this") { reporter => () }
        test("test this") {}
        def withFixture(f: Int => Unit) { f }
      }
    }
  }

  def testTestGroups() {
    
    val a = new FunSuite1[Int] {
      ignore("test this") {}
      testWithReporter("test that") { reporter => () }
      def withFixture(f: Int => Unit) { f }
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"))) {
      a.groups
    }

    val b = new FunSuite1[Int] {
      test("test this") {}
      ignoreWithReporter("test that") { reporter => () }
      def withFixture(f: Int => Unit) { f }
    }
    expect(Map("test that" -> Set("org.scalatest.Ignore"))) {
      b.groups
    }

    val c = new FunSuite1[Int] {
      ignore("test this") {}
      ignoreWithReporter("test that") { reporter => () }
      def withFixture(f: Int => Unit) { f }
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"), "test that" -> Set("org.scalatest.Ignore"))) {
      c.groups
    }

    val d = new FunSuite1[Int] {
      test("test this", new SlowAsMolasses) {}
      ignoreWithReporter("test that", new SlowAsMolasses) { reporter => () }
      def withFixture(f: Int => Unit) { f }
    }
    expect(Map("test this" -> Set("org.scalatest.fun.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.fun.SlowAsMolasses"))) {
      d.groups
    }

    val e = new FunSuite1[Int] {
      def withFixture(f: Int => Unit) { f }
    }
    expect(Map()) {
      e.groups
    }

    val f = new FunSuite1[Int] {
      test("test this", new SlowAsMolasses, new WeakAsAKitten) {}
      testWithReporter("test that", new SlowAsMolasses) { reporter => () }
      def withFixture(f: Int => Unit) { f }
    }
    expect(Map("test this" -> Set("org.scalatest.fun.SlowAsMolasses", "org.scalatest.fun.WeakAsAKitten"), "test that" -> Set("org.scalatest.fun.SlowAsMolasses"))) {
      f.groups
    }
  }

  def testExecuteOneTest() {
    
    class MySuite extends FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this") { theTestThisCalled = true }
      test("test that") { theTestThatCalled = true }
      def withFixture(f: Int => Unit) { f }
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

    val a = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this") { theTestThisCalled = true }
      testWithReporter("test that") { reporter => theTestThatCalled = true }
      def withFixture(f: Int => Unit) { f }
    }

    val repA = new MyReporter
    a.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repA.testIgnoredCalled)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      testWithReporter("test that") { reporter => theTestThatCalled = true }
      def withFixture(f: Int => Unit) { f }
    }

    val repB = new MyReporter
    b.execute(None, repB, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repB.testIgnoredCalled)
    assert(repB.lastReport.name endsWith "test this")
    assert(!b.theTestThisCalled)
    assert(b.theTestThatCalled)

    val c = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this") { theTestThisCalled = true }
      ignoreWithReporter("test that") { reporter => theTestThatCalled = true }
      def withFixture(f: Int => Unit) { f }
    }

    val repC = new MyReporter
    c.execute(None, repC, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repC.testIgnoredCalled)
    assert(repC.lastReport.name endsWith "test that", repC.lastReport.name)
    assert(c.theTestThisCalled)
    assert(!c.theTestThatCalled)

    // The order I want is order of appearance in the file.
    // Will try and implement that tomorrow. Subtypes will be able to change the order.
    val d = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      ignoreWithReporter("test that") { reporter => theTestThatCalled = true }
      def withFixture(f: Int => Unit) { f }
    }

    val repD = new MyReporter
    d.execute(None, repD, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repD.testIgnoredCalled)
    assert(repD.lastReport.name endsWith "test that") // last because should be in order of appearance
    assert(!d.theTestThisCalled)
    assert(!d.theTestThatCalled)

    // If I provide a specific testName to execute, then it should ignore an Ignore on that test
    // method and actually invoke it.
    val e = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this") { theTestThisCalled = true }
      testWithReporter("test that") { reporter => theTestThatCalled = true }
      def withFixture(f: Int => Unit) { f }
    }

    val repE = new MyReporter
    e.execute(Some("test this"), repE, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repE.testIgnoredCalled)
    assert(e.theTestThisCalled)
  }

  def testExcludes() {

    val a = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", new SlowAsMolasses) { theTestThisCalled = true }
      testWithReporter("test that") { reporter => theTestThatCalled = true }
      def withFixture(f: Int => Unit) { f }
    }
    val repA = new MyReporter
    a.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repA.testIgnoredCalled)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", new SlowAsMolasses) { theTestThisCalled = true }
      testWithReporter("test that") { reporter => theTestThatCalled = true }
      def withFixture(f: Int => Unit) { f }
    }
    val repB = new MyReporter
    b.execute(None, repB, new Stopper {}, Set("org.scalatest.fun.SlowAsMolasses"), Set(), Map(), None)
    assert(!repB.testIgnoredCalled)
    assert(b.theTestThisCalled)
    assert(!b.theTestThatCalled)

    val c = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      test("test this", new SlowAsMolasses) { theTestThisCalled = true }
      testWithReporter("test that", new SlowAsMolasses) { reporter => theTestThatCalled = true }
      def withFixture(f: Int => Unit) { f }
    }
    val repC = new MyReporter
    c.execute(None, repB, new Stopper {}, Set("org.scalatest.fun.SlowAsMolasses"), Set(), Map(), None)
    assert(!repC.testIgnoredCalled)
    assert(c.theTestThisCalled)
    assert(c.theTestThatCalled)

    val d = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignore("test this", new SlowAsMolasses) { theTestThisCalled = true }
      testWithReporter("test that", new SlowAsMolasses) { reporter => theTestThatCalled = true }
      def withFixture(f: Int => Unit) { f }
    }
    val repD = new MyReporter
    d.execute(None, repD, new Stopper {}, Set("org.scalatest.fun.SlowAsMolasses"), Set("org.scalatest.Ignore"), Map(), None)
    assert(repD.testIgnoredCalled)
    assert(!d.theTestThisCalled)
    assert(d.theTestThatCalled)

    val e = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", new SlowAsMolasses, new FastAsLight) { theTestThisCalled = true }
      testWithReporter("test that", new SlowAsMolasses) { reporter => theTestThatCalled = true }
      testWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) { f }
    }
    val repE = new MyReporter
    e.execute(None, repE, new Stopper {}, Set("org.scalatest.fun.SlowAsMolasses"), Set("org.scalatest.fun.FastAsLight"),
              Map(), None)
    assert(!repE.testIgnoredCalled)
    assert(!e.theTestThisCalled)
    assert(e.theTestThatCalled)
    assert(!e.theTestTheOtherCalled)

    val f = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignore("test this", new SlowAsMolasses, new FastAsLight) { theTestThisCalled = true }
      testWithReporter("test that", new SlowAsMolasses) { reporter => theTestThatCalled = true }
      testWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) { f }
    }
    val repF = new MyReporter
    f.execute(None, repF, new Stopper {}, Set("org.scalatest.fun.SlowAsMolasses"), Set("org.scalatest.fun.FastAsLight"),
              Map(), None)
    assert(!repF.testIgnoredCalled)
    assert(!f.theTestThisCalled)
    assert(f.theTestThatCalled)
    assert(!f.theTestTheOtherCalled)

    val g = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", new SlowAsMolasses, new FastAsLight) { theTestThisCalled = true }
      testWithReporter("test that", new SlowAsMolasses) { reporter => theTestThatCalled = true }
      ignoreWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) { f }
    }
    val repG = new MyReporter
    g.execute(None, repG, new Stopper {}, Set("org.scalatest.fun.SlowAsMolasses"), Set("org.scalatest.fun.FastAsLight"),
              Map(), None)
    assert(!repG.testIgnoredCalled)
    assert(!g.theTestThisCalled)
    assert(g.theTestThatCalled)
    assert(!g.theTestTheOtherCalled)

    val h = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", new SlowAsMolasses, new FastAsLight) { theTestThisCalled = true }
      testWithReporter("test that", new SlowAsMolasses) { reporter => theTestThatCalled = true }
      testWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) { f }
    }
    val repH = new MyReporter
    h.execute(None, repH, new Stopper {}, Set(), Set("org.scalatest.fun.FastAsLight"), Map(), None)
    assert(!repH.testIgnoredCalled)
    assert(!h.theTestThisCalled)
    assert(h.theTestThatCalled)
    assert(h.theTestTheOtherCalled)

    val i = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      test("test this", new SlowAsMolasses, new FastAsLight) { theTestThisCalled = true }
      testWithReporter("test that", new SlowAsMolasses) { reporter => theTestThatCalled = true }
      testWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) { f }
    }
    val repI = new MyReporter
    i.execute(None, repI, new Stopper {}, Set(), Set("org.scalatest.fun.SlowAsMolasses"), Map(), None)
    assert(!repI.testIgnoredCalled)
    assert(!i.theTestThisCalled)
    assert(!i.theTestThatCalled)
    assert(i.theTestTheOtherCalled)

    val j = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignore("test this", new SlowAsMolasses, new FastAsLight) { theTestThisCalled = true }
      ignoreWithReporter("test that", new SlowAsMolasses) { reporter => theTestThatCalled = true }
      testWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) { f }
    }
    val repJ = new MyReporter
    j.execute(None, repJ, new Stopper {}, Set(), Set("org.scalatest.fun.SlowAsMolasses"), Map(), None)
    assert(!repI.testIgnoredCalled)
    assert(!j.theTestThisCalled)
    assert(!j.theTestThatCalled)
    assert(j.theTestTheOtherCalled)

    val k = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignore("test this", new SlowAsMolasses, new FastAsLight) { theTestThisCalled = true }
      ignoreWithReporter("test that", new SlowAsMolasses) { reporter => theTestThatCalled = true }
      ignoreWithReporter("test the other") { reporter => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) { f }
    }
    val repK = new MyReporter
    k.execute(None, repK, new Stopper {}, Set(), Set("org.scalatest.fun.SlowAsMolasses", "org.scalatest.Ignore"), Map(), None)
    assert(repK.testIgnoredCalled)
    assert(!k.theTestThisCalled)
    assert(!k.theTestThatCalled)
    assert(!k.theTestTheOtherCalled)
  }

  def testTestCount() {

    val a = new FunSuite1[Int] {
      test("test this") {}
      testWithReporter("test that") { reporter => () }
      def withFixture(f: Int => Unit) { f }
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)

    val b = new FunSuite1[Int] {
      ignore("test this") {}
      testWithReporter("test that") { reporter => () }
      def withFixture(f: Int => Unit) { f }
    }
    assert(b.expectedTestCount(Set(), Set()) === 1)

    val c = new FunSuite1[Int] {
      test("test this", new FastAsLight) {}
      testWithReporter("test that") { reporter => () }
      def withFixture(f: Int => Unit) { f }
    }
    assert(c.expectedTestCount(Set("org.scalatest.fun.FastAsLight"), Set()) === 1)
    assert(c.expectedTestCount(Set(), Set("org.scalatest.fun.FastAsLight")) === 1)

    val d = new FunSuite1[Int] {
      test("test this", new FastAsLight, new SlowAsMolasses) {}
      testWithReporter("test that", new SlowAsMolasses) { reporter => () }
      testWithReporter("test the other thing") { reporter => () }
      def withFixture(f: Int => Unit) { f }
    }
    assert(d.expectedTestCount(Set("org.scalatest.fun.FastAsLight"), Set()) === 1)
    assert(d.expectedTestCount(Set("org.scalatest.fun.SlowAsMolasses"), Set("org.scalatest.fun.FastAsLight")) === 1)
    assert(d.expectedTestCount(Set(), Set("org.scalatest.fun.SlowAsMolasses")) === 1)
    assert(d.expectedTestCount(Set(), Set()) === 3)

    val e = new FunSuite1[Int] {
      test("test this", new FastAsLight, new SlowAsMolasses) {}
      testWithReporter("test that", new SlowAsMolasses) { reporter => () }
      ignoreWithReporter("test the other thing") { reporter => () }
      def withFixture(f: Int => Unit) { f }
    }
    assert(e.expectedTestCount(Set("org.scalatest.fun.FastAsLight"), Set()) === 1)
    assert(e.expectedTestCount(Set("org.scalatest.fun.SlowAsMolasses"), Set("org.scalatest.fun.FastAsLight")) === 1)
    assert(e.expectedTestCount(Set(), Set("org.scalatest.fun.SlowAsMolasses")) === 0)
    assert(e.expectedTestCount(Set(), Set()) === 2)

    val f = new SuperSuite(List(a, b, c, d, e))
    assert(f.expectedTestCount(Set(), Set()) === 10)
  }

  def testThatTestMethodsWithNoGroupsDontShowUpInGroupsMap() {
    
    val a = new FunSuite1[Int] {
      test("test not in a group") {}
      def withFixture(f: Int => Unit) { f }
    }
    assert(a.groups.keySet.size === 0)
  }

  def testThatTestFunctionsThatResultInNonUnitAreRegistered() {
    val a = new FunSuite1[Int] {
      test("test this") { 1 }
      testWithReporter("test that") { reporter => "hi" }
      def withFixture(f: Int => Unit) { f }
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)
    assert(a.testNames.size === 2)
    assert(a.groups.keySet.size === 0)
  }

  def testThatTestNameCantBeReused() {
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        test("test this") {}
        test("test this") {}
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        ignore("test this") {}
        test("test this") {}
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        test("test this") {}
        ignore("test this") {}
        def withFixture(f: Int => Unit) { f }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        test("test this") {}
        testWithReporter("test this") { reporter => () }
        def withFixture(f: Int => Unit) { f }
      }
    }
  }
}


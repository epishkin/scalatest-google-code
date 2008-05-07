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

class FunSuite1Suite extends Suite {

  def testAllTestNames() {
    val a = new FunSuite1[Int] {
      test("test this") {}
      testWithReporter("test that") { reporter => () }
      testWithFixture("test with fixture") { fixture => () }
      testWithFixtureAndReporter("test with fixture and reporter") { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }

    expect(List("test this", "test that", "test with fixture", "test with fixture and reporter")) {
      a.testNames.elements.toList
    }
  }

  def testThatFixtureActuallyGetsCalled() {

    val a = new FunSuite1[Int] {
      var withFixtureMethodCalledCount = 0
      test("test this") {}
      testWithReporter("test that") { reporter => () }
      testWithFixture("test with fixture") { fixture => () }
      testWithFixtureAndReporter("test with fixture and reporter") { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        withFixtureMethodCalledCount += 1
        f(8)
      }
    }

    a.execute()

    expect(2) {
      a.withFixtureMethodCalledCount
    }
  }

  def testTestNames() {

    val a = new FunSuite1[Int] {
      testWithFixture("test this") { fixture => () }
      testWithFixtureAndReporter("test that") { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) { f }
    }

    expect(List("test this", "test that")) {
      a.testNames.elements.toList
    }

    val b = new FunSuite1[Int] {
      def withFixture(f: Int => Unit) { f }
    }

    expect(List[String]()) {
      b.testNames.elements.toList
    }

    val c = new FunSuite1[Int] {
      testWithFixture("test this") {{ fixture => () }}
      testWithFixtureAndReporter("test that") { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }

    expect(List("test this", "test that")) {
      c.testNames.elements.toList
    }

    // Test duplicate names
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixture("test this") { fixture => () }
        testWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        testWithFixture("test this") { fixture => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        ignoreWithFixture("test this") { fixture => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        ignoreWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        ignoreWithFixture("test this") { fixture => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        ignoreWithFixture("test this") { fixture => () }
        ignoreWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixture("test this") { fixture => () }
        ignoreWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }

    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixture("test this") { fixture => () }
        testWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        testWithFixture("test this") { fixture => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        ignoreWithFixture("test this") { fixture => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixture("test this") { fixture => () }
        ignoreWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }

    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        testWithFixture("test this") { fixture => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixture("test this") { fixture => () }
        testWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixture("test this") { fixture => () }
        testWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        testWithFixture("test this") { fixture => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
  }

  def testTestGroups() {
    
    val a = new FunSuite1[Int] {
      ignoreWithFixture("test this") { fixture => () }
      testWithFixtureAndReporter("test that") { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"))) {
      a.groups
    }

    val b = new FunSuite1[Int] {
      testWithFixture("test this") { fixture => () }
      ignoreWithFixtureAndReporter("test that") { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    expect(Map("test that" -> Set("org.scalatest.Ignore"))) {
      b.groups
    }

    val c = new FunSuite1[Int] {
      ignoreWithFixture("test this") { fixture => () }
      ignoreWithFixtureAndReporter("test that") { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    expect(Map("test this" -> Set("org.scalatest.Ignore"), "test that" -> Set("org.scalatest.Ignore"))) {
      c.groups
    }

    val d = new FunSuite1[Int] {
      testWithFixture("test this", SlowAsMolasses) { fixture => () }
      ignoreWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
      d.groups
    }

    val e = new FunSuite1[Int] {
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    expect(Map()) {
      e.groups
    }

    val f = new FunSuite1[Int] {
      testWithFixture("test this", SlowAsMolasses, WeakAsAKitten) { fixture => () }
      testWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
      f.groups
    }
  }

  def testExecuteOneTest() {
    
    class MySuite extends FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      testWithFixture("test this") { fixture => theTestThisCalled = true }
      testWithFixture("test that") { fixture => theTestThatCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
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
      testWithFixture("test this") { fixture => theTestThisCalled = true }
      testWithFixtureAndReporter("test that") { (fixture, reporter) => theTestThatCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }

    val repA = new MyReporter
    a.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repA.testIgnoredCalled)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignoreWithFixture("test this") { fixture => theTestThisCalled = true }
      testWithFixtureAndReporter("test that") { (fixture, reporter) => theTestThatCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
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
      testWithFixture("test this") { fixture => theTestThisCalled = true }
      ignoreWithFixtureAndReporter("test that") { (fixture, reporter) => theTestThatCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
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
      ignoreWithFixture("test this") { fixture => theTestThisCalled = true }
      ignoreWithFixtureAndReporter("test that") { (fixture, reporter) => theTestThatCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
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
      ignoreWithFixture("test this") { fixture => theTestThisCalled = true }
      testWithFixtureAndReporter("test that") { (fixture, reporter) => theTestThatCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
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
      testWithFixture("test this", SlowAsMolasses) { fixture => theTestThisCalled = true }
      testWithFixtureAndReporter("test that") { (fixture, reporter) => theTestThatCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    val repA = new MyReporter
    a.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(!repA.testIgnoredCalled)
    assert(a.theTestThisCalled)
    assert(a.theTestThatCalled)

    val b = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      testWithFixture("test this", SlowAsMolasses) { fixture => theTestThisCalled = true }
      testWithFixtureAndReporter("test that") { (fixture, reporter) => theTestThatCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    val repB = new MyReporter
    b.execute(None, repB, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set(), Map(), None)
    assert(!repB.testIgnoredCalled)
    assert(b.theTestThisCalled)
    assert(!b.theTestThatCalled)

    val c = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      testWithFixture("test this", SlowAsMolasses) { fixture => theTestThisCalled = true }
      testWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => theTestThatCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    val repC = new MyReporter
    c.execute(None, repB, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set(), Map(), None)
    assert(!repC.testIgnoredCalled)
    assert(c.theTestThisCalled)
    assert(c.theTestThatCalled)

    val d = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      ignoreWithFixture("test this", SlowAsMolasses) { fixture => theTestThisCalled = true }
      testWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => theTestThatCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    val repD = new MyReporter
    d.execute(None, repD, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.Ignore"), Map(), None)
    assert(repD.testIgnoredCalled)
    assert(!d.theTestThisCalled)
    assert(d.theTestThatCalled)

    val e = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      testWithFixture("test this", SlowAsMolasses, FastAsLight) { fixture => theTestThisCalled = true }
      testWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => theTestThatCalled = true }
      testWithFixtureAndReporter("test the other") { (fixture, reporter) => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    val repE = new MyReporter
    e.execute(None, repE, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight"),
              Map(), None)
    assert(!repE.testIgnoredCalled)
    assert(!e.theTestThisCalled)
    assert(e.theTestThatCalled)
    assert(!e.theTestTheOtherCalled)

    val f = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignoreWithFixture("test this", SlowAsMolasses, FastAsLight) { fixture => theTestThisCalled = true }
      testWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => theTestThatCalled = true }
      testWithFixtureAndReporter("test the other") { (fixture, reporter) => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    val repF = new MyReporter
    f.execute(None, repF, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight"),
              Map(), None)
    assert(!repF.testIgnoredCalled)
    assert(!f.theTestThisCalled)
    assert(f.theTestThatCalled)
    assert(!f.theTestTheOtherCalled)

    val g = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      testWithFixture("test this", SlowAsMolasses, FastAsLight) { fixture => theTestThisCalled = true }
      testWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => theTestThatCalled = true }
      ignoreWithFixtureAndReporter("test the other") { (fixture, reporter) => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    val repG = new MyReporter
    g.execute(None, repG, new Stopper {}, Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight"),
              Map(), None)
    assert(!repG.testIgnoredCalled)
    assert(!g.theTestThisCalled)
    assert(g.theTestThatCalled)
    assert(!g.theTestTheOtherCalled)

    val h = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      testWithFixture("test this", SlowAsMolasses, FastAsLight) { fixture => theTestThisCalled = true }
      testWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => theTestThatCalled = true }
      testWithFixtureAndReporter("test the other") { (fixture, reporter) => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    val repH = new MyReporter
    h.execute(None, repH, new Stopper {}, Set(), Set("org.scalatest.FastAsLight"), Map(), None)
    assert(!repH.testIgnoredCalled)
    assert(!h.theTestThisCalled)
    assert(h.theTestThatCalled)
    assert(h.theTestTheOtherCalled)

    val i = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      testWithFixture("test this", SlowAsMolasses, FastAsLight) { fixture => theTestThisCalled = true }
      testWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => theTestThatCalled = true }
      testWithFixtureAndReporter("test the other") { (fixture, reporter) => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    val repI = new MyReporter
    i.execute(None, repI, new Stopper {}, Set(), Set("org.scalatest.SlowAsMolasses"), Map(), None)
    assert(!repI.testIgnoredCalled)
    assert(!i.theTestThisCalled)
    assert(!i.theTestThatCalled)
    assert(i.theTestTheOtherCalled)

    val j = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignoreWithFixture("test this", SlowAsMolasses, FastAsLight) { fixture => theTestThisCalled = true }
      ignoreWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => theTestThatCalled = true }
      testWithFixtureAndReporter("test the other") { (fixture, reporter) => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    val repJ = new MyReporter
    j.execute(None, repJ, new Stopper {}, Set(), Set("org.scalatest.SlowAsMolasses"), Map(), None)
    assert(!repI.testIgnoredCalled)
    assert(!j.theTestThisCalled)
    assert(!j.theTestThatCalled)
    assert(j.theTestTheOtherCalled)

    val k = new FunSuite1[Int] {
      var theTestThisCalled = false
      var theTestThatCalled = false
      var theTestTheOtherCalled = false
      ignoreWithFixture("test this", SlowAsMolasses, FastAsLight) { fixture => theTestThisCalled = true }
      ignoreWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => theTestThatCalled = true }
      ignoreWithFixtureAndReporter("test the other") { (fixture, reporter) => theTestTheOtherCalled = true }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    val repK = new MyReporter
    k.execute(None, repK, new Stopper {}, Set(), Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore"), Map(), None)
    assert(repK.testIgnoredCalled)
    assert(!k.theTestThisCalled)
    assert(!k.theTestThatCalled)
    assert(!k.theTestTheOtherCalled)
  }

  def testTestCount() {

    val a = new FunSuite1[Int] {
      testWithFixture("test this") { fixture => () }
      testWithFixtureAndReporter("test that") { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)

    val b = new FunSuite1[Int] {
      ignoreWithFixture("test this") { fixture => () }
      testWithFixtureAndReporter("test that") { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    assert(b.expectedTestCount(Set(), Set()) === 1)

    val c = new FunSuite1[Int] {
      testWithFixture("test this", FastAsLight) { fixture => () }
      testWithFixtureAndReporter("test that") { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    assert(c.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(c.expectedTestCount(Set(), Set("org.scalatest.FastAsLight")) === 1)

    val d = new FunSuite1[Int] {
      testWithFixture("test this", FastAsLight, SlowAsMolasses) { fixture => () }
      testWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => () }
      testWithFixtureAndReporter("test the other thing") { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    assert(d.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(d.expectedTestCount(Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight")) === 1)
    assert(d.expectedTestCount(Set(), Set("org.scalatest.SlowAsMolasses")) === 1)
    assert(d.expectedTestCount(Set(), Set()) === 3)

    val e = new FunSuite1[Int] {
      testWithFixture("test this", FastAsLight, SlowAsMolasses) { fixture => () }
      testWithFixtureAndReporter("test that", SlowAsMolasses) { (fixture, reporter) => () }
      ignoreWithFixtureAndReporter("test the other thing") { (fixture, reporter) => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    assert(e.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(e.expectedTestCount(Set("org.scalatest.SlowAsMolasses"), Set("org.scalatest.FastAsLight")) === 1)
    assert(e.expectedTestCount(Set(), Set("org.scalatest.SlowAsMolasses")) === 0)
    assert(e.expectedTestCount(Set(), Set()) === 2)

    val f = new SuperSuite(List(a, b, c, d, e))
    assert(f.expectedTestCount(Set(), Set()) === 10)
  }

  def testThatTestMethodsWithNoGroupsDontShowUpInGroupsMap() {
    
    val a = new FunSuite1[Int] {
      testWithFixture("test not in a group") { fixture => () }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    assert(a.groups.keySet.size === 0)
  }

  def testThatTestFunctionsThatResultInNonUnitAreRegistered() {
    val a = new FunSuite1[Int] {
      testWithFixture("test this") { fixture => 1 }
      testWithFixtureAndReporter("test that") { (fixture, reporter) => "hi" }
      def withFixture(f: Int => Unit) {
        f(8)
      }
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)
    assert(a.testNames.size === 2)
    assert(a.groups.keySet.size === 0)
  }

  def testThatTestNameCantBeReused() {
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixture("test this") { fixture => () }
        testWithFixture("test this") { fixture => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        ignoreWithFixture("test this") { fixture => () }
        testWithFixture("test this") { fixture => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixture("test this") { fixture => () }
        ignoreWithFixture("test this") { fixture => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
    intercept(classOf[IllegalArgumentException]) {
      new FunSuite1[Int] {
        testWithFixture("test this") { fixture => () }
        testWithFixtureAndReporter("test this") { (fixture, reporter) => () }
        def withFixture(f: Int => Unit) {
          f(8)
        }
      }
    }
  }
}


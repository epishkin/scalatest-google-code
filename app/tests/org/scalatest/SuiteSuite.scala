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
import org.scalatest.events.Event

class SuiteFriend(suite: Suite) {

  def simpleNameForTest(testName: String) = {
    val m = Class.forName("org.scalatest.Suite$class").getDeclaredMethod("simpleNameForTest", Array(classOf[org.scalatest.Suite], classOf[String]): _*)
    m.setAccessible(true)
    m.invoke(suite, Array[Object](suite, testName): _*).asInstanceOf[String]
  }
}

class SuiteSuite extends Suite with PrivateMethodTester {

  def testSimpleNameForTest() {
    val s = new SuiteFriend(new Suite {})
    assert(s.simpleNameForTest("testThis") === "testThis")
    assert(s.simpleNameForTest("testThis(Informer)") === "testThis")
    assert(s.simpleNameForTest("test(Informer)") === "test")
    assert(s.simpleNameForTest("test") === "test")
  }

  def testTestNames() {

    val a = new Suite {
      def testThis() = ()
      def testThat(info: Informer) = ()
    }
    assert(a.testNames === TreeSet("testThat(Informer)", "testThis"))

    val b = new Suite {}
    assert(b.testNames === TreeSet[String]())
  }

  def testTestGroups() {
    
    val a = new Suite {
      @Ignore
      def testThis() = ()
      def testThat(info: Informer) = ()
    }

    assert(a.groups === Map("testThis" -> Set("org.scalatest.Ignore")))

    val b = new Suite {
      def testThis() = ()
      @Ignore
      def testThat(info: Informer) = ()
    }

    assert(b.groups === Map("testThat(Informer)" -> Set("org.scalatest.Ignore")))

    val c = new Suite {
      @Ignore
      def testThis() = ()
      @Ignore
      def testThat(info: Informer) = ()
    }

    assert(c.groups === Map("testThis" -> Set("org.scalatest.Ignore"), "testThat(Informer)" -> Set("org.scalatest.Ignore")))

    val d = new Suite {
      @SlowAsMolasses
      def testThis() = ()
      @SlowAsMolasses
      @Ignore
      def testThat(info: Informer) = ()
    }

    assert(d.groups === Map("testThis" -> Set("org.scalatest.SlowAsMolasses"), "testThat(Informer)" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses")))

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
    override def apply(event: Event) {
      super.apply(event)
    }
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
      def testThat(info: Informer) { theTestThatCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
    }

    val repC = new MyReporter
    c.execute(None, repC, new Stopper {}, Set(), Set("org.scalatest.Ignore"), Map(), None)
    assert(repC.testIgnoredCalled)
    assert(repC.lastReport.name endsWith "testThat(Informer)", repC.lastReport.name)
    assert(c.theTestThisCalled)
    assert(!c.theTestThatCalled)

    val d = new Suite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      @Ignore
      def testThis() { theTestThisCalled = true }
      @Ignore
      def testThat(info: Informer) { theTestThatCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
      def testTheOther(info: Informer) { theTestTheOtherCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
      def testTheOther(info: Informer) { theTestTheOtherCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
      @Ignore
      def testTheOther(info: Informer) { theTestTheOtherCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
      def testTheOther(info: Informer) { theTestTheOtherCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
      def testTheOther(info: Informer) { theTestTheOtherCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
      def testTheOther(info: Informer) { theTestTheOtherCalled = true }
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
      def testThat(info: Informer) { theTestThatCalled = true }
      @Ignore
      def testTheOther(info: Informer) { theTestTheOtherCalled = true }
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
      def testThat(info: Informer) = ()
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)

    val b = new Suite {
      @Ignore
      def testThis() = ()
      def testThat(info: Informer) = ()
    }
    assert(b.expectedTestCount(Set(), Set()) === 1)

    val c = new Suite {
      @FastAsLight
      def testThis() = ()
      def testThat(info: Informer) = ()
    }
    assert(c.expectedTestCount(Set("org.scalatest.FastAsLight"), Set()) === 1)
    assert(c.expectedTestCount(Set(), Set("org.scalatest.FastAsLight")) === 1)

    val d = new Suite {
      @FastAsLight
      @SlowAsMolasses
      def testThis() = ()
      @SlowAsMolasses
      def testThat(info: Informer) = ()
      def testTheOtherThing(info: Informer) = ()
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
      def testThat(info: Informer) = ()
      @Ignore
      def testTheOtherThing(info: Informer) = ()
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
      def testNames(info: Informer): Unit = ()
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
      def testThat(info: Informer): String = "hi"
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)
    assert(a.testNames.size === 2)
    assert(a.groups.keySet.size === 0)
  }

  def testThatOverloadedTestMethodsAreDiscovered() {
    val a = new Suite {
      def testThis() = ()
      def testThis(info: Informer) = ()
    }
    assert(a.expectedTestCount(Set(), Set()) === 2)
    assert(a.testNames.size === 2)
    assert(a.groups.keySet.size === 0)
  }

  def testThatInterceptCatchesSubtypes() {
    class MyException extends RuntimeException
    class MyExceptionSubClass extends MyException
    intercept[MyException] {
      throw new MyException
      new AnyRef // This is needed because right now Nothing doesn't overload as an Any
    }
    intercept[MyException] {
      throw new MyExceptionSubClass
      new AnyRef // This is needed because right now Nothing doesn't overload as an Any
    }
    // Try with a trait
    trait MyTrait {
      def someRandomMethod() {}
    }
    class AnotherException extends RuntimeException with MyTrait
    val caught = intercept[MyTrait] {
      throw new AnotherException
      new AnyRef // This is needed because right now Nothing doesn't overload as an Any
    }
    // Make sure the result type is the type passed in, so I can 
    // not cast and still invoke any method on it I want
    caught.someRandomMethod()
  }

  def testThatInterceptReturnsTheCaughtException() {
    val e = new RuntimeException
    val result = intercept[RuntimeException] {
      throw e
      new AnyRef // This is needed because right now Nothing doesn't overload as an Any
    }
    assert(result eq e)
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
    expect("MySuite$1") {
     Suite.stripDollars("MySuite$1")
    }
  }
  
  def testDiffStrings() {
    expect(("[]", "[a]")) { Suite.diffStrings("", "a") }
    expect(("[a]", "[]")) { Suite.diffStrings("a", "") }
    expect(("a[]", "a[b]")) { Suite.diffStrings("a", "ab") }
    expect(("a[b]", "a[]")) { Suite.diffStrings("ab", "a") }
    expect(("[a]", "[b]")) { Suite.diffStrings("a", "b") }
    expect(("[a]big", "[]big")) { Suite.diffStrings("abig", "big") }
    expect(("[]big", "[a]big")) { Suite.diffStrings("big", "abig") }
    expect(("big[a]", "big[]")) { Suite.diffStrings("biga", "big") }
    expect(("big[]", "big[a]")) { Suite.diffStrings("big", "biga") }
    expect(("small[a]big", "small[]big")) { Suite.diffStrings("smallabig", "smallbig") }
    expect(("0123456789[]0123456789", "0123456789[a]0123456789")) {
      Suite.diffStrings("01234567890123456789", "0123456789a0123456789")
    }
    expect(("...01234567890123456789[]0123456789", "...01234567890123456789[a]0123456789")) {
      Suite.diffStrings("X012345678901234567890123456789", "X01234567890123456789a0123456789")
    }
    expect(("01234567890123456789[]01234567890123456789...", "01234567890123456789[a]01234567890123456789...")) {
        Suite.diffStrings("0123456789012345678901234567890123456789X", "01234567890123456789a01234567890123456789X")
    }
    expect(("...01234567890123456789[]01234567890123456789...", "...01234567890123456789[a]01234567890123456789...")) {
        Suite.diffStrings("XXXX0123456789012345678901234567890123456789XX", "XXXX01234567890123456789a01234567890123456789XX")
    }
    expect(("...01234567890123456789[]01234567890123456789...", "...01234567890123456789[a]01234567890123456789...")) {
        Suite.diffStrings("X0123456789012345678901234567890123456789X", "X01234567890123456789a01234567890123456789X")
    }
  }

  def testDecorateToStringValue() {

    val decorateToStringValue = PrivateMethod[String]('decorateToStringValue)

    expect("1") {
      FailureMessages invokePrivate decorateToStringValue(1.toByte)
    }
    expect("1") {
      FailureMessages invokePrivate decorateToStringValue(1.toShort)
    }
    expect("1") {
      FailureMessages invokePrivate decorateToStringValue(1)
    }
    expect("10") {
      FailureMessages invokePrivate decorateToStringValue(10L)
    }
    expect("1.0") {
      FailureMessages invokePrivate decorateToStringValue(1.0f)
    }
    expect("1.0") {
      FailureMessages invokePrivate decorateToStringValue(1.0)
    }
    expect("false") {
      FailureMessages invokePrivate decorateToStringValue(false)
    }
    expect("true") {
      FailureMessages invokePrivate decorateToStringValue(true)
    }
    expect("<(), the Unit value>") {
      FailureMessages invokePrivate decorateToStringValue(())
    }
    expect("\"Howdy!\"") {
      FailureMessages invokePrivate decorateToStringValue("Howdy!")
    }
    expect("'c'") {
      FailureMessages invokePrivate decorateToStringValue('c')
    }
    expect("Hey!") {
      FailureMessages invokePrivate decorateToStringValue(new AnyRef { override def toString = "Hey!"})
    }
  }
}


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
import org.scalatest.events._

class SuiteSuite extends Suite with PrivateMethodTester with SharedHelpers {

  def testExecuteOneTest() {
    
    class MySuite extends Suite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      def testThis() { theTestThisCalled = true }
      def testThat() { theTestThatCalled = true }
    }

    val a = new MySuite 
    a.run("testThis")
    assert(a.theTestThisCalled)
    assert(!a.theTestThatCalled)

    val b = new MySuite
    b.run()
    assert(b.theTestThisCalled)
    assert(b.theTestThatCalled)
  }

  class MyReporter extends Reporter {
    var testIgnoredReceived = false
    var lastEvent: TestIgnored = null
    def apply(event: Event) {
      event match {
        case event: TestIgnored =>
          testIgnoredReceived = true
          lastEvent = event
        case _ =>
      }
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
    a.run(None, repA, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(!repA.testIgnoredReceived)
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
    b.run(None, repB, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(repB.testIgnoredReceived)
    assert(repB.lastEvent.testName endsWith "testThis")
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
    c.run(None, repC, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(repC.testIgnoredReceived)
    assert(repC.lastEvent.testName endsWith "testThat(Informer)", repC.lastEvent.testName)
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
    d.run(None, repD, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(repD.testIgnoredReceived)
    assert(repD.lastEvent.testName endsWith "testThis") // last because run alphabetically
    assert(!d.theTestThisCalled)
    assert(!d.theTestThatCalled)

    // If I provide a specific testName to run, then it should ignore an Ignore on that test
    // method and actually invoke it.

    val e = new Suite {
      var theTestThisCalled = false
      var theTestThatCalled = false
      @Ignore
      def testThis() { theTestThisCalled = true }
      def testThat(info: Informer) { theTestThatCalled = true }
    }

    val repE = new MyReporter
    e.run(Some("testThis"), repE, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(!repE.testIgnoredReceived)
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
    a.run(None, repA, new Stopper {}, Filter(), Map(), None, new Tracker)
    assert(!repA.testIgnoredReceived)
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
    b.run(None, repB, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), Map(), None, new Tracker)
    assert(!repB.testIgnoredReceived)
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
    c.run(None, repB, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set()), Map(), None, new Tracker)
    assert(!repC.testIgnoredReceived)
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
    d.run(None, repD, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.Ignore")), Map(), None, new Tracker)
    assert(repD.testIgnoredReceived)
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
    e.run(None, repE, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
              Map(), None, new Tracker)
    assert(!repE.testIgnoredReceived)
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
    f.run(None, repF, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
              Map(), None, new Tracker)
    assert(!repF.testIgnoredReceived)
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
    g.run(None, repG, new Stopper {}, Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight")),
              Map(), None, new Tracker)
    assert(!repG.testIgnoredReceived)
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
    h.run(None, repH, new Stopper {}, Filter(None, Set("org.scalatest.FastAsLight")), Map(), None, new Tracker)
    assert(!repH.testIgnoredReceived)
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
    i.run(None, repI, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses")), Map(), None, new Tracker)
    assert(!repI.testIgnoredReceived)
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
    j.run(None, repJ, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses")), Map(), None, new Tracker)
    assert(!repI.testIgnoredReceived)
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
    k.run(None, repK, new Stopper {}, Filter(None, Set("org.scalatest.SlowAsMolasses", "org.scalatest.Ignore")), Map(), None, new Tracker)
    assert(repK.testIgnoredReceived)
    assert(!k.theTestThisCalled)
    assert(!k.theTestThatCalled)
    assert(!k.theTestTheOtherCalled)
  }

  def testTestCount() {

    val a = new Suite {
      def testThis() = ()
      def testThat(info: Informer) = ()
    }
    assert(a.expectedTestCount(Filter()) === 2)

    val b = new Suite {
      @Ignore
      def testThis() = ()
      def testThat(info: Informer) = ()
    }
    assert(b.expectedTestCount(Filter()) === 1)

    val c = new Suite {
      @FastAsLight
      def testThis() = ()
      def testThat(info: Informer) = ()
    }
    assert(c.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
    assert(c.expectedTestCount(Filter(None, Set("org.scalatest.FastAsLight"))) === 1)

    val d = new Suite {
      @FastAsLight
      @SlowAsMolasses
      def testThis() = ()
      @SlowAsMolasses
      def testThat(info: Informer) = ()
      def testTheOtherThing(info: Informer) = ()
    }
    assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
    assert(d.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
    assert(d.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 1)
    assert(d.expectedTestCount(Filter()) === 3)

    val e = new Suite {
      @FastAsLight
      @SlowAsMolasses
      def testThis() = ()
      @SlowAsMolasses
      def testThat(info: Informer) = ()
      @Ignore
      def testTheOtherThing(info: Informer) = ()
    }
    assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.FastAsLight")), Set())) === 1)
    assert(e.expectedTestCount(Filter(Some(Set("org.scalatest.SlowAsMolasses")), Set("org.scalatest.FastAsLight"))) === 1)
    assert(e.expectedTestCount(Filter(None, Set("org.scalatest.SlowAsMolasses"))) === 0)
    assert(e.expectedTestCount(Filter()) === 2)

    val f = new SuperSuite(List(a, b, c, d, e))
    assert(f.expectedTestCount(Filter()) === 10)
  }

  def testNamesAndGroupsMethodsDiscovered() {

    val a = new Suite {
      def testNames(info: Informer): Unit = ()
    }
    assert(a.expectedTestCount(Filter()) === 1)
    val tnResult: Set[String] = a.testNames
    val gResult: Map[String, Set[String]] = a.tags
    assert(tnResult.size === 1)
    assert(gResult.keySet.size === 0)
  }

  def testThatTestMethodsWithNoGroupsDontShowUpInGroupsMap() {
    
    val a = new Suite {
      def testNotInAGroup() = ()
    }
    assert(a.tags.keySet.size === 0)
  }

  def testThatTestMethodsThatReturnNonUnitAreDiscovered() {
    val a = new Suite {
      def testThis(): Int = 1
      def testThat(info: Informer): String = "hi"
    }
    assert(a.expectedTestCount(Filter()) === 2)
    assert(a.testNames.size === 2)
    assert(a.tags.keySet.size === 0)
  }

  def testThatOverloadedTestMethodsAreDiscovered() {
    val a = new Suite {
      def testThis() = ()
      def testThis(info: Informer) = ()
    }
    assert(a.expectedTestCount(Filter()) === 2)
    assert(a.testNames.size === 2)
    assert(a.tags.keySet.size === 0)
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

  def testTestDurations() {

    class MySuite extends Suite {
      def testSucceeds() = ()
      def testFails() { fail() }
    }

    val mySuite = new MySuite
    val myReporter = new TestDurationReporter
    mySuite.run(None, myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.testSucceededWasFiredAndHadADuration)
    assert(myReporter.testFailedWasFiredAndHadADuration)
  }

  def testSuiteDurations() {

    // the suite duration is sent by runNestedSuites, so MySuite needs a
    // nested suite
    class MySuite extends Suite {
      override def nestedSuites = List(new Suite {})
      def testSucceeds() = ()
      def testFails() { fail() }
    }

    val mySuite = new MySuite
    val myReporter = new SuiteDurationReporter
    mySuite.run(None, myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.suiteCompletedWasFiredAndHadADuration)

    class SuiteThatAborts extends Suite {
      override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
              config: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
        throw new RuntimeException("Aborting for testing purposes")
      }
    }

    // the suite duration is sent by runNestedSuites, so MySuite needs a
    // nested suite
    class MyOtherSuite extends Suite {
      override def nestedSuites = List(new SuiteThatAborts)
      def testSucceeds() = ()
      def testFails() { fail() }
    }

    val myOtherSuite = new MyOtherSuite
    val myOtherReporter = new SuiteDurationReporter
    myOtherSuite.run(None, myOtherReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myOtherReporter.suiteAbortedWasFiredAndHadADuration)
  }

  def testPending() {

    class MySuite extends Suite {
      def testPending() { pending }
    }

    val mySuite = new MySuite
    val myReporter = new PendingReporter
    mySuite.run(None, myReporter, new Stopper {}, Filter(), Map(), None, new Tracker(new Ordinal(99)))
    assert(myReporter.testPendingWasFired)
  }
}


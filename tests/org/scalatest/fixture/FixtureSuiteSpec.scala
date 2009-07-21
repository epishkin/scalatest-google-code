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
package org.scalatest.fixture

import collection.immutable.TreeSet
import events.TestFailed

class FixtureSuiteSpec extends org.scalatest.Spec with PrivateMethodTester with SharedHelpers {

  describe("The private testMethodTakesInformer method") {
    val testMethodTakesInformer = PrivateMethod[Boolean]('testMethodTakesInformer)
    val suiteObject = Suite
    it("should return true if passed a string that ends in (Fixture, Informer)") {
      assert(suiteObject invokePrivate testMethodTakesInformer("thisDoes(Fixture, Informer)"))
      assert(suiteObject invokePrivate testMethodTakesInformer("(Fixture, Informer)"))
      assert(suiteObject invokePrivate testMethodTakesInformer("test(Fixture, Informer)"))
    }
    it("should return false if passed a string that doesn't end in (Fixture, Informer)") {
      assert(!(suiteObject invokePrivate testMethodTakesInformer("thisDoesNot(Fixture)")))
      assert(!(suiteObject invokePrivate testMethodTakesInformer("test(Fixture)")))
    }
  }


  describe("A fixture.Suite without SimpleWithFixture") {

    it("should return the test names in alphabetical order from testNames") {
      val a = new Suite {
        type Fixture = String
        def withFixture(fun: String => Unit, config: Map[String, Any]) {}
        def testThis(fixture: String) {}
        def testThat(fixture: String) {}
      }

      expect(List("testThat(Fixture)", "testThis(Fixture)")) {
        a.testNames.elements.toList
      }

      val b = new Suite {
        type Fixture = String
        def withFixture(fun: String => Unit, config: Map[String, Any]) {}
      }

      expect(List[String]()) {
        b.testNames.elements.toList
      }

      val c = new Suite {
        type Fixture = String
        def withFixture(fun: String => Unit, config: Map[String, Any]) {}
        def testThat(fixture: String) {}
        def testThis(fixture: String) {}
      }

      expect(List("testThat(Fixture)", "testThis(Fixture)")) {
        c.testNames.elements.toList
      }
    }

    it("should discover tests with and without Informer parameters") {
      val a = new Suite {
        type Fixture = String
        def withFixture(fun: String => Unit, config: Map[String, Any]) {}
        def testThis(fixture: String) = ()
        def testThat(fixture: String, info: Informer) = ()
      }
      assert(a.testNames === TreeSet("testThat(Fixture, Informer)", "testThis(Fixture)"))
    }

    it("should pass in the fixture to every test method") {
      val a = new Suite {
        type Fixture = String
        val hello = "Hello, world!"
        def withFixture(fun: String => Unit, config: Map[String, Any]) {
          fun(hello)
        }
        def testThis(fixture: String) {
          assert(fixture === hello)
        }
        def testThat(fixture: String, info: Informer) {
          assert(fixture === hello)
        }
      }
      a.run(None, SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker())
    }

    it("can pass in the config map to every test method via the fixture") {
      val key = "greeting"
      val hello = "Hello, world!"
      val a = new Suite {
        type Fixture = Map[String, Any]
        def withFixture(fun: Fixture => Unit, config: Map[String, Any]) {
          fun(config)
        }
        def testThis(fixture: Fixture) {
          assert(fixture(key) === hello)
        }
        def testThat(fixture: Fixture, info: Informer) {
          assert(fixture(key) === hello)
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, rep, new Stopper {}, Filter(), Map(key -> hello), None, new Tracker())
      assert(!rep.eventsReceived.exists(_.isInstanceOf[TestFailed]))
    }
  }

  describe("A fixture.Suite with SimpleWithFixture") {
    it("should return the test names in alphabetical order from testNames") {
      val a = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        def testThis(fixture: String) {}
        def testThat(fixture: String) {}
      }

      expect(List("testThat(Fixture)", "testThis(Fixture)")) {
        a.testNames.elements.toList
      }

      val b = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
      }

      expect(List[String]()) {
        b.testNames.elements.toList
      }

      val c = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        def testThat(fixture: String) {}
        def testThis(fixture: String) {}
      }

      expect(List("testThat(Fixture)", "testThis(Fixture)")) {
        c.testNames.elements.toList
      }
    }

    it("should discover tests with and without Informer parameters") {
      val a = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        def testThis(fixture: String) = ()
        def testThat(fixture: String, info: Informer) = ()
      }
      assert(a.testNames === TreeSet("testThat(Fixture, Informer)", "testThis(Fixture)"))
    }

    it("should pass in the fixture to every test method") {
      val a = new Suite with SimpleWithFixture {
        type Fixture = String
        val hello = "Hello, world!"
        def withFixture(fun: String => Unit) {
          fun(hello)
        }
        def testThis(fixture: String) {
          assert(fixture === hello)
        }
        def testThat(fixture: String, info: Informer) {
          assert(fixture === hello)
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, rep, new Stopper {}, Filter(), Map(), None, new Tracker())
      assert(!rep.eventsReceived.exists(_.isInstanceOf[TestFailed]))
    }
    
    it("should return a correct tags map from the tags method") {

      val a = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        @Ignore
        def testThis(fixture: Fixture) = ()
        def testThat(fixture: Fixture, info: Informer) = ()
      }

      assert(a.tags === Map("testThis(Fixture)" -> Set("org.scalatest.Ignore")))

      val b = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        def testThis(fixture: Fixture) = ()
        @Ignore
        def testThat(fixture: Fixture, info: Informer) = ()
      }

      assert(b.tags === Map("testThat(Fixture, Informer)" -> Set("org.scalatest.Ignore")))

      val c = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        @Ignore
        def testThis(fixture: Fixture) = ()
        @Ignore
        def testThat(fixture: Fixture, info: Informer) = ()
      }

      assert(c.tags === Map("testThis(Fixture)" -> Set("org.scalatest.Ignore"), "testThat(Fixture, Informer)" -> Set("org.scalatest.Ignore")))

      val d = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        @SlowAsMolasses
        def testThis(fixture: Fixture) = ()
        @SlowAsMolasses
        @Ignore
        def testThat(fixture: Fixture, info: Informer) = ()
      }

      assert(d.tags === Map("testThis(Fixture)" -> Set("org.scalatest.SlowAsMolasses"), "testThat(Fixture, Informer)" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses")))

      val e = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
      }
      assert(e.tags === Map())
    }

    class TestWasCalledSuite extends Suite with SimpleWithFixture {
      type Fixture = String
      def withFixture(fun: String => Unit) { fun("hi") }
      var theTestThisCalled = false
      var theTestThatCalled = false
      def testThis(s: String) { theTestThisCalled = true }
      def testThat(s: String) { theTestThatCalled = true }
    }

    it("should execute all tests when run is called with testName None") {

      val b = new TestWasCalledSuite
      b.run(None, SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(b.theTestThisCalled)
      assert(b.theTestThatCalled)
    }

    it("should execute one test when run is called with a defined testName") {

      val a = new TestWasCalledSuite
      a.run(Some("testThis(Fixture)"), SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(a.theTestThisCalled)
      assert(!a.theTestThatCalled)
    }

    it("should report as ignored, ant not run, tests marked ignored") {

      val a = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) { fun("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        def testThis(fixture: Fixture) { theTestThisCalled = true }
        def testThat(fixture: Fixture, info: Informer) { theTestThatCalled = true }
      }

      val repA = new TestIgnoredTrackingReporter
      a.run(None, repA, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(!repA.testIgnoredReceived)
      assert(a.theTestThisCalled)
      assert(a.theTestThatCalled)

      val b = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) { fun("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        @Ignore
        def testThis(fixture: Fixture) { theTestThisCalled = true }
        def testThat(fixture: Fixture, info: Informer) { theTestThatCalled = true }
      }

      val repB = new TestIgnoredTrackingReporter
      b.run(None, repB, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repB.testIgnoredReceived)
      assert(repB.lastEvent.isDefined)
      assert(repB.lastEvent.get.testName endsWith "testThis(Fixture)")
      assert(!b.theTestThisCalled)
      assert(b.theTestThatCalled)

      val c = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) { fun("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        def testThis(fixture: Fixture) { theTestThisCalled = true }
        @Ignore
        def testThat(fixture: Fixture, info: Informer) { theTestThatCalled = true }
      }

      val repC = new TestIgnoredTrackingReporter
      c.run(None, repC, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repC.testIgnoredReceived)
      assert(repC.lastEvent.isDefined)
      assert(repC.lastEvent.get.testName endsWith "testThat(Fixture, Informer)", repC.lastEvent.get.testName)
      assert(c.theTestThisCalled)
      assert(!c.theTestThatCalled)

      val d = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) { fun("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        @Ignore
        def testThis(fixture: Fixture) { theTestThisCalled = true }
        @Ignore
        def testThat(fixture: Fixture, info: Informer) { theTestThatCalled = true }
      }

      val repD = new TestIgnoredTrackingReporter
      d.run(None, repD, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(repD.testIgnoredReceived)
      assert(repD.lastEvent.isDefined)
      assert(repD.lastEvent.get.testName endsWith "testThis(Fixture)") // last because run alphabetically
      assert(!d.theTestThisCalled)
      assert(!d.theTestThatCalled)
    }

    it("should run a test marked as ignored if run is invoked with that testName") {

      val e = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) { fun("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        @Ignore
        def testThis(fixture: Fixture) { theTestThisCalled = true }
        def testThat(fixture: Fixture, info: Informer) { theTestThatCalled = true }
      }

      val repE = new TestIgnoredTrackingReporter
      e.run(Some("testThis(Fixture)"), repE, new Stopper {}, Filter(), Map(), None, new Tracker)
      assert(!repE.testIgnoredReceived)
      assert(e.theTestThisCalled)
      assert(!e.theTestThatCalled)
    }

    it("should throw IllegalArgumentException if run is passed a testName that does not exist") {
      val suite = new Suite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) { fun("hi") }
        var theTestThisCalled = false
        var theTestThatCalled = false
        def testThis(fixture: Fixture) { theTestThisCalled = true }
        def testThat(fixture: Fixture, info: Informer) { theTestThatCalled = true }
      }

      intercept[IllegalArgumentException] {
        // Here, they forgot that the name is actually testThis(Fixture)
        suite.run(Some("testThis"), SilentReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
      }
    }
  }
}

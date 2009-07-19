/*
 * Copyright 2001-2009 Artima, Inc.
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

import org.scalatest.events.{TestStarting, TestFailed}
import org.scalatest.matchers.ShouldVerb
import org.scalatest.matchers.CanVerb
import org.scalatest.matchers.MustVerb

object SlowTest extends Tag("SlowTest")

class FixtureFlatSpecSpec extends org.scalatest.Spec with PrivateMethodTester with SharedHelpers {

  describe("A fixture.FlatSpec ") {
    it("A fixture.Spec should return the test names in order of registration from testNames") {
      val a = new FlatSpec with SimpleWithFixture with ShouldVerb {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        "Something" should "do that" in { fixture =>
        }
        it should "do this" in { fixture =>
        }
      }

      expect(List("Something should do that", "Something should do this")) {
        a.testNames.elements.toList
      }

      val b = new FlatSpec with SimpleWithFixture with ShouldVerb {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
      }

      expect(List[String]()) {
        b.testNames.elements.toList
      }

      val c = new FlatSpec with SimpleWithFixture with ShouldVerb {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        "Something" should "do this" in { fixture =>
        }
        it should "do that" in { fixture =>
        }
      }

      expect(List("Something should do this", "Something should do that")) {
        c.testNames.elements.toList
      }
    }

    it("should throw DuplicateTestNameException if a duplicate test name registration is attempted") {

      intercept[DuplicateTestNameException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          it should "test this" in { fixture => }
          it should "test this" in { fixture => }
        }
      }
      intercept[DuplicateTestNameException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          it should "test this" in { fixture => }
          it should "test this" taggedAs(SlowTest) ignore { fixture => }
        }
      }
      intercept[DuplicateTestNameException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          ignore should "test this" in { fixture => }
          it should "test this" ignore { fixture => }
        }
      }
      intercept[DuplicateTestNameException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          ignore should "test this" in { fixture => }
          it should "test this" in { fixture => }
        }
      }
    }

    it("should pass in the fixture to every test method") {
      val a = new FlatSpec with SimpleWithFixture with ShouldVerb {
        type Fixture = String
        val hello = "Hello, world!"
        def withFixture(fun: String => Unit) {
          fun(hello)
        }
        "Something" should "do this" in { fixture =>
          assert(fixture === hello)
        }
        it should "do that" in { fixture =>
          assert(fixture === hello)
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, rep, new Stopper {}, Filter(), Map(), None, new Tracker())
      assert(!rep.eventsReceived.exists(_.isInstanceOf[TestFailed]))
    }

    it("should run tests registered via the 'it can behave like' syntax") {
      trait SharedFlatSpecTests { this: FlatSpec =>
        def nonEmptyStack(s: String)(i: Int) {
          it can "I am shared" in { fixture => }
        }
      }
      class MyFlatSpec extends FlatSpec with SimpleWithFixture with SharedFlatSpecTests {
        type Fixture = String
        def withFixture(fun: String => Unit) {
          fun("hi")
        }
        it can behave like nonEmptyStack("hi")(1)
      }
      val suite = new MyFlatSpec
      val reporter = new EventRecordingReporter
      suite.run(None, reporter, new Stopper {}, Filter(), Map(), None, new Tracker)

      val indexedList = reporter.eventsReceived

      val testStartingOption = indexedList.find(_.isInstanceOf[TestStarting])
      assert(testStartingOption.isDefined)
      assert(testStartingOption.get.asInstanceOf[TestStarting].testName === "can I am shared")
    }
    it("should throw NullPointerException if a null test tag is provided") {
      // it
      intercept[NullPointerException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          it should "hi" taggedAs(null) in { fixture => }
        }
      }
      val caught = intercept[NullPointerException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          it should "hi" taggedAs(mytags.SlowAsMolasses, null) in { fixture => }
        }
      }
      assert(caught.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          it should "hi" taggedAs(mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) in { fixture => }
        }
      }
      // ignore
      intercept[NullPointerException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          ignore should "hi" taggedAs(null) in { fixture => }
        }
      }
      val caught2 = intercept[NullPointerException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          ignore should "hi" taggedAs(mytags.SlowAsMolasses, null) in { fixture => }
        }
      }
      assert(caught2.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          ignore should "hi" taggedAs(mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) in { fixture => }
        }
      }
      intercept[NullPointerException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          it should "hi" taggedAs(null) ignore { fixture => }
        }
      }
      val caught3 = intercept[NullPointerException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          it should "hi" taggedAs(mytags.SlowAsMolasses, null) ignore { fixture => }
        }
      }
      assert(caught3.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new FlatSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          it should "hi" taggedAs(mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) ignore { fixture => }
        }
      }
    }
    it("should return a correct tags map from the tags method, when using regular (not shorthand)" +
            " notation and ignore replacing it") {

      val a = new FlatSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        ignore should "test this" in { fixture => }
        it should "test that" in { fixture => }
      }
      expect(Map("should test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new FlatSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        it can "test this" in { fixture => }
        ignore can "test that" in { fixture => }
      }
      expect(Map("can test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new FlatSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        ignore must "test this" in { fixture => }
        ignore must "test that" in { fixture => }
      }
      expect(Map("must test this" -> Set("org.scalatest.Ignore"), "must test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new FlatSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        it must "test this" taggedAs(mytags.SlowAsMolasses) in { fixture => }
        ignore must "test that" taggedAs(mytags.SlowAsMolasses) in { fixture => }
      }
      expect(Map("must test this" -> Set("org.scalatest.SlowAsMolasses"), "must test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }

      val e = new FlatSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        it must "test this" in { fixture => }
        it must "test that" in { fixture => }
      }
      expect(Map()) {
        e.tags
      }

      val f = new FlatSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        it can "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) in { fixture => }
        it can "test that" taggedAs(mytags.SlowAsMolasses) in  { fixture => }
      }
      expect(Map("can test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "can test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        f.tags
      }

      val g = new FlatSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        it should "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) in { fixture => }
        it should "test that" taggedAs(mytags.SlowAsMolasses) in { fixture => }
      }
      expect(Map("should test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "should test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        g.tags
      }
    }
    it("should return a correct tags map from the tags method, when using regular (not shorthand)" +
            " notation and ignore replacing in") {

      val a = new FlatSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        it should "test this" ignore { fixture => }
        it should "test that" in { fixture => }
      }
      expect(Map("should test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new FlatSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        it can "test this" in { fixture => }
        it can "test that" ignore { fixture => }
      }
      expect(Map("can test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new FlatSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        it must "test this" ignore { fixture => }
        it must "test that" ignore { fixture => }
      }
      expect(Map("must test this" -> Set("org.scalatest.Ignore"), "must test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new FlatSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        it must "test this" taggedAs(mytags.SlowAsMolasses) in { fixture => }
        it must "test that" taggedAs(mytags.SlowAsMolasses) ignore { fixture => }
      }
      expect(Map("must test this" -> Set("org.scalatest.SlowAsMolasses"), "must test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }
    }

    it("should return a correct tags map from the tags method, when using shorthand notation") {

      val a = new FlatSpec with SimpleWithFixture with ShouldVerb {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        "A Stack" should "test this" ignore { fixture => }
        "A Stack" should "test that" in { fixture => }
      }
      expect(Map("A Stack should test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new FlatSpec with SimpleWithFixture with CanVerb {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        "A Stack" can "test this" in { fixture => }
        "A Stack" can "test that" ignore { fixture => }
      }
      expect(Map("A Stack can test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new FlatSpec with SimpleWithFixture with MustVerb {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        "A Stack" must "test this" ignore { fixture => }
        "A Stack" must "test that" ignore { fixture => }
      }
      expect(Map("A Stack must test this" -> Set("org.scalatest.Ignore"), "A Stack must test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new FlatSpec with SimpleWithFixture with MustVerb {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        "A Stack" must "test this" taggedAs(mytags.SlowAsMolasses) in { fixture => }
        "A Stack" must "test that" taggedAs(mytags.SlowAsMolasses) ignore { fixture => }
      }
      expect(Map("A Stack must test this" -> Set("org.scalatest.SlowAsMolasses"), "A Stack must test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }

      val e = new FlatSpec with SimpleWithFixture with MustVerb {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        "A Stack" must "test this" in { fixture => }
        "A Stack" must "test that" in { fixture => }
      }
      expect(Map()) {
        e.tags
      }

      val f = new FlatSpec with SimpleWithFixture with CanVerb {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        "A Stack" can "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) in { fixture => }
        "A Stack" can "test that" taggedAs(mytags.SlowAsMolasses) in  { fixture => }
      }
      expect(Map("A Stack can test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "A Stack can test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        f.tags
      }

      val g = new FlatSpec with SimpleWithFixture with ShouldVerb {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        "A Stack" should "test this" taggedAs(mytags.SlowAsMolasses, mytags.WeakAsAKitten) in { fixture => }
        "A Stack" should "test that" taggedAs(mytags.SlowAsMolasses) in  { fixture => }
      }
      expect(Map("A Stack should test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "A Stack should test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        g.tags
      }
    }
  }
}

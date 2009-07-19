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

import events.TestFailed

class FixtureFunSuiteSpec extends org.scalatest.Spec with PrivateMethodTester with SharedHelpers {

  describe("A fixture.FunSuite") {
    it("should return the test names in order of registration from testNames") {
      val a = new FunSuite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        test("that") { fixture =>
        }
        test("this") { fixture =>
        }
      }

      expect(List("that", "this")) {
        a.testNames.elements.toList
      }

      val b = new FunSuite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
      }

      expect(List[String]()) {
        b.testNames.elements.toList
      }

      val c = new FunSuite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        test("this") { fixture =>
        }
        test("that") { fixture =>
        }
      }

      expect(List("this", "that")) {
        c.testNames.elements.toList
      }
    }

    it("should throw NotAllowedException if a duplicate test name registration is attempted") {

      intercept[DuplicateTestNameException] {
        new FunSuite with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          test("test this") { fixture =>
          }
          test("test this") { fixture =>
          }
        }
      }
      intercept[DuplicateTestNameException] {
        new FunSuite with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          test("test this") { fixture =>
          }
          ignore("test this") { fixture =>
          }
        }
      }
      intercept[DuplicateTestNameException] {
        new FunSuite with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          ignore("test this") { fixture =>
          }
          ignore("test this") { fixture =>
          }
        }
      }
      intercept[DuplicateTestNameException] {
        new FunSuite with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          ignore("test this") { fixture =>
          }
          test("test this") { fixture =>
          }
        }
      }
    }

    it("should pass in the fixture to every test method") {
      val a = new FunSuite with SimpleWithFixture {
        type Fixture = String
        val hello = "Hello, world!"
        def withFixture(fun: String => Unit) {
          fun(hello)
        }
        test("this") { fixture =>
          assert(fixture === hello)
        }
        test("that") { fixture =>
          assert(fixture === hello)
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, rep, new Stopper {}, Filter(), Map(), None, new Tracker())
      assert(!rep.eventsReceived.exists(_.isInstanceOf[TestFailed]))
    }

    it("should throw NullPointerException if a null test tag is provided") {
      // test
      intercept[NullPointerException] {
        new FunSuite with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          test("hi", null) { fixture => }
        }
      }
      val caught = intercept[NullPointerException] {
        new FunSuite with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          test("hi", mytags.SlowAsMolasses, null) { fixture => }
        }
      }
      assert(caught.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new FunSuite with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          test("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) { fixture => }
        }
      }
      // ignore
      intercept[NullPointerException] {
        new FunSuite with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          ignore("hi", null) { fixture => }
        }
      }
      val caught2 = intercept[NullPointerException] {
        new FunSuite with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          ignore("hi", mytags.SlowAsMolasses, null) { fixture => }
        }
      }
      assert(caught2.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new FunSuite with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          ignore("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) { fixture => }
        }
      }
    }
    it("should return a correct tags map from the tags method") {

      val a = new FunSuite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        ignore("test this") { fixture => }
        test("test that") { fixture => }
      }
      expect(Map("test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new FunSuite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        test("test this") { fixture => }
        ignore("test that") { fixture => }
      }
      expect(Map("test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new FunSuite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        ignore("test this") { fixture => }
        ignore("test that") { fixture => }
      }
      expect(Map("test this" -> Set("org.scalatest.Ignore"), "test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new FunSuite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        test("test this", mytags.SlowAsMolasses) { fixture => }
        ignore("test that", mytags.SlowAsMolasses) { fixture => }
      }
      expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }

      val e = new FunSuite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
      }
      expect(Map()) {
        e.tags
      }

      val f = new FunSuite with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        test("test this", mytags.SlowAsMolasses, mytags.WeakAsAKitten) { fixture => }
        test("test that", mytags.SlowAsMolasses) { fixture => }
      }
      expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        f.tags
      }
    }
  }
}

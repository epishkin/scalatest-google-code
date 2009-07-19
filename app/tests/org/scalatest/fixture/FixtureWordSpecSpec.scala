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

class FixtureWordSpecSpec extends org.scalatest.Spec with PrivateMethodTester with SharedHelpers {

  describe("A fixture.WordSpec") {

    it("should return the test names in order of registration from testNames") {
      val a = new WordSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        "Something" should {
          "do that" in { fixture =>
          }
          "do this" in { fixture =>
          }
        }
      }

      expect(List("Something should do that", "Something should do this")) {
        a.testNames.elements.toList
      }

      val b = new WordSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
      }

      expect(List[String]()) {
        b.testNames.elements.toList
      }

      val c = new WordSpec with SimpleWithFixture {
        type Fixture = String
        def withFixture(fun: String => Unit) {}
        "Something" should {
          "do this" in { fixture =>
          }
          "do that" in { fixture =>
          }
        }
      }

      expect(List("Something should do this", "Something should do that")) {
        c.testNames.elements.toList
      }
    }

    it("should throw DuplicateTestNameException if a duplicate test name registration is attempted") {

      intercept[DuplicateTestNameException] {
        new WordSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          "should test this" in { fixture => }
          "should test this" in { fixture => }
        }
      }
      intercept[DuplicateTestNameException] {
        new WordSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          "should test this" in { fixture => }
          "should test this" ignore { fixture => }
        }
      }
      intercept[DuplicateTestNameException] {
        new WordSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          "should test this" ignore { fixture => }
          "should test this" ignore { fixture => }
        }
      }
      intercept[DuplicateTestNameException] {
        new WordSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          "should test this" ignore { fixture => }
          "should test this" in { fixture => }
        }
      }
    }

    it("should pass in the fixture to every test method") {
      val a = new WordSpec with SimpleWithFixture {
        type Fixture = String
        val hello = "Hello, world!"
        def withFixture(fun: String => Unit) {
          fun(hello)
        }
        "Something" should {
          "do this" in { fixture =>
            assert(fixture === hello)
          }
          "do that" in { fixture =>
            assert(fixture === hello)
          }
        }
      }
      val rep = new EventRecordingReporter
      a.run(None, rep, new Stopper {}, Filter(), Map(), None, new Tracker())
      assert(!rep.eventsReceived.exists(_.isInstanceOf[TestFailed]))
    }
    it("should throw NullPointerException if a null test tag is provided") {
      // it
      intercept[NullPointerException] {
        new WordSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          "hi" taggedAs(null) in { fixture => }
        }
      }
      val caught = intercept[NullPointerException] {
        new WordSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          "hi" taggedAs(mytags.SlowAsMolasses, null) in { fixture => }
        }
      }
      assert(caught.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new WordSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          "hi" taggedAs(mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) in { fixture => }
        }
      }
      // ignore
      intercept[NullPointerException] {
        new WordSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          "hi" taggedAs(null) ignore { fixture => }
        }
      }
      val caught2 = intercept[NullPointerException] {
        new WordSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          "hi" taggedAs(mytags.SlowAsMolasses, null) ignore { fixture => }
        }
      }
      assert(caught2.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new WordSpec with SimpleWithFixture {
          type Fixture = String
          def withFixture(fun: String => Unit) {}
          "hi" taggedAs(mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) ignore { fixture => }
        }
      }
    }
  }
}

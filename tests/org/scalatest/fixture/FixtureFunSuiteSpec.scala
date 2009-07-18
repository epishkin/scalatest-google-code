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
  }
}

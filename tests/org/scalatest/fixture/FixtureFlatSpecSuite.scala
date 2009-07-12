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
import matchers.ShouldVerb

class FixtureFlatSpecSuite extends org.scalatest.FunSuite with PrivateMethodTester with SharedHelpers {

  test("A fixture.Spec should return the test names in alphabetical order from testNames") {
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

    expect(List("should do this", "should do that")) {
      c.testNames.elements.toList
    }
  }

  test("A fixture.Spec should pass in the fixture to every test method") {
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
}

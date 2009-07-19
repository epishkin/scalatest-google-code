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
package org.scalatest

import org.scalatest.events.TestStarting

class FeatureSpecSpec extends Spec with SharedHelpers {

  describe("A FeatureSpec") {

    it("should return the scenario names in registration order from testNames") {

      val a = new FeatureSpec {
        scenario("test this") {}
        scenario("test that") {}
      }

      expect(List("test this", "test that")) {
        a.testNames.elements.toList
      }

      val b = new FeatureSpec {}

      expect(List[String]()) {
        b.testNames.elements.toList
      }

      val c = new FeatureSpec {
        scenario("test that") {}
        scenario("test this") {}
      }

      expect(List("test that", "test this")) {
        c.testNames.elements.toList
      }
    }

    it("should throw NotAllowedException if a duplicate scenario name registration is attempted") {

      intercept[DuplicateTestNameException] {
        new FeatureSpec {
          scenario("test this") {}
          scenario("test this") {}
        }
      }
      intercept[DuplicateTestNameException] {
        new FeatureSpec {
          scenario("test this") {}
          ignore("test this") {}
        }
      }
      intercept[DuplicateTestNameException] {
        new FeatureSpec {
          ignore("test this") {}
          ignore("test this") {}
        }
      }
      intercept[DuplicateTestNameException] {
        new FeatureSpec {
          ignore("test this") {}
          scenario("test this") {}
        }
      }
    }

    it("should run tests registered via the scenariosFor syntax") {
      trait SharedFeatureSpecTests { this: FeatureSpec =>
        def nonEmptyStack(s: String)(i: Int) {
          scenario("I am shared") {}
        }
      }
      class MySuite extends FeatureSpec with SharedFeatureSpecTests {
        scenariosFor(nonEmptyStack("hi")(1))
      }
      val suite = new MySuite
      val reporter = new EventRecordingReporter
      suite.run(None, reporter, new Stopper {}, Filter(), Map(), None, new Tracker)

      val indexedList = reporter.eventsReceived

      val testStartingOption = indexedList.find(_.isInstanceOf[TestStarting])
      assert(testStartingOption.isDefined)
      assert(testStartingOption.get.asInstanceOf[TestStarting].testName === "I am shared")
    }

    it("should throw NullPointerException if a null test tag is provided") {
      // scenario
      intercept[NullPointerException] {
        new FeatureSpec {
          scenario("hi", null) {}
        }
      }
      val caught = intercept[NullPointerException] {
        new FeatureSpec {
          scenario("hi", mytags.SlowAsMolasses, null) {}
        }
      }
      assert(caught.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new FeatureSpec {
          scenario("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) {}
        }
      }
      // ignore
      intercept[NullPointerException] {
        new FeatureSpec {
          ignore("hi", null) {}
        }
      }
      val caught2 = intercept[NullPointerException] {
        new FeatureSpec {
          ignore("hi", mytags.SlowAsMolasses, null) {}
        }
      }
      assert(caught2.getMessage === "a test tag was null")
      intercept[NullPointerException] {
        new FeatureSpec {
          ignore("hi", mytags.SlowAsMolasses, null, mytags.WeakAsAKitten) {}
        }
      }
    }
    it("should return a correct tags map from the tags method") {

      val a = new FeatureSpec {
        ignore("test this") {}
        scenario("test that") {}
      }
      expect(Map("test this" -> Set("org.scalatest.Ignore"))) {
        a.tags
      }

      val b = new FeatureSpec {
        scenario("test this") {}
        ignore("test that") {}
      }
      expect(Map("test that" -> Set("org.scalatest.Ignore"))) {
        b.tags
      }

      val c = new FeatureSpec {
        ignore("test this") {}
        ignore("test that") {}
      }
      expect(Map("test this" -> Set("org.scalatest.Ignore"), "test that" -> Set("org.scalatest.Ignore"))) {
        c.tags
      }

      val d = new FeatureSpec {
        scenario("test this", mytags.SlowAsMolasses) {}
        ignore("test that", mytags.SlowAsMolasses) {}
      }
      expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses"), "test that" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses"))) {
        d.tags
      }

      val e = new FeatureSpec {}
      expect(Map()) {
        e.tags
      }

      val f = new FeatureSpec {
        scenario("test this", mytags.SlowAsMolasses, mytags.WeakAsAKitten) {}
        scenario("test that", mytags.SlowAsMolasses) {}
      }
      expect(Map("test this" -> Set("org.scalatest.SlowAsMolasses", "org.scalatest.WeakAsAKitten"), "test that" -> Set("org.scalatest.SlowAsMolasses"))) {
        f.tags
      }
    }
  }
}

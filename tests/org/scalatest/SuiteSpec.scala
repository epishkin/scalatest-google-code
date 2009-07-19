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

import collection.immutable.TreeSet
import org.scalatest.events._

class SuiteSpec extends Spec with PrivateMethodTester with SharedHelpers {

  describe("The simpleNameForTest method") {
    it("should return the correct test simple name with or without Informer") {
      val simpleNameForTest = PrivateMethod[String]('simpleNameForTest)
      assert((Suite invokePrivate simpleNameForTest("testThis")) === "testThis")
      assert((Suite invokePrivate simpleNameForTest("testThis(Informer)")) === "testThis")
      assert((Suite invokePrivate simpleNameForTest("test(Informer)")) === "test")
      assert((Suite invokePrivate simpleNameForTest("test")) === "test")
    }
  }

  describe("A Suite") {
    it("should return the test names in alphabetical order from testNames") {
      val a = new Suite {
        def testThis() {}
        def testThat() {}
      }

      expect(List("testThat", "testThis")) {
        a.testNames.elements.toList
      }

      val b = new Suite {}

      expect(List[String]()) {
        b.testNames.elements.toList
      }

      val c = new Suite {
        def testThat() {}
        def testThis() {}
      }

      expect(List("testThat", "testThis")) {
        c.testNames.elements.toList
      }
    }
    
    it("should return the proper testNames for test methods whether or not they take an Informer") {

      val a = new Suite {
        def testThis() = ()
        def testThat(info: Informer) = ()
      }
      assert(a.testNames === TreeSet("testThat(Informer)", "testThis"))

      val b = new Suite {}
      assert(b.testNames === TreeSet[String]())
    }

    it("should return a correct tags map from the tags method") {

      val a = new Suite {
        @Ignore
        def testThis() = ()
        def testThat(info: Informer) = ()
      }

      assert(a.tags === Map("testThis" -> Set("org.scalatest.Ignore")))

      val b = new Suite {
        def testThis() = ()
        @Ignore
        def testThat(info: Informer) = ()
      }

      assert(b.tags === Map("testThat(Informer)" -> Set("org.scalatest.Ignore")))

      val c = new Suite {
        @Ignore
        def testThis() = ()
        @Ignore
        def testThat(info: Informer) = ()
      }

      assert(c.tags === Map("testThis" -> Set("org.scalatest.Ignore"), "testThat(Informer)" -> Set("org.scalatest.Ignore")))

      val d = new Suite {
        @SlowAsMolasses
        def testThis() = ()
        @SlowAsMolasses
        @Ignore
        def testThat(info: Informer) = ()
      }

      assert(d.tags === Map("testThis" -> Set("org.scalatest.SlowAsMolasses"), "testThat(Informer)" -> Set("org.scalatest.Ignore", "org.scalatest.SlowAsMolasses")))

      val e = new Suite {}
      assert(e.tags === Map())
    }
  }
}


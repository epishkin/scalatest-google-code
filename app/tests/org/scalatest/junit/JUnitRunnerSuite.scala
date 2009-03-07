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

package org.scalatest.junit {

  package helpers {

    import org.junit.runner.RunWith

    @RunWith(classOf[JUnitRunner])
    class EasySuite extends FunSuite {

      val runCount = 3
      val failedCount = 2

      test("JUnit ran this OK!") {
        assert(1 === 1)
      }

      test("JUnit ran this OK!, but it had a failure we hope") {
        assert(1 === 2)
      }

      test("bla bla bla") {
        assert(1 === 2)
      }
    }
  }

  import org.junit.runner.JUnitCore
  import org.junit.runner.Description
  import org.junit.runner.notification.Failure
  import org.scalatest.junit.helpers.EasySuite

  class JUnitRunnerSuite extends FunSuite {

    test("That EasySuite gets run by JUnit given its RunWith annotation") {
      val result = JUnitCore.runClasses(classOf[EasySuite])
      val easySuite = new EasySuite
      assert(result.getRunCount === easySuite.runCount) // EasySuite has 3 tests
      assert(result.getFailureCount === easySuite.failedCount) // EasySuite has 2 tests that blow up
    }
  }
}

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

package org.scalatest.junit

import org.junit.runner.JUnitCore
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.scalatest.junit.helpers.EasySuite

class JUnitRunnerSuite extends FunSuite {

  test("That EasySuite gets run by JUnit given its RunWith annotation") {
    val result = JUnitCore.runClasses(classOf[EasySuite])
    assert(result.getRunCount === 3) // EasySuite has 3 tests
    assert(result.getFailureCount === 2) // EasySuite has 2 tests that blow up
  }
}

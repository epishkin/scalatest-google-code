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

import org.scalatest.junit.helpers.HappySuite

class JUnitSuiteSuite extends FunSuite {

  class MyReporter extends Reporter {
    var testSucceededCalled = false
    var testStartingCalled = false
    var testStartingReport: Report = null
    var testSucceededReport: Report = null
    override def testStarting(report: Report) {
      testStartingCalled = true
      testStartingReport = report
    }
    override def testSucceeded(report: Report) {
      testSucceededCalled = true
      testSucceededReport = report
    }
  }

  test("A JUnitSuite with a JUnit 4 Test annotation will cause run starting to be invoked") {

    val happy = new HappySuite
    val repA = new MyReporter
    happy.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(repA.testStartingCalled)
    assert(repA.testStartingReport.name === "All")
  }

  test("A JUnitSuite with a JUnit 4 Test annotation will cause run succeeded to be invoked") {

    val happy = new HappySuite
    val repA = new MyReporter
    happy.execute(None, repA, new Stopper {}, Set(), Set(), Map(), None)
    assert(repA.testSucceededCalled)
    assert(repA.testSucceededReport.name === "verifySomething(org.scalatest.junit.helpers.HappySuite)")
  }
}

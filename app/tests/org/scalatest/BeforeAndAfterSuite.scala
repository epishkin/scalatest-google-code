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

import scala.collection.mutable.ListBuffer
import org.scalatest.events.Event
import org.scalatest.events.Ordinal

class BeforeAndAfterSuite extends FunSuite {

  class TheSuper extends Suite {
    var runTestWasCalled = false
    var runWasCalled = false
    override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any], tracker: Tracker) {
      runTestWasCalled = true
      super.runTest(testName, reporter, stopper, properties, tracker)
    }
    override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
                         properties: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
      runWasCalled = true
      super.run(testName, reporter, stopper, filter, properties, distributor, tracker)
    }
  }
  
  class MySuite extends TheSuper with BeforeAndAfter {
    var beforeEachCalledBeforeRunTest = false
    var afterEachCalledAfterRunTest = false
    var beforeAllCalledBeforeExecute = false
    var afterAllCalledAfterExecute = false
    override def beforeAll() {
      if (!runWasCalled)
        beforeAllCalledBeforeExecute = true
    }
    override def beforeEach() {
      if (!runTestWasCalled)
        beforeEachCalledBeforeRunTest = true
    }
    def testSomething() = ()
    override def afterEach() {
      if (runTestWasCalled)
        afterEachCalledAfterRunTest = true
    }
    override def afterAll() {
      if (runWasCalled)
        afterAllCalledAfterExecute = true
    }
  }

  test("super's runTest must be called") {
    val a = new MySuite
    a.run()
    assert(a.runTestWasCalled)
  }
  
  test("super's run must be called") {
    val a = new MySuite
    a.run()
    assert(a.runWasCalled)
  }

  test("beforeEach gets called before runTest") {
    val a = new MySuite
    a.run()
    assert(a.beforeEachCalledBeforeRunTest)
  }
  
  test("afterEach gets called after runTest") {
    val a = new MySuite
    a.run()
    assert(a.afterEachCalledAfterRunTest)
  }

  test("beforeAll gets called before run") {
    val a = new MySuite
    a.run()
    assert(a.beforeAllCalledBeforeExecute)
  }
  
  test("afterAll gets called after run") {
    val a = new MySuite
    a.run()
    assert(a.afterAllCalledAfterExecute)
  }
  
  // test exceptions with runTest
  test("If any invocation of beforeEach completes abruptly with an exception, runTest " +
    "will complete abruptly with the same exception.") {
    
    class MySuite extends Suite with BeforeAndAfter {
      override def beforeEach() { throw new NumberFormatException } 
    }
    intercept[NumberFormatException] {
      val a = new MySuite
      a.runTest("july", StubReporter, new Stopper {}, Map(), new Tracker)
    }
  }
  
  test("If any call to super.runTest completes abruptly with an exception, runTest " +
    "will complete abruptly with the same exception, however, before doing so, it will invoke afterEach") {
    trait FunkySuite extends Suite {
      override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any], tracker: Tracker) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with BeforeAndAfter {
      var afterEachCalled = false
      override def afterEach() {
        afterEachCalled = true
      }
    }
    val a = new MySuite
    intercept[NumberFormatException] {
      a.runTest("july", StubReporter, new Stopper {}, Map(), new Tracker)
    }
    assert(a.afterEachCalled)
  }
  
  test("If both super.runTest and afterEach complete abruptly with an exception, runTest " + 
    "will complete abruptly with the exception thrown by super.runTest.") {
    trait FunkySuite extends Suite {
      override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any], tracker: Tracker) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with BeforeAndAfter {
      var afterEachCalled = false
      override def afterEach() {
        afterEachCalled = true
        throw new IllegalArgumentException
      }
    }
    val a = new MySuite
    intercept[NumberFormatException] {
      a.runTest("july", StubReporter, new Stopper {}, Map(), new Tracker)
    }
    assert(a.afterEachCalled)
  }
  
  test("If super.runTest returns normally, but afterEach completes abruptly with an " +
    "exception, runTest will complete abruptly with the same exception.") {
       
    class MySuite extends Suite with BeforeAndAfter {
      override def afterEach() { throw new NumberFormatException }
      def testJuly() = ()
    }
    intercept[NumberFormatException] {
      val a = new MySuite
      a.runTest("testJuly", StubReporter, new Stopper {}, Map(), new Tracker)
    }
  }
 
  // test exceptions with run
  test("If any invocation of beforeAll completes abruptly with an exception, run " +
    "will complete abruptly with the same exception.") {
    
    class MySuite extends Suite with BeforeAndAfter {
      override def beforeAll() { throw new NumberFormatException }
      def testJuly() = ()
    }
    intercept[NumberFormatException] {
      val a = new MySuite
      a.run(None, StubReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    }
  }
 
  test("If any call to super.run completes abruptly with an exception, run " +
    "will complete abruptly with the same exception, however, before doing so, it will invoke afterAll") {
    trait FunkySuite extends Suite {
      override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
                           properties: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with BeforeAndAfter {
      var afterAllCalled = false
      override def afterAll() {
        afterAllCalled = true
      }
    }
    val a = new MySuite
    intercept[NumberFormatException] {
      a.run(None, StubReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    }
    assert(a.afterAllCalled)
  }
   
  test("If both super.run and afterAll complete abruptly with an exception, run " + 
    "will complete abruptly with the exception thrown by super.run.") {
    trait FunkySuite extends Suite {
      override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
                           properties: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with BeforeAndAfter {
      var afterAllCalled = false
      override def afterAll() {
        afterAllCalled = true
        throw new IllegalArgumentException
      }
    }
    val a = new MySuite
    intercept[NumberFormatException] {
      a.run(None, StubReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    }
    assert(a.afterAllCalled)
  }
  
  test("If super.run returns normally, but afterAll completes abruptly with an " +
    "exception, run will complete abruptly with the same exception.") {
       
    class MySuite extends Suite with BeforeAndAfter {
      override def afterAll() { throw new NumberFormatException }
      def testJuly() = ()
    }
    intercept[NumberFormatException] {
      val a = new MySuite
      a.run(None, StubReporter, new Stopper {}, Filter(), Map(), None, new Tracker)
    }
  }
}

class BeforeAndAfterExtendingSuite extends Suite with BeforeAndAfter {

  var sb: StringBuilder = _
  val lb = new ListBuffer[String]

  override def beforeEach() {
    sb = new StringBuilder("ScalaTest is ")
    lb.clear()
  }

  def testEasy() {
    sb.append("easy!")
    assert(sb.toString === "ScalaTest is easy!")
    assert(lb.isEmpty)
    lb += "sweet"
  }

  def testFun() {
    sb.append("fun!")
    assert(sb.toString === "ScalaTest is fun!")
    assert(lb.isEmpty)
  }
}

class BeforeAndAfterExtendingFunSuite extends FunSuite with BeforeAndAfter {

  var sb: StringBuilder = _
  val lb = new ListBuffer[String]

  override def beforeEach() {
    sb = new StringBuilder("ScalaTest is ")
    lb.clear()
  }

  test("easy") {
    sb.append("easy!")
    assert(sb.toString === "ScalaTest is easy!")
    assert(lb.isEmpty)
    lb += "sweet"
  }

  test("fun") {
    sb.append("fun!")
    assert(sb.toString === "ScalaTest is fun!")
    assert(lb.isEmpty)
  }
  
  // This now fails to compile, as I want
  // class IWantThisToFailToCompile extends Examples with BeforeAndAfter
}



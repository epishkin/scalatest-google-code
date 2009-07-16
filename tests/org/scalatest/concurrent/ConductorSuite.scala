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
package org.scalatest.concurrent

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.CountDownLatch
import matchers.ShouldMatchers
import Thread.State._

trait MustBeSugar { this: ShouldMatchers =>

  implicit def anyToMustBe(a: Any) = new {
    def mustBe(b: Any) {
      a should be(b)
    }

    def must_be(b: Any) {
      a should be(b)
    }
  }
}

class ConductorSuite extends FunSuite with ConductorMethods with ShouldMatchers with MustBeSugar {

  test("metronome order") {

    var s = ""

    thread("t1") {
      waitForBeat(1)
      s = s + "A"

      waitForBeat(3)
      s = s + "C"

      waitForBeat(6)
      s = s + "F"
    }

    thread("t2") {
      waitForBeat(2)
      s = s + "B"

      waitForBeat(5)
      s = s + "E"

      waitForBeat(8)
      s = s + "H"
    }

    thread("t3") {
      waitForBeat(4)
      s = s + "D"

      waitForBeat(7)
      s = s + "G"

      waitForBeat(9)
      s = s + "I"
    }

    whenFinished {
      s mustBe "ABCDEFGHI" // "Threads were not called in correct order"
    }
  }

  test("init before threads before finish") {
    val v1 = new AtomicInteger(0)
    val v2 = new AtomicInteger(0)
    assert(v1.compareAndSet(0, 1))
    assert(v2.compareAndSet(0, 1))
    val c = new CountDownLatch(2)

    thread("t1") {
      assert(v1.compareAndSet(1, 2))
      c.countDown()
      c.await()
    }

    thread("t2") {
      assert(v2.compareAndSet(1, 2))
      c.countDown()
      c.await()
    }

    whenFinished {
      v1.intValue() mustBe 2
      v2.intValue() mustBe 2
    }
  }

  test("wait for tick advances when threads are blocked") {
    var c: CountDownLatch = new CountDownLatch(3)

    thread {
      c.countDown()
      c.await()
    }

    thread {
      c.countDown()
      c.await()
    }

    thread {
      waitForBeat(1)
      c.getCount mustBe 1
      waitForBeat(2) // advances quickly
      c.getCount mustBe 1
      c.countDown()
    }

    whenFinished {
      c.getCount() mustBe 0
    }
  }

  test("wait for tick blocks thread") {

    logLevel = everything

    var t: Thread = null

    thread {
      t = currentThread
      waitForBeat(2)
    }

    thread {
      waitForBeat(1)
      t.getState should (be (WAITING) or be (BLOCKED))
    }
  }

  test("thread terminates before finish called") {
    var t1, t2: Thread = null

    thread {
      t1 = Thread.currentThread
    }

    thread {
      t2 = Thread.currentThread
    }

    whenFinished {
      t1.getState mustBe TERMINATED
      t2.getState mustBe TERMINATED
    }
  }

  test("thread methods invoked on different threads") {
    var t1, t2: Thread = null

    thread {
      t1 = Thread.currentThread
      waitForBeat(2)
    }

    thread {
      t2 = Thread.currentThread
      waitForBeat(2)
    }

    thread {
      waitForBeat(1)
      t1 should not(be(t2))
    }
  }

  // Josh, when a test is marked (pending) it completes abruptly with TestPendingException. This is
  // not a test failure but an indication the test is pending, not yet implemented. We need to check
  // for this in ConductorMethods, and let it through somehow. Right now it causes a big red stack
  // trace. Once that works, you can uncomment the rest of these and they'll print out nicely as
  // pending tests.

  // test("handle TestPendingException properly in ConductorMehthods") (pending)

  // TODO:
  // 1. rename start() to conductTest() in both Conductor and ConductorMethods
  // 2. add a testWasConducted: Boolean method to both Conductor and ConductorMethods

  // test("if conductTest is called twice, the second time it throws an IllegalStateException") (pending)

  // test("if conductTest has not been called, testWasConducted should return false") (pending)

  // test("if conductTest has been called, testWasConducted should return true") (pending)
  
  // test("two thread calls return threads that both are in the same thread group") (pending)

  // test("if a thread call is nested inside another thread call, both threads are in the same thread group") (pending)

  // test("top level thread calls result in a running thread that is blocked such that it doesn't execute " +
  //        "prior to conductTest being called.") (pending)

  // test("nested thread calls result in a running thread that is allowed to execute immediately") (pending)

  // test("if whenFinished is called from a thread that did not construct the conductor, an IllegalStateException is thrown" +
  //        "that explains the problem") (pending)

  // test("if whenFinished is called twice on the same conductor, an IllegalStateException is thrown that explains it" +
  //        "can only be called once") (pending)

  // One issue is whther ConductorMethods should have a conductTest on it at all. Possibly not, but the trouble
  // is then that Conductor does have this method, and ConductorMethods wouldn't be being completely honest.
  // Also there would be something you could call in ConductorFixture that you couldn't call in ConductorMethods.
  // So my feeling is go ahead and add it to be consistent, but I'm not sure. Please check also to see if there are any
  // other public methods on Conductor that are not in ConductorMethods. If this is the only one, then probably we
  // should add it.

  // For ConductorMethods (probably in a ConductorMethodsSuite)
  // test("if conductTest is called from within the test itself, the test still succeeds (in other words," +
  //        "ConductorMethods doesn't call it if testWasConducted is true.") (pending)

  // test("if conductTest is not called from within the test itself, the test still executes (because
  //       "ConductorMethods calls it given testWasConducted is false") (pending)

  // ConductorFixture  (probably in a ConductorMethodsSuite)
  // test("if conductTest is called from within the test itself, the test still succeeds (in other words," +
  //        "ConductorFixture doesn't call it if testWasConducted is true.") (pending)

  // test("if conductTest is not called from within the test itself, the test still executes (because
  //       "ConductorFixture calls it given testWasConducted is false") (pending)
}

package org.scalatest.concurrent

import org.scalatest.fixture.FixtureFunSuite
import org.scalatest.matchers.ShouldMatchers
import _root_.java.util.concurrent.{Callable, CountDownLatch}
import java.lang.Thread.State._

/**
 * @author Josh Cough
 */

class ConductorFixtureSuite extends FixtureFunSuite with ConductorFixture with ShouldMatchers {

  test("metronome order") { conductor => import conductor._

    @volatile var s = ""

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
      s should equal ("ABCDEFGHI") // "Threads were not called in correct order"
    }
  }

  test("wait for tick advances when threads are blocked") { conductor => import conductor._
    var c = new CountDownLatch(3)

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
      c.getCount should equal (1)
      waitForBeat(2) // advances quickly
      c.getCount should equal (1)
      c.countDown()
    }

    whenFinished {
      c.getCount() should equal (0)
    }
  }

  // TODO: t1.getState should (be(WAITING) or be(BLOCKED)) failed with:
  // RUNNABLE was not equal to WAITING, and RUNNABLE was not equal to BLOCKED
  test("wait for beat blocks thread") { conductor => import conductor._

    val t1 = thread {waitForBeat(2)}

    thread {
      waitForBeat(1)
      t1.getState should (be(WAITING) or be(BLOCKED))
    }
  }

  test("thread terminates before finish called") { conductor => import conductor._

    val t1 = thread {1 should equal (1)}
    val t2 = thread {1 should equal (1)}

    whenFinished {
      t1.getState should equal (TERMINATED)
      t2.getState should equal (TERMINATED)
    }
  }

  test("two thread calls return threads that both are in the same thread group") { conductor => import conductor._

    val t1 = thread {waitForBeat(2)}
    val t2 = thread {waitForBeat(2)}

    thread {
      waitForBeat(1)
      t1.getThreadGroup should equal (t2.getThreadGroup)
    }
  }

  test("if a thread call is nested inside another thread call, both threads are in the same thread group") { conductor => import conductor._
    thread {
      val t2 = thread {waitForBeat(2)}
      waitForBeat(1)
      t2.getThreadGroup should equal (currentThread.getThreadGroup)
    }
  }

  test("whenFinished can only be called by thread that created Conductor.") { conductor => import conductor._
    thread {
      intercept[IllegalStateException] {
        whenFinished {1 should equal (1)}
      }.getMessage should equal ("whenFinished can only be called by thread that created Conductor.")
    }
    whenFinished {1 should equal (1)}
  }


  // TODO: I don't understand this test. Josh, can you clarify?
  test("top level thread calls result in a running thread that is blocked such that it doesn't execute " +
       "prior to conductTest being called.") { conductor => import conductor._
    val anotherConductor = new Conductor
    val t = anotherConductor.thread{ 1 should equal (1) }
    thread{ t.getState should equal (WAITING) }
  }

  test("nested thread calls result in a running thread that is allowed to execute immediately") (pending)

  test("runnables are run") { conductor => import conductor._
    val r = new Runnable{
      var runCount = 0
      def run = { runCount+=1 }
    }
    thread(r)
    whenFinished{ r.runCount should equal (1) }
  }

  // Josh, you have been neglecting to synchonize mutable variables shared by multiple
  // threads. You must always synchronize these in some way. This test also failed
  // intermittently. I will make the var volatile so that threads are guaranteed to
  // see each others' changes.
  test("runnables can wait for beats") { conductor => import conductor._
    val r = new Runnable {
      @volatile var runCount = 0
      def run = {
        waitForBeat(1)
        runCount += 1
      }
    }
    thread(r)
    thread{ r.runCount should equal (0) } // This failed even with the volatile. Is there a race condition?
    whenFinished{ r.runCount should equal (1) }
  }

  test("callables are called") { conductor => import conductor._
    val c = new Callable[Unit]{
      var callCount = 0
      def call = { callCount+=1 }
    }
    thread(c)
    whenFinished{ c.callCount should equal (1) }
  }


  // Got a: 1 was not equal to 0 test failure, even with the var
  test("callables can wait for beats") { conductor => import conductor._
    val c = new Callable[Unit]{
      @volatile var callCount = 0
      def call = {
        waitForBeat(1)
        callCount+=1
      }
    }
    thread(c)
    thread{ c.callCount should equal (0) } // This failed even with the volatile. Is there a race condition?
    whenFinished{ c.callCount should equal (1) }
  }

  /////////////////////////////////////////////////

  // One issue is whther ConductorMethods should have a conductTest on it at all. Possibly not, but the trouble
  // is then that Conductor does have this method, and ConductorMethods wouldn't be being completely honest.
  // Also there would be something you could call in ConductorFixture that you couldn't call in ConductorMethods.
  // So my feeling is go ahead and add it to be consistent, but I'm not sure. Please check also to see if there are any
  // other public methods on Conductor that are not in ConductorMethods. If this is the only one, then probably we
  // should add it.

  // TODO: i dont think we should expose conductTest on CM's. it just doesn't make sense.
  // even if it is the only method that isn't exposed (still need to check) if it simply doesnt
  // make sense to the api, and causes problems, then why add it?
  // wed have to do MORE documentation if we add it than if we dont.


  // For ConductorMethods (probably in a ConductorMethodsSuite)
  // TODO: ask bill, dont think we should expose this in CM's
  test("if conductTest is called from within the test itself, the test still succeeds (in other words," +
          "ConductorMethods doesn't call it if testWasConducted is true.") (pending)

  // TODO: ask bill, dont think we should expose this in CM's
  test("if conductTest is not called from within the test itself, the test still executes (because " +
         "ConductorMethods calls it given testWasConducted is false") (pending)

  // TODO: ask bill, dont think we should expose this in CM's
  // ConductorFixture  (probably in a ConductorMethodsSuite)
  test("if conductTest is called from within the test itself, the test still succeeds (in other words," +
          "ConductorFixture doesn't call it if testWasConducted is true.") (pending)

  // TODO: ask bill, dont think we should expose this in CM's
  test("if conductTest is not called from within the test itself, the test still executes (because " +
         "ConductorFixture calls it given testWasConducted is false") (pending)
}
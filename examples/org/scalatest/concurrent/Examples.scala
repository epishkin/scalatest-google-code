package org.scalatest.concurrent

/**
 * Created by IntelliJ IDEA.
 * User: joshcough
 * Date: Jul 12, 2009
 * Time: 10:53:19 PM
 */
package org.scalatest.concurrent

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{TimeUnit, Semaphore, ArrayBlockingQueue}

/**
 *
 */
class ConductorExamples extends FunSuite with ConductorMethods with MustMatchers {
  
  test("call to put on a full queue blocks the producer thread") {
    val buf = new ArrayBlockingQueue[Int](1)

    thread("producer") {
      buf put 42
      buf put 17
      tick mustBe 1
    }

    thread("consumer") {
      waitForTick(1)
      buf.take must be(42)
      buf.take must be(17)
    }

    finish {
      buf must be('empty)
    }
  }

  test("compare and set") {
    val ai = new AtomicInteger(1)

    thread {
      while (!ai.compareAndSet(2, 3)) Thread.`yield`
    }

    thread {
      ai.compareAndSet(1, 2) must be(true)
    }

    finish {
      ai.ge must be(3)
    }
  }

  test("interrupted aquire") {
    val s = new Semaphore(0)

    val nice = thread("nice") {
      intercept[InterruptedException] {s.acquire}
      tick must be(1)
    }

    thread("rude") {
      waitForTick(1)
      nice.interrupt
    }
  }

  test("thread ordering") {
    val ai = new AtomicInteger(0)

    thread {
      ai.compareAndSet(0, 1) must be(true) // S1
      waitForTick(3)
      ai.get() must be(3) // S4
    }

    thread {
      waitForTick(1)
      ai.compareAndSet(1, 2) must be(true) // S2
      waitForTick(3)
      ai.get must be(3) // S4
    }

    thread {
      waitForTick(2)
      ai.compareAndSet(2, 3) must be(true) // S3
    }
  }

  test("timed offer") {
    val q = new ArrayBlockingQueue[String](2)

    val producer = thread("producer") {
      q put "w"
      q put "x"

      withClockFrozen {
        q.offer("y", 25, TimeUnit.MILLISECONDS) mustBe false
      }

      intercept[InterruptedException] {
        q.offer("z", 2500, TimeUnit.MILLISECONDS)
      }

      tick mustBe 1
    }

    val consumer = thread("consumer") {
      waitForTick(1)
      producer.interrupt()
    }

    ()
  }
}

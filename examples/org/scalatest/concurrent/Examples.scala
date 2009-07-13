package org.scalatest.concurrent

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{TimeUnit, Semaphore, ArrayBlockingQueue}
import org.scalatest.matchers.ShouldMatchers

class ConductorExamples extends FunSuite with ConductorMethods with ShouldMatchers {
  
  test("call to put on a full queue blocks the producer thread") {
    val buf = new ArrayBlockingQueue[Int](1)

    thread("producer") {
      buf put 42
      buf put 17
      tick should be (1)
    }

    thread("consumer") {
      waitForTick(1)
      buf.take should be (42)
      buf.take should be (17)
    }

    finish {
      buf should be ('empty)
    }
  }

  test("compare and set") {
    val ai = new AtomicInteger(1)

    thread {
      while (!ai.compareAndSet(2, 3)) Thread.`yield`
    }

    thread {
      ai.compareAndSet(1, 2) should be (true)
    }

    finish {
      ai.get should be (3)
    }
  }

  test("interrupted aquire") {
    val s = new Semaphore(0)

    val nice = thread("nice") {
      intercept[InterruptedException] {s.acquire}
      tick should be (1)
    }

    thread("rude") {
      waitForTick(1)
      nice.interrupt
    }
  }

  test("thread ordering") {
    val ai = new AtomicInteger(0)

    thread {
      ai.compareAndSet(0, 1) should be (true) // S1
      waitForTick(3)
      ai.get() should be (3) // S4
    }

    thread {
      waitForTick(1)
      ai.compareAndSet(1, 2) should be (true) // S2
      waitForTick(3)
      ai.get should be (3) // S4
    }

    thread {
      waitForTick(2)
      ai.compareAndSet(2, 3) should be (true) // S3
    }
  }

  test("timed offer") {
    val q = new ArrayBlockingQueue[String](2)

    val producer = thread("producer") {
      q put "w"
      q put "x"

      withClockFrozen {
        q.offer("y", 25, TimeUnit.MILLISECONDS) should be (false)
      }

      intercept[InterruptedException] {
        q.offer("z", 2500, TimeUnit.MILLISECONDS)
      }

      tick should be (1)
    }

    val consumer = thread("consumer") {
      waitForTick(1)
      producer.interrupt()
    }

    ()
  }
}

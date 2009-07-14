package org.scalatest.concurrent

import org.scalatest.concurrent.PimpedReadWriteLock._
import org.scalatest.matchers.ShouldMatchers

class PimpedReadWriteLockTest extends FunSuite with ConductorMethods with ShouldMatchers {
  val lock = new java.util.concurrent.locks.ReentrantReadWriteLock

  test("demonstrate various functionality") {
    // create 5 named test threads that all do the same thing
    5.threads("reader thread") {
      lock.read {
        logger.debug.around("using read lock") {waitForBeat(2)}
      }
    }

    // create 10 test threads that all do the same thing
    10 threads {
      lock.read {
        logger.debug.around("using read lock") {waitForBeat(2)}
      }
    }

    // create a single, named thread
    thread("writer thread") {
      waitForBeat(1)
      lock.write {
        logger.debug.around("using write lock") {beat should be (2)}
      }
    }
  }
}

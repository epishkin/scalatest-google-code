package org.scalatest.concurrent

import org.scalatest.concurrent.PimpedReadWriteLock._

/**
 * User: joshcough
 * Date: Jul 12, 2009
 * Time: 10:55:54 PM
 */
class PimpedReadWriteLockTest extends FunSuite with ConductorMethods with MustMatchers {
  val lock = new java.util.concurrent.locks.ReentrantReadWriteLock

  test("demonstrate various functionality") {
    // create 5 named test threads that all do the same thing
    5.threads("reader thread") {
      lock.read {
        logger.debug.around("using read lock") {waitForTick(2)}
      }
    }

    // create 10 test threads that all do the same thing
    10 threads {
      lock.read {
        logger.debug.around("using read lock") {waitForTick(2)}
      }
    }

    // create a single, named thread
    thread("writer thread") {
      waitForTick(1)
      lock.write {
        logger.debug.around("using write lock") {tick mustBe 2}
      }
    }
  }
}

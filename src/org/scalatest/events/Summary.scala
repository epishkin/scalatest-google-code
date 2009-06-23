package org.scalatest.events

/**
 * Class each of whose instances hold summary information about one ScalaTest run.
 *
 * @param testsSucceededCount the number of tests that were reported as succeeded during the run
 * @param testsFailedCount the number of tests that were reported as failed during the run
 * @param testsIgnoredCount the number of tests that were were reported as ignored during the run
 * @param testsPendingCount the number of tests that were reported as pending during the run
 */
final case class Summary(testsSucceededCount: Int, testsFailedCount: Int, testsIgnoredCount: Int, testsPendingCount: Int,
  suitesCompletedCount: Int, suitesAbortedCount: Int) {

  /**
   * The number of tests completed, which is the sum of the number of tests that succeeded and failed, excluding any
   * tests that were ignored or reported as pending.
   */
  val testsCompletedCount = testsSucceededCount + testsFailedCount
}

/**
 * Companion object for case class <a href="Summary.html"><code>Summary</code></a>.
 */
object Summary

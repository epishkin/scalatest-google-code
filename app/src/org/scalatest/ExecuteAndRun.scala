package org.scalatest

/**
 * Trait that abstracts out the <code>execute</code> and <code>runTest</code> methods of <code>Suite</code>. This
 * trait exists to support the use of trait <code>BeforeAndAfter</code>, which is a direct subtrait of this trait. The
 * <code>BeforeAndAfter</code> trait's implementation of <code>runTest</code> surrounds an invocation of
 * <code>super.runTest</code> in calls to <code>beforeEach</code> and <code>afterEach</code>. Similarly, the
 * <code>BeforeAndAfter</code> trait's implementation of <code>execute</code> surrounds an invocation of
 * <code>super.execute</code> in calls to <code>beforeAll</code> and <code>afterAll</code>. This enables trait
 * <code>BeforeAndAfter</code> to be mixed into any <code>Suite</code>, but not into an <code>Examples</code>.
 *
 * <p>
 * The main purpose of <code>ExecuteAndRun</code> is to render a compiler error the mistake of attempting
 * to mix <code>BeforeAndAfter</code> into an <code>Examples</code>. If <code>BeforeAndAfter</code> extended
 * </code>Suite</code> itself, then it would compile if mixed into an <code>Examples</code>, but wouldn't work
 * as expected (i.e., the <code>beforeEach</code> and <code>afterEach</code> methods would never be called).
 * This goal can't be achieved by making the self type of <code>BeforeAndAfter</code> <code>Suite</code>, because
 * in that case <code>BeforeAndAfter</code> couldn't wrap calls to the mixed into <code>Suite</code>,
 * given <code>Suite</code> would not be <code>super</code>, and calling <code>execute</code> on <code>this</code>
 * would result in infinite recursion.
 * </p>
 *
 * @author Bill Venners
 */
trait ExecuteAndRun {

  /**
   * Execute this <code>ExecuteAndRun</code>.
   *
   * @param testName an optional name of one test to execute. If <code>None</code>, all relevant tests should be executed.
   *                 I.e., <code>None</code> acts like a wildcard that means execute all relevant tests in this <code>Suite</code>.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param groupsToInclude a <code>Set</code> of <code>String</code> test names to include in the execution of this <code>Suite</code>
   * @param groupsToExclude a <code>Set</code> of <code>String</code> test names to exclude in the execution of this <code>Suite</code>
   * @param goodies a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @param distributor an optional <code>Distributor</code>, into which to put nested <code>Suite</code>s to be executed
   *              by another entity, such as concurrently by a pool of threads. If <code>None</code>, nested <code>Suite</code>s will be executed sequentially.
   * @throws NullPointerException if any passed parameter is <code>null</code>.
   */
  def execute(
    testName: Option[String],
    reporter: Reporter,
    stopper: Stopper,
    groupsToInclude: Set[String],
    groupsToExclude: Set[String],
    goodies: Map[String, Any],
    distributor: Option[Distributor]
  )

  /**
   * Run a test.
   *
   * @param testName the name of one test to execute.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param goodies a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, or <code>goodies</code>
   *     is <code>null</code>.
   */
  protected def runTest(
    testName: String,
    reporter: Reporter,
    stopper: Stopper,
    goodies: Map[String, Any]
  )
}

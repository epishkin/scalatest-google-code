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

/**
 * Trait that abstracts out the <code>run</code>, <code>runNestedSuites</code>, <code>runTests</code>, and <code>runTest</code>
 * methods of <code>Suite</code>. This
 * trait exists primarily to support the use of traits <code>BeforeAndAfterEach</code> and <code>BeforeAndAfterAll</code>, which are
 * direct subtraits of this trait. The
 * <code>BeforeAndAfterEach</code> trait's implementation of <code>runTest</code> surrounds an invocation of
 * <code>super.runTest</code> in calls to <code>beforeEach</code> and <code>afterEach</code>. Similarly, the
 * <code>BeforeAndAfterAll</code> trait's implementation of <code>run</code> surrounds an invocation of
 * <code>super.run</code> in calls to <code>beforeAll</code> and <code>afterAll</code>. This enables traits
 * <code>BeforeAndAfterEach</code> and <code>BeforeAndAfterAll</code> to be mixed into any <code>Suite</code>, but not
 * into a trait that contains shared tests (See, for example, documentation about <a href="FunSuite.html#SharedTests">shared tests</a>
 * in trait <code>FunSuite</code>).
 *
 * <p>
 * The main purpose of <code>RunMethods</code> is to render a compiler error any attempt
 * to mix <code>BeforeAndAfterEach</code> or <code>BeforeAndAfterAll</code> into a trait containing shared tests. (An example of such a trait is the
 * <a href="FunSuite.html#SharedTests"><code>StackBehaviors</code></a> trait shown in the documentation for <code>FunSuite</code>.)
 * If <code>BeforeAndAfterEach</code> extended
 * </code>Suite</code> itself, then it would compile if mixed into such a trait, but might not work
 * as expected. A user might reasonable think that the <code>beforeEach</code> and <code>afterEach</code> methods
 * would be called only before the shared tests in the trait that mixes in <code>BeforeAndAfterEach</code>, but they would
 * actually be called before all tests of the suite class the trait gets mixed into. And if that user tried to mix into the same suite
 * another shared tests trait that also mixed in <code>BeforeAndAfterEach</code>, the result would compile, but very likely
 * not work as expected. Unless the <code>beforeEach</code> and <code>afterEach</code> methods all delegate to the superclass, with
 * <code>super.beforeEach</code> for example, only the methods defined in the last trait mixed in would actually be executed. Even if
 * superclass delegation were used, each <code>beforeEach</code> and <code>afterEach</code> method would be called once for each
 * test in the entire suite.
 * </p>
 *
 * </p>
 * The goal of making <code>BeforeAndAfterEach</code> incompatible with shared tests traits can't be achieved solely by making the
 * self type of <code>BeforeAndAfterEach</code> <code>Suite</code>, because
 * in that case <code>BeforeAndAfterEach</code> couldn't wrap calls to the mixed into <code>Suite</code>,
 * given <code>Suite</code> would not be <code>super</code>. The two run methods not called by either <code>BeforeAndAfterEach</code>
 * or <code>BeforeAndAfterEach</code>, <code>runNestedSuites</code>, and <code>runTests</code>, are included in this trait both for
 * completeness and also to enable other traits that override these methods to be made incompatible with shared test traits. For example, 
 * <code>SequentialNestedSuiteExecution</code> extends <code>RunMethods</code> instead of <code>Suite</code> so that
 * it doesn't end up mixed into shared tests traits.
 * </p>
 *
 * @author Bill Venners
 */
trait RunMethods { this: Suite =>

  /**
   * Execute this suite of tests.
   *
   * @param testName an optional name of one test to execute. If <code>None</code>, all relevant tests should be executed.
   *                 I.e., <code>None</code> acts like a wildcard that means execute all relevant tests in this <code>Suite</code>.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param filter a <code>Filter</code> with which to filter tests based on their tags
   * @param configMap a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @param distributor an optional <code>Distributor</code>, into which to put nested <code>Suite</code>s to be executed
   *              by another entity, such as concurrently by a pool of threads. If <code>None</code>, nested <code>Suite</code>s will be executed sequentially.
   * @param tracker a <code>Tracker</code> tracking <code>Ordinal</code>s being fired by the current thread.
   *
   * @throws NullPointerException if any passed parameter is <code>null</code>.
   */
  def run(
    testName: Option[String],
    reporter: Reporter,
    stopper: Stopper,
    filter: Filter,
    configMap: Map[String, Any],
    distributor: Option[Distributor],
    tracker: Tracker
  )

  /**
   *
   * Run zero to many of this suite's nested suites.
   *
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param filter a <code>Filter</code> with which to filter tests based on their tags
   * @param configMap a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @param distributor an optional <code>Distributor</code>, into which to put nested <code>Suite</code>s to be run
   *              by another entity, such as concurrently by a pool of threads. If <code>None</code>, nested <code>Suite</code>s will be run sequentially.
   * @param tracker a <code>Tracker</code> tracking <code>Ordinal</code>s being fired by the current thread.
   *         
   * @throws NullPointerException if any passed parameter is <code>null</code>.
   */
  protected def runNestedSuites(reporter: Reporter, stopper: Stopper, filter: Filter,
                                configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker)

  /**
   * Run zero to many of this suite's tests.
   *
   * @param testName an optional name of one test to run. If <code>None</code>, all relevant tests should be run.
   *                 I.e., <code>None</code> acts like a wildcard that means run all relevant tests in this <code>Suite</code>.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param filter a <code>Filter</code> with which to filter tests based on their tags
   * @param configMap a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @param distributor an optional <code>Distributor</code>, into which instances of this <code>Suite</code> class
   *              that are responsible for executing individual tests contained in this </code>Suite</code>, or groups of this <code>Suite</code>'s
   *              tests, may be placed so as to be run
   *              by another entity, such as concurrently by a pool of threads.
   * @param tracker a <code>Tracker</code> tracking <code>Ordinal</code>s being fired by the current thread.
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, <code>groupsToInclude</code>,
   *     <code>groupsToExclude</code>, or <code>configMap</code> is <code>null</code>.
   */
  protected def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
                             configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker)

  /**
   * Run a test.
   *
   * @param testName the name of one test to execute.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param configMap a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @param tracker a <code>Tracker</code> tracking <code>Ordinal</code>s being fired by the current thread.
   *
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, <code>configMap</code>,
   *     or <code>tracker</code> is <code>null</code>.
   */
  protected def runTest(
    testName: String,
    reporter: Reporter,
    stopper: Stopper,
    configMap: Map[String, Any],
    tracker: Tracker
  )
}

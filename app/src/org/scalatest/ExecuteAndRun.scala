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

import org.scalatest.events.Ordinal

/**
 * Trait that abstracts out the <code>run</code> and <code>runTest</code> methods of <code>Suite</code>. This
 * trait exists to support the use of trait <code>BeforeAndAfter</code>, which is a direct subtrait of this trait. The
 * <code>BeforeAndAfter</code> trait's implementation of <code>runTest</code> surrounds an invocation of
 * <code>super.runTest</code> in calls to <code>beforeEach</code> and <code>afterEach</code>. Similarly, the
 * <code>BeforeAndAfter</code> trait's implementation of <code>run</code> surrounds an invocation of
 * <code>super.run</code> in calls to <code>beforeAll</code> and <code>afterAll</code>. This enables trait
 * <code>BeforeAndAfter</code> to be mixed into any <code>Suite</code>, but not into a trait that contains shared tests
 * (See documentation for trait <a href="SharedTests.html"><code>SharedTests</code></a>).
 *
 * <p>
 * The main purpose of <code>TwoRunMethods</code> is to render a compiler error any attempt
 * to mix <code>BeforeAndAfter</code> into a trait containing shared tests. (An example of such a trait is the
 * <a href="SharedTests.html#StackBehaviors"><code>StackBehaviors</code></a> trait shown in the documentation for <code>SharedTests</code>.)
 * If <code>BeforeAndAfter</code> extended
 * </code>Suite</code> itself, then it would compile if mixed into such a trait, but might not work
 * as expected. A user might reasonable think that the <code>beforeEach</code> and <code>afterEach</code> methods
 * would be called only before the shared tests in the trait that mixes in <code>BeforeAndAfter</code>, but they would
 * actually be called before all tests of the suite class the trait gets mixed into. And if that user tried to mix into the same suite
 * another shared tests trait that also mixed in <code>BeforeAndAfter</code>, the result would compile, but very likely
 * not work as expected. Unless the <code>beforeEach</code> and <code>afterEach</code> methods all delegate to the superclass, with
 * <code>super.beforeEach</code> for example, only the methods defined in the last trait mixed in would actually be executed. Even if
 * superclass delegation were used, each <code>beforeEach</code> and <code>afterEach</code> method would be called once for each
 * test in the entire suite.
 * </p>
 *
 * </p>
 * The goal of making <code>BeforeAndAfter</code> incompatible with shared tests traits can't be achieved solely by making the
 * self type of <code>BeforeAndAfter</code> <code>Suite</code>, because
 * in that case <code>BeforeAndAfter</code> couldn't wrap calls to the mixed into <code>Suite</code>,
 * given <code>Suite</code> would not be <code>super</code>.
 * </p>
 *
 * @author Bill Venners
 */
trait TwoRunMethods { this: Suite =>

  /**
   * Run this suite of tests.
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
   * @param tracker a <code>Tracker</code> tracking <code>Ordinal</code>s being fired by the current thread.
   *
   * @throws NullPointerException if any passed parameter is <code>null</code>.
   */
  def run(
    testName: Option[String],
    reporter: Reporter,
    stopper: Stopper,
    groupsToInclude: Set[String],
    groupsToExclude: Set[String],
    goodies: Map[String, Any],
    distributor: Option[Distributor],
    tracker: Tracker
  )

  /**
   * Run a test.
   *
   * @param testName the name of one test to execute.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param goodies a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @param tracker a <code>Tracker</code> tracking <code>Ordinal</code>s being fired by the current thread.
   *
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, <code>goodies</code>,
   *     or <code>tracker</code> is <code>null</code>.
   */
  protected def runTest(
    testName: String,
    reporter: Reporter,
    stopper: Stopper,
    goodies: Map[String, Any],
    tracker: Tracker
  )
}

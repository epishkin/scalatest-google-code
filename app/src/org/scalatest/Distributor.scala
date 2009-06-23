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
 * Function trait whose instances facilitate concurrent and/or distributed execution of <code>Suite</code>s.
 * An optional <code>DistributeFunction</code> is passed to the <code>run</code> method of <code>Suite</code>. If a
 * <code>DistributeFunction</code> is indeed passed, trait <code>Suite</code>'s implementation of <code>run</code> will
 * populate that <code>DistributeFunction</code> with its nested <code>Suite</code>s (by passing them to the <code>DistributeFunctions</code>'s
 * <code>apply</code> method) rather than executing the nested <code>Suite</code>s directly. It is then up to another party or parties
 * to execute those <code>Suite</code>s.
 *
 * <p>
 * If you have a set of nested <code>Suite</code>s that must be executed sequentially, mix in trait
 * <code>SequentialNestedSuiteExecution</code>, which overrides <code>runNestedSuites</code> and
 * calls <code>super</code>'s <code>runNestedSuites</code> implementation, passing in <code>None</code> for the
 * <code>Distributor</code>. For example:
 * </p>
 * 
 * <p>
 * Implementations of this trait must be thread safe.
 * </p>
 *
 * @author Bill Venners
 */
trait Distributor extends ((Suite, Tracker) => Unit) {

  /**
   * Puts a <code>Suite</code> into the <code>Distributor</code>.
   *
   * @param suite the <code>Suite</code> to put into the <code>Distributor</code>.
   * @param tracker a <code>Tracker</code> to pass to the <code>Suite</code>'s <code>run</code> method.
   *
   * @throws NullPointerException if either <code>suite</code> or <code>tracker</code> is <code>null</code>.
   */
  override def apply(suite: Suite, tracker: Tracker)
}

/*
   Could make this a function too. Would simply be (Suite) => Unit. Could name the parameter stopRequested
   Then the code would be:

   distribute(suite)

   instead of:

   distributor.put(suite)

   or, could bump it up to an execution strategy

   execute(suite)

   I don't think so.

  Could rename it DistributeFunction instead of Distributor, so they'd write:
  distribute: DistributeFunction

  Could I call it DistributeFun? or Distribute
  distribute: DistributeFun

  I use Fun elsewhere. In FunSuite. So maybe.

  The distribute method needs the ordinal too:
  trait DistributeFunction {

    def apply(suite: Suite, nextOrdinal: Ordinal)
  }

 I wonder if I could call these Stopper, Distributor, Reporter, etc., then make type aliases for Suite.
 val type ReportFunction = Reporter
 val type DistributeFunction = Distributor
 val type FilterFunction = Filter
 val type Function = Filter

  Maybe just leave the names the same and make the names verbs:
  testName: Option[String], report: Reporter, stopRequested: Stopper, filter: Filter, distributor: Option[Distributor], goodies: Map[String, Set[String]]

  Calling code is ugly. Looks like report=, instead of reporter= But really, this is the framework doing this usually, and you don't have to use that param notation.
  Frankly, I think it should be for inside the method. Though that's wierd. It should be for users, and here, the users are implementing this method, not calling it.
  distributor is a noun, whereas report, stopRequested, filter are ready to go verby functionnames, because distributor is optional
  distributor match {
    case Some(distribute) => distribute(suite, nextOrdinal)
    case None => bla bla bla
  }

  Can have a DisposableReporter with a dispose() method. Can then just do some pattern matching when the time comes to dispose. That way the parameter type can have
  the strong name of Reporter.
*/

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
 * <p>
 * A object that facilitates concurrent and/or distributed execution of <code>Suite</code>s.
 * An optional <code>Distributor</code> is passed to the <code>execute</code> method of <code>Suite</code>. If a
 * <code>Distributor</code> is indeed passed, trait <code>Suite</code>'s implementation of <code>execute</code> will
 * populate that <code>Distributor</code> with its nested <code>Suite</code>s (by passing them to the <code>Distributor</code>'s
 * <code>put</code> method) rather than executing the nested <code>Suite</code>s directly. It is then up to another party or parties
 * to execute those <code>Suite</code>s.
 * </p>
 *
 * <p>
 * If you have a set of nested <code>Suite</code>s that must be executed sequentially, you can override <code>runNestedSuites</code> and
 * call this trait's implementation, passing in <code>None</code> for the <code>Distributor</code>. For example:
 * </p>
 * 
 * <pre>
 * override protected def runNestedSuites(reporter: Reporter, stopper: Stopper,
 *     includes: Set[String], excludes: Set[String], properties: Map[String, Any],
 *     puttable: Option[Distributor]) {
 *
 *   // Execute nested suites sequentially
 *   super.runNestedSuites(reporter, stopper, includes, excludes, properties, None)
 * }
 * </pre>
 *
 * <p>
 * Implementations of this trait must be thread safe.
 * </p>
 *
 * @author Bill Venners
 */
trait Distributor {

  /**
   * Puts a <code>Suite</code> into the <code>Distributor</code>.
   *
   * @param suite the <code>Suite</code> to put into the <code>Distributor</code>.
   *
   * @throws NullPointerException if <code>suite</code> is <code>null</code>.
   */
  def put(suite: Suite)
}

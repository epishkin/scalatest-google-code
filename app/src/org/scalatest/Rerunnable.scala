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
 * Trait whose instances can rerun tests or other entities (such as suites). An object extending
 * this trait can be passed to a <code>Reporter</code> as part of a <code>Report</code>. The
 * test or other entity about which the report is made can then be rerun by invoking the
 * <code>rerun</code> method on the <code>Rerunnable</code>.
 *
 * @author Bill Venners
 */
trait Rerunnable {

  // DEPRECATE
  /**
   * <strong>Deprecated: this method will be deleted in a future release of ScalaTest. Please rewrite any Rerunnable you may have
   * so that it only has the rerun form that takes a runStamp, and inherits the default implementation of this rerun method, which forwards
   * to the other one.</strong> Rerun a test or other entity (such as a suite), reporting results to the specified <code>Reporter</code>.
   *
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param loader the <code>ClassLoader</code> from which to load classes needed to rerun
   *     the test or other entity.
   * @throws NullPointerException if <CODE>reporter</CODE> is <CODE>null</CODE>.
   */
  @deprecated def rerun(reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
            properties: Map[String, Any], distributor: Option[Distributor], loader: ClassLoader) {
    rerun(reporter, stopper, includes, excludes, properties, distributor, loader, 99)
  }

  /**
   * Rerun a test or other entity (such as a suite), reporting results to the specified <code>Reporter</code>.
   *
   * <strong>Note: After the deprecation period, the other form of rerun will be removed from this trait, and this
   * form will be made abstract. Currently it just forwards to the old, deprecated rerun form as its default implementation, so 
   * existing <code>Rerunnable</code>s can simply be recompiled and continue to work during the deprecation period.</strong>
   *
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param loader the <code>ClassLoader</code> from which to load classes needed to rerun
   *     the test or other entity.
   * @throws NullPointerException if <CODE>reporter</CODE> is <CODE>null</CODE>.
   */
  def rerun(reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
            properties: Map[String, Any], distributor: Option[Distributor], loader: ClassLoader, runStamp: Int) {
    rerun(reporter, stopper, includes, excludes, properties, distributor, loader)
  }
}

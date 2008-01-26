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

      /**
       * Rerun a test or other entity (such as a suite), reporting results to the specified <code>Reporter</code>.
       *
       * @param reporter the <code>Reporter</code> to which results will be reported
       * @param loader the <code>ClassLoader</code> from which to load classes needed to rerun
       *     the test or other entity.
       * @throws NullPointerException if <CODE>reporter</CODE> is <CODE>null</CODE>.
       */
      def rerun(reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
                properties: Map[String, Any], distributor: Option[Distributor], loader: ClassLoader): Unit
}

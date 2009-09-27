/*
 * Copyright 2001-2009 Artima, Inc.
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
package org.scalatest.verb

import org.scalatest._

/**
 * Supports the shorthand form of shared test registration.
 *
 * <p>
 * For example, this trait enables syntax such as the following in <code>FlatSpec</code>
 * and <code>FixtureFlatSpec</code>:
 * </p>
 *
 * <pre>
 * "A Stack (with one item)" should behave like nonEmptyStack(stackWithOneItem, lastValuePushed)
 *                                         ^
 * </pre>
 *
 * <p>
 * This type is returned by the function passed as an implicit parameter to a <code>should</code> method
 * provided in <code>ShouldVerb</code>, a <code>must</code> method
 * provided in <code>MustVerb</code>, and a <code>can</code> method
 * provided in <code>CanVerb</code>. The implicit function parameter is provided by <code>FlatSpec</code>
 * and <code>FixtureFlatSpec</code>.
 * </p>
 */
trait ResultOfBehaveWordPassedToVerb {

  /**
   * Supports the shorthand form of shared test registration.
   *
   * <p>
   * For example, this method enables syntax such as the following in <code>FlatSpec</code>
   * and <code>FixtureFlatSpec</code>:
   * </p>
   *
   * <pre>
   * "A Stack (with one item)" should behave like nonEmptyStack(stackWithOneItem, lastValuePushed)
   *                                         ^
   * </pre>
   *
   */
  def like(unit: Unit)
}

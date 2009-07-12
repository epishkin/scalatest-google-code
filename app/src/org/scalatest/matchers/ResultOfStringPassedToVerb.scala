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
package org.scalatest.matchers

/**
 * Trait used to enable  syntax such as the following in a <code>FlatSpec</code> that
 * mixes in <code>ShouldMatchers</code>:
 *
 * <pre>
 * "A Stack (when empty)" should "be empty" in {
 *   // ...
 * }
 * </pre>
 *
 * Or syntax such as the following in a <code>FlatSpec</code> that
 * mixes in <code>MustMatchers</code>:
 *
 * <pre>
 * "A Stack (when empty)" must "be empty" in {
 *   // ...
 * }
 * </pre>
 *
 * <p>
 * This trait is the result type of the <code>should</code> method in trait
 * <code>ShouldMatcher</code>'s <code>ShouldStringWrapper</code> class and the result
 * type of the <code>must</code> method in <code>MustMatchers</code>'s <code>MustStringWrapper</code> class.
 * <code>FlatSpec</code> passes in an implicit function to that <code>should</code> (or <code>must</code>) method
 * that takes three strings and results in  a <code>ResultOfStringPassedToVerb</code>.
 * The <code>should</code> (or <code>must</code>) method simply invokes the passed function,
 * passing in left, right, and the verb string <code>"should"</code> (or <code>"must"</code>). The result of the
 * function application becomes the result of the <code>should</code> (or <code>"must"</code>) method, which
 * then means any of this trait's methods, such as <code>in</code>, can be invoked next.
 * </p>
 */
trait ResultOfStringPassedToVerb[Fixture] {

  /**
   * Register the test function passed as <code>testFun</code> in a <code>FlatSpec</code>.
   *
   * <p>
   * This method enables the following syntax in a <code>FlatSpec</code> that mixes in <code>ShouldMatchers</code>:
   * </p>
   *
   * <pre>
   * "A Stack (when empty)" should "be empty" in { /* ... */ }
   *                                          ^
   * </pre>
   *
   * <p>
   * And this method enables the following syntax in a <code>FlatSpec</code> that mixes in <code>MustMatchers</code>:
   * </p>
   *
   * <pre>
   * "A Stack (when empty)" must "be empty" in { /* ... */ }
   *                                          ^
   * </pre>
   */
  def in(testFun: => Unit)

  def in(testFun: Fixture => Unit)
}

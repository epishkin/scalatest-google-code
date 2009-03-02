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
package org.scalatest.matchers

/**
 * <h2>Writing custom <code>BeMatchers</code></h2>
 *
 * If you want to create a new way of using <code>be</code>, which doesn't map to an actual property on the
 * type you care about, you can create a <code>BeMatcher</code>. You could use this, for example, to create <code>BeMatcher[Int]</code>
 * called <code>odd</code>, which would match any odd <code>Int</code>, and <code>even</code>, which would match
 * any even <code>Int</code>. 
 * Given this pair of <code>BeMatcher</code>s, you could check whether an <code>Int</code> was odd or even with expressions like:
 * </p>
 *
 * <pre class="indent">
 * num should be (odd)
 * num should not be (even)
 * </pre>
 *
 * For more information, see the documentation for <a href="BeMatcher.html"><code>BeMatcher</code></a>.
*/
trait BeMatcher[-T] extends Function1[T, MatchResult] {

  /**
   * TODO
   */
  def apply(left: T): MatchResult
}


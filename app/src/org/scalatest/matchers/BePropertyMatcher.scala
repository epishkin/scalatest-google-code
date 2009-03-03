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

// T is the type of the object that has a Boolean property to verify with an instance of this trait
// This is not a subtype of BeMatcher, because BeMatcher only works after "be", but 
// BePropertyMatcher will work after "be", "be a", or "be an"
/**
 * Trait extended by matcher objects, which may appear after the word <code>be</code>, that can match against a <code>Boolean</code>
 * property. The match will succeed if the <code>Boolean</code> property equals <code>true</code>, else it will fail.
 * The object containing the property, which must be of the type specifid by the <code>BePropertyMatcher</code>'s type
 * parameter <code>T</code>, is passed to the <code>BePropertyMatcher</code>'s
 * <code>apply</code> method. The result is a <code>BePropertyMatchResult</code>.
 * A <code>BePropertyMatcher</code> is, therefore, a function from the specified type, <code>T</code>, to
 * a <code>BePropertyMatchResult</code>.
 *
 * <p>
 * Although <code>BePropertyMatcher</code>
 * and <code>Matcher</code> represent very similar concepts, they have no inheritance relationship
 * because <code>Matcher</code> is intended for use right after <code>should</code> or <code>must</code>
 * whereas <code>BeMatcher</code> is intended for use right after <code>be</code>.
 * </p>
 *
 * <p>
 * As an example, you could create <code>BeMatcher[Int]</code>
 * called <code>odd</code> that would match any odd <code>Int</code>, and one called <code>even</code> that would match
 * any even <code>Int</code>. 
 * Given this pair of <code>BeMatcher</code>s, you could check whether an <code>Int</code> was odd or even with expressions like:
 * </p>
 *
 * <pre class="indent">
 * num should be (odd)
 * num should not be (even)
 * </pre>
 *
 * <p>
 * Here's is how you might define the odd and even <code>BeMatchers</code>:
 * </p>
 * 
 * <pre>
 * trait CustomMatchers {
 *
 *   class OddMatcher extends BeMatcher[Int] {
 *     def apply(left: Int) =
 *       MatchResult(
 *         left % 2 == 1,
 *         left.toString + " was even",
 *         left.toString + " was odd"
 *       )
 *   }
 *   val odd = new OddMatcher
 *   val even = not (odd)
 * }
 *
 * // Make them easy to import with:
 * // import CustomMatchers._
 * object CustomMatchers extends CustomMatchers
 * </pre>
 *
 * <p>
 * These <code>BeMatcher</code>s are defined inside a trait to make them easy to mix into any
 * suite or spec that needs them.
 * The <code>CustomMatchers</code> companion object exists to make it easy to bring the
 * <code>BeMatcher</code>s defined in this trait into scope via importing, instead of mixing in the trait. The ability
 * to import them is useful, for example, when you want to use the matchers defined in a trait in the Scala interpreter console.
 * </p>
 *
 * <p>
 * Here's an rather contrived example of how you might use <code>odd</code> and <code>even</code>: 
 * </p>
 *
 * <pre>
 * class DoubleYourPleasureSuite extends FunSuite with MustMatchers with CustomMatchers {
 *
 *   def doubleYourPleasure(i: Int): Int = i * 2
 *
 *   test("The doubleYourPleasure method must return proper odd or even values")
 *
 *     val evenNum = 2
 *     evenNum must be (even)
 *     doubleYourPleasure(evenNum) must be (even)
 *
 *     val oddNum = 3
 *     oddNum must be (odd)
 *     doubleYourPleasure(oddNum) must be (odd) // This will fail
 *   }
 * }
 * </pre>
 *
 * <p>
 * The last assertion in the above test will fail with this failure message:
 * </p>
 *
 * <pre>
 * 6 was even
 * </pre>
 *
 * <p>
 * For more information on <code>MatchResult</code> and the meaning of its fields, please
 * see the documentation for <a href="MatchResult.html"><code>MatchResult</code></a>. To understand why <code>BeMatcher</code>
 * is contravariant in its type parameter, see the section entitled "Matcher's variance" in the
 * documentation for <a href="Matcher.html"><code>Matcher</code></a>.
 * </p>
 *
 * @author Bill Venners
*/
trait BePropertyMatcher[-T] extends Function1[T, BePropertyMatchResult] {
  def apply(objectWithProperty: T): BePropertyMatchResult
}


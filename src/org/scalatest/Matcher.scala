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
 * Trait extended by objects that can match a value of the specified type. The value to match is
 * passed to the matcher's <code>apply</code> method. The result is a <code>MatcherResult</code>.
 * A matcher is, therefore, a function from the specified type, <code>T</code>, to a <code>MatcherResult</code>.
 *
 * <p>
 * <code>Matcher</code> is contravariant in its type parameter, <code>T</code>, to make its use more flexible.
 * As an example, consider the hierarchy:
 * </p>
 *
 * <pre>
 * class Fruit
 * class Orange extends Fruit
 * class ValenciaOrange extends Orange
 * </pre>
 *
 * <p>
 * Given an orange:
 * </p>
 *
 * <pre>
 * val orange = Orange
 * </pre>
 *
 * <p>
 * The expression "<code>orange should</code>" will, via an implicit conversion in <code>ShouldMatchers</code>,
 * result in an object that has a <code>should</code>
 * method that takes a <code>Matcher[Orange]</code>. If the static type of the matcher being passed to <code>should</code> is
 * <code>Matcher[Valencia]</code> it shouldn't (and won't) compile. The reason it shouldn't compile is that
 * the left value is an <code>Orange</code>, but not necessarily a <code>Valencia</code>, and a
 * <code>Matcher[Valencia]</code> only knows how to match against a <code>Valencia</code>. The reason
 * it won't compile is given that <code>Matcher</code> is contravariant in its type parameter, <code>T</code>, a
 * <code>Matcher[Valencia]</code> is <em>not</em> a subtype of <code>Matcher[Orange]</code>.
 * </p>
 *
 * <p>
 * By contrast, if the static type of the matcher being passed to <code>should</code> is <code>Matcher[Fruit]</code>,
 * it should (and will) compile. The reason is should compile is that given the left value is an <code>Orange</code>,
 * it is also a <code>Fruit</code>, and a <code>Matcher[Fruit]</code> knows how to match against <code>Fruit</code>s.
 * The reason it will compile is that given  that <code>Matcher</code> is contravariant in its type parameter, <code>T</code>, a
 * <code>Matcher[Fruit]</code> is indeed a subtype of <code>Matcher[Orange]</code>.
 * </p>
 *
 */
trait Matcher[-T] extends Function1[T, MatcherResult] { leftMatcher =>

  // TODO: to make these nice, I think I'll have to put everything that's after should or must also
  // after not, or, and and
  /**
   * Check to see if the specified object, <code>left</code>, matches, and report the result in
   * the returned <code>MatcherResult</code>. The parameter is named <code>left</code>, because it is
   * usually the value to the left of a <code>should</code> or <code>must</code> invocation. For example,
   * in:
   *
   * <pre>
   * list should equal (List(1, 2, 3))
   * </pre>
   *
   * The <code>equal (List(1, 2, 3))</code> expression returns a matcher that holds a reference to the
   * right value, <code>List(1, 2, 3)</code>. The <code>should</code> method invokes <code>apply</code>
   * on this matcher, passing in <code>list</code>, which is therefore the "<code>left</code>" value. The
   * matcher will compare the <code>list</code> (the <code>left</code> value) with <code>List(1, 2, 3)</code> (the right
   * value), and report the result in the returned <code>MatcherResult</code>.
   *
   * @param left the value against which to match
   * @return the <code>MatcherResult</code> that represents the result of the match
   */
  def apply(left: T): MatcherResult

  // left is generally the object on which should is invoked. leftMatcher
  // is the left operand to and. For example, in:
  // cat should { haveLives (9) and landOn (feet) }
  // left is 'cat' and leftMatcher is the matcher produced by 'haveLives (9)'.
  // rightMatcher, by the way, is the matcher produced by 'landOn (feet)'

  /**
   * Returns a matcher whose <code>apply</code> method returns a <code>MatcherResult</code>
   * that represents the logical-and of the results of this and the passed matcher applied to
   * the same value.
   *
   * <p>
   * The reason <code>and</code> has an upper bound on its type parameter is so that the <code>Matcher</code>
   * resulting from an invocation of <code>and</code> will have the correct type parameter. If you call
   * <code>and</code> on a <code>Matcher[Orange]</code>, passing in a <code>Matcher[Valencia]</code>,
   * the result will have type <code>Matcher[Valencia]</code>. This is correct because both a
   * <code>Matcher[Orange]</code> and a <code>Matcher[Valencia]</code> know how to match a
   * <code>Valencia</code> (but a <code>Matcher[Valencia]</code> doesn't know how to
   * match any old <code>Orange</code>).  If you call
   * <code>and</code> on a <code>Matcher[Orange]</code>, passing in a <code>Matcher[Fruit]</code>,
   * the result will have type <code>Matcher[Orange]</code>. This is also correct because both a
   * <code>Matcher[Orange]</code> and a <code>Matcher[Fruit]</code> know how to match an
   * <code>Orange</code> (but a <code>Matcher[Orange]</code> doesn't know how to
   * match any old <code>Fruit</code>).
   * </p>
   *
   * @param the matcher to logical-and with this matcher
   * @return a matcher that performs the logical-and of this and the passed matcher 
   */
  def and[U <: T](rightMatcher: => Matcher[U]): Matcher[U] =
    new Matcher[U] {
      def apply(left: U) = {
        val leftMatcherResult = leftMatcher(left)
        if (!leftMatcherResult.matches)
          MatcherResult(
            false,
            leftMatcherResult.failureMessage,
            leftMatcherResult.negativeFailureMessage
          )
        else {
          val rightMatcherResult = rightMatcher(left)
          MatcherResult(
            rightMatcherResult.matches,
            Resources("commaBut", leftMatcherResult.negativeFailureMessage, rightMatcherResult.failureMessage),
            Resources("commaAnd", leftMatcherResult.negativeFailureMessage, rightMatcherResult.negativeFailureMessage)
          )
        }
      }
    }

  // def &&[U <: T](rightMatcher: => Matcher[U]): Matcher[U] = and(rightMatcher)

  // Dropping to eliminate redundancy. Redundant with and not { ... }
  // def andNot[U <: T](rightMatcher: => Matcher[U]): Matcher[U] = leftMatcher and Helper.not { rightMatcher }

  /**
   * Returns a matcher whose <code>apply</code> method returns a <code>MatcherResult</code>
   * that represents the logical-or of the results of this and the passed matcher applied to
   * the same value.
   *
   * <p>
   * The reason <code>or</code> has an upper bound on its type parameter is so that the <code>Matcher</code>
   * resulting from an invocation of <code>or</code> will have the correct type parameter. If you call
   * <code>or</code> on a <code>Matcher[Orange]</code>, passing in a <code>Matcher[Valencia]</code>,
   * the result will have type <code>Matcher[Valencia]</code>. This is correct because both a
   * <code>Matcher[Orange]</code> and a <code>Matcher[Valencia]</code> know how to match a
   * <code>Valencia</code> (but a <code>Matcher[Valencia]</code> doesn't know how to
   * match any old <code>Orange</code>).  If you call
   * <code>or</code> on a <code>Matcher[Orange]</code>, passing in a <code>Matcher[Fruit]</code>,
   * the result will have type <code>Matcher[Orange]</code>. This is also correct because both a
   * <code>Matcher[Orange]</code> and a <code>Matcher[Fruit]</code> know how to match an
   * <code>Orange</code> (but a <code>Matcher[Orange]</code> doesn't know how to
   * match any old <code>Fruit</code>).
   * </p>
   *
   * @param the matcher to logical-or with this matcher
   * @return a matcher that performs the logical-or of this and the passed matcher
   */
  def or[U <: T](rightMatcher: => Matcher[U]): Matcher[U] =
    new Matcher[U] {
      def apply(left: U) = {
        val leftMatcherResult = leftMatcher(left)
        if (leftMatcherResult.matches)
          MatcherResult(
            true,
            leftMatcherResult.negativeFailureMessage,
            leftMatcherResult.failureMessage
          )
        else {
          val rightMatcherResult = rightMatcher(left)
          MatcherResult(
            rightMatcherResult.matches,
            Resources("commaAnd", leftMatcherResult.failureMessage, rightMatcherResult.failureMessage),
            Resources("commaAnd", leftMatcherResult.failureMessage, rightMatcherResult.negativeFailureMessage)
          )
        }
      }
    }

  // def ||[U <: T](rightMatcher: => Matcher[U]): Matcher[U] = and(rightMatcher)

  // Dropping to eliminate redundancy. Redundant with or not { ... }
  // def orNot[U <: T](rightMatcher: => Matcher[U]): Matcher[U] = leftMatcher or Helper.not { rightMatcher }
}


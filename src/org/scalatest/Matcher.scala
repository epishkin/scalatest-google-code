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
 * 
 */
trait Matcher[-T] extends Function1[T, MatcherResult] { leftMatcher =>

  // left is generally the object on which should is invoked.
  def apply(left: T): MatcherResult

  // left is generally the object on which should is invoked. leftMatcher
  // is the left operand to and. For example, in:
  // cat should { haveLives (9) and landOn (feet) }
  // left is 'cat' and leftMatcher is the matcher produced by 'haveLives (9)'.
  // rightMatcher, by the way, is the matcher produced by 'landOn (feet)'
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

  // Dropping to eliminate redundancy. Redundant with and not { ... }
  // def andNot[U <: T](rightMatcher: => Matcher[U]): Matcher[U] = leftMatcher and Helper.not { rightMatcher }

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

  // Dropping to eliminate redundancy. Redundant with or not { ... }
  // def orNot[U <: T](rightMatcher: => Matcher[U]): Matcher[U] = leftMatcher or Helper.not { rightMatcher }
}


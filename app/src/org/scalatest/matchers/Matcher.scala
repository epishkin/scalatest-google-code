package org.scalatest.matchers

case class MatcherResult(
  matches: Boolean,
  failureMessage: String,
  negativeFailureMessage: String
)

trait Matcher[T] { leftMatcher =>

  // left is generally the object on which should is invoked.
  def apply(left: T): MatcherResult

  // left is generally the object on which should is invoked. leftMatcher
  // is the left operand to and. For example, in:
  // cat should { haveLives (9) and landOn (feet) }
  // left is 'cat' and leftMatcher is the matcher produced by 'haveLives (9)'.
  // rightMatcher, by the way, is the matcher produced by 'landOn (feet)'
  def and(rightMatcher: => Matcher[T]): Matcher[T] =
    new Matcher[T] {
      def apply(left: T) = {
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
            leftMatcherResult.negativeFailureMessage +", but "+ rightMatcherResult.failureMessage,
            leftMatcherResult.negativeFailureMessage +", but "+ rightMatcherResult.negativeFailureMessage
          )
        }
      }
    }
}

class Shouldalizer[T](left: T) {
  def should(rightMatcher: Matcher[T]) {
    rightMatcher(left) match {
      case MatcherResult(false, failureMessage, _) => throw new AssertionError(failureMessage)
      case _ => ()
    }
  }
}

object Matchers {

  implicit def shouldify[T](o: T): Shouldalizer[T] = new Shouldalizer(o)

  def equal[S <: Any](right: S) =
    new Matcher[S] {
      def apply(left: S) =
        MatcherResult(left == right, left +" did not equal "+ right, left +" equaled "+ right)
    }

  def be(right: Boolean) = 
    new Matcher[Boolean] {
      def apply(left: Boolean) =
        MatcherResult(left == right, left +" was not "+ right, left +" was "+ right)
    }

  def be[S <: AnyRef](o: Null) = 
    new Matcher[S] {
      def apply(left: S) = {
        val theParam = left
        if (theParam == null)
          MatcherResult(true, "null was not null", "null was null")
        else
          MatcherResult(false, theParam +" was not null", theParam +"was null")
      }
    }

  def not[S <: Any](matcher: Matcher[S]) =
    new Matcher[S] {
      def apply(left: S) =
        matcher(left) match {
          case MatcherResult(bool, s1, s2) => MatcherResult(!bool, s2, s1)
        }
    }

  def endWith[T <: String](right: T) =
    new Matcher[T] {
      def apply(left: T) =
        MatcherResult(
          left endsWith right,
          "\""+ left +"\" did not end with \""+ right +"\"",
          "\""+ left +"\" ended with \""+ right +"\""
        )
    }
}


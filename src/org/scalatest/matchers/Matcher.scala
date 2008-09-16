package org.scalatest.matchers

case class MatcherResult(
  matches: Boolean,
  failureMessage: String,
  negativeFailureMessage: String
)

trait Matcher[T] { left =>

  def apply(param: => T): MatcherResult

  def and(right: Matcher[T]): Matcher[T] =
    new Matcher[T] {
      def apply(param: => T) = {
        val leftMatcherResult = left.apply(param)
        if (!leftMatcherResult.matches)
          MatcherResult(
            false,
            leftMatcherResult.failureMessage,
            leftMatcherResult.negativeFailureMessage
          )
        else {
          val rightMatcherResult = right.apply(param)
          MatcherResult(
            rightMatcherResult.matches,
            leftMatcherResult.negativeFailureMessage +", but "+ rightMatcherResult.failureMessage,
            leftMatcherResult.negativeFailureMessage +", but "+ rightMatcherResult.negativeFailureMessage
          )
        }
      }
    }
}

class Shouldilizer[T](left: => T) {
  def should(right: Matcher[T]) {
    right(left) match {
      case MatcherResult(false, s1, s2) => throw new AssertionError(s1)
      case _ => ()
    }
  }
}

object Matchers {
  implicit def shouldify[T](o: T): Shouldilizer[T] = new Shouldilizer(o)
  def equal[S <: Any](o: S) =
    new Matcher[S] {
      def apply(param: => S) =
        MatcherResult(param == o, param +" did not equal "+ o, param +" equaled "+ o)
    }

  def be[S <: Boolean](o: S) = equal(o)

  def not[S <: Any](matcher: Matcher[S]) =
    new Matcher[S] {
      def apply(param: => S) =
        matcher(param) match {
          case MatcherResult(bool, s1, s2) => MatcherResult(!bool, s2, s1)
        }
    }

  def endWith[T <: String](s: T) =
    new Matcher[T] {
      def apply(param: => T) =
        MatcherResult(
          param endsWith s,
          "\""+ param +"\" did not end with \""+ s +"\"",
          "\""+ param +"\" ended with \""+ s +"\""
        )
    }
}


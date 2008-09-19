package org.scalatest.matchers

import java.lang.reflect.Method
import java.lang.reflect.Modifier

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
            Resources("commaBut", leftMatcherResult.negativeFailureMessage, rightMatcherResult.failureMessage),
            Resources("commaBut", leftMatcherResult.negativeFailureMessage, rightMatcherResult.negativeFailureMessage)
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
        MatcherResult(
          left == right,
          Resources("didNotEqual", left, right),
          Resources("equaled", left, right)
        )
    }

  def be(right: Boolean) = 
    new Matcher[Boolean] {
      def apply(left: Boolean) =
        MatcherResult(
          left == right,
          Resources("wasNot", left, right),
          Resources("was", left, right)
        )
    }

  def be[S <: AnyRef](o: Null) = 
    new Matcher[S] {
      def apply(left: S) = {
        MatcherResult(
          left == null,
          Resources("wasNotNull", left),
          Resources("wasNull", left)
        )
      }
    }

  def beA[S <: AnyRef](right: Symbol): Matcher[S] = be(right)
  def beAn[S <: AnyRef](right: Symbol): Matcher[S] = be(right)

  def be[S <: AnyRef](right: Symbol): Matcher[S] = {

    new Matcher[S] {
      def apply(left: S) = {

        // If 'empty passed, rightNoTick would be "empty"
        val rightNoTick = right.toString.substring(1)

        // methodNameToInvoke would also be "empty"
        val methodNameToInvoke = rightNoTick

        // methodNameToInvokeWithIs would be "isEmpty"
        val methodNameToInvokeWithIs = "is"+ rightNoTick(0).toUpperCase + rightNoTick.substring(1)

        def isMethodToInvoke(m: Method) = {

          val isInstanceMethod = !Modifier.isStatic(m.getModifiers())
          val simpleName = m.getName
          val paramTypes = m.getParameterTypes
          val hasNoParams = paramTypes.length == 0
          val resultType = m.getReturnType

          isInstanceMethod && hasNoParams &&
          (simpleName == methodNameToInvoke || simpleName == methodNameToInvokeWithIs) &&
          resultType == classOf[Boolean]
        }

        // Store in an array, because may have both isEmpty and empty, in which case I
        // will throw an exception.
        val methodArray =
          for (m <- left.getClass.getMethods; if isMethodToInvoke(m))
            yield m

        methodArray.length match {
          case 0 =>
            throw new IllegalArgumentException(
              left +" has neither a "+ methodNameToInvoke +" or a "+ methodNameToInvokeWithIs +" method."
            )
          case 1 =>
            val result = methodArray(0).invoke(left, Array[AnyRef]()).asInstanceOf[Boolean]
            MatcherResult(result, left +" was not "+ rightNoTick, left +" was "+ rightNoTick)
          case _ => // Should only ever be 2, but just in case
            throw new IllegalArgumentException(
              left +" has both a "+ methodNameToInvoke +" and a "+ methodNameToInvokeWithIs +" method."
            )
        }
      }
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


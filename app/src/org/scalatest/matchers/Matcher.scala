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

//
// This class is used as the return type of the overloaded should method (in ShouldalizerForMap)
// that takes a HaveWord. It's key method will be called in situations like this:
//
// map should have key 1
//
// This gets changed to :
//
// shouldifyForMap(map).should(have).key(1)
//
// Thus, the map is wrapped in a shouldifyForMap call via an implicit conversion, which results in 
// a ShouldilizerForMap. This has a should method that takes a HaveWord. That method returns a
// ResultOfHaveWordPassedToShould that remembers the map to the left of should. Then this class
// ha a key method that takes a K type, they key type of the map. It does the assertion thing.
// 
class ResultOfHaveWord[K, V](left: Map[K, V], shouldBeTrue: Boolean) {
  def key(keyValue: K) =
    if (left.contains(keyValue) != shouldBeTrue)
      throw new AssertionError(
        Resources(
          if (shouldBeTrue) "didNotHaveKey" else "hadKey",
          left.toString,
          keyValue.toString)
      )
}

class ResultOfHaveWordPassedToShould[K, V](left: Map[K, V])
    extends ResultOfHaveWord(left, true)

class ResultOfHaveWordPassedToShouldNot[K, V](left: Map[K, V])
    extends ResultOfHaveWord(left, false)

class Shouldalizer[T](left: T) {
  def should(rightMatcher: Matcher[T]) {
    rightMatcher(left) match {
      case MatcherResult(false, failureMessage, _) => throw new AssertionError(failureMessage)
      case _ => ()
    }
  }
  def shouldNot(rightMatcher: Matcher[T]) {
    rightMatcher(left) match {
      case MatcherResult(true, _, failureMessage) => throw new AssertionError(failureMessage)
      case _ => ()
    }
  }
}

class HaveWord {
  //
  // This key method is called when "have" is used in a logical expression, such as:
  // map should { have key 1 and equal (Map(1 -> "Howdy")) }. It results in a matcher
  // that remembers the key value.
  // 
  def key[K, V](keyVal: K): Matcher[Map[K, V]] =
    new Matcher[Map[K, V]] {
      def apply(left: Map[K, V]) =
        MatcherResult(
          left.contains(keyVal), 
          Resources("didNotHaveKey", left.toString, keyVal.toString),
          Resources("hadKey", left.toString, keyVal.toString)
        )
    }
}

class ShouldalizerForMap[K, V](left: Map[K, V]) extends Shouldalizer(left) {
  def should(haveWord: HaveWord): ResultOfHaveWordPassedToShould[K, V] = {
    new ResultOfHaveWordPassedToShould(left)
  }
  def shouldNot(haveWord: HaveWord): ResultOfHaveWordPassedToShouldNot[K, V] = {
    new ResultOfHaveWordPassedToShouldNot(left)
  }
}

object Matchers {

  implicit def shouldify[T](o: T): Shouldalizer[T] = new Shouldalizer(o)
  implicit def shouldifyForMap[K, V](left: Map[K, V]): ShouldalizerForMap[K, V] = new ShouldalizerForMap[K, V](left)

  def equal[S <: Any](right: S) =
    new Matcher[S] {
      def apply(left: S) =
        MatcherResult(
          left == right,
          Resources("didNotEqual", left.toString, right.toString),
          Resources("equaled", left.toString, right.toString)
        )
    }

  def be(right: Boolean) = 
    new Matcher[Boolean] {
      def apply(left: Boolean) =
        MatcherResult(
          left == right,
          Resources("wasNot", left.toString, right.toString),
          Resources("was", left.toString, right.toString)
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

        val firstChar = rightNoTick(0).toLowerCase
        val methodNameStartsWithVowel = firstChar == 'a' || firstChar == 'e' || firstChar == 'i' ||
          firstChar == 'o' || firstChar == 'u'

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
              Resources(
                if (methodNameStartsWithVowel) "hasNeitherAnOrAnMethod" else "hasNeitherAOrAnMethod",
                left,
                methodNameToInvoke,
                methodNameToInvokeWithIs
              )
            )
          case 1 =>
            val result = methodArray(0).invoke(left, Array[AnyRef]()).asInstanceOf[Boolean]
            MatcherResult(
              result,
              Resources("wasNot", left.toString, rightNoTick.toString),
              Resources("was", left.toString, rightNoTick.toString)
            )
          case _ => // Should only ever be 2, but just in case
            throw new IllegalArgumentException(
              Resources(
                if (methodNameStartsWithVowel) "hasBothAnAndAnMethod" else "hasBothAAndAnMethod",
                left,
                methodNameToInvoke,
                methodNameToInvokeWithIs
              )
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
          Resources("didNotEndWith", left, right),
          Resources("endedWith", left, right)
        )
    }
/*
    In HaveWord's methods key, value, length, and size, I can give type parameters.
    The type HaveWord can contain a key method that takes a S or what not, and returns a matcher, which
    stores the key value in a val and whose apply method checks the passed map for the remembered key. This
    one would be used in things like:

    map should { have key 9 and have value "bob" }

    There's an overloaded should method on Shouldifier that takes a HaveWord. This method results in
    a different type that also has a key method that takes an S. So when you say:

    map should have key 9

    what happens is that this alternate should method gets invoked. The result is this other class that
    has a key method, and its constructor takes the map and stores it in a val. So this time when key is
    invoked, it checks to make sure the passed key is in the remembered map, and does the assertion.

    length and size can probably use structural types, because I want to use length on string and array for
    starters, and other people may create classes that have length methods. Would be nice to be able to use them.
  */
  def have = new HaveWord
}


package org.scalatest.matchers

import java.lang.reflect.Method
import java.lang.reflect.Modifier

case class MatcherResult(
  matches: Boolean,
  failureMessage: String,
  negativeFailureMessage: String
)

/*
There are a set of implicit conversions that take different static types to Shouldalizers.
The one that gets applied will be the one that matches the static type of left. The result
of the implicit conversion will be a Shouldalizer. There's a hierarchy of these, so that
more specific types inherit the should methods of more general types. For example:

        Shouldalizer
             ^
             |
    CollectionShouldalizer
             ^
             |
       MapShouldalizer

The should methods take different static types, so they are overloaded. These types don't all
inherit from the same supertype. There's a plain-old Matcher for example, but there's also maybe
a BeMatcher, and BeMatcher doesn't extend Matcher. This reduces the number of incorrect static
matches, which can happen if a more specific type is held from a more general variable type.
And reduces the chances for ambiguity, I suspect.

The type parameter to Matcher can be used to set an upper limit on the types passed to the apply
method. So for example, if a matcher can only be used with Lists, then we say it is a Matcher[List].
If it can be used on any type of Collection, it is a Matcher[Collection].
*/

trait Matcher[T] { leftMatcher =>

  // left is generally the object on which should is invoked.
  // It must be a subtype of T
  def apply[S <: T](left: S): MatcherResult

  // left is generally the object on which should is invoked. leftMatcher
  // is the left operand to and. For example, in:
  // cat should { haveLives (9) and landOn (feet) }
  // left is 'cat' and leftMatcher is the matcher produced by 'haveLives (9)'.
  // rightMatcher, by the way, is the matcher produced by 'landOn (feet)'
  def and(rightMatcher: => Matcher[T]): Matcher[T] =
    new Matcher[T] {
      def apply[S <: T](left: S) = {
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
// This class is used as the return type of the overloaded should method (in MapShouldalizer)
// that takes a HaveWord. It's key method will be called in situations like this:
//
// map should have key 1
//
// This gets changed to :
//
// shouldifyForMap(map).should(have).key(1)
//
// Thus, the map is wrapped in a shouldifyForMap call via an implicit conversion, which results in 
// a MapShouldalizer. This has a should method that takes a HaveWord. That method returns a
// ResultOfHaveWordPassedToShould that remembers the map to the left of should. Then this class
// ha a key method that takes a K type, they key type of the map. It does the assertion thing.
// 
class ResultOfHaveWordForMap[K, V](left: Map[K, V], shouldBeTrue: Boolean) extends ResultOfHaveWordForCollection[(K, V)](left, shouldBeTrue) {
  def key(expectedKey: K) =
    if (left.contains(expectedKey) != shouldBeTrue)
      throw new AssertionError(
        Resources(
          if (shouldBeTrue) "didNotHaveKey" else "hadKey",
          left.toString,
          expectedKey.toString)
      )
  def value(expectedValue: V) =
    if (left.values.contains(expectedValue) != shouldBeTrue)
      throw new AssertionError(
        Resources(
          if (shouldBeTrue) "didNotHaveValue" else "hadValue",
          left.toString,
          expectedValue.toString)
      )
}

class ResultOfHaveWordForMapPassedToShould[K, V](left: Map[K, V])
    extends ResultOfHaveWordForMap(left, true)

class ResultOfHaveWordForMapPassedToShouldNot[K, V](left: Map[K, V])
    extends ResultOfHaveWordForMap(left, false)

class Shouldalizer[T](left: T) {
  def should(rightMatcher: Matcher[T]) {
    // println("*@*@*@*@*@*@*@*@*@*@**@*@ left passed to should"+ left.toString)
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
  def key[K, V](expectedKey: K): Matcher[Map[K, V]] =
    new Matcher[Map[K, V]] {
      def apply[S <: Map[K, V]](left: S) =
        MatcherResult(
          left.contains(expectedKey), 
          Resources("didNotHaveKey", left.toString, expectedKey.toString),
          Resources("hadKey", left.toString, expectedKey.toString)
        )
    }
  def value[K, V](expectedValue: V): Matcher[Map[K, V]] =
    new Matcher[Map[K, V]] {
      def apply[S <: Map[K, V]](left: S) =
        MatcherResult(
          left.values.contains(expectedValue), 
          Resources("didNotHaveValue", left.toString, expectedValue.toString),
          Resources("hadValue", left.toString, expectedValue.toString)
        )
    }
}

class MapShouldalizer[K, V](left: Map[K, V]) extends Shouldalizer(left) {
  def should(haveWord: HaveWord): ResultOfHaveWordForMapPassedToShould[K, V] = {
    new ResultOfHaveWordForMapPassedToShould(left)
  }
  def shouldNot(haveWord: HaveWord): ResultOfHaveWordForMapPassedToShouldNot[K, V] = {
    new ResultOfHaveWordForMapPassedToShouldNot(left)
  }
}

//
// This class is used as the return type of the overloaded should method (in CollectionShouldalizer)
// that takes a HaveWord. It's size method will be called in situations like this:
//
// list should have size 1
//
// This gets changed to :
//
// shouldifyForCollection(list).should(have).size(1)
//
// Thus, the list is wrapped in a shouldifyForCollection call via an implicit conversion, which results in 
// a CollectionShouldalizer. This has a should method that takes a HaveWord. That method returns a
// ResultOfHaveWordForCollectionPassedToShould that remembers the map to the left of should. Then this class
// has a size method that takes a T type, type parameter of the iterable. It does the assertion thing.
// 
class ResultOfHaveWordForCollection[T](left: Collection[T], shouldBeTrue: Boolean) {
  def size(expectedSize: Int) =
    if ((left.size == expectedSize) != shouldBeTrue)
      throw new AssertionError(
        Resources(
          if (shouldBeTrue) "didNotHaveExpectedSize" else "hadExpectedSize",
          left.toString,
          expectedSize.toString)
      )
}

class ResultOfHaveWordPassedToShouldForCollection[T](left: Collection[T])
    extends ResultOfHaveWordForCollection(left, true)

class ResultOfHaveWordPassedToShouldNotForCollection[T](left: Collection[T])
    extends ResultOfHaveWordForCollection(left, false)

class AnyRefShouldalizer[T <: AnyRef](left: T) extends Shouldalizer(left) {
  def should(rightSymbol: Symbol) {
    val dynaMatcher = new DynamicPredicateMatcher(rightSymbol)
    dynaMatcher(left) match {
      case MatcherResult(false, failureMessage, _) => throw new AssertionError(failureMessage)
      case _ => ()
    }
  }
  def shouldNot(rightSymbol: Symbol) {
    val dynaMatcher = new DynamicPredicateMatcher(rightSymbol)
    dynaMatcher(left) match {
      case MatcherResult(true, _, failureMessage) => throw new AssertionError(failureMessage)
      case _ => ()
    }
  }
}

class CollectionShouldalizer[T](left: Collection[T]) extends AnyRefShouldalizer(left) {
  def should(haveWord: HaveWord): ResultOfHaveWordPassedToShouldForCollection[T] = {
    new ResultOfHaveWordPassedToShouldForCollection(left)
  }
  def shouldNot(haveWord: HaveWord): ResultOfHaveWordPassedToShouldNotForCollection[T] = {
    new ResultOfHaveWordPassedToShouldNotForCollection(left)
  }
}

class DynamicPredicateMatcher(right: Symbol) extends Matcher[AnyRef] {

  require(right.toString startsWith "'be", "Symbol was "+ right.toString +", but must start with \"'be\"") 

  def apply[S <: AnyRef](left: S) = {
  
    // println("*@*@*@*@*@*@*@*@*@*@**@*@ "+ left.getClass.getName)
    // If 'beEmpty passed, rightNoTick would be "empty"
    val rightNoTick = right.toString.substring(3)
    
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

object Matchers {

  implicit def shouldify[T](o: T): Shouldalizer[T] = new Shouldalizer(o)
  implicit def shouldifyForMap[K, V](left: Map[K, V]): MapShouldalizer[K, V] = new MapShouldalizer[K, V](left)
  implicit def shouldifyForCollection[T](left: Collection[T]): CollectionShouldalizer[T] = new CollectionShouldalizer[T](left)

  implicit def symbolToDynamicPredicateMatcher(symbol: Symbol) = new DynamicPredicateMatcher(symbol)

  def equal[T <: Any](right: T) =
    new Matcher[T] {
      def apply[S <: Any](left: S) =
        MatcherResult(
          left == right,
          Resources("didNotEqual", left.toString, right.toString),
          Resources("equaled", left.toString, right.toString)
        )
    }

  def be[T <: Any](right: T): Matcher[T] = {
    new Matcher[T] {
      def apply[S <: Any](left: S) =
        MatcherResult(
          left == right,
          Resources("wasNot", if (left == null) "null" else left.toString, if (right == null) "null" else right.toString),
          Resources("was", if (left == null) "null" else left.toString, if (right == null) "null" else right.toString)
        )
    }
  }

/*
  def be[S <: Any](right: Any): Matcher[S] = {

    def handleRightIsSymbolCase(right: Symbol): Matcher[S] = {
      new Matcher[S] {
        def apply(left: S) = {
  
          def handleLeftIsAnyRefCase[T <: AnyRef](left: T): MatcherResult = {
  
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

          left match {
            case anyRef: AnyRef => handleLeftIsAnyRefCase(anyRef)
            // This is left is an AnyVal, and right is a Symbol, so I know this is false
            case _ =>
              MatcherResult(
                false, 
                Resources("wasNot", left.toString, right.toString),
                Resources("was", left.toString, right.toString)
              )
          }
        }
      }
    }

    right match {
      case symbol: Symbol => handleRightIsSymbolCase(symbol)
      case _ =>
        new Matcher[S] {
          def apply(left: S) =
            MatcherResult(
              left == right,
              Resources("wasNot", if (left == null) "null" else left.toString, if (left == null) "null" else right.toString),
              Resources("was", if (left == null) "null" else left.toString, if (right == null) "null" else right.toString)
            )
        }
    }
  }
*/

  def beA(right: Any): Matcher[Any] = be(right)
  def beAn(right: Any): Matcher[Any] = be(right)

  def not[T <: Any](matcher: Matcher[T]) =
    new Matcher[T] {
      def apply[S <: T](left: S) =
        matcher(left) match {
          case MatcherResult(bool, s1, s2) => MatcherResult(!bool, s2, s1)
        }
    }

  def not(symbol: Symbol) = {
    val dynaMatcher = new DynamicPredicateMatcher(symbol)
    new Matcher[AnyRef] {
      def apply[S <: AnyRef](left: S) = {
        // println("*@*@*@*@*@*@*@*@*@*@**@*@ Passed to not matcher's apply"+ left.getClass.getName)
        dynaMatcher(left) match {
          case MatcherResult(bool, s1, s2) => MatcherResult(!bool, s2, s1)
        }
      }
    }
  }

  def endWith(right: String) =
    new Matcher[String] {
      def apply[S <: String](left: S) =
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

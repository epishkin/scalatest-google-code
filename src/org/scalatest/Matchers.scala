package org.scalatest

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import scala.util.matching.Regex

private[scalatest] case class MatcherResult(
  matches: Boolean,
  failureMessage: String,
  negativeFailureMessage: String
)

// This is used to pass a string to the FailureMessages apply method
// but prevent it from being quoted. This is useful when using a string
// to talk about method names, for example.
private[scalatest] class UnquotedString(s: String) {
  override def toString = s
}

private[scalatest] object UnquotedString {
  def apply(s: String) = new UnquotedString(s)
}

/*
There are a set of implicit conversions that take different static types to Shouldalizers.
The one that gets applied will be the one that matches the static type of left. The result
of the implicit conversion will be a Shouldalizer.

The should methods take different static types, so they are overloaded. These types don't all
inherit from the same supertype. There's a plain-old Matcher for example, but there's also maybe
a BeMatcher, and BeMatcher doesn't extend Matcher. This reduces the number of incorrect static
matches, which can happen if a more specific type is held from a more general variable type.
And reduces the chances for ambiguity, I suspect.

On my jog I thought perhaps that Matcher should be contravariant in T, because
if I have hierarchy Fruit <-- Orange <-- ValenciaOrange, and I have:

val orange = Orange

"orange should" will give me a Shouldalizer[Orange], which has an apply method that takes a Matcher[Orange].
If I have a Matcher[ValenciaOrange], that shouldn't compile, but if I have a Matcher[Fruit], it should compile.
Thus I should be able to pass a Matcher[Fruit] to a should method that expects a Matcher[Orange], which is
contravariance. Then the type of the "left" parameter of the apply method can just be T, because in the case
of Matcher[Fruit], for example, T is Fruit, and you can pass an Orange to an apply method that expects a Fruit.

So it should be:

trait Matcher[-T] { leftMatcher => ...

Yay, that worked, so long as I do the upper bound thing in add. All makes sense. If I do
matcherOfOrange and matcherOfValencia, then the type of the resulting matcher needs to be
matcherOfValencia. But if I do "matcherOfOrange and matcherOfFruit", the type stays at
matcherOfOrange. And the right operand is considered a matcher of orange, because of contravariance.

Made it extend Function1 for the heck of it. Can pass it as a Function1 now.
*/

private[scalatest] object Helper {
  def not[S <: Any](matcher: Matcher[S]) =
    new Matcher[S] {
      def apply(left: S) =
        matcher(left) match {
          case MatcherResult(bool, s1, s2) => MatcherResult(!bool, s2, s1)
        }
    }

  def equalAndBeAnyMatcher(right: Any, equaledResourceName: String, didNotEqualResourceName: String) = {

      new Matcher[Any] {
        def apply(left: Any) =
          left match {
            case leftArray: Array[_] => 
              MatcherResult(
                leftArray.deepEquals(right),
                FailureMessages(didNotEqualResourceName, left, right),
                FailureMessages(equaledResourceName, left, right)
              )
            case _ => 
              MatcherResult(
                left == right,
                FailureMessages(didNotEqualResourceName, left, right),
                FailureMessages(equaledResourceName, left, right)
              )
        }
      }
  }
}

private[scalatest] trait Matcher[-T] extends Function1[T, MatcherResult] { leftMatcher =>

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
            Resources("commaBut", leftMatcherResult.negativeFailureMessage, rightMatcherResult.negativeFailureMessage)
          )
        }
      }
    }

  def andNot[U <: T](rightMatcher: => Matcher[U]): Matcher[U] = leftMatcher and Helper.not { rightMatcher }

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

  def orNot[U <: T](rightMatcher: => Matcher[U]): Matcher[U] = leftMatcher or Helper.not { rightMatcher }
}

private[scalatest] trait BaseMatchers extends Assertions {

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
  private[scalatest] class ResultOfHaveWordForMap[K, V](left: Map[K, V], shouldBeTrue: Boolean) extends ResultOfHaveWordForCollection[Tuple2[K, V]](left, shouldBeTrue) {
    def key(expectedKey: K) {
      if (left.contains(expectedKey) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveKey" else "hadKey",
            left,
            expectedKey)
        )
    }
    def value(expectedValue: V) {
      if (left.values.contains(expectedValue) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveValue" else "hadValue",
            left,
            expectedValue)
        )
    }
  /*
    def size(expectedSize: Int) =
      if ((left.size == expectedSize) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedSize" else "hadExpectedSize",
            left,
            expectedSize)
        )
  */
  }
  
  private[scalatest] class BehaveWord
  private[scalatest] class ContainWord
  private[scalatest] class IncludeWord {
    def substring(expectedSubstring: String): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) =
          MatcherResult(
            left.indexOf(expectedSubstring) >= 0, 
            FailureMessages("didNotIncludeSubstring", left, expectedSubstring),
            FailureMessages("includedSubstring", left, expectedSubstring)
          )
      }
  }

  private[scalatest] class StartWithWord {
    def substring[T <: String](right: T) =
      new Matcher[T] {
        def apply(left: T) =
          MatcherResult(
            left startsWith right,
            FailureMessages("didNotStartWith", left, right),
            FailureMessages("startedWith", left, right)
          )
      }
    def regex[T <: String](right: T): Matcher[T] = regex(right.r)
    def regex(rightRegex: Regex): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) =
          MatcherResult(
            rightRegex.pattern.matcher(left).lookingAt,
            FailureMessages("didNotStartWithRegex", left, rightRegex),
            FailureMessages("startedWithRegex", left, rightRegex)
          )
      }
  }

  private[scalatest] class EndWithWord {
    def substring[T <: String](right: T) =
      new Matcher[T] {
        def apply(left: T) =
          MatcherResult(
            left endsWith right,
            FailureMessages("didNotEndWith", left, right),
            FailureMessages("endedWith", left, right)
          )
      }
    def regex[T <: String](right: T): Matcher[T] = regex(right.r)
    def regex(rightRegex: Regex): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) = {
          val allMatches = rightRegex.findAllIn(left)
          MatcherResult(
            allMatches.hasNext && (allMatches.end == left.length),
            FailureMessages("didNotEndWithRegex", left, rightRegex),
            FailureMessages("endedWithRegex", left, rightRegex)
          )
        }
      }
  }

  private[scalatest] class FullyMatchWord {
    def regex(rightRegexString: String): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) =
          MatcherResult(
            java.util.regex.Pattern.matches(rightRegexString, left),
            FailureMessages("didNotFullyMatchRegex", left, rightRegexString),
            FailureMessages("fullyMatchedRegex", left, rightRegexString)
          )
      }
    def regex(rightRegex: Regex): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) =
          MatcherResult(
            rightRegex.pattern.matcher(left).matches,
            FailureMessages("didNotFullyMatchRegex", left, rightRegex),
            FailureMessages("fullyMatchedRegex", left, rightRegex)
          )
      }
  }

  private[scalatest] class HaveWord {
    //
    // This key method is called when "have" is used in a logical expression, such as:
    // map should { have key 1 and equal (Map(1 -> "Howdy")) }. It results in a matcher
    // that remembers the key value. By making the value type Any, it causes overloaded shoulds
    // to work, because for example a Matcher[Map[Int, Any]] is a subtype of Matcher[Map[Int, String]],
    // given Map is covariant in its V (the value type stored in the map) parameter and Matcher is
    // contravariant in its lone type parameter. Thus, the type of the Matcher resulting from have key 1
    // is a subtype of the map type that has a known value type parameter because its that of the map
    // to the left of should. This means the should method that takes a map will be selected by Scala's
    // method overloading rules.
    // 
    def key[K](expectedKey: K): Matcher[Map[K, Any]] =
      new Matcher[Map[K, Any]] {
        def apply(left: Map[K, Any]) =
          MatcherResult(
            left.contains(expectedKey), 
            FailureMessages("didNotHaveKey", left, expectedKey),
            FailureMessages("hadKey", left, expectedKey)
          )
      }
  
    // Holy smokes I'm starting to scare myself. I fixed the problem of the compiler not being
    // able to infer the value type in  have value 1 and ... like expressions, because the
    // value type is there, with an existential type. Since I don't know what K is, I decided to
    // try just saying that with an existential type, and it compiled and ran. Pretty darned
    // amazing compiler. The problem could not be fixed like I fixed the key method above, because
    // Maps are nonvariant in their key type parameter, whereas they are covariant in their value
    // type parameter, so the same trick wouldn't work. But this existential type trick seems to
    // work like a charm.
    def value[V](expectedValue: V): Matcher[Map[K, V] forSome { type K }] =
      new Matcher[Map[K, V] forSome { type K }] {
        def apply(left: Map[K, V] forSome { type K }) =
          MatcherResult(
            left.values.contains(expectedValue), 
            FailureMessages("didNotHaveValue", left, expectedValue),
            FailureMessages("hadValue", left, expectedValue)
          )
      }
  
    def size(expectedSize: Int) =
      new Matcher[Collection[Any]] {
        def apply(left: Collection[Any]) =
          MatcherResult(
            left.size == expectedSize, 
            FailureMessages("didNotHaveValue", left, expectedSize),
            FailureMessages("hadValue", left, expectedSize)
          )
      }
  /*
    // Go ahead and use a structural type here too, to make it more general. Can then
    // use this on any type that has a size method. I guess it doesn't matter in structural
    // types if you put the empty parens on there or not.
    def size(expectedSize: Int) =
      new Matcher[{ def size(): Int }] {
        def apply(left: { def size(): Int }) =
          MatcherResult(
            left.size == expectedSize, 
            FailureMessages("didNotHaveExpectedSize", left, expectedSize),
            FailureMessages("hadExpectedSize", left, expectedSize)
          )
      }
  */
  
  /*
    // This should give me { def length(): Int } I don't
    // know the type, but it has a length method. This would work on strings and ints, but
    // I"m not sure what the story is on the parameterless or not. Probably should put parens in there.
    // String is a structural subtype of { def length(): Int }. Thus Matcher[{ def length(): Int }] should
    // be a subtype of Matcher[String], because of contravariance. Yeah, this worked! 
    // Darn structural type won't work for both arrays and strings, because one requres a length and the other a length()
    // So they aren't the same structural type. Really want the syntax, so moving to reflection and a runtime error.
    def length(expectedLength: Int) =
      new Matcher[{ def length: Int }] {
        def apply(left: { def length: Int }) =
          MatcherResult(
            left.length == expectedLength, 
            FailureMessages("didNotHaveExpectedLength", left, expectedLength),
            FailureMessages("hadExpectedLength", left, expectedLength)
          )
      }
  */
    def length(expectedLength: Int) =
      new Matcher[AnyRef] {
        def apply(left: AnyRef) =
          left match {
            case leftSeq: Seq[_] =>
              MatcherResult(
                leftSeq.length == expectedLength, 
                FailureMessages("didNotHaveExpectedLength", left, expectedLength),
                FailureMessages("hadExpectedLength", left, expectedLength)
              )
            case leftString: String =>
              MatcherResult(
                leftString.length == expectedLength, 
                FailureMessages("didNotHaveExpectedLength", left, expectedLength),
                FailureMessages("hadExpectedLength", left, expectedLength)
              )
            case _ =>
              val methods = left.getClass.getMethods
              val methodOption = methods.find(_.getName == "length")
              val hasLengthMethod =
                methodOption match {
                  case Some(method) =>
                    method.getParameterTypes.length == 0
                  case None => false
                }
              val fields = left.getClass.getFields
              val fieldOption = fields.find(_.getName == "length")
              val hasLengthField =
                fieldOption match {
                  case Some(_) => true
                  case None => false
                }
              if (hasLengthMethod) {
                MatcherResult(
                  methodOption.get.invoke(left, Array[Object]()) == expectedLength, 
                  FailureMessages("didNotHaveExpectedLength", left, expectedLength),
                  FailureMessages("hadExpectedLength", left, expectedLength)
                )
              }
              else if (hasLengthField) {
                MatcherResult(
                  fieldOption.get.get(left) == expectedLength, 
                  FailureMessages("didNotHaveExpectedLength", left, expectedLength),
                  FailureMessages("hadExpectedLength", left, expectedLength)
                )
              }
              else {
                throw new AssertionError("'have length "+ expectedLength +"' used with an object that had neither a public field or method named 'length'.")
              }
        }
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
  private[scalatest] class ResultOfHaveWordForCollection[T](left: Collection[T], shouldBeTrue: Boolean) {
    def size(expectedSize: Int) {
      if ((left.size == expectedSize) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedSize" else "hadExpectedSize",
            left,
            expectedSize)
        )
    }
  }
  
  private[scalatest] class ResultOfHaveWordForSeq[T](left: Seq[T], shouldBeTrue: Boolean) extends ResultOfHaveWordForCollection[T](left, shouldBeTrue) {
    def length(expectedLength: Int) {
      if ((left.length == expectedLength) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
            left,
            expectedLength)
        )
    }
  }
  
  private[scalatest] class ResultOfBeWordForAnyRef(left: AnyRef, shouldBeTrue: Boolean) {
    def theSameInstanceAs(right: AnyRef) {
      if ((left eq right) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "wasNotSameInstanceAs" else "wasSameInstanceAs",
            left,
            right
          )
        )
    }
  }

/*
  // This worked, but the < because it's an operator gets run first: be < 7. So I need to do the
  // matcher for that one and that should suffice.
  private[scalatest] class ResultOfBeWordForOrdered[T <% Ordered[T]](left: T, shouldBeTrue: Boolean) {
    def lessThan(right: T) {
      if ((left < right) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "wasNotLessThan" else "wasLessThan",
            left,
            right
          )
        )
    }
  }

  private[scalatest] implicit def resultOfBeWordToForOrdered[T <% Ordered[T]](resultOfBeWord: ResultOfBeWord[T]): ResultOfBeWordForOrdered[T] =
    new ResultOfBeWordForOrdered(resultOfBeWord.left, resultOfBeWord.shouldBeTrue)
*/

  private[scalatest] implicit def resultOfBeWordToForAnyRef[T <: AnyRef](resultOfBeWord: ResultOfBeWord[T]): ResultOfBeWordForAnyRef =
    new ResultOfBeWordForAnyRef(resultOfBeWord.left, resultOfBeWord.shouldBeTrue)

  private[scalatest] class ResultOfBeWord[T](val left: T, val shouldBeTrue: Boolean) {
    def a[S <: AnyRef](right: Symbol): Matcher[S] = be(right) // TODO: I think these two are wrong
    def an[S <: AnyRef](right: Symbol): Matcher[S] = be(right)
  }

  private[scalatest] class ResultOfHaveWordForString(left: String, shouldBeTrue: Boolean) {
    def length(expectedLength: Int) {
      if ((left.length == expectedLength) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
            left,
            expectedLength)
        )
    }
  }
  
  private[scalatest] class ResultOfIncludeWordForString(left: String, shouldBeTrue: Boolean) {
    def substring(expectedSubstring: String) {
      if ((left.indexOf(expectedSubstring) >= 0) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotIncludeSubstring" else "includedSubstring",
            left,
            expectedSubstring
          )
        )
    }
    def regex(rightRegexString: String) { regex(rightRegexString.r) }
    def regex(rightRegex: Regex) {
      if (rightRegex.findFirstIn(left).isDefined != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotIncludeRegex" else "includedRegex",
            left,
            rightRegex
          )
        )
    }
  }

  private[scalatest] class ResultOfStartWithWordForString(left: String, shouldBeTrue: Boolean) {
    def substring(right: String) {
      if ((left startsWith right) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotStartWith" else "startedWith",
            left,
            right
          )
        )
    }
    def regex(rightRegexString: String) { regex(rightRegexString.r) }
    def regex(rightRegex: Regex) {
      if (rightRegex.pattern.matcher(left).lookingAt != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotStartWithRegex" else "startedWithRegex",
            left,
            rightRegex
          )
        )
    }
  }

  private[scalatest] class ResultOfEndWithWordForString(left: String, shouldBeTrue: Boolean) {
    def substring(right: String) {
      if ((left endsWith right) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotEndWith" else "endedWith",
            left,
            right
          )
        )
    }
    def regex(rightRegexString: String) { regex(rightRegexString.r) }
    def regex(rightRegex: Regex) {
      val allMatches = rightRegex.findAllIn(left)
      if ((allMatches.hasNext && (allMatches.end == left.length)) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotEndWithRegex" else "endedWithRegex",
            left,
            rightRegex
          )
        )
    }
  }

  private[scalatest] class ResultOfFullyMatchWordForString(left: String, shouldBeTrue: Boolean) {
    def regex(rightRegexString: String) { regex(rightRegexString.r) }
    def regex(rightRegex: Regex) {
      if (rightRegex.pattern.matcher(left).matches != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotFullyMatchRegex" else "fullyMatchedRegex",
            left,
            rightRegex
          )
        )
    }
  }
  
  private[scalatest] class ResultOfContainWordForIterable[T](left: Iterable[T], shouldBeTrue: Boolean) {
    def element(expectedElement: T) {
      if ((left.elements.contains(expectedElement)) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotContainExpectedElement" else "containedExpectedElement",
            left,
            expectedElement)
        )
    }
  }
  
  // Hey, how about an implicit conversion from val length to def length, and one from def length() to def length().
  // A bit ugly, but it might let me use structural typing. Is this being used now?
  implicit def stringToHasLength(s: AnyRef with String): { def length: Int } = new { def length: Int = s.length() }

  def equal(right: Any): Matcher[Any] =
    Helper.equalAndBeAnyMatcher(right, "equaled", "didNotEqual")
/*
    new Matcher[Any] {
      def apply(left: Any) =
        left match {
          case leftArray: Array[_] => 
            MatcherResult(
              leftArray.deepEquals(right),
              FailureMessages("didNotEqual", left, right),
              FailureMessages("equaled", left, right)
            )
          case _ => 
            MatcherResult(
              left == right,
              FailureMessages("didNotEqual", if (left != null) left else "null", if (right != null) right else "null"),
              FailureMessages("equaled", if (left != null) left else "null", if (right != null) right else "null")
            )
      }
    }
*/

  private[scalatest] class BeWordForOrdered {
    def <[T <% Ordered[T]](right: T): Matcher[T] =
      new Matcher[T] {
        def apply(left: T) =
          MatcherResult(
            left < right,
            FailureMessages("wasNotLessThan", left, right),
            FailureMessages("wasLessThan", left, right)
          )
      }
    def >[T <% Ordered[T]](right: T): Matcher[T] =
      new Matcher[T] {
        def apply(left: T) =
          MatcherResult(
            left > right,
            FailureMessages("wasNotGreaterThan", left, right),
            FailureMessages("wasGreaterThan", left, right)
          )
      }
    def <=[T <% Ordered[T]](right: T): Matcher[T] =
      new Matcher[T] {
        def apply(left: T) =
          MatcherResult(
            left <= right,
            FailureMessages("wasNotLessThanOrEqualTo", left, right),
            FailureMessages("wasLessThanOrEqualTo", left, right)
          )
      }
    def >=[T <% Ordered[T]](right: T): Matcher[T] =
      new Matcher[T] {
        def apply(left: T) =
          MatcherResult(
            left >= right,
            FailureMessages("wasNotGreaterThanOrEqualTo", left, right),
            FailureMessages("wasGreaterThanOrEqualTo", left, right)
          )
      }
  }

  private[scalatest] implicit def beWordToForOrdered(beWord: BeWord): BeWordForOrdered = new BeWordForOrdered

  private[scalatest] class BeWord {

    // These two are used if this shows up in a "x should { be a 'file and ..." type clause
    def a[S <: AnyRef](right: Symbol): Matcher[S] = apply(right)
    def an[S <: AnyRef](right: Symbol): Matcher[S] = apply(right)

    def apply(doubleTolerance: DoubleTolerance): Matcher[Double] =
      new Matcher[Double] {
        def apply(left: Double) = {
          import doubleTolerance._
          MatcherResult(
            left <= right + tolerance && left >= right - tolerance,
            FailureMessages("wasNotPlusOrMinus", left, right, tolerance),
            FailureMessages("wasPlusOrMinus", left, right, tolerance)
          )
        }
      }

    def theSameInstanceAs(right: AnyRef): Matcher[AnyRef] =
      new Matcher[AnyRef] {
        def apply(left: AnyRef) =
          MatcherResult(
            left eq right,
            FailureMessages("wasNotSameInstanceAs", left, right),
            FailureMessages("wasSameInstanceAs", left, right)
          )
      }

    def apply(right: Boolean) = 
      new Matcher[Boolean] {
        def apply(left: Boolean) =
          MatcherResult(
            left == right,
            FailureMessages("wasNot", left, right),
            FailureMessages("was", left, right)
          )
      }

    def apply(o: Null) = 
      new Matcher[AnyRef] {
        def apply(left: AnyRef) = {
          MatcherResult(
            left == null,
            FailureMessages("wasNotNull", left),
            FailureMessages("wasNull", left)
          )
        }
      }

    def apply(o: None.type) = 
      new Matcher[Option[_]] {
        def apply(left: Option[_]) = {
          MatcherResult(
            left == None,
            FailureMessages("wasNotNone", left),
            FailureMessages("wasNone", left)
          )
        }
      }
  
    def apply[S <: AnyRef](right: Symbol): Matcher[S] = {

      def matcherUsingReflection(left: S): MatcherResult = {

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
              FailureMessages(
                if (methodNameStartsWithVowel) "hasNeitherAnOrAnMethod" else "hasNeitherAOrAnMethod",
                left,
                UnquotedString(methodNameToInvoke),
                UnquotedString(methodNameToInvokeWithIs)
              )
            )
          case 1 =>
            val result = methodArray(0).invoke(left, Array[AnyRef]()).asInstanceOf[Boolean]
            MatcherResult(
              result,
              FailureMessages("wasNot", left, UnquotedString(rightNoTick)),
              FailureMessages("was", left, UnquotedString(rightNoTick))
            )
          case _ => // Should only ever be 2, but just in case
            throw new IllegalArgumentException(
              FailureMessages(
                if (methodNameStartsWithVowel) "hasBothAnAndAnMethod" else "hasBothAAndAnMethod",
                left,
                UnquotedString(methodNameToInvoke),
                UnquotedString(methodNameToInvokeWithIs)
              )
            )
        }
      }

      new Matcher[S] {
        def apply(left: S) = {
  
          left match {
            case leftString: String if right.toString == "'empty" => 
              MatcherResult(
                leftString.length == 0,
                FailureMessages("wasNotEmpty", left),
                FailureMessages("wasEmpty", left)
              )
            case _ => matcherUsingReflection(left)
          }
        }
      }
    }

    def apply(right: Nil.type): Matcher[List[_]] = equal(right)

    def apply(right: Any): Matcher[Any] =
      Helper.equalAndBeAnyMatcher(right, "was", "wasNot")
  }

  def not[S <: Any](matcher: Matcher[S]) = Helper.not { matcher }

/*
  def endWith[T <: String](right: T) =
    new Matcher[T] {
      def apply(left: T) =
        MatcherResult(
          left endsWith right,
          FailureMessages("didNotEndWith", left, right),
          FailureMessages("endedWith", left, right)
        )
    }
*/

  val behave = new BehaveWord
  val be = new BeWord

  def importSharedBehavior(behavior: Behavior)

  class Likifier[T](left: T) {
    def like(fun: (T) => Behavior) {
      importSharedBehavior(fun(left))
    }
  }

  def beNil: Matcher[List[_]] = Helper.equalAndBeAnyMatcher(Nil, "was", "wasNot")

  def beNull: Matcher[Any] = Helper.equalAndBeAnyMatcher(null, "was", "wasNot")

  def beEmpty: Matcher[AnyRef] = be.apply('empty)

  def beNone: Matcher[Option[_]] = be.apply(None)

  def beDefined: Matcher[AnyRef] = be.apply('defined)

  def beTrue: Matcher[Boolean] =
    new Matcher[Boolean] {
      def apply(left: Boolean) =
        MatcherResult(
          left,
          FailureMessages("booleanExpressionWasNot", true),
          FailureMessages("booleanExpressionWas", true)
        )
    }

  def beFalse: Matcher[Boolean] =
    new Matcher[Boolean] {
      def apply(left: Boolean) =
        MatcherResult(
          !left,
          FailureMessages("booleanExpressionWasNot", false),
          FailureMessages("booleanExpressionWas", false)
        )
    }

  def beSome[S](payload: S): Matcher[Option[S]] =
      new Matcher[Option[S]] {
        def apply(left: Option[S]) = {
          if (left.isEmpty) 
            MatcherResult(
              false,
              FailureMessages("wasNone", left),
              FailureMessages("wasSome", left)
            )
          else
            MatcherResult(
              left.get == payload,
              FailureMessages("wasSomeWrongValue", left, left.get),
              FailureMessages("wasSomeRightValue", left, left.get)
            )
        }
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
  val have = new HaveWord // TODO: Should I just make these singleton objects?
  val contain = new ContainWord // TODO: I think I forgot to do contain element x for the logical expressions
  val include = new IncludeWord
  val fullyMatch = new FullyMatchWord
  val startWith = new StartWithWord
  val endWith = new EndWithWord

  val anException: Class[Throwable] = classOf[Throwable]

  case class DoubleTolerance(right: Double, tolerance: Double)

  class HasPlusOrMinusForDouble(right: Double) {
    def plusOrMinus(tolerance: Double): DoubleTolerance = DoubleTolerance(right, tolerance)
  }

  implicit def doubleToHasPlusOrMinus(right: Double) = new HasPlusOrMinusForDouble(right)

  class HasExactlyForDouble(doubleValue: Double) {
    def exactly: Double = doubleValue
  }

  implicit def doubleToHasExactly(doubleValue: Double) = new HasExactlyForDouble(doubleValue)

  class HasExactlyForFloat(floatValue: Float) {
    def exactly: Float = floatValue
  }

  implicit def floatToHasExactly(floatValue: Float) = new HasExactlyForFloat(floatValue)
}

private[scalatest] trait ShouldMatchers extends BaseMatchers {

  private[scalatest] trait ShouldMethods[T] {
    protected val leftOperand: T
    def should(rightMatcher: Matcher[T]) {
      rightMatcher(leftOperand) match {
        case MatcherResult(false, failureMessage, _) => throw new AssertionError(failureMessage)
        case _ => ()
      }
    }
    def shouldNot(rightMatcher: Matcher[T]) {
      rightMatcher(leftOperand) match {
        case MatcherResult(true, _, failureMessage) => throw new AssertionError(failureMessage)
        case _ => ()
      }
    }
    // This one supports it should behave like
    def should(behaveWord: BehaveWord) = new Likifier[T](leftOperand)
    def should(beWord: BeWord): ResultOfBeWord[T] = new ResultOfBeWord(leftOperand, true)
    def shouldNot(beWord: BeWord): ResultOfBeWord[T] = new ResultOfBeWord(leftOperand, false)
    def shouldEqual(rightOperand: Any) {
      if (leftOperand != rightOperand) {
        throw new AssertionError(FailureMessages("didNotEqual", leftOperand, rightOperand))
      }
    }
    def shouldNotEqual(rightOperand: Any) {
      if (leftOperand == rightOperand) {
        throw new AssertionError(FailureMessages("equaled", leftOperand, rightOperand))
      }
    }
    def shouldMatch(rightOperand: PartialFunction[T, Boolean]) {
      if (rightOperand.isDefinedAt(leftOperand)) {
        val result = rightOperand(leftOperand)
        if (!result) {
          throw new AssertionError(FailureMessages("matchResultedInFalse", leftOperand))
        }
      }
      else {
        throw new AssertionError(FailureMessages("didNotMatch", leftOperand))
      }
    }
    def shouldNotMatch(rightOperand: PartialFunction[T, Boolean]) {
      if (rightOperand.isDefinedAt(leftOperand)) {
        val result = rightOperand(leftOperand)
        if (result) {
          throw new AssertionError(FailureMessages("matchResultedInTrue", leftOperand))
        }
      }
    }
  }

  private[scalatest] class ShouldalizerForBlocks(left: => Any) {
    def shouldThrow(clazz: java.lang.Class[_ <: AnyRef]): Throwable = { intercept(clazz)(left) }
    def shouldNotThrow(clazz: java.lang.Class[Throwable]) {
      try {
        left
      }
      catch {
        case u: Throwable =>
          val message = FailureMessages("anException", UnquotedString(u.getClass.getName))
          val ae = new AssertionError(message)
          ae.initCause(u)
          throw ae
      }
    }
  }

  private[scalatest] class Shouldalizer[T](left: T) extends { val leftOperand = left } with ShouldMethods[T]

  private[scalatest] class StringShouldalizer(left: String) extends { val leftOperand = left } with ShouldMethods[String] {
    def should(haveWord: HaveWord): ResultOfHaveWordForString = {
      new ResultOfHaveWordForString(left, true)
    }
    def shouldNot(haveWord: HaveWord): ResultOfHaveWordForString = {
      new ResultOfHaveWordForString(left, false)
    }
    def should(includeWord: IncludeWord): ResultOfIncludeWordForString = {
      new ResultOfIncludeWordForString(left, true)
    }
    def shouldNot(includeWord: IncludeWord): ResultOfIncludeWordForString = {
      new ResultOfIncludeWordForString(left, false)
    }
    def should(startWithWord: StartWithWord): ResultOfStartWithWordForString = {
      new ResultOfStartWithWordForString(left, true)
    }
    def shouldNot(startWithWord: StartWithWord): ResultOfStartWithWordForString = {
      new ResultOfStartWithWordForString(left, false)
    }
    def should(endWithWord: EndWithWord): ResultOfEndWithWordForString = {
      new ResultOfEndWithWordForString(left, true)
    }
    def shouldNot(endWithWord: EndWithWord): ResultOfEndWithWordForString = {
      new ResultOfEndWithWordForString(left, false)
    }
    def should(fullyMatchWord: FullyMatchWord): ResultOfFullyMatchWordForString = {
      new ResultOfFullyMatchWordForString(left, true)
    }
    def shouldNot(fullyMatchWord: FullyMatchWord): ResultOfFullyMatchWordForString = {
      new ResultOfFullyMatchWordForString(left, false)
    }
  }

  private[scalatest] class MapShouldalizer[K, V](left: Map[K, V]) extends { val leftOperand = left } with ShouldMethods[Map[K, V]] {
    def should(containWord: ContainWord): ResultOfContainWordForIterable[(K, V)] = {
      new ResultOfContainWordForIterable(left, true)
    }
    def shouldNot(containWord: ContainWord): ResultOfContainWordForIterable[(K, V)] = {
      new ResultOfContainWordForIterable(left, false)
    }
    def should(haveWord: HaveWord): ResultOfHaveWordForMap[K, V] = {
      new ResultOfHaveWordForMap(left, true)
    }
    def shouldNot(haveWord: HaveWord): ResultOfHaveWordForMap[K, V] = {
      new ResultOfHaveWordForMap(left, false)
    }
  }
  
  private[scalatest] trait ShouldContainWordForIterableMethods[T] {
    protected val leftOperand: Iterable[T]
    def should(containWord: ContainWord): ResultOfContainWordForIterable[T] = {
      new ResultOfContainWordForIterable(leftOperand, true)
    }
    def shouldNot(containWord: ContainWord): ResultOfContainWordForIterable[T] = {
      new ResultOfContainWordForIterable(leftOperand, false)
    }
  }
  
  private[scalatest] class IterableShouldalizer[T](left: Iterable[T]) extends { val leftOperand = left } with ShouldMethods[Iterable[T]]
      with ShouldContainWordForIterableMethods[T]
  
  private[scalatest] trait ShouldHaveWordForCollectionMethods[T] {
    protected val leftOperand: Collection[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForCollection[T] = {
      new ResultOfHaveWordForCollection(leftOperand, true)
    }
    def shouldNot(haveWord: HaveWord): ResultOfHaveWordForCollection[T] = {
      new ResultOfHaveWordForCollection(leftOperand, false)
    }
  }
  
  private[scalatest] trait ShouldHaveWordForSeqMethods[T] {
    protected val leftOperand: Seq[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForSeq[T] = {
      new ResultOfHaveWordForSeq(leftOperand, true)
    }
    def shouldNot(haveWord: HaveWord): ResultOfHaveWordForSeq[T] = {
      new ResultOfHaveWordForSeq(leftOperand, false)
    }
  }
  
  private[scalatest] class CollectionShouldalizer[T](left: Collection[T]) extends { val leftOperand = left } with ShouldMethods[Collection[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForCollectionMethods[T]
  
  private[scalatest] class SeqShouldalizer[T](left: Seq[T]) extends { val leftOperand = left } with ShouldMethods[Seq[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]
  
  private[scalatest] class ArrayShouldalizer[T](left: Array[T]) extends { val leftOperand = left } with ShouldMethods[Array[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]
  
  private[scalatest] class ListShouldalizer[T](left: List[T]) extends { val leftOperand = left } with ShouldMethods[List[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]
  
  implicit def shouldify[T](o: T): Shouldalizer[T] = new Shouldalizer(o)
  implicit def shouldifyForMap[K, V](o: Map[K, V]): MapShouldalizer[K, V] = new MapShouldalizer[K, V](o)
  implicit def shouldifyForCollection[T](o: Collection[T]): CollectionShouldalizer[T] = new CollectionShouldalizer[T](o)
  implicit def shouldifyForSeq[T](o: Seq[T]): SeqShouldalizer[T] = new SeqShouldalizer[T](o)
  implicit def shouldifyForArray[T](o: Array[T]): ArrayShouldalizer[T] = new ArrayShouldalizer[T](o)
  implicit def shouldifyForList[T](o: List[T]): ListShouldalizer[T] = new ListShouldalizer[T](o)
  implicit def shouldifyForString[K, V](o: String): StringShouldalizer = new StringShouldalizer(o)
  implicit def theBlock(f: => Any) = new ShouldalizerForBlocks(f)
}

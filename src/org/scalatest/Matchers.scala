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

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import scala.util.matching.Regex
import java.lang.reflect.Field

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
of the implicit conversion will be a ShouldWrapper.

The should methods take different static types, so they are overloaded. These types don't all
inherit from the same supertype. There's a plain-old Matcher for example, but there's also maybe
a BeMatcher, and BeMatcher doesn't extend Matcher. This reduces the number of incorrect static
matches, which can happen if a more specific type is held from a more general variable type.
And reduces the chances for ambiguity, I suspect.
*/

private[scalatest] object Helper {

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

trait Matchers extends Assertions { matchers =>

  //
  // This class is used as the return type of the overloaded should method (in MapShouldWrapper)
  // that takes a HaveWord. It's key method will be called in situations like this:
  //
  // map should have key 1
  //
  // This gets changed to :
  //
  // convertToMapShouldWrapper(map).should(have).key(1)
  //
  // Thus, the map is wrapped in a convertToMapShouldWrapper call via an implicit conversion, which results in
  // a MapShouldWrapper. This has a should method that takes a HaveWord. That method returns a
  // ResultOfHaveWordPassedToShould that remembers the map to the left of should. Then this class
  // ha a key method that takes a K type, they key type of the map. It does the assertion thing.
  // 
  protected class ResultOfContainWordForMap[K, V](left: scala.collection.Map[K, V], shouldBeTrue: Boolean) extends ResultOfContainWordForIterable[Tuple2[K, V]](left, shouldBeTrue) {
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
  }
  
  protected class BehaveWord
  protected class ContainWord {
    def element[T](expectedElement: T): Matcher[Iterable[T]] =
      new Matcher[Iterable[T]] {
        def apply(left: Iterable[T]) =
          MatcherResult(
            left.elements.contains(expectedElement), 
            FailureMessages("didNotContainExpectedElement", left, expectedElement),
            FailureMessages("containedExpectedElement", left, expectedElement)
          )
      }

    //
    // This key method is called when "contain" is used in a logical expression, such as:
    // map should { contain key 1 and equal (Map(1 -> "Howdy")) }. It results in a matcher
    // that remembers the key value. By making the value type Any, it causes overloaded shoulds
    // to work, because for example a Matcher[Map[Int, Any]] is a subtype of Matcher[Map[Int, String]],
    // given Map is covariant in its V (the value type stored in the map) parameter and Matcher is
    // contravariant in its lone type parameter. Thus, the type of the Matcher resulting from contain key 1
    // is a subtype of the map type that has a known value type parameter because its that of the map
    // to the left of should. This means the should method that takes a map will be selected by Scala's
    // method overloading rules.
    //
    def key[K](expectedKey: K): Matcher[scala.collection.Map[K, Any]] =
      new Matcher[scala.collection.Map[K, Any]] {
        def apply(left: scala.collection.Map[K, Any]) =
          MatcherResult(
            left.contains(expectedKey),
            FailureMessages("didNotHaveKey", left, expectedKey),
            FailureMessages("hadKey", left, expectedKey)
          )
      }

    // Holy smokes I'm starting to scare myself. I fixed the problem of the compiler not being
    // able to infer the value type in contain value 1 and ... like expressions, because the
    // value type is there, with an existential type. Since I don't know what K is, I decided to
    // try just saying that with an existential type, and it compiled and ran. Pretty darned
    // amazing compiler. The problem could not be fixed like I fixed the key method above, because
    // Maps are nonvariant in their key type parameter, whereas they are covariant in their value
    // type parameter, so the same trick wouldn't work. But this existential type trick seems to
    // work like a charm.
    def value[V](expectedValue: V): Matcher[scala.collection.Map[K, V] forSome { type K }] =
      new Matcher[scala.collection.Map[K, V] forSome { type K }] {
        def apply(left: scala.collection.Map[K, V] forSome { type K }) =
          MatcherResult(
            left.values.contains(expectedValue),
            FailureMessages("didNotHaveValue", left, expectedValue),
            FailureMessages("hadValue", left, expectedValue)
          )
      }
  }

  protected class IncludeWord {
    def substring(expectedSubstring: String): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) =
          MatcherResult(
            left.indexOf(expectedSubstring) >= 0, 
            FailureMessages("didNotIncludeSubstring", left, expectedSubstring),
            FailureMessages("includedSubstring", left, expectedSubstring)
          )
      }
    def regex[T <: String](right: T): Matcher[T] = regex(right.r)
    def regex(expectedRegex: Regex): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) =
          MatcherResult(
            expectedRegex.findFirstIn(left).isDefined,
            FailureMessages("didNotIncludeRegex", left, expectedRegex),
            FailureMessages("includedRegex", left, expectedRegex)
          )
      }
  }

  protected class StartWithWord {
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

  protected class EndWithWord {
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

  protected class FullyMatchWord {
    def regex(rightRegexString: String): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) =
          MatcherResult(
            java.util.regex.Pattern.matches(rightRegexString, left),
            FailureMessages("didNotFullyMatchRegex", left, UnquotedString(rightRegexString)),
            FailureMessages("fullyMatchedRegex", left, UnquotedString(rightRegexString))
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

  trait LengthWrapper {
    def length: Long
  }

  implicit def convertLengthFieldToIntLengthWrapper(o: { val length: Int }) =
    new LengthWrapper {
      def length = o.length
    }

  implicit def convertLengthMethodToIntLengthWrapper(o: { def length(): Int }) =
    new LengthWrapper {
      def length = o.length()
    }

  implicit def convertGetLengthFieldToIntLengthWrapper(o: { val getLength: Int }) =
    new LengthWrapper {
      def length = o.getLength
    }

  implicit def convertGetLengthMethodToIntLengthWrapper(o: { def getLength(): Int }) =
    new LengthWrapper {
      def length = o.getLength()
    }

  implicit def convertLengthFieldToLongLengthWrapper(o: { val length: Long }) =
    new LengthWrapper {
      def length = o.length
    }

  implicit def convertLengthMethodToLongLengthWrapper(o: { def length(): Long }) =
    new LengthWrapper {
      def length = o.length()
    }

  implicit def convertGetLengthFieldToLongLengthWrapper(o: { val getLength: Long }) =
    new LengthWrapper {
      def length = o.getLength
    }

  implicit def convertGetLengthMethodToLongLengthWrapper(o: { def getLength(): Long }) =
    new LengthWrapper {
      def length = o.getLength()
    }

  trait SizeWrapper {
    def size: Long
  }

  implicit def convertSizeFieldToIntSizeWrapper(o: { val size: Int }) =
    new SizeWrapper {
      def size = o.size
    }

  implicit def convertSizeMethodToIntSizeWrapper(o: { def size(): Int }) =
    new SizeWrapper {
      def size = o.size()
    }

  implicit def convertGetSizeFieldToIntSizeWrapper(o: { val getSize: Int }) =
    new SizeWrapper {
      def size = o.getSize
    }

  implicit def convertGetSizeMethodToIntSizeWrapper(o: { def getSize(): Int }) =
    new SizeWrapper {
      def size = o.getSize()
    }

  implicit def convertSizeFieldToLongSizeWrapper(o: { val size: Long }) =
    new SizeWrapper {
      def size = o.size
    }

  implicit def convertSizeMethodToLongSizeWrapper(o: { def size(): Long }) =
    new SizeWrapper {
      def size = o.size()
    }

  implicit def convertGetSizeFieldToLongSizeWrapper(o: { val getSize: Long }) =
    new SizeWrapper {
      def size = o.getSize
    }

  implicit def convertGetSizeMethodToLongSizeWrapper(o: { def getSize(): Long }) =
    new SizeWrapper {
      def size = o.getSize()
    }

  protected class HaveWord {

    // I couldn't figure out how to combine view bounds with existential types. May or may not
    // be possible, but going dynamic for now at least.
    def length(expectedLength: Long) =
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
            case leftJavaList: java.util.List[_] =>
              MatcherResult(
                leftJavaList.size == expectedLength,
                FailureMessages("didNotHaveExpectedLength", left, expectedLength),
                FailureMessages("hadExpectedLength", left, expectedLength)
              )
            case _ =>

              // I'm only checking for public methods here. Maybe it should also do package access, protected, etc.
              // if it is accessible, it would work.
              def isMethodToInvoke(method: Method): Boolean =
                (method.getName == "length" || method.getName == "getLength") &&
                    method.getParameterTypes.length == 0 && !Modifier.isStatic(method.getModifiers()) &&
                    (method.getReturnType == classOf[Int] || method.getReturnType == classOf[Long])

              def isFieldToAccess(field: Field): Boolean =
                (field.getName == "length" || field.getName == "getLength") &&
                (field.getType == classOf[Int] || field.getType == classOf[Long])

              val methodArray =
                for (method <- left.getClass.getMethods; if isMethodToInvoke(method))
                 yield method
              //YYY
              val fieldArray =
                for (field <- left.getClass.getFields; if isFieldToAccess(field))
                 yield field // rhymes: code as poetry

              (methodArray.length, fieldArray.length) match {

                case (0, 0) =>
                  throw new AssertionError(Resources("noLengthStructure", expectedLength.toString))

                case (0, 1) => // Has either a length or getLength field
                  val field = fieldArray(0)
                  val value: Long = if (field.getType == classOf[Int]) field.getInt(left) else field.getLong(left)
                  MatcherResult(
                    value == expectedLength,
                    FailureMessages("didNotHaveExpectedLength", left, expectedLength),
                    FailureMessages("hadExpectedLength", left, expectedLength)
                  )

                case (1, 0) => // Has either a length or getLength method
                  val method = methodArray(0)
                  val result: Long =
                    if (method.getReturnType == classOf[Int])
                      method.invoke(left, Array[AnyRef](): _*).asInstanceOf[Int]
                    else
                      method.invoke(left, Array[AnyRef](): _*).asInstanceOf[Long]

                  MatcherResult(
                    result == expectedLength,
                    FailureMessages("didNotHaveExpectedLength", left, expectedLength),
                    FailureMessages("hadExpectedLength", left, expectedLength)
                  )

                case _ => // too many
                  throw new IllegalArgumentException(Resources("lengthAndGetLength", expectedLength.toString))
              }
          }
      }

    def size(expectedSize: Long) =
      new Matcher[AnyRef] {
        def apply(left: AnyRef) =
          left match {
            case leftSeq: Collection[_] =>
              MatcherResult(
                leftSeq.size == expectedSize, 
                FailureMessages("didNotHaveExpectedSize", left, expectedSize),
                FailureMessages("hadExpectedSize", left, expectedSize)
              )
            case leftJavaList: java.util.List[_] =>
              MatcherResult(
                leftJavaList.size == expectedSize,
                FailureMessages("didNotHaveExpectedSize", left, expectedSize),
                FailureMessages("hadExpectedSize", left, expectedSize)
              )
            case _ =>

              // I'm only checking for public methods here. Maybe it should also do package access, protected, etc.
              // if it is accessible, it would work.
              def isMethodToInvoke(method: Method): Boolean =
                (method.getName == "size" || method.getName == "getSize") &&
                    method.getParameterTypes.size == 0 && !Modifier.isStatic(method.getModifiers()) &&
                    (method.getReturnType == classOf[Int] || method.getReturnType == classOf[Long])

              def isFieldToAccess(field: Field): Boolean =
                (field.getName == "size" || field.getName == "getSize") &&
                (field.getType == classOf[Int] || field.getType == classOf[Long])

              val methodArray =
                for (method <- left.getClass.getMethods; if isMethodToInvoke(method))
                 yield method
              //YYY
              val fieldArray =
                for (field <- left.getClass.getFields; if isFieldToAccess(field))
                 yield field // rhymes: code as poetry

              (methodArray.size, fieldArray.size) match {

                case (0, 0) =>
                  throw new AssertionError(Resources("noSizeStructure", expectedSize.toString))

                case (0, 1) => // Has either a size or getSize field
                  val field = fieldArray(0)
                  val value: Long = if (field.getType == classOf[Int]) field.getInt(left) else field.getLong(left)
                  MatcherResult(
                    value == expectedSize,
                    FailureMessages("didNotHaveExpectedSize", left, expectedSize),
                    FailureMessages("hadExpectedSize", left, expectedSize)
                  )

                case (1, 0) => // Has either a size or getSize method
                  val method = methodArray(0)
                  val result: Long =
                    if (method.getReturnType == classOf[Int])
                      method.invoke(left, Array[AnyRef](): _*).asInstanceOf[Int]
                    else
                      method.invoke(left, Array[AnyRef](): _*).asInstanceOf[Long]

                  MatcherResult(
                    result == expectedSize,
                    FailureMessages("didNotHaveExpectedSize", left, expectedSize),
                    FailureMessages("hadExpectedSize", left, expectedSize)
                  )

                case _ => // too many
                  throw new IllegalArgumentException(Resources("sizeAndGetSize", expectedSize.toString))
              }
          }
      }
  }
  
  //
  // This class is used as the return type of the overloaded should method (in CollectionShouldWrapper)
  // that takes a HaveWord. It's size method will be called in situations like this:
  //
  // list should have size 1
  //
  // This gets changed to :
  //
  // convertToCollectionShouldWrapper(list).should(have).size(1)
  //
  // Thus, the list is wrapped in a convertToCollectionShouldWrapper call via an implicit conversion, which results in
  // a CollectionShouldWrapper. This has a should method that takes a HaveWord. That method returns a
  // ResultOfHaveWordForCollectionPassedToShould that remembers the map to the left of should. Then this class
  // has a size method that takes a T type, type parameter of the iterable. It does the assertion thing.
  // 
  protected class ResultOfHaveWordForCollection[T](left: Collection[T], shouldBeTrue: Boolean) {
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
  
  protected class ResultOfHaveWordForJavaCollection[T](left: java.util.Collection[T], shouldBeTrue: Boolean) {
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

  protected class ResultOfHaveWordForSeq[T](left: Seq[T], shouldBeTrue: Boolean) extends ResultOfHaveWordForCollection[T](left, shouldBeTrue) {
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
  
  protected class ResultOfHaveWordForJavaList[T](left: java.util.List[T], shouldBeTrue: Boolean) extends ResultOfHaveWordForJavaCollection[T](left, shouldBeTrue) {
    def length(expectedLength: Int) {
      if ((left.size == expectedLength) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
            left,
            expectedLength)
        )
    }
  }

  protected class ResultOfBeWordForAnyRef(left: AnyRef, shouldBeTrue: Boolean) {
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

  // What's this one for again?
  implicit def resultOfBeWordToForAnyRef[T <: AnyRef](resultOfBeWord: ResultOfBeWord[T]): ResultOfBeWordForAnyRef =
    new ResultOfBeWordForAnyRef(resultOfBeWord.left, resultOfBeWord.shouldBeTrue)

  protected class ResultOfBeWord[T](val left: T, val shouldBeTrue: Boolean) {
    def a[S <: AnyRef](right: Symbol): Matcher[S] = be(right)
    def an[S <: AnyRef](right: Symbol): Matcher[S] = be(right)
  }

  protected class ResultOfNotWord[T](val left: T, val shouldBeTrue: Boolean) {
    def equal(right: Any) {
      if ((left == right) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
           if (shouldBeTrue) "didNotEqual" else "equaled",
            left,
            right
          )
        )
    }
  }
  protected class ResultOfNotWordForString(lowerLeft: String, lowerShouldBeTrue: Boolean)
      extends ResultOfNotWord[String](lowerLeft, lowerShouldBeTrue) {

    def have(resultOfLengthWordApplication: ResultOfLengthWordApplication) {
      val right = resultOfLengthWordApplication.expectedLength
      if ((left.length == right) != shouldBeTrue) {
          throw new AssertionError(
            FailureMessages(
             if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
              left,
              right
            )
          )
      }
    }
  }

  protected class ResultOfHaveWordForString(left: String, shouldBeTrue: Boolean) {
    def length(expectedLength: Int) {
      if ((left.length == expectedLength) != shouldBeTrue)
        throw new AssertionError(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
            left,
            expectedLength
          )
        )
    }
  }
  
  protected class ResultOfIncludeWordForString(left: String, shouldBeTrue: Boolean) {
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

  protected class ResultOfStartWithWordForString(left: String, shouldBeTrue: Boolean) {
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

  protected class ResultOfEndWithWordForString(left: String, shouldBeTrue: Boolean) {
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

  protected class ResultOfFullyMatchWordForString(left: String, shouldBeTrue: Boolean) {
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
  
  protected class ResultOfContainWordForIterable[T](left: Iterable[T], shouldBeTrue: Boolean) {
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
  
  def equal(right: Any): Matcher[Any] =
    Helper.equalAndBeAnyMatcher(right, "equaled", "didNotEqual")

  protected class TreatedAsOrderedWrapper {
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

  // This one is for one should be < (7)
  implicit def convertBeWordToForOrdered(beWord: BeWord): TreatedAsOrderedWrapper = new TreatedAsOrderedWrapper

  protected class BeWord {

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
        // XXX
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
            val result = methodArray(0).invoke(left, Array[AnyRef](): _*).asInstanceOf[Boolean]
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
        def apply(left: S) = matcherUsingReflection(left)
      }
    }

    def apply(right: Nil.type): Matcher[List[_]] =
      new Matcher[List[_]] {
        def apply(left: List[_]) = {
          MatcherResult(
            left == Nil,
            FailureMessages("wasNotNil", left),
            FailureMessages("wasNil", left)
          )
        }
      }

    def apply(right: Any): Matcher[Any] =
      Helper.equalAndBeAnyMatcher(right, "was", "wasNot")
  }


/*  def not[S <: Any](matcher: Matcher[S]) =
    new Matcher[S] {
      def apply(left: S) =
        matcher(left) match {
          case MatcherResult(bool, s1, s2) => MatcherResult(!bool, s2, s1)
        }
    }*/

  class NotWord {

    def apply[S <: Any](matcher: Matcher[S]) =
      new Matcher[S] {
        def apply(left: S) =
          matcher(left) match {
            case MatcherResult(bool, s1, s2) => MatcherResult(!bool, s2, s1)
          }
      }

    def equal(right: Any): Matcher[Any] = apply(matchers.equal(right))
  }

  val not = new NotWord
  val behave = new BehaveWord
  val be = new BeWord

  class ResultOfBehaveWord[T](left: T) {
    def like(fun: (T) => Unit) {
      fun(left)
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
  val contain = new ContainWord
  val include = new IncludeWord
  val fullyMatch = new FullyMatchWord
  val startWith = new StartWithWord
  val endWith = new EndWithWord

  class ResultOfLengthWordApplication(val expectedLength: Long)

  class LengthWord {
    def apply(expectedLength: Long) = new ResultOfLengthWordApplication(expectedLength)
  }

  val length = new LengthWord
    
  case class DoubleTolerance(right: Double, tolerance: Double)

  class PlusOrMinusWrapper(right: Double) {
    def plusOrMinus(tolerance: Double): DoubleTolerance = DoubleTolerance(right, tolerance)
  }

  implicit def convertDoubleToPlusOrMinusWrapper(right: Double) = new PlusOrMinusWrapper(right)

    class ResultOfHaveWordForLengthWrapper[A <% LengthWrapper](left: A, shouldBeTrue: Boolean) {
      def length(expectedLength: Int) {
        if ((left.length == expectedLength) != shouldBeTrue)
          throw new AssertionError(
            FailureMessages(
              if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
              left,
              expectedLength)
          )
      }
      def length(expectedLength: Long) {
        if ((left.length == expectedLength) != shouldBeTrue)
          throw new AssertionError(
            FailureMessages(
              if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
              left,
              expectedLength)
          )
      }
    }

    class ResultOfHaveWordForSizeWrapper[A <% SizeWrapper](left: A, shouldBeTrue: Boolean) {
      def size(expectedSize: Int) {
        if ((left.size == expectedSize) != shouldBeTrue)
          throw new AssertionError(
            FailureMessages(
              if (shouldBeTrue) "didNotHaveExpectedSize" else "hadExpectedSize",
              left,
              expectedSize)
          )
      }
      def size(expectedSize: Long) {
        if ((left.size == expectedSize) != shouldBeTrue)
          throw new AssertionError(
            FailureMessages(
              if (shouldBeTrue) "didNotHaveExpectedSize" else "hadExpectedSize",
              left,
              expectedSize)
          )
      }
    }
}


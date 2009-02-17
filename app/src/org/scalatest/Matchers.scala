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
  def newTestFailedException(message: String): TestFailedException = {
    val fileNames = List("Matchers.scala", "ShouldMatchers.scala", "MustMatchers.scala")
    val temp = new RuntimeException
    val stackDepth = temp.getStackTrace.takeWhile(stackTraceElement => fileNames.exists(_ == stackTraceElement.getFileName)).length
    new TestFailedException(message, stackDepth)
  }
}

import Helper.newTestFailedException

trait Matchers extends Assertions { matchers =>

  private def matchSymbolToPredicateMethod[S <: AnyRef](left: S, right: Symbol, hasArticle: Boolean, articleIsA: Boolean): MatcherResult = {

    def transformOperatorChars(s: String) = {
      val builder = new StringBuilder
      for (i <- 0 until s.length) {
        val ch = s.charAt(i)
        val replacement =
          ch match {
            case '!' => "$bang"
            case '#' => "$hash"
            case '~' => "$tilde"
            case '|' => "$bar"
            case '^' => "$up"
            case '\\' => "$bslash"
            case '@' => "$at"
            case '?' => "$qmark"
            case '>' => "$greater"
            case '=' => "$eq"
            case '<' => "$less"
            case ':' => "$colon"
            case '/' => "$div"
            case '-' => "$minus"
            case '+' => "$plus"
            case '*' => "$times"
            case '&' => "$amp"
            case '%' => "$percent"
            case _ => ""
          }

        if (replacement.length > 0)
          builder.append(replacement)
        else
          builder.append(ch)
      }
      builder.toString
    }

    // If 'empty passed, rightNoTick would be "empty"
    val rightNoTick = right.name

    // methodNameToInvoke would also be "empty"
    val methodNameToInvoke = transformOperatorChars(rightNoTick)

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
        val result = methodArray(0).invoke(left, Array[AnyRef](): _*).asInstanceOf[Boolean]
        val (wasNot, was) =
          if (hasArticle) {
            if (articleIsA) ("wasNotA", "wasA") else ("wasNotAn", "wasAn")
          }
          else ("wasNot", "was")
        MatcherResult(
          result,
          FailureMessages(wasNot, left, UnquotedString(rightNoTick)),
          FailureMessages(was, left, UnquotedString(rightNoTick))
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

  class MatcherWrapper[T](leftMatcher: Matcher[T]) { matchersWrapper =>

    /**
     * Returns a matcher whose <code>apply</code> method returns a <code>MatcherResult</code>
     * that represents the logical-and of the results of the wrapped and the passed matcher applied to
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
    def and[U <: T](rightMatcher: Matcher[U]): Matcher[U] =
      new Matcher[U] {
        def apply(left: U) = {
          val leftMatcherResult = leftMatcher(left)
          val rightMatcherResult = rightMatcher(left) // Not short circuiting anymore
          if (!leftMatcherResult.matches)
            MatcherResult(
              false,
              leftMatcherResult.failureMessage,
              leftMatcherResult.negativeFailureMessage
            )
          else {
            MatcherResult(
              rightMatcherResult.matches,
              Resources("commaBut", leftMatcherResult.negativeFailureMessage, rightMatcherResult.failureMessage),
              Resources("commaAnd", leftMatcherResult.negativeFailureMessage, rightMatcherResult.negativeFailureMessage)
            )
          }
        }
      }

    class AndHaveWord {
      def length(expectedLength: Long) = and(have.length(expectedLength))
      // Array(1, 2) should (have size (2) and have size (3 - 1))
      //                                       ^
      def size(expectedSize: Long) = and(have.size(expectedSize))
    }

    def and(haveWord: HaveWord): AndHaveWord = new AndHaveWord

    class AndContainWord {

      // Array(1, 2) should (contain element (2) and contain element (3 - 1))
      //                                                     ^
      def element[T](expectedElement: T) = matchersWrapper.and(matchers.contain.element(expectedElement))
    }

    def and(containWord: ContainWord): AndContainWord = new AndContainWord

    class AndBeWord {

      // isFileMock should (be a ('file) and be a ('file))
      //                                        ^
      def a(symbol: Symbol) = and(be.a(symbol))

      // isAppleMock should (be an ('apple) and be an ('apple))
      //                                           ^
      def an(symbol: Symbol) = and(be.an(symbol))

      // obj should (be theSameInstanceAs (string) and be theSameInstanceAs (string))
      //                                                  ^
      def theSameInstanceAs(anyRef: AnyRef) = and(be.theSameInstanceAs(anyRef))
    }

    // isFileMock should (be a ('file) and be a ('file))
    //                                 ^
    def and(beWord: BeWord): AndBeWord = new AndBeWord

    class AndFullyMatchWord {
      // "1.7" should (fullyMatch regex (decimal) and fullyMatch regex (decimal))
      //                                                         ^
      def regex(regexString: String) = and(fullyMatch.regex(regexString))

      // "1.7" should (fullyMatch regex (decimalRegex) and fullyMatch regex (decimalRegex))
      //                                                              ^
      def regex(regex: Regex) = and(fullyMatch.regex(regex))
    }

    def and(fullyMatchWord: FullyMatchWord): AndFullyMatchWord = new AndFullyMatchWord

    class AndIncludeWord {
      // "1.7" should (include regex (decimal) and include regex (decimal))
      //                                                   ^
      def regex(regexString: String) = and(include.regex(regexString))

      // "1.7" should (include regex (decimalRegex) and include regex (decimalRegex))
      //                                                        ^
      def regex(regex: Regex) = and(include.regex(regex))

      // "a1.7b" should (include substring ("1.7") and include substring ("1.7"))
      //                                                       ^
      def substring(expectedSubstring: String) = and(include.substring(expectedSubstring))
    }

    def and(includeWord: IncludeWord): AndIncludeWord = new AndIncludeWord

    class AndStartWithWord {
      // "1.7" should (startWith regex (decimal) and startWith regex (decimal))
      //                                                        ^
      def regex(regexString: String) = and(startWith.regex(regexString))

      // "1.7" should (startWith regex (decimalRegex) and startWith regex (decimalRegex))
      //                                                            ^
      def regex(regex: Regex) = and(startWith.regex(regex))

      // "1.7" should (startWith substring ("1.7") and startWith substring ("1.7"))
      //                                                         ^
      def substring(expectedSubstring: String) = and(startWith.substring(expectedSubstring))
    }

    def and(startWithWord: StartWithWord): AndStartWithWord = new AndStartWithWord

    class AndEndWithWord {
      // "1.7" should (endWith regex (decimal) and endWith regex (decimal))
      //                                                   ^
      def regex(regexString: String) = and(endWith.regex(regexString))

      // "1.7" should (endWith regex (decimalRegex) and endWith regex (decimalRegex))
      //                                                        ^
      def regex(regex: Regex) = and(endWith.regex(regex))

      // "1.7b" should ((endWith substring ("1.7b")) and (endWith substring ("7b")))
      //                                
      def substring(expectedSubstring: String) = and(endWith.substring(expectedSubstring))
    }

    def and(endWithWord: EndWithWord): AndEndWithWord = new AndEndWithWord

    class AndNotWord {

      // 1 should (not equal (2) and not equal (3 - 1)) The second half, after "not"
      def equal(any: Any) =
        matchersWrapper.and(matchers.not.apply(matchers.equal(any)))

      // By-name parameter is to get this to short circuit:
      // "hi" should (have length (1) and not have length {mockClown.hasBigRedNose; 1})
      // I had to do it this way to support short-circuiting after and, because i need to use by-name parameters
      // that result in the two subclasses ResultOfLengthWordApplication and ResultOfSizeWordApplication. The by-name
      // param ends up as a type Function0[Unit] I think, and so these two don't overload because the type is the
      // same after erasure (they are both Function0). Darn. So I just make one
      // of the superclass type, ResultOfLengthOrSizeWordApplication, and then do a pattern match. At first I tried
      // to do it the OO way and have an rather ugly expectedLengthOrSize val set by each subclass, but I needed to
      // konw whether it was length or size to be able to call length or size to get the appropriate error message on
      // a failure. TODO: Since I'm not short circuiting anymore, can I simplify this?
      def have(resultOfLengthOrSizeWordApplication: ResultOfLengthOrSizeWordApplication) =
        matchersWrapper.and(
          matchers.not.apply(
            resultOfLengthOrSizeWordApplication match {
              case resultOfLengthWordApplication: ResultOfLengthWordApplication =>
                matchers.have.length(resultOfLengthWordApplication.expectedLength)
              case resultOfSizeWordApplication: ResultOfSizeWordApplication =>
                matchers.have.size(resultOfSizeWordApplication.expectedSize)
            }
          )
        )

      def be[T](resultOfLessThanComparison: ResultOfLessThanComparison[T]) =
        matchersWrapper.and(matchers.not.be(resultOfLessThanComparison))

      def be[T](resultOfGreaterThanComparison: ResultOfGreaterThanComparison[T]) =
        matchersWrapper.and(matchers.not.be(resultOfGreaterThanComparison))

      def be[T](resultOfLessThanOrEqualToComparison: ResultOfLessThanOrEqualToComparison[T]) =
        matchersWrapper.and(matchers.not.be(resultOfLessThanOrEqualToComparison))

      def be[T](resultOfGreaterThanOrEqualToComparison: ResultOfGreaterThanOrEqualToComparison[T]) =
        matchersWrapper.and(matchers.not.be(resultOfGreaterThanOrEqualToComparison))

      // notEmptyMock should (not be ('empty) and not be ('empty))
      //                                              ^
      def be[T](symbol: Symbol) = matchersWrapper.and(matchers.not.be(symbol))

      // isNotFileMock should (not be a ('file) and not be a ('file))
      //                                                ^
      def be[T](resultOfAWordApplication: ResultOfAWordApplication) = matchersWrapper.and(matchers.not.be(resultOfAWordApplication))

      // isNotAppleMock should (not be an ('apple) and not be an ('apple)) 
      //                                                   ^
      def be[T](resultOfAnWordApplication: ResultOfAnWordApplication) = matchersWrapper.and(matchers.not.be(resultOfAnWordApplication))

      // obj should (not be theSameInstanceAs (otherString) and not be theSameInstanceAs (otherString))
      //                                                            ^
      def be[T](resultOfTheSameInstanceAsApplication: ResultOfTheSameInstanceAsApplication) = matchersWrapper.and(matchers.not.be(resultOfTheSameInstanceAsApplication))

      // sevenDotOh should (not be (17.0 plusOrMinus 0.2) and not be (17.0 plusOrMinus 0.2))
      //                                                          ^
      def be(doubleTolerance: DoubleTolerance) = matchersWrapper.and(matchers.not.be(doubleTolerance))

      // sevenDotOhFloat should (not be (17.0f plusOrMinus 0.2f) and not be (17.0f plusOrMinus 0.2f))
      //                                                                 ^
      def be(floatTolerance: FloatTolerance) = matchersWrapper.and(matchers.not.be(floatTolerance))

      // sevenLong should (not be (17L plusOrMinus 2L) and not be (17L plusOrMinus 2L))
      //                                                       ^
      def be(longTolerance: LongTolerance) = matchersWrapper.and(matchers.not.be(longTolerance))

      // sevenInt should (not be (17 plusOrMinus 2) and not be (17 plusOrMinus 2))
      //                                                    ^
      def be(intTolerance: IntTolerance) = matchersWrapper.and(matchers.not.be(intTolerance))

      // sevenShort should (not be (17.toShort plusOrMinus 2.toShort) and not be (17.toShort plusOrMinus 2.toShort))
      //                                                                      ^
      def be(shortTolerance: ShortTolerance) = matchersWrapper.and(matchers.not.be(shortTolerance))

      // sevenByte should ((not be (19.toByte plusOrMinus 2.toByte)) and (not be (19.toByte plusOrMinus 2.toByte)))
      //                                                                      ^
      def be(byteTolerance: ByteTolerance) = matchersWrapper.and(matchers.not.be(byteTolerance))

      // "fred" should (not fullyMatch regex ("bob") and not fullyMatch regex (decimal))
      //                                                     ^
      def fullyMatch(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.and(matchers.not.fullyMatch(resultOfRegexWordApplication))

      // "fred" should (not include regex ("bob") and not include regex (decimal))
      //                                                     ^
      def include(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.and(matchers.not.include(resultOfRegexWordApplication))

      // "fred" should (not include substring ("bob") and not include substring ("1.7"))
      //                                                      ^
      def include(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) =
        matchersWrapper.and(matchers.not.include(resultOfSubstringWordApplication))

      // "fred" should (not startWith regex ("bob") and not startWith regex (decimal))
      //                                                    ^
      def startWith(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.and(matchers.not.startWith(resultOfRegexWordApplication))

      // "fred" should (not startWith substring ("red") and not startWith substring ("1.7"))
      //                                                        ^
      def startWith(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) =
        matchersWrapper.and(matchers.not.startWith(resultOfSubstringWordApplication))

      // "fred" should (not endWith regex ("bob") and not endWith regex (decimal))
      //                                                  ^
      def endWith(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.and(matchers.not.endWith(resultOfRegexWordApplication))

      // "fred" should (not endWith substring ("fre") and not endWith substring ("1.7"))
      //                                                      ^
      def endWith(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) =
        matchersWrapper.and(matchers.not.endWith(resultOfSubstringWordApplication))

/*
TODO: Ah, maybe this was the simplification
      This won't override because the types are the same after erasure. See note on definition of ResultOfLengthOrSizeWordApplication
      // By-name parameter is to get this to short circuit:
      // "hi" should (have length (1) and not have length {mockClown.hasBigRedNose; 1})
      def have(resultOfLengthWordApplication: => ResultOfLengthWordApplication) =
        matchersWrapper.and(matchers.not.apply(matchers.have.length(resultOfLengthWordApplication.expectedLength)))

      // Array(1, 2) should (not have size (5) and not have size (3))
      // By-name parameter is to get this to short circuit:
      // Array(1, 2) should (have size (1) and not have size {mockClown.hasBigRedNose; 1})
      def have(resultOfSizeWordApplication: => ResultOfSizeWordApplication) =
        matchersWrapper.and(matchers.not.apply(matchers.have.size(resultOfSizeWordApplication.expectedSize)))
*/
    }

    def and(notWord: NotWord): AndNotWord = new AndNotWord

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
    def or[U <: T](rightMatcher: Matcher[U]): Matcher[U] =
      new Matcher[U] {
        def apply(left: U) = {
          val leftMatcherResult = leftMatcher(left)
          val rightMatcherResult = rightMatcher(left) // Not short circuiting anymore
          if (leftMatcherResult.matches)
            MatcherResult(
              true,
              leftMatcherResult.negativeFailureMessage,
              leftMatcherResult.failureMessage
            )
          else {
            MatcherResult(
              rightMatcherResult.matches,
              Resources("commaAnd", leftMatcherResult.failureMessage, rightMatcherResult.failureMessage),
              Resources("commaAnd", leftMatcherResult.failureMessage, rightMatcherResult.negativeFailureMessage)
            )
          }
        }
      }

    class OrHaveWord {
      def length(expectedLength: Long) = or(have.length(expectedLength))

      // Array(1, 2) should (have size (2) and have size (3 - 1))
      //                                       ^
      def size(expectedSize: Long) = or(have.size(expectedSize))
    }

    def or(haveWord: HaveWord): OrHaveWord = new OrHaveWord

    class OrContainWord {

      // Array(1, 2) should (contain element (2) or contain element (3 - 1))
      //                                                     ^
      def element[T](expectedElement: T) = matchersWrapper.or(matchers.contain.element(expectedElement))
    }

    def or(containWord: ContainWord): OrContainWord = new OrContainWord

    class OrBeWord {

      // isFileMock should (be a ('file) or be a ('directory))
      //                                       ^
      def a(symbol: Symbol) = or(be.a(symbol))

      // appleMock should (be an ('orange) or be an ('apple))
      //                                         ^
      def an(symbol: Symbol) = or(be.an(symbol))

      // obj should (be theSameInstanceAs (string) or be theSameInstanceAs (otherString))
      //                                                 ^
      def theSameInstanceAs(anyRef: AnyRef) = or(be.theSameInstanceAs(anyRef))
    }

    // isFileMock should (be a ('file) or be a ('directory))
    //                                 ^
    def or(beWord: BeWord): OrBeWord = new OrBeWord

    class OrFullyMatchWord {
      // "1.7" should (fullyMatch regex ("hello") or fullyMatch regex (decimal))
      //                                                        ^
      def regex(regexString: String) = or(fullyMatch.regex(regexString))

      // "1.7" should (fullyMatch regex ("hello") or fullyMatch regex (decimal))
      //                                                        ^
      def regex(regex: Regex) = or(fullyMatch.regex(regex))
    }

    def or(fullyMatchWord: FullyMatchWord): OrFullyMatchWord = new OrFullyMatchWord

    class OrIncludeWord {
      // "1.7" should (include regex ("hello") or include regex (decimal))
      //                                                  ^
      def regex(regexString: String) = or(include.regex(regexString))

      // "1.7" should (include regex ("hello") or include regex (decimal))
      //                                                  ^
      def regex(regex: Regex) = or(include.regex(regex))

      // "a1.7b" should (include substring ("1.7") or include substring ("1.7"))
      //                                                      ^
      def substring(expectedSubstring: String) = or(include.substring(expectedSubstring))
    }

    def or(includeWord: IncludeWord): OrIncludeWord = new OrIncludeWord

    class OrStartWithWord {
      // "1.7" should (startWith regex ("hello") or startWith regex (decimal))
      //                                                      ^
      def regex(regexString: String) = or(startWith.regex(regexString))

      // "1.7" should (startWith regex ("hello") or startWith regex (decimal))
      //                                                      ^
      def regex(regex: Regex) = or(startWith.regex(regex))

      // "1.7" should (startWith substring ("hello") or startWith substring ("1.7"))
      //                                                          ^
      def substring(expectedSubstring: String) = or(startWith.substring(expectedSubstring))
    }

    def or(startWithWord: StartWithWord): OrStartWithWord = new OrStartWithWord

    class OrEndWithWord {
      // "1.7" should (endWith regex ("hello") or endWith regex (decimal))
      //                                                  ^
      def regex(regexString: String) = or(endWith.regex(regexString))

      // "1.7" should (endWith regex ("hello") or endWith regex (decimal))
      //                                                  ^
      def regex(regex: Regex) = or(endWith.regex(regex))

      // "1.7b" should (endWith substring ("hello") or endWith substring ("7b"))
      //                                                       ^
      def substring(expectedSubstring: String) = or(endWith.substring(expectedSubstring))
    }

    def or(endWithWord: EndWithWord): OrEndWithWord = new OrEndWithWord

    // This is not yet short-circuiting. Need by-name params for things passed here.
    class OrNotWord {

      def equal(any: Any) =
        matchersWrapper.or(matchers.not.apply(matchers.equal(any)))

      // See explanation in have for AndNotWord
      def have(resultOfLengthOrSizeWordApplication: ResultOfLengthOrSizeWordApplication) =
        matchersWrapper.or(
          matchers.not.apply(
            resultOfLengthOrSizeWordApplication match {
              case resultOfLengthWordApplication: ResultOfLengthWordApplication =>
                matchers.have.length(resultOfLengthWordApplication.expectedLength)
              case resultOfSizeWordApplication: ResultOfSizeWordApplication =>
                matchers.have.size(resultOfSizeWordApplication.expectedSize)
            }
          )
        )

      def be[T](resultOfLessThanComparison: ResultOfLessThanComparison[T]) =
        matchersWrapper.or(matchers.not.be(resultOfLessThanComparison))

      def be[T](resultOfGreaterThanComparison: ResultOfGreaterThanComparison[T]) =
        matchersWrapper.or(matchers.not.be(resultOfGreaterThanComparison))

      def be[T](resultOfLessThanOrEqualToComparison: ResultOfLessThanOrEqualToComparison[T]) =
        matchersWrapper.or(matchers.not.be(resultOfLessThanOrEqualToComparison))

      def be[T](resultOfGreaterThanOrEqualToComparison: ResultOfGreaterThanOrEqualToComparison[T]) =
        matchersWrapper.or(matchers.not.be(resultOfGreaterThanOrEqualToComparison))

      // notEmptyMock should (not be ('full) or not be ('empty))
      //                                            ^
      def be[T](symbol: Symbol) = matchersWrapper.or(matchers.not.be(symbol))

      // isNotFileMock should (not be a ('directory) or not be a ('file))
      //                                                    ^
      def be[T](resultOfAWordApplication: ResultOfAWordApplication) = matchersWrapper.or(matchers.not.be(resultOfAWordApplication))

      // notAppleMock should (not be an ('apple) or not be an ('apple))
      //                                                ^
      def be[T](resultOfAnWordApplication: ResultOfAnWordApplication) = matchersWrapper.or(matchers.not.be(resultOfAnWordApplication))

      // obj should (not be theSameInstanceAs (otherString) or not be theSameInstanceAs (string))
      //                                                           ^
      def be[T](resultOfTheSameInstanceAsApplication: ResultOfTheSameInstanceAsApplication) = matchersWrapper.or(matchers.not.be(resultOfTheSameInstanceAsApplication))

      // sevenDotOh should (not be (17.0 plusOrMinus 0.2) or not be (17.0 plusOrMinus 0.2))
      //                                                         ^
      def be(doubleTolerance: DoubleTolerance) = matchersWrapper.or(matchers.not.be(doubleTolerance))

      // sevenDotOhFloat should (not be (17.0f plusOrMinus 0.2f) or not be (17.0f plusOrMinus 0.2f))
      //                                                                ^
      def be(floatTolerance: FloatTolerance) = matchersWrapper.or(matchers.not.be(floatTolerance))

      // sevenLong should (not be (17L plusOrMinus 2L) or not be (17L plusOrMinus 2L))
      //                                                      ^
      def be(longTolerance: LongTolerance) = matchersWrapper.or(matchers.not.be(longTolerance))

      // sevenInt should (not be (17 plusOrMinus 2) or not be (17 plusOrMinus 2))
      //                                                   ^
      def be(intTolerance: IntTolerance) = matchersWrapper.or(matchers.not.be(intTolerance))

      // sevenShort should (not be (17.toShort plusOrMinus 2.toShort) or not be (17.toShort plusOrMinus 2.toShort))
      //                                                                     ^
      def be(shortTolerance: ShortTolerance) = matchersWrapper.or(matchers.not.be(shortTolerance))

      // sevenByte should ((not be (19.toByte plusOrMinus 2.toByte)) or (not be (19.toByte plusOrMinus 2.toByte)))
      //                                                                     ^
      def be(byteTolerance: ByteTolerance) = matchersWrapper.or(matchers.not.be(byteTolerance))

      // "fred" should (not fullyMatch regex ("fred") or not fullyMatch regex (decimal))
      //                                                     ^
      def fullyMatch(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.or(matchers.not.fullyMatch(resultOfRegexWordApplication))

      // "fred" should (not include regex ("fred") or not include regex (decimal))
      //                                                  ^
      def include(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.or(matchers.not.include(resultOfRegexWordApplication))

      // "fred" should (not include substring ("bob") or not include substring ("1.7"))
      //                                                     ^
      def include(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) =
        matchersWrapper.or(matchers.not.include(resultOfSubstringWordApplication))

      // "fred" should (not startWith regex ("bob") or not startWith regex (decimal))
      //                                                   ^
      def startWith(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.or(matchers.not.startWith(resultOfRegexWordApplication))

      // "fred" should (not startWith substring ("fred") or not startWith substring ("1.7"))
      //                                                        ^
      def startWith(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) =
        matchersWrapper.or(matchers.not.startWith(resultOfSubstringWordApplication))

      // "fred" should (not endWith regex ("bob") or not endWith regex (decimal))
      //                                                 ^
      def endWith(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.or(matchers.not.endWith(resultOfRegexWordApplication))

      // "fred" should (not endWith substring ("fred") or not endWith substring ("1.7"))
      //                                                      ^
      def endWith(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) =
        matchersWrapper.or(matchers.not.endWith(resultOfSubstringWordApplication))

/*
TODO: Do the same simplification as above
      // By-name parameter is to get this to short circuit:
      // "hi" should (have length (1) and not have length {mockClown.hasBigRedNose; 1})
      def have(resultOfLengthWordApplication: => ResultOfLengthWordApplication) =
        matchersWrapper.or(matchers.not.apply(matchers.have.length(resultOfLengthWordApplication.expectedLength)))

      // Array(1, 2) should (not have size (2) or not have size (3))
      //                                          ^
      def have(resultOfSizeWordApplication: ResultOfSizeWordApplication) =
        matchersWrapper.or(matchers.not.apply(matchers.have.size(resultOfSizeWordApplication.expectedSize)))
*/
    }

    def or(notWord: NotWord): OrNotWord = new OrNotWord
  }

  implicit def convertToMatcherWrapper[T](leftMatcher: Matcher[T]): MatcherWrapper[T] = new MatcherWrapper(leftMatcher)

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
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveKey" else "hadKey",
            left,
            expectedKey)
        )
    }
    def value(expectedValue: V) {
      if (left.values.contains(expectedValue) != shouldBeTrue)
        throw newTestFailedException(
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

              val fieldArray =
                for (field <- left.getClass.getFields; if isFieldToAccess(field))
                 yield field // rhymes: code as poetry

              (methodArray.length, fieldArray.length) match {

                case (0, 0) =>
                  throw newTestFailedException(Resources("noLengthStructure", expectedLength.toString))

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

              val fieldArray =
                for (field <- left.getClass.getFields; if isFieldToAccess(field))
                 yield field // rhymes: code as poetry

              (methodArray.size, fieldArray.size) match {

                case (0, 0) =>
                  throw newTestFailedException(Resources("noSizeStructure", expectedSize.toString))

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
        throw newTestFailedException(
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
        throw newTestFailedException(
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
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
            left,
            expectedLength)
        )
    }
  }
  
  protected class ResultOfNotWordForIterable[E, T <: Iterable[E]](left: T, shouldBeTrue: Boolean)
      extends ResultOfNotWordForAnyRef(left, shouldBeTrue) {

    def contain(resultOfElementWordApplication: ResultOfElementWordApplication[E]) {
      val right = resultOfElementWordApplication.expectedElement
      if ((left.exists(_ == right)) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotContainExpectedElement" else "containedExpectedElement",
              left,
              right
            )
          )
      }
    }
  }
  
  protected class ResultOfNotWordForCollection[E, T <: Collection[E]](left: T, shouldBeTrue: Boolean)
      extends ResultOfNotWordForIterable[E, T](left, shouldBeTrue) {

    def have(resultOfSizeWordApplication: ResultOfSizeWordApplication) {
      val right = resultOfSizeWordApplication.expectedSize
      if ((left.size == right) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedSize" else "hadExpectedSize",
              left,
              right
            )
          )
      }
    }
  }

  protected class ResultOfNotWordForJavaCollection[T <: java.util.Collection[_]](left: T, shouldBeTrue: Boolean)
      extends ResultOfNotWordForAnyRef(left, shouldBeTrue) {

    def have(resultOfSizeWordApplication: ResultOfSizeWordApplication) {
      val right = resultOfSizeWordApplication.expectedSize
      if ((left.size == right) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedSize" else "hadExpectedSize",
              left,
              right
            )
          )
      }
    }
  }

  protected class ResultOfNotWordForMap[K, V](left: scala.collection.Map[K, V], shouldBeTrue: Boolean)
      extends ResultOfNotWordForCollection[(K, V), scala.collection.Map[K, V]](left, shouldBeTrue)

  protected class ResultOfNotWordForSeq[E, T <: Seq[E]](left: T, shouldBeTrue: Boolean)
      extends ResultOfNotWordForCollection[E, T](left, shouldBeTrue) {

    def have(resultOfLengthWordApplication: ResultOfLengthWordApplication) {
      val right = resultOfLengthWordApplication.expectedLength
      if ((left.length == right) != shouldBeTrue) {
          throw newTestFailedException(
            FailureMessages(
             if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
              left,
              right
            )
          )
      }
    }
  }

  protected class ResultOfHaveWordForJavaList[T](left: java.util.List[T], shouldBeTrue: Boolean) extends ResultOfHaveWordForJavaCollection[T](left, shouldBeTrue) {
    def length(expectedLength: Int) {
      if ((left.size == expectedLength) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
            left,
            expectedLength)
        )
    }
  }

  protected class ResultOfNotWordForJavaList[T <: java.util.List[_]](left: T, shouldBeTrue: Boolean)
      extends ResultOfNotWordForAnyRef(left, shouldBeTrue) {

    def have(resultOfLengthWordApplication: ResultOfLengthWordApplication) {
      val right = resultOfLengthWordApplication.expectedLength
      if ((left.size == right) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
              left,
              right
            )
          )
      }
    }

    // UUU I think I should inherit this from one used by Java collections
    def have(resultOfSizeWordApplication: ResultOfSizeWordApplication) {
      val right = resultOfSizeWordApplication.expectedSize
      if ((left.size == right) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedSize" else "hadExpectedSize",
              left,
              right
            )
          )
      }
    }
  }

  protected class ResultOfBeWordForAnyRef(left: AnyRef, shouldBeTrue: Boolean) {

    def theSameInstanceAs(right: AnyRef) {
      if ((left eq right) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "wasNotSameInstanceAs" else "wasSameInstanceAs",
            left,
            right
          )
        )
    }

    // fileMock should be a ('file)
    //                    ^
    def a(symbol: Symbol) {
      val matcherResult = matchSymbolToPredicateMethod(left, symbol, true, true)
      if (matcherResult.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negativeFailureMessage
        )
      }
    }

    // fruit should be an ('orange)
    //                    ^
    // TODO, in both of these, the failure message doesn't have a/an
    def an(symbol: Symbol) {
      val matcherResult = matchSymbolToPredicateMethod(left, symbol, true, false)
      if (matcherResult.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negativeFailureMessage
        )
      }
    }
  }

/*
  // What's this one for again?
  implicit def resultOfBeWordToForAnyRef[T <: AnyRef](resultOfBeWord: ResultOfBeWord[T]): ResultOfBeWordForAnyRef =
    new ResultOfBeWordForAnyRef(resultOfBeWord.left, resultOfBeWord.shouldBeTrue)

  protected class ResultOfBeWord[T](val left: T, val shouldBeTrue: Boolean) {
    def a[S <: AnyRef](right: Symbol): Matcher[S] = be(right)
    def an[S <: AnyRef](right: Symbol): Matcher[S] = be(right)
  }
*/

  protected class ResultOfNotWord[T](left: T, shouldBeTrue: Boolean) {
    def equal(right: Any) {
      if ((left == right) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
           if (shouldBeTrue) "didNotEqual" else "equaled",
            left,
            right
          )
        )
    }

    def be(comparison: ResultOfLessThanOrEqualToComparison[T]) {
      if (comparison(left) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "wasNotLessThanOrEqualTo" else "wasLessThanOrEqualTo",
            left,
            comparison.right
          )
        )
      }
    }

    def be(comparison: ResultOfGreaterThanOrEqualToComparison[T]) {
      if (comparison(left) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "wasNotGreaterThanOrEqualTo" else "wasGreaterThanOrEqualTo",
            left,
            comparison.right
          )
        )
      }
    }

    def be(comparison: ResultOfLessThanComparison[T]) {
      if (comparison(left) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "wasNotLessThan" else "wasLessThan",
            left,
            comparison.right
          )
        )
      }
    }

    def be(comparison: ResultOfGreaterThanComparison[T]) {
      if (comparison(left) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "wasNotGreaterThan" else "wasGreaterThan",
            left,
            comparison.right
          )
        )
      }
    }
  }

  protected class ResultOfNotWordForAnyRef[T <: AnyRef](left: T, shouldBeTrue: Boolean)
      extends ResultOfNotWord[T](left, shouldBeTrue) {

    // emptyMock should not be ('empty)
    //                      ^
    def be(symbol: Symbol) {
      val matcherResult = matchSymbolToPredicateMethod(left, symbol, false, false)
      if (matcherResult.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negativeFailureMessage
        )
      }
    }

    // notFileMock should not be a ('file)
    //                        ^
    def be(resultOfAWordApplication: ResultOfAWordApplication) {
      val matcherResult = matchSymbolToPredicateMethod(left, resultOfAWordApplication.symbol, true, true)
      if (matcherResult.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negativeFailureMessage
        )
      }
    }

    // notAppleMock should not be an ('apple)
    //                         ^
    def be(resultOfAnWordApplication: ResultOfAnWordApplication) {
      val matcherResult = matchSymbolToPredicateMethod(left, resultOfAnWordApplication.symbol, true, false)
      if (matcherResult.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negativeFailureMessage
        )
      }
    }

    // otherString should not be theSameInstanceAs (string)
    //                        ^
    def be(resultOfSameInstanceAsApplication: ResultOfTheSameInstanceAsApplication) {
      if ((resultOfSameInstanceAsApplication.right eq left) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "wasNotSameInstanceAs" else "wasSameInstanceAs",
            left,
            resultOfSameInstanceAsApplication.right
          )
        )
      }
    }
  }

  protected class ResultOfNotWordForString(left: String, shouldBeTrue: Boolean)
      extends ResultOfNotWordForAnyRef[String](left, shouldBeTrue) {

    def have(resultOfLengthWordApplication: ResultOfLengthWordApplication) {
      val right = resultOfLengthWordApplication.expectedLength
      if ((left.length == right) != shouldBeTrue) {
          throw newTestFailedException(
            FailureMessages(
             if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
              left,
              right
            )
          )
      }
    }

    def fullyMatch(resultOfRegexWordApplication: ResultOfRegexWordApplication) {
      val rightRegex = resultOfRegexWordApplication.regex
      if (rightRegex.pattern.matcher(left).matches != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotFullyMatchRegex" else "fullyMatchedRegex",
            left,
            rightRegex
          )
        )
    }

    def include(resultOfRegexWordApplication: ResultOfRegexWordApplication) {
      val rightRegex = resultOfRegexWordApplication.regex
      if (rightRegex.findFirstIn(left).isDefined != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotIncludeRegex" else "includedRegex",
            left,
            rightRegex
          )
        )
    }

    def include(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) {
      val expectedSubstring = resultOfSubstringWordApplication.substring
      if ((left.indexOf(expectedSubstring) >= 0) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotIncludeSubstring" else "includedSubstring",
            left,
            expectedSubstring
          )
        )
    }

    def startWith(resultOfRegexWordApplication: ResultOfRegexWordApplication) {
      val rightRegex = resultOfRegexWordApplication.regex
      if (rightRegex.pattern.matcher(left).lookingAt != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotStartWithRegex" else "startedWithRegex",
            left,
            rightRegex
          )
        )
    }

    // "eight" should not startWith substring ("1.7")
    //                    ^
    def startWith(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) {
      val expectedSubstring = resultOfSubstringWordApplication.substring
      if ((left.indexOf(expectedSubstring) == 0) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotStartWith" else "startedWith",
            left,
            expectedSubstring
          )
        )
    }

    def endWith(resultOfRegexWordApplication: ResultOfRegexWordApplication) {
      val rightRegex = resultOfRegexWordApplication.regex
      val allMatches = rightRegex.findAllIn(left)
      if (allMatches.hasNext && (allMatches.end == left.length) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotEndWithRegex" else "endedWithRegex",
            left,
            rightRegex
          )
        )
    }

    // "eight" should not endWith substring ("1.7")
    //                    ^
    def endWith(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) {
      val expectedSubstring = resultOfSubstringWordApplication.substring
      if ((left endsWith expectedSubstring) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotEndWith" else "endedWith",
            left,
            expectedSubstring
          )
        )
    }
  }

  protected class ResultOfNotWordForDouble(left: Double, shouldBeTrue: Boolean)
      extends ResultOfNotWord[Double](left, shouldBeTrue) {

    // sevenDotOh should not be (6.5 plusOrMinus 0.2)
    //                       ^
    def be(doubleTolerance: DoubleTolerance) {
      import doubleTolerance._
      if ((left <= right + tolerance && left >= right - tolerance) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "wasNotPlusOrMinus" else "wasPlusOrMinus",
            left,
            right,
            tolerance
          )
        )
      }
    }
  }

  protected class ResultOfNotWordForFloat(left: Float, shouldBeTrue: Boolean)
      extends ResultOfNotWord[Float](left, shouldBeTrue) {

    // sevenDotOhFloat should not be (6.5f plusOrMinus 0.2f)
    //                            ^
    def be(floatTolerance: FloatTolerance) {
      import floatTolerance._
      if ((left <= right + tolerance && left >= right - tolerance) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "wasNotPlusOrMinus" else "wasPlusOrMinus",
            left,
            right,
            tolerance
          )
        )
      }
    }
  }

  protected class ResultOfNotWordForLong(left: Long, shouldBeTrue: Boolean)
      extends ResultOfNotWord[Long](left, shouldBeTrue) {

    // sevenDotOhLong should not be (4L plusOrMinus 2L)
    //                           ^
    def be(longTolerance: LongTolerance) {
      import longTolerance._
      if ((left <= right + tolerance && left >= right - tolerance) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "wasNotPlusOrMinus" else "wasPlusOrMinus",
            left,
            right,
            tolerance
          )
        )
      }
    }
  }

  protected class ResultOfNotWordForInt(left: Int, shouldBeTrue: Boolean)
      extends ResultOfNotWord[Int](left, shouldBeTrue) {

    // sevenDotOhInt should not be (4 plusOrMinus 2)
    //                          ^
    def be(intTolerance: IntTolerance) {
      import intTolerance._
      if ((left <= right + tolerance && left >= right - tolerance) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "wasNotPlusOrMinus" else "wasPlusOrMinus",
            left,
            right,
            tolerance
          )
        )
      }
    }
  }

  protected class ResultOfNotWordForShort(left: Short, shouldBeTrue: Boolean)
      extends ResultOfNotWord[Short](left, shouldBeTrue) {

    // sevenDotOhShort should not be (4.toShort plusOrMinus 2.toShort)
    //                            ^
    def be(shortTolerance: ShortTolerance) {
      import shortTolerance._
      if ((left <= right + tolerance && left >= right - tolerance) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "wasNotPlusOrMinus" else "wasPlusOrMinus",
            left,
            right,
            tolerance
          )
        )
      }
    }
  }

  protected class ResultOfNotWordForByte(left: Byte, shouldBeTrue: Boolean)
      extends ResultOfNotWord[Byte](left, shouldBeTrue) {

    // sevenDotOhByte should not be (4.toByte plusOrMinus 2.toByte)
    //                            ^
    def be(byteTolerance: ByteTolerance) {
      import byteTolerance._
      if ((left <= right + tolerance && left >= right - tolerance) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "wasNotPlusOrMinus" else "wasPlusOrMinus",
            left,
            right,
            tolerance
          )
        )
      }
    }
  }

  class RegexWord {

    def apply(regexString: String) = new ResultOfRegexWordApplication(regexString)

    // "eight" should not fullyMatch regex (decimalRegex)
    //                               ^
    def apply(regex: Regex) = new ResultOfRegexWordApplication(regex)
  }

  class ResultOfRegexWordApplication(val regex: Regex) {
    def this(regexString: String) = this(new Regex(regexString))
  }

  class SubstringWord {

    // "eight" should not fullyMatch substring ("seven")
    //                               ^
    def apply(substring: String) = new ResultOfSubstringWordApplication(substring)
  }

  class ResultOfSubstringWordApplication(val substring: String)
 
  protected class ResultOfHaveWordForString(left: String, shouldBeTrue: Boolean) {
    def length(expectedLength: Int) {
      if ((left.length == expectedLength) != shouldBeTrue)
        throw newTestFailedException(
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
        throw newTestFailedException(
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
        throw newTestFailedException(
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
        throw newTestFailedException(
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
        throw newTestFailedException(
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
        throw newTestFailedException(
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
        throw newTestFailedException(
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
        throw newTestFailedException(
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
        throw newTestFailedException(
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

    // fileMock should not { be a ('file) }
    //                          ^
    def a[S <: AnyRef](right: Symbol): Matcher[S] =
      new Matcher[S] {
        def apply(left: S) = matchSymbolToPredicateMethod[S](left, right, true, true)
      }

    // animal should not { be an ('elephant) }
    //                        ^
    def an[S <: AnyRef](right: Symbol): Matcher[S] =
      new Matcher[S] {
        def apply(left: S) = matchSymbolToPredicateMethod[S](left, right, true, false)
      }

    // sevenDotOh should be (7.1 plusOrMinus 0.2)
    //                      ^
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

    // sevenDotOhFloat should be (7.1f plusOrMinus 0.2f)
    //                           ^
    def apply(floatTolerance: FloatTolerance): Matcher[Float] =
      new Matcher[Float] {
        def apply(left: Float) = {
          import floatTolerance._
          MatcherResult(
            left <= right + tolerance && left >= right - tolerance,
            FailureMessages("wasNotPlusOrMinus", left, right, tolerance),
            FailureMessages("wasPlusOrMinus", left, right, tolerance)
          )
        }
      }

    // sevenLong should be (7L plusOrMinus 2L)
    //                     ^
    def apply(longTolerance: LongTolerance): Matcher[Long] =
      new Matcher[Long] {
        def apply(left: Long) = {
          import longTolerance._
          MatcherResult(
            left <= right + tolerance && left >= right - tolerance,
            FailureMessages("wasNotPlusOrMinus", left, right, tolerance),
            FailureMessages("wasPlusOrMinus", left, right, tolerance)
          )
        }
      }

    // sevenInt should be (7 plusOrMinus 2)
    //                     ^
    def apply(intTolerance: IntTolerance): Matcher[Int] =
      new Matcher[Int] {
        def apply(left: Int) = {
          import intTolerance._
          MatcherResult(
            left <= right + tolerance && left >= right - tolerance,
            FailureMessages("wasNotPlusOrMinus", left, right, tolerance),
            FailureMessages("wasPlusOrMinus", left, right, tolerance)
          )
        }
      }

    // sevenShort should be (7.toShort plusOrMinus 2.toShort)
    //                     ^
    def apply(shortTolerance: ShortTolerance): Matcher[Short] =
      new Matcher[Short] {
        def apply(left: Short) = {
          import shortTolerance._
          MatcherResult(
            left <= right + tolerance && left >= right - tolerance,
            FailureMessages("wasNotPlusOrMinus", left, right, tolerance),
            FailureMessages("wasPlusOrMinus", left, right, tolerance)
          )
        }
      }

    // sevenByte should be (7.toByte plusOrMinus 2.toByte)
    //                     ^
    def apply(byteTolerance: ByteTolerance): Matcher[Byte] =
      new Matcher[Byte] {
        def apply(left: Byte) = {
          import byteTolerance._
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
  
    def apply[S <: AnyRef](right: Symbol): Matcher[S] =
      new Matcher[S] {
        def apply(left: S) = matchSymbolToPredicateMethod[S](left, right, false, false)
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

    def have(resultOfLengthWordApplication: ResultOfLengthWordApplication): Matcher[AnyRef] =
      apply(matchers.have.length(resultOfLengthWordApplication.expectedLength))

    // This looks similar to the AndNotWord one, but not quite the same because no and
    // Array(1, 2) should (not have size (5) and not have size (3))
    //                     ^
    def have(resultOfSizeWordApplication: ResultOfSizeWordApplication): Matcher[AnyRef] =
      apply(matchers.have.size(resultOfSizeWordApplication.expectedSize))

    // These next four are for things like not be </>/<=/>=:
    // left should ((not be < (right)) and (not be < (right + 1)))
    //               ^
    def be[T](resultOfLessThanComparison: ResultOfLessThanComparison[T]): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) =
          MatcherResult(
            !resultOfLessThanComparison(left),
            FailureMessages("wasLessThan", left, resultOfLessThanComparison.right),
            FailureMessages("wasNotLessThan", left, resultOfLessThanComparison.right)
          )
      }
    }

    def be[T](resultOfGreaterThanComparison: ResultOfGreaterThanComparison[T]): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) =
          MatcherResult(
            !resultOfGreaterThanComparison(left),
            FailureMessages("wasGreaterThan", left, resultOfGreaterThanComparison.right),
            FailureMessages("wasNotGreaterThan", left, resultOfGreaterThanComparison.right)
          )
      }
    }

    def be[T](resultOfLessThanOrEqualToComparison: ResultOfLessThanOrEqualToComparison[T]): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) =
          MatcherResult(
            !resultOfLessThanOrEqualToComparison(left),
            FailureMessages("wasLessThanOrEqualTo", left, resultOfLessThanOrEqualToComparison.right),
            FailureMessages("wasNotLessThanOrEqualTo", left, resultOfLessThanOrEqualToComparison.right)
          )
      }
    }

    def be[T](resultOfGreaterThanOrEqualToComparison: ResultOfGreaterThanOrEqualToComparison[T]): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) =
          MatcherResult(
            !resultOfGreaterThanOrEqualToComparison(left),
            FailureMessages("wasGreaterThanOrEqualTo", left, resultOfGreaterThanOrEqualToComparison.right),
            FailureMessages("wasNotGreaterThanOrEqualTo", left, resultOfGreaterThanOrEqualToComparison.right)
          )
      }
    }

    def be[T <: AnyRef](symbol: Symbol): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) = {
          val positiveMatcherResult = matchSymbolToPredicateMethod(left, symbol, false, false)
          MatcherResult(
            !positiveMatcherResult.matches,
            positiveMatcherResult.negativeFailureMessage,
            positiveMatcherResult.failureMessage
          )
        }
      }
    }

    // isNotFileMock should (not be a ('file) and not be a ('file))
    //                           ^
    def be[T <: AnyRef](resultOfAWordApplication: ResultOfAWordApplication): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) = {
          val positiveMatcherResult = matchSymbolToPredicateMethod(left, resultOfAWordApplication.symbol, true, true)
          MatcherResult(
            !positiveMatcherResult.matches,
            positiveMatcherResult.negativeFailureMessage,
            positiveMatcherResult.failureMessage
          )
        }
      }
    }

    // isNotAppleMock should (not be an ('apple) and not be an ('apple))
    //                            ^
    def be[T <: AnyRef](resultOfAnWordApplication: ResultOfAnWordApplication): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) = {
          val positiveMatcherResult = matchSymbolToPredicateMethod(left, resultOfAnWordApplication.symbol, true, false)
          MatcherResult(
            !positiveMatcherResult.matches,
            positiveMatcherResult.negativeFailureMessage,
            positiveMatcherResult.failureMessage
          )
        }
      }
    }

    // obj should (not be theSameInstanceAs (otherString) and not be theSameInstanceAs (otherString))
    //                 ^
    def be[T <: AnyRef](resultOfTheSameInstanceAsApplication: ResultOfTheSameInstanceAsApplication): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) = {
          MatcherResult(
            resultOfTheSameInstanceAsApplication.right ne left,
            FailureMessages("wasSameInstanceAs", left, resultOfTheSameInstanceAsApplication.right),
            FailureMessages("wasNotSameInstanceAs", left, resultOfTheSameInstanceAsApplication.right)
          )
        }
      }
    }

    // sevenDotOh should ((not be (17.1 plusOrMinus 0.2)) and (not be (17.1 plusOrMinus 0.2)))
    //                         ^
    def be(doubleTolerance: DoubleTolerance): Matcher[Double] = {
      import doubleTolerance._
      new Matcher[Double] {
        def apply(left: Double) = {
          MatcherResult(
            !(left <= right + tolerance && left >= right - tolerance),
            FailureMessages("wasPlusOrMinus", left, right, tolerance),
            FailureMessages("wasNotPlusOrMinus", left, right, tolerance)
          )
        }
      }
    }

    // sevenDotOhFloat should ((not be (17.1f plusOrMinus 0.2f)) and (not be (17.1f plusOrMinus 0.2f)))
    //                         ^
    def be(floatTolerance: FloatTolerance): Matcher[Float] = {
      import floatTolerance._
      new Matcher[Float] {
        def apply(left: Float) = {
          MatcherResult(
            !(left <= right + tolerance && left >= right - tolerance),
            FailureMessages("wasPlusOrMinus", left, right, tolerance),
            FailureMessages("wasNotPlusOrMinus", left, right, tolerance)
          )
        }
      }
    }

    // sevenLong should ((not be (19L plusOrMinus 2L)) and (not be (19L plusOrMinus 2L)))
    //                        ^
    def be(longTolerance: LongTolerance): Matcher[Long] = {
      import longTolerance._
      new Matcher[Long] {
        def apply(left: Long) = {
          MatcherResult(
            !(left <= right + tolerance && left >= right - tolerance),
            FailureMessages("wasPlusOrMinus", left, right, tolerance),
            FailureMessages("wasNotPlusOrMinus", left, right, tolerance)
          )
        }
      }
    }

    // sevenInt should ((not be (19 plusOrMinus 2)) and (not be (19 plusOrMinus 2)))
    //                       ^
    def be(intTolerance: IntTolerance): Matcher[Int] = {
      import intTolerance._
      new Matcher[Int] {
        def apply(left: Int) = {
          MatcherResult(
            !(left <= right + tolerance && left >= right - tolerance),
            FailureMessages("wasPlusOrMinus", left, right, tolerance),
            FailureMessages("wasNotPlusOrMinus", left, right, tolerance)
          )
        }
      }
    }

    // sevenShort should ((not be (19.toShort plusOrMinus 2.toShort)) and (not be (19.toShort plusOrMinus 2.toShort)))
    //                         ^
    def be(shortTolerance: ShortTolerance): Matcher[Short] = {
      import shortTolerance._
      new Matcher[Short] {
        def apply(left: Short) = {
          MatcherResult(
            !(left <= right + tolerance && left >= right - tolerance),
            FailureMessages("wasPlusOrMinus", left, right, tolerance),
            FailureMessages("wasNotPlusOrMinus", left, right, tolerance)
          )
        }
      }
    }

    // sevenByte should ((not be (19.toByte plusOrMinus 2.toByte)) and (not be (19.toByte plusOrMinus 2.toByte)))
    //                        ^
    def be(byteTolerance: ByteTolerance): Matcher[Byte] = {
      import byteTolerance._
      new Matcher[Byte] {
        def apply(left: Byte) = {
          MatcherResult(
            !(left <= right + tolerance && left >= right - tolerance),
            FailureMessages("wasPlusOrMinus", left, right, tolerance),
            FailureMessages("wasNotPlusOrMinus", left, right, tolerance)
          )
        }
      }
    }

    def fullyMatch(resultOfRegexWordApplication: ResultOfRegexWordApplication): Matcher[String] = {
      val rightRegexString = resultOfRegexWordApplication.regex.toString
      new Matcher[String] {
        def apply(left: String) =
          MatcherResult(
            !java.util.regex.Pattern.matches(rightRegexString, left),
            FailureMessages("fullyMatchedRegex", left, UnquotedString(rightRegexString)),
            FailureMessages("didNotFullyMatchRegex", left, UnquotedString(rightRegexString))
          )
      }
    }

    // "fred" should (not include regex ("bob") and not include regex (decimal))
    //                    ^
    def include(resultOfRegexWordApplication: ResultOfRegexWordApplication): Matcher[String] = {
      val rightRegex = resultOfRegexWordApplication.regex
      new Matcher[String] {
        def apply(left: String) =
          MatcherResult(
            !rightRegex.findFirstIn(left).isDefined,
            FailureMessages("includedRegex", left, rightRegex),
            FailureMessages("didNotIncludeRegex", left, rightRegex)
          )
      }
    }

    // "fred" should (not include substring ("bob") and not include substring ("1.7"))
    //                    ^
    def include(resultOfSubstringWordApplication: ResultOfSubstringWordApplication): Matcher[String] = {
      val expectedSubstring = resultOfSubstringWordApplication.substring
      new Matcher[String] {
        def apply(left: String) =
          MatcherResult(
            !(left.indexOf(expectedSubstring) >= 0), 
            FailureMessages("includedSubstring", left, expectedSubstring),
            FailureMessages("didNotIncludeSubstring", left, expectedSubstring)
          )
      }
    }

    // "fred" should (not startWith regex ("bob") and not startWith regex (decimal))
    //                    ^
    def startWith(resultOfRegexWordApplication: ResultOfRegexWordApplication): Matcher[String] = {
      val rightRegex = resultOfRegexWordApplication.regex
      new Matcher[String] {
        def apply(left: String) =
          MatcherResult(
            !rightRegex.pattern.matcher(left).lookingAt,
            FailureMessages("startedWithRegex", left, rightRegex),
            FailureMessages("didNotStartWithRegex", left, rightRegex)
          )
      }
    }

    // "fred" should ((not startWith substring ("red")) and (not startWith substring ("1.7")))
    //                     ^
    def startWith(resultOfSubstringWordApplication: ResultOfSubstringWordApplication): Matcher[String] = {
      val expectedSubstring = resultOfSubstringWordApplication.substring
      new Matcher[String] {
        def apply(left: String) =
          MatcherResult(
            left.indexOf(expectedSubstring) != 0,
            FailureMessages("startedWith", left, expectedSubstring),
            FailureMessages("didNotStartWith", left, expectedSubstring)
          )
      }
    }

    // "fred" should (not endWith regex ("bob") and not endWith regex (decimal))
    //                    ^
    def endWith(resultOfRegexWordApplication: ResultOfRegexWordApplication): Matcher[String] = {
      val rightRegex = resultOfRegexWordApplication.regex
      new Matcher[String] {
        def apply(left: String) = {
          val allMatches = rightRegex.findAllIn(left)
          MatcherResult(
            !(allMatches.hasNext && (allMatches.end == left.length)),
            FailureMessages("endedWithRegex", left, rightRegex),
            FailureMessages("didNotEndWithRegex", left, rightRegex)
          )
        }
      }
    }

    // "fred" should (not endWith substring ("fre") and not endWith substring ("1.7"))
    //                    ^
    def endWith(resultOfSubstringWordApplication: ResultOfSubstringWordApplication): Matcher[String] = {
      val expectedSubstring = resultOfSubstringWordApplication.substring
      new Matcher[String] {
        def apply(left: String) = {
          MatcherResult(
            !(left endsWith expectedSubstring),
            FailureMessages("endedWith", left, expectedSubstring),
            FailureMessages("didNotEndWith", left, expectedSubstring)
          )
        }
      }
    }
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

  // This guy is needed to support short-circuiting after and and or, because i need to use by-name parameters
  // that result in the two subclasses ResultOfLengthWordApplication and ResultOfSizeWordApplication. The by-name
  // param ends up as a type Function0[Unit] I think, and so these two don't overload. Darn. So I just make one
  // of the superclass type, ResultOfLengthOrSizeWordApplication, and then do a pattern match. At first I tried
  // to do it the OO way and have an rather ugly expectedLengthOrSize val set by each subclass, but I needed to
  // konw whether it was length or size to be able to call length or size to get the appropriate error message on
  // a failure.
  abstract class ResultOfLengthOrSizeWordApplication

  class ResultOfLengthWordApplication(val expectedLength: Long) extends ResultOfLengthOrSizeWordApplication

  class LengthWord {
    def apply(expectedLength: Long) = new ResultOfLengthWordApplication(expectedLength)
  }

  val length = new LengthWord
    
  class ResultOfSizeWordApplication(val expectedSize: Long) extends ResultOfLengthOrSizeWordApplication

  class SizeWord {
    def apply(expectedSize: Long) = new ResultOfSizeWordApplication(expectedSize)
  }

  val size = new SizeWord

  class ResultOfElementWordApplication[T](val expectedElement: T)

  class ElementWord {
    // array should not contain element (10)
    //                                  ^
    def apply[T](expectedElement: T) = new ResultOfElementWordApplication(expectedElement)
  }

  // array should not contain element (10)
  //                          ^
  val element = new ElementWord

  class ResultOfAWordApplication(val symbol: Symbol)

  class AWord {
    def apply(symbol: Symbol) = new ResultOfAWordApplication(symbol)
  }

  val a = new AWord

  class ResultOfAnWordApplication(val symbol: Symbol)

  class AnWord {
    def apply(symbol: Symbol) = new ResultOfAnWordApplication(symbol)
  }

  val an = new AnWord

  class ResultOfTheSameInstanceAsApplication(val right: AnyRef)

  class TheSameInstanceAsPhrase {
    // otherString should not be theSameInstanceAs (string)
    //                                             ^
    def apply(anyRef: AnyRef) = new ResultOfTheSameInstanceAsApplication(anyRef)
  }

  // otherString should not be theSameInstanceAs (string)
  //                           ^
  val theSameInstanceAs = new TheSameInstanceAsPhrase

  val regex = new RegexWord

  val substring = new SubstringWord

  case class DoubleTolerance(right: Double, tolerance: Double)

  class DoublePlusOrMinusWrapper(right: Double) {
    def plusOrMinus(tolerance: Double): DoubleTolerance = {
      if (tolerance <= 0.0)
        throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      DoubleTolerance(right, tolerance)
    }
  }

  implicit def convertDoubleToPlusOrMinusWrapper(right: Double) = new DoublePlusOrMinusWrapper(right)

  case class FloatTolerance(right: Float, tolerance: Float)

  class FloatPlusOrMinusWrapper(right: Float) {
    def plusOrMinus(tolerance: Float): FloatTolerance = {
      if (tolerance <= 0.0f)
        throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      FloatTolerance(right, tolerance)
    }
  }

  implicit def convertFloatToPlusOrMinusWrapper(right: Float) = new FloatPlusOrMinusWrapper(right)

  case class LongTolerance(right: Long, tolerance: Long)

  class LongPlusOrMinusWrapper(right: Long) {
    def plusOrMinus(tolerance: Long): LongTolerance = {
      if (tolerance <= 0L)
        throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      LongTolerance(right, tolerance)
    }
  }

  implicit def convertLongToPlusOrMinusWrapper(right: Long) = new LongPlusOrMinusWrapper(right)

  case class IntTolerance(right: Int, tolerance: Int)

  class IntPlusOrMinusWrapper(right: Int) {
    def plusOrMinus(tolerance: Int): IntTolerance = {
      if (tolerance <= 0)
        throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      IntTolerance(right, tolerance)
    }
  }

  implicit def convertIntToPlusOrMinusWrapper(right: Int) = new IntPlusOrMinusWrapper(right)

  case class ShortTolerance(right: Short, tolerance: Short)

  class ShortPlusOrMinusWrapper(right: Short) {
    def plusOrMinus(tolerance: Short): ShortTolerance = {
      if (tolerance <= 0)
        throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      ShortTolerance(right, tolerance)
    }
  }

  implicit def convertShortToPlusOrMinusWrapper(right: Short) = new ShortPlusOrMinusWrapper(right)

  case class ByteTolerance(right: Byte, tolerance: Byte)

  class BytePlusOrMinusWrapper(right: Byte) {
    def plusOrMinus(tolerance: Byte): ByteTolerance = {
      if (tolerance <= 0)
        throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      ByteTolerance(right, tolerance)
    }
  }

  implicit def convertByteToPlusOrMinusWrapper(right: Byte) = new BytePlusOrMinusWrapper(right)

  class ResultOfNotWordForLengthWrapper[A <% LengthWrapper](left: A, shouldBeTrue: Boolean)
      extends ResultOfNotWord(left, shouldBeTrue) {

    def have(resultOfLengthWordApplication: ResultOfLengthWordApplication) {
      val right = resultOfLengthWordApplication.expectedLength
      if ((left.length == right) != shouldBeTrue) {
          throw newTestFailedException(
            FailureMessages(
             if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
              left,
              right
            )
          )
      }
    }
  }

  class ResultOfHaveWordForLengthWrapper[A <% LengthWrapper](left: A, shouldBeTrue: Boolean) {
    def length(expectedLength: Int) {
      if ((left.length == expectedLength) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
            left,
            expectedLength)
        )
    }
    def length(expectedLength: Long) {
      if ((left.length == expectedLength) != shouldBeTrue)
        throw newTestFailedException(
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
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedSize" else "hadExpectedSize",
            left,
            expectedSize)
        )
    }
    def size(expectedSize: Long) {
      if ((left.size == expectedSize) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotHaveExpectedSize" else "hadExpectedSize",
            left,
            expectedSize)
        )
    }
  }

  protected class ResultOfLessThanComparison[T <% Ordered[T]](val right: T) {
    def apply(left: T): Boolean = left < right
  }

  protected class ResultOfGreaterThanComparison[T <% Ordered[T]](val right: T) {
    def apply(left: T): Boolean = left > right
  }

  protected class ResultOfLessThanOrEqualToComparison[T <% Ordered[T]](val right: T) {
    def apply(left: T): Boolean = left <= right
  }

  protected class ResultOfGreaterThanOrEqualToComparison[T <% Ordered[T]](val right: T) {
    def apply(left: T): Boolean = left >= right
  }

  protected def <[T <% Ordered[T]] (right: T): ResultOfLessThanComparison[T] =
    new ResultOfLessThanComparison(right)

  protected def >[T <% Ordered[T]] (right: T): ResultOfGreaterThanComparison[T] =
    new ResultOfGreaterThanComparison(right)

  protected def <=[T <% Ordered[T]] (right: T): ResultOfLessThanOrEqualToComparison[T] =
    new ResultOfLessThanOrEqualToComparison(right)

  protected def >=[T <% Ordered[T]] (right: T): ResultOfGreaterThanOrEqualToComparison[T] =
    new ResultOfGreaterThanOrEqualToComparison(right)
}


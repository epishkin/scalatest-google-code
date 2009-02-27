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
package org.scalatest.matchers

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import scala.util.matching.Regex
import java.lang.reflect.Field
import scala.reflect.Manifest
import Helper.transformOperatorChars

// TODO: drop generic support for be as an equality comparison, in favor of specific ones.
// TODO: mention on JUnit and TestNG docs that you can now mix in ShouldMatchers or MustMatchers
// TODO: Rename negatedFailureMessage to oppositeFailureMessage, and explain it better in the doc comments
// TODO: Put the TFE info in print reporter output too
// TODO: Put links from ShouldMatchers to wherever I reveal the matrix and algo of how properties are checked dynamically.
// TODO: double check that I wrote tests for (length (7)) and (size (8)) in parens
// TODO: document how to turn off the === implicit conversion

// This is used to pass a string to the FailureMessages apply method
// but prevent it from being quoted. This is useful when using a string
// to talk about method names, for example.
private[scalatest] class UnquotedString(s: String) {
  override def toString = s
}

private[scalatest] object UnquotedString {
  def apply(s: String) = new UnquotedString(s)
}

private[scalatest] object Helper {

  def newTestFailedException(message: String): TestFailedException = {
    val fileNames = List("Matchers.scala", "ShouldMatchers.scala", "MustMatchers.scala")
    val temp = new RuntimeException
    val stackDepth = temp.getStackTrace.takeWhile(stackTraceElement => fileNames.exists(_ == stackTraceElement.getFileName)).length
    new TestFailedException(message, stackDepth)
  }

  // If the symbol passed is 'title, this will look for a field named "title", a method named "title", or a
  // method named "getTitle". The method must take no parameters.
  //
  // F (field) | M (method) | G (get or is method) | Result
  // 0           0            0                      None
  // 0           0            1                      Some(G)
  // 0           1            0                      Some(M)
  // 0           1            1                      Some(M) prefer a Scala style one of a Java style, such as when using BeanProperty annotation
  // 1           0            0                      Some(F) ignore the field if there's a method. in Java often name a field and get method the same
  // 1           0            1                      Some(G)
  // 1           1            0                      Some(M)
  // 1           1            1                      Some(M) prefer a Scala style one of a Java style, such as when using BeanProperty annotation
  // 
  def accessProperty(objectWithProperty: AnyRef, propertySymbol: Symbol, isBooleanProperty: Boolean): Option[Any] = {

    // If 'title passed, propertyName would be "title"
    val propertyName = propertySymbol.name

    // if propertyName is '>, mangledPropertyName would be "$greater"
    val mangledPropertyName = transformOperatorChars(propertyName)

    // fieldNameToAccess and methodNameToInvoke would also be "title"
    val fieldNameToAccess = mangledPropertyName
    val methodNameToInvoke = mangledPropertyName

    // methodNameToInvokeWithGet would be "getTitle"
    val prefix = if (isBooleanProperty) "is" else "get"
    val methodNameToInvokeWithGet = prefix + mangledPropertyName(0).toUpperCase + mangledPropertyName.substring(1)

    val firstChar = propertyName(0).toLowerCase
    val methodNameStartsWithVowel = firstChar == 'a' || firstChar == 'e' || firstChar == 'i' ||
      firstChar == 'o' || firstChar == 'u'

    def isFieldToAccess(field: Field): Boolean = field.getName == fieldNameToAccess

    // If it is a predicate, I check the result type, otherwise I don't. Maybe I should just do that. Could be a later enhancement.
    def isMethodToInvoke(method: Method): Boolean =
      method.getName == methodNameToInvoke && method.getParameterTypes.length == 0 && !Modifier.isStatic(method.getModifiers()) &&
        (!isBooleanProperty || method.getReturnType == classOf[Boolean])

    def isGetMethodToInvoke(method: Method): Boolean =
      method.getName == methodNameToInvokeWithGet && method.getParameterTypes.length == 0 && !Modifier.isStatic(method.getModifiers()) &&
        (!isBooleanProperty || method.getReturnType == classOf[Boolean])

    val fieldOption = objectWithProperty.getClass.getFields.find(isFieldToAccess)

    val methodOption = objectWithProperty.getClass.getMethods.find(isMethodToInvoke)

    val getMethodOption = objectWithProperty.getClass.getMethods.find(isGetMethodToInvoke)

    (fieldOption, methodOption, getMethodOption) match {

      case (_, Some(method), _) => Some(method.invoke(objectWithProperty, Array[AnyRef](): _*))

      case (_, None, Some(getMethod)) => Some(getMethod.invoke(objectWithProperty, Array[AnyRef](): _*))

      case (Some(field), None, None) => Some(field.get(objectWithProperty))

      case (None, None, None) => None
    }
  }

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

}

import Helper.newTestFailedException
import Helper.accessProperty

/**
 * This trait is part of the ScalaTest matchers DSL. Please see the documentation for ShouldMatchers for an overview of
 * the matchers DSL.
 *
 * @author Bill Venners
 */
trait Matchers extends Assertions { matchers =>

  private def matchSymbolToPredicateMethod[S <: AnyRef](left: S, right: Symbol, hasArticle: Boolean, articleIsA: Boolean): MatchResult = {

    // If 'empty passed, rightNoTick would be "empty"
    val propertyName = right.name

    accessProperty(left, right, true) match {

      case None =>

        // if propertyName is '>, mangledPropertyName would be "$greater"
        val mangledPropertyName = transformOperatorChars(propertyName)

        // methodNameToInvoke would also be "empty"
        val methodNameToInvoke = mangledPropertyName

        // methodNameToInvokeWithIs would be "isEmpty"
        val methodNameToInvokeWithIs = "is"+ mangledPropertyName(0).toUpperCase + mangledPropertyName.substring(1)

        val firstChar = propertyName(0).toLowerCase
        val methodNameStartsWithVowel = firstChar == 'a' || firstChar == 'e' || firstChar == 'i' ||
          firstChar == 'o' || firstChar == 'u'

        throw newTestFailedException(
          FailureMessages(
            if (methodNameStartsWithVowel) "hasNeitherAnOrAnMethod" else "hasNeitherAOrAnMethod",
            left,
            UnquotedString(methodNameToInvoke),
            UnquotedString(methodNameToInvokeWithIs)
          )
        )

      case Some(result) =>

        val (wasNot, was) =
          if (hasArticle) {
            if (articleIsA) ("wasNotA", "wasA") else ("wasNotAn", "wasAn")
          }
          else ("wasNot", "was")

        MatchResult(
          result == true, // Right now I just leave the return value of accessProperty as Any
          FailureMessages(wasNot, left, UnquotedString(propertyName)),
          FailureMessages(was, left, UnquotedString(propertyName))
        )
    }
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class MatcherWrapper[T](leftMatcher: Matcher[T]) { matchersWrapper =>

// TODO: mention not short circuited, and the precendence is even between and and or

    /**
     * Returns a matcher whose <code>apply</code> method returns a <code>MatchResult</code>
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
          val leftMatchResult = leftMatcher(left)
          val rightMatchResult = rightMatcher(left) // Not short circuiting anymore
          if (!leftMatchResult.matches)
            MatchResult(
              false,
              leftMatchResult.failureMessage,
              leftMatchResult.negatedFailureMessage,
              leftMatchResult.midSentenceFailureMessage,
              leftMatchResult.midSentenceNegatedFailureMessage
            )
          else {
            MatchResult(
              rightMatchResult.matches,
              Resources("commaBut", leftMatchResult.negatedFailureMessage, rightMatchResult.midSentenceFailureMessage),
              Resources("commaAnd", leftMatchResult.negatedFailureMessage, rightMatchResult.midSentenceNegatedFailureMessage),
              Resources("commaBut", leftMatchResult.midSentenceNegatedFailureMessage, rightMatchResult.midSentenceFailureMessage),
              Resources("commaAnd", leftMatchResult.midSentenceNegatedFailureMessage, rightMatchResult.midSentenceNegatedFailureMessage)
            )
          }
        }
      }

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class AndHaveWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Array(1, 2) should (have length (2) and have length (3 - 1))
       *                                              ^
       * </pre>
       */
      def length(expectedLength: Long) = and(have.length(expectedLength))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Array(1, 2) should (have size (2) and have size (3 - 1))
       *                                            ^ 
       * </pre>
       */
      def size(expectedSize: Long) = and(have.size(expectedSize))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * Array(1, 2) should (have size (2) and have size (3 - 1))
     *                                   ^ 
     * </pre>
     */
    def and(haveWord: HaveWord): AndHaveWord = new AndHaveWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class AndContainWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Array(1, 2) should (contain element (2) and contain element (3 - 1))
       *                                                     ^
       * </pre>
       */
      def element[T](expectedElement: T) = matchersWrapper.and(matchers.contain.element(expectedElement))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Map("one" -> 1, "two" -> 2) should (contain key ("two") and contain key ("one"))
       *                                                                     ^
       * </pre>
       */
      def key[T](expectedElement: T) = matchersWrapper.and(matchers.contain.key(expectedElement))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Map("one" -> 1, "two" -> 2) should (contain value (2) and contain value (1))
       *                                                                   ^
       * </pre>
       */
      def value[T](expectedValue: T) = matchersWrapper.and(matchers.contain.value(expectedValue))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * Map("one" -> 1, "two" -> 2) should (contain key ("two") and contain key ("one"))
     *                                                         ^ 
     * </pre>
     */
    def and(containWord: ContainWord): AndContainWord = new AndContainWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class AndBeWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * isFileMock should (be a ('file) and be a ('file))
       *                                        ^
       * </pre>
       */
      def a(symbol: Symbol) = and(be.a(symbol))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * myFile should (be a (file) and be a (file))
       *                                        ^
       * </pre>
       */
      def a[T](bePropertyMatcher: BePropertyMatcher[T]) = and(be.a(bePropertyMatcher))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * isAppleMock should (be an ('apple) and be an ('apple))
       *                                           ^
       * </pre>
       */
      def an(symbol: Symbol) = and(be.an(symbol))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * isAppleMock should (be an (apple) and be an (apple))
       *                                           ^
       * </pre>
       */
      def an[T](bePropertyMatcher: BePropertyMatcher[T]) = and(be.an(bePropertyMatcher))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * obj should (be theSameInstanceAs (string) and be theSameInstanceAs (string))
       *                                                  ^
       * </pre>
       */
      def theSameInstanceAs(anyRef: AnyRef) = and(be.theSameInstanceAs(anyRef))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * isFileMock should (be a ('file) and be a ('file))
     *                                 ^
     * </pre>
     */
    def and(beWord: BeWord): AndBeWord = new AndBeWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class AndFullyMatchWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (fullyMatch regex (decimal) and fullyMatch regex (decimal))
       *                                                         ^
       * </pre>
       */
      def regex(regexString: String) = and(fullyMatch.regex(regexString))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (fullyMatch regex (decimalRegex) and fullyMatch regex (decimalRegex))
       *                                                              ^
       * </pre>
       */
      def regex(regex: Regex) = and(fullyMatch.regex(regex))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * "1.7" should (fullyMatch regex (decimalRegex) and fullyMatch regex (decimalRegex))
     *                                               ^
     * </pre>
     */
    def and(fullyMatchWord: FullyMatchWord): AndFullyMatchWord = new AndFullyMatchWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class AndIncludeWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (include regex (decimal) and include regex (decimal))
       *                                                   ^
       * </pre>
       */
      def regex(regexString: String) = and(include.regex(regexString))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (include regex (decimalRegex) and include regex (decimalRegex))
       *                                                        ^
       * </pre>
       */
      def regex(regex: Regex) = and(include.regex(regex))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "a1.7b" should (include substring ("1.7") and include substring ("1.7"))
       *                                                       ^
       * </pre>
       */
      def substring(expectedSubstring: String) = and(include.substring(expectedSubstring))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * "a1.7b" should (include substring ("1.7") and include substring ("1.7"))
     *                                           ^
     * </pre>
     */
    def and(includeWord: IncludeWord): AndIncludeWord = new AndIncludeWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class AndStartWithWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (startWith regex (decimal) and startWith regex (decimal))
       *                                                       ^
       * </pre>
       */
      def regex(regexString: String) = and(startWith.regex(regexString))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (startWith regex (decimalRegex) and startWith regex (decimalRegex))
       *                                                            ^
       * </pre>
       */
      def regex(regex: Regex) = and(startWith.regex(regex))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (startWith substring ("1.7") and startWith substring ("1.7"))
       *                                                         ^
       * </pre>
       */
      def substring(expectedSubstring: String) = and(startWith.substring(expectedSubstring))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * "1.7" should (startWith substring ("1.7") and startWith substring ("1.7"))
     *                                           ^
     * </pre>
     */
    def and(startWithWord: StartWithWord): AndStartWithWord = new AndStartWithWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class AndEndWithWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (endWith regex (decimal) and endWith regex (decimal))
       *                                                   ^
       * </pre>
       */
      def regex(regexString: String) = and(endWith.regex(regexString))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (endWith regex (decimalRegex) and endWith regex (decimalRegex))
       *                                                        ^
       * </pre>
       */
      def regex(regex: Regex) = and(endWith.regex(regex))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7b" should (endWith substring ("1.7b") and endWith substring ("7b"))
       *                                                       ^
       * </pre>
       */
      def substring(expectedSubstring: String) = and(endWith.substring(expectedSubstring))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * "1.7" should (endWith regex (decimalRegex) and endWith regex (decimalRegex))
     *                                            ^
     * </pre>
     */
    def and(endWithWord: EndWithWord): AndEndWithWord = new AndEndWithWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class AndNotWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * 1 should (not equal (2) and not equal (3 - 1))
       *                                 ^
       * </pre>
       */
      def equal(any: Any) =
        matchersWrapper.and(matchers.not.apply(matchers.equal(any)))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Array(1, 2) should (not have size (5) and not have length (3))
       *                                               ^
       * </pre>
       */
      def have(resultOfLengthWordApplication: ResultOfLengthWordApplication) =
        matchersWrapper.and(matchers.not.apply(matchers.have.length(resultOfLengthWordApplication.expectedLength)))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Array(1, 2) should (not have size (5) and not have size (3))
       *                                               ^
       * </pre>
       */
      def have(resultOfSizeWordApplication: ResultOfSizeWordApplication) =
        matchersWrapper.and(matchers.not.apply(matchers.have.size(resultOfSizeWordApplication.expectedSize)))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * book should (not have (title ("Moby Dick")) and not have (author ("Melville")))
       *                                                     ^
       * </pre>
       */
      def have[T](firstPropertyMatcher: HavePropertyMatcher[T, _], propertyMatchers: HavePropertyMatcher[T, _]*) =
        matchersWrapper.and(matchers.not.apply(matchers.have(firstPropertyMatcher, propertyMatchers: _*)))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * 5 should (not be < (2) and not be < (6))
       *                                ^
       * </pre>
       */
      def be[T](resultOfLessThanComparison: ResultOfLessThanComparison[T]) =
        matchersWrapper.and(matchers.not.be(resultOfLessThanComparison))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * 7 should (not be > (8) and not be > (6))
       *                                ^
       * </pre>
       */
      def be[T](resultOfGreaterThanComparison: ResultOfGreaterThanComparison[T]) =
        matchersWrapper.and(matchers.not.be(resultOfGreaterThanComparison))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * 2 should (not be <= (1) and not be <= (2))
       *                                 ^
       * </pre>
       */
      def be[T](resultOfLessThanOrEqualToComparison: ResultOfLessThanOrEqualToComparison[T]) =
        matchersWrapper.and(matchers.not.be(resultOfLessThanOrEqualToComparison))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * 7 should (not be >= (8) and not be >= (6))
       *                                 ^
       * </pre>
       */
      def be[T](resultOfGreaterThanOrEqualToComparison: ResultOfGreaterThanOrEqualToComparison[T]) =
        matchersWrapper.and(matchers.not.be(resultOfGreaterThanOrEqualToComparison))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * notEmptyMock should (not be ('empty) and not be ('empty))
       *                                              ^
       * </pre>
       */
      def be(symbol: Symbol) = matchersWrapper.and(matchers.not.be(symbol))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * 2 should (not be (odd) and not be (odd))
       *                                ^
       * </pre>
       */
      def be[T](beMatcher: BeMatcher[T]) = matchersWrapper.and(matchers.not.be(beMatcher))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * myFile should (not be (directory) and not be (directory))
       *                                              ^
       * </pre>
       */
      def be[T](bePropertyMatcher: BePropertyMatcher[T]) = matchersWrapper.and(matchers.not.be(bePropertyMatcher))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * isNotFileMock should (not be a ('file) and not be a ('file))
       *                                                ^
       * </pre>
       */
      def be[T](resultOfAWordApplication: ResultOfAWordToSymbolApplication) = matchersWrapper.and(matchers.not.be(resultOfAWordApplication))
// TODO drop the type param on the previous one

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * myFile should (not be a (directory) and not be a (directory))
       *                                             ^
       * </pre>
       */
      def be[T <: AnyRef](resultOfAWordApplication: ResultOfAWordToBePropertyMatcherApplication[T]) = matchersWrapper.and(matchers.not.be(resultOfAWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * isNotAppleMock should (not be an ('apple) and not be an ('apple)) 
       *                                                   ^
       * </pre>
       */
      def be[T](resultOfAnWordApplication: ResultOfAnWordToSymbolApplication) = matchersWrapper.and(matchers.not.be(resultOfAnWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * myFile should (not be an (directory) and not be an (directory))
       *                                              ^
       * </pre>
       */
      def be[T <: AnyRef](resultOfAnWordApplication: ResultOfAnWordToBePropertyMatcherApplication[T]) = matchersWrapper.and(matchers.not.be(resultOfAnWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * obj should (not be theSameInstanceAs (otherString) and not be theSameInstanceAs (otherString))
       *                                                            ^
       * </pre>
       */
      def be[T](resultOfTheSameInstanceAsApplication: ResultOfTheSameInstanceAsApplication) = matchersWrapper.and(matchers.not.be(resultOfTheSameInstanceAsApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * sevenDotOh should (not be (17.0 plusOrMinus 0.2) and not be (17.0 plusOrMinus 0.2))
       *                                                          ^
       * </pre>
       */
      def be(doubleTolerance: DoubleTolerance) = matchersWrapper.and(matchers.not.be(doubleTolerance))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * sevenDotOhFloat should (not be (17.0f plusOrMinus 0.2f) and not be (17.0f plusOrMinus 0.2f))
       *                                                                 ^
       * </pre>
       */
      def be(floatTolerance: FloatTolerance) = matchersWrapper.and(matchers.not.be(floatTolerance))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * sevenLong should (not be (17L plusOrMinus 2L) and not be (17L plusOrMinus 2L))
       *                                                       ^
       * </pre>
       */
      def be(longTolerance: LongTolerance) = matchersWrapper.and(matchers.not.be(longTolerance))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * sevenInt should (not be (17 plusOrMinus 2) and not be (17 plusOrMinus 2))
       *                                                    ^
       * </pre>
       */
      def be(intTolerance: IntTolerance) = matchersWrapper.and(matchers.not.be(intTolerance))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * sevenShort should (not be (17.toShort plusOrMinus 2.toShort) and not be (17.toShort plusOrMinus 2.toShort))
       *                                                                      ^
       * </pre>
       */
      def be(shortTolerance: ShortTolerance) = matchersWrapper.and(matchers.not.be(shortTolerance))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * sevenByte should ((not be (19.toByte plusOrMinus 2.toByte)) and (not be (19.toByte plusOrMinus 2.toByte)))
       *                                                                      ^
       * </pre>
       */
      def be(byteTolerance: ByteTolerance) = matchersWrapper.and(matchers.not.be(byteTolerance))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not fullyMatch regex ("bob") and not fullyMatch regex (decimal))
       *                                                     ^
       * </pre>
       */
      def fullyMatch(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.and(matchers.not.fullyMatch(resultOfRegexWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not include regex ("bob") and not include regex (decimal))
       *                                                     ^
       * </pre>
       */
      def include(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.and(matchers.not.include(resultOfRegexWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not include substring ("bob") and not include substring ("1.7"))
       *                                                      ^
       * </pre>
       */
      def include(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) =
        matchersWrapper.and(matchers.not.include(resultOfSubstringWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not startWith regex ("bob") and not startWith regex (decimal))
       *                                                    ^
       * </pre>
       */
      def startWith(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.and(matchers.not.startWith(resultOfRegexWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not startWith substring ("red") and not startWith substring ("1.7"))
       *                                                        ^
       * </pre>
       */
      def startWith(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) =
        matchersWrapper.and(matchers.not.startWith(resultOfSubstringWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not endWith regex ("bob") and not endWith regex (decimal))
       *                                                  ^
       * </pre>
       */
      def endWith(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.and(matchers.not.endWith(resultOfRegexWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not endWith substring ("fre") and not endWith substring ("1.7"))
       *                                                      ^
       * </pre>
       */
      def endWith(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) =
        matchersWrapper.and(matchers.not.endWith(resultOfSubstringWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Array(1, 2) should (not contain element (5) and not contain element (3))
       *                                                     ^
       * </pre>
       */
      def contain[T](resultOfElementWordApplication: ResultOfElementWordApplication[T]) =
        matchersWrapper.and(matchers.not.contain(resultOfElementWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Map("one" -> 1, "two" -> 2) should (not contain key ("five") and not contain key ("three"))
       *                                                                      ^
       * </pre>
       */
      def contain[T](resultOfKeyWordApplication: ResultOfKeyWordApplication[T]) =
        matchersWrapper.and(matchers.not.contain(resultOfKeyWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Map("one" -> 1, "two" -> 2) should (not contain value (5) and not contain value (3))
       *                                                                   ^
       * </pre>
       */
      def contain[T](resultOfValueWordApplication: ResultOfValueWordApplication[T]) =
        matchersWrapper.and(matchers.not.contain(resultOfValueWordApplication))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * Map("one" -> 1, "two" -> 2) should (not contain value (5) and not contain value (3))
     *                                                           ^
     * </pre>
     */
    def and(notWord: NotWord): AndNotWord = new AndNotWord

    /**
     * Returns a matcher whose <code>apply</code> method returns a <code>MatchResult</code>
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
          val leftMatchResult = leftMatcher(left)
          val rightMatchResult = rightMatcher(left) // Not short circuiting anymore
          if (leftMatchResult.matches)
            MatchResult(
              true,
              leftMatchResult.negatedFailureMessage,
              leftMatchResult.failureMessage,
              leftMatchResult.midSentenceNegatedFailureMessage,
              leftMatchResult.midSentenceFailureMessage,
            )
          else {
            MatchResult(
              rightMatchResult.matches,
              Resources("commaAnd", leftMatchResult.failureMessage, rightMatchResult.midSentenceFailureMessage),
              Resources("commaAnd", leftMatchResult.failureMessage, rightMatchResult.midSentenceNegatedFailureMessage),
              Resources("commaAnd", leftMatchResult.midSentenceFailureMessage, rightMatchResult.midSentenceFailureMessage),
              Resources("commaAnd", leftMatchResult.midSentenceFailureMessage, rightMatchResult.midSentenceNegatedFailureMessage)
            )
          }
        }
      }

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class OrHaveWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Array(1, 2) should (have length (2) and have length (3 - 1))
       *                                              ^
       * </pre>
       */
      def length(expectedLength: Long) = or(have.length(expectedLength))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Array(1, 2) should (have size (2) and have size (3 - 1))
       *                                       ^
       * </pre>
       */
      def size(expectedSize: Long) = or(have.size(expectedSize))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * Array(1, 2) should (have size (2) and have size (3 - 1))
     *                                   ^
     * </pre>
     */
    def or(haveWord: HaveWord): OrHaveWord = new OrHaveWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class OrContainWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Array(1, 2) should (contain element (2) or contain element (3 - 1))
       *                                                    ^
       * </pre>
       */
      def element[T](expectedElement: T) = matchersWrapper.or(matchers.contain.element(expectedElement))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Map("one" -> 1, "two" -> 2) should (contain key ("cat") or contain key ("one"))
       *                                                                    ^
       * </pre>
       */
      def key[T](expectedKey: T) = matchersWrapper.or(matchers.contain.key(expectedKey))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Map("one" -> 1, "two" -> 2) should (contain value (7) or contain value (1))
       *                                                                  ^
       * </pre>
       */
      def value[T](expectedValue: T) = matchersWrapper.or(matchers.contain.value(expectedValue))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * Map("one" -> 1, "two" -> 2) should (contain value (7) or contain value (1))
     *                                                       ^
     * </pre>
     */
    def or(containWord: ContainWord): OrContainWord = new OrContainWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class OrBeWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * isFileMock should (be a ('file) or be a ('directory))
       *                                       ^
       * </pre>
       */
      def a(symbol: Symbol) = or(be.a(symbol))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * isFileMock should (be a (file) or be a (directory))
       *                                      ^
       * </pre>
       */
      def a[T](bePropertyMatcher: BePropertyMatcher[T]) = or(be.a(bePropertyMatcher))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * appleMock should (be an ('orange) or be an ('apple))
       *                                         ^
       * </pre>
       */
      def an(symbol: Symbol) = or(be.an(symbol))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * appleMock should (be an (orange) or be an (apple))
       *                                        ^
       * </pre>
       */
      def an[T](bePropertyMatcher: BePropertyMatcher[T]) = or(be.an(bePropertyMatcher))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * obj should (be theSameInstanceAs (string) or be theSameInstanceAs (otherString))
       *                                                 ^
       * </pre>
       */
      def theSameInstanceAs(anyRef: AnyRef) = or(be.theSameInstanceAs(anyRef))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * isFileMock should (be a ('file) or be a ('directory))
     *                                 ^
     * </pre>
     */
    def or(beWord: BeWord): OrBeWord = new OrBeWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class OrFullyMatchWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (fullyMatch regex ("hello") or fullyMatch regex (decimal))
       *                                                        ^
       * </pre>
       */
      def regex(regexString: String) = or(fullyMatch.regex(regexString))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (fullyMatch regex ("hello") or fullyMatch regex (decimal))
       *                                                        ^
       * </pre>
       */
      def regex(regex: Regex) = or(fullyMatch.regex(regex))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * "1.7" should (fullyMatch regex ("hello") or fullyMatch regex (decimal))
     *                                          ^
     * </pre>
     */
    def or(fullyMatchWord: FullyMatchWord): OrFullyMatchWord = new OrFullyMatchWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class OrIncludeWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (include regex ("hello") or include regex (decimal))
       *                                                  ^
       * </pre>
       */
      def regex(regexString: String) = or(include.regex(regexString))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (include regex ("hello") or include regex (decimal))
       *                                                  ^
       * </pre>
       */
      def regex(regex: Regex) = or(include.regex(regex))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "a1.7b" should (include substring ("1.7") or include substring ("1.7"))
       *                                                      ^
       * </pre>
       */
      def substring(expectedSubstring: String) = or(include.substring(expectedSubstring))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * "a1.7b" should (include substring ("1.7") or include substring ("1.7"))
     *                                           ^
     * </pre>
     */
    def or(includeWord: IncludeWord): OrIncludeWord = new OrIncludeWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class OrStartWithWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (startWith regex ("hello") or startWith regex (decimal))
       *                                                      ^
       * </pre>
       */
      def regex(regexString: String) = or(startWith.regex(regexString))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (startWith regex ("hello") or startWith regex (decimal))
       *                                                      ^
       * </pre>
       */
      def regex(regex: Regex) = or(startWith.regex(regex))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (startWith substring ("hello") or startWith substring ("1.7"))
       *                                                          ^
       * </pre>
       */
      def substring(expectedSubstring: String) = or(startWith.substring(expectedSubstring))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * "1.7" should (startWith substring ("hello") or startWith substring ("1.7"))
     *                                             ^
     * </pre>
     */
    def or(startWithWord: StartWithWord): OrStartWithWord = new OrStartWithWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class OrEndWithWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (endWith regex ("hello") or endWith regex (decimal))
       *                                                  ^
       * </pre>
       */
      def regex(regexString: String) = or(endWith.regex(regexString))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7" should (endWith regex ("hello") or endWith regex (decimal))
       *                                                  ^
       * </pre>
       */
      def regex(regex: Regex) = or(endWith.regex(regex))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "1.7b" should (endWith substring ("hello") or endWith substring ("7b"))
       *                                                       ^
       * </pre>
       */
      def substring(expectedSubstring: String) = or(endWith.substring(expectedSubstring))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * "1.7b" should (endWith substring ("hello") or endWith substring ("7b"))
     *                                            ^
     * </pre>
     */
    def or(endWithWord: EndWithWord): OrEndWithWord = new OrEndWithWord

    /**
     * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
     * the matchers DSL.
     *
     * @author Bill Venners
     */
    class OrNotWord {

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * 1 should (not equal (1) or not equal (2))
       *                                ^
       * </pre>
       */
      def equal(any: Any) =
        matchersWrapper.or(matchers.not.apply(matchers.equal(any)))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Array(1, 2) should (not have length (2) or not have length (3))
       *                                                ^
       * </pre>
       */
      def have(resultOfLengthWordApplication: ResultOfLengthWordApplication) =
        matchersWrapper.or(matchers.not.apply(matchers.have.length(resultOfLengthWordApplication.expectedLength)))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Array(1, 2) should (not have size (2) or not have size (3))
       *                                              ^
       * </pre>
       */
      def have(resultOfSizeWordApplication: ResultOfSizeWordApplication) =
        matchersWrapper.or(matchers.not.apply(matchers.have.size(resultOfSizeWordApplication.expectedSize)))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * book should (not have (title ("Moby Dick")) or not have (author ("Melville")))
       *                                                    ^
       * </pre>
       */
      def have[T](firstPropertyMatcher: HavePropertyMatcher[T, _], propertyMatchers: HavePropertyMatcher[T, _]*) =
        matchersWrapper.or(matchers.not.apply(matchers.have(firstPropertyMatcher, propertyMatchers: _*)))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * 5 should (not be < (7) or not be < (8))
       *                               ^
       * </pre>
       */
      def be[T](resultOfLessThanComparison: ResultOfLessThanComparison[T]) =
        matchersWrapper.or(matchers.not.be(resultOfLessThanComparison))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * 7 should (not be > (5) or not be > (6))
       *                               ^
       * </pre>
       */
      def be[T](resultOfGreaterThanComparison: ResultOfGreaterThanComparison[T]) =
        matchersWrapper.or(matchers.not.be(resultOfGreaterThanComparison))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * 2 should (not be <= (3) or not be <= (2))
       *                                ^
       * </pre>
       */
      def be[T](resultOfLessThanOrEqualToComparison: ResultOfLessThanOrEqualToComparison[T]) =
        matchersWrapper.or(matchers.not.be(resultOfLessThanOrEqualToComparison))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * 8 should (not be >= (7) or not be >= (6))
       *                                ^
       * </pre>
       */
      def be[T](resultOfGreaterThanOrEqualToComparison: ResultOfGreaterThanOrEqualToComparison[T]) =
        matchersWrapper.or(matchers.not.be(resultOfGreaterThanOrEqualToComparison))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * notEmptyMock should (not be ('full) or not be ('empty))
       *                                            ^
       * </pre>
       */
      def be[T](symbol: Symbol) = matchersWrapper.or(matchers.not.be(symbol))
// TODO drop the type parameter

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * 2 should (not be (even) or not be (odd))
       *                                ^
       * </pre>
       */
      def be[T](beMatcher: BeMatcher[T]) = matchersWrapper.or(matchers.not.be(beMatcher))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * myFile should (not be (directory) or not be (file))
       *                                          ^
       * </pre>
       */
      def be[T](bePropertyMatcher: BePropertyMatcher[T]) = matchersWrapper.or(matchers.not.be(bePropertyMatcher))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * isNotFileMock should (not be a ('directory) or not be a ('file))
       *                                                    ^
       * </pre>
       */
      def be[T](resultOfAWordApplication: ResultOfAWordToSymbolApplication) = matchersWrapper.or(matchers.not.be(resultOfAWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * myFile should (not be a (directory) or not be a (file))
       *                                            ^
       * </pre>
       */
      def be[T <: AnyRef](resultOfAWordApplication: ResultOfAWordToBePropertyMatcherApplication[T]) = matchersWrapper.or(matchers.not.be(resultOfAWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * notAppleMock should (not be an ('apple) or not be an ('apple))
       *                                                ^
       * </pre>
       */
      def be[T](resultOfAnWordApplication: ResultOfAnWordToSymbolApplication) = matchersWrapper.or(matchers.not.be(resultOfAnWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * myFile should (not be an (directory) or not be an (file))
       *                                             ^
       * </pre>
       */
      def be[T <: AnyRef](resultOfAnWordApplication: ResultOfAnWordToBePropertyMatcherApplication[T]) = matchersWrapper.or(matchers.not.be(resultOfAnWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * obj should (not be theSameInstanceAs (otherString) or not be theSameInstanceAs (string))
       *                                                           ^
       * </pre>
       */
      def be[T](resultOfTheSameInstanceAsApplication: ResultOfTheSameInstanceAsApplication) = matchersWrapper.or(matchers.not.be(resultOfTheSameInstanceAsApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * sevenDotOh should (not be (17.0 plusOrMinus 0.2) or not be (17.0 plusOrMinus 0.2))
       *                                                         ^
       * </pre>
       */
      def be(doubleTolerance: DoubleTolerance) = matchersWrapper.or(matchers.not.be(doubleTolerance))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * sevenDotOhFloat should (not be (17.0f plusOrMinus 0.2f) or not be (17.0f plusOrMinus 0.2f))
       *                                                                ^
       * </pre>
       */
      def be(floatTolerance: FloatTolerance) = matchersWrapper.or(matchers.not.be(floatTolerance))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * sevenLong should (not be (17L plusOrMinus 2L) or not be (17L plusOrMinus 2L))
       *                                                      ^
       * </pre>
       */
      def be(longTolerance: LongTolerance) = matchersWrapper.or(matchers.not.be(longTolerance))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * sevenInt should (not be (17 plusOrMinus 2) or not be (17 plusOrMinus 2))
       *                                                   ^
       * </pre>
       */
      def be(intTolerance: IntTolerance) = matchersWrapper.or(matchers.not.be(intTolerance))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * sevenShort should (not be (17.toShort plusOrMinus 2.toShort) or not be (17.toShort plusOrMinus 2.toShort))
       *                                                                     ^
       * </pre>
       */
      def be(shortTolerance: ShortTolerance) = matchersWrapper.or(matchers.not.be(shortTolerance))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * sevenByte should ((not be (19.toByte plusOrMinus 2.toByte)) or (not be (19.toByte plusOrMinus 2.toByte)))
       *                                                                     ^
       * </pre>
       */
      def be(byteTolerance: ByteTolerance) = matchersWrapper.or(matchers.not.be(byteTolerance))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not fullyMatch regex ("fred") or not fullyMatch regex (decimal))
       *                                                     ^
       * </pre>
       */
      def fullyMatch(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.or(matchers.not.fullyMatch(resultOfRegexWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not include regex ("fred") or not include regex (decimal))
       *                                                  ^
       * </pre>
       */
      def include(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.or(matchers.not.include(resultOfRegexWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not include substring ("bob") or not include substring ("1.7"))
       *                                                     ^
       * </pre>
       */
      def include(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) =
        matchersWrapper.or(matchers.not.include(resultOfSubstringWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not startWith regex ("bob") or not startWith regex (decimal))
       *                                                   ^
       * </pre>
       */
      def startWith(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.or(matchers.not.startWith(resultOfRegexWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not startWith substring ("fred") or not startWith substring ("1.7"))
       *                                                        ^
       * </pre>
       */
      def startWith(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) =
        matchersWrapper.or(matchers.not.startWith(resultOfSubstringWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not endWith regex ("bob") or not endWith regex (decimal))
       *                                                 ^
       * </pre>
       */
      def endWith(resultOfRegexWordApplication: ResultOfRegexWordApplication) =
        matchersWrapper.or(matchers.not.endWith(resultOfRegexWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * "fred" should (not endWith substring ("fred") or not endWith substring ("1.7"))
       *                                                      ^
       * </pre>
       */
      def endWith(resultOfSubstringWordApplication: ResultOfSubstringWordApplication) =
        matchersWrapper.or(matchers.not.endWith(resultOfSubstringWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Array(1, 2) should (not contain element (1) or not contain element (3))
       *                                                    ^
       * </pre>
       */
      def contain[T](resultOfElementWordApplication: ResultOfElementWordApplication[T]) =
        matchersWrapper.or(matchers.not.contain(resultOfElementWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Map("one" -> 1, "two" -> 2) should (not contain key ("two") or not contain key ("three"))
       *                                                                    ^
       * </pre>
       */
      def contain[T](resultOfKeyWordApplication: ResultOfKeyWordApplication[T]) =
        matchersWrapper.or(matchers.not.contain(resultOfKeyWordApplication))

      /**
       * This method enables the following syntax:
       *
       * <pre>
       * Map("one" -> 1, "two" -> 2) should (not contain value (2) or not contain value (3))
       *                                                                  ^
       * </pre>
       */
      def contain[T](resultOfValueWordApplication: ResultOfValueWordApplication[T]) =
        matchersWrapper.or(matchers.not.contain(resultOfValueWordApplication))
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * Map("one" -> 1, "two" -> 2) should (not contain value (2) or not contain value (3))
     *                                                           ^
     * </pre>
     */
    def or(notWord: NotWord): OrNotWord = new OrNotWord
  }

  /**
   * This implicit conversion method enables ScalaTest matchers expressions that involve <code>and</code> and <code>or</code>.
   */
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
  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfContainWordForMap[K, V](left: scala.collection.Map[K, V], shouldBeTrue: Boolean) extends ResultOfContainWordForIterable[Tuple2[K, V]](left, shouldBeTrue) {

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * map should contain key ("one")
     *                    ^
     * </pre>
     */
    def key(expectedKey: K) {
      if (left.contains(expectedKey) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotContainKey" else "containedKey",
            left,
            expectedKey)
        )
    }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * map should contain value (1)
     *                    ^
     * </pre>
     */
    def value(expectedValue: V) {
      if (left.values.contains(expectedValue) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotContainValue" else "containedValue",
            left,
            expectedValue)
        )
    }
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfContainWordForJavaMap[K, V](left: java.util.Map[K, V], shouldBeTrue: Boolean) {

    /**
     * This method enables the following syntax (<code>javaMap</code> is a <code>java.util.Map</code>):
     *
     * <pre>
     * javaMap should contain key ("two")
     *                        ^
     * </pre>
     */
    def key(expectedKey: K) {
      if (left.containsKey(expectedKey) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotContainKey" else "containedKey",
            left,
            expectedKey)
        )
    }

    /**
     * This method enables the following syntax (<code>javaMap</code> is a <code>java.util.Map</code>):
     *
     * <pre>
     * javaMap should contain value ("2")
     *                        ^
     * </pre>
     */
    def value(expectedValue: V) {
      if (left.containsValue(expectedValue) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotContainValue" else "containedValue",
            left,
            expectedValue)
        )
    }
  }

  /**
   * This implicit conversion method enables the following syntax (<code>javaColl</code> is a <code>java.util.Collection</code>):
   *
   * <pre>
   * javaColl should (contain element ("two"))
   * </pre>
   *
   * The <code>(contain element ("two"))</code> expression will result in a <code>Matcher[scala.Iterable[String]]</code>. This
   * implicit conversion method will convert that matcher to a <code>Matcher[java.util.Collection[String]]</code>.
   */
  protected implicit def convertIterableMatcherToJavaCollectionMatcher[T](iterableMatcher: Matcher[Iterable[T]]) = 
    new Matcher[java.util.Collection[T]] {
      def apply(left: java.util.Collection[T]) = {
        val iterable = new Iterable[T] {
          def elements = new Iterator[T] {
            private val javaIterator = left.iterator
            def next: T = javaIterator.next
            def hasNext: Boolean = javaIterator.hasNext
          }
          override def toString = left.toString
        }
        iterableMatcher.apply(iterable)
      }
    }

  /**
   * This implicit conversion method enables the following syntax (<code>javaMap</code> is a <code>java.util.Map</code>):
   *
   * <pre>
   * javaMap should (contain key ("two"))
   * </pre>
   *
   * The <code>(contain key ("two"))</code> expression will result in a <code>Matcher[scala.collection.Map[String, Any]]</code>. This
   * implicit conversion method will convert that matcher to a <code>Matcher[java.util.Map[String, Any]]</code>.
   */
  protected implicit def convertMapMatcherToJavaMapMatcher[K, V](mapMatcher: Matcher[scala.collection.Map[K, V]]) = 
    new Matcher[java.util.Map[K, V]] {
      def apply(left: java.util.Map[K, V]) = {
        // Even though the java map is mutable I just wrap it it to a plain old Scala map, because
        // I have no intention of mutating it.
        val scalaMap = new scala.collection.Map[K, V] {
          def size: Int = left.size
          def get(key: K): Option[V] =
            if (left.containsKey(key)) Some(left.get(key)) else None
          def elements = new Iterator[(K, V)] {
            private val javaIterator = left.keySet.iterator
            def next: (K, V) = {
              val nextKey = javaIterator.next
              (nextKey, left.get(nextKey))
            }
            def hasNext: Boolean = javaIterator.hasNext
          }
          override def toString = left.toString
        }
        mapMatcher.apply(scalaMap)
      }
    }

  // Ack. The above conversion doesn't apply to java.util.Maps, because java.util.Map is not a subinterface
  // of java.util.Collection. But right now Matcher[Iterable] supports only "contain element" and "have size"
  // syntax, and thus that should work on Java maps too, why not. Well I'll tell you why not. It is too complicated.
  // Since java Map is not a java Collection, I'll say the contain syntax doesn't work on it. But you can say
  // have key.

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  private[scalatest] class BehaveWord

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ContainWord {

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * Array(1, 2) should (contain element (2) and contain element (1))
     *                             ^
     * </pre>
     */
    def element[T](expectedElement: T): Matcher[Iterable[T]] =
      new Matcher[Iterable[T]] {
        def apply(left: Iterable[T]) =
          MatchResult(
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
    /**
     * This method enables the following syntax:
     *
     * <pre>
     * map should (contain key ("fifty five") or contain key ("twenty two"))
     *                     ^
     * </pre>
     *
     * The map's value type parameter cannot be inferred because only a key type is provided in
     * an expression like <code>(contain key ("fifty five"))</code>. The matcher returned
     * by this method matches <code>scala.collection.Map</code>s with the inferred key type and value type <code>Any</code>. Given
     * <code>Map</code> is covariant in its value type, and <code>Matcher</code> is contravariant in
     * its type parameter, a <code>Matcher[Map[Int, Any]]</code>, for example, is a subtype of <code>Matcher[Map[Int, String]]</code>.
     * This will enable the matcher returned by this method to be used against any <code>Map</code> that has
     * the inferred key type.
     */
    def key[K](expectedKey: K): Matcher[scala.collection.Map[K, Any]] =
      new Matcher[scala.collection.Map[K, Any]] {
        def apply(left: scala.collection.Map[K, Any]) =
          MatchResult(
            left.contains(expectedKey),
            FailureMessages("didNotContainKey", left, expectedKey),
            FailureMessages("containedKey", left, expectedKey)
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
    /**
     * This method enables the following syntax:
     *
     * <pre>
     * Map("one" -> 1, "two" -> 2) should (not contain value (5) and not contain value (3))
     *                                                 ^
     * </pre>
     *
     * The map's key type parameter cannot be inferred because only a value type is provided in
     * an expression like <code>(contain value (5))</code>. The matcher returned
     * by this method matches <code>scala.collection.Map</code>s with the inferred value type and the existential key
     * type <code>[K] forSome { type K }</code>. Even though <code>Matcher</code> is contravariant in its type parameter, because
     * <code>Map</code> is nonvariant in its key type, 
     * a <code>Matcher[Map[Any, Int]]</code>, for example, is <em>not</em> a subtype of <code>Matcher[Map[String, Int]]</code>,
     * so the key type parameter of the <code>Map</code> returned by this method cannot be <code>Any</code>. By making it
     * an existential type, the Scala compiler will not infer it to anything more specific.
     * This will enable the matcher returned by this method to be used against any <code>Map</code> that has
     * the inferred value type.
     *
     */
    def value[V](expectedValue: V): Matcher[scala.collection.Map[K, V] forSome { type K }] =
      new Matcher[scala.collection.Map[K, V] forSome { type K }] {
        def apply(left: scala.collection.Map[K, V] forSome { type K }) =
          MatchResult(
            left.values.contains(expectedValue),
            FailureMessages("didNotContainValue", left, expectedValue),
            FailureMessages("containedValue", left, expectedValue)
          )
      }
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class IncludeWord {

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * "1.7" should (include substring ("1.7") and include substring ("1.8"))
     *                       ^
     * </pre>
     */
    def substring(expectedSubstring: String): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) =
          MatchResult(
            left.indexOf(expectedSubstring) >= 0, 
            FailureMessages("didNotIncludeSubstring", left, expectedSubstring),
            FailureMessages("includedSubstring", left, expectedSubstring)
          )
      }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * val decimal = """(-)?(\d+)(\.\d*)?"""
     * "a1.7b" should (include regex (decimal) and include regex (decimal))
     *                         ^
     * </pre>
     */
    def regex[T <: String](right: T): Matcher[T] = regex(right.r)

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * val decimalRegex = """(-)?(\d+)(\.\d*)?""".r
     * "a1.7" should (include regex (decimalRegex) and include regex (decimalRegex))
     *                        ^
     * </pre>
     */
    def regex(expectedRegex: Regex): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) =
          MatchResult(
            expectedRegex.findFirstIn(left).isDefined,
            FailureMessages("didNotIncludeRegex", left, expectedRegex),
            FailureMessages("includedRegex", left, expectedRegex)
          )
      }
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class StartWithWord {

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * "1.7b" should (startWith substring ("1.7") and startWith substring ("1.7b"))
     *                          ^
     * </pre>
     */
    def substring[T <: String](right: T) =
      new Matcher[T] {
        def apply(left: T) =
          MatchResult(
            left startsWith right,
            FailureMessages("didNotStartWith", left, right),
            FailureMessages("startedWith", left, right)
          )
      }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * val decimal = """(-)?(\d+)(\.\d*)?"""
     * "1.7b" should (startWith regex (decimal) and startWith regex (decimal))
     *                          ^
     * </pre>
     */
    def regex[T <: String](right: T): Matcher[T] = regex(right.r)

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * val decimalRegex = """(-)?(\d+)(\.\d*)?""".r
     * "1.7" should (startWith regex (decimalRegex) and startWith regex (decimalRegex))
     *                         ^
     * </pre>
     */
    def regex(rightRegex: Regex): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) =
          MatchResult(
            rightRegex.pattern.matcher(left).lookingAt,
            FailureMessages("didNotStartWithRegex", left, rightRegex),
            FailureMessages("startedWithRegex", left, rightRegex)
          )
      }
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class EndWithWord {

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * "1.7b" should (endWith substring ("1.7b") and endWith substring ("7b"))
     *                        ^
     * </pre>
     */
    def substring[T <: String](right: T) =
      new Matcher[T] {
        def apply(left: T) =
          MatchResult(
            left endsWith right,
            FailureMessages("didNotEndWith", left, right),
            FailureMessages("endedWith", left, right)
          )
      }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * val decimal = """(-)?(\d+)(\.\d*)?"""
     * "b1.7" should (endWith regex (decimal) and endWith regex (decimal))
     *                        ^
     * </pre>
     */
    def regex[T <: String](right: T): Matcher[T] = regex(right.r)

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * val decimalRegex = """(-)?(\d+)(\.\d*)?""".r
     * "b1.7" should (endWith regex (decimalRegex) and endWith regex (decimalRegex))
     *                        ^
     * </pre>
     */
    def regex(rightRegex: Regex): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) = {
          val allMatches = rightRegex.findAllIn(left)
          MatchResult(
            allMatches.hasNext && (allMatches.end == left.length),
            FailureMessages("didNotEndWithRegex", left, rightRegex),
            FailureMessages("endedWithRegex", left, rightRegex)
          )
        }
      }
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class FullyMatchWord {

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * val decimal = """(-)?(\d+)(\.\d*)?"""
     * "1.7" should (fullyMatch regex (decimal) and fullyMatch regex (decimal))
     *                          ^
     * </pre>
     */
    def regex(rightRegexString: String): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) =
          MatchResult(
            java.util.regex.Pattern.matches(rightRegexString, left),
            FailureMessages("didNotFullyMatchRegex", left, UnquotedString(rightRegexString)),
            FailureMessages("fullyMatchedRegex", left, UnquotedString(rightRegexString))
          )
      }

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * val decimalRegex = """(-)?(\d+)(\.\d*)?""".r
     * "1.7" should (fullyMatch regex (decimalRegex) and fullyMatch regex (decimalRegex))
     *                          ^
     * </pre>
     */
    def regex(rightRegex: Regex): Matcher[String] =
      new Matcher[String] {
        def apply(left: String) =
          MatchResult(
            rightRegex.pattern.matcher(left).matches,
            FailureMessages("didNotFullyMatchRegex", left, rightRegex),
            FailureMessages("fullyMatchedRegex", left, rightRegex)
          )
      }
  }

// TODO: Drop the getLength and getSize field conversions, so that this is consistent with
// what I do in symbol HavePropertyMatchers. Actually, no, keep them, because a val geLength is a 
// perfectly valid Scala way to get a JavaBean property in the bytecodes.

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * <p>
   * Subclasses of this abstract class are used as the result of implicit conversions from the various structural types that
   * are considered to represent length: <code>length</code> or <code>getLength</code> methods or fields that return <code>Int</code>
   * or <code>Long</code>. This enables the <code>have length (7)</code> syntax to be used with any object that has a length.
   * The implicit conversion methods that result in this type are:
   * </p>
   *
   * <ul>
   * <li><code>convertLengthFieldToIntLengthWrapper</code></li>
   * <li><code>convertLengthMethodToIntLengthWrapper</code></li>
   * <li><code>convertGetLengthFieldToIntLengthWrapper</code></li>
   * <li><code>convertGetLengthMethodToIntLengthWrapper</code></li>
   * <li><code>convertLengthFieldToLongLengthWrapper</code></li>
   * <li><code>convertLengthFieldToLongLengthWrapper</code></li>
   * <li><code>convertGetLengthFieldToLongLengthWrapper</code></li>
   * <li><code>convertGetLengthMethodToLongLengthWrapper</code></li>
   * </ul>
   *
   * @author Bill Venners
   */
  abstract class LengthWrapper {
    def length: Long
  }

  /**
   * This implicit conversion method converts an object with a <code>length</code> field of type <code>Int</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have length (7)</code> syntax.
   */
  implicit def convertLengthFieldToIntLengthWrapper(o: { val length: Int }) =
    new LengthWrapper {
      def length = o.length
    }

  /**
   * This implicit conversion method converts an object with a <code>length</code> method of type <code>Int</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have length (7)</code> syntax.
   */
  implicit def convertLengthMethodToIntLengthWrapper(o: { def length(): Int }) =
    new LengthWrapper {
      def length = o.length()
    }

  /**
   * This implicit conversion method converts an object with a <code>getLength</code> field of type <code>Int</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have length (7)</code> syntax.
   */
  implicit def convertGetLengthFieldToIntLengthWrapper(o: { val getLength: Int }) =
    new LengthWrapper {
      def length = o.getLength
    }

  /**
   * This implicit conversion method converts an object with a <code>getLength</code> method of type <code>Int</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have length (7)</code> syntax.
   */
  implicit def convertGetLengthMethodToIntLengthWrapper(o: { def getLength(): Int }) =
    new LengthWrapper {
      def length = o.getLength()
    }

  /**
   * This implicit conversion method converts an object with a <code>length</code> field of type <code>Long</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have length (7)</code> syntax.
   */
  implicit def convertLengthFieldToLongLengthWrapper(o: { val length: Long }) =
    new LengthWrapper {
      def length = o.length
    }

  /**
   * This implicit conversion method converts an object with a <code>length</code> method of type <code>Long</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have length (7)</code> syntax.
   */
  implicit def convertLengthMethodToLongLengthWrapper(o: { def length(): Long }) =
    new LengthWrapper {
      def length = o.length()
    }

  /**
   * This implicit conversion method converts an object with a <code>getLength</code> field of type <code>Long</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have length (7)</code> syntax.
   */
  implicit def convertGetLengthFieldToLongLengthWrapper(o: { val getLength: Long }) =
    new LengthWrapper {
      def length = o.getLength
    }

  /**
   * This implicit conversion method converts an object with a <code>getLength</code> method of type <code>Long</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have length (7)</code> syntax.
   */
  implicit def convertGetLengthMethodToLongLengthWrapper(o: { def getLength(): Long }) =
    new LengthWrapper {
      def length = o.getLength()
    }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * <p>
   * Subclasses of this abstract class are used as the result of implicit conversions from the various structural types that
   * are considered to represent size: <code>size</code> or <code>getSize</code> methods or fields that return <code>Int</code>
   * or <code>Long</code>. This enables the <code>have size (7)</code> syntax to be used with any object that has a size.
   * The implicit conversion methods that result in this type are:
   * </p>
   *
   * <ul>
   * <li><code>convertSizeFieldToIntSizeWrapper</code></li>
   * <li><code>convertSizeMethodToIntSizeWrapper</code></li>
   * <li><code>convertGetSizeFieldToIntSizeWrapper</code></li>
   * <li><code>convertGetSizeMethodToIntSizeWrapper</code></li>
   * <li><code>convertSizeFieldToLongSizeWrapper</code></li>
   * <li><code>convertSizeFieldToLongSizeWrapper</code></li>
   * <li><code>convertGetSizeFieldToLongSizeWrapper</code></li>
   * <li><code>convertGetSizeMethodToLongSizeWrapper</code></li>
   * </ul>
   *
   * @author Bill Venners
   */
  abstract class SizeWrapper {
    def size: Long
  }

  /**
   * This implicit conversion method converts an object with a <code>size</code> field of type <code>Int</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have size (7)</code> syntax.
   */
  implicit def convertSizeFieldToIntSizeWrapper(o: { val size: Int }) =
    new SizeWrapper {
      def size = o.size
    }

  /**
   * This implicit conversion method converts an object with a <code>size</code> method of type <code>Int</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have size (7)</code> syntax.
   */
  implicit def convertSizeMethodToIntSizeWrapper(o: { def size(): Int }) =
    new SizeWrapper {
      def size = o.size()
    }

  /**
   * This implicit conversion method converts an object with a <code>getSize</code> field of type <code>Int</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have size (7)</code> syntax.
   */
  implicit def convertGetSizeFieldToIntSizeWrapper(o: { val getSize: Int }) =
    new SizeWrapper {
      def size = o.getSize
    }

  /**
   * This implicit conversion method converts an object with a <code>getSize</code> method of type <code>Int</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have size (7)</code> syntax.
   */
  implicit def convertGetSizeMethodToIntSizeWrapper(o: { def getSize(): Int }) =
    new SizeWrapper {
      def size = o.getSize()
    }

  /**
   * This implicit conversion method converts an object with a <code>size</code> field of type <code>Long</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have size (7)</code> syntax.
   */
  implicit def convertSizeFieldToLongSizeWrapper(o: { val size: Long }) =
    new SizeWrapper {
      def size = o.size
    }

  /**
   * This implicit conversion method converts an object with a <code>size</code> method of type <code>Long</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have size (7)</code> syntax.
   */
  implicit def convertSizeMethodToLongSizeWrapper(o: { def size(): Long }) =
    new SizeWrapper {
      def size = o.size()
    }

  /**
   * This implicit conversion method converts an object with a <code>getSize</code> field of type <code>Long</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have size (7)</code> syntax.
   */
  implicit def convertGetSizeFieldToLongSizeWrapper(o: { val getSize: Long }) =
    new SizeWrapper {
      def size = o.getSize
    }

  /**
   * This implicit conversion method converts an object with a <code>getSize</code> method of type <code>Long</code> to a
   * <code>LengthWrapper</code>, to enable that object to be used with the <code>have size (7)</code> syntax.
   */
  implicit def convertGetSizeMethodToLongSizeWrapper(o: { def getSize(): Long }) =
    new SizeWrapper {
      def size = o.getSize()
    }
 
  // This guy is generally done through an implicit conversion from a symbol. It takes that symbol, and 
  // then represents an object with an apply method. So it gives an apply method to symbols.
  // book should have ('author ("Gibson"))
  //                   ^ // Basically this 'author symbol gets converted into this class, and its apply  method takes "Gibson"
  // TODO, put the documenation of the details of the algo for selecting a method or field to use here.
  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * <p>
   * This class is used as the result of an implicit conversion from class <code>Symbol</code>, to enable symbols to be
   * used in <code>have ('author ("Dickens"))</code> syntax. The name of the implicit conversion method is
   * <code>convertSymbolToHavePropertyMatcherGenerator</code>.
   * </p>
   *
   * @author Bill Venners
   */
  protected class HavePropertyMatcherGenerator(symbol: Symbol) {

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * book should have ('title ("A Tale of Two Cities"))
     *                          ^
     * </pre>
     * 
     * <p>
     * This class has an <code>apply</code> method that will produce a <code>HavePropertyMatcher[AnyRef, Any]</code>.
     * The implicit conversion method, <code>convertSymbolToHavePropertyMatcherGenerator</code>, will cause the 
     * above line of code to be eventually transformed into:
     * </p>
     * 
     * <pre>
     * book should have (convertSymbolToHavePropertyMatcherGenerator('title).apply("A Tale of Two Cities"))
     * </pre>
     */
    def apply(expectedValue: Any) =
      new HavePropertyMatcher[AnyRef, Any] {

        /**
         * This method enables the following syntax:
         *
         * <pre>
         * book should have ('title ("A Tale of Two Cities"))
         * </pre>
         * 
         * <p>
         * This method uses reflection to discover a field or method with a name that indicates it represents
         * the value of the property with the name contained in the <code>Symbol</code> passed to the 
         * <code>HavePropertyMatcherGenerator</code>'s constructor. The field or method must be public. To be a
         * candidate, a field must have the name <code>symbol.name</code>, so if <code>symbol</code> is <code>'title</code>,
         * the field name sought will be <code>"title"</code>. To be a candidate, a method must either have the name
         * <code>symbol.name</code>, or have a JavaBean-style <code>get</code> or <code>is</code>. If the type of the
         * passed <code>expectedValue</code> is <code>Boolean</code>, <code>"is"</code> is prepended, else <code>"get"</code>
         * is prepended. Thus if <code>'title</code> is passed as <code>symbol</code>, and the type of the <code>expectedValue</code> is
         * <code>String</code>, a method named <code>getTitle</code> will be considered a candidate (the return type
         * of <code>getTitle</code> will not be checked, so it need not be <code>String</code>. By contrast, if <code>'defined</code>
         * is passed as <code>symbol</code>, and the type of the <code>expectedValue</code> is <code>Boolean</code>, a method
         * named <code>isTitle</code> will be considered a candidate so long as its return type is <code>Boolean</code>.
         * </p>
         * TODO continue the story
         */
        def apply(objectWithProperty: AnyRef): HavePropertyMatchResult[Any] = {

          // If 'empty passed, propertyName would be "empty"
          val propertyName = symbol.name

          val isBooleanProperty =
            expectedValue match {
              case o: Boolean => true
              case _ => false
            }

          accessProperty(objectWithProperty, symbol, isBooleanProperty) match {

            case None =>

              // if propertyName is '>, mangledPropertyName would be "$greater"
              val mangledPropertyName = transformOperatorChars(propertyName)

              // methodNameToInvoke would also be "title"
              val methodNameToInvoke = mangledPropertyName

              // methodNameToInvokeWithGet would be "getTitle"
              val methodNameToInvokeWithGet = "get"+ mangledPropertyName(0).toUpperCase + mangledPropertyName.substring(1)

              throw newTestFailedException(Resources("propertyNotFound", methodNameToInvoke, expectedValue.toString, methodNameToInvokeWithGet))

            case Some(result) =>

              new HavePropertyMatchResult[Any](
                result == expectedValue,
                propertyName,
                expectedValue,
                result
              )
          }
        }
      }
  }

  /**
   * This implicit conversion method converts a <code>Symbol</code> to a
   * <code>HavePropertyMatcherGenerator</code>, to enable the symbol to be used with the <code>have ('author ("Dickens"))</code> syntax.
   */
  protected implicit def convertSymbolToHavePropertyMatcherGenerator(symbol: Symbol) = new HavePropertyMatcherGenerator(symbol)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class HaveWord {

    // TODO: How about returning a Matcher[Gazornimplatz] and then providing implicit conversion
    // methods from Matcher[Gazornimplatz] to Matcher[Seq], Matcher[String], Matcher[java.util.List], and
    // Matcher[the structural length methods]. This is similar to the technique I used with "contain element (7)"
    // to get it to work with java.util.Collection.
    // I couldn't figure out how to combine view bounds with existential types. May or may not
    // be possible, but going dynamic for now at least.
    def length(expectedLength: Long) =
      new Matcher[AnyRef] {
        def apply(left: AnyRef) =
          left match {
            case leftSeq: Seq[_] =>
              MatchResult(
                leftSeq.length == expectedLength, 
                FailureMessages("didNotHaveExpectedLength", left, expectedLength),
                FailureMessages("hadExpectedLength", left, expectedLength)
              )
            case leftString: String =>
              MatchResult(
                leftString.length == expectedLength, 
                FailureMessages("didNotHaveExpectedLength", left, expectedLength),
                FailureMessages("hadExpectedLength", left, expectedLength)
              )
            case leftJavaList: java.util.List[_] =>
              MatchResult(
                leftJavaList.size == expectedLength,
                FailureMessages("didNotHaveExpectedLength", left, expectedLength),
                FailureMessages("hadExpectedLength", left, expectedLength)
              )
            case _ =>

              accessProperty(left, 'length, false) match {

                case None =>

                  throw newTestFailedException(Resources("noLengthStructure", expectedLength.toString))

                case Some(result) =>

                  MatchResult(
                    result == expectedLength,
                    FailureMessages("didNotHaveExpectedLength", left, expectedLength),
                    FailureMessages("hadExpectedLength", left, expectedLength)
                  )
              }
          }
      }

    def size(expectedSize: Long) =
      new Matcher[AnyRef] {
        def apply(left: AnyRef) =
          left match {
            case leftSeq: Collection[_] =>
              MatchResult(
                leftSeq.size == expectedSize, 
                FailureMessages("didNotHaveExpectedSize", left, expectedSize),
                FailureMessages("hadExpectedSize", left, expectedSize)
              )
            case leftJavaList: java.util.List[_] =>
              MatchResult(
                leftJavaList.size == expectedSize,
                FailureMessages("didNotHaveExpectedSize", left, expectedSize),
                FailureMessages("hadExpectedSize", left, expectedSize)
              )
            case _ =>

              accessProperty(left, 'size, false) match {

                case None =>

                  throw newTestFailedException(Resources("noSizeStructure", expectedSize.toString))

                case Some(result) =>

                  MatchResult(
                    result == expectedSize,
                    FailureMessages("didNotHaveExpectedSize", left, expectedSize),
                    FailureMessages("hadExpectedSize", left, expectedSize)
                  )
              }
          }
      }

      // book should have (title ("A Tale of Two Cities"))
      //                  ^
      def apply[T](firstPropertyMatcher: HavePropertyMatcher[T, _], propertyMatchers: HavePropertyMatcher[T, _]*): Matcher[T] =

        new Matcher[T] {

          def apply(left: T) = {

            val results =
              for (propertyVerifier <- firstPropertyMatcher :: propertyMatchers.toList) yield
                propertyVerifier(left)

            val firstFailureOption = results.find(pv => !pv.matches)

            val justOneProperty = propertyMatchers.length == 0

            firstFailureOption match {

              case Some(firstFailure) =>

                val failedVerification = firstFailure
                val failureMessage =
                  FailureMessages(
                    "propertyDidNotHaveExpectedValue",
                    UnquotedString(failedVerification.propertyName),
                    failedVerification.expectedValue,
                    failedVerification.actualValue,
                    left
                  )
                val midSentenceFailureMessage =
                  FailureMessages(
                    "midSentencePropertyDidNotHaveExpectedValue",
                    UnquotedString(failedVerification.propertyName),
                    failedVerification.expectedValue,
                    failedVerification.actualValue,
                    left
                  )

                MatchResult(false, failureMessage, failureMessage, midSentenceFailureMessage, midSentenceFailureMessage)

              case None =>

                val failureMessage =
                  if (justOneProperty) {
                    val firstPropertyResult = results.head // know this will succeed, because firstPropertyMatcher was required
                    FailureMessages(
                      "propertyHadExpectedValue",
                      UnquotedString(firstPropertyResult.propertyName),
                      firstPropertyResult.expectedValue,
                      left
                    )
                  }
                  else FailureMessages("allPropertiesHadExpectedValues", left)

                val midSentenceFailureMessage =
                  if (justOneProperty) {
                    val firstPropertyResult = results.head // know this will succeed, because firstPropertyMatcher was required
                    FailureMessages(
                      "midSentencePropertyHadExpectedValue",
                      UnquotedString(firstPropertyResult.propertyName),
                      firstPropertyResult.expectedValue,
                      left
                    )
                  }
                  else FailureMessages("midSentenceAllPropertiesHadExpectedValues", left)

                MatchResult(true, failureMessage, failureMessage, midSentenceFailureMessage, midSentenceFailureMessage)
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
  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfHaveWordForJavaMap(left: java.util.Map[_, _], shouldBeTrue: Boolean) {
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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
  
  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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
  
  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfNotWordForJavaCollection[E, T <: java.util.Collection[E]](left: T, shouldBeTrue: Boolean)
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

    def contain(resultOfElementWordApplication: ResultOfElementWordApplication[E]) {
      val right = resultOfElementWordApplication.expectedElement
      if ((left.contains(right)) != shouldBeTrue) {
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfNotWordForMap[K, V](left: scala.collection.Map[K, V], shouldBeTrue: Boolean)
      extends ResultOfNotWordForCollection[(K, V), scala.collection.Map[K, V]](left, shouldBeTrue) {

    def contain(resultOfKeyWordApplication: ResultOfKeyWordApplication[K]) {
      val right = resultOfKeyWordApplication.expectedKey
      if ((left.contains(right)) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotContainKey" else "containedKey",
              left,
              right
            )
          )
      }
    }

    // Map("one" -> 1, "two" -> 2) should not contain value (3)
    //                                        ^
    def contain(resultOfValueWordApplication: ResultOfValueWordApplication[V]) {
      val right = resultOfValueWordApplication.expectedValue
      if ((left.values.exists(_ == right)) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotContainValue" else "containedValue",
              left,
              right
            )
          )
      }
    }
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfNotWordForJavaMap[K, V](left: java.util.Map[K, V], shouldBeTrue: Boolean)
      extends ResultOfNotWordForAnyRef(left, shouldBeTrue) {

    // javaMap should not contain key ("three")
    //                    ^
    def contain(resultOfKeyWordApplication: ResultOfKeyWordApplication[K]) {
      val right = resultOfKeyWordApplication.expectedKey
      if ((left.containsKey(right)) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotContainKey" else "containedKey",
              left,
              right
            )
          )
      }
    }

    // javaMap should not contain value (3)
    //                            ^
    def contain(resultOfValueWordApplication: ResultOfValueWordApplication[V]) {
      val right = resultOfValueWordApplication.expectedValue
      if ((left.containsValue(right)) != shouldBeTrue) {
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotContainValue" else "containedValue",
              left,
              right
            )
          )
      }
    }
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfNotWordForJavaList[E, T <: java.util.List[E]](left: T, shouldBeTrue: Boolean)
      extends ResultOfNotWordForJavaCollection[E, T](left, shouldBeTrue) {

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
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfBeWordForAnyRef[T <: AnyRef](left: T, shouldBeTrue: Boolean) {

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
          if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negatedFailureMessage
        )
      }
    }

    // TODO: Check the shouldBeTrues, are they sometimes always false or true?
    // badBook should be a (goodRead)
    //                   ^
    def a(bePropertyMatcher: BePropertyMatcher[T]) {
      val result = bePropertyMatcher(left)
      if (result.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue)
            FailureMessages("wasNotA", left, UnquotedString(result.propertyName))  // TODO, delete. OK
          else
            FailureMessages("wasA", left, UnquotedString(result.propertyName))
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
          if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negatedFailureMessage
        )
      }
    }

    // book should be an (excellentRead)
    //                ^
    def an(beTrueMatcher: BePropertyMatcher[T]) {
      val beTrueMatchResult = beTrueMatcher(left)
      if (beTrueMatchResult.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue)
            FailureMessages("wasNotAn", left, UnquotedString(beTrueMatchResult.propertyName))
          else
            FailureMessages("wasAn", left, UnquotedString(beTrueMatchResult.propertyName))
        )
      }
    }
  }

// TODO: make everything that I can protected

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
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

    // 2 should not be (odd)
    //              ^
    def be(beMatcher: BeMatcher[T]) {
      val result = beMatcher(left)
      if (result.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue)
            result.failureMessage
          else
            result.negatedFailureMessage
        )
      }
    }
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfNotWordForAnyRef[T <: AnyRef](left: T, shouldBeTrue: Boolean)
      extends ResultOfNotWord[T](left, shouldBeTrue) {

    // stack should not be ('empty)
    //                      ^
    def be(symbol: Symbol) {
      val matcherResult = matchSymbolToPredicateMethod(left, symbol, false, false)
      if (matcherResult.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negatedFailureMessage
        )
      }
    }

    // where empty is a BePropertyMatcher
    // stack should not be (empty)
    //                      ^
    def be(bePropertyMatcher: BePropertyMatcher[T]) {
      val result = bePropertyMatcher(left)
      if (result.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue)
            FailureMessages("wasNot", left, UnquotedString(result.propertyName))
          else
            FailureMessages("was", left, UnquotedString(result.propertyName))
        )
      }
    }

    // notFileMock should not be a ('file)
    //                        ^
    def be(resultOfAWordApplication: ResultOfAWordToSymbolApplication) {
      val matcherResult = matchSymbolToPredicateMethod(left, resultOfAWordApplication.symbol, true, true)
      if (matcherResult.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negatedFailureMessage
        )
      }
    }

    // notFileMock should not be a ('file)
    //                        ^
    def be[U >: T](resultOfAWordApplication: ResultOfAWordToBePropertyMatcherApplication[U]) {
      val result = resultOfAWordApplication.bePropertyMatcher(left)
      if (result.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue)
            FailureMessages("wasNotA", left, UnquotedString(result.propertyName))  // TODO, delete. OK
          else
            FailureMessages("wasA", left, UnquotedString(result.propertyName))
        )
      }
    }

    // notAppleMock should not be an ('apple)
    //                         ^
    def be(resultOfAnWordApplication: ResultOfAnWordToSymbolApplication) {
      val matcherResult = matchSymbolToPredicateMethod(left, resultOfAnWordApplication.symbol, true, false)
      if (matcherResult.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negatedFailureMessage
        )
      }
    }

    // notAppleMock should not be an ('apple)
    //                         ^
    def be[U >: T](resultOfAnWordApplication: ResultOfAnWordToBePropertyMatcherApplication[U]) {
      val result = resultOfAnWordApplication.bePropertyMatcher(left)
      if (result.matches != shouldBeTrue) {
        throw newTestFailedException(
          if (shouldBeTrue)
            FailureMessages("wasNotAn", left, UnquotedString(result.propertyName))
          else
            FailureMessages("wasAn", left, UnquotedString(result.propertyName))
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

    // book should not have (title ("One Hundred Years of Solitude"))
    //                 ^
    // The type parameter U has T as its lower bound, which means that U must be T or a supertype of T. Left is T, oh, because
    // HavePropertyMatcher is contravariant in its type parameter T, and that nmakes sense, because a HavePropertyMatcher of Any should
    // be able to match on a String.
    // <code>not have (a (1), b (2))</code> must mean the opposite of <code>have (a (1), b (2))</code>, which means that 
    // <code>not have (a (1), b (2))</code> will be true if either <code>(a (1)).matches</code> or <code>(b (1)).matches</code> is false.
    // Only if both <code>(a (1)).matches</code> or <code>(b (1)).matches</code> are true will <code>not have (a (1), b (2))</code> be false.
    // title/author matches | have | have not
    // 0 0 | 0 | 1 
    // 0 1 | 0 | 1
    // 1 0 | 0 | 1
    // 1 1 | 1 | 0
    // 
    def have[U >: T](firstPropertyMatcher: HavePropertyMatcher[U, _], propertyMatchers: HavePropertyMatcher[U, _]*) {

      val results =
        for (propertyVerifier <- firstPropertyMatcher :: propertyMatchers.toList) yield
          propertyVerifier(left)

      val firstFailureOption = results.find(pv => !pv.matches)

      val justOneProperty = propertyMatchers.length == 0

      // if shouldBeTrue is false, then it is like "not have ()", and should throw TFE if firstFailureOption.isDefined is false
      // if shouldBeTrue is true, then it is like "not (not have ()), which should behave like have ()", and should throw TFE if firstFailureOption.isDefined is true
      if (firstFailureOption.isDefined == shouldBeTrue) {
        firstFailureOption match {
          case Some(firstFailure) =>
            // This is one of these cases, thus will only get here if shouldBeTrue is true
            // 0 0 | 0 | 1
            // 0 1 | 0 | 1
            // 1 0 | 0 | 1
            throw newTestFailedException(
              FailureMessages(
                "propertyDidNotHaveExpectedValue",
                 UnquotedString(firstFailure.propertyName),
                 firstFailure.expectedValue,
                 firstFailure.actualValue,
                 left
              )
            )
          case None =>
            // This is this cases, thus will only get here if shouldBeTrue is false
            // 1 1 | 1 | 0
            val failureMessage =
              if (justOneProperty) {
                val firstPropertyResult = results.head // know this will succeed, because firstPropertyMatcher was required
                FailureMessages(
                  "propertyHadExpectedValue",
                  UnquotedString(firstPropertyResult.propertyName),
                  firstPropertyResult.expectedValue,
                  left
                )
              }
              else FailureMessages("allPropertiesHadExpectedValues", left)

            throw newTestFailedException(failureMessage)
        } 
      }
    }
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class RegexWord {

    def apply(regexString: String) = new ResultOfRegexWordApplication(regexString)

    // "eight" should not fullyMatch regex (decimalRegex)
    //                               ^
    def apply(regex: Regex) = new ResultOfRegexWordApplication(regex)
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfRegexWordApplication(val regex: Regex) {
    def this(regexString: String) = this(new Regex(regexString))
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class SubstringWord {

    // "eight" should not fullyMatch substring ("seven")
    //                               ^
    def apply(substring: String) = new ResultOfSubstringWordApplication(substring)
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfSubstringWordApplication(val substring: String)
 
  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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
  
  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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
  
  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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
  
  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfContainWordForJavaCollection[T](left: java.util.Collection[T], shouldBeTrue: Boolean) {
    def element(expectedElement: T) {
      if ((left.contains(expectedElement)) != shouldBeTrue)
        throw newTestFailedException(
          FailureMessages(
            if (shouldBeTrue) "didNotContainExpectedElement" else "containedExpectedElement",
            left,
            expectedElement)
        )
    }
  }
  
  // TODO Mention that Arrays get deep comparisons here and be Any and in the ShouldMatchers overview
  def equal(right: Any): Matcher[Any] =
      new Matcher[Any] {
        def apply(left: Any) =
          left match {
            case leftArray: Array[_] => 
              MatchResult(
                leftArray.deepEquals(right),
                FailureMessages("didNotEqual", left, right),
                FailureMessages("equaled", left, right)
              )
            case _ => 
              MatchResult(
                left == right,
                FailureMessages("didNotEqual", left, right),
                FailureMessages("equaled", left, right)
              )
        }
      }


  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class TreatedAsOrderedWrapper {
    def <[T <% Ordered[T]](right: T): Matcher[T] =
      new Matcher[T] {
        def apply(left: T) =
          MatchResult(
            left < right,
            FailureMessages("wasNotLessThan", left, right),
            FailureMessages("wasLessThan", left, right)
          )
      }
    def >[T <% Ordered[T]](right: T): Matcher[T] =
      new Matcher[T] {
        def apply(left: T) =
          MatchResult(
            left > right,
            FailureMessages("wasNotGreaterThan", left, right),
            FailureMessages("wasGreaterThan", left, right)
          )
      }
    def <=[T <% Ordered[T]](right: T): Matcher[T] =
      new Matcher[T] {
        def apply(left: T) =
          MatchResult(
            left <= right,
            FailureMessages("wasNotLessThanOrEqualTo", left, right),
            FailureMessages("wasLessThanOrEqualTo", left, right)
          )
      }
    def >=[T <% Ordered[T]](right: T): Matcher[T] =
      new Matcher[T] {
        def apply(left: T) =
          MatchResult(
            left >= right,
            FailureMessages("wasNotGreaterThanOrEqualTo", left, right),
            FailureMessages("wasGreaterThanOrEqualTo", left, right)
          )
      }
  }

  // This one is for one should be < (7)
  implicit def convertBeWordToForOrdered(beWord: BeWord): TreatedAsOrderedWrapper = new TreatedAsOrderedWrapper

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class BeWord {

    // fileMock should not { be a ('file) }
    //                          ^
    def a[S <: AnyRef](right: Symbol): Matcher[S] =
      new Matcher[S] {
        def apply(left: S) = matchSymbolToPredicateMethod[S](left, right, true, true)
      }

    // fileMock should not { be a (file) }
    //                          ^
    def a[S <: AnyRef](bePropertyMatcher: BePropertyMatcher[S]): Matcher[S] =
      new Matcher[S] {
        def apply(left: S) = {
          val result = bePropertyMatcher(left)
          MatchResult(
            result.matches,
            FailureMessages("wasNotA", left, UnquotedString(result.propertyName)), 
            FailureMessages("wasA", left, UnquotedString(result.propertyName))
          )
        }
      }

    // animal should not { be an ('elephant) }
    //                        ^
    def an[S <: AnyRef](right: Symbol): Matcher[S] =
      new Matcher[S] {
        def apply(left: S) = matchSymbolToPredicateMethod[S](left, right, true, false)
      }

    // animal should not { be an (elephant) }
    //                          ^
    def an[S <: AnyRef](bePropertyMatcher: BePropertyMatcher[S]): Matcher[S] =
      new Matcher[S] {
        def apply(left: S) = {
          val result = bePropertyMatcher(left)
          MatchResult(
            result.matches,
            FailureMessages("wasNotAn", left, UnquotedString(result.propertyName)),
            FailureMessages("wasAn", left, UnquotedString(result.propertyName))
          )
        }
      }

    // sevenDotOh should be (7.1 plusOrMinus 0.2)
    //                      ^
    def apply(doubleTolerance: DoubleTolerance): Matcher[Double] =
      new Matcher[Double] {
        def apply(left: Double) = {
          import doubleTolerance._
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
            left <= right + tolerance && left >= right - tolerance,
            FailureMessages("wasNotPlusOrMinus", left, right, tolerance),
            FailureMessages("wasPlusOrMinus", left, right, tolerance)
          )
        }
      }

    def theSameInstanceAs(right: AnyRef): Matcher[AnyRef] =
      new Matcher[AnyRef] {
        def apply(left: AnyRef) =
          MatchResult(
            left eq right,
            FailureMessages("wasNotSameInstanceAs", left, right),
            FailureMessages("wasSameInstanceAs", left, right)
          )
      }

// Actually, I want to keep this one, becuause it should say "was not true", not "was not equal to true"
// TODO No, I can get rid of it and just use a pattern match in apply(Any)

    def apply(right: Boolean) = 
      new Matcher[Boolean] {
        def apply(left: Boolean) =
          MatchResult(
            left == right,
            FailureMessages("wasNot", left, right),
            FailureMessages("was", left, right)
          )
      }

/* Well heck if I don't need this one
      [fsc] both method apply in class BeWord of type [T](org.scalatest.BePropertyMatcher[T])org.scalatest.Matcher[T]
      [fsc] and  method apply in class BeWord of type [T](org.scalatest.BeMatcher[T])org.scalatest.Matcher[T]
      [fsc] match argument types (Null)
      [fsc]         o should be (null)
      [fsc]                  ^
*/

    def apply(o: Null) = 
      new Matcher[AnyRef] {
        def apply(left: AnyRef) = {
          MatchResult(
            left == null,
            FailureMessages("wasNotNull", left),
            FailureMessages("wasNull", left)
          )
        }
      }

// And this I want to say was not None, but again, I could use wasNot But even so, i need to have an apply method, because otherwise it would say was not equal to None
    def apply(o: None.type) = 
      new Matcher[Option[_]] {
        def apply(left: Option[_]) = {
          MatchResult(
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

/*
    // Yes, this one I want to say was not Nil, or maybe not, because Nil is not Nil.toString
    // Dropping to just say List()
    def apply(right: Nil.type): Matcher[List[_]] =
      new Matcher[List[_]] {
        def apply(left: List[_]) = {
          MatchResult(
            left == Nil,
            FailureMessages("wasNotNil", left),
            FailureMessages("wasNil", left)
          )
        }
      }
*/

    def apply[T](right: BeMatcher[T]): Matcher[T] =
      new Matcher[T] {
        def apply(left: T) = right(left)
      }

    /**
     * This method enables the following syntax, where <code>open</code> refers to a <code>BePropertyMatcher</code>:
     *
     * <pre>
     * door should be (open)
     *                ^
     * </pre>
     */
    def apply[T](bePropertyMatcher: BePropertyMatcher[T]): Matcher[T] =
      new Matcher[T] {
        def apply(left: T) = {
          val result = bePropertyMatcher(left)
          MatchResult(
            result.matches,
            FailureMessages("wasNot", left, UnquotedString(result.propertyName)), 
            FailureMessages("was", left, UnquotedString(result.propertyName))
          )
        }
      }

// This is the guy XXX
    def apply(right: Any): Matcher[Any] =
      new Matcher[Any] {
        def apply(left: Any) =
          left match {
            case null =>
              MatchResult(
                left == null,
                FailureMessages("wasNotNull", left),
                FailureMessages("wasNull"),
                FailureMessages("wasNotNull", left),
                FailureMessages("midSentenceWasNull")
              )
            case leftArray: Array[_] => 
              MatchResult(
                leftArray.deepEquals(right),
                FailureMessages("wasNotEqualTo", left, right),
                FailureMessages("wasEqualTo", left, right)
              )
            case _ => 
              MatchResult(
                left == right,
                FailureMessages("wasNotEqualTo", left, right),
                FailureMessages("wasEqualTo", left, right)
              )
        }
      }
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class NotWord {

    def apply[S <: Any](matcher: Matcher[S]) =
      new Matcher[S] {
        def apply(left: S) =
          matcher(left) match {
            case MatchResult(bool, s1, s2, s3, s4) => MatchResult(!bool, s2, s1, s4, s3)
          }
      }

    // val even = not (odd)
    //                ^
    def apply[S <: Any](beMatcher: BeMatcher[S]) =
      new BeMatcher[S] {
        def apply(left: S) =
          beMatcher(left) match {
            case MatchResult(bool, s1, s2, s3, s4) => MatchResult(!bool, s2, s1, s4, s3)
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

    // book should (not have (title ("Moby Dick")) and (not have (author ("Melville"))))
    //                  ^
    def have[T](firstPropertyMatcher: HavePropertyMatcher[T, _], propertyMatchers: HavePropertyMatcher[T, _]*): Matcher[T] =
      apply(matchers.have(firstPropertyMatcher, propertyMatchers: _*))

    // 2 should (not be (odd) and not be (odd))
    //               ^
    def be[T](beMatcher: BeMatcher[T]): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) =
          beMatcher(left) match {
            case MatchResult(bool, s1, s2, s3, s4) => MatchResult(!bool, s2, s1, s4, s3)
          }
      }
    }

    // These next four are for things like not be </>/<=/>=:
    // left should ((not be < (right)) and (not be < (right + 1)))
    //               ^
    def be[T](resultOfLessThanComparison: ResultOfLessThanComparison[T]): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) =
          MatchResult(
            !resultOfLessThanComparison(left),
            FailureMessages("wasLessThan", left, resultOfLessThanComparison.right),
            FailureMessages("wasNotLessThan", left, resultOfLessThanComparison.right)
          )
      }
    }

    def be[T](resultOfGreaterThanComparison: ResultOfGreaterThanComparison[T]): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) =
          MatchResult(
            !resultOfGreaterThanComparison(left),
            FailureMessages("wasGreaterThan", left, resultOfGreaterThanComparison.right),
            FailureMessages("wasNotGreaterThan", left, resultOfGreaterThanComparison.right)
          )
      }
    }

    def be[T](resultOfLessThanOrEqualToComparison: ResultOfLessThanOrEqualToComparison[T]): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) =
          MatchResult(
            !resultOfLessThanOrEqualToComparison(left),
            FailureMessages("wasLessThanOrEqualTo", left, resultOfLessThanOrEqualToComparison.right),
            FailureMessages("wasNotLessThanOrEqualTo", left, resultOfLessThanOrEqualToComparison.right)
          )
      }
    }

    def be[T](resultOfGreaterThanOrEqualToComparison: ResultOfGreaterThanOrEqualToComparison[T]): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) =
          MatchResult(
            !resultOfGreaterThanOrEqualToComparison(left),
            FailureMessages("wasGreaterThanOrEqualTo", left, resultOfGreaterThanOrEqualToComparison.right),
            FailureMessages("wasNotGreaterThanOrEqualTo", left, resultOfGreaterThanOrEqualToComparison.right)
          )
      }
    }

    // Probably
    // myFile should (not be ('hidden) and not be ('hidden))
    //                    ^
    def be[T <: AnyRef](symbol: Symbol): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) = {
          val positiveMatchResult = matchSymbolToPredicateMethod(left, symbol, false, false)
          MatchResult(
            !positiveMatchResult.matches,
            positiveMatchResult.negatedFailureMessage,
            positiveMatchResult.failureMessage
          )
        }
      }
    }

    // myFile should (not be (hidden) and not be (hidden))
    //                    ^
    def be[T <: AnyRef](bePropertyMatcher: BePropertyMatcher[T]): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) = {
          val result = bePropertyMatcher(left)
          MatchResult(
            !result.matches,
            FailureMessages("was", left, UnquotedString(result.propertyName)),
            FailureMessages("wasNot", left, UnquotedString(result.propertyName))
          )
        }
      }
    }

    // isNotFileMock should (not be a ('file) and not be a ('file))
    //                           ^
    def be[T <: AnyRef](resultOfAWordApplication: ResultOfAWordToSymbolApplication): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) = {
          val positiveMatchResult = matchSymbolToPredicateMethod(left, resultOfAWordApplication.symbol, true, true)
          MatchResult(
            !positiveMatchResult.matches,
            positiveMatchResult.negatedFailureMessage,
            positiveMatchResult.failureMessage
          )
        }
      }
    }

    // myFile should (not be a (directory) and not be a (directory))
    //                    ^
    def be[T <: AnyRef](resultOfAWordApplication: ResultOfAWordToBePropertyMatcherApplication[T]): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) = {
          val result = resultOfAWordApplication.bePropertyMatcher(left)
          MatchResult(
            !result.matches,
            FailureMessages("wasA", left, UnquotedString(result.propertyName)),
            FailureMessages("wasNotA", left, UnquotedString(result.propertyName))
          )
        }
      }
    }

    // isNotAppleMock should (not be an ('apple) and not be an ('apple))
    //                            ^
    def be[T <: AnyRef](resultOfAnWordApplication: ResultOfAnWordToSymbolApplication): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) = {
          val positiveMatchResult = matchSymbolToPredicateMethod(left, resultOfAnWordApplication.symbol, true, false)
          MatchResult(
            !positiveMatchResult.matches,
            positiveMatchResult.negatedFailureMessage,
            positiveMatchResult.failureMessage
          )
        }
      }
    }

    // myFile should (not be an (directory) and not be an (directory))
    //                    ^
    def be[T <: AnyRef](resultOfAnWordApplication: ResultOfAnWordToBePropertyMatcherApplication[T]): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) = {
          val result = resultOfAnWordApplication.bePropertyMatcher(left)
          MatchResult(
            !result.matches,
            FailureMessages("wasAn", left, UnquotedString(result.propertyName)),
            FailureMessages("wasNotAn", left, UnquotedString(result.propertyName))
          )
        }
      }
    }

    // obj should (not be theSameInstanceAs (otherString) and not be theSameInstanceAs (otherString))
    //                 ^
    def be[T <: AnyRef](resultOfTheSameInstanceAsApplication: ResultOfTheSameInstanceAsApplication): Matcher[T] = {
      new Matcher[T] {
        def apply(left: T) = {
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
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
          MatchResult(
            !(left endsWith expectedSubstring),
            FailureMessages("endedWith", left, expectedSubstring),
            FailureMessages("didNotEndWith", left, expectedSubstring)
          )
        }
      }
    }

    // Array(1, 2) should (not contain element (5) and not contain element (3))
    //                         ^
    def contain[T](resultOfElementWordApplication: ResultOfElementWordApplication[T]): Matcher[Iterable[T]] = {
      val expectedElement = resultOfElementWordApplication.expectedElement
      new Matcher[Iterable[T]] {
        def apply(left: Iterable[T]) = {
          MatchResult(
            !(left.exists(_ == expectedElement)),
            FailureMessages("containedExpectedElement", left, expectedElement),
            FailureMessages("didNotContainExpectedElement", left, expectedElement)
          )
        }
      }
    }

    // Map("one" -> 1, "two" -> 2) should (not contain key ("three"))
    //                                         ^
    def contain[K](resultOfKeyWordApplication: ResultOfKeyWordApplication[K]): Matcher[scala.collection.Map[K, Any]] = {
      val expectedKey = resultOfKeyWordApplication.expectedKey
      new Matcher[scala.collection.Map[K, Any]] {
        def apply(left: scala.collection.Map[K, Any]) = {
          MatchResult(
            !(left.contains(expectedKey)),
            FailureMessages("containedKey", left, expectedKey),
            FailureMessages("didNotContainKey", left, expectedKey)
          )
        }
      }
    }

    // Map("one" -> 1, "two" -> 2) should (not contain value (3))
    //                                         ^
    def contain[K, V](resultOfValueWordApplication: ResultOfValueWordApplication[V]): Matcher[scala.collection.Map[K, V] forSome { type K }] = {
      val expectedValue = resultOfValueWordApplication.expectedValue
      new Matcher[scala.collection.Map[K, V] forSome { type K }] {
        def apply(left: scala.collection.Map[K, V] forSome { type K }) = {
          MatchResult(
            !(left.values.exists(_ == expectedValue)),
            FailureMessages("containedValue", left, expectedValue),
            FailureMessages("didNotContainValue", left, expectedValue)
          )
        }
      }
    }
  }

  val not = new NotWord
  private[scalatest] val behave = new BehaveWord
  val be = new BeWord

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  private [scalatest] class ResultOfBehaveWord[T](left: T) {
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfLengthWordApplication(val expectedLength: Long) extends HavePropertyMatcher[AnyRef, Long] {
    def apply(objectWithProperty: AnyRef): HavePropertyMatchResult[Long] = {

      accessProperty(objectWithProperty, 'length, false) match {

        case None =>

          throw newTestFailedException(Resources("propertyNotFound", "length", expectedLength.toString, "getLength"))

        case Some(result) =>

          new HavePropertyMatchResult[Long](
            result == expectedLength,
            "length",
            expectedLength,
            result match {
              case value: Byte => value.toLong
              case value: Short => value.toLong
              case value: Int => value.toLong
              case value: Long => value
              case _ => throw newTestFailedException(Resources("lengthPropertyNotAnInteger"))
            }
          )
      }
    }
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class LengthWord {
    def apply(expectedLength: Long) = new ResultOfLengthWordApplication(expectedLength)
  }

  val length = new LengthWord
 
  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfSizeWordApplication(val expectedSize: Long) extends HavePropertyMatcher[AnyRef, Long] {
    def apply(objectWithProperty: AnyRef): HavePropertyMatchResult[Long] = {

      accessProperty(objectWithProperty, 'size, false) match {

        case None =>

          throw newTestFailedException(Resources("propertyNotFound", "size", expectedSize.toString, "getSize"))

        case Some(result) =>

          new HavePropertyMatchResult[Long](
            result == expectedSize,
            "size",
            expectedSize,
            result match {
              case value: Byte => value.toLong
              case value: Short => value.toLong
              case value: Int => value.toLong
              case value: Long => value
              case _ => throw newTestFailedException(Resources("sizePropertyNotAnInteger"))
            }
          )
      }
    }
  }


  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class SizeWord {
    def apply(expectedSize: Long) = new ResultOfSizeWordApplication(expectedSize)
  }

  val size = new SizeWord

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfElementWordApplication[T](val expectedElement: T)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ElementWord {
    // array should not contain element (10)
    //                                  ^
    def apply[T](expectedElement: T) = new ResultOfElementWordApplication(expectedElement)
  }

  // array should not contain element (10)
  //                          ^
  val element = new ElementWord

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfKeyWordApplication[T](val expectedKey: T)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class KeyWord {
    // map should not contain key (10)
    //                            ^
    def apply[T](expectedKey: T) = new ResultOfKeyWordApplication(expectedKey)
  }

  // map should not contain key (10)
  //                        ^
  val key = new KeyWord

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfValueWordApplication[T](val expectedValue: T)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ValueWord {
    // map should not contain value (10)
    //                              ^
    def apply[T](expectedValue: T) = new ResultOfValueWordApplication(expectedValue)
  }

  // map should not contain value (10)
  //                        ^
  val value = new ValueWord

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfAWordToSymbolApplication(val symbol: Symbol)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfAWordToBePropertyMatcherApplication[T](val bePropertyMatcher: BePropertyMatcher[T])

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class AWord {

    // badBook should not be a ('goodRead)
    //                         ^
    def apply(symbol: Symbol) = new ResultOfAWordToSymbolApplication(symbol)

    // badBook should not be a (goodRead)
    //                         ^
    def apply[T](beTrueMatcher: BePropertyMatcher[T]) = new ResultOfAWordToBePropertyMatcherApplication(beTrueMatcher)
  }

  val a = new AWord

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfAnWordToSymbolApplication(val symbol: Symbol)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfAnWordToBePropertyMatcherApplication[T](val bePropertyMatcher: BePropertyMatcher[T])

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class AnWord {

    // badBook should not be an ('goodRead)
    //                          ^
    def apply(symbol: Symbol) = new ResultOfAnWordToSymbolApplication(symbol)

    // badBook should not be an (goodRead)
    //                          ^
    def apply[T](beTrueMatcher: BePropertyMatcher[T]) = new ResultOfAnWordToBePropertyMatcherApplication(beTrueMatcher)
  }

  val an = new AnWord

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfTheSameInstanceAsApplication(val right: AnyRef)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  case class DoubleTolerance(right: Double, tolerance: Double)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class DoublePlusOrMinusWrapper(right: Double) {
    def plusOrMinus(tolerance: Double): DoubleTolerance = {
      if (tolerance <= 0.0)
        throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      DoubleTolerance(right, tolerance)
    }
  }

  implicit def convertDoubleToPlusOrMinusWrapper(right: Double) = new DoublePlusOrMinusWrapper(right)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  case class FloatTolerance(right: Float, tolerance: Float)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class FloatPlusOrMinusWrapper(right: Float) {
    def plusOrMinus(tolerance: Float): FloatTolerance = {
      if (tolerance <= 0.0f)
        throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      FloatTolerance(right, tolerance)
    }
  }

  implicit def convertFloatToPlusOrMinusWrapper(right: Float) = new FloatPlusOrMinusWrapper(right)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  case class LongTolerance(right: Long, tolerance: Long)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class LongPlusOrMinusWrapper(right: Long) {
    def plusOrMinus(tolerance: Long): LongTolerance = {
      if (tolerance <= 0L)
        throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      LongTolerance(right, tolerance)
    }
  }

  implicit def convertLongToPlusOrMinusWrapper(right: Long) = new LongPlusOrMinusWrapper(right)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  case class IntTolerance(right: Int, tolerance: Int)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class IntPlusOrMinusWrapper(right: Int) {
    def plusOrMinus(tolerance: Int): IntTolerance = {
      if (tolerance <= 0)
        throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      IntTolerance(right, tolerance)
    }
  }

  implicit def convertIntToPlusOrMinusWrapper(right: Int) = new IntPlusOrMinusWrapper(right)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  case class ShortTolerance(right: Short, tolerance: Short)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ShortPlusOrMinusWrapper(right: Short) {
    def plusOrMinus(tolerance: Short): ShortTolerance = {
      if (tolerance <= 0)
        throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      ShortTolerance(right, tolerance)
    }
  }

  implicit def convertShortToPlusOrMinusWrapper(right: Short) = new ShortPlusOrMinusWrapper(right)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  case class ByteTolerance(right: Byte, tolerance: Byte)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class BytePlusOrMinusWrapper(right: Byte) {
    def plusOrMinus(tolerance: Byte): ByteTolerance = {
      if (tolerance <= 0)
        throw newTestFailedException(Resources("negativeOrZeroRange", tolerance.toString))
      ByteTolerance(right, tolerance)
    }
  }

  implicit def convertByteToPlusOrMinusWrapper(right: Byte) = new BytePlusOrMinusWrapper(right)

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  class ResultOfNotWordForLengthWrapper[A <: AnyRef <% LengthWrapper](left: A, shouldBeTrue: Boolean)
      extends ResultOfNotWordForAnyRef(left, shouldBeTrue) {

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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfLessThanComparison[T <% Ordered[T]](val right: T) {
    def apply(left: T): Boolean = left < right
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfGreaterThanComparison[T <% Ordered[T]](val right: T) {
    def apply(left: T): Boolean = left > right
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
  protected class ResultOfLessThanOrEqualToComparison[T <% Ordered[T]](val right: T) {
    def apply(left: T): Boolean = left <= right
  }

  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * @author Bill Venners
   */
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


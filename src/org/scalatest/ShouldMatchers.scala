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

import scala.reflect.Manifest
import Helper.newTestFailedException

/**
 * <p>
 * Trait that provides a domain specific language (DSL) for expressing assertions in tests
 * using the word <code>should</code>. (If you prefer the word <code>must</code>, you can alternatively
 * mix in trait <a href="MustMatchers.html"><code>MustMatchers</code></a>.) For example, if you mix <code>ShouldMatchers</code> into
 * a suite class, you can write an equality assertion in that suite like this:
 * </p>
 * 
 * <pre class="indent">
 * object should equal (3)
 * </pre>
 * <p>
 * Here <code>object</code> is a variable, and can be of any type. If the object is an
 * <code>Int</code> with the value 3, execution will continue (<em>i.e.</em>, the expression will result
 * in the unit value, <code>()</code>). Otherwise, a <code>TestFailedException</code>
 * will be thrown with a detail message that explains the problem, such as <code>"7 did not equal 3"</code> if the 
 * object was an <code>Int</code> with the value 7. This
 * <code>TestFailedException</code> will cause the test to fail.
 * </p>
 * 
 * <h2>Checking size and length</h2>
 * 
 * <p>
 * You can check for a size of length of just about any type of object for which it
 * would make sense. Here's how checking for length looks:
 * </p>
 * <pre class="indent">
 * object should have length (3)
 * </pre>
 * 
 * <p>
 * This syntax can be used with any object that has a field or method named <code>length</code>
 * or a method named <code>getLength</code>. (The Scala compiler will check for this at compile
 * time.) Size is similar:
 * </p>
 * 
 * <pre class="indent">
 * object should have size (10)
 * </pre>
 * 
 * <h2>Checking strings</h2>
 * 
 * <p>
 * You can check for whether a string starts with, ends with, or includes a substring
 * or regular expression, like this:
 * </p>
 * 
 * <pre class="indent">
 * string should startWith substring ("Hello")
 * string should startWith regex ("Hel*o")
 * string should endWith substring ("world")
 * string should endWith regex ("wo.ld")
 * string should include substring ("seven")
 * string should include regex ("wo.ld")
 * </pre>
 * 
 * <p>
 * You can check whether a string fully matches a regular expression, like this:
 * </p>
 * 
 * <pre class="indent">
 * string should fullyMatch regex (decimal)
 * </pre>
 * 
 * <h2>Greater and less than</h2>
 * <p>
 * You can check whether any type that either is, or can be implicitly converted to,
 * an <code>Ordered[T]</code> is greater than, less than, greater than or equal, or less
 * than or equal to a value of type <code>T</code>, like this: 
 * </p>
 * <pre class="indent">
 * one should be < (7)
 * one should be > (0)
 * one should be <= (7)
 * one should be >= (0)
 * </pre>
 * 
 * <h2>Checking <code>Boolean</code> properties with <code>be</code></h2>
 * 
 * <p>
 * If an object has a method that takes no parameters and returns boolean, you can check
 * it by placing a <code>Symbol</code> (after <code>be</code>) that specifies the name
 * of the method (excluding an optional prefix of "<code>is</code>"). A symbol literal
 * in Scala begins with a tick mark and ends at the first non-identifier character. Thus,
 * <code>'empty</code> results in a <code>Symbol</code> object at runtime, as does
 * <code>'defined</code> and <code>'file</code>. Here's an example:
 * </p>
 * 
 * <pre class="indent">
 * emptySet should be ('empty)
 * </pre>
 * 
 * Given this code, ScalaTest will use reflection to look on the object referenced from
 * <code>emptySet</code> for a method that takes no parameters and results in <code>Boolean</code>,
 * with either the name <code>empty</code> or <code>isEmpty</code>. If found, it invokes
 * that method. If the method returns <code>true</code>, execution continues. But if it returns
 * <code>false</code>, a <code>TestFailedException</code> will be thrown that will contain a detail message like:
 * 
 * <pre class="indent">
 * Set(1, 2, 3) was not empty</code>
 * </pre>
 * 
 * <p>
 * This <code>be</code> syntax can be used with any type.  If the object does
 * not have an appropriately named predicate method, you'll get a <code>TestFailedException</code>
 * at runtime with a detail message that explains the problem.
 * </p>
 * 
 * <p>
 * If you think it reads better, you can optionally put <code>a</code> or <code>an</code> after
 * <code>be</code>. For example, <code>java.util.File</code> has two predicate methods,
 * <code>isFile</code> and <code>isDirectory</code>. Thus with a <code>File</code> object
 * named <code>temp</code>, you could write:
 * </p>
 * 
 * <pre class="indent">
 * temp should be a ('file)
 * </pre>
 * 
 * <p>TODO: need an <code>an</code> example</p>
 *
 * <p>
 * If you prefer to check <code>Boolean</code> properties in a type-safe manner, you can use a <code>BePropertyMatcher</code>.
 * This would allow you to write expressions such as:
 * </p>
 *
 * <pre class="indent">
 * emptySet should be (empty)
 * </pre>
 * 
 * <p>
 * or:
 * </p>
 * 
 * <pre class="indent">
 * temp should be a (file)
 * </pre>
 * 
 * <p>
 * These expressions would fail to compile if <code>should</code> is used on an inappropriate type, as determined
 * by the type parameter of each <code>BePropertyMatcher</code>. If used with an appropriate type, they will compile
 * and at run time the <code>Boolean</code> property method or field will be accessed directly; <em>i.e.</em>, no reflection will be used.
 * See the documentation for <code>BePropertyMatcher</code> for more information.
 * </p>
 *
 * <h2>Checking object identity</h2>
 * 
 * <p>
 * If you need to check that two references refer to the exact same object, you can write:
 * </p>
 * 
 * <pre class="indent">
 * ref1 should be theSameInstanceAs (ref2)
 * </pre>
 * 
 * <h2>Checking numbers against a range</h2>
 * 
 * <p>
 * To check whether a floating point number has a value that exactly matches another, you
 * can use <code>should equal</code>:
 * </p>
 * 
 * <pre class="indent">
 * sevenDotOh should equal (7.0)
 * </pre>
 * 
 * <p>
 * Often, however, you may want to check whether a floating point number is within a
 * range. You can do that using <code>be</code> and <code>plusOrMinus</code>, like this:
 * </p>
 * 
 * <pre class="indent">
 * sevenDotOh should be (6.9 plusOrMinus 0.2)
 * </pre>
 * 
 * <p>
 * This expression will cause a <code>TestFailedException</code> to be thrown if the floating point
 * value, <code>sevenDotOh</code> is outside the range <code>6.7</code> to <code>7.1</code>.
 * You can also use <code>plusOrMinus</code> with integral types, for example:
 * </p>
 * 
 * <pre class="indent">
 * seven should be (6 plusOrMinus 2)
 * </pre>
 * 
 * <h2>Iterables, collections, sequences, and maps</h2>
 * 
 * <p>
 * You can use some of the syntax shown previously with <code>Iterable</code> and its
 * subtypes. For example, you can check whether an <code>Iterable</code> is <code>empty</code>,
 * like this:
 * </p>
 * 
 * <pre class="indent">
 * iterable should be ('empty)
 * </pre>
 * 
 * <p>
 * You can check the length of an <code>Seq</code> (<code>Array</code>, <code>List</code>, etc.),
 * like this:
 * </p>
 * 
 * <pre class="indent">
 * array should have length (3)
 * list should have length (9)
 * </pre>
 * 
 * <p>
 * You can check the size of any <code>Collection</code>, like this:
 * </p>
 * 
 * <pre class="indent">
 * map should have size (20)
 * set should have size (90)
 * </pre>
 * 
 * <p>
 * In addition, you can check whether an <code>Iterable</code> contains a particular
 * element, like this:
 * </p>
 * 
 * <pre class="indent">
 * iterable should contain element ("five")
 * </pre>
 * 
 * <p>
 * You can also check whether a <code>Map</code> contains a particular key, or value, like this:
 * </p>
 * 
 * <pre class="indent">
 * map should contain key (1)
 * map should contain value ("Howdy")
 * </pre>
 * 
 * <h2>Java collections and maps</h2>
 * 
 * <p>TODO: fill this in.
 * Mention JavaMaps don't support should contain element, as well as mentioning Java collection support.
 * </p>
 *
 * <h2>Be as an equality comparison</h2>
 * 
 * <p>
 * In a few specific cases, <code>be</code> can be used as an equality comparison, because it enables syntax
 * that sounds more natural than using <code>equals</code>. For example, instead of writing: 
 * </p>
 * 
 * <pre class="indent">
 * result should equal (null)
 * </pre>
 * 
 * <p>
 * You can write:
 * </p>
 * 
 * <pre class="indent">
 * result should be (null)
 * </pre>
 * 
 * <p>
 * (Hopefully you won't write that too much given <code>null</code> is error prone, and <code>Option</code>
 * is usually a better, well, option.) In addition to <code>null</code>, you can use <code>be</code> for equality
 * comparison of value types (<code>AnyVal</code>s), <code>Option</code>s, and <code>Nil</code>, the empty <code>List</code>.
 * Here are some other examples of <code>be</code> used for equality comparison:
 * </p>
 * 
 * <pre class="indent">
 * sum should be (7.0)
 * boring should be (false)
 * fun should be (true)
 * list should be (Nil)
 * option should be (None)
 * option should be (Some(1))
 * </pre>
 * 
 * <p>
 * For an equality assertion with any other type, you must use <code>equals</code>.
 * </p>
 *
 * <h2>Being negative</h2>
 * 
 * <p>
 * If you wish to check the opposite of some condition, you can simply insert <code>not</code> in the expression.
 * Here are a few examples:
 * </p>
 * 
 * <pre class="indent">
 * object should not be (null)
 * sum should not be <= (10)
 * mylist should not equal (yourList)
 * string should not startWith substring ("Hello")
 * </pre>
 * 
 * <h2>Logical expressions with <code>and</code> and <code>or</code></h2>
 * 
 * <p>
 * You can also combine matcher expressions with <code>and</code> and/or <code>or</code>, however,
 * you must place parentheses or curly braces around the <code>and</code>-ed or <code>or</code>-ed expression. For example, 
 * this <code>and</code>-expression would not compile, because the parentheses are missing:
 * </p>
 * 
 * <pre class="indent">
 * map should have key ("two") and not have value (7) // ERROR, parentheses missing!
 * </pre>
 * 
 * <p>
 * Instead, you'd need to write:
 * </p>
 * 
 * <pre class="indent">
 * map should (have key ("two") and not have value (7))
 * </pre>
 * 
 * <p>
 * Here are some more examples:
 * </p>
 * 
 * <pre class="indent">
 * number should (be > (0) and be <= (10))
 * option should (equal (Some(List(1, 2, 3))) or be (None))
 * string should (
 *   equal ("fee") or
 *   equal ("fie") or
 *   equal ("foe") or
 *   equal ("fum")
 * )
 * </pre>
 * 
 * <p>
 * Two differences exist between these <code>and</code> and <code>or</code> and the expressions you can write
 * on regular <code>Boolean</code>s using <code>&&</code> and <code>||</code>. First, expressions with <code>and</code>
 * and <code>or</code> do not short-circuit. The following contrived expression, for example, would print <code>"hello, world!"</code>:
 * </p>
 *
 * <pre class="indent">
 * "yellow" should (equal ("blue") and equal { println("hello, world!"); "green" })
 * </pre>
 * 
 * <p>
 * In other words, the entire <code>and</code> or <code>or</code> expression is always evaluated, so you'll see any side effects
 * of the right-hand side even if evaluating
 * only the left-hand side is enough to determine the ultimate result of the larger expression. Failure messages produced by these
 * expressions will "short-circuit," however,
 * mentioning only the left-hand side if that's enough to determine the result of the entire expression. This behavior is intended
 * to make it easier and quicker for you to ascertain which part of the expression caused the failure. The failure message for the previous
 * expression, for example, would be:
 * </p>
 * 
 * <pre class="indent">
 * "yellow" did not equal "blue"
 * </pre>
 * 
 * <p>
 * The other difference is that although <code>&&</code> has a higher precedence than <code>||</code>, <code>and</code> and <code>or</code>
 * have the same precedence. Thus although the <code>Boolean</code> expression <code>(a || b && c)</code> will evaluate the <code>&&</code> expression
 * before the <code>||</code> expression, like <code>(a || (b && c))</code>, the following expression:
 * </p>
 * 
 * <pre class="indent">
 * collection should (contain element (7) or contain contain element (8) and have size (9))
 * </pre>
 * 
 * Will evaluate left to right, as:
 * 
 * <pre class="indent">
 * collection should ((contain element (7) or contain contain element (8)) and have size (9))
 * </pre>
 * 
 * <h2>Working with <code>Option</code>s</h2>
 * 
 * <p>
 * Other than the ability to use <code>be</code> to compare options for equality,
 * ScalaTest matchers has no special support for <code>Option</code>s. Nevertheless, you can 
 * work with them quite easily using syntax shown previously. For example, if you wish to check
 * whether an option is <code>None</code>, you can write any of:
 * </p>
 * 
 * <pre class="indent">
 * option should equal (None)
 * option should be (None)
 * option should not be ('defined)
 * option should be ('empty)
 * </pre>
 * 
 * <p>
 * If you wish to check an option is defined, and holds a specific value, you can write either of:
 * </p>
 * 
 * <pre class="indent">
 * option should equal (Some("hi"))
 * option should be (Some("ho"))
 * </pre>
 * 
 * <p>
 * If you only wish to check that an option is defined, but don't care what it's value is, you can write:
 * </p>
 * 
 * <pre class="indent">
 * option should be ('defined)
 * </pre>
 * 
 * <h2>Checking arbitrary properties with <code>have</code></h2>
 * 
 * <p>
 * Using <code>have</code>, you can check properties of any type, where a <em>property</em> is an attribute of any
 * object that can be retrieved either by a public field, method, or JavaBean-style <code>get</code>
 * or <code>is</code> method, like this:
 * </p>
 * 
 * <pre class="indent">
 * book should have (
 *   'title ("Programming in Scala"),
 *   'author (List("Odersky", "Spoon", "Venners")),
 *   'pubYear (2008)
 * )
 * </pre>
 * 
 * <p>
 * This expression will use reflection to ensure the title, author, and pubYear properties of object <code>book</code>
 * are equal to the specified values. For example, it will ensure that <code>book</code> has either a public Java field or method
 * named <code>title</code>, or a public method named <code>getTitle</code>, that when invoked (or accessed in the field case) results
 * in a the string <code>"Programming in Scala"</code>. If all three properties exist and have their expected values, respectively,
 * execution will continue. If one or more of the properties either does not exist, or exists but results in an unexpected value,
 * a <code>TestFailedException</code> will be thrown that explains the problem.
 * </p>
 * 
 * <p>
 *  TODO: discuss <code>HavePropertyMatchers</code>
 * </p>
 * 
 * <h2>Those pesky parens</h2>
 * 
 * <p>
 * You may have noticed that I always put parentheses on the last token in the expressions I've
 * shown. This not always required, but the rule is a bit subtle. If the number of tokens in
 * the expression is odd, the parentheses are not needed. But if the number of tokens is even,
 * the parentheses are required. As a result, I usually include them, because then there's no
 * subtle rule to remember. In addition, although ScalaTest matchers doesn't define which
 * value is "actual" and which "expected," I usually put the expected value last and I think
 * wrapping it in parentheses emphasizes the expected value nicely. Nevertheless, you're
 * free to leave them off in many cases, and you may feel it makes the code more readable.
 * Here are some expressions that work without parentheses:
 * </p>
 * 
 * <pre class="indent">
 * object should have length 3
 * object should have size 10
 * string should startWith substring "Hello"
 * string should startWith regex "Hel*o"
 * string should endWith substring "world"
 * string should endWith regex "wo.ld"
 * string should include substring "seven"
 * string should include regex "wo.ld"
 * string should fullyMatch regex decimal
 * one should be < 7
 * one should be > 0
 * one should be <= 7
 * one should be >= 0
 * temp should be a 'file
 * object1 should be theSameInstanceAs object2
 * iterable should contain element "five"
 * map should contain key 1
 * map should contain value "Howdy"
 * </pre>
 * 
 * <p>
 * should behave like works only as is, can't be used with not, and, or or. The shared
 * thing can include it's or describes.
 * </p>
 *
 */
trait ShouldMatchers extends Matchers {

  // TODO: In the tests, make sure they can create their own matcher and use it.
  protected trait ShouldMethods[T] {
    protected val leftOperand: T
    def should(rightMatcher: Matcher[T]) {
      rightMatcher(leftOperand) match {
        case MatchResult(false, failureMessage, _, _, _) => throw newTestFailedException(failureMessage)
        case _ => ()
      }
    }
// This one supports it should behave like
    def should(behaveWord: BehaveWord) = new ResultOfBehaveWord[T](leftOperand)
    // I don't think there's a be on Any, because a (symbol) and an (symbol), plus
    // theSameInstanceAs only work on AnyRefs. And 1 should be (1) words because be (1) results in a matcher already
    // def should(beWord: BeWord): ResultOfBeWord[T] = new ResultOfBeWord(leftOperand, true)
    def should(notWord: NotWord) = new ResultOfNotWord[T](leftOperand, false)
  }

  // TODO: Shouldn't this one extend ShouldMethods? See the reminder at the end of this file.
  protected trait ShouldMethodsForAnyRef[T <: AnyRef] {

    protected val leftOperand: T

    def should(rightMatcher: Matcher[T]) { // First step, duplicate code. TODO: Eliminate the duplication
      rightMatcher(leftOperand) match {
        case MatchResult(false, failureMessage, _, _, _) => throw newTestFailedException(failureMessage)
        case _ => ()
      }
    }

    // This one supports it should behave like
    def should(behaveWord: BehaveWord) = new ResultOfBehaveWord[T](leftOperand)

    def should(notWord: NotWord): ResultOfNotWordForAnyRef[T] = {
      new ResultOfNotWordForAnyRef(leftOperand, false)
    }

    def should(beWord: BeWord): ResultOfBeWordForAnyRef[T] = new ResultOfBeWordForAnyRef[T](leftOperand, true)
  }

  protected class ShouldWrapper[T](left: T) extends { val leftOperand = left } with ShouldMethods[T]

  // I think the type hasn't been converted yet here. It is just a pass-through. It finally gets
  // converted in ResultOfHaveWordForLengthWrapper, at which point the actual implicit conversions
  // from String, Array, and the structural types get applied.
  protected class LengthShouldWrapper[A <: AnyRef <% LengthWrapper](left: A) extends { val leftOperand = left } with ShouldMethods[A] {

    def should(haveWord: HaveWord): ResultOfHaveWordForLengthWrapper[A] = {
      new ResultOfHaveWordForLengthWrapper(left, true)
    }

    override def should(notWord: NotWord): ResultOfNotWordForLengthWrapper[A] = {
      new ResultOfNotWordForLengthWrapper(leftOperand, false)
    }

    def should(beWord: BeWord): ResultOfBeWordForAnyRef[A] = new ResultOfBeWordForAnyRef[A](leftOperand, true)
  }

  // TODO, add should(BeWord) here, and investigate why there's no should(NotWord) here
  protected class SizeShouldWrapper[A <: AnyRef <% SizeWrapper](left: A) extends { val leftOperand = left } with ShouldMethods[A] {
    def should(haveWord: HaveWord): ResultOfHaveWordForSizeWrapper[A] = {
      new ResultOfHaveWordForSizeWrapper(left, true)
    }

    // TODO I just added this. Didn't do a test for it.
    def should(beWord: BeWord): ResultOfBeWordForAnyRef[A] = new ResultOfBeWordForAnyRef[A](leftOperand, true)
  }

  protected class StringShouldWrapper(left: String) extends { val leftOperand = left } with ShouldMethodsForAnyRef[String] {
    def should(haveWord: HaveWord): ResultOfHaveWordForString = {
      new ResultOfHaveWordForString(left, true)
    }
    def should(includeWord: IncludeWord): ResultOfIncludeWordForString = {
      new ResultOfIncludeWordForString(left, true)
    }
    def should(startWithWord: StartWithWord): ResultOfStartWithWordForString = {
      new ResultOfStartWithWordForString(left, true)
    }
    def should(endWithWord: EndWithWord): ResultOfEndWithWordForString = {
      new ResultOfEndWithWordForString(left, true)
    }
    def should(fullyMatchWord: FullyMatchWord): ResultOfFullyMatchWordForString = {
      new ResultOfFullyMatchWordForString(left, true)
    }
    override def should(notWord: NotWord): ResultOfNotWordForString = {
      new ResultOfNotWordForString(left, false)
    }
  }

  protected class DoubleShouldWrapper(left: Double) extends { val leftOperand = left } with ShouldMethods[Double] {
    override def should(notWord: NotWord): ResultOfNotWordForDouble = {
      new ResultOfNotWordForDouble(left, false)
    }
  }

  protected class FloatShouldWrapper(left: Float) extends { val leftOperand = left } with ShouldMethods[Float] {
    override def should(notWord: NotWord): ResultOfNotWordForFloat = {
      new ResultOfNotWordForFloat(left, false)
    }
  }

  protected class LongShouldWrapper(left: Long) extends { val leftOperand = left } with ShouldMethods[Long] {
    override def should(notWord: NotWord): ResultOfNotWordForLong = {
      new ResultOfNotWordForLong(left, false)
    }
  }

  protected class IntShouldWrapper(left: Int) extends { val leftOperand = left } with ShouldMethods[Int] {
    override def should(notWord: NotWord): ResultOfNotWordForInt = {
      new ResultOfNotWordForInt(left, false)
    }
  }

  protected class ShortShouldWrapper(left: Short) extends { val leftOperand = left } with ShouldMethods[Short] {
    override def should(notWord: NotWord): ResultOfNotWordForShort = {
      new ResultOfNotWordForShort(left, false)
    }
  }

  protected class ByteShouldWrapper(left: Byte) extends { val leftOperand = left } with ShouldMethods[Byte] {
    override def should(notWord: NotWord): ResultOfNotWordForByte = {
      new ResultOfNotWordForByte(left, false)
    }
  }

  protected class MapShouldWrapper[K, V](left: scala.collection.Map[K, V]) extends { val leftOperand = left } with ShouldMethods[scala.collection.Map[K, V]]
      with ShouldHaveWordForCollectionMethods[(K, V)] {

    def should(containWord: ContainWord): ResultOfContainWordForMap[K, V] = {
      new ResultOfContainWordForMap(left, true)
    }

    override def should(notWord: NotWord): ResultOfNotWordForMap[K, V] = {
      new ResultOfNotWordForMap(left, false)
    }
  }

  protected trait ShouldContainWordForJavaCollectionMethods[T] {
    protected val leftOperand: java.util.Collection[T]
    // javaList should contain element (2)
    //          ^
    def should(containWord: ContainWord): ResultOfContainWordForJavaCollection[T] = {
      new ResultOfContainWordForJavaCollection(leftOperand, true)
    }
  }

  protected trait ShouldContainWordForIterableMethods[T] {
    protected val leftOperand: Iterable[T]
    def should(containWord: ContainWord): ResultOfContainWordForIterable[T] = {
      new ResultOfContainWordForIterable(leftOperand, true)
    }
  }

  protected trait ShouldHaveWordForCollectionMethods[T] {
    protected val leftOperand: Collection[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForCollection[T] = {
      new ResultOfHaveWordForCollection(leftOperand, true)
    }
  }
  
  protected trait ShouldHaveWordForJavaCollectionMethods[T] {
    protected val leftOperand: java.util.Collection[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForJavaCollection[T] = {
      new ResultOfHaveWordForJavaCollection(leftOperand, true)
    }
  }

  protected trait ShouldHaveWordForSeqMethods[T] {
    protected val leftOperand: Seq[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForSeq[T] = {
      new ResultOfHaveWordForSeq(leftOperand, true)
    }
  }
  
  protected trait ShouldHaveWordForJavaListMethods[T] {
    protected val leftOperand: java.util.List[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForJavaList[T] = {
      new ResultOfHaveWordForJavaList(leftOperand, true)
    }
  }

  protected class AnyRefShouldWrapper[T <: AnyRef](left: T) extends { val leftOperand = left } with ShouldMethodsForAnyRef[T]

  protected class CollectionShouldWrapper[T](left: Collection[T]) extends { val leftOperand = left } with ShouldMethodsForAnyRef[Collection[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForCollectionMethods[T] {

    override def should(notWord: NotWord): ResultOfNotWordForCollection[T, Collection[T]] = {
      new ResultOfNotWordForCollection(leftOperand, false)
    }
  }

  // TODO: Shouldn't this mix in ShouldMethodsForAnyRef instead of ShouldMethods?
  protected class JavaCollectionShouldWrapper[T](left: java.util.Collection[T]) extends { val leftOperand = left } with ShouldMethods[java.util.Collection[T]]
      with ShouldContainWordForJavaCollectionMethods[T] with ShouldHaveWordForJavaCollectionMethods[T] {

    override def should(notWord: NotWord): ResultOfNotWordForJavaCollection[T, java.util.Collection[T]] = {
      new ResultOfNotWordForJavaCollection(leftOperand, false)
    }
  }

  protected class JavaMapShouldWrapper[K, V](left: java.util.Map[K, V]) extends { val leftOperand = left } with ShouldMethodsForAnyRef[java.util.Map[K, V]] {

    def should(containWord: ContainWord): ResultOfContainWordForJavaMap[K, V] = {
      new ResultOfContainWordForJavaMap(left, true)
    }
 
    def should(haveWord: HaveWord): ResultOfHaveWordForJavaMap = {
      new ResultOfHaveWordForJavaMap(leftOperand, true)
    }

    override def should(notWord: NotWord): ResultOfNotWordForJavaMap[K, V] = {
      new ResultOfNotWordForJavaMap[K, V](leftOperand, false)
    }
  }

  protected class SeqShouldWrapper[T](left: Seq[T]) extends { val leftOperand = left } with ShouldMethods[Seq[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]

  protected class ArrayShouldWrapper[T](left: Array[T]) extends { val leftOperand = left } with ShouldMethods[Array[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T] {

    override def should(notWord: NotWord): ResultOfNotWordForSeq[T, Array[T]] = {
      new ResultOfNotWordForSeq(leftOperand, false)
    }
  }

  protected class ListShouldWrapper[T](left: List[T]) extends { val leftOperand = left } with ShouldMethods[List[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T] {

    override def should(notWord: NotWord): ResultOfNotWordForSeq[T, List[T]] = {
      new ResultOfNotWordForSeq(leftOperand, false)
    }
  }

  protected class JavaListShouldWrapper[T](left: java.util.List[T]) extends { val leftOperand = left } with ShouldMethods[java.util.List[T]]
      with ShouldContainWordForJavaCollectionMethods[T] with ShouldHaveWordForJavaListMethods[T]  {

    override def should(notWord: NotWord): ResultOfNotWordForJavaList[T, java.util.List[T]] = {
      new ResultOfNotWordForJavaList(leftOperand, false)
    }
  }

  implicit def convertToShouldWrapper[T](o: T): ShouldWrapper[T] = new ShouldWrapper(o)
  implicit def convertToDoubleShouldWrapper(o: Double): DoubleShouldWrapper = new DoubleShouldWrapper(o)
  implicit def convertToFloatShouldWrapper(o: Float): FloatShouldWrapper = new FloatShouldWrapper(o)
  implicit def convertToLongShouldWrapper(o: Long): LongShouldWrapper = new LongShouldWrapper(o)
  implicit def convertToIntShouldWrapper(o: Int): IntShouldWrapper = new IntShouldWrapper(o)
  implicit def convertToShortShouldWrapper(o: Short): ShortShouldWrapper = new ShortShouldWrapper(o)
  implicit def convertToByteShouldWrapper(o: Byte): ByteShouldWrapper = new ByteShouldWrapper(o)
  implicit def convertToAnyRefShouldWrapper[T <: AnyRef](o: T): AnyRefShouldWrapper[T] = new AnyRefShouldWrapper[T](o)
  implicit def convertToCollectionShouldWrapper[T](o: Collection[T]): CollectionShouldWrapper[T] = new CollectionShouldWrapper[T](o)
  implicit def convertToSeqShouldWrapper[T](o: Seq[T]): SeqShouldWrapper[T] = new SeqShouldWrapper[T](o)
  implicit def convertToArrayShouldWrapper[T](o: Array[T]): ArrayShouldWrapper[T] = new ArrayShouldWrapper[T](o)
  implicit def convertToListShouldWrapper[T](o: List[T]): ListShouldWrapper[T] = new ListShouldWrapper[T](o)
  implicit def convertToMapShouldWrapper[K, V](o: scala.collection.Map[K, V]): MapShouldWrapper[K, V] = new MapShouldWrapper[K, V](o)
  implicit def convertToStringShouldWrapper(o: String): StringShouldWrapper = new StringShouldWrapper(o)

  // One problem, though, is java.List doesn't have a length field, method, or getLength method, but I'd kind
  // of like to have it work with should have length too, so I have to do one for it explicitly here.
  implicit def convertToJavaCollectionShouldWrapper[T](o: java.util.Collection[T]): JavaCollectionShouldWrapper[T] = new JavaCollectionShouldWrapper[T](o)
  implicit def convertToJavaListShouldWrapper[T](o: java.util.List[T]): JavaListShouldWrapper[T] = new JavaListShouldWrapper[T](o)

  implicit def convertToJavaMapShouldWrapper[K, V](o: java.util.Map[K, V]): JavaMapShouldWrapper[K, V] = new JavaMapShouldWrapper[K, V](o)

  // This implicit conversion is just used to trigger the addition of the should method. The LengthShouldWrapper
  // doesn't actually convert them, just passes it through. The conversion that happens here is to LengthShouldWrapper,
  // and later, inside ResultOfHaveWordForLengthWrapper, the implicit conversion from T to LengthWrapper takes place. So
  // weirdly enough, here strings are treated structurally for the implicit that adds the should, but later they are
  // treated nominally by the implicit conversion from plain old String to StringLengthWrapper. So when length is
  // ultimately invoked up in ResultOfHaveWordForLengthWrapper, it is done directly, not with reflection. That's my
  // theory anyway.
  implicit def convertHasIntGetLengthMethodToLengthShouldWrapper[T <: AnyRef { def getLength(): Int}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)
  implicit def convertHasIntGetLengthFieldToLengthShouldWrapper[T <: AnyRef { val getLength: Int}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)
  implicit def convertHasIntLengthFieldToLengthShouldWrapper[T <: AnyRef { val length: Int}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)
  implicit def convertHasIntLengthMethodToLengthShouldWrapper[T <: AnyRef { def length(): Int}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)

  implicit def convertHasLongGetLengthMethodToLengthShouldWrapper[T <: AnyRef { def getLength(): Long}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)
  implicit def convertHasLongGetLengthFieldToLengthShouldWrapper[T <: AnyRef { val getLength: Long}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)
  implicit def convertHasLongLengthFieldToLengthShouldWrapper[T <: AnyRef { val length: Long}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)
  implicit def convertHasLongLengthMethodToLengthShouldWrapper[T <: AnyRef { def length(): Long}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)

  implicit def convertHasIntGetSizeMethodToSizeShouldWrapper[T <: AnyRef { def getSize(): Int}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
  implicit def convertHasIntGetSizeFieldToSizeShouldWrapper[T <: AnyRef { val getSize: Int}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
  implicit def convertHasIntSizeFieldToSizeShouldWrapper[T <: AnyRef { val size: Int}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
  implicit def convertHasIntSizeMethodToSizeShouldWrapper[T <: AnyRef { def size(): Int}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)

  implicit def convertHasLongGetSizeMethodToSizeShouldWrapper[T <: AnyRef { def getSize(): Long}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
  implicit def convertHasLongGetSizeFieldToSizeShouldWrapper[T <: AnyRef { val getSize: Long}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
  implicit def convertHasLongSizeFieldToSizeShouldWrapper[T <: AnyRef { val size: Long}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
  implicit def convertHasLongSizeMethodToSizeShouldWrapper[T <: AnyRef { def size(): Long}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
}
/*
When Scala must chose between an implicit with a structural type and one with a nominal one,
the nominal one wins.

scala> set.size
res0: Int = 3

scala> class SetWrapper(payload: Set[Int]) { def prove() { println("SetWrapper") }}
defined class SetWrapper

scala> class SizeWrapper(payload: { def size: Int }) { def prove() { println("SizeWrapper") }}
defined class SizeWrapper

scala> new SizeWrapper(set)
res1: SizeWrapper = SizeWrapper@39ce9b

scala> res1.prove
SizeWrapper

scala> new SetWrapper(set)
res3: SetWrapper = SetWrapper@9fc9fe

scala> res3.prove
SetWrapper

scala> implicit def convertToSetWrapper(setParam: Set[Int]): SetWrapper = new SetWrapper(setParam)
convertToSetWrapper: (Set[Int])SetWrapper

scala> implicit def convertToSizeWrapper(setParam: { def size: Int }): SizeWrapper = new SizeWrapper(setParam)
convertToSizeWrapper: (AnyRef{def size: Int})SizeWrapper

scala> convertToSetWrapper(set)
res5: SetWrapper = SetWrapper@598095

scala> convertToSizeWrapper(set)
res6: SizeWrapper = SizeWrapper@660ff1

scala> set.prove
SetWrapper
 */
/*
leave this explanation in. It is a useful reminder.
THIS DOESN'T WORK BECAUSE...
  protected trait ShouldMethods[T] {
    protected val leftOperand: T
    def should(rightMatcher: Matcher[T]) {
      rightMatcher(leftOperand) match {
        case MatchResult(false, failureMessage, _) => throw newTestFailedException(failureMessage)
        case _ => ()
      }
    }

    // This one supports it should behave like
    def should(behaveWord: BehaveWord) = new ResultOfBehaveWord[T](leftOperand)
    // I don't think there's a be on Any, because a (symbol) and an (symbol), pluse
    // theSameInstanceAs only work on AnyRefs
    // def should(beWord: BeWord): ResultOfBeWord[T] = new ResultOfBeWord(leftOperand, true)
    def should(notWord: NotWord) = new ResultOfNotWord[T](leftOperand, false)
  }
  protected trait ShouldMethodsForAnyRef[T <: AnyRef] extends ShouldMethods[T] {
    val leftOperand: T
    override def should(notWord: NotWord): ResultOfNotWordForAnyRef[T] = {
      new ResultOfNotWordForAnyRef(leftOperand, false)
    }
    def should(beWord: BeWord): ResultOfBeWordForAnyRef[T] = new ResultOfBeWordForAnyRef[T](leftOperand, true)
  }

  protected class CollectionShouldWrapper[T](left: Collection[T]) extends { val leftOperand = left }
  with ShouldMethodsForAnyRef[Collection[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForCollectionMethods[T] {

    override def should(notWord: NotWord): ResultOfNotWordForCollection[Collection[T]] = {
      new ResultOfNotWordForCollection(leftOperand, false)
    }
  }
When you mix in the latter, the result type of should(BeWord) is still the more generic ResultOfNotWord, not ResultOfNotWordForAnyRef.
As a result it doesn't have an "a (Symbol)" method on it. This triggers another implicit conversion in this case:

emptySet should be a ('empty)

Turns into:

BeSymbolSpec.this.convertToAnyRefShouldWrapper[BeSymbolSpec.this.CollectionShouldWrapper[T]]
(BeSymbolSpec.this.convertToCollectionShouldWrapper[T](emptySet)).should(BeSymbolSpec.this.be).
a(scala.Symbol.apply("empty"));

So the problem with having these "methods" traits extend each other is the covariant result
types don't get more specific visibly enough.

LATER: Well, I'm wondering if now that I've removed the be method in ShouldMethods if this will work. 
*/
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

import scala.reflect.Manifest
import Helper.newTestFailedException

/* PUT IN a ShouldBehaveLike trait
 * <h2>Shared behaviors</h2>
 *
 * <p>
 * <code>ShouldMatchers</code> provides syntax that allows you to import <em>shared behavior</em> into a <code>FunSuite</code> or <code>Spec</code>,
 * where shared
 * behavior consists of tests that you want to run multiple times with different instances of certain fixture objects each time. For example, you may have
 * a set of tests that verify that given <code>Stack</code> object behaves like a non-empty stack. Rather than duplicating the code of
 * these tests for different instances of <code>Stack</code>, you can factor out the commonality into a method or function
 * and import those tests when needed. If you define a shared behavior method named <code>nonEmptyStack</code>, and want to run those
 * tests on a <code>Stack</code> that has one item, the object referred to <code>lastValuePushed</code>, you could write:
 * </p>
 *
 * <pre>
 * stackWithOneItem should behave like (nonEmptyStack(lastValuePushed))
 * </pre>
 *
 * <p>
 * This <code>behave like</code> works only as shown here. It can't be used with <code>not</code> or combined with <code>and</code>
 * or <code>or</code>. For more information on shared behavior, see the documentation for traits <a href="FunSuite.html"><code>FunSuite</code></a> and
 * <a href="Spec.html"><code>Spec</code></a>.
 * </p>
 *
*/

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
 * will be thrown with a detail message that explains the problem, such as <code>"7 did not equal 3"</code>.
 * This <code>TestFailedException</code> will cause the test to fail.
 * </p>
 * 
 * <p>
 * The <code>left should equal (right)</code> syntax works by calling == (<em>i.e.</em>, <code>equals</code>) on the <code>left</code>
 * value, passing in the <code>right</code> value, on every type except arrays. If <code>left</code> is an array, <code>deepEquals</code>
 * will be invoked on <code>left</code>, passing in <code>right</code>. Thus, even though this expression
 * will yield false, because <code>Array</code>'s <code>equals</code> method compares object identity:
 * </p>
 * 
 * <pre class="indent">
 * Array(1, 2) == Array(1, 2) // yields false
 * </pre>
 *
 * <p>
 * The following expression will <em>not</em> result in a <code>TestFailedException</code>, because <code>deepEquals</code> compares
 * the two arrays structurally, taking into consideration the equality of the array's contents:
 * </p>
 *
 * <pre class="indent">
 * Array(1, 2) should equal (Array(1, 2)) // succeeds (i.e., does not throw TestFailedException)
 * </pre>
 *
 * <p>
 * If you ever do want to verify that two arrays are actually the same object (have the same identity), you can use the
 * <code>be theSameInstanceAs</code> syntax, described below.
 * </p>
 *
 * <h2>Checking size and length</h2>
 * 
 * <p>
 * You can check the size or length of just about any type of object for which it
 * would make sense. Here's how checking for length looks:
 * </p>
 * <pre class="indent">
 * object should have length (3)
 * </pre>
 * 
 * <p>
 *Size is similar:
 * </p>
 * 
 * <pre class="indent">
 * object should have size (10)
 * </pre>
 * 
 * <p>
 * The <code>length</code> syntax can be used with any object that has a field or method named <code>length</code>
 * or a method named <code>getLength</code>.   Similarly, the <code>size</code> syntax can be used with any
 * object that has a field or method named <code>size</code> or a method named <code>getSize</code>.
 * The type of a <code>length</code> or <code>size</code> field, or return type of a method, must be either <code>Int</code>
 * or <code>Long</code>. Any such method must take no parameters. (The Scala compiler will ensure at compile time that
 * the object on which <code>should</code> is being invoked has the appropriate structure.)
 * </p>
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
 * You can check whether any type that is, or can be implicitly converted to,
 * an <code>Ordered[T]</code> is greater than, less than, greater than or equal, or less
 * than or equal to a value of type <code>T</code>. The syntax is:
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
 * with either the name <code>empty</code> or <code>isEmpty</code>. If found, it will invoke
 * that method. If the method returns <code>true</code>, execution will continue. But if it returns
 * <code>false</code>, a <code>TestFailedException</code> will be thrown that will contain a detail message, such as:
 * 
 * <pre class="indent">
 * Set(1, 2, 3) was not empty
 * </pre>
 * 
 * <p>
 * This <code>be</code> syntax can be used with any type.  If the object does
 * not have an appropriately named predicate method, you'll get a <code>TestFailedException</code>
 * at runtime with a detail message that explains the problem.
 * (For the details on how a field or method is selected during this
 * process, see the documentation for <a href="Matchers.BeWord.html"><code>BeWord</code></a>.)
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
 * <p>
 * Or, given <code>java.awt.event.ActionKey</code> has a method <code>isActionKey</code> that takes
 * no arguments and returns <code>Boolean</code>, you could assert that a <code>KeyEvent</code> is
 * an action key with:
 *</p>
 *
 * <pre class="indent">
 * keyEvent should be an ('actionKey)
 * </pre>
 * 
 * <p>
 * If you prefer to check <code>Boolean</code> properties in a type-safe manner, you can use a <code>BePropertyMatcher</code>.
 * This would allow you to write expressions such as:
 * </p>
 *
 * <pre class="indent">
 * emptySet should be (empty)
 * temp should be a (file)
 * keyEvent should be an (actionKey)
 * </pre>
 * 
 * <p>
 * These expressions would fail to compile if <code>should</code> is used on an inappropriate type, as determined
 * by the type parameter of the <code>BePropertyMatcher</code> being used. (For example, <code>file</code> in this example
 * would likely be of type <code>BePropertyMatcher[java.util.File]</code>. If used with an appropriate type, such an expression will compile
 * and at run time the <code>Boolean</code> property method or field will be accessed directly; <em>i.e.</em>, no reflection will be used.
 * See the documentation for <a href="BePropertyMatcher.html"><code>BePropertyMatcher</code></a> for more information.
 * </p>
 *
 * <h2>Using custom <code>BeMatchers</code></h2>
 *
 * If you want to create a new way of using <code>be</code>, which doesn't map to an actual property on the
 * type you care about, you can create a <code>BeMatcher</code>. You could use this, for example, to create <code>BeMatcher[Int]</code>
 * called <code>odd</code>, which would match any odd <code>Int</code>, and <code>even</code>, which would match
 * any even <code>Int</code>. 
 * Given this pair of <code>BeMatcher</code>s, you could check whether an <code>Int</code> was odd or even with expressions like:
 * </p>
 *
 * <pre class="indent">
 * num should be (odd)
 * num should not be (even)
 * </pre>
 *
 * For more information, see the documentation for <a href="BeMatcher.html"><code>BeMatcher</code></a>.
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
 * <p>
 * You can use similar syntax on Java collections (<code>java.util.Collection</code>) and maps (<code>java.util.Map</code>).
 * For example, you can check whether a Java <code>Collection</code> or <code>Map</code> is <code>empty</code>,
 * like this:
 * </p>
 * 
 * <pre class="indent">
 * javaCollection should be ('empty)
 * javaMap should be ('empty)
 * </pre>
 * 
 * <p>
 * Even though Java's <code>List</code> type doesn't actually have a <code>length</code> or <code>getLength</code> method,
 * you can nevertheless check the length of a Java <code>List</code> (<code>java.util.List</code>) like this:
 * </p>
 * 
 * <pre class="indent">
 * javaList should have length (9)
 * </pre>
 * 
 * <p>
 * You can check the size of any Java <code>Collection</code> or <code>Map</code>, like this:
 * </p>
 * 
 * <pre class="indent">
 * javaMap should have size (20)
 * javaSet should have size (90)
 * </pre>
 * 
 * <p>
 * In addition, you can check whether a Java <code>Collection</code> contains a particular
 * element, like this:
 * </p>
 * 
 * <pre class="indent">
 * javaCollection should contain element ("five")
 * </pre>
 * 
 * <p>
 * One difference to note between the syntax supported on Java collections and that of Scala
 * iterables is that you can't use <code>contain element (...)</code> syntax with a Java <code>Map</code>.
 * Java differs from Scala in that its <code>Map</code> is not a subtype of its <code>Collection</code> type.
 * If you want to check that a Java <code>Map</code> contains a specific key/value pair, the best approach is
 * to invoke <code>entrySet</code> on the Java <code>Map</code> and check that entry set for the appropriate
 * element (a <code>java.util.Map.Entry</code>) using <code>contain element (...)</code>.
 * </p>
 *
 * <p>
 * Despite this difference, the other (more commonly used) map matcher syntax works just fine on Java <code>Map</code>s.
 * You can, for example, check whether a Java <code>Map</code> contains a particular key, or value, like this:
 * </p>
 * 
 * <pre class="indent">
 * javaMap should contain key (1)
 * javaMap should contain value ("Howdy")
 * </pre>
 * 
 * <h2>Be as an equality comparison</h2>
 * 
 * <p>
 * All uses of <code>be</code> other than those shown previously perform an equality comparison. In other words, they work
 * the same as <code>equals</code>. This redundance between <code>be</code> and <code>equals</code> exists because it enables syntax
 * that sometimes sounds more natural. For example, instead of writing: 
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
 * is usually a better, well, option.) 
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
 * As with <code>equal</code>, using <code>be</code> on arrays results in <code>deepEquals</code> being called, not <code>equals</code>. As a result,
 * the following expression would <em>not</em> throw a <code>TestFailedException</code>:
 * </p>
 *
 * <pre class="indent">
 * Array(1, 2) should be (Array(1, 2)) // succeeds (i.e., does not throw TestFailedException)
 * </pre>
 *
 * <p>
 * Because <code>be</code> is used in several ways in ScalaTest matcher syntax, just as it is used in many ways in English, one
 * potential point of confusion in the event of a failure is determining whether <code>be</code> was being used as an equality comparison or
 * in some other way, such as a property assertion. To make it more obvious when <code>be</code> is being used for equality, the failure
 * messages generated for those equality checks will include the word <code>equal</code> in them. For example, if this expression fails with a
 * <code>TestFailedException</code>:
 * </p>
 *
 * <pre class="indent">
 * option should be (Some(1))
 * </pre>
 *
 * <p>
 * The detail message in that <code>TestFailedException</code> will include the words <code>"equal to"</code> to signify <code>be</code>
 * was in this case being used for equality comparison:
 * </p>
 *
 * <pre class="indent">
 * Some(2) was not equal to Some(1)
 * </pre>
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
 * you must place parentheses or curly braces around the <code>and</code> or <code>or</code> expression. For example, 
 * this <code>and</code>-expression would not compile, because the parentheses are missing:
 * </p>
 * 
 * <pre class="indent">
 * map should have key ("two") and not have value (7) // ERROR, parentheses missing!
 * </pre>
 * 
 * <p>
 * Instead, you need to write:
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
 * Two differences exist between expressions composed of these <code>and</code> and <code>or</code> operators and the expressions you can write
 * on regular <code>Boolean</code>s using its <code>&&</code> and <code>||</code> operators. First, expressions with <code>and</code>
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
 * mentioning only the left-hand side if that's enough to determine the result of the entire expression. This "short-circuiting" behavior
 * of failure messages is intended
 * to make it easier and quicker for you to ascertain which part of the expression caused the failure. The failure message for the previous
 * expression, for example, would be:
 * </p>
 * 
 * <pre class="indent">
 * "yellow" did not equal "blue"
 * </pre>
 * 
 * <p>
 * Most likely this lack of short-circuiting would rarely be noticeable, because evaluating the right hand side will usually not
 * involve a side effect. One situation where it might show up, however, is if you attempt to <code>and</code> a <code>null</code> check on a variable with an expression
 * that uses the variable, like this:
 * </p>
 *
 * <pre class="indent">
 * map should (not be (null) and contain key ("ouch"))
 * </pre>
 * 
 * <p>
 * If <code>map</code> is <code>null</code>, the test will indeed fail, but with a <code>NullPointerException</code>, not a
 * <code>TestFailedException</code>. Here, the <code>NullPointerException</code> is the visible right-hand side effect. To get a
 * <code>TestFailedException</code>, you would need to check each assertion separately:
 * </p>
 *
 * <pre class="indent">
 * map should not be (null)
 * map should contain key ("ouch")
 * </pre>
 * 
 * <p>
 * If <code>map</code> is <code>null</code> in this case, the <code>null</code> check in the first expression will fail with
 * a <code>TestFailedException</code>, and the second expression will never be executed.
 * </p>
 *
 * <p>
 * The other difference with <code>Boolean</code> operators is that although <code>&&</code> has a higher precedence than <code>||</code>,
 * <code>and</code> and <code>or</code>
 * have the same precedence. Thus although the <code>Boolean</code> expression <code>(a || b && c)</code> will evaluate the <code>&&</code> expression
 * before the <code>||</code> expression, like <code>(a || (b && c))</code>, the following expression:
 * </p>
 * 
 * <pre class="indent">
 * collection should (contain element (7) or contain contain element (8) and have size (9))
 * </pre>
 * 
 * <p>
 * Will evaluate left to right, as:
 * </p>
 * 
 * <pre class="indent">
 * collection should ((contain element (7) or contain contain element (8)) and have size (9))
 * </pre>
 * 
 * <p>
 * If you really want the <code>and</code> part to be evaluated first, you'll need to put in parentheses, like this:
 * </p>
 * 
 * <pre class="indent">
 * collection should (contain element (7) or (contain contain element (8) and have size (9)))
 * </pre>
 * 
 * <h2>Working with <code>Option</code>s</h2>
 * 
 * <p>
 * ScalaTest matchers has no special support for <code>Option</code>s, but you can 
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
 * option should be (Some("hi"))
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
 * This expression will use reflection to ensure the <code>title</code>, <code>author</code>, and <code>pubYear</code> properties of object <code>book</code>
 * are equal to the specified values. For example, it will ensure that <code>book</code> has either a public Java field or method
 * named <code>title</code>, or a public method named <code>getTitle</code>, that when invoked (or accessed in the field case) results
 * in a the string <code>"Programming in Scala"</code>. If all specified properties exist and have their expected values, respectively,
 * execution will continue. If one or more of the properties either does not exist, or exists but results in an unexpected value,
 * a <code>TestFailedException</code> will be thrown that explains the problem. (For the details on how a field or method is selected during this
 * process, see the documentation for <a href="Matchers.HavePropertyMatcherGenerator.html"><code>HavePropertyMatcherGenerator</code></a>.)
 * </p>
 * 
 * <p>
 * When you use this syntax, you must place one or more property values in parentheses after <code>have</code>, seperated by commas, where a <em>property
 * value</em> is a symbol indicating the name of the property followed by the expected value in parentheses. The only exceptions to this rule is the syntax
 * for checking size and length shown previously, which does not require parentheses. If you forget and put parentheses in, however, everything will
 * still work as you'd expect. Thus instead of writing:
 * </p>
 *
 * <pre class="indent">
 * array should have length (3)
 * set should have size (90)
 * </pre>
 * 
 * <p>
 * You can alternatively, write:
 * </p>
 *
 * <pre class="indent">
 * array should have (length (3))
 * set should have (size (90))
 * </pre>
 * 
 * <p>
 * If a property has a value different from the specified expected value, a <code>TestFailedError</code> will be thrown
 * with a detail message that explains the problem. For example, if you assert the following on
 * a <code>book</code> whose title is <code>Moby Dick</code>:
 * </p>
 *
 * <pre class="indent">
 * book should have ('title ("A Tale of Two Cities"))
 * </pre>
 *
 * <p>
 * You'll get a <code>TestFailedException</code> with this detail message:
 * </p>
 *
 * <pre class="indent">
 * The title property had value "Moby Dick", instead of its expected value "A Tale of Two Cities",
 * on object Book("Moby Dick", "Melville", 1851)
 * </pre>
 * 
 * <p>
 * If you prefer to check properties in a type-safe manner, you can use a <code>HavePropertyMatcher</code>.
 * This would allow you to write expressions such as:
 * </p>
 *
 * <pre class="indent">
 * book should have (
 *   title ("Programming in Scala"),
 *   author (List("Odersky", "Spoon", "Venners")),
 *   pubYear (2008)
 * )
 * </pre>
 * 
 * <p>
 * These expressions would fail to compile if <code>should</code> is used on an inappropriate type, as determined
 * by the type parameter of the <code>HavePropertyMatcher</code> being used. (For example, <code>title</code> in this example
 * might be of type <code>HavePropertyMatcher[org.publiclibrary.Book]</code>. If used with an appropriate type, such an expression will compile
 * and at run time the property method or field will be accessed directly; <em>i.e.</em>, no reflection will be used.
 * See the documentation for <a href="HavePropertyMatcher.html"><code>HavePropertyMatcher</code></a> for more information.
 * </p>
 *
 * <h2>Using custom matchers</h2>
 * 
 * <p>
 * If none of the built-in matcher syntax (or options shown so far for extending the syntax) satisfy a particular need you have, you can create
 * custom <code>Matcher</code>s that allow
 * you to place your own syntax directly after <code>should</code>. For example, class <code>java.util.File</code> has a method <code>exists</code>, which
 * indicates whether a file of a certain path and name exists. Because the <code>exists</code> method takes no parameters and returns <code>Boolean</code>,
 * you can call it using <code>be</code> with a symbol or <code>BePropertyMatcher</code>, yielding assertions like:
 * </p>
 * 
 * <pre class="indent">
 * file should be ('exists)  // using a symbol
 * file should be (inExistance)   // using a BePropertyMatcher
 * </pre>
 * 
 * <p>
 * Although these expressions will achieve your goal of throwing a <code>TestFailedException</code> if the file does not exist, they don't produce
 * the most readable code because the English is either incorrect or awkward. In this case, you might want to create a
 * custom <code>Matcher[java.util.File]</code>
 * named <code>exist</code>, which you could then use to write expressions like:
 * </p>
 *
 * <pre class="indent">
 * // using a plain-old Matcher
 * file should exist
 * file should not (exist)
 * file should (exist and have ('name ("temp.txt")))
 * </pre>
 * 
 * <p>
 * Note that when you use custom <code>Matcher</code>s, you will need to put parentheses around the custom matcher in more cases than with
 * the built-in syntax. For example you will often need the parentheses after <code>not</code>, as shown above. (There's no penalty for
 * always surrounding custom matchers with parentheses, and if you ever leave them off when they are needed, you'll get a compiler error.)
 * For more information about how to create custom <code>Matcher</code>s, please see the documentation for the <a href="Matcher.html"><code>Matcher</code></a> trait.
 * </p>
 *
 * <h2>Those pesky parens</h2>
 * 
 * <p>
 * Perhaps the most tricky part of writing assertions using ScalaTest matchers is remembering
 * when you need or don't need parentheses, but bearing in mind a few simple rules <!-- PRESERVE -->should help.
 * It is also reassuring to know that if you ever leave off a set of parentheses when they are
 * required, your code will not compile. Thus the compiler will help you remember when you need the parens.
 * That said, the rules are:
 * </p>
 *
 * <p>
 * 1. Although you don't always need them, it is recommended style to always put parentheses
 * around right-hand values, such as the <code>7</code> in <code>num should equal (7)</code>:
 * </p>
 *
 * <pre class="indent">
 * result should equal <span style="color: #CC3300; font-weight: bold">(</span>4<span style="color: #CC3300; font-weight: bold">)</span>
 * array should have length <span style="color: #CC3300; font-weight: bold">(</span>3<span style="color: #CC3300; font-weight: bold">)</span>
 * book should have (
 *   'title <span style="color: #CC3300; font-weight: bold">(</span>"Programming in Scala"<span style="color: #CC3300; font-weight: bold">)</span>,
 *   'author <span style="color: #CC3300; font-weight: bold">(</span>List("Odersky", "Spoon", "Venners")<span style="color: #CC3300; font-weight: bold">)</span>,
 *   'pubYear <span style="color: #CC3300; font-weight: bold">(</span>2008<span style="color: #CC3300; font-weight: bold">)</span>
 * )
 * option should be <span style="color: #CC3300; font-weight: bold">(</span>'defined<span style="color: #CC3300; font-weight: bold">)</span>
 * catMap should (contain key <span style="color: #CC3300; font-weight: bold">(</span>9<span style="color: #CC3300; font-weight: bold">)</span> and contain value <span style="color: #CC3300; font-weight: bold">(</span>"lives"<span style="color: #CC3300; font-weight: bold">)</span>)</span>
 * keyEvent should be an <span style="color: #CC3300; font-weight: bold">(</span>'actionKey<span style="color: #CC3300; font-weight: bold">)</span>
 * javaSet should have size <span style="color: #CC3300; font-weight: bold">(</span>90<span style="color: #CC3300; font-weight: bold">)</span>
 * </pre>
 *
 * <p>
 * 2. Except for <code>length</code> and <code>size</code>, you must always put parentheses around
 * the list of one or more property values following a <code>have</code>:
 * </p>
 *
 * <pre class="indent">
 * file should (exist and have <span style="color: #CC3300; font-weight: bold">(</span>'name ("temp.txt")<span style="color: #CC3300; font-weight: bold">)</span>)
 * book should have <span style="color: #CC3300; font-weight: bold">(</span>
 *   title ("Programming in Scala"),
 *   author (List("Odersky", "Spoon", "Venners")),
 *   pubYear (2008)
 * <span style="color: #CC3300; font-weight: bold">)</span>
 * javaList should have length (9) // parens optional for length and size
 * </pre>
 *
 * <p>
 * 3. You must always put parentheses around <code>and</code> and <code>or</code> expressions, as in:
 * </p>
 *
 * <pre class="indent">
 * catMap should <span style="color: #CC3300; font-weight: bold">(</span>contain key (9) and contain value ("lives")<span style="color: #CC3300; font-weight: bold">)</span>
 * number should <span style="color: #CC3300; font-weight: bold">(</span>equal (2) or equal (4) or equal (8)<span style="color: #CC3300; font-weight: bold">)</span>
 * </pre>
 * 
 * <p>
 * 4. Although you don't always need them, it is recommended style to always put parentheses
 * around custom <code>Matcher</code>s when they appear directly after <code>not</code>:
 * </p>
 * 
 * <pre class="indent">
 * file should exist
 * file should not <span style="color: #CC3300; font-weight: bold">(</span>exist<span style="color: #CC3300; font-weight: bold">)</span>
 * file should (exist and have ('name ("temp.txt")))
 * file should (not <span style="color: #CC3300; font-weight: bold">(</span>exist<span style="color: #CC3300; font-weight: bold">)</span> and have ('name ("temp.txt"))
 * file should (have ('name ("temp.txt") or exist)
 * file should (have ('name ("temp.txt") or not <span style="color: #CC3300; font-weight: bold">(</span>exist<span style="color: #CC3300; font-weight: bold">)</span>)
 * </pre>
 *
 * <p>
 * That's it. With a bit of practice it <!-- PRESERVE -->should become natural to you, and the compiler will always be there to tell you if you
 * forget a set of needed parentheses.
 * </p>
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

  /**
   * Implicitly converts an object of type <code>T</code> to a <code>ShouldWrapper</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToShouldWrapper[T](o: T): ShouldWrapper[T] = new ShouldWrapper(o)

  /**
   * Implicitly converts an object of type <code>T</code> to a <code>DoubleShouldWrapper</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToDoubleShouldWrapper(o: Double): DoubleShouldWrapper = new DoubleShouldWrapper(o)

  /**
   * Implicitly converts an object of type <code>scala.Float</code> to a <code>FloatShouldWrapper</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToFloatShouldWrapper(o: Float): FloatShouldWrapper = new FloatShouldWrapper(o)

  /**
   * Implicitly converts an object of type <code>scala.Long</code> to a <code>LongShouldWrapper</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToLongShouldWrapper(o: Long): LongShouldWrapper = new LongShouldWrapper(o)

  /**
   * Implicitly converts an object of type <code>scala.Int</code> to a <code>IntShouldWrapper</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToIntShouldWrapper(o: Int): IntShouldWrapper = new IntShouldWrapper(o)

  /**
   * Implicitly converts an object of type <code>scala.Short</code> to a <code>ShortShouldWrapper</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToShortShouldWrapper(o: Short): ShortShouldWrapper = new ShortShouldWrapper(o)

  /**
   * Implicitly converts an object of type <code>scala.Byte</code> to a <code>ByteShouldWrapper</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToByteShouldWrapper(o: Byte): ByteShouldWrapper = new ByteShouldWrapper(o)

  /**
   * Implicitly converts a <code>scala.AnyRef</code> of type <code>T</code> to an <code>AnyRefShouldWrapper[T]</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToAnyRefShouldWrapper[T <: AnyRef](o: T): AnyRefShouldWrapper[T] = new AnyRefShouldWrapper[T](o)

  /**
   * Implicitly converts an object of type <code>scala.Collection[T]</code> to a <code>CollectionShouldWrapper</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToCollectionShouldWrapper[T](o: Collection[T]): CollectionShouldWrapper[T] = new CollectionShouldWrapper[T](o)

  /**
   * Implicitly converts an object of type <code>scala.Seq[T]</code> to a <code>SeqShouldWrapper[T]</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToSeqShouldWrapper[T](o: Seq[T]): SeqShouldWrapper[T] = new SeqShouldWrapper[T](o)

  /**
   * Implicitly converts an object of type <code>scala.Array[T]</code> to a <code>ArrayShouldWrapper[T]</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToArrayShouldWrapper[T](o: Array[T]): ArrayShouldWrapper[T] = new ArrayShouldWrapper[T](o)

  /**
   * Implicitly converts an object of type <code>scala.List[T]</code> to a <code>ListShouldWrapper[T]</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToListShouldWrapper[T](o: List[T]): ListShouldWrapper[T] = new ListShouldWrapper[T](o)

  /**
   * Implicitly converts an object of type <code>scala.collection.Map[K, V]</code> to a <code>MapShouldWrapper[K, V]</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToMapShouldWrapper[K, V](o: scala.collection.Map[K, V]): MapShouldWrapper[K, V] = new MapShouldWrapper[K, V](o)

  /**
   * Implicitly converts an object of type <code>java.lang.String</code> to a <code>StringShouldWrapper</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToStringShouldWrapper(o: String): StringShouldWrapper = new StringShouldWrapper(o)

  /**
   * Implicitly converts an object of type <code>java.util.Collection[T]</code> to a <code>JavaCollectionShouldWrapper[T]</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToJavaCollectionShouldWrapper[T](o: java.util.Collection[T]): JavaCollectionShouldWrapper[T] = new JavaCollectionShouldWrapper[T](o)

  /**
   * Implicitly converts an object of type <code>java.util.List[T]</code> to a <code>JavaListShouldWrapper[T]</code>,
   * to enable <code>should</code> methods to be invokable on that object. This conversion is necessary to enable
   * <code>length</code> to be used on Java <code>List</code>s.
   */
  implicit def convertToJavaListShouldWrapper[T](o: java.util.List[T]): JavaListShouldWrapper[T] = new JavaListShouldWrapper[T](o)


  /**
   * Implicitly converts an object of type <code>java.util.Map[K, V]</code> to a <code>JavaMapShouldWrapper[K, V]</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToJavaMapShouldWrapper[K, V](o: java.util.Map[K, V]): JavaMapShouldWrapper[K, V] = new JavaMapShouldWrapper[K, V](o)

  // Implicitly just used to trigger the addition of the should method. The LengthShouldWrapper
  // doesn't actually convert them, just passes it through. The conversion that happens here is to LengthShouldWrapper,
  // and later, inside ResultOfHaveWordForLengthWrapper, the implicit conversion from T to LengthWrapper takes place. So
  // weirdly enough, here strings are treated structurally for the implicit that adds the should, but later they are
  // treated nominally by the implicit conversion from plain old String to StringLengthWrapper. So when length is
  // ultimately invoked up in ResultOfHaveWordForLengthWrapper, it is done directly, not with reflection. That's my
  // theory anyway.

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>getLength</code> method that results in <code>Int</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasIntGetLengthMethodToLengthShouldWrapper[T <: AnyRef { def getLength(): Int}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>getLength</code> <code>val</code> of type <code>Int</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasIntGetLengthFieldToLengthShouldWrapper[T <: AnyRef { val getLength: Int}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>length</code> <code>val</code> of type <code>Int</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasIntLengthFieldToLengthShouldWrapper[T <: AnyRef { val length: Int}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>length</code> method that results in <code>Int</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasIntLengthMethodToLengthShouldWrapper[T <: AnyRef { def length(): Int}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)


  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>getLength</code> method that results in <code>Long</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasLongGetLengthMethodToLengthShouldWrapper[T <: AnyRef { def getLength(): Long}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>getLength</code> <code>val</code> of type <code>Long</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasLongGetLengthFieldToLengthShouldWrapper[T <: AnyRef { val getLength: Long}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>length</code> <code>val</code> of type <code>Long</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasLongLengthFieldToLengthShouldWrapper[T <: AnyRef { val length: Long}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>length</code> method that results in <code>Long</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasLongLengthMethodToLengthShouldWrapper[T <: AnyRef { def length(): Long}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>getSize</code> method that results in <code>Int</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasIntGetSizeMethodToSizeShouldWrapper[T <: AnyRef { def getSize(): Int}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>getSize</code> <code>val</code> of type <code>Int</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasIntGetSizeFieldToSizeShouldWrapper[T <: AnyRef { val getSize: Int}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>size</code> <code>val</code> of type <code>Int</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasIntSizeFieldToSizeShouldWrapper[T <: AnyRef { val size: Int}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>size</code> method that results in <code>Int</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasIntSizeMethodToSizeShouldWrapper[T <: AnyRef { def size(): Int}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)


  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>getSize</code> method that results in <code>Long</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasLongGetSizeMethodToSizeShouldWrapper[T <: AnyRef { def getSize(): Long}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>getSize</code> <code>val</code> of type <code>Long</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasLongGetSizeFieldToSizeShouldWrapper[T <: AnyRef { val getSize: Long}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>size</code> <code>val</code> type <code>Long</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertHasLongSizeFieldToSizeShouldWrapper[T <: AnyRef { val size: Long}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)

  /**
   * Implicitly converts an <code>AnyRef</code> of type <code>T</code> whose structure includes
   * a <code>size</code> method that results in <code>Long</code>
   * to a <code>SizeShouldWrapper[T]</code>, to enable <code>should</code> methods to be invokable on that object.
   */
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

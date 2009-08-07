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
package org.scalatest.junit

import _root_.junit.framework.AssertionFailedError
import scala.reflect.Manifest

/**
 * Trait that contains ScalaTest's basic assertion methods, suitable for use with JUnit 3.
 *
 * <p>
 * The assertion methods provided in this trait look and behave exactly like the ones in <code>org.scalatest.Assertions</code>,
 * except instead of throwing <code>org.scalatest.TestFailedException</code> they throw <code>junit.framework.AssertionFailedError</code>.
 * In JUnit version 3 and earlier, JUnit distinguished between <em>failures</em> and <em>errors</em>. (JUnit 4 dropped this distinction.)
 * If a test failed because of a failed assertion, that was considered a <em>failure</em> in JUnit 3. If a test failed for any other
 * reason, either the test code or the application being tested threw an unexpected exception, that was considered an <em>error</em> in JUnit 3.
 * </p>
 *
 * <p>
 * The way JUnit 3 decided whether an exception represented a failure or error is that only thrown
 * <code>junit.framework.AssertionFailedError</code>s were considered failures. Any other exception type was considered an error. The
 * exception type thrown by the JUnit 3 assertion methods declared in <code>junit.framework.Assert</code> (such as
 * <code>assertEquals</code>, <code>assertTrue</code>, and <code>fail</code>) was, therefore, <code>AssertionFailedError</code>.
 * </p>
 * 
 * <p>
 * The assertions provided by <code>org.scalatest.Assertions</code> throw <code>TestFailedException</code>, which does 
 * not extend <code>junit.framework.AssertionFailedError</code>. As a result, JUnit 3 will report a failed
 * <code>org.scalatest.Assertions</code> assertion as an error, not a failure. If <code>TestFailedException</code>
 * extended <code>AssertionFailedError</code>, anyone using ScalaTest would need to have JUnit on the
 * class path. To avoid this dependency, ScalaTest provides <code>org.scalatest.junit.AssertionsForJUnit3</code>, which gives JUnit 3 users
 * the same ScalaTest assertion syntax as <code>org.scalatest.Assertions</code> but with failed assertions
 * reported as failures, not errors, in JUnit 3.
 * </p>
 *
 * <p>
 * To use this trait in a JUnit 3 <code>TestCase</code>, you can mix it into your <code>TestCase</code> class, like this:
 * </p>
 *
 * <pre>
 * import junit.framework.TestCase
 * import org.scalatest.junit.AssertionsForJUnit3
 *
 * class MyTestCase extends TestCase with AssertionsForJUnit3 {
 *
 *   def testSomething() {
 *     assert("hi".charAt(1) === 'i')
 *   }
 *
 *   // ...
 * }
 * </pre>
 *
 * <p>
 * You can alternatively import the methods defined in this trait.
 * </p>
 *
 * <pre>
 * import junit.framework.TestCase
 * import org.scalatest.junit.AssertionsForJUnit3._
 *
 * class MyTestCase extends TestCase {
 *
 *   def testSomething() {
 *     assert("hi".charAt(1) === 'i')
 *   }
 *
 *   // ...
 * }
 * </pre>
 *
 * <p>
 * For details on the importing approach, see the documentation
 * for the <a href="AssertionsForJUnit3$object.html"><code>AssertionsForJUnit3</code> companion object</a>.
 * Now, on to the assertion syntax...
 * </p>
 *
 * <p>
 * In any Scala program, you can write assertions by invoking <code>assert</code> and passing in a <code>Boolean</code> expression,
 * such as:
 * </p>
 *
 * <pre>
 * val left = 2
 * val right = 1
 * assert(left == right)
 * </pre>
 *
 * <p>
 * If the passed expression is <code>true</code>, <code>assert</code> will return normally. If <code>false</code>,
 * <code>assert</code> will complete abruptly with an <code>AssertionError</code>. This behavior is provided by
 * the <code>assert</code> method defined in object <code>Predef</code>, whose members are implicitly imported into every
 * Scala source file. This <code>AssertionsForJUnit3</code> traits defines another <code>assert</code> method that hides the
 * one in <code>Predef</code>. It behaves the same, except that if <code>false</code> is passed it throws
 * <code>AssertionFailedError</code> instead of <code>AssertionError</code>. The reason it throws <code>AssertionFailedError</code>
 * is because <code>AssertionFailedError</code> carries information about exactly which item in the stack trace represents
 * the line of test code that failed, which can help users more quickly find an offending line of code in a failing test.
 * <p>
 *
 * <p>
 * If you pass the previous <code>Boolean</code> expression, <code>left == right</code> to <code>assert</code> in a ScalaTest test, a failure
 * will be reported, but without reporting the left and right values. You can alternatively encode these values in a <code>String</code> passed as
 * a second argument to <code>assert</code>, like this:
 * </p>
 * 
 * <pre>
 * val left = 2
 * val right = 1
 * assert(left == right, left + " did not equal " + right)
 * </pre>
 *
 * <p>
 * Using this form of <code>assert</code>, the failure report will include the left and right values, thereby
 * helping you debug the problem. However, ScalaTest provides the <code>===</code> operator to make this easier.
 * You use it like this:
 * </p>
 *
 * <pre>
 * val left = 2
 * val right = 1
 * assert(left === right)
 * </pre>
 *
 * <p>
 * Because you use <code>===</code> here instead of <code>==</code>, the failure report will include the left
 * and right values. For example, the detail message in the thrown <code>AssertionFailedError</code> from the <code>assert</code>
 * shown previously will include, "2 did not equal 1".
 * From this message you will know that the operand on the left had the value 2, and the operand on the right had the value 1.
 * </p>
 *
 * <p>
 * If you're familiar with JUnit, you would use <code>===</code>
 * in a ScalaTest <code>Suite</code> where you'd use <code>assertEquals</code> in a JUnit <code>TestCase</code>.
 * The <code>===</code> operator is made possible by an implicit conversion from <code>Any</code>
 * to <code>Equalizer</code>. If you're curious to understand the mechanics, see the <a href="AssertionsForJUnit3.Equalizer.html">documentation for
 * <code>Equalizer</code></a> and the <code>convertToEqualizer</code> method.
 * </p>
 *
 * <p>
 * <strong>Expected results</strong>
 * </p>
 *
 * Although <code>===</code> provides a natural, readable extension to Scala's <code>assert</code> mechanism,
 * as the operands become lengthy, the code becomes less readable. In addition, the <code>===</code> comparison
 * doesn't distinguish between actual and expected values. The operands are just called <code>left</code> and <code>right</code>,
 * because if one were named <code>expected</code> and the other <code>actual</code>, it would be difficult for people to
 * remember which was which. To help with these limitations of assertions, <code>Suite</code> includes a method called <code>expect</code> that
 * can be used as an alternative to <code>assert</code> with <code>===</code>. To use <code>expect</code>, you place
 * the expected value in parentheses after <code>expect</code>, followed by curly braces containing code 
 * that should result in the expected value. For example:
 *
 * <pre>
 * val a = 5
 * val b = 2
 * expect(2) {
 *   a - b
 * }
 * </pre>
 *
 * <p>
 * In this case, the expected value is <code>2</code>, and the code being tested is <code>a - b</code>. This expectation will fail, and
 * the detail message in the <code>AssertionFailedError</code> will read, "Expected 2, but got 3."
 * </p>
 *
 * <p>
 * <strong>Intercepted exceptions</strong>
 * </p>
 *
 * <p>
 * Sometimes you need to test whether a method throws an expected exception under certain circumstances, such
 * as when invalid arguments are passed to the method. You can do this in the JUnit 3 style, like this:
 * </p>
 *
 * <pre>
 * val s = "hi"
 * try {
 *   s.charAt(-1)
 *   fail()
 * }
 * catch {
 *   case _: IndexOutOfBoundsException => // Expected, so continue
 * }
 * </pre>
 *
 * <p>
 * If <code>charAt</code> throws <code>IndexOutOfBoundsException</code> as expected, control will transfer
 * to the catch case, which does nothing. If, however, <code>charAt</code> fails to throw an exception,
 * the next statement, <code>fail()</code>, will be run. The <code>fail</code> method always completes abruptly with
 * an <code>AssertionFailedError</code>, thereby signaling a failed test.
 * </p>
 *
 * <p>
 * To make this common use case easier to express and read, ScalaTest provides an <code>intercept</code>
 * method. You use it like this:
 * </p>
 *
 * <pre>
 * val s = "hi"
 * intercept[IndexOutOfBoundsException] {
 *   s.charAt(-1)
 * }
 * </pre>
 *
 * <p>
 * This code behaves much like the previous example. If <code>charAt</code> throws an instance of <code>IndexOutOfBoundsException</code>,
 * <code>intercept</code> will return that exception. But if <code>charAt</code> completes normally, or throws a different
 * exception, <code>intercept</code> will complete abruptly with an <code>AssertionFailedError</code>. <code>intercept</code> returns the
 * caught exception so that you can inspect it further if you wish, for example, to ensure that data contained inside
 * the exception has the expected values.
 * </p>
 *
 * @author Bill Venners
 */
trait AssertionsForJUnit3 {

  /**
   * Class used via an implicit conversion to enable any two objects to be compared with
   * <code>===</code> in assertions in JUnit 3 tests. For example:
   *
   * <pre>
   * assert(a === b)
   * </pre>
   *
   * <p>
   * The benefit of using <code>assert(a === b)</code> rather than <code>assert(a == b)</code> is
   * that an <code>AssertionFailedError</code> produced by the former will include the values of <code>a</code> and <code>b</code>
   * in its detail message.
   * The implicit method that performs the conversion from <code>Any</code> to <code>Equalizer</code> is
   * <code>convertToEqualizer</code> in trait <code>AssertionsForJUnit3</code>.
   * </p>
   *
   * <p>
   * In case you're not familiar with how implicit conversions work in Scala, here's a quick explanation.
   * The <code>convertToEqualizer</code> method in <code>AssertionsForJUnit3</code> is defined as an "implicit" method that takes an
   * <code>Any</code>, which means you can pass in any object, and it will convert it to an <code>Equalizer</code>.
   * The <code>Equalizer</code> has <code>===</code> defined. Most objects don't have <code>===</code> defined as a method
   * on them. Take two Strings, for example:
   * </p>
   *
   * <pre>
   * assert("hello" === "world")
   * </pre>
   *
   * <p>
   * Given this code, the Scala compiler looks for a <code>===</code> method on class <code>String</code>, because that's the class of
   * <code>"hello"</code>. <code>String</code> doesn't define <code>===</code>, so the compiler looks for an implicit conversion from
   * <code>String</code> to something that does have a <code>===</code> method, and it finds the <code>convertToEqualizer</code> method. It
   * then rewrites the code to this:
   * </p>
   *
   * <pre>
   * assert(convertToEqualizer("hello").===("world"))
   * </pre>
   *
   * <p>
   * So inside a JUnit 3 <code>TestCase</code> that mixes in <code>AssertionsForJUnit3</code> (or resides in a file
   * in which <code>AssertionsForJUnit3._</code> is imported), <code>===</code> will work on anything. The only situation in
   * which the implicit conversion wouldn't happen is on types that have an <code>===</code> method already defined.
   * </p>
   * 
   * <p>
   * The primary constructor takes one object, <code>left</code>, whose type is being converted to <code>Equalizer</code>. The <code>left</code>
   * value may be a <code>null</code> reference, because this is allowed by Scala's <code>==</code> operator.
   * </p>
   *
   * @param left An object to convert to <code>Equalizer</code>, which represents the <code>left</code> value
   *     of an assertion.
   *
   * @author Bill Venners
   */
  class Equalizer(left: Any) {

    /**
     * The <code>===</code> operation compares this <code>Equalizer</code>'s <code>left</code> value (passed
     * to the constructor, usually via an implicit conversion) with the passed <code>right</code> value 
     * for equality as determined by the expression <code>left == right</code>.
     * If <code>true</code>, <code>===</code> returns <code>None</code>. Else, <code>===</code> returns
     * a <code>Some</code> whose <code>String</code> value indicates the <code>left</code> and <code>right</code> values.
     *
     * <p>
     * In its typical usage, the <code>Option[String]</code> returned by <code>===</code> will be passed to one of two
     * of trait <code>Assertions</code>' overloaded <code>assert</code> methods. If <code>None</code>,
     * which indicates the assertion succeeded, <code>assert</code> will return normally. But if <code>Some</code> is passed,
     * which indicates the assertion failed, <code>assert</code> will throw an <code>AssertionFailedError</code> whose detail
     * message will include the <code>String</code> contained inside the <code>Some</code>, which in turn includes the
     * <code>left</code> and <code>right</code> values.
     * </p>
     */
    def ===(right: Any) =
      if (left == right)
        None
      else {
        val (leftee, rightee) = Suite.getObjectsForFailureMessage(left, right)
        Some(FailureMessages("didNotEqual", leftee, rightee))
      }
/*
    def !==(right: Any) =
      if (left != right)
        None
      else {
        val (leftee, rightee) = Suite.getObjectsForFailureMessage(left, right)
        Some(FailureMessages("equaled", leftee, rightee))
      }
*/
  }

  /**
   * Assert that a boolean condition is true.
   * If the condition is <code>true</code>, this method returns normally.
   * Else, it throws <code>AssertionFailedError</code>.
   *
   * @param condition the boolean condition to assert
   * @throws junit.framework.AssertionFailedError if the condition is <code>false</code>.
   */
  def assert(condition: Boolean) {
    if (!condition)
      throw new AssertionFailedError
  }

  /**
   * Assert that a boolean condition, described in <code>String</code>
   * <code>message</code>, is true.
   * If the condition is <code>true</code>, this method returns normally.
   * Else, it throws <code>AssertionFailedError</code> with the
   * <code>String</code> obtained by invoking <code>toString</code> on the
   * specified <code>message</code> as the exception's detail message.
   *
   * @param condition the boolean condition to assert
   * @param message An objects whose <code>toString</code> method returns a message to include in a failure report.
   * @throws junit.framework.AssertionFailedError if the condition is <code>false</code>.
   * @throws NullPointerException if <code>message</code> is <code>null</code>.
   */
  def assert(condition: Boolean, message: Any) {
    if (!condition)
      throw new AssertionFailedError(message.toString)
  }

  /**
   * Assert that an <code>Option[String]</code> is <code>None</code>. 
   * If the condition is <code>None</code>, this method returns normally.
   * Else, it throws <code>AssertionFailedError</code> with the <code>String</code>
   * value of the <code>Some</code>, as well as the 
   * <code>String</code> obtained by invoking <code>toString</code> on the
   * specified <code>message</code>,
   * included in the <code>AssertionFailedError</code>'s detail message.
   *
   * <p>
   * This form of <code>assert</code> is usually called in conjunction with an
   * implicit conversion to <code>Equalizer</code>, using a <code>===</code> comparison, as in:
   * </p>
   *
   * <pre>
   * assert(a === b, "extra info reported if assertion fails")
   * </pre>
   *
   * <p>
   * For more information on how this mechanism works, see the <a href="Suite.Equalizer.html">documentation for
   * <code>Equalizer</code></a>.
   * </p>
   *
   * @param o the <code>Option[String]</code> to assert
   * @param message An objects whose <code>toString</code> method returns a message to include in a failure report.
   * @throws junit.framework.AssertionFailedError if the <code>Option[String]</code> is <code>Some</code>.
   * @throws NullPointerException if <code>message</code> is <code>null</code>.
   */
  def assert(o: Option[String], message: Any) {
    o match {
      case Some(s) => throw new AssertionFailedError(message + "\n" + s)
      case None =>
    }
  }
  
  /**
   * Assert that an <code>Option[String]</code> is <code>None</code>.
   * If the condition is <code>None</code>, this method returns normally.
   * Else, it throws <code>AssertionFailedError</code> with the <code>String</code>
   * value of the <code>Some</code> included in the <code>AssertionFailedError</code>'s
   * detail message.
   *
   * <p>
   * This form of <code>assert</code> is usually called in conjunction with an
   * implicit conversion to <code>Equalizer</code>, using a <code>===</code> comparison, as in:
   * </p>
   *
   * <pre>
   * assert(a === b)
   * </pre>
   *
   * <p>
   * For more information on how this mechanism works, see the <a href="Suite.Equalizer.html">documentation for
   * <code>Equalizer</code></a>.
   * </p>
   *
   * @param o the <code>Option[String]</code> to assert
   * @throws junit.framework.AssertionFailedError if the <code>Option[String]</code> is <code>Some</code>.
   */
  def assert(o: Option[String]) {
    o match {
      case Some(s) => throw new AssertionFailedError(s)
      case None =>
    }
  }

  /**
   * Implicit conversion from <code>Any</code> to <code>Equalizer</code>, used to enable
   * assertions with <code>===</code> comparisons.
   *
   * <p>
   * For more information
   * on this mechanism, see the <a href="Suite.Equalizer.html">documentation for </code>Equalizer</code></a>.
   * </p>
   *
   * @param left the object whose type to convert to <code>Equalizer</code>.
   * @throws NullPointerException if <code>left</code> is <code>null</code>.
   */
  implicit def convertToEqualizer(left: Any) = new Equalizer(left)

  /**
   * Intercept and return an exception that's expected to
   * be thrown by the passed function value. The thrown exception must be an instance of the
   * type specified by the type parameter of this method. This method invokes the passed
   * function. If the function throws an exception that's an instance of the specified type,
   * this method returns that exception. Else, whether the passed function returns normally
   * or completes abruptly with a different exception, this method throws <code>AssertionFailedError</code>.
   *
   * <p>
   * Note that the type specified as this method's type parameter may represent any subtype of
   * <code>AnyRef</code>, not just <code>Throwable</code> or one of its subclasses. In
   * Scala, exceptions can be caught based on traits they implement, so it may at times make sense
   * to specify a trait that the intercepted exception's class must mix in. If a class instance is
   * passed for a type that could not possibly be used to catch an exception (such as <code>String</code>,
   * for example), this method will complete abruptly with an <code>AssertionFailedError</code>.
   * </p>
   *
   * @param f the function value that should throw the expected exception
   * @param manifest an implicit <code>Manifest</code> representing the type of the specified
   * type parameter.
   * @return the intercepted exception, if it is of the expected type
   * @throws junit.framework.AssertionFailedError if the passed function does not complete abruptly with an exception
   *    that's an instance of the specified type
   *     passed <code>expected</code> value.
   */
  def intercept[T <: AnyRef](f: => Any)(implicit manifest: Manifest[T]): T = {
    val clazz = manifest.erasure.asInstanceOf[Class[T]]
    val caught = try {
      f
      None
    }
    catch {
      case u: Throwable => {
        if (!clazz.isAssignableFrom(u.getClass)) {
          val s = Resources("wrongException", clazz.getName, u.getClass.getName)
          val assertionFailedError = new AssertionFailedError(s)
          assertionFailedError.initCause(u)
          throw assertionFailedError
        }
        else {
          Some(u)
        }
      }
    }
    caught match {
      case None =>
        val message = Resources("exceptionExpected", clazz.getName)
        throw new AssertionFailedError(message)
      case Some(e) => e.asInstanceOf[T] // I know this cast will succeed, becuase iSAssignableFrom succeeded above
    }
  }

  /*
   * Intercept and return an instance of the passed exception class (or an instance of a subclass of the
   * passed class), which is expected to be thrown by the passed function value. This method invokes the passed
   * function. If it throws an exception that's an instance of the passed class or one of its
   * subclasses, this method returns that exception. Else, whether the passed function returns normally
   * or completes abruptly with a different exception, this method throws <code>AssertionFailedError</code>.
   *
   * <p>
   * Note that the passed <code>Class</code> may represent any type, not just <code>Throwable</code> or one of its subclasses. In
   * Scala, exceptions can be caught based on traits they implement, so it may at times make sense to pass in a class instance for
   * a trait. If a class instance is passed for a type that could not possibly be used to catch an exception (such as <code>String</code>,
   * for example), this method will complete abruptly with an <code>AssertionFailedError</code>.
   * </p>
   *
   * @param clazz a type to which the expected exception class is assignable, i.e., the exception should be an instance of the type represented by <code>clazz</code>.
   * @param f the function value that should throw the expected exception
   * @return the intercepted exception, if 
   * @throws junit.framework.AssertionFailedError if the passed function does not complete abruptly with an exception that is assignable to the 
   *     passed <code>Class</code>.
   * @throws IllegalArgumentException if the passed <code>clazz</code> is not <code>Throwable</code> or
   *     one of its subclasses.
   */

/*
  def intercept[T <: AnyRef](clazz: java.lang.Class[T])(f: => Unit): T = {
    // intercept(clazz)(f)(manifest)
    "hi".asInstanceOf[T]
  }
*/
/*
  def intercept[T <: AnyRef](clazz: java.lang.Class[T])(f: => Unit)(implicit manifest: Manifest[T]): T = {
    intercept(clazz)(f)(manifest)
  }
*/


  /**
   * Expect that the value passed as <code>expected</code> equals the value passed as <code>actual</code>.
   * If the <code>actual</code> equals the <code>expected</code>
   * (as determined by <code>==</code>), <code>expect</code> returns
   * normally. Else, if <code>actual</code> is not equal to <code>expected</code>, <code>expect</code> throws an
   * <code>AssertionFailedError</code> whose detail message includes the expected and actual values, as well as the <code>String</code>
   * obtained by invoking <code>toString</code> on the passed <code>message</code>.
   *
   * @param expected the expected value
   * @param message An object whose <code>toString</code> method returns a message to include in a failure report.
   * @param actual the actual value, which should equal the passed <code>expected</code> value
   * @throws junit.framework.AssertionFailedError if the passed <code>actual</code> value does not equal the passed <code>expected</code> value.
   */
  def expect(expected: Any, message: Any)(actual: Any) {
    if (actual != expected) {
      val (act, exp) = Suite.getObjectsForFailureMessage(actual, expected)
      val s = FailureMessages("expectedButGot", exp, act)
      throw new AssertionFailedError(message + "\n" + s)
    }
  }

  /** 
   * Expect that the value passed as <code>expected</code> equals the value passed as <code>actual</code>.
   * If the <code>actual</code> value equals the <code>expected</code> value
   * (as determined by <code>==</code>), <code>expect</code> returns
   * normally. Else, <code>expect</code> throws an
   * <code>AssertionFailedError</code> whose detail message includes the expected and actual values.
   *
   * @param expected the expected value
   * @param actual the actual value, which should equal the passed <code>expected</code> value
   * @throws junit.framework.AssertionFailedError if the passed <code>actual</code> value does not equal the passed <code>expected</code> value.
   */
  def expect(expected: Any)(actual: Any) {
    if (actual != expected) {
      val (act, exp) = Suite.getObjectsForFailureMessage(actual, expected)
      val s = FailureMessages("expectedButGot", exp, act)
      throw new AssertionFailedError(s)
    }
  }
  
  /**
   * Throws <code>AssertionFailedError</code> to indicate a test failed.
   */
  def fail() = throw new AssertionFailedError

  /**
   * Throws <code>AssertionFailedError</code>, with the passed
   * <code>String</code> <code>message</code> as the exception's detail
   * message, to indicate a test failed.
   *
   * @param message A message describing the failure.
   * @throws NullPointerException if <code>message</code> is <code>null</code>
   */
  def fail(message: String) = {

    if (message == null)
        throw new NullPointerException("message is null")
     
    throw new AssertionFailedError(message)
  }

  /**
   * Throws <code>AssertionFailedError</code>, with the passed
   * <code>String</code> <code>message</code> as the exception's detail
   * message and <code>Throwable</code> cause, to indicate a test failed.
   *
   * @param message A message describing the failure.
   * @param cause A <code>Throwable</code> that indicates the cause of the failure.
   * @throws NullPointerException if <code>message</code> or <code>cause</code> is <code>null</code>
   */
  def fail(message: String, cause: Throwable) = {

    if (message == null)
      throw new NullPointerException("message is null")

    if (cause == null)
      throw new NullPointerException("cause is null")

    val assertionFailedError = new AssertionFailedError(message)
    assertionFailedError.initCause(cause)
    throw assertionFailedError
  }

  /**
   * Throws <code>AssertionFailedError</code>, with the passed
   * <code>Throwable</code> cause, to indicate a test failed.
   * The <code>getMessage</code> method of the thrown <code>AssertionFailedError</code>
   * will return <code>cause.toString()</code>.
   *
   * @param cause a <code>Throwable</code> that indicates the cause of the failure.
   * @throws NullPointerException if <code>cause</code> is <code>null</code>
   */
  def fail(cause: Throwable) = {

    if (cause == null)
      throw new NullPointerException("cause is null")
        
    val assertionFailedError = new AssertionFailedError
    assertionFailedError.initCause(cause)
    throw assertionFailedError
  }
}

/**
 * Companion object that facilitates the importing of <code>AssertionsForJUnit3</code> members as 
 * an alternative to mixing it in. One use case is to import <code>AssertionsForJUnit3</code> members so you can use
 * them in the Scala interpreter:
 *
 * <pre>
 * $ scala -cp junit3.8.2/junit.jar:../target/jar_contents 
 * Welcome to Scala version 2.7.5.final (Java HotSpot(TM) Client VM, Java 1.5.0_16).
 * Type in expressions to have them evaluated.
 * Type :help for more information.
 *
 * scala> import org.scalatest.junit.AssertionsForJUnit3._
 * import org.scalatest.junit.AssertionsForJUnit3._
 *
 * scala> assert(1 === 2)
 * junit.framework.AssertionFailedError: 1 did not equal 2
 * 	at org.scalatest.junit.AssertionsForJUnit3$class.assert(AssertionsForJUnit3.scala:353)
 * 	at org.scalatest.junit.AssertionsForJUnit3$.assert(AssertionsForJUnit3.scala:672)
 * 	at .<init>(<console>:7)
 * 	at .<clinit>(<console>)
 * 	at RequestResult$.<init>(<console>:3)
 * 	at RequestResult$.<clinit>(<console>)
 * 	at RequestResult$result(<consol...
 * scala> expect(3) { 1 + 3 }
 * junit.framework.AssertionFailedError: Expected 3, but got 4
 * 	at org.scalatest.junit.AssertionsForJUnit3$class.expect(AssertionsForJUnit3.scala:563)
 * 	at org.scalatest.junit.AssertionsForJUnit3$.expect(AssertionsForJUnit3.scala:672)
 * 	at .<init>(<console>:7)
 * 	at .<clinit>(<console>)
 * 	at RequestResult$.<init>(<console>:3)
 * 	at RequestResult$.<clinit>(<console>)
 * 	at RequestResult$result(<co...
 * scala> val caught = intercept[StringIndexOutOfBoundsException] { "hi".charAt(-1) }
 * caught: StringIndexOutOfBoundsException = java.lang.StringIndexOutOfBoundsException: String index out of range: -1
 * <pre>
 *
 * @author Bill Venners
 */
object AssertionsForJUnit3 extends AssertionsForJUnit3

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

/**
 * Trait that contains ScalaTest's basic assertion methods.
 *
 * @author Bill Venners
 */
trait Assertions {

  /**
   * Class used via an implicit conversion to enable any two objects to be compared with
   * <code>===</code> in assertions in tests. For example:
   *
   * <pre>
   * assert(a === b)
   * </pre>
   *
   * <p>
   * The benefit of using <code>assert(a === b)</code> rather than <code>assert(a == b)</code> is
   * that a <code>TestFailedException</code> produced by the former will include the values of <code>a</code> and <code>b</code>
   * in its detail message.
   * The implicit method that performs the conversion from <code>Any</code> to <code>Equalizer</code> is
   * <code>convertToEqualizer</code> in trait <code>Suite</code>.
   * </p>
   *
   * <p>
   * In case you're not familiar with how implicit conversions work in Scala, here's a quick explanation.
   * The <code>convertToEqualizer</code> method in <code>Suite</code> is defined as an "implicit" method that takes an
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
   * Given this code, the Scala compiler looks for an <code>===</code> method on class <code>String</code>, because that's the class of
   * <code>"hello"</code>. <code>String</code> doesn't define <code>===</code>, so the compiler looks for an implicit conversion from
   * <code>String</code> to something that does have an <code>===</code> method, and it finds the <code>convertToEqualizer</code> method. It
   * then rewrites the code to this:
   * </p>
   *
   * <pre>
   * assert(convertToEqualizer("hello").===("world"))
   * </pre>
   *
   * <p>
   * So inside a <code>Suite</code>, <code>===</code> will work on anything. The only situation in which the implicit conversion wouldn't 
   * happen is on types that have an <code>===</code> method already defined.
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
     * of trait <code>Suite</code>'s overloaded <code>assert</code> methods. If <code>None</code>,
     * which indicates the assertion succeeded, <code>assert</code> will return normally. But if <code>Some</code> is passed,
     * which indicates the assertion failed, <code>assert</code> will throw a <code>TestFailedException</code> whose detail
     * message will include the <code>String</code> contained inside the <code>Some</code>, which in turn includes the
     * <code>left</code> and <code>right</code> values. This <code>TestFailedException</code> is typically embedded in a 
     * <code>Report</code> and passed to a <code>Reporter</code>, which can present the <code>left</code> and <code>right</code>
     * values to the user.
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
   * Else, it throws <code>TestFailedException</code>.
   *
   * @param condition the boolean condition to assert
   * @throws TestFailedException if the condition is <code>false</code>.
   */
  def assert(condition: Boolean) {
    if (!condition)
      throw new TestFailedException(2)
  }

  /**
   * Assert that a boolean condition, described in <code>String</code>
   * <code>message</code>, is true.
   * If the condition is <code>true</code>, this method returns normally.
   * Else, it throws <code>TestFailedException</code> with the
   * <code>String</code> obtained by invoking <code>toString</code> on the
   * specified <code>message</code> as the exception's detail message.
   *
   * @param condition the boolean condition to assert
   * @param message An objects whose <code>toString</code> method returns a message to include in a failure report.
   * @throws TestFailedException if the condition is <code>false</code>.
   * @throws NullPointerException if <code>message</code> is <code>null</code>.
   */
  def assert(condition: Boolean, message: Any) {
    if (!condition)
      throw new TestFailedException(message.toString, 2)
  }

  /**
   * Assert that an <code>Option[String]</code> is <code>None</code>. 
   * If the condition is <code>None</code>, this method returns normally.
   * Else, it throws <code>TestFailedException</code> with the <code>String</code>
   * value of the <code>Some</code>, as well as the 
   * <code>String</code> obtained by invoking <code>toString</code> on the
   * specified <code>message</code>,
   * included in the <code>TestFailedException</code>'s detail message.
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
   * @throws TestFailedException if the <code>Option[String]</code> is <code>Some</code>.
   * @throws NullPointerException if <code>message</code> is <code>null</code>.
   */
  def assert(o: Option[String], message: Any) {
    o match {
      case Some(s) => throw new TestFailedException(message + "\n" + s, 2)
      case None =>
    }
  }
  
  /**
   * Assert that an <code>Option[String]</code> is <code>None</code>.
   * If the condition is <code>None</code>, this method returns normally.
   * Else, it throws <code>TestFailedException</code> with the <code>String</code>
   * value of the <code>Some</code> included in the <code>TestFailedException</code>'s
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
   * @throws TestFailedException if the <code>Option[String]</code> is <code>Some</code>.
   */
  def assert(o: Option[String]) {
    o match {
      case Some(s) => throw new TestFailedException(s, 2)
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
   * <p>
   * Because trait <code>Suite</code> mixes in <code>Assertions</code>, this implicit conversion will always be
   * available by default in ScalaTest <code>Suite</code>s. This is the only implicit conversion that is in scope by default in every
   * ScalaTest <code>Suite</code>. Other implicit conversions offered by ScalaTest, such as those that support the matchers DSL
   * or <code>invokePrivate</code>, must be explicitly invited into your test code, either by mixing in a trait or importing the
   * members of its companion object. The reason ScalaTest requires you to invite in implicit conversions (with the exception of the
   * implicit conversion for <code>===</code> operator)  is because if one of ScalaTest's implicit conversions clashes with an
   * implicit conversion used in the code you are trying to test, your program won't compile. Thus there is a chance that if you
   * are ever trying to use a library or test some code that also offers an implicit conversion involving a <code>===</code> operator,
   * you could run into the problem of a compiler error due to an ambiguous implicit conversion. If that happens, you can turn off
   * the implicit conversion offered by this <code>convertToEqualizer</code> method simply by overriding the method in your
   * <code>Suite</code> subclass, but not marking it as implicit:
   * </p>
   *
   * <pre>
   * // In your Suite subclass
   * override def convertToEqualizer(left: Any) = new Equalizer(left)
   * </pre>
   * 
   * @param left the object whose type to convert to <code>Equalizer</code>.
   * @throws NullPointerException if <code>left</code> is <code>null</code>.
   */
  implicit def convertToEqualizer(left: Any) = new Equalizer(left)

  /*
   * Intercept and return an instance of the passed exception class (or an instance of a subclass of the
   * passed class), which is expected to be thrown by the passed function value. This method invokes the passed
   * function. If it throws an exception that's an instance of the passed class or one of its
   * subclasses, this method returns that exception. Else, whether the passed function returns normally
   * or completes abruptly with a different exception, this method throws <code>TestFailedException</code>
   * whose detail message includes the <code>String</code> obtained by invoking <code>toString</code> on the passed <code>message</code>.
   *
   * <p>
   * Note that the passed <code>Class</code> may represent any type, not just <code>Throwable</code> or one of its subclasses. In
   * Scala, exceptions can be caught based on traits they implement, so it may at times make sense to pass in a class instance for
   * a trait. If a class instance is passed for a type that could not possibly be used to catch an exception (such as <code>String</code>,
   * for example), this method will complete abruptly with a <code>TestFailedException</code>.
   * </p>
   *
   * @param message An object whose <code>toString</code> method returns a message to include in a failure report.
   * @param f the function value that should throw the expected exception
   * @return the intercepted exception, if it is of the expected type
   * @throws TestFailedException if the passed function does not result in a value equal to the
   *     passed <code>expected</code> value.
  def intercept[T <: AnyRef](message: Any)(f: => Any)(implicit manifest: Manifest[T]): T = {
    val clazz = manifest.erasure.asInstanceOf[Class[T]]
    val messagePrefix = if (message.toString.trim.isEmpty) "" else (message +"\n")
    val caught = try {
      f
      None
    }
    catch {
      case u: Throwable => {
        if (!clazz.isAssignableFrom(u.getClass)) {
          val s = Resources("wrongException", clazz.getName, u.getClass.getName)
          throw new TestFailedException(messagePrefix + s, u, 2)
        }
        else {
          Some(u)
        }
      }
    }
    caught match {
      case None =>
        val message = messagePrefix + Resources("exceptionExpected", clazz.getName)
        throw new TestFailedException(message, 2)
      case Some(e) => e.asInstanceOf[T] // I know this cast will succeed, becuase iSAssignableFrom succeeded above
    }
  }
THIS DOESN'T OVERLOAD. I THINK I'LL EITHER NEED TO USE interceptWithMessage OR JUST LEAVE IT OUT. FOR NOW I'LL LEAVE IT OUT.
   */

  /**
   * Intercept and return an exception that's expected to
   * be thrown by the passed function value. The thrown exception must be an instance of the
   * type specified by the type parameter of this method. This method invokes the passed
   * function. If the function throws an exception that's an instance of the specified type,
   * this method returns that exception. Else, whether the passed function returns normally
   * or completes abruptly with a different exception, this method throws <code>TestFailedException</code>.
   *
   * <p>
   * Note that the type specified as this method's type parameter may represent any subtype of
   * <code>AnyRef</code>, not just <code>Throwable</code> or one of its subclasses. In
   * Scala, exceptions can be caught based on traits they implement, so it may at times make sense
   * to specify a trait that the intercepted exception's class must mix in. If a class instance is
   * passed for a type that could not possibly be used to catch an exception (such as <code>String</code>,
   * for example), this method will complete abruptly with a <code>TestFailedException</code>.
   * </p>
   *
   * @param f the function value that should throw the expected exception
   * @param manifest an implicit <code>Manifest</code> representing the type of the specified
   * type parameter.
   * @return the intercepted exception, if it is of the expected type
   * @throws TestFailedException if the passed function does not complete abruptly with an exception
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
          throw new TestFailedException(s, u, 2)
        }
        else {
          Some(u)
        }
      }
    }
    caught match {
      case None =>
        val message = Resources("exceptionExpected", clazz.getName)
        throw new TestFailedException(message, 2)
      case Some(e) => e.asInstanceOf[T] // I know this cast will succeed, becuase iSAssignableFrom succeeded above
    }
  }

  /*
   * Intercept and return an instance of the passed exception class (or an instance of a subclass of the
   * passed class), which is expected to be thrown by the passed function value. This method invokes the passed
   * function. If it throws an exception that's an instance of the passed class or one of its
   * subclasses, this method returns that exception. Else, whether the passed function returns normally
   * or completes abruptly with a different exception, this method throws <code>TestFailedException</code>.
   *
   * <p>
   * Note that the passed <code>Class</code> may represent any type, not just <code>Throwable</code> or one of its subclasses. In
   * Scala, exceptions can be caught based on traits they implement, so it may at times make sense to pass in a class instance for
   * a trait. If a class instance is passed for a type that could not possibly be used to catch an exception (such as <code>String</code>,
   * for example), this method will complete abruptly with a <code>TestFailedException</code>.
   * </p>
   *
   * @param clazz a type to which the expected exception class is assignable, i.e., the exception should be an instance of the type represented by <code>clazz</code>.
   * @param f the function value that should throw the expected exception
   * @return the intercepted exception, if 
   * @throws TestFailedException if the passed function does not complete abruptly with an exception that is assignable to the 
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
   * <code>TestFailedException</code> whose detail message includes the expected and actual values, as well as the <code>String</code>
   * obtained by invoking <code>toString</code> on the passed <code>message</code>.
   *
   * @param expected the expected value
   * @param message An object whose <code>toString</code> method returns a message to include in a failure report.
   * @param actual the actual value, which should equal the passed <code>expected</code> value
   * @throws TestFailedException if the passed <code>actual</code> value does not equal the passed <code>expected</code> value.
   */
  def expect(expected: Any, message: Any)(actual: Any) {
    if (actual != expected) {
      val (act, exp) = Suite.getObjectsForFailureMessage(actual, expected)
      val s = FailureMessages("expectedButGot", exp, act)
      throw new TestFailedException(message + "\n" + s, 2)
    }
  }

  /** 
   * Expect that the value passed as <code>expected</code> equals the value passed as <code>actual</code>.
   * If the <code>actual</code> value equals the <code>expected</code> value
   * (as determined by <code>==</code>), <code>expect</code> returns
   * normally. Else, <code>expect</code> throws an
   * <code>TestFailedException</code> whose detail message includes the expected and actual values.
   *
   * @param expected the expected value
   * @param actual the actual value, which should equal the passed <code>expected</code> value
   * @throws TestFailedException if the passed <code>actual</code> value does not equal the passed <code>expected</code> value.
   */
  def expect(expected: Any)(actual: Any) {
    if (actual != expected) {
      val (act, exp) = Suite.getObjectsForFailureMessage(actual, expected)
      val s = FailureMessages("expectedButGot", exp, act)
      throw new TestFailedException(s, 2)
    }
  }
  
  /**
   * Throws <code>TestFailedException</code> to indicate a test failed.
   */
  def fail() = throw new TestFailedException(2)

  /**
   * Throws <code>TestFailedException</code>, with the passed
   * <code>String</code> <code>message</code> as the exception's detail
   * message, to indicate a test failed.
   *
   * @param message A message describing the failure.
   * @throws NullPointerException if <code>message</code> is <code>null</code>
   */
  def fail(message: String) = {

    if (message == null)
        throw new NullPointerException("message is null")
     
    throw new TestFailedException(message, 2)
  }

  /**
   * Throws <code>TestFailedException</code>, with the passed
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

    throw new TestFailedException(message, cause, 2)
  }

  /**
   * Throws <code>TestFailedException</code>, with the passed
   * <code>Throwable</code> cause, to indicate a test failed.
   * The <code>getMessage</code> method of the thrown <code>TestFailedException</code>
   * will return <code>cause.toString()</code>.
   *
   * @param cause a <code>Throwable</code> that indicates the cause of the failure.
   * @throws NullPointerException if <code>cause</code> is <code>null</code>
   */
  def fail(cause: Throwable) = {

    if (cause == null)
      throw new NullPointerException("cause is null")
        
    throw new TestFailedException(cause, 2)
  }
}

/**
 * Companion object that facilitates the importing of <code>Assertions</code> members as 
 * an alternative to mixing it in. One use case is to import <code>Assertions</code> members so you can use
 * them in the Scala interpreter:
 *
 * <pre>
 * $scala -classpath scalatest.jar
 * Welcome to Scala version 2.7.3.final (Java HotSpot(TM) Client VM, Java 1.5.0_16).
 * Type in expressions to have them evaluated.
 * Type :help for more information.
 * 
 * scala> import org.scalatest.Assertions._
 * import org.scalatest.Assertions._
 * 
 * scala> assert(1 === 2)
 * org.scalatest.TestFailedException: 1 did not equal 2
 * 	at org.scalatest.Assertions$class.assert(Assertions.scala:211)
 * 	at org.scalatest.Assertions$.assert(Assertions.scala:511)
 * 	at .<init>(<console>:7)
 * 	at .<clinit>(<console>)
 * 	at RequestResult$.<init>(<console>:3)
 * 	at RequestResult$.<clinit>(<console>)
 * 	at RequestResult$result(<console>)
 * 	at sun.reflect.NativeMethodAccessorImpl.invoke...
 *
 * scala> expect(3) { 1 + 3 }
 * org.scalatest.TestFailedException: Expected 3, but got 4
 * 	at org.scalatest.Assertions$class.expect(Assertions.scala:447)
 * 	at org.scalatest.Assertions$.expect(Assertions.scala:511)
 * 	at .<init>(<console>:7)
 * 	at .<clinit>(<console>)
 * 	at RequestResult$.<init>(<console>:3)
 * 	at RequestResult$.<clinit>(<console>)
 * 	at RequestResult$result(<console>)
 * 	at sun.reflect.NativeMethodAccessorImpl.in...
 *
 * scala> val caught = intercept[StringIndexOutOfBoundsException] { "hi".charAt(-1) }
 * caught: StringIndexOutOfBoundsException = java.lang.StringIndexOutOfBoundsException: String index out of range: -1
 * <pre>
 *
 * @author Bill Venners
 */
object Assertions extends Assertions

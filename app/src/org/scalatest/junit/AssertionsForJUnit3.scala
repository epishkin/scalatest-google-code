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
 * For the details on the <code>AssertionsForJUnit3</code> syntax, see the Scaladoc documentation for
 * <a href="../Assertions.html"><code>org.scalatest.Assertions</code></a>
 * </p>
 *
 * @author Bill Venners
 */
trait AssertionsForJUnit3 extends Assertions {
  protected[scalatest] override def newAssertionFailedException(message: Option[Any], cause: Option[Throwable], stackDepth: Int): Throwable =
    message match {
      case Some(msg) => new AssertionFailedError(msg.toString)
      case None => new AssertionFailedError
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

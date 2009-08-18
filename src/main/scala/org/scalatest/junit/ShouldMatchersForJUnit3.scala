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
import org.scalatest.matchers.ShouldMatchers

/**
 * Trait that makes ScalaTest's <code>ShouldMatchers</code> DSL syntax available for use with JUnit 3.
 *
 * <p>
 * The matchers provided in this trait look and behave exactly like the ones
 * in <code>org.scalatest.matchers.ShouldMatchers</code>,
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
 * The matchers provided by <code>org.scalatest.matchers.ShouldMatchers</code> throw <code>TestFailedException</code>, which does 
 * not extend <code>junit.framework.AssertionFailedError</code>. As a result, JUnit 3 will report a failed
 * <code>org.scalatest.matchers.ShouldMatchers</code> match as an error, not a failure. If <code>TestFailedException</code>
 * extended <code>AssertionFailedError</code>, anyone using ScalaTest would need to have JUnit on the
 * class path. To avoid this dependency, ScalaTest provides <code>org.scalatest.junit.ShouldMatchersForJUnit3</code>, which gives JUnit 3 users
 * the same ScalaTest matcher DSL syntax as <code>org.scalatest.matchers.ShouldMatchers</code> but with failed matches
 * reported as failures, not errors, in JUnit 3.
 * </p>
 *
 * <p>
 * To use this trait in a JUnit 3 <code>TestCase</code>, you can mix it into your <code>TestCase</code> class, like this:
 * </p>
 *
 * <pre>
 * import junit.framework.TestCase
 * import org.scalatest.junit.ShouldMatchersForJUnit3
 *
 * class MyTestCase extends TestCase with ShouldMatchersForJUnit3 {
 *
 *   def testSomething() {
 *     "hello, world!" should startWith ("hello")
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
 * import org.scalatest.junit.ShouldMatchersForJUnit3._
 *
 * class MyTestCase extends TestCase {
 *
 *   def testSomething() {
 *     "hello, world!" should startWith ("hello")
 *   }
 *
 *   // ...
 * }
 * </pre>
 *
 * <p>
 * For details on the importing approach, see the documentation
 * for the <a href="ShouldMatchersForJUnit3$object.html"><code>ShouldMatchersForJUnit3</code> companion object</a>.
 * For the details on the <code>ShouldMatchersForJUnit3</code> syntax, see the Scaladoc documentation for
 * <a href="../matchers/ShouldMatchers.html"><code>org.scalatest.matchers.ShouldMatchers</code></a>
 * </p>
 *
 * @author Bill Venners
 */
trait ShouldMatchersForJUnit3 extends ShouldMatchers with AssertionsForJUnit3 {
  private[scalatest] override def newTestFailedException(message: String): Throwable = new AssertionFailedError(message)
}

/**
 * Companion object that facilitates the importing of <code>ShouldMatchersForJUnit3</code> members as 
 * an alternative to mixing it in. One use case is to import <code>ShouldMatchersForJUnit3</code> members so you can use
 * them in the Scala interpreter:
 *
 * <pre>
 * Macintosh-65:delus bv$ scala -cp .:../target/jar_contents:junit3.8.2/junit.jar
 * Welcome to Scala version 2.7.5.final (Java HotSpot(TM) Client VM, Java 1.5.0_16).
 * Type in expressions to have them evaluated.
 * Type :help for more information.
 * 
 * scala> import org.scalatest.junit.ShouldMatchersForJUnit3._
 * import org.scalatest.junit.ShouldMatchersForJUnit3._
 * 
 * scala> "hi" should have length (3)
 * junit.framework.AssertionFailedError: "hi" did not have length 3
 * 	at org.scalatest.junit.ShouldMatchersForJUnit3$class.newTestFailedException(ShouldMatchersForJUnit3.scala:22)
 * 	at org.scalatest.junit.ShouldMatchersForJUnit3$.newTestFailedException(ShouldMatchersForJUnit3.scala:63)
 * 	at org.scalatest.matchers.Matchers$ResultOfHaveWordForString.length(Matchers.scala:4102)
 * 	at .<init>(<co...
 * scala> 1 should equal (2)
 * junit.framework.AssertionFailedError: 1 did not equal 2
 * 	at org.scalatest.junit.ShouldMatchersForJUnit3$class.newTestFailedException(ShouldMatchersForJUnit3.scala:22)
 * 	at org.scalatest.junit.ShouldMatchersForJUnit3$.newTestFailedException(ShouldMatchersForJUnit3.scala:63)
 * 	at org.scalatest.matchers.ShouldMatchers$ShouldMethodHelper$.shouldMatcher(ShouldMatchers.scala:800)
 * 	at org.scal...
 * scala> "hello, world" should startWith ("hello")
 * 
 * scala> 7 should (be >= (3) and not be <= (7))
 * junit.framework.AssertionFailedError: 7 was greater than or equal to 3, but 7 was less than or equal to 7
 * 	at org.scalatest.junit.ShouldMatchersForJUnit3$class.newTestFailedException(ShouldMatchersForJUnit3.scala:22)
 * 	at org.scalatest.junit.ShouldMatchersForJUnit3$.newTestFailedException(ShouldMatchersForJUnit3.scala:63)
 * 	at org.scalatest.matchers.ShouldMatchers$ShouldMethodHelper$.sh...
 * <pre>
 *
 * @author Bill Venners
 */
object ShouldMatchersForJUnit3 extends ShouldMatchersForJUnit3

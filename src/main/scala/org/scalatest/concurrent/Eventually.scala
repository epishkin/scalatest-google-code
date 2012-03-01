/*
 * Copyright 2001-2011 Artima, Inc.
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
package org.scalatest.concurrent

import org.scalatest._
import java.awt.AWTError
import java.nio.charset.CoderMalfunctionError
import javax.xml.parsers.FactoryConfigurationError
import javax.xml.transform.TransformerFactoryConfigurationError
import java.lang.annotation.AnnotationFormatError
import org.scalatest.StackDepthExceptionHelper.getStackDepthFun
import org.scalatest.Suite.anErrorThatShouldCauseAnAbort
import scala.annotation.tailrec

/**
 * Trait that provides the <code>eventually</code> construct, which periodically retries executing
 * a passed by-name parameter, until it either succeeds or the configured timeout has been surpassed.
 *
 * <p>
 * The by-name parameter "succeeds" if it returns a result. It "fails" if it throws any exception that
 * would normally cause a test to fail. (These are any exceptions except <a href="TestPendingException"><code>TestPendingException</code></a> and
 * <code>Error</code>s listed in the
 * <a href="Suite.html#errorHandling">Treatment of <code>java.lang.Error</code>s</a> section of the
 * documentation of trait <code>Suite</code>.)
 * </p>
 *
 * <p>
 * For example, the following invocation of <code>eventually</code> would succeed (not throw an exception):
 * </p>
 *
 * <pre class="stHighlight">
 * val xs = 1 to 125
 * val it = xs.iterator
 * eventually { it.next should be (3) }
 * </pre>
 *
 * <p>
 * However, because the default timeout is one second, the following invocation of
 * <code>eventually</code> would ultimately produce a <code>TestFailedException</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * val xs = 1 to 125
 * val it = xs.iterator
 * eventually { Thread.sleep(999); it.next should be (110) }
 * </pre>
 *
 * <p>
 * Assuming the default configuration parameters, <code>timeout</code> 1000 milliseconds and <code>interval</code> 10 milliseconds,
 * were passed implicitly to <code>eventually</code>, the detail message of the thrown
 * <code>TestFailedException</code> would look like:
 * </p>
 *
 * <p>
 * <code>The code passed to eventually never returned normally. Attempted 2 times, sleeping 10 milliseconds between each attempt.</code>
 * </p>
 *
 * <a name="retryConfig"></a><h2>Configuration of <code>eventually</code></h2>
 *
 * <p>
 * The <code>eventually</code> methods of this trait can be flexibly configured.
 * The two configuration parameters for <code>eventually</code> along with their 
 * default values and meanings are described in the following table:
 * </p>
 *
 * <table style="border-collapse: collapse; border: 1px solid black">
 * <tr>
 * <th style="background-color: #CCCCCC; border-width: 1px; padding: 3px; text-align: center; border: 1px solid black">
 * <strong>Configuration Parameter</strong>
 * </th>
 * <th style="background-color: #CCCCCC; border-width: 1px; padding: 3px; text-align: center; border: 1px solid black">
 * <strong>Default Value</strong>
 * </th>
 * <th style="background-color: #CCCCCC; border-width: 1px; padding: 3px; text-align: center; border: 1px solid black">
 * <strong>Meaning</strong>
 * </th>
 * </tr>
 * <tr>
 * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
 * timeout
 * </td>
 * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
 * 1000
 * </td>
 * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">
 * the maximum amount of time in milliseconds to allow unsuccessful attempts before giving up and throwing <code>TestFailedException</code>
 * </td>
 * </tr>
 * <tr>
 * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
 * interval
 * </td>
 * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
 * 10
 * </td>
 * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">
 * the number of milliseconds to sleep between each attempt
 * </td>
 * </tr>
 * </table>
 *
* <p>
 * The <code>eventually</code> methods of trait <code>Eventually</code> each take an <code>RetryConfig</code>
 * object as an implicit parameter. This object provides values for the two configuration parameters. Trait
 * <code>Eventually</code> provides an implicit <code>val</code> named <code>retryConfig</code> with each
 * configuration parameter set to its default value. 
 * If you want to set one or more configuration parameters to a different value for all invocations of
 * <code>eventually</code> in a suite you can override this
 * val (or hide it, for example, if you are importing the members of the <code>Eventually</code> companion object rather
 * than mixing in the trait). For example, if
 * you always want the default <code>timeout</code> to be 2 seconds and the default <code>interval</code> to be 5 milliseconds, you
 * can override <code>retryConfig</code>, like this:
 *
 * <pre class="stHighlight">
 * implicit override val retryConfig =
 *   RetryConfig(timeout = 2000, interval = 5)
 * </pre>
 *
 * <p>
 * Or, hide it by declaring a variable of the same name in whatever scope you want the changed values to be in effect:
 * </p>
 *
 * <pre class="stHighlight">
 * implicit val retryConfig =
 *   RetryConfig(timeout = 2000, interval = 5)
 * </pre>
 *
 * <p>
 * In addition to taking a <code>RetryConfig</code> object as an implicit parameter, the <code>eventually</code> methods of trait
 * <code>Eventually</code> include overloaded forms that take one or two <code>RetryConfigParam</code>
 * objects that you can use to override the values provided by the implicit <code>RetryConfig</code> for a single <code>eventually</code>
 * invocation. For example, if you want to set <code>timeout</code> to 5000 for just one particular <code>eventually</code> invocation,
 * you can do so like this:
 * </p>
 *
 * <pre class="stHighlight">
 * eventually (timeout(5000)) { Thread.sleep(10); it.next should be (110) }
 * </pre>
 *
 * <p>
 * This invocation of <code>eventually</code> will use 5000 for <code>timeout</code> and whatever value is specified by the 
 * implicitly passed <code>RetryConfig</code> object for the <code>interval</code> configuration parameter.
 * If you want to set both configuration parameters in this way, just list them separated by commas:
 * </p>
 * 
 * <pre class="stHighlight">
 * eventually (timeout(5000), interval(5)) { it.next should be (110) }
 * </pre>
 *
 * @author Bill Venners
 * @author Chua Chee Seng
 */
trait Eventually extends RetryConfiguration {

  /**
   * Invokes the passed by-name parameter repeatedly until it either succeeds, or a configured maximum
   * amount of time has passed, sleeping a configured interval between attempts.
   *
   * <p>
   * The by-name parameter "succeeds" if it returns a result. It "fails" if it throws any exception that
   * would normally cause a test to fail. (These are any exceptions except <a href="TestPendingException"><code>TestPendingException</code></a> and
   * <code>Error</code>s listed in the
   * <a href="Suite.html#errorHandling">Treatment of <code>java.lang.Error</code>s</a> section of the
   * documentation of trait <code>Suite</code>.)
   * </p>
   *
   * <p>
   * The maximum amount of time in milliseconds to tolerate unsuccessful attempts before giving up and throwing
   * <code>TestFailedException</code> is configured by the value contained in the passed
   * <code>timeout</code> parameter.
   * The interval to sleep between attempts is configured by the value contained in the passed
   * <code>interval</code> parameter.
   * </p>
   *
   * @param timeout the <code>Timeout</code> configuration parameter
   * @param interval the <code>Interval</code> configuration parameter
   * @param fun the by-name parameter to repeatedly invoke
   * @param config an <code>RetryConfig</code> object containing <code>timeout</code> and
   *          <code>interval</code> parameters that are unused by this method
   * @return the result of invoking the <code>fun</code> by-name parameter, the first time it succeeds
   */
  def eventually[T](timeout: Timeout, interval: Interval)(fun: => T)(implicit config: RetryConfig): T =
    eventually(fun)(RetryConfig(timeout.value, interval.value))

  /**
   * Invokes the passed by-name parameter repeatedly until it either succeeds, or a configured maximum
   * amount of time has passed, sleeping a configured interval between attempts.
   *
   * <p>
   * The by-name parameter "succeeds" if it returns a result. It "fails" if it throws any exception that
   * would normally cause a test to fail. (These are any exceptions except <a href="TestPendingException"><code>TestPendingException</code></a> and
   * <code>Error</code>s listed in the
   * <a href="Suite.html#errorHandling">Treatment of <code>java.lang.Error</code>s</a> section of the
   * documentation of trait <code>Suite</code>.)
   * </p>
   *
   * <p>
   * The maximum amount of time in milliseconds to tolerate unsuccessful attempts before giving up and throwing
   * <code>TestFailedException</code> is configured by the value contained in the passed
   * <code>timeout</code> parameter.
   * The interval to sleep between attempts is configured by the value contained in the passed
   * <code>interval</code> parameter.
   * </p>
   *
   * @param interval the <code>Interval</code> configuration parameter
   * @param timeout the <code>Timeout</code> configuration parameter
   * @param fun the by-name parameter to repeatedly invoke
   * @param config an <code>RetryConfig</code> object containing <code>timeout</code> and
   *          <code>interval</code> parameters that are unused by this method
   * @return the result of invoking the <code>fun</code> by-name parameter, the first time it succeeds
   */
  def eventually[T](interval: Interval, timeout: Timeout)(fun: => T)(implicit config: RetryConfig): T =
    eventually(fun)(RetryConfig(timeout.value, interval.value))

  /**
   * Invokes the passed by-name parameter repeatedly until it either succeeds, or a configured maximum
   * amount of time has passed, sleeping a configured interval between attempts.
   *
   * <p>
   * The by-name parameter "succeeds" if it returns a result. It "fails" if it throws any exception that
   * would normally cause a test to fail. (These are any exceptions except <a href="TestPendingException"><code>TestPendingException</code></a> and
   * <code>Error</code>s listed in the
   * <a href="Suite.html#errorHandling">Treatment of <code>java.lang.Error</code>s</a> section of the
   * documentation of trait <code>Suite</code>.)
   * </p>
   *
   * <p>
   * The maximum amount of time in milliseconds to tolerate unsuccessful attempts before giving up and throwing
   * <code>TestFailedException</code> is configured by the value contained in the passed
   * <code>timeout</code> parameter.
   * The interval to sleep between attempts is configured by the <code>interval</code> field of
   * the <code>RetryConfig</code> passed implicitly as the last parameter.
   * </p>
   *
   * @param timeout the <code>Timeout</code> configuration parameter
   * @param fun the by-name parameter to repeatedly invoke
   * @param config the <code>RetryConfig</code> object containing the (unused) <code>timeout</code> and
   *          (used) <code>interval</code> parameters
   * @return the result of invoking the <code>fun</code> by-name parameter, the first time it succeeds
   */
  def eventually[T](timeout: Timeout)(fun: => T)(implicit config: RetryConfig): T =
    eventually(fun)(RetryConfig(timeout.value, config.interval))

  /**
   * Invokes the passed by-name parameter repeatedly until it either succeeds, or a configured maximum
   * amount of time has passed, sleeping a configured interval between attempts.
   *
   * <p>
   * The by-name parameter "succeeds" if it returns a result. It "fails" if it throws any exception that
   * would normally cause a test to fail. (These are any exceptions except <a href="TestPendingException"><code>TestPendingException</code></a> and
   * <code>Error</code>s listed in the
   * <a href="Suite.html#errorHandling">Treatment of <code>java.lang.Error</code>s</a> section of the
   * documentation of trait <code>Suite</code>.)
   * </p>
   *
   * <p>
   * The maximum amount of time in milliseconds to tolerate unsuccessful attempts before giving up is configured by the <code>timeout</code> field of
   * the <code>RetryConfig</code> passed implicitly as the last parameter.
   * The interval to sleep between attempts is configured by the value contained in the passed
   * <code>interval</code> parameter.
   * </p>
   *
   * @param interval the <code>Interval</code> configuration parameter
   * @param fun the by-name parameter to repeatedly invoke
   * @param config the <code>RetryConfig</code> object containing the (used) <code>timeout</code> and
   *          (unused) <code>interval</code> parameters
   * @return the result of invoking the <code>fun</code> by-name parameter, the first time it succeeds
   */
  def eventually[T](interval: Interval)(fun: => T)(implicit config: RetryConfig): T =
    eventually(fun)(RetryConfig(config.timeout, interval.value))

  /**
   * Invokes the passed by-name parameter repeatedly until it either succeeds, or a configured maximum
   * amount of time has passed, sleeping a configured interval between attempts.
   *
   * <p>
   * The by-name parameter "succeeds" if it returns a result. It "fails" if it throws any exception that
   * would normally cause a test to fail. (These are any exceptions except <a href="TestPendingException"><code>TestPendingException</code></a> and
   * <code>Error</code>s listed in the
   * <a href="Suite.html#errorHandling">Treatment of <code>java.lang.Error</code>s</a> section of the
   * documentation of trait <code>Suite</code>.)
   * </p>
   *
   * <p>
   * The maximum amount of time in milliseconds to tolerate unsuccessful attempts before giving up is configured by the <code>timeout</code> field of
   * the <code>RetryConfig</code> passed implicitly as the last parameter.
   * The interval to sleep between attempts is configured by the <code>interval</code> field of
   * the <code>RetryConfig</code> passed implicitly as the last parameter.
   * </p>
   *
   * @param fun the by-name parameter to repeatedly invoke
   * @param config the <code>RetryConfig</code> object containing the <code>timeout</code> and
   *          <code>interval</code> parameters
   * @return the result of invoking the <code>fun</code> by-name parameter, the first time it succeeds
   */
  def eventually[T](fun: => T)(implicit config: RetryConfig): T = {
    val startMillis = System.currentTimeMillis
    def makeAValiantAttempt(): Either[Throwable, T] = {
      try {
        Right(fun)
      }
      catch {
        case tpe: TestPendingException => throw tpe
        case e: Throwable if !anErrorThatShouldCauseAnAbort(e) => Left(e)
      }
    }

    @tailrec
    def tryTryAgain(attempt: Int): T = {
      val timeout = config.timeout
      val interval = config.interval
      makeAValiantAttempt() match {
        case Right(result) => result
        case Left(e) => 
          val duration = System.currentTimeMillis - startMillis
          if (duration < timeout)
            Thread.sleep(interval)
          else {
            def msg =
              if (e.getMessage == null)
                Resources("didNotEventuallySucceed", attempt.toString, interval.toString)
              else
                Resources("didNotEventuallySucceedBecause", attempt.toString, interval.toString, e.getMessage)
            throw new TestFailedException(
              sde => Some(msg),
              Some(e),
              getStackDepthFun("Eventually.scala", "eventually")
            )
          }

          tryTryAgain(attempt + 1)
      }
    }
    tryTryAgain(1)
  }
}

/**
 * Companion object that facilitates the importing of <code>Eventually</code> members as 
 * an alternative to mixing in the trait. One use case is to import <code>Eventually</code>'s members so you can use
 * them in the Scala interpreter:
 *
 * <pre class="stREPL">
 * $ scala -cp scalatest-1.8.jar
 * Welcome to Scala version 2.9.1.final (Java HotSpot(TM) 64-Bit Server VM, Java 1.6.0_29).
 * Type in expressions to have them evaluated.
 * Type :help for more information.
 *
 * scala&gt; import org.scalatest._
 * import org.scalatest._
 *
 * scala&gt; import matchers.ShouldMatchers._
 * import matchers.ShouldMatchers._
 *
 * scala&gt; import concurrent.Eventually._
 * import concurrent.Eventually._
 *
 * scala&gt; val xs = 1 to 125
 * xs: scala.collection.immutable.Range.Inclusive = Range(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, ..., 125)
 * 
 * scala&gt; val it = xs.iterator
 * it: Iterator[Int] = non-empty iterator
 *
 * scala&gt; eventually { it.next should be (3) }
 *
 * scala&gt; eventually { Thread.sleep(999); it.next should be (3) }
 * org.scalatest.TestFailedException: The code passed to eventually never returned normally.
 *     Attempted 2 times, sleeping 10 milliseconds between each attempt.
 *   at org.scalatest.Eventually$class.tryTryAgain$1(Eventually.scala:313)
 *   at org.scalatest.Eventually$class.eventually(Eventually.scala:322)
 *   ...
 * </pre>
 */
object Eventually extends Eventually

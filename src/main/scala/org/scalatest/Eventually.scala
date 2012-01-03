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
package org.scalatest

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
 * a passed by-name parameter, until it either succeeds or the configured maximum number of failed attempts is  
 * exhausted.
 *
 * <p>
 * The by-name parameter "succeeds" if it returns a result. It "fails" if it throws any exception that
 * would normally cause a test to fail. (These are any exceptions except those listed in the
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
 * However, because the default number of attempts is 100, the following invocation of
 * <code>eventually</code> would ultimately produce a <code>TestFailedException</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * val xs = 1 to 125
 * val it = xs.iterator
 * eventually { it.next should be (110) }
 * </pre>
 *
 * <p>
 * Assuming the default configuration parameters, <code>maxAttempts</code> 100 and <code>interval</code> 10 milliseconds,
 * were passed implicitly to <code>eventually</code>, the detail message of the thrown
 * <code>TestFailedException</code> would be:
 * </p>
 *
 * <p>
 * <code>The code passed to eventually never returned normally. Attempted 100 times, sleeping 10 milliseconds between each attempt.</code>
 * </p>
 *
 */
trait Eventually {

  /**
   * Configuration object for the <code>eventually</code> construct.
   *
   * <p>
   * The default values for the parameters are:
   * </p>
   *
   * <table style="border-collapse: collapse; border: 1px solid black">
   * <tr>
   * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
   * maxAttempts
   * </td>
   * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
   * 100
   * </td>
   * </tr>
   * <tr>
   * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
   * interval
   * </td>
   * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
   * 10
   * </td>
   * </tr>
   * </table>
   *
   * @param maxAttempts the maximum number of unsuccessful attempts before giving up and throwing
   *   <code>TestFailedException</code>.
   * @param interval the number of milliseconds to sleep between each attempt
   * @throws IllegalArgumentException if the specified <code>maxAttempts</code> value is less than or equal to zero,
   *   the specified <code>interval</code> value is less than zero.
   *
   * @author Bill Venners
   * @author Chua Chee Seng
   */
  case class EventuallyConfig(maxAttempts: Int = 100, interval: Int = 10) {
    require(maxAttempts > 0, "maxAttempts had value " + maxAttempts + ", but must be greater than zero")
    require(interval >= 0, "interval had value " + interval + ", but must be greater than or equal to zero")
  }

  /**
   * Abstract class defining a family of configuration parameters for the <code>eventually</code> construct.
   * 
   * <p>
   * The subclasses of this abstract class are used to pass configuration information to
   * the <code>eventually</code> methods of trait <code>Eventually</code>.
   * </p>
   *
   * @author Bill Venners
   * @author Chua Chee Seng
   */
  sealed abstract class EventuallyConfigParam

  /**
   * An <code>EventuallyConfigParam</code> that specifies the maximum number of times to invoke the
   * by-name parameter passed to <code>eventually</code> with an unsucessful result.
   *
   * @param value the maximum number of unsuccessful attempts before giving up and throwing
   *   <code>TestFailedException</code>.
   * @throws IllegalArgumentException if specified <code>value</code> is less than or equal to zero.
   *
   * @author Bill Venners
   */
  case class MaxAttempts(value: Int) extends EventuallyConfigParam {
    require(value > 0, "The passed value, " + value + ", was not greater than zero")
  }

  /**
   * An <code>EventuallyConfigParam</code> that specifies the number of milliseconds to sleep after
   * each unsuccessful invocation of the by-name parameter passed to <code>eventually</code>.
   *
   * @param value the number of milliseconds to sleep between each attempt
   * @throws IllegalArgumentException if specified <code>value</code> is less than or equal to zero.
   *
   * @author Bill Venners
   */
  case class Interval(value: Int) extends EventuallyConfigParam {
    require(value >= 0, "The passed value, " + value + ", was not greater than or equal to zero")
  }

  /**
   * Implicit <code>EventuallyConfig</code> value providing default configuration values.
   *
   * <p>
   * To change the default configuration, override or hide this <code>val</code> with another implicit
   * <code>EventuallyConfig</code> containing your desired default configuration values.
   * </p>
   */
  implicit val eventuallyConfig = EventuallyConfig()

  /**
   * Returns a <code>MaxAttempts</code> configuration parameter containing the passed value, which
   * specifies the maximum number of times to invoke the
   * by-name parameter passed to <code>eventually</code> with an unsucessful result.
   *
   * @throws IllegalArgumentException if specified <code>value</code> is less than or equal to zero.
   */
  def maxAttempts(value: Int) = MaxAttempts(value)

  /**
   * Returns an <code>Interval</code> configuration parameter containing the passed value, which
   * specifies the number of milliseconds to sleep after
   * each unsuccessful invocation of the by-name parameter passed to <code>eventually</code>.
   *
   * @throws IllegalArgumentException if specified <code>value</code> is less than or equal to zero.
   */
  def interval(value: Int) = Interval(value)

  /**
   * Invokes the passed by-name parameter repeatedly until it either succeeds, or fails a configured maximum
   * number of times, sleeping a configured interval between attempts.
   *
   * <p>
   * The by-name parameter "succeeds" if it returns a result. It "fails" if it throws any exception that
   * would normally cause a test to fail. (These are any exceptions except those listed in the
   * <a href="Suite.html#errorHandling">Treatment of <code>java.lang.Error</code>s</a> section of the
   * documentation of trait <code>Suite</code>.)
   * </p>
   *
   * <p>
   * The maximum attempts to make before giving up is configured by the value contained in the passed
   * <code>maxAttempts</code> parameter.
   * The interval to sleep between attempts is configured by the value contained in the passed
   * <code>interval</code> parameter.
   * </p>
   *
   * @param maxAttempts the <code>MaxAttempts</code> configuration parameter
   * @param interval the <code>Interval</code> configuration parameter
   * @param fun the by-name parameter to repeatedly invoke
   * @param config an <code>EventuallyConfig</code> object containing <code>maxAttempts</code> and
   *          <code>interval</code> parameters that are unused by this method
   * @return the result of invoking the <code>fun</code> by-name parameter, the first time it succeeds
   */
  def eventually[T](maxAttempts: MaxAttempts, interval: Interval)(fun: => T)(implicit config: EventuallyConfig): T =
    eventually(fun)(EventuallyConfig(maxAttempts.value, interval.value))

  /**
   * Invokes the passed by-name parameter repeatedly until it either succeeds, or fails a configured maximum
   * number of times, sleeping a configured interval between attempts.
   *
   * <p>
   * The by-name parameter "succeeds" if it returns a result. It "fails" if it throws any exception that
   * would normally cause a test to fail. (These are any exceptions except those listed in the
   * <a href="Suite.html#errorHandling">Treatment of <code>java.lang.Error</code>s</a> section of the
   * documentation of trait <code>Suite</code>.)
   * </p>
   *
   * <p>
   * The maximum attempts to make before giving up is configured by the value contained in the passed
   * <code>maxAttempts</code> parameter.
   * The interval to sleep between attempts is configured by the value contained in the passed
   * <code>interval</code> parameter.
   * </p>
   *
   * @param interval the <code>Interval</code> configuration parameter
   * @param maxAttempts the <code>MaxAttempts</code> configuration parameter
   * @param fun the by-name parameter to repeatedly invoke
   * @param config an <code>EventuallyConfig</code> object containing <code>maxAttempts</code> and
   *          <code>interval</code> parameters that are unused by this method
   * @return the result of invoking the <code>fun</code> by-name parameter, the first time it succeeds
   */
  def eventually[T](interval: Interval, maxAttempts: MaxAttempts)(fun: => T)(implicit config: EventuallyConfig): T =
    eventually(fun)(EventuallyConfig(maxAttempts.value, interval.value))

  /**
   * Invokes the passed by-name parameter repeatedly until it either succeeds, or fails a configured maximum
   * number of times, sleeping a configured interval between attempts.
   *
   * <p>
   * The by-name parameter "succeeds" if it returns a result. It "fails" if it throws any exception that
   * would normally cause a test to fail. (These are any exceptions except those listed in the
   * <a href="Suite.html#errorHandling">Treatment of <code>java.lang.Error</code>s</a> section of the
   * documentation of trait <code>Suite</code>.)
   * </p>
   *
   * <p>
   * The maximum attempts to make before giving up is configured by the value contained in the passed
   * <code>maxAttempts</code> parameter.
   * The interval to sleep between attempts is configured by the <code>interval</code> field of
   * the <code>EventuallyConfig</code> passed implicitly as the last parameter.
   * </p>
   *
   * @param maxAttempts the <code>MaxAttempts</code> configuration parameter
   * @param fun the by-name parameter to repeatedly invoke
   * @param config the <code>EventuallyConfig</code> object containing the (unused) <code>maxAttempts</code> and
   *          (used) <code>interval</code> parameters
   * @return the result of invoking the <code>fun</code> by-name parameter, the first time it succeeds
   */
  def eventually[T](maxAttempts: MaxAttempts)(fun: => T)(implicit config: EventuallyConfig): T =
    eventually(fun)(EventuallyConfig(maxAttempts.value, config.interval))

  /**
   * Invokes the passed by-name parameter repeatedly until it either succeeds, or fails a configured maximum
   * number of times, sleeping a configured interval between attempts.
   *
   * <p>
   * The by-name parameter "succeeds" if it returns a result. It "fails" if it throws any exception that
   * would normally cause a test to fail. (These are any exceptions except those listed in the
   * <a href="Suite.html#errorHandling">Treatment of <code>java.lang.Error</code>s</a> section of the
   * documentation of trait <code>Suite</code>.)
   * </p>
   *
   * <p>
   * The maximum attempts to make before giving up is configured by the <code>maxAttempts</code> field of
   * the <code>EventuallyConfig</code> passed implicitly as the last parameter.
   * The interval to sleep between attempts is configured by the value contained in the passed
   * <code>interval</code> parameter.
   * </p>
   *
   * @param interval the <code>Interval</code> configuration parameter
   * @param fun the by-name parameter to repeatedly invoke
   * @param config the <code>EventuallyConfig</code> object containing the (used) <code>maxAttempts</code> and
   *          (unused) <code>interval</code> parameters
   * @return the result of invoking the <code>fun</code> by-name parameter, the first time it succeeds
   */
  def eventually[T](interval: Interval)(fun: => T)(implicit config: EventuallyConfig): T =
    eventually(fun)(EventuallyConfig(config.maxAttempts, interval.value))

  /**
   * Invokes the passed by-name parameter repeatedly until it either succeeds, or fails a configured maximum
   * number of times, sleeping a configured interval between attempts.
   *
   * <p>
   * The by-name parameter "succeeds" if it returns a result. It "fails" if it throws any exception that
   * would normally cause a test to fail. (These are any exceptions except those listed in the
   * <a href="Suite.html#errorHandling">Treatment of <code>java.lang.Error</code>s</a> section of the
   * documentation of trait <code>Suite</code>.)
   * </p>
   *
   * <p>
   * The maximum attempts to make before giving up is configured by the <code>maxAttempts</code> field of
   * the <code>EventuallyConfig</code> passed implicitly as the last parameter.
   * The interval to sleep between attempts is configured by the <code>interval</code> field of
   * the <code>EventuallyConfig</code> passed implicitly as the last parameter.
   * </p>
   *
   * @param fun the by-name parameter to repeatedly invoke
   * @param config the <code>EventuallyConfig</code> object containing the <code>maxAttempts</code> and
   *          <code>interval</code> parameters
   * @return the result of invoking the <code>fun</code> by-name parameter, the first time it succeeds
   */
  def eventually[T](fun: => T)(implicit config: EventuallyConfig): T = {

    def makeAValiantAttempt(): Either[Throwable, T] = {
      try {
        Right(fun)
      }
      catch {
        case e: Throwable if !anErrorThatShouldCauseAnAbort(e) => Left(e)
      }
    }

    @tailrec
    def tryTryAgain(attempt: Int): T = {
      val maxAttempts = config.maxAttempts
      val interval = config.interval
      makeAValiantAttempt() match {
        case Right(result) => result
        case Left(e) => 
          if (attempt < maxAttempts)
            Thread.sleep(interval)
          else
            throw new TestFailedException(
              sde => Some(Resources("didNotEventuallySucceeded", maxAttempts.toString, interval.toString)),
              Some(e),
              getStackDepthFun("Eventually.scala", "eventually")
            )

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
 * scala&gt; import Eventually._
 * import Eventually._
 *
 * scala&gt; val xs = 1 to 125
 * xs: scala.collection.immutable.Range.Inclusive = Range(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125)
 * 
 * scala&gt; val it = xs.iterator
 * it: Iterator[Int] = non-empty iterator
 *
 * scala&gt; eventually { it.next should be (3) }
 *
 * scala&gt; eventually { it.next should be (110) }
 * org.scalatest.TestFailedException: The code passed to eventually never returned normally. Attempted 100 times, sleeping 10 milliseconds between each attempt.
 *   at org.scalatest.Eventually$class.tryTryAgain$1(Eventually.scala:313)
 *   at org.scalatest.Eventually$class.eventually(Eventually.scala:322)
 *   ...
 * </pre>
 */
object Eventually extends Eventually

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

  def eventually[T](maxAttempts: MaxAttempts, interval: Interval)(fun: => T)(implicit config: EventuallyConfig): T =
    eventually(fun)(EventuallyConfig(maxAttempts.value, interval.value))

  def eventually[T](interval: Interval, maxAttempts: MaxAttempts)(fun: => T)(implicit config: EventuallyConfig): T =
    eventually(fun)(EventuallyConfig(maxAttempts.value, interval.value))

  def eventually[T](maxAttempts: MaxAttempts)(fun: => T)(implicit config: EventuallyConfig): T =
    eventually(fun)(EventuallyConfig(maxAttempts.value, config.interval))

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
      makeAValiantAttempt() match {
        case Right(result) => result
        case Left(e) => 
          if (attempt < config.maxAttempts)
            Thread.sleep(config.interval)
          else
            throw new TestFailedException(
              sde => Some(Resources("eventuallyNotReturn")),
              Some(e),
              getStackDepthFun("Eventually.scala", "eventually")
            )

          tryTryAgain(attempt + 1)
      }
    }
    tryTryAgain(1)
  }
}

object Eventually extends Eventually

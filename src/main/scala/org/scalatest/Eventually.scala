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

/**
 * Trait that provides the <code>eventually</code> construct, which periodically retries executing
 * a passed by-name parameter, until it either succeeds or the configured maximum number of attempts is  
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

  def eventually[T](maxAttempts: MaxAttempts, interval: Interval)(f: => T)(implicit config: EventuallyConfig): T =
    eventually(f)(EventuallyConfig(maxAttempts.value, interval.value))

  def eventually[T](interval: Interval, maxAttempts: MaxAttempts)(f: => T)(implicit config: EventuallyConfig): T =
    eventually(f)(EventuallyConfig(maxAttempts.value, interval.value))

  def eventually[T](maxAttempts: MaxAttempts)(f: => T)(implicit config: EventuallyConfig): T =
    eventually(f)(EventuallyConfig(maxAttempts.value, config.interval))

  def eventually[T](interval: Interval)(f: => T)(implicit config: EventuallyConfig): T =
    eventually(f)(EventuallyConfig(config.maxAttempts, interval.value))

  def eventually[T](f: => T)(implicit config: EventuallyConfig): T = {
    val maxAttempts = config.maxAttempts
    val interval = config.interval
    var count = 0
    var cause: Throwable = null
    while (count < maxAttempts) {
      try {
        return f
      }
      catch {
        case e: Throwable if !anErrorThatShouldCauseAnAbort(e) =>
          count += 1
          cause = e
      }
      Thread.sleep(interval)
    }
    throw new TestFailedException(sde => Some(Resources("eventuallyNotReturn")), Some(cause), getStackDepthFun("Eventually.scala", "eventually"))
  }
}

object Eventually extends Eventually

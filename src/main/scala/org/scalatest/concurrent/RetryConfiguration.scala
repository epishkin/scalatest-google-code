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
 * Trait providing methods and classes used to configure retries performed by the
 * the <code>eventually</code> methods of trait <a href="Eventually.html"><code>Eventually</code></a>
 * and the <code>whenReady</code> methods of trait <a href="WhenReady.html"><code>WhenReady</code></a>.
 *
 * @author Bill Venners
 */
trait RetryConfiguration {

  /**
   * Configuration object for traits <code>Eventually</code> and <code>WhenReady</code>.
   *
   * <p>
   * The default values for the parameters are:
   * </p>
   *
   * <table style="border-collapse: collapse; border: 1px solid black">
   * <tr>
   * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
   * timeout
   * </td>
   * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
   * 1000
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
   * @param timeout the maximum amount of time in milliseconds to retry before giving up and throwing
   *   <code>TestFailedException</code>.
   * @param interval the number of milliseconds to sleep between each attempt
   * @throws IllegalArgumentException if the specified <code>timeout</code> value is less than or equal to zero,
   *   the specified <code>interval</code> value is less than zero.
   *
   * @author Bill Venners
   * @author Chua Chee Seng
   */
  final case class RetryConfig(timeout: Int = 1000, interval: Int = 10) {
    require(timeout > 0, "timeout had value " + timeout + ", but must be greater than zero")
    require(interval >= 0, "interval had value " + interval + ", but must be greater than or equal to zero")
  }

  /**
   * Abstract class defining a family of configuration parameters for traits <code>Eventually</code> and <code>WhenReady</code>.
   * 
   * <p>
   * The subclasses of this abstract class are used to pass configuration information to
   * the <code>eventually</code> methods of trait <code>Eventually</code> and the <code>whenReady</code> method of trait <code>WhenReady</code>.
   * </p>
   *
   * @author Bill Venners
   * @author Chua Chee Seng
   */
  sealed abstract class RetryConfigParam

  /**
   * A <code>RetryConfigParam</code> that specifies the maximum amount of time in milliseconds to allow retries: either invocations of the
   * by-name parameter passed to <code>eventually</code> that give an unsucessful result, or futures passed to <code>whenReady</code> that
   * are not ready, canceled, or expired.
   *
   * @param value the maximum amount of time in milliseconds to retry before giving up and throwing
   *   <code>TestFailedException</code>.
   * @throws IllegalArgumentException if specified <code>value</code> is less than or equal to zero.
   *
   * @author Bill Venners
   */
  case class Timeout(value: Int) extends RetryConfigParam {
    require(value > 0, "The passed value, " + value + ", was not greater than zero")
  }

  /**
   * A <code>RetryConfigParam</code> that specifies the number of milliseconds to sleep after
   * each retry: each unsuccessful invocation of the by-name parameter passed to <code>eventually</code> or
   * each query of a future passed to <code>whenReady</code>.
   *
   * @param value the number of milliseconds to sleep between each attempt
   * @throws IllegalArgumentException if specified <code>value</code> is less than or equal to zero.
   *
   * @author Bill Venners
   */
  case class Interval(value: Int) extends RetryConfigParam {
    require(value >= 0, "The passed value, " + value + ", was not greater than or equal to zero")
  }

  /**
   * Implicit <code>RetryConfig</code> value providing default configuration values.
   *
   * <p>
   * To change the default configuration, override or hide this <code>val</code> with another implicit
   * <code>RetryConfig</code> containing your desired default configuration values.
   * </p>
   */
  implicit val retryConfig = RetryConfig()

  /**
   * Returns a <code>Timeout</code> configuration parameter containing the passed value, which
   * specifies the maximum amount of time in milliseconds to retry: to allow invocations of the
   * by-name parameter passed to <code>eventually</code> to give an unsucessful result, or to
   * allow a future passed to <code>whenReady</code> to not be ready, canceled, or expired.
   *
   * @throws IllegalArgumentException if specified <code>value</code> is less than or equal to zero.
   */
  def timeout(value: Int) = Timeout(value)

  /**
   * Returns an <code>Interval</code> configuration parameter containing the passed value, which
   * specifies the number of milliseconds to sleep after
   * each retry: each unsuccessful invocation of the by-name parameter passed to <code>eventually</code>
   * or each query of a non-ready, canceled, or expired future passed to <code>whenReady</code>.
   *
   * @throws IllegalArgumentException if specified <code>value</code> is less than or equal to zero.
   */
  def interval(value: Int) = Interval(value)
}

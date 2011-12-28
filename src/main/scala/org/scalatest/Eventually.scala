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

private[scalatest] trait Eventually {

  case class EventuallyConfig(maxAttempts: Int = 100, interval: Int = 10)
  class MaxAttemptsConfig(maxAttempts: Int, interval: Int) extends EventuallyConfig(maxAttempts = maxAttempts)
  class IntervalConfig(maxAttempts: Int, interval: Int) extends EventuallyConfig(interval = interval)

  implicit val eventuallyConfig = EventuallyConfig()

  def maxAttempts(value: Int)(implicit config: EventuallyConfig) = new MaxAttemptsConfig(maxAttempts = value, interval = config.interval)

  def interval(value: Int)(implicit config: EventuallyConfig) = new IntervalConfig(maxAttempts = config.maxAttempts, interval = value)
  
  def eventually[T](maxAttempts: MaxAttemptsConfig, interval: IntervalConfig)(f: => T): T = eventually(f)(new EventuallyConfig(maxAttempts.maxAttempts, interval.interval))
  def eventually[T](interval: IntervalConfig, maxAttempts: MaxAttemptsConfig)(f: => T): T = eventually(maxAttempts, interval)(f)
  def eventually[T](maxAttempts: MaxAttemptsConfig)(f: => T): T = eventually(f)(new EventuallyConfig(maxAttempts.maxAttempts, eventuallyConfig.interval))
  def eventually[T](interval: IntervalConfig)(f: => T): T = eventually(f)(new EventuallyConfig(eventuallyConfig.maxAttempts, interval.interval))

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

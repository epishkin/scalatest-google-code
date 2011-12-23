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

// TODO: Make this private[scalatest]
trait Eventually {

  case class EventuallyConfig(maxAttempts: Int = 100, interval: Int = 10)
  class MaxAttemptsConfig(maxAttempts: Int, interval: Int) extends EventuallyConfig(maxAttempts = maxAttempts)
  class IntervalConfig(maxAttempts: Int, interval: Int) extends EventuallyConfig(interval = interval)

  implicit val eventuallyConfig = EventuallyConfig()

  def maxAttempts(value: Int)(implicit config: EventuallyConfig) = new MaxAttemptsConfig(maxAttempts = value, interval = config.interval)

  def interval(value: Int)(implicit config: EventuallyConfig) = new IntervalConfig(maxAttempts = config.maxAttempts, interval = value)

/*
This compiled but is ugly. I think config should be at the end in this case.
  def eventually[T](config: EventuallyConfig)(f: => T): T = eventually(f)(config)
*/
  implicit def uglyConversion(pair: (MaxAttemptsConfig, IntervalConfig)): EventuallyConfig = {
    val (maxAttemptsConfig, intervalConfig) = pair
    EventuallyConfig(maxAttemptsConfig.maxAttempts, intervalConfig.interval)
  }

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
    // TODO: Get string from resource file
    // TODO: Verify and possibly be smarter about stack depth
    throw new TestFailedException("The code passed to eventually never returned normally.", cause, 2)
  }

  // TODO: Use the one in the Suite singleton object
  // Another TODO: I think that this should work:
  // case _: AnnotationFormatError |
  //     AWTError |
  //     CoderMalfunctionError |
  //     FactoryConfigurationError |
  //     LinkageError |
  //     ThreadDeath |
  //     TransformerFactoryConfigurationError |
  //     VirtualMachineError => true
  // Try it, and if it works, use it. (Better make sure there are tests for all these at that time too.)
  private def anErrorThatShouldCauseAnAbort(throwable: Throwable) =
    throwable match {
      case _: AnnotationFormatError => true
      case _: AWTError => true
      case _: CoderMalfunctionError => true
      case _: FactoryConfigurationError => true
      case _: LinkageError => true
      case _: ThreadDeath => true
      case _: TransformerFactoryConfigurationError => true
      case _: VirtualMachineError => true
      case _ => false
    }

}

object Eventually extends Eventually

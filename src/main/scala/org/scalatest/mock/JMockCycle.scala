/*
 * Copyright 2001-2009 Artima, Inc.
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
package org.scalatest.mock

import org.scalatest._
import org.jmock.api.ExpectationError
import org.jmock.{Expectations, Mockery}
import scala.reflect.Manifest

class JMockCycle {

  private val context = new Mockery

  /**
   *
   * <pre>
   * val context = new Mockery
   * val reporter = context.mock(classOf[Reporter])
   * </pre>
   *
   * <pre>
   * val reporter = mock[Reporter]
   * </pre>
   */
  def mock[T <: AnyRef](implicit manifest: Manifest[T]): T = {
    context.mock(manifest.erasure.asInstanceOf[Class[T]])
  }

  /**
   * <pre>
   * context.checking(
   *   new Expectations() {
   *     oneOf (reporter).apply(`with`(new IsAnything[SuiteStarting]))
   *     oneOf (reporter).apply(`with`(new IsAnything[TestStarting]))
   *     oneOf (reporter).apply(`with`(new IsAnything[TestSucceeded]))
   *     oneOf (reporter).apply(`with`(new IsAnything[SuiteCompleted]))
   *    }
   *  )
   * </pre>
   *
   * <pre>
   * expecting { e => import e._
   *   oneOf (reporter).apply(`with`(new IsAnything[SuiteStarting]))
   *   oneOf (reporter).apply(`with`(new IsAnything[TestStarting]))
   *   oneOf (reporter).apply(`with`(new IsAnything[TestSucceeded]))
   *   oneOf (reporter).apply(`with`(new IsAnything[SuiteCompleted]))
   * }
   * </pre>
   */
  def expecting(expectationsFunction: Expectations => Unit) {
    val e = new Expectations
    expectationsFunction(e)
    context.checking(e)
  }

  /**
   * <pre>
   * (new SuccessTestNGSuite()).runTestNG(reporter, new Tracker)
   *
   * context.assertIsSatisfied()
   * </pre>
   *
   * <pre>
   * whenExecuting {
   *   (new SuccessTestNGSuite()).runTestNG(reporter, new Tracker)
   * }
   * </pre>
   */
  def whenExecuting(f: => Unit) = {
    try {
      f
      context.assertIsSatisfied()
    }
    catch {
      case ee: ExpectationError =>
        val ae = new AssertionError(ee.toString)
        ae.initCause(ee)
        throw ae
    }
  }
}

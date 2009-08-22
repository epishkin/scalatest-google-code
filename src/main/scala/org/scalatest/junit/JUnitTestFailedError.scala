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
package org.scalatest.junit

import org.scalatest.StackDepth
import _root_.junit.framework.AssertionFailedError

class JUnitTestFailedError(val message: Option[String], val cause: Option[Throwable], val failedCodeStackDepth: Int)
    extends AssertionFailedError(if (message.isDefined) message.get else "") with StackDepth {

  if (message == null) throw new NullPointerException("message was null")
  message match {
    case Some(null) => throw new NullPointerException("message was a Some(null)")
    case _ =>
  }

  if (cause == null) throw new NullPointerException("cause was null")
  cause match {
    case Some(null) => throw new NullPointerException("cause was a Some(null)")
    case _ =>
  }

  if (cause.isDefined)
    super.initCause(cause.get)

  /*
  * Throws <code>IllegalStateException</code>, because <code>StackDepthException</code>s are
  * always initialized with a cause passed to the constructor of superclass <code>
  */
  override final def initCause(throwable: Throwable): Throwable = { throw new IllegalStateException }
  
  /**
   * Create a <code>JUnitTestFailedError</code> with specified stack depth and no detail message or cause.
   *
   * @param failedCodeStackDepth the depth in the stack trace of this exception at which the line of test code that failed resides.
   *
   */
  def this(failedCodeStackDepth: Int) = this(None, None, failedCodeStackDepth)

  /**
   * Create a <code>JUnitTestFailedError</code> with a specified stack depth and detail message.
   *
   * @param message A detail message for this <code>JUnitTestFailedError</code>.
   * @param failedCodeStackDepth the depth in the stack trace of this exception at which the line of test code that failed resides.
   *
   * @throws NullPointerException if <code>message</code> is <code>null</code>.
   */
  def this(message: String, failedCodeStackDepth: Int) =
    this(
      {
        if (message == null) throw new NullPointerException("message was null")
        Some(message)
      },
      None,
      failedCodeStackDepth
    )

  /**
   * Create a <code>JUnitTestFailedError</code> with the specified stack depth and cause.  The
   * <code>message</code> field of this exception object will be initialized to
   * <code>if (cause.getMessage == null) "" else cause.getMessage</code>.
   *
   * @param cause the cause, the <code>Throwable</code> that caused this <code>JUnitTestFailedError</code> to be thrown.
   * @param failedCodeStackDepth the depth in the stack trace of this exception at which the line of test code that failed resides.
   *
   * @throws NullPointerException if <code>cause</code> is <code>null</code>.
   */
  def this(cause: Throwable, failedCodeStackDepth: Int) =
    this(
      {
        if (cause == null) throw new NullPointerException("cause was null")
        Some(if (cause.getMessage == null) "" else cause.getMessage)
      },
      Some(cause),
      failedCodeStackDepth
    )

  /**
   * Create a <code>JUnitTestFailedError</code> with the specified stack depth, detail
   * message, and cause.
   *
   * <p>Note that the detail message associated with cause is
   * <em>not</em> automatically incorporated in this throwable's detail
   * message.
   *
   * @param message A detail message for this <code>JUnitTestFailedError</code>.
   * @param cause the cause, the <code>Throwable</code> that caused this <code>JUnitTestFailedError</code> to be thrown.
   * @param failedCodeStackDepth the depth in the stack trace of this exception at which the line of test code that failed resides.
   *
   * @throws NullPointerException if <code>message</code> is <code>null</code>.
   * @throws NullPointerException if <code>cause</code> is <code>null</code>.
   */
  def this(message: String, cause: Throwable, failedCodeStackDepth: Int) =
    this(
      {
        if (message == null) throw new NullPointerException("message was null")
        Some(message)
      },
      {
        if (cause == null) throw new NullPointerException("cause was null")
        Some(cause)
      },
      failedCodeStackDepth
    )
}

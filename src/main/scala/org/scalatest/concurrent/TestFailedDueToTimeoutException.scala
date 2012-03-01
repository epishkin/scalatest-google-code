/*
 * Copyright 2001-2012 Artima, Inc.
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

import org.scalatest.TestFailedException
import org.scalatest.StackDepthException

/**
 * Subclass of <a href="../TestFailedException.html"><code>TestFailedException</code></a> thrown by the <code>failAfter</code>
 * method of trait <a href="Timeouts.html"><code>Timeouts</code></a> if it times out.
 *
 * @param messageFun a function that produces an optional detail message for this <code>TestFailedDueToTimeoutException</code>.
 * @param cause an optional cause, the <code>Throwable</code> that caused this <code>TestFailedDueToTimeoutException</code> to be thrown.
 * @param failedCodeStackDepthFun a function that produces the depth in the stack trace of this exception at which the line of test code that failed resides.
 * @param timeout the timeout that expired
 *
 * @throws NullPointerException if either <code>messageFun</code>, <code>cause</code> or <code>failedCodeStackDepthFun</code> is <code>null</code>, or <code>Some(null)</code>.
 *
 * @author Bill Venners
 */
class TestFailedDueToTimeoutException(
  messageFun: StackDepthException => Option[String],
  cause: Option[Throwable],
  failedCodeStackDepthFun: StackDepthException => Int,
  val timeout: Long
) extends TestFailedException(messageFun, cause, failedCodeStackDepthFun) with TimeoutException {

  /**
   * Returns an instance of this exception's class, identical to this exception,
   * except with the detail message option string replaced with the result of passing
   * the current detail message to the passed function, <code>fun</code>.
   *
   * @param fun A function that, given the current optional detail message, will produce
   * the modified optional detail message for the result instance of <code>TestFailedDueToTimeoutException</code>.
   */
  override def modifyMessage(fun: Option[String] => Option[String]): TestFailedDueToTimeoutException = {
    val mod = new TestFailedDueToTimeoutException(sde => fun(message), cause, failedCodeStackDepthFun, timeout)
    mod.setStackTrace(getStackTrace)
    mod
  }
}

/*
Will need to add cancelAfter to the doc comment in 2.0.
*/


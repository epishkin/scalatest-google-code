/*
 * Copyright 2001-2008 Artima, Inc.
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
package prop

/**
 * Exception that indicates a property check failed.
 *
 * @param message a detail message, not optional) for this <code>PropertyCheckFailedException</code>.
 * @param cause an optional cause, the <code>Throwable</code> that caused this <code>PropertyCheckFailedException</code> to be thrown.
 * @param failedCodeStackDepth the depth in the stack trace of this exception at which the line of test code that failed resides.
 * @param undecoratedMessage just a short message that has no redundancy with args, labels, etc. The regular "message" has everything in it
 * @param args the argument values
 * @param args the argument values
 *
 * @throws NullPointerException if either <code>message</code> or <code>cause</code> is <code>null</code>, or <code>Some(null)</code>.
 *
 * @author Bill Venners
 */
class TablePropertyCheckFailedException(
  message: String,
  cause: Option[Throwable],
  failedCodeStackDepth: Int,
  undecoratedMessage: String,
  args: List[Any],
  namesOfArgs: List[String],
  val row: Int
) extends PropertyCheckFailedException(message, cause, failedCodeStackDepth, undecoratedMessage, args, Some(namesOfArgs)) {

  if (message == null) throw new NullPointerException("message was null")

  if (cause == null) throw new NullPointerException("cause was null")
  cause match {
    case Some(null) => throw new NullPointerException("cause was a Some(null)")
    case _ =>
  }
}


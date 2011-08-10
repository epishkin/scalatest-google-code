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

import events.NameInfo
import java.util.concurrent.atomic.AtomicReference

private[scalatest] abstract class ThreadAwareDocumenter extends Documenter {

  private final val atomic = new AtomicReference[Thread](Thread.currentThread)

  def isConstructingThread: Boolean = {
    val constructingThread = atomic.get
    Thread.currentThread == constructingThread
  }
}

private[scalatest] class ConcurrentDocumenter(fire: (String, Boolean) => Unit) extends ThreadAwareDocumenter {

  def apply(message: String) {
    if (message == null)
      throw new NullPointerException
    fire(message, isConstructingThread) // Fire the info provided event using the passed function
  }
}

private[scalatest] object ConcurrentDocumenter {
  def apply(fire: (String, Boolean) => Unit) = new ConcurrentDocumenter(fire)
}

//
// Three params of function are the string message, a boolean indicating this was from the current thread, and
// the last one is an optional boolean that indicates the message is about a pending test, in which case it would
// be printed out in yellow.
//
// This kind of informer is only used during the execution of tests, to delay the printing out of info's fired
// during tests until after the test succeeded, failed, or pending gets sent out.
//
private[scalatest] class MessageRecordingDocumenter(fire: (String, Boolean, Boolean) => Unit) extends ThreadAwareDocumenter {

  private var messages = List[String]()

  // Should only be called by the thread that constructed this
  // ConcurrentDocumenter, because don't want to worry about synchronization here. Just send stuff from
  // other threads whenever they come in. So only call record after first checking isConstructingThread
  private def record(message: String) {
    require(isConstructingThread)
    messages ::= message
  }

  // Returns them in order recorded
  private def recordedMessages: List[String] = messages.reverse

  def apply(message: String) {
    if (message == null)
      throw new NullPointerException
    if (isConstructingThread)
      record(message)
    else 
      fire(message, false, false) // Fire the info provided event using the passed function
  }

  // send out any recorded messages
  def fireRecordedMessages(testWasPending: Boolean) {
    for (message <- recordedMessages)
      fire(message, true, testWasPending) // Fire the info provided event using the passed function
  }
}

private[scalatest] object MessageRecordingDocumenter {
  def apply(fire: (String, Boolean, Boolean) => Unit) = new MessageRecordingDocumenter(fire)
}

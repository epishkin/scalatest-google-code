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

import java.util.concurrent.atomic.AtomicReference
import MessageRecorder.RecordedMessageFiringFun
import MessageRecorder.ConcurrentMessageFiringFun
import org.scalatest.events.Location
import org.scalatest.Suite.getLineInFile

/*
 This is used by Suite and test informers created as tests run, which therefore have
 populated defined NameInfos. These informers are returned by info in FunSuite and Spec,
 or passed to test methods that take an Informer in Suite, for example. If called by the
 thread that constructed them, which is the thread that was executing the suite and the tests
 inside the suite, then that NameInfo should be propagated. However, if a test starts other
 threads for a multi-threaded test, and those threads apply the Informer, then the NameInfo
 should *not* be propagated, because otherwise it could become very confusing to figure out
 what came from where in the report. Threads started by the test could outlast the thread
 that was running the test, for example. There will be a thread-name, so they can use that
 to figure out who sent what. And threads that call these informers will share a Tracker with
 the thread that was running the tests, so they should be ordered close together after
 sorting by Ordinal. But that's it. NameInfo only goes out when the thread running a test
 or suite applies the Informer.

 This in turn means that a reporter may get hit by multiple threads sending InfoProvided
 messages. If run with the Runner, that will be OK, because DispatchReporter will be in front
 serializing events with its actor. If run() is invoked directly on a suite instance, such as
 from the Scala interpreter, then it may not work. I think I may just say that when running
 from the interpreter, say with run(), you may get interleaved output. This would only happen
 when doing a multi-threaded test that starts threads that calls informer methods, likely a
 rare case. Also, in that case I think it is reasonable to say you may get interleaved output
 in the interpreter, so if you don't like that, use the Runner.
*/
private[scalatest] abstract class ThreadAwareness {

  private final val atomic = new AtomicReference[Thread](Thread.currentThread)

  def isConstructingThread: Boolean = {
    val constructingThread = atomic.get
    Thread.currentThread == constructingThread
  }
}

private[scalatest] class ConcurrentMessageSender(fire: ConcurrentMessageFiringFun) extends ThreadAwareness {

  def apply(message: String) {
    if (message == null)
      throw new NullPointerException("message was null")
    fire(message, isConstructingThread, getLineInFile(Thread.currentThread.getStackTrace, 2)) // Fire the info provided event using the passed function
  }
}

private[scalatest] class ConcurrentInformer(fire: ConcurrentMessageFiringFun) extends ConcurrentMessageSender(fire) with Informer
private[scalatest] object ConcurrentInformer {
  def apply(fire: (String, Boolean, Option[Location]) => Unit) = new ConcurrentInformer(fire)
}

private[scalatest] class ConcurrentDocumenter(fire: ConcurrentMessageFiringFun) extends ConcurrentMessageSender(fire) with Documenter
private[scalatest] object ConcurrentDocumenter {
  def apply(fire: (String, Boolean, Option[Location]) => Unit) = new ConcurrentDocumenter(fire)
}

//
// Three params of function are the string message, a boolean indicating this was from the current thread, and
// the last one is an optional boolean that indicates the message is about a pending test, in which case it would
// be printed out in yellow.
//
// This kind of informer is only used during the execution of tests, to delay the printing out of info's fired
// during tests until after the test succeeded, failed, or pending gets sent out.
//
private[scalatest] class MessageRecorder extends ThreadAwareness {

  private var messages = List[(String, RecordedMessageFiringFun, Option[Location])]()

  // Should only be called by the thread that constructed this
  // ConcurrentInformer, because don't want to worry about synchronization here. Just send stuff from
  // other threads whenever they come in. So only call record after first checking isConstructingThread
  private def record(message: String, fire: RecordedMessageFiringFun, location: Option[Location]) {
    require(isConstructingThread)
    messages ::= (message, fire, location)
  }

  // Returns them in order recorded
  private def recordedMessages: List[(String, RecordedMessageFiringFun, Option[Location])] = messages.reverse

  def apply(message: String, fire: RecordedMessageFiringFun, location: Option[Location]) {
    if (message == null)
      throw new NullPointerException
    if (isConstructingThread)
      record(message, fire, location)
    else 
      fire(message, false, false, false, location) // Fire the info provided event using the passed function
  }

  // send out any recorded messages
  def fireRecordedMessages(testWasPending: Boolean, testWasCanceled: Boolean) {
    for ((message, fire, location) <- recordedMessages)
      fire(message, true, testWasPending, testWasCanceled, location) // Fire the info provided event using the passed function
  }
}

private[scalatest] class MessageRecordingInformer(recorder: MessageRecorder, fire: RecordedMessageFiringFun) extends Informer {
  def apply(message: String) {
    recorder.apply(message, fire, getLineInFile(Thread.currentThread.getStackTrace, 2))
  }
}

private[scalatest] object MessageRecordingInformer {
  def apply(recorder: MessageRecorder, fire: RecordedMessageFiringFun) = new MessageRecordingInformer(recorder, fire)
}

private[scalatest] class MessageRecordingDocumenter(recorder: MessageRecorder, fire: RecordedMessageFiringFun) extends Documenter {
  def apply(message: String) {
    recorder.apply(message, fire, getLineInFile(Thread.currentThread.getStackTrace, 2))
  }
}

private[scalatest] object MessageRecordingDocumenter {
  def apply(recorder: MessageRecorder, fire: RecordedMessageFiringFun) = new MessageRecordingDocumenter(recorder, fire)
}

private[scalatest] object MessageRecorder {
  // Three params of function are the string message, a boolean indicating this was from the current
  // thread, two booleans that indicate the message is about a pending or canceled
  // test (in which case it would be printed out in yellow) and an optional location.
  type RecordedMessageFiringFun = (String, Boolean, Boolean, Boolean, Option[Location]) => Unit 

  // First two params of function are the string message and a boolean indicating this was from the current thread, 
  // and an optional location.
  type ConcurrentMessageFiringFun = (String, Boolean, Option[Location]) => Unit 
}

// For path traits, need a message recording informer that only later gets 
// (theSuite: Suite, report: Reporter, tracker: Tracker, testName: String, theTest: TestLeaf, includeIcon: Boolean. thread: Thread)
private[scalatest] class PathMessageRecordingInformer(fire: (String, Boolean, Boolean, Suite, Reporter, Tracker, String, Int, Boolean, Thread) => Unit) extends ThreadAwareness with Informer {

  import scala.collection.mutable.SynchronizedBuffer
  import scala.collection.mutable.ArrayBuffer
  type Tup = (String, Thread, Boolean)
  private val messages = new ArrayBuffer[Tup] with SynchronizedBuffer[Tup]

  // Should only be called by the thread that constructed this
  // ConcurrentInformer, because don't want to worry about synchronization here. Just send stuff from
  // other threads whenever they come in. So only call record after first checking isConstructingThread
  // So now do have to worry about concurrency
  private def record(message: String) {
    messages += ((message, Thread.currentThread, isConstructingThread))
  }

  // Returns them in order recorded
 // private def recordedMessages: List[String] = for ((msg, _) <- messages) yield toList

  def apply(message: String) {
    if (message == null)
      throw new NullPointerException
    if (isConstructingThread)
      record(message)
    else 
      record(message) 
  }

  // send out any recorded messages
  def fireRecordedMessages(testWasPending: Boolean, theSuite: Suite, report: Reporter, tracker: Tracker, testName: String, indentation: Int, includeIcon: Boolean) {
    for ((message, thread, wasConstructingThread) <- messages) {
     // (theSuite: Suite, report: Reporter, tracker: Tracker, testName: String, theTest: TestLeaf, includeIcon: Boolean)
      fire(message, wasConstructingThread, testWasPending, theSuite, report, tracker, testName, indentation, includeIcon, thread) // Fire the info provided event using the passed function
    }
  }
}

private[scalatest] object PathMessageRecordingInformer {
  def apply(fire: (String, Boolean, Boolean, Suite, Reporter, Tracker, String, Int, Boolean, Thread) => Unit) = new PathMessageRecordingInformer(fire)
}

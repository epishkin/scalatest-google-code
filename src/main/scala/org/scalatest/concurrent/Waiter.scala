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

import org.scalatest._
import Assertions.fail

/**
 * Class that facilitates performing assertions outside the main test thread, such as assertions in callback threads
 * that are invoked asynchronously.
 *
 * <p>
 * To use <code>Waiter</code>, create an instance of it from the main test thread:
 * </p>
 * 
 * <pre class=stHighlight">
 * val w = new Waiter // Do this in the main test thread
 * </pre>
 *
 * <p>
 * At some point later, call <code>await</code> on the waiter:
 * </p>
 * 
 * <pre class="stHighlight">
 * w.await() // Call await() from the main test thread
 * </pre>
 * 
 * <p>
 * The <code>await</code> call will block until it either receives a report of a failed assertion from a different thread, at which
 * point it will complete abruptly with the same exception, or until it is <em>dismissed</em> by a different thread (or threads), at
 * which point it will return normally. You an optionally specify a timeout and/or a number of dismissals to wait for: 
 * </p>
 *
 * <pre class="stHighlight">
 * w.await(timeout = 10, dismissals = 2)
 * </pre>
 * 
 * <p>
 * The default value for <code>timeout</code> is -1, which means wait until dismissed without a timeout. The default value for
 * <code>dismissals</code> is 1. The <code>await</code> method will block until either it is dismissed a sufficient number of times by other threads or
 * an assertion fails in another thread. Thus if you just want to perform assertions in just one other thread, only that thread will be
 * performing a dismissal, so you can use the default value of 1 for <code>dismissals</code>.
 * </p>
 * 
 * <p>
 * To dismiss a waiter, you just invoke <code>dismiss</code> on it:
 * </p>
 * 
 * <pre class="stHighlight">
 * w.dismiss() // Call this from one or more other threads
 * </pre>
 * 
 * <p>
 * You may want to put <code>dismiss</code> invocations in a finally clause to ensure they happen even if an exception is thrown.
 * Otherwise if a dismissal is missed because of a thrown exception, an <code>await</code> call without a timeout will block forever. 
 * If the <code>await</code> is called with a timeout, though, this won't be a problem.
 * </p>
 * 
 * <p>
 * Finally, to perform an assertion in a different thread, you just apply the <code>Waiter</code> to the assertion code. Here are
 * some examples:
 * </p>
 *
 * <pre class="stHighlight">
 * w { assert(1 + 1 === 3) }    // Can use assertions
 * w { 1 + 1 should equal (3) } // Or matchers
 * w { "hi".charAt(-1) }        // Any exceptions will be forwarded to await
 * </pre>
 * 
 * <p>
 * Here's a complete example:
 * </p>
 * 
 * <pre class="stHighlight">
 * import org.scalatest._
 * import concurrent.Waiter
 * import matchers.ShouldMatchers
 * import scala.actors.Actor
 * 
 * class ExampleSuite extends FunSuite with ShouldMatchers {
 * 
 *   case class Message(text: String)
 * 
 *   class Publisher extends Actor {
 * 
 *     @volatile private var handle: Message => Unit = { (msg) => }
 * 
 *     def registerHandler(f: Message => Unit) {
 *       handle = f
 *     }
 * 
 *     def act() {
 *       var done = false
 *       while (!done) {
 *         receive {
 *           case msg: Message => handle(msg)
 *           case "Exit" => done = true
 *         }
 *       }
 *     }
 *   }
 * 
 *   test("example one") {
 *     
 *     val publisher = new Publisher
 *     val message = new Message("hi")
 *     val w = new Waiter
 * 
 *     publisher.start()
 * 
 *     publisher.registerHandler { msg =>
 *       w { msg should equal (message) }
 *       w.dismiss()
 *     }
 * 
 *     publisher ! message
 *     w.await()
 *     publisher ! "Exit"
 *   }
 * }
 * </pre>
 * 
 * @author Bill Venners
 */
class Waiter {

  private final val creatingThread = Thread.currentThread

  @volatile private var dismissedCount = 0
  @volatile private var thrown: Option[Throwable] = None
  
  private def setThrownIfEmpty(t: Throwable) {
    synchronized {
      if (thrown.isEmpty) thrown = Some(t)
    }
  }
  
  /**
   * Executes the passed by-name, and if it throws an exception, forwards it to the thread that calls <code>await</code>, unless
   * a by-name passed during a previous invocation of this method threw an exception.
   * 
   * <p>
   * This method returns normally whether or not the passed function completes abruptly. If called multiple times, only the
   * first invocation that yields an exception will "win" and have its exception forwarded to the thread that calls <code>await</code>.
   * Any subsequent exceptions will be "swallowed." This method may be invoked by multiple threads concurrently, in which case it is a race
   * to see who wins and has their exception forwarded to <code>await</code>. The <code>await</code> call will eventually complete
   * abruptly with the winning exception, or return normally if that instance of <code>Waiter</code> is dismissed. Any exception thrown by
   * a by-name passed to <code>apply</code> after the <code>Waiter</code> has been dismissed will also be "swallowed."
   * </p>
   * 
   * @param fun the by-name function to execute
   */
  def apply(fun: => Unit) {
    try {
      fun
    }
    catch { // Exceptions after the first are swallowed (need to get to dismissals later)
      case t: Throwable => setThrownIfEmpty(t)
    }
  }
  
  // -1 is forever? Or should I have a default of 1000?
  /**
   * Wait for an exception to be produced by the by-name passed to <code>apply</code> or the specified number of dismissals.
   * 
   * <p>
   * This method may only be invoked by the thread that created the <code>Waiter</code>. 
   * Each time this method completes, its internal dismissal count is reset to zero, so it can be invoked multiple times. However,
   * once <code>await</code> has completed abruptly with an exception produced during a call to <code>apply</code>, it will continue
   * to complete abruptly with that exception. The default value for the <code>dismissals</code> parameter is 1.
   * </p>
   * 
   * <p>
   * The <code>timeout</code> parameter allows you to specify a timeout after which a <code>TestFailedException</code> will be thrown with
   * a detail message indicating the <code>await</code> call timed out. The default value for <code>timeout</code> is -1, which indicates
   * no timeout at all. Any positive value (or zero) will be interpreted as a timeout expressed in milliseconds. If no calls to <code>apply</code>
   * have produced an exception and an insufficient number of dismissals has been received by the time the <code>timeout</code> number
   * of milliseconds has passed, <code>await</code> will complete abruptly with <code>TestFailedException</code>.
   * </p>
   * 
   * @param timeout the number of milliseconds timeout, or -1 to indicate no timeout (default is -1)
   * @param dismissals the number of dismissals to wait for (default is 1)
   */
  def await(timeout: Long = -1, dismissals: Int = 1) {
    if (Thread.currentThread != creatingThread)
      throw new NotAllowedException(Resources("awaitMustBeCalledOnCreatingThread"), 1)
    
    val startTime = System.currentTimeMillis
    def timedOut = timeout >= 0 && startTime + timeout < System.currentTimeMillis
    while (dismissedCount < dismissals && !timedOut && thrown.isEmpty)
      Thread.sleep(10)

    dismissedCount = 0 // reset the dismissed count to support multiple await calls
    if (thrown.isDefined)
      throw thrown.get
    else if (timedOut)
      throw new TestFailedException(Resources("awaitTimedOut"), 1)
  }
  
  /**
   * Increases the dismissal count by one. 
   * 
   * <p>
   * Once the dismissal count has reached the value passed to <code>await</code> (and no prior invocations of <code>apply</code>
   * produced an exception), <code>await</code> will return normally. 
   * </p>
   */
  def dismiss() {
    dismissedCount += 1
  }
}

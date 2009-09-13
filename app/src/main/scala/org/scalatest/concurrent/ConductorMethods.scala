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
package org.scalatest.concurrent

import events._
import java.util.concurrent.atomic.AtomicReference
import _root_.java.util.concurrent.Callable

/**
 * A Scala port of <a href="http://code.google.com/p/multithreadedtc/">MultithreadedTC</a>
 * (also <a href="http://www.cs.umd.edu/projects/PL/multithreadedtc/overview.html">here</a>)
 * which was built by Bill Pugh and Nat Ayewah at the University of Maryland.
 *
 * <blockquote>"The MultithreadedTC framework was created to make it easier to test small concurrent abstractions.
 * It enables test designers to guarantee a specific interleaving of two or more threads,
 * even in the presence of blocking and timing issues."</blockquote>
 *
 * <blockquote>"MultithreadedTC is a framework for testing concurrent applications.
 * It features a metronome that is used to provide fine control over the sequence
 * of activities in multiple threads."</blockquote>
 *
 * The Scala version offers significant API improvements over the original Java version,
 * while maintaining most of the intent. Some original ideas were introduced, while some
 * non-scala-like features were cut.  
 *
 * @author Josh Cough
 */
trait ConductorMethods extends RunMethods { this: Suite =>

  private val conductor = new AtomicReference[Conductor]()

  /**
   * Create a new thread that will execute the given function.
   * If the test is started, then the thread will run the function immediately.
   * If it is not yet started, the Thread will wait to run the function until
   * all threads are up and ready to go.
   * @param f the function to be executed by the thread
   */
  protected def thread[T](f: => T): Thread = conductor.get.thread{ f }

  /**
   * Create a new thread that will execute the given function.
   * If the test is started, then the thread will run the function immediately.
   * If it is not yet started, the Thread will wait to run the function until
   * all threads are up and ready to go.
   * @param name the name of the thread
   * @param f the function to be executed by the thread
   */
  protected def thread[T](name: String)(f: => T): Thread = conductor.get.thread(name){ f }

  /*
   * Create a new thread that will execute the given Runnable
   * @param runnable the Runnable to be executed by the thread
   */
  // def thread[T](runnable: Runnable): Thread = conductor.get.thread(runnable)

  /*
   * Create a new thread that will execute the given Runnable
   * @param name the name of the thread
   * @param runnable the Runnable to be executed by the thread
   */
  // def thread[T](name: String, runnable: Runnable): Thread = conductor.get.thread(name,runnable)

  /*
   * Create a new thread that will execute the given Callable
   * @param callable the Callable to be executed by the thread
   */
  // def thread[T](callable: Callable[T]): Thread = conductor.get.thread(callable)

  /*
   * Create a new thread that will execute the given Callable
   * @param name the name of the thread
   * @param callable the Callable to be executed by the thread
   */
  // def thread[T](name: String, callable: Callable[T]): Thread = conductor.get.thread(name,callable)

  /**
   * Force the current thread to block until the thread clock reaches the
   * specified value, at which point the current thread is unblocked.
   * @param c the tick value to wait for
   */
  protected def waitForBeat(beat:Int) = conductor.get.waitForBeat(beat)

  /**
   * Run the passed function, ensuring the clock does not advance while the function is running
   * (has not yet returned or thrown an exception).
   */
  protected def withConductorFrozen[T](f: => T) = conductor.get.withConductorFrozen(f)

  /**
   * Check if the clock has been frozen by any threads. (The only way a thread
   * can freeze the clock is by calling withClockFrozen.)
   */
  protected def isConductorFrozen: Boolean = conductor.get.isConductorFrozen

  /**
   * Gets the current value of the clock. Primarily useful in assert statements.
   * @return the current tick value
   */
  protected def beat = conductor.get.beat

  /**
   * Register a function to be executed after the simulation has finished.
   */
  protected def whenFinished(f: => Unit) = conductor.get.whenFinished{ f }

  override def withFixture(test: NoArgTest) {
    conductor.compareAndSet(conductor.get, new Conductor)
    test()
    if (!conductor.get.conductingHasBegun)
      conductor.get.conduct()
  }
}

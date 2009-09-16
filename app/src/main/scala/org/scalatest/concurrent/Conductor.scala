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

import org.scalatest._
import Thread.State._
import PimpedThreadGroup._
import _root_.java.util.concurrent._
import _root_.java.util.concurrent.atomic.AtomicReference
import org.scalatest.StackDepthExceptionHelper.getStackDepth

/**
 * Class that facilitates the testing of classes, traits, and libraries designed
 * to be used by multiple threads concurrently.
 *
 * <p>
 * A <code>Conductor</code> conducts a multi-threaded scenario by maintaining
 * a clock of "beats." Beats are numbered starting with 0. You can ask a
 * <code>Conductor</code> to run threads that interact with the class, trait,
 * or library (the <em>subject</em>)
 * you want to test. A thread can call the <code>Conductor</code>'s
 * <code>waitForBeat</code> method, which will cause the thread to block
 * until that beat has been reached. The <code>Conductor</code> will advance
 * the beat only when all threads participating in the test are blocked. By
 * tying the timing of thread activities to specific beats, you can write
 * tests for concurrent systems that have deterministic interleavings of
 * threads.
 * </p>
 *
 * <p>
 * A <code>Conductor</code> object has a three-phase lifecycle. It begins its life
 * in the <em>setup</em> phase. During this phase, you can start threads by
 * invoking the <code>thread</code> method on the <code>Conductor</code>.
 * When <code>conduct</code> is invoked on a <code>Conductor</code>, it enters
 * the <em>conducting</em> phase. During this phase it conducts the one multi-threaded
 * scenario it was designed to conduct. After all participating threads have exited, either by
 * returning normally or throwing an exception, the <code>conduct</code> method
 * will complete, either by returning normally or throwing an exception. As soon as
 * the <code>conduct</code> method completes, the <code>Conductor</code>
 * enters its <em>defunct</em> phase. Once the <code>Conductor</code> has conducted
 * a multi-threaded scenario, it is defunct and can't be reused. To run the same test again,
 * you'll need to create a new instance of <code>Conductor</code>.
 * </p>
 *
 * <p>
 * Here's an example of the use of <code>Conductor</code> to test the <code>ArrayBlockingQueue</code>
 * class from <code>java.util.concurrent</code>:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FunSuite
 * import org.scalatest.matchers.ShouldMatchers
 * import java.util.concurrent.ArrayBlockingQueue
 *
 * class ArrayBlockingQueueSuite extends FunSuite with ShouldMatchers {
 * 
 *   test("calling put on a full queue blocks the producer thread") {
 *
 *     val conductor = new Conductor
 *     import conductor._
 *
 *     val buf = new ArrayBlockingQueue[Int](1)
 * 
 *     thread("producer") {
 *       buf put 42
 *       buf put 17
 *       beat should be (1)
 *     }
 * 
 *     thread("consumer") {
 *       waitForBeat(1)
 *       buf.take should be (42)
 *       buf.take should be (17)
 *     }
 * 
 *     whenFinished {
 *       buf should be ('empty)
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * When the test shown is run, it will create one thread named <em>producer</em> and another named
 * <em>consumer</em>. The producer thread will eventually execute the code passed as a by-name
 * parameter to <code>thread("producer")</code>:
 * </p>
 *
 * <pre>
 * buf put 42
 * buf put 17
 * beat should be (1)
 * </pre>
 *
 * Similarly, the consumer thread will eventually execute the code passed as a by-name parameter
 * to <code>thread("consumer")</code>:
 * </p>
 *
 * <pre>
 * waitForBeat(1)
 * buf.take should be (42)
 * buf.take should be (17)
 * </pre>
 *
 * <p>
 * The <code>thread</code> calls create the threads and starts them, but they will not immediately
 * execute the by-name parameter passed to them. They will first block, waiting for the <code>Conductor</code>
 * to give them a green light to proceed.
 * </p>
 *
 * <p>
 * The next call in the test is <code>whenFinished</code>. This method will first call <code>conduct</code> on
 * the <code>Conductor</code>, which will wait until all threads that were created (in this case, producer and consumer) are
 * at the "starting line", <em>i.e.</em>, they have all started and are blocked, waiting on the green light.
 * The <code>conduct</code> method will then give these threads the green light and they will
 * all start executing their blocks concurrently.
 * </p>
 * 
 * <p>
 * When the threads are given the green light, the beat is 0. The first thing the producer thread does is put 42 in
 * into the queue. As the queue is empty at this point, this succeeds. The producer thread next attempts to put a 17
 * into the queue, but because the queue has size 1, this can't succeed until the consumer thread has read the 42
 * from the queue. This hasn't happened yet, so producer blocks. Meanwhile, the consumer thread's first act is to
 * call <code>waitForBeat(1)</code>. Because the beat starts out at 0, this call will block the consumer thread.
 * As a result, once the producer thread has executed <code>buf put 17</code> and the consumer thread has executed
 * <code>waitForBeat(1)</code>, both threads will be blocked.
 * </p>
 *
 * <p>
 * The <code>Conductor</code> maintains a clock that wakes up periodically and checks to see if all threads
 * participating in the multi-threaded scenario (in this case, producer and consumer) are blocked. If so, it
 * increments the beat. Thus sometime later the beat will be incremented, from 0 to 1. Because consumer was
 * waiting for beat 1, it will wake up (<em>i.e.</em>, the <code>waitForBeat(1)</code> call will return) and
 * execute the next line of code in its block, <code>buf.take should be (42)</code>. This will succeed, because
 * the producer thread had previously (during beat 0) put 42 into the queue. This act will also make
 * producer runnable again, because it was blocked on the second <code>put</code>, which was waiting for another
 * thread to read that 42.
 * </p>
 *
 * <p>
 * Now both threads are unblocked and able to execute their next statement. The order is
 * non-deterministic, and can even be simultaneous if running on multiple cores. If the <code>consumer</code> thread
 * happens to execute <code>buf.take should be (17)</code> first, it will block (<code>buf.take</code> will not return), because the queue is
 * at that point empty. At some point later, the producer thread will execute <code>buf put 17</code>, which will
 * unblock the consumer thread. Again both threads will be runnable and the order non-deterministic and
 * possibly simulataneous. The producer thread may charge ahead and run its next statement, <code>beat should be (1)</code>.
 * This will succeed because the beat is indeed 1 at this point. As this is the last statement in the producer's block,
 * the producer thread will exit normally (it won't throw an exception). At some point later the consumer thread will
 * be allowed to complete its last statement, the <code>buf.take</code> call will return 17. The consumer thread will
 * execute <code>17 should be (17)</code>. This will succeed and as this was the last statement in its block, the consumer will return
 * normally.
 * </p>
 *
 * <p>
 * If either the producer or consumer thread had completed abruptbly with an exception, the <code>conduct</code> method
 * (which was called by <code>whenFinished</code>) would have completed abruptly with an exception to indicate the test
 * failed. However, since both threads returned normally, <code>conduct</code> will return. Because <code>conduct</code> doesn't
 * throw an exception, <code>whenFinished</code> will execute the block of code passed as a by-name parameter to it: <code>buf should be ('empty)</code>.
 * This will succeed, because the queue is indeed empty at this point. The <code>whenFinished</code> method will then return, and
 * because the <code>whenFinished</code> call was the last statement in the test and it didn't throw an exception, the test completes successfully.
 * </p>
 *
 * <p>
 * This test tests <code>ArrayBlockingQueue</code>, to make sure it works as expected. If there were a bug in <code>ArrayBlockingQueue</code>
 * such as a <code>put</code> called on a full queue didn't block, but instead overwrote the previous value, this test would detect
 * it. However, if there were a bug in <code>ArrayBlockingQueue</code> such that a call to <code>take</code> called on an empty queue
 * never blocked and always returned 0, this test might not detect it. The reason is that whether the consumer thread will ever call
 * <code>take</code> on an empty queue during this test is non-deterministic. It depends on how the threads get scheduled during beat 1.
 * What is deterministic in this test, because the consumer thread blocks during beat 0, is that the producer thread will definitely 
 * attempt to write to a full queue. To make sure the other scenario is tested, you'd need a different test:
 * </p>
 *
 * <pre>
 * test("calling take on an empty queue blocks the consumer thread") {
 *
 *   val conductor = new Conductor
 *   import conductor._
 *
 *   val buf = new ArrayBlockingQueue[Int](1)
 *
 *   thread("producer") {
 *     waitForBeat(1)
 *     buf put 42
 *     buf put 17
 *   }
 *
 *   thread("consumer") {
 *     buf.take should be (42)
 *     buf.take should be (17)
 *     beat should be (1)
 *   }
 *
 *   whenFinished {
 *     buf should be ('empty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * In this test, the producer thread will block, waiting for beat 1. The consumer thread will invoke <code>buf.take</code>
 * as its first act. This will block, because the queue is empty. Because both threads are blocked, the <code>Conductor</code>
 * will at some point later increment the beat to 1. This will awaken the producer thread. It will return from its
 * <code>waitForBeat(1)</code> call and execute <code>buf put 42</code>. This will unblock the consumer thread, which will
 * take the 42, and so on.
 * </p>
 *
 * <p>
 * The problem that <code>Conductor</code> is designed to address is the difficulty, caused by the non-deterministic nature
 * of thread scheduling, of testing classes, traits, and libraries that are intended to be used by multiple threads.
 * If you just create a test in which one thread reads from an <code>ArrayBlockingQueue</code> and
 * another writes to it, you can't be sure that you have tested all possible interleavings of threads, no matter
 * how many times you run the test. The purpose of <code>Conductor</code>
 * is to enable you to write tests with deterministic interleavings of threads. If you write one test for each possible
 * interleaving of threads, then you can be sure you have all the scenarios tested. The two tests shown here, for example,
 * ensure that both the scenario in which a producer thread tries to write to a full queue and the scenario in which a
 * consumer thread tries to take from an empty queue are tested.
 * </p>
 *
 * <p>
 * Class <code>Conductor</code> was inspired by the
 * <a href="http://www.cs.umd.edu/projects/PL/multithreadedtc/">MultithreadedTC project</a>,
 * created by Bill Pugh and Nat Ayewah of the University of Maryland.
 * </p>
 *
 * @author Josh Cough
 * @author Bill Venners
 */
class Conductor {

  /**
   * The metronome used to coordinate between threads.
   * This clock is advanced by the clock thread.
   * The clock will not advance if it is frozen.
   */
  private final val clock = new Clock

  /////////////////////// thread management start //////////////////////////////

  // place all threads in a new thread group
  private final val threadGroup = new ThreadGroup("Orchestra")

  // all the threads in this test
  // This need not be volatile, because it is initialized with one object and
  // that stays forever. Because it is final, it 
  private final val threads = new CopyOnWriteArrayList[Thread]()

  // Used to keep track of what names have been created so far, so that
  // it can be enforced that the names are unique.
  private final val threadNames = new CopyOnWriteArrayList[String]()

  // the main test thread
  private final val mainThread = currentThread

  /**
   * Creates a new thread that will execute the specified function.
   *
   * <p>
   * The name of the thread will be of the form Conductor-Thread-N, where N is some integer.
   * </p>
   *
   * <p>
   * This method may be safely called by any thread.
   * </p>
   *
   * @param fun the function to be executed by the newly created thread
   * @return the newly created thread
   */
  def thread[T](fun: => T): Thread = thread("Conductor-Thread-" + threads.size) { fun }

  /*
   * Create a new thread that will execute the given Runnable
   * @param runnable the Runnable to be executed by the thread
   */
  // def thread[T](runnable: Runnable): Thread = thread("thread" + threads.size) {runnable.run}

  /*
   * Create a new thread that will execute the given Runnable
   * @param runnable the Runnable to be executed by the thread
   */
  // def thread[T](name: String, runnable: Runnable): Thread = thread(name) {runnable.run}

  /*
   * Create a new thread that will execute the given Callable
   * @param callable the Callable to be executed by the thread
   */
  // def thread[T](callable: Callable[T]): Thread = thread("thread" + threads.size) {callable.call}

  /*
   * Create a new thread that will execute the given Callable
   * @param callable the Callable to be executed by the thread
   */
  // def thread[T](name: String, callable: Callable[T]): Thread = thread(name) {callable.call}

  /**
   * Creates a new thread with the specified name that will execute the specified function.
   *
   * <p>
   * This method may be safely called by any thread.
   * </p>
   *
   * @param name the name of the newly created thread
   * @param fun the function to be executed by the newly created thread
   * @return the newly created thread
   */
  def thread[T](name: String)(fun: => T): Thread = {
    currentState.get match {
      case TestFinished => throw new IllegalStateException(Resources("threadCalledAfterConductingHasCompleted"))
      case _ =>
        if (threadNames contains name)
          throw new NotAllowedException(Resources("cantRegisterThreadsWithSameName", name), getStackDepth("Conductor.scala", "thread"))
        val t = TestThread(name, fun _)
        threads add t
        threadNames add name
        startThread(t)
    }
  }
  
  // The reason that the thread is started immediately, is so that nested threads
  // will start immediately, without requiring the user to explicitly start() them.
  // Also, so that the thread method can return a Thread object.

  /*
   * Adds threads methods to int, so one can say:<br/>
   * val threads:List[Thread] = 5.threads("some name"){ ... }<br/>
   * val anonymous_threads:List[Thread] = 10 threads { ... }<br/>
   * @param nrThreads the number of threads to be created
   */

  /*
  private implicit def addThreadsMethodToInt(nrThreads:Int) = new ThreadedInt(nrThreads)

  private class ThreadedInt(nrThreads:Int) {
    def threads[T](name: String)(f: => T): List[Thread] = {
      val seq = for( i <- 1 to nrThreads) yield thread(name + "("+i+")") {f}
      seq.toList
    }
    def threads[T](f: => T): List[Thread] = {
      val seq = for( i <- 1 to nrThreads) yield thread{f}
      seq.toList
    }
  }
*/
  
  /*
   * A test thread runs the given function.
   * It only does so after it is given permission to do so by the main thread.
   * The main thread grants permission after it receives notication that
   * all test threads are ready to go.
   */
  private case class TestThread[T](name: String, f: () => T) extends Thread(threadGroup, name) {

    testThreadsStartingCounter.increment()

    override def run() {
      try {
        // notify the main thread that we are indeed ready to go.
        testThreadsStartingCounter.decrement()

        // wait for the main thread to say its ok to go.
        greenLightForTestThreads.await

        // go
        f()
      } catch {
        // The reason this is a catch Throwable is because you want to let ThreadDeath through
        // without signalling errors. Otherwise the signalError could have been in a finally.
        // If the simulation is aborted, then stop will be called,
        // which will cause ThreadDeath, so just die and do nothing.
        case e: ThreadDeath => // Do nothing and return from run()
        case t: Throwable => signalError(t)
      }
    }
  }
  // TODO: I'm not sure signalError isn't going to not cause threads to be stopped that no longer exist,
  // because it caues AssertionErrors to be thrown in the other threads, so they'll also end up here
  // and call signalError, and so multiple threads will be attempting to kill the same threads. Sloppy.
  // Possibly throw a special exception that indicates a thread was killed because a test failure has
  // already been detected. This can be detected here and then *not* call signalError, but simply be
  // added to the errors queue. So the first thread to get an exception would be responsible for stopping
  // all the other threads.
  /**
   * Starts a thread, logging before and after
   */
  private def startThread(thread: Thread): Thread = {
    logAround("Starting thread named: " + thread) {
      thread.start()
      thread
    }
  }

  /////////////////////// thread management end /////////////////////////////

  /////////////////////// error handling start //////////////////////////////

  /**
   * A BlockingQueue containing the first Error/Exception that occured
   * in thread methods or that are thrown by the clock thread.
   */
  private val errorsQueue = new ArrayBlockingQueue[Throwable](20)

  /*
   * A list of any errors thrown by test threads at the time this method is called.
   */
  /*
  def exceptions: List[Throwable] = {
    def exceptions(errorList: List[Throwable], it: java.util.Iterator[Throwable]): List[Throwable] = {
      if(it.hasNext) exceptions( errorList ::: List(it.next), it)
      else errorList
    }
    exceptions(Nil, errorsQueue.iterator)
  }
    */

  /**
   * Stop all test case threads and clock thread, except the thread from
   * which this method is called. This method is used when a thread is
   * ready to end in failure and it wants to make sure all the other
   * threads have ended before throwing an exception.
   * Clock thread will return normally when no threads are running.
   */
  private def signalError(t: Throwable) {
    log(t)
    errorsQueue offer t
    for (t <- threadGroup.getThreads; if (t != currentThread)) {
      log("signaling error to " + t.getName)
      val assertionError = new java.lang.AssertionError(t.getName + " killed by " + currentThread.getName)
   // TODO: Fix bug: t.getStackTrace could be null for some reason. I got
      // a NPE here.
      assertionError setStackTrace t.getStackTrace
      t stop assertionError
    }
  }

  /////////////////////// error handling end //////////////////////////////

  /**
   * Invokes <code>conduct</code> and after <code>conduct</code> method returns,
   * if <code>conduct</code> returns normally (<em>i.e.</em>, without throwing
   * an exception), invokes the passed function.
   *
   * <p>
   * If <code>conduct</code> completes abruptly with an exception, this method
   * will complete abruptly with the same exception and not execute the passed
   * function.
   * </p>
   *
   * <p>
   * This method must be called by the thread that instantiated this <code>Conductor</code>,
   * and that same thread will invoke <code>conduct</code> and, if it returns noramlly, execute
   * the passed function.
   * </p>
   *
   * <p>
   * Because <code>whenFinished</code> invokes <code>conduct</code>, it can only be invoked
   * once on a <code>Conductor</code> instance. As a result, if you need to pass a block of
   * code to <code>whenFinished</code> it should be the last statement of your test. If you
   * don't have a block of code that needs to be run once all the threads have finished
   * successfully, then you can simply invoke <code>conduct</code> and never invoke
   * <code>whenFinished</code>.
   * </p>
   *
   * @param fun the function to execute after <code>conduct</code> call returns
   * @throws IllegalStateException if the calling thread is not the thread that
   *   instantiated this <code>Conductor</code>, or if <code>conduct</code> has already
   *    been invoked on this conductor.
   */
  def whenFinished(fun: => Unit) {

    if (currentThread != mainThread)  // TODO: Get from resources, write a test
      throw new IllegalStateException("whenFinished can only be called by the thread that created Conductor.")

    if (conductingHasBegun)
      throw new IllegalStateException(Resources("cannotInvokeWhenFinishedAfterConduct"))
    
    conduct()

    fun
  }

  /////////////////////// clock management start //////////////////////////
  // TODO: Throw an illegalArgEx if they pass an int <= 0
  // And document that the beat starts at zero
  // or actually a beat less than the current beat, right? That would wait forever.
  /**
   * Blocks the current thread until the thread beat reaches the
   * specified value, at which point the current thread will be unblocked.
   *
   * @param beat the tick value to wait for
   */
  def waitForBeat(beat: Int) { clock waitForBeat beat }

  /**
   * The current value of the thread clock.
   *
   * @return the current beat value
   */
  def beat: Int = clock.currentBeat

  /**
   * Executes the passed function with the <code>Conductor</code> <em>frozen</em> so that it
   * won't advance the clock.
   *
   * <p>
   * While the <code>Conductor</code> is frozen, the beat will not advance. Once the
   * passed function has completed executing, the <code>Conductor</code> will be unfrozen
   * so that the beat will advance when all threads are blocked, as normal.
   * </p>
   *
   * @param fun the function to execute while the <code>Conductor</code> is frozen.
   */
  def withConductorFrozen[T](fun: => T) = clock.withClockFrozen(fun _)

  /**
   * Indicates whether the conductor has been frozen.
   *
   * <p>
   * Note: The only way a thread
   * can freeze the conductor is by calling <code>withConductorFrozen</code>.
   * </p>
   */
  def isConductorFrozen: Boolean = clock.isFrozen

  /////////////////////// clock management end //////////////////////////////

  /////////////////////// run methods start /////////////////////////////////

  /*
   * Keeps the main thread from allowing the test threads to execute their bodies
   * until all of them are started, and ready to go. When a test thread is started,
   * it will call increment from its constructor. It then calls decrement from its
   * run method. Test threads are started immediately by the thread() methods, and
   * so this allows the main thread to block until all test threads have started.
   * It does this by calling the waitUntilAllTestThreadsHaveStarted method, which
   * blocks in the wait set if the count is not 0. (The count is only non-zero when
   * one or more test threads have been created but not yet gotten their run methods
   * going.) This is only used for threads started by the main thread. By the time
   * conduct is invoked, all threads started by the main thread will have called
   * increment. (Increment in this case will be called by the main thread.) After
   * those threads go, they may actually call thread method again, but the main thread
   * will only call waitUntilAllTestThreadsHaveStarted once, so it won't matter. - bv
   */
  private class TestThreadsStartingCounter {
    private var count: Int = 0
    def increment() {
      synchronized {
        count += 1
      }
    }
    def decrement() {
      synchronized {
        count -= 1
        notifyAll()
      }
    }
    def waitUntilAllTestThreadsHaveStarted() {
      synchronized {
        while (count != 0) {
          wait()
        }
      }
    }
  }

  private val testThreadsStartingCounter = new TestThreadsStartingCounter

  /**
   * Keeps the test threads from executing their bodies until the main thread
   * allows them to.
   */
  private val greenLightForTestThreads = new CountDownLatch(1)

  /**
   * Conducts a multithreaded test with a default clock period of 10 milliseconds
   * and default run limit of 5 seconds.
   */
  def conduct() {
    val DefaultClockPeriod = 10 // milliseconds
    val DefaultRunLimit = 5 // seconds
    conduct(DefaultClockPeriod, DefaultRunLimit)
  }

  private val currentState: AtomicReference[ConductorState] = new AtomicReference(Setup)

  /**
   * Indicates whether either of the two overloaded <code>conduct</code> methods
   * have been invoked.
   *
   * <p>
   * This method returns true if either <code>conduct</code> method has been invoked. The
   * <code>conduct</code> method may have returned or not. (In other words, a <code>true</code>
   * result from this method does not mean the <code>conduct</code> method has returned,
   * just that it was already been invoked and,therefore, the multi-threaded scenario it
   * conducts has definitely begun.)
   * </p>
   */
  def conductingHasBegun: Boolean = currentState.get.testWasStarted

  /**
   * Conducts a multithreaded test with the specified clock period (in milliseconds)
   * and run limit (in seconds).
   *
   * @param clockPeriod The period (in ms) between checks for the clock
   * @param runLimit The limit to run the test in seconds
   * @throws Throwable The first error or exception that is thrown by one of the threads
   */
  def conduct(clockPeriod: Int, runLimit: Int) {

    // if the test was started already, explode
    // otherwise, change state to TestStarted                          
    if (conductingHasBegun)
      throw new NotAllowedException(Resources("cannotCallConductTwice"), getStackDepth("Conductor.scala", "conduct"))
    else
      currentState set TestStarted

    // wait until all threads are definitely ready to go
    // mainThreadStartLatch.await()  RACE CONDITION
    testThreadsStartingCounter.waitUntilAllTestThreadsHaveStarted()

    // release the latch, allowing all threads to start
    // wait for all the test threads to start before starting the clock
    greenLightForTestThreads.countDown()

    // start the clock thread
    val clockThread = startThread(ClockThread(clockPeriod, runLimit))

    // wait until all threads have ended
    waitForThreads

    // change state to test finished
    currentState set TestFinished
  }

  /**
   * Wait for all of the test case threads to complete, or for one
   * of the threads to throw an exception, or for the clock thread to
   * interrupt this (main) thread of execution. When the clock thread
   * or other threads fail, the error is placed in the shared error array
   * and thrown by this method.
   *
   * @param threads List of all the test case threads and the clock thread
   */
  // Explain how we understand it works: if the thread that's been joined already dies with an exception
  // that will go into errors, and this thread the join will return. If the thread returns and doesn't
  // die, that means all went well, and join will return and it can loop to the next one.
  // There should be no race condition between the last thread being waited on by join, it dies, join
  // returns, and after that the error gets into the errors. Because if you look in run() in the
  // thread inside createTestThread, the signalling error happens in a catch Throwable block before the thread
  // returns.
  private def waitForThreads{
    while(threadGroup.areAnyThreadsAlive){
      threadGroup.getThreads foreach waitForThread
    }
  }

  // TODO: Grab strings from resources for all log messages
  private def waitForThread(t: Thread) {
    log("waiting for: " + t.getName + " which is in state:" + t.getState)
    try {
      // TODO: Why is this stopping the threads too? Isn't the signalError approach sufficient?
      // This one just kills them.
      if (t.isAlive && !errorsQueue.isEmpty) logAround("stopping: " + t) { t.stop() }
      else logAround("joining: " + t) { t.join() }
      assert(t.getState == TERMINATED) // TODO: Drop this for the release
    } catch {
      case e: InterruptedException => {
        log("killed waiting for threads. probably deadlock or timeout.")
        errorsQueue offer new AssertionError(e)
      }                                          // TODO: CHECK JCIP. NEED TO RESET THE INTERRUPTED FLAG?
      // THE MAIN THREAD CAN GET INTERRUPTED BY THE Clock Thread's DEADLOCK DETECTOR
    }
  }

  // For 1.0, don't offer logging as a feature, to reduce the documentation
  // and work load for 1.0, and also to find out what real users really want
  // in terms of logging from Conductor, if anything.
  private def log(a: Any) = {
/*
    informer match {
      case Some(info) => info(a.toString)
      case None => 
    }
*/
  }

  /**
   * Logs before and after executing the given function.
   */
  private def logAround[T](msg: String)(f: => T): T = {
    log("|starting: " + msg)
    val t = f
    log("|done with: " + msg)
    t
  }

  /////////////////////// logging end /////////////////////////////


  /**
   * A Clock manages the current beat in a Conductor.
   * Several duties stem from that responsibility.
   *
   * The clock will:
   *
   * <ol>
   * <li>Block a thread until the tick has reached a particular time.</li>
   * <li>Report the current time</li>
   * <li>Run operations with the clock frozen.</li>
   * </ol>
   */
  private class Clock {

    import java.util.concurrent.locks.ReentrantReadWriteLock
    import PimpedReadWriteLock._

    // clock starts at time 0
    private var currentTime = 0

    // methods in Clock that access or modify the private instance vars of this
    // Clock are synchronized on the object referenced from lock
    private val lock = new AnyRef

    /**
     * Read locks are acquired when clock is frozen and must be
     * released before the clock can advance in a advance(). (In a
     * ReentrantReadWriteLock, multiple threads can hold the read lock (and these
     * threads might read the value of currentTime (the currentBeat method), or just execute a
     * function with the clock frozen (the withClockFrozen method). The write lock
     * of a ReentrantReadWriteLock is exclusive, so only one can hold it, and it
     * can't be held if there are a thread or threads holding the read lock. This
     * is why the clock can't advance during a withClockFrozen, because the read
     * lock is grabbed before the function is executed in withClockFrozen, thus
     * advance will not be able to acquire the write lock to update currentTime
     * until after withClockFrozen has released the read lock (and there are no other
     * threads holding a read lock or the write lock).
     */
    private val rwLock = new ReentrantReadWriteLock

    private var highestBeatBeingWaitedOn = 0

    /**
     * Advance the current beat. In order to do so, the clock will wait
     * until it has become unfrozen.
     *
     * All threads waiting for the clock to advance (they would have been put in the lock
     * object's wait set by invoking the waitForBeat method) will be notified after the advance.
     *
     * Only the clock thread should be calling this.
     *
     * If the clock has been frozen by a thread, then that thread will own the readLock. Write
     * lock can only be acquired when there are no readers, so ticks won't progress while someone
     * has the clock frozen. Other methods also grab the read lock, like time (which gets
     * the current beat.)
     */
    def advance() {
      lock.synchronized {
        rwLock.write {
          log("clock advancing from: " + currentTime + " to: " + (currentTime + 1))
          currentTime += 1
        }
        lock.notifyAll()
      }
    }

    /**
     * The current beat.
     */
    def currentBeat: Int =
      lock.synchronized {
        rwLock read currentTime
      }

    /**
     * When wait for beat is called, the current thread will block until
     * the given beat is reached by the clock.
     */
    def waitForBeat(beat: Int) {
      lock.synchronized {
        if (beat > highestBeatBeingWaitedOn)
          highestBeatBeingWaitedOn = beat
        logAround(currentThread.getName + " is waiting for beat " + beat) {
          while (currentBeat < beat) {
            try {
              lock.wait()
            } catch {     // TODO: this is probably fine, but check JCIP about InterEx again
              case e: InterruptedException => throw new AssertionError(e)
            }         // Actually I"m not sure. Maybe should reset the interupted status
          }
        }
      }
    }

    // The reason there's no race condition between calling currentBeat in the while and calling
    // lock.wait() later (between that) and some other thread incrementing the beat and doing
    // a notify that this thread would miss (which it would want to know about if that's the
    // new time that it's waiting for) is because both this and the currentBeat method are synchronized
    // on the lock.

    /**
     * Returns true if any thread is waiting for a beat in the future (greater than the current beat)
     */
    def isAnyThreadWaitingForABeat = {
      lock.synchronized { highestBeatBeingWaitedOn > currentTime }
    }

    /**
     * When the clock is frozen, it will not advance even when all threads
     * are blocked. Use this to block the current thread with a time limit,
     * but prevent the clock from advancing due to a waitForBeat(Int) in
     * another thread.
     */
    def withClockFrozen[T](f: => T): T = rwLock read f

    /**
     * Check if the clock has been frozen by any threads.
     */
    def isFrozen: Boolean = rwLock.getReadLockCount > 0
  }

  /**
   * The clock thread is the manager of the multi-threaded scenario.
   * Periodically checks all the test threads and regulates them.
   * If all the threads are blocked and at least one is waiting for a beat,
   * the clock advances to the next beat and all waiting threads are notified.
   * If none of the threads are waiting for a tick or in timed waiting,
   * a deadlock is detected. The clock thread times out if a thread is in runnable
   * or all are blocked and one is in timed waiting for longer than the runLimit.
   *
   * Algorithm in detail:
   *
   * While there are threads alive
   *
   *    If there are threads RUNNING
   *
   *       If they have been running too long
   *
   *          stop the test with a timeout error
   *
   *    else if there are threads waiting for a beat
   *
   *       advance the clock
   *
   *    else if there are threads in TIMED_WAITING
   *
   *       increment the deadlock counter
   *
   *       if the deadlock counter has reached a threshold
   *
   *          stop the test due to potential deadlock
   *
   *    sleep clockPeriod ms
   *
   *
   * @param mainThread The main test thread. This thread will be waiting
   * for all the test threads to finish. It will be interrupted if the
   * ClockThread detects a deadlock or timeout.
   *
   * @param clockPeriod The period (in ms) between checks for the clock
   *
   * @param maxRunTime The limit to run the test in seconds
   */
  private case class ClockThread(clockPeriod: Int, maxRunTime: Int) extends Thread("Conductor-Clock") {
    this setDaemon true // TODO: Why is this a daemon thread? If no good reason, drop it.

    // used in detecting timeouts
    private var lastProgress = System.currentTimeMillis

    // used in detecting deadlocks
    private var deadlockCount = 0
    private val MaxDeadlockDetectionsBeforeDeadlock = 50

    /**
     * Runs the steps described above.
     */
    override def run {
      while (threadGroup.areAnyThreadsAlive) {
        if (threadGroup.areAnyThreadsRunning) {
          if (runningTooLong) timeout()
        }
        else if (clock.isAnyThreadWaitingForABeat) {
          clock.advance()
          deadlockCount = 0
          lastProgress = System.currentTimeMillis
        }
        else if (!threadGroup.areAnyThreadsInTimedWaiting) {
          detectDeadlock()
        }
        Thread sleep clockPeriod
      }
    }

    /**
     * Threads have been running too long (timeout) if
     * The number of seconds since the last progress are more
     * than the allowed maximum run time.
     */
    private def runningTooLong = System.currentTimeMillis - lastProgress > 1000L * maxRunTime

    /**
     * Stop the test due to a timeout.
     */
    private def timeout() {
      val errorMessage = "Timeout! Test ran longer than " + maxRunTime + " seconds."
      signalError(new IllegalStateException(errorMessage))
      mainThread.interrupt()
    }

    /**
     * Determine if there is a deadlock and if so, stop the test.
     */
    private def detectDeadlock() {
      // Should never get to >= before ==, but just playing it safe
      if (deadlockCount >= MaxDeadlockDetectionsBeforeDeadlock) {
        val errorMessage = "Apparent Deadlock! Threads waiting 50 clock periods (" + (clockPeriod * 50) + "ms)"
        signalError(new IllegalStateException(errorMessage))

        // The mainThread may be joined to a thread, and this would break it out of its doldrums?
        // I'm not sure why this is necessary. 
        mainThread.interrupt()
      }
      else deadlockCount += 1
    }
  }

  /**
   * Base class for the possible states of the Conductor.
   */
  private sealed case class ConductorState(testWasStarted: Boolean, testIsFinished: Boolean)

  /**
   * The initial state of the Conductor.
   * Any calls the thread{ ... } will result in started Threads that quickly block waiting for the
   * main thread to give it the green light.
   * Any call to conduct will start the test.
   */
  private case object Setup extends ConductorState(false, false)

  /**
   * The state of the Conductor while its running.
   * Any calls the thread{ ... } will result in running Threads.
   * Any further call to conduct will result in an exception.
   */
  private case object TestStarted extends ConductorState(true, false)

  /**
   * The state of the Conductor after all threads have finished,
   * and the whenFinished method has completed.
   * Any calls the thread{ ... } will result in an exception
   * Any call to conduct will result in an exception.
   */
  private case object TestFinished extends ConductorState(true, true)
}

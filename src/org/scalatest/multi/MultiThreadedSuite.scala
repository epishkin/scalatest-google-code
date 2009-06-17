package org.scalatest.multi


import java.io.{StringWriter, PrintWriter}
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.{ArrayBlockingQueue, Semaphore, CountDownLatch, TimeUnit}
import matchers.MustMatchers


trait MultiThreadedFunSuite extends MultiThreadedSuite with FunSuite with MustMatchers {
  implicit def anyToMustBe(a: Any) = new {
    def mustBe(b: Any) {
      a must be(b)
    }

    def must_be(b: Any) {
      a must be(b)
    }
  }
}


/**
 * @author dood
 * Date: Jun 16, 2009
 * Time: 7:25:34 PM
 */

trait MultiThreadedSuite extends Suite { thisSuite =>

  /**
   * Execute this <code>TestNGSuite</code>.
   *
   * @param testName an optional name of one test to execute. If <code>None</code>, this class will execute all relevant tests.
   *                 I.e., <code>None</code> acts like a wildcard that means execute all relevant tests in this <code>TestNGSuite</code>.
   * @param   reporter	 The reporter to be notified of test events (success, failure, etc).
   * @param   groupsToInclude	Contains the names of groups to run. Only tests in these groups will be executed.
   * @param   groupsToExclude	Tests in groups in this Set will not be executed.
   *
   * @param stopper the <code>Stopper</code> may be used to request an early termination of a suite of tests. However, because TestNG does
   *                not support the notion of aborting a run early, this class ignores this parameter.
   * @param   properties         a <code>Map</code> of properties that can be used by the executing <code>Suite</code> of tests. This class
   *                      does not use this parameter.
   * @param distributor an optional <code>Distributor</code>, into which nested <code>Suite</code>s could be put to be executed
   *              by another entity, such as concurrently by a pool of threads. If <code>None</code>, nested <code>Suite</code>s will be executed sequentially.
   *              Because TestNG handles its own concurrency, this class ignores this parameter.
   * <br><br>
   */
  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, groupsToInclude: Set[String],
      groupsToExclude: Set[String], properties: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    runMultiThreadedTest(reporter)
  }



  ////////////////////////// copied/pasted here for now /////////////

  /**
   * Command line key for indicating the regularity (in milliseconds)
   * with which the clock thread regulates the thread methods.
   */
  val CLOCKPERIOD_KEY = "tunit.clockPeriod"

  /**
   * Command line key for indicating the time limit (in seconds) for
   * runnable threads.
   */
  val RUNLIMIT_KEY = "tunit.runLimit"

  /**
   * The default clock period in milliseconds
   */
  val DEFAULT_CLOCKPERIOD = 10

  /**
   * The default run limit in seconds
   */
  val DEFAULT_RUNLIMIT = 5

  //////////////////////////////////////////////////////////////////////


  /**
   * The metronome used to coordinate between threads. This clock
   * is advanced by the clock thread started by       { @link TestFramework }.
   * The clock will not advance if it is frozen.
   *
   * @see # waitForTick ( int )
   * @see # freezeClock ( )
   * @see # unfreezeClock ( )
   */
  var clock = 0

  /**
   * The primary lock to synchronize on in this test case before
   * accessing fields in this class.
   */
  val lock = new AnyRef

  /**
   * If true, the debugging information is printed to standard out
   * while the test runs
   */
  var trace = System.getProperty("tunit.trace") == "true"

  /**
   * This flag is set to true when a test fails due to deadlock or
   * timeout.
   *
   * @see TestFramework
   */
  var failed = false

  /**
   * a BlockingQueue containing the first Error/Exception that occured
   * in thread methods or that are thrown by the clock thread
   */
  private val error = new ArrayBlockingQueue[Throwable](1)


  /////////////////////// thread management start //////////////////////////////

  // place all threads in a new thread group
  val threadGroup = new ThreadGroup("MTC-Threads")
  var threads = List[Thread]()
  val threadRegistration = new Semaphore(0)

  /**
   * Map each thread to the clock tick it is waiting for.
   */
  val threadsWithTickCounts = scala.collection.mutable.Map[Thread, Int]()

  var functions: Map[String, () => _] = Map()

  def thread[T](f: => T): Thread = thread("thread" + functions.size) {f}

  /**
   * Get a thread given the method name that it corresponds to. E.g.
   * to get the thread running the contents of the method
   * <code>thread1()</code>, call <code>getThreadByName("thread1")</code>
   *
   * <p>
   * NOTE:       { @link # initialize ( ) } is called before threads are created,
   * so this method returns null if called from       { @link # initialize ( ) }
   * (but not from       { @link # finish ( ) } ).
   *
   * @see # getThread ( int )
   *
   * @param methodName
   * 			the name of the method corresponding to the thread requested
   * @return
   * the thread corresponding to methodName
   */
  def getThread(name: String): Thread = {
    lock.synchronized {
      threads.find(_.getName == name) match {
        case Some(t) => t
        case None => null
      }
    }
  }

  def getThread(i: Int): Thread = getThread("thread" + i)

  def thread[T](desc: String)(f: => T): Thread = {
     functions += (desc -> f _)
     val t = createTestThread(desc, f _)
     threads = t :: threads
     t
   }

   private lazy val latch = new CountDownLatch(functions.size)

   private def createTestThread[T](name: String, f: () => T) = {
     val r = new Runnable() {
       def run() {
         //println("thread is running!")
         try {
           threadRegistration.release
           latch.countDown
           latch.await
           // At this point all threads are created and released
           // (in random order?) together to run in parallel
           hello
           println("about to execute function!")
           f.apply
           //println("executed function!")
         } catch {
           case e: ThreadDeath => return
           case t: Throwable => {
             //println("offering error: " + t)
             error offer t
             signalError()
           }
         } finally {
           //println("goodbye")
           goodbye()
         }
       }
     }
     new Thread(threadGroup, r, name)
   }

  /////////////////////// thread management end //////////////////////////////


  /////////////////////// init handler start //////////////////////////////


  def initialize(f: => Unit) {initFunction = Some(f _)}

  private var initFunction: Option[() => Unit] = None


  /**
   * This method is invoked in a test run before any test threads have
   * started.
   *
   */
  private def runInitFunction {
    initFunction match {
      case Some(f) => f()
      case _ =>
    }
  }

  /////////////////////// init handler end//////////////////////////////



  /////////////////////// finish handler end //////////////////////////////


  def finish(f: => Unit) {finishFunction = Some(f _)}

  private var finishFunction: Option[() => Unit] = None

  /**
   * This method is invoked in a test after after all test threads have
   * finished.
   *
   */
  private def runFinishFunction {
    finishFunction match {
      case Some(f) => f()
      case _ =>
    }
  }

  /////////////////////// finish handler end //////////////////////////////


  /**
   * This method is called right after a new testcase thread is created by
   * the       { @link TestFramework }. It provides initial values for
   * { @link # currentTestCase } and       { @link # threads }.
   */
  def hello() {
    //MultithreadedTestCase.currentTestCase set this
    lock.synchronized {threadsWithTickCounts.put(Thread.currentThread, 0)}
  }

  /**
   * This method is called just before a testcase thread completes.
   * It cleans out       { @link # currentTestCase } and       { @link # threads }.
   */
  def goodbye() {
    lock.synchronized {threadsWithTickCounts -= Thread.currentThread}
    //MultithreadedTestCase.currentTestCase set null
  }


  /////////////////////// clock tick management start //////////////////////////////

  /**
   * Force this thread to block until the thread metronome reaches the
   * specified value, at which point the thread is unblocked.
   *
   * @param c the tick value to wait for
   */
  def waitForTick(c: Int) {
    lock.synchronized {
      threadsWithTickCounts.put(Thread.currentThread(), c);
      while (!failed && clock < c)
        try {
          if (trace) {
            println(Thread.currentThread().getName() + " is waiting for time " + c)
            println("the current tick is: " + tick)
          }
          lock.wait
        } catch {
          case e: InterruptedException => throw new AssertionError(e)
        }
      if (failed) throw new IllegalStateException("Clock never reached " + c)
      if (trace) println("Releasing " + Thread.currentThread.getName + " at time " + clock)
    }

  }

  /**
   * Gets the current value of the thread metronome. Primarily useful in
   * assert statements.
   *
   * @see # assertTick ( int )
   *
   * @return the current tick value
   */
  def tick: Int = lock.synchronized {clock}


  // =======================================
  // -- Components for freezing the clock --
  // - - - - - - - - - - - - - - - - - - - -

  /**
   * Read locks are acquired when clock is frozen and must be
   * released before the clock can advance in a waitForTick().
   */
  val clockLock = new ReentrantReadWriteLock()

  /**
   * When the clock is frozen, it will not advance even when all threads
   * are blocked. Use this to block the current thread with a time limit,
   * but prevent the clock from advancing due to a       { @link # waitForTick ( int ) } in
   * another thread. This statements that occur when clock is frozen should be
   * followed by       { @link # unfreezeClock ( ) } in the same thread.
   */
  def freezeClock {clockLock.readLock.lock}

  def withClockFrozen(f: => Unit) {
    freezeClock
    f
    unfreezeClock
  }

  /**
   * Unfreeze a clock that has been frozen by       { @link # freezeClock ( ) }. Both
   * methods must be called from the same thread.
   */
  def unfreezeClock() {clockLock.readLock().unlock()}

  /**
   * Check if the clock has been frozen by any threads.
   */
  def isClockFrozen: Boolean = clockLock.getReadLockCount > 0

  /////////////////////// clock tick management end //////////////////////////////


  // ===============================
  // -- Customized Wait Functions --
  // - - - - - - - - - - - - - - - -

//
//  /**
//   * Calling this method from one of the test threads may cause the
//   * thread to yield. Use this between statements to generate more
//   * interleavings.
//   */
//  def mayYield() {mayYield(0.5)}
//
//  /**
//   * Calling this method from one of the test threads may cause the
//   * thread to yield. Use this between statements to generate more
//   * interleavings.
//   *
//   * @param probability
//   * 			(a number between 0 and 1) the likelihood that Thread.yield() is called
//   */
//  def mayYield(probability: Double) {
//    if (MultithreadedTestCase.mtcRandomizer.get().nextDouble() < probability) Thread.`yield`
//  }



  def getStackTraces = {
    val sw = new StringWriter()
    val out = new PrintWriter(sw)
    for (t <- threads) {
      out.println(t.getName + " " + t.getState)
      for (st <- t.getStackTrace) {
        out.println("  " + st)
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  def runMultiThreadedTest(reporter: Reporter) {
    def getInt(prop: String, default: Int): Int = Integer.getInteger(prop, default).intValue
    runMultiThreadedTest(reporter,
      getInt(CLOCKPERIOD_KEY, DEFAULT_CLOCKPERIOD),
      getInt(RUNLIMIT_KEY, DEFAULT_RUNLIMIT))
  }

  /**
   * Run multithreaded test case once.
   *
   * @param clockPeriod
   * 			  The period (in ms) between checks for the clock (or null for
   * 			  default or global setting)
   * @param runLimit
   * 			  The limit to run the test in seconds (or null for default or
   * 			  global setting)
   */
  def runMultiThreadedTest(reporter: Reporter, clockPeriod: Int, runLimit: Int) {

    def buildReport(ex:Option[Throwable]) = {
      new Report(getClass.getName, getClass.getName, ex, None)
    }

    reporter.testStarting(buildReport(None))


    // invoke initialize method before each run
    runInitFunction
    clock = 0

    // invoke each thread method in a seperate thread
    startThreads

    // start and add clock thread
    val clockThread = startClock(clockPeriod, runLimit)

    // wait until all threads have ended
    waitForMethodThreads(threads + clockThread)

    // invoke finish at the end of each run
    runFinishFunction

    if( error.isEmpty ) reporter.testSucceeded(buildReport(None))
    else reporter.testSucceeded(buildReport(Some(error.peek)))
  }

  /**
   * Invoke each of the thread methods in a seperate thread and
   * place them all in a common (new) thread group. As a side-effect
   * all the threads are placed in the 'threads' LinkedList parameter,
   * and any errors detected are placed in the 'error' array parameter.
   *
   * @param test
   * 			The test case containing the thread methods
   * @param methods
   * 			Collection of the methods to be invoked
   * @param threads
   * 			By the time this method returns, this parameter will
   * 			contain all the test case threads
   * @param error
   * 			By the time this method returns, this parameter will
   * 			contains the first error thrown by one of the threads.
   * @return
   * The thread group for all the newly created test case threads
   */
  private def startThreads {
    threads.foreach {
      t => {
        start(t)
        threadRegistration.acquireUninterruptibly
      }
    }
    (threadGroup, threads)
  }

  /**
   * Start and return a clock thread which periodically checks all the test case
   * threads and regulates them.
   *
   * <p>
   * If all the threads are blocked and at least one is waiting for a tick, the clock
   * advances to the next tick and the waiting thread is notified. If none of the
   * threads are waiting for a tick or in timed waiting, a deadlock is detected. The
   * clock thread times out if a thread is in runnable or all are blocked and one is
   * in timed waiting for longer than the runLimit.
   *
   * @param test
   * 			the test case the clock thread is regulating
   * @param threadGroup
   * 			the thread group containing the running thread methods
   * @param error
   * 			an array containing any Errors/Exceptions that occur in thread methods
   * 			or that are thrown by the clock thread
   * @param clockPeriod
   * 			The period (in ms) between checks for the clock (or null for
   * 			default or global setting)
   * @param runLimit
   * 			The limit to run the test in seconds (or null for default or
   * 			global setting)
   * @return
   * The (already started) clock thread
   */
  def startClock(clockPeriod: Int, runLimit: Int): Thread = {
    // hold a reference to the current thread. This thread
    // will be waiting for all the test threads to finish. It
    // should be interrupted if there is an deadlock or timeout
    // in the clock thread
    start(ClockThread( /* mainThread= */ Thread.currentThread, clockPeriod, runLimit))
  }

  def start(t: Thread): Thread = {
    println("starting thread: " + t.getName)
    t.start
    t
  }

  /**
   * Wait for all of the test case threads to complete, or for one
   * of the threads to throw an exception, or for the clock thread to
   * interrupt this (main) thread of execution. When the clock thread
   * or other threads fail, the error is placed in the shared error array
   * and thrown by this method.
   *
   * @param threads
   * 			List of all the test case threads and the clock thread
   * @param error
   * 			an array containing any Errors/Exceptions that occur in thread methods
   * 			or that are thrown by the clock thread
   * @throws Throwable
   * 			The first error or exception that is thrown by one of the threads
   */
  private def waitForMethodThreads(threads: List[Thread]) {
    //println("waiting for threads: " + threads)

    def waitForThread(t: Thread) {
      //println("waiting for: " + t.getName + " which is in state:" + t.getState)
      try {
        if (t.isAlive() && error.peek != null) {
          println("stopping thread: " + t.getName)
          t.stop()
        }
        else {
          println("joining thread: " + t.getName)
          t.join()
        }
      } catch {
        case e: InterruptedException => {
          error.peek match {
            case null => throw new AssertionError(e)
            case _ => throw error.peek
          }
        }
      }
    }

    threads.foreach(waitForThread)

    error.peek match {
      case null =>
      case e => throw e
    }
  }

  /**
   * Stop all test case threads and clock thread, except the thread from
   * which this method is called. This method is used when a thread is
   * ready to end in failure and it wants to make sure all the other
   * threads have ended before throwing an exception.
   *
   * @param threads
   * 			LinkedList of all the test case threads and the clock thread
   */
  def signalError() {
    //println("signaling error to all threads")
    val currentThread = Thread.currentThread()
    for (t <- threads) {
      //println("signaling error to " + t.getName)
      if (t != currentThread) {
        val assertionError = new AssertionError(t.getName + " killed by " + currentThread.getName)
        assertionError setStackTrace t.getStackTrace
        t stop assertionError
      }
    }
  }

  case class ClockThread(mainThread: Thread, clockPeriod: Int, runLimit: Int) extends Thread("Clock") {
    this setDaemon true

    val isJDK14 = System.getProperty("java.version").indexOf("1.4.") != -1

    var lastProgress = System.currentTimeMillis
    var deadlocksDetected = 0
    var readyToTick = 0

    override def run() {
      try {
        while (true) {
          Thread sleep clockPeriod
          if (!doIt) return
        }
      } catch {
        case t: Throwable => {
          println("Clock thread killed")
          t.printStackTrace
        }
      }
    }

    def doIt: Boolean = {

      // Attempt to get a write lock; this succeeds
      // if clock is not frozen
      def aquireWriteLock = {
        clockLock.writeLock.tryLock(1000L * runLimit, TimeUnit.MILLISECONDS)
      }

      def noProgressFail {
        lock.synchronized {
          failed = true
          lock.notifyAll()
          error.offer(new IllegalStateException("No progress"))
          mainThread.interrupt
        }
      }

      if (!aquireWriteLock) {
        noProgressFail
        return false
      }

      lock.synchronized {

        try {

          // Get the contents of the thread group
          val ths = new Array[Thread](threadGroup.activeCount() + 10)
          val tgCount = threadGroup.enumerate(ths, true)
          if (tgCount == 0) return false // all threads are done

          // will set to true to force a check for timeout conditions
          // and restart the loop
          var checkProgress = false

          // will set true if any thread is in state TIMED_WAITING
          var timedWaiting = false

          var nextTick = Integer.MAX_VALUE

          // examine the threads in the thread group; look for
          // next tick
          for (t <- ths; if (t != null)) {
            println("in thread group: " + t)
            if (!isJDK14) {
              try {
                if (trace) println(t.getName() + " is in state " + t.getState())
                if (t.getState() == Thread.State.RUNNABLE) checkProgress = true
                if (t.getState() == Thread.State.TIMED_WAITING) timedWaiting = true
              } catch {
                case e: Throwable =>
                  // JVM may not support Thread.State
                  checkProgress = false
                  timedWaiting = true
              }
            } else {
              // JVM does not support Thread.State
              checkProgress = false
              timedWaiting = true
            }
            val waitingFor = threadsWithTickCounts(t)
            if (waitingFor > clock) nextTick = Math.min(nextTick, waitingFor)
          }

          // If not waiting for anything, but a thread is in
          // TIMED_WAITING, then check progress and loop again
          if (nextTick == Integer.MAX_VALUE && timedWaiting) checkProgress = true

          // Check for timeout conditions and restart the loop
          if (checkProgress) {
            if (readyToTick > 0) {
              if (trace) println("Was Ready to tick too early")
              readyToTick = 0
            }
            val now = System.currentTimeMillis()
            if (now - lastProgress > 1000L * runLimit) {
              failed = true
              lock.notifyAll
              error offer new IllegalStateException("No progress")
              mainThread.interrupt
              return false
            }
            deadlocksDetected = 0
            return true
          }

          // Detect deadlock
          if (nextTick == Integer.MAX_VALUE) {
            if (readyToTick > 0) {
              if (trace) println("Was Ready to tick too early")
              readyToTick = 0
            }

            deadlocksDetected += 1

            if ((deadlocksDetected) < 50) {
              if (deadlocksDetected % 10 == 0 && trace)
                println("[Detecting deadlock... " + deadlocksDetected + " trys]")
              return true
            }

            if (trace) println("Deadlock!")

            failed = true;
            error.offer(new IllegalStateException("Apparent deadlock\n" + getStackTraces))
            mainThread.interrupt()
            return false
          }

          deadlocksDetected = 0

          readyToTick += 1
          if (readyToTick < 2) return true

          readyToTick = 0

          // Advance to next tick
          clock = nextTick
          lastProgress = System.currentTimeMillis

          // notify any threads that are waiting for this tick
          lock.notifyAll
          if (trace) println("Time is now " + clock)
        } finally {
          clockLock.writeLock.unlock
        }
      }
      true
    }
  }
}
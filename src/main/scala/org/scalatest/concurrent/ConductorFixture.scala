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
import org.scalatest.fixture.FixtureSuite

/**
 * Trait that can pass a new <code>Conductor</code> fixture into tests.
 *
 * <p>
 * Here's an example of the use of this trait to test the <code>ArrayBlockingQueue</code>
 * class from <code>java.util.concurrent</code>:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixtureFunSuite
 * import org.scalatest.concurrent.ConductorFixture
 * import org.scalatest.matchers.ShouldMatchers
 * import java.util.concurrent.ArrayBlockingQueue
 *
 * class ArrayBlockingQueueSuite extends FixtureFunSuite with ConductorFixture with ShouldMatchers {
 * 
 *   test("calling put on a full queue blocks the producer thread") { conductor => import conductor._
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
 * parameter to <code>thread("producer") { ... }</code>:
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
 * However, prior to the <code>whenFinished</code> call, both threads start and immediately block,
 * waiting on the <code>Conductor</code> to give them a green light to start executing their block.
 * <code>whenFinished</code> calls <code>conduct</code> on the <code>Conductor</code>. The <code>conduct</code<>
 * method will make sure all threads that were created (in this case, producer and consumer) have 
 * started and blocked, waiting for the green light. Once all threads are at the starting line (<em>i.e.</em>, have started
 * and blocked), the <code>conduct</code> method will give these threads the green light and they will
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
 * waiting for beat 1, it will wake up (<em>i.e.</em>, the <code>waitForBeat(1)</code> call will return), and
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
 * execute 17 should be (17). This will succeed and as this was the last statement in its block, the consumer will return
 * normally.
 * </p>
 *
 * <p>
 * If either the producer or consumer thread had completed abruptbly with an exception, the <code>conduct</code> method
 * (which was called by <code>whenFinished</code>) would have completed abruptly with an exception to indicate the test
 * failed. However, since both threads returned normally, <code>conduct</code> will return. Because <code>conduct</code> doesn't
 * throw an exception, <code>whenFinished</code> will execute the block of code passed as a by-name parameter to it: <code>buf should be ('empty)</code>.
 * This will succeed, because the queue is indeed empty at this point. The <code>whenFinished</code> method will then return, and
 * because the <code>whenFinished</code> call was the last statement in the test, the test completes successfully.
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
 * test("calling take on an empty queue blocks the consumer thread") { conductor => import conductor._
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
 * will at some point later increement the beat to 1. This will awaken the producer thread. It will return from its
 * <code>waitForBeat(1)</code> call and execute <code>buf put 42</code>. This will unblock the consumer thread, which will
 * take the 42, and so on.
 * </p>
 *
 * <p>
 * The difficulty with testing classes, traits, and libraries that are intended to be used by multiple threads, which
 * <code>Conductor</code> is designed to address, is the non-deterministic nature of thread scheduling. If you just
 * create a test in which one thread reads from an <code>ArrayBlockingQueue</code> and
 * another writes to it, you can't be sure that you have tested all possible interleavings of threads, no matter
 * how many times you run the test. The purpose of <code>Conductor</code>
 * is to enable you to write tests with deterministic interleavings of threads. If you write one test for each possible
 * interleaving of threads, then you can be sure you have all the scenarios tested. The two tests shown here, for example,
 * ensure that both the scenario in which a producer thread tries to write to a full queue and the scenario in which a
 * consumer thread tries to take from an empty queue are tested.
 * </p>
 *
 * @author Bill Venners
 */
trait ConductorFixture { this: FixtureSuite =>

  /**
   * Defines type <code>Fixture</code> to be <code>Conductor</code>.
   */
  type Fixture = Conductor
  
  /**
   * Creates a new <code>Conductor</code>, passes the <code>Conductor</code> to the
   * specified test function, and ensures that <code>conduct</code> gets invoked
   * on the <code>Conductor</code>.
   *
   * <p>
   * After the test function returns (so long as it returns normally and doesn't
   * complete abruptly with an exception), this method will determine whether the
   * <code>conduct</code> method has already been called (by invoking
   * <code>conductingHasBegun</code> on the <code>Conductor</code>). If not,
   * this method will invoke <code>conduct</code> to ensure that the
   * multi-threaded scenario is actually conducted.
   * </p>
   */
  def withFixture(test: OneArgTest) {
    val conductor = new Conductor
    test(conductor)
    if (!conductor.conductingHasBegun)
      conductor.conduct()
  }
}

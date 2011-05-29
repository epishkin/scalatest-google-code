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
package org.scalatest

import java.util.concurrent.atomic.AtomicReference

/**
 * Trait that can be mixed into suites that need code executed before and after executing the
 * suite. This trait allows code to be executed before and/or after all the tests and nested suites of a
 * suite are run. This trait overrides <code>run</code> (the main <code>run</code> method that
 * takes seven parameters, an optional test name, reporter, stopper, filter, configMap, optional distributor,
 * and tracker) and calls the code passed to the
 * <code>beforeAll</code> method, if any, then calls <code>super.run</code>. After the <code>super.run</code>
 * invocation completes, whether it returns normally or completes abruptly with an exception,
 * this trait's <code>run</code> method will execute the code passed to <code>afterAll</code>, if any.
 *
 * <p>
 * For example, the following <code>MasterSuite</code> mixes in <code>BeforeAndAfterAllFunctions</code> and
 * in the code passed to <code>beforeAll</code>, creates and writes to a temp file.
 * After all of the nested suites have executed, the code passed to <code>afterAll</code> is invoked, which
 * deletes the file:
 * </p>
 * 
 * <pre class="stHighlight">
 * import org.scalatest.SuperSuite
 * import org.scalatest.BeforeAndAfterAllFunctions
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 *
 * class MasterSuite extends Suite with BeforeAndAfterAllFunctions {
 *
 *   private val tempFileName = "temp.txt"
 *
 *   // Set up the temp file needed by the test
 *   beforeAll {
 *
 *     val fileName = configMap(tempFileName)
 *
 *     val writer = new FileWriter(fileName)
 *     try {
 *       writer.write("Hello, suite of tests!")
 *     }
 *     finally {
 *       writer.close()
 *     }
 *   }
 *
 *   override def nestedSuites =
 *     List(new OneSuite, new TwoSuite, new RedSuite, new BlueSuite)
 * 
 *   // Delete the temp file
 *   afterAll {
 *     val fileName = configMap(tempFileName))
 *     val file = new File(fileName)
 *     file.delete()
 *   }
 * }
 * </pre>
 *
 * <p>
 * Because the <code>BeforeAndAfterAllFunctions</code> trait invokes <code>super.run</code> to run the suite, you may need to
 * mix this trait in last to get the desired behavior. For example, this won't
 * work, because <code>BeforeAndAfterAllFunctions</code> is "super" to </code>FunSuite</code>:
 * </p>
 * <pre class="stHighlight">
 * class MySuite extends BeforeAndAfterAllFunctions with FunSuite
 * </pre>
 * <p>
 * You'd need to turn it around, so that <code>FunSuite</code> is "super" to <code>BeforeAndAfterAllFunctions</code>, like this:
 * </p>
 * <pre class="stHighlight">
 * class MySuite extends FunSuite with BeforeAndAfterAllFunctions
 * </pre>
 *
 * <strong>Note: This trait does not currently ensure that the code passed to <code>afterAll</code> is executed after
 * all the tests and nested suites are executed if a <code>Distributor</code> is passed. The
 * plan is to do that eventually, but in the meantime, be aware that the code passed to <code>afterAll</code> is
 * guaranteed to be run after all the tests and nested suites only when they are run
 * sequentially.</strong>
 *
 * @author Bill Venners
 */
trait BeforeAndAfterAllFunctions extends AbstractSuite {

  this: Suite =>

  private val beforeFunctionAtomic = new AtomicReference[Option[() => Any]](None)
  private val afterFunctionAtomic = new AtomicReference[Option[() => Any]](None)
  @volatile private var runHasBeenInvoked = false

  /**
   * Registers code to be executed before any of this suite's tests or nested suites are run.
   *
   * <p>
   * This trait's implementation
   * of <code>runTest</code> executes the code passed to this method before running
   * any tests or nested suites. Thus the code passed to this method can be used to set up a test fixture
   * needed by the entire suite.
   * </p>
   *
   * @throws NotAllowedException if invoked more than once on the same <code>Suite</code> or if
   *                             invoked after <code>run</code> has been invoked on the <code>Suite</code>
   */
  protected def beforeAll(fun: => Any) {
    if (runHasBeenInvoked)
      throw new NotAllowedException("You cannot call beforeEach after run has been invoked (such as, from within a test). It is probably best to move it to the top level of the Suite class so it is executed during object construction.", 0)
    val success = beforeFunctionAtomic.compareAndSet(None, Some(() => fun))
    if (!success)
      throw new NotAllowedException("You are only allowed to call beforeEach once in each Suite that mixes in BeforeAndAfterEachFunctions.", 0)
  }

  protected def afterAll() = ()
  /**
   * Registers code to be executed after all of this suite's tests and nested suites have
   * been run.
   *
   * <p>
   * This trait's implementation of <code>runTest</code> executes the code passed to this method after running
   * each test. Thus the code passed to this method can be used to tear down a test fixture
   * needed by the entire suite.
   * </p>
   *
   * @throws NotAllowedException if invoked more than once on the same <code>Suite</code> or if
   *                             invoked after <code>run</code> has been invoked on the <code>Suite</code>
   */
  protected def afterAll(fun: => Any) {
    if (runHasBeenInvoked)
      throw new NotAllowedException("You cannot call afterEach after run has been invoked (such as, from within a test. It is probably best to move it to the top level of the Suite class so it is executed during object construction.", 0)
    val success = afterFunctionAtomic.compareAndSet(None, Some(() => fun))
    if (!success)
      throw new NotAllowedException("You are only allowed to call beforeEach once in each Suite that mixes in BeforeAndAfterEachFunctions.", 0)
  }

  /**
   * Execute a suite surrounded by calls to <code>beforeAll</code> and <code>afterAll</code>.
   *
   * <p>
   * This trait's implementation of this method ("this method") invokes <code>beforeAll(Map[String, Any])</code>
   * before executing any tests or nested suites and <code>afterAll(Map[String, Any])</code>
   * after executing all tests and nested suites. It runs the suite by invoking <code>super.run</code>, passing along
   * the seven parameters passed to it.
   * </p>
   *
   * <p>
   * If any invocation of <code>beforeAll</code> completes abruptly with an exception, this
   * method will complete abruptly with the same exception. If any call to
   * <code>super.run</code> completes abruptly with an exception, this method
   * will complete abruptly with the same exception, however, before doing so, it will
   * invoke <code>afterAll</code>. If <cod>afterAll</code> <em>also</em> completes abruptly with an exception, this
   * method will nevertheless complete abruptly with the exception previously thrown by <code>super.run</code>.
   * If <code>super.run</code> returns normally, but <code>afterAll</code> completes abruptly with an
   * exception, this method will complete abruptly with the same exception.
   * </p>
  */
  abstract override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
                       configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    runHasBeenInvoked = true

    var thrownException: Option[Throwable] = None

    beforeFunctionAtomic.get match {
      case Some(fun) => fun()
      case None =>
    }

    try {
      super.run(testName, reporter, stopper, filter, configMap, distributor, tracker)
    }
    catch {
      case e: Exception => thrownException = Some(e)
    }
    finally {
      try {
        // Make sure that code passed to afterAll, if any, is called even if run completes abruptly.
        afterFunctionAtomic.get match {
          case Some(fun) => fun()
          case None =>
        }
        thrownException match {
          case Some(e) => throw e
          case None =>
        }
      }
      catch {
        case laterException: Exception =>
          thrownException match { // If both run and afterAll throw an exception, report the test exception
            case Some(earlierException) => throw earlierException
            case None => throw laterException
          }
      }
    }
  }
}

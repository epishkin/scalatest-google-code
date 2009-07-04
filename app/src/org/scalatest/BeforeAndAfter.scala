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

/**
 * Trait that can be mixed into suites that need methods invoked before and after executing the
 * suite, and/or before and after running each test. This trait facilitates a style of testing in which mutable
 * fixture objects held in instance variables are replaced or reinitialized before each test or
 * suite. Here's an example:
 * 
 * <pre>
 * import org.scalatest._
 * import scala.collection.mutable.ListBuffer
 *
 * class MySuite extends BeforeAndAfter {
 *
 *   // Fixtures as reassignable variables and mutable objects
 *   var sb: StringBuilder = _
 *   val lb = new ListBuffer[String]
 * 
 *   override def beforeEach() {
 *     sb = new StringBuilder("ScalaTest is ")
 *     lb.clear()
 *   }
 *
 *   def testEasy() {
 *     sb.append("easy!")
 *     assert(sb.toString === "ScalaTest is easy!")
 *     assert(lb.isEmpty)
 *     lb += "sweet"
 *   }
 *
 *   def testFun() {
 *     sb.append("fun!")
 *     assert(sb.toString === "ScalaTest is fun!")
 *     assert(lb.isEmpty)
 *   }
 * }
 * </pre>
 * 
 * <p>
 * Because this trait invokes <code>super.run</code> to run the suite and <code>super.runTest</code> to
 * run each test, you may need to mix this trait in last to get the desired behavior. For example, this won't
 * work, because <code>BeforeAndAfter</code> is "super" to </code>FunSuite</code>:
 * </p>
 * <pre>
 * class MySuite extends BeforeAndAfter with FunSuite 
 * </pre>
 * <p>
 * You'd need to turn it around, so that <code>FunSuite</code> is "super" to <code>BeforeAndAfter</code>, like this:
 * </p>
 * <pre>
 * class MySuite extends FunSuite with BeforeAndAfter
 * </pre>
 * <p>
 *
 * <p>
 * If you want to do something before and after both the tests and the nested <code>Suite</code>s,
 * then you can override <code>run</code> itself, or use the <code>beforeAll</code>
 * and <code>afterAll</code> methods of <code>BeforeAndAfter</code>.
 * </p>
 *
 * @author Bill Venners
 */
trait BeforeAndAfter extends RunMethods {

  this: Suite =>
  
  /**
   * Defines a method to be run before each of this suite's tests. This trait's implementation
   * of <code>runTest</code> invokes this method before running
   * each test, thus this method can be used to set up a test fixture
   * needed by each test. This trait's implementation of this method does nothing.
   */
  protected def beforeEach() = ()
  
  /**
   * Defines a method to be run after each of this suite's tests. This trait's implementation
   * of <code>runTest</code> invokes this method after running
   * each test, thus this method can be used to tear down a test fixture
   * needed by each test. This trait's implementation of this method does nothing.
   */
  protected def afterEach() = ()
  
  /**
   * Defines a method to be run before any of this suite's tests or nested suites are run. This trait's implementation
   * of <code>run</code> invokes this method before executing
   * any tests or nested suites, thus this method can be used to set up a test fixture
   * needed by the entire suite. This trait's implementation of this method does nothing.
   */
  protected def beforeAll() = ()
  
  /**
   * Defines a method to be run after all of this suite's tests and nested suites have
   * been run. This trait's implementation
   * of <code>run</code> invokes this method after executing
   * all tests and nested suites, thus this method can be used to tear down a test fixture
   * needed by the entire suite. This trait's implementation of this method does nothing.
   */
  protected def afterAll() = ()
  
  /**
   * Run a test surrounded by calls to <code>beforeEach</code> and <code>afterEach</code>.
   * This trait's implementation of this method ("this method") invokes <code>beforeEach</code>
   * before running each test and <code>afterEach</code>
   * after running each test. It runs each test by invoking <code>super.runTest</code>, passing along
   * the four parameters passed to it.
   * 
   * <p>
   * If any invocation of <code>beforeEach</code> completes abruptly with an exception, this
   * method will complete abruptly with the same exception. If any call to
   * <code>super.runTest</code> completes abruptly with an exception, this method
   * will complete abruptly with the same exception, however, before doing so, it will
   * invoke <code>afterEach</code>. If <cod>afterEach</code> <em>also</em> completes abruptly with an exception, this 
   * method will nevertheless complete abruptly with the exception previously thrown by <code>super.runTest</code>.
   * If <code>super.runTest</code> returns normally, but <code>afterEach</code> completes abruptly with an
   * exception, this method will complete abruptly with the same exception.
   * </p>
  */
  abstract override def runTest(testName: String, reporter: Reporter, stopper: Stopper, goodies: Map[String, Any], tracker: Tracker) {

    var thrownException: Option[Throwable] = None

    beforeEach()
    try {
      super.runTest(testName, reporter, stopper, goodies, tracker)
    }
    catch {
      case e: Exception => thrownException = Some(e)
    }
    finally {
      try {
        afterEach() // Make sure that afterEach is called even if runTest completes abruptly.
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

  /**
   * Execute a suite surrounded by calls to <code>beforeAll</code> and <code>afterAll</code>.
   * This trait's implementation of this method ("this method") invokes <code>beforeAll</code>
   * before executing any tests or nested suites and <code>afterAll</code>
   * after executing all tests and nested suites. It runs the suite by invoking <code>super.run</code>, passing along
   * the seven parameters passed to it.
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
                       goodies: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    var thrownException: Option[Throwable] = None

    beforeAll()
    try {
      super.run(testName, reporter, stopper, filter, goodies, distributor, tracker)
    }
    catch {
      case e: Exception => thrownException = Some(e)
    }
    finally {
      try {
        afterAll() // Make sure that afterAll is called even if run completes abruptly.
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

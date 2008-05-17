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
 * suite, and/or before and after running each test. The "imp" in <code>ImpSuite</code> stands
 * for <em>imperative</em>, because this trait facilitates a style of testing in which mutable
 * fixture objects held in instance variables are replaced or reinitialized before each test or
 * suite. Here's an example:
 * 
 * <pre>
 * import org.scalatest._
 * import scala.collection.mutable.ListBuffer
 *
 * class MySuite extends ImpSuite {
 *
 *   // Fixtures as reassignable variables and mutable objects
 *   var sb = _
 *   val lb = new ListBuffer[String]
 * 
 *   override def before() {
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
 * Because this trait invokes <code>super.execute</code> to execute the suite and <code>super.runTest</code> to
 * run each test, you may mix this trait in last to get the desired behavior. For example, this won't
 * work:
 * </p>
 * <pre>
 * class MySuite extends ImpSuite with FunSuite 
 * </pre>
 * <p>
 * You'd need to say:
 * </p>
 * <pre>
 * class MySuite extends FunSuite with ImpSuite
 * </pre>
 */
trait ImpSuite extends Suite {
  
  /**
   * Defines a method to be run before each of this suite's tests. This trait's implementation
   * of <code>runTest</code> invokes this method before running
   * each test, thus this method can be used to set up a test fixture
   * needed by each test. This trait's implementation of this method does nothing.
   */
  protected def before() = ()
  
  /**
   * Defines a method to be run after each of this suite's tests. This trait's implementation
   * of <code>runTest</code> invokes this method after running
   * each test, thus this method can be used to tear down a test fixture
   * needed by each test. This trait's implementation of this method does nothing.
   */
  protected def after() = ()
  
  /**
   * Defines a method to be run before any of this suite's tests or nested suites are executed. This trait's implementation
   * of <code>execute</code> invokes this method before executing
   * any tests or nested suites, thus this method can be used to set up a test fixture
   * needed by the entire suite. This trait's implementation of this method does nothing.
   */
  protected def beforeSuite() = ()
  
  /**
   * Defines a method to be run after all of this suite's tests and nested suites have
   * been executed. This trait's implementation
   * of <code>execute</code> invokes this method after executing
   * all tests and nested suites, thus this method can be used to tear down a test fixture
   * needed by the entire suite. This trait's implementation of this method does nothing.
   */
  protected def afterSuite() = ()
  
  /**
   * Run a test surrounded by calls to <code>before</code> and <code>after</code>.
   * This trait's implementation of this method ("this method") invokes <code>before</code>
   * before running each test and <code>after</code>
   * after running each test. It runs each test by invoking <code>super.runTest</code>, passing along
   * the four parameters passed to it.
   * 
   * <p>
   * If any invocation of <code>before</code> completes abruptly with an exception, this
   * method will complete abruptly with the same exception. If any call to
   * <code>super.runTest</code> completes abruptly with an exception, this method
   * will complete abruptly with the same exception, however, before doing so, it will
   * invoke <code>after</code>. If <cod>after</code> <em>also</em> completes abruptly with an exception, this 
   * method will nevertheless complete abruptly with the exception previously thrown by <code>super.runTest</code>.
   * If <code>super.runTest</code> returns normally, but <code>after</code> completes abruptly with an
   * exception, this method will complete abruptly with the same exception.
   * </p>
  */
  override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any]) {
    before()
    try {
      super.runTest(testName, reporter, stopper, properties)
      after()
    }
    catch {
     case e: Exception =>
       try {
         after() // Make sure that after is called even if the runTest completes abruptly.
       }
       finally {
         throw e // If both runTest and after throw an exception, report the test exception
       }
    }
  }

  /**
   * Execute a suite surrounded by calls to <code>beforeSuite</code> and <code>afterSuite</code>.
   * This trait's implementation of this method ("this method") invokes <code>beforeSuite</code>
   * before executing any tests or nested suites and <code>afterSuite</code>
   * after executing all tests and nested suites. It executes the suite by invoking <code>super.execute</code>, passing along
   * the seven parameters passed to it.
   * 
   * <p>
   * If any invocation of <code>beforeSuite</code> completes abruptly with an exception, this
   * method will complete abruptly with the same exception. If any call to
   * <code>super.execute</code> completes abruptly with an exception, this method
   * will complete abruptly with the same exception, however, before doing so, it will
   * invoke <code>afterSuite</code>. If <cod>afterSuite</code> <em>also</em> completes abruptly with an exception, this 
   * method will nevertheless complete abruptly with the exception previously thrown by <code>super.execute</code>.
   * If <code>super.execute</code> returns normally, but <code>afterSuite</code> completes abruptly with an
   * exception, this method will complete abruptly with the same exception.
   * </p>
  */
  override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
                       properties: Map[String, Any], distributor: Option[Distributor]) {
    beforeSuite()
    try {
      super.execute(testName, reporter, stopper, includes, excludes, properties, distributor)
      afterSuite()
    }
    catch {
     case e: Exception =>
       try {
         afterSuite() // Make sure that afterSuite is called even if execute completes abruptly.
       }
       finally {
         throw e // If both execute and afterSuite throw an exception, report the test exception
       }
    }
  }
}

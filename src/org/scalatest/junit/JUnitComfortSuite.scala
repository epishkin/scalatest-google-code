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
package org.scalatest.junit

import org.junit.runner.RunWith

/* A blast from the past.
 *
 * @author Bill Venners
 */
@RunWith(classOf[JUnitRunner])
class JUnitComfortSuite extends Suite { // TODO: Add OneInstancePerTest when that gets done

  /**
   * Defines a method to be run before each of this suite's tests. This trait's implementation
   * of <code>runTest</code> invokes this method before running
   * each test, thus this method can be used to set up a test fixture
   * needed by each test. This trait's implementation of this method does nothing.
   */
  protected def setUp() = ()
  
  /**
   * Defines a method to be run after each of this suite's tests. This trait's implementation
   * of <code>runTest</code> invokes this method after running
   * each test, thus this method can be used to tear down a test fixture
   * needed by each test. This trait's implementation of this method does nothing.
   */
  protected def tearDown() = ()
  
  /**
   * Run a test surrounded by calls to <code>setUp</code> and <code>tearDown</code>.
   * This trait's implementation of this method ("this method") invokes <code>setUp</code>
   * before running each test and <code>tearDown</code>
   * after running each test. It runs each test by invoking <code>super.runTest</code>, passing along
   * the four parameters passed to it.
   * 
   * <p>
   * If any invocation of <code>setUp</code> completes abruptly with an exception, this
   * method will complete abruptly with the same exception. If any call to
   * <code>super.runTest</code> completes abruptly with an exception, this method
   * will complete abruptly with the same exception, however, before doing so, it will
   * invoke <code>tearDown</code>. If <cod>tearDown</code> <em>also</em> completes abruptly with an exception, this 
   * method will nevertheless complete abruptly with the exception previously thrown by <code>super.runTest</code>.
   * If <code>super.runTest</code> returns normally, but <code>tearDown</code> completes abruptly with an
   * exception, this method will complete abruptly with the same exception.
   * </p>
  */
  override def runTest(testName: String, reporter: Reporter, stopper: Stopper, goodies: Map[String, Any], tracker: Tracker) {

    var thrownException: Option[Throwable] = None

    setUp()
    try {
      super.runTest(testName, reporter, stopper, goodies, tracker)
    }
    catch {
      case e: Exception => thrownException = Some(e)
    }
    finally {
      try {
        tearDown() // Make sure that afterEach is called even if runTest completes abruptly.
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

/**
 * <strong>Note: <code>JUnit3Suite</code> has been deprecated, and will be removed in a future version of ScalaTest. Please
 * change to using <code>JUnitComforSuite</code> instead. <code>JUnitComfortSuite</code> does not extend <code>junit.framework.TestCase</code>. In versions 
 * of ScalaTest prior to 0.9.5, <code>JUnit3Suite</code> extended <code>TestCase</code> so that it could be run by a JUnit 3 runner. In
 * 0.9.6, ScalaTest's <code>execute</code> methods were renamed to <code>run</code>, which is not compatible with <code>TestCase</code>.
 * As a result the goal of providing a trait in ScalaTest that can either run with ScalaTest and JUnit 3 was dropped. Instead, the
 * <code>JUnitComfortSuite</code> trait can give you that comfortable feeling of using JUnit 3-like syntax, and it can be run with
 * either ScalaTest or JUnit 4.</strong>
 */
@deprecated
class JUnit3Suite extends JUnitComfortSuite

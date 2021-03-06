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
package org.scalatest.junit;

import org.scalatest._
import org.scalatest.Suite
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.notification.RunListener
import org.junit.runner.notification.Failure
import org.junit.runner.Description
import org.junit.runner.Result
import org.scalatest.events._
import java.util.Collections
import java.util.HashSet
import java.util.regex.Pattern

import java.lang.reflect

/**
 * <p>
 * A wrapper to allow JUnit tests to be run by the ScalaTest runner.
 * </p>
 *
 * <p>
 * Instances of this trait are not thread safe.
 * </p>
 *
 * @author Bill Venners
 * @author Daniel Watson
 * @author Joel Neely
 * @author George Berger
 */
class JUnitWrapperSuite(junitClassName: String, loader: ClassLoader) extends Suite {

  // TODO: This may need to be made thread safe, because who
  // knows what Thread JUnit will fire through this
  private var theTracker = new Tracker
  private val junitClass = Class.forName(junitClassName, false, loader)

  override def run(testName: Option[String],
                   report: Reporter, 
                   stopper: Stopper,
                   filter: Filter,
                   config: Map[String, Any],
                   distributor: Option[Distributor], 
                   tracker: Tracker) {

    theTracker = tracker

    val jUnitCore = new JUnitCore

    jUnitCore.addListener(new MyRunListener(report, config, tracker))

    jUnitCore.run(junitClass)
  }

  override def expectedTestCount(filter: Filter): Int = {
    getRequest.getRunner.getDescription.testCount
  }

  //
  // Retrieves a JUnit4 Request object for the junit test
  // class.
  //
  // The JUnit Request.classes() method has different
  // signatures in different versions of JUnit4, so reflection
  // is used here to identify and use whichever version is
  // available in the junit jar on the user's classpath.
  //
  def getRequest(): Request = {
    var classArgs = new Array[Class[_]](1)
    classArgs(0) = junitClass

    val requestClass =
      try { Class.forName("org.junit.runner.Request") }
      catch {
        case e: ClassNotFoundException =>
          throw new RuntimeException(
            "Could not find class: org.junit.runner.Request.  " +
            "Note: a junit4 jar must be included on " +
            "the classpath if using the -j option.")
      }

    try { // works for junit-4.4.jar
      val method = requestClass.getMethod("classes", classOf[String],
                                          classOf[Array[Class[_]]])
      val result = method.invoke(null, "", classArgs)

      result.asInstanceOf[Request]
    }
    catch {
      case e: NoSuchMethodException =>
        try { // works for junit-4.6.jar
          val method = requestClass.getMethod("classes",
                                              classOf[Array[Class[_]]])
          val result = method.invoke(null, classArgs)

          result.asInstanceOf[Request]
        }
        catch {
          case e: NoSuchMethodException =>
            throw new RuntimeException("Could not find method " +
                                     "org.junit.runner.Request.classes. " +
                                     "Possibly a junit version problem. " +
                                     "Try junit-4.6.jar.")
        }
    }
  }

  /**
   * Throws <code>UnsupportedOperationException</code>, because this method is unused by this
   * class, given this class's <code>run</code> method delegates to JUnit to run
   * its tests.
   *
   * <p>
   * The main purpose of this method implementation is to render a compiler error an attempt
   * to mix in a trait that overrides <code>withFixture</code>. Because this
   * trait does not actually use <code>withFixture</code>, the attempt to mix
   * in behavior would very likely not work.
   * </p>
   *
   *
   * @param test the no-arg test function to run with a fixture
   */
  override final protected def withFixture(test: NoArgTest) {
     throw new UnsupportedOperationException
  }

  /**
   * Throws <code>UnsupportedOperationException</code>, because this method is unused by this
   * class, given this class's <code>run</code> method delegates to JUnit to run
   * its tests.
   *
   * <p>
   * The main purpose of this method implementation is to render a compiler error an attempt
   * to mix in a trait that overrides <code>runNestedSuites</code>. Because this
   * trait does not actually use <code>runNestedSuites</code>, the attempt to mix
   * in behavior would very likely not work.
   * </p>
   *
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param filter a <code>Filter</code> with which to filter tests based on their tags
   * @param configMap a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @param distributor an optional <code>Distributor</code>, into which to put nested <code>Suite</code>s to be run
   *              by another entity, such as concurrently by a pool of threads. If <code>None</code>, nested <code>Suite</code>s will be run sequentially.
   * @param tracker a <code>Tracker</code> tracking <code>Ordinal</code>s being fired by the current thread.
   *
   * @throws UnsupportedOperationException always.
   */
  override final protected def runNestedSuites(reporter: Reporter, stopper: Stopper, filter: Filter,
                                configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    throw new UnsupportedOperationException
  }

  /**
   * Throws <code>UnsupportedOperationException</code>, because this method is unused by this
   * class, given this class's <code>run</code> method delegates to JUnit to run
   * its tests.
   *
   * <p>
   * The main purpose of this method implementation is to render a compiler error an attempt
   * to mix in a trait that overrides <code>runTests</code>. Because this
   * trait does not actually use <code>runTests</code>, the attempt to mix
   * in behavior would very likely not work.
   * </p>
   *
   * @param testName an optional name of one test to run. If <code>None</code>, all relevant tests should be run.
   *                 I.e., <code>None</code> acts like a wildcard that means run all relevant tests in this <code>Suite</code>.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param filter a <code>Filter</code> with which to filter tests based on their tags
   * @param configMap a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @param distributor an optional <code>Distributor</code>, into which to put nested <code>Suite</code>s to be run
   *              by another entity, such as concurrently by a pool of threads. If <code>None</code>, nested <code>Suite</code>s will be run sequentially.
   * @param tracker a <code>Tracker</code> tracking <code>Ordinal</code>s being fired by the current thread.
   * @throws UnsupportedOperationException always.
   */
  override protected final def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
                            configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    throw new UnsupportedOperationException
  }

  /**
   * Throws <code>UnsupportedOperationException</code>, because this method is unused by this
   * class, given this class's <code>run</code> method delegates to JUnit to run
   * its tests.
   *
   * <p>
   * The main purpose of this method implementation is to render a compiler error an attempt
   * to mix in a trait that overrides <code>runTest</code>. Because this
   * trait does not actually use <code>runTest</code>, the attempt to mix
   * in behavior would very likely not work.
   * </p>
   *
   * @param testName the name of one test to run.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param configMap a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @param tracker a <code>Tracker</code> tracking <code>Ordinal</code>s being fired by the current thread.
   * @throws UnsupportedOperationException always.
   */
  override protected final def runTest(testName: String, reporter: Reporter, stopper: Stopper, configMap: Map[String, Any], tracker: Tracker) {

        throw new UnsupportedOperationException
  }
}

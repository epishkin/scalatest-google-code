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
package org.scalatest.events

import org.scalatest._
import java.util.Date

/**
 * Deprecated singleton object for the <a href="TestStarting.html"><code>TestStarting</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>TestStarting</code> objects.
 * This object contains methods that were in the <code>TestStarting</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use TestStarting with named and/or default parameters instead.")
object DeprecatedTestStarting {

  /**
   * Constructs a new <code>TestStarting</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that is starting
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that is starting
   * @param testName the name of the test that is starting
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param rerunner an optional <code>Rerunner</code> that can be used to rerun the test that is starting (if <code>None</code>
   *        is passed, the test cannot be rerun)
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>TestStarting</code> event
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestStarting</code> instance initialized with the passed and default values
   *
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    formatter: Option[Formatter],
    rerunner: Option[Rerunner],
    payload: Option[Any]
  ): TestStarting = {
    TestStarting(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, formatter, None, rerunner, payload, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestStarting</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that is starting
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that is starting
   * @param testName the name of the test that is starting
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param rerunner an optional <code>Rerunner</code> that can be used to rerun the test that is starting (if <code>None</code>
   *        is passed, the test cannot be rerun)
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestStarting</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    formatter: Option[Formatter],
    rerunner: Option[Rerunner]
  ): TestStarting = {
    TestStarting(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, formatter, None, rerunner, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestStarting</code> event with the passed parameters, passing <code>None</code> as the
   * <code>rerunner</code>, <code>None</code> as the <code>payload</code>, the current threads name as <code>threadname</code>,
   * and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that is starting
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that is starting
   * @param testName the name of the test that is starting
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestStarting</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    formatter: Option[Formatter]
  ): TestStarting = {
    TestStarting(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, formatter, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestStarting</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>rerunner</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that is starting
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that is starting
   * @param testName the name of the test that is starting
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestStarting</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String
  ): TestStarting = {
    TestStarting(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

/**
 * Deprecated singleton object for the <a href="TestSucceeded.html"><code>TestSucceeded</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>TestSucceeded</code> objects.
 * This object contains methods that were in the <code>TestSucceeded</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use TestSucceeded with named and/or default parameters instead.")
object DeprecatedTestSucceeded {

  /**
   * Constructs a new <code>TestSucceeded</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that has succeeded
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that has succeeded
   * @param testName the name of the test that has succeeded
   * @param duration an optional amount of time, in milliseconds, that was required to run the test that has succeeded
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param rerunner an optional <code>Rerunner</code> that can be used to rerun the test that has succeeded (if <code>None</code>
   *        is passed, the test cannot be rerun)
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>TestSucceeded</code> event
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestSucceeded</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    duration: Option[Long],
    formatter: Option[Formatter],
    rerunner: Option[Rerunner],
    payload: Option[Any]
  ): TestSucceeded = {
    TestSucceeded(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, duration, formatter, None, rerunner, payload, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestSucceeded</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that has succeeded
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that has succeeded
   * @param testName the name of the test that has succeeded
   * @param duration an optional amount of time, in milliseconds, that was required to run the test that has succeeded
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param rerunner an optional <code>Rerunner</code> that can be used to rerun the test that has succeeded (if <code>None</code>
   *        is passed, the test cannot be rerun)
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestSucceeded</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    duration: Option[Long],
    formatter: Option[Formatter],
    rerunner: Option[Rerunner]
  ): TestSucceeded = {
    TestSucceeded(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, duration, formatter, None, rerunner, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestSucceeded</code> event with the passed parameters, passing <code>None</code> as the
   * <code>rerunner</code>, <code>None</code> as the <code>payload</code>, the current threads name as <code>threadname</code>,
   * and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that has succeeded
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that has succeeded
   * @param testName the name of the test that has succeeded
   * @param duration an optional amount of time, in milliseconds, that was required to run the test that has succeeded
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestSucceeded</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    duration: Option[Long],
    formatter: Option[Formatter]
  ): TestSucceeded = {
    TestSucceeded(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, duration, formatter, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestSucceeded</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>rerunner</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that has succeeded
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that has succeeded
   * @param testName the name of the test that has succeeded
   * @param duration an optional amount of time, in milliseconds, that was required to run the test that has succeeded
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestSucceeded</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    duration: Option[Long]
  ): TestSucceeded = {
    TestSucceeded(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, duration, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestSucceeded</code> event with the passed parameters, passing <code>None</code> for <code>duration</code>,
   * <code>None</code> for <code>formatter</code>, <code>None</code> as the <code>rerunner</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that has succeeded
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that has succeeded
   * @param testName the name of the test that has succeeded
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestSucceeded</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String
  ): TestSucceeded = {
    TestSucceeded(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, None, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

/**
 * Deprecated singleton object for the <a href="TestFailed.html"><code>TestFailed</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>TestFailed</code> objects.
 * This object contains methods that were in the <code>TestFailed</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use TestFailed with named and/or default parameters instead.")
object DeprecatedTestFailed {

  /**
   * Constructs a new <code>TestFailed</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param suiteName the name of the suite containing the test that has failed
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that has failed
   * @param testName the name of the test that has failed
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the test has failed,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   * @param duration an optional amount of time, in milliseconds, that was required to run the test that has failed
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param rerunner an optional <code>Rerunner</code> that can be used to rerun the test that has failed (if <code>None</code>
   *        is passed, the test cannot be rerun)
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>TestFailed</code> event
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestFailed</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    throwable: Option[Throwable],
    duration: Option[Long],
    formatter: Option[Formatter],
    rerunner: Option[Rerunner],
    payload: Option[Any]
  ): TestFailed = {
    TestFailed(ordinal, message, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, throwable, duration, formatter, None, rerunner, payload, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestFailed</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param suiteName the name of the suite containing the test that has failed
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that has failed
   * @param testName the name of the test that has failed
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the test has failed,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   * @param duration an optional amount of time, in milliseconds, that was required to run the test that has failed
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param rerunner an optional <code>Rerunner</code> that can be used to rerun the test that has failed (if <code>None</code>
   *        is passed, the test cannot be rerun)
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestFailed</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    throwable: Option[Throwable],
    duration: Option[Long],
    formatter: Option[Formatter],
    rerunner: Option[Rerunner]
  ): TestFailed = {
    TestFailed(ordinal, message, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, throwable, duration, formatter, None, rerunner, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestFailed</code> event with the passed parameters, passing <code>None</code> as the
   * <code>rerunner</code>, <code>None</code> as the <code>payload</code>, the current threads name as <code>threadname</code>,
   * and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param suiteName the name of the suite containing the test that has failed
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that has failed
   * @param testName the name of the test that has failed
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the test has failed,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   * @param duration an optional amount of time, in milliseconds, that was required to run the test that has failed
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestFailed</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    throwable: Option[Throwable],
    duration: Option[Long],
    formatter: Option[Formatter]
  ): TestFailed = {
    TestFailed(ordinal, message, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, throwable, duration, formatter, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestFailed</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>rerunner</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param suiteName the name of the suite containing the test that has failed
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that has failed
   * @param testName the name of the test that has failed
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the test has failed,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   * @param duration an optional amount of time, in milliseconds, that was required to run the test that has failed
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestFailed</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    throwable: Option[Throwable],
    duration: Option[Long]
  ): TestFailed = {
    TestFailed(ordinal, message, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, throwable, duration, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestFailed</code> event with the passed parameters, passing <code>None</code> for <code>duration</code>,
   * <code>None</code> for <code>formatter</code>, <code>None</code> as the <code>rerunner</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param suiteName the name of the suite containing the test that has failed
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that has failed
   * @param testName the name of the test that has failed
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the test has failed,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestFailed</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    throwable: Option[Throwable]
  ): TestFailed = {
    TestFailed(ordinal, message, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, throwable, None, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

/**
 * Deprecated singleton object for the <a href="TestIgnored.html"><code>TestIgnored</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>TestIgnored</code> objects.
 * This object contains methods that were in the <code>TestIgnored</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use TestIgnored with named and/or default parameters instead.")
object DeprecatedTestIgnored {

  /**
   * Constructs a new <code>TestIgnored</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that was ignored
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that was ignored
   * @param testName the name of the test that was ignored
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>TestIgnored</code> event
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestIgnored</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    formatter: Option[Formatter],
    payload: Option[Any]
  ): TestIgnored = {
    TestIgnored(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, formatter, None, payload, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestIgnored</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that was ignored
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that was ignored
   * @param testName the name of the test that was ignored
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestIgnored</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    formatter: Option[Formatter]
  ): TestIgnored = {
    TestIgnored(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, formatter, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestIgnored</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that was ignored
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that was ignored
   * @param testName the name of the test that was ignored
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestIgnored</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String
  ): TestIgnored = {
    TestIgnored(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

/**
 * Deprecated singleton object for the <a href="TestPending.html"><code>TestPending</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>TestPending</code> objects.
 * This object contains methods that were in the <code>TestPending</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use TestPending with named and/or default parameters instead.")
object DeprecatedTestPending {

  /**
   * Constructs a new <code>TestPending</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that is pending
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that is pending
   * @param testName the name of the test that is pending
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>TestPending</code> event
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestPending</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    formatter: Option[Formatter],
    payload: Option[Any]
  ): TestPending = {
    TestPending(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, formatter, None, payload, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestPending</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that is pending
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that is pending
   * @param testName the name of the test that is pending
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestPending</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String,
    formatter: Option[Formatter]
  ): TestPending = {
    TestPending(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, formatter, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>TestPending</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the test that is pending
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the test that is pending
   * @param testName the name of the test that is pending
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>TestPending</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    testName: String
  ): TestPending = {
    TestPending(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, testName, testName, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

/**
 * Deprecated singleton object for the <a href="SuiteStarting.html"><code>SuiteStarting</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>SuiteStarting</code> objects.
 * This object contains methods that were in the <code>SuiteStarting</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use SuiteStarting with named and/or default parameters instead.")
object DeprecatedSuiteStarting {

  /**
   * Constructs a new <code>SuiteStarting</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName a localized name identifying the suite that is starting, which should include the
   *        suite name, suitable for presenting to the user
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name of the suite that is starting
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param rerunner an optional <code>Rerunner</code> that can be used to rerun the suite that is starting (if <code>None</code>
   *        is passed, the suite cannot be rerun)
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>SuiteStarting</code> event
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteStarting</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    formatter: Option[Formatter],
    rerunner: Option[Rerunner],
    payload: Option[Any]
  ): SuiteStarting = {
    SuiteStarting(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, formatter, None, rerunner, payload, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>SuiteStarting</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName a localized name identifying the suite that is starting, which should include the
   *        suite name, suitable for presenting to the user
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name of the suite that is starting
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param rerunner an optional <code>Rerunner</code> that can be used to rerun the suite that is starting (if <code>None</code>
   *        is passed, the suite cannot be rerun)
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteStarting</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    formatter: Option[Formatter],
    rerunner: Option[Rerunner]
  ): SuiteStarting = {
    SuiteStarting(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, formatter, None, rerunner, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>SuiteStarting</code> event with the passed parameters, passing <code>None</code> as the
   * <code>rerunner</code>, <code>None</code> as the <code>payload</code>, the current threads name as <code>threadname</code>,
   * and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName a localized name identifying the suite that is starting, which should include the
   *        suite name, suitable for presenting to the user
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name of the suite that is starting
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteStarting</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    formatter: Option[Formatter]
  ): SuiteStarting = {
    SuiteStarting(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, formatter, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>SuiteStarting</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>rerunner</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName a localized name identifying the suite that is starting, which should include the
   *        suite name, suitable for presenting to the user
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name of the suite that is starting
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteStarting</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String]
  ): SuiteStarting = {
    SuiteStarting(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

/**
 * Deprecated singleton object for the <a href="SuiteCompleted.html"><code>SuiteCompleted</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>SuiteCompleted</code> objects.
 * This object contains methods that were in the <code>SuiteCompleted</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use SuiteCompleted with named and/or default parameters instead.")
object DeprecatedSuiteCompleted {

  /**
   * Constructs a new <code>SuiteCompleted</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the suite that has completed
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the suite that has completed
   * @param duration an optional amount of time, in milliseconds, that was required to execute the suite that has completed
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param rerunner an optional <code>Rerunner</code> that can be used to rerun the suite that has completed (if <code>None</code>
   *        is passed, the suite cannot be rerun)
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>SuiteCompleted</code> event
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteCompleted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    duration: Option[Long],
    formatter: Option[Formatter],
    rerunner: Option[Rerunner],
    payload: Option[Any]
  ): SuiteCompleted = {
    SuiteCompleted(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, duration, formatter, None, rerunner, payload, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>SuiteCompleted</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the suite that has completed
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the suite that has completed
   * @param duration an optional amount of time, in milliseconds, that was required to execute the suite that has completed
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param rerunner an optional <code>Rerunner</code> that can be used to rerun the suite that has completed (if <code>None</code>
   *        is passed, the suite cannot be rerun)
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteCompleted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    duration: Option[Long],
    formatter: Option[Formatter],
    rerunner: Option[Rerunner]
  ): SuiteCompleted = {
    SuiteCompleted(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, duration, formatter, None, rerunner, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>SuiteCompleted</code> event with the passed parameters, passing <code>None</code> as the
   * <code>rerunner</code>, <code>None</code> as the <code>payload</code>, the current threads name as <code>threadname</code>,
   * and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the suite that has completed
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the suite that has completed
   * @param duration an optional amount of time, in milliseconds, that was required to execute the suite that has completed
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteCompleted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    duration: Option[Long],
    formatter: Option[Formatter]
  ): SuiteCompleted = {
    SuiteCompleted(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, duration, formatter, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>SuiteCompleted</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>rerunner</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param suiteName the name of the suite containing the suite that has completed
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the suite that has completed
   * @param duration an optional amount of time, in milliseconds, that was required to execute the suite that has completed
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteCompleted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String],
    duration: Option[Long]
  ): SuiteCompleted = {
    SuiteCompleted(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, duration, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>SuiteCompleted</code> event with the passed parameters, passing <code>None</code> for <code>duration</code>,
   * <code>None</code> for <code>formatter</code>, <code>None</code> as the <code>rerunner</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param name a localized name identifying the suite that has completed, which should include the
   *        suite name, suitable for presenting to the user
   * @param suiteName the name of the suite containing the suite that has completed
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the suite that has completed
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteCompleted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    suiteName: String,
    suiteClassName: Option[String]
  ): SuiteCompleted = {
    SuiteCompleted(ordinal, suiteName, suiteClassName getOrElse suiteName, suiteClassName, None, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

/**
 * Deprecated singleton object for the <a href="SuiteAborted.html"><code>SuiteAborted</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>SuiteAborted</code> objects.
 * This object contains methods that were in the <code>SuiteAborted</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use SuiteAborted with named and/or default parameters instead.")
object DeprecatedSuiteAborted {

  /**
   * Constructs a new <code>SuiteAborted</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param name a localized name identifying the suite that has aborted, which should include the
   *        suite name, suitable for presenting to the user
   * @param message a localized message suitable for presenting to the user
   * @param suiteName the name of the suite containing the suite that has aborted
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the suite that has aborted
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the suite has aborted,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   * @param duration an optional amount of time, in milliseconds, that was required to execute the suite that has aborted
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param rerunner an optional <code>Rerunner</code> that can be used to rerun the suite that has aborted (if <code>None</code>
   *        is passed, the suite cannot be rerun)
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>SuiteAborted</code> event
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteAborted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    suiteName: String,
    suiteClassName: Option[String],
    throwable: Option[Throwable],
    duration: Option[Long],
    formatter: Option[Formatter],
    rerunner: Option[Rerunner],
    payload: Option[Any]
  ): SuiteAborted = {
    SuiteAborted(ordinal, message, suiteName, suiteClassName getOrElse suiteName, suiteClassName, throwable, duration, formatter, None, rerunner, payload, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>SuiteAborted</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param suiteName the name of the suite containing the suite that has aborted
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the suite that has aborted
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the suite has aborted,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   * @param duration an optional amount of time, in milliseconds, that was required to execute the suite that has aborted
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param rerunner an optional <code>Rerunner</code> that can be used to rerun the suite that has aborted (if <code>None</code>
   *        is passed, the suite cannot be rerun)
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteAborted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    suiteName: String,
    suiteClassName: Option[String],
    throwable: Option[Throwable],
    duration: Option[Long],
    formatter: Option[Formatter],
    rerunner: Option[Rerunner]
  ): SuiteAborted = {
    SuiteAborted(ordinal, message, suiteName, suiteClassName getOrElse suiteName, suiteClassName, throwable, duration, formatter, None, rerunner, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>SuiteAborted</code> event with the passed parameters, passing <code>None</code> as the
   * <code>rerunner</code>, <code>None</code> as the <code>payload</code>, the current threads name as <code>threadname</code>,
   * and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the suite has aborted,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   * @param duration an optional amount of time, in milliseconds, that was required to execute the suite that has aborted
   * @param suiteName the name of the suite containing the suite that has aborted
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the suite that has aborted
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteAborted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    suiteName: String,
    suiteClassName: Option[String],
    throwable: Option[Throwable],
    duration: Option[Long],
    formatter: Option[Formatter]
  ): SuiteAborted = {
    SuiteAborted(ordinal, message, suiteName, suiteClassName getOrElse suiteName, suiteClassName, throwable, duration, formatter, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>SuiteAborted</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>rerunner</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param suiteName the name of the suite containing the suite that has aborted
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the suite that has aborted
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the suite has aborted,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   * @param duration an optional amount of time, in milliseconds, that was required to execute the suite that has aborted
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteAborted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    suiteName: String,
    suiteClassName: Option[String],
    throwable: Option[Throwable],
    duration: Option[Long]
  ): SuiteAborted = {
    SuiteAborted(ordinal, message, suiteName, suiteClassName getOrElse suiteName, suiteClassName, throwable, duration, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>SuiteAborted</code> event with the passed parameters, passing <code>None</code> for <code>duration</code>,
   * <code>None</code> for <code>formatter</code>, <code>None</code> as the <code>rerunner</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param suiteName the name of the suite containing the suite that has aborted
   * @param suiteClassName an optional fully qualifed <code>Suite</code> class name containing the suite that has aborted
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the suite has aborted,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>SuiteAborted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    suiteName: String,
    suiteClassName: Option[String],
    throwable: Option[Throwable]
  ): SuiteAborted = {
    SuiteAborted(ordinal, message, suiteName, suiteClassName getOrElse suiteName, suiteClassName, throwable, None, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

/**
 * Deprecated singleton object for the <a href="RunStarting.html"><code>RunStarting</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>RunStarting</code> objects.
 * This object contains methods that were in the <code>RunStarting</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>, and <code>IllegalArgumentException</code> if
 * <code>testCount</code> is less than zero.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use RunStarting with named and/or default parameters instead.")
object DeprecatedRunStarting {

  /**
   * Constructs a new <code>RunStarting</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param testCount the number of tests expected during this run
   * @param configMap a <code>Map</code> of key-value pairs that can be used by custom <code>Reporter</code>s
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>RunStarting</code> event
   *
   * @throws IllegalArgumentException if <code>testCount</code> is less than zero.
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunStarting</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    testCount: Int,
    configMap: Map[String, Any],
    formatter: Option[Formatter],
    payload: Option[Any]
  ): RunStarting = {
    RunStarting(ordinal, testCount, configMap, formatter, None, payload, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunStarting</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>,
   * and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param testCount the number of tests expected during this run
   * @param configMap a <code>Map</code> of key-value pairs that can be used by custom <code>Reporter</code>s
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunStarting</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    testCount: Int,
    configMap: Map[String, Any],
    formatter: Option[Formatter]
  ): RunStarting = {
    RunStarting(ordinal, testCount, configMap, formatter, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunStarting</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param testCount the number of tests expected during this run
   * @param configMap a <code>Map</code> of key-value pairs that can be used by custom <code>Reporter</code>s
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunStarting</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    testCount: Int,
    configMap: Map[String, Any]
  ): RunStarting = {
    RunStarting(ordinal, testCount, configMap, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

/**
 * Deprecated singleton object for the <a href="RunCompleted.html"><code>RunCompleted</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>RunCompleted</code> objects.
 * This object contains methods that were in the <code>RunCompleted</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use RunCompleted with named and/or default parameters instead.")
object DeprecatedRunCompleted {

  /**
   * Constructs a new <code>RunCompleted</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param duration an optional amount of time, in milliseconds, that was required by the run that has completed
   * @param summary an optional summary of the number of tests that were reported as succeeded, failed, ignored, and pending
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>RunCompleted</code> event
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunCompleted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    duration: Option[Long],
    summary: Option[Summary],
    formatter: Option[Formatter],
    payload: Option[Any]
  ): RunCompleted = {
    RunCompleted(ordinal, duration, summary, formatter, None, payload, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunCompleted</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>,
   * and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param duration an optional amount of time, in milliseconds, that was required by the run that has completed
   * @param summary an optional summary of the number of tests that were reported as succeeded, failed, ignored, and pending
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunCompleted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    duration: Option[Long],
    summary: Option[Summary],
    formatter: Option[Formatter]
  ): RunCompleted = {
    RunCompleted(ordinal, duration, summary, formatter, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunCompleted</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param duration an optional amount of time, in milliseconds, that was required by the run that has completed
   * @param summary an optional summary of the number of tests that were reported as succeeded, failed, ignored, and pending
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunCompleted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    duration: Option[Long],
    summary: Option[Summary]
  ): RunCompleted = {
    RunCompleted(ordinal, duration, summary, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunCompleted</code> event with the passed parameters, passing <code>None</code> for <code>summary</code>,
   *  <code>None</code> for <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param duration an optional amount of time, in milliseconds, that was required by the run that has completed
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunCompleted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    duration: Option[Long]
  ): RunCompleted = {
    RunCompleted(ordinal, duration, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunCompleted</code> event with the passed parameters, passing <code>None</code> for <code>duration</code>,
   * <code>None</code> for <code>summary</code>, <code>None</code> for <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunCompleted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal
  ): RunCompleted = {
    RunCompleted(ordinal, None, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

/**
 * Deprecated singleton object for the <a href="RunStopped.html"><code>RunStopped</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>RunStopped</code> objects.
 * This object contains methods that were in the <code>RunStopped</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use RunStopped with named and/or default parameters instead.")
object DeprecatedRunStopped {

  /**
   * Constructs a new <code>RunStopped</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param duration an optional amount of time, in milliseconds, that was required by the run that has stopped
   * @param summary an optional summary of the number of tests that were reported as succeeded, failed, ignored, and pending
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>RunStopped</code> event
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunStopped</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    duration: Option[Long],
    summary: Option[Summary],
    formatter: Option[Formatter],
    payload: Option[Any]
  ): RunStopped = {
    RunStopped(ordinal, duration, summary, formatter, None, payload, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunStopped</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>,
   * and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param duration an optional amount of time, in milliseconds, that was required by the run that has stopped
   * @param summary an optional summary of the number of tests that were reported as succeeded, failed, ignored, and pending
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunStopped</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    duration: Option[Long],
    summary: Option[Summary],
    formatter: Option[Formatter]
  ): RunStopped = {
    RunStopped(ordinal, duration, summary, formatter, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunStopped</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param duration an optional amount of time, in milliseconds, that was required by the run that has stopped
   * @param summary an optional summary of the number of tests that were reported as succeeded, failed, ignored, and pending
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunStopped</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    duration: Option[Long],
    summary: Option[Summary]
  ): RunStopped = {
    RunStopped(ordinal, duration, summary, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunStopped</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param duration an optional amount of time, in milliseconds, that was required by the run that has stopped
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunStopped</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    duration: Option[Long]
  ): RunStopped = {
    RunStopped(ordinal, duration, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunStopped</code> event with the passed parameters, passing <code>None</code> for <code>duration</code>,
   * <code>None</code> for <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunStopped</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal
  ): RunStopped = {
    RunStopped(ordinal, None, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

/**
 * Deprecated singleton object for the <a href="RunAborted.html"><code>RunAborted</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>RunAborted</code> objects.
 * This object contains methods that were in the <code>RunAborted</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use RunAborted with named and/or default parameters instead.")
object DeprecatedRunAborted {

  /**
   * Constructs a new <code>RunAborted</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the suite has aborted,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   * @param duration an optional amount of time, in milliseconds, that was required by the run that has aborted
   * @param summary an optional summary of the number of tests that were reported as succeeded, failed, ignored, and pending
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>RunAborted</code> event
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunAborted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    throwable: Option[Throwable],
    duration: Option[Long],
    summary: Option[Summary],
    formatter: Option[Formatter],
    payload: Option[Any]
  ): RunAborted = {
    RunAborted(ordinal, message, throwable, duration, summary, formatter, None, payload, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunAborted</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>,
   * and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the suite has aborted,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   * @param duration an optional amount of time, in milliseconds, that was required by the run that has aborted
   * @param summary an optional summary of the number of tests that were reported as succeeded, failed, ignored, and pending
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunAborted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    throwable: Option[Throwable],
    duration: Option[Long],
    summary: Option[Summary],
    formatter: Option[Formatter]
  ): RunAborted = {
    RunAborted(ordinal, message, throwable, duration, summary, formatter, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunAborted</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the suite has aborted,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   * @param duration an optional amount of time, in milliseconds, that was required by the run that has aborted
   * @param summary an optional summary of the number of tests that were reported as succeeded, failed, ignored, and pending
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunAborted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    throwable: Option[Throwable],
    duration: Option[Long],
    summary: Option[Summary]
  ): RunAborted = {
    RunAborted(ordinal, message, throwable, duration, summary, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunAborted</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the suite has aborted,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   * @param duration an optional amount of time, in milliseconds, that was required by the run that has aborted
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunAborted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    throwable: Option[Throwable],
    duration: Option[Long]
  ): RunAborted = {
    RunAborted(ordinal, message, throwable, duration, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>RunAborted</code> event with the passed parameters, passing <code>None</code> for <code>duration</code>,
   * <code>None</code> for <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param throwable an optional <code>Throwable</code> that, if a <code>Some</code>, indicates why the suite has aborted,
   *        or a <code>Throwable</code> created to capture stack trace information about the problem.
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>RunAborted</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    throwable: Option[Throwable]
  ): RunAborted = {
    RunAborted(ordinal, message, throwable, None, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

/**
 * Deprecated singleton object for the <a href="InfoProvided.html"><code>InfoProvided</code></a> event, which contains overloaded factory methods
 * and an extractor method to facilitate pattern matching on <code>InfoProvided</code> objects.
 * This object contains methods that were in the <code>InfoProvided</code> companion object prior to ScalaTest 2.0. If you get a compiler error when upgrading
 * to 2.0 for one of the methods formerly in the companion object, a quick way to fix it is to put <code>Deprecated</code> in front of your call.
 * Eventually you will need to fix it properly, as this singleton object is deprecated and will be removed in a future version of ScalaTest, but
 * this will work as a quick fix to get you compiling again.
 *
 * <p>
 * All factory methods throw <code>NullPointerException</code> if any of the passed values are <code>null</code>.
 * </p>
 *
 * @author Bill Venners
 */
@deprecated("Use InfoProvided with named and/or default parameters instead.")
object DeprecatedInfoProvided {

  /**
   * Constructs a new <code>InfoProvided</code> event with the passed parameters, passing the current thread's
   * name as <code>threadname</code> and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param nameInfo an optional <code>NameInfo</code> that if defined, provides names for the suite and optionally the test 
   *        in the context of which the information was provided
   * @param aboutAPendingTest indicates whether the information being provided via this event is about a pending test
   * @param throwable an optional <code>Throwable</code>
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   * @param payload an optional object that can be used to pass custom information to the reporter about the <code>InfoProvided</code> event
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>InfoProvided</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    nameInfo: Option[NameInfo],
    aboutAPendingTest: Option[Boolean],
    throwable: Option[Throwable],
    formatter: Option[Formatter],
    payload: Option[Any]
  ): InfoProvided = {
    InfoProvided(ordinal, message, nameInfo, aboutAPendingTest, Some(false), throwable, formatter, None, payload, Thread.currentThread.getName, (new Date).getTime)
  }


  /**
   * Constructs a new <code>InfoProvided</code> event with the passed parameters, passing <code>None</code> as the
   * <code>payload</code>, the current threads name as <code>threadname</code>,
   * and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param nameInfo an optional <code>NameInfo</code> that if defined, provides names for the suite and optionally the test
   *        in the context of which the information was provided
   * @param aboutAPendingTest indicates whether the information being provided via this event is about a pending test
   * @param throwable an optional <code>Throwable</code>
   * @param formatter an optional formatter that provides extra information that can be used by reporters in determining
   *        how to present this event to the user
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>InfoProvided</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    nameInfo: Option[NameInfo],
    aboutAPendingTest: Option[Boolean],
    throwable: Option[Throwable],
    formatter: Option[Formatter]
  ): InfoProvided = {
    InfoProvided(ordinal, message, nameInfo, aboutAPendingTest, Some(false), throwable, formatter, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>InfoProvided</code> event with the passed parameters, passing <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param nameInfo an optional <code>NameInfo</code> that if defined, provides names for the suite and optionally the test 
   *        in the context of which the information was provided
   * @param aboutAPendingTest indicates whether the information being provided via this event is about a pending test
   * @param throwable an optional <code>Throwable</code>
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>InfoProvided</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    nameInfo: Option[NameInfo],
    aboutAPendingTest: Option[Boolean],
    throwable: Option[Throwable]
  ): InfoProvided = {
    InfoProvided(ordinal, message, nameInfo, aboutAPendingTest, Some(false), throwable, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>InfoProvided</code> event with the passed parameters, passing <code>None</code> for
   * the <code>throwable</code>, <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param nameInfo an optional <code>NameInfo</code> that if defined, provides names for the suite and optionally the test 
   *        in the context of which the information was provided
   * @param aboutAPendingTest indicates whether the information being provided via this event is about a pending test
   * @param throwable an optional <code>Throwable</code>
   * * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>InfoProvided</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    nameInfo: Option[NameInfo],
    aboutAPendingTest: Option[Boolean]
  ): InfoProvided = {
    InfoProvided(ordinal, message, nameInfo, aboutAPendingTest, Some(false), None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }

  /**
   * Constructs a new <code>InfoProvided</code> event with the passed parameters, passing <code>None</code> for
   * the <code>throwable</code>, <code>None</code> for
   * <code>formatter</code>, <code>None</code> as the <code>payload</code>,
   * the current threads name as <code>threadname</code>, and the current time as <code>timeStamp</code>.
   *
   * @param ordinal an <code>Ordinal</code> that can be used to place this event in order in the context of
   *        other events reported during the same run
   * @param message a localized message suitable for presenting to the user
   * @param nameInfo an optional <code>NameInfo</code> that if defined, provides names for the suite and optionally the test
   *        in the context of which the information was provided
   * @param throwable an optional <code>Throwable</code>
   *
   * @throws NullPointerException if any of the passed values are <code>null</code>
   *
   * @return a new <code>InfoProvided</code> instance initialized with the passed and default values
   */
  def apply(
    ordinal: Ordinal,
    message: String,
    nameInfo: Option[NameInfo]
  ): InfoProvided = {
    InfoProvided(ordinal, message, nameInfo, None, None, None, None, None, None, Thread.currentThread.getName, (new Date).getTime)
  }
}

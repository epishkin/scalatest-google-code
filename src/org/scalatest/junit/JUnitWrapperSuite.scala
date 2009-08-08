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
class JUnitWrapperSuite(junitClassName: String, loader: ClassLoader)
extends Suite {

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

  private class MyRunListener(report: Reporter, config: Map[String, Any],
                              theTracker: Tracker)
  extends RunListener {
    val failedTests = Collections.synchronizedSet(new HashSet[String])

    override def testFailure(failure: Failure) {
      failedTests.add(failure.getDescription.getDisplayName)
      val (testName, testClass, testClassName) =
        parseTestDescription(failure.getDescription)
      val throwableOrNull = failure.getException
      val throwable =
        if (throwableOrNull != null)
          Some(throwableOrNull)
        else
          None

      val message =
        if (throwableOrNull != null)
          throwableOrNull.getMessage
        else
          Resources("jUnitTestFailed")

      report(TestFailed(theTracker.nextOrdinal(), message, testClassName,
                        Some(testClass), testName, throwable))
      // TODO: can I add a duration?
    }

    override def testFinished(description: Description) {
      if (!failedTests.contains(description.getDisplayName)) {
        val (testName, testClass, testClassName) =
          parseTestDescription(description)
        report(TestSucceeded(theTracker.nextOrdinal(), testClassName,
                             Some(testClass), testName))
        // TODO: can I add a duration?
      }
    }

    override def testIgnored(description: Description) {
      val (testName, testClass, testClassName) =
        parseTestDescription(description)
      report(TestIgnored(theTracker.nextOrdinal(), testClassName,
                         Some(testClass), testName))
    }

    override def testRunFinished(result: Result) {
      // don't report these - they get reported by Runner
    }

    override def testRunStarted(description: Description) {
      // don't report these - they get reported by Runner
    }

    override def testStarted(description: Description) {
      val (testName, testClass, testClassName) =
        parseTestDescription(description)
      report(TestStarting(theTracker.nextOrdinal(), testClassName, 
                         Some(testClass), testName))
    }

    //
    // Parses test name and suite name from description.  Returns them
    // as a tuple (testname, test class (fully qualified), test class name).
    //
    // The test descriptions I've seen have had the form testname(testclass).
    // This may need to be modified if other formats are discovered.
    //
    val TEST_DESCRIPTION_PATTERN = Pattern.compile("""^(.*)\((.*)\)""")
    private def parseTestDescription(description: Description):
    (String, String, String) = {
      val matcher =
        TEST_DESCRIPTION_PATTERN.matcher(description.getDisplayName)

      if (!matcher.find())
        throw new RuntimeException("unexpected displayName [" +
                                   description.getDisplayName + "]")

      val testName = matcher.group(1)
      val testClass = matcher.group(2)
      val testClassName = testClass.replaceAll(".*\\.", "")

      (testName, testClass, testClassName)
    }
  }
}

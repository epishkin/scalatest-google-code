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

/**
 * Trait that can be mixed into suites that need methods invoked before and after executing the
 * suite. This trait allows code to be executed before and/or after all the tests and nested suites of a
 * suite are run. This trait overrides <code>run</code> (the main <code>run</code> method that
 * takes seven parameters, an optional test name, reporter, stopper, filter, config, optional distributor,
 * and tracker) and calls the
 * <code>beforeAll</code> method, then calls <code>super.run</code>. After the <code>super.run</code>
 * invocation completes, whether it returns normally or completes abruptly with an exception,
 * this trait's <code>run</code> method will invoke <code>afterAll</code>.
 *
 * <p>
 * Trait <code>BeforeAndAfterEach</code> defines two overloaded variants  each of <code>beforeAll</code>
 * and <code>afterAll</code>, one which takes a <code>config</code> map and another that takes no
 * arguments. This traits implemention of the variant that takes the <code>config</code> map
 * simply invokes the variant that takes no parameters, which does nothing. Thus you can override
 * whichever variant you want. If you need something from the <code>config</code> map before
 * all tests and nested suites are run, override <code>beforeAll(Map[String, Any])</code>. Otherwise,
 * override <code>beforeAll()</code>.
 * </p>
 *
 * <p>
 * For example, the following <code>MasterSuite</code> mixes in <code>BeforeAndAfterAll</code> and
 * in <code>beforeAll</code>, creates and writes to a temp file, taking the name of the temp file
 * from the <code>config</code> map. This same config map is then passed to the <code>run</code>
 * methods of the nested suites, <code>OneSuite</code>, <code>TwoSuite</code>, <code>RedSuite</code>,
 * and <code>BlueSuite</code>, so those suites can access the filename and, therefore, the file's
 * contents. After all of the nested suites have executed, <code>afterAll</code> is invoked, which
 * again grabs the file name from the <code>config</code> map and deletes the file:
 * </p>
 * 
 * <pre>
 * import org.scalatest.SuperSuite
 * import org.scalatest.BeforeAndAfterEach
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 *
 * class MasterSuite extends Suite with BeforeAndAfterAll {
 *
 *   private val FileNameKeyInGoodies = "tempFileName"
 *
 *   // Set up the temp file needed by the test, taking
 *   // a file name from the config map
 *   override def beforeAll(config: Map[String, Any]) {
 *
 *     require(
 *       config.isDefinedAt(FileNameKeyInGoodies),
 *       "must place a temp file name in the config map under the key: " + FileNameKeyInGoodies
 *     )
 *
 *     val fileName = config(tempFileName))
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
 *   override def afterAll(config: Map[String, Any]) {
 *     // No need to require that config contains the key again because it won't get
 *     // here if it didn't contain the key in beforeAll 
 *     val fileName = config(tempFileName))
 *     val file = new File(fileName)
 *     file.delete()
 *   }
 * }
 * </pre>
 *
 * <p>
 * Because the <code>BeforeAndAfterAll</code> trait invokes <code>super.run</code> to run the suite, you may need to
 * mix this trait in last to get the desired behavior. For example, this won't
 * work, because <code>BeforeAndAfterAll</code> is "super" to </code>FunSuite</code>:
 * </p>
 * <pre>
 * class MySuite extends BeforeAndAfterAll with FunSuite
 * </pre>
 * <p>
 * You'd need to turn it around, so that <code>FunSuite</code> is "super" to <code>BeforeAndAfterAll</code>, like this:
 * </p>
 * <pre>
 * class MySuite extends FunSuite with BeforeAndAfterAll
 * </pre>
 *
 * <strong>Note: This trait does not currently ensure that the code in <code>afterAll</code> is executed after
 * all the tests and nested suites are executed if a <code>Distributor</code> is passed. The
 * plan is to do that eventually, but in the meantime, be aware that <code>afterAll</code> is
 * guaranteed to be run after all the tests and nested suites only when they are run
 * sequentially.</strong>
 *
 * @author Bill Venners
 */
trait BeforeAndAfterAll  extends RunMethods {

  this: Suite =>

  /**
   * Defines a method to be run before any of this suite's tests or nested suites are run.
   *
   * <p>
   * This trait's implementation
   * of <code>run</code> invokes this method before executing
   * any tests or nested suites, thus this method can be used to set up a test fixture
   * needed by the entire suite. This trait's implementation of this method does nothing.
   * </p>
   */
  protected def beforeAll() = ()

  /**
   * Defines a method to be run after all of this suite's tests and nested suites have
   * been run.
   *
   * <p>
   * This trait's implementation
   * of <code>run</code> invokes this method after executing
   * all tests and nested suites, thus this method can be used to tear down a test fixture
   * needed by the entire suite. This trait's implementation of this method does nothing.
   * </p>
   */
  protected def afterAll() = ()
  
  /**
   * Execute a suite surrounded by calls to <code>beforeAll</code> and <code>afterAll</code>.
   *
   * <p>
   * This trait's implementation of this method ("this method") invokes <code>beforeAll</code>
   * before executing any tests or nested suites and <code>afterAll</code>
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
                       config: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    var thrownException: Option[Throwable] = None

    beforeAll()
    try {
      super.run(testName, reporter, stopper, filter, config, distributor, tracker)
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

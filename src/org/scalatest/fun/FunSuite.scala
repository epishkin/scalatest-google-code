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
package org.scalatest.fun

import scala.collection.immutable.ListSet
import java.util.ConcurrentModificationException
import java.util.concurrent.atomic.AtomicReference

/**
 * Abstract class whose subclasses can be as to <code>FunSuite</code> and <code>FunSuiteN</code>'s test
 * registration methods to place tests into groups. For example, if you define:
 * <pre>
 * case class SlowTest extends Group("SlowTest")
 * </pre>
 *
 * then you can place a test into the <code>SlowTest</code> group like this:
 * <pre>
 * class MySuite extends FunSuite {
 *
 *   test("my test", SlowTest()) {
 *     Thread.sleep(1000)
 *   }
 * }
 * </pre>
 *
 * If you have created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>FunSuite</code>s that match. To do so, simply use
 * pass the fully qualified names of the Java interfaces to the <code>Group</code> constructor. For example, if you've
 * defined a Java annotation interface with fully qualified name, <code>com.mycompany.groups.SlowTest</code>, then you could
 * create a matching group for <code>FunSuite</code>s like this:
 * <pre>
 * case class SlowTest extends Group("com.mycompany.groups.SlowTest")
 * </pre>
 */
abstract class Group(val name: String)

trait FunSuite extends Suite {

  // Until it shows up in Predef
  private def require(b: Boolean, msg: String) { if (!b) throw new IllegalArgumentException(msg) }

  private val IgnoreGroupName = "org.scalatest.Ignore"

  private trait Test
  private case class PlainOldTest(testName: String, f: () => Unit) extends Test
  private case class ReporterTest(testName: String, f: (Reporter) => Unit) extends Test

  // Access to the testNamesList, testsMap, and groupsMap must be synchronized, because the test methods are invoked by
  // the primary constructor, but testNames, groups, and runTest get invoked directly or indirectly
  // by execute. When running tests concurrently with ScalaTest Runner, different threads can
  // instantiate and execute the Suite. Instead of synchronizing, I put them in an immutable Bundle object (and
  // all three collections--testNamesList, testsMap, and groupsMap--are immuable collections), then I put the Bundle
  // in an AtomicReference. Since the expected use case is the test, testWithReporter, etc., methods will be called
  // from the primary constructor, which will be all done by one thread, I just in effect use optimistic locking on the Bundle.
  // If two threads ever called test at the same time, they could get a ConcurrentModificationException.
  // Test names are in reverse order of test registration method invocations
  private class Bundle private(val testNamesList: List[String], val testsMap: Map[String, Test], val groupsMap: Map[String, Set[String]]) {
    def unpack = (testNamesList, testsMap, groupsMap)
  }
  private object Bundle {
    def apply(testNamesList: List[String], testsMap: Map[String, Test], groupsMap: Map[String, Set[String]]): Bundle =
      new Bundle(testNamesList, testsMap, groupsMap)
  }

  private val atomic = new AtomicReference[Bundle](Bundle(Nil, Map(), Map()))

  private def updateAtomic(oldBundle: Bundle, newBundle: Bundle) {
    if (!atomic.compareAndSet(oldBundle, newBundle))
      throw new ConcurrentModificationException
  }

  /**
   * Register a test with the specified name, optional groups, and function value that takes no arguments.
   * This method will register the test for later execution via an invocation of one of the <code>execute</code>
   * methods. The passed test name must not have been registered previously on
   * this <code>FunSuite</code> instance.
   *
   * @throws IllegalArgumentException if <code>testName</code> had been registered previously
   */
  protected def test(testName: String, testGroups: Group*)(f: => Unit) {

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    require(!testsMap.keySet.contains(testName), "Duplicate test name: " + testName)

    testsMap += (testName -> PlainOldTest(testName, f _))
    testNamesList ::= testName
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    if (!groupNames.isEmpty)
      groupsMap += (testName -> groupNames)

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  /**
   * Register a test with the specified name, optional groups, and function value that takes a <code>Reporter</code>.
   * This method will register the test for later execution via an invocation of one of the <code>execute</code>
   * methods. The <code>Reporter</code> passed to <code>execute</code>, or a <code>Reporter</code> that wraps it, will be passed to the function value.
   * The passed test name must not have been registered previously on
   * this <code>FunSuite</code> instance.
   *
   * @throws IllegalArgumentException if <code>testName</code> had been registered previously
   */
  protected def testWithReporter(testName: String, testGroups: Group*)(f: (Reporter) => Unit) {

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    require(!testsMap.keySet.contains(testName), "Duplicate test name: " + testName)

    testsMap += (testName -> ReporterTest(testName, f))
    testNamesList ::= testName
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    if (!groupNames.isEmpty)
      groupsMap += (testName -> groupNames)

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and function value that takes a no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>execute</code>
   * methods. This method exists to make it easy to ignore an existing test method by changing the call to <code>test</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be executed, but a
   * report will be sent that indicates the test was ignored. The passed test name must not have been registered previously on
   * this <code>FunSuite</code> instance.
   *
   * @throws IllegalArgumentException if <code>testName</code> had been registered previously
   */
  protected def ignore(testName: String, testGroups: Group*)(f: => Unit) {

    test(testName)(f) // Call test without passing the groups

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    val groupNames = Set[String]() ++ testGroups.map(_.name)
    groupsMap += (testName -> (groupNames + IgnoreGroupName))

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and function value that takes a <code>Reporter</code>.
   * This method will register the test for later ignoring via an invocation of one of the <code>execute</code>
   * methods. This method exists to make it easy to ignore an existing test method by changing the call to <code>testWithReporter</code>
   * to <code>ignoreWithReporter</code> without deleting or commenting out the actual test code. The test will not be executed, but a
   * report will be sent that indicates the test was ignored. The passed test name must not have been registered previously on
   * this <code>FunSuite</code> instance.
   *
   * @throws IllegalArgumentException if <code>testName</code> had been registered previously
   */
  protected def ignoreWithReporter(testName: String, testGroups: Group*)(f: (Reporter) => Unit) {

    testWithReporter(testName)(f) // Call testWithReporter without passing the groups

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    val groupNames = Set[String]() ++ testGroups.map(_.name)
    groupsMap += (testName -> (groupNames + IgnoreGroupName))

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  /**
  * An immutable <code>Set</code> of test names. If this <code>Suite</code> contains no tests, this method returns an empty <code>Set</code>.
  *
  * <p>
  * This trait's implementation of this method will return a set that contains the names of all registered tests. The set's iterator will
  * return those names in the order in which the tests were registered.
  * </p>
  */
  override def testNames: Set[String] = {
    // I'm returning a ListSet here so that they tests will be executed in registration order
    ListSet(atomic.get.testNamesList.toArray: _*)
  }

  protected override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any]) {

    if (testName == null || reporter == null || stopper == null || properties == null)
      throw new NullPointerException

    val wrappedReporter = reporter

    val report = new Report(testName, this.getClass.getName)

    wrappedReporter.testStarting(report)

    try {

      atomic.get.testsMap(testName) match {
        case PlainOldTest(testName, f) => f()
        case ReporterTest(testName, f) => f(reporter)
      }

      val report = new Report(getTestNameForReport(testName), this.getClass.getName)

      wrappedReporter.testSucceeded(report)
    }
    catch { 
      case e: Exception => {
        handleFailedTest(e, false, testName, None, wrappedReporter)
      }
      case ae: AssertionError => {
        handleFailedTest(ae, false, testName, None, wrappedReporter)
      }
    }
  }

  private def handleFailedTest(t: Throwable, hasPublicNoArgConstructor: Boolean, testName: String,
      rerunnable: Option[Rerunnable], reporter: Reporter) {

    val msg =
      if (t.getMessage != null) // [bv: this could be factored out into a helper method]
        t.getMessage
      else
        t.toString

    val report = new Report(getTestNameForReport(testName), msg, Some(t), None)

    reporter.testFailed(report)
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> group names to which tests in this <code>FunSuite</code> belong, and values
   * the <code>Set</code> of test names that belong to each group. If this <code>FunSuite</code> contains no groups, this method returns an empty <code>Map</code>.
   *
   * <p>
   * This trait's implementation returns groups that were passed as strings contained in <code>Group</code> objects passed to 
   * methods <code>test</code>, <code>testWithReporter</code>, <code>ignore</code>, and <code>ignoreWithReporter</code>. 
   * </p>
   */
  override def groups: Map[String, Set[String]] = atomic.get.groupsMap
}

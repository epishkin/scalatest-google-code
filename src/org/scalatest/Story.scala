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

import scala.collection.immutable.ListSet
import java.util.ConcurrentModificationException
import java.util.concurrent.atomic.AtomicReference

/**
 * @author Bill Venners
 */
class Story(override val suiteName: String) extends Suite with GivenWhenThen {

  private val IgnoreGroupName = "org.scalatest.Ignore"

  private abstract class FunNode
  private case class Test(testName: String, testFunction: () => Unit) extends FunNode
  private case class Info(report: Report) extends FunNode

  // Access to the testNamesList, testsMap, and groupsMap must be synchronized, because the test methods are invoked by
  // the primary constructor, but testNames, groups, and runTest get invoked directly or indirectly
  // by execute. When running tests concurrently with ScalaTest Runner, different threads can
  // instantiate and execute the Suite. Instead of synchronizing, I put them in an immutable Bundle object (and
  // all three collections--testNamesList, testsMap, and groupsMap--are immuable collections), then I put the Bundle
  // in an AtomicReference. Since the expected use case is the test, testWithInformer, etc., methods will be called
  // from the primary constructor, which will be all done by one thread, I just in effect use optimistic locking on the Bundle.
  // If two threads ever called test at the same time, they could get a ConcurrentModificationException.
  // Test names are in reverse order of test registration method invocations
  private class Bundle private(
    val testNamesList: List[String],
    val doList: List[FunNode],
    val testsMap: Map[String, Test],
    val groupsMap: Map[String, Set[String]],
    val executeHasBeenInvoked: Boolean
  ) {
    def unpack = (testNamesList, doList, testsMap, groupsMap, executeHasBeenInvoked)
  }
  
  private object Bundle {
    def apply(
      testNamesList: List[String],
      doList: List[FunNode],
      testsMap: Map[String, Test],
      groupsMap: Map[String, Set[String]],
      executeHasBeenInvoked: Boolean
    ): Bundle =
      new Bundle(testNamesList, doList,testsMap, groupsMap, executeHasBeenInvoked)
  }

  private val atomic = new AtomicReference[Bundle](Bundle(List(), List(), Map(), Map(), false))

  private def updateAtomic(oldBundle: Bundle, newBundle: Bundle) {
    if (!atomic.compareAndSet(oldBundle, newBundle))
      throw new ConcurrentModificationException
  }
  
  // later will initialize with an informer that registers things between tests for later passing to the informer
  private var currentInformer = zombieInformer
  implicit def info: Informer = {
    if (currentInformer == null)
      registrationInformer
    else
      currentInformer
  }
  
  // Hey, my first lazy val. Turns out classes must be initialized before
  // the traits they mix in. Thus currentInformer was null when it was accessed via
  // an info outside a test. This solves the problem.
  private lazy val registrationInformer: Informer =
    new Informer {
      def nameForReport: String = suiteName
      def apply(report: Report) {
        if (report == null)
          throw new NullPointerException
        val oldBundle = atomic.get
        var (testNamesList, doList, testsMap, groupsMap, executeHasBeenInvoked) = oldBundle.unpack
        doList ::= Info(report)
        updateAtomic(oldBundle, Bundle(testNamesList, doList, testsMap, groupsMap, executeHasBeenInvoked))
      }
      def apply(message: String) {
        if (message == null)
          throw new NullPointerException
        apply(new SpecReport(nameForReport, message, message, "  " + message, true))
      }
    }
    
  private val zombieInformer =
    new Informer {
      private val complaint = "Sorry, you can only use FunSuite's info when executing the suite."
      def nameForReport: String = { throw new IllegalStateException(complaint) }
      def apply(report: Report) {
        if (report == null)
          throw new NullPointerException
        throw new IllegalStateException(complaint)
      }
      def apply(message: String) {
        if (message == null)
          throw new NullPointerException
        throw new IllegalStateException(complaint)
      }
    }

  /**
   * Register a test with the specified name, optional groups, and function value that takes no arguments.
   * This method will register the test for later execution via an invocation of one of the <code>execute</code>
   * methods. The passed test name must not have been registered previously on
   * this <code>FunSuite</code> instance.
   *
   * @throws IllegalArgumentException if <code>testName</code> had been registered previously
   */
  protected def scenario(testName: String, testGroups: Group*)(f: => Unit) {

    val oldBundle = atomic.get
    var (testNamesList, doList, testsMap, groupsMap, executeHasBeenInvoked) = oldBundle.unpack

    if (executeHasBeenInvoked)
      throw new IllegalStateException("You cannot register a test  on a FunSuite after execute has been invoked.")
    
    require(!testsMap.keySet.contains(testName), "Duplicate test name: " + testName)

    val testNode = Test(testName, f _)
    testsMap += (testName -> testNode)
    testNamesList ::= testName
    doList ::= testNode
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    if (!groupNames.isEmpty)
      groupsMap += (testName -> groupNames)

    updateAtomic(oldBundle, Bundle(testNamesList, doList, testsMap, groupsMap, executeHasBeenInvoked))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>execute</code>
   * methods. This method exists to make it easy to ignore an existing test method by changing the call to <code>test</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be executed, but a
   * report will be sent that indicates the test was ignored. The passed test name must not have been registered previously on
   * this <code>FunSuite</code> instance.
   *
   * @throws IllegalArgumentException if <code>testName</code> had been registered previously
   */
  protected def ignore(testName: String, testGroups: Group*)(f: => Unit) {

    scenario(testName)(f) // Call test without passing the groups

    val oldBundle = atomic.get
    var (testNamesList, doList, testsMap, groupsMap, executeHasBeenInvoked) = oldBundle.unpack

    val groupNames = Set[String]() ++ testGroups.map(_.name)
    groupsMap += (testName -> (groupNames + IgnoreGroupName))

    updateAtomic(oldBundle, Bundle(testNamesList, doList, testsMap, groupsMap, executeHasBeenInvoked))
  }

  /**
  * An immutable <code>Set</code> of test names. If this <code>FunSuite</code> contains no tests, this method returns an empty <code>Set</code>.
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

  // runTest should throw IAE if a test name is passed that doesn't exist. Looks like right now it just reports a test failure.
  /**
   * Run a test. This trait's implementation runs the test registered with the name specified by <code>testName</code>.
   *
   * @param testName the name of one test to execute.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param properties a <code>Map</code> of properties that can be used by the executing <code>Suite</code> of tests.
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, or <code>properties</code>
   *     is <code>null</code>.
   */
  protected override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any]) {

    if (testName == null || reporter == null || stopper == null || properties == null)
      throw new NullPointerException

    val wrappedReporter = wrapReporterIfNecessary(reporter)

    val specText = "Scenario: " + testName
    val report = new SpecReport(getTestNameForReport(testName), specText, specText, "\n  " + specText, true)

    wrappedReporter.testStarting(report)

    try {

      val theTest = atomic.get.testsMap(testName)

      val oldInformer = info
      try {
        currentInformer =
          new Informer {
            val nameForReport: String = getTestNameForReport(testName)
            def apply(report: Report) {
              if (report == null)
                throw new NullPointerException
              wrappedReporter.infoProvided(report)
            }
            def apply(message: String) {
              if (message == null)
                throw new NullPointerException
              val report = new SpecReport(nameForReport, message, message, "    " + message, true)
              wrappedReporter.infoProvided(report)
            }
          }
        theTest.testFunction()
      }
      finally {
        currentInformer = oldInformer
      }

      // Supress this report in the spec output. (Will show it if there was a failure, though.)
      val report = new SpecReport(getTestNameForReport(testName), specText, specText, "  " + specText, false)

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

    val specText = "Scenario: " + testName
    val report = new SpecReport(getTestNameForReport(testName), msg, specText, "  " + specText, true, Some(t), None)

    reporter.testFailed(report)
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> group names to which tests in this <code>FunSuite</code> belong, and values
   * the <code>Set</code> of test names that belong to each group. If this <code>FunSuite</code> contains no groups, this method returns an empty <code>Map</code>.
   *
   * <p>
   * This trait's implementation returns groups that were passed as strings contained in <code>Group</code> objects passed to 
   * methods <code>test</code>, <code>testWithInformer</code>, <code>ignore</code>, and <code>ignoreWithInformer</code>. 
   * </p>
   */
  override def groups: Map[String, Set[String]] = atomic.get.groupsMap

  private[scalatest] override def getTestNameForReport(testName: String) = {

    if (testName == null)
      throw new NullPointerException("testName was null")

    suiteName + ": " + testName
  }
  
  protected override def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
      goodies: Map[String, Any]) {

    if (testName == null)
      throw new NullPointerException("testName was null")
    if (reporter == null)
      throw new NullPointerException("reporter was null")
    if (stopper == null)
      throw new NullPointerException("stopper was null")
    if (includes == null)
      throw new NullPointerException("includes was null")
    if (excludes == null)
      throw new NullPointerException("excludes was null")
    if (goodies == null)
      throw new NullPointerException("goodies was null")

    // Wrap any non-DispatchReporter, non-CatchReporter in a CatchReporter,
    // so that exceptions are caught and transformed
    // into error messages on the standard error stream.
    val wrappedReporter = wrapReporterIfNecessary(reporter)

    // If a testName to execute is passed, just execute that, else execute the tests returned
    // by testNames.
    testName match {
      case Some(tn) => runTest(tn, wrappedReporter, stopper, goodies)
      case None => {
        val doList = atomic.get.doList.reverse
        for (node <- doList) {
          node match {
            case Info(message) => info(message)
            case Test(tn, _) =>
              if (!stopper.stopRequested && (includes.isEmpty || !(includes ** groups.getOrElse(tn, Set())).isEmpty)) {
                if (excludes.contains(IgnoreGroupName) && groups.getOrElse(tn, Set()).contains(IgnoreGroupName)) {
                  wrappedReporter.testIgnored(new Report(getTestNameForReport(tn), ""))
                }
                else if ((excludes ** groups.getOrElse(tn, Set())).isEmpty) {
                  runTest(tn, wrappedReporter, stopper, goodies)
                }
              }
          }
        }
      }
    }
  }

  override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
      goodies: Map[String, Any], distributor: Option[Distributor]) {

    // Set the flag that indicates execute has been invoked, which will disallow any further
    // invocations of "test" with an IllegalStateException.
    val oldBundle = atomic.get
    val (testNamesList, doList, testsMap, groupsMap, executeHasBeenInvoked) = oldBundle.unpack
    if (!executeHasBeenInvoked)
      updateAtomic(oldBundle, Bundle(testNamesList, doList, testsMap, groupsMap, true))

    val wrappedReporter = wrapReporterIfNecessary(reporter)

    currentInformer =
      new Informer {
        val nameForReport: String = suiteName
        def apply(report: Report) {
          if (report == null)
            throw new NullPointerException
          wrappedReporter.infoProvided(report)
        }
        def apply(message: String) {
          if (message == null)
            throw new NullPointerException
          val report = new Report(nameForReport, message)
          wrappedReporter.infoProvided(report)
        }
      }

    try {
      super.execute(testName, wrappedReporter, stopper, includes, excludes, goodies, distributor)
    }
    finally {
      currentInformer = zombieInformer
    }
  }
}

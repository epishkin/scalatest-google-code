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

trait Group

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
  // If two threads every called test at the same time, they could get a ConcurrentModificationException.
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

  protected def test(testName: String, groupClasses: Group*)(f: => Unit) {

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    require(!testsMap.keySet.contains(testName), "Duplicate test name: " + testName)

    testsMap += (testName -> PlainOldTest(testName, f _))
    testNamesList ::= testName
    val groupNames = Set[String]() ++ groupClasses.map(_.getClass.getName)
    if (!groupNames.isEmpty)
      groupsMap += (testName -> groupNames)

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  protected def testWithReporter(testName: String, groupClasses: Group*)(f: (Reporter) => Unit) {

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    require(!testsMap.keySet.contains(testName), "Duplicate test name: " + testName)

    testsMap += (testName -> ReporterTest(testName, f))
    testNamesList ::= testName
    val groupNames = Set[String]() ++ groupClasses.map(_.getClass.getName)
    if (!groupNames.isEmpty)
      groupsMap += (testName -> groupNames)

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  protected def ignore(testName: String, groupClasses: Group*)(f: => Unit) {

    test(testName)(f) // Call test without passing the groups

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    val groupNames = Set[String]() ++ groupClasses.map(_.getClass.getName)
    groupsMap += (testName -> (groupNames + IgnoreGroupName))

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  protected def ignoreWithReporter(testName: String, groupClasses: Group*)(f: (Reporter) => Unit) {

    testWithReporter(testName)(f) // Call testWithReporter without passing the groups

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    val groupNames = Set[String]() ++ groupClasses.map(_.getClass.getName)
    groupsMap += (testName -> (groupNames + IgnoreGroupName))

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  override def testNames: Set[String] = {
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

  override def groups: Map[String, Set[String]] = atomic.get.groupsMap
}

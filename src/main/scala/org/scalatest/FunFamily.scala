/*
 * Copyright 2001-2011 Artima, Inc.
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

import java.util.concurrent.atomic.AtomicReference
import java.util.ConcurrentModificationException
import org.scalatest.StackDepthExceptionHelper.getStackDepth
import FunSuite.IgnoreTagName 

// T will be () => Unit for FunSuite and FixtureParam => Any for FixtureFunSuite
private[scalatest] class FunFamily[T](concurrentBundleModResourceName: String, simpleClassName: String)  {
// class FunFamily[T] {

  abstract class FunNode
  case class TestNode(testName: String, fun: T) extends FunNode
  case class InfoNode(message: String) extends FunNode

  // Access to the testNamesList, testsMap, and tagsMap must be synchronized, because the test methods are invoked by
  // the primary constructor, but testNames, tags, and runTest get invoked directly or indirectly
  // by run. When running tests concurrently with ScalaTest Runner, different threads can
  // instantiate and run the suite. Instead of synchronizing, I put them in an immutable Bundle object (and
  // all three collections--testNamesList, testsMap, and tagsMap--are immuable collections), then I put the Bundle
  // in an AtomicReference. Since the expected use case is the test method will be called
  // from the primary constructor, which will be all done by one thread, I just in effect use optimistic locking on the Bundle.
  // If two threads ever called test at the same time, they could get a ConcurrentModificationException.
  // Test names are in reverse order of test registration method invocations
  class Bundle private(
    val testNamesList: List[String],
    val doList: List[FunNode],
    val testsMap: Map[String, TestNode],
    val tagsMap: Map[String, Set[String]],
    val registrationClosed: Boolean
  ) {
    def unpack = (testNamesList, doList, testsMap, tagsMap, registrationClosed)
  }

  object Bundle {
    def apply(
      testNamesList: List[String],
      doList: List[FunNode],
      testsMap: Map[String, TestNode],
      tagsMap: Map[String, Set[String]],
      registrationClosed: Boolean
    ): Bundle =
      new Bundle(testNamesList, doList,testsMap, tagsMap, registrationClosed)
  }

  val atomic = new AtomicReference[Bundle](Bundle(List(), List(), Map(), Map(), false))

  def updateAtomic(oldBundle: Bundle, newBundle: Bundle) {
    val shouldBeOldBundle = atomic.getAndSet(newBundle)
    if (!(shouldBeOldBundle eq oldBundle))
      throw new ConcurrentModificationException(Resources(concurrentBundleModResourceName))
  }

  class RegistrationInformer extends Informer {
    def apply(message: String) {
      if (message == null)
        throw new NullPointerException
      val oldBundle = atomic.get
      var (testNamesList, doList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack
      doList ::= InfoNode(message)
      updateAtomic(oldBundle, Bundle(testNamesList, doList, testsMap, tagsMap, registrationClosed))
    }
  }

  // The informer will be a registration informer until run is called for the first time. (This
  // is the registration phase of a FunSuite's lifecycle.)
  final val atomicInformer = new AtomicReference[Informer](new RegistrationInformer)

  val zombieInformer =
    new Informer {
      private val complaint = Resources("cantCallInfoNow", simpleClassName)
      def apply(message: String) {
        if (message == null)
          throw new NullPointerException
        throw new IllegalStateException(complaint)
      }
    }

  // Shared between FunSuite.test and fixture.FixtureFunSuite.test
  def testImpl(testName: String, f: T, sourceFileName: String, testTags: Tag*) {

    checkTestOrIgnoreParamsForNull(testName, testTags: _*)

    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("testCannotAppearInsideAnotherTest"), getStackDepth(sourceFileName, "test"))

    if (atomic.get.testsMap.keySet.contains(testName))
      throw new DuplicateTestNameException(testName, getStackDepth(sourceFileName, "test"))

    val oldBundle = atomic.get
    var (testNamesList, doList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack

    val testNode = TestNode(testName, f)
    testsMap += (testName -> testNode)
    testNamesList ::= testName
    doList ::= testNode
    val tagNames = Set[String]() ++ testTags.map(_.name)
    if (!tagNames.isEmpty)
      tagsMap += (testName -> tagNames)

    updateAtomic(oldBundle, Bundle(testNamesList, doList, testsMap, tagsMap, registrationClosed))
  }

  def ignoreImpl(testName: String, f: T, sourceFileName: String, testTags: Tag*) {

    checkTestOrIgnoreParamsForNull(testName, testTags: _*)

    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("ignoreCannotAppearInsideATest"), getStackDepth(sourceFileName, "ignore"))

    testImpl(testName, f, sourceFileName) // Call test without passing the tags

    val oldBundle = atomic.get
    var (testNamesList, doList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack

    val tagNames = Set[String]() ++ testTags.map(_.name)
    tagsMap += (testName -> (tagNames + IgnoreTagName))

    updateAtomic(oldBundle, Bundle(testNamesList, doList, testsMap, tagsMap, registrationClosed))
  }

  private def checkTestOrIgnoreParamsForNull(testName: String, testTags: Tag*) {
    if (testName == null)
      throw new NullPointerException("testName was null")
    if (testTags.exists(_ == null))
      throw new NullPointerException("a test tag was null")
  }

  def runTestsImpl(
    theSuite: Suite,
    testName: Option[String],
    reporter: Reporter,
    stopper: Stopper,
    filter: Filter,
    configMap: Map[String, Any],
    distributor: Option[Distributor],
    tracker: Tracker,
    info: String => Unit,
    runTest: (String, Reporter, Stopper, Map[String, Any], Tracker) => Unit
  ) {
    if (testName == null)
      throw new NullPointerException("testName was null")
    if (reporter == null)
      throw new NullPointerException("reporter was null")
    if (stopper == null)
      throw new NullPointerException("stopper was null")
    if (filter == null)
      throw new NullPointerException("filter was null")
    if (configMap == null)
      throw new NullPointerException("configMap was null")
    if (distributor == null)
      throw new NullPointerException("distributor was null")
    if (tracker == null)
      throw new NullPointerException("tracker was null")

    val stopRequested = stopper

    // Wrap any non-DispatchReporter, non-CatchReporter in a CatchReporter,
    // so that exceptions are caught and transformed
    // into error messages on the standard error stream.
    val report = theSuite.wrapReporterIfNecessary(reporter)

    // If a testName is passed to run, just run that, else run the tests returned
    // by testNames.
    testName match {
      case Some(tn) => runTest(tn, report, stopRequested, configMap, tracker)
      case None =>

        val doList = atomic.get.doList.reverse
        for (node <- doList) {
          node match {
            case InfoNode(message) => info(message)
            case TestNode(tn, _) =>
              val (filterTest, ignoreTest) = filter(tn, theSuite.tags)
              if (!filterTest)
                if (ignoreTest)
                  theSuite.reportTestIgnored(report, tracker, tn)
                else
                  runTest(tn, report, stopRequested, configMap, tracker)
          }
        }
    }
  }
}


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
import org.scalatest.StackDepthExceptionHelper.getStackDepthFun
import FunSuite.IgnoreTagName
import org.scalatest.NodeFamily.TestLeaf
import org.scalatest.Suite._
import fixture.NoArgTestWrapper
import org.scalatest.events.LineInFile
import org.scalatest.events.SeeStackDepthException
import scala.annotation.tailrec
import org.scalatest.PathEngine.isInTargetPath

// T will be () => Unit for FunSuite and FixtureParam => Any for fixture.FunSuite
private[scalatest] sealed abstract class SuperEngine[T](concurrentBundleModResourceName: String, simpleClassName: String)  {

  sealed abstract class Node(val parentOption: Option[Branch]) {
    def indentationLevel: Int = {
      def calcLevel(currentParentOpt: Option[Branch], currentLevel: Int): Int = 
        currentParentOpt match {
          case None => currentLevel
          case Some(parent) => calcLevel(parent.parentOption, currentLevel + 1)
        }
      val level = calcLevel(parentOption, -1)
      if (level < 0) 0 else level
    }
  }

  abstract class Branch(parentOption: Option[Branch]) extends Node(parentOption) {
    var subNodes: List[Node] = Nil
  }

  case object Trunk extends Branch(None)

  // Path traits need to register a function that returns a MessageRecordingInformer, because its tests are ru
  // at construction time when these five args aren't available.
  // type RecorderFun = (Suite, Reporter, Tracker, String, TestLeaf, Boolean) => MessageRecordingInformer2

  case class TestLeaf(
    parent: Branch,
    testName: String, // The full test name
    testText: String, // The last portion of the test name that showed up on an inner most nested level
    testFun: T, 
    lineInFile: Option[LineInFile],
    recordedDuration: Option[Long] = None,
    recordedMessages: Option[PathMessageRecordingInformer] = None
  ) extends Node(Some(parent))

  case class InfoLeaf(parent: Branch, message: String, location: Option[LineInFile]) extends Node(Some(parent))
  case class MarkupLeaf(parent: Branch, message: String, location: Option[LineInFile]) extends Node(Some(parent))

  case class DescriptionBranch(
    parent: Branch,
    descriptionText: String,
    childPrefix: Option[String], // If defined, put it at the beginning of any child descriptionText or testText 
    lineInFile: Option[LineInFile]
  ) extends Branch(Some(parent))   

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
    val currentBranch: Branch,
    val testNamesList: List[String],
    val testsMap: Map[String, TestLeaf],
    val tagsMap: Map[String, Set[String]],
    val registrationClosed: Boolean
  ) {
    def unpack = (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed)
  }

  object Bundle {
    def apply(
      currentBranch: Branch,
      testNamesList: List[String],
      testsMap: Map[String, TestLeaf],
      tagsMap: Map[String, Set[String]],
      registrationClosed: Boolean
    ): Bundle =
      new Bundle(currentBranch, testNamesList, testsMap, tagsMap, registrationClosed)
  }

  final val atomic = new AtomicReference[Bundle](Bundle(Trunk, List(), Map(), Map(), false))

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
      var (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack
      currentBranch.subNodes ::= InfoLeaf(currentBranch, message, getLineInFile(Thread.currentThread().getStackTrace, 2))
      updateAtomic(oldBundle, Bundle(currentBranch, testNamesList, testsMap, tagsMap, registrationClosed))
    }
  }

  class RegistrationDocumenter extends Documenter {
    def apply(message: String) {
      if (message == null)
        throw new NullPointerException
      val oldBundle = atomic.get
      var (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack
      currentBranch.subNodes ::= MarkupLeaf(currentBranch, message, getLineInFile(Thread.currentThread().getStackTrace, 2))
      updateAtomic(oldBundle, Bundle(currentBranch, testNamesList, testsMap, tagsMap, registrationClosed))
    }
  }

  // The informer will be a registration informer until run is called for the first time. (This
  // is the registration phase of a style trait's lifecycle.)
  final val atomicInformer = new AtomicReference[Informer](new RegistrationInformer)

  // The documenter will be a registration informer until run is called for the first time. (This
  // is the registration phase of a style trait's lifecycle.)
  final val atomicDocumenter = new AtomicReference[Documenter](new RegistrationDocumenter)

  final val zombieInformer =
    new Informer {
      private val complaint = Resources("cantCallInfoNow", simpleClassName)
      def apply(message: String) {
        if (message == null)
          throw new NullPointerException
        throw new IllegalStateException(complaint)
      }
    }

  final val zombieDocumenter =
    new Documenter {
      private val complaint = Resources("cantCallMarkupNow", simpleClassName)
      def apply(message: String) {
        if (message == null)
          throw new NullPointerException
        throw new IllegalStateException(complaint)
      }
    }

  private def checkTestOrIgnoreParamsForNull(testName: String, testTags: Tag*) {
    if (testName == null)
      throw new NullPointerException("testName was null")
    if (testTags.exists(_ == null))
      throw new NullPointerException("a test tag was null")
  }

  def runTestImpl(
    theSuite: Suite,
    testName: String,
    reporter: Reporter,
    stopper: Stopper,
    configMap: Map[String, Any],
    tracker: Tracker,
    includeIcon: Boolean,
    invokeWithFixture: TestLeaf => Unit
  ) {

    checkRunTestParamsForNull(testName, reporter, stopper, configMap, tracker)

    val (stopRequested, report, hasPublicNoArgConstructor, rerunnable, testStartTime) =
      theSuite.getRunTestGoodies(stopper, reporter, testName)

    if (!atomic.get.testsMap.contains(testName))
      throw new IllegalArgumentException("No test in this suite has name: \"" + testName + "\"")

    val theTest = atomic.get.testsMap(testName)

    reportTestStarting(theSuite, report, tracker, testName, theTest.testText, None, rerunnable, theTest.lineInFile)

    val testTextWithOptionalPrefix = prependChildPrefix(theTest.parent, theTest.testText)
    val formatter = getIndentedText(testTextWithOptionalPrefix, theTest.indentationLevel, includeIcon)

    val messageRecorderForThisTest = new MessageRecorder
    val informerForThisTest =
      MessageRecordingInformer(
        messageRecorderForThisTest,
        (message, isConstructingThread, testWasPending, testWasCanceled, location) => reportInfoProvided(theSuite, report, tracker, Some(testName), message, theTest.indentationLevel + 1, location, isConstructingThread, includeIcon, Some(testWasPending), Some(testWasCanceled))
      )

    val documenterForThisTest =
      MessageRecordingDocumenter(
        messageRecorderForThisTest,
        (message, isConstructingThread, testWasPending, testWasCanceled, location) => reportMarkupProvided(theSuite, report, tracker, Some(testName), message, theTest.indentationLevel + 1, location, isConstructingThread, Some(testWasPending), Some(testWasCanceled))
      )

    val oldInformer = atomicInformer.getAndSet(informerForThisTest)
    val oldDocumenter = atomicDocumenter.getAndSet(documenterForThisTest)
    var testWasPending = false
    var testWasCanceled = false

    try {

      invokeWithFixture(theTest)

      val duration = System.currentTimeMillis - testStartTime
      val durationToReport = theTest.recordedDuration.getOrElse(duration)
      reportTestSucceeded(theSuite, report, tracker, testName, theTest.testText, None, durationToReport, formatter, rerunnable, theTest.lineInFile)
    }
    catch { // XXX
      case _: TestPendingException =>
        val duration = System.currentTimeMillis - testStartTime
        reportTestPending(theSuite, report, tracker, testName, theTest.testText, None, duration, formatter, theTest.lineInFile)
        testWasPending = true // Set so info's printed out in the finally clause show up yellow
      case e: TestCanceledException =>
        val duration = System.currentTimeMillis - testStartTime
        reportTestCanceled(theSuite, report, e, testName, theTest.testText, None, rerunnable, tracker, duration, theTest.indentationLevel, includeIcon, theTest.lineInFile)
        testWasCanceled = true // Set so info's printed out in the finally clause show up yellow
      case e if !anErrorThatShouldCauseAnAbort(e) =>
        val duration = System.currentTimeMillis - testStartTime
        val durationToReport = theTest.recordedDuration.getOrElse(duration)
        reportTestFailed(theSuite, report, e, testName, theTest.testText, None, rerunnable, tracker, durationToReport, theTest.indentationLevel, includeIcon, Some(SeeStackDepthException))
      case e => throw e
    }
    finally {
      messageRecorderForThisTest.fireRecordedMessages(testWasPending, testWasCanceled)
      if (theTest.recordedMessages.isDefined)
        theTest.recordedMessages.get.fireRecordedMessages(testWasPending, theSuite, report, tracker, testName, theTest.indentationLevel + 1, includeIcon)
      val shouldBeInformerForThisTest = atomicInformer.getAndSet(oldInformer)
      val swapAndCompareSucceeded = shouldBeInformerForThisTest eq informerForThisTest
      if (!swapAndCompareSucceeded)
        throw new ConcurrentModificationException(Resources("concurrentInformerMod", theSuite.getClass.getName))
    }
  }

  private def runTestsInBranch(
    theSuite: Suite,
    branch: Branch,
    report: Reporter,
    stopRequested: Stopper,
    filter: Filter,
    configMap: Map[String, Any],
    tracker: Tracker,
    includeIcon: Boolean,
    runTest: (String, Reporter, Stopper, Map[String, Any], Tracker) => Unit
  ) {

    branch match {

      case desc @ DescriptionBranch(parent, descriptionText, _, lineInFile) =>

        val descriptionTextWithOptionalPrefix = prependChildPrefix(parent, descriptionText)
        val indentationLevel = desc.indentationLevel
        reportScopeOpened(theSuite, report, tracker, None, descriptionTextWithOptionalPrefix, indentationLevel, false, None, None, lineInFile)
        traverseSubNodes()
        reportScopeClosed(theSuite, report, tracker, None, descriptionTextWithOptionalPrefix, indentationLevel, false, None, None, lineInFile)

      case Trunk =>
        traverseSubNodes()
    }


    def traverseSubNodes() {
      branch.subNodes.reverse.foreach { node =>
        if (!stopRequested()) {
          node match {
            case testLeaf @ TestLeaf(_, testName, testText, _, _, _, _) =>
              val (filterTest, ignoreTest) = filter(testName, theSuite.tags)
              if (!filterTest)
                if (ignoreTest) {
                  val testTextWithOptionalPrefix = prependChildPrefix(branch, testText)
                  val theTest = atomic.get.testsMap(testName)
                  reportTestIgnored(theSuite, report, tracker, testName, testTextWithOptionalPrefix, None, testLeaf.indentationLevel, theTest.lineInFile)
                }
                else
                  runTest(testName, report, stopRequested, configMap, tracker)

            case infoLeaf @ InfoLeaf(_, message, location) =>
              reportInfoProvided(theSuite, report, tracker, None, message, infoLeaf.indentationLevel, location, true, includeIcon)

            case markupLeaf @ MarkupLeaf(_, message, location) =>
              reportMarkupProvided(theSuite, report, tracker, None, message, markupLeaf.indentationLevel, location, true)

            case branch: Branch => runTestsInBranch(theSuite, branch, report, stopRequested, filter, configMap, tracker, includeIcon, runTest)
          }
        }
      }
    }
  }

  def prependChildPrefix(branch: Branch, testText: String): String =
    branch match {
      case DescriptionBranch(_, _, Some(cp), _) => Resources("prefixSuffix", cp, testText)
      case _ => testText
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
    includeIcon: Boolean,
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
      case Some(tn) =>
        val (filterTest, ignoreTest) = filter(tn, theSuite.tags)
        if (!filterTest) {
          if (ignoreTest) {
            val theTest = atomic.get.testsMap(tn)
            reportTestIgnored(theSuite, report, tracker, tn, tn, getDecodedName(tn), 1, theTest.lineInFile)
          }
          else {
            runTest(tn, report, stopRequested, configMap, tracker)
          }
        }
      case None => runTestsInBranch(theSuite, Trunk, report, stopRequested, filter, configMap, tracker, includeIcon, runTest)
    }
  }

  def runImpl(
    theSuite: Suite,
    testName: Option[String],
    reporter: Reporter,
    stopper: Stopper,
    filter: Filter,
    configMap: Map[String, Any],
    distributor: Option[Distributor],
    tracker: Tracker,
    superRun: (Option[String], Reporter, Stopper, Filter, Map[String, Any], Option[Distributor], Tracker) => Unit
  ) {
    val stopRequested = stopper

    // Set the flag that indicates registration is closed (because run has now been invoked),
    // which will disallow any further invocations of "test" or "ignore" with
    // an RegistrationClosedException.    
    val oldBundle = atomic.get
    val (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack
    if (!registrationClosed)
      updateAtomic(oldBundle, Bundle(currentBranch, testNamesList, testsMap, tagsMap, true))

    val report = theSuite.wrapReporterIfNecessary(reporter)

    val informerForThisSuite =
      ConcurrentInformer(
        (message, isConstructingThread, location) => {
          reportInfoProvided(theSuite, report, tracker, None, message, 1, location, isConstructingThread)
        }
      )

    atomicInformer.set(informerForThisSuite)

    var swapAndCompareSucceeded = false
    try {
      superRun(testName, report, stopRequested, filter, configMap, distributor, tracker)
    }
    finally {
      val shouldBeInformerForThisSuite = atomicInformer.getAndSet(zombieInformer)
      swapAndCompareSucceeded = shouldBeInformerForThisSuite eq informerForThisSuite
    }
    if (!swapAndCompareSucceeded)  // Do outside finally to workaround Scala compiler bug
      throw new ConcurrentModificationException(Resources("concurrentInformerMod", theSuite.getClass.getName))
  }
  /*
  def describeImpl(description: String, fun: => Unit, registrationClosedResource: String, sourceFile: String, methodName: String) {

    val oldBundle = atomic.get
    val (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack

    if (registrationClosed)
      throw new TestRegistrationClosedException(Resources(registrationClosedResource), getStackDepth(sourceFile, methodName))

    val oldBranch = currentBranch
    val newBranch = DescriptionBranch(currentBranch, description, None)
    oldBranch.subNodes ::= newBranch

    // Update atomic, making the current branch to the new branch
    updateAtomic(oldBundle, Bundle(newBranch, testNamesList, testsMap, tagsMap, registrationClosed))

    fun // Execute the function

    { // Put the old branch back as the current branch
      val oldBundle = atomic.get
      val (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack
      updateAtomic(oldBundle, Bundle(oldBranch, testNamesList, testsMap, tagsMap, registrationClosed))
    }
  } */

  def registerNestedBranch(description: String, childPrefix: Option[String], fun: => Unit, registrationClosedResource: String, sourceFile: String, methodName: String, stackDepth: Int) {

    val oldBundle = atomic.get
    val (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack

    if (registrationClosed)
      throw new TestRegistrationClosedException(Resources(registrationClosedResource), getStackDepthFun(sourceFile, methodName, 1))

    val oldBranch = currentBranch
    val newBranch = DescriptionBranch(currentBranch, description, childPrefix, getLineInFile(Thread.currentThread().getStackTrace, stackDepth))
    oldBranch.subNodes ::= newBranch

    // Update atomic, making the current branch to the new branch
    updateAtomic(oldBundle, Bundle(newBranch, testNamesList, testsMap, tagsMap, registrationClosed))

    fun // Execute the function

    { // Put the old branch back as the current branch
      val oldBundle = atomic.get
      val (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack
      updateAtomic(oldBundle, Bundle(oldBranch, testNamesList, testsMap, tagsMap, registrationClosed))
    }
  }

  // Used by FlatSpec, which doesn't nest. So this one just makes a new one off of the trunk
  def registerFlatBranch(description: String, registrationClosedResource: String, sourceFile: String, methodName: String, stackDepth: Int) {

    val oldBundle = atomic.get
    val (_, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack

    if (registrationClosed)
      throw new TestRegistrationClosedException(Resources(registrationClosedResource), getStackDepthFun(sourceFile, methodName, 1))

    // Need to use Trunk here. I think it will be visible to all threads because
    // of the atomic, even though it wasn't inside it.
    val newBranch = DescriptionBranch(Trunk, description, None, getLineInFile(Thread.currentThread().getStackTrace, stackDepth))
    Trunk.subNodes ::= newBranch

    // Update atomic, making the current branch to the new branch
    updateAtomic(oldBundle, Bundle(newBranch, testNamesList, testsMap, tagsMap, registrationClosed))
  }

  def currentBranchIsTrunk: Boolean = {

    val oldBundle = atomic.get
    var (currentBranch, _, _, _, _) = oldBundle.unpack
    currentBranch == Trunk
  }

  // Path traits need to register the message recording informer, so it can fire any info events later
  def registerTest(testText: String, testFun: T, testRegistrationClosedResourceName: String, sourceFileName: String, methodName: String, stackDepth: Int, duration: Option[Long], informer: Option[PathMessageRecordingInformer], testTags: Tag*): String = { // returns testName

    checkRegisterTestParamsForNull(testText, testTags: _*)

    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources(testRegistrationClosedResourceName), getStackDepthFun(sourceFileName, methodName, 1))
//    throw new TestRegistrationClosedException(Resources("testCannotAppearInsideAnotherTest"), getStackDepth(sourceFileName, "test"))

    val oldBundle = atomic.get
    var (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack

    val testName = getTestName(testText, currentBranch)

    if (atomic.get.testsMap.keySet.contains(testName))
      throw new DuplicateTestNameException(testName, getStackDepthFun(sourceFileName, methodName, 1))

    val testLeaf = TestLeaf(currentBranch, testName, testText, testFun, getLineInFile(Thread.currentThread().getStackTrace, stackDepth), duration, informer)
    testsMap += (testName -> testLeaf)
    testNamesList ::= testName
    currentBranch.subNodes ::= testLeaf

    val tagNames = Set[String]() ++ testTags.map(_.name)
    if (!tagNames.isEmpty)
      tagsMap += (testName -> tagNames)

    updateAtomic(oldBundle, Bundle(currentBranch, testNamesList, testsMap, tagsMap, registrationClosed))

    testName
  }

  def registerIgnoredTest(testText: String, f: T, testRegistrationClosedResourceName: String, sourceFileName: String, methodName: String, stackDepth: Int, testTags: Tag*) {

    checkRegisterTestParamsForNull(testText, testTags: _*)

// If this works delete this. I think we can rely on registerTest's check
//    if (atomic.get.registrationClosed)
//      throw new TestRegistrationClosedException(Resources("ignoreCannotAppearInsideATest"), getStackDepth(sourceFileName, "ignore"))

    val testName = registerTest(testText, f, testRegistrationClosedResourceName, sourceFileName, methodName, stackDepth + 1, None, None) // Call test without passing the tags

    val oldBundle = atomic.get
    var (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack

    val tagNames = Set[String]() ++ testTags.map(_.name)
    tagsMap += (testName -> (tagNames + IgnoreTagName))

    updateAtomic(oldBundle, Bundle(currentBranch, testNamesList, testsMap, tagsMap, registrationClosed))
  }

  private[scalatest] def getTestNamePrefix(branch: Branch): String =
    branch match {
      case Trunk => ""
      // Call to getTestNamePrefix is not tail recursive, but I don't expect
      // the describe nesting to be very deep (famous last words).
      case DescriptionBranch(parent, descriptionText, childPrefix, lineInFile) =>
        val optionalChildPrefixAndDescriptionText =
          childPrefix match {
            case Some(cp) => Resources("prefixSuffix", descriptionText, cp)
            case _ => descriptionText
          }
        Resources("prefixSuffix", getTestNamePrefix(parent), optionalChildPrefixAndDescriptionText).trim
    }

  private[scalatest] def getTestName(testText: String, parent: Branch): String =
    Resources("prefixSuffix", getTestNamePrefix(parent), testText).trim

  private def checkRegisterTestParamsForNull(testText: String, testTags: Tag*) {
    if (testText == null)
      throw new NullPointerException("testText was null")
    if (testTags.exists(_ == null))
      throw new NullPointerException("a test tag was null")
  }
  
  private[scalatest] def testPath(testName: String): List[Int] = {
    val theTestOpt = atomic.get.testsMap.get(testName)
    theTestOpt match {
      case Some(theTest) =>
        findPath(theTest.parent, theTest, List.empty)
      case None => 
        throw new IllegalArgumentException("Test name '" + testName + "' not found.")
    }
  }
 
  @tailrec
  private def findPath(branch: Branch, node: Node, currentPath: List[Int]): List[Int] = {
    val idx = branch.subNodes.reverse.indexOf(node)
    branch.parentOption match {
      case Some(parent) => 
        findPath(parent, branch, idx :: currentPath)
      case None => 
        idx :: currentPath
    }
  }
}

private[scalatest] class Engine(concurrentBundleModResourceName: String, simpleClassName: String)
    extends SuperEngine[() => Unit](concurrentBundleModResourceName, simpleClassName)

private[scalatest] class FixtureEngine[FixtureParam](concurrentBundleModResourceName: String, simpleClassName: String)
    extends SuperEngine[FixtureParam => Any](concurrentBundleModResourceName, simpleClassName)

import scala.collection.mutable

private[scalatest] class PathEngine(concurrentBundleModResourceName: String, simpleClassName: String)
    extends Engine(concurrentBundleModResourceName, simpleClassName) { thisEngine =>
 
  private final var registeredPathSet = mutable.Set.empty[List[Int]] // TODO How can this be a final var?
  private final var targetPath: Option[List[Int]] = None

  // A describe clause registered no tests
  private var describeRegisteredNoTests: Boolean = false
  private var insideAPathTest: Boolean = false
  private var currentPath = List.empty[Int]
  private var usedPathSet = Set.empty[String]
  // Used in each instance to track the paths of things encountered, so can figure out
  // the next path. Each instance must use their own copies of currentPath and usedPathSet.
  def getNextPath() = {
    var next: List[Int] = null
    var count = 0
    while (next == null) {
      val candidate = currentPath ::: List(count)
      if (!usedPathSet.contains(candidate.toList.toString)) {
        next = candidate
        usedPathSet += candidate.toList.toString
      }
      else
        count += 1
    }
    next
  }
  
  // Once the target leaf has been reached for an instance, targetLeafHasBeenReached
  // will be set to true. And because of that, the path of the next describe or it encountered will
  // be placed into nextTargetPath. If no other describe or it clause comes along, then nextTargetPath
  // will stay at None, and the while loop will stop.
  @volatile private var targetLeafHasBeenReached = false
  @volatile private var nextTargetPath: Option[List[Int]] = None
  @volatile private var testResultsRegistered = false
  def ensureTestResultsRegistered(callingInstance: Suite with OneInstancePerTest) {
    synchronized {
      val isAnInitialInstance = targetPath.isEmpty
      // Only register tests if this is an initial instance (and only if they haven't
      // already been registered).
      if (isAnInitialInstance  && !testResultsRegistered) {
        testResultsRegistered = true
        var currentInstance: Suite = callingInstance
        while (nextTargetPath.isDefined) {
          targetPath = Some(nextTargetPath.get)
          PathEngine.setEngine(thisEngine)
          currentPath = List.empty[Int]
          usedPathSet = Set.empty[String]
          targetLeafHasBeenReached = false
          nextTargetPath = None
          // testResultsRegistered = false 
          currentInstance = callingInstance.newInstance  
        }
      }
    }
  }
/*
 * (theSuite, report, tracker, testName, theTest, includeIcon) => MessageRecordingInformer2
 * 
 * 
 */
  def handleTest(handlingSuite: Suite, testText: String, testFun: () => Unit, testRegistrationClosedResourceName: String, sourceFileName: String, methodName: String, stackDepth: Int, testTags: Tag*) {

    if (insideAPathTest) 
      throw new TestRegistrationClosedException(Resources("itCannotAppearInsideAnotherIt"), getStackDepthFun(sourceFileName, methodName, 1))
    
    insideAPathTest = true
    
    try {
      describeRegisteredNoTests = false
      val nextPath = getNextPath()
      if (isInTargetPath(nextPath, targetPath)) {
        // Default value of None indicates successful test
        var resultOfRunningTest: Option[Throwable] = None
          //theTest.indentationLevel + 1
        val informerForThisTest =
          PathMessageRecordingInformer( // TODO: Put locations into path traits!
            (message, wasConstructingThread, testWasPending, theSuite, report, tracker, testName, indentation, includeIcon, thread) =>
              reportInfoProvided(theSuite, report, tracker, Some(testName), message, indentation, None, wasConstructingThread, includeIcon, Some(testWasPending))
          )

        val oldInformer = atomicInformer.getAndSet(informerForThisTest)

        var durationOfRunningTest: Long = -1
        var startTime: Long = System.currentTimeMillis
        try { // TODO: Correctly record the time a test takes and report that
          // I think I need to replace the Informer with one that records the message and whether the
          // thread was this thread, and then...
          testFun()
          // If no exception, leave at None to indicate success
        }
        catch {
          case e: Throwable if !Suite.anErrorThatShouldCauseAnAbort(e) =>
            resultOfRunningTest = Some(e)
        }
        finally {
          durationOfRunningTest = System.currentTimeMillis - startTime
          val shouldBeInformerForThisTest = atomicInformer.getAndSet(oldInformer)
          val swapAndCompareSucceeded = shouldBeInformerForThisTest eq informerForThisTest
          if (!swapAndCompareSucceeded)
            throw new ConcurrentModificationException(Resources("concurrentInformerMod", handlingSuite.getClass.getName))
        }

        val newTestFun = { () =>
          // Here in the test function, replay those info calls. But I can't do this from different threads is the issue. Unless
          // I downcast to MessageRecordingInformer, and have another apply method on it that takes the true/false. Or override
          // runTestImpl and do something different. How about registering a different kind of test. EagerTest. Then it has
          // yes, that's how.
          if (resultOfRunningTest.isDefined)
            throw resultOfRunningTest.get
        }
        // register with         informerForThisTest.fireRecordedMessages(testWasPending)

       registerTest(testText, newTestFun, "itCannotAppearInsideAnotherIt", "FunSpec.scala", "apply", stackDepth, Some(durationOfRunningTest), Some(informerForThisTest), testTags: _*)
        targetLeafHasBeenReached = true
      }
      else if (targetLeafHasBeenReached && nextTargetPath.isEmpty) {
        nextTargetPath = Some(nextPath)
      }
    }
    finally {
      insideAPathTest = false
    }
  }

  def handleNestedBranch(description: String, childPrefix: Option[String], fun: => Unit, registrationClosedResource: String, sourceFile: String, methodName: String, stackDepth: Int) {

    if (insideAPathTest)
      throw new TestRegistrationClosedException(Resources("describeCannotAppearInsideAnIt"), getStackDepthFun(sourceFile, methodName, 1))

    val nextPath = getNextPath()
    // val nextPathZero = if (nextPath.length > 0) nextPath(0) else -1
    // val nextPathOne = if (nextPath.length > 1) nextPath(1) else -1
    // val nextPathTwo = if (nextPath.length > 2) nextPath(2) else -1
    // val isDef = targetPath.isDefined
    // val isInTarget = if (isDef) isInTargetPath(nextPath, targetPath) else false
    // val theTarget = if (isDef) targetPath.get else List()
    // val targetPathZero = if (theTarget.length > 0) theTarget(0) else -1
    // val targetPathOne = if (theTarget.length > 1) theTarget(1) else -1
    // val targetPathTwo = if (theTarget.length > 2) theTarget(2) else -1
    if (targetLeafHasBeenReached && nextTargetPath.isEmpty) {
      nextTargetPath = Some(nextPath)
    }
    else if (isInTargetPath(nextPath, targetPath)) { 
      val oldCurrentPath = currentPath // I added !previousDescribeWasEmpty to get a sibling describe following an empty describe to get executed.
      currentPath = nextPath
      if (!registeredPathSet.contains(nextPath)) {
        // Set this to true before executing the describe. If it has a test, then handleTest will be invoked
        // and it will set previousDescribeWasEmpty back to false
        describeRegisteredNoTests = true
        registerNestedBranch(description, None, fun, "describeCannotAppearInsideAnIt", "FunSpec.scala", "describe", stackDepth)
        registeredPathSet += nextPath
        if (describeRegisteredNoTests)
          targetLeafHasBeenReached = true
      }
      else {
        navigateToNestedBranch(nextPath, fun, "describeCannotAppearInsideAnIt", "FunSpec.scala", "describe")
      }
      currentPath = oldCurrentPath
    }
  }

 def navigateToNestedBranch(path: List[Int], fun: => Unit, registrationClosedResource: String, sourceFile: String, methodName: String) {

    val oldBundle = atomic.get
    val (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack

    if (registrationClosed)
      throw new TestRegistrationClosedException(Resources(registrationClosedResource), getStackDepthFun(sourceFile, methodName, 1))

    // First look in current branch's subnodes for another branch
    def getBranch(b: Branch, path: List[Int]): Branch = {
      path match {
        case Nil => b
        case i :: tail =>
          val index = b.subNodes.length - 1 - i // They are in reverse order
          getBranch(b.subNodes(index).asInstanceOf[Branch], tail)
      }
    }
    
    val oldBranch = currentBranch
    val newBranch = getBranch(Trunk, path)
    // oldBranch.subNodes ::= newBranch

    // Update atomic, making the current branch to the new branch
    updateAtomic(oldBundle, Bundle(newBranch, testNamesList, testsMap, tagsMap, registrationClosed))

    fun // Execute the function

    { // Put the old branch back as the current branch
      val oldBundle = atomic.get
      val (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack
      updateAtomic(oldBundle, Bundle(oldBranch, testNamesList, testsMap, tagsMap, registrationClosed))
    }
  }
 
   def runPathTestsImpl(
    theSuite: Suite,
    testName: Option[String],
    reporter: Reporter,
    stopper: Stopper,
    filter: Filter,
    configMap: Map[String, Any],
    distributor: Option[Distributor],
    tracker: Tracker,
    info: String => Unit,
    includeIcon: Boolean,
    runTest: (String, Reporter, Stopper, Map[String, Any], Tracker) => Unit
  ) {
     // All but one line of code copied from runImpl. Factor out duplication later...
    val stopRequested = stopper

    // Set the flag that indicates registration is closed (because run has now been invoked),
    // which will disallow any further invocations of "test" or "ignore" with
    // an RegistrationClosedException.    
    val oldBundle = atomic.get
    val (currentBranch, testNamesList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack
    if (!registrationClosed)
      updateAtomic(oldBundle, Bundle(currentBranch, testNamesList, testsMap, tagsMap, true))

    val report = theSuite.wrapReporterIfNecessary(reporter)

    val informerForThisSuite =
      ConcurrentInformer(
        (message, isConstructingThread, location) =>
          reportInfoProvided(theSuite, report, tracker, None, message, 1, location, isConstructingThread)
      )

    atomicInformer.set(informerForThisSuite)

    var swapAndCompareSucceeded = false
    try {
     runTestsImpl(theSuite, testName, reporter, stopper, filter, configMap, distributor, tracker, info, true, runTest)
    }
    finally {
      val shouldBeInformerForThisSuite = atomicInformer.getAndSet(zombieInformer)
      swapAndCompareSucceeded = shouldBeInformerForThisSuite eq informerForThisSuite
    }
    if (!swapAndCompareSucceeded)  // Do outside finally to workaround Scala compiler bug
      throw new ConcurrentModificationException(Resources("concurrentInformerMod", theSuite.getClass.getName))
  }
   
  def handleIgnoredTest(testText: String, f: () => Unit, testRegistrationClosedResourceName: String, sourceFileName: String, methodName: String, stackDepth: Int, testTags: Tag*) {

    if (insideAPathTest) 
      throw new TestRegistrationClosedException(Resources("ignoreCannotAppearInsideAnIt"), getStackDepthFun(sourceFileName, methodName, 1))
    
    describeRegisteredNoTests = false
    val nextPath = getNextPath()
    if (isInTargetPath(nextPath, targetPath)) {

      super.registerIgnoredTest(testText, f, "ignoreCannotAppearInsideAnIt", "FunSpec.scala", "ignore", stackDepth, testTags: _*)
      targetLeafHasBeenReached = true
    }
    else if (targetLeafHasBeenReached && nextTargetPath.isEmpty) {
      nextTargetPath = Some(nextPath)
    }
  }
}

private[scalatest] object PathEngine {
  
   private[this] val engine = new ThreadLocal[PathEngine]

   def setEngine(en: PathEngine) {
     if (engine.get != null)
       throw new IllegalStateException("Engine was already defined when setEngine was called")
     engine.set(en)
   }

   def getEngine(): PathEngine = {
     val en = engine.get
     engine.set(null)
     if (en == null) (new PathEngine("concurrentSpecMod", "Spec")) else en
   }
   
  /*
   * First time this is instantiated, targetPath will be None. In that case, execute the
   * first test, and each describe clause on the way to the first test (the all zeros path).
   */
  def isInTargetPath(currentPath: List[Int], targetPath: Option[List[Int]]): Boolean = {
    def allZeros(xs: List[Int]) = xs.count(_ == 0) == xs.length
    if (targetPath.isEmpty)
      allZeros(currentPath)
    else {
      if (currentPath.length < targetPath.get.length)
        targetPath.get.take(currentPath.length) == currentPath
      else if (currentPath.length > targetPath.get.length)
        (currentPath.take(targetPath.get.length) == targetPath.get) && (!currentPath.drop(targetPath.get.length).exists(_ != 0)) // TODO: deal with sibling describes
      else
        targetPath.get == currentPath
    }
  }
}

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
package org.scalatest.fixture

import FixtureNodeFamily._
import matchers.{CanVerb, ResultOfAfterWordApplication, ShouldVerb, BehaveWord, MustVerb,
  StringVerbBlockRegistration}
import scala.collection.immutable.ListSet
import org.scalatest.StackDepthExceptionHelper.getStackDepth
import java.util.concurrent.atomic.AtomicReference
import java.util.ConcurrentModificationException
import org.scalatest.events._

/**
 *
 * @author Bill Venners
 */
trait FixtureWordSpec extends FixtureSuite with ShouldVerb with MustVerb with CanVerb { thisSuite =>

  private val IgnoreTagName = "org.scalatest.Ignore"

  private class Bundle private(
    val trunk: Trunk,
    val currentBranch: Branch,
    val tagsMap: Map[String, Set[String]],

    // All tests, in reverse order of registration
    val testsList: List[FixtureTestLeaf[Fixture]],

    // Used to detect at runtime that they've stuck a describe or an it inside an it,
    // which should result in a TestRegistrationClosedException
    val registrationClosed: Boolean
  ) {
    def unpack = (trunk, currentBranch, tagsMap, testsList, registrationClosed)
  }

  private object Bundle {
    def apply(
      trunk: Trunk,
      currentBranch: Branch,
      tagsMap: Map[String, Set[String]],
      testsList: List[FixtureTestLeaf[Fixture]],
      registrationClosed: Boolean
    ): Bundle =
      new Bundle(trunk, currentBranch, tagsMap, testsList, registrationClosed)

    def initialize(
      trunk: Trunk,
      tagsMap: Map[String, Set[String]],
      testsList: List[FixtureTestLeaf[Fixture]],
      registrationClosed: Boolean
    ): Bundle =
      new Bundle(trunk, trunk, tagsMap, testsList, registrationClosed)
  }

  private val atomic =
    new AtomicReference[Bundle](
      Bundle.initialize(new Trunk, Map(), List[FixtureTestLeaf[Fixture]](), false)
    )

  private val shouldRarelyIfEverBeSeen = """
    Two threads attempted to modify Spec's internal data, which should only be
    modified by the thread that constructs the object. This likely means that a subclass
    has allowed the this reference to escape during construction, and some other thread
    attempted to invoke the "describe" or "it" method on the object before the first
    thread completed its construction.
  """

  private def updateAtomic(oldBundle: Bundle, newBundle: Bundle) {
    if (!atomic.compareAndSet(oldBundle, newBundle))
      throw new ConcurrentModificationException(shouldRarelyIfEverBeSeen)
  }

  private def registerTest(specText: String, f: (Fixture) => Unit) = {

    val oldBundle = atomic.get
    var (trunk, currentBranch, tagsMap, testsList, registrationClosed) = oldBundle.unpack

    val testName = getTestName(specText, currentBranch)
    if (testsList.exists(_.testName == testName)) {
      throw new DuplicateTestNameException(testName, getStackDepth("Spec.scala", "it"))
    }
    val testShortName = specText
    val test = FixtureTestLeaf(currentBranch, testName, specText, f)
    currentBranch.subNodes ::= test
    testsList ::= test

    updateAtomic(oldBundle, Bundle(trunk, currentBranch, tagsMap, testsList, registrationClosed))

    testName
  }

  // later will initialize with an informer that registers things between tests for later passing to the informer
  private val atomicInformer = new AtomicReference[Informer](zombieInformer)

  /**
   * Returns an <code>Informer</code> that during test execution will forward strings (and other objects) passed to its
   * apply method to the current reporter. If invoked inside a test function, it will forward the information to
   * the current reporter immediately. If invoked outside a test function, but in the primary constructor, it
   * will register the info for forwarding later during test execution. If invoked at any other time, it will
   * throw an exception. This method can be called safely by any thread.
   */
  implicit def info: Informer = {
    if (atomicInformer == null || atomicInformer.get == null)
      registrationInformer
    else
      atomicInformer.get
  }

  // Must be a lazy val because classes are initialized before
  // the traits they mix in. Thus currentInformer will be null if it is accessed via
  // an info call outside a test. Making it lazy solves the problem.
  private lazy val registrationInformer: Informer =
    new Informer {
      def apply(message: String) {
        if (message == null)
          throw new NullPointerException

        val oldBundle = atomic.get
        var (trunk, currentBranch, tagsMap, testsList, registrationClosed) = oldBundle.unpack

        currentBranch.subNodes ::= InfoLeaf(currentBranch, message)

        updateAtomic(oldBundle, Bundle(trunk, currentBranch, tagsMap, testsList, registrationClosed))
      }
    }

  // This must *not* be lazy, so that it will stay null while the class's constructors are being executed,
  // because that's how I detect that construction is happenning (the registration phase) in the info method.
  private val zombieInformer =
    new Informer {
      private val complaint = "Sorry, you can only use Spec's info when executing the suite."
      def apply(message: String) {
        if (message == null)
          throw new NullPointerException
        throw new IllegalStateException(complaint)
      }
    }

  /**
   * Register a test with the given spec text, optional tags, and test function value that takes no arguments.
   * An invocation of this method is called an &#8220;example.&#8221;
   *
   * This method will register the test for later execution via an invocation of one of the <code>execute</code>
   * methods. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>Spec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testTags the optional list of tags for this test
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullPointerException if <code>specText</code> or any passed test tag is <code>null</code>
   */
  private def registerTestToRun(specText: String, testTags: List[Tag], testFun: Fixture => Unit) {

    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("itCannotAppearInsideAnotherIt"), getStackDepth("Spec.scala", "it"))
    if (specText == null)
      throw new NullPointerException("specText was null")
    if (testTags.exists(_ == null))
      throw new NullPointerException("a test tag was null")

    val testName = registerTest(specText, testFun)

    val oldBundle = atomic.get
    var (trunk, currentBranch, tagsMap, testsList, registrationClosed2) = oldBundle.unpack
    val tagNames = Set[String]() ++ testTags.map(_.name)
    if (!tagNames.isEmpty)
      tagsMap += (testName -> tagNames)

    updateAtomic(oldBundle, Bundle(trunk, currentBranch, tagsMap, testsList, registrationClosed2))
  }

  /**
   * Register a test with the given spec text and test function value that takes no arguments.
   *
   * This method will register the test for later execution via an invocation of one of the <code>execute</code>
   * methods. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>Spec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullPointerException if <code>specText</code> or any passed test tag is <code>null</code>
   */
  /* protected def it(specText: String)(testFun: => Unit) {
    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("itCannotAppearInsideAnotherIt"), getStackDepth("Spec.scala", "it"))
    it(specText, Array[Tag](): _*)(testFun)
  } */

  /**
   * Register a test to ignore, which has the given spec text, optional tags, and test function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>execute</code>
   * methods. This method exists to make it easy to ignore an existing test method by changing the call to <code>it</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be executed, but a
   * report will be sent that indicates the test was ignored. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>Spec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testTags the optional list of tags for this test
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullPointerException if <code>specText</code> or any passed test tag is <code>null</code>
   */
  private def registerTestToIgnore(specText: String, testTags: List[Tag], testFun: Fixture => Unit) {
    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("ignoreCannotAppearInsideAnIt"), getStackDepth("Spec.scala", "ignore"))
    if (specText == null)
      throw new NullPointerException("specText was null")
    if (testTags.exists(_ == null))
      throw new NullPointerException("a test tag was null")
    val testName = registerTest(specText, testFun)
    val tagNames = Set[String]() ++ testTags.map(_.name)
    val oldBundle = atomic.get
    var (trunk, currentBranch, tagsMap, testsList, registrationClosed) = oldBundle.unpack
    tagsMap += (testName -> (tagNames + IgnoreTagName))
    updateAtomic(oldBundle, Bundle(trunk, currentBranch, tagsMap, testsList, registrationClosed))
  }

  /**
   * Register a test to ignore, which has the given spec text and test function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>execute</code>
   * methods. This method exists to make it easy to ignore an existing test method by changing the call to <code>it</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be executed, but a
   * report will be sent that indicates the test was ignored. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>Spec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullPointerException if <code>specText</code> or any passed test tag is <code>null</code>
   */
  /* protected def ignore(specText: String)(testFun: => Unit) {
    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("ignoreCannotAppearInsideAnIt"), getStackDepth("Spec.scala", "ignore"))
    ignore(specText, Array[Tag](): _*)(testFun)
  } */

  /**
   * Describe a &#8220;subject&#8221; being specified and tested by the passed function value. The
   * passed function value may contain more describers (defined with <code>describe</code>) and/or tests
   * (defined with <code>it</code>). This trait's implementation of this method will register the
   * description string and immediately invoke the passed function.
   */
  private def registerVerbBranch(description: String, verb: String, f: () => Unit) {
    registerBranch(f, VerbBranch(_, description, verb))
  }

  private def registerDescriptionBranch(description: String, f: () => Unit) {
    registerBranch(f, DescriptionBranch(_, description))
  }

  private def registerBranch(f: () => Unit, constructBranch: Branch => Branch) {

    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("describeCannotAppearInsideAnIt"), getStackDepth("Spec.scala", "describe"))

    def createNewBranch() = {
      val oldBundle = atomic.get
      var (trunk, currentBranch, tagsMap, testsList, registrationClosed) = oldBundle.unpack

      val newBranch = constructBranch(currentBranch)
      // val newBranch = VerbBranch(currentBranch, description, verb)
      val oldBranch = currentBranch
      currentBranch.subNodes ::= newBranch
      currentBranch = newBranch

      updateAtomic(oldBundle, Bundle(trunk, currentBranch, tagsMap, testsList, registrationClosed))

      oldBranch
    }

    val oldBranch = createNewBranch()

    f()

    val oldBundle = atomic.get
    val (trunk, currentBranch, tagsMap, testsList, registrationClosed) = oldBundle.unpack

    updateAtomic(oldBundle, Bundle(trunk, oldBranch, tagsMap, testsList, registrationClosed))
  }

  protected class StringTaggedAs(specText: String, tags: List[Tag]) {
    def in(testFun: Fixture => Unit) {
      registerTestToRun(specText, tags, testFun)
    }
    // "test this" taggedAs(mytags.SlowAsMolasses) is (pending)
    //                                             ^
    def is(testFun: => PendingNothing) {
      registerTestToRun(specText, tags, unusedFixture => testFun)
    }
    // "hi" taggedAs(mytags.SlowAsMolasses) ignore { fixture => }
    def ignore(testFun: Fixture => Unit) {
      registerTestToIgnore(specText, tags, testFun)
    }
  }

  protected class IgnoreTestStringTaggedAs(specText: String, tags: List[Tag]) {
    def in(testFun: Fixture => Unit) {
      registerTestToIgnore(specText, tags, testFun)
    }
  }

  protected class WordSpecStringWrapper(string: String) {
    def in(testFun: Fixture => Unit) {
      registerTestToRun(string, List(), testFun)
    }
    // "test that" is (pending)
    //             ^
    def is(testFun: => PendingNothing) {
      registerTestToRun(string, List(), unusedFixtre => testFun)
    }
    def ignore(testFun: Fixture => Unit) {
      registerTestToIgnore(string, List(), testFun)
    }
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new StringTaggedAs(string, tagList)
    }
    /* def can(f: => Unit) {
      registerVerbBranch(string, "can", f _)
    } */
    def when(f: => Unit) {
      registerDescriptionBranch(string + " (when", f _)
    }
    def when(resultOfAfterWordApplication: ResultOfAfterWordApplication) {
      registerDescriptionBranch(string + " (when " + resultOfAfterWordApplication.text, resultOfAfterWordApplication.f)
    }
    def that(f: => Unit) {
      registerDescriptionBranch(string + " that", f _)
    }
    def that(resultOfAfterWordApplication: ResultOfAfterWordApplication) {
      registerDescriptionBranch(string + " that " + resultOfAfterWordApplication.text, resultOfAfterWordApplication.f)
    }
  }

  protected class AfterWord(text: String) {
    def apply(f: => Unit) = new ResultOfAfterWordApplication(text, f _)
  }

  protected def afterWord(text: String) = new AfterWord(text)

  protected implicit def convertToWordSpecStringWrapper(s: String) = new WordSpecStringWrapper(s)

  protected class IgnoredTest(specText: String) {
    def in(f: Fixture => Unit) {
      registerTestToIgnore(specText, List(), f)
    }
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new IgnoreTestStringTaggedAs(specText, tagList)
    }
  }
  protected class IgnoreWord {
    def test(specText: String) = new IgnoredTest(specText)
  }

  protected val ignore = new IgnoreWord

  implicit val doVerbThing: StringVerbBlockRegistration =
    new StringVerbBlockRegistration {
      def apply(left: String, verb: String, f: () => Unit) = registerVerbBranch(left, verb, f)
    }


  implicit val doAfterVerbThing: (String, ResultOfAfterWordApplication, String) => Unit = {
    (left, resultOfAfterWordApplication, verb) => {
      val afterWordFunction =
        () => {
          registerDescriptionBranch(resultOfAfterWordApplication.text, resultOfAfterWordApplication.f)
        }
      registerVerbBranch(left, verb, afterWordFunction)
    }
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> tag names to which tests in this <code>Spec</code> belong, and values
   * the <code>Set</code> of test names that belong to each tag. If this <code>FunSuite</code> contains no tags, this method returns an empty <code>Map</code>.
   *
   * <p>
   * This trait's implementation returns tags that were passed as strings contained in <code>Tag</code> objects passed to
   * methods <code>test</code> and <code>ignore</code>.
   * </p>
   */
  override def tags: Map[String, Set[String]] = atomic.get.tagsMap

  private def runTestsInBranch(branch: Branch, reporter: Reporter, stopper: Stopper, filter: Filter, configMap: Map[String, Any], tracker: Tracker) {

    val stopRequested = stopper
    // Wrap any non-DispatchReporter, non-CatchReporter in a CatchReporter,
    // so that exceptions are caught and transformed
    // into error messages on the standard error stream.
    val report = wrapReporterIfNecessary(reporter)
    branch match {
      case desc @ VerbBranch(_, descriptionName, verb) =>

        // Need to use the full name of the description, which includes all the descriptions it is nested inside
        // Call getPrefix and pass in this Desc, to get the full name
        val descriptionFullName = getPrefixWithoutVerb(desc).trim

        // Call getTestNameForReport with the description, because that puts the Suite name
        // in front of the description, which looks good in the regular report.
        report(InfoProvided(tracker.nextOrdinal(), descriptionFullName, Some(NameInfo(thisSuite.suiteName, Some(thisSuite.getClass.getName), None)), None, Some(IndentedText(descriptionFullName, descriptionFullName, 0))))

      case _ =>
    }
    branch.subNodes.reverse.foreach(
      _ match {
        case FixtureTestLeaf(parent, tn, specText, _) =>
          if (!stopRequested()) { // TODO: Seems odd to me to check for stop here but still fire infos
            val (filterTest, ignoreTest) = filter(tn, tags)
            if (!filterTest)
              if (ignoreTest) {
                val testSucceededIcon = Resources("testSucceededIconChar")
                val formattedSpecText = Resources("iconPlusShortName", testSucceededIcon, getFormattedSpecTextPrefix(parent) + " " + specText)
                report(TestIgnored(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), tn, Some(IndentedText(formattedSpecText, specText, 1))))
              }
              else
                runTest(tn, report, stopRequested, configMap, tracker)
          }
        case InfoLeaf(_, message) =>
          val infoProvidedIcon = Resources("infoProvidedIconChar")
          val formattedText = Resources("iconPlusShortName", infoProvidedIcon, message)
          report(InfoProvided(tracker.nextOrdinal(), message,
            Some(NameInfo(thisSuite.suiteName, Some(thisSuite.getClass.getName), None)),
              None, Some(IndentedText(formattedText, message, 1))))
        case branch: Branch => runTestsInBranch(branch, reporter, stopRequested, filter, configMap, tracker)
      }
    )
  }

  /**
   * Run a test. This trait's implementation runs the test registered with the name specified by
   * <code>testName</code>. Each test's name is a concatenation of the text of all describers surrounding a test,
   * from outside in, and the test's  spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.)
   *
   * @param testName the name of one test to execute.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param configMap a <code>Map</code> of properties that can be used by this <code>Spec</code>'s executing tests.
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, or <code>configMap</code>
   *     is <code>null</code>.
   */
  protected override def runTest(testName: String, reporter: Reporter, stopper: Stopper, configMap: Map[String, Any], tracker: Tracker) {

    if (testName == null || reporter == null || stopper == null || configMap == null)
      throw new NullPointerException

    atomic.get.testsList.find(_.testName == testName) match {
      case None => throw new IllegalArgumentException("Requested test doesn't exist: " + testName)
      case Some(test) => {
        val report = wrapReporterIfNecessary(reporter)

        val testSucceededIcon = Resources("testSucceededIconChar")
        val formattedSpecText = Resources("iconPlusShortName", testSucceededIcon, getFormattedSpecTextPrefix(test.parent) + " " + test.specText)

        // Create a Rerunner if the Spec has a no-arg constructor
        val hasPublicNoArgConstructor = org.scalatest.Suite.checkForPublicNoArgConstructor(getClass)

        val rerunnable =
          if (hasPublicNoArgConstructor)
            Some(new TestRerunner(getClass.getName, testName))
          else
            None

        val testStartTime = System.currentTimeMillis

        // A TestStarting event won't normally show up in a specification-style output, but
        // will show up in a test-style output.
        report(TestStarting(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), test.testName, Some(MotionToSuppress), rerunnable))

        val formatter = IndentedText(formattedSpecText, getFormattedSpecTextPrefix(test.parent) + " " + test.specText, 1)
        val oldInformer = atomicInformer.get
        val informerForThisTest =
          new MessageRecordingInformer(NameInfo(thisSuite.suiteName, Some(thisSuite.getClass.getName), Some(testName))) {
            def apply(message: String) {
              if (message == null)
                throw new NullPointerException
              if (shouldRecord)
                record(message)
              else {
                val infoProvidedIcon = Resources("infoProvidedIconChar")
                val formattedText = "  " + Resources("iconPlusShortName", infoProvidedIcon, message)
                report(InfoProvided(tracker.nextOrdinal(), message, nameInfoForCurrentThread, None, Some(IndentedText(formattedText, message, 2))))
              }
            }
          }

        atomicInformer.set(informerForThisTest)
        try {
          withFixture(new TestFunAndConfigMap(test.f, configMap))

          val duration = System.currentTimeMillis - testStartTime
          report(TestSucceeded(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), test.testName, Some(duration), Some(formatter), rerunnable))
        }
        catch {
          case _: TestPendingException =>
            report(TestPending(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), test.testName, Some(formatter)))
          case e: Exception =>
            val duration = System.currentTimeMillis - testStartTime
            handleFailedTest(e, false, test.testName, test.specText, formattedSpecText, rerunnable, report, tracker, duration)
          case ae: AssertionError =>
            val duration = System.currentTimeMillis - testStartTime
            handleFailedTest(ae, false, test.testName, test.specText, formattedSpecText, rerunnable, report, tracker, duration)
        }
        finally {
          // send out any recorded messages
          for (message <- informerForThisTest.recordedMessages) {
            val infoProvidedIcon = Resources("infoProvidedIconChar")
            val formattedText = "  " + Resources("iconPlusShortName", infoProvidedIcon, message)
            report(InfoProvided(tracker.nextOrdinal(), message, informerForThisTest.nameInfoForCurrentThread, None, Some(IndentedText(formattedText, message, 2))))
          }

          val success = atomicInformer.compareAndSet(informerForThisTest, oldInformer)
          val rarelyIfEverSeen = """
            Two threads have apparently attempted to run tests at the same time. This has
            resulted in both threads attempting to change the current informer.
          """
          if (!success)
            throw new ConcurrentModificationException(rarelyIfEverSeen)
        }
      }
    }
  }

  private[scalatest] override def getTestNameForReport(testName: String) = {

    if (testName == null)
      throw new NullPointerException("testName was null")

    suiteName + ", " + testName
  }

  private def handleFailedTest(throwable: Throwable, hasPublicNoArgConstructor: Boolean, testName: String,
      specText: String, formattedSpecText: String, rerunnable: Option[Rerunner], report: Reporter, tracker: Tracker, duration: Long) {

    val message =
      if (throwable.getMessage != null) // [bv: this could be factored out into a helper method]
        throwable.getMessage
      else
        throwable.toString

    val formatter = IndentedText(formattedSpecText, specText, 1)
    report(TestFailed(tracker.nextOrdinal(), message, thisSuite.suiteName, Some(thisSuite.getClass.getName), testName, Some(throwable), Some(duration), Some(formatter), rerunnable))
  }

  /**
   * <p>
   * Run zero to many of this <code>Spec</code>'s tests.
   * </p>
   *
   * <p>
   * This method takes a <code>testName</code> parameter that optionally specifies a test to invoke.
   * If <code>testName</code> is <code>Some</code>, this trait's implementation of this method
   * invokes <code>runTest</code> on this object, passing in:
   * </p>
   *
   * <ul>
   * <li><code>testName</code> - the <code>String</code> value of the <code>testName</code> <code>Option</code> passed
   *   to this method</li>
   * <li><code>reporter</code> - the <code>Reporter</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>stopper</code> - the <code>Stopper</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>configMap</code> - the <code>configMap</code> passed to this method, or one that wraps and delegates to it</li>
   * </ul>
   *
   * <p>
   * This method takes a <code>Set</code> of tag names that should be included (<code>tagsToInclude</code>), and a <code>Set</code>
   * that should be excluded (<code>tagsToExclude</code>), when deciding which of this <code>Suite</code>'s tests to execute.
   * If <code>tagsToInclude</code> is empty, all tests will be executed
   * except those those belonging to tags listed in the <code>tagsToExclude</code> <code>Set</code>. If <code>tagsToInclude</code> is non-empty, only tests
   * belonging to tags mentioned in <code>tagsToInclude</code>, and not mentioned in <code>tagsToExclude</code>
   * will be executed. However, if <code>testName</code> is <code>Some</code>, <code>tagsToInclude</code> and <code>tagsToExclude</code> are essentially ignored.
   * Only if <code>testName</code> is <code>None</code> will <code>tagsToInclude</code> and <code>tagsToExclude</code> be consulted to
   * determine which of the tests named in the <code>testNames</code> <code>Set</code> should be run. For more information on trait tags, see the main documentation for this trait.
   * </p>
   *
   * <p>
   * If <code>testName</code> is <code>None</code>, this trait's implementation of this method
   * invokes <code>testNames</code> on this <code>Suite</code> to get a <code>Set</code> of names of tests to potentially execute.
   * (A <code>testNames</code> value of <code>None</code> essentially acts as a wildcard that means all tests in
   * this <code>Suite</code> that are selected by <code>tagsToInclude</code> and <code>tagsToExclude</code> should be executed.)
   * For each test in the <code>testName</code> <code>Set</code>, in the order
   * they appear in the iterator obtained by invoking the <code>elements</code> method on the <code>Set</code>, this trait's implementation
   * of this method checks whether the test should be run based on the <code>tagsToInclude</code> and <code>tagsToExclude</code> <code>Set</code>s.
   * If so, this implementation invokes <code>runTest</code>, passing in:
   * </p>
   *
   * <ul>
   * <li><code>testName</code> - the <code>String</code> name of the test to run (which will be one of the names in the <code>testNames</code> <code>Set</code>)</li>
   * <li><code>reporter</code> - the <code>Reporter</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>stopper</code> - the <code>Stopper</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>configMap</code> - the <code>configMap</code> passed to this method, or one that wraps and delegates to it</li>
   * </ul>
   *
   * @param testName an optional name of one test to execute. If <code>None</code>, all relevant tests should be executed.
   *                 I.e., <code>None</code> acts like a wildcard that means execute all relevant tests in this <code>Spec</code>.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param tagsToInclude a <code>Set</code> of <code>String</code> tag names to include in the execution of this <code>Spec</code>
   * @param tagsToExclude a <code>Set</code> of <code>String</code> tag names to exclude in the execution of this <code>Spec</code>
   * @param configMap a <code>Map</code> of key-value pairs that can be used by this <code>Spec</code>'s executing tests.
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, <code>tagsToInclude</code>,
   *     <code>tagsToExclude</code>, or <code>configMap</code> is <code>null</code>.
   */
  protected override def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

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

    testName match {
      case None => runTestsInBranch(atomic.get.trunk, reporter, stopRequested, filter, configMap, tracker)
      case Some(tn) => runTest(tn, reporter, stopRequested, configMap, tracker)
    }
  }

  /**
   * An immutable <code>Set</code> of test names. If this <code>Spec</code> contains no tests, this method returns an
   * empty <code>Set</code>.
   *
   * <p>
   * This trait's implementation of this method will return a set that contains the names of all registered tests. The set's
   * iterator will return those names in the order in which the tests were registered. Each test's name is composed
   * of the concatenation of the text of each surrounding describer, in order from outside in, and the text of the
   * example itself, with all components separated by a space. For example, consider this Spec:
   * </p>
   *
   * <pre>
   * class StackSpec {
   *   describe("A Stack") {
   *     describe("(when not empty)") {
   *       it("must allow me to pop") {}
   *     }
   *     describe("(when not full)") {
   *       it("must allow me to push") {}
   *     }
   *   }
   * }
   * </pre>
   *
   * <p>
   * Invoking <code>testNames</code> on this <code>Spec</code> will yield a set that contains the following
   * two test name strings:
   * </p>
   *
   * <pre>
   * "A Stack (when not empty) must allow me to pop"
   * "A Stack (when not full) must allow me to push"
   * </pre>
   */
  override def testNames: Set[String] = ListSet(atomic.get.testsList.map(_.testName): _*)

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    val stopRequested = stopper

    // Set the flag that indicates registration is closed (because run has now been invoked),
    // which will disallow any further invocations of "describe", it", or "ignore" with
    // an RegistrationClosedException.
    val oldBundle = atomic.get
    var (trunk, currentBranch, tagsMap, testsList, registrationClosed) = oldBundle.unpack
    if (!registrationClosed)
      updateAtomic(oldBundle, Bundle(trunk, currentBranch, tagsMap, testsList, true))

    val report = wrapReporterIfNecessary(reporter)

    val informerForThisSuite =
      new ConcurrentInformer(NameInfo(thisSuite.suiteName, Some(thisSuite.getClass.getName), None)) {
        def apply(message: String) {
          if (message == null)
            throw new NullPointerException
          report(InfoProvided(tracker.nextOrdinal(), message, nameInfoForCurrentThread))
        }
      }

    atomicInformer.set(informerForThisSuite)
    try {
      super.run(testName, report, stopRequested, filter, configMap, distributor, tracker)
    }
    finally {
      val success = atomicInformer.compareAndSet(informerForThisSuite, zombieInformer)
      val rarelyIfEverSeen = """
        Two threads have apparently attempted to run suite at the same time. This has
        resulted in both threads attempting to concurrently change the current informer.
      """
      if (!success)
        throw new ConcurrentModificationException(rarelyIfEverSeen + "Suite class name: " + thisSuite.getClass.getName)
    }
  }

  val behave = new BehaveWord
}

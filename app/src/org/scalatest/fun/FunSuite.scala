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
import org.scalacheck.Arbitrary
import org.scalacheck.Arb
import org.scalacheck.Prop
import org.scalacheck.Test.Params
import org.scalacheck.Test
import org.scalacheck.Test._

/**
 * Abstract class whose subclasses can be as to <code>FunSuite</code> and <code>FunSuiteN</code>'s test
 * registration methods to place tests into groups. For example, if you define:
 * <pre>
 * object SlowTest extends Group("SlowTest")
 * </pre>
 *
 * then you can place a test into the <code>SlowTest</code> group like this:
 * <pre>
 * class MySuite extends FunSuite {
 *
 *   test("my test", SlowTest) {
 *     Thread.sleep(1000)
 *   }
 * }
 * </pre>
 *
 * If you have created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>FunSuite</code>s that match. To do so, simply 
 * pass the fully qualified names of the Java interfaces to the <code>Group</code> constructor. For example, if you've
 * defined a Java annotation interface with fully qualified name, <code>com.mycompany.groups.SlowTest</code>, then you could
 * create a matching group for <code>FunSuite</code>s like this:
 * <pre>
 * object SlowTest extends Group("com.mycompany.groups.SlowTest")
 * </pre>
 *
 * @author Bill Venners
 */
abstract class Group(val name: String)

/**
 * A suite of tests in which each test is represented as a function value. The &#8220;<code>Fun</code> &#8221;in <code>FunSuite</code> stands for functional.
 * Here's an example <code>FunSuite</code>:
 *
 * <pre>
 * import org.scalatest.fun.FunSuite
 *
 * class MySuite extends FunSuite {
 *
 *   test("addition") {
 *     val sum = 1 + 1
 *     assert(sum === 2)
 *     assert(sum + 2 === 4)
 *   }
 *
 *   test("subtraction") {
 *     val diff = 4 - 1
 *     assert(diff === 3)
 *     assert(diff - 2 === 1)
 *   }
 * }
 * </pre>
 *
 * <p>
 * &#8220;<code>test</code>&#8221; is a method defined in <code>FunSuite</code>, which will be invoked
 * by the primary constructor of <code>MySuite</code>. You specify the name of the test as
 * a string between the parentheses, and the test code itself between curly braces.
 * The test code is a function passed as a by-name parameter to <code>test</code>, which registers
 * it for later execution. One benefit of <code>FunSuite</code> compared to <code>Suite</code> is you need not name all your
 * tests starting with &#8220;<code>test</code>.&#8221; In addition, you can more easily give long names to
 * your tests, because you need not encode them in camel case, as you must do
 * with test methods.
 * </p>
 * 
 * <p>
 * If you prefer, you can alternatively use the word "specify" to register tests:
 * </p>
 *
 * <pre>
 * import org.scalatest.fun.FunSuite
 *
 * class MySuite extends FunSuite {
 *
 *   specify("addition") {
 *     val sum = 1 + 1
 *     assert(sum === 2)
 *     assert(sum + 2 === 4)
 *   }
 *
 *   specify("subtraction") {
 *     val diff = 4 - 1
 *     assert(diff === 3)
 *     assert(diff - 2 === 1)
 *   }
 * }
 * </pre>
 *
 * <p>
 * <strong>Property-based tests</strong>
 * </p>
 * 
 * <p>
 * <code>FunSuite</code> makes it easy to combine assertion-based tests with property-based
 * tests using ScalaCheck. <code>FunSuite</code> mixes in trait <code>Checkers</code>, so you
 * can write property checks alongside assertion-based tests in your test functions:
 * </p>
 * <pre>
 * import org.scalatest.fun.FunSuite
 *
 * class MySuite extends FunSuite {
 *
 *   test("list concatenation") {
 * 
 *     val a = List(1, 2, 3)
 *     val b = List(4, 5, 6)
 *     assert(a ::: b === List(1, 2, 3, 4, 5, 6))

 *     check((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
 *   }
 * }
 * </pre>
 *
 * <p>
 * You can also register properties as tests, using either "test" or "specify". Here's an
 * example that uses "specify":
 * </p>
 * <pre>
 * import org.scalatest.fun.FunSuite
 *
 * class StringSuite extends FunSuite {
 *
 *   specify("startsWith", (a: String, b: String) => (a + b).startsWith(a))
 *
 *   specify("endsWith", (a: String, b: String) => (a + b).endsWith(b))
 *
 *   specify(
 *     "substring should start from passed index and go to end of string",
 *     (a: String, b: String) => (a + b).substring(a.length) == b
 *   )
 *
 *   specify(
 *     "substring should be commutative",
 *       (a: String, b: String, c: String) => (a + b + c).substring(a.length, a.length + b.length) == b
 *   )
 * }
 * </pre>
 * 
 * <p>
 * <strong>Test fixtures</strong>
 * </p>
 * 
 * <p>
 * If you want to write tests that need the same mutable fixture objects, you can
 * extend one of the traits <code>FunSuite1</code> through <code>FunSuite9</code>. If you need three
 * fixture objects, for example, you would extend <code>FunSuite3</code>. Here's an example
 * that extends <code>FunSuite1</code>, to initialize a <code>StringBuilder</code> fixture object for each test:
 * </p>
 * 
 * <pre>
 * import org.scalatest.fun.FunSuite1
 *
 * class EasySuite extends FunSuite1[StringBuilder] {
 *
 *   testWithFixture("easy test") {
 *     sb => {
 *       sb.append("easy!")
 *       assert(sb.toString === "Testing is easy!")
 *     }
 *   }
 *
 *   testWithFixture("fun test") {
 *     sb => {
 *       sb.append("fun!")
 *       assert(sb.toString === "Testing is fun!")
 *     }
 *   }
 *
 *   def withFixture(f: StringBuilder => Unit) {
 *     val sb = new StringBuilder("Testing is ")
 *     f(sb)
 *   }
 * }
 * </pre>
 * 
 * <p>
 * In the class declaration of this example, <code>FunSuite1</code> is parameterized with the type of the
 * lone fixture object, <code>StringBuilder</code>. Two tests are defined with
 * <code>testWithFixture</code>. The function values provided here take the fixture object,
 * a <code>StringBuilder</code>, as a parameter and use it in the test code. Note that
 * the fixture object, referenced by <code>sb</code>, is mutated by both tests with the call to <code>append</code>. Lastly, a <code>withFixture</code>
 * method is provided that takes a test function. This method creates a new <code>StringBuilder</code>,
 * initializes it to <code>"Testing is "</code>, and passes it to the test function.
 * When ScalaTest runs this suite, it will pass each test function to <code>withFixture</code>.
 * The <code>withFixture</code> method will create and initialize a new <code>StringBuilder</code> object and
 * pass that to the test function. In this way, each test function will get a fresh copy
 * of the fixture. For more information on using <code>FunSuite</code>s with fixtures, see the documentation
 * for <code>FunSuite1</code> through <code>FunSuite9</code>.
 * </p>
 *
 * <p>
 * <strong>Test groups</strong>
 * </p>
 *
 * <p>
 * A <code>FunSuite</code>'s tests may be classified into named <em>groups</em>.
 * As with any suite, when executing a <code>FunSuite</code>, groups of tests can
 * optionally be included and/or excluded. To place <code>FunSuite</code> tests into
 * groups, you pass objects that extend abstract class <code>org.scalatest.fun.Group</code> to methods
 * that register tests. Class <code>Group</code> takes one type parameter, a string name.  If you have
 * created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>FunSuite</code>s that match. To do so, simply 
 * pass the fully qualified names of the Java interfaces to the <code>Group</code> constructor. For example, if you've
 * defined Java annotation interfaces with fully qualified names, <code>com.mycompany.groups.SlowTest</code> and <code>com.mycompany.groups.DBTest</code>, then you could
 * create matching groups for <code>FunSuite</code>s like this:
 * </p>
 * <pre>
 * object SlowTest extends Group("com.mycompany.groups.SlowTest")
 * object DBTest extends Group("com.mycompany.groups.DBTest")
 * </pre>
 * <p>
 * Given these definitions, you could place <code>FunSuite</code> tests into groups like this:
 * </p>
 * <pre>
 * import org.scalatest.fun.FunSuite
 *
 * class MySuite extends FunSuite {
 *
 *   test("addition", SlowTest) {
 *     val sum = 1 + 1
 *     assert(sum === 2)
 *     assert(sum + 2 === 4)
 *   }
 *
 *   test("subtraction", SlowTest, DBTest) {
 *     val diff = 4 - 1
 *     assert(diff === 3)
 *     assert(diff - 2 === 1)
 *   }
 * }
 * </pre>
 *
 * <p>
 * This code places both tests, <code>addition</code> and <code>subtraction</code>, into the <code>com.mycompany.groups.SlowTest</code> group, 
 * and test <code>subtraction</code> into the <code>com.mycompany.groups.DBTest</code> group.
 * </p>
 *
 * <p>
 * The primary execute method takes two <code>Set[String]</code>s called <code>includes</code> and
 * <code>excludes</code>. If <code>includes</code> is empty, all tests will be executed
 * except those those belonging to groups listed in the
 * <code>excludes</code> <code>Set</code>. If <code>includes</code> is non-empty, only tests
 * belonging to groups mentioned in <code>includes</code>, and not mentioned in <code>excludes</code>,
 * will be executed.
 * </p>
 *
 * <p>
 * <strong>Ignored tests</strong>
 * </p>
 *
 * <p>
 * To support the common use case of &#8220;temporarily&#8221; disabling tests, with the
 * good intention of resurrecting the test at a later time, <code>FunSuite</code> provides registration
 * methods that start with <code>ignore</code> instead of <code>test</code>. For example, to temporarily
 * disable the test named <code>addition</code>, just change &#8220;<code>test</code>&#8221; into &#8220;<code>ignore</code>,&#8221; like this:
 * </p>
 *
 * <pre>
 * import org.scalatest.fun.FunSuite
 *
 * class MySuite extends FunSuite {
 *
 *   ignore("addition") {
 *     val sum = 1 + 1
 *     assert(sum === 2)
 *     assert(sum + 2 === 4)
 *   }
 *
 *   test("subtraction") {
 *     val diff = 4 - 1
 *     assert(diff === 3)
 *     assert(diff - 2 === 1)
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you run this version of <code>MySuite</code> with:
 * </p>
 *
 * <pre>
 * scala> (new MySuite).execute()
 * </pre>
 *
 * <p>
 * It will run only <code>subtraction</code> and report that <code>addition</code> was ignored.
 * </p>
 *
 * <p>
 * As with <code>org.scalatest.Suite</code>, the ignore feature is implemented as a group. The <code>execute</code> method that takes no parameters
 * adds <code>org.scalatest.Ignore</code> to the <code>excludes</code> <code>Set</code> it passes to
 * the primary <code>execute</code> method, as does <code>Runner</code>. The only difference between
 * <code>org.scalatest.Ignore</code> and the groups you may define and exclude is that ScalaTest reports
 * ignored tests to the <code>Reporter</code>. The reason ScalaTest reports ignored tests is as a feeble
 * attempt to encourage ignored tests to be eventually fixed and added back into the active suite of tests.
 * </p>
 *
 * <p>
 * <strong>Reporters</strong>
 * </p>
 *
 * <p>
 * One of the parameters to the primary <code>execute</code> method is a <code>Reporter</code>, which
 * will collect and report information about the running suite of tests.
 * Information about suites and tests that were run, whether tests succeeded or failed, 
 * and tests that were ignored will be passed to the <code>Reporter</code> as the suite runs.
 * Most often the reporting done by default by <code>FunSuite</code>'s methods will be sufficient, but
 * occasionally you may wish to provide custom information to the <code>Reporter</code> from a test.
 * For this purpose, you can optionally register a test with a function value that takes a <code>Reporter</code> parameter via the &#8220;<code>...WithReporter</code>
 * variants of the test registration methods. You can then
 * pass extra information to the <code>Reporter</code>'s <code>infoProvided</code> method in the body of the test functions.
 * Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest.fun.FunSuite
 * import org.scalatest.Report
 *
 * class MySuite extends FunSuite {
 *
 *   testWithReporter("addition") {
 *     reporter => {
 *       val sum = 1 + 1
 *       assert(sum === 2)
 *       assert(sum + 2 === 4)
 *       val report = new Report("MySuite.addition", "Addition seems to work.")
 *       reporter.infoProvided(report)
 *     }
 *   }
 * }
 * </pre>
 *
 * If you run this <code>Suite</code> from the interpreter, you will see the following message
 * included in the printed report:
 *
 * <pre>
 * Info Provided: MySuite.addition: Addition seems to work.
 * </pre>
 *
 * <p>
 * This trait depends on ScalaCheck, so you must include ScalaCheck's
 * jar file on either the classpath or runpath.
 * </p>
 *
 * @author Bill Venners
 */
trait FunSuite extends Suite with Checkers {

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
   * Register a test with the specified name, optional groups, and function value that takes no arguments (a convenience
   * method for those who prefer "specify" to "test").
   * This method will register the test for later execution via an invocation of one of the <code>execute</code>
   * methods. The passed test name must not have been registered previously on
   * this <code>FunSuite</code> instance.
   *
   * @throws IllegalArgumentException if <code>testName</code> had been registered previously
   */
  protected def specify(testName: String, testGroups: Group*)(f: => Unit) {
    test(testName, testGroups: _*)(f)
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
   * Register a test with the specified name, optional groups, and function value that takes a <code>Reporter</code> (a
   * convenience method for those who prefer "specify" to "test").
   * This method will register the test for later execution via an invocation of one of the <code>execute</code>
   * methods. The <code>Reporter</code> passed to <code>execute</code>, or a <code>Reporter</code> that wraps it, will be passed to the function value.
   * The passed test name must not have been registered previously on
   * this <code>FunSuite</code> instance.
   *
   * @throws IllegalArgumentException if <code>testName</code> had been registered previously
   */
  protected def specifyWithReporter(testName: String, testGroups: Group*)(f: (Reporter) => Unit) {
    testWithReporter(testName, testGroups: _*)(f)
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

    val wrappedReporter = reporter

    val report = new Report(getTestNameForReport(testName), "")

    wrappedReporter.testStarting(report)

    try {

      atomic.get.testsMap(testName) match {
        case PlainOldTest(testName, f) => f()
        case ReporterTest(testName, f) => f(reporter)
      }

      val report = new Report(getTestNameForReport(testName), "")

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

  /**
   * Convert the passed 1-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test[A1,P](testName: String, f: A1 => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1]
    ) {
    test(testName, Prop.property(f)(p, a1))
  }

  /**
   * Convert the passed 1-arg function into a property, and register it as a test (a convenience method for those who prefer "specify" to "test").
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def specify[A1,P](testName: String, f: A1 => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1]
    ) {
    test(testName, f, testGroups: _*)
  }

  /**
   * Convert the passed 2-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test[A1,A2,P](testName: String, f: (A1,A2) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2]
    ) {
    test(testName, Prop.property(f)(p, a1, a2))
  }

  /**
   * Convert the passed 2-arg function into a property, and register it as a test (a convenience method for those who prefer "specify" to "test").
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def specify[A1,A2,P](testName: String, f: (A1,A2) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2]
    ) {
    test(testName, f, testGroups: _*)
  }

  /**
   * Convert the passed 3-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test[A1,A2,A3,P](testName: String, f: (A1,A2,A3) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2],
      a3: Arb[A3] => Arbitrary[A3]
    ) {
    test(testName, Prop.property(f)(p, a1, a2, a3))
  }

  /**
   * Convert the passed 3-arg function into a property, and register it as a test (a convenience method for those who prefer "specify" to "test").
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def specify[A1,A2,A3,P](testName: String, f: (A1,A2,A3) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2],
      a3: Arb[A3] => Arbitrary[A3]
    ) {
    test(testName, f, testGroups: _*)
  }

  /**
   * Convert the passed 4-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test[A1,A2,A3,A4,P](testName: String, f: (A1,A2,A3,A4) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2],
      a3: Arb[A3] => Arbitrary[A3],
      a4: Arb[A4] => Arbitrary[A4]
    ) {
    test(testName, Prop.property(f)(p, a1, a2, a3, a4))
  }

  /**
   * Convert the passed 4-arg function into a property, and register it as a test (a convenience method for those who prefer "specify" to "test").
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def specify[A1,A2,A3,A4,P](testName: String, f: (A1,A2,A3,A4) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2],
      a3: Arb[A3] => Arbitrary[A3],
      a4: Arb[A4] => Arbitrary[A4]
    ) {
    test(testName, f, testGroups: _*)
  }

  /**
   * Convert the passed 5-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test[A1,A2,A3,A4,A5,P](testName: String, f: (A1,A2,A3,A4,A5) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2],
      a3: Arb[A3] => Arbitrary[A3],
      a4: Arb[A4] => Arbitrary[A4],
      a5: Arb[A5] => Arbitrary[A5]
    ) {
    test(testName, Prop.property(f)(p, a1, a2, a3, a4, a5))
  }

  /**
   * Convert the passed 5-arg function into a property, and register it as a test (a convenience method for those who prefer "specify" to "test").
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def specify[A1,A2,A3,A4,A5,P](testName: String, f: (A1,A2,A3,A4,A5) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2],
      a3: Arb[A3] => Arbitrary[A3],
      a4: Arb[A4] => Arbitrary[A4],
      a5: Arb[A5] => Arbitrary[A5]
    ) {
    test(testName, f, testGroups: _*)
  }

  /**
   * Convert the passed 6-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test[A1,A2,A3,A4,A5,A6,P](testName: String, f: (A1,A2,A3,A4,A5,A6) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2],
      a3: Arb[A3] => Arbitrary[A3],
      a4: Arb[A4] => Arbitrary[A4],
      a5: Arb[A5] => Arbitrary[A5],
      a6: Arb[A6] => Arbitrary[A6]
    ) {
    test(testName, Prop.property(f)(p, a1, a2, a3, a4, a5, a6))
  }

  /**
   * Convert the passed 6-arg function into a property, and register it as a test (a convenience method for those who prefer "specify" to "test").
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def specify[A1,A2,A3,A4,A5,A6,P](testName: String, f: (A1,A2,A3,A4,A5,A6) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2],
      a3: Arb[A3] => Arbitrary[A3],
      a4: Arb[A4] => Arbitrary[A4],
      a5: Arb[A5] => Arbitrary[A5],
      a6: Arb[A6] => Arbitrary[A6]
    ) {
    test(testName, f, testGroups: _*)
  }

  /**
   * Register as a test a property with the given testing parameters.
   *
   * @param p the property to check
   * @param prms the test parameters
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test(testName: String, p: Prop, prms: Params, testGroups: Group*) {
    test(testName, testGroups: _*) {
      check(p, prms)
    }
  }

  /**
   * Register as a test a property with the given testing parameters (a convenience method for those who prefer "specify" to "test").
   *
   * @param p the property to check
   * @param prms the test parameters
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def specify(testName: String, p: Prop, prms: Params, testGroups: Group*) {
    test(testName, p, prms, testGroups: _*)
  }

  /**
   * Register a property as a test.
   *
   * @param p the property to check
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test(testName: String, p: Prop, testGroups: Group*) {
    test(testName, p, Test.defaultParams)
  }

  /**
   * Register a property as a test (a convenience method for those who prefer "specify" to "test").
   *
   * @param p the property to check
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def specify(testName: String, p: Prop, testGroups: Group*) {
    test(testName, p, testGroups: _*)
  }
}

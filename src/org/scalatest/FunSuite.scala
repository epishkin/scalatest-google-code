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
 * A suite of tests in which each test is represented as a function value. The &#8220;<code>Fun</code>&#8221; in <code>FunSuite</code> stands for functional.
 * Here's an example <code>FunSuite</code>:
 *
 * <pre>
 * import org.scalatest.FunSuite
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
 * &#8220;<code>test</code>&#8221; is a method, defined in <code>FunSuite</code>, which will be invoked
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
 * <strong>Test fixtures</strong>
 * </p>
 *
 * <p>
 * A test <em>fixture</em> is objects or other artifacts (such as files, sockets, database
 * connections, etc.) used by tests to do their work. You can use fixtures in
 * <code>FunSuite</code>s with the same approaches suggested for <code>Suite</code> in
 * its documentation. The same text that appears in the test fixture
 * section of <code>Suite</code>'s documentation is repeated here, with examples changed from
 * <code>Suite</code> to <code>FunSuite</code>.
 * </p>
 *
 * <p>
 * If a fixture is used by only one test, then the definitions of the fixture objects should
 * be local to the test function, such as the objects assigned to <code>sum</code> and <code>diff</code> in the
 * previous <code>MySuite</code> examples. If multiple tests need to share a fixture, the best approach
 * is to assign them to instance variables. Here's a (very contrived) example, in which the object assigned
 * to <code>shared</code> is used by multiple test functions:
 * </p>
 *
 * <pre>
 * import org.scalatest.FunSuite
 *
 * class MySuite extends FunSuite {
 *
 *   // Sharing fixture objects via instance variables
 *   val shared = 5
 *
 *   test("Addition") {
 *     val sum = 2 + 3
 *     assert(sum === shared)
 *   }
 *
 *   test("Subtraction") {
 *     val diff = 7 - 2
 *     assert(diff === shared)
 *   }
 * }
 * </pre>
 *
 * <p>
 * In some cases, however, shared <em>mutable</em> fixture objects may be changed by test methods such that
 * it needs to be recreated or reinitialized before each test. Shared resources such
 * as files or database connections may also need to 
 * be cleaned up after each test. JUnit offers methods <code>setup</code> and
 * <code>tearDown</code> for this purpose. In ScalaTest, you can use the <code>BeforeAndAfter</code> trait,
 * which will be described later, to implement an approach similar to JUnit's <code>setup</code>
 * and <code>tearDown</code>, however, this approach often involves reassigning <code>var</code>s
 * between tests. Before going that route, you should consider two approaches that
 * avoid <code>var</code>s. One approach is to write one or more "create" methods
 * that return a new instance of a needed object (or a tuple of new instances of
 * multiple objects) each time it is called. You can then call a create method at the beginning of each
 * test that needs the fixture, storing the fixture object or objects in local variables. Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest.FunSuite
 * import scala.collection.mutable.ListBuffer
 *
 * class MySuite extends FunSuite {
 *
 *   // create objects needed by tests and return as a tuple
 *   def createFixture = (
 *     new StringBuilder("ScalaTest is "),
 *     new ListBuffer[String]
 *   )
 *
 *   test("Easy") {
 *     val (builder, lbuf) = createFixture
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(lbuf.isEmpty)
 *     lbuf += "sweet"
 *   }
 *
 *   test("Fun") {
 *     val (builder, lbuf) = createFixture
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(lbuf.isEmpty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * Another approach to mutable fixture objects that avoids <code>var</code>s is to create "with" methods,
 * which take test code as a function that takes the fixture objects as parameters, and wrap test code in calls to the "with" method. Here's an example:
 * </p>
 * <pre>
 * import org.scalatest.FunSuite
 * import scala.collection.mutable.ListBuffer
 *
 * class MySuite extends FunSuite {
 *
 *   def withFixture(testFunction: (StringBuilder, ListBuffer[String]) => Unit) {
 *
 *     // Create needed mutable objects
 *     val sb = new StringBuilder("ScalaTest is ")
 *     val lb = new ListBuffer[String]
 *
 *     // Invoke the test function, passing in the mutable objects
 *     testFunction(sb, lb)
 *   }
 *
 *   test("Easy") {
 *     withFixture {
 *       (builder, lbuf) => {
 *         builder.append("easy!")
 *         assert(builder.toString === "ScalaTest is easy!")
 *         assert(lbuf.isEmpty)
 *         lbuf += "sweet"
 *       }
 *     }
 *   }
 *
 *   test("Fun") {
 *     withFixture {
 *       (builder, lbuf) => {
 *         builder.append("fun!")
 *         assert(builder.toString === "ScalaTest is fun!")
 *         assert(lbuf.isEmpty)
 *       }
 *     }
 *   }
 * }
 * </pre>
 * 
 * One advantage of this approach compared to the create method approach shown previously is that
 * you can more easily perform cleanup after each test executes. For example, you
 * could create a temporary file before each test, and delete it afterwords, by
 * doing so before and after invoking the test function in a <code>withTempFile</code>
 * method. Here's an example:
 *
 * <pre>
 * import org.scalatest.FunSuite
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MySuite extends FunSuite {
 * 
 *   def withTempFile(testFunction: FileReader => Unit) {
 * 
 *     val FileName = "TempFile.txt"
 *  
 *     // Set up the temp file needed by the test
 *     val writer = new FileWriter(FileName)
 *     try {
 *       writer.write("Hello, test!")
 *     }
 *     finally {
 *       writer.close()
 *     }
 *  
 *     // Create the reader needed by the test
 *     val reader = new FileReader(FileName)
 *  
 *     try {
 *       // Run the test using the temp file
 *       testFunction(reader)
 *     }
 *     finally {
 *       // Close and delete the temp file
 *       reader.close()
 *       val file = new File(FileName)
 *       file.delete()
 *     }
 *   }
 * 
 *   test("Reading from the temp file") {
 *     withTempFile {
 *       (reader) => {
 *         var builder = new StringBuilder
 *         var c = reader.read()
 *         while (c != -1) {
 *           builder.append(c.toChar)
 *           c = reader.read()
 *         }
 *         assert(builder.toString === "Hello, test!")
 *       }
 *     }
 *   }
 * 
 *   test("First char of the temp file") {
 *     withTempFile {
 *       (reader) => {
 *         assert(reader.read() === 'H')
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you are more comfortable with reassigning instance variables, however, you can
 * instead use the <code>BeforeAndafter</code> trait, which provides
 * methods that will be run before and after each test. <code>BeforeAndAfter</code>'s
 * <code>beforeEach</code> method will be run before, and its <code>afterEach</code>
 * method after, each test (like JUnit's <code>setup</code>  and <code>tearDown</code>
 * methods, respectively). For example, here's how you'd write the previous
 * test that uses a temp file with <code>BeforeAndAfter</code>:
 * </p>
 *
 * <pre>
 * import org.scalatest.FunSuite
 * import org.scalatest.BeforeAndAfter
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 *
 * class MySuite extends FunSuite with BeforeAndAfter {
 *
 *   private val FileName = "TempFile.txt"
 *   private var reader: FileReader = _
 *
 *   // Set up the temp file needed by the test
 *   override def beforeEach() {
 *     val writer = new FileWriter(FileName)
 *     try {
 *       writer.write("Hello, test!")
 *     }
 *     finally {
 *       writer.close()
 *     }
 *
 *     // Create the reader needed by the test
 *     reader = new FileReader(FileName)
 *   }
 *
 *   // Close and delete the temp file
 *   override def afterEach() {
 *     reader.close()
 *     val file = new File(FileName)
 *     file.delete()
 *   }
 *
 *   test("Reading from the temp file") {
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     assert(builder.toString === "Hello, test!")
 *   }
 *
 *   test("First char of the temp file") {
 *     assert(reader.read() === 'H')
 *   }
 * }
 * </pre>
 *
 * <p>
 * In this example, the instance variable <code>reader</code> is a <code>var</code>, so
 * it can be reinitialized between tests by the <code>beforeEach</code> method. If you
 * want to execute code before and after all tests (and nested suites) in a suite, such
 * as you could do with <code>@BeforeClass</code> and <code>@AfterClass</code>
 * annotations in JUnit 4, you can use the <code>beforeAll</code> and <code>afterAll</code>
 * methods of <code>BeforeAndAfter</code>. See the documentation for <code>BeforeAndAfter</code> for
 * an example.
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
 * groups, you pass objects that extend abstract class <code>org.scalatest.Group</code> to methods
 * that register tests. Class <code>Group</code> takes one parameter, a string name.  If you have
 * created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>FunSuite</code>s that match. To do so, simply 
 * pass the fully qualified names of the Java interfaces to the <code>Group</code> constructor. For example, if you've
 * defined Java annotation interfaces with fully qualified names, <code>com.mycompany.groups.SlowTest</code> and <code>com.mycompany.groups.DBTest</code>, then you could
 * create matching groups for <code>FunSuite</code>s like this:
 * </p>
 * <pre>
 * import org.scalatest.Group
 *
 * object SlowTest extends Group("com.mycompany.groups.SlowTest")
 * object DBTest extends Group("com.mycompany.groups.DBTest")
 * </pre>
 * <p>
 * Given these definitions, you could place <code>FunSuite</code> tests into groups like this:
 * </p>
 * <pre>
 * import org.scalatest.FunSuite
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
 * This code places both tests, "addition" and "subtraction," into the <code>com.mycompany.groups.SlowTest</code> group, 
 * and test "subtraction" into the <code>com.mycompany.groups.DBTest</code> group.
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
 * To support the common use case of &#8220;temporarily&#8221; disabling a test, with the
 * good intention of resurrecting the test at a later time, <code>FunSuite</code> provides registration
 * methods that start with <code>ignore</code> instead of <code>test</code>. For example, to temporarily
 * disable the test named <code>addition</code>, just change &#8220;<code>test</code>&#8221; into &#8220;<code>ignore</code>,&#8221; like this:
 * </p>
 *
 * <pre>
 * import org.scalatest.FunSuite
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
 * It will run only <code>subtraction</code> and report that <code>addition</code> was ignored:
 * </p>
 *
 * <pre>
 * Test Ignored - MySuite: addition
 * Test Starting - MySuite: subtraction
 * Test Succeeded - MySuite: subtraction
 * </pre>
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
 * <strong>Informers</strong>
 * </p>
 *
 * <p>
 * One of the parameters to the primary <code>execute</code> method is a <code>Reporter</code>, which
 * will collect and report information about the running suite of tests.
 * Information about suites and tests that were run, whether tests succeeded or failed, 
 * and tests that were ignored will be passed to the <code>Reporter</code> as the suite runs.
 * Most often the reporting done by default by <code>FunSuite</code>'s methods will be sufficient, but
 * occasionally you may wish to provide custom information to the <code>Reporter</code> from a test.
 * For this purpose, an <code>Informer</code> that will forward information to the current <code>Reporter</code>
 * is provided via the <code>info</code> parameterless method.
 * You can pass the extra information to the <code>Informer</code> via one of its <code>apply</code> methods.
 * The <code>Informer</code> will then pass the information to the <code>Reporter</code>'s <code>infoProvided</code> method.
 * Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest.FunSuite
 *
 * class MySuite extends FunSuite {
 *
 *   test("addition") {
 *     val sum = 1 + 1
 *     assert(sum === 2)
 *     assert(sum + 2 === 4)
 *     info("Addition seems to work")
 *   }
 * }
 * </pre>
 *
 * If you run this <code>Suite</code> from the interpreter, you will see the following message
 * included in the printed report:
 *
 * <pre>
 * Test Starting - MySuite: addition
 * Info Provided - MySuite.addition: Addition seems to work
 * Test Succeeded - MySuite: addition
 * </pre>
 *
 * @author Bill Venners
 */
trait FunSuite extends Suite {

  private val IgnoreGroupName = "org.scalatest.Ignore"

  private abstract class FunNode
  private case class Test(testName: String, testFunction: () => Unit) extends FunNode
  private case class Info(report: Report) extends FunNode

  // Access to the testNamesList, testsMap, and groupsMap must be synchronized, because the test methods are invoked by
  // the primary constructor, but testNames, groups, and runTest get invoked directly or indirectly
  // by execute. When running tests concurrently with ScalaTest Runner, different threads can
  // instantiate and execute the Suite. Instead of synchronizing, I put them in an immutable Bundle object (and
  // all three collections--testNamesList, testsMap, and groupsMap--are immuable collections), then I put the Bundle
  // in an AtomicReference. Since the expected use case is the test method will be called
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
        apply(new Report(nameForReport, message))
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
  protected def test(testName: String, testGroups: Group*)(f: => Unit) {

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

    test(testName)(f) // Call test without passing the groups

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

    val report = new Report(getTestNameForReport(testName), "")

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
              val report = new Report(nameForReport, message)
              wrappedReporter.infoProvided(report)
            }
          }
        theTest.testFunction()
      }
      finally {
        currentInformer = oldInformer
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
   * methods <code>test</code> and <code>ignore</code>. 
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

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

import scala.collection.immutable.ListSet
import java.util.ConcurrentModificationException
import java.util.concurrent.atomic.AtomicReference
import org.scalatest.StackDepthExceptionHelper.getStackDepth
import org.scalatest.events._

/**
 * A suite of tests in which each test is represented as a function value. The &#8220;<code>Fun</code>&#8221; in <code>FunSuite</code> stands
 * for &#8220;function.&#8221; Here's an example <code>FunSuite</code>:
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
 * A <code>FunSuite</code>'s lifecycle has two phases: the <em>registration</em> phase and the
 * <em>ready</em> phase. It starts in registration phase and enters ready phase the first time
 * <code>run</code> is called on it. It then remains in ready phase for the remainder of its lifetime.
 * </p>
 *
 * <p>
 * Tests can only be registered with the <code>test</code> method while the <code>FunSuite</code> is
 * in its registration phase. Any attempt to register a test after the <code>FunSuite</code> has
 * entered its ready phase, <em>i.e.</em>, after <code>run</code> has been invoked on the <code>FunSuite</code>,
 * will be met with a thrown <code>TestRegistrationClosedException</code>. The recommended style
 * of using <code>FunSuite</code> is to register tests during object construction as is done in all
 * the examples shown here. If you keep to the recommended style, you should never see a
 * <code>TestRegistrationClosedException</code>.
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
 *   test("addition") {
 *     val sum = 2 + 3
 *     assert(sum === shared)
 *   }
 *
 *   test("subtraction") {
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
 * be cleaned up after each test. JUnit offers methods <code>setUp</code> and
 * <code>tearDown</code> for this purpose. In ScalaTest, you can use the <code>BeforeAndAfterEach</code> trait,
 * which will be described later, to implement an approach similar to JUnit's <code>setUp</code>
 * and <code>tearDown</code>, however, this approach often involves reassigning <code>var</code>s
 * between tests. Before going that route, you should consider two approaches that
 * avoid <code>var</code>s. One approach is to write one or more <em>create-fixture</em> methods
 * that return a new instance of a needed object (or a tuple or case class holding new instances of
 * multiple objects) each time it is called. You can then call a create-fixture method at the beginning of each
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
 *   test("easy") {
 *     val (builder, lbuf) = createFixture
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(lbuf.isEmpty)
 *     lbuf += "sweet"
 *   }
 *
 *   test("fun") {
 *     val (builder, lbuf) = createFixture
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(lbuf.isEmpty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * If different tests in the same <code>FunSuite</code> require different fixtures, you can create multiple create-fixture methods and
 * call the method (or methods) needed by each test at the begining of the test.
 * </p>
 *
 * <p>
 * Another approach to mutable fixture objects that avoids <code>var</code>s is to create with-fixture methods,
 * which take test code as a function that takes the fixture objects as parameters, and wrap test code in calls to the
 * with-fixture method. Here's an example:
 * </p>
 *
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
 *   test("easy") {
 *     withFixture { (builder, lbuf) =>
 *       builder.append("easy!")
 *       assert(builder.toString === "ScalaTest is easy!")
 *       assert(lbuf.isEmpty)
 *       lbuf += "sweet"
 *     }
 *   }
 *
 *   test("fun") {
 *     withFixture { (builder, lbuf) =>
 *       builder.append("fun!")
 *       assert(builder.toString === "ScalaTest is fun!")
 *       assert(lbuf.isEmpty)
 *     }
 *   }
 * }
 * </pre>
 *
 * One advantage of this approach compared to the create-fixture approach shown previously is that
 * you can more easily perform cleanup after each test runs. For example, you
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
 *   test("reading from the temp file") {
 *     withTempFile { (reader) =>
 *       var builder = new StringBuilder
 *       var c = reader.read()
 *       while (c != -1) {
 *         builder.append(c.toChar)
 *         c = reader.read()
 *       }
 *       assert(builder.toString === "Hello, test!")
 *     }
 *   }
 *
 *   test("first char of the temp file") {
 *     withTempFile { (reader) =>
 *       assert(reader.read() === 'H')
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If different tests in the same <code>FunSuite</code> require different fixtures, you can create multiple with-fixture methods and
 * call the method (or methods) needed by each test at the beginning of the test. A common case, however, will be that all
 * the tests in a suite need to share the same fixture. To facilitate the with-fixture approach in this common case of a single, shared fixture,
 * ScalaTest provides sister traits in the <code>org.scalatest.fixture</code> package that
 * directly support the with-fixture approach. Every test in an <code>org.scalatest.fixture</code> trait takes a fixture whose type
 * is defined by the <code>Fixture</code> type. For example, trait <code>org.scalatest.fixture.FunSuite</code> behaves exactly like
 * <code>org.scalatest.FunSuite</code>, except each test method takes a <code>Fixture</code>. For the details, see the documentation for
 * <a href="fixture/FunSuite.html"><code>FunSuite</code></a>. To get the idea, however, here's what the previous example would
 * look like rewritten to use an <code>org.scalatest.fixture.FunSuite</code>:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FunSuite
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 *
 * class MySuite extends FunSuite with SimpleWithFixture {
 *
 *   type Fixture = FileReader
 *
 *   def withFixture(testFunction: FileReader => Unit) {
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
 *   test("reading from the temp file") { reader =>
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     assert(builder.toString === "Hello, test!")
 *   }
 *
 *   test("first char of the temp file") { reader =>
 *     assert(reader.read() === 'H')
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you are more comfortable with reassigning instance variables, however, you can
 * instead use the <code>BeforeAndafter</code> trait, which provides
 * methods that will be run before and after each test. <code>BeforeAndAfterEach</code>'s
 * <code>beforeEach</code> method will be run before, and its <code>afterEach</code>
 * method after, each test (like JUnit's <code>setUp</code>  and <code>tearDown</code>
 * methods, respectively). For example, here's how you'd write the previous
 * test that uses a temp file with <code>BeforeAndAfterEach</code>:
 * </p>
 *
 * <pre>
 * import org.scalatest.FunSuite
 * import org.scalatest.BeforeAndAfterEach
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 *
 * class MySuite extends FunSuite with BeforeAndAfterEach {
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
 *   test("reading from the temp file") {
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     assert(builder.toString === "Hello, test!")
 *   }
 *
 *   test("first char of the temp file") {
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
 * methods of <code>BeforeAndAfterAll</code>. See the documentation for <code>BeforeAndAfterAll</code> for
 * an example.
 * </p>
 *
 * <p>
 * <strong>Tagging tests</strong>
 * </p>
 *
 * <p>
 * A <code>FunSuite</code>'s tests may be classified into groups by <em>tagging</em> them with string names.
 * As with any suite, when executing a <code>FunSuite</code>, groups of tests can
 * optionally be included and/or excluded. To tag a <code>FunSuite</code>'s tests,
 * you pass objects that extend abstract class <code>org.scalatest.Tag</code> to methods
 * that register tests, <code>test</code> and <code>ignore</code>. Class <code>Tag</code> takes one parameter, a string name.  If you have
 * created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>FunSuite</code>s that match. To do so, simply
 * pass the fully qualified names of the Java interfaces to the <code>Tag</code> constructor. For example, if you've
 * defined Java annotation interfaces with fully qualified names, <code>com.mycompany.groups.SlowTest</code> and
 * <code>com.mycompany.groups.DBTest</code>, then you could
 * create matching groups for <code>FunSuite</code>s like this:
 * </p>
 *
 * <pre>
 * import org.scalatest.Tag
 *
 * object SlowTest extends Tag("com.mycompany.groups.SlowTest")
 * object DBTest extends Tag("com.mycompany.groups.DBTest")
 * </pre>
 *
 * <p>
 * Given these definitions, you could place <code>FunSuite</code> tests into groups like this:
 * </p>
 *
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
 * This code marks both tests, "addition" and "subtraction," with the <code>com.mycompany.groups.SlowTest</code> tag,
 * and test "subtraction" with the <code>com.mycompany.groups.DBTest</code> tag.
 * </p>
 *
 * <p>
 * The primary <code>run</code> method takes a <code>Filter</code>, whose constructor takes an optional
 * <code>Set[String]</code>s called <code>tagsToInclude</code> and a <code>Set[String]</code> called
 * <code>tagsToExclude</code>. If <code>tagsToInclude</code> is <code>None</code>, all tests will be run
 * except those those belonging to tags listed in the
 * <code>tagsToExclude</code> <code>Set</code>. If <code>tagsToInclude</code> is defined, only tests
 * belonging to tags mentioned in the <code>tagsToInclude</code> set, and not mentioned in <code>tagsToExclude</code>,
 * will be run.
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
 * scala> (new MySuite).run()
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
 * As with <code>org.scalatest.Suite</code>, the ignore feature is implemented as a group. The <code>run</code> method that takes no parameters
 * adds <code>org.scalatest.Ignore</code> to the <code>groupsToExclude</code> <code>Set</code> it passes to
 * the primary <code>run</code> method, as does <code>Runner</code>. The only difference between
 * <code>org.scalatest.Ignore</code> and the groups you may define and exclude is that ScalaTest reports
 * ignored tests to the <code>Reporter</code>. The reason ScalaTest reports ignored tests is as a feeble
 * attempt to encourage ignored tests to be eventually fixed and added back into the active suite of tests.
 * </p>
 *
 * <p>
 * <strong>Pending tests</strong>
 * </p>
 *
 * <p>
 * A <em>pending test</em> is one that has been given a name but is not yet implemented. The purpose of
 * pending tests is to facilitate a style of testing in which documentation of behavior is sketched
 * out before tests are written to verify that behavior (and often, the before the behavior of
 * the system being tested is itself implemented). Such sketches form a kind of specification of
 * what tests and functionality to implement later.
 * </p>
 *
 * <p>
 * To support this style of testing, a test can be given a name that specifies one
 * bit of behavior required by the system being tested. The test can also include some code that
 * sends more information about the behavior to the reporter when the tests run. At the end of the test,
 * it can call method <code>pending</code>, which will cause it to complete abruptly with <code>TestPendingException</code>.
 * Because tests in ScalaTest can be designated as pending with <code>TestPendingException</code>, both the test name and any information
 * sent to the reporter when running the test can appear in the report of a test run. (In other words,
 * the code of a pending test is executed just like any other test.) However, because the test completes abruptly
 * with <code>TestPendingException</code>, the test will be reported as pending, to indicate
 * the actual test, and possibly the functionality, has not yet been implemented.
 * </p>
 *
 * <p>
 * Although pending tests may be used more often in specification-style suites, such as
 * <code>org.scalatest.Spec</code>, you can also use it in <code>FunSuite</code>, like this:
 * </p>
 *
 * <pre>
 * import org.scalatest.FunSuite
 *
 * class MySuite extends FunSuite {
 *
 *   def test("addition") {
 *     val sum = 1 + 1
 *     assert(sum === 2)
 *     assert(sum + 2 === 4)
 *   }
 *
 *   def test("subtraction") (pending)
 * }
 * </pre>
 *
 * <p>
 * (Note: "<code>(pending)</code>" is the body of the test. Thus the test contains just one statement, an invocation
 * of the <code>pending</code> method, which throws <code>TestPendingException</code>.)
 * If you run this version of <code>MySuite</code> with:
 * </p>
 *
 * <pre>
 * scala> (new MySuite).run()
 * </pre>
 *
 * <p>
 * It will run both tests, but report that <code>subtraction</code> is pending. You'll see:
 * </p>
 *
 * <pre>
 * Test Starting - MySuite: addition
 * Test Succeeded - MySuite: addition
 * Test Starting - MySuite: subtraction
 * Test Pending - MySuite: subtraction
 * </pre>
 *
 * <p>
 * <strong>Informers</strong>
 * </p>
 *
 * <p>
 * One of the parameters to the primary <code>run</code> method is a <code>Reporter</code>, which
 * will collect and report information about the running suite of tests.
 * Information about suites and tests that were run, whether tests succeeded or failed,
 * and tests that were ignored will be passed to the <code>Reporter</code> as the suite runs.
 * Most often the reporting done by default by <code>FunSuite</code>'s methods will be sufficient, but
 * occasionally you may wish to provide custom information to the <code>Reporter</code> from a test.
 * For this purpose, an <code>Informer</code> that will forward information to the current <code>Reporter</code>
 * is provided via the <code>info</code> parameterless method.
 * You can pass the extra information to the <code>Informer</code> via one of its <code>apply</code> methods.
 * The <code>Informer</code> will then pass the information to the <code>Reporter</code> via an <code>InfoProvided</code> event.
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
trait FunSuite extends org.scalatest.Suite with FixtureSuite { thisSuite =>

  private val IgnoreTagName = "org.scalatest.Ignore"

  private abstract class FunNode
  private case class Test(testName: String, testFunction: Fixture => Unit) extends FunNode
  private case class Info(message: String) extends FunNode

  // Access to the testNamesList, testsMap, and tagsMap must be synchronized, because the test methods are invoked by
  // the primary constructor, but testNames, tags, and runTest get invoked directly or indirectly
  // by run. When running tests concurrently with ScalaTest Runner, different threads can
  // instantiate and run the suite. Instead of synchronizing, I put them in an immutable Bundle object (and
  // all three collections--testNamesList, testsMap, and tagsMap--are immuable collections), then I put the Bundle
  // in an AtomicReference. Since the expected use case is the test method will be called
  // from the primary constructor, which will be all done by one thread, I just in effect use optimistic locking on the Bundle.
  // If two threads ever called test at the same time, they could get a ConcurrentModificationException.
  // Test names are in reverse order of test registration method invocations
  private class Bundle private(
    val testNamesList: List[String],
    val doList: List[FunNode],
    val testsMap: Map[String, Test],
    val tagsMap: Map[String, Set[String]],
    val registrationClosed: Boolean
  ) {
    def unpack = (testNamesList, doList, testsMap, tagsMap, registrationClosed)
  }

  private object Bundle {
    def apply(
      testNamesList: List[String],
      doList: List[FunNode],
      testsMap: Map[String, Test],
      tagsMap: Map[String, Set[String]],
      registrationClosed: Boolean
    ): Bundle =
      new Bundle(testNamesList, doList,testsMap, tagsMap, registrationClosed)
  }

  private val atomic = new AtomicReference[Bundle](Bundle(List(), List(), Map(), Map(), false))

  private val shouldRarelyIfEverBeSeen = """
    Two threads attempted to modify FunSuite's internal data, which should only be
    modified by the thread that constructs the object. This likely means that a subclass
    has allowed the this reference to escape during construction, and some other thread
    attempted to invoke the "testsFor" or "test" method on the object before the first
    thread completed its construction.
  """

  private def updateAtomic(oldBundle: Bundle, newBundle: Bundle) {
    if (!atomic.compareAndSet(oldBundle, newBundle))
      throw new ConcurrentModificationException(shouldRarelyIfEverBeSeen)
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
  implicit protected def info: Informer = {
    if (atomicInformer == null || atomicInformer.get == null)
      registrationInformer
    else
      atomicInformer.get
  }

  // Hey, my first lazy val. Turns out classes must be initialized before
  // the traits they mix in. Thus currentInformer was null when it was accessed via
  // an info outside a test. This solves the problem.
  private lazy val registrationInformer: Informer =
    new Informer {
      def apply(message: String) {
        if (message == null)
          throw new NullPointerException
        val oldBundle = atomic.get
        var (testNamesList, doList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack
        doList ::= Info(message)
        updateAtomic(oldBundle, Bundle(testNamesList, doList, testsMap, tagsMap, registrationClosed))
      }
    }

  // This must *not* be lazy, so that it will stay null while the class's constructors are being executed,
  // because that's how I detect that construction is happenning (the registration phase) in the info method.
  private val zombieInformer =
    new Informer {
      private val complaint = "Sorry, you can only use FunSuite's info when executing the suite."
      def apply(message: String) {
        if (message == null)
          throw new NullPointerException
        throw new IllegalStateException(complaint)
      }
    }

  /**
   * Register a test with the specified name, optional tags, and function value that takes no arguments.
   * This method will register the test for later execution via an invocation of one of the <code>run</code>
   * methods. The passed test name must not have been registered previously on
   * this <code>FunSuite</code> instance.
   *
   * @param testName the name of the test
   * @param testTags the optional list of tags for this test
   * @param testFun the test function
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws NotAllowedException if <code>testName</code> had been registered previously
   * @throws NullPointerException if <code>testName</code> or any passed test tag is <code>null</code>
   */
  protected def test(testName: String, testTags: Tag*)(f: Fixture => Unit) {

    if (testName == null)
      throw new NullPointerException("testName was null")
    if (testTags.exists(_ == null))
      throw new NullPointerException("a test tag was null")

    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("testCannotAppearInsideAnotherTest"), getStackDepth("FunSuite.scala", "test"))

    if (atomic.get.testsMap.keySet.contains(testName))
      throw new DuplicateTestNameException(Resources("duplicateTestName", testName), getStackDepth("FunSuite.scala", "test"))

    val oldBundle = atomic.get
    var (testNamesList, doList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack

    val testNode = Test(testName, f)
    testsMap += (testName -> testNode)
    testNamesList ::= testName
    doList ::= testNode
    val tagNames = Set[String]() ++ testTags.map(_.name)
    if (!tagNames.isEmpty)
      tagsMap += (testName -> tagNames)

    updateAtomic(oldBundle, Bundle(testNamesList, doList, testsMap, tagsMap, registrationClosed))
  }

  /**
   * Register a test to ignore, which has the specified name, optional tags, and function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>run</code>
   * methods. This method exists to make it easy to ignore an existing test method by changing the call to <code>test</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be run, but a
   * report will be sent that indicates the test was ignored. The passed test name must not have been registered previously on
   * this <code>FunSuite</code> instance.
   *
   * @param testName the name of the test
   * @param testTags the optional list of tags for this test
   * @param testFun the test function
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws NotAllowedException if <code>testName</code> had been registered previously
   */
  protected def ignore(testName: String, testTags: Tag*)(f: Fixture => Unit) {

    if (testName == null)
      throw new NullPointerException("testName was null")
    if (testTags.exists(_ == null))
      throw new NullPointerException("a test tag was null")

    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("ignoreCannotAppearInsideATest"), getStackDepth("FunSuite.scala", "ignore"))

    test(testName)(f) // Call test without passing the tags

    val oldBundle = atomic.get
    var (testNamesList, doList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack

    val tagNames = Set[String]() ++ testTags.map(_.name)
    tagsMap += (testName -> (tagNames + IgnoreTagName))

    updateAtomic(oldBundle, Bundle(testNamesList, doList, testsMap, tagsMap, registrationClosed))
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
    // I'm returning a ListSet here so that they tests will be run in registration order
    ListSet(atomic.get.testNamesList.toArray: _*)
  }

  // runTest should throw IAE if a test name is passed that doesn't exist. Looks like right now it just reports a test failure.
  /**
   * Run a test. This trait's implementation runs the test registered with the name specified by <code>testName</code>.
   *
   * @param testName the name of one test to run.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param goodies a <code>Map</code> of properties that can be used by the executing <code>Suite</code> of tests.
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, or <code>goodies</code>
   *     is <code>null</code>.
   */
  protected override def runTest(testName: String, reporter: Reporter, stopper: Stopper, goodies: Map[String, Any], tracker: Tracker) {

    if (testName == null || reporter == null || stopper == null || goodies == null)
      throw new NullPointerException

    val stopRequested = stopper
    val report = wrapReporterIfNecessary(reporter)

    // Create a Rerunner if the FunSuite has a no-arg constructor
    val hasPublicNoArgConstructor = org.scalatest.Suite.checkForPublicNoArgConstructor(getClass)

    val rerunnable =
      if (hasPublicNoArgConstructor)
        Some(new TestRerunner(getClass.getName, testName))
      else
        None

    val testStartTime = System.currentTimeMillis
    report(TestStarting(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), testName, None, rerunnable))

    try {

      val theTest = atomic.get.testsMap(testName)

      val oldInformer = atomicInformer.get
      val informerForThisTest =
        new ConcurrentInformer(NameInfo(thisSuite.suiteName, Some(thisSuite.getClass.getName), Some(testName))) {
          def apply(message: String) {
            if (message == null)
              throw new NullPointerException
            report(InfoProvided(tracker.nextOrdinal(), message, nameInfoForCurrentThread))
          }
        }

      atomicInformer.set(informerForThisTest)
      try {
        withFixture(theTest.testFunction, goodies)
      }
      finally {
        val success = atomicInformer.compareAndSet(informerForThisTest, oldInformer)
        val rarelyIfEverSeen = """
          Two threads have apparently attempted to run tests at the same time. This has
          resulted in both threads attempting to change the current informer.
        """
        if (!success)
          throw new ConcurrentModificationException(rarelyIfEverSeen)
      }

      val duration = System.currentTimeMillis - testStartTime
      report(TestSucceeded(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), testName, Some(duration), None, rerunnable))
    }
    catch {
      case _: TestPendingException =>
        report(TestPending(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), testName))
      case e: Exception => {
        val duration = System.currentTimeMillis - testStartTime
        handleFailedTest(e, false, testName, rerunnable, report, tracker, duration)
      }
      case ae: AssertionError => {
        val duration = System.currentTimeMillis - testStartTime
        handleFailedTest(ae, false, testName, rerunnable, report, tracker, duration)
      }
    }
  }

  private def handleFailedTest(throwable: Throwable, hasPublicNoArgConstructor: Boolean, testName: String,
      rerunnable: Option[Rerunner], reporter: Reporter, tracker: Tracker, duration: Long) {

    val message =
      if (throwable.getMessage != null) // [bv: this could be factored out into a helper method]
        throwable.getMessage
      else
        throwable.toString

    reporter(TestFailed(tracker.nextOrdinal(), message, thisSuite.suiteName, Some(thisSuite.getClass.getName), testName, Some(throwable), Some(duration), None, rerunnable))
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> tag names to which tests in this <code>FunSuite</code> belong, and values
   * the <code>Set</code> of test names that belong to each tag. If this <code>FunSuite</code> contains no tags, this method returns an empty <code>Map</code>.
   *
   * <p>
   * This trait's implementation returns tags that were passed as strings contained in <code>Tag</code> objects passed to
   * methods <code>test</code> and <code>ignore</code>.
   * </p>
   */
  override def tags: Map[String, Set[String]] = atomic.get.tagsMap

  private[scalatest] override def getTestNameForReport(testName: String) = {

    if (testName == null)
      throw new NullPointerException("testName was null")

    suiteName + ", " + testName
  }

  protected override def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      goodies: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    if (testName == null)
      throw new NullPointerException("testName was null")
    if (reporter == null)
      throw new NullPointerException("reporter was null")
    if (stopper == null)
      throw new NullPointerException("stopper was null")
    if (filter == null)
      throw new NullPointerException("filter was null")
    if (goodies == null)
      throw new NullPointerException("goodies was null")
    if (distributor == null)
      throw new NullPointerException("distributor was null")
    if (tracker == null)
      throw new NullPointerException("tracker was null")

    val stopRequested = stopper

    // Wrap any non-DispatchReporter, non-CatchReporter in a CatchReporter,
    // so that exceptions are caught and transformed
    // into error messages on the standard error stream.
    val report = wrapReporterIfNecessary(reporter)

    // If a testName is passed to run, just run that, else run the tests returned
    // by testNames.
    testName match {
      case Some(tn) => runTest(tn, report, stopRequested, goodies, tracker)
      case None =>

        val doList = atomic.get.doList.reverse
        for (node <- doList) {
          node match {
            case Info(message) => info(message)
            case Test(tn, _) =>
              val (filterTest, ignoreTest) = filter(tn, tags)
              if (!filterTest)
                if (ignoreTest)
                  report(TestIgnored(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), tn))
                else
                  runTest(tn, report, stopRequested, goodies, tracker)
          }
        }
    }
  }

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      goodies: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    val stopRequested = stopper

    // Set the flag that indicates registration is closed (because run has now been invoked),
    // which will disallow any further invocations of "test" or "ignore" with
    // an RegistrationClosedException.
    val oldBundle = atomic.get
    val (testNamesList, doList, testsMap, tagsMap, registrationClosed) = oldBundle.unpack
    if (!registrationClosed)
      updateAtomic(oldBundle, Bundle(testNamesList, doList, testsMap, tagsMap, true))

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
      super.run(testName, report, stopRequested, filter, goodies, distributor, tracker)
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

  class FixtureTestsForPhrase {

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * testsFor(nonEmptyStack(lastValuePushed))
     *         ^
     * </pre>
     */
    def apply(unit: Unit) {}
  }

  val testsFor = new FixtureTestsForPhrase
}

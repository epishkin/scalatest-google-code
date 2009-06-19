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

import NodeFamily._
import scala.collection.immutable.ListSet
import org.scalatest.TestFailedExceptionHelper.getStackDepth

import org.scalatest.events._

/*
Note: the info in this class will when the test is running, put it into cold storage, and send it after the test completes. This
will be a bit odd though when a test fails. In the print report, the given when then things will show up after the stack trace. Perhaps
when a test fails, it doesn't print them? No, I know, it .... No
*/

/**
 * Trait that facilitates a &#8220;behavior-driven&#8221; style of development (BDD), in which tests
 * are combined with text that specifies the behavior the tests verify.
 * Here's an example <code>Spec</code>:
 *
 * <pre>
 * import org.scalatest.Spec
 * import scala.collection.mutable.Stack
 *
 * class StackSpec extends Spec {
 *
 *   describe("A Stack") {
 *
 *     it("should pop values in last-in-first-out order") {
 *       val stack = new Stack[Int]
 *       stack.push(1)
 *       stack.push(2)
 *       assert(stack.pop() === 2)
 *       assert(stack.pop() === 1)
 *     }
 *
 *     it("should throw NoSuchElementException if an empty stack is popped") {
 *       val emptyStack = new Stack[String]
 *       intercept[NoSuchElementException] {
 *         emptyStack.pop()
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * A <code>Spec</code> contains <em>describers</em> and <em>examples</em>. You define a describer
 * with <code>describe</code>, and a example with <code>it</code>. Both
 * <code>describe</code> and <code>it</code> are methods, defined in
 * <code>Spec</code>, which will be invoked
 * by the primary constructor of <code>StackSpec</code>. 
 * A describer names, or gives more information about, the <em>subject</em> (class or other entity) you are specifying
 * and testing. In the above example, "A Stack"
 * is the subject under specification and test. With each example you provide a string (the <em>spec text</em>) that specifies
 * one bit of behavior of the subject, and a block of code that tests that behavior.
 * You place the spec text between the parentheses, followed by the test code between curly
 * braces.  The test code will be wrapped up as a function passed as a by-name parameter to
 * <code>it</code>, which will register the test for later execution.
 * </p>
 *
 * <p>
 * When you execute a <code>Spec</code>, it will send <code>SpecReport</code>s to the
 * <code>Reporter</code>. ScalaTest's built-in reporters will report these <code>SpecReports</code> in such a way
 * that the output is easy to read as an informal specification of the entity under test.
 * For example, if you ran <code>StackSpec</code> from within the Scala interpreter:
 * </p>
 *
 * <pre>
 * scala> (new StackSpec).execute()
 * </pre>
 *
 * <p>
 * You would see:
 * </p>
 *
 * <pre>
 * A Stack
 * - should pop values in last-in-first-out order
 * - should throw NoSuchElementException if an empty stack is popped
 * </pre>
 *
 * <p>
 * <strong>Test fixtures</strong>
 * </p>
 *
 * <p>
 * A test <em>fixture</em> is objects or other artifacts (such as files, sockets, database
 * connections, etc.) used by tests to do their work. You can use fixtures in
 * <code>Spec</code>s with the same approaches suggested for <code>Suite</code> in
 * its documentation. The same text that appears in the test fixture
 * section of <code>Suite</code>'s documentation is repeated here, with examples changed from
 * <code>Suite</code> to <code>Spec</code>.
 * </p>
 *
 * <p>
 * If a fixture is used by only one test, then the definitions of the fixture objects should
 * be local to the test function, such as the objects assigned to <code>stack</code> and <code>emptyStack</code> in the
 * previous <code>StackSpec</code> examples. If multiple tests need to share a fixture, the best approach
 * is to assign them to instance variables. Here's a (very contrived) example, in which the object assigned
 * to <code>shared</code> is used by multiple test functions:
 * </p>
 *
 * <pre>
 * import org.scalatest.Spec
 *
 * class ArithmeticSpec extends Spec {
 *
 *   // Sharing fixture objects via instance variables
 *   val shared = 5
 *
 *   it("should add correctly") {
 *     val sum = 2 + 3
 *     assert(sum === shared)
 *   }
 *
 *   it("should subtract correctly") {
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
 * import org.scalatest.Spec
 * import scala.collection.mutable.ListBuffer
 *
 * class MySpec extends Spec {
 *
 *   // create objects needed by tests and return as a tuple
 *   def createFixture = (
 *     new StringBuilder("ScalaTest is "),
 *     new ListBuffer[String]
 *   )
 *
 *   it("should mutate shared fixture objects") {
 *     val (builder, lbuf) = createFixture
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(lbuf.isEmpty)
 *     lbuf += "sweet"
 *   }
 *
 *   it("should get a fresh set of mutable fixture objects") {
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
 * import org.scalatest.Spec
 * import scala.collection.mutable.ListBuffer
 *
 * class MySpec extends Spec {
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
 *   it("should mutate shared fixture objects") {
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
 *   it("should get a fresh set of mutable fixture objects") {
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
 * import org.scalatest.Spec
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MySpec extends Spec {
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
 *   it("should read from a temp file") {
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
 *   it("should read the first char of a temp file") {
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
 * import org.scalatest.Spec
 * import org.scalatest.BeforeAndAfter
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 *
 * class MySpec extends Spec with BeforeAndAfter {
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
 *   it("should read from a temp file") {
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     assert(builder.toString === "Hello, test!")
 *   }
 *
 *   it("should read the first char of a temp file") {
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
 * A <code>Spec</code>'s tests may be classified into named <em>groups</em>.
 * As with any suite, when executing a <code>Spec</code>, groups of tests can
 * optionally be included and/or excluded. To place <code>Spec</code> tests into
 * groups, you pass objects that extend abstract class <code>org.scalatest.Group</code> to the methods
 * that register tests, <code>it</code> and <code>ignore</code>. Class <code>Group</code> takes one parameter,
 * a string name.  If you have
 * created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>Spec</code>s that match. To do so, simply 
 * pass the fully qualified names of the Java interfaces to the <code>Group</code> constructor. For example, if you've
 * defined Java annotation interfaces with fully qualified names, <code>com.mycompany.groups.SlowTest</code> and <code>com.mycompany.groups.DBTest</code>, then you could
 * create matching groups for <code>Spec</code>s like this:
 * </p>
 * <pre>
 * import org.scalatest.Group
 *
 * object SlowTest extends Group("com.mycompany.groups.SlowTest")
 * object DBTest extends Group("com.mycompany.groups.DBTest")
 * </pre>
 * <p>
 * Given these definitions, you could place <code>Spec</code> tests into groups like this:
 * </p>
 * <pre>
 * import org.scalatest.Spec
 *
 * class MySuite extends Spec {
 *
 *   it("should add correctly", SlowTest) {
 *     val sum = 1 + 1
 *     assert(sum === 2)
 *     assert(sum + 2 === 4)
 *   }
 *
 *   it("should subtract correctly", SlowTest, DBTest) {
 *     val diff = 4 - 1
 *     assert(diff === 3)
 *     assert(diff - 2 === 1)
 *   }
 * }
 * </pre>
 *
 * <p>
 * This code places both tests into the <code>com.mycompany.groups.SlowTest</code> group, 
 * and test <code>"should subtract correctly"</code> into the <code>com.mycompany.groups.DBTest</code> group.
 * </p>
 *
 * <p>
 * The primary execute method takes two <code>Set[String]</code>s called <code>groupsToInclude</code> and
 * <code>groupsToExclude</code>. If <code>groupsToInclude</code> is empty, all tests will be executed
 * except those those belonging to groups listed in the
 * <code>groupsToExclude</code> <code>Set</code>. If <code>groupsToInclude</code> is non-empty, only tests
 * belonging to groups mentioned in <code>groupsToInclude</code>, and not mentioned in <code>groupsToExclude</code>,
 * will be executed.
 * </p>
 *
 * <p>
 * <strong>Ignored tests</strong>
 * </p>
 *
 * <p>
 * To support the common use case of &#8220;temporarily&#8221; disabling a test, with the
 * good intention of resurrecting the test at a later time, <code>Spec</code> provides registration
 * methods that start with <code>ignore</code> instead of <code>it</code>. For example, to temporarily
 * disable the test with the name <code>"should pop values in last-in-first-out order"</code>, just change &#8220;<code>it</code>&#8221; into &#8220;<code>ignore</code>,&#8221; like this:
 * </p>
 *
 * <pre>
 * import org.scalatest.Spec
 * import scala.collection.mutable.Stack
 *
 * class StackSpec extends Spec {
 *
 *   describe("A Stack") {
 *
 *     ignore("should pop values in last-in-first-out order") {
 *       val stack = new Stack[Int]
 *       stack.push(1)
 *       stack.push(2)
 *       assert(stack.pop() === 2)
 *       assert(stack.pop() === 1)
 *     }
 *
 *     it("should throw NoSuchElementException if an empty stack is popped") {
 *       val emptyStack = new Stack[String]
 *       intercept[NoSuchElementException] {
 *         emptyStack.pop()
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you run this version of <code>StackSpec</code> with:
 * </p>
 *
 * <pre>
 * scala> (new StackSpec).execute()
 * </pre>
 *
 * <p>
 * It will run only the second test and report that the first test was ignored:
 * </p>
 *
 * <pre>
 * A Stack
 * - should pop values in last-in-first-out order !!! IGNORED !!!
 * - should throw NoSuchElementException if an empty stack is popped
 * </pre>
 *
 * <p>
 * As with <code>org.scalatest.Suite</code>, the ignore feature is implemented as a group. The
 * <code>execute</code> method that takes no parameters
 * adds <code>org.scalatest.Ignore</code> to the <code>groupsToExclude</code> <code>Set</code> it passes to
 * the primary <code>execute</code> method, as does <code>Runner</code>. The only difference between
 * <code>org.scalatest.Ignore</code> and the groups you may define and exclude is that ScalaTest reports
 * ignored tests to the <code>Reporter</code>. The reason ScalaTest reports ignored tests is as a feeble
 * attempt to encourage ignored tests to be eventually fixed and added back into the active suite of tests.
 * </p>
 *
 * @author Bill Venners
 */
trait Spec extends Suite { thisSuite =>

  private val IgnoreGroupName = "org.scalatest.Ignore"

  private val trunk: Trunk = new Trunk
  private var currentBranch: Branch = trunk
  private var groupsMap: Map[String, Set[String]] = Map()

  // All examples, in reverse order of registration
  private var examplesList = List[Example]()

  // Used to detect at runtime that they've stuck a describe or an it inside an it,
  // which should result in a TestFailedException
  private var runningATest = false

  private def registerExample(specText: String, f: => Unit) = {
    val testName = getTestName(specText, currentBranch)
    if (examplesList.exists(_.testName == testName)) {
      throw new TestFailedException(Resources("duplicateTestName", testName), getStackDepth("Spec.scala", "it"))
    }
    val exampleShortName = specText
    val example = Example(currentBranch, testName, specText, currentBranch.level + 1, f _)
    currentBranch.subNodes ::= example
    examplesList ::= example
    testName
  }

  /**
   * Register a test with the given spec text, optional groups, and test function value that takes no arguments.
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
   * @param testGroups the optional list of groups to which this test belongs
   * @param testFun the test function
   * @throws TestFailedException if a test with the same name has been registered previously
   * @throws NullPointerException if <code>specText</code> or any passed test group is <code>null</code>
   */
  protected def it(specText: String, testGroups: Group*)(testFun: => Unit) {
    if (runningATest)
      throw new TestFailedException(Resources("itCannotAppearInsideAnotherIt"), getStackDepth("Spec.scala", "it"))
    if (specText == null)
      throw new NullPointerException("specText was null")
    if (testGroups.exists(_ == null))
      throw new NullPointerException("a test group was null")
    val testName = registerExample(specText, testFun)
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    if (!groupNames.isEmpty)
      groupsMap += (testName -> groupNames)
  }

  /**
   * Register a test with the given spec text and test function value that takes no arguments.
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
   * @param testFun the test function
   * @throws TestFailedException if a test with the same name has been registered previously
   * @throws NullPointerException if <code>specText</code> or any passed test group is <code>null</code>
   */
  protected def it(specText: String)(testFun: => Unit) {
    if (runningATest)
      throw new TestFailedException(Resources("itCannotAppearInsideAnotherIt"), getStackDepth("Spec.scala", "it"))
    it(specText, Array[Group](): _*)(testFun)
  }

  /**
   * Register a test to ignore, which has the given spec text, optional groups, and test function value that takes no arguments.
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
   * @param testGroups the optional list of groups to which this test belongs
   * @param testFun the test function
   * @throws TestFailedException if a test with the same name has been registered previously
   * @throws NullPointerException if <code>specText</code> or any passed test group is <code>null</code>
   */
  protected def ignore(specText: String, testGroups: Group*)(testFun: => Unit) {
    if (runningATest)
      throw new TestFailedException(Resources("ignoreCannotAppearInsideAnIt"), getStackDepth("Spec.scala", "ignore"))
    if (specText == null)
      throw new NullPointerException("specText was null")
    if (testGroups.exists(_ == null))
      throw new NullPointerException("a test group was null")
    val testName = registerExample(specText, testFun)
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    groupsMap += (testName -> (groupNames + IgnoreGroupName))
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
   * @throws TestFailedException if a test with the same name has been registered previously
   * @throws NullPointerException if <code>specText</code> or any passed test group is <code>null</code>
   */
  protected def ignore(specText: String)(testFun: => Unit) {
    if (runningATest)
      throw new TestFailedException(Resources("ignoreCannotAppearInsideAnIt"), getStackDepth("Spec.scala", "ignore"))
    ignore(specText, Array[Group](): _*)(testFun)
  }
  /**
   * Describe a &#8220;subject&#8221; being specified and tested by the passed function value. The
   * passed function value may contain more describers (defined with <code>describe</code>) and/or examples
   * (defined with <code>it</code>). This trait's implementation of this method will register the
   * description string and immediately invoke the passed function.
   */
  protected def describe(description: String)(f: => Unit) {

    if (runningATest)
      throw new TestFailedException(Resources("describeCannotAppearInsideAnIt"), getStackDepth("Spec.scala", "describe"))

    def insertBranch(newBranch: Branch, f: () => Unit) {
      val oldBranch = currentBranch
      currentBranch.subNodes ::= newBranch
      currentBranch = newBranch
      f()
      currentBranch = oldBranch
    }

    insertBranch(Description(currentBranch, description, currentBranch.level + 1), f _)
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> group names to which tests in this <code>Spec</code> belong, and values
   * the <code>Set</code> of test names that belong to each group. If this <code>FunSuite</code> contains no groups, this method returns an empty <code>Map</code>.
   *
   * <p>
   * This trait's implementation returns groups that were passed as strings contained in <code>Group</code> objects passed to 
   * methods <code>test</code> and <code>ignore</code>. 
   * </p>
   */
  override def groups: Map[String, Set[String]] = groupsMap

  private def runTestsInBranch(branch: Branch, reporter: Reporter, stopRequested: Stopper, groupsToInclude: Set[String], groupsToExclude: Set[String], goodies: Map[String, Any], tracker: Tracker) {
    branch match {
      case desc @ Description(_, descriptionName, level) => {

        def sendInfoProvidedMessage() {
          // Need to use the full name of the description, which includes all the descriptions it is nested inside
          // Call getPrefix and pass in this Desc, to get the full name
          val descriptionFullName = getPrefix(desc).trim
         
          val wrappedReporter = wrapReporterIfNecessary(reporter)
         
          // Call getTestNameForReport with the description, because that puts the Suite name
          // in front of the description, which looks good in the regular report.
          //val descriptionNameForReport = getTestNameForReport(descriptionFullName)
          //val report = new SpecReport(descriptionNameForReport, descriptionFullName, descriptionFullName, descriptionFullName, true, Some(suiteName), Some(thisSuite.getClass.getName), None)
          //val report = new SpecReport(descriptionNameForReport, descriptionFullName, descriptionFullName, descriptionFullName, true)
          wrappedReporter(InfoProvided(tracker.nextOrdinal(), descriptionFullName, Some(NameInfo(thisSuite.suiteName, Some(thisSuite.getClass.getName), None)), None, Some(IndentedText(descriptionFullName, descriptionFullName, 0))))
        }
        
        // Only send an infoProvided message if the first thing in the subNodes is *not* sub-description, i.e.,
        // it is an example, because otherwise we get a lame description that doesn't have any examples under it.
        // But send it if the list is empty.
        if (desc.subNodes.isEmpty)
          sendInfoProvidedMessage() 
        else
          desc.subNodes.reverse.head match {
            case ex: Example => sendInfoProvidedMessage()           
            case _ => // Do nothing in this case
          }
      }
      case _ =>
    }
    branch.subNodes.reverse.foreach(
      _ match {
        case ex @ Example(parent, testName, specText, level, f) => {
          // Wrap any non-DispatchReporter, non-CatchReporter in a CatchReporter,
          // so that exceptions are caught and transformed
          // into error messages on the standard error stream.
          val wrappedReporter = wrapReporterIfNecessary(reporter)

          val tn = ex.testName
          if (!stopRequested() && (groupsToInclude.isEmpty || !(groupsToInclude ** groups.getOrElse(tn, Set())).isEmpty)) {
            if (groupsToExclude.contains(IgnoreGroupName) && groups.getOrElse(tn, Set()).contains(IgnoreGroupName)) {
              val exampleSucceededIcon = Resources("exampleSucceededIconChar")
              val formattedSpecText = Resources("exampleIconPlusShortName", exampleSucceededIcon, ex.specText)
              //val report = new SpecReport(getTestNameForReport(tn), "", ex.specText, formattedSpecText, true, Some(suiteName), Some(thisSuite.getClass.getName), Some(testName))
              //val report = new SpecReport(getTestNameForReport(tn), "", ex.specText, formattedSpecText, true)
              wrappedReporter(TestIgnored(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), tn, Some(IndentedText(formattedSpecText, ex.specText, 1))))
            }
            else if ((groupsToExclude ** groups.getOrElse(tn, Set())).isEmpty) {
              runTest(tn, wrappedReporter, stopRequested, goodies, tracker)
            }
          }
        }
        case branch: Branch => runTestsInBranch(branch, reporter, stopRequested, groupsToInclude, groupsToExclude, goodies, tracker)
      }
    )
  }

  /**
   * Run a test. This trait's implementation runs the test registered with the name specified by
   * <code>testName</code>. Each test's name is a concatenation of the text of all describers surrounding an example,
   * from outside in, and the example's  spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.)
   *
   * @param testName the name of one test to execute.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopRequested the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param goodies a <code>Map</code> of properties that can be used by this <code>Spec</code>'s executing tests.
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopRequested</code>, or <code>goodies</code>
   *     is <code>null</code>.
   */
  override def runTest(testName: String, reporter: Reporter, stopRequested: Stopper, goodies: Map[String, Any], tracker: Tracker) {

    if (testName == null || reporter == null || stopRequested == null || goodies == null)
      throw new NullPointerException

    runningATest = true
    try {
      examplesList.find(_.testName == testName) match {
        case None => throw new IllegalArgumentException("Requested test doesn't exist: " + testName)
        case Some(example) => {
          val wrappedReporter = wrapReporterIfNecessary(reporter)
  
          val exampleSucceededIcon = Resources("exampleSucceededIconChar")
          val formattedSpecText = Resources("exampleIconPlusShortName", exampleSucceededIcon, example.specText)
  
          // Create a Rerunner if the Spec has a no-arg constructor
          val hasPublicNoArgConstructor = Suite.checkForPublicNoArgConstructor(getClass)
  
          val rerunnable =
            if (hasPublicNoArgConstructor)
              Some(new TestRerunner(getClass.getName, testName))
            else
              None
       
          // A TestStarting event won't normally show up in a specification-style output, but
          // will show up in a test-style output.
/*
          val report =
            //new SpecReport(getTestNameForReport(example.testName), "", example.specText, formattedSpecText, false, Some(suiteName), Some(thisSuite.getClass.getName), Some(testName), None, rerunnable)
            new SpecReport(getTestNameForReport(example.testName), "", example.specText, formattedSpecText, false, None, rerunnable)
*/
          wrappedReporter(TestStarting(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), example.testName, Some(MotionToSuppress), rerunnable))

          try {
            example.f()

	    //val report = new SpecReport(getTestNameForReport(example.testName), "", example.specText, formattedSpecText, true, Some(suiteName), Some(thisSuite.getClass.getName), Some(testName), None, rerunnable)
	    // val report = new SpecReport(getTestNameForReport(example.testName), "", example.specText, formattedSpecText, true, None, rerunnable)
 
            val formatter = IndentedText(formattedSpecText, example.specText, 1)
            wrappedReporter(TestSucceeded(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), example.testName, None, Some(formatter), rerunnable)) // TODO: add a duration
          }
          catch { 
            case e: Exception => 
              handleFailedTest(e, false, example.testName, example.specText, formattedSpecText, rerunnable, wrappedReporter, tracker)
            case ae: AssertionError =>
              handleFailedTest(ae, false, example.testName, example.specText, formattedSpecText, rerunnable, wrappedReporter, tracker)
          }
        }
      }
    }
    finally {
      runningATest = false
    }
  }

  private[scalatest] override def getTestNameForReport(testName: String) = {

    if (testName == null)
      throw new NullPointerException("testName was null")

    suiteName + ", " + testName
  }

  private def handleFailedTest(throwable: Throwable, hasPublicNoArgConstructor: Boolean, testName: String,
      specText: String, formattedSpecText: String, rerunnable: Option[Rerunner], reporter: Reporter, tracker: Tracker) {

    val message =
      if (throwable.getMessage != null) // [bv: this could be factored out into a helper method]
        throwable.getMessage
      else
        throwable.toString

    //val report = new SpecReport(getTestNameForReport(testName), msg, specText, "- " + specText, true, Some(suiteName), Some(thisSuite.getClass.getName), Some(testName), Some(t), rerunnable)
    // val report = new SpecReport(getTestNameForReport(testName), msg, specText, "- " + specText, true, Some(t), rerunnable)

    val formatter = IndentedText(formattedSpecText, specText, 1)
    reporter(TestFailed(tracker.nextOrdinal(), message, thisSuite.suiteName, Some(thisSuite.getClass.getName), testName, Some(throwable), None, Some(formatter), rerunnable)) // TODO: Add a duration
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
   * <li><code>stopRequested</code> - the <code>Stopper</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>goodies</code> - the <code>goodies</code> <code>Map</code> passed to this method, or one that wraps and delegates to it</li>
   * </ul>
   *
   * <p>
   * This method takes a <code>Set</code> of group names that should be included (<code>groupsToInclude</code>), and a <code>Set</code>
   * that should be excluded (<code>groupsToExclude</code>), when deciding which of this <code>Suite</code>'s tests to execute.
   * If <code>groupsToInclude</code> is empty, all tests will be executed
   * except those those belonging to groups listed in the <code>groupsToExclude</code> <code>Set</code>. If <code>groupsToInclude</code> is non-empty, only tests
   * belonging to groups mentioned in <code>groupsToInclude</code>, and not mentioned in <code>groupsToExclude</code>
   * will be executed. However, if <code>testName</code> is <code>Some</code>, <code>groupsToInclude</code> and <code>groupsToExclude</code> are essentially ignored.
   * Only if <code>testName</code> is <code>None</code> will <code>groupsToInclude</code> and <code>groupsToExclude</code> be consulted to
   * determine which of the tests named in the <code>testNames</code> <code>Set</code> should be run. For more information on trait groups, see the main documentation for this trait.
   * </p>
   *
   * <p>
   * If <code>testName</code> is <code>None</code>, this trait's implementation of this method
   * invokes <code>testNames</code> on this <code>Suite</code> to get a <code>Set</code> of names of tests to potentially execute.
   * (A <code>testNames</code> value of <code>None</code> essentially acts as a wildcard that means all tests in
   * this <code>Suite</code> that are selected by <code>groupsToInclude</code> and <code>groupsToExclude</code> should be executed.)
   * For each test in the <code>testName</code> <code>Set</code>, in the order
   * they appear in the iterator obtained by invoking the <code>elements</code> method on the <code>Set</code>, this trait's implementation
   * of this method checks whether the test should be run based on the <code>groupsToInclude</code> and <code>groupsToExclude</code> <code>Set</code>s.
   * If so, this implementation invokes <code>runTest</code>, passing in:
   * </p>
   *
   * <ul>
   * <li><code>testName</code> - the <code>String</code> name of the test to run (which will be one of the names in the <code>testNames</code> <code>Set</code>)</li>
   * <li><code>reporter</code> - the <code>Reporter</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>stopRequested</code> - the <code>Stopper</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>goodies</code> - the <code>goodies</code> <code>Map</code> passed to this method, or one that wraps and delegates to it</li>
   * </ul>
   *
   * @param testName an optional name of one test to execute. If <code>None</code>, all relevant tests should be executed.
   *                 I.e., <code>None</code> acts like a wildcard that means execute all relevant tests in this <code>Spec</code>.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopRequested the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param groupsToInclude a <code>Set</code> of <code>String</code> group names to include in the execution of this <code>Spec</code>
   * @param groupsToExclude a <code>Set</code> of <code>String</code> group names to exclude in the execution of this <code>Spec</code>
   * @param goodies a <code>Map</code> of key-value pairs that can be used by this <code>Spec</code>'s executing tests.
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopRequested</code>, <code>groupsToInclude</code>,
   *     <code>groupsToExclude</code>, or <code>goodies</code> is <code>null</code>.
   */
  override def runTests(testName: Option[String], reporter: Reporter, stopRequested: Stopper, groupsToInclude: Set[String], groupsToExclude: Set[String],
      goodies: Map[String, Any], tracker: Tracker) {
    
    if (testName == null)
      throw new NullPointerException("testName was null")
    if (reporter == null)
      throw new NullPointerException("reporter was null")
    if (stopRequested == null)
      throw new NullPointerException("stopRequested was null")
    if (groupsToInclude == null)
      throw new NullPointerException("groupsToInclude was null")
    if (groupsToExclude == null)
      throw new NullPointerException("groupsToExclude was null")
    if (goodies == null)
      throw new NullPointerException("goodies was null")

    testName match {
      case None => runTestsInBranch(trunk, reporter, stopRequested, groupsToInclude, groupsToExclude, goodies, tracker)
      case Some(exampleName) => runTest(exampleName, reporter, stopRequested, goodies, tracker)
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
  override def testNames: Set[String] = ListSet(examplesList.map(_.testName): _*)
}

/*
Here's one way to do pending. I'll need to add a testPending message to Reporter. The pending methods
that take a testFun would execute the function, catch any exception that comes out and probably throw
it away, then throw a pending exception. The caller would catch PendingException and report it with
a testPending rather than a TestSucceeded or TestFailed message:

Actually I changed my mind. Won't do this. Will just make a

object pending

And have it, text, scenario, and ignore that take it after the string, before any groups.

  it("should do something", pending) {}

I could eventually give the pending object an apply method, which would just do the usual take
an implicit Informer thing, but I think I will ask people just to use info to start with:

  scenario("should be almost spiritual", pending) {}

  scenario("should be almost spiritual", pending) {

    given("bla given")
      info("pending bla bla bla bla bla")
    when("bla when")
    and("bla and")
    then("bla then")
  }

The reason I thought it might be confusing is to see pending down there you may think the whole thing is pending
but it isn't pending unless you put pending after the specText:
  scenario("should be almost spiritual", pending) {

    given("bla given")
      pending("bla bla bla bla bla")
    when("bla when")
    and("bla and")
    then("bla then")
  }

Well maybe that's OK. I could also make them different by making the one after specText all caps:

  scenario("should be almost spiritual", PENDING) {

    given("bla given")
      pending("bla bla bla bla bla")
    when("bla when")
    and("bla and")
    then("bla then")
  }

Or just the first letter upper case?

  scenario("should be almost spiritual", Pending) {

    given("bla given")
      pending("bla bla bla bla bla")
    when("bla when")
    and("bla and")
    then("bla then")
  }

Nah, that's too subtle

I can have an @Pending annotation for Suite, and this can be when
I make a requirement that the group annotations must extend GroupAnnotation
interface.

class Spec {

  class PendingException(msg: String) extends RuntimeException(msg)

  def pending(msg: String)(testFun: => Unit) {
    testFun
    throw new PendingException(msg)
  }

  def pending: Unit = pending("Not Yet Implemented") {}

  def pending(testFun: => Unit): Unit = pending("Not Yet Implemented") {}

  def it(specText: String)(testFun: => Unit) {
    println(specText + " implemented")
  }
  def given(s: String) { println("given " + s) }
  def when(s: String) { println("when " + s) }
  def then(s: String) { println("then " + s) }
  def and(s: String) { println("and " + s) }
}

class MySpec extends Spec {

  it("should be almost spiritual") { pending }

  it("should be almost spiritual") {
    pending("something") {
      given("bla given")
      when("bla when")
      and("bla and")
      then("bla then")
    }
  }

  it("should be almost spiritual") {
    pending {
      given("bla given")
      when("bla when")
      and("bla and")
      then("bla then")
    }
  }

  it("should be quite animal") {
    println("hi")
  }
}

The other thought I had was that runTests can wrap the passed reporter in a SpecReporter, which would
 hold onto infoProvided messages until after the next TestSucceeded or TestFailed message, at which
 point it would release them. This would also be done in FeatureSuite. That way the given, when, then stuff
 would show up under the line about the test itself:

 - should do something decent !!! FAILED !!!
   + given some setup thing
   + and some other setup then
   + when some action happens
   + then some result occurs
   + and some other result occurs

 This would be the spec output even though the information that the test failed was reported chronologically
 after the given when then info provided's were reported.

// I like this one. Features, not Feature. Can have
// a FeatureParser though.
class MyFeatures extends Features {

  feature("transfer from savings to checking account") {

    As a "savings account holder"
    I want "to transfer money from my savings account to my checking account"
    So that "I can get cash easily from an ATM"

    scenario("savings account has sufficient funds", PENDING) {
      given("my savings account balance is $100")
      and("my checking account balance is $10")
      when("I transfer $20 from savings to checking")
      then("my savings account balance should be $80")
      and("my checking account balance should be $30")
    }

    scenario("savings account has insufficient funds", PENDING) {
      given("my savings account balance is $50")
      and("my checking account balance is $10")
      when("I transfer $60 from savings to checking")
      then("my savings account balance should be $50")
      and("my checking account balance should be $10")
    }
  }

  feature("transfer from checking to savings account") {

    As a "savings account holder"
    I want "to transfer money from my savings account to my checking account"
    So that "I can get cash easily from an ATM"

    scenario("savings account has sufficient funds", PENDING) {
      given("my savings account balance is $100")
      and("my checking account balance is $10")
      when("I transfer $20 from savings to checking")
      then("my savings account balance should be $80")
      and("my checking account balance should be $30")
    }

    scenario("savings account has insufficient funds", PENDING) {
      given("my savings account balance is $50")
      and("my checking account balance is $10")
      when("I transfer $60 from savings to checking")
      then("my savings account balance should be $50")
      and("my checking account balance should be $10")
    }
  }
}
 */

/*
I dropped this from the scaladoc for spec, because it is ugly until I release
the should behave like stuff, and really I question how much 'should behave like' is needed.
 * <strong>Shared examples</strong>
 *
 * <p>
 * You can place examples that you would like to reuse in multiple places in methods or functions, then include these
 * "shared" examples wherever you want to reuse them by invoking the method or function. Here's a longer code example
 * illustrating this manner of factoring out of common examples into methods:
 *
 * <pre>
 * 
 * import org.scalatest.Spec
 * 
 * import scala.collection.mutable.ListBuffer
 * 
 * class Stack[T] {
 *   val MAX = 10
 *   private var buf = new ListBuffer[T]
 *   def push(o: T) {
 *     if (!full)
 *       o +: buf
 *     else
 *       throw new IllegalStateException("can't push onto a full stack")
 *   }
 *   def pop(): T = {
 *     if (!empty)
 *       buf.remove(0)
 *     else
 *       throw new IllegalStateException("can't pop an empty stack")
 *   }
 *   def peek: T = {
 *     if (!empty)
 *       buf(0)
 *     else
 *       throw new IllegalStateException("can't pop an empty stack")
 *   }
 *   def full: Boolean = buf.size == MAX
 *   def empty: Boolean = buf.size == 0
 *   def size = buf.size
 * }
 * 
 * 
 * trait StackBehaviors { this: Spec =>
 * 
 *   def includeNonEmptyStackExamples(stack: Stack[Int], lastItemAdded: Int) {
 * 
 *     it("should be non-empty") {
 *       assert(!stack.empty)
 *     }  
 * 
 *     it("should return the top item on peek") {
 *       assert(stack.peek === lastItemAdded)
 *     }
 *   
 *     it("should not remove the top item on peek") {
 *       val size = stack.size
 *       assert(stack.peek === lastItemAdded)
 *       assert(stack.size === size)
 *     }
 *   
 *     it("should remove the top item on pop") {
 *       val size = stack.size
 *       assert(stack.pop === lastItemAdded)
 *       assert(stack.size === size - 1)
 *     }
 *   }
 *   
 *   def includeNonFullStackExamples(stack: Stack[Int]) {
 *       
 *     it("should not be full") {
 *       assert(!stack.full)
 *     }
 *       
 *     it("should add to the top on push") {
 *       val size = stack.size
 *       stack.push(7)
 *       assert(stack.size === size + 1)
 *       assert(stack.peek === 7)
 *     }
 *   }
 * }
 * 
 * class StackSpec extends Spec with StackBehaviors {
 * 
 *   // fixture creation methods
 *   def emptyStack = new Stack[Int]
 *   def fullStack = {
 *     val stack = new Stack[Int]
 *     for (i <- 0 until stack.MAX)
 *       stack.push(i)
 *     stack
 *   }
 *   def stackWithOneItem = {
 *     val stack = new Stack[Int]
 *     stack.push(9)
 *     stack
 *   }
 *   def stackWithOneItemLessThanCapacity = {
 *     val stack = new Stack[Int]
 *     for (i <- 1 to 9)
 *       stack.push(i)
 *     stack
 *   }
 *   val lastValuePushed = 9
 * 
 *   describe("A Stack") {
 * 
 *     describe("(when empty)") {
 *       
 *       it("should be empty") {
 *         assert(emptyStack.empty)
 *       }
 * 
 *       it("should complain on peek") {
 *         intercept[IllegalStateException] {
 *           emptyStack.peek
 *         }
 *       }
 * 
 *       it("should complain on pop") {
 *         intercept[IllegalStateException] {
 *           emptyStack.pop
 *         }
 *       }
 *     }
 * 
 *     describe("(with one item)") {
 *       includeNonEmptyStackExamples(stackWithOneItem, lastValuePushed)
 *       includeNonFullStackExamples(stackWithOneItem)
 *     }
 *     
 *     describe("(with one item less than capacity)") {
 *       includeNonEmptyStackExamples(stackWithOneItemLessThanCapacity, lastValuePushed)
 *       includeNonFullStackExamples(stackWithOneItemLessThanCapacity)
 *     }
 * 
 *     describe("(when full)") {
 *       
 *       it("should be full") {
 *         assert(fullStack.full)
 *       }
 * 
 *       includeNonEmptyStackExamples(fullStack, lastValuePushed)
 * 
 *       it("should complain on a push") {
 *         intercept[IllegalStateException] {
 *           fullStack.push(10)
 *         }
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you load this file into the Scala interpreter (with scalatest's JAR file on the class path), and execute it,
 * you'll see:
 * </p>
 *
 * <pre>
 * scala> (new StackSpec).execute()
 * A Stack (when empty)
 * - should be empty
 * - should complain on peek
 * - should complain on pop
 * A Stack (with one item)
 * - should be non-empty
 * - should return the top item on peek
 * - should not remove the top item on peek
 * - should remove the top item on pop
 * - should not be full
 * - should add to the top on push
 * A Stack (with one item less than capacity)
 * - should be non-empty
 * - should return the top item on peek
 * - should not remove the top item on peek
 * - should remove the top item on pop
 * - should not be full
 * - should add to the top on push
 * A Stack (when full)
 * - should be full
 * - should be non-empty
 * - should return the top item on peek
 * - should not remove the top item on peek
 * - should remove the top item on pop
 * - should complain on a push
 * </pre>
 * 
*/

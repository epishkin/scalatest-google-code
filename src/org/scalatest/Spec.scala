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
 *       intercept(classOf[NoSuchElementException]) {
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
 *       intercept(classOf[NoSuchElementException]) {
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
 * adds <code>org.scalatest.Ignore</code> to the <code>excludes</code> <code>Set</code> it passes to
 * the primary <code>execute</code> method, as does <code>Runner</code>. The only difference between
 * <code>org.scalatest.Ignore</code> and the groups you may define and exclude is that ScalaTest reports
 * ignored tests to the <code>Reporter</code>. The reason ScalaTest reports ignored tests is as a feeble
 * attempt to encourage ignored tests to be eventually fixed and added back into the active suite of tests.
 * </p>
 *
 * @author Bill Venners
 */
trait Spec extends Suite {

  private val IgnoreGroupName = "org.scalatest.Ignore"

  private val trunk: Trunk = new Trunk
  private var currentBranch: Branch = trunk
  private var groupsMap: Map[String, Set[String]] = Map()

  // All examples, in reverse order of registration
  private var examplesList = List[Example]()

  /**
   *
   */
  protected def assertBehavesLike[T](target: T, fun: (T) => Behavior) {
    val sharedBehavior = fun(target)
    val sharedExamples = sharedBehavior.examples(currentBranch)
    currentBranch.subNodes :::= sharedExamples
    examplesList :::= sharedExamples
  }

  private def registerExample(exampleRawName: String, f: => Unit) = {
    val exampleFullName = getExampleFullName(exampleRawName, currentBranch)
    require(!examplesList.exists(_.exampleFullName == exampleFullName), "Duplicate test name: " + exampleFullName)
    val exampleShortName = getExampleShortName(exampleRawName, currentBranch)
    val example = Example(currentBranch, exampleFullName, exampleRawName, exampleShortName, currentBranch.level + 1, f _)
    currentBranch.subNodes ::= example
    examplesList ::= example
    exampleFullName
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
   * @throws IllegalArgumentException if a test with the same name had been registered previously
   */
  protected def it(specText: String, testGroups: Group*)(f: => Unit) {
    val exampleFullName = registerExample(specText, f)
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    if (!groupNames.isEmpty)
      groupsMap += (exampleFullName -> groupNames)
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
   * @throws IllegalArgumentException if a test with the same name had been registered previously
   */
  protected def ignore(specText: String, testGroups: Group*)(f: => Unit) {
    val exampleFullName = registerExample(specText, f)
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    groupsMap += (exampleFullName -> (groupNames + IgnoreGroupName))

  }

  /**
   * Describe a &#8220;subject&#8221; being specified and tested by the passed function value. The
   * passed function value may contain more describers (defined with <code>describe</code>) and/or examples
   * (defined with <code>it</code>). This trait's implementation of this method will register the
   * description string and immediately invoke the passed function.
   */
  protected def describe(description: String)(f: => Unit) {

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

  private def runTestsInBranch(branch: Branch, reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String], goodies: Map[String, Any]) {
    branch match {
      case desc @ Description(_, descriptionName, level) => {

        def sendInfoProvidedMessage() {
          // Need to use the full name of the description, which includes all the descriptions it is nested inside
          // Call getPrefix and pass in this Desc, to get the full name
          val descriptionFullName = getPrefix(desc).trim
         
          val wrappedReporter = wrapReporterIfNecessary(reporter)
         
          // Call getTestNameForReport with the description, because that puts the Suite name
          // in front of the description, which looks good in the regular report.
          val descriptionNameForReport = getTestNameForReport(descriptionFullName)
          val report = new SpecReport(descriptionNameForReport, descriptionFullName, descriptionFullName, descriptionFullName, true)
          wrappedReporter.infoProvided(report)
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
        case ex @ Example(parent, exampleFullName, exampleRawName, specText, level, f) => {
          // Wrap any non-DispatchReporter, non-CatchReporter in a CatchReporter,
          // so that exceptions are caught and transformed
          // into error messages on the standard error stream.
          val wrappedReporter = wrapReporterIfNecessary(reporter)

          val tn = ex.exampleFullName
          if (!stopper.stopRequested && (includes.isEmpty || !(includes ** groups.getOrElse(tn, Set())).isEmpty)) {
            if (excludes.contains(IgnoreGroupName) && groups.getOrElse(tn, Set()).contains(IgnoreGroupName)) {
              val exampleSucceededIcon = Resources("exampleSucceededIconChar")
              val formattedSpecText = Resources("exampleIconPlusShortName", exampleSucceededIcon, ex.specText)
              val report = new SpecReport(getTestNameForReport(tn), "", ex.specText, formattedSpecText, true)
              wrappedReporter.testIgnored(report)
            }
            else if ((excludes ** groups.getOrElse(tn, Set())).isEmpty) {
              runTest(tn, wrappedReporter, stopper, goodies)
            }
          }
        }
        case branch: Branch => runTestsInBranch(branch, reporter, stopper, includes, excludes, goodies)
      }
    )
  }

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
  override def runTest(testName: String, reporter: Reporter, stopper: Stopper, goodies: Map[String, Any]) {

    examplesList.find(_.exampleFullName == testName) match {
      case None => throw new IllegalArgumentException("Requested test doesn't exist: " + testName)
      case Some(example) => {
        val wrappedReporter = wrapReporterIfNecessary(reporter)

        val exampleSucceededIcon = Resources("exampleSucceededIconChar")
        val formattedSpecText = Resources("exampleIconPlusShortName", exampleSucceededIcon, example.specText)

        // A testStarting report won't normally show up in a specification-style output, but
        // will show up in a test-style output.
        val report = new SpecReport(getTestNameForReport(example.exampleFullName), "", example.specText, formattedSpecText, false)

        wrappedReporter.testStarting(report)

        try {
          example.f()

          val report = new SpecReport(getTestNameForReport(example.exampleFullName), "", example.specText, formattedSpecText, true)

          wrappedReporter.testSucceeded(report)
        }
        catch { 
          case e: Exception => 
            handleFailedTest(e, false, example.exampleFullName, example.specText, None, wrappedReporter)          
          case ae: AssertionError =>
            handleFailedTest(ae, false, example.exampleFullName, example.specText, None, wrappedReporter)
        }
      }
    }
  }
  
  private[scalatest] override def getTestNameForReport(testName: String) = {

    if (testName == null)
      throw new NullPointerException("testName was null")

    suiteName + ": " + testName
  }

  private def handleFailedTest(t: Throwable, hasPublicNoArgConstructor: Boolean, exampleFullName: String,
      specText: String, rerunnable: Option[Rerunnable], reporter: Reporter) {

    val msg =
      if (t.getMessage != null) // [bv: this could be factored out into a helper method]
        t.getMessage
      else
        t.toString

    val report = new SpecReport(getTestNameForReport(exampleFullName), msg, specText, "- " + specText, true, Some(t), None)

    reporter.testFailed(report)
  }

  override def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
      goodies: Map[String, Any]) {
    
    testName match {
      case None => runTestsInBranch(trunk, reporter, stopper, includes, excludes, goodies)
      case Some(exampleName) => runTest(exampleName, reporter, stopper, goodies)
    }
    
  }

  // ACK: TODO: COUNT tests in nested suites!
  /**
   * The total number of tests that are expected to run when this <code>Spec</code>'s <code>execute</code> method is invoked.
   * This trait's implementation of this method returns the sum of:
   *
   * <ul>
   * <li>the size of the <code>testNames</code> <code>List</code>
   * <li>the sum of the values obtained by invoking
   *     <code>expecteTestCount</code> on every nested <code>Suite</code> contained in
   *     <code>nestedSuites</code>
   * </ul>
   */
  override def expectedTestCount(includes: Set[String], excludes: Set[String]): Int = {
    countTestsInBranch(trunk)
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
  override def testNames: Set[String] = ListSet(examplesList.map(_.exampleFullName): _*)
}


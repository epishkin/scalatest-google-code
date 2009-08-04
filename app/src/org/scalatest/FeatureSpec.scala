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
import org.scalatest.StackDepthExceptionHelper.getStackDepth
import java.util.concurrent.atomic.AtomicReference
import java.util.ConcurrentModificationException
import org.scalatest.events._

/**
 * A suite of tests in which each test represents one <em>scenario</em> of a <em>feature</em>. 
 * <code>FeatureSpec</code> is intended for writing tests that are "higher level" than unit tests, for example, integration
 * tests, functional tests, and acceptance tests. You can use <code>FeatureSpec</code> for unit testing if you prefer, however.
 * Here's an example:
 *
 * <pre>
 * import org.scalatest.FeatureSpec
 * import org.scalatest.GivenWhenThen
 * import scala.collection.mutable.Stack
 * 
 * class StackFeatureSpec extends FeatureSpec with GivenWhenThen {
 * 
 *   feature("The user can pop an element off the top of the stack") {
 * 
 *     info("As a programmer")
 *     info("I want to be able to pop items off the stack")
 *     info("So that I can get them in last-in-first-out order")
 * 
 *     scenario("pop is invoked on a non-empty stack") {
 * 
 *       given("a non-empty stack")
 *       val stack = new Stack[Int]
 *       stack.push(1)
 *       stack.push(2)
 *       val oldSize = stack.size
 * 
 *       when("when pop is invoked on the stack")
 *       val result = stack.pop()
 * 
 *       then("the most recently pushed element should be returned")
 *       assert(result === 2)
 * 
 *       and("the stack should have one less item than before")
 *       assert(stack.size === oldSize - 1)
 *     }
 * 
 *     scenario("pop is invoked on an empty stack") {
 * 
 *       given("an empty stack")
 *       val emptyStack = new Stack[String]
 * 
 *       when("when pop is invoked on the stack")
 *       then("NoSuchElementException should be thrown")
 *       intercept[NoSuchElementException] {
 *         emptyStack.pop()
 *       }
 * 
 *       and("the stack should still be empty")
 *       assert(emptyStack.isEmpty)
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * A <code>FeatureSpec</code> contains <em>feature clauses</em> and <em>scenarios</em>. You define a feature clause
 * with <code>feature</code>, and a scenario with <code>scenario</code>. Both
 * <code>feature</code> and <code>scenario</code> are methods, defined in
 * <code>FeatureSpec</code>, which will be invoked
 * by the primary constructor of <code>StackFeatureSpec</code>. 
 * A feature clause describes a feature of the <em>subject</em> (class or other entity) you are specifying
 * and testing. In the previous example, 
 * the subject under specification and test is a stack. The feature being specified and tested is 
 * the ability for a user (a programmer in this case) to pop an element off the top of the stack. With each scenario you provide a
 * string (the <em>spec text</em>) that specifies the behavior of the subject for
 * one scenario in which the feature may be used, and a block of code that tests that behavior.
 * You place the spec text between the parentheses, followed by the test code between curly
 * braces.  The test code will be wrapped up as a function passed as a by-name parameter to
 * <code>scenario</code>, which will register the test for later execution.
 * </p>
 *
 * <p>
 * A <code>FeatureSpec</code>'s lifecycle has two phases: the <em>registration</em> phase and the
 * <em>ready</em> phase. It starts in registration phase and enters ready phase the first time
 * <code>run</code> is called on it. It then remains in ready phase for the remainder of its lifetime.
 * </p>
 *
 * <p>
 * Scenarios can only be registered with the <code>scenario</code> method while the <code>FeatureSpec</code> is
 * in its registration phase. Any attempt to register a scenario after the <code>FeatureSpec</code> has
 * entered its ready phase, <em>i.e.</em>, after <code>run</code> has been invoked on the <code>FeatureSpec</code>,
 * will be met with a thrown <code>TestRegistrationClosedException</code>. The recommended style
 * of using <code>FeatureSpec</code> is to register tests during object construction as is done in all
 * the examples shown here. If you keep to the recommended style, you should never see a
 * <code>TestRegistrationClosedException</code>.
 * </p>
 *
 * <p>
 * Each scenario represents one test. The name of the test is the spec text passed to the <code>scenario</code> method.
 * The feature name does not appear as part of the test name. In a <code>FeatureSpec</code>, therefore, you must take care
 * to ensure that each test has a unique name (in other words, that each <code>scenario</code> has unique spec text).
 * </p>
 *
 * <p>
 * When you run a <code>FeatureSpec</code>, it will send <code>Formatter</code>s in the events it sends to the
 * <code>Reporter</code>. ScalaTest's built-in reporters will report these events in such a way
 * that the output is easy to read as an informal specification of the <em>subject</em> being tested.
 * For example, if you ran <code>StackFeatureSpec</code> from within the Scala interpreter:
 * </p>
 *
 * <pre>
 * scala> (new StackFeatureSpec).run()
 * </pre>
 *
 * <p>
 * You would see:
 * </p>
 *
 * <pre>
 * Feature: The user can pop an element off the top of the stack 
 *   As a programmer 
 *   I want to be able to pop items off the stack 
 *   So that I can get them in last-in-first-out order 
 *   Scenario: pop is invoked on a non-empty stack
 *     Given a non-empty stack 
 *     When when pop is invoked on the stack 
 *     Then the most recently pushed element should be returned 
 *     And the stack should have one less item than before 
 *   Scenario: pop is invoked on an empty stack
 *     Given an empty stack 
 *     When when pop is invoked on the stack 
 *     Then NoSuchElementException should be thrown 
 *     And the stack should still be empty 
 * </pre>
 *
 * <p>
 * <strong>Shared fixtures</strong>
 * </p>
 *
 * <p>
 * A test <em>fixture</em> is objects or other artifacts (such as files, sockets, database
 * connections, etc.) used by tests to do their work. You can use fixtures in
 * <code>FeatureSpec</code>s with the same approaches suggested for <code>Suite</code> in
 * its documentation. The same text that appears in the test fixture
 * section of <code>Suite</code>'s documentation is repeated here, with examples changed from
 * <code>Suite</code> to <code>FeatureSpec</code>.
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
 * import org.scalatest.FeatureSpec
 *
 * class MySuite extends FeatureSpec {
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
 * import org.scalatest.FeatureSpec
 * import scala.collection.mutable.ListBuffer
 *
 * class MySuite extends FeatureSpec {
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
 * If different tests in the same <code>FeatureSpec</code> require different fixtures, you can create multiple create-fixture methods and
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
 * import org.scalatest.FeatureSpec
 * import scala.collection.mutable.ListBuffer
 *
 * class MySuite extends FeatureSpec {
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
 * import org.scalatest.FeatureSpec
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MySuite extends FeatureSpec {
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
 * If different tests in the same <code>FeatureSpec</code> require different fixtures, you can create multiple with-fixture methods and
 * call the method (or methods) needed by each test at the beginning of the test. A common case, however, will be that all
 * the tests in a suite need to share the same fixture. To facilitate the with-fixture approach in this common case of a single, shared fixture,
 * ScalaTest provides sister traits in the <code>org.scalatest.fixture</code> package that
 * directly support the with-fixture approach. Every test in an <code>org.scalatest.fixture</code> trait takes a fixture whose type
 * is defined by the <code>Fixture</code> type. For example, trait <code>org.scalatest.fixture.FeatureSpec</code> behaves exactly like
 * <code>org.scalatest.FeatureSpec</code>, except each test method takes a <code>Fixture</code>. For the details, see the documentation for
 * <a href="fixture/FeatureSpec.html"><code>FeatureSpec</code></a>. To get the idea, however, here's what the previous example would
 * look like rewritten to use an <code>org.scalatest.fixture.FeatureSpec</code>:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixtureFeatureSpec
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MySuite extends FixtureFeatureSpec {
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
 * import org.scalatest.FeatureSpec
 * import org.scalatest.BeforeAndAfterEach
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 *
 * class MySuite extends FeatureSpec with BeforeAndAfterEach {
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
 * <strong>Shared tests</strong>
 * </p>
 *
 * <p>
 * In a <code>FeatureSpec</code>
 * there is no nesting construct analogous to <code>Spec</code>'s <code>describe</code> clause. If the duplicate test name problem shows up in a
 * <code>FeatureSpec</code>, you'll need to pass in a prefix or suffix string to add to each test name. You can pass this string
 * the same way you pass any other data needed by the shared tests, or just call <code>toString</code> on the shared fixture object.
 * Here's an example of how <code>StackBehaviors</code> might look for a <code>FeatureSpec</code>:
 * </p>
 *
 * <pre>
 * trait StackBehaviors { this: FeatureSpec =>
 * 
 *   def nonEmptyStack(lastItemAdded: Int)(stack: Stack[Int]) {
 * 
 *     test(stack.toString + " should be non-empty") {
 *       assert(!stack.empty)
 *     }  
 * 
 *     test(stack.toString + " should return the top item on peek") {
 *       assert(stack.peek === lastItemAdded)
 *     }
 *   
 *     test(stack.toString + " should not remove the top item on peek") {
 *       val size = stack.size
 *       assert(stack.peek === lastItemAdded)
 *       assert(stack.size === size)
 *     }
 *   
 *     test(stack.toString + " should remove the top item on pop") {
 *       val size = stack.size
 *       assert(stack.pop === lastItemAdded)
 *       assert(stack.size === size - 1)
 *     }
 *   }
 *   
 *   // ...
 * }
 * </pre>
 *
 * <p>
 * Given this <code>StackBahaviors</code> trait, calling it with the <code>stackWithOneItem</code> fixture, like this:
 * </p>
 *
 * <pre>
 * testsFor(nonEmptyStack(stackWithOneItem, lastValuePushed))
 * </pre>
 *
 * <p>
 * would yield test names:
 * </p>
 *
 * <ul>
 * <li><code>Stack(9) should be non-empty</code></li>
 * <li><code>Stack(9) should return the top item on peek</code></li>
 * <li><code>Stack(9) should not remove the top item on peek</code></li>
 * <li><code>Stack(9) should remove the top item on pop</code></li>
 * </ul>
 *
 * <p>
 * Whereas calling it with the <code>stackWithOneItemLessThanCapacity</code> fixture, like this:
 * </p>
 *
 * <pre>
 * testsFor(nonEmptyStack(stackWithOneItemLessThanCapacity, lastValuePushed))
 * </pre>
 *
 * <p>
 * would yield different test names:
 * </p>
 *
 * <ul>
 * <li><code>Stack(9, 8, 7, 6, 5, 4, 3, 2, 1) should be non-empty</code></li>
 * <li><code>Stack(9, 8, 7, 6, 5, 4, 3, 2, 1) should return the top item on peek</code></li>
 * <li><code>Stack(9, 8, 7, 6, 5, 4, 3, 2, 1) should not remove the top item on peek</code></li>
 * <li><code>Stack(9, 8, 7, 6, 5, 4, 3, 2, 1) should remove the top item on pop</code></li>
 * </ul>
 *
 * <p>
 * <strong>Tagging tests</strong>
 * </p>
 *
 * <p>
 * A <code>FeatureSpec</code>'s tests may be classified into groups by <em>tagging</em> them with string names.
 * As with any suite, when executing a <code>FeatureSpec</code>, groups of tests can
 * optionally be included and/or excluded. To tag a <code>FeatureSpec</code>'s tests,
 * you pass objects that extend abstract class <code>org.scalatest.Tag</code> to methods
 * that register tests, <code>test</code> and <code>ignore</code>. Class <code>Tag</code> takes one parameter, a string name.  If you have
 * created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>FeatureSpec</code>s that match. To do so, simply 
 * pass the fully qualified names of the Java interfaces to the <code>Tag</code> constructor. For example, if you've
 * defined Java annotation interfaces with fully qualified names, <code>com.mycompany.groups.SlowTest</code> and
 * <code>com.mycompany.groups.DBTest</code>, then you could
 * create matching groups for <code>FeatureSpec</code>s like this:
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
 * Given these definitions, you could place <code>FeatureSpec</code> tests into groups like this:
 * </p>
 *
 * <pre>
 * import org.scalatest.FeatureSpec
 *
 * class MySuite extends FeatureSpec {
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
 * good intention of resurrecting the test at a later time, <code>FeatureSpec</code> provides registration
 * methods that start with <code>ignore</code> instead of <code>test</code>. For example, to temporarily
 * disable the test named <code>addition</code>, just change &#8220;<code>test</code>&#8221; into &#8220;<code>ignore</code>,&#8221; like this:
 * </p>
 *
 * <pre>
 * import org.scalatest.FeatureSpec
 *
 * class MySuite extends FeatureSpec {
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
 * <code>org.scalatest.Spec</code>, you can also use it in <code>FeatureSpec</code>, like this:
 * </p>
 *
 * <pre>
 * import org.scalatest.FeatureSpec
 *
 * class MySuite extends FeatureSpec {
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
 * Most often the reporting done by default by <code>FeatureSpec</code>'s methods will be sufficient, but
 * occasionally you may wish to provide custom information to the <code>Reporter</code> from a test.
 * For this purpose, an <code>Informer</code> that will forward information to the current <code>Reporter</code>
 * is provided via the <code>info</code> parameterless method.
 * You can pass the extra information to the <code>Informer</code> via one of its <code>apply</code> methods.
 * The <code>Informer</code> will then pass the information to the <code>Reporter</code> via an <code>InfoProvided</code> event.
 * Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest.FeatureSpec
 *
 * class MySuite extends FeatureSpec {
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
trait FeatureSpec extends Suite { thisSuite =>

  private val IgnoreTagName = "org.scalatest.Ignore"

  private class Bundle private(
    val trunk: Trunk,
    val currentBranch: Branch,
    val tagsMap: Map[String, Set[String]],

    // All tests, in reverse order of registration
    val testsList: List[TestLeaf],

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
      testsList: List[TestLeaf],
      registrationClosed: Boolean
    ): Bundle =
      new Bundle(trunk, currentBranch, tagsMap, testsList, registrationClosed)

    def initialize(
      trunk: Trunk,
      tagsMap: Map[String, Set[String]],
      testsList: List[TestLeaf],
      registrationClosed: Boolean
    ): Bundle =
      new Bundle(trunk, trunk, tagsMap, testsList, registrationClosed)
  }

  private val atomic =
    new AtomicReference[Bundle](
      Bundle.initialize(new Trunk, Map(), List[TestLeaf](), false)
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

  private def registerTest(specText: String, f: => Unit) = {

    val oldBundle = atomic.get
    var (trunk, currentBranch, tagsMap, testsList, registrationClosed) = oldBundle.unpack

    val testName = getTestName(specText, currentBranch)
    if (testsList.exists(_.testName == testName)) {
      throw new DuplicateTestNameException(testName, getStackDepth("Spec.scala", "it"))
    }
    val testShortName = specText
    val test = TestLeaf(currentBranch, testName, specText, f _)
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
  protected def scenario(specText: String, testTags: Tag*)(testFun: => Unit) {

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
  protected def scenario(specText: String)(testFun: => Unit) {
    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("itCannotAppearInsideAnotherIt"), getStackDepth("Spec.scala", "it"))
    scenario(specText, Array[Tag](): _*)(testFun)
  }

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
  protected def ignore(specText: String, testTags: Tag*)(testFun: => Unit) {
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
  protected def ignore(specText: String)(testFun: => Unit) {
    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("ignoreCannotAppearInsideAnIt"), getStackDepth("Spec.scala", "ignore"))
    ignore(specText, Array[Tag](): _*)(testFun)
  }
  
  /**
   * Describe a &#8220;subject&#8221; being specified and tested by the passed function value. The
   * passed function value may contain more describers (defined with <code>describe</code>) and/or tests
   * (defined with <code>it</code>). This trait's implementation of this method will register the
   * description string and immediately invoke the passed function.
   */
  protected def feature(description: String)(f: => Unit) {

    if (atomic.get.registrationClosed)
      throw new TestRegistrationClosedException(Resources("describeCannotAppearInsideAnIt"), getStackDepth("Spec.scala", "describe"))

    def createNewBranch() = {
      val oldBundle = atomic.get
      var (trunk, currentBranch, tagsMap, testsList, registrationClosed) = oldBundle.unpack

      val newBranch = DescriptionBranch(currentBranch, Resources("feature", description))
      val oldBranch = currentBranch
      currentBranch.subNodes ::= newBranch
      currentBranch = newBranch

      updateAtomic(oldBundle, Bundle(trunk, currentBranch, tagsMap, testsList, registrationClosed))

      oldBranch
    }

    val oldBranch = createNewBranch()

    f

    val oldBundle = atomic.get
    val (trunk, currentBranch, tagsMap, testsList, registrationClosed) = oldBundle.unpack

    updateAtomic(oldBundle, Bundle(trunk, oldBranch, tagsMap, testsList, registrationClosed))
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
      case desc @ DescriptionBranch(_, descriptionName) =>

        def sendInfoProvidedMessage() {
          // Need to use the full name of the description, which includes all the descriptions it is nested inside
          // Call getPrefix and pass in this Desc, to get the full name
          val descriptionFullName = getPrefix(desc).trim
         
          // Call getTestNameForReport with the description, because that puts the Suite name
          // in front of the description, which looks good in the regular report.
          report(InfoProvided(tracker.nextOrdinal(), descriptionFullName, Some(NameInfo(thisSuite.suiteName, Some(thisSuite.getClass.getName), None)), None, Some(IndentedText(descriptionFullName, descriptionFullName, 0))))
        }
        
        sendInfoProvidedMessage()

      case _ =>
    }
    branch.subNodes.reverse.foreach(
      _ match {
        case TestLeaf(_, tn, specText, _) =>
          if (!stopRequested()) { // TODO: Seems odd to me to check for stop here but still fire infos
            val (filterTest, ignoreTest) = filter(tn, tags)
            if (!filterTest)
              if (ignoreTest) {
                val formattedSpecText = "  " + Resources("scenario", specText)
                report(TestIgnored(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), tn, Some(IndentedText(formattedSpecText, specText, 1))))
              }
              else
                runTest(tn, report, stopRequested, configMap, tracker)
          }
        case InfoLeaf(_, message) =>
          val formattedText = "  " +  message
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

        val scenarioSpecText = Resources("scenario", test.specText)
        val formattedSpecText = "  " + scenarioSpecText

        // Create a Rerunner if the Spec has a no-arg constructor
        val hasPublicNoArgConstructor = Suite.checkForPublicNoArgConstructor(getClass)

        val rerunnable =
          if (hasPublicNoArgConstructor)
            Some(new TestRerunner(getClass.getName, testName))
          else
            None

        val testStartTime = System.currentTimeMillis

        // A TestStarting event won't normally show up in a specification-style output, but
        // will show up in a test-style output.
        report(TestStarting(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), test.testName, Some(MotionToSuppress), rerunnable))

        val formatter = IndentedText(formattedSpecText, scenarioSpecText, 1)
        val oldInformer = atomicInformer.get
        val informerForThisTest =
          new MessageRecordingInformer(NameInfo(thisSuite.suiteName, Some(thisSuite.getClass.getName), Some(testName))) {
            def apply(message: String) {
              if (message == null)
                throw new NullPointerException
              if (shouldRecord)
                record(message)
              else {
                val formattedText = "    " + message
                report(InfoProvided(tracker.nextOrdinal(), message, nameInfoForCurrentThread, None, Some(IndentedText(formattedText, message, 2))))
              }
            }
          }

        atomicInformer.set(informerForThisTest)
        try {
          test.f()

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
            val formattedText = "    " + message
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

  class ScenariosForPhrase {

    /**
     * This method enables the following syntax:
     *
     * <pre>
     * scenariosFor(nonEmptyStack(lastValuePushed))
     *             ^
     * </pre>
     */
    def apply(unit: Unit) {}
  }

  val scenariosFor = new ScenariosForPhrase
}
/*
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
 * scala> (new StackSpec).run()
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
 * groups, you pass objects that extend abstract class <code>org.scalatest.Tag</code> to the methods
 * that register tests, <code>it</code> and <code>ignore</code>. Class <code>Tag</code> takes one parameter,
 * a string name.  If you have
 * created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>Spec</code>s that match. To do so, simply 
 * pass the fully qualified names of the Java interfaces to the <code>Tag</code> constructor. For example, if you've
 * defined Java annotation interfaces with fully qualified names, <code>com.mycompany.groups.SlowTest</code> and <code>com.mycompany.groups.DBTest</code>, then you could
 * create matching groups for <code>Spec</code>s like this:
 * </p>
 * <pre>
 * import org.scalatest.Tag
 *
 * object SlowTest extends Tag("com.mycompany.groups.SlowTest")
 * object DBTest extends Tag("com.mycompany.groups.DBTest")
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
 * scala> (new StackSpec).run()
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
 * You can mark a test as pending in <code>Spec</code> by placing "<code>(pending)</code>" after the 
 * test name, like this:
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
 *     it("should pop values in last-in-first-out order") {
 *       val stack = new Stack[Int]
 *       stack.push(1)
 *       stack.push(2)
 *       assert(stack.pop() === 2)
 *       assert(stack.pop() === 1)
 *     }
 *
 *     it("should throw NoSuchElementException if an empty stack is popped") (pending)
 *   }
 * }
 * </pre>
 *
 * <p>
 * (Note: "<code>(pending)</code>" is the body of the test. Thus the test contains just one statement, an invocation
 * of the <code>pending</code> method, which throws <code>TestPendingException</code>.)
 * If you run this version of <code>StackSpec</code> with:
 * </p>
 *
 * <pre>
 * scala> (new StackSpec).run()
 * </pre>
 *
 * <p>
 * It will run both tests, but report that the test named "<code>A stack should pop values in last-in-first-out order</code>" is pending. You'll see:
 * </p>
 *
 * <pre>
 * A Stack 
 * - should pop values in last-in-first-out order
 * - should throw NoSuchElementException if an empty stack is popped (pending)
 * </pre>
 */

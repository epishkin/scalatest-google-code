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

import java.io.Serializable
import java.lang.annotation.Annotation
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import Suite.parseSimpleName
import Suite.stripDollars
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType
import scala.collection.immutable.TreeSet

/**
 * <p>
 * A suite of tests. A <code>Suite</code> instance encapsulates a conceptual
 * suite (i.e., a collection) of tests. This trait defines a default way to create
 * and execute tests, which involves writing <em>test methods</em>. This approach will likely suffice
 * in the vast majority of applications, but if desired, subtypes can override certain methods
 * to define other ways to create and execute tests.
 * </p>
 *
 * <p>
 * The easiest way to use this trait is to use its default approach: Simply create classes that
 * extend <code>Suite</code> and define test methods. Test methods have names of the form <code>testX</code>, 
 * where <code>X</code> is some unique, hopefully meaningful, string. A test method must be public and
 * can have any result type, but the most common result type is <code>Unit</code>. Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest.Suite
 *
 * class MySuite extends Suite {
 *
 *   def testAddition() {
 *     val sum = 1 + 1
 *     assert(sum === 2)
 *     assert(sum + 2 === 4)
 *   }
 *
 *   def testSubtraction() {
 *     val diff = 4 - 1
 *     assert(diff === 3)
 *     assert(diff - 2 === 1)
 *   }
 * }
 * </pre>
 *
 * <p>
 * You run a <code>Suite</code> by invoking on it one of three overloaded <code>execute</code>
 * methods. Two of these <code>execute</code> methods, which print test results to the
 * standard output, are intended to serve as a
 * convenient way to run tests from within the Scala interpreter. For example,
 * to run <code>MySuite</code> from within the Scala interpreter, you could write:
 * </p>
 *
 * <pre>
 * scala> (new MySuite).execute()
 * </pre>
 *
 * <p>
 * And you would see:
 * </p>
 *
 * <pre>
 * Test Starting - MySuite.testAddition
 * Test Succeeded - MySuite.testAddition
 * Test Starting - MySuite.testSubtraction
 * Test Succeeded - MySuite.testSubtraction
 * </pre>
 *
 * <p>
 * Or, to run just the <code>testAddition</code> method, you could write:
 * </p>
 *
 * <pre>
 * scala> (new MySuite).execute("testAddition")
 * </pre>
 *
 * <p>
 * And you would see:
 * </p>
 *
 * <pre>
 * Test Starting - MySuite.testAddition
 * Test Succeeded - MySuite.testAddition
 * </pre>
 *
 * <p>
 * The third overloaded <code>execute</code> method takes seven parameters, so it is a bit unwieldy to invoke from
 * within the Scala interpreter. Instead, this <code>execute</code> method is intended to be invoked indirectly by a test runner, such
 * as <code>org.scalatest.tools.Runner</code> or an IDE. See the <a href="tools/Runner$object.html">documentation for <code>Runner</code></a> for more detail.
 * </p>
 *
 * <p>
 * <strong>Assertions and ===</strong>
 * </p>
 *
 * <p>
 * Inside test methods, you can write assertions by invoking <code>assert</code> and passing in a <code>Boolean</code> expression,
 * such as:
 * </p>
 *
 * <pre>
 * val left = 2
 * val right = 1
 * assert(left == right)
 * </pre>
 *
 * <p>
 * If the passed expression is <code>true</code>, <code>assert</code> will return normally. If <code>false</code>,
 * <code>assert</code> will complete abruptly with an <code>AssertionError</code>. This exception is usually not caught
 * by the test method, which means the test method itself will complete abruptly by throwing the <code>AssertionError</code>. Any
 * test method that completes abruptly with an <code>AssertionError</code> or any <code>Exception</code> is considered a failed
 * test. A test method that returns normally is considered a successful test.
 * </p>
 *
 * <p>
 * If you pass a <code>Boolean</code> expression to <code>assert</code>, a failed assertion will be reported, but without
 * reporting the left and right values. You can alternatively encode these values in a <code>String</code> passed as
 * a second argument to <code>assert</code>, as in:
 * </p>
 * 
 * <pre>
 * val left = 2
 * val right = 1
 * assert(left == right, left + " did not equal " + right)
 * </pre>
 *
 * <p>
 * Using this form of <code>assert</code>, the failure report will include the left and right values, thereby
 * helping you debug the problem. However, <code>Suite</code> provides the <code>===</code> operator to make this easier.
 * You use it like this:
 * </p>
 *
 * <pre>
 * val left = 2
 * val right = 1
 * assert(left === right)
 * </pre>
 *
 * <p>
 * Because you use <code>===</code> here instead of <code>==</code>, the failure report will include the left
 * and right values. For example, the detail message in the thrown <code>AssertionErrorm</code> from the <code>assert</code>
 * shown previously will include, "2 did not equal 1".
 * From this message you will know that the operand on the left had the value 2, and the operand on the right had the value 1.
 * </p>
 *
 * <p>
 * If you're familiar with JUnit, you would use <code>===</code>
 * in a ScalaTest <code>Suite</code> where you'd use <code>assertEquals</code> in a JUnit <code>TestCase</code>.
 * The <code>===</code> operator is made possible by an implicit conversion from <code>Any</code>
 * to <code>Equalizer</code>. If you're curious to understand the mechanics, see the <a href="Suite.Equalizer.html">documentation for
 * <code>Equalizer</code></a> and <code>Suite</code>'s <code>convertToEqualizer</code> method.
 * </p>
 *
 * <p>
 * <strong>Expected results</strong>
 * </p>
 *
 * Although <code>===</code> provides a natural, readable extension to Scala's <code>assert</code> mechanism,
 * as the operands become lengthy, the code becomes less readable. In addition, the <code>===</code> comparison
 * doesn't distinguish between actual and expected values. The operands are just called <code>left</code> and <code>right</code>,
 * because if one were named <code>expected</code> and the other <code>actual</code>, it would be difficult for people to
 * remember which was which. To help with these limitations of assertions, <code>Suite</code> includes a method called <code>expect</code> that
 * can be used as an alternative to <code>assert</code> with <code>===</code>. To use <code>expect</code>, you place
 * the expected value in parentheses after <code>expect</code>, and follow that by code contained inside
 * curly braces that results in a value that you expect should equal the expected value. For example:
 *
 * <pre>
 * val a = 5
 * val b = 2
 * expect(2) {
 *   a - b
 * }
 * </pre>
 *
 * <p>
 * In this case, the expected value is <code>2</code>, and the code being tested is <code>a - b</code>. This expectation will fail, and
 * the detail message in the <code>AssertionError</code> will read, "Expected 2, but got 3."
 * </p>
 *
 * <p>
 * <strong>Intercepted exceptions</strong>
 * </p>
 *
 * <p>
 * Sometimes you need to test whether a method throws an expected exception under certain circumstances, such
 * as when invalid arguments are passed to the method. You can do this in the JUnit style, like this:
 * </p>
 *
 * <pre>
 * val s = "hi"
 * try {
 *   s.charAt(-1)
 *   fail()
 * }
 * catch {
 *   case e: IndexOutOfBoundsException => // Expected, so continue
 * }
 * </pre>
 *
 * <p>
 * If <code>charAt</code> throws <code>IndexOutOfBoundsException</code> as left, control will transfer
 * to the catch case, which does nothing. If, however, <code>charAt</code> fails to throw an exception,
 * the next statement, <code>fail()</code>, will be executed. The <code>fail</code> method always completes abruptly with
 * an <code>AssertionError</code>, thereby signaling a failed test.
 * </p>
 *
 * <p>
 * To make this common use case easier to express and read, <code>Suite</code> provides an <code>intercept</code>
 * method. You use it like this:
 * </p>
 *
 * <pre>
 * val s = "hi"
 * intercept(classOf[IndexOutOfBoundsException]) {
 *   s.charAt(-1)
 * }
 * </pre>
 *
 * <p>
 * This code behaves much like the previous example. If <code>charAt</code> throws an instance of <code>IndexOutOfBoundsException</code>,
 * <code>intercept</code> will return that exception. But if <code>charAt</code> completes normally, or throws a different
 * exception, <code>intercept</code> will complete abruptly with an <code>AssertionError</code>. <code>intercept</code> returns the
 * caught exception so that you can inspect it further if you wish, for example, to ensure that data contained inside
 * the exception has the expected values.
 * </p>
 *
 * <p>
 * <strong>Nested suites</strong>
 * </p>
 *
 * <p>
 * A <code>Suite</code> can refer to a collection of other <code>Suite</code>s,
 * which are called <em>nested</em> <code>Suite</code>s. Those nested  <code>Suite</code>s can in turn have
 * their own nested  <code>Suite</code>s, and so on. Large test suites can be organized, therefore, as a tree of
 * nested <code>Suite</code>s.
 * This trait's <code>execute</code> method, in addition to invoking its
 * test methods, invokes <code>execute</code> on each of its nested <code>Suite</code>s.
 * </p>
 *
 * <p>
 * A <code>List</code> of a <code>Suite</code>'s nested <code>Suite</code>s can be obtained by invoking its
 * <code>nestedSuites</code> method. If you wish to create a <code>Suite</code> that serves as a
 * container for nested <code>Suite</code>s, whether or not it has test methods of its own, simply override <code>nestedSuites</code>
 * to return a <code>List</code> of the nested <code>Suite</code>s. Because this is a common use case, ScalaTest provides
 * a convenience <code>SuperSuite</code> class, which takes a <code>List</code> of nested <code>Suite</code>s as a constructor
 * parameter. Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatet.Suite
 *
 * class ASuite extends Suite
 * class BSuite extends Suite
 * class CSuite extends Suite
 *
 * class AlphabetSuite extends SuperSuite(
 *   List(
 *     new ASuite,
 *     new BSuite,
 *     new CSuite
 *   )
 * )
 * </pre>
 *
 * <p>
 * If you now run <code>AlphabetSuite</code>, for example from the interpreter:
 * </p>
 *
 * <pre>
 * scala> (new AlphabetSuite).execute()
 * </pre>
 *
 * <p>
 * You will see reports printed to the standard output that indicate nested
 * suites&#8212;<code>ASuite</code>, <code>BSuite</code>, and
 * <code>CSuite</code>&#8212;were run.
 * </p>
 *
 * <p>
 * Note that <code>Runner</code> can discover <code>Suite</code>s automatically, so you need not
 * necessarily specify <code>SuperSuite</code>s explicitly. See the <a href="tools/Runner$object.html">documentation
 * for <code>Runner</code></a> for more information.
 * </p>
 *
 * <p>
 * <strong>Test fixtures</strong>
 * </p>
 *
 * <p>
 * A test <em>fixture</em> is objects or other artifacts (such as files, sockets, database
 * connections, etc.) used by tests to do their work.
 * If a fixture is used by only one test method, then the definitions of the fixture objects should
 * be local to the method, such as the objects assigned to <code>sum</code> and <code>diff</code> in the
 * previous <code>MySuite</code> examples. If multiple methods need to share a fixture, the best approach
 * is to assign them to instance variables. Here's a (very contrived) example, in which the object assigned
 * to <code>shared</code> is used by multiple test methods:
 * </p>
 *
 * <pre>
 * import org.scalatest.Suite
 *
 * class MySuite extends Suite {
 *
 *   // Sharing fixture objects via instance variables
 *   val shared = 5
 *
 *   def testAddition() {
 *     val sum = 2 + 3
 *     assert(sum === shared)
 *   }
 *
 *   def testSubtraction() {
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
 * <code>tearDown</code> for this purpose. In ScalaTest, you can use <code>ImpSuite</code>,
 * which will be described later, to implement an approach similar to JUnit's <code>setup</code>
 * and <code>tearDown</code>, however, this approach often involves reassigning <code>var</code>s
 * between tests. Before going that route, you should consider two approaches that
 * avoid <code>var</code>s. One approach is to write one or more "create" methods
 * that return a new instance of a needed object (or a tuple of new instances of
 * multiple objects) each time it is called. You can then call a create method at the beginning of each
 * test method that needs the fixture, storing the fixture object or objects in local variables. Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest.Suite
 * import scala.collection.mutable.ListBuffer
 *
 * class MySuite extends Suite {
 *
 *   // create objects needed by tests and return as a tuple
 *   def createFixture = (
 *     new StringBuilder("ScalaTest is "),
 *     new ListBuffer[String]
 *   )
 *
 *   def testEasy() {
 *     val (builder, lbuf) = createFixture
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(lbuf.isEmpty)
 *     lbuf += "sweet"
 *   }
 *
 *   def testFun() {
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
 * import org.scalatest.Suite
 * import scala.collection.mutable.ListBuffer
 *
 * class MySuite extends Suite {
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
 *   def testEasy() {
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
 *   def testFun() {
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
 * import org.scalatest.Suite
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MySuite extends Suite {
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
 *   def testReadingFromTheTempFile() {
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
 *   def testFirstCharOfTheTempFile() {
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
 * instead use <code>ImpSuite</code>, a subtrait of <code>Suite</code> that provides
 * methods that will be run before and after each test. <code>ImpSuite</code>'s
 * <code>beforeEach</code> method will be run before, and its <code>afterEach</code>
 * method after, each test (like JUnit's <code>setup</code>  and <code>tearDown</code>
 * methods, respectively). For example, here's how you'd write the previous
 * test that uses a temp file with an <code>ImpSuite</code>:
 * </p>
 *
 * <pre>
 * import org.scalatest.ImpSuite
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 *
 * class MySuite extends ImpSuite {
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
 *   def testReadingFromTheTempFile() {
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     assert(builder.toString === "Hello, test!")
 *   }
 *
 *   def testFirstCharOfTheTempFile() {
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
 * methods of <code>ImpSuite</code>. See the documentation for <code>ImpSuite</code> for
 * an example.
 * </p>
 *
 * <p>
 * <strong>Goodies</strong>
 * </p>
 *
 * <p>
 * In some cases you may need to pass information from a suite to its nested suites.
 * For example, perhaps a main suite needs to open a database connection that is then
 * used by all of its nested suites. You can accomplish this in ScalaTest by using
 * goodies, which are passed to <code>execute</code> as a <code>Map[String, Any]</code>.
 * This trait's <code>execute</code> method calls two other methods, both of which you
 * can override:
 * </p>
 *
 * <ul>
 * <li><code>runNestedSuites</code> - responsible for running this <code>Suite</code>'s nested <code>Suite</code>s</li>
 * <li><code>runTests</code> - responsible for running this <code>Suite</code>'s tests</li>
 * </ul>
 *
 * <p>
 * To pass goodies to nested <code>Suite</code>s, simply override <code>runNestedSuites</code>.
 * Here's an example:
 * </p>
 * 
 * <pre>
 * import org.scalatest._
 * import java.io.FileWriter
 *
 * object Constants {
 *   val GoodieKey = "fixture.FileWriter"
 * }
 *
 * class NestedSuite extends Suite {
 *
 *   override def execute(
 *     testName: Option[String],
 *     reporter: Reporter,
 *     stopper: Stopper,
 *     includes: Set[String],
 *     excludes: Set[String],
 *     goodies: Map[String, Any],
 *     distributor: Option[Distributor]
 *   ) {
 *     def complain() = fail("Hey, where's my goodie?")
 *
 *     if (goodies.contains(Constants.GoodieKey)) {
 *       goodies(Constants.GoodieKey) match {
 *         case fw: FileWriter => fw.write("hi there\n") // Use the goodie
 *         case _ => complain()
 *       }
 *     }
 *     else complain()
 *   }
 * }
 *
 * class MainSuite extends SuperSuite(List(new NestedSuite)) {
 *
 *   override def runNestedSuites(
 *     reporter: Reporter,
 *     stopper: Stopper,
 *     includes: Set[String],
 *     excludes: Set[String],
 *     goodies: Map[String, Any],
 *     distributor: Option[Distributor]
 *   ) {
 *     val writer = new FileWriter("fixture.txt")
 *     try {
 *       val myGoodies = goodies + (Constants.GoodieKey -> writer)
 *       super.runNestedSuites(reporter, stopper, includes, excludes, myGoodies, distributor)  
 *     }
 *     finally {
 *       writer.close()
 *     }
 *   }
 * }
 * </pre>
 * 
 * <p>
 * In this example, <code>MainSuite</code>'s runNestedSuites method opens a file for writing, then passes
 * the <code>FileWriter</code> to its <code>NestedSuite</code> via the goodies <code>Map</code>. The <code>NestedSuite</code>
 * grabs the <code>FileWriter</code> from the goodies <code>Map</code> and writes a friendly message to the file.
 * </p>
 * 
 * <p>
 * <strong>Test groups</strong>
 * </p>
 *
 * <p>
 * A <code>Suite</code>'s tests may be classified into named <em>groups</em>. When executing
 * a <code>Suite</code>, groups of tests can optionally be included and/or excluded. In this
 * trait's implementation, groups are indicated by annotations attached to the test method. To
 * create a group, simply define a new Java annotation. (Currently, for annotations to be
 * visible in Scala programs via Java reflection, the annotations themselves must be written in Java.) For example,
 * to create a group named <code>SlowAsMolasses</code>, to use to mark slow tests, you would
 * write in Java:
 * </p>
 *
 * <pre>
 * import java.lang.annotation.*; 
 * 
 * @Retention(RetentionPolicy.RUNTIME)
 * @Target({ElementType.METHOD, ElementType.TYPE})
 * public @interface SlowAsMolasses {}
 * </pre>
 *
 * <p>
 * Given this new annotation, you could place methods into the <code>SlowAsMolasses</code> group
 * like this:
 * </p>
 *
 * <pre>
 * @SlowAsMolasses
 * def testSleeping() = sleep(1000000)
 * </pre>
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
 * Another common use case is that tests must be &#8220;temporarily&#8221; disabled, with the
 * good intention of resurrecting the test at a later time. ScalaTest provides an <code>Ignore</code>
 * annotation for this purpose. You use it like this:
 * </p>
 *
 * <pre>
 * import org.scalatest.Suite
 * import org.scalatest.Ignore
 *
 * class MySuite extends Suite {
 *
 *   def testAddition() {
 *     val sum = 1 + 1
 *     assert(sum === 2)
 *     assert(sum + 2 === 4)
 *   }
 *
 *   @Ignore
 *   def testSubtraction() {
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
 * It will run only <code>testAddition</code> and report that <code>testSubtraction</code> was ignored. You'll see:
 * </p>
 *
 * <pre>
 * Test Starting - MySuite.testAddition
 * Test Succeeded - MySuite.testAddition
 * Test Ignored - MySuite.testSubtraction
 * </pre>
 * 
 * <p>
 * <code>Ignore</code> is implemented as a group. The <code>execute</code> method that takes no parameters
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
 * Most often the reporting done by default by <code>Suite</code>'s methods will be sufficient, but
 * occasionally you may wish to provide custom information to the <code>Reporter</code> from a test method.
 * For this purpose, you can optionally include a <code>Reporter</code> parameter in a test method, and then
 * pass the extra information to the <code>Reporter</code>'s <code>infoProvided</code> method.
 * Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest._
 * 
 * class MySuite extends Suite {
 *   def testAddition(reporter: Reporter) {
 *     assert(1 + 1 === 2)
 *     val report =
 *       new Report("MySuite.testAddition(Reporter)", "Addition seems to work")
 *     reporter.infoProvided(report)
 *   }
 * }
 * </pre>
 *
 * If you run this <code>Suite</code> from the interpreter, you will see the message
 * included in the printed report:
 *
 * <pre>
 * scala> (new MySuite).execute()
 * Test Starting - MySuite.testAddition(Reporter)
 * Info Provided - MySuite.testAddition(Reporter): Addition seems to work
 * Test Succeeded - MySuite.testAddition(Reporter)
 * </pre>
 *
 * <p>
 * <strong>Executing suites concurrently</strong>
 * </p>
 *
 * <p>
 * The primary <code>execute</code> method takes as its last parameter an optional <code>Distributor</code>. If 
 * a <code>Distributor</code> is passed in, this trait's implementation of <code>execute</code> puts its nested
 * <code>Suite</code>s into the distributor rather than executing them directly. The caller of <code>execute</code>
 * is responsible for ensuring that some entity executes the <code>Suite</code>s placed into the 
 * distributor. The <code>-c</code> command line parameter to <code>Runner</code>, for example, will cause
 * <code>Suite</code>s put into the <code>Distributor</code> to be executed concurrently via a pool of threads.
 * </p>
 *
 * <p>
 * <strong>Extensibility</strong>
 * </p>
 *
 * <p>
 * Trait <code>Suite</code> provides default implementations of its methods that should
 * be sufficient for most applications, but many methods can be overridden when desired. Here's
 * a summary of the methods that are intended to be overridden:
 * </p>
 *
 * <ul>
 * <li><code>execute</code> - override this method to define custom ways to executes suites of
 *   tests.</li>
 * <li><code>runTest</code> - override this method to define custom ways to execute a single named test.</li>
 * <li><code>testNames</code> - override this method to specify the <code>Suite</code>'s test names in a custom way.</li>
 * <li><code>groups</code> - override this method to specify the <code>Suite</code>'s test groups in a custom way.</li>
 * <li><code>nestedSuites</code> - override this method to specify the <code>Suite</code>'s nested <code>Suite</code>s in a custom way.</li>
 * <li><code>suiteName</code> - override this method to specify the <code>Suite</code>'s name in a custom way.</li>
 * <li><code>expectedTestCount</code> - override this method to count this <code>Suite</code>'s expected tests in a custom way.</li>
 * </ul>
 *
 * <p>
 * For example, this trait's implementation of <code>testNames</code> performs reflection to discover methods starting with <code>test</code>,
 * and places these in a <code>Set</code> whose iterator returns the names in alphabetical order. If you wish to run tests in a different
 * order in a particular <code>Suite</code>, perhaps because a test named <code>testAlpha</code> can only succeed after a test named
 * <code>testBeta</code> has run, you can override <code>testNames</code> so that it returns a <code>Set</code> whose iterator returns
 * <code>testBeta</code> <em>before</em> <code>testAlpha</code>. (This trait's implementation of <code>execute</code> will invoke tests
 * in the order they come out of the <code>testNames</code> <code>Set</code> iterator.)
 * </p>
 *
 * <p>
 * Alternatively, you may not like starting your test methods with <code>test</code>, and prefer using <code>@Test</code> annotations in
 * the style of Java's JUnit 4 or TestNG. If so, you can override <code>testNames</code> to discover tests using either of these two APIs
 * <code>@Test</code> annotations, or one of your own invention. (This is in fact
 * how <code>org.scalatest.junit.JUnit4Suite</code> and <code>org.scalatest.testng.TestNGSuite</code> work.)
 * </p>
 *
 * <p>
 * Moreover, <em>test</em> in ScalaTest does not necessarily mean <em>test method</em>. A test can be anything that can be given a name,
 * that starts and either succeeds or fails, and can be ignored. In <code>org.scalatest.FunSuite</code>, for example, tests are represented
 * as function values. This
 * approach might look foreign to JUnit users, but may feel more natural to programmers with a functional programming background.
 * To facilitate this style of writing tests, <code>FunSuite</code> overrides <code>testNames</code>, <code>runTest</code>, and <code>execute</code> such that you can 
 * define tests as function values.
 * </p>
 *
 * <p>
 * You can also model existing JUnit 3, JUnit 4, or TestNG tests as suites of tests, thereby incorporating Java tests into a ScalaTest suite.
 * The "wrapper" classes in packages <code>org.scalatest.junit</code> and <code>org.scalatest.testng</code> exist to make this easy. The point here, however, is that
 * no matter what legacy tests you may have, it is likely you can create or use an existing <code>Suite</code> subclass that allows you to model those tests
 * as ScalaTest suites and tests and incorporate them into a ScalaTest suite. You can then write new tests in Scala and continue supporting
 * older tests in Java.
 * </p>
 *
 * @author Bill Venners
 */
@serializable
trait Suite {

  private val TestMethodPrefix = "test"
  private val ReporterInParens = "(Reporter)"
  private val IgnoreAnnotation = "org.scalatest.Ignore"

    /*
* @param nestedSuites A <CODE>List</CODE> of <CODE>Suite</CODE>
* objects. The specified <code>List</code> must be non-empty. Each element must be non-<code>null</code> and an instance
* of <CODE>org.scalatest.Suite</CODE>.
*
* @throws NullPointerException if <CODE>nestedSuites</CODE>
* is <CODE>null</CODE> or any element of <CODE>nestedSuites</CODE>
* set is <CODE>null</CODE>.
*/
  
  // should nestedSuites return a Set[String] instead?
  /**
  * A <code>List</code> of this <code>Suite</code> object's nested <code>Suite</code>s. If this <code>Suite</code> contains no nested <code>Suite</code>s,
  * this method returns an empty <code>List</code>. This trait's implementation of this method returns an empty <code>List</code>.
  */
  def nestedSuites: List[Suite] = Nil
  
  /**
   * Executes this <code>Suite</code>, printing results to the standard output. This method
   * implementation calls on this <code>Suite</code> the <code>execute</code> method that takes
   * seven parameters, passing in:
   *
   * <ul>
   * <li><code>testName</code> - <code>None</code></li>
   * <li><code>reporter</code> - a reporter that prints to the standard output</li>
   * <li><code>stopper</code> - a <code>Stopper</code> whose <code>stopRequested</code> method always returns <code>false</code></li>
   * <li><code>includes</code> - an empty <code>Set[String]</code></li>
   * <li><code>excludes</code> - an <code>Set[String]</code> that contains only one element, <code>"org.scalatest.Ignore"</code></li>
   * <li><code>goodies</code> - an empty <code>Map[String, Any]</code></li>
   * <li><code>distributor</code> - <code>None</code></li>
   * </ul>
   *
   * <p>
   * This method serves as a convenient way to execute a <code>Suite</code>, especially from within the Scala interpreter.
   * </p>
   */
  final def execute() {
    execute(None, new StandardOutReporter, new Stopper {}, Set(), Set(IgnoreAnnotation), Map(), None)
  }

  /**
   * Executes the test specified <code>testName</code> in this <code>Suite</code>, printing results to the standard output. This method
   * implementation calls on this <code>Suite</code> the <code>execute</code> method that takes
   * seven parameters, passing in:
   *
   * <ul>
   * <li><code>testName</code> - <code>Some(testName)</code></li>
   * <li><code>reporter</code> - a reporter that prints to the standard output</li>
   * <li><code>stopper</code> - a <code>Stopper</code> whose <code>stopRequested</code> method always returns <code>false</code></li>
   * <li><code>includes</code> - an empty <code>Set[String]</code></li>
   * <li><code>excludes</code> - an empty <code>Set[String]</code></li>
   * <li><code>goodies</code> - an empty <code>Map[String, Any]</code></li>
   * <li><code>distributor</code> - <code>None</code></li>
   * </ul>
   *
   * <p>
   * This method serves as a convenient way to execute a single test, especially from within the Scala interpreter.
   * </p>
   */
  final def execute(testName: String) {
    execute(Some(testName), new StandardOutReporter, new Stopper {}, Set(), Set(), Map(), None)
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> group names to which tests in this <code>Suite</code> belong, and values
   * the <code>Set</code> of test names that belong to each group.  If this <code>Suite</code> contains no groups, this method returns an empty <code>Map</code>.
   *
   * <p>
   * This trait's implementation uses Java reflection to discover any Java annotations attached to its test methods. Each unique
   * annotation name is considered a group. This trait's implementation, therefore, places one key/value pair into to the
   * <code>Map</code> for each unique annotation name discovered through reflection. The value for each group name key will contain
   * the test method name, as provided via the <code>testNames</code> method. 
   * </p>
   *
   * <p>
   * Subclasses may override this method to define and/or discover groups in a custom manner, but overriding method implementations
   * should never return an empty <code>Set</code> as a value. If a group has no tests, its name should not appear as a key in the
   * returned <code>Map</code>.
   * </p>
   */
  def groups: Map[String, Set[String]] = {

    def getGroups(testName: String) =
      for (a <- getMethodForTestName(testName).getDeclaredAnnotations)
        yield a.annotationType.getName

    val elements =
      for (testName <- testNames; if !getGroups(testName).isEmpty)
        yield testName -> (Set() ++ getGroups(testName))

    Map() ++ elements
  }

  /**
  * An immutable <code>Set</code> of test names. If this <code>Suite</code> contains no tests, this method returns an empty <code>Set</code>.
  *
  * <p>
  * This trait's implementation of this method uses Java reflection to discover all public methods whose name starts with <code>"test"</code>,
  * which take either nothing or a single <code>Reporter</code> as parameters. For each discovered test method, it assigns a test name
  * comprised of just the method name if the method takes no parameters, or the method name plus <code>(Reporter)</code> if the
  * method takes a <code>Reporter</code>. Here are a few method signatures and the names that this trait's implementation assigns them:
  * </p>
  *
  * <pre>
  * def testCat() {}         // test name: "testCat"
  * def testCat(Reporter) {} // test name: "testCat(Reporter)"
  * def testDog() {}         // test name: "testDog"
  * def testDog(Reporter) {} // test name: "testDog(Reporter)"
  * def test() {}            // test name: "test"
  * def test(Reporter) {}    // test name: "test(Reporter)"
  * </pre>
  *
  * <p>
  * This trait's implementation of this method returns an immutable <code>Set</code> of all such names, excluding the name
  * <code>testName</code>. The iterator obtained by invoking <code>elements</code> on this
  * returned <code>Set</code> will produce the test names in their <em>natural order</em>, as determined by <code>String</code>'s
  * <code>compareTo</code> method.
  * </p>
  *
  * <p>
  * This trait's implementation of <code>runTests</code> invokes this method
  * and calls <code>runTest</code> for each test name in the order they appear in the returned <code>Set</code>'s iterator.
  * Although this trait's implementation of this method returns a <code>Set</code> whose iterator produces <code>String</code>
  * test names in a well-defined order, the contract of this method does not required a defined order. Subclasses are free to
  * override this method and return test names in an undefined order, or in a defined order that's different from <code>String</code>'s
  * natural order.
  * </p>
  *
  * <p>
  * Subclasses may override this method to produce test names in a custom manner. One potential reason to override <code>testNames</code> is
  * to execute tests in a different order, for example, to ensure that tests that depend on other tests are run after those other tests.
  * Another potential reason to override is to discover test methods annotated with JUnit 4 or TestNG <code>@Test</code> annotations. Or
  * a subclass could override this method and return a static, hard-coded <code>Set</code> of tests, etc.
  * </p>
  */
  def testNames: Set[String] = {

    def takesReporter(m: Method) = {
      val paramTypes = m.getParameterTypes
      paramTypes.length == 1 && classOf[Reporter].isAssignableFrom(paramTypes(0))
    }

    def isTestMethod(m: Method) = {

      val isInstanceMethod = !Modifier.isStatic(m.getModifiers())

      // name must have at least 4 chars (minimum is "test")
      val simpleName = m.getName
      val firstFour = if (simpleName.length >= 4) simpleName.substring(0, 4) else "" 

      val paramTypes = m.getParameterTypes
      val hasNoParams = paramTypes.length == 0

      val isTestNames = simpleName == "testNames"

      isInstanceMethod && (firstFour == "test") && ((hasNoParams && !isTestNames) || takesReporter(m))
    }

    val testNameArray =
      for (m <- getClass.getMethods; if isTestMethod(m)) 
        yield if (takesReporter(m)) m.getName + ReporterInParens else m.getName

    TreeSet[String]() ++ testNameArray
  }

  private def simpleNameForTest(testName: String) = 
    if (testName.endsWith(ReporterInParens))
      testName.substring(0, testName.length - ReporterInParens.length)
    else
      testName

  private def testMethodTakesReporter(testName: String) = testName.endsWith(ReporterInParens)

  private def getMethodForTestName(testName: String) = getClass.getMethod(simpleNameForTest(testName),
                                                         if (testMethodTakesReporter(testName)) Array(classOf[Reporter]) else Array())

  /**
   * Run a test. This trait's implementation uses Java reflection to invoke on this object the test method identified by the passed <code>testName</code>.
   *
   * @param testName the name of one test to execute.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param goodies a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, or <code>goodies</code>
   *     is <code>null</code>.
   */
  protected def runTest(testName: String, reporter: Reporter, stopper: Stopper, goodies: Map[String, Any]) {

    if (testName == null || reporter == null || stopper == null || goodies == null)
      throw new NullPointerException

    val wrappedReporter = wrapReporterIfNecessary(reporter)
    val method = getMethodForTestName(testName)

    // Create a Rerunnable if the Suite has a no-arg constructor
    val hasPublicNoArgConstructor = Suite.checkForPublicNoArgConstructor(getClass)

    val rerunnable =
      if (hasPublicNoArgConstructor)
        Some(new TestRerunner(getClass.getName, testName))
      else
        None
     
    val report =
      if (hasPublicNoArgConstructor)
        new Report(getTestNameForReport(testName), "", None, rerunnable)
      else
        new Report(getTestNameForReport(testName), "")

    wrappedReporter.testStarting(report)

    val args: Array[Object] = if (testMethodTakesReporter(testName)) Array(reporter) else Array()

    try {
      method.invoke(this, args)

      val report =
        if (hasPublicNoArgConstructor)
          new Report(getTestNameForReport(testName), "", None, rerunnable)
        else 
          new Report(getTestNameForReport(testName), "")

      wrappedReporter.testSucceeded(report)
    }
    catch { 
      case ite: InvocationTargetException => {
        val t = ite.getTargetException
        handleFailedTest(t, hasPublicNoArgConstructor, testName, rerunnable, wrappedReporter)
      }
      case e: Exception => {
        handleFailedTest(e, hasPublicNoArgConstructor, testName, rerunnable, wrappedReporter)
      }
      case ae: AssertionError => {
        handleFailedTest(ae, hasPublicNoArgConstructor, testName, rerunnable, wrappedReporter)
      }
    }
  }

  /**
   * <p>
   * Run zero to many of this <code>Suite</code>'s tests.
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
   * <li><code>goodies</code> - the <code>goodies</code> <code>Map</code> passed to this method, or one that wraps and delegates to it</li>
   * </ul>
   *
   * <p>
   * This method takes a <code>Set</code> of group names that should be included (<code>includes</code>), and a <code>Set</code>
   * that should be excluded (<code>excludes</code>), when deciding which of this <code>Suite</code>'s tests to execute.
   * If <code>includes</code> is empty, all tests will be executed
   * except those those belonging to groups listed in the <code>excludes</code> <code>Set</code>. If <code>includes</code> is non-empty, only tests
   * belonging to groups mentioned in <code>includes</code>, and not mentioned in <code>excludes</code>
   * will be executed. However, if <code>testName</code> is <code>Some</code>, <code>includes</code> and <code>excludes</code> are essentially ignored.
   * Only if <code>testName</code> is <code>None</code> will <code>includes</code> and <code>excludes</code> be consulted to
   * determine which of the tests named in the <code>testNames</code> <code>Set</code> should be run. This trait's implementation
   * behaves this way, and it is part of the general contract of this method, so all overridden forms of this method should behave
   * this way as well.  For more information on trait groups, see the main documentation for this trait.
   * </p>
   *
   * <p>
   * If <code>testName</code> is <code>None</code>, this trait's implementation of this method
   * invokes <code>testNames</code> on this <code>Suite</code> to get a <code>Set</code> of names of tests to potentially execute.
   * (A <code>testNames</code> value of <code>None</code> essentially acts as a wildcard that means all tests in
   * this <code>Suite</code> that are selected by <code>includes</code> and <code>excludes</code> should be executed.)
   * For each test in the <code>testName</code> <code>Set</code>, in the order
   * they appear in the iterator obtained by invoking the <code>elements</code> method on the <code>Set</code>, this trait's implementation
   * of this method checks whether the test should be run based on the <code>includes</code> and <code>excludes</code> <code>Set</code>s.
   * If so, this implementation invokes <code>runTest</code>, passing in:
   * </p>
   *
   * <ul>
   * <li><code>testName</code> - the <code>String</code> name of the test to run (which will be one of the names in the <code>testNames</code> <code>Set</code>)</li>
   * <li><code>reporter</code> - the <code>Reporter</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>stopper</code> - the <code>Stopper</code> passed to this method, or one that wraps and delegates to it</li>
   * <li><code>goodies</code> - the <code>goodies</code> <code>Map</code> passed to this method, or one that wraps and delegates to it</li>
   * </ul>
   *
   * @param testName an optional name of one test to execute. If <code>None</code>, all relevant tests should be executed.
   *                 I.e., <code>None</code> acts like a wildcard that means execute all relevant tests in this <code>Suite</code>.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param includes a <code>Set</code> of <code>String</code> test names to include in the execution of this <code>Suite</code>
   * @param excludes a <code>Set</code> of <code>String</code> test names to exclude in the execution of this <code>Suite</code>
   * @param goodies a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, <code>includes</code>,
   *     <code>excludes</code>, or <code>goodies</code> is <code>null</code>.
   *
   * This trait's implementation of this method executes tests
   * in the manner described in detail in the following paragraphs, but subclasses may override the method to provide different
   * behavior. The most common reason to override this method is to set up and, if also necessary, to clean up a test fixture
   * used by all the methods of this <code>Suite</code>.
   */
  protected def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
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
        for (tn <- testNames) {
          if (!stopper.stopRequested && (includes.isEmpty || !(includes ** groups.getOrElse(tn, Set())).isEmpty)) {
            if (excludes.contains(IgnoreAnnotation) && groups.getOrElse(tn, Set()).contains(IgnoreAnnotation)) {
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

  /**
   * Execute this <code>Suite</code>.
   *
   * <p>If <code>testName</code> is <code>None</code>, this trait's implementation of this method
   * calls these two methods on this object in this order:</p>
   *
   * <ol>
   * <li><code>runNestedSuites(wrappedReporter, stopper, includes, excludes, goodies, distributor)</code></li>
   * <li><code>runTests(testName, wrappedReporter, stopper, includes, excludes, goodies)</code></li>
   * </ol>
   *
   * <p>
   * If <code>testName</code> is <code>Some</code>, then this trait's implementation of this method
   * calls <code>runTests</code>, but does not call <code>runNestedSuites</code>.
   * </p>
   *
   * @param testName an optional name of one test to execute. If <code>None</code>, all relevant tests should be executed.
   *                 I.e., <code>None</code> acts like a wildcard that means execute all relevant tests in this <code>Suite</code>.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param includes a <code>Set</code> of <code>String</code> test names to include in the execution of this <code>Suite</code>
   * @param excludes a <code>Set</code> of <code>String</code> test names to exclude in the execution of this <code>Suite</code>
   * @param goodies a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @param distributor an optional <code>Distributor</code>, into which to put nested <code>Suite</code>s to be executed
   *              by another entity, such as concurrently by a pool of threads. If <code>None</code>, nested <code>Suite</code>s will be executed sequentially.
   *         
   *
   * @throws NullPointerException if any passed parameter is <code>null</code>.
   */
  def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
              goodies: Map[String, Any], distributor: Option[Distributor]) {

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
    if (distributor == null)
      throw new NullPointerException("distributor was null")

    val wrappedReporter = wrapReporterIfNecessary(reporter)

    testName match {
      case None => runNestedSuites(wrappedReporter, stopper, includes, excludes, goodies, distributor)
      case Some(_) =>
    }
    runTests(testName, wrappedReporter, stopper, includes, excludes, goodies)

    if (stopper.stopRequested) {
      val rawString = Resources("executeStopping")
      wrappedReporter.infoProvided(new Report(suiteName, rawString))
    }
  }

  /* [bv: This is a good example of that common refactor for initialization]
        Report report;
        String msg = t.getMessage();
        if (msg == null) {
            msg = t.toString();
        }
        if (hasPublicNoArgConstructor) {

            report = new Report(getTestNameForReport(userFriendlyMethodName), msg, t, rerunnable);
        }
        else {

            report = new Report(getTestNameForReport(userFriendlyMethodName), msg, t);
        }
        myReporter.testFailed(report);
*/
  private def handleFailedTest(t: Throwable, hasPublicNoArgConstructor: Boolean, testName: String,
      rerunnable: Option[Rerunnable], reporter: Reporter) {

    val msg =
      if (t.getMessage != null) // [bv: this could be factored out into a helper method]
        t.getMessage
      else
        t.toString

    val report =
      if (hasPublicNoArgConstructor)
        new Report(getTestNameForReport(testName), msg, Some(t), rerunnable)
      else
        new Report(getTestNameForReport(testName), msg, Some(t), None)

    reporter.testFailed(report)
  }

  /**
   * <p>
   * Execute zero to many of this <code>Suite</code>'s nested <code>Suite</code>s.
   * </p>
   *
   * <p>
   * If the passed <code>distributor</code> is <code>None</code>, this trait's
   * implementation of this method invokes <code>execute</code> on each
   * nested <code>Suite</code> in the <code>List</code> obtained by invoking <code>nestedSuites</code>.
   * If a nested <code>Suite</code>'s <code>execute</code>
   * method completes abruptly with an exception, this trait's implementation of this
   * method reports that the <code>Suite</code> aborted and attempts to execute the
   * next nested <code>Suite</code>.
   * If the passed <code>distributor</code> is <code>Some</code>, this trait's implementation
   * puts each nested <code>Suite</code> 
   * into the <code>Distributor</code> contained in the <code>Some</code>, in the order in which the
   * <code>Suite</code>s appear in the <code>List</code> returned by <code>nestedSuites</code>.
   * </p>
   *
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @throws NullPointerException if <CODE>reporter</CODE> is <CODE>null</CODE>.
   */
  protected def runNestedSuites(reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
                                    goodies: Map[String, Any], distributor: Option[Distributor]) {

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
    if (distributor == null)
      throw new NullPointerException("distributor was null")

    val wrappedReporter = wrapReporterIfNecessary(reporter)

    def callExecuteOnSuite(nestedSuite: Suite) {

      if (!stopper.stopRequested) {

        // Create a Rerunnable if the Suite has a no-arg constructor 
        val hasPublicNoArgConstructor = Suite.checkForPublicNoArgConstructor(nestedSuite.getClass)

        val rerunnable =
          if (hasPublicNoArgConstructor)
            Some(new SuiteRerunner(nestedSuite.getClass.getName))
          else
            None

        val rawString = Resources("suiteExecutionStarting")

        val report =
          if (hasPublicNoArgConstructor) 
            new Report(nestedSuite.suiteName, rawString, None, rerunnable)
          else 
            new Report(nestedSuite.suiteName, rawString)
        
        wrappedReporter.suiteStarting(report)

        try {
          nestedSuite.execute(None, wrappedReporter, stopper, includes, excludes, goodies, distributor)

          val rawString = Resources("suiteCompletedNormally")

          val report =
            if (hasPublicNoArgConstructor)
              new Report(nestedSuite.suiteName, rawString, None, rerunnable)
            else
              new Report(nestedSuite.suiteName, rawString)

          wrappedReporter.suiteCompleted(report)
        }
        catch {       
          case e: RuntimeException => {

            val rawString = Resources("executeException")

            val report =
              if (hasPublicNoArgConstructor)
                new Report(nestedSuite.suiteName, rawString, Some(e), rerunnable)
              else
                new Report(nestedSuite.suiteName, rawString, Some(e), None)

            wrappedReporter.suiteAborted(report)

            // Don't continue onto the next Suite here, because first must
            // attempt to invoke tearDownSuite.
          }
        }
      }
    }

    distributor match {
      case None => nestedSuites.foreach(callExecuteOnSuite)
      case Some(d) => nestedSuites.foreach(d.put)
    }
  }

  /**
   * A user-friendly suite name for this <code>Suite</code>. This trait's
   * implementation of this method returns the simple name of this object's class. This
   * trait's implementation of <code>runNestedSuites</code> calls this method to obtain a
   * name for <code>Report</code>s to pass to the <code>suiteStarting</code>, <code>suiteCompleted</code>,
   * and <code>suiteAborted</code> methods of the <code>Reporter</code>.
   *
   * @return this <code>Suite</code> object's suite name.
   */
  def suiteName = getSimpleNameOfThisObjectsClass

  /**
   * <p>
   * Get a user-friendly test name for one of this <code>Suite</code>'s test methods, whose
   * <code>String</code> name is passed as <code>testName</code>. This trait's
   * implementation of this method returns the concatenation of the simple name of this object's
   * class, a dot, and the specified <code>String</code> <code>testMethodName</code>. This
   * class's implementation of <code>runTests</code> calls this method to obtain a
   * name for <code>Report</code>s to pass to the <code>testStarting</code>, <code>testSucceeded</code>,
   * and <code>testFailed</code> methods of the <code>Reporter</code>.
   * </p>
   *
   * @param testName the name of the test
   * @throws NullPointerException if <code>testMethodName</code> is <code>null</code>
   * @return a test name for a test of this <code>Suite</code>.
   */
  private[scalatest] def getTestNameForReport(testName: String) = {

    if (testName == null)
      throw new NullPointerException("testName was null")

    suiteName + "." + testName
  }

  private def getSimpleNameOfThisObjectsClass = stripDollars(parseSimpleName(getClass().getName()))

  /**
   * The total number of tests that are expected to run when this <code>Suite</code>'s <code>execute</code> method is invoked.
   * This trait's implementation of this method returns the sum of:
   *
   * <ul>
   * <li>the size of the <code>testNames</code> <code>List</code>
   * <li>the sum of the values obtained by invoking
   *     <code>expecteTestCount</code> on every nested <code>Suite</code> contained in
   *     <code>nestedSuites</code>
   * </ul>
   */
  def expectedTestCount(includes: Set[String], excludes: Set[String]): Int = {

    // [bv: here was another tricky refactor. How to increment a counter in a loop]
    def countNestedSuiteTests(nestedSuites: List[Suite], includes: Set[String], excludes: Set[String]): Int =
      nestedSuites match {
        case List() => 0
        case nestedSuite :: nestedSuites => nestedSuite.expectedTestCount(includes, excludes) +
            countNestedSuiteTests(nestedSuites, includes, excludes)
    }
    // Semicolon inference bit me here for the first time. I had said:
    //  case nestedSuite :: nestedSuites => nestedSuite.expectedTestCount(includes, excludes)
    //      + countNestedSuiteTests(nestedSuites, includes, excludes)
    // That won't work. It thinks + starts a new expression
 
    expectedTestCountThisSuiteOnly(includes, excludes) + countNestedSuiteTests(nestedSuites, includes, excludes)
  }

  private def expectedTestCountThisSuiteOnly(includes: Set[String], excludes: Set[String]) = {
    val tns =
      for (tn <- testNames; if (includes.isEmpty || !(includes ** groups.getOrElse(tn, Set())).isEmpty)
         && ((excludes ** groups.getOrElse(tn, Set())).isEmpty) && (!(groups.getOrElse(tn, Set()).contains(IgnoreAnnotation))))
        yield tn

    tns.size
  }

  /**
   * Throws <code>AssertionError</code> to indicate a test failed.
   */
  def fail() = throw new AssertionError

  /**
   * Throws <code>AssertionError</code>, with the passed
   * <code>String</code> <code>message</code> as the exception's detail
   * message, to indicate a test failed.
   *
   * @param message A message describing the failure.
   * @throws NullPointerException if <code>message</code> is <code>null</code>
   */
  def fail(message: String) = {

    if (message == null)
        throw new NullPointerException("message is null")
     
    throw new AssertionError(message)
  }

  /**
   * Throws <code>AssertionError</code>, with the passed
   * <code>String</code> <code>message</code> as the exception's detail
   * message and <code>Throwable</code> cause, to indicate a test failed.
   *
   * @param message A message describing the failure.
   * @param cause A <code>Throwable</code> that indicates the cause of the failure.
   * @throws NullPointerException if <code>message</code> or <code>cause</code> is <code>null</code>
   */
  def fail(message: String, cause: Throwable) = {

    if (message == null)
      throw new NullPointerException("message is null")

    if (cause == null)
      throw new NullPointerException("cause is null")

    val ae = new AssertionError(message)
    ae.initCause(cause)
    throw ae
  }

  /**
   * Throws <code>AssertionError</code>, with the passed
   * <code>Throwable</code> cause, to indicate a test failed.
   * The <code>getMessage</code> method of the thrown <code>AssertionError</code>
   * will return <code>cause.toString()</code>.
   *
   * @param cause a <code>Throwable</code> that indicates the cause of the failure.
   * @throws NullPointerException if <code>cause</code> is <code>null</code>
   */
  def fail(cause: Throwable) = {

    if (cause == null)
      throw new NullPointerException("cause is null")
        
    throw new AssertionError(cause)
  }

  /**
   * Class used via an implicit conversion to enable any two objects to be compared with
   * <code>===</code> in assertions in tests. For example:
   *
   * <pre>
   * assert(a === b)
   * </pre>
   *
   * <p>
   * The benefit of using <code>assert(a === b)</code> rather than <code>assert(a == b)</code> is
   * that an <code>AssertionError</code> produced by the former will include the values of <code>a</code> and <code>b</code>
   * in its detail message.
   * The implicit method that performs the conversion from <code>Any</code> to <code>Equalizer</code> is
   * <code>convertToEqualizer</code> in trait <code>Suite</code>.
   * </p>
   *
   * <p>
   * In case you're not familiar with how implicit conversions work in Scala, here's a quick explanation.
   * The <code>convertToEqualizer</code> method in <code>Suite</code> is defined as an "implicit" method that takes an
   * <code>Any</code>, which means you can pass in any object, and it will convert it to an <code>Equalizer</code>.
   * The <code>Equalizer</code> has <code>===</code> defined. Most objects don't have <code>===</code> defined as a method
   * on them. Take two Strings, for example:
   * </p>
   *
   * <pre>
   * assert("hello" === "world")
   * </pre>
   *
   * <p>
   * Given this code, the Scala compiler looks for an <code>===</code> method on class <code>String</code>, because that's the class of
   * <code>"hello"</code>. <code>String</code> doesn't define <code>===</code>, so the compiler looks for an implicit conversion from
   * <code>String</code> to something that does have an <code>===</code> method, and it finds the <code>convertToEqualizer</code> method. It
   * then rewrites the code to this:
   * </p>
   *
   * <pre>
   * assert(convertToEqualizer("hello").===("world"))
   * </pre>
   *
   * <p>
   * So inside a <code>Suite</code>, <code>===</code> will work on anything. The only situation in which the implicit conversion wouldn't 
   * happen is on types that have an <code>===</code> method already defined.
   * </p>
   * 
   * <p>
   * The primary constructor takes one object whose type is being converted to <code>Equalizer</code>.
   * </p>
   *
   * @param left An object to convert to <code>Equalizer</code>, which represents the <code>left</code> value
   *     of an assertion.
   * @throws NullPointerException if <code>left</code> is <code>null</code>
   */
  class Equalizer(left: Any) {

    if (left == null)
      throw new NullPointerException

    /**
     * The <code>===</code> operation compares this <code>Equalizer</code>'s <code>left</code> value (passed
     * to the constructor, usually via an implicit conversion) with the passed <code>right</code> value 
     * for equality as determined by the expression <code>left == right</code>.
     * If <code>true</code>, <code>===</code> returns <code>None</code>. Else, <code>===</code> returns
     * a <code>Some</code> whose <code>String</code> value indicates the <code>left</code> and <code>right</code> values.
     *
     * <p>
     * In its typical usage, the <code>Option[String]</code> returned by <code>===</code> will be passed to one of two
     * of trait <code>Suite</code>'s overloaded <code>assert</code> methods. If <code>None</code>,
     * which indicates the assertion succeeded, <code>assert</code> will return normally. But if <code>Some</code> is passed,
     * which indicates the assertion failed, <code>assert</code> will throw an <code>AssertionError</code> whose detail
     * message will include the <code>String</code> contained inside the <code>Some</code>, which in turn includes the
     * <code>left</code> and <code>right</code> values. This <code>AssertionError</code> is typically embedded in a 
     * <code>Report</code> and passed to a <code>Reporter</code>, which can present the <code>left</code> and <code>right</code>
     * values to the user.
     * </p>
     */
    def ===(right: Any) =
      if (left == right)
        None
      else {
        val (leftee, rightee) = Suite.getObjectsForFailureMessage(left, right)
        Some(Resources("didNotEqual", Suite.decoratedToStringValue(leftee), Suite.decoratedToStringValue(rightee)))
      }
  }

  /**
   * Assert that a boolean condition is true.
   * If the condition is <code>true</code>, this method returns normally.
   * Else, it throws <code>AssertionError</code>.
   *
   * @param condition the boolean condition to assert
   * @throws AssertionError if the condition is <code>false</code>.
   */
  def assert(condition: Boolean) {
    Predef.assert(condition)
  }

  /**
   * Assert that a boolean condition, described in <code>String</code>
   * <code>message</code>, is true.
   * If the condition is <code>true</code>, this method returns normally.
   * Else, it throws <code>AssertionError</code> with the
   * <code>String</code> obtained by invoking <code>toString</code> on the
   * specified <code>message</code> as the exception's detail message.
   *
   * @param condition the boolean condition to assert
   * @param message An objects whose <code>toString</code> method returns a message to include in a failure report.
   * @throws AssertionError if the condition is <code>false</code>.
   * @throws NullPointerException if <code>message</code> is <code>null</code>.
   */
  def assert(condition: Boolean, message: Any) {
    Predef.assert(condition, message)
  }

  /**
   * Assert that an <code>Option[String]</code> is <code>None</code>. 
   * If the condition is <code>None</code>, this method returns normally.
   * Else, it throws <code>AssertionError</code> with the <code>String</code>
   * value of the <code>Some</code>, as well as the 
   * <code>String</code> obtained by invoking <code>toString</code> on the
   * specified <code>message</code>,
   * included in the <code>AssertionError</code>'s detail message.
   *
   * <p>
   * This form of <code>assert</code> is usually called in conjunction with an
   * implicit conversion to <code>Equalizer</code>, using a <code>===</code> comparison, as in:
   * </p>
   *
   * <pre>
   * assert(a === b, "extra info reported if assertion fails")
   * </pre>
   *
   * <p>
   * For more information on how this mechanism works, see the <a href="Suite.Equalizer.html">documentation for
   * <code>Equalizer</code></a>.
   * </p>
   *
   * @param o the <code>Option[String]</code> to assert
   * @param message An objects whose <code>toString</code> method returns a message to include in a failure report.
   * @throws AssertionError if the <code>Option[String]</code> is <code>Some</code>.
   * @throws NullPointerException if <code>message</code> is <code>null</code>.
   */
  def assert(o: Option[String], message: Any) {
    o match {
      case Some(s) => throw new AssertionError(message + "\n" + s)
      case None => ()
    }
  }
  
  /**
   * Assert that an <code>Option[String]</code> is <code>None</code>.
   * If the condition is <code>None</code>, this method returns normally.
   * Else, it throws <code>AssertionError</code> with the <code>String</code>
   * value of the <code>Some</code> included in the <code>AssertionError</code>'s
   * detail message.
   *
   * <p>
   * This form of <code>assert</code> is usually called in conjunction with an
   * implicit conversion to <code>Equalizer</code>, using a <code>===</code> comparison, as in:
   * </p>
   *
   * <pre>
   * assert(a === b)
   * </pre>
   *
   * <p>
   * For more information on how this mechanism works, see the <a href="Suite.Equalizer.html">documentation for
   * <code>Equalizer</code></a>.
   * </p>
   *
   * @param o the <code>Option[String]</code> to assert
   * @throws AssertionError if the <code>Option[String]</code> is <code>Some</code>.
   */
  def assert(o: Option[String]) {
    assert(o, "")
  }

  /**
   * Implicit conversion from <code>Any</code> to <code>Equalizer</code>, used to enable
   * assertions with <code>===</code> comparisons. For more information
   * on this mechanism, see the <a href="Suite.Equalizer.html">documentation for </code>Equalizer</code></a>.
   *
   * @param left the object whose type to convert to <code>Equalizer</code>.
   * @throws NullPointerException if <code>left</code> is <code>null</code>.
   */
  implicit def convertToEqualizer(left: Any) = new Equalizer(left)

  /**
   * Intercept and return an instance of the passed exception class (or an instance of a subclass of the
   * passed class), which is expected to be thrown by the passed function value. This method invokes the passed
   * function. If it throws an exception that's an instance of the passed class or one of its
   * subclasses, this method returns that exception. Else, whether the passed function returns normally
   * or completes abruptly with a different exception, this method throws <code>AssertionError</code>
   * whose detail message includes the <code>String</code> obtained by invoking <code>toString</code> on the passed <code>message</code>.
   *
   * <p>
   * Note that the passed <code>Class</code> may represent any type, not just <code>Throwable</code> or one of its subclasses. In
   * Scala, exceptions can be caught based on traits they implement, so it may at times make sense to pass in a class instance for
   * a trait. If a class instance is passed for a type that could not possibly be used to catch an exception (such as <code>String</code>,
   * for example), this method will complete abruptly with an <code>AssertionError</code>.
   * </p>
   *
   * @param clazz a type to which the expected exception class is assignable, i.e., the exception should be an instance of the type represented by <code>clazz</code>.
   * @param message An objects whose <code>toString</code> method returns a message to include in a failure report.
   * @param f the function value that should throw the expected exception
   * @return the intercepted exception, if 
   * @throws AssertionError if the passed function does not result in a value equal to the
   *     passed <code>expected</code> value.
   */
  def intercept(clazz: java.lang.Class[_ <: AnyRef], message: Any)(f: => Unit): Throwable = {
    val caught = try {
      f
      None
    }
    catch {
      case u: Throwable => {
        if (!clazz.isAssignableFrom(u.getClass)) {
          val s = Resources("wrongException", clazz.getName, u.getClass.getName)
          val ae = new AssertionError(message + "\n" + s)
          ae.initCause(u)
          throw ae
        }
        else {
          Some(u)
        }
      }
    }
    caught match {
      case None => fail(message + "\n" + Resources("exceptionExpected", clazz.getName))
      case Some(e) => e
    }
  }

  /**
   * Intercept and return an instance of the passed exception class (or an instance of a subclass of the
   * passed class), which is expected to be thrown by the passed function value. This method invokes the passed
   * function. If it throws an exception that's an instance of the passed class or one of its
   * subclasses, this method returns that exception. Else, whether the passed function returns normally
   * or completes abruptly with a different exception, this method throws <code>AssertionError</code>.
   *
   * <p>
   * Note that the passed <code>Class</code> may represent any type, not just <code>Throwable</code> or one of its subclasses. In
   * Scala, exceptions can be caught based on traits they implement, so it may at times make sense to pass in a class instance for
   * a trait. If a class instance is passed for a type that could not possibly be used to catch an exception (such as <code>String</code>,
   * for example), this method will complete abruptly with an <code>AssertionError</code>.
   * </p>
   *
   * @param clazz a type to which the expected exception class is assignable, i.e., the exception should be an instance of the type represented by <code>clazz</code>.
   * @param f the function value that should throw the expected exception
   * @return the intercepted exception, if 
   * @throws AssertionError if the passed function does not complete abruptly with an exception that is assignable to the 
   *     passed <code>Class</code>.
   * @throws IllegalArgumentException if the passed <code>clazz</code> is not <code>Throwable</code> or
   *     one of its subclasses.
   */
  def intercept(clazz: java.lang.Class[_ <: AnyRef])(f: => Unit): Throwable = {
    intercept(clazz, "")(f)
  }

  /**
   * Expect that the value passed as <code>expected</code> equals the value resulting from the passed function <code>f</code>.
   * The <code>expect</code> method invokes the passed function. If the function results in a value that equals <code>expected</code>
   * (as determined by <code>==</code>), <code>expect</code> returns
   * normally. Else, if the function results in a value that is not equal to <code>expected</code>, <code>expect</code> throws an
   * <code>AssertionError</code> whose detail message includes the expected and actual values, as well as the <code>String</code>
   * obtained by invoking <code>toString</code> on the passed <code>message</code>.
   * If the function, completes abruptly an exception, the <code>expect</code> method will complete abruptly with that same exception.
   *
   * @param expected the expected result of the passed function 
   * @param message An objects whose <code>toString</code> method returns a message to include in a failure report.
   * @param f the function value whose result when invoked should equal the passed <code>expected</code> value
   * @throws AssertionError if the passed function does not complete abruptly with an exception that is assignable to the 
   *     passed <code>Class</code>.
   */
  def expect(expected: Any, message: Any)(f: => Any): Unit = {
    val actual = f
    if (actual != expected) {
        val (act, exp) = Suite.getObjectsForFailureMessage(actual, expected)
          val s = Resources("expectedButGot", Suite.decoratedToStringValue(exp), Suite.decoratedToStringValue(act))
      throw new AssertionError(message + "\n" + s)
    }
  }

  /**
   * Expect that the value passed as <code>expected</code> equals the value resulting from the passed function <code>f</code>.
   * The <code>expect</code> method invokes the passed function. If the function results in a value that equals <code>expected</code>
   * (as determined by <code>==</code>), <code>expect</code> returns
   * normally. Else, if the function results in a value that is not equal to <code>expected</code>, <code>expect</code> throws an
   * <code>AssertionError</code> whose detail message includes the expected and actual values.
   * If the function, completes abruptly an exception, the <code>expect</code> method will complete abruptly with that same exception.
   *
   * @param expected the expected result of the passed function 
   * @param f the function value whose result when invoked should equal the passed <code>expected</code> value
   * @throws AssertionError if the passed function does not complete abruptly with an exception that is assignable to the 
   *     passed <code>Class</code>.
   */
  def expect(expected: Any)(f: => Any): Unit = {
    expect(expected, "")(f)
  }

  // Wrap any non-DispatchReporter, non-CatchReporter in a CatchReporter,
  // so that exceptions are caught and transformed
  // into error messages on the standard error stream.
  private[scalatest] def wrapReporterIfNecessary(reporter: Reporter) = reporter match {
    case dr: DispatchReporter => dr
    case cr: CatchReporter => cr
    case _ => new CatchReporter(reporter)
  }
}

private[scalatest] object Suite {

  // [bv: this is a good example of the expression type refactor. I moved this from SuiteClassNameListCellRenderer]
  // this will be needed by the GUI classes, etc.
  private[scalatest] def parseSimpleName(fullyQualifiedName: String) = {

    val dotPos = fullyQualifiedName.lastIndexOf('.')

    // [bv: need to check the dotPos != fullyQualifiedName.length]
    if (dotPos != -1 && dotPos != fullyQualifiedName.length)
      fullyQualifiedName.substring(dotPos + 1)
    else
      fullyQualifiedName
  }
  
  private[scalatest] def checkForPublicNoArgConstructor(clazz: java.lang.Class[_]) = {
    
    try {
      val constructor = clazz.getConstructor(new Array[java.lang.Class[T] forSome { type T }](0))

      Modifier.isPublic(constructor.getModifiers)
    }
    catch {
      case nsme: NoSuchMethodException => false
    }
  }

  private[scalatest] def stripDollars(s: String): String = {
    val lastDollarIndex = s.lastIndexOf('$')
    if (lastDollarIndex < s.length - 1)
      if (lastDollarIndex == -1 || !s.startsWith("line")) s else s.substring(lastDollarIndex + 1)
    else {
      // The last char is a dollar sign
      val lastNonDollarChar = s.reverse.find(_ != '$')
      lastNonDollarChar match {
        case None => s
        case Some(c) => {
          val lastNonDollarIndex = s.lastIndexOf(c)
          if (lastNonDollarIndex == -1) s
          else stripDollars(s.substring(0, lastNonDollarIndex + 1))
        }
      }
    }
  }
  
  private[scalatest] def diffStrings(s: String, t: String): Tuple2[String, String] = {
    def findCommonPrefixLength(s: String, t: String): Int = {
      val max = s.length.min(t.length) // the maximum potential size of the prefix
      var i = 0
      var found = false
      while (i < max & !found) {
        found = (s.charAt(i) != t.charAt(i))
        if (!found)
          i = i + 1
      }
      i
    }
    def findCommonSuffixLength(s: String, t: String): Int = {
      val max = s.length.min(t.length) // the maximum potential size of the suffix
      var i = 0
      var found = false
      while (i < max & !found) {
        found = (s.charAt(s.length - 1 - i) != t.charAt(t.length - 1 - i))
        if (!found)
          i = i + 1
      }
      i
    }
    val commonPrefixLength = findCommonPrefixLength(s, t)
    val commonSuffixLength = findCommonSuffixLength(s.substring(commonPrefixLength), t.substring(commonPrefixLength))
    val prefix = s.substring(0, commonPrefixLength)
    val suffix = if (s.length - commonSuffixLength < 0) "" else s.substring(s.length - commonSuffixLength)
    val sMiddleEnd = s.length - commonSuffixLength
    val tMiddleEnd = t.length - commonSuffixLength
    val sMiddle = s.substring(commonPrefixLength, sMiddleEnd)
    val tMiddle = t.substring(commonPrefixLength, tMiddleEnd)
    val MaxContext = 20
    val shortPrefix = if (commonPrefixLength > MaxContext) "..." + prefix.substring(prefix.length - MaxContext) else prefix
    val shortSuffix = if (commonSuffixLength > MaxContext) suffix.substring(0, MaxContext) + "..." else suffix
    (shortPrefix + "[" + sMiddle + "]" + shortSuffix, shortPrefix + "[" + tMiddle + "]" + shortSuffix)
  }
  
  // If the objects are two strings, replace them with whatever is returned by diffStrings.
  // Otherwise, use the same objects.
  private def getObjectsForFailureMessage(a: Any, b: Any) = 
    a match {
      case aStr: String => {
        b match {
          case bStr: String => {
            Suite.diffStrings(aStr, bStr)    
          }
          case _ => (a, b)
        }
      } 
      case _ => (a, b)
    }
  
  private[scalatest] def decoratedToStringValue(o: Any): String =
    o match {
      case aByte: Byte => aByte.toString
      case aShort: Short => aShort.toString
      case anInt: Int => anInt.toString
      case aLong: Long => aLong.toString // Can't we all get aLong?
      case aFloat: Float => aFloat.toString
      case aDouble: Double => aDouble.toString
      case aBoolean: Boolean => aBoolean.toString
      case aUnit: Unit => "<(), the Unit value>"
      case aString: String => "\"" + aString + "\""
      case aChar: Char =>  "\'" + aChar + "\'"
      case o: Any => "<" + o.toString + ">"
    }
}


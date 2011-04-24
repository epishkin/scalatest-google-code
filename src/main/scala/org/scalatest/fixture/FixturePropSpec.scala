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

import org.scalatest._
import scala.collection.immutable.ListSet
import java.util.ConcurrentModificationException
import java.util.concurrent.atomic.AtomicReference
import org.scalatest.StackDepthExceptionHelper.getStackDepth
import org.scalatest.events._
import Suite.anErrorThatShouldCauseAnAbort
import Suite.checkRunTestParamsForNull

/**
 * A sister trait to <code>org.scalatest.PropSpec</code> that can pass a fixture object into its tests.
 *
 * <p>
 * This trait behaves similarly to trait <code>org.scalatest.PropSpec</code>, except that tests may take a fixture object. The type of the
 * fixture object passed is defined by the abstract <code>FixtureParam</code> type, which is declared as a member of this trait (inherited
 * from supertrait <code>FixtureSuite</code>).
 * This trait also inherits the abstract method <code>withFixture</code> from supertrait <code>FixtureSuite</code>. The <code>withFixture</code> method
 * takes a <code>OneArgTest</code>, which is a nested trait defined as a member of supertrait <code>FixtureSuite</code>.
 * <code>OneArgTest</code> has an <code>apply</code> method that takes a <code>FixtureParam</code>.
 * This <code>apply</code> method is responsible for running a test.
 * This trait's <code>runTest</code> method delegates the actual running of each test to <code>withFixture</code>, passing
 * in the test code to run via the <code>OneArgTest</code> argument. The <code>withFixture</code> method (abstract in this trait) is responsible
 * for creating the fixture and passing it to the test function.
 * </p>
 * 
 * <p>
 * Subclasses of this trait must, therefore, do three things differently from a plain old <code>org.scalatest.PropSpec</code>:
 * </p>
 * 
 * <ol>
 * <li>define the type of the fixture object by specifying type <code>FixtureParam</code></li>
 * <li>define the <code>withFixture</code> method</li>
 * <li>write tests that take a <code>FixtureParam</code> (You can also define tests that don't take a <code>FixtureParam</code>.)</li>
 * </ol>
 *
 * <p>
 * Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixturePropSpec
 * import org.scalatest.prop.PropertyChecks
 * import org.scalatest.matchers.ShouldMatchers
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MyPropSpec extends FixturePropSpec with PropertyChecks with ShouldMatchers {
 *
 *   // 1. define type FixtureParam
 *   type FixtureParam = FileReader
 *
 *   // 2. define the withFixture method
 *   def withFixture(test: OneArgTest) {
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
 *       test(reader)
 *     }
 *     finally {
 *       // Close and delete the temp file
 *       reader.close()
 *       val file = new File(FileName)
 *       file.delete()
 *     }
 *   }
 * 
 *   // 3. write property-based tests that take a fixture parameter
 *   // (Hopefully less contrived than the examples shown here.)
 *   property("reading from the temp file") { reader =>
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     val fileContents = builder.toString
 *     forAll { (c: Char) =>
 *       whenever (c != 'H') {
 *         fileContents should not startWith c.toString
 *       }
 *     }
 *   }
 * 
 *   property("first char of the temp file") { reader =>
 *     val firstChar = reader.read()
 *     forAll { (c: Char) =>
 *       whenever (c != 'H') {
 *         c should not equal firstChar
 *       }
 *     }
 *   }
 * 
 *   // (You can also write tests that don't take a fixture parameter.)
 *   property("without a fixture") { () =>
 *     forAll { (i: Int) => i + i should equal (2 * i) }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If the fixture you want to pass into your tests consists of multiple objects, you will need to combine
 * them into one object to use this trait. One good approach to passing multiple fixture objects is
 * to encapsulate them in a tuple. Here's an example that takes the tuple approach:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixturePropSpec
 * import org.scalatest.prop.PropertyChecks
 * import org.scalatest.matchers.ShouldMatchers
 * import scala.collection.mutable.ListBuffer
 *
 * class MyPropSpec extends FixturePropSpec with PropertyChecks with ShouldMatchers {
 *
 *   type FixtureParam = (StringBuilder, ListBuffer[String])
 *
 *   def withFixture(test: OneArgTest) {
 *
 *     // Create needed mutable objects
 *     val stringBuilder = new StringBuilder("ScalaTest is ")
 *     val listBuffer = new ListBuffer[String]
 *
 *     // Invoke the test function, passing in the mutable objects
 *     test(stringBuilder, listBuffer)
 *   }
 *
 *   property("easy") { fixture => 
 *     val (builder, buffer) = fixture
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     val firstChar = builder(0)
 *     forAll { (c: Char) =>
 *       whenever (c != 'S') {
 *         c should not equal firstChar
 *       }
 *     }
 *     buffer += "sweet"
 *   }
 *
 *   property("fun") { fixture =>
 *     val (builder, buffer) = fixture
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(buffer.isEmpty)
 *     val firstChar = builder(0)
 *     forAll { (c: Char) =>
 *       whenever (c != 'S') {
 *         c should not equal firstChar
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * When using a tuple to pass multiple fixture objects, it is usually helpful to give names to each
 * individual object in the tuple with a pattern-match assignment, as is done at the beginning
 * of each test here with:
 * </p>
 *
 * <pre>
 * val (builder, buffer) = fixture
 * </pre>
 *
 * <p>
 * Another good approach to passing multiple fixture objects is
 * to encapsulate them in a case class. Here's an example that takes the case class approach:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixturePropSpec
 * import org.scalatest.prop.PropertyChecks
 * import org.scalatest.matchers.ShouldMatchers
 * import scala.collection.mutable.ListBuffer
 *
 * class MyPropSpec extends FixturePropSpec with PropertyChecks with ShouldMatchers {
 *
 *   case class FixtureHolder(builder: StringBuilder, buffer: ListBuffer[String])
 *
 *   type FixtureParam = FixtureHolder
 *
 *   def withFixture(test: OneArgTest) {
 *
 *     // Create needed mutable objects
 *     val stringBuilder = new StringBuilder("ScalaTest is ")
 *     val listBuffer = new ListBuffer[String]
 *
 *     // Invoke the test function, passing in the mutable objects
 *     test(FixtureHolder(stringBuilder, listBuffer))
 *   }
 *
 *   property("easy") { fixture =>
 *     import fixture._
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     val firstChar = builder(0)
 *     forAll { (c: Char) =>
 *       whenever (c != 'S') {
 *         c should not equal firstChar
 *       }
 *     }
 *     buffer += "sweet"
 *   }
 *
 *   property("fun") { fixture =>
 *     fixture.builder.append("fun!")
 *     assert(fixture.builder.toString === "ScalaTest is fun!")
 *     assert(fixture.buffer.isEmpty)
 *     val firstChar = fixture.builder(0)
 *     forAll { (c: Char) =>
 *       whenever (c != 'S') {
 *         c should not equal firstChar
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * When using a case class to pass multiple fixture objects, it can be helpful to make the names of each
 * individual object available as a single identifier with an import statement. This is the approach
 * taken by the <code>testEasy</code> method in the previous example. Because it imports the members
 * of the fixture object, the test code can just use them as unqualified identifiers:
 * </p>
 *
 * <pre>
 * property("easy") { fixture =>
 *   import fixture._
 *   builder.append("easy!")
 *   assert(builder.toString === "ScalaTest is easy!")
 *   assert(buffer.isEmpty)
 *   val firstChar = builder(0)
 *   forAll { (c: Char) =>
 *     whenever (c != 'S') {
 *       c should not equal firstChar
 *     }
 *   }
 *   buffer += "sweet"
 * }
 * </pre>
 *
 * <p>
 * Alternatively, you may sometimes prefer to qualify each use of a fixture object with the name
 * of the fixture parameter. This approach, taken by the <code>testFun</code> method in the previous
 * example, makes it more obvious which variables in your test 
 * are part of the passed-in fixture:
 * </p>
 *
 * <pre>
 * property("fun") { fixture =>
 *   fixture.builder.append("fun!")
 *   assert(fixture.builder.toString === "ScalaTest is fun!")
 *   assert(fixture.buffer.isEmpty)
 *   val firstChar = fixture.builder(0)
 *   forAll { (c: Char) =>
 *     whenever (c != 'S') {
 *       c should not equal firstChar
 *     }
 *   }
 * }
 * </pre>
 *
 * <h2>Configuring fixtures and tests</h2>
 * 
 * <p>
 * Sometimes you may want to write tests that are configurable. For example, you may want to write
 * a suite of tests that each take an open temp file as a fixture, but whose file name is specified
 * externally so that the file name can be can be changed from run to run. To accomplish this
 * the <code>OneArgTest</code> trait has a <code>configMap</code>
 * method, which will return a <code>Map[String, Any]</code> from which configuration information may be obtained.
 * The <code>runTest</code> method of this trait will pass a <code>OneArgTest</code> to <code>withFixture</code>
 * whose <code>configMap</code> method returns the <code>configMap</code> passed to <code>runTest</code>.
 * Here's an example in which the name of a temp file is taken from the passed <code>configMap</code>:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixturePropSpec
 * import org.scalatest.prop.PropertyChecks
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MyPropSpec extends FixturePropSpec with PropertyChecks {
 *
 *   type FixtureParam = FileReader
 *
 *   def withFixture(test: OneArgTest) {
 *
 *     require(
 *       test.configMap.contains("TempFileName"),
 *       "This suite requires a TempFileName to be passed in the configMap"
 *     )
 *
 *     // Grab the file name from the configMap
 *     val FileName = test.configMap("TempFileName").asInstanceOf[String]
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
 *       test(reader)
 *     }
 *     finally {
 *       // Close and delete the temp file
 *       reader.close()
 *       val file = new File(FileName)
 *       file.delete()
 *     }
 *   }
 * 
 *   property("reading from the temp file") { reader =>
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     val fileContents = builder.toString
 *     forAll { (c: Char) =>
 *       whenever (c != 'H') {
 *         fileContents should not startWith c.toString
 *       }
 *     }
 *   }
 * 
 *   property("first char of the temp file") { reader =>
 *     val firstChar = reader.read()
 *     forAll { (c: Char) =>
 *       whenever (c != 'H') {
 *         c should not equal firstChar
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you want to pass into each test the entire <code>configMap</code> that was passed to <code>runTest</code>, you 
 * can mix in trait <code>ConfigMapFixture</code>. See the <a href="ConfigMapFixture.html">documentation
 * for <code>ConfigMapFixture</code></a> for the details, but here's a quick
 * example of how it looks:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixturePropSpec
 * import org.scalatest.prop.PropertyChecks
 * import org.scalatest.matchers.ShouldMatchers
 * import org.scalatest.fixture.ConfigMapFixture
 *
 * class MyPropSpec extends FixturePropSpec with ConfigMapFixture with PropertyChecks with ShouldMatchers {
 *
 *   property("hello") { configMap =>
 *     // Use the configMap passed to runTest in the test
 *     forAll { (s: String) =>
 *       whenever (!configMap.contains(s)) {
 *         s should not equal "hello"
 *       }
 *     }
 *   }
 *
 *   property("world") { configMap =>
 *     forAll { (s: String) =>
 *       whenever (!configMap.contains(s)) {
 *         s should not equal "world"
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * <code>ConfigMapFixture</code> can also be used to facilitate writing <code>FixturePropSpec</code>s that include tests
 * that take different fixture types. See the documentation for <a href="MultipleFixturePropSpec.html"><code>MultipleFixturePropSpec</code></a> for more information.
 * </p>
 *
 * @author Bill Venners
 */
trait FixturePropSpec extends FixtureSuite { thisSuite =>

  private final val engine = new FixtureEngine[FixtureParam]("concurrentFixturePropSpecMod", "FixturePropSpec")
  import engine._

  /**
   * Returns an <code>Informer</code> that during test execution will forward strings (and other objects) passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked while this
   * <code>FixturePropSpec</code> is being executed, such as from inside a test function, it will forward the information to
   * the current reporter immediately. If invoked at any other time, it will
   * throw an exception. This method can be called safely by any thread.
   */
  implicit protected def info: Informer = atomicInformer.get

  /**
   * Register a property-based test with the specified name, optional tags, and function value that takes no arguments.
   * This method will register the test for later execution via an invocation of one of the <code>run</code>
   * methods. The passed test name must not have been registered previously on
   * this <code>PropSpec</code> instance.
   *
   * @param testName the name of the test
   * @param testTags the optional list of tags for this test
   * @param testFun the test function
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws NotAllowedException if <code>testName</code> had been registered previously
   * @throws NullPointerException if <code>testName</code> or any passed test tag is <code>null</code>
   */
  protected def property(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    registerTest(testName, testFun, "testCannotAppearInsideAnotherTest", "FixturePropSpec.scala", "test", testTags: _*)
  }

  /**
   * Register a property-based test to ignore, which has the specified name, optional tags, and function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>run</code>
   * methods. This method exists to make it easy to ignore an existing test by changing the call to <code>test</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be run, but a
   * report will be sent that indicates the test was ignored. The passed test name must not have been registered previously on
   * this <code>PropSpec</code> instance.
   *
   * @param testName the name of the test
   * @param testTags the optional list of tags for this test
   * @param testFun the test function
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws NotAllowedException if <code>testName</code> had been registered previously
   */
  protected def ignore(testName: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    registerIgnoredTest(testName, testFun, "ignoreCannotAppearInsideATest", "FixturePropSpec.scala", "ignore", testTags: _*)
  }

  /**
  * An immutable <code>Set</code> of test names. If this <code>FixturePropSpec</code> contains no tests, this method returns an empty <code>Set</code>.
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

  /**
   * Run a test. This trait's implementation runs the test registered with the name specified by <code>testName</code>.
   *
   * @param testName the name of one test to run.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param configMap a <code>Map</code> of properties that can be used by the executing <code>Suite</code> of tests.
   * @throws IllegalArgumentException if <code>testName</code> is defined but a test with that name does not exist on this <code>FixturePropSpec</code>
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, or <code>configMap</code>
   *     is <code>null</code>.
   */
  protected override def runTest(testName: String, reporter: Reporter, stopper: Stopper, configMap: Map[String, Any], tracker: Tracker) {

    def invokeWithFixture(theTest: TestLeaf) {
      theTest.testFun match {
        case wrapper: NoArgTestWrapper[_] =>
          withFixture(new FixturelessTestFunAndConfigMap(testName, wrapper.test, configMap))
        case fun => withFixture(new TestFunAndConfigMap(testName, fun, configMap))
      }
    }

    runTestImpl(thisSuite, testName, reporter, stopper, configMap, tracker, true, invokeWithFixture)
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> tag names to which tests in this <code>FixturePropSpec</code> belong, and values
   * the <code>Set</code> of test names that belong to each tag. If this <code>FixturePropSpec</code> contains no tags, this method returns an empty
   * <code>Map</code>.
   *
   * <p>
   * This trait's implementation returns tags that were passed as strings contained in <code>Tag</code> objects passed to
   * methods <code>test</code> and <code>ignore</code>.
   * </p>
   */
  override def tags: Map[String, Set[String]] = atomic.get.tagsMap

  protected override def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    runTestsImpl(thisSuite, testName, reporter, stopper, filter, configMap, distributor, tracker, info, true, runTest)
  }

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    runImpl(thisSuite, testName, reporter, stopper, filter, configMap, distributor, tracker, super.run)
  }

  /**
   * Registers shared tests.
   *
   * <p>
   * This method enables the following syntax for shared tests in a <code>FixturePropSpec</code>:
   * </p>
   *
   * <pre>
   * testsFor(nonEmptyStack(lastValuePushed))
   * </pre>
   *
   * <p>
   * This method just provides syntax sugar intended to make the intent of the code clearer.
   * Because the parameter passed to it is
   * type <code>Unit</code>, the expression will be evaluated before being passed, which
   * is sufficient to register the shared tests. For examples of shared tests, see the
   * <a href="../PropSpec.html#SharedTests">Shared tests section</a> in the main documentation for
   * trait <code>PropSpec</code>.
   * </p>
   */
  protected def testsFor(unit: Unit) {}

  /**
   * Implicitly converts a function that takes no parameters and results in <code>PendingNothing</code> to
   * a function from <code>FixtureParam</code> to <code>Any</code>, to enable pending tests to registered as by-name parameters
   * by methods that require a test function that takes a <code>FixtureParam</code>.
   *
   * <p>
   * This method makes it possible to write pending tests as simply <code>(pending)</code>, without needing
   * to write <code>(fixture => pending)</code>.
   * </p>
   */
  protected implicit def convertPendingToFixtureFunction(f: => PendingNothing): (FixtureParam => Any) = {
    fixture => f
  }

  /**
   * Implicitly converts a function that takes no parameters and results in <code>Any</code> to
   * a function from <code>FixtureParam</code> to <code>Any</code>, to enable no-arg tests to registered
   * by methods that require a test function that takes a <code>FixtureParam</code>.
   */
  protected implicit def convertNoArgToFixtureFunction(fun: () => Any): (FixtureParam => Any) =
    new NoArgTestWrapper(fun)
}

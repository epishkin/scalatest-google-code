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
import FixtureNodeFamily._
import scala.collection.immutable.ListSet
import org.scalatest.StackDepthExceptionHelper.getStackDepth
import java.util.concurrent.atomic.AtomicReference
import java.util.ConcurrentModificationException
import org.scalatest.events._
import Suite.anErrorThatShouldCauseAnAbort

/**
 * A sister trait to <code>org.scalatest.FeatureSpec</code> that can pass a fixture object into its tests.
 *
 * <p>
 * This trait behaves similarly to trait <code>org.scalatest.FeatureSpec</code>, except that tests may take a fixture object. The type of the
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
 * Subclasses of this trait must, therefore, do three things differently from a plain old <code>org.scalatest.FeatureSpec</code>:
 * </p>
 * 
 * <ol>
 * <li>define the type of the fixture object by specifying type <code>FixtureParam</code></li>
 * <li>define the <code>withFixture</code> method</li>
 * <li>write scenarios that take a <code>FixtureParam</code> (You can also define scenarios that don't take a <code>FixtureParam</code>.)</li>
 * </ol>
 *
 * <p>
 * Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixtureFeatureSpec
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MyFeatureSpec extends FixtureFeatureSpec {
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
 *   // 3. write scenarios that take a fixture parameter
 *   feature("Reading and writing files") {
 *     scenario("reading from the temp file") { reader =>
 *       var builder = new StringBuilder
 *       var c = reader.read()
 *       while (c != -1) {
 *         builder.append(c.toChar)
 *         c = reader.read()
 *       }
 *       assert(builder.toString === "Hello, test!")
 *     }
 * 
 *     scenario("first char of the temp file") { reader =>
 *       assert(reader.read() === 'H')
 *     }
 * 
 *     // (You can also write scenarios that don't take a fixture parameter.)
 *     scenario("no fixture is passed") { () =>
 *       assert(1 + 1 === 2)
 *     }
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
 * import org.scalatest.fixture.FixtureFeatureSpec
 * import scala.collection.mutable.ListBuffer
 *
 * class MyFeatureSpec extends FixtureFeatureSpec {
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
 *   scenario("easy") { fixture => 
 *     val (builder, buffer) = fixture
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 *
 *   scenario("fun") { fixture =>
 *     val (builder, buffer) = fixture
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(buffer.isEmpty)
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
 * import org.scalatest.fixture.FixtureFeatureSpec
 * import scala.collection.mutable.ListBuffer
 *
 * class MyFeatureSpec extends FixtureFeatureSpec {
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
 *   scenario("easy") { fixture =>
 *     import fixture._
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 *
 *   scenario("fun") { fixture =>
 *     fixture.builder.append("fun!")
 *     assert(fixture.builder.toString === "ScalaTest is fun!")
 *     assert(fixture.buffer.isEmpty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * When using a case class to pass multiple fixture objects, it can be helpful to make the names of each
 * individual object available as a single identifier with an import statement. This is the approach
 * taken by the <code>testEasy</code> method in the previous example. Because it imports the members
 * of the fixture object, the test method code can just use them as unqualified identifiers:
 * </p>
 *
 * <pre>
 * scenario("easy") { fixture =>
 *   import fixture._
 *   builder.append("easy!")
 *   assert(builder.toString === "ScalaTest is easy!")
 *   assert(buffer.isEmpty)
 *   buffer += "sweet"
 * }
 * </pre>
 *
 * <p>
 * Alternatively, you may sometimes prefer to qualify each use of a fixture object with the name
 * of the fixture parameter. This approach, taken by the <code>testFun</code> method in the previous
 * example, makes it more obvious which variables in your test method
 * are part of the passed-in fixture:
 * </p>
 *
 * <pre>
 * scenario("fun") { fixture =>
 *   fixture.builder.append("fun!")
 *   assert(fixture.builder.toString === "ScalaTest is fun!")
 *   assert(fixture.buffer.isEmpty)
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
 * import org.scalatest.fixture.FixtureFeatureSpec
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MyFeatureSpec extends FixtureFeatureSpec {
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
 *   scenario("reading from the temp file") { reader =>
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     assert(builder.toString === "Hello, test!")
 *   }
 * 
 *   scenario("first char of the temp file") { reader =>
 *     assert(reader.read() === 'H')
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
 *  import org.scalatest.fixture.FixtureFeatureSpec
 *  import org.scalatest.fixture.ConfigMapFixture
 *
 *  class MyFeatureSpec extends FixtureFeatureSpec with ConfigMapFixture {
 *
 *    scenario("hello") { configMap =>
 *      // Use the configMap passed to runTest in the test
 *      assert(configMap.contains("hello"))
 *    }
 *
 *    scenario("world") { configMap =>
 *      assert(configMap.contains("world"))
 *    }
 *  }
 * </pre>
 *
 * <p>
 * <code>ConfigMapFixture</code> can also be used to facilitate writing <code>FixtureFeatureSpec</code>s that include tests
 * that take different fixture types. See the documentation for <a href="MultipleFixtureFeatureSpec.html"><code>MultipleFixtureFeatureSpec</code></a> for more information.
 * </p>
 *
 * @author Bill Venners
 */
trait FixtureFeatureSpec extends FixtureSuite { thisSuite =>

  private final val engine = new FixtureEngine[FixtureParam]("concurrentFeatureSpecMod", "FixtureFeatureSpec")
  import engine._

  /**
   * Returns an <code>Informer</code> that during test execution will forward strings (and other objects) passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked while this
   * <code>FixtureFeatureSpec</code> is being executed, such as from inside a test function, it will forward the information to
   * the current reporter immediately. If invoked at any other time, it will
   * throw an exception. This method can be called safely by any thread.
   */
  implicit protected def info: Informer = atomicInformer.get

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
  protected def scenario(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) {

    registerTest(Resources("scenario", specText), testFun, "scenarioCannotAppearInsideAnotherScenario", "FixtureFeatureSpec.scala", "scenario", testTags: _*)
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
  protected def ignore(specText: String, testTags: Tag*)(testFun: FixtureParam => Any) {
    registerIgnoredTest(Resources("scenario", specText), testFun , "ignoreCannotAppearInsideAScenario", "FixtureFeatureSpec.scala", "ignore", testTags: _*)
  }

  /**
   * Describe a &#8220;subject&#8221; being specified and tested by the passed function value. The
   * passed function value may contain more describers (defined with <code>describe</code>) and/or tests
   * (defined with <code>it</code>). This trait's implementation of this method will register the
   * description string and immediately invoke the passed function.
   */
  protected def feature(description: String)(fun: => Unit) {

    if (!currentBranchIsTrunk)
      throw new NotAllowedException(Resources("cantNestFeatureClauses"), getStackDepth("FixtureFeatureSpec.scala", "feature"))

    registerNestedBranch(description, None, fun, "featureCannotAppearInsideAScenario", "FixtureFeatureSpec.scala", "feature")
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> tag names to which tests in this <code>Spec</code> belong, and values
   * the <code>Set</code> of test names that belong to each tag. If this <code>FeatureSpec</code> contains no tags, this method returns an empty <code>Map</code>.
   *
   * <p>
   * This trait's implementation returns tags that were passed as strings contained in <code>Tag</code> objects passed to
   * methods <code>test</code> and <code>ignore</code>.
   * </p>
   */
  override def tags: Map[String, Set[String]] = atomic.get.tagsMap

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


    def invokeWithFixture(theTest: TestLeaf) {
      theTest.testFun match {
        case wrapper: NoArgTestWrapper[_] =>
          withFixture(new FixturelessTestFunAndConfigMap(testName, wrapper.test, configMap))
        case fun => withFixture(new TestFunAndConfigMap(testName, fun, configMap))
      }
    }

    runTestImpl(thisSuite, testName, reporter, stopper, configMap, tracker, false, invokeWithFixture)
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

    runTestsImpl(thisSuite, testName, reporter, stopper, filter, configMap, distributor, tracker, info, false, runTest)
  }

  /**
   * An immutable <code>Set</code> of test names. If this <code>FixtureFeatureSpec</code> contains no tests, this method returns an
   * empty <code>Set</code>.
   *
   * <p>
   * This trait's implementation of this method will return a set that contains the names of all registered tests. The set's
   * iterator will return those names in the order in which the tests were registered. Each test's name is composed
   * of the concatenation of the text of each surrounding describer, in order from outside in, and the text of the
   * example itself, with all components separated by a space.
   * </p>
   */
  //override def testNames: Set[String] = ListSet(atomic.get.testsList.map(_.testName): _*)
  override def testNames: Set[String] = {
    // I'm returning a ListSet here so that they tests will be run in registration order
    ListSet(atomic.get.testNamesList.toArray: _*)
  }

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    runImpl(thisSuite, testName, reporter, stopper, filter, configMap, distributor, tracker, super.run)
  }

  /**
   * Registers shared scenarios.
   *
   * <p>
   * This method enables the following syntax for shared scenarios in a <code>FixtureFeatureSpec</code>:
   * </p>
   *
   * <pre>
   * scenariosFor(nonEmptyStack(lastValuePushed))
   * </pre>
   *
   * <p>
   * This method just provides syntax sugar intended to make the intent of the code clearer.
   * Because the parameter passed to it is
   * type <code>Unit</code>, the expression will be evaluated before being passed, which
   * is sufficient to register the shared scenarios. For examples of shared scenarios, see the
   * <a href="../FeatureSpec.html#SharedScenarios">Shared scenarios section</a> in the main documentation for
   * trait <code>FeatureSpec</code>.
   * </p>
   */
  protected def scenariosFor(unit: Unit) {}

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
  protected implicit def convertPendingToFixtureFunction(f: => PendingNothing): FixtureParam => Any = {
    fixture => f
  }

  // I need this implicit because the function is passed to scenario as the 2nd parameter list, and
  // I can't overload on that. I could if I took the ScenarioWord approach, but that has possibly a worse
  // downside of people could just say scenario("...") and nothing else.
  /**
   * Implicitly converts a function that takes no parameters and results in <code>Any</code> to
   * a function from <code>FixtureParam</code> to <code>Any</code>, to enable no-arg tests to registered
   * by methods that require a test function that takes a <code>FixtureParam</code>.
   */
  protected implicit def convertNoArgToFixtureFunction(fun: () => Any): (FixtureParam => Any) =
    new NoArgTestWrapper(fun)
}


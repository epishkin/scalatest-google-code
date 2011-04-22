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
import verb.{ResultOfTaggedAsInvocation, ResultOfStringPassedToVerb, BehaveWord, ShouldVerb, MustVerb, CanVerb}
import scala.collection.immutable.ListSet
import org.scalatest.StackDepthExceptionHelper.getStackDepth
import java.util.concurrent.atomic.AtomicReference
import java.util.ConcurrentModificationException
import org.scalatest.events._
import Suite.anErrorThatShouldCauseAnAbort

/**
 * A sister trait to <code>org.scalatest.FlatSpec</code> that can pass a fixture object into its tests.
 *
 * <p>
 * This trait behaves similarly to trait <code>org.scalatest.FlatSpec</code>, except that tests may take a fixture object. The type of the
 * fixture object passed is defined by the abstract <code>Fixture</code> type, which is declared as a member of this trait (inherited
 * from supertrait <code>FixtureSuite</code>).
 * This trait also inherits the abstract method <code>withFixture</code> from supertrait <code>FixtureSuite</code>. The <code>withFixture</code> method
 * takes a <code>OneArgTest</code>, which is a nested trait defined as a member of supertrait <code>FixtureSuite</code>.
 * <code>OneArgTest</code> has an <code>apply</code> method that takes a <code>Fixture</code>.
 * This <code>apply</code> method is responsible for running a test.
 * This trait's <code>runTest</code> method delegates the actual running of each test to <code>withFixture</code>, passing
 * in the test code to run via the <code>OneArgTest</code> argument. The <code>withFixture</code> method (abstract in this trait) is responsible
 * for creating the fixture and passing it to the test function.
 * </p>
 * 
 * <p>
 * Subclasses of this trait must, therefore, do three things differently from a plain old <code>org.scalatest.FlatSpec</code>:
 * </p>
 * 
 * <ol>
 * <li>define the type of the fixture object by specifying type <code>Fixture</code></li>
 * <li>define the <code>withFixture</code> method</li>
 * <li>write tests that take a <code>Fixture</code> (You can also define tests that don't take a <code>Fixture</code>.)</li>
 * </ol>
 *
 * <p>
 * Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixtureFlatSpec
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MyFlatSpec extends FixtureFlatSpec {
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
 *   // 3. write tests that take a fixture parameter
 *   it should "read from the temp file" in { reader =>
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     assert(builder.toString === "Hello, test!")
 *   }
 * 
 *   it should "read the first char of the temp file" in { reader =>
 *     assert(reader.read() === 'H')
 *   }
 * 
 *   // (You can also write tests that don't take a fixture parameter.)
 *   it should "work without a fixture" in { () =>
 *     assert(1 + 1 === 2)
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
 * import org.scalatest.fixture.FixtureFlatSpec
 * import scala.collection.mutable.ListBuffer
 *
 * class MyFlatSpec extends FixtureFlatSpec {
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
 *   it should "mutate shared fixture objects" in { fixture =>
 *     val (builder, buffer) = fixture
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 *
 *   it should "get a fresh set of mutable fixture objects" in { fixture =>
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
 * import org.scalatest.fixture.FixtureFlatSpec
 * import scala.collection.mutable.ListBuffer
 *
 * class MyFlatSpec extends FixtureFlatSpec {
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
 *   it should "mutate shared fixture objects" in { fixture =>
 *     import fixture._
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 *
 *   it should "get a fresh set of mutable fixture objects" in { fixture =>
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
 * of the fixture object, the test code can just use them as unqualified identifiers:
 * </p>
 *
 * <pre>
 * it should "mutate shared fixture objects" in { fixture =>
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
 * example, makes it more obvious which variables in your test 
 * are part of the passed-in fixture:
 * </p>
 *
 * <pre>
 * it should "get a fresh set of mutable fixture objects" in { fixture =>
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
 * import org.scalatest.fixture.FixtureFlatSpec
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MyFlatSpec extends FixtureFlatSpec {
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
 *     val FileName = test.configMap("TempFileName")
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
 *   it should "read from the temp file" in { reader =>
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     assert(builder.toString === "Hello, test!")
 *   }
 * 
 *   it should "read the first char of the temp file" in { reader =>
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
 *  import org.scalatest.fixture.FixtureFlatSpec
 *  import org.scalatest.fixture.ConfigMapFixture
 *
 *  class MyFlatSpec extends FixtureFlatSpec with ConfigMapFixture {
 *
 *    it should "contain hello" in { configMap =>
 *      // Use the configMap passed to runTest in the test
 *      assert(configMap.contains("hello")
 *    }
 *
 *    it should "contain world" in { configMap =>
 *      assert(configMap.contains("world")
 *    }
 *  }
 * </pre>
 *
 * <p>
 * <code>ConfigMapFixture</code> can also be used to facilitate writing <code>FixtureFlatSpec</code>s that include tests
 * that take different fixture types. See the documentation for <a href="MultipleFixtureFlatSpec.html"><code>MultipleFixtureFlatSpec</code></a> for more information.
 * </p>
 *
 * @author Bill Venners
 */
trait FixtureFlatSpec extends FixtureSuite with ShouldVerb with MustVerb with CanVerb { thisSuite =>

  private final val engine = new Engine[FixtureParam => Any]("concurrentFixtureFlatSpecMod", "FixtureFlatSpec")
  import engine._

  /**
   * Returns an <code>Informer</code> that during test execution will forward strings (and other objects) passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked while this
   * <code>FixtureFlatSpec</code> is being executed, such as from inside a test function, it will forward the information to
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
  private def registerTestToRun(specText: String, testTags: List[Tag], testFun: FixtureParam => Any) {

    // TODO: This is what was being used before but it is wrong
    registerTest(specText, testFun, "itCannotAppearInsideAnotherIt", "FixtureFlatSpec.scala", "it", testTags: _*)
  }

  /**
   * Class that supports the registration of a &#8220;subject&#8221; being specified and tested via the
   * instance referenced from <code>FixtureFlatSpec</code>'s <code>behavior</code> field.
   *
   * <p>
   * This field enables syntax such as the following subject registration:
   * </p>
   *
   * <pre>
   * behavior of "A Stack"
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>behavior</code> field, see the <a href="../FlatSpec.html">main documentation</a>
   * for trait <code>FlatSpec</code>.
   * </p>
   */
  protected final class BehaviorWord {

    /**
     * Supports the registration of a &#8220;subject&#8221; being specified and tested via the
     * instance referenced from <code>FixtureFlatSpec</code>'s <code>behavior</code> field.
     *
     * <p>
     * This method enables syntax such as the following subject registration:
     * </p>
     *
     * <pre>
     * behavior of "A Stack"
     *          ^
     * </pre>
     *
     * <p>
     * For more information and examples of the use of this method, see the <a href="../FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def of(description: String) {
      // TODO: This is what was here, but it needs fixing.
      behaviorOfImpl(description, "describeCannotAppearInsideAnIt", "FixtureFlatSpec.scala", "describe")
    }
  }

  /**
   * Supports the registration of a &#8220;subject&#8221; being specified and tested.
   *
   * <p>
   * This field enables syntax such as the following subject registration:
   * </p>
   *
   * <pre>
   * behavior of "A Stack"
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>behavior</code> field, see the <a href="../FlatSpec.html">main documentation</a>
   * for trait <code>FlatSpec</code>.
   * </p>
   */
  protected val behavior = new BehaviorWord

  // TODO: Do a walk through. Are all these being used. I guess I'll find out when
  // I document them.
  /**
   * Class that supports the registration of tagged tests via the <code>ItWord</code> instance
   * referenced from <code>FixtureFlatSpec</code>'s <code>it</code> field.
   *
   * <p>
   * This class enables syntax such as the following tagged test registration:
   * </p>
   *
   * <pre>
   * it should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                                      ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following registration of an ignored, tagged test:
   * </p>
   *
   * <pre>
   * it should "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { ... }
   *                                                                      ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of a pending, tagged test:
   * </p>
   *
   * <pre>
   * it should "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
   *                                                                      ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field to register tagged tests, see
   * the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
   * </p>
   */
  protected final class ItVerbStringTaggedAs(verb: String, name: String, tags: List[Tag]) {

    /**
     * Supports the registration of tagged, no-arg tests in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) in { () => ... }
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see
     * the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: () => Any) {
      registerTestToRun(verb + " " + name, tags, new NoArgTestWrapper(testFun))
    }

    /**
     * Supports the registration of tagged, one-arg tests (tests that take a <code>Fixture</code> object as a parameter) in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) in { fixture => ... }
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see
     * the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: FixtureParam => Any) {
      registerTestToRun(verb + " " + name, tags, testFun)
    }

    /**
     * Supports the registration of pending, tagged tests in a <code>FlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of pending test registration, see the <a href="../FlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def is(testFun: => PendingNothing) {
      registerTestToRun(verb + " " + name, tags, unusedFixtureParam => testFun)
    }

    /**
     * Supports the registration of ignored, tagged, no-arg tests in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { () => ... }
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: () => Any) {
      registerTestToIgnore(verb + " " + name, tags, new NoArgTestWrapper(testFun))
    }

    /**
     * Supports the registration of ignored, tagged, one-arg tests (tests that take a <code>Fixture</code> object
     * as a parameter) in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { fixture => ... }
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: FixtureParam => Any) {
      registerTestToIgnore(verb + " " + name, tags, testFun)
    }
  }

  /**
   * Class that supports test registration via the instance referenced from <code>FixtureFlatSpec</code>'s <code>it</code> field.
   *
   * <p>
   * This class enables syntax such as the following test registration:
   * </p>
   *
   * <pre>
   * it should "pop values in last-in-first-out order" in { ... }
   *                                                   ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre>
   * it should "pop values in last-in-first-out order" ignore { ... }
   *                                                   ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of a pending test:
   * </p>
   *
   * <pre>
   * it should "pop values in last-in-first-out order" is (pending)
   *                                                   ^
   * </pre>
   *
   * <p>
   * And finally, it also enables syntax such as the following tagged test registration:
   * </p>
   *
   * <pre>
   * it should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                   ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the <a href="FixtureFlatSpec.html">main documentation</a>
   * for trait <code>FixtureFlatSpec</code>.
   * </p>
   */
  protected final class ItVerbString(verb: String, name: String) {

    /**
     * Supports the registration of no-arg tests in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must "pop values in last-in-first-out order" in { () => ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of no-arg test registration, see the <a href="FixtureFlatSpec.html">main documentation</a>
     * for trait <code>FixtureFlatSpec</code>.
     * </p>
     */
    def in(testFun: () => Any) {
      registerTestToRun(verb + " " + name, List(), new NoArgTestWrapper(testFun))
    }

    /**
     * Supports the registration of one-arg tests (tests that take a <code>Fixture</code> object as a parameter) in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must "pop values in last-in-first-out order" in { fixture => ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of one-arg test registration, see the <a href="FixtureFlatSpec.html">main documentation</a>
     * for trait <code>FixtureFlatSpec</code>.
     * </p>
     */
    def in(testFun: FixtureParam => Any) {
      registerTestToRun(verb + " " + name, List(), testFun)
    }

    /**
     * Supports the registration of pending tests in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must "pop values in last-in-first-out order" is (pending)
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of pending test registration, see the <a href="../FlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def is(testFun: => PendingNothing) {
      registerTestToRun(verb + " " + name, List(), unusedFixtureParam => testFun)
    }

    /**
     * Supports the registration of ignored no-arg tests in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must "pop values in last-in-first-out order" ignore { () => ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: () => Any) {
      registerTestToIgnore(verb + " " + name, List(), new NoArgTestWrapper(testFun))
    }

    /**
     * Supports the registration of ignored one-arg tests (tests that take a <code>Fixture</code> object as a parameter) in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must "pop values in last-in-first-out order" ignore { fixture => ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: FixtureParam => Any) {
      registerTestToIgnore(verb + " " + name, List(), testFun)
    }

    /**
     * Supports the registration of tagged tests in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new ItVerbStringTaggedAs(verb, name, tagList)
    }
  }

  /**
   * Class that supports test (and shared test) registration via the instance referenced from <code>FixtureFlatSpec</code>'s <code>it</code> field.
   *
   * <p>
   * This class enables syntax such as the following test registration:
   * </p>
   *
   * <pre>
   * it should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following shared test registration:
   * </p>
   *
   * <pre>
   * it should behave like nonEmptyStack(lastItemPushed)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the main documentation 
   * for trait <a href="../FlatSpec.html"><code>FlatSpec</code></a>.
   * </p>
   */
  protected final class ItWord {

    /**
     * Supports the registration of tests with <code>should</code> in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it should "pop values in last-in-first-out order" in { ... }
     *    ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="../FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def should(string: String) = new ItVerbString("should", string)

    /**
     * Supports the registration of tests with <code>must</code> in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must "pop values in last-in-first-out order" in { ... }
     *    ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="../FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def must(string: String) = new ItVerbString("must", string)

    /**
     * Supports the registration of tests with <code>can</code> in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it can "pop values in last-in-first-out order" in { ... }
     *    ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="../FlatSpec.html">main documentation</a>
     * for trait <code>FlatSpec</code>.
     * </p>
     */
    def can(string: String) = new ItVerbString("can", string)

    /**
     * Supports the registration of shared tests with <code>should</code> in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it should behave like nonFullStack(stackWithOneItem)
     *    ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="../FlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def should(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests with <code>must</code> in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it must behave like nonFullStack(stackWithOneItem)
     *    ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="../FlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def must(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests with <code>can</code> in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * it can behave like nonFullStack(stackWithOneItem)
     *    ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="../FlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def can(behaveWord: BehaveWord) = behaveWord
  }

  /**
   * Supports test (and shared test) registration in <code>FixtureFlatSpec</code>s.
   *
   * <p>
   * This field enables syntax such as the following test registration:
   * </p>
   *
   * <pre>
   * it should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following shared test registration:
   * </p>
   *
   * <pre>
   * it should behave like nonEmptyStack(lastItemPushed)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the main documentation 
   * for trait <a href="../FlatSpec.html"><code>FlatSpec</code></a>.
   * </p>
   */
  protected val it = new ItWord

  /**
   * Class that supports registration of ignored, tagged tests via the <code>IgnoreWord</code> instance referenced
   * from <code>FixtureFlatSpec</code>'s <code>ignore</code> field.
   *
   * <p>
   * This class enables syntax such as the following registration of an ignored, tagged test:
   * </p>
   *
   * <pre>
   * ignore should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                                          ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of an ignored, tagged, pending test:
   * </p>
   *
   * <pre>
   * ignore should "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
   *                                                                          ^
   * </pre>
   *
   * <p>
   * Note: the <code>is</code> method is provided for completeness and design symmetry, given there's no way
   * to prevent changing <code>is</code> to <code>ignore</code> and marking a pending test as ignored that way.
   * Although it isn't clear why someone would want to mark a pending test as ignored, it can be done.
   * </p>
   *
   * <p>
   * For more information and examples of the use of the <code>ignore</code> field, see
   * the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
   * in the main documentation for trait <code>FlatSpec</code>. For examples of tagged test registration, see
   * the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
   * </p>
   */
  protected final class IgnoreVerbStringTaggedAs(verb: String, name: String, tags: List[Tag]) {

    /**
     * Supports the registration of ignored, tagged, no-arg tests in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * ignore must "pop values in last-in-first-out order" taggedAs(SlowTest) in { () => ... }
     *                                                                        ^
     * </pre>
     *
     * <p>
     * For examples of the registration of ignored tests, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>. For examples of tagged test registration, see
     * the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: () => Any) {
      registerTestToIgnore(verb + " " + name, tags, new NoArgTestWrapper(testFun))
    }

    /**
     * Supports the registration of ignored, tagged, one-arg tests (tests that take a <code>Fixture</code> object as a parameter)
     * in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * ignore must "pop values in last-in-first-out order" taggedAs(SlowTest) in { fixture => ... }
     *                                                                        ^
     * </pre>
     *
     * <p>
     * For examples of the registration of ignored tests, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>. For examples of tagged test registration, see
     * the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: FixtureParam => Any) {
      registerTestToIgnore(verb + " " + name, tags, testFun)
    }

    /**
     * Supports the registration of ignored, tagged, pending tests in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * ignore must "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
     *                                                                        ^
     * </pre>
     *
     * <p>
     * Note: this <code>is</code> method is provided for completeness and design symmetry, given there's no way
     * to prevent changing <code>is</code> to <code>ignore</code> and marking a pending test as ignored that way.
     * Although it isn't clear why someone would want to mark a pending test as ignored, it can be done.
     * </p>
     *
     * <p>
     * For examples of pending test registration, see the <a href="../FlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  For examples of the registration of ignored tests,
     * see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>. For examples of tagged test registration, see
     * the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def is(testFun: => PendingNothing) {
      registerTestToIgnore(verb + " " + name, tags, unusedFixtureParam => testFun)
    }
  }

  /**
   * Class that supports registration of ignored tests via the <code>IgnoreWord</code> instance referenced
   * from <code>FixtureFlatSpec</code>'s <code>ignore</code> field.
   *
   * <p>
   * This class enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre>
   * ignore should "pop values in last-in-first-out order" in { ... }
   *                                                       ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of an ignored, pending test:
   * </p>
   *
   * <pre>
   * ignore should "pop values in last-in-first-out order" is (pending)
   *                                                       ^
   * </pre>
   *
   * Note: the <code>is</code> method is provided for completeness and design symmetry, given there's no way
   * to prevent changing <code>is</code> to <code>ignore</code> and marking a pending test as ignored that way.
   * Although it isn't clear why someone would want to mark a pending test as ignored, it can be done.
   * </p>
   *
   * <p>
   * And finally, it also enables syntax such as the following ignored, tagged test registration:
   * </p>
   *
   * <pre>
   * ignore should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                       ^
   * </pre>
   *
   * <p>
   * <p>
   * For more information and examples of the use of the <code>ignore</code> field, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
   * in the main documentation for trait <code>FlatSpec</code>.
   * </p>
   */
  protected final class IgnoreVerbString(verb: String, name: String) {

    /**
     * Supports the registration of ignored, no-arg tests in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * ignore must "pop values in last-in-first-out order" in { () => ... }
     *                                                     ^
     * </pre>
     *
     * <p>
     * For examples of the registration of ignored tests, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: () => Any) {
      registerTestToIgnore(verb + " " + name, List(), new NoArgTestWrapper(testFun))
    }
     
    /**
     * Supports the registration of ignored, one-arg tests (tests that take a <code>Fixture</code> object
     * as a parameter) in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * ignore must "pop values in last-in-first-out order" in { fixture => ... }
     *                                                     ^
     * </pre>
     *
     * <p>
     * For examples of the registration of ignored tests, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: FixtureParam => Any) {
      registerTestToIgnore(verb + " " + name, List(), testFun)
    }

    /**
     * Supports the registration of ignored, pending tests in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * ignore must "pop values in last-in-first-out order" is (pending)
     *                                                     ^
     * </pre>
     *
     * <p>
     * Note: this <code>is</code> method is provided for completeness and design symmetry, given there's no way
     * to prevent changing <code>is</code> to <code>ignore</code> and marking a pending test as ignored that way.
     * Although it isn't clear why someone would want to mark a pending test as ignored, it can be done.
     * </p>
     *
     * <p>
     * For examples of pending test registration, see the <a href="../FlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  For examples of the registration of ignored tests,
     * see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def is(testFun: => PendingNothing) {
      registerTestToIgnore(verb + " " + name, List(), unusedFixtureParam => testFun)
    }

    /**
     * Supports the registration of ignored, tagged tests in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * ignore must "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
     *                                                     ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation
     * for trait <code>FlatSpec</code>.  For examples of the registration of ignored tests,
     * see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new IgnoreVerbStringTaggedAs(verb, name, tagList)
    }
  }

  /**
   * Class that supports registration of ignored tests via the instance referenced from <code>FixtureFlatSpec</code>'s <code>ignore</code> field.
   *
   * <p>
   * This class enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre>
   * ignore should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>ignore</code> field, see <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
   * in the main documentation for this trait.
   * </p>
   */
  protected final class IgnoreWord {

    /**
     * Supports the registration of ignored tests with <code>should</code> in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * ignore should "pop values in last-in-first-out order" in { ... }
     *        ^
     * </pre>
     *
     * <p>
     * For more information and examples of the use of the <code>ignore</code> field, see <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def should(string: String) = new IgnoreVerbString("should", string)

    /**
     * Supports the registration of ignored tests with <code>must</code> in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * ignore must "pop values in last-in-first-out order" in { ... }
     *        ^
     * </pre>
     *
     * <p>
     * For more information and examples of the use of the <code>ignore</code> field, see <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def must(string: String) = new IgnoreVerbString("must", string)

    /**
     * Supports the registration of ignored tests with <code>can</code> in a <code>FixtureFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * ignore can "pop values in last-in-first-out order" in { ... }
     *        ^
     * </pre>
     *
     * <p>
     * For more information and examples of the use of the <code>ignore</code> field, see <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def can(string: String) = new IgnoreVerbString("can", string)
  }

  /**
   * Supports registration of ignored tests in <code>FixtureFlatSpec</code>s.
   *
   * <p>
   * This field enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre>
   * ignore should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>ignore</code> field, see the
   * <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation for trait <code>FlatSpec</code>.
   * </p>
   */
  protected val ignore = new IgnoreWord

  /**
   * Class that supports test registration in shorthand form.
   *
   * <p>
   * For example, this class enables syntax such as the following test registration
   * in shorthand form:
   * </p>
   *
   * <pre>
   * "A Stack (when empty)" should "be empty" in { ... }
   *                                          ^
   * </pre>
   *
   * <p>
   * This class also enables syntax such as the following ignored test registration
   * in shorthand form:
   * </p>
   *
   * <pre>
   * "A Stack (when empty)" should "be empty" ignore { ... }
   *                                          ^
   * </pre>
   *
   * <p>
   * This class is used via an implicit conversion (named <code>convertToInAndIgnoreMethods</code>)
   * from <code>ResultOfStringPassedToVerb</code>. The <code>ResultOfStringPassedToVerb</code> class
   * does not declare any methods named <code>in</code>, because the
   * type passed to <code>in</code> differs in a <code>FlatSpec</code> and a <code>FixtureFlatSpec</code>.
   * A <code>FixtureFlatSpec</code> needs two <code>in</code> methods, one that takes a no-arg
   * test function and another that takes a one-arg test function (a test that takes a
   * <code>Fixture</code> as its parameter). By constrast, a <code>FlatSpec</code> needs
   * only one <code>in</code> method that takes a by-name parameter. As a result,
   * <code>FlatSpec</code> and <code>FixtureFlatSpec</code> each provide an implicit conversion
   * from <code>ResultOfStringPassedToVerb</code> to a type that provides the appropriate
   * <code>in</code> methods.
   * </p>
   *
   * @author Bill Venners
   */
  protected final class InAndIgnoreMethods(resultOfStringPassedToVerb: ResultOfStringPassedToVerb) {

    import resultOfStringPassedToVerb.verb
    import resultOfStringPassedToVerb.rest

    /**
     * Supports the registration of no-arg tests in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * "A Stack" must "pop values in last-in-first-out order" in { () => ... }
     *                                                        ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="FixtureFlatSpec.html">main documentation</a>
     * for trait <code>FixtureFlatSpec</code>.
     * </p>
     */
    def in(testFun: () => Any) {
      registerTestToRun(verb + " " + rest, List(), new NoArgTestWrapper(testFun))
    }

    /**
     * Supports the registration of ignored, no-arg tests in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * "A Stack" must "pop values in last-in-first-out order" ignore { () => ... }
     *                                                        ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: () => Any) {
      registerTestToIgnore(verb + " " + rest, List(), new NoArgTestWrapper(testFun))
    }

    /**
     * Supports the registration of one-arg tests (tests that take a <code>Fixture</code> parameter) in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * "A Stack" must "pop values in last-in-first-out order" in { fixture => ... }
     *                                                        ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="FixtureFlatSpec.html">main documentation</a>
     * for trait <code>FixtureFlatSpec</code>.
     * </p>
     */
    def in(testFun: FixtureParam => Any) {
      registerTestToRun(verb + " " + rest, List(), testFun)
    }

    /**
     * Supports the registration of ignored, one-arg tests (tests that take a <code>Fixture</code> parameter) in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * "A Stack" must "pop values in last-in-first-out order" ignore { fixture => ... }
     *                                                        ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: FixtureParam => Any) {
      registerTestToIgnore(verb + " " + rest, List(), testFun)
    }
  }

  /**
   * Implicitly converts an object of type <code>ResultOfStringPassedToVerb</code> to an
   * <code>InAndIgnoreMethods</code>, to enable <code>in</code> and <code>ignore</code>
   * methods to be invokable on that object.
   */
  protected implicit def convertToInAndIgnoreMethods(resultOfStringPassedToVerb: ResultOfStringPassedToVerb) =
    new InAndIgnoreMethods(resultOfStringPassedToVerb)

  /**
   * Class that supports tagged test registration in shorthand form.
   *
   * <p>
   * For example, this class enables syntax such as the following tagged test registration
   * in shorthand form:
   * </p>
   *
   * <pre>
   * "A Stack (when empty)" should "be empty" taggedAs() in { ... }
   *                                                     ^
   * </pre>
   *
   * <p>
   * This class also enables syntax such as the following tagged, ignored test registration
   * in shorthand form:
   * </p>
   *
   * <pre>
   * "A Stack (when empty)" should "be empty" taggedAs(SlowTest) ignore { ... }
   *                                                             ^
   * </pre>
   *
   * <p>
   * This class is used via an implicit conversion (named <code>convertToInAndIgnoreMethodsAfterTaggedAs</code>)
   * from <code>ResultOfTaggedAsInvocation</code>. The <code>ResultOfTaggedAsInvocation</code> class
   * does not declare any methods named <code>in</code>, because the
   * type passed to <code>in</code> differs in a <code>FlatSpec</code> and a <code>FixtureFlatSpec</code>.
   * A <code>FixtureFlatSpec</code> needs two <code>in</code> methods, one that takes a no-arg
   * test function and another that takes a one-arg test function (a test that takes a
   * <code>Fixture</code> as its parameter). By constrast, a <code>FlatSpec</code> needs
   * only one <code>in</code> method that takes a by-name parameter. As a result,
   * <code>FlatSpec</code> and <code>FixtureFlatSpec</code> each provide an implicit conversion
   * from <code>ResultOfTaggedAsInvocation</code> to a type that provides the appropriate
   * <code>in</code> methods.
   * </p>
   *
   * @author Bill Venners
   */
  protected final class InAndIgnoreMethodsAfterTaggedAs(resultOfTaggedAsInvocation: ResultOfTaggedAsInvocation) {

    import resultOfTaggedAsInvocation.verb
    import resultOfTaggedAsInvocation.rest
    import resultOfTaggedAsInvocation.{tags => tagsList}

    /**
     * Supports the registration of tagged, no-arg tests in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * "A Stack" must "pop values in last-in-first-out order" taggedAs(SlowTest) in { () => ... }
     *                                                                           ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: () => Any) {
      registerTestToRun(verb + " " + rest, tagsList, new NoArgTestWrapper(testFun))
    }

    /**
     * Supports the registration of tagged, ignored, no-arg tests in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * "A Stack" must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { () => ... }
     *                                                                           ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * For examples of tagged test registration, see the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: () => Any) {
      registerTestToIgnore(verb + " " + rest, tagsList, new NoArgTestWrapper(testFun))
    }

    /**
     * Supports the registration of tagged, one-arg tests (tests that take a <code>Fixture</code> parameter) in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * "A Stack" must "pop values in last-in-first-out order" taggedAs(SlowTest) in { fixture => ... }
     *                                                                           ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def in(testFun: FixtureParam => Any) {
      registerTestToRun(verb + " " + rest, tagsList, testFun)
    }

    /**
     * Supports the registration of tagged, ignored, one-arg tests (tests that take a <code>Fixture</code> parameter) in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre>
     * "A Stack" must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { fixture => ... }
     *                                                                           ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="../FlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * For examples of tagged test registration, see the <a href="../FlatSpec.html#TaggingTests">Tagging tests section</a>
     * in the main documentation for trait <code>FlatSpec</code>.
     * </p>
     */
    def ignore(testFun: FixtureParam => Any) {
      registerTestToIgnore(verb + " " + rest, tagsList, testFun)
    }
  }

  /**
   * Implicitly converts an object of type <code>ResultOfTaggedAsInvocation</code> to an
   * <code>InAndIgnoreMethodsAfterTaggedAs</code>, to enable <code>in</code> and <code>ignore</code>
   * methods to be invokable on that object.
   */
  protected implicit def convertToInAndIgnoreMethodsAfterTaggedAs(resultOfTaggedAsInvocation: ResultOfTaggedAsInvocation) =
    new InAndIgnoreMethodsAfterTaggedAs(resultOfTaggedAsInvocation)

  /**
   * Supports the shorthand form of test registration.
   *
   * <p>
   * For example, this method enables syntax such as the following:
   * </p>
   *
   * <pre>
   * "A Stack (when empty)" should "be empty" in { ... }
   *                        ^
   * </pre>
   *
   * <p>
   * This function is passed as an implicit parameter to a <code>should</code> method
   * provided in <code>ShouldVerb</code>, a <code>must</code> method
   * provided in <code>MustVerb</code>, and a <code>can</code> method
   * provided in <code>CanVerb</code>. When invoked, this function registers the
   * subject description (the first parameter to the function) and returns a <code>ResultOfStringPassedToVerb</code>
   * initialized with the verb and rest parameters (the second and third parameters to
   * the function, respectively).
   * </p>
   */
  protected implicit val shorthandTestRegistrationFunction: (String, String, String) => ResultOfStringPassedToVerb = {
    (subject, verb, rest) => {
      behavior.of(subject)
      new ResultOfStringPassedToVerb(verb, rest) {
        def is(testFun: => PendingNothing) {
          registerTestToRun(verb + " " + rest, List(), unusedFixtureParam => testFun)
        }
        def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
          val tagList = firstTestTag :: otherTestTags.toList
          new ResultOfTaggedAsInvocation(verb, rest, tagList) {
            // "A Stack" must "test this" taggedAs(mytags.SlowAsMolasses) is (pending)
            //                                                            ^
            def is(testFun: => PendingNothing) {
              registerTestToRun(verb + " " + rest, tags, new NoArgTestWrapper(testFun _))
            }
          }
        }
      }
    }
  }

  // TODO: Get rid of unusedfixture, and use NoArgTestFunction instead

  /**
   * Supports the shorthand form of shared test registration.
   *
   * <p>
   * For example, this method enables syntax such as the following:
   * </p>
   *
   * <pre>
   * "A Stack (with one item)" should behave like nonEmptyStack(stackWithOneItem, lastValuePushed)
   *                           ^
   * </pre>
   *
   * <p>
   * This function is passed as an implicit parameter to a <code>should</code> method
   * provided in <code>ShouldVerb</code>, a <code>must</code> method
   * provided in <code>MustVerb</code>, and a <code>can</code> method
   * provided in <code>CanVerb</code>. When invoked, this function registers the
   * subject description (the  parameter to the function) and returns a <code>BehaveWord</code>.
   * </p>
   */
  protected implicit val shorthandSharedTestRegistrationFunction: (String) => BehaveWord = {
    (left) => {
      behavior.of(left)
      new BehaveWord
    }
  }

  /**
   * Register a test to ignore, which has the given spec text, optional tags, and test function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>execute</code>
   * methods. This method exists to make it easy to ignore an existing test by changing the call to <code>it</code>
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
  private def registerTestToIgnore(specText: String, testTags: List[Tag], testFun: FixtureParam => Any) {

    // TODO: This is how these were, but it needs attention. Mentions "it".
    registerIgnoredTest(specText, testFun, "ignoreCannotAppearInsideAnIt", "FixtureFlatSpec.scala", "ignore", testTags: _*)
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> tag names to which tests in this <code>Spec</code> belong, and values
   * the <code>Set</code> of test names that belong to each tag. If this <code>FlatSpec</code> contains no tags, this method returns an empty <code>Map</code>.
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

    runTestImpl(thisSuite, testName, reporter, stopper, configMap, tracker, true, invokeWithFixture)
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


    runTestsImpl(thisSuite, testName, reporter, stopper, filter, configMap, distributor, tracker, info, true, runTest)
  }

  /**
   * An immutable <code>Set</code> of test names. If this <code>FixtureFlatSpec</code> contains no tests, this method returns an
   * empty <code>Set</code>.
   *
   * <p>
   * This trait's implementation of this method will return a set that contains the names of all registered tests. The set's
   * iterator will return those names in the order in which the tests were registered. Each test's name is composed
   * of the concatenation of the text of each surrounding describer, in order from outside in, and the text of the
   * example itself, with all components separated by a space.
   * </p>
   */
  override def testNames: Set[String] = {
    // I'm returning a ListSet here so that they tests will be run in registration order
    ListSet(atomic.get.testNamesList.toArray: _*)
  }

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {


    runImpl(thisSuite, testName, reporter, stopper, filter, configMap, distributor, tracker, super.run)
  }

  /**
   * Supports shared test registration in <code>FixtureFlatSpec</code>s.
   *
   * <p>
   * This field supports syntax such as the following:
   * </p>
   *
   * <pre>
   * it should behave like nonFullStack(stackWithOneItem)
   *           ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of <code>behave</code>, see the <a href="../FlatSpec.html#SharedTests">Shared tests section</a>
   * in the main documentation for trait <code>FlatSpec</code>.
   * </p>
   */
  protected val behave = new BehaveWord
}

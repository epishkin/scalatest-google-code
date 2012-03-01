package org.scalatest.path

import org.scalatest.Suite
import org.scalatest.OneInstancePerTest
import org.scalatest.Reporter
import org.scalatest.Stopper
import org.scalatest.Filter
import org.scalatest.Tracker
import org.scalatest.Distributor
import org.scalatest.PathEngine
import org.scalatest.Informer
import org.scalatest.Tag
import org.scalatest.verb.BehaveWord
import scala.collection.immutable.ListSet
import org.scalatest.PathEngine.isInTargetPath
import org.scalatest.Style

/**
 * A sister trait to <code>org.scalatest.FunSpec</code> that isolates tests by running each test in its own
 * instance of the test class, and for each test, only executing the <em>path</em> leading to that test.
 *
 * <p>
 * Trait <code>path.FunSpec</code> behaves similarly to trait <code>org.scalatest.FunSpec</code>, except that tests
 * are isolated based on their path. The purpose of <code>path.FunSpec</code> is to facilitate writing
 * specification-style tests for mutable objects in a clear, boilerpate-free way. To test mutable objects, you need to
 * mutate them. Using a path trait, you can make a statement in text, then implement that statement in code (including
 * mutating state), and nest and combine these test/code pairs in any way you wish. Each test will only see
 * the side effects of code that is in blocks that enclose the test. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.path
 * import org.scalatest.matchers.ShouldMatchers
 * import scala.collection.mutable.ListBuffer
 *
 * class ExampleSpec extends path.FunSpec with ShouldMatchers {
 *
 *   describe("A ListBuffer") {
 *
 *     val buf = ListBuffer.empty[Int] // This implements "A ListBuffer"
 *
 *     it("should be empty when created") {
 *
 *       // This test sees:
 *       //   val buf = ListBuffer.empty[Int]
 *       // So buf is: ListBuffer()
 *
 *       buf should be ('empty)
 *     }
 *
 *     describe("when 1 is appended") {
 *
 *       buf += 1 // This implements "when 1 is appended", etc...
 *
 *       it("should contain 1") {
 *
 *         // This test sees:
 *         //   val buf = ListBuffer.empty[Int]
 *         //   buf += 1
 *         // So buf is: ListBuffer(1)
 *
 *         buf.remove(0) should equal (1)
 *         buf should be ('empty)
 *       }
 *
 *       describe("when 2 is appended") {
 *
 *         buf += 2
 *
 *         it("should contain 1 and 2") {
 *
 *           // This test sees:
 *           //   val buf = ListBuffer.empty[Int]
 *           //   buf += 1
 *           //   buf += 2
 *           // So buf is: ListBuffer(1, 2)
 *
 *           buf.remove(0) should equal (1)
 *           buf.remove(0) should equal (2)
 *           buf should be ('empty)
 *         }
 *
 *         describe("when 2 is removed") {
 *
 *           buf -= 2
 *
 *           it("should contain only 1 again") {
 *
 *             // This test sees:
 *             //   val buf = ListBuffer.empty[Int]
 *             //   buf += 1
 *             //   buf += 2
 *             //   buf -= 2
 *             // So buf is: ListBuffer(1)
 *
 *             buf.remove(0) should equal (1)
 *             buf should be ('empty)
 *           }
 *         }
 *
 *         describe("when 3 is appended") {
 *
 *           buf += 3
 *
 *           it("should contain 1, 2, and 3") {
 *
 *             // This test sees:
 *             //   val buf = ListBuffer.empty[Int]
 *             //   buf += 1
 *             //   buf += 2
 *             //   buf += 3
 *             // So buf is: ListBuffer(1, 2, 3)
 *
 *             buf.remove(0) should equal (1)
 *             buf.remove(0) should equal (2)
 *             buf.remove(0) should equal (3)
 *             buf should be ('empty)
 *           }
 *         }
 *       }
 *
 *       describe("when 88 is appended") {
 *
 *         buf += 88
 *
 *         it("should contain 1 and 88") {
 *
 *           // This test sees:
 *           //   val buf = ListBuffer.empty[Int]
 *           //   buf += 1
 *           //   buf += 88
 *           // So buf is: ListBuffer(1, 88)
 *
 *           buf.remove(0) should equal (1)
 *           buf.remove(0) should equal (88)
 *           buf should be ('empty)
 *         }
 *       }
 *     }
 *
 *     it("should have size 0 when created") {
 *
 *       // This test sees:
 *       //   val buf = ListBuffer.empty[Int]
 *       // So buf is: ListBuffer()
 *
 *       buf should have size 0
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * Note that the above class is organized by writing a bit of specification text that opens a new block followed
 * by, at the top of the new block, some code that "implements" or "performs" what is described in the text. This is repeated as
 * the mutable object (here, a <code>ListBuffer</code>), is prepared for the enclosed tests. For example:
 * <p>
 *
 * <pre class="stHighlight">
 * describe("A ListBuffer") {
 *   val buf = ListBuffer.empty[Int]
 * </pre>
 *
 * <p>
 * Or:
 * </p>
 *
 * <pre class="stHighlight">
 * describe("when 2 is appended") {
 *   buf += 2
 * </pre>
 *
 * <p>
 * Note also that although each test mutates the <code>ListBuffer</code>, none of the other tests observe those
 * side effects:
 * <p>
 *
 * <pre class="stHighlight">
 * it("should contain 1") {
 *
 *   buf.remove(0) should equal (1)
 *   // ...
 * }
 *
 * describe("when 2 is appended") {
 *
 *   buf += 2
 *
 *   it("should contain 1 and 2") {
 *
 *     // This test does not see the buf.remove(0) from the previous test,
 *     // so the first element in the ListBuffer is again 1
 *     buf.remove(0) should equal (1)
 *     buf.remove(0) should equal (2)
 * </pre>
 *
 * <p>
 * This kind of isolation of tests from each other is a consequence of running each test in its own instance of the test
 * class, and can also be achieved by simply mixing <code>OneInstancePerTest</code> into a regular
 * <code>org.scalatest.FunSpec</code>. However, <code>path.FunSpec</code> takes isolation one step further: a test
 * in a <code>path.FunSpec</code> does not observe side effects performed outside tests in earlier blocks that do not
 * enclose it. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * describe("when 2 is removed") {
 *
 *   buf -= 2
 *
 *   // ...
 * }
 *
 * describe("when 3 is appended") {
 *
 *   buf += 3
 *
 *   it("should contain 1, 2, and 3") {
 *
 *     // This test does not see the buf -= 2 from the earlier "when 2 is removed" block,
 *     // because that block does not enclose this test, so the second element in the
 *     // ListBuffer is still 2
 *     buf.remove(0) should equal (1)
 *     buf.remove(0) should equal (2)
 *     buf.remove(0) should equal (3)
 * </pre>
 *
 * <p>
 * Running the full <code>ExampleSpec</code>, shown above, in the Scala interpeter would give you:
 * </p>
 *
 * <pre class="stREPL">
 * scala> import org.scalatest._
 * import org.scalatest._
 *
 * scala> run(new ExampleSpec)
 * <span class="stGreen">ExampleSpec:
 * A ListBuffer
 * - should be empty when created
 * &nbsp; when 1 is appended
 * &nbsp; - should contain 1
 * &nbsp;   when 2 is appended
 * &nbsp;   - should contain 1 and 2
 * &nbsp;     when 2 is removed
 * &nbsp;     - should contain only 1 again
 * &nbsp;     when 3 is appended
 * &nbsp;     - should contain 1, 2, and 3
 * &nbsp;   when 88 is appended
 * &nbsp;   - should contain 1 and 88
 * - should have size 0 when created</span>
 * </pre>
 *
 * <p>
 * <em>Note: trait <code>path.FunSpec</code>'s approach to isolation was inspired in part by the
 * <a href="https://github.com/orfjackal/specsy">specsy</a> framework, written by Esko Luontola.</em>
 * </p>
 *
 * <a name="sharedFixtures"></a><h2>Shared fixtures</h2>
 *
 * <p>
 * A test <em>fixture</em> is objects or other artifacts (such as files, sockets, database
 * connections, <em>etc.</em>) used by tests to do their work.
 * If a fixture is used by only one test, then the definitions of the fixture objects can
 * be local to the method. If multiple tests need to share an immutable fixture, you can simply
 * assign them to instance variables. If multiple tests need to share mutable fixture objects or <code>var</code>s,
 * there's one and only one way to do it in a <code>path.FunSpec</code>: place the mutable objects lexically before
 * the test. Any mutations needed by the test must be placed lexically before and/or after the test.
 * As used here, "Lexically before" means that the code needs to be executed during construction of that test's
 * instance of the test class to <em>reach</em> the test (or put another way, the
 * code is along the "path to the test.") "Lexically after" means that the code needs to be executed to exit the
 * constructor after the test has been executed.
 * </p>
 *
 * <p>
 * The reason lexical placement is the one and only one way to share fixtures in a <code>path.FunSpec</code> is because
 * all of its lifecycle methods are overridden and declared <code>final</code>. Thus you can't override
 * <code>withFixture</code>, because it is <code>final</code>, or mix in <code>BeforeAndAfter</code> or
 * <code>BeforeAndAfterEach</code>, because both override <code>runTest</code>, which is <code>final</code> in
 * a <code>path.FunSpec</code>. In short:
 * </p>
 *
 * <p>
 * <table style="border-collapse: collapse; border: 1px solid black; width: 70%; margin: auto">
 * <tr>
 * <th style="background-color: #CCCCCC; border-width: 1px; padding: 15px; text-align: left; border: 1px solid black; font-size: 125%; font-weight: bold">
 * In a <code>path.FunSpec</code>, if you need some code to execute before a test, place that code lexically before
 * the test. If you need some code to execute after a test, place that code lexically after the test.
 * </th>
 * </tr>
 * </table>
 * </p>
 *
 * <p>
 * The reason the life cycle methods are final, by the way, is to prevent users from attempting to combine
 * a <code>path.FunSpec</code>'s approach to isolation with other ways ScalaTest provides to share fixtures or
 * execute tests, because doing so could make the resulting test code hard to reason about. A
 * <code>path.FunSpec</code>'s execution model is a bit magical, but because it executes in one and only one
 * way, users should be able to reason about the code.
 * To help you visualize how a <code>path.FunSpec</code> is executed, consider the following variant of
 * <code>ExampleSpec</code> that includes print statements:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.path
 * import org.scalatest.matchers.ShouldMatchers
 * import scala.collection.mutable.ListBuffer
 *
 * class ExampleSpec extends path.FunSpec with ShouldMatchers {
 *
 *   println("Start of: ExampleSpec")
 *   describe("A ListBuffer") {
 *
 *     println("Start of: A ListBuffer")
 *     val buf = ListBuffer.empty[Int]
 *
 *     it("should be empty when created") {
 *
 *       println("In test: should be empty when created; buf is: " + buf)
 *       buf should be ('empty)
 *     }
 *
 *     describe("when 1 is appended") {
 *
 *       println("Start of: when 1 is appended")
 *       buf += 1
 *
 *       it("should contain 1") {
 *
 *         println("In test: should contain 1; buf is: " + buf)
 *         buf.remove(0) should equal (1)
 *         buf should be ('empty)
 *       }
 *
 *       describe("when 2 is appended") {
 *
 *         println("Start of: when 2 is appended")
 *         buf += 2
 *
 *         it("should contain 1 and 2") {
 *
 *           println("In test: should contain 1 and 2; buf is: " + buf)
 *           buf.remove(0) should equal (1)
 *           buf.remove(0) should equal (2)
 *           buf should be ('empty)
 *         }
 *
 *         describe("when 2 is removed") {
 *
 *           println("Start of: when 2 is removed")
 *           buf -= 2
 *
 *           it("should contain only 1 again") {
 *
 *             println("In test: should contain only 1 again; buf is: " + buf)
 *             buf.remove(0) should equal (1)
 *             buf should be ('empty)
 *           }
 *
 *           println("End of: when 2 is removed")
 *         }
 *
 *         describe("when 3 is appended") {
 *
 *           println("Start of: when 3 is appended")
 *           buf += 3
 *
 *           it("should contain 1, 2, and 3") {
 *
 *             println("In test: should contain 1, 2, and 3; buf is: " + buf)
 *             buf.remove(0) should equal (1)
 *             buf.remove(0) should equal (2)
 *             buf.remove(0) should equal (3)
 *             buf should be ('empty)
 *           }
 *           println("End of: when 3 is appended")
 *         }
 *
 *         println("End of: when 2 is appended")
 *       }
 *
 *       describe("when 88 is appended") {
 *
 *         println("Start of: when 88 is appended")
 *         buf += 88
 *
 *         it("should contain 1 and 88") {
 *
 *           println("In test: should contain 1 and 88; buf is: " + buf)
 *           buf.remove(0) should equal (1)
 *           buf.remove(0) should equal (88)
 *           buf should be ('empty)
 *         }
 *
 *         println("End of: when 88 is appended")
 *       }
 *
 *       println("End of: when 1 is appended")
 *     }
 *
 *     it("should have size 0 when created") {
 *
 *       println("In test: should have size 0 when created; buf is: " + buf)
 *       buf should have size 0
 *     }
 *
 *     println("End of: A ListBuffer")
 *   }
 *   println("End of: ExampleSpec")
 *   println()
 * }
 * </pre>
 *
 * <p>
 * Running the above version of <code>ExampleSpec</code> in the Scala interpreter will give you output similar to:
 * </p>
 *
 * <pre class="stREPL">
 * scala> import org.scalatest._
 * import org.scalatest._
 *
 * scala> run(new ExampleSpec)
 * <span class="stGreen">ExampleSpec:</span>
 * Start of: ExampleSpec
 * Start of: A ListBuffer
 * In test: should be empty when created; buf is: ListBuffer()
 * End of: A ListBuffer
 * End of: ExampleSpec
 *
 * Start of: ExampleSpec
 * Start of: A ListBuffer
 * Start of: when 1 is appended
 * In test: should contain 1; buf is: ListBuffer(1)
 * ExampleSpec:
 * End of: when 1 is appended
 * End of: A ListBuffer
 * End of: ExampleSpec
 *
 * Start of: ExampleSpec
 * Start of: A ListBuffer
 * Start of: when 1 is appended
 * Start of: when 2 is appended
 * In test: should contain 1 and 2; buf is: ListBuffer(1, 2)
 * End of: when 2 is appended
 * End of: when 1 is appended
 * End of: A ListBuffer
 * End of: ExampleSpec
 *
 * Start of: ExampleSpec
 * Start of: A ListBuffer
 * Start of: when 1 is appended
 * Start of: when 2 is appended
 * Start of: when 2 is removed
 * In test: should contain only 1 again; buf is: ListBuffer(1)
 * End of: when 2 is removed
 * End of: when 2 is appended
 * End of: when 1 is appended
 * End of: A ListBuffer
 * End of: ExampleSpec
 *
 * Start of: ExampleSpec
 * Start of: A ListBuffer
 * Start of: when 1 is appended
 * Start of: when 2 is appended
 * Start of: when 3 is appended
 * In test: should contain 1, 2, and 3; buf is: ListBuffer(1, 2, 3)
 * End of: when 3 is appended
 * End of: when 2 is appended
 * End of: when 1 is appended
 * End of: A ListBuffer
 * End of: ExampleSpec
 *
 * Start of: ExampleSpec
 * Start of: A ListBuffer
 * Start of: when 1 is appended
 * Start of: when 88 is appended
 * In test: should contain 1 and 88; buf is: ListBuffer(1, 88)
 * End of: when 88 is appended
 * End of: when 1 is appended
 * End of: A ListBuffer
 * End of: ExampleSpec
 *
 * Start of: ExampleSpec
 * Start of: A ListBuffer
 * In test: should have size 0 when created; buf is: ListBuffer()
 * End of: A ListBuffer
 * End of: ExampleSpec
 *
 * <span class="stGreen">A ListBuffer
 * - should be empty when created
 *   when 1 is appended
 * &nbsp; - should contain 1
 * &nbsp;   when 2 is appended
 * &nbsp;   - should contain 1 and 2
 * &nbsp;     when 2 is removed
 * &nbsp;     - should contain only 1 again
 * &nbsp;     when 3 is appended
 * &nbsp;     - should contain 1, 2, and 3
 * &nbsp;   when 88 is appended
 * &nbsp;   - should contain 1 and 88
 * - should have size 0 when created</span>
 * </pre>
 *
 * <p>
 * Note that each test is executed in order of appearance in the <code>path.FunSpec</code>, and that only
 * those <code>println</code> statements residing in blocks that enclose the test being run are executed. Any
 * <code>println</code> statements in blocks that do not form the "path" to a test are not executed in the
 * instance of the class that executes that test.
 * </p>
 *
 * <a name="howItExecutes" />
 * <h2>How it executes</h2>
 *
 * <p>
 * To provide its special brand of test isolation, <code>path.FunSpec</code> executes quite differently from its
 * sister trait in <code>org.scalatest</code>. An <code>org.scalatest.FunSpec</code>
 * registers tests during construction and executes them when <code>run</code> is invoked. An
 * <code>org.scalatest.path.FunSpec</code>, by contrast, runs each test in its own instance <em>while that
 * instance is being constructed</em>. During construction, it registers not the tests to run, but the results of
 * running those tests. When <code>run</code> is invoked on a <code>path.FunSpec</code>, it reports the registered
 * results and does not run the tests again. If <code>run</code> is invoked a second or third time, in fact,
 * a <code>path.FunSpec</code> will each time report the same results registered during construction. If you want
 * to run the tests of a <code>path.FunSpec</code> anew, you'll need to create a new instance and invoke
 * <code>run</code> on that.
 * <p>
 *
 * <p>
 * A <code>path.FunSpec</code> will create one instance for each "leaf" node it contains. The main kind of leaf node is
 * a test, such as:
 * </p>
 *
 * <pre class="stHighlight">
 * // One instance will be created for each test
 * it("should be empty when created") {
 *   buf should be ('empty)
 * }
 * </pre>
 *
 * <p>
 * However, an empty scope (a scope that contains no tests or nested scopes) is also a leaf node:
 * </p>
 *
 * <pre class="stHighlight">
 *  // One instance will be created for each empty scope
 * describe("when 99 is added") {
 *   // A scope is "empty" and therefore a leaf node if it has no
 *   // tests or nested scopes, though it may have other code (which
 *   // will be executed in the instance created for that leaf node)
 *   buf += 99
 * }
 * </pre>
 *
 * <p>
 * The tests will be executed sequentially, in the order of appearance. The first test (or empty scope,
 * if that is first) will be executed when a class that mixes in <code>path.FunSpec</code> is
 * instantiated. Only the first test will be executed during this initial instance, and of course, only
 * the path to that test. Then, the first time the client uses the initial instance (by invoking one of <code>run</code>,
 * <code>expectedTestsCount</code>, <code>tags</code>, or <code>testNames</code> on the instance), the initial instance will,
 * before doing anything else, ensure that any remaining tests are executed, each in its own instance.
 * </p>
 *
 * <p>
 * To ensure that the correct path is taken in each instance, and to register its test results, the initial
 * <code>path.FunSpec</code> instance must communicate with the other instances it creates for running any subsequent
 * leaf nodes. It does so by setting a thread-local variable prior to creating each instance (a technique
 * suggested by Esko Luontola). Each instance
 * of <code>path.FunSpec</code> checks the thread-local variable. If the thread-local is not set, it knows it
 * is an initial instance and therefore executes every block it encounters until it discovers, and executes the
 * first test (or empty scope, if that's the first leaf node). It then discovers, but does not execute the next
 * leaf node, or discovers there are no other leaf nodes remaining to execute. It communicates the path to the next
 * leaf node, if any, and the result of running the test it did execute, if any, back to the initial instance. The
 * initial instance repeats this process until all leaf nodes have been executed and all test results registered.
 * </p>
 *
 * <a name="ignoredTests" />
 * <h2>Ignored tests</h2>
 *
 * <p>
 * You mark a test as ignored in an <code>org.scalatest.path.FunSpec</code> in the same manner as in
 * an <code>org.scalatest.FunSpec</code>. Please see the <a href="../FunSpec.html#ignoredTests">Ignored tests</a> section
 * in its documentation for more information.
 * </p>
 *
 * <p>
 * Note that a separate instance will be created for an ignored test,
 * and the path to the ignored test will be executed in that instance, but the test function itself will not
 * be executed. Instead, a <code>TestIgnored</code> event will be fired.
 * </p>
 *
 * <a name="informers" />
 * <h2>Informers</h2>
 *
 * <p>
 * You output information using <code>Informer</code>s in an <code>org.scalatest.path.FunSpec</code> in the same manner
 * as in an <code>org.scalatest.FunSpec</code>. Please see the <a href="../FunSpec.html#informers">Informers</a>
 * section in its documentation for more information.
 * </p>
 *
 * <a name="pendingTests" />
 * <h2>Pending tests</h2>
 *
 * <p>
 * You mark a test as pending in an <code>org.scalatest.path.FunSpec</code> in the same manner as in
 * an <code>org.scalatest.FunSpec</code>. Please see the <a href="../FunSpec.html#pendingTests">Pending tests</a>
 * section in its documentation for more information.
 * </p>
 * 
 * <p>
 * Note that a separate instance will be created for a pending test,
 * and the path to the ignored test will be executed in that instance, as well as the test function (up until it
 * completes abruptly with a <code>TestPendingException</code>).
 * </p>
 *
 * <a name="taggingTests" />
 * <h2>Tagging tests</h2>
 *
 * <p>
 * You can place tests into groups by tagging them in an <code>org.scalatest.path.FunSpec</code> in the same manner
 * as in an <code>org.scalatest.FunSpec</code>. Please see the <a href="../FunSpec.html#taggingTests">Tagging tests</a>
 * section in its documentation for more information.
 * </p>
 *
 * <p>
 * Note that one difference between this trait and its sister trait
 * <code>org.scalatest.FunSpec</code> is that because tests are executed at construction time, rather than each
 * time run is invoked, an <code>org.scalatest.path.FunSpec</code> will always execute all non-ignored tests. When
 * <code>run</code> is invoked on a <code>path.FunSpec</code>, if some tests are excluded based on tags, the registered
 * results of running those tests will not be reported. (But those tests will have already run and the results
 * registered.) By contrast, because an <code>org.scalatest.FunSpec</code> only executes tests after <code>run</code>
 * has been called, and at that time the tags to include and exclude are known, only tests selected by the tags
 * will be executed.
 * </p>
 * 
 * <p>
 * In short, in an <code>org.scalatest.FunSpec</code>, tests not selected by the tags to include
 * and exclude specified for the run (via the <code>Filter</code> passed to <code>run</code>) will not be executed.
 * In an <code>org.scalatest.path.FunSpec</code>, by contrast, all non-ignored tests will be executed, each
 * during the construction of its own instance, and tests not selected by the tags to include and exclude specified
 * for a run will not be reported. (One upshot of this is that if you have tests that you want to tag as being slow so
 * you can sometimes exclude them during a run, you probably don't want to put them in a <code>path.FunSpec</code>. Because
 * in a <code>path.Freespec</code> the slow tests will be run regardless, with only their registered results not being <em>reported</em>
 * if you exclude slow tests during a run.)
 * </p>
 *
 * <a name="SharedTests"></a><h2>Shared tests</h2>
 * <p>
 * You can factor out shared tests in an <code>org.scalatest.path.FunSpec</code> in the same manner as in
 * an <code>org.scalatest.FunSpec</code>. Please see the <a href="../FunSpec.html#SharedTests">Shared tests</a>
 * section in its documentation for more information.
 * </p>
 *
 * <a name="nestedSuites"></a><h2>Nested suites</h2>
 *
 * <p>
 * Nested suites are not allowed in a <code>path.FunSpec</code>. Because
 * a <code>path.FunSpec</code> executes tests eagerly at construction time, registering the results of those test runs
 * and reporting them later when <code>run</code> is invoked, the order of nested suites versus test runs would be
 * different in a <code>org.scalatest.path.FunSpec</code> than in an <code>org.scalatest.FunSpec</code>. In
 * <code>org.scalatest.FunSpec</code>'s implementation of <code>run</code>, nested suites are executed then tests
 * are executed. A <code>org.scalatest.path.FunSpec</code> with nested suites would execute these in the opposite
 * order: first tests then nested suites. To help make <code>path.FunSpec</code> code easier to
 * reason about by giving readers of one less difference to think about, nested suites are not allowed. If you want
 * to add nested suites to a <code>path.FunSpec</code>, you can instead wrap them all in a
 * <a href="../Suites.html"><code>Suites</code></a> or <a href="../Specs.html"><code>Specs</code></a> object. They will
 * be executed in the order of appearance (unless a <a href="../Distributor">Distributor</a> is passed, in which case
 * they will execute in parallel).
 * </p>

 * </p>
 *
 * <a name="durations"></a><h2>Durations</h2>
 * <p>
 * Many ScalaTest events include a duration that indicates how long the event being reported took to execute. For
 * example, a <code>TestSucceeded</code> event provides a duration indicating how long it took for that test
 * to execute. A <code>SuiteCompleted</code> event provides a duration indicating how long it took for that entire
 * suite of tests to execute.
 * </p>
 *
 * <p>
 * In the test completion events fired by a <code>path.FunSpec</code> (<code>TestSucceeded</code>,
 * <code>TestFailed</code>, or <code>TestPending</code>), the durations reported refer
 * to the time it took for the tests to run. This time is registered with the test results and reported along
 * with the test results each time <code>run</code> is invoked.
 * By contrast, the suite completion events fired for a <code>path.FunSpec</code> represent the amount of time
 * it took to report the registered results. (These events are not fired by <code>path.FunSpec</code>, but instead
 * by the entity that invokes <code>run</code> on the <code>path.FunSpec</code>.) As a result, the total time
 * for running the tests of a <code>path.FunSpec</code>, calculated by summing the durations of all the individual
 * test completion events, may be greater than the duration reported for executing the entire suite.
 * </p>
 *
 * @author Bill Venners
 * @author Chua Chee Seng
 */
@Style("org.scalatest.finders.FunSpecFinder")
trait FunSpec extends org.scalatest.Suite with OneInstancePerTest { thisSuite =>
  
  private final val engine = PathEngine.getEngine()
  import engine._

  private final val stackDepth = 3 // TODO: Minimize this state. 

  override def newInstance = this.getClass.newInstance.asInstanceOf[FunSpec]

  /**
   * Returns an <code>Informer</code> that during test execution will forward strings (and other objects) passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor (including within a test, since
   * those are invoked during construction in a <code>path.FunSpec</code>, it
   * will register the passed string for forwarding later when <code>run</code> is invoked. If invoked at any other
   * time, it will throw an exception. This method can be called safely by any thread.
   */
  implicit protected def info: Informer = atomicInformer.get

  /**
   * Class that, via an instance referenced from the <code>it</code> field,
   * supports test (and shared test) registration in <code>FunSpec</code>s.
   *
   * <p>
   * This class supports syntax such as the following test registration:
   * </p>
   *
   * <pre class="stExamples">
   * it("should be empty")
   * ^
   * </pre>
   *
   * <p>
   * and the following shared test registration:
   * </p>
   *
   * <pre class="stExamples">
   * it should behave like nonFullStack(stackWithOneItem)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples, see the <a href="FunSpec.html">main documentation for <code>path.FunSpec</code></a>.
   * </p>
   */
  protected class ItWord {

    /**
     * Supports test registration.
     *
     * <p>
     * This trait's implementation of this method will decide whether to register the text and invoke the passed function
     * based on whether or not this is part of the current "test path." For the details on this process, see
     * the <a href="#howItExecutes">How it executes</a> section of the main documentation for
     * trait <code>org.scalatest.path.FunSpec</code>.
     * </p>
     *
     * @param testText the test text, which will be combined with the descText of any surrounding describers
     * to form the test name
     * @param testTags the optional list of tags for this test
     * @param testFun the test function
     * @throws DuplicateTestNameException if a test with the same name has been registered previously
     * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
     * @throws NullPointerException if <code>specText</code> or any passed test tag is <code>null</code>
     */
    def apply(testText: String, testTags: Tag*)(testFun: => Unit) {
      handleTest(thisSuite, testText, testFun _, "itCannotAppearInsideAnotherIt", "FunSpec.scala", "apply", stackDepth, testTags: _*)
    }
    
    /**
     * Supports the registration of shared tests.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stExamples">
     * it should behave like nonFullStack(stackWithOneItem)
     *    ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="../FunSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>org.scalatest.FunSpec</code>.
     * </p>
     */
    def should(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stExamples">
     * it must behave like nonFullStack(stackWithOneItem)
     *    ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="../FunSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>org.scalatest.FunSpec</code>.
     * </p>
     */
    def must(behaveWord: BehaveWord) = behaveWord
  }

  /**
   * Supports test (and shared test) registration in <code>FunSpec</code>s.
   *
   * <p>
   * This field supports syntax such as the following:
   * </p>
   *
   * <pre class="stExamples">
   * it("should be empty")
   * ^
   * </pre>
   *
   * <pre> class="stExamples"
   * it should behave like nonFullStack(stackWithOneItem)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the main documentation for this trait.
   * </p>
   */
  protected val it = new ItWord

  /**
   * Supports registration of a test to ignore.
   *
   * <p>
   * For more information and examples of this method's use, see the
   * <a href="../FunSpec.html#ignoredTests">Ignored tests</a> section in the main documentation for sister
   * trait <code>org.scalatest.FunSpec</code>. Note that a separate instance will be created for an ignored test,
   * and the path to the ignored test will be executed in that instance, but the test function itself will not
   * be executed. Instead, a <code>TestIgnored</code> event will be fired.
   * </p>
   *
   * @param testText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testTags the optional list of tags for this test
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullPointerException if <code>specText</code> or any passed test tag is <code>null</code>
   */
  protected def ignore(testText: String, testTags: Tag*)(testFun: => Unit) {
    // Might not actually register it. Only will register it if it is its turn.
    handleIgnoredTest(testText, testFun _, "ignoreCannotAppearInsideAnIt", "FunSpec.scala", "ignore", stackDepth + 1, testTags: _*)
  }
  
  /**
   * Describe a &#8220;subject&#8221; being specified and tested by the passed function value. The
   * passed function value may contain more describers (defined with <code>describe</code>) and/or tests
   * (defined with <code>it</code>).
   *
   * <p>
   * This class's implementation of this method will decide whether to
   * register the description text and invoke the passed function
   * based on whether or not this is part of the current "test path." For the details on this process, see
   * the <a href="#howItExecutes">How it executes</a> section of the main documentation for trait
   * <code>org.scalatest.path.FunSpec</code>.
   * </p>
   */
  protected def describe(description: String)(fun: => Unit) {
    handleNestedBranch(description, None, fun, "describeCannotAppearInsideAnIt", "FunSpec.scala", "describe", stackDepth + 1)
  }
  
  /**
   * Supports shared test registration in <code>path.FunSpec</code>s.
   *
   * <p>
   * This field supports syntax such as the following:
   * </p>
   *
   * <pre class="stExamples">
   * it should behave like nonFullStack(stackWithOneItem)
   *           ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of <cod>behave</code>, see the
   * <a href="../FunSpec.html#SharedTests">Shared tests</a> section in the main documentation for sister
   * trait <code>org.scalatest.FunSpec</code>.
   * </p>
   */
  protected val behave = new BehaveWord

  /**
   * This lifecycle method is unused by this trait, and will complete abruptly with
   * <code>UnsupportedOperationException</code> if invoked.
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   */
  final override def withFixture(test: NoArgTest) {
    throw new UnsupportedOperationException
  }

  /**
   * An immutable <code>Set</code> of test names. If this <code>FunSpec</code> contains no tests, this method returns an
   * empty <code>Set</code>.
   *
   * <p>
   * This trait's implementation of this method will first ensure that the results of all tests, each run its its
   * own instance executing only the path to the test, are registered. For details on this process see the
   * <a href="#howItExecutes">How it executes</a> section in the main documentation for this trait.
   * </p>
   *
   * <p>
   * This trait's implementation of this method will return a set that contains the names of all registered tests. The set's
   * iterator will return those names in the order in which the tests were registered. Each test's name is composed
   * of the concatenation of the text of each surrounding describer, in order from outside in, and the text of the
   * example itself, with all components separated by a space. For example, consider this <code>FunSpec</code>:
   * </p>
   *
   * <pre class="stHighlight">
   * import org.scalatest.path
   *
   * class StackSpec extends path.FunSpec {
   *   describe("A Stack") {
   *     describe("when not empty") {
   *       "must allow me to pop" in {}
   *     }
   *     describe("when not full") {
   *       "must allow me to push" in {}
   *     }
   *   }
   * }
   * </pre>
   *
   * <p>
   * Invoking <code>testNames</code> on this <code>FunSpec</code> will yield a set that contains the following
   * two test name strings:
   * </p>
   *
   * <pre>
   * "A Stack when not empty must allow me to pop"
   * "A Stack when not full must allow me to push"
   * </pre>
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   */
  final override def testNames: Set[String] = {
    ensureTestResultsRegistered(thisSuite)
    // I'm returning a ListSet here so that they tests will be run in registration order
    ListSet(atomic.get.testNamesList.toArray: _*)
  }

  /**
   * The total number of tests that are expected to run when this <code>path.FunSpec</code>'s <code>run</code> method
   * is invoked.
   *
   * <p>
   * This trait's implementation of this method will first ensure that the results of all tests, each run its its
   * own instance executing only the path to the test, are registered. For details on this process see the
   * <a href="#howItExecutes">How it executes</a> section in the main documentation for this trait.
   * </p>
   *
   * <p>
   * This trait's implementation of this method returns the size of the <code>testNames</code> <code>List</code>, minus
   * the number of tests marked as ignored as well as any tests excluded by the passed <code>Filter</code>.
   * </p>
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   *
   * @param filter a <code>Filter</code> with which to filter tests to count based on their tags
   */
  final override def expectedTestCount(filter: Filter): Int = {
    ensureTestResultsRegistered(thisSuite)
    super.expectedTestCount(filter)
  }

  /**
   * Runs a test.
   *
   * <p>
   * This trait's implementation of this method will first ensure that the results of all tests, each run its its
   * own instance executing only the path to the test, are registered. For details on this process see the
   * <a href="#howItExecutes">How it executes</a> section in the main documentation for this trait.
   * </p>
   *
   * <p>
   * This trait's implementation reports the test results registered with the name specified by
   * <code>testName</code>. Each test's name is a concatenation of the text of all describers surrounding a test,
   * from outside in, and the test's  spec text, with one space placed between each item. (See the documentation
   * for <code>testNames</code> for an example.)
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   *
   * @param testName the name of one test to execute.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param configMap a <code>Map</code> of properties that can be used by this <code>FreeSpec</code>'s executing tests.
   * @throws NullPointerException if any of <code>testName</code>, <code>reporter</code>, <code>stopper</code>, or <code>configMap</code>
   *     is <code>null</code>.
   */
  final protected override def runTest(testName: String, reporter: Reporter, stopper: Stopper, configMap: Map[String, Any], tracker: Tracker) {

    ensureTestResultsRegistered(thisSuite)
    
    def dontInvokeWithFixture(theTest: TestLeaf) {
      theTest.testFun()
    }

    runTestImpl(thisSuite, testName, reporter, stopper, configMap, tracker, true, dontInvokeWithFixture)
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> tag names to which tests in this <code>path.FreeSpec</code>
   * belong, and values the <code>Set</code> of test names that belong to each tag. If this <code>path.FreeSpec</code>
   * contains no tags, this method returns an empty <code>Map</code>.
   *
   * <p>
   * This trait's implementation of this method will first ensure that the results of all tests, each run its its
   * own instance executing only the path to the test, are registered. For details on this process see the
   * <a href="#howItExecutes">How it executes</a> section in the main documentation for this trait.
   * </p>
   *
   * <p>
   * This trait's implementation returns tags that were passed as strings contained in <code>Tag</code> objects passed
   * to methods <code>it</code> and <code>ignore</code>.
   * </p>
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   */
  final override def testTags: Map[String, Set[String]] = {
    ensureTestResultsRegistered(thisSuite)
    atomic.get.tagsMap
  }

  /**
   * Runs this <code>path.FunSpec</code>, reporting test results that were registered when the tests
   * were run, each during the construction of its own instance.
   *
   * <p>
   * This trait's implementation of this method will first ensure that the results of all tests, each run its its
   * own instance executing only the path to the test, are registered. For details on this process see the
   * <a href="#howItExecutes">How it executes</a> section in the main documentation for this trait.
   * </p>
   *
   * <p>If <code>testName</code> is <code>None</code>, this trait's implementation of this method
   * will report the registered results for all tests except any excluded by the passed <code>Filter</code>.
   * If <code>testName</code> is defined, it will report the results of only that named test. Because a
   * <code>path.FunSpec</code> is not allowed to contain nested suites, this trait's implementation of
   * this method does not call <code>runNestedSuites</code>.
   * </p>
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   *
   * @param testName an optional name of one test to run. If <code>None</code>, all relevant tests should be run.
   *                 I.e., <code>None</code> acts like a wildcard that means run all relevant tests in this <code>Suite</code>.
   * @param reporter the <code>Reporter</code> to which results will be reported
   * @param stopper the <code>Stopper</code> that will be consulted to determine whether to stop execution early.
   * @param filter a <code>Filter</code> with which to filter tests based on their tags
   * @param configMap a <code>Map</code> of key-value pairs that can be used by the executing <code>Suite</code> of tests.
   * @param distributor an optional <code>Distributor</code>, into which to put nested <code>Suite</code>s to be run
   *              by another entity, such as concurrently by a pool of threads. If <code>None</code>, nested <code>Suite</code>s will be run sequentially.
   * @param tracker a <code>Tracker</code> tracking <code>Ordinal</code>s being fired by the current thread.
   *
   * @throws NullPointerException if any passed parameter is <code>null</code>.
   * @throws IllegalArgumentException if <code>testName</code> is defined, but no test with the specified test name
   *     exists in this <code>Suite</code>
   */
  final override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    ensureTestResultsRegistered(thisSuite)
    runPathTestsImpl(thisSuite, testName, reporter, stopper, filter, configMap, distributor, tracker, info, true, runTest)
  }

  /**
   * This lifecycle method is unused by this trait, and will complete abruptly with
   * <code>UnsupportedOperationException</code> if invoked.
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   */
  final protected override def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
                             configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    throw new UnsupportedOperationException
    // ensureTestResultsRegistered(isAnInitialInstance, this)
    // runTestsImpl(thisSuite, testName, reporter, stopper, filter, configMap, distributor, tracker, info, true, runTest)
  }

  /**
   * This lifecycle method is unused by this trait, and is implemented to do nothing. If invoked, it will
   * just return immediately.
   *
   * <p>
   * Nested suites are not allowed in a <code>path.FunSpec</code>. Because
   * a <code>path.FunSpec</code> executes tests eagerly at construction time, registering the results of
   * those test runs and reporting them later, the order of nested suites versus test runs would be different
   * in a <code>org.scalatest.path.FunSpec</code> than in an <code>org.scalatest.FunSpec</code>. In an
   * <code>org.scalatest.FunSpec</code>, nested suites are executed then tests are executed. In an
   * <code>org.scalatest.path.FunSpec</code> it would be the opposite. To make the code easy to reason about,
   * therefore, this is just not allowed. If you want to add nested suites to a <code>path.FunSpec</code>, you can
   * instead wrap them all in a <a href="../Suites.html"><code>Suites</code></a> or
   * <a href="../Specs.html"><code>Specs</code></a> object and put them in whatever order
   * you wish.
   * </p>
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   */
  final protected override def runNestedSuites(reporter: Reporter, stopper: Stopper, filter: Filter,
                                configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
  }

  /**
   * Returns an empty list.
   *
   * <p>
   * This lifecycle method is unused by this trait. If invoked, it will return an empty list, because
   * nested suites are not allowed in a <code>path.FunSpec</code>. Because
   * a <code>path.FunSpec</code> executes tests eagerly at construction time, registering the results of
   * those test runs and reporting them later, the order of nested suites versus test runs would be different
   * in a <code>org.scalatest.path.FunSpec</code> than in an <code>org.scalatest.FunSpec</code>. In an
   * <code>org.scalatest.FunSpec</code>, nested suites are executed then tests are executed. In an
   * <code>org.scalatest.path.FunSpec</code> it would be the opposite. To make the code easy to reason about,
   * therefore, this is just not allowed. If you want to add nested suites to a <code>path.FunSpec</code>, you can
   * instead wrap them all in a <a href="../Suites.html"><code>Suites</code></a> or
   * <a href="../Specs.html"><code>Specs</code></a> object and put them in whatever order
   * you wish.
   * </p>
   *
   * <p>
   * This trait's implementation of this method is  marked as final. For insight onto why, see the
   * <a href="#sharedFixtures">Shared fixtures</a> section in the main documentation for this trait.
   * </p>
   */
  final override def nestedSuites: List[Suite] = Nil
}


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
package org.scalatest.fun

import scala.collection.immutable.ListSet
import java.util.ConcurrentModificationException
import java.util.concurrent.atomic.AtomicReference
import org.scalacheck.Arbitrary
import org.scalacheck.Shrink
import org.scalacheck.Prop
import org.scalacheck.Test.Params
import org.scalacheck.Test
import org.scalacheck.Test._

/**
 * A suite of tests in which each test is represented as a function value. The &#8220;<code>Fun</code> &#8221;in <code>FunSuite</code> stands for functional.
 * Here's an example <code>FunSuite</code>:
 *
 * <pre>
 * import org.scalatest.fun.FunSuite
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
 * &#8220;<code>test</code>&#8221; is a method defined in <code>FunSuite</code>, which will be invoked
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
 * <strong>Property-based tests</strong>
 * </p>
 * 
 * <p>
 * <code>FunSuite</code> makes it easy to combine assertion-based tests with property-based
 * tests using ScalaCheck. <code>FunSuite</code> mixes in trait <code>Checkers</code>, so you
 * can write property checks alongside assertion-based tests in your test functions:
 * </p>
 * <pre>
 * import org.scalatest.fun.FunSuite
 *
 * class MySuite extends FunSuite {
 *
 *   test("list concatenation") {
 * 
 *     val a = List(1, 2, 3)
 *     val b = List(4, 5, 6)
 *     assert(a ::: b === List(1, 2, 3, 4, 5, 6))

 *     check((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
 *   }
 * }
 * </pre>
 *
 * <p>
 * You can also register properties as tests, using "test". Here's an
 * example:
 * </p>
 * <pre>
 * import org.scalatest.fun.FunSuite
 *
 * class StringSuite extends FunSuite {
 *
 *   test("startsWith", (a: String, b: String) => (a + b).startsWith(a))
 *
 *   test("endsWith", (a: String, b: String) => (a + b).endsWith(b))
 *
 *   test(
 *     "substring should start from passed index and go to end of string",
 *     (a: String, b: String) => (a + b).substring(a.length) == b
 *   )
 *
 *   test(
 *     "substring should start at passed index and extract passed number of chars",
 *     (a: String, b: String, c: String) => (a + b + c).substring(a.length, a.length + b.length) == b
 *   )
 * }
 * </pre>
 * 
 * <p>
 * <strong>Test fixtures</strong>
 * </p>
 * 
 * <p>
 * If you want to write tests that need the same mutable fixture objects, you can
 * extend one of the traits <code>FunSuite1</code> through <code>FunSuite9</code>. If you need three
 * fixture objects, for example, you would extend <code>FunSuite3</code>. Here's an example
 * that extends <code>FunSuite1</code>, to initialize a <code>StringBuilder</code> fixture object for each test:
 * </p>
 * 
 * <pre>
 * import org.scalatest.fun.FunSuite1
 *
 * class EasySuite extends FunSuite1[StringBuilder] {
 *
 *   testWithFixture("easy test") {
 *     sb => {
 *       sb.append("easy!")
 *       assert(sb.toString === "Testing is easy!")
 *     }
 *   }
 *
 *   testWithFixture("fun test") {
 *     sb => {
 *       sb.append("fun!")
 *       assert(sb.toString === "Testing is fun!")
 *     }
 *   }
 *
 *   def withFixture(f: StringBuilder => Unit) {
 *     val sb = new StringBuilder("Testing is ")
 *     f(sb)
 *   }
 * }
 * </pre>
 * 
 * <p>
 * In the class declaration of this example, <code>FunSuite1</code> is parameterized with the type of the
 * lone fixture object, <code>StringBuilder</code>. Two tests are defined with
 * <code>testWithFixture</code>. The function values provided here take the fixture object,
 * a <code>StringBuilder</code>, as a parameter and use it in the test code. Note that
 * the fixture object, referenced by <code>sb</code>, is mutated by both tests with the call to <code>append</code>. Lastly, a <code>withFixture</code>
 * method is provided that takes a test function. This method creates a new <code>StringBuilder</code>,
 * initializes it to <code>"Testing is "</code>, and passes it to the test function.
 * When ScalaTest runs this suite, it will pass each test function to <code>withFixture</code>.
 * The <code>withFixture</code> method will create and initialize a new <code>StringBuilder</code> object and
 * pass that to the test function. In this way, each test function will get a fresh copy
 * of the fixture. For more information on using <code>FunSuite</code>s with fixtures, see the documentation
 * for <code>FunSuite1</code> through <code>FunSuite9</code>.
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
 * groups, you pass objects that extend abstract class <code>org.scalatest.fun.Group</code> to methods
 * that register tests. Class <code>Group</code> takes one type parameter, a string name.  If you have
 * created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>FunSuite</code>s that match. To do so, simply 
 * pass the fully qualified names of the Java interfaces to the <code>Group</code> constructor. For example, if you've
 * defined Java annotation interfaces with fully qualified names, <code>com.mycompany.groups.SlowTest</code> and <code>com.mycompany.groups.DBTest</code>, then you could
 * create matching groups for <code>FunSuite</code>s like this:
 * </p>
 * <pre>
 * object SlowTest extends Group("com.mycompany.groups.SlowTest")
 * object DBTest extends Group("com.mycompany.groups.DBTest")
 * </pre>
 * <p>
 * Given these definitions, you could place <code>FunSuite</code> tests into groups like this:
 * </p>
 * <pre>
 * import org.scalatest.fun.FunSuite
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
 * This code places both tests, <code>addition</code> and <code>subtraction</code>, into the <code>com.mycompany.groups.SlowTest</code> group, 
 * and test <code>subtraction</code> into the <code>com.mycompany.groups.DBTest</code> group.
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
 * To support the common use case of &#8220;temporarily&#8221; disabling tests, with the
 * good intention of resurrecting the test at a later time, <code>FunSuite</code> provides registration
 * methods that start with <code>ignore</code> instead of <code>test</code>. For example, to temporarily
 * disable the test named <code>addition</code>, just change &#8220;<code>test</code>&#8221; into &#8220;<code>ignore</code>,&#8221; like this:
 * </p>
 *
 * <pre>
 * import org.scalatest.fun.FunSuite
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
 * It will run only <code>subtraction</code> and report that <code>addition</code> was ignored.
 * </p>
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
 * <strong>Reporters</strong>
 * </p>
 *
 * <p>
 * One of the parameters to the primary <code>execute</code> method is a <code>Reporter</code>, which
 * will collect and report information about the running suite of tests.
 * Information about suites and tests that were run, whether tests succeeded or failed, 
 * and tests that were ignored will be passed to the <code>Reporter</code> as the suite runs.
 * Most often the reporting done by default by <code>FunSuite</code>'s methods will be sufficient, but
 * occasionally you may wish to provide custom information to the <code>Reporter</code> from a test.
 * For this purpose, you can optionally register a test with a function value that takes a <code>Reporter</code> parameter via the &#8220;<code>...WithReporter</code>
 * variants of the test registration methods. You can then
 * pass extra information to the <code>Reporter</code>'s <code>infoProvided</code> method in the body of the test functions.
 * Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest.fun.FunSuite
 * import org.scalatest.Report
 *
 * class MySuite extends FunSuite {
 *
 *   testWithReporter("addition") {
 *     reporter => {
 *       val sum = 1 + 1
 *       assert(sum === 2)
 *       assert(sum + 2 === 4)
 *       val report = new Report("MySuite.addition", "Addition seems to work.")
 *       reporter.infoProvided(report)
 *     }
 *   }
 * }
 * </pre>
 *
 * If you run this <code>Suite</code> from the interpreter, you will see the following message
 * included in the printed report:
 *
 * <pre>
 * Info Provided: MySuite.addition: Addition seems to work.
 * </pre>
 *
 * <p>
 * This trait depends on ScalaCheck, so you must include ScalaCheck's
 * jar file on either the classpath or runpath.
 * </p>
 *
 * @author Bill Venners
 */
trait PropSuite extends FunSuite with Checkers {

  /**
   * Convert the passed 1-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test[A1,P](testName: String, f: A1 => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1]
    ) {
    test(testName, Prop.property(f)(p, a1, s1))
  }

  /**
   * Convert the passed 2-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test[A1,A2,P](testName: String, f: (A1,A2) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2]
    ) {
    test(testName, Prop.property(f)(p, a1, s1, a2, s2))
  }

  /**
   * Convert the passed 3-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test[A1,A2,A3,P](testName: String, f: (A1,A2,A3) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3]
    ) {
    test(testName, Prop.property(f)(p, a1, s1, a2, s2, a3, s3))
  }

  /**
   * Convert the passed 4-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test[A1,A2,A3,A4,P](testName: String, f: (A1,A2,A3,A4) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4]
    ) {
    test(testName, Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4))
  }

  /**
   * Convert the passed 5-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test[A1,A2,A3,A4,A5,P](testName: String, f: (A1,A2,A3,A4,A5) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4],
      a5: Arbitrary[A5], s5: Shrink[A5]
    ) {
    test(testName, Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5))
  }

  /**
   * Convert the passed 6-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test[A1,A2,A3,A4,A5,A6,P](testName: String, f: (A1,A2,A3,A4,A5,A6) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4],
      a5: Arbitrary[A5], s5: Shrink[A5],
      a6: Arbitrary[A6], s6: Shrink[A6]
    ) {
    test(testName, Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5, a6, s6))
  }


  /**
   * Register as a test a property with the given testing parameters.
   *
   * @param p the property to check
   * @param prms the test parameters
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test(testName: String, p: Prop, prms: Params, testGroups: Group*) {
    test(testName, testGroups: _*) {
      check(p, prms)
    }
  }

  /**
   * Register a property as a test.
   *
   * @param p the property to check
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def test(testName: String, p: Prop, testGroups: Group*) {
    test(testName, p, Test.defaultParams)
  }
}

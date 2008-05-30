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
package org.scalatest.prop

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
 * A <code>FunSuite</code> subtrait that provides methods that perform
 * ScalaCheck property checks.
 * If ScalaCheck, when invoked via one of the methods provided by <cod>PropSuite</code>, finds a test case for which a property doesn't hold, the problem will be reported as a ScalaTest test failure.
 * 
 * <p>
 * To use ScalaCheck, you specify properties and, in some cases, generators that generate test data. Often you need not 
 * provide generators, because ScalaCheck provides many default generators that you can use in many situations.
 * ScalaCheck will use the generators to generate test data and with that data run tests that check that the property holds.
 * Property-based tests can, therefore, can give you a lot more testing for a lot less code than assertion-based tests.
 * Here's an example of using ScalaCheck from a <code>PropSuite</code>:
 * </p>
 *
 * <pre>
 * import org.scalatest.prop.PropSuite
 * import org.scalacheck.Arbitrary._
 * import org.scalacheck.Prop._
 *
 * class MySuite extends PropSuite {
 *
 *   test("list concatenation") {
 * 
 *     val x = List(1, 2, 3)
 *     val y = List(4, 5, 6)
 *     assert(x ::: y === List(1, 2, 3, 4, 5, 6))
 *
 *     check((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
 *   }
 *
 *   test(
 *     "list concatenation using a test method",
 *     (a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size
 *   )
 * }
 * </pre>
 *
 * <p>
 * <code>PropSuite</code> mixes in trait <code>Checkers</code>, so you can call any of its
 * <code>check</code> methods inside a test function. This is shown in the first test:
 * </p>
 * <pre>
 * test("list concatenation") {
 * 
 *   val x = List(1, 2, 3)
 *   val y = List(4, 5, 6)
 *   assert(x ::: y === List(1, 2, 3, 4, 5, 6))
 *
 *   check((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
 * }
 * </pre>
 *
 * <p>
 * The <code>check</code> methods provided by <code>Checkers</code> allow you to combine assertion- and property-based
 * testing in the same test function. If you want to define a test that is composed only
 * of a single property check, you can use one of several <code>test</code> methods
 * <code>PropSuite</code> defines. These <code>test</code> methods allow you to
 * register just a property as a test function. This is shown in the previous example
 * in the second test:
 *
 * <pre>
 * test(
 *   "list concatenation using a test method",
 *   (a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size
 * )
 * </pre>
 *
 * <p>
 * Here are a few other examples:
 * </p>
 *
 * <pre>
 * import org.scalatest.prop.PropSuite
 *
 * class StringSuite extends PropSuite {
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
 * <strong>Test groups</strong>
 * </p>
 *
 * <p>
 * A <code>PropSuite</code>'s tests may be classified into named <em>groups</em> in
 * the same manner as its supertrait <code>FunSuite</code>.
 * As with any suite, when executing a <code>PropSuite</code>, groups of tests can
 * optionally be included and/or excluded. To place <code>PropSuite</code> tests into
 * groups, you pass objects that extend abstract class <code>org.scalatest.Group</code> to methods
 * that register tests. Class <code>Group</code> takes one type parameter, a string name.  If you have
 * created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>FunSuite</code>s that match. To do so, simply 
 * pass the fully qualified names of the Java interfaces to the <code>Group</code> constructor. For example, if you've
 * defined Java annotation interfaces with fully qualified names, <code>com.mycompany.groups.SlowTest</code> and <code>com.mycompany.groups.DBTest</code>, then you could
 * create matching groups for <code>FunSuite</code>s like this:
 * </p>
 * <pre>
 * import org.scalatest.Group
 *
 * object SlowTest extends Group("com.mycompany.groups.SlowTest")
 * object DBTest extends Group("com.mycompany.groups.DBTest")
 * </pre>
 * <p>
 * Given these definitions, you could place <code>PropSuite</code> tests into groups like this:
 * </p>
 * <pre>
 * import org.scalatest.prop.PropSuite
 * import org.scalacheck.Arbitrary._
 * import org.scalacheck.Prop._
 *
 * class MySuite extends PropSuite {
 *
 *   test("list concatenation", SlowTest) {
 *
 *     val x = List(1, 2, 3)
 *     val y = List(4, 5, 6)
 *     assert(x ::: y === List(1, 2, 3, 4, 5, 6))
 *
 *     check((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
 *   }
 *
 *   test(
 *     "list concatenation using a test method",
 *     (a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size,
 *     SlowTest,
 *     DBTest
 *   )
 * }
 * </pre>
 *
 * <p>
 * This code places both tests, "list concatenation" and "list concatenation using
 * a test method," into the <code>com.mycompany.groups.SlowTest</code> group, 
 * and test "list concatenation using a test method" into the <code>com.mycompany.groups.DBTest</code> group.
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
 * good intention of resurrecting the test at a later time, <code>PropSuite</code> provides registration
 * methods that start with <code>ignore</code> instead of <code>test</code>. For example, to temporarily
 * disable the tests defined in the <code>MySuite</code> example shown previously, just change &#8220;<code>test</code>&#8221; into &#8220;<code>ignore</code>,&#8221; like this:
 * </p>
 *
 * <pre>
 * import org.scalatest.prop.PropSuite
 * import org.scalacheck.Arbitrary._
 * import org.scalacheck.Prop._
 *
 * class MySuite extends PropSuite {
 *
 *   ignore("list concatenation") {
 *
 *     val x = List(1, 2, 3)
 *     val y = List(4, 5, 6)
 *     assert(x ::: y === List(1, 2, 3, 4, 5, 6))
 *
 *     check((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
 *   }
 *
 *   ignore(
 *     "list concatenation using a test method",
 *     (a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size
 *   )
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
 * It will run neither test and report that both were ignored:
 * </p>
 *
 * <pre>
 * Test Ignored - MySuite: list concatenation
 * Test Ignored - MySuite: list concatenation using a test method
 * </pre>
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

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that converts the passed 1-arg function into a property and checks
   * it.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore[A1,P](testName: String, f: A1 => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1]
    ) {
    ignore(testName, Prop.property(f)(p, a1, s1))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that converts the passed 2-arg function into a property and checks
   * it.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore[A1,A2,P](testName: String, f: (A1,A2) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2]
    ) {
    ignore(testName, Prop.property(f)(p, a1, s1, a2, s2))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that converts the passed 3-arg function into a property and checks
   * it.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore[A1,A2,A3,P](testName: String, f: (A1,A2,A3) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3]
    ) {
    ignore(testName, Prop.property(f)(p, a1, s1, a2, s2, a3, s3))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that converts the passed 4-arg function into a property and checks
   * it.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore[A1,A2,A3,A4,P](testName: String, f: (A1,A2,A3,A4) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4]
    ) {
    ignore(testName, Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that converts the passed 5-arg function into a property and checks
   * it.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore[A1,A2,A3,A4,A5,P](testName: String, f: (A1,A2,A3,A4,A5) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4],
      a5: Arbitrary[A5], s5: Shrink[A5]
    ) {
    ignore(testName, Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that converts the passed 6-arg function into a property and checks
   * it.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore[A1,A2,A3,A4,A5,A6,P](testName: String, f: (A1,A2,A3,A4,A5,A6) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4],
      a5: Arbitrary[A5], s5: Shrink[A5],
      a6: Arbitrary[A6], s6: Shrink[A6]
    ) {
    ignore(testName, Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5, a6, s6))
  }


  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that tests the specified property with the specified testing parameters.
   *
   * @param p the property to check
   * @param prms the test parameters
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore(testName: String, p: Prop, prms: Params, testGroups: Group*) {
    ignore(testName, testGroups: _*) {
      check(p, prms)
    }
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that tests the specified property.
   *
   * @param p the property to check
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore(testName: String, p: Prop, testGroups: Group*) {
    ignore(testName, p, Test.defaultParams)
  }
}

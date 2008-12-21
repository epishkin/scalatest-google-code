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
 * A <code>Spec</code> subtrait that provides methods that perform
 * ScalaCheck property checks.
 * If ScalaCheck, when invoked via one of the methods provided by <cod>Spec</code>, finds a test case for which a property doesn't hold, the problem will be reported as a ScalaTest test failure.
 * 
 * <p>
 * To use ScalaCheck, you specify properties and, in some cases, generators that generate test data. Often you need not 
 * provide generators, because ScalaCheck provides many default generators that you can use in many situations.
 * ScalaCheck will use the generators to generate test data and with that data run tests that check that the property holds.
 * Property-based tests can, therefore, can give you a lot more testing for a lot less code than assertion-based tests.
 * Here's an example of using ScalaCheck from a <code>Spec</code>:
 * </p>
 *
 * <pre>
 * import org.scalatest.prop.Spec
 * import org.scalacheck.Arbitrary._
 * import org.scalacheck.Prop._
 *
 * class MySpec extends Spec {
 *
 *   describe("Using ScalaCheck from a Spec") {
 *     it("should work when passing the property to a check method") {
 * 
 *       val x = List(1, 2, 3)
 *       val y = List(4, 5, 6)
 *       assert(x ::: y === List(1, 2, 3, 4, 5, 6))
 *
 *       check((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
 *     }
 *
 *     it("should work when passing the property as an it method parameter",
 *       (a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size
 *     )
 *   }
 * }
 * </pre>
 *
 * <p>
 * <code>Spec</code> mixes in trait <code>Checkers</code>, so you can call any of its
 * <code>check</code> methods inside a test function of an example. This is shown in the first example:
 * </p>
 * <pre>
 * it("should work when passing the property to a check method") {
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
 * of a single property check, you can use one of several <code>it</code> methods
 * <code>Spec</code> defines. These <code>it</code> methods allow you to
 * register just a property as a test function. This is shown in the previous example
 * in the second test:
 *
 * <pre>
 * it("should work when passing the property as an it method parameter",
 *   (a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size
 * )
 * </pre>
 *
 * <p>
 * Here are a few other examples:
 * </p>
 *
 * <pre>
 * import org.scalatest.prop.Spec
 *
 * class StringSpec extends Spec {
 *
 *   describe("String.startsWith") {
 *     it("should return true if invoked on a string that starts with the passed string",
 *       (a: String, b: String) => (a + b).startsWith(a)
 *     )
 *   }
 *
 *   describe("String.endsWith") {
 *     it("should return true if invoked on a string that ends with the passed string",
 *       (a: String, b: String) => (a + b).endsWith(b)
 *     )
 *   }
 *
 *   describe("String.substring") {
 *     it("should start from passed index and go to end of string",
 *       (a: String, b: String) => (a + b).substring(a.length) == b
 *     )
 *
 *     it("should start at passed index and extract passed number of chars",
 *       (a: String, b: String, c: String) => (a + b + c).substring(a.length, a.length + b.length) == b
 *     )
 *   }
 * }
 * </pre>
 *
 * <p>
 * <strong>Test groups</strong>
 * </p>
 *
 * <p>
 * A <code>Spec</code>'s tests may be classified into named <em>groups</em> in
 * the same manner as its supertrait <code>org.scalatest.Spec</code>.
 * As with any suite, when executing a <code>Spec</code>, groups of tests can
 * optionally be included and/or excluded. To place <code>Spec</code> tests into
 * groups, you pass objects that extend abstract class <code>org.scalatest.Group</code> to methods
 * that register tests. Class <code>Group</code> takes one type parameter, a string name.  If you have
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
 * Given these definitions, you could place <code>FunSuite</code> tests into groups like this:
 * </p>
 * <pre>
 * import org.scalatest.prop.Spec
 * import org.scalacheck.Arbitrary._
 * import org.scalacheck.Prop._
 *
 * class MySpec extends Spec {
 *
 *   it("should check a ScalaCheck property using check", SlowTest) {
 *
 *     val x = List(1, 2, 3)
 *     val y = List(4, 5, 6)
 *     assert(x ::: y === List(1, 2, 3, 4, 5, 6))
 *
 *     check((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
 *   }
 *
 *   it("should check a ScalaCheck property passed as an it method parameter",
 *     (a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size,
 *     SlowTest,
 *     DBTest
 *   )
 * }
 * </pre>
 *
 * <p>
 * This code places both tests into the <code>com.mycompany.groups.SlowTest</code> group,
 * and latter test into the <code>com.mycompany.groups.DBTest</code> group as well.
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
 * To support the common use case of &#8220;temporarily&#8221; disabling tests, with the
 * good intention of resurrecting the test at a later time, <code>Spec</code> provides registration
 * methods that start with <code>ignore</code> instead of <code>it</code>. For example, to temporarily
 * disable the tests defined in the <code>MySPec</code> example shown previously, just change &#8220;<code>it</code>&#8221; into &#8220;<code>ignore</code>,&#8221; like this:
 * </p>
 *
 * <pre>
 * import org.scalatest.prop.Spec
 * import org.scalacheck.Arbitrary._
 * import org.scalacheck.Prop._
 *
 * class MySpec extends Spec {
 *
 *   ignore("should check a ScalaCheck property using check", SlowTest) {
 *
 *     val x = List(1, 2, 3)
 *     val y = List(4, 5, 6)
 *     assert(x ::: y === List(1, 2, 3, 4, 5, 6))
 *
 *     check((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
 *   }
 *
 *   ignore("should check a ScalaCheck property passed as an it method parameter",
 *     (a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size,
 *     SlowTest,
 *     DBTest
 *   )
 * }
 * </pre>
 *
 * <p>
 * If you run this version of <code>MySpec</code> with:
 * </p>
 *
 * <pre>
 * scala> (new MySpec).execute()
 * </pre>
 *
 * <p>
 * It will run neither test and report that both were ignored:
 * </p>
 *
 * <pre>
 * - should check a ScalaCheck property using check !!! IGNORED !!!
 * - should check a ScalaCheck property passed as an it method parameter !!! IGNORED !!!
 * </pre>
 *
 * <p>
 * As with <code>org.scalatest.Suite</code>, the ignore feature is implemented as a group. The <code>execute</code> method that takes no parameters
 * adds <code>org.scalatest.Ignore</code> to the <code>groupsToExclude</code> <code>Set</code> it passes to
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
trait Spec extends scalatest.Spec with Checkers {

  /**
   * Convert the passed 1-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def it[A1,P](specText: String, f: A1 => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1]
    ) {
    it(specText, Prop.property(f)(p, a1, s1))
  }

  /**
   * Convert the passed 2-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def it[A1,A2,P](specText: String, f: (A1,A2) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2]
    ) {
    it(specText, Prop.property(f)(p, a1, s1, a2, s2))
  }

  /**
   * Convert the passed 3-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def it[A1,A2,A3,P](specText: String, f: (A1,A2,A3) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3]
    ) {
    it(specText, Prop.property(f)(p, a1, s1, a2, s2, a3, s3))
  }

  /**
   * Convert the passed 4-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def it[A1,A2,A3,A4,P](specText: String, f: (A1,A2,A3,A4) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4]
    ) {
    it(specText, Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4))
  }

  /**
   * Convert the passed 5-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def it[A1,A2,A3,A4,A5,P](specText: String, f: (A1,A2,A3,A4,A5) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4],
      a5: Arbitrary[A5], s5: Shrink[A5]
    ) {
    it(specText, Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5))
  }

  /**
   * Convert the passed 6-arg function into a property, and register it as a test.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def it[A1,A2,A3,A4,A5,A6,P](specText: String, f: (A1,A2,A3,A4,A5,A6) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4],
      a5: Arbitrary[A5], s5: Shrink[A5],
      a6: Arbitrary[A6], s6: Shrink[A6]
    ) {
    it(specText, Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5, a6, s6))
  }


  /**
   * Register as a test a property with the given testing parameters.
   *
   * @param p the property to check
   * @param prms the test parameters
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def it(specText: String, p: Prop, prms: Params, testGroups: Group*) {
    it(specText, testGroups: _*) {
      check(p, prms)
    }
  }

  // TODO: I think there are bugs here in that the groups aren't being passed. These
  // are also probably in prop.FunSuite.
  /**
   * Register a property as a test.
   *
   * @param p the property to check
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def it(specText: String, p: Prop, testGroups: Group*) {
    it(specText, p, Test.defaultParams)
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that converts the passed 1-arg function into a property and checks
   * it.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore[A1,P](specText: String, f: A1 => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1]
    ) {
    ignore(specText, Prop.property(f)(p, a1, s1))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that converts the passed 2-arg function into a property and checks
   * it.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore[A1,A2,P](specText: String, f: (A1,A2) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2]
    ) {
    ignore(specText, Prop.property(f)(p, a1, s1, a2, s2))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that converts the passed 3-arg function into a property and checks
   * it.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore[A1,A2,A3,P](specText: String, f: (A1,A2,A3) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3]
    ) {
    ignore(specText, Prop.property(f)(p, a1, s1, a2, s2, a3, s3))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that converts the passed 4-arg function into a property and checks
   * it.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore[A1,A2,A3,A4,P](specText: String, f: (A1,A2,A3,A4) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4]
    ) {
    ignore(specText, Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that converts the passed 5-arg function into a property and checks
   * it.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore[A1,A2,A3,A4,A5,P](specText: String, f: (A1,A2,A3,A4,A5) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4],
      a5: Arbitrary[A5], s5: Shrink[A5]
    ) {
    ignore(specText, Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5))
  }

  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that converts the passed 6-arg function into a property and checks
   * it.
   *
   * @param f the function to be converted into a property and checked
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore[A1,A2,A3,A4,A5,A6,P](specText: String, f: (A1,A2,A3,A4,A5,A6) => P, testGroups: Group*)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4],
      a5: Arbitrary[A5], s5: Shrink[A5],
      a6: Arbitrary[A6], s6: Shrink[A6]
    ) {
    ignore(specText, Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5, a6, s6))
  }


  /**
   * Register a test to ignore, which has the specified name, optional groups, and
   * function value that tests the specified property with the specified testing parameters.
   *
   * @param p the property to check
   * @param prms the test parameters
   * @throws AssertionError if a test case is discovered for which the property doesn't hold.
   */
  def ignore(specText: String, p: Prop, prms: Params, testGroups: Group*) {
    ignore(specText, testGroups: _*) {
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
  def ignore(specText: String, p: Prop, testGroups: Group*) {
    ignore(specText, p, Test.defaultParams)
  }
}

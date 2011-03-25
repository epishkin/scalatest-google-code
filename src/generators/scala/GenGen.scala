/*
 * Copyright 2001-2011 Artima, Inc.
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
import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import java.util.Calendar
import scala.collection.JavaConversions._

object GenGen extends Application {

val scaladocForTableFor1VerbatimString = """
/**
 * A table with 1 column.
 *
 * <p>
 * For an overview of using tables, see the documentation for trait
 * <a href="TableDrivenPropertyChecks.html">TableDrivenPropertyChecks</a>.
 * </p>
 *
 * <p>
 * This table is a sequence of objects, where each object represents one row of the (one-column) table.
 * This table also carries with it a <em>heading</em> tuple that gives a string name to the
 * lone column of the table.
 * </p>
 *
 * <p>
 * A handy way to create a <code>TableFor1</code> is via an <code>apply</code> factory method in the <code>Table</code>
 * singleton object provided by the <code>Tables</code> trait. Here's an example:
 * </p>
 *
 * <pre>
 * val examples =
 *   Table(
 *     "a",
 *       0,
 *       1,
 *       2,
 *       3,
 *       4,
 *       5,
 *       6,
 *       7,
 *       8,
 *       9
 *   )
 * </pre>
 *
 * <p>
 * Because you supplied a list of non-tuple objects, the type you'll get back will be a <code>TableFor1</code>.
 * </p>
 *
 * <p>
 * The table provides an <code>apply</code> method that takes a function with a parameter list that matches
 * the type of the objects contained in this table. The <code>apply</code> method will invoke the
 * function with the object in each row passed as the lone argument, in ascending order by index. (<em>I.e.</em>,
 * the zeroth object is checked first, then the object with index 1, then index 2, and so on until all the rows
 * have been checked (or until a failure occurs). The function represents a property of the code under test
 * that should succeed for every row of the table. If the function returns normally, that indicates the property
 * check succeeded for that row. If the function completes abruptly with an exception, that indicates the
 * property check failed and the <code>apply</code> method will complete abruptly with a
 * <code>TableDrivenPropertyCheckFailedException</code> that wraps the exception thrown by the supplied property function.
 * </p>
 * 
 * <p>
 * The usual way you'd invoke the <code>apply</code> method that checks a property is via a <code>forAll</code> method
 * provided by trait <code>TableDrivenPropertyChecks</code>. The <code>forAll</code> method takes a <code>TableFor1</code> as its
 * first argument, then in a curried argument list takes the property check function. It invokes <code>apply</code> on
 * the <code>TableFor1</code>, passing in the property check function. Here's an example:
 * </p>
 *
 * <pre>
 * forAll (examples) { (a) =>
 *   a should equal (a * 1)
 * }
 * </pre>
 *
 * <p>
 * Because <code>TableFor1</code> is a <code>Seq[(A)]</code>, you can use it as a <code>Seq</code>. For example, here's how
 * you could get a sequence of optional exceptions for each row of the table, indicating whether a property check succeeded or failed
 * on each row of the table:
 * </p>
 *
 * <pre>
 * for (row <- examples) yield {
 *   failureOf { row._1 should not equal (7) }
 * }
 * </pre>
 *
 * <p>
 * Note: the <code>failureOf</code> method, contained in the <code>FailureOf</code> trait, will execute the supplied code (a by-name parameter) and
 * catch any exception. If no exception is thrown by the code, <code>failureOf</code> will result in <code>None</code>, indicating the "property check"
 * succeeded. If the supplied code completes abruptly in an exception that would normally cause a test to fail, <code>failureOf</code> will result in
 * a <code>Some</code> wrapping that exception. For example, the previous for expression would give you:
 * </p>
 *
 * <pre>
 * Vector(None, None, None, None, None, None, None,
 *     Some(org.scalatest.TestFailedException: 7 equaled 7), None, None)
 * </pre>
 *
 * <p>
 * This shows that all the property checks succeeded, except for the one at index 7.
 * <p>
 *
 * <p>
 * One other way to use a <code>TableFor1</code> is to test subsequent return values
 * of a stateful function. Imagine, for example, you had an object named <code>FiboGen</code>
 * whose <code>next</code> method returned the <em>next</em> fibonacci number, where next
 * means the next number in the series following the number previously returned by <code>next</code>.
 * So the first time <code>next</code> was called, it would return 0. The next time it was called
 * it would return 1. Then 1. Then 2. Then 3, and so on. <code>FiboGen</code> would need to
 * be stateful, because it has to remember where it is in the series. In such a situation,
 * you could create a <code>TableFor1</code> (a table with one column, which you could alternatively
 * think of as one row), in which each row represents
 * the next value you expect.
 * </p>
 *
 * <pre>
 * val first14FiboNums =
 *   Table("n", 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233)
 * </pre>
 *
 * <p>
 * Then in your <code>forAll</code> simply call the function and compare it with the
 * expected return value, like this:
 * </p>
 *
 * <pre>
 *  forAll (first14FiboNums) { n =>
 *    FiboGen.next should equal (n)
 *  }
 * </pre>
 *
 * @param heading a string name for the lone column of this table
 * @param rows a variable length parameter list of objects containing the data of this table
 *
 * @author Bill Venners 
 */
"""

val copyrightTemplate = """/*
 * Copyright 2001-$year$ Artima, Inc.
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
package prop
"""

val propertyCheckPreamble = """
import org.scalacheck.Arbitrary
import org.scalacheck.Shrink
import org.scalacheck.Prop
import org.scalacheck.Gen
import org.scalacheck.Prop._
import org.scalacheck.Test.Params
import Helper.getParams

/**
 * Trait containing methods that faciliate property checks against generated data using ScalaCheck.
 *
 * <p>
 * This trait contains <code>forAll</code> methods that provide various ways to check properties using
 * generated data. Use of this trait requires that ScalaCheck be on the class path when you compile and run your tests.
 * It also contains a <code>wherever</code> method that can be used to indicate a property need only hold whenever
 * some condition is true.
 * </p>
 *
 * <p>
 * For an example of trait <code>GeneratorDrivenPropertyChecks</code> in action, imagine you want to test this <code>Fraction</code> class:
 * </p>
 *  
 * <pre>
 * class Fraction(n: Int, d: Int) {
 *
 *   require(d != 0)
 *   require(d != Integer.MIN_VALUE)
 *   require(n != Integer.MIN_VALUE)
 *
 *   val numer = if (d < 0) -1 * n else n
 *   val denom = d.abs
 *
 *   override def toString = numer + " / " + denom
 * }
 * </pre>
 *
 * <p>
 * To test the behavior of <code>Fraction</code>, you could mix in or import the members of <code>GeneratorDrivenPropertyChecks</code>
 * (and <code>ShouldMatchers</code>) and check a property using a <code>forAll</code> method, like this:
 * </p>
 *
 * <pre>
 * forAll { (n: Int, d: Int) =>
 *
 *   whenever (d != 0 && d != Integer.MIN_VALUE
 *       && n != Integer.MIN_VALUE) {
 *
 *     val f = new Fraction(n, d)
 *
 *     if (n < 0 && d < 0 || n > 0 && d > 0)
 *       f.numer should be > 0
 *     else if (n != 0)
 *       f.numer should be < 0
 *     else
 *       f.numer should be === 0
 *
 *     f.denom should be > 0
 *   }
 * }
 * </pre>
 *
 * <p>
 * Trait <code>GeneratorDrivenPropertyChecks</code> provides overloaded <code>forAll</code> methods
 * that allow you to check properties using the data provided by a ScalaCheck generator. The simplest form
 * of <code>forAll</code> method takes two parameter lists, the second of which is implicit. The first parameter list
 * is a "property" function with one to six parameters. An implicit <code>Arbitrary</code> generator and <code>Shrink</code> object needs to be supplied for
 * The <code>forAll</code> method will pass each row of data to
 * each parameter type. ScalaCheck provides many implicit <code>Arbitrary</code> generators for common types such as
 * <code>Int</code>, <code>String</code>, <code>List[Float]</code>, <em>etc.</em>, in its <code>org.scalacheck.Arbitrary</code> companion
 * object. So long as you use types for which ScalaCheck already provides implicit <code>Arbitrary</code> generators, you needn't
 * worry about them. Same for <code>Shrink</code> objects, which are provided by ScalaCheck's <code>org.scalacheck.Shrink</code> companion
 * object. Most often you can simply pass a property function to <code>forAll</code>, and the compiler will grab the implicit
 * values provided by ScalaCheck.
 * </p>
 *
 * <p>
 * The <code>forAll</code> method use the supplied <code>Arbitrary</code> generators to generate example
 * arguments and pass them to the property function, and
 * generate a <code>GeneratorDrivenPropertyCheckFailedException</code> if the function
 * completes abruptly any exception that would <a href="../Suite.html#errorHandling">normally cause</a> a test to
 * fail in ScalaTest other than <code>UnmetConditionException</code>. An
 * <code>UnmetConditionException</code>,
 * which is thrown by the <code>whenever</code> method (also defined in this trait) to indicate
 * a condition required by the property function is not met by a row
 * of passed data, will simply cause <code>forAll</code> to skip that row of data.
 * </p>
 *
 * @author Bill Venners
 */
trait GeneratorDrivenPropertyChecks extends Whenever with ConfigMethods {

  /**
   * Implicit <code>PropertyCheckConfig</code> value providing default configuration values. 
   */
  implicit val generatorDrivenConfig = PropertyCheckConfig()

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by implicitly passed generators, modifying the values in the implicitly passed 
   * <code>PropertyGenConfig</code> object with explicitly passed parameter values.
   *
   * <p>
   * This method creates a <code>ConfiguredPropertyCheck</code> object that has six overloaded apply methods
   * that take a function. Thus it is used with functions of all six arities.
   * Here are some examples:
   * </p>
   *
   * <pre>
   * forAll (minSize(1), maxSize(10)) { (a: String) =>
   *   a.length should equal ((a).length)
   * }
   *
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String) =>
   *   a.length + b.length should equal ((a + b).length)
   * }
   *
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String, c: String) =>
   *   a.length + b.length + c.length should equal ((a + b + c).length)
   * }
   *
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String, c: String, d: String) =>
   *   a.length + b.length + c.length + d.length should equal ((a + b + c + d).length)
   * }
   *
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String, c: String, d: String, e: String) =>
   *   a.length + b.length + c.length + d.length + e.length should equal ((a + b + c + d + e).length)
   * }
   *
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
   *   a.length + b.length + c.length + d.length + e.length + f.length should equal ((a + b + c + d + e + f).length)
   * }
   * </pre>
   *
   * @param configParams a variable length list of <code>PropertyCheckConfigParam</code> objects that should override corresponding
   *   values in the <code>PropertyCheckConfig</code> implicitly passed to the <code>apply</code> methods of the <code>ConfiguredPropertyCheck</code>
   *   object returned by this method.
   */
  def forAll(configParams: PropertyCheckConfigParam*): ConfiguredPropertyCheck = new ConfiguredPropertyCheck(configParams)

  /**
   * Performs a configured property checks by applying property check functions passed to its <code>apply</code> methods to arguments
   * supplied by implicitly passed generators, modifying the values in the 
   * <code>PropertyGenConfig</code> object passed implicitly to its <code>apply</code> methods with parameter values passed to its constructor.
   *
   * <p>
   * Instances of this class are returned by trait <code>GeneratorDrivenPropertyChecks</code> <code>forAll</code> method that accepts a variable length
   * argument list of <code>PropertyCheckConfigParam</code> objects. Thus it is used with functions of all six arities.
   * Here are some examples:
   * </p>
   *
   * <pre>
   * forAll (minSize(1), maxSize(10)) { (a: String) =>
   *   a.length should equal ((a).length)
   * }
   *
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String) =>
   *   a.length + b.length should equal ((a + b).length)
   * }
   *
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String, c: String) =>
   *   a.length + b.length + c.length should equal ((a + b + c).length)
   * }
   *
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String, c: String, d: String) =>
   *   a.length + b.length + c.length + d.length should equal ((a + b + c + d).length)
   * }
   *
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String, c: String, d: String, e: String) =>
   *   a.length + b.length + c.length + d.length + e.length should equal ((a + b + c + d + e).length)
   * }
   *
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
   *   a.length + b.length + c.length + d.length + e.length + f.length should equal ((a + b + c + d + e + f).length)
   * }
   * </pre>
   *
   * <p>
   * In the first example above, the <code>ConfiguredPropertyCheck</code> object is returned by:
   * </p>
   *
   * <pre>
   * forAll (minSize(1), maxSize(10))
   * </pre>
   *
   * <p>
   * The code that follows is an invocation of one of the <code>ConfiguredPropertyCheck</code> <code>apply</code> methods:
   * </p>
   *
   * <pre>
   * { (a: String) =>
   *   a.length should equal ((a).length)
   * }
   * </pre>
   *
   * @param configParams a variable length list of <code>PropertyCheckConfigParam</code> objects that should override corresponding
   *   values in the <code>PropertyCheckConfig</code> implicitly passed to the <code>apply</code> methods of instances of this class.
   *
   * @author Bill Venners
  */
  class ConfiguredPropertyCheck(configParams: Seq[PropertyCheckConfigParam]) {

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by implicitly passed generators, modifying the values in the implicitly passed 
   * <code>PropertyGenConfig</code> object with parameter values passed to this object's constructor.
   *
   * <p>
   * Here's an example:
   * </p>
   *
   * <pre>
   * forAll (minSize(1), maxSize(10)) { (a: String) =>
   *   a.length should equal ((a).length)
   * }
   * </pre>
   *
   * @param fun the property check function to apply to the generated arguments
   */
    def apply[A](fun: (A) => Unit)
      (implicit
        config: PropertyCheckConfig,
      arbA: Arbitrary[A], shrA: Shrink[A]
      ) {
        val propF = { (a: A) =>
          val (unmetCondition, exception) =
            try {
              fun(a)
              (false, None)
            }
            catch {
              case e: UnmetConditionException => (true, None)
              case e => (false, Some(e))
            }
          !unmetCondition ==> (
            if (exception.isEmpty) Prop.passed else Prop.exception(exception.get)
          )
        }
        val prop = Prop.forAll(propF)
        val params = getParams(configParams, config)
        Checkers.doCheck(prop, params, "GeneratorDrivenPropertyChecks.scala", "apply")
    }

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by implicitly passed generators, modifying the values in the implicitly passed 
   * <code>PropertyGenConfig</code> object with parameter values passed to this object's constructor.
   *
   * <p>
   * Here's an example:
   * </p>
   *
   * <pre>
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String) =>
   *   a.length + b.length should equal ((a + b).length)
   * }
   * </pre>
   *
   * @param fun the property check function to apply to the generated arguments
   */
    def apply[A, B](fun: (A, B) => Unit)
      (implicit
        config: PropertyCheckConfig,
      arbA: Arbitrary[A], shrA: Shrink[A],
      arbB: Arbitrary[B], shrB: Shrink[B]
      ) {
        val propF = { (a: A, b: B) =>
          val (unmetCondition, exception) =
            try {
              fun(a, b)
              (false, None)
            }
            catch {
              case e: UnmetConditionException => (true, None)
              case e => (false, Some(e))
            }
          !unmetCondition ==> (
            if (exception.isEmpty) Prop.passed else Prop.exception(exception.get)
          )
        }
        val prop = Prop.forAll(propF)
        val params = getParams(configParams, config)
        Checkers.doCheck(prop, params, "GeneratorDrivenPropertyChecks.scala", "apply")
    }

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by implicitly passed generators, modifying the values in the implicitly passed 
   * <code>PropertyGenConfig</code> object with parameter values passed to this object's constructor.
   *
   * <p>
   * Here's an example:
   * </p>
   *
   * <pre>
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String, c: String) =>
   *   a.length + b.length + c.length should equal ((a + b + c).length)
   * }
   * </pre>
   *
   * @param fun the property check function to apply to the generated arguments
   */
    def apply[A, B, C](fun: (A, B, C) => Unit)
      (implicit
        config: PropertyCheckConfig,
      arbA: Arbitrary[A], shrA: Shrink[A],
      arbB: Arbitrary[B], shrB: Shrink[B],
      arbC: Arbitrary[C], shrC: Shrink[C]
      ) {
        val propF = { (a: A, b: B, c: C) =>
          val (unmetCondition, exception) =
            try {
              fun(a, b, c)
              (false, None)
            }
            catch {
              case e: UnmetConditionException => (true, None)
              case e => (false, Some(e))
            }
          !unmetCondition ==> (
            if (exception.isEmpty) Prop.passed else Prop.exception(exception.get)
          )
        }
        val prop = Prop.forAll(propF)
        val params = getParams(configParams, config)
        Checkers.doCheck(prop, params, "GeneratorDrivenPropertyChecks.scala", "apply")
    }

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by implicitly passed generators, modifying the values in the implicitly passed 
   * <code>PropertyGenConfig</code> object with parameter values passed to this object's constructor.
   *
   * <p>
   * Here's an example:
   * </p>
   *
   * <pre>
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String, c: String, d: String) =>
   *   a.length + b.length + c.length + d.length should equal ((a + b + c + d).length)
   * }
   * </pre>
   *
   * @param fun the property check function to apply to the generated arguments
   */
    def apply[A, B, C, D](fun: (A, B, C, D) => Unit)
      (implicit
        config: PropertyCheckConfig,
      arbA: Arbitrary[A], shrA: Shrink[A],
      arbB: Arbitrary[B], shrB: Shrink[B],
      arbC: Arbitrary[C], shrC: Shrink[C],
      arbD: Arbitrary[D], shrD: Shrink[D]
      ) {
        val propF = { (a: A, b: B, c: C, d: D) =>
          val (unmetCondition, exception) =
            try {
              fun(a, b, c, d)
              (false, None)
            }
            catch {
              case e: UnmetConditionException => (true, None)
              case e => (false, Some(e))
            }
          !unmetCondition ==> (
            if (exception.isEmpty) Prop.passed else Prop.exception(exception.get)
          )
        }
        val prop = Prop.forAll(propF)
        val params = getParams(configParams, config)
        Checkers.doCheck(prop, params, "GeneratorDrivenPropertyChecks.scala", "apply")
    }

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by implicitly passed generators, modifying the values in the implicitly passed 
   * <code>PropertyGenConfig</code> object with parameter values passed to this object's constructor.
   *
   * <p>
   * Here's an example:
   * </p>
   *
   * <pre>
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String, c: String, d: String, e: String) =>
   *   a.length + b.length + c.length + d.length + e.length should equal ((a + b + c + d + e).length)
   * }
   * </pre>
   *
   * @param fun the property check function to apply to the generated arguments
   */
    def apply[A, B, C, D, E](fun: (A, B, C, D, E) => Unit)
      (implicit
        config: PropertyCheckConfig,
      arbA: Arbitrary[A], shrA: Shrink[A],
      arbB: Arbitrary[B], shrB: Shrink[B],
      arbC: Arbitrary[C], shrC: Shrink[C],
      arbD: Arbitrary[D], shrD: Shrink[D],
      arbE: Arbitrary[E], shrE: Shrink[E]
      ) {
        val propF = { (a: A, b: B, c: C, d: D, e: E) =>
          val (unmetCondition, exception) =
            try {
              fun(a, b, c, d, e)
              (false, None)
            }
            catch {
              case e: UnmetConditionException => (true, None)
              case e => (false, Some(e))
            }
          !unmetCondition ==> (
            if (exception.isEmpty) Prop.passed else Prop.exception(exception.get)
          )
        }
        val prop = Prop.forAll(propF)
        val params = getParams(configParams, config)
        Checkers.doCheck(prop, params, "GeneratorDrivenPropertyChecks.scala", "apply")
    }

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by implicitly passed generators, modifying the values in the implicitly passed 
   * <code>PropertyGenConfig</code> object with parameter values passed to this object's constructor.
   *
   * <p>
   * Here's an example:
   * </p>
   *
   * <pre>
   * forAll (minSize(1), maxSize(10)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
   *   a.length + b.length + c.length + d.length + e.length + f.length should equal ((a + b + c + d + e + f).length)
   * }
   * </pre>
   *
   * @param fun the property check function to apply to the generated arguments
   */
    def apply[A, B, C, D, E, F](fun: (A, B, C, D, E, F) => Unit)
      (implicit
        config: PropertyCheckConfig,
      arbA: Arbitrary[A], shrA: Shrink[A],
      arbB: Arbitrary[B], shrB: Shrink[B],
      arbC: Arbitrary[C], shrC: Shrink[C],
      arbD: Arbitrary[D], shrD: Shrink[D],
      arbE: Arbitrary[E], shrE: Shrink[E],
      arbF: Arbitrary[F], shrF: Shrink[F]
      ) {
        val propF = { (a: A, b: B, c: C, d: D, e: E, f: F) =>
          val (unmetCondition, exception) =
            try {
              fun(a, b, c, d, e, f)
              (false, None)
            }
            catch {
              case e: UnmetConditionException => (true, None)
              case e => (false, Some(e))
            }
          !unmetCondition ==> (
            if (exception.isEmpty) Prop.passed else Prop.exception(exception.get)
          )
        }
        val prop = Prop.forAll(propF)
        val params = getParams(configParams, config)
        Checkers.doCheck(prop, params, "GeneratorDrivenPropertyChecks.scala", "apply")
    }
  }
"""

val propertyCheckForAllTemplate = """
  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by implicitly passed generators.
   *
   * <p>
   * Here's an example:
   * </p>
   *
   * <pre>
   * forAll { ($namesAndTypes$) =>
   *   $sumOfArgLengths$ should equal (($sumOfArgs$).length)
   * }
   * </pre>
   *
   * @param fun the property check function to apply to the generated arguments
   */
  def forAll[$alphaUpper$](fun: ($alphaUpper$) => Unit)
    (implicit
      config: PropertyCheckConfig,
$arbShrinks$
    ) {
      val propF = { ($argType$) =>
        val (unmetCondition, exception) =
          try {
            fun($alphaLower$)
            (false, None)
          }
          catch {
            case e: UnmetConditionException => (true, None)
            case e => (false, Some(e))
          }
        !unmetCondition ==> (
          if (exception.isEmpty) Prop.passed else Prop.exception(exception.get)
        )
      }
      val prop = Prop.forAll(propF)
      val params = getParams(Seq(), config)
      Checkers.doCheck(prop, params, "GeneratorDrivenPropertyChecks.scala", "forAll")
  }

  /**
   * Performs a property check by applying the specified property check function with the specified
   * argument names to arguments supplied by implicitly passed generators.
   *
   * <p>
   * Here's an example:
   * </p>
   *
   * <pre>
   * forAll ($argNames$) { ($namesAndTypes$) =>
   *   $sumOfArgLengths$ should equal (($sumOfArgs$).length)
   * }
   * </pre>
   *
   * @param fun the property check function to apply to the generated arguments
   */
  def forAll[$alphaUpper$]($argNameNamesAndTypes$, configParams: PropertyCheckConfigParam*)(fun: ($alphaUpper$) => Unit)
    (implicit
      config: PropertyCheckConfig,
$arbShrinks$
    ) {
      val propF = { ($argType$) =>
        val (unmetCondition, exception) =
          try {
            fun($alphaLower$)
            (false, None)
          }
          catch {
            case e: UnmetConditionException => (true, None)
            case e => (false, Some(e))
          }
        !unmetCondition ==> (
          if (exception.isEmpty) Prop.passed else Prop.exception(exception.get)
        )
      }
      val prop = Prop.forAll(propF)
      val params = getParams(configParams, config)
      Checkers.doCheck(prop, params, "GeneratorDrivenPropertyChecks.scala", "forAll", Some(List($argNameNames$)))
  }

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by the specified generators.
   *
   * <p>
   * Here's an example:
   * </p>
   *
   * <pre>
   * import org.scalacheck.Gen
   *
   * // Define your own string generator:
   * val famousLastWords = for {
   *   s <- Gen.oneOf("the", "program", "compiles", "therefore", "it", "should", "work")
   * } yield s
   * 
   * forAll ($famousArgs$) { ($namesAndTypes$) =>
   *   $sumOfArgLengths$ should equal (($sumOfArgs$).length)
   * }
   * </pre>
   *
   * @param fun the property check function to apply to the generated arguments
   */
  def forAll[$alphaUpper$]($genArgsAndTypes$, configParams: PropertyCheckConfigParam*)(fun: ($alphaUpper$) => Unit)
    (implicit
      config: PropertyCheckConfig,
$shrinks$
    ) {
      val propF = { ($argType$) =>
        val (unmetCondition, exception) =
          try {
            fun($alphaLower$)
            (false, None)
          }
          catch {
            case e: UnmetConditionException => (true, None)
            case e => (false, Some(e))
          }
        !unmetCondition ==> (
          if (exception.isEmpty) Prop.passed else Prop.exception(exception.get)
        )
      }
      val prop = Prop.forAll($genArgs$)(propF)
      val params = getParams(configParams, config)
      Checkers.doCheck(prop, params, "GeneratorDrivenPropertyChecks.scala", "forAll")
  }

  /**
   * Performs a property check by applying the specified property check function to named arguments
   * supplied by the specified generators.
   *
   * <p>
   * Here's an example:
   * </p>
   *
   * <pre>
   * import org.scalacheck.Gen
   *
   * // Define your own string generator:
   * val famousLastWords = for {
   *   s <- Gen.oneOf("the", "program", "compiles", "therefore", "it", "should", "work")
   * } yield s
   * 
   * forAll ($nameGenTuples$) { ($namesAndTypes$) =>
   *   $sumOfArgLengths$ should equal (($sumOfArgs$).length)
   * }
   * </pre>
   *
   * @param fun the property check function to apply to the generated arguments
   */
  def forAll[$alphaUpper$]($nameAndGenArgsAndTypes$, configParams: PropertyCheckConfigParam*)(fun: ($alphaUpper$) => Unit)
    (implicit
      config: PropertyCheckConfig,
$shrinks$
    ) {

$tupleBusters$

      val propF = { ($argType$) =>
        val (unmetCondition, exception) =
          try {
            fun($alphaLower$)
            (false, None)
          }
          catch {
            case e: UnmetConditionException => (true, None)
            case e => (false, Some(e))
          }
        !unmetCondition ==> (
          if (exception.isEmpty) Prop.passed else Prop.exception(exception.get)
        )
      }
      val prop = Prop.forAll($genArgs$)(propF)
      val params = getParams(configParams, config)
      Checkers.doCheck(prop, params, "GeneratorDrivenPropertyChecks.scala", "forAll")
  }
"""

val generatorDrivenPropertyChecksCompanionObjectVerbatimString = """

object GeneratorDrivenPropertyChecks extends GeneratorDrivenPropertyChecks
"""

val generatorSuitePreamble = """

import matchers.ShouldMatchers
import org.scalacheck.Gen

class GeneratorDrivenSuite extends FunSuite with GeneratorDrivenPropertyChecks with ShouldMatchers {

  val famousLastWords = for {
    s <- Gen.oneOf("the", "program", "compiles", "therefore", "it", "should", "work")
  } yield s
"""

val generatorSuiteTemplate = """
  test("generator-driven property that takes $n$ args, which succeeds") {

    forAll { ($namesAndTypes$) =>
      $sumOfArgLengths$ should equal (($sumOfArgs$).length)
    }
  }

  test("generator-driven property that takes $n$ args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll { ($namesAndTypes$) =>
        $sumOfArgLengths$ should be < 0
      }
    }
  }

  test("generator-driven property that takes $n$ named args, which succeeds") {

    forAll ($argNames$) { ($namesAndTypes$) =>
      $sumOfArgLengths$ should equal (($sumOfArgs$).length)
    }
  }

  test("generator-driven property that takes $n$ named args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ($argNames$) { ($namesAndTypes$) =>
        $sumOfArgLengths$ should be < 0
      }
    }
  }

  test("generator-driven property that takes $n$ args and generators, which succeeds") {

    forAll ($famousArgs$) { ($namesAndTypes$) =>
      $sumOfArgLengths$ should equal (($sumOfArgs$).length)
    }
  }

  test("generator-driven property that takes $n$ args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ($famousArgs$) { ($namesAndTypes$) =>
        $sumOfArgLengths$ should be < 0
      }
    }
  }

  test("generator-driven property that takes $n$ named args and generators, which succeeds") {

    forAll ($nameGenTuples$) { ($namesAndTypes$) =>
      $sumOfArgLengths$ should equal (($sumOfArgs$).length)
    }
  }

  test("generator-driven property that takes $n$ named args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ($nameGenTuples$) { ($namesAndTypes$) =>
        $sumOfArgLengths$ should be < 0
      }
    }
  }

  // Same thing, but with config params
  test("generator-driven property that takes $n$ args, which succeeds, with config params") {

    forAll (minSize(10), maxSize(20)) { ($namesAndTypes$) =>
      $sumOfArgLengths$ should equal (($sumOfArgs$).length)
    }
  }

  test("generator-driven property that takes $n$ args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (minSize(10), maxSize(20)) { ($namesAndTypes$) =>
        $sumOfArgLengths$ should be < 0
      }
    }
  }

  test("generator-driven property that takes $n$ named args, which succeeds, with config params") {

    forAll ($argNames$, minSize(10), maxSize(20)) { ($namesAndTypes$) =>
      $sumOfArgLengths$ should equal (($sumOfArgs$).length)
    }
  }

  test("generator-driven property that takes $n$ named args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ($argNames$, minSize(10), maxSize(20)) { ($namesAndTypes$) =>
        $sumOfArgLengths$ should be < 0
      }
    }
  }

  test("generator-driven property that takes $n$ args and generators, which succeeds, with config params") {

    forAll ($famousArgs$, minSize(10), maxSize(20)) { ($namesAndTypes$) =>
      $sumOfArgLengths$ should equal (($sumOfArgs$).length)
    }
  }

  test("generator-driven property that takes $n$ args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ($famousArgs$, minSize(10), maxSize(20)) { ($namesAndTypes$) =>
        $sumOfArgLengths$ should be < 0
      }
    }
  }

  test("generator-driven property that takes $n$ named args and generators, which succeeds, with config params") {

    forAll ($nameGenTuples$, minSize(10), maxSize(20)) { ($namesAndTypes$) =>
      $sumOfArgLengths$ should equal (($sumOfArgs$).length)
    }
  }

  test("generator-driven property that takes $n$ named args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ($nameGenTuples$, minSize(10), maxSize(20)) { ($namesAndTypes$) =>
        $sumOfArgLengths$ should be < 0
      }
    }
  }
"""

// For some reason that I don't understand, I need to leave off the stars before the <pre> when 
// they are next to ST commands. So I say  "   <pre>" sometimes instead of " * <pre>".

  val thisYear = Calendar.getInstance.get(Calendar.YEAR)
  val mainDir = new File("target/generated/src/main/scala/org/scalatest/prop")
  val testDir = new File("target/generated/src/test/scala/org/scalatest/prop")
  mainDir.mkdirs()
  testDir.mkdirs()

  def genPropertyChecks() {

    val bw = new BufferedWriter(new FileWriter("target/generated/src/main/scala/org/scalatest/prop/GeneratorDrivenPropertyChecks.scala"))
 
    try {
      val st = new org.antlr.stringtemplate.StringTemplate(copyrightTemplate)
      st.setAttribute("year", thisYear);
      bw.write(st.toString)
      bw.write(propertyCheckPreamble)
      val alpha = "abcdefghijklmnopqrstuv"
      for (i <- 1 to 6) {
        val st = new org.antlr.stringtemplate.StringTemplate(propertyCheckForAllTemplate)
        val alphaLower = alpha.take(i).mkString(", ")
        val alphaUpper = alpha.take(i).toUpperCase.mkString(", ")
        val argType = alpha.take(i).map(c => c + ": " + c.toUpper).mkString(", ")
        val strings = List.fill(i)("String").mkString(", ")
        val arbShrinks = alpha.take(i).toUpperCase.map(
          c => "      arb" + c + ": Arbitrary[" + c + "], shr" + c + ": Shrink[" + c + "]"
        ).mkString(",\n")
        val shrinks = alpha.take(i).toUpperCase.map(
          c => "      shr" + c + ": Shrink[" + c + "]"
        ).mkString(",\n")
        val sumOfArgLengths = alpha.take(i).map(_ + ".length").mkString(" + ")
        val namesAndTypes = alpha.take(i).map(_ + ": String").mkString(", ")
        val sumOfArgs = alpha.take(i).mkString(" + ")
        val genArgsAndTypes = alpha.take(i).toUpperCase.map(c => "gen" + c + ": Gen[" + c + "]").mkString(", ")
        val genArgs = alpha.take(i).toUpperCase.map(c => "gen" + c).mkString(", ")
        val famousArgs = List.fill(i)("famousLastWords").mkString(", ")
        val argNames = alpha.take(i).map("\"" + _ + "\"").mkString(", ")
        val argNameNames = alpha.take(i).toUpperCase.map("name" + _).mkString(", ")
        val argNameNamesAndTypes = alpha.take(i).toUpperCase.map("name" + _ + ": String").mkString(", ")
        val nameGenTuples = alpha.take(i).map("(\"" + _ + "\", famousLastWords)").mkString(", ")
        val nameAndGenArgsAndTypes = alpha.take(i).toUpperCase.map(c => "nameGen" + c + ": (String, Gen[" + c + "])").mkString(", ")
        val tupleBusters = alpha.take(i).toUpperCase.map(c => "      val (name" + c + ", gen" + c + ") = nameGen" + c).mkString("\n")
        st.setAttribute("n", i)
        st.setAttribute("argType", argType)
        st.setAttribute("arbShrinks", arbShrinks)
        st.setAttribute("shrinks", shrinks)
        st.setAttribute("alphaLower", alphaLower)
        st.setAttribute("alphaUpper", alphaUpper)
        st.setAttribute("strings", strings)
        st.setAttribute("sumOfArgLengths", sumOfArgLengths)
        st.setAttribute("namesAndTypes", namesAndTypes)
        st.setAttribute("sumOfArgs", sumOfArgs)
        st.setAttribute("genArgs", genArgs)
        st.setAttribute("genArgsAndTypes", genArgsAndTypes)
        st.setAttribute("famousArgs", famousArgs)
        st.setAttribute("argNames", argNames)
        st.setAttribute("tupleBusters", tupleBusters)
        st.setAttribute("nameGenTuples", nameGenTuples)
        st.setAttribute("nameAndGenArgsAndTypes", nameAndGenArgsAndTypes)
        st.setAttribute("argNameNames", argNameNames)
        st.setAttribute("argNameNamesAndTypes", argNameNamesAndTypes)
        bw.write(st.toString)
      }
      bw.write("}\n")
      bw.write(generatorDrivenPropertyChecksCompanionObjectVerbatimString)
    }
    finally {
      bw.close()
    }
  }

  def genGeneratorDrivenSuite() {

    val bw = new BufferedWriter(new FileWriter("target/generated/src/test/scala/org/scalatest/prop/GeneratorDrivenSuite.scala"))
 
    try {
      val st = new org.antlr.stringtemplate.StringTemplate(copyrightTemplate)
      st.setAttribute("year", thisYear);
      bw.write(st.toString)
      bw.write(generatorSuitePreamble)
      val alpha = "abcdefghijklmnopqrstuv"
      for (i <- 1 to 6) {
        val st = new org.antlr.stringtemplate.StringTemplate(generatorSuiteTemplate)
        val rowOfOnes = List.fill(i)("  1").mkString(", ")
        val rowOfTwos = List.fill(i)("  2").mkString(", ")
        val listOfIs = List.fill(i)("i").mkString(", ")
        val columnsOfOnes = List.fill(i)("        (" + rowOfOnes + ")").mkString(",\n")
        val columnsOfTwos = List.fill(i)("        (" + rowOfTwos + ")").mkString(",\n")
        val rawRows =                              
          for (idx <- 0 to 9) yield                
            List.fill(i)("  " + idx).mkString("        (", ", ", ")")
        val columnsOfIndexes = rawRows.mkString(",\n")
        val argNames = alpha.take(i).map("\"" + _ + "\"").mkString(", ")
        //val argNames = alpha.map("\"" + _ + "\"").take(i).mkString(", ")
        val names = alpha.take(i).mkString(", ")
        val namesAndTypes = alpha.take(i).map(_ + ": String").mkString(", ")
        val sumOfArgs = alpha.take(i).mkString(" + ")
        val sumOfArgLengths = alpha.take(i).map(_ + ".length").mkString(" + ")
        val famousArgs = List.fill(i)("famousLastWords").mkString(", ")
        val nameGenTuples = alpha.take(i).map("(\"" + _ + "\", famousLastWords)").mkString(", ")
        st.setAttribute("n", i)
        st.setAttribute("columnsOfOnes", columnsOfOnes)
        st.setAttribute("columnsOfTwos", columnsOfTwos)
        st.setAttribute("columnsOfIndexes", columnsOfIndexes)
        st.setAttribute("argNames", argNames)
        st.setAttribute("names", names)
        st.setAttribute("namesAndTypes", namesAndTypes)
        st.setAttribute("sumOfArgs", sumOfArgs)
        st.setAttribute("sumOfArgLengths", sumOfArgLengths)
        st.setAttribute("listOfIs", listOfIs)
        st.setAttribute("famousArgs", famousArgs)
        st.setAttribute("nameGenTuples", nameGenTuples)
        bw.write(st.toString)
      }

      bw.write("}\n")
    }
    finally {
      bw.close()
    }
  }

  genPropertyChecks()
  genGeneratorDrivenSuite()
}


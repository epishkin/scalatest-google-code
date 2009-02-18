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

import org.scalatest.Suite
import org.scalacheck.Arbitrary
import org.scalacheck.Shrink
import org.scalacheck.Arg
import org.scalacheck.Prop
import org.scalacheck.Test
import org.scalatest.prop.Helper._

/**
 * Trait that contains several &#8220;check&#8221; methods that perform ScalaCheck property checks.
 * If ScalaCheck finds a test case for which a property doesn't hold, the problem will be reported as a ScalaTest test failure.
 * 
 * <p>
 * To use ScalaCheck, you specify properties and, in some cases, generators that generate test data. You need not always
 * create generators, because ScalaCheck provides many default generators for you that can be used in many situations.
 * ScalaCheck will use the generators to generate test data and with that data run tests that check that the property holds.
 * Property-based tests can, therefore, give you a lot more testing for a lot less code than assertion-based tests.
 * Here's an example of using ScalaCheck from a <code>JUnit3Suite</code>:
 * </p>
 * <pre>
 * import org.scalatest.junit.JUnit3Suite
 * import org.scalatest.fun.Checkers
 * import org.scalacheck.Arbitrary._
 * import org.scalacheck.Prop._
 *
 * class MySuite extends JUnit3Suite with Checkers {
 *   def testConcat() {
 *     check((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
 *   }
 * }
 * </pre>
 * <p>
 * The <code>check</code> method, defined in <code>Checkers</code>, makes it easy to write property-based tests inside
 * ScalaTest, JUnit, and TestNG test suites. This example specifies a property that <code>List</code>'s <code>:::</code> method
 * should obey. ScalaCheck properties are expressed as function values that take the required
 * test data as parameters. ScalaCheck will generate test data using generators and 
repeatedly pass generated data to the function. In this case, the test data is composed of integer lists named <code>a</code> and <code>b</code>.
 * Inside the body of the function, you see:
 * </p>
 * <pre>
 * a.size + b.size == (a ::: b).size
 * </pre>
 * <p>
 * The property in this case is a <code>Boolean</code> expression that will yield true if the size of the concatenated list is equal
 * to the size of each individual list added together. With this small amount
 * of code, ScalaCheck will generate possibly hundreds of value pairs for <code>a</code> and <code>b</code> and test each pair, looking for
 * a pair of integers for which the property doesn't hold. If the property holds true for every value ScalaCheck tries,
 * <code>check</code> returns normally. Otherwise, <code>check</code> will complete abruptly with a <code>TestFailedException</code> that
 * contains information about the failure, including the values that cause the property to be false.
 * </p>
 *
 * <p>
 * For more information on using ScalaCheck properties, see the documentation for ScalaCheck, which is available
 * from <a href="http://code.google.com/p/scalacheck/">http://code.google.com/p/scalacheck/</a>.
 * </p>
 *
 * <p>
 * To execute a suite that mixes in <code>Checkers</code> with ScalaTest's <code>Runner</code>, you must include ScalaCheck's jar file on the class path or runpath.
 * This version of <code>Checkers</code> was tested with ScalaCheck version 1.1.1. This trait must
 * be mixed into a ScalaTest <code>Suite</code>, because its self type is <code>org.scalatest.Suite</code>.
 * </p>
 *
 * @author Bill Venners
 */
trait Checkers {

  this: Suite =>

  /**
   * Convert the passed 1-arg function into a property, and check it.
   *
   * @param f the function to be converted into a property and checked
   * @throws TestFailedException if a test case is discovered for which the property doesn't hold.
   */
  def check[A1,P](f: A1 => P)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1]
    ) {
    check(Prop.property(f)(p, a1, s1))
  }

  /**
   * Convert the passed 2-arg function into a property, and check it.
   *
   * @param f the function to be converted into a property and checked
   * @throws TestFailedException if a test case is discovered for which the property doesn't hold.
   */
  def check[A1,A2,P](f: (A1,A2) => P)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2]
    ) {
    check(Prop.property(f)(p, a1, s1, a2, s2))
  }

  /**
   * Convert the passed 3-arg function into a property, and check it.
   *
   * @param f the function to be converted into a property and checked
   * @throws TestFailedException if a test case is discovered for which the property doesn't hold.
   */
  def check[A1,A2,A3,P](f: (A1,A2,A3) => P)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3]
    ) {
    check(Prop.property(f)(p, a1, s1, a2, s2, a3, s3))
  }

  /**
   * Convert the passed 4-arg function into a property, and check it.
   *
   * @param f the function to be converted into a property and checked
   * @throws TestFailedException if a test case is discovered for which the property doesn't hold.
   */
  def check[A1,A2,A3,A4,P](f: (A1,A2,A3,A4) => P)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4]
    ) {
    check(Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4))
  }

  /**
   * Convert the passed 5-arg function into a property, and check it.
   *
   * @param f the function to be converted into a property and checked
   * @throws TestFailedException if a test case is discovered for which the property doesn't hold.
   */
  def check[A1,A2,A3,A4,A5,P](f: (A1,A2,A3,A4,A5) => P)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4],
      a5: Arbitrary[A5], s5: Shrink[A5]
    ) {
    check(Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5))
  }

  /**
   * Convert the passed 6-arg function into a property, and check it.
   *
   * @param f the function to be converted into a property and checked
   * @throws TestFailedException if a test case is discovered for which the property doesn't hold.
   */
  def check[A1,A2,A3,A4,A5,A6,P](f: (A1,A2,A3,A4,A5,A6) => P)
    (implicit
      p: P => Prop,
      a1: Arbitrary[A1], s1: Shrink[A1],
      a2: Arbitrary[A2], s2: Shrink[A2],
      a3: Arbitrary[A3], s3: Shrink[A3],
      a4: Arbitrary[A4], s4: Shrink[A4],
      a5: Arbitrary[A5], s5: Shrink[A5],
      a6: Arbitrary[A6], s6: Shrink[A6]
    ) {
    check(Prop.property(f)(p, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5, a6, s6))
  }

  /**
   * Check a property with the given testing parameters.
   *
   * @param p the property to check
   * @param prms the test parameters
   * @throws TestFailedException if a test case is discovered for which the property doesn't hold.
   */
  def check(p: Prop, prms: Test.Params) {
    val result = Test.check(prms, p)
    if (!result.passed) {
      throw newTestFailedException(prettyTestStats(result))
    }
  }

  /**
   * Check a property.
   *
   * @param p the property to check
   * @throws TestFailedException if a test case is discovered for which the property doesn't hold.
   */
  def check(p: Prop) {
    check(p, Test.defaultParams)
  }

  private def prettyTestStats(result: Test.Result) = result.status match {
    case Test.Proved(args) =>
      "OK, proved property:                   \n" + prettyArgs(args)
    case Test.Passed =>
      "OK, passed " + result.succeeded + " tests."
    case Test.Failed(args, labels) =>
      "Falsified after "+result.succeeded+" passed tests:\n"+prettyArgs(args)
    case Test.Exhausted =>
      "Gave up after only " + result.succeeded + " passed tests. " +
      result.discarded + " tests were discarded."
    case Test.PropException(args, e, labels) =>
      "Exception \"" + e + "\" raised on property evaluation:\n" +
      prettyArgs(args)
    case Test.GenException(e) =>
      "Exception \"" + e + "\" raised on argument generation."
  }

  private def prettyArgs(args: List[Arg]) = {
    val strs = for((a,i) <- args.zipWithIndex) yield (
      "> " +
      (if(a.label == "") "ARG_" + i else a.label) +
      " = \"" + a.arg +
      (if(a.shrinks > 0) "\" (" + a.shrinks + " shrinks)" else "\"")
    )
    strs.mkString("\n")
  }
}

/*
0 org.scalatest.prop.Checkers$class.check(Checkers.scala:194)
1 org.scalatest.ShouldContainElementSpec.check(ShouldContainElementSpec.scala:23)
2 org.scalatest.prop.Checkers$class.check(Checkers.scala:205)
3 org.scalatest.ShouldContainElementSpec.check(ShouldContainElementSpec.scala:23)
4 org.scalatest.prop.Checkers$class.check(Checkers.scala:96)
5 org.scalatest.ShouldContainElementSpec.check(ShouldContainElementSpec.scala:23)
6 org.scalatest.ShouldContainElementSpec$$anonfun$1$$anonfun$apply$1$$anonfun$apply$28.apply(ShouldContainElementSpec.scala:80)
*/
private[prop] object Helper {

  def newTestFailedException(message: String): TestFailedException = {

    val temp = new RuntimeException
    val stackTraceList = temp.getStackTrace.toList

    val fileNameIsCheckersDotScalaList: List[Boolean] =
      for (element <- stackTraceList) yield
        element.getFileName == "Checkers.scala"

    val methodNameIsCheckList: List[Boolean] =
      for (element <- stackTraceList) yield
        element.getMethodName == "check"

    // For element 0, the previous file name was not Checkers.scala, because there is no previous
    // one, so you start with false. For element 1, it depends on whether element 0 of the stack trace
    // had file name Checkers.scala, and so forth.
    val previousFileNameIsCheckersDotScalaList: List[Boolean] = false :: (fileNameIsCheckersDotScalaList.dropRight(1))

    // Zip these two related lists together. They now have two boolean values together, when both
    // are true, that's a stack trace element that should be included in the stack depth. In the 
    val zipped1 = methodNameIsCheckList zip previousFileNameIsCheckersDotScalaList
    val methodNameIsCheckAndPreviousFileNameIsCheckersDotScalaList: List[Boolean] =
      for ((methodNameIsCheck, previousFileNameIsCheckersDotScala) <- zipped1) yield
        methodNameIsCheck && previousFileNameIsCheckersDotScala

    // Zip the two lists together, that when one or the other is true is an include.
    val zipped2 = fileNameIsCheckersDotScalaList zip methodNameIsCheckAndPreviousFileNameIsCheckersDotScalaList
    val includeInStackDepthList: List[Boolean] =
      for ((fileNameIsCheckersDotScala, methodNameIsCheckAndPreviousFileNameIsCheckersDotScala) <- zipped2) yield
        fileNameIsCheckersDotScala || methodNameIsCheckAndPreviousFileNameIsCheckersDotScala

    val stackDepth = includeInStackDepthList.takeWhile(include => include).length

    new TestFailedException(message, stackDepth)
  }
}

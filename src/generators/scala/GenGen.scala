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
import org.scalacheck.Prop._
import org.scalacheck.Test.defaultParams

/**
 * Trait containing methods that faciliate property checks against generated data.
 *
 * @author Bill Venners
 */
trait GeneratorDrivenPropertyChecks extends Whenever {
"""

val propertyCheckForAllTemplate = """
  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by implicitly passed generators.
   *
   * @param fun the property check function to apply to the generated arguments
   */
  def forAll[$alphaUpper$](fun: ($alphaUpper$) => Unit)
    (implicit
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
      Checkers.doCheck(prop, defaultParams, "GeneratorDrivenPropertyChecks.scala", "forAll")
  }
"""

val generatorDrivenPropertyChecksCompanionObjectVerbatimString = """

object GeneratorDrivenPropertyChecks extends GeneratorDrivenPropertyChecks
"""

val generatorSuitePreamble = """

import matchers.ShouldMatchers

class GeneratorDrivenSuite extends FunSuite with GeneratorDrivenPropertyChecks with ShouldMatchers {
"""

val generatorSuiteTemplate = """
  test("generator-driven property that takes $n$ args, which succeeds") {

    forAll { ($namesAndTypes$) => $sumOfArgLengths$ should equal (($sumOfArgs$).length) }
  }

  test("generator-driven property that takes $n$ args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll { ($namesAndTypes$) => $sumOfArgLengths$ should be < 0 }
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
        st.setAttribute("n", i)
        st.setAttribute("argType", argType)
        st.setAttribute("arbShrinks", arbShrinks)
        st.setAttribute("alphaLower", alphaLower)
        st.setAttribute("alphaUpper", alphaUpper)
        st.setAttribute("strings", strings)
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
        val argNames = alpha.map("\"" + _ + "\"").take(i).mkString(", ")
        val names = alpha.take(i).mkString(", ")
        val namesAndTypes = alpha.take(i).map(_ + ": String").mkString(", ")
        val sumOfArgs = alpha.take(i).mkString(" + ")
        val sumOfArgLengths = alpha.take(i).map(_ + ".length").mkString(" + ")
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


import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import java.util.Calendar
import scala.collection.JavaConversions._

object GenTable extends Application {

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

import scala.collection.mutable.Builder
import scala.collection.mutable.ArrayBuffer
import scala.collection.IndexedSeqLike
import scala.collection.generic.CanBuildFrom
"""

val tableTemplate = """
/**
 * A table with $n$ columns.
 *
 * <p>
 * This table is a sequence of <code>Tuple$n$</code> objects, where each tuple represents one row of the table.
 * The first element of each tuple comprise the first column of the table, the second element of 
 * each tuple comprise the second column, and so on.  This table also carries with it
 * a <em>heading</em> tuple that gives string names to the columns of the table.
 * </p>
 *
 * <p>
 * A handy way to create a <code>TableFor$n$</code> is via an <code>apply</code> factory method in the <code>Table</code>
 * singleton object provided by the <code>PropertyChecks</code> trait. Here's an example:
 * </p>
 *
 * <pre>
 * val examples =
 *   Table(
 *     ($argNames$),
$columnsOfIndexes$
 *   )
 * </pre>
 *
 * <p>
 * Because you supplied $n$ members in each tuple, the type you'll get back will be a <code>TableFor$n$</code>.
 * <p>
 *
 * <p>
 * The table provides an <code>apply</code> method that takes a function with a parameter list that matches
 * the types and arity of the tuples contained in this table. The <code>apply</code> method will invoke the
 * function with the members of each row tuple passed as arguments, in ascending order by index. (<em>I.e.</em>,
 * the zeroth tuple is checked first, then the tuple with index 1, then index 2, and so on until all the rows
 * have been checked (or until a failure occurs). The function represents a property of the code under test
 * that should succeed for every row of the table. If the function returns normally, that indicates the property
 * check succeeded for that row. If the function completes abruptly with an exception, that indicates the
 * property check failed and the <code>apply</code> method will complete abruptly with a
 * <code>TablePropertyCheckFailedException</code> that wraps the exception thrown by the supplied property function.
 * </p>
 * 
 * <p>
 * The usual way you'd invoke the <code>apply</code> method that checks a property is via a <code>forAll</code> method
 * provided by trait <code>PropertyChecks</code>. The <code>forAll</code> method takes a <code>TableFor$n$</code> as its
 * first argument, then in a curried argument list takes the property check function. It invokes <code>apply</code> on
 * the <code>TableFor$n$</code>, passing in the property check function. Here's an example:
 * </p>
 *
 * <pre>
 * forAll (examples) { ($alphaLower$) =>
 *   $sumOfArgs$ should equal (a * $n$)
 * }
 * </pre>
 *
 * <p>
 * Because <code>TableFor$n$</code> is a <code>Seq[($alphaUpper$)]</code>, you can use it as a <code>Seq</code>. For example, here's how
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
 * Note: the <code>failureOf</code> method will execute the supplied code (a by-name parameter) and catch any exception. If no exception is thrown by the code,
 * <code>failureOf</code> will result in <code>None</code>, indicating the "property check" succeeded. If the supplied code completes
 * abruptly in an exception that would normally cause a test to fail, <code>failureOf</code> will result in a <code>Some</code> wrapping
 * that exception. For example, the previous for expression would give you:
 * </p>
 *
 * <pre>
 * Vector(None, None, None, None, None, None, None, Some(org.scalatest.TestFailedException: 7 equaled 7), None, None)
 * </pre>
 *
 * <p>
 * This shows that all the property checks succeeded, except for the one at index 7.
 * <p>
 *
 * @author Bill Venners 
 */
class TableFor$n$[$alphaUpper$](val heading: ($strings$), rows: ($alphaUpper$)*) extends IndexedSeq[($alphaUpper$)] with IndexedSeqLike[($alphaUpper$), TableFor$n$[$alphaUpper$]] {

  def apply(idx: Int): ($alphaUpper$) = rows(idx)

  def length: Int = rows.length

  override protected[this] def newBuilder: Builder[($alphaUpper$), TableFor$n$[$alphaUpper$]] =
    new ArrayBuffer mapResult { (buf: Seq[($alphaUpper$)]) =>
      new TableFor$n$(heading, buf: _*)
    }

  def apply(fun: ($alphaUpper$) => Unit) {
    for ((($alphaLower$), idx) <- rows.zipWithIndex) {
      try {
        fun($alphaLower$)
      }
      catch {
        case _: UnmetConditionException =>
        case e =>
          val ($alphaName$) = heading

          throw new TablePropertyCheckFailedException(
            FailureMessages("tablePropertyException", UnquotedString(e.getClass.getSimpleName)) + "\n" + 
              "  " + FailureMessages("thrownExceptionsMessage", if (e.getMessage == null) "None" else UnquotedString(e.getMessage)) + "\n" +
              (
                e match {
                  case sd: StackDepth if sd.failedCodeFileNameAndLineNumberString.isDefined =>
                    "  " + FailureMessages("thrownExceptionsLocation", UnquotedString(sd.failedCodeFileNameAndLineNumberString.get)) + "\n"
                  case _ => ""
                }
              ) +
              "  " + FailureMessages("occurredAtRow", idx) + "\n" +
$namesAndValues$
              "  )",
            Some(e),
            7, //getStackDepth("TableFor2.scala", "apply"),
            FailureMessages("undecoratedPropertyCheckFailureMessage"),
            List($alphaLower$),
            List($alphaName$),
            idx
          )
      }
    }
  }

  override def toString: String = stringPrefix + "(" + heading.toString + ", " +  rows.mkString(", ") + ")"
}

/**
 * Companion object for class <code>TableFor$n$</code> that provides an implicit <code>canBuildFrom</code> method
 * that enables higher order functions defined on <code>TableFor$n$</code> to return another <code>TableFor$n$</code>.
 *
 * @author Bill Venners 
 */
object TableFor$n$ {

  implicit def canBuildFrom[$alphaUpper$]: CanBuildFrom[TableFor$n$[$alphaUpper$], ($alphaUpper$), TableFor$n$[$alphaUpper$]] =
    new CanBuildFrom[TableFor$n$[$alphaUpper$], ($alphaUpper$), TableFor$n$[$alphaUpper$]] {
      def apply(): Builder[($alphaUpper$), TableFor$n$[$alphaUpper$]] =
        new ArrayBuffer mapResult { (buf: Seq[($alphaUpper$)]) =>
          new TableFor$n$(($argsNamedArg$))
        }
      def apply(from: TableFor$n$[$alphaUpper$]): Builder[($alphaUpper$), TableFor$n$[$alphaUpper$]] =
        new ArrayBuffer mapResult { (buf: Seq[($alphaUpper$)]) =>
          new TableFor$n$(from.heading, buf: _*)
        }
    }
}
"""

val tableObjectApplyTemplate = """
    def apply[$alphaUpper$](heading: ($strings$), rows: ($alphaUpper$)*) =
      new TableFor$n$(heading, rows: _*)
"""

val propertyCheckPreamble = """
trait PropertyChecks {

  def whenever(condition: => Boolean)(fun: => Unit) {
    if (!condition)
      throw new UnmetConditionException(condition _)
    fun
  }
"""

val propertyCheckForAllTemplate = """
  def forAll[$alphaUpper$](table: TableFor$n$[$alphaUpper$])(fun: ($alphaUpper$) => Unit) {
    table(fun)
  }
"""

val tableSuitePreamble = """

import matchers.ShouldMatchers

class TableSuite extends FunSuite with PropertyChecks with ShouldMatchers {
"""

val tableSuiteTemplate = """
  test("table for $n$ that succeeds") {

    val examples =
      Table(
        ($argNames$),
$columnsOfOnes$
      )

    forAll (examples) { ($names$) => $sumOfArgs$ should equal ($n$) }
  }

  test("table for $n$, which fails") {

    val examples =
      Table(
        ($argNames$),
$columnsOfTwos$
      )

    intercept[TablePropertyCheckFailedException] {
      forAll (examples) { ($names$) => $sumOfArgs$ should equal ($n$) }
    }
  }

  test("table for $n$ apply, length, and iterator methods work correctly") {

    val examples =
      Table(
        ($argNames$),
$columnsOfIndexes$
      )

    for (i <- 0 to 9) {
      examples(i) should equal ($listOfIs$)
    }

    examples.length should equal (10)

    var i = 0
    for (example <- examples.iterator) {
      example should equal ($listOfIs$)
      i += 1
    }

    examples.iterator.length should equal (10)
  }
"""

// For some reason that I don't understand, I need to leave off the stars before the <pre> when 
// they are next to ST commands. So I say  "   <pre>" sometimes instead of " * <pre>".

  val thisYear = Calendar.getInstance.get(Calendar.YEAR)
  val mainDir = new File("target/generated/src/main/scala/org/scalatest/prop")
  val testDir = new File("target/generated/src/test/scala/org/scalatest/prop")
  mainDir.mkdirs()
  testDir.mkdirs()

  def genTables() {

    val bw = new BufferedWriter(new FileWriter("target/generated/src/main/scala/org/scalatest/prop/Table.scala"))
 
    try {
      val st = new org.antlr.stringtemplate.StringTemplate(copyrightTemplate)
      st.setAttribute("year", thisYear);
      bw.write(st.toString)
      val alpha = "abcdefghijklmnopqrstuv"
      for (i <- 1 to 22) {
        val st = new org.antlr.stringtemplate.StringTemplate(tableTemplate)
        val alphaLower = alpha.take(i).mkString(", ")
        val alphaUpper = alpha.take(i).toUpperCase.mkString(", ")
        val alphaName = alpha.take(i).map(_ + "Name").mkString(", ")
        val namesAndValues = alpha.take(i).map(c => "              \"    \" + " + c + "Name + \" = \" + " + c).mkString("", " + \",\" + \"\\n\" +\n", " + \"\\n\" +\n")
        val strings = List.fill(i)("String").mkString(", ")
        val argsNamedArgSeq =
          for (argsIdx <- 0 until i) yield
            "\"" + "arg" + argsIdx + "\""
        val argsNamedArg = argsNamedArgSeq.mkString(",")                                  
        val sumOfArgs = alpha.take(i).mkString(" + ")
        val argNames = alpha.map("\"" + _ + "\"").take(i).mkString(", ")
        val rawRows =                              
          for (idx <- 0 to 9) yield                
            List.fill(i)("  " + idx).mkString(" *     (", ", ", ")")
        val columnsOfIndexes = rawRows.mkString(",\n")
        st.setAttribute("n", i)
        st.setAttribute("alphaLower", alphaLower)
        st.setAttribute("alphaUpper", alphaUpper)
        st.setAttribute("alphaName", alphaName)
        st.setAttribute("strings", strings)
        st.setAttribute("argsNamedArg", argsNamedArg)
        st.setAttribute("namesAndValues", namesAndValues)
        st.setAttribute("sumOfArgs", sumOfArgs)
        st.setAttribute("argNames", argNames)
        st.setAttribute("columnsOfIndexes", columnsOfIndexes)
        bw.write(st.toString)
      }
    }
    finally {
      bw.close()
    }
  }

  def genPropertyChecks() {

    val bw = new BufferedWriter(new FileWriter("target/generated/src/main/scala/org/scalatest/prop/PropertyChecks.scala"))
 
    try {
      val st = new org.antlr.stringtemplate.StringTemplate(copyrightTemplate)
      st.setAttribute("year", thisYear);
      bw.write(st.toString)
      bw.write(propertyCheckPreamble)
      val alpha = "abcdefghijklmnopqrstuv"
      for (i <- 1 to 22) {
        val st = new org.antlr.stringtemplate.StringTemplate(propertyCheckForAllTemplate)
        val alphaLower = alpha.take(i).mkString(", ")
        val alphaUpper = alpha.take(i).toUpperCase.mkString(", ")
        val strings = List.fill(i)("String").mkString(", ")
        st.setAttribute("n", i)
        st.setAttribute("alphaLower", alphaLower)
        st.setAttribute("alphaUpper", alphaUpper)
        st.setAttribute("strings", strings)
        bw.write(st.toString)
      }


      bw.write("\n  object Table {\n")
      for (i <- 1 to 22) {
        val st = new org.antlr.stringtemplate.StringTemplate(tableObjectApplyTemplate)
        val alphaLower = alpha.take(i).mkString(", ")
        val alphaUpper = alpha.take(i).toUpperCase.mkString(", ")
        val strings = List.fill(i)("String").mkString(", ")
        st.setAttribute("n", i)
        st.setAttribute("alphaLower", alphaLower)
        st.setAttribute("alphaUpper", alphaUpper)
        st.setAttribute("strings", strings)
        bw.write(st.toString)
      }

      bw.write("  }\n")
      bw.write("}\n")
      bw.write("\nobject PropertyChecks extends PropertyChecks\n\n")
    }
    finally {
      bw.close()
    }
  }

  def genTableSuite() {

    val bw = new BufferedWriter(new FileWriter("target/generated/src/test/scala/org/scalatest/prop/TableSuite.scala"))
 
    try {
      val st = new org.antlr.stringtemplate.StringTemplate(copyrightTemplate)
      st.setAttribute("year", thisYear);
      bw.write(st.toString)
      bw.write(tableSuitePreamble)
      val alpha = "abcdefghijklmnopqrstuv"
      for (i <- 1 to 22) {
        val st = new org.antlr.stringtemplate.StringTemplate(tableSuiteTemplate)
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
        val sumOfArgs = alpha.take(i).mkString(" + ")
        st.setAttribute("n", i)
        st.setAttribute("columnsOfOnes", columnsOfOnes)
        st.setAttribute("columnsOfTwos", columnsOfTwos)
        st.setAttribute("columnsOfIndexes", columnsOfIndexes)
        st.setAttribute("argNames", argNames)
        st.setAttribute("names", names)
        st.setAttribute("sumOfArgs", sumOfArgs)
        st.setAttribute("listOfIs", listOfIs)
        bw.write(st.toString)
      }

      bw.write("}\n")
    }
    finally {
      bw.close()
    }
  }

  genTables()
  genPropertyChecks()
  genTableSuite()
}

/*
$if (moreThanFour)$
 * <pre>
 * class MySuite extends FunSuite$num$[
 *   $exampleParams$
 * ] {
$else$
*/

/*
IAException was thrown...
Thrown exception's message: 1 did not equal 7
Occurred at row N (zero-based), which had values (
  n = 0,
  d = 1
)
*/

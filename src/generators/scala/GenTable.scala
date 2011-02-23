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
"""

val tableTemplate = """
class TableFor$n$[$alphaUpper$](heading: ($strings$), rows: ($alphaUpper$)*) {
  def apply(idx: Int): ($alphaUpper$) = rows(idx)
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

  test("table for $n$ apply method works correctly") {

    val examples =
      Table(
        ($argNames$),
$columnsOfIndexes$
      )

    for (i <- 0 to 9) {
      examples(i) should equal ($listOfIs$)
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
        st.setAttribute("n", i)
        st.setAttribute("alphaLower", alphaLower)
        st.setAttribute("alphaUpper", alphaUpper)
        st.setAttribute("alphaName", alphaName)
        st.setAttribute("strings", strings)
        st.setAttribute("namesAndValues", namesAndValues)
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

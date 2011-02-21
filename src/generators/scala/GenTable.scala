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
  def apply(fun: ($alphaUpper$) => Unit) {
    for ((($alphaLower$), idx) <- rows.zipWithIndex) {
      try {
        fun($alphaLower$)
      }
      catch {
        case _: UnmetConditionException =>
        case e =>
          val ($alphaName$) = heading

          throw new PropertyTestFailedException(
            FailureMessages("propertyException", UnquotedString(e.getClass.getSimpleName)) + "\n" + 
              "  " + FailureMessages("thrownExceptionsMessage", if (e.getMessage == null) "None" else e.getMessage) + "\n" +
              "  " + FailureMessages("occurredAtRow", idx) + "\n" +
$namesAndValues$
              "  )",
            Some(e),
            7, //getStackDepth("TableFor2.scala", "apply"),
            FailureMessages("propertyException", UnquotedString(e.getClass.getSimpleName)),
            List($alphaLower$),
            List($alphaName$)
          )
      }
    }
  }
}
"""

val tablesTraitApplyTemplate = """
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


// For some reason that I don't understand, I need to leave off the stars before the <pre> when 
// they are next to ST commands. So I say  "   <pre>" sometimes instead of " * <pre>".

  val thisYear = Calendar.getInstance.get(Calendar.YEAR)
  val dir = new File("target/generated/src/main/scala/org/scalatest/prop")
  dir.mkdirs()

  def genTables() {

    val bw = new BufferedWriter(new FileWriter("target/generated/src/main/scala/org/scalatest/prop/Table.scala"))
 
    try {
      val st = new org.antlr.stringtemplate.StringTemplate(copyrightTemplate)
      st.setAttribute("year", thisYear);
      bw.write(st.toString)
      val alpha = "abcdefghijklmnopqrstuv"
      for (i <- 2 to 22) {
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

      bw.write("\ntrait Tables {\n")
      for (i <- 2 to 22) {
        val st = new org.antlr.stringtemplate.StringTemplate(tablesTraitApplyTemplate)
        val alphaLower = alpha.take(i).mkString(", ")
        val alphaUpper = alpha.take(i).toUpperCase.mkString(", ")
        val strings = List.fill(i)("String").mkString(", ")
        st.setAttribute("n", i)
        st.setAttribute("alphaLower", alphaLower)
        st.setAttribute("alphaUpper", alphaUpper)
        st.setAttribute("strings", strings)
        bw.write(st.toString)
      }

      bw.write("}\n")
      bw.write("\nobject Tables extends Tables\n\n")
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
      for (i <- 2 to 22) {
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

      bw.write("}\n")
      bw.write("\nobject PropertyChecks extends PropertyChecks\n\n")
    }
    finally {
      bw.close()
    }
  }

  genTables()
  genPropertyChecks()
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

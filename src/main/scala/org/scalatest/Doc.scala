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
package org.scalatest

import scala.xml.Elem
import Suite.reportMarkupProvided
import Doc.stripMargin
import Doc.trimMarkup

/**
 * A <code>Doc</code> class that takes one XML node markup
 *  which will be returned from its <code>nestedSuites</code> method.
 *
 * <p>
 * For example, you can define a suite that always executes a list of
 * nested suites like this:
 * </p>
 *
 * <pre class="stHighlight">
 * class StepsSuite extends Suites(
 *   new Step1Suite,
 *   new Step2Suite,
 *   new Step3Suite,
 *   new Step4Suite,
 *   new Step5Suite
 * )
 * </pre>
 *
 * <p>
 * When <code>StepsSuite</code> is executed, it will execute its
 * nested suites in the passed order: <code>Step1Suite</code>, <code>Step2Suite</code>,
 * <code>Step3Suite</code>, <code>Step4Suite</code>, and <code>Step5Suite</code>.
 * </p>
 *
 * @param suitesToNest a sequence of <code>Suite</code>s to nest.
 *
 * @throws NullPointerException if <code>suitesToNest</code>, or any suite
 * it contains, is <code>null</code>.
 *
 * @author Bill Venners
 */
trait Doc extends Suite { thisDoc =>

  def body: Elem

  private val snippets: List[Snippet] = getSnippets(body.text)

  /*
   * Returns a list containing the suites mentioned in the body XML element,
   * in the order they were mentioned.
   */
  final override val nestedSuites = for (InsertedSuite(suite) <- snippets) yield suite
/*
println("^^^^^^^^^^^")
println(body.text)
println("###########")
println(snippets)
println("&&&&&&&&&&&")
*/

  override protected def runNestedSuites(reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    reportMarkupProvided(thisDoc, reporter, tracker, None, trimMarkup(stripMargin(body.text)), 0, true, None, None)
  }

  private[scalatest] def getSnippets(text: String): List[Snippet] = {
println("text: " + text)
    val lines = text.lines.toList
println("lines: " + lines)
    val pairs = lines map { line =>
      val trimmed = line.trim
      val suite =
        if (trimmed.startsWith("insert[") && trimmed.endsWith("]")) {
println("GOT HERE: " + trimmed + ", " + trimmed.substring(7).init)
          Some(trimmed.substring(7).init)
}
        else
          None
      (line, suite)
    }
println("pairs: " + pairs)
    // val zipped = pairs.zipWithIndex
    // val insertionIndexes = for (((_, Some(_)), index) <- zipped) yield index
// Output of my fold left is: List[Snippet] (left is a list of snippets, right is a pair
    (List[Snippet](Markup("")) /: pairs) { (left: List[Snippet], right: (String, Option[String])) =>
      right match {
        case (_, Some(className)) => InsertedSuite(thisDoc.getClass.getClassLoader.loadClass(className).newInstance.asInstanceOf[Suite]) :: left
        case (line, None) =>
          left.head match {
            case Markup(text) => Markup(text + "\n" + line) :: left.tail
            case _ => Markup(line) :: left
          }
      }
    }
  }

  private[scalatest] sealed trait Snippet
  private[scalatest] case class Markup(text: String) extends Snippet
  private[scalatest] case class InsertedSuite(suite: Suite) extends Snippet
}

object Doc {

  def insert[T <: Suite](implicit manifest: Manifest[T]): String = {
    val clazz = manifest.erasure.asInstanceOf[Class[T]]
    "\ninsert[" + clazz.getName + "]\n"
  }

  private[scalatest] def trimMarkup(text: String): String = {
    val lines = text.lines.toList
    val zipLines = lines.zipWithIndex
    val firstNonWhiteLine = zipLines.find { case (line, _) => !line.trim.isEmpty }
    val lastNonWhiteLine = zipLines.reverse.find { case (line, _) => !line.trim.isEmpty } 
    (firstNonWhiteLine, lastNonWhiteLine) match {
      case (None, None) => text.trim // Will be either (None, None) or (Some, Some)
      case (Some((_, frontIdx)), Some((_, backIdx))) => lines.take(backIdx + 1).drop(frontIdx).mkString("\n")
    }
  }

  private[scalatest] def stripMargin(text: String): String = {
    val lines = text.lines.toList
    val firstNonWhiteLine = lines.find(!_.trim.isEmpty)
    firstNonWhiteLine match {
      case None => text.trim
      case Some(nonWhiteLine) =>
        val initialWhite = nonWhiteLine.dropWhile(_.isWhitespace)
        val margin =  nonWhiteLine.length - initialWhite.length
        val choppedLines = lines map { line =>
          val strip = if (line.length > margin) margin else line.length
          line.substring(strip)
        }
        choppedLines.mkString("\n")
    }
  }
}


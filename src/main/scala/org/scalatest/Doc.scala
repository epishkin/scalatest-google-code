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
class Doc(markup: Elem) extends Suite { thisDoc =>

  /**
   * Returns a list containing the suites passed to the constructor in
   * the order they were passed.
   */
  final override val nestedSuites = Nil

  override protected def runNestedSuites(reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    reportMarkupProvided(thisDoc, reporter, tracker, None, markup.text, 0, true, None, None)
  }

}

object Doc {

  def insert[T <: Suite](implicit manifest: Manifest[T]): String = {
    val clazz = manifest.erasure.asInstanceOf[Class[T]]
    "\ninsert[" + clazz.getName + "]\n"
  }

  private[scalatest] def stripMargin(text: String): String = {
    if (!text.trim.isEmpty) {
      val lines = text.lines
      val initialWhite = lines.toList.head.dropWhile(_.isWhitespace)
      val margin = initialWhite.length
      val choppedLines = lines map (_.substring(margin))
      choppedLines.mkString
    }
    else text.trim
  }
}



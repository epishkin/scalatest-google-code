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
package org.scalatest

import NodeFamily._

/**
 * Trait that facilitates writing specification-oriented tests in a literary-programming style.
 *
 * <pre>
 * import org.scalatest.spec.Spec
 *
 * class MySpec extends Spec {
 *
 *   describe("Stack") {
 *
 *     it should "work right the first time" in {
 *       println("and how")
 *     }
 *   }
 * }
 * @author Bill Venners
 */
trait Spec extends Suite with Behavior {

  private def runTestsInBranch(branch: Branch, reporter: Reporter, stopper: Stopper) {
    branch match {
      case Description(_, descriptionName, level) => {
        val wrappedReporter = wrapReporterIfNecessary(reporter)
        val report = new SpecReport("what do I put here?", descriptionName, descriptionName, descriptionName, true)
        wrappedReporter.infoProvided(report)
      }
      case _ =>
    }
    branch.subNodes.reverse.foreach(
      _ match {
        case ex @ Example(parent, exampleFullName, specText, level, f) => {
          runExample(ex, reporter)
        }
        case SharedBehaviorNode(parent, sharedBehavior, level) => {
          // sharedBehavior.execute(None, reporter, stopper, Set(), Set(), Map(), None)
          runTestsInBranch(sharedBehavior.trunk, reporter, stopper)
          
          // (testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
          //    properties: Map[String, Any], distributor: Option[Distributor]
        }
        case branch: Branch => runTestsInBranch(branch, reporter, stopper)
      }
    )
  }
  
  private def runExample(example: Example, reporter: Reporter) {

    if (example == null || reporter == null)
      throw new NullPointerException

    val wrappedReporter = wrapReporterIfNecessary(reporter)

    val exampleSucceededIcon = Resources("exampleSucceededIconChar")
    val formattedSpecText = Resources("exampleIconPlusShortName", exampleSucceededIcon, example.specText)

    // A testStarting report won't normally show up in a specification-style output, but
    // will show up in a test-style output.
    val report = new SpecReport(getTestNameForReport(example.exampleFullName), "", example.specText, formattedSpecText, false)

    wrappedReporter.testStarting(report)

    try {
      example.f()

      val report = new SpecReport(getTestNameForReport(example.exampleFullName), "", example.specText, formattedSpecText, true)

      wrappedReporter.testSucceeded(report)
    }
    catch { 
      case e: Exception => {
        handleFailedTest(e, false, example.exampleFullName, example.specText, None, wrappedReporter)
      }
      case ae: AssertionError => {
        handleFailedTest(ae, false, example.exampleFullName, example.specText, None, wrappedReporter)
      }
    }
  }
    
  private[scalatest] override def getTestNameForReport(testName: String) = {

    if (testName == null)
      throw new NullPointerException("testName was null")

    suiteName + ": " + testName
  }

  private def handleFailedTest(t: Throwable, hasPublicNoArgConstructor: Boolean, exampleFullName: String,
      specText: String, rerunnable: Option[Rerunnable], reporter: Reporter) {

    val msg =
      if (t.getMessage != null) // [bv: this could be factored out into a helper method]
        t.getMessage
      else
        t.toString

    val report = new SpecReport(getTestNameForReport(exampleFullName), msg, specText, "- " + specText, true, Some(t), None)

    reporter.testFailed(report)
  }

  override def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
                        properties: Map[String, Any]) {
    runTestsInBranch(trunk, reporter, stopper)
  }
 
  override def expectedTestCount(includes: Set[String], excludes: Set[String]): Int = {
    countTestsInBranch(trunk)
  }

  override def testNames: Set[String] = {
    // I use a buf here to make it easier for my imperative brain to flatten the tree to a list
    var buf = List[String]()
    def traverse(branch: Branch, prefixOption: Option[String]) {
      for (node <- branch.subNodes)
        yield node match {
          case ex: Example => {
            buf ::= ex.exampleFullName 
          }
          case desc: Description => {
            val descName =
              prefixOption match {
                case Some(prefix) => Resources("prefixSuffix", prefix, desc.descriptionName) 
                case None => desc.descriptionName
              }
            traverse(desc, Some(descName))
          }
          case br: Branch => traverse(br, prefixOption)
        }
    }
    traverse(trunk, None)
    Set[String]() ++ buf.toList
  }
}


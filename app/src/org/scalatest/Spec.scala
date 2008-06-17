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
trait Spec extends Suite {

  private[scalatest] val trunk: Trunk = new Trunk
  private var currentBranch: Branch = trunk

  class ItWord {
    def should(exampleName: String) = new Inifier(exampleName)
    def should(behaveWord: BehaveWord) = new Likifier()
  }
  
  class BehaveWord {}

  private def registerExample(exampleRawName: String, needsShould: Boolean, f: => Unit) {
    currentBranch.subNodes ::=
      Example(currentBranch, getExampleFullName(exampleRawName, needsShould, currentBranch), exampleRawName, needsShould, getExampleShortName(exampleRawName, needsShould, currentBranch), currentBranch.level + 1, f _)
  }
  
  def specify(exampleRawName: String)(f: => Unit) {
    registerExample(exampleRawName, false, f)
  }
    
  class Inifier(exampleRawName: String) {
    def in(f: => Unit) {
      registerExample(exampleRawName, true, f)
    }
  }

  protected val behave = new BehaveWord
  class Likifier {
    def like(sharedBehavior: Behavior) {
      currentBranch.subNodes :::= sharedBehavior.trunk.subNodes.map(transformSharedExamplesFullName(_, currentBranch))
      // println("%$%$%$%$%$%$% sharedBehavior.trunk.subNodes: " + sharedBehavior.trunk.subNodes)
    }
  }
  
  protected val it = new ItWord

  protected def describe(name: String)(f: => Unit) {
    
    def insertBranch(newBranch: Branch, f: () => Unit) {
      val oldBranch = currentBranch
      currentBranch.subNodes ::= newBranch
      currentBranch = newBranch
      f()
      currentBranch = oldBranch
    }

    insertBranch(Description(currentBranch, name, currentBranch.level + 1), f _)
  }
  
  private def runTestsInBranch(branch: Branch, reporter: Reporter, stopper: Stopper) {
    branch match {
      case desc @ Description(_, descriptionName, level) => {

        def sendInfoProvidedMessage() {
          // Need to use the full name of the description, which includes all the descriptions it is nested inside
          // Call getPrefix and pass in this Desc, to get the full name
          val descriptionFullName = getPrefix(desc).trim
            
          val wrappedReporter = wrapReporterIfNecessary(reporter)
            
          // Call getTestNameForReport with the description, because that puts the Suite name
          // in front of the description, which looks good in the regular report.
          val descriptionNameForReport = getTestNameForReport(descriptionFullName)
          val report = new SpecReport(descriptionNameForReport, descriptionFullName, descriptionFullName, descriptionFullName, true)
          wrappedReporter.infoProvided(report)
        }
        
        // Only send an infoProvided message if the first thing in the subNodes is *not* sub-description, i.e.,
        // it is an example, because otherwise we get a lame description that doesn't have any examples under it.
        // But send it if the list is empty.
        if (desc.subNodes.isEmpty)
          sendInfoProvidedMessage() 
        else
          desc.subNodes.reverse.head match {
            case ex: Example => sendInfoProvidedMessage()           
            case _ => // Do nothing in this case
          }
      }
      case _ =>
    }
    branch.subNodes.reverse.foreach(
      _ match {
        case ex @ Example(parent, exampleFullName, exampleRawName, needsShould, specText, level, f) => {
          runExample(ex, reporter)
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
 
/*  private def findExample(exampleName: String): Option[Example] = {
    trunk.subNodes.foreach {
      
    }  
  }
  
  override def runTest(testName: Option[String], reporter: Reporter, stopper: Stopper, goodies: Map[String, Any]) {
   
    runTestsInBranch(trunk, reporter, stopper)
  }*/

  override def expectedTestCount(includes: Set[String], excludes: Set[String]): Int = {
    countTestsInBranch(trunk)
  }

  override def testNames: Set[String] = {
    // I use a buf here to make it easier for my imperative brain to flatten the tree to a list
    var buf = List[String]()
    def traverse(branch: Branch) {
      branch.subNodes.foreach(
        _ match {
          case ex: Example => buf ::= ex.exampleFullName 
          case br: Branch => traverse(br)
        }
      )
    }
    traverse(trunk)
    Set[String]() ++ buf.toList
  }
}


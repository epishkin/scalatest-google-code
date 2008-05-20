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
package org.scalatest.spec

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

  /*private*/ abstract class Node(parentOption: Option[Branch])
  /*private*/ abstract class Branch(parentOption: Option[Branch]) extends Node(parentOption) {
    var subNodes: List[Node] = Nil
  }
  /*private*/ case class Trunk extends Branch(None)
  /*private*/ case class Example(parent: Branch, exampleName: String, f: () => Unit) extends Node(Some(parent))
  /*private*/ case class SharedBehaviorNode(parent: Branch, sharedBehavior: SharedBehavior) extends Node(Some(parent))
  /*private*/ case class ExampleGivenReporter(parent: Branch, exampleName: String, f: (Reporter) => Unit) extends Node(Some(parent))
  /*private*/ case class Description(parent: Branch, descriptionName: String) extends Branch(Some(parent))

  /*private*/ val trunk: Trunk = new Trunk
  /*private*/ var currentBranch: Branch = trunk
  
  /*private*/ def runTestsInBranch(branch: Branch, reporter: Reporter, stopper: Stopper) {
    branch.subNodes.reverse.foreach(
      _ match {
        case ex @ Example(parent, exampleName, f) => {
          runExample(ex, reporter)
        }
        case sb @ SharedBehaviorNode(parent, sharedBehavior) => {
          sharedBehavior.execute(reporter, stopper)
        }
        case ex @ ExampleGivenReporter(parent, exampleName, f) => {
          runExample(Example(parent, exampleName, () => f(reporter)), reporter)
        }
        case branch: Branch => runTestsInBranch(branch, reporter, stopper)
      }
    )
  }

  private def getExampleFullName(example: Example): String = {
    def getPrefix(branch: Branch): String = {
      branch match {
        case Trunk() => ""
        // Call to getPrefix is not tail recursive, but I don't expect the describe nesting to be very deep
        case Description(parent, descriptionName) => Resources("prefixSuffix", getPrefix(parent), descriptionName)
      }
    }
    val prefix = getPrefix(example.parent)
    if (prefix.trim.isEmpty)
      Resources("itShould", example.exampleName) 
    else
      Resources("prefixShouldSuffix", prefix, example.exampleName)  
  }
  
  /*private*/ def runExample(example: Example, reporter: Reporter) {

    if (example == null || reporter == null)
      throw new NullPointerException

    val wrappedReporter = wrapReporterIfNecessary(reporter)

    val report = new Report(getTestNameForReport(getExampleFullName(example)), "")

    wrappedReporter.testStarting(report)

    try {

      example.f()

      val report = new Report(getTestNameForReport(getExampleFullName(example)), "")

      wrappedReporter.testSucceeded(report)
    }
    catch { 
      case e: Exception => {
        handleFailedTest(e, false, getExampleFullName(example), None, wrappedReporter)
      }
      case ae: AssertionError => {
        handleFailedTest(ae, false, getExampleFullName(example), None, wrappedReporter)
      }
    }
  }

  /*private*/ def handleFailedTest(t: Throwable, hasPublicNoArgConstructor: Boolean, testName: String,
      rerunnable: Option[Rerunnable], reporter: Reporter) {

    val msg =
      if (t.getMessage != null) // [bv: this could be factored out into a helper method]
        t.getMessage
      else
        t.toString

    val report = new Report(getTestNameForReport(testName), msg, Some(t), None)

    reporter.testFailed(report)
  }

  /*private*/ def countTestsInBranch(branch: Branch): Int = {
    var count = 0
    branch.subNodes.reverse.foreach(
      _ match {
        case Example(parent, exampleName, f) => count += 1
        case ExampleGivenReporter(parent, exampleName, f) => count += 1
        case SharedBehaviorNode(parent, sharedBehavior) => { 
          count += sharedBehavior.expectedExampleCount
        }
        case branch: Branch => count += countTestsInBranch(branch)
      }
    )
    count
  }

  override def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
                        properties: Map[String, Any]) {
    runTestsInBranch(trunk, reporter, stopper)
  }
 
  override def expectedTestCount(includes: Set[String], excludes: Set[String]): Int = {
    countTestsInBranch(trunk)
  }

  /*private*/ def getExampleFullName(prefixOption: Option[String], exampleName: String) =
    prefixOption match {
      case Some(prefix) => Resources("prefixShouldSuffix", prefix, exampleName)
      case None => Resources("itShould", exampleName)
    }

  override def testNames: Set[String] = {
    // I use a buf here to make it easier for my imperative brain to flatten the tree to a list
    var buf = List[String]()
    def traverse(branch: Branch, prefixOption: Option[String]) {
      for (node <- branch.subNodes)
        yield node match {
          case ex: Example => {
            buf ::= getExampleFullName(prefixOption, ex.exampleName) 
          }
          case ex: ExampleGivenReporter => {
            buf ::= getExampleFullName(prefixOption, ex.exampleName) 
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

  class ReporterInifier(exampleName: String) {
    def in(f: (Reporter) => Unit) {
      currentBranch.subNodes ::= ExampleGivenReporter(currentBranch, exampleName, f)
    }
  }
    
  class Inifier(exampleName: String) {
    def in(f: => Unit) {
      currentBranch.subNodes ::= Example(currentBranch, exampleName, f _)
    }
    def given(reporterWord: ReporterWord) = new ReporterInifier(exampleName)
  }

  protected class ReporterWord {}
  protected val reporter = new ReporterWord
  
  class Itifier {
    def should(exampleName: String) = new Inifier(exampleName)
    def should(behaveWord: BehaveWord) = new Likifier()
  }

  protected class BehaveWord {}
  protected val behave = new BehaveWord
  class Likifier {
    def like(sharedBehavior: SharedBehavior) {
      currentBranch.subNodes ::= SharedBehaviorNode(currentBranch, sharedBehavior)
    }
  }
  
  protected def it = new Itifier

  protected def describe(name: String)(f: => Unit) {
    insertBranch(Description(currentBranch, name), f _)
  }
  
  /*private*/ def insertBranch(newBranch: Branch, f: () => Unit) {
    val oldBranch = currentBranch
    currentBranch.subNodes ::= newBranch
    currentBranch = newBranch
    f()
    currentBranch = oldBranch
  }
              
  private[scalatest] override def getTestNameForReport(testName: String) = {

    if (testName == null)
      throw new NullPointerException("testName was null")

    suiteName + ": " + testName
  }

}


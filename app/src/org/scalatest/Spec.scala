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
private[scalatest] trait Spec extends Suite {
  
  private val trunk: Trunk = new Trunk
  private var currentBranch: Branch = trunk
  
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
        case ex @ Example(parent, exampleFullName, exampleShortName, level, f) => {
          runExample(ex, reporter)
        }
        case SharedBehaviorNode(parent, sharedBehavior, level) => {
          sharedBehavior.execute(None, reporter, stopper, Set(), Set(), Map(), None)
          
          // (testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
          //    properties: Map[String, Any], distributor: Option[Distributor]
        }
        case ExampleGivenReporter(parent, exampleFullName, exampleShortName, level, f) => {
          runExample(Example(parent, exampleFullName, exampleShortName, level, () => f(reporter)), reporter)
        }
        case branch: Branch => runTestsInBranch(branch, reporter, stopper)
      }
    )
  }

  private def getPrefix(branch: Branch): String = {
    branch match {
       case Trunk() => ""
      // Call to getPrefix is not tail recursive, but I don't expect the describe nesting to be very deep
      case Description(parent, descriptionName, level) => Resources("prefixSuffix", getPrefix(parent), descriptionName)
    }
  }
/*
  private def needsIt(branch: Branch): Boolean = {
    branch match {
      case Trunk() => true
      case Description(parent, descriptionName) => false
      // Later, will need to add cases for ignore and group, which call needsIt(parent)
    }
  }
*/
  private[scalatest] def getExampleFullName(exampleRawName: String, needsShould: Boolean): String = {
    val prefix = getPrefix(currentBranch).trim
    if (prefix.isEmpty) {
      if (needsShould) {
        // class MySpec extends Spec {
        //   it should "pop when asked" in {}
        // }
        // Should yield: "it should pop when asked"
        Resources("itShould", exampleRawName)
      }
      else {
        // class MySpec extends Spec {
        //   specify("It sure ought to pop when asked") {}
        // }
        // Should yield: "It sure ought to pop when asked"
        exampleRawName
      }
    }
    else {
      if (needsShould) {
        // class MySpec extends Spec {
        //   describe("A Stack") {
        //     it should "pop when asked" in {}
        //   }
        // }
        // Should yield: "A Stack should pop when asked"
        Resources("prefixShouldSuffix", prefix, exampleRawName)
      }
      else {
        // class MySpec extends Spec {
        //   describe("A Stack") {
        //     specify("must pop when asked") {}
        //   }
        // }
        // Should yield: "A Stack must pop when asked"
        Resources("prefixSuffix", prefix, exampleRawName)
      }
    }
  }


  private[scalatest] def getExampleShortName(exampleRawName: String, needsShould: Boolean): String = {
    val prefix = getPrefix(currentBranch).trim
    if (prefix.isEmpty) {
      if (needsShould) {
        // class MySpec extends Spec {
        //   it should "pop when asked" in {}
        // }
        // Should yield: "it should pop when asked"
        Resources("itShould", exampleRawName)
      }
      else {
        // class MySpec extends Spec {
        //   specify("It sure ought to pop when asked") {}
        // }
        // Should yield: "It sure ought to pop when asked"
        exampleRawName
      }
    }
    else {
      if (needsShould) {
        // class MySpec extends Spec {
        //   describe("A Stack") {
        //     it should "pop when asked" in {}
        //   }
        // }
        // Should yield: "should pop when asked"
        Resources("should", exampleRawName)
      }
      else {
        // class MySpec extends Spec {
        //   describe("A Stack") {
        //     specify("must pop when asked") {}
        //   }
        // }
        // Should yield: "A Stack must pop when asked"
        exampleRawName
      }
    }
  }

  private def runExample(example: Example, reporter: Reporter) {

    if (example == null || reporter == null)
      throw new NullPointerException

    val wrappedReporter = wrapReporterIfNecessary(reporter)

    val report = new SpecReport(getTestNameForReport(example.exampleFullName), "", example.exampleShortName, example.exampleShortName, false)

    wrappedReporter.testStarting(report)

    try {
      example.f()

      val report = new SpecReport(getTestNameForReport(example.exampleFullName), "", example.exampleShortName, example.exampleShortName, true)

      wrappedReporter.testSucceeded(report)
    }
    catch { 
      case e: Exception => {
        handleFailedTest(e, false, example.exampleFullName, example.exampleShortName, None, wrappedReporter)
      }
      case ae: AssertionError => {
        handleFailedTest(ae, false, example.exampleFullName, example.exampleShortName, None, wrappedReporter)
      }
    }
  }

  private def handleFailedTest(t: Throwable, hasPublicNoArgConstructor: Boolean, exampleFullName: String,
      exampleShortName: String, rerunnable: Option[Rerunnable], reporter: Reporter) {

    val msg =
      if (t.getMessage != null) // [bv: this could be factored out into a helper method]
        t.getMessage
      else
        t.toString

    val report = new SpecReport(getTestNameForReport(exampleFullName), msg, exampleShortName, exampleShortName, true, Some(t), None)

    reporter.testFailed(report)
  }

  private def countTestsInBranch(branch: Branch): Int = {
    var count = 0
    branch.subNodes.reverse.foreach(
      _ match {
        case Example(parent, exampleFullName, exampleShortName, level, f) => count += 1
        case ExampleGivenReporter(parent, exampleFullName, exampleShortName, level, f) => count += 1
        case SharedBehaviorNode(parent, sharedBehavior, level) => { 
          count += sharedBehavior.expectedTestCount(Set(), Set()) // TODO: Should I call this? What about the includes and excludes?
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

  override def testNames: Set[String] = {
    // I use a buf here to make it easier for my imperative brain to flatten the tree to a list
    var buf = List[String]()
    def traverse(branch: Branch, prefixOption: Option[String]) {
      for (node <- branch.subNodes)
        yield node match {
          case ex: Example => {
            buf ::= ex.exampleFullName 
          }
          case ex: ExampleGivenReporter => {
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

  private def registerExample(exampleRawName: String, needsShould: Boolean, f: => Unit) {
    currentBranch.subNodes ::=
      Example(currentBranch, getExampleFullName(exampleRawName, needsShould), getExampleShortName(exampleRawName, needsShould), currentBranch.level + 1, f _)
  }
  
  private def registerExampleGivenReporter(exampleRawName: String, needsShould: Boolean, f: (Reporter) => Unit) {
    currentBranch.subNodes ::=
      ExampleGivenReporter(currentBranch, getExampleFullName(exampleRawName, true), getExampleShortName(exampleRawName, needsShould), currentBranch.level + 1, f)
  }

  def specify(exampleRawName: String)(f: => Unit) {
    registerExample(exampleRawName, false, f)
  }
    
  def specifyGivenReporter(exampleRawName: String)(f: (Reporter) => Unit) {
    registerExampleGivenReporter(exampleRawName, false, f)
  }
    
  class Inifier(exampleRawName: String) {
    def in(f: => Unit) {
      registerExample(exampleRawName, true, f)
    }
    /*
    def in(f: Reporter => Unit) {
      // Does this compile? No. Ask Martin.
    }*/
    def given(reporterWord: ReporterWord) = new ReporterInifier(exampleRawName)
  }

  class ReporterInifier(exampleRawName: String) {
    def in(f: (Reporter) => Unit) {
//      currentBranch.subNodes ::=
//        ExampleGivenReporter(currentBranch, getExampleFullName(exampleRawName, true), getExampleShortName(exampleRawName, true), f)
      registerExampleGivenReporter(exampleRawName, true, f)
    }
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
    def like(sharedBehavior: Behavior) {
      currentBranch.subNodes ::= SharedBehaviorNode(currentBranch, sharedBehavior, currentBranch.level + 1)
    }
  }
  
  protected val it = new Itifier

  protected def describe(name: String)(f: => Unit) {
    insertBranch(Description(currentBranch, name, currentBranch.level + 1), f _)
  }
  
  private def insertBranch(newBranch: Branch, f: () => Unit) {
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


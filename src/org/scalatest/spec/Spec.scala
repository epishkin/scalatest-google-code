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
 * class MySuite extends Spec {
 *
 *   share("a non-empty stack") {
 *     it should "return the top when sent #peek" in {
 *       println("and how")
 *     }
 *   }
 *
 *   describe("Stack") {
 *
 *     before each {
 *       println("do the setup thing")
 *     }
 *
 *     it should "work right the first time" in {
 *       println("and how")
 *     }
 *
 *     it should behave like "a non-empty stack"
 *   }
 * }
 * @author Bill Venners
 */
trait Spec extends Suite {

  private abstract class Node(parentOption: Option[Branch])
  private abstract class Branch(parentOption: Option[Branch]) extends Node(parentOption) {
    var subNodes: List[Node] = Nil
    var beforeEach: Option[() => Unit] = None
    var afterEach: Option[() => Unit] = None
    var beforeAll: Option[() => Unit] = None
    var afterAll: Option[() => Unit] = None
    def sharedBehaviorIsInScope(behaviorName: String): Boolean = {
      val sharedBehaviorExistsInSubNodes: Boolean =
        subNodes.exists(
          _ match {
            case SharedBehavior(parent, `behaviorName`) => true
            case _ => false
          }
        )
       sharedBehaviorExistsInSubNodes || (
         parentOption match {
           case Some(parent) => parent.sharedBehaviorIsInScope(behaviorName)
           case None => false
         }
       )
    }
    def invokeSharedBehavior(behaviorName: String, reporter: Reporter, stopper: Stopper) {
      val sharedBehaviorOption: Option[SharedBehavior] = {
        subNodes.find(
          _ match {
            case SharedBehavior(parent, `behaviorName`) => true
            case _ => false
          }
        ).asInstanceOf[Option[SharedBehavior]]
      }
      sharedBehaviorOption match {
        case Some(sharedBehavior) => runTestsInBranch(sharedBehavior, reporter, stopper)
        case None => {
          parentOption match {
            case Some(parent) => parent.invokeSharedBehavior(behaviorName, reporter, stopper)
            case None => throw new NoSuchElementException("A requested shared behavior was not found: " + behaviorName)
          }
        }
      } 
    }
    def countTestsInSharedBehavior(behaviorName: String): Int = {
      val sharedBehaviorOption: Option[SharedBehavior] = {
        subNodes.find(
          _ match {
            case SharedBehavior(parent, `behaviorName`) => true
            case _ => false
          }
        ).asInstanceOf[Option[SharedBehavior]]
      }
      sharedBehaviorOption match {
        case Some(sharedBehavior) => countTestsInBranch(sharedBehavior)
        case None => {
          parentOption match {
            case Some(parent) => parent.countTestsInSharedBehavior(behaviorName)
            case None => throw new NoSuchElementException("A requested shared behavior was not found: " + behaviorName)
          }
        }
      } 
    }
  }
  private case class Example(parent: Branch, exampleName: String, f: () => Unit) extends Node(Some(parent))
  private case class Description(parent: Branch, descriptionName: String) extends Branch(Some(parent))
  private case class SharedBehavior(parent: Branch, behaviorName: String) extends Branch(Some(parent))
  private case class SharedBehaviorInvocation(parent: Branch, behaviorName: String) extends Node(Some(parent))

  private val trunk: Branch = new Branch(None) {}
  private var currentBranch: Branch = trunk
  
  private def runTestsInBranch(branch: Branch, reporter: Reporter, stopper: Stopper) {
    branch.subNodes.reverse.foreach(
      _ match {
        case ex @ Example(parent, exampleName, f) => {
          parent.beforeEach match {
            case Some(be) => be()
            case None => 
          }
          runExample(ex, reporter)
          parent.afterEach match {
            case Some(af) => af()
            case None => 
          }
        }
        case sb: SharedBehavior =>
        case SharedBehaviorInvocation(parent, behaviorName) => parent.invokeSharedBehavior(behaviorName, reporter, stopper)
        case branch: Branch => runTestsInBranch(branch, reporter, stopper)
      }
    )
  }

  private  def runExample(example: Example, reporter: Reporter) {

    if (example == null || reporter == null)
      throw new NullPointerException

    val wrappedReporter = wrapReporterIfNecessary(reporter)

    val report = new Report(getTestNameForReport(example.exampleName), "")

    wrappedReporter.testStarting(report)

    try {

      example.f()

      val report = new Report(getTestNameForReport(example.exampleName), "")

      wrappedReporter.testSucceeded(report)
    }
    catch { 
      case e: Exception => {
        handleFailedTest(e, false, example.exampleName, None, wrappedReporter)
      }
      case ae: AssertionError => {
        handleFailedTest(ae, false, example.exampleName, None, wrappedReporter)
      }
    }
  }

  private def handleFailedTest(t: Throwable, hasPublicNoArgConstructor: Boolean, testName: String,
      rerunnable: Option[Rerunnable], reporter: Reporter) {

    val msg =
      if (t.getMessage != null) // [bv: this could be factored out into a helper method]
        t.getMessage
      else
        t.toString

    val report = new Report(getTestNameForReport(testName), msg, Some(t), None)

    reporter.testFailed(report)
  }

  private def countTestsInBranch(branch: Branch): Int = {
    var count = 0
    branch.subNodes.reverse.foreach(
      _ match {
        case Example(parent, exampleName, f) => count += 1
        case sb: SharedBehavior =>
        case SharedBehaviorInvocation(parent, behaviorName) => count += parent.countTestsInSharedBehavior(behaviorName)
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
            val exName =
              prefixOption match {
                case Some(prefix) => Resources("prefixShouldSuffix", prefix, ex.exampleName)
                case None => Resources("itShould", ex.exampleName)
              }
            buf ::= exName 
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
  
  class Inifier(name: String) {
    def in(f: => Unit) {
      currentBranch.subNodes ::= Example(currentBranch, name, f _)
    }
  }

  class Itifier {
    def should(exampleName: String) = new Inifier(exampleName)
    def should(behaveWord: Behavifier) = new Behavifier
  }

  class Behavifier {
    def like(sharedBehaviorName: String) {
       if (currentBranch.sharedBehaviorIsInScope(sharedBehaviorName))
         currentBranch.subNodes ::= SharedBehaviorInvocation(currentBranch, sharedBehaviorName)
       else
         throw new NoSuchElementException("A requested shared behavior was not found: " + sharedBehaviorName)
     }
  }

  class Beforifier {
    def each(f: => Unit) {
      currentBranch.beforeEach match {
        case Some(x) => throw new RuntimeException("Multiple 'before each' clauses found in same describe or share clause.")
        case None => currentBranch.beforeEach = Some(f _)
      }
    }
    def all(f: => Unit) {
      println("do something before all examples")
    }
  }

  class Afterizer {
    def each(f: => Unit) {
      currentBranch.afterEach match {
        case Some(x) => throw new RuntimeException("Multiple 'arfter each' clauses found in same describe or share clause.")
        case None => currentBranch.afterEach = Some(f _)
      }
    }
    def all(f: => Unit) {
      println("do something after all examples")
    }
  }

  protected def it = new Itifier

  protected def behave = new Behavifier
  protected def before = new Beforifier
  protected def after = new Afterizer

  protected def describe(name: String)(f: => Unit) {
    insertBranch(Description(currentBranch, name), f _)
  }
  
  protected def share(name: String)(f: => Unit) {
    insertBranch(SharedBehavior(currentBranch, name), f _)
  }

  private def insertBranch(newBranch: Branch, f: () => Unit) {
    val oldBranch = currentBranch
    currentBranch.subNodes ::= newBranch
    currentBranch = newBranch
    f()
    currentBranch = oldBranch
  }
}


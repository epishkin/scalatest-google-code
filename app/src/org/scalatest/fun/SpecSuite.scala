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
package org.scalatest.fun

/**
 * Trait that facilitates writing specification-oriented tests in a literary-programming style.
 *
 * <pre>
 * import org.scalatest.fun.SpecSuite
 *
 * class MySuite extends SpecSuite {
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
trait SpecSuite extends Suite {

  private abstract class Node(parentOption: Option[Branch])
  private abstract class Branch(parentOption: Option[Branch]) extends Node(parentOption) {
    var subNodes: List[Node] = Nil
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
    def invokeSharedBehavior(behaviorName: String) {
      val sharedBehaviorOption: Option[SharedBehavior] = {
        subNodes.find(
          _ match {
            case SharedBehavior(parent, `behaviorName`) => true
            case _ => false
          }
        ).asInstanceOf[Option[SharedBehavior]]
      }
      sharedBehaviorOption match {
        case Some(sharedBehavior) => runTestsInBranch(sharedBehavior)
        case None => {
          parentOption match {
            case Some(parent) => parent.invokeSharedBehavior(behaviorName)
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
  
  private def runTestsInBranch(branch: Branch) {
    branch.subNodes.reverse.foreach(
      _ match {
        case Example(parent, exampleName, f) => f()
        case sb: SharedBehavior =>
        case SharedBehaviorInvocation(parent, behaviorName) => parent.invokeSharedBehavior(behaviorName)
        case branch: Branch => runTestsInBranch(branch)
      }
    )
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
    runTestsInBranch(trunk)
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
      println("do something before each example")
    }
    def all(f: => Unit) {
      println("do something before all examples")
    }
  }

  class Afterizer {
    def each(f: => Unit) {
      println("do something after each example")
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
  

/*
  override def suiteName = specName

  protected def doBefore(f: => Unit) {
    println("registered doBefore")
  }

  protected def doAfter(f: => Unit) {
    println("registered doAfter")
  }

  protected def ignore(f: => Unit) {
    println("registered ignore")
  }

  private def registerShould(sut: String) {
    println("registered sut with should: " + sut)
  }

  private def registerCan(sut: String) {
    println("registered sut with can: " + sut)
  }

  private def unregisterShould(sut: String) {
    println("unregistered sut with should: " + sut)
  }

  private def unregisterCan(sut: String) {
    println("unregistered sut with can: " + sut)
  }

  private def registerExample(example: String, f: => Unit) {
    println("registered example: " + example)
  }

  class InWrapper(spec: SpecSuite, example: String) {
    def >>(f: => Unit) {
      in(f)
    }
    def in(f: => Unit) {
      spec.registerExample(example, f)
    }
  }

  class CanWrapper(spec: SpecSuite, sut: String) {
    def can(f: => Unit) {
      spec.registerCan(sut)
      f
      spec.unregisterCan(sut)
    }
  }

  class ShouldWrapper(spec: SpecSuite, sut: String) {
    def should(f: => Unit) {
      spec.registerShould(sut)
      f
      spec.unregisterShould(sut)
    }
  }

  implicit def declare(sut: String): ShouldWrapper = new ShouldWrapper(this, sut)
  implicit def forExample(example: String): InWrapper = new InWrapper(this, example)
  implicit def declareCan(sut: String): CanWrapper = new CanWrapper(this, sut)
*/
}


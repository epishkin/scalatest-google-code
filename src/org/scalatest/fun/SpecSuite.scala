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

  trait Node
  abstract class Branch extends Node {
    var subNodes: List[Node] = Nil
  }
  case class Example(exampleName: String, f: () => Unit) extends Node
  case class Description(descriptionName: String) extends Branch

  private val trunk: Branch = new Branch {}
  private var currentBranch: Branch = trunk
  
  class Inifier(name: String) {
    def in(f: => Unit) {
      currentBranch.subNodes ::= Example(name, f _)
    }
  }

  override def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
                        properties: Map[String, Any]) {
    def traverse(branch: Branch) {
      branch.subNodes.reverse.foreach(
        _ match {
          case Example(exampleName, f) => f()
          case branch: Branch => traverse(branch)
        }
      )
    }
    traverse(trunk)
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
  
  class CousinIt {
    def should(exampleName: String) = new Inifier(exampleName)
    def should(behaveWord: CousinBehave) = new CousinBehave
  }

  class CousinBehave {
    def like(sharedBehaviorName: String) = println("it should behave like " + sharedBehaviorName)
  }

  class CousinBefore {
    def each(f: => Unit) {
      println("do something before each example")
    }
  }

  protected def it = new CousinIt

  protected def behave = new CousinBehave
  protected def before = new CousinBefore

  protected def describe(name: String)(f: => Unit) {
    val oldBranch = currentBranch
    val newBranch = Description(name)
    currentBranch.subNodes ::= newBranch
    currentBranch = newBranch
    f
    currentBranch = oldBranch
  }
  protected def share(name: String)(f: => Unit) { println("sharing " + name); f }


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


package org.scalatest

import NodeFamily._

trait Behavior {

  private[scalatest] val trunk: Trunk = new Trunk
  private var currentBranch: Branch = trunk

  private[scalatest] def countTestsInBranch(branch: Branch): Int = {
    var count = 0
    branch.subNodes.reverse.foreach(
      _ match {
        case Example(parent, exampleFullName, exampleRawName, needsShould, exampleShortName, level, f) => count += 1
        case SharedBehaviorNode(parent, sharedBehavior, level) => { 
          count += countTestsInBranch(sharedBehavior.trunk) // TODO: Will need to handle includes and excludes?
        }
        case branch: Branch => count += countTestsInBranch(branch)
      }
    )
    count
  }

  private def registerExample(exampleRawName: String, needsShould: Boolean, f: => Unit) {
    currentBranch.subNodes ::=
      Example(currentBranch, Behavior.getExampleFullName(exampleRawName, needsShould, currentBranch), exampleRawName, needsShould, Behavior.getExampleShortName(exampleRawName, needsShould, currentBranch), currentBranch.level + 1, f _)
  }
  
  def specify(exampleRawName: String)(f: => Unit) {
    registerExample(exampleRawName, false, f)
  }
    
  class Inifier(exampleRawName: String) {
    def in(f: => Unit) {
      registerExample(exampleRawName, true, f)
    }
  }

  class ItWord {
    def should(exampleName: String) = new Inifier(exampleName)
    def should(behaveWord: BehaveWord) = new Likifier()
  }

  private def transformTheParent(node: Node, newParent: Branch) = {
    node match {
      case Example(oldParent, exampleFullName, exampleRawName, needsShould, exampleShortName, level, f) => 
        Example(newParent, Behavior.getExampleFullName(exampleRawName, needsShould, newParent), exampleRawName, needsShould, Behavior.getExampleShortName(exampleRawName, needsShould, newParent), level, f)
      case oldDesc @ Description(oldParent, descriptionName, level) =>
        val newDesc = Description(newParent, descriptionName, level)
        newDesc.subNodes = oldDesc.subNodes
        newDesc
      case _ => node
    }
  }
  
  private def transformSharedExamplesFullName(node: Node, newParent: Branch): Node = {
  
    node match {
      case Example(oldParent, exampleFullName, exampleRawName, needsShould, exampleShortName, level, f) => 
        Example(oldParent, Behavior.getExampleFullName(exampleRawName, needsShould, currentBranch), exampleRawName, needsShould, Behavior.getExampleShortName(exampleRawName, needsShould, currentBranch), level, f)
      case oldDesc @ Description(oldParent, descriptionName, level) =>
        val newDesc = Description(newParent, descriptionName, level)
        newDesc.subNodes = oldDesc.subNodes.map(transformTheParent(_, newDesc))
        println("**&*&*&*&*&*&*&*&*& oldDesc.subNodes: " + oldDesc.subNodes)
        newDesc
      case _ => node
    }
  }
  
  protected class BehaveWord {}
  protected val behave = new BehaveWord
  class Likifier {
    def like(sharedBehavior: Behavior) {
      // currentBranch.subNodes ::= SharedBehaviorNode(currentBranch, sharedBehavior, currentBranch.level + 1)
      currentBranch.subNodes :::= sharedBehavior.trunk.subNodes.map(transformSharedExamplesFullName(_, currentBranch))
      println("%$%$%$%$%$%$% sharedBehavior.trunk.subNodes: " + sharedBehavior.trunk.subNodes)
    }
  }
  
  protected val it = new ItWord

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
}

private[scalatest] object Behavior {
  protected[scalatest] def getPrefix(branch: Branch): String = {
    branch match {
       case Trunk() => ""
      // Call to getPrefix is not tail recursive, but I don't expect the describe nesting to be very deep
      case Description(parent, descriptionName, level) => Resources("prefixSuffix", getPrefix(parent), descriptionName)
    }
  }
  
    private[scalatest] def getExampleFullName(exampleRawName: String, needsShould: Boolean, parent: Branch): String = {
    val prefix = Behavior.getPrefix(parent).trim
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

  private[scalatest] def getExampleShortName(exampleRawName: String, needsShould: Boolean, parent: Branch): String = {
    val prefix = Behavior.getPrefix(parent).trim
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
}

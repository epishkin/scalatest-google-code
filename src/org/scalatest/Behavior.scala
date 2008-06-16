package org.scalatest

import NodeFamily._

trait Behavior {

  private[scalatest] val trunk: Trunk = new Trunk
  private var currentBranch: Branch = trunk

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

  class ItWord {
    def should(exampleName: String) = new Inifier(exampleName)
    def should(behaveWord: BehaveWord) = new Likifier()
  }
  
  protected class BehaveWord {}
  protected val behave = new BehaveWord
  class Likifier {
    def like(sharedBehavior: Behavior) {

      def transformSharedExamplesFullName(node: Node, newParent: Branch): Node = {

        def transformTheParent(node: Node, newParent: Branch) = {
          node match {
            case Example(oldParent, exampleFullName, exampleRawName, needsShould, exampleShortName, level, f) => 
              Example(newParent, getExampleFullName(exampleRawName, needsShould, newParent), exampleRawName, needsShould, getExampleShortName(exampleRawName, needsShould, newParent), level, f)
            case oldDesc @ Description(oldParent, descriptionName, level) =>
              val newDesc = Description(newParent, descriptionName, level)
              newDesc.subNodes = oldDesc.subNodes
              newDesc
            case _ => node
          }
        }
    
        node match {
          case Example(oldParent, exampleFullName, exampleRawName, needsShould, exampleShortName, level, f) => 
            Example(oldParent, getExampleFullName(exampleRawName, needsShould, currentBranch), exampleRawName, needsShould, getExampleShortName(exampleRawName, needsShould, currentBranch), level, f)
          case oldDesc @ Description(oldParent, descriptionName, level) =>
            val newDesc = Description(newParent, descriptionName, level)
            newDesc.subNodes = oldDesc.subNodes.map(transformTheParent(_, newDesc))
            println("**&*&*&*&*&*&*&*&*& oldDesc.subNodes: " + oldDesc.subNodes)
            newDesc
          case _ => node
        }
      }
      
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


package org.scalatest

import NodeFamily._

trait Behavior {

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
}


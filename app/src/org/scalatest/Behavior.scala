package org.scalatest

import NodeFamily._

trait Behavior extends Assertions {

  // All shared examples, in reverse order of registration
  private var sharedExamplesList = List[SharedExample]()
    
  private[scalatest] def examples(newParent: Branch): List[Example] = {
    
    def transform(sharedExample: SharedExample): Example = {
      val exampleFullName = getExampleFullName(sharedExample.exampleRawName, newParent)
      val exampleShortName = getExampleShortName(sharedExample.exampleRawName, newParent)
      Example(newParent, exampleFullName, sharedExample.exampleRawName, exampleShortName, -1, sharedExample.f)
    }
    sharedExamplesList.map(transform)
  }

  class ShouldWord {
    def behave(likeWord: LikeWord) = new Likifier()
  }
  
  class LikeWord {}

  private def registerExample(exampleRawName: String, f: => Unit) {
    sharedExamplesList ::= SharedExample(exampleRawName, f _)
  }
  
  def it(exampleRawName: String)(f: => Unit) {
    registerExample(exampleRawName, f)
  }

  protected val like = new LikeWord
  class Likifier {
    def a(sharedBehavior: Behavior) {
      sharedExamplesList :::= sharedBehavior.sharedExamplesList
    }
  }
  
  protected val should = new ShouldWord
}

trait BehaviorDasher { this: Behavior =>

  class Dasher(s: String) {
    def - (f: => Unit) {
      it(s)(f)
    }
  }
  
  implicit def stringToDasher(s: String) = new Dasher(s)
}

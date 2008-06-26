package org.scalatest

import NodeFamily._

trait Behavior {

  // All shared examples, in reverse order of registration
  private var sharedExamplesList = List[SharedExample]()
    
  private[scalatest] def examples(newParent: Branch): List[Example] = {
    
    def transform(sharedExample: SharedExample): Example = {
      val exampleFullName = getExampleFullName(sharedExample.exampleRawName, sharedExample.needsShould, newParent)
      val exampleShortName = getExampleShortName(sharedExample.exampleRawName, sharedExample.needsShould, newParent)
      Example(newParent, exampleFullName, sharedExample.exampleRawName, sharedExample.needsShould, exampleShortName, -1, sharedExample.f)
    }
    sharedExamplesList.map(transform)
  }

  class ItWord {
    def should(exampleName: String) = new Inifier(exampleName)
    def should(behaveWord: BehaveWord) = new Likifier()
  }
  
  class BehaveWord {}

  private def registerExample(exampleRawName: String, needsShould: Boolean, f: => Unit) {
    sharedExamplesList ::= SharedExample(exampleRawName, needsShould, f _)
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
      sharedExamplesList :::= sharedBehavior.sharedExamplesList
    }
  }
  
  protected val it = new ItWord

  class Dasher(s: String) {
    def - (f: => Unit) {
      specify(s)(f)
    }
  }
  
  implicit def stringToDasher(s: String) = new Dasher(s)

}


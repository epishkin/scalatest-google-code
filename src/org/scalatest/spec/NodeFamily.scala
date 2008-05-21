package org.scalatest

private[scalatest] object NodeFamily {
  
  abstract class Node(parentOption: Option[Branch])
  abstract class Branch(parentOption: Option[Branch]) extends Node(parentOption) {
    var subNodes: List[Node] = Nil
  }
  case class Trunk extends Branch(None)
  case class Example(parent: Branch, exampleName: String, f: () => Unit) extends Node(Some(parent))
  case class SharedBehaviorNode(parent: Branch, sharedBehavior: SharedBehavior) extends Node(Some(parent))
  case class ExampleGivenReporter(parent: Branch, exampleName: String, f: (Reporter) => Unit) extends Node(Some(parent))
  case class Description(parent: Branch, descriptionName: String) extends Branch(Some(parent))
}

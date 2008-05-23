package org.scalatest

// shouldImplied in Example indicates we need to insert a should in front of the example name when
// showing it to the user
private[scalatest] object NodeFamily {
  
  abstract class Node(parentOption: Option[Branch])
  abstract class Branch(parentOption: Option[Branch]) extends Node(parentOption) {
    var subNodes: List[Node] = Nil
  }
  case class Trunk extends Branch(None)
  case class Example(parent: Branch, exampleName: String, exampleFullName: String, f: () => Unit) extends Node(Some(parent))
  case class SharedBehaviorNode(parent: Branch, sharedBehavior: Spec) extends Node(Some(parent))
  case class ExampleGivenReporter(parent: Branch, exampleName: String, exampleFullName: String, f: (Reporter) => Unit) extends Node(Some(parent))
  case class Description(parent: Branch, descriptionName: String) extends Branch(Some(parent))
}

package org.scalatest

// shouldImplied in Example indicates we need to insert a should in front of the example name when
// showing it to the user
private[scalatest] object NodeFamily {
  
  abstract class Node(parentOption: Option[Branch], val level: Int)
  abstract class Branch(parentOption: Option[Branch], override val level: Int) extends Node(parentOption, level) {
    var subNodes: List[Node] = Nil
  }
  case class Trunk extends Branch(None, -1)
  case class Example(parent: Branch, exampleFullName: String, exampleShortName: String, override val level: Int, f: () => Unit) extends Node(Some(parent), level)
  case class ExampleGivenReporter(parent: Branch, exampleFullName: String, exampleShortName: String, override val level: Int, f: (Reporter) => Unit) extends Node(Some(parent), level)
  case class SharedBehaviorNode(parent: Branch, sharedBehavior: Spec, override val level: Int) extends Node(Some(parent), level)
  case class Description(parent: Branch, descriptionName: String, override val level: Int) extends Branch(Some(parent), level)
}

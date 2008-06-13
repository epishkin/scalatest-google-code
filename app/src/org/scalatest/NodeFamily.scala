package org.scalatest

// shouldImplied in Example indicates we need to insert a should in front of the example name when
// showing it to the user
private[scalatest] object NodeFamily {
  
  abstract class Node(parentOption: Option[Branch], val level: Int)
  abstract class Branch(parentOption: Option[Branch], override val level: Int) extends Node(parentOption, level) {
    var subNodes: List[Node] = Nil
  }
  case class Trunk extends Branch(None, -1)
  case class Example(parent: Branch, exampleFullName: String, exampleRawName: String, needsShould: Boolean, specText: String, override val level: Int, f: () => Unit) extends Node(Some(parent), level)
  case class SharedBehaviorNode(parent: Branch, sharedBehavior: Behavior, override val level: Int) extends Node(Some(parent), level)
  case class Description(parent: Branch, descriptionName: String, override val level: Int) extends Branch(Some(parent), level)
}
/*
 * The exampleRawName and needsShould is now stored in Example because when I import a
 * shared example, I recalculate the exampleFullName and specText. This is needed because
 * when a shared behavior is instantiated, it doesn't know what describe clauses it might
 * be nested in yet. When it is passed to like, though, at the point it is known, so the
 * Example is transformed into one that has recalculated these strings.
 */
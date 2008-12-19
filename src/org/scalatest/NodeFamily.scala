package org.scalatest

private[scalatest] object NodeFamily {
  
  case class SharedExample(exampleRawName: String, f: () => Unit)
  
  sealed abstract class Node(parentOption: Option[Branch], val level: Int)

  abstract class Branch(parentOption: Option[Branch], override val level: Int) extends Node(parentOption, level) {
    var subNodes: List[Node] = Nil
  }

  case class Trunk extends Branch(None, -1)

  case class Example(
    parent: Branch,
    exampleFullName: String,
    exampleRawName: String,
    specText: String,
    override val level: Int,
    f: () => Unit
  ) extends Node(Some(parent), level)

  case class Description(
    parent: Branch,
    descriptionName: String,
    override val level: Int
  ) extends Branch(Some(parent), level)

  protected[scalatest] def getPrefix(branch: Branch): String = {
    branch match {
       case Trunk() => ""
      // Call to getPrefix is not tail recursive, but I don't expect the describe nesting to be very deep
      case Description(parent, descriptionName, level) => Resources("prefixSuffix", getPrefix(parent), descriptionName)
    }
  }
  
  private[scalatest] def getExampleFullName(exampleRawName: String, parent: Branch): String = {
    val prefix = getPrefix(parent).trim
    if (prefix.isEmpty) {
      // class MySpec extends Spec {
      //   it("should pop when asked") {}
      // }
      // Should yield: "should pop when asked"
      exampleRawName
    }
    else {
      // class MySpec extends Spec {
      //   describe("A Stack") {
      //     it("must pop when asked") {}
      //   }
      // }
      // Should yield: "A Stack must pop when asked"
      Resources("prefixSuffix", prefix, exampleRawName)
    }
  }
}

/*
 * The exampleRawName is now stored in Example because when I import a
 * shared example, I recalculate the exampleFullName and specText. This is needed because
 * when a shared behavior is instantiated, it doesn't know what describe clauses it might
 * be nested in yet. When it is passed to like, though, at the point it is known, so the
 * Example is transformed into one that has recalculated these strings.
 */

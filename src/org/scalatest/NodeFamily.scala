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

  protected[scalatest] def getPrefix(branch: Branch): String = {
    branch match {
       case Trunk() => ""
      // Call to getPrefix is not tail recursive, but I don't expect the describe nesting to be very deep
      case Description(parent, descriptionName, level) => Resources("prefixSuffix", getPrefix(parent), descriptionName)
    }
  }
  
  private[scalatest] def getExampleFullName(exampleRawName: String, needsShould: Boolean, parent: Branch): String = {
    val prefix = getPrefix(parent).trim
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
    val prefix = getPrefix(parent).trim
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
  
  private[scalatest] def transformSharedExamplesFullName(node: Node, newParent: Branch): Node = {

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
          Example(oldParent, getExampleFullName(exampleRawName, needsShould, /*currentBranch?*/newParent), exampleRawName, needsShould, getExampleShortName(exampleRawName, needsShould, /*currentBranch?*/newParent), level, f)
        case oldDesc @ Description(oldParent, descriptionName, level) =>
          val newDesc = Description(newParent, descriptionName, level)
          newDesc.subNodes = oldDesc.subNodes.map(transformTheParent(_, newDesc))
          println("**&*&*&*&*&*&*&*&*& oldDesc.subNodes: " + oldDesc.subNodes)
          newDesc
        case _ => node
      }
    }
}

/*
 * The exampleRawName and needsShould is now stored in Example because when I import a
 * shared example, I recalculate the exampleFullName and specText. This is needed because
 * when a shared behavior is instantiated, it doesn't know what describe clauses it might
 * be nested in yet. When it is passed to like, though, at the point it is known, so the
 * Example is transformed into one that has recalculated these strings.
 */

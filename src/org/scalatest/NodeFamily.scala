/*
 * Copyright 2001-2008 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest

private[scalatest] object NodeFamily {
  
  case class SharedExample(specText: String, f: () => Unit)
  
  sealed abstract class Node(parentOption: Option[Branch], val level: Int)

  abstract class Branch(parentOption: Option[Branch], override val level: Int) extends Node(parentOption, level) {
    var subNodes: List[Node] = Nil
  }

  case class Trunk extends Branch(None, -1)

  case class Example(
    parent: Branch,
    testName: String,
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
  
  private[scalatest] def getTestName(specText: String, parent: Branch): String = {
    val prefix = getPrefix(parent).trim
    if (prefix.isEmpty) {
      // class MySpec extends Spec {
      //   it("should pop when asked") {}
      // }
      // Should yield: "should pop when asked"
      specText
    }
    else {
      // class MySpec extends Spec {
      //   describe("A Stack") {
      //     it("must pop when asked") {}
      //   }
      // }
      // Should yield: "A Stack must pop when asked"
      Resources("prefixSuffix", prefix, specText)
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

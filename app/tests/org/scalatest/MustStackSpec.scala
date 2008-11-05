package org.scalatest

import scala.collection.mutable.ListBuffer

trait MustStackBehaviors extends MustMatchers {

  val full = 'full

  def nonEmptyStack(lastItemAdded: Int)(stack: Stack[Int]): Behavior = new Behavior {

    "must be non-empty" - {
      stack mustNot beEmpty
    }  

    "must return the top item on peek" - {
      stack.peek mustEqual lastItemAdded
    }
  
    "must not remove the top item on peek" - {
      val size = stack.size
      stack.peek mustEqual lastItemAdded
      stack.size mustEqual size
    }
  
    "must remove the top item on pop" - {
      val size = stack.size
      stack.pop mustEqual lastItemAdded
      stack.size mustEqual (size - 1)
    }
  }
  
  def nonFullStack(stack: Stack[Int]): Behavior = new Behavior {
      
    "must not be full" - {
      stack mustNot be (full)
    }

    "must add to the top on push" - {
      val size = stack.size
      stack.push(7)
      stack.size mustEqual (size + 1)
      stack.peek mustEqual 7
    }
  }
}

class MustStackSpec extends MustSpec with StackFixtureCreationMethods with MustStackBehaviors {

  "A Stack" -- {

    "(when empty)" -- {
      
      "must be empty" - {
        emptyStack must beEmpty
      }

      "must complain on peek" - {
        emptyStack.peek mustThrow classOf[IllegalStateException]
      }

      "must complain on pop" - {
        emptyStack.pop mustThrow classOf[IllegalStateException]
      }
    }

    "(with one item)" -- {
      stackWithOneItem must behave like nonEmptyStack(lastValuePushed)
      stackWithOneItem must behave like nonFullStack
    }
    
    "(with one item less than capacity)"-- {
      stackWithOneItemLessThanCapacity must behave like nonEmptyStack(lastValuePushed)
      stackWithOneItemLessThanCapacity must behave like nonFullStack
    }

    "(full)" -- {
      
      // fullStack must be full  .... could I get this to print the message "- must be full" ?
      "must be full" - {
        fullStack must be (full)
      }
      
      fullStack must behave like nonEmptyStack(lastValuePushed)

      "must complain on a push" - {
        fullStack.push(10) mustThrow classOf[IllegalStateException]
      }
    }
  }
}
 

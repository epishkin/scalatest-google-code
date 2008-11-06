package org.scalatest

import scala.collection.mutable.ListBuffer

trait ShouldStackBehaviors extends ShouldMatchers {

  val full = 'full

  def nonEmptyStack(lastItemAdded: Int)(stack: Stack[Int]): Behavior = new Behavior {

    "should be non-empty" - {
      stack shouldNot beEmpty
    }  

    "should return the top item on peek" - {
      stack.peek shouldEqual lastItemAdded
    }
  
    "should not remove the top item on peek" - {
      val size = stack.size
      stack.peek shouldEqual lastItemAdded
      stack.size shouldEqual size
    }
  
    "should remove the top item on pop" - {
      val size = stack.size
      stack.pop shouldEqual lastItemAdded
      stack.size shouldEqual size - 1
    }
  }
  
  def nonFullStack(stack: Stack[Int]): Behavior = new Behavior {
      
    "should not be full" - {
      stack shouldNot be (full)
    }

    "should add to the top on push" - {
      val size = stack.size
      stack.push(7)
      stack.size shouldEqual size + 1
      stack.peek shouldEqual 7
    }
  }
}

class ShouldStackSpec extends ShouldSpec with StackFixtureCreationMethods with ShouldStackBehaviors {

  "A Stack" -- {

    "(when empty)" -- {
      
      "should be empty" - {
        emptyStack should beEmpty
      }

      "should complain on peek" - {
        emptyStack.peek shouldThrow classOf[IllegalStateException]
      }

      "should complain on pop" - {
        emptyStack.pop shouldThrow classOf[IllegalStateException]
      }
    }

    "(with one item)" -- {
      stackWithOneItem should behave like nonEmptyStack(lastValuePushed)
      stackWithOneItem should behave like nonFullStack
    }
    
    "(with one item less than capacity)"-- {
      stackWithOneItemLessThanCapacity should behave like nonEmptyStack(lastValuePushed)
      stackWithOneItemLessThanCapacity should behave like nonFullStack
    }

    "(full)" -- {
      
      // fullStack should be full  .... could I get this to print the message "- should be full" ?
      "should be full" - {
        fullStack should be (full)
      }
      
      fullStack should behave like nonEmptyStack(lastValuePushed)

      "should complain on a push" - {
        fullStack.push(10) shouldThrow classOf[IllegalStateException]
      }
    }
  }
}
 

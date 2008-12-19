package org.scalatest

import scala.collection.mutable.ListBuffer

trait ShouldStackBehaviors extends ShouldMatchers {

  val full = 'full

  def nonEmptyStack(lastItemAdded: Int)(stack: Stack[Int]): Examples = new Examples with ExamplesDasher {

    "should be non-empty" - {
      stack shouldNot be ('empty)
    }  

    "should return the top item on peek" - {
      stack.peek should equal (lastItemAdded)
    }
  
    "should not remove the top item on peek" - {
      val size = stack.size
      stack.peek should equal (lastItemAdded)
      stack.size should equal (size)
    }
  
    "should remove the top item on pop" - {
      val size = stack.size
      stack.pop should equal (lastItemAdded)
      stack.size should equal (size - 1)
    }
  }
  
  def nonFullStack(stack: Stack[Int]): Examples = new Examples with ExamplesDasher {
      
    "should not be full" - {
      stack shouldNot be (full)
    }

    "should add to the top on push" - {
      val size = stack.size
      stack.push(7)
      stack.size should equal (size + 1)
      stack.peek should equal (7)
    }
  }
}

class ShouldStackSpec extends Spec with SpecDasher with ShouldMatchers with StackFixtureCreationMethods with ShouldStackBehaviors {

  "A Stack" -- {

    "(when empty)" -- {
      
      "should be empty" - {
        emptyStack should be ('empty)
      }

      "should complain on peek" - {
        intercept[IllegalStateException] {
          emptyStack.peek
        }
      }

      "should complain on pop" - {
        intercept[IllegalStateException] {
          emptyStack.pop
        }
      }
    }

    "(with one item)" -- {
      stackWithOneItem should behave like (nonEmptyStack(lastValuePushed))
      stackWithOneItem should behave like (nonFullStack)
    }
    
    "(with one item less than capacity)"-- {
      stackWithOneItemLessThanCapacity should behave like (nonEmptyStack(lastValuePushed))
      stackWithOneItemLessThanCapacity should behave like (nonFullStack)
    }

    "(full)" -- {
      
      // fullStack should be full  .... could I get this to print the message "- should be full" ?
      "should be full" - {
        fullStack should be (full)
      }
      
      fullStack should behave like nonEmptyStack(lastValuePushed)

      "should complain on a push" - {
        intercept[IllegalStateException] {
          fullStack.push(10)
        }
      }
    }
  }
}
 

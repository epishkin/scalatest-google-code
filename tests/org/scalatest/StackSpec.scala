package org.scalatest

trait StackBehaviors { this: Spec with SpecDasher =>

  def nonEmptyStack(lastItemAdded: Int)(stack: Stack[Int]) {

    "should be non-empty" - {
      assert(!stack.empty)
    }  

    "should return the top item on peek" - {
      assert(stack.peek === lastItemAdded)
    }
  
    "should not remove the top item on peek" - {
      val size = stack.size
      assert(stack.peek === lastItemAdded)
      assert(stack.size === size)
    }
  
    "should remove the top item on pop" - {
      val size = stack.size
      assert(stack.pop === lastItemAdded)
      assert(stack.size === size - 1)
    }
  }
  
  def nonFullStack(stack: Stack[Int]) {
      
    "should not be full" - {
      assert(!stack.full)
    }
      
    "should add to the top on push" - {
      val size = stack.size
      stack.push(7)
      assert(stack.size === size + 1)
      assert(stack.peek === 7)
    }
  }
}

class StackSpec extends Spec with SpecDasher with StackFixtureCreationMethods with ShouldMatchers with StackBehaviors {

  "A Stack" -- {

    "(when empty)" -- {
      
      "should be empty" - {
        assert(emptyStack.empty)
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
      nonEmptyStack(lastValuePushed)(stackWithOneItem)
      nonFullStack(stackWithOneItem)
    }
    
    "(with one item less than capacity)"-- {
      nonEmptyStack(lastValuePushed)(stackWithOneItemLessThanCapacity)
      nonFullStack(stackWithOneItemLessThanCapacity)
    }

    "(full)" -- {
      
      "should be full" - {
        assert(fullStack.full)
      }

      nonEmptyStack(lastValuePushed)(fullStack)

      "should complain on a push" - {
        intercept[IllegalStateException] {
          fullStack.push(10)
        }
      }
    }
  }
}
 

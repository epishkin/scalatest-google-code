package org.scalatest

import scala.collection.mutable.ListBuffer

trait ShouldStackBehaviors extends ShouldMatchers {

  val full = 'full
  val empty = 'empty

  def nonEmptyStack(lastItemAdded: Int)(stack: Stack[Int]): Examples = new Examples {

    it("should be non-empty") {
      stack shouldNot be (empty)
    }  

    it("should return the top item on peek") {
      stack.peek should equal (lastItemAdded)
    }
  
    it("should not remove the top item on peek") {
      val size = stack.size
      stack.peek should equal (lastItemAdded)
      stack.size should equal (size)
    }
  
    it("should remove the top item on pop") {
      val size = stack.size
      stack.pop should equal (lastItemAdded)
      stack.size should equal (size - 1)
    }
  }
  
  def nonFullStack(stack: Stack[Int]): Examples = new Examples with ExamplesDasher {
      
    it("should not be full") {
      stack shouldNot be (full)
    }

    it("should add to the top on push") {
      val size = stack.size
      stack.push(7)
      stack.size should equal (size + 1)
      stack.peek should equal (7)
    }
  }
}

class ShouldStackSpec extends Spec with ShouldMatchers with StackFixtureCreationMethods with ShouldStackBehaviors {

  describe("A Stack") {

    describe("(when empty)") {
      
      it("should be empty") {
        emptyStack should be (empty)
      }

      it("should complain on peek") {
        intercept[IllegalStateException] {
          emptyStack.peek
        }
      }

      it("should complain on pop") {
        intercept[IllegalStateException] {
          emptyStack.pop
        }
      }
    }

    describe("(with one item)") {
      stackWithOneItem should behave like (nonEmptyStack(lastValuePushed))
      stackWithOneItem should behave like (nonFullStack)
    }
    
    describe("(with one item less than capacity)") {
      stackWithOneItemLessThanCapacity should behave like (nonEmptyStack(lastValuePushed))
      stackWithOneItemLessThanCapacity should behave like (nonFullStack)
    }

    describe("(full)") {
      
      // fullStack should be full  .... could I get this to print the message "- should be full" ?
      it("should be full") {
        fullStack should be (full)
      }
      
      fullStack should behave like nonEmptyStack(lastValuePushed)

      it("should complain on a push") {
        intercept[IllegalStateException] {
          fullStack.push(10)
        }
      }
    }
  }
}
 

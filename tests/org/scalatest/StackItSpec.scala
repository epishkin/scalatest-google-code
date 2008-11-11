package org.scalatest

import scala.collection.mutable.ListBuffer

trait StackItBehaviors {

  def nonEmptyStack(lastItemAdded: Int)(stack: Stack[Int]): Behavior = new Behavior {

    it("should be non-empty") {
      assert(!stack.empty)
    }  

    it("should return the top item on peek") {
      assert(stack.peek === lastItemAdded)
    }
  
    it("should not remove the top item on peek") {
      val size = stack.size
      assert(stack.peek === lastItemAdded)
      assert(stack.size === size)
    }
  
    it("should remove the top item on pop") {
      val size = stack.size
      assert(stack.pop === lastItemAdded)
      assert(stack.size === size - 1)
    }
  }
  
  def nonFullStack(stack: Stack[Int]): Behavior = new Behavior {
      
    it("should not be full") {
      assert(!stack.full)
    }
      
    it("should add to the top on push") {
      val size = stack.size
      stack.push(7)
      assert(stack.size === size + 1)
      assert(stack.peek === 7)
    }
  }
}
class StackItSpec extends Spec with StackFixtureCreationMethods with StackItBehaviors {

  describe("A Stack") {

    describe("(when empty)") {
      
      it("should be empty") {
        assert(emptyStack.empty)
      }

      it("should complain on peek") {
        intercept(classOf[IllegalStateException]) {
          emptyStack.peek
        }
      }

      it("should complain on pop") {
        intercept(classOf[IllegalStateException]) {
          emptyStack.pop
        }
      }
    }

    describe("(with one item)") {
      assertBehavesLike(stackWithOneItem, nonEmptyStack(lastValuePushed))
      assertBehavesLike(stackWithOneItem, nonFullStack)
    }
    
    describe("(with one item less than capacity)") {
      assertBehavesLike(stackWithOneItemLessThanCapacity, nonEmptyStack(lastValuePushed))
      assertBehavesLike(stackWithOneItemLessThanCapacity, nonFullStack)
    }

    describe("(full)") {
      
      it("should be full") {
        assert(fullStack.full)
      }

      assertBehavesLike(fullStack, nonEmptyStack(lastValuePushed))

      it("should complain on a push") {
        intercept(classOf[IllegalStateException]) {
          fullStack.push(10)
        }
      }
    }
  }
}
 

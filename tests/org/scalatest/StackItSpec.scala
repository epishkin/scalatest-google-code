package org.scalatest

import scala.collection.mutable.ListBuffer

class StackSpec extends Spec with StackFixtureCreationMethods with StackBehaviors {

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
 

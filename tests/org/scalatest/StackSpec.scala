package org.scalatest

import scala.collection.mutable.ListBuffer

class StackSpec extends Spec {

  class Stack[T] {
    val MAX = 10
    private var buf = new ListBuffer[T]
    def push(o: T) {
      if (!full)
        o +: buf
      else
        throw new IllegalStateException("can't push onto a full stack")
    }
    def pop(): T = {
      if (!empty)
        buf.remove(0)
      else
        throw new IllegalStateException("can't pop an empty stack")
    }
    def peek: T = {
      if (!empty)
        buf(0)
      else
        throw new IllegalStateException("can't pop an empty stack")
    }
    def full: Boolean = buf.size == MAX
    def empty: Boolean = buf.size == 0
    def size = buf.size
  }
      
  // Fixture creation methods
  def emptyStack = new Stack[Int]
  def fullStack = {
    val stack = new Stack[Int]
    for (i <- 0 until stack.MAX)
      stack.push(i)
    stack
  }
  def stackWithOneItem = {
    val stack = new Stack[Int]
    stack.push(9)
    stack
  }
  def stackWithOneItemLessThanCapacity = {
    val stack = new Stack[Int]
    for (i <- 1 to 9)
      stack.push(i)
    stack
  }
  val lastValuePushed = 9
      
  case class NonEmptyStack(stack: Stack[Int], lastItemAdded: Int) extends Behavior {
    
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
      
  case class NonFullStack(stack: Stack[Int]) extends Behavior {
    
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
      
  "A Stack" -- {

    "(when empty)" -- {
      
      "should be empty" - {
        assert(emptyStack.empty)
      }
      
      "should complain on peek" - {
        intercept(classOf[IllegalStateException]) {
          emptyStack.peek
        }
      }
      
      "should complain on pop" - {
        intercept(classOf[IllegalStateException]) {
          emptyStack.pop
        }
      }
    }
    
    "(with one item)" -- {
      it should behave like NonEmptyStack(stackWithOneItem, lastValuePushed)
      it should behave like NonFullStack(stackWithOneItem)
    }
    
    "(with one item less than capacity)"-- {
      it should behave like NonEmptyStack(stackWithOneItemLessThanCapacity, lastValuePushed)
      it should behave like NonFullStack(stackWithOneItemLessThanCapacity)
    }

    "(full)" -- {
      
      "should be full" - {
        assert(fullStack.full)
      }
      
      it should behave like NonEmptyStack(fullStack, lastValuePushed)
      
      "should complain on a push" - {
        intercept(classOf[IllegalStateException]) {
          fullStack.push(10)
        }
      }
    }
  }
}
 

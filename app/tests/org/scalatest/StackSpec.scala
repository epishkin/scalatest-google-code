package org.scalatest

import scala.collection.mutable.ListBuffer

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


trait StackBehaviors {

  def nonEmptyStack(lastItemAdded: Int)(stack: Stack[Int]): Examples = new Examples with BehaviorDasher {

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
  
  def nonFullStack(stack: Stack[Int]): Examples = new Examples with BehaviorDasher {
      
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

trait StackFixtureCreationMethods {

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
      assertBehavesLike(stackWithOneItem, nonEmptyStack(lastValuePushed))
      assertBehavesLike(stackWithOneItem, nonFullStack)
    }
    
    "(with one item less than capacity)"-- {
      assertBehavesLike(stackWithOneItemLessThanCapacity, nonEmptyStack(lastValuePushed))
      assertBehavesLike(stackWithOneItemLessThanCapacity, nonFullStack)
    }

    "(full)" -- {
      
      "should be full" - {
        assert(fullStack.full)
      }

      assertBehavesLike(fullStack, nonEmptyStack(lastValuePushed))

      "should complain on a push" - {
        intercept[IllegalStateException] {
          fullStack.push(10)
        }
      }
    }
  }
}
 

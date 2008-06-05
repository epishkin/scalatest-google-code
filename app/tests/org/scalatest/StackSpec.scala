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
    
    it should "be non-empty" in {
      assert(!stack.empty)
    }  

    it should "return the top item on peek" in {
      assert(stack.peek === lastItemAdded)
    }

    it should "not remove the top item on peek" in {
      val size = stack.size
      assert(stack.peek === lastItemAdded)
      assert(stack.size === size)
    }

    it should "remove the top item on pop" in {
      val size = stack.size
      assert(stack.pop === lastItemAdded)
      assert(stack.size === size - 1)
    }
  }
      
  case class NonFullStack(stack: Stack[Int]) extends Behavior {
    
    it should "not be full" in {
      assert(!stack.full)
    }
    
    it should "add to the top on push" in {
      val size = stack.size
      stack.push(7)
      assert(stack.size === size + 1)
      assert(stack.peek === 7)
    }
  }
      
  describe("A Stack") {
    
    describe("(when empty)") {
      
      it should "be empty" in {
        assert(emptyStack.empty)
      }
      
      it should "complain on peek" in {
        intercept(classOf[IllegalStateException]) {
          emptyStack.peek
        }
      }
      
      it should "complain on pop" in {
        intercept(classOf[IllegalStateException]) {
          emptyStack.pop
        }
      }
    }
    
    describe("(with one item)") {
      it should behave like { NonEmptyStack(stackWithOneItem, lastValuePushed) }
      it should behave like { NonFullStack(stackWithOneItem) }
    }
    
    describe("(with one item less than capacity)") {
      it should behave like { NonEmptyStack(stackWithOneItemLessThanCapacity, lastValuePushed) }
      it should behave like { NonFullStack(stackWithOneItemLessThanCapacity) }
    }
    
    describe("(full)") {
      
      it should "be full" in {
        assert(fullStack.full)
      }
      
      it should behave like { NonEmptyStack(fullStack, lastValuePushed) }
      
      it should "complain on a push" in {
        intercept(classOf[IllegalStateException]) {
          fullStack.push(10)
        }
      }
    }
  }
}
 

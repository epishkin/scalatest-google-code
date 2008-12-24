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


package org.scalatest

import NodeFamily._

/**
 * A trait that specifies and tests behavior that can be shared by different subjects.
 *
 * <p>
 * In general, you'll need to pass in any fixture objects needed by the examples. One
 * way to do this is to define a method that takes the needed fixture objects, and returns
 * an <code>Examples</code> that uses them. For instance:
 * </p>
 *
 * <pre>
 * def nonFullStack(stack: Stack[Int]) = new Examples {
 *
 *   it("should not be full") {
 *     assert(!stack.full)
 *   }
 *
 *   it("should add to the top on push") {
 *     val size = stack.size
 *     stack.push(7)
 *     assert(stack.size === size + 1)
 *     assert(stack.peek === 7)
 *   }
 * }
 * </pre>
 *
 * <p>
 * An <code>Examples</code> is only a container for examples, and does not inherit from <code>Suite</code>.
 * As a result, you can't mix in <code>BeforeAndAfter</code> into an <code>Examples</code>. To do something
 * before and/or after each example, you'll need to use one of the more functional approaches described
 * in the documentation for <code>Spec</code>, such as <code>createFixture</code> or <code>withFixture</code>
 * methods.
 * </p>
 *
 * <p>
 * To get a better idea of how to use <code>Examples</code>, here's a more complete, well, example:
 * </p>
 *
 * <pre>
 * import org.scalatest.Examples
 * import org.scalatest.Spec
 * import scala.collection.mutable.ListBuffer
 *
 * class Stack[T] {
 *   val MAX = 10
 *   private var buf = new ListBuffer[T]
 *   def push(o: T) {
 *     if (!full)
 *       o +: buf
 *     else
 *       throw new IllegalStateException("can't push onto a full stack")
 *   }
 *   def pop(): T = {
 *     if (!empty)
 *       buf.remove(0)
 *     else
 *       throw new IllegalStateException("can't pop an empty stack")
 *   }
 *   def peek: T = {
 *     if (!empty)
 *       buf(0)
 *     else
 *       throw new IllegalStateException("can't pop an empty stack")
 *   }
 *   def full: Boolean = buf.size == MAX
 *   def empty: Boolean = buf.size == 0
 *   def size = buf.size
 * }
 *
 * trait SharedExamples {
 *
 *   def nonEmptyStack(lastItemAdded: Int)(stack: Stack[Int]) = new Examples {
 *
 *     it("should be non-empty") {
 *       assert(!stack.empty)
 *     }
 *
 *     it("should return the top item on peek") {
 *       assert(stack.peek === lastItemAdded)
 *     }
 *
 *     it("should not remove the top item on peek") {
 *       val size = stack.size
 *       assert(stack.peek === lastItemAdded)
 *       assert(stack.size === size)
 *     }
 *
 *     it("should remove the top item on pop") {
 *       val size = stack.size
 *       assert(stack.pop === lastItemAdded)
 *       assert(stack.size === size - 1)
 *     }
 *   }
 *
 *   def nonFullStack(stack: Stack[Int]) = new Examples {
 *
 *     it("should not be full") {
 *       assert(!stack.full)
 *     }
 *
 *     it("should add to the top on push") {
 *       val size = stack.size
 *       stack.push(7)
 *       assert(stack.size === size + 1)
 *       assert(stack.peek === 7)
 *     }
 *   }
 * }
 *
 * class StackItSpec extends Spec with SharedExamples {
 *
 *   // Fixtures
 *   def emptyStack = new Stack[Int]
 *   def fullStack = {
 *     val stack = new Stack[Int]
 *     for (i <- 0 until stack.MAX)
 *       stack.push(i)
 *     stack
 *   }
 *   def stackWithOneItem = {
 *     val stack = new Stack[Int]
 *     stack.push(9)
 *     stack
 *   }
 *   def stackWithOneItemLessThanCapacity = {
 *     val stack = new Stack[Int]
 *     for (i <- 1 to 9)
 *       stack.push(i)
 *     stack
 *   }
 *   val lastValuePushed = 9
 *
 *   describe("A Stack") {
 *
 *     describe("(when empty)") {
 *
 *       it("should be empty") {
 *         assert(emptyStack.empty)
 *       }
 *
 *       it("should complain on peek") {
 *         intercept[IllegalStateException] {
 *           emptyStack.peek
 *         }
 *       }
 *
 *       it("should complain on pop") {
 *         intercept[IllegalStateException] {
 *           emptyStack.pop
 *         }
 *       }
 *     }
 *
 *     describe("(with one item)") {
 *       includeExamples(nonEmptyStack(lastValuePushed)(stackWithOneItem))
 *       includeExamples(nonFullStack(stackWithOneItem))
 *     }
 *
 *     describe("(with one item less than capacity)") {
 *       includeExamples(nonEmptyStack(lastValuePushed)(stackWithOneItemLessThanCapacity))
 *       includeExamples(nonFullStack(stackWithOneItemLessThanCapacity))
 *     }
 *
 *     describe("(full)") {
 *
 *       it("should be full") {
 *         assert(fullStack.full)
 *       }
 *
 *       includeExamples(nonEmptyStack(lastValuePushed)(fullStack))
 *
 *       it("should complain on a push") {
 *         intercept[IllegalStateException] {
 *           fullStack.push(10)
 *         }
 *       }
 *     }
 *   }
 * }
 * </pre>
 * 
 * <p>
 * Were you to run this in the Scala interpreter, here's what you'd see:
 * </p>
 *
 * <pre>
 * scala> (new StackItSpec).execute()
 * A Stack (when empty)
 * - should be empty
 * - should complain on peek
 * - should complain on pop
 * A Stack (with one item)
 * - should be non-empty
 * - should return the top item on peek
 * - should not remove the top item on peek
 * - should remove the top item on pop
 * - should not be full
 * - should add to the top on push
 * A Stack (with one item less than capacity)
 * - should be non-empty
 * - should return the top item on peek
 * - should not remove the top item on peek
 * - should remove the top item on pop
 * - should not be full
 * - should add to the top on push
 * A Stack (full)
 * - should be full
 * - should be non-empty
 * - should return the top item on peek
 * - should not remove the top item on peek
 * - should remove the top item on pop
 * - should complain on a push
 * </pre>
 *
 * @author Bill Venners
 */
trait Examples extends Assertions {

  // All shared examples, in reverse order of registration
  private var sharedExamplesList = List[SharedExample]()
    
  private[scalatest] def examples(newParent: Branch): List[Example] = {
    
    def transform(sharedExample: SharedExample): Example = {
      val testName = getTestName(sharedExample.specText, newParent)
      Example(newParent, testName, sharedExample.specText, -1, sharedExample.f)
    }
    sharedExamplesList.map(transform)
  }

  // TODO: Sheesh, I forgot about groups! And ignore!
  /**
   * Register a test with the given spec text, optional groups, and test function value that takes no arguments.
   * An invocation of this method is called an &#8220;example.&#8221;
   *
   * This method will register the example for later importing into a <code>Spec</code>. The passed
   * spec text must not have been registered previously on this <code>Examples</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testFun the test function
   * @throws IllegalArgumentException if an example with the same spec text has been registered previously
   * @throws NullPointerException if <code>specText</code>is <code>null</code>
   */
  def it(specText: String)(testFun: => Unit) {
    if (specText == null)
      throw new NullPointerException("specText was null")
    if (sharedExamplesList.exists(_.specText == specText)) {
      val duplicateName = sharedExamplesList.find(_.specText == specText).getOrElse("")
      throw new IllegalArgumentException("Duplicate spec text: " + duplicateName)
    }
    sharedExamplesList ::= SharedExample(specText, testFun _)
  }
}

package org.scalatest

/**
 * Trait that enables a more concise expression of <code>Spec</code> describers and examples by providing an
 * implicit conversion that adds &#8220;dash methods&#8221; (<code>--</code> and <code>-</code>) to
 * <code>String</code>. A string followed by a double dash (<code>--</code>) denotes a describer, and
 * results at runtime in an invocation of <code>describe</code>. A string followed by a single
 * dash (<code>-</code>) denotes an example, and results at runtime in an invocation of <code>it</code>.
 * Here's an sample <code>Spec</code> written in &#8220;dashes&#8221; style:
 *
 * <pre>
 * import org.scalatest.Spec
 * import scala.collection.mutable.Stack
 *
 * class StackDashSpec extends Spec with SpecDasher {
 *
 *   "A Stack" -- {
 *
 *     "should pop values in last-in-first-out order" - {
 *       val stack = new Stack[Int]
 *       stack.push(1)
 *       stack.push(2)
 *       assert(stack.pop() === 2)
 *       assert(stack.pop() === 1)
 *     }
 *
 *     "should throw NoSuchElementException if an empty stack is popped" - {
 *       val emptyStack = new Stack[String]
 *       intercept(classOf[NoSuchElementException]) {
 *         emptyStack.pop()
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * Running <code>StackDashSpec</code> from within the Scala interpreter will yield:
 * </p>
 *
 * <pre>
 * scala> (new StackDashSpec).execute()
 * </pre>
 *
 * <p>
 * You would see:
 * </p>
 *
 * <pre>
 * A Stack
 * - should pop values in last-in-first-out order
 * - should throw NoSuchElementException if an empty stack is popped
 * </pre>
 *
 * @author Bill Venners
 */
trait SpecDasher { this: Spec => 

  class Dasher(s: String) {
    def -- (f: => Unit) {
      describe(s)(f)
    }
    def - (f: => Unit) {
      it(s)(f)
    }
  }
  
  implicit def stringToDasher(s: String) = new Dasher(s)
}

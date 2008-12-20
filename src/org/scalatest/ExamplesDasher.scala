package org.scalatest

import NodeFamily._

/**
 * Trait that enables a more concise expression of <code>Examples</code> examples by providing an
 * implicit conversion that adds a &#8220;dash method&#8221; (<code>-</code>) to
 * <code>String</code>. A string followed by a single
 * dash (<code>-</code>) denotes an example, and results at runtime in an invocation of <code>it</code>.
 * Here's an sample <code>Examples</code> written in &#8220;dashes&#8221; style:
 *
 * <pre>
 * def nonFullStack(stack: Stack[Int]): Examples = new Examples with ExamplesDasher {
 *
 *   "should not be full" - {
 *     assert(!stack.full)
 *   }
 *
 *   "should add to the top on push" - {
 *     val size = stack.size
 *     stack.push(7)
 *     assert(stack.size === size + 1)
 *     assert(stack.peek === 7)
 *   }
 * }
 * </pre>
 *
 * @author Bill Venners
 */
trait ExamplesDasher { this: Examples =>

  class Dasher(s: String) {
    def - (f: => Unit) {
      it(s)(f)
    }
  }
  
  implicit def stringToDasher(s: String) = new Dasher(s)
}

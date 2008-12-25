/*
 * Copyright 2001-2008 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 *       intercept[NoSuchElementException] {
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
 * <p>
 * <em>Note: This trait is deprecated and may be removed in a future release of ScalaTest. I included it in 0.9.4 so I could get
 * informed feedback from users as to whether you think this should be included in the ScalaTest API. If
 * it gets removed, then those who like this style of code can always just do this themselves, so it
 * isn't that risky to use. But bear in mind it may go away.</em>
 * </p>
 *
 * @author Bill Venners
 */
@deprecated trait SpecDasher { this: Spec => 

  /**
   * Class used via an implicit conversion to enable describers and examples in a <code>Spec</code>
   * subclass to be specified by string followed by dashes, such as:
   *
   * <pre>
   * "A Stack (when empty)" -- {
   *   "should not be full" - {
   *     assert(!stack.full)
   *   }
   * }
   * </pre>
   *
   * @param text the describer or specification text
   *
   * @author Bill Venners
   */
  @deprecated class Dasher(text: String) {
    def -- (descFun: => Unit) {
      describe(text)(descFun)
    }
    def - (testFun: => Unit) {
      it(text)(testFun)
    }
  }
  
  implicit def stringToDasher(s: String) = new Dasher(s)
}

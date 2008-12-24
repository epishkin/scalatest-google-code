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
 * def nonFullStack(stack: Stack[Int]) = new Examples with ExamplesDasher {
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
 * <p>
 * The dashed strings will result in invocations of <code>it</code> on <code>this</code> (<code>ExamplesDasher</code>)
 * has a self type of <code>Examples</code>. Thus, the previous code has the same meaning as:
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
 * <em>Note: This trait is deprecated and may be removed in a future release of ScalaTest. I included it in 0.9.4 so I could get
 * informed feedback from users as to whether you think this should be included in the ScalaTest API. If
 * it gets removed, then those who like this style of code can always just do this themselves, so it
 * isn't that risky to use. But bear in mind it may go away.</em>
 * </p>
 *
 * @author Bill Venners
 */
private[scalatest] trait ExamplesDasher { this: Examples =>

  /**
   * Class used via an implicit conversion to enable examples in an <code>Examples</code>
   * subclass to be specified by a string followed by a dash, such as:
   *
   * <pre>
   * "should not be full" - {
   *   assert(!stack.full)
   * }
   * </pre>
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   *
   * @author Bill Venners
   */
  @deprecated class Dasher(specText: String) {

    /**
     * The <code>-</code> method invokes <code>it</code> on <code>this</code> (possible because <code>ExamplesDasher</code>
     * has a self type of <code>Examples</code>), passing in the string <code>specText</code> passed to the <code>Dasher</code>
     * constructor as the first parameter and the function value passed as
     * a by-name parameter to <code>-</code> as the second parameter.
     *
     * @param testFun the test function
     */
    def - (testFun: => Unit) {
      it(specText)(testFun)
    }
  }
  
  /**
   * Implicit conversion from <code>String</code> to <code>Dasher</code>, used to enable
   * the &#8220;dashes&#8221; style of specification. For more information
   * on this mechanism, see the <a href="ExamplesDasher.html">documentation for </code>ExamplesDasher</code></a>.
   *
   * @param specText the string to convert to <code>Dasher</code>.
   * @throws NullPointerException if <code>specText</code> is <code>null</code>.
   */
  implicit def stringToDasher(specText: String) = new Dasher(specText)
}

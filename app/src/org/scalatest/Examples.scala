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
 * def nonFullStack(stack: Stack[Int]): Examples = new Examples {
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

  /**
   * Register a test with the given spec text, optional groups, and test function value that takes no arguments.
   * An invocation of this method is called an &#8220;example.&#8221;
   *
   * This method will register the example for later importing into a <code>Spec</code>. The passed
   * spec text must not have been registered previously on this <code>Examples</code> instance.
   *
   * @throws IllegalArgumentException if an example with the same spec text has been registered previously
   */
  def it(specText: String)(f: => Unit) {
    if (sharedExamplesList.exists(_.specText == specText)) {
      val duplicateName = sharedExamplesList.find(_.specText == specText).getOrElse("")
      throw new IllegalArgumentException("Duplicate spec text: " + duplicateName)
    }
    sharedExamplesList ::= SharedExample(specText, f _)
  }
}

package org.scalatest

import NodeFamily._

/**
 * A trait that specifies and tests behavior that can be shared by different subjects.
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

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
      val exampleFullName = getExampleFullName(sharedExample.exampleRawName, newParent)
      val exampleShortName = getExampleShortName(sharedExample.exampleRawName, newParent)
      Example(newParent, exampleFullName, sharedExample.exampleRawName, exampleShortName, -1, sharedExample.f)
    }
    sharedExamplesList.map(transform)
  }

  // TODO: test that duplicate names are caught. Actually, they may be caught only when sucked into a
  // Spec, but why not catch them here too.
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
    sharedExamplesList ::= SharedExample(specText, f _)
  }
}

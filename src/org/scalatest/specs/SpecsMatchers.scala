package org.scalatest.specs

import org.specs.specification._
import org.specs.matcher._
import org.specs.Example

/**
 * Convenience trait that allows you to use Specs matchers in ScalaTest suites.
 *
 * @author Eric Torreborre
 * @author Bill Venners
 */ 
trait SpecsMatchers extends AssertFactory with Matchers {
 override var example: Option[Example] = None    // unused here
 def forExample(s: String) = new Example(s, null) // unused here
}

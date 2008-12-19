package org.scalatest

import NodeFamily._

trait ExamplesDasher { this: Examples =>

  class Dasher(s: String) {
    def - (f: => Unit) {
      it(s)(f)
    }
  }
  
  implicit def stringToDasher(s: String) = new Dasher(s)
}

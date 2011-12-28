package org.scalatest

trait ValueOnPartialFunction {
  implicit def convertPartialFunctionToValuable[A, B](pf: PartialFunction[A, B]) = new Valuable(pf)
  
  class Valuable[A, B](pf: PartialFunction[A, B]) {
    def valueAt(v1: A): B = {
      if (pf.isDefinedAt(v1)) {
        pf.apply(v1)
      }
      else
        throw new TestFailedException(Resources("partialFunctionValueNotDefined", v1.toString()), 1)
    }
  }
}

object ValueOnPartialFunction extends ValueOnPartialFunction

package org.scalatest

import org.scalatest.StackDepthExceptionHelper.getStackDepthFun

trait ValueOnPartialFunction {
  implicit def convertPartialFunctionToValuable[A, B](pf: PartialFunction[A, B]) = new Valuable(pf)
  
  class Valuable[A, B](pf: PartialFunction[A, B]) {
    def valueAt(v1: A): B = {
      if (pf.isDefinedAt(v1)) {
        pf.apply(v1)
      }
      else
        throw new TestFailedException(sde => Some(Resources("partialFunctionValueNotDefined", v1.toString())), None, getStackDepthFun("ValueOnPartialFunction.scala", "valueAt"))
    }
  }
}

object ValueOnPartialFunction extends ValueOnPartialFunction

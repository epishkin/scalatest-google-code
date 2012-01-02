package org.scalatest

import org.scalatest.StackDepthExceptionHelper.getStackDepthFun

trait ValueOnEither {
  implicit def convertEitherToValuable[L, R](either: Either[L, R]) = new Valuable(either)
  
  class Valuable[L, R](either: Either[L, R]) {
    
    def leftValue: L = {
      try {
        either.left.get
      }
      catch {
        case cause: NoSuchElementException => 
          throw new TestFailedException(sde => Some(Resources("eitherLeftValueNotDefined")), Some(cause), getStackDepthFun("ValueOnEither.scala", "leftValue"))
      }
    }
    
    def rightValue: R = {
      try {
        either.right.get
      }
      catch {
        case cause: NoSuchElementException => 
          throw new TestFailedException(sde => Some(Resources("eitherRightValueNotDefined")), Some(cause), getStackDepthFun("ValueOnEither.scala", "rightValue"))
      }
    }
  }
}

object ValueOnEither extends ValueOnEither

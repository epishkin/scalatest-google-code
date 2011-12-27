package org.scalatest

private[scalatest] trait ValueOnEither {
  implicit def convertEitherToValuable[L, R](either: Either[L, R]) = new Valuable(either)
  
  class Valuable[L, R](either: Either[L, R]) {
    
    def leftValue: L = {
      try {
        either.left.get
      }
      catch {
        case cause: NoSuchElementException => 
          // TODO: Verify and possibly be smarter about stack depth
          throw new TestFailedException(Resources("eitherLeftValueNotDefined"), cause, 1)
      }
    }
    
    def rightValue: R = {
      try {
        either.right.get
      }
      catch {
        case cause: NoSuchElementException => 
          // TODO: Verify and possibly be smarter about stack depth
          throw new TestFailedException(Resources("eitherRightValueNotDefined"), cause, 1)
      }
    }
  }
}

object ValueOnEither extends ValueOnEither
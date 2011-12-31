package org.scalatest

import org.scalatest.ValueMethods._
import org.scalatest.matchers.ShouldMatchers

class ValueMethodsSpec extends FunSpec with ShouldMatchers {
  describe("When ValueMethods._ is imported") {
    
    it("should be able to use syntax 'opt.value should be > 8'") {
      val opt: Option[Int] = Some(10)
      opt.value should be > 8
    }
    
    it("should be able to use syntax 'either.leftValue should be > 8'") {
      val either: Either[Int, Int] = Left(10)
      either.leftValue should be > 8
    }
    
    it("should be able to use syntax 'either.rightValue should be <= 99'") {
      val either: Either[Int, Int] = Right(10)
      either.rightValue should be <= 99
    }
    
    it("""should be able to use syntax 'pf.valueAt("age") should be >= 21'""") {
      val pf = new PartialFunction[String, Int]() {
        def isDefinedAt(x: String): Boolean = x == "age"
        def apply(x: String): Int = 28
      }
      pf.valueAt("age") should be >= 21
    }
  }
}
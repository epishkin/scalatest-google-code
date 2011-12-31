package org.scalatest

import org.scalatest.ValueOnEither._
import org.scalatest.ValueOnOption._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.SharedHelpers.thisLineNumber

class ValueOnEitherSpec extends FunSpec with ShouldMatchers {
  describe("values on Either") {

    it("should return the left value inside an either if leftValue is defined") {
      val e: Either[String, String] = Left("hi there")
      e.leftValue should be === ("hi there")
      e.leftValue should startWith ("hi")
    }

    it("should throw TestFailedException if leftValue is empty") {
      val e: Either[String, String] = Right("hi there")
      val caught = 
        evaluating {
          e.leftValue should startWith ("hi")
        } should produce [TestFailedException]
      caught.failedCodeLineNumber.value should equal (thisLineNumber - 2)
      caught.failedCodeFileName.value should be ("ValueOnEitherSpec.scala")
      caught.message.value should be (Resources("eitherLeftValueNotDefined"))
    }
    
    it("should return the right value inside an either if rightValue is defined") {
      val e: Either[String, String] = Right("hi there")
      e.rightValue should be === ("hi there")
      e.rightValue should startWith ("hi")
    }
    
    it("should throw TestFailedException if rightValue is empty") {
      val e: Either[String, String] = Left("hi there")
      val caught = 
        evaluating {
          e.rightValue should startWith ("hi")
        } should produce [TestFailedException]
      caught.failedCodeLineNumber.value should equal (thisLineNumber - 2)
      caught.failedCodeFileName.value should be ("ValueOnEitherSpec.scala")
      caught.message.value should be (Resources("eitherRightValueNotDefined"))
    }
  } 
}
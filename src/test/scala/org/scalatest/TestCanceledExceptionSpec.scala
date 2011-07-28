/*
 * Copyright 2001-2008 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest

import org.scalatest.matchers.ShouldMatchers

class TestCanceledExceptionSpec extends Spec with ShouldMatchers {

  val baseLineNumber = 22

  describe("The TestCanceledException") {

    it("should give the proper line on cancel()") {
      try {
        cancel()
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 6))
            case None => cancel("cancel() didn't produce a file name and line number string: " + e.failedCodeFileNameAndLineNumberString, e)
          }
        case e =>
          cancel("cancel() didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on cancel(\"message\")") {
      try {
        cancel("some message")
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 21))
            case None => cancel("cancel(\"some message\") didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("cancel(\"some message\") didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on cancel(throwable)") {
      try {
        cancel(new RuntimeException)
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 36))
            case None => cancel("cancel(throwable) didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("cancel(throwable) didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on cancel(\"some message\", throwable)") {
      try {
        cancel("some message", new RuntimeException)
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 51))
            case None => cancel("cancel(\"some message\", throwable) didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("cancel(\"some message\", throwable) didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on assume(false)") {
      try {
        assume(false)
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 66))
            case None => cancel("assume(false) didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("assume(false) didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on assume(false, \"some message\")") {
      try {
        assume(false, "some message")
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 81))
            case None => cancel("assume(false, \"some message\") didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("assume(false, \"some message\") didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on assume(1 === 2)") {
      try {
        assume(1 === 2)
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 96))
            case None => cancel("assume(1 === 2) didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("assume(1 === 2) didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on assume(1 === 2, \"some message\")") {
      try {
        assume(1 === 2, "some message")
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 111))
            case None => cancel("assume(1 === 2, \"some message\") didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("assume(1 === 2, \"some message\") didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on expect(1) { 2 }") {
      try {
        expect(1) { 2 }
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 126))
            case None => cancel("expect(1) { 2 } didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("expect(1) { 2 } didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on expect(1, \"some message\") { 2 }") {
      try {
        expect(1, "some message") { 2 }
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 141))
            case None => cancel("expect(1, \"some message\") { 2 } didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("expect(1, \"some message\") { 2 } didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on intercept[IllegalArgumentException] {}") {
      try {
        intercept[IllegalArgumentException] {}
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 156))
            case None => cancel("intercept[IllegalArgumentException] {} didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("intercept[IllegalArgumentException] {} didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on intercept[IllegalArgumentException] { throw new RuntimeException }") {
      try {
        intercept[IllegalArgumentException] { if (false) 1 else throw new RuntimeException }
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 171))
            case None => cancel("intercept[IllegalArgumentException] { throw new RuntimeException } didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("intercept[IllegalArgumentException] { throw new RuntimeException } didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on 1 should be === 2") {
      try {
        1 should be === 2
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) =>
              if (s != ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 186))) {
                cancel("s was: " + s, e)
              }
            case None => cancel("1 should be === 2 didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("1 should be === 2 didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on evaluating {} should produce [IllegalArgumentException] {}") {
      try {
        evaluating {} should produce [IllegalArgumentException]
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => // s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 204))
            if (s != ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 204))) {
                cancel("s was: " + s, e)
              }
            case None => cancel("evaluating {} should produce [IllegalArgumentException] didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("evaluating {} should produce [IllegalArgumentException] didn't produce a TestCanceledException", e)
      }
    }

    it("should give the proper line on evaluating { throw new RuntimeException } should produce [IllegalArgumentException]") {
      try {
        evaluating { if (false) 1 else throw new RuntimeException } should produce [IllegalArgumentException]
      }
      catch {
        case e: TestCanceledException =>
          e.failedCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestCanceledExceptionSpec.scala:" + (baseLineNumber + 222))
            case None => cancel("evaluating { throw new RuntimeException } should produce [IllegalArgumentException] didn't produce a file name and line number string", e)
          }
        case e =>
          cancel("evaluating { throw new RuntimeException } should produce [IllegalArgumentException] didn't produce a TestCanceledException", e)
      }
    }

    it("should return the cause in both cause and getCause") {
      val theCause = new IllegalArgumentException("howdy")
      val tfe = new TestCanceledException(Some("doody"), Some(theCause), 3)
      assume(tfe.cause.isDefined)
      assume(tfe.cause.get === theCause)
      assume(tfe.getCause == theCause)
    }

    it("should return None in cause and null in getCause if no cause") {
      val tfe = new TestCanceledException(Some("doody"), None, 3)
      assume(tfe.cause.isEmpty)
      assume(tfe.getCause == null)
    }

    it("should be equal to itself") {
      val tfe = new TestCanceledException(Some("doody"), None, 3)
      assume(tfe equals tfe)
    }
  }
}
 

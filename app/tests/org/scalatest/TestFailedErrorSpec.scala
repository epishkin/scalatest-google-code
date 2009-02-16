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

class TestFailedErrorSpec extends Spec with ShouldMatchers {

  val baseLineNumber = 20

  describe("The FailedTestError") {

    it("should give the proper line on fail()") {
      try {
        fail()
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:" + (baseLineNumber + 6))
            case None => fail("fail() didn't produce a file name and line number string", e)
          }
        case e =>
          fail("fail() didn't produce a TestFailedError", e)
      }
    }

    it("should give the proper line on fail(\"message\")") {
      try {
        fail("some message")
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:" + (baseLineNumber + 21))
            case None => fail("fail(\"some message\") didn't produce a file name and line number string", e)
          }
        case e =>
          fail("fail(\"some message\") didn't produce a TestFailedError", e)
      }
    }

    it("should give the proper line on fail(throwable)") {
      try {
        fail(new RuntimeException)
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:" + (baseLineNumber + 36))
            case None => fail("fail(throwable) didn't produce a file name and line number string", e)
          }
        case e =>
          fail("fail(throwable) didn't produce a TestFailedError", e)
      }
    }

    it("should give the proper line on fail(\"some message\", throwable)") {
      try {
        fail("some message", new RuntimeException)
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:" + (baseLineNumber + 51))
            case None => fail("fail(\"some message\", throwable) didn't produce a file name and line number string", e)
          }
        case e =>
          fail("fail(\"some message\", throwable) didn't produce a TestFailedError", e)
      }
    }

    it("should give the proper line on assert(false)") {
      try {
        assert(false)
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:" + (baseLineNumber + 66))
            case None => fail("assert(false) didn't produce a file name and line number string", e)
          }
        case e =>
          fail("assert(false) didn't produce a TestFailedError", e)
      }
    }

    it("should give the proper line on assert(false, \"some message\")") {
      try {
        assert(false, "some message")
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:" + (baseLineNumber + 81))
            case None => fail("assert(false, \"some message\") didn't produce a file name and line number string", e)
          }
        case e =>
          fail("assert(false, \"some message\") didn't produce a TestFailedError", e)
      }
    }

    it("should give the proper line on assert(1 === 2)") {
      try {
        assert(1 === 2)
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:" + (baseLineNumber + 96))
            case None => fail("assert(1 === 2) didn't produce a file name and line number string", e)
          }
        case e =>
          fail("assert(1 === 2) didn't produce a TestFailedError", e)
      }
    }

    it("should give the proper line on assert(1 === 2, \"some message\")") {
      try {
        assert(1 === 2, "some message")
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:" + (baseLineNumber + 111))
            case None => fail("assert(1 === 2, \"some message\") didn't produce a file name and line number string", e)
          }
        case e =>
          fail("assert(1 === 2, \"some message\") didn't produce a TestFailedError", e)
      }
    }

    it("should give the proper line on expect(1) { 2 }") {
      try {
        expect(1) { 2 }
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:" + (baseLineNumber + 126))
            case None => fail("expect(1) { 2 } didn't produce a file name and line number string", e)
          }
        case e =>
          fail("expect(1) { 2 } didn't produce a TestFailedError", e)
      }
    }

    it("should give the proper line on expect(1, \"some message\") { 2 }") {
      try {
        expect(1, "some message") { 2 }
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:" + (baseLineNumber + 141))
            case None => fail("expect(1, \"some message\") { 2 } didn't produce a file name and line number string", e)
          }
        case e =>
          fail("expect(1, \"some message\") { 2 } didn't produce a TestFailedError", e)
      }
    }

    it("should give the proper line on intercept[IllegalArgumentException] {}") {
      try {
        intercept[IllegalArgumentException] {}
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:" + (baseLineNumber + 156))
            case None => fail("intercept[IllegalArgumentException] {} didn't produce a file name and line number string", e)
          }
        case e =>
          fail("intercept[IllegalArgumentException] {} didn't produce a TestFailedError", e)
      }
    }

    it("should give the proper line on intercept[IllegalArgumentException] { throw new RuntimeException }") {
      try {
        intercept[IllegalArgumentException] { if (false) 1 else throw new RuntimeException }
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:" + (baseLineNumber + 171))
            case None => fail("intercept[IllegalArgumentException] { throw new RuntimeException } didn't produce a file name and line number string", e)
          }
        case e =>
          fail("intercept[IllegalArgumentException] { throw new RuntimeException } didn't produce a TestFailedError", e)
      }
    }

    it("bla bla bla") {
      // fail("message")
      // fail(new Throwable)
      // fail("message", new Throwable)
      // assert(1 === 2, "some message")
      // assert(1 === 2)
      intercept[IllegalArgumentException] { if (false) 1 else throw new RuntimeException }
      // intercept[IllegalArgumentException] {}
    }
  }
}
 

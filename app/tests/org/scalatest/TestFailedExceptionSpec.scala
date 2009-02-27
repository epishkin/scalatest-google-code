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

class TestFailedErrorSpec extends Spec with ShouldMatchers {

  val baseLineNumber = 22

  describe("The TestFailedException") {

    it("should give the proper line on fail()") {
      try {
        fail()
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 6))
            case None => fail("fail() didn't produce a file name and line number string: " + e.failedTestCodeFileNameAndLineNumberString, e)
          }
        case e =>
          fail("fail() didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on fail(\"message\")") {
      try {
        fail("some message")
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 21))
            case None => fail("fail(\"some message\") didn't produce a file name and line number string", e)
          }
        case e =>
          fail("fail(\"some message\") didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on fail(throwable)") {
      try {
        fail(new RuntimeException)
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 36))
            case None => fail("fail(throwable) didn't produce a file name and line number string", e)
          }
        case e =>
          fail("fail(throwable) didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on fail(\"some message\", throwable)") {
      try {
        fail("some message", new RuntimeException)
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 51))
            case None => fail("fail(\"some message\", throwable) didn't produce a file name and line number string", e)
          }
        case e =>
          fail("fail(\"some message\", throwable) didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on assert(false)") {
      try {
        assert(false)
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 66))
            case None => fail("assert(false) didn't produce a file name and line number string", e)
          }
        case e =>
          fail("assert(false) didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on assert(false, \"some message\")") {
      try {
        assert(false, "some message")
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 81))
            case None => fail("assert(false, \"some message\") didn't produce a file name and line number string", e)
          }
        case e =>
          fail("assert(false, \"some message\") didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on assert(1 === 2)") {
      try {
        assert(1 === 2)
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 96))
            case None => fail("assert(1 === 2) didn't produce a file name and line number string", e)
          }
        case e =>
          fail("assert(1 === 2) didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on assert(1 === 2, \"some message\")") {
      try {
        assert(1 === 2, "some message")
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 111))
            case None => fail("assert(1 === 2, \"some message\") didn't produce a file name and line number string", e)
          }
        case e =>
          fail("assert(1 === 2, \"some message\") didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on expect(1) { 2 }") {
      try {
        expect(1) { 2 }
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 126))
            case None => fail("expect(1) { 2 } didn't produce a file name and line number string", e)
          }
        case e =>
          fail("expect(1) { 2 } didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on expect(1, \"some message\") { 2 }") {
      try {
        expect(1, "some message") { 2 }
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 141))
            case None => fail("expect(1, \"some message\") { 2 } didn't produce a file name and line number string", e)
          }
        case e =>
          fail("expect(1, \"some message\") { 2 } didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on intercept[IllegalArgumentException] {}") {
      try {
        intercept[IllegalArgumentException] {}
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 156))
            case None => fail("intercept[IllegalArgumentException] {} didn't produce a file name and line number string", e)
          }
        case e =>
          fail("intercept[IllegalArgumentException] {} didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on intercept[IllegalArgumentException] { throw new RuntimeException }") {
      try {
        intercept[IllegalArgumentException] { if (false) 1 else throw new RuntimeException }
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 171))
            case None => fail("intercept[IllegalArgumentException] { throw new RuntimeException } didn't produce a file name and line number string", e)
          }
        case e =>
          fail("intercept[IllegalArgumentException] { throw new RuntimeException } didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on intercept(classOf[IllegalArgumentException]) {}") { // Once I remove this deprecated one, will delete this test
      try {
        intercept(classOf[IllegalArgumentException]) {}
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 186))
            case None => fail("intercept(classOf[IllegalArgumentException]) {} didn't produce a file name and line number string", e)
          }
        case e =>
          fail("intercept(classOf([IllegalArgumentException]) {} didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on intercept(classOf[IllegalArgumentException]) { throw new RuntimeException }") {
      try {
        intercept(classOf[IllegalArgumentException]) { if (false) 1 else throw new RuntimeException }
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 201))
            case None => fail("intercept(classOf([IllegalArgumentException]) { throw new RuntimeException } didn't produce a file name and line number string", e)
          }
        case e =>
          fail("intercept(classOf([IllegalArgumentException]) { throw new RuntimeException } didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on intercept(classOf[IllegalArgumentException], \"some message\") {}") { // Once I remove this deprecated one, will delete this test
      try {
        intercept(classOf[IllegalArgumentException], "some message") {}
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 216))
            case None => fail("intercept(classOf[IllegalArgumentException], \"some message\") {} didn't produce a file name and line number string", e)
          }
        case e =>
          fail("intercept(classOf([IllegalArgumentException], \"some message\") {} didn't produce a TestFailedException", e)
      }
    }

    it("should give the proper line on intercept(classOf[IllegalArgumentException], \"some message\") { throw new RuntimeException }") {
      try {
        intercept(classOf[IllegalArgumentException], "some message") { if (false) 1 else throw new RuntimeException }
      }
      catch {
        case e: TestFailedException =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedExceptionSpec.scala:" + (baseLineNumber + 231))
            case None => fail("intercept(classOf([IllegalArgumentException], \"some message\") { throw new RuntimeException } didn't produce a file name and line number string", e)
          }
        case e =>
          fail("intercept(classOf([IllegalArgumentException], \"some message\") { throw new RuntimeException } didn't produce a TestFailedException", e)
      }
    }

    it("bla bla bla") {
      // fail("message")
      // fail(new Throwable)
      // fail("message", new Throwable)
      // assert(1 === 2, "some message")
      // assert(1 === 2)
      // val cause0 = new IllegalArgumentException("this is cause 0")
      // val cause1 = new IllegalStateException("this is cause 1", cause0)
      // intercept[IllegalArgumentException] { if (false) 1 else throw new RuntimeException(cause1) }
      // intercept[IllegalArgumentException] {}
    }
  }
}
 
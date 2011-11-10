/*
 * Copyright 2001-2011 Artima, Inc.
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

import org.scalatest.ValueOnOption._
import org.scalatest.matchers.ShouldMatchers

class ValueOnOptionSpec extends Spec with ShouldMatchers {

  describe("value on Option") {

    it("should return the value inside an option if that option is defined") {

      val o: Option[String] = Some("hi there")
      o.value should be === ("hi there")
      o.value should startWith ("hi")
    }

    it("should throw TestFailedException if that option is empty") {

      val o: Option[String] = None
      val caught =
        evaluating {
          o.value should startWith ("hi")
        } should produce [TestFailedException]
      caught.failedCodeLineNumber.value should equal (thisLineNumber - 2)
      caught.failedCodeFileName.value should be ("ValueOnOptionSpec.scala")
    }
  }

   // TODO: This is copied and pasted from TestFailedExceptionSpec. Eventually
   // eliminate the duplication.
  //
  // Returns the line number from which this method was called.
  //
  // Found that on some machines it was in the third element in the stack
  // trace, and on others it was the fourth, so here we check the method
  // name of the third element to decide which of the two to return.
  //
  private def thisLineNumber = {
    val st = Thread.currentThread.getStackTrace

    if (!st(2).getMethodName.contains("thisLineNum"))
      st(2).getLineNumber
    else
      st(3).getLineNumber
  }
}


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

import org.scalatest.Eventually._
import org.scalatest.matchers.ShouldMatchers

class EventuallySpec extends Spec with ShouldMatchers with ValueOnOption {

  describe("The eventually construct") {

    it("should just return if the by-name returns normally") {

      eventually { 1 + 1 should equal (2) }
    }

    it("should invoke the function just once if the by-name returns normally the first time") {

      var count = 0
      eventually {
        count += 1
        1 + 1 should equal (2)
      }
      count should equal (1)
    }

    it("should invoke the function just once and return the result if the by-name returns normally the first time") {

      var count = 0
      val result =
        eventually {
          count += 1
          99
        }
      count should equal (1)
      result should equal (99)
    }

    it("should invoke the function five times if the by-name throws an exception four times before finally returning normally the fifth time") {

      var count = 0
      eventually {
        count += 1
        if (count < 5) throw new Exception
        1 + 1 should equal (2)
      }
      count should equal (5)
    }

    it("should eventually blow up with a TFE if the by-name continuously throws an exception") {

      val caught = evaluating {
        eventually { 1 + 1 should equal (3) }
      } should produce [TestFailedException]

      caught.message.value should be ("The code passed to eventually never returned normally.")
      caught.failedCodeLineNumber.value should equal (thisLineNumber - 4)
      caught.failedCodeFileName.value should be ("EventuallySpec.scala")
    }

    it("should by default invoke an always-failing by-name 100 times") {
      var count = 0
      evaluating {
        eventually {
          count += 1
          1 + 1 should equal (3)
        }
      } should produce [TestFailedException]
      count should equal (100)
    }

    it("should, if an alternate implicit MaxAttempts is provided, invoke an always-failing by-name by the specified number of times") {

      implicit val eventuallyConfig = EventuallyConfig(maxAttempts = 88)

      var count = 0
      evaluating {
        eventually {
          count += 1
          1 + 1 should equal (3)
        }
      } should produce [TestFailedException]
      count should equal (88)
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


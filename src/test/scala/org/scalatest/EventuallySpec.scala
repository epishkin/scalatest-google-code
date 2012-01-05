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
import org.scalatest.SharedHelpers.thisLineNumber

class EventuallySpec extends FunSpec with ShouldMatchers with ValueOnOption {

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

      caught.message.value should be (Resources("didNotEventuallySucceed", "100", "10"))
      caught.failedCodeLineNumber.value should equal (thisLineNumber - 4)
      caught.failedCodeFileName.value should be ("EventuallySpec.scala")
    }
    
    it("should provides correct stack depth when eventually is called from the overload method") {
      
      val caught1 = evaluating {
        eventually(maxAttempts(10), interval(1)) { 1 + 1 should equal (3) }
      } should produce [TestFailedException]
      caught1.failedCodeLineNumber.value should equal (thisLineNumber - 2)
      caught1.failedCodeFileName.value should be ("EventuallySpec.scala")
      
      val caught2 = evaluating {
        eventually(interval(1), maxAttempts(10)) { 1 + 1 should equal (3) }
      } should produce [TestFailedException]
      caught2.failedCodeLineNumber.value should equal (thisLineNumber - 2)
      caught2.failedCodeFileName.value should be ("EventuallySpec.scala")
      
      val caught3 = evaluating {
        eventually(maxAttempts(10)) { 1 + 1 should equal (3) }
      } should produce [TestFailedException]
      caught3.failedCodeLineNumber.value should equal (thisLineNumber - 2)
      caught3.failedCodeFileName.value should be ("EventuallySpec.scala")
      
      val caught4 = evaluating {
        eventually(interval(1)) { 1 + 1 should equal (3) }
      } should produce [TestFailedException]
      caught4.failedCodeLineNumber.value should equal (thisLineNumber - 2)
      caught4.failedCodeFileName.value should be ("EventuallySpec.scala")
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

    it("should, if an alternate explicit max attempts is provided, invoke an always-failing by-name by the specified number of times") {

      var count = 0
      evaluating {
        eventually (maxAttempts(77)) {
          count += 1
          1 + 1 should equal (3)
        } 
      } should produce [TestFailedException]
      count should equal (77)
    }

    it("should, if an alternate explicit max attempts is provided along with an explicit interval, invoke an always-failing by-name by the specified number of times, even if a different implicit is provided") {

      var count = 0
      evaluating {
        eventually (maxAttempts(78), interval(1)) {
          count += 1
          1 + 1 should equal (3)
        } 
      } should produce [TestFailedException]
      count should equal (78)
    }
  }
}


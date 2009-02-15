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

  describe("The FailedTestError") {
    it("should give the proper line on fail()") {
      try {
        fail()
      }
      catch {
        case e: TestFailedError =>
          e.failedTestCodeFileNameAndLineNumberString match {
            case Some(s) => s should equal ("TestFailedErrorSpec.scala:23")
            case None => fail("fail() didn't produce a file name and line number string", e)
          }
        case e =>
          fail("fail() didn't produce a TestFailedError", e)
      }
    }
  }
}
 

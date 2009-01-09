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

import prop.Checkers
import org.mockito.Mockito._

class LogicalMatcherExprSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion {

  class Clown {
    def hasBigRedNose = true
  }

  describe("Matcher expressions to the right of and") {
    describe("(A plain-old matcher)") {
      it("should short-circuit if left matcher doesn't match") {

        val mockClown = mock(classOf[Clown])

        intercept[AssertionError] {
          "hi" should (have length (1) and { mockClown.hasBigRedNose; have length (2) })
        }
 
        verify(mockClown, times(0)).hasBigRedNose
      }
    }

    describe("(have length N syntax)") {
      it("should short-circuit if left matcher doesn't match") {

        val mockClown = mock(classOf[Clown])

        intercept[AssertionError] {
          "hi" should (have length (1) and have length {mockClown.hasBigRedNose; 2})
        }

        verify(mockClown, times(0)).hasBigRedNose

        intercept[AssertionError] {
          "hi" should (have length (1) and {mockClown.hasBigRedNose; have length 2})
        }

        verify(mockClown, times(0)).hasBigRedNose
      }
    }
  }
}
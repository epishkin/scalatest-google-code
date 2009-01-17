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

    describe("(not have length N syntax)") {
      it("should short-circuit if left matcher doesn't match") {

        val mockClown = mock(classOf[Clown])

        intercept[AssertionError] {
          "hi" should (have length (1) and not have length {mockClown.hasBigRedNose; 1})
        }

        verify(mockClown, times(0)).hasBigRedNose

        intercept[AssertionError] {
          "hi" should (have length (1) and not {mockClown.hasBigRedNose; have length (1)})
        }

        verify(mockClown, times(0)).hasBigRedNose

        intercept[AssertionError] {
          "hi" should (have length (1) and {mockClown.hasBigRedNose; not have length (1)})
        }

        verify(mockClown, times(0)).hasBigRedNose
      }
    }

    describe("(have size N syntax)") {
      it("should short-circuit if left matcher doesn't match") {

        val mockClown = mock(classOf[Clown])

        intercept[AssertionError] {
          Array(1, 2) should (have size (1) and have size {mockClown.hasBigRedNose; 2})
        }

        verify(mockClown, times(0)).hasBigRedNose

        intercept[AssertionError] {
          "hi" should (have size (1) and {mockClown.hasBigRedNose; have size 2})
        }

        verify(mockClown, times(0)).hasBigRedNose
      }
    }

    describe("(not have size N syntax)") {
      it("should short-circuit if left matcher doesn't match") {

        val mockClown = mock(classOf[Clown])

        intercept[AssertionError] {
          Array(1, 2) should (have size (1) and not have size {mockClown.hasBigRedNose; 1})
        }

        verify(mockClown, times(0)).hasBigRedNose

        intercept[AssertionError] {
          Array(1, 2) should (have size (1) and not {mockClown.hasBigRedNose; have size (1)})
        }

        verify(mockClown, times(0)).hasBigRedNose

        intercept[AssertionError] {
          Array(1, 2) should (have size (1) and {mockClown.hasBigRedNose; not have size (1)})
        }

        verify(mockClown, times(0)).hasBigRedNose
      }
    }

    describe("(equal N syntax)") {
      it("should short-circuit if left matcher doesn't match") {

        val mockClown = mock(classOf[Clown])

        intercept[AssertionError] {
          "hi" should (equal ("ho") and equal {mockClown.hasBigRedNose; "ho"})
        }

        verify(mockClown, times(0)).hasBigRedNose

        intercept[AssertionError] {
          "hi" should (equal ("ho") and {mockClown.hasBigRedNose; equal ("ho")})
        }

        verify(mockClown, times(0)).hasBigRedNose
      }
    }
  }

  describe("(not equal N syntax)") {
    it("should short-circuit if left matcher doesn't match") {

      val mockClown = mock(classOf[Clown])

      intercept[AssertionError] {
        "hi" should (equal ("ho") and not equal {mockClown.hasBigRedNose; "ho"})
      }

      verify(mockClown, times(0)).hasBigRedNose

      intercept[AssertionError] {
        "hi" should (equal ("ho") and {mockClown.hasBigRedNose; not equal ("ho")})
      }

      verify(mockClown, times(0)).hasBigRedNose
    }
  }

  describe("Matcher expressions to the right of or") {
    describe("(A plain-old matcher)") {
      it("should short-circuit if left matcher does match") {

        val mockClown = mock(classOf[Clown])

        "hi" should (have length (2) or { mockClown.hasBigRedNose; have length (2) })
 
        verify(mockClown, times(0)).hasBigRedNose
      }
    }

    describe("(have length N syntax)") {
      it("should short-circuit if left matcher does match") {

        val mockClown = mock(classOf[Clown])

        "hi" should (have length (2) or have length {mockClown.hasBigRedNose; 2})

        verify(mockClown, times(0)).hasBigRedNose

        "hi" should (have length (2) or {mockClown.hasBigRedNose; have length 2})

        verify(mockClown, times(0)).hasBigRedNose
      }
    }

    describe("(not have length N syntax)") {
      it("should short-circuit if left matcher does match") {

        val mockClown = mock(classOf[Clown])

        "hi" should (have length (2) or not have length {mockClown.hasBigRedNose; 1})

        verify(mockClown, times(0)).hasBigRedNose

        "hi" should (have length (2) or not {mockClown.hasBigRedNose; have length (1)})

        verify(mockClown, times(0)).hasBigRedNose

        "hi" should (have length (2) or {mockClown.hasBigRedNose; not have length (1)})

        verify(mockClown, times(0)).hasBigRedNose
      }
    }

    describe("(have size N syntax)") {
      it("should short-circuit if left matcher does match") {

        val mockClown = mock(classOf[Clown])

        Array(1, 2) should (have size (2) or have size {mockClown.hasBigRedNose; 2})

        verify(mockClown, times(0)).hasBigRedNose

        Array(1, 2) should (have size (2) or {mockClown.hasBigRedNose; have size 2})

        verify(mockClown, times(0)).hasBigRedNose
      }
    }

    describe("(not have size N syntax)") {
      it("should short-circuit if left matcher does match") {

        val mockClown = mock(classOf[Clown])

        Array(1, 2) should (have size (2) or not have size {mockClown.hasBigRedNose; 1})

        verify(mockClown, times(0)).hasBigRedNose

        Array(1, 2) should (have size (2) or not {mockClown.hasBigRedNose; have size (1)})

        verify(mockClown, times(0)).hasBigRedNose

        Array(1, 2) should (have size (2) or {mockClown.hasBigRedNose; not have size (1)})

        verify(mockClown, times(0)).hasBigRedNose
      }
    }

    describe("(equal N syntax)") {
      it("should short-circuit if left matcher does match") {

        val mockClown = mock(classOf[Clown])

        "hi" should (equal ("hi") or equal {mockClown.hasBigRedNose; "ho"})

        verify(mockClown, times(0)).hasBigRedNose

        "hi" should (equal ("hi") or {mockClown.hasBigRedNose; equal ("ho")})

        verify(mockClown, times(0)).hasBigRedNose
      }
    }

    describe("(not equal N syntax)") {
      it("should short-circuit if left matcher does match") {

        val mockClown = mock(classOf[Clown])

        "hi" should (equal ("hi") or not equal {mockClown.hasBigRedNose; "ho"})

        verify(mockClown, times(0)).hasBigRedNose

        "hi" should (equal ("hi") or {mockClown.hasBigRedNose; not equal ("ho")})

        verify(mockClown, times(0)).hasBigRedNose
      }
    }
  }
}

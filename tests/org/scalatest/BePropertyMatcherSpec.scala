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
import org.scalacheck._
import Arbitrary._
import Prop._
import scala.reflect.BeanProperty

class BePropertyMatcherSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion with BookPropertyMatchers {

  // Checking for a specific size
  describe("The be property syntax") {

    describe("on an object with properties") {

      val book = new Book("A Tale of Two Cities", "Dickens", 1859, 45, true)
      val badBook = new Book("A Tale of Two Cities", "Dickens", 1859, 45, false)
      val bookshelf = new Bookshelf(book, badBook, book)

      it("should do nothing if a BePropertyMatcher is used and the property is true") {
          book should be a (goodRead)
      }

      it("should throw TestFailedException if a \"be property\" matcher is used with be and the property is false") {

        val caught1 = intercept[TestFailedException] {
          badBook should be a (goodRead)
        }
        assert(caught1.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,false) was not a goodRead")
      }

      it("should throw TestFailedException if a \"be odd\" matcher is used with be and the Int isn't odd") {

        class OddMatcher extends BeMatcher[Int] {
          def apply(left: Int): MatchResult = {
            MatchResult(
              left % 2 == 1,
              left.toString + " was even",
              left.toString + " was odd"
            )
          }
        }
        val odd = new OddMatcher

        val caught1 = intercept[TestFailedException] {
          4 should be (odd)
        }
        assert(caught1.getMessage === "4 was even")

        val even = not (odd)

        val caught2 = intercept[TestFailedException] {
          5 should be (even)
        }
        assert(caught2.getMessage === "5 was odd")
      }
    }
  }
}

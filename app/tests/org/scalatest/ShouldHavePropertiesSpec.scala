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

trait BookPropertyVerifiers { this: Matchers => 

  class Book(
    var title: String,
    val author: String,
    val pubYear: Int
  )

  class TitleVerifier(expectedValue: String) extends PropertyVerifier[Book, String] {
    def apply(book: Book) = {
      if (book.title != expectedValue) {
        Some(new PropertyVerificationResult("title", expectedValue, book.title))
      }
      else None
    }
  }

  def title(expectedValue: String) = new TitleVerifier(expectedValue)

  class AuthorVerifier(expectedValue: String) extends PropertyVerifier[Book, String] {
    def apply(book: Book) = {
      if (book.author != expectedValue) {
        Some(new PropertyVerificationResult("author", expectedValue, book.author))
      }
      else None
    }
  }

  def author(expectedValue: String) = new AuthorVerifier(expectedValue)

  class PubYearVerifier(expectedValue: Int) extends PropertyVerifier[Book, Int] {
    def apply(book: Book) = {
      if (book.pubYear != expectedValue) {
        Some(new PropertyVerificationResult("pubYear", expectedValue, book.pubYear))
      }
      else None
    }
  }

  def pubYear(expectedValue: Int) = new PubYearVerifier(expectedValue)
}

class ShouldHavePropertiesSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion with BookPropertyVerifiers {

  // Checking for a specific size
  describe("The 'have {' syntax") {

    describe("on an object with Scala-style properties") {

      it ("should work") {

        val book = new Book("A Tale of Two Cities", "Dickens", 1859)

        book should have (
          title ("A Tale of Two Cities"),
          'author ("Gibson"),
          pubYear (1859)
        )
      }
    }
  }
}

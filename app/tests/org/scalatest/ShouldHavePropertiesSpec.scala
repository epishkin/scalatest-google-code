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
    val pubYear: Int,
    val length: Int
  )

  class TitleVerifier(expectedValue: String) extends PropertyVerifier[Book, String] {
    def apply(book: Book) = {
      new PropertyVerificationResult(book.title == expectedValue, "title", expectedValue, book.title)
    }
  }

  def title(expectedValue: String) = new TitleVerifier(expectedValue)

  class AuthorVerifier(expectedValue: String) extends PropertyVerifier[Book, String] {
    def apply(book: Book) = {
      new PropertyVerificationResult(book.author == expectedValue, "author", expectedValue, book.author)
    }
  }

  def author(expectedValue: String) = new AuthorVerifier(expectedValue)

  class PubYearVerifier(expectedValue: Int) extends PropertyVerifier[Book, Int] {
    def apply(book: Book) = {
      new PropertyVerificationResult(book.pubYear == expectedValue, "pubYear", expectedValue, book.pubYear)
    }
  }

  def pubYear(expectedValue: Int) = new PubYearVerifier(expectedValue)
}

class ShouldHavePropertiesSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion with BookPropertyVerifiers {

  // Checking for a specific size
  describe("The 'have {' syntax") {

    describe("on an object with Scala-style properties") {

      val book = new Book("A Tale of Two Cities", "Dickens", 1859, 45)

      it ("should do nothing if all the properties match") {
        book should have (
          title ("A Tale of Two Cities"),
          author ("Dickens"),
          pubYear (1859)
        )
      }

      it ("should throw TestFailedException if at least one of the properties don't match") {

        val caught = intercept[TestFailedException] {
          book should have (
            title ("A Tale of Two Cities"),
            author ("Gibson"),
            pubYear (1859)
          )
        }
        assert(caught.getMessage === "Expected property \"author\" to have value \"Gibson\", but it had value \"Dickens\".")
      }

      it ("should throw TestFailedException if at least one of the properties don't match, when using symbols") {

        val caught1 = intercept[TestFailedException] {
          book should have (
            title ("A Tale of Two Cities"),
            'author ("Gibson"),
            pubYear (1859)
          )
        }
        assert(caught1.getMessage === "Expected property \"author\" to have value \"Gibson\", but it had value \"Dickens\".")

        val caught2 = intercept[TestFailedException] {
          book should have (
            'title ("A Tale of Two Cities"),
            'author ("Dickens"),
            'pubYear (1959)
          )
        }
        assert(caught2.getMessage === "Expected property \"pubYear\" to have value 1959, but it had value 1859.")
      }

      it ("should throw TestFailedException if there's just one property and it doesn't match") {

        val caught1 = intercept[TestFailedException] {
          book should have (author ("Gibson"))
        }
        assert(caught1.getMessage === "Expected property \"author\" to have value \"Gibson\", but it had value \"Dickens\".")
      }

      it ("should throw TestFailedException if there's just one property and it doesn't match, when using a symbol") {

        val caught1 = intercept[TestFailedException] {
          book should have ('author ("Gibson"))
        }
        assert(caught1.getMessage === "Expected property \"author\" to have value \"Gibson\", but it had value \"Dickens\".")
      }

      it ("should work with length not a symbol without anything special, in case someone forgets you don't need the parens with length") {

        val caught1 = intercept[TestFailedException] {
          book should have (length (43))
        }
        assert(caught1.getMessage === "Expected property \"length\" to have value 43, but it had value 45.")
      }

      /*
      This does not compile, which is what I want
      it ("should not compile if you don't enter any verifiers") {
        book should have ()
      }
      */
    }
  }
}

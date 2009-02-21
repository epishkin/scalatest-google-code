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

trait BookPropertyMatchers { this: Matchers => 

  case class Book(
    var title: String,
    val author: String,
    val pubYear: Int,
    val length: Int,
    val isGoodRead: Boolean
  )

  case class Bookshelf(
    val book1: Book,
    val book2: Book,
    val book3: Book
  )

  class BookPropertiesMatcher(firstPropertyMatcher: HavePropertyMatcher[Book, _], propertyMatchers: HavePropertyMatcher[Book, _]*)
      extends HavePropertyMatcher[Bookshelf, Book] {

    def apply(bookshelf: Bookshelf) = {
      val propertyMatcherList = firstPropertyMatcher :: propertyMatchers.toList
      val propertyMatchResults = // This is the list of results
        for (propertyMatcher <- propertyMatcherList) yield
          propertyMatcher(bookshelf.book1)

      val firstFailure = propertyMatchResults.find(_.matches == false)
      firstFailure match {
        case Some(failure) =>
          new HavePropertyMatchResult(false, "book1." + failure.propertyName, failure.expectedValue, failure.actualValue)
        case None =>
          new HavePropertyMatchResult(true, "book1", null, null) // What to do here?
      }
    }
  }

  def book1(firstPropertyMatcher: HavePropertyMatcher[Book, _], propertyMatchers: HavePropertyMatcher[Book, _]*) =
    new BookPropertiesMatcher(firstPropertyMatcher, propertyMatchers: _*)

  class TitleMatcher(expectedValue: String) extends HavePropertyMatcher[Book, String] {
    def apply(book: Book) = {
      new HavePropertyMatchResult(book.title == expectedValue, "title", expectedValue, book.title)
    }
  }

  def title(expectedValue: String) = new TitleMatcher(expectedValue)

  class AuthorMatcher(expectedValue: String) extends HavePropertyMatcher[Book, String] {
    def apply(book: Book) = {
      new HavePropertyMatchResult(book.author == expectedValue, "author", expectedValue, book.author)
    }
  }

  def author(expectedValue: String) = new AuthorMatcher(expectedValue)

  class PubYearMatcher(expectedValue: Int) extends HavePropertyMatcher[Book, Int] {
    def apply(book: Book) = {
      new HavePropertyMatchResult(book.pubYear == expectedValue, "pubYear", expectedValue, book.pubYear)
    }
  }

  def pubYear(expectedValue: Int) = new PubYearMatcher(expectedValue)

  class GoodReadMatcher(expectedValue: Boolean) extends HavePropertyMatcher[Book, Boolean] {
    def apply(book: Book) = {
      new HavePropertyMatchResult(book.isGoodRead == expectedValue, "goodRead", expectedValue, book.isGoodRead)
    }
  }

  class GoodReadBePropertyMatcher extends BePropertyMatcher[Book] {
    def apply(book: Book) = {
      new BePropertyMatchResult(book.isGoodRead, "goodRead")
    }
  }

  def goodRead(expectedValue: Boolean) = new GoodReadMatcher(expectedValue)
  def goodRead = new GoodReadBePropertyMatcher
}

class ShouldHavePropertiesSpec extends Spec with ShouldMatchers with Checkers with ReturnsNormallyThrowsAssertion with BookPropertyMatchers {

  // Checking for a specific size
  describe("The 'have {' syntax") {

    describe("on an object with Scala-style properties") {

      val book = new Book("A Tale of Two Cities", "Dickens", 1859, 45, true)
      val badBook = new Book("A Tale of Two Cities", "Dickens", 1859, 45, false)
      val bookshelf = new Bookshelf(book, badBook, book)

      it("should do nothing if all the properties match") {
        book should have (
          title ("A Tale of Two Cities"),
          author ("Dickens"),
          pubYear (1859)
        )
      }

      it("should throw TestFailedException if at least one of the properties don't match") {

        val caught = intercept[TestFailedException] {
          book should have (
            title ("A Tale of Two Cities"),
            author ("Gibson"),
            pubYear (1859)
          )
        }
        assert(caught.getMessage === "Expected property \"author\" to have value \"Gibson\", but it had value \"Dickens\".")
      }

      it("should throw TestFailedException if at least one of the properties don't match, when using symbols") {

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

      it("should throw TestFailedException if there's just one property and it doesn't match") {

        val caught1 = intercept[TestFailedException] {
          book should have (author ("Gibson"))
        }
        assert(caught1.getMessage === "Expected property \"author\" to have value \"Gibson\", but it had value \"Dickens\".")
      }

      it("should throw TestFailedException if there's just one property and it doesn't match, when using a symbol") {

        val caught1 = intercept[TestFailedException] {
          book should have ('author ("Gibson"))
        }
        assert(caught1.getMessage === "Expected property \"author\" to have value \"Gibson\", but it had value \"Dickens\".")
      }

      it("should throw TestFailedException if a \"be true\" matcher is used with be and the property is false") {

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

      it("should throw TestFailedException if a nested property matcher expression is used and a nested property doesn't match") {

        // I'm not too hot on this syntax, but can't prevent it and wouldn't want to. If people want do to nested property
        // checks, they can do it this way.
        val caught1 = intercept[TestFailedException] {
          bookshelf should have (
            book1 (
              title ("A Tale of Two Cities"),
              author ("Gibson"),
              pubYear (1859)
            )
          )
        }
        assert(caught1.getMessage === "Expected property \"book1.author\" to have value \"Gibson\", but it had value \"Dickens\".")
      }

/*
      it("should work with length not a symbol without anything special, in case someone forgets you don't need the parens with length") {

        val caught1 = intercept[TestFailedException] {
          book should have (length (43))
        }
        assert(caught1.getMessage === "Expected property \"length\" to have value 43, but it had value 45.")
      }
*/

/*
I decided not to support this syntax in 0.9.5, and maybe never. It is not clear to me that it is
readable enough. I can't prevent someone from making HavePropertyMatchers to do this kind of thing,
and that's fine. It actually gives them a way to do it if they want to do it.
      it("should throw TestFailedException if a nested property matcher expression with a symbol is used and a nested property doesn't match") {

        val caught1 = intercept[TestFailedException] {
          bookshelf should have (
            'book1 (
              title ("A Tale of Two Cities"),
              author ("Gibson"),
              pubYear (1859)
            )
          )
        }
        assert(caught1.getMessage === "Expected property \"book1.author\" to have value \"Gibson\", but it had value \"Dickens\".")
      }
*/

      /*
      This does not compile, which is what I want
      it("should not compile if you don't enter any verifiers") {
        book should have ()
      }
      */
    }
  }
}

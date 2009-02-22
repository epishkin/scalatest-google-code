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

trait BookPropertyMatchers { this: Matchers => 

  case class Book(
    var title: String,
    val author: String,
    val pubYear: Int,
    val length: Int,
    val isGoodRead: Boolean
  )

/*
  case class JavaBook(
    @BeanProperty var title: String,
    private val author: String,
    @BeanProperty val pubYear: Int,
    private var length: Int,
    private val goodRead: Boolean
  ) {
    def getAuthor: String = author
    def getLength: Int = length
    def setLength(len: Int) { length = len }
    def isGoodRead: Boolean = goodRead
  }
*/

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
  describe("The 'have (' syntax") {

    describe("on an object with properties") {

      val book = new Book("A Tale of Two Cities", "Dickens", 1859, 45, true)
      val badBook = new Book("A Tale of Two Cities", "Dickens", 1859, 45, false)
      val bookshelf = new Bookshelf(book, badBook, book)

      it("should do nothing if there's just one property and it matches") {
        book should have (title ("A Tale of Two Cities"))
        book should have ('title ("A Tale of Two Cities"))
      }

      it("should do nothing if all the properties match") {
        book should have (
          title ("A Tale of Two Cities"),
          author ("Dickens"),
          pubYear (1859)
        )
        book should have (
          'title ("A Tale of Two Cities"),
          'author ("Dickens"),
          'pubYear (1859)
        )
      }

      it("should do nothing if there's just one property and it does not match, when used with not") {
        book should not have (title ("One Hundred Years of Solitude"))
        book should not have ('title ("One Hundred Years of Solitude"))
      }

      it("should do nothing if none of the properties match, when used with not") {
        book should not have (
          title ("Moby Dick"),
          author ("Melville"),
          pubYear (1851)
        )
        book should not have (
          'title ("Moby Dick"),
          'author ("Melville"),
          'pubYear (1851)
        )
      }

      it("should throw TestFailedException if there's just one property and it doesn't match") {

        val caught1 = intercept[TestFailedException] {
          book should have (author ("Gibson"))
        }
        assert(caught1.getMessage === "Expected property author to have value \"Gibson\", but it had value \"Dickens\", on object Book(A Tale of Two Cities,Dickens,1859,45,true).")

        val caught2 = intercept[TestFailedException] {
          book should have ('author ("Gibson"))
        }
        assert(caught2.getMessage === "Expected property author to have value \"Gibson\", but it had value \"Dickens\", on object Book(A Tale of Two Cities,Dickens,1859,45,true).")
      }

      it("should throw TestFailedException if at least one of the properties doesn't match") {

        val caught1 = intercept[TestFailedException] {
          book should have (
            title ("A Tale of Two Cities"),
            author ("Gibson"),
            pubYear (1859)
          )
        }
        assert(caught1.getMessage === "Expected property author to have value \"Gibson\", but it had value \"Dickens\", on object Book(A Tale of Two Cities,Dickens,1859,45,true).")

        val caught2 = intercept[TestFailedException] {
          book should have (
            title ("A Tale of Two Cities"),
            'author ("Gibson"),
            pubYear (1859)
          )
        }
        assert(caught2.getMessage === "Expected property author to have value \"Gibson\", but it had value \"Dickens\", on object Book(A Tale of Two Cities,Dickens,1859,45,true).")

        val caught3 = intercept[TestFailedException] {
          book should have (
            'title ("A Tale of Two Cities"),
            'author ("Dickens"),
            'pubYear (1959)
          )
        }
        assert(caught3.getMessage === "Expected property pubYear to have value 1959, but it had value 1859, on object Book(A Tale of Two Cities,Dickens,1859,45,true).")
      }

      it("should throw TestFailedException if there's just one property and it matches, when used with not") {

        val caught1 = intercept[TestFailedException] {
          book should not have (author ("Dickens"))
        }
        assert(caught1.getMessage === "Property author had its expected value \"Dickens\", on object Book(A Tale of Two Cities,Dickens,1859,45,true).")

        val caught2 = intercept[TestFailedException] {
          book should not have ('author ("Dickens"))
        }
        assert(caught2.getMessage === "Property author had its expected value \"Dickens\", on object Book(A Tale of Two Cities,Dickens,1859,45,true).")
      }

/*
I've been doing this wrongly. not (matcher) needs to yield the opposite result as (matcher) itself, and
that means that not (matcher) will be true if at least one 

title/author/pubYear matches | have | not have
0 0 0 | 0 | 1
0 0 1 | 0 | 1
0 1 0 | 0 | 1
0 1 1 | 0 | 1
1 0 0 | 0 | 1
1 0 1 | 0 | 1
1 1 0 | 0 | 1
1 1 1 | 1 | 0

So 'not have" means that at least one is false, not all are false.

To reduce the number of tests cases just use two:

title/author matches | have | have not
0 0 | 0 | 1
0 1 | 0 | 1
1 0 | 0 | 1
1 1 | 1 | 0


have matches (1 1) All properties matched.
have does not match (0 0, 0 1, 1 0) the (first property found that doesn't match) didn't match
not have matches (0 0, 0 1, 1 0) the (first property found that doesn't match), as expected
not have does not match (1, 1) All properties matched.
*/
      it("should throw TestFailedException if all of the properties match, when used with not") {
        val caught1 = intercept[TestFailedException] {
          book should not have (
            title ("A Tale of Two Cities"),
            author ("Dickens")
          )
        }
        assert(caught1.getMessage === "All properties had their expected values, respectively, on object Book(A Tale of Two Cities,Dickens,1859,45,true).")
      }
/*
      it("should throw TestFailedException if at least one of the properties matches, when used with not") {

        val caught1 = intercept[TestFailedException] {
          book should not have (
            title ("Moby Dick"),
            author ("Dickens"),
            pubYear (1851)
          )
        }
        assert(caught1.getMessage === "Expected property author to NOT have value "Dickens", but it did have that value, on object Book(A Tale of Two Cities,Dickens,1859,45,true).")

        val caught2 = intercept[TestFailedException] {
          book should not have (
            title ("Moby Dick"),
            'author ("Dickens"),
            pubYear (1851)
          )
        }
        assert(caught2.getMessage === "")

        val caught3 = intercept[TestFailedException] {
          book should not have (
            'title ("Moby Dick"),
            'author ("Gibson"),
            'pubYear (1959)
          )
        }
        assert(caught3.getMessage === "")
      }
*/

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
        assert(caught1.getMessage === "Expected property book1.author to have value \"Gibson\", but it had value \"Dickens\", on object Bookshelf(Book(A Tale of Two Cities,Dickens,1859,45,true),Book(A Tale of Two Cities,Dickens,1859,45,false),Book(A Tale of Two Cities,Dickens,1859,45,true)).")
      }

      it("should work with length not a symbol without anything special, in case someone forgets you don't need the parens with length") {

        val caught1 = intercept[TestFailedException] {
          book should have (length (43))
        }
        assert(caught1.getMessage === "Expected property length to have value 43, but it had value 45, on object Book(A Tale of Two Cities,Dickens,1859,45,true).")
      }

      it("should throw TestFailedException if length used in parens but the length property is not an integral type") {

        class LengthSeven {
          def length = "seven"
        }

        val caught1 = intercept[TestFailedException] {
          (new LengthSeven) should have (length (43))
        }
        assert(caught1.getMessage === "The length property was none of Byte, Short, Int, or Long.")
      }

      it("should work with size not a symbol without anything special, in case someone forgets you don't need the parens with size") {

        case class Size(val size: Int)

        val caught1 = intercept[TestFailedException] {
          (new Size(7)) should have (size (43))
        }
        assert(caught1.getMessage === "Expected property size to have value 43, but it had value 7, on object Size(7).")
      }

      it("should throw TestFailedException if size used in parens but the size property is not an integral type") {

        class SizeSeven {
          def size = "seven"
        }

        val caught1 = intercept[TestFailedException] {
          (new SizeSeven) should have (size (43))
        }
        assert(caught1.getMessage === "The size property was none of Byte, Short, Int, or Long.")
      }

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
        assert(caught1.getMessage === "Expected property book1.author to have value \"Gibson\", but it had value \"Dickens\".")
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

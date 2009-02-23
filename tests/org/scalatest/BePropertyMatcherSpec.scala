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
  describe("The be (BePropertyMatcher) syntax") {

    case class MyFile(
      val name: String,
      val file: Boolean,
      val isDirectory: Boolean
    )

    class FileBePropertyMatcher extends BePropertyMatcher[MyFile] {
      def apply(file: MyFile) = {
        new BePropertyMatchResult(file.file, "file")
      }
    }

    class DirectoryBePropertyMatcher extends BePropertyMatcher[MyFile] {
      def apply(file: MyFile) = {
        new BePropertyMatchResult(file.isDirectory, "directory")
      }
    }

    def file = new FileBePropertyMatcher
    def directory = new DirectoryBePropertyMatcher

    val myFile = new MyFile("temp.txt", true, false)

    val book = new Book("A Tale of Two Cities", "Dickens", 1859, 45, true)
    val badBook = new Book("A Tale of Two Cities", "Dickens", 1859, 45, false)
    val bookshelf = new Bookshelf(book, badBook, book)

    it("should do nothing if the property is true") {
      book should be (goodRead)
      book should be a (goodRead)
      book should be an (goodRead)
    }

    it("should throw TestFailedException if the property is false") {

      val caught1 = intercept[TestFailedException] {
        badBook should be (goodRead)
      }
      assert(caught1.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,false) was not goodRead")

      val caught2 = intercept[TestFailedException] {
        badBook should be a (goodRead)
      }
      assert(caught2.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,false) was not a goodRead")

      val caught3 = intercept[TestFailedException] {
        badBook should be an (goodRead)
      }
      assert(caught3.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,false) was not an goodRead")
    }

    it("should do nothing if the property is false, when used with not") {
      badBook should not be (goodRead)
      badBook should not be a (goodRead)
      badBook should not be an (goodRead)
    }

    it("should throw TestFailedException if the property is true, when used with not") {

      val caught1 = intercept[TestFailedException] {
        book should not be (goodRead)
      }
      assert(caught1.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,true) was goodRead")

      val caught2 = intercept[TestFailedException] {
        book should not be a (goodRead)
      }
      assert(caught2.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,true) was a goodRead")

      val caught3 = intercept[TestFailedException] {
        book should not be an (goodRead)
      }
      assert(caught3.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,true) was an goodRead")

      val caught4 = intercept[TestFailedException] {
        book should not (be (goodRead))
      }
      assert(caught4.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,true) was goodRead")

      val caught5 = intercept[TestFailedException] {
        book should not (be a (goodRead))
      }
      assert(caught5.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,true) was a goodRead")

      val caught6 = intercept[TestFailedException] {
        book should not (be an (goodRead))
      }
      assert(caught6.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,true) was an goodRead")

      val caught7 = intercept[TestFailedException] {
        book should (not (be (goodRead)))
      }
      assert(caught7.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,true) was goodRead")

      val caught8 = intercept[TestFailedException] {
        book should (not (be a (goodRead)))
      }
      assert(caught8.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,true) was a goodRead")

      val caught9 = intercept[TestFailedException] {
        book should (not (be an (goodRead)))
      }
      assert(caught9.getMessage === "Book(A Tale of Two Cities,Dickens,1859,45,true) was an goodRead")
    }

    it("should do nothing if the the property returns true, when used in a logical-and expression") {

      myFile should ((be (file)) and (be (file)))
      myFile should (be (file) and (be (file)))
      myFile should (be (file) and be (file))

      myFile should ((be a (file)) and (be a (file)))
      myFile should (be a (file) and (be a (file)))
      myFile should (be a (file) and be a (file))

      myFile should ((be an (file)) and (be an (file)))
      myFile should (be an (file) and (be an (file)))
      myFile should (be an (file) and be an (file))
    }

    it("should do nothing if the property returns true, when used in a logical-or expression") {

      // second true
      myFile should ((be (directory)) or (be (file)))
      myFile should (be (directory) or (be (file)))
      myFile should (be (directory) or be (file))

      myFile should ((be a (directory)) or (be a (file)))
      myFile should (be a (directory) or (be a (file)))
      myFile should (be a (directory) or be a (file))

      myFile should ((be an (directory)) or (be an (file)))
      myFile should (be an (directory) or (be an (file)))
      myFile should (be an (directory) or be an (file))

      // first true
      myFile should ((be (file)) or (be (directory)))
      myFile should (be (file) or (be (directory)))
      myFile should (be (file) or be (directory))

      myFile should ((be a (file)) or (be a (directory)))
      myFile should (be a (file) or (be a (directory)))
      myFile should (be a (file) or be a (directory))

      myFile should ((be an (file)) or (be an (directory)))
      myFile should (be an (file) or (be an (directory)))
      myFile should (be an (file) or be an (directory))

      // both true
      myFile should ((be (file)) or (be (file)))
      myFile should (be (file) or (be (file)))
      myFile should (be (file) or be (file))

      myFile should ((be a (file)) or (be a (file)))
      myFile should (be a (file) or (be a (file)))
      myFile should (be a (file) or be a (file))

      myFile should ((be an (file)) or (be an (file)))
      myFile should (be an (file) or (be an (file)))
      myFile should (be an (file) or be an (file))
    }

    it("should do nothing if the property returns false, when used in a logical-and expression with not") {

      myFile should (not (be (directory)) and not (be (directory)))
      myFile should ((not be (directory)) and (not be (directory)))
      myFile should (not be (directory) and not be (directory))

      myFile should (not (be a (directory)) and not (be a (directory)))
      myFile should ((not be a (directory)) and (not be a (directory)))
      myFile should (not be a (directory) and not be a (directory))

      myFile should (not (be an (directory)) and not (be an (directory)))
      myFile should ((not be an (directory)) and (not be an (directory)))
      myFile should (not be an (directory) and not be an (directory))
    }

    it("should do nothing if the property returns false, when used in a logical-or expression with not") {

      // first true
      myFile should (not (be (directory)) or not (be (file)))
      myFile should ((not be (directory)) or (not be (file)))
      myFile should (not be (directory) or not be (file))

      myFile should (not (be a (directory)) or not (be a (file)))
      myFile should ((not be a (directory)) or (not be a (file)))
      myFile should (not be a (directory) or not be a (file))

      myFile should (not (be an (directory)) or not (be an (file)))
      myFile should ((not be an (directory)) or (not be an (file)))
      myFile should (not be an (directory) or not be an (file))

      // second true
      myFile should (not (be (file)) or not (be (directory)))
      myFile should ((not be (file)) or (not be (directory)))
      myFile should (not be (file) or not be (directory))

      myFile should (not (be a (file)) or not (be a (directory)))
      myFile should ((not be a (file)) or (not be a (directory)))
      myFile should (not be a (file) or not be a (directory))

      myFile should (not (be an (file)) or not (be an (directory)))
      myFile should ((not be an (file)) or (not be an (directory)))
      myFile should (not be an (file) or not be an (directory))

      // both true
      myFile should (not (be (directory)) or not (be (directory)))
      myFile should ((not be (directory)) or (not be (directory)))
      myFile should (not be (directory) or not be (directory))

      myFile should (not (be a (directory)) or not (be a (directory)))
      myFile should ((not be a (directory)) or (not be a (directory)))
      myFile should (not be a (directory) or not be a (directory))

      myFile should (not (be an (directory)) or not (be an (directory)))
      myFile should ((not be an (directory)) or (not be an (directory)))
      myFile should (not be an (directory) or not be an (directory))
    }

/*
    it("should do nothing if the property returns false, when used in a logical-or expression with not") {

      myFile should (not (be (file)) or not (be (file)))
      myFile should ((not be (file)) or (not be (file)))
      myFile should (not be (file) or not be (file))

      myFile should (not (be (directory)) or not (be (file)))
      myFile should ((not be (directory)) or (not be (file)))
      myFile should (not be (directory) or not be (file))
    }
*/
  }
}
